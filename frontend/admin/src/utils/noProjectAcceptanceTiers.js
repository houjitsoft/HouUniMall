const TIER_COUNT = 6
const DECIMAL_NUMBER_PATTERN = /^[+-]?(?:\d+(?:\.\d*)?|\.\d+)$/
const INTEGER_NUMBER_PATTERN = /^[+-]?\d+$/

export const DEFAULT_NO_ACCEPTANCE_TIERS = Object.freeze(
  Array.from({ length: TIER_COUNT }, (_, index) => Object.freeze({
    monthNo: index + 1,
    performanceRate: index < 2 ? 0 : 100,
    positionRate: 0
  }))
)

const cloneTier = (tier) => ({ ...tier })

const toFiniteDecimalNumber = (value) => {
  if (typeof value === 'number') return Number.isFinite(value) ? value : null
  if (typeof value !== 'string') return null
  const trimmed = value.trim()
  if (!trimmed || !DECIMAL_NUMBER_PATTERN.test(trimmed)) return null
  const numericValue = Number(trimmed)
  return Number.isFinite(numericValue) ? numericValue : null
}

const isNumberAtCentScale = (numericValue) => {
  const scaled = numericValue * 100
  const tolerance = Number.EPSILON * Math.max(1, Math.abs(scaled)) * 2
  return Math.abs(scaled - Math.round(scaled)) <= tolerance
}

const normalizeRate = (value) => {
  const numericValue = toFiniteDecimalNumber(value)
  if (typeof value === 'number') {
    return numericValue !== null && isNumberAtCentScale(numericValue)
      ? Math.round(numericValue * 100) / 100
      : value
  }
  if (typeof value !== 'string' || numericValue === null) return value
  const fraction = value.trim().split('.')[1] || ''
  return fraction.length <= 2 ? numericValue : value
}

const toIntegerNumber = (value) => {
  if (typeof value === 'number') return Number.isInteger(value) ? value : null
  if (typeof value !== 'string') return null
  const trimmed = value.trim()
  if (!INTEGER_NUMBER_PATTERN.test(trimmed)) return null
  const numericValue = Number(trimmed)
  return Number.isSafeInteger(numericValue) ? numericValue : null
}

const hasValidDecimalScale = (value, numericValue) => {
  if (typeof value === 'string') {
    const fraction = value.trim().split('.')[1] || ''
    return fraction.length <= 2
  }

  return isNumberAtCentScale(numericValue)
}

export function normalizeNoAcceptanceTiers(input) {
  const tiersByMonth = new Map()

  if (Array.isArray(input)) {
    input.forEach((tier) => {
      if (!tier || typeof tier !== 'object') return
      const monthNo = toIntegerNumber(tier.monthNo)
      if (!Number.isInteger(monthNo) || monthNo < 1 || monthNo > TIER_COUNT) return
      if (tiersByMonth.has(monthNo)) return

      tiersByMonth.set(monthNo, {
        monthNo,
        performanceRate: normalizeRate(tier.performanceRate),
        positionRate: 0
      })
    })
  }

  return DEFAULT_NO_ACCEPTANCE_TIERS.map((defaultTier) => (
    tiersByMonth.has(defaultTier.monthNo)
      ? cloneTier(tiersByMonth.get(defaultTier.monthNo))
      : cloneTier(defaultTier)
  ))
}

const validateRate = (value, tierIndex, label) => {
  if (value === null || value === undefined || (typeof value === 'string' && value.trim() === '')) {
    return `第${tierIndex + 1}档${label}不能为空`
  }

  const numericValue = toFiniteDecimalNumber(value)
  if (numericValue === null) {
    return `第${tierIndex + 1}档${label}必须是有限数值`
  }
  if (numericValue < 0 || numericValue > 100) {
    return `第${tierIndex + 1}档${label}必须在0到100之间`
  }
  if (!hasValidDecimalScale(value, numericValue)) {
    return `第${tierIndex + 1}档${label}最多保留2位小数`
  }
  return ''
}

export function validateNoAcceptanceTiers(tiers) {
  if (!Array.isArray(tiers) || tiers.length !== TIER_COUNT) {
    return '连续无验收扣减比例必须配置6档'
  }

  const months = new Set()
  for (let index = 0; index < tiers.length; index += 1) {
    const tier = tiers[index]
    const monthNo = toIntegerNumber(tier?.monthNo)
    if (!Number.isInteger(monthNo) || monthNo < 1 || monthNo > TIER_COUNT) {
      return `第${index + 1}档月份必须是1到6的整数`
    }
    if (months.has(monthNo)) {
      return '连续无验收档位月份必须唯一'
    }
    months.add(monthNo)

    const performanceError = validateRate(tier.performanceRate, index, '绩效工资扣减比例')
    if (performanceError) return performanceError
  }

  return ''
}

const formatRate = (value) => String(Math.round(Number(value) * 100) / 100)

export function buildNoAcceptanceSummary(tiers) {
  if (validateNoAcceptanceTiers(tiers)) return ''

  return [...tiers]
    .sort((left, right) => Number(left.monthNo) - Number(right.monthNo))
    .map((tier) => {
      const monthLabel = Number(tier.monthNo) === TIER_COUNT
        ? '6个月及以上'
        : `${Number(tier.monthNo)}个月`
      return `${monthLabel}：绩效${formatRate(tier.performanceRate)}%`
    })
    .join('；')
}
