<template>
  <div class="task-management-container">
    <!-- 页面标题 -->
    <div class="page-header">
      <h2>任务管理</h2>
      <p>展示所有模块正在执行的任务，提供在线设置与管理功能</p>
    </div>

    <!-- 统计卡片 -->
    <div class="stats-section">
      <el-row :gutter="20">
        <el-col :span="6">
          <el-card class="stats-card">
            <div class="card-content">
              <div class="card-icon running">
                <el-icon><VideoPlay /></el-icon>
              </div>
              <div class="card-info">
                <div class="card-title">执行中任务</div>
                <div class="card-value">{{ stats.running }}</div>
              </div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card class="stats-card">
            <div class="card-content">
              <div class="card-icon scheduled">
                <el-icon><Timer /></el-icon>
              </div>
              <div class="card-info">
                <div class="card-title">定时任务</div>
                <div class="card-value">{{ stats.scheduled }}</div>
              </div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card class="stats-card">
            <div class="card-content">
              <div class="card-icon success">
                <el-icon><SuccessFilled /></el-icon>
              </div>
              <div class="card-info">
                <div class="card-title">今日完成</div>
                <div class="card-value">{{ stats.todayCompleted }}</div>
              </div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card class="stats-card">
            <div class="card-content">
              <div class="card-icon failed">
                <el-icon><CircleClose /></el-icon>
              </div>
              <div class="card-info">
                <div class="card-title">今日失败</div>
                <div class="card-value">{{ stats.todayFailed }}</div>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </div>

    <!-- 筛选与操作栏 -->
    <div class="filter-section">
      <el-card>
        <div class="filter-bar">
          <div class="filter-left">
            <el-select v-model="filter.module" placeholder="所属模块" clearable style="width: 160px">
              <el-option label="全部模块" value="" />
              <el-option label="订单模块" value="order" />
              <el-option label="支付模块" value="payment" />
              <el-option label="消息模块" value="message" />
              <el-option label="用户模块" value="userms" />
              <el-option label="商品模块" value="goods" />
              <el-option label="系统模块" value="system" />
            </el-select>
            <el-select v-model="filter.status" placeholder="任务状态" clearable style="width: 140px">
              <el-option label="全部状态" value="" />
              <el-option label="执行中" value="running" />
              <el-option label="已暂停" value="paused" />
              <el-option label="已完成" value="completed" />
              <el-option label="已失败" value="failed" />
            </el-select>
            <el-select v-model="filter.type" placeholder="任务类型" clearable style="width: 140px">
              <el-option label="全部类型" value="" />
              <el-option label="定时任务" value="cron" />
              <el-option label="一次性任务" value="once" />
              <el-option label="循环任务" value="loop" />
            </el-select>
            <el-input v-model="filter.keyword" placeholder="搜索任务名称" clearable style="width: 200px">
              <template #prefix>
                <el-icon><Search /></el-icon>
              </template>
            </el-input>
            <el-button type="primary" @click="handleSearch">
              <el-icon><Search /></el-icon>
              查询
            </el-button>
          </div>
          <div class="filter-right">
            <el-button type="primary" @click="handleRefresh" :loading="loading">
              <el-icon><Refresh /></el-icon>
              刷新
            </el-button>
            <el-button type="success" @click="handleCreateTask">
              <el-icon><Plus /></el-icon>
              新建任务
            </el-button>
          </div>
        </div>
      </el-card>
    </div>

    <!-- 任务列表 -->
    <div class="table-section">
      <el-card>
        <template #header>
          <div class="card-header">
            <span>任务列表</span>
            <el-tag type="info">共 {{ filteredTaskList.length }} 条</el-tag>
          </div>
        </template>

        <el-table
          v-loading="loading"
          :data="filteredTaskList"
          stripe
          border
          style="width: 100%"
          @selection-change="handleSelectionChange"
        >
          <el-table-column type="selection" width="50" />
          <el-table-column prop="id" label="ID" width="80" sortable />
          <el-table-column prop="name" label="任务名称" min-width="180" show-overflow-tooltip />
          <el-table-column prop="module" label="所属模块" width="120">
            <template #default="{ row }">
              <el-tag size="small">{{ row.module }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="type" label="任务类型" width="110">
            <template #default="{ row }">
              <el-tag :type="getTypeTagType(row.type)" size="small" effect="plain">
                {{ getTypeText(row.type) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="status" label="状态" width="100">
            <template #default="{ row }">
              <el-tag :type="getStatusTagType(row.status)" size="small">
                {{ getStatusText(row.status) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="cron" label="执行表达式" width="160" show-overflow-tooltip />
          <el-table-column prop="lastExecuteTime" label="上次执行" width="170" sortable />
          <el-table-column prop="nextExecuteTime" label="下次执行" width="170" sortable />
          <el-table-column prop="enabled" label="启用" width="80" align="center">
            <template #default="{ row }">
              <el-switch
                v-model="row.enabled"
                @change="handleToggleEnabled(row)"
              />
            </template>
          </el-table-column>
          <el-table-column label="操作" width="240" fixed="right">
            <template #default="{ row }">
              <el-button link type="primary" size="small" @click="handleViewDetail(row)">详情</el-button>
              <el-button link type="primary" size="small" @click="handleEdit(row)">编辑</el-button>
              <el-button
                v-if="row.status === 'running'"
                link type="warning" size="small"
                @click="handlePause(row)"
              >暂停</el-button>
              <el-button
                v-if="row.status === 'paused'"
                link type="success" size="small"
                @click="handleResume(row)"
              >恢复</el-button>
              <el-button
                v-if="row.status === 'running' || row.status === 'paused'"
                link type="primary" size="small"
                @click="handleTriggerNow(row)"
              >立即执行</el-button>
              <el-button link type="danger" size="small" @click="handleDelete(row)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>

        <!-- 分页 -->
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

    <!-- 任务详情抽屉 -->
    <el-drawer v-model="detailDrawerVisible" title="任务详情" size="520px">
      <template v-if="currentTask">
        <el-descriptions :column="1" border>
          <el-descriptions-item label="任务ID">{{ currentTask.id }}</el-descriptions-item>
          <el-descriptions-item label="任务名称">{{ currentTask.name }}</el-descriptions-item>
          <el-descriptions-item label="所属模块">{{ currentTask.module }}</el-descriptions-item>
          <el-descriptions-item label="任务类型">
            <el-tag :type="getTypeTagType(currentTask.type)" size="small" effect="plain">
              {{ getTypeText(currentTask.type) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="getStatusTagType(currentTask.status)" size="small">
              {{ getStatusText(currentTask.status) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="执行表达式">{{ currentTask.cron }}</el-descriptions-item>
          <el-descriptions-item label="任务描述">{{ currentTask.description }}</el-descriptions-item>
          <el-descriptions-item label="上次执行时间">{{ currentTask.lastExecuteTime }}</el-descriptions-item>
          <el-descriptions-item label="下次执行时间">{{ currentTask.nextExecuteTime }}</el-descriptions-item>
          <el-descriptions-item label="启用状态">
            <el-tag :type="currentTask.enabled ? 'success' : 'info'" size="small">
              {{ currentTask.enabled ? '已启用' : '已禁用' }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="创建时间">{{ currentTask.createdAt }}</el-descriptions-item>
          <el-descriptions-item label="更新时间">{{ currentTask.updatedAt }}</el-descriptions-item>
        </el-descriptions>
      </template>
    </el-drawer>

    <!-- 新建/编辑任务对话框 -->
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="600px" destroy-on-close>
      <el-form ref="taskFormRef" :model="taskForm" :rules="taskFormRules" label-width="110px">
        <el-form-item label="任务名称" prop="name">
          <el-input v-model="taskForm.name" placeholder="请输入任务名称" />
        </el-form-item>
        <el-form-item label="所属模块" prop="module">
          <el-select v-model="taskForm.module" placeholder="请选择所属模块" style="width: 100%">
            <el-option label="订单模块" value="order" />
            <el-option label="支付模块" value="payment" />
            <el-option label="消息模块" value="message" />
            <el-option label="用户模块" value="userms" />
            <el-option label="商品模块" value="goods" />
            <el-option label="系统模块" value="system" />
          </el-select>
        </el-form-item>
        <el-form-item label="任务类型" prop="type">
          <el-radio-group v-model="taskForm.type">
            <el-radio value="cron">定时任务</el-radio>
            <el-radio value="once">一次性任务</el-radio>
            <el-radio value="loop">循环任务</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="执行表达式" prop="cron">
          <el-input v-model="taskForm.cron" placeholder="如：0 0/30 * * * ?">
            <template #append>
              <el-button @click="showCronHelp = true">说明</el-button>
            </template>
          </el-input>
        </el-form-item>
        <el-form-item label="任务描述">
          <el-input v-model="taskForm.description" type="textarea" :rows="3" placeholder="请输入任务描述" />
        </el-form-item>
        <el-form-item label="启用状态">
          <el-switch v-model="taskForm.enabled" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="handleSubmitTask">确定</el-button>
      </template>
    </el-dialog>

    <!-- Cron 表达式说明对话框 -->
    <el-dialog v-model="showCronHelp" title="Cron 表达式说明" width="500px">
      <el-descriptions :column="1" border size="small">
        <el-descriptions-item label="格式">秒 分 时 日 月 周</el-descriptions-item>
        <el-descriptions-item label="每30分钟">0 0/30 * * * ?</el-descriptions-item>
        <el-descriptions-item label="每小时">0 0 * * * ?</el-descriptions-item>
        <el-descriptions-item label="每天凌晨2点">0 0 2 * * ?</el-descriptions-item>
        <el-descriptions-item label="每周一上午9点">0 0 9 ? * MON</el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script setup>
/**
 * 任务管理页面
 * 展示所有模块正在执行的任务，提供在线设置与管理功能
 */
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  VideoPlay, Timer, SuccessFilled, CircleClose,
  Search, Refresh, Plus
} from '@element-plus/icons-vue'
import request from '@/utils/request'

// 加载状态
const loading = ref(false)
const submitLoading = ref(false)

// 统计数据
const stats = reactive({
  running: 0,
  scheduled: 0,
  todayCompleted: 0,
  todayFailed: 0
})

// 筛选条件
const filter = reactive({
  module: '',
  status: '',
  type: '',
  keyword: ''
})

// 分页参数
const pagination = reactive({
  page: 1,
  pageSize: 20,
  total: 0
})

// 任务列表
const taskList = ref([])

// 多选
const selectedIds = ref([])

// 抽屉相关
const detailDrawerVisible = ref(false)
const currentTask = ref(null)

// 对话框相关
const dialogVisible = ref(false)
const dialogTitle = ref('新建任务')
const taskFormRef = ref(null)
const showCronHelp = ref(false)

/**
 * 任务表单数据
 */
const taskForm = reactive({
  id: null,
  name: '',
  module: '',
  type: 'cron',
  cron: '',
  description: '',
  enabled: true
})

/**
 * 任务表单校验规则
 */
const taskFormRules = {
  name: [{ required: true, message: '请输入任务名称', trigger: 'blur' }],
  module: [{ required: true, message: '请选择所属模块', trigger: 'change' }],
  type: [{ required: true, message: '请选择任务类型', trigger: 'change' }],
  cron: [{ required: true, message: '请输入执行表达式', trigger: 'blur' }]
}

/**
 * 加载任务统计数据
 */
const loadStats = async () => {
  try {
    const res = await request.get('/web/setting/task/stats')
    if (res && res.data) {
      Object.assign(stats, res.data)
    }
  } catch {
    // 接口未就绪时使用模拟数据
    stats.running = 5
    stats.scheduled = 12
    stats.todayCompleted = 89
    stats.todayFailed = 3
  }
}

/**
 * 加载任务列表
 * 使用分页默认值，防止前端未传参导致NPE
 */
const loadTaskList = async () => {
  loading.value = true
  try {
    const page = pagination.page != null ? pagination.page : 1
    const pageSize = pagination.pageSize != null ? pagination.pageSize : 20
    const res = await request.get('/web/setting/task/list', {
      params: {
        page,
        limit: pageSize,
        module: filter.module || undefined,
        status: filter.status || undefined,
        type: filter.type || undefined,
        keyword: filter.keyword || undefined
      }
    })
    if (res && res.data) {
      taskList.value = res.data.list || []
      pagination.total = res.data.total || 0
    }
  } catch {
    // 接口未就绪时使用模拟数据
    taskList.value = [
      { id: 1, name: '订单超时自动关闭', module: '订单模块', type: 'cron', status: 'running', cron: '0 0/10 * * * ?', lastExecuteTime: '2026-06-29 09:30:00', nextExecuteTime: '2026-06-29 09:40:00', enabled: true, description: '每10分钟检查超时未支付订单并自动关闭', createdAt: '2026-01-01 00:00:00', updatedAt: '2026-06-29 09:00:00' },
      { id: 2, name: '每日销售报表生成', module: '系统模块', type: 'cron', status: 'running', cron: '0 0 2 * * ?', lastExecuteTime: '2026-06-29 02:00:00', nextExecuteTime: '2026-06-30 02:00:00', enabled: true, description: '每天凌晨2点生成前一天的销售汇总报表', createdAt: '2026-01-01 00:00:00', updatedAt: '2026-06-28 02:00:00' },
      { id: 3, name: '库存预警检查', module: '商品模块', type: 'loop', status: 'running', cron: '0 0/30 * * * ?', lastExecuteTime: '2026-06-29 09:00:00', nextExecuteTime: '2026-06-29 09:30:00', enabled: true, description: '每30分钟检查库存低于阈值的商品并发送预警', createdAt: '2026-02-01 00:00:00', updatedAt: '2026-06-29 09:00:00' },
      { id: 4, name: '会员积分月度结算', module: '用户模块', type: 'cron', status: 'paused', cron: '0 0 1 1 * ?', lastExecuteTime: '2026-06-01 01:00:00', nextExecuteTime: '2026-07-01 01:00:00', enabled: false, description: '每月1号凌晨1点结算会员过期积分', createdAt: '2026-01-01 00:00:00', updatedAt: '2026-06-01 01:00:00' },
      { id: 5, name: '支付对账文件下载', module: '支付模块', type: 'once', status: 'completed', cron: '-', lastExecuteTime: '2026-06-29 06:00:00', nextExecuteTime: '-', enabled: true, description: '下载微信/支付宝对账文件并进行自动对账', createdAt: '2026-03-01 00:00:00', updatedAt: '2026-06-29 06:00:00' },
      { id: 6, name: '物流轨迹批量同步', module: '订单模块', type: 'loop', status: 'running', cron: '0 0/15 * * * ?', lastExecuteTime: '2026-06-29 09:15:00', nextExecuteTime: '2026-06-29 09:30:00', enabled: true, description: '每15分钟批量同步已发货订单的物流轨迹信息', createdAt: '2026-01-15 00:00:00', updatedAt: '2026-06-29 09:15:00' }
    ]
    pagination.total = 6
  } finally {
    loading.value = false
  }
}

/**
 * 过滤后的任务列表
 */
const filteredTaskList = computed(() => {
  if (!filter.keyword) return taskList.value
  const kw = filter.keyword.toLowerCase()
  return taskList.value.filter(item => item.name.toLowerCase().includes(kw))
})

/**
 * 查询
 */
const handleSearch = () => {
  pagination.page = 1
  loadTaskList()
}

/**
 * 刷新
 */
const handleRefresh = () => {
  loadStats()
  loadTaskList()
}

/**
 * 多选变化
 */
const handleSelectionChange = (rows) => {
  selectedIds.value = rows.map(r => r.id)
}

/**
 * 查看详情
 */
const handleViewDetail = (row) => {
  currentTask.value = row
  detailDrawerVisible.value = true
}

/**
 * 新建任务
 */
const handleCreateTask = () => {
  dialogTitle.value = '新建任务'
  Object.assign(taskForm, { id: null, name: '', module: '', type: 'cron', cron: '', description: '', enabled: true })
  dialogVisible.value = true
}

/**
 * 编辑任务
 */
const handleEdit = (row) => {
  dialogTitle.value = '编辑任务'
  Object.assign(taskForm, {
    id: row.id,
    name: row.name,
    module: row.module,
    type: row.type,
    cron: row.cron,
    description: row.description,
    enabled: row.enabled
  })
  dialogVisible.value = true
}

/**
 * 提交任务表单（新建/编辑）
 */
const handleSubmitTask = async () => {
  if (!taskFormRef.value) return
  await taskFormRef.value.validate()
  submitLoading.value = true
  try {
    const url = taskForm.id ? '/web/setting/task/update' : '/web/setting/task/create'
    await request.post(url, { ...taskForm })
    ElMessage.success(taskForm.id ? '任务更新成功' : '任务创建成功')
    dialogVisible.value = false
    loadTaskList()
    loadStats()
  } catch {
    // 接口未就绪或异常
  } finally {
    submitLoading.value = false
  }
}

/**
 * 暂停任务
 */
const handlePause = async (row) => {
  try {
    await ElMessageBox.confirm(`确定暂停任务「${row.name}」吗？`, '提示', { type: 'warning' })
    await request.post(`/web/setting/task/pause/${row.id}`)
    ElMessage.success('任务已暂停')
    loadTaskList()
  } catch {
    // 用户取消或接口未就绪
  }
}

/**
 * 恢复任务
 */
const handleResume = async (row) => {
  try {
    await ElMessageBox.confirm(`确定恢复任务「${row.name}」吗？`, '提示', { type: 'info' })
    await request.post(`/web/setting/task/resume/${row.id}`)
    ElMessage.success('任务已恢复执行')
    loadTaskList()
  } catch {
    // 用户取消或接口未就绪
  }
}

/**
 * 立即触发任务执行
 */
const handleTriggerNow = async (row) => {
  try {
    await ElMessageBox.confirm(`确定立即执行任务「${row.name}」吗？`, '提示', { type: 'warning' })
    await request.post(`/web/setting/task/trigger/${row.id}`)
    ElMessage.success('任务已触发执行')
    loadTaskList()
    loadStats()
  } catch {
    // 用户取消或接口未就绪
  }
}

/**
 * 切换任务启用/禁用状态
 */
const handleToggleEnabled = async (row) => {
  try {
    await request.post(`/web/setting/task/toggleEnabled/${row.id}`, { enabled: row.enabled })
    ElMessage.success(row.enabled ? '任务已启用' : '任务已禁用')
  } catch {
    row.enabled = !row.enabled
  }
}

/**
 * 删除任务
 */
const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm(`确定删除任务「${row.name}」吗？此操作不可恢复！`, '警告', { type: 'warning' })
    await request.delete(`/web/setting/task/delete/${row.id}`)
    ElMessage.success('任务已删除')
    loadTaskList()
    loadStats()
  } catch {
    // 用户取消或接口未就绪
  }
}

/** 获取状态标签类型 */
const getStatusTagType = (status) => {
  const map = { running: 'primary', paused: 'warning', completed: 'success', failed: 'danger' }
  return map[status] || 'info'
}

/** 获取状态文本 */
const getStatusText = (status) => {
  const map = { running: '执行中', paused: '已暂停', completed: '已完成', failed: '已失败' }
  return map[status] || '未知'
}

/** 获取任务类型标签 */
const getTypeTagType = (type) => {
  const map = { cron: 'primary', once: 'success', loop: 'warning' }
  return map[type] || 'info'
}

/** 获取任务类型文本 */
const getTypeText = (type) => {
  const map = { cron: '定时任务', once: '一次性任务', loop: '循环任务' }
  return map[type] || '未知'
}

onMounted(() => {
  loadStats()
  loadTaskList()
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
.card-icon.running { background-color: #409eff; }
.card-icon.scheduled { background-color: #e6a23c; }
.card-icon.success { background-color: #67c23a; }
.card-icon.failed { background-color: #f56c6c; }
.card-info { flex: 1; }
.card-title { font-size: 14px; color: #909399; margin-bottom: 8px; }
.card-value { font-size: 24px; font-weight: bold; color: #303133; }
.filter-section { margin-bottom: 20px; }
.filter-bar { display: flex; justify-content: space-between; align-items: center; flex-wrap: wrap; gap: 10px; }
.filter-left { display: flex; align-items: center; gap: 10px; flex-wrap: wrap; }
.filter-right { display: flex; align-items: center; gap: 10px; }
.card-header { display: flex; justify-content: space-between; align-items: center; }
.table-section { margin-bottom: 20px; }
.pagination-wrapper { display: flex; justify-content: flex-end; margin-top: 16px; }
</style>