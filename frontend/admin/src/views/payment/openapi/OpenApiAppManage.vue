<template>
  <div class="openapi-app-manage">
    <el-card class="filter-card">
      <el-form :inline="true" :model="queryForm" @submit.prevent="onSearch">
        <el-form-item label="AppID">
          <el-input v-model="queryForm.appId" clearable style="width:180px" />
        </el-form-item>
        <el-form-item label="商户号">
          <el-input v-model="queryForm.mchNo" clearable style="width:160px" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="queryForm.status" clearable placeholder="全部" style="width:110px">
            <el-option label="启用" :value="1" />
            <el-option label="停用" :value="0" />
          </el-select>
        </el-form-item>
        <el-form-item label="关键字">
          <el-input v-model="queryForm.keyword" clearable placeholder="名称/备注" style="width:160px" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="onSearch">查询</el-button>
          <el-button @click="onReset">重置</el-button>
          <el-button type="success" @click="onAdd">新建应用</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card style="margin-top:12px">
      <el-table v-loading="loading" :data="tableData" border stripe>
        <el-table-column label="ID" prop="id" width="70" />
        <el-table-column label="AppID" prop="appId" min-width="160" show-overflow-tooltip />
        <el-table-column label="密钥" prop="appSecretMasked" width="140" />
        <el-table-column label="商户号" prop="mchNo" width="140" show-overflow-tooltip />
        <el-table-column label="应用名" prop="appName" min-width="140" show-overflow-tooltip />
        <el-table-column label="状态" width="90">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'">{{ row.status === 1 ? '启用' : '停用' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="备注" prop="remark" min-width="120" show-overflow-tooltip />
        <el-table-column label="更新时间" prop="updateTime" width="170" />
        <el-table-column label="操作" width="280" fixed="right">
          <template #default="{ row }">
            <el-button size="small" @click="onEdit(row)">编辑</el-button>
            <el-button size="small" :type="row.status === 1 ? 'warning' : 'success'" @click="onToggle(row)">
              {{ row.status === 1 ? '停用' : '启用' }}
            </el-button>
            <el-button size="small" type="danger" plain @click="onResetSecret(row)">重置密钥</el-button>
          </template>
        </el-table-column>
      </el-table>
      <div class="pager">
        <el-pagination
          v-model:current-page="queryForm.pageNum"
          v-model:page-size="queryForm.pageSize"
          :page-sizes="[10, 20, 50]"
          :total="total"
          layout="total, sizes, prev, pager, next"
          @size-change="onSearch"
          @current-change="fetchList" />
      </div>
    </el-card>

    <el-dialog v-model="editVisible" :title="form.id ? '编辑应用' : '新建应用'" width="520px" destroy-on-close>
      <el-form :model="form" label-width="90px">
        <el-form-item label="AppID" required>
          <el-input v-model="form.appId" :disabled="!!form.id" placeholder="唯一应用标识" />
        </el-form-item>
        <el-form-item label="商户号" required>
          <el-input v-model="form.mchNo" placeholder="mchNo" />
        </el-form-item>
        <el-form-item label="应用名">
          <el-input v-model="form.appName" />
        </el-form-item>
        <el-form-item label="状态">
          <el-switch v-model="form.status" :active-value="1" :inactive-value="0" />
        </el-form-item>
        <el-form-item label="IP白名单">
          <el-input v-model="form.allowIps" type="textarea" :rows="2" placeholder="逗号分隔，空=不限" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="form.remark" type="textarea" :rows="2" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="editVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="onSave">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="secretVisible" title="应用密钥（仅显示一次）" width="560px" :close-on-click-modal="false">
      <el-alert type="warning" show-icon :closable="false" title="请立即复制并妥善保存，关闭后无法再次查看明文。" />
      <el-form label-width="90px" style="margin-top:16px">
        <el-form-item label="AppID"><el-input :model-value="secretInfo.appId" readonly /></el-form-item>
        <el-form-item label="AppSecret">
          <el-input :model-value="secretInfo.appSecret" readonly>
            <template #append>
              <el-button @click="copySecret">复制</el-button>
            </template>
          </el-input>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button type="primary" @click="secretVisible = false">已保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { openApiAppApi } from '@/api/payment/openApiApp'

const loading = ref(false)
const saving = ref(false)
const tableData = ref([])
const total = ref(0)
const editVisible = ref(false)
const secretVisible = ref(false)
const secretInfo = reactive({ appId: '', appSecret: '' })

const queryForm = reactive({
  pageNum: 1,
  pageSize: 10,
  appId: '',
  mchNo: '',
  status: undefined,
  keyword: ''
})

const form = reactive({
  id: null,
  appId: '',
  mchNo: '',
  appName: '',
  status: 1,
  allowIps: '',
  remark: ''
})

async function fetchList() {
  loading.value = true
  try {
    const res = await openApiAppApi.page({ ...queryForm })
    const data = res?.data ?? res
    tableData.value = data?.records || []
    total.value = Number(data?.total || 0)
  } catch (e) {
    ElMessage.error(e?.message || '查询失败')
  } finally {
    loading.value = false
  }
}

function onSearch() {
  queryForm.pageNum = 1
  fetchList()
}
function onReset() {
  queryForm.appId = ''
  queryForm.mchNo = ''
  queryForm.status = undefined
  queryForm.keyword = ''
  onSearch()
}

function onAdd() {
  Object.assign(form, { id: null, appId: '', mchNo: '', appName: '', status: 1, allowIps: '', remark: '' })
  editVisible.value = true
}

function onEdit(row) {
  Object.assign(form, {
    id: row.id,
    appId: row.appId,
    mchNo: row.mchNo,
    appName: row.appName || '',
    status: row.status,
    allowIps: row.allowIps || '',
    remark: row.remark || ''
  })
  editVisible.value = true
}

async function onSave() {
  if (!form.appId || !form.mchNo) {
    ElMessage.warning('AppID 与商户号必填')
    return
  }
  saving.value = true
  try {
    const res = await openApiAppApi.save({ ...form })
    const data = res?.data ?? res
    editVisible.value = false
    if (data?.appSecret) {
      secretInfo.appId = data.appId
      secretInfo.appSecret = data.appSecret
      secretVisible.value = true
    } else {
      ElMessage.success('已保存')
    }
    fetchList()
  } catch (e) {
    ElMessage.error(e?.message || '保存失败')
  } finally {
    saving.value = false
  }
}

async function onToggle(row) {
  const next = row.status === 1 ? 0 : 1
  try {
    await openApiAppApi.toggle({ id: row.id, status: next })
    ElMessage.success(next === 1 ? '已启用' : '已停用')
    fetchList()
  } catch (e) {
    ElMessage.error(e?.message || '操作失败')
  }
}

async function onResetSecret(row) {
  try {
    await ElMessageBox.confirm(`确认重置应用 ${row.appId} 的密钥？旧密钥将立即失效。`, '重置密钥', { type: 'warning' })
    const res = await openApiAppApi.resetSecret({ id: row.id })
    const data = res?.data ?? res
    if (data?.appSecret) {
      secretInfo.appId = data.appId
      secretInfo.appSecret = data.appSecret
      secretVisible.value = true
    }
    fetchList()
  } catch (e) {
    if (e !== 'cancel' && e !== 'close') {
      ElMessage.error(e?.message || '重置失败')
    }
  }
}

async function copySecret() {
  try {
    await navigator.clipboard.writeText(secretInfo.appSecret || '')
    ElMessage.success('已复制')
  } catch {
    ElMessage.warning('复制失败，请手动选择')
  }
}

onMounted(fetchList)
</script>

<style scoped>
.pager { margin-top: 12px; display: flex; justify-content: flex-end; }
</style>
