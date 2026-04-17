package com.example.sensor.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.sensor.entity.BatchReportConfig;
import com.example.sensor.mapper.BatchReportConfigMapper;
import com.example.sensor.service.BatchReportConfigService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 批量报表配置 Service 实现类
 */
@Service
public class BatchReportConfigServiceImpl implements BatchReportConfigService {

    private static final Logger logger = LoggerFactory.getLogger(BatchReportConfigServiceImpl.class);

    @Autowired
    private BatchReportConfigMapper batchReportConfigMapper;

    @Override
    public IPage<BatchReportConfig> getBatchReportConfigPage(
            int pageNum,
            int pageSize,
            Integer reportCategory,
            Integer reportType,
            Integer nameId) {

        logger.info("查询批量报表配置，页码: {}, 每页数量: {}, 报告类别: {}, 报告类型: {}, 名称ID: {}",
                pageNum, pageSize, reportCategory, reportType, nameId);

        // 创建分页对象
        Page<BatchReportConfig> page = new Page<>(pageNum, pageSize);

        // 执行查询
        IPage<BatchReportConfig> result = batchReportConfigMapper.selectPageWithText(
                page,
                reportCategory,
                reportType,
                nameId
        );

        logger.info("查询完成，总记录数: {}", result.getTotal());

        return result;
    }
}