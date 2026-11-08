<template>
  <div class="page-container">
    <el-card shadow="never">
      <template #header>
        <div class="card-header">
          <span class="card-title">客户标签</span>
          <el-button type="primary" @click="handleAdd">新增标签</el-button>
        </div>
      </template>

      <!-- 搜索表单 -->
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="标签名称">
          <el-input v-model="searchForm.name" placeholder="请输入标签名称" clearable />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>

      <!-- 数据表格 -->
      <el-table :data="tableData" v-loading="loading" border stripe>
        <el-table-column prop="id" label="标签ID" width="80" />
        <el-table-column prop="name" label="标签名称" min-width="150" />
        <el-table-column label="标签颜色" width="120">
          <template #default="{ row }">
            <el-tag :color="row.color" style="border: none;">{{ row.name }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="customerCount" label="客户数量" width="100" />
        <el-table-column prop="description" label="标签描述" min-width="200" show-overflow-tooltip />
        <el-table-column prop="createTime" label="创建时间" width="160" />
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button text type="primary" size="small" @click="handleEdit(row)">编辑</el-button>
            <el-button text type="primary" size="small" @click="handleViewCustomers(row)">查看客户</el-button>
            <el-button text type="danger" size="small" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <el-pagination
        v-model:current-page="pagination.page"
        v-model:page-size="pagination.size"
        :total="pagination.total"
        :page-sizes="[10, 20, 50, 100]"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="fetchData"
        @current-change="fetchData"
        class="pagination"
      />
    </el-card>

    <!-- 编辑对话框 -->
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="500px">
      <el-form :model="form" label-width="100px">
        <el-form-item label="标签名称">
          <el-input v-model="form.name" placeholder="请输入标签名称" />
        </el-form-item>
        <el-form-item label="标签颜色">
          <el-color-picker v-model="form.color" />
        </el-form-item>
        <el-form-item label="标签描述">
          <el-input v-model="form.description" type="textarea" :rows="3" placeholder="请输入标签描述" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'

const loading = ref(false)
const dialogVisible = ref(false)
const dialogTitle = ref('新增标签')

const searchForm = reactive({
  name: ''
})

const form = reactive({
  id: null,
  name: '',
  color: '#409EFF',
  description: ''
})

const pagination = reactive({
  page: 1,
  size: 10,
  total: 0
})

const tableData = ref([
  { id: 1, name: '高价值客户', color: '#f56c6c', customerCount: 128, description: '消费金额超过10000元的客户', createTime: '2024-01-01 10:00:00' },
  { id: 2, name: '活跃客户', color: '#67c23a', customerCount: 356, description: '最近30天有购买行为的客户', createTime: '2024-01-02 11:00:00' },
  { id: 3, name: '潜在客户', color: '#e6a23c', customerCount: 89, description: '有浏览记录但未下单的客户', createTime: '2024-01-03 12:00:00' },
  { id: 4, name: '流失客户', color: '#909399', customerCount: 45, description: '超过90天未购买的客户', createTime: '2024-01-04 13:00:00' }
])

const fetchData = async () => {
  loading.value = true
  setTimeout(() => {
    pagination.total = tableData.value.length
    loading.value = false
  }, 500)
}

const handleSearch = () => {
  pagination.page = 1
  fetchData()
}

const handleReset = () => {
  searchForm.name = ''
  fetchData()
}

const handleAdd = () => {
  dialogTitle.value = '新增标签'
  Object.assign(form, { id: null, name: '', color: '#409EFF', description: '' })
  dialogVisible.value = true
}

const handleEdit = (row) => {
  dialogTitle.value = '编辑标签'
  Object.assign(form, row)
  dialogVisible.value = true
}

const handleViewCustomers = (row) => {
  ElMessage.info('查看标签客户：' + row.name)
}

const handleDelete = (row) => {
  ElMessageBox.confirm('确认删除该标签？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    ElMessage.success('删除成功')
    fetchData()
  })
}

const handleSubmit = () => {
  ElMessage.success('保存成功')
  dialogVisible.value = false
  fetchData()
}

onMounted(() => {
  fetchData()
})
</script>

<style scoped>
.page-container { padding: 20px; }
.card-header { display: flex; justify-content: space-between; align-items: center; }
.card-title { font-weight: bold; font-size: 16px; }
.search-form { margin-bottom: 20px; }
.pagination { margin-top: 20px; justify-content: flex-end; }
</style>
