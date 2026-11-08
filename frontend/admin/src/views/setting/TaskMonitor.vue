<template>
  <div class="task-monitor-container">
    <!-- 页面标题 -->
    <div class="page-header">
      <h2>任务监控</h2>
      <p>实时监控任务执行状态和系统性能</p>
    </div>

    <!-- 监控面板 -->
    <div class="monitor-section">
      <el-row :gutter="20">
        <el-col :span="6">
          <el-card class="monitor-card">
            <div class="card-content">
              <div class="card-icon success">
                <el-icon><SuccessFilled /></el-icon>
              </div>
              <div class="card-info">
                <div class="card-title">成功任务</div>
                <div class="card-value">{{ stats.success }}</div>
              </div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card class="monitor-card">
            <div class="card-content">
              <div class="card-icon warning">
                <el-icon><Warning /></el-icon>
              </div>
              <div class="card-info">
                <div class="card-title">执行中</div>
                <div class="card-value">{{ stats.running }}</div>
              </div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card class="monitor-card">
            <div class="card-content">
              <div class="card-icon danger">
                <el-icon><CircleClose /></el-icon>
              </div>
              <div class="card-info">
                <div class="card-title">失败任务</div>
                <div class="card-value">{{ stats.failed }}</div>
              </div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card class="monitor-card">
            <div class="card-content">
              <div class="card-icon info">
                <el-icon><InfoFilled /></el-icon>
              </div>
              <div class="card-info">
                <div class="card-title">待执行</div>
                <div class="card-value">{{ stats.pending }}</div>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </div>

    <!-- 实时任务列表 -->
    <div class="realtime-section">
      <el-card>
        <template #header>
          <div class="card-header">
            <span>实时任务列表</span>
            <el-button type="primary" @click="refreshTasks" :loading="loading">
              <el-icon><Refresh /></el-icon>
              刷新
            </el-button>
          </div>
        </template>
        
        <el-table
          v-loading="loading"
          :data="taskList"
          stripe
          border
          style="width: 100%"
        >
          <el-table-column prop="id" label="ID" width="80" />
          <el-table-column prop="name" label="任务名称" min-width="200" />
          <el-table-column prop="status" label="状态" width="100">
            <template #default="{ row }">
              <el-tag :type="getStatusTagType(row.status)">
                {{ getStatusText(row.status) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="progress" label="进度" width="150">
            <template #default="{ row }">
              <el-progress 
                :percentage="row.progress" 
                :status="getProgressStatus(row.status)"
                :stroke-width="10"
              />
            </template>
          </el-table-column>
          <el-table-column prop="assignee" label="执行人" width="120" />
          <el-table-column prop="startTime" label="开始时间" width="180" />
          <el-table-column prop="duration" label="执行时长" width="120" />
        </el-table>
      </el-card>
    </div>

    <!-- 性能图表 -->
    <div class="chart-section">
      <el-row :gutter="20">
        <el-col :span="12">
          <el-card>
            <template #header>
              <div class="card-header">
                <span>任务执行趋势</span>
              </div>
            </template>
            <div ref="trendChartRef" class="chart-container"></div>
          </el-card>
        </el-col>
        <el-col :span="12">
          <el-card>
            <template #header>
              <div class="card-header">
                <span>任务状态分布</span>
              </div>
            </template>
            <div ref="statusChartRef" class="chart-container"></div>
          </el-card>
        </el-col>
      </el-row>
    </div>
  </div>
</template>

<script setup>

// 使用 Element Plus 国际化系统
import { useI18n } from '@/i18n-element'
const { t } = useI18n()
import { ref, reactive, onMounted, onBeforeUnmount } from 'vue'
import { ElMessage } from 'element-plus'
import { 
  SuccessFilled, Warning, CircleClose, InfoFilled, Refresh 
} from '@element-plus/icons-vue'
import * as echarts from 'echarts'

// 数据
const loading = ref(false)
const trendChartRef = ref()
const statusChartRef = ref()
let trendChart = null
let statusChart = null

const stats = reactive({
  success: 24,
  running: 3,
  failed: 2,
  pending: 8
})

const taskList = ref([
  {
    id: 1,
    name: '数据备份任务',
    status: 'success',
    progress: 100,
    assignee: '系统管理员',
    startTime: '2024-01-01 09:00:00',
    duration: '15分钟'
  },
  {
    id: 2,
    name: '日志清理任务',
    status: 'running',
    progress: 65,
    assignee: '运维人员',
    startTime: '2024-01-01 10:00:00',
    duration: '8分钟'
  },
  {
    id: 3,
    name: '邮件发送任务',
    status: 'failed',
    progress: 30,
    assignee: '邮件服务',
    startTime: '2024-01-01 11:00:00',
    duration: '2分钟'
  }
])

// 方法
const loadStats = async () => {
  try {
    // 模拟API调用
    await new Promise(resolve => setTimeout(resolve, 500))
    ElMessage.success('统计数据加载成功')
  } catch (error) {
    ElMessage.error('加载统计数据失败: ' + error.message)
  }
}

const loadTaskList = async () => {
  loading.value = true
  try {
    // 模拟API调用
    await new Promise(resolve => setTimeout(resolve, 1000))
    ElMessage.success('任务列表加载成功')
  } catch (error) {
    ElMessage.error('加载任务列表失败: ' + error.message)
  } finally {
    loading.value = false
  }
}

const refreshTasks = () => {
  loadTaskList()
}

const initCharts = () => {
  // 初始化趋势图
  trendChart = echarts.init(trendChartRef.value)
  const trendOption = {
    tooltip: {
      trigger: 'axis'
    },
    xAxis: {
      type: 'category',
      data: ['00:00', '04:00', '08:00', '12:00', '16:00', '20:00', '24:00']
    },
    yAxis: {
      type: 'value'
    },
    series: [
      {
        name: '成功',
        type: 'line',
        stack: '总量',
        areaStyle: {},
        data: [12, 18, 15, 22, 20, 18, 24]
      },
      {
        name: '失败',
        type: 'line',
        stack: '总量',
        areaStyle: {},
        data: [2, 1, 3, 2, 1, 2, 2]
      }
    ]
  }
  trendChart.setOption(trendOption)

  // 初始化状态分布图
  statusChart = echarts.init(statusChartRef.value)
  const statusOption = {
    tooltip: {
      trigger: 'item'
    },
    legend: {
      top: '5%',
      left: 'center'
    },
    series: [
      {
        name: '任务状态',
        type: 'pie',
        radius: ['40%', '70%'],
        avoidLabelOverlap: false,
        itemStyle: {
          borderRadius: 10,
          borderColor: '#fff',
          borderWidth: 2
        },
        label: {
          show: false,
          position: 'center'
        },
        emphasis: {
          label: {
            show: true,
            fontSize: '18',
            fontWeight: 'bold'
          }
        },
        labelLine: {
          show: false
        },
        data: [
          { value: 24, name: '成功' },
          { value: 3, name: '执行中' },
          { value: 2, name: '失败' },
          { value: 8, name: '待执行' }
        ]
      }
    ]
  }
  statusChart.setOption(statusOption)
}

// 状态相关方法
const getStatusTagType = (status) => {
  const statusMap = {
    success: 'success',
    running: 'primary',
    failed: 'danger',
    pending: 'warning'
  }
  return statusMap[status] || 'info'
}

const getStatusText = (status) => {
  const statusMap = {
    success: '成功',
    running: '执行中',
    failed: '失败',
    pending: '待执行'
  }
  return statusMap[status] || '未知'
}

const getProgressStatus = (status) => {
  const statusMap = {
    success: 'success',
    failed: 'exception'
  }
  return statusMap[status] || ''
}

// 生命周期
onMounted(() => {
  loadStats()
  loadTaskList()
  initCharts()
  
  // 监听窗口大小变化，重新调整图表大小
  window.addEventListener('resize', () => {
    if (trendChart) trendChart.resize()
    if (statusChart) statusChart.resize()
  })
})

onBeforeUnmount(() => {
  if (trendChart) trendChart.dispose()
  if (statusChart) statusChart.dispose()
})
</script>

<style scoped>
.task-monitor-container {
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

.monitor-section {
  margin-bottom: 20px;
}

.monitor-card {
  height: 120px;
}

.card-content {
  display: flex;
  align-items: center;
  height: 100%;
}

.card-icon {
  width: 60px;
  height: 60px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 20px;
  font-size: 24px;
  color: #fff;
}

.card-icon.success {
  background-color: #67c23a;
}

.card-icon.warning {
  background-color: #e6a23c;
}

.card-icon.danger {
  background-color: #f56c6c;
}

.card-icon.info {
  background-color: #409eff;
}

.card-info {
  flex: 1;
}

.card-title {
  font-size: 14px;
  color: #909399;
  margin-bottom: 8px;
}

.card-value {
  font-size: 24px;
  font-weight: bold;
  color: #303133;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.chart-container {
  height: 400px;
}
</style>
