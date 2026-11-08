import request from '../utils/request'

/**
 * 分销推广接口（mall 原生 /mall/distribution/** → BFF 直通 /web/mall/module/distribution/**）
 *
 * 注意分页键：distributor / commission-record 读 page+size，
 * 与 rebate 系列的 pageNum+pageSize 不同；level 与 rule 的 list 不分页（直接返回数组）。
 */
const BASE = '/web/mall/module/distribution'

export const distributorApi = {
  list: (params) => request.post(`${BASE}/distributor/list`, params),
  detail: (params) => request.post(`${BASE}/distributor/detail`, params),
  apply: (data) => request.post(`${BASE}/distributor/apply`, data),
  // 后台手工建号：不走待审核，可指定邀请码 / 上级 / 等级
  create: (data) => request.post(`${BASE}/distributor/create`, data),
  // 后台编辑：可改姓名 / 手机号 / 邀请码 / 等级 / 上级 / 状态（会同步维护上下级计数）
  edit: (data) => request.post(`${BASE}/distributor/edit`, data),
  audit: (params) => request.post(`${BASE}/distributor/audit`, params),
  updateStatus: (params) => request.post(`${BASE}/distributor/status`, params)
}

export const distributorLevelApi = {
  list: (params) => request.post(`${BASE}/level/list`, params),
  all: (params) => request.post(`${BASE}/level/all`, params),
  detail: (params) => request.post(`${BASE}/level/detail`, params),
  create: (data) => request.post(`${BASE}/level/create`, data),
  update: (data) => request.post(`${BASE}/level/update`, data),
  // 后端 delete 是 POST + body{id}，不是路径删除（别写成 DELETE /delete/{id}）
  remove: (id) => request.post(`${BASE}/level/delete`, { id })
  // 「设为默认」不做：POST /level/default 强制要求 shopId（Long.parseLong，缺了 500），
  // 而 admin 前端没有可靠的当前店铺 ID 来源，硬做只会得到一个必崩的按钮。
}

export const commissionRuleApi = {
  list: (params) => request.post(`${BASE}/rule/list`, params),
  detail: (params) => request.post(`${BASE}/rule/detail`, params),
  create: (data) => request.post(`${BASE}/rule/create`, data),
  update: (data) => request.post(`${BASE}/rule/update`, data),
  remove: (id) => request.post(`${BASE}/rule/delete`, { id }),
  applicable: (params) => request.post(`${BASE}/rule/applicable`, params)
}

export const commissionRecordApi = {
  list: (params) => request.post(`${BASE}/commission/list`, params),
  detail: (params) => request.post(`${BASE}/commission/detail`, params),
  create: (data) => request.post(`${BASE}/commission/create`, data),
  settle: (params) => request.post(`${BASE}/commission/settle`, params),
  batchSettle: (ids) => request.post(`${BASE}/commission/batchSettle`, { ids })
}

export const distributionGoodsApi = {
  list: (params) => request.post(`${BASE}/goods/list`, params),
  enabled: (params) => request.post(`${BASE}/goods/enabled`, params),
  detail: (params) => request.post(`${BASE}/goods/detail`, params),
  byGoods: (params) => request.post(`${BASE}/goods/byGoods`, params),
  create: (data) => request.post(`${BASE}/goods/create`, data),
  update: (data) => request.post(`${BASE}/goods/update`, data),
  updateStatus: (params) => request.post(`${BASE}/goods/status`, params),
  remove: (id) => request.post(`${BASE}/goods/delete`, { id })
}

export const distributionOrderApi = {
  list: (params) => request.post(`${BASE}/order/list`, params),
  detail: (params) => request.post(`${BASE}/order/detail`, params),
  create: (data) => request.post(`${BASE}/order/create`, data),
  update: (data) => request.post(`${BASE}/order/update`, data),
  updateStatus: (params) => request.post(`${BASE}/order/status`, params),
  remove: (id) => request.post(`${BASE}/order/delete`, { id }),
  count: (params) => request.post(`${BASE}/order/count`, params)
}

export const distributionSettingApi = {
  // 分销设置（总开关/层级数/申请方式/绑定/结算/提现门槛），shopId 缺省=平台配置
  detail: () => request.post(`${BASE}/setting/detail`, {}),
  save: (data) => request.post(`${BASE}/setting/save`, data)
}

export const distributionSettleApi = {
  // 佣金结算：一键（生成+结算到期）/ 仅生成 / 仅结算到期 / 概览
  run: (params) => request.post(`${BASE}/settle/run`, params || {}),
  generate: (params) => request.post(`${BASE}/settle/generate`, params || {}),
  settleDue: (params) => request.post(`${BASE}/settle/settle-due`, params || {}),
  stats: (params) => request.post(`${BASE}/settle/stats`, params || {})
}

export const distributionWithdrawApi = {
  // 分销提现：分页 / 申请 / 审核（status 1=通过待打款 2=驳回 3=已打款）
  page: (params) => request.post(`${BASE}/withdraw/page`, params),
  apply: (data) => request.post(`${BASE}/withdraw/apply`, data),
  audit: (params) => request.post(`${BASE}/withdraw/audit`, params)
}
