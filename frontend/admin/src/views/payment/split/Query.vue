<template>
  <div class="split-query-container">
    <el-card class="search-card" shadow="never">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="分账单号">
          <el-input v-model="searchForm.splitNo" placeholder="请输入分账单号" clearable style="width: 180px;" />
        </el-form-item>
        <el-form-item label="原订单号">
          <el-input v-model="searchForm.outTradeNo" placeholder="请输入原订单号" clearable style="width: 180px;" />
        </el-form-item>
        <el-form-item label="接收方编号">
          <el-input v-model="searchForm.receiverId" placeholder="请输入接收方编号" clearable style="width: 160px;" />
        </el-form-item>
        <el-form-item label="分账状态">
          <el-select v-model="searchForm.status" placeholder="请选择" clearable style="width: 130px;">
            <el-option label="待分账" value="pending" />
            <el-option label="分账中" value="processing" />
            <el-option label="已完成" value="success" />
            <el-option label="已失败" value="failed" />
          </el-select>
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
          <span class="card-title">分账明细列表</span>
          <el-button @click="handleExport"><el-icon><Download /></el-icon>导出</el-button>
        </div>
      </template>

      <el-table :data="tableData" v-loading="loading" stripe style="width: 100%;">
        <el-table-column prop="splitNo" label="分账单号" min-width="180" />
        <el-table-column prop="outTradeNo" label="原订单号" min-width="180" />
        <el-table-column prop="receiverName" label="接收方" min-width="160" />
        <el-table-column prop="amount" label="分账金额" width="120" align="right">
          <template #default="{ row }">¥{{ row.amount }}</template>
        </el-table-column>
        <el-table-column prop="status" label="分账状态" width="100">
          <template #default="{ row }">
            <el-tag size="small" :type="statusTagType[row.status]">{{ statusText[row.status] || row.status }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="finishTime" label="完成时间" width="180">
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

    <el-dialog v-model="detailDialogVisible" title="分账明细详情" width="650px">
      <el-descriptions v-if="currentRow" :column="2" border>
        <el-descriptions-item label="分账单号">{{ currentRow.splitNo }}</el-descriptions-item>
        <el-descriptions-item label="原订单号">{{ currentRow.outTradeNo }}</el-descriptions-item>
        <el-descriptions-item label="接收方">{{ currentRow.receiverName }}</el-descriptions-item>
        <el-descriptions-item label="接收方编号">{{ currentRow.receiverId }}</el-descriptions-item>
        <el-descriptions-item label="分账金额">¥{{ currentRow.amount }}</el-descriptions-item>
        <el-descriptions-item label="分账状态">
          <el-tag size="small" :type="statusTagType[currentRow.status]">{{ statusText[currentRow.status] }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="发起时间">{{ currentRow.createTime || '-' }}</el-descriptions-item>
        <el-descriptions-item label="完成时间">{{ currentRow.finishTime || '-' }}</el-descriptions-item>
      </el-descriptions>
      <template #footer>
        <el-button @click="detailDialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
/**
 * 分账查询页面
 * 查询分账明细与接收方到账状态
 */
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Search, Refresh, Download } from '@element-plus/icons-vue'
import { payApi } from '@/api/payment/pay'

const statusText = { pending: '待分账', processing: '分账中', success: '已完成', failed: '已失败' }
const statusTagType = { pending: 'warning', processing: 'primary', success: 'success', failed: 'danger' }

const loading = ref(false)
const detailDialogVisible = ref(false)
const currentRow = ref(null)
const tableData = ref([])

const searchForm = reactive({ splitNo: '', outTradeNo: '', receiverId: '', status: '' })
const pagination = reactive({ page: 1, size: 10, total: 0 })

const fetchList = async () => {
  loading.value = true
  try {
    const params = { pageNum: pagination.page, pageSize: pagination.size, splitNo: searchForm.splitNo || undefined, outTradeNo: searchForm.outTradeNo || undefined, receiverId: searchForm.receiverId || undefined, status: searchForm.status || undefined }
    const res = await payApi.queryDivision(params)
    if (res && res.code === 200) { tableData.value = res.data?.records || res.data?.list || []; pagination.total = res.data?.total || 0 }
  } catch (e) { console.error('查询分账明细失败:', e) } finally { loading.value = false }
}

const handleSearch = () => { pagination.page = 1; fetchList() }
const handleReset = () => { Object.assign(searchForm, { splitNo: '', outTradeNo: '', receiverId: '', status: '' }); handleSearch() }
const handleDetail = (row) => { currentRow.value = row; detailDialogVisible.value = true }
const handleExport = () => { ElMessage.info('导出功能开发中') }

onMounted(() => { fetchList() })
</script>

<style scoped>
.split-query-container { padding: 16px; }
.search-card { margin-bottom: 12px; border-radius: 8px; }
.search-card :deep(.el-card__body) { padding: 14px 16px 0 16px; }
.search-form :deep(.el-form-item) { margin-bottom: 14px; }
.table-card { border-radius: 8px; }
.table-card :deep(.el-card__header) { padding: 12px 16px; }
.card-header { display: flex; align-items: center; justify-content: space-between; }
.card-title { font-size: 15px; font-weight: 600; color: #303133; }
.pagination-wrap { display: flex; justify-content: flex-end; margin-top: 12px; }
</style>
