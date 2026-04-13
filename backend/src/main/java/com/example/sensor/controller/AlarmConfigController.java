package com.example.sensor.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.sensor.entity.AlarmConfig;
import com.example.sensor.service.AlarmConfigService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 警报配置 REST 控制器
 */
@RestController
@RequestMapping("/api/alarm")
public class AlarmConfigController {

    private static final Logger logger = LoggerFactory.getLogger(AlarmConfigController.class);

    @Autowired
    private AlarmConfigService alarmConfigService;

    /**
     * 分页获取警报配置
     * GET /api/alarm/page?pageNum=1&pageSize=12&alarmKey=
     * @param pageNum 页码（从1开始）
     * @param pageSize 每页数量
     * @param alarmKey 警报ID（可选，用于模糊搜索）
     */
    @GetMapping("/page")
    public ResponseEntity<Map<String, Object>> getAlarmConfigPage(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "12") int pageSize,
            @RequestParam(required = false) String alarmKey) {

        logger.info("请求警报配置数据，页码: {}, 每页数量: {}, 警报ID: {}", pageNum, pageSize, alarmKey);

        try {
            IPage<AlarmConfig> pageData = alarmConfigService.getAlarmConfigPage(pageNum, pageSize, alarmKey);

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
            logger.error("获取警报配置失败", e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("code", 500);
            errorResponse.put("message", "获取警报配置失败: " + e.getMessage());
            return ResponseEntity.status(500).body(errorResponse);
        }
    }
}