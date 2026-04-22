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

    /**
     * 分页查询，按设备名分组
     * @param pageNum 页码（从1开始）
     * @param pageSize 每页数量
     * @return 分页结果，包含数据和总数
     */
    Map<String, Object> getDataGroupBySourceFileWithPage(int pageNum, int pageSize);

    /**
     * 测试Redis连接
     */
    void testRedisConnection();

    /**
     * 刷新缓存（清除旧缓存并重新加载数据）
     */
    void refreshCache();

    /**
     * 根据设备名称获取传感器数据
     * @param device 设备名称
     * @return 传感器数据列表
     */
    List<SensorData> getDataByDevice(String device);

    /**
     * 批量获取多个设备的传感器数据
     * @param devices 设备名称列表
     * @return 设备名称 -> 数据列表 的映射
     */
    Map<String, List<SensorData>> getDataByDevices(List<String> devices);

    /**
     * 获取所有设备名称列表
     * @return 设备名称列表
     */
    List<String> getDeviceList();
}
