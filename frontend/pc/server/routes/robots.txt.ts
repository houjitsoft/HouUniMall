export default defineEventHandler((event) => {
  const { public: pub } = useRuntimeConfig(event)
  const siteUrl = String(pub.siteUrl || '').replace(/\/+$/, '')
  const appEnv = String(pub.appEnv || '').toLowerCase()
  const isProd = appEnv === 'production'

  const lines: string[] = []
  lines.push('User-agent: *')

  if (isProd) {
    lines.push('Allow: /')
    if (siteUrl) lines.push(`Sitemap: ${siteUrl}/sitemap.xml`)
  } else {
    // 避免测试/预发被搜索引擎收录
    lines.push('Disallow: /')
  }

  setHeader(event, 'content-type', 'text/plain; charset=utf-8')
  return lines.join('\n') + '\n'
})

