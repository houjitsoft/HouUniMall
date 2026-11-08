<template>
  <div class="page-container">
    <!-- 统计卡片 -->
    <el-row :gutter="20" class="stats-row">
      <el-col :span="6">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-content">
            <div class="stat-icon" style="background: linear-gradient(135deg, #667eea 0%, #764ba2 100%)">
              <el-icon :size="32"><DocumentCopy /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-label">待处理工单</div>
              <div class="stat-value">{{ stats.pending }}</div>
              <div class="stat-desc">需及时处理</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-content">
            <div class="stat-icon" style="background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%)">
              <el-icon :size="32"><Clock /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-label">处理中工单</div>
              <div class="stat-value">{{ stats.processing }}</div>
              <div class="stat-desc">实时跟进</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-content">
            <div class="stat-icon" style="background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%)">
              <el-icon :size="32"><CircleCheckFilled /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-label">已完成工单</div>
              <div class="stat-value">{{ stats.completed }}</div>
              <div class="stat-desc">本月完成</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-content">
            <div class="stat-icon" style="background: linear-gradient(135deg, #fa709a 0%, #fee140 100%)">
              <el-icon :size="32"><TrendCharts /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-label">平均处理时长</div>
              <div class="stat-value">{{ stats.avgTime }}h</div>
              <div class="stat-desc">比上月↓15%</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 服务类型统计 -->
    <el-row :gutter="20" class="charts-row">
      <el-col :span="12">
        <el-card shadow="never">
          <template #header>
            <span class="card-title">服务类型分布</span>
          </template>
          <el-table :data="typeStats" border stripe>
            <el-table-column prop="type" label="服务类型" width="120">
              <template #default="{ row }">
                <el-tag :type="getTypeTag(row.type)">
                  {{ getTypeText(row.type) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="count" label="工单数" width="100" align="right">
              <template #default="{ row }">
                <span class="number-highlight">{{ row.count }}</span>
              </template>
            </el-table-column>
            <el-table-column label="占比" width="150">
              <template #default="{ row }">
                <el-progress
                  :percentage="row.percentage"
                  :color="getProgressColor(row.percentage)"
                  :stroke-width="12"
                />
              </template>
            </el-table-column>
            <el-table-column prop="avgTime" label="平均时长" align="center">
              <template #default="{ row }">
                <span>{{ row.avgTime }}小时</span>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card shadow="never">
          <template #header>
            <span class="card-title">服务满意度统计</span>
          </template>
          <div class="satisfaction-stats">
            <div class="satisfaction-item">
              <div class="satisfaction-label">非常满意</div>
              <el-progress
                :percentage="satisfactionStats.verySatisfied"
                color="#67c23a"
                :stroke-width="20"
              />
              <div class="satisfaction-value">{{ satisfactionStats.verySatisfied }}%</div>
            </div>
            <div class="satisfaction-item">
              <div class="satisfaction-label">满意</div>
              <el-progress
                :percentage="satisfactionStats.satisfied"
                color="#409eff"
                :stroke-width="20"
              />
              <div class="satisfaction-value">{{ satisfactionStats.satisfied }}%</div>
            </div>
            <div class="satisfaction-item">
              <div class="satisfaction-label">一般</div>
              <el-progress
                :percentage="satisfactionStats.normal"
                color="#e6a23c"
                :stroke-width="20"
              />
              <div class="satisfaction-value">{{ satisfactionStats.normal }}%</div>
            </div>
            <div class="satisfaction-item">
              <div class="satisfaction-label">不满意</div>
              <el-progress
                :percentage="satisfactionStats.unsatisfied"
                color="#f56c6c"
                :stroke-width="20"
              />
              <div class="satisfaction-value">{{ satisfactionStats.unsatisfied }}%</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 筛选条件 -->
    <el-card shadow="never" class="filter-card">
      <el-form :inline="true" :model="searchForm">
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

    <!-- 服务统计明细 -->
    <el-card shadow="never">
      <template #header>
        <span class="card-title">服务统计明细</span>
      </template>
      <el-table :data="tableData" border stripe v-loading="loading">
        <el-table-column prop="date" label="日期" width="120" />
        <el-table-column prop="totalCount" label="总工单数" width="100" align="right">
          <template #default="{ row }">
            <span class="number-highlight">{{ row.totalCount }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="returnCount" label="退货" width="80" align="right" />
        <el-table-column prop="exchangeCount" label="换货" width="80" align="right" />
        <el-table-column prop="repairCount" label="维修" width="80" align="right" />
        <el-table-column prop="completedCount" label="已完成" width="100" align="right">
          <template #default="{ row }">
            <span style="color: #67c23a; font-weight: bold">{{ row.completedCount }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="completionRate" label="完成率" width="120">
          <template #default="{ row }">
            <el-progress
              :percentage="row.completionRate"
              :color="getCompletionColor(row.completionRate)"
              :stroke-width="12"
            />
          </template>
        </el-table-column>
        <el-table-column prop="avgResponseTime" label="平均响应时长" width="130" align="center">
          <template #default="{ row }">
            <el-tag :type="getTimeTag(row.avgResponseTime)" size="small">
              {{ row.avgResponseTime }}小时
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="avgProcessTime" label="平均处理时长" width="130" align="center">
          <template #default="{ row }">
            <el-tag :type="getTimeTag(row.avgProcessTime)" size="small">
              {{ row.avgProcessTime }}小时
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="satisfactionRate" label="满意度" width="100">
          <template #default="{ row }">
            <el-rate
              v-model="row.satisfactionRate"
              disabled
              show-score
              score-template="{value}"
            />
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
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import {
  DocumentCopy, Clock, CircleCheckFilled, TrendCharts,
  Search, RefreshRight, Download
} from '@element-plus/icons-vue'

const loading = ref(false)

// 统计数据
const stats = reactive({
  pending: 45,
  processing: 128,
  completed: 856,
  avgTime: 12.5
})

// 服务类型统计
const typeStats = ref([
  { type: 'return', count: 523, percentage: 52, avgTime: 15.2 },
  { type: 'exchange', count: 312, percentage: 31, avgTime: 10.8 },
  { type: 'repair', count: 165, percentage: 17, avgTime: 18.5 }
])

// 满意度统计
const satisfactionStats = reactive({
  verySatisfied: 68,
  satisfied: 25,
  normal: 5,
  unsatisfied: 2
})

// 搜索表单
const searchForm = reactive({
  dateRange: [],
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

// 获取数据
const fetchData = async () => {
  loading.value = true
  try {
    // TODO: 调用接口获取数据
    // 模拟数据
    tableData.value = [
      {
        date: '2024-01-12',
        totalCount: 45,
        returnCount: 23,
        exchangeCount: 15,
        repairCount: 7,
        completedCount: 42,
        completionRate: 93,
        avgResponseTime: 2.5,
        avgProcessTime: 12.3,
        satisfactionRate: 4.5
      },
      {
        date: '2024-01-11',
        totalCount: 52,
        returnCount: 28,
        exchangeCount: 18,
        repairCount: 6,
        completedCount: 50,
        completionRate: 96,
        avgResponseTime: 1.8,
        avgProcessTime: 10.5,
        satisfactionRate: 4.7
      }
    ]
    pagination.total = 30
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
    type: '',
    status: ''
  })
  handleSearch()
}

// 导出
const handleExport = () => {
  ElMessage.success('导出成功')
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

// 获取进度条颜色
const getProgressColor = (percentage) => {
  if (percentage >= 40) return '#67c23a'
  if (percentage >= 20) return '#e6a23c'
  return '#909399'
}

// 获取完成率颜色
const getCompletionColor = (rate) => {
  if (rate >= 90) return '#67c23a'
  if (rate >= 70) return '#e6a23c'
  return '#f56c6c'
}

// 获取时长标签
const getTimeTag = (time) => {
  if (time <= 2) return 'success'
  if (time <= 12) return ''
  if (time <= 24) return 'warning'
  return 'danger'
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

.stat-desc {
  font-size: 12px;
  color: #c0c4cc;
}

.charts-row {
  margin-bottom: 20px;
}

.card-title {
  font-size: 16px;
  font-weight: bold;
  color: #303133;
}

.number-highlight {
  color: #409eff;
  font-weight: bold;
}

.satisfaction-stats {
  padding: 20px 0;
}

.satisfaction-item {
  display: flex;
  align-items: center;
  gap: 15px;
  margin-bottom: 20px;
}

.satisfaction-label {
  width: 80px;
  font-size: 14px;
  color: #606266;
}

.satisfaction-value {
  width: 50px;
  text-align: right;
  font-size: 14px;
  font-weight: bold;
  color: #303133;
}

.filter-card {
  margin-bottom: 20px;
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}
</style>
