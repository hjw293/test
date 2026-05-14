-- 创建警报配置表
USE testdb;

CREATE TABLE IF NOT EXISTS alarm_config (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    alarm_key VARCHAR(100) NOT NULL COMMENT '警报ID',
    language_name VARCHAR(50) COMMENT '警报内容的语言',
    alarm_text TEXT COMMENT '警报的内容',
    text_color VARCHAR(20) COMMENT '警报内容的颜色（RGB格式）',
    bg_color VARCHAR(20) COMMENT '警报的颜色（RGB格式）',
    response_req VARCHAR(50) COMMENT '警报的性质',
    machine_action VARCHAR(50) COMMENT '警报的处理方式',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_alarm_key (alarm_key)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='警报配置表';

-- 插入测试数据
INSERT INTO alarm_config (alarm_key, language_name, alarm_text, text_color, bg_color, response_req, machine_action) VALUES
('TEMP_HIGH', '中文', '温度过高警报', '#FFFFFF', '#FF0000', '立即处理', '停机'),
('TEMP_LOW', '中文', '温度过低警报', '#FFFFFF', '#0000FF', '需要处理', '减速'),
('PRESSURE_HIGH', '中文', '压力过高警报', '#FFFFFF', '#FFA500', '立即处理', '紧急停机'),
('PRESSURE_LOW', '中文', '压力过低警报', '#FFFFFF', '#800080', '需要处理', '减速运行'),
('VIBRATION_HIGH', '中文', '振动过高警报', '#FFFFFF', '#FF6347', '立即处理', '紧急停机'),
('FLOW_HIGH', '中文', '流量过高警报', '#FFFFFF', '#32CD32', '需要处理', '调节流量'),
('FLOW_LOW', '中文', '流量过低警报', '#FFFFFF', '#FFD700', '需要处理', '调节流量'),
('LEVEL_HIGH', '中文', '液位过高警报', '#FFFFFF', '#FF1493', '立即处理', '排液'),
('LEVEL_LOW', '中文', '液位过低警报', '#FFFFFF', '#00CED1', '需要处理', '补液'),
('MOTOR_ERROR', '中文', '电机故障警报', '#FFFFFF', '#8B0000', '立即处理', '停机检查'),
('SENSOR_FAULT', '中文', '传感器故障', '#FFFFFF', '#DC143C', '立即处理', '更换传感器'),
('NETWORK_ERROR', '中文', '网络连接异常', '#FFFFFF', '#4B0082', '需要处理', '检查网络连接'),
('POWER_ERROR', '中文', '电源异常', '#FFFFFF', '#FF4500', '立即处理', '检查电源'),
('FAN_ERROR', '中文', '风扇故障', '#FFFFFF', '#DAA520', '需要处理', '检查风扇'),
('COOLING_ERROR', '中文', '冷却系统故障', '#FFFFFF', '#006400', '立即处理', '检查冷却系统'),
('EMERGENCY_STOP', '中文', '紧急停止', '#FFFFFF', '#000000', '立即处理', '系统停机');