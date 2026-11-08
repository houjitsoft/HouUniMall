<template>
  <div class="payment-reconciliation-container">
    <!-- 统计卡片 -->
    <el-row :gutter="20" class="stats-cards">
      <el-col :span="6">
        <el-card class="stat-card total-reconcile">
          <div class="stat-content">
            <div class="stat-icon"><el-icon><Check /></el-icon></div>
            <div class="stat-info">
              <div class="stat-value">{{ reconcileStats.total }}</div>
              <div class="stat-label">对账总数</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card matched-reconcile">
          <div class="stat-content">
            <div class="stat-icon"><el-icon><CircleCheck /></el-icon></div>
            <div class="stat-info">
              <div class="stat-value">{{ reconcileStats.matched }}</div>
              <div class="stat-label">对账一致</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card unmatched-reconcile">
          <div class="stat-content">
            <div class="stat-icon"><el-icon><Warning /></el-icon></div>
            <div class="stat-info">
              <div class="stat-value">{{ reconcileStats.unmatched }}</div>
              <div class="stat-label">对账差异</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card amount-diff">
          <div class="stat-content">
            <div class="stat-icon"><el-icon><Money /></el-icon></div>
            <div class="stat-info">
              <div class="stat-value">¥{{ reconcileStats.amountDiff }}</div>
              <div class="stat-label">金额差异</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 搜索筛选 -->
    <el-card class="search-card">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="对账日期">
          <el-date-picker
            v-model="searchForm.reconcileDate"
            type="date"
            placeholder="选择对账日期"
            clearable
          />
        </el-form-item>
        <el-form-item label="支付渠道">
          <el-select v-model="searchForm.channel" placeholder="请选择支付渠道" clearable>
            <el-option label="支付宝" value="alipay" />
            <el-option label="微信支付" value="wechat" />
            <el-option label="银行卡" value="bank" />
            <el-option label="余额支付" value="balance" />
          </el-select>
        </el-form-item>
        <el-form-item label="对账状态">
          <el-select v-model="searchForm.status" placeholder="请选择状态" clearable>
            <el-option label="待对账" value="pending" />
            <el-option label="对账中" value="processing" />
            <el-option label="对账成功" value="success" />
            <el-option label="对账失败" value="failed" />
            <el-option label="有差异" value="difference" />
          </el-select>
        </el-form-item>
        <el-form-item label="对账类型">
          <el-select v-model="searchForm.type" placeholder="请选择对账类型" clearable>
            <el-option label="自动对账" value="auto" />
            <el-option label="手动对账" value="manual" />
            <el-option label="批量对账" value="batch" />
            <el-option label="补充对账" value="supplement" />
          </el-select>
        </el-form-item>
        <el-form-item label="时间范围">
          <el-date-picker
            v-model="searchForm.dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
          />
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
          <el-button type="primary" @click="handleStartReconcile">
            <el-icon><Check /></el-icon>开始对账
          </el-button>
          <el-button type="success" @click="handleBatchReconcile">
            <el-icon><Operation /></el-icon>批量对账
          </el-button>
          <el-button type="warning" @click="handleDownloadStatement">
            <el-icon><Download /></el-icon>下载对账单
          </el-button>
          <el-button type="info" @click="handleExport">
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
      <el-table
        :data="reconcileList"
        v-loading="loading"
        style="width: 100%"
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="55" />
        <el-table-column prop="reconcileId" label="对账单号" width="180" />
        <el-table-column prop="reconcileDate" label="对账日期" width="120" />
        <el-table-column prop="channel" label="支付渠道" width="100">
          <template #default="{ row }">
            <el-tag :type="getChannelColor(row.channel)">
              {{ getChannelText(row.channel) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="type" label="对账类型" width="100">
          <template #default="{ row }">
            <el-tag :type="getTypeColor(row.type)">
              {{ getTypeText(row.type) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="totalCount" label="交易总笔数" width="120" />
        <el-table-column prop="totalAmount" label="交易总金额" width="120">
          <template #default="{ row }">
            ¥{{ row.totalAmount }}
          </template>
        </el-table-column>
        <el-table-column prop="matchedCount" label="匹配笔数" width="100" />
        <el-table-column prop="unmatchedCount" label="差异笔数" width="100">
          <template #default="{ row }">
            <span v-if="row.unmatchedCount > 0" class="unmatched-count">{{ row.unmatchedCount }}</span>
            <span v-else>{{ row.unmatchedCount }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="amountDiff" label="金额差异" width="120">
          <template #default="{ row }">
            <span v-if="row.amountDiff !== '0.00'" class="amount-diff">¥{{ row.amountDiff }}</span>
            <span v-else>¥{{ row.amountDiff }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="对账状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="180" />
        <el-table-column label="操作" width="250" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="handleView(row)">
              <el-icon><View /></el-icon>查看
            </el-button>
            <el-button 
              v-if="row.status === 'pending'"
              link 
              type="success" 
              @click="handleProcess(row)"
            >
              <el-icon><Operation /></el-icon>处理
            </el-button>
            <el-button 
              v-if="row.unmatchedCount > 0"
              link 
              type="warning" 
              @click="handleViewDifference(row)"
            >
              <el-icon><Warning /></el-icon>查看差异
            </el-button>
            <el-button link type="info" @click="handleDownload(row)">
              <el-icon><Download /></el-icon>下载
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

    <!-- 详情对话框 -->
    <el-dialog v-model="dialogVisible" title="对账详情" width="1000px">
      <div v-if="currentReconcile" class="reconcile-detail">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-descriptions title="对账信息" :column="1" border>
              <el-descriptions-item label="对账单号">{{ currentReconcile.reconcileId }}</el-descriptions-item>
              <el-descriptions-item label="对账日期">{{ currentReconcile.reconcileDate }}</el-descriptions-item>
              <el-descriptions-item label="支付渠道">
                <el-tag :type="getChannelColor(currentReconcile.channel)">
                  {{ getChannelText(currentReconcile.channel) }}
                </el-tag>
              </el-descriptions-item>
              <el-descriptions-item label="对账类型">
                <el-tag :type="getTypeColor(currentReconcile.type)">
                  {{ getTypeText(currentReconcile.type) }}
                </el-tag>
              </el-descriptions-item>
              <el-descriptions-item label="对账状态">
                <el-tag :type="getStatusType(currentReconcile.status)">
                  {{ getStatusText(currentReconcile.status) }}
                </el-tag>
              </el-descriptions-item>
            </el-descriptions>
          </el-col>
          <el-col :span="12">
            <el-descriptions title="统计信息" :column="1" border>
              <el-descriptions-item label="交易总笔数">{{ currentReconcile.totalCount }}</el-descriptions-item>
              <el-descriptions-item label="交易总金额">¥{{ currentReconcile.totalAmount }}</el-descriptions-item>
              <el-descriptions-item label="匹配笔数">{{ currentReconcile.matchedCount }}</el-descriptions-item>
              <el-descriptions-item label="差异笔数">
                <span v-if="currentReconcile.unmatchedCount > 0" class="unmatched-count">{{ currentReconcile.unmatchedCount }}</span>
                <span v-else>{{ currentReconcile.unmatchedCount }}</span>
              </el-descriptions-item>
              <el-descriptions-item label="金额差异">
                <span v-if="currentReconcile.amountDiff !== '0.00'" class="amount-diff">¥{{ currentReconcile.amountDiff }}</span>
                <span v-else>¥{{ currentReconcile.amountDiff }}</span>
              </el-descriptions-item>
            </el-descriptions>
          </el-col>
        </el-row>
        <div class="reconcile-detail-remark" v-if="currentReconcile.remark">
          <h4>对账说明</h4>
          <p>{{ currentReconcile.remark }}</p>
        </div>
        <div class="reconcile-progress" v-if="currentReconcile.progressInfo">
          <h4>对账进度</h4>
          <el-progress 
            :percentage="currentReconcile.progressInfo.percentage" 
            :status="currentReconcile.progressInfo.status"
          />
          <p>{{ currentReconcile.progressInfo.description }}</p>
        </div>
      </div>
      <template #footer>
        <el-button @click="dialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>

    <!-- 差异详情对话框 -->
    <el-dialog v-model="differenceDialogVisible" title="对账差异详情" width="1200px">
      <div v-if="differenceData" class="difference-detail">
        <el-table :data="differenceData" style="width: 100%">
          <el-table-column prop="transactionId" label="交易流水号" width="180" />
          <el-table-column prop="orderNo" label="订单号" width="150" />
          <el-table-column prop="ourAmount" label="我方金额" width="100">
            <template #default="{ row }">
              ¥{{ row.ourAmount }}
            </template>
          </el-table-column>
          <el-table-column prop="thirdPartyAmount" label="第三方金额" width="100">
            <template #default="{ row }">
              ¥{{ row.thirdPartyAmount }}
            </template>
          </el-table-column>
          <el-table-column prop="diffAmount" label="差异金额" width="100">
            <template #default="{ row }">
              <span class="amount-diff">¥{{ row.diffAmount }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="diffType" label="差异类型" width="120">
            <template #default="{ row }">
              <el-tag :type="getDiffTypeColor(row.diffType)">
                {{ getDiffTypeText(row.diffType) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="transactionTime" label="交易时间" width="180" />
          <el-table-column prop="remark" label="差异说明" min-width="200" />
        </el-table>
      </div>
      <template #footer>
        <el-button @click="differenceDialogVisible = false">关闭</el-button>
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
  Check, CircleCheck, Warning, Money, Search, Refresh, Operation, Download,
  View
} from '@element-plus/icons-vue'
import request from '@/utils/request'

// 响应式数据
const loading = ref(false)
const dialogVisible = ref(false)
const differenceDialogVisible = ref(false)
const reconcileList = ref([])
const selectedReconciles = ref([])
const currentReconcile = ref(null)
const differenceData = ref([])

// 统计数据
const reconcileStats = ref({
  total: 0,
  matched: 0,
  unmatched: 0,
  amountDiff: '0.00'
})

// 搜索表单
const searchForm = reactive({
  reconcileDate: null,
  channel: '',
  status: '',
  type: '',
  dateRange: null
})

// 分页信息
const pagination = reactive({
  page: 1,
  size: 10,
  total: 0
})

// 支付渠道映射
const getChannelText = (channel) => {
  const map = {
    'alipay': '支付宝',
    'wechat': '微信支付',
    'bank': '银行卡',
    'balance': '余额支付'
  }
  return map[channel] || channel
}

const getChannelColor = (channel) => {
  const map = {
    'alipay': 'primary',
    'wechat': 'success',
    'bank': 'warning',
    'balance': 'info'
  }
  return map[channel] || 'info'
}

// 对账类型映射
const getTypeText = (type) => {
  const map = {
    'auto': '自动对账',
    'manual': '手动对账',
    'batch': '批量对账',
    'supplement': '补充对账'
  }
  return map[type] || type
}

const getTypeColor = (type) => {
  const map = {
    'auto': 'success',
    'manual': 'primary',
    'batch': 'warning',
    'supplement': 'info'
  }
  return map[type] || 'info'
}

// 状态映射
const getStatusText = (status) => {
  const map = {
    'pending': '待对账',
    'processing': '对账中',
    'success': '对账成功',
    'failed': '对账失败',
    'difference': '有差异'
  }
  return map[status] || status
}

const getStatusType = (status) => {
  const map = {
    'pending': 'info',
    'processing': 'warning',
    'success': 'success',
    'failed': 'danger',
    'difference': 'warning'
  }
  return map[status] || 'info'
}

// 差异类型映射
const getDiffTypeText = (type) => {
  const map = {
    'amount': '金额差异',
    'status': '状态差异',
    'missing': '缺失记录',
    'extra': '多余记录'
  }
  return map[type] || type
}

const getDiffTypeColor = (type) => {
  const map = {
    'amount': 'danger',
    'status': 'warning',
    'missing': 'info',
    'extra': 'primary'
  }
  return map[type] || 'info'
}

// 获取对账列表（透传对账报表分页，走 hou_pay_recon_report 真实数据）
const getReconcileList = async () => {
  loading.value = true
  try {
    const params = {
      page: pagination.page,
      size: pagination.size,
      channel: searchForm.channel || undefined,
      status: searchForm.status || undefined,
      reportType: searchForm.type || undefined
    }
    const res = await request.post('/web/pay/recon/reportList', params)
    if (res.code === 200) {
      const data = res.data || {}
      const records = data.records || data.list || []
      reconcileList.value = records.map((r) => ({
        id: r.id,
        reconcileId: r.reportNo || r.id,
        reconcileDate: r.reportDate || r.statDate || r.statDay || '',
        channel: r.channelCode || '',
        type: r.reportType || r.billType || 'auto',
        totalCount: r.channelTotalCount ?? r.localTotalCount ?? 0,
        totalAmount: r.channelTotalAmount ?? r.localTotalAmount ?? 0,
        matchedCount: r.matchCount ?? r.diffCount ?? 0,
        unmatchedCount: r.diffCount ?? 0,
        amountDiff: r.diffAmount ?? '0.00',
        status: mapReconStatus(r.status),
        createTime: r.createTime || r.gmtCreate || '',
        remark: r.remark || r.reportDesc || '',
        progressInfo: mapReconStatus(r.status) === 'processing' ? {
          percentage: 50,
          status: 'active',
          description: '正在对账中...'
        } : null,
        _raw: r
      }))
      pagination.total = data.total || data.totalCount || 0
      let matched = 0
      let unmatched = 0
      let diffSum = 0
      records.forEach((r) => {
        const dn = r.diffCount ?? 0
        if (dn > 0) unmatched += dn
        else matched += 1
        diffSum += Number(r.diffAmount || 0)
      })
      reconcileStats.value = {
        total: data.total || data.totalCount || records.length,
        matched,
        unmatched,
        amountDiff: diffSum.toLocaleString('zh-CN', { minimumFractionDigits: 2 })
      }
    } else {
      reconcileList.value = []
      pagination.total = 0
    }
  } catch (e) {
    console.error('获取对账列表失败:', e)
    ElMessage.error('获取对账列表失败: ' + (e.message || '网络错误'))
    reconcileList.value = []
    pagination.total = 0
  } finally {
    loading.value = false
  }
}

// 对账状态映射
const mapReconStatus = (status) => {
  const s = String(status == null ? '' : status).toUpperCase()
  const map = {
    '0': 'pending',
    '1': 'processing',
    '2': 'success',
    '3': 'failed',
    'PENDING': 'pending',
    'PROCESSING': 'processing',
    'SUCCESS': 'success',
    'FAILED': 'failed',
    'DIFF': 'difference',
    'HAS_DIFF': 'difference',
    'MATCHED': 'success'
  }
  return map[s] || map[status] || 'success'
}

// 查看差异（透传对账差异分页）
const loadDifferences = async (row) => {
  const params = {
    page: 1,
    size: 20,
    reportId: row._raw?.id || row.id,
    reportNo: row._raw?.reportNo || row.reconcileId,
    channelCode: row._raw?.channelCode || row.channel
  }
  try {
    const res = await request.post('/web/pay/recon/diffList', params)
    if (res.code === 200) {
      const data = res.data || {}
      const records = data.records || data.list || []
      return records.map((r) => ({
        transactionId: r.tradeNo || r.localTradeNo || r.channelTradeNo || r.id,
        orderNo: r.localOrderNo || r.mallOrderNo || '',
        ourAmount: r.localAmount ?? 0,
        thirdPartyAmount: r.channelAmount ?? 0,
        diffAmount: r.diffAmount ?? 0,
        diffType: r.diffType || 'amount',
        transactionTime: r.tradeTime || r.createTime || '',
        remark: r.remark || r.reason || ''
      }))
    }
    return []
  } catch (e) {
    console.error('获取对账差异失败:', e)
    return []
  }
}

// 搜索
const handleSearch = () => {
  pagination.page = 1
  getReconcileList()
}

// 重置
const handleReset = () => {
  Object.assign(searchForm, {
    reconcileDate: null,
    channel: '',
    status: '',
    type: '',
    dateRange: null
  })
  handleSearch()
}

// 刷新
const handleRefresh = () => {
  getReconcileList()
}

// 开始对账（手动触发对账：拉取+核对）
const handleStartReconcile = async () => {
  try {
    const res = await request.post('/web/pay/recon/trigger', {})
    if (res.code === 200) {
      ElMessage.success('已触发对账任务')
      getReconcileList()
    } else {
      ElMessage.error(res.message || '触发对账失败')
    }
  } catch (e) {
    ElMessage.error('触发对账失败: ' + (e.message || '网络错误'))
  }
}

// 批量对账
const handleBatchReconcile = () => {
  if (selectedReconciles.value.length === 0) {
    ElMessage.warning('请选择要对账的记录')
    return
  }
  handleStartReconcile()
}

// 下载对账单
const handleDownloadStatement = () => {
  ElMessage.info('请前往「对账管理 → 对账文件下载」下载渠道对账单')
}

// 导出
const handleExport = () => {
  ElMessage.info('导出功能开发中')
}

// 查看详情
const handleView = (row) => {
  currentReconcile.value = row
  dialogVisible.value = true
}

// 处理
const handleProcess = (row) => {
  ElMessage.info('请前往「对账管理 → 对账差异处理」处理差异')
}

// 查看差异
const handleViewDifference = async (row) => {
  const list = await loadDifferences(row)
  differenceData.value = list
  differenceDialogVisible.value = true
}

// 下载
const handleDownload = (row) => {
  ElMessage.info('请前往「对账管理 → 对账文件下载」下载')
}

// 选择变化
const handleSelectionChange = (selection) => {
  selectedReconciles.value = selection
}

// 分页大小变化
const handleSizeChange = (size) => {
  pagination.size = size
  getReconcileList()
}

// 页码变化
const handleCurrentChange = (page) => {
  pagination.page = page
  getReconcileList()
}

// 组件挂载
onMounted(() => {
  getReconcileList()
})
</script>

<style scoped>
.payment-reconciliation-container {
  padding: 20px;
}

.stats-cards {
  margin-bottom: 20px;
}

.stat-card {
  border-left: 4px solid #409eff;
}

.stat-card.total-reconcile {
  border-left-color: #409eff;
}

.stat-card.matched-reconcile {
  border-left-color: #67c23a;
}

.stat-card.unmatched-reconcile {
  border-left-color: #e6a23c;
}

.stat-card.amount-diff {
  border-left-color: #f56c6c;
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

.unmatched-count {
  color: #e6a23c;
  font-weight: bold;
}

.amount-diff {
  color: #f56c6c;
  font-weight: bold;
}

.reconcile-detail-remark {
  margin-top: 20px;
}

.reconcile-detail-remark h4 {
  margin-bottom: 10px;
  color: #303133;
}

.reconcile-detail-remark p {
  color: #606266;
  line-height: 1.5;
}

.reconcile-progress {
  margin-top: 20px;
}

.reconcile-progress h4 {
  margin-bottom: 15px;
  color: #303133;
}

.reconcile-progress p {
  margin-top: 10px;
  color: #606266;
}

.difference-detail {
  max-height: 500px;
  overflow-y: auto;
}
</style>
