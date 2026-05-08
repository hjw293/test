package com.example.sensor.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.sensor.entity.AlarmLog;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

/**
 * 报警日志 Mapper 接口
 */
@Mapper
public interface AlarmLogMapper extends BaseMapper<AlarmLog> {

    /**
     * 根据报警ID统计报警次数
     * @param alarmId 报警ID
     * @return 报警次数
     */
    @Select("SELECT COUNT(*) FROM alarm_log WHERE alarm_id = #{alarmId}")
    int countByAlarmId(Integer alarmId);

    /**
     * 获取所有报警ID及其报警次数
     * @return 列表
     */
    @Select("SELECT alarm_id as alarmId, COUNT(*) as count FROM alarm_log GROUP BY alarm_id")
    List<Map<String, Object>> countGroupByAlarmId();

    /**
     * 根据报警ID查询报警日志列表
     * @param alarmId 报警ID
     * @return 报警日志列表
     */
    @Select("SELECT * FROM alarm_log WHERE alarm_id = #{alarmId} ORDER BY `time` ASC")
    List<AlarmLog> selectByAlarmId(Integer alarmId);
}