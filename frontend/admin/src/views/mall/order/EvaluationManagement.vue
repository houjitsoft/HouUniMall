<template>
  <div class="evaluation-management-container">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-left">
        <h2>评价管理</h2>
        <span class="subtitle">管理商品评价，维护平台口碑</span>
      </div>
      <div class="header-right">
        <el-button icon="Refresh" @click="handleRefresh">刷新</el-button>
      </div>
    </div>

    <!-- 搜索筛选 -->
    <el-card class="search-card">
      <el-form :model="searchForm" inline>
        <el-form-item label="订单编号">
          <el-input v-model="searchForm.orderNo" placeholder="请输入订单编号" clearable style="width: 180px" />
        </el-form-item>
        <el-form-item label="商品名称">
          <el-input v-model="searchForm.goodsName" placeholder="请输入商品名称" clearable style="width: 180px" />
        </el-form-item>
        <el-form-item label="评分">
          <el-select v-model="searchForm.rating" placeholder="全部" clearable style="width: 120px">
            <el-option label="5星" :value="5" />
            <el-option label="4星" :value="4" />
            <el-option label="3星" :value="3" />
            <el-option label="2星" :value="2" />
            <el-option label="1星" :value="1" />
          </el-select>
        </el-form-item>
        <el-form-item label="回复状态">
          <el-select v-model="searchForm.replyStatus" placeholder="全部" clearable style="width: 120px">
            <el-option label="未回复" :value="0" />
            <el-option label="已回复" :value="1" />
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
        <el-table-column prop="orderNo" label="订单编号" width="180" />
        <el-table-column prop="goodsName" label="商品名称" min-width="200" show-overflow-tooltip />
        <el-table-column prop="userName" label="评价人" width="120" />
        <el-table-column prop="rating" label="评分" width="160">
          <template #default="{ row }">
            <el-rate v-model="row.rating" disabled show-score />
          </template>
        </el-table-column>
        <el-table-column prop="content" label="评价内容" min-width="250" show-overflow-tooltip />
        <el-table-column prop="images" label="图片" width="100">
          <template #default="{ row }">
            <span v-if="row.images && row.images.length">{{ row.images.length }}张</span>
            <span v-else class="text-muted">无</span>
          </template>
        </el-table-column>
        <el-table-column prop="replyStatus" label="回复状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.replyStatus === 1 ? 'success' : 'info'" size="small">
              {{ row.replyStatus === 1 ? '已回复' : '未回复' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="评价时间" width="160" />
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button v-if="row.replyStatus !== 1" type="primary" link size="small" @click="handleReply(row)">回复</el-button>
            <el-button type="info" link size="small" @click="handleViewDetail(row)">详情</el-button>
            <el-button type="danger" link size="small" @click="handleDelete(row)">删除</el-button>
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

    <!-- 回复对话框 -->
    <el-dialog v-model="replyDialogVisible" title="回复评价" width="550px">
      <div class="reply-evaluation-info">
        <p><strong>评价人：</strong>{{ replyForm.userName }}</p>
        <p><strong>评分：</strong><el-rate v-model="replyForm.rating" disabled /></p>
        <p><strong>评价内容：</strong>{{ replyForm.content }}</p>
      </div>
      <el-divider />
      <el-form label-width="80px">
        <el-form-item label="回复内容" required>
          <el-input v-model="replyForm.replyContent" type="textarea" :rows="4" placeholder="请输入回复内容" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="replyDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmitReply" :loading="submitLoading">提交回复</el-button>
      </template>
    </el-dialog>

    <!-- 详情对话框 -->
    <el-dialog v-model="detailDialogVisible" title="评价详情" width="650px">
      <el-descriptions :column="2" border>
        <el-descriptions-item label="订单编号">{{ detail.orderNo }}</el-descriptions-item>
        <el-descriptions-item label="评价人">{{ detail.userName }}</el-descriptions-item>
        <el-descriptions-item label="商品名称" :span="2">{{ detail.goodsName }}</el-descriptions-item>
        <el-descriptions-item label="评分" :span="2">
          <el-rate v-model="detail.rating" disabled show-score />
        </el-descriptions-item>
        <el-descriptions-item label="评价内容" :span="2">{{ detail.content }}</el-descriptions-item>
        <el-descriptions-item label="评价图片" :span="2">
          <div v-if="detail.images && detail.images.length" class="image-list">
            <el-image
              v-for="(img, index) in detail.images"
              :key="index"
              :src="img"
              :preview-src-list="detail.images"
              fit="cover"
              class="eval-image"
            />
          </div>
          <span v-else>无图片</span>
        </el-descriptions-item>
        <el-descriptions-item label="评价时间">{{ detail.createTime }}</el-descriptions-item>
        <el-descriptions-item label="回复状态">
          <el-tag :type="detail.replyStatus === 1 ? 'success' : 'info'" size="small">
            {{ detail.replyStatus === 1 ? '已回复' : '未回复' }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item v-if="detail.replyContent" label="商家回复" :span="2">
          {{ detail.replyContent }}
        </el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script setup>
/**
 * 评价管理页面
 * 功能：评价列表查询、回复评价、删除评价、查看详情
 */
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Refresh, Search, RefreshLeft } from '@element-plus/icons-vue'
import { getEvaluationList, deleteEvaluation, replyEvaluation } from '@/api/order/order'

// 搜索表单
const searchForm = reactive({
  orderNo: '',
  goodsName: '',
  rating: '',
  replyStatus: ''
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

// 回复对话框
const replyDialogVisible = ref(false)
const replyForm = reactive({
  evaluationId: '',
  userName: '',
  rating: 0,
  content: '',
  replyContent: ''
})
const submitLoading = ref(false)

// 详情对话框
const detailDialogVisible = ref(false)
const detail = reactive({
  orderNo: '',
  userName: '',
  goodsName: '',
  rating: 0,
  content: '',
  images: [],
  replyStatus: 0,
  replyContent: '',
  createTime: ''
})

/**
 * 获取评价列表数据
 * @description 调用BFF接口获取分页列表
 */
const fetchList = async () => {
  loading.value = true
  try {
    const params = { page: pagination.page, size: pagination.size, ...searchForm }
    const response = await getEvaluationList(params)
    if (response.code === 200) {
      tableData.value = response.data?.list || response.data?.records || []
      pagination.total = response.data?.total || 0
    }
  } catch (error) {
    console.error('获取评价列表失败:', error)
    ElMessage.error('获取评价列表失败')
  } finally {
    loading.value = false
  }
}

/** 查询 */
const handleSearch = () => {
  pagination.page = 1
  fetchList()
}

/** 重置搜索条件 */
const handleReset = () => {
  Object.assign(searchForm, { orderNo: '', goodsName: '', rating: '', replyStatus: '' })
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
 * 查看评价详情
 * @param {Object} row - 当前行数据
 */
const handleViewDetail = (row) => {
  Object.assign(detail, row)
  detailDialogVisible.value = true
}

/**
 * 打开回复对话框
 * @param {Object} row - 当前行数据
 */
const handleReply = (row) => {
  replyForm.evaluationId = row.id
  replyForm.userName = row.userName
  replyForm.rating = row.rating
  replyForm.content = row.content
  replyForm.replyContent = ''
  replyDialogVisible.value = true
}

/**
 * 提交回复
 * @description 调用BFF接口提交评价回复
 */
const handleSubmitReply = async () => {
  if (!replyForm.replyContent) {
    ElMessage.warning('请输入回复内容')
    return
  }
  submitLoading.value = true
  try {
    const response = await replyEvaluation({
      evaluationId: replyForm.evaluationId,
      replyContent: replyForm.replyContent
    })
    if (response.code === 200) {
      ElMessage.success('回复成功')
      replyDialogVisible.value = false
      fetchList()
    } else {
      ElMessage.error(response.message || '回复失败')
    }
  } catch (error) {
    console.error('回复评价失败:', error)
    ElMessage.error('回复失败')
  } finally {
    submitLoading.value = false
  }
}

/**
 * 删除评价
 * @param {Object} row - 当前行数据
 */
const handleDelete = (row) => {
  ElMessageBox.confirm('确认删除该条评价吗？删除后不可恢复。', '确认删除', {
    confirmButtonText: '确定删除',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      const response = await deleteEvaluation({ evaluationId: row.id })
      if (response.code === 200) {
        ElMessage.success('删除成功')
        fetchList()
      } else {
        ElMessage.error(response.message || '删除失败')
      }
    } catch (error) {
      console.error('删除评价失败:', error)
      ElMessage.error('删除失败')
    }
  }).catch(() => {})
}

// 初始化
onMounted(() => {
  fetchList()
})
</script>

<style scoped lang="scss">
.evaluation-management-container {
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

  .text-muted { color: #909399; }
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

.reply-evaluation-info {
  p {
    margin: 8px 0;
    color: #606266;
  }
}

.image-list {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;

  .eval-image {
    width: 80px;
    height: 80px;
    border-radius: 4px;
  }
}
</style>
