<template>
  <div class="ai-usage-page">
    <div class="page-banner">
      <div>
        <div class="page-title">AI 用量</div>
        <div class="page-meta">
          <span class="member-name">{{ memberDisplay }}</span>
          <el-tag size="small" type="info">会员ID：{{ memberId || '—' }}</el-tag>
          <el-tag v-if="isAdminUser" size="small" type="warning">管理员</el-tag>
        </div>
      </div>
      <div class="page-actions">
        <el-button :icon="Refresh" @click="reloadCurrentTab" :loading="statsLoading || detailLoading">刷新</el-button>
        <el-button type="primary" @click="goCostingBalance">用量费用</el-button>
      </div>
    </div>

    <el-tabs v-model="activeTab" class="usage-tabs" @tab-change="onTabChange">
      <el-tab-pane label="我的用量" name="mine" />
      <el-tab-pane v-if="isAdminUser" label="全部用量" name="all" />
    </el-tabs>

    <el-card shadow="never" class="main-card filter-card">
      <div class="filter-bar">
        <el-radio-group v-model="filters.modelScope" class="filter-scope" @change="applyFilters">
          <el-radio-button value="gpt">GPT 模型</el-radio-button>
          <el-radio-button value="all">全部模型</el-radio-button>
        </el-radio-group>
        <el-select v-model="filters.moduleName" clearable placeholder="按模块" class="filter-item">
          <el-option
            v-for="item in filterOptions.modules"
            :key="item.moduleName"
            :label="item.moduleLabel || getModuleLabel(item.moduleName)"
            :value="item.moduleName"
          />
        </el-select>
        <el-select v-model="filters.modelName" clearable filterable placeholder="按模型" class="filter-item">
          <el-option
            v-for="model in filterOptions.models"
            :key="model"
            :label="model"
            :value="model"
          />
        </el-select>
        <el-date-picker
          v-model="filters.dateRange"
          type="daterange"
          range-separator="至"
          start-placeholder="开始日期"
          end-placeholder="结束日期"
          value-format="YYYY-MM-DD"
          class="filter-date"
        />
        <el-button type="primary" @click="applyFilters" :loading="statsLoading || detailLoading">查询</el-button>
        <el-button @click="resetFilters">重置</el-button>
      </div>
      <el-alert
        v-if="filters.modelScope === 'gpt'"
        class="scope-tip"
        type="info"
        :closable="false"
        show-icon
        title="当前仅统计模型名称包含「GPT」的调用记录，可切换「全部模型」查看整体用量。"
      />
    </el-card>

    <el-row :gutter="16" class="stats-row">
      <el-col :xs="12" :sm="8">
        <div class="stat-card">
          <div class="stat-value">{{ tokenStats.totalRecords }}</div>
          <div class="stat-label">调用次数</div>
        </div>
      </el-col>
      <el-col :xs="12" :sm="8">
        <div class="stat-card">
          <div class="stat-value">{{ formatUsageTokens(tokenStats.totalTokens) }}</div>
          <div class="stat-label">Token 消耗</div>
        </div>
      </el-col>
      <el-col :xs="24" :sm="8">
        <div class="stat-card stat-cost">
          <div class="stat-value">{{ formatUsageCost(tokenStats.totalPrice, tokenStats.currency) }}</div>
          <div class="stat-label">费用合计</div>
        </div>
      </el-col>
    </el-row>

    <el-card shadow="never" class="main-card">
      <template #header>
        <div class="card-header">
          <div>
            <span>{{ activeTab === 'all' ? '全部会员用量明细' : '我的用量明细' }}</span>
            <span class="card-sub">按时间倒序，展示每次调用：执行了什么操作、用了多少 Token</span>
          </div>
          <el-button :icon="Refresh" size="small" @click="applyFilters" :loading="detailLoading || statsLoading">刷新</el-button>
        </div>
      </template>

      <el-table :data="detailList" v-loading="detailLoading" border stripe empty-text="当前筛选条件下暂无 AI 用量记录">
        <el-table-column label="时间" width="170">
          <template #default="{ row }">{{ formatTime(row.usedAt || row.createTime) }}</template>
        </el-table-column>
        <el-table-column v-if="activeTab === 'all'" label="会员" width="140">
          <template #default="{ row }">
            <el-tooltip :content="`会员ID：${row.createBy || row.tenantId || '-'}`" placement="top" :show-after="400">
              <span>{{ row.memberName || row.createBy || '-' }}</span>
            </el-tooltip>
          </template>
        </el-table-column>
        <el-table-column label="模块" width="110">
          <template #default="{ row }">
            <el-tag size="small" effect="plain">{{ row.moduleLabel || getModuleLabel(row.moduleName) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="执行操作" width="130">
          <template #default="{ row }">{{ row.bizTypeName || getBizTypeName(row.bizType) }}</template>
        </el-table-column>
        <el-table-column prop="modelName" label="模型" width="130" />
        <el-table-column label="Token 消耗" width="170" align="right">
          <template #default="{ row }">
            <div class="token-detail">
              <span class="token-total">{{ formatUsageTokens(row.usageTokens ?? row.totalTokens) }} Token</span>
              <span class="token-breakdown">
                入/出 {{ formatUsageTokens(row.promptTokens) }}/{{ formatUsageTokens(row.completionTokens) }}
              </span>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="费用" width="110" align="right">
          <template #default="{ row }">
            {{ formatUsageCost(row.usageCost ?? row.totalPrice, row.currency || tokenStats.currency) }}
          </template>
        </el-table-column>
        <el-table-column label="执行内容" min-width="180">
          <template #default="{ row }">
            <el-tooltip :content="row.seed || '-'" placement="top" :show-after="400">
              <span class="text-ellipsis">{{ row.seed || '-' }}</span>
            </el-tooltip>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="80" align="center" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="showDetail(row)">详情</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        v-model:current-page="detailPage.current"
        v-model:page-size="detailPage.size"
        :total="detailPage.total"
        :page-sizes="[10, 20, 50, 100]"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="loadDetails"
        @current-change="loadDetails"
        class="pagination"
      />
    </el-card>

    <el-row :gutter="16" class="summary-row">
      <el-col :xs="24" :md="activeTab === 'all' ? 8 : 12">
        <el-card shadow="never" class="main-card nested">
          <template #header>
            <div class="card-header"><span>按模型汇总</span></div>
          </template>
          <el-table :data="tokenStats.modelStats" size="small" border empty-text="暂无模型用量">
            <el-table-column prop="modelName" label="模型" min-width="120" />
            <el-table-column label="调用次数" prop="callCount" width="90" align="center" />
            <el-table-column label="Token" width="110" align="right">
              <template #default="{ row }">{{ formatUsageTokens(row.totalTokens) }}</template>
            </el-table-column>
            <el-table-column label="费用" width="100" align="right">
              <template #default="{ row }">{{ formatUsageCost(row.totalPrice, tokenStats.currency) }}</template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
      <el-col :xs="24" :md="activeTab === 'all' ? 8 : 12">
        <el-card shadow="never" class="main-card nested">
          <template #header>
            <div class="card-header"><span>按执行操作汇总</span></div>
          </template>
          <el-table :data="tokenStats.bizTypeStats" size="small" border empty-text="暂无操作用量">
            <el-table-column label="执行操作" min-width="120">
              <template #default="{ row }">{{ row.bizTypeName || getBizTypeName(row.bizType) }}</template>
            </el-table-column>
            <el-table-column label="调用次数" prop="callCount" width="90" align="center" />
            <el-table-column label="Token" width="110" align="right">
              <template #default="{ row }">{{ formatUsageTokens(row.totalTokens) }}</template>
            </el-table-column>
            <el-table-column label="费用" width="100" align="right">
              <template #default="{ row }">{{ formatUsageCost(row.totalPrice, tokenStats.currency) }}</template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
      <el-col v-if="activeTab === 'all'" :xs="24" :md="8">
        <el-card shadow="never" class="main-card nested">
          <template #header>
            <div class="card-header"><span>按会员汇总</span></div>
          </template>
          <el-table :data="tokenStats.memberStats" size="small" border empty-text="暂无会员用量">
            <el-table-column label="会员" min-width="110">
              <template #default="{ row }">
                <el-tooltip :content="`会员ID：${row.memberId || '-'}`" placement="top" :show-after="400">
                  <span>{{ row.memberName || row.memberId || '-' }}</span>
                </el-tooltip>
              </template>
            </el-table-column>
            <el-table-column label="调用次数" prop="callCount" width="90" align="center" />
            <el-table-column label="Token" width="110" align="right">
              <template #default="{ row }">{{ formatUsageTokens(row.totalTokens) }}</template>
            </el-table-column>
            <el-table-column label="费用" width="100" align="right">
              <template #default="{ row }">{{ formatUsageCost(row.totalPrice, tokenStats.currency) }}</template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
    </el-row>

    <el-dialog v-model="detailDialogVisible" title="本次调用详情" width="700px">
      <el-descriptions :column="2" border v-if="currentDetail">
        <el-descriptions-item label="时间">{{ formatTime(currentDetail.usedAt || currentDetail.createTime) }}</el-descriptions-item>
        <el-descriptions-item label="模块">{{ currentDetail.moduleLabel || getModuleLabel(currentDetail.moduleName) }}</el-descriptions-item>
        <el-descriptions-item v-if="activeTab === 'all'" label="会员" :span="2">
          {{ currentDetail.memberName || currentDetail.createBy || '-' }}（ID：{{ currentDetail.createBy || currentDetail.tenantId || '-' }}）
        </el-descriptions-item>
        <el-descriptions-item label="执行操作">{{ currentDetail.bizTypeName || getBizTypeName(currentDetail.bizType) }}</el-descriptions-item>
        <el-descriptions-item label="提供商">{{ currentDetail.provider || '-' }}</el-descriptions-item>
        <el-descriptions-item label="模型" :span="2">{{ currentDetail.modelName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="场景">{{ currentDetail.scene || '-' }}</el-descriptions-item>
        <el-descriptions-item label="延迟">{{ currentDetail.latencyMs != null ? `${currentDetail.latencyMs}ms` : '-' }}</el-descriptions-item>
        <el-descriptions-item label="执行内容" :span="2">
          <div class="detail-content">{{ currentDetail.seed || '-' }}</div>
        </el-descriptions-item>
        <el-descriptions-item label="输入 Token">{{ formatUsageTokens(currentDetail.promptTokens) }}</el-descriptions-item>
        <el-descriptions-item label="输出 Token">{{ formatUsageTokens(currentDetail.completionTokens) }}</el-descriptions-item>
        <el-descriptions-item label="Token 合计" :span="2">
          <strong>{{ formatUsageTokens(currentDetail.usageTokens ?? currentDetail.totalTokens) }}</strong>
        </el-descriptions-item>
        <el-descriptions-item label="费用">
          <strong class="cost-highlight">{{ formatUsageCost(currentDetail.usageCost ?? currentDetail.totalPrice, currentDetail.currency || tokenStats.currency) }}</strong>
        </el-descriptions-item>
        <el-descriptions-item label="货币">{{ currentDetail.currency || tokenStats.currency }}</el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { Refresh } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import request from '@/utils/request'
import { getUserInfo, getUserId } from '@/utils/auth'
import { isSuperAdmin } from '@/utils/permission'
import {
  getModuleLabel,
  getBizTypeName,
  resolveMemberDisplay,
  resolveMemberId,
  formatUsageTokens,
  formatUsageCost,
  isApiOk,
  buildUsageFilterPayload,
  normalizeFilterOptions
} from '@/utils/aiCostingBalance'

const router = useRouter()

const activeTab = ref('mine')
const isAdminUser = ref(isSuperAdmin())
const statsLoading = ref(false)
const detailLoading = ref(false)
const detailList = ref([])
const detailDialogVisible = ref(false)
const currentDetail = ref(null)
const memberDisplay = ref('当前会员')
const memberId = ref('')

const detailPage = reactive({
  current: 1,
  size: 20,
  total: 0
})

const filters = reactive({
  modelScope: 'gpt',
  moduleName: '',
  modelName: '',
  dateRange: null
})

const filterOptions = reactive({
  modules: [],
  models: []
})

const tokenStats = reactive({
  totalTokens: 0,
  totalPrice: 0,
  totalRecords: 0,
  currency: 'USD',
  modelStats: [],
  bizTypeStats: [],
  memberStats: []
})

/**
 * 组装本页筛选请求体：通用筛选 + GPT 模型关键字（modelScope=gpt 时后端按模型名 LIKE 过滤）
 * @returns {Object} 请求体参数
 */
const buildFilterPayload = () => {
  const payload = buildUsageFilterPayload(filters)
  if (filters.modelScope === 'gpt') payload.modelKeyword = 'GPT'
  return payload
}

/**
 * 根据统计接口返回同步筛选下拉选项（模块 / 模型）
 * @param {Object} data - 接口返回的 data
 */
const syncFilterOptions = (data) => {
  const normalized = normalizeFilterOptions(data?.filterOptions)
  if (normalized.modules.length || !filterOptions.modules.length) filterOptions.modules = normalized.modules
  if (normalized.models.length || !filterOptions.models.length) filterOptions.models = normalized.models
}

/**
 * 加载当前选项卡的用量统计（我的用量 / 全部用量各自走独立接口）
 */
const loadStats = async () => {
  statsLoading.value = true
  try {
    const url = activeTab.value === 'all'
      ? '/web/geo/ai/token-usage-admin-stats'
      : '/web/geo/ai/token-usage-stats-query'
    const res = await request.post(url, buildFilterPayload())
    if (!isApiOk(res)) {
      resetStats()
      if (res?.code === 403) {
        ElMessage.warning(res.msg || '仅管理员可查看全部用量')
        isAdminUser.value = false
        activeTab.value = 'mine'
      }
      return
    }
    const data = res.data || {}
    tokenStats.totalTokens = Number(data.totalTokens || 0)
    tokenStats.totalPrice = Number(data.totalPrice || 0)
    tokenStats.totalRecords = Number(data.totalRecords || 0)
    tokenStats.currency = data.currency || 'USD'
    tokenStats.modelStats = Array.isArray(data.modelStats) ? data.modelStats : []
    tokenStats.bizTypeStats = Array.isArray(data.bizTypeStats) ? data.bizTypeStats : []
    tokenStats.memberStats = Array.isArray(data.memberStats) ? data.memberStats : []
    syncFilterOptions(data)
    if (data.member) {
      memberDisplay.value = resolveMemberDisplay(getUserInfo(), data.member)
      memberId.value = String(data.member.memberId || memberId.value || '')
      // 后端权威判定：is_admin=1 才展示「全部用量」选项卡
      isAdminUser.value = data.member.isAdmin === true || data.member.isAdmin === 1
    }
  } catch (e) {
    resetStats()
  } finally {
    statsLoading.value = false
  }
}

/**
 * 加载当前选项卡的用量明细（分页）
 */
const loadDetails = async () => {
  detailLoading.value = true
  try {
    const url = activeTab.value === 'all'
      ? '/web/geo/ai/token-usage-admin-details'
      : '/web/geo/ai/token-usage-details'
    const res = await request.post(url, {
      page: detailPage.current,
      limit: detailPage.size,
      ...buildFilterPayload()
    })
    if (!isApiOk(res)) {
      detailList.value = []
      detailPage.total = 0
      return
    }
    const data = res.data || {}
    detailList.value = Array.isArray(data.list) ? data.list : []
    detailPage.total = Number(data.total || 0)
    syncFilterOptions(data)
  } catch (e) {
    detailList.value = []
    detailPage.total = 0
  } finally {
    detailLoading.value = false
  }
}

/**
 * 清空统计数据（接口异常或无权限时兜底展示）
 */
const resetStats = () => {
  tokenStats.totalTokens = 0
  tokenStats.totalPrice = 0
  tokenStats.totalRecords = 0
  tokenStats.modelStats = []
  tokenStats.bizTypeStats = []
  tokenStats.memberStats = []
}

/**
 * 应用筛选条件：重置到第一页并重新加载统计与明细
 */
const applyFilters = () => {
  detailPage.current = 1
  loadStats()
  loadDetails()
}

/**
 * 重置筛选条件（保留模型范围为 GPT 默认值）并重新加载
 */
const resetFilters = () => {
  filters.modelScope = 'gpt'
  filters.moduleName = ''
  filters.modelName = ''
  filters.dateRange = null
  applyFilters()
}

/**
 * 切换选项卡：重置分页并按新口径加载数据
 */
const onTabChange = () => {
  detailPage.current = 1
  loadStats()
  loadDetails()
}

/**
 * 刷新当前选项卡数据
 */
const reloadCurrentTab = () => {
  loadStats()
  loadDetails()
}

/**
 * 打开单条调用记录的详情弹窗
 * @param {Object} row - 表格行数据
 */
const showDetail = (row) => {
  currentDetail.value = row
  detailDialogVisible.value = true
}

/**
 * 跳转到用量费用页
 */
const goCostingBalance = () => {
  router.push('/setting/aicostingbalance')
}

/**
 * 格式化时间显示（兼容 ISO 字符串与时间戳）
 * @param {string|number} val - 时间值
 * @returns {string} yyyy-MM-dd HH:mm:ss
 */
const formatTime = (val) => {
  if (!val) return '-'
  const date = new Date(val)
  if (Number.isNaN(date.getTime())) return String(val)
  const pad = (n) => String(n).padStart(2, '0')
  return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())} ${pad(date.getHours())}:${pad(date.getMinutes())}:${pad(date.getSeconds())}`
}

onMounted(() => {
  memberDisplay.value = resolveMemberDisplay(getUserInfo(), null)
  memberId.value = resolveMemberId(getUserInfo(), getUserId)
  loadStats()
  loadDetails()
})
</script>

<style lang="scss" scoped>
.ai-usage-page {
  padding: 16px;
  background: #f5f7fa;
  min-height: 100%;
}

.page-banner {
  display: flex;
  align-items: center;
  justify-content: space-between;
  flex-wrap: wrap;
  gap: 12px;
  background: #fff;
  border-radius: 8px;
  padding: 16px 20px;
  margin-bottom: 16px;
}

.page-title {
  font-size: 18px;
  font-weight: 600;
  color: #303133;
}

.page-meta {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-top: 8px;

  .member-name {
    font-weight: 500;
    color: #606266;
  }
}

.usage-tabs {
  margin-bottom: 4px;

  :deep(.el-tabs__header) {
    margin-bottom: 12px;
  }

  :deep(.el-tabs__item) {
    font-size: 15px;
  }
}

.filter-card {
  margin-bottom: 16px;

  :deep(.el-card__body) {
    padding: 14px 16px;
  }
}

.filter-bar {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 10px;

  .filter-item {
    width: 160px;
  }

  .filter-date {
    width: 260px;
  }
}

.scope-tip {
  margin-top: 10px;
}

.stats-row {
  margin-bottom: 16px;
}

.stat-card {
  background: #fff;
  border-radius: 8px;
  padding: 18px 20px;
  text-align: center;

  .stat-value {
    font-size: 22px;
    font-weight: 600;
    color: #303133;
  }

  .stat-label {
    margin-top: 6px;
    font-size: 13px;
    color: #909399;
  }

  &.stat-cost .stat-value {
    color: #e6a23c;
  }
}

.main-card {
  border-radius: 8px;
  margin-bottom: 16px;

  &.nested {
    margin-bottom: 0;
  }
}

.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;

  .card-sub {
    margin-left: 10px;
    font-size: 12px;
    color: #909399;
  }
}

.token-detail {
  display: flex;
  flex-direction: column;
  line-height: 1.4;

  .token-total {
    font-weight: 500;
  }

  .token-breakdown {
    font-size: 12px;
    color: #909399;
  }
}

.text-ellipsis {
  display: inline-block;
  max-width: 100%;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  vertical-align: bottom;
}

.pagination {
  margin-top: 14px;
  justify-content: flex-end;
}

.summary-row {
  margin-bottom: 16px;

  .el-col {
    margin-bottom: 16px;
  }
}

.detail-content {
  max-height: 160px;
  overflow: auto;
  white-space: pre-wrap;
  word-break: break-all;
}

.cost-highlight {
  color: #e6a23c;
}

@media (max-width: 768px) {
  .filter-bar {
    .filter-item,
    .filter-date {
      width: 100%;
    }
  }
}
</style>
