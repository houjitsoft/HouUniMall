<template>
  <div class="system-database">
    <el-card class="box-card">
      <template #header>
        <div class="card-header">
          <span>数据库管理</span>
          <el-button type="primary" @click="showAddDialog">新增连接</el-button>
        </div>
      </template>
      
      <el-tabs v-model="activeTab">
        <el-tab-pane label="基本信息" name="basic">
          <template #label>
            <span class="tab-label"><el-icon><Document /></el-icon> 基本信息</span>
          </template>
          <div class="tab-content database-basic">
            <div class="basic-toolbar">
              <span class="db-select-label"><el-icon><Coin /></el-icon> 数据库</span>
              <el-select
                v-model="selectedDb"
                class="db-select"
                filterable
                placeholder="选择数据库"
                :loading="dbLoading"
                @change="selectDb"
              >
                <el-option
                  v-for="db in databases"
                  :key="db.name"
                  :label="db.name"
                  :value="db.name"
                />
              </el-select>
              <el-button type="primary" link :loading="dbLoading" @click="loadDatabases">刷新库列表</el-button>
            </div>
            <el-row :gutter="16" class="basic-layout">
              <el-col :span="9">
                <el-card shadow="never" class="table-list-card" v-loading="tableLoading">
                  <template #header>
                    <div class="card-header">
                      <span><el-icon><SetUp /></el-icon> 数据表</span>
                      <span class="header-sub">共 {{ tables.length }} 张</span>
                    </div>
                  </template>
                  <div class="table-list">
                    <div
                      v-for="t in tables"
                      :key="t.tableName"
                      class="table-item"
                      :class="{ active: selectedTable === t.tableName }"
                      @click="selectTable(t.tableName)"
                    >
                      <div class="table-main">
                        <span class="table-name" :title="t.tableName">{{ t.tableName }}</span>
                        <span v-if="t.tableComment" class="table-comment" :title="t.tableComment">{{ t.tableComment }}</span>
                      </div>
                      <div class="table-meta">
                        <el-tag size="small" type="info">{{ formatBytes(t.dataLength) }}</el-tag>
                        <el-tag size="small" type="success">{{ t.tableRows || 0 }} 行</el-tag>
                      </div>
                    </div>
                    <el-empty v-if="!tables.length" description="请先选择数据库" :image-size="60" />
                  </div>
                </el-card>
              </el-col>

              <el-col :span="15">
                <el-card shadow="never" class="column-list-card" v-loading="columnLoading">
                  <template #header>
                    <div class="card-header">
                      <span><el-icon><DataLine /></el-icon> 表结构</span>
                      <span v-if="selectedTable" class="header-sub">{{ selectedTable }}</span>
                    </div>
                  </template>
                  <el-table v-if="columns.length" :data="columns" size="small" max-height="520">
                    <el-table-column prop="columnName" label="字段名" min-width="140" show-overflow-tooltip />
                    <el-table-column prop="dataType" label="类型" width="110" />
                    <el-table-column prop="isNullable" label="可空" width="70" />
                    <el-table-column prop="columnKey" label="键" width="80" />
                    <el-table-column prop="columnComment" label="注释" min-width="160" show-overflow-tooltip />
                  </el-table>
                  <el-empty v-else description="请选择数据表" :image-size="60" />
                </el-card>
              </el-col>
            </el-row>
          </div>
        </el-tab-pane>

        <el-tab-pane label="查询执行" name="execute" lazy>
          <template #label>
            <span class="tab-label"><el-icon><DataLine /></el-icon> 查询执行</span>
          </template>
          <div class="tab-content">
            <DatabaseExecute />
          </div>
        </el-tab-pane>

        <el-tab-pane label="备份管理" name="backup">
          <div class="tab-content">
            <div class="toolbar">
              <el-button type="primary" @click="createBackup">创建备份</el-button>
              <el-button @click="refreshBackups">刷新</el-button>
            </div>
            
            <el-table :data="backups" style="width: 100%">
              <el-table-column prop="name" label="备份名称" min-width="200" />
              <el-table-column prop="size" label="大小" width="100" />
              <el-table-column prop="createTime" label="创建时间" width="180" />
              <el-table-column prop="status" label="状态" width="100">
                <template #default="{ row }">
                  <el-tag :type="row.status === 'success' ? 'success' : 'danger'">
                    {{ row.status === 'success' ? '成功' : '失败' }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column label="操作" width="200">
                <template #default="{ row }">
                  <el-button size="small" type="primary" link @click="downloadBackup(row)">
                    下载
                  </el-button>
                  <el-button size="small" type="danger" link @click="deleteBackup(row)">
                    删除
                  </el-button>
                </template>
              </el-table-column>
            </el-table>
          </div>
        </el-tab-pane>

        <el-tab-pane label="性能监控" name="monitor">
          <div class="tab-content">
            <el-row :gutter="20">
              <el-col :span="8">
                <el-card>
                  <template #header>
                    <div class="card-header">
                      <span>连接数</span>
                    </div>
                  </template>
                  <div class="metric-value">{{ metrics.connectionCount }}</div>
                  <div class="metric-desc">当前连接数</div>
                </el-card>
              </el-col>
              <el-col :span="8">
                <el-card>
                  <template #header>
                    <div class="card-header">
                      <span>QPS</span>
                    </div>
                  </template>
                  <div class="metric-value">{{ metrics.qps }}</div>
                  <div class="metric-desc">每秒查询数</div>
                </el-card>
              </el-col>
              <el-col :span="8">
                <el-card>
                  <template #header>
                    <div class="card-header">
                      <span>慢查询</span>
                    </div>
                  </template>
                  <div class="metric-value">{{ metrics.slowQueryCount }}</div>
                  <div class="metric-desc">慢查询数量</div>
                </el-card>
              </el-col>
            </el-row>
            
            <div class="chart-container">
              <div ref="chartRef" style="width: 100%; height: 300px;"></div>
            </div>
          </div>
        </el-tab-pane>
      </el-tabs>
    </el-card>
    
    <!-- 新增/编辑连接对话框 -->
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="600px">
      <el-form :model="connectionForm" label-width="100px">
        <el-form-item label="连接名称" required>
          <el-input v-model="connectionForm.name" placeholder="请输入连接名称" />
        </el-form-item>
        <el-form-item label="数据库类型" required>
          <el-select v-model="connectionForm.type" placeholder="请选择数据库类型">
            <el-option label="MySQL" value="mysql" />
            <el-option label="PostgreSQL" value="postgresql" />
            <el-option label="Oracle" value="oracle" />
            <el-option label="SQL Server" value="sqlserver" />
          </el-select>
        </el-form-item>
        <el-form-item label="主机地址" required>
          <el-input v-model="connectionForm.host" placeholder="请输入主机地址" />
        </el-form-item>
        <el-form-item label="端口" required>
          <el-input-number v-model="connectionForm.port" :min="1" :max="65535" />
        </el-form-item>
        <el-form-item label="数据库名" required>
          <el-input v-model="connectionForm.database" placeholder="请输入数据库名" />
        </el-form-item>
        <el-form-item label="用户名" required>
          <el-input v-model="connectionForm.username" placeholder="请输入用户名" />
        </el-form-item>
        <el-form-item label="密码" required>
          <el-input v-model="connectionForm.password" type="password" placeholder="请输入密码" />
        </el-form-item>
        <el-form-item label="连接池大小">
          <el-input-number v-model="connectionForm.poolSize" :min="1" :max="100" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveConnection">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Coin, Document, DataLine, SetUp } from '@element-plus/icons-vue'
import DatabaseExecute from './DatabaseExecute.vue'
import { listDatabases, listTables, listColumns } from '@/api/setting/database'

const activeTab = ref('basic')
const dialogVisible = ref(false)
const dialogTitle = ref('新增连接')
const isEdit = ref(false)

// 连接数据
const connections = ref([
  {
    id: 1,
    name: '主数据库',
    type: 'mysql',
    host: '192.168.1.100',
    port: 3306,
    database: 'houcloud',
    username: 'root',
    password: '******',
    poolSize: 20,
    status: 'connected'
  },
  {
    id: 2,
    name: '日志数据库',
    type: 'mysql',
    host: '192.168.1.101',
    port: 3306,
    database: 'houcloud_log',
    username: 'logger',
    password: '******',
    poolSize: 10,
    status: 'connected'
  }
])

// 备份数据
const backups = ref([
  {
    id: 1,
    name: 'houcloud_backup_20240108.sql',
    size: '2.5GB',
    createTime: '2024-01-08 02:00:00',
    status: 'success'
  },
  {
    id: 2,
    name: 'houcloud_log_backup_20240108.sql',
    size: '1.2GB',
    createTime: '2024-01-08 03:00:00',
    status: 'success'
  }
])

// 性能指标
const metrics = reactive({
  connectionCount: 15,
  qps: 1250,
  slowQueryCount: 3
})

// 连接表单
const connectionForm = reactive({
  id: null,
  name: '',
  type: 'mysql',
  host: '',
  port: 3306,
  database: '',
  username: '',
  password: '',
  poolSize: 10
})

// 数据库基本信息
const databases = ref([])
const tables = ref([])
const columns = ref([])
const selectedDb = ref('')
const selectedTable = ref('')
const dbLoading = ref(false)
const tableLoading = ref(false)
const columnLoading = ref(false)

const loadDatabases = async () => {
  dbLoading.value = true
  try {
    databases.value = await listDatabases()
    if (databases.value.length && !selectedDb.value) {
      selectDb(databases.value[0].name)
    }
  } catch (e) {
    ElMessage.error(e?.message || '加载数据库列表失败')
  } finally {
    dbLoading.value = false
  }
}

const selectDb = async (dbName) => {
  selectedDb.value = dbName
  selectedTable.value = ''
  columns.value = []
  tableLoading.value = true
  try {
    tables.value = await listTables(dbName)
  } catch (e) {
    ElMessage.error(e?.message || '加载表列表失败')
    tables.value = []
  } finally {
    tableLoading.value = false
  }
}

const selectTable = async (tableName) => {
  selectedTable.value = tableName
  columnLoading.value = true
  try {
    columns.value = await listColumns(selectedDb.value, tableName)
  } catch (e) {
    ElMessage.error(e?.message || '加载表结构失败')
    columns.value = []
  } finally {
    columnLoading.value = false
  }
}

const formatBytes = (bytes) => {
  const num = Number(bytes) || 0
  if (num === 0) return '0 B'
  const k = 1024
  const sizes = ['B', 'KB', 'MB', 'GB', 'TB']
  const i = Math.floor(Math.log(num) / Math.log(k))
  return (num / Math.pow(k, i)).toFixed(2) + ' ' + sizes[i]
}

const showAddDialog = () => {
  dialogTitle.value = '新增连接'
  isEdit.value = false
  resetForm()
  dialogVisible.value = true
}

const editConnection = (row) => {
  dialogTitle.value = '编辑连接'
  isEdit.value = true
  Object.assign(connectionForm, row)
  dialogVisible.value = true
}

const deleteConnection = async (row) => {
  try {
    await ElMessageBox.confirm(`确定要删除连接 "${row.name}" 吗？`, '确认删除', {
      type: 'warning'
    })
    ElMessage.success('删除成功')
  } catch {
    ElMessage.info('已取消删除')
  }
}

const testConnection = (row) => {
  ElMessage.success(`正在测试连接 "${row.name}"...`)
}

const saveConnection = () => {
  if (!connectionForm.name || !connectionForm.host || !connectionForm.database || !connectionForm.username) {
    ElMessage.warning('请填写完整信息')
    return
  }
  
  if (isEdit.value) {
    ElMessage.success('连接信息更新成功')
  } else {
    connectionForm.id = Date.now()
    connectionForm.status = 'connected'
    connections.value.push({ ...connectionForm })
    ElMessage.success('连接信息添加成功')
  }
  
  dialogVisible.value = false
}

const createBackup = () => {
  ElMessage.success('开始创建数据库备份...')
}

const refreshBackups = () => {
  ElMessage.success('备份列表已刷新')
}

const downloadBackup = (row) => {
  ElMessage.success(`正在下载备份文件 "${row.name}"...`)
}

const deleteBackup = async (row) => {
  try {
    await ElMessageBox.confirm(`确定要删除备份 "${row.name}" 吗？`, '确认删除', {
      type: 'warning'
    })
    ElMessage.success('备份删除成功')
  } catch {
    ElMessage.info('已取消删除')
  }
}

const resetForm = () => {
  Object.assign(connectionForm, {
    id: null,
    name: '',
    type: 'mysql',
    host: '',
    port: 3306,
    database: '',
    username: '',
    password: '',
    poolSize: 10
  })
}

onMounted(() => {
  // 初始化图表
  console.log('初始化数据库监控图表')
  loadDatabases()
})
</script>

<style scoped>
.system-database {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.tab-content {
  padding: 20px 0;
}

.toolbar {
  margin-bottom: 20px;
}

.metric-value {
  font-size: 24px;
  font-weight: bold;
  text-align: center;
  color: #303133;
}

.metric-desc {
  font-size: 14px;
  text-align: center;
  color: #909399;
  margin-top: 10px;
}

.chart-container {
  margin-top: 20px;
  padding: 20px;
  background: #f5f7fa;
  border-radius: 4px;
}

.database-basic .basic-toolbar {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  gap: 10px;
  margin-bottom: 12px;
}
.database-basic .db-select-label {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  color: #606266;
  font-size: 13px;
  white-space: nowrap;
}
.database-basic .db-select {
  width: 200px;
}
.database-basic .basic-layout {
  min-height: 560px;
}
.database-basic :deep(.el-card__header) {
  padding: 12px 16px;
}
.database-basic :deep(.el-card__body) {
  padding: 12px;
}
.table-list-card, .column-list-card {
  height: 100%;
}
.table-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
  max-height: 520px;
  overflow-y: auto;
}
.table-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 12px;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s;
  border: 1px solid transparent;
}
.table-item:hover {
  background: #f5f7fa;
  border-color: #e4e7ed;
}
.table-item.active {
  background: #ecf5ff;
  border-color: #409eff;
  color: #409eff;
}
.table-name {
  flex: 1;
  min-width: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  font-weight: 500;
}
.table-main {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 4px;
}
.table-comment {
  font-size: 12px;
  color: #909399;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.table-meta {
  display: flex;
  gap: 6px;
  flex-shrink: 0;
}
.header-sub {
  font-size: 12px;
  color: #909399;
  font-weight: normal;
}
.tab-label {
  display: inline-flex;
  align-items: center;
  gap: 4px;
}
</style>