/**
 * 国补（国家补贴 / 以旧换新补贴）金额纯函数。
 *
 * <p>与后端 {@code SubsidyCalculator} 同构的**前端侧最小实现**：只负责
 * 「按后端返回的补贴额做展示换算」与「本地对账校验」，不重复实现业务规则。
 *
 * <p><b>为什么不重复算比例</b>：比例 / 上限 / 类目 / 地区判定全在后端算，
 * 前端再算一遍必然出现两边不一致，最终表现为「页面显示能减 200，支付时只减 180」。
 * 因此前端只做展示与自检。
 */

/** 补贴行（对应后端 `SubsidyLine`）。 */
export interface SubsidyLine {
  goodsId?: number | null
  quantity?: number | null
  lineAmount?: number | string | null
  rawAmount?: number | string | null
  finalAmount?: number | string | null
  limitedBy?: string | null
}

/** 试算结果（对应后端 `SubsidyQuoteResponse`）。 */
export interface SubsidyQuote {
  hit: boolean
  reason?: string | null
  message?: string | null
  ruleId?: number | null
  policyCode?: string | null
  ruleName?: string | null
  subsidyRate?: number | string | null
  subsidyAmount?: number | string | null
  payableAmount?: number | string | null
  lines?: SubsidyLine[] | null
}

/** 数字安全转 value，无效值一律 0。 */
export function toAmount(value: number | string | null | undefined): number {
  if (value === null || value === undefined || value === '') return 0
  const parsed = typeof value === 'number' ? value : Number(value)
  return Number.isFinite(parsed) ? parsed : 0
}

/** 保留 2 位，与后端精度一致。 */
export function round2(value: number): number {
  return Math.round((value + Number.EPSILON) * 100) / 100
}

/**
 * 是否应在界面展示国补入口。
 *
 * <p>规则：后端明确命中、金额为正、行级明细之和与总额一致，三者全满足才展示。
 * 任何一项不满足都按「无国补」处理——宁可少展示，不可展示一个算错的价。
 */
export function shouldShowSubsidy(quote: SubsidyQuote | null | undefined): boolean {
  if (!quote || !quote.hit) return false
  const total = toAmount(quote.subsidyAmount)
  if (total <= 0) return false
  const lines = quote.lines || []
  if (lines.length === 0) return false
  const lineSum = round2(lines.reduce((sum, line) => sum + toAmount(line.finalAmount), 0))
  return Math.abs(lineSum - round2(total)) < 0.01
}

/**
 * 对账校验：商品原价 − 国补额 应等于补贴后应付。
 *
 * @returns 通过返回 null，不通过返回中文原因（用于告警，不弹给用户）
 */
export function verifyPayable(goodsAmount: number, quote: SubsidyQuote | null | undefined): string | null {
  if (!quote || !quote.hit) return null
  const base = round2(toAmount(goodsAmount))
  const subsidy = round2(toAmount(quote.subsidyAmount))
  const payable = round2(toAmount(quote.payableAmount))
  const expected = round2(Math.max(base - subsidy, 0))
  if (Math.abs(expected - payable) >= 0.01) {
    return `国补对账不一致：原价 ${base} − 补贴 ${subsidy} = ${expected}，但应付为 ${payable}`
  }
  return null
}

/**
 * 行级补贴分摊到某商品：用于商品详情与购物车在列表里各自显示「本行可减」。
 *
 * @returns 该行可减金额；未命中返回 0
 */
export function lineSubsidy(quote: SubsidyQuote | null | undefined, goodsId: number | null | undefined): number {
  if (!shouldShowSubsidy(quote) || goodsId === null || goodsId === undefined) return 0
  const line = (quote?.lines || []).find((item) => item?.goodsId === goodsId)
  return line ? round2(toAmount(line.finalAmount)) : 0
}

/** 比例文本：0.2 → 「20%」，无值返回空串。 */
export function rateText(rate: number | string | null | undefined): string {
  if (rate === null || rate === undefined || rate === '') return ''
  const percent = toAmount(rate) * 100
  if (!Number.isFinite(percent) || percent <= 0) return ''
  return `${round2(percent)}%`
}

/**
 * 叠加互斥提示：后端因券/满减互斥而未命中时，前端据此解释原因（而不是静默无入口）。
 */
export function exclusiveHint(quote: SubsidyQuote | null | undefined): string {
  if (!quote || quote.hit || !quote.reason) return ''
  const map: Record<string, string> = {
    COUPON_EXCLUSIVE: '使用优惠券后不可再享国补',
    PROMOTION_EXCLUSIVE: '参与满减活动后不可再享国补',
    POINT_EXCLUSIVE: '使用积分抵扣后不可再享国补',
    REALNAME_REQUIRED: '完成实名认证后可享国补'
  }
  return map[quote.reason] || quote.message || ''
}
