import http from '@/utils/http'

/**
 * 数据统计API
 */

// =============== 统计报表 ===============

/**
 * 获取统计概览
 */
export function getReportOverview(data) {
  return http({
    url: '/web/mall/statistics/report/overview',
    method: 'post',
    data
  })
}

/**
 * 获取统计明细
 */
export function getReportDetail(data) {
  return http({
    url: '/web/mall/statistics/report/detail',
    method: 'post',
    data
  })
}

/**
 * 获取销售趋势
 */
export function getSalesTrend(data) {
  return http({
    url: '/web/mall/statistics/report/trend',
    method: 'post',
    data
  })
}

/**
 * 导出统计报表
 */
export function exportReport(data) {
  return http({
    url: '/web/mall/statistics/report/export',
    method: 'post',
    data
  })
}

// =============== 销售统计 ===============

/**
 * 获取销售统计概览
 */
export function getSalesStatistics(data) {
  return http({
    url: '/web/mall/statistics/sales/overview',
    method: 'post',
    data
  })
}

/**
 * 获取销售趋势详情
 */
export function getSalesTrendDetail(data) {
  return http({
    url: '/web/mall/statistics/sales/trend',
    method: 'post',
    data
  })
}

// =============== 财务统计 ===============

/**
 * 获取财务统计
 */
export function getFinanceStatistics(data) {
  return http({
    url: '/web/mall/statistics/finance/overview',
    method: 'post',
    data
  })
}

// =============== 库存统计 ===============

/**
 * 获取库存统计
 */
export function getInventoryStatistics(data) {
  return http({
    url: '/web/mall/statistics/inventory/overview',
    method: 'post',
    data
  })
}

// =============== 客户统计 ===============

/**
 * 获取客户统计
 */
export function getCustomerStatistics(data) {
  return http({
    url: '/web/mall/statistics/customer/overview',
    method: 'post',
    data
  })
}

// =============== 商品统计 ===============

/**
 * 获取商品统计
 */
export function getGoodsStatistics(data) {
  return http({
    url: '/web/mall/statistics/goods/overview',
    method: 'post',
    data
  })
}
