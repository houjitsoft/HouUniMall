<template>
  <div class="page-container">
    <el-card shadow="never">
      <template #header>
        <div class="card-header">
          <span class="card-title">直播互动</span>
          <el-button type="primary" @click="handleAdd">
            <el-icon><Plus /></el-icon> 创建直播
          </el-button>
        </div>
      </template>

      <!-- 搜索表单 -->
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="直播标题">
          <el-input v-model="searchForm.liveTitle" placeholder="请输入直播标题" clearable style="width: 200px;" />
        </el-form-item>
        <el-form-item label="主播名称">
          <el-input v-model="searchForm.anchorName" placeholder="请输入主播名称" clearable style="width: 150px;" />
        </el-form-item>
        <el-form-item label="直播状态">
          <el-select v-model="searchForm.status" placeholder="请选择" clearable style="width: 150px;">
            <el-option label="未开始" :value="0" />
            <el-option label="直播中" :value="1" />
            <el-option label="已结束" :value="2" />
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
        <el-table-column prop="liveId" label="直播ID" width="80" />
        <el-table-column label="直播封面" width="100">
          <template #default="{ row }">
            <el-image v-if="row.coverImage" :src="row.coverImage" :preview-src-list="[row.coverImage]" fit="cover" style="width: 60px; height: 60px;" />
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column prop="liveTitle" label="直播标题" min-width="150" show-overflow-tooltip />
        <el-table-column prop="anchorName" label="主播名称" width="100" />
        <el-table-column prop="viewCount" label="观看人数" width="100" />
        <el-table-column prop="likeCount" label="点赞数" width="100" />
        <el-table-column label="直播时间" width="180">
          <template #default="{ row }">
            <div v-if="row.startTime">
              <div>开始：{{ row.startTime }}</div>
              <div v-if="row.endTime">结束：{{ row.endTime }}</div>
            </div>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="240" fixed="right">
          <template #default="{ row }">
            <el-button text type="primary" size="small" @click="handleView(row)">
              <el-icon><View /></el-icon> 查看
            </el-button>
            <el-button text type="primary" size="small" @click="handleEdit(row)" v-if="row.status === 0">
              <el-icon><Edit /></el-icon> 编辑
            </el-button>
            <el-button text type="danger" size="small" @click="handleDelete(row)" v-if="row.status !== 1">
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
      width="700px"
      @close="handleDialogClose"
    >
      <el-form :model="formData" :rules="formRules" ref="formRef" label-width="100px">
        <el-form-item label="直播标题" prop="liveTitle">
          <el-input v-model="formData.liveTitle" placeholder="请输入直播标题" />
        </el-form-item>
        <el-form-item label="主播名称" prop="anchorName">
          <el-input v-model="formData.anchorName" placeholder="请输入主播名称" />
        </el-form-item>
        <el-form-item label="直播封面" prop="coverImage">
          <el-input v-model="formData.coverImage" placeholder="请输入封面图片URL" />
        </el-form-item>
        <el-form-item label="开始时间" prop="startTime">
          <el-date-picker
            v-model="formData.startTime"
            type="datetime"
            placeholder="选择开始时间"
            style="width: 100%;"
            format="YYYY-MM-DD HH:mm:ss"
            value-format="YYYY-MM-DD HH:mm:ss"
          />
        </el-form-item>
        <el-form-item label="直播简介" prop="description">
          <el-input v-model="formData.description" type="textarea" :rows="3" placeholder="请输入直播简介" />
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
import { Plus, Search, Refresh, View, Edit, Delete } from '@element-plus/icons-vue'

const loading = ref(false)
const dialogVisible = ref(false)
const dialogTitle = ref('创建直播')
const formRef = ref(null)

const searchForm = reactive({
  liveTitle: '',
  anchorName: '',
  status: ''
})

const pagination = reactive({
  page: 1,
  size: 10,
  total: 0
})

const tableData = ref([])

const formData = reactive({
  liveId: null,
  liveTitle: '',
  anchorName: '',
  coverImage: '',
  startTime: '',
  description: ''
})

const formRules = {
  liveTitle: [{ required: true, message: '请输入直播标题', trigger: 'blur' }],
  anchorName: [{ required: true, message: '请输入主播名称', trigger: 'blur' }],
  startTime: [{ required: true, message: '请选择开始时间', trigger: 'change' }]
}

const getStatusType = (status) => {
  const map = { 0: 'info', 1: 'success', 2: 'warning' }
  return map[status] || 'info'
}

const getStatusText = (status) => {
  const map = { 0: '未开始', 1: '直播中', 2: '已结束' }
  return map[status] || '未知'
}

const loadData = async () => {
  loading.value = true
  try {
    // TODO: 调用API获取数据
    tableData.value = [
      {
        liveId: 1,
        liveTitle: '新品发布直播',
        anchorName: '主播小王',
        coverImage: '',
        viewCount: 1280,
        likeCount: 356,
        startTime: '2024-01-05 20:00:00',
        endTime: '',
        status: 0,
        description: '最新产品发布直播',
        createTime: '2024-01-01 10:00:00'
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
  searchForm.liveTitle = ''
  searchForm.anchorName = ''
  searchForm.status = ''
  handleSearch()
}

const handleAdd = () => {
  dialogTitle.value = '创建直播'
  Object.assign(formData, {
    liveId: null,
    liveTitle: '',
    anchorName: '',
    coverImage: '',
    startTime: '',
    description: ''
  })
  dialogVisible.value = true
}

const handleView = (row) => {
  ElMessage.info('查看直播详情')
}

const handleEdit = (row) => {
  dialogTitle.value = '编辑直播'
  Object.assign(formData, row)
  dialogVisible.value = true
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm('确定要删除该直播吗？', '提示', {
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
