import { computed, onMounted, onUnmounted, ref } from 'vue'
import { adminLanguageApi } from '@/api/setting/adminLanguage'
import { getLocale, setLocale } from '@/i18n-element'
import {
  ADMIN_LANGUAGE_CONFIG_EVENT,
  ADMIN_LANGUAGE_CONFIG_STORAGE_KEY,
  applyReadingDirection,
  normalizeAdminLanguageConfig,
  readCachedAdminLanguageConfig,
  resolveAdminLocale,
  visibleAdminLanguages
} from '@/locales/adminLanguageCatalog'

function browserLocales() {
  const languages = globalThis.navigator?.languages
  if (languages?.length) return languages
  return [globalThis.navigator?.language].filter(Boolean)
}

/**
 * 右上角语言菜单：只展示网站设置里勾选并保存的语言。
 */
export function useAdminLanguageMenu() {
  const config = ref(readCachedAdminLanguageConfig())
  const languages = computed(() => visibleAdminLanguages(config.value))

  function applyConfig(next, { reconcileLocale = true, persist = true } = {}) {
    const normalized = normalizeAdminLanguageConfig(next)
    if (!normalized) return
    config.value = normalized
    if (persist) {
      try {
        globalThis.localStorage?.setItem(ADMIN_LANGUAGE_CONFIG_STORAGE_KEY, JSON.stringify(normalized))
      } catch {
        // 本地缓存失败不影响当前页面按接口结果展示
      }
    }
    const resolved = resolveAdminLocale({
      config: normalized,
      storedLocale: getLocale(),
      browserLocales: browserLocales()
    })
    // 用户手动切换的语言不应被后台语言配置拉回中文：只有当前语言根本不在
    // 可见语言清单里（配置变更/首次加载）才按配置回拉。
    const visible = visibleAdminLanguages(normalized).map((lang) => lang.value)
    if (reconcileLocale && resolved && resolved !== getLocale() && !visible.includes(getLocale())) {
      setLocale(resolved)
    }
    applyReadingDirection(normalized, resolved || getLocale())
  }

  async function reload() {
    try {
      const res = await adminLanguageApi.getAdminLanguage()
      if (res?.code === 1 && res.data) {
        applyConfig(res.data)
      }
    } catch {
      applyConfig(config.value, { persist: false })
    }
  }

  function onConfigChanged(event) {
    if (event?.detail) applyConfig(event.detail, { persist: false })
  }

  function onStorage(event) {
    if (event.key !== ADMIN_LANGUAGE_CONFIG_STORAGE_KEY || !event.newValue) return
    try {
      applyConfig(JSON.parse(event.newValue), { persist: false })
    } catch {
      // 忽略损坏的本地缓存
    }
  }

  function onLanguageChanged() {
    applyReadingDirection(config.value, getLocale())
  }

  onMounted(() => {
    window.addEventListener(ADMIN_LANGUAGE_CONFIG_EVENT, onConfigChanged)
    window.addEventListener('storage', onStorage)
    window.addEventListener('language-changed', onLanguageChanged)
    reload()
  })

  onUnmounted(() => {
    window.removeEventListener(ADMIN_LANGUAGE_CONFIG_EVENT, onConfigChanged)
    window.removeEventListener('storage', onStorage)
    window.removeEventListener('language-changed', onLanguageChanged)
  })

  return { config, languages, reload }
}
