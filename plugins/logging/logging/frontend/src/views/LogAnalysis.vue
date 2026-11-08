<template>
  <div class="log-analysis">
    <el-card class="box-card">
      <template #header>
        <div class="card-header">
          <span>日志分析</span>
          <div>
            <el-tag type="info" style="margin-right: 8px">审计库 + ELK 汇总</el-tag>
            <el-button type="primary" @click="generateReport">刷新数据</el-button>
          </div>
        </div>
      </template>
      
      <el-alert
        v-if="stats.elkError"
        type="warning"
        :closable="false"
        show-icon
        :title="stats.elkError"
        style="margin-bottom: 12px"
      />

      <!-- 分析统计卡片 -->
      <el-row :gutter="20" class="stats-row">
        <el-col :span="6">
          <el-card class="stat-card">
            <div class="stat-content">
              <div class="stat-value">{{ stats.totalLogs }}</div>
              <div class="stat-label">总日志数</div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card class="stat-card">
            <div class="stat-content">
              <div class="stat-value error">{{ stats.errorLogs }}</div>
              <div class="stat-label">错误日志</div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card class="stat-card">
            <div class="stat-content">
              <div class="stat-value warning">{{ stats.warningLogs }}</div>
              <div class="stat-label">警告日志</div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card class="stat-card">
            <div class="stat-content">
              <div class="stat-value success">{{ stats.infoLogs }}</div>
              <div class="stat-label">信息日志</div>
            </div>
          </el-card>
        </el-col>
      </el-row>

      <!-- 图表分析 -->
      <el-row :gutter="20" style="margin-top: 20px;">
        <el-col :span="12">
          <el-card>
            <template #header>
              <span>日志级别分布</span>
            </template>
            <div ref="levelChart" style="height: 300px;"></div>
          </el-card>
        </el-col>
        <el-col :span="12">
          <el-card>
            <template #header>
              <span>时间趋势分析</span>
            </template>
            <div ref="trendChart" style="height: 300px;"></div>
          </el-card>
        </el-col>
      </el-row>

      <!-- 热门错误分析 -->
      <el-card style="margin-top: 20px;">
        <template #header>
          <span>热门错误分析</span>
        </template>
        <el-table :data="errorAnalysis" style="width: 100%">
          <el-table-column prop="error" label="错误信息" min-width="300"></el-table-column>
          <el-table-column prop="source" label="来源" width="90">
            <template #default="scope">
              <el-tag size="small" :type="scope.row.source === 'elk' ? 'warning' : 'primary'">
                {{ scope.row.source === 'elk' ? 'ELK' : '审计库' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="count" label="出现次数" width="120"></el-table-column>
          <el-table-column prop="percentage" label="占比" width="120">
            <template #default="scope">
              <el-progress :percentage="scope.row.percentage" :show-text="false"></el-progress>
              <span style="margin-left: 10px;">{{ scope.row.percentage }}%</span>
            </template>
          </el-table-column>
          <el-table-column prop="lastOccurrence" label="最后出现" width="180"></el-table-column>
        </el-table>
      </el-card>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onBeforeUnmount, nextTick } from 'vue'
import { ElMessage } from 'element-plus'
import * as echarts from 'echarts'
import { logAnalysisAPI, elkLogAPI } from '@/api/log/index.js'
import { unwrap, defaultDateRange } from '@/utils/logApi.js'

const loading = ref(false)
const levelChart = ref(null)
const trendChart = ref(null)
let levelChartInstance = null
let trendChartInstance = null

const stats = reactive({
  totalLogs: 0,
  errorLogs: 0,
  warningLogs: 0,
  infoLogs: 0,
  elkEnabled: false,
  elkError: ''
})

const errorAnalysis = ref([])

const renderLevelChart = (levelStats = {}) => {
  if (!levelChart.value) return
  if (!levelChartInstance) {
    levelChartInstance = echarts.init(levelChart.value)
  }
  const data = [
    { name: 'DEBUG', value: levelStats.DEBUG || 0 },
    { name: 'INFO', value: levelStats.INFO || 0 },
    { name: 'WARN', value: levelStats.WARN || 0 },
    { name: 'ERROR', value: levelStats.ERROR || 0 },
    { name: 'FATAL', value: levelStats.FATAL || 0 }
  ].filter((d) => d.value > 0)
  levelChartInstance.setOption({
    tooltip: { trigger: 'item' },
    legend: { bottom: 0 },
    series: [{
      type: 'pie',
      radius: ['40%', '65%'],
      data: data.length ? data : [{ name: '暂无 ELK 数据', value: 1 }]
    }]
  })
}

const renderTrendChart = (trendList = []) => {
  if (!trendChart.value) return
  if (!trendChartInstance) {
    trendChartInstance = echarts.init(trendChart.value)
  }
  trendChartInstance.setOption({
    tooltip: { trigger: 'axis' },
    xAxis: {
      type: 'category',
      data: trendList.map((i) => i.date)
    },
    yAxis: { type: 'value', minInterval: 1 },
    series: [{
      name: '审计操作日志',
      type: 'line',
      smooth: true,
      areaStyle: { opacity: 0.15 },
      data: trendList.map((i) => i.count)
    }]
  })
}

const loadOverview = async () => {
  const data = unwrap(await logAnalysisAPI.getOverview()) || {}
  stats.totalLogs = data.totalLogs ?? data.auditTotal ?? 0
  stats.errorLogs = data.errorLogs ?? data.errorToday ?? 0
  stats.warningLogs = data.warningLogs ?? 0
  stats.infoLogs = data.infoLogs ?? 0
  stats.elkEnabled = data.elkEnabled !== false
  stats.elkError = data.elkError || ''
}

const loadLevelStats = async () => {
  try {
    const [startTime, endTime] = defaultDateRange(24)
    const levelStats = unwrap(await elkLogAPI.levelStats({ startTime, endTime })) || {}
    await nextTick()
    renderLevelChart(levelStats)
  } catch (e) {
    console.warn('ELK 级别分布加载失败:', e.message)
    await nextTick()
    renderLevelChart({})
  }
}

const loadTrend = async () => {
  const trend = unwrap(await logAnalysisAPI.getTrend({ timeRange: 'week', logType: 'OPERATION' })) || []
  await nextTick()
  renderTrendChart(trend)
}

const loadTopErrors = async () => {
  const list = unwrap(await logAnalysisAPI.getTopErrors(10)) || []
  const totalCount = list.reduce((sum, item) => sum + (item.count || 0), 0)
  errorAnalysis.value = list.map((item) => ({
    error: item.errorType || item.error || '',
    count: item.count || 0,
    percentage: totalCount > 0 ? Math.round((item.count / totalCount) * 1000) / 10 : 0,
    lastOccurrence: item.lastOccurrence || '',
    source: item.source || 'audit'
  }))
}

const initPage = async () => {
  loading.value = true
  try {
    await Promise.all([loadOverview(), loadLevelStats(), loadTrend(), loadTopErrors()])
  } catch (error) {
    console.error('加载日志分析失败:', error)
    ElMessage.error(error.message || '加载日志分析失败，请稍后重试')
  } finally {
    loading.value = false
  }
}

const generateReport = async () => {
  ElMessage.info('正在刷新日志分析数据...')
  await initPage()
  ElMessage.success('日志分析数据已更新')
}

const handleResize = () => {
  levelChartInstance?.resize()
  trendChartInstance?.resize()
}

onMounted(() => {
  initPage()
  window.addEventListener('resize', handleResize)
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', handleResize)
  levelChartInstance?.dispose()
  trendChartInstance?.dispose()
})
</script>

<style scoped>
.log-analysis {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.stats-row {
  margin-bottom: 20px;
}

.stat-card {
  text-align: center;
}

.stat-content {
  padding: 20px;
}

.stat-value {
  font-size: 32px;
  font-weight: bold;
  color: #409EFF;
  margin-bottom: 8px;
}

.stat-value.error {
  color: #F56C6C;
}

.stat-value.warning {
  color: #E6A23C;
}

.stat-value.success {
  color: #67C23A;
}

.stat-label {
  font-size: 14px;
  color: #909399;
}
</style>
