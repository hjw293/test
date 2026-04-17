<template>
  <div class="container">
    <el-header class="header">
      <h1>批量报表配置管理系统</h1>
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
            <div class="header-title">批量报表配置列表</div>
            <div class="header-controls">
              <el-select
                v-model="filterReportCategory"
                placeholder="报告类型"
                style="width: 150px; margin-right: 10px;"
                clearable
                @change="handleSearch"
              >
                <el-option label="能耗报告" :value="0" />
                <el-option label="健康指数报告" :value="1" />
              </el-select>
              <el-select
                v-model="filterReportType"
                placeholder="项目类型"
                style="width: 150px; margin-right: 10px;"
                clearable
                @change="handleSearch"
              >
                <el-option label="项目" :value="0" />
                <el-option label="项目标题" :value="1" />
                <el-option label="标题" :value="2" />
              </el-select>
              <el-input
                v-model="searchNameId"
                placeholder="搜索名称ID"
                style="width: 150px; margin-right: 10px;"
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

        <div v-loading="loading" class="report-content">
          <el-alert
            v-if="errorMessage"
            :title="errorMessage"
            type="error"
            :closable="true"
            style="margin-bottom: 16px;"
          />

          <div v-if="reportList.length > 0" class="report-grid">
            <el-card
              v-for="report in reportList"
              :key="report.id"
              class="report-card"
              shadow="hover"
              @click="showReportDetail(report)"
            >
              <div class="report-card-header">
                <div class="report-images-container">
                  <!-- 功能图示 (fi_xxxx.png) -->
                  <div class="report-image-wrapper">
                    <img
                      v-if="report.nameId && report.nameId > 0"
                      :src="getFunctionImageUrl(report.nameId)"
                      :alt="report.nameText"
                      class="report-image"
                      @error="handleFunctionImageError"
                    />
                    <div v-else class="report-image-placeholder">
                      <span class="placeholder-text">功能</span>
                    </div>
                  </div>
                  <!-- 报告图示 (ri_xxxx.png) - 只在有image_id时显示 -->
                  <div class="report-image-wrapper">
                    <img
                      v-if="report.imageId && report.imageId > 0"
                      :src="getReportImageUrl(report.imageId)"
                      :alt="report.nameText"
                      class="report-image"
                      @error="handleReportImageError"
                    />
                    <div v-else class="report-image-placeholder">
                      <span class="placeholder-text">报告</span>
                    </div>
                  </div>
                </div>
                <div class="report-category-badge" :class="getCategoryClass(report.reportCategory)">
                  {{ getCategoryText(report.reportCategory) }}
                </div>
              </div>
              
              <div class="report-card-body">
                <div class="report-name">{{ report.nameText || `Name ID: ${report.nameId}` }}</div>
                
                <div class="report-info-row">
                  <span class="info-label">类型:</span>
                  <el-tag :type="getTypeTagType(report.reportType)" size="small">
                    {{ getTypeText(report.reportType) }}
                  </el-tag>
                </div>
                
                <div class="report-info-row">
                  <span class="info-label">启用:</span>
                  <span class="info-value">
                    <el-tag :type="report.enableNameId === 0 ? 'success' : 'info'" size="small">
                      {{ report.enableNameId === 0 ? '永久启动' : '条件启动' }}
                    </el-tag>
                  </span>
                </div>

                <div v-if="report.enableInvalidMarker === 1" class="report-info-row">
                  <span class="info-label">无效标识:</span>
                  <span class="info-value warning-text">已启用 ({{ report.invalidValue }})</span>
                </div>
              </div>
            </el-card>
          </div>

          <!-- 放大详情对话框 -->
          <el-dialog
            v-model="dialogVisible"
            width="800px"
            class="report-detail-dialog"
            :close-on-click-modal="true"
            destroy-on-close
          >
            <template #header>
              <div class="dialog-header">
                <div class="dialog-header-left">
                  <div class="dialog-images-group">
                    <!-- 功能图示 -->
                    <div
                      class="dialog-icon-wrapper"
                      :style="{ background: getDialogGradient(selectedReport?.reportCategory) }"
                    >
                      <img
                        v-if="selectedReport?.nameId && selectedReport.nameId > 0"
                        :src="getFunctionImageUrl(selectedReport.nameId)"
                        :alt="selectedReport.nameText"
                        class="dialog-image"
                        @error="handleFunctionImageError"
                      />
                      <div v-else class="dialog-image-placeholder">
                        <span class="placeholder-text">功能</span>
                      </div>
                    </div>
                    <!-- 报告图示 -->
                    <div
                      class="dialog-icon-wrapper"
                      :style="{ background: getDialogGradient(selectedReport?.reportCategory) }"
                    >
                      <img
                        v-if="selectedReport?.imageId && selectedReport.imageId > 0"
                        :src="getReportImageUrl(selectedReport.imageId)"
                        :alt="selectedReport.nameText"
                        class="dialog-image"
                        @error="handleReportImageError"
                      />
                      <div v-else class="dialog-image-placeholder">
                        <span class="placeholder-text">报告</span>
                      </div>
                    </div>
                  </div>
                  <div class="dialog-title-group">
                    <div class="dialog-title">{{ selectedReport?.nameText || `Name ID: ${selectedReport?.nameId}` }}</div>
                    <div class="dialog-subtitle">批量报表配置详情</div>
                  </div>
                </div>
              </div>
            </template>
            
            <div class="report-detail-content">
              <div class="detail-row-row">
                <div class="detail-col large">
                  <div class="section-title">
                    <el-icon><InfoFilled /></el-icon>
                    基本信息
                  </div>
                  <div class="detail-card">
                    <div class="detail-info-grid">
                      <div class="detail-info-item">
                        <span class="detail-label">报告类型:</span>
                        <el-tag :type="getCategoryTagType(selectedReport?.reportCategory)">
                          {{ getCategoryText(selectedReport?.reportCategory) }}
                        </el-tag>
                      </div>
                      <div class="detail-info-item">
                        <span class="detail-label">项目类型:</span>
                        <el-tag :type="getTypeTagType(selectedReport?.reportType)">
                          {{ getTypeText(selectedReport?.reportType) }}
                        </el-tag>
                      </div>
                      <div class="detail-info-item">
                        <span class="detail-label">名称ID:</span>
                        <span class="detail-value">{{ selectedReport?.nameId }}</span>
                      </div>
                      <div class="detail-info-item">
                        <span class="detail-label">图示ID:</span>
                        <span class="detail-value">{{ selectedReport?.imageId || '无' }}</span>
                      </div>
                    </div>
                  </div>
                </div>
                
                <div class="detail-col">
                  <div class="section-title">
                    <el-icon><Setting /></el-icon>
                    启动配置
                  </div>
                  <div class="detail-card">
                    <div class="detail-info-grid">
                      <div class="detail-info-item">
                        <span class="detail-label">启用模式:</span>
                        <el-tag :type="selectedReport?.enableNameId === 0 ? 'success' : 'warning'">
                          {{ selectedReport?.enableNameId === 0 ? '永久启动' : '条件启动' }}
                        </el-tag>
                      </div>
                      <div v-if="selectedReport?.enableNameId !== 0" class="detail-info-item">
                        <span class="detail-label">启用Name ID:</span>
                        <span class="detail-value">{{ selectedReport?.enableNameId }}</span>
                      </div>
                      <div v-if="selectedReport?.refNameId" class="detail-info-item">
                        <span class="detail-label">参考Name ID:</span>
                        <span class="detail-value">{{ selectedReport?.refNameId }}</span>
                      </div>
                    </div>
                  </div>
                </div>
                
                <div class="detail-col">
                  <div class="section-title">
                    <el-icon><Document /></el-icon>
                    其他配置
                  </div>
                  <div class="detail-card">
                    <div class="detail-info-grid">
                      <div class="detail-info-item">
                        <span class="detail-label">无效标识:</span>
                        <el-tag :type="selectedReport?.enableInvalidMarker === 1 ? 'danger' : 'info'">
                          {{ selectedReport?.enableInvalidMarker === 1 ? '已启用' : '未启用' }}
                        </el-tag>
                      </div>
                      <div v-if="selectedReport?.enableInvalidMarker === 1" class="detail-info-item">
                        <span class="detail-label">无效值:</span>
                        <span class="detail-value">{{ selectedReport?.invalidValue }}</span>
                      </div>
                      <div v-if="selectedReport?.stepParamId" class="detail-info-item">
                        <span class="detail-label">步骤参数ID:</span>
                        <span class="detail-value">{{ selectedReport?.stepParamId }}</span>
                      </div>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </el-dialog>

          <el-empty v-if="reportList.length === 0 && !loading" description="暂无批量报表配置数据" />
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
import { ArrowLeft, User, ArrowDown, Search, Document, InfoFilled, Setting } from '@element-plus/icons-vue'

const router = useRouter()

// 当前用户信息
const currentUser = computed(() => {
  const userStr = localStorage.getItem('user')
  return userStr ? JSON.parse(userStr) : null
})

const API_URL = 'http://localhost:8080/api/batch-report/page'

// 数据状态
const reportList = ref([])
const loading = ref(false)
const errorMessage = ref('')
const currentPage = ref(1)
const pageSize = ref(12)
const total = ref(0)

// 筛选条件
const filterReportCategory = ref(null)
const filterReportType = ref(null)
const searchNameId = ref('')

// 对话框状态
const dialogVisible = ref(false)
const selectedReport = ref(null)

// 获取批量报表配置数据
const fetchReportData = async () => {
  loading.value = true
  errorMessage.value = ''
  
  try {
    const token = localStorage.getItem('token')
    
    const params = {
      pageNum: currentPage.value,
      pageSize: pageSize.value
    }
    
    if (filterReportCategory.value !== null && filterReportCategory.value !== '') {
      params.reportCategory = filterReportCategory.value
    }
    
    if (filterReportType.value !== null && filterReportType.value !== '') {
      params.reportType = filterReportType.value
    }
    
    if (searchNameId.value && searchNameId.value.trim()) {
      params.nameId = searchNameId.value.trim()
    }
    
    const response = await axios.get(API_URL, {
      params,
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${token}`
      }
    })

    if (response.data.code === 200) {
      reportList.value = response.data.data || []
      total.value = response.data.pagination?.total || 0
    } else {
      errorMessage.value = response.data.message || '获取数据失败'
      reportList.value = []
      total.value = 0
    }
  } catch (error) {
    console.error('Error fetching report data:', error)
    if (error.response && error.response.status === 401) {
      errorMessage.value = '登录已过期，请重新登录'
      localStorage.removeItem('token')
      localStorage.removeItem('user')
      router.push('/login')
    } else {
      errorMessage.value = `请求失败: ${error.message || '无法连接到后端服务器'}`
    }
    reportList.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}

// 搜索
const handleSearch = () => {
  currentPage.value = 1
  fetchReportData()
}

// 显示报表详情（放大效果）
const showReportDetail = (report) => {
  selectedReport.value = report
  dialogVisible.value = true
}

// 获取功能图示URL (fi_xxxx.png)
const getFunctionImageUrl = (nameId) => {
  if (!nameId || nameId === 0) return null
  return `/figure/fi_${nameId}.png`
}

// 获取报告图示URL (ri_xxxx.png)
const getReportImageUrl = (imageId) => {
  if (!imageId || imageId === 0) return null
  return `/figure/ri_${imageId}.png`
}

// 功能图示加载错误处理
const handleFunctionImageError = (event) => {
  event.target.style.display = 'none'
  // 显示功能占位符
  const wrapper = event.target.closest('.report-image-wrapper, .report-icon-wrapper')
  if (wrapper) {
    const img = wrapper.querySelector('img')
    if (img) img.style.display = 'none'
  }
}

// 报告图示加载错误处理
const handleReportImageError = (event) => {
  event.target.style.display = 'none'
  // 显示报告占位符
  const wrapper = event.target.closest('.report-image-wrapper, .report-icon-wrapper')
  if (wrapper) {
    const img = wrapper.querySelector('img')
    if (img) img.style.display = 'none'
  }
}

// 获取报告类型文本
const getCategoryText = (category) => {
  const categoryMap = {
    0: '能耗报告',
    1: '健康指数报告'
  }
  return categoryMap[category] || '未知'
}

// 获取项目类型文本
const getTypeText = (type) => {
  const typeMap = {
    0: '项目',
    1: '项目标题',
    2: '标题'
  }
  return typeMap[type] || '未知'
}

// 获取报告类型标签类型
const getCategoryTagType = (category) => {
  const tagTypeMap = {
    0: 'warning',
    1: 'success'
  }
  return tagTypeMap[category] || 'info'
}

// 获取项目类型标签类型
const getTypeTagType = (type) => {
  const tagTypeMap = {
    0: 'primary',
    1: 'success',
    2: 'info'
  }
  return tagTypeMap[type] || 'info'
}

// 获取分类样式类
const getCategoryClass = (category) => {
  const classMap = {
    0: 'category-energy',
    1: 'category-health'
  }
  return classMap[category] || 'category-default'
}

// 获取对话框渐变色
const getDialogGradient = (category) => {
  const gradientMap = {
    0: 'linear-gradient(135deg, #FF9800 0%, #FF5722 100%)',
    1: 'linear-gradient(135deg, #4CAF50 0%, #2E7D32 100%)'
  }
  return gradientMap[category] || 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)'
}

// 处理每页显示条数变化
const handleSizeChange = (size) => {
  pageSize.value = size
  currentPage.value = 1
  fetchReportData()
}

// 处理页码变化
const handleCurrentChange = (current) => {
  currentPage.value = current
  fetchReportData()
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
  fetchReportData()
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

.report-content {
  min-height: 400px;
}

.report-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 24px;
  margin-bottom: 20px;
}

.report-card {
  border-radius: 12px;
  border: none;
  transition: all 0.3s ease;
  cursor: pointer;
  overflow: hidden;
}

.report-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
}

.report-card:active {
  transform: scale(1.05);
  box-shadow: 0 12px 32px rgba(0, 0, 0, 0.15);
}

.report-card :deep(.el-card__body) {
  padding: 0;
}

.report-card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px;
  background: linear-gradient(135deg, #f5f7fa 0%, #e8eef5 100%);
  border-bottom: 2px solid #e0e0e0;
}

.report-images-container {
  display: flex;
  gap: 10px;
  align-items: center;
}

.report-image-wrapper {
  width: 64px;
  height: 64px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  border-radius: 12px;
  background: #fff;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  position: relative;
  flex-shrink: 0;
}

.report-image {
  width: 100%;
  height: 100%;
  object-fit: contain;
  border-radius: 12px;
}

.report-image-placeholder {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 12px;
  font-size: 14px;
  font-weight: 600;
  color: #fff;
  text-shadow: 0 1px 2px rgba(0, 0, 0, 0.2);
}

.placeholder-text {
  font-size: 14px;
  font-weight: 600;
  color: #fff;
  text-shadow: 0 1px 2px rgba(0, 0, 0, 0.2);
}

.report-category-badge {
  padding: 4px 12px;
  border-radius: 20px;
  font-size: 12px;
  font-weight: 600;
  color: #fff;
  text-shadow: 0 1px 2px rgba(0, 0, 0, 0.2);
}

.category-energy {
  background: linear-gradient(135deg, #FF9800 0%, #FF5722 100%);
}

.category-health {
  background: linear-gradient(135deg, #4CAF50 0%, #2E7D32 100%);
}

.category-default {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.report-card-body {
  padding: 20px;
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.report-name {
  font-size: 17px;
  font-weight: 600;
  color: #333;
  line-height: 1.4;
  min-height: 48px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  text-overflow: ellipsis;
}

.report-info-row {
  display: flex;
  align-items: center;
  gap: 8px;
}

.info-label {
  font-size: 14px;
  color: #909399;
  font-weight: 500;
  min-width: 60px;
  flex-shrink: 0;
}

.info-value {
  font-size: 14px;
  color: #606266;
}

.warning-text {
  color: #F56C6C;
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
  .report-grid {
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

  .report-grid {
    grid-template-columns: 1fr;
  }

  .card-header {
    flex-direction: column;
    gap: 12px;
  }

  .header-controls {
    width: 100%;
    flex-wrap: wrap;
  }

  .header-controls .el-select,
  .header-controls .el-input {
    flex: 1;
    min-width: 120px;
  }
}

/* 对话框样式 */
.report-detail-dialog :deep(.el-dialog) {
  border-radius: 20px;
  overflow: hidden;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.15);
  animation: dialogSlideIn 0.3s cubic-bezier(0.16, 1, 0.3, 1);
  max-width: 1000px;
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

.report-detail-dialog :deep(.el-dialog__header) {
  padding: 0;
  margin: 0;
  background: #fff;
}

.report-detail-dialog :deep(.el-dialog__headerbtn) {
  top: 32px;
  right: 32px;
  width: 44px;
  height: 44px;
  border-radius: 50%;
  background: rgba(0, 0, 0, 0.04);
  transition: all 0.3s ease;
}

.report-detail-dialog :deep(.el-dialog__headerbtn:hover) {
  background: rgba(0, 0, 0, 0.08);
  transform: rotate(90deg);
}

.report-detail-dialog :deep(.el-dialog__headerbtn .el-dialog__close) {
  color: #606266;
  font-size: 22px;
  font-weight: bold;
}

.report-detail-dialog :deep(.el-dialog__body) {
  padding: 0;
}

.dialog-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 40px 48px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.dialog-header-left {
  display: flex;
  align-items: center;
  gap: 24px;
}

.dialog-images-group {
  display: flex;
  gap: 18px;
  align-items: center;
}

.report-icon-wrapper {
  width: 80px;
  height: 80px;
  border-radius: 18px;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.15);
  animation: pulse 2s ease-in-out infinite;
  overflow: hidden;
  position: relative;
  flex-shrink: 0;
}

@keyframes pulse {
  0%, 100% {
    transform: scale(1);
  }
  50% {
    transform: scale(1.05);
  }
}

.dialog-image {
  width: 100%;
  height: 100%;
  object-fit: contain;
}

.dialog-image-placeholder {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 16px;
  font-weight: 600;
  color: #fff;
  text-shadow: 0 1px 2px rgba(0, 0, 0, 0.3);
}

.dialog-icon {
  font-size: 36px;
  color: #fff;
}

.dialog-title-group {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.dialog-title {
  font-size: 30px;
  font-weight: 700;
  color: #fff;
  letter-spacing: -0.5px;
  text-shadow: 0 2px 8px rgba(0, 0, 0, 0.2);
  line-height: 1.2;
}

.dialog-subtitle {
  font-size: 16px;
  color: rgba(255, 255, 255, 0.85);
  font-weight: 400;
}

.report-detail-content {
  padding: 36px 48px 40px;
  background: #f8f9fa;
}

.detail-row-row {
  display: flex;
  gap: 20px;
  flex-wrap: wrap;
}

.detail-col {
  flex: 1;
  min-width: 280px;
  display: flex;
  flex-direction: column;
}

.detail-col.large {
  flex: 1;
  min-width: 280px;
}

.section-title {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 14px;
  font-weight: 600;
  color: #909399;
  text-transform: uppercase;
  letter-spacing: 1px;
  margin-bottom: 16px;
}

.section-title .el-icon {
  font-size: 16px;
  color: #409EFF;
}

.detail-card {
  background: #fff;
  border-radius: 16px;
  padding: 24px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  border: 1px solid rgba(0, 0, 0, 0.04);
  flex: 1;
}

.detail-card:hover {
  box-shadow: 0 6px 24px rgba(0, 0, 0, 0.1);
  transform: translateY(-2px);
  border-color: rgba(64, 158, 255, 0.2);
}

.detail-info-grid {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.detail-info-item {
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 16px;
}

.detail-label {
  color: #909399;
  font-weight: 500;
  min-width: 120px;
  flex-shrink: 0;
}

.detail-value {
  color: #303133;
  font-weight: 500;
}
</style>