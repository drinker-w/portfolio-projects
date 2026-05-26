<template>
  <div class="chat-container">
    <el-card shadow="never" class="chat-card">
      <div class="chat-layout">
        <!-- 左侧文档列表 -->
        <div class="doc-sidebar">
          <div class="doc-sidebar-header">选择文档</div>
          <div class="doc-list">
            <div
              v-for="doc in documentList"
              :key="doc.id"
              class="doc-item"
              :class="{ active: selectedDocId === doc.id }"
              @click="selectedDocId = doc.id"
            >
              <el-icon><Document /></el-icon>
              <span class="doc-name">{{ doc.fileName }}</span>
            </div>
            <el-empty v-if="documentList.length === 0" description="暂无文档" :image-size="60" />
          </div>
        </div>

        <!-- 右侧对话区 -->
        <div class="chat-main">
          <div class="chat-messages" ref="messageContainer">
            <div class="chat-welcome" v-if="messages.length === 0">
              <el-icon :size="48" color="#c0c4cc"><ChatDotRound /></el-icon>
              <p>选择文档，开始智能问答</p>
            </div>

            <div v-for="(msg, index) in messages" :key="index" class="message-wrapper">
              <!-- 用户消息 -->
              <div v-if="msg.role === 'user'" class="message user-message">
                <div class="message-content">{{ msg.content }}</div>
                <div class="message-avatar">
                  <el-avatar :size="36" style="background-color: #409eff;">
                    <el-icon><User /></el-icon>
                  </el-avatar>
                </div>
              </div>
              <!-- AI消息 -->
              <div v-else class="message ai-message">
                <div class="message-avatar">
                  <el-avatar :size="36" style="background-color: #67c23a;">
                    <el-icon><Monitor /></el-icon>
                  </el-avatar>
                </div>
                <div class="message-content" v-html="formatMessage(msg.content)"></div>
              </div>
            </div>

            <!-- 加载中 -->
            <div v-if="loading" class="message ai-message">
              <div class="message-avatar">
                <el-avatar :size="36" style="background-color: #67c23a;">
                  <el-icon><Monitor /></el-icon>
                </el-avatar>
              </div>
              <div class="message-content loading-content">
                <span class="typing-cursor">思考中</span>
                <span class="typing-dots">
                  <span>.</span><span>.</span><span>.</span>
                </span>
              </div>
            </div>
          </div>

          <!-- 输入区 -->
          <div class="chat-input">
            <el-input
              v-model="inputQuestion"
              placeholder="输入你的问题..."
              size="large"
              :disabled="!selectedDocId || loading"
              @keyup.enter="sendMessage"
            >
              <template #append>
                <el-button
                  type="primary"
                  :icon="Promotion"
                  :loading="loading"
                  :disabled="!selectedDocId"
                  @click="sendMessage"
                />
              </template>
            </el-input>
          </div>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted, nextTick, watch } from 'vue'
import { Promotion } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { getDocumentList } from '../api/document'
import { streamChat } from '../api/chat'

const documentList = ref([])
const selectedDocId = ref(null)
const messages = ref([])
const inputQuestion = ref('')
const loading = ref(false)
const messageContainer = ref(null)
let currentEventSource = null

const scrollToBottom = () => {
  nextTick(() => {
    if (messageContainer.value) {
      messageContainer.value.scrollTop = messageContainer.value.scrollHeight
    }
  })
}

const formatMessage = (text) => {
  if (!text) return ''
  return text.replace(/\n/g, '<br>')
}

const fetchDocuments = async () => {
  try {
    const res = await getDocumentList()
    documentList.value = res.data || res || []
  } catch (e) {
    // error handled by interceptor
  }
}

const sendMessage = () => {
  const question = inputQuestion.value.trim()
  if (!question) return
  if (!selectedDocId.value) {
    ElMessage.warning('请先选择一个文档')
    return
  }

  messages.value.push({ role: 'user', content: question })
  inputQuestion.value = ''
  loading.value = true
  scrollToBottom()

  let aiContent = ''
  messages.value.push({ role: 'ai', content: '' })

  currentEventSource = streamChat(
    selectedDocId.value,
    question,
    (data) => {
      aiContent += data
      messages.value[messages.value.length - 1].content = aiContent
      scrollToBottom()
    },
    () => {
      loading.value = false
      currentEventSource = null
    },
    (error) => {
      loading.value = false
      currentEventSource = null
      if (!aiContent) {
        messages.value[messages.value.length - 1].content = '抱歉，获取回答失败，请重试。'
      }
    }
  )
}

onMounted(() => {
  fetchDocuments()
})
</script>

<style scoped>
.chat-container {
  height: calc(100vh - 108px);
}

.chat-card {
  height: 100%;
}

.chat-card :deep(.el-card__body) {
  height: 100%;
  padding: 0;
}

.chat-layout {
  display: flex;
  height: 100%;
}

.doc-sidebar {
  width: 240px;
  border-right: 1px solid #ebeef5;
  display: flex;
  flex-direction: column;
}

.doc-sidebar-header {
  padding: 16px;
  font-size: 15px;
  font-weight: 600;
  color: #333;
  border-bottom: 1px solid #ebeef5;
}

.doc-list {
  flex: 1;
  overflow-y: auto;
  padding: 8px;
}

.doc-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 12px;
  border-radius: 6px;
  cursor: pointer;
  font-size: 14px;
  color: #606266;
  transition: all 0.2s;
}

.doc-item:hover {
  background: #f5f7fa;
}

.doc-item.active {
  background: #ecf5ff;
  color: #409eff;
}

.doc-name {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.chat-main {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.chat-messages {
  flex: 1;
  overflow-y: auto;
  padding: 20px;
}

.chat-welcome {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100%;
  color: #c0c4cc;
}

.chat-welcome p {
  margin-top: 12px;
  font-size: 15px;
}

.message-wrapper {
  margin-bottom: 20px;
}

.message {
  display: flex;
  gap: 12px;
  max-width: 80%;
}

.user-message {
  margin-left: auto;
  flex-direction: row-reverse;
}

.ai-message {
  margin-right: auto;
}

.message-content {
  padding: 12px 16px;
  border-radius: 10px;
  font-size: 14px;
  line-height: 1.6;
  word-break: break-word;
}

.user-message .message-content {
  background: #409eff;
  color: #fff;
  border-top-right-radius: 2px;
}

.ai-message .message-content {
  background: #f4f4f5;
  color: #333;
  border-top-left-radius: 2px;
}

.loading-content {
  display: flex;
  align-items: center;
  gap: 4px;
}

.typing-cursor {
  color: #909399;
}

.typing-dots span {
  animation: dot-blink 1.4s infinite both;
  color: #909399;
  font-weight: bold;
}

.typing-dots span:nth-child(2) {
  animation-delay: 0.2s;
}

.typing-dots span:nth-child(3) {
  animation-delay: 0.4s;
}

@keyframes dot-blink {
  0%, 80%, 100% { opacity: 0; }
  40% { opacity: 1; }
}

.chat-input {
  padding: 16px 20px;
  border-top: 1px solid #ebeef5;
}

.chat-input :deep(.el-input-group__append) {
  padding: 0;
}

.chat-input :deep(.el-input-group__append .el-button) {
  margin: 0;
  border-radius: 0;
  height: 40px;
  width: 50px;
}
</style>
