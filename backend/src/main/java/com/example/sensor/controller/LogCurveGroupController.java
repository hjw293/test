package com.example.sensor.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.sensor.entity.LogCurveGroup;
import com.example.sensor.service.LogCurveGroupService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 日志曲线组 REST 控制器
 */
@RestController
@RequestMapping("/api/curve-group")
public class LogCurveGroupController {

    private static final Logger logger = LoggerFactory.getLogger(LogCurveGroupController.class);

    @Autowired
    private LogCurveGroupService logCurveGroupService;

    /**
     * 分页获取曲线组配置
     * GET /api/curve-group/page?pageNum=1&pageSize=12&groupNameId=&curveType=&curveMode=
     */
    @GetMapping("/page")
    public ResponseEntity<Map<String, Object>> getLogCurveGroupPage(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "12") int pageSize,
            @RequestParam(required = false) Integer groupNameId,
            @RequestParam(required = false) Integer curveType,
            @RequestParam(required = false) Integer curveMode) {

        logger.info("请求曲线组配置数据，页码: {}, 每页数量: {}, 组ID: {}, 类型: {}, 模式: {}",
                pageNum, pageSize, groupNameId, curveType, curveMode);

        try {
            IPage<LogCurveGroup> pageData = logCurveGroupService.getLogCurveGroupPage(
                    pageNum, pageSize, groupNameId, curveType, curveMode);

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
            logger.error("获取曲线组配置失败", e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("code", 500);
            errorResponse.put("message", "获取曲线组配置失败: " + e.getMessage());
            return ResponseEntity.status(500).body(errorResponse);
        }
    }

    /**
     * 获取所有不同的组名称ID
     * GET /api/curve-group/group-ids
     */
    @GetMapping("/group-ids")
    public ResponseEntity<Map<String, Object>> getDistinctGroupNameIds() {
        try {
            List<Integer> groupIds = logCurveGroupService.getDistinctGroupNameIds();

            Map<String, Object> response = new HashMap<>();
            response.put("code", 200);
            response.put("message", "Success");
            response.put("data", groupIds);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("获取组名称ID列表失败", e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("code", 500);
            errorResponse.put("message", "获取组名称ID列表失败: " + e.getMessage());
            return ResponseEntity.status(500).body(errorResponse);
        }
    }

    /**
     * 获取指定组下的所有曲线
     * GET /api/curve-group/by-group?groupNameId=327682001
     */
    @GetMapping("/by-group")
    public ResponseEntity<Map<String, Object>> getCurvesByGroupId(
            @RequestParam Integer groupNameId) {

        logger.info("请求组 {} 的所有曲线数据", groupNameId);

        try {
            List<LogCurveGroup> curves = logCurveGroupService.getCurvesByGroupId(groupNameId);

            Map<String, Object> response = new HashMap<>();
            response.put("code", 200);
            response.put("message", "Success");
            response.put("data", curves);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("获取组曲线数据失败", e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("code", 500);
            errorResponse.put("message", "获取组曲线数据失败: " + e.getMessage());
            return ResponseEntity.status(500).body(errorResponse);
        }
    }

    /**
     * 获取所有数据，按组分组返回
     * GET /api/curve-group/all-grouped
     */
    @GetMapping("/all-grouped")
    public ResponseEntity<Map<String, Object>> getAllGrouped() {
        try {
            Map<Integer, List<LogCurveGroup>> grouped = logCurveGroupService.getAllGrouped();

            Map<String, Object> response = new HashMap<>();
            response.put("code", 200);
            response.put("message", "Success");
            response.put("data", grouped);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("获取分组曲线数据失败", e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("code", 500);
            errorResponse.put("message", "获取分组曲线数据失败: " + e.getMessage());
            return ResponseEntity.status(500).body(errorResponse);
        }
    }
}
