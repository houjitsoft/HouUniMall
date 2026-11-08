/**
 * 站点实例标识工具。
 *
 * 插件授权以「绑定实例」（instanceId）作为站点隔离依据：授权管理页只能看到绑定到本实例的授权，
 * 解绑时也只允许解绑属于本实例的授权码。因此该标识必须由域名统一派生，不能由人工填写——
 * 手填会让授权绑到别的站点、或让本站点查不到自己的授权。
 */
import { computed } from 'vue'

/**
 * 当前站点实例标识（域名，不含端口）。
 * 与后端 hou_market_plugin_license.instance_id 对应。
 */
export const getSiteInstanceId = () => {
  if (typeof window === 'undefined') {
    return ''
  }
  return window.location.hostname || ''
}

/**
 * 响应式版本，供模板直接展示。
 */
export const useSiteInstanceId = () => computed(getSiteInstanceId)
