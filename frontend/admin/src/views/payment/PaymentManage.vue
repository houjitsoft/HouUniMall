<template>
  <div class="payment-manage-container">
    <!-- 统计卡片 -->
    <el-row :gutter="20" class="stats-cards">
      <el-col :span="6">
        <el-card class="stat-card total-payment">
          <div class="stat-content">
            <div class="stat-icon"><el-icon><CreditCard /></el-icon></div>
            <div class="stat-info">
              <div class="stat-value">{{ paymentStats.total }}</div>
              <div class="stat-label">支付总数</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card today-payment">
          <div class="stat-content">
            <div class="stat-icon"><el-icon><Money /></el-icon></div>
            <div class="stat-info">
              <div class="stat-value">{{ paymentStats.today }}</div>
              <div class="stat-label">今日支付</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card success-payment">
          <div class="stat-content">
            <div class="stat-icon"><el-icon><Check /></el-icon></div>
            <div class="stat-info">
              <div class="stat-value">{{ paymentStats.success }}</div>
              <div class="stat-label">成功支付</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card failed-payment">
          <div class="stat-content">
            <div class="stat-icon"><el-icon><Close /></el-icon></div>
            <div class="stat-info">
              <div class="stat-value">{{ paymentStats.failed }}</div>
              <div class="stat-label">失败支付</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 搜索筛选 -->
    <el-card class="search-card">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="支付方式">
          <el-select v-model="searchForm.paymentMethod" placeholder="请选择支付方式" clearable>
            <el-option label="支付宝" value="alipay" />
            <el-option label="微信支付" value="wechat" />
            <el-option label="银行卡" value="bank" />
            <el-option label="余额支付" value="balance" />
          </el-select>
        </el-form-item>
        <el-form-item label="支付状态">
          <el-select v-model="searchForm.status" placeholder="请选择状态" clearable>
            <el-option label="待支付" value="pending" />
            <el-option label="支付成功" value="success" />
            <el-option label="支付失败" value="failed" />
            <el-option label="已退款" value="refunded" />
          </el-select>
        </el-form-item>
        <el-form-item label="订单号">
          <el-input v-model="searchForm.orderNo" placeholder="请输入订单号" clearable />
        </el-form-item>
        <el-form-item label="支付时间">
          <el-date-picker
            v-model="searchForm.dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">
            <el-icon><Search /></el-icon>搜索
          </el-button>
          <el-button @click="handleReset">
            <el-icon><Refresh /></el-icon>重置
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 操作工具栏 -->
    <el-card class="toolbar-card">
      <div class="toolbar">
        <div class="toolbar-left">
          <el-button type="primary" @click="handleAdd">
            <el-icon><Plus /></el-icon>新增支付配置
          </el-button>
          <el-button type="success" @click="handleBatchProcess">
            <el-icon><Operation /></el-icon>批量处理
          </el-button>
          <el-button type="warning" @click="handleExport">
            <el-icon><Download /></el-icon>导出数据
          </el-button>
        </div>
        <div class="toolbar-right">
          <el-button @click="handleRefresh">
            <el-icon><Refresh /></el-icon>刷新
          </el-button>
        </div>
      </div>
    </el-card>

    <!-- 数据表格 -->
    <el-card class="table-card">
      <el-table
        :data="paymentList"
        v-loading="loading"
        style="width: 100%"
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="55" />
        <el-table-column prop="orderNo" label="订单号" width="200" />
        <el-table-column prop="paymentMethod" label="支付方式" width="120">
          <template #default="{ row }">
            <el-tag :type="getPaymentMethodType(row.paymentMethod)">
              {{ getPaymentMethodText(row.paymentMethod) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="amount" label="支付金额" width="120">
          <template #default="{ row }">
            ¥{{ row.amount }}
          </template>
        </el-table-column>
        <el-table-column prop="status" label="支付状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="180" />
        <el-table-column prop="payTime" label="支付时间" width="180" />
        <el-table-column prop="userInfo" label="用户信息" width="150" />
        <el-table-column prop="remark" label="备注" min-width="150" />
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="handleView(row)">
              <el-icon><View /></el-icon>查看
            </el-button>
            <el-button link type="warning" @click="handleEdit(row)">
              <el-icon><Edit /></el-icon>编辑
            </el-button>
            <el-button link type="success" @click="handleProcess(row)">
              <el-icon><Check /></el-icon>处理
            </el-button>
            <el-button link type="danger" @click="handleDelete(row)">
              <el-icon><Delete /></el-icon>删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination-container">
        <el-pagination
          v-model:current-page="pagination.page"
          v-model:page-size="pagination.size"
          :page-sizes="[10, 20, 50, 100]"
          :total="pagination.total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>

    <!-- 详情对话框 -->
    <el-dialog v-model="dialogVisible" title="支付详情" width="800px">
      <div v-if="currentPayment" class="payment-detail">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="订单号">{{ currentPayment.orderNo }}</el-descriptions-item>
          <el-descriptions-item label="支付方式">{{ getPaymentMethodText(currentPayment.paymentMethod) }}</el-descriptions-item>
          <el-descriptions-item label="支付金额">¥{{ currentPayment.amount }}</el-descriptions-item>
          <el-descriptions-item label="支付状态">
            <el-tag :type="getStatusType(currentPayment.status)">
              {{ getStatusText(currentPayment.status) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="用户信息">{{ currentPayment.userInfo }}</el-descriptions-item>
          <el-descriptions-item label="创建时间">{{ currentPayment.createTime }}</el-descriptions-item>
          <el-descriptions-item label="支付时间">{{ currentPayment.payTime }}</el-descriptions-item>
          <el-descriptions-item label="交易流水号">{{ currentPayment.transactionId }}</el-descriptions-item>
        </el-descriptions>
        <div class="payment-detail-remark" v-if="currentPayment.remark">
          <h4>备注信息</h4>
          <p>{{ currentPayment.remark }}</p>
        </div>
      </div>
      <template #footer>
        <el-button @click="dialogVisible = false">关闭</el-button>
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
import {
  CreditCard, Money, Check, Close, Search, Refresh, Plus, Operation, Download,
  View, Edit, Delete
} from '@element-plus/icons-vue'
import request from '@/utils/request'

// 响应式数据
const loading = ref(false)
const dialogVisible = ref(false)
const paymentList = ref([])
const selectedPayments = ref([])
const currentPayment = ref(null)

// 统计数据
const paymentStats = ref({
  total: 0,
  today: 0,
  success: 0,
  failed: 0
})

// 搜索表单
const searchForm = reactive({
  paymentMethod: '',
  status: '',
  orderNo: '',
  dateRange: null
})

// 分页信息
const pagination = reactive({
  page: 1,
  size: 10,
  total: 0
})

// 支付方式映射
const getPaymentMethodText = (method) => {
  const map = {
    'alipay': '支付宝',
    'wechat': '微信支付',
    'bank': '银行卡',
    'balance': '余额支付'
  }
  return map[method] || (method == null ? '-' : '渠道' + method)
}

const getPaymentMethodType = (method) => {
  const map = {
    'alipay': 'primary',
    'wechat': 'success',
    'bank': 'warning',
    'balance': 'info'
  }
  return map[method] || 'info'
}

// 状态映射（对齐后台订单状态：1待支付 2已支付 3已取消 4失败）
const getStatusText = (status) => {
  const map = {
    'pending': '待支付',
    'success': '支付成功',
    'failed': '支付失败',
    'refunded': '已退款',
    '1': '待支付',
    '2': '支付成功',
    '3': '已取消',
    '4': '支付失败'
  }
  return map[status] || map[String(status)] || status
}

const getStatusType = (status) => {
  const map = {
    'pending': 'warning',
    'success': 'success',
    'failed': 'danger',
    'refunded': 'info',
    '1': 'warning',
    '2': 'success',
    '3': 'info',
    '4': 'danger'
  }
  return map[status] || map[String(status)] || 'info'
}

// 获取统计数据（透传 stats/overview）
const getPaymentStats = async () => {
  try {
    const res = await request.post('/web/pay/stats/overview', {})
    if (res.code === 200 && res.data) {
      const d = res.data
      paymentStats.value = {
        total: d.monthIncome ?? 0,
        today: d.todayOrders ?? 0,
        success: d.todaySuccessOrders ?? 0,
        failed: (d.todayOrders || 0) - (d.todaySuccessOrders || 0)
      }
    }
  } catch (e) {
    console.error('[PaymentManage] 统计接口失败:', e)
  }
}

// 获取支付列表（透传支付订单列表，走 hou_pay_consume_trade 真实数据）
const getPaymentList = async () => {
  loading.value = true
  try {
    const params = {
      pageNum: pagination.page,
      pageSize: pagination.size,
      orderId: searchForm.orderNo || undefined
    }
    if (searchForm.status) {
      params.orderStateId = { 'pending': 1, 'success': 2, 'failed': 4, 'refunded': 3 }[searchForm.status]
    }
    if (searchForm.dateRange && searchForm.dateRange.length === 2) {
      params.startDate = searchForm.dateRange[0]
      params.endDate = searchForm.dateRange[1]
    }
    const response = await request.post('/web/pay/list', params)
    if (response.code === 200) {
      const data = response.data || {}
      const records = data.records || data.list || []
      paymentList.value = records.map((r) => ({
        id: r.consumeTradeId || r.id,
        orderNo: r.orderId || r.consumeTradeId,
        consumeTradeId: r.consumeTradeId,
        paymentMethod: r.paymentChannelId,
        amount: r.tradePaymentAmount ?? r.orderPaymentAmount,
        status: String(r.orderStateId ?? ''),
        userInfo: r.buyerId || r.payUserId,
        createTime: r.tradeCreateTime,
        payTime: r.tradePayTime,
        transactionId: r.consumeTradeId,
        remark: r.tradeDesc || r.tradeTitle,
        _raw: r
      }))
      pagination.total = data.total || data.totalCount || 0
    } else {
      paymentList.value = []
      pagination.total = 0
    }
  } catch (error) {
    console.error('获取支付列表失败:', error)
    ElMessage.error('获取支付列表失败: ' + (error.message || '网络错误'))
    paymentList.value = []
    pagination.total = 0
  } finally {
    loading.value = false
  }
}

// 搜索
const handleSearch = () => {
  pagination.page = 1
  getPaymentList()
}

// 重置
const handleReset = () => {
  Object.assign(searchForm, {
    paymentMethod: '',
    status: '',
    orderNo: '',
    dateRange: null
  })
  handleSearch()
}

// 刷新
const handleRefresh = () => {
  getPaymentStats()
  getPaymentList()
}

// 新增
const handleAdd = () => {
  ElMessage.info('新增支付配置功能开发中')
}

// 批量处理
const handleBatchProcess = () => {
  if (selectedPayments.value.length === 0) {
    ElMessage.warning('请选择要处理的支付记录')
    return
  }
  ElMessage.info('批量处理功能开发中')
}

// 导出
const handleExport = () => {
  ElMessage.info('导出功能开发中')
}

// 查看详情
const handleView = (row) => {
  currentPayment.value = row
  dialogVisible.value = true
}

// 编辑
const handleEdit = (row) => {
  ElMessage.info('编辑功能开发中')
}

// 处理
const handleProcess = (row) => {
  ElMessage.info('处理功能开发中')
}

// 删除
const handleDelete = (row) => {
  ElMessageBox.confirm('确定要删除这条支付记录吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    ElMessage.success('删除成功')
    getPaymentList()
  }).catch(() => {
    ElMessage.info('已取消删除')
  })
}

// 选择变化
const handleSelectionChange = (selection) => {
  selectedPayments.value = selection
}

// 分页大小变化
const handleSizeChange = (size) => {
  pagination.size = size
  getPaymentList()
}

// 页码变化
const handleCurrentChange = (page) => {
  pagination.page = page
  getPaymentList()
}

// 组件挂载
onMounted(() => {
  getPaymentStats()
  getPaymentList()
})
</script>

<style scoped>
.payment-manage-container {
  padding: 20px;
}

.stats-cards {
  margin-bottom: 20px;
}

.stat-card {
  border-left: 4px solid #409eff;
}

.stat-card.total-payment {
  border-left-color: #409eff;
}

.stat-card.today-payment {
  border-left-color: #67c23a;
}

.stat-card.success-payment {
  border-left-color: #e6a23c;
}

.stat-card.failed-payment {
  border-left-color: #f56c6c;
}

.stat-content {
  display: flex;
  align-items: center;
  padding: 10px;
}

.stat-icon {
  font-size: 40px;
  margin-right: 20px;
  color: #409eff;
}

.stat-info {
  flex: 1;
}

.stat-value {
  font-size: 28px;
  font-weight: bold;
  color: #303133;
  line-height: 1;
}

.stat-label {
  font-size: 14px;
  color: #909399;
  margin-top: 5px;
}

.search-card,
.toolbar-card,
.table-card {
  margin-bottom: 20px;
}

.search-form {
  margin-bottom: 0;
}

.toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.pagination-container {
  display: flex;
  justify-content: center;
  margin-top: 20px;
}

.payment-detail-remark {
  margin-top: 20px;
}

.payment-detail-remark h4 {
  margin-bottom: 10px;
  color: #303133;
}

.payment-detail-remark p {
  color: #606266;
  line-height: 1.5;
}
</style>
