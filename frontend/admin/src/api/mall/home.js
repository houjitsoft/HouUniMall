/**
 * 后台首页「商城经营首屏」聚合数据接口。
 *
 * 背景：首页原本的统计卡片走的是 OA / 支付口径（待办、合同、今日营收），
 * 与「商城」定位不符。这里把首屏需要的商城经营数据一次性聚合好：
 * 核心指标（今日 / 近 7 日成交额、订单量、下单用户数、客单价、支付转化率）、
 * 运营待办与预警（待发货 / 待付款 / 退款售后 / 库存预警 / 下架违规 / 待审入驻）、
 * 交易趋势、热销商品与品类排行。
 *
 * 设计约束：
 * - 所有请求都带 skipAuthRedirect / skipErrorMessage，任一源挂掉只降级为空，
 *   绝不能因为一个统计接口失败把整个会话踢回登录页；
 * - 只取真实接口，不做任何 mock；取不到就是 0 / null，由展示层决定如何呈现。
 */
import request from '@/utils/request'

const SILENT = { skipAuthRedirect: true, skipErrorMessage: true }

/** 库存预警阈值：库存（含 SKU 汇总）不高于该值即视为预警 */
export const LOW_STOCK_THRESHOLD = 10

/**
 * 单接口拉取：失败返回 null，不抛错。
 * 网关偶发抖动会让某一路统计变成空，首屏会凭空缺一块（如热销排行），
 * 因此失败后间隔 600ms 重试一次，仍失败才降级。
 */
async function postSilent(url, body = {}, attempt = 0) {
  try {
    const res = await request.post(url, body, SILENT)
    return res?.data ?? null
  } catch (error) {
    if (attempt < 1) {
      await new Promise((resolve) => setTimeout(resolve, 600))
      return postSilent(url, body, attempt + 1)
    }
    return null
  }
}

const num = (v) => {
  const n = Number(v)
  return Number.isFinite(n) ? n : 0
}

/**
 * 拉取商城工作台（今日 / 昨日 / 累计 订单数与金额、新增客户）
 * @returns {Promise<Object|null>}
 */
export const getMallWorkbench = () => postSilent('/web/mall/dashboard/workbench')

/**
 * 拉取商城预警概览（库存预警、风险预警、待审入驻）
 * @returns {Promise<Object|null>}
 */
export const getMallOverview = () => postSilent('/web/mall/dashboard/overview')

/**
 * 拉取交易趋势（近 30 天日粒度：date / amount / count）
 * @returns {Promise<Array>}
 */
export const getMallSalesTrend = async () => {
  const data = await postSilent('/web/mall/dashboard/sales-trend', { timeRange: 'month' })
  const list = Array.isArray(data?.chartData) ? data.chartData : []
  return list
    .map((row) => ({
      date: String(row?.date ?? ''),
      amount: num(row?.amount),
      count: num(row?.count)
    }))
    .filter((row) => row.date)
    .sort((a, b) => (a.date < b.date ? -1 : a.date > b.date ? 1 : 0))
}

/**
 * 拉取订单中心各状态计数（待付款 / 待发货 / 退款 / 退货 / 售后）
 * @returns {Promise<Object>}
 */
export const getOrderStatusTotal = async () => {
  const data = await postSilent('/web/order/statistics', {})
  return {
    pendingPay: num(data?.waitingForPayment),
    pendingShip: num(data?.waitForTheDelivery),
    shipped: num(data?.shipped),
    finished: num(data?.offTheStocks),
    salesReturn: num(data?.salesReturn),
    refund: num(data?.refund),
    afterSales: num(data?.afterSales)
  }
}

/**
 * 拉取支付概览（今日交易额 / 笔数 / 退款额 / 支付成功率）
 * @returns {Promise<Object>}
 */
export const getPaymentOverview = async () => {
  const data = await postSilent('/web/payment/dashboard/overview', {})
  return {
    todayAmount: num(data?.todayAmount),
    todayCount: num(data?.todayCount),
    todayRefund: num(data?.todayRefund),
    successRate: num(data?.successRate)
  }
}

/**
 * 拉取商品列表（用于热销排行 / 品类聚合 / 库存与上下架预警）
 * @param {number} size
 * @returns {Promise<Array>}
 */
export const getMallGoodsList = async (size = 100) => {
  const data = await postSilent('/web/mall/goods/list', { page: 1, size })
  return Array.isArray(data?.lists) ? data.lists : []
}

/**
 * 拉取品类树并压平成 id -> 名称 映射
 * @returns {Promise<Object>}
 */
export const getMallCategoryNames = async () => {
  const data = await postSilent('/web/mall/goods/category/tree', {})
  const map = {}
  const walk = (nodes) => {
    if (!Array.isArray(nodes)) return
    nodes.forEach((node) => {
      if (node?.id != null) map[String(node.id)] = node.name || `分类 ${node.id}`
      walk(node?.children)
    })
  }
  walk(Array.isArray(data) ? data : data?.lists || [])
  return map
}

export default {
  getMallWorkbench,
  getMallOverview,
  getMallSalesTrend,
  getOrderStatusTotal,
  getPaymentOverview,
  getMallGoodsList,
  getMallCategoryNames
}
