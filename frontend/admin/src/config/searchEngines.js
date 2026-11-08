export const searchEngines = Object.freeze([
  { code: 'baidu', label: '百度搜索', userAgent: 'Baiduspider', webmasterUrl: 'https://ziyuan.baidu.com/', domestic: true },
  { code: 'so360', label: '360 搜索', userAgent: '360Spider', webmasterUrl: 'https://zhanzhang.so.com/', domestic: true },
  { code: 'sogou', label: '搜狗搜索', userAgent: 'Sogou web spider', webmasterUrl: 'https://zhanzhang.sogou.com/', domestic: true },
  { code: 'shenma', label: '神马搜索', userAgent: 'YisouSpider', webmasterUrl: 'https://zhanzhang.sm.cn/', domestic: true },
  { code: 'bing', label: 'Bing 搜索', userAgent: 'bingbot', webmasterUrl: 'https://www.bing.com/webmasters/', domestic: false },
  { code: 'google', label: 'Google 搜索', userAgent: 'Googlebot', webmasterUrl: 'https://search.google.com/search-console', domestic: false },
])

export const findSearchEngine = userAgent => searchEngines.find(
  engine => engine.userAgent.toLowerCase() === String(userAgent || '').trim().toLowerCase(),
)
