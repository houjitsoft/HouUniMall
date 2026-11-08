<template>
  <div class="agreement-manage">
    <el-card shadow="never">
      <template #header>
        <div class="page-header">
          <div class="page-header__title">
            协议管理
            <el-tag type="info" size="small" effect="plain" class="page-header__tag">
              协议文案与版本在此维护，发布新版本后用户需重新同意
            </el-tag>
          </div>
          <div class="page-header__actions">
            <el-button type="primary" @click="handleCreate">新增协议</el-button>
            <el-button :loading="loading" @click="fetchList">刷新</el-button>
          </div>
        </div>
      </template>

      <el-alert
        type="warning"
        :closable="false"
        show-icon
        class="tip"
        title="发布即生效：新的已发布版本会立即成为用户看到并需要勾选的正文，已同意旧版本的用户会被要求重新同意。建议先用草稿保存、确认无误再发布。"
      />

      <el-form :inline="true" :model="query" class="search-form" @submit.prevent>
        <el-form-item label="名称">
          <el-input v-model="query.name" placeholder="协议名称" clearable style="width: 160px" @keyup.enter="handleSearch" />
        </el-form-item>
        <el-form-item label="编码">
          <el-input v-model="query.code" placeholder="协议编码" clearable style="width: 160px" @keyup.enter="handleSearch" />
        </el-form-item>
        <el-form-item label="场景">
          <el-select v-model="query.scene" placeholder="全部" clearable style="width: 140px">
            <el-option v-for="s in SCENE_OPTIONS" :key="s.value" :label="s.label" :value="s.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="query.status" placeholder="全部" clearable style="width: 110px">
            <el-option label="启用" :value="1" />
            <el-option label="停用" :value="0" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="loading" @click="handleSearch">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>

      <el-table v-loading="loading" :data="list" border stripe max-height="480">
        <el-table-column prop="sort" label="排序" width="70" />
        <el-table-column prop="name" label="协议名称" min-width="200" show-overflow-tooltip />
        <el-table-column prop="code" label="编码" width="200" show-overflow-tooltip />
        <el-table-column prop="scene" label="场景" width="100">
          <template #default="{ row }">
            <el-tag size="small" effect="plain">{{ sceneText(row.scene) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="必选" width="80" align="center">
          <template #default="{ row }">
            <el-tag size="small" :type="row.required === 1 ? 'danger' : 'info'" effect="plain">
              {{ row.required === 1 ? '必选' : '可选' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="默认勾选" width="90" align="center">
          <template #default="{ row }">
            <el-tag size="small" :type="row.defaultChecked === 1 ? 'warning' : 'info'" effect="plain">
              {{ row.defaultChecked === 1 ? '是' : '否' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="80" align="center">
          <template #default="{ row }">
            <el-tag size="small" :type="row.status === 1 ? 'success' : 'info'">
              {{ row.status === 1 ? '启用' : '停用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="remark" label="备注" min-width="140" show-overflow-tooltip />
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="handleEdit(row)">编辑</el-button>
            <el-button link type="primary" @click="openVersions(row)">版本</el-button>
            <el-button link type="danger" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
        <template #empty>
          <el-empty description="暂无协议定义" />
        </template>
      </el-table>

      <el-pagination
        v-model:current-page="query.page"
        v-model:page-size="query.limit"
        :total="total"
        :page-sizes="[10, 20, 50]"
        layout="total, sizes, prev, pager, next, jumper"
        class="pager"
        @size-change="fetchList"
        @current-change="fetchList"
      />
    </el-card>

    <!-- 协议定义编辑 -->
    <el-dialog v-model="formVisible" :title="form.id ? '编辑协议' : '新增协议'" width="560px">
      <el-form :model="form" label-width="100px">
        <el-form-item label="协议名称" required>
          <el-input v-model="form.name" placeholder="如：用户服务协议" />
        </el-form-item>
        <el-form-item label="协议编码" required>
          <el-input v-model="form.code" placeholder="如：USER_SERVICE_AGREEMENT" :disabled="!!form.id" />
        </el-form-item>
        <el-form-item label="适用场景">
          <el-select v-model="form.scene" style="width: 100%">
            <el-option v-for="s in SCENE_OPTIONS" :key="s.value" :label="s.label" :value="s.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="展示排序">
          <el-input-number v-model="form.sort" :min="0" :max="9999" />
          <span class="form-hint">数字越小越靠前</span>
        </el-form-item>
        <el-form-item label="是否必选">
          <el-switch v-model="form.requiredOn" active-text="必选" inactive-text="可选" />
        </el-form-item>
        <el-form-item label="默认勾选">
          <el-switch v-model="form.defaultCheckedOn" active-text="默认勾选" inactive-text="默认不勾" />
          <span class="form-hint">合规建议保持「默认不勾」</span>
        </el-form-item>
        <el-form-item label="状态">
          <el-switch v-model="form.statusOn" active-text="启用" inactive-text="停用" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="form.remark" type="textarea" :rows="2" placeholder="内部说明，不对外展示" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="formVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="submitForm">保存</el-button>
      </template>
    </el-dialog>

    <!-- 版本管理 -->
    <el-drawer v-model="versionVisible" :title="`版本管理 · ${current?.name || ''}`" size="820px">
      <div class="version-toolbar">
        <el-button type="primary" size="small" @click="handleVersionCreate">新增版本</el-button>
        <el-button size="small" :loading="versionLoading" @click="fetchVersions">刷新</el-button>
        <span class="version-tip">已发布版本中「生效时间」最新的一个，即用户当前看到的正文</span>
      </div>

      <el-table v-loading="versionLoading" :data="versions" border stripe size="small">
        <el-table-column prop="versionNo" label="版本号" width="110" />
        <el-table-column prop="title" label="标题" min-width="160" show-overflow-tooltip />
        <el-table-column label="状态" width="90" align="center">
          <template #default="{ row }">
            <el-tag size="small" :type="versionStatusType(row.status)">{{ versionStatusText(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="effectiveTime" label="生效时间" width="165" />
        <el-table-column prop="publishTime" label="发布时间" width="165" />
        <el-table-column prop="changeNote" label="变更说明" min-width="150" show-overflow-tooltip />
        <el-table-column label="操作" width="230" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click="handleVersionView(row)">正文</el-button>
            <el-button
              link
              type="primary"
              size="small"
              :disabled="row.status === 1"
              @click="handleVersionEdit(row)"
            >
              编辑
            </el-button>
            <el-button
              v-if="row.status !== 1"
              link
              type="success"
              size="small"
              @click="handleVersionPublish(row)"
            >
              发布
            </el-button>
            <el-button v-else link type="warning" size="small" @click="handleVersionOffline(row)">下线</el-button>
            <el-button link type="danger" size="small" :disabled="row.status === 1" @click="handleVersionDelete(row)">
              删除
            </el-button>
          </template>
        </el-table-column>
        <template #empty>
          <el-empty description="该协议还没有版本，请先新增" />
        </template>
      </el-table>
    </el-drawer>

    <!-- 版本编辑 -->
    <el-dialog v-model="vformVisible" :title="vform.id ? '编辑版本' : '新增版本'" width="860px" top="5vh">
      <el-form :model="vform" label-width="90px">
        <el-row :gutter="12">
          <el-col :span="8">
            <el-form-item label="版本号" required>
              <el-input v-model="vform.versionNo" placeholder="如 v1.1.0" />
            </el-form-item>
          </el-col>
          <el-col :span="16">
            <el-form-item label="标题">
              <el-input v-model="vform.title" placeholder="弹窗标题，默认取协议名称" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="12">
          <el-col :span="12">
            <el-form-item label="生效时间">
              <el-date-picker
                v-model="vform.effectiveTime"
                type="datetime"
                placeholder="留空表示发布时立即生效"
                value-format="YYYY-MM-DD HH:mm:ss"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="变更说明">
              <el-input v-model="vform.changeNote" placeholder="面向用户的变更摘要" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="协议正文" required>
          <el-input
            v-model="vform.content"
            type="textarea"
            :rows="18"
            placeholder="协议正文，保存为草稿；发布后才会对用户生效"
            class="content-input"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="vformVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="submitVersion">保存草稿</el-button>
        <el-button v-if="vform.id" type="success" :loading="saving" @click="submitVersionAndPublish">
          保存并发布
        </el-button>
      </template>
    </el-dialog>

    <!-- 正文查看 -->
    <el-dialog v-model="contentVisible" :title="viewVersion?.title || '协议正文'" width="760px">
      <div class="content-view">{{ viewVersion?.content }}</div>
    </el-dialog>
  </div>
</template>

<script setup>
/**
 * 协议管理（后台配置页）
 *
 * 让协议文案可后台维护：协议定义（名称/编码/场景/是否必选/排序）+
 * 版本（正文快照 / 状态 / 生效时间）。新增或发布版本后，注册页会拉取到新正文，
 * 已同意旧版本的用户会被判定为「待重新同意」并弹窗补签。
 */
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { agreementApi } from '@/api/setting/agreement'

const SCENE_OPTIONS = [
  { value: 'register', label: '注册' },
  { value: 'install', label: '安装者确认' },
  { value: 'plugin_purchase', label: '插件购买' },
  { value: 'reagree', label: '重新同意' },
  { value: 'login', label: '登录' },
  { value: 'shop_join', label: '入驻' },
  { value: 'other', label: '其他' }
]

const loading = ref(false)
const saving = ref(false)
const list = ref([])
const total = ref(0)

const query = reactive({
  page: 1,
  limit: 20,
  name: '',
  code: '',
  scene: '',
  status: ''
})

const formVisible = ref(false)
const form = reactive({
  id: null,
  name: '',
  code: '',
  scene: 'register',
  sort: 10,
  requiredOn: true,
  defaultCheckedOn: false,
  statusOn: true,
  remark: ''
})

const versionVisible = ref(false)
const versionLoading = ref(false)
const versions = ref([])
const current = ref(null)

const vformVisible = ref(false)
const vform = reactive({
  id: null,
  agreementId: null,
  versionNo: '',
  title: '',
  content: '',
  changeNote: '',
  effectiveTime: ''
})

const contentVisible = ref(false)
const viewVersion = ref(null)

const sceneText = (scene) => {
  const hit = SCENE_OPTIONS.find((item) => item.value === scene)
  return hit ? hit.label : scene || '未设置'
}

const versionStatusText = (status) => {
  if (status === 1) return '已发布'
  if (status === 2) return '已下线'
  return '草稿'
}

const versionStatusType = (status) => {
  if (status === 1) return 'success'
  if (status === 2) return 'info'
  return 'warning'
}

/** 统一的成功/失败处理：后端业务码 1 为成功 */
const handleResult = (res, successMsg) => {
  if (res && res.code === 1) {
    ElMessage.success(successMsg || res.message || '操作成功')
    return true
  }
  ElMessage.error(res?.message || '操作失败')
  return false
}

const fetchList = async () => {
  loading.value = true
  try {
    const payload = { page: query.page, limit: query.limit }
    if (query.name) payload.name = query.name.trim()
    if (query.code) payload.code = query.code.trim()
    if (query.scene) payload.scene = query.scene
    if (query.status !== '' && query.status !== null && query.status !== undefined) {
      payload.status = query.status
    }
    const res = await agreementApi.adminPage(payload)
    if (res && res.code === 1 && res.data) {
      list.value = res.data.list || []
      total.value = res.data.totalCount || 0
    } else {
      list.value = []
      total.value = 0
      ElMessage.warning(res?.message || '查询失败')
    }
  } catch (error) {
    list.value = []
    total.value = 0
    ElMessage.error(error?.message || '查询失败')
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  query.page = 1
  fetchList()
}

const handleReset = () => {
  query.page = 1
  query.name = ''
  query.code = ''
  query.scene = ''
  query.status = ''
  fetchList()
}

const resetForm = () => {
  form.id = null
  form.name = ''
  form.code = ''
  form.scene = 'register'
  form.sort = 10
  form.requiredOn = true
  form.defaultCheckedOn = false
  form.statusOn = true
  form.remark = ''
}

const handleCreate = () => {
  resetForm()
  formVisible.value = true
}

const handleEdit = (row) => {
  resetForm()
  form.id = row.id
  form.name = row.name || ''
  form.code = row.code || ''
  form.scene = row.scene || 'register'
  form.sort = row.sort ?? 10
  form.requiredOn = row.required === 1
  form.defaultCheckedOn = row.defaultChecked === 1
  form.statusOn = row.status === 1
  form.remark = row.remark || ''
  formVisible.value = true
}

const submitForm = async () => {
  if (!form.name.trim()) {
    ElMessage.warning('请填写协议名称')
    return
  }
  if (!form.code.trim()) {
    ElMessage.warning('请填写协议编码')
    return
  }
  saving.value = true
  try {
    const payload = {
      name: form.name.trim(),
      code: form.code.trim(),
      scene: form.scene,
      sort: form.sort,
      required: form.requiredOn ? 1 : 0,
      defaultChecked: form.defaultCheckedOn ? 1 : 0,
      status: form.statusOn ? 1 : 0,
      remark: form.remark
    }
    if (form.id) payload.id = form.id
    const ok = handleResult(await agreementApi.adminSave(payload), '保存成功')
    if (ok) {
      formVisible.value = false
      fetchList()
    }
  } catch (error) {
    ElMessage.error(error?.message || '保存失败')
  } finally {
    saving.value = false
  }
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm(
      `删除后《${row.name}》及其全部版本都会被移除，用户已产生的历史同意记录会保留。确认删除？`,
      '删除协议',
      { type: 'warning', confirmButtonText: '确认删除', cancelButtonText: '取消' }
    )
  } catch {
    return
  }
  const ok = handleResult(await agreementApi.adminDelete(row.id))
  if (ok) {
    versionVisible.value = false
    fetchList()
  }
}

// ================= 版本 =================

const fetchVersions = async () => {
  if (!current.value) return
  versionLoading.value = true
  try {
    const res = await agreementApi.versionList({ agreementId: current.value.id })
    versions.value = res?.code === 1 ? res.data || [] : []
    if (res && res.code !== 1) ElMessage.warning(res.message || '版本查询失败')
  } catch (error) {
    versions.value = []
    ElMessage.error(error?.message || '版本查询失败')
  } finally {
    versionLoading.value = false
  }
}

const openVersions = (row) => {
  current.value = row
  versions.value = []
  versionVisible.value = true
  fetchVersions()
}

const resetVForm = () => {
  vform.id = null
  vform.versionNo = ''
  vform.title = ''
  vform.content = ''
  vform.changeNote = ''
  vform.effectiveTime = ''
}

const handleVersionCreate = () => {
  if (!current.value) return
  resetVForm()
  vform.agreementId = current.value.id
  vform.title = current.value.name || ''
  vformVisible.value = true
}

const handleVersionEdit = (row) => {
  resetVForm()
  vform.id = row.id
  vform.agreementId = row.agreementId
  vform.versionNo = row.versionNo || ''
  vform.title = row.title || ''
  vform.content = row.content || ''
  vform.changeNote = row.changeNote || ''
  vform.effectiveTime = row.effectiveTime || ''
  vformVisible.value = true
}

const handleVersionView = (row) => {
  viewVersion.value = row
  contentVisible.value = true
}

const submitVersion = async (thenPublish = false) => {
  if (!vform.versionNo.trim()) {
    ElMessage.warning('请填写版本号')
    return
  }
  if (!vform.content.trim()) {
    ElMessage.warning('请填写协议正文')
    return
  }
  saving.value = true
  try {
    const payload = {
      agreementId: vform.agreementId,
      versionNo: vform.versionNo.trim(),
      title: vform.title.trim(),
      content: vform.content,
      changeNote: vform.changeNote,
      effectiveTime: vform.effectiveTime
    }
    if (vform.id) payload.id = vform.id
    const res = await agreementApi.versionSave(payload)
    if (!handleResult(res, '版本已保存为草稿')) return
    const versionId = res?.data?.id || res?.data || vform.id
    vformVisible.value = false
    if (thenPublish && versionId) {
      await handleVersionPublish({ id: versionId })
    }
    fetchVersions()
    fetchList()
  } catch (error) {
    ElMessage.error(error?.message || '保存失败')
  } finally {
    saving.value = false
  }
}

const submitVersionAndPublish = () => submitVersion(true)

const handleVersionPublish = async (row) => {
  try {
    await ElMessageBox.confirm(
      `发布《${current.value?.name}》${row.versionNo || ''}？\n发布后立即对用户生效，已同意旧版本的用户需重新同意。`,
      '发布版本',
      { type: 'warning', confirmButtonText: '确认发布', cancelButtonText: '取消' }
    )
  } catch {
    return
  }
  const ok = handleResult(await agreementApi.versionPublish(row.id))
  if (ok) fetchVersions()
}

const handleVersionOffline = async (row) => {
  try {
    await ElMessageBox.confirm(`下线后用户将不再看到该版本正文，确认下线 ${row.versionNo}？`, '下线版本', {
      type: 'warning',
      confirmButtonText: '确认下线',
      cancelButtonText: '取消'
    })
  } catch {
    return
  }
  const ok = handleResult(await agreementApi.versionOffline(row.id))
  if (ok) fetchVersions()
}

const handleVersionDelete = async (row) => {
  try {
    await ElMessageBox.confirm(`确认删除版本 ${row.versionNo}？`, '删除版本', {
      type: 'warning',
      confirmButtonText: '确认删除',
      cancelButtonText: '取消'
    })
  } catch {
    return
  }
  const ok = handleResult(await agreementApi.versionDelete(row.id))
  if (ok) fetchVersions()
}

onMounted(fetchList)
</script>

<style scoped>
.page-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.page-header__title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 16px;
  font-weight: 600;
}

.page-header__actions {
  display: flex;
  gap: 8px;
}

.tip {
  margin-bottom: 16px;
}

.search-form {
  margin-bottom: 8px;
}

.pager {
  margin-top: 16px;
  justify-content: flex-end;
}

.form-hint {
  margin-left: 10px;
  color: var(--el-text-color-secondary);
  font-size: 12px;
}

.version-toolbar {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 12px;
}

.version-tip {
  margin-left: auto;
  color: var(--el-text-color-secondary);
  font-size: 12px;
}

.content-input :deep(textarea) {
  font-family: Menlo, Consolas, 'Courier New', monospace;
  font-size: 13px;
  line-height: 1.7;
}

.content-view {
  max-height: 60vh;
  overflow: auto;
  white-space: pre-wrap;
  word-break: break-word;
  line-height: 1.8;
  font-size: 13px;
}
</style>
