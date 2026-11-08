<template>
  <div class="page-container">
    <!-- 选项卡 -->
    <el-card shadow="never" class="tab-card">
      <el-tabs v-model="activeTab" @tab-change="handleTabChange">
        <el-tab-pane label="全部" name="all">
          <template #label>
            <span class="tab-label">全部</span>
            <el-badge :value="tabCounts.all" class="tab-badge" />
          </template>
        </el-tab-pane>
        <el-tab-pane label="待处理" name="pending">
          <template #label>
            <span class="tab-label">待处理</span>
            <el-badge :value="tabCounts.pending" class="tab-badge" type="danger" />
          </template>
        </el-tab-pane>
        <el-tab-pane label="处理中" name="processing">
          <template #label>
            <span class="tab-label">处理中</span>
            <el-badge :value="tabCounts.processing" class="tab-badge" type="warning" />
          </template>
        </el-tab-pane>
        <el-tab-pane label="已完成" name="completed">
          <template #label>
            <span class="tab-label">已完成</span>
            <el-badge :value="tabCounts.completed" class="tab-badge" type="success" />
          </template>
        </el-tab-pane>
        <el-tab-pane label="已取消" name="cancelled">
          <template #label>
            <span class="tab-label">已取消</span>
            <el-badge :value="tabCounts.cancelled" class="tab-badge" type="info" />
          </template>
        </el-tab-pane>
      </el-tabs>
    </el-card>

    <!-- 搜索栏 -->
    <el-card shadow="never" class="search-card">
      <el-form :inline="true" :model="searchForm">
        <el-form-item label="工单号">
          <el-input
            v-model="searchForm.ticketNo"
            placeholder="请输入工单号"
            clearable
            style="width: 200px"
          />
        </el-form-item>
        <el-form-item label="订单号">
          <el-input
            v-model="searchForm.orderNo"
            placeholder="请输入订单号"
            clearable
            style="width: 200px"
          />
        </el-form-item>
        <el-form-item label="服务类型">
          <el-select v-model="searchForm.type" placeholder="全部" clearable style="width: 120px">
            <el-option label="全部" value="" />
            <el-option label="退货" value="return" />
            <el-option label="换货" value="exchange" />
            <el-option label="维修" value="repair" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">
            <el-icon><Search /></el-icon>查询
          </el-button>
          <el-button @click="handleReset">
            <el-icon><RefreshRight /></el-icon>重置
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 工单列表 -->
    <el-card shadow="never">
      <el-table :data="tableData" border stripe v-loading="loading">
        <el-table-column prop="ticketNo" label="工单号" width="180" />
        <el-table-column prop="orderNo" label="订单号" width="180" />
        <el-table-column label="服务类型" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="getTypeTag(row.type)">
              {{ getTypeText(row.type) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="userName" label="用户" width="100" />
        <el-table-column prop="phone" label="联系电话" width="120" />
        <el-table-column prop="goodsName" label="商品名称" min-width="200" show-overflow-tooltip />
        <el-table-column prop="reason" label="原因" width="150" show-overflow-tooltip />
        <el-table-column label="优先级" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="getPriorityType(row.priority)" size="small">
              {{ getPriorityText(row.priority) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="handler" label="处理人" width="100" />
        <el-table-column prop="createTime" label="创建时间" width="160" />
        <el-table-column label="操作" width="280" align="center" fixed="right">
          <template #default="{ row }">
            <el-button 
              link 
              type="primary" 
              size="small" 
              @click="handleViewDetail(row)"
            >
              <el-icon><View /></el-icon>查看
            </el-button>
            <el-button 
              v-if="row.status === 'pending'" 
              link 
              type="success" 
              size="small" 
              @click="handleAccept(row)"
            >
              <el-icon><Select /></el-icon>接单
            </el-button>
            <el-button 
              v-if="row.status === 'processing'" 
              link 
              type="warning" 
              size="small" 
              @click="handleProcess(row)"
            >
              <el-icon><Edit /></el-icon>处理
            </el-button>
            <el-button 
              v-if="row.status === 'processing'" 
              link 
              type="success" 
              size="small" 
              @click="handleComplete(row)"
            >
              <el-icon><CircleCheckFilled /></el-icon>完成
            </el-button>
            <el-button 
              v-if="row.status === 'pending' || row.status === 'processing'" 
              link 
              type="danger" 
              size="small" 
              @click="handleCancel(row)"
            >
              <el-icon><Close /></el-icon>取消
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-container">
        <el-pagination
          v-model:current-page="pagination.page"
          v-model:page-size="pagination.size"
          :total="pagination.total"
          :page-sizes="[20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="fetchData"
          @current-change="fetchData"
        />
      </div>
    </el-card>

    <!-- 工单详情对话框 -->
    <el-dialog
      v-model="detailDialogVisible"
      title="工单详情"
      width="900px"
    >
      <div class="detail-content" v-if="currentTicket">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="工单号">{{ currentTicket.ticketNo }}</el-descriptions-item>
          <el-descriptions-item label="订单号">{{ currentTicket.orderNo }}</el-descriptions-item>
          <el-descriptions-item label="服务类型">
            <el-tag :type="getTypeTag(currentTicket.type)">
              {{ getTypeText(currentTicket.type) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="优先级">
            <el-tag :type="getPriorityType(currentTicket.priority)" size="small">
              {{ getPriorityText(currentTicket.priority) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="用户">{{ currentTicket.userName }}</el-descriptions-item>
          <el-descriptions-item label="联系电话">{{ currentTicket.phone }}</el-descriptions-item>
          <el-descriptions-item label="商品名称" :span="2">{{ currentTicket.goodsName }}</el-descriptions-item>
          <el-descriptions-item label="申请原因" :span="2">{{ currentTicket.reason }}</el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="getStatusType(currentTicket.status)">
              {{ getStatusText(currentTicket.status) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="处理人">{{ currentTicket.handler || '未分配' }}</el-descriptions-item>
          <el-descriptions-item label="创建时间">{{ currentTicket.createTime }}</el-descriptions-item>
          <el-descriptions-item label="完成时间">{{ currentTicket.completeTime || '-' }}</el-descriptions-item>
          <el-descriptions-item label="用户描述" :span="2">{{ currentTicket.description || '-' }}</el-descriptions-item>
          <el-descriptions-item label="处理备注" :span="2">{{ currentTicket.remark || '-' }}</el-descriptions-item>
        </el-descriptions>
      </div>
    </el-dialog>

    <!-- 处理工单对话框 -->
    <el-dialog
      v-model="processDialogVisible"
      title="处理工单"
      width="600px"
    >
      <el-form :model="processForm" label-width="100px">
        <el-form-item label="处理结果" required>
          <el-radio-group v-model="processForm.result">
            <el-radio value="approve">同意</el-radio>
            <el-radio value="reject">拒绝</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="处理说明" required>
          <el-input
            v-model="processForm.remark"
            type="textarea"
            :rows="4"
            placeholder="请输入处理说明"
          />
        </el-form-item>
        <el-form-item label="退款金额" v-if="processForm.result === 'approve'">
          <el-input-number
            v-model="processForm.refundAmount"
            :min="0"
            :precision="2"
            style="width: 200px"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="processDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSaveProcess">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Search, RefreshRight, View, Select, Edit, CircleCheckFilled, Close
} from '@element-plus/icons-vue'

const loading = ref(false)

// 选项卡
const activeTab = ref('all')
const tabCounts = reactive({
  all: 173,
  pending: 45,
  processing: 128,
  completed: 856,
  cancelled: 23
})

// 搜索表单
const searchForm = reactive({
  ticketNo: '',
  orderNo: '',
  type: ''
})

// 分页
const pagination = reactive({
  page: 1,
  size: 20,
  total: 0
})

// 表格数据
const tableData = ref([])

// 详情对话框
const detailDialogVisible = ref(false)
const currentTicket = ref(null)

// 处理对话框
const processDialogVisible = ref(false)
const processForm = reactive({
  result: 'approve',
  remark: '',
  refundAmount: 0
})

// 获取数据
const fetchData = async () => {
  loading.value = true
  try {
    // TODO: 调用接口获取数据
    // 模拟数据
    tableData.value = [
      {
        ticketNo: 'TKT20240112001',
        orderNo: 'ORD20240110001',
        type: 'return',
        userName: '张三',
        phone: '138****5678',
        goodsName: 'iPhone 15 Pro Max 256GB 深空黑色',
        reason: '商品质量问题',
        priority: 'high',
        status: 'pending',
        handler: '',
        createTime: '2024-01-12 10:30:00',
        completeTime: '',
        description: '收到的商品有明显划痕',
        remark: ''
      },
      {
        ticketNo: 'TKT20240112002',
        orderNo: 'ORD20240111002',
        type: 'exchange',
        userName: '李四',
        phone: '139****8765',
        goodsName: '华为Mate 60 Pro 512GB 雅川青',
        reason: '尺码不合适',
        priority: 'normal',
        status: 'processing',
        handler: '客服小李',
        createTime: '2024-01-11 14:00:00',
        completeTime: '',
        description: '需要换大一号',
        remark: ''
      },
      {
        ticketNo: 'TKT20240110003',
        orderNo: 'ORD20240108003',
        type: 'repair',
        userName: '王五',
        phone: '137****4321',
        goodsName: '小米14 Ultra 16GB+512GB 钛金属黑',
        reason: '商品损坏',
        priority: 'low',
        status: 'completed',
        handler: '客服小王',
        createTime: '2024-01-08 16:30:00',
        completeTime: '2024-01-10 11:20:00',
        description: '屏幕有裂痕',
        remark: '已安排维修完成'
      }
    ]
    pagination.total = 173
  } catch (error) {
    console.error('获取数据失败:', error)
    ElMessage.error('获取数据失败')
  } finally {
    loading.value = false
  }
}

// 切换选项卡
const handleTabChange = () => {
  pagination.page = 1
  fetchData()
}

// 搜索
const handleSearch = () => {
  pagination.page = 1
  fetchData()
}

// 重置
const handleReset = () => {
  Object.assign(searchForm, {
    ticketNo: '',
    orderNo: '',
    type: ''
  })
  handleSearch()
}

// 查看详情
const handleViewDetail = (row) => {
  currentTicket.value = { ...row }
  detailDialogVisible.value = true
}

// 接单
const handleAccept = (row) => {
  ElMessageBox.confirm(`确定要接受工单"${row.ticketNo}"吗？`, '确认接单', {
    type: 'warning'
  }).then(() => {
    ElMessage.success('接单成功')
    fetchData()
  })
}

// 处理
const handleProcess = (row) => {
  currentTicket.value = { ...row }
  Object.assign(processForm, {
    result: 'approve',
    remark: '',
    refundAmount: 0
  })
  processDialogVisible.value = true
}

// 保存处理
const handleSaveProcess = () => {
  if (!processForm.remark) {
    ElMessage.warning('请输入处理说明')
    return
  }
  // TODO: 调用接口保存
  ElMessage.success('处理成功')
  processDialogVisible.value = false
  fetchData()
}

// 完成
const handleComplete = (row) => {
  ElMessageBox.confirm(`确定要完成工单"${row.ticketNo}"吗？`, '确认完成', {
    type: 'warning'
  }).then(() => {
    ElMessage.success('工单已完成')
    fetchData()
  })
}

// 取消
const handleCancel = (row) => {
  ElMessageBox.prompt('请输入取消原因', '取消工单', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    inputPattern: /.+/,
    inputErrorMessage: '请输入取消原因'
  }).then(({ value }) => {
    ElMessage.success('工单已取消')
    fetchData()
  })
}

// 获取类型标签
const getTypeTag = (type) => {
  const typeMap = {
    return: 'danger',
    exchange: 'warning',
    repair: 'success'
  }
  return typeMap[type] || 'info'
}

// 获取类型文本
const getTypeText = (type) => {
  const textMap = {
    return: '退货',
    exchange: '换货',
    repair: '维修'
  }
  return textMap[type] || '未知'
}

// 获取优先级类型
const getPriorityType = (priority) => {
  const typeMap = {
    high: 'danger',
    normal: '',
    low: 'info'
  }
  return typeMap[priority] || 'info'
}

// 获取优先级文本
const getPriorityText = (priority) => {
  const textMap = {
    high: '高',
    normal: '中',
    low: '低'
  }
  return textMap[priority] || '未知'
}

// 获取状态类型
const getStatusType = (status) => {
  const typeMap = {
    pending: 'info',
    processing: 'warning',
    completed: 'success',
    cancelled: 'danger'
  }
  return typeMap[status] || 'info'
}

// 获取状态文本
const getStatusText = (status) => {
  const textMap = {
    pending: '待处理',
    processing: '处理中',
    completed: '已完成',
    cancelled: '已取消'
  }
  return textMap[status] || '未知'
}

onMounted(() => {
  fetchData()
})
</script>

<style scoped>
.page-container {
  padding: 20px;
}

.tab-card {
  margin-bottom: 20px;
}

.tab-label {
  margin-right: 8px;
}

.tab-badge {
  margin-left: 8px;
}

.search-card {
  margin-bottom: 20px;
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

.detail-content {
  padding: 10px 0;
}
</style>
