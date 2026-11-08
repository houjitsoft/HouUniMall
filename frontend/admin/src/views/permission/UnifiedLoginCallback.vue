<template>
  <div class="unilogin-callback-page">
    <div class="unilogin-callback-card" role="status" aria-live="polite">
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
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { Check, Loading, Warning } from '@element-plus/icons-vue'
import request from '@/utils/request'
import { setAuthInfo } from '@/utils/auth'

const route = useRoute()
const router = useRouter()
const status = ref('processing')
const message = ref('正在完成第三方登录…')
const inIframe = Boolean(window.parent && window.parent !== window)
const channel = computed(() => String(route.params?.channel || '').trim().toLowerCase())

const goLogin = () => router.push('/login')

const notifyParent = (payload) => {
  if (!inIframe) return
  window.parent.postMessage(
    { type: 'HOUCLOUD_UNILOGIN', channel: channel.value, ...payload },
    window.location.origin
  )
}

onMounted(async () => {
  const code = typeof route.query?.code === 'string' ? route.query.code : ''
  const state = typeof route.query?.state === 'string' ? route.query.state : ''

  if (!channel.value || !code || !state) {
    status.value = 'error'
    message.value = '授权参数不完整，请返回登录页重新扫码。'
    notifyParent({ status: 'failed', message: message.value })
    return
  }

  try {
    const response = await request.get(`/web/auth/unilogin/${channel.value}/callback`, {
      params: { code, state },
      skipAuthRedirect: true
    })
    const data = response?.data || {}

    if (data.status === 'confirmed' && data.token) {
      status.value = 'success'
      message.value = '登录成功，正在进入…'
      notifyParent({ status: 'confirmed', ...data })
      if (!inIframe) {
        setAuthInfo(data.token, data)
        setTimeout(() => router.push('/'), 600)
      }
      return
    }

    if (data.status === 'unbound') {
      status.value = 'error'
      message.value = data.message || '该第三方账号尚未绑定平台账号。'
      notifyParent({ status: 'unbound', message: message.value })
      return
    }

    status.value = 'error'
    message.value = data.message || '登录失败，请返回登录页重新扫码。'
    notifyParent({ status: 'failed', message: message.value })
  } catch (error) {
    status.value = 'error'
    message.value = error?.message || '登录失败，请稍后重试。'
    notifyParent({ status: 'failed', message: message.value })
  }
})
</script>

<style scoped>
.unilogin-callback-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 24px;
  background: #f5f7fa;
}

.unilogin-callback-card {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 16px;
  width: min(420px, 100%);
  padding: 48px 32px;
  border-radius: 16px;
  background: #fff;
  box-shadow: 0 12px 36px rgba(15, 23, 42, 0.1);
}

.status-icon {
  font-size: 48px;
}

.is-processing {
  color: #409eff;
}

.is-success {
  color: #07c160;
}

.is-error {
  color: #e6a23c;
}

.status-text {
  max-width: 320px;
  margin: 0;
  color: #303133;
  font-size: 15px;
  line-height: 1.6;
  text-align: center;
}
</style>
