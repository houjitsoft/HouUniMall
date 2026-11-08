import request from '@/utils/request'

export type AfterSaleType = {
    status: number | string //	query	integer否	状态搜索 0 申请中 10 等待退款 20 售后成功 30 售后失败
    goods_name: string //	query	string否	商品名称
    key_word: string //  query	string否	关键字搜索
    key_word_type: string //  query	string否	订单信息类型 1 订单编号 2 用户编号 3用户昵称 4收件人姓名 5收件人手机号 6售后单号
    order_from: string //	query	string否	搜索来源
    type: string //	query	string否	退款方式
    start_time: string // 	query	string否	搜索开始时间
    end_time: string // 	query	string否	搜索结束时间
}
/**
 * @return { Promise }
 * @param { OrderForm } AfterSaleType
 * @description 获取售后列表
 */
export function getAfterSaleLists(params: AfterSaleType) {
    return request.get({ url: '/mall.orderAfterSale/lists', params })
}

/**
 * @return { Promise }
 * @param { number } id
 * @description 获取售后详情
 */
export function getAfterSaleDetail(params: { id: number }) {
    return request.get({ url: '/mall.orderAfterSale/detail', params })
}

/**
 * @return { Promise }
 * @param { number } id
 * @description 审核售后
 */
export function apiAfterSaleAudit(data: { id: number; agree: number; refuse_content?: string }) {
    return request.post({ url: '/mall.orderAfterSale/audit', data })
}
export function goodsReceive(data: { id: number; agree: number; refuse_content?: string }) {
    return request.post({ url: '/mall.orderAfterSale/goodsReceive', data })
}
export function acceptgoodsReceive(data: { id: number; agree: number; refuse_content?: string }) {
    return request.post({ url: '/mall.orderAfterSale/deal', data })
}

/**
 * @return { Promise }
 * @param { number } id
 * @description 审核售后
 */
export function apiAfterSaleRefund(data: { id: number }) {
    return request.post({ url: '/mall.orderAfterSale/refund', data })
}
