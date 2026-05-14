package com.example.sensor.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.sensor.entity.AlarmLog;
import com.example.sensor.mapper.AlarmLogMapper;
import com.example.sensor.service.AlarmLogService;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 报警日志 Service 实现类
 */
@Service
public class AlarmLogServiceImpl extends ServiceImpl<AlarmLogMapper, AlarmLog> implements AlarmLogService {

    @Override
    public int countByAlarmId(Integer alarmId) {
        return baseMapper.countByAlarmId(alarmId);
    }

    @Override
    public Map<String, Integer> getAlarmCountMap() {
        Map<String, Integer> map = new HashMap<>();
        List<Map<String, Object>> list = baseMapper.countGroupByAlarmId();
        for (Map<String, Object> item : list) {
            // alarm_id 可能是 int 或 String，统一转为 String 进行匹配
            String alarmId = String.valueOf(item.get("alarmId"));
            int count = ((Number) item.get("count")).intValue();
            map.put(alarmId, count);
        }
        return map;
    }

    @Override
    public List<AlarmLog> getByAlarmId(Integer alarmId) {
        return baseMapper.selectByAlarmId(alarmId);
    }
}