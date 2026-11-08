<template>
  <div class="split-batch-container">
    <el-card class="search-card" shadow="never">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="批次编号">
          <el-input v-model="searchForm.batchNo" placeholder="请输入批次编号" clearable style="width: 180px;" />
        </el-form-item>
        <el-form-item label="原订单号">
          <el-input v-model="searchForm.outTradeNo" placeholder="请输入原订单号" clearable style="width: 180px;" />
        </el-form-item>
        <el-form-item label="批次状态">
          <el-select v-model="searchForm.status" placeholder="请选择" clearable style="width: 130px;">
            <el-option label="待分账" value="pending" />
            <el-option label="分账中" value="processing" />
            <el-option label="已完成" value="success" />
            <el-option label="已失败" value="failed" />
          </el-select>
        </el-form-item>
        <el-form-item label="发起时间">
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
          <span class="card-title">批量分账记录</span>
          <div>
            <el-button type="primary" @click="handleCreate"><el-icon><Plus /></el-icon>发起分账</el-button>
            <el-button @click="handleExport"><el-icon><Download /></el-icon>导出</el-button>
          </div>
        </div>
      </template>

      <el-table :data="tableData" v-loading="loading" stripe style="width: 100%;">
        <el-table-column prop="batchNo" label="批次编号" min-width="180" />
        <el-table-column prop="outTradeNo" label="原订单号" min-width="180" />
        <el-table-column prop="totalAmount" label="分账总额" width="140" align="right">
          <template #default="{ row }">¥{{ row.totalAmount }}</template>
        </el-table-column>
        <el-table-column prop="receiverCount" label="分账方数" width="100" align="right" />
        <el-table-column prop="status" label="批次状态" width="100">
          <template #default="{ row }">
            <el-tag size="small" :type="statusTagType[row.status]">{{ statusText[row.status] || row.status }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="发起时间" width="180" />
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

    <!-- 发起分账 -->
    <el-dialog v-model="createDialogVisible" title="发起批量分账" width="700px" :close-on-click-modal="false">
      <el-form :model="createForm" :rules="createRules" ref="createFormRef" label-width="100px">
        <el-form-item label="原订单号" prop="outTradeNo">
          <el-input v-model="createForm.outTradeNo" placeholder="请输入需分账的原交易订单号" />
        </el-form-item>
        <el-divider content-position="left">分账接收方</el-divider>
        <div v-for="(item, idx) in createForm.receivers" :key="idx" class="receiver-item">
          <el-row :gutter="12">
            <el-col :span="8"><el-input v-model="item.receiverId" placeholder="接收方编号" /></el-col>
            <el-col :span="8"><el-input v-model="item.receiverName" placeholder="接收方名称" /></el-col>
            <el-col :span="6"><el-input-number v-model="item.amount" :min="0.01" :precision="2" placeholder="金额" style="width: 100%;" /></el-col>
            <el-col :span="2"><el-button type="danger" link @click="removeReceiver(idx)" :disabled="createForm.receivers.length <= 1">删除</el-button></el-col>
          </el-row>
        </div>
        <el-button type="primary" link @click="addReceiver" style="margin-top: 8px;">+ 添加接收方</el-button>
        <el-form-item label="备注" style="margin-top: 16px;">
          <el-input v-model="createForm.remark" type="textarea" :rows="2" placeholder="分账说明" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="createDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="submitCreate">确认分账</el-button>
      </template>
    </el-dialog>

    <!-- 详情 -->
    <el-dialog v-model="detailDialogVisible" title="分账批次详情" width="700px">
      <el-descriptions v-if="currentRow" :column="2" border>
        <el-descriptions-item label="批次编号">{{ currentRow.batchNo }}</el-descriptions-item>
        <el-descriptions-item label="原订单号">{{ currentRow.outTradeNo }}</el-descriptions-item>
        <el-descriptions-item label="分账总额">¥{{ currentRow.totalAmount }}</el-descriptions-item>
        <el-descriptions-item label="分账方数">{{ currentRow.receiverCount }}</el-descriptions-item>
        <el-descriptions-item label="批次状态">
          <el-tag size="small" :type="statusTagType[currentRow.status]">{{ statusText[currentRow.status] }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="发起时间">{{ currentRow.createTime }}</el-descriptions-item>
      </el-descriptions>
      <template #footer>
        <el-button @click="detailDialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
/**
 * 批量分账页面
 * 按规则向多个分账方批量发起分账指令
 */
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Search, Refresh, Plus, Download } from '@element-plus/icons-vue'
import { payApi } from '@/api/payment/pay'

const statusText = { pending: '待分账', processing: '分账中', success: '已完成', failed: '已失败' }
const statusTagType = { pending: 'warning', processing: 'primary', success: 'success', failed: 'danger' }

const loading = ref(false)
const submitLoading = ref(false)
const createDialogVisible = ref(false)
const detailDialogVisible = ref(false)
const currentRow = ref(null)
const tableData = ref([])
const createFormRef = ref(null)

const searchForm = reactive({ batchNo: '', outTradeNo: '', status: '', dateRange: null })
const pagination = reactive({ page: 1, size: 10, total: 0 })
const createForm = reactive({ outTradeNo: '', receivers: [{ receiverId: '', receiverName: '', amount: null }], remark: '' })

const createRules = { outTradeNo: [{ required: true, message: '请输入原订单号', trigger: 'blur' }] }

const addReceiver = () => { createForm.receivers.push({ receiverId: '', receiverName: '', amount: null }) }
const removeReceiver = (idx) => { createForm.receivers.splice(idx, 1) }

const fetchList = async () => {
  loading.value = true
  try {
    const params = { pageNum: pagination.page, pageSize: pagination.size, batchNo: searchForm.batchNo || undefined, outTradeNo: searchForm.outTradeNo || undefined, status: searchForm.status || undefined, startTime: searchForm.dateRange?.[0] || undefined, endTime: searchForm.dateRange?.[1] || undefined }
    const res = await payApi.queryDivision(params)
    if (res && res.code === 200) { tableData.value = res.data?.records || res.data?.list || []; pagination.total = res.data?.total || 0 }
  } catch (e) { console.error('查询分账列表失败:', e) } finally { loading.value = false }
}

const handleSearch = () => { pagination.page = 1; fetchList() }
const handleReset = () => { Object.assign(searchForm, { batchNo: '', outTradeNo: '', status: '', dateRange: null }); handleSearch() }
const handleDetail = (row) => { currentRow.value = row; detailDialogVisible.value = true }
const handleExport = () => { ElMessage.info('导出功能开发中') }

const handleCreate = () => {
  Object.assign(createForm, { outTradeNo: '', receivers: [{ receiverId: '', receiverName: '', amount: null }], remark: '' })
  createDialogVisible.value = true
}

const submitCreate = async () => {
  if (!createForm.outTradeNo) { ElMessage.warning('请输入原订单号'); return }
  const validReceivers = createForm.receivers.filter(r => r.receiverId && r.amount)
  if (!validReceivers.length) { ElMessage.warning('请至少添加一个有效的分账接收方'); return }
  submitLoading.value = true
  try {
    const res = await payApi.batchDivision({ outTradeNo: createForm.outTradeNo, receivers: validReceivers, remark: createForm.remark })
    if (res && res.code === 200) { ElMessage.success('分账指令已发起'); createDialogVisible.value = false; fetchList() }
    else { ElMessage.error(res?.msg || '分账失败') }
  } catch (e) { ElMessage.error('分账请求异常') } finally { submitLoading.value = false }
}

onMounted(() => { fetchList() })
</script>

<style scoped>
.split-batch-container { padding: 16px; }
.search-card { margin-bottom: 12px; border-radius: 8px; }
.search-card :deep(.el-card__body) { padding: 14px 16px 0 16px; }
.search-form :deep(.el-form-item) { margin-bottom: 14px; }
.table-card { border-radius: 8px; }
.table-card :deep(.el-card__header) { padding: 12px 16px; }
.card-header { display: flex; align-items: center; justify-content: space-between; }
.card-title { font-size: 15px; font-weight: 600; color: #303133; }
.pagination-wrap { display: flex; justify-content: flex-end; margin-top: 12px; }
.receiver-item { margin-bottom: 10px; }
</style>
