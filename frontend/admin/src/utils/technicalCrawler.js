import { findSearchEngine } from '../config/searchEngines.js'

const crawlerLabels = Object.freeze({
  GPTBot: 'AI 搜索（GPTBot）',
  'OAI-SearchBot': 'OpenAI 搜索抓取',
  'ChatGPT-User': 'ChatGPT 即时访问',
  ClaudeBot: 'Claude AI 抓取',
  PerplexityBot: 'Perplexity 搜索',
  Bytespider: '字节跳动 AI 抓取',
})

export function crawlerLabel(userAgent) {
  const value = String(userAgent || '').trim()
  if (!value) return 'Google 搜索'
  return findSearchEngine(value)?.label || crawlerLabels[value] || value
}
