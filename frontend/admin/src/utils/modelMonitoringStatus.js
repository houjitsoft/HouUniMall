export function modelMonitoringStatusLabel(platform = {}) {
  if (!platform.modelConfigured) {
    return platform.subscribed ? '已纳入监测，模型未配置' : '系统模型未配置'
  }
  if (!platform.modelEnabled) {
    return platform.subscribed ? '已纳入监测，模型未启用' : '已配置但未启用'
  }
  return platform.subscribed ? '正在监测' : '模型已启用，未监测'
}

const PLATFORM_ALIASES = {
  wenxin: ['ernie', 'wenxin', 'yiyan'],
  zhipu: ['chatglm', 'zhipu', 'glm'],
  yuanbao: ['hunyuan', 'yuanbao'],
  doubao: ['doubao', 'ark', 'volcengine'],
  qwen: ['qwen', 'tongyi'],
  kimi: ['kimi', 'moonshot'],
  nano: ['nano', '360'],
  chatgpt: ['openai', 'gpt'],
  gemini: ['gemini', 'google'],
  claude: ['claude', 'anthropic'],
  perplexity: ['perplexity', 'pplx'],
  deepseek: ['deepseek']
}

function normalize(value) {
  return String(value || '').trim().toLowerCase().replaceAll('_', '-').replaceAll(' ', '')
}

function platformKey(platform = {}) {
  const identity = normalize(platform.id || platform.code || platform.name)
  if (identity.includes('deepseek') || identity.includes('深度求索')) return 'deepseek'
  if (identity.includes('豆包') || identity.includes('doubao')) return 'doubao'
  if (identity.includes('元宝') || identity.includes('yuanbao')) return 'yuanbao'
  if (identity.includes('通义') || identity.includes('千问') || identity.includes('qwen')) return 'qwen'
  if (identity.includes('文心') || identity.includes('ernie') || identity.includes('wenxin')) return 'wenxin'
  if (identity.includes('kimi') || identity.includes('月之暗面')) return 'kimi'
  if (identity.includes('智谱') || identity.includes('zhipu') || identity.startsWith('glm')) return 'zhipu'
  if (identity.includes('纳米') || identity === 'nano') return 'nano'
  if (identity.includes('chatgpt') || identity === 'openai') return 'chatgpt'
  if (identity.includes('perplexity')) return 'perplexity'
  if (identity.includes('claude') || identity.includes('anthropic')) return 'claude'
  if (identity.includes('gemini') || identity.includes('googleai')) return 'gemini'
  return identity
}

function findMatchingConfig(platform, configs) {
  const key = platformKey(platform)
  const aliases = PLATFORM_ALIASES[key] || [key]
  const modelMatch = configs.find((config) => {
    const model = normalize(config.modelName)
    return aliases.some(alias => model === alias || model.startsWith(`${alias}-`) || model.includes(alias))
  })
  if (modelMatch) return modelMatch
  return configs.find(config => aliases.includes(normalize(config.providerCode))) || null
}

/** 补全旧版 GEO 接口未区分的“已配置/已启用”状态。 */
export function resolvePlatformModelState(platform = {}, configs = []) {
  const candidates = Array.isArray(configs) ? configs : []
  const enabledConfigs = candidates.filter(config => Number(config.isEnabled) === 1 && Number(config.status) === 1)
  const configuredMatch = findMatchingConfig(platform, candidates)
  const enabledMatch = findMatchingConfig(platform, enabledConfigs)
  const displayMatch = enabledMatch || configuredMatch
  return {
    configured: Boolean(configuredMatch),
    enabled: Boolean(enabledMatch),
    modelName: displayMatch?.modelName || '',
    providerCode: displayMatch?.providerCode || ''
  }
}
