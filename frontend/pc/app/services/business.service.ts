import type { ApiResponse } from '~/types/api'
import type { QueryDeliveryTimeParams, ShopJoinAgreeData } from '~/types/business'
import type { ShopHeaderInfo, WebShopIndexVO, ShopQualificationsInfo } from '~/types/store'
import type { WebShopGoodsListDTO } from '~/types/store'
import type { QueryTransportAreaParams } from '~/types/goods'

const { post, get } = useRequest()

/**
 * 将 mall 前台商品字段归一为店铺页 ProductCard 所需结构
 */
const normalizeShopGoodsItem = (item: any) => {
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
        goodsSalenum: sales
    }
}

/** 店铺详情 → 店铺头部信息（旧 mall-business 字段） */
const mapShopDetailToHeader = (detail: any, shopId: number | string): ShopHeaderInfo => ({
    shopId: Number(detail?.id ?? shopId) || Number(shopId) || 0,
    shopName: String(detail?.name || ''),
    shopLogo: String(detail?.logo || ''),
    shopcollect: Number(detail?.fans ?? 0) || 0,
    shopSelfSupport: Number(detail?.is_self) === 1,
    type: Number(detail?.is_collect) === 1 ? 1 : 2,
    shopBanner: String(detail?.logo || ''),
    qrUrl: ''
})

/**
 * 查询配送时间接口
 *
 * 查询商品在指定区域的配送时间
 * @param params 查询参数
 * @param params.shopId 店铺ID
 * @param params.provinceId 省份ID
 * @param params.cityId 城市ID
 * @param params.areaId 区县ID
 * @returns 配送时间字符串
 */
export const queryDeliveryTime = async (params: QueryDeliveryTimeParams) => {
    // 直连 merchant（business 别名曾被错误回写成 business → 127.0.0.1:0 → 502）
    return await post<string>('/web/mall/shop/shipping/address/queryDeliveryTime', params)
}

/**
 * 查询商品配送区域接口
 *
 * 查询商品在指定区域的配送情况
 * @param params 查询参数
 * @param params.shopGoodsIds 商品ID二维数组，格式：[[goodsId]]
 * @param params.areaIds 省市区ID数组
 * @param params.countryId 国家ID
 * @returns 配送区域数据，data[0][0] === 1 表示有货
 */
export const queryTransportArea = async (params: QueryTransportAreaParams) => {
    // 直连 BFF→goods，避免 mall-business 旧路径在缓存包/漏改写时打到 merchant 404→502
    // 短超时：经典轨常慢/不可用，尽快回退本地库存，避免拖垮详情页
    return await post('/web/mall/goods/transport/area/query', params, { timeout: 5000 })
}

/**
 * 查询是否支持货到付款
 * @param params.shopIdList 店铺ID列表
 * @returns data.isDelivery 0-不支持 1-支持
 */
export const queryIsDelivery = async (params: { shopIdList: number[] }): Promise<ApiResponse<{ shopId?: number | null; isDelivery?: number }>> => {
    // business 服务已下线并入 merchant，但 COD 查询未迁完；走 merchant legacy + BFF 软兜底
    return await post('/web/mall/shop/cashOnDelivery/queryIsDelivery', params)
}

/**
 * 查询店铺商品列表（店铺内筛选）
 * 收口到 /web/mall/shop/goods，兼容旧 listFilter 的 list/totalCount 结构
 */
export const getShopGoodsListFilter = async (params?: WebShopGoodsListDTO): Promise<ApiResponse<{
    list?: any[]
    goodsList?: any[]
    totalCount?: number
}>> => {
    const shopId = params?.shopId
    const page = Number(params?.page ?? params?.currentPage ?? 1) || 1
    const limit = Number(params?.limit ?? 8) || 8
    const needLocalFilter = Number(params?.commonIsRecommend) === 1
        || params?.priceOrder != null
        || params?.saleNum != null
    const query = new URLSearchParams()
    if (shopId != null && shopId !== '') query.set('shopId', String(shopId))
    // 推荐/本地排序时先拉足量再切片；普通分页直接走服务端
    query.set('page', needLocalFilter ? '1' : String(page))
    query.set('limit', needLocalFilter ? String(Math.max(limit * page, 100)) : String(limit))
    if (params?.commonName) query.set('name', String(params.commonName))
    const catId = params?.shopCatId || params?.catParentId
    if (catId != null && catId !== '') query.set('cid', String(catId))

    const res = await get<any>(`/web/mall/shop/goods?${query.toString()}`)
    const data = res?.data
    let rawList: any[] = Array.isArray(data?.lists)
        ? data.lists
        : Array.isArray(data?.list)
            ? data.list
            : Array.isArray(data?.goodsList)
                ? data.goodsList
                : []

    if (Number(params?.commonIsRecommend) === 1) {
        rawList = rawList.filter((item) => Number(item?.shopRecommend ?? item?.commonIsRecommend ?? 0) === 1)
    }

    // 兼容旧排序参数（小店本地排序即可）
    if (params?.priceOrder === 1) {
        rawList = [...rawList].sort((a, b) => Number(b.minPrice ?? b.goodsPrice ?? 0) - Number(a.minPrice ?? a.goodsPrice ?? 0))
    } else if (params?.priceOrder === 2) {
        rawList = [...rawList].sort((a, b) => Number(a.minPrice ?? a.goodsPrice ?? 0) - Number(b.minPrice ?? b.goodsPrice ?? 0))
    } else if (params?.saleNum === 1) {
        rawList = [...rawList].sort((a, b) => Number(b.saleNum ?? b.allSaleNum ?? 0) - Number(a.saleNum ?? a.allSaleNum ?? 0))
    } else if (params?.saleNum === 2) {
        rawList = [...rawList].sort((a, b) => Number(a.saleNum ?? a.allSaleNum ?? 0) - Number(b.saleNum ?? b.allSaleNum ?? 0))
    }

    const totalCount = needLocalFilter
        ? rawList.length
        : (Number(data?.count ?? data?.totalCount ?? rawList.length) || rawList.length)
    const pageList = (needLocalFilter
        ? rawList.slice(Math.max(0, (page - 1) * limit), (page - 1) * limit + limit)
        : rawList
    ).map(normalizeShopGoodsItem)

    return {
        ...res,
        code: res?.code ?? 1,
        data: {
            ...(typeof data === 'object' && data ? data : {}),
            list: pageList,
            goodsList: pageList,
            totalCount
        }
    }
}

/**
 * 查询店铺头部信息接口
 *
 * 根据店铺ID获取店铺头部信息（包括关注状态、横幅、二维码等）
 * @param shopId 店铺ID
 * @returns 店铺头部信息数据
 */
export const getShopHeaderInfo = async (shopId: number | string): Promise<ApiResponse<ShopHeaderInfo>> => {
    const res = await get<any>(`/web/mall/shop/detail?id=${encodeURIComponent(String(shopId))}`)
    if (res?.code === 1 && res.data) {
        return {
            ...res,
            data: mapShopDetailToHeader(res.data, shopId)
        }
    }
    return res as ApiResponse<ShopHeaderInfo>
}

/**
 * 查询店铺首页信息接口
 *
 * 根据店铺ID获取店铺首页信息（包括店铺信息、自定义分类、轮播图等）
 * @param shopId 店铺ID
 * @returns 店铺首页信息数据
 */
export const getShopIndexInfo = async (shopId: number | string): Promise<ApiResponse<WebShopIndexVO>> => {
    const [detailRes, catRes] = await Promise.all([
        get<any>(`/web/mall/shop/detail?id=${encodeURIComponent(String(shopId))}`),
        get<any>(`/web/mall/shop/category?id=${encodeURIComponent(String(shopId))}`)
    ])

    if (detailRes?.code !== 1 || !detailRes.data) {
        return detailRes as ApiResponse<WebShopIndexVO>
    }

    const detail = detailRes.data
    const cats = Array.isArray(catRes?.data) ? catRes.data : []
    const header = mapShopDetailToHeader(detail, shopId)

    return {
        code: 1,
        message: detailRes.message || '操作成功',
        data: {
            shopBase: {
                shopId: header.shopId,
                shopName: header.shopName,
                shopLogo: header.shopLogo,
                wapShopLogo: header.shopLogo,
                shopCollect: header.shopcollect,
                shopBanner: header.shopBanner,
                shopImage: header.shopLogo,
                shopSlideurlList: []
            },
            shopSeltCatList: cats.map((c: any, index: number) => ({
                shopGoodsCatId: Number(c?.id ?? 0) || 0,
                shopGoodsCatName: String(c?.name || ''),
                shopGoodsCatDisplayorder: index,
                children: []
            }))
        }
    }
}

/**
 * 查询店铺资质验证码接口
 *
 * 根据店铺ID获取店铺资质验证码图片（base64格式）
 * @param shopId 店铺ID
 * @returns 验证码图片base64数据
 */
export const queryShopQualificationsCode = async (shopId: number | string): Promise<ApiResponse<string>> => {
    return await post<string>('/mall-business/shopBase/info/queryShopQualificationsCode', {
        shopId: String(shopId)
    })
}

/**
 * 查询店铺资质信息接口
 *
 * 根据验证码和店铺ID获取店铺资质信息
 * @param params 请求参数
 * @param params.code 验证码
 * @param params.shopId 店铺ID
 * @returns 店铺资质信息数据
 */
export const queryShopQualifications = async (params: {
    code: string
    shopId: number | string
}): Promise<ApiResponse<ShopQualificationsInfo>> => {
    return await post<ShopQualificationsInfo>('/mall-business/shopBase/info/queryShopQualifications', {
        code: params.code,
        shopId: String(params.shopId)
    })
}

/**
 * 商家入驻状态查询
 * POST /mall-business/shopBase/info/shop/status/query
 * @returns 入驻状态数据
 */
export const shopJoinAgree = async (): Promise<ApiResponse<ShopJoinAgreeData>> => {
    return await post<ShopJoinAgreeData>('/mall-business/shopBase/info/shop/status/query', {})
}

/**
 * 查询会员规则与隐私协议（店铺）
 * @param params.shopId 店铺ID
 * @returns { code, data: html, message }
 */
export const queryMemberRuleAgreement = async (params: { shopId: number | string }): Promise<ApiResponse<string>> => {
    return await post<string>('/mall-business/shopBase/info/queryMemberRuleAgreement', {
        shopId: String(params.shopId)
    })
}
