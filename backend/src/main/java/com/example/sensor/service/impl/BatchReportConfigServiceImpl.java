package com.example.sensor.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.sensor.entity.BatchReportConfig;
import com.example.sensor.mapper.BatchReportConfigMapper;
import com.example.sensor.service.BatchReportConfigService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * 批量报表配置 Service 实现类
 */
@Service
public class BatchReportConfigServiceImpl extends ServiceImpl<BatchReportConfigMapper, BatchReportConfig> implements BatchReportConfigService {

    private static final Logger logger = LoggerFactory.getLogger(BatchReportConfigServiceImpl.class);

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
        IPage<BatchReportConfig> result = baseMapper.selectPageWithText(
                page,
                reportCategory,
                reportType,
                nameId
        );

        logger.info("查询完成，总记录数: {}", result.getTotal());

        return result;
    }

    @Override
    public Map<String, Integer> countReportConfigs(Integer reportCategory) {
        logger.info("统计报表配置数量，报告类别: {}", reportCategory);

        Map<String, Integer> result = new HashMap<>();

        // 统计总数
        LambdaQueryWrapper<BatchReportConfig> totalWrapper = new LambdaQueryWrapper<>();
        Long total = this.count(totalWrapper);
        result.put("total", total.intValue());

        // 统计能耗报告数量 (reportCategory = 0)
        LambdaQueryWrapper<BatchReportConfig> energyWrapper = new LambdaQueryWrapper<>();
        energyWrapper.eq(BatchReportConfig::getReportCategory, 0);
        Long energyCount = this.count(energyWrapper);
        result.put("energyCount", energyCount.intValue());

        // 统计健康指数报告数量 (reportCategory = 1)
        LambdaQueryWrapper<BatchReportConfig> healthWrapper = new LambdaQueryWrapper<>();
        healthWrapper.eq(BatchReportConfig::getReportCategory, 1);
        Long healthCount = this.count(healthWrapper);
        result.put("healthCount", healthCount.intValue());

        logger.info("统计完成: {}", result);
        return result;
    }
}