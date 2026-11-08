import { getPluginContext } from './context.js'
import { getToken } from '@/utils/auth.js'

function ctx() {
  return getPluginContext()
}

function call(path, body = {}, options = {}) {
  return ctx().callApi(path, { ...body, token: getToken() || '' }, options)
}

export function dataOf(response) {
  if (response && typeof response === 'object' && Object.hasOwn(response, 'data')) return response.data
  return response
}

function unwrapPage(data) {
  if (!data || typeof data !== 'object') return { list: [], totalCount: 0 }
  const list = data.list || data.records || data.rows || data.discountBaseList || []
  const totalCount = data.totalCount ?? data.total ?? data.count ?? list.length
  return { list, totalCount }
}

export async function loadDiscounts(params = {}) {
  const data = dataOf(await call('discountBase/platform/queryList', {
    page: params.page || 1,
    limit: params.pageSize || 20,
    discountName: params.discountName || undefined,
    discountState: params.discountState ?? undefined
  }))
  return unwrapPage(data)
}

export async function loadDiscountDetail(discountId) {
  return dataOf(await call('discountBase/query/detail', {
    discountId,
    page: 1,
    limit: 200
  })) || {}
}

export function saveDiscount(payload) {
  return call('discountBase/add', payload).then(dataOf)
}

export function deleteDiscount(discountId) {
  return call('discountBase/platform/delete', { discountId }).then(dataOf)
}

export function invalidateDiscount(discountId) {
  return call('discountGoods/inactive', { discountId }).then(dataOf)
}

export async function loadGoodsOptions(params = {}) {
  const raw = await ctx().callCoreApi('/web/mall/admin/goods/lists', {
    pageNo: params.page || 1,
    pageSize: params.pageSize || 10,
    name: params.keyword || undefined,
    type: 'sale'
  })
  const data = dataOf(raw) || {}
  return {
    list: (data.lists || data.list || []).map((row) => ({
      commonId: row.id,
      goodsId: row.id,
      goodsName: row.name,
      goodsImage: row.image,
      goodsPrice: row.minPrice ?? row.maxPrice,
      commonStock: row.totalStock
    })),
    totalCount: data.count ?? data.total ?? 0
  }
}
