package com.example.sensor.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.sensor.entity.BatchReportConfig;
import com.example.sensor.service.BatchReportConfigService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 批量报表配置 REST 控制器
 */
@RestController
@RequestMapping("/api/batch-report")
public class BatchReportConfigController {

    private static final Logger logger = LoggerFactory.getLogger(BatchReportConfigController.class);

    @Autowired
    private BatchReportConfigService batchReportConfigService;

    /**
     * 分页获取批量报表配置
     * GET /api/batch-report/page?pageNum=1&pageSize=12&reportCategory=&reportType=&nameId=
     * @param pageNum 页码（从1开始）
     * @param pageSize 每页数量
     * @param reportCategory 报告类别（可选，0=能耗报告，1=健康指数报告）
     * @param reportType 报告类型（可选，0=项目，1=项目标题，2=标题）
     * @param nameId 名称ID（可选）
     */
    @GetMapping("/page")
    public ResponseEntity<Map<String, Object>> getBatchReportConfigPage(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "12") int pageSize,
            @RequestParam(required = false) Integer reportCategory,
            @RequestParam(required = false) Integer reportType,
            @RequestParam(required = false) Integer nameId) {

        logger.info("请求批量报表配置数据，页码: {}, 每页数量: {}, 报告类别: {}, 报告类型: {}, 名称ID: {}",
                pageNum, pageSize, reportCategory, reportType, nameId);

        try {
            IPage<BatchReportConfig> pageData = batchReportConfigService.getBatchReportConfigPage(
                    pageNum,
                    pageSize,
                    reportCategory,
                    reportType,
                    nameId
            );

            Map<String, Object> response = new HashMap<>();
            response.put("code", 200);
            response.put("message", "Success");
            response.put("data", pageData.getRecords());
            response.put("pagination", new HashMap<String, Object>() {{
                put("total", pageData.getTotal());
                put("pageNum", pageData.getCurrent());
                put("pageSize", pageData.getSize());
                put("totalPages", pageData.getPages());
            }});

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("获取批量报表配置失败", e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("code", 500);
            errorResponse.put("message", "获取批量报表配置失败: " + e.getMessage());
            return ResponseEntity.status(500).body(errorResponse);
        }
    }

    /**
     * 统计报表配置数量
     * GET /api/batch-report/count
     * @param reportCategory 报告类别（可选，0=能耗报告，1=健康指数报告）
     * @return 报表数量统计
     */
    @GetMapping("/count")
    public ResponseEntity<Map<String, Object>> countReportConfigs(
            @RequestParam(required = false) Integer reportCategory) {

        logger.info("请求统计报表配置数量，报告类别: {}", reportCategory);

        try {
            Map<String, Integer> countData = batchReportConfigService.countReportConfigs(reportCategory);

            Map<String, Object> response = new HashMap<>();
            response.put("code", 200);
            response.put("message", "Success");
            response.put("data", countData);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("统计报表配置数量失败", e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("code", 500);
            errorResponse.put("message", "统计报表配置数量失败: " + e.getMessage());
            return ResponseEntity.status(500).body(errorResponse);
        }
    }
}