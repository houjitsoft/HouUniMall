<template>
  <div class="order-statistics-container">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-left">
        <h2>订单统计</h2>
        <span class="subtitle">订单数据统计与分析</span>
      </div>
      <div class="header-right">
        <el-button icon="Download" @click="handleExport">
          导出报表
        </el-button>
        <el-button icon="Refresh" @click="handleRefresh">
          刷新数据
        </el-button>
      </div>
    </div>

    <!-- 时间筛选 -->
    <el-card class="filter-card">
      <el-form :model="filterForm" inline>
        <el-form-item label="统计时间">
          <el-date-picker
            v-model="filterForm.dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            style="width: 300px"
            @change="handleDateChange"
          />
        </el-form-item>
        <el-form-item label="统计维度">
          <el-select 
            v-model="filterForm.dimension" 
            placeholder="选择维度"
            style="width: 120px"
            @change="handleDimensionChange"
          >
            <el-option label="按天" value="day" />
            <el-option label="按周" value="week" />
            <el-option label="按月" value="month" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleQuery">
            查询
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 核心指标卡片 -->
    <div class="metrics-cards">
      <el-row :gutter="16">
        <el-col :span="6">
          <el-card class="metric-card">
            <div class="metric-content">
              <div class="metric-value">{{ metrics.totalOrders }}</div>
              <div class="metric-label">订单总数</div>
              <div class="metric-trend">
                <i class="el-icon-arrow-up trend-up"></i>
                <span class="trend-value">+{{ metrics.orderGrowth }}%</span>
              </div>
            </div>
            <i class="el-icon-document metric-icon"></i>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card class="metric-card">
            <div class="metric-content">
              <div class="metric-value">¥{{ metrics.totalAmount }}</div>
              <div class="metric-label">订单金额</div>
              <div class="metric-trend">
                <i class="el-icon-arrow-up trend-up"></i>
                <span class="trend-value">+{{ metrics.amountGrowth }}%</span>
              </div>
            </div>
            <i class="el-icon-money metric-icon"></i>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card class="metric-card">
            <div class="metric-content">
              <div class="metric-value">¥{{ metrics.avgOrderValue }}</div>
              <div class="metric-label">客单价</div>
              <div class="metric-trend">
                <i class="el-icon-arrow-down trend-down"></i>
                <span class="trend-value">-{{ metrics.avgGrowth }}%</span>
              </div>
            </div>
            <i class="el-icon-user metric-icon"></i>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card class="metric-card">
            <div class="metric-content">
              <div class="metric-value">{{ metrics.completionRate }}%</div>
              <div class="metric-label">完成率</div>
              <div class="metric-trend">
                <i class="el-icon-arrow-up trend-up"></i>
                <span class="trend-value">+{{ metrics.rateGrowth }}%</span>
              </div>
            </div>
            <i class="el-icon-success metric-icon"></i>
          </el-card>
        </el-col>
      </el-row>
    </div>

    <!-- 图表区域 -->
    <div class="charts-section">
      <el-row :gutter="16">
        <!-- 订单趋势图 -->
        <el-col :span="12">
          <el-card class="chart-card">
            <template #header>
              <div class="card-header">
                <span>订单趋势</span>
                <el-button text @click="viewOrderTrend">详情</el-button>
              </div>
            </template>
            <div ref="orderTrendChart" class="chart-container"></div>
          </el-card>
        </el-col>
        
        <!-- 订单状态分布 -->
        <el-col :span="12">
          <el-card class="chart-card">
            <template #header>
              <div class="card-header">
                <span>订单状态分布</span>
                <el-button text @click="viewStatusDistribution">详情</el-button>
              </div>
            </template>
            <div ref="statusChart" class="chart-container"></div>
          </el-card>
        </el-col>
      </el-row>

      <el-row :gutter="16" style="margin-top: 16px">
        <!-- 地区分布 -->
        <el-col :span="12">
          <el-card class="chart-card">
            <template #header>
              <div class="card-header">
                <span>地区分布</span>
                <el-button text @click="viewRegionDistribution">详情</el-button>
              </div>
            </template>
            <div ref="regionChart" class="chart-container"></div>
          </el-card>
        </el-col>
        
        <!-- 支付方式分布 -->
        <el-col :span="12">
          <el-card class="chart-card">
            <template #header>
              <div class="card-header">
                <span>支付方式分布</span>
                <el-button text @click="viewPaymentDistribution">详情</el-button>
              </div>
            </template>
            <div ref="paymentChart" class="chart-container"></div>
          </el-card>
        </el-col>
      </el-row>
    </div>

    <!-- 详细数据表格 -->
    <el-card class="table-card">
      <template #header>
        <div class="card-header">
          <span>统计详情</span>
          <el-button text @click="handleViewDetails">
            查看更多
          </el-button>
        </div>
      </template>
      
      <el-table 
        v-loading="loading"
        :data="statisticsData" 
        style="width: 100%"
      >
        <el-table-column prop="date" label="日期" width="120" />
        <el-table-column prop="orderCount" label="订单数量" width="100" />
        <el-table-column label="订单金额" width="120">
          <template #default="{ row }">
            <span class="amount">¥{{ row.orderAmount }}</span>
          </template>
        </el-table-column>
        <el-table-column label="客单价" width="100">
          <template #default="{ row }">
            <span>¥{{ row.avgOrderValue }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="completedCount" label="完成订单" width="100" />
        <el-table-column prop="cancelledCount" label="取消订单" width="100" />
        <el-table-column label="完成率" width="100">
          <template #default="{ row }">
            <span>{{ row.completionRate }}%</span>
          </template>
        </el-table-column>
        <el-table-column label="环比增长" min-width="100">
          <template #default="{ row }">
            <span 
              :class="row.growth >= 0 ? 'growth-positive' : 'growth-negative'"
            >
              {{ row.growth >= 0 ? '+' : '' }}{{ row.growth }}%
            </span>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination-container">
        <el-pagination
          v-model:current-page="pagination.page"
          v-model:page-size="pagination.size"
          :page-sizes="[10, 20, 50, 100]"
          :total="pagination.total"
          layout="total, sizes, prev, pager, next, jumper"
        />
      </div>
    </el-card>
  </div>
</template>

<script setup>

// 使用 Element Plus 国际化系统
import { useI18n } from '@/i18n-element'
const { t } = useI18n()
import { ref, reactive, onMounted, nextTick } from 'vue'
import { ElMessage } from 'element-plus'

// 响应式数据
const loading = ref(false)

// 筛选表单
const filterForm = reactive({
  dateRange: [
    new Date(new Date().getTime() - 7 * 24 * 60 * 60 * 1000),
    new Date()
  ],
  dimension: 'day'
})

// 核心指标
const metrics = reactive({
  totalOrders: '15,680',
  orderGrowth: 12.5,
  totalAmount: '2,345,678',
  amountGrowth: 18.2,
  avgOrderValue: '149.6',
  avgGrowth: 3.2,
  completionRate: 92.8,
  rateGrowth: 2.1
})

// 分页信息
const pagination = reactive({
  page: 1,
  size: 20,
  total: 0
})

// 统计数据
const statisticsData = ref([
  {
    date: '2024-01-15',
    orderCount: 1230,
    orderAmount: '185,670.00',
    avgOrderValue: '150.87',
    completedCount: 1142,
    cancelledCount: 88,
    completionRate: 92.8,
    growth: 12.5
  },
  {
    date: '2024-01-14',
    orderCount: 1095,
    orderAmount: '164,250.00',
    avgOrderValue: '149.95',
    completedCount: 1018,
    cancelledCount: 77,
    completionRate: 93.0,
    growth: -2.3
  },
  {
    date: '2024-01-13',
    orderCount: 1121,
    orderAmount: '168,900.00',
    avgOrderValue: '150.67',
    completedCount: 1045,
    cancelledCount: 76,
    completionRate: 93.2,
    growth: 8.7
  }
])

// 图表引用
const orderTrendChart = ref(null)
const statusChart = ref(null)
const regionChart = ref(null)
const paymentChart = ref(null)

// 方法
const handleDateChange = () => {
  handleQuery()
}

const handleDimensionChange = () => {
  handleQuery()
}

const handleQuery = () => {
  loading.value = true
  // 模拟API调用
  setTimeout(() => {
    ElMessage.success('数据更新成功')
    initCharts()
    loading.value = false
  }, 1000)
}

const handleExport = () => {
  ElMessage.info('导出统计报表')
}

const handleRefresh = () => {
  handleQuery()
}

const viewOrderTrend = () => {
  ElMessage.info('查看订单趋势详情')
}

const viewStatusDistribution = () => {
  ElMessage.info('查看状态分布详情')
}

const viewRegionDistribution = () => {
  ElMessage.info('查看地区分布详情')
}

const viewPaymentDistribution = () => {
  ElMessage.info('查看支付方式详情')
}

const handleViewDetails = () => {
  ElMessage.info('查看统计详情')
}

// 初始化图表
const initCharts = () => {
  nextTick(() => {
    // 这里应该使用ECharts或其他图表库来初始化图表
    // 模拟图表初始化
    if (orderTrendChart.value) {
      orderTrendChart.value.innerHTML = '<div style="height: 300px; display: flex; align-items: center; justify-content: center; color: #909399;">订单趋势图表</div>'
    }
    if (statusChart.value) {
      statusChart.value.innerHTML = '<div style="height: 300px; display: flex; align-items: center; justify-content: center; color: #909399;">状态分布图表</div>'
    }
    if (regionChart.value) {
      regionChart.value.innerHTML = '<div style="height: 300px; display: flex; align-items: center; justify-content: center; color: #909399;">地区分布图表</div>'
    }
    if (paymentChart.value) {
      paymentChart.value.innerHTML = '<div style="height: 300px; display: flex; align-items: center; justify-content: center; color: #909399;">支付方式图表</div>'
    }
  })
}

// 初始化
onMounted(() => {
  handleQuery()
})
</script>

<style scoped>
.order-statistics-container {
  padding: 20px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.header-left h2 {
  margin: 0;
  color: #303133;
}

.subtitle {
  color: #909399;
  font-size: 14px;
}

.filter-card, .table-card {
  margin-bottom: 20px;
}

.metrics-cards {
  margin-bottom: 20px;
}

.metric-card {
  position: relative;
  overflow: hidden;
}

.metric-content {
  position: relative;
  z-index: 2;
}

.metric-value {
  font-size: 28px;
  font-weight: bold;
  color: #303133;
  margin-bottom: 8px;
}

.metric-label {
  color: #909399;
  font-size: 14px;
  margin-bottom: 8px;
}

.metric-trend {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
}

.trend-up {
  color: #67c23a;
}

.trend-down {
  color: #f56c6c;
}

.trend-value {
  font-weight: bold;
}

.metric-icon {
  position: absolute;
  right: 20px;
  top: 50%;
  transform: translateY(-50%);
  font-size: 40px;
  color: #E4E7ED;
  z-index: 1;
}

.charts-section {
  margin-bottom: 20px;
}

.chart-card {
  margin-bottom: 0;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.chart-container {
  height: 300px;
  width: 100%;
}

.amount {
  color: #f56c6c;
  font-weight: bold;
}

.growth-positive {
  color: #67c23a;
  font-weight: bold;
}

.growth-negative {
  color: #f56c6c;
  font-weight: bold;
}

.pagination-container {
  display: flex;
  justify-content: center;
  margin-top: 20px;
}
</style>
