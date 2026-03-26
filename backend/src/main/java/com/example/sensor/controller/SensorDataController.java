package com.example.sensor.controller;

import com.example.sensor.entity.SensorData;
import com.example.sensor.service.SensorDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}
