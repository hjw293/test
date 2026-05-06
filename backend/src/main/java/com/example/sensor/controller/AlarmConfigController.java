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
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
     * 获取所有不重复的报警性质和处理方式选项
     * GET /api/alarm/filter-options
     */
    @GetMapping("/filter-options")
    public ResponseEntity<Map<String, Object>> getFilterOptions() {
        try {
            // 获取所有报警性质和处理方式（不分页，获取全部数据）
            List<String> responseReqList = alarmConfigService.getAllResponseReq();
            List<String> machineActionList = alarmConfigService.getAllMachineAction();

            Map<String, Object> response = new HashMap<>();
            response.put("code", 200);
            response.put("message", "Success");
            response.put("data", new HashMap<String, Object>() {{
                put("responseReqOptions", responseReqList);
                put("machineActionOptions", machineActionList);
            }});

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("获取筛选选项失败", e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("code", 500);
            errorResponse.put("message", "获取筛选选项失败: " + e.getMessage());
            return ResponseEntity.status(500).body(errorResponse);
        }
    }

    /**
     * 分页获取警报配置
     * GET /api/alarm/page?pageNum=1&pageSize=12&alarmKey=&responseReq=&machineAction=
     * @param pageNum 页码（从1开始）
     * @param pageSize 每页数量
     * @param alarmKey 警报ID（可选，用于模糊搜索）
     * @param responseReq 报警性质（可选，用于筛选）
     * @param machineAction 处理方式（可选，用于筛选）
     */
    @GetMapping("/page")
    public ResponseEntity<Map<String, Object>> getAlarmConfigPage(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "12") int pageSize,
            @RequestParam(required = false) String alarmKey,
            @RequestParam(required = false) String responseReq,
            @RequestParam(required = false) String machineAction) {

        logger.info("请求警报配置数据，页码: {}, 每页数量: {}, 警报ID: {}, 报警性质: {}, 处理方式: {}",
                pageNum, pageSize, alarmKey, responseReq, machineAction);

        try {
            IPage<AlarmConfig> pageData = alarmConfigService.getAlarmConfigPage(
                    pageNum, pageSize, alarmKey, responseReq, machineAction);

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

    /**
     * 获取设备状态统计（停机数量）
     * GET /api/alarm/device-status-count
     */
    @GetMapping("/device-status-count")
    public ResponseEntity<Map<String, Object>> getDeviceStatusCount() {
        try {
            Map<String, Integer> count = alarmConfigService.getDeviceStatusCount();
            Map<String, Object> response = new HashMap<>();
            response.put("code", 200);
            response.put("message", "Success");
            response.put("data", count);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("获取设备状态统计失败", e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("code", 500);
            errorResponse.put("message", "获取设备状态统计失败: " + e.getMessage());
            return ResponseEntity.status(500).body(errorResponse);
        }
    }

    /**
     * 获取报警性质种类数量
     * GET /api/alarm/response-req-count
     */
    @GetMapping("/response-req-count")
    public ResponseEntity<Map<String, Object>> getResponseReqCount() {
        try {
            List<String> responseReqList = alarmConfigService.getAllResponseReq();
            Map<String, Object> response = new HashMap<>();
            response.put("code", 200);
            response.put("message", "Success");
            response.put("data", new HashMap<String, Object>() {{
                put("count", responseReqList.size());
                put("types", responseReqList);
            }});
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("获取报警性质种类数量失败", e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("code", 500);
            errorResponse.put("message", "获取报警性质种类数量失败: " + e.getMessage());
            return ResponseEntity.status(500).body(errorResponse);
        }
    }

    /**
     * 获取每种报警性质的数量统计
     * GET /api/alarm/response-req-stats
     */
    @GetMapping("/response-req-stats")
    public ResponseEntity<Map<String, Object>> getResponseReqStats() {
        try {
            List<Map<String, Object>> stats = alarmConfigService.getResponseReqStats();
            Map<String, Object> response = new HashMap<>();
            response.put("code", 200);
            response.put("message", "Success");
            response.put("data", stats);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("获取报警性质统计失败", e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("code", 500);
            errorResponse.put("message", "获取报警性质统计失败: " + e.getMessage());
            return ResponseEntity.status(500).body(errorResponse);
        }
    }

    }