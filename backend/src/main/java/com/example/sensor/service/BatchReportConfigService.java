package com.example.sensor.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.sensor.entity.BatchReportConfig;

import java.util.Map;

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

    /**
     * 统计报表配置数量
     * @param reportCategory 报告类别（可选，0=能耗报告，1=健康指数报告）
     * @return 报表数量
     */
    Map<String, Integer> countReportConfigs(Integer reportCategory);
}