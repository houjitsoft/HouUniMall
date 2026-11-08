<template>
  <div class="exception-order-container">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-left">
        <h2>异常订单</h2>
        <span class="subtitle">管理和处理异常状态的订单</span>
      </div>
      <div class="header-right">
        <el-button type="primary" icon="Refresh" @click="handleRefresh">
          刷新数据
        </el-button>
        <el-button icon="Download" @click="handleExport">
          导出异常订单
        </el-button>
      </div>
    </div>

    <!-- 统计卡片 -->
    <div class="stats-cards">
      <el-row :gutter="16">
        <el-col :span="6">
          <el-card class="stat-card payment-timeout">
            <div class="stat-content">
              <div class="stat-value">{{ stats.paymentTimeout }}</div>
              <div class="stat-label">支付超时</div>
            </div>
            <el-icon class="stat-icon"><Clock /></el-icon>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card class="stat-card address-error">
            <div class="stat-content">
              <div class="stat-value">{{ stats.addressError }}</div>
              <div class="stat-label">地址异常</div>
            </div>
            <el-icon class="stat-icon"><Location /></el-icon>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card class="stat-card stock-shortage">
            <div class="stat-content">
              <div class="stat-value">{{ stats.stockShortage }}</div>
              <div class="stat-label">库存不足</div>
            </div>
            <el-icon class="stat-icon"><WarnTriangleFilled /></el-icon>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card class="stat-card system-error">
            <div class="stat-content">
              <div class="stat-value">{{ stats.systemError }}</div>
              <div class="stat-label">系统异常</div>
            </div>
            <el-icon class="stat-icon"><CircleCloseFilled /></el-icon>
          </el-card>
        </el-col>
      </el-row>
    </div>

    <!-- 搜索筛选区域 -->
    <el-card class="search-card">
      <el-form :model="searchForm" inline>
        <el-form-item label="订单号">
          <el-input 
            v-model="searchForm.orderNo" 
            placeholder="请输入订单号"
            clearable
            style="width: 200px"
          />
        </el-form-item>
        <el-form-item label="异常类型">
          <el-select 
            v-model="searchForm.exceptionType" 
            placeholder="请选择异常类型"
            clearable
            style="width: 180px"
          >
            <el-option label="全部" value="" />
            <el-option label="支付超时" value="payment_timeout" />
            <el-option label="地址异常" value="address_error" />
            <el-option label="库存不足" value="stock_shortage" />
            <el-option label="价格异常" value="price_error" />
            <el-option label="系统异常" value="system_error" />
            <el-option label="其他异常" value="other" />
          </el-select>
        </el-form-item>
        <el-form-item label="处理状态">
          <el-select 
            v-model="searchForm.handleStatus" 
            placeholder="请选择处理状态"
            clearable
            style="width: 150px"
          >
            <el-option label="全部" value="" />
            <el-option label="待处理" value="pending" />
            <el-option label="处理中" value="processing" />
            <el-option label="已处理" value="completed" />
            <el-option label="已忽略" value="ignored" />
          </el-select>
        </el-form-item>
        <el-form-item label="异常时间">
          <el-date-picker
            v-model="searchForm.dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            style="width: 280px"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" icon="Search" @click="handleSearch">
            查询
          </el-button>
          <el-button icon="RefreshLeft" @click="handleReset">
            重置
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 数据列表 -->
    <el-card class="table-card">
      <el-table 
        :data="tableData" 
        v-loading="loading"
        stripe
        style="width: 100%"
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="55" />
        <el-table-column prop="orderNo" label="订单号" width="180" fixed="left">
          <template #default="{ row }">
            <el-link type="primary" @click="handleViewDetail(row)">
              {{ row.orderNo }}
            </el-link>
          </template>
        </el-table-column>
        <el-table-column prop="exceptionType" label="异常类型" width="120">
          <template #default="{ row }">
            <el-tag :type="getExceptionTypeTag(row.exceptionType)">
              {{ getExceptionTypeName(row.exceptionType) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="exceptionDesc" label="异常描述" min-width="200" show-overflow-tooltip />
        <el-table-column prop="customerName" label="客户" width="120" />
        <el-table-column prop="orderAmount" label="订单金额" width="120">
          <template #default="{ row }">
            <span class="amount">¥{{ row.orderAmount }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="exceptionTime" label="异常时间" width="160" />
        <el-table-column prop="handleStatus" label="处理状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getHandleStatusTag(row.handleStatus)" size="small">
              {{ getHandleStatusName(row.handleStatus) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="handler" label="处理人" width="100" />
        <el-table-column label="操作" width="240" fixed="right">
          <template #default="{ row }">
            <el-button 
              v-if="row.handleStatus === 'pending'"
              type="primary" 
              link 
              size="small"
              @click="handleProcess(row)"
            >
              处理
            </el-button>
            <el-button 
              type="success" 
              link 
              size="small"
              @click="handleResolve(row)"
            >
              标记已解决
            </el-button>
            <el-button 
              type="warning" 
              link 
              size="small"
              @click="handleIgnore(row)"
            >
              忽略
            </el-button>
            <el-button 
              type="info" 
              link 
              size="small"
              @click="handleViewLog(row)"
            >
              查看日志
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
          @current-change="handlePageChange"
        />
      </div>
    </el-card>

    <!-- 处理异常对话框 -->
    <el-dialog 
      v-model="processDialogVisible" 
      title="处理异常订单"
      width="600px"
    >
      <el-form :model="processForm" label-width="100px">
        <el-form-item label="订单号">
          <el-input v-model="processForm.orderNo" disabled />
        </el-form-item>
        <el-form-item label="异常类型">
          <el-input v-model="processForm.exceptionType" disabled />
        </el-form-item>
        <el-form-item label="处理方案" required>
          <el-select v-model="processForm.solution" placeholder="请选择处理方案" style="width: 100%">
            <el-option label="重新发起支付" value="retry_payment" />
            <el-option label="修改收货地址" value="update_address" />
            <el-option label="补充库存后发货" value="restock" />
            <el-option label="退款" value="refund" />
            <el-option label="取消订单" value="cancel" />
            <el-option label="人工介入" value="manual" />
          </el-select>
        </el-form-item>
        <el-form-item label="处理备注" required>
          <el-input 
            v-model="processForm.remark" 
            type="textarea" 
            :rows="4"
            placeholder="请输入处理备注信息"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="processDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmitProcess" :loading="submitLoading">
          提交处理
        </el-button>
      </template>
    </el-dialog>

    <!-- 异常日志对话框 -->
    <el-dialog 
      v-model="logDialogVisible" 
      title="异常日志"
      width="800px"
    >
      <el-timeline>
        <el-timeline-item 
          v-for="(log, index) in exceptionLogs" 
          :key="index"
          :timestamp="log.createTime"
          placement="top"
        >
          <el-card>
            <h4>{{ log.action }}</h4>
            <p>{{ log.content }}</p>
            <p class="log-meta">操作人：{{ log.operator }}</p>
          </el-card>
        </el-timeline-item>
      </el-timeline>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { 
  Clock, 
  Location, 
  WarnTriangleFilled, 
  CircleCloseFilled,
  Search,
  RefreshLeft,
  Refresh,
  Download
} from '@element-plus/icons-vue'
import request from '@/utils/request'

// 统计数据
const stats = reactive({
  paymentTimeout: 0,
  addressError: 0,
  stockShortage: 0,
  systemError: 0
})

// 搜索表单
const searchForm = reactive({
  orderNo: '',
  exceptionType: '',
  handleStatus: '',
  dateRange: []
})

// 表格数据
const tableData = ref([])
const loading = ref(false)
const selectedRows = ref([])

// 分页
const pagination = reactive({
  page: 1,
  size: 20,
  total: 0
})

// 处理对话框
const processDialogVisible = ref(false)
const processForm = reactive({
  orderId: '',
  orderNo: '',
  exceptionType: '',
  solution: '',
  remark: ''
})
const submitLoading = ref(false)

// 日志对话框
const logDialogVisible = ref(false)
const exceptionLogs = ref([])

// 获取异常订单列表
const getExceptionOrders = async () => {
  loading.value = true
  try {
    const params = {
      page: pagination.page,
      size: pagination.size,
      orderNo: searchForm.orderNo,
      exceptionType: searchForm.exceptionType,
      handleStatus: searchForm.handleStatus,
      startDate: searchForm.dateRange?.[0] || '',
      endDate: searchForm.dateRange?.[1] || ''
    }
    
    const response = await request.post('/web/order/exception/list', params)
    
    if (response.code === 200) {
      tableData.value = response.data.list || []
      pagination.total = response.data.total || 0
    }
  } catch (error) {
    console.error('获取异常订单列表失败:', error)
    ElMessage.error('获取异常订单列表失败')
  } finally {
    loading.value = false
  }
}

// 获取统计数据
const getStats = async () => {
  try {
    const response = await request.post('/web/order/exception/stats')
    if (response.code === 200) {
      Object.assign(stats, response.data)
    }
  } catch (error) {
    console.error('获取统计数据失败:', error)
  }
}

// 异常类型名称
const getExceptionTypeName = (type) => {
  const typeMap = {
    'payment_timeout': '支付超时',
    'address_error': '地址异常',
    'stock_shortage': '库存不足',
    'price_error': '价格异常',
    'system_error': '系统异常',
    'other': '其他异常'
  }
  return typeMap[type] || type
}

// 异常类型标签样式
const getExceptionTypeTag = (type) => {
  const tagMap = {
    'payment_timeout': 'warning',
    'address_error': 'danger',
    'stock_shortage': 'warning',
    'price_error': 'danger',
    'system_error': 'danger',
    'other': 'info'
  }
  return tagMap[type] || 'info'
}

// 处理状态名称
const getHandleStatusName = (status) => {
  const statusMap = {
    'pending': '待处理',
    'processing': '处理中',
    'completed': '已处理',
    'ignored': '已忽略'
  }
  return statusMap[status] || status
}

// 处理状态标签样式
const getHandleStatusTag = (status) => {
  const tagMap = {
    'pending': 'danger',
    'processing': 'warning',
    'completed': 'success',
    'ignored': 'info'
  }
  return tagMap[status] || 'info'
}

// 查询
const handleSearch = () => {
  pagination.page = 1
  getExceptionOrders()
}

// 重置
const handleReset = () => {
  Object.assign(searchForm, {
    orderNo: '',
    exceptionType: '',
    handleStatus: '',
    dateRange: []
  })
  handleSearch()
}

// 刷新
const handleRefresh = () => {
  getExceptionOrders()
  getStats()
  ElMessage.success('刷新成功')
}

// 导出
const handleExport = () => {
  ElMessage.info('导出功能开发中...')
}

// 选择变化
const handleSelectionChange = (rows) => {
  selectedRows.value = rows
}

// 分页
const handlePageChange = (page) => {
  pagination.page = page
  getExceptionOrders()
}

const handleSizeChange = (size) => {
  pagination.size = size
  pagination.page = 1
  getExceptionOrders()
}

// 查看详情
const handleViewDetail = (row) => {
  ElMessage.info('订单详情：' + row.orderNo)
}

// 处理异常
const handleProcess = (row) => {
  processForm.orderId = row.id
  processForm.orderNo = row.orderNo
  processForm.exceptionType = getExceptionTypeName(row.exceptionType)
  processForm.solution = ''
  processForm.remark = ''
  processDialogVisible.value = true
}

// 提交处理
const handleSubmitProcess = async () => {
  if (!processForm.solution) {
    ElMessage.warning('请选择处理方案')
    return
  }
  if (!processForm.remark) {
    ElMessage.warning('请输入处理备注')
    return
  }
  
  submitLoading.value = true
  try {
    const response = await request.post('/web/order/exception/process', {
      orderId: processForm.orderId,
      solution: processForm.solution,
      remark: processForm.remark
    })
    
    if (response.code === 200) {
      ElMessage.success('处理成功')
      processDialogVisible.value = false
      getExceptionOrders()
      getStats()
    } else {
      ElMessage.error(response.message || '处理失败')
    }
  } catch (error) {
    console.error('处理异常订单失败:', error)
    ElMessage.error('处理失败')
  } finally {
    submitLoading.value = false
  }
}

// 标记已解决
const handleResolve = async (row) => {
  try {
    await ElMessageBox.confirm('确认标记该订单异常已解决吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    const response = await request.post('/web/order/exception/resolve', {
      orderId: row.id
    })
    
    if (response.code === 200) {
      ElMessage.success('标记成功')
      getExceptionOrders()
      getStats()
    } else {
      ElMessage.error(response.message || '标记失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('标记已解决失败:', error)
      ElMessage.error('标记失败')
    }
  }
}

// 忽略
const handleIgnore = async (row) => {
  try {
    await ElMessageBox.confirm('确认忽略该异常订单吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    const response = await request.post('/web/order/exception/ignore', {
      orderId: row.id
    })
    
    if (response.code === 200) {
      ElMessage.success('操作成功')
      getExceptionOrders()
      getStats()
    } else {
      ElMessage.error(response.message || '操作失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('忽略异常失败:', error)
      ElMessage.error('操作失败')
    }
  }
}

// 查看日志
const handleViewLog = async (row) => {
  try {
    const response = await request.post('/web/order/exception/logs', {
      orderId: row.id
    })
    
    if (response.code === 200) {
      exceptionLogs.value = response.data || []
      logDialogVisible.value = true
    } else {
      ElMessage.error(response.message || '获取日志失败')
    }
  } catch (error) {
    console.error('获取异常日志失败:', error)
    ElMessage.error('获取日志失败')
  }
}

// 初始化
onMounted(() => {
  getExceptionOrders()
  getStats()
})
</script>

<style scoped lang="scss">
.exception-order-container {
  padding: 20px;
  background: #f5f7fa;
  min-height: calc(100vh - 60px);
}

// 页面头部
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;

  .header-left {
    h2 {
      font-size: 24px;
      font-weight: 600;
      color: #303133;
      margin: 0 0 8px 0;
    }

    .subtitle {
      font-size: 14px;
      color: #909399;
    }
  }

  .header-right {
    display: flex;
    gap: 10px;
  }
}

// 统计卡片
.stats-cards {
  margin-bottom: 20px;

  .stat-card {
    position: relative;
    overflow: hidden;
    transition: all 0.3s;

    &:hover {
      transform: translateY(-5px);
      box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
    }

    :deep(.el-card__body) {
      padding: 20px;
      display: flex;
      justify-content: space-between;
      align-items: center;
    }

    .stat-content {
      .stat-value {
        font-size: 32px;
        font-weight: bold;
        margin-bottom: 8px;
      }

      .stat-label {
        font-size: 14px;
        color: #909399;
      }
    }

    .stat-icon {
      font-size: 48px;
      opacity: 0.2;
    }

    &.payment-timeout {
      .stat-value { color: #E6A23C; }
      .stat-icon { color: #E6A23C; }
    }

    &.address-error {
      .stat-value { color: #F56C6C; }
      .stat-icon { color: #F56C6C; }
    }

    &.stock-shortage {
      .stat-value { color: #E6A23C; }
      .stat-icon { color: #E6A23C; }
    }

    &.system-error {
      .stat-value { color: #F56C6C; }
      .stat-icon { color: #F56C6C; }
    }
  }
}

// 搜索卡片
.search-card {
  margin-bottom: 20px;

  :deep(.el-card__body) {
    padding: 20px;
  }

  :deep(.el-form-item) {
    margin-bottom: 0;
  }
}

// 表格卡片
.table-card {
  :deep(.el-card__body) {
    padding: 20px;
  }

  .amount {
    color: #F56C6C;
    font-weight: 600;
  }
}

// 分页
.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

// 日志元信息
.log-meta {
  margin-top: 8px;
  font-size: 12px;
  color: #909399;
}
</style>
