<template>
  <main class="pintuan-page">
    <header class="pintuan-masthead">
      <div>
        <h1>拼团报表</h1>
        <p class="pintuan-lead">汇总活动启用情况与团单状态（按活动商品抽查最多 30 个 commonId）。</p>
      </div>
      <el-button type="primary" :loading="loading" @click="fetchReport">刷新</el-button>
    </header>

    <el-row :gutter="12" class="pintuan-stat-row" v-loading="loading">
      <el-col :xs="12" :sm="8" :md="6" v-for="card in activityCards" :key="card.label">
        <el-card shadow="never" class="pintuan-stat-card">
          <div class="pintuan-stat-label">{{ card.label }}</div>
          <div class="pintuan-stat-value">{{ card.value }}</div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="12" class="pintuan-stat-row" v-loading="loading">
      <el-col :xs="12" :sm="8" :md="6" v-for="card in teamCards" :key="card.label">
        <el-card shadow="never" class="pintuan-stat-card">
          <div class="pintuan-stat-label">{{ card.label }}</div>
          <div class="pintuan-stat-value">{{ card.value }}</div>
        </el-card>
      </el-col>
    </el-row>

    <el-card shadow="never">
      <template #header>
        <div class="pintuan-card-head">
          <span>最近活动</span>
          <span class="pintuan-hint">抽样商品数 {{ summary.teamStats.sampledCommonIds }}</span>
        </div>
      </template>
      <el-table :data="summary.recentActivities" row-key="pintuanId">
        <el-table-column prop="pintuanId" label="ID" width="70" />
        <el-table-column prop="name" label="活动名称" min-width="180" show-overflow-tooltip />
        <el-table-column label="成团人数" width="95" align="center">
          <template #default="{ row }">{{ row.personNum }} 人</template>
        </el-table-column>
        <el-table-column label="商品" min-width="120">
          <template #default="{ row }">commonId {{ row.commonId ?? '-' }}</template>
        </el-table-column>
        <el-table-column label="状态" width="95">
          <template #default="{ row }">
            <el-tag size="small" :type="statusType(row.status)">{{ statusText(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <template #empty><el-empty description="暂无活动数据" /></template>
      </el-table>
    </el-card>
  </main>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { loadPintuanReportSummary } from '../api.js'

const loading = ref(false)
const summary = reactive({
  activityStats: { total: 0, enabled: 0, disabled: 0, pending: 0 },
  teamStats: { total: 0, ongoing: 0, formed: 0, failed: 0, refunded: 0, sampledCommonIds: 0 },
  recentActivities: []
})

const activityCards = computed(() => [
  { label: '活动总数', value: summary.activityStats.total },
  { label: '启用中', value: summary.activityStats.enabled },
  { label: '待开启', value: summary.activityStats.pending },
  { label: '已停用', value: summary.activityStats.disabled }
])

const teamCards = computed(() => [
  { label: '团单抽样合计', value: summary.teamStats.total },
  { label: '拼团中', value: summary.teamStats.ongoing },
  { label: '已成团', value: summary.teamStats.formed },
  { label: '失败 / 已退款', value: summary.teamStats.failed + summary.teamStats.refunded }
])

function statusText(status) {
  const value = Number(status)
  if (value === 1) return '启用'
  if (value === 2) return '待开启'
  return '停用'
}

function statusType(status) {
  const value = Number(status)
  if (value === 1) return 'success'
  if (value === 2) return 'warning'
  return 'info'
}

async function fetchReport() {
  loading.value = true
  try {
    const data = await loadPintuanReportSummary()
    Object.assign(summary.activityStats, data.activityStats || {})
    Object.assign(summary.teamStats, data.teamStats || {})
    summary.recentActivities = data.recentActivities || []
  } catch (error) {
    ElMessage.error(error?.message || '加载拼团报表失败')
  } finally {
    loading.value = false
  }
}

onMounted(fetchReport)
</script>

<style scoped>
.pintuan-page { padding: 16px; }
.pintuan-masthead { display: flex; justify-content: space-between; align-items: flex-start; gap: 16px; margin-bottom: 16px; }
.pintuan-masthead h1 { margin: 0 0 4px; font-size: 20px; }
.pintuan-lead { margin: 0; color: var(--el-text-color-secondary); font-size: 13px; max-width: 720px; line-height: 1.6; }
.pintuan-stat-row { margin-bottom: 12px; }
.pintuan-stat-card { margin-bottom: 8px; }
.pintuan-stat-label { font-size: 13px; color: var(--el-text-color-secondary); }
.pintuan-stat-value { margin-top: 8px; font-size: 24px; font-weight: 600; line-height: 1.2; }
.pintuan-card-head { display: flex; justify-content: space-between; align-items: center; }
.pintuan-hint { font-size: 12px; color: var(--el-text-color-secondary); }
</style>
