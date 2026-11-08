<template>
  <div class="ai-balance" v-loading="pageLoading">
    <div class="page-header">
      <h1>AI余额配额</h1>
      <p>按主体（租户 / 用户 / 应用）管理可用额度（hou_ai_balance）：可用 = 免费额度 + 已购 − 已用</p>
    </div>

    <el-card class="balance-container" shadow="never">
      <template #header>
        <div class="card-header">
          <span>额度列表</span>
          <el-button type="primary" plain @click="loadList">
            <el-icon><Refresh /></el-icon>
            刷新
          </el-button>
        </div>
      </template>

      <div class="filter-bar">
        <el-input v-model="filters.subjectType" placeholder="主体类型，如 tenant" clearable style="width: 200px" />
        <el-input v-model="filters.subjectKey" placeholder="主体标识，如 1" clearable style="width: 200px" />
        <el-button type="primary" @click="handleSearch">查询</el-button>
        <el-button @click="handleReset">重置</el-button>
      </div>

      <el-table v-if="list.length" :data="list" style="width: 100%" size="small">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="subjectType" label="主体类型" width="120" />
        <el-table-column prop="subjectKey" label="主体标识" width="120" />
        <el-table-column label="可用额度" min-width="200">
          <template #default="scope">
            <div class="quota-cell">
              <el-progress
                :percentage="usagePercent(scope.row)"
                :stroke-width="10"
                :color="progressColor(scope.row)"
              />
              <span class="quota-text">
                {{ formatNumber(scope.row.availableTokens) }} / {{ formatNumber(totalQuota(scope.row)) }}
              </span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="freeQuotaTokens" label="免费额度" width="120" align="right">
          <template #default="scope">{{ formatNumber(scope.row.freeQuotaTokens) }}</template>
        </el-table-column>
        <el-table-column prop="purchasedBalanceTokens" label="已购额度" width="120" align="right">
          <template #default="scope">{{ formatNumber(scope.row.purchasedBalanceTokens) }}</template>
        </el-table-column>
        <el-table-column prop="usedTokens" label="已用" width="120" align="right">
          <template #default="scope">{{ formatNumber(scope.row.usedTokens) }}</template>
        </el-table-column>
        <el-table-column label="免费额度有效期" width="230">
          <template #default="scope">
            <span v-if="scope.row.freeQuotaStartedAt || scope.row.freeQuotaExpiresAt">
              {{ scope.row.freeQuotaStartedAt || '-' }} ~ {{ scope.row.freeQuotaExpiresAt || '-' }}
            </span>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="scope">
            <el-button size="small" @click="openDetail(scope.row)">明细</el-button>
            <el-button size="small" type="primary" @click="openRecharge(scope.row)">充值</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-empty v-else description="暂无额度记录" />

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

    <!-- 明细 -->
    <el-dialog v-model="detailVisible" title="额度明细" width="520px" destroy-on-close>
      <el-descriptions v-if="detail" :column="1" border size="small">
        <el-descriptions-item label="主体类型">{{ detail.subjectType }}</el-descriptions-item>
        <el-descriptions-item label="主体标识">{{ detail.subjectKey }}</el-descriptions-item>
        <el-descriptions-item label="可用额度">{{ formatNumber(detail.availableTokens) }}</el-descriptions-item>
        <el-descriptions-item label="免费额度">{{ formatNumber(detail.freeQuotaTokens) }}</el-descriptions-item>
        <el-descriptions-item label="免费已用">{{ formatNumber(detail.freeUsedTokens) }}</el-descriptions-item>
        <el-descriptions-item label="已购额度">{{ formatNumber(detail.purchasedBalanceTokens) }}</el-descriptions-item>
        <el-descriptions-item label="累计已用">{{ formatNumber(detail.usedTokens) }}</el-descriptions-item>
        <el-descriptions-item label="更新时间">{{ detail.updateTime || '-' }}</el-descriptions-item>
      </el-descriptions>
      <el-empty v-else description="未查询到该主体的额度" />
      <template #footer>
        <el-button @click="detailVisible = false">关闭</el-button>
      </template>
    </el-dialog>

    <!-- 充值 -->
    <el-dialog v-model="rechargeVisible" title="额度充值" width="460px" destroy-on-close>
      <el-form :model="rechargeForm" :rules="rechargeRules" ref="rechargeFormRef" label-width="100px">
        <el-form-item label="主体">
          <span class="subject-text">
            {{ rechargeForm.subjectType }} / {{ rechargeForm.subjectKey }}
          </span>
        </el-form-item>
        <el-form-item label="充值数量" prop="tokens">
          <el-input-number v-model="rechargeForm.tokens" :min="1" :step="10000" controls-position="right" style="width: 100%" />
          <div class="form-tip">单位：token。充值会累加到「已购额度」上。</div>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="rechargeVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="submitRecharge">确认充值</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { Refresh } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { aiApi } from '@/api/ai/ai'

const pageLoading = ref(false)
const saving = ref(false)
const list = ref([])

const filters = reactive({ subjectType: '', subjectKey: '' })
const pagination = reactive({ page: 1, limit: 20, total: 0 })

const detailVisible = ref(false)
const detail = ref(null)

const rechargeVisible = ref(false)
const rechargeFormRef = ref(null)
const rechargeForm = ref({ subjectType: '', subjectKey: '', tokens: 100000 })
const rechargeRules = {
  tokens: [{ required: true, message: '请输入充值数量', trigger: 'change' }]
}

const formatNumber = (n) => Number(n || 0).toLocaleString('zh-CN')

const totalQuota = (row) => Number(row.freeQuotaTokens || 0) + Number(row.purchasedBalanceTokens || 0)

const usagePercent = (row) => {
  const total = totalQuota(row)
  if (!total) return 0
  const used = Number(row.usedTokens || 0)
  return Math.min(100, Math.round((used / total) * 100))
}

const progressColor = (row) => {
  const p = usagePercent(row)
  if (p >= 90) return '#f56c6c'
  if (p >= 70) return '#e6a23c'
  return '#409eff'
}

const loadList = async () => {
  pageLoading.value = true
  try {
    const params = { page: pagination.page, limit: pagination.limit }
    if (filters.subjectType) params.subjectType = filters.subjectType
    if (filters.subjectKey) params.subjectKey = filters.subjectKey
    const res = await aiApi.getBalanceList(params)
    if (res.code === 1 && res.data) {
      list.value = res.data.list || []
      pagination.total = res.data.total || 0
    } else {
      list.value = []
      pagination.total = 0
    }
  } catch (e) {
    ElMessage.error(e.message || '加载额度列表失败')
    list.value = []
  } finally {
    pageLoading.value = false
  }
}

const handleSearch = () => {
  pagination.page = 1
  loadList()
}

const handleReset = () => {
  filters.subjectType = ''
  filters.subjectKey = ''
  pagination.page = 1
  loadList()
}

const handlePageChange = (page) => {
  pagination.page = page
  loadList()
}

const handleSizeChange = (size) => {
  pagination.limit = size
  pagination.page = 1
  loadList()
}

const openDetail = async (row) => {
  detail.value = null
  detailVisible.value = true
  try {
    const res = await aiApi.getBalanceDetail({
      subjectType: row.subjectType,
      subjectKey: row.subjectKey
    })
    if (res.code === 1 && res.data) {
      detail.value = res.data
    } else {
      ElMessage.warning(res.message || '未查询到额度明细')
    }
  } catch (e) {
    ElMessage.error(e.message || '查询明细失败')
  }
}

const openRecharge = (row) => {
  rechargeForm.value = {
    subjectType: row.subjectType,
    subjectKey: row.subjectKey,
    tokens: 100000
  }
  rechargeVisible.value = true
}

const submitRecharge = async () => {
  if (!rechargeFormRef.value) return
  try {
    await rechargeFormRef.value.validate()
    saving.value = true
    const res = await aiApi.rechargeBalance({ ...rechargeForm.value })
    if (res.code === 1 && res.data?.success !== false) {
      ElMessage.success('充值成功')
      rechargeVisible.value = false
      await loadList()
    } else {
      ElMessage.error(res.message || '充值失败')
    }
  } catch (error) {
    if (error?.message) ElMessage.error(error.message)
  } finally {
    saving.value = false
  }
}

onMounted(loadList)
</script>

<style scoped>
.ai-balance {
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

.balance-container {
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

.quota-cell {
  display: flex;
  align-items: center;
  gap: 10px;
}

.quota-cell :deep(.el-progress) {
  flex: 1;
}

.quota-text {
  font-size: 12px;
  color: #606266;
  white-space: nowrap;
}

.subject-text {
  color: #303133;
  font-weight: 600;
}

.form-tip {
  font-size: 12px;
  color: #a8abb2;
  margin-top: 6px;
}

.pagination-wrap {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}
</style>
