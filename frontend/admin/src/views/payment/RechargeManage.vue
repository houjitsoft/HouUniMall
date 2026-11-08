<template>
  <div class="recharge-management">
    <el-card class="box-card">
      <template #header>
        <div class="card-header">
          <span>充值管理</span>
          <el-button type="primary" @click="handleCreateRecharge">创建充值</el-button>
        </div>
      </template>
      
      <!-- 搜索栏 -->
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="充值单号">
          <el-input v-model="searchForm.rechargeNo" placeholder="请输入充值单号" clearable />
        </el-form-item>
        <el-form-item label="用户ID">
          <el-input v-model="searchForm.userId" placeholder="请输入用户ID" clearable />
        </el-form-item>
        <el-form-item label="充值状态">
          <el-select v-model="searchForm.status" placeholder="请选择状态" clearable>
            <el-option 
              v-for="(text, value) in RECHARGE_STATUS_TEXT" 
              :key="value" 
              :label="text" 
              :value="parseInt(value)" 
            />
          </el-select>
        </el-form-item>
        <el-form-item label="充值时间">
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

      <!-- 充值记录列表 -->
      <el-table :data="rechargeList" style="width: 100%" v-loading="loading">
        <el-table-column prop="rechargeNo" label="充值单号" width="180" />
        <el-table-column prop="userId" label="用户ID" width="120" />
        <el-table-column prop="amount" label="充值金额" width="120">
          <template #default="scope">
            ¥{{ formatAmount(scope.row.amount) }}
          </template>
        </el-table-column>
        <el-table-column prop="channel" label="充值渠道" width="120">
          <template #default="scope">
            {{ getChannelText(scope.row.channel) }}
          </template>
        </el-table-column>
        <el-table-column prop="status" label="充值状态" width="100">
          <template #default="scope">
            <el-tag :type="getStatusTagType(scope.row.status)">
              {{ getStatusText(scope.row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="180">
          <template #default="scope">
            {{ formatDate(scope.row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column prop="payTime" label="支付时间" width="180">
          <template #default="scope">
            {{ formatDate(scope.row.payTime) }}
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

    <!-- 创建充值对话框 -->
    <el-dialog
      v-model="createDialogVisible"
      title="创建充值"
      width="500px"
      :close-on-click-modal="false"
    >
      <el-form :model="rechargeForm" :rules="rechargeRules" ref="rechargeFormRef" label-width="100px">
        <el-form-item label="用户ID" prop="userId">
          <el-input v-model="rechargeForm.userId" placeholder="请输入用户ID" />
        </el-form-item>
        <el-form-item label="充值金额" prop="amount">
          <el-input-number 
            v-model="rechargeForm.amount" 
            :min="0.01" 
            :precision="2" 
            :step="1" 
            controls-position="right" 
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="充值渠道" prop="channel">
          <el-select v-model="rechargeForm.channel" placeholder="请选择充值渠道">
            <el-option 
              v-for="(text, value) in PAY_CHANNEL_TEXT" 
              :key="value" 
              :label="text" 
              :value="parseInt(value)" 
            />
          </el-select>
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="rechargeForm.remark" type="textarea" placeholder="请输入备注" />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="createDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleCreate" :loading="createLoading">确定</el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 充值详情对话框 -->
    <el-dialog
      v-model="detailDialogVisible"
      title="充值详情"
      width="800px"
      :close-on-click-modal="false"
    >
      <div v-if="currentRecharge" class="recharge-detail">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="充值单号">{{ currentRecharge.rechargeNo }}</el-descriptions-item>
          <el-descriptions-item label="用户ID">{{ currentRecharge.userId }}</el-descriptions-item>
          <el-descriptions-item label="充值金额">¥{{ formatAmount(currentRecharge.amount) }}</el-descriptions-item>
          <el-descriptions-item label="充值渠道">{{ getChannelText(currentRecharge.channel) }}</el-descriptions-item>
          <el-descriptions-item label="充值状态">
            <el-tag :type="getStatusTagType(currentRecharge.status)">
              {{ getStatusText(currentRecharge.status) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="创建时间">{{ formatDate(currentRecharge.createTime) }}</el-descriptions-item>
          <el-descriptions-item label="支付时间">{{ formatDate(currentRecharge.payTime) }}</el-descriptions-item>
          <el-descriptions-item label="完成时间">{{ formatDate(currentRecharge.finishTime) }}</el-descriptions-item>
          <el-descriptions-item label="备注" :span="2">{{ currentRecharge.remark }}</el-descriptions-item>
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
const currentRecharge = ref(null)
const rechargeFormRef = ref(null)
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

// 搜索表单
const searchForm = reactive({
  rechargeNo: '',
  userId: '',
  status: '',
  dateRange: [],
  pageNum: 1,
  pageSize: 10
})

// 充值表单
const rechargeForm = reactive({
  userId: '',
  amount: 0,
  channel: 1,
  remark: ''
})

// 充值表单规则
const rechargeRules = {
  userId: [
    { required: true, message: '请输入用户ID', trigger: 'blur' }
  ],
  amount: [
    { required: true, message: '请输入充值金额', trigger: 'blur' }
  ],
  channel: [
    { required: true, message: '请选择充值渠道', trigger: 'change' }
  ]
}

// 充值记录列表
const rechargeList = ref([])

// 充值状态枚举
const RECHARGE_STATUS = {
  PENDING: 1,      // 待支付
  PAID: 2,         // 已支付
  CANCELLED: 3,    // 已取消
  FAILED: 4        // 充值失败
}

// 充值状态文本映射
const RECHARGE_STATUS_TEXT = {
  [RECHARGE_STATUS.PENDING]: '待支付',
  [RECHARGE_STATUS.PAID]: '已支付',
  [RECHARGE_STATUS.CANCELLED]: '已取消',
  [RECHARGE_STATUS.FAILED]: '充值失败'
}

// 充值状态标签类型映射
const RECHARGE_STATUS_TAG_TYPE = {
  [RECHARGE_STATUS.PENDING]: 'warning',
  [RECHARGE_STATUS.PAID]: 'success',
  [RECHARGE_STATUS.CANCELLED]: 'danger',
  [RECHARGE_STATUS.FAILED]: 'danger'
}

// 生命周期
onMounted(() => {
  fetchRechargeList()
})

// 获取充值记录列表
const fetchRechargeList = async () => {
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
    
    const response = await payApi.getRechargeList(params)
    
    if (response.code === 200 || response.success) {
      const data = response.data || response
      const records = data.records || data.list || []
      // 映射 hou_pay_consume_deposit 实体字段 → 展示列
      rechargeList.value = records.map((r) => ({
        id: r.id,
        rechargeNo: r.depositTradeNo || r.rechargeNo,
        userId: r.depositBuyerId || r.userId || '',
        amount: r.depositTotalFee ?? r.amount ?? 0,
        channel: r.depositPayChannel ?? r.channel,
        status: r.depositTradeStatus === true ? 2 : (r.depositTradeStatus === false ? 1 : (r.status ?? 1)),
        createTime: r.depositGmtCreate || r.createTime || '',
        payTime: r.depositGmtPayment || r.payTime || '',
        remark: r.depositOperator || r.remark || '',
        _raw: r
      }))
      total.value = data.total || 0
    } else {
      ElMessage.error(response.message || '获取充值记录失败')
      rechargeList.value = []
      total.value = 0
    }
  } catch (error) {
    console.error('获取充值记录失败:', error)
    ElMessage.error('获取充值记录失败: ' + (error.message || '网络错误'))
    rechargeList.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}

// 获取状态标签类型
const getStatusTagType = (status) => {
  return RECHARGE_STATUS_TAG_TYPE[status] || 'info'
}

// 获取状态文本
const getStatusText = (status) => {
  return RECHARGE_STATUS_TEXT[status] || '未知状态'
}

// 获取渠道文本
const getChannelText = (channel) => {
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
  fetchRechargeList()
}

// 重置搜索
const handleReset = () => {
  Object.assign(searchForm, {
    rechargeNo: '',
    userId: '',
    status: '',
    dateRange: [],
    pageNum: 1,
    pageSize: 10
  })
  currentPage.value = 1
  fetchRechargeList()
}

// 创建充值
const handleCreateRecharge = () => {
  createDialogVisible.value = true
  // 重置表单
  Object.assign(rechargeForm, {
    userId: '',
    amount: 0,
    channel: 1,
    remark: ''
  })
}

// 处理创建
const handleCreate = async () => {
  try {
    await rechargeFormRef.value.validate()
    
    createLoading.value = true
    const response = await payApi.createRecharge(rechargeForm)
    
    if (response.code === 200 || response.success) {
      ElMessage.success('创建充值成功')
      createDialogVisible.value = false
      fetchRechargeList()
    } else {
      ElMessage.error(response.message || '创建充值失败')
    }
  } catch (error) {
    console.error('创建充值失败:', error)
    ElMessage.error('创建充值失败: ' + (error.message || '网络错误'))
  } finally {
    createLoading.value = false
  }
}

// 查看充值详情
const handleView = async (row) => {
  try {
    const response = await payApi.getRechargeDetail(row.id)
    if (response.code === 200 || response.success) {
      let r = response.data || response
      if (Array.isArray(r?.records)) r = r.records[0]
      currentRecharge.value = {
        id: r.id,
        rechargeNo: r.depositTradeNo || r.rechargeNo || row.rechargeNo,
        userId: r.depositBuyerId || r.userId || row.userId,
        amount: r.depositTotalFee ?? r.amount ?? row.amount,
        channel: r.depositPayChannel ?? r.channel ?? row.channel,
        status: r.depositTradeStatus === true ? 2 : (r.depositTradeStatus === false ? 1 : (r.status ?? row.status)),
        createTime: r.depositGmtCreate || r.createTime || row.createTime,
        payTime: r.depositGmtPayment || r.payTime || row.payTime,
        remark: r.depositOperator || r.remark || row.remark,
        tradeNo: r.depositReturnTradeNo || '',
        _raw: r
      }
      detailDialogVisible.value = true
    } else {
      ElMessage.error(response.message || '获取充值详情失败')
    }
  } catch (error) {
    console.error('获取充值详情失败:', error)
    ElMessage.error('获取充值详情失败: ' + (error.message || '网络错误'))
  }
}

// 分页大小改变
const handleSizeChange = (val) => {
  pageSize.value = val
  searchForm.pageSize = val
  fetchRechargeList()
}

// 当前页改变
const handleCurrentChange = (val) => {
  currentPage.value = val
  searchForm.pageNum = val
  fetchRechargeList()
}
</script>

<style scoped>
.recharge-management {
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

.recharge-detail {
  padding: 20px 0;
}

.dialog-footer {
  text-align: right;
}
</style>
