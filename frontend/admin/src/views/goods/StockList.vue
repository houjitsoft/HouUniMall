<template>
  <div class="stock-management">
    <el-card class="box-card">
      <template #header>
        <div class="card-header">
          <span>库存管理</span>
          <el-button type="primary" @click="handleInStock">入库</el-button>
          <el-button type="warning" @click="handleOutStock">出库</el-button>
        </div>
      </template>
      
      <!-- 搜索栏 -->
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="商品名称">
          <el-input v-model="searchForm.name" placeholder="请输入商品名称" clearable />
        </el-form-item>
        <el-form-item label="库存状态">
          <el-select v-model="searchForm.stockStatus" placeholder="请选择状态" clearable>
            <el-option label="充足" value="sufficient" />
            <el-option label="预警" value="warning" />
            <el-option label="缺货" value="out" />
          </el-select>
        </el-form-item>
        <el-form-item label="仓库">
          <el-select v-model="searchForm.warehouse" placeholder="请选择仓库" clearable>
            <el-option label="主仓库" value="main" />
            <el-option label="分仓库A" value="branch_a" />
            <el-option label="分仓库B" value="branch_b" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>

      <!-- 库存列表 -->
      <el-table :data="stockList" style="width: 100%" v-loading="loading">
        <el-table-column prop="productName" label="商品名称" width="200" />
        <el-table-column prop="sku" label="SKU" width="150" />
        <el-table-column prop="warehouse" label="仓库" width="120">
          <template #default="scope">
            <el-tag>{{ getWarehouseText(scope.row.warehouse) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="currentStock" label="当前库存" width="100" />
        <el-table-column prop="minStock" label="最低库存" width="100" />
        <el-table-column prop="stockStatus" label="库存状态" width="100">
          <template #default="scope">
            <el-tag :type="getStockStatusType(scope.row.stockStatus)">
              {{ getStockStatusText(scope.row.stockStatus) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="lastUpdateTime" label="最后更新时间" width="180" />
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="scope">
            <el-button size="small" @click="handleViewHistory(scope.row)">查看记录</el-button>
            <el-button size="small" type="primary" @click="handleAdjustStock(scope.row)">调整库存</el-button>
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

    <!-- 入库对话框 -->
    <el-dialog
      v-model="inStockDialogVisible"
      title="商品入库"
      width="600px"
    >
      <el-form :model="inStockForm" :rules="inStockRules" ref="inStockFormRef" label-width="100px">
        <el-form-item label="商品" prop="productId">
          <el-select v-model="inStockForm.productId" placeholder="请选择商品" filterable>
            <el-option
              v-for="product in productOptions"
              :key="product.id"
              :label="product.name"
              :value="product.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="仓库" prop="warehouse">
          <el-select v-model="inStockForm.warehouse" placeholder="请选择仓库">
            <el-option label="主仓库" value="main" />
            <el-option label="分仓库A" value="branch_a" />
            <el-option label="分仓库B" value="branch_b" />
          </el-select>
        </el-form-item>
        <el-form-item label="入库数量" prop="quantity">
          <el-input-number v-model="inStockForm.quantity" :min="1" />
        </el-form-item>
        <el-form-item label="入库类型" prop="type">
          <el-select v-model="inStockForm.type" placeholder="请选择入库类型">
            <el-option label="采购入库" value="purchase" />
            <el-option label="退货入库" value="return" />
            <el-option label="调拨入库" value="transfer" />
            <el-option label="盘盈入库" value="inventory" />
          </el-select>
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input type="textarea" v-model="inStockForm.remark" :rows="3" placeholder="请输入备注" />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="inStockDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitInStock">确定</el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 出库对话框 -->
    <el-dialog
      v-model="outStockDialogVisible"
      title="商品出库"
      width="600px"
    >
      <el-form :model="outStockForm" :rules="outStockRules" ref="outStockFormRef" label-width="100px">
        <el-form-item label="商品" prop="productId">
          <el-select v-model="outStockForm.productId" placeholder="请选择商品" filterable>
            <el-option
              v-for="product in productOptions"
              :key="product.id"
              :label="product.name"
              :value="product.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="仓库" prop="warehouse">
          <el-select v-model="outStockForm.warehouse" placeholder="请选择仓库">
            <el-option label="主仓库" value="main" />
            <el-option label="分仓库A" value="branch_a" />
            <el-option label="分仓库B" value="branch_b" />
          </el-select>
        </el-form-item>
        <el-form-item label="出库数量" prop="quantity">
          <el-input-number v-model="outStockForm.quantity" :min="1" />
        </el-form-item>
        <el-form-item label="出库类型" prop="type">
          <el-select v-model="outStockForm.type" placeholder="请选择出库类型">
            <el-option label="销售出库" value="sale" />
            <el-option label="调拨出库" value="transfer" />
            <el-option label="盘亏出库" value="inventory" />
            <el-option label="其他出库" value="other" />
          </el-select>
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input type="textarea" v-model="outStockForm.remark" :rows="3" placeholder="请输入备注" />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="outStockDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitOutStock">确定</el-button>
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
const inStockDialogVisible = ref(false)
const outStockDialogVisible = ref(false)
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const inStockFormRef = ref(null)
const outStockFormRef = ref(null)

// 搜索表单
const searchForm = reactive({
  name: '',
  stockStatus: '',
  warehouse: ''
})

// 入库表单
const inStockForm = reactive({
  productId: '',
  warehouse: '',
  quantity: 1,
  type: '',
  remark: ''
})

// 出库表单
const outStockForm = reactive({
  productId: '',
  warehouse: '',
  quantity: 1,
  type: '',
  remark: ''
})

// 表单验证规则
const inStockRules = {
  productId: [{ required: true, message: '请选择商品', trigger: 'change' }],
  warehouse: [{ required: true, message: '请选择仓库', trigger: 'change' }],
  quantity: [{ required: true, message: '请输入入库数量', trigger: 'blur' }],
  type: [{ required: true, message: '请选择入库类型', trigger: 'change' }]
}

const outStockRules = {
  productId: [{ required: true, message: '请选择商品', trigger: 'change' }],
  warehouse: [{ required: true, message: '请选择仓库', trigger: 'change' }],
  quantity: [{ required: true, message: '请输入出库数量', trigger: 'blur' }],
  type: [{ required: true, message: '请选择出库类型', trigger: 'change' }]
}

// 库存数据
const stockList = ref([
  {
    productName: 'iPhone 15 Pro',
    sku: 'IP15P-128G',
    warehouse: 'main',
    currentStock: 50,
    minStock: 10,
    stockStatus: 'sufficient',
    lastUpdateTime: '2025-01-27 10:00:00'
  },
  {
    productName: 'Nike Air Max',
    sku: 'NK-AM-42',
    warehouse: 'main',
    currentStock: 8,
    minStock: 10,
    stockStatus: 'warning',
    lastUpdateTime: '2025-01-27 11:00:00'
  },
  {
    productName: '星巴克咖啡豆',
    sku: 'SB-CB-500G',
    warehouse: 'branch_a',
    currentStock: 0,
    minStock: 20,
    stockStatus: 'out',
    lastUpdateTime: '2025-01-27 12:00:00'
  }
])

// 商品选项
const productOptions = ref([
  { id: 1, name: 'iPhone 15 Pro' },
  { id: 2, name: 'Nike Air Max' },
  { id: 3, name: '星巴克咖啡豆' }
])

// 生命周期
onMounted(() => {
  fetchStockList()
})

// 获取库存列表
const fetchStockList = () => {
  loading.value = true
  setTimeout(() => {
    total.value = stockList.value.length
    loading.value = false
  }, 500)
}

// 获取仓库文本
const getWarehouseText = (warehouse) => {
  const warehouseMap = {
    main: '主仓库',
    branch_a: '分仓库A',
    branch_b: '分仓库B'
  }
  return warehouseMap[warehouse] || '未知'
}

// 获取库存状态类型
const getStockStatusType = (status) => {
  const statusMap = {
    sufficient: 'success',
    warning: 'warning',
    out: 'danger'
  }
  return statusMap[status] || 'info'
}

// 获取库存状态文本
const getStockStatusText = (status) => {
  const statusMap = {
    sufficient: '充足',
    warning: '预警',
    out: '缺货'
  }
  return statusMap[status] || '未知'
}

// 搜索
const handleSearch = () => {
  currentPage.value = 1
  fetchStockList()
}

// 重置搜索
const handleReset = () => {
  searchForm.name = ''
  searchForm.stockStatus = ''
  searchForm.warehouse = ''
  handleSearch()
}

// 入库
const handleInStock = () => {
  Object.assign(inStockForm, {
    productId: '',
    warehouse: '',
    quantity: 1,
    type: '',
    remark: ''
  })
  inStockDialogVisible.value = true
}

// 出库
const handleOutStock = () => {
  Object.assign(outStockForm, {
    productId: '',
    warehouse: '',
    quantity: 1,
    type: '',
    remark: ''
  })
  outStockDialogVisible.value = true
}

// 查看历史记录
const handleViewHistory = (row) => {
  ElMessage.info('查看历史记录功能开发中...')
}

// 调整库存
const handleAdjustStock = (row) => {
  ElMessage.info('调整库存功能开发中...')
}

// 提交入库
const submitInStock = async () => {
  if (!inStockFormRef.value) return
  
  try {
    await inStockFormRef.value.validate()
    ElMessage.success('入库成功')
    inStockDialogVisible.value = false
    fetchStockList()
  } catch (error) {
    console.error('表单验证失败:', error)
  }
}

// 提交出库
const submitOutStock = async () => {
  if (!outStockFormRef.value) return
  
  try {
    await outStockFormRef.value.validate()
    ElMessage.success('出库成功')
    outStockDialogVisible.value = false
    fetchStockList()
  } catch (error) {
    console.error('表单验证失败:', error)
  }
}

// 分页大小改变
const handleSizeChange = (val) => {
  pageSize.value = val
  fetchStockList()
}

// 当前页改变
const handleCurrentChange = (val) => {
  currentPage.value = val
  fetchStockList()
}
</script>

<style scoped>
.stock-management {
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
