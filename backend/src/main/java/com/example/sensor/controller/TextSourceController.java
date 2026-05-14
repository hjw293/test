package com.example.sensor.controller;

import com.example.sensor.service.TextSourceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 文本资源 REST 控制器
 */
@RestController
@RequestMapping("/api/text-source")
public class TextSourceController {

    @Autowired
    private TextSourceService textSourceService;

    /**
     * 根据 name_id 获取中文名称
     * GET /api/text-source/name?nameId=327682001
     */
    @GetMapping("/name")
    public ResponseEntity<Map<String, Object>> getNameByNameId(@RequestParam Integer nameId) {
        try {
            String name = textSourceService.getNameByNameId(nameId);

            Map<String, Object> response = new HashMap<>();
            response.put("code", 200);
            response.put("message", "Success");
            response.put("data", name);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("code", 500);
            errorResponse.put("message", "获取名称失败: " + e.getMessage());
            return ResponseEntity.status(500).body(errorResponse);
        }
    }

    /**
     * 批量根据 name_id 获取中文名称
     * GET /api/text-source/names?nameIds=327682001,327682002
     */
    @GetMapping("/names")
    public ResponseEntity<Map<String, Object>> getNamesByNameIds(@RequestParam String nameIds) {
        try {
            List<Integer> ids = java.util.Arrays.stream(nameIds.split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .map(Integer::parseInt)
                    .toList();

            Map<Integer, String> nameMap = textSourceService.getNamesByNameIds(ids);

            Map<String, Object> response = new HashMap<>();
            response.put("code", 200);
            response.put("message", "Success");
            response.put("data", nameMap);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("code", 500);
            errorResponse.put("message", "获取名称失败: " + e.getMessage());
            return ResponseEntity.status(500).body(errorResponse);
        }
    }
}
