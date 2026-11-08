/** Only the exact official entry shipped by this frontend release gets its content revision. */
export function resolvePluginFrontendUrl(plugin, revisions = {}) {
  const raw = String(plugin?.frontendUrl || '').trim()
  if (!raw) return ''
  if (/^[a-z][a-z0-9+.-]*:\/\//i.test(raw)) return raw
  const url = raw.startsWith('/') ? raw : `/${raw}`
  const revision = revisions[plugin?.code]
  if (!revision || !new RegExp(`^/web/plugin-assets/${plugin.code}/[^/]+/index\\.mjs$`).test(url.split('?')[0])) return url
  const parsed = new URL(url, 'https://host.invalid')
  parsed.searchParams.set('rev', revision)
  return parsed.pathname + parsed.search
}
