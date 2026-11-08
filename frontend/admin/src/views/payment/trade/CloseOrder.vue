<template>
  <div class="close-order-container">
    <!-- 搜索筛选 -->
    <el-card class="search-card" shadow="never">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="商户订单号">
          <el-input v-model="searchForm.outTradeNo" placeholder="请输入商户订单号" clearable style="width: 180px;" />
        </el-form-item>
        <el-form-item label="平台交易号">
          <el-input v-model="searchForm.tradeNo" placeholder="请输入平台交易号" clearable style="width: 180px;" />
        </el-form-item>
        <el-form-item label="关闭状态">
          <el-select v-model="searchForm.status" placeholder="请选择" clearable style="width: 140px;">
            <el-option label="待支付（可关闭）" value="pending" />
            <el-option label="已关闭" value="closed" />
          </el-select>
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
          <span class="card-title">可关闭订单列表</span>
        </div>
      </template>

      <el-table :data="tableData" v-loading="loading" stripe style="width: 100%;">
        <el-table-column prop="outTradeNo" label="商户订单号" min-width="180" />
        <el-table-column prop="tradeNo" label="平台交易号" min-width="180" />
        <el-table-column prop="amount" label="订单金额" width="120" align="right">
          <template #default="{ row }">¥{{ row.amount }}</template>
        </el-table-column>
        <el-table-column prop="channel" label="支付渠道" width="120">
          <template #default="{ row }">
            <el-tag size="small" :type="channelTagType[row.channel]">{{ channelText[row.channel] || row.channel }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="当前状态" width="100">
          <template #default="{ row }">
            <el-tag size="small" :type="statusTagType[row.status]">{{ statusText[row.status] || row.status }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="closeReason" label="关闭原因" min-width="160">
          <template #default="{ row }">{{ row.closeReason || '-' }}</template>
        </el-table-column>
        <el-table-column prop="closeTime" label="关闭时间" width="180">
          <template #default="{ row }">{{ row.closeTime || '-' }}</template>
        </el-table-column>
        <el-table-column label="操作" width="140" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click="handleDetail(row)">详情</el-button>
            <el-button link type="danger" size="small" v-if="row.status === 'pending'" @click="handleClose(row)">关闭</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-wrap">
        <el-pagination v-model:current-page="pagination.page" v-model:page-size="pagination.size" :page-sizes="[10, 20, 50, 100]" :total="pagination.total" layout="total, sizes, prev, pager, next, jumper" background @size-change="fetchList" @current-change="fetchList" />
      </div>
    </el-card>

    <!-- 关闭订单对话框 -->
    <el-dialog v-model="closeDialogVisible" title="关闭订单" width="500px" :close-on-click-modal="false">
      <el-form :model="closeForm" label-width="100px">
        <el-form-item label="订单号">
          <el-input :model-value="closeForm.outTradeNo" disabled />
        </el-form-item>
        <el-form-item label="关闭原因" required>
          <el-select v-model="closeForm.reason" placeholder="请选择关闭原因" style="width: 100%;">
            <el-option label="用户主动取消" value="用户主动取消" />
            <el-option label="超时未支付" value="超时未支付" />
            <el-option label="商户主动关闭" value="商户主动关闭" />
            <el-option label="重复下单" value="重复下单" />
            <el-option label="其他原因" value="other" />
          </el-select>
        </el-form-item>
        <el-form-item label="补充说明" v-if="closeForm.reason === 'other'">
          <el-input v-model="closeForm.remark" type="textarea" :rows="2" placeholder="请说明关闭原因" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="closeDialogVisible = false">取消</el-button>
        <el-button type="danger" :loading="submitLoading" @click="confirmClose">确认关闭</el-button>
      </template>
    </el-dialog>

    <!-- 详情对话框 -->
    <el-dialog v-model="detailDialogVisible" title="订单详情" width="650px">
      <el-descriptions v-if="currentRow" :column="2" border>
        <el-descriptions-item label="商户订单号">{{ currentRow.outTradeNo }}</el-descriptions-item>
        <el-descriptions-item label="平台交易号">{{ currentRow.tradeNo }}</el-descriptions-item>
        <el-descriptions-item label="支付渠道">{{ channelText[currentRow.channel] }}</el-descriptions-item>
        <el-descriptions-item label="订单金额">¥{{ currentRow.amount }}</el-descriptions-item>
        <el-descriptions-item label="当前状态">
          <el-tag size="small" :type="statusTagType[currentRow.status]">{{ statusText[currentRow.status] }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="创建时间">{{ currentRow.createTime }}</el-descriptions-item>
        <el-descriptions-item label="关闭原因">{{ currentRow.closeReason || '-' }}</el-descriptions-item>
        <el-descriptions-item label="关闭时间">{{ currentRow.closeTime || '-' }}</el-descriptions-item>
      </el-descriptions>
      <template #footer>
        <el-button @click="detailDialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
/**
 * 关闭订单页面
 * 主动关闭未支付的订单，释放支付渠道资源
 */
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Search, Refresh } from '@element-plus/icons-vue'
import { payApi } from '@/api/payment/pay'

const channelText = { wechat: '微信支付', alipay: '支付宝', unionpay: '银联', balance: '余额支付' }
const channelTagType = { wechat: 'success', alipay: 'primary', unionpay: 'warning', balance: 'info' }
const statusText = { pending: '待支付', success: '支付成功', closed: '已关闭', failed: '支付失败' }
const statusTagType = { pending: 'warning', success: 'success', closed: 'info', failed: 'danger' }

const loading = ref(false)
const submitLoading = ref(false)
const closeDialogVisible = ref(false)
const detailDialogVisible = ref(false)
const currentRow = ref(null)
const tableData = ref([])

const searchForm = reactive({ outTradeNo: '', tradeNo: '', status: '' })
const pagination = reactive({ page: 1, size: 10, total: 0 })
const closeForm = reactive({ outTradeNo: '', reason: '', remark: '' })

/**
 * 获取列表数据
 */
const fetchList = async () => {
  loading.value = true
  try {
    const params = {
      pageNum: pagination.page,
      pageSize: pagination.size,
      outTradeNo: searchForm.outTradeNo || undefined,
      tradeNo: searchForm.tradeNo || undefined,
      status: searchForm.status || undefined
    }
    const res = await payApi.queryTradeOrder(params)
    if (res && res.code === 200) {
      tableData.value = res.data?.records || res.data?.list || []
      pagination.total = res.data?.total || 0
    }
  } catch (e) {
    console.error('查询订单列表失败:', e)
  } finally {
    loading.value = false
  }
}

const handleSearch = () => { pagination.page = 1; fetchList() }
const handleReset = () => { Object.assign(searchForm, { outTradeNo: '', tradeNo: '', status: '' }); handleSearch() }

const handleDetail = (row) => { currentRow.value = row; detailDialogVisible.value = true }

/** 打开关闭对话框 */
const handleClose = (row) => {
  currentRow.value = row
  Object.assign(closeForm, { outTradeNo: row.outTradeNo, reason: '', remark: '' })
  closeDialogVisible.value = true
}

/** 确认关闭 */
const confirmClose = async () => {
  if (!closeForm.reason) { ElMessage.warning('请选择关闭原因'); return }
  submitLoading.value = true
  try {
    const res = await payApi.closeTradeOrder({
      outTradeNo: closeForm.outTradeNo,
      closeReason: closeForm.reason === 'other' ? closeForm.remark : closeForm.reason
    })
    if (res && res.code === 200) {
      ElMessage.success('订单已关闭')
      closeDialogVisible.value = false
      fetchList()
    } else {
      ElMessage.error(res?.msg || '关闭失败')
    }
  } catch (e) {
    ElMessage.error('关闭请求异常')
  } finally {
    submitLoading.value = false
  }
}

onMounted(() => { fetchList() })
</script>

<style scoped>
.close-order-container { padding: 16px; }
.search-card { margin-bottom: 12px; border-radius: 8px; }
.search-card :deep(.el-card__body) { padding: 14px 16px 0 16px; }
.search-form :deep(.el-form-item) { margin-bottom: 14px; }
.table-card { border-radius: 8px; }
.table-card :deep(.el-card__header) { padding: 12px 16px; }
.card-header { display: flex; align-items: center; justify-content: space-between; }
.card-title { font-size: 15px; font-weight: 600; color: #303133; }
.pagination-wrap { display: flex; justify-content: flex-end; margin-top: 12px; }
</style>
