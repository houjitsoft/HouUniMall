<template>
  <div class="monitor-performance-container">
    <!-- 页面标题 -->
    <div class="page-header">
      <h2>性能监控</h2>
      <p>实时监控系统性能指标</p>
    </div>

    <!-- 性能指标面板 -->
    <div class="metrics-section">
      <el-row :gutter="20">
        <el-col :span="6">
          <el-card class="metric-card">
            <div class="card-content">
              <div class="card-icon cpu">
                <el-icon><Cpu /></el-icon>
              </div>
              <div class="card-info">
                <div class="card-title">CPU使用率</div>
                <div class="card-value">{{ metrics.cpuUsage }}%</div>
              </div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card class="metric-card">
            <div class="card-content">
              <div class="card-icon memory">
                <el-icon><Monitor /></el-icon>
              </div>
              <div class="card-info">
                <div class="card-title">内存使用率</div>
                <div class="card-value">{{ metrics.memoryUsage }}%</div>
              </div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card class="metric-card">
            <div class="card-content">
              <div class="card-icon disk">
                <el-icon><Coin /></el-icon>
              </div>
              <div class="card-info">
                <div class="card-title">磁盘使用率</div>
                <div class="card-value">{{ metrics.diskUsage }}%</div>
              </div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card class="metric-card">
            <div class="card-content">
              <div class="card-icon network">
                <el-icon><Connection /></el-icon>
              </div>
              <div class="card-info">
                <div class="card-title">网络IO</div>
                <div class="card-value">{{ metrics.networkIO }} KB/s</div>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </div>

    <!-- 性能图表 -->
    <div class="chart-section">
      <el-row :gutter="20">
        <el-col :span="12">
          <el-card>
            <template #header>
              <div class="card-header">
                <span>CPU使用率趋势</span>
              </div>
            </template>
            <div ref="cpuChartRef" class="chart-container"></div>
          </el-card>
        </el-col>
        <el-col :span="12">
          <el-card>
            <template #header>
              <div class="card-header">
                <span>内存使用率趋势</span>
              </div>
            </template>
            <div ref="memoryChartRef" class="chart-container"></div>
          </el-card>
        </el-col>
      </el-row>
    </div>

    <!-- 服务状态 -->
    <div class="service-section">
      <el-card>
        <template #header>
          <div class="card-header">
            <span>服务状态</span>
          </div>
        </template>
        
        <el-table :data="serviceList" stripe border style="width: 100%">
          <el-table-column prop="name" label="服务名称" min-width="200" />
          <el-table-column prop="status" label="状态" width="120">
            <template #default="{ row }">
              <el-tag :type="getServiceStatusType(row.status)">
                {{ getServiceStatusText(row.status) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="cpu" label="CPU占用" width="120" />
          <el-table-column prop="memory" label="内存占用" width="120" />
          <el-table-column prop="startTime" label="启动时间" width="180" />
          <el-table-column prop="uptime" label="运行时长" width="120" />
        </el-table>
      </el-card>
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
  Cpu, Monitor, Coin, Connection 
} from '@element-plus/icons-vue'
import * as echarts from 'echarts'

// 数据
const cpuChartRef = ref()
const memoryChartRef = ref()
let cpuChart = null
let memoryChart = null

const metrics = reactive({
  cpuUsage: 45,
  memoryUsage: 65,
  diskUsage: 30,
  networkIO: 128
})

const serviceList = ref([
  {
    name: '用户服务',
    status: 'running',
    cpu: '15%',
    memory: '256MB',
    startTime: '2024-01-01 09:00:00',
    uptime: '2天3小时'
  },
  {
    name: '订单服务',
    status: 'running',
    cpu: '20%',
    memory: '512MB',
    startTime: '2024-01-01 10:00:00',
    uptime: '2天2小时'
  },
  {
    name: '支付服务',
    status: 'stopped',
    cpu: '0%',
    memory: '0MB',
    startTime: '2024-01-01 11:00:00',
    uptime: '0天0小时'
  }
])

// 方法
const loadMetrics = async () => {
  try {
    // 模拟API调用
    await new Promise(resolve => setTimeout(resolve, 500))
    ElMessage.success('性能数据加载成功')
  } catch (error) {
    ElMessage.error('加载性能数据失败: ' + error.message)
  }
}

const initCharts = () => {
  // 初始化CPU图表
  cpuChart = echarts.init(cpuChartRef.value)
  const cpuOption = {
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
        name: 'CPU使用率',
        type: 'line',
        data: [30, 45, 50, 40, 60, 55, 45],
        areaStyle: {}
      }
    ]
  }
  cpuChart.setOption(cpuOption)

  // 初始化内存图表
  memoryChart = echarts.init(memoryChartRef.value)
  const memoryOption = {
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
        name: '内存使用率',
        type: 'line',
        data: [50, 55, 60, 65, 70, 68, 65],
        areaStyle: {}
      }
    ]
  }
  memoryChart.setOption(memoryOption)
}

// 状态相关方法
const getServiceStatusType = (status) => {
  const statusMap = {
    running: 'success',
    stopped: 'danger',
    warning: 'warning'
  }
  return statusMap[status] || 'info'
}

const getServiceStatusText = (status) => {
  const statusMap = {
    running: '运行中',
    stopped: '已停止',
    warning: '警告'
  }
  return statusMap[status] || '未知'
}

// 生命周期
onMounted(() => {
  loadMetrics()
  initCharts()
  
  // 监听窗口大小变化，重新调整图表大小
  window.addEventListener('resize', () => {
    if (cpuChart) cpuChart.resize()
    if (memoryChart) memoryChart.resize()
  })
})

onBeforeUnmount(() => {
  if (cpuChart) cpuChart.dispose()
  if (memoryChart) memoryChart.dispose()
})
</script>

<style scoped>
.monitor-performance-container {
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

.metrics-section {
  margin-bottom: 20px;
}

.metric-card {
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

.card-icon.cpu {
  background-color: #409eff;
}

.card-icon.memory {
  background-color: #67c23a;
}

.card-icon.disk {
  background-color: #e6a23c;
}

.card-icon.network {
  background-color: #909399;
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
