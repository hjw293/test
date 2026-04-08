-- 数据库性能优化 SQL 脚本
-- 请在 MySQL 中执行这些语句来优化数据库性能

USE testdb;

-- 1. 检查当前表结构
SHOW CREATE TABLE sensor_data;

-- 2. 分析表，优化存储
ANALYZE TABLE sensor_data;

-- 3. 检查索引使用情况
EXPLAIN SELECT * FROM sensor_data LIMIT 10;

-- 4. 如果 real_time 字段不存在，添加它
-- ALTER TABLE sensor_data ADD COLUMN real_time VARCHAR(20);
-- UPDATE sensor_data SET real_time = FROM_UNIXTIME(timestamp/1000, '%Y-%m-%d %H:%i:%s') WHERE real_time IS NULL;

-- 5. 为常用查询字段添加复合索引
-- 注意：如果数据量很大，添加索引可能需要较长时间
CREATE INDEX IF NOT EXISTS idx_device_timestamp ON sensor_data(source_file, timestamp);
CREATE INDEX IF NOT EXISTS idx_real_time ON sensor_data(real_time);

-- 6. 检查表的存储引擎和字符集
SHOW TABLE STATUS WHERE Name = 'sensor_data';

-- 7. 优化表
OPTIMIZE TABLE sensor_data;

-- 8. 查看表的行数和大小
SELECT
    table_name,
    table_rows,
    ROUND(data_length / 1024 / 1024, 2) AS data_size_mb,
    ROUND(index_length / 1024 / 1024, 2) AS index_size_mb
FROM information_schema.tables
WHERE table_schema = 'testdb' AND table_name = 'sensor_data';