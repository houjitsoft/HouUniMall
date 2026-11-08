<template>
  <div class="pay-management">
    <el-card class="box-card">
      <template #header>
        <div class="card-header">
          <span>支付订单管理</span>
          <el-button type="primary" @click="handleExport" :loading="exportLoading">导出订单</el-button>
        </div>
      </template>
      
      <!-- 搜索栏 -->
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="订单号">
          <el-input v-model="searchForm.orderId" placeholder="请输入订单号" clearable />
        </el-form-item>
        <el-form-item label="支付单号">
          <el-input v-model="searchForm.consumeTradeId" placeholder="请输入支付单号" clearable />
        </el-form-item>
        <el-form-item label="支付状态">
          <el-select v-model="searchForm.orderStateId" placeholder="请选择状态" clearable>
            <el-option 
              v-for="(text, value) in PAY_STATUS_TEXT" 
              :key="value" 
              :label="text" 
              :value="parseInt(value)" 
            />
          </el-select>
        </el-form-item>
        <el-form-item label="支付渠道">
          <el-select v-model="searchForm.paymentChannelId" placeholder="请选择渠道" clearable>
            <el-option 
              v-for="(text, value) in PAY_CHANNEL_TEXT" 
              :key="value" 
              :label="text" 
              :value="parseInt(value)" 
            />
          </el-select>
        </el-form-item>
        <el-form-item label="支付时间">
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

      <!-- 支付订单列表 -->
      <el-table :data="payList" style="width: 100%" v-loading="loading">
        <el-table-column prop="consumeTradeId" label="支付单号" width="180" />
        <el-table-column prop="orderId" label="订单号" width="180" />
        <el-table-column prop="buyerId" label="买家ID" width="120" />
        <el-table-column prop="orderPaymentAmount" label="支付金额" width="120">
          <template #default="scope">
            ¥{{ formatAmount(scope.row.orderPaymentAmount) }}
          </template>
        </el-table-column>
        <el-table-column prop="paymentChannelId" label="支付渠道" width="120">
          <template #default="scope">
            {{ getChannelText(scope.row.paymentChannelId) }}
          </template>
        </el-table-column>
        <el-table-column prop="orderStateId" label="支付状态" width="100">
          <template #default="scope">
            <el-tag :type="getStatusTagType(scope.row.orderStateId)">
              {{ getStatusText(scope.row.orderStateId) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="tradeCreateTime" label="创建时间" width="180">
          <template #default="scope">
            {{ formatDate(scope.row.tradeCreateTime) }}
          </template>
        </el-table-column>
        <el-table-column prop="tradePayTime" label="支付时间" width="180">
          <template #default="scope">
            {{ formatDate(scope.row.tradePayTime) }}
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

    <!-- 支付订单详情对话框 -->
    <el-dialog
      v-model="dialogVisible"
      title="支付订单详情"
      width="800px"
      :close-on-click-modal="false"
    >
      <div v-if="currentPay" class="pay-detail">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="支付单号">{{ currentPay.consumeTradeId }}</el-descriptions-item>
          <el-descriptions-item label="订单号">{{ currentPay.orderId }}</el-descriptions-item>
          <el-descriptions-item label="买家ID">{{ currentPay.buyerId }}</el-descriptions-item>
          <el-descriptions-item label="卖家ID">{{ currentPay.sellerId }}</el-descriptions-item>
          <el-descriptions-item label="支付人ID">{{ currentPay.payUserId }}</el-descriptions-item>
          <el-descriptions-item label="支付状态">
            <el-tag :type="getStatusTagType(currentPay.orderStateId)">
              {{ getStatusText(currentPay.orderStateId) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="交易类型">{{ getTradeTypeText(currentPay.tradeTypeId) }}</el-descriptions-item>
          <el-descriptions-item label="支付渠道">{{ getChannelText(currentPay.paymentChannelId) }}</el-descriptions-item>
          <el-descriptions-item label="总付款金额">¥{{ formatAmount(currentPay.orderPaymentAmount) }}</el-descriptions-item>
          <el-descriptions-item label="实付金额">¥{{ formatAmount(currentPay.tradePaymentAmount) }}</el-descriptions-item>
          <el-descriptions-item label="余额支付">¥{{ formatAmount(currentPay.tradePaymentMoney) }}</el-descriptions-item>
          <el-descriptions-item label="充值卡支付">¥{{ formatAmount(currentPay.tradePaymentRechargeCard) }}</el-descriptions-item>
          <el-descriptions-item label="创建时间">{{ formatDate(currentPay.tradeCreateTime) }}</el-descriptions-item>
          <el-descriptions-item label="支付时间">{{ formatDate(currentPay.tradePayTime) }}</el-descriptions-item>
          <el-descriptions-item label="结束时间">{{ formatDate(currentPay.tradeFinishTime) }}</el-descriptions-item>
          <el-descriptions-item label="交易标题">{{ currentPay.tradeTitle }}</el-descriptions-item>
          <el-descriptions-item label="交易描述" :span="2">{{ currentPay.tradeDesc }}</el-descriptions-item>
        </el-descriptions>
      </div>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">关闭</el-button>
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
import { ElMessage } from 'element-plus'
import { payApi, PAY_STATUS, PAY_STATUS_TEXT, PAY_STATUS_TAG_TYPE, PAY_CHANNEL, PAY_CHANNEL_TEXT, TRADE_TYPE, TRADE_TYPE_TEXT } from '../../api/payment/pay'

// 响应式数据
const loading = ref(false)
const exportLoading = ref(false)
const dialogVisible = ref(false)
const currentPay = ref(null)
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

// 搜索表单
const searchForm = reactive({
  orderId: '',
  consumeTradeId: '',
  orderStateId: '',
  paymentChannelId: '',
  dateRange: [],
  pageNum: 1,
  pageSize: 10
})

// 支付订单列表
const payList = ref([])

// 生命周期
onMounted(() => {
  fetchPayList()
})

// 获取支付订单列表
const fetchPayList = async () => {
  loading.value = true
  try {
    const params = {
      ...searchForm,
      pageNum: currentPage.value,
      pageSize: pageSize.value
    }
    
    // 处理日期范围
    if (searchForm.dateRange && searchForm.dateRange.length === 2) {
      params.startDate = searchForm.dateRange[0]
      params.endDate = searchForm.dateRange[1]
    }
    delete params.dateRange
    
    const response = await payApi.getPaymentList(params)
    
    if (response.code === 200 || response.success) {
      const data = response.data || response
      payList.value = data.records || data.list || []
      total.value = data.total || 0
    } else {
      ElMessage.error(response.message || '获取支付订单列表失败')
      payList.value = []
      total.value = 0
    }
  } catch (error) {
    console.error('获取支付订单列表失败:', error)
    ElMessage.error('获取支付订单列表失败: ' + (error.message || '网络错误'))
    payList.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}

// 获取状态标签类型
const getStatusTagType = (status) => {
  return PAY_STATUS_TAG_TYPE[status] || 'info'
}

// 获取状态文本
const getStatusText = (status) => {
  return PAY_STATUS_TEXT[status] || '未知状态'
}

// 获取渠道文本
const getChannelText = (channel) => {
  return PAY_CHANNEL_TEXT[channel] || '未知渠道'
}

// 获取交易类型文本
const getTradeTypeText = (type) => {
  return TRADE_TYPE_TEXT[type] || '未知类型'
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
  fetchPayList()
}

// 重置搜索
const handleReset = () => {
  Object.assign(searchForm, {
    orderId: '',
    consumeTradeId: '',
    orderStateId: '',
    paymentChannelId: '',
    dateRange: [],
    pageNum: 1,
    pageSize: 10
  })
  currentPage.value = 1
  fetchPayList()
}

// 查看支付订单详情
const handleView = async (row) => {
  try {
    const key = row.consumeTradeId || row.id
    if (!key) {
      ElMessage.warning('缺少支付单号，无法查看详情')
      return
    }
    const response = await payApi.getPaymentDetail(key)
    if (response.code === 200 || response.success) {
      currentPay.value = response.data || response
      dialogVisible.value = true
    } else {
      ElMessage.error(response.message || '获取支付订单详情失败')
    }
  } catch (error) {
    console.error('获取支付订单详情失败:', error)
    ElMessage.error('获取支付订单详情失败: ' + (error.message || '网络错误'))
  }
}

// 导出订单
const handleExport = () => {
  ElMessage.info('导出功能开发中...')
}

// 分页大小改变
const handleSizeChange = (val) => {
  pageSize.value = val
  searchForm.pageSize = val
  fetchPayList()
}

// 当前页改变
const handleCurrentChange = (val) => {
  currentPage.value = val
  searchForm.pageNum = val
  fetchPayList()
}
</script>

<style scoped>
.pay-management {
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

.pay-detail {
  padding: 20px 0;
}

.dialog-footer {
  text-align: right;
}
</style>
