<template>
  <!-- 可拖动容器 -->
  <div
    class="customer-service-wrapper"
    :style="{ right: wrapperRight + 'px', top: wrapperTop + 'px' }"
    @mousedown="startDrag"
    @touchstart="startDrag"
  >
    <!-- 客服按钮 -->
    <div
      class="customer-service-btn"
      :class="{ expanded: isExpanded, hidden: isHidden }"
      @click="handleClick"
    >
      <!-- 客服图标 -->
      <div class="service-icon">
        <svg viewBox="0 0 24 24" width="32" height="32" fill="currentColor">
          <path d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm0 3c1.66 0 3 1.34 3 3s-1.34 3-3 3-3-1.34-3-3 1.34-3 3-3zm0 14.2c-2.5 0-4.71-1.28-6-3.22.03-1.99 4-3.08 6-3.08 1.99 0 5.97 1.09 6 3.08-1.29 1.94-3.5 3.22-6 3.22z"/>
        </svg>
      </div>
      <span class="service-text">客服</span>
    </div>

    <!-- 对话框 -->
    <transition name="fade-slide">
      <div v-if="showDialog" class="service-dialog">
        <div class="dialog-header">
          <span>AI 智能客服</span>
          <button class="close-btn" @click.stop="closeDialog">
            <svg viewBox="0 0 24 24" width="16" height="16" fill="currentColor">
              <path d="M19 6.41L17.59 5 12 10.59 6.41 5 5 6.41 10.59 12 5 17.59 6.41 19 12 13.41 17.59 19 19 17.59 13.41 12z"/>
            </svg>
          </button>
        </div>

        <!-- 聊天记录区域 -->
        <div class="dialog-content" ref="chatContainerRef">
          <div v-if="chatHistory.length === 0" class="welcome-section">
            <div class="welcome-message">
              您好！我是 AI 智能客服，有什么可以帮您的吗？
            </div>
            <div class="quick-questions">
              <div class="question-title">常见问题：</div>
              <div
                v-for="(q, index) in questions"
                :key="index"
                class="question-item"
                @click="selectQuestion(q)"
              >
                {{ q }}
              </div>
            </div>
          </div>

          <!-- 聊天消息 -->
          <div v-else class="chat-messages">
            <div
              v-for="(msg, index) in chatHistory"
              :key="index"
              class="chat-message"
              :class="{ 'user-message': msg.role === 'user', 'ai-message': msg.role === 'assistant' }"
            >
              <div class="message-content" v-html="formatMessage(msg.content)"></div>
            </div>

            <!-- 加载中 -->
            <div v-if="isLoading" class="chat-message ai-message">
              <div class="message-content loading">
                <span class="loading-dot">●</span>
                <span class="loading-dot">●</span>
                <span class="loading-dot">●</span>
                思考中...
              </div>
            </div>
          </div>
        </div>

        <!-- 输入区域 -->
        <div class="dialog-footer">
          <input
            v-model="inputMessage"
            type="text"
            placeholder="请输入您的问题..."
            class="message-input"
            @keyup.enter="sendMessage"
            :disabled="isLoading"
          />
          <button class="send-btn" @click="sendMessage" :disabled="isLoading || !inputMessage.trim()">
            {{ isLoading ? '...' : '发送' }}
          </button>
        </div>
      </div>
    </transition>
  </div>
</template>

<script setup>
import { ref, onUnmounted, nextTick, watch } from 'vue'

// 拖动相关状态
const wrapperRight = ref(-60)  // 初始半隐藏（只露出60px）
const wrapperTop = ref(200)    // 距离顶部200px
const isExpanded = ref(false) // 是否完全展开
const isHidden = ref(false)   // 是否半隐藏
const showDialog = ref(false) // 是否显示对话框
const inputMessage = ref('')

// 拖动状态
const isDragging = ref(false)
const dragOffsetX = ref(0)
const dragOffsetY = ref(0)
const isLoading = ref(false)

// 聊天记录
const chatHistory = ref([])
const chatContainerRef = ref(null)

// 常见问题列表
const questions = ref([
  '如何添加新的传感器设备？',
  '如何设置报警规则？',
  '如何导出报表数据？',
  '如何查看历史数据？'
])

// 自动滚动到最新消息
watch(chatHistory, () => {
  nextTick(() => {
    if (chatContainerRef.value) {
      chatContainerRef.value.scrollTop = chatContainerRef.value.scrollHeight
    }
  })
}, { deep: true })

// 展开/收起
const handleClick = () => {
  if (!isExpanded.value) {
    wrapperRight.value = 0
    isExpanded.value = true
    isHidden.value = false
  } else if (!showDialog.value) {
    showDialog.value = true
  } else {
    showDialog.value = false
    isExpanded.value = false
    isHidden.value = true
    wrapperRight.value = -60
  }
}

// 关闭对话框
const closeDialog = () => {
  showDialog.value = false
}

// 选择问题
const selectQuestion = (q) => {
  inputMessage.value = q
}

// 格式化消息（处理换行）
const formatMessage = (content) => {
  if (!content) return ''
  return content.replace(/\n/g, '<br/>')
}

// 发送消息
const sendMessage = async () => {
  if (!inputMessage.value.trim() || isLoading.value) return

  const userMessage = inputMessage.value.trim()
  inputMessage.value = ''

  // 添加用户消息
  chatHistory.value.push({
    role: 'user',
    content: userMessage
  })

  isLoading.value = true

  try {
    // 调用 RAG 服务（带 Function Calling）
    const ragResponse = await fetch('http://localhost:8000/api/rag/chat', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        question: userMessage,
        chat_history: chatHistory.value.slice(0, -1)  // 不包含刚添加的用户消息
      })
    })

    if (!ragResponse.ok) {
      throw new Error('RAG 请求失败')
    }

    const ragData = await ragResponse.json()

    // 添加 AI 回复
    chatHistory.value.push({
      role: 'assistant',
      content: ragData.answer
    })
  } catch (error) {
    // 添加错误消息
    chatHistory.value.push({
      role: 'assistant',
      content: `抱歉，服务暂时不可用：${error.message}。请确认 RAG 服务是否运行（python main.py in rag_service folder）。`
    })
  } finally {
    isLoading.value = false
  }
}

// 开始拖动
const startDrag = (e) => {
  isDragging.value = true

  const clientX = e.type === 'mousedown' ? e.clientX : e.touches[0].clientX
  const clientY = e.type === 'mousedown' ? e.clientY : e.touches[0].clientY

  const rect = e.currentTarget.getBoundingClientRect()
  dragOffsetX.value = rect.left - clientX + (window.innerWidth - rect.right)
  dragOffsetY.value = rect.top - clientY

  document.addEventListener('mousemove', onDrag)
  document.addEventListener('mouseup', stopDrag)
  document.addEventListener('touchmove', onDrag)
  document.addEventListener('touchend', stopDrag)
}

// 拖动中
const onDrag = (e) => {
  if (!isDragging.value) return

  const clientX = e.type === 'mousemove' ? e.clientX : e.touches[0].clientX
  const clientY = e.type === 'mousemove' ? e.clientY : e.touches[0].clientY

  const newRight = window.innerWidth - clientX - dragOffsetX.value
  const newTop = clientY - dragOffsetY.value

  wrapperRight.value = Math.max(-60, Math.min(newRight, window.innerWidth - 60))
  wrapperTop.value = Math.max(0, Math.min(newTop, window.innerHeight - 60))
}

// 停止拖动
const stopDrag = () => {
  isDragging.value = false
  document.removeEventListener('mousemove', onDrag)
  document.removeEventListener('mouseup', stopDrag)
  document.removeEventListener('touchmove', onDrag)
  document.removeEventListener('touchend', stopDrag)
}

onUnmounted(() => {
  stopDrag()
})
</script>

<style scoped>
.customer-service-wrapper {
  position: fixed;
  z-index: 9999;
  display: flex;
  align-items: center;
  gap: 10px;
  cursor: grab;
  user-select: none;
}

.customer-service-wrapper:active {
  cursor: grabbing;
}

.customer-service-btn {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px 16px;
  background: linear-gradient(135deg, #409EFF 0%, #337ecc 100%);
  border-radius: 30px;
  box-shadow: 0 4px 15px rgba(64, 158, 255, 0.4);
  color: white;
  transition: all 0.3s ease;
  cursor: pointer;
  white-space: nowrap;
}

.customer-service-btn.hidden {
  transform: translateX(60px);
}

.customer-service-btn.expanded {
  background: linear-gradient(135deg, #337ecc 0%, #2868a6 100%);
}

.customer-service-btn:hover {
  transform: scale(1.05);
  box-shadow: 0 6px 20px rgba(64, 158, 255, 0.5);
}

.service-icon {
  display: flex;
  align-items: center;
  justify-content: center;
}

.service-text {
  font-size: 14px;
  font-weight: 500;
}

/* 对话框 */
.service-dialog {
  position: absolute;
  right: 70px;
  top: 0;
  width: 320px;
  height: 400px;
  background: white;
  border-radius: 12px;
  box-shadow: 0 8px 30px rgba(0, 0, 0, 0.15);
  overflow: hidden;
  display: flex;
  flex-direction: column;
}

.dialog-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 16px;
  background: linear-gradient(135deg, #409EFF 0%, #337ecc 100%);
  color: white;
  font-weight: 500;
  flex-shrink: 0;
}

.close-btn {
  background: none;
  border: none;
  color: white;
  cursor: pointer;
  padding: 4px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  transition: background 0.2s;
}

.close-btn:hover {
  background: rgba(255, 255, 255, 0.2);
}

.dialog-content {
  flex: 1;
  padding: 16px;
  overflow-y: auto;
  min-height: 0;
}

.welcome-section {
  height: 100%;
  display: flex;
  flex-direction: column;
}

.welcome-message {
  padding: 12px;
  background: #f5f7fa;
  border-radius: 8px;
  margin-bottom: 16px;
  color: #606266;
  font-size: 14px;
}

.question-title {
  font-size: 12px;
  color: #909399;
  margin-bottom: 8px;
}

.question-item {
  padding: 10px 12px;
  background: #f5f7fa;
  border-radius: 6px;
  margin-bottom: 8px;
  font-size: 13px;
  color: #409EFF;
  cursor: pointer;
  transition: all 0.2s;
}

.question-item:hover {
  background: #ecf5ff;
  color: #337ecc;
}

/* 聊天消息 */
.chat-messages {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.chat-message {
  display: flex;
  flex-direction: column;
  max-width: 85%;
}

.user-message {
  align-self: flex-end;
}

.ai-message {
  align-self: flex-start;
}

.message-content {
  padding: 10px 14px;
  border-radius: 12px;
  font-size: 13px;
  line-height: 1.5;
  word-break: break-word;
}

.user-message .message-content {
  background: #409EFF;
  color: white;
  border-bottom-right-radius: 4px;
}

.ai-message .message-content {
  background: #f5f7fa;
  color: #303133;
  border-bottom-left-radius: 4px;
}

.message-content.loading {
  color: #909399;
  font-style: italic;
}

.loading-dot {
  animation: loadingPulse 1s infinite;
  margin: 0 2px;
}

.loading-dot:nth-child(2) {
  animation-delay: 0.2s;
}

.loading-dot:nth-child(3) {
  animation-delay: 0.4s;
}

@keyframes loadingPulse {
  0%, 60%, 100% { opacity: 0.3; }
  30% { opacity: 1; }
}

.dialog-footer {
  display: flex;
  gap: 8px;
  padding: 12px 16px;
  border-top: 1px solid #eee;
  flex-shrink: 0;
}

.message-input {
  flex: 1;
  padding: 8px 12px;
  border: 1px solid #dcdfe6;
  border-radius: 6px;
  font-size: 13px;
  outline: none;
  transition: border-color 0.2s;
}

.message-input:focus {
  border-color: #409EFF;
}

.message-input:disabled {
  background: #f5f7fa;
  cursor: not-allowed;
}

.send-btn {
  padding: 8px 16px;
  background: #409EFF;
  color: white;
  border: none;
  border-radius: 6px;
  font-size: 13px;
  cursor: pointer;
  transition: background 0.2s;
}

.send-btn:hover:not(:disabled) {
  background: #337ecc;
}

.send-btn:disabled {
  background: #a0cfff;
  cursor: not-allowed;
}

/* 动画 */
.fade-slide-enter-active,
.fade-slide-leave-active {
  transition: all 0.3s ease;
}

.fade-slide-enter-from,
.fade-slide-leave-to {
  opacity: 0;
  transform: translateX(20px);
}
</style>