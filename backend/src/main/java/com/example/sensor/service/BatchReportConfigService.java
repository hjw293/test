package com.example.sensor.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.sensor.entity.BatchReportConfig;

/**
 * 批量报表配置 Service 接口
 */
public interface BatchReportConfigService {

    /**
     * 分页获取批量报表配置
     * @param pageNum 页码（从1开始）
     * @param pageSize 每页数量
     * @param reportCategory 报告类别（可选）
     * @param reportType 报告类型（可选）
     * @param nameId 名称ID（可选）
     * @return 分页结果
     */
    IPage<BatchReportConfig> getBatchReportConfigPage(
            int pageNum,
            int pageSize,
            Integer reportCategory,
            Integer reportType,
            Integer nameId
    );
}