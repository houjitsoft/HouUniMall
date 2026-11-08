/**
 * 确认弹窗 API
 * 提供公共 API 调用方式显示确认弹窗
 * 
 * @example
 * ConfirmModal.show({
 *   message: '立即去购物车查看',
 *   onKeepShopping: () => { console.log('再逛逛') },
 *   onGoNow: () => { navigateTo('/cart') }
 * })
 */

import { ref, createApp, h } from 'vue'
import ConfirmModalComponent from '~/components/Modal/ConfirmModal.vue'
import type { ConfirmModalOptions } from '~/types/composable'

// 重新导出类型，保持向后兼容
export type { ConfirmModalOptions }

let modalApp: ReturnType<typeof createApp> | null = null
let modalContainer: HTMLElement | null = null

/**
 * 显示确认弹窗（返回 Promise）
 * @param options 弹窗选项
 * @returns Promise<boolean> true表示确认，false表示取消
 */
const confirm = (options: ConfirmModalOptions = {}): Promise<boolean> => {
    return new Promise((resolve) => {
        const {
            message = '确定要执行此操作吗？',
            title = '温馨提示',
            confirmText = '确定',
            cancelText = '取消',
            onConfirm,
            onCancel
        } = options

        // 如果已经显示，先关闭
        if (modalApp && modalContainer) {
            close()
        }

        // 创建应用实例
        const visible = ref(true)
        
        modalApp = createApp({
            setup() {
                const handleClose = () => {
                    visible.value = false
                    resolve(false)
                    onCancel?.()
                    // 延迟销毁，等待动画完成
                    setTimeout(() => {
                        destroy()
                    }, 300)
                }

                const handleCancel = () => {
                    visible.value = false
                    resolve(false)
                    onCancel?.()
                    // 延迟销毁，等待动画完成
                    setTimeout(() => {
                        destroy()
                    }, 300)
                }

                const handleConfirm = () => {
                    visible.value = false
                    resolve(true)
                    onConfirm?.()
                    // 延迟销毁，等待动画完成
                    setTimeout(() => {
                        destroy()
                    }, 300)
                }

            return () => h(ConfirmModalComponent, {
                visible: visible.value,
                message,
                title,
                confirmText,
                cancelText,
                mode: 'confirm',
                onClose: handleClose,
                onCancel: handleCancel,
                onConfirm: handleConfirm
            })
            }
        })

        // 创建容器并挂载
        modalContainer = document.createElement('div')
        document.body.appendChild(modalContainer)
        modalApp.mount(modalContainer)
    })
}

/**
 * 显示确认弹窗（原有方法，用于购物车场景）
 * @param options 弹窗选项
 */
const show = (options: ConfirmModalOptions = {}) => {
    const {
        message = '立即去购物车查看',
        title = '温馨提示',
        keepShoppingText = '再逛逛',
        goNowText = '马上去',
        onKeepShopping,
        onGoNow,
        onClose
    } = options

    // 如果已经显示，先关闭
    if (modalApp && modalContainer) {
        close()
    }

    // 创建应用实例
    const visible = ref(true)
    
    modalApp = createApp({
        setup() {
            const handleClose = () => {
                visible.value = false
                onClose?.()
                // 延迟销毁，等待动画完成
                setTimeout(() => {
                    destroy()
                }, 300)
            }

            const handleKeepShopping = () => {
                visible.value = false
                onKeepShopping?.()
                // 延迟销毁，等待动画完成
                setTimeout(() => {
                    destroy()
                }, 300)
            }

            const handleGoNow = () => {
                visible.value = false
                onGoNow?.()
                // 延迟销毁，等待动画完成
                setTimeout(() => {
                    destroy()
                }, 300)
            }

            return () => h(ConfirmModalComponent, {
                visible: visible.value,
                message,
                title,
                keepShoppingText,
                goNowText,
                onClose: handleClose,
                onKeepShopping: handleKeepShopping,
                onGoNow: handleGoNow
            })
        }
    })

    // 创建容器并挂载
    modalContainer = document.createElement('div')
    document.body.appendChild(modalContainer)
    modalApp.mount(modalContainer)
}

/**
 * 关闭弹窗
 */
const close = () => {
    if (modalApp && modalContainer) {
        destroy()
    }
}

/**
 * 销毁弹窗实例
 */
const destroy = () => {
    if (modalApp && modalContainer) {
        modalApp.unmount()
        document.body.removeChild(modalContainer)
        modalApp = null
        modalContainer = null
    }
}

/**
 * 确认弹窗 API 对象
 */
export const ConfirmModal = {
    /** 显示弹窗（原有方法，用于购物车场景） */
    show,
    /** 确认对话框（返回 Promise） */
    confirm,
    /** 关闭弹窗 */
    close
}
