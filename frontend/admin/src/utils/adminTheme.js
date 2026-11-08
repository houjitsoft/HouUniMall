/**
 * 管理后台主题：持久化 + 统一应用到 html / Element Plus / 布局 CSS 变量。
 */

export const ADMIN_THEME_STORAGE_KEY = 'admin_theme_settings'
export const ADMIN_THEME_CHANGED_EVENT = 'admin-theme-changed'

export const DEFAULT_ADMIN_THEME = Object.freeze({
  mode: 'light',
  primaryColor: '#409EFF',
  sidebarTheme: 'dark',
  showTopNav: true,
  showBreadcrumb: false,
  sidebarCollapse: false,
  pageAnimation: true,
  customCSS: ''
})

const SIDEBAR_THEMES = {
  dark: {
    bg: '#1a2332',
    bgEnd: '#243044',
    header: '#121a26',
    text: '#b8c4d4',
    active: null
  },
  light: {
    bg: '#ffffff',
    bgEnd: '#f5f7fa',
    header: '#f0f2f5',
    text: '#303133',
    active: null
  },
  blue: {
    bg: '#1d39c4',
    bgEnd: '#10239e',
    header: '#10239e',
    text: '#d6e4ff',
    active: null
  },
  green: {
    bg: '#237804',
    bgEnd: '#135200',
    header: '#135200',
    text: '#d9f7be',
    active: null
  }
}

const CUSTOM_STYLE_ID = 'admin-theme-custom-css'
let mediaQueryListener = null

/**
 * @param {unknown} value
 * @returns {string}
 */
function asHexColor(value) {
  if (typeof value !== 'string') return DEFAULT_ADMIN_THEME.primaryColor
  const trimmed = value.trim()
  if (/^#[0-9a-fA-F]{6}$/.test(trimmed)) return trimmed.toUpperCase()
  if (/^#[0-9a-fA-F]{3}$/.test(trimmed)) {
    const [, r, g, b] = trimmed
    return `#${r}${r}${g}${g}${b}${b}`.toUpperCase()
  }
  return DEFAULT_ADMIN_THEME.primaryColor
}

/**
 * @param {string} hex
 * @returns {{ r: number, g: number, b: number }}
 */
function hexToRgb(hex) {
  const normalized = asHexColor(hex).slice(1)
  return {
    r: parseInt(normalized.slice(0, 2), 16),
    g: parseInt(normalized.slice(2, 4), 16),
    b: parseInt(normalized.slice(4, 6), 16)
  }
}

/**
 * @param {{ r: number, g: number, b: number }} rgb
 * @returns {string}
 */
function rgbToHex({ r, g, b }) {
  const to = (n) => Math.max(0, Math.min(255, Math.round(n))).toString(16).padStart(2, '0')
  return `#${to(r)}${to(g)}${to(b)}`.toUpperCase()
}

/**
 * 与白色/黑色混合，生成 Element Plus 主色阶。
 * @param {string} hex
 * @param {number} weight 0~1，越大越接近 mixWith
 * @param {'white'|'black'} mixWith
 * @returns {string}
 */
export function mixColor(hex, weight, mixWith = 'white') {
  const base = hexToRgb(hex)
  const target = mixWith === 'black' ? { r: 0, g: 0, b: 0 } : { r: 255, g: 255, b: 255 }
  const w = Math.max(0, Math.min(1, Number(weight) || 0))
  return rgbToHex({
    r: base.r * (1 - w) + target.r * w,
    g: base.g * (1 - w) + target.g * w,
    b: base.b * (1 - w) + target.b * w
  })
}

/**
 * @param {Partial<typeof DEFAULT_ADMIN_THEME>|null|undefined} raw
 * @returns {typeof DEFAULT_ADMIN_THEME}
 */
export function normalizeAdminTheme(raw) {
  const src = raw && typeof raw === 'object' ? raw : {}
  const mode = ['light', 'dark', 'auto'].includes(src.mode) ? src.mode : DEFAULT_ADMIN_THEME.mode
  const sidebarTheme = Object.prototype.hasOwnProperty.call(SIDEBAR_THEMES, src.sidebarTheme)
    ? src.sidebarTheme
    : DEFAULT_ADMIN_THEME.sidebarTheme

  return {
    mode,
    primaryColor: asHexColor(src.primaryColor),
    sidebarTheme,
    showTopNav: src.showTopNav !== false,
    // 保留兼容字段，但旧版已保存的 true 不再恢复已移除的全局面包屑。
    showBreadcrumb: false,
    sidebarCollapse: src.sidebarCollapse === true,
    pageAnimation: src.pageAnimation !== false,
    customCSS: typeof src.customCSS === 'string' ? src.customCSS : ''
  }
}

/**
 * @returns {typeof DEFAULT_ADMIN_THEME}
 */
export function loadAdminTheme() {
  if (typeof localStorage === 'undefined') {
    return { ...DEFAULT_ADMIN_THEME }
  }
  try {
    const raw = localStorage.getItem(ADMIN_THEME_STORAGE_KEY)
    if (!raw) return { ...DEFAULT_ADMIN_THEME }
    return normalizeAdminTheme(JSON.parse(raw))
  } catch {
    return { ...DEFAULT_ADMIN_THEME }
  }
}

/**
 * @param {Partial<typeof DEFAULT_ADMIN_THEME>} theme
 * @returns {typeof DEFAULT_ADMIN_THEME}
 */
export function saveAdminTheme(theme) {
  const normalized = normalizeAdminTheme(theme)
  if (typeof localStorage !== 'undefined') {
    localStorage.setItem(ADMIN_THEME_STORAGE_KEY, JSON.stringify(normalized))
  }
  return normalized
}

/**
 * @param {'light'|'dark'|'auto'} mode
 * @returns {'light'|'dark'}
 */
export function resolveThemeMode(mode) {
  if (mode === 'dark') return 'dark'
  if (mode === 'light') return 'light'
  if (typeof window !== 'undefined' && window.matchMedia) {
    return window.matchMedia('(prefers-color-scheme: dark)').matches ? 'dark' : 'light'
  }
  return 'light'
}

/**
 * @param {string} css
 */
function injectCustomCss(css) {
  if (typeof document === 'undefined') return
  let el = document.getElementById(CUSTOM_STYLE_ID)
  if (!css) {
    if (el) el.remove()
    return
  }
  if (!el) {
    el = document.createElement('style')
    el.id = CUSTOM_STYLE_ID
    document.head.appendChild(el)
  }
  el.textContent = css
}

/**
 * @param {string} primary
 */
function applyPrimaryColor(primary) {
  if (typeof document === 'undefined') return
  const root = document.documentElement
  const color = asHexColor(primary)
  root.style.setProperty('--hc-primary', color)
  root.style.setProperty('--hc-primary-light', mixColor(color, 0.9, 'white'))
  root.style.setProperty('--hc-sidebar-active', color)
  root.style.setProperty('--el-color-primary', color)
  root.style.setProperty('--el-color-primary-light-3', mixColor(color, 0.3, 'white'))
  root.style.setProperty('--el-color-primary-light-5', mixColor(color, 0.5, 'white'))
  root.style.setProperty('--el-color-primary-light-7', mixColor(color, 0.7, 'white'))
  root.style.setProperty('--el-color-primary-light-8', mixColor(color, 0.8, 'white'))
  root.style.setProperty('--el-color-primary-light-9', mixColor(color, 0.9, 'white'))
  root.style.setProperty('--el-color-primary-dark-2', mixColor(color, 0.2, 'black'))
}

/**
 * @param {string} sidebarTheme
 * @param {string} primary
 */
function applySidebarTheme(sidebarTheme, primary) {
  if (typeof document === 'undefined') return
  const root = document.documentElement
  const palette = SIDEBAR_THEMES[sidebarTheme] || SIDEBAR_THEMES.dark
  const active = palette.active || asHexColor(primary)
  root.style.setProperty('--hc-sidebar-bg', palette.bg)
  root.style.setProperty('--hc-sidebar-bg-end', palette.bgEnd)
  root.style.setProperty('--hc-sidebar-header', palette.header)
  root.style.setProperty('--hc-sidebar-text', palette.text)
  root.style.setProperty('--hc-sidebar-active', active)
  root.setAttribute('data-sidebar-theme', sidebarTheme)
}

/**
 * @param {'light'|'dark'} resolvedMode
 */
function applyColorMode(resolvedMode) {
  if (typeof document === 'undefined') return
  const root = document.documentElement
  const isDark = resolvedMode === 'dark'
  root.classList.toggle('dark', isDark)
  root.setAttribute('data-theme', resolvedMode)

  if (isDark) {
    root.style.setProperty('--hc-content-bg', '#0f1419')
    root.style.setProperty('--hc-header-bg', '#1a2332')
    root.style.setProperty('--hc-text-primary', '#e5eaf3')
    root.style.setProperty('--hc-text-secondary', '#a3abb8')
    root.style.setProperty('--hc-border', '#2c3645')
    root.style.setProperty('--hc-header-chip-bg', '#1a2332')
    root.style.setProperty('--hc-header-chip-border', '#2c3645')
    root.style.setProperty('--hc-header-chip-hover-border', '#409eff')
  } else {
    root.style.setProperty('--hc-content-bg', '#f4f6f9')
    root.style.setProperty('--hc-header-bg', '#ffffff')
    root.style.setProperty('--hc-text-primary', '#303133')
    root.style.setProperty('--hc-text-secondary', '#909399')
    root.style.setProperty('--hc-border', '#e4e7ed')
    root.style.setProperty('--hc-header-chip-bg', '#ffffff')
    root.style.setProperty('--hc-header-chip-border', '#e4e7ed')
    root.style.setProperty('--hc-header-chip-hover-border', '#c6e2ff')
  }
}

/**
 * @param {typeof DEFAULT_ADMIN_THEME} theme
 */
function bindAutoModeListener(theme) {
  if (typeof window === 'undefined' || !window.matchMedia) return
  const mq = window.matchMedia('(prefers-color-scheme: dark)')
  if (mediaQueryListener) {
    mq.removeEventListener('change', mediaQueryListener)
    mediaQueryListener = null
  }
  if (theme.mode !== 'auto') return
  mediaQueryListener = () => {
    applyAdminTheme(loadAdminTheme(), { persist: false, emit: true })
  }
  mq.addEventListener('change', mediaQueryListener)
}

/**
 * 应用主题到整站后台（颜色、侧栏、深浅色、自定义 CSS）。
 * @param {Partial<typeof DEFAULT_ADMIN_THEME>} [themeInput]
 * @param {{ persist?: boolean, emit?: boolean }} [options]
 * @returns {typeof DEFAULT_ADMIN_THEME}
 */
export function applyAdminTheme(themeInput, options = {}) {
  const { persist = false, emit = true } = options
  const theme = normalizeAdminTheme(themeInput)
  if (persist) {
    saveAdminTheme(theme)
  }

  const resolvedMode = resolveThemeMode(theme.mode)
  applyColorMode(resolvedMode)
  applyPrimaryColor(theme.primaryColor)
  applySidebarTheme(theme.sidebarTheme, theme.primaryColor)
  injectCustomCss(theme.customCSS)
  bindAutoModeListener(theme)

  if (typeof document !== 'undefined') {
    document.documentElement.setAttribute('data-page-animation', theme.pageAnimation ? 'on' : 'off')
    document.documentElement.setAttribute('data-show-top-nav', theme.showTopNav ? 'on' : 'off')
    document.documentElement.setAttribute('data-show-breadcrumb', theme.showBreadcrumb ? 'on' : 'off')
  }

  if (emit && typeof window !== 'undefined') {
    window.dispatchEvent(new CustomEvent(ADMIN_THEME_CHANGED_EVENT, { detail: { ...theme } }))
  }

  return theme
}

/**
 * 启动时从本地恢复主题。
 * @returns {typeof DEFAULT_ADMIN_THEME}
 */
export function initAdminTheme() {
  return applyAdminTheme(loadAdminTheme(), { persist: false, emit: false })
}
