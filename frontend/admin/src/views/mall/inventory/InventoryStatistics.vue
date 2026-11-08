<template>
  <div class="page-container">
    <!-- 统计卡片 -->
    <el-row :gutter="20" style="margin-bottom: 20px">
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-content">
            <div class="stat-icon" style="background: linear-gradient(135deg, #667eea 0%, #764ba2 100%)">
              <el-icon :size="32"><Box /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-label">库存总量</div>
              <div class="stat-value">{{ stats.totalStock }}</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-content">
            <div class="stat-icon" style="background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%)">
              <el-icon :size="32"><WarningFilled /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-label">预警商品</div>
              <div class="stat-value">{{ stats.alertCount }}</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-content">
            <div class="stat-icon" style="background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%)">
              <el-icon :size="32"><TrendCharts /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-label">今日出库</div>
              <div class="stat-value">{{ stats.todayOutbound }}</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-content">
            <div class="stat-icon" style="background: linear-gradient(135deg, #43e97b 0%, #38f9d7 100%)">
              <el-icon :size="32"><DocumentAdd /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-label">今日入库</div>
              <div class="stat-value">{{ stats.todayInbound }}</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 筛选条件 -->
    <el-card shadow="never" style="margin-bottom: 20px">
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
        <el-form-item label="商品名称">
          <el-input v-model="searchForm.goodsName" placeholder="请输入商品名称" clearable />
        </el-form-item>
        <el-form-item label="分类">
          <el-select v-model="searchForm.categoryId" placeholder="请选择分类" clearable>
            <el-option label="全部分类" value="" />
            <el-option label="电子产品" value="1" />
            <el-option label="服装鞋帽" value="2" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" icon="Search" @click="handleSearch">查询</el-button>
          <el-button icon="Refresh" @click="handleReset">重置</el-button>
          <el-button type="success" icon="Download" @click="handleExport">导出</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 库存统计表格 -->
    <el-card shadow="never">
      <template #header>
        <span class="card-title">库存统计明细</span>
      </template>
      
      <el-table v-loading="loading" :data="tableData" border stripe>
        <el-table-column prop="goodsNo" label="商品编号" width="150" />
        <el-table-column prop="goodsName" label="商品名称" min-width="200" />
        <el-table-column prop="category" label="分类" width="120" />
        <el-table-column prop="currentStock" label="当前库存" width="120" align="right">
          <template #default="{ row }">
            <span :class="row.currentStock < row.alertStock ? 'text-danger' : ''">
              {{ row.currentStock }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="alertStock" label="预警值" width="100" align="right" />
        <el-table-column prop="totalInbound" label="入库总量" width="120" align="right" />
        <el-table-column prop="totalOutbound" label="出库总量" width="120" align="right" />
        <el-table-column prop="stockValue" label="库存价值(元)" width="140" align="right">
          <template #default="{ row }">
            <span class="price">¥{{ row.stockValue }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="updateTime" label="更新时间" width="180" />
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click="handleViewDetail(row)">
              查看详情
            </el-button>
            <el-button link type="primary" size="small" @click="handleViewFlow(row)">
              流水记录
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        v-model:current-page="pagination.page"
        v-model:page-size="pagination.size"
        :total="pagination.total"
        :page-sizes="[10, 20, 50, 100]"
        layout="total, sizes, prev, pager, next, jumper"
        style="margin-top: 20px; justify-content: flex-end"
        @size-change="fetchData"
        @current-change="fetchData"
      />
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'

// 统计数据
const stats = reactive({
  totalStock: 0,
  alertCount: 0,
  todayOutbound: 0,
  todayInbound: 0
})

// 搜索表单
const searchForm = reactive({
  dateRange: [],
  goodsName: '',
  categoryId: ''
})

// 分页
const pagination = reactive({
  page: 1,
  size: 20,
  total: 0
})

// 加载状态
const loading = ref(false)

// 表格数据
const tableData = ref([])

// 获取统计数据
const fetchStats = async () => {
  try {
    // TODO: 调用接口获取统计数据
    stats.totalStock = 15680
    stats.alertCount = 8
    stats.todayOutbound = 245
    stats.todayInbound = 356
  } catch (error) {
    console.error('获取统计数据失败:', error)
  }
}

// 获取表格数据
const fetchData = async () => {
  loading.value = true
  try {
    // TODO: 调用接口获取数据
    // 模拟数据
    tableData.value = [
      {
        goodsNo: 'G20240101',
        goodsName: 'iPhone 15 Pro Max 256GB',
        category: '电子产品',
        currentStock: 156,
        alertStock: 50,
        totalInbound: 500,
        totalOutbound: 344,
        stockValue: '1248000.00',
        updateTime: '2024-01-12 18:30:00'
      },
      {
        goodsNo: 'G20240102',
        goodsName: '华为Mate 60 Pro 512GB',
        category: '电子产品',
        currentStock: 23,
        alertStock: 30,
        totalInbound: 200,
        totalOutbound: 177,
        stockValue: '138000.00',
        updateTime: '2024-01-12 17:45:00'
      }
    ]
    pagination.total = 50
  } catch (error) {
    console.error('获取数据失败:', error)
    ElMessage.error('获取数据失败')
  } finally {
    loading.value = false
  }
}

// 查询
const handleSearch = () => {
  pagination.page = 1
  fetchData()
}

// 重置
const handleReset = () => {
  Object.assign(searchForm, {
    dateRange: [],
    goodsName: '',
    categoryId: ''
  })
  handleSearch()
}

// 导出
const handleExport = () => {
  ElMessage.success('导出功能开发中')
}

// 查看详情
const handleViewDetail = (row) => {
  ElMessage.info(`查看商品详情：${row.goodsName}`)
}

// 查看流水
const handleViewFlow = (row) => {
  ElMessage.info(`查看流水记录：${row.goodsName}`)
}

onMounted(() => {
  fetchStats()
  fetchData()
})
</script>

<style scoped>
.page-container {
  padding: 20px;
}

.stat-card {
  border-radius: 8px;
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
}

.card-title {
  font-weight: bold;
  font-size: 16px;
}

.price {
  color: #f56c6c;
  font-weight: 500;
}

.text-danger {
  color: #f56c6c;
  font-weight: 600;
}
</style>
