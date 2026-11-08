<template>
    <Transition name="modal-fade">
        <div v-if="visible" class="modal-overlay" @click="handleClose">
            <Transition name="modal-scale">
                <div class="modal-content" @click.stop>
                    <!-- 弹窗头部 -->
                    <div class="modal-header">
                        <div class="modal-title">{{ t(title) }}</div>
                        <div class="modal-close" @click="handleClose">×</div>
                    </div>
                    <!-- 内容区域 -->
                    <div class="modal-body">
                        <div class="message-text" v-html="formatMessage(message)"></div>
                    </div>
                    <!-- 底部按钮 -->
                    <div class="modal-footer">
                        <!-- 通用确认对话框模式 -->
                        <template v-if="isConfirmMode">
                            <button class="btn-cancel" @click="handleCancel">{{ t(cancelText) }}</button>
                            <button class="btn-confirm" @click="handleConfirm">{{ t(confirmText) }}</button>
                        </template>
                        <!-- 购物车场景模式 -->
                        <template v-else>
                            <button class="btn-keep-shopping" @click="handleKeepShopping">{{ t(keepShoppingText)
                            }}</button>
                            <button class="btn-go-now" @click="handleGoNow">{{ t(goNowText) }}</button>
                        </template>
                    </div>
                </div>
            </Transition>
        </div>
    </Transition>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import type { ConfirmModalProps, ConfirmModalEmits } from '~/types/component'
import { useTranslate } from '~/composables/useTranslate'
const { t } = useTranslate()

type Props = ConfirmModalProps
type Emits = ConfirmModalEmits

const props = withDefaults(defineProps<Props>(), {
    message: '立即去购物车查看',
    title: '温馨提示',
    confirmText: '确定',
    cancelText: '取消',
    keepShoppingText: '再逛逛',
    goNowText: '马上去',
    mode: 'shopping'
})

const emit = defineEmits<Emits>()

// 判断是否为通用确认对话框模式
const isConfirmMode = computed(() => {
    return props.mode === 'confirm'
})

const handleClose = () => {
    emit('close')
}

const handleKeepShopping = () => {
    emit('keepShopping')
}

const handleGoNow = () => {
    emit('goNow')
}

const handleConfirm = () => {
    emit('confirm')
}

const handleCancel = () => {
    emit('cancel')
}

// 格式化消息，将换行符转换为 <br> 标签
const formatMessage = (message?: string) => {
    if (!message) return ''
    // 先处理换行符，确保 HTML 标签不被破坏
    return message.replace(/\n/g, '<br>')
}
</script>

<style lang="scss" scoped>
.modal-content {
    width: 400px;
    max-width: 90vw;
    border-radius: 12px;
    overflow: hidden;
}

.modal-body {
    padding: 24px 20px 20px;
    text-align: center;

    .message-text {
        font-size: 15px;
        color: #333;
        font-weight: 500;
        line-height: 1.75;

        :deep(strong) {
            color: var(--color);
            font-weight: 600;
        }
    }
}

.modal-footer {
    display: flex;
    gap: 12px;
    padding: 16px 20px 20px;
    border-top: 1px solid #e8eaed;
    background: #fafbfc;

    button {
        flex: 1;
        padding: 10px 20px;
        border: none;
        border-radius: 8px;
        font-size: 15px;
        font-weight: 500;
        cursor: pointer;
        transition: background-color 0.2s, border-color 0.2s, transform 0.15s;

        &:focus {
            outline: none;
        }

        &:focus-visible {
            outline: 2px solid var(--color);
            outline-offset: 2px;
        }

        &:active {
            transform: scale(0.98);
        }
    }

    .btn-keep-shopping,
    .btn-cancel {
        background-color: #fff;
        color: #333;
        border: 1px solid #e0e0e0;

        &:hover {
            background-color: #f5f5f5;
            border-color: #d0d0d0;
        }
    }

    .btn-go-now,
    .btn-confirm {
        background-color: var(--color);
        color: #fff;
        border: 1px solid transparent;

        &:hover {
            background-color: color-mix(in srgb, var(--color) 85%, #000);
        }
    }
}
</style>
