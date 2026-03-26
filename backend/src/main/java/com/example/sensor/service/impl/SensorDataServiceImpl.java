package com.example.sensor.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.sensor.entity.SensorData;
import com.example.sensor.mapper.SensorDataMapper;
import com.example.sensor.service.SensorDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 传感器数据 Service 业务实现
 */
@Service
public class SensorDataServiceImpl extends ServiceImpl<SensorDataMapper, SensorData> implements SensorDataService {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    private static final String CACHE_KEY = "sensor:data:grouped";
    private static final long CACHE_EXPIRE = 30; // 缓存过期时间（分钟）

    /**
     * 按设备名称分组查询所有数据
     */
    @Override
    public Map<String, List<SensorData>> getDataGroupBySourceFile() {
        // 尝试从缓存获取
        try {
            Map<String, List<SensorData>> cachedData = (Map<String, List<SensorData>>) redisTemplate.opsForValue().get(CACHE_KEY);
            if (cachedData != null) {
                return cachedData;
            }
        } catch (Exception e) {
            // Redis连接失败，降级到数据库查询
            System.err.println("Redis connection failed, falling back to database: " + e.getMessage());
        }

        // 缓存未命中或Redis连接失败，从数据库查询
        List<SensorData> allData = this.list();
        Map<String, List<SensorData>> result = allData.stream()
                .collect(Collectors.groupingBy(SensorData::getDevice));

        // 尝试缓存结果
        try {
            redisTemplate.opsForValue().set(CACHE_KEY, result, CACHE_EXPIRE, TimeUnit.MINUTES);
        } catch (Exception e) {
            // Redis缓存失败，仅记录日志
            System.err.println("Redis cache failed: " + e.getMessage());
        }

        return result;
    }
}
