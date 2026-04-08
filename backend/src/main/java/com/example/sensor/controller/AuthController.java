package com.example.sensor.controller;

import com.example.sensor.entity.User;
import com.example.sensor.service.AuthService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 认证控制器
 */
@RestController
@RequestMapping("/api/auth")
@CrossOrigin(originPatterns = "http://localhost:5173")
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    @Autowired
    private AuthService authService;

    /**
     * 用户登录
     */
    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody Map<String, String> loginRequest) {
        Map<String, Object> response = new HashMap<>();

        try {
            String username = loginRequest.get("username");
            String password = loginRequest.get("password");

            logger.info("收到登录请求: {}", username);

            // 调用登录服务
            String token = authService.login(username, password);

            // 获取用户信息
            User user = authService.getUserFromToken(token);

            response.put("code", 200);
            response.put("message", "登录成功");
            response.put("data", new HashMap<String, Object>() {{
                put("token", token);
                put("user", new HashMap<String, Object>() {{
                    put("id", user.getId());
                    put("username", user.getUsername());
                    put("email", user.getEmail());
                    put("realName", user.getRealName());
                    put("role", user.getRole());
                }});
            }});

            logger.info("登录成功: {}", username);
        } catch (Exception e) {
            logger.error("登录失败: {}", e.getMessage());
            response.put("code", 401);
            response.put("message", e.getMessage());
        }

        return ResponseEntity.ok(response);
    }

    /**
     * 获取当前用户信息
     */
    @GetMapping("/user")
    public ResponseEntity<Map<String, Object>> getCurrentUser(@RequestHeader("Authorization") String authorization) {
        Map<String, Object> response = new HashMap<>();

        try {
            String token = authorization.replace("Bearer ", "");
            User user = authService.getUserFromToken(token);

            if (user == null) {
                response.put("code", 401);
                response.put("message", "未授权");
                return ResponseEntity.ok(response);
            }

            response.put("code", 200);
            response.put("message", "获取成功");
            response.put("data", new HashMap<String, Object>() {{
                put("id", user.getId());
                put("username", user.getUsername());
                put("email", user.getEmail());
                put("realName", user.getRealName());
                put("role", user.getRole());
            }});

        } catch (Exception e) {
            logger.error("获取用户信息失败: {}", e.getMessage());
            response.put("code", 401);
            response.put("message", "未授权");
        }

        return ResponseEntity.ok(response);
    }

    /**
     * 验证Token
     */
    @PostMapping("/validate")
    public ResponseEntity<Map<String, Object>> validateToken(@RequestHeader("Authorization") String authorization) {
        Map<String, Object> response = new HashMap<>();

        try {
            String token = authorization.replace("Bearer ", "");
            boolean isValid = authService.validateToken(token);

            if (isValid) {
                response.put("code", 200);
                response.put("message", "Token有效");
            } else {
                response.put("code", 401);
                response.put("message", "Token无效或已过期");
            }

        } catch (Exception e) {
            logger.error("验证Token失败: {}", e.getMessage());
            response.put("code", 401);
            response.put("message", "Token验证失败");
        }

        return ResponseEntity.ok(response);
    }
}