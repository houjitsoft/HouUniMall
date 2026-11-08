<template>
  <div class="split-cancel-container">
    <el-card class="search-card" shadow="never">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="分账单号">
          <el-input v-model="searchForm.splitNo" placeholder="请输入分账单号" clearable style="width: 180px;" />
        </el-form-item>
        <el-form-item label="原订单号">
          <el-input v-model="searchForm.outTradeNo" placeholder="请输入原订单号" clearable style="width: 180px;" />
        </el-form-item>
        <el-form-item label="操作人">
          <el-input v-model="searchForm.operator" placeholder="请输入操作人" clearable style="width: 140px;" />
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
          <span class="card-title">分账撤销记录</span>
        </div>
      </template>

      <el-table :data="tableData" v-loading="loading" stripe style="width: 100%;">
        <el-table-column prop="splitNo" label="分账单号" min-width="180" />
        <el-table-column prop="outTradeNo" label="原订单号" min-width="180" />
        <el-table-column prop="amount" label="撤销金额" width="120" align="right">
          <template #default="{ row }">¥{{ row.amount }}</template>
        </el-table-column>
        <el-table-column prop="reason" label="撤销原因" min-width="160" />
        <el-table-column prop="status" label="撤销状态" width="100">
          <template #default="{ row }">
            <el-tag size="small" :type="cancelStatusType[row.status]">{{ cancelStatusText[row.status] || row.status }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="operator" label="操作人" width="120" />
        <el-table-column prop="cancelTime" label="撤销时间" width="180" />
        <el-table-column label="操作" width="140" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click="handleDetail(row)">详情</el-button>
            <el-button link type="danger" size="small" v-if="row.status === 'pending'" @click="handleCancel(row)">撤销</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-wrap">
        <el-pagination v-model:current-page="pagination.page" v-model:page-size="pagination.size" :page-sizes="[10, 20, 50, 100]" :total="pagination.total" layout="total, sizes, prev, pager, next, jumper" background @size-change="fetchList" @current-change="fetchList" />
      </div>
    </el-card>

    <!-- 撤销对话框 -->
    <el-dialog v-model="cancelDialogVisible" title="分账撤销" width="500px" :close-on-click-modal="false">
      <el-form :model="cancelForm" label-width="100px">
        <el-form-item label="分账单号">
          <el-input :model-value="cancelForm.splitNo" disabled />
        </el-form-item>
        <el-form-item label="撤销原因" required>
          <el-select v-model="cancelForm.reason" placeholder="请选择撤销原因" style="width: 100%;">
            <el-option label="商户主动撤销" value="商户主动撤销" />
            <el-option label="交易已退款" value="交易已退款" />
            <el-option label="分账信息有误" value="分账信息有误" />
            <el-option label="其他原因" value="其他原因" />
          </el-select>
        </el-form-item>
        <el-form-item label="补充说明">
          <el-input v-model="cancelForm.remark" type="textarea" :rows="2" placeholder="补充说明" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="cancelDialogVisible = false">取消</el-button>
        <el-button type="danger" :loading="submitLoading" @click="confirmCancel">确认撤销</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="detailDialogVisible" title="撤销详情" width="600px">
      <el-descriptions v-if="currentRow" :column="2" border>
        <el-descriptions-item label="分账单号">{{ currentRow.splitNo }}</el-descriptions-item>
        <el-descriptions-item label="原订单号">{{ currentRow.outTradeNo }}</el-descriptions-item>
        <el-descriptions-item label="撤销金额">¥{{ currentRow.amount }}</el-descriptions-item>
        <el-descriptions-item label="撤销原因">{{ currentRow.reason }}</el-descriptions-item>
        <el-descriptions-item label="操作人">{{ currentRow.operator }}</el-descriptions-item>
        <el-descriptions-item label="撤销时间">{{ currentRow.cancelTime }}</el-descriptions-item>
      </el-descriptions>
      <template #footer>
        <el-button @click="detailDialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
/**
 * 分账撤销页面
 * 对未完成的分账记录进行撤销操作
 */
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Search, Refresh } from '@element-plus/icons-vue'
import { payApi } from '@/api/payment/pay'

const cancelStatusText = { pending: '待撤销', processing: '撤销中', success: '已撤销', failed: '撤销失败' }
const cancelStatusType = { pending: 'warning', processing: 'primary', success: 'success', failed: 'danger' }

const loading = ref(false)
const submitLoading = ref(false)
const cancelDialogVisible = ref(false)
const detailDialogVisible = ref(false)
const currentRow = ref(null)
const tableData = ref([])

const searchForm = reactive({ splitNo: '', outTradeNo: '', operator: '' })
const pagination = reactive({ page: 1, size: 10, total: 0 })
const cancelForm = reactive({ splitNo: '', reason: '', remark: '' })

const fetchList = async () => {
  loading.value = true
  try {
    const params = { pageNum: pagination.page, pageSize: pagination.size, splitNo: searchForm.splitNo || undefined, outTradeNo: searchForm.outTradeNo || undefined, operator: searchForm.operator || undefined, queryType: 'cancel' }
    const res = await payApi.queryDivision(params)
    if (res && res.code === 200) { tableData.value = res.data?.records || res.data?.list || []; pagination.total = res.data?.total || 0 }
  } catch (e) { console.error('查询撤销记录失败:', e) } finally { loading.value = false }
}

const handleSearch = () => { pagination.page = 1; fetchList() }
const handleReset = () => { Object.assign(searchForm, { splitNo: '', outTradeNo: '', operator: '' }); handleSearch() }
const handleDetail = (row) => { currentRow.value = row; detailDialogVisible.value = true }

const handleCancel = (row) => {
  Object.assign(cancelForm, { splitNo: row.splitNo, reason: '', remark: '' })
  cancelDialogVisible.value = true
}

const confirmCancel = async () => {
  if (!cancelForm.reason) { ElMessage.warning('请选择撤销原因'); return }
  submitLoading.value = true
  try {
    const res = await payApi.refundDivision(cancelForm)
    if (res && res.code === 200) { ElMessage.success('分账撤销已提交'); cancelDialogVisible.value = false; fetchList() }
    else { ElMessage.error(res?.msg || '撤销失败') }
  } catch (e) { ElMessage.error('撤销请求异常') } finally { submitLoading.value = false }
}

onMounted(() => { fetchList() })
</script>

<style scoped>
.split-cancel-container { padding: 16px; }
.search-card { margin-bottom: 12px; border-radius: 8px; }
.search-card :deep(.el-card__body) { padding: 14px 16px 0 16px; }
.search-form :deep(.el-form-item) { margin-bottom: 14px; }
.table-card { border-radius: 8px; }
.table-card :deep(.el-card__header) { padding: 12px 16px; }
.card-header { display: flex; align-items: center; justify-content: space-between; }
.card-title { font-size: 15px; font-weight: 600; color: #303133; }
.pagination-wrap { display: flex; justify-content: flex-end; margin-top: 12px; }
</style>
