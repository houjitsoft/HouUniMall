<template>
  <div class="page-container">
    <!-- 搜索栏 -->
    <el-card shadow="never" class="search-card">
      <el-form :inline="true" :model="searchForm">
        <el-form-item label="查询方式">
          <el-radio-group v-model="searchForm.searchType">
            <el-radio value="order">订单号</el-radio>
            <el-radio value="tracking">物流单号</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item :label="searchForm.searchType === 'order' ? '订单号' : '物流单号'">
          <el-input
            v-model="searchForm.keyword"
            :placeholder="`请输入${searchForm.searchType === 'order' ? '订单号' : '物流单号'}`"
            style="width: 300px"
            clearable
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">
            <el-icon><Search /></el-icon>查询
          </el-button>
          <el-button type="success" @click="handleBatchQuery">
            <el-icon><Document /></el-icon>批量查询
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 物流信息 -->
    <el-card shadow="never" v-if="trackingInfo" class="info-card">
      <el-row :gutter="20">
        <el-col :span="12">
          <div class="info-group">
            <div class="info-title">订单信息</div>
            <div class="info-item">
              <span class="info-label">订单号：</span>
              <span class="info-value">{{ trackingInfo.orderNo }}</span>
            </div>
            <div class="info-item">
              <span class="info-label">物流单号：</span>
              <span class="info-value">{{ trackingInfo.trackingNo }}</span>
            </div>
            <div class="info-item">
              <span class="info-label">物流公司：</span>
              <span class="info-value">{{ trackingInfo.company }}</span>
            </div>
            <div class="info-item">
              <span class="info-label">当前状态：</span>
              <el-tag :type="getStatusType(trackingInfo.status)">
                {{ getStatusText(trackingInfo.status) }}
              </el-tag>
            </div>
          </div>
        </el-col>
        <el-col :span="12">
          <div class="info-group">
            <div class="info-title">收货信息</div>
            <div class="info-item">
              <span class="info-label">收件人：</span>
              <span class="info-value">{{ trackingInfo.recipient }}</span>
            </div>
            <div class="info-item">
              <span class="info-label">联系电话：</span>
              <span class="info-value">{{ trackingInfo.phone }}</span>
            </div>
            <div class="info-item">
              <span class="info-label">收货地址：</span>
              <span class="info-value">{{ trackingInfo.address }}</span>
            </div>
            <div class="info-item">
              <span class="info-label">发货时间：</span>
              <span class="info-value">{{ trackingInfo.shipTime }}</span>
            </div>
          </div>
        </el-col>
      </el-row>
    </el-card>

    <!-- 物流轨迹 -->
    <el-card shadow="never" v-if="trackingList.length > 0" class="timeline-card">
      <template #header>
        <div class="card-header">
          <span class="card-title">物流轨迹</span>
          <el-button type="primary" size="small" @click="handleRefresh">
            <el-icon><Refresh /></el-icon>刷新
          </el-button>
        </div>
      </template>
      <el-timeline>
        <el-timeline-item
          v-for="(item, index) in trackingList"
          :key="index"
          :timestamp="item.time"
          :type="index === 0 ? 'primary' : 'info'"
          :hollow="index !== 0"
          placement="top"
        >
          <el-card :class="{ 'current-status': index === 0 }">
            <div class="timeline-content">
              <div class="timeline-status">
                <el-icon v-if="index === 0" :size="20" color="#409eff"><CircleCheckFilled /></el-icon>
                <span :class="{ 'current-text': index === 0 }">{{ item.status }}</span>
              </div>
              <div class="timeline-desc">{{ item.description }}</div>
              <div class="timeline-location" v-if="item.location">
                <el-icon><Location /></el-icon>{{ item.location }}
              </div>
            </div>
          </el-card>
        </el-timeline-item>
      </el-timeline>
    </el-card>

    <!-- 批量查询列表 -->
    <el-card shadow="never" v-if="batchList.length > 0">
      <template #header>
        <div class="card-header">
          <span class="card-title">批量查询结果</span>
          <el-button type="success" size="small" @click="handleExport">
            <el-icon><Download /></el-icon>导出
          </el-button>
        </div>
      </template>
      <el-table :data="batchList" border stripe>
        <el-table-column prop="orderNo" label="订单号" width="180" />
        <el-table-column prop="trackingNo" label="物流单号" width="180" />
        <el-table-column prop="company" label="物流公司" width="120" />
        <el-table-column label="状态" width="120" align="center">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="latestStatus" label="最新动态" min-width="200" show-overflow-tooltip />
        <el-table-column prop="updateTime" label="更新时间" width="160" />
        <el-table-column label="操作" width="120" align="center" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click="handleViewDetail(row)">
              <el-icon><View /></el-icon>查看详情
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 批量查询对话框 -->
    <el-dialog
      v-model="batchDialogVisible"
      title="批量查询物流"
      width="600px"
    >
      <el-alert
        title="支持批量查询订单物流信息"
        type="info"
        :closable="false"
        style="margin-bottom: 20px"
      >
        <template #default>
          <div>• 每行一个订单号或物流单号</div>
          <div>• 最多支持100个单号查询</div>
        </template>
      </el-alert>
      <el-input
        v-model="batchInput"
        type="textarea"
        :rows="10"
        placeholder="请输入订单号或物流单号，每行一个"
      />
      <template #footer>
        <el-button @click="batchDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleBatchConfirm">确定查询</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { ElMessage } from 'element-plus'
import {
  Search, Document, Refresh, CircleCheckFilled, Location,
  Download, View
} from '@element-plus/icons-vue'

// 搜索表单
const searchForm = reactive({
  searchType: 'order',
  keyword: ''
})

// 物流信息
const trackingInfo = ref(null)

// 物流轨迹
const trackingList = ref([])

// 批量查询列表
const batchList = ref([])

// 批量查询对话框
const batchDialogVisible = ref(false)
const batchInput = ref('')

// 查询
const handleSearch = () => {
  if (!searchForm.keyword) {
    ElMessage.warning('请输入查询关键词')
    return
  }

  // TODO: 调用接口查询
  // 模拟数据
  trackingInfo.value = {
    orderNo: 'ORD20240112001',
    trackingNo: 'SF1234567890123',
    company: '顺丰速运',
    status: 'shipping',
    recipient: '张三',
    phone: '138****5678',
    address: '北京市朝阳区某某街道123号',
    shipTime: '2024-01-10 10:30:00'
  }

  trackingList.value = [
    {
      time: '2024-01-12 15:20:00',
      status: '派件中',
      description: '快件正在派送中，请您准备签收（快递员：李师傅，电话：138****1234）',
      location: '北京市朝阳区某某配送站'
    },
    {
      time: '2024-01-12 08:30:00',
      status: '到达目的地',
      description: '快件已到达北京市朝阳区某某配送站',
      location: '北京市朝阳区某某配送站'
    },
    {
      time: '2024-01-11 22:15:00',
      status: '运输中',
      description: '快件在运输途中',
      location: '河北省廊坊市转运中心'
    },
    {
      time: '2024-01-11 18:00:00',
      status: '已发出',
      description: '快件已从上海市青浦区转运中心发出',
      location: '上海市青浦区转运中心'
    },
    {
      time: '2024-01-11 14:30:00',
      status: '已揽收',
      description: '快件已由上海市青浦区某某网点揽收',
      location: '上海市青浦区某某网点'
    },
    {
      time: '2024-01-11 10:00:00',
      status: '下单成功',
      description: '顺丰速运已接单',
      location: '上海市'
    }
  ]

  ElMessage.success('查询成功')
}

// 批量查询
const handleBatchQuery = () => {
  batchDialogVisible.value = true
  batchInput.value = ''
}

// 批量查询确认
const handleBatchConfirm = () => {
  if (!batchInput.value) {
    ElMessage.warning('请输入查询单号')
    return
  }

  const lines = batchInput.value.split('\n').filter(line => line.trim())
  if (lines.length === 0) {
    ElMessage.warning('请输入查询单号')
    return
  }

  if (lines.length > 100) {
    ElMessage.warning('最多支持100个单号查询')
    return
  }

  // TODO: 调用接口批量查询
  // 模拟数据
  batchList.value = [
    {
      orderNo: 'ORD20240112001',
      trackingNo: 'SF1234567890123',
      company: '顺丰速运',
      status: 'shipping',
      latestStatus: '快件正在派送中，请您准备签收',
      updateTime: '2024-01-12 15:20:00'
    },
    {
      orderNo: 'ORD20240112002',
      trackingNo: 'ZTO9876543210987',
      company: '中通快递',
      status: 'signed',
      latestStatus: '快件已签收，感谢使用中通快递',
      updateTime: '2024-01-12 14:30:00'
    }
  ]

  batchDialogVisible.value = false
  ElMessage.success(`成功查询${batchList.value.length}条物流信息`)
}

// 刷新
const handleRefresh = () => {
  handleSearch()
  ElMessage.success('刷新成功')
}

// 导出
const handleExport = () => {
  ElMessage.success('导出成功')
}

// 查看详情
const handleViewDetail = (row) => {
  searchForm.keyword = row.trackingNo
  searchForm.searchType = 'tracking'
  handleSearch()
}

// 获取状态类型
const getStatusType = (status) => {
  const typeMap = {
    pending: 'info',
    shipping: 'warning',
    signed: 'success',
    exception: 'danger'
  }
  return typeMap[status] || 'info'
}

// 获取状态文本
const getStatusText = (status) => {
  const textMap = {
    pending: '待发货',
    shipping: '运输中',
    signed: '已签收',
    exception: '异常'
  }
  return textMap[status] || '未知'
}
</script>

<style scoped>
.page-container {
  padding: 20px;
}

.search-card {
  margin-bottom: 20px;
}

.info-card {
  margin-bottom: 20px;
}

.info-group {
  padding: 10px;
}

.info-title {
  font-size: 16px;
  font-weight: bold;
  color: #303133;
  margin-bottom: 16px;
  padding-bottom: 10px;
  border-bottom: 2px solid #e4e7ed;
}

.info-item {
  display: flex;
  align-items: center;
  margin-bottom: 12px;
}

.info-label {
  font-size: 14px;
  color: #909399;
  width: 100px;
  flex-shrink: 0;
}

.info-value {
  font-size: 14px;
  color: #606266;
  flex: 1;
}

.timeline-card {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-title {
  font-size: 16px;
  font-weight: bold;
  color: #303133;
}

.timeline-content {
  padding: 5px 0;
}

.timeline-status {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 16px;
  font-weight: bold;
  color: #303133;
  margin-bottom: 8px;
}

.current-text {
  color: #409eff;
}

.timeline-desc {
  font-size: 14px;
  color: #606266;
  line-height: 1.6;
  margin-bottom: 8px;
}

.timeline-location {
  display: flex;
  align-items: center;
  gap: 5px;
  font-size: 12px;
  color: #909399;
}

.current-status {
  background-color: #f0f9ff;
  border-color: #409eff;
}

:deep(.el-timeline-item__wrapper) {
  padding-left: 30px;
}

:deep(.el-timeline-item__timestamp) {
  font-size: 13px;
  color: #909399;
}
</style>
