<template>
  <div class="api-interface-container">
    <!-- 页面标题 -->
    <div class="page-header">
      <h2>接口管理</h2>
      <p>从后端 OpenAPI 规范自动加载，查看、搜索、测试和管理所有 bff-web 网关注册的接口</p>
    </div>

    <!-- 统计卡片 -->
    <div class="stats-section">
      <el-row :gutter="16">
        <el-col :span="4">
          <el-card class="stats-card" shadow="hover">
            <div class="stats-value">{{ allEndpoints.length }}</div>
            <div class="stats-label">接口总数</div>
          </el-card>
        </el-col>
        <el-col :span="4">
          <el-card class="stats-card" shadow="hover">
            <div class="stats-value" style="color:#67c23a">{{ methodCounts.get }}</div>
            <div class="stats-label">GET</div>
          </el-card>
        </el-col>
        <el-col :span="4">
          <el-card class="stats-card" shadow="hover">
            <div class="stats-value" style="color:#409eff">{{ methodCounts.post }}</div>
            <div class="stats-label">POST</div>
          </el-card>
        </el-col>
        <el-col :span="4">
          <el-card class="stats-card" shadow="hover">
            <div class="stats-value" style="color:#e6a23c">{{ methodCounts.put }}</div>
            <div class="stats-label">PUT</div>
          </el-card>
        </el-col>
        <el-col :span="4">
          <el-card class="stats-card" shadow="hover">
            <div class="stats-value" style="color:#f56c6c">{{ methodCounts.delete }}</div>
            <div class="stats-label">DELETE</div>
          </el-card>
        </el-col>
        <el-col :span="4">
          <el-card class="stats-card" shadow="hover">
            <div class="stats-value">{{ tagSet.size }}</div>
            <div class="stats-label">分组数</div>
          </el-card>
        </el-col>
      </el-row>
    </div>

    <!-- 搜索与筛选 -->
    <div class="filter-section">
      <el-card shadow="never">
        <div class="filter-bar">
          <div class="filter-left">
            <el-input v-model="filter.keyword" placeholder="搜索路径或描述" clearable style="width: 240px" @input="applyFilter">
              <template #prefix><el-icon><Search /></el-icon></template>
            </el-input>
            <el-select v-model="filter.method" placeholder="请求方法" clearable style="width: 130px" @change="applyFilter">
              <el-option label="全部方法" value="" />
              <el-option label="GET" value="get" />
              <el-option label="POST" value="post" />
              <el-option label="PUT" value="put" />
              <el-option label="DELETE" value="delete" />
              <el-option label="PATCH" value="patch" />
            </el-select>
            <el-select v-model="filter.tag" placeholder="接口分组" clearable filterable style="width: 180px" @change="applyFilter">
              <el-option label="全部分组" value="" />
              <el-option v-for="tag in sortedTags" :key="tag" :label="tag" :value="tag" />
            </el-select>
            <el-select v-model="filter.auth" placeholder="认证要求" clearable style="width: 140px" @change="applyFilter">
              <el-option label="全部" value="" />
              <el-option label="需认证" value="required" />
              <el-option label="公开" value="public" />
            </el-select>
          </div>
          <div class="filter-right">
            <el-button @click="loadApiSpec" :loading="loading"><el-icon><Refresh /></el-icon>刷新</el-button>
            <el-button type="primary" @click="handleExport"><el-icon><Download /></el-icon>导出</el-button>
          </div>
        </div>
      </el-card>
    </div>

    <!-- 接口列表 -->
    <div class="table-section">
      <el-card shadow="never">
        <template #header>
          <div class="card-header">
            <span>接口列表</span>
            <div class="card-header-right">
              <el-tag type="info">显示 {{ pagedEndpoints.length }} / {{ filteredEndpoints.length }} / {{ allEndpoints.length }}</el-tag>
              <el-button v-if="selectedRows.length > 0" type="danger" size="small" @click="handleBatchDisable">批量禁用 ({{ selectedRows.length }})</el-button>
            </div>
          </div>
        </template>
        <el-table v-loading="loading" :data="pagedEndpoints" stripe border style="width: 100%" @selection-change="handleSelectionChange" :row-class-name="rowClassName">
          <el-table-column type="selection" width="45" />
          <el-table-column prop="method" label="方法" width="90">
            <template #default="{ row }">
              <el-tag :type="methodTagType(row.method)" size="small" effect="dark" style="width:62px;text-align:center">{{ row.method.toUpperCase() }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="path" label="接口路径" min-width="260">
            <template #default="{ row }"><code class="path-code">{{ row.path }}</code></template>
          </el-table-column>
          <el-table-column prop="summary" label="接口描述" min-width="200" show-overflow-tooltip />
          <el-table-column prop="tag" label="分组" width="130">
            <template #default="{ row }"><el-tag size="small" effect="plain">{{ row.tag }}</el-tag></template>
          </el-table-column>
          <el-table-column label="认证" width="80" align="center">
            <template #default="{ row }">
              <el-tag :type="row.requireAuth ? 'warning' : 'success'" size="small">{{ row.requireAuth ? '需认证' : '公开' }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="状态" width="90" align="center">
            <template #default="{ row }">
              <el-switch v-model="row.enabled" size="small" @change="handleToggleEnabled(row)" />
            </template>
          </el-table-column>
          <el-table-column label="操作" width="180" fixed="right">
            <template #default="{ row }">
              <el-button link type="primary" size="small" @click="showDetail(row)">详情</el-button>
              <el-button link type="success" size="small" @click="openTestPanel(row)">测试</el-button>
              <el-button link type="info" size="small" @click="copyCurl(row)">cURL</el-button>
            </template>
          </el-table-column>
        </el-table>
        <div class="pagination-wrapper">
          <el-pagination v-model:current-page="pagination.page" v-model:page-size="pagination.pageSize" :page-sizes="[20, 50, 100, 200]" :total="filteredEndpoints.length" layout="total, sizes, prev, pager, next, jumper" @size-change="applyFilter" @current-change="applyFilter" />
        </div>
      </el-card>
    </div>

    <!-- 接口详情抽屉 -->
    <el-drawer v-model="detailDrawerVisible" title="接口详情" size="600px" destroy-on-close>
      <template v-if="selectedEndpoint">
        <el-descriptions :column="1" border>
          <el-descriptions-item label="接口路径">
            <div class="detail-path">
              <el-tag :type="methodTagType(selectedEndpoint.method)" size="small" effect="dark">{{ selectedEndpoint.method.toUpperCase() }}</el-tag>
              <code>{{ selectedEndpoint.path }}</code>
            </div>
          </el-descriptions-item>
          <el-descriptions-item label="接口描述">{{ selectedEndpoint.summary || '无描述' }}</el-descriptions-item>
          <el-descriptions-item label="接口分组">{{ selectedEndpoint.tag }}</el-descriptions-item>
          <el-descriptions-item label="操作ID">{{ selectedEndpoint.operationId || '-' }}</el-descriptions-item>
          <el-descriptions-item label="认证要求">
            <el-tag :type="selectedEndpoint.requireAuth ? 'warning' : 'success'" size="small">{{ selectedEndpoint.requireAuth ? '需要认证' : '公开接口' }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="selectedEndpoint.enabled ? 'success' : 'info'" size="small">{{ selectedEndpoint.enabled ? '启用' : '禁用' }}</el-tag>
          </el-descriptions-item>
        </el-descriptions>
        <div class="detail-block" v-if="selectedEndpoint.parameters && selectedEndpoint.parameters.length">
          <h4>请求参数</h4>
          <el-table :data="selectedEndpoint.parameters" size="small" border>
            <el-table-column prop="name" label="参数名" width="140" />
            <el-table-column prop="in" label="位置" width="80">
              <template #default="{ row }"><el-tag size="small" effect="plain">{{ row.in }}</el-tag></template>
            </el-table-column>
            <el-table-column prop="required" label="必填" width="60" align="center">
              <template #default="{ row }"><el-tag :type="row.required ? 'danger' : 'info'" size="small">{{ row.required ? '是' : '否' }}</el-tag></template>
            </el-table-column>
            <el-table-column prop="description" label="说明" min-width="140" show-overflow-tooltip />
            <el-table-column label="类型" width="100">
              <template #default="{ row }">{{ row.schema?.type || '-' }}</template>
            </el-table-column>
          </el-table>
        </div>
        <div class="detail-block" v-if="selectedEndpoint.requestBody">
          <h4>请求体 (Request Body)</h4>
          <pre class="schema-block">{{ selectedEndpoint.requestBody }}</pre>
        </div>
        <div class="detail-block" v-if="selectedEndpoint.responses && selectedEndpoint.responses.length">
          <h4>响应</h4>
          <el-table :data="selectedEndpoint.responses" size="small" border>
            <el-table-column prop="code" label="状态码" width="100" />
            <el-table-column prop="description" label="说明" min-width="200" show-overflow-tooltip />
          </el-table>
        </div>
        <div class="detail-block" v-if="selectedEndpoint.tags && selectedEndpoint.tags.length">
          <h4>标签</h4>
          <div style="display:flex;flex-wrap:wrap;gap:4px">
            <el-tag v-for="tag in selectedEndpoint.tags" :key="tag" size="small">{{ tag }}</el-tag>
          </div>
        </div>
        <div class="detail-block">
          <h4>快捷操作</h4>
          <el-button type="success" @click="openTestPanel(selectedEndpoint)"><el-icon><CaretRight /></el-icon>在线测试</el-button>
          <el-button @click="copyCurl(selectedEndpoint)"><el-icon><CopyDocument /></el-icon>复制 cURL</el-button>
        </div>
      </template>
    </el-drawer>

    <!-- 接口测试面板 -->
    <el-drawer v-model="testPanelVisible" title="接口测试" size="650px" direction="btt" destroy-on-close>
      <template v-if="testEndpoint">
        <el-descriptions :column="2" border size="small" class="mb-16">
          <el-descriptions-item label="方法">
            <el-tag :type="methodTagType(testEndpoint.method)" size="small" effect="dark">{{ testEndpoint.method.toUpperCase() }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="路径"><code>{{ testEndpoint.path }}</code></el-descriptions-item>
        </el-descriptions>
        <div class="test-block" v-if="testEndpoint.parameters && testEndpoint.parameters.length">
          <h4>请求参数</h4>
          <el-form label-width="120px" size="small">
            <el-form-item v-for="param in testEndpoint.parameters" :key="param.name" :label="param.name">
              <el-input v-model="testParams[param.name]" :placeholder="(param.description || '') + (param.required ? ' (必填)' : '')" clearable />
              <div class="param-hint" v-if="param.in">
                <el-tag size="small" effect="plain">{{ param.in }}</el-tag>
                <span v-if="param.schema?.type"> · {{ param.schema.type }}</span>
              </div>
            </el-form-item>
          </el-form>
        </div>
        <div class="test-block" v-if="['post','put','patch'].includes(testEndpoint.method)">
          <h4>请求体 (JSON)</h4>
          <el-input v-model="testBody" type="textarea" :rows="6" placeholder='{"key": "value"}' class="json-editor" />
        </div>
        <div class="test-actions">
          <el-button type="primary" @click="sendTestRequest" :loading="testSending" size="large"><el-icon><CaretRight /></el-icon>发送请求</el-button>
          <el-button @click="testPanelVisible = false" size="large">关闭</el-button>
        </div>
        <div class="test-block" v-if="testResponse !== null">
          <h4>
            响应结果
            <el-tag :type="testResponseStatus >= 200 && testResponseStatus < 300 ? 'success' : 'danger'" size="small" style="margin-left:8px">{{ testResponseStatus }}</el-tag>
            <span class="response-time" v-if="testResponseTime > 0">{{ testResponseTime }}ms</span>
          </h4>
          <pre class="schema-block">{{ typeof testResponse === 'object' ? JSON.stringify(testResponse, null, 2) : testResponse }}</pre>
        </div>
      </template>
    </el-drawer>
  </div>
</template>

<script setup>
/**
 * API 接口管理页面
 * 从 bff-web 的 springdoc OpenAPI 端点自动加载所有注册接口，
 * 提供搜索、筛选、详情查看、在线测试、cURL 复制、导出等功能。
 */
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Search, Refresh, Download, CaretRight, CopyDocument } from '@element-plus/icons-vue'
import axios from 'axios'

// ==================== 状态 ====================
const loading = ref(false)
const allEndpoints = ref([])
const selectedRows = ref([])
const filter = reactive({ keyword: '', method: '', tag: '', auth: '' })
const pagination = reactive({ page: 1, pageSize: 50 })
const detailDrawerVisible = ref(false)
const selectedEndpoint = ref(null)
const testPanelVisible = ref(false)
const testEndpoint = ref(null)
const testParams = reactive({})
const testBody = ref('')
const testSending = ref(false)
const testResponse = ref(null)
const testResponseStatus = ref(0)
const testResponseTime = ref(0)

// ==================== 计算属性 ====================
/** HTTP 方法计数 */
const methodCounts = computed(() => {
  const c = { get: 0, post: 0, put: 0, delete: 0, patch: 0 }
  allEndpoints.value.forEach(ep => { if (c[ep.method] !== undefined) c[ep.method]++ })
  return c
})
/** 所有分组标签 */
const sortedTags = computed(() => Array.from(new Set(allEndpoints.value.map(ep => ep.tag))).sort())
/** 分组集合 */
const tagSet = computed(() => new Set(allEndpoints.value.map(ep => ep.tag)))
/** 过滤后的接口 */
const filteredEndpoints = computed(() => {
  let list = allEndpoints.value
  if (filter.keyword) {
    const kw = filter.keyword.toLowerCase()
    list = list.filter(ep => ep.path.toLowerCase().includes(kw) || (ep.summary || '').toLowerCase().includes(kw))
  }
  if (filter.method) list = list.filter(ep => ep.method === filter.method)
  if (filter.tag) list = list.filter(ep => ep.tag === filter.tag)
  if (filter.auth === 'required') list = list.filter(ep => ep.requireAuth)
  if (filter.auth === 'public') list = list.filter(ep => !ep.requireAuth)
  return list
})
/** 分页后的接口列表 */
const pagedEndpoints = computed(() => {
  const page = pagination.page != null ? pagination.page : 1
  const pageSize = pagination.pageSize != null ? pagination.pageSize : 50
  const start = (page - 1) * pageSize
  return filteredEndpoints.value.slice(start, start + pageSize)
})

// ==================== 数据加载 ====================
/**
 * 从 bff-web 加载 OpenAPI 规范
 */
const loadApiSpec = async () => {
  loading.value = true
  try {
    const res = await axios.get('/v3/api-docs', { timeout: 15000 })
    const spec = res.data
    if (!spec || !spec.paths) { ElMessage.warning('OpenAPI 规范为空'); return }
    parseOpenApiSpec(spec)
    ElMessage.success(`已加载 ${allEndpoints.value.length} 个接口`)
  } catch (e) {
    console.error('加载 OpenAPI 失败:', e)
    ElMessage.error('加载接口列表失败: ' + (e.message || '网络异常'))
    allEndpoints.value = []
  } finally { loading.value = false }
}

/**
 * 解析 OpenAPI JSON 为扁平化接口列表
 * @param {Object} spec - OpenAPI 3.x 规范
 */
const parseOpenApiSpec = (spec) => {
  const endpoints = []
  const hasGlobalSecurity = spec.security && spec.security.length > 0
  Object.entries(spec.paths || {}).forEach(([path, pathItem]) => {
    const methods = ['get', 'post', 'put', 'delete', 'patch', 'options', 'head']
    methods.forEach(method => {
      const op = pathItem[method]
      if (!op) return
      const tags = op.tags || ['未分组']
      const requireAuth = hasGlobalSecurity || (op.security && op.security.length > 0) || path.startsWith('/web/')
      const parameters = (op.parameters || []).map(p => ({
        name: p.name, in: p.in, required: p.required || false,
        description: p.description || '', schema: p.schema || {}
      }))
      let requestBody = null
      if (op.requestBody) {
        const jsonSchema = op.requestBody.content?.['application/json']?.schema
        if (jsonSchema) requestBody = JSON.stringify(resolveRef(jsonSchema, spec), null, 2)
      }
      const responses = Object.entries(op.responses || {}).map(([code, r]) => ({ code, description: r.description || '' }))
      endpoints.push({
        path, method, tags, tag: tags[0] || '未分组',
        summary: op.summary || op.operationId || '',
        operationId: op.operationId || '',
        parameters, requestBody, responses, requireAuth, enabled: true
      })
    })
  })
  allEndpoints.value = endpoints
}

/**
 * 递归解析 $ref，防止循环引用
 */
const resolveRef = (schema, spec, seen = new Set()) => {
  if (!schema) return null
  if (schema.$ref) {
    const name = schema.$ref.split('/').pop()
    if (seen.has(name)) return `[循环: ${name}]`
    seen.add(name)
    let resolved = spec
    schema.$ref.replace('#/', '').split('/').forEach(k => { resolved = resolved?.[k] })
    return resolveRef(resolved, spec, seen)
  }
  if (schema.type === 'object' && schema.properties) {
    const obj = {}
    Object.entries(schema.properties).forEach(([k, v]) => { obj[k] = resolveRef(v, spec, seen) })
    return obj
  }
  if (schema.type === 'array' && schema.items) return [resolveRef(schema.items, spec, seen)]
  return schema.type || 'any'
}

// ==================== 操作方法 ====================
/** 筛选变化 */
const applyFilter = () => { pagination.page = 1 }
/** 多选变化 */
const handleSelectionChange = (rows) => { selectedRows.value = rows }
/** 行样式 */
const rowClassName = ({ row }) => row.enabled ? '' : 'disabled-row'
/** 显示详情 */
const showDetail = (ep) => { selectedEndpoint.value = ep; detailDrawerVisible.value = true }
/** 切换启用/禁用 */
const handleToggleEnabled = (row) => { ElMessage.success(`${row.path} 已${row.enabled ? '启用' : '禁用'}`) }
/** 批量禁用 */
const handleBatchDisable = () => {
  selectedRows.value.forEach(r => { r.enabled = false })
  ElMessage.success(`已禁用 ${selectedRows.value.length} 个接口`)
  selectedRows.value = []
}
/** 打开测试面板 */
const openTestPanel = (ep) => {
  testEndpoint.value = ep
  testResponse.value = null; testResponseStatus.value = 0; testResponseTime.value = 0
  Object.keys(testParams).forEach(k => delete testParams[k])
  ;(ep.parameters || []).forEach(p => { testParams[p.name] = '' })
  testBody.value = ep.requestBody ? buildDefaultBody(ep.requestBody) : ''
  testPanelVisible.value = true
}
/** 根据 schema 生成默认请求体 */
const buildDefaultBody = (schemaStr) => {
  try { return JSON.stringify(JSON.parse(schemaStr), null, 2) } catch { return '{}' }
}
/** 发送测试请求 */
const sendTestRequest = async () => {
  if (!testEndpoint.value) return
  testSending.value = true; testResponse.value = null
  const ep = testEndpoint.value
  let url = ep.path
  Object.entries(testParams).forEach(([key, val]) => { if (val) url = url.replace(`{${key}}`, encodeURIComponent(val)) })
  const queryParams = (ep.parameters || []).filter(p => p.in === 'query' && testParams[p.name])
  if (queryParams.length) {
    const qs = queryParams.map(p => `${p.name}=${encodeURIComponent(testParams[p.name])}`).join('&')
    url += (url.includes('?') ? '&' : '?') + qs
  }
  const config = { method: ep.method, url, timeout: 15000, headers: { 'Content-Type': 'application/json' } }
  if (['post', 'put', 'patch'].includes(ep.method) && testBody.value) {
    try { config.data = JSON.parse(testBody.value) } catch { config.data = testBody.value }
  }
  const start = Date.now()
  try {
    const res = await axios(config)
    testResponseStatus.value = res.status; testResponse.value = res.data
  } catch (err) {
    testResponseStatus.value = err.response?.status || 0
    testResponse.value = err.response?.data || { error: err.message }
  } finally { testResponseTime.value = Date.now() - start; testSending.value = false }
}
/** 复制 cURL 命令 */
const copyCurl = (ep) => {
  let curl = `curl -X ${ep.method.toUpperCase()} '${ep.path}' -H 'Content-Type: application/json'`
  if (['post', 'put', 'patch'].includes(ep.method) && ep.requestBody) curl += ` -d '${ep.requestBody.replace(/'/g, "\\'")}'`
  navigator.clipboard.writeText(curl).then(() => { ElMessage.success('cURL 已复制') }).catch(() => { ElMessage.error('复制失败') })
}
/** 导出接口列表为 JSON */
const handleExport = () => {
  const data = filteredEndpoints.value.map(ep => ({
    method: ep.method.toUpperCase(), path: ep.path, summary: ep.summary,
    tag: ep.tag, requireAuth: ep.requireAuth, operationId: ep.operationId
  }))
  const blob = new Blob([JSON.stringify(data, null, 2)], { type: 'application/json' })
  const a = document.createElement('a'); a.href = URL.createObjectURL(blob)
  a.download = `api-interfaces-${new Date().toISOString().slice(0, 10)}.json`; a.click()
  ElMessage.success(`已导出 ${data.length} 个接口`)
}
/** HTTP 方法标签颜色 */
const methodTagType = (m) => ({ get: 'success', post: 'primary', put: 'warning', delete: 'danger', patch: 'info' }[m] || 'info')

onMounted(() => { loadApiSpec() })
</script>

<style scoped>
.api-interface-container { padding: 20px; }
.page-header { margin-bottom: 20px; }
.page-header h2 { margin: 0 0 8px 0; color: #303133; }
.page-header p { margin: 0; color: #606266; font-size: 14px; }
.stats-section { margin-bottom: 20px; }
.stats-card { text-align: center; }
.stats-card :deep(.el-card__body) { padding: 16px; }
.stats-value { font-size: 28px; font-weight: bold; color: #303133; }
.stats-label { font-size: 13px; color: #909399; margin-top: 4px; }
.filter-section { margin-bottom: 20px; }
.filter-bar { display: flex; justify-content: space-between; align-items: center; flex-wrap: wrap; gap: 10px; }
.filter-left { display: flex; align-items: center; gap: 10px; flex-wrap: wrap; }
.filter-right { display: flex; align-items: center; gap: 10px; }
.table-section { margin-bottom: 20px; }
.card-header { display: flex; justify-content: space-between; align-items: center; }
.card-header-right { display: flex; align-items: center; gap: 10px; }
.path-code { font-family: 'Consolas', 'Monaco', monospace; font-size: 13px; color: #303133; }
.pagination-wrapper { display: flex; justify-content: flex-end; margin-top: 16px; }
:deep(.disabled-row) { opacity: 0.5; }
.detail-path { display: flex; align-items: center; gap: 8px; }
.detail-path code { font-family: 'Consolas', 'Monaco', monospace; font-size: 14px; }
.detail-block { margin-top: 20px; }
.detail-block h4 { margin: 0 0 10px 0; color: #303133; font-size: 15px; }
.schema-block {
  background: #f5f7fa; border: 1px solid #ebeef5; border-radius: 4px;
  padding: 12px; font-family: 'Consolas', 'Monaco', monospace; font-size: 12px;
  line-height: 1.5; max-height: 300px; overflow: auto; white-space: pre-wrap; word-break: break-all;
}
.mb-16 { margin-bottom: 16px; }
.test-block { margin-top: 16px; }
.test-block h4 { margin: 0 0 10px 0; color: #303133; font-size: 15px; }
.test-actions { margin-top: 20px; display: flex; gap: 12px; }
.param-hint { font-size: 12px; color: #909399; margin-top: 4px; display: flex; align-items: center; gap: 4px; }
.json-editor :deep(textarea) { font-family: 'Consolas', 'Monaco', monospace; font-size: 13px; }
.response-time { font-size: 13px; color: #909399; margin-left: 8px; }
</style>
