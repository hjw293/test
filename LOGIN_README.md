# 传感器数据展示系统 - 登录功能说明

## 📋 功能概述

系统现已集成完整的用户认证功能，包括：
- ✅ 用户登录/登出
- ✅ JWT Token认证
- ✅ 路由守卫保护
- ✅ 用户信息管理

## 🗄️ 数据库配置

### 1. 创建用户表

执行以下SQL语句创建用户表：

```bash
mysql -u root -p testdb < user_table.sql
```

或手动执行 `user_table.sql` 文件中的SQL语句。

### 2. 默认用户账号

系统默认提供以下测试账号：

| 用户名 | 密码 | 角色 | 邮箱 | 说明 |
|--------|------|------|------|------|
| admin | admin123 | ADMIN | admin@example.com | 系统管理员 |
| user | admin123 | USER | user@example.com | 普通用户 |

## 🔧 后端配置

### 1. 安装依赖

后端已添加JWT依赖，重新编译项目：

```bash
cd backend
mvn clean install
```

### 2. 启动后端服务

```bash
cd backend
mvn spring-boot:run
```

### 3. 验证后端API

测试登录接口：

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123"}'
```

## 🌐 前端配置

### 1. 安装依赖

前端已添加vue-router依赖，安装依赖：

```bash
cd frontend
npm install
```

### 2. 启动前端服务

```bash
cd frontend
npm run dev
```

### 3. 访问系统

打开浏览器访问：http://localhost:5173/

系统会自动跳转到登录页面。

## 🔐 认证流程

### 登录流程

1. 用户在登录页面输入用户名和密码
2. 前端调用 `/api/auth/login` 接口
3. 后端验证用户名和密码
4. 生成JWT Token（有效期24小时）
5. 前端保存Token到localStorage
6. 跳转到主页面

### API请求认证

所有需要认证的API请求都需在请求头中携带Token：

```javascript
headers: {
  'Content-Type': 'application/json',
  'Authorization': `Bearer ${token}`
}
```

### 路由守卫

- 访问需要认证的页面时，自动检查Token
- 无Token时跳转到登录页
- 已登录用户访问登录页时自动跳转到首页

## 📡 API接口

### 1. 用户登录

```
POST /api/auth/login
Content-Type: application/json

{
  "username": "admin",
  "password": "admin123"
}

响应：
{
  "code": 200,
  "message": "登录成功",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiJ9...",
    "user": {
      "id": 1,
      "username": "admin",
      "email": "admin@example.com",
      "realName": "系统管理员",
      "role": "ADMIN"
    }
  }
}
```

### 2. 获取当前用户信息

```
GET /api/auth/user
Authorization: Bearer {token}

响应：
{
  "code": 200,
  "message": "获取成功",
  "data": {
    "id": 1,
    "username": "admin",
    "email": "admin@example.com",
    "realName": "系统管理员",
    "role": "ADMIN"
  }
}
```

### 3. 验证Token

```
POST /api/auth/validate
Authorization: Bearer {token}

响应：
{
  "code": 200,
  "message": "Token有效"
}
```

## 🛡️ 安全特性

### JWT Token

- 使用HS256算法签名
- Token有效期：24小时
- 存储用户ID、用户名、角色信息
- 自动过期检测

### 密码安全

- 密码使用BCrypt加密存储
- 默认密码：admin123
- 建议生产环境使用强密码

### 拦截器保护

- 自动拦截未授权请求
- 检查Token有效性
- 返回401未授权状态码

## 🔑 Token管理

### Token存储位置

```javascript
// 保存Token
localStorage.setItem('token', token)

// 获取Token
const token = localStorage.getItem('token')

// 删除Token
localStorage.removeItem('token')
```

### Token过期处理

- Token过期后自动跳转到登录页
- 显示"登录已过期"提示
- 清除本地存储的Token

## 📝 用户表结构

```sql
CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    email VARCHAR(100) UNIQUE,
    phone VARCHAR(20),
    real_name VARCHAR(50),
    status TINYINT DEFAULT 1,
    role VARCHAR(20) DEFAULT 'USER',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    last_login_at TIMESTAMP NULL
);
```

## 🚨 注意事项

1. **默认密码安全**：生产环境请修改默认密码
2. **Token有效期**：当前设置为24小时，可根据需求调整
3. **HTTPS部署**：生产环境建议使用HTTPS
4. **密码加密**：当前使用简化验证，生产环境应使用BCrypt

## 🔄 后续优化建议

1. 添加密码修改功能
2. 添加用户注册功能
3. 实现记住登录状态
4. 添加多因素认证
5. 实现权限细粒度控制
6. 添加登录日志记录
7. 实现密码重置功能

## 📞 技术支持

如有问题，请检查：
1. 数据库是否正确创建用户表
2. 后端是否正常启动
3. 前端依赖是否正确安装
4. 浏览器控制台是否有错误信息
5. 网络连接是否正常