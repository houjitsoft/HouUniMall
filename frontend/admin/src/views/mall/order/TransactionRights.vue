<template>
  <div class="platform-rights-container">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-left">
        <h2>平台介入</h2>
        <span class="subtitle">处理买卖双方交易纠纷，平台仲裁维权</span>
      </div>
      <div class="header-right">
        <el-button icon="Refresh" @click="handleRefresh">刷新</el-button>
      </div>
    </div>

    <!-- 搜索筛选 -->
    <el-card class="search-card">
      <el-form :model="searchForm" inline>
        <el-form-item label="维权单号">
          <el-input v-model="searchForm.interventionNo" placeholder="请输入维权单号" clearable style="width: 180px" />
        </el-form-item>
        <el-form-item label="维权类型">
          <el-select v-model="searchForm.interventionType" placeholder="全部" clearable style="width: 140px">
            <el-option label="退款维权" value="refund" />
            <el-option label="退货维权" value="return" />
            <el-option label="质量问题" value="quality" />
            <el-option label="服务纠纷" value="service" />
          </el-select>
        </el-form-item>
        <el-form-item label="维权状态">
          <el-select v-model="searchForm.status" placeholder="全部" clearable style="width: 140px">
            <el-option label="待处理" :value="0" />
            <el-option label="处理中" :value="1" />
            <el-option label="已解决" :value="2" />
            <el-option label="已拒绝" :value="3" />
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
        <el-table-column prop="interventionNo" label="维权单号" width="180" fixed="left">
          <template #default="{ row }">
            <el-link type="primary" @click="handleViewDetail(row)">{{ row.interventionNo }}</el-link>
          </template>
        </el-table-column>
        <el-table-column prop="orderNo" label="订单编号" width="180" />
        <el-table-column prop="goodsName" label="商品名称" min-width="200" show-overflow-tooltip />
        <el-table-column prop="interventionType" label="维权类型" width="120">
          <template #default="{ row }">
            <el-tag size="small">{{ getTypeText(row.interventionType) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="reason" label="维权原因" min-width="200" show-overflow-tooltip />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)" size="small">{{ getStatusText(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="申请时间" width="160" />
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
    <el-dialog v-model="processDialogVisible" title="平台仲裁处理" width="600px">
      <el-form :model="processForm" label-width="100px">
        <el-form-item label="维权单号">
          <el-input v-model="processForm.interventionNo" disabled />
        </el-form-item>
        <el-form-item label="仲裁结果" required>
          <el-select v-model="processForm.result" placeholder="请选择仲裁结果" style="width: 100%">
            <el-option label="支持买家" value="buyer" />
            <el-option label="支持卖家" value="seller" />
            <el-option label="协商解决" value="negotiate" />
          </el-select>
        </el-form-item>
        <el-form-item label="处理方案" required>
          <el-select v-model="processForm.solution" placeholder="请选择处理方案" style="width: 100%">
            <el-option label="全额退款" value="full_refund" />
            <el-option label="部分退款" value="partial_refund" />
            <el-option label="换货处理" value="exchange" />
            <el-option label="补偿处理" value="compensation" />
            <el-option label="维持原判" value="maintain" />
          </el-select>
        </el-form-item>
        <el-form-item label="处理说明" required>
          <el-input v-model="processForm.remark" type="textarea" :rows="4" placeholder="请输入处理说明" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="processDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmitProcess" :loading="submitLoading">提交</el-button>
      </template>
    </el-dialog>

    <!-- 详情对话框 -->
    <el-dialog v-model="detailDialogVisible" title="维权详情" width="750px">
      <el-descriptions :column="2" border>
        <el-descriptions-item label="维权单号">{{ detail.interventionNo }}</el-descriptions-item>
        <el-descriptions-item label="订单编号">{{ detail.orderNo }}</el-descriptions-item>
        <el-descriptions-item label="商品名称" :span="2">{{ detail.goodsName }}</el-descriptions-item>
        <el-descriptions-item label="维权类型">
          <el-tag size="small">{{ getTypeText(detail.interventionType) }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="当前状态">
          <el-tag :type="getStatusType(detail.status)">{{ getStatusText(detail.status) }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="维权原因" :span="2">{{ detail.reason }}</el-descriptions-item>
        <el-descriptions-item label="买家诉求" :span="2">{{ detail.buyerClaim || '暂无' }}</el-descriptions-item>
        <el-descriptions-item label="卖家回应" :span="2">{{ detail.sellerResponse || '暂无' }}</el-descriptions-item>
        <el-descriptions-item label="申请时间">{{ detail.createTime }}</el-descriptions-item>
        <el-descriptions-item label="更新时间">{{ detail.updateTime }}</el-descriptions-item>
        <el-descriptions-item label="处理结果" :span="2">{{ detail.handleResult || '暂未处理' }}</el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script setup>
/**
 * 平台介入（交易维权）页面
 * 功能：维权列表查询、详情查看、平台仲裁处理
 */
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Refresh, Search, RefreshLeft } from '@element-plus/icons-vue'
import { getPlatformList, getPlatformDetail, processPlatform } from '@/api/order/order'

// 搜索表单
const searchForm = reactive({
  interventionNo: '',
  interventionType: '',
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
  interventionId: '',
  interventionNo: '',
  result: '',
  solution: '',
  remark: ''
})
const submitLoading = ref(false)

// 详情对话框
const detailDialogVisible = ref(false)
const detail = reactive({
  interventionNo: '',
  orderNo: '',
  goodsName: '',
  interventionType: '',
  reason: '',
  buyerClaim: '',
  sellerResponse: '',
  status: 0,
  createTime: '',
  updateTime: '',
  handleResult: ''
})

/**
 * 获取平台介入列表
 * @description 调用BFF接口获取分页列表
 */
const fetchList = async () => {
  loading.value = true
  try {
    const params = { page: pagination.page, size: pagination.size, ...searchForm }
    const response = await getPlatformList(params)
    if (response.code === 200) {
      tableData.value = response.data?.list || response.data?.records || []
      pagination.total = response.data?.total || 0
    }
  } catch (error) {
    console.error('获取平台介入列表失败:', error)
    ElMessage.error('获取列表失败')
  } finally {
    loading.value = false
  }
}

/**
 * 获取维权类型文本
 * @param {string} type - 类型值
 * @returns {string} 类型中文文本
 */
const getTypeText = (type) => {
  const map = { refund: '退款维权', return: '退货维权', quality: '质量问题', service: '服务纠纷' }
  return map[type] || type || '未知'
}

/**
 * 获取状态对应的标签类型
 * @param {number} status - 状态值
 * @returns {string} Element Plus tag类型
 */
const getStatusType = (status) => {
  const map = { 0: 'warning', 1: 'primary', 2: 'success', 3: 'danger' }
  return map[status] || 'info'
}

/**
 * 获取状态文本
 * @param {number} status - 状态值
 * @returns {string} 状态中文文本
 */
const getStatusText = (status) => {
  const map = { 0: '待处理', 1: '处理中', 2: '已解决', 3: '已拒绝' }
  return map[status] || '未知'
}

/** 查询 */
const handleSearch = () => {
  pagination.page = 1
  fetchList()
}

/** 重置搜索条件 */
const handleReset = () => {
  Object.assign(searchForm, { interventionNo: '', interventionType: '', status: '' })
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
 * 查看维权详情
 * @param {Object} row - 当前行数据
 */
const handleViewDetail = async (row) => {
  try {
    const response = await getPlatformDetail({ interventionId: row.id })
    if (response.code === 200) {
      Object.assign(detail, response.data)
      detailDialogVisible.value = true
    } else {
      ElMessage.error(response.message || '获取详情失败')
    }
  } catch (error) {
    console.error('获取维权详情失败:', error)
    ElMessage.error('获取详情失败')
  }
}

/**
 * 打开处理对话框
 * @param {Object} row - 当前行数据
 */
const handleProcess = (row) => {
  processForm.interventionId = row.id
  processForm.interventionNo = row.interventionNo
  processForm.result = ''
  processForm.solution = ''
  processForm.remark = ''
  processDialogVisible.value = true
}

/**
 * 提交平台仲裁处理
 * @description 调用BFF接口提交仲裁结果
 */
const handleSubmitProcess = async () => {
  if (!processForm.result) {
    ElMessage.warning('请选择仲裁结果')
    return
  }
  if (!processForm.solution) {
    ElMessage.warning('请选择处理方案')
    return
  }
  if (!processForm.remark) {
    ElMessage.warning('请输入处理说明')
    return
  }
  submitLoading.value = true
  try {
    const response = await processPlatform({
      interventionId: processForm.interventionId,
      result: processForm.result,
      solution: processForm.solution,
      remark: processForm.remark
    })
    if (response.code === 200) {
      ElMessage.success('处理成功')
      processDialogVisible.value = false
      fetchList()
    } else {
      ElMessage.error(response.message || '处理失败')
    }
  } catch (error) {
    console.error('平台仲裁处理失败:', error)
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
.platform-rights-container {
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
