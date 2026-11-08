import { getPluginContext } from './context.js'
import { getToken } from '@/utils/auth.js'

function call(path, body = {}, options = {}) {
  return getPluginContext().callApi(path, { ...body, token: getToken() || '' }, options)
}

/**
 * 统一解包：后端 ResponseResult 为 { code, msg, data }。
 * code !== 1 时抛出可读错误，避免页面拿空数据装作成功。
 */
export function dataOf(response, fallbackMessage = '操作失败') {
  if (response == null) return response
  if (typeof response !== 'object') return response
  if (Object.hasOwn(response, 'code') && Number(response.code) !== 1) {
    throw new Error(response.msg || response.message || fallbackMessage)
  }
  return Object.hasOwn(response, 'data') ? response.data : response
}

function pageOf(data) {
  const list = Array.isArray(data?.list) ? data.list : []
  return { list, totalCount: Number(data?.totalCount) || 0 }
}

/* ------------------------------ 活动 ------------------------------ */

/** 抽奖活动分页列表 */
export async function loadActivities(params = {}) {
  const data = dataOf(
    await call('spinwin/query/list', {
      page: params.page || 1,
      limit: params.limit || 20,
      activityTitle: params.activityTitle || undefined,
      activityIsEnable: params.activityIsEnable ?? undefined
    }),
    '加载抽奖活动失败'
  )
  return pageOf(data)
}

/** 活动下拉：一次拉取足够多条（管理端活动量级不大，避免分页器跨页选择） */
export async function loadAllActivities() {
  const data = dataOf(
    await call('spinwin/query/list', { page: 1, limit: 200 }),
    '加载抽奖活动失败'
  )
  return Array.isArray(data?.list) ? data.list : []
}

export async function loadActivityDetail(activityId) {
  return dataOf(await call('spinwin/query/detail', { activityId }), '加载活动详情失败') || {}
}

export function saveActivity(payload) {
  return call('spinwin/update', payload).then((res) => dataOf(res, '保存活动失败'))
}

export function deleteActivity(activityId) {
  return call('spinwin/delete', { activityId }).then((res) => dataOf(res, '删除活动失败'))
}

/** 抽奖次数限制 */
export async function loadActivityLimit(activityId) {
  return dataOf(await call('spinwin/limit/query', { activityId }), '加载抽奖规则失败') || {}
}

export function saveActivityLimit(payload) {
  return call('spinwin/limit/update', payload).then((res) => dataOf(res, '保存抽奖规则失败'))
}

/* ------------------------------ 奖项 ------------------------------ */

export async function loadPrizes(activityId) {
  if (!activityId) return []
  const data = dataOf(
    await call('spinwinPrize/query/list', { activityId }),
    '加载奖项失败'
  )
  return Array.isArray(data) ? data : []
}

export function savePrize(payload) {
  return call('spinwinPrize/update', payload).then((res) => dataOf(res, '保存奖项失败'))
}

export function deletePrize(prizeId) {
  return call('spinwinPrize/delete', { prizeId }).then((res) => dataOf(res, '删除奖项失败'))
}

/* ------------------------------ 礼品 ------------------------------ */

export async function loadGifts(params = {}) {
  const data = dataOf(
    await call('spinwinGift/query/list', {
      page: params.page || 1,
      limit: params.limit || 20,
      giftName: params.giftName || undefined
    }),
    '加载礼品失败'
  )
  return pageOf(data)
}

/** 礼品下拉（奖项关联礼品用） */
export async function loadAllGifts() {
  const data = dataOf(
    await call('spinwinGift/query/list', { page: 1, limit: 200 }),
    '加载礼品失败'
  )
  return Array.isArray(data?.list) ? data.list : []
}

export function saveGift(payload) {
  // 后端以 giftId 是否为空串判断新增，必须显式传 ''（不能是 undefined）
  return call('spinwinGift/update', { ...payload, giftId: payload.giftId || '' })
    .then((res) => dataOf(res, '保存礼品失败'))
}

export function deleteGift(giftId) {
  return call('spinwinGift/delete', { giftId }).then((res) => dataOf(res, '删除礼品失败'))
}

/* --------------------------- 抽奖记录 / 参与用户 --------------------------- */

/** 抽奖记录（含奖项与用户） */
export async function loadDrawRecords(params = {}) {
  const data = dataOf(
    await call('spinwinPerson/query/list', {
      page: params.page || 1,
      limit: params.limit || 20,
      userName: params.userName || undefined,
      activityTitle: params.activityTitle || undefined,
      activityId: params.activityId || undefined
    }),
    '加载抽奖记录失败'
  )
  return pageOf(data)
}

/** 用户抽奖次数明细 */
export async function loadDrawTimes(params = {}) {
  const data = dataOf(
    await call('spinwinLuckyDrawTimes/list', {
      page: params.page || 1,
      limit: params.limit || 20,
      activityId: params.activityId || undefined,
      userName: params.userName || undefined
    }),
    '加载参与用户失败'
  )
  return pageOf(data)
}
