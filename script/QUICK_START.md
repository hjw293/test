# 快速启动指南

## 前置要求

- **Java 17+** (后端)
- **MySQL 8.0+** (数据库)
- **Node.js 16+** (前端)
- **Maven** (后端构建)

## 一键启动流程

### 步骤 1: 数据库初始化

```bash
# 使用 MySQL 命令行或工具（如 MySQL Workbench、Navicat）
# 执行以下命令：

### 步骤 2: 启动后端服务

```bash
# 进入后端目录
cd backend

# 编译项目
mvn clean compile

# 启动 Spring Boot 应用
mvn spring-boot:run

# 或者在 IDE 中运行 SensorDataApplication.main()
```

**验证后端**

打开浏览器访问：
```
http://localhost:8080/api/data
```

应该返回类似的 JSON 响应：
```json
{
  "code": 200,
  "message": "Success",
  "data": {
    "Device_A": [...],
    "Device_B": [...],
    "Device_C": [...]
  }
}
```

### 步骤 3: 启动前端应用

```bash
# 进入前端目录
cd frontend

# 安装依赖（首次运行）
npm install

# 启动开发服务器
npm run dev
```

**访问应用**

打开浏览器访问：
```
http://localhost:5173
```

应该看到一个美化的界面，展示多个设备的传感器数据曲线图。

## 常用命令

### 后端

```bash
# 编译
mvn clean compile

# 运行
mvn spring-boot:run

# 打包为 JAR 文件
mvn clean package

# 跳过测试进行打包
mvn clean package -DskipTests

# 运行已打包的 JAR
java -jar target/sensor-backend-1.0.0.jar
```

### 前端

```bash
# 安装依赖
npm install

# 开发模式（带热更新）
npm run dev

# 构建生产版本
npm run build

# 预览生产构建
npm run preview

# 清空依赖和构建文件
rm -rf node_modules dist
```

## 生产部署

### 后端部署

```bash
# 1. 在后端目录执行打包
mvn clean package -DskipTests

# 2. 上传 target/sensor-backend-1.0.0.jar 到服务器

# 3. 修改配置文件 application.yml（如需要）
# 改为生产数据库连接信息

# 4. 在服务器上运行
java -jar sensor-backend-1.0.0.jar
```

### 前端部署

```bash
# 1. 在前端目录构建
npm run build

# 2. 将 dist 目录上传到 Web 服务器（Nginx、Apache 等）

# 3. 配置你的生产环境后端地址
# 编辑 src/App.vue 中的 API_URL
```

## 故障排除

### 问题 1: `Access to XMLHttpRequest has been blocked by CORS policy`

**原因**: 跨域请求被拒绝

**解决**:
- 确保后端 `CorsConfig.java` 中的 `allowedOrigins` 包含前端地址
- 后端默认允许 `http://localhost:5173`

### 问题 2: `Cannot connect to MySQL`

**原因**: MySQL 连接失败

**解决**:
- 检查 MySQL 是否启动：`mysql -u root -p`
- 验证数据库和用户名密码
- 检查 `application.yml` 中的配置

### 问题 3: 前端显示 "获取数据失败"

**原因**: 后端服务未启动或连接超时

**解决**:
- 确保后端运行在 8080 端口
- 在浏览器中直接访问 `http://localhost:8080/api/data` 测试
- 检查防火墙设置

### 问题 4: 图表不显示

**原因**: 数据库中没有数据

**解决**:
- 重新执行 `schema.sql` 确保有模拟数据
- 查看浏览器控制台（F12）是否有 JavaScript 错误

## 项目文件说明

```
schema.sql                    - 数据库建表和模拟数据脚本

backend/
├── pom.xml                   - Maven 项目配置
├── src/main/
│   ├── java/com/example/sensor/
│   │   ├── SensorDataApplication.java       - 应用启动类
│   │   ├── controller/SensorDataController.java  - REST API 接口
│   │   ├── service/SensorDataService.java   - 业务逻辑接口
│   │   ├── service/impl/SensorDataServiceImpl.java - 业务逻辑实现
│   │   ├── entity/SensorData.java           - 数据实体
│   │   ├── mapper/SensorDataMapper.java     - 数据映射
│   │   └── config/CorsConfig.java           - 跨域配置
│   └── resources/application.yml            - 应用配置

frontend/
├── package.json              - npm 项目配置
├── vite.config.js            - Vite 构建配置
├── index.html                - HTML 入口
└── src/
    ├── main.js               - JavaScript 入口
    └── App.vue               - Vue 主组件（包含图表逻辑）
```

## 下一步

1. **自定义图表**: 编辑 `App.vue` 中的 ECharts 配置
2. **添加更多功能**: 如数据导出、时间范围筛选等
3. **美化界面**: 根据需要调整 CSS 样式
4. **添加认证**: 实现用户登录功能
5. **性能优化**: 分页加载数据、数据缓存等
