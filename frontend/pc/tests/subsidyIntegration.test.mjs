import assert from 'node:assert/strict'
import { readFileSync } from 'node:fs'
import { test } from 'node:test'

/**
 * 国补在三个关键页面的接入断言。
 *
 * <p>这类测试防的是「接口写了、service 写了，但页面忘了接」——
 * 功能看起来写完了，用户侧却一点变化都没有。
 */

const read = (relative) => readFileSync(new URL(relative, import.meta.url), 'utf8')

const productPage = read('../app/pages/product/[id].vue')
const cartPage = read('../app/pages/cart/[id].vue')
const confirmPage = read('../app/pages/confirmOrder.vue')
const badge = read('../app/components/subsidy/SubsidyBadge.vue')
const service = read('../app/services/subsidy.service.ts')

test('商品详情页已接入国补试算并渲染标识组件', () => {
  assert.match(productPage, /import SubsidyBadge from '~\/components\/subsidy\/SubsidyBadge\.vue'/)
  assert.match(productPage, /<SubsidyBadge/)
  assert.match(productPage, /fetchSubsidyQuote/)
})

test('购物车已接入国补预估', () => {
  assert.match(cartPage, /fetchSubsidyQuote/)
  assert.match(cartPage, /subsidyDiscountAmount/)
  assert.match(cartPage, /refreshSubsidy/)
})

test('结算页国补抵扣与其它优惠并列单列展示', () => {
  assert.match(confirmPage, /fetchSubsidyQuote/)
  assert.match(confirmPage, /国补抵扣/)
  // 必须与红包 / PLUS / 会员折扣同级，用同一个 discount-line 结构
  assert.match(confirmPage, /class="discount-line subsidy-line"/)
})

test('国补调用走插件分发端点，未安装插件时降级为无国补', () => {
  assert.match(service, /\/plugin\/national-subsidy/)
  // 任何失败都必须吞掉并返回 null，绝不能把商品页/结算页打挂
  assert.match(service, /catch[\s\S]{0,200}return null/)
})

test('国补金额不在前端二次相减（避免与宿主算出的应付不一致）', () => {
  // 结算页只做展示：应付金额仍取宿主返回的 totalPrice
  assert.match(confirmPage, /displayPaymentTotal/)
  assert.doesNotMatch(confirmPage, /displayPaymentTotal\s*-\s*subsidy/)
  assert.doesNotMatch(confirmPage, /displayPaymentTotal\s*-\s*subsidyDiscountAmount/)
})

test('命中判定由后端权威返回，前端不自行判定类目/地区/实名', () => {
  // 前端不应出现这三类业务判定
  assert.doesNotMatch(badge, /categoryId\s*===/)
  assert.doesNotMatch(badge, /provinceCode\s*===/)
  assert.doesNotMatch(badge, /idCard|realnameVerified/)
})
