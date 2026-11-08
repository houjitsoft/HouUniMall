<template>
  <div class="payment-transaction-container">
    <!-- 统计卡片 -->
    <el-row :gutter="20" class="stats-cards">
      <el-col :span="6">
        <el-card class="stat-card total-transaction">
          <div class="stat-content">
            <div class="stat-icon"><el-icon><Document /></el-icon></div>
            <div class="stat-info">
              <div class="stat-value">{{ transactionStats.total }}</div>
              <div class="stat-label">交易总数</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card today-transaction">
          <div class="stat-content">
            <div class="stat-icon"><el-icon><Calendar /></el-icon></div>
            <div class="stat-info">
              <div class="stat-value">{{ transactionStats.today }}</div>
              <div class="stat-label">今日交易</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card success-transaction">
          <div class="stat-content">
            <div class="stat-icon"><el-icon><CircleCheck /></el-icon></div>
            <div class="stat-info">
              <div class="stat-value">{{ transactionStats.success }}</div>
              <div class="stat-label">成功交易</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card amount-transaction">
          <div class="stat-content">
            <div class="stat-icon"><el-icon><Money /></el-icon></div>
            <div class="stat-info">
              <div class="stat-value">¥{{ transactionStats.amount }}</div>
              <div class="stat-label">交易总额</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 搜索筛选 -->
    <el-card class="search-card">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="交易流水号">
          <el-input v-model="searchForm.transactionId" placeholder="请输入交易流水号" clearable />
        </el-form-item>
        <el-form-item label="订单号">
          <el-input v-model="searchForm.orderNo" placeholder="请输入订单号" clearable />
        </el-form-item>
        <el-form-item label="交易类型">
          <el-select v-model="searchForm.transactionType" placeholder="请选择交易类型" clearable>
            <el-option label="支付" value="payment" />
            <el-option label="退款" value="refund" />
            <el-option label="转账" value="transfer" />
            <el-option label="提现" value="withdraw" />
          </el-select>
        </el-form-item>
        <el-form-item label="交易状态">
          <el-select v-model="searchForm.status" placeholder="请选择状态" clearable>
            <el-option label="处理中" value="processing" />
            <el-option label="成功" value="success" />
            <el-option label="失败" value="failed" />
            <el-option label="取消" value="cancelled" />
          </el-select>
        </el-form-item>
        <el-form-item label="金额范围">
          <el-input-number v-model="searchForm.minAmount" placeholder="最小金额" :min="0" style="width: 120px" />
          <span style="margin: 0 10px">至</span>
          <el-input-number v-model="searchForm.maxAmount" placeholder="最大金额" :min="0" style="width: 120px" />
        </el-form-item>
        <el-form-item label="交易时间">
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
          <el-button type="primary" @click="handleExport">
            <el-icon><Download /></el-icon>导出记录
          </el-button>
          <el-button type="success" @click="handleBatchQuery">
            <el-icon><Search /></el-icon>批量查询
          </el-button>
          <el-button type="warning" @click="handleReconciliation">
            <el-icon><Check /></el-icon>对账处理
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
        :data="transactionList"
        v-loading="loading"
        style="width: 100%"
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="55" />
        <el-table-column prop="transactionId" label="交易流水号" width="200" />
        <el-table-column prop="orderNo" label="订单号" width="180" />
        <el-table-column prop="transactionType" label="交易类型" width="100">
          <template #default="{ row }">
            <el-tag :type="getTransactionTypeColor(row.transactionType)">
              {{ getTransactionTypeText(row.transactionType) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="amount" label="交易金额" width="120">
          <template #default="{ row }">
            <span :class="getAmountClass(row.transactionType)">
              {{ getAmountPrefix(row.transactionType) }}¥{{ row.amount }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="交易状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="paymentMethod" label="支付方式" width="120">
          <template #default="{ row }">
            {{ getPaymentMethodText(row.paymentMethod) }}
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="180" />
        <el-table-column prop="finishTime" label="完成时间" width="180" />
        <el-table-column prop="userInfo" label="用户信息" width="150" />
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="handleView(row)">
              <el-icon><View /></el-icon>查看
            </el-button>
            <el-button link type="success" @click="handleQuery(row)">
              <el-icon><Search /></el-icon>查询
            </el-button>
            <el-button link type="warning" @click="handleReconcile(row)">
              <el-icon><Check /></el-icon>对账
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
    <el-dialog v-model="dialogVisible" title="交易详情" width="900px">
      <div v-if="currentTransaction" class="transaction-detail">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-descriptions title="基本信息" :column="1" border>
              <el-descriptions-item label="交易流水号">{{ currentTransaction.transactionId }}</el-descriptions-item>
              <el-descriptions-item label="订单号">{{ currentTransaction.orderNo }}</el-descriptions-item>
              <el-descriptions-item label="交易类型">
                <el-tag :type="getTransactionTypeColor(currentTransaction.transactionType)">
                  {{ getTransactionTypeText(currentTransaction.transactionType) }}
                </el-tag>
              </el-descriptions-item>
              <el-descriptions-item label="交易金额">
                <span :class="getAmountClass(currentTransaction.transactionType)">
                  {{ getAmountPrefix(currentTransaction.transactionType) }}¥{{ currentTransaction.amount }}
                </span>
              </el-descriptions-item>
              <el-descriptions-item label="交易状态">
                <el-tag :type="getStatusType(currentTransaction.status)">
                  {{ getStatusText(currentTransaction.status) }}
                </el-tag>
              </el-descriptions-item>
            </el-descriptions>
          </el-col>
          <el-col :span="12">
            <el-descriptions title="支付信息" :column="1" border>
              <el-descriptions-item label="支付方式">{{ getPaymentMethodText(currentTransaction.paymentMethod) }}</el-descriptions-item>
              <el-descriptions-item label="用户信息">{{ currentTransaction.userInfo }}</el-descriptions-item>
              <el-descriptions-item label="创建时间">{{ currentTransaction.createTime }}</el-descriptions-item>
              <el-descriptions-item label="完成时间">{{ currentTransaction.finishTime }}</el-descriptions-item>
              <el-descriptions-item label="第三方流水号">{{ currentTransaction.thirdPartyId }}</el-descriptions-item>
            </el-descriptions>
          </el-col>
        </el-row>
        <div class="transaction-detail-remark" v-if="currentTransaction.remark">
          <h4>备注信息</h4>
          <p>{{ currentTransaction.remark }}</p>
        </div>
        <div class="transaction-timeline">
          <h4>交易时间线</h4>
          <el-timeline>
            <el-timeline-item
              v-for="item in currentTransaction.timeline"
              :key="item.time"
              :timestamp="item.time"
              :type="item.type"
            >
              {{ item.content }}
            </el-timeline-item>
          </el-timeline>
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
  Document, Calendar, CircleCheck, Money, Search, Refresh, Download,
  Check, View
} from '@element-plus/icons-vue'
import { payApi } from '@/api/payment/pay'

// 响应式数据
const loading = ref(false)
const dialogVisible = ref(false)
const transactionList = ref([])
const selectedTransactions = ref([])
const currentTransaction = ref(null)

// 统计数据
const transactionStats = ref({
  total: 0,
  today: 0,
  success: 0,
  amount: '0.00'
})

// 搜索表单
const searchForm = reactive({
  transactionId: '',
  orderNo: '',
  transactionType: '',
  status: '',
  minAmount: null,
  maxAmount: null,
  dateRange: null
})

// 分页信息
const pagination = reactive({
  page: 1,
  size: 10,
  total: 0
})

// 交易类型映射
const getTransactionTypeText = (type) => {
  const map = {
    'payment': '支付',
    'refund': '退款',
    'transfer': '转账',
    'withdraw': '提现',
    '1': '充值',
    '2': '提现',
    '3': '购物消费',
    '4': '转账'
  }
  return map[type] || map[String(type)] || type
}

const getTransactionTypeColor = (type) => {
  const map = {
    'payment': 'success',
    'refund': 'warning',
    'transfer': 'primary',
    'withdraw': 'info',
    '1': 'success',
    '2': 'warning',
    '3': 'primary',
    '4': 'info'
  }
  return map[type] || map[String(type)] || 'info'
}

// 金额显示
const getAmountPrefix = (type) => {
  return type === 'refund' || type === 'withdraw' ? '-' : '+'
}

const getAmountClass = (type) => {
  return type === 'refund' || type === 'withdraw' ? 'amount-minus' : 'amount-plus'
}

// 状态映射
const getStatusText = (status) => {
  const map = {
    'processing': '处理中',
    'success': '成功',
    'failed': '失败',
    'cancelled': '取消'
  }
  return map[status] || status
}

const getStatusType = (status) => {
  const map = {
    'processing': 'warning',
    'success': 'success',
    'failed': 'danger',
    'cancelled': 'info'
  }
  return map[status] || 'info'
}

// 支付方式映射
const getPaymentMethodText = (method) => {
  const map = {
    'alipay': '支付宝',
    'wechat': '微信支付',
    'bank': '银行卡',
    'balance': '余额支付'
  }
  return map[method] || method
}

// 获取交易列表（透传资金流水分页，走 hou_pay_consume_record 真实数据）
const getTransactionList = async () => {
  loading.value = true
  try {
    const params = {
      pageNum: pagination.page,
      pageSize: pagination.size,
      recordPayorder: searchForm.orderNo || undefined
    }
    if (searchForm.dateRange && searchForm.dateRange.length === 2) {
      params.startDate = searchForm.dateRange[0]
      params.endDate = searchForm.dateRange[1]
    }
    const response = await payApi.getAccountRecords(params)
    if (response.code === 200) {
      const data = response.data || {}
      const records = data.records || data.list || []
      const rows = records.map((r) => ({
        id: r.consumeRecordId || r.id,
        transactionId: r.consumeRecordId || r.recordPayorder,
        orderNo: r.orderId || r.recordPayorder,
        transactionType: String(r.detailType ?? r.tradeTypeId ?? ''),
        amount: r.recordMoney ?? 0,
        status: r.recordStatus == null ? 'success' : (r.recordStatus === 1 ? 'success' : 'processing'),
        paymentMethod: r.tradeTypeId,
        userInfo: r.userNickname || r.userId,
        createTime: r.recordTime || r.recordDate,
        finishTime: r.recordPaytime || '',
        thirdPartyId: r.recordPayorder,
        remark: r.recordTitle || r.recordDesc || '',
        timeline: []
      }))
      transactionList.value = rows
      pagination.total = data.total || data.totalCount || 0
      // 汇总统计
      let totalAmount = 0
      let successCount = 0
      records.forEach((r) => {
        totalAmount += Number(r.recordMoney || 0)
        if (r.recordStatus === 1) successCount += 1
      })
      transactionStats.value = {
        total: data.total || data.totalCount || records.length,
        today: records.length,
        success: successCount,
        amount: totalAmount.toLocaleString('zh-CN', { minimumFractionDigits: 2 })
      }
    } else {
      transactionList.value = []
      pagination.total = 0
    }
  } catch (error) {
    console.error('获取交易记录失败:', error)
    ElMessage.error('获取交易记录失败: ' + (error.message || '网络错误'))
    transactionList.value = []
    pagination.total = 0
  } finally {
    loading.value = false
  }
}

// 搜索
const handleSearch = () => {
  pagination.page = 1
  getTransactionList()
}

// 重置
const handleReset = () => {
  Object.assign(searchForm, {
    transactionId: '',
    orderNo: '',
    transactionType: '',
    status: '',
    minAmount: null,
    maxAmount: null,
    dateRange: null
  })
  handleSearch()
}

// 刷新
const handleRefresh = () => {
  getTransactionList()
}

// 导出
const handleExport = () => {
  ElMessage.info('导出功能开发中')
}

// 批量查询
const handleBatchQuery = () => {
  if (selectedTransactions.value.length === 0) {
    ElMessage.warning('请选择要查询的交易记录')
    return
  }
  ElMessage.info('批量查询功能开发中')
}

// 对账处理
const handleReconciliation = () => {
  ElMessage.info('对账处理功能开发中')
}

// 查看详情
const handleView = (row) => {
  currentTransaction.value = row
  dialogVisible.value = true
}

// 查询
const handleQuery = (row) => {
  ElMessage.info('交易查询功能开发中')
}

// 对账
const handleReconcile = (row) => {
  ElMessage.info('交易对账功能开发中')
}

// 选择变化
const handleSelectionChange = (selection) => {
  selectedTransactions.value = selection
}

// 分页大小变化
const handleSizeChange = (size) => {
  pagination.size = size
  getTransactionList()
}

// 页码变化
const handleCurrentChange = (page) => {
  pagination.page = page
  getTransactionList()
}

// 组件挂载
onMounted(() => {
  getTransactionList()
})
</script>

<style scoped>
.payment-transaction-container {
  padding: 20px;
}

.stats-cards {
  margin-bottom: 20px;
}

.stat-card {
  border-left: 4px solid #409eff;
}

.stat-card.total-transaction {
  border-left-color: #409eff;
}

.stat-card.today-transaction {
  border-left-color: #67c23a;
}

.stat-card.success-transaction {
  border-left-color: #e6a23c;
}

.stat-card.amount-transaction {
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

.amount-plus {
  color: #67c23a;
  font-weight: bold;
}

.amount-minus {
  color: #f56c6c;
  font-weight: bold;
}

.transaction-detail-remark {
  margin-top: 20px;
}

.transaction-detail-remark h4 {
  margin-bottom: 10px;
  color: #303133;
}

.transaction-detail-remark p {
  color: #606266;
  line-height: 1.5;
}

.transaction-timeline {
  margin-top: 20px;
}

.transaction-timeline h4 {
  margin-bottom: 15px;
  color: #303133;
}
</style>
