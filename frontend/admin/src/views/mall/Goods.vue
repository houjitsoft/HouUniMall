<template>
  <div class="goods-management">
    <el-card class="box-card">
      <template #header>
        <div class="card-header">
          <span>商品管理</span>
          <el-button type="primary" @click="handleAdd">新增商品</el-button>
        </div>
      </template>
      
      <!-- 搜索栏 -->
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="商品名称">
          <el-input v-model="searchForm.name" placeholder="请输入商品名称" clearable />
        </el-form-item>
        <el-form-item label="商品分类">
          <el-select v-model="searchForm.category" placeholder="请选择分类" clearable>
            <el-option label="电子产品" value="electronics" />
            <el-option label="服装鞋帽" value="clothing" />
            <el-option label="食品饮料" value="food" />
            <el-option label="家居用品" value="home" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>

      <!-- 商品列表 -->
      <el-table :data="goodsList" style="width: 100%" v-loading="loading">
        <el-table-column prop="id" label="商品ID" width="80" />
        <el-table-column prop="name" label="商品名称" />
        <el-table-column prop="category" label="分类" width="120" />
        <el-table-column prop="price" label="价格" width="120">
          <template #default="scope">
            ¥{{ scope.row.price }}
          </template>
        </el-table-column>
        <el-table-column prop="stock" label="库存" width="100" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="scope">
            <el-tag :type="scope.row.status === 'active' ? 'success' : 'danger'">
              {{ scope.row.status === 'active' ? '上架' : '下架' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200">
          <template #default="scope">
            <el-button size="small" @click="handleEdit(scope.row)">编辑</el-button>
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

    <!-- 新增/编辑商品对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="600px"
      @close="handleDialogClose"
    >
      <el-form :model="goodsForm" :rules="rules" ref="goodsFormRef" label-width="100px">
        <el-form-item label="商品名称" prop="name">
          <el-input v-model="goodsForm.name" placeholder="请输入商品名称" />
        </el-form-item>
        <el-form-item label="商品分类" prop="category">
          <el-select v-model="goodsForm.category" placeholder="请选择分类" style="width: 100%">
            <el-option label="电子产品" value="electronics" />
            <el-option label="服装鞋帽" value="clothing" />
            <el-option label="食品饮料" value="food" />
            <el-option label="家居用品" value="home" />
          </el-select>
        </el-form-item>
        <el-form-item label="商品价格" prop="price">
          <el-input-number v-model="goodsForm.price" :precision="2" :step="0.1" :min="0" style="width: 100%" />
        </el-form-item>
        <el-form-item label="商品库存" prop="stock">
          <el-input-number v-model="goodsForm.stock" :min="0" style="width: 100%" />
        </el-form-item>
        <el-form-item label="商品状态" prop="status">
          <el-radio-group v-model="goodsForm.status">
            <el-radio label="active">上架</el-radio>
            <el-radio label="inactive">下架</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="商品描述" prop="description">
          <el-input v-model="goodsForm.description" type="textarea" :rows="3" placeholder="请输入商品描述" />
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
const loading = ref(false)
const dialogVisible = ref(false)
const dialogTitle = ref('')
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const goodsFormRef = ref()

// 搜索表单
const searchForm = reactive({
  name: '',
  category: ''
})

// 商品表单
const goodsForm = reactive({
  id: '',
  name: '',
  category: '',
  price: 0,
  stock: 0,
  status: 'active',
  description: ''
})

// 表单验证规则
const rules = {
  name: [{ required: true, message: '请输入商品名称', trigger: 'blur' }],
  category: [{ required: true, message: '请选择商品分类', trigger: 'change' }],
  price: [{ required: true, message: '请输入商品价格', trigger: 'blur' }],
  stock: [{ required: true, message: '请输入商品库存', trigger: 'blur' }],
  status: [{ required: true, message: '请选择商品状态', trigger: 'change' }]
}

// 模拟商品数据
const goodsList = ref([
  { id: 1, name: 'iPhone 15 Pro', category: 'electronics', price: 8999, stock: 100, status: 'active', description: '苹果最新旗舰手机' },
  { id: 2, name: 'MacBook Pro', category: 'electronics', price: 14999, stock: 50, status: 'active', description: '专业级笔记本电脑' },
  { id: 3, name: 'Nike运动鞋', category: 'clothing', price: 599, stock: 200, status: 'active', description: '舒适透气的运动鞋' },
  { id: 4, name: '星巴克咖啡', category: 'food', price: 35, stock: 500, status: 'active', description: '香浓醇厚的咖啡' }
])

// 生命周期
onMounted(() => {
  fetchGoodsList()
})

// 获取商品列表
const fetchGoodsList = () => {
  loading.value = true
  // 模拟API调用
  setTimeout(() => {
    total.value = goodsList.value.length
    loading.value = false
  }, 500)
}

// 搜索
const handleSearch = () => {
  currentPage.value = 1
  fetchGoodsList()
}

// 重置搜索
const handleReset = () => {
  searchForm.name = ''
  searchForm.category = ''
  handleSearch()
}

// 新增商品
const handleAdd = () => {
  dialogTitle.value = '新增商品'
  resetForm()
  dialogVisible.value = true
}

// 编辑商品
const handleEdit = (row) => {
  dialogTitle.value = '编辑商品'
  Object.assign(goodsForm, row)
  dialogVisible.value = true
}

// 删除商品
const handleDelete = (row) => {
  ElMessageBox.confirm('确定要删除这个商品吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    const index = goodsList.value.findIndex(item => item.id === row.id)
    if (index > -1) {
      goodsList.value.splice(index, 1)
      ElMessage.success('删除成功')
      fetchGoodsList()
    }
  })
}

// 提交表单
const handleSubmit = async () => {
  if (!goodsFormRef.value) return
  
  try {
    await goodsFormRef.value.validate()
    
    if (goodsForm.id) {
      // 编辑
      const index = goodsList.value.findIndex(item => item.id === goodsForm.id)
      if (index > -1) {
        Object.assign(goodsList.value[index], goodsForm)
      }
      ElMessage.success('编辑成功')
    } else {
      // 新增
      const newGoods = {
        ...goodsForm,
        id: Date.now()
      }
      goodsList.value.unshift(newGoods)
      ElMessage.success('新增成功')
    }
    
    dialogVisible.value = false
    fetchGoodsList()
  } catch (error) {
    console.error('表单验证失败:', error)
  }
}

// 重置表单
const resetForm = () => {
  Object.assign(goodsForm, {
    id: '',
    name: '',
    category: '',
    price: 0,
    stock: 0,
    status: 'active',
    description: ''
  })
}

// 对话框关闭
const handleDialogClose = () => {
  resetForm()
}

// 分页大小改变
const handleSizeChange = (val) => {
  pageSize.value = val
  fetchGoodsList()
}

// 当前页改变
const handleCurrentChange = (val) => {
  currentPage.value = val
  fetchGoodsList()
}
</script>

<style scoped>
.goods-management {
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

.pagination-container {
  margin-top: 20px;
  text-align: right;
}

.dialog-footer {
  text-align: right;
}
</style>
