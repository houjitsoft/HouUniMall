<template>
  <div class="page-container">
    <!-- 页面头部 -->
    <el-card shadow="never" style="margin-bottom: 20px">
      <div style="display: flex; justify-content: space-between; align-items: center">
        <div>
          <h3 style="margin: 0 0 8px 0">库存盘点</h3>
          <el-text type="info">定期盘点库存，确保账实相符</el-text>
        </div>
        <el-button type="primary" icon="Plus" @click="handleCreate">创建盘点单</el-button>
      </div>
    </el-card>

    <!-- 筛选条件 -->
    <el-card shadow="never" style="margin-bottom: 20px">
      <el-form :inline="true" :model="searchForm">
        <el-form-item label="盘点单号">
          <el-input v-model="searchForm.countNo" placeholder="请输入盘点单号" clearable />
        </el-form-item>
        <el-form-item label="盘点状态">
          <el-select v-model="searchForm.status" placeholder="请选择状态" clearable>
            <el-option label="全部" value="" />
            <el-option label="待盘点" value="pending" />
            <el-option label="盘点中" value="counting" />
            <el-option label="已完成" value="completed" />
            <el-option label="已取消" value="cancelled" />
          </el-select>
        </el-form-item>
        <el-form-item label="创建时间">
          <el-date-picker
            v-model="searchForm.dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            value-format="YYYY-MM-DD"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" icon="Search" @click="handleSearch">查询</el-button>
          <el-button icon="Refresh" @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 盘点单列表 -->
    <el-card shadow="never">
      <template #header>
        <span class="card-title">盘点单列表</span>
      </template>

      <el-table v-loading="loading" :data="tableData" border stripe>
        <el-table-column prop="countNo" label="盘点单号" width="180" />
        <el-table-column prop="title" label="盘点标题" min-width="200" />
        <el-table-column label="盘点状态" width="120">
          <template #default="{ row }">
            <el-tag v-if="row.status === 'pending'" type="info">待盘点</el-tag>
            <el-tag v-else-if="row.status === 'counting'" type="warning">盘点中</el-tag>
            <el-tag v-else-if="row.status === 'completed'" type="success">已完成</el-tag>
            <el-tag v-else type="info">已取消</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="goodsCount" label="商品数量" width="120" align="right" />
        <el-table-column prop="profitCount" label="盘盈数" width="100" align="right">
          <template #default="{ row }">
            <span v-if="row.profitCount > 0" class="text-success">+{{ row.profitCount }}</span>
            <span v-else>{{ row.profitCount }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="lossCount" label="盘亏数" width="100" align="right">
          <template #default="{ row }">
            <span v-if="row.lossCount > 0" class="text-danger">-{{ row.lossCount }}</span>
            <span v-else>{{ row.lossCount }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="creator" label="创建人" width="120" />
        <el-table-column prop="createTime" label="创建时间" width="180" />
        <el-table-column label="操作" width="240" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click="handleView(row)">
              查看
            </el-button>
            <el-button 
              v-if="row.status === 'pending'" 
              link 
              type="primary" 
              size="small" 
              @click="handleStart(row)"
            >
              开始盘点
            </el-button>
            <el-button 
              v-if="row.status === 'counting'" 
              link 
              type="success" 
              size="small" 
              @click="handleComplete(row)"
            >
              完成盘点
            </el-button>
            <el-button 
              v-if="row.status === 'pending'" 
              link 
              type="danger" 
              size="small" 
              @click="handleCancel(row)"
            >
              取消
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
import { ElMessage, ElMessageBox } from 'element-plus'

// 搜索表单
const searchForm = reactive({
  countNo: '',
  status: '',
  dateRange: []
})

// 分页
const pagination = reactive({
  page: 1,
  size: 20,
  total: 0
})

const loading = ref(false)
const tableData = ref([])

// 获取列表数据
const fetchData = async () => {
  loading.value = true
  try {
    // 模拟数据
    tableData.value = [
      {
        countNo: 'PD20240112001',
        title: '2024年1月库存盘点',
        status: 'completed',
        goodsCount: 156,
        profitCount: 12,
        lossCount: 5,
        creator: '张三',
        createTime: '2024-01-12 10:00:00'
      },
      {
        countNo: 'PD20240111002',
        title: '电子产品专项盘点',
        status: 'counting',
        goodsCount: 45,
        profitCount: 0,
        lossCount: 0,
        creator: '李四',
        createTime: '2024-01-11 14:30:00'
      },
      {
        countNo: 'PD20240110003',
        title: '服装鞋帽库存盘点',
        status: 'pending',
        goodsCount: 89,
        profitCount: 0,
        lossCount: 0,
        creator: '王五',
        createTime: '2024-01-10 09:15:00'
      }
    ]
    pagination.total = 25
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
    countNo: '',
    status: '',
    dateRange: []
  })
  handleSearch()
}

// 创建盘点单
const handleCreate = () => {
  ElMessage.info('创建盘点单功能开发中')
}

// 查看详情
const handleView = (row) => {
  ElMessage.info(`查看盘点单详情：${row.countNo}`)
}

// 开始盘点
const handleStart = (row) => {
  ElMessageBox.confirm(
    `确定要开始盘点"${row.title}"吗？`,
    '确认操作',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(() => {
    ElMessage.success('盘点已开始')
    fetchData()
  })
}

// 完成盘点
const handleComplete = (row) => {
  ElMessageBox.confirm(
    `确定要完成盘点"${row.title}"吗？完成后将自动调整库存。`,
    '确认操作',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(() => {
    ElMessage.success('盘点已完成，库存已调整')
    fetchData()
  })
}

// 取消盘点
const handleCancel = (row) => {
  ElMessageBox.confirm(
    `确定要取消盘点"${row.title}"吗？`,
    '确认操作',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(() => {
    ElMessage.success('盘点已取消')
    fetchData()
  })
}

onMounted(() => {
  fetchData()
})
</script>

<style scoped>
.page-container {
  padding: 20px;
}

.card-title {
  font-weight: bold;
  font-size: 16px;
}

.text-success {
  color: #67c23a;
  font-weight: 600;
}

.text-danger {
  color: #f56c6c;
  font-weight: 600;
}
</style>
