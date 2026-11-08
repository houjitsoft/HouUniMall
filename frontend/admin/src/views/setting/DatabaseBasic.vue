<template>
  <div class="database-basic-page">
    <el-card class="box-card">
      <template #header>
        <div class="card-header">
          <span>基本信息</span>
          <div class="header-actions">
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
            <el-button type="primary" :loading="dbLoading" @click="loadDatabases">刷新</el-button>
          </div>
        </div>
      </template>

      <el-row :gutter="16" class="basic-layout">
        <el-col :span="9" class="main-col">
          <el-card shadow="never" class="table-list-card" v-loading="tableLoading">
            <template #header>
              <div class="inner-header">
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
              <el-empty v-if="!tables.length" description="请先在右上角选择数据库" :image-size="60" />
            </div>
          </el-card>
        </el-col>

        <el-col :span="15" class="main-col structure-col">
          <el-card shadow="never" class="column-list-card" v-loading="columnLoading">
            <template #header>
              <div class="inner-header">
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
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Coin, DataLine, SetUp } from '@element-plus/icons-vue'
import { listDatabases, listTables, listColumns } from '@/api/setting/database'

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
      await selectDb(databases.value[0].name)
    } else if (selectedDb.value) {
      await selectDb(selectedDb.value)
    }
  } catch (e) {
    ElMessage.error(e?.message || '加载数据库列表失败')
  } finally {
    dbLoading.value = false
  }
}

const selectDb = async (dbName) => {
  if (!dbName) {
    tables.value = []
    columns.value = []
    selectedTable.value = ''
    return
  }
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

onMounted(loadDatabases)
</script>

<style scoped>
.database-basic-page {
  padding: 20px;
}
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 16px;
}
.header-actions {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-shrink: 0;
}
.db-select-label {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  color: #606266;
  font-size: 13px;
  white-space: nowrap;
}
.db-select {
  width: 200px;
}
.inner-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.basic-layout {
  min-height: 560px;
  align-items: stretch;
}
.main-col {
  display: flex;
}
.main-col > .el-card {
  width: 100%;
}
.structure-col :deep(.el-table) {
  width: 100%;
}
.database-basic-page :deep(.el-card__header) {
  padding: 12px 16px;
}
.database-basic-page :deep(.el-card__body) {
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
@media (max-width: 900px) {
  .card-header {
    flex-direction: column;
    align-items: stretch;
  }
  .header-actions {
    justify-content: flex-end;
    flex-wrap: wrap;
  }
  .db-select {
    width: min(200px, 100%);
  }
}
</style>
