import request from '@/utils/request'

export const SEO_CONFIG_KEY = 'cloud_seo_settings'

const DEFAULT_SEO = {
  title: 'UniMall 管理系统',
  keywords: '管理系统,企业应用,业务管理',
  description: 'UniMall 是一个功能强大的企业级管理系统，提供完整的业务管理解决方案。',
  logo: '',
  favicon: '',
  robots: `User-agent: *
Allow: /
Disallow: /admin/
Disallow: /api/`,
  sitemapEnabled: true,
  canonical: '',
  ogImage: ''
}

function parseConfigValue(raw) {
  if (!raw) return null
  if (typeof raw === 'object') return raw
  try {
    return JSON.parse(raw)
  } catch {
    return null
  }
}

export const seoSettingApi = {
  getDefaults: () => ({ ...DEFAULT_SEO }),

  async load() {
    try {
      const res = await request.get('/web/base/sys-config/config-key', {
        params: { configKey: SEO_CONFIG_KEY }
      })
      if (res?.code === 1 && res.data) {
        const value = res.data.configValue ?? res.data.config_value ?? res.data
        const parsed = parseConfigValue(value)
        if (parsed) {
          return { ...DEFAULT_SEO, ...parsed }
        }
      }
    } catch (e) {
      console.warn('[seoSettingApi] load remote failed', e)
    }

    try {
      const cached = localStorage.getItem(SEO_CONFIG_KEY)
      if (cached) {
        return { ...DEFAULT_SEO, ...JSON.parse(cached) }
      }
    } catch {
      /* ignore */
    }
    return { ...DEFAULT_SEO }
  },

  async save(payload) {
    localStorage.setItem(SEO_CONFIG_KEY, JSON.stringify(payload))

    try {
      const res = await request.post('/web/base/sys-config/join-setting/update', {
        configKey: SEO_CONFIG_KEY,
        configValue: JSON.stringify(payload),
        configEnable: '1'
      })
      if (res?.code === 1) {
        return res
      }
    } catch (e) {
      console.warn('[seoSettingApi] remote save failed', e)
    }

    return { code: 1, message: '已保存到本地' }
  }
}
