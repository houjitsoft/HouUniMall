<template>
  <!--
   渠道凭证管理（W4-SDK 联调阶段）
   - 渠道卡片：WECHAT_V3 / ALIPAY / STRIPE（可真联调）+ 骨架渠道可选显示
   - 卡片显示已/未配置状态 + 启停开关 + 测试连通
   - 点编辑弹窗按 channelCode 切换字段集，敏感字段提交 AES 加密
  -->
  <div class="channel-credential">
    <el-alert
      type="info"
      :closable="false"
      show-icon
      title="渠道编码与 PayMethodEnum / Strategy#getChannel 一致。可真联调：WECHAT_V3、ALIPAY、STRIPE。骨架渠道（YSF/FME/花呗/分账支付）仅占位，createPay 仍为 Mock。"
      style="margin-bottom: 12px;"
    />
    <div style="margin-bottom: 12px; display:flex; justify-content:flex-end;">
      <el-checkbox v-model="showStub">显示骨架/Mock 渠道</el-checkbox>
    </div>
    <el-alert
      title="渠道真实 SDK 联调说明"
      type="info"
      show-icon
      :closable="false"
      style="margin-bottom: 16px;"
    >
      <template #default>
        敏感字段（私钥 / API V3 Key / AppSecret 等）以 AES-256 字段级加密落库，列表/回显默认脱敏；
        点击「编辑」可重新填写，「测试连通」校验凭证完整性，缺失字段返回 <b>CHANNEL_NOT_CONFIGURED</b>。
      </template>
    </el-alert>

    <!-- 顶部操作栏 -->
    <el-card shadow="never" style="margin-bottom: 16px;">
      <el-form :inline="true" :model="query" @submit.prevent>
        <el-form-item label="商户号">
          <el-input v-model="query.merchantNo" placeholder="留空则查全局兜底配置" clearable style="width: 200px;" />
        </el-form-item>
        <el-form-item label="渠道">
          <el-select v-model="query.channelCode" clearable placeholder="全部渠道" style="width: 180px;">
            <el-option v-for="c in CHANNEL_LIST" :key="c.code" :label="c.name" :value="c.code" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadList">查询</el-button>
          <el-button @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 渠道卡片 -->
    <el-row :gutter="16">
      <el-col v-for="ch in CHANNEL_LIST" :key="ch.code" :xs="24" :sm="12" :md="12" :lg="8">
        <el-card class="channel-card" shadow="hover">
          <div class="card-head">
            <div class="ch-name">
              <el-icon :size="22" :color="ch.color"><component :is="ch.icon" /></el-icon>
              <span>{{ ch.name }}</span>
            </div>
            <el-tag :type="getChannelStatus(ch.code).tagType" size="small">
              {{ getChannelStatus(ch.code).text }}
            </el-tag>
          </div>
          <div class="card-desc">{{ ch.desc }}{{ ch.stubReason ? '（' + ch.stubReason + '）' : '' }}</div>
          <div class="card-fields">
            <div class="field-row" v-for="f in ch.fields" :key="f.key">
              <span class="field-label">{{ f.label }}：</span>
              <span class="field-value">{{ getFieldDisplay(ch.code, f.key) }}</span>
            </div>
          </div>
          <div class="card-actions">
            <el-button size="small" type="primary" @click="handleEdit(ch)">
              {{ getChannelStatus(ch.code).configured ? '编辑' : '配置' }}
            </el-button>
            <el-button
              size="small"
              :disabled="!getChannelStatus(ch.code).configured"
              @click="handleTest(ch)"
            >测试连通</el-button>
            <el-button
              size="small"
              :type="getChannelStatus(ch.code).enabled ? 'warning' : 'success'"
              :disabled="!getChannelStatus(ch.code).configured"
              @click="handleToggle(ch)"
            >{{ getChannelStatus(ch.code).enabled ? '停用' : '启用' }}</el-button>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 编辑弹窗（按 channelCode 切换动态表单） -->
    <el-dialog
      v-model="dialogVisible"
      :title="`${currentChannel?.name || ''} 凭证配置`"
      width="640px"
      :close-on-click-modal="false"
      @close="resetForm"
    >
      <el-form ref="formRef" :model="form" :rules="formRules" label-width="140px">
        <el-form-item label="商户号" prop="merchantNo">
          <el-input v-model="form.merchantNo" placeholder="留空表示该渠道全局兜底配置" />
        </el-form-item>
        <el-form-item label="渠道编码">
          <el-input :value="currentChannel?.code" disabled />
        </el-form-item>
        <template v-for="f in currentChannel?.fields || []" :key="f.key">
          <el-form-item :label="f.label" :prop="`config.${f.key}`" :rules="buildFieldRule(f)">
            <el-input
              v-if="f.type === 'textarea'"
              v-model="form.config[f.key]"
              type="textarea"
              :rows="4"
              :placeholder="f.placeholder || `请输入${f.label}`"
            />
            <el-input
              v-else
              v-model="form.config[f.key]"
              :placeholder="f.placeholder || `请输入${f.label}`"
              :type="f.sensitive ? 'password' : 'text'"
              :show-password="!!f.sensitive"
            />
          </el-form-item>
        </template>
        <el-form-item label="状态">
          <el-radio-group v-model="form.status">
            <el-radio :label="1">启用</el-radio>
            <el-radio :label="0">停用</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="form.remark" placeholder="可选，配置说明" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="handleSave">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, markRaw } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  ChatDotRound,
  Money,
  CreditCard,
  Briefcase,
  Wallet
} from '@element-plus/icons-vue'
import { channelConfigApi } from '@/api/payment/paymentBizW4'
import {
  CHANNEL_CATALOG,
  CHANNEL_MATURITY
} from '@/views/payment/channel/channelCatalog'

const showStub = ref(true)

/** 渠道字段定义（与后端 AbstractChannelPayStrategy.requiredFields / PayMethodEnum 对齐） */
const CHANNEL_LIST = computed(() => {
  const iconByCode = {
    WECHAT_V3: markRaw(ChatDotRound),
    ALIPAY: markRaw(Money),
    STRIPE: markRaw(Wallet),
    ALI_CREDIT: markRaw(Money),
    YSF_PAY: markRaw(CreditCard),
    FME_PAY: markRaw(Briefcase),
    SHARE_PAY: markRaw(Wallet)
  }
  return CHANNEL_CATALOG
    .filter((c) => showStub.value || c.maturity === CHANNEL_MATURITY.LIVE)
    .map((c) => ({
      ...c,
      icon: iconByCode[c.code] || markRaw(Wallet)
    }))
})

const query = reactive({ merchantNo: '', channelCode: '' })
const dataMap = ref({}) // { CHANNEL_CODE: row }
const dialogVisible = ref(false)
const currentChannel = ref(null)
const saving = ref(false)
const formRef = ref(null)

const form = reactive({
  id: null,
  merchantNo: '',
  channelCode: '',
  status: 1,
  remark: '',
  config: {}
})

const formRules = {
  merchantNo: []
}

/** 拉取渠道列表（脱敏分页） */
async function loadList() {
  try {
    const res = await channelConfigApi.list({
      pageNo: 1,
      pageSize: 50,
      merchantNo: query.merchantNo || undefined,
      channelCode: query.channelCode || undefined
    })
    const rows = res?.data?.records || res?.data?.list || res?.data || []
    const map = {}
    rows.forEach(r => {
      // 后端 channelCode 是字符串，按渠道+商户号优先取最新
      const k = r.channelCode
      if (!map[k] || (r.id || 0) > (map[k].id || 0)) map[k] = r
    })
    dataMap.value = map
  } catch (e) {
    ElMessage.error('加载渠道凭证失败：' + (e?.message || e))
  }
}

function resetQuery() {
  query.merchantNo = ''
  query.channelCode = ''
  loadList()
}

/** 渠道当前状态：configured / enabled */
function getChannelStatus(code) {
  const row = dataMap.value[code]
  if (!row) return { configured: false, enabled: false, text: '未配置', tagType: 'info' }
  const enabled = Number(row.status) === 1
  return {
    configured: true,
    enabled,
    text: enabled ? '已启用' : '已配置-停用',
    tagType: enabled ? 'success' : 'warning'
  }
}

/** 字段卡片简介展示（脱敏值） */
function getFieldDisplay(code, key) {
  const row = dataMap.value[code]
  if (!row) return '—'
  let cfg = row.configJson
  if (typeof cfg === 'string') {
    try { cfg = JSON.parse(cfg) } catch { cfg = {} }
  }
  const v = cfg?.[key]
  if (v === undefined || v === null || v === '') return '—'
  // 后端列表接口已脱敏；前端再做一次裁剪
  const s = String(v)
  if (s.length <= 8) return s
  return s.slice(0, 4) + '****' + s.slice(-4)
}

async function handleEdit(channel) {
  currentChannel.value = channel
  const row = dataMap.value[channel.code]
  // 重置
  form.id = null
  form.merchantNo = query.merchantNo || ''
  form.channelCode = channel.code
  form.status = 1
  form.remark = ''
  form.config = {}
  channel.fields.forEach(f => { form.config[f.key] = '' })
  if (row && row.id) {
    try {
      const res = await channelConfigApi.get(row.id)
      const data = res?.data || {}
      form.id = data.id
      form.merchantNo = data.merchantNo || ''
      form.channelCode = data.channelCode
      form.status = Number(data.status ?? 1)
      form.remark = data.remark || ''
      const cfg = data.configMap || {}
      channel.fields.forEach(f => {
        const value = cfg?.[f.key]
        form.config[f.key] = f.sensitive && value === '***' ? '' : (value ?? '')
      })
    } catch (e) {
      ElMessage.warning('回显失败，将以新建模式打开')
    }
  }
  dialogVisible.value = true
}

function buildFieldRule(f) {
  if (form.id && f.sensitive) return []
  if (!f.required) return []
  return [{ required: true, message: `请输入${f.label}`, trigger: 'blur' }]
}

async function handleSave() {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    saving.value = true
    try {
      const payload = {
        id: form.id || undefined,
        merchantNo: form.merchantNo || undefined,
        channelCode: form.channelCode,
        channelName: currentChannel.value?.name,
        payMethod: form.channelCode,
        provider: 'STRIPE' === form.channelCode ? 'STRIPE' : undefined,
        status: form.status,
        remark: form.remark,
        configMap: form.config || {}
      }
      await channelConfigApi.save(payload)
      ElMessage.success('保存成功，敏感字段已 AES 加密落库')
      dialogVisible.value = false
      await loadList()
    } catch (e) {
      ElMessage.error('保存失败：' + (e?.message || e))
    } finally {
      saving.value = false
    }
  })
}

async function handleToggle(channel) {
  const row = dataMap.value[channel.code]
  if (!row?.id) return
  const enabled = Number(row.status) === 1
  await ElMessageBox.confirm(
    `确认${enabled ? '停用' : '启用'} ${channel.name}？`,
    '提示',
    { type: 'warning' }
  )
  try {
    await channelConfigApi.toggle({ id: row.id, status: enabled ? 0 : 1 })
    ElMessage.success('操作成功')
    await loadList()
  } catch (e) {
    ElMessage.error('操作失败：' + (e?.message || e))
  }
}

async function handleTest(channel) {
  const row = dataMap.value[channel.code]
  if (!row?.id) return
  try {
    const res = await channelConfigApi.testConnect(row.id)
    const data = res?.data || {}
    if (data.success) {
      ElMessage.success(`${channel.name} 凭证完整，可进入真实 SDK 联调`)
    } else {
      ElMessageBox.alert(
        `errorCode: ${data.errorCode || 'CHANNEL_NOT_CONFIGURED'}\n缺失字段: ${(data.missing || []).join(', ') || '—'}`,
        `${channel.name} 测试连通失败`,
        { type: 'error' }
      )
    }
  } catch (e) {
    ElMessage.error('测试连通失败：' + (e?.message || e))
  }
}

function resetForm() {
  if (formRef.value) formRef.value.resetFields?.()
  currentChannel.value = null
}

onMounted(loadList)
</script>

<style scoped>
.channel-credential {
  padding: 16px;
}
.channel-card {
  margin-bottom: 16px;
}
.card-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}
.ch-name {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 16px;
  font-weight: 600;
}
.card-desc {
  color: #888;
  font-size: 12px;
  margin-bottom: 12px;
  min-height: 32px;
}
.card-fields {
  background: #fafafa;
  border-radius: 4px;
  padding: 8px 10px;
  margin-bottom: 12px;
  font-size: 12px;
  min-height: 88px;
}
.field-row {
  line-height: 22px;
}
.field-label {
  color: #666;
}
.field-value {
  color: #333;
  font-family: Menlo, Monaco, Consolas, monospace;
}
.card-actions {
  display: flex;
  gap: 6px;
  flex-wrap: wrap;
}
</style>
