/**
 * 业务相关类型定义
 */

/**
 * 查询配送时间接口参数类型
 */
export interface QueryDeliveryTimeParams {
    shopId: number
    provinceId: number
    cityId: number
    areaId: number
}

/**
 * 计算价格接口请求参数类型
 */
export interface CalculatePriceParams {
    redPacketId?: number
    orderReceiverProvinceId?: number
    orderReceiverCityId?: number
    orderReceiverAreaId?: number
    countryId?: number
    virtualGoods?: number
    storeInformationList: Array<{
        shopId: number
        couponId?: number
        goodsBaseList: Array<{
            goodsId: number
            commonId: number
            goodsNum: number
            combinationType?: number
            combinationNum?: number
            combinationId?: number
            combinationGoodsNum?: number
        }>
        useRebateType?: number
    }>
    memberClassId?: number
    platForm?: number
    businessId?: number | null
    goodsIds: string[]
    quotationId?: number | null
    /** 代客下单ID */
    valetOrderId?: string | null
    combinationId?: number | null
    combinationGoodsNum?: number | null
    isAutoChoose?: number
    orderReceiverName?: string
    orderReceiverContact?: string
    orderReceiverAddress?: string
    /** 积分商品标识：0 表示积分商品，其他值表示普通商品 */
    integralGoods?: number
}

/**
 * MapString 类型
 */
export interface MapString {
    key?: string
    [property: string]: any
}

/**
 * MapObject 类型（每件商品返利抵扣金额）
 */
export interface MapObject {
    "{}"?: { [key: string]: any }
    [property: string]: any
}

// 相关类型已移除

/**
 * 可用代金券 VO（selectavailablevcbase 接口返回）
 */
export interface VoucherBaseVO {
    voucherId?: string
    voucherCode?: string
    voucherTId?: number
    voucherTitle?: string
    voucherDesc?: string
    voucherStartDate?: string
    voucherEndDate?: string
    voucherPrice?: number
    voucherLimit?: number
    voucherShopId?: number
    voucherState?: number
    voucherActiveDate?: string
    voucherType?: number
    voucherOwnerId?: string
    voucherOwnerName?: string
    voucherOrderId?: string
    available?: number
    shopName?: string
    avaliableGoods?: number[]
    commonImage?: string
    isShopVoucher?: number
    voucherTIdList?: number[]
    houVoucherBaseVOList?: VoucherBaseVO[]
    voucherNo?: string
    commonImageList?: string[]
    [property: string]: any
}

/**
 * 满减增折商品规则类型
 */
export interface ManjianzengzheGoodsRuleVO {
    commonId?: number
    discountContent?: string
    discountType?: number
    giveawayId?: number
    giveawayImage?: string
    giveawayName?: string
    goodsId?: number
    hasGiveaway?: number
    meetAmount?: number
    promotionId?: number
    [property: string]: any
}

/**
 * 活动详情类型
 */
export interface ActivityGoodsDetailDTO {
    activityEndTime?: string
    activityPrice?: number
    activityStartStatus?: number
    activityStartTime?: string
    activityStatus?: number
    discountAmount?: number
    discountTitle?: string
    discountUpperTime?: number
    goodsBuyCount?: number
    goodsLowerLimit?: number
    goodsPrice?: number
    goodsUpperLimit?: number
    isLimit?: number
    markId?: string
    mjzzPrice?: number
    overplusTime?: number
    pintuanPrice?: number
    priceOne?: number
    promotionId?: number
    ruleList?: ManjianzengzheGoodsRuleVO[]
    seckillNum?: number
    type?: number
    [property: string]: any
}

/**
 * 商品配送信息类型
 */
export interface WapShippingVO {
    activityGoodsDetail?: ActivityGoodsDetailDTO
    activityStatus?: number
    bigbabyCommission?: number
    catId?: number
    commonId?: number
    commonIsXian?: number
    commonLimit?: number
    commonState?: number
    commonVerify?: number
    dealerCommission?: string
    dealerGoodsFlag?: number
    dealerGoodsPrice?: number
    dw?: string
    fxGoods?: number
    fxRate?: string
    goodsCubage?: number
    goodsDisountPrice?: number
    goodsId?: number
    goodsImage?: string
    goodsIsShelves?: number
    goodsMarketPrice?: number
    goodsMaxSale?: number
    goodsName?: string
    goodsNum?: number
    goodsPaymentPrice?: number
    goodsPrice?: number
    goodsShippingFee?: number
    goodsSpec?: string
    goodsStock?: number
    goodsType?: number
    internationalTransportArea?: number
    internationalTransportTemplate?: number
    moq?: number
    rebateRatio?: number
    restrictionHours?: number
    restrictionTimes?: number
    shopId?: number
    supplierGoodsId?: number
    supplierSource?: number
    supplyCommission?: string
    supplyShopId?: number
    supplyShopName?: string
    commonPrice?: number
    tgRate?: string
    totalGoodsShippingFee?: number
    transportAreaId?: number
    transportTemplate?: number
    verId?: number
    [property: string]: any
}

/**
 * 运费集合类型
 */
export interface ShippingListDTO {
    allowanceShopId?: string
    allowanceShopPrice?: number
    buyerMessage?: string
    couponPrice?: number
    districtId?: number
    dwList?: string[]
    goodsAmountList?: number[]
    goodsCatLimit?: string
    goodsCombinationPrice?: number
    goodsIdList?: number[]
    goodsNo?: number
    goodsOriginalPrice?: number
    goodsPriceList?: number[]
    goodsQuantity?: number
    goodsRebateMap?: MapObject
    goodsRebatePrice?: number
    hyPriceList?: number[]
    invoiceId?: number
    invoiceSwitchStatus?: number
    isHasSelfShop?: boolean
    itemShopId?: string
    itemShopPrice?: number
    receivingTime?: number
    redpacketTType?: number
    scale?: number
    shipping?: number
    shopDiscount?: number
    shopGiveawayList?: MapString[]
    shopId?: number
    shopInvoices?: number
    shopLimit?: string
    shopManjianDiscount?: number
    shopManzheDiscount?: number
    shopMoPrice?: number
    shopName?: string
    shopPrice?: number
    shopSelfSupport?: boolean
    shopTaxPrice?: number
    shopTotalPrice?: number
    shopWeiShuiPrice?: number
    shopZheKouPrice?: number
    stepOrHyList?: number[]
    subTotalList?: number[]
    taxIncludedPriceList?: number[]
    taxList?: number[]
    voucherId?: string
    voucherStatus?: number
    voucherTitle?: string
    wsqrList?: WapShippingVO[]
    [property: string]: any
}

/**
 * 店铺数据对象类型
 */
export interface ShopDTO {
    allowancePrice?: number
    allredPacketPrice?: number
    allShopTotalPrice?: number
    avaliableGoods?: number[]
    combinationType?: number
    couponTotalPrice?: number
    giveawayList?: MapString[]
    gradeNames?: string[]
    hotBindDiscount?: number
    hyGoodPrice?: number
    isHasSelfShop?: boolean
    memberDiscountAmount?: number
    orderDiscountType?: number
    platformVoucherCode?: string
    platformVoucherPrice?: number
    rebateMap?: MapObject
    rebatePrice?: number
    redpacketId?: string
    redPacketPrice?: number
    redPacketStatus?: number
    shippingList?: ShippingListDTO[]
    shopMoprice?: number
    shopTaxPrice?: number
    shopTotalDiscount?: number
    shopWeiShuiPrice?: number
    shopZheKouPrice?: number
    shuiFei?: number
    stepGoodPrice?: number
    stepOrMember?: number
    totalIntegral?: number
    totalManjianDiscount?: number
    totalManzheDiscount?: number
    totalPrice?: number
    totalShipping?: number
    voucherIdList?: string[]
    [property: string]: any
}

/**
 * 活动信息接口类型
 */
export interface ActivityInfo {
    /** 活动状态 */
    activityStatus?: number
    /** 活动开始状态 */
    activityStartStatus?: number
    /** 活动开始时间 */
    activityStartTime?: string
    /** 活动结束时间 */
    activityEndTime?: string
    /** 剩余时间 */
    overplusTime?: number
    /** 活动价格 */
    activityPrice?: number
    /** 商品上限 */
    goodsUpperLimit?: number
    /** 商品下限 */
    goodsLowerLimit?: number
    /** 商品购买数量 */
    goodsBuyCount?: number
    /** 折扣金额 */
    discountAmount?: number
    /** 商品价格 */
    goodsPrice?: number
    /** 折扣上限时间 */
    discountUpperTime?: number
    /** 活动名称 */
    activityName?: string
    [key: string]: any
}

/**
 * 发票信息类型
 */
export interface InvoiceInfo {
    /** 开票状态: 0-未开票; 1-审核中; 2-已出票; 3-未通过 */
    invoiceStatus?: number
    /** 发票内容: 0-商品明细; 1-商品类别 */
    invoiceContent?: number
    /** 收票人姓名 */
    userName?: string
    /** 收票人电话 */
    userMobile?: string
    /** 收票人邮件 */
    userMail?: string
    /** 收票人地址-省 */
    addressProvince?: number
    /** 收票人地址-市 */
    addressCity?: number
    /** 收票地址-区 */
    addressArea?: number
    /** 详细地址 */
    addressDetail?: string
    /** 发票类型: 0-普票；1-增值税专用票 */
    invoiceType?: number
    /** 抬头类型: 1-个人；2-单位 */
    invoiceHeadType?: number
    /** 发票抬头 */
    invoiceHead?: string
    /** 单位税号 */
    companyTaxId?: string
    /** 注册地址 */
    registerAddress?: string
    /** 注册电话 */
    registerNumber?: string
    /** 开户银行 */
    openingBank?: string
    /** 银行帐号 */
    bankAccount?: string
    /** 商户订单id */
    orderId?: string
    /** 用户id */
    userId?: string
    /** 发票id */
    invoiceId?: number
    /** 发票名称 */
    invoiceName?: string
    /** 发票金额 */
    invoiceAmount?: number

    [key: string]: any
}

/**
 * 创建订单接口请求参数类型
 */
export interface CreateOrdersParams {
    redPacketId?: number
    orderReceiverName: string
    orderReceiverContact: string
    orderReceiverAddress: string
    orderReceiverProvinceId: number
    orderReceiverCityId: number
    orderReceiverAreaId: number
    countryId: number
    countryName?: string
    internationalDialingCode?: string
    currencySymbol?: string
    exchangeRate?: string
    currencyCode?: string
    memberClassId?: number
    virtualGoods?: number
    storeInformationList: Array<{
        couponId?: string | number
        shopId: number
        invoiceId?: number
        invoiceInfo?: InvoiceInfo
        useRebateType?: number
        buyerMessage?: string
        goodsBaseList: Array<{
            goodsId: number
            goodsName?: string
            goodsNum: number
            combinationType?: number
            combinationNum?: number
            combinationId?: number
            combinationGoodsNum?: number
        }>
    }>
    type?: number
    platForm?: number
    paymentCode?: number
    useRebateType?: number
    goodsIds: number[]
    quotationId?: string | null
    /** 代客下单ID */
    valetOrderId?: string | null
    combinationId?: number | null
    combinationGoodsNum?: number | null
    isAutoChoose?: number
    /** 积分商品标识：0 表示积分商品，其他值表示普通商品 */
    integralGoods?: number
    /** 是否从购物车结算下单：为 true 时表示购物车下单 */
    goodsCat?: boolean
    /** 登录 token，创建订单时与订单字段一并加密提交 */
    token?: string
}

/**
 * 发票模板/发票信息列表项
 */
export interface InvoiceTemplate {
    invoiceTmpId?: number
    userId?: string
    invoiceType?: number
    invoiceHeadType?: number
    invoiceHead?: string
    companyTaxId?: string
    registerAddress?: string
    registerNumber?: string
    openingBank?: string
    bankAccount?: string
    isDefault?: number
    createTime?: string
    lastUpdateTime?: string
    invoiceContent?: number
    [key: string]: any
}

/**
 * 保存发票信息请求参数
 */
export interface InsertInvoiceInfoParams {
    invoiceTmpId?: number
    invoiceType?: number
    invoiceHeadType?: number
    invoiceHead?: string
    companyTaxId?: string
    registerAddress?: string
    registerNumber?: string
    openingBank?: string
    bankAccount?: string
    invoiceContent?: number
    isDefault?: number
    [key: string]: any
}

/**
 * 保存收票地址请求参数
 */
export interface InsertInvoiceAddressParams {
    addressId?: number
    userName?: string
    userMobile?: string
    userMail?: string
    addressProvince?: number
    addressCity?: number
    addressArea?: number
    addressDetail?: string
    isDefault?: number
}

/**
 * 发票收票地址类型
 */
export interface InvoiceAddress {
    /** 发票地址id */
    addressId?: number
    /** 收票人ID */
    userId?: string
    /** 收票人姓名 */
    userName?: string
    /** 收票人手机号 */
    userMobile?: string
    /** 收票人邮箱 */
    userMail?: string
    /** 收票地址-省 */
    addressProvince?: number
    /** 收票地址-市 */
    addressCity?: number
    /** 收票地址-区 */
    addressArea?: number
    /** 详细地址 */
    addressDetail?: string
    /** 是否默认: 1-是；0-否 */
    isDefault?: number
    /** 创建时间 */
    createTime?: string
    /** 上次更新时间 */
    lastUpdateTime?: string
    /** 地址（省市区拼接） */
    addressInfo?: string
    /** 预完成公司地址 */
    preCompleteAddress?: string
    /** 公司地址 */
    completeAddress?: string
}

/**
 * 商家/供应商入驻同意接口返回类型
 */
export interface ShopJoinAgreeData {
    /**
     * 错误信息
     */
    message?: string
    /**
     * 店铺状态
     * 1 未审核，店铺经营信息
     * 8 个人实名制通过
     * 9 资质审核通过
     * 2 全部通过，付款信息待审核
     * 3 开店成功
     * 4 个人基本信息审核被拒绝或入驻或个人实名制不通过
     * 5 开户银行信息
     * 6 店铺经营信息审核被拒绝
     */
    shopStatus: number | null
    /**
     * 0:个人入驻 1:企业入驻
     */
    shopBusiness: number | null
    /**
     * 付款信息审核：1待审核 2审核通过 3审核失败
     */
    paymentVerifyStatus: number | null
    /**
     * 审核信息备注
     */
    shopVerifyReason: string | null
    /**
     * 支付审核备注
     */
    paymentVerifyReason: string | null
    /**
     * 店铺类型: 1-卖家店铺; 2-供应商店铺
     */
    shopType: number | null
    /**
     * 运营后台卖家管理员标识：0 表示商家
     */
    sellerisAdmin?: number | null
    /**
     * 兼容历史字段（部分流程仍依赖）
     */
    isExist?: number
    shopBaseExist?: number
}
