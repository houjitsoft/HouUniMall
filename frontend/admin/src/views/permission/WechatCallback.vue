<template>
  <div class="wechat-callback-page">
    <div class="wechat-callback-card">
      <el-icon v-if="status === 'processing'" class="status-icon is-processing"><Loading /></el-icon>
      <el-icon v-else-if="status === 'success'" class="status-icon is-success"><Check /></el-icon>
      <el-icon v-else class="status-icon is-error"><Warning /></el-icon>

      <p class="status-text">{{ message }}</p>

      <el-button v-if="status !== 'processing' && !inIframe" type="primary" @click="goLogin">
        返回登录页
      </el-button>
    </div>
  </div>
</template>

<script setup>
/**
 * 微信快捷登录回调页
 *
 * 微信开放平台在用户扫码确认后，会携带 code/state 回跳到配置的回调地址，本页负责：
 * 1. 用 code 换取平台登录态（/web/auth/wechat/callback）
 * 2. 若本页在登录页弹窗的 iframe 中，通过 postMessage 把结果回传给父窗口，由父窗口完成登录
 * 3. 若用户是直接打开（非 iframe），则本页自行写入登录态并跳转首页
 *
 * 面向中文用户的瞬态过渡页，文案直接使用中文。
 */
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { Loading, Check, Warning } from '@element-plus/icons-vue'
import { authService } from '@/api/auth/auth.js'
import { setAuthInfo } from '@/utils/auth'

const router = useRouter()
const status = ref('processing')
const message = ref('正在完成微信登录…')
const inIframe = window.parent && window.parent !== window

const WECHAT_LOGIN_MESSAGE = 'HOUCLOUD_WECHAT_LOGIN'

const goLogin = () => router.push('/login')

/** 把登录结果回传给打开扫码弹窗的父窗口 */
const notifyParent = (payload) => {
  try {
    if (inIframe) {
      window.parent.postMessage(
        { type: WECHAT_LOGIN_MESSAGE, ...payload },
        window.location.origin
      )
    }
  } catch (error) {
    console.warn('[微信登录] 回传父窗口失败', error)
  }
}

onMounted(async () => {
  const params = new URLSearchParams(window.location.search)
  const code = params.get('code')
  const state = params.get('state')

  if (!code || !state) {
    status.value = 'error'
    message.value = '微信授权参数缺失，请重新扫码登录。'
    notifyParent({ status: 'failed', message: message.value })
    return
  }

  try {
    const response = await authService.wechatCallback(code, state)
    const data = response?.data || {}

    if (data.status === 'confirmed' && data.token) {
      status.value = 'success'
      message.value = '微信登录成功，正在跳转…'
      notifyParent({ status: 'confirmed', ...data })

      // 顶层窗口（非弹窗内嵌）时自行落登录态并跳转
      if (!inIframe) {
        setAuthInfo(data.token, data)
        setTimeout(() => router.push('/'), 600)
      }
      return
    }

    if (data.status === 'unbound') {
      status.value = 'error'
      message.value = data.message || '该微信账号尚未绑定平台账号，请先用账号密码登录后绑定微信。'
      notifyParent({ status: 'unbound', message: message.value })
      return
    }

    status.value = 'error'
    message.value = data.message || '微信登录失败，请重新扫码。'
    notifyParent({ status: 'failed', message: message.value })
  } catch (error) {
    console.error('[微信登录] 回调处理失败', error)
    status.value = 'error'
    message.value = error?.response?.data?.message || '微信登录失败，请稍后重试。'
    notifyParent({ status: 'failed', message: message.value })
  }
})
</script>

<style scoped>
.wechat-callback-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f5f7fa;
}

.wechat-callback-card {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 16px;
  padding: 40px 48px;
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.06);
}

.status-icon {
  font-size: 48px;
}

.is-processing {
  color: #409eff;
}

.is-success {
  color: #67c23a;
}

.is-error {
  color: #e6a23c;
}

.status-text {
  margin: 0;
  font-size: 15px;
  color: #303133;
  text-align: center;
  max-width: 320px;
  line-height: 1.6;
}
</style>
