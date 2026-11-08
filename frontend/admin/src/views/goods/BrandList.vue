<template>
  <div class="brand-management">
    <el-card class="box-card">
      <template #header>
        <div class="card-header">
          <span>商品品牌管理</span>
          <el-button type="primary" @click="handleCreate">新增品牌</el-button>
        </div>
      </template>
      
      <!-- 搜索栏 -->
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="品牌名称">
          <el-input v-model="searchForm.brandName" placeholder="请输入品牌名称" clearable />
        </el-form-item>
        <el-form-item label="首字母">
          <el-input v-model="searchForm.brandInitial" placeholder="请输入首字母" clearable />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>

      <!-- 品牌列表 -->
      <el-table :data="brandList" style="width: 100%" v-loading="loading">
        <el-table-column prop="brandId" label="品牌ID" width="100" />
        <el-table-column prop="brandName" label="品牌名称" width="200" />
        <el-table-column prop="brandInitial" label="首字母" width="100" />
        <el-table-column label="品牌图片" width="150">
          <template #default="scope">
            <el-image 
              v-if="scope.row.brandPic" 
              :src="scope.row.brandPic" 
              class="brand-image" 
              fit="cover"
              :preview-src-list="[scope.row.brandPic]"
            />
          </template>
        </el-table-column>
        <el-table-column label="H5图片" width="150">
          <template #default="scope">
            <el-image 
              v-if="scope.row.brandHf" 
              :src="scope.row.brandHf" 
              class="brand-image" 
              fit="cover"
              :preview-src-list="[scope.row.brandHf]"
            />
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="180">
          <template #default="scope">
            {{ formatDate(scope.row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="scope">
            <el-button size="small" @click="handleView(scope.row)">查看</el-button>
            <el-button size="small" type="primary" @click="handleEdit(scope.row)">编辑</el-button>
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

    <!-- 品牌编辑对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="600px"
      :close-on-click-modal="false"
    >
      <el-form :model="brandForm" :rules="brandRules" ref="brandFormRef" label-width="100px">
        <el-form-item label="品牌名称" prop="brandName">
          <el-input v-model="brandForm.brandName" placeholder="请输入品牌名称" />
        </el-form-item>
        <el-form-item label="首字母" prop="brandInitial">
          <el-input v-model="brandForm.brandInitial" placeholder="请输入首字母" maxlength="1" />
        </el-form-item>
        <el-form-item label="品牌图片" prop="brandPic">
          <el-upload
            class="image-uploader"
            action="/api/upload"
            :show-file-list="false"
            :on-success="handleImageSuccess"
            :before-upload="beforeImageUpload"
          >
            <img v-if="brandForm.brandPic" :src="brandForm.brandPic" class="image-preview" />
            <el-icon v-else class="image-uploader-icon"><Plus /></el-icon>
          </el-upload>
        </el-form-item>
        <el-form-item label="H5图片" prop="brandHf">
          <el-upload
            class="image-uploader"
            action="/api/upload"
            :show-file-list="false"
            :on-success="handleH5ImageSuccess"
            :before-upload="beforeImageUpload"
          >
            <img v-if="brandForm.brandHf" :src="brandForm.brandHf" class="image-preview" />
            <el-icon v-else class="image-uploader-icon"><Plus /></el-icon>
          </el-upload>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleSubmit" :loading="submitLoading">确定</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>

// 使用 Element Plus 国际化系统
import { useI18n } from '@/i18n-element'
const { t } = useI18n()
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { brandApi } from '../../api/goods/goods'

// 响应式数据
const loading = ref(false)
const submitLoading = ref(false)
const dialogVisible = ref(false)
const dialogTitle = ref('')
const brandFormRef = ref(null)
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

// 搜索表单
const searchForm = reactive({
  brandName: '',
  brandInitial: '',
  pageNum: 1,
  pageSize: 10
})

// 品牌表单
const brandForm = reactive({
  brandId: '',
  brandName: '',
  brandInitial: '',
  brandPic: '',
  brandHf: ''
})

// 品牌表单规则
const brandRules = {
  brandName: [
    { required: true, message: '请输入品牌名称', trigger: 'blur' }
  ]
}

// 品牌列表
const brandList = ref([])

// 生命周期
onMounted(() => {
  fetchBrandList()
})

// 获取品牌列表
const fetchBrandList = async () => {
  loading.value = true
  try {
    const params = {
      ...searchForm,
      pageNum: currentPage.value,
      pageSize: pageSize.value
    }
    
    const response = await brandApi.getBrandList(params)
    
    if (response.code === 200 || response.success) {
      const data = response.data || response
      brandList.value = data.records || data.list || []
      total.value = data.total || 0
    } else {
      ElMessage.error(response.message || '获取品牌列表失败')
      brandList.value = []
      total.value = 0
    }
  } catch (error) {
    console.error('获取品牌列表失败:', error)
    ElMessage.error('获取品牌列表失败: ' + (error.message || '网络错误'))
    brandList.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
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
  fetchBrandList()
}

// 重置搜索
const handleReset = () => {
  Object.assign(searchForm, {
    brandName: '',
    brandInitial: '',
    pageNum: 1,
    pageSize: 10
  })
  currentPage.value = 1
  fetchBrandList()
}

// 新增品牌
const handleCreate = () => {
  dialogTitle.value = '新增品牌'
  dialogVisible.value = true
  // 重置表单
  Object.assign(brandForm, {
    brandId: '',
    brandName: '',
    brandInitial: '',
    brandPic: '',
    brandHf: ''
  })
}

// 编辑品牌
const handleEdit = (row) => {
  dialogTitle.value = '编辑品牌'
  dialogVisible.value = true
  // 填充表单数据
  Object.assign(brandForm, row)
}

// 查看品牌
const handleView = (row) => {
  dialogTitle.value = '查看品牌'
  dialogVisible.value = true
  // 填充表单数据（只读）
  Object.assign(brandForm, row)
  // 这里可以设置表单为只读模式
}

// 删除品牌
const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm('确定要删除该品牌吗？删除后不可恢复', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    const response = await brandApi.deleteBrand(row.brandId)
    
    if (response.code === 200 || response.success) {
      ElMessage.success('删除成功')
      fetchBrandList()
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

// 图片上传成功处理
const handleImageSuccess = (response, file) => {
  if (response.code === 200 || response.success) {
    brandForm.brandPic = response.data.url
  } else {
    ElMessage.error(response.message || '图片上传失败')
  }
}

// H5图片上传成功处理
const handleH5ImageSuccess = (response, file) => {
  if (response.code === 200 || response.success) {
    brandForm.brandHf = response.data.url
  } else {
    ElMessage.error(response.message || '图片上传失败')
  }
}

// 图片上传前处理
const beforeImageUpload = (file) => {
  const isJPG = file.type === 'image/jpeg' || file.type === 'image/png'
  const isLt2M = file.size / 1024 / 1024 < 2

  if (!isJPG) {
    ElMessage.error('上传图片只能是 JPG/PNG 格式!')
  }
  if (!isLt2M) {
    ElMessage.error('上传图片大小不能超过 2MB!')
  }
  return isJPG && isLt2M
}

// 提交表单
const handleSubmit = async () => {
  try {
    await brandFormRef.value.validate()
    
    submitLoading.value = true
    let response
    
    if (brandForm.brandId) {
      // 编辑品牌
      response = await brandApi.updateBrand(brandForm)
    } else {
      // 新增品牌
      response = await brandApi.createBrand(brandForm)
    }
    
    if (response.code === 200 || response.success) {
      ElMessage.success(brandForm.brandId ? '编辑成功' : '新增成功')
      dialogVisible.value = false
      fetchBrandList()
    } else {
      ElMessage.error(response.message || (brandForm.brandId ? '编辑失败' : '新增失败'))
    }
  } catch (error) {
    console.error(brandForm.brandId ? '编辑失败:' : '新增失败:', error)
    ElMessage.error((brandForm.brandId ? '编辑失败:' : '新增失败:') + (error.message || '网络错误'))
  } finally {
    submitLoading.value = false
  }
}

// 分页大小改变
const handleSizeChange = (val) => {
  pageSize.value = val
  searchForm.pageSize = val
  fetchBrandList()
}

// 当前页改变
const handleCurrentChange = (val) => {
  currentPage.value = val
  searchForm.pageNum = val
  fetchBrandList()
}
</script>

<style scoped>
.brand-management {
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

.brand-image {
  width: 80px;
  height: 80px;
  border-radius: 4px;
}

.image-uploader .image-preview {
  width: 100%;
  height: 120px;
  display: block;
}

.image-uploader-icon {
  font-size: 28px;
  color: #8c939d;
  width: 120px;
  height: 120px;
  text-align: center;
  line-height: 120px;
}

.image-uploader {
  border: 1px dashed #d9d9d9;
  border-radius: 6px;
  cursor: pointer;
  position: relative;
  overflow: hidden;
  transition: var(--el-transition-duration-fast);
}

.image-uploader:hover {
  border-color: var(--el-color-primary);
}

.pagination-container {
  margin-top: 20px;
  text-align: right;
}

.dialog-footer {
  text-align: right;
}
</style>
