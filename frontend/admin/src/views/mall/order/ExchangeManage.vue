<template>
  <div class="exchange-management-container">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-left">
        <h2>换货管理</h2>
        <span class="subtitle">处理换货申请，保障消费者权益</span>
      </div>
      <div class="header-right">
        <el-button icon="Refresh" @click="handleRefresh">刷新</el-button>
      </div>
    </div>

    <!-- 搜索筛选 -->
    <el-card class="search-card">
      <el-form :model="searchForm" inline>
        <el-form-item label="换货单号">
          <el-input v-model="searchForm.barterNo" placeholder="请输入换货单号" clearable style="width: 180px" />
        </el-form-item>
        <el-form-item label="订单编号">
          <el-input v-model="searchForm.orderNo" placeholder="请输入订单编号" clearable style="width: 180px" />
        </el-form-item>
        <el-form-item label="换货状态">
          <el-select v-model="searchForm.status" placeholder="全部" clearable style="width: 140px">
            <el-option label="待审核" :value="0" />
            <el-option label="待换货" :value="1" />
            <el-option label="换货中" :value="2" />
            <el-option label="已换货" :value="3" />
            <el-option label="已拒绝" :value="4" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" icon="Search" @click="handleSearch">查询</el-button>
          <el-button icon="RefreshLeft" @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 数据列表 -->
    <el-card class="table-card">
      <el-table :data="tableData" v-loading="loading" stripe style="width: 100%">
        <el-table-column prop="barterNo" label="换货单号" width="180" fixed="left">
          <template #default="{ row }">
            <el-link type="primary" @click="handleViewDetail(row)">{{ row.barterNo }}</el-link>
          </template>
        </el-table-column>
        <el-table-column prop="orderNo" label="订单编号" width="180" />
        <el-table-column prop="goodsName" label="商品名称" min-width="200" show-overflow-tooltip />
        <el-table-column prop="barterNum" label="换货数量" width="100" />
        <el-table-column prop="barterReason" label="换货原因" min-width="200" show-overflow-tooltip />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)" size="small">{{ getStatusText(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="申请时间" width="160" />
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="handleViewDetail(row)">查看详情</el-button>
            <el-button v-if="row.status === 0" type="success" link size="small" @click="handleApprove(row, true)">同意</el-button>
            <el-button v-if="row.status === 0" type="danger" link size="small" @click="handleApprove(row, false)">拒绝</el-button>
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
          @current-change="handlePageChange"
        />
      </div>
    </el-card>

    <!-- 审核对话框 -->
    <el-dialog v-model="approveDialogVisible" title="换货审核" width="500px">
      <el-form :model="approveForm" label-width="100px">
        <el-form-item label="换货单号">
          <el-input v-model="approveForm.barterNo" disabled />
        </el-form-item>
        <el-form-item label="审核结果">
          <el-tag :type="approveForm.approved ? 'success' : 'danger'">
            {{ approveForm.approved ? '同意换货' : '拒绝换货' }}
          </el-tag>
        </el-form-item>
        <el-form-item label="审核备注" required>
          <el-input v-model="approveForm.remark" type="textarea" :rows="4" placeholder="请输入审核备注" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="approveDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmitApprove" :loading="submitLoading">提交</el-button>
      </template>
    </el-dialog>

    <!-- 详情对话框 -->
    <el-dialog v-model="detailDialogVisible" title="换货详情" width="700px">
      <el-descriptions :column="2" border>
        <el-descriptions-item label="换货单号">{{ detail.barterNo }}</el-descriptions-item>
        <el-descriptions-item label="订单编号">{{ detail.orderNo }}</el-descriptions-item>
        <el-descriptions-item label="商品名称" :span="2">{{ detail.goodsName }}</el-descriptions-item>
        <el-descriptions-item label="换货数量">{{ detail.barterNum }}</el-descriptions-item>
        <el-descriptions-item label="当前状态">
          <el-tag :type="getStatusType(detail.status)">{{ getStatusText(detail.status) }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="换货原因" :span="2">{{ detail.barterReason }}</el-descriptions-item>
        <el-descriptions-item label="申请时间">{{ detail.createTime }}</el-descriptions-item>
        <el-descriptions-item label="更新时间">{{ detail.updateTime }}</el-descriptions-item>
        <el-descriptions-item label="买家备注" :span="2">{{ detail.buyerRemark || '无' }}</el-descriptions-item>
        <el-descriptions-item label="卖家备注" :span="2">{{ detail.sellerRemark || '无' }}</el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script setup>
/**
 * 换货管理页面
 * 功能：列表查询、详情查看、审核(同意/拒绝)
 */
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Refresh, Search, RefreshLeft } from '@element-plus/icons-vue'
import { getExchangeList, getExchangeDetail, approveExchange } from '@/api/order/order'

// 搜索表单
const searchForm = reactive({
  barterNo: '',
  orderNo: '',
  status: ''
})

// 表格数据
const tableData = ref([])
const loading = ref(false)

// 分页
const pagination = reactive({
  page: 1,
  size: 20,
  total: 0
})

// 审核对话框
const approveDialogVisible = ref(false)
const approveForm = reactive({
  barterId: '',
  barterNo: '',
  approved: true,
  remark: ''
})
const submitLoading = ref(false)

// 详情对话框
const detailDialogVisible = ref(false)
const detail = reactive({
  barterNo: '',
  orderNo: '',
  goodsName: '',
  barterNum: 0,
  barterReason: '',
  status: 0,
  createTime: '',
  updateTime: '',
  buyerRemark: '',
  sellerRemark: ''
})

/**
 * 获取换货列表数据
 * @description 调用BFF接口获取分页列表
 */
const fetchList = async () => {
  loading.value = true
  try {
    const params = {
      page: pagination.page,
      size: pagination.size,
      ...searchForm
    }
    const response = await getExchangeList(params)
    if (response.code === 200) {
      tableData.value = response.data?.list || response.data?.records || []
      pagination.total = response.data?.total || 0
    }
  } catch (error) {
    console.error('获取换货列表失败:', error)
    ElMessage.error('获取换货列表失败')
  } finally {
    loading.value = false
  }
}

/**
 * 获取状态对应的标签类型
 * @param {number} status - 状态值
 * @returns {string} Element Plus tag类型
 */
const getStatusType = (status) => {
  const map = { 0: 'warning', 1: 'primary', 2: 'primary', 3: 'success', 4: 'danger' }
  return map[status] || 'info'
}

/**
 * 获取状态文本
 * @param {number} status - 状态值
 * @returns {string} 状态中文文本
 */
const getStatusText = (status) => {
  const map = { 0: '待审核', 1: '待换货', 2: '换货中', 3: '已换货', 4: '已拒绝' }
  return map[status] || '未知'
}

/** 查询 */
const handleSearch = () => {
  pagination.page = 1
  fetchList()
}

/** 重置搜索条件 */
const handleReset = () => {
  Object.assign(searchForm, { barterNo: '', orderNo: '', status: '' })
  handleSearch()
}

/** 刷新 */
const handleRefresh = () => {
  fetchList()
  ElMessage.success('刷新成功')
}

/** 分页变化 */
const handlePageChange = (page) => {
  pagination.page = page
  fetchList()
}

const handleSizeChange = (size) => {
  pagination.size = size
  pagination.page = 1
  fetchList()
}

/**
 * 查看换货详情
 * @param {Object} row - 当前行数据
 */
const handleViewDetail = async (row) => {
  try {
    const response = await getExchangeDetail({ barterId: row.id })
    if (response.code === 200) {
      Object.assign(detail, response.data)
      detailDialogVisible.value = true
    } else {
      ElMessage.error(response.message || '获取详情失败')
    }
  } catch (error) {
    console.error('获取换货详情失败:', error)
    ElMessage.error('获取详情失败')
  }
}

/**
 * 打开审核对话框
 * @param {Object} row - 当前行数据
 * @param {boolean} approved - 是否同意
 */
const handleApprove = (row, approved) => {
  approveForm.barterId = row.id
  approveForm.barterNo = row.barterNo
  approveForm.approved = approved
  approveForm.remark = ''
  approveDialogVisible.value = true
}

/**
 * 提交审核结果
 * @description 调用BFF接口提交审核
 */
const handleSubmitApprove = async () => {
  if (!approveForm.remark) {
    ElMessage.warning('请输入审核备注')
    return
  }
  submitLoading.value = true
  try {
    const response = await approveExchange({
      barterId: approveForm.barterId,
      status: approveForm.approved ? 1 : 4,
      remark: approveForm.remark
    })
    if (response.code === 200) {
      ElMessage.success('操作成功')
      approveDialogVisible.value = false
      fetchList()
    } else {
      ElMessage.error(response.message || '操作失败')
    }
  } catch (error) {
    console.error('审核失败:', error)
    ElMessage.error('审核操作失败')
  } finally {
    submitLoading.value = false
  }
}

// 初始化
onMounted(() => {
  fetchList()
})
</script>

<style scoped lang="scss">
.exchange-management-container {
  padding: 20px;
  background: #f5f7fa;
  min-height: calc(100vh - 60px);
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;

  .header-left {
    h2 { font-size: 24px; font-weight: 600; color: #303133; margin: 0 0 8px 0; }
    .subtitle { font-size: 14px; color: #909399; }
  }
}

.search-card {
  margin-bottom: 20px;
  :deep(.el-card__body) { padding: 20px; }
}

.table-card {
  :deep(.el-card__body) { padding: 20px; }
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}
</style>
