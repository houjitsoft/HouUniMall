/**
 * 订单相关类型定义
 */


/**
 * 评论实体类型
 */
export interface CommentsEntity {
    orderId?: string // 订单id
    goodsId?: number // 商品id
    userName?: string // 会员名
    userAvatar?: string // 会员头像
    goodsName?: string // 商品名
    scores?: number // 1-5分
    content?: string // 内容
    additionalContent?: string // 追评内容
    additionalContentTime?: string // 追评回复时间
    image?: string // 晒单图片
    additionalImage?: string // 追评图片
    createTime?: string // 创建时间
    explainContent?: string // 解释内容
    additionalExplainContent?: string // 商家回复追评内容
    updateTime?: string // 评价时间
    additionalReplyTime?: string // 商家回复追评时间
    imagesList?: string[] // 图片
    additionalImagesList?: string[] // 追评图片
    goodsSpec?: string // 商品规格
    additionalDay?: number // 追评天数
    isanonymous?: number // 是否匿名评价 0不匿名 1匿名
    goodsImage?: string | null
    evaluateStatus?: number | null
    result?: any
    [property: string]: any
}

/**
 * 分页评论响应类型
 */
export interface PagedByComments {
    allOrderNum?: number
    goodTotalCount?: number // 好评数
    neutralTotalCount?: number // 中评数
    badTotalCount?: number // 差评数
    imageTotalCount?: number // 晒单数
    rate?: string // 好评率

    /** 新接口分页信息 */
    records?: {
        totalCount?: number
        pageSize?: number
        totalPage?: number
        currPage?: number
        list?: CommentsEntity[]
        [property: string]: any
    }

    // 兼容旧字段（新接口可能为 null）
    currentPage?: number | null
    pageSize?: number | null
    totalCount?: number | null
    totalPage?: number | null
    list?: CommentsEntity[] | null
    [property: string]: any
}

/**
 * 获取评论接口参数类型
 */
export interface GetCommentsParams {
    token?: string | null
    commonId: number
    evaluation?: number // 评价类型：1好评 2中评 3差评
    page: number
    limit: number
    goodsId: string
}

/**
 * 加入购物车接口参数类型
 */
export interface InsertShoppingCartParams {
    goodsId: number
    goodsNum: number
    moq: number
}

/**
 * 查询购物车接口 data 结构（对应 ApiResponse<T> 中的 T）
 */
export interface QueryShoppingCartData {
    invalidProductList: QueryShoppingCartInvalidProductList[]
    list: QueryShoppingCartShopItem[]
    numberOfValidProducts: number
    [property: string]: any
}

export interface QueryShoppingCartInvalidProductList {
    activityGoodsDetail?: QueryShoppingCartActivityGoodsDetail
    activityGoodsState?: null
    cartId?: number
    cartStatus?: number
    commonId?: number
    commonIsXian?: number
    dealerGoodsFlag?: number
    goodsId?: number
    goodsImage?: string
    goodsIsShelves?: number
    goodsMaxSale?: number
    goodsMinSale?: null
    goodsMoq?: null
    goodsName?: string
    goodsNum?: number
    goodsPrice?: number
    goodsSpec?: null
    goodsSpecChineseValue?: string
    goodsStartTime?: null
    goodsStock?: number
    goodsTotalPrice?: number
    goodsType?: number
    insertCartTime?: string
    plusDiscount?: null
    plusGoods?: number
    plusMember?: number
    purchased?: null
    shopGateId?: null
    shopId?: number
    shopName?: string
    specification?: null
    toBuyMaxNum?: null
    userId?: string
    [property: string]: any
}

export interface QueryShoppingCartShopItem {
    discountCouponActicity?: null
    goodsDetailsList?: QueryShoppingCartGoodsDetailsList[]
    goodsNum?: number
    goodsTotalPrice?: number
    invalidGoodsDetailsList?: null
    invoiceActive?: number
    orderGoodsEvaluationStatus?: null
    orderGoodsId?: null
    orderGoodsSstatus?: null
    orderId?: null
    plusStatus?: number
    redEnvelopeActivity?: null
    shopId?: number
    shopInvoices?: null
    shopName?: string
    [property: string]: any
}

export interface QueryShoppingCartGoodsDetailsList {
    activityGoodsDetail: QueryShoppingCartActivityGoodsDetail
    activityGoodsState: null
    cartId: number
    cartStatus: number
    commonId: number
    commonIsXian: number
    dealerGoodsFlag: number
    goodsId: number
    goodsImage: string
    goodsIsShelves: number
    goodsMaxSale: number
    goodsMinSale: null
    goodsMoq: null
    goodsName: string
    goodsNum: number
    goodsPrice: number
    goodsSpec: null
    goodsSpecChineseValue: string
    goodsStartTime: null
    goodsStock: number
    goodsTotalPrice: number
    goodsType: number
    insertCartTime: string
    plusDiscount: null
    plusGoods: number
    plusMember: number
    purchased: null
    shopGateId: null
    shopId: number
    shopName: string
    specification: null
    toBuyMaxNum: null
    userId: string
    [property: string]: any
}

export interface QueryShoppingCartActivityGoodsDetail {
    activityEndTime: null
    activityId: null
    activityPrice: null
    activityStartStatus: null
    activityStartTime: null
    activityStatus: null
    commonStock: null
    currentCartQuantity: null
    discountAmount: null
    discountGoods: null
    discountTitle: null
    discountUpperTime: null
    goodsBuyCount: null
    goodsId: null
    goodsLowerLimit: null
    goodsPrice: null
    goodsStock: null
    goodsUpperLimit: null
    markId: null
    mjzzPrice: null
    overplusTime: null
    pintuanPrice: null
    priceOne: null
    promotionId: null
    ruleList: null
    seckillGoods: null
    type: null
    [property: string]: any
}

/**
 * 数据对象
 *
 * EntityObjectsDTO
 */
export interface EntityObjectsDTO {
    /**
     * 优惠券活动
     */
    discountCouponActicity?: { [key: string]: any }[]
    /**
     * 店铺详情
     */
    goodsDetailsList?: UserShoppingCartDTO[]
    /**
     * 订单商品总数
     */
    goodsNum?: number
    /**
     * 商品总价
     */
    goodsTotalPrice?: number
    /**
     * 失效商品详情
     */
    invalidGoodsDetailsList?: UserShoppingCartDTO[]
    /**
     * 发票
     */
    invoiceActive?: number
    /**
     * 发票开关审核状态:0-待审核；1-通过；2-不通过；3-未申请
     */
    invoiceAuditStatus?: number
    /**
     * 发票功能开关状态:0-关闭;1-开启
     */
    invoiceSwitchStatus?: number
    /**
     * 评价状态
     */
    orderGoodsEvaluationStatus?: number
    /**
     * 退货商品编号，0为退款
     */
    orderGoodsId?: number
    /**
     * 订单状态
     */
    orderGoodsSstatus?: number
    /**
     * 商户订单id
     */
    orderId?: string
    /**
     * 红包活动
     */
    redEnvelopeActivity?: { [key: string]: any }[]
    /**
     * 店铺id
     */
    shopId?: number
    /**
     * 店铺是否具备发票权限
     */
    shopInvoices?: number
    /**
     * 店铺名称
     */
    shopName?: string
    [property: string]: any
}

/**
 * 购物车
 *
 * UserShoppingCartDTO
 */
export interface UserShoppingCartDTO {
    /**
     * 商品公共id
     */
    commonId?: number
    /**
     * 商品是否参加促销0不参加,1砍价,2秒杀,3限时折扣
     */
    commonIsXian?: number
    /**
     * 商品状态 0下架，1正常，10违规（禁售）,2店铺下架商品下架,3商品删除
     */
    commonState?: number
    /**
     * 库存
     */
    commonStock?: number
    /**
     * 商品审核 1通过，0未通过，10审核中
     */
    commonVerify?: number
    /**
     * 是否经销商品  0普通商品   1经销商品(商家一键上架供应商商品)
     * 2购买经销商品（商家通过立即购买供应商上架的商品）
     */
    dealerGoodsFlag?: number
    /**
     * 单位
     */
    dw?: string
    /**
     * 订货编码
     */
    goodsCode?: string
    /**
     * 商品id
     */
    goodsId?: number
    /**
     * 商品id集合
     */
    goodsIds?: { [key: string]: any }[]
    /**
     * 商品图
     */
    goodsImage?: string
    /**
     * 是否上架1:上架
     */
    goodsIsShelves?: number
    /**
     * 最大购买数量
     */
    goodsMaxSale?: number
    /**
     * 商品名
     */
    goodsName?: string
    /**
     * 数量
     */
    goodsNum?: number
    /**
     * 商品价格
     */
    goodsPrice?: number
    /**
     * 商品规格
     */
    goodsSpec?: string
    /**
     * 规格中文值
     */
    goodsSpecChineseValue?: string
    /**
     * 商品库存
     */
    goodsStock?: number
    /**
     * 商品总价
     */
    goodsTotalPrice?: number
    /**
     * 起订量
     */
    moq?: number
    /**
     * 店铺Id
     */
    shopId?: number
    /**
     * 规格集合
     */
    specification?: { [key: string]: any }[]
    /**
     * 价格
     */
    commonPrice?: number
    [property: string]: any
}

/**
 * 对公转账上传支付凭证请求参数
 */
export interface InsertPaymentVoucherParams {
    orderId: string
    orderPaymentVoucherImgs: string[]
    status: string
}

