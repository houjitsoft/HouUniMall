// 导入类型定义
import type {
    GoodsBases,
    GoodsImage,
    GdsCommon,
    GoodsDetailData,
    ShopDetail,
    SelectGoodsSkuParams,
    GoodsSkuItem,
    SelectGoodsSkuResponse,
    CommonDetailDesc,
    RecommendGoodsParams,
    AddGoodsContrastParams,
    DeleteGoodsContrastParams,
    SelectGoodsContrastParams,
    GoodsContrastItem,
    GoodsCategoryItem,
    GoodsFilterQueryParams,
    GoodsFilterQueryData,
    MatchedGoodsResponse,
    VoucherTemplateVO,
    SearchVO
} from '~/types/goods'
import type { ListQueryResultVO } from '~/types/store'
import type { ApiResponse } from '~/types/api'
import type { ExhibitionIndexData, HallDetailData } from '~/types/mall'
import type { PlusRecommendedGoodsItem } from '~/types/plus'
import { useRequest } from '~/composables/useRequest'
import { getActivePinia } from 'pinia'
import { useMallStore } from '~/stores/modules/mall'

const { post, get } = useRequest()

function currentLanguageCode(): string | undefined {
    if (!import.meta.client) return undefined
    try {
        const pinia = getActivePinia()
        if (!pinia) return undefined
        return useMallStore(pinia).getLanguageCode || undefined
    } catch {
        return undefined
    }
}

/** 将前台 lists 商品字段归一为列表页 GoodsCard 所需结构 */
const normalizeFrontGoodsCard = (item: any) => {
    if (!item || typeof item !== 'object') return item
    const id = Number(item.goodsId ?? item.commonId ?? item.id ?? 0) || 0
    const name = String(item.goodsName || item.commonName || item.name || '')
    const image = String(item.goodsImage || item.commonImage || item.image || '')
    const price = item.goodsPrice ?? item.commonPrice ?? item.minPrice ?? item.price ?? 0
    const sales = item.commonSalenum ?? item.goodsSalenum ?? item.allSaleNum ?? item.saleNum ?? 0
    return {
        ...item,
        goodsId: id,
        commonId: Number(item.commonId ?? id) || id,
        goodsName: name,
        commonName: name,
        goodsImage: image,
        commonImage: image,
        goodsPrice: price,
        commonPrice: price,
        commonSalenum: sales,
        goodsSalenum: sales,
        shopName: item.shopName || item.shop_name || ''
    }
}

/** 兼容 front/lists 返回 { lists, count } 与旧搜索 { goodsList, goodsCount } */
const normalizeSearchGoodsResponse = (res: any): ApiResponse<SearchVO> => {
    const data = res?.data
    if (!data || typeof data !== 'object') return res
    const rawList = Array.isArray(data.goodsList)
        ? data.goodsList
        : Array.isArray(data.lists)
            ? data.lists
            : Array.isArray(data.list)
                ? data.list
                : []
    const goodsList = rawList.map(normalizeFrontGoodsCard)
    const goodsCount = Number(
        data.goodsCount ?? data.count ?? data.total ?? goodsList.length ?? 0
    )
    return {
        ...res,
        data: {
            ...data,
            goodsList,
            goodsCount,
            lineSaleCategories: data.lineSaleCategories || data.extend?.lineSaleCategories || [],
            lineBrands: data.lineBrands || data.extend?.lineBrands || [],
            attrs: data.attrs || data.extend?.attrs || [],
            navSaleCategories: data.navSaleCategories || data.extend?.navSaleCategories || [],
            navBrands: data.navBrands || data.extend?.navBrands || []
        }
    }
}

/**
 * ES 搜索商品列表
 * POST /mall-goods/es/searchGoodsData
 */
export const searchGoodsData = async (params?: {
    attrName?: string
    base64Str?: string
    brandId?: number
    brandIdList?: number[]
    catId?: number
    categoryId?: number
    catIds?: number[]
    commentOrder?: number
    commonEvaluate?: number
    commonIdList?: number[]
    commonName?: string
    goodsName?: string
    /** 后端 MallGoodsSearchDTO 主搜索字段 */
    name?: string
    kws?: string
    keyword?: string
    hotExchange?: number
    imagePath?: string
    integralGoods?: number
    limit?: number
    maxPrice?: number
    minPrice?: number
    orderType?: number
    page?: number
    priceOrder?: number
    saleNum?: number
    shopId?: number
    shopName?: string
    shopSelfSupport?: string
    streamName?: string
    token?: string
    [property: string]: any
}): Promise<ApiResponse<SearchVO>> => {
    const payload: Record<string, any> = { ...(params || {}) }
    const catId = Number(payload.catId || payload.categoryId || payload.cid || 0) || undefined
    if (catId) {
        payload.catId = catId
        payload.categoryId = catId
        payload.cid = catId
    }
    // 统一关键词到 name，避免仅传 commonName/goodsName 时后端忽略
    const keyword = String(
        payload.name || payload.commonName || payload.goodsName || payload.kws || payload.keyword || ''
    ).trim()
    if (keyword) {
        payload.name = keyword
        payload.commonName = keyword
        payload.goodsName = keyword
    }
    // 前台语种：有已发布 i18n 时后端覆盖名称（仅客户端带 Pinia 时附加）
    if (!payload.lang && !payload.languageCode) {
        const code = currentLanguageCode()
        if (code) payload.lang = code
    }
    const res = await post<SearchVO>('/web/mall/goods/front/lists', payload)
    return normalizeSearchGoodsResponse(res)
}

/**
 * 将 /web/mall/front/shop/list 的 like-shop 字段归一为 PC 店铺列表页结构
 */
const normalizeFrontShopListItem = (item: any) => {
    if (!item || typeof item !== 'object') return item
    const shopId = Number(item.shopId ?? item.id ?? 0) || 0
    const goodsRaw = Array.isArray(item.commonList)
        ? item.commonList
        : Array.isArray(item.goods)
            ? item.goods
            : []
    const commonList = goodsRaw.map((g: any) => {
        const goodsId = Number(g?.goodsId ?? g?.id ?? 0) || 0
        const name = String(g?.commonName || g?.goodsName || g?.name || '')
        const image = String(g?.commonImage || g?.goodsImage || g?.image || '')
        const price = g?.commonPrice ?? g?.goodsPrice ?? g?.min_price ?? g?.minPrice ?? 0
        return {
            ...g,
            goodsId,
            commonId: Number(g?.commonId ?? goodsId) || goodsId,
            commonName: name,
            goodsName: name,
            commonImage: image,
            goodsImage: image,
            commonPrice: price,
            goodsPrice: price
        }
    })
    const fans = Number(item.shopCollect ?? item.fans ?? item.collect_count ?? 0) || 0
    return {
        ...item,
        shopId,
        shopName: String(item.shopName || item.name || ''),
        shopLogo: String(item.shopLogo || item.logo || ''),
        wapShopLogo: String(item.wapShopLogo || item.shopLogo || item.logo || ''),
        shopCollect: fans,
        shopGoodsSum: Number(item.shopGoodsSum ?? item.goods_count ?? commonList.length) || 0,
        shopSelfSupport: item.shopSelfSupport != null
            ? String(item.shopSelfSupport)
            : (Number(item.is_self) === 1 ? 'true' : 'false'),
        shopEvaluationEntity: item.shopEvaluationEntity || {
            avgDesccredit: Number(item.desc_score ?? 5) || 5,
            avgServicecredit: Number(item.service_score ?? 5) || 5,
            avgDeliverycredit: Number(item.delivery_score ?? 5) || 5
        },
        shopPercent: item.shopPercent || {
            desccreditPercent: '100%',
            servicecreditPercent: '100%',
            deliverycreditPercent: '100%'
        },
        commonList
    }
}

/**
 * 店铺列表筛选（PC）
 * 收口到 /web/mall/front/shop/list，兼容旧 list/totalCount 结构
 */
export const getShopList = async (params?: {
    districtId?: number
    limit?: number
    orderType?: number
    page?: number
    shopName?: string
    shopSelfSupport?: string
}): Promise<ApiResponse<any>> => {
    const body: Record<string, unknown> = {
        page: params?.page ?? 1,
        limit: params?.limit ?? 10
    }
    if (params?.shopName) {
        body.keyword = params.shopName
    }
    const res = await post<any>('/web/mall/front/shop/list', body)
    const data = res?.data
    if (!data || typeof data !== 'object') {
        return res
    }
    const rawList = Array.isArray(data.list)
        ? data.list
        : Array.isArray(data.lists)
            ? data.lists
            : []
    let list = rawList.map(normalizeFrontShopListItem)
    // 前端筛选自营（下游暂未支持 is_self 条件）
    if (params?.shopSelfSupport === 'true') {
        list = list.filter((s: any) => s.shopSelfSupport === 'true' || Number(s.is_self) === 1)
    }
    if (Number(params?.orderType) === 2) {
        list = [...list].sort((a: any, b: any) => Number(b.shopCollect ?? 0) - Number(a.shopCollect ?? 0))
    }
    return {
        ...res,
        code: res?.code ?? 1,
        data: {
            ...data,
            list,
            totalCount: Number(data.totalCount ?? data.count ?? list.length) || list.length
        }
    }
}

/**
 * 查询特色馆（美食/展览）首页接口
 *
 * GET 请求，获取轮播图、精品特色馆、地方特色馆数据
 */
export const getExhibitionIndex = async (): Promise<ApiResponse<ExhibitionIndexData>> => {
    return await get<ExhibitionIndexData>('/mall-goods/goods/food/exhibitionHall/foodExhibitionHall')
}

// 重新导出类型，保持向后兼容
export type {
    GoodsBases,
    GoodsImage,
    GdsCommon,
    GoodsDetailData,
    ShopDetail,
    SelectGoodsSkuParams,
    GoodsSkuItem,
    SelectGoodsSkuResponse,
    CommonDetailDesc,
    RecommendGoodsParams,
    AddGoodsContrastParams,
    DeleteGoodsContrastParams,
    SelectGoodsContrastParams,
    GoodsContrastItem,
}


// 获取智能推荐商品数据
export const getIntelligentRecommendationGoods = async (params: { page: number; limit: number }) => {
    return await post('/mall-goods/goods/common/intelligentRecommendationGoods', params)
}

/**
 * 规格切换查询 SKU（新接口）
 *
 * POST /mall-goods/goods/common/spec/queryNew
 * @param params.commonId 商品 commonId
 * @param params.specValues 已选规格列表
 * @returns 返回匹配到的 goodsBases 数据（data）
 */
export const queryGoodsSpecQueryNew = async (params: {
    commonId: string | number
    specValues: Array<{ paramName: string; paramValue: string }>
}): Promise<ApiResponse<GoodsBases>> => {
    return await post<GoodsBases>('/mall-goods/goods/common/spec/queryNew', params)
}

/**
 * 国产替代商品列表：POST 请求，传参 browserId/commonName/catId/page/limit
 * @returns data: { list, totalCount }
 */
export const querySubstitutionGoodsByReplace = (params: {
    browserId?: string
    commonName?: string
    catId?: string
    page: number
    limit: number
}) => {
    return post('/mall-goods/domesticSubstitution/querySubstitutionGoodsByReplace', {
        browserId: params.browserId ?? '',
        commonName: params.commonName ?? '',
        catId: params.catId ?? '',
        page: params.page,
        limit: params.limit
    })
}

// 获取分类商品数据
export const getCategoryGoodsList = async (params: { catId: number; platForm?: number; goodsType?: number }) => {
    return await get('/mall-goods/web/index/goods/cat/id/query', {
        params: {
            catId: params.catId,
            platForm: params.platForm || 1,
            goodsType: params.goodsType || 1
        }
    })
}

/** 前台商品列表（按分类，公开接口） */
export const getFrontGoodsLists = async (params: {
    page?: number
    limit?: number
    categoryId?: number
    catId?: number
}) => {
    const categoryId = Number(params.categoryId || params.catId || 0) || undefined
    return await get('/web/mall/goods/front/lists', {
        params: {
            page: params.page || 1,
            limit: params.limit || 12,
            ...(categoryId ? { categoryId, catId: categoryId } : {})
        }
    })
}

/**
 * 将 mall front/detail 扁平商品转为 PC 详情页 GoodsDetailData 结构
 *（页面组件依赖 goodsBases / gdsCommon / goodsImages / shopDetail）
 */
const normalizeMallGoodsDetail = (raw: any): GoodsDetailData => {
    if (raw?.goodsBases && typeof raw.goodsBases === 'object') {
        return raw as GoodsDetailData
    }
    const id = Number(raw?.id ?? raw?.goodsId ?? 0) || 0
    const name = String(raw?.name || raw?.goodsName || raw?.commonName || '')
    const image = String(raw?.image || raw?.goodsImage || raw?.commonImage || '')
    const price = Number(raw?.minPrice ?? raw?.goodsPrice ?? raw?.price ?? 0) || 0
    const marketPrice = Number(
        raw?.minLinePrice ?? raw?.maxLinePrice ?? raw?.goodsMarketPrice ?? raw?.linePrice ?? price
    ) || price
    const stock = Number(raw?.totalStock ?? raw?.goodsStock ?? raw?.stock ?? 0) || 0
    const shopId = Number(raw?.shopId ?? 0) || 0
    const saleNum = Number(raw?.saleNum ?? raw?.goodsSalenum ?? raw?.commonSalenum ?? 0) || 0
    const code = String(raw?.code || raw?.goodsCode || '')
    const content = String(raw?.content || raw?.descript || raw?.commonBody || '')
    const video = String(raw?.videoUri || raw?.commonVideo || '')
    const catIds = String(raw?.categoryIds || '')
        .split(',')
        .map((x: string) => Number(x.trim()))
        .filter((n: number) => Number.isFinite(n) && n > 0)
    const catId = catIds[0] || 0

    const imageUrls: string[] = []
    const pushImage = (v: unknown) => {
        if (typeof v === 'string' && v.trim()) imageUrls.push(v.trim())
        else if (v && typeof v === 'object' && typeof (v as any).image === 'string' && (v as any).image.trim()) {
            imageUrls.push((v as any).image.trim())
        } else if (v && typeof v === 'object' && typeof (v as any).imagesImage === 'string' && (v as any).imagesImage.trim()) {
            imageUrls.push((v as any).imagesImage.trim())
        }
    }
    const rawImages = raw?.images ?? raw?.goods_image ?? raw?.imageList ?? raw?.goodsImages
    if (Array.isArray(rawImages)) rawImages.forEach(pushImage)
    if (!imageUrls.length && image) imageUrls.push(image)

    const skuNameList = Array.isArray(raw?.sku_name)
        ? raw.sku_name
        : Array.isArray(raw?.skuNameList)
            ? raw.skuNameList.map((n: any) => (typeof n === 'string' ? { name: n, sku_value: [] } : n))
            : []
    const specParamList = skuNameList
        .filter((it: any) => it && (it.name || it.paramName))
        .map((it: any, idx: number) => {
            const values = Array.isArray(it.sku_value)
                ? it.sku_value.map(String).filter(Boolean)
                : Array.isArray(it.paramNameValues)
                    ? it.paramNameValues.map(String).filter(Boolean)
                    : []
            return {
                paramName: String(it.name || it.paramName || `规格${idx + 1}`),
                sort: idx,
                isCommon: true,
                refSpecId: null,
                paramNameValues: values.length ? values : ['默认'],
                isOffShelf: [] as number[]
            }
        })

    const goodsBases = {
        commonId: id,
        goodsId: id,
        supplierGoodsId: 0,
        goodsName: name,
        catId,
        brandId: 0,
        brandName: null,
        brandPic: null,
        goodsSpec: specParamList.map((s: any) => s.paramName).join(',') || '默认',
        goodsPrice: price,
        goodsMarketPrice: marketPrice,
        goodsStock: stock,
        goodsAlarm: Number(raw?.stockWarning ?? 0) || 0,
        goodsCode: code,
        goodsIsRecommend: Number(raw?.shopRecommend ?? 0) === 1,
        goodsClick: Number(raw?.visitNum ?? 0) || 0,
        goodsSalenum: saleNum,
        goodsCollect: 0,
        goodsImage: imageUrls[0] || image,
        goodsEvaluationGoodStar: 0,
        goodsEvaluationCount: 0,
        goodsMaxSale: 0,
        goodsMinSale: 1,
        shopId,
        shopName: String(raw?.shopName || ''),
        goodsIsShelves: Number(raw?.status ?? 1),
        commonVerify: Number(raw?.auditStatus ?? 1),
        commodityPrice: null,
        activityPrice: null,
        commonState: Number(raw?.status ?? 1),
        commonIsShopTake: 0,
        activityStatus: null,
        activityGoodsDetail: null,
        commonType: null,
        commonLimit: 0,
        type: 0,
        commonVideo: video,
        specValue: '',
        commonIsXian: 0,
        needIntegral: null,
        promotionContent: null,
        promotionType: null,
        commonPrice: price,
        dw: '',
        moq: 1,
        memberAmount: 0,
        deliveryDate: 0,
        goodsOrderDate: null,
        hyZheKou: 0,
        goodsImages: imageUrls,
        zhuangTai: null,
        operateType: null,
        goodsType: 0,
        specValueList: [],
        commonImage: imageUrls[0] || image,
        virtualGoods: 0,
        commonEvaluate: null,
        commonSalenum: saleNum,
        isAddContrast: null,
        goodsListFirstShow: null,
        levelId: null,
        dealerGoodsFlag: 0,
        profitMargin: null,
        productListingStart: false,
        dropShippingStart: false,
        taxInclusiveNoDiscount: null,
        dealerDiscountAmout: null,
        isThousandsGuestsPrice: 0,
        supplierSource: 0,
        msrp: null
    } as GoodsBases

    const goodsImages: GoodsImage[] = imageUrls.map((url, i) => ({
        imagesImage: url,
        imagesDisplayorder: i,
        imagesIsDefault: i === 0 ? 1 : 0
    }))

    const gdsCommon = {
        commonId: id,
        commonImage: imageUrls[0] || image,
        commonSpecValue: '',
        commonSpecValueList: null,
        commonState: Number(raw?.status ?? 1),
        catName: '',
        catId,
        commonIsXian: 0,
        commonLimit: 0,
        commonType: null,
        cornerUrl: null,
        type: 0,
        commonVideo: video,
        specParam: '',
        virtualGoods: 0,
        periodOfValidity: '',
        pastDue: 0,
        specParamList,
        integralGoods: 0,
        goodsType: 0,
        dealerGoodsFlag: 0,
        dw: null,
        deliveryDate: 0,
        goodsOrderDate: null,
        customInformation: '',
        customInformationList: [],
        afterService: '',
        afterServiceList: [],
        technicalDirectorWx: null,
        technicalDirectorTel: null,
        manufacturer: null,
        categoryAttributesList: [],
        supplierSource: 0,
        commonBody: content,
        sampleType: 0
    } as GdsCommon

    const shopDetail = {
        invoiceActive: 0,
        depositStatus: 0,
        shopCollect: 0,
        shopCount: 0,
        shopId,
        shopInvoices: 0,
        shopLogo: '',
        shopName: String(raw?.shopName || ''),
        shopSelfSupport: false,
        userName: '',
        wapShopLogo: ''
    } as ShopDetail

    return {
        goodsBases,
        goodsImages,
        shopDetail,
        gdsCommon,
        manageGdsGoodsConfig: null,
        fxGoodsList: null,
        gdsBaseDetailList: [],
        zhuangTai: 0,
        qrUrl: '',
        manageGdsBaseInfoList: null
    }
}

// 获取商品详情（BFF 只认 body/query 的 id；附带 lang 做多语言覆盖）
export const getGoodsDetail = async (params: { goodsId: number }): Promise<ApiResponse<GoodsDetailData>> => {
    const body: Record<string, any> = { id: params.goodsId }
    const lang = currentLanguageCode()
    if (lang) body.lang = lang
    const res = await post<any>('/web/mall/goods/front/detail', body)
    if (res?.code === 1 && res.data) {
        return { ...res, data: normalizeMallGoodsDetail(res.data) }
    }
    return res as ApiResponse<GoodsDetailData>
}

/**
 * 获取积分商品详情接口
 * 
 * 根据商品ID获取积分商品的详细信息
 * @param params 查询参数
 * @param params.goodsId 商品ID（可以是数字或字符串）
 * @returns 积分商品详情数据
 */
export const getIntegralGoodsDetail = async (params: { goodsId: number | string }): Promise<ApiResponse<any>> => {
    return await post('/web/mall/goods/front/detail', { id: params.goodsId, goodsId: params.goodsId })
}

// 获取商品SKU列表
export const selectGoodsSku = async (params: SelectGoodsSkuParams) => {
    return await post<SelectGoodsSkuResponse>('/mall-goods/goods/base/selectGoodsSku', params)
}

// 获取商品详情描述
export const getCommonDetailDesc = async (params: { commonsId: number }) => {
    return await get<CommonDetailDesc>('/mall-goods/goods/common/desc/query', {
        params
    })
}

/**
 * 商品文件发送到邮箱
 * @param params.fileName 文件名
 * @param params.filePath 文件路径
 * @param params.email 邮箱
 */
export const sendProductFileToEmail = async (params: {
    fileName: string
    filePath: string
    email: string
}): Promise<ApiResponse<any>> => {
    return await post('/mall-goods/goods/CommonDetail/sendProductFileToEmail', params)
}

// 获取推荐商品列表
export const getRecommendGoods = async (params: RecommendGoodsParams) => {
    return await post<ListQueryResultVO[]>('/mall-goods/goods/common/recommend/goods/query', params)
}

/**
 * PLUS 激活页：推荐商品列表
 * @param params token
 */
export const selectPlusGoods = async (params: { token: string }): Promise<ApiResponse<PlusRecommendedGoodsItem[]>> => {
    return await post<PlusRecommendedGoodsItem[]>('/mall-goods/goods/common/selectPlusGoods', params, {
        skipToken: true
    })
}

// 添加商品到对比
export const addGoodsContrast = async (params: AddGoodsContrastParams) => {
    return await post('/mall-goods/goodsContrast/add', params)
}

// 删除对比商品
export const deleteGoodsContrast = async (params: DeleteGoodsContrastParams) => {
    return await post('/mall-goods/goodsContrast/delete', params)
}

// 查询对比商品
export const selectGoodsContrast = async (params: SelectGoodsContrastParams) => {
    return await post<{ body: GoodsContrastItem[] }>('/mall-goods/goodsContrast/select', params)
}

/**
 * 查询满减赠折商品列表接口
 * 
 * 获取满减赠折活动的商品列表，支持分页、排序、筛选等
 * @param params 查询参数
 * @param params.minPrice 最低价格（可选）
 * @param params.maxPrice 最高价格（可选）
 * @param params.orderType 排序类型，5表示综合排序（可选）
 * @param params.priceOrder 价格排序（可选）
 * @param params.saleNum 销量排序（可选）
 * @param params.page 页码，默认1
 * @param params.limit 每页数量，默认60
 * @param params.commentOrder 评价排序，1表示默认（可选）
 * @returns 返回包含count（总数）和result（商品列表）的数据
 */
/**
 * 查询所有商品分类接口
 * 
 * 根据父分类ID获取商品分类列表，支持三级级联
 * @param params 查询参数
 * @param params.catParentId 父分类ID，0表示获取顶级分类
 * @returns 分类列表数据，包含 catId、catName 和 childGoodsCatList（子分类列表）
 */
export const queryGoodsAll = async (params: { catParentId: number }): Promise<ApiResponse<GoodsCategoryItem[]>> => {
    return await post('/mall-goods/goods/cat/queryGoodsAll', params)
}

/**
 * 商品筛选查询接口
 * 
 * 根据筛选条件获取商品列表，支持分页、排序、筛选等
 * @param params 查询参数
 * @returns 商品列表数据，包含商品列表和分页信息
 */
export const queryGoodsFilter = async (params?: GoodsFilterQueryParams): Promise<ApiResponse<GoodsFilterQueryData>> => {
    return await post<GoodsFilterQueryData>('/web/mall/goods/front/filter', params || {})
}

/**
 * 根据商品编码文件查询商品接口
 *
 * 请求载荷：form-data，含 file（二进制文件）、token
 * @param file Excel文件（.xlsx或.xls格式）
 * @returns 匹配结果，包含匹配到的商品列表和总数
 */
export const queryGoodsByGoodsCodeFile = async (file: File): Promise<MatchedGoodsResponse> => {
    const formData = new FormData()
    formData.append('file', file)
    // token 由 useRequest 自动追加到 FormData

    const result = await post<MatchedGoodsResponse>('/mall-goods/goods/base/queryGoodsByGoodsCodeFile', formData)

    if (result.code === 1 && result.data) {
        return result.data
    } else {
        throw new Error(result.message || '查询商品失败')
    }
}

/**
 * 地方特色馆详情接口
 * GET，参数 exhibitionHallId
 * @param exhibitionHallId 展馆ID
 * @returns 详情：exhibitionHallName、exhibitionHallDesc1/2/3、commonList
 */
export const getExhibitionHallDetail = async (exhibitionHallId: number | string): Promise<ApiResponse<HallDetailData>> => {
    return await get<HallDetailData>('/mall-goods/goods/food/exhibitionHall/foodExhibitionHallDetail', {
        params: { exhibitionHallId }
    })
}

/**
 * 根据店铺ID和商品ID查询代金券模版列表
 * @param params 查询参数
 * @param params.shopId 店铺ID
 * @param params.goodsId 商品ID
 * @returns 代金券模版列表
 */
export const selectVcTempByShopId = async (params: {
    shopId: number
    goodsId: string | number
}): Promise<ApiResponse<VoucherTemplateVO[]>> => {
    return await post<VoucherTemplateVO[]>('/mall-activity/activity/voucherBase/selectVctempByShopId', {
        shopId: params.shopId,
        goodsId: String(params.goodsId)
    })
}

/**
 * 查询业务商品编码列表接口
 *
 * 根据输入的业务商品编码前缀，模糊查询匹配的商品编码列表
 * @param businessGoodsCode 业务商品编码
 * @returns 匹配的商品编码列表
 */
export const queryBusinessGoodsCodeBySub = async (businessGoodsCode: string): Promise<string[]> => {
    const response = await post<string[]>('/mall-goods/goods/base/queryBusinessGoodsCodeBySub', {
        businessGoodsCode: businessGoodsCode || ''
    })
    return response?.code === 1 ? (response.data || []) : []
}

/**
 * 根据业务商品编码查询商品详情接口
 *
 * @param businessGoodsCode 业务商品编码
 * @returns 商品详情数据，失败返回 null
 */
export const queryGoodsByBusinessGoodsCode = async (businessGoodsCode: string) => {
    const response = await post('/mall-goods/goods/base/queryGoodsByBusinessGoodsCode', {
        businessGoodsCode
    })
    return response?.code === 1 ? response.data : null
}

/**
 * 查询商品编码列表接口
 *
 * 根据输入的商品编码前缀，模糊查询匹配的商品编码列表
 * @param goodsCode 商品编码
 * @returns 匹配的商品编码列表
 */
export const queryGoodsCodeBySub = async (goodsCode: string): Promise<string[]> => {
    const response = await post<string[]>('/mall-goods/goods/base/queryGoodsCodeBySub', {
        goodsCode: goodsCode || ''
    })
    return response?.code === 1 ? (response.data || []) : []
}

/**
 * 根据商品编码查询商品详情接口
 *
 * @param goodsCode 商品编码
 * @returns 商品详情数据，失败返回 null
 */
export const queryGoodsByGoodsCode = async (goodsCode: string) => {
    const response = await post('/mall-goods/goods/base/queryGoodsByGoodsCode', {
        goodsCode
    })
    return response?.code === 1 ? response.data : null
}
