type SitemapUrl = {
  loc: string
  lastmod?: string
  changefreq?: 'always' | 'hourly' | 'daily' | 'weekly' | 'monthly' | 'yearly' | 'never'
  priority?: number
}

const xmlEscape = (s: string) =>
  s.replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;').replace(/"/g, '&quot;').replace(/'/g, '&apos;')

export default defineEventHandler((event) => {
  const { public: pub } = useRuntimeConfig(event)
  const siteUrl = String(pub.siteUrl || '').replace(/\/+$/, '')
  const appEnv = String(pub.appEnv || '').toLowerCase()
  const isProd = appEnv === 'production'

  // 非生产环境不输出 sitemap，避免误收录（robots.txt 也会 Disallow）
  if (!isProd || !siteUrl) {
    setHeader(event, 'content-type', 'application/xml; charset=utf-8')
    return `<?xml version="1.0" encoding="UTF-8"?>\n<urlset xmlns="http://www.sitemaps.org/schemas/sitemap/0.9"></urlset>\n`
  }

  const now = new Date().toISOString()
  const urls: SitemapUrl[] = ([
    { loc: `${siteUrl}/`, changefreq: 'daily', priority: 1.0 },
    { loc: `${siteUrl}/article`, changefreq: 'daily', priority: 0.7 },
    { loc: `${siteUrl}/feature`, changefreq: 'weekly', priority: 0.6 },
    { loc: `${siteUrl}/product/list`, changefreq: 'daily', priority: 0.8 },
    { loc: `${siteUrl}/points`, changefreq: 'weekly', priority: 0.5 },
    { loc: `${siteUrl}/information`, changefreq: 'daily', priority: 0.7 },
    { loc: `${siteUrl}/store/list`, changefreq: 'weekly', priority: 0.4 }
  ] satisfies SitemapUrl[]).map((u) => ({ ...u, lastmod: now }))

  const body =
    `<?xml version="1.0" encoding="UTF-8"?>\n` +
    `<urlset xmlns="http://www.sitemaps.org/schemas/sitemap/0.9">\n` +
    urls
      .map((u) => {
        const parts: string[] = []
        parts.push('  <url>')
        parts.push(`    <loc>${xmlEscape(u.loc)}</loc>`)
        if (u.lastmod) parts.push(`    <lastmod>${xmlEscape(u.lastmod)}</lastmod>`)
        if (u.changefreq) parts.push(`    <changefreq>${u.changefreq}</changefreq>`)
        if (typeof u.priority === 'number') parts.push(`    <priority>${u.priority.toFixed(1)}</priority>`)
        parts.push('  </url>')
        return parts.join('\n')
      })
      .join('\n') +
    `\n</urlset>\n`

  setHeader(event, 'content-type', 'application/xml; charset=utf-8')
  return body
})

