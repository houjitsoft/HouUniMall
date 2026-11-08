<template>
    <PopupModal v-model="visible" title="选择优惠券" width="480px" content-class="coupon-modal"
        overlay-class="no-flicker-overlay">
        <div v-if="loading" class="coupon-loading">{{ $t('加载中...') }}</div>
        <div v-else class="coupon-list">
            <div class="coupon-item" :class="{ active: selectedVoucherId === null }" @click="selectCoupon(null)">
                <span class="coupon-radio" :class="{ checked: selectedVoucherId === null }">
                    <span v-if="selectedVoucherId === null" class="radio-dot"></span>
                </span>
                <span class="coupon-text">{{ $t('不使用优惠券') }}</span>
            </div>
            <div v-for="item in flatCouponList" :key="item.voucherId" class="coupon-item"
                :class="{ active: selectedVoucherId === item.voucherId }" @click="selectCoupon(item)">
                <span class="coupon-radio" :class="{ checked: selectedVoucherId === item.voucherId }">
                    <span v-if="selectedVoucherId === item.voucherId" class="radio-dot"></span>
                </span>
                <span class="coupon-desc">{{ $t(getCouponDesc(item) || '') }}</span>
                <span class="coupon-price">{{ formatPriceWithSymbolSafe(item.voucherPrice) }}</span>
                <span class="coupon-limit">{{ $t('满') }} {{ formatPriceWithSymbolSafe(item.voucherLimit) }} {{ $t('可用') }}</span>
            </div>
        </div>
        <EmptyState v-if="!loading && flatCouponList.length === 0" :text="$t('暂无可用的优惠券')" :icon-size="48"
            :compact="true" />
        <template #footer>
            <button type="button" class="btn-confirm" @click="handleConfirm">{{ $t('确定') }}</button>
        </template>
    </PopupModal>
</template>

<script setup lang="ts">
import { ref, watch, computed } from 'vue'
import EmptyState from '~/components/Empty/EmptyState.vue'
import PopupModal from '~/components/Modal/PopupModal.vue'
import { selectAvailableVcBase } from '~/services/activity.service'
import { formatPriceWithSymbolSafe } from '~/composables/useFormatPrice'
import type { VoucherBaseVO } from '~/types/business'

const props = withDefaults(defineProps<{
    modelValue: boolean
    shopId: number | string
    goodsInfo: Array<{ goodsId: string | number; goodsPrice: string | number; goodsNum: string | number }>
    initialVoucherId?: string | null
}>(), {
    initialVoucherId: null
})

const emit = defineEmits<{
    'update:modelValue': [value: boolean]
    confirm: [payload: { voucherId: string | null; voucherPrice?: number; voucherTitle?: string }]
}>()

const visible = computed({
    get: () => props.modelValue,
    set: (v) => emit('update:modelValue', v)
})

const loading = ref(false)
const couponList = ref<VoucherBaseVO[]>([])
const selectedVoucherId = ref<string | null>(null)
const selectedVoucher = ref<VoucherBaseVO | null>(null)

function flattenVouchers(items: VoucherBaseVO[]): VoucherBaseVO[] {
    const result: VoucherBaseVO[] = []
    const push = (list: VoucherBaseVO[] | undefined) => {
        if (!Array.isArray(list)) return
        list.forEach(item => {
            if (item.houVoucherBaseVOList?.length) {
                push(item.houVoucherBaseVOList)
            } else if (item.voucherId) {
                result.push(item)
            }
        })
    }
    push(items)
    return result
}

// 扁平化优惠券列表（处理 houVoucherBaseVOList 嵌套）
const flatCouponList = computed(() => flattenVouchers(couponList.value))


const getCouponDesc = (item: VoucherBaseVO) => {
    return item.voucherTitle || `满${item.voucherLimit || 0}减${item.voucherPrice || 0}元`
}

const selectCoupon = (item: VoucherBaseVO | null) => {
    selectedVoucherId.value = item?.voucherId ?? null
    selectedVoucher.value = item ?? null
}

const handleConfirm = () => {
    const item = selectedVoucher.value
    emit('confirm', {
        voucherId: selectedVoucherId.value,
        voucherPrice: item?.voucherPrice,
        voucherTitle: item?.voucherTitle
    })
    visible.value = false
}

// 打开时请求接口
watch(() => props.modelValue, async (val) => {
    if (val) {
        selectedVoucherId.value = props.initialVoucherId ?? null
        selectedVoucher.value = null
        loading.value = true
        couponList.value = []
        try {
            const res = await selectAvailableVcBase({
                platForm: 1,
                shopId: props.shopId,
                goodsInfo: props.goodsInfo
            })
            if (res?.code === 1 && Array.isArray(res?.data)) {
                couponList.value = res.data
                // 若初始有选中，匹配 selectedVoucher（扁平化后查找）
                if (props.initialVoucherId) {
                    const flat = flattenVouchers(res.data)
                    const found = flat.find(v => v.voucherId === props.initialVoucherId)
                    if (found) selectedVoucher.value = found
                }
            }
        } catch {
            couponList.value = []
        } finally {
            loading.value = false
        }
    }
})
</script>

<style scoped lang="scss">
:deep(.coupon-modal .modal-body) {
    max-height: 60vh;
    overflow-y: auto;
    padding: 16px;
}

:deep(.no-flicker-overlay) {
    backdrop-filter: none;
    background-color: rgba(0, 0, 0, 0.5);
}

:deep(.no-flicker-overlay.modal-fade-enter-from),
:deep(.no-flicker-overlay.modal-fade-leave-to) {
    opacity: 1;
}

.coupon-loading {
    text-align: center;
    padding: 40px 0;
    color: #999;
}

.coupon-list {
    display: flex;
    flex-direction: column;
    gap: 0;
}

.coupon-item {
    display: flex;
    align-items: center;
    padding: 14px 16px;
    border: 1px solid transparent;
    border-radius: 6px;
    margin-bottom: 8px;
    cursor: pointer;
    transition: all 0.2s ease;

    &:last-child {
        margin-bottom: 0;
    }

    &:hover {
        background: #fafafa;
    }

    &.active {
        background: rgba(82, 196, 26, 0.08);
        border-color: var(--color, #52c41a);
        box-shadow: 0 0 0 1px rgba(82, 196, 26, 0.2);

        .coupon-text {
            color: #333;
            font-weight: 500;
        }
    }

    .coupon-radio {
        width: 18px;
        height: 18px;
        border: 2px solid #d9d9d9;
        border-radius: 50%;
        margin-right: 12px;
        flex-shrink: 0;
        display: flex;
        align-items: center;
        justify-content: center;
        transition: all 0.2s ease;

        &.checked {
            border-color: var(--color, #52c41a);
            background: #fff;

            .radio-dot {
                width: 8px;
                height: 8px;
                border-radius: 50%;
                background: var(--color, #52c41a);
            }
        }
    }

    .coupon-text {
        font-size: 14px;
        color: #666;
    }

    .coupon-desc {
        font-size: 14px;
        color: #333;
        flex: 1;
    }

    .coupon-price {
        font-size: 14px;
        color: var(--color);
        font-weight: 500;
        margin: 0 8px;
    }

    .coupon-limit {
        font-size: 14px;
        color: var(--color);
    }
}

:deep(.coupon-modal .modal-footer) {
    padding: 16px 20px;
}

.btn-confirm {
    width: 100%;
    height: 40px;
    background: var(--color);
    color: #fff;
    border: none;
    border-radius: 6px;
    font-size: 15px;
    cursor: pointer;

    &:hover {
        opacity: 0.9;
    }
}
</style>
