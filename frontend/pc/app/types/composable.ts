/**
 * Composables 相关类型定义
 * 统一管理所有 composables 的返回类型和选项类型
 */

import type { Ref } from 'vue'
import type { CountryInfo } from './form'

/**
 * 地区代码选择 composable 返回值
 */
export interface UseAreaCodeReturn {
    /** 是否显示地区代码选择器 */
    showAreaCode: Ref<boolean>
    /** 选中的国家信息 */
    selectedCountry: CountryInfo
    /** 处理地区代码选择 */
    handleSelect: (country: CountryInfo) => void
    /** 关闭地区代码选择器 */
    handleClose: () => void
    /** 打开地区代码选择器 */
    handleOpen: () => void
}

/**
 * 倒计时配置选项
 */
export interface CountdownOptions {
    /** 倒计时时长（秒），默认 60 */
    duration?: number
    /** 倒计时结束时的回调 */
    onFinish?: () => void
    /** 每次更新时的回调 */
    onUpdate?: (remaining: number) => void
}

/**
 * 倒计时 composable 返回值
 */
export interface UseCountdownReturn {
    /** 当前倒计时值（秒） */
    countdown: Ref<number>
    /** 是否正在倒计时 */
    isRunning: Ref<boolean>
    /** 开始倒计时 */
    start: () => void
    /** 停止倒计时 */
    stop: () => void
    /** 重置倒计时 */
    reset: () => void
    /** 获取倒计时文本 */
    getText: (defaultText: string) => string
}

/**
 * 确认弹窗选项
 */
export interface ConfirmModalOptions {
    /** 提示消息 */
    message?: string
    /** 标题 */
    title?: string
    /** 确认按钮文字 */
    confirmText?: string
    /** 取消按钮文字 */
    cancelText?: string
    /** 继续购物按钮文字 */
    keepShoppingText?: string
    /** 查看按钮文字 */
    goNowText?: string
    /** 点击"再逛逛"的回调 */
    onKeepShopping?: () => void
    /** 点击"马上去"的回调 */
    onGoNow?: () => void
    /** 点击关闭的回调 */
    onClose?: () => void
    /** 点击确认的回调 */
    onConfirm?: () => void
    /** 点击取消的回调 */
    onCancel?: () => void
}

/**
 * 商品输入下拉框状态
 */
export interface DropdownState {
    visible: boolean
    options: string[]
    loading: boolean
}

/**
 * 防抖定时器类型
 */
export type DebounceTimer = ReturnType<typeof setTimeout>
