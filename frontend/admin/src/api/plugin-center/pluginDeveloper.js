/**
 * 插件开发者中心 / 结算分账 API
 *
 * 对接 base 插件中心 `/base/plugin-center/developer/**` 与 `/base/plugin-center/settlement/**`，
 * 经 BFF 暴露为 `/web/plugin-center/...`。
 *
 * 权限口径：`/apply`、`/mine`、`/account/save`、`/settlement/*` 按登录态识别开发者本人；
 * `/list`、`/review`、`/freeze`、`/batch/*` 需要插件管理权限（运营）。
 */
import request from '@/utils/request'

const DEVELOPER_URL = '/web/plugin-center/developer'
const SETTLEMENT_URL = '/web/plugin-center/settlement'

const dataOf = (response) => response?.data ?? response

/** 提交/更新开发者申请：{displayName, contactEmail, contactPhone, company, taxNo, intro} */
export const applyDeveloper = async (payload) => dataOf(await request.post(`${DEVELOPER_URL}/apply`, payload))

/** 我的开发者身份 + 收款账户：{developer, account} */
export const getMyDeveloper = async () => dataOf(await request.post(`${DEVELOPER_URL}/mine`, {}))

/** 保存收款账户：{settleType, accountName, accountNo, bankName} */
export const saveSellerAccount = async (payload) =>
  dataOf(await request.post(`${DEVELOPER_URL}/account/save`, payload))

/** 开发者列表（运营）：{list, total} */
export const listDevelopers = async (status) =>
  dataOf(await request.post(`${DEVELOPER_URL}/list`, status == null ? {} : { status }))

/** 审核（运营）：approve=false 时 note 必填 */
export const reviewDeveloper = async ({ id, approve, note }) =>
  dataOf(await request.post(`${DEVELOPER_URL}/review`, { id, approve, note }))

/** 冻结/解冻（运营） */
export const freezeDeveloper = async ({ id, frozen, note }) =>
  dataOf(await request.post(`${DEVELOPER_URL}/freeze`, { id, frozen, note }))

/** 收入汇总：{pendingAmount, settledAmount, monthAmount, commissionRate} */
export const getSettlementSummary = async (developerId) =>
  dataOf(await request.post(`${SETTLEMENT_URL}/summary`, developerId ? { developerId } : {}))

/** 分账流水：{list, total, summary} */
export const listRevenueShares = async ({ developerId, status, limit } = {}) =>
  dataOf(await request.post(`${SETTLEMENT_URL}/shares`, { developerId, status, limit }))

/** 结算批次列表：{list, total} */
export const listSettlementBatches = async ({ developerId, status } = {}) =>
  dataOf(await request.post(`${SETTLEMENT_URL}/batch/list`, { developerId, status }))

/** 生成结算批次（运营）：{developerId, from, to, remark} */
export const createSettlementBatch = async (payload) =>
  dataOf(await request.post(`${SETTLEMENT_URL}/batch/create`, payload))

/** 标记打款（运营）：{id, voucherUrl} */
export const markBatchPaid = async ({ id, voucherUrl }) =>
  dataOf(await request.post(`${SETTLEMENT_URL}/batch/paid`, { id, voucherUrl }))

/** 驳回单笔流水（运营）：{id, remark} */
export const rejectRevenueShare = async ({ id, remark }) =>
  dataOf(await request.post(`${SETTLEMENT_URL}/share/reject`, { id, remark }))

/** 开发者状态文案 */
export const DEVELOPER_STATUS = {
  0: { text: '待审核', type: 'warning' },
  1: { text: '正常', type: 'success' },
  2: { text: '已冻结', type: 'info' },
  3: { text: '已驳回', type: 'danger' }
}

export const developerStatusOf = (status) => DEVELOPER_STATUS[status] || { text: '未知', type: 'info' }

/** 分账/批次状态文案 */
export const SHARE_STATUS = {
  0: { text: '待结算', type: 'warning' },
  1: { text: '已结算', type: 'success' },
  2: { text: '已驳回', type: 'danger' }
}

export const BATCH_STATUS = {
  0: { text: '待打款', type: 'warning' },
  1: { text: '已打款', type: 'success' },
  2: { text: '已关闭', type: 'info' }
}

export const shareStatusOf = (status) => SHARE_STATUS[status] || { text: '未知', type: 'info' }
export const batchStatusOf = (status) => BATCH_STATUS[status] || { text: '未知', type: 'info' }
