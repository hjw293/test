package com.example.sensor.controller;

import com.example.sensor.entity.CurveData;
import com.example.sensor.service.CurveDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 曲线数据 REST 控制器
 */
@RestController
@RequestMapping("/api/curve-data")
public class CurveDataController {

    @Autowired
    private CurveDataService curveDataService;

    /**
     * 根据曲线名称ID获取曲线数据
     * GET /api/curve-data/by-name-id?nameId=123
     */
    @GetMapping("/by-name-id")
    public ResponseEntity<Map<String, Object>> getByNameId(@RequestParam String nameId) {
        try {
            List<CurveData> data = curveDataService.getByNameId(nameId);

            Map<String, Object> response = new HashMap<>();
            response.put("code", 200);
            response.put("message", "Success");
            response.put("data", data);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("code", 500);
            errorResponse.put("message", "获取曲线数据失败: " + e.getMessage());
            return ResponseEntity.status(500).body(errorResponse);
        }
    }

    /**
     * 根据组名称ID和曲线名称ID获取数据（解决ID冲突）
     * GET /api/curve-data/by-group-and-name?groupNameId=327682000&nameId=1573105
     */
    @GetMapping("/by-group-and-name")
    public ResponseEntity<Map<String, Object>> getByGroupNameIdAndNameId(
            @RequestParam Integer groupNameId,
            @RequestParam String nameId) {
        try {
            List<CurveData> data = curveDataService.getByGroupNameIdAndNameId(groupNameId, nameId);

            Map<String, Object> response = new HashMap<>();
            response.put("code", 200);
            response.put("message", "Success");
            response.put("data", data);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("code", 500);
            errorResponse.put("message", "获取曲线数据失败: " + e.getMessage());
            return ResponseEntity.status(500).body(errorResponse);
        }
    }

    /**
     * 根据多个曲线名称ID获取数据
     * GET /api/curve-data/by-name-ids?nameIds=1,2,3&month=2025-12
     */
    @GetMapping("/by-name-ids")
    public ResponseEntity<Map<String, Object>> getByNameIds(
            @RequestParam List<String> nameIds,
            @RequestParam(required = false) String month) {
        try {
            List<CurveData> data;
            if (month != null && !month.isEmpty()) {
                data = curveDataService.getByNameIdsAndMonth(nameIds, month);
            } else {
                data = curveDataService.getByNameIds(nameIds);
            }

            Map<String, Object> response = new HashMap<>();
            response.put("code", 200);
            response.put("message", "Success");
            response.put("data", data);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("code", 500);
            errorResponse.put("message", "获取曲线数据失败: " + e.getMessage());
            return ResponseEntity.status(500).body(errorResponse);
        }
    }

    /**
     * 根据多个组名称ID和曲线名称ID获取数据（解决ID冲突）
     * GET /api/curve-data/by-groups-and-names?groupNameIds=327682000,327682001&nameIds=1573105,1573106&month=2025-12
     */
    @GetMapping("/by-groups-and-names")
    public ResponseEntity<Map<String, Object>> getByGroupNameIdsAndNameIds(
            @RequestParam List<Integer> groupNameIds,
            @RequestParam List<String> nameIds,
            @RequestParam(required = false) String month) {
        try {
            List<CurveData> data;
            if (month != null && !month.isEmpty()) {
                // 先用联表查询获取数据，再按月份过滤
                List<CurveData> allData = curveDataService.getByGroupNameIdsAndNameIds(groupNameIds, nameIds);
                data = allData.stream()
                        .filter(d -> month.equals(d.getMonth()))
                        .collect(java.util.stream.Collectors.toList());
            } else {
                data = curveDataService.getByGroupNameIdsAndNameIds(groupNameIds, nameIds);
            }

            Map<String, Object> response = new HashMap<>();
            response.put("code", 200);
            response.put("message", "Success");
            response.put("data", data);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("code", 500);
            errorResponse.put("message", "获取曲线数据失败: " + e.getMessage());
            return ResponseEntity.status(500).body(errorResponse);
        }
    }

    /**
     * 获取所有月份列表
     * GET /api/curve-data/months
     */
    @GetMapping("/months")
    public ResponseEntity<Map<String, Object>> getDistinctMonths() {
        try {
            List<String> months = curveDataService.getDistinctMonths();

            Map<String, Object> response = new HashMap<>();
            response.put("code", 200);
            response.put("message", "Success");
            response.put("data", months);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("code", 500);
            errorResponse.put("message", "获取月份列表失败: " + e.getMessage());
            return ResponseEntity.status(500).body(errorResponse);
        }
    }
}