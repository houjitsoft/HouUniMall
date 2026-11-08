import { createI18n } from 'vue-i18n'
import zhCN from './zh-CN.js'
import en from './en.js'
import ja from './ja.js'
import es from './es.js'
import {
  applyDocumentLocale,
  normalizeLocale,
  persistLocale,
  readStoredLocale,
  resolveInitialLocale,
  SUPPORTED_LOCALES
} from './localePreference.js'
import { buildDirMessages, mergeDeep } from './dirMessages.js'
import { ADMIN_LANGUAGES } from './adminLanguageCatalog.js'

export const LOCALE_OPTIONS = ADMIN_LANGUAGES

const browserLocales = globalThis.navigator?.languages?.length
  ? globalThis.navigator.languages
  : [globalThis.navigator?.language].filter(Boolean)

const initialLocale = resolveInitialLocale({
  storedLocale: readStoredLocale(),
  browserLocales
})

// 目录型 JSON（auth.login / dashboard / actions / menu / plugin …）+ 顶层 js 包（common / header /
// project / videoStudio …）合并。顶层 js 后写，遇到同名 key 以它为准，目录 JSON 只补空缺，
// 这样既有文案不受影响，历史遗漏的 key 能补上。
const bundledMessages = {
  'zh-CN': zhCN,
  en,
  ja,
  es
}
const dirMessages = buildDirMessages()
const messages = {}
// fr / de / ms 没有对应的顶层 js 包，只吃目录 JSON，缺的 key 由 fallbackLocale 补中文。
SUPPORTED_LOCALES.forEach((locale) => {
  messages[locale] = mergeDeep(dirMessages[locale] || {}, bundledMessages[locale] || {})
})

export const i18n = createI18n({
  legacy: false,
  locale: initialLocale,
  fallbackLocale: 'zh-CN',
  messages
})

applyDocumentLocale(initialLocale)

export function setLocale(value) {
  const locale = normalizeLocale(value)
  if (!locale) return false

  i18n.global.locale.value = locale
  persistLocale(locale)
  applyDocumentLocale(locale)
  if (typeof globalThis.window !== 'undefined') {
    globalThis.window.localStorage?.setItem('language', locale)
    globalThis.window.dispatchEvent(new CustomEvent('language-changed', { detail: locale }))
  }
  return true
}
