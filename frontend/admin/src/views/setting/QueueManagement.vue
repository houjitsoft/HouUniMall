<template>
  <div class="queue-page">
    <!-- 页头 -->
    <header class="queue-hero">
      <div class="hero-text">
        <h1 class="hero-title">队列管理</h1>
        <p class="hero-subtitle">全平台异步任务队列，实时掌握执行、待办与失败重试</p>
      </div>
      <div class="hero-actions">
        <el-button :icon="Refresh" :loading="loading" circle class="hero-refresh" @click="handleRefresh" />
        <el-button
          v-if="!isMobile"
          type="danger"
          plain
          :disabled="selectedIds.length === 0"
          @click="handleBatchCancel"
        >
          批量取消{{ selectedIds.length ? ` (${selectedIds.length})` : '' }}
        </el-button>
      </div>
    </header>

    <!-- KPI 统计 -->
    <section class="kpi-grid">
      <button
        v-for="item in kpiItems"
        :key="item.key"
        type="button"
        class="kpi-card"
        :class="[`kpi-card--${item.key}`, { 'is-active': activeTab === item.filterValue }]"
        @click="setStatusFilter(item.filterValue)"
      >
        <span class="kpi-icon" :style="{ background: item.gradient }">
          <el-icon><component :is="item.icon" /></el-icon>
        </span>
        <span class="kpi-body">
          <span class="kpi-label">{{ item.label }}</span>
          <span class="kpi-value">{{ item.value }}</span>
        </span>
      </button>
    </section>

    <!-- 模块筛选 -->
    <section class="chip-section">
      <div class="chip-section__label">模块</div>
      <div class="chip-scroll">
        <button
          type="button"
          class="chip"
          :class="{ 'is-active': activeModuleTab === '' }"
          @click="setModuleFilter('')"
        >
          全部
          <span class="chip-count">{{ totalAll }}</span>
        </button>
        <button
          v-for="m in moduleList"
          :key="m.value"
          type="button"
          class="chip"
          :class="{ 'is-active': activeModuleTab === m.value }"
          @click="setModuleFilter(m.value)"
        >
          {{ m.label }}
          <span v-if="m.count" class="chip-count">{{ m.count }}</span>
        </button>
      </div>
    </section>

    <!-- 状态筛选 -->
    <section class="chip-section chip-section--status">
      <div class="chip-scroll">
        <button
          v-for="s in statusChips"
          :key="s.value"
          type="button"
          class="chip chip--status"
          :class="[`chip--${s.value || 'all'}`, { 'is-active': activeTab === s.value }]"
          @click="setStatusFilter(s.value)"
        >
          {{ s.label }}
          <span v-if="s.count != null" class="chip-count">{{ s.count }}</span>
        </button>
      </div>
    </section>

    <!-- 工具栏 -->
    <section class="toolbar-card">
      <div class="toolbar-row">
        <el-input
          v-model="filter.keyword"
          placeholder="搜索队列名称"
          clearable
          class="toolbar-search"
          @keyup.enter="handleSearch"
          @clear="handleSearch"
        >
          <template #prefix><el-icon><Search /></el-icon></template>
        </el-input>
        <el-button type="primary" :icon="Search" @click="handleSearch">查询</el-button>
        <el-button v-if="isMobile" :icon="Filter" @click="showMobileFilter = true">筛选</el-button>
      </div>
      <div v-if="!isMobile" class="toolbar-row toolbar-row--advanced">
        <el-select v-model="filter.module" placeholder="所属模块" clearable class="toolbar-select" @change="syncModuleFromSelect">
          <el-option label="全部模块" value="" />
          <el-option v-for="m in moduleList" :key="m.value" :label="m.label" :value="m.value" />
        </el-select>
        <el-select v-model="filter.priority" placeholder="优先级" clearable class="toolbar-select" @change="handleSearch">
          <el-option label="全部优先级" value="" />
          <el-option label="高" value="high" />
          <el-option label="中" value="medium" />
          <el-option label="低" value="low" />
        </el-select>
      </div>
    </section>

    <!-- PC 表格 -->
    <section v-if="!isMobile" class="list-panel">
      <div class="list-panel__head">
        <span class="list-panel__title">队列列表</span>
        <el-tag type="info" effect="plain" round>共 {{ pagination.total }} 条</el-tag>
      </div>
      <el-table
        v-loading="loading"
        :data="filteredQueueList"
        stripe
        class="queue-table"
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="48" />
        <el-table-column prop="id" label="ID" width="72" sortable />
        <el-table-column prop="name" label="队列名称" min-width="200" show-overflow-tooltip />
        <el-table-column prop="module" label="模块" width="120">
          <template #default="{ row }">
            <el-tag size="small" effect="plain">{{ getModuleLabel(row.module) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="108">
          <template #default="{ row }">
            <el-tag :type="getStatusTagType(row.status)" size="small" effect="light" round>
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="priority" label="优先级" width="88">
          <template #default="{ row }">
            <el-tag :type="getPriorityTagType(row.priority)" size="small" effect="dark" round>
              {{ getPriorityText(row.priority) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="progress" label="进度" width="160">
          <template #default="{ row }">
            <el-progress :percentage="row.progress" :status="getProgressStatus(row.status)" :stroke-width="8" />
          </template>
        </el-table-column>
        <el-table-column prop="retryCount" label="重试" width="72" align="center" />
        <el-table-column prop="createdAt" label="创建时间" width="168" sortable />
        <el-table-column label="操作" width="220" fixed="right">
          <template #default="{ row }">
            <div class="table-actions">
              <el-button link type="primary" size="small" @click="handleViewDetail(row)">详情</el-button>
              <el-button
                v-if="row.status === 'running' || row.status === 'pending'"
                link type="warning" size="small"
                @click="handleCancel(row)"
              >取消</el-button>
              <el-button v-if="row.status === 'failed'" link type="success" size="small" @click="handleRetry(row)">重试</el-button>
              <el-button v-if="row.status === 'pending'" link type="primary" size="small" @click="handleExecuteNow(row)">执行</el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>
      <el-empty v-if="!loading && filteredQueueList.length === 0" description="暂无队列数据" />
      <div class="pager">
        <el-pagination
          v-model:current-page="pagination.page"
          v-model:page-size="pagination.pageSize"
          :page-sizes="[10, 20, 50, 100]"
          :total="pagination.total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="loadQueueList"
          @current-change="loadQueueList"
        />
      </div>
    </section>

    <!-- 移动端卡片列表 -->
    <section v-else class="mobile-list" v-loading="loading">
      <el-empty v-if="!loading && filteredQueueList.length === 0" description="暂无队列" />
      <article
        v-for="row in filteredQueueList"
        :key="row.id"
        class="queue-card"
        :class="`queue-card--${row.status}`"
        @click="handleViewDetail(row)"
      >
        <div class="queue-card__top">
          <el-checkbox
            :model-value="selectedIds.includes(row.id)"
            class="queue-card__check"
            @click.stop
            @change="(val) => toggleSelect(row.id, val)"
          />
          <div class="queue-card__main">
            <div class="queue-card__title-row">
              <span class="queue-card__name">{{ row.name }}</span>
              <el-tag :type="getPriorityTagType(row.priority)" size="small" effect="dark" round>
                {{ getPriorityText(row.priority) }}
              </el-tag>
            </div>
            <div class="queue-card__meta">
              <el-tag :type="getStatusTagType(row.status)" size="small" effect="light" round>
                {{ getStatusText(row.status) }}
              </el-tag>
              <span class="queue-card__module">{{ getModuleLabel(row.module) }}</span>
            </div>
          </div>
        </div>
        <div class="queue-card__progress" @click.stop>
          <div class="progress-label">
            <span>执行进度</span>
            <span>{{ row.progress }}%</span>
          </div>
          <el-progress
            :percentage="row.progress"
            :status="getProgressStatus(row.status)"
            :stroke-width="6"
            :show-text="false"
          />
        </div>
        <div class="queue-card__footer">
          <span class="queue-card__time">{{ row.createdAt }}</span>
          <span v-if="row.retryCount" class="queue-card__retry">重试 {{ row.retryCount }}</span>
        </div>
        <div class="queue-card__actions" @click.stop>
          <el-button
            v-if="row.status === 'running' || row.status === 'pending'"
            size="small"
            round
            @click="handleCancel(row)"
          >取消</el-button>
          <el-button v-if="row.status === 'failed'" type="success" size="small" round @click="handleRetry(row)">重试</el-button>
          <el-button v-if="row.status === 'pending'" type="primary" size="small" round @click="handleExecuteNow(row)">立即执行</el-button>
          <el-button size="small" round plain @click="handleViewDetail(row)">详情</el-button>
        </div>
      </article>
      <div class="mobile-pager">
        <el-pagination
          v-model:current-page="pagination.page"
          v-model:page-size="pagination.pageSize"
          :total="pagination.total"
          layout="prev, pager, next"
          small
          @current-change="loadQueueList"
        />
      </div>
    </section>

    <!-- 移动端批量操作条 -->
    <transition name="slide-up">
      <div v-if="isMobile && selectedIds.length > 0" class="mobile-batch-bar">
        <span>已选 {{ selectedIds.length }} 项</span>
        <el-button type="danger" size="small" round @click="handleBatchCancel">批量取消</el-button>
        <el-button size="small" round plain @click="clearSelection">清空</el-button>
      </div>
    </transition>

    <!-- 详情抽屉 -->
    <el-drawer
      v-model="drawerVisible"
      title="队列详情"
      :size="isMobile ? '100%' : '480px'"
      :direction="isMobile ? 'btt' : 'rtl'"
      class="queue-drawer"
    >
      <template v-if="currentQueue">
        <div class="detail-hero" :class="`detail-hero--${currentQueue.status}`">
          <el-tag :type="getStatusTagType(currentQueue.status)" effect="dark" round>
            {{ getStatusText(currentQueue.status) }}
          </el-tag>
          <h3 class="detail-name">{{ currentQueue.name }}</h3>
          <p class="detail-module">{{ getModuleLabel(currentQueue.module) }}</p>
          <el-progress
            :percentage="currentQueue.progress"
            :status="getProgressStatus(currentQueue.status)"
            :stroke-width="10"
          />
        </div>
        <el-descriptions :column="1" border class="detail-desc">
          <el-descriptions-item label="队列 ID">{{ currentQueue.id }}</el-descriptions-item>
          <el-descriptions-item label="优先级">
            <el-tag :type="getPriorityTagType(currentQueue.priority)" size="small" effect="dark" round>
              {{ getPriorityText(currentQueue.priority) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="重试次数">{{ currentQueue.retryCount }}</el-descriptions-item>
          <el-descriptions-item label="创建时间">{{ currentQueue.createdAt }}</el-descriptions-item>
          <el-descriptions-item label="更新时间">{{ currentQueue.updatedAt }}</el-descriptions-item>
          <el-descriptions-item v-if="currentQueue.errorMsg" label="错误信息">
            <span class="error-msg">{{ currentQueue.errorMsg }}</span>
          </el-descriptions-item>
        </el-descriptions>
        <div v-if="isMobile" class="detail-actions">
          <el-button
            v-if="currentQueue.status === 'running' || currentQueue.status === 'pending'"
            type="warning"
            class="detail-btn"
            @click="handleCancel(currentQueue)"
          >取消队列</el-button>
          <el-button v-if="currentQueue.status === 'failed'" type="success" class="detail-btn" @click="handleRetry(currentQueue)">重试</el-button>
          <el-button v-if="currentQueue.status === 'pending'" type="primary" class="detail-btn" @click="handleExecuteNow(currentQueue)">立即执行</el-button>
        </div>
      </template>
    </el-drawer>

    <!-- 移动端筛选抽屉 -->
    <el-drawer v-model="showMobileFilter" title="筛选条件" direction="btt" size="auto" class="filter-drawer">
      <el-form label-position="top">
        <el-form-item label="所属模块">
          <el-select v-model="filter.module" placeholder="全部模块" clearable style="width:100%" @change="syncModuleFromSelect">
            <el-option label="全部模块" value="" />
            <el-option v-for="m in moduleList" :key="m.value" :label="m.label" :value="m.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="队列状态">
          <el-select v-model="filter.status" placeholder="全部状态" clearable style="width:100%">
            <el-option label="全部状态" value="" />
            <el-option label="正在执行" value="running" />
            <el-option label="待执行" value="pending" />
            <el-option label="已完成" value="completed" />
            <el-option label="已失败" value="failed" />
          </el-select>
        </el-form-item>
        <el-form-item label="优先级">
          <el-select v-model="filter.priority" placeholder="全部优先级" clearable style="width:100%">
            <el-option label="全部优先级" value="" />
            <el-option label="高" value="high" />
            <el-option label="中" value="medium" />
            <el-option label="低" value="low" />
          </el-select>
        </el-form-item>
      </el-form>
      <div class="filter-drawer__actions">
        <el-button class="filter-drawer__btn" @click="resetFilters">重置</el-button>
        <el-button type="primary" class="filter-drawer__btn" @click="applyMobileFilter">应用筛选</el-button>
      </div>
    </el-drawer>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useWindowSize } from '@vueuse/core'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  VideoPlay, Clock, SuccessFilled, CircleClose, Search, Refresh, Filter
} from '@element-plus/icons-vue'
import request from '@/utils/request'

const { width } = useWindowSize()
const isMobile = computed(() => width.value < 768)

const loading = ref(false)
const activeTab = ref('')
const activeModuleTab = ref('')
const showMobileFilter = ref(false)

const stats = reactive({
  running: 0,
  pending: 0,
  completed: 0,
  failed: 0
})

const moduleList = ref([
  { value: 'order', label: '订单模块', count: 0 },
  { value: 'payment', label: '支付模块', count: 0 },
  { value: 'message', label: '消息模块', count: 0 },
  { value: 'userms', label: '用户模块', count: 0 },
  { value: 'goods', label: '商品模块', count: 0 },
  { value: 'express', label: '物流模块', count: 0 },
  { value: 'im', label: 'IM模块', count: 0 },
  { value: 'system', label: '系统模块', count: 0 }
])

const totalAll = computed(() => stats.running + stats.pending + stats.completed + stats.failed)

const kpiItems = computed(() => [
  { key: 'running', label: '正在执行', value: stats.running, filterValue: 'running', icon: VideoPlay, gradient: 'linear-gradient(135deg, #409eff, #66b1ff)' },
  { key: 'pending', label: '待执行', value: stats.pending, filterValue: 'pending', icon: Clock, gradient: 'linear-gradient(135deg, #e6a23c, #f0c78a)' },
  { key: 'completed', label: '已完成', value: stats.completed, filterValue: 'completed', icon: SuccessFilled, gradient: 'linear-gradient(135deg, #67c23a, #95d475)' },
  { key: 'failed', label: '已失败', value: stats.failed, filterValue: 'failed', icon: CircleClose, gradient: 'linear-gradient(135deg, #f56c6c, #f89898)' }
])

const statusChips = computed(() => [
  { label: '全部', value: '', count: totalAll.value },
  { label: '待执行', value: 'pending', count: stats.pending },
  { label: '运行中', value: 'running', count: stats.running },
  { label: '已完成', value: 'completed', count: stats.completed },
  { label: '已失败', value: 'failed', count: stats.failed }
])

const filter = reactive({
  module: '',
  status: '',
  priority: '',
  keyword: ''
})

const pagination = reactive({
  page: 1,
  pageSize: 20,
  total: 0
})

const queueList = ref([])
const selectedIds = ref([])
const drawerVisible = ref(false)
const currentQueue = ref(null)

const loadStats = async () => {
  const res = await request.get('/web/setting/queue/stats')
  if (res?.data) {
    Object.assign(stats, res.data)
    if (res.data.moduleStats) {
      moduleList.value.forEach((m) => {
        m.count = res.data.moduleStats[m.value] || 0
      })
    }
  }
}

const loadQueueList = async () => {
  loading.value = true
  try {
    const res = await request.get('/web/setting/queue/list', {
      params: {
        page: pagination.page,
        limit: pagination.pageSize,
        module: filter.module || activeModuleTab.value || undefined,
        status: filter.status || activeTab.value || undefined,
        priority: filter.priority || undefined,
        keyword: filter.keyword || undefined
      }
    })
    if (res?.data) {
      queueList.value = res.data.list || []
      pagination.total = res.data.totalCount || res.data.total || 0
    }
  } catch (e) {
    ElMessage.error(e?.message || '加载队列列表失败')
  } finally {
    loading.value = false
  }
}

const setStatusFilter = (value) => {
  activeTab.value = value
  filter.status = value
  pagination.page = 1
  loadQueueList()
}

const setModuleFilter = (value) => {
  activeModuleTab.value = value
  filter.module = value
  pagination.page = 1
  loadQueueList()
}

const syncModuleFromSelect = () => {
  activeModuleTab.value = filter.module
  pagination.page = 1
  loadQueueList()
}

const filteredQueueList = computed(() => {
  if (!filter.keyword) return queueList.value
  const kw = filter.keyword.toLowerCase()
  return queueList.value.filter((item) => item.name?.toLowerCase().includes(kw))
})

const handleSearch = () => {
  pagination.page = 1
  loadQueueList()
}

const handleRefresh = () => {
  loadStats()
  loadQueueList()
}

const handleSelectionChange = (rows) => {
  selectedIds.value = rows.map((r) => r.id)
}

const toggleSelect = (id, checked) => {
  if (checked) {
    if (!selectedIds.value.includes(id)) selectedIds.value.push(id)
  } else {
    selectedIds.value = selectedIds.value.filter((x) => x !== id)
  }
}

const clearSelection = () => {
  selectedIds.value = []
}

const resetFilters = () => {
  filter.module = ''
  filter.status = ''
  filter.priority = ''
  activeModuleTab.value = ''
  activeTab.value = ''
}

const applyMobileFilter = () => {
  activeTab.value = filter.status
  activeModuleTab.value = filter.module
  showMobileFilter.value = false
  handleSearch()
}

const handleViewDetail = (row) => {
  currentQueue.value = row
  drawerVisible.value = true
}

const handleCancel = async (row) => {
  try {
    await ElMessageBox.confirm(`确定取消队列「${row.name}」吗？`, '提示', { type: 'warning' })
    await request.post(`/web/setting/queue/cancel/${row.id}`)
    ElMessage.success('队列已取消')
    drawerVisible.value = false
    loadQueueList()
    loadStats()
  } catch { /* cancel */ }
}

const handleRetry = async (row) => {
  try {
    await ElMessageBox.confirm(`确定重试队列「${row.name}」吗？`, '提示', { type: 'warning' })
    await request.post(`/web/setting/queue/retry/${row.id}`)
    ElMessage.success('队列已重新加入执行')
    drawerVisible.value = false
    loadQueueList()
    loadStats()
  } catch { /* cancel */ }
}

const handleExecuteNow = async (row) => {
  try {
    await ElMessageBox.confirm(`确定立即执行队列「${row.name}」吗？`, '提示', { type: 'warning' })
    await request.post(`/web/setting/queue/execute/${row.id}`)
    ElMessage.success('队列已开始执行')
    drawerVisible.value = false
    loadQueueList()
    loadStats()
  } catch { /* cancel */ }
}

const handleBatchCancel = async () => {
  try {
    await ElMessageBox.confirm(`确定取消选中的 ${selectedIds.value.length} 个队列吗？`, '提示', { type: 'warning' })
    await request.post('/web/setting/queue/batchCancel', { ids: selectedIds.value })
    ElMessage.success('批量取消成功')
    selectedIds.value = []
    loadQueueList()
    loadStats()
  } catch { /* cancel */ }
}

const getStatusTagType = (status) => {
  const map = { running: 'primary', pending: 'warning', completed: 'success', failed: 'danger' }
  return map[status] || 'info'
}

const getStatusText = (status) => {
  const map = { running: '正在执行', pending: '待执行', completed: '已完成', failed: '已失败' }
  return map[status] || '未知'
}

const getPriorityTagType = (priority) => {
  const map = { high: 'danger', medium: 'warning', low: 'info' }
  return map[priority] || 'info'
}

const getPriorityText = (priority) => {
  const map = { high: '高', medium: '中', low: '低' }
  return map[priority] || '未知'
}

const getProgressStatus = (status) => {
  const map = { completed: 'success', failed: 'exception' }
  return map[status] || ''
}

const getModuleLabel = (module) => {
  const map = {
    order: '订单模块', payment: '支付模块', message: '消息模块',
    userms: '用户模块', goods: '商品模块', express: '物流模块',
    im: 'IM模块', system: '系统模块'
  }
  return map[module] || module || '系统模块'
}

onMounted(() => {
  loadStats()
  loadQueueList()
})
</script>

<style scoped>
.queue-page {
  --queue-radius: 14px;
  --queue-shadow: 0 4px 20px rgba(15, 23, 42, 0.06);
  padding: 16px;
  padding-bottom: calc(16px + env(safe-area-inset-bottom, 0px));
  min-height: 100%;
  background: linear-gradient(180deg, #f0f4ff 0%, #f5f7fa 220px, #f5f7fa 100%);
}

/* Hero */
.queue-hero {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 16px;
}

.hero-title {
  margin: 0 0 6px;
  font-size: 22px;
  font-weight: 700;
  color: #1a1a2e;
  letter-spacing: -0.02em;
}

.hero-subtitle {
  margin: 0;
  font-size: 13px;
  color: #64748b;
  line-height: 1.5;
}

.hero-actions {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-shrink: 0;
}

.hero-refresh {
  box-shadow: var(--queue-shadow);
}

/* KPI */
.kpi-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 12px;
  margin-bottom: 16px;
}

.kpi-card {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 14px 16px;
  border: 1px solid transparent;
  border-radius: var(--queue-radius);
  background: #fff;
  box-shadow: var(--queue-shadow);
  cursor: pointer;
  transition: transform 0.2s, box-shadow 0.2s, border-color 0.2s;
  text-align: left;
}

.kpi-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 24px rgba(15, 23, 42, 0.1);
}

.kpi-card.is-active {
  border-color: #409eff;
  box-shadow: 0 0 0 1px rgba(64, 158, 255, 0.25), var(--queue-shadow);
}

.kpi-icon {
  width: 44px;
  height: 44px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-size: 20px;
  flex-shrink: 0;
}

.kpi-label {
  display: block;
  font-size: 12px;
  color: #94a3b8;
  margin-bottom: 4px;
}

.kpi-value {
  font-size: 24px;
  font-weight: 700;
  color: #1e293b;
  line-height: 1;
}

/* Chips */
.chip-section {
  margin-bottom: 10px;
}

.chip-section__label {
  font-size: 12px;
  color: #94a3b8;
  margin-bottom: 8px;
  font-weight: 500;
}

.chip-scroll {
  display: flex;
  gap: 8px;
  overflow-x: auto;
  padding-bottom: 4px;
  -webkit-overflow-scrolling: touch;
  scrollbar-width: none;
}

.chip-scroll::-webkit-scrollbar {
  display: none;
}

.chip {
  flex-shrink: 0;
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 8px 14px;
  border: 1px solid #e2e8f0;
  border-radius: 999px;
  background: #fff;
  font-size: 13px;
  color: #475569;
  cursor: pointer;
  transition: all 0.2s;
}

.chip:hover {
  border-color: #cbd5e1;
  background: #f8fafc;
}

.chip.is-active {
  background: linear-gradient(135deg, #409eff, #66b1ff);
  border-color: transparent;
  color: #fff;
  box-shadow: 0 4px 12px rgba(64, 158, 255, 0.35);
}

.chip-count {
  font-size: 11px;
  padding: 1px 6px;
  border-radius: 999px;
  background: rgba(0, 0, 0, 0.08);
}

.chip.is-active .chip-count {
  background: rgba(255, 255, 255, 0.25);
}

/* Toolbar */
.toolbar-card {
  background: #fff;
  border-radius: var(--queue-radius);
  padding: 14px;
  margin-bottom: 14px;
  box-shadow: var(--queue-shadow);
}

.toolbar-row {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  align-items: center;
}

.toolbar-row--advanced {
  margin-top: 10px;
  padding-top: 10px;
  border-top: 1px solid #f1f5f9;
}

.toolbar-search {
  flex: 1;
  min-width: 160px;
}

.toolbar-select {
  width: 160px;
}

/* List panel PC */
.list-panel {
  background: #fff;
  border-radius: var(--queue-radius);
  padding: 16px;
  box-shadow: var(--queue-shadow);
}

.list-panel__head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 14px;
}

.list-panel__title {
  font-size: 15px;
  font-weight: 600;
  color: #1e293b;
}

.queue-table {
  border-radius: 8px;
  overflow: hidden;
}

.table-actions {
  display: flex;
  flex-wrap: wrap;
  gap: 2px;
}

.pager {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}

/* Mobile cards */
.mobile-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
  min-height: 200px;
}

.queue-card {
  background: #fff;
  border-radius: 16px;
  padding: 14px;
  box-shadow: 0 2px 12px rgba(15, 23, 42, 0.08);
  border: 1px solid #f1f5f9;
  transition: transform 0.15s;
}

.queue-card:active {
  transform: scale(0.99);
}

.queue-card--running {
  border-left: 3px solid #409eff;
}

.queue-card--pending {
  border-left: 3px solid #e6a23c;
}

.queue-card--completed {
  border-left: 3px solid #67c23a;
}

.queue-card--failed {
  border-left: 3px solid #f56c6c;
}

.queue-card__top {
  display: flex;
  gap: 10px;
  align-items: flex-start;
}

.queue-card__check {
  margin-top: 2px;
}

.queue-card__main {
  flex: 1;
  min-width: 0;
}

.queue-card__title-row {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 8px;
  margin-bottom: 8px;
}

.queue-card__name {
  font-size: 15px;
  font-weight: 600;
  color: #1e293b;
  line-height: 1.4;
  word-break: break-all;
}

.queue-card__meta {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
}

.queue-card__module {
  font-size: 12px;
  color: #94a3b8;
}

.queue-card__progress {
  margin: 12px 0 8px;
}

.progress-label {
  display: flex;
  justify-content: space-between;
  font-size: 12px;
  color: #64748b;
  margin-bottom: 6px;
}

.queue-card__footer {
  display: flex;
  justify-content: space-between;
  font-size: 12px;
  color: #94a3b8;
  margin-bottom: 10px;
}

.queue-card__actions {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  padding-top: 10px;
  border-top: 1px solid #f1f5f9;
}

.mobile-pager {
  display: flex;
  justify-content: center;
  padding: 8px 0 16px;
}

/* Mobile batch bar */
.mobile-batch-bar {
  position: fixed;
  left: 12px;
  right: 12px;
  bottom: calc(12px + env(safe-area-inset-bottom, 0px));
  z-index: 100;
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 12px 16px;
  background: #1e293b;
  color: #fff;
  border-radius: 16px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.25);
}

.mobile-batch-bar span {
  flex: 1;
  font-size: 14px;
}

.slide-up-enter-active,
.slide-up-leave-active {
  transition: transform 0.25s ease, opacity 0.25s;
}

.slide-up-enter-from,
.slide-up-leave-to {
  transform: translateY(100%);
  opacity: 0;
}

/* Detail drawer */
.detail-hero {
  padding: 20px;
  border-radius: 14px;
  margin-bottom: 16px;
  background: linear-gradient(135deg, #f8fafc, #eef2ff);
}

.detail-hero--running { background: linear-gradient(135deg, #eff6ff, #dbeafe); }
.detail-hero--pending { background: linear-gradient(135deg, #fffbeb, #fef3c7); }
.detail-hero--completed { background: linear-gradient(135deg, #f0fdf4, #dcfce7); }
.detail-hero--failed { background: linear-gradient(135deg, #fef2f2, #fee2e2); }

.detail-name {
  margin: 12px 0 4px;
  font-size: 18px;
  font-weight: 600;
  color: #1e293b;
  line-height: 1.4;
}

.detail-module {
  margin: 0 0 16px;
  font-size: 13px;
  color: #64748b;
}

.detail-desc {
  margin-bottom: 16px;
}

.detail-actions {
  display: flex;
  flex-direction: column;
  gap: 10px;
  padding-bottom: env(safe-area-inset-bottom, 16px);
}

.detail-btn {
  width: 100%;
  height: 44px;
  border-radius: 12px;
  font-size: 15px;
}

.filter-drawer__actions {
  display: flex;
  gap: 10px;
  padding: 8px 0 calc(8px + env(safe-area-inset-bottom, 0px));
}

.filter-drawer__btn {
  flex: 1;
  height: 44px;
  border-radius: 12px;
}

.error-msg {
  color: #f56c6c;
  font-size: 13px;
  word-break: break-all;
}

/* Responsive */
@media (max-width: 992px) {
  .kpi-grid {
    grid-template-columns: repeat(2, 1fr);
  }

  .kpi-value {
    font-size: 20px;
  }
}

@media (max-width: 768px) {
  .queue-page {
    padding: 12px;
    padding-bottom: calc(72px + env(safe-area-inset-bottom, 0px));
  }

  .queue-hero {
    flex-direction: column;
    align-items: stretch;
  }

  .hero-title {
    font-size: 20px;
  }

  .hero-actions {
    justify-content: flex-end;
  }

  .kpi-grid {
    gap: 10px;
  }

  .kpi-card {
    padding: 12px;
    border-radius: 12px;
  }

  .kpi-icon {
    width: 38px;
    height: 38px;
    border-radius: 10px;
    font-size: 18px;
  }

  .kpi-value {
    font-size: 18px;
  }

  .toolbar-row .el-button:not(.is-circle) {
    flex: 1;
    min-width: 0;
  }

  .chip-section--status {
    margin-bottom: 12px;
  }
}

@media (max-width: 380px) {
  .kpi-label {
    font-size: 11px;
  }

  .kpi-value {
    font-size: 16px;
  }
}
</style>
