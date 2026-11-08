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
  const list = data.list || data.records || data.rows || []
  const totalCount = data.totalCount ?? data.total ?? data.count ?? list.length
  return { list, totalCount }
}

/** 活动列表（后台） */
export async function loadPintuanList(params = {}) {
  const data = dataOf(await call('piecingTogether/base/query/list', {
    page: params.page || 1,
    limit: params.pageSize || params.limit || 20,
    pageSize: params.pageSize || params.limit || 20,
    name: params.name || undefined,
    status: params.status ?? undefined
  }))
  return unwrapPage(data)
}

export async function loadPintuanDetail(pintuanId) {
  return dataOf(await call('piecingTogether/query/detail', { pintuanId })) || {}
}

export function savePintuan(payload) {
  return call('piecingTogether/base/add', payload).then(dataOf)
}

export function deletePintuan(pintuanId) {
  return call('piecingTogether/base/delete', { pintuanId }).then(dataOf)
}

export function updatePintuanStatus(pintuanId, status) {
  return call('piecingTogether/base/status', { pintuanId, status }).then(dataOf)
}

/** 团单列表：按活动 commonId 查进行中的团 */
export async function loadTeamList(params = {}) {
  const data = dataOf(await call('piecingTogetherMark/query/list', {
    page: params.page || 1,
    limit: params.pageSize || 20,
    commonId: params.commonId
  }))
  return unwrapPage(data)
}

export async function loadTeamDetail(markId) {
  return dataOf(await call('piecingTogetherMark/join/query/detail', { markId })) || {}
}

/** 商品选择：走商城后台 Core Port */
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
      commonName: row.name,
      commonImage: row.image,
      commonPrice: row.minPrice ?? row.maxPrice,
      commonStock: row.totalStock
    })),
    totalCount: data.count ?? data.total ?? 0
  }
}

export async function loadGoodsSpecs(commonId) {
  const raw = await ctx().callCoreApi('/web/mall/admin/goods/detail', { id: commonId }, { method: 'get' })
  const detail = dataOf(raw) || {}
  const goods = detail.goods || {}
  const skus = Array.isArray(detail.skuList) ? detail.skuList : []
  if (!skus.length) {
    return [{
      commonId,
      goodsId: commonId,
      commonName: goods.name || `商品 ${commonId}`,
      commonImage: goods.image || '',
      specName: '默认规格',
      goodsPrice: goods.minPrice != null ? Number(goods.minPrice) : null
    }]
  }
  return skus.map((sku) => ({
    commonId,
    goodsId: sku.id,
    commonName: goods.name || `商品 ${commonId}`,
    commonImage: sku.image || goods.image || '',
    specName: sku.skuValueArr || sku.code || `SKU ${sku.id}`,
    goodsPrice: sku.price != null ? Number(sku.price) : null
  }))
}

function normalizeConfigRow(row = {}) {
  let goodsIds = []
  const raw = row.configData
  if (Array.isArray(raw)) {
    goodsIds = raw.map((item) => String(item)).filter(Boolean)
  } else if (typeof raw === 'string' && raw.trim()) {
    try {
      const parsed = JSON.parse(raw)
      if (Array.isArray(parsed)) goodsIds = parsed.map((item) => String(item)).filter(Boolean)
    } catch {
      goodsIds = raw.split(',').map((item) => item.trim()).filter(Boolean)
    }
  }
  return {
    id: row.id,
    configType: Number(row.configType ?? 2),
    configName: row.configName || '',
    configTitleOne: row.configTitleOne || '',
    configTitleTwo: row.configTitleTwo || '',
    configData: goodsIds,
    goodsPinTuanResultList: Array.isArray(row.goodsPinTuanResultList) ? row.goodsPinTuanResultList : []
  }
}

/** H5 拼团首页模块配置 */
export async function loadH5Configs() {
  const data = dataOf(await call('piecingTogetherConfig/h5Config/query', {}))
  const list = Array.isArray(data) ? data : (data?.list || [])
  return list.map(normalizeConfigRow)
}

export function saveH5Config(payload) {
  const body = {
    id: payload.id,
    configType: payload.configType,
    configName: payload.configName,
    configTitleOne: payload.configTitleOne,
    configTitleTwo: payload.configTitleTwo,
    configData: JSON.stringify(payload.configData || [])
  }
  const path = payload.id ? 'piecingTogetherConfig/h5Config/update' : 'piecingTogetherConfig/h5Config/modelAdd'
  return call(path, body).then(dataOf)
}

export function deleteH5Config(id) {
  return call('piecingTogetherConfig/h5Config/modelDelete', { id }).then(dataOf)
}

/**
 * 报表汇总：活动列表 + 按 commonId 抽查团单状态。
 * 首版不新增后端接口，最多汇总 30 个商品维度的团单。
 */
export async function loadPintuanReportSummary() {
  const activities = await loadPintuanList({ page: 1, pageSize: 200 })
  const list = activities.list || []
  const activityStats = {
    total: activities.totalCount || list.length,
    enabled: 0,
    disabled: 0,
    pending: 0
  }
  for (const row of list) {
    const status = Number(row.status)
    if (status === 1) activityStats.enabled += 1
    else if (status === 2) activityStats.pending += 1
    else activityStats.disabled += 1
  }

  const teamStats = {
    total: 0,
    ongoing: 0,
    formed: 0,
    failed: 0,
    refunded: 0,
    sampledCommonIds: 0
  }
  const commonIds = [...new Set(list.map((row) => row.commonId).filter((id) => id != null))]
  const sample = commonIds.slice(0, 30)
  teamStats.sampledCommonIds = sample.length
  await Promise.all(sample.map(async (commonId) => {
    try {
      const teams = await loadTeamList({ commonId, page: 1, pageSize: 100 })
      for (const team of teams.list || []) {
        teamStats.total += 1
        const status = Number(team.status)
        if (status === 1) teamStats.formed += 1
        else if (status === 2) teamStats.failed += 1
        else if (status === 3) teamStats.refunded += 1
        else teamStats.ongoing += 1
      }
    } catch {
      // 单商品团单失败不阻断整页
    }
  }))

  return {
    activityStats,
    teamStats,
    recentActivities: list.slice(0, 10)
  }
}
