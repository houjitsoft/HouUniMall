<template>
  <div class="ul-settings">
    <el-card shadow="never">
      <template #header>
        <div class="ul-settings-header">
          <span>统一登录（UniLogin）</span>
          <div class="ul-settings-actions">
            <el-tag :type="enabledCount > 0 ? 'success' : 'info'">
              已开通 {{ enabledCount }} / {{ channels.length }} 个渠道
            </el-tag>
            <el-button link type="primary" :loading="loading" @click="loadAll">刷新状态</el-button>
          </div>
        </div>
      </template>

      <el-tabs v-model="activeCode" class="ul-tabs">
        <el-tab-pane v-for="item in channels" :key="item.code" :name="item.code">
          <template #label>
            <span class="ul-tab-label">
              {{ item.shortTitle }}
              <el-badge v-if="statusMap[item.code]" is-dot type="success" />
            </span>
          </template>

          <el-alert type="info" :closable="false" show-icon title="使用前准备" class="ul-alert">
            <p v-for="(step, index) in item.guide" :key="index">
              <span v-if="step.text">{{ step.text }}</span>
              <a v-if="step.link" :href="step.link.url" target="_blank" rel="noopener">{{ step.link.label }}</a>
              <code v-if="step.code">{{ step.code }}</code>
              <span v-if="step.tail">{{ step.tail }}</span>
            </p>
          </el-alert>

          <el-form :model="forms[item.code]" label-width="140px" class="ul-form">
            <el-form-item label="启用">
              <el-switch v-model="forms[item.code].enabled" />
              <span class="ul-form-hint ul-inline-hint">
                关闭后登录页不会再出现该渠道入口（凭据会保留）
              </span>
            </el-form-item>

            <el-form-item
              v-for="field in item.configFields"
              :key="field.key"
              :label="field.label"
              :required="field.required === true"
            >
              <el-input
                v-model="forms[item.code][field.key]"
                :type="field.type === 'password' ? 'password' : 'text'"
                :show-password="field.type === 'password'"
                :placeholder="field.hint || field.label"
              />
            </el-form-item>

            <el-form-item label="回调地址">
              <el-input :model-value="callbackUrlOf(item)" readonly>
                <template #append>
                  <el-button @click="copy(callbackUrlOf(item))">复制</el-button>
                </template>
              </el-input>
              <div class="ul-form-hint">
                需要在{{ item.scanApp }}开放平台登记为授权回调地址（保存时自动写入，无需手填）。
              </div>
            </el-form-item>

            <el-form-item>
              <el-button type="primary" :loading="savingCode === item.code" @click="save(item)">保存</el-button>
              <el-button @click="loadOne(item)">重新载入</el-button>
            </el-form-item>
          </el-form>
        </el-tab-pane>
      </el-tabs>
    </el-card>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { CHANNELS } from '../channels.js'
import { createConfigApi, fetchChannelStatus } from '../api.js'

const channels = CHANNELS
const activeCode = ref(CHANNELS[0]?.code || '')
const savingCode = ref('')
const loading = ref(false)
/** 渠道编码 -> 服务端是否已配置凭据 */
const statusMap = reactive({})

const forms = reactive(
  CHANNELS.reduce((acc, item) => {
    const form = { enabled: true }
    item.configFields.forEach((field) => {
      form[field.key] = ''
    })
    acc[item.code] = form
    return acc
  }, {})
)

const enabledCount = computed(() => Object.values(statusMap).filter(Boolean).length)

/** 回调地址必须是绝对地址（第三方要求），域名取当前站点 */
const callbackUrlOf = (item) => `${window.location.origin}${item.callbackPath}`

const copy = async (text) => {
  try {
    await navigator.clipboard.writeText(text)
    ElMessage.success('已复制回调地址')
  } catch (error) {
    ElMessage.warning(`复制失败，请手动复制：${text}`)
  }
}

const loadStatus = async () => {
  try {
    const list = await fetchChannelStatus()
    const items = Array.isArray(list) ? list : (Array.isArray(list?.data) ? list.data : [])
    channels.forEach((item) => {
      const mine = items.find((entry) => entry && entry.code === item.code)
      statusMap[item.code] = !!(mine && mine.enabled === true)
    })
  } catch (error) {
    // 接口不可用时不谎报状态：全部按「未知/未开通」展示，保存时仍会走同一条通道
    channels.forEach((item) => {
      statusMap[item.code] = false
    })
  }
}

const loadOne = async (item) => {
  loading.value = true
  try {
    const data = await createConfigApi(item).get()
    const own = data?.[item.configKey] || {}
    item.configFields.forEach((field) => {
      forms[item.code][field.key] = own[field.key] || ''
    })
    // 后端未显式关闭即视为启用，与渠道凭据解析口径一致
    forms[item.code].enabled = data?.enabled !== false
  } catch (error) {
    // 尚未配置属正常情况，保持表单为空
  } finally {
    loading.value = false
  }
}

const loadAll = async () => {
  await Promise.all([loadStatus(), ...channels.map((item) => loadOne(item))])
}

const save = async (item) => {
  const form = forms[item.code]
  const missing = item.configFields
    .filter((field) => field.required === true && !String(form[field.key] || '').trim())
    .map((field) => field.label)

  if (form.enabled && missing.length > 0) {
    ElMessage.warning(`启用时 ${missing.join('、')} 不能为空`)
    return
  }

  const payload = { enabled: form.enabled, description: `${item.shortTitle}登录` }
  payload[item.configKey] = { callbackUrl: callbackUrlOf(item) }
  item.configFields.forEach((field) => {
    payload[item.configKey][field.key] = String(form[field.key] || '').trim()
  })

  savingCode.value = item.code
  try {
    await createConfigApi(item).save(payload)
    ElMessage.success(`${item.shortTitle}配置已保存，最长 1 分钟内在登录页生效`)
    await loadStatus()
  } catch (error) {
    ElMessage.error(error?.message || '保存失败，请稍后重试')
  } finally {
    savingCode.value = ''
  }
}

onMounted(loadAll)
</script>

<style>
.ul-settings {
  padding: 16px;
}

.ul-settings-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.ul-settings-actions {
  display: flex;
  align-items: center;
  gap: 12px;
}

.ul-tab-label {
  display: inline-flex;
  align-items: center;
  gap: 4px;
}

.ul-alert {
  margin-bottom: 20px;
}

.ul-alert p {
  margin: 4px 0;
  font-size: 13px;
  line-height: 1.6;
}

.ul-form {
  max-width: 680px;
}

.ul-form-hint {
  margin-top: 4px;
  font-size: 12px;
  color: #909399;
}

.ul-inline-hint {
  margin-top: 0;
  margin-left: 12px;
}
</style>
