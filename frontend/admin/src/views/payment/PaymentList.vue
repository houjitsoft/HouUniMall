<template>
  <div class="payment-management">
    <el-card class="box-card">
      <template #header>
        <div class="card-header">
          <span>支付管理</span>
          <el-button type="primary" @click="handleExport">导出记录</el-button>
        </div>
      </template>
      
      <!-- 搜索栏 -->
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="订单号">
          <el-input v-model="searchForm.orderNo" placeholder="请输入订单号" clearable />
        </el-form-item>
        <el-form-item label="支付状态">
          <el-select v-model="searchForm.status" placeholder="请选择状态" clearable>
            <el-option label="待支付" value="pending" />
            <el-option label="支付成功" value="success" />
            <el-option label="支付失败" value="failed" />
            <el-option label="已退款" value="refunded" />
          </el-select>
        </el-form-item>
        <el-form-item label="支付方式">
          <el-select v-model="searchForm.method" placeholder="请选择方式" clearable>
            <el-option label="微信支付" value="wechat" />
            <el-option label="支付宝" value="alipay" />
            <el-option label="银行卡" value="bank" />
            <el-option label="现金" value="cash" />
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

      <!-- 支付记录列表 -->
      <el-table :data="paymentList" style="width: 100%" v-loading="loading">
        <el-table-column prop="id" label="支付ID" width="100" />
        <el-table-column prop="orderNo" label="订单号" width="180" />
        <el-table-column prop="amount" label="支付金额" width="120">
          <template #default="scope">
            ¥{{ scope.row.amount }}
          </template>
        </el-table-column>
        <el-table-column prop="method" label="支付方式" width="120">
          <template #default="scope">
            <el-tag>{{ getMethodText(scope.row.method) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="支付状态" width="100">
          <template #default="scope">
            <el-tag :type="getStatusType(scope.row.status)">
              {{ getStatusText(scope.row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="180" />
        <el-table-column prop="payTime" label="支付时间" width="180" />
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="scope">
            <el-button size="small" @click="handleView(scope.row)">查看</el-button>
            <el-button size="small" type="warning" @click="handleRefund(scope.row)" v-if="scope.row.status === 'success'">退款</el-button>
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

    <!-- 支付详情对话框 -->
    <el-dialog
      v-model="dialogVisible"
      title="支付详情"
      width="800px"
    >
      <div v-if="currentPayment" class="payment-detail">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="支付ID">{{ currentPayment.id }}</el-descriptions-item>
          <el-descriptions-item label="订单号">{{ currentPayment.orderNo }}</el-descriptions-item>
          <el-descriptions-item label="支付金额">¥{{ currentPayment.amount }}</el-descriptions-item>
          <el-descriptions-item label="支付方式">{{ getMethodText(currentPayment.method) }}</el-descriptions-item>
          <el-descriptions-item label="支付状态">
            <el-tag :type="getStatusType(currentPayment.status)">
              {{ getStatusText(currentPayment.status) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="创建时间">{{ currentPayment.createTime }}</el-descriptions-item>
          <el-descriptions-item label="支付时间" v-if="currentPayment.payTime">{{ currentPayment.payTime }}</el-descriptions-item>
          <el-descriptions-item label="交易流水号" v-if="currentPayment.transactionId">{{ currentPayment.transactionId }}</el-descriptions-item>
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
import { ElMessage, ElMessageBox } from 'element-plus'
import { payApi } from '@/api/payment/pay'

// 响应式数据
const loading = ref(false)
const dialogVisible = ref(false)
const currentPayment = ref(null)
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

// 搜索表单
const searchForm = reactive({
  orderNo: '',
  status: '',
  method: '',
  dateRange: []
})

// 支付数据
const paymentList = ref([])

// 生命周期
onMounted(() => {
  fetchPaymentList()
})

// 获取支付列表（透传支付订单列表，走 hou_pay_consume_trade 真实数据）
const fetchPaymentList = async () => {
  loading.value = true
  try {
    const params = {
      pageNum: currentPage.value,
      pageSize: pageSize.value,
      orderId: searchForm.orderNo || undefined
    }
    if (searchForm.status) {
      params.orderStateId = { pending: 1, success: 2, failed: 4, refunded: 3 }[searchForm.status]
    }
    if (searchForm.dateRange && searchForm.dateRange.length === 2) {
      params.startDate = searchForm.dateRange[0]
      params.endDate = searchForm.dateRange[1]
    }
    const response = await payApi.getPaymentList(params)
    if (response.code === 200) {
      const data = response.data || {}
      const records = data.records || data.list || []
      paymentList.value = records.map((r) => ({
        id: r.consumeTradeId || r.id,
        orderNo: r.orderId || r.consumeTradeId,
        amount: r.tradePaymentAmount ?? r.orderPaymentAmount,
        method: r.paymentChannelId,
        status: String(r.orderStateId ?? ''),
        createTime: r.tradeCreateTime,
        payTime: r.tradePayTime,
        transactionId: r.consumeTradeId,
        userInfo: r.buyerId || r.payUserId,
        remark: r.tradeDesc || r.tradeTitle,
        _raw: r
      }))
      total.value = data.total || data.totalCount || 0
    } else {
      paymentList.value = []
      total.value = 0
    }
  } catch (error) {
    console.error('获取支付列表失败:', error)
    ElMessage.error('获取支付列表失败: ' + (error.message || '网络错误'))
    paymentList.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}

// 获取支付方式文本
const getMethodText = (method) => {
  const methodMap = {
    wechat: '微信支付',
    alipay: '支付宝',
    bank: '银行卡',
    cash: '现金',
    1: '微信支付',
    2: '支付宝',
    3: '银联支付',
    4: '云闪付',
    5: '余额支付',
    6: '银行卡快捷'
  }
  return methodMap[method] || methodMap[String(method)] || '未知'
}

// 获取状态类型
const getStatusType = (status) => {
  const statusMap = {
    pending: 'warning',
    success: 'success',
    failed: 'danger',
    refunded: 'info',
    1: 'warning',
    2: 'success',
    3: 'info',
    4: 'danger'
  }
  return statusMap[status] || statusMap[String(status)] || 'info'
}

// 获取状态文本
const getStatusText = (status) => {
  const statusMap = {
    pending: '待支付',
    success: '支付成功',
    failed: '支付失败',
    refunded: '已退款',
    1: '待支付',
    2: '支付成功',
    3: '已取消',
    4: '支付失败'
  }
  return statusMap[status] || statusMap[String(status)] || '未知'
}

// 搜索
const handleSearch = () => {
  currentPage.value = 1
  fetchPaymentList()
}

// 重置搜索
const handleReset = () => {
  searchForm.orderNo = ''
  searchForm.status = ''
  searchForm.method = ''
  searchForm.dateRange = []
  handleSearch()
}

// 查看支付详情
const handleView = (row) => {
  currentPayment.value = row
  dialogVisible.value = true
}

// 退款
const handleRefund = (row) => {
  ElMessageBox.confirm('确定要退款吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    row.status = 'refunded'
    ElMessage.success('退款成功')
  })
}

// 导出记录
const handleExport = () => {
  ElMessage.info('导出功能开发中...')
}

// 分页大小改变
const handleSizeChange = (val) => {
  pageSize.value = val
  fetchPaymentList()
}

// 当前页改变
const handleCurrentChange = (val) => {
  currentPage.value = val
  fetchPaymentList()
}
</script>

<style scoped>
.payment-management {
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

.payment-detail {
  padding: 20px 0;
}

.dialog-footer {
  text-align: right;
}
</style>
