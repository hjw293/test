package com.example.sensor.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.sensor.entity.SensorData;

import java.util.List;
import java.util.Map;

/**
 * 传感器数据 Service 业务接口
 */
public interface SensorDataService extends IService<SensorData> {

    /**
     * 按设备名分组查询数据
     * 返回格式: {"Device_A": [...], "Device_B": [...]}
     */
    Map<String, List<SensorData>> getDataGroupBySourceFile();
}
