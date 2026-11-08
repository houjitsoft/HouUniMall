/**
 * 商品相关类型定义
 */

import type { ActivityGoodsDetailDTO, ManjianzengzheGoodsRuleVO } from './business'

/** 商品列表类型定义 */
export interface GoodsList {
    goodsId: string
    commonId: string
    goodsImage: string
    goodsName: string
    dealerGoodsFlag: number
    goodsNumber: number
    goodsSpecifications: string
    goodsCode: string
    goodsStock: number
    singlePrice: number
    goodsTotal: number
    activityType: string
    commonIsXian?: number
    activityGoodsDetail?: ActivityGoodsDetailDTO | null
    /** 组合商品类型：1固定，2搭配 */
    combinationType?: number
    /** 组合购买数量（固定组合=套数，搭配组合=当前商品选购数量） */
    combinationNum?: number
    /** 组合ID */
    combinationId?: number
    /** 组合商品数量（下单/算价兼容字段） */
    combinationGoodsNum?: number
}

/** 店铺列表类型定义 */
export interface ShopList {
    shopInvoices: number
    goodsList: GoodsList[]
    shopName: string
    shopId: number
    shopTotalPrice: number
    invoiceActive: number
    virtualGoods?: number
}

/** 商品详情相关类型定义 */
export interface GoodsBases {
    commonId: number
    goodsId: number
    supplierGoodsId: number
    goodsName: string
    catId: number
    brandId: number
    brandName: string | null
    brandPic: string | null
    goodsSpec: string
    goodsPrice: number
    goodsMarketPrice: number
    goodsStock: number
    goodsAlarm: number
    goodsCode: string
    goodsIsRecommend: boolean
    goodsClick: number
    goodsSalenum: number
    goodsCollect: number
    goodsImage: string
    goodsEvaluationGoodStar: number
    goodsEvaluationCount: number
    goodsMaxSale: number
    goodsMinSale: number
    shopId: number
    shopName: string
    goodsIsShelves: number
    commonVerify: number
    commodityPrice: number | null
    activityPrice: number | null
    commonState: number
    commonIsShopTake: number
    activityStatus: any
    activityGoodsDetail: any
    /** 活动详情（新字段） */
    activityGoodsDetailDTO?: any
    commonType: any
    commonLimit: number
    type: number
    commonVideo: string
    specValue: string
    commonIsXian: number
    needIntegral: number | null
    promotionContent: string | null
    promotionType: number | null
    commonPrice: number
    dw: string
    moq: number
    memberAmount: number
    deliveryDate: number
    goodsOrderDate: string | null
    hyZheKou: number
    goodsImages: any
    zhuangTai: any
    operateType: any
    goodsType: number
    specValueList: Array<{
        paramName: string
        sort: number
        isCommon: boolean | null
        refSpecId: number | null
        paramValue: string
        skuId: number | null
    }>
    commonImage: string | null
    virtualGoods: any
    commonEvaluate: any
    commonSalenum: number
    isAddContrast: boolean | null
    goodsListFirstShow: any
    levelId: number | null
    dealerGoodsFlag: number
    profitMargin: number | null
    productListingStart: boolean
    dropShippingStart: boolean
    taxInclusiveNoDiscount: number | null
    dealerDiscountAmout: number | null
    isThousandsGuestsPrice: number
    supplierSource: number
    msrp: number | null
}

export interface GoodsImage {
    imagesImage: string
    imagesDisplayorder: number
    imagesIsDefault: number
}

export interface GdsCommon {
    commonId: number
    commonImage: string
    commonSpecValue: string
    commonSpecValueList: any
    commonState: number
    catName: string
    catId: number
    commonIsXian: number
    commonLimit: number
    commonType: any
    cornerUrl: string | null
    type: number
    commonVideo: string
    specParam: string
    virtualGoods: number
    periodOfValidity: string
    pastDue: number
    specParamList: Array<{
        paramName: string
        sort: number
        isCommon: boolean
        refSpecId: number | null
        paramNameValues: string[]
        isOffShelf: number[]
    }>
    integralGoods: number
    goodsType: number
    dealerGoodsFlag: number
    dw: string | null
    deliveryDate: number
    goodsOrderDate: string | null
    customInformation: string
    customInformationList: Array<{
        fieldName: string
        fieldValue: string
    }>
    afterService: string
    afterServiceList: string[]
    technicalDirectorWx: string | null
    technicalDirectorTel: string | null
    manufacturer: string | null
    categoryAttributesList?: CategoryAttribute[]
    supplierSource: number
    commonBody: string | null
    sampleType: number
}

export interface ShopDetail {
    invoiceActive: number
    /** 质保金状态：1 表示已交纳 */
    depositStatus?: number
    shopCollect: number
    shopCount: number
    shopId: number
    shopInvoices: number
    shopLogo: string
    shopName: string
    shopSelfSupport: boolean
    userName: string
    wapShopLogo: string
    shopSlide?: string
}

export interface CategoryAttribute {
    id: number
    commonId: number
    attributeName: string
    attributeValueName: string
    isSearch?: number
    isRequired?: number
    displayOrder?: number
    [property: string]: any
}

export interface GoodsDetailData {
    goodsBases: GoodsBases
    goodsImages: GoodsImage[]
    shopDetail: ShopDetail
    gdsCommon: GdsCommon
    manageGdsGoodsConfig: any
    fxGoodsList: any
    gdsBaseDetailList: any[]
    zhuangTai: number
    qrUrl: string
    manageGdsBaseInfoList: any
}

/** 商品SKU列表接口参数类型 */
export interface SelectGoodsSkuParams {
    goodsId: number
    specList?: Array<Array<{
        specName: string
        specId: string
    }>>
    page: number
    limit: number
    browserId?: string
}

/** 商品SKU列表返回数据类型 */
export interface GoodsSkuItem {
    goodsId?: number
    goodsNumber?: number
    deliveryDate?: string | number
    price?: string | number
    commonPrice?: number
    goodsCode?: string
    goodsName?: string
    goodsImage?: string
    dw?: string
    [key: string]: any
}

export interface SelectGoodsSkuResponse {
    list: GoodsSkuItem[]
    total: number
    page: number
    limit: number
    [key: string]: any
}

/** 商品详情描述返回数据类型 */
export interface CommonDetailDesc {
    commonId: number
    commonBody: string
    commonBodyH5: string | null
    commonBodyWechat: string | null
    propertys: any
    productFile: string
    productFileList: any[]
}

/** 推荐商品接口参数类型 */
export interface RecommendGoodsParams {
    shopId: number | string
    opType: number // 2: 热门收藏排行, 3: 热销商品排行
    limitSize: number
    token?: string | null
}

/** 添加商品到对比接口参数类型 */
export interface AddGoodsContrastParams {
    goodsId: string | number
    goodsNum: number
    browserId: string
}

/** 删除对比商品接口参数类型 */
export interface DeleteGoodsContrastParams {
    goodsIds: number[]
    browserId: string
}

/** 查询对比商品接口参数类型 */
export interface SelectGoodsContrastParams {
    commonId: string | number
    browserId: string
}

/** 对比商品项类型 */
export interface GoodsContrastItem {
    commonId: number
    goodsId: number
    goodsName: string
    goodsNum: number
    browserId: string | null
    goodsSpec: string
    brandName: string
    goodsPrice: number
    dw: string
    moq: number
    goodsCode: string
    goodsStock: number
    deliveryDate: number
    goodsOrderDate: string | null
    shopId: number
    goodsImage: string
    specValue: string
    specValueList: Array<{
        paramName: string
        sort: number
        isCommon: boolean | null
        refSpecId: number | null
        paramValue: string
        skuId: number | null
    }>
    specParam: string
}

/**
 * 匹配商品项接口定义
 */
export interface MatchedGoodsItem {
    xuHao: number // 序号
    code: string // 订货编码
    goodsNum: number // 订货数量
    goodsBase: {
        goodsId: number
        goodsName: string
        goodsImage: string
        shopId: number
        goodsSpec: string
        brandName: string
        shopName: string
        commonPrice: number
        goodsPrice: number
        concessionalRate: number
        moq: number
        dw: string // 单位
        goodsStock: number
        goodsCode: string
        businessGoodsCode: string
        goodsIsShelves: string
        commonId: number
        commonIsXian: number
        goodsMaxSale: number
        activityGoodsDetail?: {
            activityPrice: number
            overplusTime: number
            activityEndTime: string
            goodsUpperLimit: number
            goodsLowerLimit: number
            goodsBuyCount: number
            activityStatus: number
            activityStartStatus: number
            discountAmount: number
            activityStartTime: string
            discountUpperTime: number
            goodsPrice: number
        }
        goodsNum: number
    }
}

/**
 * 匹配结果接口定义
 */
export interface MatchedGoodsResponse {
    goodTotalNums: number // 匹配到的商品总数
    list: MatchedGoodsItem[] // 商品列表
}

/** 查询商品配送区域接口参数类型 */
export interface QueryTransportAreaParams {
    shopGoodsIds: number[][]
    areaIds: number[]
    countryId: number
}

/** 商品分类项类型（三级级联） */
export interface GoodsCategoryItem {
    catId: number
    catName: string
    childGoodsCatList: GoodsCategoryItem[] | null
}

/** 商品搜索返回数据类型 */
export interface SearchVO {
    attrs?: GoodsAttributeGroupVO[]
    goodsCount?: number
    goodsId?: number
    goodsList?: ArticleVO[]
    keyword?: string
    lineBrands?: GoodsBrandVO[]
    lineSaleCategories?: GoodsCatResultVO[]
    navBrands?: GoodsBrandVO[]
    navSaleCategories?: GoodsCatResultVO[]
    type?: number
    [property: string]: any
}

/** 商品属性组表 */
export interface GoodsAttributeGroupVO {
    displayOrder?: number
    goodsAttributeList?: GoodsCategoryAttributesVO[]
    groupName?: string
    [property: string]: any
}

/** 商品设置的类目属性表 */
export interface GoodsCategoryAttributesVO {
    attributeName?: string
    attributeValueName?: string
    commonId?: number
    displayOrder?: number
    id?: number
    isRequired?: number
    isSearch?: number
    [property: string]: any
}

/** 商品 */
export interface ArticleVO {
    activityGoodsDetail?: ActivityGoodsDetailDTO
    activityPrice?: number
    activityStatus?: number
    avgShippingTime?: string
    brandId?: number
    brandName?: string
    brandPic?: string
    catId?: number
    catName?: string
    commodityPrice?: number
    commonCollect?: number
    commonEvaluate?: number
    commonId?: number
    commonImage?: string
    commonIsShopTake?: number
    commonIsXian?: number
    commonLimit?: number
    commonMarketPrice?: number
    commonName?: string
    commonNameOrignial?: string
    commonPrice?: number
    commonSalenum?: number
    commonState?: number
    commonStock?: number
    commonVerify?: number
    commonVideo?: string
    dealerDiscountAmout?: number
    dealerGoodsFlag?: number
    deliveryDate?: number
    dropShippingStart?: boolean
    dw?: string
    goodsAlarm?: number
    goodsClick?: number
    goodsCode?: string
    goodsCollect?: number
    goodsEvaluationCount?: number
    goodsEvaluationGoodStar?: number
    goodsId?: number
    goodsIdOne?: number
    goodsImage?: string
    goodsImages?: ImageDetailVO[]
    goodsIsRecommend?: number
    goodsIsShelves?: number
    goodsListFirstShow?: number
    goodsMarketPrice?: number
    goodsMaxSale?: number
    goodsMinSale?: number
    goodsName?: string
    goodsOrderDate?: number
    goodsPrice?: number
    goodsSalenum?: number
    goodsSpec?: string
    goodsStock?: number
    goodsType?: number
    hyZheKou?: number
    integralGoods?: number
    isAddContrast?: number
    isDelete?: number
    isThousandsGuestsPrice?: number
    levelId?: number
    memberAmount?: number
    moq?: number
    needIntegral?: number
    operateType?: number
    pastDue?: number
    periodOfValidity?: string
    productListingStart?: boolean
    profitMargin?: number
    promotionContent?: string
    promotionType?: string
    qty?: number
    score?: number
    shopId?: number
    shopName?: string
    shopSelfSupport?: number
    specValue?: string
    supplierGoodsId?: number
    // 价格字段统一使用 commonPrice
    taxInclusiveNoDiscount?: number
    type?: number
    updateTime?: string
    vector?: number[]
    virtualGoods?: number
    zhuangTai?: number
    goodsLabelName?: string
    [property: string]: any
}

// ActivityGoodsDetailDTO 和 ManjianzengzheGoodsRuleVO 已从 business.ts 导入，重新导出以供向后兼容
export type { ActivityGoodsDetailDTO, ManjianzengzheGoodsRuleVO }

/** 图片详情 */
export interface ImageDetailVO {
    imagesDisplayorder?: number
    imagesImage?: string
    imagesIsDefault?: number
    [property: string]: any
}

/** 品牌 */
export interface GoodsBrandVO {
    brandEnable?: number
    brandId?: number
    brandKey?: string
    brandName?: string
    brandPic?: string
    catId?: number
    catName?: string
    [property: string]: any
}

/** 商品分类结果 */
export interface GoodsCatResultVO {
    catAlias?: string
    catDisplayorder?: number
    catId?: number
    catLevel?: number
    catName?: string
    catParentId?: number
    catPic?: string
    secordNameList?: SecordNameListVO[]
    [property: string]: any
}

/** 二级分类 */
export interface SecordNameListVO {
    catDisplayorder?: number
    catId?: number
    catName?: string
    [property: string]: any
}

/** 商品列表视图模式类型 */
export type ProductViewMode = 'grid' | 'list'

/** 商品排序类型 */
export type ProductOrderType = 0 | 1 | 2 | 3 | 4 | 5 | 6

/** 商品排序方向类型 */
export type ProductOrderDirection = 1 | 2

/** 商品搜索参数接口 */
export interface ProductSearchParams {
    page: number
    limit: number
    orderType: ProductOrderType
    /** 后端主搜索字段 */
    name?: string
    /** URL/兼容字段 */
    kws?: string
    keyword?: string
    commonName?: string
    goodsName?: string
    imagePath?: string
    base64Str?: string
    catId?: number
    brandIdList?: number[]
    priceOrder?: number
    saleNum?: number
    commentOrder?: number
    attrName?: string
}

/**
 * 商品筛选查询参数
 */
export interface GoodsFilterQueryParams {
    /** Token */
    token?: string | null
    /** 品牌ID */
    brandId?: string
    /** 分类ID */
    catId?: string
    /** 商品名称 */
    commonName?: string
    /** 最低价格 */
    minPrice?: string
    /** 最高价格 */
    maxPrice?: string
    /** 店铺自营 */
    shopSelfSupport?: string
    /** 排序类型 */
    orderType?: number
    /** 评价排序 */
    commentOrder?: number
    /** 价格排序 */
    priceOrder?: string
    /** 销量排序 */
    saleNum?: string
    /** 当前页码 */
    currentPage?: number
    /** 每页数量 */
    pageSize?: number
    /** 积分商品 */
    integralGoods?: number
    /** 是否推荐 */
    commonIsRecommend?: number
    [property: string]: any
}

/**
 * 商品筛选查询结果项
 */
export interface GoodsFilterQueryItem {
    /** 商品ID */
    commonId: number
    /** 商品ID（字符串） */
    goodsId: string
    /** 商品ID（备用） */
    goodsIdOne?: string | null
    /** 商品名称 */
    commonName: string
    /** 商品价格 */
    commonPrice?: number | string | null
    /** 销量 */
    commonSalenum: number
    /** 市场价格 */
    commonMarketPrice: number
    /** 商品图片 */
    commonImage: string
    /** 商品状态 */
    commonState?: number | null
    /** 商品评价 */
    commonEvaluate?: number | null
    /** 收藏数 */
    commonCollect: number
    /** 店铺名称 */
    shopName: string
    /** 店铺ID */
    shopId: number
    /** 分类名称 */
    catName?: string | null
    /** 库存 */
    commonStock?: number | null
    /** 商品销量 */
    goodsSalenum?: number | null
    /** 价格 */
    price?: number | null
    /** 价格（备用） */
    priceOne?: number | null
    /** 商品库存 */
    goodsStock?: number | null
    /** 商品价格 */
    goodsPrice: string
    /** 商品是否上架 */
    goodsIsShelves?: number | null
    /** 数量 */
    qty: number
    /** 平均发货时间 */
    avgShippingTime?: string | null
    /** 是否限购 */
    commonIsXian: number
    /** 活动状态 */
    activityStatus?: any
    /** 活动商品详情 */
    activityGoodsDetail?: any
    /** 虚拟商品 */
    virtualGoods: number
    /** 有效期 */
    periodOfValidity?: string | null
    /** 是否过期 */
    pastDue?: number | null
    /** 积分商品 */
    integralGoods: number
    /** 所需积分 */
    needIntegral: number
    /** 销售审核 */
    sellCheck?: number | null
    /** 销售配送 */
    sellDeliver?: number | null
    /** 操作类型 */
    operateType?: number | null
    /** 上架ID */
    putawayId?: number | null
    /** 最大销量 */
    goodsMaxSale?: number | null
    /** 最小起订量 */
    moq?: number | null
    [property: string]: any
}

/**
 * 商品筛选查询结果数据
 */
export interface GoodsFilterQueryData {
    /** 总数量 */
    totalCount?: number
    /** 每页数量 */
    pageSize?: number
    /** 总页数 */
    totalPage?: number
    /** 当前页 */
    currPage?: number
    /** 商品列表 */
    list?: GoodsFilterQueryItem[]
}

/**
 * 代金券模版 VO
 */
export interface VoucherTemplateVO {
    /** 代金券模版编号 */
    voucherTId?: number
    /** 代金券模版名称 */
    voucherTTitle?: string
    /** 代金券模版描述 */
    voucherTDesc?: string
    /** 店铺分类 */
    shopClassId?: number
    /** 代金券模版有效期开始时间 */
    voucherTStartDate?: string
    /** 代金券模版有效期结束时间 */
    voucherTEndDate?: string
    /** 代金券模版面额 */
    voucherTPrice?: number
    /** 代金券使用时的订单限额 */
    voucherTLimit?: string
    /** 店铺id */
    shopId?: number
    /** 店铺名称 */
    shopName?: string
    /** 代金券模版的创建者id */
    voucherTCreatorId?: string
    /** 代金券模版状态(1-有效,2-失效) */
    voucherTState?: number
    /** 模版可发放的代金券总数 */
    voucherTTotal?: number
    /** 模版已发放的代金券数量 */
    voucherTGiveout?: number
    /** 模版已经使用过的代金券 */
    voucherTUsed?: number
    /** 模版的创建时间 */
    voucherTAddDate?: string
    /** 模版的最后修改时间 */
    voucherTUpdateDate?: string
    /** 每人限领张数 */
    voucherTEachlimit?: number
    /** 是否领取： 0未领取1未用2不可领取3已用4过期5回收 */
    receiveOrNot?: number
    /** 代金券类型：1-单个店铺，2-多店铺参与 */
    voucherTType?: number
    /** 代金券类型：1 是店铺优惠券 2是商品优惠券 3 商品兑换券 */
    isShopVoucher?: number
    /** 商品列表 */
    goodsList?: Array<Record<string, any>>
    /** 已领取数量 */
    receivedCount?: number
    /** 剩余数量 */
    remainingCount?: number
    /** 是否还能领取：1=能，0=已达上限 */
    canReceive?: number
    /** 自定义代金券模板图片 */
    voucherTCustomimg?: string
    [property: string]: any
}
