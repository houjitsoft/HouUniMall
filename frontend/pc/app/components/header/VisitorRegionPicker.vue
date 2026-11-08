<template>
    <ClientOnly>
        <button ref="triggerRef" type="button" class="region-trigger" :aria-expanded="open"
            aria-haspopup="dialog" :title="`当前区域：${displayLabel}`" @click="togglePanel">
            <svg class="region-pin" viewBox="0 0 24 24" aria-hidden="true">
                <path d="M12 21s7-6.1 7-12a7 7 0 1 0-14 0c0 5.9 7 12 7 12Z" />
                <circle cx="12" cy="9" r="2.4" />
            </svg>
            <span class="region-label">{{ displayLabel }}</span>
            <svg class="region-chevron" :class="{ open }" viewBox="0 0 20 20" aria-hidden="true">
                <path d="m5.5 7.5 4.5 4 4.5-4" />
            </svg>
        </button>

        <Teleport to="body">
            <div v-if="open" ref="panelRef" class="region-panel" :style="panelStyle" role="dialog"
                aria-label="选择商城区域">
                <div class="region-panel-head">
                    <div>
                        <strong>选择区域</strong>
                        <p>用于商品库存、配送范围和预计送达时间</p>
                    </div>
                    <button type="button" class="region-close" aria-label="关闭" @click="closePanel">×</button>
                </div>

                <div class="region-current">
                    <span>当前</span>
                    <strong>{{ displayLabel }}</strong>
                </div>

                <AddressLinkage v-model="draftAreaIds" :show-area="3" return-type="id" width="132px" />
                <p v-if="validationMessage" class="region-error">{{ validationMessage }}</p>

                <div class="region-actions">
                    <button type="button" class="region-locate" :disabled="loading" @click="relocate">
                        <svg viewBox="0 0 24 24" aria-hidden="true">
                            <circle cx="12" cy="12" r="3" />
                            <path d="M12 2v3M12 19v3M2 12h3M19 12h3" />
                        </svg>
                        {{ loading ? '定位中…' : '重新定位' }}
                    </button>
                    <button type="button" class="region-confirm" @click="confirmSelection">确定</button>
                </div>
            </div>
        </Teleport>
    </ClientOnly>
</template>

<script setup lang="ts">
import { nextTick, onBeforeUnmount, onMounted, ref, watch } from 'vue'
import AddressLinkage from '~/components/Address/AddressLinkage.vue'
import { useVisitorRegion } from '~/composables/useVisitorRegion'
import { DEFAULT_CN_COUNTRY } from '~/utils/visitorArea'

const {
    location,
    loading,
    displayLabel,
    loadDistrictTree,
    resolveAreaNames,
    setVisitorRegion,
    locateByIp
} = useVisitorRegion()

const triggerRef = ref<HTMLElement | null>(null)
const panelRef = ref<HTMLElement | null>(null)
const open = ref(false)
const draftAreaIds = ref<string[]>([])
const validationMessage = ref('')
const panelStyle = ref({ top: '0px', left: '0px' })

const syncDraft = () => {
    draftAreaIds.value = Array.isArray(location.value?.areaIds)
        ? location.value!.areaIds!.map(String)
        : []
}

const updatePosition = () => {
    const trigger = triggerRef.value
    if (!trigger || typeof window === 'undefined') return
    const rect = trigger.getBoundingClientRect()
    const panelWidth = Math.min(468, window.innerWidth - 24)
    panelStyle.value = {
        top: `${rect.bottom + 8}px`,
        left: `${Math.max(12, Math.min(rect.left, window.innerWidth - panelWidth - 12))}px`
    }
}

const closePanel = () => {
    open.value = false
    validationMessage.value = ''
}

const togglePanel = async () => {
    if (open.value) {
        closePanel()
        return
    }
    syncDraft()
    validationMessage.value = ''
    await loadDistrictTree()
    open.value = true
    await nextTick()
    updatePosition()
}

const confirmSelection = async () => {
    const ids = draftAreaIds.value.map(String)
    if (ids.length !== 3 || ids.some(id => Number(id) <= 0)) {
        validationMessage.value = '请选择完整的省、市、区'
        return
    }
    const areaNames = await resolveAreaNames(ids)
    if (!areaNames) {
        validationMessage.value = '区域数据已更新，请重新选择'
        return
    }
    await setVisitorRegion({
        ...DEFAULT_CN_COUNTRY,
        areaIds: ids,
        areaNames,
        source: 'manual'
    })
    closePanel()
}

const relocate = async () => {
    validationMessage.value = ''
    await locateByIp(true)
    syncDraft()
    closePanel()
}

const onDocumentPointerDown = (event: MouseEvent) => {
    if (!open.value) return
    const target = event.target as Node | null
    if (!target || triggerRef.value?.contains(target) || panelRef.value?.contains(target)) return
    closePanel()
}

const onDocumentKeyDown = (event: KeyboardEvent) => {
    if (event.key === 'Escape' && open.value) {
        closePanel()
        triggerRef.value?.focus()
    }
}

watch(location, syncDraft, { deep: true })

onMounted(() => {
    document.addEventListener('mousedown', onDocumentPointerDown)
    document.addEventListener('keydown', onDocumentKeyDown)
    window.addEventListener('resize', updatePosition)
    window.addEventListener('scroll', updatePosition, true)
})

onBeforeUnmount(() => {
    document.removeEventListener('mousedown', onDocumentPointerDown)
    document.removeEventListener('keydown', onDocumentKeyDown)
    window.removeEventListener('resize', updatePosition)
    window.removeEventListener('scroll', updatePosition, true)
})
</script>

<style scoped lang="scss">
.region-trigger {
    max-width: 210px;
    min-height: 26px;
    padding: 2px 5px;
    border: 0;
    border-radius: 4px;
    display: inline-flex;
    align-items: center;
    gap: 4px;
    color: inherit;
    background: transparent;
    font: inherit;
    cursor: pointer;
    transition: color 0.2s ease, background 0.2s ease;

    &:hover,
    &:focus-visible {
        color: #fff;
        background: rgba(255, 255, 255, 0.08);
        outline: none;
    }
}

.region-pin,
.region-locate svg {
    width: 15px;
    height: 15px;
    flex: 0 0 auto;
    fill: none;
    stroke: currentColor;
    stroke-width: 1.8;
    stroke-linecap: round;
    stroke-linejoin: round;
}

.region-label {
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
}

.region-chevron {
    width: 12px;
    height: 12px;
    flex: 0 0 auto;
    fill: none;
    stroke: currentColor;
    stroke-width: 1.8;
    transition: transform 0.2s ease;

    &.open {
        transform: rotate(180deg);
    }
}

.region-panel {
    position: fixed;
    z-index: 10020;
    width: min(468px, calc(100vw - 24px));
    padding: 18px;
    box-sizing: border-box;
    color: #2f3437;
    background: #fff;
    border: 1px solid #e6e8ea;
    border-radius: 10px;
    box-shadow: 0 14px 38px rgba(16, 24, 40, 0.16);
}

.region-panel-head {
    display: flex;
    align-items: flex-start;
    justify-content: space-between;
    gap: 16px;
    margin-bottom: 14px;

    strong {
        font-size: 16px;
        line-height: 1.4;
    }

    p {
        margin: 3px 0 0;
        color: #777f87;
        font-size: 12px;
        line-height: 1.5;
    }
}

.region-close {
    width: 28px;
    height: 28px;
    padding: 0;
    border: 0;
    border-radius: 5px;
    color: #6f767d;
    background: transparent;
    font-size: 22px;
    line-height: 26px;
    cursor: pointer;

    &:hover,
    &:focus-visible {
        color: #202427;
        background: #f2f4f5;
        outline: none;
    }
}

.region-current {
    margin-bottom: 14px;
    padding: 9px 11px;
    display: flex;
    align-items: center;
    gap: 9px;
    color: #566068;
    background: #f5f7f7;
    border-radius: 6px;
    font-size: 13px;

    span {
        color: #8a9298;
    }

    strong {
        color: var(--color, #ea5455);
        font-weight: 600;
    }
}

.region-error {
    margin: 8px 0 0;
    color: #c33d32;
    font-size: 12px;
}

.region-actions {
    margin-top: 16px;
    display: flex;
    align-items: center;
    justify-content: space-between;
    gap: 12px;
}

.region-locate,
.region-confirm {
    min-height: 34px;
    border-radius: 5px;
    font-size: 13px;
    cursor: pointer;
}

.region-locate {
    padding: 0 4px;
    border: 0;
    display: inline-flex;
    align-items: center;
    gap: 6px;
    color: #5c666d;
    background: transparent;

    &:hover:not(:disabled),
    &:focus-visible {
        color: var(--color, #ea5455);
        outline: none;
    }

    &:disabled {
        cursor: wait;
        opacity: 0.6;
    }
}

.region-confirm {
    min-width: 76px;
    padding: 0 18px;
    border: 1px solid var(--color, #ea5455);
    color: #fff;
    background: var(--color, #ea5455);

    &:hover,
    &:focus-visible {
        filter: brightness(0.94);
        outline: 2px solid rgba(234, 84, 85, 0.18);
        outline-offset: 2px;
    }
}

@media (max-width: 560px) {
    .region-panel {
        padding: 15px;
    }

    .region-panel :deep(.area-select-wrapper) {
        flex-direction: column;
    }

    .region-panel :deep(.custom-select) {
        width: 100% !important;
    }
}
</style>
