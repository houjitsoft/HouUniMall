<template>
  <div class="task-management-container">
    <div class="page-header">
      <h2>定时任务管理</h2>
      <p>管理平台定时任务（xxl-job 调度），查看执行状态、成功与失败记录</p>
    </div>

    <div class="stats-section">
      <el-row :gutter="20">
        <el-col :span="6">
          <el-card class="stats-card">
            <div class="card-content">
              <div class="card-icon running"><el-icon><VideoPlay /></el-icon></div>
              <div class="card-info">
                <div class="card-title">运行中任务</div>
                <div class="card-value">{{ stats.running }}</div>
              </div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card class="stats-card">
            <div class="card-content">
              <div class="card-icon scheduled"><el-icon><Timer /></el-icon></div>
              <div class="card-info">
                <div class="card-title">任务总数</div>
                <div class="card-value">{{ stats.scheduled }}</div>
              </div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card class="stats-card">
            <div class="card-content">
              <div class="card-icon success"><el-icon><SuccessFilled /></el-icon></div>
              <div class="card-info">
                <div class="card-title">今日成功</div>
                <div class="card-value">{{ stats.todayCompleted }}</div>
              </div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card class="stats-card">
            <div class="card-content">
              <div class="card-icon failed"><el-icon><CircleClose /></el-icon></div>
              <div class="card-info">
                <div class="card-title">今日失败</div>
                <div class="card-value">{{ stats.todayFailed }}</div>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </div>

    <el-tabs v-model="mainTab" class="main-tabs">
      <el-tab-pane label="任务列表" name="tasks">
        <div class="module-tabs-section" v-if="moduleTabs.length">
          <el-tabs v-model="filter.module" class="module-tabs" @tab-change="handleModuleChange">
            <el-tab-pane
              v-for="tab in moduleTabs"
              :key="tab.value"
              :label="formatModuleTabLabel(tab)"
              :name="tab.value"
            />
          </el-tabs>
        </div>
        <div class="filter-section">
          <el-card>
            <div class="filter-bar">
              <div class="filter-left">
                <el-select v-model="filter.status" placeholder="任务状态" clearable style="width: 140px">
                  <el-option label="全部状态" value="" />
                  <el-option label="运行中" value="running" />
                  <el-option label="已暂停" value="paused" />
                </el-select>
                <el-input v-model="filter.keyword" placeholder="搜索任务名称" clearable style="width: 220px">
                  <template #prefix><el-icon><Search /></el-icon></template>
                </el-input>
                <el-button type="primary" @click="handleSearch"><el-icon><Search /></el-icon>查询</el-button>
              </div>
              <div class="filter-right">
                <el-button type="primary" @click="handleRefresh" :loading="loading"><el-icon><Refresh /></el-icon>刷新</el-button>
                <el-button type="success" @click="handleCreateTask"><el-icon><Plus /></el-icon>新建任务</el-button>
              </div>
            </div>
          </el-card>
        </div>

        <div class="table-section">
          <el-card>
            <el-table v-loading="loading" :data="taskList" stripe border style="width: 100%">
              <el-table-column prop="id" label="ID" width="70" />
              <el-table-column prop="name" label="任务名称" min-width="160" show-overflow-tooltip />
              <el-table-column prop="module" label="模块" width="110">
                <template #default="{ row }"><el-tag size="small">{{ row.module }}</el-tag></template>
              </el-table-column>
              <el-table-column prop="status" label="状态" width="100">
                <template #default="{ row }">
                  <el-tag :type="getStatusTagType(row.status)" size="small">{{ getStatusText(row.status) }}</el-tag>
                </template>
              </el-table-column>
              <el-table-column prop="cron" label="Cron表达式" min-width="140" show-overflow-tooltip />
              <el-table-column prop="lastExecuteTime" label="上次执行" width="165" />
              <el-table-column prop="nextExecuteTime" label="下次执行" width="165" />
              <el-table-column prop="enabled" label="启用" width="80" align="center">
                <template #default="{ row }">
                  <el-switch v-model="row.enabled" @change="handleToggleEnabled(row)" />
                </template>
              </el-table-column>
              <el-table-column label="操作" width="260" fixed="right">
                <template #default="{ row }">
                  <el-button link type="primary" size="small" @click="handleViewDetail(row)">详情</el-button>
                  <el-button v-if="row.status === 'running'" link type="warning" size="small" @click="handlePause(row)">暂停</el-button>
                  <el-button v-if="row.status === 'paused'" link type="success" size="small" @click="handleResume(row)">恢复</el-button>
                  <el-button link type="primary" size="small" @click="handleTriggerNow(row)">立即执行</el-button>
                  <el-button link type="danger" size="small" @click="handleDelete(row)">删除</el-button>
                </template>
              </el-table-column>
            </el-table>
            <div class="pagination-wrapper">
              <el-pagination
                v-model:current-page="pagination.page"
                v-model:page-size="pagination.pageSize"
                :page-sizes="[10, 20, 50, 100]"
                :total="pagination.total"
                layout="total, sizes, prev, pager, next, jumper"
                @size-change="loadTaskList"
                @current-change="loadTaskList"
              />
            </div>
          </el-card>
        </div>
      </el-tab-pane>

      <el-tab-pane label="执行日志" name="logs">
        <div class="filter-section">
          <el-card>
            <div class="filter-bar">
              <div class="filter-left">
                <el-select v-model="logFilter.status" placeholder="执行结果" clearable style="width: 140px">
                  <el-option label="全部" value="" />
                  <el-option label="执行中" value="running" />
                  <el-option label="成功" value="success" />
                  <el-option label="失败" value="failed" />
                </el-select>
                <el-input v-model="logFilter.keyword" placeholder="搜索任务名称" clearable style="width: 220px" />
                <el-button type="primary" @click="loadLogList">查询</el-button>
              </div>
              <div class="filter-right">
                <el-button @click="loadLogList" :loading="logLoading">刷新</el-button>
              </div>
            </div>
          </el-card>
        </div>
        <el-card>
          <el-table v-loading="logLoading" :data="logList" stripe border>
            <el-table-column prop="id" label="ID" width="70" />
            <el-table-column prop="jobName" label="任务名称" min-width="160" show-overflow-tooltip />
            <el-table-column prop="invokeTarget" label="调用目标" min-width="200" show-overflow-tooltip />
            <el-table-column prop="status" label="结果" width="100">
              <template #default="{ row }">
                <el-tag :type="getLogStatusType(row.status)" size="small">{{ getLogStatusText(row.status) }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="startTime" label="开始时间" width="165" />
            <el-table-column prop="endTime" label="结束时间" width="165" />
            <el-table-column prop="durationMs" label="耗时(ms)" width="100" />
            <el-table-column prop="errorMsg" label="错误信息" min-width="180" show-overflow-tooltip />
          </el-table>
          <div class="pagination-wrapper">
            <el-pagination
              v-model:current-page="logPagination.page"
              v-model:page-size="logPagination.pageSize"
              :total="logPagination.total"
              layout="total, prev, pager, next"
              @current-change="loadLogList"
            />
          </div>
        </el-card>
      </el-tab-pane>
    </el-tabs>

    <el-drawer v-model="detailDrawerVisible" title="任务详情" size="520px">
      <template v-if="currentTask">
        <el-descriptions :column="1" border>
          <el-descriptions-item label="任务ID">{{ currentTask.id }}</el-descriptions-item>
          <el-descriptions-item label="任务名称">{{ currentTask.name }}</el-descriptions-item>
          <el-descriptions-item label="调用目标">{{ currentTask.invokeTarget }}</el-descriptions-item>
          <el-descriptions-item label="所属模块">{{ currentTask.module }}</el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="getStatusTagType(currentTask.status)" size="small">{{ getStatusText(currentTask.status) }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="Cron表达式">{{ currentTask.cron }}</el-descriptions-item>
          <el-descriptions-item label="描述">{{ currentTask.description || '-' }}</el-descriptions-item>
          <el-descriptions-item label="上次执行">{{ currentTask.lastExecuteTime }}</el-descriptions-item>
          <el-descriptions-item label="下次执行">{{ currentTask.nextExecuteTime }}</el-descriptions-item>
        </el-descriptions>
      </template>
    </el-drawer>

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="620px" destroy-on-close>
      <el-form ref="taskFormRef" :model="taskForm" :rules="taskFormRules" label-width="110px">
        <el-form-item label="任务名称" prop="name">
          <el-input v-model="taskForm.name" placeholder="请输入任务名称" />
        </el-form-item>
        <el-form-item label="调用目标" prop="invokeTarget">
          <el-input v-model="taskForm.invokeTarget" placeholder="如 com.houjiang.job.job.XXXJob" />
        </el-form-item>
        <el-form-item label="Cron表达式" prop="cron">
          <el-input v-model="taskForm.cron" placeholder="如：0 0/1 * * * ?" />
        </el-form-item>
        <el-form-item label="任务描述">
          <el-input v-model="taskForm.description" type="textarea" :rows="3" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="handleSubmitTask">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { VideoPlay, Timer, SuccessFilled, CircleClose, Search, Refresh, Plus } from '@element-plus/icons-vue'
import request from '@/utils/request'

const loading = ref(false)
const logLoading = ref(false)
const submitLoading = ref(false)
const mainTab = ref('tasks')
const stats = reactive({ running: 0, scheduled: 0, todayCompleted: 0, todayFailed: 0 })
const filter = reactive({ status: '', keyword: '', module: 'ALL' })
const moduleTabs = ref([])
const logFilter = reactive({ status: '', keyword: '' })
const pagination = reactive({ page: 1, pageSize: 20, total: 0 })
const logPagination = reactive({ page: 1, pageSize: 20, total: 0 })
const taskList = ref([])
const logList = ref([])
const detailDrawerVisible = ref(false)
const currentTask = ref(null)
const dialogVisible = ref(false)
const dialogTitle = ref('新建任务')
const taskFormRef = ref(null)
const taskForm = reactive({ id: null, name: '', invokeTarget: '', cron: '', description: '' })
const taskFormRules = {
  name: [{ required: true, message: '请输入任务名称', trigger: 'blur' }],
  invokeTarget: [{ required: true, message: '请输入调用目标', trigger: 'blur' }],
  cron: [{ required: true, message: '请输入Cron表达式', trigger: 'blur' }]
}

const loadStats = async () => {
  const res = await request.get('/web/setting/task/stats')
  if (res?.data) Object.assign(stats, res.data)
}

const loadModuleTabs = async () => {
  try {
    const res = await request.get('/web/setting/task/modules')
    moduleTabs.value = Array.isArray(res?.data) ? res.data : []
  } catch {
    moduleTabs.value = [{ name: '全部', value: 'ALL', count: 0 }]
  }
}

const formatModuleTabLabel = (tab) => {
  if (tab.count == null) return tab.name
  return `${tab.name} (${tab.count})`
}

const loadTaskList = async () => {
  loading.value = true
  try {
    const res = await request.get('/web/setting/task/list', {
      params: {
        page: pagination.page,
        limit: pagination.pageSize,
        status: filter.status || undefined,
        keyword: filter.keyword || undefined,
        module: filter.module && filter.module !== 'ALL' ? filter.module : undefined
      }
    })
    if (res?.data) {
      taskList.value = res.data.list || []
      pagination.total = res.data.totalCount || res.data.total || 0
    }
  } catch (e) {
    ElMessage.error(e?.message || '加载任务列表失败')
  } finally {
    loading.value = false
  }
}

const loadLogList = async () => {
  logLoading.value = true
  try {
    const res = await request.get('/web/setting/task/log/list', {
      params: {
        page: logPagination.page,
        limit: logPagination.pageSize,
        status: logFilter.status || undefined,
        keyword: logFilter.keyword || undefined
      }
    })
    if (res?.data) {
      logList.value = res.data.list || []
      logPagination.total = res.data.totalCount || 0
    }
  } catch (e) {
    ElMessage.error(e?.message || '加载执行日志失败')
  } finally {
    logLoading.value = false
  }
}

const handleSearch = () => { pagination.page = 1; loadTaskList() }
const handleModuleChange = () => { pagination.page = 1; loadTaskList() }
const handleRefresh = () => { loadStats(); loadModuleTabs(); loadTaskList(); loadLogList() }
const handleViewDetail = (row) => { currentTask.value = row; detailDrawerVisible.value = true }
const handleCreateTask = () => {
  dialogTitle.value = '新建任务'
  Object.assign(taskForm, { id: null, name: '', invokeTarget: '', cron: '', description: '' })
  dialogVisible.value = true
}

const handleSubmitTask = async () => {
  if (!taskFormRef.value) return
  await taskFormRef.value.validate()
  submitLoading.value = true
  try {
    const url = taskForm.id ? '/web/setting/task/update' : '/web/setting/task/create'
    await request.post(url, { ...taskForm })
    ElMessage.success(taskForm.id ? '更新成功' : '创建成功')
    dialogVisible.value = false
    loadModuleTabs()
    loadTaskList()
    loadStats()
  } catch (e) {
    ElMessage.error(e?.message || '操作失败')
  } finally {
    submitLoading.value = false
  }
}

const handlePause = async (row) => {
  await ElMessageBox.confirm(`确定暂停任务「${row.name}」吗？`, '提示', { type: 'warning' })
  await request.post(`/web/setting/task/pause/${row.id}`)
  ElMessage.success('已暂停')
  loadTaskList()
}

const handleResume = async (row) => {
  await request.post(`/web/setting/task/resume/${row.id}`)
  ElMessage.success('已恢复')
  loadTaskList()
}

const handleTriggerNow = async (row) => {
  await ElMessageBox.confirm(`确定立即执行任务「${row.name}」吗？`, '提示', { type: 'warning' })
  await request.post(`/web/setting/task/trigger/${row.id}`)
  ElMessage.success('已触发执行')
  loadStats()
  loadLogList()
}

const handleToggleEnabled = async (row) => {
  try {
    await request.post(`/web/setting/task/toggleEnabled/${row.id}`, { enabled: row.enabled })
    ElMessage.success(row.enabled ? '已启用' : '已禁用')
    loadTaskList()
  } catch {
    row.enabled = !row.enabled
  }
}

const handleDelete = async (row) => {
  await ElMessageBox.confirm(`确定删除任务「${row.name}」吗？`, '警告', { type: 'warning' })
  await request.delete(`/web/setting/task/delete/${row.id}`)
  ElMessage.success('已删除')
  loadModuleTabs()
  loadTaskList()
  loadStats()
}

const getStatusTagType = (s) => ({ running: 'primary', paused: 'warning' }[s] || 'info')
const getStatusText = (s) => ({ running: '运行中', paused: '已暂停' }[s] || s)
const getLogStatusType = (s) => ({ running: 'primary', success: 'success', failed: 'danger' }[s] || 'info')
const getLogStatusText = (s) => ({ running: '执行中', success: '成功', failed: '失败' }[s] || s)

onMounted(() => {
  loadStats()
  loadModuleTabs()
  loadTaskList()
  loadLogList()
})
</script>

<style scoped>
.task-management-container { padding: 20px; }
.page-header { margin-bottom: 20px; }
.page-header h2 { margin: 0 0 8px 0; color: #303133; }
.page-header p { margin: 0; color: #606266; font-size: 14px; }
.stats-section { margin-bottom: 20px; }
.stats-card { height: 120px; }
.card-content { display: flex; align-items: center; height: 100%; }
.card-icon {
  width: 60px; height: 60px; border-radius: 50%;
  display: flex; align-items: center; justify-content: center;
  margin-right: 20px; font-size: 24px; color: #fff;
}
.card-icon.running { background: #409eff; }
.card-icon.scheduled { background: #e6a23c; }
.card-icon.success { background: #67c23a; }
.card-icon.failed { background: #f56c6c; }
.card-title { font-size: 14px; color: #909399; margin-bottom: 8px; }
.card-value { font-size: 24px; font-weight: bold; color: #303133; }
.filter-section { margin-bottom: 16px; }
.module-tabs-section { margin-bottom: 12px; }
.module-tabs :deep(.el-tabs__header) { margin-bottom: 0; }
.module-tabs :deep(.el-tabs__nav-wrap::after) { height: 1px; }
.filter-bar { display: flex; justify-content: space-between; flex-wrap: wrap; gap: 10px; }
.filter-left, .filter-right { display: flex; align-items: center; gap: 10px; flex-wrap: wrap; }
.pagination-wrapper { display: flex; justify-content: flex-end; margin-top: 16px; }
.main-tabs { margin-top: 8px; }
</style>
