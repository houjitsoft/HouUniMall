
function getConfigBaseUrl() {
  return window.location.origin || ''
}

function removeTrailingSlash(str) {
  return str && str.endsWith('/') ? str.slice(0, -1) : str
}

function getAllowedHosts() {
  return [
    removeTrailingSlash(getConfigBaseUrl()),
    'http://localhost:9531',
  ]
}

/**
 * 安全地打开新页面
 * @param {string} path - 页面路径
 * @param {Object} queryParams - 查询参数
 * @param {string} target - 打开方式，默认 '_blank'
 * @param {string|null} baseUrl - 可选 base URL
 */
export function safeOpenUrl(
  path,
  queryParams = {},
  target = '_blank',
  baseUrl = null
) {
  try {
    let finalBaseUrl = baseUrl

    if (!finalBaseUrl) {
      const allowedHosts = getAllowedHosts()
      if (!allowedHosts.includes(window.location.origin)) {
        console.warn(
          'Blocked: Current origin is not in allowed hosts',
          window.location.origin
        )
        return false
      }
      finalBaseUrl = window.location.origin
    }

    const url = new URL(path, finalBaseUrl)

    Object.keys(queryParams).forEach((key) => {
      const value = queryParams[key]
      if (value !== null && value !== undefined) {
        url.searchParams.append(key, String(value))
      }
    })

    if (!['http:', 'https:'].includes(url.protocol)) {
      console.warn('Blocked: Invalid protocol', url.protocol)
      return false
    }
    if (url.href.includes('/origin')) {
      url.href = url.href.replace('/origin', '/#')
    }
    window.open(url.href, target, 'noopener,noreferrer')
    return true
  } catch (error) {
    console.error('Invalid URL or open failed:', error)
    return false
  }
}
