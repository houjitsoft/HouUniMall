<template>
  <div class="settlement-page">
    <header class="page-header">
      <div>
        <h1>结算分账</h1>
        <p>订单支付成功后自动按抽成比例记流水；按开发者生成结算批次，打款后回填凭证。</p>
      </div>
      <div class="actions">
        <el-input v-model="developerId" placeholder="开发者ID（运营筛选，留空为自己）" clearable style="width: 240px" />
        <el-button :loading="loading" @click="loadAll">刷新</el-button>
      </div>
    </header>

    <el-row :gutter="12" class="cards">
      <el-col :span="8">
        <el-card shadow="never" class="metric">
          <div class="metric-label">待结算</div>
          <div class="metric-value warn">¥{{ money(summary.pendingAmount) }}</div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="never" class="metric">
          <div class="metric-label">已结算</div>
          <div class="metric-value ok">¥{{ money(summary.settledAmount) }}</div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="never" class="metric">
          <div class="metric-label">平台抽成</div>
          <div class="metric-value">{{ rateText }}</div>
        </el-card>
      </el-col>
    </el-row>

    <el-card shadow="never" class="block">
      <template #header>
        生成结算批次（运营）
        <span class="hint">只汇总所选区间内「待结算」的流水，已结算流水不会被重复纳入。</span>
      </template>
      <el-form inline>
        <el-form-item label="开发者ID">
          <el-input v-model="batchForm.developerId" placeholder="必填" style="width: 160px" />
        </el-form-item>
        <el-form-item label="起">
          <el-date-picker v-model="batchForm.from" type="date" value-format="YYYY-MM-DD" style="width: 160px" />
        </el-form-item>
        <el-form-item label="止">
          <el-date-picker v-model="batchForm.to" type="date" value-format="YYYY-MM-DD" style="width: 160px" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="batchForm.remark" placeholder="选填" style="width: 200px" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="creating" @click="createBatch">生成批次</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card shadow="never" class="block">
      <template #header>分账流水</template>
      <el-table :data="shares" size="small" empty-text="暂无分账流水">
        <el-table-column prop="orderNo" label="订单号" min-width="180" />
        <el-table-column prop="pluginCode" label="插件" min-width="130" />
        <el-table-column label="实收" width="110">
          <template #default="{ row }">¥{{ money(row.grossAmount) }}</template>
        </el-table-column>
        <el-table-column label="平台" width="110">
          <template #default="{ row }">¥{{ money(row.platformAmount) }}</template>
        </el-table-column>
        <el-table-column label="开发者" width="110">
          <template #default="{ row }">¥{{ money(row.developerAmount) }}</template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag size="small" :type="shareStatusOf(row.status).type">{{ shareStatusOf(row.status).text }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="batchNo" label="批次号" min-width="160" />
        <el-table-column prop="createTime" label="时间" min-width="160" />
        <el-table-column label="操作" width="100">
          <template #default="{ row }">
            <el-button v-if="row.status === 0" link type="danger" @click="rejectShare(row)">驳回</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-card shadow="never" class="block">
      <template #header>结算批次</template>
      <el-table :data="batches" size="small" empty-text="暂无结算批次">
        <el-table-column prop="batchNo" label="批次号" min-width="180" />
        <el-table-column prop="shareCount" label="笔数" width="80" />
        <el-table-column label="开发者应结" width="130">
          <template #default="{ row }">¥{{ money(row.developerAmount) }}</template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag size="small" :type="batchStatusOf(row.status).type">{{ batchStatusOf(row.status).text }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" min-width="160" />
        <el-table-column prop="payTime" label="打款时间" min-width="160" />
        <el-table-column label="操作" width="120">
          <template #default="{ row }">
            <el-button v-if="row.status === 0" link type="primary" @click="markPaid(row)">标记打款</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  batchStatusOf,
  createSettlementBatch,
  getSettlementSummary,
  listRevenueShares,
  listSettlementBatches,
  markBatchPaid,
  rejectRevenueShare,
  shareStatusOf
} from '@/api/plugin-center/pluginDeveloper'

const loading = ref(false)
const creating = ref(false)
const developerId = ref('')
const batchForm = ref({ developerId: '', from: '', to: '', remark: '' })
const summary = ref({ pendingAmount: 0, settledAmount: 0, monthAmount: 0, commissionRate: 0.3 })
const shares = ref([])
const batches = ref([])

const money = (value) => Number(value ?? 0).toFixed(2)
const rateText = computed(() => `${(Number(summary.value.commissionRate ?? 0) * 100).toFixed(1)}%`)

const loadAll = async () => {
  loading.value = true
  const id = developerId.value ? Number(developerId.value) : undefined
  try {
    const [summaryData, shareData, batchData] = await Promise.all([
      getSettlementSummary(id),
      listRevenueShares({ developerId: id, limit: 100 }),
      listSettlementBatches({ developerId: id })
    ])
    summary.value = summaryData || summary.value
    shares.value = shareData?.list ?? []
    batches.value = batchData?.list ?? []
  } catch (error) {
    ElMessage.error(error?.message || '加载结算数据失败')
  } finally {
    loading.value = false
  }
}

const createBatch = async () => {
  if (!batchForm.value.developerId) {
    ElMessage.warning('请填写开发者ID')
    return
  }
  creating.value = true
  try {
    const batch = await createSettlementBatch({
      developerId: Number(batchForm.value.developerId),
      from: batchForm.value.from || undefined,
      to: batchForm.value.to || undefined,
      remark: batchForm.value.remark
    })
    ElMessage.success(`批次 ${batch.batchNo} 已生成，应结 ¥${money(batch.developerAmount)}`)
    await loadAll()
  } catch (error) {
    ElMessage.error(error?.message || '生成批次失败')
  } finally {
    creating.value = false
  }
}

const markPaid = async (row) => {
  const input = await ElMessageBox.prompt('填写打款凭证地址（选填，便于财务留档）', '标记已打款', {
    inputPlaceholder: 'https://...'
  }).catch(() => null)
  if (!input) return
  try {
    await markBatchPaid({ id: row.id, voucherUrl: input.value || '' })
    ElMessage.success('已标记打款')
    await loadAll()
  } catch (error) {
    ElMessage.error(error?.message || '操作失败')
  }
}

const rejectShare = async (row) => {
  const input = await ElMessageBox.prompt('请填写驳回原因（退款 / 风控）', '驳回流水', {
    inputValidator: (value) => (value && value.trim().length >= 2 ? true : '请填写原因')
  }).catch(() => null)
  if (!input) return
  try {
    await rejectRevenueShare({ id: row.id, remark: input.value })
    ElMessage.success('已驳回')
    await loadAll()
  } catch (error) {
    ElMessage.error(error?.message || '操作失败')
  }
}

onMounted(loadAll)
</script>

<style scoped>
.settlement-page { padding: 20px; }
.page-header { display: flex; justify-content: space-between; align-items: flex-start; margin-bottom: 16px; }
.page-header h1 { margin: 0 0 6px; font-size: 20px; }
.page-header p { margin: 0; color: var(--el-text-color-secondary); font-size: 13px; }
.actions { display: flex; gap: 8px; align-items: center; }
.cards { margin-bottom: 4px; }
.metric { margin-bottom: 12px; }
.metric-label { color: var(--el-text-color-secondary); font-size: 12px; }
.metric-value { margin-top: 6px; font-size: 20px; font-weight: 600; }
.metric-value.warn { color: #f59e0b; }
.metric-value.ok { color: #10b981; }
.block { margin-bottom: 16px; }
.hint { margin-left: 12px; color: var(--el-text-color-secondary); font-size: 12px; }
</style>
