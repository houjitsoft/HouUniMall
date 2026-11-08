<template>
  <div class="attribute-management">
    <!-- 页面标题和统计卡片 -->
    <el-row :gutter="20" style="margin-bottom: 20px;">
      <el-col :span="6">
        <el-card class="stat-card total-attrs">
          <div class="stat-content">
            <div class="stat-icon"><el-icon><Grid /></el-icon></div>
            <div class="stat-info">
              <div class="stat-value">{{ attrStats.total }}</div>
              <div class="stat-label">总属性数</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card active-attrs">
          <div class="stat-content">
            <div class="stat-icon"><el-icon><CircleCheck /></el-icon></div>
            <div class="stat-info">
              <div class="stat-value">{{ attrStats.active }}</div>
              <div class="stat-label">启用属性</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card category-attrs">
          <div class="stat-content">
            <div class="stat-icon"><el-icon><Menu /></el-icon></div>
            <div class="stat-info">
              <div class="stat-value">{{ attrStats.categories }}</div>
              <div class="stat-label">分类属性</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card required-attrs">
          <div class="stat-content">
            <div class="stat-icon"><el-icon><Warning /></el-icon></div>
            <div class="stat-info">
              <div class="stat-value">{{ attrStats.required }}</div>
              <div class="stat-label">必选属性</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 搜索筛选 -->
    <el-card class="search-card">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="属性名称">
          <el-input
            v-model="searchForm.name"
            placeholder="请输入属性名称"
            clearable
          />
        </el-form-item>
        <el-form-item label="属性类型">
          <el-select v-model="searchForm.type" placeholder="请选择属性类型" clearable>
            <el-option label="规格属性" value="spec" />
            <el-option label="销售属性" value="sales" />
            <el-option label="基本属性" value="basic" />
            <el-option label="扩展属性" value="extra" />
          </el-select>
        </el-form-item>
        <el-form-item label="属性状态">
          <el-select v-model="searchForm.status" placeholder="请选择状态" clearable>
            <el-option label="启用" value="active" />
            <el-option label="禁用" value="inactive" />
          </el-select>
        </el-form-item>
        <el-form-item label="所属分类">
          <el-select v-model="searchForm.categoryId" placeholder="请选择分类" clearable>
            <el-option label="全部分类" value="" />
            <el-option label="服装" value="1" />
            <el-option label="数码" value="2" />
            <el-option label="家居" value="3" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">
            <el-icon><Search /></el-icon>搜索
          </el-button>
          <el-button @click="handleReset">
            <el-icon><Refresh /></el-icon>重置
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 操作工具栏 -->
    <el-card class="toolbar-card">
      <div class="toolbar">
        <div class="toolbar-left">
          <el-button type="primary" @click="handleAdd">
            <el-icon><Plus /></el-icon>新增属性
          </el-button>
          <el-button type="success" @click="handleBatchImport">
            <el-icon><Upload /></el-icon>批量导入
          </el-button>
          <el-button type="warning" @click="handleExport">
            <el-icon><Download /></el-icon>导出数据
          </el-button>
          <el-button type="info" @click="handleBatchEdit">
            <el-icon><Edit /></el-icon>批量编辑
          </el-button>
        </div>
        <div class="toolbar-right">
          <el-button @click="handleRefresh">
            <el-icon><Refresh /></el-icon>刷新
          </el-button>
        </div>
      </div>
    </el-card>

    <!-- 数据表格 -->
    <el-card class="table-card">
      <el-table
        :data="attributeList"
        v-loading="loading"
        style="width: 100%"
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="55" />
        <el-table-column prop="name" label="属性名称" width="150" />
        <el-table-column prop="type" label="属性类型" width="120">
          <template #default="{ row }">
            <el-tag :type="getTypeColor(row.type)">
              {{ getTypeText(row.type) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="inputType" label="录入方式" width="100">
          <template #default="{ row }">
            <el-tag :type="getInputTypeColor(row.inputType)">
              {{ getInputTypeText(row.inputType) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="values" label="属性值" width="200">
          <template #default="{ row }">
            <div class="attr-values">
              <el-tag 
                v-for="value in row.values.slice(0, 3)" 
                :key="value.id" 
                size="small" 
                style="margin-right: 5px;"
              >
                {{ value.value }}
              </el-tag>
              <span v-if="row.values.length > 3" class="more-values">
                等{{ row.values.length }}个值
              </span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="sort" label="排序" width="80" />
        <el-table-column prop="isRequired" label="必选" width="80">
          <template #default="{ row }">
            <el-tag :type="row.isRequired ? 'danger' : 'info'">
              {{ row.isRequired ? '是' : '否' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-switch
              v-model="row.status"
              active-value="active"
              inactive-value="inactive"
              @change="handleStatusChange(row)"
            />
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="160" />
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="handleEdit(row)">
              <el-icon><Edit /></el-icon>编辑
            </el-button>
            <el-button link type="success" @click="handleView(row)">
              <el-icon><View /></el-icon>查看
            </el-button>
            <el-button link type="warning" @click="handleManageValues(row)">
              <el-icon><Setting /></el-icon>属性值
            </el-button>
            <el-button link type="danger" @click="handleDelete(row)">
              <el-icon><Delete /></el-icon>删除
            </el-button>
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

    <!-- 新增/编辑对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="800px"
      @close="resetForm"
    >
      <el-form
        ref="formRef"
        :model="formData"
        :rules="formRules"
        label-width="100px"
      >
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="属性名称" prop="name">
              <el-input v-model="formData.name" placeholder="请输入属性名称" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="属性类型" prop="type">
              <el-select v-model="formData.type" placeholder="请选择属性类型">
                <el-option label="规格属性" value="spec" />
                <el-option label="销售属性" value="sales" />
                <el-option label="基本属性" value="basic" />
                <el-option label="扩展属性" value="extra" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="录入方式" prop="inputType">
              <el-select v-model="formData.inputType" placeholder="请选择录入方式">
                <el-option label="手工录入" value="manual" />
                <el-option label="列表选择" value="select" />
                <el-option label="多选" value="multiple" />
                <el-option label="颜色选择" value="color" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="所属分类" prop="categoryId">
              <el-select v-model="formData.categoryId" placeholder="请选择分类">
                <el-option label="服装" value="1" />
                <el-option label="数码" value="2" />
                <el-option label="家居" value="3" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="排序">
              <el-input-number v-model="formData.sort" :min="0" :max="999" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="是否必选">
              <el-switch v-model="formData.isRequired" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="属性描述">
          <el-input
            v-model="formData.description"
            type="textarea"
            :rows="3"
            placeholder="请输入属性描述"
          />
        </el-form-item>
        <el-form-item label="属性值" v-if="formData.inputType !== 'manual'">
          <div class="attr-values-editor">
            <div v-for="(value, index) in formData.values" :key="index" class="value-item">
              <el-input
                v-model="value.value"
                placeholder="属性值"
                style="width: 200px; margin-right: 10px;"
              />
              <el-color-picker
                v-if="formData.inputType === 'color'"
                v-model="value.color"
                style="margin-right: 10px;"
              />
              <el-button type="danger" @click="removeValue(index)">
                <el-icon><Delete /></el-icon>
              </el-button>
            </div>
            <el-button type="primary" @click="addValue">
              <el-icon><Plus /></el-icon>添加属性值
            </el-button>
          </div>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>

    <!-- 属性值管理对话框 -->
    <el-dialog
      v-model="valuesDialogVisible"
      title="属性值管理"
      width="600px"
    >
      <div class="values-management">
        <div class="values-toolbar">
          <el-button type="primary" @click="addAttributeValue">
            <el-icon><Plus /></el-icon>新增属性值
          </el-button>
        </div>
        <el-table :data="currentAttrValues" style="width: 100%">
          <el-table-column prop="value" label="属性值" />
          <el-table-column prop="color" label="颜色" width="100">
            <template #default="{ row }">
              <div
                v-if="row.color"
                class="color-preview"
                :style="{ backgroundColor: row.color }"
              ></div>
            </template>
          </el-table-column>
          <el-table-column prop="sort" label="排序" width="80" />
          <el-table-column label="操作" width="120">
            <template #default="{ row, $index }">
              <el-button link type="primary" @click="editAttributeValue(row, $index)">
                编辑
              </el-button>
              <el-button link type="danger" @click="deleteAttributeValue($index)">
                删除
              </el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>

// 使用 Element Plus 国际化系统
import { useI18n } from '@/i18n-element'
const { t } = useI18n()
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Grid, CircleCheck, Menu, Warning, Search, Refresh, Plus, Upload, Download, Edit,
  View, Setting, Delete
} from '@element-plus/icons-vue'
import { attributeService } from '@/api/goods/goods.js'

// 响应式数据
const loading = ref(false)
const dialogVisible = ref(false)
const valuesDialogVisible = ref(false)
const dialogTitle = ref('新增属性')
const attributeList = ref([])
const selectedAttributes = ref([])
const currentAttrValues = ref([])
const currentAttribute = ref(null)

// 统计数据
const attrStats = reactive({
  total: 156,
  active: 142,
  categories: 23,
  required: 45
})

// 搜索表单
const searchForm = reactive({
  name: '',
  type: '',
  status: '',
  categoryId: ''
})

// 分页信息
const pagination = reactive({
  page: 1,
  size: 10,
  total: 0
})

// 表单数据
const formData = reactive({
  id: '',
  name: '',
  type: '',
  inputType: '',
  categoryId: '',
  sort: 0,
  isRequired: false,
  description: '',
  values: []
})

// 表单验证规则
const formRules = {
  name: [{ required: true, message: '请输入属性名称', trigger: 'blur' }],
  type: [{ required: true, message: '请选择属性类型', trigger: 'change' }],
  inputType: [{ required: true, message: '请选择录入方式', trigger: 'change' }]
}

// 组件引用
const formRef = ref()

// 初始化
onMounted(() => {
  fetchAttributeList()
})

// 获取属性列表
const fetchAttributeList = async () => {
  loading.value = true
  try {
    const params = {
      page: pagination.page,
      size: pagination.size,
      ...searchForm
    }
    const res = await attributeService.getAttributeList(params)
    if (res && res.code === 200) {
      attributeList.value = res.data?.records || res.data?.list || []
      pagination.total = res.data?.total || 0
      // 更新统计数据
      attrStats.total = pagination.total
      attrStats.active = attributeList.value.filter(a => a.status === 'active').length
    }
  } catch (error) {
    console.error('获取属性列表失败:', error)
    ElMessage.error('获取属性列表失败')
  } finally {
    loading.value = false
  }
}

// 搜索处理
const handleSearch = () => {
  pagination.page = 1
  fetchAttributeList()
}

// 重置搜索
const handleReset = () => {
  Object.assign(searchForm, {
    name: '',
    type: '',
    status: '',
    categoryId: ''
  })
  fetchAttributeList()
}

// 新增属性
const handleAdd = () => {
  dialogTitle.value = '新增属性'
  resetForm()
  dialogVisible.value = true
}

// 编辑属性
const handleEdit = (row) => {
  dialogTitle.value = '编辑属性'
  Object.assign(formData, { ...row })
  dialogVisible.value = true
}

// 查看属性
const handleView = (row) => {
  // 实现查看逻辑
  ElMessage.info('查看属性功能')
}

// 删除属性
const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm('确定要删除这个属性吗？', '确认删除', {
      type: 'warning'
    })
    
    // 实现删除逻辑
    ElMessage.success('删除成功')
    fetchAttributeList()
  } catch (error) {
    // 用户取消删除
  }
}

// 状态变更
const handleStatusChange = (row) => {
  // 实现状态变更逻辑
  ElMessage.success(`属性状态已${row.status === 'active' ? '启用' : '禁用'}`)
}

// 管理属性值
const handleManageValues = (row) => {
  currentAttribute.value = row
  currentAttrValues.value = [...row.values]
  valuesDialogVisible.value = true
}

// 表单提交
const handleSubmit = async () => {
  try {
    await formRef.value.validate()
    
    // 实现提交逻辑
    ElMessage.success(dialogTitle.value.includes('新增') ? '新增成功' : '编辑成功')
    dialogVisible.value = false
    fetchAttributeList()
  } catch (error) {
    ElMessage.error('表单验证失败')
  }
}

// 重置表单
const resetForm = () => {
  Object.assign(formData, {
    id: '',
    name: '',
    type: '',
    inputType: '',
    categoryId: '',
    sort: 0,
    isRequired: false,
    description: '',
    values: []
  })
}

// 添加属性值
const addValue = () => {
  formData.values.push({
    value: '',
    color: '',
    sort: formData.values.length
  })
}

// 移除属性值
const removeValue = (index) => {
  formData.values.splice(index, 1)
}

// 新增属性值
const addAttributeValue = () => {
  currentAttrValues.value.push({
    value: '',
    color: '',
    sort: currentAttrValues.value.length
  })
}

// 编辑属性值
const editAttributeValue = (row, index) => {
  // 实现编辑属性值逻辑
}

// 删除属性值
const deleteAttributeValue = (index) => {
  currentAttrValues.value.splice(index, 1)
}

// 选择变更
const handleSelectionChange = (selection) => {
  selectedAttributes.value = selection
}

// 分页处理
const handleSizeChange = (size) => {
  pagination.size = size
  fetchAttributeList()
}

const handleCurrentChange = (page) => {
  pagination.page = page
  fetchAttributeList()
}

// 其他操作
const handleBatchImport = () => {
  ElMessage.info('批量导入功能')
}

const handleExport = () => {
  ElMessage.info('导出数据功能')
}

const handleBatchEdit = () => {
  ElMessage.info('批量编辑功能')
}

const handleRefresh = () => {
  fetchAttributeList()
}

// 工具函数
const getTypeText = (type) => {
  const map = {
    'spec': '规格属性',
    'sales': '销售属性',
    'basic': '基本属性',
    'extra': '扩展属性'
  }
  return map[type] || type
}

const getTypeColor = (type) => {
  const map = {
    'spec': 'primary',
    'sales': 'success',
    'basic': 'warning',
    'extra': 'info'
  }
  return map[type] || 'info'
}

const getInputTypeText = (type) => {
  const map = {
    'manual': '手工录入',
    'select': '列表选择',
    'multiple': '多选',
    'color': '颜色选择'
  }
  return map[type] || type
}

const getInputTypeColor = (type) => {
  const map = {
    'manual': 'info',
    'select': 'primary',
    'multiple': 'success',
    'color': 'warning'
  }
  return map[type] || 'info'
}

// 模拟数据
const mockAttributeData = () => {
  const types = ['spec', 'sales', 'basic', 'extra']
  const inputTypes = ['manual', 'select', 'multiple', 'color']
  const data = []
  
  for (let i = 1; i <= 20; i++) {
    const type = types[Math.floor(Math.random() * types.length)]
    const inputType = inputTypes[Math.floor(Math.random() * inputTypes.length)]
    
    data.push({
      id: i,
      name: `属性${i}`,
      type: type,
      inputType: inputType,
      categoryId: Math.floor(Math.random() * 3) + 1,
      sort: i,
      isRequired: Math.random() > 0.7,
      status: Math.random() > 0.2 ? 'active' : 'inactive',
      description: `属性${i}的描述信息`,
      values: inputType === 'manual' ? [] : [
        { id: 1, value: `值${i}-1`, color: inputType === 'color' ? '#ff0000' : '', sort: 1 },
        { id: 2, value: `值${i}-2`, color: inputType === 'color' ? '#00ff00' : '', sort: 2 },
        { id: 3, value: `值${i}-3`, color: inputType === 'color' ? '#0000ff' : '', sort: 3 }
      ],
      createTime: new Date(Date.now() - Math.random() * 30 * 24 * 60 * 60 * 1000).toLocaleString()
    })
  }
  
  return data
}
</script>

<style scoped>
.attribute-management {
  padding: 20px;
}

.stat-card {
  border-left: 4px solid #409eff;
}

.stat-card.total-attrs {
  border-left-color: #409eff;
}

.stat-card.active-attrs {
  border-left-color: #67c23a;
}

.stat-card.category-attrs {
  border-left-color: #e6a23c;
}

.stat-card.required-attrs {
  border-left-color: #f56c6c;
}

.stat-content {
  display: flex;
  align-items: center;
  padding: 10px;
}

.stat-icon {
  font-size: 40px;
  margin-right: 20px;
  color: #409eff;
}

.stat-info {
  flex: 1;
}

.stat-value {
  font-size: 28px;
  font-weight: bold;
  color: #303133;
  line-height: 1;
}

.stat-label {
  font-size: 14px;
  color: #909399;
  margin-top: 5px;
}

.search-card,
.toolbar-card,
.table-card {
  margin-bottom: 20px;
}

.search-form {
  margin-bottom: 0;
}

.toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.pagination-container {
  display: flex;
  justify-content: center;
  margin-top: 20px;
}

.attr-values {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
}

.more-values {
  color: #909399;
  font-size: 12px;
}

.attr-values-editor {
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  padding: 10px;
}

.value-item {
  display: flex;
  align-items: center;
  margin-bottom: 10px;
}

.values-management {
  max-height: 400px;
}

.values-toolbar {
  margin-bottom: 15px;
}

.color-preview {
  width: 20px;
  height: 20px;
  border-radius: 2px;
  border: 1px solid #dcdfe6;
}
</style>
