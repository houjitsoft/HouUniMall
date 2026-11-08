<template>
  <div class="attribute-type-container">
    <!-- 搜索筛选区域 -->
    <el-card class="search-card">
      <el-form :model="searchForm" inline>
        <el-form-item label="属性值名称：">
          <el-input 
            v-model="searchForm.valueName" 
            placeholder="请输入属性值名称" 
            clearable 
            style="width: 200px"
          />
        </el-form-item>
        <el-form-item label="属性类型名称：">
          <el-input 
            v-model="searchForm.typeName" 
            placeholder="请输入属性类型名称" 
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
        <el-table-column prop="name" label="属性类型名称" min-width="180" />
        <el-table-column prop="valueCount" label="属性值数量" width="120" align="center" />
        <el-table-column prop="remark" label="属性类型备注" min-width="300" show-overflow-tooltip />
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
      width="1000px"
      :close-on-click-modal="false"
      @close="handleDialogClose"
    >
      <el-form
        ref="formRef"
        :model="formData"
        :rules="formRules"
        label-width="140px"
      >
        <el-form-item label="属性类型名称：" prop="name" required>
          <el-input 
            v-model="formData.name" 
            placeholder="请输入属性类型名称"
            clearable
            style="width: 100%"
          />
        </el-form-item>
        
        <el-form-item label="属性类型备注：">
          <el-input 
            v-model="formData.remark" 
            type="textarea"
            :rows="3"
            maxlength="100"
            show-word-limit
            placeholder="请输入属性类型备注"
            style="width: 100%"
          />
        </el-form-item>

        <el-form-item label="属性值：" required>
          <el-button type="primary" icon="Plus" @click="handleAddValue">
            添加
          </el-button>
        </el-form-item>

        <!-- 属性值列表表格 -->
        <el-table 
          v-if="formData.values && formData.values.length > 0"
          :data="formData.values" 
          border
          style="width: 100%; margin-top: 10px"
          class="value-list-table"
        >
          <el-table-column label="顺序号" width="120" align="center">
            <template #default="{ $index }">
              {{ $index + 1 }}
            </template>
          </el-table-column>
          <el-table-column label="属性值名称" min-width="300">
            <template #default="{ row }">
              <el-input 
                v-model="row.valueName" 
                placeholder="请输入属性值名称"
                clearable
              />
            </template>
          </el-table-column>
          <el-table-column label="操作" width="100" align="center">
            <template #default="{ $index }">
              <el-button 
                link 
                type="danger" 
                @click="handleRemoveValue($index)"
              >
                删除
              </el-button>
            </template>
          </el-table-column>
        </el-table>

        <el-empty 
          v-else 
          description="暂无数据" 
          :image-size="80" 
        />
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
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { attributeTypeService } from '@/api/goods/goods.js'

// 响应式数据
const loading = ref(false)
const dialogVisible = ref(false)
const dialogTitle = ref('新增属性类型')
const formRef = ref(null)
const isEdit = ref(false)

// 搜索表单
const searchForm = reactive({
  valueName: '',
  typeName: ''
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
  remark: '',
  values: []
})

// 表单验证规则
const formRules = {
  name: [
    { required: true, message: '请输入属性类型名称', trigger: 'blur' }
  ]
}

// 方法
const handleSearch = () => {
  pagination.page = 1
  fetchTableData()
}

const handleReset = () => {
  searchForm.valueName = ''
  searchForm.typeName = ''
  pagination.page = 1
  fetchTableData()
}

const handleAdd = () => {
  isEdit.value = false
  dialogTitle.value = '新增属性类型'
  resetForm()
  dialogVisible.value = true
}

const handleEdit = (row) => {
  isEdit.value = true
  dialogTitle.value = '编辑属性类型'
  
  // 复制数据到表单
  Object.assign(formData, {
    id: row.id,
    name: row.name,
    remark: row.remark,
    values: row.values ? JSON.parse(JSON.stringify(row.values)) : []
  })
  
  dialogVisible.value = true
}

const handleDelete = (row) => {
  ElMessageBox.confirm(
    `确定要删除属性类型"${row.name}"吗？`,
    '确认删除',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(async () => {
    try {
      const res = await attributeTypeService.delete(row.id)
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

const handleAddValue = () => {
  formData.values.push({
    valueName: ''
  })
}

const handleRemoveValue = (index) => {
  formData.values.splice(index, 1)
}

const handleSubmit = async () => {
  if (!formRef.value) return
  
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    
    // 验证至少添加一个属性值
    if (!formData.values || formData.values.length === 0) {
      ElMessage.warning('请至少添加一个属性值')
      return
    }
    
    // 验证所有属性值都已填写
    const hasEmptyValue = formData.values.some(val => !val.valueName || val.valueName.trim() === '')
    if (hasEmptyValue) {
      ElMessage.warning('请填写所有属性值名称')
      return
    }
    
    try {
      const params = {
        name: formData.name,
        remark: formData.remark,
        values: formData.values
      }
      
      let res
      if (isEdit.value) {
        params.id = formData.id
        res = await attributeTypeService.update(params)
      } else {
        res = await attributeTypeService.create(params)
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
    remark: '',
    values: []
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
      valueName: searchForm.valueName,
      typeName: searchForm.typeName
    }
    
    const res = await attributeTypeService.getList(params)
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
.attribute-type-container {
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

.value-list-table {
  margin-top: 10px;
}

:deep(.el-form-item__label) {
  font-weight: normal;
}
</style>
