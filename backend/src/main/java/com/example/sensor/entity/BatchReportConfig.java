package com.example.sensor.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.io.Serializable;
import java.util.Date;

/**
 * 批量报表配置实体类
 */
@TableName("batch_report_config")
public class BatchReportConfig implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Integer id;

    /**
     * 源文件
     */
    private String sourceFile;

    /**
     * 报告类型（0=项目，1=项目标题，2=标题）
     */
    private Integer reportType;

    /**
     * 报告图示编号（0=无图示）
     */
    private Integer imageId;

    /**
     * 名称name_id
     */
    private Integer nameId;

    /**
     * 启动name_id（0=永久启动，非0=如果其值非零才能启动）
     */
    private Integer enableNameId;

    /**
     * 参考name_id
     */
    private Integer refNameId;

    /**
     * 报告类型（0=能耗报告，1=健康指数报告）
     */
    private Integer reportCategory;

    /**
     * 是否启用"没效"标识
     */
    private Integer enableInvalidMarker;

    /**
     * 显示"没效"标识值（如果数值="没效"，即显示"-----"而不是原来的数值）
     */
    private Integer invalidValue;

    /**
     * 步骤参数ID
     */
    private Integer stepParamId;

    /**
     * 创建时间
     */
    private Date createdAt;

    /**
     * 名称文本（从text_source表关联查询得到）
     */
    private String nameText;

    // Getters and Setters

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getSourceFile() {
        return sourceFile;
    }

    public void setSourceFile(String sourceFile) {
        this.sourceFile = sourceFile;
    }

    public Integer getReportType() {
        return reportType;
    }

    public void setReportType(Integer reportType) {
        this.reportType = reportType;
    }

    public Integer getImageId() {
        return imageId;
    }

    public void setImageId(Integer imageId) {
        this.imageId = imageId;
    }

    public Integer getNameId() {
        return nameId;
    }

    public void setNameId(Integer nameId) {
        this.nameId = nameId;
    }

    public Integer getEnableNameId() {
        return enableNameId;
    }

    public void setEnableNameId(Integer enableNameId) {
        this.enableNameId = enableNameId;
    }

    public Integer getRefNameId() {
        return refNameId;
    }

    public void setRefNameId(Integer refNameId) {
        this.refNameId = refNameId;
    }

    public Integer getReportCategory() {
        return reportCategory;
    }

    public void setReportCategory(Integer reportCategory) {
        this.reportCategory = reportCategory;
    }

    public Integer getEnableInvalidMarker() {
        return enableInvalidMarker;
    }

    public void setEnableInvalidMarker(Integer enableInvalidMarker) {
        this.enableInvalidMarker = enableInvalidMarker;
    }

    public Integer getInvalidValue() {
        return invalidValue;
    }

    public void setInvalidValue(Integer invalidValue) {
        this.invalidValue = invalidValue;
    }

    public Integer getStepParamId() {
        return stepParamId;
    }

    public void setStepParamId(Integer stepParamId) {
        this.stepParamId = stepParamId;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public String getNameText() {
        return nameText;
    }

    public void setNameText(String nameText) {
        this.nameText = nameText;
    }

    @Override
    public String toString() {
        return "BatchReportConfig{" +
                "id=" + id +
                ", sourceFile='" + sourceFile + '\'' +
                ", reportType=" + reportType +
                ", imageId=" + imageId +
                ", nameId=" + nameId +
                ", enableNameId=" + enableNameId +
                ", refNameId=" + refNameId +
                ", reportCategory=" + reportCategory +
                ", enableInvalidMarker=" + enableInvalidMarker +
                ", invalidValue=" + invalidValue +
                ", stepParamId=" + stepParamId +
                ", createdAt=" + createdAt +
                ", nameText='" + nameText + '\'' +
                '}';
    }
}
