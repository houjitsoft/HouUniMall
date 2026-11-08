<template>
  <div class="attribute-management">
    <el-card class="box-card">
      <template #header>
        <div class="card-header">
          <span>商品属性管理</span>
          <el-button type="primary" @click="handleCreate">新增属性</el-button>
        </div>
      </template>
      
      <!-- 搜索栏 -->
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="属性名称">
          <el-input v-model="searchForm.attributeName" placeholder="请输入属性名称" clearable />
        </el-form-item>
        <el-form-item label="属性类型">
          <el-select v-model="searchForm.attributeType" placeholder="请选择属性类型" clearable>
            <el-option label="文本" value="text" />
            <el-option label="数字" value="number" />
            <el-option label="日期" value="date" />
            <el-option label="单选" value="radio" />
            <el-option label="多选" value="checkbox" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>

      <!-- 属性列表 -->
      <el-table :data="attributeList" style="width: 100%" v-loading="loading">
        <el-table-column prop="attributeId" label="属性ID" width="100" />
        <el-table-column prop="attributeName" label="属性名称" width="200" />
        <el-table-column prop="attributeType" label="属性类型" width="120">
          <template #default="scope">
            {{ getAttributeTypeText(scope.row.attributeType) }}
          </template>
        </el-table-column>
        <el-table-column prop="attributeItem" label="属性值选项" width="250" show-overflow-tooltip />
        <el-table-column prop="createTime" label="创建时间" width="180">
          <template #default="scope">
            {{ formatDate(scope.row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column prop="attributeNotes" label="备注" show-overflow-tooltip />
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

    <!-- 属性编辑对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="600px"
      :close-on-click-modal="false"
    >
      <el-form :model="attributeForm" :rules="attributeRules" ref="attributeFormRef" label-width="100px">
        <el-form-item label="属性名称" prop="attributeName">
          <el-input v-model="attributeForm.attributeName" placeholder="请输入属性名称" />
        </el-form-item>
        <el-form-item label="属性类型" prop="attributeType">
          <el-select v-model="attributeForm.attributeType" placeholder="请选择属性类型" @change="handleTypeChange">
            <el-option label="文本" value="text" />
            <el-option label="数字" value="number" />
            <el-option label="日期" value="date" />
            <el-option label="单选" value="radio" />
            <el-option label="多选" value="checkbox" />
          </el-select>
        </el-form-item>
        <el-form-item 
          v-if="attributeForm.attributeType === 'radio' || attributeForm.attributeType === 'checkbox'" 
          label="属性值选项" 
          prop="attributeItem"
        >
          <el-input 
            v-model="attributeForm.attributeItem" 
            type="textarea" 
            :rows="4" 
            placeholder="请输入属性值选项，多个选项用逗号分隔" 
          />
          <div class="form-tip">多个选项请用逗号分隔，例如：红色,蓝色,绿色</div>
        </el-form-item>
        <el-form-item label="备注">
          <el-input 
            v-model="attributeForm.attributeNotes" 
            type="textarea" 
            :rows="3" 
            placeholder="请输入备注信息" 
          />
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
import { attributeApi } from '../../api/goods/goods'

// 响应式数据
const loading = ref(false)
const submitLoading = ref(false)
const dialogVisible = ref(false)
const dialogTitle = ref('')
const attributeFormRef = ref(null)
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

// 搜索表单
const searchForm = reactive({
  attributeName: '',
  attributeType: '',
  pageNum: 1,
  pageSize: 10
})

// 属性表单
const attributeForm = reactive({
  attributeId: '',
  attributeName: '',
  attributeType: '',
  attributeItem: '',
  attributeNotes: ''
})

// 属性表单规则
const attributeRules = {
  attributeName: [
    { required: true, message: '请输入属性名称', trigger: 'blur' }
  ],
  attributeType: [
    { required: true, message: '请选择属性类型', trigger: 'change' }
  ]
}

// 属性列表
const attributeList = ref([])

// 属性类型映射
const ATTRIBUTE_TYPE_MAP = {
  'text': '文本',
  'number': '数字',
  'date': '日期',
  'radio': '单选',
  'checkbox': '多选'
}

// 生命周期
onMounted(() => {
  fetchAttributeList()
})

// 获取属性列表
const fetchAttributeList = async () => {
  loading.value = true
  try {
    const params = {
      ...searchForm,
      pageNum: currentPage.value,
      pageSize: pageSize.value
    }
    
    const response = await attributeApi.getAttributeList(params)
    
    if (response.code === 200 || response.success) {
      const data = response.data || response
      attributeList.value = data.records || data.list || []
      total.value = data.total || 0
    } else {
      ElMessage.error(response.message || '获取属性列表失败')
      attributeList.value = []
      total.value = 0
    }
  } catch (error) {
    console.error('获取属性列表失败:', error)
    ElMessage.error('获取属性列表失败: ' + (error.message || '网络错误'))
    attributeList.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}

// 获取属性类型文本
const getAttributeTypeText = (type) => {
  return ATTRIBUTE_TYPE_MAP[type] || '未知类型'
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
  fetchAttributeList()
}

// 重置搜索
const handleReset = () => {
  Object.assign(searchForm, {
    attributeName: '',
    attributeType: '',
    pageNum: 1,
    pageSize: 10
  })
  currentPage.value = 1
  fetchAttributeList()
}

// 属性类型改变处理
const handleTypeChange = (value) => {
  // 如果不是单选或多选，清空属性值选项
  if (value !== 'radio' && value !== 'checkbox') {
    attributeForm.attributeItem = ''
  }
}

// 新增属性
const handleCreate = () => {
  dialogTitle.value = '新增属性'
  dialogVisible.value = true
  // 重置表单
  Object.assign(attributeForm, {
    attributeId: '',
    attributeName: '',
    attributeType: '',
    attributeItem: '',
    attributeNotes: ''
  })
}

// 编辑属性
const handleEdit = (row) => {
  dialogTitle.value = '编辑属性'
  dialogVisible.value = true
  // 填充表单数据
  Object.assign(attributeForm, row)
}

// 查看属性
const handleView = (row) => {
  dialogTitle.value = '查看属性'
  dialogVisible.value = true
  // 填充表单数据（只读）
  Object.assign(attributeForm, row)
  // 这里可以设置表单为只读模式
}

// 删除属性
const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm('确定要删除该属性吗？删除后不可恢复', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    const response = await attributeApi.deleteAttribute(row.attributeId)
    
    if (response.code === 200 || response.success) {
      ElMessage.success('删除成功')
      fetchAttributeList()
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

// 提交表单
const handleSubmit = async () => {
  try {
    await attributeFormRef.value.validate()
    
    submitLoading.value = true
    let response
    
    if (attributeForm.attributeId) {
      // 编辑属性
      response = await attributeApi.updateAttribute(attributeForm)
    } else {
      // 新增属性
      response = await attributeApi.createAttribute(attributeForm)
    }
    
    if (response.code === 200 || response.success) {
      ElMessage.success(attributeForm.attributeId ? '编辑成功' : '新增成功')
      dialogVisible.value = false
      fetchAttributeList()
    } else {
      ElMessage.error(response.message || (attributeForm.attributeId ? '编辑失败' : '新增失败'))
    }
  } catch (error) {
    console.error(attributeForm.attributeId ? '编辑失败:' : '新增失败:', error)
    ElMessage.error((attributeForm.attributeId ? '编辑失败:' : '新增失败:') + (error.message || '网络错误'))
  } finally {
    submitLoading.value = false
  }
}

// 分页大小改变
const handleSizeChange = (val) => {
  pageSize.value = val
  searchForm.pageSize = val
  fetchAttributeList()
}

// 当前页改变
const handleCurrentChange = (val) => {
  currentPage.value = val
  searchForm.pageNum = val
  fetchAttributeList()
}
</script>

<style scoped>
.attribute-management {
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

.form-tip {
  font-size: 12px;
  color: #909399;
  margin-top: 5px;
}

.pagination-container {
  margin-top: 20px;
  text-align: right;
}

.dialog-footer {
  text-align: right;
}
</style>
