package com.example.sensor.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.sensor.entity.SensorData;
import org.apache.ibatis.annotations.Mapper;

/**
 * 传感器数据 Mapper 接口
 */
@Mapper
public interface SensorDataMapper extends BaseMapper<SensorData> {

}
