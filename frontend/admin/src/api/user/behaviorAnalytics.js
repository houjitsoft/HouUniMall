/**
 * 用户行为与留存分析 API
 *
 * 数据来源：userms 服务按时间范围实时聚合真实库表（统一埋点 / 登录日志 /
 * 商品足迹 / 订单 / 会员），经 BFF 转发（/web/member/behavior/**）。
 * 四维度：行为频次、次日/7日/30日留存、DAU/WAU/MAU、注册到首购漏斗。
 * 接口不做任何模拟数据，未采集维度由 dataQuality 显式标注。
 *
 * @author Amos
 */

import request from '@/utils/request'

const BASE = '/web/member/behavior'

/**
 * 四维度分析
 *
 * @param {Object} params
 * @param {string} [params.startDate] 起始日期 yyyy-MM-dd，闭区间
 * @param {string} [params.endDate]   结束日期 yyyy-MM-dd，闭区间
 * @returns {Promise<Object>} { range, dataQuality, behavior, active, retention, funnel }
 */
export function fetchBehaviorAnalytics(params = {}) {
  return request.post(`${BASE}/analytics`, params)
}

/**
 * 统一埋点上报（供各端把搜索 / 加购 / 分享等事件写入 hou_user_behavior_event）
 *
 * @param {Object} params userId / eventType / eventTime / source / refId / ext
 */
export function trackBehaviorEvent(params = {}) {
  return request.post(`${BASE}/track`, params)
}

export default {
  fetchBehaviorAnalytics,
  trackBehaviorEvent
}
