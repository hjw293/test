package com.example.sensor;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Spring Boot 应用启动类
 */
@SpringBootApplication
@MapperScan("com.example.sensor.mapper")
public class SensorDataApplication {

    public static void main(String[] args) {
        SpringApplication.run(SensorDataApplication.class, args);
    }
}
