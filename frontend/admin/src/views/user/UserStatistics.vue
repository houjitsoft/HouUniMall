<template>
  <div class="user-statistics-container">
    <!-- 核心指标卡片 -->
    <el-row :gutter="20" class="stats-cards">
      <el-col :span="6">
        <el-card class="stat-card total-users">
          <div class="stat-content">
            <div class="stat-icon"><el-icon><User /></el-icon></div>
            <div class="stat-info">
              <div class="stat-value">{{ userStats.totalUsers }}</div>
              <div class="stat-label">总用户数</div>
              <div class="stat-change positive">+{{ userStats.userGrowth }}%</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card new-users">
          <div class="stat-content">
            <div class="stat-icon"><el-icon><Plus /></el-icon></div>
            <div class="stat-info">
              <div class="stat-value">{{ userStats.newUsers }}</div>
              <div class="stat-label">新增用户</div>
              <div class="stat-change positive">+{{ userStats.newUserGrowth }}%</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card active-users">
          <div class="stat-content">
            <div class="stat-icon"><el-icon><TrendCharts /></el-icon></div>
            <div class="stat-info">
              <div class="stat-value">{{ userStats.activeUsers }}</div>
              <div class="stat-label">活跃用户</div>
              <div class="stat-change negative">{{ userStats.activeGrowth }}%</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card retention-rate">
          <div class="stat-content">
            <div class="stat-icon"><el-icon><DataAnalysis /></el-icon></div>
            <div class="stat-info">
              <div class="stat-value">{{ userStats.retentionRate }}%</div>
              <div class="stat-label">留存率</div>
              <div class="stat-change positive">+{{ userStats.retentionGrowth }}%</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 图表分析区域 -->
    <el-row :gutter="20" class="chart-section">
      <el-col :span="12">
        <el-card class="chart-card">
          <div class="chart-header">
            <span class="chart-title">用户增长趋势</span>
            <el-select v-model="growthPeriod" size="small" @change="loadGrowthChart">
              <el-option label="最近7天" value="7d" />
              <el-option label="最近30天" value="30d" />
              <el-option label="最近90天" value="90d" />
              <el-option label="最近一年" value="1y" />
            </el-select>
          </div>
          <div ref="growthChartRef" class="chart-container" v-loading="chartLoading">
            <div class="chart-placeholder">用户增长趋势图</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card class="chart-card">
          <div class="chart-header">
            <span class="chart-title">用户活跃度分析</span>
            <el-button size="small" @click="loadActivityChart">
              <el-icon><Refresh /></el-icon>
              刷新
            </el-button>
          </div>
          <div ref="activityChartRef" class="chart-container" v-loading="chartLoading">
            <div class="chart-placeholder">用户活跃度分析图</div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" class="chart-section">
      <el-col :span="8">
        <el-card class="chart-card">
          <div class="chart-header">
            <span class="chart-title">用户来源分析</span>
          </div>
          <div ref="sourceChartRef" class="chart-container" v-loading="chartLoading">
            <div class="chart-placeholder">用户来源饼图</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card class="chart-card">
          <div class="chart-header">
            <span class="chart-title">设备类型分布</span>
          </div>
          <div ref="deviceChartRef" class="chart-container" v-loading="chartLoading">
            <div class="chart-placeholder">设备类型分布图</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card class="chart-card">
          <div class="chart-header">
            <span class="chart-title">地区分布</span>
          </div>
          <div ref="regionChartRef" class="chart-container" v-loading="chartLoading">
            <div class="chart-placeholder">地区分布图</div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 数据表格区域 -->
    <el-row :gutter="20" class="table-section">
      <el-col :span="12">
        <el-card class="table-card">
          <div class="table-header">
            <span class="table-title">用户等级分布</span>
            <el-button size="small" @click="handleRefreshLevel">
              <el-icon><Refresh /></el-icon>
              刷新
            </el-button>
          </div>
          <el-table :data="levelData" v-loading="tableLoading" stripe>
            <el-table-column prop="level" label="会员等级" />
            <el-table-column prop="count" label="用户数量" />
            <el-table-column prop="percentage" label="占比">
              <template #default="{ row }">
                <span class="percentage">{{ row.percentage }}%</span>
              </template>
            </el-table-column>
            <el-table-column prop="avgPoints" label="平均积分" />
            <el-table-column prop="avgConsume" label="平均消费">
              <template #default="{ row }">
                <span class="money">¥{{ row.avgConsume }}</span>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card class="table-card">
          <div class="table-header">
            <span class="table-title">年龄段分布</span>
            <el-button size="small" @click="handleRefreshAge">
              <el-icon><Refresh /></el-icon>
              刷新
            </el-button>
          </div>
          <el-table :data="ageData" v-loading="tableLoading" stripe>
            <el-table-column prop="ageRange" label="年龄段" />
            <el-table-column prop="count" label="用户数量" />
            <el-table-column prop="percentage" label="占比">
              <template #default="{ row }">
                <span class="percentage">{{ row.percentage }}%</span>
              </template>
            </el-table-column>
            <el-table-column prop="activity" label="活跃度">
              <template #default="{ row }">
                <el-tag :type="getActivityTagType(row.activity)">
                  {{ row.activity }}
                </el-tag>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
    </el-row>

    <!-- 操作按钮区域 -->
    <el-card class="action-card">
      <el-row :gutter="10">
        <el-col :span="24">
          <el-button type="primary" @click="handleGenerateReport">
            <el-icon><Document /></el-icon>
            生成分析报告
          </el-button>
          <el-button type="success" @click="handleExportData">
            <el-icon><Download /></el-icon>
            导出统计数据
          </el-button>
          <el-button type="warning" @click="handleUserSegmentation">
            <el-icon><Grid /></el-icon>
            用户分群
          </el-button>
          <el-button type="info" @click="handlePredictiveAnalysis">
            <el-icon><TrendCharts /></el-icon>
            预测分析
          </el-button>
          <el-button type="primary" @click="handleRealTimeMonitor">
            <el-icon><Monitor /></el-icon>
            实时监控
          </el-button>
        </el-col>
      </el-row>
    </el-card>

    <!-- 实时数据监控 -->
    <el-card class="monitor-card" v-if="showMonitor">
      <div class="monitor-header">
        <span class="monitor-title">实时用户监控</span>
        <div class="monitor-controls">
          <el-switch v-model="autoRefresh" active-text="自动刷新" />
          <el-button size="small" @click="showMonitor = false">
            <el-icon><Close /></el-icon>
            关闭
          </el-button>
        </div>
      </div>
      <el-row :gutter="20" class="monitor-stats">
        <el-col :span="6">
          <div class="monitor-item">
            <div class="monitor-value">{{ realtimeStats.onlineUsers }}</div>
            <div class="monitor-label">在线用户</div>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="monitor-item">
            <div class="monitor-value">{{ realtimeStats.newRegistrations }}</div>
            <div class="monitor-label">新注册(今日)</div>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="monitor-item">
            <div class="monitor-value">{{ realtimeStats.activeOrders }}</div>
            <div class="monitor-label">活跃订单</div>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="monitor-item">
            <div class="monitor-value">{{ realtimeStats.conversionRate }}%</div>
            <div class="monitor-label">转化率</div>
          </div>
        </el-col>
      </el-row>
    </el-card>

    <!-- 用户分群对话框 -->
    <el-dialog v-model="segmentationDialogVisible" title="用户分群" width="800px">
      <el-form :model="segmentationForm" label-width="120px">
        <el-form-item label="分群名称">
          <el-input v-model="segmentationForm.name" placeholder="请输入分群名称" />
        </el-form-item>
        <el-form-item label="分群条件">
          <el-checkbox-group v-model="segmentationForm.conditions">
            <el-checkbox label="new_users">新用户(30天内注册)</el-checkbox>
            <el-checkbox label="active_users">活跃用户(7天内登录)</el-checkbox>
            <el-checkbox label="high_value">高价值用户(消费>1000)</el-checkbox>
            <el-checkbox label="vip_users">VIP用户</el-checkbox>
            <el-checkbox label="inactive_users">沉睡用户(30天未登录)</el-checkbox>
            <el-checkbox label="mobile_users">移动端用户</el-checkbox>
          </el-checkbox-group>
        </el-form-item>
        <el-form-item label="分群描述">
          <el-input
            v-model="segmentationForm.description"
            type="textarea"
            :rows="3"
            placeholder="请输入分群描述"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="segmentationDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleCreateSegmentation">创建分群</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>

// 使用 Element Plus 国际化系统
import { useI18n } from '@/i18n-element'
const { t } = useI18n()
import { ref, reactive, onMounted, onUnmounted } from 'vue'
import { ElMessage } from 'element-plus'
import {
  User,
  Plus,
  TrendCharts,
  DataAnalysis,
  Document,
  Download,
  Grid,
  Monitor,
  Refresh,
  Close
} from '@element-plus/icons-vue'

// 响应式数据
const chartLoading = ref(false)
const tableLoading = ref(false)
const showMonitor = ref(false)
const autoRefresh = ref(false)
const growthPeriod = ref('30d')

// 图表引用
const growthChartRef = ref()
const activityChartRef = ref()
const sourceChartRef = ref()
const deviceChartRef = ref()
const regionChartRef = ref()

// 统计数据
const userStats = reactive({
  totalUsers: '156,842',
  userGrowth: 12.5,
  newUsers: '2,456',
  newUserGrowth: 8.3,
  activeUsers: '89,245',
  activeGrowth: -2.1,
  retentionRate: 78.5,
  retentionGrowth: 5.2
})

// 实时统计数据
const realtimeStats = reactive({
  onlineUsers: 1247,
  newRegistrations: 68,
  activeOrders: 156,
  conversionRate: 3.8
})

// 用户等级分布数据
const levelData = ref([
  { level: '普通会员', count: 85000, percentage: 54.2, avgPoints: 580, avgConsume: 1250 },
  { level: '银牌会员', count: 42000, percentage: 26.8, avgPoints: 2850, avgConsume: 3200 },
  { level: '金牌会员', count: 21800, percentage: 13.9, avgPoints: 8500, avgConsume: 8500 },
  { level: '钻石会员', count: 8042, percentage: 5.1, avgPoints: 25800, avgConsume: 18900 }
])

// 年龄段分布数据
const ageData = ref([
  { ageRange: '18-25岁', count: 45200, percentage: 28.8, activity: '高' },
  { ageRange: '26-35岁', count: 62400, percentage: 39.8, activity: '高' },
  { ageRange: '36-45岁', count: 31200, percentage: 19.9, activity: '中' },
  { ageRange: '46-55岁', count: 12840, percentage: 8.2, activity: '中' },
  { ageRange: '55岁以上', count: 5202, percentage: 3.3, activity: '低' }
])

// 对话框状态
const segmentationDialogVisible = ref(false)

// 分群表单
const segmentationForm = reactive({
  name: '',
  conditions: [],
  description: ''
})

// 定时器
let refreshTimer = null

// 方法定义
const loadGrowthChart = () => {
  chartLoading.value = true
  console.log('加载用户增长趋势图', growthPeriod.value)
  setTimeout(() => {
    chartLoading.value = false
  }, 1000)
}

const loadActivityChart = () => {
  chartLoading.value = true
  console.log('加载用户活跃度分析图')
  setTimeout(() => {
    chartLoading.value = false
  }, 1000)
}

const handleRefreshLevel = () => {
  tableLoading.value = true
  console.log('刷新用户等级分布数据')
  setTimeout(() => {
    tableLoading.value = false
  }, 500)
}

const handleRefreshAge = () => {
  tableLoading.value = true
  console.log('刷新年龄段分布数据')
  setTimeout(() => {
    tableLoading.value = false
  }, 500)
}

const handleGenerateReport = () => {
  ElMessage.success('用户分析报告生成成功')
}

const handleExportData = () => {
  ElMessage.success('统计数据导出成功')
}

const handleUserSegmentation = () => {
  Object.assign(segmentationForm, {
    name: '',
    conditions: [],
    description: ''
  })
  segmentationDialogVisible.value = true
}

const handleCreateSegmentation = () => {
  if (!segmentationForm.name) {
    ElMessage.warning('请输入分群名称')
    return
  }
  if (!segmentationForm.conditions.length) {
    ElMessage.warning('请选择分群条件')
    return
  }
  
  console.log('创建用户分群', segmentationForm)
  ElMessage.success('用户分群创建成功')
  segmentationDialogVisible.value = false
}

const handlePredictiveAnalysis = () => {
  ElMessage.success('预测分析功能开发中')
}

const handleRealTimeMonitor = () => {
  showMonitor.value = !showMonitor.value
  if (showMonitor.value) {
    startRealTimeUpdate()
  } else {
    stopRealTimeUpdate()
  }
}

const startRealTimeUpdate = () => {
  if (refreshTimer) return
  
  refreshTimer = setInterval(() => {
    // 模拟实时数据更新
    realtimeStats.onlineUsers += Math.floor(Math.random() * 20 - 10)
    realtimeStats.newRegistrations += Math.floor(Math.random() * 5)
    realtimeStats.activeOrders += Math.floor(Math.random() * 10 - 5)
    realtimeStats.conversionRate = (Math.random() * 2 + 3).toFixed(1)
    
    // 确保数据合理性
    if (realtimeStats.onlineUsers < 1000) realtimeStats.onlineUsers = 1000
    if (realtimeStats.newRegistrations < 0) realtimeStats.newRegistrations = 0
    if (realtimeStats.activeOrders < 100) realtimeStats.activeOrders = 100
  }, 3000)
}

const stopRealTimeUpdate = () => {
  if (refreshTimer) {
    clearInterval(refreshTimer)
    refreshTimer = null
  }
}

// 获取活跃度标签类型
const getActivityTagType = (activity) => {
  const activityMap = {
    '高': 'success',
    '中': 'warning',
    '低': 'danger'
  }
  return activityMap[activity] || 'info'
}

// 监听自动刷新开关
const watchAutoRefresh = () => {
  if (autoRefresh.value && showMonitor.value) {
    startRealTimeUpdate()
  } else {
    stopRealTimeUpdate()
  }
}

// 初始化
onMounted(() => {
  loadGrowthChart()
  loadActivityChart()
  
  // 监听自动刷新
  const unwatch = () => {
    if (autoRefresh.value !== undefined) {
      watchAutoRefresh()
    }
  }
  
  // 简单的响应式监听
  setInterval(() => {
    if (autoRefresh.value && showMonitor.value && !refreshTimer) {
      startRealTimeUpdate()
    } else if (!autoRefresh.value && refreshTimer) {
      stopRealTimeUpdate()
    }
  }, 1000)
})

// 清理
onUnmounted(() => {
  stopRealTimeUpdate()
})
</script>

<style scoped>
.user-statistics-container {
  padding: 20px;
}

.stats-cards {
  margin-bottom: 20px;
}

.chart-section, .table-section {
  margin-bottom: 20px;
}

.stat-card, .chart-card, .table-card {
  transition: all 0.3s;
}

.stat-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 4px 20px rgba(0,0,0,0.1);
}

.stat-content {
  display: flex;
  align-items: center;
  padding: 10px;
}

.stat-icon {
  font-size: 40px;
  margin-right: 15px;
  width: 60px;
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 10px;
}

.total-users .stat-icon {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
}

.new-users .stat-icon {
  background: linear-gradient(135deg, #43e97b 0%, #38f9d7 100%);
  color: white;
}

.active-users .stat-icon {
  background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
  color: white;
}

.retention-rate .stat-icon {
  background: linear-gradient(135deg, #fa709a 0%, #fee140 100%);
  color: white;
}

.stat-info {
  flex: 1;
}

.stat-value {
  font-size: 28px;
  font-weight: bold;
  color: #303133;
  margin-bottom: 5px;
}

.stat-label {
  font-size: 14px;
  color: #909399;
  margin-bottom: 3px;
}

.stat-change {
  font-size: 12px;
  font-weight: bold;
}

.stat-change.positive {
  color: #67C23A;
}

.stat-change.negative {
  color: #F56C6C;
}

.chart-header, .table-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.chart-title, .table-title {
  font-size: 16px;
  font-weight: bold;
  color: #303133;
}

.chart-container {
  height: 300px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.chart-placeholder {
  color: #909399;
  font-size: 14px;
}

.percentage {
  color: #E6A23C;
  font-weight: bold;
}

.money {
  color: #67C23A;
  font-weight: bold;
}

.action-card {
  margin-bottom: 20px;
}

.monitor-card {
  margin-bottom: 20px;
  border: 2px solid #409EFF;
}

.monitor-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.monitor-title {
  font-size: 16px;
  font-weight: bold;
  color: #409EFF;
}

.monitor-controls {
  display: flex;
  align-items: center;
  gap: 10px;
}

.monitor-stats {
  padding: 20px 0;
}

.monitor-item {
  text-align: center;
  padding: 15px;
  background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
  border-radius: 8px;
}

.monitor-value {
  font-size: 24px;
  font-weight: bold;
  color: #409EFF;
  margin-bottom: 5px;
}

.monitor-label {
  font-size: 14px;
  color: #606266;
}
</style>
