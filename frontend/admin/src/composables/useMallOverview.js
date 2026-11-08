/**
 * 后台首页「商城经营」数据模型。
 *
 * 把 @/api/mall/home 的多个真实接口聚合成首屏能直接用的一份数据：
 * KPI（含环比）、运营待办与预警、交易趋势、热销商品 / 品类排行。
 *
 * 约定：
 * - 拿不到的数据一律是 0，环比算不出来的是 null（展示层显示「—」），
 *   不用随机数造数 —— 首页是给经营决策看的，假数比 0 更危险；
 * - 环比 = (本期 - 基期) / 基期，基期为 0 时无法计算，返回 null。
 */
import { ref } from 'vue'
import {
  getMallCategoryNames,
  getMallGoodsList,
  getMallOverview,
  getMallSalesTrend,
  getMallWorkbench,
  getOrderStatusTotal,
  getPaymentOverview,
  LOW_STOCK_THRESHOLD
} from '@/api/mall/home'

const num = (v) => {
  const n = Number(v)
  return Number.isFinite(n) ? n : 0
}

/** 环比：(cur - base) / base * 100；基期为 0 时返回 null */
export const growthRate = (cur, base) => {
  const c = num(cur)
  const b = num(base)
  if (!b) return null
  return ((c - b) / b) * 100
}

const sumBy = (list, field) => list.reduce((acc, row) => acc + num(row?.[field]), 0)

const emptyKpi = () => ({
  gmvToday: 0,
  gmvYesterday: 0,
  gmv7d: 0,
  gmvPrev7d: 0,
  gmvTotal: 0,
  orderToday: 0,
  orderYesterday: 0,
  order7d: 0,
  orderPrev7d: 0,
  orderTotal: 0,
  buyersToday: 0,
  buyersYesterday: 0,
  avgOrderValue: 0,
  avgOrderValueYesterday: 0,
  payRate: null,
  refundToday: 0,
  onSaleGoods: 0,
  /** 环比（百分比，null 表示算不出来） */
  trends: {}
})

const emptyAlerts = () => ({
  pendingShip: 0,
  pendingPay: 0,
  refundAfterSale: 0,
  lowStock: 0,
  offShelf: 0,
  riskGoods: 0,
  pendingMerchant: 0
})

export const emptyMallOverview = () => ({
  updatedAt: 0,
  kpi: emptyKpi(),
  alerts: emptyAlerts(),
  trend: [],
  goodsTop: [],
  categoryTop: []
})

/** 商品销量口径：优先真实销量 + 虚拟销量（与列表页保持一致） */
const goodsSales = (goods) => num(goods?.allSaleNum) || num(goods?.saleNum)

/**
 * 按品类聚合销量。商品挂多个品类时取最末一级（最具体的那个）。
 */
const aggregateCategories = (goodsList, categoryNames) => {
  const bucket = new Map()
  goodsList.forEach((goods) => {
    const ids = Array.isArray(goods?.categoryIdsArr) ? goods.categoryIdsArr : []
    const leafId = ids.length ? ids[ids.length - 1] : 0
    const key = String(leafId)
    const hit = bucket.get(key) || { id: leafId, name: categoryNames[key] || '未分类', sales: 0, goods: 0 }
    hit.sales += goodsSales(goods)
    hit.goods += 1
    bucket.set(key, hit)
  })
  return Array.from(bucket.values())
    .sort((a, b) => b.sales - a.sales)
    .slice(0, 8)
}

/**
 * 拉取并聚合商城首屏数据。
 * @returns {Promise<Object>} 见 emptyMallOverview()
 */
export const fetchMallOverview = async () => {
  const [
    workbench,
    overview,
    trend,
    orderTotal,
    payment,
    goodsList,
    categoryNames
  ] = await Promise.all([
    getMallWorkbench(),
    getMallOverview(),
    getMallSalesTrend(),
    getOrderStatusTotal(),
    getPaymentOverview(),
    getMallGoodsList(),
    getMallCategoryNames()
  ])

  const order = workbench?.order || {}
  const today = order.today || {}
  const yesterday = order.yesterday || {}
  const all = order.all || {}
  const users = workbench?.user_add_detail || {}

  const gmvToday = num(today.money)
  const orderToday = num(today.count)
  const gmvYesterday = num(yesterday.money)
  const orderYesterday = num(yesterday.count)

  // 交易趋势（近 30 天日粒度）派生近 7 日 / 前 7 日
  const last7 = trend.slice(-7)
  const prev7 = trend.slice(-14, -7)

  const avgOrderValue = orderToday ? gmvToday / orderToday : 0
  const avgOrderValueYesterday = orderYesterday ? gmvYesterday / orderYesterday : 0

  // 支付转化率 = 今日支付成功笔数 / 今日下单笔数
  const payRate = orderToday ? (num(payment.todayCount) / orderToday) * 100 : null

  const lowStockFromGoods = goodsList.filter((g) => num(g?.totalStock) <= LOW_STOCK_THRESHOLD).length

  return {
    updatedAt: Date.now(),
    kpi: {
      gmvToday,
      gmvYesterday,
      gmv7d: sumBy(last7, 'amount'),
      gmvPrev7d: sumBy(prev7, 'amount'),
      gmvTotal: num(all.money),
      orderToday,
      orderYesterday,
      order7d: sumBy(last7, 'count'),
      orderPrev7d: sumBy(prev7, 'count'),
      orderTotal: num(all.count),
      buyersToday: num(users.today),
      buyersYesterday: num(users.yesterday),
      avgOrderValue,
      avgOrderValueYesterday,
      payRate,
      refundToday: num(payment.todayRefund),
      onSaleGoods: goodsList.filter((g) => num(g?.status) === 1).length,
      trends: {
        gmvToday: growthRate(gmvToday, gmvYesterday),
        orderToday: growthRate(orderToday, orderYesterday),
        gmv7d: growthRate(sumBy(last7, 'amount'), sumBy(prev7, 'amount')),
        order7d: growthRate(sumBy(last7, 'count'), sumBy(prev7, 'count')),
        buyersToday: growthRate(users.today, users.yesterday),
        avgOrderValue: growthRate(avgOrderValue, avgOrderValueYesterday)
      }
    },
    alerts: {
      pendingShip: num(orderTotal.pendingShip),
      pendingPay: num(orderTotal.pendingPay),
      refundAfterSale: num(orderTotal.refund) + num(orderTotal.salesReturn) + num(orderTotal.afterSales),
      lowStock: num(overview?.lowStockGoods) || lowStockFromGoods,
      offShelf: goodsList.filter((g) => num(g?.status) === 0).length,
      riskGoods: goodsList.filter((g) => num(g?.auditStatus) === 2).length,
      pendingMerchant: num(overview?.pendingMerchantEntries)
    },
    trend,
    goodsTop: goodsList
      .slice()
      .sort((a, b) => goodsSales(b) - goodsSales(a))
      .slice(0, 8)
      .map((g) => ({
        id: g.id,
        name: g.name || '-',
        image: g.image || '',
        sales: goodsSales(g),
        stock: num(g?.totalStock),
        priceText: g.priceText || ''
      })),
    categoryTop: aggregateCategories(goodsList, categoryNames)
  }
}

/**
 * 首页用的响应式封装：load() 幂等重试，失败保留上一次结果。
 */
export const useMallOverview = () => {
  const overview = ref(emptyMallOverview())
  const loading = ref(false)

  const load = async () => {
    loading.value = true
    try {
      overview.value = await fetchMallOverview()
    } catch (error) {
      // 聚合层已经逐个接口兜底，走到这里说明结构异常，保留旧值即可
      console.warn('[mall-overview] 首屏商城数据聚合失败:', error?.message || error)
    } finally {
      loading.value = false
    }
  }

  return { overview, loading, load }
}

export default useMallOverview
