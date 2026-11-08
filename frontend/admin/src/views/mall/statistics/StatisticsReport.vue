<template>
  <div class="page-container">
    <!-- 统计筛选条件 -->
    <el-card shadow="never" class="search-card">
      <el-form :inline="true" :model="searchForm" class="hc-form-stack">
        <el-form-item label="统计维度">
          <el-select v-model="searchForm.dimension" placeholder="请选择" style="width: 150px" @change="handleSearch">
            <el-option label="按日统计" value="day" />
            <el-option label="按周统计" value="week" />
            <el-option label="按月统计" value="month" />
            <el-option label="按年统计" value="year" />
          </el-select>
        </el-form-item>
        <el-form-item label="时间范围">
          <el-date-picker
            v-model="searchForm.dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            style="width: 260px"
            @change="handleSearch"
          />
        </el-form-item>
        <el-form-item label="统计类型">
          <el-select v-model="searchForm.type" placeholder="全部" clearable style="width: 150px" @change="handleSearch">
            <el-option label="全部" value="" />
            <el-option label="销售统计" value="sales" />
            <el-option label="订单统计" value="order" />
            <el-option label="客户统计" value="customer" />
            <el-option label="商品统计" value="goods" />
            <el-option label="库存统计" value="inventory" />
            <el-option label="财务统计" value="finance" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">
            <el-icon><Search /></el-icon>查询
          </el-button>
          <el-button @click="handleReset">
            <el-icon><Refresh /></el-icon>重置
          </el-button>
          <el-button type="success" @click="handleExport">
            <el-icon><Download /></el-icon>导出报表
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 统计概览 -->
    <el-row :gutter="20" class="stats-row">
      <el-col :xs="12" :sm="12" :md="6" :span="6">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-content">
            <div class="stat-icon" style="background: linear-gradient(135deg, #667eea 0%, #764ba2 100%)">
              <el-icon :size="32"><ShoppingCart /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-label">总销售额</div>
              <div class="stat-value">¥{{ formatNumber(overview.totalSales) }}</div>
              <div class="stat-desc">
                较上期 
                <span :class="overview.salesGrowth >= 0 ? 'text-success' : 'text-danger'">
                  {{ overview.salesGrowth >= 0 ? '+' : '' }}{{ overview.salesGrowth }}%
                </span>
              </div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :xs="12" :sm="12" :md="6" :span="6">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-content">
            <div class="stat-icon" style="background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%)">
              <el-icon :size="32"><DocumentCopy /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-label">总订单数</div>
              <div class="stat-value">{{ formatNumber(overview.totalOrders) }}</div>
              <div class="stat-desc">
                较上期 
                <span :class="overview.ordersGrowth >= 0 ? 'text-success' : 'text-danger'">
                  {{ overview.ordersGrowth >= 0 ? '+' : '' }}{{ overview.ordersGrowth }}%
                </span>
              </div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :xs="12" :sm="12" :md="6" :span="6">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-content">
            <div class="stat-icon" style="background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%)">
              <el-icon :size="32"><User /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-label">新增客户</div>
              <div class="stat-value">{{ formatNumber(overview.newCustomers) }}</div>
              <div class="stat-desc">
                较上期 
                <span :class="overview.customersGrowth >= 0 ? 'text-success' : 'text-danger'">
                  {{ overview.customersGrowth >= 0 ? '+' : '' }}{{ overview.customersGrowth }}%
                </span>
              </div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :xs="12" :sm="12" :md="6" :span="6">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-content">
            <div class="stat-icon" style="background: linear-gradient(135deg, #43e97b 0%, #38f9d7 100%)">
              <el-icon :size="32"><TrendCharts /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-label">客单价</div>
              <div class="stat-value">¥{{ overview.avgOrderAmount }}</div>
              <div class="stat-desc">
                较上期 
                <span :class="overview.avgGrowth >= 0 ? 'text-success' : 'text-danger'">
                  {{ overview.avgGrowth >= 0 ? '+' : '' }}{{ overview.avgGrowth }}%
                </span>
              </div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 趋势图表 -->
    <el-card shadow="never" class="chart-card">
      <template #header>
        <div class="card-header">
          <span class="card-title">销售趋势分析</span>
          <el-radio-group v-model="chartType" size="small" @change="handleChartChange">
            <el-radio-button value="sales">销售额</el-radio-button>
            <el-radio-button value="orders">订单数</el-radio-button>
            <el-radio-button value="customers">客户数</el-radio-button>
          </el-radio-group>
        </div>
      </template>
      <div ref="chartRef" class="chart-container"></div>
    </el-card>

    <!-- 统计明细表格 -->
    <el-card shadow="never">
      <template #header>
        <span class="card-title">统计明细</span>
      </template>
      <div class="hc-table-wrap">
      <el-table :data="tableData" border stripe v-loading="loading">
        <el-table-column prop="date" label="日期" width="120" />
        <el-table-column prop="salesAmount" label="销售额(元)" width="140" align="right">
          <template #default="{ row }">
            <span class="price">¥{{ formatNumber(row.salesAmount) }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="orderCount" label="订单数" width="100" align="right" />
        <el-table-column prop="customerCount" label="客户数" width="100" align="right" />
        <el-table-column prop="goodsCount" label="商品数" width="100" align="right" />
        <el-table-column prop="avgOrderAmount" label="客单价(元)" width="120" align="right">
          <template #default="{ row }">
            <span class="price">¥{{ row.avgOrderAmount }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="conversionRate" label="转化率" width="100" align="right">
          <template #default="{ row }">
            <el-tag :type="row.conversionRate >= 10 ? 'success' : row.conversionRate >= 5 ? '' : 'warning'">
              {{ row.conversionRate }}%
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="refundAmount" label="退款金额(元)" width="140" align="right">
          <template #default="{ row }">
            <span class="text-danger">¥{{ formatNumber(row.refundAmount) }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="refundRate" label="退款率" width="100" align="right">
          <template #default="{ row }">
            <el-tag :type="row.refundRate <= 2 ? 'success' : row.refundRate <= 5 ? 'warning' : 'danger'">
              {{ row.refundRate }}%
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="100" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="handleViewDetail(row)">查看详情</el-button>
          </template>
        </el-table-column>
      </el-table>
      </div>
      <el-pagination
        v-model:current-page="pagination.page"
        v-model:page-size="pagination.size"
        :total="pagination.total"
        :page-sizes="[10, 20, 50, 100]"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="fetchData"
        @current-change="fetchData"
        class="pagination"
      />
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onUnmounted, nextTick } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Refresh, Download, ShoppingCart, DocumentCopy, User, TrendCharts } from '@element-plus/icons-vue'
import * as echarts from 'echarts'
import { getReportOverview, getReportDetail, getSalesTrend, exportReport } from '@/api/mall/statistics'

const loading = ref(false)
const chartRef = ref(null)
let chartInstance = null
const chartType = ref('sales')

// 搜索表单
const searchForm = reactive({
  dimension: 'day',
  dateRange: [],
  type: ''
})

// 概览数据
const overview = reactive({
  totalSales: 0,
  salesGrowth: 0,
  totalOrders: 0,
  ordersGrowth: 0,
  newCustomers: 0,
  customersGrowth: 0,
  avgOrderAmount: 0,
  avgGrowth: 0
})

// 表格数据
const tableData = ref([])

// 分页
const pagination = reactive({
  page: 1,
  size: 20,
  total: 0
})

// 趋势数据
const trendData = ref({
  dates: [],
  sales: [],
  orders: [],
  customers: []
})

// 格式化数字
const formatNumber = (num) => {
  if (!num) return '0'
  return num.toString().replace(/\B(?=(\d{3})+(?!\d))/g, ',')
}

// 初始化图表
const initChart = () => {
  if (!chartRef.value) return
  
  chartInstance = echarts.init(chartRef.value)
  updateChart()
}

// 更新图表
const updateChart = () => {
  if (!chartInstance) return
  
  const typeMap = {
    sales: { data: trendData.value.sales, name: '销售额', unit: '元' },
    orders: { data: trendData.value.orders, name: '订单数', unit: '单' },
    customers: { data: trendData.value.customers, name: '客户数', unit: '人' }
  }
  
  const current = typeMap[chartType.value]
  
  const option = {
    tooltip: {
      trigger: 'axis',
      formatter: (params) => {
        return `${params[0].name}<br/>${params[0].marker}${current.name}: ${formatNumber(params[0].value)} ${current.unit}`
      }
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      boundaryGap: false,
      data: trendData.value.dates
    },
    yAxis: {
      type: 'value',
      axisLabel: {
        formatter: (value) => formatNumber(value)
      }
    },
    series: [
      {
        name: current.name,
        type: 'line',
        smooth: true,
        data: current.data,
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(102, 126, 234, 0.6)' },
            { offset: 1, color: 'rgba(102, 126, 234, 0.1)' }
          ])
        },
        itemStyle: {
          color: '#667eea'
        }
      }
    ]
  }
  
  chartInstance.setOption(option)
}

// 获取概览数据
const fetchOverview = async () => {
  try {
    const params = {
      dimension: searchForm.dimension,
      startDate: searchForm.dateRange && searchForm.dateRange.length > 0 ? searchForm.dateRange[0] : null,
      endDate: searchForm.dateRange && searchForm.dateRange.length > 1 ? searchForm.dateRange[1] : null,
      type: searchForm.type
    }
    
    const res = await getReportOverview(params)
    
    if (res && res.code === 1) {
      Object.assign(overview, res.data)
    }
  } catch (error) {
    console.error('获取概览数据失败:', error)
  }
}

// 获取趋势数据
const fetchTrend = async () => {
  try {
    const params = {
      dimension: searchForm.dimension,
      startDate: searchForm.dateRange && searchForm.dateRange.length > 0 ? searchForm.dateRange[0] : null,
      endDate: searchForm.dateRange && searchForm.dateRange.length > 1 ? searchForm.dateRange[1] : null,
      type: chartType.value
    }
    
    const res = await getSalesTrend(params)
    
    if (res && res.code === 1 && res.data) {
      trendData.value = res.data
      updateChart()
    }
  } catch (error) {
    console.error('获取趋势数据失败:', error)
  }
}

// 获取明细数据
const fetchData = async () => {
  loading.value = true
  try {
    const params = {
      page: pagination.page,
      limit: pagination.size,
      dimension: searchForm.dimension,
      startDate: searchForm.dateRange && searchForm.dateRange.length > 0 ? searchForm.dateRange[0] : null,
      endDate: searchForm.dateRange && searchForm.dateRange.length > 1 ? searchForm.dateRange[1] : null,
      type: searchForm.type
    }
    
    const res = await getReportDetail(params)
    
    if (res && res.code === 1) {
      const data = res.data
      if (data && data.list && Array.isArray(data.list)) {
        tableData.value = data.list
        pagination.total = data.totalCount || 0
      } else {
        tableData.value = []
        pagination.total = 0
      }
    }
  } catch (error) {
    console.error('获取统计明细失败:', error)
    ElMessage.error(error.message || '获取统计明细失败')
    tableData.value = []
  } finally {
    loading.value = false
  }
}

// 查询
const handleSearch = () => {
  pagination.page = 1
  fetchOverview()
  fetchTrend()
  fetchData()
}

// 重置
const handleReset = () => {
  searchForm.dimension = 'day'
  searchForm.dateRange = []
  searchForm.type = ''
  handleSearch()
}

// 切换图表类型
const handleChartChange = () => {
  fetchTrend()
}

// 导出报表
const handleExport = async () => {
  try {
    ElMessage.info('正在导出报表，请稍候...')
    
    const params = {
      dimension: searchForm.dimension,
      startDate: searchForm.dateRange && searchForm.dateRange.length > 0 ? searchForm.dateRange[0] : null,
      endDate: searchForm.dateRange && searchForm.dateRange.length > 1 ? searchForm.dateRange[1] : null,
      type: searchForm.type
    }
    
    const res = await exportReport(params)
    
    if (res && res.code === 1) {
      ElMessage.success('报表导出成功')
      // TODO: 下载文件
    } else {
      ElMessage.error(res.message || '导出失败')
    }
  } catch (error) {
    console.error('导出报表失败:', error)
    ElMessage.error('导出报表失败')
  }
}

// 查看详情
const handleViewDetail = (row) => {
  ElMessage.info(`查看 ${row.date} 的详细统计`)
  // TODO: 打开详情弹窗或跳转详情页
}

onMounted(() => {
  fetchOverview()
  fetchData()
  nextTick(() => {
    initChart()
    fetchTrend()
  })
  
  // 监听窗口大小变化
  window.addEventListener('resize', () => {
    chartInstance?.resize()
  })
})

onUnmounted(() => {
  chartInstance?.dispose()
  window.removeEventListener('resize', () => {})
})
</script>

<style scoped>
.page-container {
  padding: 20px;
}

.search-card {
  margin-bottom: 20px;
}

.stats-row {
  margin-bottom: 20px;
}

.stat-card {
  height: 120px;
}

.stat-content {
  display: flex;
  align-items: center;
  height: 100%;
}

.stat-icon {
  width: 80px;
  height: 80px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  margin-right: 20px;
}

.stat-info {
  flex: 1;
}

.stat-label {
  font-size: 14px;
  color: #909399;
  margin-bottom: 8px;
}

.stat-value {
  font-size: 28px;
  font-weight: bold;
  color: #303133;
  margin-bottom: 5px;
}

.stat-desc {
  font-size: 12px;
  color: #909399;
}

.text-success {
  color: #67c23a;
}

.text-danger {
  color: #f56c6c;
}

.chart-card {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-title {
  font-weight: bold;
  font-size: 16px;
}

.price {
  color: #f56c6c;
  font-weight: bold;
}

.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

.chart-container {
  width: 100%;
  height: 400px;
}

@media (max-width: 992px) {
  .page-container {
    padding: 12px;
  }

  .search-card,
  .chart-card {
    margin-bottom: 12px;
  }

  .chart-container {
    height: 260px;
  }

  .pagination {
    justify-content: center;
  }
}
</style>
