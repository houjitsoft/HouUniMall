/** GEO 管理后台品牌配置 */
export const APP_TITLE = 'HouGEO'
export const APP_TITLE_SHORT = 'HouGEO'

/** BFF API 网关域名（按部署环境） */
export const API_ORIGINS = {
  /** houjit 主站 API（116）；jar 未同步前可能滞后于 39 */
  production: 'https://geo.shangliansoft.cn',
  development: 'https://geo.shangliansoft.cn',
  /** shanglian 同机网关（39 bff-web:8391），与前端同域 /web 反代 */
  shanglian: ''
}

/**
 * 根据当前页面域名解析 BFF 根地址（运行时动态，避免生产包写死开发域）
 *
 * 规则：
 * - *.shangliansoft.cn → 同域（nginx `/web/` → 本机 bff-web:8391）
 * - *.houjit.com / localhost → geo.shangliansoft.cn
 * - VITE_APP_BASE_URL 非空时优先（仅用于特殊部署覆盖）
 */
export function resolveApiBaseUrl() {
  const override = String(import.meta.env.VITE_APP_BASE_URL || '').trim().replace(/\/$/, '')
  if (override) {
    return override
  }

  if (typeof window !== 'undefined' && window.location?.hostname) {
    const host = window.location.hostname.toLowerCase()
    if (host.includes('shangliansoft')) {
      // 空字符串 = 相对当前页面域名，走同域 /web 反代到 39 上已更新的 geo
      return API_ORIGINS.shanglian
    }
    if (host.includes('houjit') || host === 'localhost' || host === '127.0.0.1') {
      return API_ORIGINS.development
    }
  }

  return import.meta.env.PROD ? API_ORIGINS.production : API_ORIGINS.development
}

/**
 * API 根地址（运行时解析；接口路径以 /web/... 开头）
 * 示例：geo.shangliansoft.cn → POST /web/auth/smscode
 */
export const API_BASE_URL = resolveApiBaseUrl()
