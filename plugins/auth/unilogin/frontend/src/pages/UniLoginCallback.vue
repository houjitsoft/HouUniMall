<template>
  <div class="ul-callback" :class="{ 'ul-callback--overlay': inIframe }">
    <el-icon class="ul-callback-icon"><Loading /></el-icon>
    <p class="ul-callback-text">{{ message }}</p>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Loading } from '@element-plus/icons-vue'
import { createAuthApi } from '../api.js'
import { getPluginContext } from '../context.js'
import { DEFAULT_CHANNEL, getChannel } from '../channels.js'

const route = useRoute()
const message = ref('正在完成第三方登录…')
/** 在 iframe 中回调时，用全屏遮罩盖住宿主的侧栏/顶栏，只保留状态提示 */
const inIframe = ref(false)

/** 渠道由回调路径决定：/plugin/unilogin/callback/{channel}，缺省回落默认渠道 */
const channel = computed(() => getChannel(route.params?.channel) || getChannel(DEFAULT_CHANNEL))
const authApi = computed(() => createAuthApi(channel.value))
const shortTitle = computed(() => channel.value.shortTitle)

const postToOpener = (payload) => {
  try {
    if (window.parent && window.parent !== window) {
      window.parent.postMessage(
        { type: 'HOUCLOUD_UNILOGIN', channel: channel.value.code, ...payload },
        '*'
      )
      return true
    }
  } catch (error) {
    // 跨域或不可用时忽略，走独立打开分支
  }
  return false
}

onMounted(async () => {
  inIframe.value = (() => {
    try {
      return Boolean(window.parent && window.parent !== window)
    } catch (error) {
      return false
    }
  })()
  const { code, state } = route.query || {}
  if (!code || !state) {
    message.value = '授权参数不完整，请返回登录页重新扫码。'
    postToOpener({ status: 'failed', message: message.value })
    return
  }

  try {
    const data = await authApi.value.callback(code, state)
    if (data?.status === 'confirmed' && data?.token) {
      message.value = '登录成功，正在进入…'
      const posted = postToOpener({
        status: 'confirmed',
        token: data.token,
        userId: data.userId,
        userMobile: data.userMobile
      })
      // 独立打开（非 iframe）时自行完成登录
      if (!posted) {
        const ctx = getPluginContext()
        if (typeof ctx?.onLoginSuccess === 'function') ctx.onLoginSuccess(data)
        else ElMessage.error('宿主未提供登录回调')
      }
      return
    }
    if (data?.status === 'unbound') {
      message.value = data.message || `该${shortTitle.value}账号尚未绑定平台账号`
      postToOpener({ status: 'unbound', message: message.value })
      return
    }
    message.value = '登录失败，请返回登录页重新扫码。'
    postToOpener({ status: 'failed', message: message.value })
  } catch (error) {
    message.value = error?.message || `${shortTitle.value}登录失败，请稍后重试。`
    postToOpener({ status: 'failed', message: message.value })
  }
})
</script>

<style>
.ul-callback {
  min-height: 60vh;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 12px;
  color: #606266;
}

.ul-callback-icon {
  font-size: 28px;
  color: #409eff;
}

.ul-callback-text {
  font-size: 14px;
}

/* 扫码弹窗内的 iframe 回调：铺满并置顶，遮住宿主框架，只展示登录状态 */
.ul-callback--overlay {
  position: fixed;
  inset: 0;
  z-index: 9999;
  min-height: 100vh;
  background: #fff;
}
</style>
