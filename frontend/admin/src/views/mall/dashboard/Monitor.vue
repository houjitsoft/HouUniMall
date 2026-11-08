<template>
  <div class="monitor-page">
    <!-- 实时指标 -->
    <el-row :gutter="20" class="realtime-metrics">
      <el-col :xs="12" :sm="6" :md="6" :lg="6">
        <el-card shadow="hover" class="metric-card">
          <div class="metric-header">
            <span class="metric-label">在线用户</span>
            <el-tag type="success" size="small">LIVE</el-tag>
          </div>
          <div class="metric-value animate">{{ realtimeData.onlineUsers }}</div>
          <div class="metric-footer">
            <span class="metric-change up">↑ {{ realtimeData.userChange }}</span>
            <span class="metric-time">实时</span>
          </div>
        </el-card>
      </el-col>
      
      <el-col :xs="12" :sm="6" :md="6" :lg="6">
        <el-card shadow="hover" class="metric-card">
          <div class="metric-header">
            <span class="metric-label">当前订单</span>
            <el-tag type="warning" size="small">LIVE</el-tag>
          </div>
          <div class="metric-value animate">{{ realtimeData.currentOrders }}</div>
          <div class="metric-footer">
            <span class="metric-change up">↑ {{ realtimeData.orderChange }}</span>
            <span class="metric-time">实时</span>
          </div>
        </el-card>
      </el-col>
      
      <el-col :xs="12" :sm="6" :md="6" :lg="6">
        <el-card shadow="hover" class="metric-card">
          <div class="metric-header">
            <span class="metric-label">实时销售额</span>
            <el-tag type="danger" size="small">LIVE</el-tag>
          </div>
          <div class="metric-value animate">¥{{ formatNumber(realtimeData.realSales) }}</div>
          <div class="metric-footer">
            <span class="metric-change up">↑ {{ realtimeData.salesChange }}%</span>
            <span class="metric-time">实时</span>
          </div>
        </el-card>
      </el-col>
      
      <el-col :xs="12" :sm="6" :md="6" :lg="6">
        <el-card shadow="hover" class="metric-card">
          <div class="metric-header">
            <span class="metric-label">系统负载</span>
            <el-tag :type="systemLoad < 70 ? 'success' : 'warning'" size="small">{{ systemLoad }}%</el-tag>
          </div>
          <div class="metric-value animate">{{ systemLoad }}%</div>
          <div class="metric-footer">
            <el-progress :percentage="systemLoad" :show-text="false" :stroke-width="4" />
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 实时活动流 -->
    <el-row :gutter="20" style="margin-top: 20px">
      <el-col :xs="24" :sm="24" :md="16" :lg="16">
        <el-card shadow="hover">
          <template #header>
            <div class="card-header">
              <span class="header-title">实时活动流</span>
              <el-tag type="success" size="small">
                <el-icon><View /></el-icon> 实时监控中
              </el-tag>
            </div>
          </template>
          <div class="activity-stream">
            <el-timeline>
              <el-timeline-item 
                v-for="activity in activities" 
                :key="activity.id"
                :timestamp="activity.time"
                :color="activity.color"
                placement="top"
              >
                <div class="activity-content">
                  <el-icon class="activity-icon" :style="{ color: activity.color }">
                    <component :is="activity.icon" />
                  </el-icon>
                  <span class="activity-text">{{ activity.text }}</span>
                  <el-tag v-if="activity.tag" size="small" :type="activity.tagType">{{ activity.tag }}</el-tag>
                </div>
              </el-timeline-item>
            </el-timeline>
          </div>
        </el-card>
      </el-col>

      <el-col :xs="24" :sm="24" :md="8" :lg="8">
        <el-card shadow="hover">
          <template #header>
            <span class="header-title">系统状态</span>
          </template>
          <div class="system-status">
            <div v-for="item in systemStatus" :key="item.name" class="status-item">
              <div class="status-label">
                <el-icon><component :is="item.icon" /></el-icon>
                {{ item.name }}
              </div>
              <div class="status-value">
                <el-tag :type="item.type" size="small">{{ item.status }}</el-tag>
              </div>
            </div>
          </div>
        </el-card>

        <el-card shadow="hover" style="margin-top: 20px">
          <template #header>
            <span class="header-title">实时警告</span>
          </template>
          <div class="alert-list">
            <el-alert
              v-for="alert in alerts"
              :key="alert.id"
              :title="alert.title"
              :type="alert.type"
              :description="alert.desc"
              :closable="false"
              show-icon
              style="margin-bottom: 12px"
            />
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 实时数据图表 -->
    <el-row :gutter="20" style="margin-top: 20px">
      <el-col :span="24">
        <el-card shadow="hover">
          <template #header>
            <span class="header-title">实时流量监控</span>
          </template>
          <div ref="trafficChartRef" class="chart-container"></div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onBeforeUnmount } from 'vue'
import { 
  View, ShoppingCart, User, Goods, Warning, 
  CircleCheck, Service, Connection
} from '@element-plus/icons-vue'
import * as echarts from 'echarts'

const trafficChartRef = ref()
let trafficChart = null
let updateTimer = null
let chartTimer = null

const realtimeData = reactive({
  onlineUsers: 1258,
  userChange: '+12',
  currentOrders: 45,
  orderChange: '+5',
  realSales: 28650,
  salesChange: 8.5
})

const systemLoad = ref(45)

const activities = ref([
  { id: 1, time: '1秒前', text: '用户 张三 下了一个订单', icon: ShoppingCart, color: '#5ad8a6', tag: '新订单', tagType: 'success' },
  { id: 2, time: '3秒前', text: '用户 李四 注册成为会员', icon: User, color: '#5b8ff9', tag: '新用户', tagType: 'primary' },
  { id: 3, time: '5秒前', text: '商品 iPhone 15 Pro 库存不足', icon: Warning, color: '#ff9845', tag: '警告', tagType: 'warning' },
  { id: 4, time: '8秒前', text: '订单 #12580 已完成支付', icon: CircleCheck, color: '#67c23a', tag: '支付', tagType: 'success' },
  { id: 5, time: '12秒前', text: '用户 王五 浏览了 5 个商品', icon: Goods, color: '#909399', tag: '浏览', tagType: 'info' }
])

const systemStatus = ref([
  { name: '数据库', icon: Connection, status: '正常', type: 'success' },
  { name: 'Redis缓存', icon: Connection, status: '正常', type: 'success' },
  { name: '支付服务', icon: Service, status: '正常', type: 'success' },
  { name: '短信服务', icon: Service, status: '正常', type: 'success' },
  { name: '物流接口', icon: Service, status: '正常', type: 'success' }
])

const alerts = ref([
  { id: 1, title: '库存预警', desc: '18个商品库存低于预警值', type: 'warning' },
  { id: 2, title: '待处理订单', desc: '有23个订单待处理', type: 'info' }
])

const formatNumber = (num) => {
  return num.toLocaleString('zh-CN')
}

// 模拟实时数据更新
const updateRealtimeData = () => {
  realtimeData.onlineUsers += Math.floor(Math.random() * 5) - 2
  realtimeData.currentOrders = Math.max(0, realtimeData.currentOrders + Math.floor(Math.random() * 3) - 1)
  realtimeData.realSales += Math.floor(Math.random() * 500) - 200
  systemLoad.value = Math.max(30, Math.min(80, systemLoad.value + Math.floor(Math.random() * 10) - 5))
  
  // 添加新活动
  if (Math.random() > 0.7) {
    const newActivity = {
      id: Date.now(),
      time: '刚刚',
      text: '用户进行了操作',
      icon: User,
      color: '#5b8ff9',
      tag: '新动态',
      tagType: 'primary'
    }
    activities.value.unshift(newActivity)
    if (activities.value.length > 10) {
      activities.value.pop()
    }
  }
}

const initTrafficChart = () => {
  if (!trafficChartRef.value) return
  
  trafficChart = echarts.init(trafficChartRef.value)
  
  // 生成初始数据
  const now = Date.now()
  const data = []
  for (let i = 60; i >= 0; i--) {
    data.push([
      new Date(now - i * 1000),
      Math.floor(Math.random() * 300) + 800
    ])
  }
  
  const option = {
    tooltip: {
      trigger: 'axis',
      axisPointer: { type: 'cross' }
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      top: '3%',
      containLabel: true
    },
    xAxis: {
      type: 'time',
      splitLine: { show: false }
    },
    yAxis: {
      type: 'value',
      name: '在线用户数',
      splitLine: { lineStyle: { type: 'dashed' } }
    },
    series: [
      {
        name: '在线用户',
        type: 'line',
        smooth: true,
        data: data,
        itemStyle: { color: '#5b8ff9' },
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(91, 143, 249, 0.5)' },
            { offset: 1, color: 'rgba(91, 143, 249, 0.05)' }
          ])
        }
      }
    ]
  }
  
  trafficChart.setOption(option)
  
  // 模拟实时数据更新
  chartTimer = setInterval(() => {
    const now = new Date()
    const newData = option.series[0].data.slice(1)
    newData.push([now, Math.floor(Math.random() * 300) + 800])
    
    trafficChart.setOption({
      series: [{ data: newData }]
    })
  }, 2000)
}

const handleResize = () => {
  trafficChart?.resize()
}

onMounted(() => {
  setTimeout(initTrafficChart, 100)
  updateTimer = setInterval(updateRealtimeData, 3000)
  window.addEventListener('resize', handleResize)
})

onBeforeUnmount(() => {
  clearInterval(updateTimer)
  clearInterval(chartTimer)
  window.removeEventListener('resize', handleResize)
  trafficChart?.dispose()
})
</script>

<style scoped>
.monitor-page {
  padding: 20px;
  background: #f5f7fa;
  min-height: 100vh;
}

.realtime-metrics {
  margin-bottom: 20px;
}

.metric-card {
  border-radius: 12px;
  transition: all 0.3s;
}

.metric-card:hover {
  transform: translateY(-4px);
}

.metric-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.metric-label {
  font-size: 14px;
  color: #666;
}

.metric-value {
  font-size: 32px;
  font-weight: bold;
  color: #333;
  margin-bottom: 12px;
}

.metric-value.animate {
  animation: pulse 2s ease-in-out infinite;
}

@keyframes pulse {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.8; }
}

.metric-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.metric-change {
  font-size: 12px;
  font-weight: 600;
}

.metric-change.up {
  color: #67c23a;
}

.metric-change.down {
  color: #f56c6c;
}

.metric-time {
  font-size: 12px;
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

.activity-stream {
  max-height: 480px;
  overflow-y: auto;
}

.activity-content {
  display: flex;
  align-items: center;
  gap: 8px;
}

.activity-icon {
  font-size: 16px;
}

.activity-text {
  flex: 1;
  font-size: 14px;
  color: #333;
}

.system-status {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.status-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px;
  background: #f5f7fa;
  border-radius: 8px;
}

.status-label {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  color: #333;
}

.alert-list {
  max-height: 200px;
  overflow-y: auto;
}

.chart-container {
  height: 320px;
  width: 100%;
}

@media (max-width: 768px) {
  .monitor-page {
    padding: 10px;
  }
  
  .metric-value {
    font-size: 24px;
  }
  
  .chart-container {
    height: 240px;
  }
}
</style>
