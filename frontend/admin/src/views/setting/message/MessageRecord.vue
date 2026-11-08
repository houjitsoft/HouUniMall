<template>
  <main class="message-record">
    <header class="record-header">
      <div>
        <h1>投递记录</h1>
        <p>按业务事件和投递结果定位通知问题</p>
      </div>
      <el-button :icon="RefreshRight" :loading="refreshing" @click="refreshRecordData">
        刷新
      </el-button>
    </header>

    <el-tabs v-model="activeChannel" class="channel-tabs" @tab-change="handleChannelChange">
      <el-tab-pane name="">
        <template #label>
          <span class="tab-label"><el-icon><List /></el-icon>全部</span>
        </template>
      </el-tab-pane>
      <el-tab-pane name="INBOX">
        <template #label>
          <span class="tab-label"><el-icon><Message /></el-icon>站内信投递</span>
        </template>
      </el-tab-pane>
      <el-tab-pane v-for="channel in externalChannels" :key="channel.value" :name="channel.value">
        <template #label>
          <span class="tab-label"><el-icon><Message /></el-icon>{{ channel.label }}</span>
        </template>
      </el-tab-pane>
    </el-tabs>

    <section class="stats-grid" aria-label="投递统计">
      <el-card v-for="item in statItems" :key="item.key" shadow="never" class="stat-card" :class="item.key">
        <div class="stat-value">{{ item.value }}</div>
        <div class="stat-label">{{ item.label }}</div>
      </el-card>
    </section>

    <form class="record-filters" @submit.prevent="handleSearch">
      <label class="filter-field">
        <span>业务类型</span>
        <el-select v-model="searchForm.bizType" placeholder="全部业务" clearable>
          <el-option label="项目管理（全部）" value="PM" />
          <el-option label="任务" value="PM_TASK" />
          <el-option label="项目" value="PM_PROJECT" />
        </el-select>
      </label>
      <label class="filter-field">
        <span>事件类型</span>
        <el-select v-model="searchForm.eventType" placeholder="全部事件" clearable>
          <el-option v-for="event in eventTypes" :key="event.value" :label="event.label" :value="event.value" />
        </el-select>
      </label>
      <label class="filter-field">
        <span>发送状态</span>
        <el-select v-model="searchForm.sendStatus" placeholder="全部状态" clearable>
          <el-option label="待发送" :value="0" />
          <el-option label="发送中" :value="1" />
          <el-option label="成功" :value="2" />
          <el-option label="失败" :value="3" />
        </el-select>
      </label>
      <label class="filter-field">
        <span>接收人</span>
        <el-input v-model="searchForm.receiver" placeholder="姓名或联系方式" clearable />
      </label>
      <label class="filter-field date-filter">
        <span>创建时间</span>
        <el-date-picker
          v-model="searchForm.dateRange"
          type="daterange"
          range-separator="至"
          start-placeholder="开始日期"
          end-placeholder="结束日期"
          :default-time="defaultDateTimes"
          clearable
        />
      </label>
      <div class="filter-actions">
        <el-button :icon="RefreshRight" @click="handleReset">重置</el-button>
        <el-button native-type="submit" type="primary" :icon="Search">查询</el-button>
      </div>
    </form>

    <div v-if="listError" class="record-error" role="alert">
      <span>{{ listError }}</span>
      <el-button :icon="RefreshRight" text @click="refreshRecordData">重试</el-button>
    </div>

    <el-table v-else :data="recordList" v-loading="loading" border stripe class="record-table">
      <el-table-column prop="id" label="ID" width="88" />
      <el-table-column prop="bizType" label="业务类型" width="120" />
      <el-table-column prop="eventType" label="事件类型" min-width="160" show-overflow-tooltip />
      <el-table-column prop="channel" label="渠道" width="110">
        <template #default="{ row }">
          <el-tag :type="getChannelTagType(row.channel)" size="small">{{ getChannelName(row.channel) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="receiverName" label="接收人" width="120" show-overflow-tooltip />
      <el-table-column prop="title" label="消息标题" min-width="200" show-overflow-tooltip />
      <el-table-column label="发送状态" width="100" align="center">
        <template #default="{ row }">
          <el-tag :type="getStatusType(row.sendStatus)" size="small">{{ getStatusText(row.sendStatus) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="retryCount" label="重试" width="70" align="center" />
      <el-table-column label="发送时间" width="170">
        <template #default="{ row }">{{ formatTime(row.sendTime || row.createTime) }}</template>
      </el-table-column>
      <el-table-column label="操作" width="140" fixed="right">
        <template #default="{ row }">
          <el-button
            type="primary"
            link
            size="small"
            :icon="View"
            :data-testid="`record-detail-${row.id}`"
            @click="handleDetail(row)"
          >
            详情
          </el-button>
          <el-button
            v-if="row.sendStatus === 3"
            type="warning"
            link
            size="small"
            :icon="RefreshRight"
            :data-testid="`record-retry-${row.id}`"
            :loading="row.retrying"
            @click="handleRetry(row)"
          >
            重试
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-pagination
      v-model:current-page="pagination.page"
      v-model:page-size="pagination.size"
      :total="pagination.total"
      layout="total, sizes, prev, pager, next, jumper"
      :page-sizes="[10, 20, 50, 100]"
      class="pagination"
      @size-change="handlePageChange"
      @current-change="handlePageChange"
    />

    <el-dialog v-model="detailVisible" title="投递诊断" width="min(650px, 92vw)">
      <el-skeleton v-if="detailLoading" :rows="6" animated />
      <el-descriptions v-else-if="currentRecord" :column="2" border>
        <el-descriptions-item label="记录 ID">{{ currentRecord.id }}</el-descriptions-item>
        <el-descriptions-item label="发送渠道">
          <el-tag :type="getChannelTagType(currentRecord.channel)" size="small">
            {{ getChannelName(currentRecord.channel) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="事件类型">{{ currentRecord.eventType || '-' }}</el-descriptions-item>
        <el-descriptions-item label="业务类型">{{ currentRecord.bizType || '-' }}</el-descriptions-item>
        <el-descriptions-item label="业务编号">{{ currentRecord.bizId || '-' }}</el-descriptions-item>
        <el-descriptions-item label="幂等键摘要">
          <code>{{ currentRecord.idempotencyKeyDigest || '-' }}</code>
        </el-descriptions-item>
        <el-descriptions-item label="发送状态">
          <el-tag :type="getStatusType(currentRecord.sendStatus)" size="small">
            {{ getStatusText(currentRecord.sendStatus) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="重试次数">{{ currentRecord.retryCount || 0 }}</el-descriptions-item>
        <el-descriptions-item label="失败码">{{ currentRecord.errorCode || '-' }}</el-descriptions-item>
        <el-descriptions-item label="失败原因">{{ currentRecord.failReason || '-' }}</el-descriptions-item>
        <el-descriptions-item label="创建时间">{{ formatTime(currentRecord.createTime) }}</el-descriptions-item>
        <el-descriptions-item label="发送时间">{{ formatTime(currentRecord.sendTime) }}</el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </main>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { List, Message, RefreshRight, Search, View } from '@element-plus/icons-vue'
import { recordApi } from '@/api/setting/message'

const route = useRoute()
const supportedBizTypes = new Set(['PM', 'PM_TASK', 'PM_PROJECT'])
const routeBizType = Array.isArray(route.query.bizType) ? route.query.bizType[0] : route.query.bizType

const loading = ref(false)
const refreshing = ref(false)
const detailLoading = ref(false)
const detailVisible = ref(false)
const currentRecord = ref(null)
const listError = ref('')
const activeChannel = ref('')
const recordList = ref([])

const stats = reactive({ total: 0, success: 0, failed: 0, pending: 0 })
const searchForm = reactive({
  bizType: supportedBizTypes.has(routeBizType) ? routeBizType : '',
  eventType: '',
  sendStatus: null,
  receiver: '',
  dateRange: []
})
const pagination = reactive({ page: 1, size: 20, total: 0 })

const defaultDateTimes = [
  new Date(2000, 0, 1, 0, 0, 0),
  new Date(2000, 0, 1, 23, 59, 59)
]
const externalChannels = [
  { label: '邮件', value: 'EMAIL' },
  { label: '短信', value: 'SMS' },
  { label: '钉钉', value: 'DINGTALK' },
  { label: '企微', value: 'WECOM' },
  { label: '微信', value: 'WECHAT' },
  { label: '飞书', value: 'FEISHU' }
]
const eventTypes = [
  { label: '任务创建', value: 'TASK_CREATED' },
  { label: '任务分配', value: 'TASK_ASSIGNED' },
  { label: '任务提及', value: 'TASK_MENTIONED' },
  { label: '任务完成', value: 'TASK_COMPLETED' },
  { label: '任务逾期', value: 'TASK_OVERDUE' },
  { label: '项目更新', value: 'PROJECT_UPDATED' }
]
const statItems = computed(() => [
  { key: 'total', label: '总投递数', value: stats.total },
  { key: 'success', label: '发送成功', value: stats.success },
  { key: 'failed', label: '发送失败', value: stats.failed },
  { key: 'pending', label: '待发送', value: stats.pending }
])

const channelMap = {
  INBOX: '站内信投递', EMAIL: '邮件', SMS: '短信', DINGTALK: '钉钉',
  WECOM: '企微', WECHAT: '微信', FEISHU: '飞书'
}
const channelTagTypes = {
  INBOX: '', EMAIL: 'success', SMS: 'warning', DINGTALK: 'primary',
  WECOM: 'success', WECHAT: 'success', FEISHU: 'info'
}

const isSuccessful = (response) => [0, 1, 200].includes(response?.code)
const getChannelName = (code) => channelMap[code] || code || '-'
const getChannelTagType = (code) => channelTagTypes[code] || 'info'
const getStatusType = (status) => ({ 0: 'info', 1: 'warning', 2: 'success', 3: 'danger' })[status] || 'info'
const getStatusText = (status) => ({ 0: '待发送', 1: '发送中', 2: '成功', 3: '失败' })[status] || '未知'

const formatTime = (timestamp) => {
  if (!timestamp) return '-'
  const numeric = Number(timestamp)
  const milliseconds = numeric < 10000000000 ? numeric * 1000 : numeric
  return new Date(milliseconds).toLocaleString('zh-CN')
}

const toEpochSeconds = (value) => {
  const milliseconds = value instanceof Date ? value.getTime() : new Date(value).getTime()
  return Number.isFinite(milliseconds) ? Math.floor(milliseconds / 1000) : null
}

const buildRecordFilter = () => {
  const filter = { page: pagination.page, size: pagination.size }
  if (searchForm.bizType) filter.bizType = searchForm.bizType
  if (searchForm.eventType) filter.eventType = searchForm.eventType
  if (activeChannel.value) filter.channel = activeChannel.value
  if (searchForm.sendStatus !== null && searchForm.sendStatus !== '') filter.sendStatus = searchForm.sendStatus
  if (searchForm.receiver.trim()) filter.receiver = searchForm.receiver.trim()
  if (Array.isArray(searchForm.dateRange) && searchForm.dateRange.length === 2) {
    const startTime = toEpochSeconds(searchForm.dateRange[0])
    const endTime = toEpochSeconds(searchForm.dateRange[1])
    if (startTime !== null) filter.startTime = startTime
    if (endTime !== null) filter.endTime = endTime
  }
  return filter
}

const fetchRecordList = async (filter = buildRecordFilter()) => {
  loading.value = true
  listError.value = ''
  try {
    const response = await recordApi.getList(filter)
    if (!isSuccessful(response)) throw new Error(response?.msg || '获取投递记录失败')
    const data = response.data || {}
    recordList.value = data.records || data.list || []
    pagination.total = Number(data.total || 0)
    pagination.page = Number(data.page || filter.page || 1)
    pagination.size = Number(data.size || filter.size || 20)
  } catch (error) {
    listError.value = error?.message || '获取投递记录失败'
    ElMessage.error(listError.value)
  } finally {
    loading.value = false
  }
}

const fetchStatistics = async (filter = buildRecordFilter()) => {
  try {
    const response = await recordApi.getStatistics(filter)
    if (!isSuccessful(response)) throw new Error(response?.msg || '获取投递统计失败')
    const data = response.data || {}
    stats.total = Number(data.total || 0)
    stats.success = Number(data.success || 0)
    stats.failed = Number(data.failed || 0)
    stats.pending = Number(data.pending || 0)
  } catch (error) {
    ElMessage.error(error?.message || '获取投递统计失败')
  }
}

const refreshRecordData = async () => {
  refreshing.value = true
  const filter = buildRecordFilter()
  try {
    await Promise.all([fetchRecordList(filter), fetchStatistics(filter)])
  } finally {
    refreshing.value = false
  }
}

const handleChannelChange = (channel) => {
  activeChannel.value = String(channel || '')
  pagination.page = 1
  refreshRecordData()
}

const handleSearch = () => {
  pagination.page = 1
  refreshRecordData()
}

const handleReset = () => {
  searchForm.bizType = supportedBizTypes.has(routeBizType) ? routeBizType : ''
  searchForm.eventType = ''
  searchForm.sendStatus = null
  searchForm.receiver = ''
  searchForm.dateRange = []
  activeChannel.value = ''
  pagination.page = 1
  refreshRecordData()
}

const handlePageChange = () => fetchRecordList(buildRecordFilter())

const handleDetail = async (row) => {
  detailVisible.value = true
  detailLoading.value = true
  currentRecord.value = null
  try {
    const response = await recordApi.getById(row.id)
    if (!isSuccessful(response)) throw new Error(response?.msg || '获取投递详情失败')
    currentRecord.value = response.data || null
  } catch (error) {
    detailVisible.value = false
    ElMessage.error(error?.message || '获取投递详情失败')
  } finally {
    detailLoading.value = false
  }
}

const handleRetry = async (row) => {
  row.retrying = true
  try {
    const response = await recordApi.resend(row.id)
    if (!isSuccessful(response)) throw new Error(response?.msg || '重试失败')
    await refreshRecordData()
    if (detailVisible.value && currentRecord.value?.id === row.id) await handleDetail(row)
    if (response.data?.success === false) {
      ElMessage.error(response.data.errorMessage || '本次重试仍未成功')
    } else {
      ElMessage.success('重试已完成')
    }
  } catch (error) {
    ElMessage.error(error?.message || '重试失败')
  } finally {
    row.retrying = false
  }
}

onMounted(refreshRecordData)

defineExpose({
  activeChannel,
  buildRecordFilter,
  handleDetail,
  handleRetry,
  refreshRecordData,
  searchForm
})
</script>

<style scoped>
.message-record {
  min-height: 100%;
  padding: 24px;
  color: #17191f;
  background: #f7f7f8;
  font-family: "Helvetica Neue", Arial, sans-serif;
}

.record-header,
.channel-tabs,
.stats-grid,
.record-filters,
.record-table,
.record-error,
.pagination {
  max-width: 1440px;
  margin-right: auto;
  margin-left: auto;
}

.record-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 20px;
}

.record-header h1 {
  margin: 0;
  font-size: 24px;
  line-height: 1.2;
  letter-spacing: 0;
}

.record-header p {
  margin: 7px 0 0;
  color: #626a78;
  font-size: 13px;
}

.channel-tabs {
  margin-bottom: 16px;
  background: #fff;
  border: 1px solid #d9dce3;
  border-radius: 6px;
}

.channel-tabs :deep(.el-tabs__header) {
  margin: 0;
  padding: 0 16px;
}

.tab-label {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  letter-spacing: 0;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 12px;
  margin-bottom: 16px;
}

.stat-card {
  border: 1px solid #d9dce3;
  border-radius: 6px;
}

.stat-card :deep(.el-card__body) {
  padding: 18px 20px;
}

.stat-value {
  color: #002fa7;
  font-size: 28px;
  font-weight: 700;
  font-variant-numeric: tabular-nums;
}

.stat-card.success .stat-value { color: #16794b; }
.stat-card.failed .stat-value { color: #e4002b; }
.stat-card.pending .stat-value { color: #9a6700; }

.stat-label {
  margin-top: 4px;
  color: #626a78;
  font-size: 13px;
}

.record-filters {
  display: grid;
  grid-template-columns: repeat(4, minmax(150px, 1fr));
  gap: 14px;
  margin-bottom: 16px;
  padding: 16px;
  background: #fff;
  border: 1px solid #d9dce3;
  border-radius: 6px;
}

.filter-field {
  min-width: 0;
  display: grid;
  gap: 7px;
  color: #444b57;
  font-size: 12px;
}

.filter-field :deep(.el-select),
.filter-field :deep(.el-date-editor) {
  width: 100%;
}

.date-filter {
  grid-column: span 2;
}

.filter-actions {
  grid-column: span 2;
  display: flex;
  align-items: flex-end;
  justify-content: flex-end;
  gap: 8px;
}

.record-table {
  width: 100%;
}

.record-error {
  min-height: 160px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 10px;
  color: #b42318;
  background: #fff;
  border: 1px solid #d9dce3;
  font-size: 13px;
}

.pagination {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}

code {
  overflow-wrap: anywhere;
  font-family: ui-monospace, SFMono-Regular, Menlo, Consolas, monospace;
  font-size: 12px;
}

@media (max-width: 900px) {
  .stats-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .record-filters {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 600px) {
  .message-record {
    padding: 14px;
  }

  .record-header h1 {
    font-size: 20px;
  }

  .channel-tabs {
    overflow-x: auto;
  }

  .channel-tabs :deep(.el-tabs__nav-wrap) {
    min-width: max-content;
  }

  .record-filters {
    grid-template-columns: minmax(0, 1fr);
    padding: 14px;
  }

  .date-filter,
  .filter-actions {
    grid-column: span 1;
  }

  .filter-actions {
    justify-content: stretch;
  }

  .filter-actions :deep(.el-button) {
    flex: 1;
  }

  .pagination {
    overflow-x: auto;
    justify-content: flex-start;
    padding-bottom: 4px;
  }
}
</style>
