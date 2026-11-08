<template>
  <div class="refund-log-container">
    <!-- 搜索筛选 -->
    <el-card class="search-card" shadow="never">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="退款单号">
          <el-input v-model="searchForm.refundNo" placeholder="请输入退款单号" clearable style="width: 180px;" />
        </el-form-item>
        <el-form-item label="退款状态">
          <el-select v-model="searchForm.status" placeholder="请选择" clearable style="width: 140px;">
            <el-option label="退款中" value="processing" />
            <el-option label="退款成功" value="success" />
            <el-option label="退款失败" value="failed" />
          </el-select>
        </el-form-item>
        <el-form-item label="退款时间">
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
          <span class="card-title">退款记录</span>
          <el-button @click="handleExport"><el-icon><Download /></el-icon>导出</el-button>
        </div>
      </template>

      <el-table :data="tableData" v-loading="loading" stripe style="width: 100%;">
        <el-table-column prop="refundNo" label="退款单号" min-width="180" />
        <el-table-column prop="outTradeNo" label="原订单号" min-width="180" />
        <el-table-column prop="refundAmount" label="退款金额" width="120" align="right">
          <template #default="{ row }">¥{{ row.refundAmount }}</template>
        </el-table-column>
        <el-table-column prop="channel" label="退款渠道" width="120">
          <template #default="{ row }">
            <el-tag size="small" type="info">{{ channelText[row.channel] || row.channel || '-' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="退款状态" width="100">
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

    <!-- 详情对话框 -->
    <el-dialog v-model="detailDialogVisible" title="退款记录详情" width="650px">
      <el-descriptions v-if="currentRow" :column="2" border>
        <el-descriptions-item label="退款单号">{{ currentRow.refundNo }}</el-descriptions-item>
        <el-descriptions-item label="原订单号">{{ currentRow.outTradeNo }}</el-descriptions-item>
        <el-descriptions-item label="退款金额">¥{{ currentRow.refundAmount }}</el-descriptions-item>
        <el-descriptions-item label="退款渠道">{{ channelText[currentRow.channel] || '-' }}</el-descriptions-item>
        <el-descriptions-item label="退款状态">
          <el-tag size="small" :type="statusTagType[currentRow.status]">{{ statusText[currentRow.status] }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="申请时间">{{ currentRow.applyTime || '-' }}</el-descriptions-item>
        <el-descriptions-item label="完成时间">{{ currentRow.finishTime || '-' }}</el-descriptions-item>
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
 * 退款记录页面
 * 查看历史退款记录与渠道回执流水
 */
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Search, Refresh, Download } from '@element-plus/icons-vue'
import { payApi } from '@/api/payment/pay'

const channelText = { wechat: '微信支付', alipay: '支付宝', unionpay: '银联', balance: '余额' }
const statusText = { processing: '退款中', success: '退款成功', failed: '退款失败' }
const statusTagType = { processing: 'primary', success: 'success', failed: 'danger' }

const loading = ref(false)
const detailDialogVisible = ref(false)
const currentRow = ref(null)
const tableData = ref([])

const searchForm = reactive({ refundNo: '', status: '', dateRange: null })
const pagination = reactive({ page: 1, size: 10, total: 0 })

const mapStatus = (s) => {
  const up = String(s == null ? '' : s).toUpperCase()
  if (up === 'SUCCESS') return 'success'
  if (up === 'FAILED' || up === 'FAIL') return 'failed'
  return 'processing'
}

const fetchList = async () => {
  loading.value = true
  try {
    const params = {
      pageNum: pagination.page,
      pageSize: pagination.size,
      refundNo: searchForm.refundNo || undefined,
      status: searchForm.status || undefined,
      startTime: searchForm.dateRange?.[0] || undefined,
      endTime: searchForm.dateRange?.[1] || undefined
    }
    const res = await payApi.getRefundList(params)
    if (res && res.code === 200) {
      const records = res.data?.records || res.data?.list || []
      tableData.value = records.map((r) => ({
        id: r.id,
        refundNo: r.refundNo,
        outTradeNo: r.mallOrderNo || r.cashierNo || '',
        refundAmount: r.amount ?? 0,
        channel: r.channelCode || r.payChannel || '',
        status: mapStatus(r.status),
        finishTime: r.successTime || r.updateTime || '',
        applyTime: r.createTime || '',
        failReason: r.failureReason || '',
        _raw: r
      }))
      pagination.total = res.data?.total || 0
    }
  } catch (e) {
    console.error('查询退款记录失败:', e)
  } finally {
    loading.value = false
  }
}

const handleSearch = () => { pagination.page = 1; fetchList() }
const handleReset = () => { Object.assign(searchForm, { refundNo: '', status: '', dateRange: null }); handleSearch() }
const handleDetail = (row) => { currentRow.value = row; detailDialogVisible.value = true }
const handleExport = () => { ElMessage.info('导出功能开发中') }

onMounted(() => { fetchList() })
</script>

<style scoped>
.refund-log-container { padding: 16px; }
.search-card { margin-bottom: 12px; border-radius: 8px; }
.search-card :deep(.el-card__body) { padding: 14px 16px 0 16px; }
.search-form :deep(.el-form-item) { margin-bottom: 14px; }
.table-card { border-radius: 8px; }
.table-card :deep(.el-card__header) { padding: 12px 16px; }
.card-header { display: flex; align-items: center; justify-content: space-between; }
.card-title { font-size: 15px; font-weight: 600; color: #303133; }
.pagination-wrap { display: flex; justify-content: flex-end; margin-top: 12px; }
</style>
