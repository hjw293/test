package com.example.sensor.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.sensor.entity.AlarmLog;

import java.util.List;
import java.util.Map;

/**
 * 报警日志 Service 业务接口
 */
public interface AlarmLogService extends IService<AlarmLog> {

    /**
     * 根据报警ID统计报警次数
     * @param alarmId 报警ID
     * @return 报警次数
     */
    int countByAlarmId(Integer alarmId);

    /**
     * 获取所有报警ID及其报警次数的映射
     * @return Map<alarmId, count>
     */
    Map<String, Integer> getAlarmCountMap();

    /**
     * 根据报警ID查询报警日志列表
     * @param alarmId 报警ID
     * @return 报警日志列表
     */
    List<AlarmLog> getByAlarmId(Integer alarmId);
}