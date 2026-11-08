<template>
  <div class="payment-statistics">
    <!-- 概览卡片 -->
    <el-row :gutter="20" class="overview-row">
      <el-col :span="6">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-content">
            <div class="stat-icon" style="background:#ecf5ff;color:#409EFF;">
              <el-icon :size="28"><Money /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-number">¥{{ formatMoney(overview.todayIncome) }}</div>
              <div class="stat-label">今日收入</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-content">
            <div class="stat-icon" style="background:#f0f9eb;color:#67C23A;">
              <el-icon :size="28"><TrendCharts /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-number">¥{{ formatMoney(overview.monthIncome) }}</div>
              <div class="stat-label">本月收入</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-content">
            <div class="stat-icon" style="background:#fdf6ec;color:#E6A23C;">
              <el-icon :size="28"><Document /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-number">{{ overview.todayOrders || 0 }}</div>
              <div class="stat-label">今日订单</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-content">
            <div class="stat-icon" style="background:#fef0f0;color:#F56C6C;">
              <el-icon :size="28"><CircleCheck /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-number">{{ overview.successRate || 0 }}%</div>
              <div class="stat-label">支付成功率</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 图表区域 -->
    <el-row :gutter="20" style="margin-top: 20px;">
      <el-col :span="16">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>收入趋势</span>
              <el-radio-group v-model="trendDays" size="small" @change="loadTrend">
                <el-radio-button :value="7">近7天</el-radio-button>
                <el-radio-button :value="15">近15天</el-radio-button>
                <el-radio-button :value="30">近30天</el-radio-button>
              </el-radio-group>
            </div>
          </template>
          <div ref="trendChartRef" class="chart-container"></div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>支付方式分布</span>
              <el-select v-model="distPeriod" size="small" style="width:100px;" @change="loadChannelDist">
                <el-option label="今日" value="today" />
                <el-option label="本月" value="month" />
                <el-option label="全部" value="all" />
              </el-select>
            </div>
          </template>
          <div ref="pieChartRef" class="chart-container"></div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 订单量趋势 -->
    <el-row :gutter="20" style="margin-top: 20px;">
      <el-col :span="24">
        <el-card>
          <template #header>
            <span>订单量趋势</span>
          </template>
          <div ref="orderChartRef" class="chart-container"></div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 商户 TOP10 排行 -->
    <el-row :gutter="20" style="margin-top: 20px;">
      <el-col :span="24">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>商户交易排行 TOP{{ rankLimit }}</span>
              <div>
                <el-select v-model="rankPeriod" size="small" style="width:100px;margin-right:8px;" @change="loadMerchantRank">
                  <el-option label="今日" value="today" />
                  <el-option label="本月" value="month" />
                  <el-option label="全部" value="all" />
                </el-select>
                <el-select v-model="rankLimit" size="small" style="width:90px;" @change="loadMerchantRank">
                  <el-option :value="5" label="TOP 5" />
                  <el-option :value="10" label="TOP 10" />
                  <el-option :value="20" label="TOP 20" />
                </el-select>
              </div>
            </div>
          </template>
          <div ref="merchantRankChartRef" class="chart-container chart-tall"></div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount, nextTick } from 'vue'
import { Money, TrendCharts, Document, CircleCheck } from '@element-plus/icons-vue'
import * as echarts from 'echarts'
import request from '@/utils/request.js'

// ========= 数据 =========
const overview = ref({
  todayIncome: 0,
  monthIncome: 0,
  todayOrders: 0,
  successRate: 0
})
const trendDays = ref(7)
const distPeriod = ref('month')
const rankPeriod = ref('month')
const rankLimit = ref(10)

// ========= 图表实例 =========
const trendChartRef = ref(null)
const pieChartRef = ref(null)
const orderChartRef = ref(null)
const merchantRankChartRef = ref(null)
let trendChart = null
let pieChart = null
let orderChart = null
let merchantRankChart = null

// ========= 工具函数 =========
/**
 * 格式化金额显示
 * @param {number} val 金额数值
 * @returns {string} 格式化后的金额字符串
 */
const formatMoney = (val) => {
  if (!val) return '0.00'
  const num = Number(val)
  if (num >= 10000) return (num / 10000).toFixed(2) + '万'
  return num.toFixed(2)
}

// ========= API 调用 =========
/**
 * 加载概览数据
 */
const loadOverview = async () => {
  try {
    const res = await request.post('/web/pay/stats/overview', {})
    if (res.code === 200 && res.data) {
      overview.value = res.data
    }
  } catch (e) {
    console.error('[Stats] overview 失败', e)
  }
}

/**
 * 加载趋势数据并渲染图表
 */
const loadTrend = async () => {
  try {
    const res = await request.post('/web/pay/stats/trend', { days: trendDays.value })
    if (res.code === 200 && res.data) {
      renderTrendChart(res.data)
      renderOrderChart(res.data)
    }
  } catch (e) {
    console.error('[Stats] trend 失败', e)
  }
}

/**
 * 加载渠道分布数据并渲染饼图
 */
const loadChannelDist = async () => {
  try {
    const res = await request.post('/web/pay/stats/channelDist', { period: distPeriod.value })
    if (res.code === 200 && res.data) {
      renderPieChart(res.data)
    }
  } catch (e) {
    console.error('[Stats] channelDist 失败', e)
  }
}

/**
 * 加载商户 TOP N 排行并渲染横向柱状图
 */
const loadMerchantRank = async () => {
  try {
    const res = await request.post('/web/pay/stats/merchantRank', {
      period: rankPeriod.value,
      limit: rankLimit.value
    })
    if (res.code === 200 && res.data) {
      renderMerchantRankChart(res.data)
    }
  } catch (e) {
    console.error('[Stats] merchantRank 失败', e)
  }
}

// ========= 图表渲染 =========
/**
 * 渲染收入趋势折线图
 * @param {Array} data 趋势数据数组
 */
const renderTrendChart = (data) => {
  if (!trendChart) {
    trendChart = echarts.init(trendChartRef.value)
  }
  const dates = data.map(d => d.date.substring(5)) // MM-DD
  const incomes = data.map(d => Number(d.income) || 0)
  trendChart.setOption({
    tooltip: { trigger: 'axis', formatter: '{b}<br/>收入: ¥{c}' },
    grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
    xAxis: { type: 'category', data: dates, boundaryGap: false },
    yAxis: { type: 'value', axisLabel: { formatter: v => v >= 10000 ? (v / 10000) + '万' : v } },
    series: [{
      name: '收入',
      type: 'line',
      smooth: true,
      areaStyle: { opacity: 0.15 },
      lineStyle: { width: 2 },
      itemStyle: { color: '#409EFF' },
      data: incomes
    }]
  })
}

/**
 * 渲染订单量柱状图
 * @param {Array} data 趋势数据数组
 */
const renderOrderChart = (data) => {
  if (!orderChart) {
    orderChart = echarts.init(orderChartRef.value)
  }
  const dates = data.map(d => d.date.substring(5))
  const orders = data.map(d => d.orders || 0)
  const successOrders = data.map(d => d.successOrders || 0)
  orderChart.setOption({
    tooltip: { trigger: 'axis' },
    legend: { data: ['总订单', '成功订单'] },
    grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
    xAxis: { type: 'category', data: dates },
    yAxis: { type: 'value' },
    series: [
      { name: '总订单', type: 'bar', barMaxWidth: 30, itemStyle: { color: '#409EFF' }, data: orders },
      { name: '成功订单', type: 'bar', barMaxWidth: 30, itemStyle: { color: '#67C23A' }, data: successOrders }
    ]
  })
}

/**
 * 渲染支付方式分布饼图
 * @param {Array} data 渠道分布数据
 */
const renderPieChart = (data) => {
  if (!pieChart) {
    pieChart = echarts.init(pieChartRef.value)
  }
  const pieData = data.map(d => ({ name: d.channelName, value: Number(d.amount) || 0 }))
  pieChart.setOption({
    tooltip: { trigger: 'item', formatter: '{b}: ¥{c} ({d}%)' },
    legend: { orient: 'vertical', left: 'left', top: 'middle' },
    series: [{
      type: 'pie',
      radius: ['40%', '70%'],
      center: ['60%', '50%'],
      avoidLabelOverlap: true,
      itemStyle: { borderRadius: 6, borderColor: '#fff', borderWidth: 2 },
      label: { show: false },
      emphasis: { label: { show: true, fontWeight: 'bold' } },
      data: pieData
    }]
  })
}

/**
 * 渲染商户排行横向柱状图
 * @param {Array} data 商户排行数据，每项 {merchantId, merchantName, amount, count}
 */
const renderMerchantRankChart = (data) => {
  if (!merchantRankChart) {
    merchantRankChart = echarts.init(merchantRankChartRef.value)
  }
  // 反转顺序，保证最高金额展示在顶部
  const sorted = [...data].reverse()
  const names = sorted.map(d => d.merchantName || d.merchantId || '未知')
  const amounts = sorted.map(d => Number(d.amount) || 0)
  const counts = sorted.map(d => Number(d.count) || 0)
  merchantRankChart.setOption({
    tooltip: {
      trigger: 'axis',
      axisPointer: { type: 'shadow' },
      formatter: (params) => {
        const idx = params[0].dataIndex
        const name = names[idx]
        const amount = amounts[idx]
        const count = counts[idx]
        return `${name}<br/>交易额: ¥${amount.toLocaleString()}<br/>笔数: ${count}`
      }
    },
    grid: { left: '3%', right: '6%', bottom: '3%', containLabel: true },
    xAxis: {
      type: 'value',
      axisLabel: { formatter: v => v >= 10000 ? (v / 10000) + '万' : v }
    },
    yAxis: {
      type: 'category',
      data: names,
      axisLabel: { fontSize: 12, width: 140, overflow: 'truncate' }
    },
    series: [{
      name: '交易额',
      type: 'bar',
      barMaxWidth: 22,
      itemStyle: {
        color: { type: 'linear', x: 0, y: 0, x2: 1, y2: 0,
          colorStops: [{ offset: 0, color: '#36CFC9' }, { offset: 1, color: '#409EFF' }] },
        borderRadius: [0, 6, 6, 0]
      },
      label: { show: true, position: 'right', formatter: ({ value }) => '¥' + (value >= 10000 ? (value / 10000).toFixed(2) + '万' : Number(value).toFixed(2)) },
      data: amounts
    }]
  })
}

// ========= 窗口自适应 =========
const handleResize = () => {
  trendChart && trendChart.resize()
  pieChart && pieChart.resize()
  orderChart && orderChart.resize()
  merchantRankChart && merchantRankChart.resize()
}

// ========= 生命周期 =========
onMounted(async () => {
  await nextTick()
  loadOverview()
  loadTrend()
  loadChannelDist()
  loadMerchantRank()
  window.addEventListener('resize', handleResize)
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', handleResize)
  trendChart && trendChart.dispose()
  pieChart && pieChart.dispose()
  orderChart && orderChart.dispose()
  merchantRankChart && merchantRankChart.dispose()
})
</script>

<style scoped>
.payment-statistics {
  padding: 20px;
}

.overview-row {
  margin-bottom: 0;
}

.stat-card {
  border-radius: 8px;
}

.stat-content {
  display: flex;
  align-items: center;
  padding: 10px 0;
}

.stat-icon {
  width: 56px;
  height: 56px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 16px;
}

.stat-info {
  flex: 1;
}

.stat-number {
  font-size: 22px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 4px;
}

.stat-label {
  font-size: 13px;
  color: #909399;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.chart-container {
  height: 320px;
  width: 100%;
}

.chart-tall {
  height: 460px;
}
</style>
