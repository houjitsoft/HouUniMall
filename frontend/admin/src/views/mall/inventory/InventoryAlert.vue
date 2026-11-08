<template>
  <div class="page-container">
    <!-- 预警统计 -->
    <el-row :gutter="20" style="margin-bottom: 20px">
      <el-col :span="8">
        <el-card shadow="hover" class="alert-card danger">
          <div class="alert-content">
            <el-icon class="alert-icon" :size="40"><WarningFilled /></el-icon>
            <div class="alert-info">
              <div class="alert-label">严重预警</div>
              <div class="alert-value">{{ stats.danger }}</div>
              <div class="alert-desc">库存低于预警值50%</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="hover" class="alert-card warning">
          <div class="alert-content">
            <el-icon class="alert-icon" :size="40"><Warning /></el-icon>
            <div class="alert-info">
              <div class="alert-label">一般预警</div>
              <div class="alert-value">{{ stats.warning }}</div>
              <div class="alert-desc">库存低于预警值</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="hover" class="alert-card success">
          <div class="alert-content">
            <el-icon class="alert-icon" :size="40"><SuccessFilled /></el-icon>
            <div class="alert-info">
              <div class="alert-label">正常库存</div>
              <div class="alert-value">{{ stats.normal }}</div>
              <div class="alert-desc">库存充足</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 筛选条件 -->
    <el-card shadow="never" style="margin-bottom: 20px">
      <el-form :inline="true" :model="searchForm">
        <el-form-item label="预警级别">
          <el-select v-model="searchForm.alertLevel" placeholder="请选择预警级别" clearable>
            <el-option label="全部" value="" />
            <el-option label="严重预警" value="danger" />
            <el-option label="一般预警" value="warning" />
          </el-select>
        </el-form-item>
        <el-form-item label="商品名称">
          <el-input v-model="searchForm.goodsName" placeholder="请输入商品名称" clearable />
        </el-form-item>
        <el-form-item label="分类">
          <el-select v-model="searchForm.categoryId" placeholder="请选择分类" clearable>
            <el-option label="全部分类" value="" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" icon="Search" @click="handleSearch">查询</el-button>
          <el-button icon="Refresh" @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 预警列表 -->
    <el-card shadow="never">
      <template #header>
        <div style="display: flex; justify-content: space-between; align-items: center">
          <span class="card-title">库存预警列表</span>
          <el-button type="primary" icon="Setting" @click="handleSetting">预警设置</el-button>
        </div>
      </template>

      <el-table v-loading="loading" :data="tableData" border stripe>
        <el-table-column prop="goodsNo" label="商品编号" width="150" />
        <el-table-column prop="goodsName" label="商品名称" min-width="200" />
        <el-table-column prop="category" label="分类" width="120" />
        <el-table-column label="预警级别" width="120">
          <template #default="{ row }">
            <el-tag v-if="row.alertLevel === 'danger'" type="danger" effect="dark">
              严重预警
            </el-tag>
            <el-tag v-else-if="row.alertLevel === 'warning'" type="warning" effect="dark">
              一般预警
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="currentStock" label="当前库存" width="120" align="right">
          <template #default="{ row }">
            <span class="text-danger">{{ row.currentStock }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="alertStock" label="预警值" width="100" align="right" />
        <el-table-column label="预警比例" width="120">
          <template #default="{ row }">
            <el-progress 
              :percentage="Math.round((row.currentStock / row.alertStock) * 100)" 
              :color="row.alertLevel === 'danger' ? '#f56c6c' : '#e6a23c'"
              :stroke-width="16"
            />
          </template>
        </el-table-column>
        <el-table-column prop="updateTime" label="更新时间" width="180" />
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click="handlePurchase(row)">
              采购申请
            </el-button>
            <el-button link type="primary" size="small" @click="handleAdjust(row)">
              调整预警值
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

// 统计数据
const stats = reactive({
  danger: 0,
  warning: 0,
  normal: 0
})

// 搜索表单
const searchForm = reactive({
  alertLevel: '',
  goodsName: '',
  categoryId: ''
})

// 分页
const pagination = reactive({
  page: 1,
  size: 20,
  total: 0
})

const loading = ref(false)
const tableData = ref([])

// 获取统计数据
const fetchStats = async () => {
  try {
    stats.danger = 5
    stats.warning = 12
    stats.normal = 453
  } catch (error) {
    console.error('获取统计数据失败:', error)
  }
}

// 获取列表数据
const fetchData = async () => {
  loading.value = true
  try {
    // 模拟数据
    tableData.value = [
      {
        goodsNo: 'G20240103',
        goodsName: '小米14 Ultra 16GB+512GB',
        category: '电子产品',
        alertLevel: 'danger',
        currentStock: 8,
        alertStock: 30,
        updateTime: '2024-01-12 18:30:00'
      },
      {
        goodsNo: 'G20240104',
        goodsName: 'OPPO Find X7 Ultra 12GB+256GB',
        category: '电子产品',
        alertLevel: 'warning',
        currentStock: 25,
        alertStock: 30,
        updateTime: '2024-01-12 17:45:00'
      }
    ]
    pagination.total = 17
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
    alertLevel: '',
    goodsName: '',
    categoryId: ''
  })
  handleSearch()
}

// 预警设置
const handleSetting = () => {
  ElMessage.info('预警设置功能开发中')
}

// 采购申请
const handlePurchase = (row) => {
  ElMessage.success(`已创建采购申请：${row.goodsName}`)
}

// 调整预警值
const handleAdjust = (row) => {
  ElMessageBox.prompt('请输入新的预警值', '调整预警值', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    inputPattern: /^\d+$/,
    inputErrorMessage: '请输入有效的数字'
  }).then(({ value }) => {
    ElMessage.success(`预警值已调整为：${value}`)
    fetchData()
  })
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

.alert-card {
  border-radius: 8px;
  cursor: pointer;
  transition: transform 0.2s;
}

.alert-card:hover {
  transform: translateY(-4px);
}

.alert-card.danger {
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
  color: white;
}

.alert-card.warning {
  background: linear-gradient(135deg, #ffecd2 0%, #fcb69f 100%);
  color: #606266;
}

.alert-card.success {
  background: linear-gradient(135deg, #a8edea 0%, #fed6e3 100%);
  color: #606266;
}

.alert-content {
  display: flex;
  align-items: center;
  gap: 20px;
}

.alert-icon {
  opacity: 0.9;
}

.alert-info {
  flex: 1;
}

.alert-label {
  font-size: 14px;
  opacity: 0.9;
  margin-bottom: 8px;
}

.alert-value {
  font-size: 36px;
  font-weight: bold;
  margin-bottom: 4px;
}

.alert-desc {
  font-size: 12px;
  opacity: 0.8;
}

.card-title {
  font-weight: bold;
  font-size: 16px;
}

.text-danger {
  color: #f56c6c;
  font-weight: 600;
}
</style>
