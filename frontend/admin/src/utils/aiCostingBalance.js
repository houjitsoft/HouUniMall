/**
 * 用量费用页：按当前登录会员展示模块 / 时间 / 用量标签与充值、筛选。
 */

export const MODULE_LABEL_MAP = {
  geo: 'HouGEO',
  mall: '商城',
  oa: 'OA',
  cloud: 'Cloud',
  pay: '支付',
  ai: 'AI'
}

export const BIZ_TYPE_NAME_MAP = {
  ai_studio_keyword_expand: '关键词扩展',
  ai_studio_content_generate: '内容生成',
  ai_studio_platform_adapt: '平台适配',
  support_chat: '智能客服'
}

export const AI_RECHARGE_DONE_EVENT = 'houcloud-ai-recharge-done'

export function getModuleLabel(moduleName) {
  if (!moduleName) return '未知模块'
  const key = String(moduleName).trim().toLowerCase()
  return MODULE_LABEL_MAP[key] || moduleName
}

export function getBizTypeName(bizType) {
  if (!bizType) return '未知'
  return BIZ_TYPE_NAME_MAP[bizType] || bizType
}

export function resolveMemberDisplay(userInfo, memberPayload) {
  if (memberPayload?.displayName) return String(memberPayload.displayName)
  if (!userInfo || typeof userInfo !== 'object') return '当前会员'
  return (
    userInfo.userNickname
    || userInfo.user_nickname
    || userInfo.nickname
    || userInfo.userName
    || userInfo.user_name
    || userInfo.username
    || userInfo.userId
    || userInfo.user_id
    || '当前会员'
  )
}

export function resolveMemberId(userInfo, getUserIdFn) {
  const fromUser = userInfo?.userId ?? userInfo?.user_id ?? userInfo?.id
  if (fromUser != null && String(fromUser).trim()) {
    return String(fromUser).trim()
  }
  if (typeof getUserIdFn === 'function') {
    const id = getUserIdFn()
    if (id != null && String(id).trim()) return String(id).trim()
  }
  return ''
}

export function formatUsageTokens(val) {
  if (val == null || val === '') return '0'
  return Number(val).toLocaleString('zh-CN')
}

export function formatUsageCost(val, currency = 'USD') {
  const num = Number(val || 0)
  const symbol = currency === 'CNY' || currency === 'RMB' ? '¥' : '$'
  return `${symbol}${num.toFixed(4)}`
}

export function normalizeQuota(raw) {
  const q = raw && typeof raw === 'object' ? raw : {}
  return {
    freeQuotaTokens: Number(q.free_quota_tokens ?? q.freeQuotaTokens ?? 0),
    usedTokens: Number(q.used_tokens ?? q.usedTokens ?? 0),
    remainingTokens: Number(q.remaining_tokens ?? q.remainingTokens ?? 0),
    usagePercent: Number(q.usage_percent ?? q.usagePercent ?? 0)
  }
}

export function normalizeRechargePackages(payload) {
  const list = payload?.packages || payload?.list || (Array.isArray(payload) ? payload : [])
  return (list || []).map((pkg) => ({
    id: pkg.id,
    title: pkg.title,
    description: pkg.description || '',
    tokenAmount: Number(pkg.token_amount ?? pkg.tokenAmount ?? 0),
    priceYuan: Number(pkg.price_yuan ?? pkg.priceYuan ?? pkg.price ?? 0)
  })).filter((pkg) => pkg.id)
}

export function isApiOk(res) {
  return res && (res.code === 0 || res.code === 1 || res.code === 200)
}

export function buildCloudRechargeJumpUrl(origin = (typeof window !== 'undefined' ? window.location.origin : '')) {
  return `${String(origin || '').replace(/\/$/, '')}/setting/ai-recharge-callback`
}

/** 组装用量筛选请求参数（模块 / 模型 / 日期区间） */
export function buildUsageFilterPayload({ moduleName, modelName, dateRange } = {}) {
  const payload = {}
  if (moduleName) payload.moduleName = String(moduleName).trim()
  if (modelName) payload.modelName = String(modelName).trim()
  if (Array.isArray(dateRange) && dateRange.length === 2 && dateRange[0] && dateRange[1]) {
    payload.startTime = `${dateRange[0]} 00:00:00`
    payload.endTime = `${dateRange[1]} 23:59:59`
  }
  return payload
}

export function normalizeFilterOptions(raw) {
  const options = raw && typeof raw === 'object' ? raw : {}
  const modules = Array.isArray(options.modules)
    ? options.modules.map((m) => ({
      moduleName: m.moduleName || m.value || '',
      moduleLabel: m.moduleLabel || getModuleLabel(m.moduleName || m.value)
    })).filter((m) => m.moduleName)
    : []
  const models = Array.isArray(options.models)
    ? options.models.map((m) => (typeof m === 'string' ? m : (m.modelName || m.value || ''))).filter(Boolean)
    : []
  return { modules, models }
}
