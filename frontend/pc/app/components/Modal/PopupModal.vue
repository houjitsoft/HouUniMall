<template>
    <Teleport to="body">
        <Transition name="modal-fade">
            <div v-if="visible" class="modal-overlay" :class="overlayClass" @click="handleOverlayClick">
                <Transition name="modal-scale">
                    <div class="modal-content" :class="contentClass" :style="contentStyle" role="dialog"
                        aria-modal="true" :aria-labelledby="title ? 'popup-modal-title' : undefined" @click.stop>
                        <!-- 头部：优先使用 header 插槽，否则用 title -->
                        <div class="modal-header">
                            <template v-if="$slots.header">
                                <slot name="header" />
                            </template>
                            <template v-else-if="title">
                                <span :id="'popup-modal-title'" class="modal-title">{{ $t(title) }}</span>
                            </template>
                            <span v-else class="modal-title-placeholder"></span>
                            <button v-if="showClose" type="button" class="modal-close" aria-label="{{ $t('关闭') }}"
                                @click="handleClose">
                                ×
                            </button>
                        </div>
                        <!-- 主体内容 -->
                        <div class="modal-body">
                            <slot />
                        </div>
                        <!-- 底部：有 footer 插槽则渲染 -->
                        <div v-if="$slots.footer" class="modal-footer">
                            <slot name="footer" />
                        </div>
                    </div>
                </Transition>
            </div>
        </Transition>
    </Teleport>
</template>

<script setup lang="ts">
import { computed, watch, onMounted, onUnmounted } from 'vue'

const BODY_LOCK_CLASS = 'modal-open'

const props = withDefaults(
    defineProps<{
        /** v-model 控制显隐 */
        modelValue: boolean
        /** 标题（不使用则可通过 header 插槽自定义） */
        title?: string
        /** 是否显示右上角关闭按钮 */
        showClose?: boolean
        /** 点击遮罩是否关闭 */
        closeOnOverlay?: boolean
        /** 打开时是否锁定背景滚动 */
        lockScroll?: boolean
        /** 遮罩层额外 class */
        overlayClass?: string
        /** 内容框额外 class（如宽度等） */
        contentClass?: string
        /** 内容框宽度，支持 px、%（如 '440px'、'90%'） */
        width?: string
    }>(),
    {
        title: '',
        showClose: true,
        closeOnOverlay: true,
        lockScroll: true,
        overlayClass: '',
        contentClass: '',
        width: ''
    }
)

const emit = defineEmits<{
    'update:modelValue': [value: boolean]
    close: []
}>()

const visible = computed({
    get: () => props.modelValue,
    set: (v) => emit('update:modelValue', v)
})

const contentStyle = computed(() => {
    if (!props.width?.trim()) return undefined
    return { width: props.width.trim(), maxWidth: props.width.trim() }
})

const handleClose = () => {
    visible.value = false
    emit('close')
}

const handleOverlayClick = () => {
    if (props.closeOnOverlay) {
        handleClose()
    }
}

// 打开时锁定背景滚动，关闭时解除
watch(
    () => props.modelValue,
    (val) => {
        if (!props.lockScroll) return
        if (val) {
            document.body.classList.add(BODY_LOCK_CLASS)
        } else {
            document.body.classList.remove(BODY_LOCK_CLASS)
        }
    }
)

// ESC 关闭
const onKeydown = (e: KeyboardEvent) => {
    if (e.key === 'Escape' && props.modelValue) {
        handleClose()
    }
}

onMounted(() => window.addEventListener('keydown', onKeydown))
onUnmounted(() => {
    window.removeEventListener('keydown', onKeydown)
    document.body.classList.remove(BODY_LOCK_CLASS)
})
</script>

<style lang="scss" scoped>
.modal-title-placeholder {
    display: block;
    width: 1px;
    height: 1px;
    overflow: hidden;
}
</style>
