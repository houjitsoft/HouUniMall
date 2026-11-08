<template>
  <div class="payment-refund-container">
    <!-- 统计卡片 -->
    <el-row :gutter="20" class="stats-cards">
      <el-col :span="6">
        <el-card class="stat-card total-refund">
          <div class="stat-content">
            <div class="stat-icon"><el-icon><RefreshLeft /></el-icon></div>
            <div class="stat-info">
              <div class="stat-value">{{ refundStats.total }}</div>
              <div class="stat-label">退款总数</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card pending-refund">
          <div class="stat-content">
            <div class="stat-icon"><el-icon><Clock /></el-icon></div>
            <div class="stat-info">
              <div class="stat-value">{{ refundStats.pending }}</div>
              <div class="stat-label">待处理</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card success-refund">
          <div class="stat-content">
            <div class="stat-icon"><el-icon><CircleCheck /></el-icon></div>
            <div class="stat-info">
              <div class="stat-value">{{ refundStats.success }}</div>
              <div class="stat-label">成功退款</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card amount-refund">
          <div class="stat-content">
            <div class="stat-icon"><el-icon><Money /></el-icon></div>
            <div class="stat-info">
              <div class="stat-value">¥{{ refundStats.amount }}</div>
              <div class="stat-label">退款总额</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 搜索筛选 -->
    <el-card class="search-card">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="退款单号">
          <el-input v-model="searchForm.refundNo" placeholder="请输入退款单号" clearable />
        </el-form-item>
        <el-form-item label="原订单号">
          <el-input v-model="searchForm.orderNo" placeholder="请输入原订单号" clearable />
        </el-form-item>
        <el-form-item label="退款类型">
          <el-select v-model="searchForm.refundType" placeholder="请选择退款类型" clearable>
            <el-option label="全额退款" value="full" />
            <el-option label="部分退款" value="partial" />
            <el-option label="手续费退款" value="fee" />
            <el-option label="异常退款" value="exception" />
          </el-select>
        </el-form-item>
        <el-form-item label="退款状态">
          <el-select v-model="searchForm.status" placeholder="请选择状态" clearable>
            <el-option label="待审核" value="pending" />
            <el-option label="审核通过" value="approved" />
            <el-option label="退款中" value="processing" />
            <el-option label="已退款" value="success" />
            <el-option label="退款失败" value="failed" />
            <el-option label="已拒绝" value="rejected" />
          </el-select>
        </el-form-item>
        <el-form-item label="退款原因">
          <el-select v-model="searchForm.reason" placeholder="请选择退款原因" clearable>
            <el-option label="用户申请" value="user_request" />
            <el-option label="商品问题" value="product_issue" />
            <el-option label="服务问题" value="service_issue" />
            <el-option label="系统异常" value="system_error" />
          </el-select>
        </el-form-item>
        <el-form-item label="申请时间">
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
          <el-button type="primary" @click="handleBatchApprove">
            <el-icon><Check /></el-icon>批量审核
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
        :data="refundList"
        v-loading="loading"
        style="width: 100%"
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="55" />
        <el-table-column prop="refundNo" label="退款单号" width="180" />
        <el-table-column prop="orderNo" label="原订单号" width="180" />
        <el-table-column prop="refundType" label="退款类型" width="100">
          <template #default="{ row }">
            <el-tag :type="getRefundTypeColor(row.refundType)">
              {{ getRefundTypeText(row.refundType) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="refundAmount" label="退款金额" width="120">
          <template #default="{ row }">
            <span class="refund-amount">¥{{ row.refundAmount }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="originalAmount" label="原交易金额" width="120">
          <template #default="{ row }">
            ¥{{ row.originalAmount }}
          </template>
        </el-table-column>
        <el-table-column prop="status" label="退款状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="reason" label="退款原因" width="120">
          <template #default="{ row }">
            {{ getReasonText(row.reason) }}
          </template>
        </el-table-column>
        <el-table-column prop="applicant" label="申请人" width="120" />
        <el-table-column prop="createTime" label="申请时间" width="180" />
        <el-table-column prop="processTime" label="处理时间" width="180" />
        <el-table-column label="操作" width="250" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="handleView(row)">
              <el-icon><View /></el-icon>查看
            </el-button>
            <el-button 
              v-if="row.status === 'pending'"
              link 
              type="success" 
              @click="handleApprove(row)"
            >
              <el-icon><Check /></el-icon>审核
            </el-button>
            <el-button 
              v-if="row.status === 'approved'"
              link 
              type="warning" 
              @click="handleProcess(row)"
            >
              <el-icon><Operation /></el-icon>处理
            </el-button>
            <el-button 
              v-if="row.status === 'pending'"
              link 
              type="danger" 
              @click="handleReject(row)"
            >
              <el-icon><Close /></el-icon>拒绝
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
    <el-dialog v-model="dialogVisible" title="退款详情" width="900px">
      <div v-if="currentRefund" class="refund-detail">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-descriptions title="退款信息" :column="1" border>
              <el-descriptions-item label="退款单号">{{ currentRefund.refundNo }}</el-descriptions-item>
              <el-descriptions-item label="原订单号">{{ currentRefund.orderNo }}</el-descriptions-item>
              <el-descriptions-item label="退款类型">
                <el-tag :type="getRefundTypeColor(currentRefund.refundType)">
                  {{ getRefundTypeText(currentRefund.refundType) }}
                </el-tag>
              </el-descriptions-item>
              <el-descriptions-item label="退款金额">
                <span class="refund-amount">¥{{ currentRefund.refundAmount }}</span>
              </el-descriptions-item>
              <el-descriptions-item label="原交易金额">¥{{ currentRefund.originalAmount }}</el-descriptions-item>
              <el-descriptions-item label="退款状态">
                <el-tag :type="getStatusType(currentRefund.status)">
                  {{ getStatusText(currentRefund.status) }}
                </el-tag>
              </el-descriptions-item>
            </el-descriptions>
          </el-col>
          <el-col :span="12">
            <el-descriptions title="申请信息" :column="1" border>
              <el-descriptions-item label="退款原因">{{ getReasonText(currentRefund.reason) }}</el-descriptions-item>
              <el-descriptions-item label="申请人">{{ currentRefund.applicant }}</el-descriptions-item>
              <el-descriptions-item label="申请时间">{{ currentRefund.createTime }}</el-descriptions-item>
              <el-descriptions-item label="处理时间">{{ currentRefund.processTime }}</el-descriptions-item>
              <el-descriptions-item label="处理人">{{ currentRefund.processor }}</el-descriptions-item>
              <el-descriptions-item label="退款流水号">{{ currentRefund.refundTransactionId }}</el-descriptions-item>
            </el-descriptions>
          </el-col>
        </el-row>
        <div class="refund-detail-remark" v-if="currentRefund.remark">
          <h4>申请说明</h4>
          <p>{{ currentRefund.remark }}</p>
        </div>
        <div class="refund-detail-process" v-if="currentRefund.processRemark">
          <h4>处理说明</h4>
          <p>{{ currentRefund.processRemark }}</p>
        </div>
        <div class="refund-timeline">
          <h4>退款进度</h4>
          <el-timeline>
            <el-timeline-item
              v-for="item in currentRefund.timeline"
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

    <!-- 审核对话框 -->
    <el-dialog v-model="approveDialogVisible" title="退款审核" width="500px">
      <el-form :model="approveForm" label-width="80px">
        <el-form-item label="审核结果">
          <el-radio-group v-model="approveForm.result">
            <el-radio label="approved">审核通过</el-radio>
            <el-radio label="rejected">拒绝退款</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="审核说明">
          <el-input
            v-model="approveForm.remark"
            type="textarea"
            :rows="3"
            placeholder="请输入审核说明"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="approveDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmApprove">确认</el-button>
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
  RefreshLeft, Clock, CircleCheck, Money, Search, Refresh, Check, Operation,
  Download, View, Close
} from '@element-plus/icons-vue'
import { payApi } from '@/api/payment/pay'

// 响应式数据
const loading = ref(false)
const dialogVisible = ref(false)
const approveDialogVisible = ref(false)
const refundList = ref([])
const selectedRefunds = ref([])
const currentRefund = ref(null)

// 统计数据
const refundStats = ref({
  total: 0,
  pending: 0,
  success: 0,
  amount: '0.00'
})

// 搜索表单
const searchForm = reactive({
  refundNo: '',
  orderNo: '',
  refundType: '',
  status: '',
  reason: '',
  dateRange: null
})

// 审核表单
const approveForm = reactive({
  result: 'approved',
  remark: ''
})

// 分页信息
const pagination = reactive({
  page: 1,
  size: 10,
  total: 0
})

// 退款类型映射
const getRefundTypeText = (type) => {
  const map = {
    'full': '全额退款',
    'partial': '部分退款',
    'fee': '手续费退款',
    'exception': '异常退款'
  }
  return map[type] || type
}

const getRefundTypeColor = (type) => {
  const map = {
    'full': 'primary',
    'partial': 'warning',
    'fee': 'info',
    'exception': 'danger'
  }
  return map[type] || 'info'
}

// 状态映射（对齐后台退款单状态 PROCESSING/SUCCESS/FAILED）
const getStatusText = (status) => {
  const map = {
    'pending': '待审核',
    'approved': '审核通过',
    'processing': '退款中',
    'success': '已退款',
    'failed': '退款失败',
    'rejected': '已拒绝',
    'PROCESSING': '退款中',
    'SUCCESS': '已退款',
    'FAILED': '退款失败'
  }
  return map[status] || map[String(status)] || status
}

const getStatusType = (status) => {
  const map = {
    'pending': 'warning',
    'approved': 'primary',
    'processing': 'info',
    'success': 'success',
    'failed': 'danger',
    'rejected': 'danger',
    'PROCESSING': 'info',
    'SUCCESS': 'success',
    'FAILED': 'danger'
  }
  return map[status] || map[String(status)] || 'info'
}

// 退款原因映射
const getReasonText = (reason) => {
  const map = {
    'user_request': '用户申请',
    'product_issue': '商品问题',
    'service_issue': '服务问题',
    'system_error': '系统异常'
  }
  return map[reason] || reason
}

// 获取退款列表（透传退款单分页，走 hou_pay_refund_order 真实数据）
const getRefundList = async () => {
  loading.value = true
  try {
    const params = {
      pageNum: pagination.page,
      pageSize: pagination.size,
      refundNo: searchForm.refundNo || undefined,
      mallOrderNo: searchForm.orderNo || undefined,
      status: searchForm.status ? searchForm.status.toUpperCase() : undefined
    }
    const response = await payApi.getRefundList(params)
    if (response.code === 200) {
      const data = response.data || {}
      const records = data.records || data.list || []
      refundList.value = records.map((r) => ({
        id: r.id,
        refundNo: r.refundNo,
        orderNo: r.mallOrderNo || r.cashierNo,
        cashierNo: r.cashierNo,
        refundType: 'full',
        refundAmount: r.amount ?? 0,
        originalAmount: r.amount ?? 0,
        status: r.status || 'PROCESSING',
        reason: r.reason || '',
        applicant: r.cashierNo || '',
        createTime: r.createTime,
        processTime: r.successTime || '',
        processor: '',
        refundTransactionId: r.channelRefundNo || '',
        remark: r.reason || '',
        processRemark: r.failureReason || '',
        timeline: [
          { time: r.createTime, content: '退款申请提交', type: 'primary' }
        ]
      }))
      pagination.total = data.total || data.totalCount || 0
      // 汇总统计
      let pendingCount = 0
      let successCount = 0
      let amountSum = 0
      records.forEach((r) => {
        if (r.status === 'SUCCESS') successCount += 1
        if (r.status === 'PROCESSING') pendingCount += 1
        amountSum += Number(r.amount || 0)
      })
      refundStats.value = {
        total: data.total || data.totalCount || records.length,
        pending: pendingCount,
        success: successCount,
        amount: amountSum.toLocaleString('zh-CN', { minimumFractionDigits: 2 })
      }
    } else {
      refundList.value = []
      pagination.total = 0
    }
  } catch (error) {
    console.error('获取退款列表失败:', error)
    ElMessage.error('获取退款列表失败: ' + (error.message || '网络错误'))
    refundList.value = []
    pagination.total = 0
  } finally {
    loading.value = false
  }
}

// 搜索
const handleSearch = () => {
  pagination.page = 1
  getRefundList()
}

// 重置
const handleReset = () => {
  Object.assign(searchForm, {
    refundNo: '',
    orderNo: '',
    refundType: '',
    status: '',
    reason: '',
    dateRange: null
  })
  handleSearch()
}

// 刷新
const handleRefresh = () => {
  getRefundList()
}

// 批量审核
const handleBatchApprove = () => {
  if (selectedRefunds.value.length === 0) {
    ElMessage.warning('请选择要审核的退款申请')
    return
  }
  ElMessage.info('批量审核功能开发中')
}

// 批量处理
const handleBatchProcess = () => {
  if (selectedRefunds.value.length === 0) {
    ElMessage.warning('请选择要处理的退款申请')
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
  currentRefund.value = row
  dialogVisible.value = true
}

// 审核
const handleApprove = (row) => {
  currentRefund.value = row
  approveForm.result = 'approved'
  approveForm.remark = ''
  approveDialogVisible.value = true
}

// 处理
const handleProcess = (row) => {
  ElMessage.info('退款处理功能开发中')
}

// 拒绝
const handleReject = (row) => {
  ElMessageBox.confirm('确定要拒绝这个退款申请吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    ElMessage.success('退款申请已拒绝')
    getRefundList()
  }).catch(() => {
    ElMessage.info('已取消操作')
  })
}

// 确认审核
const confirmApprove = () => {
  if (!approveForm.remark.trim()) {
    ElMessage.warning('请输入审核说明')
    return
  }
  
  ElMessage.success('审核完成')
  approveDialogVisible.value = false
  getRefundList()
}

// 选择变化
const handleSelectionChange = (selection) => {
  selectedRefunds.value = selection
}

// 分页大小变化
const handleSizeChange = (size) => {
  pagination.size = size
  getRefundList()
}

// 页码变化
const handleCurrentChange = (page) => {
  pagination.page = page
  getRefundList()
}

// 组件挂载
onMounted(() => {
  getRefundList()
})
</script>

<style scoped>
.payment-refund-container {
  padding: 20px;
}

.stats-cards {
  margin-bottom: 20px;
}

.stat-card {
  border-left: 4px solid #409eff;
}

.stat-card.total-refund {
  border-left-color: #409eff;
}

.stat-card.pending-refund {
  border-left-color: #e6a23c;
}

.stat-card.success-refund {
  border-left-color: #67c23a;
}

.stat-card.amount-refund {
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

.refund-amount {
  color: #f56c6c;
  font-weight: bold;
}

.refund-detail-remark,
.refund-detail-process {
  margin-top: 20px;
}

.refund-detail-remark h4,
.refund-detail-process h4 {
  margin-bottom: 10px;
  color: #303133;
}

.refund-detail-remark p,
.refund-detail-process p {
  color: #606266;
  line-height: 1.5;
}

.refund-timeline {
  margin-top: 20px;
}

.refund-timeline h4 {
  margin-bottom: 15px;
  color: #303133;
}
</style>
