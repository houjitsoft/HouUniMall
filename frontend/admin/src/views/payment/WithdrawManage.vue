<template>
  <div class="withdraw-management">
    <el-card class="box-card">
      <template #header>
        <div class="card-header">
          <span>提现管理</span>
          <el-button type="primary" @click="handleApplyWithdraw">申请提现</el-button>
        </div>
      </template>
      
      <!-- 搜索栏 -->
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="提现单号">
          <el-input v-model="searchForm.withdrawNo" placeholder="请输入提现单号" clearable />
        </el-form-item>
        <el-form-item label="用户ID">
          <el-input v-model="searchForm.userId" placeholder="请输入用户ID" clearable />
        </el-form-item>
        <el-form-item label="提现状态">
          <el-select v-model="searchForm.status" placeholder="请选择状态" clearable>
            <el-option 
              v-for="(text, value) in WITHDRAW_STATUS_TEXT" 
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

      <!-- 提现记录列表 -->
      <el-table :data="withdrawList" style="width: 100%" v-loading="loading">
        <el-table-column prop="withdrawNo" label="提现单号" width="180" />
        <el-table-column prop="userId" label="用户ID" width="120" />
        <el-table-column prop="amount" label="提现金额" width="120">
          <template #default="scope">
            ¥{{ formatAmount(scope.row.amount) }}
          </template>
        </el-table-column>
        <el-table-column prop="fee" label="手续费" width="100">
          <template #default="scope">
            ¥{{ formatAmount(scope.row.fee) }}
          </template>
        </el-table-column>
        <el-table-column prop="actualAmount" label="实际到账" width="120">
          <template #default="scope">
            ¥{{ formatAmount(scope.row.actualAmount) }}
          </template>
        </el-table-column>
        <el-table-column prop="channel" label="提现渠道" width="120">
          <template #default="scope">
            {{ getChannelText(scope.row.channel) }}
          </template>
        </el-table-column>
        <el-table-column prop="status" label="提现状态" width="100">
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

    <!-- 申请提现对话框 -->
    <el-dialog
      v-model="applyDialogVisible"
      title="申请提现"
      width="500px"
      :close-on-click-modal="false"
    >
      <el-form :model="withdrawForm" :rules="withdrawRules" ref="withdrawFormRef" label-width="100px">
        <el-form-item label="用户ID" prop="userId">
          <el-input v-model="withdrawForm.userId" placeholder="请输入用户ID" />
        </el-form-item>
        <el-form-item label="提现金额" prop="amount">
          <el-input-number 
            v-model="withdrawForm.amount" 
            :min="0.01" 
            :precision="2" 
            :step="1" 
            controls-position="right" 
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="提现渠道" prop="channel">
          <el-select v-model="withdrawForm.channel" placeholder="请选择提现渠道">
            <el-option 
              v-for="(text, value) in WITHDRAW_CHANNEL_TEXT" 
              :key="value" 
              :label="text" 
              :value="parseInt(value)" 
            />
          </el-select>
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="withdrawForm.remark" type="textarea" placeholder="请输入备注" />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="applyDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleApply" :loading="applyLoading">确定</el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 提现详情对话框 -->
    <el-dialog
      v-model="detailDialogVisible"
      title="提现详情"
      width="800px"
      :close-on-click-modal="false"
    >
      <div v-if="currentWithdraw" class="withdraw-detail">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="提现单号">{{ currentWithdraw.withdrawNo }}</el-descriptions-item>
          <el-descriptions-item label="用户ID">{{ currentWithdraw.userId }}</el-descriptions-item>
          <el-descriptions-item label="提现金额">¥{{ formatAmount(currentWithdraw.amount) }}</el-descriptions-item>
          <el-descriptions-item label="手续费">¥{{ formatAmount(currentWithdraw.fee) }}</el-descriptions-item>
          <el-descriptions-item label="实际到账">¥{{ formatAmount(currentWithdraw.actualAmount) }}</el-descriptions-item>
          <el-descriptions-item label="提现渠道">{{ getChannelText(currentWithdraw.channel) }}</el-descriptions-item>
          <el-descriptions-item label="提现状态">
            <el-tag :type="getStatusTagType(currentWithdraw.status)">
              {{ getStatusText(currentWithdraw.status) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="申请时间">{{ formatDate(currentWithdraw.createTime) }}</el-descriptions-item>
          <el-descriptions-item label="处理时间">{{ formatDate(currentWithdraw.processTime) }}</el-descriptions-item>
          <el-descriptions-item label="完成时间">{{ formatDate(currentWithdraw.finishTime) }}</el-descriptions-item>
          <el-descriptions-item label="备注" :span="2">{{ currentWithdraw.remark }}</el-descriptions-item>
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
import { payApi } from '../../api/payment/pay'

// 响应式数据
const loading = ref(false)
const applyLoading = ref(false)
const applyDialogVisible = ref(false)
const detailDialogVisible = ref(false)
const currentWithdraw = ref(null)
const withdrawFormRef = ref(null)
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

// 搜索表单
const searchForm = reactive({
  withdrawNo: '',
  userId: '',
  status: '',
  dateRange: [],
  pageNum: 1,
  pageSize: 10
})

// 提现表单
const withdrawForm = reactive({
  userId: '',
  amount: 0,
  channel: 1,
  remark: ''
})

// 提现表单规则
const withdrawRules = {
  userId: [
    { required: true, message: '请输入用户ID', trigger: 'blur' }
  ],
  amount: [
    { required: true, message: '请输入提现金额', trigger: 'blur' }
  ],
  channel: [
    { required: true, message: '请选择提现渠道', trigger: 'change' }
  ]
}

// 提现记录列表
const withdrawList = ref([])

// 提现状态枚举
const WITHDRAW_STATUS = {
  PENDING: 1,      // 待审核
  APPROVED: 2,     // 已批准
  PROCESSING: 3,   // 处理中
  SUCCESS: 4,      // 提现成功
  FAILED: 5,       // 提现失败
  REJECTED: 6      // 已拒绝
}

// 后台 isSucceed 原始值映射到前端枚举
// 0待审核 / 2打款完成 / 3通过 / 4不通过
const mapWithdrawStatus = (raw) => {
  const s = String(raw == null ? '' : raw)
  const map = {
    '0': 1, // 待审核
    '2': 4, // 打款完成 → 提现成功
    '3': 3, // 通过 → 处理中
    '4': 5, // 不通过 → 提现失败
    '1': 3
  }
  return map[s] ?? map[raw] ?? 1
}

// 提现状态文本映射
const WITHDRAW_STATUS_TEXT = {
  [WITHDRAW_STATUS.PENDING]: '待审核',
  [WITHDRAW_STATUS.APPROVED]: '已批准',
  [WITHDRAW_STATUS.PROCESSING]: '处理中',
  [WITHDRAW_STATUS.SUCCESS]: '提现成功',
  [WITHDRAW_STATUS.FAILED]: '提现失败',
  [WITHDRAW_STATUS.REJECTED]: '已拒绝'
}

// 提现状态标签类型映射
const WITHDRAW_STATUS_TAG_TYPE = {
  [WITHDRAW_STATUS.PENDING]: 'warning',
  [WITHDRAW_STATUS.APPROVED]: 'primary',
  [WITHDRAW_STATUS.PROCESSING]: 'info',
  [WITHDRAW_STATUS.SUCCESS]: 'success',
  [WITHDRAW_STATUS.FAILED]: 'danger',
  [WITHDRAW_STATUS.REJECTED]: 'danger'
}

// 提现渠道枚举
const WITHDRAW_CHANNEL = {
  BANK: 1,         // 银行卡
  WECHAT: 2,       // 微信
  ALIPAY: 3        // 支付宝
}

// 提现渠道文本映射
const WITHDRAW_CHANNEL_TEXT = {
  [WITHDRAW_CHANNEL.BANK]: '银行卡',
  [WITHDRAW_CHANNEL.WECHAT]: '微信',
  [WITHDRAW_CHANNEL.ALIPAY]: '支付宝'
}

// 生命周期
onMounted(() => {
  fetchWithdrawList()
})

// 获取提现记录列表
const fetchWithdrawList = async () => {
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
    
    const response = await payApi.getWithdrawList(params)
    
    if (response.code === 200 || response.success) {
      const data = response.data || response
      const records = data.records || data.list || []
      // 映射 hou_pay_consume_withdraw 实体字段 → 展示列
      withdrawList.value = records.map((r) => ({
        id: r.id,
        withdrawNo: r.orderid || r.withdrawNo || r.id,
        userId: r.payUid || r.userId || '',
        amount: r.amount ?? 0,
        fee: r.fee ?? 0,
        actualAmount: r.payAmount ?? r.amount ?? 0,
        channel: r.bank || r.withdrawType || '',
        status: mapWithdrawStatus(r.isSucceed),
        createTime: r.addTime || r.createTime || '',
        processTime: r.checkTime || r.processTime || '',
        bankName: r.bank || '',
        bankCard: r.cardno || '',
        cardName: r.cardname || '',
        remark: r.con || r.remark || '',
        _raw: r
      }))
      total.value = data.total || 0
    } else {
      ElMessage.error(response.message || '获取提现记录失败')
      withdrawList.value = []
      total.value = 0
    }
  } catch (error) {
    console.error('获取提现记录失败:', error)
    ElMessage.error('获取提现记录失败: ' + (error.message || '网络错误'))
    withdrawList.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}

// 获取状态标签类型
const getStatusTagType = (status) => {
  return WITHDRAW_STATUS_TAG_TYPE[status] || 'info'
}

// 获取状态文本
const getStatusText = (status) => {
  return WITHDRAW_STATUS_TEXT[status] || '未知状态'
}

// 获取渠道文本
const getChannelText = (channel) => {
  return WITHDRAW_CHANNEL_TEXT[channel] || '未知渠道'
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
  fetchWithdrawList()
}

// 重置搜索
const handleReset = () => {
  Object.assign(searchForm, {
    withdrawNo: '',
    userId: '',
    status: '',
    dateRange: [],
    pageNum: 1,
    pageSize: 10
  })
  currentPage.value = 1
  fetchWithdrawList()
}

// 申请提现
const handleApplyWithdraw = () => {
  applyDialogVisible.value = true
  // 重置表单
  Object.assign(withdrawForm, {
    userId: '',
    amount: 0,
    channel: 1,
    remark: ''
  })
}

// 处理申请
const handleApply = async () => {
  try {
    await withdrawFormRef.value.validate()
    
    applyLoading.value = true
    const response = await payApi.applyWithdraw(withdrawForm)
    
    if (response.code === 200 || response.success) {
      ElMessage.success('申请提现成功')
      applyDialogVisible.value = false
      fetchWithdrawList()
    } else {
      ElMessage.error(response.message || '申请提现失败')
    }
  } catch (error) {
    console.error('申请提现失败:', error)
    ElMessage.error('申请提现失败: ' + (error.message || '网络错误'))
  } finally {
    applyLoading.value = false
  }
}

// 查看提现详情
const handleView = async (row) => {
  try {
    const response = await payApi.getWithdrawDetail(row.id)
    if (response.code === 200 || response.success) {
      let r = response.data || response
      if (Array.isArray(r?.records)) r = r.records[0]
      currentWithdraw.value = {
        id: r.id,
        withdrawNo: r.orderid || r.withdrawNo || row.withdrawNo,
        userId: r.payUid || r.userId || row.userId,
        amount: r.amount ?? row.amount,
        fee: r.fee ?? row.fee,
        actualAmount: r.payAmount ?? r.amount ?? row.actualAmount,
        channel: r.bank || r.withdrawType || row.channel,
        status: mapWithdrawStatus(r.isSucceed ?? row.isSucceed) || row.status,
        createTime: r.addTime || r.createTime || row.createTime,
        processTime: r.checkTime || row.processTime,
        finishTime: r.checkTime || row.finishTime || '',
        bankName: r.bank || row.bankName || '',
        bankCard: r.cardno || row.bankCard || '',
        cardName: r.cardname || '',
        remark: r.con || r.remark || row.remark,
        _raw: r
      }
      detailDialogVisible.value = true
    } else {
      ElMessage.error(response.message || '获取提现详情失败')
    }
  } catch (error) {
    console.error('获取提现详情失败:', error)
    ElMessage.error('获取提现详情失败: ' + (error.message || '网络错误'))
  }
}

// 分页大小改变
const handleSizeChange = (val) => {
  pageSize.value = val
  searchForm.pageSize = val
  fetchWithdrawList()
}

// 当前页改变
const handleCurrentChange = (val) => {
  currentPage.value = val
  searchForm.pageNum = val
  fetchWithdrawList()
}
</script>

<style scoped>
.withdraw-management {
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

.withdraw-detail {
  padding: 20px 0;
}

.dialog-footer {
  text-align: right;
}
</style>
