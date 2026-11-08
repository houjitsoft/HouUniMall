export function buildUsageFilterPayload(filters = {}) {
  const payload = {}
  if (filters.moduleName) payload.moduleName = filters.moduleName
  if (filters.modelName) payload.modelName = filters.modelName
  if (Array.isArray(filters.dateRange) && filters.dateRange.length === 2) {
    payload.startTime = `${filters.dateRange[0]} 00:00:00`
    payload.endTime = `${filters.dateRange[1]} 23:59:59`
  }
  return payload
}

export function buildAiUsageTabs(canViewAllUsage = false, canManageQuota = false) {
  return [
    { name: 'mine', label: '我的使用量' },
    ...(canViewAllUsage ? [{ name: 'all', label: '全部使用量' }] : []),
    ...(canManageQuota ? [{ name: 'quota', label: '用户额度' }] : [])
  ]
}

export function loadActiveAiUsageTab(activeTab, loaders) {
  if (activeTab === 'all') return loaders.all()
  if (activeTab === 'quota') return loaders.quota()
  return loaders.mine()
}

export function formatUsageTokens(value) {
  const numeric = Number(value || 0)
  return Number.isFinite(numeric) ? numeric.toLocaleString('zh-CN') : '0'
}

export function formatUsageCost(value, currency = 'USD') {
  const numeric = Number(value || 0)
  const amount = Number.isFinite(numeric) ? numeric : 0
  const code = String(currency || 'USD').toUpperCase()
  const symbol = code === 'CNY' ? '¥' : code === 'USD' ? '$' : `${code} `
  return `${symbol}${amount.toFixed(4)}`
}

export function normalizeUsageCosts(row = {}) {
  const rawBreakdown = row.cost_breakdown ?? row.costBreakdown
  const breakdown = Array.isArray(rawBreakdown) ? rawBreakdown : []
  const costs = breakdown.length > 0
    ? breakdown
    : [{ currency: row.currency, total_price: row.total_price ?? row.totalPrice }]

  return costs.map(cost => ({
    currency: String(cost?.currency || 'USD').trim().toUpperCase() || 'USD',
    totalPrice: finiteNumber(cost?.total_price ?? cost?.totalPrice)
  }))
}

export function buildUsageCostKpis(row = {}) {
  const costs = normalizeUsageCosts(row)
  const multipleCurrencies = costs.length > 1
  return costs.map(cost => ({
    title: multipleCurrencies ? `费用合计 (${cost.currency})` : '费用合计',
    value: cost.totalPrice,
    precision: 4,
    suffix: cost.currency,
    span: multipleCurrencies ? 6 : 8
  }))
}

export function formatUsageTime(value) {
  if (!value) return '-'
  const date = new Date(value)
  if (Number.isNaN(date.getTime())) return String(value)
  const pad = number => String(number).padStart(2, '0')
  return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())} ${pad(date.getHours())}:${pad(date.getMinutes())}:${pad(date.getSeconds())}`
}

export function normalizeUsageOptions(options = {}) {
  return {
    modules: Array.isArray(options.modules) ? options.modules : [],
    models: Array.isArray(options.models) ? options.models : []
  }
}

export function normalizeQuota(quota = {}) {
  return {
    freeQuotaTokens: finiteNumber(quota.free_quota_tokens ?? quota.freeQuotaTokens),
    freeUsedTokens: finiteNumber(quota.free_used_tokens ?? quota.freeUsedTokens),
    freeRemainingTokens: finiteNumber(quota.free_remaining_tokens ?? quota.freeRemainingTokens),
    freeQuotaStartedAt: quota.free_quota_started_at ?? quota.freeQuotaStartedAt ?? null,
    freeQuotaExpiresAt: quota.free_quota_expires_at ?? quota.freeQuotaExpiresAt ?? null,
    freeQuotaExpired: quota.free_quota_expired === true || quota.freeQuotaExpired === true,
    purchasedBalanceTokens: finiteNumber(quota.purchased_balance_tokens ?? quota.purchasedBalanceTokens),
    remainingTokens: finiteNumber(quota.remaining_tokens ?? quota.remainingTokens),
    usagePercent: finiteNumber(quota.usage_percent ?? quota.usagePercent)
  }
}

export function formatQuotaExpiry(quota = {}) {
  if (quota.freeQuotaExpired) return '已失效'
  if (!quota.freeQuotaExpiresAt) return '永久有效'
  return formatUsageTime(quota.freeQuotaExpiresAt)
}

function finiteNumber(value) {
  const numeric = Number(value ?? 0)
  return Number.isFinite(numeric) ? numeric : 0
}
