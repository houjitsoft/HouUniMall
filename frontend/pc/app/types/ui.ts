/**
 * UI 组件相关类型定义
 */

/** 弹窗显示位置类型 */
export type ToastPosition = 'top' | 'center' | 'bottom'

/** 弹窗类型 */
export type ToastType = 'success' | 'error' | 'warning' | 'info'

/** 弹窗选项接口 */
export interface ToastOptions {
    /** 弹窗消息内容 */
    message: string
    /** 弹窗类型，默认 'info' */
    type?: ToastType
    /** 显示位置，默认 'top' */
    position?: ToastPosition
    /** 显示时长（毫秒），默认 3000，0 表示永久显示 */
    duration?: number
    /** 是否显示关闭按钮，默认 false */
    showClose?: boolean
}

/** 主题配置接口 */
export interface ThemeConfig {
    /** 主色调 */
    primary: string
    /** 背景色 */
    background?: string
    /** 文本色 */
    text?: string
}

/** Toast 弹窗数据结构 */
export interface Toast {
    /** 弹窗唯一 ID */
    id: number
    /** 弹窗消息内容 */
    message: string
    /** 弹窗类型 */
    type: ToastType
    /** 显示位置 */
    position: ToastPosition
    /** 显示时长（毫秒） */
    duration: number
    /** 是否显示关闭按钮 */
    showClose: boolean
}
