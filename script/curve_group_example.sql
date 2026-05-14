-- =====================================================
-- 曲线组主副轴实际数据示例
-- 展示不同业务场景下的主轴(curveType=1)和副轴(curveType=2)配置
-- =====================================================

USE testdb;

-- 创建曲线组配置表（如果不存在）
CREATE TABLE IF NOT EXISTS log_curve_group (
    id INT AUTO_INCREMENT PRIMARY KEY,
    source_file VARCHAR(255) COMMENT '源文件',
    group_name_id INT NOT NULL COMMENT '组名称ID',
    group_name VARCHAR(255) COMMENT '组名称',
    group_name_type INT DEFAULT 0 COMMENT '名称类型：0=name属性, 1=name_id文字',
    group_enable_nid INT DEFAULT 0 COMMENT '启动name_id：0=永久启动',
    group_enable_value INT DEFAULT 0 COMMENT '启动值',
    group_bg_color VARCHAR(20) DEFAULT '#FFFFFF' COMMENT '背景颜色',
    group_time_div_color VARCHAR(20) DEFAULT '#CCCCCC' COMMENT '时间标示线颜色',
    group_time BIGINT COMMENT '建立时间戳',
    curve_name_id INT NOT NULL COMMENT '曲线名称ID',
    curve_color VARCHAR(20) DEFAULT '#000000' COMMENT '曲线颜色',
    curve_type INT DEFAULT 1 COMMENT '曲线类型：0=标准, 1=主轴, 2=副轴',
    curve_ref INT COMMENT '曲线引用',
    curve_enable_nid INT DEFAULT 0 COMMENT '曲线启用节点ID',
    curve_mode INT DEFAULT 0 COMMENT '曲线模式：0=固定值, 1=节点值, 4=自动',
    curve_max BIGINT COMMENT '曲线最大值',
    curve_max_nid INT COMMENT '最大值节点ID',
    curve_max_row INT COMMENT '最大值行',
    curve_max_col INT COMMENT '最大值列',
    curve_min BIGINT COMMENT '曲线最小值',
    curve_min_nid INT COMMENT '最小值节点ID',
    curve_min_row INT COMMENT '最小值行',
    curve_min_col INT COMMENT '最小值列',
    curve_predict INT DEFAULT 0 COMMENT '预估曲线：0=否, 1=是',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_group_name_id (group_name_id),
    INDEX idx_curve_type (curve_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 清空现有数据（可选）
TRUNCATE TABLE log_curve_group;

-- =====================================================
-- 场景1：工业锅炉监控系统 (groupNameId = 327682001)
-- 业务背景：监控锅炉运行状态，需要观察温度、压力、流量等核心参数
--           以及氧含量、环境温度等辅助指标
-- =====================================================

INSERT INTO log_curve_group VALUES
-- ========== 主轴曲线（核心运行参数）==========
-- 曲线1001: 炉膛温度 - 核心安全指标
(NULL, 'boiler_2024_03.log', 327682001, '锅炉#1监控', 0, 0, 0, 
 '#FFFFFF', '#CCCCCC', 1711270800,
 1001, '#FF0000', 1, NULL, 0, 0, 1200, NULL, NULL, NULL, 800, NULL, NULL, NULL, 0, NOW()),

-- 曲线1002: 蒸汽压力 - 关键运行参数，与温度强相关
(NULL, 'boiler_2024_03.log', 327682001, '锅炉#1监控', 0, 0, 0, 
 '#FFFFFF', '#CCCCCC', 1711270800,
 1002, '#0000FF', 1, NULL, 0, 0, 15, NULL, NULL, NULL, 10, NULL, NULL, NULL, 0, NOW()),

-- 曲线1003: 给水流量 - 量级相近，便于对比分析
(NULL, 'boiler_2024_03.log', 327682001, '锅炉#1监控', 0, 0, 0, 
 '#FFFFFF', '#CCCCCC', 1711270800,
 1003, '#00FF00', 1, NULL, 0, 0, 200, NULL, NULL, NULL, 50, NULL, NULL, NULL, 0, NOW()),

-- ========== 副轴曲线（辅助监测指标）==========
-- 曲线1004: 排烟氧含量 - 量级太小(2-8%)，放主轴会变成直线
(NULL, 'boiler_2024_03.log', 327682001, '锅炉#1监控', 0, 0, 0, 
 '#FFFFFF', '#CCCCCC', 1711270800,
 1004, '#FFA500', 2, NULL, 0, 4, 8, NULL, NULL, NULL, 2, NULL, NULL, NULL, 0, NOW()),

-- 曲线1005: 环境温度 - 外部参考因素，非核心指标
(NULL, 'boiler_2024_03.log', 327682001, '锅炉#1监控', 0, 0, 0, 
 '#FFFFFF', '#CCCCCC', 1711270800,
 1005, '#800080', 2, NULL, 0, 4, 35, NULL, NULL, NULL, 15, NULL, NULL, NULL, 0, NOW());


-- =====================================================
-- 场景2：数据中心服务器监控 (groupNameId = 327682002)
-- 业务背景：监控服务器健康状态，观察温度和负载
--           同时监控风扇转速和功耗作为辅助诊断
-- =====================================================

INSERT INTO log_curve_group VALUES
-- ========== 主轴曲线（核心健康指标）==========
-- 曲线2001: CPU温度
(NULL, 'server_rack_01.log', 327682002, '服务器机架#1', 0, 0, 0, 
 '#F5F5F5', '#DDDDDD', 1711270800,
 2001, '#FF4444', 1, NULL, 0, 0, 90, NULL, NULL, NULL, 40, NULL, NULL, NULL, 0, NOW()),

-- 曲线2002: GPU温度
(NULL, 'server_rack_01.log', 327682002, '服务器机架#1', 0, 0, 0, 
 '#F5F5F5', '#DDDDDD', 1711270800,
 2002, '#4444FF', 1, NULL, 0, 0, 95, NULL, NULL, NULL, 50, NULL, NULL, NULL, 0, NOW()),

-- 曲线2003: CPU使用率
(NULL, 'server_rack_01.log', 327682002, '服务器机架#1', 0, 0, 0, 
 '#F5F5F5', '#DDDDDD', 1711270800,
 2003, '#44FF44', 1, NULL, 0, 4, 100, NULL, NULL, NULL, 0, NULL, NULL, NULL, 0, NOW()),

-- ========== 副轴曲线（辅助诊断指标）==========
-- 曲线2004: 风扇转速 - 量级差异大(800-3000 RPM)
(NULL, 'server_rack_01.log', 327682002, '服务器机架#1', 0, 0, 0, 
 '#F5F5F5', '#DDDDDD', 1711270800,
 2004, '#FFAA00', 2, NULL, 0, 4, 3000, NULL, NULL, NULL, 800, NULL, NULL, NULL, 0, NOW()),

-- 曲线2005: 功耗 - 不同单位(W)，参考指标
(NULL, 'server_rack_01.log', 327682002, '服务器机架#1', 0, 0, 0, 
 '#F5F5F5', '#DDDDDD', 1711270800,
 2005, '#AA00FF', 2, NULL, 0, 4, 500, NULL, NULL, NULL, 100, NULL, NULL, NULL, 0, NOW());


-- =====================================================
-- 场景3：化学反应釜监控 (groupNameId = 327682003)
-- 业务背景：监控化学反应过程，观察温度、搅拌、压力等工艺参数
--           同时监测pH值和液位作为质量控制指标
-- =====================================================

INSERT INTO log_curve_group VALUES
-- ========== 主轴曲线（核心工艺参数）==========
-- 曲线3001: 反应温度
(NULL, 'reactor_batch_042.log', 327682003, '反应釜批次#042', 0, 0, 0, 
 '#FFF8DC', '#E0E0E0', 1711270800,
 3001, '#FF0000', 1, NULL, 0, 0, 250, NULL, NULL, NULL, 150, NULL, NULL, NULL, 0, NOW()),

-- 曲线3002: 搅拌速度
(NULL, 'reactor_batch_042.log', 327682003, '反应釜批次#042', 0, 0, 0, 
 '#FFF8DC', '#E0E0E0', 1711270800,
 3002, '#0000FF', 1, NULL, 0, 0, 500, NULL, NULL, NULL, 100, NULL, NULL, NULL, 0, NOW()),

-- 曲线3003: 内部压力
(NULL, 'reactor_batch_042.log', 327682003, '反应釜批次#042', 0, 0, 0, 
 '#FFF8DC', '#E0E0E0', 1711270800,
 3003, '#00AA00', 1, NULL, 0, 0, 2, NULL, NULL, NULL, 0, NULL, NULL, NULL, 0, NOW()),

-- ========== 副轴曲线（质量控制指标）==========
-- 曲线3004: pH值 - 量级太小(0-14)，必须用副轴
(NULL, 'reactor_batch_042.log', 327682003, '反应釜批次#042', 0, 0, 0, 
 '#FFF8DC', '#E0E0E0', 1711270800,
 3004, '#FF6600', 2, NULL, 0, 4, 14, NULL, NULL, NULL, 0, NULL, NULL, NULL, 0, NOW()),

-- 曲线3005: 液位高度 - 百分比单位，辅助监控
(NULL, 'reactor_batch_042.log', 327682003, '反应釜批次#042', 0, 0, 0, 
 '#FFF8DC', '#E0E0E0', 1711270800,
 3005, '#9933CC', 2, NULL, 0, 4, 100, NULL, NULL, NULL, 0, NULL, NULL, NULL, 0, NOW());


-- =====================================================
-- 场景4：单轴示例 - 所有曲线量级相近 (groupNameId = 327682004)
-- 业务背景：监控多个温度传感器，数值范围接近，无需副轴
-- =====================================================

INSERT INTO log_curve_group VALUES
-- 所有曲线都用主轴，无需副轴
(NULL, 'temp_sensors.log', 327682004, '温度传感器阵列', 0, 0, 0, 
 '#FFFFFF', '#CCCCCC', 1711270800,
 4001, '#FF0000', 1, NULL, 0, 0, 100, NULL, NULL, NULL, 20, NULL, NULL, NULL, 0, NOW()),

(NULL, 'temp_sensors.log', 327682004, '温度传感器阵列', 0, 0, 0, 
 '#FFFFFF', '#CCCCCC', 1711270800,
 4002, '#00FF00', 1, NULL, 0, 0, 100, NULL, NULL, NULL, 20, NULL, NULL, NULL, 0, NOW()),

(NULL, 'temp_sensors.log', 327682004, '温度传感器阵列', 0, 0, 0, 
 '#FFFFFF', '#CCCCCC', 1711270800,
 4003, '#0000FF', 1, NULL, 0, 0, 100, NULL, NULL, NULL, 20, NULL, NULL, NULL, 0, NOW());


-- =====================================================
-- 查询验证
-- =====================================================

-- 查看每个曲线组的配置统计
SELECT 
    group_name_id AS '组ID',
    group_name AS '组名称',
    COUNT(*) AS '曲线总数',
    SUM(CASE WHEN curve_type IN (0, 1) THEN 1 ELSE 0 END) AS '主轴曲线数',
    SUM(CASE WHEN curve_type = 2 THEN 1 ELSE 0 END) AS '副轴曲线数',
    GROUP_CONCAT(DISTINCT curve_color ORDER BY curve_name_id) AS '颜色列表'
FROM log_curve_group
GROUP BY group_name_id, group_name
ORDER BY group_name_id;

-- 查看锅炉监控组的详细配置
SELECT 
    curve_name_id AS '曲线ID',
    curve_color AS '颜色',
    CASE curve_type 
        WHEN 0 THEN '标准'
        WHEN 1 THEN '主轴'
        WHEN 2 THEN '副轴'
    END AS '曲线类型',
    curve_min AS '最小值',
    curve_max AS '最大值',
    CASE curve_mode
        WHEN 0 THEN '固定值'
        WHEN 1 THEN '节点值'
        WHEN 4 THEN '自动'
        ELSE '未知'
    END AS '模式'
FROM log_curve_group
WHERE group_name_id = 327682001
ORDER BY curve_type DESC, curve_name_id;

-- 计算每个组的Y轴范围（前端会自动计算）
SELECT 
    group_name_id,
    curve_type,
    MIN(curve_min) AS '合并后最小值',
    MAX(curve_max) AS '合并后最大值',
    (MAX(curve_max) - MIN(curve_min)) * 0.1 AS '10%边距',
    MIN(curve_min) - (MAX(curve_max) - MIN(curve_min)) * 0.1 AS '最终min',
    MAX(curve_max) + (MAX(curve_max) - MIN(curve_min)) * 0.1 AS '最终max'
FROM log_curve_group
GROUP BY group_name_id, curve_type
ORDER BY group_name_id, curve_type;
