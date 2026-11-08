<template>
  <div class="merchant-qual-manage">
    <el-card class="filter-card">
      <el-form :inline="true" :model="queryForm" @submit.prevent="onSearch">
        <el-form-item label="终端">
          <el-select v-model="queryForm.terminal" placeholder="全部" clearable style="width:160px">
            <el-option v-for="t in TERMINAL_OPTIONS" :key="t.value" :label="t.label" :value="t.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="渠道">
          <el-select v-model="queryForm.channel" placeholder="全部" clearable style="width:140px">
            <el-option v-for="c in CHANNEL_OPTIONS" :key="c.value" :label="c.label" :value="c.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="环境">
          <el-select v-model="queryForm.env" placeholder="全部" clearable style="width:120px">
            <el-option v-for="e in ENV_OPTIONS" :key="e.value" :label="e.label" :value="e.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="queryForm.status" placeholder="全部" clearable style="width:120px">
            <el-option label="启用" :value="1" />
            <el-option label="停用" :value="0" />
          </el-select>
        </el-form-item>
        <el-form-item label="关键字">
          <el-input v-model="queryForm.keyword" placeholder="资质名称/AppID" clearable style="width:200px" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="onSearch">查询</el-button>
          <el-button @click="onReset">重置</el-button>
          <el-button type="success" @click="onAdd">新增资质</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card class="list-card" style="margin-top:12px">
      <el-table v-loading="loading" :data="tableData" border stripe>
        <el-table-column label="ID" prop="id" width="80" />
        <el-table-column label="资质名称" prop="qualName" min-width="160" show-overflow-tooltip />
        <el-table-column label="终端" prop="terminal" width="130">
          <template #default="{ row }">{{ terminalLabel(row.terminal) }}</template>
        </el-table-column>
        <el-table-column label="渠道" prop="channel" width="110">
          <template #default="{ row }">{{ channelLabel(row.channel) }}</template>
        </el-table-column>
        <el-table-column label="环境" prop="env" width="80">
          <template #default="{ row }">{{ row.env === 'S' ? '沙箱' : '生产' }}</template>
        </el-table-column>
        <el-table-column label="AppID" prop="appId" width="180" show-overflow-tooltip />
        <el-table-column label="商户号" prop="mchId" width="140" show-overflow-tooltip />
        <el-table-column label="状态" width="90">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'">{{ row.status === 1 ? '启用' : '停用' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="更新时间" prop="updatedAt" width="170" />
        <el-table-column label="操作" width="240" fixed="right">
          <template #default="{ row }">
            <el-button size="small" @click="onEdit(row)">编辑</el-button>
            <el-button size="small" :type="row.status === 1 ? 'warning' : 'success'" @click="onToggle(row)">
              {{ row.status === 1 ? '停用' : '启用' }}
            </el-button>
            <el-button size="small" type="danger" @click="onDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pager">
        <el-pagination
          v-model:current-page="queryForm.pageNum"
          v-model:page-size="queryForm.pageSize"
          :page-sizes="[10, 20, 50, 100]"
          :total="total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="onSearch"
          @current-change="fetchList" />
      </div>
    </el-card>

    <!-- 新增/编辑 弹窗 -->
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="780px" :close-on-click-modal="false" destroy-on-close>
      <el-form ref="formRef" :model="form" :rules="formRules" label-width="160px">
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="终端类型" prop="terminal">
              <el-select v-model="form.terminal" placeholder="请选择" :disabled="isEdit" style="width:100%">
                <el-option v-for="t in TERMINAL_OPTIONS" :key="t.value" :label="t.label" :value="t.value" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="支付渠道" prop="channel">
              <el-select v-model="form.channel" placeholder="请选择" :disabled="isEdit" style="width:100%" @change="onChannelChange">
                <el-option v-for="c in CHANNEL_OPTIONS" :key="c.value" :label="c.label" :value="c.value" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="环境" prop="env">
              <el-radio-group v-model="form.env" :disabled="isEdit">
                <el-radio v-for="e in ENV_OPTIONS" :key="e.value" :value="e.value">{{ e.label }}</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="资质名称" prop="qualName">
              <el-input v-model="form.qualName" placeholder="例：主体A-微信小程序-生产" />
            </el-form-item>
          </el-col>

          <!-- 差异化字段 -->
          <template v-for="(meta, key) in currentFieldRules" :key="key">
            <el-col :span="meta.textarea ? 24 : 12">
              <el-form-item
                :label="meta.label"
                :prop="key"
                :rules="meta.required ? [{ required: true, message: '请输入' + meta.label, trigger: 'blur' }] : []">
                <el-input
                  v-if="meta.textarea"
                  v-model="form[key]"
                  type="textarea"
                  :rows="4"
                  :placeholder="`请输入${meta.label}` + (meta.sensitive ? '（保存后将加密存储）' : '')" />
                <el-input
                  v-else
                  v-model="form[key]"
                  :type="meta.sensitive ? 'password' : 'text'"
                  :show-password="!!meta.sensitive"
                  :placeholder="meta.sensitive ? '已存在则显示 ****** 表示未修改' : `请输入${meta.label}`" />
              </el-form-item>
            </el-col>
          </template>

          <el-col :span="24">
            <el-form-item label="备注">
              <el-input v-model="form.remark" type="textarea" :rows="2" placeholder="可选" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="状态">
              <el-switch v-model="form.status" :active-value="1" :inactive-value="0" active-text="启用" inactive-text="停用" />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="onSubmit">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  merchantQualApi,
  TERMINAL_OPTIONS,
  CHANNEL_OPTIONS,
  ENV_OPTIONS,
  CHANNEL_FIELD_RULES,
  SENSITIVE_MASK
} from '@/api/payment/merchantQual'

const loading = ref(false)
const submitting = ref(false)
const tableData = ref([])
const total = ref(0)

const queryForm = reactive({
  pageNum: 1,
  pageSize: 10,
  terminal: '',
  channel: '',
  env: '',
  status: '',
  keyword: ''
})

const dialogVisible = ref(false)
const dialogTitle = ref('')
const isEdit = ref(false)
const formRef = ref(null)

const initialForm = () => ({
  id: undefined,
  terminal: '',
  channel: '',
  env: 'P',
  qualName: '',
  appId: '',
  appSecret: '',
  mchId: '',
  apiKey: '',
  apiPublicKey: '',
  notifyUrl: '',
  returnUrl: '',
  extConfig: '',
  status: 1,
  remark: ''
})
const form = reactive(initialForm())

const formRules = {
  terminal: [{ required: true, message: '请选择终端', trigger: 'change' }],
  channel: [{ required: true, message: '请选择渠道', trigger: 'change' }],
  env: [{ required: true, message: '请选择环境', trigger: 'change' }],
  qualName: [{ required: true, message: '请输入资质名称', trigger: 'blur' }]
}

const currentFieldRules = computed(() => CHANNEL_FIELD_RULES[form.channel] || {
  appId: { required: true, label: 'AppID' },
  appSecret: { required: false, label: 'AppSecret', sensitive: true },
  notifyUrl: { required: false, label: '回调 URL' }
})

const terminalLabel = (v) => TERMINAL_OPTIONS.find(t => t.value === v)?.label || v
const channelLabel = (v) => CHANNEL_OPTIONS.find(c => c.value === v)?.label || v

onMounted(fetchList)

async function fetchList() {
  loading.value = true
  try {
    const params = { ...queryForm }
    if (params.status === '') delete params.status
    const res = await merchantQualApi.page(params)
    if (res.code === 200 || res.success) {
      const data = res.data || {}
      tableData.value = data.records || []
      total.value = data.total || 0
    } else {
      ElMessage.error(res.message || '查询失败')
    }
  } catch (e) {
    ElMessage.error('查询异常：' + (e.message || e))
  } finally {
    loading.value = false
  }
}

function onSearch() {
  queryForm.pageNum = 1
  fetchList()
}

function onReset() {
  Object.assign(queryForm, { pageNum: 1, pageSize: 10, terminal: '', channel: '', env: '', status: '', keyword: '' })
  fetchList()
}

function onAdd() {
  isEdit.value = false
  dialogTitle.value = '新增商户资质'
  Object.assign(form, initialForm())
  dialogVisible.value = true
}

function onChannelChange() {
  // 切换渠道时清空旧字段，避免不同渠道字段相互污染
  if (!isEdit.value) {
    const keep = {
      terminal: form.terminal,
      channel: form.channel,
      env: form.env,
      qualName: form.qualName,
      status: form.status,
      remark: form.remark
    }
    Object.assign(form, initialForm(), keep)
  }
}

async function onEdit(row) {
  isEdit.value = true
  dialogTitle.value = '编辑商户资质'
  try {
    const res = await merchantQualApi.detail(row.id, true) // 编辑时解密
    if (res.code === 200 || res.success) {
      const data = res.data || {}
      Object.assign(form, initialForm(), data)
      dialogVisible.value = true
    } else {
      ElMessage.error(res.message || '加载详情失败')
    }
  } catch (e) {
    ElMessage.error('加载详情异常：' + (e.message || e))
  }
}

async function onSubmit() {
  await formRef.value.validate()
  submitting.value = true
  try {
    const payload = { ...form }
    // 编辑时若敏感字段未修改（仍为 ******）则置空，后端跳过覆盖
    if (isEdit.value) {
      const rules = currentFieldRules.value
      Object.keys(rules).forEach(k => {
        if (rules[k].sensitive && payload[k] === SENSITIVE_MASK) payload[k] = ''
      })
    }
    const res = await merchantQualApi.save(payload)
    if (res.code === 200 || res.success) {
      ElMessage.success('保存成功')
      dialogVisible.value = false
      fetchList()
    } else {
      ElMessage.error(res.message || '保存失败')
    }
  } catch (e) {
    ElMessage.error('保存异常：' + (e.message || e))
  } finally {
    submitting.value = false
  }
}

async function onToggle(row) {
  const target = row.status === 1 ? 0 : 1
  try {
    await ElMessageBox.confirm(`确定要${target === 1 ? '启用' : '停用'}该资质吗？`, '提示', { type: 'warning' })
    const res = await merchantQualApi.toggle({ id: row.id, status: target })
    if (res.code === 200 || res.success) {
      ElMessage.success('操作成功')
      fetchList()
    } else {
      ElMessage.error(res.message || '操作失败')
    }
  } catch (e) {
    if (e !== 'cancel') ElMessage.error('操作异常：' + (e.message || e))
  }
}

async function onDelete(row) {
  try {
    await ElMessageBox.confirm('确定要删除该资质吗？该操作为逻辑删除，可联系运维恢复。', '提示', { type: 'warning' })
    const res = await merchantQualApi.delete({ id: row.id })
    if (res.code === 200 || res.success) {
      ElMessage.success('删除成功')
      fetchList()
    } else {
      ElMessage.error(res.message || '删除失败')
    }
  } catch (e) {
    if (e !== 'cancel') ElMessage.error('删除异常：' + (e.message || e))
  }
}
</script>

<style scoped>
.merchant-qual-manage { padding: 16px; }
.pager { margin-top: 12px; text-align: right; }
</style>
