<template>
  <div class="attribute-group-container">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-left">
        <h2>属性组管理</h2>
      </div>
      <div class="header-right">
        <el-button type="primary" icon="Plus" @click="handleAdd">
          新增
        </el-button>
        <el-button icon="Refresh" @click="handleRefresh">
          刷新
        </el-button>
      </div>
    </div>

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
        <el-table-column prop="name" label="属性组名称" min-width="150" />
        <el-table-column prop="categoryName" label="关联属性类型" min-width="200" show-overflow-tooltip />
        <el-table-column prop="remark" label="属性组备注" min-width="300" show-overflow-tooltip />
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
      width="900px"
      :close-on-click-modal="false"
      @close="handleDialogClose"
    >
      <el-form
        ref="formRef"
        :model="formData"
        :rules="formRules"
        label-width="120px"
      >
        <el-form-item label="属性组名称：" prop="name" required>
          <el-input 
            v-model="formData.name" 
            placeholder="请输入属性组名称"
            clearable
          />
        </el-form-item>
        
        <el-form-item label="属性组备注：">
          <el-input 
            v-model="formData.remark" 
            type="textarea"
            :rows="3"
            maxlength="100"
            show-word-limit
            placeholder="请输入属性组备注"
          />
        </el-form-item>

        <el-form-item label="属性值：" required>
          <el-button type="primary" icon="Plus" @click="handleAddAttribute">
            添加
          </el-button>
        </el-form-item>

        <!-- 属性列表 -->
        <div v-if="formData.attributes && formData.attributes.length > 0" class="attribute-list">
          <el-table :data="formData.attributes" border style="width: 100%">
            <el-table-column label="顺序号" width="80" align="center">
              <template #default="{ $index }">
                {{ $index + 1 }}
              </template>
            </el-table-column>
            <el-table-column label="属性类型名称" min-width="200">
              <template #default="{ row }">
                <el-select 
                  v-model="row.attributeTypeId" 
                  placeholder="请选择属性类型"
                  filterable
                  clearable
                  style="width: 100%"
                  @change="handleAttributeTypeChange(row)"
                >
                  <el-option 
                    v-for="type in attributeTypes" 
                    :key="type.id" 
                    :label="type.name" 
                    :value="type.id"
                  />
                </el-select>
              </template>
            </el-table-column>
            <el-table-column label="设为筛选条件" width="150" align="center">
              <template #default="{ row }">
                <el-switch 
                  v-model="row.isFilter" 
                  :active-value="1"
                  :inactive-value="0"
                />
              </template>
            </el-table-column>
            <el-table-column label="操作" width="80" align="center">
              <template #default="{ $index }">
                <el-button 
                  link 
                  type="danger" 
                  @click="handleRemoveAttribute($index)"
                >
                  删除
                </el-button>
              </template>
            </el-table-column>
          </el-table>
        </div>
        <el-empty v-else description="暂无数据" :image-size="80" />
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
import { attributeGroupService } from '@/api/goods/goods.js'

// 响应式数据
const loading = ref(false)
const dialogVisible = ref(false)
const dialogTitle = ref('新增属性组')
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

// 属性类型列表
const attributeTypes = ref([])

// 表单数据
const formData = reactive({
  id: null,
  name: '',
  remark: '',
  attributes: []
})

// 表单验证规则
const formRules = {
  name: [
    { required: true, message: '请输入属性组名称', trigger: 'blur' }
  ]
}

// 方法
const handleAdd = () => {
  isEdit.value = false
  dialogTitle.value = '新增属性组'
  resetForm()
  dialogVisible.value = true
}

const handleEdit = (row) => {
  isEdit.value = true
  dialogTitle.value = '编辑属性组'
  
  // 复制数据到表单
  Object.assign(formData, {
    id: row.id,
    name: row.name,
    remark: row.remark,
    attributes: row.attributes ? JSON.parse(JSON.stringify(row.attributes)) : []
  })
  
  dialogVisible.value = true
}

const handleDelete = (row) => {
  ElMessageBox.confirm(
    `确定要删除属性组“${row.name}”吗？`,
    '确认删除',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(async () => {
    try {
      const res = await attributeGroupService.delete(row.id)
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

const handleAddAttribute = () => {
  formData.attributes.push({
    attributeTypeId: null,
    attributeTypeName: '',
    isFilter: 0
  })
}

const handleRemoveAttribute = (index) => {
  formData.attributes.splice(index, 1)
}

const handleAttributeTypeChange = (row) => {
  const selectedType = attributeTypes.value.find(t => t.id === row.attributeTypeId)
  if (selectedType) {
    row.attributeTypeName = selectedType.name
  }
}

const handleSubmit = async () => {
  if (!formRef.value) return
  
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    
    // 验证至少添加一个属性
    if (!formData.attributes || formData.attributes.length === 0) {
      ElMessage.warning('请至少添加一个属性')
      return
    }
    
    // 验证所有属性都已选择类型
    const hasEmptyType = formData.attributes.some(attr => !attr.attributeTypeId)
    if (hasEmptyType) {
      ElMessage.warning('请为所有属性选择类型')
      return
    }
    
    try {
      const params = {
        name: formData.name,
        remark: formData.remark,
        attributes: formData.attributes
      }
      
      let res
      if (isEdit.value) {
        params.id = formData.id
        res = await attributeGroupService.update(params)
      } else {
        res = await attributeGroupService.create(params)
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
    attributes: []
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
    
    const res = await attributeGroupService.getList(params)
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
    tableData.value = []
    pagination.total = 0
  } finally {
    loading.value = false
  }
}

// 获取属性类型列表
const fetchAttributeTypes = async () => {
  try {
    const res = await attributeGroupService.getAttributeTypes()
    if (res && (res.code === 1 || res.code === 200)) {
      attributeTypes.value = res.data?.list || res.data?.records || res.data || []
    }
  } catch (error) {
    console.error('获取属性类型列表失败:', error)
    // 如果接口失败，使用默认数据
    attributeTypes.value = []
  }
}

// 初始化
onMounted(() => {
  fetchTableData()
  fetchAttributeTypes()
})
</script>

<style scoped>
.attribute-group-container {
  padding: 20px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.page-header h2 {
  margin: 0;
  color: #303133;
}

.table-card {
  margin-bottom: 20px;
}

.pagination-container {
  display: flex;
  justify-content: center;
  margin-top: 20px;
}

.attribute-list {
  margin-top: 10px;
}

:deep(.el-form-item__label) {
  font-weight: normal;
}
</style>
