/**
 * 支付页面返回拦截
 * 当用户点击返回上一页时，弹出确认框：
 * - 取消：关闭弹窗，留在当前页
 * - 确认：关闭当前页，跳转订单列表
 */
import { onMounted, onBeforeUnmount } from 'vue'
import { ConfirmModal } from '~/composables/useConfirmModal'
import { useIsLogin } from '~/composables/useAuth'

/** 跳转订单列表（替换当前页） */
const goToOrderList = () => {
    if (!useIsLogin()) return
    const config = useRuntimeConfig()
    const baseURL = config.public.apiBaseUrl as string
    window.location.href = `${baseURL}/ucenter/#/order/list`
}

/**
 * 支付页面返回拦截
 * 需在 setup 中调用
 */
export const usePaymentBackGuard = () => {
    let popstateHandler: (() => void) | null = null

    const showBackConfirm = () => {
        ConfirmModal.confirm({
            message: '确定要离开支付页面吗？',
            title: '温馨提示',
            confirmText: '确认',
            cancelText: '取消',
            onConfirm: () => {
                goToOrderList()
            },
            onCancel: () => {
                // 取消：关闭弹窗，留在当前页
            }
        })
    }

    const handlePopState = () => {
        // 阻止实际返回，推回当前状态
        history.pushState(null, '', window.location.href)
        showBackConfirm()
    }

    onMounted(() => {
        if (import.meta.server || typeof window === 'undefined') return
        // 推入一个历史记录，使返回时有可拦截的 popstate
        history.pushState(null, '', window.location.href)
        popstateHandler = handlePopState
        window.addEventListener('popstate', popstateHandler)
    })

    onBeforeUnmount(() => {
        if (popstateHandler) {
            window.removeEventListener('popstate', popstateHandler)
        }
    })

    return { showBackConfirm }
}
