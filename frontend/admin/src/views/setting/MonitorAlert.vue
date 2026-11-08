<template>
  <div class="monitor-alert-container">
    <!-- 页面标题 -->
    <div class="page-header">
      <h2>告警管理</h2>
      <p>查看和管理系统的告警信息</p>
    </div>

    <!-- 搜索和过滤区域 -->
    <div class="search-section">
      <el-row :gutter="20">
        <el-col :span="6">
          <el-input
            v-model="searchForm.keyword"
            placeholder="搜索告警名称或内容"
            clearable
            @keyup.enter="handleSearch"
          >
            <template #prefix>
              <el-icon><Search /></el-icon>
            </template>
          </el-input>
        </el-col>
        <el-col :span="4">
          <el-select v-model="searchForm.level" placeholder="告警级别" clearable>
            <el-option label="全部" value="" />
            <el-option label="信息" value="info" />
            <el-option label="警告" value="warning" />
            <el-option label="严重" value="critical" />
            <el-option label="紧急" value="emergency" />
          </el-select>
        </el-col>
        <el-col :span="4">
          <el-select v-model="searchForm.status" placeholder="处理状态" clearable>
            <el-option label="全部" value="" />
            <el-option label="未处理" value="pending" />
            <el-option label="已处理" value="resolved" />
            <el-option label="已忽略" value="ignored" />
          </el-select>
        </el-col>
        <el-col :span="6">
          <el-date-picker
            v-model="searchForm.dateRange"
            type="datetimerange"
            range-separator="至"
            start-placeholder="开始时间"
            end-placeholder="结束时间"
            format="YYYY-MM-DD HH:mm:ss"
            value-format="YYYY-MM-DD HH:mm:ss"
          />
        </el-col>
        <el-col :span="4">
          <el-button type="primary" @click="handleSearch">
            <el-icon><Search /></el-icon>
            搜索
          </el-button>
          <el-button @click="handleReset">
            <el-icon><Refresh /></el-icon>
            重置
          </el-button>
        </el-col>
      </el-row>
    </div>

    <!-- 告警列表 -->
    <div class="table-section">
      <el-table
        v-loading="loading"
        :data="alertList"
        stripe
        border
        style="width: 100%"
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="55" />
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="name" label="告警名称" min-width="200" />
        <el-table-column prop="level" label="告警级别" width="100">
          <template #default="{ row }">
            <el-tag :type="getLevelTagType(row.level)">
              {{ getLevelText(row.level) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="message" label="告警内容" min-width="250" show-overflow-tooltip />
        <el-table-column prop="status" label="处理状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusTagType(row.status)">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="source" label="告警来源" width="120" />
        <el-table-column prop="createTime" label="告警时间" width="180" />
        <el-table-column prop="resolveTime" label="处理时间" width="180" />
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" size="small" @click="handleView(row)">
              查看详情
            </el-button>
            <el-button type="warning" size="small" @click="handleProcess(row)">
              处理
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination-section">
        <el-pagination
          v-model:current-page="pagination.current"
          v-model:page-size="pagination.size"
          :page-sizes="[10, 20, 50, 100]"
          :total="pagination.total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </div>

    <!-- 告警详情对话框 -->
    <el-dialog
      v-model="showDetailDialog"
      title="告警详情"
      width="800px"
      :close-on-click-modal="false"
    >
      <div v-if="currentAlert" class="alert-detail">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="告警名称">{{ currentAlert.name }}</el-descriptions-item>
          <el-descriptions-item label="告警级别">
            <el-tag :type="getLevelTagType(currentAlert.level)">
              {{ getLevelText(currentAlert.level) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="处理状态">
            <el-tag :type="getStatusTagType(currentAlert.status)">
              {{ getStatusText(currentAlert.status) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="告警来源">{{ currentAlert.source }}</el-descriptions-item>
          <el-descriptions-item label="告警时间">{{ currentAlert.createTime }}</el-descriptions-item>
          <el-descriptions-item label="处理时间">{{ currentAlert.resolveTime }}</el-descriptions-item>
          <el-descriptions-item label="告警内容" :span="2">
            {{ currentAlert.message }}
          </el-descriptions-item>
          <el-descriptions-item label="详细信息" :span="2" v-if="currentAlert.details">
            <pre class="alert-details">{{ currentAlert.details }}</pre>
          </el-descriptions-item>
        </el-descriptions>
      </div>
      <template #footer>
        <el-button @click="showDetailDialog = false">关闭</el-button>
        <el-button type="primary" @click="handleProcess(currentAlert)" v-if="currentAlert && currentAlert.status === 'pending'">
          处理告警
        </el-button>
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
import { 
  Search, Refresh 
} from '@element-plus/icons-vue'

// 数据
const loading = ref(false)
const showDetailDialog = ref(false)
const currentAlert = ref(null)
const selectedAlerts = ref([])
const alertList = ref([])

const searchForm = reactive({
  keyword: '',
  level: '',
  status: '',
  dateRange: []
})

const pagination = reactive({
  current: 1,
  size: 20,
  total: 0
})

// 模拟数据
const mockAlerts = [
  {
    id: 1,
    name: 'CPU使用率过高',
    level: 'critical',
    message: 'CPU使用率达到95%，超过阈值80%',
    status: 'pending',
    source: '服务器1',
    createTime: '2024-01-01 09:00:00',
    resolveTime: '',
    details: 'CPU使用率: 95%\n阈值: 80%\n持续时间: 5分钟'
  },
  {
    id: 2,
    name: '内存使用率告警',
    level: 'warning',
    message: '内存使用率达到85%，接近阈值90%',
    status: 'resolved',
    source: '服务器2',
    createTime: '2024-01-01 10:00:00',
    resolveTime: '2024-01-01 10:30:00',
    details: '内存使用率: 85%\n阈值: 90%\n可用内存: 2GB'
  },
  {
    id: 3,
    name: '磁盘空间不足',
    level: 'emergency',
    message: '磁盘使用率达到98%，剩余空间不足1GB',
    status: 'pending',
    source: '服务器3',
    createTime: '2024-01-01 11:00:00',
    resolveTime: '',
    details: '磁盘使用率: 98%\n总空间: 100GB\n剩余空间: 0.8GB'
  }
]

// 方法
const loadAlertList = async () => {
  loading.value = true
  try {
    // 模拟API调用
    await new Promise(resolve => setTimeout(resolve, 1000))
    
    alertList.value = mockAlerts
    pagination.total = mockAlerts.length
    
    ElMessage.success('告警列表加载成功')
  } catch (error) {
    ElMessage.error('加载告警列表失败: ' + error.message)
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  pagination.current = 1
  loadAlertList()
}

const handleReset = () => {
  searchForm.keyword = ''
  searchForm.level = ''
  searchForm.status = ''
  searchForm.dateRange = []
  handleSearch()
}

const handleView = (alert) => {
  currentAlert.value = alert
  showDetailDialog.value = true
}

const handleProcess = async (alert) => {
  try {
    await ElMessageBox.confirm(
      `确定要处理告警"${alert.name}"吗？`,
      '确认处理',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    // 模拟处理操作
    alert.status = 'resolved'
    alert.resolveTime = new Date().toLocaleString()
    
    ElMessage.success('告警处理成功')
    showDetailDialog.value = false
  } catch {
    ElMessage.info('已取消处理')
  }
}

const handleSelectionChange = (selection) => {
  selectedAlerts.value = selection
}

const handleSizeChange = (size) => {
  pagination.size = size
  loadAlertList()
}

const handleCurrentChange = (current) => {
  pagination.current = current
  loadAlertList()
}

// 状态相关方法
const getLevelTagType = (level) => {
  const levelMap = {
    info: 'info',
    warning: 'warning',
    critical: 'danger',
    emergency: 'danger'
  }
  return levelMap[level] || 'info'
}

const getLevelText = (level) => {
  const levelMap = {
    info: '信息',
    warning: '警告',
    critical: '严重',
    emergency: '紧急'
  }
  return levelMap[level] || '未知'
}

const getStatusTagType = (status) => {
  const statusMap = {
    pending: 'warning',
    resolved: 'success',
    ignored: 'info'
  }
  return statusMap[status] || 'info'
}

const getStatusText = (status) => {
  const statusMap = {
    pending: '未处理',
    resolved: '已处理',
    ignored: '已忽略'
  }
  return statusMap[status] || '未知'
}

// 生命周期
onMounted(() => {
  loadAlertList()
})
</script>

<style scoped>
.monitor-alert-container {
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

.search-section {
  margin-bottom: 20px;
}

.table-section {
  background: #fff;
  padding: 20px;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.pagination-section {
  margin-top: 20px;
  text-align: right;
}

.alert-detail {
  padding: 20px 0;
}

.alert-details {
  background: #f5f7fa;
  padding: 10px;
  border-radius: 4px;
  font-family: 'Courier New', monospace;
  font-size: 12px;
  line-height: 1.5;
  white-space: pre-wrap;
  word-break: break-all;
}
</style>
