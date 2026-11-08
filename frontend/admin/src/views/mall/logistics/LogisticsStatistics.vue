<template>
  <div class="page-container">
    <!-- 统计卡片 -->
    <el-row :gutter="20" class="stats-row">
      <el-col :xs="12" :sm="12" :md="6" :span="6">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-content">
            <div class="stat-icon" style="background: linear-gradient(135deg, #667eea 0%, #764ba2 100%)">
              <el-icon :size="32"><Box /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-label">待发货订单</div>
              <div class="stat-value">{{ stats.pendingShip }}</div>
              <div class="stat-desc">需尽快处理</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :xs="12" :sm="12" :md="6" :span="6">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-content">
            <div class="stat-icon" style="background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%)">
              <el-icon :size="32"><Odometer /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-label">运输中订单</div>
              <div class="stat-value">{{ stats.shipping }}</div>
              <div class="stat-desc">实时监控</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :xs="12" :sm="12" :md="6" :span="6">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-content">
            <div class="stat-icon" style="background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%)">
              <el-icon :size="32"><Select /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-label">已签收订单</div>
              <div class="stat-value">{{ stats.signed }}</div>
              <div class="stat-desc">今日完成</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :xs="12" :sm="12" :md="6" :span="6">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-content">
            <div class="stat-icon" style="background: linear-gradient(135deg, #fa709a 0%, #fee140 100%)">
              <el-icon :size="32"><WarningFilled /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-label">异常订单</div>
              <div class="stat-value error">{{ stats.exception }}</div>
              <div class="stat-desc">待处理</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 物流公司统计 -->
    <el-card shadow="never" class="section-card">
      <template #header>
        <div class="card-header">
          <span class="card-title">物流公司统计</span>
        </div>
      </template>
      <el-row :gutter="20">
        <el-col :xs="24" :sm="24" :md="12" :span="12">
          <div class="chart-container">
            <div class="chart-title">发货量排行</div>
            <div class="hc-table-wrap">
            <el-table :data="companyStats" border stripe>
              <el-table-column prop="company" label="物流公司" width="150" />
              <el-table-column prop="shipCount" label="发货量" width="100" align="right">
                <template #default="{ row }">
                  <span class="number-highlight">{{ row.shipCount }}</span>
                </template>
              </el-table-column>
              <el-table-column label="占比" width="120">
                <template #default="{ row }">
                  <el-progress 
                    :percentage="row.percentage" 
                    :color="getProgressColor(row.percentage)"
                    :stroke-width="12"
                  />
                </template>
              </el-table-column>
              <el-table-column prop="avgTime" label="平均时效" align="center">
                <template #default="{ row }">
                  <el-tag :type="getTimeType(row.avgTime)" size="small">
                    {{ row.avgTime }}天
                  </el-tag>
                </template>
              </el-table-column>
            </el-table>
            </div>
          </div>
        </el-col>
        <el-col :xs="24" :sm="24" :md="12" :span="12">
          <div class="chart-container">
            <div class="chart-title">签收率统计</div>
            <div class="hc-table-wrap">
            <el-table :data="companyStats" border stripe>
              <el-table-column prop="company" label="物流公司" width="150" />
              <el-table-column prop="signRate" label="签收率" width="120">
                <template #default="{ row }">
                  <el-progress 
                    :percentage="row.signRate" 
                    :color="getSignRateColor(row.signRate)"
                    :stroke-width="12"
                  />
                </template>
              </el-table-column>
              <el-table-column prop="exceptionCount" label="异常量" align="center">
                <template #default="{ row }">
                  <span :class="row.exceptionCount > 0 ? 'text-danger' : ''">
                    {{ row.exceptionCount }}
                  </span>
                </template>
              </el-table-column>
              <el-table-column label="状态" align="center" width="100">
                <template #default="{ row }">
                  <el-tag :type="row.signRate >= 95 ? 'success' : row.signRate >= 90 ? 'warning' : 'danger'" size="small">
                    {{ row.signRate >= 95 ? '优秀' : row.signRate >= 90 ? '良好' : '较差' }}
                  </el-tag>
                </template>
              </el-table-column>
            </el-table>
            </div>
          </div>
        </el-col>
      </el-row>
    </el-card>

    <!-- 筛选条件 -->
    <el-card shadow="never" class="filter-card">
      <el-form :inline="true" :model="searchForm" class="filter-form hc-form-stack">
        <el-form-item label="时间范围">
          <el-date-picker
            v-model="searchForm.dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            value-format="YYYY-MM-DD"
          />
        </el-form-item>
        <el-form-item label="物流公司">
          <el-select v-model="searchForm.company" placeholder="请选择" clearable style="width: 150px">
            <el-option label="全部" value="" />
            <el-option label="顺丰速运" value="顺丰速运" />
            <el-option label="中通快递" value="中通快递" />
            <el-option label="圆通速递" value="圆通速递" />
            <el-option label="韵达快递" value="韵达快递" />
            <el-option label="申通快递" value="申通快递" />
          </el-select>
        </el-form-item>
        <el-form-item label="订单状态">
          <el-select v-model="searchForm.status" placeholder="请选择" clearable style="width: 150px">
            <el-option label="全部" value="" />
            <el-option label="待发货" value="pending" />
            <el-option label="运输中" value="shipping" />
            <el-option label="已签收" value="signed" />
            <el-option label="异常" value="exception" />
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

    <!-- 物流统计明细 -->
    <el-card shadow="never">
      <template #header>
        <div class="card-header">
          <span class="card-title">物流统计明细</span>
        </div>
      </template>
      <div class="hc-table-wrap">
      <el-table :data="tableData" border stripe v-loading="loading">
        <el-table-column prop="orderNo" label="订单编号" width="180" />
        <el-table-column prop="trackingNo" label="物流单号" width="180" />
        <el-table-column prop="company" label="物流公司" width="120" />
        <el-table-column label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="shipTime" label="发货时间" width="160" />
        <el-table-column prop="signTime" label="签收时间" width="160" />
        <el-table-column prop="duration" label="运输时长" width="100" align="center">
          <template #default="{ row }">
            <span v-if="row.duration">{{ row.duration }}天</span>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column prop="recipient" label="收件人" width="100" />
        <el-table-column prop="address" label="收货地址" min-width="200" show-overflow-tooltip />
        <el-table-column label="操作" width="150" align="center" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click="handleViewDetail(row)">
              <el-icon><View /></el-icon>查看详情
            </el-button>
            <el-button link type="primary" size="small" @click="handleTrack(row)">
              <el-icon><Location /></el-icon>物流跟踪
            </el-button>
          </template>
        </el-table-column>
      </el-table>
      </div>

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
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { 
  Box, Odometer, Select, WarningFilled, Search, RefreshRight, 
  Download, View, Location 
} from '@element-plus/icons-vue'

const loading = ref(false)

// 统计数据
const stats = reactive({
  pendingShip: 128,
  shipping: 456,
  signed: 234,
  exception: 12
})

// 物流公司统计
const companyStats = ref([
  { company: '顺丰速运', shipCount: 1256, percentage: 35, avgTime: 2.3, signRate: 98, exceptionCount: 2 },
  { company: '中通快递', shipCount: 986, percentage: 28, avgTime: 3.1, signRate: 96, exceptionCount: 4 },
  { company: '圆通速递', shipCount: 745, percentage: 21, avgTime: 3.5, signRate: 94, exceptionCount: 5 },
  { company: '韵达快递', shipCount: 352, percentage: 10, avgTime: 3.8, signRate: 93, exceptionCount: 3 },
  { company: '申通快递', shipCount: 218, percentage: 6, avgTime: 4.2, signRate: 91, exceptionCount: 5 }
])

// 搜索表单
const searchForm = reactive({
  dateRange: [],
  company: '',
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

// 获取数据
const fetchData = async () => {
  loading.value = true
  try {
    // TODO: 调用接口获取数据
    // const res = await getLogisticsStatistics({
    //   page: pagination.page,
    //   limit: pagination.size,
    //   ...searchForm
    // })
    
    // 模拟数据
    tableData.value = [
      {
        orderNo: 'ORD20240112001',
        trackingNo: 'SF1234567890123',
        company: '顺丰速运',
        status: 'signed',
        shipTime: '2024-01-10 10:30:00',
        signTime: '2024-01-12 15:20:00',
        duration: 2,
        recipient: '张三',
        address: '北京市朝阳区某某街道123号'
      },
      {
        orderNo: 'ORD20240112002',
        trackingNo: 'ZTO9876543210987',
        company: '中通快递',
        status: 'shipping',
        shipTime: '2024-01-11 14:00:00',
        signTime: '',
        duration: null,
        recipient: '李四',
        address: '上海市浦东新区某某路456号'
      },
      {
        orderNo: 'ORD20240112003',
        trackingNo: 'YTO1357924680246',
        company: '圆通速递',
        status: 'pending',
        shipTime: '',
        signTime: '',
        duration: null,
        recipient: '王五',
        address: '广州市天河区某某大道789号'
      },
      {
        orderNo: 'ORD20240112004',
        trackingNo: 'YD2468013579135',
        company: '韵达快递',
        status: 'exception',
        shipTime: '2024-01-09 09:00:00',
        signTime: '',
        duration: null,
        recipient: '赵六',
        address: '深圳市南山区某某科技园101栋'
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
    dateRange: [],
    company: '',
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
  ElMessage.info(`查看订单详情: ${row.orderNo}`)
}

// 物流跟踪
const handleTrack = (row) => {
  ElMessage.info(`查看物流跟踪: ${row.trackingNo}`)
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

// 获取进度条颜色
const getProgressColor = (percentage) => {
  if (percentage >= 30) return '#67c23a'
  if (percentage >= 20) return '#e6a23c'
  return '#909399'
}

// 获取时效类型
const getTimeType = (avgTime) => {
  if (avgTime <= 2) return 'success'
  if (avgTime <= 3) return ''
  if (avgTime <= 4) return 'warning'
  return 'danger'
}

// 获取签收率颜色
const getSignRateColor = (signRate) => {
  if (signRate >= 95) return '#67c23a'
  if (signRate >= 90) return '#e6a23c'
  return '#f56c6c'
}

onMounted(() => {
  fetchData()
})
</script>

<style scoped>
.page-container {
  padding: 20px;
}

.stats-row {
  margin-bottom: 20px;
}

.stat-card {
  border-radius: 8px;
  overflow: hidden;
}

.stat-content {
  display: flex;
  align-items: center;
  gap: 16px;
}

.stat-icon {
  width: 64px;
  height: 64px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  flex-shrink: 0;
}

.stat-info {
  flex: 1;
}

.stat-label {
  font-size: 14px;
  color: #909399;
  margin-bottom: 8px;
}

.stat-value {
  font-size: 28px;
  font-weight: bold;
  color: #303133;
  margin-bottom: 4px;
}

.stat-value.error {
  color: #f56c6c;
}

.stat-desc {
  font-size: 12px;
  color: #c0c4cc;
}

.section-card {
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

.chart-container {
  padding: 10px 0;
}

.chart-title {
  font-size: 14px;
  font-weight: bold;
  color: #606266;
  margin-bottom: 16px;
  padding-left: 10px;
  border-left: 3px solid #409eff;
}

.number-highlight {
  color: #409eff;
  font-weight: bold;
}

.filter-card {
  margin-bottom: 20px;
}

.filter-form {
  margin-bottom: 0;
}

.text-danger {
  color: #f56c6c;
  font-weight: bold;
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

@media (max-width: 992px) {
  .page-container {
    padding: 12px;
  }

  .section-card {
    margin-bottom: 12px;
  }

  .filter-card {
    margin-bottom: 12px;
  }

  .chart-container + .chart-container {
    margin-top: 16px;
  }

  .pagination-container {
    justify-content: center;
  }
}
</style>
