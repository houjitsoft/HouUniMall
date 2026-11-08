<template>
  <div class="withdraw-log-container">
    <el-card class="search-card" shadow="never">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="提现单号">
          <el-input v-model="searchForm.withdrawNo" placeholder="请输入提现单号" clearable style="width: 180px;" />
        </el-form-item>
        <el-form-item label="会员编号">
          <el-input v-model="searchForm.memberNo" placeholder="请输入会员编号" clearable style="width: 160px;" />
        </el-form-item>
        <el-form-item label="提现状态">
          <el-select v-model="searchForm.status" placeholder="请选择" clearable style="width: 130px;">
            <el-option label="审核中" value="auditing" />
            <el-option label="处理中" value="processing" />
            <el-option label="已到账" value="success" />
            <el-option label="已失败" value="failed" />
          </el-select>
        </el-form-item>
        <el-form-item label="申请时间">
          <el-date-picker v-model="searchForm.dateRange" type="daterange" range-separator="至" start-placeholder="开始日期" end-placeholder="结束日期" style="width: 240px;" />
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
          <span class="card-title">提现记录</span>
          <el-button @click="handleExport"><el-icon><Download /></el-icon>导出</el-button>
        </div>
      </template>

      <el-table :data="tableData" v-loading="loading" stripe style="width: 100%;">
        <el-table-column prop="withdrawNo" label="提现单号" min-width="180" />
        <el-table-column prop="memberName" label="会员名称" min-width="160" />
        <el-table-column prop="amount" label="提现金额" width="120" align="right">
          <template #default="{ row }">¥{{ row.amount }}</template>
        </el-table-column>
        <el-table-column prop="bankName" label="到账银行" width="140" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag size="small" :type="statusTagType[row.status]">{{ statusText[row.status] || row.status }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="applyTime" label="申请时间" width="180" />
        <el-table-column prop="finishTime" label="到账时间" width="180">
          <template #default="{ row }">{{ row.finishTime || '-' }}</template>
        </el-table-column>
        <el-table-column label="操作" width="100" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click="handleDetail(row)">详情</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-wrap">
        <el-pagination v-model:current-page="pagination.page" v-model:page-size="pagination.size" :page-sizes="[10, 20, 50, 100]" :total="pagination.total" layout="total, sizes, prev, pager, next, jumper" background @size-change="fetchList" @current-change="fetchList" />
      </div>
    </el-card>

    <!-- 详情 -->
    <el-dialog v-model="detailDialogVisible" title="提现详情" width="650px">
      <el-descriptions v-if="currentRow" :column="2" border>
        <el-descriptions-item label="提现单号">{{ currentRow.withdrawNo }}</el-descriptions-item>
        <el-descriptions-item label="会员名称">{{ currentRow.memberName }}</el-descriptions-item>
        <el-descriptions-item label="提现金额">¥{{ currentRow.amount }}</el-descriptions-item>
        <el-descriptions-item label="到账银行">{{ currentRow.bankName }}</el-descriptions-item>
        <el-descriptions-item label="银行卡号">{{ currentRow.bankCard || '-' }}</el-descriptions-item>
        <el-descriptions-item label="提现状态">
          <el-tag size="small" :type="statusTagType[currentRow.status]">{{ statusText[currentRow.status] }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="申请时间">{{ currentRow.applyTime }}</el-descriptions-item>
        <el-descriptions-item label="到账时间">{{ currentRow.finishTime || '-' }}</el-descriptions-item>
        <el-descriptions-item label="失败原因" v-if="currentRow.status === 'failed'">{{ currentRow.failReason || '-' }}</el-descriptions-item>
      </el-descriptions>
      <template #footer>
        <el-button @click="detailDialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
/**
 * 提现记录页面
 * 查看历史提现申请与到账流水
 */
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Search, Refresh, Download } from '@element-plus/icons-vue'
import { payApi } from '@/api/payment/pay'

const statusText = { auditing: '审核中', processing: '处理中', success: '已到账', failed: '已失败' }
const statusTagType = { auditing: 'warning', processing: 'primary', success: 'success', failed: 'danger' }

const loading = ref(false)
const detailDialogVisible = ref(false)
const currentRow = ref(null)
const tableData = ref([])

const searchForm = reactive({ withdrawNo: '', memberNo: '', status: '', dateRange: null })
const pagination = reactive({ page: 1, size: 10, total: 0 })

const mapStatus = (s) => {
  const v = String(s == null ? '' : s)
  const map = {
    '0': 'auditing',
    '1': 'processing',
    '2': 'success',
    '3': 'success',
    '4': 'failed',
    'AUDITING': 'auditing',
    'PROCESSING': 'processing',
    'SUCCESS': 'success',
    'FAILED': 'failed'
  }
  return map[v] || map[s] || 'auditing'
}

const fetchList = async () => {
  loading.value = true
  try {
    const params = { pageNum: pagination.page, pageSize: pagination.size, withdrawNo: searchForm.withdrawNo || undefined, memberNo: searchForm.memberNo || undefined, status: searchForm.status || undefined, startTime: searchForm.dateRange?.[0] || undefined, endTime: searchForm.dateRange?.[1] || undefined }
    const res = await payApi.getWithdrawList(params)
    if (res && res.code === 200) {
      const records = res.data?.records || res.data?.list || []
      tableData.value = records.map((r) => ({
        id: r.id,
        withdrawNo: r.orderid || r.withdrawNo || r.id,
        memberName: r.userName || r.shopName || r.payUid || '',
        memberNo: r.payUid || '',
        amount: r.amount ?? 0,
        bankName: r.bank || '',
        bankCard: r.cardno || '',
        status: mapStatus(r.isSucceed),
        applyTime: r.addTime || '',
        finishTime: r.checkTime || '',
        failReason: r.con || '',
        _raw: r
      }))
      pagination.total = res.data?.total || 0
    }
  } catch (e) { console.error('查询提现记录失败:', e) } finally { loading.value = false }
}

const handleSearch = () => { pagination.page = 1; fetchList() }
const handleReset = () => { Object.assign(searchForm, { withdrawNo: '', memberNo: '', status: '', dateRange: null }); handleSearch() }
const handleDetail = (row) => { currentRow.value = row; detailDialogVisible.value = true }
const handleExport = () => { ElMessage.info('导出功能开发中') }

onMounted(() => { fetchList() })
</script>

<style scoped>
.withdraw-log-container { padding: 16px; }
.search-card { margin-bottom: 12px; border-radius: 8px; }
.search-card :deep(.el-card__body) { padding: 14px 16px 0 16px; }
.search-form :deep(.el-form-item) { margin-bottom: 14px; }
.table-card { border-radius: 8px; }
.table-card :deep(.el-card__header) { padding: 12px 16px; }
.card-header { display: flex; align-items: center; justify-content: space-between; }
.card-title { font-size: 15px; font-weight: 600; color: #303133; }
.pagination-wrap { display: flex; justify-content: flex-end; margin-top: 12px; }
</style>
