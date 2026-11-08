/** OAuth 弹窗授权：父页与回调页通信用 */
export const GEO_OAUTH_MESSAGE = 'geo-oauth-done'

/**
 * 新窗口打开 OAuth 授权页
 * @returns {Window|null} 弹窗引用；被拦截时返回 null
 */
export function openOAuthPopup(url, platform) {
  if (!url) return null
  const name = `geo_oauth_${platform || 'platform'}`
  const width = 520
  const height = 680
  const left = Math.max(0, Math.round((window.screen.width - width) / 2))
  const top = Math.max(0, Math.round((window.screen.height - height) / 2))
  const features = `width=${width},height=${height},left=${left},top=${top},scrollbars=yes,resizable=yes`
  const popup = window.open(url, name, features)
  popup?.focus?.()
  return popup
}

/** 监听 OAuth 弹窗完成（授权成功/失败） */
export function listenOAuthPopup(onDone) {
  const handler = (event) => {
    if (event.origin !== window.location.origin) return
    const data = event.data
    if (!data || data.type !== GEO_OAUTH_MESSAGE) return
    onDone(data)
  }
  window.addEventListener('message', handler)
  return () => window.removeEventListener('message', handler)
}

/** 回调页通知 opener 并尝试关闭弹窗 */
export function notifyOAuthOpener(payload) {
  if (window.opener && !window.opener.closed) {
    window.opener.postMessage({ type: GEO_OAUTH_MESSAGE, ...payload }, window.location.origin)
  }
}
