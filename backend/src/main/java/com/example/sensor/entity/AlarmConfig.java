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
 * 警报配置实体类
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("alarm_config")
public class AlarmConfig implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 警报ID
     */
    private String alarmKey;

    /**
     * 警报内容的语言
     */
    private String languageName;

    /**
     * 警报的内容
     */
    private String alarmText;

    /**
     * 警报内容的颜色（RGB格式）
     */
    private String textColor;

    /**
     * 警报的颜色（RGB格式）
     */
    private String bgColor;

    /**
     * 警报的性质
     */
    private String responseReq;

    /**
     * 警报的处理方式
     */
    private String machineAction;

    /**
     * 创建时间
     */
    
}