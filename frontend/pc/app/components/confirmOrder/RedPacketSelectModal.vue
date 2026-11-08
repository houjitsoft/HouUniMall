<template>
    <PopupModal v-model="visible" title="选择红包" width="480px" content-class="redpacket-modal"
        overlay-class="no-flicker-overlay">
        <div v-if="loading" class="rp-loading">{{ $t('加载中...') }}</div>
        <div v-else class="rp-list">
            <div class="rp-item" :class="{ active: selectedRedPacketId === null }" @click="selectRedPacket(null)">
                <span class="rp-radio" :class="{ checked: selectedRedPacketId === null }">
                    <span v-if="selectedRedPacketId === null" class="radio-dot"></span>
                </span>
                <span class="rp-text">{{ $t('不使用红包') }}</span>
            </div>
            <div v-for="item in redPacketList" :key="item.redpacketId" class="rp-item"
                :class="{ active: selectedRedPacketId === item.redpacketId }" @click="selectRedPacket(item)">
                <span class="rp-radio" :class="{ checked: selectedRedPacketId === item.redpacketId }">
                    <span v-if="selectedRedPacketId === item.redpacketId" class="radio-dot"></span>
                </span>
                <span class="rp-desc">{{ $t(item.redpacketTitle || '红包') }}</span>
                <span class="rp-price">-{{ formatPriceWithSymbolSafe(item.redpacketPrice || 0) }}</span>
            </div>
        </div>
        <EmptyState v-if="!loading && redPacketList.length === 0" :text="$t('暂无可用红包')" :icon-size="48"
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
import { formatPriceWithSymbolSafe } from '~/composables/useFormatPrice'
import { selectRedPacketByDeposit } from '~/services/activity.service'
import type { RedPacketVO } from '~/types/activity'

const props = withDefaults(defineProps<{
    modelValue: boolean
    shopList: Array<{
        shopId: number | string
        voucherTId: string | number | ''
        goodsList: Array<{ goodsId: string | number; goodsPrice: string | number; goodsNumber: string | number }>
    }>
    totalPrice: string | number
    initialRedPacketId?: number | null
}>(), {
    initialRedPacketId: null
})

const emit = defineEmits<{
    'update:modelValue': [value: boolean]
    confirm: [payload: { redPacketId: number | null; redPacketTitle?: string; redPacketPrice?: number }]
}>()

const visible = computed({
    get: () => props.modelValue,
    set: (v) => emit('update:modelValue', v)
})

const loading = ref(false)
const redPacketList = ref<RedPacketVO[]>([])
const selectedRedPacketId = ref<number | null>(null)
const selectedRedPacket = ref<RedPacketVO | null>(null)

const selectRedPacket = (item: RedPacketVO | null) => {
    selectedRedPacketId.value = item?.redpacketId ?? null
    selectedRedPacket.value = item ?? null
}

const handleConfirm = () => {
    const item = selectedRedPacket.value
    emit('confirm', {
        redPacketId: selectedRedPacketId.value,
        redPacketTitle: item?.redpacketTitle,
        redPacketPrice: item?.redpacketPrice
    })
    visible.value = false
}

watch(() => props.modelValue, async (val) => {
    if (!val) return
    selectedRedPacketId.value = props.initialRedPacketId ?? null
    selectedRedPacket.value = null
    loading.value = true
    redPacketList.value = []
    try {
        const res = await selectRedPacketByDeposit({
            platForm: 1,
            redpacketTId: '',
            shopList: props.shopList,
            totalPrice: props.totalPrice
        })
        if (res?.code === 1 && Array.isArray(res?.data)) {
            redPacketList.value = res.data
            if (props.initialRedPacketId != null) {
                const found = res.data.find((v) => Number(v.redpacketId) === Number(props.initialRedPacketId))
                if (found) selectedRedPacket.value = found
            }
        }
    } catch {
        redPacketList.value = []
    } finally {
        loading.value = false
    }
})
</script>

<style scoped lang="scss">
:deep(.redpacket-modal .modal-body) {
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

.rp-loading { text-align: center; padding: 40px 0; color: #999; }
.rp-list { display: flex; flex-direction: column; }
.rp-item { display: flex; align-items: center; padding: 14px 16px; border: 1px solid transparent; border-radius: 6px; margin-bottom: 8px; cursor: pointer; &:hover { background: #fafafa; } &.active { background: rgba(82, 196, 26, 0.08); border-color: var(--color, #52c41a); } }
.rp-radio { width: 18px; height: 18px; border: 2px solid #d9d9d9; border-radius: 50%; margin-right: 12px; display: flex; align-items: center; justify-content: center; &.checked { border-color: var(--color, #52c41a); .radio-dot { width: 8px; height: 8px; border-radius: 50%; background: var(--color, #52c41a); } } }
.rp-text, .rp-desc { font-size: 14px; color: #333; }
.rp-desc { flex: 1; }
.rp-price { font-size: 14px; color: var(--color); font-weight: 600; }
:deep(.redpacket-modal .modal-footer) { padding: 16px 20px; }
.btn-confirm { width: 100%; height: 40px; background: var(--color); color: #fff; border: none; border-radius: 6px; font-size: 15px; cursor: pointer; }
</style>
