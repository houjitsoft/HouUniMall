<template>
  <div class="complaint-management-container">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-left">
        <h2>投诉管理</h2>
        <span class="subtitle">处理客户投诉，提升服务质量</span>
      </div>
      <div class="header-right">
        <el-button icon="Refresh" @click="handleRefresh">刷新</el-button>
      </div>
    </div>

    <!-- 搜索筛选 -->
    <el-card class="search-card">
      <el-form :model="searchForm" inline>
        <el-form-item label="投诉单号">
          <el-input v-model="searchForm.complaintsNo" placeholder="请输入投诉单号" clearable style="width: 180px" />
        </el-form-item>
        <el-form-item label="投诉类型">
          <el-select v-model="searchForm.complaintsType" placeholder="全部" clearable style="width: 140px">
            <el-option label="商品问题" value="goods" />
            <el-option label="服务问题" value="service" />
            <el-option label="物流问题" value="logistics" />
            <el-option label="其他" value="other" />
          </el-select>
        </el-form-item>
        <el-form-item label="处理状态">
          <el-select v-model="searchForm.status" placeholder="全部" clearable style="width: 140px">
            <el-option label="待处理" :value="0" />
            <el-option label="处理中" :value="1" />
            <el-option label="已处理" :value="2" />
            <el-option label="已关闭" :value="3" />
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
        <el-table-column prop="complaintsNo" label="投诉单号" width="180" fixed="left">
          <template #default="{ row }">
            <el-link type="primary" @click="handleViewDetail(row)">{{ row.complaintsNo }}</el-link>
          </template>
        </el-table-column>
        <el-table-column prop="orderNo" label="订单编号" width="180" />
        <el-table-column prop="complaintsType" label="投诉类型" width="120">
          <template #default="{ row }">
            <el-tag size="small">{{ getTypeText(row.complaintsType) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="complaintsContent" label="投诉内容" min-width="250" show-overflow-tooltip />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)" size="small">{{ getStatusText(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="投诉时间" width="160" />
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="handleViewDetail(row)">查看详情</el-button>
            <el-button v-if="row.status === 0 || row.status === 1" type="warning" link size="small" @click="handleProcess(row)">处理</el-button>
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

    <!-- 处理对话框 -->
    <el-dialog v-model="processDialogVisible" title="处理投诉" width="600px">
      <el-form :model="processForm" label-width="100px">
        <el-form-item label="投诉单号">
          <el-input v-model="processForm.complaintsNo" disabled />
        </el-form-item>
        <el-form-item label="投诉内容">
          <el-input v-model="processForm.complaintsContent" type="textarea" :rows="3" disabled />
        </el-form-item>
        <el-form-item label="处理结果" required>
          <el-select v-model="processForm.handleResult" placeholder="请选择处理结果" style="width: 100%">
            <el-option label="已解决" value="resolved" />
            <el-option label="部分解决" value="partial" />
            <el-option label="无法处理" value="cannot" />
          </el-select>
        </el-form-item>
        <el-form-item label="处理备注" required>
          <el-input v-model="processForm.handleRemark" type="textarea" :rows="4" placeholder="请输入处理备注" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="processDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmitProcess" :loading="submitLoading">提交</el-button>
      </template>
    </el-dialog>

    <!-- 详情对话框 -->
    <el-dialog v-model="detailDialogVisible" title="投诉详情" width="700px">
      <el-descriptions :column="2" border>
        <el-descriptions-item label="投诉单号">{{ detail.complaintsNo }}</el-descriptions-item>
        <el-descriptions-item label="订单编号">{{ detail.orderNo }}</el-descriptions-item>
        <el-descriptions-item label="投诉类型">
          <el-tag size="small">{{ getTypeText(detail.complaintsType) }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="当前状态">
          <el-tag :type="getStatusType(detail.status)">{{ getStatusText(detail.status) }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="投诉内容" :span="2">{{ detail.complaintsContent }}</el-descriptions-item>
        <el-descriptions-item label="投诉时间">{{ detail.createTime }}</el-descriptions-item>
        <el-descriptions-item label="更新时间">{{ detail.updateTime }}</el-descriptions-item>
        <el-descriptions-item label="处理结果" :span="2">{{ detail.handleResult || '暂未处理' }}</el-descriptions-item>
        <el-descriptions-item label="处理备注" :span="2">{{ detail.handleRemark || '暂无' }}</el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script setup>
/**
 * 投诉管理页面
 * 功能：列表查询、详情查看、处理投诉
 */
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Refresh, Search, RefreshLeft } from '@element-plus/icons-vue'
import { getComplaintList, getComplaintDetail, handleComplaint } from '@/api/order/order'

// 搜索表单
const searchForm = reactive({
  complaintsNo: '',
  complaintsType: '',
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

// 处理对话框
const processDialogVisible = ref(false)
const processForm = reactive({
  complaintsId: '',
  complaintsNo: '',
  complaintsContent: '',
  handleResult: '',
  handleRemark: ''
})
const submitLoading = ref(false)

// 详情对话框
const detailDialogVisible = ref(false)
const detail = reactive({
  complaintsNo: '',
  orderNo: '',
  complaintsType: '',
  complaintsContent: '',
  status: 0,
  createTime: '',
  updateTime: '',
  handleResult: '',
  handleRemark: ''
})

/**
 * 获取投诉列表数据
 * @description 调用BFF接口获取分页列表
 */
const fetchList = async () => {
  loading.value = true
  try {
    const params = { page: pagination.page, size: pagination.size, ...searchForm }
    const response = await getComplaintList(params)
    if (response.code === 200) {
      tableData.value = response.data?.list || response.data?.records || []
      pagination.total = response.data?.total || 0
    }
  } catch (error) {
    console.error('获取投诉列表失败:', error)
    ElMessage.error('获取投诉列表失败')
  } finally {
    loading.value = false
  }
}

/**
 * 获取投诉类型文本
 * @param {string} type - 类型值
 * @returns {string} 类型中文文本
 */
const getTypeText = (type) => {
  const map = { goods: '商品问题', service: '服务问题', logistics: '物流问题', other: '其他' }
  return map[type] || type || '未知'
}

/**
 * 获取状态对应的标签类型
 * @param {number} status - 状态值
 * @returns {string} Element Plus tag类型
 */
const getStatusType = (status) => {
  const map = { 0: 'warning', 1: 'primary', 2: 'success', 3: 'info' }
  return map[status] || 'info'
}

/**
 * 获取状态文本
 * @param {number} status - 状态值
 * @returns {string} 状态中文文本
 */
const getStatusText = (status) => {
  const map = { 0: '待处理', 1: '处理中', 2: '已处理', 3: '已关闭' }
  return map[status] || '未知'
}

/** 查询 */
const handleSearch = () => {
  pagination.page = 1
  fetchList()
}

/** 重置搜索条件 */
const handleReset = () => {
  Object.assign(searchForm, { complaintsNo: '', complaintsType: '', status: '' })
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
 * 查看投诉详情
 * @param {Object} row - 当前行数据
 */
const handleViewDetail = async (row) => {
  try {
    const response = await getComplaintDetail({ complaintsId: row.id })
    if (response.code === 200) {
      Object.assign(detail, response.data)
      detailDialogVisible.value = true
    } else {
      ElMessage.error(response.message || '获取详情失败')
    }
  } catch (error) {
    console.error('获取投诉详情失败:', error)
    ElMessage.error('获取详情失败')
  }
}

/**
 * 打开处理对话框
 * @param {Object} row - 当前行数据
 */
const handleProcess = (row) => {
  processForm.complaintsId = row.id
  processForm.complaintsNo = row.complaintsNo
  processForm.complaintsContent = row.complaintsContent
  processForm.handleResult = ''
  processForm.handleRemark = ''
  processDialogVisible.value = true
}

/**
 * 提交处理结果
 * @description 调用BFF接口提交投诉处理
 */
const handleSubmitProcess = async () => {
  if (!processForm.handleResult) {
    ElMessage.warning('请选择处理结果')
    return
  }
  if (!processForm.handleRemark) {
    ElMessage.warning('请输入处理备注')
    return
  }
  submitLoading.value = true
  try {
    const response = await handleComplaint({
      complaintsId: processForm.complaintsId,
      handleResult: processForm.handleResult,
      handleRemark: processForm.handleRemark
    })
    if (response.code === 200) {
      ElMessage.success('处理成功')
      processDialogVisible.value = false
      fetchList()
    } else {
      ElMessage.error(response.message || '处理失败')
    }
  } catch (error) {
    console.error('处理投诉失败:', error)
    ElMessage.error('处理失败')
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
.complaint-management-container {
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
