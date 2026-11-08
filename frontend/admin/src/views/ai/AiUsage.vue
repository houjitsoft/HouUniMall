<template>
  <div class="ai-usage" v-loading="pageLoading">
    <div class="page-header">
      <h1>AI用量统计</h1>
      <p>全平台 AI 调用计量（hou_ai_token_usage）：GEO 的消耗与中台新链路记在同一张表，一套账对得上</p>
    </div>

    <!-- 概览 -->
    <el-row :gutter="16" class="stat-row">
      <el-col :span="6">
        <el-card shadow="never" class="stat-card">
          <div class="stat-label">总调用次数</div>
          <div class="stat-value">{{ formatNumber(summary.totalCalls) }}</div>
        </el-card>
      </el-col>
      <el-col :span="6" v-for="m in moduleCards" :key="m.key">
        <el-card shadow="never" class="stat-card">
          <div class="stat-label">{{ m.label }}</div>
          <div class="stat-value">{{ formatNumber(m.value) }}</div>
          <div class="stat-extra">占比 {{ percent(m.value) }}%</div>
        </el-card>
      </el-col>
    </el-row>

    <el-card class="usage-container" shadow="never">
      <template #header>
        <div class="card-header">
          <span>调用明细</span>
          <el-button type="primary" plain @click="loadAll">
            <el-icon><Refresh /></el-icon>
            刷新
          </el-button>
        </div>
      </template>

      <!-- 筛选 -->
      <div class="filter-bar">
        <el-select v-model="filters.module" placeholder="来源模块" clearable style="width: 160px">
          <el-option label="GEO" value="geo" />
          <el-option label="AI 中台" value="aiagent" />
          <el-option label="商城" value="mall" />
        </el-select>
        <el-input v-model="filters.model" placeholder="模型名称，如 qwen-plus" clearable style="width: 220px" />
        <el-input v-model="filters.bizType" placeholder="业务类型" clearable style="width: 180px" />
        <el-button type="primary" @click="handleSearch">查询</el-button>
        <el-button @click="handleReset">重置</el-button>
      </div>

      <el-table v-if="records.length" :data="records" style="width: 100%" size="small">
        <el-table-column prop="id" label="ID" width="90" />
        <el-table-column prop="moduleName" label="来源模块" width="110">
          <template #default="scope">
            <el-tag :type="moduleTag(scope.row.moduleName)" size="small">
              {{ moduleLabel(scope.row.moduleName) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="modelName" label="模型" min-width="140" show-overflow-tooltip />
        <el-table-column prop="bizType" label="业务类型" width="120" show-overflow-tooltip />
        <el-table-column prop="scene" label="场景" width="120" show-overflow-tooltip />
        <el-table-column label="Tokens" width="180" align="right">
          <template #default="scope">
            <span>{{ formatNumber(scope.row.promptTokens) }} / {{ formatNumber(scope.row.completionTokens) }}</span>
            <div class="cell-sub">提示 / 完成，合计 {{ formatNumber(scope.row.totalTokens) }}</div>
          </template>
        </el-table-column>
        <el-table-column prop="totalPrice" label="费用" width="110" align="right">
          <template #default="scope">
            {{ formatPrice(scope.row.totalPrice, scope.row.currency) }}
          </template>
        </el-table-column>
        <el-table-column prop="latencyMs" label="耗时" width="90" align="right">
          <template #default="scope">{{ scope.row.latencyMs ?? '-' }} ms</template>
        </el-table-column>
        <el-table-column prop="createTime" label="调用时间" width="170" />
      </el-table>
      <el-empty v-else description="暂无调用记录" />

      <div class="pagination-wrap">
        <el-pagination
          background
          layout="total, sizes, prev, pager, next"
          :current-page="pagination.page"
          :page-size="pagination.limit"
          :page-sizes="[10, 20, 50, 100]"
          :total="pagination.total"
          @current-change="handlePageChange"
          @size-change="handleSizeChange"
        />
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { Refresh } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { aiApi } from '@/api/ai/ai'

const pageLoading = ref(false)
const records = ref([])
const summary = ref({ totalCalls: 0, modules: {} })

const filters = reactive({ module: '', model: '', bizType: '' })
const pagination = reactive({ page: 1, limit: 20, total: 0 })

/** 概览里的模块卡片：后端固定返回 geo / aiagent / mall 三项 */
const moduleCards = computed(() => {
  const mods = summary.value.modules || {}
  return [
    { key: 'geo', label: 'GEO 优化', value: mods.geo || 0 },
    { key: 'aiagent', label: 'AI 中台', value: mods.aiagent || 0 },
    { key: 'mall', label: '商城导购', value: mods.mall || 0 }
  ]
})

const formatNumber = (n) => {
  const v = Number(n || 0)
  return v.toLocaleString('zh-CN')
}

const percent = (v) => {
  const total = Number(summary.value.totalCalls || 0)
  if (!total) return '0.0'
  return ((Number(v || 0) / total) * 100).toFixed(1)
}

const formatPrice = (price, currency) => {
  if (price == null) return '-'
  const symbol = currency === 'USD' ? '$' : '¥'
  return `${symbol}${Number(price).toFixed(4)}`
}

const moduleTag = (m) => ({ geo: 'success', aiagent: 'primary', mall: 'warning' }[m] || 'info')
const moduleLabel = (m) => ({ geo: 'GEO', aiagent: 'AI中台', mall: '商城' }[m] || m || '-')

const loadSummary = async () => {
  try {
    const res = await aiApi.getUsageSummary()
    if (res.code === 1 && res.data) {
      summary.value = {
        totalCalls: res.data.totalCalls || 0,
        modules: res.data.modules || {}
      }
    }
  } catch (e) {
    ElMessage.error(e.message || '加载用量概览失败')
  }
}

const loadRecords = async () => {
  pageLoading.value = true
  try {
    const params = {
      page: pagination.page,
      limit: pagination.limit
    }
    if (filters.module) params.module = filters.module
    if (filters.model) params.model = filters.model
    if (filters.bizType) params.bizType = filters.bizType
    const res = await aiApi.getUsageRecords(params)
    if (res.code === 1 && res.data) {
      records.value = res.data.list || []
      pagination.total = res.data.total || 0
    } else {
      records.value = []
      pagination.total = 0
    }
  } catch (e) {
    ElMessage.error(e.message || '加载用量明细失败')
    records.value = []
  } finally {
    pageLoading.value = false
  }
}

const loadAll = async () => {
  await Promise.all([loadSummary(), loadRecords()])
}

const handleSearch = () => {
  pagination.page = 1
  loadRecords()
}

const handleReset = () => {
  filters.module = ''
  filters.model = ''
  filters.bizType = ''
  pagination.page = 1
  loadRecords()
}

const handlePageChange = (page) => {
  pagination.page = page
  loadRecords()
}

const handleSizeChange = (size) => {
  pagination.limit = size
  pagination.page = 1
  loadRecords()
}

onMounted(loadAll)
</script>

<style scoped>
.ai-usage {
  padding: 16px;
  background-color: #f5f5f5;
  min-height: 100vh;
}

.page-header {
  margin-bottom: 16px;
  text-align: center;
}

.page-header h1 {
  font-size: 24px;
  color: #303133;
  margin-bottom: 8px;
}

.page-header p {
  font-size: 14px;
  color: #909399;
}

.stat-row {
  margin-bottom: 16px;
}

.stat-card {
  border-radius: 8px;
  text-align: center;
}

.stat-label {
  font-size: 13px;
  color: #909399;
}

.stat-value {
  font-size: 26px;
  font-weight: 600;
  color: #303133;
  margin-top: 6px;
}

.stat-extra {
  font-size: 12px;
  color: #a8abb2;
  margin-top: 4px;
}

.usage-container {
  border-radius: 8px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.filter-bar {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
  margin-bottom: 16px;
}

.cell-sub {
  font-size: 12px;
  color: #a8abb2;
}

.pagination-wrap {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}
</style>
