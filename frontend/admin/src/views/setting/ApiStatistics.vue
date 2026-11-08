<template>
  <div class="api-statistics-container">
    <!-- 页面标题 -->
    <div class="page-header">
      <h2>API统计</h2>
      <p>查看API调用统计和性能分析</p>
    </div>

    <!-- 统计面板 -->
    <div class="statistics-section">
      <el-row :gutter="20">
        <el-col :span="6">
          <el-card class="stat-card">
            <div class="card-content">
              <div class="card-icon primary">
                <el-icon><DataAnalysis /></el-icon>
              </div>
              <div class="card-info">
                <div class="card-title">总调用次数</div>
                <div class="card-value">{{ stats.totalCalls }}</div>
              </div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card class="stat-card">
            <div class="card-content">
              <div class="card-icon success">
                <el-icon><SuccessFilled /></el-icon>
              </div>
              <div class="card-info">
                <div class="card-title">成功调用</div>
                <div class="card-value">{{ stats.successCalls }}</div>
              </div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card class="stat-card">
            <div class="card-content">
              <div class="card-icon warning">
                <el-icon><Warning /></el-icon>
              </div>
              <div class="card-info">
                <div class="card-title">错误调用</div>
                <div class="card-value">{{ stats.errorCalls }}</div>
              </div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card class="stat-card">
            <div class="card-content">
              <div class="card-icon info">
                <el-icon><TrendCharts /></el-icon>
              </div>
              <div class="card-info">
                <div class="card-title">平均响应时间</div>
                <div class="card-value">{{ stats.avgResponseTime }}ms</div>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </div>

    <!-- 图表区域 -->
    <div class="chart-section">
      <el-row :gutter="20">
        <el-col :span="12">
          <el-card>
            <template #header>
              <div class="card-header">
                <span>调用趋势</span>
              </div>
            </template>
            <div ref="trendChartRef" class="chart-container"></div>
          </el-card>
        </el-col>
        <el-col :span="12">
          <el-card>
            <template #header>
              <div class="card-header">
                <span>接口调用排行</span>
              </div>
            </template>
            <div ref="rankingChartRef" class="chart-container"></div>
          </el-card>
        </el-col>
      </el-row>
    </div>

    <!-- 详细统计 -->
    <div class="detail-section">
      <el-card>
        <template #header>
          <div class="card-header">
            <span>详细统计</span>
            <el-date-picker
              v-model="dateRange"
              type="daterange"
              range-separator="至"
              start-placeholder="开始日期"
              end-placeholder="结束日期"
              format="YYYY-MM-DD"
              value-format="YYYY-MM-DD"
              @change="handleDateChange"
            />
          </div>
        </template>
        
        <el-table :data="detailStats" stripe border style="width: 100%">
          <el-table-column prop="apiName" label="接口名称" min-width="200" />
          <el-table-column prop="path" label="接口路径" min-width="200" />
          <el-table-column prop="method" label="请求方法" width="100">
            <template #default="{ row }">
              <el-tag :type="getMethodTagType(row.method)">
                {{ row.method }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="callCount" label="调用次数" width="120" />
          <el-table-column prop="successCount" label="成功次数" width="120" />
          <el-table-column prop="errorCount" label="错误次数" width="120" />
          <el-table-column prop="avgTime" label="平均耗时(ms)" width="120" />
          <el-table-column prop="maxTime" label="最大耗时(ms)" width="120" />
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
  DataAnalysis, SuccessFilled, Warning, TrendCharts 
} from '@element-plus/icons-vue'
import * as echarts from 'echarts'

// 数据
const trendChartRef = ref()
const rankingChartRef = ref()
let trendChart = null
let rankingChart = null

const dateRange = ref([])
const stats = reactive({
  totalCalls: 12840,
  successCalls: 12560,
  errorCalls: 280,
  avgResponseTime: 125
})

const detailStats = ref([
  {
    apiName: '用户列表接口',
    path: '/api/users',
    method: 'GET',
    callCount: 3240,
    successCount: 3200,
    errorCount: 40,
    avgTime: 85,
    maxTime: 320
  },
  {
    apiName: '创建用户接口',
    path: '/api/users',
    method: 'POST',
    callCount: 1850,
    successCount: 1840,
    errorCount: 10,
    avgTime: 150,
    maxTime: 420
  },
  {
    apiName: '订单列表接口',
    path: '/api/orders',
    method: 'GET',
    callCount: 2100,
    successCount: 2080,
    errorCount: 20,
    avgTime: 120,
    maxTime: 380
  },
  {
    apiName: '创建订单接口',
    path: '/api/orders',
    method: 'POST',
    callCount: 1650,
    successCount: 1630,
    errorCount: 20,
    avgTime: 210,
    maxTime: 520
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

const initCharts = () => {
  // 初始化趋势图
  trendChart = echarts.init(trendChartRef.value)
  const trendOption = {
    tooltip: {
      trigger: 'axis'
    },
    xAxis: {
      type: 'category',
      data: ['周一', '周二', '周三', '周四', '周五', '周六', '周日']
    },
    yAxis: {
      type: 'value'
    },
    series: [
      {
        name: '总调用',
        type: 'line',
        stack: '总量',
        areaStyle: {},
        data: [1200, 1800, 1500, 2200, 2000, 1800, 2400]
      },
      {
        name: '成功调用',
        type: 'line',
        stack: '总量',
        areaStyle: {},
        data: [1180, 1750, 1480, 2150, 1980, 1780, 2380]
      },
      {
        name: '错误调用',
        type: 'line',
        stack: '总量',
        areaStyle: {},
        data: [20, 50, 20, 50, 20, 20, 20]
      }
    ]
  }
  trendChart.setOption(trendOption)

  // 初始化排行图
  rankingChart = echarts.init(rankingChartRef.value)
  const rankingOption = {
    tooltip: {
      trigger: 'axis',
      axisPointer: {
        type: 'shadow'
      }
    },
    xAxis: {
      type: 'value'
    },
    yAxis: {
      type: 'category',
      data: ['用户列表', '订单列表', '产品列表', '创建用户', '创建订单']
    },
    series: [
      {
        name: '调用次数',
        type: 'bar',
        data: [3240, 2100, 1800, 1850, 1650]
      }
    ]
  }
  rankingChart.setOption(rankingOption)
}

const handleDateChange = () => {
  // 处理日期变化
  ElMessage.info('日期范围已更新')
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

// 生命周期
onMounted(() => {
  loadStats()
  initCharts()
  
  // 监听窗口大小变化，重新调整图表大小
  window.addEventListener('resize', () => {
    if (trendChart) trendChart.resize()
    if (rankingChart) rankingChart.resize()
  })
})

onBeforeUnmount(() => {
  if (trendChart) trendChart.dispose()
  if (rankingChart) rankingChart.dispose()
})
</script>

<style scoped>
.api-statistics-container {
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

.statistics-section {
  margin-bottom: 20px;
}

.stat-card {
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

.card-icon.primary {
  background-color: #409eff;
}

.card-icon.success {
  background-color: #67c23a;
}

.card-icon.warning {
  background-color: #e6a23c;
}

.card-icon.info {
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
