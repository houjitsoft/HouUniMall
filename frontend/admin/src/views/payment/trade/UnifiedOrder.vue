<template>
  <div class="unified-order-container">
    <!-- 搜索筛选 -->
    <el-card class="search-card" shadow="never">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="商户订单号">
          <el-input v-model="searchForm.outTradeNo" placeholder="请输入商户订单号" clearable style="width: 180px;" />
        </el-form-item>
        <el-form-item label="平台交易号">
          <el-input v-model="searchForm.tradeNo" placeholder="请输入平台交易号" clearable style="width: 180px;" />
        </el-form-item>
        <el-form-item label="支付渠道">
          <el-select v-model="searchForm.channel" placeholder="请选择" clearable style="width: 140px;">
            <el-option label="微信支付" value="wechat" />
            <el-option label="支付宝" value="alipay" />
            <el-option label="银联" value="unionpay" />
            <el-option label="余额支付" value="balance" />
          </el-select>
        </el-form-item>
        <el-form-item label="订单状态">
          <el-select v-model="searchForm.status" placeholder="请选择" clearable style="width: 140px;">
            <el-option label="待支付" value="pending" />
            <el-option label="支付成功" value="success" />
            <el-option label="已关闭" value="closed" />
            <el-option label="支付失败" value="failed" />
          </el-select>
        </el-form-item>
        <el-form-item label="下单时间">
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
          <span class="card-title">下单记录</span>
          <div class="header-actions">
            <el-button type="primary" @click="handleCreate"><el-icon><Plus /></el-icon>创建下单</el-button>
            <el-button @click="handleExport"><el-icon><Download /></el-icon>导出</el-button>
          </div>
        </div>
      </template>

      <el-table :data="tableData" v-loading="loading" stripe style="width: 100%;">
        <el-table-column prop="outTradeNo" label="商户订单号" min-width="180" />
        <el-table-column prop="tradeNo" label="平台交易号" min-width="180" />
        <el-table-column prop="channel" label="支付渠道" width="120">
          <template #default="{ row }">
            <el-tag :type="channelTagType[row.channel]" size="small">{{ channelText[row.channel] || row.channel }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="amount" label="下单金额" width="120" align="right">
          <template #default="{ row }">¥{{ row.amount }}</template>
        </el-table-column>
        <el-table-column prop="status" label="订单状态" width="100">
          <template #default="{ row }">
            <el-tag :type="statusTagType[row.status]" size="small">{{ statusText[row.status] || row.status }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="下单时间" width="180" />
        <el-table-column label="操作" width="160" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click="handleDetail(row)">详情</el-button>
            <el-button link type="warning" size="small" v-if="row.status === 'pending'" @click="handleClose(row)">关闭</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-wrap">
        <el-pagination v-model:current-page="pagination.page" v-model:page-size="pagination.size" :page-sizes="[10, 20, 50, 100]" :total="pagination.total" layout="total, sizes, prev, pager, next, jumper" background @size-change="fetchList" @current-change="fetchList" />
      </div>
    </el-card>

    <!-- 创建下单对话框 -->
    <el-dialog v-model="createDialogVisible" title="创建下单" width="600px" :close-on-click-modal="false">
      <el-form :model="createForm" :rules="createRules" ref="createFormRef" label-width="100px">
        <el-form-item label="商户订单号" prop="outTradeNo">
          <el-input v-model="createForm.outTradeNo" placeholder="请输入商户订单号" />
        </el-form-item>
        <el-form-item label="支付渠道" prop="channel">
          <el-select v-model="createForm.channel" placeholder="请选择支付渠道" style="width: 100%;">
            <el-option label="微信支付" value="wechat" />
            <el-option label="支付宝" value="alipay" />
            <el-option label="银联" value="unionpay" />
            <el-option label="余额支付" value="balance" />
          </el-select>
        </el-form-item>
        <el-form-item label="下单金额" prop="amount">
          <el-input-number v-model="createForm.amount" :min="0.01" :precision="2" :step="1" style="width: 100%;" />
        </el-form-item>
        <el-form-item label="商品描述" prop="body">
          <el-input v-model="createForm.body" placeholder="请输入商品描述" />
        </el-form-item>
        <el-form-item label="回调地址" prop="notifyUrl">
          <el-input v-model="createForm.notifyUrl" placeholder="请输入异步通知地址" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="createForm.remark" type="textarea" :rows="2" placeholder="备注信息" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="createDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="submitCreate">确认下单</el-button>
      </template>
    </el-dialog>

    <!-- 详情对话框 -->
    <el-dialog v-model="detailDialogVisible" title="订单详情" width="700px">
      <el-descriptions v-if="currentRow" :column="2" border>
        <el-descriptions-item label="商户订单号">{{ currentRow.outTradeNo }}</el-descriptions-item>
        <el-descriptions-item label="平台交易号">{{ currentRow.tradeNo }}</el-descriptions-item>
        <el-descriptions-item label="支付渠道">{{ channelText[currentRow.channel] }}</el-descriptions-item>
        <el-descriptions-item label="下单金额">¥{{ currentRow.amount }}</el-descriptions-item>
        <el-descriptions-item label="订单状态">
          <el-tag :type="statusTagType[currentRow.status]" size="small">{{ statusText[currentRow.status] }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="下单时间">{{ currentRow.createTime }}</el-descriptions-item>
        <el-descriptions-item label="支付时间">{{ currentRow.payTime || '-' }}</el-descriptions-item>
        <el-descriptions-item label="商品描述">{{ currentRow.body || '-' }}</el-descriptions-item>
      </el-descriptions>
      <template #footer>
        <el-button @click="detailDialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
/**
 * 统一下单页面
 * 对接多渠道统一下单能力，创建支付订单并管理
 */
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Refresh, Plus, Download } from '@element-plus/icons-vue'
import { payApi } from '@/api/payment/pay'

// 渠道/状态映射
const channelText = { wechat: '微信支付', alipay: '支付宝', unionpay: '银联', balance: '余额支付' }
const channelTagType = { wechat: 'success', alipay: 'primary', unionpay: 'warning', balance: 'info' }
const statusText = { pending: '待支付', success: '支付成功', closed: '已关闭', failed: '支付失败' }
const statusTagType = { pending: 'warning', success: 'success', closed: 'info', failed: 'danger' }

// 响应式数据
const loading = ref(false)
const submitLoading = ref(false)
const createDialogVisible = ref(false)
const detailDialogVisible = ref(false)
const currentRow = ref(null)
const tableData = ref([])
const createFormRef = ref(null)

// 搜索表单
const searchForm = reactive({
  outTradeNo: '',
  tradeNo: '',
  channel: '',
  status: '',
  dateRange: null
})

// 分页
const pagination = reactive({ page: 1, size: 10, total: 0 })

// 创建表单
const createForm = reactive({
  outTradeNo: '',
  channel: '',
  amount: null,
  body: '',
  notifyUrl: '',
  remark: ''
})

const createRules = {
  outTradeNo: [{ required: true, message: '请输入商户订单号', trigger: 'blur' }],
  channel: [{ required: true, message: '请选择支付渠道', trigger: 'change' }],
  amount: [{ required: true, message: '请输入下单金额', trigger: 'blur' }],
  body: [{ required: true, message: '请输入商品描述', trigger: 'blur' }]
}

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
      channel: searchForm.channel || undefined,
      status: searchForm.status || undefined,
      startTime: searchForm.dateRange?.[0] || undefined,
      endTime: searchForm.dateRange?.[1] || undefined
    }
    const res = await payApi.queryTradeOrder(params)
    if (res && res.code === 200) {
      tableData.value = res.data?.records || res.data?.list || []
      pagination.total = res.data?.total || 0
    }
  } catch (e) {
    console.error('查询下单列表失败:', e)
  } finally {
    loading.value = false
  }
}

/** 搜索 */
const handleSearch = () => {
  pagination.page = 1
  fetchList()
}

/** 重置 */
const handleReset = () => {
  Object.assign(searchForm, { outTradeNo: '', tradeNo: '', channel: '', status: '', dateRange: null })
  handleSearch()
}

/** 打开创建弹窗 */
const handleCreate = () => {
  Object.assign(createForm, { outTradeNo: '', channel: '', amount: null, body: '', notifyUrl: '', remark: '' })
  createDialogVisible.value = true
}

/** 提交创建 */
const submitCreate = async () => {
  const valid = await createFormRef.value?.validate().catch(() => false)
  if (!valid) return
  submitLoading.value = true
  try {
    const res = await payApi.unifiedOrder(createForm)
    if (res && res.code === 200) {
      ElMessage.success('下单成功')
      createDialogVisible.value = false
      fetchList()
    } else {
      ElMessage.error(res?.msg || '下单失败')
    }
  } catch (e) {
    ElMessage.error('下单请求异常')
  } finally {
    submitLoading.value = false
  }
}

/** 查看详情 */
const handleDetail = (row) => {
  currentRow.value = row
  detailDialogVisible.value = true
}

/** 关闭订单 */
const handleClose = (row) => {
  ElMessageBox.confirm(`确认关闭订单「${row.outTradeNo}」？关闭后不可恢复。`, '关闭订单', { type: 'warning' }).then(async () => {
    try {
      const res = await payApi.closeTradeOrder({ outTradeNo: row.outTradeNo })
      if (res && res.code === 200) {
        ElMessage.success('订单已关闭')
        fetchList()
      } else {
        ElMessage.error(res?.msg || '关闭失败')
      }
    } catch (e) {
      ElMessage.error('关闭请求异常')
    }
  }).catch(() => {})
}

/** 导出 */
const handleExport = () => {
  ElMessage.info('导出功能开发中')
}

onMounted(() => {
  fetchList()
})
</script>

<style scoped>
.unified-order-container { padding: 16px; }
.search-card { margin-bottom: 12px; border-radius: 8px; }
.search-card :deep(.el-card__body) { padding: 14px 16px 0 16px; }
.search-form :deep(.el-form-item) { margin-bottom: 14px; }
.table-card { border-radius: 8px; }
.table-card :deep(.el-card__header) { padding: 12px 16px; }
.card-header { display: flex; align-items: center; justify-content: space-between; }
.card-title { font-size: 15px; font-weight: 600; color: #303133; }
.pagination-wrap { display: flex; justify-content: flex-end; margin-top: 12px; }
</style>
