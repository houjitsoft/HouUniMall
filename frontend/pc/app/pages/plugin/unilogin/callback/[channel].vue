<template>
    <main class="unilogin-callback">
        <div class="callback-card" role="status" aria-live="polite">
            <div class="callback-icon" :class="`callback-icon--${status}`">
                {{ status === 'success' ? '✓' : status === 'failed' ? '!' : '…' }}
            </div>
            <h1>{{ title }}</h1>
            <p>{{ message }}</p>
        </div>
    </main>
</template>

<script setup lang="ts">
definePageMeta({ layout: 'full-screen' })

import { computed, onMounted, ref } from 'vue'
import { useRoute } from 'vue-router'
import { useUniLogin } from '~/composables/useUniLogin'
import { useUserStore } from '~/stores/modules/user'

const route = useRoute()
const userStore = useUserStore()
const { completeCallback } = useUniLogin()

const status = ref<'loading' | 'success' | 'failed'>('loading')
const message = ref('正在完成第三方登录，请稍候…')
const title = computed(() => status.value === 'success' ? '登录成功' : status.value === 'failed' ? '登录失败' : '正在登录')

const notifyParent = (payload: Record<string, unknown>) => {
    if (window.parent === window) return false
    window.parent.postMessage({ type: 'UNIMALL_UNILOGIN', ...payload }, window.location.origin)
    return true
}

onMounted(async () => {
    const channel = String(route.params.channel || '').trim().toLowerCase()
    const code = typeof route.query.code === 'string' ? route.query.code : ''
    const state = typeof route.query.state === 'string' ? route.query.state : ''

    if (!channel || !code || !state) {
        status.value = 'failed'
        message.value = '授权参数不完整，请返回登录页重新扫码。'
        notifyParent({ channel, status: 'failed', message: message.value })
        return
    }

    try {
        const data = await completeCallback(channel, code, state)
        if (data?.status === 'confirmed' && data.token) {
            status.value = 'success'
            message.value = '授权完成，正在进入商城…'
            if (!notifyParent({ channel, status: 'confirmed', token: data.token })) {
                await userStore.finishLogin(data.token)
                await navigateTo('/')
            }
            return
        }
        if (data?.status === 'unbound') {
            status.value = 'failed'
            message.value = data.message || '该账号尚未绑定，请返回登录页完成手机号绑定。'
            notifyParent({ channel, status: 'unbound', message: message.value })
            return
        }
        throw new Error(data?.message || '授权未完成')
    } catch (error: any) {
        status.value = 'failed'
        message.value = error?.message || '登录失败，请返回登录页重新扫码。'
        notifyParent({ channel, status: 'failed', message: message.value })
    }
})
</script>

<style scoped>
.unilogin-callback {
    min-height: 100vh;
    display: grid;
    place-items: center;
    padding: 24px;
    background: #f5f7fa;
}

.callback-card {
    width: min(420px, 100%);
    padding: 48px 32px;
    border-radius: 16px;
    background: #fff;
    box-shadow: 0 12px 36px rgba(15, 23, 42, 0.1);
    text-align: center;
}

.callback-icon {
    width: 56px;
    height: 56px;
    display: grid;
    place-items: center;
    margin: 0 auto 20px;
    border-radius: 50%;
    background: #e8f3ff;
    color: #1677ff;
    font-size: 28px;
    font-weight: 700;
}

.callback-icon--success {
    background: #e8f8ef;
    color: #07c160;
}

.callback-icon--failed {
    background: #fff3e8;
    color: #f59e0b;
}

h1 {
    margin: 0 0 12px;
    color: #111827;
    font-size: 22px;
}

p {
    margin: 0;
    color: #6b7280;
    font-size: 14px;
    line-height: 1.7;
}
</style>
