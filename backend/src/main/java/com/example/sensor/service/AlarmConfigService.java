package com.example.sensor.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.sensor.entity.AlarmConfig;

/**
 * 警报配置 Service 业务接口
 */
public interface AlarmConfigService extends IService<AlarmConfig> {

    /**
     * 分页查询警报配置
     * @param pageNum 页码（从1开始）
     * @param pageSize 每页数量
     * @param alarmKey 警报ID（可选）
     * @return 分页结果
     */
    IPage<AlarmConfig> getAlarmConfigPage(int pageNum, int pageSize, String alarmKey);
}