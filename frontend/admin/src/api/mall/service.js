import http from '@/utils/http'

/**
 * 售后服务API
 */

// =============== 服务设置 ===============

/**
 * 获取售后政策配置
 */
export function getPolicyConfig(data) {
  return http({
    url: '/web/mall/service/setting/policy/get',
    method: 'post',
    data
  })
}

/**
 * 保存售后政策配置
 */
export function savePolicyConfig(data) {
  return http({
    url: '/web/mall/service/setting/policy/save',
    method: 'post',
    data
  })
}

/**
 * 获取售后原因列表
 */
export function getReasonList(data) {
  return http({
    url: '/web/mall/service/setting/reason/list',
    method: 'post',
    data
  })
}

/**
 * 保存售后原因
 */
export function saveReason(data) {
  return http({
    url: '/web/mall/service/setting/reason/save',
    method: 'post',
    data
  })
}

/**
 * 删除售后原因
 */
export function deleteReason(data) {
  return http({
    url: '/web/mall/service/setting/reason/delete',
    method: 'post',
    data
  })
}

// =============== 服务统计 ===============

/**
 * 获取服务统计概览
 */
export function getStatisticsOverview(data) {
  return http({
    url: '/web/mall/service/statistics/overview',
    method: 'post',
    data
  })
}

/**
 * 获取服务类型统计
 */
export function getTypeStatistics(data) {
  return http({
    url: '/web/mall/service/statistics/type',
    method: 'post',
    data
  })
}

/**
 * 获取满意度统计
 */
export function getSatisfactionStatistics(data) {
  return http({
    url: '/web/mall/service/statistics/satisfaction',
    method: 'post',
    data
  })
}

/**
 * 获取服务统计明细
 */
export function getStatisticsDetail(data) {
  return http({
    url: '/web/mall/service/statistics/detail',
    method: 'post',
    data
  })
}

// =============== 服务记录 ===============

/**
 * 获取服务记录列表
 */
export function getRecordList(data) {
  return http({
    url: '/web/mall/service/record/list',
    method: 'post',
    data
  })
}

/**
 * 获取服务记录详情
 */
export function getRecordDetail(data) {
  return http({
    url: '/web/mall/service/record/detail',
    method: 'post',
    data
  })
}

/**
 * 获取服务进度
 */
export function getRecordTimeline(data) {
  return http({
    url: '/web/mall/service/record/timeline',
    method: 'post',
    data
  })
}

/**
 * 获取服务评价
 */
export function getRecordReview(data) {
  return http({
    url: '/web/mall/service/record/review',
    method: 'post',
    data
  })
}

// =============== 服务工单 ===============

/**
 * 获取工单列表
 */
export function getTicketList(data) {
  return http({
    url: '/web/mall/service/ticket/list',
    method: 'post',
    data
  })
}

/**
 * 获取工单详情
 */
export function getTicketDetail(data) {
  return http({
    url: '/web/mall/service/ticket/detail',
    method: 'post',
    data
  })
}

/**
 * 接单
 */
export function acceptTicket(data) {
  return http({
    url: '/web/mall/service/ticket/accept',
    method: 'post',
    data
  })
}

/**
 * 处理工单
 */
export function processTicket(data) {
  return http({
    url: '/web/mall/service/ticket/process',
    method: 'post',
    data
  })
}

/**
 * 完成工单
 */
export function completeTicket(data) {
  return http({
    url: '/web/mall/service/ticket/complete',
    method: 'post',
    data
  })
}

/**
 * 取消工单
 */
export function cancelTicket(data) {
  return http({
    url: '/web/mall/service/ticket/cancel',
    method: 'post',
    data
  })
}

/**
 * 获取工单统计
 */
export function getTicketCount(data) {
  return http({
    url: '/web/mall/service/ticket/count',
    method: 'post',
    data
  })
}
