CREATE DATABASE IF NOT EXISTS testdb CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE testdb;

CREATE TABLE IF NOT EXISTS sensor_data (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    source_file VARCHAR(100) NOT NULL,
    timestamp BIGINT NOT NULL,
    value DOUBLE NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_source_file (source_file),
    INDEX idx_timestamp (timestamp)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT INTO sensor_data (source_file, timestamp, value) VALUES
('Device_A', 1711270800000, 23.5),
('Device_A', 1711270860000, 24.1),
('Device_A', 1711270920000, 23.8),
('Device_A', 1711270980000, 24.5),
('Device_A', 1711271040000, 24.2);

INSERT INTO sensor_data (source_file, timestamp, value) VALUES
('Device_B', 1711270800000, 18.2),
('Device_B', 1711270860000, 18.9),
('Device_B', 1711270920000, 18.5),
('Device_B', 1711270980000, 19.3),
('Device_B', 1711271040000, 19.1);

INSERT INTO sensor_data (source_file, timestamp, value) VALUES
('Device_C', 1711270800000, 45.2),
('Device_C', 1711270860000, 46.1),
('Device_C', 1711270920000, 45.8);
