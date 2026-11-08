import { getPluginContext } from './context.js'
import { getToken } from '@/utils/auth.js'

function call(path, body = {}, options = {}) {
  return getPluginContext().callApi(path, { ...body, token: getToken() || '' }, options)
}

export function dataOf(response) {
  if (response && typeof response === 'object' && Object.hasOwn(response, 'data')) return response.data
  return response
}

export async function loadSeckillList(params = {}) {
  const data = dataOf(await call('SeckillBase/query/list', {
    page: params.page || 1,
    limit: params.limit || 20,
    seckillName: params.seckillName || undefined,
    seckillState: params.seckillState ?? undefined
  }))
  return data || { totalCount: 0, list: [] }
}

export async function loadSeckillDetail(seckillId) {
  return dataOf(await call('SeckillBase/base/query', { seckillId })) || {}
}

export function saveSeckill(payload) {
  return call('SeckillBase/add', payload).then(dataOf)
}

export function deleteSeckill(seckillId) {
  return call('SeckillBase/delete', { seckillId }).then(dataOf)
}

/** 可参与秒杀的商品（在售、未挂其他活动），common 级 */
export async function loadGoodsOptions(params = {}) {
  const data = dataOf(await call('seckillGoods/query/list', {
    page: params.page || 1,
    limit: params.limit || 20,
    commonName: params.commonName || undefined
  }))
  return data || { totalCount: 0, list: [] }
}

/** 某商品（commonId）下的全部规格，用于逐规格定价 */
export async function loadGoodsSpecs(commonId) {
  return dataOf(await call('seckillGoods/specOfCommon/query', { commonId })) || []
}

/** 某场次下已配置的秒杀商品（规格级，含规格名与秒杀价） */
export async function loadSeckillGoods(seckillId) {
  return dataOf(await call('seckillGoods/spec/query', { seckillId })) || []
}

/** 移除此场次下的某个商品（连同其全部规格） */
export function removeSeckillGoods(seckillId, commonId) {
  return call('seckillGoods/delete', { seckillId, commonId }).then(dataOf)
}

/** 某场次各秒杀商品的预约人数（返回 { [goodsId]: count }） */
export async function loadSeckillSubscribeCount(seckillId) {
  return dataOf(await call('seckillSubscribe/count', { seckillId })) || {}
}

/* ---------------- 千客千价价格策略 ---------------- */

/** 分页查询价格策略（返回 { records, total, page, size }） */
export async function loadPriceList(params = {}) {
  const data = dataOf(await call('thousandsGuestsPrice/config/page', {
    page: params.page || 1,
    size: params.size || 10,
    goodsId: params.goodsId ?? undefined,
    segmentType: params.segmentType ?? undefined,
    status: params.status ?? undefined
  }))
  return data || { records: [], total: 0 }
}

/** 新增或更新价格策略（startTime/endTime 传毫秒时间戳） */
export function savePriceConfig(payload) {
  return call('thousandsGuestsPrice/config/save', payload).then(dataOf)
}

/** 更新启用状态：status 0-禁用 1-启用 */
export function updatePriceStatus(id, status) {
  return call('thousandsGuestsPrice/config/status/update', { id, status }).then(dataOf)
}

/** 删除价格策略 */
export function deletePriceConfig(id) {
  return call('thousandsGuestsPrice/config/delete', { id }).then(dataOf)
}
