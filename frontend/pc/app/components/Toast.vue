<template>
    <div class="toast-container">
        <TransitionGroup name="toast" tag="div" class="toast-list">
            <div v-for="toast in toasts" :key="toast.id" class="toast-item" :class="[
                `toast-${toast.type}`,
                `toast-${toast.position}`
            ]">
                <div class="toast-content">
                    <span class="toast-icon" :class="getIconClass(toast.type)" aria-hidden="true"></span>
                    <span class="toast-message">{{ t(toast.message || '') }}</span>
                </div>
                <button v-if="toast.showClose" type="button" class="toast-close" aria-label="关闭"
                    @click="close(toast.id)">
                    <span class="toast-close-icon">×</span>
                </button>
            </div>
        </TransitionGroup>
    </div>
</template>

<script setup lang="ts">
import type { Toast, ToastType } from '~/types/ui'
import { useTranslate } from '~/composables/useTranslate'
const { t } = useTranslate()
/** 组件 Props */
const props = defineProps<{
    /** 所有弹窗的数组 */
    toasts: Toast[]
}>()

/** 组件事件 */
const emit = defineEmits<{
    /** 点击关闭按钮时触发 */
    close: [id: number]
}>()

/**
 * 根据弹窗类型获取对应的图标类名
 * @param type 弹窗类型
 * @returns 图标类名
 */
const getIconClass = (type: ToastType): string => {
    const iconMap: Record<ToastType, string> = {
        success: 'toast-icon-success',
        error: 'toast-icon-error',
        warning: 'toast-icon-warning',
        info: 'toast-icon-info'
    }
    return iconMap[type] || iconMap.info
}

/**
 * 关闭指定 ID 的弹窗
 * @param id 弹窗 ID
 */
const close = (id: number) => {
    emit('close', id)
}
</script>

<style scoped>
.toast-container {
    position: fixed;
    top: 0;
    left: 0;
    width: 100%;
    height: 100%;
    pointer-events: none;
    z-index: 9999;
}

.toast-list {
    position: relative;
    width: 100%;
    height: 100%;
    pointer-events: none;
}

.toast-item {
    position: absolute;
    display: flex;
    align-items: center;
    justify-content: space-between;
    min-width: 220px;
    max-width: 420px;
    padding: 14px 18px;
    border-radius: 10px;
    background-color: #fff;
    box-shadow: 0 4px 20px rgba(0, 0, 0, 0.12), 0 2px 8px rgba(0, 0, 0, 0.08);
    pointer-events: auto;
    transition: box-shadow 0.25s ease, transform 0.25s ease;
}

.toast-item:hover {
    box-shadow: 0 6px 24px rgba(0, 0, 0, 0.14), 0 2px 10px rgba(0, 0, 0, 0.08);
}

.toast-top {
    top: 24px;
    left: 50%;
    transform: translateX(-50%);
}

.toast-center {
    top: 50%;
    left: 50%;
    transform: translate(-50%, -50%);
}

.toast-bottom {
    bottom: 24px;
    left: 50%;
    transform: translateX(-50%);
}

.toast-success {
    border-left: 4px solid #52c41a;
    background: linear-gradient(to right, #f6ffed 0%, #fff 12px);
}

.toast-error {
    border-left: 4px solid #ff4d4f;
    background: linear-gradient(to right, #fff2f0 0%, #fff 12px);
}

.toast-warning {
    border-left: 4px solid #faad14;
    background: linear-gradient(to right, #fffbe6 0%, #fff 12px);
}

.toast-info {
    border-left: 4px solid #1890ff;
    background: linear-gradient(to right, #e6f7ff 0%, #fff 12px);
}

.toast-content {
    display: flex;
    align-items: center;
    gap: 12px;
    min-width: 0;
}

.toast-icon {
    flex-shrink: 0;
    display: flex;
    align-items: center;
    justify-content: center;
    width: 24px;
    height: 24px;
    border-radius: 50%;
    font-size: 14px;
    font-weight: 700;
    line-height: 1;
}

.toast-icon-success {
    color: #52c41a;
    background-color: rgba(82, 196, 26, 0.12);
}

.toast-icon-error {
    color: #ff4d4f;
    background-color: rgba(255, 77, 79, 0.12);
}

.toast-icon-warning {
    color: #d48806;
    background-color: rgba(250, 173, 20, 0.15);
}

.toast-icon-info {
    color: #1890ff;
    background-color: rgba(24, 144, 255, 0.12);
}

.toast-message {
    flex: 1;
    font-size: 14px;
    color: #333;
    line-height: 1.5;
    font-weight: 500;
}

.toast-close {
    flex-shrink: 0;
    background: none;
    border: none;
    color: #999;
    cursor: pointer;
    padding: 0;
    width: 28px;
    height: 28px;
    display: flex;
    align-items: center;
    justify-content: center;
    border-radius: 6px;
    transition: background-color 0.2s ease, color 0.2s ease;
}

.toast-close-icon {
    font-size: 20px;
    line-height: 1;
    font-weight: 300;
}

.toast-close:hover {
    background-color: rgba(0, 0, 0, 0.06);
    color: #333;
}

/* 入场 / 离场动效 */
.toast-enter-active,
.toast-leave-active {
    transition: opacity 0.25s ease, transform 0.25s ease;
}

.toast-enter-from,
.toast-leave-to {
    opacity: 0;
}

.toast-enter-from.toast-top,
.toast-leave-to.toast-top {
    transform: translate(-50%, -12px);
}

.toast-enter-from.toast-bottom,
.toast-leave-to.toast-bottom {
    transform: translate(-50%, 12px);
}

.toast-enter-from.toast-center,
.toast-leave-to.toast-center {
    transform: translate(-50%, -50%) scale(0.95);
}
</style>