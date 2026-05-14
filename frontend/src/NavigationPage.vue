<template>
  <div class="navigation-container">
    <!-- 顶部黑色阴影 -->
    <div class="top-shadow"></div>

    <!-- 品牌展示区域 -->
    <div class="brand-area">
      <img src="@/resource/logo.png" alt="Logo" class="brand-logo" />
      <span class="brand-title">立信染整智能监控平台</span>
    </div>

    <!-- 用户信息区域 -->
    <div class="user-area">
      <div class="user-info">
        <el-icon class="user-icon"><Avatar /></el-icon>
        <span class="user-text">{{ currentUser?.username || '用户' }}</span>
      </div>
      <div class="logout-link" @click="handleLogout">注销</div>
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
import { HomeFilled, Bell, Document, TrendCharts, Avatar } from '@element-plus/icons-vue'
import CustomerService from '@/components/CustomerService.vue'

// 当前用户信息
const currentUser = computed(() => {
  const userStr = localStorage.getItem('user')
  return userStr ? JSON.parse(userStr) : null
})

// 跳转到对应平台
const goTo = (name) => {
  const routes = {
    'dashboard': '/dashboard',
    'alarm-config': '/alarm-config',
    'batch-report-config': '/batch-report-config',
    'curve-group': '/curve-group'
  }
  window.location.href = routes[name]
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

/* 顶部黑色阴影 */
.top-shadow {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  height: 140px;
  background: linear-gradient(to bottom, rgba(0, 0, 0, 0.5),transparent);
  z-index: 9;
  pointer-events: none;
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
  display: flex;
  flex-direction: column;
  align-items: center;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #fff;
  padding: 10px 20px;
  border-radius: 25px;
  background: rgba(0, 0, 0, 0.4);
  backdrop-filter: blur(10px);
}

.user-icon {
  font-size: 24px;
}

.user-text {
  font-size: 18px;
  font-weight: 500;
}

.logout-link {
  text-align: right;
  font-size: 19px;
  color: #ff4d4d;
  cursor: pointer;
  margin-top: 4px;
  width: 100%;
}

.logout-link:hover {
  color: #ff1a1a;
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
  background: linear-gradient(135deg, #6ba83a 0%, #5a9430 100%);
}

.dashboard:hover {
  background: linear-gradient(135deg, #5a9430 0%, #4d8529 100%);
}

/* AlarmConfig - 橙黄色 */
.alarm {
  background: linear-gradient(135deg, #dba81a 0%, #cf9a14 100%);
}

.alarm:hover {
  background: linear-gradient(135deg, #cf9a14 0%, #b8890d 100%);
}

/* BatchReportConfig - 蓝色 */
.batch {
  background: linear-gradient(135deg, #4a8ac5 0%, #3a7ab5 100%);
}

.batch:hover {
  background: linear-gradient(135deg, #3a7ab5 0%, #2d6599 100%);
}

/* CurveGroup - 紫色 */
.curve {
  background: linear-gradient(135deg, #8e5aad 0%, #7d4a9e 100%);
}

.curve:hover {
  background: linear-gradient(135deg, #7d4a9e 0%, #6d3d8d 100%);
}
</style>
