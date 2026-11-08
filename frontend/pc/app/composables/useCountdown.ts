/**
 * 倒计时 composable
 * 
 * 提供统一的倒计时功能，用于验证码倒计时等场景
 * 
 * @example
 * const { countdown, start, stop, reset } = useCountdown(60)
 * start() // 开始倒计时
 * stop() // 停止倒计时
 * reset() // 重置倒计时
 */

import { ref, onBeforeUnmount } from 'vue'
import type { CountdownOptions, UseCountdownReturn } from '~/types/composable'

// 重新导出类型，保持向后兼容
export type { CountdownOptions, UseCountdownReturn }

/**
 * 倒计时 composable
 * 
 * @param options 配置选项
 * @returns 倒计时相关的方法和状态
 */
export const useCountdown = (options: CountdownOptions = {}): UseCountdownReturn => {
    const {
        duration = 60,
        onFinish,
        onUpdate
    } = options

    const countdown = ref(0)
    const isRunning = ref(false)
    let timer: ReturnType<typeof setInterval> | null = null

    /**
     * 清理定时器
     */
    const clearTimer = () => {
        if (timer) {
            clearInterval(timer)
            timer = null
        }
        isRunning.value = false
    }

    /**
     * 开始倒计时
     */
    const start = () => {
        // 如果正在倒计时，不重复启动
        if (isRunning.value && countdown.value > 0) {
            return
        }

        clearTimer()
        countdown.value = duration
        isRunning.value = true

        timer = setInterval(() => {
            countdown.value--
            
            // 触发更新回调
            if (onUpdate) {
                onUpdate(countdown.value)
            }

            if (countdown.value <= 0) {
                clearTimer()
                // 触发结束回调
                if (onFinish) {
                    onFinish()
                }
            }
        }, 1000)
    }

    /**
     * 停止倒计时
     */
    const stop = () => {
        clearTimer()
    }

    /**
     * 重置倒计时
     */
    const reset = () => {
        clearTimer()
        countdown.value = 0
    }

    /**
     * 获取倒计时文本
     * 
     * @param defaultText 默认文本
     * @returns 倒计时文本
     */
    const getText = (defaultText: string): string => {
        return countdown.value > 0 ? `${countdown.value}秒后重新获取` : defaultText
    }

    // 组件卸载时清理定时器
    onBeforeUnmount(() => {
        clearTimer()
    })

    return {
        countdown,
        isRunning,
        start,
        stop,
        reset,
        getText
    }
}
