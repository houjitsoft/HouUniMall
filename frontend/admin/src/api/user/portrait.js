/**
 * 会员画像 API
 *
 * 数据来源：userms 服务按会员聚合真实库表数据（会员/订单/登录/足迹/手工标签），
 * 经 BFF 转发（/web/member/portrait/**）。接口不做任何模拟数据。
 *
 * @author Amos
 */

import request from '@/utils/request'

const BASE = '/web/member/portrait'

/**
 * 会员画像分页
 *
 * @param {Object} params
 * @param {string} [params.keyword]   昵称 / 手机号 / 用户ID
 * @param {string} [params.layer]     core / growth / edge
 * @param {number} [params.minOrders] 最少订单数
 * @param {number} [params.page]      页码，从 1 开始
 * @param {number} [params.size]      每页条数
 * @returns {Promise<Object>} { records, total, current, size, summary }
 */
export function fetchMemberPortraitPage(params = {}) {
  return request.post(`${BASE}/page`, params)
}

/**
 * 单个会员画像详情（含偏好、价格带、行为时间线）
 *
 * @param {string|number} userId 会员ID
 * @returns {Promise<Object>}
 */
export function fetchMemberPortraitDetail(userId) {
  return request.post(`${BASE}/detail`, { userId })
}

/**
 * RFM 八分群矩阵
 *
 * @returns {Promise<Object>} { segments, quadrant, total, tradedTotal, rule }
 */
export function fetchMemberPortraitRfm() {
  return request.post(`${BASE}/rfm`, {})
}

/**
 * 智能分群：按条件实时圈人
 *
 * @param {Object} params 圈人条件（layer / rfmSegment / lifecycle / minOrders / maxOrders /
 *                        minAmount / maxAmount / minPending / inactiveDays / hasOrder / hasTag / tagIds）
 * @returns {Promise<Object>} { total, records, ruleText, allMemberTotal }
 */
export function previewMemberSegment(params = {}) {
  return request.post(`${BASE}/segment/preview`, params)
}

/**
 * 保存人群包
 *
 * @param {Object} params 圈人条件 + crowdName / remark
 * @returns {Promise<Object>} { id, memberCount }
 */
export function saveMemberCrowd(params = {}) {
  return request.post(`${BASE}/crowd/save`, params)
}

/**
 * 人群包列表
 *
 * @returns {Promise<Object>}
 */
export function fetchMemberCrowdList() {
  return request.post(`${BASE}/crowd/list`, {})
}

/**
 * 删除人群包
 *
 * @param {number|string} id
 * @returns {Promise<Object>}
 */
export function deleteMemberCrowd(id) {
  return request.post(`${BASE}/crowd/delete`, { id })
}

/**
 * 标签字典（含每个标签覆盖人数）
 *
 * @returns {Promise<Object>} { list, total, usedTagCount }
 */
export function fetchMemberTagList() {
  return request.post(`${BASE}/tag/list`, {})
}

/**
 * 新建 / 更新标签
 *
 * @param {Object} params tagId / tagName / tagWeight / tagDescribe / tagIsactive
 * @returns {Promise<Object>}
 */
export function saveMemberTag(params = {}) {
  return request.post(`${BASE}/tag/save`, params)
}

/**
 * 删除标签（置为停用）
 *
 * @param {number} tagId
 * @returns {Promise<Object>}
 */
export function deleteMemberTag(tagId) {
  return request.post(`${BASE}/tag/delete`, { tagId })
}

/**
 * 批量打标
 *
 * @param {number} tagId
 * @param {Array<number|string>} userIds
 * @returns {Promise<Object>} { affected }
 */
export function attachMemberTag(tagId, userIds) {
  return request.post(`${BASE}/tag/attach`, { tagId, userIds })
}

/**
 * 批量取消打标
 *
 * @param {number} tagId
 * @param {Array<number|string>} userIds
 * @returns {Promise<Object>} { affected }
 */
export function detachMemberTag(tagId, userIds) {
  return request.post(`${BASE}/tag/detach`, { tagId, userIds })
}

export default {
  fetchMemberPortraitPage,
  fetchMemberPortraitDetail,
  fetchMemberPortraitRfm,
  previewMemberSegment,
  saveMemberCrowd,
  fetchMemberCrowdList,
  deleteMemberCrowd,
  fetchMemberTagList,
  saveMemberTag,
  deleteMemberTag,
  attachMemberTag,
  detachMemberTag
}
