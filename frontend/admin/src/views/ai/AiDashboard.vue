<template>
  <div class="ai-dashboard" v-loading="pageLoading">
    <div class="page-header">
      <h2>AI智能系统</h2>
      <div class="header-actions">
        <el-button v-if="hasPermission('shell:ai:dashboard:refresh')" type="primary" @click="refreshData">
          <el-icon><Refresh /></el-icon>
          刷新数据
        </el-button>
      </div>
    </div>

    <!-- 统计卡片 -->
    <el-row :gutter="20" class="stats-row">
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon">
              <el-icon><ChatDotRound /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-number">{{ stats.totalChats }}</div>
              <div class="stat-label">总对话次数</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon">
              <el-icon><MagicStick /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-number">{{ stats.totalGenerations }}</div>
              <div class="stat-label">智能生成次数</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon">
              <el-icon><Cpu /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-number">{{ stats.activeModels }}</div>
              <div class="stat-label">活跃模型数</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon">
              <el-icon><DocumentCopy /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-number">{{ stats.totalPrompts }}</div>
              <div class="stat-label">提示词模板</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 主要内容区域 -->
    <el-row :gutter="20" style="margin-top: 20px;">
      <!-- 左侧：AI分析和最近活动 -->
      <el-col :span="16">
        <!-- AI分析图表 -->
        <el-card class="ai-analysis">
          <template #header>
            <span>AI服务分析</span>
          </template>
          <div class="chart-container">
            <div class="chart-item" v-for="chart in aiCharts" :key="chart.id">
              <div class="chart-header">
                <h4>{{ chart.title }}</h4>
                <el-tag :type="chart.type" size="small">{{ chart.value }}</el-tag>
              </div>
              <div class="chart-content">
                <el-progress 
                  :percentage="chart.percentage" 
                  :status="getProgressStatus(chart.percentage)"
                  :stroke-width="8"
                />
              </div>
            </div>
          </div>
        </el-card>

        <!-- 最近AI活动 -->
        <el-card class="recent-activities" style="margin-top: 20px;">
          <template #header>
            <span>最近AI活动</span>
          </template>
          <div class="activity-list">
            <el-empty v-if="!recentActivities.length" description="暂无最近活动" :image-size="64" />
            <div 
              v-for="activity in recentActivities" 
              :key="activity.id" 
              class="activity-item"
            >
              <div class="activity-icon">
                <el-icon><component :is="activity.icon" /></el-icon>
              </div>
              <div class="activity-content">
                <div class="activity-title">{{ activity.title }}</div>
                <div class="activity-desc">{{ activity.description }}</div>
                <div class="activity-meta">
                  <span class="activity-time">{{ activity.time }}</span>
                </div>
              </div>
            </div>
          </div>
        </el-card>
      </el-col>

      <!-- 右侧：快速操作和模型状态 -->
      <el-col :span="8">
        <!-- 快速操作 -->
        <el-card class="quick-actions">
          <template #header>
            <span>快速操作</span>
          </template>
          <div class="action-buttons">
            <el-button type="primary" @click="navigateTo('/ai/chat')" block>
              <el-icon><ChatDotRound /></el-icon>
              AI对话
            </el-button>
            <el-button type="success" @click="navigateTo('/ai/generation')" block>
              <el-icon><MagicStick /></el-icon>
              智能生成
            </el-button>
            <el-button type="warning" @click="navigateTo('/ai/models')" block>
              <el-icon><Cpu /></el-icon>
              模型管理
            </el-button>
            <el-button type="info" @click="navigateTo('/ai/prompts')" block>
              <el-icon><DocumentCopy /></el-icon>
              提示词管理
            </el-button>
          </div>
        </el-card>

        <!-- 模型状态分布 -->
        <el-card class="model-status" style="margin-top: 20px;">
          <template #header>
            <span>模型状态分布</span>
          </template>
          <div class="status-chart">
            <div class="status-item" v-for="status in modelStatus" :key="status.name">
              <div class="status-info">
                <span class="status-name">{{ status.name }}</span>
                <span class="status-count">{{ status.count }}</span>
              </div>
              <div class="status-bar">
                <div 
                  class="status-fill" 
                  :style="{ 
                    width: (stats.activeModels ? (status.count / stats.activeModels * 100) : 0) + '%',
                    backgroundColor: status.color 
                  }"
                ></div>
              </div>
            </div>
          </div>
        </el-card>

        <!-- 快捷入口 -->
        <el-card class="upcoming-tasks" style="margin-top: 20px;">
          <template #header>
            <span>快捷入口</span>
          </template>
          <div class="task-list">
            <div class="task-item" v-for="link in quickLinks" :key="link.path">
              <div class="task-info">
                <div class="task-name">{{ link.title }}</div>
                <div class="task-title">{{ link.desc }}</div>
              </div>
              <el-button size="small" type="primary" @click="navigateTo(link.path)">进入</el-button>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 模型列表 -->
    <el-card class="model-list" style="margin-top: 20px;">
      <template #header>
        <span>活跃模型</span>
      </template>
      <el-table v-if="activeModels.length" :data="activeModels" style="width: 100%">
        <el-table-column prop="name" label="模型名称" min-width="200">
          <template #default="scope">
            <el-link type="primary" @click="viewModel(scope.row)">
              {{ scope.row.name }}
            </el-link>
          </template>
        </el-table-column>
        <el-table-column prop="type" label="模型类型" width="120">
          <template #default="scope">
            <el-tag :type="getModelTypeType(scope.row.type)" size="small">
              {{ getModelTypeText(scope.row.type) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="scope">
            <el-tag :type="getModelStatusType(scope.row.status)">
              {{ getModelStatusText(scope.row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="provider" label="提供商" width="120" />
        <el-table-column prop="version" label="版本" width="120" />
        <el-table-column prop="lastUsed" label="最后使用" width="120" />
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="scope">
            <el-button size="small" @click="editModel(scope.row)">编辑</el-button>
            <el-button size="small" type="success" @click="viewModel(scope.row)">查看</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-empty v-else description="暂无活跃模型，请先在模型管理中添加" />
    </el-card>
  </div>
</template>

<script setup>

// 使用 Element Plus 国际化系统
import { useI18n } from '@/i18n-element'
const { t } = useI18n()
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { 
  Refresh, ChatDotRound, MagicStick, Cpu, DocumentCopy
} from '@element-plus/icons-vue'
import { aiApi } from '@/api/ai/ai'
import { hasPermission } from '@/utils/permission'

const router = useRouter()
const pageLoading = ref(false)

// 统计数据
const stats = ref({
  totalChats: 0,
  totalGenerations: 0,
  activeModels: 0,
  totalPrompts: 0,
  totalModels: 0
})

const aiCharts = ref([])
const recentActivities = ref([])
const modelStatus = ref([])
const activeModels = ref([])

const quickLinks = [
  { path: '/ai/chat', title: 'AI 对话', desc: '与助手进行多轮对话' },
  { path: '/ai/generation', title: '智能生成', desc: '代码/文档/图像生成' },
  { path: '/ai/models', title: '模型管理', desc: '配置 DashScope 等模型' },
  { path: '/ai/prompts', title: '提示词', desc: '管理提示词模板' }
]

const formatActivityTime = (value) => {
  if (!value) return '-'
  const d = new Date(value)
  if (Number.isNaN(d.getTime())) return String(value)
  return d.toLocaleString('zh-CN', { month: '2-digit', day: '2-digit', hour: '2-digit', minute: '2-digit' })
}

const buildCharts = (data) => {
  const total = (data.totalChats ?? 0) + (data.totalGenerations ?? 0)
  const chatPct = total ? Math.min(100, Math.round(((data.totalChats ?? 0) / total) * 100)) : 0
  const genPct = total ? Math.min(100, Math.round(((data.totalGenerations ?? 0) / total) * 100)) : 0
  const modelPct = data.totalModels
    ? Math.min(100, Math.round(((data.activeModels ?? 0) / data.totalModels) * 100))
    : 0
  const promptPct = Math.min(100, (data.totalPrompts ?? 0) * 5)
  return [
    { id: 1, title: '对话占比', value: `${data.totalChats ?? 0} 次`, percentage: chatPct, type: 'success' },
    { id: 2, title: '生成占比', value: `${data.totalGenerations ?? 0} 次`, percentage: genPct, type: 'primary' },
    { id: 3, title: '模型启用率', value: `${data.activeModels ?? 0}/${data.totalModels ?? 0}`, percentage: modelPct, type: 'warning' },
    { id: 4, title: '提示词模板', value: `${data.totalPrompts ?? 0} 个`, percentage: promptPct, type: 'info' }
  ]
}

const buildActivities = (chats, generations, data) => {
  const items = (chats || []).slice(0, 3).map(c => ({
    id: `chat-${c.id}`,
    title: 'AI 对话',
    description: c.title || '对话记录',
    time: formatActivityTime(c.updateTime || c.createTime),
    icon: 'ChatDotRound'
  }))
  const genItems = (generations || []).slice(0, 4).map(g => ({
    id: `gen-${g.id}`,
    title: g.type === 'image' ? '图像生成' : g.type === 'chat' ? 'AI 对话' : '智能生成',
    description: (g.prompt || '').length > 40 ? `${g.prompt.slice(0, 40)}...` : (g.prompt || '生成记录'),
    time: formatActivityTime(g.createTime),
    icon: g.type === 'image' ? 'Picture' : 'MagicStick'
  }))
  return [...genItems, ...items].slice(0, 6)
}

// 方法
const refreshData = async () => {
  pageLoading.value = true
  try {
    const [statRes, modelRes, historyRes, genRes] = await Promise.all([
      aiApi.getStatistics(),
      aiApi.getList({ page: 1, limit: 10, status: 1 }),
      aiApi.getChatHistory({ page: 1, limit: 5 }),
      aiApi.getGenerationRecords({ page: 1, limit: 6 })
    ])
    if (statRes.code === 1 && statRes.data) {
      const data = statRes.data
      stats.value = {
        totalChats: data.totalChats ?? 0,
        totalGenerations: data.totalGenerations ?? 0,
        activeModels: data.activeModels ?? 0,
        totalPrompts: data.totalPrompts ?? 0,
        totalModels: data.totalModels ?? 0
      }
      aiCharts.value = buildCharts(data)
      modelStatus.value = [
        { name: '启用', count: data.activeModels ?? 0, color: '#67C23A' },
        { name: '禁用', count: Math.max(0, (data.totalModels ?? 0) - (data.activeModels ?? 0)), color: '#F56C6C' }
      ]
      const chats = historyRes.code === 1 && Array.isArray(historyRes.data) ? historyRes.data : []
      const generations = genRes.code === 1 && genRes.data?.list ? genRes.data.list : []
      recentActivities.value = buildActivities(chats, generations, data)
    }
    if (modelRes.code === 1 && modelRes.data?.list) {
      activeModels.value = modelRes.data.list
        .filter(m => m.status === 1)
        .map(m => ({
          id: m.id,
          name: m.name,
          type: m.type,
          status: 'online',
          provider: m.type,
          version: '-',
          lastUsed: m.updateTime || '-'
        }))
    } else {
      activeModels.value = []
    }
    ElMessage.success('数据已刷新')
  } catch (error) {
    ElMessage.error('刷新失败：' + (error.message || '未知错误'))
  } finally {
    pageLoading.value = false
  }
}

const navigateTo = (path) => {
  router.push(path)
}

const viewModel = () => {
  router.push('/ai/models')
}

const editModel = () => {
  router.push('/ai/models')
}

// 工具方法
const getProgressStatus = (percentage) => {
  if (percentage >= 80) return 'success'
  if (percentage >= 60) return 'warning'
  return '' // Element UI Progress组件的status属性不接受'normal'，使用空字符串
}

const getModelTypeType = (type) => {
  const typeMap = {
    text: 'primary',
    image: 'success',
    code: 'warning'
  }
  return typeMap[type] || 'info'
}

const getModelTypeText = (type) => {
  const typeMap = {
    text: '文本模型',
    image: '图像模型',
    code: '代码模型'
  }
  return typeMap[type] || type
}

const getModelStatusType = (status) => {
  const statusMap = {
    online: 'success',
    maintenance: 'warning',
    offline: 'danger'
  }
  return statusMap[status] || 'info'
}

const getModelStatusText = (status) => {
  const statusMap = {
    online: '在线',
    maintenance: '维护中',
    offline: '离线'
  }
  return statusMap[status] || status
}

// 生命周期
onMounted(() => {
  refreshData()
})
</script>

<style scoped>
.ai-dashboard {
  padding: 20px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.page-header h2 {
  margin: 0;
  color: #303133;
}

.header-actions {
  display: flex;
  gap: 10px;
}

.stats-row {
  margin-bottom: 20px;
}

.stat-card {
  height: 100px;
}

.stat-content {
  display: flex;
  align-items: center;
  height: 100%;
}

.stat-icon {
  font-size: 48px;
  color: #409eff;
  margin-right: 20px;
}

.stat-info {
  flex: 1;
}

.stat-number {
  font-size: 24px;
  font-weight: bold;
  color: #303133;
  margin-bottom: 5px;
}

.stat-label {
  color: #909399;
  font-size: 14px;
}

.ai-analysis {
  margin-bottom: 20px;
}

.chart-container {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 20px;
}

.chart-item {
  padding: 15px;
  border: 1px solid #f0f0f0;
  border-radius: 6px;
}

.chart-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
}

.chart-header h4 {
  margin: 0;
  color: #303133;
  font-size: 14px;
}

.recent-activities {
  margin-bottom: 20px;
}

.activity-list {
  max-height: 300px;
  overflow-y: auto;
}

.activity-item {
  display: flex;
  align-items: flex-start;
  padding: 12px 0;
  border-bottom: 1px solid #f0f0f0;
}

.activity-item:last-child {
  border-bottom: none;
}

.activity-icon {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  background-color: #f0f9ff;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 12px;
  color: #409eff;
  flex-shrink: 0;
}

.activity-content {
  flex: 1;
}

.activity-title {
  font-weight: 600;
  color: #303133;
  margin-bottom: 4px;
}

.activity-desc {
  color: #606266;
  font-size: 13px;
  margin-bottom: 8px;
  line-height: 1.4;
}

.activity-meta {
  display: flex;
  justify-content: space-between;
  font-size: 12px;
  color: #909399;
}

.quick-actions {
  margin-bottom: 20px;
}

.action-buttons {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.model-status {
  margin-bottom: 20px;
}

.status-item {
  margin-bottom: 15px;
}

.status-info {
  display: flex;
  justify-content: space-between;
  margin-bottom: 5px;
  font-size: 14px;
}

.status-name {
  color: #606266;
}

.status-count {
  color: #303133;
  font-weight: 600;
}

.status-bar {
  height: 8px;
  background-color: #f0f0f0;
  border-radius: 4px;
  overflow: hidden;
}

.status-fill {
  height: 100%;
  transition: width 0.3s ease;
}

.upcoming-tasks {
  margin-bottom: 20px;
}

.task-list {
  max-height: 200px;
  overflow-y: auto;
}

.task-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px 0;
  border-bottom: 1px solid #f0f0f0;
}

.task-item:last-child {
  border-bottom: none;
}

.task-info {
  flex: 1;
}

.task-name {
  font-weight: 600;
  color: #303133;
  margin-bottom: 4px;
}

.task-title {
  font-size: 12px;
  color: #606266;
  margin-bottom: 4px;
}

.task-date {
  font-size: 12px;
  color: #909399;
}

.task-date.urgent {
  color: #f56c6c;
  font-weight: 600;
}

.model-list {
  margin-bottom: 20px;
}

:deep(.el-table .el-table__row) {
  cursor: pointer;
}

:deep(.el-table .el-table__row:hover) {
  background-color: #f5f7fa;
}

/* 响应式设计 */
@media (max-width: 1200px) {
  .el-col-6 {
    width: 50%;
    margin-bottom: 20px;
  }
  
  .chart-container {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 768px) {
  .el-col-6 {
    width: 100%;
  }
  
  .page-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 15px;
  }
}
</style>
