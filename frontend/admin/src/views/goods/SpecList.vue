<template>
  <div class="spec-management">
    <el-card class="box-card">
      <template #header>
        <div class="card-header">
          <span>商品规格管理</span>
          <el-button type="primary" @click="handleCreate">新增规格</el-button>
        </div>
      </template>
      
      <!-- 搜索栏 -->
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="规格名称">
          <el-input v-model="searchForm.specName" placeholder="请输入规格名称" clearable />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>

      <!-- 规格列表 -->
      <el-table :data="specList" style="width: 100%" v-loading="loading">
        <el-table-column prop="specId" label="规格ID" width="100" />
        <el-table-column prop="specName" label="规格名称" width="200" />
        <el-table-column prop="specValues" label="规格值" width="300">
          <template #default="scope">
            <el-tag 
              v-for="value in scope.row.specValues" 
              :key="value" 
              class="spec-value-tag"
            >
              {{ value }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="displayOrder" label="排序" width="100" />
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

    <!-- 规格编辑对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="600px"
      :close-on-click-modal="false"
    >
      <el-form :model="specForm" :rules="specRules" ref="specFormRef" label-width="100px">
        <el-form-item label="规格名称" prop="specName">
          <el-input v-model="specForm.specName" placeholder="请输入规格名称" />
        </el-form-item>
        <el-form-item label="排序" prop="displayOrder">
          <el-input-number 
            v-model="specForm.displayOrder" 
            :min="0" 
            :max="999" 
            controls-position="right" 
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="规格值" prop="specValues">
          <el-select 
            v-model="specForm.specValues" 
            multiple 
            filterable 
            allow-create 
            default-first-option
            placeholder="请输入规格值，可创建新值"
            style="width: 100%"
          >
            <el-option
              v-for="item in existingSpecValues"
              :key="item"
              :label="item"
              :value="item"
            />
          </el-select>
          <div class="form-tip">可输入新的规格值，按回车确认添加</div>
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
import { specApi } from '../../api/goods/goods'

// 响应式数据
const loading = ref(false)
const submitLoading = ref(false)
const dialogVisible = ref(false)
const dialogTitle = ref('')
const specFormRef = ref(null)
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

// 搜索表单
const searchForm = reactive({
  specName: '',
  pageNum: 1,
  pageSize: 10
})

// 规格表单
const specForm = reactive({
  specId: '',
  specName: '',
  specValues: [],
  displayOrder: 0
})

// 规格表单规则
const specRules = {
  specName: [
    { required: true, message: '请输入规格名称', trigger: 'blur' }
  ],
  specValues: [
    { required: true, message: '请至少添加一个规格值', trigger: 'change' }
  ]
}

// 规格列表
const specList = ref([])

// 已存在的规格值（用于下拉选项）
const existingSpecValues = ref([])

// 生命周期
onMounted(() => {
  fetchSpecList()
  fetchExistingSpecValues()
})

// 获取规格列表
const fetchSpecList = async () => {
  loading.value = true
  try {
    const params = {
      ...searchForm,
      pageNum: currentPage.value,
      pageSize: pageSize.value
    }
    
    const response = await specApi.getSpecList(params)
    
    if (response.code === 200 || response.success) {
      const data = response.data || response
      specList.value = data.records || data.list || []
      total.value = data.total || 0
    } else {
      ElMessage.error(response.message || '获取规格列表失败')
      specList.value = []
      total.value = 0
    }
  } catch (error) {
    console.error('获取规格列表失败:', error)
    ElMessage.error('获取规格列表失败: ' + (error.message || '网络错误'))
    specList.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}

// 获取已存在的规格值
const fetchExistingSpecValues = async () => {
  try {
    // 这里可以调用专门获取所有规格值的接口
    // 暂时使用模拟数据
    existingSpecValues.value = ['红色', '蓝色', '绿色', '黑色', '白色', 'S', 'M', 'L', 'XL', 'XXL']
  } catch (error) {
    console.error('获取规格值失败:', error)
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
  fetchSpecList()
}

// 重置搜索
const handleReset = () => {
  Object.assign(searchForm, {
    specName: '',
    pageNum: 1,
    pageSize: 10
  })
  currentPage.value = 1
  fetchSpecList()
}

// 新增规格
const handleCreate = () => {
  dialogTitle.value = '新增规格'
  dialogVisible.value = true
  // 重置表单
  Object.assign(specForm, {
    specId: '',
    specName: '',
    specValues: [],
    displayOrder: 0
  })
}

// 编辑规格
const handleEdit = (row) => {
  dialogTitle.value = '编辑规格'
  dialogVisible.value = true
  // 填充表单数据
  Object.assign(specForm, {
    specId: row.specId,
    specName: row.specName,
    specValues: row.specValues || [],
    displayOrder: row.displayOrder || 0
  })
}

// 查看规格
const handleView = (row) => {
  dialogTitle.value = '查看规格'
  dialogVisible.value = true
  // 填充表单数据（只读）
  Object.assign(specForm, {
    specId: row.specId,
    specName: row.specName,
    specValues: row.specValues || [],
    displayOrder: row.displayOrder || 0
  })
  // 这里可以设置表单为只读模式
}

// 删除规格
const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm('确定要删除该规格吗？删除后不可恢复', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    const response = await specApi.deleteSpec(row.specId)
    
    if (response.code === 200 || response.success) {
      ElMessage.success('删除成功')
      fetchSpecList()
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
    await specFormRef.value.validate()
    
    submitLoading.value = true
    let response
    
    if (specForm.specId) {
      // 编辑规格
      response = await specApi.updateSpec(specForm)
    } else {
      // 新增规格
      response = await specApi.createSpec(specForm)
    }
    
    if (response.code === 200 || response.success) {
      ElMessage.success(specForm.specId ? '编辑成功' : '新增成功')
      dialogVisible.value = false
      fetchSpecList()
    } else {
      ElMessage.error(response.message || (specForm.specId ? '编辑失败' : '新增失败'))
    }
  } catch (error) {
    console.error(specForm.specId ? '编辑失败:' : '新增失败:', error)
    ElMessage.error((specForm.specId ? '编辑失败:' : '新增失败:') + (error.message || '网络错误'))
  } finally {
    submitLoading.value = false
  }
}

// 分页大小改变
const handleSizeChange = (val) => {
  pageSize.value = val
  searchForm.pageSize = val
  fetchSpecList()
}

// 当前页改变
const handleCurrentChange = (val) => {
  currentPage.value = val
  searchForm.pageNum = val
  fetchSpecList()
}
</script>

<style scoped>
.spec-management {
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

.spec-value-tag {
  margin-right: 5px;
  margin-bottom: 5px;
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
