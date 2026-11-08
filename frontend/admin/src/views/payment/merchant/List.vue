<template>
  <div class="merchant-list-container">
    <el-card class="search-card" shadow="never">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="商户编号">
          <el-input v-model="searchForm.merchantNo" placeholder="请输入商户编号" clearable style="width: 160px;" />
        </el-form-item>
        <el-form-item label="商户名称">
          <el-input v-model="searchForm.merchantName" placeholder="请输入商户名称" clearable style="width: 180px;" />
        </el-form-item>
        <el-form-item label="经营状态">
          <el-select v-model="searchForm.status" placeholder="请选择" clearable style="width: 120px;">
            <el-option label="正常" value="active" />
            <el-option label="已暂停" value="paused" />
            <el-option label="已关闭" value="closed" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch"><el-icon><Search /></el-icon>搜索</el-button>
          <el-button @click="handleReset"><el-icon><Refresh /></el-icon>重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card shadow="never" class="table-card">
      <template #header>
        <div class="card-header">
          <span class="card-title">商户列表</span>
          <el-button @click="handleExport"><el-icon><Download /></el-icon>导出</el-button>
        </div>
      </template>

      <el-table :data="tableData" v-loading="loading" stripe style="width: 100%;">
        <el-table-column prop="merchantNo" label="商户编号" width="160" />
        <el-table-column prop="merchantName" label="商户名称" min-width="200" />
        <el-table-column prop="category" label="行业分类" width="140" />
        <el-table-column prop="channelCount" label="开通渠道数" width="110" align="right" />
        <el-table-column prop="status" label="经营状态" width="100">
          <template #default="{ row }">
            <el-tag size="small" :type="statusTagType[row.status]">{{ statusText[row.status] || row.status }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="入驻时间" width="180" />
        <el-table-column label="操作" width="160" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click="handleDetail(row)">详情</el-button>
            <el-button link type="warning" size="small" v-if="row.status === 'active'" @click="handlePause(row)">暂停</el-button>
            <el-button link type="success" size="small" v-if="row.status === 'paused'" @click="handleResume(row)">恢复</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-wrap">
        <el-pagination v-model:current-page="pagination.page" v-model:page-size="pagination.size" :page-sizes="[10, 20, 50, 100]" :total="pagination.total" layout="total, sizes, prev, pager, next, jumper" background @size-change="fetchList" @current-change="fetchList" />
      </div>
    </el-card>

    <el-dialog v-model="detailDialogVisible" title="商户详情" width="700px">
      <el-descriptions v-if="currentRow" :column="2" border>
        <el-descriptions-item label="商户编号">{{ currentRow.merchantNo }}</el-descriptions-item>
        <el-descriptions-item label="商户名称">{{ currentRow.merchantName }}</el-descriptions-item>
        <el-descriptions-item label="行业分类">{{ currentRow.category }}</el-descriptions-item>
        <el-descriptions-item label="开通渠道数">{{ currentRow.channelCount }}</el-descriptions-item>
        <el-descriptions-item label="经营状态">
          <el-tag size="small" :type="statusTagType[currentRow.status]">{{ statusText[currentRow.status] }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="联系人">{{ currentRow.contactName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="联系电话">{{ currentRow.contactMobile || '-' }}</el-descriptions-item>
        <el-descriptions-item label="入驻时间">{{ currentRow.createTime }}</el-descriptions-item>
      </el-descriptions>
      <template #footer>
        <el-button @click="detailDialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
/**
 * 商户列表页面
 * 查看已入驻支付中心的商户信息与经营状态
 */
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Refresh, Download } from '@element-plus/icons-vue'
import { payApi } from '@/api/payment/pay'
import request from '@/utils/request'

const statusText = { active: '正常', paused: '已暂停', closed: '已关闭' }
const statusTagType = { active: 'success', paused: 'warning', closed: 'info' }

const loading = ref(false)
const detailDialogVisible = ref(false)
const currentRow = ref(null)
const tableData = ref([])

const searchForm = reactive({ merchantNo: '', merchantName: '', status: '' })
const pagination = reactive({ page: 1, size: 10, total: 0 })

const fetchList = async () => {
  loading.value = true
  try {
    const params = { pageNum: pagination.page, pageSize: pagination.size, merchantNo: searchForm.merchantNo || undefined, merchantName: searchForm.merchantName || undefined, status: searchForm.status || undefined }
    const res = await payApi.queryMchList(params)
    if (res && res.code === 200) { tableData.value = res.data?.records || res.data?.list || []; pagination.total = res.data?.total || 0 }
  } catch (e) { console.error('查询商户列表失败:', e) } finally { loading.value = false }
}

const handleSearch = () => { pagination.page = 1; fetchList() }
const handleReset = () => { Object.assign(searchForm, { merchantNo: '', merchantName: '', status: '' }); handleSearch() }
const handleDetail = (row) => { currentRow.value = row; detailDialogVisible.value = true }
const handleExport = () => { ElMessage.info('导出功能开发中') }

const handlePause = (row) => {
  ElMessageBox.confirm(`确认暂停商户「${row.merchantName}」？`, '暂停商户', { type: 'warning' }).then(async () => {
    try {
      const res = await request.post('/web/pay/merchant/changeStatus', { mchNo: row.mchNo || row.merchantNo, status: 2 })
      if (res && res.code === 200) {
        ElMessage.success('商户已暂停')
        fetchList()
      } else {
        ElMessage.error(res?.message || '暂停商户失败')
      }
    } catch (e) {
      ElMessage.error('暂停商户失败: ' + (e.message || '网络错误'))
    }
  }).catch(() => {})
}

const handleResume = (row) => {
  ElMessageBox.confirm(`确认恢复商户「${row.merchantName}」经营？`, '恢复商户', { type: 'info' }).then(async () => {
    try {
      const res = await request.post('/web/pay/merchant/changeStatus', { mchNo: row.mchNo || row.merchantNo, status: 1 })
      if (res && res.code === 200) {
        ElMessage.success('商户已恢复')
        fetchList()
      } else {
        ElMessage.error(res?.message || '恢复商户失败')
      }
    } catch (e) {
      ElMessage.error('恢复商户失败: ' + (e.message || '网络错误'))
    }
  }).catch(() => {})
}

onMounted(() => { fetchList() })
</script>

<style scoped>
.merchant-list-container { padding: 16px; }
.search-card { margin-bottom: 12px; border-radius: 8px; }
.search-card :deep(.el-card__body) { padding: 14px 16px 0 16px; }
.search-form :deep(.el-form-item) { margin-bottom: 14px; }
.table-card { border-radius: 8px; }
.table-card :deep(.el-card__header) { padding: 12px 16px; }
.card-header { display: flex; align-items: center; justify-content: space-between; }
.card-title { font-size: 15px; font-weight: 600; color: #303133; }
.pagination-wrap { display: flex; justify-content: flex-end; margin-top: 12px; }
</style>
