<template>
  <div class="goods-audit-container">
    <el-card shadow="never">
      <template #header>
        <div class="card-header">
          <span>商品审核</span>
        </div>
      </template>
      
      <!-- 搜索区域 -->
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="商品名称">
          <el-input v-model="searchForm.goodsName" placeholder="请输入商品名称" clearable />
        </el-form-item>
        <el-form-item label="商家名称">
          <el-input v-model="searchForm.shopName" placeholder="请输入商家名称" clearable />
        </el-form-item>
        <el-form-item label="审核状态">
          <el-select v-model="searchForm.status" placeholder="请选择" clearable>
            <el-option label="待审核" value="pending" />
            <el-option label="审核通过" value="approved" />
            <el-option label="审核拒绝" value="rejected" />
          </el-select>
        </el-form-item>
        <el-form-item label="提交时间">
          <el-date-picker
            v-model="searchForm.dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>

      <!-- 批量操作 -->
      <div class="batch-actions">
        <el-button type="success" :disabled="!selectedRows.length" @click="handleBatchApprove">
          批量通过
        </el-button>
        <el-button type="danger" :disabled="!selectedRows.length" @click="handleBatchReject">
          批量拒绝
        </el-button>
      </div>

      <!-- 表格 -->
      <el-table 
        :data="tableData" 
        v-loading="loading" 
        stripe 
        border
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="55" />
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column label="商品图片" width="100">
          <template #default="{ row }">
            <el-image 
              :src="row.image" 
              fit="cover" 
              style="width: 60px; height: 60px"
              :preview-src-list="[row.image]"
            />
          </template>
        </el-table-column>
        <el-table-column prop="goodsName" label="商品名称" min-width="150" show-overflow-tooltip />
        <el-table-column prop="shopName" label="商家名称" width="120" />
        <el-table-column prop="categoryName" label="商品分类" width="120" />
        <el-table-column prop="price" label="价格" width="100">
          <template #default="{ row }">
            ¥{{ row.price }}
          </template>
        </el-table-column>
        <el-table-column prop="status" label="审核状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">{{ getStatusText(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="submitTime" label="提交时间" width="160" />
        <el-table-column prop="auditTime" label="审核时间" width="160" />
        <el-table-column label="操作" width="280" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleView(row)">查看</el-button>
            <AiAgentPanel
              code="goods_audit"
              label="AI预审"
              link
              size="small"
              :payload="() => buildAuditPayload(row)"
            />
            <template v-if="row.status === 'pending'">
              <el-button type="success" link @click="handleApprove(row)">通过</el-button>
              <el-button type="danger" link @click="handleReject(row)">拒绝</el-button>
            </template>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <el-pagination
        class="pagination"
        v-model:current-page="pagination.page"
        v-model:page-size="pagination.size"
        :total="pagination.total"
        :page-sizes="[10, 20, 50, 100]"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
      />
    </el-card>

    <!-- 审核弹窗 -->
    <el-dialog v-model="auditDialogVisible" title="审核拒绝" width="500px">
      <el-form :model="auditForm" label-width="100px">
        <el-form-item label="拒绝原因" required>
          <el-input
            v-model="auditForm.reason"
            type="textarea"
            :rows="4"
            placeholder="请输入拒绝原因"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="auditDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitReject">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import AiAgentPanel from '@/components/AiAgentPanel.vue'
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'

const loading = ref(false)
const tableData = ref([])
const selectedRows = ref([])
const auditDialogVisible = ref(false)
const currentRow = ref(null)

const searchForm = reactive({
  goodsName: '',
  shopName: '',
  status: '',
  dateRange: null
})

const pagination = reactive({
  page: 1,
  size: 10,
  total: 0
})

const auditForm = reactive({
  reason: ''
})

const getStatusType = (status) => {
  const types = { pending: 'warning', approved: 'success', rejected: 'danger' }
  return types[status] || 'info'
}

const getStatusText = (status) => {
  const texts = { pending: '待审核', approved: '审核通过', rejected: '审核拒绝' }
  return texts[status] || status
}

const fetchData = async () => {
  loading.value = true
  try {
    // TODO: 调用API获取数据
    tableData.value = []
    pagination.total = 0
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  pagination.page = 1
  fetchData()
}

const handleReset = () => {
  searchForm.goodsName = ''
  searchForm.shopName = ''
  searchForm.status = ''
  searchForm.dateRange = null
  handleSearch()
}

const handleSelectionChange = (rows) => {
  selectedRows.value = rows
}

const handleView = (row) => {
  ElMessage.info('查看商品详情: ' + row.id)
}

const handleApprove = (row) => {
  ElMessageBox.confirm('确定通过该商品审核吗?', '提示').then(() => {
    ElMessage.success('审核通过')
    fetchData()
  })
}

const handleReject = (row) => {
  currentRow.value = row
  auditForm.reason = ''
  auditDialogVisible.value = true
}

const submitReject = () => {
  if (!auditForm.reason) {
    ElMessage.warning('请输入拒绝原因')
    return
  }
  ElMessage.success('已拒绝')
  auditDialogVisible.value = false
  fetchData()
}

const handleBatchApprove = () => {
  ElMessageBox.confirm(`确定批量通过选中的 ${selectedRows.value.length} 个商品吗?`, '提示').then(() => {
    ElMessage.success('批量通过成功')
    fetchData()
  })
}

const handleBatchReject = () => {
  currentRow.value = null
  auditForm.reason = ''
  auditDialogVisible.value = true
}

const handleSizeChange = () => fetchData()
const handleCurrentChange = () => fetchData()

onMounted(() => {
  fetchData()
})

/**
 * 组装交给「商品内容合规审核」智能体的文本。
 * 审核页列表只有标题/分类/价格，先审这些能公开看到的文案；
 * 详情图文的审核等详情弹窗接入后再补，不在这里编造字段。
 */
const buildAuditPayload = (row) => {
  if (!row) return ''
  const lines = [
    `商品名称：${row.goodsName || ''}`,
    `所属分类：${row.categoryName || '未分类'}`,
    `商家：${row.shopName || ''}`,
    `售价：${row.price == null ? '未提供' : `¥${row.price}`}`
  ]
  return `请审核以下商品信息的合规风险：\n${lines.join('\n')}`
}
</script>

<style scoped>
.goods-audit-container {
  padding: 20px;
}
.card-header {
  font-weight: bold;
}
.search-form {
  margin-bottom: 20px;
}
.batch-actions {
  margin-bottom: 15px;
}
.pagination {
  margin-top: 20px;
  justify-content: flex-end;
}
</style>
