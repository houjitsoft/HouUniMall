<template>
  <div class="coupon-management">
    <!-- 统计卡片 -->
    <el-row :gutter="20" style="margin-bottom: 20px;">
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon"><el-icon><Ticket /></el-icon></div>
            <div class="stat-info">
              <div class="stat-value">{{ couponStats.total }}</div>
              <div class="stat-label">优惠券总数</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon"><el-icon><CircleCheck /></el-icon></div>
            <div class="stat-info">
              <div class="stat-value">{{ couponStats.active }}</div>
              <div class="stat-label">活跃优惠券</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon"><el-icon><Check /></el-icon></div>
            <div class="stat-info">
              <div class="stat-value">{{ couponStats.used }}</div>
              <div class="stat-label">已使用</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon"><el-icon><Clock /></el-icon></div>
            <div class="stat-info">
              <div class="stat-value">{{ couponStats.expired }}</div>
              <div class="stat-label">已过期</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 搜索筛选 -->
    <el-card class="search-card">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="优惠券名称">
          <el-input v-model="searchForm.name" placeholder="请输入优惠券名称" clearable />
        </el-form-item>
        <el-form-item label="类型">
          <el-select v-model="searchForm.type" placeholder="请选择类型" clearable>
            <el-option label="满减券" value="full_reduction" />
            <el-option label="折扣券" value="discount" />
            <el-option label="立减券" value="instant" />
            <el-option label="包邮券" value="free_shipping" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="请选择状态" clearable>
            <el-option label="待生效" value="pending" />
            <el-option label="进行中" value="active" />
            <el-option label="已结束" value="ended" />
            <el-option label="已停用" value="disabled" />
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
            <el-icon><Plus /></el-icon>新建优惠券
          </el-button>
          <el-button type="success" @click="handleBatchEnable">
            <el-icon><Check /></el-icon>批量启用
          </el-button>
          <el-button type="warning" @click="handleBatchDisable">
            <el-icon><Close /></el-icon>批量停用
          </el-button>
          <el-button @click="handleExport">
            <el-icon><Download /></el-icon>导出数据
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
      <el-table :data="couponList" v-loading="loading" style="width: 100%" @selection-change="handleSelectionChange">
        <el-table-column type="selection" width="55" />
        <el-table-column prop="name" label="优惠券名称" width="180" />
        <el-table-column prop="type" label="类型" width="100">
          <template #default="{ row }">
            <el-tag :type="getTypeColor(row.type)">{{ getTypeText(row.type) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="value" label="优惠价值" width="120">
          <template #default="{ row }">
            <span class="coupon-value">{{ formatCouponValue(row) }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="condition" label="使用条件" width="120">
          <template #default="{ row }">
            <span v-if="row.minAmount > 0">满¥{{ row.minAmount }}可用</span>
            <span v-else>无门槛</span>
          </template>
        </el-table-column>
        <el-table-column prop="totalCount" label="发放数量" width="100" />
        <el-table-column prop="usedCount" label="已使用" width="100" />
        <el-table-column prop="remainCount" label="剩余数量" width="100">
          <template #default="{ row }">
            <span :class="getRemainClass(row.remainCount)">{{ row.remainCount }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">{{ getStatusText(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="250" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="handleView(row)">
              <el-icon><View /></el-icon>查看
            </el-button>
            <el-button link type="success" @click="handleEdit(row)">
              <el-icon><Edit /></el-icon>编辑
            </el-button>
            <el-button link type="info" @click="handleCopy(row)">
              <el-icon><CopyDocument /></el-icon>复制
            </el-button>
            <el-button link :type="row.status === 'active' ? 'warning' : 'success'" @click="handleToggleStatus(row)">
              <el-icon><Switch /></el-icon>{{ row.status === 'active' ? '停用' : '启用' }}
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
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="800px" @close="resetForm">
      <el-form ref="formRef" :model="formData" :rules="formRules" label-width="120px">
        <el-form-item label="优惠券名称" prop="name">
          <el-input v-model="formData.name" placeholder="请输入优惠券名称" />
        </el-form-item>
        <el-form-item label="优惠券类型" prop="type">
          <el-select v-model="formData.type" placeholder="请选择类型">
            <el-option label="满减券" value="full_reduction" />
            <el-option label="折扣券" value="discount" />
            <el-option label="立减券" value="instant" />
            <el-option label="包邮券" value="free_shipping" />
          </el-select>
        </el-form-item>
        <el-form-item label="优惠金额" prop="discountAmount" v-if="formData.type !== 'discount'">
          <el-input-number v-model="formData.discountAmount" :min="0.01" :precision="2" style="width: 100%" />
        </el-form-item>
        <el-form-item label="折扣比例" prop="discountRate" v-if="formData.type === 'discount'">
          <el-input-number v-model="formData.discountRate" :min="0.1" :max="9.9" :step="0.1" :precision="1" style="width: 100%" />
        </el-form-item>
        <el-form-item label="最低消费金额" v-if="formData.type === 'full_reduction'">
          <el-input-number v-model="formData.minAmount" :min="0" :precision="2" style="width: 100%" />
        </el-form-item>
        <el-form-item label="发放数量" prop="totalCount">
          <el-input-number v-model="formData.totalCount" :min="1" style="width: 100%" />
        </el-form-item>
        <el-form-item label="使用说明">
          <el-input v-model="formData.description" type="textarea" :rows="3" placeholder="请输入使用说明" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
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
import {
  Ticket, CircleCheck, Check, Clock, Search, Refresh, Plus, Close, Download,
  View, Edit, CopyDocument, Switch, Delete
} from '@element-plus/icons-vue'

// 响应式数据
const loading = ref(false)
const dialogVisible = ref(false)
const dialogTitle = ref('新建优惠券')
const couponList = ref([])
const selectedCoupons = ref([])

// 统计数据
const couponStats = reactive({
  total: 245,
  active: 78,
  used: 1256,
  expired: 89
})

// 搜索表单
const searchForm = reactive({
  name: '',
  type: '',
  status: ''
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
  discountAmount: 0,
  discountRate: 8.5,
  minAmount: 0,
  totalCount: 100,
  description: ''
})

// 表单验证规则
const formRules = {
  name: [{ required: true, message: '请输入优惠券名称', trigger: 'blur' }],
  type: [{ required: true, message: '请选择优惠券类型', trigger: 'change' }],
  totalCount: [{ required: true, message: '请输入发放数量', trigger: 'blur' }]
}

const formRef = ref()

// 初始化
onMounted(() => {
  fetchCouponList()
})

// 获取优惠券列表
const fetchCouponList = async () => {
  loading.value = true
  setTimeout(() => {
    couponList.value = mockCouponData()
    pagination.total = couponList.value.length
    loading.value = false
  }, 500)
}

// 搜索处理
const handleSearch = () => {
  pagination.page = 1
  fetchCouponList()
}

const handleReset = () => {
  Object.assign(searchForm, { name: '', type: '', status: '' })
  fetchCouponList()
}

// CRUD操作
const handleAdd = () => {
  dialogTitle.value = '新建优惠券'
  resetForm()
  dialogVisible.value = true
}

const handleEdit = (row) => {
  dialogTitle.value = '编辑优惠券'
  Object.assign(formData, { ...row })
  dialogVisible.value = true
}

const handleView = (row) => {
  ElMessage.info('查看优惠券详情')
}

const handleCopy = (row) => {
  dialogTitle.value = '复制优惠券'
  Object.assign(formData, { ...row, id: '', name: `${row.name}-复制` })
  dialogVisible.value = true
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm('确定要删除这个优惠券吗？', '确认删除', { type: 'warning' })
    ElMessage.success('删除成功')
    fetchCouponList()
  } catch (error) {}
}

const handleToggleStatus = async (row) => {
  const action = row.status === 'active' ? '停用' : '启用'
  try {
    await ElMessageBox.confirm(`确定要${action}这个优惠券吗？`, `确认${action}`, { type: 'warning' })
    row.status = row.status === 'active' ? 'disabled' : 'active'
    ElMessage.success(`${action}成功`)
  } catch (error) {}
}

const handleSubmit = async () => {
  try {
    await formRef.value.validate()
    ElMessage.success(dialogTitle.value.includes('新建') ? '创建成功' : '编辑成功')
    dialogVisible.value = false
    fetchCouponList()
  } catch (error) {
    ElMessage.error('表单验证失败')
  }
}

const resetForm = () => {
  Object.assign(formData, {
    id: '', name: '', type: '', discountAmount: 0, discountRate: 8.5, minAmount: 0, totalCount: 100, description: ''
  })
}

// 批量操作
const handleBatchEnable = () => {
  if (selectedCoupons.value.length === 0) {
    ElMessage.warning('请选择要启用的优惠券')
    return
  }
  ElMessage.info('批量启用功能')
}

const handleBatchDisable = () => {
  if (selectedCoupons.value.length === 0) {
    ElMessage.warning('请选择要停用的优惠券')
    return
  }
  ElMessage.info('批量停用功能')
}

const handleExport = () => ElMessage.info('导出数据功能')
const handleRefresh = () => fetchCouponList()

// 工具函数
const getTypeText = (type) => {
  const map = { 'full_reduction': '满减券', 'discount': '折扣券', 'instant': '立减券', 'free_shipping': '包邮券' }
  return map[type] || type
}

const getTypeColor = (type) => {
  const map = { 'full_reduction': 'primary', 'discount': 'success', 'instant': 'warning', 'free_shipping': 'info' }
  return map[type] || 'info'
}

const getStatusText = (status) => {
  const map = { 'pending': '待生效', 'active': '进行中', 'ended': '已结束', 'disabled': '已停用' }
  return map[status] || status
}

const getStatusType = (status) => {
  const map = { 'pending': 'info', 'active': 'success', 'ended': 'warning', 'disabled': 'danger' }
  return map[status] || 'info'
}

const formatCouponValue = (coupon) => {
  switch (coupon.type) {
    case 'full_reduction': return `减¥${coupon.discountAmount}`
    case 'discount': return `${coupon.discountRate}折`
    case 'instant': return `立减¥${coupon.discountAmount}`
    case 'free_shipping': return '包邮'
    default: return '-'
  }
}

const getRemainClass = (count) => {
  if (count <= 0) return 'remain-out'
  if (count <= 10) return 'remain-low'
  return 'remain-normal'
}

const handleSelectionChange = (selection) => {
  selectedCoupons.value = selection
}

const handleSizeChange = (size) => {
  pagination.size = size
  fetchCouponList()
}

const handleCurrentChange = (page) => {
  pagination.page = page
  fetchCouponList()
}

// 模拟数据
const mockCouponData = () => {
  const types = ['full_reduction', 'discount', 'instant', 'free_shipping']
  const statuses = ['pending', 'active', 'ended', 'disabled']
  const data = []
  
  for (let i = 1; i <= 20; i++) {
    const type = types[Math.floor(Math.random() * types.length)]
    const totalCount = Math.floor(Math.random() * 1000) + 100
    const usedCount = Math.floor(totalCount * Math.random() * 0.8)
    
    data.push({
      id: i,
      name: `优惠券${i}`,
      type: type,
      discountAmount: type !== 'discount' ? (Math.random() * 50 + 5).toFixed(2) : 0,
      discountRate: type === 'discount' ? (Math.random() * 3 + 6).toFixed(1) : 0,
      minAmount: type === 'full_reduction' ? (Math.random() * 200 + 50).toFixed(2) : 0,
      totalCount: totalCount,
      usedCount: usedCount,
      remainCount: totalCount - usedCount,
      status: statuses[Math.floor(Math.random() * statuses.length)],
      description: `优惠券${i}的使用说明`
    })
  }
  
  return data
}
</script>

<style scoped>
.coupon-management {
  padding: 20px;
}

.stat-card {
  border-left: 4px solid #409eff;
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

.search-card, .toolbar-card, .table-card {
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

.coupon-value {
  font-weight: bold;
  color: #e6a23c;
}

.remain-normal {
  color: #67c23a;
  font-weight: bold;
}

.remain-low {
  color: #e6a23c;
  font-weight: bold;
}

.remain-out {
  color: #f56c6c;
  font-weight: bold;
}
</style>
