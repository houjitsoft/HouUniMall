<template>
  <div class="overview-page">
    <!-- 核心指标卡片 -->
    <el-row :gutter="20" class="metrics-row">
      <el-col :xs="12" :sm="12" :md="6" :lg="6">
        <el-card shadow="hover" class="metric-card">
          <div class="metric-content">
            <div class="metric-icon" style="background: linear-gradient(135deg, #667eea 0%, #764ba2 100%)">
              <el-icon :size="24"><ShoppingCart /></el-icon>
            </div>
            <div class="metric-info">
              <div class="metric-label">总订单数</div>
              <div class="metric-value">{{ overview.totalOrders }}</div>
              <div class="metric-trend">
                <span :class="['trend-text', overview.ordersTrend >= 0 ? 'up' : 'down']">
                  <el-icon><component :is="overview.ordersTrend >= 0 ? Top : Bottom" /></el-icon>
                  {{ Math.abs(overview.ordersTrend) }}%
                </span>
                <span class="trend-label">较上月</span>
              </div>
            </div>
          </div>
        </el-card>
      </el-col>
      
      <el-col :xs="12" :sm="12" :md="6" :lg="6">
        <el-card shadow="hover" class="metric-card">
          <div class="metric-content">
            <div class="metric-icon" style="background: linear-gradient(135deg, #5ad8a6 0%, #3ab881 100%)">
              <el-icon :size="24"><Money /></el-icon>
            </div>
            <div class="metric-info">
              <div class="metric-label">总营业额</div>
              <div class="metric-value">¥{{ formatNumber(overview.totalRevenue) }}</div>
              <div class="metric-trend">
                <span :class="['trend-text', overview.revenueTrend >= 0 ? 'up' : 'down']">
                  <el-icon><component :is="overview.revenueTrend >= 0 ? Top : Bottom" /></el-icon>
                  {{ Math.abs(overview.revenueTrend) }}%
                </span>
                <span class="trend-label">较上月</span>
              </div>
            </div>
          </div>
        </el-card>
      </el-col>
      
      <el-col :xs="12" :sm="12" :md="6" :lg="6">
        <el-card shadow="hover" class="metric-card">
          <div class="metric-content">
            <div class="metric-icon" style="background: linear-gradient(135deg, #ff9845 0%, #ff7320 100%)">
              <el-icon :size="24"><User /></el-icon>
            </div>
            <div class="metric-info">
              <div class="metric-label">活跃用户</div>
              <div class="metric-value">{{ formatNumber(overview.activeUsers) }}</div>
              <div class="metric-trend">
                <span :class="['trend-text', overview.usersTrend >= 0 ? 'up' : 'down']">
                  <el-icon><component :is="overview.usersTrend >= 0 ? Top : Bottom" /></el-icon>
                  {{ Math.abs(overview.usersTrend) }}%
                </span>
                <span class="trend-label">较上月</span>
              </div>
            </div>
          </div>
        </el-card>
      </el-col>
      
      <el-col :xs="12" :sm="12" :md="6" :lg="6">
        <el-card shadow="hover" class="metric-card">
          <div class="metric-content">
            <div class="metric-icon" style="background: linear-gradient(135deg, #5b8ff9 0%, #3a6fcc 100%)">
              <el-icon :size="24"><Goods /></el-icon>
            </div>
            <div class="metric-info">
              <div class="metric-label">在售商品</div>
              <div class="metric-value">{{ formatNumber(overview.onlineGoods) }}</div>
              <div class="metric-trend">
                <span :class="['trend-text', overview.goodsTrend >= 0 ? 'up' : 'down']">
                  <el-icon><component :is="overview.goodsTrend >= 0 ? Top : Bottom" /></el-icon>
                  {{ Math.abs(overview.goodsTrend) }}%
                </span>
                <span class="trend-label">较上月</span>
              </div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 新增核心指标卡片（根据设计图） -->
    <el-row :gutter="20" style="margin-top: 20px">
      <el-col :xs="12" :sm="12" :md="6" :lg="6">
        <el-card shadow="hover" class="new-metric-card">
          <div class="new-metric-header">
            <span class="new-metric-title">成交订单(笔)</span>
            <el-tag size="small" type="info">今日</el-tag>
          </div>
          <div class="new-metric-main">
            <div class="new-metric-number">{{ todayMetrics.orders }}</div>
            <div class="new-metric-compare">
              <span>昨日: {{ todayMetrics.ordersYesterday }}</span>
              <span :class="['compare-rate', todayMetrics.ordersRate >= 0 ? 'up' : 'down']">
                {{ todayMetrics.ordersRate >= 0 ? '↑' : '↓' }} {{ Math.abs(todayMetrics.ordersRate).toFixed(2) }}
              </span>
            </div>
          </div>
          <div class="new-metric-footer">
            <span>累计成交订单</span>
            <span class="footer-value">{{ totalMetrics.orders }}</span>
          </div>
        </el-card>
      </el-col>
      
      <el-col :xs="12" :sm="12" :md="6" :lg="6">
        <el-card shadow="hover" class="new-metric-card">
          <div class="new-metric-header">
            <span class="new-metric-title">营业额(元)</span>
            <el-tag size="small" type="info">今日</el-tag>
          </div>
          <div class="new-metric-main">
            <div class="new-metric-number">{{ todayMetrics.revenue }}</div>
            <div class="new-metric-compare">
              <span>昨日: {{ todayMetrics.revenueYesterday }}</span>
              <span :class="['compare-rate', todayMetrics.revenueRate >= 0 ? 'up' : 'down']">
                {{ todayMetrics.revenueRate >= 0 ? '↑' : '↓' }} {{ Math.abs(todayMetrics.revenueRate).toFixed(2) }}
              </span>
            </div>
          </div>
          <div class="new-metric-footer">
            <span>累计营业额</span>
            <span class="footer-value">{{ totalMetrics.revenue }}</span>
          </div>
        </el-card>
      </el-col>
      
      <el-col :xs="12" :sm="12" :md="6" :lg="6">
        <el-card shadow="hover" class="new-metric-card">
          <div class="new-metric-header">
            <span class="new-metric-title">新增会员(人)</span>
            <el-tag size="small" type="info">今日</el-tag>
          </div>
          <div class="new-metric-main">
            <div class="new-metric-number">{{ todayMetrics.newMembers }}</div>
            <div class="new-metric-compare">
              <span>昨日: {{ todayMetrics.newMembersYesterday }}</span>
              <span :class="['compare-rate', todayMetrics.newMembersRate >= 0 ? 'up' : 'down']">
                {{ todayMetrics.newMembersRate >= 0 ? '↑' : '↓' }} {{ Math.abs(todayMetrics.newMembersRate).toFixed(2) }}
              </span>
            </div>
          </div>
          <div class="new-metric-footer">
            <span>会员总人数</span>
            <span class="footer-value">{{ totalMetrics.members }}</span>
          </div>
        </el-card>
      </el-col>
      
      <el-col :xs="12" :sm="12" :md="6" :lg="6">
        <el-card shadow="hover" class="new-metric-card">
          <div class="new-metric-header">
            <span class="new-metric-title">会员访问量(人)</span>
            <el-tag size="small" type="info">今日</el-tag>
          </div>
          <div class="new-metric-main">
            <div class="new-metric-number">{{ todayMetrics.visits }}</div>
            <div class="new-metric-compare">
              <span>昨日: {{ todayMetrics.visitsYesterday }}</span>
              <span :class="['compare-rate', todayMetrics.visitsRate >= 0 ? 'up' : 'down']">
                {{ todayMetrics.visitsRate >= 0 ? '↑' : '↓' }} {{ Math.abs(todayMetrics.visitsRate).toFixed(2) }}
              </span>
            </div>
          </div>
          <div class="new-metric-footer">
            <span>累计会员访问量</span>
            <span class="footer-value">{{ totalMetrics.visits }}</span>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 商家排行 -->
    <el-row :gutter="20" style="margin-top: 20px">
      <el-col :xs="24" :sm="24" :md="12" :lg="12">
        <el-card shadow="hover">
          <template #header>
            <span class="header-title">销冠商家(前5名)</span>
          </template>
          <el-table :data="topMerchants" :show-header="true" style="width: 100%">
            <el-table-column label="排名" width="80" align="center">
              <template #default="{ $index }">
                {{ $index + 1 }}
              </template>
            </el-table-column>
            <el-table-column label="商家名称" prop="name">
              <template #default="{ row }">
                <div style="display: flex; align-items: center; gap: 8px">
                  <el-avatar :size="32" :src="row.logo" />
                  <span>{{ row.name }}</span>
                </div>
              </template>
            </el-table-column>
            <el-table-column label="销售金额(元)" prop="sales" align="right" width="150" />
          </el-table>
        </el-card>
      </el-col>
      
      <el-col :xs="24" :sm="24" :md="12" :lg="12">
        <el-card shadow="hover">
          <template #header>
            <span class="header-title">人气商家(前5名)</span>
          </template>
          <el-table :data="popularMerchants" :show-header="true" style="width: 100%">
            <el-table-column label="排名" width="80" align="center">
              <template #default="{ $index }">
                {{ $index + 1 }}
              </template>
            </el-table-column>
            <el-table-column label="商家名称" prop="name">
              <template #default="{ row }">
                <div style="display: flex; align-items: center; gap: 8px">
                  <el-avatar :size="32" :src="row.logo" />
                  <span>{{ row.name }}</span>
                </div>
              </template>
            </el-table-column>
            <el-table-column label="进店人数" prop="visits" align="right" width="150" />
          </el-table>
        </el-card>
      </el-col>
    </el-row>

    <!-- 近15天会员访问量趋势 -->
    <el-row :gutter="20" style="margin-top: 20px">
      <el-col :span="24">
        <el-card shadow="hover">
          <template #header>
            <span class="header-title">近15天会员访问量(人)</span>
          </template>
          <div ref="visitTrendChartRef" class="chart-container"></div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 详细数据表格 -->
    <el-row :gutter="20" style="margin-top: 20px">
      <el-col :xs="24" :sm="24" :md="12" :lg="12">
        <el-card shadow="hover">
          <template #header>
            <div class="card-header">
              <span class="header-title">运营数据明细</span>
              <el-radio-group v-model="timeRange" size="small">
                <el-radio-button label="today">今日</el-radio-button>
                <el-radio-button label="week">本周</el-radio-button>
                <el-radio-button label="month">本月</el-radio-button>
              </el-radio-group>
            </div>
          </template>
          <el-table :data="detailData" stripe>
            <el-table-column prop="metric" label="指标" width="150" />
            <el-table-column prop="value" label="数值" width="120" />
            <el-table-column prop="trend" label="趋势" width="100">
              <template #default="{ row }">
                <el-tag :type="row.trendType" size="small">{{ row.trend }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="description" label="说明" />
          </el-table>
        </el-card>
      </el-col>
      
      <el-col :xs="24" :sm="24" :md="12" :lg="12">
        <el-card shadow="hover">
          <template #header>
            <span class="header-title">分类销售排行</span>
          </template>
          <el-table :data="categoryRanking" stripe>
            <el-table-column type="index" label="排名" width="60" />
            <el-table-column prop="category" label="分类" />
            <el-table-column prop="sales" label="销量" width="100" />
            <el-table-column prop="amount" label="销售额" width="120">
              <template #default="{ row }">
                <span style="color: #ff9845; font-weight: 600">¥{{ row.amount }}</span>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
    </el-row>

    <!-- 分析图表 -->
    <el-row :gutter="20" style="margin-top: 20px">
      <el-col :span="24">
        <el-card shadow="hover">
          <template #header>
            <span class="header-title">营业额趋势分析</span>
          </template>
          <div ref="trendChartRef" class="chart-container"></div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onBeforeUnmount, watch, nextTick } from 'vue'
import { ShoppingCart, Money, User, Goods, Top, Bottom } from '@element-plus/icons-vue'
import * as echarts from 'echarts'
import { getPlaceholderDataUri } from '@/utils/placeholder'

const timeRange = ref('today')
const trendChartRef = ref()
const visitTrendChartRef = ref() // 新增：会员访问量图表
let trendChart = null
let visitTrendChart = null // 新增：会员访问量图表实例

const overview = reactive({
  totalOrders: 12580,
  ordersTrend: 15.8,
  totalRevenue: 3680000,
  revenueTrend: 22.5,
  activeUsers: 8650,
  usersTrend: 12.3,
  onlineGoods: 2340,
  goodsTrend: -3.2
})

const detailData = ref([
  { metric: '访问量(PV)', value: '125,680', trend: '+15.2%', trendType: 'success', description: '页面浏览总次数' },
  { metric: '访客数(UV)', value: '58,320', trend: '+12.8%', trendType: 'success', description: '独立访客数量' },
  { metric: '转化率', value: '3.6%', trend: '+0.5%', trendType: 'success', description: '订单转化比例' },
  { metric: '客单价', value: '¥286', trend: '-5.2%', trendType: 'danger', description: '平均订单金额' },
  { metric: '退款率', value: '2.3%', trend: '-0.8%', trendType: 'success', description: '退款订单占比' }
])

const categoryRanking = ref([
  { category: '数码电器', sales: 2580, amount: '1,258,000' },
  { category: '服饰鞋帽', sales: 1980, amount: '896,000' },
  { category: '食品生鲜', sales: 1650, amount: '568,000' },
  { category: '家居用品', sales: 1320, amount: '456,000' },
  { category: '美妆护肤', sales: 980, amount: '328,000' }
])

// 新增：今日数据
const todayMetrics = reactive({
  orders: 0,
  ordersYesterday: 0,
  ordersRate: 0.00,
  revenue: 0,
  revenueYesterday: 0,
  revenueRate: 0.00,
  newMembers: 0,
  newMembersYesterday: 0,
  newMembersRate: 0.00,
  visits: 0,
  visitsYesterday: 0,
  visitsRate: 0.00
})

// 新增：累计数据
const totalMetrics = reactive({
  orders: 6,
  revenue: 1372.03,
  members: 2,
  visits: 27
})

// 新增：销冠商家数据
const topMerchants = ref([
  { name: 'Nike', logo: getPlaceholderDataUri(32), sales: '118200' },
  { name: '迪奥', logo: getPlaceholderDataUri(32), sales: '19000' },
  { name: 'apple', logo: getPlaceholderDataUri(32), sales: '0' }
])

// 新增：人气商家数据
const popularMerchants = ref([
  { name: 'Nike', logo: getPlaceholderDataUri(32), visits: '0' },
  { name: 'apple', logo: getPlaceholderDataUri(32), visits: '0' },
  { name: '迪奥', logo: getPlaceholderDataUri(32), visits: '0' }
])

const formatNumber = (num) => {
  return num.toLocaleString('zh-CN')
}

const initTrendChart = () => {
  if (!trendChartRef.value) return
  
  trendChart = echarts.init(trendChartRef.value)
  const option = {
    tooltip: {
      trigger: 'axis',
      axisPointer: { type: 'cross' }
    },
    legend: {
      data: ['营业额', '订单数'],
      bottom: 0
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '10%',
      top: '3%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      boundaryGap: false,
      data: ['1月', '2月', '3月', '4月', '5月', '6月', '7月', '8月', '9月', '10月', '11月', '12月']
    },
    yAxis: [
      {
        type: 'value',
        name: '营业额(万元)',
        position: 'left',
        axisLine: { show: true, lineStyle: { color: '#5b8ff9' } }
      },
      {
        type: 'value',
        name: '订单数',
        position: 'right',
        axisLine: { show: true, lineStyle: { color: '#5ad8a6' } }
      }
    ],
    series: [
      {
        name: '营业额',
        type: 'line',
        smooth: true,
        yAxisIndex: 0,
        data: [280, 320, 350, 380, 420, 450, 480, 520, 560, 600, 650, 680],
        itemStyle: { color: '#5b8ff9' },
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(91, 143, 249, 0.3)' },
            { offset: 1, color: 'rgba(91, 143, 249, 0.05)' }
          ])
        }
      },
      {
        name: '订单数',
        type: 'line',
        smooth: true,
        yAxisIndex: 1,
        data: [980, 1120, 1250, 1350, 1480, 1620, 1750, 1880, 2020, 2150, 2280, 2420],
        itemStyle: { color: '#5ad8a6' },
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(90, 216, 166, 0.3)' },
            { offset: 1, color: 'rgba(90, 216, 166, 0.05)' }
          ])
        }
      }
    ]
  }
  trendChart.setOption(option)
}

const handleResize = () => {
  trendChart?.resize()
  visitTrendChart?.resize() // 新增：会员访问量图表响应式
}

// 新增：初始化会员访问量趋势图
const initVisitTrendChart = () => {
  if (!visitTrendChartRef.value) return
  
  visitTrendChart = echarts.init(visitTrendChartRef.value)
  
  // 生成15天的日期
  const dates = []
  const today = new Date()
  for (let i = 14; i >= 0; i--) {
    const date = new Date(today)
    date.setDate(date.getDate() - i)
    dates.push(`${date.getFullYear()}${String(date.getMonth() + 1).padStart(2, '0')}${String(date.getDate()).padStart(2, '0')}`)
  }
  
  const option = {
    tooltip: {
      trigger: 'axis',
      axisPointer: { type: 'line' }
    },
    legend: {
      data: ['访问量'],
      bottom: 0
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '10%',
      top: '5%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      boundaryGap: false,
      data: dates,
      axisLabel: {
        formatter: function(value) {
          return value.substring(4, 6) + '/' + value.substring(6, 8)
        }
      }
    },
    yAxis: {
      type: 'value',
      min: 0,
      max: 1,
      interval: 0.2,
      axisLabel: {
        formatter: '{value}'
      }
    },
    series: [
      {
        name: '访问量',
        type: 'line',
        smooth: true,
        data: Array(15).fill(0), // 默认全部0
        itemStyle: { color: '#5b8ff9' },
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(91, 143, 249, 0.3)' },
            { offset: 1, color: 'rgba(91, 143, 249, 0.05)' }
          ])
        },
        symbol: 'circle',
        symbolSize: 6
      }
    ]
  }
  visitTrendChart.setOption(option)
}

// 监听时间范围变化
watch(timeRange, (newVal) => {
  // 根据时间范围更新数据
  console.log('时间范围变更为:', newVal)
  // 这里可以调用API获取对应时间范围的数据
})

onMounted(async () => {
  await nextTick()
  initTrendChart()
  initVisitTrendChart() // 新增：初始化会员访问量图表
  window.addEventListener('resize', handleResize)
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', handleResize)
  trendChart?.dispose()
  visitTrendChart?.dispose() // 新增：销毁会员访问量图表
})
</script>

<style scoped>
.overview-page {
  padding: 20px;
  background: #f5f7fa;
  min-height: 100vh;
}

.metrics-row {
  margin-bottom: 20px;
}

.metric-card {
  border-radius: 12px;
  transition: all 0.3s;
}

.metric-card:hover {
  transform: translateY(-4px);
}

.metric-content {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 8px 0;
}

.metric-icon {
  width: 56px;
  height: 56px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  flex-shrink: 0;
}

.metric-info {
  flex: 1;
  min-width: 0;
}

.metric-label {
  font-size: 14px;
  color: #666;
  margin-bottom: 8px;
}

.metric-value {
  font-size: 26px;
  font-weight: bold;
  color: #333;
  margin-bottom: 8px;
  font-variant-numeric: tabular-nums;
  word-break: break-all;
  line-height: 1.15;
}

.metric-trend {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 12px;
}

.trend-text {
  display: inline-flex;
  align-items: center;
  gap: 2px;
  font-weight: 600;
}

.trend-text.up {
  color: #67c23a;
}

.trend-text.down {
  color: #f56c6c;
}

.trend-label {
  color: #999;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-title {
  font-weight: 600;
  color: #333;
  font-size: 16px;
}

.chart-container {
  height: 360px;
  width: 100%;
}

/* 新增：新指标卡片样式 */
.new-metric-card {
  border-radius: 8px;
  transition: all 0.3s;
}

.new-metric-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0,0,0,0.1);
}

.new-metric-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.new-metric-title {
  font-size: 14px;
  color: #666;
  font-weight: 500;
}

.new-metric-main {
  margin-bottom: 16px;
}

.new-metric-number {
  font-size: 32px;
  font-weight: bold;
  color: #333;
  line-height: 1.2;
  margin-bottom: 8px;
}

.new-metric-compare {
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 12px;
  color: #999;
}

.new-metric-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-top: 12px;
  border-top: 1px solid #eee;
  font-size: 12px;
  color: #999;
  gap: 8px;
}

.footer-value {
  font-variant-numeric: tabular-nums;
  word-break: break-all;
  text-align: right;
  flex-shrink: 1;
  min-width: 0;
}

@media (max-width: 768px) {
  .overview-page {
    padding: 10px;
    min-height: auto;
  }

  .metrics-row {
    margin-bottom: 8px;
  }

  .metric-card :deep(.el-card__body) {
    padding: 12px;
  }

  .metric-content {
    flex-direction: column;
    align-items: flex-start;
    gap: 10px;
    padding: 0;
  }

  .metric-icon {
    width: 44px;
    height: 44px;
  }

  .metric-label {
    font-size: 12px;
    margin-bottom: 4px;
  }

  .metric-value {
    font-size: clamp(15px, 4.2vw, 20px);
    margin-bottom: 4px;
  }

  .metric-trend {
    flex-wrap: wrap;
    gap: 4px;
    font-size: 11px;
  }

  .new-metric-card :deep(.el-card__body) {
    padding: 12px;
  }

  .new-metric-number {
    font-size: clamp(20px, 5vw, 28px);
  }

  .new-metric-compare {
    flex-direction: column;
    align-items: flex-start;
    gap: 4px;
  }

  .new-metric-title {
    font-size: 12px;
  }

  .card-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 8px;
  }
  
  .chart-container {
    height: 240px;
  }
}
</style>
