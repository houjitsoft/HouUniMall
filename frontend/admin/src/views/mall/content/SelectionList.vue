<template>
  <div class="page-container">
    <el-card shadow="never">
      <template #header>
        <div class="card-header">
          <span class="card-title">选题列表</span>
          <el-button type="primary" @click="handleAdd">
            <el-icon><Plus /></el-icon> 新增选题
          </el-button>
        </div>
      </template>

      <!-- 搜索表单 -->
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="选题标题">
          <el-input v-model="searchForm.title" placeholder="请输入选题标题" clearable style="width: 200px;" />
        </el-form-item>
        <el-form-item label="话题分类">
          <el-select v-model="searchForm.categoryId" placeholder="请选择" clearable style="width: 150px;">
            <el-option label="购物心得" :value="1" />
            <el-option label="产品评测" :value="2" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="请选择" clearable style="width: 150px;">
            <el-option label="待发布" :value="0" />
            <el-option label="已发布" :value="1" />
            <el-option label="已下架" :value="2" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">
            <el-icon><Search /></el-icon> 查询
          </el-button>
          <el-button @click="handleReset">
            <el-icon><Refresh /></el-icon> 重置
          </el-button>
        </el-form-item>
      </el-form>

      <!-- 数据表格 -->
      <el-table :data="tableData" v-loading="loading" border stripe>
        <el-table-column prop="topicId" label="选题ID" width="80" />
        <el-table-column label="封面图" width="100">
          <template #default="{ row }">
            <el-image v-if="row.coverImage" :src="row.coverImage" :preview-src-list="[row.coverImage]" fit="cover" style="width: 60px; height: 60px;" />
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column prop="title" label="选题标题" min-width="200" show-overflow-tooltip />
        <el-table-column prop="categoryName" label="话题分类" width="100" />
        <el-table-column prop="author" label="作者" width="100" />
        <el-table-column prop="viewCount" label="浏览量" width="100" />
        <el-table-column prop="likeCount" label="点赞数" width="100" />
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="publishTime" label="发布时间" width="160" />
        <el-table-column label="操作" width="260" fixed="right">
          <template #default="{ row }">
            <el-button text type="primary" size="small" @click="handleView(row)">
              <el-icon><View /></el-icon> 查看
            </el-button>
            <el-button text type="primary" size="small" @click="handleEdit(row)">
              <el-icon><Edit /></el-icon> 编辑
            </el-button>
            <el-button text type="success" size="small" @click="handlePublish(row)" v-if="row.status === 0">
              <el-icon><Upload /></el-icon> 发布
            </el-button>
            <el-button text type="danger" size="small" @click="handleDelete(row)">
              <el-icon><Delete /></el-icon> 删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination">
        <el-pagination
          v-model:current-page="pagination.page"
          v-model:page-size="pagination.size"
          :total="pagination.total"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSearch"
          @current-change="handleSearch"
        />
      </div>
    </el-card>

    <!-- 新增/编辑对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="800px"
      @close="handleDialogClose"
    >
      <el-form :model="formData" :rules="formRules" ref="formRef" label-width="100px">
        <el-form-item label="选题标题" prop="title">
          <el-input v-model="formData.title" placeholder="请输入选题标题" />
        </el-form-item>
        <el-form-item label="话题分类" prop="categoryId">
          <el-select v-model="formData.categoryId" placeholder="请选择分类" style="width: 100%;">
            <el-option label="购物心得" :value="1" />
            <el-option label="产品评测" :value="2" />
          </el-select>
        </el-form-item>
        <el-form-item label="作者" prop="author">
          <el-input v-model="formData.author" placeholder="请输入作者名称" />
        </el-form-item>
        <el-form-item label="封面图" prop="coverImage">
          <el-input v-model="formData.coverImage" placeholder="请输入封面图URL" />
        </el-form-item>
        <el-form-item label="内容摘要" prop="summary">
          <el-input v-model="formData.summary" type="textarea" :rows="3" placeholder="请输入内容摘要" />
        </el-form-item>
        <el-form-item label="正文内容" prop="content">
          <el-input v-model="formData.content" type="textarea" :rows="6" placeholder="请输入正文内容" />
        </el-form-item>
        <el-form-item label="标签">
          <el-input v-model="formData.tags" placeholder="多个标签用逗号分隔" />
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
import { Plus, Search, Refresh, View, Edit, Upload, Delete } from '@element-plus/icons-vue'

const loading = ref(false)
const dialogVisible = ref(false)
const dialogTitle = ref('新增选题')
const formRef = ref(null)

const searchForm = reactive({
  title: '',
  categoryId: '',
  status: ''
})

const pagination = reactive({
  page: 1,
  size: 10,
  total: 0
})

const tableData = ref([])

const formData = reactive({
  topicId: null,
  title: '',
  categoryId: null,
  author: '',
  coverImage: '',
  summary: '',
  content: '',
  tags: ''
})

const formRules = {
  title: [{ required: true, message: '请输入选题标题', trigger: 'blur' }],
  categoryId: [{ required: true, message: '请选择话题分类', trigger: 'change' }],
  author: [{ required: true, message: '请输入作者名称', trigger: 'blur' }],
  content: [{ required: true, message: '请输入正文内容', trigger: 'blur' }]
}

const getStatusType = (status) => {
  const map = { 0: 'warning', 1: 'success', 2: 'info' }
  return map[status] || 'info'
}

const getStatusText = (status) => {
  const map = { 0: '待发布', 1: '已发布', 2: '已下架' }
  return map[status] || '未知'
}

const loadData = async () => {
  loading.value = true
  try {
    // TODO: 调用API获取数据
    tableData.value = [
      {
        topicId: 1,
        title: '2024年度购物心得分享',
        categoryName: '购物心得',
        categoryId: 1,
        author: '小编',
        coverImage: '',
        viewCount: 1280,
        likeCount: 356,
        status: 1,
        publishTime: '2024-01-01 10:00:00',
        createTime: '2024-01-01 09:00:00'
      }
    ]
    pagination.total = 1
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  pagination.page = 1
  loadData()
}

const handleReset = () => {
  searchForm.title = ''
  searchForm.categoryId = ''
  searchForm.status = ''
  handleSearch()
}

const handleAdd = () => {
  dialogTitle.value = '新增选题'
  Object.assign(formData, {
    topicId: null,
    title: '',
    categoryId: null,
    author: '',
    coverImage: '',
    summary: '',
    content: '',
    tags: ''
  })
  dialogVisible.value = true
}

const handleView = (row) => {
  ElMessage.info('查看选题详情')
}

const handleEdit = (row) => {
  dialogTitle.value = '编辑选题'
  Object.assign(formData, row)
  dialogVisible.value = true
}

const handlePublish = async (row) => {
  try {
    await ElMessageBox.confirm('确定要发布该选题吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    // TODO: 调用API发布
    ElMessage.success('发布成功')
    loadData()
  } catch {}
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm('确定要删除该选题吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    // TODO: 调用API删除
    ElMessage.success('删除成功')
    loadData()
  } catch {}
}

const handleSubmit = async () => {
  await formRef.value.validate()
  try {
    // TODO: 调用API保存
    ElMessage.success('保存成功')
    dialogVisible.value = false
    loadData()
  } catch (error) {
    ElMessage.error('保存失败')
  }
}

const handleDialogClose = () => {
  formRef.value?.resetFields()
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.page-container {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-title {
  font-weight: bold;
  font-size: 16px;
}

.search-form {
  margin-bottom: 20px;
}

.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}
</style>
