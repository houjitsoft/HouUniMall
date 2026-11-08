<template>
  <div class="setting-system-page" v-loading="loading">
    <header class="sys-hero">
      <div>
        <h1 class="sys-hero__title">协议管理</h1>
        <p class="sys-hero__desc">管理入驻协议与合作文档，支持 PDF 在线更新</p>
      </div>
      <el-button type="primary" round class="sys-hero__add" @click="showAddDialog">
        <el-icon><Plus /></el-icon>
        <span class="btn-text">新增</span>
      </el-button>
    </header>

    <el-row :gutter="12" class="sys-stats">
      <el-col :xs="8" :sm="8">
        <div class="stat-card">
          <span class="stat-card__num">{{ protocolList.length }}</span>
          <span class="stat-card__label">协议总数</span>
        </div>
      </el-col>
      <el-col :xs="8" :sm="8">
        <div class="stat-card stat-card--ok">
          <span class="stat-card__num">{{ enabledCount }}</span>
          <span class="stat-card__label">已启用</span>
        </div>
      </el-col>
      <el-col :xs="8" :sm="8">
        <div class="stat-card stat-card--muted">
          <span class="stat-card__num">{{ pdfCount }}</span>
          <span class="stat-card__label">已上传</span>
        </div>
      </el-col>
    </el-row>

    <div class="toolbar-row">
      <el-input
        v-model="keyword"
        clearable
        placeholder="搜索协议名称"
        :prefix-icon="Search"
        class="toolbar-search"
      />
      <el-button :icon="Refresh" circle @click="loadProtocols" />
    </div>

    <el-empty v-if="!filteredList.length && !loading" description="暂无协议，点击右上角新增" />

    <div v-else class="protocol-list">
      <article
        v-for="item in filteredList"
        :key="item.id"
        class="protocol-item"
        :class="{ disabled: item.status === 0 }"
      >
        <div class="protocol-item__head">
          <div class="protocol-icon" :style="{ '--c': typeColor(item.type) }">
            <el-icon><Document /></el-icon>
          </div>
          <div class="protocol-item__meta">
            <h3>{{ item.name }}</h3>
            <span class="type-tag">{{ typeLabel(item.type) }}</span>
          </div>
          <el-tag :type="item.status === 1 ? 'success' : 'info'" size="small" effect="light">
            {{ item.status === 1 ? '启用' : '禁用' }}
          </el-tag>
        </div>

        <div class="protocol-item__body">
          <div class="info-line">
            <span class="label">文件</span>
            <el-link v-if="item.fileUrl" :href="item.fileUrl" target="_blank" type="primary" class="file-link">
              {{ item.fileName || '查看 PDF' }}
            </el-link>
            <span v-else class="empty-text">未上传</span>
          </div>
          <div class="info-line">
            <span class="label">更新</span>
            <span>{{ item.updateTime || '—' }}</span>
          </div>
        </div>

        <div class="protocol-item__actions">
          <el-upload
            :action="uploadUrl"
            :headers="uploadHeaders"
            :show-file-list="false"
            :on-success="(res) => handleUploadSuccess(res, item)"
            :before-upload="beforeUpload"
            accept=".pdf"
          >
            <el-button size="small" type="primary" plain>上传 PDF</el-button>
          </el-upload>
          <el-button size="small" @click="copyLink(item.fileUrl)">复制链接</el-button>
          <el-button size="small" type="primary" link @click="editProtocol(item)">编辑</el-button>
          <el-button size="small" type="danger" link @click="deleteProtocol(item)">删除</el-button>
        </div>
      </article>
    </div>

    <button type="button" class="mobile-fab" aria-label="新增协议" @click="showAddDialog">
      <el-icon><Plus /></el-icon>
    </button>

    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="92%"
      class="protocol-dialog"
      destroy-on-close
      @close="resetForm"
    >
      <el-form ref="formRef" :model="protocolForm" :rules="formRules" label-position="top">
        <el-form-item label="协议名称" prop="name">
          <el-input v-model="protocolForm.name" placeholder="如：个人入驻协议" />
        </el-form-item>
        <el-form-item label="协议类型" prop="type">
          <el-select v-model="protocolForm.type" placeholder="请选择" style="width: 100%">
            <el-option label="个人入驻" value="PERSONAL" />
            <el-option label="企业入驻" value="ENTERPRISE" />
            <el-option label="供应商入驻" value="SUPPLIER" />
            <el-option label="合作协议" value="COOPERATION" />
          </el-select>
        </el-form-item>
        <el-form-item label="协议文件" prop="fileUrl">
          <el-upload
            :action="uploadUrl"
            :headers="uploadHeaders"
            :show-file-list="false"
            :on-success="handleFormUploadSuccess"
            :before-upload="beforeUpload"
            accept=".pdf"
          >
            <el-button type="primary">选择 PDF</el-button>
          </el-upload>
          <div v-if="protocolForm.fileUrl" class="file-chip">
            <el-icon><Document /></el-icon>
            <span>{{ protocolForm.fileName || '已选择文件' }}</span>
          </div>
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="protocolForm.sort" :min="0" :max="999" style="width: 100%" />
        </el-form-item>
        <el-form-item label="状态">
          <el-switch
            v-model="protocolForm.status"
            :active-value="1"
            :inactive-value="0"
            active-text="启用"
            inactive-text="禁用"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="saveProtocol">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Document, Plus, Refresh, Search } from '@element-plus/icons-vue'
import request from '@/utils/request'
import { getToken } from '@/utils/auth'

const loading = ref(false)
const saving = ref(false)
const keyword = ref('')
const protocolList = ref([])
const dialogVisible = ref(false)
const formRef = ref()

const protocolForm = reactive({
  id: null,
  name: '',
  type: '',
  fileUrl: '',
  fileName: '',
  sort: 0,
  status: 1
})

const formRules = {
  name: [{ required: true, message: '请输入协议名称', trigger: 'blur' }],
  type: [{ required: true, message: '请选择协议类型', trigger: 'change' }],
  fileUrl: [{ required: true, message: '请上传协议文件', trigger: 'change' }]
}

const uploadUrl = `${import.meta.env.VITE_APP_BASE_API}/web/common/upload`
const uploadHeaders = computed(() => ({ Authorization: `Bearer ${getToken()}` }))

const dialogTitle = computed(() => (protocolForm.id ? '编辑协议' : '新增协议'))
const enabledCount = computed(() => protocolList.value.filter((p) => p.status === 1).length)
const pdfCount = computed(() => protocolList.value.filter((p) => p.fileUrl).length)
const filteredList = computed(() => {
  const q = keyword.value.trim().toLowerCase()
  if (!q) return protocolList.value
  return protocolList.value.filter((p) => (p.name || '').toLowerCase().includes(q))
})

const TYPE_MAP = {
  PERSONAL: { label: '个人入驻', color: '#6366f1' },
  ENTERPRISE: { label: '企业入驻', color: '#0ea5e9' },
  SUPPLIER: { label: '供应商入驻', color: '#f59e0b' },
  COOPERATION: { label: '合作协议', color: '#10b981' }
}

function typeLabel(type) {
  return TYPE_MAP[type]?.label || type || '其他'
}

function typeColor(type) {
  return TYPE_MAP[type]?.color || '#909399'
}

async function loadProtocols() {
  loading.value = true
  try {
    const res = await request.post('/web/base/protocol/list', {})
    if (res?.code === 200 || res?.code === 1) {
      protocolList.value = res.data || []
    }
  } catch (e) {
    console.error('加载协议失败', e)
    ElMessage.error('加载协议列表失败')
  } finally {
    loading.value = false
  }
}

function showAddDialog() {
  resetForm()
  dialogVisible.value = true
}

function editProtocol(row) {
  Object.assign(protocolForm, {
    id: row.id,
    name: row.name,
    type: row.type,
    fileUrl: row.fileUrl,
    fileName: row.fileName,
    sort: row.sort || 0,
    status: row.status ?? 1
  })
  dialogVisible.value = true
}

async function saveProtocol() {
  if (!formRef.value) return
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  saving.value = true
  try {
    const res = await request.post('/web/base/protocol/save', { ...protocolForm })
    if (res?.code === 200 || res?.code === 1) {
      ElMessage.success(protocolForm.id ? '编辑成功' : '添加成功')
      dialogVisible.value = false
      loadProtocols()
    } else {
      ElMessage.error(res?.message || '保存失败')
    }
  } catch {
    ElMessage.error('保存失败')
  } finally {
    saving.value = false
  }
}

function deleteProtocol(row) {
  ElMessageBox.confirm(`确定删除「${row.name}」？`, '删除确认', { type: 'warning' })
    .then(async () => {
      const res = await request.post('/web/base/protocol/delete', { id: row.id })
      if (res?.code === 200 || res?.code === 1) {
        ElMessage.success('删除成功')
        loadProtocols()
      } else {
        ElMessage.error(res?.message || '删除失败')
      }
    })
    .catch(() => {})
}

async function handleUploadSuccess(response, protocol) {
  if (response?.code !== 200 && response?.code !== 1) {
    ElMessage.error(response?.message || '上传失败')
    return
  }
  const url = response.data?.url || response.data?.fileUrl
  const fileName = response.data?.fileName
  try {
    const res = await request.post('/web/base/protocol/save', {
      id: protocol.id,
      fileUrl: url,
      fileName
    })
    if (res?.code === 200 || res?.code === 1) {
      ElMessage.success('上传成功')
      loadProtocols()
    }
  } catch {
    ElMessage.error('更新失败')
  }
}

function handleFormUploadSuccess(response) {
  if (response?.code === 200 || response?.code === 1) {
    protocolForm.fileUrl = response.data?.url || response.data?.fileUrl
    protocolForm.fileName = response.data?.fileName
    ElMessage.success('上传成功')
  } else {
    ElMessage.error(response?.message || '上传失败')
  }
}

function beforeUpload(file) {
  const isPDF = file.type === 'application/pdf'
  const isLt2M = file.size / 1024 / 1024 < 2
  if (!isPDF) {
    ElMessage.error('只能上传 PDF 文件')
    return false
  }
  if (!isLt2M) {
    ElMessage.error('文件不能超过 2MB')
    return false
  }
  return true
}

function copyLink(url) {
  if (!url) {
    ElMessage.warning('链接为空')
    return
  }
  navigator.clipboard.writeText(url).then(
    () => ElMessage.success('已复制'),
    () => ElMessage.error('复制失败')
  )
}

function resetForm() {
  Object.assign(protocolForm, {
    id: null,
    name: '',
    type: '',
    fileUrl: '',
    fileName: '',
    sort: 0,
    status: 1
  })
  formRef.value?.clearValidate()
}

onMounted(loadProtocols)
</script>

<style scoped lang="scss">
.setting-system-page {
  padding: 16px;
  padding-bottom: calc(80px + env(safe-area-inset-bottom, 0px));
  max-width: 960px;
  margin: 0 auto;
}

.sys-hero {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 12px;
  padding: 20px;
  border-radius: 16px;
  background: linear-gradient(135deg, #0f766e 0%, #0891b2 50%, #2563eb 100%);
  color: #fff;
  margin-bottom: 16px;
  box-shadow: 0 8px 24px rgba(8, 145, 178, 0.22);

  &__title {
    margin: 0 0 6px;
    font-size: 22px;
    font-weight: 700;
  }

  &__desc {
    margin: 0;
    font-size: 13px;
    opacity: 0.92;
    line-height: 1.5;
  }

  &__add {
    flex-shrink: 0;
    min-height: 40px;
    border: none;
    background: rgba(255, 255, 255, 0.95);
    color: #0f766e;
    font-weight: 600;
  }
}

.sys-stats {
  margin-bottom: 14px;
}

.stat-card {
  background: #fff;
  border-radius: 12px;
  border: 1px solid #ebeef5;
  padding: 12px 8px;
  text-align: center;
  margin-bottom: 8px;

  &--ok .stat-card__num {
    color: #67c23a;
  }

  &--muted .stat-card__num {
    color: #909399;
  }

  &__num {
    display: block;
    font-size: 22px;
    font-weight: 700;
    color: #303133;
    line-height: 1.2;
  }

  &__label {
    font-size: 11px;
    color: #909399;
    margin-top: 4px;
    display: block;
  }
}

.toolbar-row {
  display: flex;
  gap: 10px;
  margin-bottom: 14px;

  .toolbar-search {
    flex: 1;
  }
}

.protocol-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.protocol-item {
  background: #fff;
  border: 1px solid #ebeef5;
  border-radius: 14px;
  padding: 14px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.03);
  transition: transform 0.15s, box-shadow 0.15s;

  &:active {
    transform: scale(0.995);
  }

  &.disabled {
    opacity: 0.72;
  }

  &__head {
    display: flex;
    align-items: center;
    gap: 12px;
    margin-bottom: 12px;
  }

  &__meta {
    flex: 1;
    min-width: 0;

    h3 {
      margin: 0 0 4px;
      font-size: 16px;
      font-weight: 600;
      color: #303133;
      white-space: nowrap;
      overflow: hidden;
      text-overflow: ellipsis;
    }
  }

  &__body {
    padding: 10px 12px;
    background: #f8fafc;
    border-radius: 10px;
    margin-bottom: 12px;
  }

  &__actions {
    display: flex;
    flex-wrap: wrap;
    gap: 8px;
  }
}

.protocol-icon {
  width: 44px;
  height: 44px;
  border-radius: 12px;
  background: color-mix(in srgb, var(--c) 12%, white);
  color: var(--c);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
  flex-shrink: 0;
}

.type-tag {
  font-size: 11px;
  color: #909399;
}

.info-line {
  display: flex;
  gap: 8px;
  font-size: 13px;
  color: #606266;
  line-height: 1.5;

  & + & {
    margin-top: 6px;
  }

  .label {
    flex-shrink: 0;
    width: 36px;
    color: #909399;
  }

  .file-link {
    word-break: break-all;
  }

  .empty-text {
    color: #c0c4cc;
  }
}

.file-chip {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  margin-top: 10px;
  padding: 8px 12px;
  background: #f0f9ff;
  border-radius: 8px;
  font-size: 13px;
  color: #0369a1;
}

.mobile-fab {
  display: flex;
  position: fixed;
  right: 20px;
  bottom: calc(20px + env(safe-area-inset-bottom, 0px));
  width: 56px;
  height: 56px;
  border-radius: 50%;
  border: none;
  background: linear-gradient(135deg, #0891b2, #2563eb);
  color: #fff;
  font-size: 24px;
  align-items: center;
  justify-content: center;
  box-shadow: 0 8px 20px rgba(37, 99, 235, 0.35);
  z-index: 90;
  cursor: pointer;
}

@media (min-width: 992px) {
  .setting-system-page {
    padding: 20px 24px;
    padding-bottom: 24px;
  }

  .mobile-fab {
    display: none;
  }

  .sys-hero__add .btn-text {
    display: inline;
  }

  .protocol-list {
    display: grid;
    grid-template-columns: repeat(2, 1fr);
    gap: 16px;
  }
}

@media (max-width: 991px) {
  .sys-hero__add .btn-text {
    display: none;
  }
}
</style>
