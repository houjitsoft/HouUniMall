<template>
  <div class="mall-dashboard">
    <!-- 欢迎区域 -->
    <div class="welcome-banner">
      <div class="welcome-content">
        <h2 class="welcome-title">
          <el-icon><TrendCharts /></el-icon>
          商城运营数据中心
        </h2>
        <p class="welcome-desc">实时监控商城运营情况，掌握关键业务指标</p>
      </div>
      <div class="welcome-time">
        <el-icon><Clock /></el-icon>
        <span>{{ currentTime }}</span>
      </div>
    </div>

    <!-- 统计卡片 -->
    <el-row :gutter="20" class="stat-cards">
      <el-col :xs="12" :sm="12" :md="6" :lg="6">
        <div class="stat-card stat-card-blue" @click="handleCardClick('sales')">
          <div class="stat-bg-chart"></div>
          <div class="stat-content">
            <div class="stat-icon">
              <el-icon><Wallet /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-title">年交易额</div>
              <div class="stat-value">¥{{ formatNumber(dashboardData.yearAmount) }}</div>
              <div class="stat-sub">
                <span>日交易额：¥{{ formatNumber(dashboardData.dayAmount) }}</span>
                <span class="trend up" v-if="dashboardData.salesTrend > 0">
                  <el-icon><Top /></el-icon>{{ dashboardData.salesTrend }}%
                </span>
              </div>
            </div>
          </div>
        </div>
      </el-col>
      <el-col :xs="12" :sm="12" :md="6" :lg="6">
        <div class="stat-card stat-card-green" @click="handleCardClick('users')">
          <div class="stat-bg-chart"></div>
          <div class="stat-content">
            <div class="stat-icon">
              <el-icon><User /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-title">用户量</div>
              <div class="stat-value">{{ formatNumber(dashboardData.userCount) }}</div>
              <div class="stat-sub">
                <span>日用户量：{{ dashboardData.dayUserCount }}</span>
                <span class="trend down" v-if="dashboardData.userTrend < 0">
                  <el-icon><Bottom /></el-icon>{{ Math.abs(dashboardData.userTrend) }}%
                </span>
              </div>
            </div>
          </div>
        </div>
      </el-col>
      <el-col :xs="12" :sm="12" :md="6" :lg="6">
        <div class="stat-card stat-card-cyan" @click="handleCardClick('shops')">
          <div class="stat-bg-chart"></div>
          <div class="stat-content">
            <div class="stat-icon">
              <el-icon><ShoppingBag /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-title">店铺量</div>
              <div class="stat-value">{{ formatNumber(dashboardData.shopCount) }}</div>
              <div class="stat-sub">
                <span>日店铺量：{{ dashboardData.dayShopCount }}</span>
              </div>
            </div>
          </div>
        </div>
      </el-col>
      <el-col :xs="12" :sm="12" :md="6" :lg="6">
        <div class="stat-card stat-card-orange" @click="handleCardClick('goods')">
          <div class="stat-bg-chart"></div>
          <div class="stat-content">
            <div class="stat-icon">
              <el-icon><Goods /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-title">商品量</div>
              <div class="stat-value">{{ formatNumber(dashboardData.goodsCount) }}</div>
              <div class="stat-sub">
                <span>日商品量：{{ dashboardData.dayGoodsCount }}</span>
              </div>
            </div>
          </div>
        </div>
      </el-col>
    </el-row>

    <!-- 待办事项 -->
    <div class="todo-section">
      <div class="section-header">
        <div class="header-left">
          <div class="header-icon">
            <el-icon><Flag /></el-icon>
          </div>
          <h3 class="section-title">待办事项</h3>
          <span class="section-badge">{{ totalTodoCount }}</span>
        </div>
        <el-button text type="primary" @click="viewAllTodos">
          查看全部 <el-icon><ArrowRight /></el-icon>
        </el-button>
      </div>
      <el-row :gutter="20" class="todo-cards">
        <el-col :xs="24" :sm="8" :md="8" :lg="8">
          <div class="todo-card" @click="handleTodoClick('goods')">
            <div class="todo-icon" style="background: linear-gradient(135deg, #667eea 0%, #764ba2 100%)">
              <el-icon size="24"><Document /></el-icon>
            </div>
            <div class="todo-info">
              <div class="todo-title">待审核商品</div>
              <div class="todo-count">{{ todoData.pendingGoods }}</div>
              <div class="todo-action">立即处理 <el-icon><ArrowRight /></el-icon></div>
            </div>
          </div>
        </el-col>
        <el-col :xs="24" :sm="8" :md="8" :lg="8">
          <div class="todo-card" @click="handleTodoClick('intervention')">
            <div class="todo-icon" style="background: linear-gradient(135deg, #5ad8a6 0%, #3ab881 100%)">
              <el-icon size="24"><Service /></el-icon>
            </div>
            <div class="todo-info">
              <div class="todo-title">待处理平台介入</div>
              <div class="todo-count">{{ todoData.pendingIntervention }}</div>
              <div class="todo-action">立即处理 <el-icon><ArrowRight /></el-icon></div>
            </div>
          </div>
        </el-col>
        <el-col :xs="24" :sm="8" :md="8" :lg="8">
          <div class="todo-card" @click="handleTodoClick('invoice')">
            <div class="todo-icon" style="background: linear-gradient(135deg, #ff9845 0%, #ff7320 100%)">
              <el-icon size="24"><Tickets /></el-icon>
            </div>
            <div class="todo-info">
              <div class="todo-title">待处理系统退回发票</div>
              <div class="todo-count">{{ todoData.pendingInvoice }}</div>
              <div class="todo-action">立即处理 <el-icon><ArrowRight /></el-icon></div>
            </div>
          </div>
        </el-col>
      </el-row>
    </div>

    <!-- 图表和数据 -->
    <el-row :gutter="20">
      <!-- 商品数据趋势 -->
      <el-col :xs="24" :sm="24" :md="12" :lg="12">
        <el-card class="chart-card" shadow="hover">
          <template #header>
            <div class="card-header">
              <div class="header-left">
                <el-icon class="header-icon" color="#5b8ff9"><TrendCharts /></el-icon>
                <span class="header-title">商品数据趋势</span>
              </div>
              <div class="header-tabs">
                <span 
                  v-for="tab in ['商品排名', '店铺排名']" 
                  :key="tab"
                  :class="['tab-item', { active: activeTab === tab }]"
                  @click="switchTab(tab)"
                >
                  {{ tab }}
                </span>
              </div>
            </div>
          </template>
          <div class="chart-content">
            <div class="trend-header">
              <span class="trend-title">商品数趋势</span>
              <span class="trend-desc">(每月新增商品数)</span>
            </div>
            <div ref="goodsTrendChartRef" class="chart-wrapper"></div>
          </div>
        </el-card>
      </el-col>

      <!-- 商品销售额排名 -->
      <el-col :xs="24" :sm="24" :md="12" :lg="12">
        <el-card class="chart-card" shadow="hover">
          <template #header>
            <div class="card-header">
              <div class="header-left">
                <el-icon class="header-icon" color="#ff9845"><Histogram /></el-icon>
                <span class="header-title">商品销售额排名</span>
              </div>
              <el-radio-group v-model="timeRange" size="small" class="time-filter">
                <el-radio-button label="全部">全部</el-radio-button>
                <el-radio-button label="当日">当日</el-radio-button>
                <el-radio-button label="本周">本周</el-radio-button>
                <el-radio-button label="本月">本月</el-radio-button>
                <el-radio-button label="本年">本年</el-radio-button>
              </el-radio-group>
            </div>
          </template>
          <div ref="salesRankChartRef" class="chart-wrapper"></div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 快捷操作 + 最新动态 -->
    <el-row :gutter="20" style="margin-top: 20px">
      <!-- 快捷操作 -->
      <el-col :xs="24" :sm="24" :md="12" :lg="8">
        <el-card class="quick-card" shadow="hover">
          <template #header>
            <div class="card-header">
              <div class="header-left">
                <el-icon class="header-icon" color="#5b8ff9"><Lightning /></el-icon>
                <span class="header-title">快捷操作</span>
              </div>
            </div>
          </template>
          <div class="quick-grid">
            <div 
              v-for="item in quickActions" 
              :key="item.name" 
              class="quick-item"
              @click="handleQuickAction(item.path)"
            >
              <div class="quick-icon" :style="{ background: item.color }">
                <el-icon><component :is="item.icon" /></el-icon>
              </div>
              <span class="quick-name">{{ item.name }}</span>
            </div>
          </div>
        </el-card>
      </el-col>

      <!-- 最新订单 -->
      <el-col :xs="24" :sm="24" :md="12" :lg="8">
        <el-card class="activity-card" shadow="hover">
          <template #header>
            <div class="card-header">
              <div class="header-left">
                <el-icon class="header-icon" color="#5ad8a6"><Bell /></el-icon>
                <span class="header-title">最新订单</span>
              </div>
              <el-button text type="primary" size="small" @click="viewAllOrders">查看全部</el-button>
            </div>
          </template>
          <el-timeline class="activity-timeline">
            <el-timeline-item 
              v-for="order in recentOrders" 
              :key="order.id"
              :timestamp="order.time"
              placement="top"
            >
              <div class="timeline-content">
                <div class="order-info">
                  <span class="order-no">订单号：{{ order.orderNo }}</span>
                  <el-tag :type="order.statusType" size="small">{{ order.status }}</el-tag>
                </div>
                <div class="order-amount">金额：¥{{ order.amount }}</div>
              </div>
            </el-timeline-item>
          </el-timeline>
        </el-card>
      </el-col>

      <!-- 最新用户 -->
      <el-col :xs="24" :sm="24" :md="12" :lg="8">
        <el-card class="user-card" shadow="hover">
          <template #header>
            <div class="card-header">
              <div class="header-left">
                <el-icon class="header-icon" color="#ff9845"><UserFilled /></el-icon>
                <span class="header-title">最新用户</span>
              </div>
              <el-button text type="primary" size="small" @click="viewAllUsers">查看全部</el-button>
            </div>
          </template>
          <div class="user-list">
            <div v-for="user in recentUsers" :key="user.id" class="user-item">
              <el-avatar :size="40" :src="user.avatar">{{ user.name[0] }}</el-avatar>
              <div class="user-info">
                <div class="user-name">{{ user.name }}</div>
                <div class="user-time">{{ user.registerTime }}</div>
              </div>
              <el-tag size="small" type="success">新用户</el-tag>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onBeforeUnmount, watch, computed } from 'vue'
import { useRouter } from 'vue-router'
import { 
  Document, Service, Tickets, TrendCharts, Clock, Wallet, User, 
  ShoppingBag, Goods, Flag, ArrowRight, Top, Bottom, Histogram,
  Lightning, Bell, UserFilled
} from '@element-plus/icons-vue'
import * as echarts from 'echarts'
import { ElMessage } from 'element-plus'

const router = useRouter()
const activeTab = ref('商品排名')
const timeRange = ref('全部')
const goodsTrendChartRef = ref()
const salesRankChartRef = ref()
let goodsTrendChart = null
let salesRankChart = null

// 当前时间
const currentTime = ref('')
const updateTime = () => {
  const now = new Date()
  const year = now.getFullYear()
  const month = String(now.getMonth() + 1).padStart(2, '0')
  const day = String(now.getDate()).padStart(2, '0')
  const hour = String(now.getHours()).padStart(2, '0')
  const minute = String(now.getMinutes()).padStart(2, '0')
  const weekdays = ['星期日', '星期一', '星期二', '星期三', '星期四', '星期五', '星期六']
  const weekday = weekdays[now.getDay()]
  currentTime.value = `${year}年${month}月${day}日 ${hour}:${minute} ${weekday}`
}

// 仪表板数据
const dashboardData = reactive({
  yearAmount: 0,
  dayAmount: 0,
  userCount: 6,
  dayUserCount: 0,
  shopCount: 3,
  dayShopCount: 0,
  goodsCount: 383,
  dayGoodsCount: 0,
  salesTrend: 0,
  userTrend: 0
})

// 待办事项数据
const todoData = reactive({
  pendingGoods: 125,
  pendingIntervention: 1,
  pendingInvoice: 0
})

// 待办事项总数
const totalTodoCount = computed(() => {
  return todoData.pendingGoods + todoData.pendingIntervention + todoData.pendingInvoice
})

// 快捷操作
const quickActions = ref([
  { name: '新增商品', icon: 'Plus', path: '/mall/goods/list', color: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)' },
  { name: '订单管理', icon: 'Document', path: '/mall/order/list', color: 'linear-gradient(135deg, #5ad8a6 0%, #3ab881 100%)' },
  { name: '查看报表', icon: 'DataAnalysis', path: '/mall/statistics/sales', color: 'linear-gradient(135deg, #ff9845 0%, #ff7320 100%)' },
  { name: '用户管理', icon: 'User', path: '/mall/customer/list', color: 'linear-gradient(135deg, #5b8ff9 0%, #3a6fcc 100%)' },
  { name: '营销活动', icon: 'Promotion', path: '/mall/marketing/coupon', color: 'linear-gradient(135deg, #f093fb 0%, #f5576c 100%)' },
  { name: '店铺管理', icon: 'Shop', path: '/mall/store/list', color: 'linear-gradient(135deg, #4facfe 0%, #00f2fe 100%)' }
])

// 最新订单
const recentOrders = ref([
  { id: 1, orderNo: 'ORD20250101001', amount: '2,580.00', status: '已支付', statusType: 'success', time: '2分钟前' },
  { id: 2, orderNo: 'ORD20250101002', amount: '1,280.00', status: '待支付', statusType: 'warning', time: '5分钟前' },
  { id: 3, orderNo: 'ORD20250101003', amount: '3,680.00', status: '已完成', statusType: 'info', time: '8分钟前' },
  { id: 4, orderNo: 'ORD20250101004', amount: '980.00', status: '已取消', statusType: 'danger', time: '12分钟前' }
])

// 最新用户
const recentUsers = ref([
  { id: 1, name: '张三', avatar: '', registerTime: '5分钟前' },
  { id: 2, name: '李四', avatar: '', registerTime: '10分钟前' },
  { id: 3, name: '王五', avatar: '', registerTime: '15分钟前' },
  { id: 4, name: '赵六', avatar: '', registerTime: '20分钟前' },
  { id: 5, name: '孙七', avatar: '', registerTime: '25分钟前' }
])

// 格式化数字
const formatNumber = (num) => {
  if (num === 0) return '0'
  return num.toLocaleString('zh-CN')
}

// 卡片点击
const handleCardClick = (type) => {
  const routes = {
    sales: '/mall/finance/overview',
    users: '/mall/customer/list',
    shops: '/mall/store/list',
    goods: '/mall/goods/list'
  }
  if (routes[type]) {
    router.push(routes[type])
  }
}

// 待办事项点击
const handleTodoClick = (type) => {
  const routes = {
    goods: '/mall/goods/audit',
    intervention: '/mall/order/rights',
    invoice: '/mall/finance/invoice'
  }
  if (routes[type]) {
    router.push(routes[type])
  }
}

// 快捷操作
const handleQuickAction = (path) => {
  router.push(path)
}

// 查看所有
const viewAllTodos = () => {
  ElMessage.info('跳转到待办事项列表')
}

const viewAllOrders = () => {
  router.push('/mall/order/list')
}

const viewAllUsers = () => {
  router.push('/mall/customer/list')
}

// Tab切换
const switchTab = (tab) => {
  activeTab.value = tab
  // TODO: 根据tab更新图表数据
}

// 初始化商品趋势图表
const initGoodsTrendChart = () => {
  if (!goodsTrendChartRef.value) return
  
  goodsTrendChart = echarts.init(goodsTrendChartRef.value)
  const option = {
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      top: '10%',
      containLabel: true
    },
    tooltip: {
      trigger: 'axis',
      axisPointer: {
        type: 'shadow'
      }
    },
    xAxis: {
      type: 'category',
      data: ['1月', '2月', '3月', '4月', '5月', '6月', '7月', '8月', '9月', '10月', '11月', '12月'],
      axisLine: { lineStyle: { color: '#e5e5e5' } },
      axisLabel: { color: '#666' }
    },
    yAxis: {
      type: 'value',
      axisLine: { show: false },
      axisTick: { show: false },
      splitLine: { lineStyle: { color: '#f0f0f0', type: 'dashed' } },
      axisLabel: { color: '#666' }
    },
    series: [
      {
        data: [0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1],
        type: 'bar',
        barWidth: 20,
        itemStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: '#5b8ff9' },
            { offset: 1, color: '#85b5ff' }
          ]),
          borderRadius: [4, 4, 0, 0]
        }
      }
    ]
  }
  goodsTrendChart.setOption(option)
}

// 初始化销售排名图表
const initSalesRankChart = () => {
  if (!salesRankChartRef.value) return
  
  salesRankChart = echarts.init(salesRankChartRef.value)
  const option = {
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      top: '3%',
      containLabel: true
    },
    tooltip: {
      trigger: 'axis',
      axisPointer: {
        type: 'shadow'
      }
    },
    xAxis: {
      type: 'value',
      axisLine: { show: false },
      axisTick: { show: false },
      splitLine: { show: false },
      axisLabel: { show: false }
    },
    yAxis: {
      type: 'category',
      data: [],
      axisLine: { show: false },
      axisTick: { show: false },
      axisLabel: { color: '#666', fontSize: 12 }
    },
    series: [
      {
        type: 'bar',
        data: [],
        barWidth: 16,
        itemStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 1, 0, [
            { offset: 0, color: '#ff9845' },
            { offset: 1, color: '#ffb876' }
          ]),
          borderRadius: [0, 4, 4, 0]
        },
        label: {
          show: false
        }
      }
    ]
  }
  salesRankChart.setOption(option)
}

// 获取仪表板数据
const fetchDashboardData = async () => {
  // TODO: 调用API获取数据
  // 目前使用模拟数据
  dashboardData.yearAmount = 0
  dashboardData.dayAmount = 0
  dashboardData.userCount = 6
  dashboardData.dayUserCount = 0
  dashboardData.shopCount = 3
  dashboardData.dayShopCount = 0
  dashboardData.goodsCount = 383
  dashboardData.dayGoodsCount = 0
  dashboardData.salesTrend = 0
  dashboardData.userTrend = 0
}

// 获取待办事项数据
const fetchTodoData = async () => {
  // TODO: 调用API获取数据
  todoData.pendingGoods = 125
  todoData.pendingIntervention = 1
  todoData.pendingInvoice = 0
}

// 监听时间范围变化
watch(timeRange, () => {
  // TODO: 根据时间范围更新图表数据
})

// 窗口大小改变时重绘图表
const handleResize = () => {
  goodsTrendChart?.resize()
  salesRankChart?.resize()
}

onMounted(() => {
  updateTime()
  setInterval(updateTime, 60000) // 每分钟更新时间
  
  fetchDashboardData()
  fetchTodoData()
  
  setTimeout(() => {
    initGoodsTrendChart()
    initSalesRankChart()
  }, 100)
  
  window.addEventListener('resize', handleResize)
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', handleResize)
  goodsTrendChart?.dispose()
  salesRankChart?.dispose()
})
</script>

<style scoped>
.mall-dashboard {
  padding: 20px;
  background: #f5f7fa;
  min-height: 100vh;
}

/* 欢迎区域 */
.welcome-banner {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 30px;
  border-radius: 12px;
  margin-bottom: 20px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  color: #fff;
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.2);
}

.welcome-content {
  flex: 1;
}

.welcome-title {
  font-size: 24px;
  font-weight: 600;
  margin-bottom: 8px;
  display: flex;
  align-items: center;
  gap: 8px;
}

.welcome-desc {
  font-size: 14px;
  opacity: 0.9;
}

.welcome-time {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 16px;
  opacity: 0.95;
}

/* 统计卡片 */
.stat-cards {
  margin-bottom: 20px;
}

.stat-card {
  position: relative;
  height: 140px;
  border-radius: 12px;
  padding: 24px;
  color: #fff;
  overflow: hidden;
  cursor: pointer;
  transition: all 0.3s;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.stat-card:hover {
  transform: translateY(-6px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.15);
}

.stat-card-blue {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.stat-card-green {
  background: linear-gradient(135deg, #5ad8a6 0%, #3ab881 100%);
}

.stat-card-cyan {
  background: linear-gradient(135deg, #5b8ff9 0%, #3a6fcc 100%);
}

.stat-card-orange {
  background: linear-gradient(135deg, #ff9845 0%, #ff7320 100%);
}

.stat-bg-chart {
  position: absolute;
  right: 0;
  bottom: 0;
  width: 50%;
  height: 60px;
  opacity: 0.15;
}

.stat-content {
  position: relative;
  z-index: 1;
  display: flex;
  align-items: flex-start;
  gap: 16px;
}

.stat-icon {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  background: rgba(255, 255, 255, 0.2);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
}

.stat-info {
  flex: 1;
  min-width: 0;
}

.stat-title {
  font-size: 14px;
  margin-bottom: 8px;
  opacity: 0.9;
}

.stat-value {
  font-size: 28px;
  font-weight: bold;
  margin-bottom: 8px;
  font-variant-numeric: tabular-nums;
  word-break: break-all;
  line-height: 1.15;
}

.stat-sub {
  font-size: 12px;
  opacity: 0.85;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.trend {
  display: inline-flex;
  align-items: center;
  gap: 2px;
  font-weight: 600;
}

.trend.up {
  color: #67c23a;
}

.trend.down {
  color: #f56c6c;
}

/* 待办事项 */
.todo-section {
  margin-bottom: 20px;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 12px;
}

.header-icon {
  width: 36px;
  height: 36px;
  border-radius: 8px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 18px;
}

.section-title {
  font-size: 18px;
  font-weight: 600;
  color: #333;
  margin: 0;
}

.section-badge {
  background: #f56c6c;
  color: #fff;
  padding: 2px 8px;
  border-radius: 10px;
  font-size: 12px;
  font-weight: 600;
}

.todo-cards {
  background: transparent;
}

.todo-card {
  display: flex;
  align-items: center;
  padding: 24px;
  background: #fff;
  border-radius: 12px;
  transition: all 0.3s;
  cursor: pointer;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
}

.todo-card:hover {
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
  transform: translateY(-4px);
}

.todo-icon {
  width: 56px;
  height: 56px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  margin-right: 16px;
  flex-shrink: 0;
}

.todo-info {
  flex: 1;
}

.todo-title {
  font-size: 14px;
  color: #666;
  margin-bottom: 8px;
}

.todo-count {
  font-size: 32px;
  font-weight: bold;
  color: #333;
  margin-bottom: 4px;
}

.todo-action {
  font-size: 12px;
  color: #409eff;
  display: flex;
  align-items: center;
  gap: 4px;
}

/* 图表卡片 */
.chart-card {
  margin-bottom: 20px;
  border-radius: 12px;
  overflow: hidden;
}

.chart-card :deep(.el-card__header) {
  border-bottom: 1px solid #f0f0f0;
  padding: 16px 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 8px;
}

.header-icon {
  font-size: 20px;
}

.header-title {
  font-weight: 600;
  color: #333;
  font-size: 16px;
}

.header-tabs {
  display: flex;
  gap: 16px;
}

.tab-item {
  cursor: pointer;
  padding: 6px 16px;
  border-radius: 6px;
  font-size: 14px;
  color: #666;
  transition: all 0.3s;
  background: #f5f7fa;
}

.tab-item:hover {
  color: #5b8ff9;
  background: #e6f0ff;
}

.tab-item.active {
  color: #fff;
  background: #5b8ff9;
  font-weight: 600;
}

.time-filter {
  display: flex;
  gap: 8px;
}

.chart-content {
  padding: 16px 0;
}

.trend-header {
  margin-bottom: 16px;
  padding: 0 20px;
}

.trend-title {
  font-size: 14px;
  color: #666;
}

.trend-desc {
  font-size: 12px;
  color: #999;
  margin-left: 4px;
}

.chart-wrapper {
  height: 280px;
  width: 100%;
}

/* 快捷操作 */
.quick-card :deep(.el-card__body) {
  padding: 16px;
}

.quick-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 12px;
}

.quick-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  padding: 16px;
  border-radius: 8px;
  background: #f5f7fa;
  cursor: pointer;
  transition: all 0.3s;
}

.quick-item:hover {
  background: #e6f0ff;
  transform: translateY(-2px);
}

.quick-icon {
  width: 44px;
  height: 44px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-size: 20px;
}

.quick-name {
  font-size: 13px;
  color: #333;
  font-weight: 500;
}

/* 活动卡片 */
.activity-card :deep(.el-card__body),
.user-card :deep(.el-card__body) {
  padding: 16px 20px;
}

.activity-timeline {
  padding: 0;
}

.activity-timeline :deep(.el-timeline-item__timestamp) {
  font-size: 12px;
  color: #999;
}

.timeline-content {
  padding: 8px 0;
}

.order-info {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 4px;
}

.order-no {
  font-size: 13px;
  color: #333;
  font-weight: 500;
}

.order-amount {
  font-size: 12px;
  color: #666;
}

/* 用户列表 */
.user-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.user-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px;
  border-radius: 8px;
  background: #f5f7fa;
  transition: all 0.3s;
}

.user-item:hover {
  background: #e6f0ff;
}

.user-info {
  flex: 1;
}

.user-name {
  font-size: 14px;
  color: #333;
  font-weight: 500;
  margin-bottom: 4px;
}

.user-time {
  font-size: 12px;
  color: #999;
}

/* 响应式 */
@media (max-width: 992px) {
  .welcome-banner {
    flex-direction: column;
    gap: 16px;
    align-items: flex-start;
  }
  
  .quick-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 768px) {
  .mall-dashboard {
    padding: 10px;
  }
  
  .welcome-banner {
    padding: 16px;
    border-radius: 12px;
    margin-bottom: 12px;
  }
  
  .welcome-title {
    font-size: 18px;
  }

  .welcome-desc {
    font-size: 12px;
  }
  
  .stat-card {
    margin-bottom: 8px;
    height: auto;
    min-height: 100px;
    padding: 14px;
    border-radius: 12px;
  }

  .stat-content {
    align-items: flex-start;
  }
  
  .stat-value {
    font-size: clamp(16px, 4.5vw, 22px);
  }

  .stat-sub {
    flex-direction: column;
    align-items: flex-start;
    gap: 2px;
    font-size: 11px;
  }
  
  .todo-card {
    margin-bottom: 10px;
    padding: 14px;
  }
  
  .todo-count {
    font-size: 24px;
  }

  .chart-wrapper {
    height: 240px;
  }
  
  .quick-grid {
    grid-template-columns: repeat(2, 1fr);
  }
  
  .header-tabs,
  .time-filter {
    flex-wrap: wrap;
  }
}

@media (max-width: 576px) {
  .stat-content {
    flex-direction: column;
    gap: 8px;
  }
  
  .stat-icon {
    width: 40px;
    height: 40px;
  }
  
  .quick-grid {
    grid-template-columns: 1fr;
  }
}
</style>
