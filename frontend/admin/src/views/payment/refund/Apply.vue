<template>
  <div class="refund-apply-container">
    <!-- 搜索筛选 -->
    <el-card class="search-card" shadow="never">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="原订单号">
          <el-input v-model="searchForm.outTradeNo" placeholder="请输入原订单号" clearable style="width: 180px;" />
        </el-form-item>
        <el-form-item label="退款单号">
          <el-input v-model="searchForm.refundNo" placeholder="请输入退款单号" clearable style="width: 180px;" />
        </el-form-item>
        <el-form-item label="审核状态">
          <el-select v-model="searchForm.status" placeholder="请选择" clearable style="width: 140px;">
            <el-option label="待审核" value="pending" />
            <el-option label="审核通过" value="approved" />
            <el-option label="已驳回" value="rejected" />
            <el-option label="退款中" value="processing" />
            <el-option label="退款成功" value="success" />
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

    <!-- 数据表格 -->
    <el-card shadow="never" class="table-card">
      <template #header>
        <div class="card-header">
          <span class="card-title">退款申请列表</span>
          <el-button type="primary" @click="handleCreate"><el-icon><Plus /></el-icon>发起退款</el-button>
        </div>
      </template>

      <el-table :data="tableData" v-loading="loading" stripe style="width: 100%;">
        <el-table-column prop="refundNo" label="退款单号" min-width="180" />
        <el-table-column prop="outTradeNo" label="原订单号" min-width="180" />
        <el-table-column prop="refundAmount" label="退款金额" width="120" align="right">
          <template #default="{ row }">¥{{ row.refundAmount }}</template>
        </el-table-column>
        <el-table-column prop="reason" label="退款原因" min-width="160" />
        <el-table-column prop="status" label="审核状态" width="100">
          <template #default="{ row }">
            <el-tag size="small" :type="statusTagType[row.status]">{{ statusText[row.status] || row.status }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="applyTime" label="申请时间" width="180" />
        <el-table-column label="操作" width="140" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click="handleDetail(row)">详情</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-wrap">
        <el-pagination v-model:current-page="pagination.page" v-model:page-size="pagination.size" :page-sizes="[10, 20, 50, 100]" :total="pagination.total" layout="total, sizes, prev, pager, next, jumper" background @size-change="fetchList" @current-change="fetchList" />
      </div>
    </el-card>

    <!-- 发起退款对话框 -->
    <el-dialog v-model="createDialogVisible" title="发起退款申请" width="600px" :close-on-click-modal="false">
      <el-form :model="createForm" :rules="createRules" ref="createFormRef" label-width="100px">
        <el-form-item label="原订单号" prop="outTradeNo">
          <el-input v-model="createForm.outTradeNo" placeholder="请输入需退款的原订单号" />
        </el-form-item>
        <el-form-item label="退款金额" prop="refundAmount">
          <el-input-number v-model="createForm.refundAmount" :min="0.01" :precision="2" :step="1" style="width: 100%;" />
        </el-form-item>
        <el-form-item label="退款原因" prop="reason">
          <el-select v-model="createForm.reason" placeholder="请选择退款原因" style="width: 100%;">
            <el-option label="用户申请退款" value="用户申请退款" />
            <el-option label="商品质量问题" value="商品质量问题" />
            <el-option label="服务未提供" value="服务未提供" />
            <el-option label="重复支付" value="重复支付" />
            <el-option label="其他" value="其他" />
          </el-select>
        </el-form-item>
        <el-form-item label="退款说明">
          <el-input v-model="createForm.remark" type="textarea" :rows="3" placeholder="补充退款说明（选填）" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="createDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="submitCreate">提交申请</el-button>
      </template>
    </el-dialog>

    <!-- 详情对话框 -->
    <el-dialog v-model="detailDialogVisible" title="退款申请详情" width="650px">
      <el-descriptions v-if="currentRow" :column="2" border>
        <el-descriptions-item label="退款单号">{{ currentRow.refundNo }}</el-descriptions-item>
        <el-descriptions-item label="原订单号">{{ currentRow.outTradeNo }}</el-descriptions-item>
        <el-descriptions-item label="退款金额">¥{{ currentRow.refundAmount }}</el-descriptions-item>
        <el-descriptions-item label="退款原因">{{ currentRow.reason }}</el-descriptions-item>
        <el-descriptions-item label="审核状态">
          <el-tag size="small" :type="statusTagType[currentRow.status]">{{ statusText[currentRow.status] }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="申请时间">{{ currentRow.applyTime }}</el-descriptions-item>
        <el-descriptions-item label="审核时间">{{ currentRow.auditTime || '-' }}</el-descriptions-item>
        <el-descriptions-item label="退款说明">{{ currentRow.remark || '-' }}</el-descriptions-item>
      </el-descriptions>
      <template #footer>
        <el-button @click="detailDialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
/**
 * 退款申请页面
 * 对已支付订单发起退款申请单
 */
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Search, Refresh, Plus } from '@element-plus/icons-vue'
import { payApi } from '@/api/payment/pay'

const statusText = { pending: '待审核', approved: '审核通过', rejected: '已驳回', processing: '退款中', success: '退款成功' }
const statusTagType = { pending: 'warning', approved: 'success', rejected: 'danger', processing: 'primary', success: 'success' }

const loading = ref(false)
const submitLoading = ref(false)
const createDialogVisible = ref(false)
const detailDialogVisible = ref(false)
const currentRow = ref(null)
const tableData = ref([])
const createFormRef = ref(null)

const searchForm = reactive({ outTradeNo: '', refundNo: '', status: '', dateRange: null })
const pagination = reactive({ page: 1, size: 10, total: 0 })
const createForm = reactive({ outTradeNo: '', refundAmount: null, reason: '', remark: '' })

const createRules = {
  outTradeNo: [{ required: true, message: '请输入原订单号', trigger: 'blur' }],
  refundAmount: [{ required: true, message: '请输入退款金额', trigger: 'blur' }],
  reason: [{ required: true, message: '请选择退款原因', trigger: 'change' }]
}

const fetchList = async () => {
  loading.value = true
  try {
    const params = {
      pageNum: pagination.page,
      pageSize: pagination.size,
      outTradeNo: searchForm.outTradeNo || undefined,
      refundNo: searchForm.refundNo || undefined,
      status: searchForm.status || undefined,
      startTime: searchForm.dateRange?.[0] || undefined,
      endTime: searchForm.dateRange?.[1] || undefined
    }
    const res = await payApi.queryTradeRefund(params)
    if (res && res.code === 200) {
      tableData.value = res.data?.records || res.data?.list || []
      pagination.total = res.data?.total || 0
    }
  } catch (e) {
    console.error('查询退款申请列表失败:', e)
  } finally {
    loading.value = false
  }
}

const handleSearch = () => { pagination.page = 1; fetchList() }
const handleReset = () => { Object.assign(searchForm, { outTradeNo: '', refundNo: '', status: '', dateRange: null }); handleSearch() }
const handleDetail = (row) => { currentRow.value = row; detailDialogVisible.value = true }

const handleCreate = () => {
  Object.assign(createForm, { outTradeNo: '', refundAmount: null, reason: '', remark: '' })
  createDialogVisible.value = true
}

const submitCreate = async () => {
  const valid = await createFormRef.value?.validate().catch(() => false)
  if (!valid) return
  submitLoading.value = true
  try {
    const res = await payApi.tradeRefund(createForm)
    if (res && res.code === 200) {
      ElMessage.success('退款申请已提交')
      createDialogVisible.value = false
      fetchList()
    } else {
      ElMessage.error(res?.msg || '提交失败')
    }
  } catch (e) {
    ElMessage.error('提交退款请求异常')
  } finally {
    submitLoading.value = false
  }
}

onMounted(() => { fetchList() })
</script>

<style scoped>
.refund-apply-container { padding: 16px; }
.search-card { margin-bottom: 12px; border-radius: 8px; }
.search-card :deep(.el-card__body) { padding: 14px 16px 0 16px; }
.search-form :deep(.el-form-item) { margin-bottom: 14px; }
.table-card { border-radius: 8px; }
.table-card :deep(.el-card__header) { padding: 12px 16px; }
.card-header { display: flex; align-items: center; justify-content: space-between; }
.card-title { font-size: 15px; font-weight: 600; color: #303133; }
.pagination-wrap { display: flex; justify-content: flex-end; margin-top: 12px; }
</style>
