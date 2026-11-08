import { normalizeWebsiteUrl, resolveProjectWebsiteUrl } from './technicalProjectUrl.js'

function normalizePageUrl(value, websiteUrl) {
  const raw = String(value || '').trim()
  if (!raw) return ''
  try {
    const url = /^https?:\/\//i.test(raw)
      ? new URL(raw)
      : new URL(raw.startsWith('/') ? raw : `/${raw}`, `${websiteUrl}/`)
    url.hash = ''
    const normalized = url.toString()
    return normalized.endsWith('/') ? normalized.slice(0, -1) : normalized
  } catch {
    return normalizeWebsiteUrl(raw).replace(/\/$/, '')
  }
}

export function buildTechnicalPageOptions({ projectId, projects = [], pageResponse = {}, additionalUrls = [] }) {
  const websiteUrl = resolveProjectWebsiteUrl(projectId, projects).replace(/\/$/, '')
  const options = new Map()
  if (websiteUrl) options.set(websiteUrl, { label: '项目官网', value: websiteUrl })

  const pages = Array.isArray(pageResponse) ? pageResponse : (pageResponse.list || pageResponse.records || [])
  for (const page of pages) {
    const value = normalizePageUrl(page?.url || page?.pageUrl, websiteUrl)
    if (value && !options.has(value)) {
      options.set(value, { label: page?.title || page?.name || value, value })
    }
  }
  for (const item of additionalUrls) {
    const raw = typeof item === 'string' ? item : (item?.url || item?.pageUrl)
    const value = normalizePageUrl(raw, websiteUrl)
    if (value && !options.has(value)) {
      options.set(value, { label: typeof item === 'string' ? value : (item?.title || item?.name || value), value })
    }
  }
  return [...options.values()]
}
