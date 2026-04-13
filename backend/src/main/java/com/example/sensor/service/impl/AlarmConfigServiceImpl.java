package com.example.sensor.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.sensor.entity.AlarmConfig;
import com.example.sensor.mapper.AlarmConfigMapper;
import com.example.sensor.service.AlarmConfigService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * 警报配置 Service 业务实现
 */
@Service
public class AlarmConfigServiceImpl extends ServiceImpl<AlarmConfigMapper, AlarmConfig> implements AlarmConfigService {

    private static final Logger logger = LoggerFactory.getLogger(AlarmConfigServiceImpl.class);

    /**
     * 分页查询警报配置
     */
    @Override
    public IPage<AlarmConfig> getAlarmConfigPage(int pageNum, int pageSize, String alarmKey) {
        logger.debug("查询警报配置，页码: {}, 每页数量: {}, 警报ID: {}", pageNum, pageSize, alarmKey);

        // 创建分页对象
        Page<AlarmConfig> page = new Page<>(pageNum, pageSize);

        // 创建查询条件
        LambdaQueryWrapper<AlarmConfig> queryWrapper = new LambdaQueryWrapper<>();
        
        // 只查询 language_name 为"中文"的记录
        queryWrapper.eq(AlarmConfig::getLanguageName, "中文");
        
        // 如果提供了 alarmKey，则添加查询条件
        if (alarmKey != null && !alarmKey.trim().isEmpty()) {
            queryWrapper.like(AlarmConfig::getAlarmKey, alarmKey);
        }

        // 按 alarmKey 排序
        queryWrapper.orderByAsc(AlarmConfig::getAlarmKey);

        // 执行分页查询
        IPage<AlarmConfig> result = this.page(page, queryWrapper);

        logger.debug("查询完成，总记录数: {}", result.getTotal());

        return result;
    }
}