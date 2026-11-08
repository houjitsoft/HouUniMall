<template>
  <div class="api-document-container">
    <!-- 页面标题 -->
    <div class="page-header">
      <h2>API 文档</h2>
      <p>自动从后端 OpenAPI 规范加载，展示所有 bff-web 网关暴露的接口</p>
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
            <div class="stats-value" style="color:#67c23a">{{ methodCounts.get || 0 }}</div>
            <div class="stats-label">GET</div>
          </el-card>
        </el-col>
        <el-col :span="4">
          <el-card class="stats-card" shadow="hover">
            <div class="stats-value" style="color:#409eff">{{ methodCounts.post || 0 }}</div>
            <div class="stats-label">POST</div>
          </el-card>
        </el-col>
        <el-col :span="4">
          <el-card class="stats-card" shadow="hover">
            <div class="stats-value" style="color:#e6a23c">{{ methodCounts.put || 0 }}</div>
            <div class="stats-label">PUT</div>
          </el-card>
        </el-col>
        <el-col :span="4">
          <el-card class="stats-card" shadow="hover">
            <div class="stats-value" style="color:#f56c6c">{{ methodCounts.delete || 0 }}</div>
            <div class="stats-label">DELETE</div>
          </el-card>
        </el-col>
        <el-col :span="4">
          <el-card class="stats-card" shadow="hover">
            <div class="stats-value">{{ tagCounts.size }}</div>
            <div class="stats-label">分组数</div>
          </el-card>
        </el-col>
      </el-row>
    </div>

    <!-- 搜索与筛选 -->
    <div class="filter-section">
      <el-card shadow="never">
        <div class="filter-bar">
          <el-input
            v-model="filter.keyword"
            placeholder="搜索接口路径或描述"
            clearable
            style="width: 280px"
            @input="handleFilterChange"
          >
            <template #prefix><el-icon><Search /></el-icon></template>
          </el-input>
          <el-select v-model="filter.method" placeholder="请求方法" clearable style="width: 130px" @change="handleFilterChange">
            <el-option label="全部方法" value="" />
            <el-option label="GET" value="get" />
            <el-option label="POST" value="post" />
            <el-option label="PUT" value="put" />
            <el-option label="DELETE" value="delete" />
          </el-select>
          <el-select v-model="filter.tag" placeholder="接口分组" clearable filterable style="width: 200px" @change="handleFilterChange">
            <el-option label="全部分组" value="" />
            <el-option v-for="tag in sortedTags" :key="tag" :label="tag" :value="tag" />
          </el-select>
          <el-button type="primary" @click="loadApiSpec" :loading="loading">
            <el-icon><Refresh /></el-icon>
            刷新
          </el-button>
        </div>
      </el-card>
    </div>

    <!-- 接口列表 -->
    <div class="endpoint-section">
      <el-card shadow="never">
        <template #header>
          <div class="card-header">
            <span>接口列表</span>
            <el-tag type="info">显示 {{ filteredEndpoints.length }} / {{ allEndpoints.length }}</el-tag>
          </div>
        </template>

        <div v-loading="loading">
          <!-- 按分组折叠展示 -->
          <template v-if="groupedEndpoints.length > 0">
            <el-collapse v-model="expandedTags" class="endpoint-collapse">
              <el-collapse-item
                v-for="group in groupedEndpoints"
                :key="group.tag"
                :name="group.tag"
              >
                <template #title>
                  <div class="group-title">
                    <el-icon><Folder /></el-icon>
                    <span>{{ group.tag }}</span>
                    <el-tag size="small" type="info" round>{{ group.endpoints.length }}</el-tag>
                  </div>
                </template>
                <div class="endpoint-list">
                  <div
                    v-for="ep in group.endpoints"
                    :key="ep.method + ep.path"
                    class="endpoint-item"
                    @click="showDetail(ep)"
                  >
                    <el-tag
                      :type="methodTagType(ep.method)"
                      size="small"
                      effect="dark"
                      class="method-tag"
                    >{{ ep.method.toUpperCase() }}</el-tag>
                    <span class="endpoint-path">{{ ep.path }}</span>
                    <span class="endpoint-summary">{{ ep.summary }}</span>
                  </div>
                </div>
              </el-collapse-item>
            </el-collapse>
          </template>

          <el-empty v-else-if="!loading" description="暂无匹配的接口" />
        </div>
      </el-card>
    </div>

    <!-- 接口详情抽屉 -->
    <el-drawer v-model="drawerVisible" title="接口详情" size="600px" destroy-on-close>
      <template v-if="selectedEndpoint">
        <el-descriptions :column="1" border>
          <el-descriptions-item label="接口路径">
            <div class="detail-path">
              <el-tag :type="methodTagType(selectedEndpoint.method)" size="small" effect="dark">
                {{ selectedEndpoint.method.toUpperCase() }}
              </el-tag>
              <code>{{ selectedEndpoint.path }}</code>
            </div>
          </el-descriptions-item>
          <el-descriptions-item label="接口描述">{{ selectedEndpoint.summary || '无描述' }}</el-descriptions-item>
          <el-descriptions-item label="接口分组">{{ selectedEndpoint.tag || '未分组' }}</el-descriptions-item>
          <el-descriptions-item label="操作ID">{{ selectedEndpoint.operationId || '-' }}</el-descriptions-item>
        </el-descriptions>

        <!-- 请求参数 -->
        <div class="detail-block" v-if="selectedEndpoint.parameters && selectedEndpoint.parameters.length">
          <h4>请求参数</h4>
          <el-table :data="selectedEndpoint.parameters" size="small" border>
            <el-table-column prop="name" label="参数名" width="140" />
            <el-table-column prop="in" label="位置" width="80">
              <template #default="{ row }">
                <el-tag size="small" effect="plain">{{ row.in }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="required" label="必填" width="60" align="center">
              <template #default="{ row }">
                <el-tag :type="row.required ? 'danger' : 'info'" size="small">{{ row.required ? '是' : '否' }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="description" label="说明" min-width="160" show-overflow-tooltip />
            <el-table-column label="类型" width="100">
              <template #default="{ row }">
                {{ resolveParamType(row) }}
              </template>
            </el-table-column>
          </el-table>
        </div>

        <!-- 请求体 -->
        <div class="detail-block" v-if="selectedEndpoint.requestBody">
          <h4>请求体 (Request Body)</h4>
          <el-tag type="warning" size="small">Content-Type: application/json</el-tag>
          <pre class="schema-block">{{ selectedEndpoint.requestBody }}</pre>
        </div>

        <!-- 响应 -->
        <div class="detail-block" v-if="selectedEndpoint.responses && selectedEndpoint.responses.length">
          <h4>响应</h4>
          <el-table :data="selectedEndpoint.responses" size="small" border>
            <el-table-column prop="code" label="状态码" width="100" />
            <el-table-column prop="description" label="说明" min-width="200" show-overflow-tooltip />
          </el-table>
        </div>

        <!-- 标签 -->
        <div class="detail-block" v-if="selectedEndpoint.tags && selectedEndpoint.tags.length">
          <h4>标签</h4>
          <div class="tag-list">
            <el-tag v-for="tag in selectedEndpoint.tags" :key="tag" size="small" style="margin-right:6px">{{ tag }}</el-tag>
          </div>
        </div>
      </template>
    </el-drawer>
  </div>
</template>

<script setup>
/**
 * API 文档管理页面
 * 从 bff-web 的 springdoc OpenAPI 端点自动加载接口文档，
 * 提供搜索、分组浏览、详情查看功能。
 */
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Search, Refresh, Folder } from '@element-plus/icons-vue'
import axios from 'axios'

// 状态
const loading = ref(false)
const allEndpoints = ref([])
const drawerVisible = ref(false)
const selectedEndpoint = ref(null)
const expandedTags = ref([])

// 筛选条件
const filter = reactive({
  keyword: '',
  method: '',
  tag: ''
})

/**
 * 各 HTTP 方法计数
 */
const methodCounts = computed(() => {
  const counts = { get: 0, post: 0, put: 0, delete: 0 }
  allEndpoints.value.forEach(ep => {
    if (counts[ep.method] !== undefined) counts[ep.method]++
  })
  return counts
})

/**
 * 所有分组标签（去重排序）
 */
const sortedTags = computed(() => {
  const tags = new Set(allEndpoints.value.map(ep => ep.tag || '未分组'))
  return Array.from(tags).sort()
})

/**
 * 分组标签数量
 */
const tagCounts = computed(() => new Set(allEndpoints.value.map(ep => ep.tag || '未分组')))

/**
 * 过滤后的接口列表
 */
const filteredEndpoints = computed(() => {
  let list = allEndpoints.value
  if (filter.keyword) {
    const kw = filter.keyword.toLowerCase()
    list = list.filter(ep =>
      ep.path.toLowerCase().includes(kw) ||
      (ep.summary && ep.summary.toLowerCase().includes(kw))
    )
  }
  if (filter.method) {
    list = list.filter(ep => ep.method === filter.method)
  }
  if (filter.tag) {
    list = list.filter(ep => (ep.tag || '未分组') === filter.tag)
  }
  return list
})

/**
 * 按分组聚合
 */
const groupedEndpoints = computed(() => {
  const map = new Map()
  filteredEndpoints.value.forEach(ep => {
    const tag = ep.tag || '未分组'
    if (!map.has(tag)) map.set(tag, { tag, endpoints: [] })
    map.get(tag).endpoints.push(ep)
  })
  return Array.from(map.values()).sort((a, b) => a.tag.localeCompare(b.tag))
})

/**
 * 从 bff-web 加载 OpenAPI 规范
 */
const loadApiSpec = async () => {
  loading.value = true
  try {
    const res = await axios.get('/v3/api-docs', { timeout: 15000 })
    const spec = res.data
    if (!spec || !spec.paths) {
      ElMessage.warning('OpenAPI 规范为空或格式异常')
      return
    }
    parseOpenApiSpec(spec)
    // 默认展开前 5 个分组
    expandedTags.value = sortedTags.value.slice(0, 5)
    ElMessage.success(`已加载 ${allEndpoints.value.length} 个接口`)
  } catch (e) {
    console.error('加载 OpenAPI 规范失败:', e)
    ElMessage.error('加载 API 文档失败: ' + (e.message || '网络异常'))
    // 降级：使用空列表
    allEndpoints.value = []
  } finally {
    loading.value = false
  }
}

/**
 * 解析 OpenAPI JSON 为扁平化接口列表
 * @param {Object} spec - OpenAPI 3.x 规范对象
 */
const parseOpenApiSpec = (spec) => {
  const endpoints = []
  const paths = spec.paths || {}

  Object.entries(paths).forEach(([path, pathItem]) => {
    const methods = ['get', 'post', 'put', 'delete', 'patch', 'options', 'head']
    methods.forEach(method => {
      const operation = pathItem[method]
      if (!operation) return

      const tags = operation.tags || []
      const tag = tags[0] || '未分组'
      const summary = operation.summary || operation.operationId || ''
      const operationId = operation.operationId || ''

      // 解析参数
      const parameters = (operation.parameters || []).map(p => ({
        name: p.name,
        in: p.in,
        required: p.required || false,
        description: p.description || '',
        schema: p.schema || {}
      }))

      // 解析请求体
      let requestBody = null
      if (operation.requestBody) {
        const content = operation.requestBody.content || {}
        const jsonSchema = content['application/json']?.schema
        if (jsonSchema) {
          requestBody = JSON.stringify(resolveSchemaRef(jsonSchema, spec), null, 2)
        }
      }

      // 解析响应
      const responses = Object.entries(operation.responses || {}).map(([code, resp]) => ({
        code,
        description: resp.description || ''
      }))

      endpoints.push({
        path,
        method,
        summary,
        tag,
        tags,
        operationId,
        parameters,
        requestBody,
        responses
      })
    })
  })

  allEndpoints.value = endpoints
}

/**
 * 递归解析 $ref 引用，返回可读的 schema 对象
 * @param {Object} schema
 * @param {Object} spec
 * @param {Set} seen - 防止循环引用
 * @returns {Object}
 */
const resolveSchemaRef = (schema, spec, seen = new Set()) => {
  if (!schema) return null
  if (schema.$ref) {
    const refPath = schema.$ref.replace('#/', '').split('/')
    let resolved = spec
    for (const key of refPath) {
      resolved = resolved?.[key]
    }
    const refName = refPath[refPath.length - 1]
    if (seen.has(refName)) return `[循环引用: ${refName}]`
    seen.add(refName)
    return resolveSchemaRef(resolved, spec, seen)
  }
  if (schema.type === 'object' && schema.properties) {
    const obj = {}
    Object.entries(schema.properties).forEach(([key, val]) => {
      obj[key] = resolveSchemaRef(val, spec, seen)
    })
    return obj
  }
  if (schema.type === 'array' && schema.items) {
    return [resolveSchemaRef(schema.items, spec, seen)]
  }
  return schema.type || 'any'
}

/**
 * 获取参数类型显示文本
 * @param {Object} param
 * @returns {string}
 */
const resolveParamType = (param) => {
  if (param.schema) {
    return param.schema.type || param.schema.format || '-'
  }
  return '-'
}

/**
 * 获取 HTTP 方法对应的标签颜色
 * @param {string} method
 * @returns {string}
 */
const methodTagType = (method) => {
  const map = { get: 'success', post: 'primary', put: 'warning', delete: 'danger', patch: 'info' }
  return map[method] || 'info'
}

/**
 * 筛选条件变化
 */
const handleFilterChange = () => {
  // 响应式自动过滤，无需额外操作
}

/**
 * 显示接口详情
 * @param {Object} ep - 接口对象
 */
const showDetail = (ep) => {
  selectedEndpoint.value = ep
  drawerVisible.value = true
}

onMounted(() => {
  loadApiSpec()
})
</script>

<style scoped>
.api-document-container {
  padding: 20px;
}

.page-header {
  margin-bottom: 20px;
}

.page-header h2 {
  margin: 0 0 8px 0;
  color: #303133;
}

.page-header p {
  margin: 0;
  color: #606266;
  font-size: 14px;
}

.stats-section {
  margin-bottom: 20px;
}

.stats-card {
  text-align: center;
  cursor: default;
}

.stats-card :deep(.el-card__body) {
  padding: 16px;
}

.stats-value {
  font-size: 28px;
  font-weight: bold;
  color: #303133;
}

.stats-label {
  font-size: 13px;
  color: #909399;
  margin-top: 4px;
}

.filter-section {
  margin-bottom: 20px;
}

.filter-bar {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.endpoint-section {
  margin-bottom: 20px;
}

.endpoint-collapse {
  border: none;
}

.group-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: 500;
  font-size: 14px;
}

.endpoint-list {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.endpoint-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 8px 12px;
  border-radius: 4px;
  cursor: pointer;
  transition: background-color 0.2s;
}

.endpoint-item:hover {
  background-color: #f5f7fa;
}

.method-tag {
  width: 65px;
  text-align: center;
  font-family: monospace;
  flex-shrink: 0;
}

.endpoint-path {
  font-family: 'Consolas', 'Monaco', monospace;
  font-size: 13px;
  color: #303133;
  flex-shrink: 0;
  max-width: 360px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.endpoint-summary {
  font-size: 13px;
  color: #909399;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  flex: 1;
}

/* 详情抽屉 */
.detail-path {
  display: flex;
  align-items: center;
  gap: 8px;
}

.detail-path code {
  font-family: 'Consolas', 'Monaco', monospace;
  font-size: 14px;
  color: #303133;
}

.detail-block {
  margin-top: 20px;
}

.detail-block h4 {
  margin: 0 0 10px 0;
  color: #303133;
  font-size: 15px;
}

.schema-block {
  background: #f5f7fa;
  border: 1px solid #ebeef5;
  border-radius: 4px;
  padding: 12px;
  font-family: 'Consolas', 'Monaco', monospace;
  font-size: 12px;
  line-height: 1.5;
  max-height: 300px;
  overflow: auto;
  margin-top: 8px;
  white-space: pre-wrap;
  word-break: break-all;
}

.tag-list {
  display: flex;
  flex-wrap: wrap;
  gap: 4px;
}
</style>
