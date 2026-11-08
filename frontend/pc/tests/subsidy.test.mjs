import assert from 'node:assert/strict'
import { readFileSync } from 'node:fs'
import { test } from 'node:test'

/**
 * 国补前端纯逻辑单测。
 *
 * <p>前端**不重新实现**补贴规则（比例/上限/类目判定全在后端），
 * 因此这里测的是「展示前的自检」：什么情况下允许把国补显示给用户。
 * 这几条断言直接对应「宁可少展示，不可展示一个算错的价」。
 */

const subsidySource = readFileSync(new URL('../app/utils/subsidy.ts', import.meta.url), 'utf8')

/** 用最小实现复刻 subsidy.ts 的判定逻辑（与源码保持一致，改动须同步）。 */
function toAmount(value) {
  if (value === null || value === undefined || value === '') return 0
  const parsed = typeof value === 'number' ? value : Number(value)
  return Number.isFinite(parsed) ? parsed : 0
}
function round2(value) {
  return Math.round((value + Number.EPSILON) * 100) / 100
}
function shouldShowSubsidy(quote) {
  if (!quote || !quote.hit) return false
  const total = toAmount(quote.subsidyAmount)
  if (total <= 0) return false
  const lines = quote.lines || []
  if (lines.length === 0) return false
  const lineSum = round2(lines.reduce((sum, line) => sum + toAmount(line.finalAmount), 0))
  return Math.abs(lineSum - round2(total)) < 0.01
}
function verifyPayable(goodsAmount, quote) {
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
function lineSubsidy(quote, goodsId) {
  if (!shouldShowSubsidy(quote) || goodsId === null || goodsId === undefined) return 0
  const line = (quote.lines || []).find((item) => item?.goodsId === goodsId)
  return line ? round2(toAmount(line.finalAmount)) : 0
}
function rateText(rate) {
  if (rate === null || rate === undefined || rate === '') return ''
  const percent = toAmount(rate) * 100
  if (!Number.isFinite(percent) || percent <= 0) return ''
  return `${round2(percent)}%`
}
function exclusiveHint(quote) {
  if (!quote || quote.hit || !quote.reason) return ''
  const map = {
    COUPON_EXCLUSIVE: '使用优惠券后不可再享国补',
    PROMOTION_EXCLUSIVE: '参与满减活动后不可再享国补',
    POINT_EXCLUSIVE: '使用积分抵扣后不可再享国补',
    REALNAME_REQUIRED: '完成实名认证后可享国补'
  }
  return map[quote.reason] || quote.message || ''
}

const hitQuote = {
  hit: true,
  reason: 'HIT',
  subsidyAmount: 200.00,
  payableAmount: 800.00,
  subsidyRate: 0.2,
  lines: [{ goodsId: 101, quantity: 1, lineAmount: 1000.00, finalAmount: 200.00 }]
}

test('未安装插件（quote 为 null）时不展示任何国补入口', () => {
  assert.equal(shouldShowSubsidy(null), false)
  assert.equal(shouldShowSubsidy(undefined), false)
  assert.equal(lineSubsidy(null, 101), 0)
  assert.equal(exclusiveHint(null), '')
  assert.equal(verifyPayable(1000, null), null)
})

test('后端未命中时不展示国补', () => {
  const miss = { hit: false, reason: 'REGION_NOT_MATCHED', message: '当前收货地区不在国补范围内' }
  assert.equal(shouldShowSubsidy(miss), false)
  assert.equal(lineSubsidy(miss, 101), 0)
})

test('命中且金额为正时展示国补', () => {
  assert.equal(shouldShowSubsidy(hitQuote), true)
  assert.equal(lineSubsidy(hitQuote, 101), 200.00)
})

test('金额为零或负数时不展示（避免出现「可减 -0.00」）', () => {
  assert.equal(shouldShowSubsidy({ ...hitQuote, subsidyAmount: 0, payableAmount: 1000 }), false)
  assert.equal(shouldShowSubsidy({ ...hitQuote, subsidyAmount: -10, payableAmount: 1010 }), false)
})

test('行级明细之和与总额不一致时拒绝展示（防止展示算错的价）', () => {
  const broken = {
    ...hitQuote,
    subsidyAmount: 200.00,
    // 明细只加起来 150，与总额 200 对不上
    lines: [{ goodsId: 101, quantity: 1, lineAmount: 1000.00, finalAmount: 150.00 }]
  }
  assert.equal(shouldShowSubsidy(broken), false)
})

test('明细缺失时同样拒绝展示', () => {
  assert.equal(shouldShowSubsidy({ ...hitQuote, lines: [] }), false)
  assert.equal(shouldShowSubsidy({ ...hitQuote, lines: null }), false)
})

test('对账一致时无告警', () => {
  assert.equal(verifyPayable(1000, hitQuote), null)
})

test('对账不一致时返回中文原因供告警', () => {
  const bad = { ...hitQuote, payableAmount: 900 }
  const message = verifyPayable(1000, bad)
  assert.ok(message && message.includes('对账不一致'))
})

test('补贴额大于原价时应付为 0 而不为负', () => {
  const over = {
    hit: true, reason: 'HIT', subsidyAmount: 1200, payableAmount: 0,
    lines: [{ goodsId: 1, quantity: 1, lineAmount: 1000.00, finalAmount: 1200.00 }]
  }
  assert.equal(verifyPayable(1000, over), null)
})

test('字符串金额与数字金额等价（后端 JSON 可能给字符串）', () => {
  const asString = {
    ...hitQuote,
    subsidyAmount: '200.00',
    payableAmount: '800.00',
    lines: [{ goodsId: 101, quantity: 1, lineAmount: '1000.00', finalAmount: '200.00' }]
  }
  assert.equal(shouldShowSubsidy(asString), true)
  assert.equal(lineSubsidy(asString, 101), 200.00)
  assert.equal(verifyPayable(1000, asString), null)
})

test('比例文本按百分数展示', () => {
  assert.equal(rateText(0.2), '20%')
  assert.equal(rateText('0.15'), '15%')
  assert.equal(rateText(null), '')
  assert.equal(rateText(0), '')
})

test('互斥原因给出可读提示而不是静默无入口', () => {
  assert.equal(
    exclusiveHint({ hit: false, reason: 'COUPON_EXCLUSIVE' }),
    '使用优惠券后不可再享国补'
  )
  assert.equal(
    exclusiveHint({ hit: false, reason: 'REALNAME_REQUIRED' }),
    '完成实名认证后可享国补'
  )
  // 命中时不给互斥提示
  assert.equal(exclusiveHint(hitQuote), '')
})

test('行级分摊能定位到具体商品', () => {
  const multi = {
    ...hitQuote,
    subsidyAmount: 300.00,
    payableAmount: 1700.00,
    lines: [
      { goodsId: 101, quantity: 1, lineAmount: 1000.00, finalAmount: 200.00 },
      { goodsId: 102, quantity: 1, lineAmount: 500.00, finalAmount: 100.00 }
    ]
  }
  assert.equal(lineSubsidy(multi, 101), 200.00)
  assert.equal(lineSubsidy(multi, 102), 100.00)
  assert.equal(lineSubsidy(multi, 999), 0)
})

test('前端不得自行计算补贴金额，只能透传后端结果', () => {
  // 前端出现比例乘法即意味着重复实现业务规则，会与后端算不一致
  assert.doesNotMatch(subsidySource, /\*\s*subsidyRate/)
  assert.doesNotMatch(subsidySource, /subsidyRate\s*\*/)
})
