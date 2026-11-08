<template>
  <el-button
    v-if="entryVisible"
    class="alt-btn unilogin-btn"
    :class="{ 'unilogin-btn--off': enabled === false }"
    :style="brandStyle"
    :title="channel.title"
    :aria-label="`${channel.title}登录`"
    circle
    @click="openDialog"
  >
    <el-icon class="alt-icon"><component :is="iconComponent" /></el-icon>
  </el-button>

  <el-dialog
    v-model="visible"
    :title="channel.title"
    width="400px"
    class="unilogin-dialog"
    :style="brandStyle"
    @close="handleClose"
  >
    <div class="ul-content">
      <div class="ul-qr-box">
        <iframe
          v-if="status === 'ready' && scanUrl"
          :key="scanUrl"
          :src="scanUrl"
          class="ul-iframe"
          frameborder="0"
          scrolling="no"
          :title="`${channel.shortTitle}登录二维码`"
        ></iframe>

        <div v-else-if="status === 'loading'" class="ul-placeholder">
          <el-icon class="ul-icon"><Loading /></el-icon>
          <p class="ul-text">正在生成二维码…</p>
        </div>

        <div v-else-if="status === 'disabled'" class="ul-placeholder">
          <el-icon class="ul-icon"><Warning /></el-icon>
          <p class="ul-text">尚未配置{{ channel.shortTitle }}登录凭据</p>
          <p class="ul-hint">请前往「设置 → 第三方集成 → {{ channel.shortTitle }}登录」填写 AppID、AppSecret 与回调地址</p>
        </div>

        <div v-else-if="status === 'scanned'" class="ul-placeholder">
          <el-icon class="ul-icon"><Check /></el-icon>
          <p class="ul-text">已扫码，请在{{ channel.scanApp }}中确认</p>
        </div>

        <div v-else-if="status === 'unbound'" class="ul-placeholder">
          <el-icon class="ul-icon"><Warning /></el-icon>
          <p class="ul-text">该{{ channel.shortTitle }}账号尚未绑定平台账号</p>
          <p class="ul-hint">请先用账号密码登录，再到「账号设置」绑定{{ channel.shortTitle }}</p>
        </div>

        <div v-else class="ul-placeholder">
          <el-icon class="ul-icon"><RefreshRight /></el-icon>
          <p class="ul-text">二维码已失效，请点击刷新</p>
        </div>
      </div>

      <div class="ul-tips">
        <div v-for="(step, index) in channel.steps" :key="index" class="ul-tip">
          <el-icon><component :is="stepIcon(index)" /></el-icon>
          <span>{{ step }}</span>
        </div>
      </div>

      <!-- 部分浏览器可能拦截扫码页的 iframe 展示，提供新窗口兜底 -->
      <div v-if="status === 'ready'" class="ul-fallback">
        <el-link type="primary" :underline="false" @click="openInWindow">二维码未显示？在新窗口打开</el-link>
      </div>
    </div>

    <template #footer>
      <el-button @click="visible = false">取消</el-button>
      <el-button type="primary" :loading="refreshing" @click="refresh">刷新二维码</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { computed, onBeforeUnmount, onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { Check, Iphone, Loading, RefreshRight, Warning } from '@element-plus/icons-vue'
import { createAuthApi, fetchChannelStatusCached } from '../api.js'
import { getPluginContext } from '../context.js'

const props = defineProps({
  /** channels.js 中的渠道描述 */
  channel: { type: Object, required: true }
})

const authApi = computed(() => createAuthApi(props.channel))
const brandStyle = computed(() => ({ '--ul-brand': props.channel.brandColor || '#409eff' }))

const iconComponent = computed(() => props.channel.icon || 'Iphone')
const stepIcon = (index) => (index === 0 ? Iphone : index === 1 ? props.channel.icon || 'Iphone' : Check)

const visible = ref(false)
const refreshing = ref(false)
// loading / ready / scanned / unbound / expired / disabled
const status = ref('loading')
const scanUrl = ref('')
const qrCodeId = ref('')
/** 凭据是否已配置：null=未知，true=已配置，false=未配置（探测成功后明确） */
const enabled = ref(null)
/**
 * 是否在登录页展示入口：渠道表未显式关掉就展示（defaultVisible !== false）。
 * 「未配置凭据」不再隐藏入口 —— 隐藏会让人以为功能没做，
 * 正确做法是照样展示，点开弹窗给出「尚未配置 + 去哪里配」的引导。
 */
const entryVisible = ref(props.channel.defaultVisible !== false)

let pollTimer = null

const stopPolling = () => {
  if (pollTimer) {
    clearInterval(pollTimer)
    pollTimer = null
  }
}

/** 写入登录态并进入首页；宿主通过上下文回调完成，插件不接触宿主内部实现 */
const finishLogin = (payload) => {
  stopPolling()
  const ctx = getPluginContext()
  if (typeof ctx?.onLoginSuccess !== 'function') {
    ElMessage.error(`宿主未提供登录回调，无法完成${props.channel.shortTitle}登录`)
    return
  }
  status.value = 'scanned'
  visible.value = false
  ctx.onLoginSuccess(payload)
}

const fetchQrcode = async () => {
  status.value = 'loading'
  try {
    const data = await authApi.value.qrcode()
    // 只有显式 enabled===true 才算开通：未发版的桩接口不返回该字段，
    // 按「非 false 即启用」判定会把拼出来的假二维码当成可用二维码。
    if (data?.enabled !== true) {
      enabled.value = false
      status.value = 'disabled'
      return
    }
    enabled.value = true
    scanUrl.value = data?.qrCode || ''
    qrCodeId.value = data?.qrCodeId || data?.qrcodeId || ''
    status.value = scanUrl.value ? 'ready' : 'expired'
    if (status.value === 'ready') startPolling()
  } catch (error) {
    // 接口不可用（未发版 / 服务未就绪）时同样给出明确引导，绝不展示无法扫描的死二维码
    enabled.value = false
    status.value = 'disabled'
  }
}

const startPolling = () => {
  stopPolling()
  pollTimer = setInterval(async () => {
    if (!qrCodeId.value) return
    try {
      const data = await authApi.value.status(qrCodeId.value)
      const state = data?.status
      if (state === 'confirmed' && data?.token) {
        finishLogin(data)
      } else if (state === 'scanned') {
        status.value = 'scanned'
      } else if (state === 'unbound') {
        stopPolling()
        status.value = 'unbound'
        ElMessage.warning(data?.message || `该${props.channel.shortTitle}账号尚未绑定平台账号`)
      } else if (state === 'expired') {
        stopPolling()
        status.value = 'expired'
      }
    } catch (error) {
      // 轮询失败静默重试，避免打断用户扫码
    }
  }, 2000)
}

/** 回调页（iframe 内）完成授权后回传结果；按 channel 过滤，避免多渠道互相串扰 */
const handleMessage = (event) => {
  const payload = event?.data
  if (!payload || payload.type !== 'HOUCLOUD_UNILOGIN') return
  if (payload.channel && payload.channel !== props.channel.code) return
  if (payload.status === 'confirmed' && payload.token) {
    finishLogin(payload)
  } else if (payload.status === 'unbound') {
    stopPolling()
    status.value = 'unbound'
    ElMessage.warning(payload.message || `该${props.channel.shortTitle}账号尚未绑定平台账号`)
  } else if (payload.status === 'failed') {
    stopPolling()
    status.value = 'expired'
    ElMessage.warning(payload.message || `${props.channel.shortTitle}登录失败，请重新扫码`)
  }
}

const openInWindow = () => {
  if (!scanUrl.value) return
  window.open(scanUrl.value, `houcloud_unilogin_${props.channel.code}`, 'width=620,height=620')
  // 新窗口授权完成后同样会回调并更新状态，这里继续轮询即可
  if (!pollTimer) startPolling()
}

const openDialog = async () => {
  visible.value = true
  // 已探明未配置凭据：直接落到引导态，不发无意义的取码请求
  if (enabled.value === false) {
    status.value = 'disabled'
    return
  }
  if (status.value !== 'ready') await fetchQrcode()
  else startPolling()
}

const refresh = async () => {
  refreshing.value = true
  stopPolling()
  try {
    if (qrCodeId.value) await authApi.value.cancel(qrCodeId.value).catch(() => {})
    await fetchQrcode()
  } finally {
    refreshing.value = false
  }
}

const handleClose = () => {
  stopPolling()
  if (qrCodeId.value) authApi.value.cancel(qrCodeId.value).catch(() => {})
  qrCodeId.value = ''
  status.value = enabled.value ? 'expired' : 'disabled'
}

onMounted(() => {
  window.addEventListener('message', handleMessage)
  // 登录页可能同时注册多个渠道，这里不预取二维码（避免一进登录页就打 N 个请求），
  // 只做一次共享的开通状态探测：已配置的渠道才展示入口，未配置的不占位置。
  resolveEntryVisible()
})

/**
 * 预热本渠道的开通状态（凭据是否已配置）。
 *
 * 只用于「点开弹窗时直接落到 disabled 提示、不发无效请求」，不参与入口可见性：
 * 入口恒展示，未配置的渠道点开给配置引导。探测失败保持 enabled=null，
 * 交由 openDialog 时真实拉取二维码去判定。
 */
const resolveEntryVisible = async () => {
  try {
    const list = await fetchChannelStatusCached()
    const items = Array.isArray(list) ? list : (Array.isArray(list?.data) ? list.data : null)
    if (!items) return
    const mine = items.find((item) => item && item.code === props.channel.code)
    if (mine) enabled.value = mine.enabled === true
  } catch (error) {
    // 探测失败不隐藏入口，保持 enabled=null 由打开弹窗时判定
  }
}

onBeforeUnmount(() => {
  window.removeEventListener('message', handleMessage)
  stopPolling()
})
</script>

<style>
/* 只展示品牌色圆形图标，渠道名走 title 提示，保证一行排完 */
.unilogin-btn {
  flex: 0 0 auto;
  width: 40px;
  min-width: 40px;
  max-width: 40px;
  height: 40px;
  padding: 0;
  gap: 0;
  border-radius: 50%;
  background: var(--ul-brand, #409eff);
  border-color: var(--ul-brand, #409eff);
  color: #fff;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.12);
}

.unilogin-btn .alt-icon {
  font-size: 18px;
  color: #fff;
}

.unilogin-btn:hover {
  background: var(--ul-brand, #409eff);
  border-color: var(--ul-brand, #409eff);
  color: #fff;
  opacity: 0.88;
}

.unilogin-btn--off {
  opacity: 0.4;
  box-shadow: none;
}

.unilogin-dialog .el-dialog__title {
  color: var(--ul-brand, #409eff);
}

.ul-content {
  text-align: center;
}

.ul-qr-box {
  position: relative;
  width: 220px;
  height: 220px;
  margin: 0 auto;
  border: 1px solid #ebeef5;
  border-radius: 10px;
  background: #fafafa;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
}

.ul-iframe {
  width: 100%;
  height: 100%;
  border: 0;
}

.ul-placeholder {
  padding: 12px;
}

.ul-icon {
  font-size: 32px;
  color: var(--ul-brand, #409eff);
}

.ul-text {
  margin: 10px 0 4px;
  font-size: 14px;
  color: #303133;
}

.ul-hint {
  margin: 0;
  font-size: 12px;
  color: #909399;
  line-height: 1.5;
}

.ul-tips {
  margin-top: 16px;
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.ul-tip {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  font-size: 13px;
  color: #909399;
}

.ul-fallback {
  margin-top: 10px;
  font-size: 13px;
}
</style>
