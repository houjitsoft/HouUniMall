<template>
  <div class="stock-management">
    <!-- 页面标题和统计卡片 -->
    <el-row :gutter="20" style="margin-bottom: 20px;">
      <el-col :span="6">
        <el-card class="stat-card total-stock">
          <div class="stat-content">
            <div class="stat-icon"><el-icon><Box /></el-icon></div>
            <div class="stat-info">
              <div class="stat-value">{{ stockStats.total }}</div>
              <div class="stat-label">总库存</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card low-stock">
          <div class="stat-content">
            <div class="stat-icon"><el-icon><Warning /></el-icon></div>
            <div class="stat-info">
              <div class="stat-value">{{ stockStats.lowStock }}</div>
              <div class="stat-label">库存预警</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card out-stock">
          <div class="stat-content">
            <div class="stat-icon"><el-icon><CircleClose /></el-icon></div>
            <div class="stat-info">
              <div class="stat-value">{{ stockStats.outStock }}</div>
              <div class="stat-label">缺货商品</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card stock-value">
          <div class="stat-content">
            <div class="stat-icon"><el-icon><Money /></el-icon></div>
            <div class="stat-info">
              <div class="stat-value">¥{{ stockStats.totalValue }}</div>
              <div class="stat-label">库存价值</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 搜索筛选 -->
    <el-card class="search-card">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="商品名称">
          <el-input
            v-model="searchForm.goodsName"
            placeholder="请输入商品名称"
            clearable
          />
        </el-form-item>
        <el-form-item label="商品编码">
          <el-input
            v-model="searchForm.goodsCode"
            placeholder="请输入商品编码"
            clearable
          />
        </el-form-item>
        <el-form-item label="库存状态">
          <el-select v-model="searchForm.stockStatus" placeholder="请选择库存状态" clearable>
            <el-option label="正常" value="normal" />
            <el-option label="预警" value="warning" />
            <el-option label="缺货" value="out" />
          </el-select>
        </el-form-item>
        <el-form-item label="商品分类">
          <el-select v-model="searchForm.categoryId" placeholder="请选择分类" clearable>
            <el-option label="全部分类" value="" />
            <el-option label="服装" value="1" />
            <el-option label="数码" value="2" />
            <el-option label="家居" value="3" />
          </el-select>
        </el-form-item>
        <el-form-item label="仓库">
          <el-select v-model="searchForm.warehouseId" placeholder="请选择仓库" clearable>
            <el-option label="全部仓库" value="" />
            <el-option label="主仓库" value="1" />
            <el-option label="分仓库A" value="2" />
            <el-option label="分仓库B" value="3" />
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
          <el-button type="primary" @click="handleStockIn">
            <el-icon><Plus /></el-icon>入库
          </el-button>
          <el-button type="warning" @click="handleStockOut">
            <el-icon><Minus /></el-icon>出库
          </el-button>
          <el-button type="success" @click="handleStockTransfer">
            <el-icon><Sort /></el-icon>调拨
          </el-button>
          <el-button type="info" @click="handleStockCheck">
            <el-icon><List /></el-icon>盘点
          </el-button>
          <el-button type="danger" @click="handleBatchUpdate">
            <el-icon><Edit /></el-icon>批量调整
          </el-button>
          <el-button @click="handleExport">
            <el-icon><Download /></el-icon>导出
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
        :data="stockList"
        v-loading="loading"
        style="width: 100%"
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="55" />
        <el-table-column prop="goodsImage" label="商品图片" width="80">
          <template #default="{ row }">
            <el-image
              :src="row.goodsImage || '/default-goods.png'"
              style="width: 50px; height: 50px"
              fit="cover"
            />
          </template>
        </el-table-column>
        <el-table-column prop="goodsName" label="商品名称" width="200" />
        <el-table-column prop="goodsCode" label="商品编码" width="120" />
        <el-table-column prop="categoryName" label="商品分类" width="100" />
        <el-table-column prop="warehouseName" label="仓库" width="100" />
        <el-table-column prop="currentStock" label="当前库存" width="100">
          <template #default="{ row }">
            <span :class="getStockClass(row)">
              {{ row.currentStock }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="availableStock" label="可售库存" width="100">
          <template #default="{ row }">
            <span :class="getStockClass(row)">
              {{ row.availableStock }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="lockedStock" label="锁定库存" width="100" />
        <el-table-column prop="minStock" label="最低库存" width="100" />
        <el-table-column prop="maxStock" label="最高库存" width="100" />
        <el-table-column prop="stockStatus" label="库存状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.stockStatus)">
              {{ getStatusText(row.stockStatus) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="lastUpdateTime" label="最后更新" width="160" />
        <el-table-column label="操作" width="280" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="handleView(row)">
              <el-icon><View /></el-icon>查看
            </el-button>
            <el-button link type="success" @click="handleStockInSingle(row)">
              <el-icon><Plus /></el-icon>入库
            </el-button>
            <el-button link type="warning" @click="handleStockOutSingle(row)">
              <el-icon><Minus /></el-icon>出库
            </el-button>
            <el-button link type="info" @click="handleAdjust(row)">
              <el-icon><Edit /></el-icon>调整
            </el-button>
            <el-button link type="primary" @click="handleHistory(row)">
              <el-icon><Clock /></el-icon>记录
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

    <!-- 库存操作对话框 -->
    <el-dialog
      v-model="operationDialogVisible"
      :title="operationTitle"
      width="600px"
      @close="resetOperationForm"
    >
      <el-form
        ref="operationFormRef"
        :model="operationForm"
        :rules="operationRules"
        label-width="100px"
      >
        <el-form-item label="商品信息">
          <div class="goods-info">
            <el-image
              :src="operationForm.goodsImage || '/default-goods.png'"
              style="width: 60px; height: 60px; margin-right: 10px;"
              fit="cover"
            />
            <div>
              <div>{{ operationForm.goodsName }}</div>
              <div class="text-gray">{{ operationForm.goodsCode }}</div>
            </div>
          </div>
        </el-form-item>
        <el-form-item label="当前库存">
          <span class="stock-info">{{ operationForm.currentStock }}</span>
        </el-form-item>
        <el-form-item label="操作类型" prop="operationType">
          <el-radio-group v-model="operationForm.operationType">
            <el-radio label="in">入库</el-radio>
            <el-radio label="out">出库</el-radio>
            <el-radio label="adjust">调整</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="操作数量" prop="quantity">
          <el-input-number
            v-model="operationForm.quantity"
            :min="1"
            :max="operationForm.operationType === 'out' ? operationForm.currentStock : 999999"
            style="width: 200px;"
          />
        </el-form-item>
        <el-form-item label="操作原因" prop="reason">
          <el-select v-model="operationForm.reason" placeholder="请选择操作原因">
            <el-option
              v-for="reason in getReasonOptions(operationForm.operationType)"
              :key="reason.value"
              :label="reason.label"
              :value="reason.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="备注">
          <el-input
            v-model="operationForm.remark"
            type="textarea"
            :rows="3"
            placeholder="请输入备注信息"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="operationDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleOperationSubmit">确定</el-button>
      </template>
    </el-dialog>

    <!-- 库存记录对话框 -->
    <el-dialog
      v-model="historyDialogVisible"
      title="库存变动记录"
      width="800px"
    >
      <el-table :data="stockHistory" style="width: 100%">
        <el-table-column prop="operationType" label="操作类型" width="100">
          <template #default="{ row }">
            <el-tag :type="getOperationTypeColor(row.operationType)">
              {{ getOperationTypeText(row.operationType) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="quantity" label="数量" width="100">
          <template #default="{ row }">
            <span :class="getQuantityClass(row.operationType)">
              {{ getQuantityPrefix(row.operationType) }}{{ row.quantity }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="beforeStock" label="操作前库存" width="120" />
        <el-table-column prop="afterStock" label="操作后库存" width="120" />
        <el-table-column prop="reason" label="操作原因" width="120" />
        <el-table-column prop="operator" label="操作人" width="100" />
        <el-table-column prop="operateTime" label="操作时间" width="160" />
        <el-table-column prop="remark" label="备注" />
      </el-table>
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
  Box, Warning, CircleClose, Money, Search, Refresh, Plus, Minus, Sort, List, Edit,
  Download, View, Clock
} from '@element-plus/icons-vue'

// 响应式数据
const loading = ref(false)
const operationDialogVisible = ref(false)
const historyDialogVisible = ref(false)
const operationTitle = ref('库存操作')
const stockList = ref([])
const selectedStock = ref([])
const stockHistory = ref([])

// 统计数据
const stockStats = reactive({
  total: 12548,
  lowStock: 23,
  outStock: 8,
  totalValue: '2,456,789'
})

// 搜索表单
const searchForm = reactive({
  goodsName: '',
  goodsCode: '',
  stockStatus: '',
  categoryId: '',
  warehouseId: ''
})

// 分页信息
const pagination = reactive({
  page: 1,
  size: 10,
  total: 0
})

// 操作表单
const operationForm = reactive({
  id: '',
  goodsName: '',
  goodsCode: '',
  goodsImage: '',
  currentStock: 0,
  operationType: 'in',
  quantity: 1,
  reason: '',
  remark: ''
})

// 操作表单验证规则
const operationRules = {
  operationType: [{ required: true, message: '请选择操作类型', trigger: 'change' }],
  quantity: [{ required: true, message: '请输入操作数量', trigger: 'blur' }],
  reason: [{ required: true, message: '请选择操作原因', trigger: 'change' }]
}

// 组件引用
const operationFormRef = ref()

// 初始化
onMounted(() => {
  fetchStockList()
})

// 获取库存列表
const fetchStockList = async () => {
  loading.value = true
  try {
    // 模拟API调用
    setTimeout(() => {
      stockList.value = mockStockData()
      pagination.total = stockList.value.length
      loading.value = false
    }, 500)
  } catch (error) {
    ElMessage.error('获取库存列表失败')
    loading.value = false
  }
}

// 搜索处理
const handleSearch = () => {
  pagination.page = 1
  fetchStockList()
}

// 重置搜索
const handleReset = () => {
  Object.assign(searchForm, {
    goodsName: '',
    goodsCode: '',
    stockStatus: '',
    categoryId: '',
    warehouseId: ''
  })
  fetchStockList()
}

// 库存操作
const handleStockIn = () => {
  ElMessage.info('批量入库功能')
}

const handleStockOut = () => {
  ElMessage.info('批量出库功能')
}

const handleStockTransfer = () => {
  ElMessage.info('库存调拨功能')
}

const handleStockCheck = () => {
  ElMessage.info('库存盘点功能')
}

const handleBatchUpdate = () => {
  ElMessage.info('批量调整功能')
}

// 单个商品库存操作
const handleStockInSingle = (row) => {
  openOperationDialog(row, 'in', '入库操作')
}

const handleStockOutSingle = (row) => {
  openOperationDialog(row, 'out', '出库操作')
}

const handleAdjust = (row) => {
  openOperationDialog(row, 'adjust', '库存调整')
}

// 打开操作对话框
const openOperationDialog = (row, type, title) => {
  operationTitle.value = title
  Object.assign(operationForm, {
    id: row.id,
    goodsName: row.goodsName,
    goodsCode: row.goodsCode,
    goodsImage: row.goodsImage,
    currentStock: row.currentStock,
    operationType: type,
    quantity: 1,
    reason: '',
    remark: ''
  })
  operationDialogVisible.value = true
}

// 查看库存详情
const handleView = (row) => {
  ElMessage.info('查看库存详情功能')
}

// 查看库存变动记录
const handleHistory = (row) => {
  stockHistory.value = mockHistoryData(row.id)
  historyDialogVisible.value = true
}

// 操作提交
const handleOperationSubmit = async () => {
  try {
    await operationFormRef.value.validate()
    
    // 实现库存操作逻辑
    ElMessage.success('操作成功')
    operationDialogVisible.value = false
    fetchStockList()
  } catch (error) {
    ElMessage.error('表单验证失败')
  }
}

// 重置操作表单
const resetOperationForm = () => {
  Object.assign(operationForm, {
    id: '',
    goodsName: '',
    goodsCode: '',
    goodsImage: '',
    currentStock: 0,
    operationType: 'in',
    quantity: 1,
    reason: '',
    remark: ''
  })
}

// 选择变更
const handleSelectionChange = (selection) => {
  selectedStock.value = selection
}

// 分页处理
const handleSizeChange = (size) => {
  pagination.size = size
  fetchStockList()
}

const handleCurrentChange = (page) => {
  pagination.page = page
  fetchStockList()
}

// 其他操作
const handleExport = () => {
  ElMessage.info('导出库存数据功能')
}

const handleRefresh = () => {
  fetchStockList()
}

// 工具函数
const getStockClass = (row) => {
  if (row.currentStock <= 0) return 'stock-out'
  if (row.currentStock <= row.minStock) return 'stock-warning'
  return 'stock-normal'
}

const getStatusText = (status) => {
  const map = {
    'normal': '正常',
    'warning': '预警',
    'out': '缺货'
  }
  return map[status] || status
}

const getStatusType = (status) => {
  const map = {
    'normal': 'success',
    'warning': 'warning',
    'out': 'danger'
  }
  return map[status] || 'info'
}

const getOperationTypeText = (type) => {
  const map = {
    'in': '入库',
    'out': '出库',
    'adjust': '调整',
    'transfer': '调拨',
    'check': '盘点'
  }
  return map[type] || type
}

const getOperationTypeColor = (type) => {
  const map = {
    'in': 'success',
    'out': 'warning',
    'adjust': 'primary',
    'transfer': 'info',
    'check': 'danger'
  }
  return map[type] || 'info'
}

const getQuantityPrefix = (type) => {
  return type === 'out' ? '-' : '+'
}

const getQuantityClass = (type) => {
  return type === 'out' ? 'quantity-minus' : 'quantity-plus'
}

const getReasonOptions = (type) => {
  const options = {
    'in': [
      { label: '采购入库', value: 'purchase' },
      { label: '退货入库', value: 'return' },
      { label: '调拨入库', value: 'transfer_in' },
      { label: '其他入库', value: 'other_in' }
    ],
    'out': [
      { label: '销售出库', value: 'sale' },
      { label: '退货出库', value: 'refund' },
      { label: '调拨出库', value: 'transfer_out' },
      { label: '损耗出库', value: 'loss' },
      { label: '其他出库', value: 'other_out' }
    ],
    'adjust': [
      { label: '盘点调整', value: 'check_adjust' },
      { label: '损坏调整', value: 'damage' },
      { label: '系统调整', value: 'system' },
      { label: '其他调整', value: 'other_adjust' }
    ]
  }
  return options[type] || []
}

// 模拟库存数据
const mockStockData = () => {
  const statuses = ['normal', 'warning', 'out']
  const categories = ['服装', '数码', '家居']
  const warehouses = ['主仓库', '分仓库A', '分仓库B']
  const data = []
  
  for (let i = 1; i <= 20; i++) {
    const currentStock = Math.floor(Math.random() * 1000)
    const minStock = 10
    const maxStock = 500
    let status = 'normal'
    
    if (currentStock <= 0) status = 'out'
    else if (currentStock <= minStock) status = 'warning'
    
    data.push({
      id: i,
      goodsName: `商品${i}`,
      goodsCode: `GOODS${String(i).padStart(4, '0')}`,
      goodsImage: `/goods-${i}.jpg`,
      categoryName: categories[Math.floor(Math.random() * categories.length)],
      warehouseName: warehouses[Math.floor(Math.random() * warehouses.length)],
      currentStock: currentStock,
      availableStock: Math.max(0, currentStock - Math.floor(Math.random() * 10)),
      lockedStock: Math.floor(Math.random() * 10),
      minStock: minStock,
      maxStock: maxStock,
      stockStatus: status,
      lastUpdateTime: new Date(Date.now() - Math.random() * 7 * 24 * 60 * 60 * 1000).toLocaleString()
    })
  }
  
  return data
}

// 模拟库存记录数据
const mockHistoryData = (goodsId) => {
  const types = ['in', 'out', 'adjust', 'transfer', 'check']
  const reasons = ['采购入库', '销售出库', '盘点调整', '调拨转入', '系统调整']
  const operators = ['管理员1', '管理员2', '系统']
  const data = []
  
  for (let i = 1; i <= 10; i++) {
    const type = types[Math.floor(Math.random() * types.length)]
    const quantity = Math.floor(Math.random() * 100) + 1
    const beforeStock = Math.floor(Math.random() * 500) + 100
    const afterStock = type === 'out' ? beforeStock - quantity : beforeStock + quantity
    
    data.push({
      id: i,
      operationType: type,
      quantity: quantity,
      beforeStock: beforeStock,
      afterStock: afterStock,
      reason: reasons[Math.floor(Math.random() * reasons.length)],
      operator: operators[Math.floor(Math.random() * operators.length)],
      operateTime: new Date(Date.now() - Math.random() * 30 * 24 * 60 * 60 * 1000).toLocaleString(),
      remark: `操作备注${i}`
    })
  }
  
  return data
}
</script>

<style scoped>
.stock-management {
  padding: 20px;
}

.stat-card {
  border-left: 4px solid #409eff;
}

.stat-card.total-stock {
  border-left-color: #409eff;
}

.stat-card.low-stock {
  border-left-color: #e6a23c;
}

.stat-card.out-stock {
  border-left-color: #f56c6c;
}

.stat-card.stock-value {
  border-left-color: #67c23a;
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

.stock-normal {
  color: #67c23a;
  font-weight: bold;
}

.stock-warning {
  color: #e6a23c;
  font-weight: bold;
}

.stock-out {
  color: #f56c6c;
  font-weight: bold;
}

.goods-info {
  display: flex;
  align-items: center;
}

.text-gray {
  color: #909399;
  font-size: 12px;
}

.stock-info {
  font-size: 16px;
  font-weight: bold;
  color: #409eff;
}

.quantity-plus {
  color: #67c23a;
  font-weight: bold;
}

.quantity-minus {
  color: #f56c6c;
  font-weight: bold;
}
</style>
