<template>
  <div class="order-statistics-container">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-left">
        <h2>订单统计</h2>
        <span class="subtitle">多维度分析订单数据，为经营决策提供数据支持</span>
      </div>
      <div class="header-right">
        <el-date-picker
          v-model="dateRange"
          type="daterange"
          range-separator="至"
          start-placeholder="开始日期"
          end-placeholder="结束日期"
          @change="handleDateChange"
          style="width: 280px; margin-right: 10px"
        />
        <el-button type="primary" icon="Download" @click="handleExport">
          导出报表
        </el-button>
        <el-button icon="Refresh" @click="handleRefresh">
          刷新
        </el-button>
      </div>
    </div>

    <!-- 核心指标卡片 -->
    <div class="stats-overview">
      <el-row :gutter="20">
        <el-col :span="6">
          <el-card class="stat-card" shadow="hover">
            <div class="stat-content">
              <div class="stat-icon" style="background: linear-gradient(135deg, #667eea 0%, #764ba2 100%)">
                <el-icon :size="32"><ShoppingCart /></el-icon>
              </div>
              <div class="stat-info">
                <div class="stat-value">{{ overview.totalOrders }}</div>
                <div class="stat-label">订单总数</div>
                <div class="stat-trend" :class="overview.ordersTrend >= 0 ? 'up' : 'down'">
                  <el-icon><component :is="overview.ordersTrend >= 0 ? 'Top' : 'Bottom'" /></el-icon>
                  {{ Math.abs(overview.ordersTrend) }}%
                </div>
              </div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card class="stat-card" shadow="hover">
            <div class="stat-content">
              <div class="stat-icon" style="background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%)">
                <el-icon :size="32"><Money /></el-icon>
              </div>
              <div class="stat-info">
                <div class="stat-value">¥{{ formatNumber(overview.totalAmount) }}</div>
                <div class="stat-label">订单总额</div>
                <div class="stat-trend" :class="overview.amountTrend >= 0 ? 'up' : 'down'">
                  <el-icon><component :is="overview.amountTrend >= 0 ? 'Top' : 'Bottom'" /></el-icon>
                  {{ Math.abs(overview.amountTrend) }}%
                </div>
              </div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card class="stat-card" shadow="hover">
            <div class="stat-content">
              <div class="stat-icon" style="background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%)">
                <el-icon :size="32"><User /></el-icon>
              </div>
              <div class="stat-info">
                <div class="stat-value">{{ overview.totalCustomers }}</div>
                <div class="stat-label">客户数量</div>
                <div class="stat-trend" :class="overview.customersTrend >= 0 ? 'up' : 'down'">
                  <el-icon><component :is="overview.customersTrend >= 0 ? 'Top' : 'Bottom'" /></el-icon>
                  {{ Math.abs(overview.customersTrend) }}%
                </div>
              </div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card class="stat-card" shadow="hover">
            <div class="stat-content">
              <div class="stat-icon" style="background: linear-gradient(135deg, #fa709a 0%, #fee140 100%)">
                <el-icon :size="32"><TrendCharts /></el-icon>
              </div>
              <div class="stat-info">
                <div class="stat-value">¥{{ formatNumber(overview.avgOrderAmount) }}</div>
                <div class="stat-label">客单价</div>
                <div class="stat-trend" :class="overview.avgTrend >= 0 ? 'up' : 'down'">
                  <el-icon><component :is="overview.avgTrend >= 0 ? 'Top' : 'Bottom'" /></el-icon>
                  {{ Math.abs(overview.avgTrend) }}%
                </div>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </div>

    <!-- 趋势图表 -->
    <el-row :gutter="20">
      <el-col :span="16">
        <el-card class="chart-card">
          <template #header>
            <div class="card-header">
              <span>订单趋势分析</span>
              <el-radio-group v-model="trendType" size="small" @change="handleTrendTypeChange">
                <el-radio-button label="day">按天</el-radio-button>
                <el-radio-button label="week">按周</el-radio-button>
                <el-radio-button label="month">按月</el-radio-button>
              </el-radio-group>
            </div>
          </template>
          <div ref="trendChartRef" style="height: 400px"></div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card class="chart-card">
          <template #header>
            <span>订单状态分布</span>
          </template>
          <div ref="statusChartRef" style="height: 400px"></div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 商品排行 -->
    <el-row :gutter="20" style="margin-top: 20px">
      <el-col :span="12">
        <el-card class="rank-card">
          <template #header>
            <div class="card-header">
              <span>热销商品TOP10</span>
              <el-link type="primary" @click="viewMoreGoods">查看更多</el-link>
            </div>
          </template>
          <el-table :data="topGoods" style="width: 100%">
            <el-table-column type="index" label="排名" width="60" />
            <el-table-column prop="goodsName" label="商品名称" show-overflow-tooltip />
            <el-table-column prop="salesCount" label="销量" width="100">
              <template #default="{ row }">
                <el-tag size="small">{{ row.salesCount }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="salesAmount" label="销售额" width="120">
              <template #default="{ row }">
                <span class="amount">¥{{ formatNumber(row.salesAmount) }}</span>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card class="rank-card">
          <template #header>
            <div class="card-header">
              <span>高价值客户TOP10</span>
              <el-link type="primary" @click="viewMoreCustomers">查看更多</el-link>
            </div>
          </template>
          <el-table :data="topCustomers" style="width: 100%">
            <el-table-column type="index" label="排名" width="60" />
            <el-table-column prop="customerName" label="客户名称" show-overflow-tooltip />
            <el-table-column prop="orderCount" label="订单数" width="100">
              <template #default="{ row }">
                <el-tag size="small" type="success">{{ row.orderCount }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="totalAmount" label="消费总额" width="120">
              <template #default="{ row }">
                <span class="amount">¥{{ formatNumber(row.totalAmount) }}</span>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, nextTick } from 'vue'
import { ElMessage } from 'element-plus'
import { 
  ShoppingCart, 
  Money, 
  User, 
  TrendCharts,
  Top,
  Bottom,
  Download,
  Refresh
} from '@element-plus/icons-vue'
import * as echarts from 'echarts'
import request from '@/utils/request'

// 日期范围
const dateRange = ref([])

// 概览数据
const overview = reactive({
  totalOrders: 0,
  ordersTrend: 0,
  totalAmount: 0,
  amountTrend: 0,
  totalCustomers: 0,
  customersTrend: 0,
  avgOrderAmount: 0,
  avgTrend: 0
})

// 趋势类型
const trendType = ref('day')

// 图表引用
const trendChartRef = ref(null)
const statusChartRef = ref(null)
let trendChart = null
let statusChart = null

// TOP数据
const topGoods = ref([])
const topCustomers = ref([])

// 获取概览数据
const getOverviewData = async () => {
  try {
    const response = await request.post('/web/order/statistics/overview', {
      startDate: dateRange.value?.[0] || '',
      endDate: dateRange.value?.[1] || ''
    })
    
    if (response.code === 200) {
      Object.assign(overview, response.data)
    }
  } catch (error) {
    console.error('获取概览数据失败:', error)
  }
}

// 获取趋势数据
const getTrendData = async () => {
  try {
    const response = await request.post('/web/order/statistics/trend', {
      type: trendType.value,
      startDate: dateRange.value?.[0] || '',
      endDate: dateRange.value?.[1] || ''
    })
    
    if (response.code === 200) {
      renderTrendChart(response.data)
    }
  } catch (error) {
    console.error('获取趋势数据失败:', error)
  }
}

// 获取状态分布
const getStatusData = async () => {
  try {
    const response = await request.post('/web/order/statistics/status', {
      startDate: dateRange.value?.[0] || '',
      endDate: dateRange.value?.[1] || ''
    })
    
    if (response.code === 200) {
      renderStatusChart(response.data)
    }
  } catch (error) {
    console.error('获取状态分布失败:', error)
  }
}

// 获取TOP数据
const getTopData = async () => {
  try {
    const response = await request.post('/web/order/statistics/top', {
      startDate: dateRange.value?.[0] || '',
      endDate: dateRange.value?.[1] || ''
    })
    
    if (response.code === 200) {
      topGoods.value = response.data.topGoods || []
      topCustomers.value = response.data.topCustomers || []
    }
  } catch (error) {
    console.error('获取TOP数据失败:', error)
  }
}

// 渲染趋势图
const renderTrendChart = (data) => {
  if (!trendChart) {
    trendChart = echarts.init(trendChartRef.value)
  }
  
  const option = {
    tooltip: {
      trigger: 'axis'
    },
    legend: {
      data: ['订单数量', '订单金额']
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      data: data.dates || []
    },
    yAxis: [
      {
        type: 'value',
        name: '订单数量'
      },
      {
        type: 'value',
        name: '订单金额'
      }
    ],
    series: [
      {
        name: '订单数量',
        type: 'line',
        data: data.orderCounts || [],
        smooth: true
      },
      {
        name: '订单金额',
        type: 'line',
        yAxisIndex: 1,
        data: data.orderAmounts || [],
        smooth: true
      }
    ]
  }
  
  trendChart.setOption(option)
}

// 渲染状态图
const renderStatusChart = (data) => {
  if (!statusChart) {
    statusChart = echarts.init(statusChartRef.value)
  }
  
  const option = {
    tooltip: {
      trigger: 'item'
    },
    legend: {
      orient: 'vertical',
      left: 'left'
    },
    series: [
      {
        name: '订单状态',
        type: 'pie',
        radius: '50%',
        data: data || [],
        emphasis: {
          itemStyle: {
            shadowBlur: 10,
            shadowOffsetX: 0,
            shadowColor: 'rgba(0, 0, 0, 0.5)'
          }
        }
      }
    ]
  }
  
  statusChart.setOption(option)
}

// 格式化数字
const formatNumber = (num) => {
  if (!num) return '0.00'
  return Number(num).toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })
}

// 日期变化
const handleDateChange = () => {
  loadAllData()
}

// 趋势类型变化
const handleTrendTypeChange = () => {
  getTrendData()
}

// 刷新
const handleRefresh = () => {
  loadAllData()
  ElMessage.success('刷新成功')
}

// 导出
const handleExport = () => {
  ElMessage.info('导出功能开发中...')
}

// 查看更多商品
const viewMoreGoods = () => {
  ElMessage.info('跳转到商品分析页面')
}

// 查看更多客户
const viewMoreCustomers = () => {
  ElMessage.info('跳转到客户分析页面')
}

// 加载所有数据
const loadAllData = () => {
  getOverviewData()
  getTrendData()
  getStatusData()
  getTopData()
}

// 初始化
onMounted(() => {
  // 设置默认日期为最近30天
  const end = new Date()
  const start = new Date()
  start.setDate(start.getDate() - 30)
  dateRange.value = [start, end]
  
  nextTick(() => {
    loadAllData()
  })
})
</script>

<style scoped lang="scss">
.order-statistics-container {
  padding: 20px;
  background: #f5f7fa;
  min-height: calc(100vh - 60px);
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;

  .header-left {
    h2 {
      font-size: 24px;
      font-weight: 600;
      color: #303133;
      margin: 0 0 8px 0;
    }

    .subtitle {
      font-size: 14px;
      color: #909399;
    }
  }

  .header-right {
    display: flex;
    align-items: center;
    gap: 10px;
  }
}

.stats-overview {
  margin-bottom: 20px;

  .stat-card {
    :deep(.el-card__body) {
      padding: 20px;
    }

    .stat-content {
      display: flex;
      align-items: center;
      gap: 20px;

      .stat-icon {
        width: 70px;
        height: 70px;
        border-radius: 12px;
        display: flex;
        align-items: center;
        justify-content: center;
        color: white;
      }

      .stat-info {
        flex: 1;

        .stat-value {
          font-size: 28px;
          font-weight: bold;
          color: #303133;
          margin-bottom: 5px;
        }

        .stat-label {
          font-size: 14px;
          color: #909399;
          margin-bottom: 5px;
        }

        .stat-trend {
          font-size: 12px;
          display: flex;
          align-items: center;
          gap: 3px;

          &.up {
            color: #67C23A;
          }

          &.down {
            color: #F56C6C;
          }
        }
      }
    }
  }
}

.chart-card {
  margin-bottom: 20px;

  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
  }
}

.rank-card {
  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
  }

  .amount {
    color: #F56C6C;
    font-weight: 600;
  }
}
</style>
