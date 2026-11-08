<template>
  <div class="system-performance">
    <el-card class="box-card">
      <template #header>
        <div class="card-header">
          <span>性能监控</span>
          <div class="header-actions">
            <el-select v-model="timeRange" @change="changeTimeRange" style="width: 120px; margin-right: 10px;">
              <el-option label="最近1小时" value="1h" />
              <el-option label="最近6小时" value="6h" />
              <el-option label="最近12小时" value="12h" />
              <el-option label="最近24小时" value="24h" />
              <el-option label="最近7天" value="7d" />
            </el-select>
            <el-button @click="refreshData">刷新</el-button>
          </div>
        </div>
      </template>
      
      <el-tabs v-model="activeTab">
        <el-tab-pane label="系统概览" name="overview">
          <div class="tab-content">
            <el-row :gutter="20">
              <el-col :span="6">
                <el-card>
                  <template #header>
                    <div class="card-header">
                      <span>CPU使用率</span>
                    </div>
                  </template>
                  <div class="metric-value">{{ cpuUsage }}%</div>
                  <div class="metric-desc">当前CPU使用率</div>
                  <el-progress :percentage="cpuUsage" :status="cpuUsage > 80 ? 'exception' : 'success'" />
                </el-card>
              </el-col>
              <el-col :span="6">
                <el-card>
                  <template #header>
                    <div class="card-header">
                      <span>内存使用</span>
                    </div>
                  </template>
                  <div class="metric-value">{{ memoryUsage }}%</div>
                  <div class="metric-desc">当前内存使用率</div>
                  <el-progress :percentage="memoryUsage" :status="memoryUsage > 80 ? 'exception' : 'success'" />
                </el-card>
              </el-col>
              <el-col :span="6">
                <el-card>
                  <template #header>
                    <div class="card-header">
                      <span>磁盘使用</span>
                    </div>
                  </template>
                  <div class="metric-value">{{ diskUsage }}%</div>
                  <div class="metric-desc">当前磁盘使用率</div>
                  <el-progress :percentage="diskUsage" :status="diskUsage > 90 ? 'exception' : 'success'" />
                </el-card>
              </el-col>
              <el-col :span="6">
                <el-card>
                  <template #header>
                    <div class="card-header">
                      <span>网络流量</span>
                    </div>
                  </template>
                  <div class="metric-value">{{ networkUsage }}</div>
                  <div class="metric-desc">当前网络流量</div>
                  <div class="network-indicator">
                    <div class="network-up">↑ {{ networkUp }}</div>
                    <div class="network-down">↓ {{ networkDown }}</div>
                  </div>
                </el-card>
              </el-col>
            </el-row>
            
            <div class="charts-container">
              <div class="chart-row">
                <div class="chart-item">
                  <div class="chart-title">CPU使用率趋势</div>
                  <div ref="cpuChartRef" class="chart-wrapper"></div>
                </div>
                <div class="chart-item">
                  <div class="chart-title">内存使用趋势</div>
                  <div ref="memoryChartRef" class="chart-wrapper"></div>
                </div>
              </div>
              <div class="chart-row">
                <div class="chart-item">
                  <div class="chart-title">磁盘IO</div>
                  <div ref="diskChartRef" class="chart-wrapper"></div>
                </div>
                <div class="chart-item">
                  <div class="chart-title">网络流量</div>
                  <div ref="networkChartRef" class="chart-wrapper"></div>
                </div>
              </div>
            </div>
          </div>
        </el-tab-pane>
        
        <el-tab-pane label="JVM监控" name="jvm">
          <div class="tab-content">
            <el-row :gutter="20">
              <el-col :span="8">
                <el-card>
                  <template #header>
                    <div class="card-header">
                      <span>堆内存</span>
                    </div>
                  </template>
                  <div class="metric-value">{{ jvmMetrics.heapUsed }}</div>
                  <div class="metric-desc">已使用堆内存</div>
                  <el-progress :percentage="jvmMetrics.heapUsage" :status="jvmMetrics.heapUsage > 80 ? 'exception' : 'success'" />
                </el-card>
              </el-col>
              <el-col :span="8">
                <el-card>
                  <template #header>
                    <div class="card-header">
                      <span>非堆内存</span>
                    </div>
                  </template>
                  <div class="metric-value">{{ jvmMetrics.nonHeapUsed }}</div>
                  <div class="metric-desc">已使用非堆内存</div>
                  <el-progress :percentage="jvmMetrics.nonHeapUsage" :status="jvmMetrics.nonHeapUsage > 80 ? 'exception' : 'success'" />
                </el-card>
              </el-col>
              <el-col :span="8">
                <el-card>
                  <template #header>
                    <div class="card-header">
                      <span>GC统计</span>
                    </div>
                  </template>
                  <div class="metric-value">{{ jvmMetrics.gcCount }}</div>
                  <div class="metric-desc">GC总次数</div>
                  <div class="gc-stats">
                    <div>Young GC: {{ jvmMetrics.youngGC }}</div>
                    <div>Full GC: {{ jvmMetrics.fullGC }}</div>
                  </div>
                </el-card>
              </el-col>
            </el-row>
            
            <div class="jvm-charts">
              <div class="chart-item">
                <div class="chart-title">JVM内存使用趋势</div>
                <div ref="jvmChartRef" class="chart-wrapper"></div>
              </div>
            </div>
          </div>
        </el-tab-pane>
        
        <el-tab-pane label="数据库监控" name="database">
          <div class="tab-content">
            <el-row :gutter="20">
              <el-col :span="8">
                <el-card>
                  <template #header>
                    <div class="card-header">
                      <span>连接数</span>
                    </div>
                  </template>
                  <div class="metric-value">{{ dbMetrics.connections }}</div>
                  <div class="metric-desc">当前连接数</div>
                  <el-progress :percentage="dbMetrics.connectionUsage" :status="dbMetrics.connectionUsage > 80 ? 'exception' : 'success'" />
                </el-card>
              </el-col>
              <el-col :span="8">
                <el-card>
                  <template #header>
                    <div class="card-header">
                      <span>QPS</span>
                    </div>
                  </template>
                  <div class="metric-value">{{ dbMetrics.qps }}</div>
                  <div class="metric-desc">每秒查询数</div>
                </el-card>
              </el-col>
              <el-col :span="8">
                <el-card>
                  <template #header>
                    <div class="card-header">
                      <span>慢查询</span>
                    </div>
                  </template>
                  <div class="metric-value">{{ dbMetrics.slowQueries }}</div>
                  <div class="metric-desc">慢查询数量</div>
                </el-card>
              </el-col>
            </el-row>
            
            <div class="db-charts">
              <div class="chart-item">
                <div class="chart-title">数据库性能趋势</div>
                <div ref="dbChartRef" class="chart-wrapper"></div>
              </div>
            </div>
          </div>
        </el-tab-pane>
      </el-tabs>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onUnmounted } from 'vue'
import { ElMessage } from 'element-plus'

const activeTab = ref('overview')
const timeRange = ref('1h')

// 系统指标
const cpuUsage = ref(45)
const memoryUsage = ref(62)
const diskUsage = ref(75)
const networkUsage = ref('15.2 Mbps')
const networkUp = ref('8.5 Mbps')
const networkDown = ref('6.7 Mbps')

// JVM指标
const jvmMetrics = reactive({
  heapUsed: '256MB',
  heapUsage: 65,
  nonHeapUsed: '128MB',
  nonHeapUsage: 45,
  gcCount: 125,
  youngGC: 100,
  fullGC: 25
})

// 数据库指标
const dbMetrics = reactive({
  connections: 15,
  connectionUsage: 30,
  qps: 1250,
  slowQueries: 3
})

// 图表引用
const cpuChartRef = ref(null)
const memoryChartRef = ref(null)
const diskChartRef = ref(null)
const networkChartRef = ref(null)
const jvmChartRef = ref(null)
const dbChartRef = ref(null)

let timer = null

const changeTimeRange = (value) => {
  ElMessage.info(`切换时间范围到: ${value}`)
}

const refreshData = () => {
  ElMessage.success('性能数据已刷新')
}

const updateMetrics = () => {
  // 模拟更新指标数据
  cpuUsage.value = Math.floor(Math.random() * 30) + 30
  memoryUsage.value = Math.floor(Math.random() * 20) + 50
  diskUsage.value = Math.floor(Math.random() * 10) + 70
}

const initCharts = () => {
  // 初始化图表
  console.log('初始化性能监控图表')
}

onMounted(() => {
  initCharts()
  timer = setInterval(updateMetrics, 5000)
})

onUnmounted(() => {
  if (timer) {
    clearInterval(timer)
  }
})
</script>

<style scoped>
.system-performance {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-actions {
  display: flex;
  align-items: center;
}

.tab-content {
  padding: 20px 0;
}

.metric-value {
  font-size: 24px;
  font-weight: bold;
  text-align: center;
  color: #303133;
}

.metric-desc {
  font-size: 14px;
  text-align: center;
  color: #909399;
  margin: 10px 0;
}

.network-indicator {
  display: flex;
  justify-content: space-around;
  margin-top: 10px;
}

.network-up {
  color: #67c23a;
}

.network-down {
  color: #409eff;
}

.charts-container {
  margin-top: 20px;
}

.chart-row {
  display: flex;
  gap: 20px;
  margin-bottom: 20px;
}

.chart-item {
  flex: 1;
}

.chart-title {
  font-size: 16px;
  font-weight: 500;
  margin-bottom: 10px;
  color: #303133;
}

.chart-wrapper {
  height: 300px;
  background: #f5f7fa;
  border-radius: 4px;
}

.jvm-charts,
.db-charts {
  margin-top: 20px;
}

.gc-stats {
  font-size: 12px;
  color: #909399;
  margin-top: 10px;
}
</style>