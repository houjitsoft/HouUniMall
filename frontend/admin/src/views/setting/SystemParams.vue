<template>
  <div class="system-params">
    <el-card class="box-card">
      <template #header>
        <div class="card-header">
          <span>系统参数配置</span>
          <el-button type="primary" @click="showAddDialog">新增参数</el-button>
        </div>
      </template>
      
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="参数名称">
          <el-input v-model="searchForm.paramName" placeholder="请输入参数名称" clearable></el-input>
        </el-form-item>
        <el-form-item label="参数类型">
          <el-select v-model="searchForm.paramType" placeholder="请选择参数类型" clearable>
            <el-option label="系统参数" value="system"></el-option>
            <el-option label="业务参数" value="business"></el-option>
            <el-option label="安全参数" value="security"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="searchParams">搜索</el-button>
          <el-button @click="resetSearch">重置</el-button>
        </el-form-item>
      </el-form>
      
      <el-table :data="paramList" style="width: 100%" v-loading="loading">
        <el-table-column prop="paramName" label="参数名称" width="200"></el-table-column>
        <el-table-column prop="paramKey" label="参数键" width="200"></el-table-column>
        <el-table-column prop="paramValue" label="参数值" min-width="200"></el-table-column>
        <el-table-column prop="paramType" label="参数类型" width="120">
          <template #default="scope">
            <el-tag :type="getParamTypeTag(scope.row.paramType)">
              {{ getParamTypeName(scope.row.paramType) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="description" label="描述" min-width="200"></el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="scope">
            <el-tag :type="scope.row.status === 1 ? 'success' : 'danger'">
              {{ scope.row.status === 1 ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="updateTime" label="更新时间" width="180"></el-table-column>
        <el-table-column label="操作" width="200">
          <template #default="scope">
            <el-button size="small" @click="editParam(scope.row)">编辑</el-button>
            <el-button size="small" type="danger" @click="deleteParam(scope.row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      
      <!-- 分页组件 -->
      <el-pagination
        v-model:current-page="pagination.current"
        v-model:page-size="pagination.pageSize"
        :page-sizes="[10, 20, 50, 100]"
        :total="pagination.total"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="handleSizeChange"
        @current-change="handlePageChange"
        style="margin-top: 20px; text-align: right;"
      />
    </el-card>
    
    <!-- 新增/编辑参数对话框 -->
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="600px">
      <el-form :model="paramForm" label-width="100px">
        <el-form-item label="参数名称" required>
          <el-input v-model="paramForm.paramName" placeholder="请输入参数名称"></el-input>
        </el-form-item>
        <el-form-item label="参数键" required>
          <el-input v-model="paramForm.paramKey" placeholder="请输入参数键"></el-input>
        </el-form-item>
        <el-form-item label="参数值" required>
          <el-input v-model="paramForm.paramValue" placeholder="请输入参数值"></el-input>
        </el-form-item>
        <el-form-item label="参数类型" required>
          <el-select v-model="paramForm.paramType" placeholder="请选择参数类型">
            <el-option label="系统参数" value="system"></el-option>
            <el-option label="业务参数" value="business"></el-option>
            <el-option label="安全参数" value="security"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="paramForm.description" type="textarea" placeholder="请输入参数描述"></el-input>
        </el-form-item>
        <el-form-item label="状态">
          <el-switch v-model="paramForm.status" :active-value="1" :inactive-value="0"></el-switch>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveParam">保存</el-button>
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
import { systemParamsService } from '@/api/setting/setting.js'

const searchForm = reactive({
  paramName: '',
  paramType: ''
})

const paramList = ref([])
const loading = ref(false)
const dialogVisible = ref(false)
const dialogTitle = ref('')
const isEdit = ref(false)

const paramForm = reactive({
  id: null,
  paramName: '',
  paramKey: '',
  paramValue: '',
  paramType: '',
  description: '',
  status: 1
})

// 分页信息
const pagination = reactive({
  current: 1,
  pageSize: 10,
  total: 0
})

// 加载参数列表
const loadParamsList = async () => {
  try {
    loading.value = true
    const params = {
      ...searchForm,
      page: pagination.current,
      size: pagination.pageSize
    }
    const response = await systemParamsService.getParamsList(params)
    if (response.code === 1 && response.data) {
      paramList.value = response.data.list || []
      pagination.total = response.data.total || 0
    }
  } catch (error) {
    console.error('加载参数列表失败:', error)
    ElMessage.error('加载参数列表失败')
  } finally {
    loading.value = false
  }
}

// 搜索参数
const searchParams = () => {
  pagination.current = 1
  loadParamsList()
}

// 重置搜索
const resetSearch = () => {
  Object.assign(searchForm, {
    paramName: '',
    paramType: ''
  })
  pagination.current = 1
  loadParamsList()
}

// 显示新增对话框
const showAddDialog = () => {
  dialogTitle.value = '新增参数'
  isEdit.value = false
  resetForm()
  dialogVisible.value = true
}

// 编辑参数
const editParam = async (row) => {
  try {
    const response = await systemParamsService.getParamDetail(row.id)
    if (response.code === 1 && response.data) {
      dialogTitle.value = '编辑参数'
      isEdit.value = true
      Object.assign(paramForm, response.data)
      dialogVisible.value = true
    } else {
      ElMessage.error('获取参数详情失败')
    }
  } catch (error) {
    console.error('获取参数详情失败:', error)
    ElMessage.error('获取参数详情失败')
  }
}

// 删除参数
const deleteParam = async (row) => {
  try {
    await ElMessageBox.confirm('确定要删除这个参数吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    const response = await systemParamsService.deleteParam(row.id)
    if (response.code === 1) {
      ElMessage.success('删除成功')
      loadParamsList()
    } else {
      ElMessage.error(response.message || '删除失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除参数失败:', error)
      ElMessage.error('删除参数失败')
    }
  }
}

// 保存参数
const saveParam = async () => {
  if (!paramForm.paramName || !paramForm.paramKey || !paramForm.paramValue || !paramForm.paramType) {
    ElMessage.warning('请填写完整信息')
    return
  }
  
  try {
    loading.value = true
    let response
    
    if (isEdit.value) {
      response = await systemParamsService.updateParam(paramForm.id, paramForm)
    } else {
      response = await systemParamsService.createParam(paramForm)
    }
    
    if (response.code === 1) {
      ElMessage.success(isEdit.value ? '编辑成功' : '新增成功')
      dialogVisible.value = false
      loadParamsList()
    } else {
      ElMessage.error(response.message || '保存失败')
    }
  } catch (error) {
    console.error('保存参数失败:', error)
    ElMessage.error('保存参数失败')
  } finally {
    loading.value = false
  }
}

// 重置表单
const resetForm = () => {
  Object.assign(paramForm, {
    id: null,
    paramName: '',
    paramKey: '',
    paramValue: '',
    paramType: '',
    description: '',
    status: 1
  })
}

// 获取参数类型标签
const getParamTypeTag = (type) => {
  const typeMap = {
    system: 'primary',
    business: 'success',
    security: 'danger'
  }
  return typeMap[type] || 'info'
}

// 获取参数类型名称
const getParamTypeName = (type) => {
  const typeMap = {
    system: '系统参数',
    business: '业务参数',
    security: '安全参数'
  }
  return typeMap[type] || '未知类型'
}

// 分页变化
const handlePageChange = (page) => {
  pagination.current = page
  loadParamsList()
}

// 页面大小变化
const handleSizeChange = (size) => {
  pagination.pageSize = size
  pagination.current = 1
  loadParamsList()
}

onMounted(() => {
  loadParamsList()
})
</script>

<style scoped>
.system-params {
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
</style>
