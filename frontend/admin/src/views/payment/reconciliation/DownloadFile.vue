<template>
  <div class="download-file-container">
    <el-card class="search-card" shadow="never">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="支付渠道">
          <el-select v-model="searchForm.channel" placeholder="请选择" clearable style="width: 140px;">
            <el-option label="微信支付" value="wechat" />
            <el-option label="支付宝" value="alipay" />
            <el-option label="银联" value="unionpay" />
          </el-select>
        </el-form-item>
        <el-form-item label="账单类型">
          <el-select v-model="searchForm.billType" placeholder="请选择" clearable style="width: 120px;">
            <el-option label="日账单" value="daily" />
            <el-option label="月账单" value="monthly" />
          </el-select>
        </el-form-item>
        <el-form-item label="账单日期">
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
          <span class="card-title">对账文件列表</span>
          <el-button type="primary" @click="handleRequestDownload"><el-icon><Download /></el-icon>请求下载</el-button>
        </div>
      </template>

      <el-table :data="tableData" v-loading="loading" stripe style="width: 100%;">
        <el-table-column prop="channel" label="支付渠道" width="120">
          <template #default="{ row }">
            <el-tag size="small" :type="channelTagType[row.channel]">{{ channelText[row.channel] || row.channel }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="billType" label="账单类型" width="100">
          <template #default="{ row }">{{ row.billType === 'daily' ? '日账单' : '月账单' }}</template>
        </el-table-column>
        <el-table-column prop="billDate" label="账单日期" width="160" />
        <el-table-column prop="fileSize" label="文件大小" width="120" align="right" />
        <el-table-column prop="status" label="下载状态" width="100">
          <template #default="{ row }">
            <el-tag size="small" :type="statusTagType[row.status]">{{ statusText[row.status] || row.status }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="updateTime" label="更新时间" width="180" />
        <el-table-column label="操作" width="140" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" size="small" v-if="row.status === 'ready'" @click="handleDownload(row)">下载</el-button>
            <el-button link type="info" size="small" v-if="row.status === 'processing'">下载中...</el-button>
            <el-button link type="primary" size="small" @click="handleDetail(row)">详情</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-wrap">
        <el-pagination v-model:current-page="pagination.page" v-model:page-size="pagination.size" :page-sizes="[10, 20, 50, 100]" :total="pagination.total" layout="total, sizes, prev, pager, next, jumper" background @size-change="fetchList" @current-change="fetchList" />
      </div>
    </el-card>

    <!-- 请求下载 -->
    <el-dialog v-model="requestDialogVisible" title="请求对账文件" width="500px" :close-on-click-modal="false">
      <el-form :model="requestForm" label-width="100px">
        <el-form-item label="支付渠道" required>
          <el-select v-model="requestForm.channel" placeholder="请选择" style="width: 100%;">
            <el-option label="微信支付" value="wechat" />
            <el-option label="支付宝" value="alipay" />
            <el-option label="银联" value="unionpay" />
          </el-select>
        </el-form-item>
        <el-form-item label="账单类型" required>
          <el-radio-group v-model="requestForm.billType">
            <el-radio value="daily">日账单</el-radio>
            <el-radio value="monthly">月账单</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="账单日期" required>
          <el-date-picker v-model="requestForm.billDate" :type="requestForm.billType === 'monthly' ? 'month' : 'date'" placeholder="请选择日期" style="width: 100%;" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="requestDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="submitRequest">发起请求</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="detailDialogVisible" title="对账文件详情" width="600px">
      <el-descriptions v-if="currentRow" :column="2" border>
        <el-descriptions-item label="支付渠道">{{ channelText[currentRow.channel] }}</el-descriptions-item>
        <el-descriptions-item label="账单类型">{{ currentRow.billType === 'daily' ? '日账单' : '月账单' }}</el-descriptions-item>
        <el-descriptions-item label="账单日期">{{ currentRow.billDate }}</el-descriptions-item>
        <el-descriptions-item label="文件大小">{{ currentRow.fileSize || '-' }}</el-descriptions-item>
        <el-descriptions-item label="下载状态">
          <el-tag size="small" :type="statusTagType[currentRow.status]">{{ statusText[currentRow.status] }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="更新时间">{{ currentRow.updateTime }}</el-descriptions-item>
      </el-descriptions>
      <template #footer>
        <el-button @click="detailDialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
/**
 * 对账文件下载页面
 * 下载各支付渠道的日/月对账文件
 */
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Search, Refresh, Download } from '@element-plus/icons-vue'
import { payApi } from '@/api/payment/pay'

const channelText = { wechat: '微信支付', alipay: '支付宝', unionpay: '银联' }
const channelTagType = { wechat: 'success', alipay: 'primary', unionpay: 'warning' }
const statusText = { pending: '待下载', processing: '下载中', ready: '已就绪', failed: '下载失败' }
const statusTagType = { pending: 'info', processing: 'primary', ready: 'success', failed: 'danger' }

const loading = ref(false)
const submitLoading = ref(false)
const requestDialogVisible = ref(false)
const detailDialogVisible = ref(false)
const currentRow = ref(null)
const tableData = ref([])

const searchForm = reactive({ channel: '', billType: '', dateRange: null })
const pagination = reactive({ page: 1, size: 10, total: 0 })
const requestForm = reactive({ channel: '', billType: 'daily', billDate: '' })

const fetchList = async () => {
  loading.value = true
  try {
    const params = { pageNum: pagination.page, pageSize: pagination.size, channel: searchForm.channel || undefined, billType: searchForm.billType || undefined, startTime: searchForm.dateRange?.[0] || undefined, endTime: searchForm.dateRange?.[1] || undefined }
    const res = await payApi.downloadStatement(params)
    if (res && res.code === 200) { tableData.value = res.data?.records || res.data?.list || []; pagination.total = res.data?.total || 0 }
  } catch (e) { console.error('查询对账文件失败:', e) } finally { loading.value = false }
}

const handleSearch = () => { pagination.page = 1; fetchList() }
const handleReset = () => { Object.assign(searchForm, { channel: '', billType: '', dateRange: null }); handleSearch() }
const handleDetail = (row) => { currentRow.value = row; detailDialogVisible.value = true }

const handleRequestDownload = () => {
  Object.assign(requestForm, { channel: '', billType: 'daily', billDate: '' })
  requestDialogVisible.value = true
}

const submitRequest = async () => {
  if (!requestForm.channel || !requestForm.billDate) { ElMessage.warning('请完善下载信息'); return }
  submitLoading.value = true
  try {
    const res = await payApi.downloadStatement({ ...requestForm, action: 'request' })
    if (res && res.code === 200) { ElMessage.success('下载请求已发起'); requestDialogVisible.value = false; fetchList() }
    else { ElMessage.error(res?.msg || '请求失败') }
  } catch (e) { ElMessage.error('请求异常') } finally { submitLoading.value = false }
}

const handleDownload = async (row) => {
  try {
    const res = await payApi.downloadStatement({ action: 'download', billDate: row.billDate, channel: row.channel, billType: row.billType || 'daily' })
    if (res && res.code === 200) {
      const data = res.data || {}
      const fileUrl = data.fileUrl || data.url || data.downloadUrl
      if (fileUrl) {
        ElMessage.success('开始下载对账文件')
        window.open(fileUrl, '_blank')
      } else {
        ElMessage.success('已请求下载，请留意浏览器下载')
      }
    } else {
      ElMessage.error(res?.msg || res?.message || '下载失败')
    }
  } catch (e) {
    ElMessage.error('下载失败: ' + (e.message || '网络错误'))
  }
}

onMounted(() => { fetchList() })
</script>

<style scoped>
.download-file-container { padding: 16px; }
.search-card { margin-bottom: 12px; border-radius: 8px; }
.search-card :deep(.el-card__body) { padding: 14px 16px 0 16px; }
.search-form :deep(.el-form-item) { margin-bottom: 14px; }
.table-card { border-radius: 8px; }
.table-card :deep(.el-card__header) { padding: 12px 16px; }
.card-header { display: flex; align-items: center; justify-content: space-between; }
.card-title { font-size: 15px; font-weight: 600; color: #303133; }
.pagination-wrap { display: flex; justify-content: flex-end; margin-top: 12px; }
</style>
