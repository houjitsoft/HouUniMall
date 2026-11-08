<template>
  <div class="ai-cost-page">
    <div class="member-banner">
      <div>
        <div class="member-title">当前登录会员用量</div>
        <div class="member-meta">
          <span class="member-name">{{ memberDisplay }}</span>
          <el-tag size="small" type="info">会员ID：{{ memberId || '—' }}</el-tag>
          <el-tag size="small" type="success">剩余额度：{{ formatUsageTokens(quota.remainingTokens) }} Token</el-tag>
        </div>
      </div>
      <div class="member-actions">
        <el-button type="warning" @click="openRechargeDialog">充值</el-button>
        <el-button :icon="Refresh" @click="reloadAll" :loading="tokenLoading || detailLoading">刷新</el-button>
        <el-button type="primary" @click="goAiConfig">管理 AI 配置</el-button>
      </div>
    </div>

    <el-row :gutter="16" class="stats-row">
      <el-col :xs="12" :sm="6">
        <div class="stat-card stat-quota">
          <div class="stat-value">{{ formatUsageTokens(quota.remainingTokens) }}</div>
          <div class="stat-label">剩余 Token 额度</div>
        </div>
      </el-col>
      <el-col :xs="12" :sm="6">
        <div class="stat-card">
          <div class="stat-value">{{ formatUsageTokens(tokenStats.totalTokens) }}</div>
          <div class="stat-label">累计消耗 Token</div>
        </div>
      </el-col>
      <el-col :xs="12" :sm="6">
        <div class="stat-card stat-cost">
          <div class="stat-value">{{ formatUsageCost(tokenStats.totalPrice, tokenStats.currency) }}</div>
          <div class="stat-label">累计费用</div>
        </div>
      </el-col>
      <el-col :xs="12" :sm="6">
        <div class="stat-card">
          <div class="stat-value">{{ tokenStats.totalRecords }}</div>
          <div class="stat-label">调用次数</div>
        </div>
      </el-col>
    </el-row>

    <el-card shadow="never" class="main-card">
      <template #header>
        <div class="card-header">
          <div>
            <span>使用步骤明细</span>
            <span class="card-sub">按时间倒序，展示每一步：哪个模块、什么时候用、用了多少</span>
          </div>
          <el-button :icon="Refresh" size="small" @click="applyFilters" :loading="detailLoading || tokenLoading">刷新</el-button>
        </div>
      </template>

      <div class="filter-bar">
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
        <el-button type="primary" @click="applyFilters" :loading="detailLoading || tokenLoading">查询</el-button>
        <el-button @click="resetFilters">重置</el-button>
      </div>

      <el-table :data="detailList" v-loading="detailLoading" border stripe empty-text="当前筛选条件下暂无 AI 用量记录">
        <el-table-column label="什么时候用的" width="180">
          <template #default="{ row }">{{ formatTime(row.usedAt || row.createTime) }}</template>
        </el-table-column>
        <el-table-column label="哪个模块" width="120">
          <template #default="{ row }">
            <el-tag size="small" effect="plain">{{ row.moduleLabel || getModuleLabel(row.moduleName) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="功能步骤" width="140">
          <template #default="{ row }">{{ row.bizTypeName || getBizTypeName(row.bizType) }}</template>
        </el-table-column>
        <el-table-column label="用了多少" width="170" align="right">
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
        <el-table-column prop="modelName" label="模型" width="120" />
        <el-table-column label="执行内容" min-width="200">
          <template #default="{ row }">
            <el-tooltip :content="row.seed || '-'" placement="top" :show-after="400">
              <span class="text-ellipsis">{{ row.seed || '-' }}</span>
            </el-tooltip>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="90" align="center" fixed="right">
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

    <el-alert
      class="quota-manage-tip"
      type="info"
      :closable="false"
      show-icon
      title="新会员初始 Token 额度：在 HouGEO「设置 → 智能体与客户支持」中配置登录用户/访客免费 Token（存 hou_geo_system_setting）；首次开户时写入 hou_geo_ai_balance。"
    />

    <el-row :gutter="16" class="summary-row">
      <el-col :xs="24" :md="12">
        <el-card shadow="never" class="main-card nested">
          <template #header>
            <div class="card-header"><span>按模块汇总</span></div>
          </template>
          <el-table :data="tokenStats.moduleStats" size="small" border empty-text="暂无模块用量">
            <el-table-column label="模块" min-width="120">
              <template #default="{ row }">{{ row.moduleLabel || getModuleLabel(row.moduleName) }}</template>
            </el-table-column>
            <el-table-column label="调用次数" prop="callCount" width="90" align="center" />
            <el-table-column label="Token" width="110" align="right">
              <template #default="{ row }">{{ formatUsageTokens(row.totalTokens) }}</template>
            </el-table-column>
            <el-table-column label="费用" width="110" align="right">
              <template #default="{ row }">{{ formatUsageCost(row.totalPrice, tokenStats.currency) }}</template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
      <el-col :xs="24" :md="12">
        <el-card shadow="never" class="main-card nested">
          <template #header>
            <div class="card-header"><span>按功能步骤汇总</span></div>
          </template>
          <el-table :data="tokenStats.bizTypeStats" size="small" border empty-text="暂无功能用量">
            <el-table-column label="功能" min-width="140">
              <template #default="{ row }">{{ row.bizTypeName || getBizTypeName(row.bizType) }}</template>
            </el-table-column>
            <el-table-column label="调用次数" prop="callCount" width="90" align="center" />
            <el-table-column label="Token" width="110" align="right">
              <template #default="{ row }">{{ formatUsageTokens(row.totalTokens) }}</template>
            </el-table-column>
            <el-table-column label="费用" width="110" align="right">
              <template #default="{ row }">{{ formatUsageCost(row.totalPrice, tokenStats.currency) }}</template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
    </el-row>

    <el-dialog v-model="detailDialogVisible" title="本步用量详情" width="700px">
      <el-descriptions :column="2" border v-if="currentDetail">
        <el-descriptions-item label="什么时候用的">{{ formatTime(currentDetail.usedAt || currentDetail.createTime) }}</el-descriptions-item>
        <el-descriptions-item label="哪个模块">{{ currentDetail.moduleLabel || getModuleLabel(currentDetail.moduleName) }}</el-descriptions-item>
        <el-descriptions-item label="功能步骤">{{ currentDetail.bizTypeName || getBizTypeName(currentDetail.bizType) }}</el-descriptions-item>
        <el-descriptions-item label="提供商">{{ currentDetail.provider || '-' }}</el-descriptions-item>
        <el-descriptions-item label="模型" :span="2">{{ currentDetail.modelName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="场景">{{ currentDetail.scene || '-' }}</el-descriptions-item>
        <el-descriptions-item label="延迟">{{ currentDetail.latencyMs != null ? `${currentDetail.latencyMs}ms` : '-' }}</el-descriptions-item>
        <el-descriptions-item label="执行内容" :span="2">
          <div class="detail-content">{{ currentDetail.seed || '-' }}</div>
        </el-descriptions-item>
        <el-descriptions-item label="输入 Token">{{ formatUsageTokens(currentDetail.promptTokens) }}</el-descriptions-item>
        <el-descriptions-item label="输出 Token">{{ formatUsageTokens(currentDetail.completionTokens) }}</el-descriptions-item>
        <el-descriptions-item label="用了多少 Token" :span="2">
          <strong>{{ formatUsageTokens(currentDetail.usageTokens ?? currentDetail.totalTokens) }}</strong>
        </el-descriptions-item>
        <el-descriptions-item label="费用">
          <strong class="cost-highlight">{{ formatUsageCost(currentDetail.usageCost ?? currentDetail.totalPrice, currentDetail.currency || tokenStats.currency) }}</strong>
        </el-descriptions-item>
        <el-descriptions-item label="货币">{{ currentDetail.currency || tokenStats.currency }}</el-descriptions-item>
      </el-descriptions>
    </el-dialog>

    <el-dialog
      v-model="showRecharge"
      title="充值 AI Token"
      width="460px"
      destroy-on-close
      @closed="onRechargeDialogClosed"
    >
      <div v-loading="rechargeLoading">
        <p class="recharge-tip">
          当前剩余 <b>{{ formatUsageTokens(quota.remainingTokens) }}</b> Token
          （已用 {{ formatUsageTokens(quota.usedTokens) }} / 总额度 {{ formatUsageTokens(quota.freeQuotaTokens) }}）。
          选择套餐后跳转支付，支付成功自动到账。
        </p>
        <el-radio-group v-model="selectedPackageId" class="recharge-list">
          <el-radio
            v-for="pkg in rechargePackages"
            :key="pkg.id"
            :value="pkg.id"
            class="recharge-item"
            border
          >
            <div class="recharge-item__main">
              <span class="recharge-item__title">{{ pkg.title }}</span>
              <span class="recharge-item__price">¥{{ Number(pkg.priceYuan || 0).toFixed(2) }}</span>
            </div>
            <div class="recharge-item__desc">
              {{ pkg.description || `${formatUsageTokens(pkg.tokenAmount)} Token` }}
            </div>
          </el-radio>
        </el-radio-group>
        <div v-if="pendingOrderNo" class="recharge-pending">
          <p>订单号：{{ pendingOrderNo }}</p>
          <p>支付完成后请点击「我已支付」同步到账。</p>
          <el-button type="success" :loading="syncLoading" @click="syncPendingOrder()">我已支付</el-button>
        </div>
      </div>
      <template #footer>
        <el-button @click="showRecharge = false">取消</el-button>
        <el-button type="warning" :loading="payLoading" @click="startRechargePay">去支付</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onBeforeUnmount } from 'vue'
import { useRouter } from 'vue-router'
import { Refresh } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import request from '@/utils/request'
import { getUserInfo, getUserId, getToken } from '@/utils/auth'
import {
  getModuleLabel,
  getBizTypeName,
  resolveMemberDisplay,
  resolveMemberId,
  formatUsageTokens,
  formatUsageCost,
  normalizeQuota,
  normalizeRechargePackages,
  isApiOk,
  buildCloudRechargeJumpUrl,
  AI_RECHARGE_DONE_EVENT,
  buildUsageFilterPayload
} from '@/utils/aiCostingBalance'

const router = useRouter()

const tokenLoading = ref(false)
const detailLoading = ref(false)
const detailList = ref([])
const detailDialogVisible = ref(false)
const currentDetail = ref(null)
const memberDisplay = ref('当前会员')
const memberId = ref('')

const showRecharge = ref(false)
const rechargeLoading = ref(false)
const payLoading = ref(false)
const syncLoading = ref(false)
const rechargePackages = ref([])
const selectedPackageId = ref('')
const pendingOrderNo = ref('')
let payPollTimer = null

const detailPage = reactive({
  current: 1,
  size: 20,
  total: 0
})

const filters = reactive({
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
  moduleStats: [],
  bizTypeStats: []
})

const quota = reactive(normalizeQuota(null))

const buildFilterPayload = () => buildUsageFilterPayload(filters)

const syncFilterOptions = (data) => {
  const options = data?.filterOptions || {}
  if (Array.isArray(options.modules)) {
    filterOptions.modules = options.modules
  }
  if (Array.isArray(options.models)) {
    filterOptions.models = options.models
  }
}

const formatTime = (time) => {
  if (!time) return '-'
  const date = new Date(time)
  if (Number.isNaN(date.getTime())) return String(time)
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit'
  })
}

const applyQuota = (raw) => {
  Object.assign(quota, normalizeQuota(raw))
}

const syncMemberFromPayload = (data) => {
  const userInfo = getUserInfo()
  memberId.value = String(data?.member?.memberId || data?.memberId || resolveMemberId(userInfo, getUserId) || '')
  memberDisplay.value = resolveMemberDisplay(userInfo, data?.member)
  if (data?.quota) applyQuota(data.quota)
  syncFilterOptions(data)
}

const loadTokenStats = async () => {
  try {
    tokenLoading.value = true
    const res = await request.get('/web/geo/ai/token-usage-stats', { params: buildFilterPayload() })
    if (isApiOk(res)) {
      const data = res.data || {}
      syncMemberFromPayload(data)
      tokenStats.totalTokens = data.totalTokens || 0
      tokenStats.totalPrice = data.totalPrice || 0
      tokenStats.totalRecords = data.totalRecords || 0
      tokenStats.currency = data.currency || 'USD'
      tokenStats.modelStats = data.modelStats || []
      tokenStats.moduleStats = data.moduleStats || []
      tokenStats.bizTypeStats = data.bizTypeStats || []
      if (!data.quota) {
        await loadQuota()
      }
    } else {
      ElMessage.error(res.msg || res.message || '加载 token 统计失败')
    }
  } catch (e) {
    console.error('加载 token 使用统计失败:', e)
    ElMessage.error('加载 token 统计失败')
  } finally {
    tokenLoading.value = false
  }
}

const loadQuota = async () => {
  try {
    const res = await request.post('/web/geo/support/quota', {})
    if (isApiOk(res)) {
      applyQuota(res.data || {})
    }
  } catch (e) {
    console.warn('加载 Token 余额失败:', e)
  }
}

const loadDetails = async () => {
  try {
    detailLoading.value = true
    const res = await request.post('/web/geo/ai/token-usage-details', {
      page: detailPage.current,
      limit: detailPage.size,
      ...buildFilterPayload()
    })
    if (isApiOk(res)) {
      const data = res.data || {}
      syncMemberFromPayload(data)
      detailList.value = data.list || []
      detailPage.total = data.total || 0
    } else {
      ElMessage.error(res.msg || res.message || '加载明细失败')
    }
  } catch (e) {
    console.error('加载 token 使用明细失败:', e)
    ElMessage.error('加载明细失败')
  } finally {
    detailLoading.value = false
  }
}

const applyFilters = async () => {
  detailPage.current = 1
  await Promise.all([loadTokenStats(), loadDetails()])
}

const resetFilters = async () => {
  filters.moduleName = ''
  filters.modelName = ''
  filters.dateRange = null
  await applyFilters()
}

const showDetail = (row) => {
  currentDetail.value = row
  detailDialogVisible.value = true
}

const goAiConfig = () => {
  router.push('/setting/ai-config')
}

const loadRechargePackages = async () => {
  rechargeLoading.value = true
  try {
    const res = await request.get('/web/geo/support/recharge/packages')
    if (isApiOk(res)) {
      const list = normalizeRechargePackages(res.data || {})
      rechargePackages.value = list
      if (list.length && !selectedPackageId.value) {
        selectedPackageId.value = list[0].id
      }
      if (!list.length) {
        ElMessage.warning('暂无可用的充值套餐')
      }
    } else {
      ElMessage.error(res?.msg || res?.message || '加载充值套餐失败')
    }
  } catch (e) {
    ElMessage.error(e?.response?.data?.message || '加载充值套餐失败')
  } finally {
    rechargeLoading.value = false
  }
}

const openRechargeDialog = () => {
  if (!getToken()) {
    ElMessage.warning('请先登录后再充值')
    return
  }
  showRecharge.value = true
  loadRechargePackages()
}

const stopPayPoll = () => {
  if (payPollTimer) {
    clearInterval(payPollTimer)
    payPollTimer = null
  }
}

const applyRechargeResult = async (data) => {
  if (data?.quota) {
    applyQuota(data.quota)
  } else {
    await loadQuota()
  }
  pendingOrderNo.value = ''
  showRecharge.value = false
  ElMessage.success('充值成功，Token 已到账')
  await loadTokenStats()
}

const syncPendingOrder = async (orderNo = pendingOrderNo.value) => {
  if (!orderNo) return
  syncLoading.value = true
  try {
    const res = await request.post('/web/geo/support/recharge/sync', { orderNo })
    if (isApiOk(res) && (res.data?.fulfilled || res.data?.payStatus === 'paid')) {
      stopPayPoll()
      await applyRechargeResult(res.data)
    } else {
      ElMessage.info('尚未检测到支付成功，请完成支付后再试')
    }
  } catch (e) {
    ElMessage.error(e?.response?.data?.message || '同步支付结果失败')
  } finally {
    syncLoading.value = false
  }
}

const startPayPoll = (orderNo) => {
  stopPayPoll()
  pendingOrderNo.value = orderNo
  payPollTimer = setInterval(() => {
    syncPendingOrder(orderNo)
  }, 4000)
}

const startRechargePay = async () => {
  if (!selectedPackageId.value) {
    ElMessage.warning('请选择充值套餐')
    return
  }
  payLoading.value = true
  try {
    const res = await request.post('/web/geo/support/recharge/pay', {
      packageId: selectedPackageId.value,
      jumpUrl: buildCloudRechargeJumpUrl()
    })
    if (!isApiOk(res) || !res.data?.h5Url) {
      ElMessage.error(res?.msg || res?.message || '创建支付订单失败')
      return
    }
    const orderNo = res.data.orderNo || res.data.mchOrderNo
    startPayPoll(orderNo)
    window.open(res.data.h5Url, '_blank', 'noopener,noreferrer')
    ElMessage.info('已打开支付页面，支付完成后 Token 将自动到账')
  } catch (e) {
    ElMessage.error(e?.response?.data?.message || '发起支付失败')
  } finally {
    payLoading.value = false
  }
}

const onRechargeDialogClosed = () => {
  stopPayPoll()
}

const onRechargeMessage = async (event) => {
  if (event.origin !== window.location.origin) return
  if (event.data?.type === AI_RECHARGE_DONE_EVENT) {
    stopPayPoll()
    await applyRechargeResult(event.data.data || {})
  }
}

const reloadAll = async () => {
  await Promise.all([loadTokenStats(), loadDetails()])
}

onMounted(() => {
  const userInfo = getUserInfo()
  memberId.value = resolveMemberId(userInfo, getUserId)
  memberDisplay.value = resolveMemberDisplay(userInfo, null)
  window.addEventListener('message', onRechargeMessage)
  reloadAll()
})

onBeforeUnmount(() => {
  stopPayPoll()
  window.removeEventListener('message', onRechargeMessage)
})
</script>

<style scoped lang="scss">
.ai-cost-page {
  padding: 16px 0 24px;
  background: #f0f2f5;
}

.member-banner {
  margin: 0 20px 16px;
  padding: 16px 20px;
  background: #fff;
  border-radius: 10px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 16px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.06);
}

.member-title {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.member-meta {
  margin-top: 8px;
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
}

.member-name {
  font-size: 14px;
  color: #606266;
}

.member-actions {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.stats-row,
.summary-row {
  margin: 0 20px 16px;
}

.quota-manage-tip {
  margin: 0 20px 16px;
}

.filter-bar {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  margin-bottom: 16px;
  align-items: center;
}

.filter-item {
  width: 160px;
}

.filter-date {
  width: 280px;
}

.stat-card {
  background: #fff;
  border-radius: 10px;
  padding: 20px;
  text-align: center;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.06);

  &.stat-cost .stat-value {
    color: #e6a23c;
  }

  &.stat-quota .stat-value {
    color: #67c23a;
  }
}

.stat-value {
  font-size: 28px;
  font-weight: 700;
  color: #303133;
  line-height: 1.2;
}

.stat-label {
  margin-top: 8px;
  font-size: 13px;
  color: #909399;
}

.main-card {
  margin: 0 20px 16px;
  border-radius: 10px;

  &.nested {
    margin: 0 0 16px;
  }

  :deep(.el-card__body) {
    padding: 20px;
  }
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 12px;
  font-weight: 600;
  font-size: 16px;
}

.card-sub {
  display: block;
  margin-top: 4px;
  font-size: 12px;
  font-weight: 400;
  color: #909399;
}

.text-ellipsis {
  display: inline-block;
  max-width: 100%;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.token-detail {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 2px;

  .token-total {
    font-weight: 600;
    color: #303133;
  }

  .token-breakdown {
    font-size: 12px;
    color: #909399;
  }
}

.pagination {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
}

.detail-content {
  max-height: 100px;
  overflow-y: auto;
  padding: 8px;
  background: #f5f7fa;
  border-radius: 4px;
  font-family: monospace;
  font-size: 12px;
  line-height: 1.6;
}

.cost-highlight {
  color: #e6a23c;
  font-size: 16px;
}

.recharge-tip {
  margin: 0 0 12px;
  color: #606266;
  font-size: 13px;
  line-height: 1.6;
}

.recharge-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
  width: 100%;
}

.recharge-item {
  width: 100%;
  height: auto;
  margin-right: 0;
  padding: 12px;
}

.recharge-item__main {
  display: flex;
  justify-content: space-between;
  align-items: center;
  width: 100%;
}

.recharge-item__title {
  font-weight: 600;
  color: #303133;
}

.recharge-item__price {
  color: #e6a23c;
  font-weight: 600;
}

.recharge-item__desc {
  margin-top: 4px;
  font-size: 12px;
  color: #909399;
}

.recharge-pending {
  margin-top: 16px;
  padding: 12px;
  background: #f4f4f5;
  border-radius: 8px;
  color: #606266;
}

.recharge-pending p {
  margin: 0 0 8px;
}
</style>
