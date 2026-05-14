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
import java.sql.Timestamp;

/**
 * 日志曲线组实体类
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("log_curve_group")
public class LogCurveGroup implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 主键ID */
    @TableId(type = IdType.AUTO)
    private Integer id;

    /** 源文件 */
    private String sourceFile;

    /** 组名称ID */
    private Integer groupNameId;

    /** 名称 */
    private String groupName;

    /** 名称类型（0=使用name属性值，1=使用name_id文字） */
    private Integer groupNameType;

    /** 启动name_id（0=永久启动） */
    private Integer groupEnableNid;

    /** 启动值（当启动name_id的值等于启动值时，此曲线组才生效） */
    private Integer groupEnableValue;

    /** 背景颜色 */
    private String groupBgColor;

    /** 时间标示线的颜色 */
    private String groupTimeDivColor;

    /** 建立时间 */
    private Long groupTime;

    /** 曲线名称ID */
    private Integer curveNameId;

    /** 曲线名称（从text_source表获取） */
    @TableField(exist = false)
    private String name;

    /** 曲线颜色 */
    private String curveColor;

    /** 曲线类型 */
    private Integer curveType;

    /** 曲线引用 */
    private Integer curveRef;

    /** 曲线启用节点ID */
    private Integer curveEnableNid;

    /** 曲线模式 */
    private Integer curveMode;

    /** 曲线最大值 */
    private Long curveMax;

    /** 曲线最大值节点ID */
    private Integer curveMaxNid;

    /** 曲线最大值行 */
    private Integer curveMaxRow;

    /** 曲线最大值列 */
    private Integer curveMaxCol;

    /** 曲线最小值 */
    private Long curveMin;

    /** 曲线最小值节点ID */
    private Integer curveMinNid;

    /** 曲线最小值行 */
    private Integer curveMinRow;

    /** 曲线最小值列 */
    private Integer curveMinCol;

    /** 预估曲线（0=不是预估曲线，1=预估曲线） */
    private Integer curvePredict;

    /** 创建时间 */
    private Timestamp createdAt;
}
