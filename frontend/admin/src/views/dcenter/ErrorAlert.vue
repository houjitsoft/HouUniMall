<template>
  <div class="error-alert">
    <el-card class="box-card">
      <template #header>
        <div class="card-header">
          <span>运行告警</span>
          <div class="header-actions">
            <el-button :loading="loading" @click="loadData">刷新</el-button>
          </div>
        </div>
      </template>

      <!-- 搜索栏 -->
      <el-form :inline="true" :model="search" class="search-form">
        <el-form-item label="状态">
          <el-select v-model="search.status" placeholder="请选择状态" style="width: 140px">
            <el-option label="未处理" :value="0" />
            <el-option label="已处理" :value="1" />
            <el-option label="已忽略" :value="2" />
          </el-select>
        </el-form-item>
        <el-form-item label="模块">
          <el-input v-model="search.module" placeholder="如 order、goods" clearable style="width: 160px" />
        </el-form-item>
        <el-form-item label="关键词">
          <el-input v-model="search.keyword" placeholder="错误消息 / 异常类 / 接口" clearable style="width: 220px" />
        </el-form-item>
        <el-form-item label="时间范围">
          <el-date-picker
            v-model="search.dateRange"
            type="datetimerange"
            range-separator="至"
            start-placeholder="开始时间"
            end-placeholder="结束时间"
            format="YYYY-MM-DD HH:mm:ss"
            value-format="YYYY-MM-DD HH:mm:ss"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="resetSearch">重置</el-button>
        </el-form-item>
      </el-form>

      <!-- 告警列表 -->
      <el-table :data="list" style="width: 100%" v-loading="loading">
        <el-table-column prop="errorMessage" label="错误摘要" min-width="260" show-overflow-tooltip />
        <el-table-column prop="errorType" label="异常类" width="220" show-overflow-tooltip />
        <el-table-column prop="requestUrl" label="触发接口" min-width="200" show-overflow-tooltip>
          <template #default="scope">
            <span v-if="scope.row.requestUrl">
              <el-tag size="small" type="info">{{ scope.row.requestMethod || '-' }}</el-tag>
              {{ scope.row.requestUrl }}
            </span>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column prop="module" label="模块" width="110" />
        <el-table-column prop="username" label="最近触发用户" width="140">
          <template #default="scope">
            {{ scope.row.username || '未登录用户' }}
          </template>
        </el-table-column>
        <el-table-column prop="hitCount" label="次数" width="90" align="center">
          <template #default="scope">
            <el-tag v-if="scope.row.hitCount > 10" type="danger">{{ scope.row.hitCount }}</el-tag>
            <el-tag v-else-if="scope.row.hitCount > 1" type="warning">{{ scope.row.hitCount }}</el-tag>
            <span v-else>{{ scope.row.hitCount }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="firstTime" label="首次出现" width="170" />
        <el-table-column prop="lastTime" label="最近出现" width="170" />
        <el-table-column label="状态" width="90" align="center">
          <template #default="scope">
            <el-tag :type="statusType(scope.row.status)">{{ statusText(scope.row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="scope">
            <el-button size="small" @click="openDetail(scope.row)">详情</el-button>
            <el-button
              v-if="scope.row.status !== 1"
              size="small"
              type="success"
              @click="handleAlert(scope.row, 'handle')"
            >已修复</el-button>
            <el-button
              v-if="scope.row.status !== 2"
              size="small"
              type="info"
              @click="handleAlert(scope.row, 'ignore')"
            >忽略</el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination">
        <el-pagination
          v-model:current-page="page.current"
          v-model:page-size="page.size"
          :page-sizes="[20, 50, 100]"
          :total="page.total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="loadData"
          @current-change="loadData"
        />
      </div>
    </el-card>

    <!-- 详情弹窗 -->
    <el-dialog v-model="detailVisible" title="告警详情" width="70%" top="5vh">
      <el-descriptions :column="2" border v-if="detail">
        <el-descriptions-item label="异常类">{{ detail.errorType || '-' }}</el-descriptions-item>
        <el-descriptions-item label="模块">{{ detail.module || '-' }}</el-descriptions-item>
        <el-descriptions-item label="触发接口">
          {{ detail.requestMethod || '-' }} {{ detail.requestUrl || '-' }}
        </el-descriptions-item>
        <el-descriptions-item label="触发用户">
          {{ detail.username || '未登录用户' }}（{{ detail.userId || '-' }}）
        </el-descriptions-item>
        <el-descriptions-item label="客户端IP">{{ detail.ip || '-' }}</el-descriptions-item>
        <el-descriptions-item label="累计次数">{{ detail.hitCount }}</el-descriptions-item>
        <el-descriptions-item label="首次出现">{{ detail.firstTime || '-' }}</el-descriptions-item>
        <el-descriptions-item label="最近出现">{{ detail.lastTime || '-' }}</el-descriptions-item>
        <el-descriptions-item label="处理状态" :span="2">
          <el-tag :type="statusType(detail.status)">{{ statusText(detail.status) }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="错误消息" :span="2">
          <span class="message-text">{{ detail.errorMessage || '-' }}</span>
        </el-descriptions-item>
        <el-descriptions-item label="错误指纹" :span="2">
          <span class="fingerprint">{{ detail.fingerprint || '-' }}</span>
        </el-descriptions-item>
      </el-descriptions>

      <div class="stack-block" v-if="detail">
        <div class="stack-title">最近一次堆栈</div>
        <pre class="stack-text">{{ detail.stackTrace || '（无堆栈）' }}</pre>
      </div>

      <template #footer>
        <el-input
          v-model="remark"
          type="textarea"
          :rows="2"
          placeholder="填写修复原因，便于以后回溯（可选）"
          class="remark-input"
        />
        <div class="dialog-footer">
          <el-button @click="detailVisible = false">关闭</el-button>
          <el-button type="info" @click="submitIgnore">标为忽略</el-button>
          <el-button type="success" @click="submitHandle">标为已修复</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onUnmounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { errorAlertAPI } from '@/api/log/index.js'

// 列表数据
const loading = ref(false)
const list = ref([])
const page = reactive({ current: 1, size: 20, total: 0 })

// 查询条件：默认只看未处理
const search = reactive({
  status: 0,
  module: '',
  keyword: '',
  dateRange: []
})

// 详情
const detailVisible = ref(false)
const detail = ref(null)
const remark = ref('')

// 自动刷新句柄
let refreshTimer = null

/**
 * 加载告警列表
 */
async function loadData() {
  loading.value = true
  try {
    const params = {
      status: search.status,
      module: search.module || undefined,
      keyword: search.keyword || undefined
    }
    if (Array.isArray(search.dateRange) && search.dateRange.length === 2) {
      params.startTime = search.dateRange[0]
      params.endTime = search.dateRange[1]
    }
    const res = await errorAlertAPI.getPage(params, page.current, page.size)
    if (res && (res.code === 200 || res.code === 0)) {
      const data = res.data || {}
      list.value = data.records || []
      page.total = data.total || 0
    } else {
      list.value = []
      page.total = 0
    }
  } catch (e) {
    list.value = []
    page.total = 0
  } finally {
    loading.value = false
  }
}

/**
 * 搜索（回到第一页）
 */
function handleSearch() {
  page.current = 1
  loadData()
}

/**
 * 重置查询条件
 */
function resetSearch() {
  search.status = 0
  search.module = ''
  search.keyword = ''
  search.dateRange = []
  handleSearch()
}

/**
 * 打开详情弹窗
 */
async function openDetail(row) {
  remark.value = row.handleRemark || ''
  try {
    const res = await errorAlertAPI.getDetail(row.id)
    if (res && (res.code === 200 || res.code === 0) && res.data) {
      detail.value = res.data
    } else {
      detail.value = row
    }
  } catch (e) {
    detail.value = row
  }
  detailVisible.value = true
}

/**
 * 列表行内标记已修复 / 忽略
 */
async function handleAlert(row, action) {
  let text = ''
  if (action === 'handle') {
    try {
      const { value } = await ElMessageBox.prompt('填写修复原因（可选）', '标记为已修复', {
        inputPlaceholder: '例如：参数校验漏了，已补上',
        inputValue: '',
        confirmButtonText: '确定',
        cancelButtonText: '取消'
      })
      text = value || ''
    } catch (e) {
      // 用户取消
      return
    }
  }
  await submitAction(row.id, action, text)
}

/**
 * 弹窗内标记已修复
 */
async function submitHandle() {
  if (!detail.value) return
  await submitAction(detail.value.id, 'handle', remark.value)
}

/**
 * 弹窗内标记忽略
 */
async function submitIgnore() {
  if (!detail.value) return
  await submitAction(detail.value.id, 'ignore', remark.value)
}

/**
 * 提交状态变更
 */
async function submitAction(id, action, text) {
  try {
    const res = action === 'handle'
      ? await errorAlertAPI.handle(id, text)
      : await errorAlertAPI.ignore(id, text)
    if (res && (res.code === 200 || res.code === 0)) {
      ElMessage.success(action === 'handle' ? '已标记为已修复' : '已忽略')
      detailVisible.value = false
      loadData()
    } else {
      ElMessage.error((res && res.message) || '操作失败')
    }
  } catch (err) {
    ElMessage.error('操作失败')
  }
}

/**
 * 状态文案
 */
function statusText(status) {
  if (status === 1) return '已处理'
  if (status === 2) return '已忽略'
  return '未处理'
}

/**
 * 状态标签样式
 */
function statusType(status) {
  if (status === 1) return 'success'
  if (status === 2) return 'info'
  return 'danger'
}

onMounted(() => {
  loadData()
  // 告警是给人盯的，页面开着就周期性刷新；关掉页面即停
  refreshTimer = setInterval(() => {
    loadData()
  }, 60000)
})

onUnmounted(() => {
  if (refreshTimer) {
    clearInterval(refreshTimer)
    refreshTimer = null
  }
})
</script>

<style scoped>
.error-alert {
  padding: 16px;
}

.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.search-form {
  margin-bottom: 8px;
}

.pagination {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
}

.message-text {
  word-break: break-all;
}

.fingerprint {
  color: #909399;
  font-family: monospace;
}

.stack-block {
  margin-top: 16px;
}

.stack-title {
  font-weight: 600;
  margin-bottom: 8px;
}

.stack-text {
  max-height: 320px;
  overflow: auto;
  background: #f5f7fa;
  padding: 12px;
  border-radius: 4px;
  font-size: 12px;
  line-height: 1.6;
  white-space: pre-wrap;
  word-break: break-all;
  margin: 0;
}

.remark-input {
  margin-bottom: 12px;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
}
</style>
