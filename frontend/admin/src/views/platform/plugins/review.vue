<template>
  <div class="review-page">
    <header class="page-header">
      <div>
        <h1>发布审核</h1>
        <p>审核待发布版本：通过后立即上架到插件市场，驳回需写明原因（发布方可修改后重新提交）</p>
      </div>
      <div class="header-actions">
        <el-button :icon="Refresh" :loading="loading" @click="loadPending">刷新</el-button>
        <el-button :icon="Upload" @click="goPublish">去上传制品</el-button>
      </div>
    </header>

    <el-alert type="warning" :closable="false" class="scope-tip">
      <template #title>
        审核要点：清单 <code>code</code> / <code>hostService</code> 与架构清单一致；摘要已由服务端重算；
        签名 kid 为当前生效值；更新说明能对上本次改动。任一项不符请驳回，不要「先通过再说」。
      </template>
    </el-alert>

    <el-table v-loading="loading" :data="pending" border stripe empty-text="没有待审核版本">
      <el-table-column prop="pluginCode" label="插件" min-width="150" />
      <el-table-column prop="version" label="版本" width="110" />
      <el-table-column prop="changelog" label="更新说明" min-width="260" show-overflow-tooltip />
      <el-table-column prop="sizeBytes" label="大小" width="110">
        <template #default="{ row }">{{ formatSize(row.sizeBytes) }}</template>
      </el-table-column>
      <el-table-column prop="submitTime" label="提交时间" width="180" />
      <el-table-column prop="submittedBy" label="提交人" width="130">
        <template #default="{ row }">{{ row.submittedBy || '—' }}</template>
      </el-table-column>
      <el-table-column label="操作" width="200" fixed="right">
        <template #default="{ row }">
          <el-button link type="success" :loading="acting === row.versionId" @click="approve(row)">
            通过并上架
          </el-button>
          <el-button link type="danger" :loading="acting === row.versionId" @click="reject(row)">
            驳回
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-card class="history-card" shadow="never">
      <template #header>
        <div class="card-header">
          <span>最近已审核</span>
          <el-button link type="primary" @click="loadHistory">刷新</el-button>
        </div>
      </template>
      <el-table :data="history" border stripe size="small" empty-text="暂无已完成审核的版本">
        <el-table-column prop="pluginCode" label="插件" min-width="140" />
        <el-table-column prop="version" label="版本" width="100" />
        <el-table-column prop="status" label="结果" width="100">
          <template #default="{ row }">
            <el-tag :type="versionStatusOf(row.status).type" size="small">
              {{ versionStatusOf(row.status).text }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="reviewNote" label="审核意见" min-width="220" show-overflow-tooltip />
        <el-table-column prop="updateTime" label="时间" width="180" />
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { Refresh, Upload } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { listVersions, reviewVersion, versionStatusOf } from '@/api/plugin-center/pluginPublish'

const router = useRouter()
const loading = ref(false)
const acting = ref(null)
const pending = ref([])
const history = ref([])

function formatSize (bytes) {
  if (!bytes && bytes !== 0) return '—'
  if (bytes < 1024) return `${bytes} B`
  if (bytes < 1024 * 1024) return `${(bytes / 1024).toFixed(1)} KB`
  return `${(bytes / 1024 / 1024).toFixed(1)} MB`
}

async function loadPending () {
  loading.value = true
  try {
    const list = await listVersions(undefined, 2)
    pending.value = Array.isArray(list) ? list : (list?.list || [])
  } catch (error) {
    ElMessage.error(error?.message || '加载待审列表失败')
  } finally {
    loading.value = false
  }
}

async function loadHistory () {
  try {
    const [published, rejected] = await Promise.all([
      listVersions(undefined, 1),
      listVersions(undefined, 3)
    ])
    const merge = (list) => (Array.isArray(list) ? list : (list?.list || []))
    history.value = [...merge(published), ...merge(rejected)]
      .sort((a, b) => String(b.updateTime || '').localeCompare(String(a.updateTime || '')))
      .slice(0, 20)
  } catch (error) {
    ElMessage.error(error?.message || '加载审核记录失败')
  }
}

async function approve (row) {
  try {
    await ElMessageBox.confirm(
      `通过后 ${row.pluginCode}@${row.version} 会立即上架到插件市场，站点可以购买/安装。确认通过？`,
      '通过审核',
      { confirmButtonText: '通过并上架', cancelButtonText: '取消', type: 'warning' }
    )
  } catch {
    return
  }
  acting.value = row.versionId
  try {
    await reviewVersion(row.versionId, 'approve', '')
    ElMessage.success('已通过并上架')
    await refreshAll()
  } catch (error) {
    ElMessage.error(error?.message || '审核失败')
  } finally {
    acting.value = null
  }
}

async function reject (row) {
  let note = ''
  try {
    const result = await ElMessageBox.prompt('请写明驳回原因，发布方会看到这段说明', '驳回版本', {
      confirmButtonText: '确认驳回',
      cancelButtonText: '取消',
      inputPlaceholder: '如：plugin.yaml 的 hostService 与架构清单不一致',
      inputValidator: (value) => (value && value.trim().length >= 4) || '驳回原因至少 4 个字'
    })
    note = result.value
  } catch {
    return
  }
  acting.value = row.versionId
  try {
    await reviewVersion(row.versionId, 'reject', note)
    ElMessage.success('已驳回')
    await refreshAll()
  } catch (error) {
    ElMessage.error(error?.message || '驳回失败')
  } finally {
    acting.value = null
  }
}

async function refreshAll () {
  await Promise.all([loadPending(), loadHistory()])
}

function goPublish () {
  router.push('/platform/plugins/publish')
}

onMounted(refreshAll)
</script>

<style scoped>
.review-page {
  padding: 16px;
}
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 16px;
}
.page-header h1 {
  margin: 0 0 4px;
  font-size: 20px;
}
.page-header p {
  margin: 0;
  color: var(--el-text-color-secondary);
  font-size: 13px;
}
.scope-tip {
  margin-bottom: 16px;
}
.history-card {
  margin-top: 16px;
}
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>
