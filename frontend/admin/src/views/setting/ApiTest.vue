<template>
  <div class="api-test-container">
    <!-- 页面标题 -->
    <div class="page-header">
      <h2>API测试</h2>
      <p>在线测试API接口功能</p>
    </div>

    <el-row :gutter="20">
      <!-- 请求配置区域 -->
      <el-col :span="12">
        <el-card class="request-card">
          <template #header>
            <div class="card-header">
              <span>请求配置</span>
            </div>
          </template>
          
          <el-form :model="requestForm" label-width="100px">
            <el-form-item label="请求地址">
              <el-input v-model="requestForm.url" placeholder="请输入完整的API地址">
                <template #prepend>
                  <el-select v-model="requestForm.method" style="width: 100px;">
                    <el-option label="GET" value="GET" />
                    <el-option label="POST" value="POST" />
                    <el-option label="PUT" value="PUT" />
                    <el-option label="DELETE" value="DELETE" />
                  </el-select>
                </template>
              </el-input>
            </el-form-item>
            
            <el-form-item label="请求头">
              <el-input 
                v-model="requestForm.headers" 
                type="textarea" 
                :rows="4" 
                placeholder="请输入请求头，每行一个，格式：Key: Value"
              />
            </el-form-item>
            
            <el-form-item label="请求参数">
              <el-tabs v-model="paramsTab">
                <el-tab-pane label="查询参数" name="query">
                  <el-input 
                    v-model="requestForm.queryParams" 
                    type="textarea" 
                    :rows="4" 
                    placeholder="请输入查询参数，每行一个，格式：Key=Value"
                  />
                </el-tab-pane>
                <el-tab-pane label="请求体" name="body">
                  <el-input 
                    v-model="requestForm.body" 
                    type="textarea" 
                    :rows="6" 
                    placeholder="请输入请求体内容"
                  />
                  <div class="form-tip">支持JSON、XML、表单等格式</div>
                </el-tab-pane>
              </el-tabs>
            </el-form-item>
            
            <el-form-item>
              <el-button type="primary" @click="handleSendRequest" :loading="sending">
                <el-icon><Promotion /></el-icon>
                发送请求
              </el-button>
              <el-button @click="handleClear">
                <el-icon><Delete /></el-icon>
                清空
              </el-button>
            </el-form-item>
          </el-form>
        </el-card>
      </el-col>
      
      <!-- 响应结果区域 -->
      <el-col :span="12">
        <el-card class="response-card">
          <template #header>
            <div class="card-header">
              <span>响应结果</span>
              <div class="response-info" v-if="responseInfo">
                <el-tag :type="getResponseStatusType(responseInfo.status)">
                  {{ responseInfo.status }} {{ responseInfo.statusText }}
                </el-tag>
                <span class="response-time">耗时: {{ responseInfo.time }}ms</span>
              </div>
            </div>
          </template>
          
          <el-tabs v-model="responseTab">
            <el-tab-pane label="响应体" name="body">
              <pre class="response-content">{{ responseContent }}</pre>
            </el-tab-pane>
            <el-tab-pane label="响应头" name="headers">
              <pre class="response-content">{{ responseHeaders }}</pre>
            </el-tab-pane>
          </el-tabs>
        </el-card>
      </el-col>
    </el-row>
    
    <!-- 历史记录 -->
    <el-card class="history-card" style="margin-top: 20px;">
      <template #header>
        <div class="card-header">
          <span>测试历史</span>
        </div>
      </template>
      
      <el-table :data="historyList" stripe border style="width: 100%">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="method" label="方法" width="100">
          <template #default="{ row }">
            <el-tag :type="getMethodTagType(row.method)">
              {{ row.method }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="url" label="请求地址" min-width="300" />
        <el-table-column prop="status" label="状态码" width="100">
          <template #default="{ row }">
            <el-tag :type="getResponseStatusType(row.status)">
              {{ row.status }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="time" label="耗时(ms)" width="120" />
        <el-table-column prop="createTime" label="测试时间" width="180" />
        <el-table-column label="操作" width="150">
          <template #default="{ row }">
            <el-button type="primary" size="small" @click="handleReplay(row)">
              重放
            </el-button>
            <el-button type="danger" size="small" @click="handleDeleteHistory(row)">
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup>

// 使用 Element Plus 国际化系统
import { useI18n } from '@/i18n-element'
const { t } = useI18n()
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { 
  Promotion, Delete 
} from '@element-plus/icons-vue'

// 数据
const sending = ref(false)
const paramsTab = ref('query')
const responseTab = ref('body')
const responseContent = ref('')
const responseHeaders = ref('')
const responseInfo = ref(null)

const requestForm = reactive({
  method: 'GET',
  url: '',
  headers: '',
  queryParams: '',
  body: ''
})

const historyList = ref([
  {
    id: 1,
    method: 'GET',
    url: '/users',
    status: 200,
    time: 120,
    createTime: '2024-01-01 09:00:00'
  },
  {
    id: 2,
    method: 'POST',
    url: '/users',
    status: 201,
    time: 150,
    createTime: '2024-01-01 10:00:00'
  }
])

// 方法
const handleSendRequest = async () => {
  if (!requestForm.url) {
    ElMessage.warning('请输入请求地址')
    return
  }
  
  sending.value = true
  responseContent.value = ''
  responseHeaders.value = ''
  responseInfo.value = null
  
  try {
    // 模拟API调用
    await new Promise(resolve => setTimeout(resolve, 1000))
    
    // 模拟响应数据
    responseContent.value = JSON.stringify({
      code: 200,
      message: 'success',
      data: [
        { id: 1, name: '张三', email: 'zhangsan@example.com' },
        { id: 2, name: '李四', email: 'lisi@example.com' }
      ]
    }, null, 2)
    
    responseHeaders.value = `Content-Type: application/json\nContent-Length: 1234\nServer: nginx`
    
    responseInfo.value = {
      status: 200,
      statusText: 'OK',
      time: Math.floor(Math.random() * 500) + 100
    }
    
    // 添加到历史记录
    addToHistory()
    
    ElMessage.success('请求发送成功')
  } catch (error) {
    responseContent.value = `请求失败: ${error.message}`
    ElMessage.error('请求发送失败: ' + error.message)
  } finally {
    sending.value = false
  }
}

const handleClear = () => {
  requestForm.url = ''
  requestForm.headers = ''
  requestForm.queryParams = ''
  requestForm.body = ''
  responseContent.value = ''
  responseHeaders.value = ''
  responseInfo.value = null
}

const handleReplay = (history) => {
  requestForm.method = history.method
  requestForm.url = history.url
  // 其他字段需要从历史记录中恢复，这里简化处理
  ElMessage.info('重放请求功能开发中...')
}

const handleDeleteHistory = async (history) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除这条历史记录吗？`,
      '确认删除',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    ElMessage.success('删除成功')
    // 重新加载历史记录
  } catch {
    ElMessage.info('已取消删除')
  }
}

const addToHistory = () => {
  // 添加到历史记录
  const newRecord = {
    id: historyList.value.length + 1,
    method: requestForm.method,
    url: requestForm.url,
    status: responseInfo.value?.status || 0,
    time: responseInfo.value?.time || 0,
    createTime: new Date().toLocaleString()
  }
  
  historyList.value.unshift(newRecord)
}

// 状态相关方法
const getMethodTagType = (method) => {
  const methodMap = {
    GET: 'success',
    POST: 'warning',
    PUT: 'primary',
    DELETE: 'danger'
  }
  return methodMap[method] || 'info'
}

const getResponseStatusType = (status) => {
  if (status >= 200 && status < 300) {
    return 'success'
  } else if (status >= 400 && status < 500) {
    return 'warning'
  } else if (status >= 500) {
    return 'danger'
  }
  return 'info'
}

// 生命周期
onMounted(() => {
  // 初始化
})
</script>

<style scoped>
.api-test-container {
  padding: 20px;
}

.page-header {
  margin-bottom: 20px;
}

.page-header h2 {
  margin: 0 0 8px 0;
  color: #303133;
}

.page-header p {
  margin: 0;
  color: #606266;
  font-size: 14px;
}

.request-card,
.response-card,
.history-card {
  height: 100%;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.response-info {
  display: flex;
  align-items: center;
  gap: 10px;
}

.response-time {
  font-size: 12px;
  color: #909399;
}

.response-content {
  background: #f5f7fa;
  padding: 15px;
  border-radius: 4px;
  font-family: 'Courier New', monospace;
  font-size: 12px;
  line-height: 1.5;
  white-space: pre-wrap;
  word-break: break-all;
  max-height: 400px;
  overflow-y: auto;
}

.form-tip {
  margin-top: 5px;
  color: #909399;
  font-size: 12px;
}
</style>
