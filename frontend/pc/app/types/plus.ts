/**
 * PLUS 会员权益 / 套餐（selectPlusMember）
 */

export interface PlusRedList {
    redpacketTId?: number
    redpacketTOrderlimit?: number
    redpacketTPrice?: number
    redpacketTTitle?: string
    redTermofvalidity?: number
    [property: string]: unknown
}

export interface PlusEquity {
    discountLimit?: number | null
    equityAddtime?: string
    equityId?: number
    equityName?: string
    equityStatus?: number
    /** 权益类型：1折上折 3红包 4积分 5积分倍率等；其余走通用展示（以后端为准） */
    equityType?: number
    equityValue?: string
    redEffective?: unknown
    redList?: PlusRedList[]
    redProvide?: number | null
    token?: unknown
    [property: string]: unknown
}

export interface PlusMemberPackageDatum {
    earnPoints?: unknown
    equityTotal?: number
    logoImg?: string | null
    memberStatus?: unknown
    nextThaliType?: unknown
    plusEquitys?: PlusEquity[]
    propaganda?: string
    recommend?: string
    redAvailable?: unknown
    redTotal?: unknown
    renewalDisplay?: unknown
    thaliEndtime?: unknown
    thaliEquity?: string
    thaliEquityList?: number[]
    thaliId?: number
    thaliOldPrice?: number
    thaliPrice?: number
    /** 是否推荐 0 不推荐 1 推荐 */
    thaliRecommend?: number
    /** 套餐状态 0 关闭 1 启用 */
    thaliStatus?: number
    /** 套餐类型 1 月卡 2 季卡 3 年卡 */
    thaliType?: number
    totalAmount?: unknown
    userAvatar?: unknown
    userName?: string
    [property: string]: unknown
}

/**
 * PLUS 激活页：推荐商品列表项（selectPlusGoods）
 */
export interface PlusRecommendedGoodsItem {
    commonName: string
    commonImage: string
    shopName: string
    commonEvaluate?: number | null
    commonPrice?: number | string | null
    commonMarketPrice?: number | string | null
    commonId?: number | null
    goodsId: number | string
    commonCollect?: number | null
    commonSalenum?: number | null
    shopSelfSupport?: string | null
    plusGoods?: number | null
    [property: string]: unknown
}

/**
 * PLUS 充值记录项（plusInfoOrder/get）
 */
export interface PlusInfoOrderItem {
    userId?: string
    plusOrderid?: string
    payPrice?: number | string | null
    payType?: number | string | null
    startTime?: string | null
    endTime?: string | null
    thaliType?: number | string | null
    creatTime?: string | null
    userName?: string | null
    userMobile?: string | null
    renewStatus?: number | string | null
    [property: string]: unknown
}

/**
 * PLUS 充值记录分页结果
 */
export interface PlusInfoOrderPageData {
    totalCount?: number
    pageSize?: number
    totalPage?: number
    currPage?: number
    list?: PlusInfoOrderItem[]
    [property: string]: unknown
}
