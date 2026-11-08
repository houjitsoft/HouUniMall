<template>
  <div class="publish-page">
    <header class="page-header">
      <div>
        <h1>插件发布</h1>
        <p>上传制品 → 提交审核 → 平台审核通过后自动上架，出现在插件市场供站点购买与安装</p>
      </div>
      <div class="header-actions">
        <el-button :icon="Refresh" :loading="loading" @click="loadVersions">刷新</el-button>
        <el-button :icon="Histogram" @click="goMarket">去插件市场</el-button>
      </div>
    </header>

    <el-alert type="info" :closable="false" class="scope-tip">
      <template #title>
        只有平台管理员可以发布。制品需带 <code>plugin.yaml</code>（含 <code>code</code> /
        <code>hostService</code> / <code>version</code>），上传时会重算摘要并按当前 kid 签名；
        未通过校验的文件不会被登记为任何版本。
      </template>
    </el-alert>

    <el-card class="upload-card" shadow="never">
      <template #header>
        <div class="card-header">
          <span>上传新版本</span>
          <span class="hint">jar 单文件，建议 ≤ 64MB（超出上限会被拒）</span>
        </div>
      </template>

      <el-form ref="formRef" :model="form" :rules="rules" label-width="96px" class="upload-form">
        <el-form-item label="插件编码" prop="pluginCode">
          <el-input
            v-model="form.pluginCode"
            placeholder="如 oa / finance / crm（必须与包内 plugin.yaml 的 code 一致）"
            clearable
          />
        </el-form-item>
        <el-form-item label="版本号" prop="version">
          <el-input v-model="form.version" placeholder="如 1.2.0（语义化版本，必须与包内一致）" clearable />
        </el-form-item>
        <el-form-item label="更新说明">
          <el-input
            v-model="form.changelog"
            type="textarea"
            :rows="3"
            maxlength="1000"
            show-word-limit
            placeholder="本次版本改了什么（会展示在审核页与市场详情）"
          />
        </el-form-item>
        <el-form-item label="制品文件" prop="file">
          <el-upload
            drag
            :auto-upload="false"
            :limit="1"
            accept=".jar"
            :on-change="onFileChange"
            :on-remove="onFileRemove"
            :file-list="fileList"
          >
            <el-icon class="el-icon--upload"><UploadFilled /></el-icon>
            <div class="el-upload__text">拖入 jar 或<em>点击选择</em></div>
            <template #tip>
              <div class="el-upload__tip">上传后会重算 MD5 / SHA-256，摘要不一致直接被拒</div>
            </template>
          </el-upload>
        </el-form-item>

        <el-form-item>
          <el-button type="primary" :loading="uploading" :disabled="uploading" @click="doUpload">
            {{ uploading ? '上传中…' : '上传并创建草稿' }}
          </el-button>
          <el-button @click="resetForm">重置</el-button>
        </el-form-item>
      </el-form>

      <el-alert
        v-if="uploadIssues.length"
        type="error"
        :closable="false"
        title="上传校验未通过"
        class="issues"
      >
        <ul class="issue-list">
          <li v-for="(issue, index) in uploadIssues" :key="index">
            <strong>{{ issue.field || '校验' }}</strong>：{{ issue.message || issue }}
          </li>
        </ul>
      </el-alert>
    </el-card>

    <el-card class="list-card" shadow="never">
      <template #header>
        <div class="card-header">
          <span>版本列表</span>
          <div class="filters">
            <el-input v-model="filterCode" placeholder="按插件编码筛选" clearable style="width: 220px" />
            <el-select v-model="filterStatus" placeholder="全部状态" clearable style="width: 150px">
              <el-option label="草稿" :value="0" />
              <el-option label="待审核" :value="2" />
              <el-option label="已发布" :value="1" />
              <el-option label="已驳回" :value="3" />
              <el-option label="已下架" :value="4" />
            </el-select>
            <el-button :icon="Search" @click="loadVersions">查询</el-button>
          </div>
        </div>
      </template>

      <el-table v-loading="loading" :data="versions" border stripe empty-text="暂无版本记录">
        <el-table-column prop="pluginCode" label="插件" min-width="150" />
        <el-table-column prop="version" label="版本" width="120" />
        <el-table-column prop="status" label="状态" width="110">
          <template #default="{ row }">
            <el-tag :type="versionStatusOf(row.status).type" size="small">
              {{ versionStatusOf(row.status).text }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="changelog" label="更新说明" min-width="220" show-overflow-tooltip />
        <el-table-column prop="sizeBytes" label="大小" width="110">
          <template #default="{ row }">{{ formatSize(row.sizeBytes) }}</template>
        </el-table-column>
        <el-table-column prop="reviewNote" label="审核意见" min-width="180" show-overflow-tooltip />
        <el-table-column prop="updateTime" label="更新时间" width="180" />
        <el-table-column label="操作" width="230" fixed="right">
          <template #default="{ row }">
            <el-button
              v-if="row.status === 0 || row.status === 3"
              link
              type="primary"
              @click="doSubmit(row)"
            >
              提交审核
            </el-button>
            <el-button
              v-if="row.status === 1"
              link
              type="warning"
              @click="doOffline(row)"
            >
              下架
            </el-button>
            <el-button
              v-if="row.status === 4"
              link
              type="success"
              @click="doRelist(row)"
            >
              重新上架
            </el-button>
            <span v-if="row.status === 2" class="muted">等待审核</span>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { Histogram, Refresh, Search, UploadFilled } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  listVersions,
  offlineVersion,
  relistVersion,
  submitVersion,
  uploadArtifact,
  versionStatusOf
} from '@/api/plugin-center/pluginPublish'

const router = useRouter()

const loading = ref(false)
const uploading = ref(false)
const versions = ref([])
const uploadIssues = ref([])
const filterCode = ref('')
const filterStatus = ref('')

const formRef = ref()
const form = reactive({ pluginCode: '', version: '', changelog: '' })
const fileList = ref([])
const fileRef = ref(null)
// el-upload 的 on-change 只给 raw 文件，单独存一份便于提交
const rules = {
  pluginCode: [{ required: true, message: '请填写插件编码', trigger: 'blur' }],
  version: [{ required: true, message: '请填写版本号', trigger: 'blur' }]
}

function onFileChange (uploadFile) {
  fileRef.value = uploadFile?.raw || null
  fileList.value = uploadFile ? [uploadFile] : []
}

function onFileRemove () {
  fileRef.value = null
  fileList.value = []
}

function resetForm () {
  form.pluginCode = ''
  form.version = ''
  form.changelog = ''
  fileRef.value = null
  fileList.value = []
  uploadIssues.value = []
  formRef.value?.clearValidate()
}

function formatSize (bytes) {
  if (!bytes && bytes !== 0) return '—'
  if (bytes < 1024) return `${bytes} B`
  if (bytes < 1024 * 1024) return `${(bytes / 1024).toFixed(1)} KB`
  return `${(bytes / 1024 / 1024).toFixed(1)} MB`
}

async function doUpload () {
  uploadIssues.value = []
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return
  if (!fileRef.value) {
    ElMessage.warning('请选择要上传的 jar 文件')
    return
  }
  uploading.value = true
  const submittedCode = form.pluginCode.trim()
  const submittedVersion = form.version.trim()
  try {
    await uploadArtifact({
      file: fileRef.value,
      pluginCode: submittedCode,
      version: submittedVersion,
      changelog: form.changelog.trim()
    })
    ElMessage.success(`已上传 ${submittedCode}@${submittedVersion}，当前状态为草稿`)
    resetForm()
    await loadVersions()
    // 顺手提示下一步：草稿必须提交审核才会进市场
    ElMessageBox.confirm('草稿已创建，是否现在提交审核？', '下一步', {
      confirmButtonText: '提交审核',
      cancelButtonText: '稍后',
      type: 'info'
    })
      .then(() => doSubmit({ pluginCode: submittedCode, version: submittedVersion }))
      .catch(() => {})
  } catch (error) {
    const payload = error?.response?.data?.data
    uploadIssues.value = payload?.validations || []
    ElMessage.error(error?.message || '上传失败')
  } finally {
    uploading.value = false
  }
}

async function loadVersions () {
  loading.value = true
  try {
    const list = await listVersions(filterCode.value.trim() || undefined, filterStatus.value)
    versions.value = Array.isArray(list) ? list : (list?.list || [])
  } catch (error) {
    ElMessage.error(error?.message || '加载版本列表失败')
  } finally {
    loading.value = false
  }
}

async function doSubmit (row) {
  try {
    await submitVersion(row.pluginCode, row.version)
    ElMessage.success('已提交审核，等待平台审核通过后自动上架')
    await loadVersions()
  } catch (error) {
    ElMessage.error(error?.message || '提交审核失败')
  }
}

async function doOffline (row) {
  let reason = ''
  try {
    const result = await ElMessageBox.prompt('下架后市场不再展示、也不可安装（已安装的实例不受影响）', '下架版本', {
      confirmButtonText: '确认下架',
      cancelButtonText: '取消',
      inputPlaceholder: '下架原因（可空）'
    })
    reason = result?.value || ''
  } catch {
    return
  }
  try {
    await offlineVersion(row.pluginCode, row.version, reason)
    ElMessage.success('已下架')
    await loadVersions()
  } catch (error) {
    ElMessage.error(error?.message || '下架失败')
  }
}

async function doRelist (row) {
  try {
    await relistVersion(row.pluginCode, row.version)
    ElMessage.success('已重新上架')
    await loadVersions()
  } catch (error) {
    ElMessage.error(error?.message || '重新上架失败')
  }
}

function goMarket () {
  router.push('/platform/plugins/market')
}

onMounted(loadVersions)
</script>

<style scoped>
.publish-page {
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
.upload-card,
.list-card {
  margin-bottom: 16px;
}
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
}
.hint {
  color: var(--el-text-color-secondary);
  font-size: 12px;
}
.filters {
  display: flex;
  gap: 8px;
  align-items: center;
}
.upload-form {
  max-width: 720px;
}
.issues {
  margin-top: 8px;
}
.issue-list {
  margin: 0;
  padding-left: 18px;
  font-size: 13px;
}
.muted {
  color: var(--el-text-color-secondary);
  font-size: 12px;
}
</style>
