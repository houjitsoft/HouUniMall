/**
 * 自定义弹窗 API
 * 提供成功、错误、警告、信息四种类型的弹窗
 * 支持顶部、居中、底部三种显示位置
 * 
 * @example
 * Toast.success('操作成功')
 * Toast.error('操作失败', { position: 'center', duration: 5000 })
 */

import { ref, createApp, h } from 'vue'
import ToastComponent from '../components/Toast.vue'
import type { ToastPosition, ToastType, ToastOptions } from '~/types/ui'

// 重新导出类型，保持向后兼容
export type { ToastPosition, ToastType }

/** 弹窗管理状态 */
const toasts = ref<Array<{
    id: number
    message: string
    type: ToastType
    position: ToastPosition
    duration: number
    showClose: boolean
}>>([])

let toastId = 0

/**
 * 创建弹窗
 * @param options 弹窗选项
 * @returns 弹窗 ID，用于手动关闭
 */
const createToast = (options: ToastOptions) => {
    const id = ++toastId
    const {
        message,
        type = 'info',
        position = 'top',
        duration = 3000,
        showClose = false
    } = options

    toasts.value.push({
        id,
        message,
        type,
        position,
        duration,
        showClose
    })

    if (duration > 0) {
        setTimeout(() => {
            removeToast(id)
        }, duration)
    }

    return id
}

/**
 * 移除弹窗
 * @param id 弹窗 ID
 */
const removeToast = (id: number) => {
    const index = toasts.value.findIndex(toast => toast.id === id)
    if (index > -1) {
        toasts.value.splice(index, 1)
    }
}

/**
 * 显示成功提示
 * @param message 提示消息
 * @param options 弹窗选项
 * @returns 弹窗 ID
 * @example Toast.success('操作成功')
 */
const success = (message: string, options?: Omit<ToastOptions, 'message' | 'type'>) => {
    return createToast({
        message,
        type: 'success',
        ...options
    })
}

/**
 * 显示错误提示
 * @param message 提示消息
 * @param options 弹窗选项
 * @returns 弹窗 ID
 * @example Toast.error('操作失败')
 */
const error = (message: string, options?: Omit<ToastOptions, 'message' | 'type'>) => {
    return createToast({
        message,
        type: 'error',
        ...options
    })
}

/**
 * 显示警告提示
 * @param message 提示消息
 * @param options 弹窗选项
 * @returns 弹窗 ID
 * @example Toast.warning('请谨慎操作')
 */
const warning = (message: string, options?: Omit<ToastOptions, 'message' | 'type'>) => {
    return createToast({
        message,
        type: 'warning',
        ...options
    })
}

/**
 * 显示信息提示
 * @param message 提示消息
 * @param options 弹窗选项
 * @returns 弹窗 ID
 * @example Toast.info('这是一条信息')
 */
const info = (message: string, options?: Omit<ToastOptions, 'message' | 'type'>) => {
    return createToast({
        message,
        type: 'info',
        ...options
    })
}

/**
 * 手动关闭弹窗
 * @param id 弹窗 ID
 * @example
 * const toastId = Toast.success('可手动关闭')
 * Toast.close(toastId)
 */
const close = (id: number) => {
    removeToast(id)
}

/**
 * 挂载弹窗组件到 DOM
 * 应该在应用初始化时调用一次
 * @example Toast.mountToasts()
 */
const mountToasts = () => {
    const app = createApp({
        setup() {
            return () => h(ToastComponent, {
                toasts: toasts.value,
                onClose: close
            })
        }
    })
    const container = document.createElement('div')
    document.body.appendChild(container)
    app.mount(container)
}

/**
 * 自定义弹窗 API 对象
 * 包含所有弹窗管理方法
 */
export const Toast = {
    /** 所有弹窗的响应式数组 */
    toasts,
    /** 显示成功提示 */
    success,
    /** 显示错误提示 */
    error,
    /** 显示警告提示 */
    warning,
    /** 显示信息提示 */
    info,
    /** 手动关闭弹窗 */
    close,
    /** 挂载弹窗组件到 DOM */
    mountToasts
}

// 应用初始化时自动挂载 Toast 组件
if (typeof window !== 'undefined') {
    // 确保 DOM 加载完成后再挂载
    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', () => {
            Toast.mountToasts()
        })
    } else {
        // 如果 DOM 已经加载完成，立即挂载
        Toast.mountToasts()
    }
}