<template>
  <div class="page-container">
    <!-- 搜索栏 -->
    <el-card shadow="never" class="search-card">
      <el-form :inline="true" :model="searchForm">
        <el-form-item label="订单号">
          <el-input
            v-model="searchForm.orderNo"
            placeholder="请输入订单号"
            clearable
            style="width: 200px"
          />
        </el-form-item>
        <el-form-item label="用户信息">
          <el-input
            v-model="searchForm.userInfo"
            placeholder="用户名/手机号"
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
        <el-form-item label="处理状态">
          <el-select v-model="searchForm.status" placeholder="全部" clearable style="width: 120px">
            <el-option label="全部" value="" />
            <el-option label="待处理" value="pending" />
            <el-option label="处理中" value="processing" />
            <el-option label="已完成" value="completed" />
            <el-option label="已取消" value="cancelled" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">
            <el-icon><Search /></el-icon>查询
          </el-button>
          <el-button @click="handleReset">
            <el-icon><RefreshRight /></el-icon>重置
          </el-button>
          <el-button type="success" @click="handleExport">
            <el-icon><Download /></el-icon>导出
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 服务记录列表 -->
    <el-card shadow="never">
      <el-table :data="tableData" border stripe v-loading="loading">
        <el-table-column prop="serviceNo" label="服务单号" width="180" />
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
        <el-table-column label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="handler" label="处理人" width="100" />
        <el-table-column prop="createTime" label="申请时间" width="160" />
        <el-table-column prop="completeTime" label="完成时间" width="160" />
        <el-table-column label="操作" width="200" align="center" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click="handleViewDetail(row)">
              <el-icon><View /></el-icon>查看
            </el-button>
            <el-button link type="primary" size="small" @click="handleViewTimeline(row)">
              <el-icon><Clock /></el-icon>进度
            </el-button>
            <el-button link type="primary" size="small" @click="handleViewReview(row)" v-if="row.status === 'completed'">
              <el-icon><Star /></el-icon>评价
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

    <!-- 服务详情对话框 -->
    <el-dialog
      v-model="detailDialogVisible"
      title="服务详情"
      width="900px"
    >
      <div class="detail-content" v-if="currentRecord">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="服务单号">{{ currentRecord.serviceNo }}</el-descriptions-item>
          <el-descriptions-item label="订单号">{{ currentRecord.orderNo }}</el-descriptions-item>
          <el-descriptions-item label="服务类型">
            <el-tag :type="getTypeTag(currentRecord.type)">
              {{ getTypeText(currentRecord.type) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="getStatusType(currentRecord.status)">
              {{ getStatusText(currentRecord.status) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="用户">{{ currentRecord.userName }}</el-descriptions-item>
          <el-descriptions-item label="联系电话">{{ currentRecord.phone }}</el-descriptions-item>
          <el-descriptions-item label="商品名称" :span="2">{{ currentRecord.goodsName }}</el-descriptions-item>
          <el-descriptions-item label="申请原因" :span="2">{{ currentRecord.reason }}</el-descriptions-item>
          <el-descriptions-item label="申请时间">{{ currentRecord.createTime }}</el-descriptions-item>
          <el-descriptions-item label="处理人">{{ currentRecord.handler || '-' }}</el-descriptions-item>
          <el-descriptions-item label="完成时间" :span="2">{{ currentRecord.completeTime || '-' }}</el-descriptions-item>
          <el-descriptions-item label="备注" :span="2">{{ currentRecord.remark || '-' }}</el-descriptions-item>
        </el-descriptions>

        <div class="proof-images" v-if="currentRecord.images && currentRecord.images.length > 0">
          <div class="section-title">凭证图片</div>
          <div class="image-list">
            <el-image
              v-for="(img, index) in currentRecord.images"
              :key="index"
              :src="img"
              fit="cover"
              style="width: 100px; height: 100px; margin-right: 10px"
              :preview-src-list="currentRecord.images"
              :initial-index="index"
            />
          </div>
        </div>
      </div>
    </el-dialog>

    <!-- 处理进度对话框 -->
    <el-dialog
      v-model="timelineDialogVisible"
      title="处理进度"
      width="600px"
    >
      <el-timeline v-if="timelineList.length > 0">
        <el-timeline-item
          v-for="(item, index) in timelineList"
          :key="index"
          :timestamp="item.time"
          :type="index === 0 ? 'primary' : 'info'"
          placement="top"
        >
          <el-card>
            <div class="timeline-content">
              <div class="timeline-status">{{ item.status }}</div>
              <div class="timeline-desc">{{ item.description }}</div>
              <div class="timeline-operator" v-if="item.operator">
                操作人：{{ item.operator }}
              </div>
            </div>
          </el-card>
        </el-timeline-item>
      </el-timeline>
    </el-dialog>

    <!-- 评价详情对话框 -->
    <el-dialog
      v-model="reviewDialogVisible"
      title="服务评价"
      width="600px"
    >
      <div class="review-content" v-if="currentReview">
        <div class="review-item">
          <span class="review-label">满意度：</span>
          <el-rate v-model="currentReview.rating" disabled show-score />
        </div>
        <div class="review-item">
          <span class="review-label">评价标签：</span>
          <el-tag
            v-for="tag in currentReview.tags"
            :key="tag"
            style="margin-right: 10px"
          >
            {{ tag }}
          </el-tag>
        </div>
        <div class="review-item">
          <span class="review-label">评价内容：</span>
          <div class="review-text">{{ currentReview.content }}</div>
        </div>
        <div class="review-item">
          <span class="review-label">评价时间：</span>
          <span>{{ currentReview.createTime }}</span>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import {
  Search, RefreshRight, Download, View, Clock, Star
} from '@element-plus/icons-vue'

const loading = ref(false)

// 搜索表单
const searchForm = reactive({
  orderNo: '',
  userInfo: '',
  type: '',
  status: ''
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
const currentRecord = ref(null)

// 进度对话框
const timelineDialogVisible = ref(false)
const timelineList = ref([])

// 评价对话框
const reviewDialogVisible = ref(false)
const currentReview = ref(null)

// 获取数据
const fetchData = async () => {
  loading.value = true
  try {
    // TODO: 调用接口获取数据
    // 模拟数据
    tableData.value = [
      {
        serviceNo: 'SRV20240112001',
        orderNo: 'ORD20240110001',
        type: 'return',
        userName: '张三',
        phone: '138****5678',
        goodsName: 'iPhone 15 Pro Max 256GB 深空黑色',
        reason: '商品质量问题',
        status: 'completed',
        handler: '客服小王',
        createTime: '2024-01-10 10:30:00',
        completeTime: '2024-01-12 15:20:00',
        remark: '已退款',
        images: []
      },
      {
        serviceNo: 'SRV20240112002',
        orderNo: 'ORD20240111002',
        type: 'exchange',
        userName: '李四',
        phone: '139****8765',
        goodsName: '华为Mate 60 Pro 512GB 雅川青',
        reason: '尺码不合适',
        status: 'processing',
        handler: '客服小李',
        createTime: '2024-01-11 14:00:00',
        completeTime: '',
        remark: '',
        images: []
      },
      {
        serviceNo: 'SRV20240112003',
        orderNo: 'ORD20240111003',
        type: 'repair',
        userName: '王五',
        phone: '137****4321',
        goodsName: '小米14 Ultra 16GB+512GB 钛金属黑',
        reason: '商品损坏',
        status: 'pending',
        handler: '',
        createTime: '2024-01-11 16:30:00',
        completeTime: '',
        remark: '',
        images: []
      }
    ]
    pagination.total = 156
  } catch (error) {
    console.error('获取数据失败:', error)
    ElMessage.error('获取数据失败')
  } finally {
    loading.value = false
  }
}

// 搜索
const handleSearch = () => {
  pagination.page = 1
  fetchData()
}

// 重置
const handleReset = () => {
  Object.assign(searchForm, {
    orderNo: '',
    userInfo: '',
    type: '',
    status: ''
  })
  handleSearch()
}

// 导出
const handleExport = () => {
  ElMessage.success('导出成功')
}

// 查看详情
const handleViewDetail = (row) => {
  currentRecord.value = { ...row }
  detailDialogVisible.value = true
}

// 查看进度
const handleViewTimeline = (row) => {
  // TODO: 调用接口获取进度
  timelineList.value = [
    {
      time: '2024-01-12 15:20:00',
      status: '服务完成',
      description: '售后服务已完成，商品已退回，退款已到账',
      operator: '系统'
    },
    {
      time: '2024-01-11 10:00:00',
      status: '商品已收到',
      description: '商家已收到退回商品，正在进行质检',
      operator: '客服小王'
    },
    {
      time: '2024-01-10 16:30:00',
      status: '用户已发货',
      description: '用户已将商品寄回，物流单号：SF1234567890',
      operator: '用户'
    },
    {
      time: '2024-01-10 14:00:00',
      status: '审核通过',
      description: '售后申请已审核通过，请用户寄回商品',
      operator: '客服小王'
    },
    {
      time: '2024-01-10 10:30:00',
      status: '提交申请',
      description: '用户提交了退货申请',
      operator: '用户'
    }
  ]
  timelineDialogVisible.value = true
}

// 查看评价
const handleViewReview = (row) => {
  // TODO: 调用接口获取评价
  currentReview.value = {
    rating: 5,
    tags: ['服务态度好', '处理及时', '解决问题专业'],
    content: '客服非常专业，处理速度快，问题解决得很满意！',
    createTime: '2024-01-12 16:00:00'
  }
  reviewDialogVisible.value = true
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

.section-title {
  font-size: 14px;
  font-weight: bold;
  color: #303133;
  margin: 20px 0 10px;
}

.proof-images {
  margin-top: 20px;
}

.image-list {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.timeline-content {
  padding: 5px 0;
}

.timeline-status {
  font-size: 16px;
  font-weight: bold;
  color: #303133;
  margin-bottom: 8px;
}

.timeline-desc {
  font-size: 14px;
  color: #606266;
  margin-bottom: 5px;
}

.timeline-operator {
  font-size: 12px;
  color: #909399;
}

.review-content {
  padding: 10px 0;
}

.review-item {
  margin-bottom: 20px;
}

.review-label {
  font-size: 14px;
  font-weight: bold;
  color: #606266;
  margin-right: 10px;
}

.review-text {
  font-size: 14px;
  color: #303133;
  line-height: 1.6;
  margin-top: 10px;
  padding: 10px;
  background-color: #f5f7fa;
  border-radius: 4px;
}
</style>
