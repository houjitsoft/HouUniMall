<template>
  <div class="wf-page" v-loading="pageLoading">
    <div class="wf-header">
      <div class="wf-header__main">
        <h1 class="wf-header__title">工作流编排</h1>
        <p class="wf-header__desc">
          把 AI 要干的几步连成一条流程：开始 → 模型调用 / 条件分支 / 变量加工 → 结束，
          节点之间用 <code>{{ brace('变量名') }}</code> 传数据。
          注意：流程编完<b>不会自己跑</b> —— 要去「智能体管理」里，把某个智能体的执行模式切成「工作流」并选中这条流程。
        </p>
      </div>
      <div class="wf-header__actions">
        <el-tooltip content="刷新列表" placement="bottom">
          <el-button class="wf-icon-btn" plain @click="loadList">
            <el-icon><Refresh /></el-icon>
          </el-button>
        </el-tooltip>
        <el-tooltip content="回到智能体管理，把某个智能体切成工作流模式并选中流程" placement="bottom">
          <el-button type="primary" plain @click="router.push('/ai/agents')">
            <el-icon><Service /></el-icon>
            智能体管理
          </el-button>
        </el-tooltip>
        <el-button type="primary" @click="router.push('/ai/workflow/design')">
          <el-icon><Plus /></el-icon>
          新建工作流
        </el-button>
      </div>
    </div>

    <el-card shadow="never" class="wf-panel">
      <div class="wf-toolbar">
        <el-select v-model="filters.terminal" placeholder="全部端" clearable style="width: 150px">
          <el-option v-for="t in TERMINALS" :key="t.value" :label="t.label" :value="t.value" />
        </el-select>
        <el-select v-model="filters.status" placeholder="全部状态" clearable style="width: 130px">
          <el-option label="已启用" :value="1" />
          <el-option label="已停用" :value="0" />
        </el-select>
        <el-input
          v-model="filters.keyword"
          placeholder="搜索名称或编码"
          clearable
          style="width: 220px"
          @keyup.enter="loadList"
        />
        <div class="wf-toolbar__spacer" />
        <el-button type="primary" @click="loadList">查询</el-button>
        <el-button @click="handleReset">重置</el-button>
      </div>

      <el-table :data="list" border stripe size="small" empty-text="还没有工作流，点右上角新建一个">
        <el-table-column prop="name" label="名称" min-width="180">
          <template #default="{ row }">
            <div class="wf-cell__name">{{ row.name }}</div>
            <div class="wf-cell__code">{{ row.code }}</div>
          </template>
        </el-table-column>
        <el-table-column label="所属端" width="110">
          <template #default="{ row }">
            <el-tag size="small" effect="plain">{{ terminalLabel(row.terminal) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="nodeCount" label="节点数" width="90">
          <template #default="{ row }">
            <span>{{ row.nodeCount ?? '—' }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="version" label="版本" width="80">
          <template #default="{ row }">
            <span>v{{ row.version ?? 1 }}</span>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="90">
          <template #default="{ row }">
            <el-tag size="small" :type="row.status === 1 ? 'success' : 'info'" effect="light">
              {{ row.status === 1 ? '已启用' : '已停用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="description" label="说明" min-width="220" show-overflow-tooltip />
        <el-table-column prop="updateTime" label="更新时间" width="170" />
        <el-table-column label="操作" width="290" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="openDesigner(row)">编排</el-button>
            <el-button link type="primary" @click="openRuns(row)">运行记录</el-button>
            <el-button link type="primary" @click="onCopy(row)">复制</el-button>
            <el-button link :type="row.status === 1 ? 'warning' : 'success'" @click="onToggle(row)">
              {{ row.status === 1 ? '停用' : '启用' }}
            </el-button>
            <el-button link type="danger" @click="onDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="wf-pagination">
        <el-pagination
          v-model:current-page="pagination.page"
          v-model:page-size="pagination.limit"
          :total="pagination.total"
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next"
          @change="loadList"
        />
      </div>
    </el-card>

    <!-- 运行记录 -->
    <el-drawer v-model="runsVisible" :title="`运行记录 · ${runsTitle}`" size="52%">
      <el-table :data="runs" size="small" v-loading="runsLoading" @row-click="openRunDetail">
        <el-table-column prop="runNo" label="运行编号" width="200" />
        <el-table-column label="状态" width="90">
          <template #default="{ row }">
            <el-tag size="small" :type="row.status === 'success' ? 'success' : 'danger'" effect="plain">
              {{ row.status === 'success' ? '成功' : '失败' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="costMs" label="耗时(ms)" width="100" />
        <el-table-column prop="tokenUsed" label="Token" width="90" />
        <el-table-column prop="errorMsg" label="错误" min-width="160" show-overflow-tooltip />
        <el-table-column prop="createTime" label="时间" width="170" />
      </el-table>

      <el-divider v-if="runDetail" content-position="left">轨迹明细</el-divider>
      <el-table v-if="runDetail" :data="runDetail.traces || []" size="small">
        <el-table-column prop="name" label="节点" width="130" />
        <el-table-column prop="type" label="类型" width="90" />
        <el-table-column label="状态" width="80">
          <template #default="{ row }">
            <el-tag size="small" :type="row.status === 'failed' ? 'danger' : 'success'" effect="plain">
              {{ row.status === 'failed' ? '失败' : '成功' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="costMs" label="耗时(ms)" width="90" />
        <el-table-column label="输出" min-width="240">
          <template #default="{ row }">
            <span class="wf-clip">{{ clip(row.output) }}</span>
          </template>
        </el-table-column>
      </el-table>
      <div v-if="runDetail" class="wf-runout">
        <div class="wf-runout__label">最终输出</div>
        <div class="wf-runout__text">{{ runDetail.outputs?.text }}</div>
      </div>
    </el-drawer>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Refresh, Service } from '@element-plus/icons-vue'
import { workflowApi } from '@/api/ai/workflow'

const router = useRouter()

const TERMINALS = [
  { value: 'merchant', label: '商家端' },
  { value: 'frontend', label: '前端' },
  { value: 'admin', label: '管理后台' },
  { value: 'im', label: 'IM 客服' }
]

const pageLoading = ref(false)
const list = ref([])
const pagination = reactive({ page: 1, limit: 10, total: 0 })
const filters = reactive({ terminal: '', status: '', keyword: '' })

const runsVisible = ref(false)
const runsLoading = ref(false)
const runs = ref([])
const runsTitle = ref('')
const runDetail = ref(null)

const brace = (name) => '{{' + name + '}}'

const terminalLabel = (t) => TERMINALS.find((x) => x.value === t)?.label || t || '—'
const clip = (v) => {
  const s = v == null ? '' : typeof v === 'object' ? JSON.stringify(v) : String(v)
  return s.length > 200 ? s.slice(0, 200) + '…' : s
}

const loadList = async () => {
  pageLoading.value = true
  try {
    const params = { page: pagination.page, limit: pagination.limit }
    if (filters.terminal) params.terminal = filters.terminal
    if (filters.status !== '' && filters.status !== null) params.status = filters.status
    if (filters.keyword) params.keyword = filters.keyword
    const res = await workflowApi.getList(params)
    if (res.code === 1 && res.data) {
      list.value = res.data.list || []
      pagination.total = res.data.total || 0
    } else {
      list.value = []
      pagination.total = 0
    }
  } catch (e) {
    ElMessage.error(e.message || '加载工作流列表失败')
  } finally {
    pageLoading.value = false
  }
}

const handleReset = () => {
  filters.terminal = ''
  filters.status = ''
  filters.keyword = ''
  pagination.page = 1
  loadList()
}

const openDesigner = (row) => router.push(`/ai/workflow/design?code=${encodeURIComponent(row.code)}`)

const onCopy = async (row) => {
  try {
    const res = await workflowApi.copy({ code: row.code })
    if (res.code === 1) {
      ElMessage.success(`已复制为 ${res.data?.code}`)
      loadList()
    } else {
      ElMessage.error(res.msg || '复制失败')
    }
  } catch (e) {
    ElMessage.error(e.message || '复制失败')
  }
}

const onToggle = async (row) => {
  const next = row.status === 1 ? 0 : 1
  try {
    const res = await workflowApi.updateStatus({ code: row.code, status: next })
    if (res.code === 1) {
      ElMessage.success(next === 1 ? '已启用' : '已停用')
      loadList()
    } else {
      ElMessage.error(res.msg || '操作失败')
    }
  } catch (e) {
    ElMessage.error(e.message || '操作失败')
  }
}

const onDelete = async (row) => {
  try {
    await ElMessageBox.confirm(`确定删除工作流「${row.name}」吗？删除后引用它的智能体会跑不起来。`, '删除确认', {
      type: 'warning'
    })
  } catch {
    return
  }
  try {
    const res = await workflowApi.remove({ id: row.id })
    if (res.code === 1 && res.data?.success !== false) {
      ElMessage.success('已删除')
      loadList()
    } else {
      ElMessage.error(res.data?.message || res.msg || '删除失败')
    }
  } catch (e) {
    ElMessage.error(e.message || '删除失败')
  }
}

const openRuns = async (row) => {
  runsTitle.value = row.name || row.code
  runs.value = []
  runDetail.value = null
  runsVisible.value = true
  runsLoading.value = true
  try {
    const res = await workflowApi.getRuns({ workflowCode: row.code, page: 1, limit: 20 })
    if (res.code === 1 && res.data) runs.value = res.data.list || []
  } catch (e) {
    ElMessage.error(e.message || '加载运行记录失败')
  } finally {
    runsLoading.value = false
  }
}

const openRunDetail = async (row) => {
  try {
    const res = await workflowApi.getRunDetail(row.runNo)
    if (res.code === 1) runDetail.value = res.data
  } catch (e) {
    ElMessage.error(e.message || '加载轨迹失败')
  }
}

onMounted(loadList)
</script>

<style scoped>
.wf-page {
  padding: 4px;
}
.wf-header {
  display: flex;
  align-items: flex-start;
  gap: 16px;
  margin-bottom: 14px;
}
.wf-header__main {
  flex: 1;
}
.wf-header__title {
  font-size: 20px;
  margin: 0 0 6px;
}
.wf-header__desc {
  margin: 0;
  color: #909399;
  font-size: 13px;
  line-height: 1.7;
}
.wf-header__desc code {
  background: #f0f2f5;
  padding: 0 3px;
  border-radius: 3px;
}
.wf-header__actions {
  display: flex;
  align-items: center;
  gap: 10px;
}
.wf-header__actions .wf-icon-btn {
  padding-left: 8px;
  padding-right: 8px;
}
.wf-panel {
  margin-bottom: 12px;
}
.wf-toolbar {
  display: flex;
  gap: 10px;
  align-items: center;
  margin-bottom: 12px;
}
.wf-toolbar__spacer {
  flex: 1;
}
.wf-cell__name {
  font-weight: 600;
  font-size: 13px;
}
.wf-cell__code {
  font-size: 11px;
  color: #909399;
}
.wf-pagination {
  margin-top: 12px;
  display: flex;
  justify-content: flex-end;
}
.wf-clip {
  font-size: 12px;
  color: #606266;
}
.wf-runout {
  margin-top: 14px;
}
.wf-runout__label {
  font-size: 12px;
  color: #909399;
  margin-bottom: 4px;
}
.wf-runout__text {
  background: #f5f7fa;
  border-radius: 6px;
  padding: 10px;
  font-size: 13px;
  line-height: 1.7;
  white-space: pre-wrap;
  max-height: 220px;
  overflow: auto;
}
</style>
