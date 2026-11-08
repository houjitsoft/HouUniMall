<template>
  <div class="operation-logs">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>操作日志</span>
          <el-tag type="primary">审计库 MySQL</el-tag>
          <el-tag type="info">含自动采集的增改删查</el-tag>
        </div>
      </template>

      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="用户名">
          <el-input v-model="searchForm.username" clearable placeholder="用户名" />
        </el-form-item>
        <el-form-item label="模块">
          <el-input v-model="searchForm.module" clearable placeholder="如 oa / goods" />
        </el-form-item>
        <el-form-item label="操作">
          <el-input v-model="searchForm.operation" clearable placeholder="操作描述关键字" />
        </el-form-item>
        <el-form-item label="关键词">
          <el-input v-model="searchForm.keyword" clearable placeholder="URL / 操作" />
        </el-form-item>
        <el-form-item label="时间范围">
          <el-date-picker
            v-model="searchForm.dateRange"
            type="datetimerange"
            value-format="YYYY-MM-DD HH:mm:ss"
            start-placeholder="开始"
            end-placeholder="结束"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>

      <el-table v-loading="loading" :data="logList" max-height="520" @row-click="openDetail">
        <el-table-column prop="operationTime" label="时间" width="170" />
        <el-table-column prop="username" label="用户" width="120" />
        <el-table-column prop="module" label="模块" width="90" />
        <el-table-column prop="operationDesc" label="操作" min-width="160" show-overflow-tooltip />
        <el-table-column prop="requestMethod" label="方法" width="80" />
        <el-table-column prop="requestUrl" label="请求地址" min-width="180" show-overflow-tooltip />
        <el-table-column prop="ipAddress" label="IP" width="120" />
        <el-table-column prop="status" label="结果" width="80">
          <template #default="{ row }">
            <el-tag :type="row.status === 'success' ? 'success' : 'danger'" size="small">
              {{ row.status === 'success' ? '成功' : '失败' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="详情" width="72" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click.stop="openDetail(row)">查看</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        v-model:current-page="pagination.current"
        v-model:page-size="pagination.size"
        :total="pagination.total"
        :page-sizes="[10, 20, 50]"
        layout="total, sizes, prev, pager, next"
        class="pager"
        @size-change="loadList"
        @current-change="loadList"
      />
    </el-card>

    <el-drawer v-model="detailVisible" title="操作日志详情" size="480px" destroy-on-close>
      <template v-if="detail">
        <el-descriptions :column="1" border>
          <el-descriptions-item label="时间">{{ detail.operationTime || '—' }}</el-descriptions-item>
          <el-descriptions-item label="用户">{{ detail.username || '—' }}</el-descriptions-item>
          <el-descriptions-item label="模块">{{ detail.module || '—' }}</el-descriptions-item>
          <el-descriptions-item label="操作">{{ detail.operationDesc || '—' }}</el-descriptions-item>
          <el-descriptions-item label="HTTP 方法">{{ detail.requestMethod || '—' }}</el-descriptions-item>
          <el-descriptions-item label="请求地址">{{ detail.requestUrl || '—' }}</el-descriptions-item>
          <el-descriptions-item label="类方法">{{ detail.method || '—' }}</el-descriptions-item>
          <el-descriptions-item label="IP">{{ detail.ipAddress || '—' }}</el-descriptions-item>
          <el-descriptions-item label="耗时">
            {{ detail.executionTime != null ? `${detail.executionTime} ms` : '—' }}
          </el-descriptions-item>
          <el-descriptions-item label="结果">
            {{ detail.status === 'success' ? '成功' : '失败' }}
          </el-descriptions-item>
          <el-descriptions-item v-if="detail.errorMsg" label="错误">
            {{ detail.errorMsg }}
          </el-descriptions-item>
        </el-descriptions>
        <div class="params-block">
          <div class="params-title">请求参数</div>
          <pre class="params-pre">{{ formatParams(detail.requestParams) }}</pre>
        </div>
      </template>
    </el-drawer>
  </div>
</template>

<script setup>
import { reactive, ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { operationLogAPI } from '@/api/log/index.js'
import { unwrapPage, mapOperationRow } from '@/utils/logApi.js'

const searchForm = reactive({
  username: '',
  module: '',
  operation: '',
  keyword: '',
  dateRange: []
})
const logList = ref([])
const loading = ref(false)
const pagination = reactive({ current: 1, size: 20, total: 0 })
const detailVisible = ref(false)
const detail = ref(null)

const buildParams = () => {
  const p = {}
  if (searchForm.username) p.username = searchForm.username
  if (searchForm.module) p.module = searchForm.module
  if (searchForm.operation) p.operation = searchForm.operation
  if (searchForm.keyword) p.keyword = searchForm.keyword
  if (searchForm.dateRange?.length === 2) {
    p.startTime = searchForm.dateRange[0]
    p.endTime = searchForm.dateRange[1]
  }
  return p
}

const loadList = async () => {
  loading.value = true
  try {
    const page = unwrapPage(await operationLogAPI.getPage(buildParams(), pagination.current, pagination.size))
    logList.value = (page.records || []).map(mapOperationRow)
    pagination.total = page.total || 0
  } catch (e) {
    ElMessage.error(e.message || '加载失败')
  } finally {
    loading.value = false
  }
}

const handleSearch = () => { pagination.current = 1; loadList() }
const handleReset = () => {
  searchForm.username = ''
  searchForm.module = ''
  searchForm.operation = ''
  searchForm.keyword = ''
  searchForm.dateRange = []
  pagination.current = 1
  loadList()
}

const openDetail = (row) => {
  detail.value = row
  detailVisible.value = true
}

const formatParams = (raw) => {
  if (raw == null || raw === '') return '（无）'
  if (typeof raw !== 'string') {
    try {
      return JSON.stringify(raw, null, 2)
    } catch {
      return String(raw)
    }
  }
  try {
    return JSON.stringify(JSON.parse(raw), null, 2)
  } catch {
    return raw
  }
}

onMounted(loadList)
</script>

<style scoped>
.operation-logs { padding: 20px; }
.card-header { display: flex; align-items: center; gap: 12px; }
.search-form { margin-bottom: 16px; }
.pager { margin-top: 16px; justify-content: flex-end; }
.params-block { margin-top: 16px; }
.params-title { font-weight: 600; margin-bottom: 8px; }
.params-pre {
  margin: 0;
  padding: 12px;
  background: var(--el-fill-color-light);
  border-radius: 6px;
  font-size: 12px;
  line-height: 1.5;
  white-space: pre-wrap;
  word-break: break-all;
  max-height: 360px;
  overflow: auto;
}
</style>
