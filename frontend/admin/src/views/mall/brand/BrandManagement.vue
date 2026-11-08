<template>
  <div class="brand-management-container">
    <!-- 搜索筛选区域 -->
    <el-card class="search-card">
      <el-form :model="searchForm" inline>
        <el-form-item label="品牌名称：">
          <el-input 
            v-model="searchForm.name" 
            placeholder="请输入品牌名称" 
            clearable 
            style="width: 250px"
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

    <!-- 操作按钮 -->
    <el-card class="action-card">
      <el-button type="primary" icon="Plus" @click="handleAdd">
        新增
      </el-button>
      <el-button icon="Refresh" @click="handleRefresh">
        刷新
      </el-button>
    </el-card>

    <!-- 数据列表 -->
    <el-card class="table-card">
      <el-table 
        v-loading="loading"
        :data="tableData" 
        style="width: 100%"
      >
        <el-table-column label="操作" width="150" fixed="left">
          <template #default="{ row }">
            <el-button 
              link 
              type="danger" 
              @click="handleDelete(row)"
            >
              删除
            </el-button>
            <el-button 
              link 
              type="primary" 
              @click="handleEdit(row)"
            >
              编辑
            </el-button>
          </template>
        </el-table-column>
        <el-table-column prop="id" label="品牌ID" width="120" />
        <el-table-column prop="name" label="品牌名称" min-width="200" />
        <el-table-column label="品牌图片" width="150" align="center">
          <template #default="{ row }">
            <el-image 
              v-if="row.logo"
              :src="row.logo" 
              :preview-src-list="[row.logo]"
              fit="contain"
              style="width: 80px; height: 80px; cursor: pointer"
            />
            <span v-else class="no-image">暂无图片</span>
          </template>
        </el-table-column>
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

    <!-- 新增/编辑弹窗 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="600px"
      :close-on-click-modal="false"
      @close="handleDialogClose"
    >
      <el-form
        ref="formRef"
        :model="formData"
        :rules="formRules"
        label-width="120px"
      >
        <el-form-item label="品牌名称：" prop="name" required>
          <el-input 
            v-model="formData.name" 
            placeholder="请输入品牌名称"
            clearable
          />
        </el-form-item>
        
        <el-form-item label="品牌LOGO：">
          <el-upload
            class="brand-logo-uploader"
            :action="uploadAction"
            :show-file-list="false"
            :on-success="handleUploadSuccess"
            :before-upload="beforeUpload"
            :headers="uploadHeaders"
          >
            <el-image 
              v-if="formData.logo" 
              :src="formData.logo" 
              class="logo-preview"
              fit="contain"
            />
            <el-icon v-else class="logo-uploader-icon">
              <Plus />
            </el-icon>
          </el-upload>
          <div class="form-tips">
            建议尺寸：200x200像素，支持jpg、png格式，大小不超过2MB
          </div>
        </el-form-item>

        <el-form-item label="品牌描述：">
          <el-input 
            v-model="formData.description" 
            type="textarea"
            :rows="4"
            maxlength="500"
            show-word-limit
            placeholder="请输入品牌描述"
          />
        </el-form-item>

        <el-form-item label="排序：" prop="sort">
          <el-input-number 
            v-model="formData.sort" 
            :min="0"
            :max="9999"
            placeholder="请输入排序号"
          />
        </el-form-item>

        <el-form-item label="是否启用：">
          <el-switch 
            v-model="formData.status"
            :active-value="1"
            :inactive-value="0"
          />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="dialogVisible = false">
          取消
        </el-button>
        <el-button type="primary" @click="handleSubmit">
          保存
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { brandService } from '@/api/goods/goods.js'
import { getToken } from '@/utils/auth'

// 响应式数据
const loading = ref(false)
const dialogVisible = ref(false)
const dialogTitle = ref('新增品牌')
const formRef = ref(null)
const isEdit = ref(false)

// 搜索表单
const searchForm = reactive({
  name: ''
})

// 分页信息
const pagination = reactive({
  page: 1,
  size: 10,
  total: 0
})

// 列表数据
const tableData = ref([])

// 表单数据
const formData = reactive({
  id: null,
  name: '',
  logo: '',
  description: '',
  sort: 0,
  status: 1
})

// 表单验证规则
const formRules = {
  name: [
    { required: true, message: '请输入品牌名称', trigger: 'blur' }
  ],
  sort: [
    { required: true, message: '请输入排序号', trigger: 'blur' }
  ]
}

// 上传相关配置
const uploadAction = computed(() => {
  // TODO: 替换为实际的上传接口地址
  return `${import.meta.env.VITE_API_BASE_URL}/web/common/upload`
})

const uploadHeaders = computed(() => {
  return {
    Authorization: `Bearer ${getToken()}`
  }
})

// 方法
const handleSearch = () => {
  pagination.page = 1
  fetchTableData()
}

const handleReset = () => {
  searchForm.name = ''
  pagination.page = 1
  fetchTableData()
}

const handleAdd = () => {
  isEdit.value = false
  dialogTitle.value = '新增品牌'
  resetForm()
  dialogVisible.value = true
}

const handleEdit = (row) => {
  isEdit.value = true
  dialogTitle.value = '编辑品牌'
  
  // 复制数据到表单
  Object.assign(formData, {
    id: row.id,
    name: row.name,
    logo: row.logo,
    description: row.description,
    sort: row.sort,
    status: row.status
  })
  
  dialogVisible.value = true
}

const handleDelete = (row) => {
  ElMessageBox.confirm(
    `确定要删除品牌"${row.name}"吗？`,
    '确认删除',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(async () => {
    try {
      const res = await brandService.deleteBrand(row.id)
      if (res && (res.code === 1 || res.code === 200)) {
        ElMessage.success('删除成功')
        fetchTableData()
      } else {
        ElMessage.error(res.message || '删除失败')
      }
    } catch (error) {
      console.error('删除失败:', error)
      ElMessage.error(error.message || '删除失败')
    }
  })
}

const handleRefresh = () => {
  fetchTableData()
}

const beforeUpload = (file) => {
  const isImage = file.type === 'image/jpeg' || file.type === 'image/png'
  const isLt2M = file.size / 1024 / 1024 < 2

  if (!isImage) {
    ElMessage.error('上传图片只能是 JPG 或 PNG 格式!')
    return false
  }
  if (!isLt2M) {
    ElMessage.error('上传图片大小不能超过 2MB!')
    return false
  }
  return true
}

const handleUploadSuccess = (response) => {
  if (response && (response.code === 1 || response.code === 200)) {
    formData.logo = response.data?.url || response.data
    ElMessage.success('图片上传成功')
  } else {
    ElMessage.error(response.message || '图片上传失败')
  }
}

const handleSubmit = async () => {
  if (!formRef.value) return
  
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    
    try {
      const params = {
        name: formData.name,
        logo: formData.logo,
        description: formData.description,
        sort: formData.sort,
        status: formData.status
      }
      
      let res
      if (isEdit.value) {
        params.id = formData.id
        res = await brandService.updateBrand(params)
      } else {
        res = await brandService.createBrand(params)
      }
      
      if (res && (res.code === 1 || res.code === 200)) {
        ElMessage.success(isEdit.value ? '编辑成功' : '新增成功')
        dialogVisible.value = false
        fetchTableData()
      } else {
        ElMessage.error(res.message || '保存失败')
      }
    } catch (error) {
      console.error('保存失败:', error)
      ElMessage.error(error.message || '保存失败')
    }
  })
}

const handleDialogClose = () => {
  resetForm()
}

const resetForm = () => {
  Object.assign(formData, {
    id: null,
    name: '',
    logo: '',
    description: '',
    sort: 0,
    status: 1
  })
  formRef.value?.clearValidate()
}

const handleSizeChange = (newSize) => {
  pagination.size = newSize
  fetchTableData()
}

const handleCurrentChange = (newPage) => {
  pagination.page = newPage
  fetchTableData()
}

// 获取列表数据
const fetchTableData = async () => {
  loading.value = true
  try {
    const params = {
      page: pagination.page,
      size: pagination.size,
      name: searchForm.name
    }
    
    const res = await brandService.getBrandList(params)
    if (res && (res.code === 1 || res.code === 200)) {
      // 兼容多种响应格式
      tableData.value = res.data?.list || res.data?.records || res.data || []
      pagination.total = res.data?.totalCount || res.data?.total || 0
    } else {
      ElMessage.error(res.message || '获取数据失败')
      tableData.value = []
      pagination.total = 0
    }
  } catch (error) {
    console.error('获取列表数据失败:', error)
    // 如果是"用户未登录"错误，响应拦截器已经处理了跳转，不需要显示错误消息
    if (error.message && (
      error.message.includes('用户未登录') || 
      error.message.includes('未登录') ||
      error.message.includes('token') ||
      error.message.includes('Token')
    )) {
      return
    }
    ElMessage.error(error.message || '获取数据失败')
    tableData.value = []
    pagination.total = 0
  } finally {
    loading.value = false
  }
}

// 初始化
onMounted(() => {
  fetchTableData()
})
</script>

<style scoped>
.brand-management-container {
  padding: 20px;
}

.search-card {
  margin-bottom: 20px;
}

.action-card {
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

.no-image {
  color: #909399;
  font-size: 12px;
}

.brand-logo-uploader {
  display: inline-block;
}

.logo-preview {
  width: 120px;
  height: 120px;
  display: block;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  cursor: pointer;
}

.logo-uploader-icon {
  width: 120px;
  height: 120px;
  border: 1px dashed #d9d9d9;
  border-radius: 4px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 28px;
  color: #8c939d;
  cursor: pointer;
  transition: all 0.3s;
}

.logo-uploader-icon:hover {
  border-color: #409eff;
  color: #409eff;
}

.form-tips {
  color: #909399;
  font-size: 12px;
  line-height: 1.5;
  margin-top: 5px;
}

:deep(.el-form-item__label) {
  font-weight: normal;
}

:deep(.el-upload) {
  display: inline-block;
}
</style>
