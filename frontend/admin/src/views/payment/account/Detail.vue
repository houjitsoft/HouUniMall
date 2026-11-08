<template>
  <div class="account-detail-container">
    <el-card class="search-card" shadow="never">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="会员编号">
          <el-input v-model="searchForm.memberNo" placeholder="请输入会员编号" clearable style="width: 160px;" />
        </el-form-item>
        <el-form-item label="业务类型">
          <el-select v-model="searchForm.bizType" placeholder="请选择" clearable style="width: 130px;">
            <el-option label="充值" value="recharge" />
            <el-option label="消费" value="consume" />
            <el-option label="退款" value="refund" />
            <el-option label="提现" value="withdraw" />
            <el-option label="分账" value="split" />
          </el-select>
        </el-form-item>
        <el-form-item label="变动方向">
          <el-select v-model="searchForm.direction" placeholder="请选择" clearable style="width: 120px;">
            <el-option label="收入" value="in" />
            <el-option label="支出" value="out" />
          </el-select>
        </el-form-item>
        <el-form-item label="发生时间">
          <el-date-picker v-model="searchForm.dateRange" type="daterange" range-separator="至" start-placeholder="开始" end-placeholder="结束" style="width: 240px;" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch"><el-icon><Search /></el-icon>搜索</el-button>
          <el-button @click="handleReset"><el-icon><Refresh /></el-icon>重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card shadow="never" class="table-card">
      <template #header>
        <div class="card-header">
          <span class="card-title">账户明细</span>
          <el-button @click="handleExport"><el-icon><Download /></el-icon>导出</el-button>
        </div>
      </template>

      <el-table :data="tableData" v-loading="loading" stripe style="width: 100%;">
        <el-table-column prop="serialNo" label="流水号" min-width="180" />
        <el-table-column prop="memberName" label="会员名称" min-width="160" />
        <el-table-column prop="bizType" label="业务类型" width="100">
          <template #default="{ row }">
            <el-tag size="small" :type="bizTypeTag[row.bizType]">{{ bizTypeText[row.bizType] || row.bizType }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="changeAmount" label="变动金额" width="120" align="right">
          <template #default="{ row }">
            <span :class="row.direction === 'in' ? 'amount-in' : 'amount-out'">
              {{ row.direction === 'in' ? '+' : '-' }}¥{{ row.changeAmount }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="balance" label="账户余额" width="120" align="right">
          <template #default="{ row }">¥{{ row.balance }}</template>
        </el-table-column>
        <el-table-column prop="remark" label="备注" min-width="160">
          <template #default="{ row }">{{ row.remark || '-' }}</template>
        </el-table-column>
        <el-table-column prop="createTime" label="发生时间" width="180" />
        <el-table-column label="操作" width="80" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click="handleDetail(row)">详情</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-wrap">
        <el-pagination v-model:current-page="pagination.page" v-model:page-size="pagination.size" :page-sizes="[10, 20, 50, 100]" :total="pagination.total" layout="total, sizes, prev, pager, next, jumper" background @size-change="fetchList" @current-change="fetchList" />
      </div>
    </el-card>

    <el-dialog v-model="detailDialogVisible" title="明细详情" width="600px">
      <el-descriptions v-if="currentRow" :column="2" border>
        <el-descriptions-item label="流水号">{{ currentRow.serialNo }}</el-descriptions-item>
        <el-descriptions-item label="会员名称">{{ currentRow.memberName }}</el-descriptions-item>
        <el-descriptions-item label="业务类型">{{ bizTypeText[currentRow.bizType] }}</el-descriptions-item>
        <el-descriptions-item label="变动方向">{{ currentRow.direction === 'in' ? '收入' : '支出' }}</el-descriptions-item>
        <el-descriptions-item label="变动金额">
          <span :class="currentRow.direction === 'in' ? 'amount-in' : 'amount-out'">
            {{ currentRow.direction === 'in' ? '+' : '-' }}¥{{ currentRow.changeAmount }}
          </span>
        </el-descriptions-item>
        <el-descriptions-item label="账户余额">¥{{ currentRow.balance }}</el-descriptions-item>
        <el-descriptions-item label="发生时间">{{ currentRow.createTime }}</el-descriptions-item>
        <el-descriptions-item label="备注">{{ currentRow.remark || '-' }}</el-descriptions-item>
      </el-descriptions>
      <template #footer>
        <el-button @click="detailDialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
/**
 * 账户明细页面
 * 查看会员账户的资金变动明细流水
 */
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Search, Refresh, Download } from '@element-plus/icons-vue'
import { payApi } from '@/api/payment/pay'

const bizTypeText = { recharge: '充值', consume: '消费', refund: '退款', withdraw: '提现', split: '分账' }
const bizTypeTag = { recharge: 'success', consume: 'primary', refund: 'warning', withdraw: 'info', split: '' }

const loading = ref(false)
const detailDialogVisible = ref(false)
const currentRow = ref(null)
const tableData = ref([])

const searchForm = reactive({ memberNo: '', bizType: '', direction: '', dateRange: null })
const pagination = reactive({ page: 1, size: 10, total: 0 })

const fetchList = async () => {
  loading.value = true
  try {
    const params = { pageNum: pagination.page, pageSize: pagination.size, memberNo: searchForm.memberNo || undefined, bizType: searchForm.bizType || undefined, direction: searchForm.direction || undefined, startTime: searchForm.dateRange?.[0] || undefined, endTime: searchForm.dateRange?.[1] || undefined }
    const res = await payApi.getAssetDetails(params)
    if (res && res.code === 200) {
      const records = res.data?.records || res.data?.list || []
      tableData.value = records.map((r) => ({
        id: r.id,
        serialNo: r.consumeRecordId || r.recordPayorder || r.id,
        memberName: r.userNickname || r.userId || '',
        memberNo: r.userId || '',
        bizType: mapBizType(r.detailType ?? r.tradeTypeId),
        changeAmount: r.recordMoney ?? 0,
        direction: (r.plusOrMinus === 2 || r.recordMoney < 0) ? 'out' : 'in',
        balance: r.recordAfterMoney ?? r.creditRemain ?? 0,
        remark: r.recordTitle || r.recordDesc || '',
        createTime: r.recordTime || r.recordDate || '',
        _raw: r
      }))
      pagination.total = res.data?.total || 0
    }
  } catch (e) { console.error('查询账户明细失败:', e) } finally { loading.value = false }
}

const mapBizType = (type) => {
  const map = { 1: 'recharge', 2: 'withdraw', 3: 'consume', 4: 'transfer', 5: 'refund', 6: 'split' }
  return map[type] || map[String(type)] || 'consume'
}

const handleSearch = () => { pagination.page = 1; fetchList() }
const handleReset = () => { Object.assign(searchForm, { memberNo: '', bizType: '', direction: '', dateRange: null }); handleSearch() }
const handleDetail = (row) => { currentRow.value = row; detailDialogVisible.value = true }
const handleExport = () => { ElMessage.info('导出功能开发中') }

onMounted(() => { fetchList() })
</script>

<style scoped>
.account-detail-container { padding: 16px; }
.search-card { margin-bottom: 12px; border-radius: 8px; }
.search-card :deep(.el-card__body) { padding: 14px 16px 0 16px; }
.search-form :deep(.el-form-item) { margin-bottom: 14px; }
.table-card { border-radius: 8px; }
.table-card :deep(.el-card__header) { padding: 12px 16px; }
.card-header { display: flex; align-items: center; justify-content: space-between; }
.card-title { font-size: 15px; font-weight: 600; color: #303133; }
.pagination-wrap { display: flex; justify-content: flex-end; margin-top: 12px; }
.amount-in { color: #67c23a; font-weight: 600; }
.amount-out { color: #f56c6c; font-weight: 600; }
</style>
