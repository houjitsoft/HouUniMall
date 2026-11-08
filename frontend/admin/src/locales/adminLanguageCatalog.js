/**
 * 后台可选语言的唯一清单。
 * 网站设置里的「支持的语言」和右上角语言菜单都读这份清单，勾选结果决定菜单里出现哪些语言。
 */

export const ADMIN_LANGUAGE_CONFIG_EVENT = 'admin-language-config-changed'
export const ADMIN_LANGUAGE_CONFIG_STORAGE_KEY = 'adminLanguageConfig'

export const ADMIN_LANGUAGES = Object.freeze([
  { value: 'zh-CN', label: '简体中文' },
  { value: 'zh-TW', label: '繁体中文' },
  { value: 'en', label: 'English' },
  { value: 'ja', label: '日本語' },
  { value: 'ko', label: '한국어' },
  { value: 'fr', label: 'Français' },
  { value: 'de', label: 'Deutsch' },
  { value: 'es', label: 'Español' },
  { value: 'ru', label: 'Русский' },
  { value: 'ar', label: 'العربية' },
  { value: 'ms', label: 'Bahasa Melayu' }
])

export const ADMIN_LANGUAGE_CODES = Object.freeze(ADMIN_LANGUAGES.map((item) => item.value))

const DEFAULT_SUPPORTED = Object.freeze(['zh-CN', 'en'])

const ALIASES = Object.freeze({
  zh: 'zh-CN',
  'zh-cn': 'zh-CN',
  'zh-hans': 'zh-CN',
  'zh-tw': 'zh-TW',
  'zh-hk': 'zh-TW',
  'zh-hant': 'zh-TW',
  en: 'en',
  'en-us': 'en',
  'en-gb': 'en',
  ja: 'ja',
  'ja-jp': 'ja',
  ko: 'ko',
  'ko-kr': 'ko',
  fr: 'fr',
  'fr-fr': 'fr',
  de: 'de',
  'de-de': 'de',
  es: 'es',
  'es-es': 'es',
  'es-mx': 'es',
  ru: 'ru',
  'ru-ru': 'ru',
  ar: 'ar',
  'ar-sa': 'ar',
  ms: 'ms',
  'ms-my': 'ms'
})

export function normalizeLanguageCode(value) {
  const key = String(value || '').trim().toLowerCase().replaceAll('_', '-')
  if (!key) return null
  if (ALIASES[key]) return ALIASES[key]
  if (key.startsWith('zh-tw') || key.startsWith('zh-hk') || key.startsWith('zh-hant')) return 'zh-TW'
  if (key === 'zh' || key.startsWith('zh-')) return 'zh-CN'
  return ALIASES[key.split('-')[0]] || null
}

function asBoolean(value, fallback) {
  if (value === true || value === false) return value
  if (value == null || value === '') return fallback
  const text = String(value).trim().toLowerCase()
  if (text === '1' || text === 'true' || text === 'yes') return true
  if (text === '0' || text === 'false' || text === 'no') return false
  return fallback
}

function orderedSupported(values, { useDefaultWhenEmpty }) {
  const picked = []
  const source = Array.isArray(values) ? values : (useDefaultWhenEmpty ? DEFAULT_SUPPORTED : [])
  source.forEach((item) => {
    const code = normalizeLanguageCode(item)
    if (code && !picked.includes(code)) picked.push(code)
  })
  if (!picked.length && useDefaultWhenEmpty) {
    DEFAULT_SUPPORTED.forEach((code) => picked.push(code))
  }
  return ADMIN_LANGUAGE_CODES.filter((code) => picked.includes(code))
}

export function defaultAdminLanguageConfig() {
  return {
    systemLanguage: 'zh-CN',
    defaultLanguage: 'zh-CN',
    multiLanguage: true,
    autoDetect: true,
    supportedLanguages: [...DEFAULT_SUPPORTED],
    rtlSupport: false
  }
}

/**
 * 把接口或表单数据收成可保存的配置。
 * 显式传入的支持语言一个都对不上时返回 null，避免把右上角菜单清空。
 */
export function normalizeAdminLanguageConfig(raw, { useDefaultWhenEmpty = true } = {}) {
  const source = raw && typeof raw === 'object' ? raw : {}
  const hasSupportedField = Object.prototype.hasOwnProperty.call(source, 'supportedLanguages')
  const supportedLanguages = orderedSupported(source.supportedLanguages, {
    useDefaultWhenEmpty: hasSupportedField ? false : useDefaultWhenEmpty
  })
  if (!supportedLanguages.length) {
    if (!useDefaultWhenEmpty || hasSupportedField) return null
    return defaultAdminLanguageConfig()
  }

  const systemLanguage = supportedLanguages.includes(normalizeLanguageCode(source.systemLanguage))
    ? normalizeLanguageCode(source.systemLanguage)
    : supportedLanguages[0]
  const defaultLanguage = supportedLanguages.includes(normalizeLanguageCode(source.defaultLanguage))
    ? normalizeLanguageCode(source.defaultLanguage)
    : supportedLanguages[0]

  return {
    systemLanguage,
    defaultLanguage,
    multiLanguage: asBoolean(source.multiLanguage, true),
    autoDetect: asBoolean(source.autoDetect, true),
    supportedLanguages,
    rtlSupport: asBoolean(source.rtlSupport, false)
  }
}

/** 右上角实际展示的语言。关闭多语言时只保留默认语言。 */
export function visibleAdminLanguages(config) {
  const normalized = normalizeAdminLanguageConfig(config) || defaultAdminLanguageConfig()
  const allowed = normalized.multiLanguage
    ? normalized.supportedLanguages
    : [normalized.defaultLanguage]
  return ADMIN_LANGUAGES.filter((item) => allowed.includes(item.value))
}

export function resolveAdminLocale({ config, storedLocale, browserLocales = [] } = {}) {
  const visible = visibleAdminLanguages(config).map((item) => item.value)
  const stored = normalizeLanguageCode(storedLocale)
  if (stored && visible.includes(stored)) return stored

  const normalized = normalizeAdminLanguageConfig(config) || defaultAdminLanguageConfig()
  if (normalized.autoDetect) {
    for (const candidate of browserLocales) {
      const code = normalizeLanguageCode(candidate)
      if (code && visible.includes(code)) return code
    }
  }
  if (visible.includes(normalized.defaultLanguage)) return normalized.defaultLanguage
  return visible[0] || 'zh-CN'
}

export function readCachedAdminLanguageConfig(storage) {
  try {
    const target = storage === undefined ? globalThis.localStorage : storage
    const raw = target?.getItem(ADMIN_LANGUAGE_CONFIG_STORAGE_KEY)
    if (!raw) return defaultAdminLanguageConfig()
    return normalizeAdminLanguageConfig(JSON.parse(raw)) || defaultAdminLanguageConfig()
  } catch {
    return defaultAdminLanguageConfig()
  }
}

export function writeCachedAdminLanguageConfig(config, storage) {
  const normalized = normalizeAdminLanguageConfig(config)
  if (!normalized) return false
  try {
    const target = storage === undefined ? globalThis.localStorage : storage
    target?.setItem(ADMIN_LANGUAGE_CONFIG_STORAGE_KEY, JSON.stringify(normalized))
    return true
  } catch {
    return false
  }
}

export function publishAdminLanguageConfig(config) {
  const normalized = normalizeAdminLanguageConfig(config)
  if (!normalized || typeof globalThis.window === 'undefined') return null
  writeCachedAdminLanguageConfig(normalized)
  globalThis.window.dispatchEvent(new CustomEvent(ADMIN_LANGUAGE_CONFIG_EVENT, { detail: normalized }))
  return normalized
}

export function applyReadingDirection(config, locale, documentRef = globalThis.document) {
  const normalized = normalizeAdminLanguageConfig(config) || defaultAdminLanguageConfig()
  const rtl = normalized.rtlSupport === true && normalizeLanguageCode(locale) === 'ar'
  if (!documentRef?.documentElement) return false
  documentRef.documentElement.setAttribute('dir', rtl ? 'rtl' : 'ltr')
  return true
}
