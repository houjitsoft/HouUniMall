<template>
  <div class="goods-review-container">
    <el-card shadow="never">
      <template #header>
        <div class="card-header">
          <span>商品评价管理</span>
        </div>
      </template>
      
      <!-- 搜索区域 -->
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="商品名称">
          <el-input v-model="searchForm.goodsName" placeholder="请输入商品名称" clearable />
        </el-form-item>
        <el-form-item label="评价星级">
          <el-select v-model="searchForm.rating" placeholder="请选择" clearable>
            <el-option label="5星" :value="5" />
            <el-option label="4星" :value="4" />
            <el-option label="3星" :value="3" />
            <el-option label="2星" :value="2" />
            <el-option label="1星" :value="1" />
          </el-select>
        </el-form-item>
        <el-form-item label="审核状态">
          <el-select v-model="searchForm.status" placeholder="请选择" clearable>
            <el-option label="待审核" value="pending" />
            <el-option label="已通过" value="approved" />
            <el-option label="已拒绝" value="rejected" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>

      <!-- 表格 -->
      <el-table :data="tableData" v-loading="loading" stripe border>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="goodsName" label="商品名称" min-width="150" />
        <el-table-column prop="userName" label="评价用户" width="120" />
        <el-table-column label="评价星级" width="150">
          <template #default="{ row }">
            <el-rate v-model="row.rating" disabled />
          </template>
        </el-table-column>
        <el-table-column prop="content" label="评价内容" min-width="200" show-overflow-tooltip />
        <el-table-column label="评价图片" width="120">
          <template #default="{ row }">
            <el-image v-if="row.images && row.images.length" 
              :src="row.images[0]" 
              :preview-src-list="row.images"
              fit="cover"
              style="width: 50px; height: 50px" />
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">{{ getStatusText(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="评价时间" width="160" />
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleView(row)">查看</el-button>
            <el-button type="success" link @click="handleApprove(row)" v-if="row.status === 'pending'">通过</el-button>
            <el-button type="danger" link @click="handleReject(row)" v-if="row.status === 'pending'">拒绝</el-button>
            <el-button type="danger" link @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <el-pagination
        class="pagination"
        v-model:current-page="pagination.page"
        v-model:page-size="pagination.size"
        :total="pagination.total"
        :page-sizes="[10, 20, 50, 100]"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
      />
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'

const loading = ref(false)
const tableData = ref([])
const searchForm = reactive({
  goodsName: '',
  rating: null,
  status: ''
})
const pagination = reactive({
  page: 1,
  size: 10,
  total: 0
})

const getStatusType = (status) => {
  const types = { pending: 'warning', approved: 'success', rejected: 'danger' }
  return types[status] || 'info'
}

const getStatusText = (status) => {
  const texts = { pending: '待审核', approved: '已通过', rejected: '已拒绝' }
  return texts[status] || status
}

const fetchData = async () => {
  loading.value = true
  try {
    // TODO: 调用API获取数据
    tableData.value = []
    pagination.total = 0
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  pagination.page = 1
  fetchData()
}

const handleReset = () => {
  searchForm.goodsName = ''
  searchForm.rating = null
  searchForm.status = ''
  handleSearch()
}

const handleView = (row) => {
  ElMessage.info('查看评价详情: ' + row.id)
}

const handleApprove = (row) => {
  ElMessageBox.confirm('确定通过该评价吗?', '提示').then(() => {
    ElMessage.success('审核通过')
    fetchData()
  })
}

const handleReject = (row) => {
  ElMessageBox.confirm('确定拒绝该评价吗?', '提示').then(() => {
    ElMessage.success('已拒绝')
    fetchData()
  })
}

const handleDelete = (row) => {
  ElMessageBox.confirm('确定删除该评价吗?', '警告', { type: 'warning' }).then(() => {
    ElMessage.success('删除成功')
    fetchData()
  })
}

const handleSizeChange = () => fetchData()
const handleCurrentChange = () => fetchData()

onMounted(() => {
  fetchData()
})
</script>

<style scoped>
.goods-review-container {
  padding: 20px;
}
.card-header {
  font-weight: bold;
}
.search-form {
  margin-bottom: 20px;
}
.pagination {
  margin-top: 20px;
  justify-content: flex-end;
}
</style>
