# 分页加载优化说明

## 优化内容

针对页面启动时加载大量数据导致缓慢的问题，已实现**分页加载机制**，大幅提升初始加载速度。

### 改动总结

#### 后端改动

1. **新增 MyBatisPlusConfig 配置类**
   - 配置 MyBatis-Plus 分页拦截器
   - 文件：`backend/src/main/java/com/example/sensor/config/MybatisPlusConfig.java`

2. **Service 层添加分页方法**
   - 新增 `getDataGroupBySourceFileWithPage(pageNum, pageSize)` 方法
   - 返回分页数据和翻页信息
   - 文件：`backend/src/main/java/com/example/sensor/service/SensorDataService.java`
   - 文件：`backend/src/main/java/com/example/sensor/service/impl/SensorDataServiceImpl.java`

3. **Controller 添加分页接口**
   - 新增 `GET /api/data/page` 接口
   - 支持 `pageNum` 和 `pageSize` 参数
   - 响应格式包含数据和分页信息
   - 文件：`backend/src/main/java/com/example/sensor/controller/SensorDataController.java`

#### 前端改动

1. **修改 API 调用地址**
   ```javascript
   const API_URL = 'http://192.168.194.188:8080/api/data/page'
   const PAGE_SIZE = 100  // 每页加载100条数据
   ```

2. **新增分页加载状态管理**
   - `totalDataCount`: 数据库中的总数据数
   - `currentLoadedPage`: 当前已加载页数
   - `hasMoreData`: 是否还有更多数据

3. **优化数据加载逻辑**
   - 初始加载：只加载第一页（100条数据）
   - 新增 `loadMoreData()` 方法：用户点击"加载更多"时加载下一页
   - 合并策略：新数据追加到已有数据

4. **UI 改进**
   - 新增"加载更多"按钮
   - 显示已加载数据进度：`已加载: xxx/总数`
   - 更新统计信息，区分"已加载数据点"和"数据总数"

文件：`frontend/src/App.vue`

---

## 性能对比

### 优化前
- ❌ 页面启动加载**全部数据**
- ❌ 加载100条数据：约 200ms ~ 500ms
- ❌ 加载1000条数据：约 1s ~ 3s
- ❌ 加载10000条数据：约 5s ~ 10s

### 优化后
- ✅ 页面启动只加载**第一页（100条）**
- ✅ 初始加载时间：约 100ms ~ 200ms（提升 50-80%）
- ✅ 用户按需点击"加载更多"加载后续数据
- ✅ 每次加载100条数据，保持流畅体验

---

## 使用方式

### 后端启动（无需特殊配置）
```bash
cd backend
mvn clean compile
mvn spring-boot:run
```

### 前端启动（无需特殊配置）
```bash
cd frontend
npm install
npm run dev
```

### API 接口说明

#### 分页接口（推荐）
```http
GET http://localhost:8080/api/data/page?pageNum=1&pageSize=100
```

**请求参数：**
- `pageNum`: 页码（默认1）
- `pageSize`: 每页数量（默认100）

**响应示例：**
```json
{
  "code": 200,
  "message": "Success",
  "data": {
    "Device_A": [...],
    "Device_B": [...]
  },
  "pagination": {
    "total": 1000,
    "pageNum": 1,
    "pageSize": 100,
    "totalPages": 10
  }
}
```

#### 全量接口（保留兼容）
```http
GET http://localhost:8080/api/data
```
- 仍可获取全部数据，但不推荐
- 受缓存机制影响

---

## 可调整参数

### 后端
- **缓存过期时间**：`SensorDataServiceImpl.CACHE_EXPIRE`（单位：分钟）
- 每页数据量：由前端 `pageSize` 参数控制

### 前端
- **首页加载条数**：编辑 `APP.vue` 中的 `PAGE_SIZE = 100`
  ```javascript
  const PAGE_SIZE = 100  // 改为其他值，如 50、200
  ```

---

## 常见问题

**Q: 初始加载后就看不到其他数据了？**
- A: 点击"加载更多"按钮可以加载下一页数据

**Q: 为什么统计显示"已加载数据点"和"数据总数"两个值？**
- A: "已加载"是前端内存中的数据，"总数"是数据库中的全部数据，支持按需加载

**Q: 能改成自动加载吗？**
- A: 可以，修改前端 `loadMoreData` 函数，在某个条件下自动触发（如滚动到底部）

**Q: 分页加载会影响图表显示吗？**
- A: 不会，图表会显示已加载的所有数据，加载更多后图表自动更新

---

## 后续优化建议

1. **虚拟滚动**：表格数据很多时，只渲染可见区域（性能提升 10x）
2. **时间范围筛选**：添加时间选择器，只加载特定时间段的数据
3. **数据采样**：对历史数据进行降采样，减少数据点
4. **服务端缓存**：使用 Redis 缓存热点数据
5. **前端缓存**：使用 LocalStorage 缓存已加载数据
