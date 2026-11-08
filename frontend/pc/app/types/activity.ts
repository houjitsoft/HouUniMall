/**
 * 活动相关类型定义
 */

/**
 * 可用红包 VO（selectByDeposit 接口返回）
 */
export interface RedPacketVO {
    redpacketId?: number
    redpacketTId?: number
    redpacketTitle?: string
    redpacketDesc?: string
    redpacketStartDate?: string
    redpacketEndDate?: string
    redpacketPrice?: number
    redpacketOwnerId?: string
    avaliableRp?: number
    goodsCatLimit?: string
    shopLimit?: string
    shopId?: string
    redpacketTType?: number
    redpacketTOrderlimit?: number
    redpacketState?: number
    redpacketUsePlatform?: string
    [property: string]: any
}

/**
 * 组合促销商品数据类型
 */
export interface CombinationGoodsDTO {
    combinationId?: number
    combinationType?: number
    commonId?: number
    goodsCode?: string
    goodsId?: number
    goodsImage?: string
    goodsName?: string
    goodsNum?: number
    goodsPrice?: number
    goodsSpec?: string
    goodsStock?: number
    isMainProduct?: number
    matchingPrices?: number
    shopId?: number
}

/**
 * 组合活动详情（商品详情页接口返回）
 */
export interface CombinationBaseVO {
    combinationId?: number
    combinationImage?: string
    combinationName?: string
    combinationPrice?: number
    combinationStatus?: number
    combinationType?: number
    endTime?: string
    goodsId?: number
    goodsList?: CombinationGoodsDTO[]
    goodsName?: string
    goodsNum?: number
    goodsOriginalPrice?: number
    goodsPrice?: number
    goodsSpec?: string
    goodsStock?: number
    matchingPrices?: number
    maxBuyNum?: number
    startTime?: string
    transportAreaId?: number
    transportAreaName?: string
    transportTemplateId?: number
    transportTemplateName?: string
    [key: string]: any
}

/**
 * 组合促销PC端数据类型
 */
export interface CombinationPcVO {
    combinationId?: number
    combinationImage?: string
    combinationName?: string
    combinationPrice?: number
    combinationStatus?: number
    combinationType?: number
    endTime?: string
    goodsList?: CombinationGoodsDTO[]
    goodsNum?: number
    goodsOriginalPrice?: number
    goodsStock?: number
    invoiceActive?: number
    maxBuyNum?: number
    shopId?: number
    shopInvoices?: number
    shopName?: string
    startTime?: string
}

/**
 * 满减增折商品数据类型
 */
export interface ManJianZengZheProduct {
    commonName: string
    commonSalenum: number
    goodsId: string
    shopName: string
    commonMarketPrice: number
    commonPrice: string | number
    commonEvaluate: number
    goodsPrice: string | number
    commonId: number
    startTime: string
    shopId: number
    endTime: string
    commonImage: string
}

/**
 * 折扣商品数据类型
 */
export interface DiscountProduct {
    commonName: string
    commonSalenum: number
    goodsId: number
    shopName: string
    discountAmount: number
    commonPrice: string | number
    activityPrice: string | number
    commonEvaluate: number
    commonId: number
    startTime: string
    shopId: number
    endTime: string
    commonImage: string
}

/**
 * 秒杀商品数据类型
 */
export interface SeckillProduct {
    commonId: number
    goodsId: number
    commonName: string
    commonImage: string
    commonPrice: string | number
    seckillPrice: string | number
    commonSalenum: string | number
    goodsStartTime: string
    goodsEndTime: string
    seckillUpperLimit: number | null
    goodsUpperLimit: number
    seckillNum: number
    commonEvaluate: number
    seckillGoodsState: number // 1: 可抢购, 其他: 已抢完等
    shopName: string
    shopId: number
    activityStartStatus: number // 1: 进行中, 0: 未开始
    overplusTime: number // 剩余时间（毫秒）
    cornerUrl: string | null
}

/**
 * 时间场次类型
 */
export interface TimeSlot {
    startTime: string
    timeText: string
    status: 'active' | 'coming' | 'ended'
    statusText: string
}

/**
 * 排序类型
 */
export type SortType = 'comprehensive' | 'price' | 'comment'
