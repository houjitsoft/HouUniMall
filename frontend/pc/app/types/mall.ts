/**
 * 商城相关类型定义
 */

/** 导航Logo和文案配置类型定义 */
export interface NavLogoConfig {
    configKey: string
    configValue: string
    configType?: string
    [key: string]: any
}

/** 特色馆轮播图单项 */
export interface ExhibitionSlideItem {
    opType?: string
    configValue: string
    opValue?: string
    configKey?: string
    configComment?: string
    [key: string]: any
}

/** 特色馆推荐商品项（与 ArticleVO 兼容） */
export interface ExhibitionGoodsItem {
    commonId: number
    goodsId: number
    goodsName: string
    goodsPrice: number
    goodsMarketPrice?: number
    goodsImage: string
    shopName?: string
    shopId?: number
    goodsSalenum?: number
    commonIsXian?: number
    [key: string]: any
}

/** 精品特色馆 */
export interface RecommendHall {
    exhibitionHallId: number
    exhibitionHallLogo?: string
    goodsId?: string
    goodsList: ExhibitionGoodsItem[]
    [key: string]: any
}

/** 地方特色馆单项 */
export interface AllHallItem {
    exhibitionHallId: number
    exhibitionHallName?: string
    exhibitionHallArea?: string | null
    exhibitionHallImage?: string
    exhibitionHallLogo?: string | null
    exhibitionHallLogoWap?: string | null
    exhibitionHallDesc1?: string | null
    exhibitionHallDesc2?: string | null
    exhibitionHallDesc3?: string | null
    goodsId?: string | null
    publishTime?: string | null
    exhibitionHallStatus?: number | null
    [key: string]: any
}

/** 特色馆首页接口返回 */
export interface ExhibitionIndexData {
    /** 轮播图列表 */
    slides: ExhibitionSlideItem[]
    /** 精品特色馆（数组，横向排列，每项含 exhibitionHallLogo + goodsList） */
    recommendHall: RecommendHall[] | RecommendHall | null
    /** 地方特色馆（单项或数组，接口可能返回多个） */
    allHall: AllHallItem | AllHallItem[] | null
}

/** 地方特色馆详情 - 商品项（commonList 单项） */
export interface HallDetailCommonItem {
    commonId: number
    goodsId: number
    commonName: string
    commonPrice: number
    commonSalenum?: number
    commonMarketPrice?: number
    commonImage: string
    commonIsXian?: number
    activityStatus?: number | null
    activityGoodsDetail?: unknown
    cornerUrl?: string | null
    [key: string]: any
}

/** 地方特色馆详情接口返回 data */
export interface HallDetailData {
    exhibitionHallDesc1?: string | null
    exhibitionHallDesc2?: string | null
    exhibitionHallDesc3?: string | null
    exhibitionHallName?: string
    commonList: HallDetailCommonItem[]
}

/** 基础货币符号实体（queryCountryAndCurrency 接口 baseCurrencySymbolEntities 单项） */
export interface BaseCurrencySymbolEntity {
    id: number
    currencySymbol: string
    remark: string
    exchangeRate: number
    status: number
    currencyCode: string
    [key: string]: any
}
