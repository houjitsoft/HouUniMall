<template>
  <div class="page-container">
    <el-card shadow="never">
      <template #header>
        <div class="card-header">
          <span class="card-title">用户标签</span>
          <div>
            <el-button @click="fetchData">刷新</el-button>
            <el-button type="primary" @click="handleAdd">新增标签</el-button>
          </div>
        </div>
      </template>
      <el-alert title="与用户画像共用会员标签：新增、编辑、打标及删除实时读取和保存，不包含 CRM 客户标签。" type="info" :closable="false" class="source-hint" />
      <el-alert v-if="loadError" :title="loadError" type="error" :closable="false" class="source-hint" />
      <el-form :inline="true" :model="searchForm" class="search-form" @submit.prevent="handleSearch">
        <el-form-item label="标签名称">
          <el-input v-model="searchForm.name" placeholder="请输入标签名称" clearable @keyup.enter="handleSearch" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
      <el-table :data="tableData" v-loading="loading" border stripe empty-text="暂无匹配标签">
        <el-table-column prop="tagId" label="标签ID" width="80" />
        <el-table-column prop="tagName" label="标签名称" min-width="140" />
        <el-table-column label="标签颜色" width="130">
          <template #default="{ row }">
            <el-tag effect="plain" :style="{ color: row.tagColor || '#409EFF', borderColor: row.tagColor || '#409EFF' }">{{ row.tagName }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="memberCount" label="覆盖会员数" width="110" />
        <el-table-column prop="tagWeight" label="权重" width="80" />
        <el-table-column prop="tagDescribe" label="标签描述" min-width="200" show-overflow-tooltip />
        <el-table-column prop="createTime" label="创建时间" width="170" />
        <el-table-column label="操作" width="220" fixed="right">
          <template #default="{ row }">
            <el-button text type="primary" size="small" @click="handleEdit(row)">编辑</el-button>
            <el-button text type="primary" size="small" @click="handleViewCustomers(row)">查看客户</el-button>
            <el-button text type="danger" size="small" :disabled="deletingId === row.tagId" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination v-model:current-page="pagination.page" v-model:page-size="pagination.size"
        :total="filteredTags.length" :page-sizes="[10, 20, 50, 100]" layout="total, sizes, prev, pager, next, jumper"
        @size-change="pagination.page = 1" class="pagination" />
    </el-card>

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="500px" :close-on-click-modal="!saving" :show-close="!saving">
      <el-form :model="form" label-width="100px" @submit.prevent="handleSubmit">
        <el-form-item label="标签名称" required>
          <el-input v-model="form.tagName" placeholder="请输入标签名称" maxlength="40" show-word-limit />
        </el-form-item>
        <el-form-item label="标签颜色"><el-color-picker v-model="form.tagColor" /></el-form-item>
        <el-form-item label="权重"><el-input-number v-model="form.tagWeight" :min="0" :max="999" /></el-form-item>
        <el-form-item label="标签描述">
          <el-input v-model="form.tagDescribe" type="textarea" :rows="3" maxlength="255" show-word-limit placeholder="请输入标签描述" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button :disabled="saving" @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="membersVisible" :title="`标签会员：${currentTag?.tagName || ''}`" width="860px">
      <el-alert v-if="membersError" :title="membersError" type="error" :closable="false" />
      <p>命中 {{ memberTotal }} 人（来自会员打标关系）</p>
      <el-table :data="members" v-loading="membersLoading" border empty-text="此标签尚未覆盖会员">
        <el-table-column prop="userId" label="会员ID" min-width="180" />
        <el-table-column prop="nickname" label="昵称" min-width="120" />
        <el-table-column prop="mobile" label="手机号" min-width="140" />
        <el-table-column prop="layerLabel" label="画像分层" min-width="110" />
      </el-table>
      <el-pagination v-model:current-page="memberPage" :page-size="10" :total="memberTotal" layout="total, prev, pager, next"
        @current-change="loadMembers" class="pagination" />
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, onActivated } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { fetchMemberTagList, saveMemberTag, deleteMemberTag, previewMemberSegment } from '@/api/user/portrait'

const loading = ref(false)
const loadError = ref('')
const tags = ref([])
const searchForm = reactive({ name: '' })
const appliedName = ref('')
const pagination = reactive({ page: 1, size: 10 })
const filteredTags = computed(() => tags.value.filter(t => !appliedName.value || t.tagName?.includes(appliedName.value)))
const tableData = computed(() => filteredTags.value.slice((pagination.page - 1) * pagination.size, pagination.page * pagination.size))
const dialogVisible = ref(false)
const dialogTitle = ref('新增标签')
const saving = ref(false)
const deletingId = ref(null)
const emptyForm = () => ({ tagId: null, tagName: '', tagColor: '#409EFF', tagWeight: 0, tagDescribe: '' })
const form = reactive(emptyForm())
const membersVisible = ref(false)
const currentTag = ref(null)
const membersLoading = ref(false)
const membersError = ref('')
const members = ref([])
const memberTotal = ref(0)
const memberPage = ref(1)
let memberRequest = 0
const requireSuccess = (res) => {
  if (Number(res?.code) !== 1) throw new Error(res?.message || '操作失败，请重试')
  return res.data
}
const fetchData = async () => {
  if (loading.value) return
  loading.value = true
  loadError.value = ''
  try {
    const data = requireSuccess(await fetchMemberTagList())
    tags.value = data?.list || []
    pagination.page = Math.min(pagination.page, Math.max(1, Math.ceil(filteredTags.value.length / pagination.size)))
  } catch (e) {
    tags.value = []
    loadError.value = e.message || '标签加载失败，请刷新重试'
  } finally { loading.value = false }
}
const handleSearch = () => { appliedName.value = searchForm.name.trim(); pagination.page = 1 }
const handleReset = () => { searchForm.name = ''; appliedName.value = ''; pagination.page = 1; fetchData() }
const handleAdd = () => { dialogTitle.value = '新增标签'; Object.assign(form, emptyForm()); dialogVisible.value = true }
const handleEdit = (row) => {
  dialogTitle.value = '编辑标签'
  Object.assign(form, emptyForm(), row, { tagColor: row.tagColor || '#409EFF', tagDescribe: row.tagDescribe || '' })
  dialogVisible.value = true
}
const handleSubmit = async () => {
  if (saving.value) return
  if (!form.tagName.trim()) return ElMessage.warning('请填写标签名称')
  if (!/^#[0-9a-fA-F]{6}$/.test(form.tagColor || '')) return ElMessage.warning('请选择标签颜色')
  saving.value = true
  try {
    requireSuccess(await saveMemberTag({ tagId: form.tagId, tagName: form.tagName.trim(), tagColor: form.tagColor, tagWeight: form.tagWeight, tagDescribe: form.tagDescribe.trim() }))
    dialogVisible.value = false
    ElMessage.success('保存成功')
    await fetchData()
  } catch (e) { ElMessage.error(e.message || '保存失败') }
  finally { saving.value = false }
}
const handleDelete = async (row) => {
  try { await ElMessageBox.confirm(`确认删除标签「${row.tagName}」？删除后将不再用于画像和圈人。`, '提示', { type: 'warning' }) }
  catch { return }
  deletingId.value = row.tagId
  try {
    requireSuccess(await deleteMemberTag(row.tagId))
    ElMessage.success('删除成功')
    await fetchData()
  } catch (e) { ElMessage.error(e.message || '删除失败') }
  finally { deletingId.value = null }
}
const loadMembers = async () => {
  const requestId = ++memberRequest
  membersLoading.value = true
  membersError.value = ''
  try {
    const data = requireSuccess(await previewMemberSegment({ tagIds: [currentTag.value.tagId], page: memberPage.value, size: 10 }))
    if (requestId !== memberRequest) return
    members.value = data?.records || []
    memberTotal.value = data?.total || 0
  } catch (e) {
    if (requestId !== memberRequest) return
    members.value = []; memberTotal.value = 0; membersError.value = e.message || '会员加载失败'
  } finally { if (requestId === memberRequest) membersLoading.value = false }
}
const handleViewCustomers = (row) => {
  currentTag.value = row; members.value = []; memberTotal.value = 0; memberPage.value = 1
  membersVisible.value = true; loadMembers()
}
onMounted(fetchData)
onActivated(fetchData)
</script>

<style scoped>
.page-container { padding: 20px; }
.card-header { display: flex; justify-content: space-between; align-items: center; }
.card-title { font-weight: bold; font-size: 16px; }
.source-hint { margin-bottom: 16px; }
.search-form { margin-bottom: 20px; }
.pagination { margin-top: 20px; justify-content: flex-end; }
</style>
