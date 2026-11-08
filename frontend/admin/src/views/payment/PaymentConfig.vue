<template>
  <div class="payment-config">
    <el-alert
      type="info"
      :closable="false"
      show-icon
      title="支付设置与渠道凭证共用 hou_pay_channel；渠道编码必须与收银台 Strategy 一致（WECHAT_V3 / ALIPAY / STRIPE）。骨架渠道仅供联调占位，不会真扣款。"
      style="margin-bottom: 12px;"
    />

    <el-card class="box-card">
      <template #header>
        <div class="card-header">
          <span>支付配置</span>
          <div class="header-actions">
            <el-checkbox v-model="showStub">显示骨架/Mock 渠道</el-checkbox>
            <el-button type="primary" @click="handleAddConfig">新增配置</el-button>
          </div>
        </div>
      </template>

      <el-table :data="configList" style="width: 100%" v-loading="loading">
        <el-table-column prop="channelName" label="支付渠道" min-width="160" />
        <el-table-column prop="channelCode" label="渠道编码" width="140">
          <template #default="{ row }">
            <el-tag size="small">{{ row.channelCode }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="成熟度" width="120">
          <template #default="{ row }">
            <el-tag v-if="maturityOf(row.channelCode) === 'live'" type="success" size="small">可真联调</el-tag>
            <el-tag v-else type="warning" size="small">骨架/Mock</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="merchantNo" label="商户号" width="140">
          <template #default="{ row }">{{ row.merchantNo || '全局' }}</template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'">
              {{ row.status === 1 ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="updateTime" label="更新时间" width="180">
          <template #default="{ row }">{{ formatDate(row.updateTime || row.createTime) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="220" fixed="right">
          <template #default="{ row }">
            <el-button size="small" @click="handleEdit(row)">编辑凭证</el-button>
            <el-button size="small" @click="handleToggleStatus(row)">
              {{ row.status === 1 ? '禁用' : '启用' }}
            </el-button>
            <el-button size="small" type="danger" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-container">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :page-sizes="[10, 20, 50, 100]"
          :total="total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="fetchConfigList"
          @current-change="fetchConfigList"
        />
      </div>
    </el-card>

    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="640px"
      :close-on-click-modal="false"
      destroy-on-close
    >
      <el-alert
        v-if="selectedChannel?.maturity === 'stub'"
        type="warning"
        :closable="false"
        show-icon
        :title="selectedChannel.stubReason || '该渠道仅为骨架/Mock，保存凭证也不会走真实扣款'"
        style="margin-bottom: 12px;"
      />
      <el-form :model="configForm" :rules="configRules" ref="configFormRef" label-width="140px">
        <el-form-item label="支付渠道" prop="channelCode">
          <el-select
            v-model="configForm.channelCode"
            placeholder="请选择支付渠道"
            :disabled="isEdit"
            style="width: 100%;"
            @change="onChannelChange"
          >
            <el-option
              v-for="item in channelOptions"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="商户号 merchantNo">
          <el-input v-model="configForm.merchantNo" placeholder="留空=全局凭证；多商户时填业务商户号" />
        </el-form-item>
        <template v-if="selectedChannel">
          <el-form-item
            v-for="f in selectedChannel.fields"
            :key="f.key"
            :label="f.label"
            :prop="'configMap.' + f.key"
            :rules="f.required ? [{ required: true, message: `请输入${f.label}`, trigger: 'blur' }] : []"
          >
            <el-input
              v-model="configForm.configMap[f.key]"
              :type="f.type === 'textarea' ? 'textarea' : (f.sensitive ? 'password' : 'text')"
              :show-password="!!f.sensitive && f.type !== 'textarea'"
              :rows="f.type === 'textarea' ? 4 : undefined"
              :placeholder="f.placeholder || ''"
              autocomplete="off"
            />
          </el-form-item>
        </template>
        <el-form-item label="备注">
          <el-input v-model="configForm.remark" type="textarea" placeholder="可选备注" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="handleSubmit">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { channelConfigApi } from '@/api/payment/paymentBizW4'
import { payApi } from '@/api/payment/pay'
import {
  CHANNEL_CATALOG,
  channelSelectOptions,
  findChannel
} from '@/views/payment/channel/channelCatalog'

const loading = ref(false)
const submitLoading = ref(false)
const dialogVisible = ref(false)
const isEdit = ref(false)
const dialogTitle = ref('')
const configFormRef = ref(null)
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const configList = ref([])
const showStub = ref(false)

const configForm = reactive({
  id: '',
  channelCode: '',
  channelName: '',
  provider: '',
  payMethod: '',
  merchantNo: '',
  remark: '',
  status: 1,
  configMap: {}
})

const configRules = {
  channelCode: [{ required: true, message: '请选择支付渠道', trigger: 'change' }]
}

const channelOptions = computed(() => channelSelectOptions({ includeStub: showStub.value }))
const selectedChannel = computed(() => findChannel(configForm.channelCode))

function maturityOf(code) {
  return findChannel(code)?.maturity || 'stub'
}

function resetConfigMap(channel) {
  const map = {}
  ;(channel?.fields || []).forEach((f) => { map[f.key] = '' })
  configForm.configMap = map
}

function onChannelChange(code) {
  const ch = findChannel(code)
  configForm.channelName = ch?.name || ''
  configForm.provider = ch?.provider || ''
  configForm.payMethod = code
  resetConfigMap(ch)
}

onMounted(() => { fetchConfigList() })

async function fetchConfigList() {
  loading.value = true
  try {
    const response = await channelConfigApi.list({
      page: currentPage.value,
      size: pageSize.value,
      pageNum: currentPage.value,
      pageSize: pageSize.value
    })
    if (response.code === 200 || response.success) {
      const data = response.data || {}
      let rows = data.records || data.list || []
      if (!showStub.value) {
        rows = rows.filter((r) => maturityOf(r.channelCode) === 'live')
      }
      configList.value = rows
      total.value = data.total || rows.length
    } else {
      ElMessage.error(response.message || '获取支付配置失败')
      configList.value = []
      total.value = 0
    }
  } catch (error) {
    console.error(error)
    ElMessage.error('获取支付配置失败: ' + (error.message || '网络错误'))
    configList.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}

function formatDate(dateStr) {
  if (!dateStr) return '-'
  const date = new Date(dateStr)
  if (isNaN(date.getTime())) return '-'
  return date.toLocaleString('zh-CN')
}

function handleAddConfig() {
  isEdit.value = false
  dialogTitle.value = '新增支付配置'
  Object.assign(configForm, {
    id: '',
    channelCode: 'WECHAT_V3',
    channelName: '',
    provider: '',
    payMethod: '',
    merchantNo: '',
    remark: '',
    status: 1,
    configMap: {}
  })
  onChannelChange('WECHAT_V3')
  dialogVisible.value = true
}

async function handleEdit(row) {
  isEdit.value = true
  dialogTitle.value = `编辑凭证 · ${row.channelCode}`
  Object.assign(configForm, {
    id: row.id,
    channelCode: row.channelCode,
    channelName: row.channelName,
    provider: row.provider,
    payMethod: row.payMethod || row.channelCode,
    merchantNo: row.merchantNo || '',
    remark: row.remark || '',
    status: row.status,
    configMap: {}
  })
  resetConfigMap(findChannel(row.channelCode))
  try {
    const res = await channelConfigApi.get(row.id)
    if (res.code === 200 && res.data) {
      const data = res.data
      const cfg = data.configMap || data.config || {}
      Object.keys(configForm.configMap).forEach((k) => {
        if (cfg[k] != null) configForm.configMap[k] = String(cfg[k])
      })
      // 兼容历史明文散落字段
      ;['appId', 'mchId', 'merchantId', 'merchantKey', 'apiV3Key'].forEach((k) => {
        if (data[k] != null && configForm.configMap[k] !== undefined && !configForm.configMap[k]) {
          configForm.configMap[k] = String(data[k])
        }
      })
      if (data.merchantId && configForm.configMap.mchId !== undefined && !configForm.configMap.mchId) {
        configForm.configMap.mchId = String(data.merchantId)
      }
      if (data.merchantKey && configForm.configMap.apiV3Key !== undefined && !configForm.configMap.apiV3Key) {
        configForm.configMap.apiV3Key = String(data.merchantKey)
      }
    }
  } catch (e) {
    console.warn('load credential detail failed', e)
  }
  dialogVisible.value = true
}

async function handleSubmit() {
  try {
    await configFormRef.value.validate()
  } catch {
    return
  }
  const ch = findChannel(configForm.channelCode)
  if (!ch) {
    ElMessage.error('未知渠道编码')
    return
  }
  submitLoading.value = true
  try {
    const payload = {
      id: configForm.id || undefined,
      channelCode: configForm.channelCode,
      channelName: ch.name,
      provider: ch.provider,
      payMethod: configForm.channelCode,
      merchantNo: configForm.merchantNo || null,
      remark: configForm.remark,
      status: configForm.status ?? 1,
      configMap: { ...configForm.configMap }
    }
    const response = await channelConfigApi.save(payload)
    if (response.code === 200 || response.success) {
      ElMessage.success('保存成功（已按 Strategy 渠道码写入 hou_pay_channel）')
      dialogVisible.value = false
      fetchConfigList()
    } else {
      ElMessage.error(response.message || '保存失败')
    }
  } catch (error) {
    ElMessage.error('保存失败: ' + (error.message || '网络错误'))
  } finally {
    submitLoading.value = false
  }
}

async function handleToggleStatus(row) {
  const next = row.status === 1 ? 0 : 1
  try {
    await ElMessageBox.confirm(`确认${next === 1 ? '启用' : '禁用'} ${row.channelCode}？`, '提示', { type: 'warning' })
    const response = await channelConfigApi.toggle({ id: row.id, status: next })
    if (response.code === 200 || response.success) {
      ElMessage.success('状态已更新')
      fetchConfigList()
    } else {
      ElMessage.error(response.message || '操作失败')
    }
  } catch (e) {
    if (e !== 'cancel') ElMessage.error(e.message || '操作失败')
  }
}

async function handleDelete(row) {
  try {
    await ElMessageBox.confirm(`确认删除 ${row.channelCode} 配置？`, '警告', { type: 'warning' })
    const response = await payApi.deletePaymentConfig({ id: row.id })
    if (response.code === 200 || response.success) {
      ElMessage.success('删除成功')
      fetchConfigList()
    } else {
      ElMessage.error(response.message || '删除失败')
    }
  } catch (e) {
    if (e !== 'cancel') ElMessage.error(e.message || '删除失败')
  }
}
</script>

<style scoped>
.payment-config { padding: 0; }
.card-header { display: flex; justify-content: space-between; align-items: center; }
.header-actions { display: flex; gap: 12px; align-items: center; }
.pagination-container { margin-top: 16px; display: flex; justify-content: flex-end; }
</style>
