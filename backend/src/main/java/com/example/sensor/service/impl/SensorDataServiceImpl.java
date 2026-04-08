package com.example.sensor.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.sensor.entity.SensorData;
import com.example.sensor.mapper.SensorDataMapper;
import com.example.sensor.service.SensorDataService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 传感器数据 Service 业务实现
 */
@Service
public class SensorDataServiceImpl extends ServiceImpl<SensorDataMapper, SensorData> implements SensorDataService {

    private static final Logger logger = LoggerFactory.getLogger(SensorDataServiceImpl.class);

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    private static final String CACHE_KEY = "sensor:data:grouped";
    private static final long CACHE_EXPIRE = 5; // 缓存过期时间（分钟）

    /**
     * 按设备分组数据，不再聚合，直接返回所有数据
     * 从 realTime 字段中提取月份和日期
     */
    private Map<String, List<SensorData>> aggregateData(List<SensorData> allData) {
        long startTime = System.currentTimeMillis();
        Map<String, List<SensorData>> result = new HashMap<>();

        logger.info("开始聚合数据，总数据量: {}", allData.size());

        // 按设备分组
        long groupStartTime = System.currentTimeMillis();
        Map<String, List<SensorData>> groupedByDevice = allData.stream()
                .collect(Collectors.groupingBy(SensorData::getDevice));
        long groupDuration = System.currentTimeMillis() - groupStartTime;
        logger.info("按设备分组耗时: {}ms, 设备数: {}", groupDuration, groupedByDevice.size());

        // 对每个设备的数据按时间戳排序
        long processStartTime = System.currentTimeMillis();
        groupedByDevice.forEach((device, deviceData) -> {
            logger.debug("处理设备: {}, 数据量: {}", device, deviceData.size());
            // 从 realTime 提取月份和日期
            deviceData.forEach(item -> {
                if (item.getRealTime() != null && item.getRealTime().length() >= 10) {
                    String realTime = item.getRealTime();
                    item.setMonth(realTime.substring(0, 7));  // 2026-03
                    item.setDate(realTime.substring(8, 10)); // 17
                } else {
                    // 如果 realTime 不存在，从 timestamp 提取
                    if (item.getTimestamp() != null) {
                        java.util.Date date = new java.util.Date(item.getTimestamp());
                        java.text.SimpleDateFormat monthFormat = new java.text.SimpleDateFormat("yyyy-MM");
                        java.text.SimpleDateFormat dateFormat = new java.text.SimpleDateFormat("dd");
                        item.setMonth(monthFormat.format(date));
                        item.setDate(dateFormat.format(date));
                    }
                }
            });

            deviceData.sort(Comparator.comparing(SensorData::getTimestamp));
            result.put(device, deviceData);
        });
        long processDuration = System.currentTimeMillis() - processStartTime;
        logger.info("数据处理耗时: {}ms", processDuration);

        long duration = System.currentTimeMillis() - startTime;
        logger.info("聚合完成，总耗时: {}ms", duration);
        return result;
    }

    /**
     * 应用启动后，进行缓存预热
     * 预先将数据加载到 Redis，提升首次请求速度
     */
    @EventListener(ApplicationReadyEvent.class)
    public void warmUpCache() {
        logger.info("开始缓存预热...");
        try {
            long startTime = System.currentTimeMillis();

            // 查询所有数据
            List<SensorData> allData = this.list();
            
            // 聚合数据：合并相同时间戳的数据，计算平均值
            Map<String, List<SensorData>> result = aggregateData(allData);

            // 存入 Redis
            redisTemplate.opsForValue().set(CACHE_KEY, result, CACHE_EXPIRE, TimeUnit.MINUTES);

            long duration = System.currentTimeMillis() - startTime;
            logger.info("缓存预热完成！耗时: {}ms, 数据量: {} 条", duration, allData.size());
        } catch (Exception e) {
            logger.error("缓存预热失败: ", e);
        }
    }

    /**
     * 按设备名称分组查询所有数据
     * 优先从 Redis 缓存获取，缓存未命中时从数据库获取并更新缓存
     */
    @Override
    public Map<String, List<SensorData>> getDataGroupBySourceFile() {
        long startTime = System.currentTimeMillis();

        try {
            logger.debug("尝试从Redis缓存获取数据...");
            logger.debug("RedisTemplate: {}", redisTemplate);
            if (redisTemplate != null) {
                logger.debug("RedisTemplate.getConnectionFactory(): {}", redisTemplate.getConnectionFactory());
                RedisConnectionFactory connectionFactory = redisTemplate.getConnectionFactory();
                if (connectionFactory != null) {
                    logger.debug("ConnectionFactory class: {}", connectionFactory.getClass().getName());
                    try {
                        RedisConnection connection = connectionFactory.getConnection();
                        logger.debug("成功获取Redis连接: {}", connection);
                        connection.close();
                    } catch (Exception e) {
                        logger.error("获取Redis连接失败: {}", e.getMessage(), e);
                    }
                }
            }
            // 尝试从缓存获取
            Map<String, List<SensorData>> cachedData = (Map<String, List<SensorData>>) redisTemplate.opsForValue().get(CACHE_KEY);
            if (cachedData != null) {
                long duration = System.currentTimeMillis() - startTime;
                logger.debug("缓存命中！响应时间: {}ms", duration);
                return cachedData;
            }
            logger.debug("缓存未命中，从数据库查询...");
        } catch (Exception e) {
            logger.warn("Redis 连接失败，降级到数据库查询: {}", e.getMessage());
            e.printStackTrace();
        }

        // 缓存未命中或 Redis 连接失败，从数据库查询
        Runtime runtime = Runtime.getRuntime();
        long memoryBefore = runtime.totalMemory() - runtime.freeMemory();
        logger.info("查询前内存使用: {} MB", memoryBefore / 1024 / 1024);

        long dbStartTime = System.currentTimeMillis();
        // 查询所有数据
        List<SensorData> allData = this.list();
        long dbDuration = System.currentTimeMillis() - dbStartTime;

        long memoryAfter = runtime.totalMemory() - runtime.freeMemory();
        long memoryUsed = memoryAfter - memoryBefore;
        logger.info("数据库查询耗时: {}ms, 数据量: {} 条, 内存使用: {} MB", dbDuration, allData.size(), memoryUsed / 1024 / 1024);

        // 过滤掉设备名称为null的数据
        long filterStartTime = System.currentTimeMillis();
        allData = allData.stream()
                .filter(item -> item.getDevice() != null)
                .collect(Collectors.toList());
        long filterDuration = System.currentTimeMillis() - filterStartTime;
        logger.info("过滤数据耗时: {}ms", filterDuration);

        // 聚合数据：合并相同时间戳的数据，计算平均值
        long aggregateStartTime = System.currentTimeMillis();
        Map<String, List<SensorData>> result = aggregateData(allData);
        long aggregateDuration = System.currentTimeMillis() - aggregateStartTime;
        logger.info("数据聚合耗时: {}ms", aggregateDuration);

        // 尝试缓存结果
        long cacheStartTime = System.currentTimeMillis();
        try {
            redisTemplate.opsForValue().set(CACHE_KEY, result, CACHE_EXPIRE, TimeUnit.MINUTES);
            logger.debug("数据已缓存到 Redis");
        } catch (Exception e) {
            logger.warn("Redis 缓存失败: {}", e.getMessage());
        }
        long cacheDuration = System.currentTimeMillis() - cacheStartTime;
        logger.info("Redis缓存耗时: {}ms", cacheDuration);

        long duration = System.currentTimeMillis() - startTime;
        logger.info("总响应时间: {}ms", duration);
        return result;
    }

    /**
     * 分页查询传感器数据，按设备名分组
     */
    @Override
    public Map<String, Object> getDataGroupBySourceFileWithPage(int pageNum, int pageSize) {
        long startTime = System.currentTimeMillis();

        // 创建分页对象
        IPage<SensorData> page = new Page<>(pageNum, pageSize);

        // 从数据库分页查询数据
        IPage<SensorData> result = this.page(page);

        // 按设备分组
        Map<String, List<SensorData>> groupedData = result.getRecords().stream()
                .collect(Collectors.groupingBy(SensorData::getDevice));

        // 构建响应
        Map<String, Object> response = new HashMap<>();
        response.put("data", groupedData);
        response.put("total", result.getTotal());
        response.put("pageNum", pageNum);
        response.put("pageSize", pageSize);
        response.put("totalPages", result.getPages());

        long duration = System.currentTimeMillis() - startTime;
        logger.debug("分页查询完成！响应时间: {}ms", duration);

        return response;
    }

    /**
     * 清除缓存（用于数据更新时调用）
     */
    public void clearCache() {
        try {
            redisTemplate.delete(CACHE_KEY);
            logger.info("缓存已清除");
        } catch (Exception e) {
            logger.error("清除缓存失败: ", e);
        }
    }

    /**
     * 测试Redis连接
     */
    @Override
    public void testRedisConnection() {
        logger.debug("测试Redis连接...");
        logger.debug("RedisTemplate: {}", redisTemplate);
        if (redisTemplate != null) {
            logger.debug("RedisTemplate.getConnectionFactory(): {}", redisTemplate.getConnectionFactory());
            RedisConnectionFactory connectionFactory = redisTemplate.getConnectionFactory();
            if (connectionFactory != null) {
                logger.debug("ConnectionFactory class: {}", connectionFactory.getClass().getName());
                try {
                    RedisConnection connection = connectionFactory.getConnection();
                    logger.debug("成功获取Redis连接: {}", connection);
                    connection.close();
                    logger.info("Redis连接测试成功！");
                } catch (Exception e) {
                    logger.error("获取Redis连接失败: {}", e.getMessage(), e);
                    throw new RuntimeException("Redis连接失败: " + e.getMessage(), e);
                }
            } else {
                logger.error("RedisConnectionFactory 为 null");
                throw new RuntimeException("RedisConnectionFactory 为 null");
            }
        } else {
            logger.error("RedisTemplate 为 null");
            throw new RuntimeException("RedisTemplate 为 null");
        }
    }

    /**
     * 刷新缓存（清除旧缓存并重新加载数据）
     */
    @Override
    public void refreshCache() {
        logger.info("开始刷新缓存...");
        try {
            // 清除旧缓存
            long clearStartTime = System.currentTimeMillis();
            clearCache();
            long clearDuration = System.currentTimeMillis() - clearStartTime;
            logger.info("清除缓存耗时: {}ms", clearDuration);

            // 重新加载数据到缓存
            long startTime = System.currentTimeMillis();

            // 查询所有数据
            long queryStartTime = System.currentTimeMillis();
            List<SensorData> allData = this.list();
            long queryDuration = System.currentTimeMillis() - queryStartTime;
            logger.info("数据库查询耗时: {}ms, 数据量: {} 条", queryDuration, allData.size());

            // 聚合数据：合并相同时间戳的数据，计算平均值
            long groupStartTime = System.currentTimeMillis();
            Map<String, List<SensorData>> result = aggregateData(allData);
            long groupDuration = System.currentTimeMillis() - groupStartTime;
            logger.info("数据聚合耗时: {}ms, 设备数: {}", groupDuration, result.size());

            // 存入 Redis
            long cacheStartTime = System.currentTimeMillis();
            redisTemplate.opsForValue().set(CACHE_KEY, result, CACHE_EXPIRE, TimeUnit.MINUTES);
            long cacheDuration = System.currentTimeMillis() - cacheStartTime;
            logger.info("Redis写入耗时: {}ms", cacheDuration);

            long totalDuration = System.currentTimeMillis() - startTime;
            logger.info("缓存刷新完成！总耗时: {}ms", totalDuration);
        } catch (Exception e) {
            logger.error("缓存刷新失败: ", e);
            throw new RuntimeException("缓存刷新失败: " + e.getMessage(), e);
        }
    }
}
