<template>
  <div class="category-management">
    <el-card class="box-card">
      <template #header>
        <div class="card-header">
          <span>商品分类管理</span>
          <el-button type="primary" @click="handleAdd">新增分类</el-button>
        </div>
      </template>
      
      <!-- 分类树形表格 -->
      <el-table
        :data="categoryList"
        row-key="id"
        border
        :tree-props="{ children: 'children', hasChildren: 'hasChildren' }"
        style="width: 100%"
      >
        <el-table-column prop="name" label="分类名称" width="200" />
        <el-table-column prop="code" label="分类编码" width="150" />
        <el-table-column prop="sort" label="排序" width="100" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="scope">
            <el-tag :type="scope.row.status === 'enabled' ? 'success' : 'info'">
              {{ scope.row.status === 'enabled' ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="180" />
        <el-table-column label="操作" width="250">
          <template #default="scope">
            <el-button size="small" @click="handleAdd(scope.row)">添加子分类</el-button>
            <el-button size="small" type="primary" @click="handleEdit(scope.row)">编辑</el-button>
            <el-button size="small" type="danger" @click="handleDelete(scope.row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 分类编辑对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="500px"
    >
      <el-form :model="categoryForm" :rules="rules" ref="categoryFormRef" label-width="100px">
        <el-form-item label="分类名称" prop="name">
          <el-input v-model="categoryForm.name" placeholder="请输入分类名称" />
        </el-form-item>
        <el-form-item label="分类编码" prop="code">
          <el-input v-model="categoryForm.code" placeholder="请输入分类编码" />
        </el-form-item>
        <el-form-item label="上级分类" prop="parentId">
          <el-tree-select
            v-model="categoryForm.parentId"
            :data="categoryOptions"
            :props="{ label: 'name', value: 'id' }"
            placeholder="请选择上级分类"
            clearable
          />
        </el-form-item>
        <el-form-item label="排序" prop="sort">
          <el-input-number v-model="categoryForm.sort" :min="0" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="categoryForm.status">
            <el-radio label="enabled">启用</el-radio>
            <el-radio label="disabled">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input type="textarea" v-model="categoryForm.description" :rows="3" placeholder="请输入分类描述" />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleSubmit">确定</el-button>
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

// 响应式数据
const dialogVisible = ref(false)
const dialogTitle = ref('新增分类')
const categoryFormRef = ref(null)

// 分类表单
const categoryForm = reactive({
  id: '',
  name: '',
  code: '',
  parentId: null,
  sort: 0,
  status: 'enabled',
  description: ''
})

// 表单验证规则
const rules = {
  name: [{ required: true, message: '请输入分类名称', trigger: 'blur' }],
  code: [{ required: true, message: '请输入分类编码', trigger: 'blur' }],
  sort: [{ required: true, message: '请输入排序值', trigger: 'blur' }]
}

// 分类数据
const categoryList = ref([
  {
    id: 1,
    name: '数码产品',
    code: 'DIGITAL',
    sort: 1,
    status: 'enabled',
    createTime: '2025-01-27 10:00:00',
    children: [
      {
        id: 11,
        name: '手机',
        code: 'PHONE',
        sort: 1,
        status: 'enabled',
        createTime: '2025-01-27 10:00:00'
      },
      {
        id: 12,
        name: '电脑',
        code: 'COMPUTER',
        sort: 2,
        status: 'enabled',
        createTime: '2025-01-27 10:00:00'
      }
    ]
  },
  {
    id: 2,
    name: '服装鞋帽',
    code: 'CLOTHING',
    sort: 2,
    status: 'enabled',
    createTime: '2025-01-27 11:00:00',
    children: [
      {
        id: 21,
        name: '男装',
        code: 'MEN',
        sort: 1,
        status: 'enabled',
        createTime: '2025-01-27 11:00:00'
      },
      {
        id: 22,
        name: '女装',
        code: 'WOMEN',
        sort: 2,
        status: 'enabled',
        createTime: '2025-01-27 11:00:00'
      }
    ]
  }
])

// 分类选项（用于上级分类选择）
const categoryOptions = ref([])

// 生命周期
onMounted(() => {
  initCategoryOptions()
})

// 初始化分类选项
const initCategoryOptions = () => {
  categoryOptions.value = [
    { id: null, name: '顶级分类' },
    ...categoryList.value
  ]
}

// 新增分类
const handleAdd = (parentCategory = null) => {
  dialogTitle.value = parentCategory ? '新增子分类' : '新增分类'
  Object.assign(categoryForm, {
    id: '',
    name: '',
    code: '',
    parentId: parentCategory ? parentCategory.id : null,
    sort: 0,
    status: 'enabled',
    description: ''
  })
  dialogVisible.value = true
}

// 编辑分类
const handleEdit = (row) => {
  dialogTitle.value = '编辑分类'
  Object.assign(categoryForm, row)
  dialogVisible.value = true
}

// 删除分类
const handleDelete = (row) => {
  ElMessageBox.confirm('确定要删除这个分类吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    // 这里应该调用API删除分类
    ElMessage.success('删除成功')
  })
}

// 提交表单
const handleSubmit = async () => {
  if (!categoryFormRef.value) return
  
  try {
    await categoryFormRef.value.validate()
    
    if (categoryForm.id) {
      // 编辑模式
      ElMessage.success('编辑成功')
    } else {
      // 新增模式
      ElMessage.success('新增成功')
    }
    
    dialogVisible.value = false
  } catch (error) {
    console.error('表单验证失败:', error)
  }
}
</script>

<style scoped>
.category-management {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.dialog-footer {
  text-align: right;
}
</style>
