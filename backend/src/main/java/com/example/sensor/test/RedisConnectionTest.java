package com.example.sensor.test;

import redis.clients.jedis.Jedis;

public class RedisConnectionTest {
    public static void main(String[] args) {
        try {
            // 尝试连接到Redis服务器
            Jedis jedis = new Jedis("127.0.0.1", 6379);
            // 尝试使用密码认证
            jedis.auth("123456");
            // 执行PING命令
            String result = jedis.ping();
            System.out.println("Redis连接测试结果: " + result);
            // 关闭连接
            jedis.close();
        } catch (Exception e) {
            System.err.println("Redis连接测试失败: " + e.getMessage());
            e.printStackTrace();
        }
    }
}