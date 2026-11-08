<template>
  <div class="ai-chat" v-loading="historyLoading">
    <div class="page-header">
      <h1>AI智能对话</h1>
      <p>与AI助手进行智能对话交流</p>
    </div>

    <el-row :gutter="20">
      <el-col :span="6">
        <el-card class="chat-sidebar">
          <template #header>
            <div class="card-header">
              <span>对话历史</span>
              <el-button type="primary" size="small" @click="startNewChat">
                <el-icon><Plus /></el-icon>
                新对话
              </el-button>
            </div>
          </template>
          <div class="chat-history">
            <el-empty v-if="!chatHistory.length" description="暂无对话，点击新对话开始" :image-size="80" />
            <div 
              v-for="chat in chatHistory" 
              :key="getChatKey(chat)"
              class="chat-item"
              :class="{ active: getChatKey(chat) === selectedChatKey }"
              @click="selectChat(chat)"
            >
              <div class="chat-title">{{ chat.title }}</div>
              <div class="chat-time">{{ chat.time }}</div>
            </div>
          </div>
        </el-card>
      </el-col>
      
      <el-col :span="18">
        <el-card class="chat-main">
          <template #header>
            <div class="card-header">
              <span>{{ selectedChat?.title || 'AI对话' }}</span>
              <div class="chat-actions">
                <el-select
                  v-model="selectedModelId"
                  placeholder="选择模型"
                  size="small"
                  style="width: 160px; margin-right: 8px"
                  :loading="modelsLoading"
                >
                  <el-option
                    v-for="opt in modelOptions"
                    :key="opt.value"
                    :label="opt.label"
                    :value="opt.value"
                  />
                </el-select>
                <el-button v-if="hasPermission('shell:ai:chat:clear')" size="small" @click="clearChat">
                  <el-icon><Delete /></el-icon>
                  清空对话
                </el-button>
              </div>
            </div>
          </template>
          
          <div class="chat-messages" ref="messagesContainer">
            <el-empty v-if="!currentMessages.length" description="发送消息开始对话" :image-size="100" />
            <div 
              v-for="message in currentMessages" 
              :key="message.id"
              class="message"
              :class="{ 'user-message': message.type === 'user', 'ai-message': message.type === 'ai' }"
            >
              <div class="message-avatar">
                <el-icon v-if="message.type === 'user'"><User /></el-icon>
                <el-icon v-else><Service /></el-icon>
              </div>
              <div class="message-content">
                <div class="message-text">{{ message.content }}</div>
                <div class="message-time">{{ message.time }}</div>
              </div>
            </div>
          </div>
          
          <div class="chat-input">
            <el-input
              v-model="inputMessage"
              placeholder="请输入您的问题..."
              type="textarea"
              :rows="3"
              @keydown.ctrl.enter="sendMessage"
            />
            <div class="input-actions">
              <el-button v-if="hasPermission('shell:ai:chat:send')" type="primary" @click="sendMessage" :loading="isLoading">
                <el-icon><Position /></el-icon>
                发送 (Ctrl+Enter)
              </el-button>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>

// 使用 Element Plus 国际化系统
import { useI18n } from '@/i18n-element'
const { t } = useI18n()
import { ref, onMounted, nextTick, computed, watch } from 'vue'
import { Plus, Delete, User, Service, Position } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { aiApi } from '@/api/ai/ai'
import { usePaasAiModelOptions } from '@/composables/usePaasAiModelOptions'
import { hasPermission } from '@/utils/permission'

const { modelOptions, loading: modelsLoading, defaultModelId } = usePaasAiModelOptions()
const selectedModelId = ref(null)

watch(defaultModelId, (id) => {
  if (id && !selectedModelId.value) selectedModelId.value = id
}, { immediate: true })

const inputMessage = ref('')
const isLoading = ref(false)
const historyLoading = ref(false)
const messagesContainer = ref(null)

const selectedChat = ref(null)
const currentMessages = ref([])
const chatHistory = ref([])

const getChatKey = (chat) => chat?.id ?? chat?.localId
const selectedChatKey = computed(() => getChatKey(selectedChat.value))

const formatTime = (value) => {
  if (!value) return '刚刚'
  const d = new Date(value)
  if (Number.isNaN(d.getTime())) return String(value)
  return d.toLocaleString('zh-CN', { month: '2-digit', day: '2-digit', hour: '2-digit', minute: '2-digit' })
}

const parseMessages = (content) => {
  if (!content) return []
  try {
    const arr = typeof content === 'string' ? JSON.parse(content) : content
    if (!Array.isArray(arr)) return []
    return arr.map((item, idx) => ({
      id: idx + 1,
      type: item.role === 'assistant' ? 'ai' : 'user',
      content: item.content || '',
      time: item.time ? formatTime(item.time) : ''
    }))
  } catch {
    return []
  }
}

const mapHistoryItem = (item) => ({
  id: item.id,
  title: item.title || '对话',
  time: formatTime(item.updateTime || item.createTime),
  messages: parseMessages(item.content),
  raw: item
})

const loadChatHistory = async () => {
  historyLoading.value = true
  try {
    const res = await aiApi.getChatHistory({ page: 1, limit: 50 })
    if (res.code === 1 && Array.isArray(res.data)) {
      const localNew = chatHistory.value.filter(c => !c.id && c.localId)
      chatHistory.value = [...localNew, ...res.data.map(mapHistoryItem)]
      if (!selectedChat.value && chatHistory.value.length) {
        selectChat(chatHistory.value[0])
      }
    }
  } catch (e) {
    ElMessage.error(e.message || '加载对话历史失败')
  } finally {
    historyLoading.value = false
  }
}

const selectChat = (chat) => {
  selectedChat.value = chat
  currentMessages.value = [...(chat.messages || [])]
  nextTick(() => scrollToBottom())
}

const startNewChat = () => {
  const newChat = {
    id: null,
    localId: `local-${Date.now()}`,
    title: '新对话',
    time: '刚刚',
    messages: []
  }
  chatHistory.value.unshift(newChat)
  selectedChat.value = newChat
  currentMessages.value = []
}

const sendMessage = async () => {
  if (!inputMessage.value.trim() || isLoading.value) return

  if (!selectedChat.value) {
    startNewChat()
  }

  const userMessage = {
    id: Date.now(),
    type: 'user',
    content: inputMessage.value,
    time: new Date().toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })
  }

  currentMessages.value.push(userMessage)
  selectedChat.value.messages.push(userMessage)

  const message = inputMessage.value
  inputMessage.value = ''
  isLoading.value = true

  await nextTick()
  scrollToBottom()

  try {
    const payload = { message }
    if (selectedModelId.value) payload.modelId = selectedModelId.value
    if (selectedChat.value.id) payload.chatId = selectedChat.value.id
    const res = await aiApi.chat(payload)
    if (res.code === 1 && res.data) {
      const reply = res.data.reply || res.data.content || ''
      const aiMessage = {
        id: Date.now() + 1,
        type: 'ai',
        content: reply,
        time: new Date().toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })
      }
      currentMessages.value.push(aiMessage)
      selectedChat.value.messages.push(aiMessage)

      if (res.data.chatId) {
        selectedChat.value.id = res.data.chatId
      }
      if (selectedChat.value.title === '新对话') {
        selectedChat.value.title = message.length > 20 ? message.substring(0, 20) + '...' : message
      }
      await loadChatHistory()
      const updated = chatHistory.value.find(c => c.id === selectedChat.value.id)
      if (updated) selectChat(updated)
    } else {
      ElMessage.error(res.message || 'AI 回复失败')
    }
  } catch (error) {
    ElMessage.error(error.message || '发送失败')
  } finally {
    isLoading.value = false
    nextTick(() => scrollToBottom())
  }
}

const clearChat = async () => {
  if (!selectedChat.value) return
  try {
    await ElMessageBox.confirm('确定要清空当前对话吗？', '确认清空', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    if (selectedChat.value.id) {
      const res = await aiApi.clearChatHistory({ chatId: selectedChat.value.id })
      if (res.code !== 1) {
        ElMessage.error(res.message || '清空失败')
        return
      }
    }
    selectedChat.value.messages = []
    currentMessages.value = []
    await loadChatHistory()
    ElMessage.success('对话已清空')
  } catch {
    ElMessage.info('已取消')
  }
}

const scrollToBottom = () => {
  if (messagesContainer.value) {
    messagesContainer.value.scrollTop = messagesContainer.value.scrollHeight
  }
}

onMounted(() => {
  loadChatHistory()
})
</script>

<style scoped>
.ai-chat {
  padding: 20px;
  background-color: #f5f5f5;
  min-height: 100vh;
}

.page-header {
  margin-bottom: 20px;
  text-align: center;
}

.page-header h1 {
  font-size: 28px;
  color: #303133;
  margin-bottom: 10px;
}

.page-header p {
  font-size: 16px;
  color: #909399;
}

.chat-sidebar {
  height: 600px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.chat-history {
  max-height: 500px;
  overflow-y: auto;
}

.chat-item {
  padding: 12px;
  border-radius: 6px;
  cursor: pointer;
  margin-bottom: 8px;
  transition: background-color 0.3s;
}

.chat-item:hover {
  background-color: #f0f9ff;
}

.chat-item.active {
  background-color: #409EFF;
  color: white;
}

.chat-title {
  font-size: 14px;
  font-weight: 500;
  margin-bottom: 4px;
}

.chat-time {
  font-size: 12px;
  opacity: 0.7;
}

.chat-main {
  height: 600px;
  display: flex;
  flex-direction: column;
}

.chat-actions {
  display: flex;
  gap: 10px;
}

.chat-messages {
  flex: 1;
  overflow-y: auto;
  padding: 20px;
  background-color: #fafafa;
  border-radius: 6px;
  margin-bottom: 20px;
}

.message {
  display: flex;
  margin-bottom: 20px;
}

.user-message {
  flex-direction: row-reverse;
}

.message-avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 10px;
  font-size: 18px;
}

.user-message .message-avatar {
  background-color: #409EFF;
  color: white;
}

.ai-message .message-avatar {
  background-color: #67C23A;
  color: white;
}

.message-content {
  max-width: 70%;
  background-color: white;
  padding: 12px 16px;
  border-radius: 12px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
}

.user-message .message-content {
  background-color: #409EFF;
  color: white;
}

.message-text {
  font-size: 14px;
  line-height: 1.5;
  white-space: pre-wrap;
}

.message-time {
  font-size: 12px;
  opacity: 0.7;
  margin-top: 5px;
}

.chat-input {
  padding-top: 20px;
  border-top: 1px solid #e4e7ed;
}

.input-actions {
  display: flex;
  justify-content: flex-end;
  margin-top: 10px;
}
</style>
