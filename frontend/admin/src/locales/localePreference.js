import { ADMIN_LANGUAGE_CODES, normalizeLanguageCode } from './adminLanguageCatalog.js'

export const LOCALE_STORAGE_KEY = 'hougeo_locale'
export const DEFAULT_LOCALE = 'zh-CN'
// 与 i18n-element.js 的 elementPlusLocales、网站设置里的语言清单保持同一套编码。
export const SUPPORTED_LOCALES = ADMIN_LANGUAGE_CODES

export function normalizeLocale(value) {
  return normalizeLanguageCode(value)
}

export function resolveInitialLocale({ storedLocale, browserLocales = [] } = {}) {
  const saved = normalizeLocale(storedLocale)
  if (saved) return saved

  for (const candidate of browserLocales) {
    const locale = normalizeLocale(candidate)
    if (locale) return locale
  }
  return DEFAULT_LOCALE
}

export function readStoredLocale(storage) {
  try {
    const target = storage === undefined ? globalThis.localStorage : storage
    return target?.getItem(LOCALE_STORAGE_KEY) || null
  } catch {
    return null
  }
}

export function persistLocale(locale, storage) {
  const normalized = normalizeLocale(locale)
  if (!normalized) return false

  try {
    const target = storage === undefined ? globalThis.localStorage : storage
    target?.setItem(LOCALE_STORAGE_KEY, normalized)
    return true
  } catch {
    return false
  }
}

export function applyDocumentLocale(locale, documentRef = globalThis.document) {
  const normalized = normalizeLocale(locale)
  if (!normalized || !documentRef?.documentElement) return false

  try {
    documentRef.documentElement.setAttribute('lang', normalized)
    return true
  } catch {
    return false
  }
}
