import request from '@/utils/request'

// 财务中心
export function getFinanceCount() {
    return request.get({ url: '/mall.finance/index' })
}

// 余额明细
export function getBalanceLists(params: any) {
    return request.get({ url: '/mall.shopAccountLog/lists', params })
}

// 退款记录列表
export function getRefundLists(params: any) {
    return request.get({ url: '/mall.Refund/lists', params })
}

// 退款金额统计
export function getRefundMoney(params: any) {
    return request.get({ url: '/mall.Refund/sums', params })
}

// 退退款以及重新退款
export function apiRefundDeal(params: any) {
    return request.post({ url: '/mall.Refund/deal', params })
}
export function apiRefund(params: any) {
    return request.post({ url: '/mall.Refund/re_refund', params })
}

// 提现列表
export function getWithdrawLists(params: any) {
    return request.get({ url: '/mall.shopWithdraw/lists', params })
}

// 添加提现
export function apiWithdrawAdd(params: any) {
    return request.post({ url: '/mall.shopWithdraw/add', params })
}

// 提现详情
export function getWithdrawDetail(params: { id: number }) {
    return request.get({ url: '/mall.shopWithdraw/detail', params })
}

// 结算记录
export function getSettlementLists2(params: { shop_id: number }) {
    return request.get({ url: '/mall.settlement/lists2', params })
}

// 结算记录统计
export function getSettlementCount(params: { shop_id: number }) {
    return request.get({ url: '/mall.settlement/count', params })
}

// 批次详情列表
export function getSettlementRecordLists(params: { settlement_id: number; from_sn: string }) {
    return request.get({ url: '/mall.settlementRecord/lists', params })
}
