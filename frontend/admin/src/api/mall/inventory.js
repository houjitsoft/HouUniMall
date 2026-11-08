import http from '@/utils/http'

/**
 * 库存管理API
 */

// =============== 库存统计 ===============

/**
 * 获取库存统计概览数据
 */
export function getInventoryStatistics(data) {
  return http({
    url: '/web/mall/inventory/statistics/overview',
    method: 'post',
    data
  })
}

/**
 * 获取库存统计列表
 */
export function getInventoryList(data) {
  return http({
    url: '/web/mall/inventory/statistics/list',
    method: 'post',
    data
  })
}

/**
 * 查看库存流水记录
 */
export function getInventoryFlow(data) {
  return http({
    url: '/web/mall/inventory/statistics/flow',
    method: 'post',
    data
  })
}

// =============== 库存预警 ===============

/**
 * 获取预警统计概览
 */
export function getAlertOverview(data) {
  return http({
    url: '/web/mall/inventory/alert/overview',
    method: 'post',
    data
  })
}

/**
 * 获取预警商品列表
 */
export function getAlertList(data) {
  return http({
    url: '/web/mall/inventory/alert/list',
    method: 'post',
    data
  })
}

/**
 * 调整预警值
 */
export function adjustAlertValue(data) {
  return http({
    url: '/web/mall/inventory/alert/adjust',
    method: 'post',
    data
  })
}

/**
 * 创建采购申请
 */
export function createPurchaseRequest(data) {
  return http({
    url: '/web/mall/inventory/alert/purchase',
    method: 'post',
    data
  })
}

// =============== 库存盘点 ===============

/**
 * 获取盘点单列表
 */
export function getCountList(data) {
  return http({
    url: '/web/mall/inventory/count/list',
    method: 'post',
    data
  })
}

/**
 * 创建盘点单
 */
export function createCount(data) {
  return http({
    url: '/web/mall/inventory/count/create',
    method: 'post',
    data
  })
}

/**
 * 查看盘点单详情
 */
export function getCountDetail(data) {
  return http({
    url: '/web/mall/inventory/count/detail',
    method: 'post',
    data
  })
}

/**
 * 开始盘点
 */
export function startCount(data) {
  return http({
    url: '/web/mall/inventory/count/start',
    method: 'post',
    data
  })
}

/**
 * 完成盘点
 */
export function completeCount(data) {
  return http({
    url: '/web/mall/inventory/count/complete',
    method: 'post',
    data
  })
}

/**
 * 取消盘点
 */
export function cancelCount(data) {
  return http({
    url: '/web/mall/inventory/count/cancel',
    method: 'post',
    data
  })
}

// =============== 出库管理 ===============

/**
 * 获取出库单列表
 */
export function getOutboundList(data) {
  return http({
    url: '/web/mall/inventory/outbound/list',
    method: 'post',
    data
  })
}

/**
 * 创建出库单
 */
export function createOutbound(data) {
  return http({
    url: '/web/mall/inventory/outbound/create',
    method: 'post',
    data
  })
}

/**
 * 查看出库单详情
 */
export function getOutboundDetail(data) {
  return http({
    url: '/web/mall/inventory/outbound/detail',
    method: 'post',
    data
  })
}

/**
 * 审核出库单
 */
export function approveOutbound(data) {
  return http({
    url: '/web/mall/inventory/outbound/approve',
    method: 'post',
    data
  })
}

/**
 * 确认出库
 */
export function confirmOutbound(data) {
  return http({
    url: '/web/mall/inventory/outbound/confirm',
    method: 'post',
    data
  })
}

/**
 * 取消出库单
 */
export function cancelOutbound(data) {
  return http({
    url: '/web/mall/inventory/outbound/cancel',
    method: 'post',
    data
  })
}

// =============== 入库管理 ===============

/**
 * 获取入库单列表
 */
export function getInboundList(data) {
  return http({
    url: '/web/mall/inventory/inbound/list',
    method: 'post',
    data
  })
}

/**
 * 创建入库单
 */
export function createInbound(data) {
  return http({
    url: '/web/mall/inventory/inbound/create',
    method: 'post',
    data
  })
}

/**
 * 查看入库单详情
 */
export function getInboundDetail(data) {
  return http({
    url: '/web/mall/inventory/inbound/detail',
    method: 'post',
    data
  })
}

/**
 * 审核入库单
 */
export function approveInbound(data) {
  return http({
    url: '/web/mall/inventory/inbound/approve',
    method: 'post',
    data
  })
}

/**
 * 确认入库
 */
export function confirmInbound(data) {
  return http({
    url: '/web/mall/inventory/inbound/confirm',
    method: 'post',
    data
  })
}

/**
 * 取消入库单
 */
export function cancelInbound(data) {
  return http({
    url: '/web/mall/inventory/inbound/cancel',
    method: 'post',
    data
  })
}
