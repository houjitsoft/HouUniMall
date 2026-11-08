<template>
  <div class="account-management">
    <el-card class="box-card">
      <template #header>
        <div class="card-header">
          <span>账户管理</span>
          <el-button type="primary" @click="handleRefresh">刷新</el-button>
        </div>
      </template>
      
      <!-- 账户概览 -->
      <el-row :gutter="20" class="account-overview">
        <el-col :span="6">
          <el-card class="overview-card">
            <div class="overview-item">
              <div class="item-label">账户余额</div>
              <div class="item-value">¥{{ formatAmount(accountInfo.balance) }}</div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card class="overview-card">
            <div class="overview-item">
              <div class="item-label">冻结金额</div>
              <div class="item-value">¥{{ formatAmount(accountInfo.frozenAmount) }}</div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card class="overview-card">
            <div class="overview-item">
              <div class="item-label">可用余额</div>
              <div class="item-value">¥{{ formatAmount(accountInfo.availableBalance) }}</div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card class="overview-card">
            <div class="overview-item">
              <div class="item-label">累计收入</div>
              <div class="item-value">¥{{ formatAmount(accountInfo.totalIncome) }}</div>
            </div>
          </el-card>
        </el-col>
      </el-row>

      <!-- 账户信息 -->
      <el-card class="info-card">
        <template #header>
          <div class="card-header">
            <span>账户信息</span>
          </div>
        </template>
        <el-descriptions :column="2" border>
          <el-descriptions-item label="账户ID">{{ accountInfo.accountId }}</el-descriptions-item>
          <el-descriptions-item label="用户ID">{{ accountInfo.userId }}</el-descriptions-item>
          <el-descriptions-item label="账户类型">{{ getAccountTypeText(accountInfo.accountType) }}</el-descriptions-item>
          <el-descriptions-item label="账户状态">
            <el-tag :type="getAccountStatusTagType(accountInfo.status)">
              {{ getAccountStatusText(accountInfo.status) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="创建时间">{{ formatDate(accountInfo.createTime) }}</el-descriptions-item>
          <el-descriptions-item label="更新时间">{{ formatDate(accountInfo.updateTime) }}</el-descriptions-item>
          <el-descriptions-item label="备注" :span="2">{{ accountInfo.remark }}</el-descriptions-item>
        </el-descriptions>
      </el-card>

      <!-- 交易记录 -->
      <el-card class="transaction-card">
        <template #header>
          <div class="card-header">
            <span>交易记录</span>
            <div>
              <el-button type="primary" @click="handleExportTransactions">导出记录</el-button>
            </div>
          </div>
        </template>
        
        <!-- 交易记录搜索 -->
        <el-form :inline="true" :model="transactionSearchForm" class="search-form">
          <el-form-item label="交易类型">
            <el-select v-model="transactionSearchForm.tradeType" placeholder="请选择类型" clearable>
              <el-option 
                v-for="(text, value) in TRADE_TYPE_TEXT" 
                :key="value" 
                :label="text" 
                :value="parseInt(value)" 
              />
            </el-select>
          </el-form-item>
          <el-form-item label="交易时间">
            <el-date-picker
              v-model="transactionSearchForm.dateRange"
              type="daterange"
              range-separator="至"
              start-placeholder="开始日期"
              end-placeholder="结束日期"
              format="YYYY-MM-DD"
              value-format="YYYY-MM-DD"
            />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="handleSearchTransactions">搜索</el-button>
            <el-button @click="handleResetTransactions">重置</el-button>
          </el-form-item>
        </el-form>

        <!-- 交易记录列表 -->
        <el-table :data="transactionList" style="width: 100%" v-loading="transactionLoading">
          <el-table-column prop="tradeNo" label="交易编号" width="180" />
          <el-table-column prop="tradeType" label="交易类型" width="120">
            <template #default="scope">
              {{ getTradeTypeText(scope.row.tradeType) }}
            </template>
          </el-table-column>
          <el-table-column prop="amount" label="交易金额" width="120">
            <template #default="scope">
              {{ scope.row.amount >= 0 ? '+' : '' }}{{ formatAmount(scope.row.amount) }}
            </template>
          </el-table-column>
          <el-table-column prop="balance" label="账户余额" width="120">
            <template #default="scope">
              ¥{{ formatAmount(scope.row.balance) }}
            </template>
          </el-table-column>
          <el-table-column prop="tradeTime" label="交易时间" width="180">
            <template #default="scope">
              {{ formatDate(scope.row.tradeTime) }}
            </template>
          </el-table-column>
          <el-table-column prop="remark" label="备注" show-overflow-tooltip />
        </el-table>

        <!-- 分页 -->
        <div class="pagination-container">
          <el-pagination
            v-model:current-page="transactionPage.currentPage"
            v-model:page-size="transactionPage.pageSize"
            :page-sizes="[10, 20, 50, 100]"
            :total="transactionPage.total"
            layout="total, sizes, prev, pager, next, jumper"
            @size-change="handleTransactionSizeChange"
            @current-change="handleTransactionCurrentChange"
          />
        </div>
      </el-card>
    </el-card>
  </div>
</template>

<script setup>

// 使用 Element Plus 国际化系统
import { useI18n } from '@/i18n-element'
const { t } = useI18n()
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { payApi, TRADE_TYPE, TRADE_TYPE_TEXT } from '../../api/payment/pay'
import { useAuthStore } from '@/stores/auth'
import { getUserId } from '@/utils/auth'

// 响应式数据
const loading = ref(false)
const transactionLoading = ref(false)

// 账户信息
const accountInfo = ref({
  accountId: '',
  userId: '',
  accountType: 1,
  status: 1,
  balance: 0,
  frozenAmount: 0,
  availableBalance: 0,
  totalIncome: 0,
  createTime: '',
  updateTime: '',
  remark: ''
})

// 交易记录搜索表单
const transactionSearchForm = reactive({
  tradeType: '',
  dateRange: []
})

// 交易记录列表
const transactionList = ref([])

// 交易记录分页
const transactionPage = reactive({
  currentPage: 1,
  pageSize: 10,
  total: 0
})

// 账户类型枚举
const ACCOUNT_TYPE = {
  PERSONAL: 1,  // 个人账户
  BUSINESS: 2   // 企业账户
}

// 账户类型文本映射
const ACCOUNT_TYPE_TEXT = {
  [ACCOUNT_TYPE.PERSONAL]: '个人账户',
  [ACCOUNT_TYPE.BUSINESS]: '企业账户'
}

// 账户状态枚举
const ACCOUNT_STATUS = {
  ACTIVE: 1,    // 正常
  FROZEN: 2,    // 冻结
  CLOSED: 3     // 注销
}

// 账户状态文本映射
const ACCOUNT_STATUS_TEXT = {
  [ACCOUNT_STATUS.ACTIVE]: '正常',
  [ACCOUNT_STATUS.FROZEN]: '冻结',
  [ACCOUNT_STATUS.CLOSED]: '注销'
}

// 账户状态标签类型映射
const ACCOUNT_STATUS_TAG_TYPE = {
  [ACCOUNT_STATUS.ACTIVE]: 'success',
  [ACCOUNT_STATUS.FROZEN]: 'warning',
  [ACCOUNT_STATUS.CLOSED]: 'danger'
}

// 生命周期
onMounted(() => {
  fetchAccountInfo()
  fetchTransactionList()
})

// 获取账户信息
const fetchAccountInfo = async () => {
  loading.value = true
  try {
    // 使用当前登录用户 ID，避免写死示例值
    const authStore = useAuthStore()
    const userId = authStore.userInfo?.userId ?? authStore.userInfo?.user_id ?? getUserId()
    if (!userId) {
      ElMessage.warning('未获取到当前登录用户，请重新登录后重试')
      return
    }
    const response = await payApi.getAccountBalance(userId)
    
    if (response.code === 200 || response.success) {
      const data = response.data || response
      // 兼容新接口返回 IPage{records,total} 结构
      if (Array.isArray(data?.records)) {
        const item = data.records[0] || {}
        accountInfo.value = {
          ...accountInfo.value,
          userId: item.userId || userId,
          balance: item.userMoney ?? 0,
          frozenAmount: item.userMoneyFrozen ?? 0,
          availableBalance: item.userMoneyPendingSettlement ?? 0,
          userRechargeCard: item.userRechargeCard ?? 0,
          ...item
        }
      } else {
        accountInfo.value = { ...accountInfo.value, userId, ...data }
      }
    } else {
      ElMessage.error(response.message || '获取账户信息失败')
    }
  } catch (error) {
    console.error('获取账户信息失败:', error)
    ElMessage.error('获取账户信息失败: ' + (error.message || '网络错误'))
  } finally {
    loading.value = false
  }
}

// 获取交易记录列表
const fetchTransactionList = async () => {
  transactionLoading.value = true
  try {
    const params = {
      pageNum: transactionPage.currentPage,
      pageSize: transactionPage.pageSize,
      ...transactionSearchForm
    }
    
    // 处理日期范围
    if (transactionSearchForm.dateRange && transactionSearchForm.dateRange.length === 2) {
      params.startDate = transactionSearchForm.dateRange[0]
      params.endDate = transactionSearchForm.dateRange[1]
    }
    delete params.dateRange
    
    const response = await payApi.getAccountRecords(params)
    
    if (response.code === 200 || response.success) {
      const data = response.data || response
      const records = data.records || data.list || []
      // 映射 hou_pay_consume_record 实体字段 → 展示列
      transactionList.value = records.map((r) => ({
        id: r.id,
        tradeNo: r.consumeRecordId || r.recordPayorder || r.id,
        tradeType: r.tradeTypeId ?? r.detailType ?? 3,
        amount: r.recordMoney ?? 0,
        balance: r.recordAfterMoney ?? r.creditRemain ?? 0,
        tradeTime: r.recordTime || r.recordDate || '',
        remark: r.recordTitle || r.recordDesc || '',
        _raw: r
      }))
      transactionPage.total = data.total || 0
    } else {
      ElMessage.error(response.message || '获取交易记录失败')
      transactionList.value = []
      transactionPage.total = 0
    }
  } catch (error) {
    console.error('获取交易记录失败:', error)
    ElMessage.error('获取交易记录失败: ' + (error.message || '网络错误'))
    transactionList.value = []
    transactionPage.total = 0
  } finally {
    transactionLoading.value = false
  }
}

// 获取账户类型文本
const getAccountTypeText = (type) => {
  return ACCOUNT_TYPE_TEXT[type] || '未知类型'
}

// 获取账户状态文本
const getAccountStatusText = (status) => {
  return ACCOUNT_STATUS_TEXT[status] || '未知状态'
}

// 获取账户状态标签类型
const getAccountStatusTagType = (status) => {
  return ACCOUNT_STATUS_TAG_TYPE[status] || 'info'
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

// 刷新
const handleRefresh = () => {
  fetchAccountInfo()
  fetchTransactionList()
}

// 搜索交易记录
const handleSearchTransactions = () => {
  transactionPage.currentPage = 1
  fetchTransactionList()
}

// 重置交易记录搜索
const handleResetTransactions = () => {
  Object.assign(transactionSearchForm, {
    tradeType: '',
    dateRange: []
  })
  transactionPage.currentPage = 1
  fetchTransactionList()
}

// 导出交易记录
const handleExportTransactions = () => {
  ElMessage.info('导出功能开发中...')
}

// 交易记录分页大小改变
const handleTransactionSizeChange = (val) => {
  transactionPage.pageSize = val
  fetchTransactionList()
}

// 交易记录当前页改变
const handleTransactionCurrentChange = (val) => {
  transactionPage.currentPage = val
  fetchTransactionList()
}
</script>

<style scoped>
.account-management {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.account-overview {
  margin-bottom: 20px;
}

.overview-card {
  height: 120px;
}

.overview-item {
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  height: 100%;
}

.item-label {
  font-size: 14px;
  color: #606266;
  margin-bottom: 10px;
}

.item-value {
  font-size: 20px;
  font-weight: bold;
  color: #303133;
}

.info-card {
  margin-bottom: 20px;
}

.search-form {
  margin-bottom: 20px;
}

.pagination-container {
  margin-top: 20px;
  text-align: right;
}
</style>
