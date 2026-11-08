import { safeOpenUrl as openUrl } from '@/utils/safeOpenUrl'

/**
 * 入驻流程使用：站点根地址（bcenter 生产为同源部署）
 */
export function useBaseUrl() {
  return window.location.origin || ''
}

export function useAppConfig() {
  const baseUrl = useBaseUrl()
  const safeOpenUrl = (path, queryParams, target) =>
    openUrl(path, queryParams, target, baseUrl)
  return { baseUrl, safeOpenUrl }
}
