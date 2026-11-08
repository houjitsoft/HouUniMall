<template>
  <div class="domestic-replace-container">
    <!-- 搜索筛选区域 -->
    <el-card class="search-card">
      <el-form :model="searchForm" inline>
        <el-form-item label="产品名称：">
          <el-input 
            v-model="searchForm.productName" 
            placeholder="请输入产品名称"
            clearable
            style="width: 200px"
          />
        </el-form-item>
        <el-form-item label="店铺名称：">
          <el-input 
            v-model="searchForm.shopName" 
            placeholder="请输入店铺名称"
            clearable
            style="width: 200px"
          />
        </el-form-item>
        <el-form-item label="创建人：">
          <el-input 
            v-model="searchForm.creator" 
            placeholder="请输入创建人"
            clearable
            style="width: 200px"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">
            查询
          </el-button>
          <el-button @click="handleReset">
            重置
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 数据列表 -->
    <el-card class="table-card">
      <el-table 
        v-loading="loading"
        :data="tableData" 
        style="width: 100%"
      >
        <el-table-column label="操作" width="120" fixed="left">
          <template #default="{ row }">
            <el-button 
              link 
              type="primary" 
              @click="handleViewReplacement(row)"
            >
              查看替代品
            </el-button>
          </template>
        </el-table-column>
        <el-table-column prop="productName" label="产品名称" min-width="150" show-overflow-tooltip />
        <el-table-column prop="category" label="产品类别" min-width="200" show-overflow-tooltip />
        <el-table-column prop="manufacturer" label="生产制造商" width="150">
          <template #default="{ row }">
            {{ row.manufacturer || '—' }}
          </template>
        </el-table-column>
        <el-table-column prop="creator" label="创建人" width="150" />
        <el-table-column prop="updateTime" label="更新时间" width="180" />
        <el-table-column prop="createTime" label="创建时间" width="180" />
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
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>

    <!-- 查看替代品弹窗 -->
    <el-dialog
      v-model="dialogVisible"
      title="查看替代品"
      width="80%"
      :close-on-click-modal="false"
    >
      <el-table 
        :data="replacementList" 
        style="width: 100%"
        max-height="400"
      >
        <el-table-column prop="productName" label="产品名称" min-width="150" />
        <el-table-column prop="category" label="产品类别" min-width="200" show-overflow-tooltip />
        <el-table-column prop="manufacturer" label="生产制造商" width="150">
          <template #default="{ row }">
            {{ row.manufacturer || '—' }}
          </template>
        </el-table-column>
        <el-table-column prop="creator" label="创建人" width="150" />
        <el-table-column prop="createTime" label="创建时间" width="180" />
      </el-table>

      <!-- 弹窗内分页 -->
      <div class="dialog-pagination">
        <el-pagination
          v-model:current-page="dialogPagination.page"
          v-model:page-size="dialogPagination.size"
          :page-sizes="[10, 20, 50]"
          :total="dialogPagination.total"
          layout="total, sizes, prev, pager, next, jumper"
          small
          @size-change="handleDialogSizeChange"
          @current-change="handleDialogPageChange"
        />
      </div>

      <template #footer>
        <el-button type="primary" @click="dialogVisible = false">
          确定
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { domesticReplaceService } from '@/api/goods/goods.js'

// 响应式数据
const loading = ref(false)
const dialogVisible = ref(false)
const currentRow = ref(null)

// 搜索表单
const searchForm = reactive({
  productName: '',
  shopName: '',
  creator: ''
})

// 分页信息
const pagination = reactive({
  page: 1,
  size: 10,
  total: 0
})

// 弹窗分页信息
const dialogPagination = reactive({
  page: 1,
  size: 10,
  total: 0
})

// 列表数据
const tableData = ref([])

// 替代品列表
const replacementList = ref([])

// 方法
const handleSearch = () => {
  pagination.page = 1
  fetchTableData()
}

const handleReset = () => {
  Object.assign(searchForm, {
    productName: '',
    shopName: '',
    creator: ''
  })
  handleSearch()
}

const handleViewReplacement = (row) => {
  currentRow.value = row
  dialogVisible.value = true
  dialogPagination.page = 1
  fetchReplacementList(row.id)
}

const handleSizeChange = (newSize) => {
  pagination.size = newSize
  fetchTableData()
}

const handleCurrentChange = (newPage) => {
  pagination.page = newPage
  fetchTableData()
}

const handleDialogSizeChange = (newSize) => {
  dialogPagination.size = newSize
  if (currentRow.value) {
    fetchReplacementList(currentRow.value.id)
  }
}

const handleDialogPageChange = (newPage) => {
  dialogPagination.page = newPage
  if (currentRow.value) {
    fetchReplacementList(currentRow.value.id)
  }
}

// 获取列表数据
const fetchTableData = async () => {
  loading.value = true
  try {
    const params = {
      page: pagination.page,
      size: pagination.size,
      ...searchForm
    }
    const res = await domesticReplaceService.getList(params)
    
    if (res && (res.code === 1 || res.code === 200)) {
      tableData.value = res.data?.list || res.data?.records || res.data || []
      pagination.total = res.data?.totalCount || res.data?.total || 0
    } else {
      ElMessage.error(res.message || '获取数据失败')
    }
  } catch (error) {
    console.error('获取列表数据失败:', error)
    // 如果是“用户未登录”错误，响应拦截器已经处理了跳转，不需要显示错误消息
    if (error.message && (
      error.message.includes('用户未登录') || 
      error.message.includes('未登录') ||
      error.message.includes('token') ||
      error.message.includes('Token')
    )) {
      return
    }
    ElMessage.error(error.message || '获取数据失败')
  } finally {
    loading.value = false
  }
}

// 获取替代品列表
const fetchReplacementList = async (productId) => {
  try {
    const params = {
      productId,
      page: dialogPagination.page,
      size: dialogPagination.size
    }
    const res = await domesticReplaceService.getReplacementList(params)
    
    if (res && (res.code === 1 || res.code === 200)) {
      replacementList.value = res.data?.list || res.data?.records || res.data || []
      dialogPagination.total = res.data?.totalCount || res.data?.total || 0
    } else {
      ElMessage.error(res.message || '获取替代品列表失败')
    }
  } catch (error) {
    console.error('获取替代品列表失败:', error)
    // 如果是“用户未登录”错误，响应拦截器已经处理了跳转，不需要显示错误消息
    if (error.message && (
      error.message.includes('用户未登录') || 
      error.message.includes('未登录') ||
      error.message.includes('token') ||
      error.message.includes('Token')
    )) {
      return
    }
    ElMessage.error(error.message || '获取替代品列表失败')
  }
}

// 初始化
onMounted(() => {
  fetchTableData()
})
</script>

<style scoped>
.domestic-replace-container {
  padding: 20px;
}

.search-card {
  margin-bottom: 20px;
}

.table-card {
  margin-bottom: 20px;
}

.pagination-container {
  display: flex;
  justify-content: center;
  margin-top: 20px;
}

.dialog-pagination {
  display: flex;
  justify-content: center;
  margin-top: 20px;
  padding-top: 15px;
  border-top: 1px solid #EBEEF5;
}
</style>
