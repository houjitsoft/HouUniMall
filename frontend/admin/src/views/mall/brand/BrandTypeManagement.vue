<template>
  <div class="brand-type-container">
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
              type="primary" 
              @click="handleEdit(row)"
            >
              编辑
            </el-button>
            <el-button 
              link 
              type="danger" 
              @click="handleDelete(row)"
            >
              删除
            </el-button>
          </template>
        </el-table-column>
        <el-table-column prop="id" label="ID" width="100" />
        <el-table-column prop="name" label="类型名称" min-width="200" />
        <el-table-column prop="sort" label="排序" width="120" align="center" />
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
      width="800px"
      :close-on-click-modal="false"
      @close="handleDialogClose"
    >
      <el-form
        ref="formRef"
        :model="formData"
        :rules="formRules"
        label-width="100px"
      >
        <el-form-item label="类型：" prop="name" required>
          <el-input 
            v-model="formData.name" 
            placeholder="请输入类型名称"
            clearable
          />
        </el-form-item>
        
        <el-form-item label="排序：" prop="sort">
          <el-input-number 
            v-model="formData.sort" 
            :min="1"
            :max="9999"
            placeholder="请输入排序号"
          />
          <div class="form-tips">
            请填写自然数。类型列表将会根据排序进行由小到大排列显示。
          </div>
        </el-form-item>

        <el-form-item label="选择关联品牌：">
          <div class="brand-selection">
            <el-checkbox-group v-model="formData.selectedBrands">
              <el-row :gutter="10">
                <el-col 
                  v-for="brand in availableBrands" 
                  :key="brand.id" 
                  :span="6"
                >
                  <el-checkbox :label="brand.id">
                    {{ brand.name }}
                  </el-checkbox>
                </el-col>
              </el-row>
            </el-checkbox-group>
          </div>
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="dialogVisible = false">
          取消
        </el-button>
        <el-button type="primary" @click="handleSubmit">
          确认提交
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { brandTypeService, brandService } from '@/api/goods/goods.js'

// 响应式数据
const loading = ref(false)
const dialogVisible = ref(false)
const dialogTitle = ref('新增品牌类型')
const formRef = ref(null)
const isEdit = ref(false)

// 分页信息
const pagination = reactive({
  page: 1,
  size: 10,
  total: 0
})

// 列表数据
const tableData = ref([])

// 可选品牌列表
const availableBrands = ref([])

// 表单数据
const formData = reactive({
  id: null,
  name: '',
  sort: 1,
  selectedBrands: []
})

// 表单验证规则
const formRules = {
  name: [
    { required: true, message: '请输入类型名称', trigger: 'blur' }
  ],
  sort: [
    { required: true, message: '请输入排序号', trigger: 'blur' }
  ]
}

// 方法
const handleAdd = () => {
  isEdit.value = false
  dialogTitle.value = '新增品牌类型'
  resetForm()
  dialogVisible.value = true
}

const handleEdit = (row) => {
  isEdit.value = true
  dialogTitle.value = '编辑品牌类型'
  
  // 复制数据到表单
  Object.assign(formData, {
    id: row.id,
    name: row.name,
    sort: row.sort,
    selectedBrands: row.brandIds ? [...row.brandIds] : []
  })
  
  dialogVisible.value = true
}

const handleDelete = (row) => {
  ElMessageBox.confirm(
    `确定要删除品牌类型"${row.name}"吗？`,
    '确认删除',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(async () => {
    try {
      const res = await brandTypeService.delete(row.id)
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

const handleSubmit = async () => {
  if (!formRef.value) return
  
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    
    try {
      const params = {
        name: formData.name,
        sort: formData.sort,
        brandIds: formData.selectedBrands
      }
      
      let res
      if (isEdit.value) {
        params.id = formData.id
        res = await brandTypeService.update(params)
      } else {
        res = await brandTypeService.create(params)
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
    sort: 1,
    selectedBrands: []
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
      size: pagination.size
    }
    
    const res = await brandTypeService.getList(params)
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

// 获取所有品牌列表（用于关联选择）
const fetchAvailableBrands = async () => {
  try {
    const res = await brandService.getAllBrands()
    if (res && (res.code === 1 || res.code === 200)) {
      availableBrands.value = res.data || []
    }
  } catch (error) {
    console.error('获取品牌列表失败:', error)
    availableBrands.value = []
  }
}

// 初始化
onMounted(() => {
  fetchTableData()
  fetchAvailableBrands()
})
</script>

<style scoped>
.brand-type-container {
  padding: 20px;
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

.form-tips {
  color: #909399;
  font-size: 12px;
  line-height: 1.5;
  margin-top: 5px;
}

.brand-selection {
  width: 100%;
  max-height: 400px;
  overflow-y: auto;
  padding: 10px;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
}

:deep(.el-form-item__label) {
  font-weight: normal;
}

:deep(.el-checkbox) {
  margin-right: 10px;
  margin-bottom: 10px;
  width: 100%;
}
</style>
