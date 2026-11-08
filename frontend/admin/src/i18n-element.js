// Element Plus 国际化系统 - 完全按照官方文档方式
// 参考文档: https://element-plus.org/zh-CN/guide/i18n.html
import { ref } from 'vue'
import zhCn from 'element-plus/dist/locale/zh-cn.mjs'
import zhTw from 'element-plus/dist/locale/zh-tw.mjs'
import en from 'element-plus/dist/locale/en.mjs'
import fr from 'element-plus/dist/locale/fr.mjs'
import de from 'element-plus/dist/locale/de.mjs'
import ja from 'element-plus/dist/locale/ja.mjs'
import ko from 'element-plus/dist/locale/ko.mjs'
import ms from 'element-plus/dist/locale/ms.mjs'
import es from 'element-plus/dist/locale/es.mjs'
import ru from 'element-plus/dist/locale/ru.mjs'
import ar from 'element-plus/dist/locale/ar.mjs'
import { normalizeLanguageCode } from '@/locales/adminLanguageCatalog'

// Element Plus 标准语言包（保持官方格式，不添加自定义属性）
export const elementPlusLocales = {
  'zh-CN': zhCn,
  'zh': zhCn,
  'zh-TW': zhTw,
  'en': en,
  'fr': fr,
  'de': de,
  'ja': ja,
  'ko': ko,
  'ms': ms,
  'es': es,
  'ru': ru,
  'ar': ar
}

// 业务文本语言包（独立管理，不混入 Element Plus locale 对象）
const modules = import.meta.glob('./locales/**/*.json', { eager: true, import: 'default' })
const businessMessages = {}
const pluginMessages = {}

Object.keys(modules).forEach(key => {
  const pathParts = key.split('/')
  const lang = pathParts[2]
  const modulePath = pathParts.slice(3)
  const fileName = modulePath[modulePath.length - 1].replace('.json', '')
  const moduleParts = [...modulePath.slice(0, -1), fileName]
  
  if (!businessMessages[lang]) {
    businessMessages[lang] = {}
  }
  
  const moduleContent = modules[key]
  let current = businessMessages[lang]
  for (let i = 0; i < moduleParts.length; i++) {
    const part = moduleParts[i]
    if (i === moduleParts.length - 1) {
      // plugin.json historically contained an extra top-level `plugin` object;
      // normalize it here so keys remain `plugin.{code}.*` like runtime bundles.
      current[part] = fileName === 'plugin' && moduleContent?.plugin && typeof moduleContent.plugin === 'object'
        ? moduleContent.plugin
        : moduleContent
    } else {
      if (!current[part]) {
        current[part] = {}
      }
      current = current[part]
    }
  }
})

// 确保 zh 和 zh-CN 都指向同一个对象
if (businessMessages['zh-CN']) {
  businessMessages['zh'] = businessMessages['zh-CN']
}

// 获取初始语言
const getInitialLocale = () => {
  const storage = typeof globalThis.localStorage !== 'undefined' ? globalThis.localStorage : null
  const savedLocale = normalizeLanguageCode(storage?.getItem('language'))
  if (savedLocale && elementPlusLocales[savedLocale]) {
    return savedLocale
  }

  const browserLocale = globalThis.navigator?.language || globalThis.navigator?.userLanguage || 'zh-CN'
  const normalizedBrowser = normalizeLanguageCode(browserLocale)
  if (normalizedBrowser && elementPlusLocales[normalizedBrowser]) {
    return normalizedBrowser
  }

  return 'zh-CN'
}

// 当前语言（Vue ref，响应式）
const currentLocale = ref(getInitialLocale())

// 业务文本翻译函数（独立于 Element Plus locale）
// 注意：此函数本身不是响应式的，请在组件中使用 useI18n() composable 获取响应式 t 函数
export const t = (key, locale = currentLocale.value) => {
  const lookup = (messages) => {
    const keys = String(key || '').split('.')
    let result = messages
    for (const k of keys) {
      if (result && typeof result === 'object' && Object.prototype.hasOwnProperty.call(result, k)) result = result[k]
      else return undefined
    }
    return typeof result === 'string' ? result : undefined
  }
  // 插件运行时语言包优先于宿主内置兼容文案，允许插件独立升级翻译。
  return lookup(pluginMessages[locale])
    || lookup(businessMessages[locale])
    || lookup(pluginMessages['zh-CN'])
    || lookup(businessMessages['zh-CN'])
    || key
}

const mergeMessages = (target, source) => {
  if (!source || typeof source !== 'object') return target
  Object.entries(source).forEach(([key, value]) => {
    if (value && typeof value === 'object' && !Array.isArray(value)) {
      target[key] = mergeMessages(target[key] && typeof target[key] === 'object' ? target[key] : {}, value)
    } else if (value != null) {
      target[key] = value
    }
  })
  return target
}

/** 注册插件语言包。语言包归宿主管理，插件只提供命名空间内的 messages。 */
export const registerPluginMessages = (code, messages) => {
  const pluginCode = String(code || '').trim().toLowerCase()
  if (!pluginCode || !messages || typeof messages !== 'object') return false
  Object.entries(messages).forEach(([locale, values]) => {
    const normalizedLocale = locale === 'zh' ? 'zh-CN' : locale
    pluginMessages[normalizedLocale] = pluginMessages[normalizedLocale] || {}
    pluginMessages[normalizedLocale].plugin = pluginMessages[normalizedLocale].plugin || {}
    pluginMessages[normalizedLocale].plugin[pluginCode] = mergeMessages(
      pluginMessages[normalizedLocale].plugin[pluginCode] || {}, values
    )
  })
  if (typeof window !== 'undefined') window.dispatchEvent(new CustomEvent('plugin-i18n-changed', { detail: pluginCode }))
  return true
}

export const unregisterPluginMessages = (code) => {
  const pluginCode = String(code || '').trim().toLowerCase()
  if (!pluginCode) return false
  Object.values(pluginMessages).forEach((messages) => {
    if (messages?.plugin) delete messages.plugin[pluginCode]
  })
  if (typeof window !== 'undefined') window.dispatchEvent(new CustomEvent('plugin-i18n-changed', { detail: pluginCode }))
  return true
}

export const createPluginI18n = (code) => {
  const pluginCode = String(code || '').trim().toLowerCase()
  const translate = (key) => t(`plugin.${pluginCode}.${String(key || '').replace(/^\.+/, '')}`)
  const subscribe = (handler) => {
    if (typeof window === 'undefined' || typeof handler !== 'function') return () => {}
    const listener = () => handler(currentLocale.value)
    window.addEventListener('language-changed', listener)
    window.addEventListener('plugin-i18n-changed', listener)
    return () => {
      window.removeEventListener('language-changed', listener)
      window.removeEventListener('plugin-i18n-changed', listener)
    }
  }
  return { locale: currentLocale, t: translate, subscribe }
}

// 设置语言
export const setLocale = (locale) => {
  if (elementPlusLocales[locale] && currentLocale.value !== locale) {
    currentLocale.value = locale
    if (typeof globalThis.localStorage !== 'undefined') globalThis.localStorage.setItem('language', locale)
    if (typeof window !== 'undefined') window.dispatchEvent(new CustomEvent('language-changed', { detail: locale }))
    return true
  }
  return false
}

if (typeof window !== 'undefined') {
  window.addEventListener('language-changed', (event) => {
    const next = event.detail
    if (elementPlusLocales[next] && currentLocale.value !== next) currentLocale.value = next
  })
}

// 获取当前语言
export const getLocale = () => currentLocale.value

/**
 * 响应式 i18n composable
 * 返回可直接调用的 t(key)，在 computed/模板中读取 currentLocale 实现语言切换响应。
 * @returns {{ t: (key: string) => string, locale: import('vue').Ref<string> }}
 */
export const useI18n = () => {
  const translate = (key) => t(key, currentLocale.value)
  return {
    t: translate,
    locale: currentLocale
  }
}

// 获取 Element Plus locale（完全按照官方文档方式，不添加自定义属性）
export const getElementPlusLocale = (locale = currentLocale.value) => {
  return elementPlusLocales[locale] || elementPlusLocales['zh-CN']
}

// 获取可用语言列表
export const getAvailableLocales = () => Object.keys(elementPlusLocales)

// 获取业务文本消息（用于 tm 函数等）
export const getBusinessMessages = (locale = currentLocale.value) => {
  return businessMessages[locale] || businessMessages['zh-CN'] || {}
}

export default {
  t,
  setLocale,
  getLocale,
  getElementPlusLocale,
  getAvailableLocales,
  getBusinessMessages,
  useI18n,
  registerPluginMessages,
  unregisterPluginMessages,
  createPluginI18n,
  elementPlusLocales,
  businessMessages: businessMessages
}
