import type { ApiResponse, ApiParams } from '~/types/api'
import type { MerchantSysConfigResponse, BaseArticleGroupEntity, BaseArticleBaseEntity, HelpInfoData, InformationSliderConfig, InformationClassifyItem, InformationListData, InformationDetailData, NextChapterData, IntegralIndexData, BankCardInfo, DetectingImStatusData } from '~/types/base'
import {
    DEMO_INFORMATION_CLASSIFY,
    DEMO_INFORMATION_LIST,
    findDemoInformation,
    normalizeInformationNavUrl
} from '~/utils/demoInformation'
const { post, get } = useRequest()

/**
 * 底部帮助接口
 * 
 * 获取页面底部的帮助信息（如常见问题、联系我们等）
 * @param params 查询参数（可选）
 * @returns 帮助信息数据
 */
export const getHelpInfo = async (params?: ApiParams): Promise<ApiResponse<HelpInfoData>> => {
    const response: any = await getMallIndexConfig()
    const data = response?.data || {}
    return {
        ...response,
        data: {
            articleGroups: data.articleGroups || data.help || [],
            webColor: data.style?.selectedColor || data.mall_style?.color || ''
        }
    } as ApiResponse<HelpInfoData>
}

/**
 * 商户系统配置查询接口
 * 
 * 根据配置键获取商户系统配置信息
 * @param configKey 配置键
 * @returns 配置信息数据
 */
export const selectByConfigKey = async (configKey: string) => {
    const response: any = await getMallIndexConfig()
    const data = response?.data || {}
    const logoConfigs = data.logo_copywriting || data.logoCopywriting || data.configs || {}
    const website = data.website || {}
    const aliases: Record<string, string> = {
        setting_logo: website.shop_logo || website.shopLogo || '',
        setting_phone: website.mobile || website.phone || website.contact || '',
        setting_copyright: website.copyright || website.shop_name || website.name || '',
        setting_records: website.icp || website.records || website.record || '',
        cybersecurity_filing_number: website.cybersecurity_filing_number || website.cybersecurityFilingNumber || '',
        mobile_qrcode_image: website.mobile_qrcode_image || website.mobileQrcodeImage || ''
    }
    let value: any = aliases[configKey] || ''
    if (Array.isArray(logoConfigs)) {
        value = logoConfigs.find((item: any) => item?.configKey === configKey)?.configValue ?? value
    } else if (logoConfigs && typeof logoConfigs === 'object' && configKey in logoConfigs) {
        value = (logoConfigs as any)[configKey]
    }
    return { ...response, data: { configKey, configValue: value } }
}

/**
 * 检测客服配置状态接口
 * @param params 查询参数，configKey: 'customer_service_type'
 * @returns 客服配置数据
 */
export const detectingImStatus = async (params: { configKey: string }): Promise<ApiResponse<DetectingImStatusData>> => {
    return await get<DetectingImStatusData>('/web/base/index/detecting-im-status', {
        params
    })
}

/**
 * 推荐咨询接口
 * 
 * 获取推荐咨询列表信息
 * @param params 查询参数（可选）
 * @returns 推荐咨询列表数据
 */
export const getUserQueryInformationInfo = async (params?: ApiParams) => {
    try {
        const res: any = await post('/mall-base/baseInformationInfo/userQueryInformationInfo', params)
        const list = Array.isArray(res?.data) ? res.data : []
        if (res?.code === 1 && list.length > 0) {
            return res
        }
    } catch (e) {
        console.warn('推荐资讯接口不可用，使用演示数据', e)
    }
    const recommendOnly = params && (params as any).isRecommend != null
    const list = recommendOnly
        ? DEMO_INFORMATION_LIST.filter((item) => item.isRecommend === 1)
        : DEMO_INFORMATION_LIST
    return { code: 1, message: '操作成功', data: list }
}

/**
 * 查询导航Logo和文案配置接口
 * 
 * 获取导航Logo和相关文案配置信息
 * @param params 查询参数
 * @returns 配置信息数据数组
 */
export const queryNavLogo = async (params?: ApiParams) => {
    const response = await getMallIndexConfig()
    const raw = response?.data?.logo_copywriting || response?.data?.logoCopywriting || response?.data?.configs
    const website = response?.data?.website || {}
    const configs = Array.isArray(raw)
        ? raw
        : raw && typeof raw === 'object'
            ? Object.entries(raw).map(([configKey, configValue]) => ({ configKey, configValue }))
            : []
    const websiteConfigs = [
        ['setting_logo', website.shop_logo || website.shopLogo],
        ['logo_copywriting_ico', website.web_favicon || website.shop_browser_icon],
        ['logo_copywriting_ico_pc', website.shop_name || website.name],
        ['setting_phone', website.mobile || website.phone || website.contact],
        ['setting_copyright', website.copyright || website.shop_name || website.name],
        ['setting_records', website.icp || website.records || website.record]
    ].filter(([, value]) => value != null && String(value) !== '')
        .map(([configKey, configValue]) => ({ configKey, configValue: String(configValue) }))
    const merged = [...configs, ...websiteConfigs]
    const filtered = params?.configKey
        ? merged.filter((item: any) => item.configKey === params.configKey)
        : merged
    return {
        ...response,
        data: filtered
    }
}

/**
 * 查询会员授权协议（店铺）
 * @param params.shopId 店铺ID
 * @returns { code, data: html, message }
 */
export const queryMemberAuthorizationAgreement = async (params: { shopId: number | string }): Promise<ApiResponse<string>> => {
    return await post<string>('/mall-base/base/merchantSysConfig/setting/queryMemberAuthorizationAgreement', {
        shopId: String(params.shopId)
    })
}

/**
 * 查询商户是否开启 PLUS 会员（平台开关）
 * @returns { code, data: 1 开启 / 0 关闭, message }
 */
export const queryMerchantPlusRule = async (params: ApiParams = {}): Promise<ApiResponse<number>> => {
    return await post<number>('/web/base/merchant-plus-rule/query', params)
}

/**
 * 查询 PLUS 会员用户协议（返回 HTML）
 * POST /mall-base/base/merchantPlusRule/selectPlusUser
 * @returns { code, data: html, message }
 */
export const selectPlusUser = async (params: ApiParams = {}): Promise<ApiResponse<string>> => {
    return await post<string>('/mall-base/base/merchantPlusRule/selectPlusUser', params)
}

/**
 * 获取省市区三级联动数据接口
 * 
 * 获取完整的省市区三级联动数据
 * @param params 查询参数，包含token
 * @returns 省市区三级联动数据
 */
export const queryDistrictAll = async (params?: ApiParams) => {
    return await post('/mall-base/base/district/all/district/query', params || {})
}

/**
 * 根据访问者 IP 粗定位国家/省市（BFF：ip-api + 太平洋 IP 库）
 */
export const getVisitorIpLocation = async (): Promise<ApiResponse<{
    ip?: string
    country?: string
    countryCode?: string
    province?: string
    city?: string
    region?: string
    proCode?: string
    cityCode?: string
    addr?: string
    internal?: boolean
    source?: string
}>> => {
    return await get('/web/mall/front/location/ip')
}

/**
 * 查询国家和货币接口
 * 
 * 获取国家列表和货币信息
 * @param params 查询参数，包含token
 * @returns 国家列表数据
 */
export const queryCountryAndCurrency = async (params?: ApiParams) => {
    return await post('/mall-base/baseCountry/queryCountryAndCurrency', params || {})
}

/**
 * 查询平台应用开关接口
 * 
 * 根据配置键查询平台是否开启应用
 * @param configKey 配置键
 * @param options 可选请求配置（如 timeout / retry）
 * @returns 配置信息数据（包含configValue、configKey等）
 */
export const queryLanguage = async (
    configKey: string,
    options?: { timeout?: number; retry?: number | false }
): Promise<ApiResponse<MerchantSysConfigResponse>> => {
    return await get<MerchantSysConfigResponse>('/web/base/sys-config/switch/query-language', {
        params: {
            configKey
        },
        ...options
    })
}

/**
 * 查询文章分类列表接口
 * 
 * 获取所有文章分类（一级导航）
 * @param params 查询参数（可选）
 * @returns 文章分类列表数据
 */
export const queryArticleGroup = async (params?: ApiParams): Promise<ApiResponse<BaseArticleGroupEntity[]>> => {
    return await post<BaseArticleGroupEntity[]>('/mall-base/base/article/group/query', params || {})
}

/**
 * 查询文章详情接口
 *
 * @param articleId 文章 ID
 * @returns 单篇文章详情
 */
export const queryArticleGroupDetail = async (articleId: number): Promise<ApiResponse<BaseArticleBaseEntity>> => {
    return await post<BaseArticleBaseEntity>('/mall-base/base/articleBase/query/detail', {
       articleId
    })
}

/**
 * 查询业务开关接口
 * 
 * 获取注册类型开关配置
 * @param params 查询参数（可选，默认传空对象）
 * @returns 业务开关数据，data: 0-个人注册，1-企业注册，2-均可注册
 */
export const queryBusinessSwitch = async (params?: ApiParams): Promise<ApiResponse<number>> => {
    return await post<number>('/mall-base/businessSwitch/query', params || {})
}

import type { PasswordConfig } from '~/types/base'

/**
 * 查询密码规则配置接口
 * 
 * 获取系统密码格式和长度规则
 * @param params 查询参数（可选，默认传空对象）
 * @returns 密码规则配置数据
 */
export const queryPasswordConfig = async (params?: ApiParams): Promise<ApiResponse<PasswordConfig>> => {
    return await post<PasswordConfig>('/mall-base/base/sysConfig/business/passwordConfig', params || {})
}

/**
 * 查询资讯轮播图配置接口
 * 
 * 获取资讯中心左侧轮播图的配置信息
 * @param params 查询参数，包含 configType: 'information_slider'
 * @returns 轮播图配置数据数组
 */
export const queryInformationSlider = async (params?: ApiParams): Promise<ApiResponse<InformationSliderConfig[]>> => {
    return await post<InformationSliderConfig[]>('/mall-base/base/sysConfig/information/slider/query', params || {
        configType: 'information_slider'
    })
}

/**
 * 查询所有资讯分类接口
 * 
 * 获取所有资讯分类列表，包含子分类
 * @param params 查询参数（可选）
 * @returns 资讯分类数据数组
 */
export const queryAllInformationClassify = async (params?: ApiParams): Promise<ApiResponse<InformationClassifyItem[]>> => {
    try {
        const res = await get<InformationClassifyItem[]>('/mall-base/baseInformationClassify/queryAll', {
            params: params || {}
        })
        const list = Array.isArray(res?.data) ? res.data : []
        if (res?.code === 1 && list.length > 0) {
            return res
        }
    } catch (e) {
        console.warn('资讯分类接口不可用，使用演示分类', e)
    }
    return {
        code: 1,
        message: '操作成功',
        data: DEMO_INFORMATION_CLASSIFY as InformationClassifyItem[]
    }
}

/**
 * 根据ID查询资讯列表接口
 * 
 * 获取资讯列表，支持按分类筛选和分页
 * @param params 查询参数，包含 classifyId（分类ID，空字符串表示全部）、page（页码）、limit（每页数量）
 * @returns 资讯列表分页数据
 */
export const userQueryInformationInfoById = async (params: {
    classifyId?: string | number
    page?: number
    limit?: number
}): Promise<ApiResponse<InformationListData>> => {
    try {
        const res = await post<InformationListData>('/mall-base/baseInformationInfo/userQueryInformationInfoById', {
            classifyId: params.classifyId || '',
            page: params.page || 1,
            limit: params.limit || 10
        })
        const list = res?.data?.list
        if (res?.code === 1 && Array.isArray(list) && list.length > 0) {
            return res
        }
    } catch (e) {
        console.warn('资讯列表接口不可用，使用演示资讯', e)
    }
    const page = Number(params.page || 1) || 1
    const limit = Number(params.limit || 10) || 10
    const classifyId = params.classifyId === '' || params.classifyId == null ? '' : String(params.classifyId)
    let rows = [...DEMO_INFORMATION_LIST]
    if (classifyId) {
        const cid = Number(classifyId)
        const childIds = DEMO_INFORMATION_CLASSIFY
            .filter((c) => Number(c.classifyId) === cid)
            .flatMap((c) => (c.childMenu || []).map((x: any) => Number(x.classifyId)))
        const allow = new Set<number>([cid, ...childIds])
        rows = rows.filter((item) => allow.has(Number(item.classifyId)))
    }
    const totalCount = rows.length
    const start = (page - 1) * limit
    const list = rows.slice(start, start + limit)
    return {
        code: 1,
        message: '操作成功',
        data: {
            currPage: page,
            pageSize: limit,
            totalCount,
            totalPage: Math.max(1, Math.ceil(totalCount / limit) || 1),
            list: list as any
        }
    }
}

/**
 * 查询资讯详情接口
 * 
 * 根据资讯ID获取资讯详情信息
 * @param params 查询参数，包含 informationId（资讯ID）
 * @returns 资讯详情数据
 */
export const queryInformationInfo = async (params: {
    informationId: string | number
}): Promise<ApiResponse<InformationDetailData>> => {
    try {
        const res = await post<InformationDetailData>('/mall-base/baseInformationInfo/queryInformationInfo', {
            informationId: params.informationId
        })
        if (res?.code === 1 && res?.data && (res.data as any).informationId) {
            return res
        }
    } catch (e) {
        console.warn('资讯详情接口不可用，使用演示资讯', e)
    }
    const demo = findDemoInformation(params.informationId)
    if (demo) {
        return {
            code: 1,
            message: '操作成功',
            data: {
                ...demo,
                createTime: demo.releaseTime,
                updateTime: demo.releaseTime,
                classifyParentId: null
            } as any
        }
    }
    return { code: 0, message: '资讯不存在', data: null as any }
}

/**
 * 查询资讯上一篇下一篇和推荐接口
 * 
 * 根据分类ID和资讯ID获取上一篇、下一篇和推荐资讯
 * @param params 查询参数，包含 classifyId（分类ID）、informationId（资讯ID）
 * @returns 上一篇、下一篇和推荐资讯数据
 */
export const queryInformationInfoNextChapter = async (params: {
    classifyId: string | number
    informationId: string | number
}): Promise<ApiResponse<NextChapterData>> => {
    try {
        const res = await post<NextChapterData>('/mall-base/baseInformationInfo/queryInformationInfoNextChapter', {
            classifyId: String(params.classifyId),
            informationId: String(params.informationId)
        })
        if (res?.code === 1 && res?.data) {
            return res
        }
    } catch (e) {
        console.warn('资讯上下篇接口不可用，使用演示数据', e)
    }
    const id = Number(params.informationId)
    const idx = DEMO_INFORMATION_LIST.findIndex((item) => Number(item.informationId) === id)
    const previous = idx > 0 ? DEMO_INFORMATION_LIST[idx - 1] : null
    const next = idx >= 0 && idx < DEMO_INFORMATION_LIST.length - 1 ? DEMO_INFORMATION_LIST[idx + 1] : null
    const recommended = DEMO_INFORMATION_LIST.filter((item) => Number(item.informationId) !== id).slice(0, 4)
    return {
        code: 1,
        message: '操作成功',
        data: {
            previous: previous as any,
            next: next as any,
            recommended: recommended as any
        } as any
    }
}

/**
 * PC 首页楼层数据（原 goods.service 的 newQueryPcIndex）
 * POST /mall-base/web/index/newQueryPcIndex
 */
export const newQueryPcIndex = async (params?: ApiParams) => {
    return await post('/mall-base/web/index/newQueryPcIndex', params || {})
}

/**
 * 新版商城首页 BFF 接口。首页装修、分类和全局配置必须从这里读取，
 * 不再依赖已下线的 mall-base/web/index/* 接口。
 */
export const getMallIndexConfig = async () => {
    return await get('/web/mall/index/config')
}

export const getMallIndexCategory = async () => {
    return await get('/web/mall/index/category')
}

/** 首页接口短缓存 + 单飞：index/Banner/楼层同时挂载时避免重复打爆 mall 连接池 */
const HOME_API_TTL_MS = 30_000
let mallIndexInflight: { key: string; promise: Promise<any> } | null = null
let mallIndexCache: { at: number; key: string; data: any } | null = null
let mallHomeInflight: Promise<any> | null = null
let mallHomeCache: { at: number; data: any } | null = null

export const getMallIndexData = async (params: ApiParams = {}) => {
    const key = JSON.stringify(params || {})
    const now = Date.now()
    if (mallIndexCache && mallIndexCache.key === key && now - mallIndexCache.at < HOME_API_TTL_MS) {
        return mallIndexCache.data
    }
    if (mallIndexInflight && mallIndexInflight.key === key) {
        return mallIndexInflight.promise
    }
    const promise = post('/web/mall/index/data', params)
        .then((data) => {
            mallIndexCache = { at: Date.now(), key, data }
            return data
        })
        .finally(() => {
            if (mallIndexInflight?.promise === promise) {
                mallIndexInflight = null
            }
        })
    mallIndexInflight = { key, promise }
    return promise
}

/** PC 首页旧版楼层/楼层广告聚合数据（由 BFF 转发 base/floorAdpage） */
let mallFloorsInflight: Promise<any> | null = null
let mallFloorsCache: { at: number; data: any } | null = null
export const getMallIndexFloors = async () => {
    const now = Date.now()
    if (mallFloorsCache && now - mallFloorsCache.at < HOME_API_TTL_MS) return mallFloorsCache.data
    if (mallFloorsInflight) return mallFloorsInflight
    mallFloorsInflight = get('/web/mall/index/floors')
        .then((data) => {
            mallFloorsCache = { at: Date.now(), data }
            return data
        })
        .finally(() => { mallFloorsInflight = null })
    return mallFloorsInflight
}

/** 首页聚合数据：轮播、快报、推荐商品（/web/mall/home/data） */
export const getMallHomeAggregate = async () => {
    const now = Date.now()
    if (mallHomeCache && now - mallHomeCache.at < HOME_API_TTL_MS) {
        return mallHomeCache.data
    }
    if (mallHomeInflight) {
        return mallHomeInflight
    }
    mallHomeInflight = get('/web/mall/home/data')
        .then((data) => {
            mallHomeCache = { at: Date.now(), data }
            return data
        })
        .finally(() => {
            mallHomeInflight = null
        })
    return mallHomeInflight
}

export const getMallIndexHotSearch = async () => {
    return await get('/web/mall/index/hotSearch')
}

/** 将首页装修数据拍平成组件列表，兼容 [ [page-meta], component, ... ] 格式。 */
export const flattenMallIndexModules = (response: any): any[] => {
    const pageData = response?.data?.page?.data ?? response?.data?.data ?? response?.data ?? []
    if (!Array.isArray(pageData)) return []
    // mall 服务返回的装修页数据有两种形态：直接数组，或分页包装为
    // { value: [...], Count: n }。统一递归展开，避免把包装对象当作楼层，
    // 否则首页楼层导航和模块渲染都会拿不到 name/content。
    const flatten = (items: any[]): any[] => items.flatMap((item: any) => {
        if (!item) return []
        if (Array.isArray(item)) return flatten(item)
        if (Array.isArray(item.value)) return flatten(item.value)
        return [item]
    })
    return flatten(pageData).filter((item: any) => item && item.disabled !== 1 && item.disabled !== '1')
}

/**
 * 首页轮播图、热搜、导航、分类数据
 * GET /mall-base/web/index/query
 */
export const getIndexSlides = (params: { integralType?: number } = { integralType: 0 }) => {
    return getMallIndexConfig().then((response: any) => {
        const data = response?.data || {}
        // 热搜配置在不同版本接口中可能是字符串 JSON、字符串数组或对象数组。
        // 统一转换为可直接渲染的文本，避免页面把 { id, name, sort } 原样输出。
        const normalizeHotWords = (raw: unknown): string[] => {
            let value: unknown = raw
            if (typeof value === 'string') {
                const text = value.trim()
                if (!text) return []
                try {
                    value = JSON.parse(text)
                } catch {
                    return text.split(/[,，|、\n]/).map((item) => item.trim()).filter(Boolean)
                }
            }
            if (value && typeof value === 'object' && !Array.isArray(value)) {
                const record = value as Record<string, unknown>
                value = record.data ?? record.value ?? record.list ?? record.items ?? record.hotWords ?? []
                if (typeof value === 'string') {
                    try {
                        value = JSON.parse(value)
                    } catch {
                        value = value.split(/[,，|、\n]/).map((item) => item.trim()).filter(Boolean)
                    }
                }
            }
            if (!Array.isArray(value)) return []
            return value
                .map((item: unknown) => {
                    if (typeof item === 'string' || typeof item === 'number') return String(item).trim()
                    if (item && typeof item === 'object') {
                        const record = item as Record<string, unknown>
                        return String(record.name ?? record.keyword ?? record.word ?? record.title ?? record.label ?? '').trim()
                    }
                    return ''
                })
                .filter(Boolean)
        }
        const rawPlatformNav = data.mall_style?.pc_nav || data.platformNav || []
        const platformNav = Array.isArray(rawPlatformNav)
            ? rawPlatformNav
                .map((item: unknown) => {
                    if (!item || typeof item !== 'object') return null
                    const record = item as Record<string, unknown>
                    const navTitle = String(record.navTitle ?? record.name ?? record.title ?? '').trim()
                    const navUrl = normalizeInformationNavUrl(
                        String(record.navUrl ?? record.path ?? record.url ?? '/').trim() || '/'
                    )
                    if (!navTitle || record.visible === false || record.visible === 0 || record.visible === '0') return null
                    return {
                        navTitle,
                        navUrl,
                        navNewOpen: Number(record.navNewOpen ?? record.target ?? 0) || 0,
                        navImage: String(record.navImage ?? record.image ?? '').trim()
                    }
                })
                .filter(Boolean)
            : []
        return {
            ...response,
            data: {
                ...data,
                hotWords: normalizeHotWords(data.hot_search?.data ?? data.hotWords),
                platformNav,
                goodsCats: []
            }
        }
    })
}

const mapHomeGoodsToPromo = (list: any[] = []) =>
    (Array.isArray(list) ? list : [])
        .filter((item: any) => item && (item.goodsImage || item.image || item.goodsId || item.id))
        .map((item: any) => ({
            images: item.goodsImage || item.image || item.commonImage || '',
            commonImage: item.goodsImage || item.image || item.commonImage || '',
            commonName: item.goodsName || item.commonName || item.name || '',
            commonPrice: item.goodsPrice ?? item.commonPrice ?? item.price ?? '',
            price: item.goodsPrice ?? item.commonPrice ?? item.price ?? '',
            commonId: item.goodsId || item.commonId || item.id || '',
            goodsId: item.goodsId || item.id || '',
            dataType: 1,
            copywriting: item.label || item.copywriting || '',
            labelText: item.label || ''
        }))

const mapHomeBannersToSlide = (list: any[] = []) =>
    (Array.isArray(list) ? list : [])
        .map((item: any) => ({
            configValue: item.imageUrl || item.image || item.bannerImage || item.img || '',
            opValue: item.linkUrl || item.link || item.url || item.opValue || '',
            opType: Number(item.opType ?? item.linkType ?? 3) || 3,
            configEnable: item.status ?? item.isShow ?? 1,
            opValueLink: item
        }))
        .filter((item: any) => !!item.configValue)

/**
 * 查询首页轮播区模块（含轮播 indexSlideData 及模块1-5）
 * 优先装修页模块；缺数据时回退 /web/mall/home/data 聚合推荐
 */
export const queryPcIndexSlideModule = async (params: ApiParams = {}) => {
    const [response, homeRes] = await Promise.all([
        getMallIndexData(params),
        getMallHomeAggregate().catch(() => null)
    ])
    const modules = flattenMallIndexModules(response)
    const findModule = (name: string) => modules.find((item: any) => item?.name === name && item?.disabled !== 1)
    const banner = findModule('banner')
    let indexSlideData = (banner?.content?.data || []).map((item: any) => ({
        configValue: item.image || '',
        opValue: item.link?.query?.id || item.link?.path || '',
        opType: item.link?.type === 'article' ? 3 : 0,
        configEnable: item.is_show ?? 1,
        opValueLink: item.link
    }))
    const homeData = (homeRes as any)?.data || {}
    if (!indexSlideData.length) {
        indexSlideData = mapHomeBannersToSlide(homeData.banners || [])
    }
    /** 聚合推荐分区，避免各模块重复同一批商品 */
    const hot = mapHomeGoodsToPromo(homeData.hotGoods || [])
    const neu = mapHomeGoodsToPromo(homeData.newGoods || [])
    const guess = mapHomeGoodsToPromo(homeData.guessGoods || [])
    const popular = mapHomeGoodsToPromo(homeData.popularGoods || [])
    const pool = [...popular, ...hot, ...neu, ...guess]
    const uniq: any[] = []
    const seen = new Set<string>()
    for (const item of pool) {
        const key = String(item.goodsId || item.commonId || item.images || '')
        if (!key || seen.has(key)) continue
        seen.add(key)
        uniq.push(item)
    }
    const mapModule = (name: string, fallbackTitle: string, fallbackGoods: any[]) => {
        const item = findModule(name)
        const raw = item?.content?.data || item?.content?.goodsLists?.list || []
        const hasGoods = Array.isArray(raw) && raw.some((row: any) => row && (row.images || row.commonImage || row.goodsImage || row.image || row.goodsId || row.commonId || row.id))
        return {
            title: item?.content?.title || item?.title || fallbackTitle,
            data: hasGoods ? raw : fallbackGoods
        }
    }
    return {
        ...response,
        data: {
            ...(response as any)?.data,
            indexSlideData,
            index_slider_module1: mapModule('subsidy', '国家补贴', uniq.slice(0, 4)),
            // 新品上市模块已下线，预留空位避免旧接口结构断裂
            index_slider_module2: { title: '', data: [] },
            index_slider_module3: mapModule('live', '精选直播', uniq.slice(2, 3)),
            index_slider_module4: mapModule('seckill', '限时秒杀', uniq.slice(4, 6)),
            index_slider_module5: mapModule('discount', '满减折扣', uniq.slice(6, 8))
        }
    }
}

/**
 * 获取子分类数据
 * GET /mall-base/web/index/itemcat/query
 */
export const getSubCategoryData = async (params: { catParentId: number }) => {
    return await get('/mall-base/web/index/itemcat/query', {
        params
    })
}

/**
 * 查询积分商城首页配置接口
 * 
 * 获取积分商城首页的配置信息，包括左侧大图和右侧广告图
 * @param params 查询参数（可选，默认传空对象）
 * @returns 积分商城首页配置数据，包含配置列表
 */
export const queryIntegralIndex = async (params?: ApiParams): Promise<ApiResponse<IntegralIndexData>> => {
    return await post<IntegralIndexData>('/mall-base/base/sysConfig/integral/queryIndex', params || {})
}

/**
 * 查询银行卡信息接口
 *
 * 获取对公转账所需的银行卡信息
 * @param params 请求参数，可为空对象
 * @returns 银行卡信息对象
 */
export const queryBankCard = async (params: ApiParams = {}): Promise<ApiResponse<BankCardInfo>> => {
    return await post<BankCardInfo>('/mall-base/base/platformBrank/queryBrank', params)
}

/**
 * 查询全部已启用语言列表
 *
 * 只走 BFF `/web/thirdparty/baidu/queryLanguageAll`（底层是百度翻译语言表）。
 * 该接口在未配置百度翻译 AK 时返回 code=1 + data:[]（成功但无数据），按「无多语言」处理即可。
 *
 * 🔴 不要再回退 `/baseLanguage/queryLanguageType`：base 服务从来没有这个接口
 * （只有 base/languageTranslation 语言包翻译一套），回退必然 404，控制台刷
 * 「获取语言列表失败」红字，而 Header 本身就有「只有中文」的降级，回退没有任何收益。
 * 返回 { code, data: [{ "en": "English" }, ...], message }
 */
export const queryLanguageAll = async () => {
    try {
        const res = await post('/web/thirdparty/baidu/queryLanguageAll', {})
        if (res && Number((res as any).code) === 1 && Array.isArray((res as any).data)) {
            return res
        }
    } catch {
        // 三方不可用按「无多语言」处理，调用方自行回退到中文
    }
    return { code: 1, data: [] as Record<string, string>[], message: '操作成功' }
}
