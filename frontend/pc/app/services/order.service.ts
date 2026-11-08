import type { ApiParams, ApiResponse } from '~/types/api'
import type { CalculatePriceParams, ShopDTO, InvoiceAddress, InvoiceTemplate, InsertInvoiceAddressParams, InsertInvoiceInfoParams } from '~/types/business'
import type {
    CommentsEntity,
    PagedByComments,
    GetCommentsParams,
    InsertShoppingCartParams,
    QueryShoppingCartData,
    InsertPaymentVoucherParams
} from '~/types/order'
import type { QueryPaymentAmountParams, QueryPaymentAmountResult } from '~/types/payment'

const { post, get } = useRequest()

/**
 * 计算订单价格（含商品、运费、优惠等）
 * POST /mall-order/order/calculatePrice
 */
export const calculatePrice = async (params: CalculatePriceParams): Promise<ApiResponse<ShopDTO>> => {
    return await post<ShopDTO>('/mall-order/order/calculatePrice', params)
}

/**
 * 创建订单（App 端提交）
 * POST /mall-order/order/appCreateOrders
 * body：AES 加密后的 Base64 原始字符串（非 JSON）；token 仍由 useRequest 写入请求头
 */
export const createOrders = async (encryptedBody: string): Promise<ApiResponse<any>> => {
    return await post('/mall-order/order/appCreateOrders', encryptedBody, {
        headers: { 'Content-Type': 'text/plain;charset=UTF-8' }
    })
}

// 获取商品评论列表
export const getComments = async (params: GetCommentsParams) => {
    return await post<PagedByComments>('/mall-order/order/goodsEvaluation/getComments', params, { timeout: 8000 })
}

// 加入购物车
export const insertShoppingCart = async (params: InsertShoppingCartParams) => {
    return await post('/web/mall/cart/add', params)
}

// 查询支付金额
export const queryPaymentAmount = async (params: QueryPaymentAmountParams) => {
    return await post<QueryPaymentAmountResult>('/mall-order/order/base/queryAmountPaid', params)
}

// 查询购物车
export const queryShoppingCart = async (params?: ApiParams) => {
    return await post<QueryShoppingCartData>('/web/mall/cart/list', params || {})
}

/**
 * 查询购物车数量
 * @param params 请求参数，传空对象 {} 
 * @returns 返回值 { code, data, message }，code===1 时 data 为数量
 */
export const queryShoppingCartCount = async (params: Record<string, never> = {}) => {
    return await post<{ code: number; data: number; message?: string }>('/web/mall/cart/count', params)
}

// 批量删除购物车商品
export const batchDeleteGoods = async (params: { goodsId: number[] }) => {
    return await post('/web/mall/cart/remove', params)
}

// 修改购物车商品数量
export const changeGoodsNum = async (params: { goodsId: number; goodsNum: number }) => {
    return await post('/web/mall/cart/update', params)
}

/**
 * 对公转账上传支付凭证
 * POST mall-order/order/paymentVocher/insert
 */
export const insertPaymentVoucher = async (params: InsertPaymentVoucherParams) => {
    return await post('/mall-order/order/paymentVocher/insert', params)
}

/**
 * 查询收票地址列表
 * POST /mall-order/order/invoiceAddress/list
 */
export const getInvoiceAddressList = async (params: ApiParams = {}): Promise<ApiResponse<{ list: InvoiceAddress[] }>> => {
    return await post('/mall-order/order/invoiceAddress/list', params)
}

/**
 * 新增或更新收票地址
 * POST /mall-order/order/invoiceAddress/add
 */
export const insertInvoiceAddress = async (params: InsertInvoiceAddressParams): Promise<ApiResponse<any>> => {
    return await post('/mall-order/order/invoiceAddress/add', params)
}

/**
 * 新增或更新发票信息/模板
 * POST /mall-order/order/invoiceTemplateInfo/insert
 */
export const insertInvoiceInfo = async (params: InsertInvoiceInfoParams): Promise<ApiResponse<any>> => {
    return await post('/mall-order/order/invoiceTemplateInfo/insert', params)
}

/**
 * 查询发票信息/模板列表
 * POST /mall-order/order/invoiceTemplateInfo/list
 */
export const getInvoiceInfoList = async (params: ApiParams = {}): Promise<ApiResponse<{ list: InvoiceTemplate[] }>> => {
    return await post('/mall-order/order/invoiceTemplateInfo/list', params)
}
