import type { ApiResponse, ApiParams } from '~/types/api'

const { post } = useRequest()

/** 演示站 activity 旧表无货时，统一 POST 拉取商城在售商品（GET 会超时） */
const fetchMallGoodsListsRaw = async (params?: {
    page?: number
    limit?: number
    keyword?: string
    minPrice?: string
    maxPrice?: string
}): Promise<{ code: number; message: string; raw: any[]; totalCount: number }> => {
    const page = Number(params?.page ?? 1) || 1
    const limit = Number(params?.limit ?? 60) || 60
    const payload: Record<string, any> = {
        page,
        pageNum: page,
        size: limit,
        pageSize: limit,
        limit
    }
    const keyword = String(params?.keyword || '').trim()
    if (keyword) {
        payload.name = keyword
        payload.commonName = keyword
        payload.goodsName = keyword
    }
    const minPrice = String(params?.minPrice || '').trim()
    const maxPrice = String(params?.maxPrice || '').trim()
    if (minPrice) payload.minPrice = minPrice
    if (maxPrice) payload.maxPrice = maxPrice
    const res = await post<any>('/web/mall/goods/front/lists', payload)
    const data = res?.data
    const raw = Array.isArray(data?.lists)
        ? data.lists
        : Array.isArray(data?.list)
            ? data.list
            : Array.isArray(data?.goodsList)
                ? data.goodsList
                : []
    return {
        code: Number(res?.code ?? 1) === 1 ? 1 : Number(res?.code ?? 0),
        message: res?.message || '操作成功',
        raw,
        totalCount: Number(data?.count ?? data?.totalCount ?? raw.length) || raw.length
    }
}

const pickGoodsId = (item: any) => Number(item?.goodsId ?? item?.commonId ?? item?.id ?? 0) || 0
const pickGoodsPrice = (item: any) =>
    Number(item?.minPrice ?? item?.goodsPrice ?? item?.commonPrice ?? item?.price ?? 0) || 0
const pickGoodsName = (item: any) => String(item?.goodsName || item?.commonName || item?.name || '')
const pickGoodsImage = (item: any) => String(item?.goodsImage || item?.commonImage || item?.image || '')

/** 将商城商品映射为折扣页 DiscountProduct（约 8.5 折） */
const mapGoodsToDiscountProduct = (item: any) => {
    const id = pickGoodsId(item)
    const price = pickGoodsPrice(item)
    const activityPrice = Math.max(0.01, Math.round(price * 0.85 * 100) / 100)
    const end = new Date()
    end.setDate(end.getDate() + 1)
    end.setHours(23, 59, 59, 0)
    const start = new Date()
    start.setHours(0, 0, 0, 0)
    return {
        commonId: id,
        goodsId: id,
        commonName: pickGoodsName(item),
        commonImage: pickGoodsImage(item),
        commonPrice: price,
        activityPrice,
        discountAmount: Math.max(0, Math.round((price - activityPrice) * 100) / 100),
        commonSalenum: Number(item?.saleNum ?? item?.allSaleNum ?? item?.commonSalenum ?? 0) || 0,
        commonEvaluate: Number(item?.commonEvaluate ?? 0) || 0,
        shopName: String(item?.shopName || item?.shop_name || ''),
        shopId: Number(item?.shopId ?? 0) || 0,
        startTime: start.toISOString().slice(0, 19).replace('T', ' '),
        endTime: end.toISOString().slice(0, 19).replace('T', ' ')
    }
}

/** 首页 DiscountBox：字段含 discountPrice / commonName / commonImage */
const mapGoodsToHomeDiscountProduct = (item: any) => {
    const mapped = mapGoodsToDiscountProduct(item)
    return {
        ...mapped,
        goodsName: mapped.commonName,
        goodsImage: mapped.commonImage,
        goodsPrice: mapped.commonPrice,
        discountPrice: mapped.activityPrice,
        preferentialPrice: mapped.discountAmount,
        tag: '限时折扣'
    }
}

/**
 * 限时折扣商品列表接口（首页模块）
 *
 * 获取限时折扣商品列表
 * @param params 查询参数，包含 limitSize（限制数量）
 * @returns 限时折扣商品列表数据
 */
export const getDiscountProductList = async <T = any>(params?: ApiParams): Promise<ApiResponse<T>> => {
    try {
        const res = await post<T>('/mall-activity/activity/discountGoods/index/limitQuery', params)
        const list = Array.isArray(res?.data) ? res.data : []
        if (res?.code === 1 && list.length > 0) {
            return res
        }
    } catch (e) {
        console.warn('折扣首页接口不可用，回退商城商品列表', e)
    }
    const limit = Number((params as any)?.limitSize ?? 20) || 20
    const fetched = await fetchMallGoodsListsRaw({ page: 1, limit })
    const data = fetched.raw.map(mapGoodsToHomeDiscountProduct) as T
    return {
        code: fetched.code,
        message: fetched.message,
        data
    }
}

/**
 * 查询所有折扣商品接口
 * 
 * 获取限时折扣活动的所有商品列表，支持分页、排序、筛选等
 * @param params 查询参数
 * @param params.token 用户token（可选）
 * @param params.minPrice 最低价格（可选）
 * @param params.maxPrice 最高价格（可选）
 * @param params.orderType 排序类型，3表示综合排序（可选）
 * @param params.priceOrder 价格排序（可选）
 * @param params.saleNum 销量排序（可选）
 * @param params.page 页码，默认1
 * @param params.limit 每页数量，默认60
 * @param params.commentOrder 评价排序，1表示默认（可选）
 * @returns 返回包含totalCount（总数）和list（商品列表）的数据
 */
export const queryAllZheKou = async <T = any>(params?: {
    token?: string | null
    minPrice?: string
    maxPrice?: string
    orderType?: number
    priceOrder?: string | number
    saleNum?: string
    page?: number
    limit?: number
    commentOrder?: number
}): Promise<ApiResponse<{
    totalCount: number
    list: T[]
}>> => {
    try {
        const res = await post<{
            totalCount: number
            list: T[]
        }>('/mall-activity/activity/discountBase/queryAllZheKou', params)
        const list = res?.data?.list
        if (res?.code === 1 && Array.isArray(list) && list.length > 0) {
            return res
        }
    } catch (e) {
        console.warn('折扣活动接口不可用，回退商城商品列表', e)
    }
    const fetched = await fetchMallGoodsListsRaw({
        page: params?.page,
        limit: params?.limit,
        minPrice: params?.minPrice,
        maxPrice: params?.maxPrice
    })
    let list = fetched.raw.map(mapGoodsToDiscountProduct) as T[]
    // 前端轻量排序：活动表无数据时仍响应折扣页「评价/价格」切换
    const orderType = Number(params?.orderType ?? 3)
    if (orderType === 1) {
        const desc = Number(params?.priceOrder) === 1
        list = [...list].sort((a: any, b: any) => {
            const pa = Number(a?.activityPrice ?? 0)
            const pb = Number(b?.activityPrice ?? 0)
            return desc ? pb - pa : pa - pb
        })
    } else if (orderType === 5) {
        const highFirst = Number(params?.commentOrder) === 2
        list = [...list].sort((a: any, b: any) => {
            const ea = Number(a?.commonEvaluate ?? 0)
            const eb = Number(b?.commonEvaluate ?? 0)
            return highFirst ? eb - ea : ea - eb
        })
    }
    return {
        code: fetched.code,
        message: fetched.message,
        data: {
            totalCount: fetched.totalCount,
            list
        }
    }
}

/** 将商城在售商品映射为秒杀页 SeckillProduct 结构（演示站 activity 旧表无数据时的兜底） */
const mapGoodsToSeckillProduct = (item: any, goodsStartTime?: string) => {
    const id = pickGoodsId(item)
    const price = pickGoodsPrice(item)
    const seckillPrice = Math.max(0.01, Math.round(price * 0.7 * 100) / 100)
    const slotStart = goodsStartTime ? new Date(goodsStartTime.replace(/-/g, '/')).getTime() : Date.now()
    const started = !Number.isNaN(slotStart) ? Date.now() >= slotStart : true
    return {
        commonId: id,
        goodsId: id,
        commonName: pickGoodsName(item),
        commonImage: pickGoodsImage(item),
        commonPrice: price,
        seckillPrice,
        commonSalenum: Number(item?.saleNum ?? item?.allSaleNum ?? item?.commonSalenum ?? 0) || 0,
        goodsStartTime: goodsStartTime || '',
        goodsEndTime: '',
        seckillUpperLimit: null,
        goodsUpperLimit: 0,
        seckillNum: Number(item?.totalStock ?? item?.stock ?? 99) || 99,
        commonEvaluate: Number(item?.commonEvaluate ?? 0) || 0,
        seckillGoodsState: 1,
        shopName: String(item?.shopName || item?.shop_name || ''),
        shopId: Number(item?.shopId ?? 0) || 0,
        activityStartStatus: started ? 1 : 0,
        overplusTime: 0,
        cornerUrl: null
    }
}

/**
 * 活动秒杀表无数据时，用商城在售商品兜底，保证 /activity/seckill 有货可展示
 */
const fetchSeckillGoodsFallback = async <T = any>(params?: {
    commonName?: string
    goodsStartTime?: string
    page?: number
    limit?: number
}): Promise<ApiResponse<{
    totalCount: number
    promotionGoodsList: T[]
}>> => {
    const fetched = await fetchMallGoodsListsRaw({
        page: params?.page,
        limit: params?.limit,
        keyword: params?.commonName
    })
    const promotionGoodsList = fetched.raw.map((g: any) =>
        mapGoodsToSeckillProduct(g, params?.goodsStartTime)
    ) as T[]
    return {
        code: fetched.code,
        message: fetched.message,
        data: {
            totalCount: fetched.totalCount,
            promotionGoodsList
        }
    }
}

/**
 * 查询秒杀商品列表接口
 * 
 * 获取限时秒杀活动的商品列表，支持分页、搜索等
 * @param params 查询参数
 * @param params.commonName 商品名称（可选，用于搜索）
 * @param params.goodsStartTime 秒杀开始时间，格式：YYYY-MM-DD HH:mm:ss
 * @param params.page 页码，默认1
 * @param params.limit 每页数量，默认60
 * @returns 返回包含totalCount（总数）和promotionGoodsList（商品列表）的数据
 */
export const querySeckillGoods = async <T = any>(params?: {
    commonName?: string
    goodsStartTime?: string
    page?: number
    limit?: number
}): Promise<ApiResponse<{
    totalCount: number
    promotionGoodsList: T[]
}>> => {
    try {
        const res = await post<{
            totalCount: number
            promotionGoodsList: T[]
        }>('/mall-activity/activity/seckillGoods/seckill/index/query', params)
        const list = res?.data?.promotionGoodsList
        if (res?.code === 1 && Array.isArray(list) && list.length > 0) {
            return res
        }
    } catch (e) {
        console.warn('秒杀活动接口不可用，回退商城商品列表', e)
    }
    return fetchSeckillGoodsFallback<T>(params)
}

/** 秒杀限购校验接口返回 data（与旧 PC 详情一致） */
export interface SeckillCheckLimitData {
    goodsUpperLimit?: number
    buyNum?: number
}

/**
 * 秒杀商品限购校验接口
 *
 * 立即购买秒杀商品前校验是否超过限购
 * @param params.goodsId 商品 SKU ID
 * @param params.commonId SPU commonId
 */
export const seckillCheckLimit = async (
    params: { goodsId: number; commonId: number }
): Promise<ApiResponse<SeckillCheckLimitData>> => {
    return await post('/mall-activity/activity/seckillGoods/seckill/check/limit', params)
}

/**
 * 秒杀开抢预约（未开始场次点击「提醒我」）
 *
 * token 由 useRequest 自动注入请求体，未登录时后端返回「请先登录后再预约」。
 * @param params.goodsId 秒杀商品 goodsId
 * @param params.seckillId 场次 ID（可选，缺省时后端取该商品最近一场未开抢秒杀）
 */
export const seckillSubscribe = async (params: {
    goodsId: number
    seckillId?: number
}): Promise<ApiResponse<any>> => {
    return await post('/mall-activity/activity/seckillSubscribe/add', params)
}

/**
 * 取消秒杀开抢预约
 * @param params.goodsId 秒杀商品 goodsId
 * @param params.seckillId 场次 ID（可选）
 */
export const seckillCancelSubscribe = async (params: {
    goodsId: number
    seckillId?: number
}): Promise<ApiResponse<any>> => {
    return await post('/mall-activity/activity/seckillSubscribe/cancel', params)
}

/**
 * 查询当前用户已预约的秒杀商品 goodsId 列表
 * @param params.seckillId 场次 ID（可选，缺省返回用户全部已预约 goodsId）
 */
export const seckillMySubscribes = async (params?: {
    seckillId?: number
}): Promise<ApiResponse<number[]>> => {
    return await post<number[]>('/mall-activity/activity/seckillSubscribe/my', params ?? {})
}

/**
 * 折扣商品限购校验接口
 *
 * 加入购物车/下单前校验是否超过折扣活动限购
 * @param params.list 折扣校验列表，元素包含 commonId 与 goodsNum
 */
export const discountCheckLimit = async (
    params: { list: Array<{ commonId: number; goodsNum: number }> }
): Promise<ApiResponse<any>> => {
    return await post('/mall-activity/activity/discountGoods/check/limit', params)
}

/**
 * 折扣/秒杀商品批量限购校验（购物车去下单等）
 * @param params.list 每项含 commonId、goodsId、goodsNum
 */
export const checkActivityGoodsLimit = async (
    params: { list: Array<{ commonId: number; goodsId: number; goodsNum: number }> }
): Promise<ApiResponse<unknown>> => {
    return await post('/mall-activity/activity/goods/check/limit', params)
}

/**
 * 查询组合促销列表接口
 * 
 * 获取组合促销活动的商品列表，支持分页、排序、筛选等
 * @param params 查询参数
 * @param params.catParentId 父分类（可选）
 * @param params.currentPage 当前页（可选）
 * @param params.limit 限度（可选）
 * @param params.navProperty 导航（可选）
 * @param params.page 页（可选）
 * @param params.pageSize 页大小（可选）
 * @param params.priceDesc 价格排序 1 降序 2 升序（可选）
 * @param params.status 组合活动状态 0-未开始;1-已开始;2-已结束；3-已失效（可选）
 * @param params.timeDesc 时间排序 1 降序 2 升序（可选）
 * @returns 返回包含totalCount（总数）和list（商品列表）的数据
 */
/**
 * 根据商品ID查询组合活动详情（商品详情页用）
 *
 * @param params.goodsId 商品ID
 * @returns 组合活动列表
 */
export const queryCombinationBaseByPcDetails = async <T = any>(params: {
    goodsId: number
}): Promise<ApiResponse<T[]>> => {
    return await post('/mall-activity/activityCombinationBase/queryCombinationBaseByPcDetails', params)
}

/**
 * 领取优惠券（代金券）接口
 * @param params.voucherTId 优惠券模板ID
 */
export const insertVcBase = async (params: { voucherTId: number | string }): Promise<ApiResponse<any>> => {
    return await post('/mall-activity/activity/voucherBase/insertvcbase', {
        voucherTId: params.voucherTId
    })
}

/** 查询当前用户未使用的优惠券，供 PC 快捷工具栏显示真实数量。 */
export const queryUserVoucherList = async (): Promise<ApiResponse<{ totalCount?: number }>> => {
    return await post('/web/activity/voucher/user/list', { voucherState: 1 })
}

/**
 * 查询可用代金券列表接口（确认订单页选择优惠券）
 * @param params.platForm 平台，默认 1
 * @param params.shopId 店铺ID
 * @param params.goodsInfo 商品信息 [{ goodsId, goodsPrice, goodsNum }]
 */
export const selectAvailableVcBase = async (params: {
    platForm?: number
    shopId: number | string
    goodsInfo: Array<{
        goodsId: string | number
        goodsPrice: string | number
        goodsNum: string | number
    }>
}): Promise<ApiResponse<import('~/types/business').VoucherBaseVO[]>> => {
    return await post('/mall-activity/activity/voucherBase/selectavailablevcbase', {
        platForm: params.platForm ?? 1,
        shopId: String(params.shopId),
        goodsInfo: params.goodsInfo.map(g => ({
            goodsId: String(g.goodsId),
            goodsPrice: String(g.goodsPrice),
            goodsNum: String(g.goodsNum)
        }))
    })
}

/**
 * 查询可用红包列表（确认订单页选择红包）
 */
export const selectRedPacketByDeposit = async (params: {
    platForm: number
    redpacketTId: string
    shopList: Array<{
        shopId: number | string
        voucherTId: string | number | ''
        goodsList: Array<{
            goodsId: string | number
            goodsPrice: string | number
            goodsNumber: string | number
        }>
    }>
    totalPrice: string | number
}): Promise<ApiResponse<import('~/types/activity').RedPacketVO[]>> => {
    return await post('/mall-activity/activity/redpacketBase/selectByDeposit', params)
}

/**
 * 查询平台可领取红包（首页浮标等）
 *
 * @param params.platForm 平台标识，PC 传 1
 * @returns 活动服务标准响应，code === 1 表示有可领红包等成功态（以后端为准）
 */
export const redpacketBaseAllAvailable = async <T = unknown>(params?: {
    platForm?: number
}): Promise<ApiResponse<T>> => {
    return await post('/mall-activity/activity/redpacketBase/allAvaliable', {
        platForm: params?.platForm ?? 1
    })
}

/**
 * 首页一键领取平台红包
 *
 * @param params.platForm 平台标识，PC 传 1
 * @returns code / data / message；code === 1 为成功；data === 0 时前端隐藏浮标（与后端约定一致）
 */
export const redpacketInsertRedAll = async <T = unknown>(params?: {
    platForm?: number
}): Promise<ApiResponse<T>> => {
    return await post('/mall-activity/activity/redpacketBase/insertRed/all', {
        platForm: params?.platForm ?? 1
    })
}

export const queryCombinationPcList = async <T = any>(params?: {
    catParentId?: number
    currentPage?: number
    limit?: number
    navProperty?: number
    page?: number
    pageSize?: number
    priceDesc?: number
    status?: number
    timeDesc?: number
    [property: string]: any
}): Promise<ApiResponse<{
    currPage?: number
    list?: T[]
    pageSize?: number
    totalCount?: number
    totalPage?: number
    [property: string]: any
}>> => {
    return await post('/mall-activity/activityCombinationBase/queryCombinationPcList', params)
}

/**
 * 查询满减赠折商品列表接口
 * POST /mall-activity/activity/reductionPolicyBase/selectAll
 */
export const queryAllManJianZengZhe = async <T = any>(params?: {
    minPrice?: string
    maxPrice?: string
    orderType?: number
    priceOrder?: string
    saleNum?: string
    page?: number
    limit?: number
    commentOrder?: number
}): Promise<ApiResponse<{
    totalCount: number
    list: T[]
}>> => {
    return await post('/mall-activity/activity/reductionPolicyBase/selectAll', params)
}
