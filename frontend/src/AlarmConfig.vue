<template>
  <div class="container">
    <el-header class="header">
      <h1>警报配置管理系统</h1>
      <div class="header-right">
        <el-button type="default" @click="goBack" class="back-btn">
          <el-icon><ArrowLeft /></el-icon>
          返回
        </el-button>
        <el-dropdown @command="handleLogout" trigger="click">
          <span class="user-info">
            <el-icon><User /></el-icon>
            <span>{{ currentUser?.username || '用户' }}</span>
            <el-icon class="el-icon--right"><ArrowDown /></el-icon>
          </span>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item command="logout">退出登录</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </div>
    </el-header>

    <el-main class="main-content">
      <el-card class="content-card" shadow="hover">
        <template #header>
          <div class="card-header">
            <div class="header-title">警报配置列表</div>
            <div class="header-controls">
              <el-input
                v-model="searchAlarmKey"
                placeholder="搜索警报ID"
                style="width: 200px; margin-right: 10px;"
                clearable
                @clear="handleSearch"
              >
                <template #prefix>
                  <el-icon><Search /></el-icon>
                </template>
              </el-input>
              <el-button type="primary" :loading="loading" @click="handleSearch">
                搜索
              </el-button>
            </div>
          </div>
        </template>

        <div v-loading="loading" class="alarm-content">
          <el-alert
            v-if="errorMessage"
            :title="errorMessage"
            type="error"
            :closable="true"
            style="margin-bottom: 16px;"
          />

          <div v-if="alarmList.length > 0" class="alarm-grid">
            <el-card
              v-for="alarm in alarmList"
              :key="alarm.alarmKey"
              class="alarm-card"
              shadow="hover"
              @click="showAlarmDetail(alarm)"
            >
              <div class="alarm-card-header">
                <div class="alarm-key">{{ alarm.alarmKey }}</div>
                <div
                  class="alarm-color-indicator"
                  :style="{ backgroundColor: alarm.bgColor }"
                  :title="`背景颜色: ${alarm.bgColor}`"
                ></div>
              </div>
              
              <div class="alarm-card-body">
                <div class="alarm-info-row">
                  <span class="info-label">警报内容:</span>
                  <span
                    class="info-value alarm-text"
                    :style="{ color: alarm.textColor }"
                  >
                    {{ alarm.alarmText }}
                  </span>
                </div>
                
                <div class="alarm-info-row">
                  <span class="info-label">警报性质:</span>
                  <span class="info-value">{{ alarm.responseReq }}</span>
                </div>
                
                <div class="alarm-info-row">
                  <span class="info-label">处理方式:</span>
                  <span class="info-value">{{ alarm.machineAction }}</span>
                </div>
              </div>
            </el-card>
          </div>

          <!-- 放大详情对话框 -->
          <el-dialog
            v-model="dialogVisible"
            width="680px"
            class="alarm-detail-dialog"
            :close-on-click-modal="true"
            destroy-on-close
          >
            <template #header>
              <div class="dialog-header">
                <div class="dialog-header-left">
                  <div
                    class="alarm-icon-wrapper"
                    :style="{ background: `linear-gradient(135deg, ${selectedAlarm?.bgColor} 0%, ${adjustColor(selectedAlarm?.bgColor, -30)} 100%)` }"
                  >
                    <el-icon class="alarm-icon"><Warning /></el-icon>
                  </div>
                  <div class="dialog-title-group">
                    <div class="dialog-title">{{ selectedAlarm?.alarmKey }}</div>
                    <div class="dialog-subtitle">警报配置详情</div>
                  </div>
                </div>
              </div>
            </template>
            
            <div class="alarm-detail-content">
              <div class="detail-row-row">
                <div class="detail-col large">
                  <div class="section-title">
                    <el-icon><Document /></el-icon>
                    警报内容
                  </div>
                  <div class="detail-card">
                    <div
                      class="detail-content alarm-text"
                      :style="{ 
                        color: selectedAlarm?.textColor,
                        textShadow: `0 1px 2px rgba(${hexToRgb(selectedAlarm?.bgColor)}, 0.1)`
                      }"
                    >
                      {{ selectedAlarm?.alarmText }}
                    </div>
                  </div>
                </div>
                
                <div class="detail-col">
                  <div class="section-title">
                    <el-icon><InfoFilled /></el-icon>
                    警报性质
                  </div>
                  <div class="detail-card">
                    <div class="detail-content">{{ selectedAlarm?.responseReq }}</div>
                  </div>
                </div>
                
                <div class="detail-col">
                  <div class="section-title">
                    <el-icon><Setting /></el-icon>
                    处理方式
                  </div>
                  <div class="detail-card">
                    <div class="detail-content">{{ selectedAlarm?.machineAction }}</div>
                  </div>
                </div>
              </div>
            </div>
          </el-dialog>

          <el-empty v-if="alarmList.length === 0 && !loading" description="暂无警报配置数据" />
        </div>

        <div v-if="total > 0" class="pagination-container">
          <el-pagination
            v-model:current-page="currentPage"
            v-model:page-size="pageSize"
            :page-sizes="[12, 24, 48]"
            :total="total"
            layout="total, sizes, prev, pager, next, jumper"
            @size-change="handleSizeChange"
            @current-change="handleCurrentChange"
          />
        </div>
      </el-card>
    </el-main>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import axios from 'axios'
import { ArrowLeft, User, ArrowDown, Search, Warning, Document, InfoFilled, Setting } from '@element-plus/icons-vue'

const router = useRouter()

// 当前用户信息
const currentUser = computed(() => {
  const userStr = localStorage.getItem('user')
  return userStr ? JSON.parse(userStr) : null
})

const API_URL = 'http://localhost:8080/api/alarm/page'

// 数据状态
const alarmList = ref([])
const loading = ref(false)
const errorMessage = ref('')
const currentPage = ref(1)
const pageSize = ref(12)
const total = ref(0)
const searchAlarmKey = ref('')

// 对话框状态
const dialogVisible = ref(false)
const selectedAlarm = ref(null)

// 获取警报配置数据
const fetchAlarmData = async () => {
  loading.value = true
  errorMessage.value = ''
  
  try {
    const token = localStorage.getItem('token')
    
    const params = {
      pageNum: currentPage.value,
      pageSize: pageSize.value
    }
    
    if (searchAlarmKey.value && searchAlarmKey.value.trim()) {
      params.alarmKey = searchAlarmKey.value.trim()
    }
    
    const response = await axios.get(API_URL, {
      params,
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${token}`
      }
    })

    if (response.data.code === 200) {
      alarmList.value = response.data.data || []
      total.value = response.data.pagination?.total || 0
    } else {
      errorMessage.value = response.data.message || '获取数据失败'
      alarmList.value = []
      total.value = 0
    }
  } catch (error) {
    console.error('Error fetching alarm data:', error)
    if (error.response && error.response.status === 401) {
      errorMessage.value = '登录已过期，请重新登录'
      localStorage.removeItem('token')
      localStorage.removeItem('user')
      router.push('/login')
    } else {
      errorMessage.value = `请求失败: ${error.message || '无法连接到后端服务器'}`
    }
    alarmList.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}

// 搜索
const handleSearch = () => {
  currentPage.value = 1
  fetchAlarmData()
}

// 显示警报详情（放大效果）
const showAlarmDetail = (alarm) => {
  selectedAlarm.value = alarm
  dialogVisible.value = true
}

// 颜色处理函数
const hexToRgb = (hex) => {
  if (!hex) return '255, 255, 255'
  const result = /^#?([a-f\d]{2})([a-f\d]{2})([a-f\d]{2})$/i.exec(hex)
  return result 
    ? `${parseInt(result[1], 16)}, ${parseInt(result[2], 16)}, ${parseInt(result[3], 16)}`
    : '255, 255, 255'
}

const adjustColor = (hex, amount) => {
  if (!hex) return '#409EFF'
  const num = parseInt(hex.replace('#', ''), 16)
  const r = Math.min(255, Math.max(0, (num >> 16) + amount))
  const g = Math.min(255, Math.max(0, ((num >> 8) & 0x00FF) + amount))
  const b = Math.min(255, Math.max(0, (num & 0x0000FF) + amount))
  return `#${(1 << 24 | r << 16 | g << 8 | b).toString(16).slice(1)}`
}

// 处理每页显示条数变化
const handleSizeChange = (size) => {
  pageSize.value = size
  currentPage.value = 1
  fetchAlarmData()
}

// 处理页码变化
const handleCurrentChange = (current) => {
  currentPage.value = current
  fetchAlarmData()
}

// 返回上一页
const goBack = () => {
  router.push('/')
}

// 退出登录
const handleLogout = () => {
  localStorage.removeItem('token')
  localStorage.removeItem('user')
  router.push('/login')
}

// 页面加载时获取数据
onMounted(() => {
  fetchAlarmData()
})
</script>

<style scoped>
.container {
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 20px;
}

.header {
  background: transparent;
  border-radius: 16px;
  margin-bottom: 20px;
  padding: 20px 40px;
  border: none;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.header h1 {
  color: #fff;
  font-size: 28px;
  font-weight: 600;
  text-shadow: 0 2px 4px rgba(0, 0, 0, 0.2);
  margin: 0;
  text-align: left;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 12px;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #fff;
  cursor: pointer;
  padding: 8px 16px;
  border-radius: 8px;
  transition: all 0.3s ease;
  background: rgba(255, 255, 255, 0.1);
}

.user-info:hover {
  background: rgba(255, 255, 255, 0.2);
}

.back-btn {
  background: rgba(255, 255, 255, 0.9);
  border: none;
  color: #667eea;
  font-weight: 500;
}

.back-btn:hover {
  background: #fff;
  color: #764ba2;
}

.main-content {
  padding: 0;
}

.content-card {
  background: rgba(255, 255, 255, 0.95);
  border-radius: 16px;
  border: none;
}

.content-card :deep(.el-card__header) {
  background: rgba(255, 255, 255, 0.98);
  padding: 20px;
  border-bottom: 2px solid #f0f0f0;
  border-radius: 16px 16px 0 0;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-title {
  font-size: 18px;
  font-weight: 600;
  color: #333;
}

.header-controls {
  display: flex;
  align-items: center;
}

.alarm-content {
  min-height: 400px;
}

.alarm-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 20px;
  margin-bottom: 20px;
}

.alarm-card {
  border-radius: 12px;
  border: none;
  transition: all 0.3s ease;
  cursor: pointer;
}

.alarm-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
}

.alarm-card:active {
  transform: scale(1.05);
  box-shadow: 0 12px 32px rgba(0, 0, 0, 0.15);
}

.alarm-card :deep(.el-card__body) {
  padding: 16px;
}

.alarm-card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
  padding-bottom: 12px;
  border-bottom: 2px solid #f0f0f0;
}

.alarm-key {
  font-size: 16px;
  font-weight: 600;
  color: #333;
}

.alarm-color-indicator {
  width: 24px;
  height: 24px;
  border-radius: 50%;
  border: 2px solid #e0e0e0;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.alarm-card-body {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.alarm-info-row {
  display: flex;
  align-items: flex-start;
  gap: 8px;
}

.info-label {
  font-size: 13px;
  color: #909399;
  font-weight: 500;
  min-width: 60px;
  flex-shrink: 0;
}

.info-value {
  font-size: 13px;
  color: #606266;
  word-break: break-all;
  line-height: 1.4;
}

.alarm-text {
  font-weight: 500;
}

.pagination-container {
  display: flex;
  justify-content: center;
  margin-top: 20px;
  padding-top: 20px;
  border-top: 1px solid #f0f0f0;
}

@media (max-width: 1400px) {
  .alarm-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 768px) {
  .header {
    flex-direction: column;
    gap: 16px;
    text-align: center;
  }

  .header h1 {
    text-align: center;
  }

  .alarm-grid {
    grid-template-columns: 1fr;
  }

  .card-header {
    flex-direction: column;
    gap: 12px;
  }

  .header-controls {
    width: 100%;
  }

  .header-controls .el-input {
    flex: 1;
  }
}

/* 对话框样式 */
.alarm-detail-dialog :deep(.el-dialog) {
  border-radius: 20px;
  overflow: hidden;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.15);
  animation: dialogSlideIn 0.3s cubic-bezier(0.16, 1, 0.3, 1);
}

@keyframes dialogSlideIn {
  from {
    opacity: 0;
    transform: scale(0.9) translateY(-20px);
  }
  to {
    opacity: 1;
    transform: scale(1) translateY(0);
  }
}

.alarm-detail-dialog :deep(.el-dialog__header) {
  padding: 0;
  margin: 0;
  background: #fff;
}

.alarm-detail-dialog :deep(.el-dialog__headerbtn) {
  top: 24px;
  right: 24px;
  width: 36px;
  height: 36px;
  border-radius: 50%;
  background: rgba(0, 0, 0, 0.04);
  transition: all 0.3s ease;
}

.alarm-detail-dialog :deep(.el-dialog__headerbtn:hover) {
  background: rgba(0, 0, 0, 0.08);
  transform: rotate(90deg);
}

.alarm-detail-dialog :deep(.el-dialog__headerbtn .el-dialog__close) {
  color: #606266;
  font-size: 18px;
  font-weight: bold;
}

.alarm-detail-dialog :deep(.el-dialog__body) {
  padding: 0;
}

.dialog-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 24px 32px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.dialog-header-left {
  display: flex;
  align-items: center;
  gap: 16px;
}

.alarm-icon-wrapper {
  width: 48px;
  height: 48px;
  border-radius: 14px;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.15);
  animation: pulse 2s ease-in-out infinite;
}

@keyframes pulse {
  0%, 100% {
    transform: scale(1);
  }
  50% {
    transform: scale(1.05);
  }
}

.alarm-icon {
  font-size: 24px;
  color: #fff;
}

.dialog-title-group {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.dialog-title {
  font-size: 22px;
  font-weight: 700;
  color: #fff;
  letter-spacing: -0.5px;
  text-shadow: 0 2px 8px rgba(0, 0, 0, 0.2);
}

.dialog-subtitle {
  font-size: 13px;
  color: rgba(255, 255, 255, 0.8);
  font-weight: 400;
}

.alarm-detail-content {
  padding: 20px 32px 24px;
  background: #f8f9fa;
}

.detail-row-row {
  display: flex;
  gap: 16px;
}

.detail-col {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.detail-col.large {
  flex: 1.5;
}

.section-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 12px;
  font-weight: 600;
  color: #909399;
  text-transform: uppercase;
  letter-spacing: 1px;
  margin-bottom: 10px;
}

.section-title .el-icon {
  font-size: 14px;
  color: #409EFF;
}

.detail-card {
  background: #fff;
  border-radius: 12px;
  padding: 16px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  border: 1px solid rgba(0, 0, 0, 0.04);
  flex: 1;
  display: flex;
  align-items: center;
}

.detail-card:hover {
  box-shadow: 0 6px 24px rgba(0, 0, 0, 0.1);
  transform: translateY(-2px);
  border-color: rgba(64, 158, 255, 0.2);
}

.detail-content {
  font-size: 14px;
  color: #303133;
  line-height: 1.6;
  word-break: break-word;
}

.alarm-text {
  font-weight: 600;
  font-size: 15px;
}
</style>