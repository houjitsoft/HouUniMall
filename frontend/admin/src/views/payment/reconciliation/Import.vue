<template>
  <div class="bill-import-container">
    <el-card class="search-card" shadow="never">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="文件名称">
          <el-input v-model="searchForm.fileName" placeholder="请输入文件名" clearable style="width: 180px;" />
        </el-form-item>
        <el-form-item label="支付渠道">
          <el-select v-model="searchForm.channel" placeholder="请选择" clearable style="width: 140px;">
            <el-option label="微信支付" value="wechat" />
            <el-option label="支付宝" value="alipay" />
            <el-option label="银联" value="unionpay" />
          </el-select>
        </el-form-item>
        <el-form-item label="导入状态">
          <el-select v-model="searchForm.status" placeholder="请选择" clearable style="width: 130px;">
            <el-option label="待导入" value="pending" />
            <el-option label="导入中" value="processing" />
            <el-option label="导入成功" value="success" />
            <el-option label="导入失败" value="failed" />
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
          <span class="card-title">账单导入记录</span>
          <el-upload :show-file-list="false" :before-upload="handleBeforeUpload" accept=".csv,.xls,.xlsx">
            <el-button type="primary"><el-icon><Upload /></el-icon>导入账单</el-button>
          </el-upload>
        </div>
      </template>

      <el-table :data="tableData" v-loading="loading" stripe style="width: 100%;">
        <el-table-column prop="fileName" label="文件名" min-width="220" />
        <el-table-column prop="channel" label="渠道" width="120">
          <template #default="{ row }">
            <el-tag size="small" :type="channelTagType[row.channel]">{{ channelText[row.channel] || row.channel }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="recordCount" label="记录数" width="100" align="right" />
        <el-table-column prop="status" label="导入状态" width="100">
          <template #default="{ row }">
            <el-tag size="small" :type="statusTagType[row.status]">{{ statusText[row.status] || row.status }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="operator" label="操作人" width="120" />
        <el-table-column prop="importTime" label="导入时间" width="180" />
        <el-table-column label="操作" width="140" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click="handleDetail(row)">详情</el-button>
            <el-button link type="danger" size="small" v-if="row.status === 'failed'" @click="handleRetry(row)">重试</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-wrap">
        <el-pagination v-model:current-page="pagination.page" v-model:page-size="pagination.size" :page-sizes="[10, 20, 50, 100]" :total="pagination.total" layout="total, sizes, prev, pager, next, jumper" background @size-change="fetchList" @current-change="fetchList" />
      </div>
    </el-card>

    <el-dialog v-model="detailDialogVisible" title="导入详情" width="600px">
      <el-descriptions v-if="currentRow" :column="2" border>
        <el-descriptions-item label="文件名">{{ currentRow.fileName }}</el-descriptions-item>
        <el-descriptions-item label="支付渠道">{{ channelText[currentRow.channel] }}</el-descriptions-item>
        <el-descriptions-item label="记录数">{{ currentRow.recordCount }}</el-descriptions-item>
        <el-descriptions-item label="导入状态">
          <el-tag size="small" :type="statusTagType[currentRow.status]">{{ statusText[currentRow.status] }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="操作人">{{ currentRow.operator }}</el-descriptions-item>
        <el-descriptions-item label="导入时间">{{ currentRow.importTime }}</el-descriptions-item>
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
 * 账单导入页面
 * 导入渠道对账文件到平台进行后续比对
 */
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Search, Refresh, Upload } from '@element-plus/icons-vue'
import { payApi } from '@/api/payment/pay'

const channelText = { wechat: '微信支付', alipay: '支付宝', unionpay: '银联' }
const channelTagType = { wechat: 'success', alipay: 'primary', unionpay: 'warning' }
const statusText = { pending: '待导入', processing: '导入中', success: '导入成功', failed: '导入失败' }
const statusTagType = { pending: 'info', processing: 'primary', success: 'success', failed: 'danger' }

const loading = ref(false)
const detailDialogVisible = ref(false)
const currentRow = ref(null)
const tableData = ref([])

const searchForm = reactive({ fileName: '', channel: '', status: '' })
const pagination = reactive({ page: 1, size: 10, total: 0 })

const fetchList = async () => {
  loading.value = true
  try {
    const params = { pageNum: pagination.page, pageSize: pagination.size, fileName: searchForm.fileName || undefined, channel: searchForm.channel || undefined, status: searchForm.status || undefined }
    const res = await payApi.importBill(params)
    if (res && res.code === 200) { tableData.value = res.data?.records || res.data?.list || []; pagination.total = res.data?.total || 0 }
  } catch (e) { console.error('查询导入记录失败:', e) } finally { loading.value = false }
}

const handleSearch = () => { pagination.page = 1; fetchList() }
const handleReset = () => { Object.assign(searchForm, { fileName: '', channel: '', status: '' }); handleSearch() }
const handleDetail = (row) => { currentRow.value = row; detailDialogVisible.value = true }

const handleBeforeUpload = async (file) => {
  ElMessage.info(`正在导入文件: ${file.name}`)
  try {
    const formData = new FormData()
    formData.append('file', file)
    const res = await payApi.importBill(formData)
    if (res && res.code === 200) { ElMessage.success('文件导入成功'); fetchList() }
    else { ElMessage.error(res?.msg || '导入失败') }
  } catch (e) { ElMessage.error('导入异常') }
  return false
}

const handleRetry = async (row) => {
  ElMessage.info(`正在重新导入: ${row.fileName || row.fileNo}`)
  try {
    const res = await payApi.importBill({ action: 'retry', fileId: row.id, fileName: row.fileName })
    if (res && res.code === 200) { ElMessage.success('重新导入成功'); fetchList() }
    else { ElMessage.error(res?.msg || res?.message || '重新导入失败') }
  } catch (e) { ElMessage.error('重新导入异常: ' + (e.message || '网络错误')) }
}

onMounted(() => { fetchList() })
</script>

<style scoped>
.bill-import-container { padding: 16px; }
.search-card { margin-bottom: 12px; border-radius: 8px; }
.search-card :deep(.el-card__body) { padding: 14px 16px 0 16px; }
.search-form :deep(.el-form-item) { margin-bottom: 14px; }
.table-card { border-radius: 8px; }
.table-card :deep(.el-card__header) { padding: 12px 16px; }
.card-header { display: flex; align-items: center; justify-content: space-between; }
.card-title { font-size: 15px; font-weight: 600; color: #303133; }
.pagination-wrap { display: flex; justify-content: flex-end; margin-top: 12px; }
</style>
