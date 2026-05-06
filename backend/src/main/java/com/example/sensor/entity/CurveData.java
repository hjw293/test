package com.example.sensor.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 曲线数据实体类
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("curve_data")
public class CurveData implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 主键ID */
    @TableId(type = IdType.AUTO)
    private Integer id;

    /** 曲线名称ID，关联 log_curve_group.curve_name_id */
    @TableField("name_id")
    private String nameId;

    /** 曲线名称 */
    private String name;

    /** 时间戳 */
    private String timestamp;

    /** 实时时间 */
    @TableField("real_time")
    private LocalDateTime realTime;

    /** 数值 */
    private Double value;
}