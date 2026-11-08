<template>
  <div class="cart-management">
    <el-card class="box-card">
      <template #header>
        <div class="card-header">
          <span>购物车管理</span>
          <el-button type="primary" @click="handleExport">导出购物车</el-button>
        </div>
      </template>
      
      <!-- 搜索栏 -->
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="用户ID">
          <el-input v-model="searchForm.userId" placeholder="请输入用户ID" clearable />
        </el-form-item>
        <el-form-item label="商品名称">
          <el-input v-model="searchForm.goodsName" placeholder="请输入商品名称" clearable />
        </el-form-item>
        <el-form-item label="店铺名称">
          <el-input v-model="searchForm.shopName" placeholder="请输入店铺名称" clearable />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>

      <!-- 购物车列表 -->
      <el-table :data="cartList" style="width: 100%" v-loading="loading">
        <el-table-column prop="userId" label="用户ID" width="120" />
        <el-table-column prop="userName" label="用户名" width="120" />
        <el-table-column prop="shopName" label="店铺名称" width="150" />
        <el-table-column prop="goodsName" label="商品名称" width="200" />
        <el-table-column prop="goodsSpecChineseValue" label="规格" width="150" />
        <el-table-column prop="goodsPrice" label="单价" width="100">
          <template #default="scope">
            ¥{{ formatAmount(scope.row.goodsPrice) }}
          </template>
        </el-table-column>
        <el-table-column prop="goodsNum" label="数量" width="80" />
        <el-table-column prop="goodsStock" label="库存" width="80" />
        <el-table-column prop="insertCartTime" label="加入时间" width="180">
          <template #default="scope">
            {{ formatDate(scope.row.insertCartTime) }}
          </template>
        </el-table-column>
        <el-table-column prop="cartStatus" label="状态" width="100">
          <template #default="scope">
            <el-tag :type="getCartStatusTagType(scope.row.cartStatus)">
              {{ getCartStatusText(scope.row.cartStatus) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="scope">
            <el-button size="small" type="danger" @click="handleDelete(scope.row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination-container">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :page-sizes="[10, 20, 50, 100]"
          :total="total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>

    <!-- 批量操作 -->
    <el-card class="batch-operation" v-if="selectedCarts.length > 0">
      <div class="batch-info">
        <span>已选择 {{ selectedCarts.length }} 项</span>
        <div class="batch-buttons">
          <el-button type="danger" @click="handleBatchDelete">批量删除</el-button>
          <el-button @click="clearSelection">清空选择</el-button>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup>

// 使用 Element Plus 国际化系统
import { useI18n } from '@/i18n-element'
const { t } = useI18n()
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { orderApi } from '@/api/order/order'

// 响应式数据
const loading = ref(false)
const exportLoading = ref(false)
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const selectedCarts = ref([])

// 搜索表单
const searchForm = reactive({
  userId: '',
  goodsName: '',
  shopName: '',
  pageNum: 1,
  pageSize: 10
})

// 购物车列表
const cartList = ref([])

// 生命周期
onMounted(() => {
  fetchCartList()
})

// 获取购物车列表
const fetchCartList = async () => {
  loading.value = true
  try {
    const params = {
      ...searchForm,
      pageNum: currentPage.value,
      pageSize: pageSize.value
    }
    
    const response = await orderApi.getCartList(params)
    
    if (response.code === 200 || response.success) {
      const data = response.data || response
      cartList.value = data.records || data.list || []
      total.value = data.total || 0
    } else {
      ElMessage.error(response.message || '获取购物车列表失败')
      cartList.value = []
      total.value = 0
    }
  } catch (error) {
    console.error('获取购物车列表失败:', error)
    ElMessage.error('获取购物车列表失败: ' + (error.message || '网络错误'))
    cartList.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}

// 获取购物车状态标签类型
const getCartStatusTagType = (status) => {
  const statusMap = {
    0: 'success',
    1: 'danger',
    2: 'warning'
  }
  return statusMap[status] || 'info'
}

// 获取购物车状态文本
const getCartStatusText = (status) => {
  const statusMap = {
    0: '正常',
    1: '已删除',
    2: '商品下架'
  }
  return statusMap[status] || '未知'
}

// 格式化金额
const formatAmount = (amount) => {
  if (amount === null || amount === undefined) return '0.00'
  return Number(amount).toFixed(2)
}

// 格式化日期
const formatDate = (dateStr) => {
  if (!dateStr) return '-'
  const date = new Date(dateStr)
  if (isNaN(date.getTime())) return '-'
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit'
  })
}

// 搜索
const handleSearch = () => {
  currentPage.value = 1
  fetchCartList()
}

// 重置搜索
const handleReset = () => {
  Object.assign(searchForm, {
    userId: '',
    goodsName: '',
    shopName: '',
    pageNum: 1,
    pageSize: 10
  })
  currentPage.value = 1
  fetchCartList()
}

// 删除购物车商品
const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm('确定要删除这个购物车商品吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    const response = await orderApi.removeFromCart({ 
      cartIds: [row.cartId]
    })
    if (response.code === 200 || response.success) {
      ElMessage.success('删除成功')
      fetchCartList() // 刷新列表
    } else {
      ElMessage.error(response.message || '删除失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除失败:', error)
      ElMessage.error('删除失败: ' + (error.message || '网络错误'))
    }
  }
}

// 批量删除
const handleBatchDelete = async () => {
  if (selectedCarts.value.length === 0) {
    ElMessage.warning('请先选择要删除的商品')
    return
  }
  
  try {
    await ElMessageBox.confirm(`确定要删除选中的 ${selectedCarts.value.length} 个购物车商品吗？`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    const cartIds = selectedCarts.value.map(item => item.cartId)
    const response = await orderApi.removeFromCart({ cartIds })
    if (response.code === 200 || response.success) {
      ElMessage.success('批量删除成功')
      clearSelection()
      fetchCartList() // 刷新列表
    } else {
      ElMessage.error(response.message || '批量删除失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('批量删除失败:', error)
      ElMessage.error('批量删除失败: ' + (error.message || '网络错误'))
    }
  }
}

// 清空选择
const clearSelection = () => {
  selectedCarts.value = []
}

// 导出购物车
const handleExport = () => {
  ElMessage.info('导出功能开发中...')
}

// 分页大小改变
const handleSizeChange = (val) => {
  pageSize.value = val
  searchForm.pageSize = val
  fetchCartList()
}

// 当前页改变
const handleCurrentChange = (val) => {
  currentPage.value = val
  searchForm.pageNum = val
  fetchCartList()
}
</script>

<style scoped>
.cart-management {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.search-form {
  margin-bottom: 20px;
}

.pagination-container {
  margin-top: 20px;
  text-align: right;
}

.batch-operation {
  margin-top: 20px;
}

.batch-info {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.batch-buttons {
  display: flex;
  gap: 10px;
}
</style>
