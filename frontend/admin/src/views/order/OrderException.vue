<template>
  <div class="order-exception-container">
    <!-- 页面标题 -->
    <div class="page-header">
      <div class="header-content">
        <h1 class="page-title">
          <el-icon><Warning /></el-icon>
          异常订单管理
        </h1>
        <p class="page-description">识别、分析和处理各类异常订单，确保订单流程顺畅</p>
      </div>
    </div>

    <!-- 统计卡片 -->
    <el-row :gutter="20" class="stats-cards">
      <el-col :span="6">
        <el-card class="stat-card exception-total">
          <div class="stat-content">
            <div class="stat-icon">
              <el-icon><Warning /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ exceptionStats.total }}</div>
              <div class="stat-label">异常订单总数</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card exception-pending">
          <div class="stat-content">
            <div class="stat-icon">
              <el-icon><Clock /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ exceptionStats.pending }}</div>
              <div class="stat-label">待处理异常</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card exception-processing">
          <div class="stat-content">
            <div class="stat-icon">
              <el-icon><Loading /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ exceptionStats.processing }}</div>
              <div class="stat-label">处理中异常</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card exception-resolved">
          <div class="stat-content">
            <div class="stat-icon">
              <el-icon><Check /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ exceptionStats.resolved }}</div>
              <div class="stat-label">已解决异常</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 查询表单 -->
    <el-card class="search-card">
      <el-form :model="searchForm" :inline="true" class="search-form">
        <el-form-item label="异常类型">
          <el-select v-model="searchForm.exceptionType" placeholder="请选择异常类型" clearable>
            <el-option label="支付异常" value="payment" />
            <el-option label="库存异常" value="inventory" />
            <el-option label="配送异常" value="delivery" />
            <el-option label="系统异常" value="system" />
            <el-option label="用户操作异常" value="user" />
            <el-option label="商家异常" value="merchant" />
          </el-select>
        </el-form-item>
        <el-form-item label="处理状态">
          <el-select v-model="searchForm.status" placeholder="请选择处理状态" clearable>
            <el-option label="待处理" value="pending" />
            <el-option label="处理中" value="processing" />
            <el-option label="已解决" value="resolved" />
            <el-option label="已关闭" value="closed" />
          </el-select>
        </el-form-item>
        <el-form-item label="优先级">
          <el-select v-model="searchForm.priority" placeholder="请选择优先级" clearable>
            <el-option label="低" value="low" />
            <el-option label="中" value="medium" />
            <el-option label="高" value="high" />
            <el-option label="紧急" value="urgent" />
          </el-select>
        </el-form-item>
        <el-form-item label="订单号">
          <el-input v-model="searchForm.orderNo" placeholder="请输入订单号" clearable />
        </el-form-item>
        <el-form-item label="创建时间">
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
          <el-button type="primary" @click="handleSearch">
            <el-icon><Search /></el-icon>
            查询
          </el-button>
          <el-button @click="resetSearch">
            <el-icon><Refresh /></el-icon>
            重置
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 异常订单列表 -->
    <el-card class="table-card">
      <template #header>
        <div class="table-header">
          <div class="header-left">
            <h3>异常订单列表</h3>
          </div>
          <div class="header-right">
            <el-button type="primary" @click="handleBatchProcess" :disabled="!selectedOrders.length">
              <el-icon><Operation /></el-icon>
              批量处理
            </el-button>
            <el-button @click="handleExport">
              <el-icon><Download /></el-icon>
              导出数据
            </el-button>
            <el-button @click="handleRefresh">
              <el-icon><Refresh /></el-icon>
              刷新
            </el-button>
          </div>
        </div>
      </template>

      <el-table
        :data="tableData"
        v-loading="loading"
        @selection-change="handleSelectionChange"
        stripe
        class="exception-table"
      >
        <el-table-column type="selection" width="55" />
        <el-table-column prop="orderNo" label="订单号" width="150" fixed="left">
          <template #default="{ row }">
            <el-link type="primary" @click="handleViewOrder(row)">{{ row.orderNo }}</el-link>
          </template>
        </el-table-column>
        <el-table-column prop="exceptionType" label="异常类型" width="120">
          <template #default="{ row }">
            <el-tag :type="getExceptionTypeColor(row.exceptionType)">
              {{ getExceptionTypeName(row.exceptionType) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="title" label="异常标题" width="200" show-overflow-tooltip />
        <el-table-column prop="description" label="异常描述" width="300" show-overflow-tooltip />
        <el-table-column prop="priority" label="优先级" width="100">
          <template #default="{ row }">
            <el-tag :type="getPriorityColor(row.priority)">
              {{ getPriorityName(row.priority) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="处理状态" width="120">
          <template #default="{ row }">
            <el-tag :type="getStatusColor(row.status)">
              {{ getStatusName(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="orderAmount" label="订单金额" width="120">
          <template #default="{ row }">
            <span class="amount">¥{{ row.orderAmount }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="customerName" label="客户姓名" width="120" />
        <el-table-column prop="assignee" label="处理人" width="120" />
        <el-table-column prop="createTime" label="发现时间" width="160" />
        <el-table-column prop="updateTime" label="更新时间" width="160" />
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" size="small" @click="handleProcess(row)">
              处理
            </el-button>
            <el-button type="info" size="small" @click="handleViewDetail(row)">
              详情
            </el-button>
            <el-dropdown @command="(command) => handleDropdownCommand(command, row)">
              <el-button type="text" size="small">
                更多<el-icon><ArrowDown /></el-icon>
              </el-button>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="assign">分配处理人</el-dropdown-item>
                  <el-dropdown-item command="escalate">升级处理</el-dropdown-item>
                  <el-dropdown-item command="close">关闭异常</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination-container">
        <el-pagination
          v-model:current-page="pagination.page"
          v-model:page-size="pagination.size"
          :total="pagination.total"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>

    <!-- 处理异常对话框 -->
    <el-dialog v-model="processDialogVisible" title="处理异常订单" width="600px">
      <el-form :model="processForm" :rules="processRules" ref="processFormRef" label-width="100px">
        <el-form-item label="处理方案" prop="solution">
          <el-select v-model="processForm.solution" placeholder="请选择处理方案">
            <el-option label="重新支付" value="repay" />
            <el-option label="补充库存" value="restock" />
            <el-option label="重新配送" value="redeliver" />
            <el-option label="退款处理" value="refund" />
            <el-option label="人工介入" value="manual" />
            <el-option label="系统修复" value="system_fix" />
          </el-select>
        </el-form-item>
        <el-form-item label="处理说明" prop="remark">
          <el-input
            v-model="processForm.remark"
            type="textarea"
            :rows="4"
            placeholder="请输入处理说明..."
          />
        </el-form-item>
        <el-form-item label="预计完成时间">
          <el-date-picker
            v-model="processForm.estimatedTime"
            type="datetime"
            placeholder="选择预计完成时间"
            format="YYYY-MM-DD HH:mm:ss"
            value-format="YYYY-MM-DD HH:mm:ss"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="processDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitProcess">确定处理</el-button>
      </template>
    </el-dialog>

    <!-- 异常详情对话框 -->
    <el-dialog v-model="detailDialogVisible" title="异常详情" width="800px">
      <div v-if="currentException" class="exception-detail">
        <el-descriptions title="基本信息" :column="2" border>
          <el-descriptions-item label="订单号">{{ currentException.orderNo }}</el-descriptions-item>
          <el-descriptions-item label="异常类型">
            <el-tag :type="getExceptionTypeColor(currentException.exceptionType)">
              {{ getExceptionTypeName(currentException.exceptionType) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="异常标题">{{ currentException.title }}</el-descriptions-item>
          <el-descriptions-item label="优先级">
            <el-tag :type="getPriorityColor(currentException.priority)">
              {{ getPriorityName(currentException.priority) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="处理状态">
            <el-tag :type="getStatusColor(currentException.status)">
              {{ getStatusName(currentException.status) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="发现时间">{{ currentException.createTime }}</el-descriptions-item>
        </el-descriptions>

        <div class="detail-section">
          <h4>异常描述</h4>
          <p>{{ currentException.description }}</p>
        </div>

        <div class="detail-section">
          <h4>处理记录</h4>
          <el-timeline>
            <el-timeline-item
              v-for="(record, index) in currentException.processRecords"
              :key="index"
              :timestamp="record.time"
              :type="record.type"
            >
              <div class="timeline-content">
                <div class="timeline-title">{{ record.title }}</div>
                <div class="timeline-description">{{ record.description }}</div>
                <div class="timeline-operator">操作人：{{ record.operator }}</div>
              </div>
            </el-timeline-item>
          </el-timeline>
        </div>
      </div>
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
  Warning,
  Clock,
  Loading,
  Check,
  Search,
  Refresh,
  Operation,
  Download,
  ArrowDown
} from '@element-plus/icons-vue'

// 响应式数据
const loading = ref(false)
const tableData = ref([])
const selectedOrders = ref([])
const processDialogVisible = ref(false)
const detailDialogVisible = ref(false)
const currentException = ref(null)

// 统计数据
const exceptionStats = reactive({
  total: 324,
  pending: 56,
  processing: 23,
  resolved: 245
})

// 搜索表单
const searchForm = reactive({
  exceptionType: '',
  status: '',
  priority: '',
  orderNo: '',
  dateRange: []
})

// 处理表单
const processForm = reactive({
  solution: '',
  remark: '',
  estimatedTime: ''
})

const processRules = {
  solution: [{ required: true, message: '请选择处理方案', trigger: 'change' }],
  remark: [{ required: true, message: '请输入处理说明', trigger: 'blur' }]
}

// 分页
const pagination = reactive({
  page: 1,
  size: 20,
  total: 0
})

// 获取异常类型颜色
const getExceptionTypeColor = (type) => {
  const colors = {
    payment: 'danger',
    inventory: 'warning',
    delivery: 'info',
    system: 'danger',
    user: 'warning',
    merchant: 'info'
  }
  return colors[type] || 'info'
}

// 获取异常类型名称
const getExceptionTypeName = (type) => {
  const names = {
    payment: '支付异常',
    inventory: '库存异常',
    delivery: '配送异常',
    system: '系统异常',
    user: '用户操作异常',
    merchant: '商家异常'
  }
  return names[type] || '未知异常'
}

// 获取优先级颜色
const getPriorityColor = (priority) => {
  const colors = {
    low: 'info',
    medium: 'warning',
    high: 'danger',
    urgent: 'danger'
  }
  return colors[priority] || 'info'
}

// 获取优先级名称
const getPriorityName = (priority) => {
  const names = {
    low: '低',
    medium: '中',
    high: '高',
    urgent: '紧急'
  }
  return names[priority] || '未知'
}

// 获取状态颜色
const getStatusColor = (status) => {
  const colors = {
    pending: 'warning',
    processing: 'primary',
    resolved: 'success',
    closed: 'info'
  }
  return colors[status] || 'info'
}

// 获取状态名称
const getStatusName = (status) => {
  const names = {
    pending: '待处理',
    processing: '处理中',
    resolved: '已解决',
    closed: '已关闭'
  }
  return names[status] || '未知状态'
}

// 加载数据
const loadData = async () => {
  loading.value = true
  try {
    // 模拟API调用
    await new Promise(resolve => setTimeout(resolve, 1000))
    
    // 模拟数据
    const mockData = []
    for (let i = 1; i <= 20; i++) {
      mockData.push({
        id: i,
        orderNo: `EXC${Date.now()}${i}`,
        exceptionType: ['payment', 'inventory', 'delivery', 'system', 'user'][Math.floor(Math.random() * 5)],
        title: `订单异常${i}`,
        description: `这是第${i}个异常订单的详细描述信息，包含异常原因和相关背景`,
        priority: ['low', 'medium', 'high', 'urgent'][Math.floor(Math.random() * 4)],
        status: ['pending', 'processing', 'resolved'][Math.floor(Math.random() * 3)],
        orderAmount: (Math.random() * 1000 + 100).toFixed(2),
        customerName: `客户${i}`,
        assignee: Math.random() > 0.5 ? `处理员${i}` : '',
        createTime: new Date(Date.now() - Math.random() * 30 * 24 * 60 * 60 * 1000).toLocaleString(),
        updateTime: new Date(Date.now() - Math.random() * 7 * 24 * 60 * 60 * 1000).toLocaleString(),
        processRecords: [
          {
            time: new Date().toLocaleString(),
            type: 'primary',
            title: '异常发现',
            description: '系统自动检测到订单异常',
            operator: '系统'
          }
        ]
      })
    }
    
    tableData.value = mockData
    pagination.total = 324
  } catch (error) {
    ElMessage.error('加载数据失败：' + error.message)
  } finally {
    loading.value = false
  }
}

// 搜索
const handleSearch = () => {
  console.log('搜索条件：', searchForm)
  loadData()
}

// 重置搜索
const resetSearch = () => {
  Object.keys(searchForm).forEach(key => {
    searchForm[key] = key === 'dateRange' ? [] : ''
  })
  loadData()
}

// 选择变化
const handleSelectionChange = (selection) => {
  selectedOrders.value = selection
}

// 处理异常
const handleProcess = (row) => {
  currentException.value = row
  processForm.solution = ''
  processForm.remark = ''
  processForm.estimatedTime = ''
  processDialogVisible.value = true
}

// 提交处理
const submitProcess = async () => {
  try {
    console.log('处理异常：', processForm)
    ElMessage.success('异常处理成功')
    processDialogVisible.value = false
    loadData()
  } catch (error) {
    ElMessage.error('处理失败：' + error.message)
  }
}

// 查看详情
const handleViewDetail = (row) => {
  currentException.value = row
  detailDialogVisible.value = true
}

// 查看订单
const handleViewOrder = (row) => {
  console.log('查看订单：', row.orderNo)
  ElMessage.info(`查看订单：${row.orderNo}`)
}

// 下拉菜单命令
const handleDropdownCommand = async (command, row) => {
  switch (command) {
    case 'assign':
      ElMessage.info('分配处理人功能开发中')
      break
    case 'escalate':
      ElMessage.info('升级处理功能开发中')
      break
    case 'close':
      try {
        await ElMessageBox.confirm('确定要关闭此异常吗？', '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        })
        ElMessage.success('异常已关闭')
        loadData()
      } catch {
        // 用户取消
      }
      break
  }
}

// 批量处理
const handleBatchProcess = () => {
  ElMessage.info('批量处理功能开发中')
}

// 导出数据
const handleExport = () => {
  ElMessage.info('导出数据功能开发中')
}

// 刷新
const handleRefresh = () => {
  loadData()
}

// 分页
const handleSizeChange = (size) => {
  pagination.size = size
  loadData()
}

const handleCurrentChange = (page) => {
  pagination.page = page
  loadData()
}

// 初始化
onMounted(() => {
  loadData()
})
</script>

<style scoped>
.order-exception-container {
  padding: 20px;
  background-color: #f5f5f5;
  min-height: calc(100vh - 60px);
}

.page-header {
  margin-bottom: 20px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 10px;
  padding: 30px;
  color: white;
}

.header-content {
  text-align: center;
}

.page-title {
  margin: 0 0 10px 0;
  font-size: 28px;
  font-weight: 600;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
}

.page-description {
  margin: 0;
  font-size: 16px;
  opacity: 0.9;
}

.stats-cards {
  margin-bottom: 20px;
}

.stat-card {
  height: 120px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.stat-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 4px 20px rgba(0,0,0,0.1);
}

.stat-content {
  display: flex;
  align-items: center;
  height: 100%;
  padding: 20px;
}

.stat-icon {
  width: 60px;
  height: 60px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 20px;
  font-size: 24px;
}

.exception-total .stat-icon {
  background: linear-gradient(135deg, #667eea, #764ba2);
  color: white;
}

.exception-pending .stat-icon {
  background: linear-gradient(135deg, #f093fb, #f5576c);
  color: white;
}

.exception-processing .stat-icon {
  background: linear-gradient(135deg, #4facfe, #00f2fe);
  color: white;
}

.exception-resolved .stat-icon {
  background: linear-gradient(135deg, #43e97b, #38f9d7);
  color: white;
}

.stat-info {
  flex: 1;
}

.stat-value {
  font-size: 32px;
  font-weight: 700;
  color: #2c3e50;
  margin-bottom: 5px;
}

.stat-label {
  font-size: 14px;
  color: #7f8c8d;
  font-weight: 500;
}

.search-card {
  margin-bottom: 20px;
}

.search-form {
  padding: 10px 0;
}

.table-card {
  background: white;
  border-radius: 10px;
  overflow: hidden;
}

.table-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-left h3 {
  margin: 0;
  color: #2c3e50;
  font-weight: 600;
}

.header-right {
  display: flex;
  gap: 10px;
}

.exception-table {
  margin-top: 20px;
}

.amount {
  font-weight: 600;
  color: #e74c3c;
}

.pagination-container {
  display: flex;
  justify-content: center;
  padding: 20px 0;
}

.exception-detail {
  padding: 20px 0;
}

.detail-section {
  margin-top: 20px;
}

.detail-section h4 {
  margin-bottom: 10px;
  color: #2c3e50;
  font-weight: 600;
}

.timeline-content {
  padding: 10px;
}

.timeline-title {
  font-weight: 600;
  color: #2c3e50;
  margin-bottom: 5px;
}

.timeline-description {
  color: #7f8c8d;
  margin-bottom: 5px;
}

.timeline-operator {
  font-size: 12px;
  color: #95a5a6;
}
</style>
