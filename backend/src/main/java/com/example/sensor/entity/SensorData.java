package com.example.sensor.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 传感器数据实体类
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("sensor_data")
public class SensorData implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 设备名称
     */
    private String device;

    /**
     * 月份
     */
    private String month;

    /**
     * 日期
     */
    private String date;

    /**
     * 时间戳（毫秒）
     */
    private Long timestamp;

    /**
     * 传感器数值
     */
    private Double value;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;
}
