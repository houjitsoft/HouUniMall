<template>
  <div class="cache-statistics-container">
    <!-- 页面标题 -->
    <div class="page-header">
      <h2>缓存统计</h2>
      <p>查看系统缓存的详细统计信息</p>
    </div>

    <!-- 统计概览 -->
    <el-row :gutter="20" class="stats-overview">
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon redis">
              <el-icon><DataBoard /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.redis.hitRate }}%</div>
              <div class="stat-label">Redis命中率</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon app">
              <el-icon><Monitor /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.app.hitRate }}%</div>
              <div class="stat-label">应用缓存命中率</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon static">
              <el-icon><Files /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.static.hitRate }}%</div>
              <div class="stat-label">静态资源命中率</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon total">
              <el-icon><TrendCharts /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.total.hitRate }}%</div>
              <div class="stat-label">总体命中率</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 详细统计 -->
    <el-row :gutter="20">
      <el-col :span="12">
        <el-card class="detail-card">
          <template #header>
            <span>Redis缓存统计</span>
          </template>
          <div class="detail-content">
            <div class="stat-item">
              <span class="label">总键数：</span>
              <span class="value">{{ stats.redis.totalKeys }}</span>
            </div>
            <div class="stat-item">
              <span class="label">内存使用：</span>
              <span class="value">{{ stats.redis.memoryUsed }}MB / {{ stats.redis.memoryTotal }}MB</span>
            </div>
            <div class="stat-item">
              <span class="label">命中次数：</span>
              <span class="value">{{ stats.redis.hits }}</span>
            </div>
            <div class="stat-item">
              <span class="label">未命中次数：</span>
              <span class="value">{{ stats.redis.misses }}</span>
            </div>
            <div class="stat-item">
              <span class="label">过期键数：</span>
              <span class="value">{{ stats.redis.expiredKeys }}</span>
            </div>
            <div class="stat-item">
              <span class="label">连接数：</span>
              <span class="value">{{ stats.redis.connections }}</span>
            </div>
          </div>
        </el-card>
      </el-col>
      
      <el-col :span="12">
        <el-card class="detail-card">
          <template #header>
            <span>应用缓存统计</span>
          </template>
          <div class="detail-content">
            <div class="stat-item">
              <span class="label">缓存项数：</span>
              <span class="value">{{ stats.app.totalItems }}</span>
            </div>
            <div class="stat-item">
              <span class="label">缓存大小：</span>
              <span class="value">{{ stats.app.size }}MB</span>
            </div>
            <div class="stat-item">
              <span class="label">命中次数：</span>
              <span class="value">{{ stats.app.hits }}</span>
            </div>
            <div class="stat-item">
              <span class="label">未命中次数：</span>
              <span class="value">{{ stats.app.misses }}</span>
            </div>
            <div class="stat-item">
              <span class="label">过期项数：</span>
              <span class="value">{{ stats.app.expiredItems }}</span>
            </div>
            <div class="stat-item">
              <span class="label">最大项数：</span>
              <span class="value">{{ stats.app.maxItems }}</span>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 图表统计 -->
    <el-card class="chart-card">
      <template #header>
        <div class="chart-header">
          <span>缓存使用趋势</span>
          <el-select v-model="timeRange" @change="updateChart" style="width: 150px;">
            <el-option label="最近1小时" value="1h" />
            <el-option label="最近24小时" value="24h" />
            <el-option label="最近7天" value="7d" />
            <el-option label="最近30天" value="30d" />
          </el-select>
        </div>
      </template>
      
      <div class="chart-container">
        <div class="chart-placeholder">
          <el-icon><TrendCharts /></el-icon>
          <p>图表组件开发中...</p>
          <p>时间范围：{{ timeRange }}</p>
        </div>
      </div>
    </el-card>

    <!-- 性能指标 -->
    <el-card class="performance-card">
      <template #header>
        <span>性能指标</span>
      </template>
      
      <el-table :data="performanceData" stripe>
        <el-table-column prop="metric" label="指标名称" width="200" />
        <el-table-column prop="current" label="当前值" width="120" />
        <el-table-column prop="average" label="平均值" width="120" />
        <el-table-column prop="max" label="最大值" width="120" />
        <el-table-column prop="min" label="最小值" width="120" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusTagType(row.status)">
              {{ row.status }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="trend" label="趋势" width="100">
          <template #default="{ row }">
            <el-icon :class="getTrendClass(row.trend)">
              <component :is="getTrendIcon(row.trend)" />
            </el-icon>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 操作按钮 -->
    <div class="action-buttons">
      <el-button type="primary" @click="refreshStats">
        <el-icon><Refresh /></el-icon>
        刷新统计
      </el-button>
      <el-button type="success" @click="exportStats">
        <el-icon><Download /></el-icon>
        导出统计
      </el-button>
      <el-button type="info" @click="generateReport">
        <el-icon><Document /></el-icon>
        生成报告
      </el-button>
    </div>
  </div>
</template>

<script setup>

// 使用 Element Plus 国际化系统
import { useI18n } from '@/i18n-element'
const { t } = useI18n()
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { 
  DataBoard, Monitor, Files, TrendCharts, Refresh, Download, Document,
  ArrowUp, ArrowDown, Minus
} from '@element-plus/icons-vue'

// 数据
const timeRange = ref('24h')

const stats = reactive({
  redis: {
    hitRate: 95.6,
    totalKeys: 1234,
    memoryUsed: 128,
    memoryTotal: 512,
    hits: 45678,
    misses: 2103,
    expiredKeys: 45,
    connections: 12
  },
  app: {
    hitRate: 88.2,
    totalItems: 456,
    size: 64,
    hits: 12345,
    misses: 1654,
    expiredItems: 23,
    maxItems: 1000
  },
  static: {
    hitRate: 92.1,
    totalFiles: 1890,
    size: 256,
    hits: 78901,
    misses: 6789,
    expiredFiles: 12,
    maxFiles: 5000
  },
  total: {
    hitRate: 91.8
  }
})

const performanceData = ref([
  {
    metric: '平均响应时间',
    current: '12ms',
    average: '15ms',
    max: '45ms',
    min: '8ms',
    status: '正常',
    trend: 'down'
  },
  {
    metric: 'QPS（每秒查询）',
    current: '1250',
    average: '1180',
    max: '2100',
    min: '890',
    status: '正常',
    trend: 'up'
  },
  {
    metric: '内存使用率',
    current: '25%',
    average: '28%',
    max: '45%',
    min: '18%',
    status: '正常',
    trend: 'down'
  },
  {
    metric: 'CPU使用率',
    current: '15%',
    average: '18%',
    max: '35%',
    min: '12%',
    status: '正常',
    trend: 'stable'
  },
  {
    metric: '网络延迟',
    current: '2ms',
    average: '3ms',
    max: '8ms',
    min: '1ms',
    status: '正常',
    trend: 'down'
  }
])

// 方法
const refreshStats = () => {
  ElMessage.success('统计信息已刷新')
}

const exportStats = () => {
  ElMessage.success('统计信息导出成功')
}

const generateReport = () => {
  ElMessage.success('统计报告生成成功')
}

const updateChart = () => {
  ElMessage.info(`图表已更新为 ${timeRange.value} 数据`)
}

const getStatusTagType = (status) => {
  const statusMap = {
    '正常': 'success',
    '警告': 'warning',
    '异常': 'danger'
  }
  return statusMap[status] || 'info'
}

const getTrendIcon = (trend) => {
  const trendMap = {
    'up': ArrowUp,
    'down': ArrowDown,
    'stable': Minus
  }
  return trendMap[trend] || Minus
}

const getTrendClass = (trend) => {
  const classMap = {
    'up': 'trend-up',
    'down': 'trend-down',
    'stable': 'trend-stable'
  }
  return classMap[trend] || 'trend-stable'
}

// 生命周期
onMounted(() => {
  refreshStats()
})
</script>

<style scoped>
.cache-statistics-container {
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

.stats-overview {
  margin-bottom: 20px;
}

.stat-card {
  margin-bottom: 20px;
}

.stat-content {
  display: flex;
  align-items: center;
}

.stat-icon {
  width: 60px;
  height: 60px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 15px;
  font-size: 24px;
  color: white;
}

.stat-icon.redis {
  background: linear-gradient(135deg, #ff6b6b, #ee5a52);
}

.stat-icon.app {
  background: linear-gradient(135deg, #4ecdc4, #44a08d);
}

.stat-icon.static {
  background: linear-gradient(135deg, #45b7d1, #96c93d);
}

.stat-icon.total {
  background: linear-gradient(135deg, #f093fb, #f5576c);
}

.stat-info {
  flex: 1;
}

.stat-value {
  font-size: 28px;
  font-weight: bold;
  color: #303133;
  line-height: 1;
}

.stat-label {
  font-size: 14px;
  color: #606266;
  margin-top: 5px;
}

.detail-card {
  margin-bottom: 20px;
}

.detail-content {
  padding: 10px 0;
}

.stat-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 0;
  border-bottom: 1px solid #f0f0f0;
}

.stat-item:last-child {
  border-bottom: none;
}

.stat-item .label {
  color: #606266;
  font-size: 14px;
}

.stat-item .value {
  color: #303133;
  font-weight: bold;
  font-size: 14px;
}

.chart-card {
  margin-bottom: 20px;
}

.chart-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.chart-container {
  height: 300px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.chart-placeholder {
  text-align: center;
  color: #909399;
}

.chart-placeholder .el-icon {
  font-size: 48px;
  margin-bottom: 10px;
}

.performance-card {
  margin-bottom: 20px;
}

.action-buttons {
  text-align: center;
  margin-top: 20px;
}

.action-buttons .el-button {
  margin: 0 10px;
}

.trend-up {
  color: #67c23a;
}

.trend-down {
  color: #f56c6c;
}

.trend-stable {
  color: #909399;
}
</style>
