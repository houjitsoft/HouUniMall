<template>
  <div class="refund-management">
    <el-card class="box-card">
      <template #header>
        <div class="card-header">
          <span>退款管理</span>
          <el-button type="primary" @click="handleCreateRefund">创建退款</el-button>
        </div>
      </template>
      
      <!-- 搜索栏 -->
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="退款单号">
          <el-input v-model="searchForm.refundNo" placeholder="请输入退款单号" clearable />
        </el-form-item>
        <el-form-item label="原支付单号">
          <el-input v-model="searchForm.originalTradeNo" placeholder="请输入原支付单号" clearable />
        </el-form-item>
        <el-form-item label="退款状态">
          <el-select v-model="searchForm.status" placeholder="请选择状态" clearable>
            <el-option 
              v-for="(text, value) in REFUND_STATUS_TEXT" 
              :key="value" 
              :label="text" 
              :value="parseInt(value)" 
            />
          </el-select>
        </el-form-item>
        <el-form-item label="申请时间">
          <el-date-picker
            v-model="searchForm.dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            format="YYYY-MM-DD"
            value-format="YYYY-MM-DD"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>

      <!-- 退款记录列表 -->
      <el-table :data="refundList" style="width: 100%" v-loading="loading">
        <el-table-column prop="refundNo" label="退款单号" width="180" />
        <el-table-column prop="originalTradeNo" label="原支付单号" width="180" />
        <el-table-column prop="amount" label="退款金额" width="120">
          <template #default="scope">
            ¥{{ formatAmount(scope.row.amount) }}
          </template>
        </el-table-column>
        <el-table-column prop="channel" label="退款渠道" width="120">
          <template #default="scope">
            {{ getChannelText(scope.row.channel) }}
          </template>
        </el-table-column>
        <el-table-column prop="status" label="退款状态" width="100">
          <template #default="scope">
            <el-tag :type="getStatusTagType(scope.row.status)">
              {{ getStatusText(scope.row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="申请时间" width="180">
          <template #default="scope">
            {{ formatDate(scope.row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column prop="processTime" label="处理时间" width="180">
          <template #default="scope">
            {{ formatDate(scope.row.processTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="scope">
            <el-button size="small" @click="handleView(scope.row)">查看</el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination-container">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :page-sizes="[10, 20, 50, 100]"
          :total="total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>

    <!-- 创建退款对话框 -->
    <el-dialog
      v-model="createDialogVisible"
      title="创建退款"
      width="500px"
      :close-on-click-modal="false"
    >
      <el-form :model="refundForm" :rules="refundRules" ref="refundFormRef" label-width="120px">
        <el-form-item label="原支付单号" prop="originalTradeNo">
          <el-input v-model="refundForm.originalTradeNo" placeholder="请输入原支付单号" />
        </el-form-item>
        <el-form-item label="退款金额" prop="amount">
          <el-input-number 
            v-model="refundForm.amount" 
            :min="0.01" 
            :precision="2" 
            :step="1" 
            controls-position="right" 
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="退款原因" prop="reason">
          <el-input v-model="refundForm.reason" type="textarea" placeholder="请输入退款原因" />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="createDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleCreate" :loading="createLoading">确定</el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 退款详情对话框 -->
    <el-dialog
      v-model="detailDialogVisible"
      title="退款详情"
      width="800px"
      :close-on-click-modal="false"
    >
      <div v-if="currentRefund" class="refund-detail">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="退款单号">{{ currentRefund.refundNo }}</el-descriptions-item>
          <el-descriptions-item label="原支付单号">{{ currentRefund.originalTradeNo }}</el-descriptions-item>
          <el-descriptions-item label="退款金额">¥{{ formatAmount(currentRefund.amount) }}</el-descriptions-item>
          <el-descriptions-item label="退款渠道">{{ getChannelText(currentRefund.channel) }}</el-descriptions-item>
          <el-descriptions-item label="退款状态">
            <el-tag :type="getStatusTagType(currentRefund.status)">
              {{ getStatusText(currentRefund.status) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="申请时间">{{ formatDate(currentRefund.createTime) }}</el-descriptions-item>
          <el-descriptions-item label="处理时间">{{ formatDate(currentRefund.processTime) }}</el-descriptions-item>
          <el-descriptions-item label="完成时间">{{ formatDate(currentRefund.finishTime) }}</el-descriptions-item>
          <el-descriptions-item label="退款原因" :span="2">{{ currentRefund.reason }}</el-descriptions-item>
        </el-descriptions>
      </div>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="detailDialogVisible = false">关闭</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>

// 使用 Element Plus 国际化系统
import { useI18n } from '@/i18n-element'
const { t } = useI18n()
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { payApi, PAY_CHANNEL_TEXT } from '../../api/payment/pay'

// 响应式数据
const loading = ref(false)
const createLoading = ref(false)
const createDialogVisible = ref(false)
const detailDialogVisible = ref(false)
const currentRefund = ref(null)
const refundFormRef = ref(null)
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

// 搜索表单
const searchForm = reactive({
  refundNo: '',
  originalTradeNo: '',
  status: '',
  dateRange: [],
  pageNum: 1,
  pageSize: 10
})

// 退款表单
const refundForm = reactive({
  originalTradeNo: '',
  amount: 0,
  reason: ''
})

// 退款表单规则
const refundRules = {
  originalTradeNo: [
    { required: true, message: '请输入原支付单号', trigger: 'blur' }
  ],
  amount: [
    { required: true, message: '请输入退款金额', trigger: 'blur' }
  ],
  reason: [
    { required: true, message: '请输入退款原因', trigger: 'blur' }
  ]
}

// 退款记录列表
const refundList = ref([])

// 退款状态枚举
const REFUND_STATUS = {
  PENDING: 1,      // 待处理
  PROCESSING: 2,   // 处理中
  SUCCESS: 3,      // 退款成功
  FAILED: 4,       // 退款失败
  REJECTED: 5      // 已拒绝
}

// 退款状态文本映射
const REFUND_STATUS_TEXT = {
  [REFUND_STATUS.PENDING]: '待处理',
  [REFUND_STATUS.PROCESSING]: '处理中',
  [REFUND_STATUS.SUCCESS]: '退款成功',
  [REFUND_STATUS.FAILED]: '退款失败',
  [REFUND_STATUS.REJECTED]: '已拒绝'
}

// 退款状态标签类型映射
const REFUND_STATUS_TAG_TYPE = {
  [REFUND_STATUS.PENDING]: 'warning',
  [REFUND_STATUS.PROCESSING]: 'info',
  [REFUND_STATUS.SUCCESS]: 'success',
  [REFUND_STATUS.FAILED]: 'danger',
  [REFUND_STATUS.REJECTED]: 'danger'
}

// 生命周期
onMounted(() => {
  fetchRefundList()
})

// 获取退款记录列表
const fetchRefundList = async () => {
  loading.value = true
  try {
    const params = {
      ...searchForm,
      mallOrderNo: searchForm.originalTradeNo || undefined,
      pageNum: currentPage.value,
      pageSize: pageSize.value
    }
    delete params.originalTradeNo
    
    // 处理日期范围
    if (searchForm.dateRange && searchForm.dateRange.length === 2) {
      params.startDate = searchForm.dateRange[0]
      params.endDate = searchForm.dateRange[1]
    }
    delete params.dateRange
    
    const response = await payApi.getRefundList(params)
    
    if (response.code === 200 || response.success) {
      const data = response.data || response
      const records = data.records || data.list || []
      // 映射 hou_pay_refund_order 实体字段 → 展示列
      refundList.value = records.map((r) => ({
        id: r.id,
        refundNo: r.refundNo,
        originalTradeNo: r.mallOrderNo || r.cashierNo || '',
        outTradeNo: r.mallOrderNo || r.cashierNo || '',
        amount: r.amount ?? 0,
        refundAmount: r.amount ?? 0,
        channel: r.payChannel || r.channelCode || '',
        status: mapRefundStatus(r.status),
        createTime: r.createTime || '',
        applyTime: r.createTime || '',
        processTime: r.updateTime || r.successTime || '',
        finishTime: r.successTime || r.updateTime || '',
        failReason: r.failureReason || '',
        _raw: r
      }))
      total.value = data.total || 0
    } else {
      ElMessage.error(response.message || '获取退款记录失败')
      refundList.value = []
      total.value = 0
    }
  } catch (error) {
    console.error('获取退款记录失败:', error)
    ElMessage.error('获取退款记录失败: ' + (error.message || '网络错误'))
    refundList.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}

// 退款状态映射：兼容后台字符串状态(PROCESSING/SUCCESS/FAILED)与数字枚举
const mapRefundStatus = (status) => {
  const s = String(status == null ? '' : status)
  const map = {
    'PROCESSING': 2,
    'SUCCESS': 3,
    'FAILED': 4,
    'REJECTED': 5,
    'PENDING': 1,
    'processing': 2,
    'success': 3,
    'failed': 4,
    'rejected': 5,
    'pending': 1
  }
  return map[s] ?? map[status] ?? status
}

// 获取状态标签类型
const getStatusTagType = (status) => {
  return REFUND_STATUS_TAG_TYPE[status] || 'info'
}

// 获取状态文本
const getStatusText = (status) => {
  return REFUND_STATUS_TEXT[status] || '未知状态'
}

// 获取渠道文本
const getChannelText = (channel) => {
  // 使用支付渠道的文本映射
  return PAY_CHANNEL_TEXT[channel] || '未知渠道'
}

// 格式化金额
const formatAmount = (amount) => {
  if (amount === null || amount === undefined) return '0.00'
  return Number(amount).toFixed(2)
}

// 格式化日期
const formatDate = (dateStr) => {
  if (!dateStr) return '-'
  const date = new Date(dateStr)
  if (isNaN(date.getTime())) return '-'
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit'
  })
}

// 搜索
const handleSearch = () => {
  currentPage.value = 1
  fetchRefundList()
}

// 重置搜索
const handleReset = () => {
  Object.assign(searchForm, {
    refundNo: '',
    originalTradeNo: '',
    status: '',
    dateRange: [],
    pageNum: 1,
    pageSize: 10
  })
  currentPage.value = 1
  fetchRefundList()
}

// 创建退款
const handleCreateRefund = () => {
  createDialogVisible.value = true
  // 重置表单
  Object.assign(refundForm, {
    originalTradeNo: '',
    amount: 0,
    reason: ''
  })
}

// 处理创建
const handleCreate = async () => {
  try {
    await refundFormRef.value.validate()
    
    createLoading.value = true
    const response = await payApi.createRefund(refundForm)
    
    if (response.code === 200 || response.success) {
      ElMessage.success('创建退款成功')
      createDialogVisible.value = false
      fetchRefundList()
    } else {
      ElMessage.error(response.message || '创建退款失败')
    }
  } catch (error) {
    console.error('创建退款失败:', error)
    ElMessage.error('创建退款失败: ' + (error.message || '网络错误'))
  } finally {
    createLoading.value = false
  }
}

// 查看退款详情
const handleView = async (row) => {
  try {
    const key = row.refundNo || row.id
    if (!key) {
      ElMessage.warning('缺少退款单号，无法查看详情')
      return
    }
    const response = await payApi.getRefundDetail(key)
    if (response.code === 200 || response.success) {
      const data = response.data || response
      // 兼容分页查询结果，取首条
      currentRefund.value = (Array.isArray(data?.records) ? data.records[0] : data) || data
      detailDialogVisible.value = true
    } else {
      ElMessage.error(response.message || '获取退款详情失败')
    }
  } catch (error) {
    console.error('获取退款详情失败:', error)
    ElMessage.error('获取退款详情失败: ' + (error.message || '网络错误'))
  }
}

// 分页大小改变
const handleSizeChange = (val) => {
  pageSize.value = val
  searchForm.pageSize = val
  fetchRefundList()
}

// 当前页改变
const handleCurrentChange = (val) => {
  currentPage.value = val
  searchForm.pageNum = val
  fetchRefundList()
}
</script>

<style scoped>
.refund-management {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.search-form {
  margin-bottom: 20px;
}

.pagination-container {
  margin-top: 20px;
  text-align: right;
}

.refund-detail {
  padding: 20px 0;
}

.dialog-footer {
  text-align: right;
}
</style>
