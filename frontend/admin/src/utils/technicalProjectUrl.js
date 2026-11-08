export function normalizeWebsiteUrl(value) {
  const raw = String(value || '').trim()
  if (!raw) return ''
  if (/^https?:\/\//i.test(raw)) return raw
  return `https://${raw.replace(/^\/+/, '')}`
}

export function resolveProjectWebsiteUrl(projectId, projects = []) {
  const project = projects.find(item => String(item?.id) === String(projectId))
  if (!project) return ''
  return normalizeWebsiteUrl(
    project.domain || project.websiteUrl || project.siteUrl || project.url,
  )
}

