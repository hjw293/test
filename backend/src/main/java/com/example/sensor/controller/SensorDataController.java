package com.example.sensor.controller;

import com.example.sensor.entity.SensorData;
import com.example.sensor.service.SensorDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 传感器数据 REST 控制器
 */
@RestController
@RequestMapping("/api")
public class SensorDataController {

    @Autowired
    private SensorDataService sensorDataService;

    /**
     * 获取所有传感器数据，按设备名分组
     * GET /api/data
     */
    @GetMapping("/data")
    public ResponseEntity<Map<String, Object>> getData() {
        Map<String, List<SensorData>> dataGroupByDevice = sensorDataService.getDataGroupBySourceFile();

        Map<String, Object> response = new HashMap<>();
        response.put("code", 200);
        response.put("message", "Success");
        response.put("data", dataGroupByDevice);

        return ResponseEntity.ok(response);
    }

    /**
     * 分页获取传感器数据，按设备名分组
     * GET /api/data/page?pageNum=1&pageSize=100
     * @param pageNum 页码（从1开始）
     * @param pageSize 每页数量
     */
    @GetMapping("/data/page")
    public ResponseEntity<Map<String, Object>> getDataWithPage(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "100") int pageSize) {

        Map<String, Object> pageData = sensorDataService.getDataGroupBySourceFileWithPage(pageNum, pageSize);

        Map<String, Object> response = new HashMap<>();
        response.put("code", 200);
        response.put("message", "Success");
        response.put("data", pageData.get("data"));
        response.put("pagination", new HashMap<String, Object>() {{
            put("total", pageData.get("total"));
            put("pageNum", pageData.get("pageNum"));
            put("pageSize", pageData.get("pageSize"));
            put("totalPages", pageData.get("totalPages"));
        }});

        return ResponseEntity.ok(response);
    }
}
