<template>
  <div class="goods-management">
    <el-card class="box-card">
      <template #header>
        <div class="card-header">
          <span>商品管理</span>
          <div class="header-actions">
            <el-button @click="columnSettingVisible = true" icon="Setting">自定义列</el-button>
            <el-button type="primary" @click="handleAdd">新增商品</el-button>
          </div>
        </div>
      </template>
      
      <!-- 搜索栏 -->
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="商品名称">
          <el-input v-model="searchForm.name" placeholder="请输入商品名称" clearable />
        </el-form-item>
        <el-form-item label="商品分类">
          <el-select v-model="searchForm.category" placeholder="请选择分类" clearable>
            <el-option 
              v-for="category in categoryList" 
              :key="category.catId"
              :label="category.catName" 
              :value="category.catId" 
            />
          </el-select>
        </el-form-item>
        <el-form-item label="商品状态">
          <el-select v-model="searchForm.status" placeholder="请选择状态" clearable>
            <el-option label="上架" value="on" />
            <el-option label="下架" value="off" />
            <el-option label="缺货" value="out" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>

      <!-- 商品列表 -->
      <el-table 
        :data="goodsList" 
        style="width: 100%" 
        v-loading="loading"
        @sort-change="handleSortChange"
        :default-sort="{ prop: sortField, order: sortOrder }">
        
        <el-table-column 
          v-if="visibleColumns.includes('id')" 
          prop="id" 
          label="商品ID" 
          width="80" 
          sortable="custom" />
        
        <el-table-column 
          v-if="visibleColumns.includes('name')" 
          prop="name" 
          label="商品名称" 
          width="200" 
          sortable="custom" />
        
        <el-table-column 
          v-if="visibleColumns.includes('category')" 
          prop="category" 
          label="商品分类" 
          width="120" 
          sortable="custom">
          <template #default="scope">
            <el-tag>{{ getCategoryText(scope.row.category) }}</el-tag>
          </template>
        </el-table-column>
        
        <el-table-column 
          v-if="visibleColumns.includes('price')" 
          prop="price" 
          label="价格" 
          width="120" 
          sortable="custom">
          <template #default="scope">
            ¥{{ scope.row.price }}
          </template>
        </el-table-column>
        
        <el-table-column 
          v-if="visibleColumns.includes('stock')" 
          prop="stock" 
          label="库存" 
          width="100" 
          sortable="custom" />
        
        <el-table-column 
          v-if="visibleColumns.includes('status')" 
          prop="status" 
          label="状态" 
          width="100" 
          sortable="custom">
          <template #default="scope">
            <el-tag :type="getStatusType(scope.row.status)">
              {{ getStatusText(scope.row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        
        <el-table-column 
          v-if="visibleColumns.includes('createTime')" 
          prop="createTime" 
          label="创建时间" 
          width="180" 
          sortable="custom" />
        
        <el-table-column label="操作" width="250" fixed="right">
          <template #default="scope">
            <el-button size="small" @click="handleView(scope.row)">查看</el-button>
            <el-button size="small" type="primary" @click="handleEdit(scope.row)">编辑</el-button>
            <el-button size="small" type="warning" @click="handleToggleStatus(scope.row)">
              {{ scope.row.status === 'on' ? '下架' : '上架' }}
            </el-button>
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

    <!-- 自定义列设置弹窗 -->
    <el-dialog 
      v-model="columnSettingVisible" 
      title="自定义显示列" 
      width="500px">
      <div class="column-setting">
        <div class="tip">请选择要显示的列（拖拽可调整顺序）</div>
        <el-checkbox-group v-model="visibleColumns" class="column-list">
          <div 
            v-for="col in allColumns" 
            :key="col.prop" 
            class="column-item"
            draggable="true"
            @dragstart="handleDragStart($event, col.prop)"
            @dragover.prevent
            @drop="handleDrop($event, col.prop)">
            <el-checkbox :label="col.prop">
              <i class="el-icon-rank drag-handle"></i>
              {{ col.label }}
            </el-checkbox>
          </div>
        </el-checkbox-group>
      </div>
      <template #footer>
        <el-button @click="resetColumns">恢复默认</el-button>
        <el-button type="primary" @click="saveColumnSetting">确定</el-button>
      </template>
    </el-dialog>

    <!-- 商品编辑对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="800px"
    >
      <el-form :model="goodsForm" :rules="rules" ref="goodsFormRef" label-width="100px">
        <el-form-item label="商品名称" prop="name">
          <el-input v-model="goodsForm.name" placeholder="请输入商品名称" />
        </el-form-item>
        <el-form-item label="商品分类" prop="category">
          <el-select v-model="goodsForm.category" placeholder="请选择分类">
            <el-option 
              v-for="category in categoryList" 
              :key="category.catId"
              :label="category.catName" 
              :value="category.catId" 
            />
          </el-select>
        </el-form-item>
        <el-form-item label="商品价格" prop="price">
          <el-input-number v-model="goodsForm.price" :precision="2" :min="0" />
        </el-form-item>
        <el-form-item label="商品库存" prop="stock">
          <el-input-number v-model="goodsForm.stock" :min="0" />
        </el-form-item>
        <el-form-item label="商品描述" prop="description">
          <el-input type="textarea" v-model="goodsForm.description" :rows="4" placeholder="请输入商品描述" />
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
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { goodsApi, categoryApi } from '@/api/goods/goods'

// 路由
const router = useRouter()

// 响应式数据
const loading = ref(false)
const dialogVisible = ref(false)
const dialogTitle = ref('新增商品')
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const goodsFormRef = ref(null)

// 排序
const sortField = ref('id')
const sortOrder = ref('descending') // ascending | descending

// 自定义列设置
const columnSettingVisible = ref(false)
let draggedColumn = null

// 所有可用列定义
const allColumns = ref([
  { prop: 'id', label: '商品ID' },
  { prop: 'name', label: '商品名称' },
  { prop: 'category', label: '商品分类' },
  { prop: 'price', label: '价格' },
  { prop: 'stock', label: '库存' },
  { prop: 'status', label: '状态' },
  { prop: 'createTime', label: '创建时间' },
])

// 默认显示的列
const defaultColumns = ['id', 'name', 'category', 'price', 'stock', 'status']

// 当前显示的列（从localStorage读取或使用默认值）
const visibleColumns = ref([])

// 搜索表单
const searchForm = reactive({
  name: '',
  category: '',
  status: ''
})

// 商品表单
const goodsForm = reactive({
  id: '',
  name: '',
  category: '',
  price: 0,
  stock: 0,
  description: ''
})

// 表单验证规则
const rules = {
  name: [{ required: true, message: '请输入商品名称', trigger: 'blur' }],
  category: [{ required: true, message: '请选择商品分类', trigger: 'change' }],
  price: [{ required: true, message: '请输入商品价格', trigger: 'blur' }],
  stock: [{ required: true, message: '请输入商品库存', trigger: 'blur' }]
}

// 商品数据
const goodsList = ref([])
const categoryList = ref([])

// 生命周期
onMounted(async () => {
  initColumnSetting()
  await fetchCategoryList()
  await fetchGoodsList()
})

// 获取分类列表
const fetchCategoryList = async () => {
  try {
    const response = await categoryApi.getCategoryList({})
    if (response.code === 200) {
      categoryList.value = response.data || []
    }
  } catch (error) {
    console.error('获取分类列表失败:', error)
  }
}

// 获取商品列表
const fetchGoodsList = async () => {
  loading.value = true
  try {
    const params = {
      pageNum: currentPage.value,
      pageSize: pageSize.value,
      sortField: sortField.value,
      sortOrder: sortOrder.value === 'ascending' ? 'asc' : 'desc',
      ...searchForm
    }
    const response = await goodsApi.getGoodsList(params)
    if (response.code === 200) {
      goodsList.value = response.data.list || []
      total.value = response.data.total || 0
    } else {
      ElMessage.error(response.message || '获取商品列表失败')
    }
  } catch (error) {
    console.error('获取商品列表失败:', error)
    ElMessage.error('获取商品列表失败')
  } finally {
    loading.value = false
  }
}

// 获取分类名称
const getCategoryText = (catId) => {
  const category = categoryList.value.find(cat => cat.catId === catId)
  return category ? category.catName : '未知'
}

// 获取状态类型
const getStatusType = (status) => {
  const statusMap = {
    on: 'success',
    off: 'info',
    out: 'warning'
  }
  return statusMap[status] || 'info'
}

// 获取状态文本
const getStatusText = (status) => {
  const statusMap = {
    on: '上架',
    off: '下架',
    out: '缺货'
  }
  return statusMap[status] || '未知'
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
  searchForm.status = ''
  handleSearch()
}

// 新增商品
const handleAdd = () => {
  router.push('/create')
}

// 编辑商品
const handleEdit = (row) => {
  router.push(`/detail/${row.commonId || row.id}`)
}

// 查看商品
const handleView = (row) => {
  router.push(`/detail/${row.commonId || row.id}`)
}

// 切换状态
const handleToggleStatus = async (row) => {
  const action = row.status === 'on' ? '下架' : '上架'
  try {
    await ElMessageBox.confirm(`确定要${action}这个商品吗？`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    const newStatus = row.status === 'on' ? 'off' : 'on'
    const response = await goodsApi.updateGoodsStatus(row.id, newStatus)
    
    if (response.code === 200) {
      row.status = newStatus
      ElMessage.success(`${action}成功`)
    } else {
      ElMessage.error(response.message || `${action}失败`)
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error(`${action}失败:`, error)
      ElMessage.error(`${action}失败`)
    }
  }
}

// 删除商品
const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm('确定要删除这个商品吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    const response = await goodsApi.deleteGoods(row.id)
    
    if (response.code === 200) {
      ElMessage.success('删除成功')
      // 重新获取列表
      await fetchGoodsList()
    } else {
      ElMessage.error(response.message || '删除失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除失败:', error)
      ElMessage.error('删除失败')
    }
  }
}

// 提交表单
const handleSubmit = async () => {
  if (!goodsFormRef.value) return
  
  try {
    await goodsFormRef.value.validate()
    
    let response
    if (goodsForm.id) {
      // 编辑模式
      response = await goodsApi.updateGoods(goodsForm)
      if (response.code === 200) {
        ElMessage.success('编辑成功')
      } else {
        ElMessage.error(response.message || '编辑失败')
        return
      }
    } else {
      // 新增模式
      response = await goodsApi.createGoods(goodsForm)
      if (response.code === 200) {
        ElMessage.success('新增成功')
      } else {
        ElMessage.error(response.message || '新增失败')
        return
      }
    }
    
    dialogVisible.value = false
    await fetchGoodsList()
  } catch (error) {
    if (error.message) {
      // API调用错误
      console.error('提交失败:', error)
      ElMessage.error('提交失败')
    } else {
      // 表单验证失败
      console.error('表单验证失败:', error)
    }
  }
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

// 处理排序变化
const handleSortChange = ({ prop, order }) => {
  sortField.value = prop
  sortOrder.value = order
  fetchGoodsList()
}

// 拖拽开始
const handleDragStart = (event, columnProp) => {
  draggedColumn = columnProp
  event.dataTransfer.effectAllowed = 'move'
}

// 拖拽放下
const handleDrop = (event, targetColumnProp) => {
  if (!draggedColumn || draggedColumn === targetColumnProp) return
  
  const draggedIndex = allColumns.value.findIndex(col => col.prop === draggedColumn)
  const targetIndex = allColumns.value.findIndex(col => col.prop === targetColumnProp)
  
  if (draggedIndex !== -1 && targetIndex !== -1) {
    const draggedItem = allColumns.value[draggedIndex]
    allColumns.value.splice(draggedIndex, 1)
    allColumns.value.splice(targetIndex, 0, draggedItem)
  }
  
  draggedColumn = null
}

// 保存列设置
const saveColumnSetting = () => {
  if (visibleColumns.value.length === 0) {
    ElMessage.warning('至少选择一列')
    return
  }
  
  // 保存到localStorage
  localStorage.setItem('goods_list_columns', JSON.stringify(visibleColumns.value))
  localStorage.setItem('goods_list_columns_order', JSON.stringify(allColumns.value))
  
  columnSettingVisible.value = false
  ElMessage.success('列设置已保存')
}

// 恢复默认列设置
const resetColumns = () => {
  visibleColumns.value = [...defaultColumns]
  allColumns.value = [
    { prop: 'id', label: '商品ID' },
    { prop: 'name', label: '商品名称' },
    { prop: 'category', label: '商品分类' },
    { prop: 'price', label: '价格' },
    { prop: 'stock', label: '库存' },
    { prop: 'status', label: '状态' },
    { prop: 'createTime', label: '创建时间' },
  ]
  localStorage.removeItem('goods_list_columns')
  localStorage.removeItem('goods_list_columns_order')
  ElMessage.success('已恢复默认设置')
}

// 初始化列设置
const initColumnSetting = () => {
  // 从localStorage读取列设置
  const savedColumns = localStorage.getItem('goods_list_columns')
  const savedOrder = localStorage.getItem('goods_list_columns_order')
  
  if (savedColumns) {
    visibleColumns.value = JSON.parse(savedColumns)
  } else {
    visibleColumns.value = [...defaultColumns]
  }
  
  if (savedOrder) {
    allColumns.value = JSON.parse(savedOrder)
  }
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

.header-actions {
  display: flex;
  gap: 10px;
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

/* 自定义列设置样式 */
.column-setting {
  .tip {
    color: #909399;
    font-size: 13px;
    margin-bottom: 15px;
  }
  
  .column-list {
    display: flex;
    flex-direction: column;
    gap: 8px;
    
    .column-item {
      padding: 8px 12px;
      border: 1px solid #dcdfe6;
      border-radius: 4px;
      cursor: move;
      transition: all 0.3s;
      
      &:hover {
        background-color: #f5f7fa;
        border-color: #409eff;
      }
      
      :deep(.el-checkbox) {
        width: 100%;
        
        .el-checkbox__label {
          display: flex;
          align-items: center;
          gap: 8px;
        }
      }
      
      .drag-handle {
        cursor: move;
        color: #909399;
        
        &::before {
          content: '☰';
        }
      }
    }
  }
}
</style>
