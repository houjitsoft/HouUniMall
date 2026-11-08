<template>
    <div class="offline-payment-container">
        <h1 class="page-title">{{ $t('对公转账') }}</h1>

        <!-- 转账银行信息 -->
        <div class="section">
            <div class="section-title">{{ $t('转账银行信息') }}</div>
            <div class="bank-info-container">
                <div class="bank-info-left">
                    <div class="info-item">
                        <span class="info-label">{{ $t('开户人姓名') }}</span>
                        <span class="info-value">{{ state.bankInfo.bankCardName }}</span>
                    </div>
                    <div class="info-item">
                        <span class="info-label">{{ $t('银行卡号') }}</span>
                        <span class="info-value">{{ state.bankInfo.bankCardId }}</span>
                    </div>
                    <div class="info-item">
                        <span class="info-label">{{ $t('开户银行') }}</span>
                        <span class="info-value">{{ state.bankInfo.bankName }}</span>
                    </div>
                    <div class="info-item">
                        <span class="info-label">{{ $t('银联号') }}</span>
                        <span class="info-value">{{ state.bankInfo.brankUnionpayGood }}</span>
                    </div>
                    <div class="info-item">
                        <span class="info-label">{{ $t('备注') }}</span>
                        <span class="info-value">{{ state.bankInfo.brankNote }}</span>
                    </div>
                </div>
                <div class="bank-info-right">
                    <div class="notice-title">{{ $t('注意事项（转账必读）') }}</div>
                    <div class="notice-content">
                        <p>{{ $t('1.下单请尽快转账，务必保证转账金额与订单金额一致，请勿多转少转，少转和分次转账，否则影响订单对账进度，7天内未对账系统自动取消订单') }}；</p>
                        <p>{{ $t('2.汇款时将订单编码填写至汇款单“用途/备注/摘要”等栏') }}；</p>
                    </div>
                </div>
            </div>
        </div>

        <!-- 订单支付信息 -->
        <div class="section">
            <div class="section-title">{{ $t('订单支付信息') }}</div>
            <div class="order-info-container">
                <div class="info-item">
                    <span class="info-label">{{ $t('支付编号') }}</span>
                    <span class="info-value">{{ paymentNumber }}</span>
                </div>
                <div class="info-item">
                    <span class="info-label">{{ $t('订单金额') }}</span>
                    <span class="info-value">{{ formatPriceWithSymbol(Number(orderAmount)) }}</span>
                </div>
            </div>
        </div>

        <!-- 上传支付凭证 -->
        <div class="section">
            <div class="section-title">{{ $t('上传支付凭证') }}</div>
            <div class="upload-container">
                <div class="upload-option">
                    <input type="radio" id="upload-now" value="1" v-model="state.uploadOption">
                    <label for="upload-now">{{ $t('立即上传') }}</label>
                    <div v-if="state.uploadOption === '1'" class="upload-slots">
                        <UploadImages v-for="(_, index) in 5" :key="index"
                            :model-value="state.uploadedImages[index] ?? ''" :placeholder="$t('上传支付凭证')"
                            accept="image/png,image/jpeg,image/jpg"
                            @update:model-value="(url) => (state.uploadedImages[index] = url)" />
                    </div>
                    <div class="upload-hint">{{ $t('（注：最多只能上传5张图片，只能上传png、jpeg、jpg格式图片）') }}</div>
                </div>
                <div class="upload-option">
                    <input type="radio" id="upload-later" value="2" v-model="state.uploadOption">
                    <label for="upload-later">{{ $t('稍后上传') }}</label>
                </div>
            </div>
        </div>

        <!-- 提交按钮 -->
        <div class="submit-container">
            <button class="submit-button" @click="handleSubmit">{{ $t('提交') }}</button>
        </div>
    </div>
</template>

<script setup lang="ts">
import { onMounted, reactive } from 'vue'
import { formatPriceWithSymbol } from '~/composables/useFormatPrice'
import { queryBankCard } from '~/services/base.service'
import { insertPaymentVoucher } from '~/services/order.service'
import type { BankCardInfo } from '~/types/base'
import UploadImages from '~/components/Upload/UploadImages.vue'
import { ConfirmModal } from '~/composables/useConfirmModal'
import { UCENTER_URL } from '~/utils/externalUrl'
import { usePaymentBackGuard } from '~/composables/usePaymentBackGuard'

const route = useRoute()

// 返回上一页时弹窗确认
usePaymentBackGuard()
const paymentNumber = computed(() => {
    return (route.query.payId as string) || (route.params.payId as string) || ''
})
const orderAmount = computed(() => {
    return (route.query.orderAmount as string) || (route.params.orderAmount as string) || ''
})
const isVirtualOrder = computed(() => String(route.query.virtualGoods ?? '') === '0')
const orderListPath = computed(() =>
    isVirtualOrder.value ? '/order/list?type=2' : '/order/list'
)
const state = reactive({
    // 银行信息（从接口获取）
    bankInfo: {} as BankCardInfo,
    // 上传选项：1-立即上传，2-稍后上传
    uploadOption: '1',
    // 上传的图片URL列表（最多5张）
    uploadedImages: ['', '', '', '', ''] as string[]
})

// 提交按钮处理
const handleSubmit = async () => {
    if (state.uploadOption === '1') {
        // 立即上传：调用支付凭证接口
        const images = state.uploadedImages.filter(Boolean)
        if (!images.length) {
            Toast.warning('请至少上传一张支付凭证')
            return
        }
        if (!paymentNumber.value) {
            Toast.warning('支付编号不存在')
            return
        }
        try {
            const params = {
                orderId: paymentNumber.value,
                orderPaymentVoucherImgs: images,
                status: '1'
            }
            const res = await insertPaymentVoucher(params)
            if (res?.code === 1) {
                tipModal()
            } else {
                Toast.error(res?.message || '提交失败，请重试')
            }
        } catch (error: any) {
            console.error('提交支付凭证失败:', error)
            Toast.error(error?.message || '提交失败，请重试')
        }
    } else {
        // 稍后上传
        tipModal()
    }
}

const tipModal = () => {
    ConfirmModal.show({
        message: '提交成功，立即去订单列表查看？',
        title: '温馨提示',
        keepShoppingText: '在逛逛',
        goNowText: '马上去',
        onKeepShopping: () => navigateTo('/'),
        onGoNow: () => UCENTER_URL(orderListPath.value)
    })
}

const fetchBankCardInfo = async () => {
    try {
        const res = await queryBankCard({})
        if (res?.code === 1 && res?.data) {
            state.bankInfo = res.data
        }
    } catch (error) {
        console.error('获取银行卡信息失败:', error)
    }
}

onMounted(() => {
    fetchBankCardInfo()
})

defineExpose({ state })

</script>

<style lang="scss" scoped>
.offline-payment-container {
    max-width: 1200px;
    margin: 0 auto;
    padding: 20px;
    background-color: #fff;

    .page-title {
        font-size: 24px;
        font-weight: bold;
        color: #333;
        margin-bottom: 30px;
    }

    .section {
        margin-bottom: 30px;

        .section-title {
            font-size: 16px;
            font-weight: bold;
            color: #333;
            border-left: 3px solid #ff4d4f;
            padding-left: 10px;
            margin-bottom: 15px;
        }
    }

    .bank-info-container {
        display: flex;
        border: 1px solid #e8e8e8;
        border-radius: 4px;
        overflow: hidden;

        .bank-info-left {
            flex: 1;
            padding: 20px;
            background-color: #fafafa;

            .info-item {
                display: flex;
                justify-content: space-between;
                margin-bottom: 15px;

                .info-label {
                    font-size: 14px;
                    color: #666;
                }

                .info-value {
                    font-size: 14px;
                    color: #333;
                    font-weight: 500;
                }
            }
        }

        .bank-info-right {
            flex: 1;
            padding: 20px;
            border-left: 1px solid #e8e8e8;

            .notice-title {
                font-size: 16px;
                font-weight: bold;
                color: #333;
                text-align: center;
                margin-bottom: 15px;
            }

            .notice-content {
                font-size: 14px;
                color: #666;
                line-height: 1.5;

                p {
                    margin-bottom: 10px;
                }
            }
        }
    }

    .order-info-container {
        border: 1px solid #e8e8e8;
        border-radius: 4px;
        padding: 20px;

        .info-item {
            display: flex;
            justify-content: space-between;
            margin-bottom: 15px;

            .info-label {
                font-size: 14px;
                color: #666;
            }

            .info-value {
                font-size: 14px;
                color: #333;
                font-weight: 500;
            }
        }
    }

    .upload-container {
        border: 1px solid #e8e8e8;
        border-radius: 4px;
        padding: 20px;

        .upload-option {
            margin-bottom: 20px;

            input[type="radio"] {
                margin-right: 5px;
                margin-left: 5px;
            }

            label {
                font-size: 14px;
                color: #333;
                margin-right: 20px;
            }
        }

        .upload-slots {
            display: flex;
            flex-wrap: wrap;
            gap: 16px;
            margin-top: 10px;

            >* {
                width: 120px;
                aspect-ratio: 1;

                :deep(.upload-area) {
                    aspect-ratio: 1;
                    height: 100%;
                    padding: 8px;
                    box-sizing: border-box;
                }

                :deep(.upload-preview .preview-image) {
                    max-height: 100%;
                    object-fit: cover;
                }
            }
        }

        .upload-hint {
            font-size: 12px;
            color: #999;
            margin-top: 5px;
        }
    }

    .submit-container {
        text-align: center;
        margin-top: 40px;

        .submit-button {
            width: 300px;
            height: 48px;
            background-color: #ff4d4f;
            color: #fff;
            border: none;
            border-radius: 4px;
            font-size: 16px;
            font-weight: bold;
            cursor: pointer;

            &:hover {
                background-color: #ff7875;
            }

            &:active {
                background-color: #d9363e;
            }
        }
    }
}
</style>