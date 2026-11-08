/**
 * Store 状态相关类型定义
 */

import type { UserInfo } from './user'
import type { GoodsList, ShopList } from './goods'
import type { NavLogoConfig, BaseCurrencySymbolEntity } from './mall'
import type { ActivityGoodsDetailDTO, ManjianzengzheGoodsRuleVO } from './business'

/** 用户状态类型定义 */
export interface UserState {
    userInfo: UserInfo | null
    token: string | null
    isLogin: boolean
}

/** 商品状态类型定义 */
export interface GoodsState {
    shopList: ShopList[]
    /** 购物车角标数量（已登录时由 queryShoppingCartCount 拉取，添加/删除购物车后需调用 fetchCartCount 刷新） */
    cartCount: number
}

/** 商城状态类型定义 */
export interface MallState {
    navLogoConfigs: NavLogoConfig[]
    /** 当前启用的货币符号（status===1 仅有一项），来自 queryCountryAndCurrency.baseCurrencySymbolEntities */
    baseCurrencySymbolEntity: BaseCurrencySymbolEntity | null
    /** 当前选中的语言编码（如 en、jp、ru），用于 PC 端语言持久化 */
    languageCode: string | null
    /** 首页平台红包是否被手动关闭 */
    homePlatformRedpacketClosed: boolean
    /** 首页平台红包浮标是否展示（由 allAvaliable 等接口结果驱动，不持久化） */
    homePlatformRedpacketEntryVisible: boolean
}

/** 文章状态类型定义 */
export interface ArticleState {
    articleGroupId: number | null
    articleId: number | null // 文章ID，用于初始化回显
}

/** 对比商品类型 */
export interface ComparisonItem {
    goodsId: number
    goodsName: string
    goodsImage?: string
    goodsCode?: string
    commonPrice?: number
    [key: string]: any
}

/** 对比状态类型定义 */
export interface ComparisonState {
    items: ComparisonItem[]
    visible: boolean
    currentCommonId: number | null // 当前商品的 commonId
    loading: boolean // 加载状态，防止重复调用
}

/**
 * 店铺列表筛选接口请求参数类型
 */
export interface ShopListRequest {
    /**
     * 所在地，使用最后一级分类
     */
    districtId?: number
    /**
     * 限度
     */
    limit?: number
    /**
     * 1默认排序，2收藏量排序
     */
    orderType?: number
    /**
     * 页
     */
    page?: number
    /**
     * 店铺名称
     */
    shopName?: string
    /**
     * 店铺自营'true'自营,'false'
     */
    shopSelfSupport?: string
}

/**
 * 店铺列表筛选接口响应类型
 */
export interface ShopListResponse {
    code?: number
    data?: {
        list?: WebShopListVO[]
        totalCount?: number
    }
    message?: string
    [property: string]: any
}

/**
 * 店铺百分比类型
 */
export interface ShopPercentVO {
    deliverycreditPercent?: string
    desccreditPercent?: string
    servicecreditPercent?: string
}

/**
 * 店铺评分类型
 */
export interface ShopEvaluationVO {
    avgDeliverycredit?: number
    avgDesccredit?: number
    avgServicecredit?: number
    avgShippingTime?: number
    evaluationCreateTime?: string
    evaluationDeliverycredit?: number
    evaluationDesccredit?: number
    evaluationId?: number
    evaluationServicecredit?: number
    orderId?: string
    shopId?: number
    shopPercent?: ShopPercentVO
    userId?: string
    [property: string]: any
}

/**
 * 商品查询结果类型
 */
export interface ListQueryResultVO {
    activityGoodsDetail?: ActivityGoodsDetailDTO
    activityStatus?: number
    avgShippingTime?: string
    catName?: string
    commonCollect?: number
    commonEvaluate?: number
    commonId?: number
    commonImage?: string
    commonIsXian?: number
    commonMarketPrice?: number
    commonName?: string
    commonPrice?: number
    commonSalenum?: number
    commonState?: number
    commonStock?: number
    goodsId?: string
    goodsIdOne?: number
    goodsIsShelves?: number
    goodsMaxSale?: number
    goodsPrice?: number
    goodsSalenum?: number
    goodsStock?: number
    integralGoods?: number
    moq?: number
    needIntegral?: number
    operateType?: number
    pastDue?: number
    periodOfValidity?: string
    price?: number
    priceOne?: number
    putawayId?: number
    qty?: number
    sellCheck?: number
    sellDeliver?: number
    shopId?: number
    shopName?: string
    virtualGoods?: number
    [property: string]: any
}

/**
 * 店铺列表VO类型
 */
export interface WebShopListVO {
    commonList?: ListQueryResultVO[]
    districtId?: number
    shopAddress?: string
    shopCollect?: number
    shopEvaluationEntity?: ShopEvaluationVO
    shopGoodsSum?: number
    shopId?: number
    shopLevelId?: number
    shopLogo?: string
    shopName?: string
    shopPercent?: ShopPercentVO
    shopSelfSupport?: string
    shopSlide?: string
    wapShopLogo?: string
    [property: string]: any
}

/**
 * 店铺头部信息响应类型
 */
export interface ShopHeaderInfo {
    /**
     * 关注状态：1为已关注，2未关注（店铺头部接口字段）
     */
    type?: number
    /**
     * @deprecated 部分旧接口用 favorites，新接口以 type 为准
     */
    favorites?: number
    /**
     * 店铺横幅
     */
    shopBanner?: string
    /**
     * 店铺二维码
     */
    qrUrl?: string
    /**
     * 店铺logo
     */
    shopLogo?: string
    /**
     * 店铺名称
     */
    shopName?: string
    /**
     * 是否自营
     */
    shopSelfSupport?: boolean | string
    /**
     * 店铺id
     */
    shopId?: number
    /**
     * 收藏数
     */
    shopcollect?: number
    [property: string]: any
}

/**
 * 店铺首页信息响应类型
 */
export interface WebShopIndexVO {
    /**
     * 查询店铺信息
     */
    shopBase?: WebShopBaseIndexVO
    /**
     * 查询店铺自定义分类
     */
    shopSeltCatList?: ManageShopSelfCatMenuVO[]
    [property: string]: any
}

/**
 * 查询店铺信息
 */
export interface WebShopBaseIndexVO {
    /**
     * pc店招
     */
    shopBanner?: string
    /**
     * 店铺粉丝数
     */
    shopCollect?: number
    /**
     * 店铺id
     */
    shopId?: number
    /**
     * 店招
     */
    shopImage?: string
    /**
     * 店铺logo
     */
    shopLogo?: string
    /**
     * 店铺名称
     */
    shopName?: string
    /**
     * 轮播图
     */
    shopSlide?: string
    /**
     * 轮播图对应地址
     */
    shopSlideurl?: string
    /**
     * 店铺信息返回集合
     */
    shopSlideurlList?: ShopSlideUrlVO[]
    /**
     * 店铺logo
     */
    wapShopLogo?: string
    [property: string]: any
}

/**
 * 店铺轮播图地址信息
 */
export interface ShopSlideUrlVO {
    /**
     * 活动页
     */
    activityPage?: string
    /**
     * 分类id
     */
    catId?: number
    /**
     * 商品查询条件对象
     */
    commonIdAndGoodsId?: CommonAndGoodsVO
    /**
     * 1新品，2收藏量3销售量4价格
     * 2商品编号{"goodsId":5026,"commonId":5026}，
     * 3链接{"url":http://},4店铺{"shopId":11}
     * 6分类id{\"catId\":10000104}
     * 7不跳转8活动页
     */
    opType?: number
    /**
     * 店铺id
     */
    shopId?: number
    /**
     * 轮播图对应地址
     */
    shopSlideUrl?: string
    /**
     * 地址
     */
    url?: string
    [property: string]: any
}

/**
 * 商品查询条件对象
 */
export interface CommonAndGoodsVO {
    /**
     * 商品公共id
     */
    commonId?: number
    /**
     * 商品id
     */
    goodsId?: number
    [property: string]: any
}

/**
 * 店铺自定义商品分类表
 */
export interface ManageShopSelfCatMenuVO {
    /**
     * 商品子集
     */
    children?: ManageShopSelfCatMenuVO[]
    /**
     * 类目父id
     */
    parentId?: number
    /**
     * 排序
     */
    shopGoodsCatDisplayorder?: number
    /**
     * 物品类目id
     */
    shopGoodsCatId?: number
    /**
     * 物品种类
     */
    shopGoodsCatName?: string
    /**
     * 类目状态
     */
    shopGoodsCatStatus?: number
    [property: string]: any
}

/**
 * 店铺商品列表筛选请求参数
 */
export interface WebShopGoodsListDTO {
    /**
     * 1降序，2升序
     */
    addTime?: number
    /**
     * 父分类
     */
    catParentId?: number
    /**
     * 1降序，2升序
     */
    collectSum?: number
    /**
     * 是否推荐1 是 2 否
     */
    commonIsRecommend?: number
    /**
     * 商品spu名称
     */
    commonName?: string
    /**
     * 当前页
     */
    currentPage?: number
    /**
     * 评价 1降序，2升序
     */
    evaluate?: number
    /**
     * 限度
     */
    limit?: number
    /**
     * 导航
     */
    navProperty?: number
    /**
     * ''综合，1新品，2收藏量，3销售量，4价格
     */
    orderType?: number | string
    /**
     * 页
     */
    page?: number
    /**
     * 页大小
     */
    pageSize?: number
    /**
     * 父id
     */
    parentId?: number
    /**
     * 1降序，2升序
     */
    priceOrder?: number
    /**
     * 1降序，2升序
     */
    saleNum?: number
    /**
     * 归属店铺分类id
     */
    shopCatId?: string
    /**
     * 店铺商品分类id集合
     */
    shopCatIdList?: number[]
    /**
     * 店铺id
     */
    shopId?: number
    /**
     * token
     */
    token?: string
    /**
     * 总数量
     */
    totalCount?: number
    [property: string]: any
}

/**
 * 店铺商品列表响应类型
 */
export interface WebShopGoodsListResponse {
    code?: number
    data?: {
        goodsList?: ListQueryResultVO[]
        totalCount?: number
    }
    message?: string
    [property: string]: any
}

/**
 * Store 类型联合类型
 * 用于类型推断和组合所有 Store 实例
 * 实际定义在 stores/index.ts，此处重新导出以便统一管理
 */
export type { StoreType } from '../stores/index'

/**
 * 店铺资质信息类型
 */
export interface ShopQualificationsInfo {
    /**
     * 店铺名称
     */
    shopName?: string
    /**
     * 店铺id
     */
    shopId?: number
    /**
     * 用户名称
     */
    userName?: string
    /**
     * 店铺所在地-省
     */
    shopProvince?: number
    /**
     * 店铺所在地-市
     */
    shopCity?: number
    /**
     * 店铺所在地-区
     */
    shopArea?: number
    /**
     * 店铺所在地-详细地址
     */
    shopAddress?: string
    /**
     * 完整店铺地址
     */
    completeShopAddress?: string
    /**
     * 店铺所在地
     */
    shopLocation?: string
    /**
     * 店铺类型
     */
    shopType?: number
    /**
     * 店铺状态-3：开店成功 2:待审核付款 1:待审核资料 0:关闭
     */
    shopStatus?: number
    /**
     * 公司名称
     */
    enterpriseName?: string
    /**
     * 税号
     */
    taxId?: string
    /**
     * 营业执照所在地-省
     */
    blProvince?: number
    /**
     * 营业执照所在地-市
     */
    blCity?: number
    /**
     * 营业执照所在地-区
     */
    blArea?: number
    /**
     * 营业执照所在地-详细地址
     */
    blAddress?: string
    /**
     * 完整营业执照地址
     */
    completeBlAddress?: string
    /**
     * 营业执照所在地
     */
    blLocation?: string
    /**
     * 证件有效期开始时间
     */
    validateStartTime?: string
    /**
     * 证件有效期结束时间
     */
    validateEndTime?: string
    /**
     * 是否长期有效
     */
    validateLongTerm?: number
    /**
     * 企业电话
     */
    enterprisePhone?: string
    /**
     * 企业员工
     */
    employeeNum?: number
    /**
     * 注册资金
     */
    registerMoney?: number
    /**
     * 营业执照图片
     */
    businessLicencePic?: string
    /**
     * 营业执照照片base64
     */
    businessLicencePicBase64?: string
    /**
     * 生产许可证
     */
    manufacturePermitPic?: string
    /**
     * 开户许可证
     */
    openPermitPic?: string
    /**
     * 入驻id
     */
    joinId?: string
    /**
     * 0-待审核,1-进行中,2-打款完成,3-通过,4-不通过
     */
    auditStatus?: number
    /**
     * 审核备注
     */
    auditComment?: string
    /**
     * 企业性质 0未选择 1个体工商户 2企业
     */
    enterpriseNature?: number
    /**
     * 法人姓名
     */
    legalPersonName?: string
    /**
     * 法人手机号
     */
    legalPersonMobile?: string
    /**
     * 法人身份证
     */
    legalPersonIdCard?: string
    /**
     * 法人身份证正面照
     */
    idCardFront?: string
    /**
     * 法人身份证反面照
     */
    idCardBack?: string
    /**
     * 通联审核状态 0待入网 1待通联审核 2通联审核拒绝 3已入网
     */
    tlAuditStatus?: string
    /**
     * 通联审核原因
     */
    tlAuditMessage?: string
    /**
     * 0:个人入驻 1:企业入驻 2:门店厂商 3:供应商 4:品牌店铺
     */
    shopBusiness?: number
    /**
     * 自我声明
     */
    selfDeclaration?: string
    [property: string]: any
}
