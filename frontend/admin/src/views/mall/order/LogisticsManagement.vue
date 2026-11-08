<template>
  <div class="logistics-management-container">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-left">
        <h2>物流管理</h2>
        <span class="subtitle">统一管理订单物流信息，实时跟踪配送状态</span>
      </div>
      <div class="header-right">
        <el-button type="primary" icon="Plus" @click="handleBatchShip">
          批量发货
        </el-button>
        <el-button icon="Refresh" @click="handleRefresh">
          刷新
        </el-button>
      </div>
    </div>

    <!-- 统计卡片 -->
    <div class="stats-cards">
      <el-row :gutter="16">
        <el-col :span="6">
          <el-card class="stat-card pending-ship">
            <div class="stat-content">
              <div class="stat-value">{{ stats.pendingShip }}</div>
              <div class="stat-label">待发货</div>
            </div>
            <el-icon class="stat-icon"><Box /></el-icon>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card class="stat-card shipping">
            <div class="stat-content">
              <div class="stat-value">{{ stats.shipping }}</div>
              <div class="stat-label">配送中</div>
            </div>
            <el-icon class="stat-icon"><Van /></el-icon>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card class="stat-card delivered">
            <div class="stat-content">
              <div class="stat-value">{{ stats.delivered }}</div>
              <div class="stat-label">已送达</div>
            </div>
            <el-icon class="stat-icon"><CircleCheck /></el-icon>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card class="stat-card exception">
            <div class="stat-content">
              <div class="stat-value">{{ stats.exception }}</div>
              <div class="stat-label">物流异常</div>
            </div>
            <el-icon class="stat-icon"><WarningFilled /></el-icon>
          </el-card>
        </el-col>
      </el-row>
    </div>

    <!-- 搜索筛选 -->
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
        <el-form-item label="物流单号">
          <el-input 
            v-model="searchForm.trackingNo" 
            placeholder="请输入物流单号"
            clearable
            style="width: 200px"
          />
        </el-form-item>
        <el-form-item label="物流状态">
          <el-select 
            v-model="searchForm.status" 
            placeholder="请选择物流状态"
            clearable
            style="width: 150px"
          >
            <el-option label="全部" value="" />
            <el-option label="待发货" value="pending" />
            <el-option label="已发货" value="shipped" />
            <el-option label="运输中" value="in_transit" />
            <el-option label="派送中" value="out_for_delivery" />
            <el-option label="已签收" value="delivered" />
            <el-option label="异常" value="exception" />
          </el-select>
        </el-form-item>
        <el-form-item label="物流公司">
          <el-select 
            v-model="searchForm.logisticsCompany" 
            placeholder="请选择物流公司"
            clearable
            style="width: 150px"
          >
            <el-option label="全部" value="" />
            <el-option label="顺丰速运" value="SF" />
            <el-option label="圆通速递" value="YTO" />
            <el-option label="中通快递" value="ZTO" />
            <el-option label="韵达快递" value="YUNDA" />
            <el-option label="申通快递" value="STO" />
          </el-select>
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
            <el-link type="primary" @click="handleViewOrder(row)">
              {{ row.orderNo }}
            </el-link>
          </template>
        </el-table-column>
        <el-table-column prop="trackingNo" label="物流单号" width="200" />
        <el-table-column prop="logisticsCompany" label="物流公司" width="120" />
        <el-table-column prop="receiver" label="收货人" width="120" />
        <el-table-column prop="phone" label="联系电话" width="120" />
        <el-table-column prop="address" label="收货地址" min-width="200" show-overflow-tooltip />
        <el-table-column prop="status" label="物流状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)" size="small">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="shipTime" label="发货时间" width="160" />
        <el-table-column label="操作" width="280" fixed="right">
          <template #default="{ row }">
            <el-button 
              v-if="row.status === 'pending'"
              type="primary" 
              link 
              size="small"
              @click="handleShip(row)"
            >
              发货
            </el-button>
            <el-button 
              type="success" 
              link 
              size="small"
              @click="handleTrack(row)"
            >
              物流跟踪
            </el-button>
            <el-button 
              v-if="row.status !== 'pending'"
              type="warning" 
              link 
              size="small"
              @click="handleEditLogistics(row)"
            >
              修改物流
            </el-button>
            <el-button 
              type="info" 
              link 
              size="small"
              @click="handlePrint(row)"
            >
              打印面单
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

    <!-- 发货对话框 -->
    <el-dialog 
      v-model="shipDialogVisible" 
      title="订单发货"
      width="600px"
    >
      <el-form :model="shipForm" label-width="100px">
        <el-form-item label="订单号">
          <el-input v-model="shipForm.orderNo" disabled />
        </el-form-item>
        <el-form-item label="物流公司" required>
          <el-select v-model="shipForm.logisticsCompany" placeholder="请选择物流公司" style="width: 100%">
            <el-option label="顺丰速运" value="SF" />
            <el-option label="圆通速递" value="YTO" />
            <el-option label="中通快递" value="ZTO" />
            <el-option label="韵达快递" value="YUNDA" />
            <el-option label="申通快递" value="STO" />
          </el-select>
        </el-form-item>
        <el-form-item label="物流单号" required>
          <el-input v-model="shipForm.trackingNo" placeholder="请输入物流单号" />
        </el-form-item>
        <el-form-item label="发货备注">
          <el-input 
            v-model="shipForm.remark" 
            type="textarea" 
            :rows="3"
            placeholder="请输入发货备注"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="shipDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmitShip" :loading="submitLoading">
          确认发货
        </el-button>
      </template>
    </el-dialog>

    <!-- 物流跟踪对话框 -->
    <el-dialog 
      v-model="trackDialogVisible" 
      title="物流跟踪"
      width="700px"
    >
      <div class="logistics-info">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="订单号">{{ currentTrack.orderNo }}</el-descriptions-item>
          <el-descriptions-item label="物流单号">{{ currentTrack.trackingNo }}</el-descriptions-item>
          <el-descriptions-item label="物流公司">{{ currentTrack.logisticsCompany }}</el-descriptions-item>
          <el-descriptions-item label="当前状态">
            <el-tag :type="getStatusType(currentTrack.status)">
              {{ getStatusText(currentTrack.status) }}
            </el-tag>
          </el-descriptions-item>
        </el-descriptions>
      </div>
      
      <el-divider />
      
      <el-timeline>
        <el-timeline-item 
          v-for="(item, index) in trackList" 
          :key="index"
          :timestamp="item.time"
          placement="top"
          :color="index === 0 ? '#67C23A' : '#909399'"
        >
          <p>{{ item.content }}</p>
        </el-timeline-item>
      </el-timeline>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { 
  Plus,
  Refresh,
  Search,
  RefreshLeft,
  Box,
  Van,
  CircleCheck,
  WarningFilled
} from '@element-plus/icons-vue'
import request from '@/utils/request'

// 统计数据
const stats = reactive({
  pendingShip: 0,
  shipping: 0,
  delivered: 0,
  exception: 0
})

// 搜索表单
const searchForm = reactive({
  orderNo: '',
  trackingNo: '',
  status: '',
  logisticsCompany: ''
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

// 发货对话框
const shipDialogVisible = ref(false)
const shipForm = reactive({
  orderId: '',
  orderNo: '',
  logisticsCompany: '',
  trackingNo: '',
  remark: ''
})
const submitLoading = ref(false)

// 物流跟踪对话框
const trackDialogVisible = ref(false)
const currentTrack = reactive({
  orderNo: '',
  trackingNo: '',
  logisticsCompany: '',
  status: ''
})
const trackList = ref([])

// 获取物流列表
const getLogisticsList = async () => {
  loading.value = true
  try {
    const params = {
      page: pagination.page,
      size: pagination.size,
      ...searchForm
    }
    
    const response = await request.post('/web/order/logistics/list', params)
    
    if (response.code === 200) {
      tableData.value = response.data.list || []
      pagination.total = response.data.total || 0
    }
  } catch (error) {
    console.error('获取物流列表失败:', error)
    ElMessage.error('获取物流列表失败')
  } finally {
    loading.value = false
  }
}

// 获取统计数据
const getStats = async () => {
  try {
    const response = await request.post('/web/order/logistics/stats')
    if (response.code === 200) {
      Object.assign(stats, response.data)
    }
  } catch (error) {
    console.error('获取统计数据失败:', error)
  }
}

// 状态文本
const getStatusText = (status) => {
  const statusMap = {
    'pending': '待发货',
    'shipped': '已发货',
    'in_transit': '运输中',
    'out_for_delivery': '派送中',
    'delivered': '已签收',
    'exception': '异常'
  }
  return statusMap[status] || status
}

// 状态类型
const getStatusType = (status) => {
  const typeMap = {
    'pending': 'warning',
    'shipped': 'primary',
    'in_transit': 'primary',
    'out_for_delivery': 'success',
    'delivered': 'success',
    'exception': 'danger'
  }
  return typeMap[status] || 'info'
}

// 查询
const handleSearch = () => {
  pagination.page = 1
  getLogisticsList()
}

// 重置
const handleReset = () => {
  Object.assign(searchForm, {
    orderNo: '',
    trackingNo: '',
    status: '',
    logisticsCompany: ''
  })
  handleSearch()
}

// 刷新
const handleRefresh = () => {
  getLogisticsList()
  getStats()
  ElMessage.success('刷新成功')
}

// 批量发货
const handleBatchShip = () => {
  if (selectedRows.value.length === 0) {
    ElMessage.warning('请选择需要发货的订单')
    return
  }
  ElMessage.info('批量发货功能开发中...')
}

// 选择变化
const handleSelectionChange = (rows) => {
  selectedRows.value = rows
}

// 分页
const handlePageChange = (page) => {
  pagination.page = page
  getLogisticsList()
}

const handleSizeChange = (size) => {
  pagination.size = size
  pagination.page = 1
  getLogisticsList()
}

// 查看订单
const handleViewOrder = (row) => {
  ElMessage.info('订单详情：' + row.orderNo)
}

// 发货
const handleShip = (row) => {
  shipForm.orderId = row.id
  shipForm.orderNo = row.orderNo
  shipForm.logisticsCompany = ''
  shipForm.trackingNo = ''
  shipForm.remark = ''
  shipDialogVisible.value = true
}

// 提交发货
const handleSubmitShip = async () => {
  if (!shipForm.logisticsCompany) {
    ElMessage.warning('请选择物流公司')
    return
  }
  if (!shipForm.trackingNo) {
    ElMessage.warning('请输入物流单号')
    return
  }
  
  submitLoading.value = true
  try {
    const response = await request.post('/web/order/logistics/ship', shipForm)
    
    if (response.code === 200) {
      ElMessage.success('发货成功')
      shipDialogVisible.value = false
      getLogisticsList()
      getStats()
    } else {
      ElMessage.error(response.message || '发货失败')
    }
  } catch (error) {
    console.error('发货失败:', error)
    ElMessage.error('发货失败')
  } finally {
    submitLoading.value = false
  }
}

// 物流跟踪
const handleTrack = async (row) => {
  try {
    const response = await request.post('/web/order/logistics/track', {
      orderId: row.id
    })
    
    if (response.code === 200) {
      Object.assign(currentTrack, {
        orderNo: row.orderNo,
        trackingNo: row.trackingNo,
        logisticsCompany: row.logisticsCompany,
        status: row.status
      })
      trackList.value = response.data || []
      trackDialogVisible.value = true
    } else {
      ElMessage.error(response.message || '获取物流信息失败')
    }
  } catch (error) {
    console.error('获取物流信息失败:', error)
    ElMessage.error('获取物流信息失败')
  }
}

// 修改物流
const handleEditLogistics = (row) => {
  shipForm.orderId = row.id
  shipForm.orderNo = row.orderNo
  shipForm.logisticsCompany = row.logisticsCompany
  shipForm.trackingNo = row.trackingNo
  shipForm.remark = ''
  shipDialogVisible.value = true
}

// 打印面单
const handlePrint = (row) => {
  ElMessage.info('打印面单功能开发中...')
}

// 初始化
onMounted(() => {
  getLogisticsList()
  getStats()
})
</script>

<style scoped lang="scss">
.logistics-management-container {
  padding: 20px;
  background: #f5f7fa;
  min-height: calc(100vh - 60px);
}

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

    &.pending-ship {
      .stat-value { color: #E6A23C; }
      .stat-icon { color: #E6A23C; }
    }

    &.shipping {
      .stat-value { color: #409EFF; }
      .stat-icon { color: #409EFF; }
    }

    &.delivered {
      .stat-value { color: #67C23A; }
      .stat-icon { color: #67C23A; }
    }

    &.exception {
      .stat-value { color: #F56C6C; }
      .stat-icon { color: #F56C6C; }
    }
  }
}

.search-card {
  margin-bottom: 20px;

  :deep(.el-card__body) {
    padding: 20px;
  }
}

.table-card {
  :deep(.el-card__body) {
    padding: 20px;
  }
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

.logistics-info {
  margin-bottom: 20px;
}
</style>
