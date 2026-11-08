<template>
  <div class="workflow-monitor">
    <el-card class="monitor-card">
      <template #header>
        <div class="card-header">
          <el-icon><Monitor /></el-icon>
          <span>流程监控</span>
          <div class="header-actions">
            <el-button @click="refreshData">
              <el-icon><Refresh /></el-icon>
              刷新数据
            </el-button>
            <el-button @click="exportReport">
              <el-icon><Download /></el-icon>
              导出报告
            </el-button>
          </div>
        </div>
      </template>
      
      <!-- 统计概览 -->
      <div class="stats-overview">
        <el-row :gutter="20">
          <el-col :span="6">
            <el-card class="stat-card">
              <div class="stat-content">
                <div class="stat-icon">
                  <el-icon size="32" color="#409eff"><List /></el-icon>
                </div>
                <div class="stat-info">
                  <div class="stat-number">{{ totalInstances }}</div>
                  <div class="stat-label">总实例数</div>
                </div>
              </div>
            </el-card>
          </el-col>
          
          <el-col :span="6">
            <el-card class="stat-card">
              <div class="stat-content">
                <div class="stat-icon">
                  <el-icon size="32" color="#67c23a"><Check /></el-icon>
                </div>
                <div class="stat-info">
                  <div class="stat-number">{{ completedInstances }}</div>
                  <div class="stat-label">已完成</div>
                </div>
              </div>
            </el-card>
          </el-col>
          
          <el-col :span="6">
            <el-card class="stat-card">
              <div class="stat-icon">
                <el-icon size="32" color="#e6a23c"><Clock /></el-icon>
              </div>
              <div class="stat-info">
                <div class="stat-number">{{ runningInstances }}</div>
                <div class="stat-label">运行中</div>
              </div>
            </el-card>
          </el-col>
          
          <el-col :span="6">
            <el-card class="stat-card">
              <div class="stat-content">
                <div class="stat-icon">
                  <el-icon size="32" color="#f56c6c"><Warning /></el-icon>
                </div>
                <div class="stat-info">
                  <div class="stat-number">{{ errorInstances }}</div>
                  <div class="stat-label">异常</div>
                </div>
              </div>
            </el-card>
          </el-col>
        </el-row>
      </div>

      <!-- 图表区域 -->
      <div class="charts-section">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-card>
              <template #header>
                <span>流程执行趋势</span>
              </template>
              <div class="chart-container">
                <div class="chart-placeholder">
                  <el-icon size="48"><TrendCharts /></el-icon>
                  <p>流程执行趋势图</p>
                  <p>显示最近7天的流程执行情况</p>
                </div>
              </div>
            </el-card>
          </el-col>
          
          <el-col :span="12">
            <el-card>
              <template #header>
                <span>流程完成率</span>
              </template>
              <div class="chart-container">
                <div class="chart-placeholder">
                  <el-icon size="48"><PieChart /></el-icon>
                  <p>流程完成率饼图</p>
                  <p>显示各流程的完成情况</p>
                </div>
              </div>
            </el-card>
          </el-col>
        </el-row>
      </div>

      <!-- 性能指标 -->
      <div class="performance-section">
        <el-card>
          <template #header>
            <span>性能指标</span>
          </template>
          <el-row :gutter="20">
            <el-col :span="8">
              <div class="metric-item">
                <div class="metric-label">平均处理时间</div>
                <div class="metric-value">{{ averageProcessTime }}</div>
                <div class="metric-unit">小时</div>
              </div>
            </el-col>
            <el-col :span="8">
              <div class="metric-item">
                <div class="metric-label">平均任务数</div>
                <div class="metric-value">{{ averageTaskCount }}</div>
                <div class="metric-unit">个</div>
              </div>
            </el-col>
            <el-col :span="8">
              <div class="metric-item">
                <div class="metric-label">超时率</div>
                <div class="metric-value">{{ timeoutRate }}</div>
                <div class="metric-unit">%</div>
              </div>
            </el-col>
          </el-row>
        </el-card>
      </div>

      <!-- 流程统计表格 -->
      <div class="workflow-stats">
        <el-card>
          <template #header>
            <span>流程统计详情</span>
          </template>
          <el-table :data="workflowStats" stripe>
            <el-table-column prop="workflowName" label="流程名称" width="150" />
            <el-table-column prop="workflowKey" label="流程标识" width="120" />
            <el-table-column prop="totalInstances" label="总实例数" width="100" />
            <el-table-column prop="completedInstances" label="已完成" width="100" />
            <el-table-column prop="runningInstances" label="运行中" width="100" />
            <el-table-column prop="completionRate" label="完成率" width="100">
              <template #default="{ row }">
                <el-progress :percentage="row.completionRate" :color="getProgressColor(row.completionRate)" />
              </template>
            </el-table-column>
            <el-table-column prop="averageTime" label="平均耗时" width="120" />
            <el-table-column prop="lastRunTime" label="最后运行" width="160" />
            <el-table-column label="操作" width="120">
              <template #default="{ row }">
                <el-button type="primary" size="small" @click="viewWorkflowDetail(row)">
                  详情
                </el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </div>

      <!-- 异常监控 -->
      <div class="error-monitor">
        <el-card>
          <template #header>
            <span>异常监控</span>
            <el-badge :value="errorCount" class="error-badge" />
          </template>
          <el-table :data="errorList" stripe>
            <el-table-column prop="instanceId" label="实例ID" width="120" />
            <el-table-column prop="workflowName" label="流程名称" width="150" />
            <el-table-column prop="errorType" label="异常类型" width="120">
              <template #default="{ row }">
                <el-tag :type="getErrorTagType(row.errorType)">
                  {{ getErrorText(row.errorType) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="errorMessage" label="异常信息" min-width="200" />
            <el-table-column prop="occurTime" label="发生时间" width="160" />
            <el-table-column prop="status" label="处理状态" width="100">
              <template #default="{ row }">
                <el-tag :type="getStatusTagType(row.status)">
                  {{ getStatusText(row.status) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="120">
              <template #default="{ row }">
                <el-button type="primary" size="small" @click="handleError(row)">
                  处理
                </el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </div>
    </el-card>
  </div>
</template>

<script setup>

// 使用 Element Plus 国际化系统
import { useI18n } from '@/i18n-element'
const { t } = useI18n()
import { ref, onMounted, computed } from 'vue'
import { Monitor, Refresh, Download, List, Check, Clock, Warning, TrendCharts, PieChart } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'

/**
 * 流程监控页面
 * 
 * @author Amos
 * @since 2025-01-09
 */

// 响应式数据
const totalInstances = ref(156)
const completedInstances = ref(128)
const runningInstances = ref(25)
const errorInstances = ref(3)

const averageProcessTime = ref(4.5)
const averageTaskCount = ref(3.2)
const timeoutRate = ref(2.8)

// 流程统计数据
const workflowStats = ref([
  {
    workflowName: '请假申请流程',
    workflowKey: 'leave_request',
    totalInstances: 45,
    completedInstances: 42,
    runningInstances: 3,
    completionRate: 93,
    averageTime: '2.5小时',
    lastRunTime: '2025-01-09 14:30:00'
  },
  {
    workflowName: '采购审批流程',
    workflowKey: 'purchase_approval',
    totalInstances: 32,
    completedInstances: 28,
    runningInstances: 4,
    completionRate: 88,
    averageTime: '1.8小时',
    lastRunTime: '2025-01-09 16:20:00'
  },
  {
    workflowName: '合同审批流程',
    workflowKey: 'contract_approval',
    totalInstances: 28,
    completedInstances: 25,
    runningInstances: 2,
    completionRate: 89,
    averageTime: '3.2小时',
    lastRunTime: '2025-01-09 11:15:00'
  },
  {
    workflowName: '项目立项流程',
    workflowKey: 'project_approval',
    totalInstances: 18,
    completedInstances: 15,
    runningInstances: 3,
    completionRate: 83,
    averageTime: '5.5小时',
    lastRunTime: '2025-01-09 09:45:00'
  },
  {
    workflowName: '报销审批流程',
    workflowKey: 'expense_approval',
    totalInstances: 33,
    completedInstances: 31,
    runningInstances: 2,
    completionRate: 94,
    averageTime: '1.2小时',
    lastRunTime: '2025-01-09 17:10:00'
  }
])

// 异常数据
const errorList = ref([
  {
    instanceId: 'WF001',
    workflowName: '请假申请流程',
    errorType: 'timeout',
    errorMessage: '任务超时：部门经理审批超过24小时未处理',
    occurTime: '2025-01-09 10:30:00',
    status: 'pending'
  },
  {
    instanceId: 'WF015',
    workflowName: '采购审批流程',
    errorType: 'exception',
    errorMessage: '系统异常：无法获取审批人信息',
    occurTime: '2025-01-09 14:20:00',
    status: 'handled'
  },
  {
    instanceId: 'WF028',
    workflowName: '合同审批流程',
    errorType: 'reject',
    errorMessage: '流程被拒绝：法务审核不通过',
    occurTime: '2025-01-09 16:45:00',
    status: 'pending'
  }
])

// 计算属性
const errorCount = computed(() => errorList.value.filter(item => item.status === 'pending').length)

// 辅助函数
const getProgressColor = (percentage) => {
  if (percentage >= 90) return '#67c23a'
  if (percentage >= 70) return '#e6a23c'
  return '#f56c6c'
}

const getErrorTagType = (errorType) => {
  const typeMap = {
    'timeout': 'warning',
    'exception': 'danger',
    'reject': 'info'
  }
  return typeMap[errorType] || 'info'
}

const getErrorText = (errorType) => {
  const textMap = {
    'timeout': '超时',
    'exception': '异常',
    'reject': '拒绝'
  }
  return textMap[errorType] || errorType
}

const getStatusTagType = (status) => {
  const typeMap = {
    'pending': 'warning',
    'handled': 'success',
    'ignored': 'info'
  }
  return typeMap[status] || 'info'
}

const getStatusText = (status) => {
  const textMap = {
    'pending': '待处理',
    'handled': '已处理',
    'ignored': '已忽略'
  }
  return textMap[status] || status
}

// 事件处理
const refreshData = () => {
  ElMessage.success('数据刷新成功')
}

const exportReport = () => {
  ElMessage.info('导出报告功能开发中...')
}

const viewWorkflowDetail = (row) => {
  ElMessage.info(`查看流程详情: ${row.workflowName}`)
}

const handleError = (row) => {
  ElMessage.info(`处理异常: ${row.errorMessage}`)
}

onMounted(() => {
})
</script>

<style scoped>
.workflow-monitor {
  padding: 20px;
  background-color: #f5f5f5;
  min-height: 100vh;
}

.monitor-card {
  max-width: 1400px;
  margin: 0 auto;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.card-header {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 18px;
  font-weight: 500;
  color: #303133;
}

.header-actions {
  margin-left: auto;
  display: flex;
  gap: 8px;
}

.stats-overview {
  margin-bottom: 30px;
}

.stat-card {
  border-radius: 8px;
  transition: all 0.3s;
}

.stat-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px 0 rgba(0, 0, 0, 0.15);
}

.stat-content {
  display: flex;
  align-items: center;
  padding: 10px 0;
}

.stat-icon {
  margin-right: 15px;
}

.stat-info {
  flex: 1;
}

.stat-number {
  font-size: 28px;
  font-weight: bold;
  color: #303133;
  line-height: 1;
  margin-bottom: 5px;
}

.stat-label {
  font-size: 14px;
  color: #909399;
}

.charts-section {
  margin-bottom: 30px;
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

.chart-placeholder p {
  margin: 10px 0 0 0;
  font-size: 14px;
}

.performance-section {
  margin-bottom: 30px;
}

.metric-item {
  text-align: center;
  padding: 20px 0;
}

.metric-label {
  font-size: 14px;
  color: #606266;
  margin-bottom: 10px;
}

.metric-value {
  font-size: 32px;
  font-weight: bold;
  color: #409eff;
  line-height: 1;
  margin-bottom: 5px;
}

.metric-unit {
  font-size: 12px;
  color: #909399;
}

.workflow-stats {
  margin-bottom: 30px;
}

.error-monitor {
  margin-bottom: 30px;
}

.error-badge {
  margin-left: 10px;
}

/* 表格样式优化 */
:deep(.el-table) {
  border-radius: 8px;
  overflow: hidden;
}

:deep(.el-table th) {
  background-color: #f8f9fa;
  color: #606266;
  font-weight: 600;
}

:deep(.el-table tr:hover) {
  background-color: #f5f7fa;
}

/* 标签样式优化 */
:deep(.el-tag) {
  border-radius: 4px;
  font-weight: 500;
}

/* 按钮样式优化 */
:deep(.el-button) {
  border-radius: 4px;
  font-weight: 500;
}

/* 进度条样式优化 */
:deep(.el-progress-bar__outer) {
  border-radius: 10px;
}

:deep(.el-progress-bar__inner) {
  border-radius: 10px;
}

/* 卡片样式优化 */
:deep(.el-card__header) {
  background-color: #f8f9fa;
  border-bottom: 1px solid #e4e7ed;
  font-weight: 600;
}

:deep(.el-card__body) {
  padding: 20px;
}
</style>
