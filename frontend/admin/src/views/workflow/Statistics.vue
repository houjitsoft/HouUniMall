<template>
  <div class="statistics-content">
    <div class="page-header">
      <h2>流程统计</h2>
      <p>查看工作流程的统计数据和趋势分析</p>
    </div>

    <!-- 统计概览 -->
    <div class="overview-cards">
      <div class="overview-card">
        <div class="card-icon">
          <el-icon :size="32"><Document /></el-icon>
        </div>
        <div class="card-content">
          <div class="card-number">{{ overview.totalProcesses }}</div>
          <div class="card-label">总流程数</div>
        </div>
      </div>
      
      <div class="overview-card">
        <div class="card-icon">
          <el-icon :size="32"><Edit /></el-icon>
        </div>
        <div class="card-content">
          <div class="card-number">{{ overview.totalApplications }}</div>
          <div class="card-label">总申请数</div>
        </div>
      </div>
      
      <div class="overview-card">
        <div class="card-icon">
          <el-icon :size="32"><Clock /></el-icon>
        </div>
        <div class="card-content">
          <div class="card-number">{{ overview.pendingTasks }}</div>
          <div class="card-label">待办任务</div>
        </div>
      </div>
      
      <div class="overview-card">
        <div class="card-icon">
          <el-icon :size="32"><TrendCharts /></el-icon>
        </div>
        <div class="card-content">
          <div class="card-number">{{ overview.approvalRate }}%</div>
          <div class="card-label">通过率</div>
        </div>
      </div>
    </div>

    <!-- 图表区域 -->
    <div class="charts-section">
      <el-row :gutter="24">
        <!-- 申请趋势图 -->
        <el-col :span="12">
          <div class="chart-card">
            <div class="chart-header">
              <h3>申请趋势</h3>
              <el-select v-model="trendPeriod" size="small" style="width: 120px">
                <el-option label="最近7天" value="7d" />
                <el-option label="最近30天" value="30d" />
                <el-option label="最近90天" value="90d" />
              </el-select>
            </div>
            <div class="chart-container">
              <div ref="trendChart" class="chart"></div>
            </div>
          </div>
        </el-col>

        <!-- 流程分布图 -->
        <el-col :span="12">
          <div class="chart-card">
            <div class="chart-header">
              <h3>流程分布</h3>
            </div>
            <div class="chart-container">
              <div ref="distributionChart" class="chart"></div>
            </div>
          </div>
        </el-col>
      </el-row>

      <el-row :gutter="24" style="margin-top: 24px">
        <!-- 部门统计 -->
        <el-col :span="12">
          <div class="chart-card">
            <div class="chart-header">
              <h3>部门申请统计</h3>
            </div>
            <div class="chart-container">
              <div ref="departmentChart" class="chart"></div>
            </div>
          </div>
        </el-col>

        <!-- 处理时长统计 -->
        <el-col :span="12">
          <div class="chart-card">
            <div class="chart-header">
              <h3>平均处理时长</h3>
            </div>
            <div class="chart-container">
              <div ref="durationChart" class="chart"></div>
            </div>
          </div>
        </el-col>
      </el-row>
    </div>

    <!-- 详细统计表格 -->
    <div class="table-section">
      <div class="table-header">
        <h3>详细统计</h3>
        <div class="table-actions">
          <el-button size="small">导出数据</el-button>
        </div>
      </div>
      
      <el-table :data="statisticsTable" style="width: 100%">
        <el-table-column prop="processName" label="流程名称" width="150" />
        <el-table-column prop="totalCount" label="申请总数" width="100" />
        <el-table-column prop="approvedCount" label="通过数" width="100" />
        <el-table-column prop="rejectedCount" label="驳回数" width="100" />
        <el-table-column prop="pendingCount" label="待处理" width="100" />
        <el-table-column prop="approvalRate" label="通过率" width="100">
          <template #default="{ row }">
            {{ row.approvalRate }}%
          </template>
        </el-table-column>
        <el-table-column prop="avgDuration" label="平均处理时长" width="120">
          <template #default="{ row }">
            {{ row.avgDuration }}小时
          </template>
        </el-table-column>
        <el-table-column prop="lastUpdate" label="最后更新" width="160">
          <template #default="{ row }">
            {{ formatDateTime(row.lastUpdate) }}
          </template>
        </el-table-column>
      </el-table>
    </div>
  </div>
</template>

<script setup>

// 使用 Element Plus 国际化系统
import { useI18n } from '@/i18n-element'
const { t } = useI18n()
import { ref, reactive, onMounted, nextTick } from 'vue'
import { Document, Edit, Clock, TrendCharts } from '@element-plus/icons-vue'
import * as echarts from 'echarts'

const trendPeriod = ref('7d')

// 概览数据
const overview = reactive({
  totalProcesses: 21,
  totalApplications: 156,
  pendingTasks: 8,
  approvalRate: 85.6
})

// 图表引用
const trendChart = ref()
const distributionChart = ref()
const departmentChart = ref()
const durationChart = ref()

// 统计表格数据
const statisticsTable = ref([
  {
    processName: '请假申请',
    totalCount: 45,
    approvedCount: 38,
    rejectedCount: 3,
    pendingCount: 4,
    approvalRate: 84.4,
    avgDuration: 2.5,
    lastUpdate: '2025-09-18 15:30:00'
  },
  {
    processName: '报销申请',
    totalCount: 32,
    approvedCount: 28,
    rejectedCount: 2,
    pendingCount: 2,
    approvalRate: 87.5,
    avgDuration: 4.2,
    lastUpdate: '2025-09-18 14:20:00'
  },
  {
    processName: '费用申请',
    totalCount: 28,
    approvedCount: 25,
    rejectedCount: 1,
    pendingCount: 2,
    approvalRate: 89.3,
    avgDuration: 3.8,
    lastUpdate: '2025-09-18 13:15:00'
  },
  {
    processName: '出差申请',
    totalCount: 15,
    approvedCount: 13,
    rejectedCount: 1,
    pendingCount: 1,
    approvalRate: 86.7,
    avgDuration: 5.2,
    lastUpdate: '2025-09-18 12:45:00'
  },
  {
    processName: '采购申请',
    totalCount: 36,
    approvedCount: 32,
    rejectedCount: 2,
    pendingCount: 2,
    approvalRate: 88.9,
    avgDuration: 6.1,
    lastUpdate: '2025-09-18 11:30:00'
  }
])

// 格式化日期时间
const formatDateTime = (dateTime) => {
  if (!dateTime) return '-'
  return new Date(dateTime).toLocaleString('zh-CN')
}

// 初始化图表
const initCharts = async () => {
  await nextTick()
  
  // 申请趋势图
  const trendChartInstance = echarts.init(trendChart.value)
  const trendOption = {
    title: {
      text: '申请趋势',
      left: 'center',
      textStyle: {
        fontSize: 14,
        fontWeight: 'normal'
      }
    },
    tooltip: {
      trigger: 'axis'
    },
    xAxis: {
      type: 'category',
      data: ['9/12', '9/13', '9/14', '9/15', '9/16', '9/17', '9/18']
    },
    yAxis: {
      type: 'value'
    },
    series: [
      {
        name: '申请数量',
        type: 'line',
        data: [12, 15, 18, 14, 16, 20, 22],
        smooth: true,
        itemStyle: {
          color: '#409eff'
        }
      }
    ]
  }
  trendChartInstance.setOption(trendOption)

  // 流程分布图
  const distributionChartInstance = echarts.init(distributionChart.value)
  const distributionOption = {
    title: {
      text: '流程分布',
      left: 'center',
      textStyle: {
        fontSize: 14,
        fontWeight: 'normal'
      }
    },
    tooltip: {
      trigger: 'item'
    },
    series: [
      {
        type: 'pie',
        radius: '60%',
        data: [
          { value: 45, name: '请假申请' },
          { value: 32, name: '报销申请' },
          { value: 28, name: '费用申请' },
          { value: 15, name: '出差申请' },
          { value: 36, name: '采购申请' }
        ],
        emphasis: {
          itemStyle: {
            shadowBlur: 10,
            shadowOffsetX: 0,
            shadowColor: 'rgba(0, 0, 0, 0.5)'
          }
        }
      }
    ]
  }
  distributionChartInstance.setOption(distributionOption)

  // 部门统计图
  const departmentChartInstance = echarts.init(departmentChart.value)
  const departmentOption = {
    title: {
      text: '部门申请统计',
      left: 'center',
      textStyle: {
        fontSize: 14,
        fontWeight: 'normal'
      }
    },
    tooltip: {
      trigger: 'axis',
      axisPointer: {
        type: 'shadow'
      }
    },
    xAxis: {
      type: 'category',
      data: ['技术部', '市场部', '销售部', '交付单元']
    },
    yAxis: {
      type: 'value'
    },
    series: [
      {
        name: '申请数量',
        type: 'bar',
        data: [45, 32, 28, 15],
        itemStyle: {
          color: '#67c23a'
        }
      }
    ]
  }
  departmentChartInstance.setOption(departmentOption)

  // 处理时长统计图
  const durationChartInstance = echarts.init(durationChart.value)
  const durationOption = {
    title: {
      text: '平均处理时长',
      left: 'center',
      textStyle: {
        fontSize: 14,
        fontWeight: 'normal'
      }
    },
    tooltip: {
      trigger: 'axis'
    },
    xAxis: {
      type: 'category',
      data: ['请假申请', '报销申请', '费用申请', '出差申请', '采购申请']
    },
    yAxis: {
      type: 'value',
      name: '小时'
    },
    series: [
      {
        name: '处理时长',
        type: 'bar',
        data: [2.5, 4.2, 3.8, 5.2, 6.1],
        itemStyle: {
          color: '#e6a23c'
        }
      }
    ]
  }
  durationChartInstance.setOption(durationOption)

  // 监听窗口大小变化
  window.addEventListener('resize', () => {
    trendChartInstance.resize()
    distributionChartInstance.resize()
    departmentChartInstance.resize()
    durationChartInstance.resize()
  })
}

// 初始化
onMounted(() => {
  initCharts()
})
</script>

<style scoped>
.statistics {
  background-color: #fff;
  border-radius: 8px;
  padding: 24px;
}

.page-header {
  margin-bottom: 24px;
}

.page-header h2 {
  margin: 0 0 8px 0;
  color: #303133;
  font-size: 24px;
  font-weight: 600;
}

.page-header p {
  margin: 0;
  color: #909399;
  font-size: 14px;
}

.overview-cards {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
  margin-bottom: 32px;
}

.overview-card {
  display: flex;
  align-items: center;
  padding: 20px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 8px;
  color: white;
}

.card-icon {
  margin-right: 16px;
}

.card-number {
  font-size: 24px;
  font-weight: bold;
  margin-bottom: 4px;
}

.card-label {
  font-size: 14px;
  opacity: 0.9;
}

.charts-section {
  margin-bottom: 32px;
}

.chart-card {
  background-color: #fff;
  border: 1px solid #e4e7ed;
  border-radius: 8px;
  padding: 20px;
}

.chart-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.chart-header h3 {
  margin: 0;
  color: #303133;
  font-size: 16px;
  font-weight: 600;
}

.chart-container {
  height: 300px;
}

.chart {
  width: 100%;
  height: 100%;
}

.table-section {
  background-color: #fff;
  border: 1px solid #e4e7ed;
  border-radius: 8px;
  padding: 20px;
}

.table-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.table-header h3 {
  margin: 0;
  color: #303133;
  font-size: 16px;
  font-weight: 600;
}
</style>
