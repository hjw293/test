# 传感器数据展示系统

这是一个前后端分离的传感器数据展示项目，用于实时展示来自多个设备的传感器数据曲线图。

## 项目结构

```
.
├── schema.sql           # 数据库建表及模拟数据
├── backend/             # Spring Boot 后端项目
│   ├── pom.xml         # Maven 配置
│   ├── src/main/resources/application.yml  # 配置文件
│   └── src/main/java/com/example/sensor/   # Java 源代码
└── frontend/            # Vue 3 + Vite 前端项目
    ├── package.json    # npm 配置
    ├── vite.config.js  # Vite 配置
    ├── index.html      # HTML 入口
    └── src/            # Vue 源代码
```

## 技术栈

### 后端
- **Java 17+**
- **Spring Boot 3.2.3**
- **MyBatis-Plus 3.5.5**
- **MySQL 8.0+**
- **Lombok**

### 前端
- **Vue 3**
- **Vite**
- **Element Plus**
- **ECharts**
- **Axios**

## 快速开始

### 1. 数据库配置

使用 MySQL 8.0+ 执行 `schema.sql` 文件：

```sql
-- 在 MySQL 命令行或客户端中执行
source ./schema.sql;
```

或者手动创建：
- 数据库：`testdb`
- 表：`sensor_data`
- 根据 `schema.sql` 中的 SQL 语句建表

**修改数据库连接信息**（如需要）：
- 编辑 `backend/src/main/resources/application.yml`
- 修改 `username` 和 `password` 为你的 MySQL 凭证

### 2. 后端启动

```bash
cd backend

# 使用 Maven 编译并运行
mvn clean compile
mvn spring-boot:run

# 或使用 IDE 启动 SensorDataApplication 类
```

后端服务将运行在 `http://localhost:8080`

**后端接口：**
- `GET /api/data` - 获取所有传感器数据（按设备名分组）

### 3. 前端启动

```bash
cd frontend

# 安装依赖
npm install

# 启动开发服务器
npm run dev

# 构建生产版本
npm build
```

前端应用将运行在 `http://localhost:5173`

## API 文档

### 获取传感器数据

**请求**
```http
GET http://localhost:8080/api/data
```

**响应示例**
```json
{
  "code": 200,
  "message": "Success",
  "data": {
    "Device_A": [
      {
        "id": 1,
        "sourceFile": "Device_A",
        "timestamp": 1711270800000,
        "value": 23.5,
        "createdAt": "2024-03-24T10:00:00"
      },
      ...
    ],
    "Device_B": [
      ...
    ]
  }
}
```

## 功能特性

✨ **主要功能**
- 支持多个设备的实时数据展示
- 折线图展示传感器数据趋势
- 设备选择与切换（图例可点击）
- 数据统计信息卡片
- 自动数据刷新
- 响应式设计，支持多种屏幕尺寸
- 全局 CORS 跨域配置

## 模拟数据

系统包含 3 个模拟设备的数据：
- **Device_A**: 温度传感器数据（23-25°C）
- **Device_B**: 温度传感器数据（18-19°C）
- **Device_C**: 温度传感器数据（45-46°C）

每个设备有 3-5 条数据点。

## 配置说明

### 后端配置 (`application.yml`)

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/testdb?useSSL=false&serverTimezone=UTC
    username: root      # 修改为你的 MySQL 用户名
    password: root      # 修改为你的 MySQL 密码
    driver-class-name: com.mysql.cj.jdbc.Driver

server:
  port: 8080
```

### 前端 CORS 配置

前端默认配置在 `http://localhost:5173`。若要修改，编辑后端的 `CorsConfig.java`：

```java
registry.addMapping("/**")
    .allowedOrigins("http://your-frontend-url:your-port")
```

## 开发说明

### 添加新的设备数据

1. 在 MySQL 中插入数据到 `sensor_data` 表
2. 前端会自动从后端获取新数据并展示

### 修改图表样式

编辑 `frontend/src/App.vue` 中的 `updateChart()` 方法中的 `option` 对象。

### 修改数据查询逻辑

编辑 `backend/src/main/java/com/example/sensor/service/impl/SensorDataServiceImpl.java`

## 常见问题

**Q: 前端连接不到后端？**
- A: 确保后端运行在 8080 端口，前端在 5173 端口
- 检查防火墙设置
- 检查 CORS 配置是否正确

**Q: 数据库连接失败？**
- A: 检查 MySQL 是否运行
- 确认数据库名称为 `testdb`
- 修改 `application.yml` 中的用户名和密码

**Q: 图表不显示？**
- A: 检查浏览器控制台是否有错误
- 确保数据库中有数据
- 尝试点击"刷新数据"按钮

## 许可证

MIT

## 联系方式

如有问题，欢迎提出 Issue 或 Pull Request。

文件结构
-CCSHAServer
	-local_storage
		-2026-03
			-01
				-curve
					-machine01