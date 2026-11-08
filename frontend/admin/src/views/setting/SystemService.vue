<template>
  <div class="system-service">
    <el-card class="box-card">
      <template #header>
        <div class="card-header">
          <span>服务管理</span>
          <el-button type="primary" @click="refreshServices">刷新</el-button>
        </div>
      </template>
      
      <el-tabs v-model="activeTab">
        <el-tab-pane label="服务列表" name="list">
          <div class="tab-content">
            <el-table :data="services" style="width: 100%">
              <el-table-column prop="name" label="服务名称" width="200" />
              <el-table-column prop="description" label="描述" min-width="200" />
              <el-table-column prop="version" label="版本" width="100" />
              <el-table-column prop="status" label="状态" width="120">
                <template #default="{ row }">
                  <el-tag :type="getServiceStatusTag(row.status)">
                    {{ getServiceStatusText(row.status) }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column prop="port" label="端口" width="80" />
              <el-table-column prop="pid" label="进程ID" width="100" />
              <el-table-column prop="startTime" label="启动时间" width="180" />
              <el-table-column label="操作" width="200">
                <template #default="{ row }">
                  <el-button 
                    size="small" 
                    type="primary" 
                    link 
                    @click="viewServiceDetail(row)"
                  >
                    详情
                  </el-button>
                  <el-button 
                    v-if="row.status === 'stopped'" 
                    size="small" 
                    type="success" 
                    link 
                    @click="startService(row)"
                  >
                    启动
                  </el-button>
                  <el-button 
                    v-if="row.status === 'running'" 
                    size="small" 
                    type="warning" 
                    link 
                    @click="stopService(row)"
                  >
                    停止
                  </el-button>
                  <el-button 
                    size="small" 
                    type="danger" 
                    link 
                    @click="restartService(row)"
                  >
                    重启
                  </el-button>
                </template>
              </el-table-column>
            </el-table>
          </div>
        </el-tab-pane>
        
        <el-tab-pane label="服务监控" name="monitor">
          <div class="tab-content">
            <el-row :gutter="20">
              <el-col :span="6">
                <el-card>
                  <template #header>
                    <div class="card-header">
                      <span>运行服务</span>
                    </div>
                  </template>
                  <div class="metric-value">{{ serviceStats.running }}</div>
                  <div class="metric-desc">正在运行的服务数</div>
                </el-card>
              </el-col>
              <el-col :span="6">
                <el-card>
                  <template #header>
                    <div class="card-header">
                      <span>停止服务</span>
                    </div>
                  </template>
                  <div class="metric-value">{{ serviceStats.stopped }}</div>
                  <div class="metric-desc">已停止的服务数</div>
                </el-card>
              </el-col>
              <el-col :span="6">
                <el-card>
                  <template #header>
                    <div class="card-header">
                      <span>异常服务</span>
                    </div>
                  </template>
                  <div class="metric-value">{{ serviceStats.error }}</div>
                  <div class="metric-desc">异常的服务数</div>
                </el-card>
              </el-col>
              <el-col :span="6">
                <el-card>
                  <template #header>
                    <div class="card-header">
                      <span>总服务数</span>
                    </div>
                  </template>
                  <div class="metric-value">{{ serviceStats.total }}</div>
                  <div class="metric-desc">系统服务总数</div>
                </el-card>
              </el-col>
            </el-row>
            
            <div class="service-chart">
              <div ref="chartRef" style="width: 100%; height: 300px;"></div>
            </div>
          </div>
        </el-tab-pane>
        
        <el-tab-pane label="日志查看" name="logs">
          <div class="tab-content">
            <div class="toolbar">
              <el-select v-model="logForm.service" placeholder="选择服务" style="width: 200px; margin-right: 10px;">
                <el-option
                  v-for="service in services"
                  :key="service.name"
                  :label="service.name"
                  :value="service.name"
                />
              </el-select>
              <el-select v-model="logForm.level" placeholder="日志级别" style="width: 120px; margin-right: 10px;">
                <el-option label="全部" value="" />
                <el-option label="INFO" value="info" />
                <el-option label="WARN" value="warn" />
                <el-option label="ERROR" value="error" />
              </el-select>
              <el-date-picker
                v-model="logForm.dateRange"
                type="datetimerange"
                range-separator="至"
                start-placeholder="开始时间"
                end-placeholder="结束时间"
                format="YYYY-MM-DD HH:mm:ss"
                value-format="YYYY-MM-DD HH:mm:ss"
                style="width: 350px; margin-right: 10px;"
              />
              <el-button type="primary" @click="searchLogs">
                <el-icon><Search /></el-icon>
                搜索
              </el-button>
              <el-button @click="clearLogs">
                <el-icon><Delete /></el-icon>
                清空
              </el-button>
            </div>
            
            <div class="log-container">
              <pre class="log-content">{{ logContent }}</pre>
            </div>
          </div>
        </el-tab-pane>
      </el-tabs>
    </el-card>
    
    <!-- 服务详情对话框 -->
    <el-dialog v-model="detailVisible" title="服务详情" width="600px">
      <el-descriptions :column="1" border>
        <el-descriptions-item label="服务名称">{{ currentService.name }}</el-descriptions-item>
        <el-descriptions-item label="描述">{{ currentService.description }}</el-descriptions-item>
        <el-descriptions-item label="版本">{{ currentService.version }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="getServiceStatusTag(currentService.status)">
            {{ getServiceStatusText(currentService.status) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="端口">{{ currentService.port }}</el-descriptions-item>
        <el-descriptions-item label="进程ID">{{ currentService.pid }}</el-descriptions-item>
        <el-descriptions-item label="启动时间">{{ currentService.startTime }}</el-descriptions-item>
        <el-descriptions-item label="内存使用">{{ currentService.memory }}</el-descriptions-item>
        <el-descriptions-item label="CPU使用">{{ currentService.cpu }}</el-descriptions-item>
      </el-descriptions>
      <template #footer>
        <el-button @click="detailVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>

// 使用 Element Plus 国际化系统
import { useI18n } from '@/i18n-element'
const { t } = useI18n()
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Delete } from '@element-plus/icons-vue'

const activeTab = ref('list')
const detailVisible = ref(false)
const currentService = ref({})

// 服务数据
const services = ref([
  {
    id: 1,
    name: 'user-service',
    description: '用户管理服务',
    version: '1.2.3',
    status: 'running',
    port: 8081,
    pid: 12345,
    startTime: '2024-01-08 09:00:00',
    memory: '256MB',
    cpu: '15%'
  },
  {
    id: 2,
    name: 'order-service',
    description: '订单管理服务',
    version: '1.1.0',
    status: 'running',
    port: 8082,
    pid: 12346,
    startTime: '2024-01-08 09:05:00',
    memory: '180MB',
    cpu: '8%'
  },
  {
    id: 3,
    name: 'payment-service',
    description: '支付服务',
    version: '1.0.5',
    status: 'stopped',
    port: 8083,
    pid: 0,
    startTime: '',
    memory: '0MB',
    cpu: '0%'
  },
  {
    id: 4,
    name: 'notification-service',
    description: '通知服务',
    version: '1.3.2',
    status: 'error',
    port: 8084,
    pid: 12348,
    startTime: '2024-01-08 09:15:00',
    memory: '95MB',
    cpu: '35%'
  }
])

// 服务统计信息
const serviceStats = reactive({
  running: 2,
  stopped: 1,
  error: 1,
  total: 4
})

// 日志表单
const logForm = reactive({
  service: '',
  level: '',
  dateRange: []
})

// 日志内容
const logContent = ref(`[2024-01-08 10:30:15] INFO  [user-service] User login successful for user admin
[2024-01-08 10:30:12] WARN  [order-service] Order timeout warning for order #12345
[2024-01-08 10:30:10] INFO  [user-service] User profile updated for user john_doe
[2024-01-08 10:30:08] ERROR [notification-service] Email sending failed: SMTP connection timeout
[2024-01-08 10:30:05] INFO  [payment-service] Payment processed successfully for transaction #54321
[2024-01-08 10:30:02] INFO  [order-service] New order created #67890
[2024-01-08 10:30:00] INFO  [user-service] User registered: jane_smith`)

const getServiceStatusText = (status) => {
  const statusMap = {
    'running': '运行中',
    'stopped': '已停止',
    'error': '异常',
    'starting': '启动中',
    'stopping': '停止中'
  }
  return statusMap[status] || status
}

const getServiceStatusTag = (status) => {
  const tagMap = {
    'running': 'success',
    'stopped': 'info',
    'error': 'danger',
    'starting': 'warning',
    'stopping': 'warning'
  }
  return tagMap[status] || 'info'
}

const refreshServices = () => {
  ElMessage.success('服务列表已刷新')
}

const viewServiceDetail = (row) => {
  currentService.value = row
  detailVisible.value = true
}

const startService = async (row) => {
  try {
    await ElMessageBox.confirm(`确定要启动服务 "${row.name}" 吗？`, '确认启动', {
      type: 'warning'
    })
    ElMessage.success(`服务 "${row.name}" 启动成功`)
    row.status = 'running'
  } catch {
    ElMessage.info('已取消启动')
  }
}

const stopService = async (row) => {
  try {
    await ElMessageBox.confirm(`确定要停止服务 "${row.name}" 吗？`, '确认停止', {
      type: 'warning'
    })
    ElMessage.success(`服务 "${row.name}" 停止成功`)
    row.status = 'stopped'
  } catch {
    ElMessage.info('已取消停止')
  }
}

const restartService = async (row) => {
  try {
    await ElMessageBox.confirm(`确定要重启服务 "${row.name}" 吗？`, '确认重启', {
      type: 'warning'
    })
    ElMessage.success(`服务 "${row.name}" 重启成功`)
  } catch {
    ElMessage.info('已取消重启')
  }
}

const searchLogs = () => {
  ElMessage.success('正在搜索日志...')
}

const clearLogs = () => {
  logContent.value = ''
  ElMessage.success('日志已清空')
}

onMounted(() => {
  // 初始化图表
  console.log('初始化服务监控图表')
})
</script>

<style scoped>
.system-service {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.tab-content {
  padding: 20px 0;
}

.toolbar {
  margin-bottom: 20px;
  display: flex;
  align-items: center;
}

.metric-value {
  font-size: 24px;
  font-weight: bold;
  text-align: center;
  color: #303133;
}

.metric-desc {
  font-size: 14px;
  text-align: center;
  color: #909399;
  margin-top: 10px;
}

.service-chart {
  margin-top: 20px;
  padding: 20px;
  background: #f5f7fa;
  border-radius: 4px;
}

.log-container {
  margin-top: 20px;
  padding: 20px;
  background: #1e1e1e;
  border-radius: 4px;
  max-height: 400px;
  overflow-y: auto;
}

.log-content {
  color: #d4d4d4;
  font-family: 'Courier New', monospace;
  font-size: 12px;
  line-height: 1.5;
  margin: 0;
}
</style>
