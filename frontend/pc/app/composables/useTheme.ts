/**
 * 主题管理 Composable
 * 根据后台返回的颜色主题，动态设置 CSS 变量
 * 支持主题切换、存储和恢复
 */

import { ref, computed } from 'vue'
import type { ThemeConfig } from '~/types/ui'

// 重新导出类型，保持向后兼容
export type { ThemeConfig }

/** 与 main.css :root 一致，避免 SSR / 客户端主色打架；对齐后台当前 TC 配置色（#EA5455） */
const DEFAULT_PRIMARY = '#ea5455'

/** 默认主题配置 */
const defaultTheme: ThemeConfig = {
    primary: DEFAULT_PRIMARY,
    background: '#ffffff',
    text: '#202a33'
}

/** 存储主题的本地存储键 */
const THEME_STORAGE_KEY = 'app-theme-config'

/** 响应式主题配置 */
const themeConfig = ref<ThemeConfig>(defaultTheme)

const clampByte = (n: number): number => Math.max(0, Math.min(255, Math.round(n)))

const parseHex = (hex: string): [number, number, number] | null => {
    hex = hex.replace(/^#/, '')
    if (hex.length === 3) {
        hex = hex.split('').map((c) => c + c).join('')
    }
    if (hex.length !== 6 || Number.isNaN(parseInt(hex, 16))) {
        return null
    }
    return [
        parseInt(hex.slice(0, 2), 16),
        parseInt(hex.slice(2, 4), 16),
        parseInt(hex.slice(4, 6), 16)
    ]
}

/** 按百分比加深（负值）或提亮（正值） */
const shadeHex = (hex: string, percent: number): string => {
    const rgb = parseHex(hex)
    if (!rgb) return hex
    const factor = 1 + percent / 100
    const [r, g, b] = rgb.map((c) => clampByte(c * factor))
    return `#${[r, g, b].map((c) => c.toString(16).padStart(2, '0')).join('')}`
}

/** 颜色转换工具：将十六进制颜色转换为 RGBA 颜色 */
const hexToRgba = (hex: string, alpha: number): string => {
    const rgb = parseHex(hex)
    if (!rgb) {
        return `rgba(234, 84, 85, ${alpha})`
    }
    const [r, g, b] = rgb
    return `rgba(${r}, ${g}, ${b}, ${alpha})`
}

const buildThemeVars = (config: ThemeConfig): Record<string, string> => {
    const primary = config.primary || DEFAULT_PRIMARY
    const vars: Record<string, string> = {
        '--color': primary,
        '--color-primary': primary,
        '--color-primary-hover': shadeHex(primary, -14),
        '--color-primary-soft': hexToRgba(primary, 0.08),
        '--color-price': primary,
        '--ui-primary': primary,
        '--nav-bg-color': primary,
        '--nav-text-color': '#ffffff',
        '--rgba6': hexToRgba(primary, 0.6),
        '--rgba4': hexToRgba(primary, 0.4),
        '--rgba2': hexToRgba(primary, 0.2)
    }
    if (config.background) {
        vars['--background'] = config.background
        vars['--bg-card'] = config.background
    }
    if (config.text) {
        vars['--text'] = config.text
        vars['--color-text'] = config.text
        vars['--text-primary'] = config.text
        vars['--ui-text-highlighted'] = config.text
    }
    return vars
}

/** 获取当前主题配置 */
export const useTheme = () => {
    /**
     * 生成主题样式字符串，用于 SSR 阶段注入
     * @param config 主题配置
     */
    const generateThemeStyles = (config: ThemeConfig): string => {
        const vars = buildThemeVars(config)
        const body = Object.entries(vars).map(([k, v]) => `${k}: ${v}`).join('; ')
        return `:root { ${body} }`
    }

    /**
     * 初始化主题配置
     * 在服务器端渲染时调用，确保主题一致性
     */
    const initializeTheme = (config?: Partial<ThemeConfig>) => {
        if (config) {
            themeConfig.value = { ...defaultTheme, ...config }
        }
    }

    /**
     * 设置主题变量到 CSS 根元素（客户端）
     * @param config 主题配置
     */
    const setCSSVariables = (config: ThemeConfig) => {
        if (typeof window === 'undefined') return

        const root = document.documentElement
        const vars = buildThemeVars(config)
        Object.entries(vars).forEach(([key, value]) => {
            root.style.setProperty(key, value)
        })
    }

    /**
     * 设置主题
     * @param config 主题配置
     * @param save 是否保存到本地存储，默认为 true
     */
    const setTheme = (config: Partial<ThemeConfig>, save: boolean = true) => {
        const newConfig = { ...defaultTheme, ...config }
        themeConfig.value = newConfig
        setCSSVariables(newConfig)

        if (save && typeof window !== 'undefined') {
            try {
                localStorage.setItem(THEME_STORAGE_KEY, JSON.stringify(newConfig))
            } catch (error) {
                console.error('Failed to save theme config:', error)
            }
        }
    }

    /**
     * 从后台获取主题配置并应用
     * @param themeData 后台返回的主题数据
     */
    const applyBackendTheme = (themeData: Partial<ThemeConfig>) => {
        setTheme(themeData, true)
    }

    /**
     * 重置为默认主题
     */
    const resetToDefaultTheme = () => {
        setTheme(defaultTheme, true)
    }

    /**
     * 清除本地存储的主题配置
     */
    const clearSavedTheme = () => {
        if (typeof window !== 'undefined') {
            try {
                localStorage.removeItem(THEME_STORAGE_KEY)
            } catch (error) {
                console.error('Failed to clear theme config:', error)
            }
        }
    }

    /**
     * 主题配置的计算属性
     */
    const currentTheme = computed(() => themeConfig.value)

    /**
     * 获取当前主题的主色调
     */
    const primaryColor = computed(() => themeConfig.value.primary)

    /**
     * 检查当前主题是否是默认主题
     */
    const isDefaultTheme = computed(() =>
        JSON.stringify(themeConfig.value) === JSON.stringify(defaultTheme)
    )

    return {
        /** 初始化主题配置（SSR阶段） */
        initializeTheme,
        /** 生成主题样式字符串 */
        generateThemeStyles,
        /** 设置主题变量到 CSS 根元素（客户端） */
        setCSSVariables,
        /** 设置主题 */
        setTheme,
        /** 应用后台返回的主题 */
        applyBackendTheme,
        /** 重置为默认主题 */
        resetToDefaultTheme,
        /** 清除本地存储的主题配置 */
        clearSavedTheme,
        /** 当前主题配置 */
        currentTheme,
        /** 主色调 */
        primaryColor,
        /** 是否是默认主题 */
        isDefaultTheme
    }
}
