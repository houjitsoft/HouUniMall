<template>
  <div class="page-container">
    <el-card shadow="never">
      <template #header>
        <div class="card-header">
          <span class="card-title">客户跟进</span>
          <el-button type="primary" @click="handleAdd">新增跟进</el-button>
        </div>
      </template>

      <!-- 搜索表单 -->
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="客户名称">
          <el-input v-model="searchForm.customerName" placeholder="请输入客户名称" clearable />
        </el-form-item>
        <el-form-item label="跟进人">
          <el-input v-model="searchForm.follower" placeholder="请输入跟进人" clearable />
        </el-form-item>
        <el-form-item label="跟进类型">
          <el-select v-model="searchForm.type" placeholder="请选择" clearable>
            <el-option label="电话沟通" value="phone" />
            <el-option label="邮件联系" value="email" />
            <el-option label="上门拜访" value="visit" />
            <el-option label="其他" value="other" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>

      <!-- 数据表格 -->
      <el-table :data="tableData" v-loading="loading" border stripe>
        <el-table-column prop="id" label="跟进ID" width="80" />
        <el-table-column prop="customerName" label="客户名称" min-width="120" />
        <el-table-column prop="phone" label="客户电话" width="130" />
        <el-table-column label="跟进类型" width="100">
          <template #default="{ row }">
            <el-tag :type="getTypeColor(row.type)">{{ getTypeText(row.type) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="content" label="跟进内容" min-width="200" show-overflow-tooltip />
        <el-table-column prop="follower" label="跟进人" width="100" />
        <el-table-column prop="followTime" label="跟进时间" width="160" />
        <el-table-column prop="nextFollowTime" label="下次跟进" width="160" />
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button text type="primary" size="small" @click="handleView(row)">查看</el-button>
            <el-button text type="primary" size="small" @click="handleEdit(row)">编辑</el-button>
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
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="600px">
      <el-form :model="form" label-width="100px">
        <el-form-item label="客户名称">
          <el-select v-model="form.customerId" placeholder="请选择客户" filterable>
            <el-option label="张三" :value="1" />
            <el-option label="李四" :value="2" />
            <el-option label="王五" :value="3" />
          </el-select>
        </el-form-item>
        <el-form-item label="跟进类型">
          <el-select v-model="form.type" placeholder="请选择">
            <el-option label="电话沟通" value="phone" />
            <el-option label="邮件联系" value="email" />
            <el-option label="上门拜访" value="visit" />
            <el-option label="其他" value="other" />
          </el-select>
        </el-form-item>
        <el-form-item label="跟进内容">
          <el-input v-model="form.content" type="textarea" :rows="4" placeholder="请输入跟进内容" />
        </el-form-item>
        <el-form-item label="跟进时间">
          <el-date-picker
            v-model="form.followTime"
            type="datetime"
            placeholder="选择跟进时间"
            value-format="YYYY-MM-DD HH:mm:ss"
          />
        </el-form-item>
        <el-form-item label="下次跟进">
          <el-date-picker
            v-model="form.nextFollowTime"
            type="datetime"
            placeholder="选择下次跟进时间"
            value-format="YYYY-MM-DD HH:mm:ss"
          />
        </el-form-item>
        <el-form-item label="跟进人">
          <el-input v-model="form.follower" placeholder="请输入跟进人" />
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
const dialogTitle = ref('新增跟进')

const searchForm = reactive({
  customerName: '',
  follower: '',
  type: ''
})

const form = reactive({
  id: null,
  customerId: null,
  type: '',
  content: '',
  followTime: '',
  nextFollowTime: '',
  follower: ''
})

const pagination = reactive({
  page: 1,
  size: 10,
  total: 0
})

const tableData = ref([
  { 
    id: 1, 
    customerName: '张三', 
    phone: '13800138001', 
    type: 'phone', 
    content: '电话沟通了解客户需求，客户表示对我们的产品很感兴趣', 
    follower: '李销售', 
    followTime: '2024-01-01 10:00:00',
    nextFollowTime: '2024-01-08 10:00:00'
  },
  { 
    id: 2, 
    customerName: '李四', 
    phone: '13800138002', 
    type: 'email', 
    content: '发送了产品资料邮件，等待客户反馈', 
    follower: '王销售', 
    followTime: '2024-01-02 14:30:00',
    nextFollowTime: '2024-01-09 14:30:00'
  },
  { 
    id: 3, 
    customerName: '王五', 
    phone: '13800138003', 
    type: 'visit', 
    content: '上门拜访客户，详细介绍了产品功能和优势', 
    follower: '赵销售', 
    followTime: '2024-01-03 15:00:00',
    nextFollowTime: '2024-01-10 15:00:00'
  }
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
  Object.assign(searchForm, { customerName: '', follower: '', type: '' })
  fetchData()
}

const handleAdd = () => {
  dialogTitle.value = '新增跟进'
  Object.assign(form, { 
    id: null, 
    customerId: null, 
    type: '', 
    content: '', 
    followTime: '', 
    nextFollowTime: '', 
    follower: '' 
  })
  dialogVisible.value = true
}

const handleEdit = (row) => {
  dialogTitle.value = '编辑跟进'
  Object.assign(form, row)
  dialogVisible.value = true
}

const handleView = (row) => {
  ElMessage.info('查看跟进详情：' + row.customerName)
}

const handleDelete = (row) => {
  ElMessageBox.confirm('确认删除该跟进记录？', '提示', {
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

const getTypeColor = (type) => {
  const map = { phone: 'success', email: 'primary', visit: 'warning', other: 'info' }
  return map[type] || ''
}

const getTypeText = (type) => {
  const map = { phone: '电话沟通', email: '邮件联系', visit: '上门拜访', other: '其他' }
  return map[type] || '-'
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
