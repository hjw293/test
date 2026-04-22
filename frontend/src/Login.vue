<template>
  <div class="login-container">
    <!-- 品牌展示区域 -->
    <div class="brand-area">
      <img src="@/resource/logo.png" alt="Logo" class="brand-logo" />
      <span class="brand-title">立信染整智能监控平台</span>
    </div>

    <div class="login-card">
      <div class="login-header">
        <h1>用户登录</h1>
      </div>

      <el-form :model="loginForm" :rules="rules" ref="loginFormRef" class="login-form">
        <el-form-item prop="username">
         
          <el-input
            v-model="loginForm.username"
            placeholder="请输入用户名"
            size="large"
          >
            <template #prefix>
              <el-icon class="input-icon"><UserFilled /></el-icon>
              <span class="input-prefix-text">用户名</span>
            </template>
          </el-input>
        </el-form-item>

        <el-form-item prop="password">
         
          <el-input
            v-model="loginForm.password"
            type="password"
            placeholder="请输入密码"
            size="large"
            show-password
            @keyup.enter="handleLogin"
          >
            <template #prefix>
              <el-icon class="input-icon"><Lock /></el-icon>
              <span class="input-prefix-text">密码</span>
            </template>
          </el-input>
        </el-form-item>

        <el-form-item>
          <el-button
            type="primary"
            size="large"
            :loading="loading"
            @click="handleLogin"
            class="login-button"
          >
            {{ loading ? '登录中...' : '登录' }}
          </el-button>
        </el-form-item>
      </el-form>

      
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { UserFilled, Lock } from '@element-plus/icons-vue'
import axios from 'axios'

const router = useRouter()
const loginFormRef = ref(null)
const loading = ref(false)

const loginForm = reactive({
  username: '',
  password: ''
})

const rules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '用户名长度在 3 到 20 个字符', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度在 6 到 20 个字符', trigger: 'blur' }
  ]
}

const handleLogin = async () => {
  if (!loginFormRef.value) return

  try {
    await loginFormRef.value.validate()

    loading.value = true

    const response = await axios.post('http://localhost:8080/api/auth/login', {
      username: loginForm.username,
      password: loginForm.password
    })

    if (response.data.code === 200) {
      // 保存token和用户信息
      localStorage.setItem('token', response.data.data.token)
      localStorage.setItem('user', JSON.stringify(response.data.data.user))

      ElMessage.success('登录成功')

      // 跳转到首页
      router.push('/')
    } else {
      ElMessage.error(response.data.message || '登录失败')
    }
  } catch (error) {
    console.error('登录错误:', error)
    if (error.response && error.response.data) {
      ElMessage.error(error.response.data.message || '登录失败')
    } else {
      ElMessage.error('登录失败，请检查网络连接')
    }
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-container {
  min-height: 100vh;
  display: flex;
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

.login-card {
  width: 100%;
  max-width: 600px;
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  border-radius: 20px;
  padding: 30px 50px;
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.1);
  border: 1px solid rgba(255, 255, 255, 0.2);
}

.login-header {
  text-align: center;
  margin-bottom: 30px;
}

.login-header h1 {
  font-size: 36px;
  font-weight: 700;
  color: #333;
  margin: 0;
  letter-spacing: -0.5px;
}

.login-form {
  margin-top: 30px;
}

:deep(.el-form-item) {
  margin-bottom: 28px;
}

:deep(.el-input__wrapper) {
  border-radius: 10px;
  padding: 16px 20px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
  transition: all 0.3s ease;
}

:deep(.el-input__wrapper .el-input__inner) {
  font-size: 24px;
  height: 32px;
}

:deep(.el-input__wrapper:hover) {
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.15);
}

:deep(.el-input__wrapper.is-focus) {
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.2);
}

:deep(.el-input__inner) {
  font-size: 24px;
}

:deep(.el-input__inner::placeholder) {
  font-size: 24px;
}

.input-label {
  font-size: 24px;
  font-weight: bold;
  color: #333;
}

:deep(.el-input__prefix) {
  font-size: 24px;
  display: flex;
  align-items: center;
}

.input-icon {
  font-size: 24px;
  color: #666;
}

.input-prefix-text {
  font-size: 24px;
  margin-left: 8px;
  color: #666;
}

.login-button {
  width: 100%;
  height: 60px;
  font-size: 24px;
  font-weight: 600;
  border-radius: 10px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border: none;
  transition: all 0.3s ease;
  box-shadow: 0 4px 15px rgba(102, 126, 234, 0.3);
}

.login-button:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(102, 126, 234, 0.4);
}

.login-button:active {
  transform: translateY(0);
  box-shadow: 0 2px 10px rgba(102, 126, 234, 0.3);
}

.login-footer {
  margin-top: 30px;
  text-align: center;
}

.tips {
  padding: 15px;
  background: #f5f7fa;
  border-radius: 8px;
  border-left: 4px solid #667eea;
}

.tips p {
  margin: 5px 0;
  font-size: 20px;
  color: #666;
  line-height: 1.6;
}

:deep(.el-form-item__error) {
  font-size: 18px;
  padding-top: 4px;
}

:deep(.el-form-item__label) {
  font-size: 20px;
}
</style>