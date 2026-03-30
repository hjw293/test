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

import java.util.HashMap;
import java.util.List;
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
    private static final long CACHE_EXPIRE = 60; // 缓存过期时间（分钟）

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
            Map<String, List<SensorData>> result = allData.stream()
                    .collect(Collectors.groupingBy(SensorData::getDevice));

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
        List<SensorData> allData = this.list();
        Map<String, List<SensorData>> result = allData.stream()
                .collect(Collectors.groupingBy(SensorData::getDevice));

        // 尝试缓存结果
        try {
            redisTemplate.opsForValue().set(CACHE_KEY, result, CACHE_EXPIRE, TimeUnit.MINUTES);
            logger.debug("数据已缓存到 Redis");
        } catch (Exception e) {
            logger.warn("Redis 缓存失败: {}", e.getMessage());
        }

        long duration = System.currentTimeMillis() - startTime;
        logger.debug("数据库查询完成！响应时间: {}ms", duration);
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
}
