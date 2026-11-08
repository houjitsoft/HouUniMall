import request from '@/utils/request'

export type OrderForm = {
    status: number | string //	query	integer否	状态搜索 0待付款 1待发货 2待收货 3已完成 4已取消
    key_word: string //	query	string否	关键字搜索
    key_word_type: string //	query	string否	关键字类型 1 商品名 2 商品货号 3订单编号
    buyer_key_word: string //  query	string否	买家信息关键字搜索
    buyer_key_word_type: string //  query	string否	买家信息类型 1 用户昵称 2 用户编号 3收货人姓名 4 收货人手机
    from: string //	query	string否	搜索来源
    pay_way: string //	query	string否	搜索支付类型
    time_type: string // 	query	string否	搜索时间类型
    start_time: string // 	query	string否	搜索开始时间
    end_time: string // 	query	string否	搜索结束时间
}

/**
 * @return { Promise }
 * @param { OrderForm } OrderForm
 * @description 获取订单列表
 */
export function getOrderLists(params: OrderForm) {
    return request.get({ url: '/mall.order/lists', params })
}

/**
 * @return { Promise }
 * @param { number } id
 * @description 获取订单x详情
 */
export function getOrderDetail(params: { id: number }) {
    return request.get({ url: '/mall.order/detail', params })
}

export type DeliveryType = {
    id?: number | string
    has_express: number // integer是   发货方式 无需物流0，  需要物流1
    express_no: number | string //	string否	物流单号 express_company_id > 0时必须
    express_company_id: number | string //	integer是	快递公司id 无需物流传0 需要物流传快递公司id
}
/**
 * @return { Promise }
 * @param { number } DeliveryType
 * @description 发货
 */
export function apiOrderDelivery(data: DeliveryType) {
    return request.post({ url: '/mall.order/send', data })
}

/**
 * @return { Promise }
 * @param { number } id
 * @description 查看物流
 */
export function apiOrderLogistics(data: { id: number }) {
    return request.post({ url: '/mall.order/logistics', data })
}

/**
 * @return { Promise }
 * @param { number } id
 * @description 取消订单
 */
export function apiOrderCancel(data: { id: number }) {
    return request.post({ url: '/mall.order/cancel', data })
}

/**
 * @return { Promise }
 * @param { number } id
 * @description 确认订单
 */
export function apiOrderConfirm(data: { id: number }) {
    return request.post({ url: '/mall.order/confirm', data })
}

/**
 * @return { Promise }
 * @param { number } id
 * @description 设置商家备注
 */
export function apiOrderRemark(data: { id: number; shop_remark: string }) {
    return request.post({ url: '/mall.order/set_shop_remark', data })
}

/**
 * @return { Promise }
 * @param { number } id
 * @description 发货检测
 */
export function apiAftersale(data: { id: any }) {
    return request.post({ url: '/mall.order/has_after_sale_wait_deal', data })
}
