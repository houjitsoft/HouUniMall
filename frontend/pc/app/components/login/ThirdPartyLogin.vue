<template>
    <div v-if="channels.length" class="third-party-login">
        <div class="tp-divider">
            <span>{{ $t('其他登录方式') }}</span>
        </div>

        <div class="tp-buttons">
            <button
                v-for="item in channels"
                :key="item.code"
                type="button"
                class="tp-btn"
                :class="{ 'tp-btn--off': item.enabled === false }"
                :title="item.name"
                :aria-label="`${item.name}登录`"
                @click="open(item)"
            >
                <svg
                    class="tp-brand-icon"
                    :viewBox="getLoginBrand(item.code, item.name).viewBox"
                    aria-hidden="true"
                    focusable="false"
                >
                    <path
                        v-for="(path, index) in getLoginBrand(item.code, item.name).paths"
                        :key="index"
                        :d="path.d"
                        :fill="path.fill"
                    />
                </svg>
                <span class="tp-label">{{ item.name }}</span>
            </button>
        </div>

        <!-- 扫码弹窗 -->
        <div v-if="visible" class="tp-mask" @click.self="close">
            <div class="tp-dialog">
                <div class="tp-head">
                    <span class="tp-title">{{ current?.name }}{{ $t('登录') }}</span>
                    <button type="button" class="tp-close" @click="close">×</button>
                </div>

                <div class="tp-body">
                    <template v-if="status === 'ready' && scanUrl">
                        <iframe
                            :key="scanUrl"
                            :src="scanUrl"
                            class="tp-iframe"
                            frameborder="0"
                            scrolling="no"
                            :title="`${current?.name}${$t('登录二维码')}`"
                        ></iframe>
                        <div class="tp-hint">{{ $t('请使用') }}{{ current?.name }}{{ $t('扫描二维码') }}</div>
                        <button type="button" class="tp-link" @click="openInWindow">
                            {{ $t('二维码未显示？在新窗口打开') }}
                        </button>
                    </template>

                    <div v-else-if="status === 'loading'" class="tp-state">
                        <div class="tp-spinner" />
                        <p>{{ $t('二维码加载中...') }}</p>
                    </div>

                    <div v-else-if="status === 'scanned'" class="tp-state">
                        <p>{{ $t('已扫码，请在') }}{{ current?.name }}{{ $t('中确认') }}</p>
                    </div>

                    <div v-else-if="status === 'disabled'" class="tp-state tp-state--warn">
                        <p>{{ $t('尚未配置') }}{{ current?.name }}{{ $t('登录凭据') }}</p>
                        <p class="tp-sub">{{ $t('请到管理后台「设置 → 第三方集成」配置后再使用') }}</p>
                    </div>

                    <!-- 未绑定：手机号 + 验证码，完成绑定并登录（无账号自动开通） -->
                    <div v-else-if="status === 'unbound'" class="tp-bind">
                        <p class="tp-bind-tip">
                            {{ $t('该') }}{{ current?.name }}{{ $t('账号尚未绑定，填写手机号即可绑定并登录') }}
                        </p>
                        <div class="tp-field">
                            <input
                                v-model="bindPhone"
                                type="tel"
                                class="tp-input"
                                :placeholder="$t('请输入手机号')"
                            />
                        </div>
                        <div class="tp-field">
                            <input
                                v-model="bindCode"
                                type="text"
                                class="tp-input"
                                :placeholder="$t('请输入验证码')"
                            />
                            <button
                                type="button"
                                class="tp-code-btn"
                                :class="{ disabled: smsCountdown > 0 || sending }"
                                @click="sendCode"
                            >
                                {{ smsCountdown > 0 ? `${smsCountdown}${$t('秒后重发')}` : $t('发送验证码') }}
                            </button>
                        </div>
                        <button
                            type="button"
                            class="tp-submit"
                            :class="{ disabled: binding }"
                            @click="submitBind"
                        >
                            {{ binding ? $t('处理中...') : $t('绑定并登录') }}
                        </button>
                    </div>

                    <div v-else class="tp-state">
                        <p>{{ $t('二维码已失效') }}</p>
                        <button type="button" class="tp-refresh" @click="refresh">{{ $t('点击刷新') }}</button>
                    </div>
                </div>

                <div class="tp-foot">
                    <button type="button" class="tp-refresh" @click="refresh">{{ $t('刷新二维码') }}</button>
                </div>
            </div>
        </div>
    </div>
</template>

<script setup lang="ts">
/**
 * 第三方登录挂载区
 *
 * 渠道清单与状态全部来自后端统一登录接口，本组件不内置任何厂商逻辑：
 * 后端新增渠道后前端自动出现对应入口。
 */
import { ref, onMounted, onUnmounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useUniLogin, type UniLoginChannel } from '~/composables/useUniLogin'
import { useUserStore } from '~/stores/modules/user'
import { Toast } from '~/composables/useToast'
import { getLoginBrand } from '~/utils/loginBrands'

const { loadChannels, fetchQrcode, fetchStatus, cancelLogin, bindLogin, sendSmsCode } = useUniLogin()
const userStore = useUserStore()
const router = useRouter()
const route = useRoute()

const channels = ref<UniLoginChannel[]>([])
const visible = ref(false)
const current = ref<UniLoginChannel | null>(null)
// loading / ready / scanned / confirmed / unbound / expired / disabled
const status = ref('loading')
const scanUrl = ref('')
const qrCodeId = ref('')

const bindPhone = ref('')
const bindCode = ref('')
const sending = ref(false)
const binding = ref(false)
const smsCountdown = ref(0)

let pollTimer: ReturnType<typeof setInterval> | null = null
let countdownTimer: ReturnType<typeof setInterval> | null = null

const stopPolling = () => {
    if (pollTimer) {
        clearInterval(pollTimer)
        pollTimer = null
    }
}

const startPolling = () => {
    stopPolling()
    pollTimer = setInterval(async () => {
        if (!qrCodeId.value || !current.value) return
        try {
            const data = await fetchStatus(current.value.code, qrCodeId.value)
            if (!data) return
            const state = data.status
            if (state === 'confirmed' && data.token) {
                stopPolling()
                await finish(data.token)
            } else if (state === 'scanned') {
                status.value = 'scanned'
            } else if (state === 'unbound') {
                // 停止轮询：等用户填手机号完成绑定，绑定接口会直接返回登录态
                stopPolling()
                status.value = 'unbound'
            } else if (state === 'expired') {
                stopPolling()
                status.value = 'expired'
            }
        } catch (error) {
            // 轮询失败静默重试
        }
    }, 2000)
}

const loadQrcode = async () => {
    if (!current.value) return
    status.value = 'loading'
    try {
        const data = await fetchQrcode(current.value.code)
        if (data?.enabled !== true) {
            status.value = 'disabled'
            return
        }
        scanUrl.value = data.qrCode || ''
        qrCodeId.value = data.qrCodeId || data.qrcodeId || ''
        status.value = scanUrl.value ? 'ready' : 'expired'
        if (status.value === 'ready') startPolling()
    } catch (error) {
        status.value = 'disabled'
    }
}

const open = async (item: UniLoginChannel) => {
    current.value = item
    visible.value = true
    status.value = 'loading'
    scanUrl.value = ''
    qrCodeId.value = ''
    bindPhone.value = ''
    bindCode.value = ''
    await loadQrcode()
}

const refresh = async () => {
    if (qrCodeId.value && current.value) {
        await cancelLogin(current.value.code, qrCodeId.value)
    }
    await loadQrcode()
}

const close = async () => {
    visible.value = false
    stopPolling()
    if (qrCodeId.value && current.value) {
        await cancelLogin(current.value.code, qrCodeId.value)
    }
    qrCodeId.value = ''
    current.value = null
}

const openInWindow = () => {
    if (!scanUrl.value) return
    window.open(scanUrl.value, `unimall_unilogin_${current.value?.code || 'channel'}`, 'width=620,height=620')
    if (!pollTimer) startPolling()
}

const sendCode = async () => {
    if (smsCountdown.value > 0 || sending.value) return
    const phone = (bindPhone.value || '').trim()
    if (!/^1[3-9]\d{9}$/.test(phone)) {
        Toast.error('请输入正确的手机号')
        return
    }
    sending.value = true
    try {
        const response = await sendSmsCode(phone)
        if (response?.code === 1) {
            Toast.success('验证码已发送')
            smsCountdown.value = 60
            countdownTimer = setInterval(() => {
                smsCountdown.value -= 1
                if (smsCountdown.value <= 0 && countdownTimer) {
                    clearInterval(countdownTimer)
                    countdownTimer = null
                }
            }, 1000)
        } else {
            Toast.error(response?.message || '验证码发送失败')
        }
    } catch (error) {
        Toast.error('验证码发送失败，请稍后重试')
    } finally {
        sending.value = false
    }
}

const submitBind = async () => {
    if (binding.value || !current.value || !qrCodeId.value) return
    const phone = (bindPhone.value || '').trim()
    const code = (bindCode.value || '').trim()
    if (!/^1[3-9]\d{9}$/.test(phone)) {
        Toast.error('请输入正确的手机号')
        return
    }
    if (!code) {
        Toast.error('请输入验证码')
        return
    }
    binding.value = true
    try {
        const data = await bindLogin(current.value.code, qrCodeId.value, phone, code)
        if (data?.status === 'confirmed' && data.token) {
            await finish(data.token)
            return
        }
        Toast.error(data?.message || '绑定失败，请稍后重试')
    } catch (error: any) {
        Toast.error(error?.message || '绑定失败，请稍后重试')
    } finally {
        binding.value = false
    }
}

/** 写登录态并跳回目标页（与账号密码登录保持同一套落地逻辑） */
const finish = async (token: string) => {
    await userStore.finishLogin(token)
    visible.value = false
    const redirect = typeof route.query.redirect === 'string' ? decodeURIComponent(route.query.redirect) : ''
    if (redirect && redirect !== route.fullPath) {
        router.replace(redirect)
        return
    }
    router.replace('/')
}

onMounted(async () => {
    channels.value = await loadChannels()
})

onUnmounted(() => {
    stopPolling()
    if (countdownTimer) clearInterval(countdownTimer)
})
</script>

<style lang="scss" scoped>
.third-party-login {
    margin-top: 18px;
}

.tp-divider {
    display: flex;
    align-items: center;
    margin-bottom: 14px;
    color: #9ca3af;
    font-size: 12px;

    &::before,
    &::after {
        content: '';
        flex: 1;
        height: 1px;
        background: #eee;
    }

    span {
        padding: 0 10px;
    }
}

.tp-buttons {
    display: flex;
    align-items: center;
    justify-content: center;
    flex-wrap: wrap;
    gap: 10px;
}

.tp-btn {
    display: inline-flex;
    align-items: center;
    justify-content: center;
    gap: 7px;
    min-width: 76px;
    height: 40px;
    padding: 0 12px;
    border: 1px solid #e5e7eb;
    border-radius: 10px;
    background: #fff;
    color: #374151;
    cursor: pointer;
    font-size: 13px;
    transition: transform 0.15s, border-color 0.15s, box-shadow 0.15s, opacity 0.15s;

    &:hover {
        transform: translateY(-2px);
        border-color: #cbd5e1;
        box-shadow: 0 4px 12px rgba(15, 23, 42, 0.08);
    }

    &:active {
        transform: translateY(0);
    }

    &.tp-btn--off {
        opacity: 0.48;
        box-shadow: none;
    }
}

.tp-brand-icon {
    flex: 0 0 20px;
    width: 20px;
    height: 20px;
    overflow: visible;
}

.tp-label {
    white-space: nowrap;
}

.tp-mask {
    position: fixed;
    inset: 0;
    z-index: 3000;
    display: flex;
    align-items: center;
    justify-content: center;
    background: rgba(0, 0, 0, 0.45);
}

.tp-dialog {
    width: 380px;
    max-width: calc(100vw - 32px);
    background: #fff;
    border-radius: 12px;
    overflow: hidden;
}

.tp-head {
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 12px 16px;
    border-bottom: 1px solid #f0f0f0;
    font-size: 15px;
}

.tp-close {
    border: none;
    background: none;
    font-size: 20px;
    line-height: 1;
    color: #9ca3af;
    cursor: pointer;
}

.tp-body {
    padding: 16px;
    min-height: 220px;
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    gap: 10px;
}

.tp-iframe {
    width: 220px;
    height: 220px;
    border: none;
}

.tp-hint {
    font-size: 13px;
    color: #6b7280;
}

.tp-state {
    text-align: center;
    color: #6b7280;
    font-size: 13px;
}

.tp-state--warn {
    color: #b45309;
}

.tp-sub {
    margin-top: 6px;
    font-size: 12px;
    color: #9ca3af;
}

.tp-spinner {
    width: 24px;
    height: 24px;
    margin: 0 auto 8px;
    border: 2px solid #e5e7eb;
    border-top-color: #409eff;
    border-radius: 50%;
    animation: tp-spin 0.8s linear infinite;
}

@keyframes tp-spin {
    to {
        transform: rotate(360deg);
    }
}

.tp-link,
.tp-refresh {
    border: none;
    background: none;
    color: #2563eb;
    font-size: 12px;
    cursor: pointer;
}

.tp-bind {
    width: 100%;
}

.tp-bind-tip {
    margin-bottom: 12px;
    color: #6b7280;
    font-size: 13px;
    text-align: center;
}

.tp-field {
    display: flex;
    align-items: center;
    gap: 8px;
    margin-bottom: 10px;
}

.tp-input {
    flex: 1;
    height: 38px;
    padding: 0 10px;
    border: 1px solid #e5e7eb;
    border-radius: 8px;
    outline: none;
    font-size: 13px;

    &:focus {
        border-color: #409eff;
    }
}

.tp-code-btn {
    height: 38px;
    padding: 0 10px;
    border: 1px solid #409eff;
    border-radius: 8px;
    background: #fff;
    color: #409eff;
    font-size: 12px;
    white-space: nowrap;
    cursor: pointer;

    &.disabled {
        border-color: #d1d5db;
        color: #9ca3af;
        cursor: not-allowed;
    }
}

.tp-submit {
    width: 100%;
    height: 40px;
    border: none;
    border-radius: 8px;
    background: #409eff;
    color: #fff;
    font-size: 14px;
    cursor: pointer;

    &.disabled {
        background: #a5c8f0;
        cursor: not-allowed;
    }
}

.tp-foot {
    padding: 10px 16px;
    border-top: 1px solid #f0f0f0;
    text-align: right;
}
</style>
