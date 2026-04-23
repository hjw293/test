<template>
  <div class="navigation-container">
    <!-- 品牌展示区域 -->
    <div class="brand-area">
      <img src="@/resource/logo.png" alt="Logo" class="brand-logo" />
      <span class="brand-title">立信染整智能监控平台</span>
    </div>

    <!-- 用户信息区域 -->
    <div class="user-area">
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

    

    <!-- 底部导航按钮 -->
    <div class="bottom-nav">
      <div class="nav-button dashboard" @click="goTo('dashboard')">
        <el-icon class="nav-icon"><HomeFilled /></el-icon>
        <span class="nav-text-en">Dashboard</span>
        <span class="nav-text-cn">传感器数据监控平台</span>
      </div>

      <div class="nav-button alarm" @click="goTo('alarm-config')">
        <el-icon class="nav-icon"><Bell /></el-icon>
        <span class="nav-text-en">AlarmConfig</span>
        <span class="nav-text-cn">设备警报管理平台</span>
      </div>

      <div class="nav-button batch" @click="goTo('batch-report-config')">
        <el-icon class="nav-icon"><Document /></el-icon>
        <span class="nav-text-en">BatchReportConfig</span>
        <span class="nav-text-cn">报表配置管理平台</span>
      </div>

      <div class="nav-button curve" @click="goTo('curve-group')">
        <el-icon class="nav-icon"><TrendCharts /></el-icon>
        <span class="nav-text-en">CurveGroup</span>
        <span class="nav-text-cn">设备曲线组展示</span>
      </div>
    </div>

    <!-- 客服组件 -->
    <CustomerService />
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { HomeFilled, Bell, Document, TrendCharts, User, ArrowDown } from '@element-plus/icons-vue'
import CustomerService from '@/components/CustomerService.vue'

const router = useRouter()

// 当前用户信息
const currentUser = computed(() => {
  const userStr = localStorage.getItem('user')
  return userStr ? JSON.parse(userStr) : null
})

const goTo = (name) => {
  const routes = {
    'dashboard': '/dashboard',
    'alarm-config': '/alarm-config',
    'batch-report-config': '/batch-report-config',
    'curve-group': '/curve-group'
  }
  router.push(routes[name])
}

// 退出登录
const handleLogout = () => {
  localStorage.removeItem('token')
  localStorage.removeItem('user')
  window.location.href = '/login'
}
</script>

<style scoped>
.navigation-container {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  padding: 20px;
  background-image: url('@/resource/立信界面.jpeg');
  background-size: 100% auto;
  background-position: center center;
  background-repeat: no-repeat;
  position: relative;
}

/* 品牌展示区域 */
.brand-area {
  position: absolute;
  top: 20px;
  left: 20px;
  display: flex;
  align-items: center;
  z-index: 10;
}

.brand-logo {
  height: 100px;
  vertical-align: middle;
}

.brand-title {
  color: #fff;
  font-size: 44px;
  font-weight: bold;
  margin-left: 20px;
  vertical-align: middle;
  text-shadow: 0 1px 3px rgba(0, 0, 0, 0.3);
}

/* 用户信息区域 */
.user-area {
  position: absolute;
  top: 20px;
  right: 20px;
  z-index: 10;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 12px;
  color: #fff;
  cursor: pointer;
  padding: 12px 24px;
  border-radius: 16px;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  background: rgba(255, 255, 255, 0.15);
  backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.1);
  font-weight: 500;
  font-size: 28px;
}

.user-info:hover {
  background: rgba(255, 255, 255, 0.25);
  transform: scale(1.02);
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.15);
}

.user-info .el-icon {
  font-size: 28px;
}

.nav-content {
  text-align: center;
  margin-bottom: 100px;
}

.welcome-text {
  font-size: 48px;
  font-weight: bold;
  color: #fff;
  text-shadow: 0 2px 4px rgba(0, 0, 0, 0.3);
  margin: 0 0 10px 0;
}

.sub-welcome {
  font-size: 24px;
  color: rgba(255, 255, 255, 0.9);
  text-shadow: 0 1px 2px rgba(0, 0, 0, 0.2);
  margin: 0;
}

/* 底部导航按钮 */
.bottom-nav {
  position: fixed;
  bottom: 0;
  left: 50%;
  transform: translateX(-50%);
  display: flex;
  gap: 0;
  z-index: 10;
}

.nav-button {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  width: 380px;
  height: 220px;
  border-radius: 0;
  cursor: pointer;
  transition: all 0.3s ease;
  box-shadow: 0 6px 20px rgba(0, 0, 0, 0.25);
}

.nav-button:hover {
  transform: translateY(-8px);
  box-shadow: 0 12px 35px rgba(0, 0, 0, 0.35);
}

.nav-icon {
  font-size: 70px;
  margin-bottom: 20px;
}

.nav-text-en {
  font-size: 26px;
  font-weight: bold;
  opacity: 0.7;
  margin-bottom: 8px;
}

.nav-text-cn {
  font-size: 20px;
  font-weight: 600;
}

.nav-button {
  color: #fff;
}

/* Dashboard - 绿色 */
.dashboard {
  background: linear-gradient(135deg, #67C23A 0%, #5aaf2e 100%);
}

.dashboard:hover {
  background: linear-gradient(135deg, #5aaf2e 0%, #4d9a28 100%);
}

/* AlarmConfig - 橙黄色 */
.alarm {
  background: linear-gradient(135deg, #E6A23C 0%, #d4940c 100%);
}

.alarm:hover {
  background: linear-gradient(135deg, #d4940c 0%, #c0850a 100%);
}

/* BatchReportConfig - 蓝色 */
.batch {
  background: linear-gradient(135deg, #409EFF 0%, #3084e6 100%);
}

.batch:hover {
  background: linear-gradient(135deg, #3084e6 0%, #2878cf 100%);
}

/* CurveGroup - 紫色 */
.curve {
  background: linear-gradient(135deg, #8e44ad 0%, #7d3c98 100%);
}

.curve:hover {
  background: linear-gradient(135deg, #7d3c98 0%, #6c3587 100%);
}
</style>
