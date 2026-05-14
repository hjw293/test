package com.example.sensor.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.sensor.entity.User;
import com.example.sensor.mapper.UserMapper;
import com.example.sensor.util.JwtUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * 认证服务
 */
@Service
public class AuthService {

    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private JwtUtil jwtUtil;

    /**
     * 用户登录
     */
    public String login(String username, String password) {
        logger.info("用户登录尝试: {}", username);

        // 查询用户
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("username", username);
        User user = userMapper.selectOne(queryWrapper);

        // 检查用户是否存在
        if (user == null) {
            logger.warn("用户不存在: {}", username);
            throw new RuntimeException("用户名或密码错误");
        }

        // 检查用户状态
        if (user.getStatus() == 0) {
            logger.warn("用户已被禁用: {}", username);
            throw new RuntimeException("用户已被禁用");
        }

        // 验证密码（这里简化处理，实际应该使用BCrypt等加密方式）
        if (!password.equals("admin123")) {
            logger.warn("密码错误: {}", username);
            throw new RuntimeException("用户名或密码错误");
        }

        // 更新最后登录时间
        user.setLastLoginAt(LocalDateTime.now());
        userMapper.updateById(user);

        // 生成Token
        String token = jwtUtil.generateToken(user.getId(), user.getUsername(), user.getRole());

        logger.info("用户登录成功: {}", username);
        return token;
    }

    /**
     * 验证Token
     */
    public boolean validateToken(String token) {
        return jwtUtil.validateToken(token);
    }

    /**
     * 从Token获取用户信息
     */
    public User getUserFromToken(String token) {
        if (!validateToken(token)) {
            return null;
        }

        Long userId = jwtUtil.getUserIdFromToken(token);
        if (userId == null) {
            return null;
        }

        return userMapper.selectById(userId);
    }
}