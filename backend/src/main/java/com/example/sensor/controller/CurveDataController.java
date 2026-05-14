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
     * 根据多个曲线名称ID获取数据
     * GET /api/curve-data/by-name-ids?nameIds=1,2,3&month=2025-12&date=02
     */
    @GetMapping("/by-name-ids")
    public ResponseEntity<Map<String, Object>> getByNameIds(
            @RequestParam List<String> nameIds,
            @RequestParam(required = false) String month,
            @RequestParam(required = false) String date) {
        try {
            List<CurveData> data;
            if (date != null && !date.isEmpty()) {
                data = curveDataService.getByNameIdsAndDate(nameIds, date);
            } else if (month != null && !month.isEmpty()) {
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

    /**
     * 根据月份获取日期列表
     * GET /api/curve-data/dates?month=2026-03
     */
    @GetMapping("/dates")
    public ResponseEntity<Map<String, Object>> getDistinctDatesByMonth(@RequestParam String month) {
        try {
            List<String> dates = curveDataService.getDistinctDatesByMonth(month);

            Map<String, Object> response = new HashMap<>();
            response.put("code", 200);
            response.put("message", "Success");
            response.put("data", dates);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("code", 500);
            errorResponse.put("message", "获取日期列表失败: " + e.getMessage());
            return ResponseEntity.status(500).body(errorResponse);
        }
    }
}