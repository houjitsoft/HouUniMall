<template>
  <div class="database-execute">
    <el-alert
      type="warning"
      show-icon
      :closable="false"
      title="高危操作"
      description="仅超级管理员使用。禁止 DROP DATABASE。建议在执行前备份数据库。生产环境请谨慎操作。"
      class="mb-4"
    />

    <el-row :gutter="16">
      <el-col :span="14">
        <el-card shadow="never">
          <template #header>
            <span>SQL 执行</span>
          </template>
          <el-input
            v-model="sqlText"
            type="textarea"
            :rows="14"
            placeholder="输入 SQL，多条语句以分号分隔"
            class="sql-editor"
          />
          <div class="toolbar">
            <el-checkbox v-model="stopOnError">遇错即停</el-checkbox>
            <el-button type="primary" :loading="executing" @click="handleExecute">
              执行 SQL
            </el-button>
            <el-button @click="sqlText = ''">清空</el-button>
          </div>
        </el-card>
      </el-col>

      <el-col :span="10">
        <el-card shadow="never">
          <template #header>
            <div class="card-header">
              <span>预设补丁</span>
              <el-button type="success" size="small" :loading="importing" @click="handleImportWecom">
                一键导入企微 SCRM
              </el-button>
            </div>
          </template>
          <el-table :data="patches" v-loading="patchLoading" size="small">
            <el-table-column prop="name" label="名称" min-width="120" />
            <el-table-column prop="file" label="文件" min-width="160" show-overflow-tooltip />
            <el-table-column label="状态" width="72">
              <template #default="{ row }">
                <el-tag :type="row.exists ? 'success' : 'danger'" size="small">
                  {{ row.exists ? '就绪' : '缺失' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="130" fixed="right">
              <template #default="{ row }">
                <el-button type="primary" link size="small" @click="loadPatch(row)">载入</el-button>
                <el-button
                  type="success"
                  link
                  size="small"
                  :disabled="!row.exists"
                  :loading="patchRunning === row.id"
                  @click="runPatch(row)"
                >
                  执行
                </el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
    </el-row>

    <el-card v-if="result" shadow="never" class="mt-4">
      <template #header>
        <span>执行结果</span>
        <el-tag class="ml-2" type="info">成功 {{ result.success }} / 失败 {{ result.failed }} / 共 {{ result.total }}</el-tag>
      </template>
      <el-table :data="result.details || []" size="small" max-height="320" row-key="index">
        <el-table-column type="expand" width="40">
          <template #default="{ row }">
            <div v-if="row.rows && row.rows.length" class="query-result-preview">
              <div class="preview-title">查询结果（{{ row.rows.length }} 行）</div>
              <el-table :data="row.rows" size="small" max-height="260">
                <el-table-column
                  v-for="key in getRowKeys(row.rows)"
                  :key="key"
                  :prop="key"
                  :label="key"
                  min-width="120"
                  show-overflow-tooltip
                />
              </el-table>
            </div>
            <el-empty v-else description="无结果数据" :image-size="60" />
          </template>
        </el-table-column>
        <el-table-column prop="index" label="#" width="50" />
        <el-table-column prop="status" label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.status === 'ok' ? 'success' : 'danger'" size="small">
              {{ row.status }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="affected" label="影响" width="70" />
        <el-table-column prop="message" label="错误" min-width="120" show-overflow-tooltip />
        <el-table-column prop="sql" label="SQL" min-width="280" show-overflow-tooltip />
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  listSqlPatches,
  readSqlPatch,
  executeSql,
  executeSqlPatch,
  importWecomScrmSql
} from '@/api/setting/database'

const sqlText = ref('')
const stopOnError = ref(true)
const executing = ref(false)
const importing = ref(false)
const patchLoading = ref(false)
const patchRunning = ref('')
const patches = ref([])
const result = ref(null)

const loadPatches = async () => {
  patchLoading.value = true
  try {
    const list = await listSqlPatches()
    patches.value = Array.isArray(list) ? list : []
  } catch (e) {
    patches.value = []
    console.error('加载 SQL 补丁列表失败', e)
  } finally {
    patchLoading.value = false
  }
}

const loadPatch = async (row) => {
  try {
    const data = await readSqlPatch(row.id)
    sqlText.value = data?.content || ''
    ElMessage.success('已载入: ' + row.name)
  } catch {
    ElMessage.error('载入失败')
  }
}

const getRowKeys = (rows) => {
  if (!Array.isArray(rows) || !rows.length) return []
  return Object.keys(rows[0])
}

const handleExecute = async () => {
  if (!sqlText.value.trim()) {
    ElMessage.warning('请输入 SQL')
    return
  }
  try {
    await ElMessageBox.confirm('确认执行当前 SQL？', '确认', { type: 'warning' })
  } catch {
    return
  }
  executing.value = true
  try {
    const data = await executeSql({
      sql: sqlText.value,
      stopOnError: stopOnError.value
    })
    result.value = data && typeof data === 'object' ? data : null
    ElMessage.success('执行完成')
  } catch (e) {
    ElMessage.error(e?.message || '执行失败')
  } finally {
    executing.value = false
  }
}

const runPatch = async (row) => {
  try {
    await ElMessageBox.confirm(`确认执行补丁「${row.name}」？`, '确认', { type: 'warning' })
  } catch {
    return
  }
  patchRunning.value = row.id
  try {
    const data = await executeSqlPatch(row.id, { stopOnError: stopOnError.value })
    result.value = data && typeof data === 'object' ? data : null
    ElMessage.success('补丁执行完成')
  } catch (e) {
    ElMessage.error(e?.message || '补丁执行失败')
  } finally {
    patchRunning.value = ''
  }
}

const handleImportWecom = async () => {
  try {
    await ElMessageBox.confirm(
      '将依次执行：hou_oa_wecom_scrm_extend.sql、permission_wecom_menus_extend.sql，是否继续？',
      '导入企微 SCRM',
      { type: 'warning' }
    )
  } catch {
    return
  }
  importing.value = true
  try {
    const data = await importWecomScrmSql({ stopOnError: true })
    result.value = data?.runs?.[data.runs.length - 1]?.data || data
    ElMessage.success('企微 SCRM SQL 导入完成')
    await loadPatches()
  } catch (e) {
    ElMessage.error(e?.message || '导入失败')
  } finally {
    importing.value = false
  }
}

onMounted(loadPatches)
</script>

<style scoped>
.database-execute {
  padding: 0;
}
.mb-4 {
  margin-bottom: 16px;
}
.mt-4 {
  margin-top: 16px;
}
.toolbar {
  margin-top: 12px;
  display: flex;
  align-items: center;
  gap: 12px;
}
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.sql-editor :deep(textarea) {
  font-family: Consolas, Monaco, 'Courier New', monospace;
  font-size: 13px;
}
.ml-2 {
  margin-left: 8px;
}
.query-result-preview {
  padding: 12px 16px;
  background: #f8f9fb;
  border-radius: 6px;
}
.preview-title {
  font-size: 13px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 8px;
}
</style>
