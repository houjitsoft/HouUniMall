<template>
    <div class="online-payment">
        <!-- 支付信息卡片 -->
        <div class="payment-card">
            <div class="payment-info">
                <div class="payment-label">{{ $t('支付金额') }}</div>
                <div class="payment-amount">{{ formatPriceWithSymbol(displayPayAmount) }}</div>
                <div v-if="!isPlusPayment && shouldShowPaymentCountdown" class="payment-desc">{{ $t('请您在') }} <span class="countdown">{{
                    formattedCountdown }}</span> {{ $t('内完成支付，否则订单会被自动取消') }}
                </div>
            </div>

            <!-- 订单信息 -->
            <div v-if="!isPlusPayment" class="order-info">
                <div class="order-item">
                    <span class="label">{{ $t('支付单号') }}：</span>
                    <span class="value">{{ paymentNumber }}</span>
                </div>
                <div class="order-item">
                    <span class="label">{{ $t('订单时间') }}：</span>
                    <span class="value">{{ state.paymentAmount?.orderCreateTime }}</span>
                </div>
            </div>

            <!-- 支付方式 -->
            <div class="payment-methods">
                <div class="method-section"
                    v-if="!isPlusPayment && (state.balanceInfo?.isAccount === 1 || state.balanceInfo?.isAccount === 2)">
                    <div class="section-title">{{ $t('账期支付') }}</div>
                    <div class="method-list mall-methods">
                        <div class="method-item" :class="{ active: state.selectedMethod === 24 }"
                            @click="selectMethod(24)">
                            <div class="method-radio">
                                <input type="radio" :checked="state.selectedMethod === 24" @change="selectMethod(24)">
                            </div>
                            <div class="method-logo">
                                <img :src="paymentLogoDisplayUrl" :alt="$t('账期支付')" class="logo-icon">
                            </div>
                            <div class="method-info">
                                <div class="method-name">{{ $t('账期支付') }}</div>
                                <div class="method-desc">({{ state.balanceInfo?.accountIsLimit === 0 ? $t('可用额度不限') : `${$t('可用额度')}:
                                    ${formatPriceWithSymbol(state.balanceInfo?.availableAmount)}` }})</div>
                            </div>
                        </div>
                    </div>
                </div>
                <div class="method-section">
                    <div class="section-title">{{ $t('商城支付') }}</div>
                    <div class="method-list mall-methods">
                        <div class="method-item" :class="{ active: state.selectedMethod === 1 }"
                            @click="selectMethod(1)">
                            <div class="method-radio">
                                <input type="radio" :checked="state.selectedMethod === 1" @change="selectMethod(1)">
                            </div>
                            <div class="method-logo">
                                <img :src="paymentLogoDisplayUrl" :alt="$t('余额支付')" class="logo-icon">
                            </div>
                            <div class="method-info">
                                <div class="method-name">{{ $t('余额支付') }}</div>
                                <div class="method-desc">({{ $t('可用余额') }}: {{
                                    formatPriceWithSymbol(state.balanceInfo?.userMoney) }})</div>
                            </div>
                        </div>

                        <div class="method-item" :class="{ active: state.selectedMethod === 2 }"
                            @click="selectMethod(2)">
                            <div class="method-radio">
                                <input type="radio" :checked="state.selectedMethod === 2" @change="selectMethod(2)">
                            </div>
                            <div class="method-logo">
                                <img :src="paymentLogoDisplayUrl" :alt="$t('卡余额支付')" class="logo-icon">
                            </div>
                            <div class="method-info">
                                <div class="method-name">{{ $t('卡余额支付') }}</div>
                                <div class="method-desc">({{ $t('可用余额') }}: {{
                                    formatPriceWithSymbol(state.balanceInfo?.userRechargeCard) }})</div>
                            </div>
                        </div>
                    </div>
                </div>

                <div class="method-section">
                    <div class="section-title">{{ $t('第三方支付') }}</div>
                    <div class="method-list third-party-methods">
                        <div v-for="item in thirdPartyMethods" :key="item.type" class="method-item"
                            :class="{ active: state.selectedMethod === item.type }" @click="selectMethod(item.type)">
                            <div class="method-radio">
                                <input type="radio" :checked="state.selectedMethod === item.type"
                                    @change="selectMethod(item.type)">
                            </div>
                            <div class="method-logo" :class="item.logoClass" :style="getLogoStyle(item)">
                                <span v-if="item.name" class="method-name">{{ item.name }}</span>
                            </div>
                        </div>
                    </div>
                </div>

                <!-- 确认支付按钮 -->
                <div class="payment-action">
                    <button class="pay-btn" @click="confirmPayment">
                        {{ $t('确认付款') }}
                    </button>
                </div>
            </div>
        </div>

        <!-- 输入密码弹窗 -->
        <PasswordInputModal :visible="state.showPasswordModal" :title="$t('输入支付密码')" :placeholder="$t('请输入支付密码')"
            @close="closeModal" @confirm="confirmPaymentWithPassword" />

        <!-- 设置密码弹窗 -->
        <PasswordSetModal :visible="state.showSetPasswordModal" :title="$t('设置支付密码')" :placeholder="$t('请设置支付密码(6位数字)')"
            @close="closeModal" @confirm="confirmSetPassword" />

        <!-- 二维码弹窗 -->
        <ModalQrcodeModal :visible="state.showQrcodeModal" :title="$t('扫码支付')" :qrcode="state.qrcode"
            @close="closeQrcodeModal" />
    </div>
</template>

<script setup lang="ts">
import { onMounted, reactive, computed, ref, onBeforeUnmount } from 'vue'
import { formatPriceWithSymbol } from '~/composables/useFormatPrice'
import { queryPaymentAmount } from '~/services/order.service'
import { queryNavLogo } from '~/services/base.service'
import { balanceDisplay, checkPassword, evokePayment, paypalPay, plusMemberPay, cashierCreate, cashierStatus, updatePaymentPassword } from '~/services/payment.service'
import { Toast } from '~/composables/useToast'
import type { PaymentPageState } from '~/types/payment'
import { useMallStore, usePaymentStore } from '~/stores'
import { useIsKeySwitch } from '~/composables/useIsKeySwitch'
import { ConfirmModal } from '~/composables/useConfirmModal'
import { UCENTER_URL, IMAGE_URL } from '~/utils/externalUrl'
import { getSafeRedirectUrl } from '~/utils/safeRedirectUrl'
import { usePaymentBackGuard } from '~/composables/usePaymentBackGuard'
import { encryptPassword } from '~/utils/JSEncrypt'
import ModalQrcodeModal from '~/components/Modal/QrcodeModal.vue'
const route = useRoute()

// 返回上一页时弹窗确认
usePaymentBackGuard()
const mallStore = useMallStore()
const paymentStore = usePaymentStore()
const { checkKeySwitch } = useIsKeySwitch()
const paymentNumber = computed(() => {
    if (isPlusPayment.value) return ''
    // 优先从查询参数获取，如果没有则从路径参数获取
    return (route.query.payId as string) || (route.params.payId as string) || ''
})
const plusPaymentContext = computed(() => paymentStore.getPlusPaymentContext)
const isPlusPayment = computed(() => {
    const queryFlag = String(route.query.plusStatus ?? '')
    return queryFlag === '1' || !!plusPaymentContext.value
})

// 否积分支付（如果查询参数 integralPay=1 则视为积分支付，影响支付成功后的提示文案）
const isIntegralPayment = computed(() => String(route.query.integralPay ?? '') === '1');

// 虚拟商品订单（virtualGoods=0 表示虚拟商品）
const isVirtualOrder = computed(() => String(route.query.virtualGoods ?? '') === '0')
const orderListPath = computed(() =>
    isVirtualOrder.value ? '/order/list?type=2' : '/order/list'
)

const displayPayAmount = computed(() => {
    if (isPlusPayment.value) return plusPaymentContext.value?.payPrice ?? 0
    return state.paymentAmount?.orderPaymentAmount ?? 0
})

// 支付 logo URL（本地 ref，请求完成后赋值以保证客户端必定更新视图）
const paymentLogoUrl = ref('')

// 默认支付 logo 文件名（无配置时使用，与账期支付图标一致）
const DEFAULT_PAYMENT_LOGO = 'zqzf.png'

// 账期/余额/购物卡 logo 显示 URL（无配置时用默认图；有值时拼接完整 URL 并加时间戳防缓存）
const paymentLogoDisplayUrl = computed(() => {
    const url = (paymentLogoUrl.value || mallStore.getPaymentLogoUrl || '').trim()
    const finalUrl = url || DEFAULT_PAYMENT_LOGO
    const fullUrl = finalUrl.startsWith('http') ? finalUrl : IMAGE_URL(finalUrl)
    const sep = fullUrl.includes('?') ? '&' : '?'
    return fullUrl + sep + 't=' + Date.now()
})

const state = reactive<PaymentPageState>({
    // 支付金额信息
    paymentAmount: null,
    // 余额信息
    balanceInfo: null,
    // 选中的支付方式（支付类型码：1余额 2卡 24账期 5支付宝 11微信 14PayPal 15Stripe 22通联）
    selectedMethod: 0,
    // 倒计时（秒）
    countdown: 0,
    // 密码相关
    hasPassword: false,
    // 弹窗显示状态
    showPasswordModal: false,
    showSetPasswordModal: false,
    showQrcodeModal: false,
    qrcode: ''
})

// tongLian_switch == 1 时显示通联支付
const showTongLianPay = ref(false)
// paypal_switch == 1 时显示 PayPal
const showPaypalPay = ref(false)
// stripe_switch == 1 时显示 Stripe
const showStripePay = ref(false)

// 第三方支付 logo 样式（长方形比例 5:2；微信放大 5% 为 126x50，其他缩小 10% 为 92x37）
const getLogoStyle = (item: { image?: string; width?: number; height?: number; logoClass?: string }) => {
    const w = item.width ?? 126
    const h = item.height ?? 48
    const style: Record<string, string> = item.image ? { backgroundImage: `url(${IMAGE_URL(item.image)})` } : {}
    const withName = item.logoClass?.includes('logo-bg--with-name')
    style.backgroundSize = `${w}px ${h}px`
    if (!withName) style.width = `${w}px`
    style.height = `${h}px`
    if (withName) style.paddingLeft = `${w + 8}px`
    return style
}

// 第三方支付方式配置（长方形 5:2；微信 126x50，其他 92x37）
const thirdPartyMethods = computed(() => {
    const list = [
        { type: 5, image: 'pcImg/zhifubaozhifu.png', name: '', logoClass: 'logo-bg', width: 92, height: 37 },
        { type: 11, image: 'pcImg/weixinzhifu.svg', name: '', logoClass: 'logo-bg', width: 126, height: 50 }
    ]
    if (!isPlusPayment.value && showPaypalPay.value) {
        list.push({ type: 14, image: 'pcImg/pp_fc_hl.svg', name: '', logoClass: 'logo-bg', width: 92, height: 37 })
    }
    if (!isPlusPayment.value && showStripePay.value) {
        list.push({ type: 15, image: 'pcImg/stripe.png', name: '', logoClass: 'logo-bg', width: 92, height: 37 })
    }
    if (showTongLianPay.value && !isPlusPayment.value) {
        list.push({ type: 22, image: 'pcImg/tonglianzhifu.png', name: '通联支付', logoClass: 'logo-bg logo-bg--with-name', width: 92, height: 37 })
    }
    return list
})

// 轮询支付状态定时器
let payStatusPollTimer: ReturnType<typeof setInterval> | null = null
const PAY_STATUS_POLL_INTERVAL = 2000
let activeCashierNo = ''


const PAYMENT_PASSWORD_RESET_PATH = '/member/changephonestep4'

const isPaymentPasswordError = (message?: string) => {
    if (!message) return false
    return message.includes('密码错误') || message.includes('密码重置')
}

const showPaymentPasswordErrorModal = async (message?: string) => {
    await ConfirmModal.confirm({
        title: '温馨提示',
        message: message || '密码错误，可以选择密码重置!',
        confirmText: '去重置支付密码',
        cancelText: '取消',
        onConfirm: () => {
            state.showPasswordModal = false
            UCENTER_URL(PAYMENT_PASSWORD_RESET_PATH)
        }
    })
}

const showPaySuccessModal = () => {
    if (isPlusPayment.value) {
        paymentStore.clearPlusPaymentContext()
        ConfirmModal.show({
            message: '支付成功，立即去plus会员中心查看？',
            title: '温馨提示',
            keepShoppingText: '再逛逛',
            goNowText: '马上去',
            onKeepShopping: () => navigateTo('/'),
            onGoNow: () => navigateTo('/member/plusActivate')
        })
        return
    }
    ConfirmModal.show({
        message: isIntegralPayment.value ? '提交成功，立即去我的积分查看？' : '提交成功，立即去订单列表查看？',
        title: '温馨提示',
        keepShoppingText: '再逛逛',
        goNowText: '马上去',
        onKeepShopping: () => navigateTo('/'),
        onGoNow: () => {
            if (isIntegralPayment.value) {
                UCENTER_URL('/property/integral')
                return
            }
            UCENTER_URL(orderListPath.value)
        }
    })
}

// 格式化倒计时
const formattedCountdown = computed(() => {
    const hours = Math.floor(state.countdown / 3600)
    const minutes = Math.floor((state.countdown % 3600) / 60)
    const seconds = state.countdown % 60
    const formatTime = (time: number) => String(time).padStart(2, '0')
    return `${formatTime(hours)} h ${formatTime(minutes)} m ${formatTime(seconds)} s `
})

// 只有配置了自动取消时间，才显示倒计时与执行超时提示
const shouldShowPaymentCountdown = computed(() => {
    return Boolean(state.balanceInfo?.seckillCancelTime || state.paymentAmount?.orderCancelTime)
})

// 选择支付方式
const selectMethod = (type: number) => {
    state.selectedMethod = type
}

// PayPal 支付
const handlePaypalPayment = async () => {
    const money = state.paymentAmount?.orderPaymentAmount ?? 0
    const orderId = paymentNumber.value
    const url = typeof window !== 'undefined'
        ? `${window.location.origin}/ucenter/#${orderListPath.value}`
        : ''
    try {
        const res = await paypalPay({
            currencyCode: '￥',
            money,
            orderId,
            url
        })
        if (res.code !== 1) {
            Toast.error(res.message || 'PayPal 支付失败')
        }
    } catch (error) {
        console.error('PayPal 支付失败:', error)
        Toast.error(error instanceof Error ? error.message : 'PayPal 支付失败，请稍后重试')
    }
}

// 支付宝支付：统一收银台 /web/pay/cashier/create（payMethod=ALIPAY, terminal=PC）
const handleAlipayPayment = async () => {
    const orderId = paymentNumber.value
    const plus = plusPaymentContext.value
    const amount = Number(plus?.payPrice ?? state.paymentAmount?.orderPaymentAmount ?? 0)
    if (!amount || amount <= 0) {
        Toast.error('支付金额无效')
        return
    }
    try {
        const returnUrl = typeof window !== 'undefined'
            ? `${window.location.origin}/payment/result`
            : undefined
        const res = await cashierCreate({
            terminal: 'PC',
            payMethod: 'ALIPAY',
            amount,
            mallOrderNo: orderId || undefined,
            mchOrderNo: orderId || undefined,
            requestNo: orderId ? `pc-ali-${orderId}` : undefined,
            subject: plus ? 'PLUS会员开通' : '商城订单支付',
            currency: 'CNY',
            returnUrl
        })
        if (res.code !== 1 || !res.data) {
            Toast.error(res.message || '支付宝下单失败（渠道密钥未配置时属正常）')
            return
        }
        activeCashierNo = res.data.cashierNo || ''
        const payData = (res.data.payData || {}) as Record<string, any>
        const formHtml = payData.formHtml as string | undefined
        const payUrl = (payData.payUrl || payData.url || payData.codeUrl) as string | undefined
        if (formHtml && typeof document !== 'undefined') {
            const w = window.open('', '_blank')
            if (w) {
                w.document.open()
                w.document.write(formHtml)
                w.document.close()
            } else {
                const div = document.createElement('div')
                div.style.display = 'none'
                div.innerHTML = formHtml
                document.body.appendChild(div)
                const form = div.querySelector('form')
                if (form) (form as HTMLFormElement).submit()
            }
            if (activeCashierNo) startPayStatusPoll(activeCashierNo)
            return
        }
        if (payUrl) {
            if (showTongLianPay.value || !String(payUrl).startsWith('http')) {
                state.qrcode = String(payUrl)
                state.showQrcodeModal = true
                if (activeCashierNo) startPayStatusPoll(activeCashierNo)
            } else {
                const safe = getSafeRedirectUrl(String(payUrl))
                if (safe && typeof window !== 'undefined') {
                    window.location.href = safe
                } else {
                    state.qrcode = String(payUrl)
                    state.showQrcodeModal = true
                    if (activeCashierNo) startPayStatusPoll(activeCashierNo)
                }
            }
            return
        }
        Toast.error('未返回支付宝调起参数，请检查渠道配置')
    } catch (error) {
        console.error('支付宝支付失败:', error)
        Toast.error(error instanceof Error ? error.message : '支付宝支付失败，请稍后重试')
    }
}


// 微信支付：统一收银台 /web/pay/cashier/create（payMethod=WECHAT, terminal=PC）
const handleWechatPayment = async () => {
    const orderId = paymentNumber.value
    const plus = plusPaymentContext.value
    const amount = Number(plus?.payPrice ?? state.paymentAmount?.orderPaymentAmount ?? 0)
    if (!amount || amount <= 0) {
        Toast.error('支付金额无效')
        return
    }
    try {
        const res = await cashierCreate({
            terminal: 'PC',
            payMethod: 'WECHAT',
            amount,
            mallOrderNo: orderId || undefined,
            mchOrderNo: orderId || undefined,
            requestNo: orderId ? `pc-wx-${orderId}` : undefined,
            subject: plus ? 'PLUS会员开通' : '商城订单支付',
            currency: 'CNY'
        })
        if (res.code !== 1 || !res.data) {
            Toast.error(res.message || '微信下单失败（渠道密钥未配置时属正常）')
            return
        }
        activeCashierNo = res.data.cashierNo || ''
        const payData = (res.data.payData || {}) as Record<string, any>
        const codeUrl = (payData.codeUrl || payData.payUrl || payData.url) as string | undefined
        if (!codeUrl) {
            Toast.error(payData.tips || payData.message || '未返回微信二维码，请检查渠道配置')
            return
        }
        state.qrcode = String(codeUrl)
        state.showQrcodeModal = true
        if (activeCashierNo) startPayStatusPoll(activeCashierNo)
    } catch (error) {
        console.error('微信支付失败:', error)
        Toast.error(error instanceof Error ? error.message : '微信支付失败，请稍后重试')
    }
}


const startPayStatusPoll = (cashierNo: string) => {
    if (!cashierNo) return
    stopPayStatusPoll()
    activeCashierNo = cashierNo
    payStatusPollTimer = setInterval(async () => {
        try {
            const res = await cashierStatus({ cashierNo: activeCashierNo })
            if (res.code === 1 && res.data) {
                const st = res.data.status
                const code = res.data.statusCode
                if (st === 2 || code === 'SUCCESS') {
                    stopPayStatusPoll()
                    state.showQrcodeModal = false
                    showPaySuccessModal()
                } else if (st === 3 || st === 4 || code === 'FAIL' || code === 'CLOSED') {
                    stopPayStatusPoll()
                    state.showQrcodeModal = false
                    Toast.error(res.data.errMsg || res.data.statusDesc || '支付未完成')
                }
            }
        } catch {
            // 轮询失败忽略，继续下一次
        }
    }, PAY_STATUS_POLL_INTERVAL)
}


const stopPayStatusPoll = () => {
    if (payStatusPollTimer) {
        clearInterval(payStatusPollTimer)
        payStatusPollTimer = null
    }
}

const closeQrcodeModal = () => {
    stopPayStatusPoll()
    activeCashierNo = ''
    state.showQrcodeModal = false
    state.qrcode = ''
}

// Stripe 支付：统一收银台 payMethod=STRIPE
const handleStripePayment = async () => {
    const orderId = paymentNumber.value
    const amount = Number(state.paymentAmount?.orderPaymentAmount ?? 0)
    if (!amount || amount <= 0) {
        Toast.error('支付金额无效')
        return
    }
    try {
        const returnUrl = typeof window !== 'undefined'
            ? `${window.location.origin}/payment/result`
            : undefined
        const res = await cashierCreate({
            terminal: 'PC',
            payMethod: 'STRIPE',
            amount,
            mallOrderNo: orderId || undefined,
            mchOrderNo: orderId || undefined,
            requestNo: orderId ? `pc-stripe-${orderId}` : undefined,
            subject: '商城订单支付',
            currency: 'CNY',
            returnUrl
        })
        if (res.code !== 1 || !res.data) {
            Toast.error(res.message || 'Stripe 下单失败（密钥未配置时属正常）')
            return
        }
        const payData = (res.data.payData || {}) as Record<string, any>
        const url = (payData.checkoutUrl || payData.payUrl || payData.url) as string | undefined
        if (url && typeof window !== 'undefined') {
            const safe = getSafeRedirectUrl(String(url))
            if (safe) {
                window.location.href = safe
                return
            }
        }
        Toast.error(payData.tips || '未返回 Stripe 跳转地址，请检查渠道配置')
    } catch (error) {
        console.error('Stripe 支付失败:', error)
        Toast.error(error instanceof Error ? error.message : 'Stripe 支付失败，请稍后重试')
    }
}


// 通联支付：使用 evokePayment，无需密码，根据 data 跳转
const handleTongLianPayment = async () => {
    const orderId = paymentNumber.value
    try {
        const res = await evokePayment({
            passwd: '',
            orderId,
            type: 23
        })
        if (res.code === 1) {
            const redirectUrl = typeof res.data === 'string' ? res.data : (res.data as { url?: string })?.url
            if (redirectUrl) {
                const safe = getSafeRedirectUrl(redirectUrl)
                if (safe && typeof window !== 'undefined') {
                    window.location.href = safe
                } else {
                    Toast.error('支付跳转地址无效，请重试或联系客服')
                }
            } else {
                Toast.success('支付成功')
            }
        } else {
            Toast.error(res.message || '通联支付失败')
        }
    } catch (error) {
        console.error('通联支付失败:', error)
        Toast.error(error instanceof Error ? error.message : '通联支付失败，请稍后重试')
    }
}

// 确认支付
const confirmPayment = async () => {
    if (!state.selectedMethod || state.selectedMethod <= 0) {
        Toast.warning('请选择支付方式')
        return
    }

    const payAmount = displayPayAmount.value
    // 账期、商城支付前判断可用金额是否大于等于需支付金额（accountIsLimit === 0 表示额度不限，不校验）
    if (state.selectedMethod === 24) {
        if (state.balanceInfo?.accountIsLimit !== 0) {
            const available = state.balanceInfo?.availableAmount ?? 0
            if (available < payAmount) {
                Toast.warning(`可用额度不足，当前可用 ${formatPriceWithSymbol(available)}，需支付 ${formatPriceWithSymbol(payAmount)}`)
                return
            }
        }
    } else if (state.selectedMethod === 1) {
        const available = state.balanceInfo?.userMoney ?? 0
        if (available < payAmount) {
            Toast.warning(`可用余额不足，当前可用 ${formatPriceWithSymbol(available)}，需支付 ${formatPriceWithSymbol(payAmount)}`)
            return
        }
    } else if (state.selectedMethod === 2) {
        const available = state.balanceInfo?.userRechargeCard ?? 0
        if (available < payAmount) {
            Toast.warning(`卡余额不足，当前可用 ${formatPriceWithSymbol(available)}，需支付 ${formatPriceWithSymbol(payAmount)}`)
            return
        }
    }

    // 支付宝支付
    if (state.selectedMethod === 5) {
        await handleAlipayPayment()
        return
    }

    // 微信支付
    if (state.selectedMethod === 11) {
        await handleWechatPayment()
        return
    }

    // PayPal 支付
    if (state.selectedMethod === 14) {
        await handlePaypalPayment()
        return
    }

    // Stripe 支付
    if (state.selectedMethod === 15) {
        await handleStripePayment()
        return
    }

    // 通联支付：使用 evokePayment，无需密码，根据 data 跳转
    if (state.selectedMethod === 22) {
        await handleTongLianPayment()
        return
    }

    // 判断是否设置了密码
    if (state.hasPassword) {
        // 已设置密码，显示输入密码弹窗
        state.showPasswordModal = true
    } else {
        // 未设置密码，显示设置密码弹窗
        state.showSetPasswordModal = true
    }
}

// 确认支付（输入密码后）
const confirmPaymentWithPassword = async (password: string) => {
    if (!paymentNumber.value && !isPlusPayment.value) {
        Toast.error('订单号不存在')
        return
    }

    if (!state.selectedMethod || state.selectedMethod <= 0) {
        Toast.error('请选择支付方式')
        return
    }

    try {
        const encryptedPwd = encryptPassword(password)
        let response
        if (isPlusPayment.value) {
            const plus = plusPaymentContext.value
            if (!plus) {
                Toast.error('PLUS 支付信息不存在，请返回重试')
                return
            }
            response = await plusMemberPay({
                passwd: encryptedPwd,
                payPrice: plus.payPrice,
                thaliEquity: plus.thaliEquity,
                thaliType: plus.thaliType,
                type: state.selectedMethod === 2 ? 2 : 1
            })
        } else {
            response = await evokePayment({
                passwd: encryptedPwd,
                orderIdList: paymentNumber.value,
                type: state.selectedMethod
            })
        }

        if (response.code === 1) {
            Toast.success('支付成功')
            state.showPasswordModal = false
            // 可以在这里添加支付成功后的跳转逻辑
            showPaySuccessModal()
        } else {
            const message = response.message || '支付失败'
            if (isPaymentPasswordError(message)) {
                await showPaymentPasswordErrorModal(message)
            } else {
                Toast.error(message)
            }
        }
    } catch (error) {
        console.error('支付失败:', error)
        const errorMessage = error instanceof Error ? error.message : '支付失败，请稍后重试'
        if (isPaymentPasswordError(errorMessage)) {
            await showPaymentPasswordErrorModal(errorMessage)
        } else {
            Toast.error(errorMessage)
        }
    }
}

// 确认设置密码
const confirmSetPassword = async (password: string) => {
    try {
        const res = await updatePaymentPassword({ passwd: encryptPassword(password) })
        if (res.code === 1) {
            state.hasPassword = true
            state.showSetPasswordModal = false
            state.showPasswordModal = true
            return
        }
        Toast.error(res.message || '设置支付密码失败')
    } catch (error) {
        console.error('设置支付密码失败:', error)
        Toast.error(error instanceof Error ? error.message : '设置支付密码失败，请稍后重试')
    }
}

// 关闭弹窗
const closeModal = () => {
    state.showPasswordModal = false
    state.showSetPasswordModal = false
}

// 初始化数据
const initData = async () => {
    if (!paymentNumber.value && !isPlusPayment.value) {
        console.error('订单号不存在')
        return
    }

    try {
        if (isPlusPayment.value) {
            const plus = plusPaymentContext.value
            if (!plus) {
                Toast.error('PLUS 支付信息已失效，请返回重新选择套餐')
                navigateTo('/member/plusRecharge')
                return
            }
            state.paymentAmount = {
                orderCreateTime: '',
                orderCancelTime: '',
                orderPaymentAmount: plus.payPrice
            }
            const [balanceRes, passwordRes] = await Promise.all([
                balanceDisplay({ orderId: '' }),
                checkPassword()
            ])
            if (balanceRes.code === 1 && balanceRes.data) state.balanceInfo = balanceRes.data
            if (passwordRes.code === 1) state.hasPassword = passwordRes.data === 1
            state.countdown = 0
            return
        }
        const [paymentRes, balanceRes, passwordRes] = await Promise.all([
            queryPaymentAmount({ paymentNumber: paymentNumber.value }),
            balanceDisplay({ orderId: paymentNumber.value }),
            checkPassword()
        ])

        if (paymentRes.code === 1 && paymentRes.data) {
            state.paymentAmount = paymentRes.data
        }

        if (balanceRes.code === 1 && balanceRes.data) {
            state.balanceInfo = balanceRes.data
            if (balanceRes.data.seckillCancelTime) {
                const cancelTime = new Date(balanceRes.data.seckillCancelTime).getTime()
                state.countdown = Math.max(0, Math.floor((cancelTime - Date.now()) / 1000))
            }
        }

        if (state.countdown <= 0 && state.paymentAmount?.orderCancelTime) {
            const cancelTime = new Date(state.paymentAmount.orderCancelTime).getTime()
            state.countdown = Math.max(0, Math.floor((cancelTime - Date.now()) / 1000))
        }

        if (passwordRes.code === 1) {
            state.hasPassword = passwordRes.data === 1
        }
    } catch (error) {
        console.error('初始化数据失败:', error)
    }
}

// 倒计时定时器
let countdownTimer: ReturnType<typeof setInterval> | null = null

const COUNTDOWN_INTERVAL = 1000 // 倒计时更新间隔（毫秒）

// 获取商城/账期支付 logo 配置（支付页独立请求，拿到后写入 store 并同步到本地 ref 保证视图更新）
const fetchPaymentLogoConfig = async () => {
    const fromStore = mallStore.getPaymentLogoUrl
    if (fromStore) {
        paymentLogoUrl.value = fromStore
        return
    }
    try {
        const response = await queryNavLogo({ configKey: 'logo_copywriting_ico_pay', configType: 'logo_copywriting' })
        if (response.code !== 1 || !response.data) return
        const raw = response.data
        const newConfigs = Array.isArray(raw) ? raw : (raw && typeof raw === 'object' && raw.configKey ? [raw] : [])
        if (newConfigs.length === 0) return
        const existing = mallStore.getNavLogoConfigs || []
        const rest = existing.filter((c: { configKey?: string }) => c.configKey !== 'logo_copywriting_ico_pay')
        mallStore.setNavLogoConfigs([...rest, ...newConfigs])
        const url = mallStore.getPaymentLogoUrl || ''
        if (url) paymentLogoUrl.value = url
    } catch (e) {
        console.error('获取支付 logo 配置失败:', e)
    }
}

onMounted(async () => {
    const [tongLianValue, paypalValue, stripeValue] = await Promise.all([
        checkKeySwitch('tongLian_switch'),
        checkKeySwitch('paypal_switch'),
        checkKeySwitch('stripe_switch')
    ])
    showTongLianPay.value = tongLianValue === '1'
    showPaypalPay.value = paypalValue === '1'
    showStripePay.value = stripeValue === '1'

    await fetchPaymentLogoConfig()
    await initData()
    if (!isPlusPayment.value && shouldShowPaymentCountdown.value) {
        countdownTimer = setInterval(() => {
            if (state.countdown > 0) {
                state.countdown--
            } else {
                if (countdownTimer) {
                    clearInterval(countdownTimer)
                    countdownTimer = null
                }
                Toast.error('支付超时，订单已取消')
            }
        }, COUNTDOWN_INTERVAL)
    }
})

// 组件卸载时清理定时器
onBeforeUnmount(() => {
    if (countdownTimer) {
        clearInterval(countdownTimer)
        countdownTimer = null
    }
    stopPayStatusPoll()
})

defineExpose({ state })
</script>

<style lang="scss" scoped>
.online-payment {
    padding: 20px;
    background-color: #f5f5f5;
    min-height: 100vh;
}

.payment-card {
    background-color: #fff;
    border-radius: 8px;
    box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
    overflow: hidden;
}

.payment-info {
    padding: 30px;
    background-color: #fef5f5;
    text-align: center;

    .payment-label {
        font-size: 16px;
        color: #666;
        margin-bottom: 10px;
    }

    .payment-amount {
        font-size: 36px;
        color: var(--color);
        font-weight: 600;
        margin-bottom: 20px;
    }

    .payment-desc {
        font-size: 14px;
        color: #666;

        .countdown {
            color: #1890ff;
            font-weight: 600;
        }
    }
}

.order-info {
    padding: 15px 30px;
    background-color: #f0f0f0;
    border-bottom: 1px solid #e0e0e0;

    .order-item {
        display: flex;
        // justify-content: space-between;
        margin-bottom: 8px;

        .label {
            font-size: 14px;
            color: #666;
        }

        .value {
            font-size: 14px;
            color: #333;
        }
    }
}

.payment-methods {
    padding: 30px;
}

.method-section {
    margin-bottom: 30px;

    .section-title {
        font-size: 16px;
        color: #333;
        font-weight: 600;
        background-color: #fef0f0;
        padding: 8px 15px;
        border-radius: 4px;
        margin-bottom: 15px;
        display: inline-block;
    }

    /* 商城支付每行展示3个，第三方支付每行展示5个 */
    .method-list.mall-methods .method-item {
        flex: 0 0 calc(33.333% - 8px);
        /* 每行3个，减去间距 */
    }

    .method-list.third-party-methods .method-item {
        flex: 0 0 calc(20% - 8px);
        /* 每行5个，减去间距 */
    }

    .method-list {
        display: flex;
        flex-wrap: wrap;
        gap: 10px;

        .method-item {
            display: flex;
            align-items: center;
            padding: 15px;
            border: 1px solid #e0e0e0;
            border-radius: 4px;
            cursor: pointer;
            transition: all 0.3s;
            box-sizing: border-box;

            &:hover {
                border-color: var(--color);
            }

            &.active {
                border-color: var(--color);
                background-color: #fff7f7;
            }

            .method-radio {
                margin-right: 15px;

                input[type="radio"] {
                    width: 18px;
                    height: 18px;
                    cursor: pointer;
                }
            }

            .method-logo {
                margin-right: 15px;

                .logo-icon {
                    width: 40px;
                    height: 40px;
                    border-radius: 4px;
                    object-fit: contain;
                }

                &.logo-bg {
                    flex: 1;
                    margin-right: 0;
                    display: flex;
                    align-items: center;
                    justify-content: center;
                    gap: 8px;
                    flex-shrink: 0;
                    background-repeat: no-repeat;
                    background-position: center;

                    .method-name {
                        font-size: 14px;
                        color: #333;
                        font-weight: 500;
                    }

                    &.logo-bg--with-name {
                        width: auto;
                        justify-content: flex-start;
                        background-position: left center;
                    }
                }
            }

            .method-info {
                flex: 1;

                .method-name {
                    font-size: 16px;
                    color: #333;
                    margin-bottom: 5px;
                }

                .method-desc {
                    font-size: 12px;
                    color: #999;
                }
            }
        }
    }
}

.payment-action {
    padding: 30px;
    text-align: center;

    .pay-btn {
        width: 300px;
        height: 48px;
        background-color: var(--color);
        color: #fff;
        border: none;
        border-radius: 4px;
        font-size: 16px;
        font-weight: 600;
        cursor: pointer;
        transition: all 0.3s;

        &:hover {
            background-color: #ff7875;
        }

        &:disabled {
            background-color: #e0e0e0;
            cursor: not-allowed;
        }
    }
}
</style>