import type { CountryEntity, DistrictItem } from '~/types/address'
import { queryCountryAndCurrency, queryDistrictAll } from '~/services/base.service'

export type IpLocationInfo = {
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
}

export type VisitorCountry = {
    countryId: number
    countryName: string
    phoneCode: string
}

export type VisitorDeliveryLocation = VisitorCountry & {
    /** 仅中国有省市区；非中国为 null */
    areaIds: string[] | null
    /** 与 areaIds 对应的省/市/区名称，用于全站统一回显 */
    areaNames?: string[] | null
    /** 地区来源：手动选择、登录地址、IP 定位或系统兜底 */
    source?: 'manual' | 'address' | 'ip' | 'fallback'
}

/** 中国兜底：国家 + 北京市/北京市/东城区 */
export const DEFAULT_CN_COUNTRY: VisitorCountry = {
    countryId: 1,
    countryName: '中国',
    phoneCode: '+86'
}
export const DEFAULT_CN_AREA_IDS = ['6331', '1', '32'] as const

const CACHE_KEY = 'mall_pc_ip_delivery_v2'

export const VISITOR_DELIVERY_CHANGE_EVENT = 'mall:visitor-delivery-change'

/** ISO2 → 商城国家表中文名（表内无 ISO 字段） */
const ISO_TO_CN_NAME: Record<string, string> = {
    CN: '中国',
    US: '美国',
    GB: '英国',
    UK: '英国',
    AU: '澳大利亚',
    IN: '印度',
    DE: '德国',
    FR: '法国',
    JP: '日本',
    KR: '韩国',
    SG: '新加坡',
    MY: '马来西亚',
    TH: '泰国',
    RU: '俄罗斯',
    BR: '巴西',
    ZA: '南非'
}

const TRADITIONAL_ALIASES: Record<string, string> = {
    澳大利亞: '澳大利亚',
    美國: '美国',
    英國: '英国',
    德國: '德国',
    法國: '法国',
    韓國: '韩国',
    馬來西亞: '马来西亚',
    俄羅斯: '俄罗斯'
}

const normalizePlace = (name: string) =>
    String(name || '')
        .trim()
        .replace(/特别行政区$/, '')
        .replace(/(壮族|回族|维吾尔|藏族|蒙古族)?自治区$/, '')
        .replace(/省$/, '')
        .replace(/市$/, '')
        .replace(/地区$/, '')
        .replace(/盟$/, '')
        .replace(/自治州$/, '')

const normalizeCountryName = (name: string) => {
    const raw = String(name || '').trim()
    if (!raw) return ''
    return TRADITIONAL_ALIASES[raw] || raw
}

const scoreNameMatch = (districtName: string, target: string) => {
    const a = String(districtName || '')
    const b = String(target || '')
    if (!a || !b) return 0
    if (a === b) return 100
    const na = normalizePlace(a)
    const nb = normalizePlace(b)
    if (!na || !nb) return 0
    if (na === nb) return 90
    if (a.includes(b) || b.includes(a)) return 70
    if (na.includes(nb) || nb.includes(na)) return 60
    return 0
}

const pickBestDistrict = (list: DistrictItem[] | undefined, target: string): DistrictItem | null => {
    if (!Array.isArray(list) || !list.length || !target) return null
    let best: DistrictItem | null = null
    let bestScore = 0
    for (const item of list) {
        const score = scoreNameMatch(item.districtName, target)
        if (score > bestScore) {
            bestScore = score
            best = item
        }
    }
    return bestScore >= 60 ? best : null
}

/** 根据三级区划 ID 取得名称；ID 不完整或已失效时返回 null。 */
export const mapAreaIdsToNames = (
    areaIds: readonly unknown[] | null | undefined,
    provinces: DistrictItem[]
): string[] | null => {
    if (!Array.isArray(areaIds) || areaIds.length !== 3 || !Array.isArray(provinces)) return null
    const province = provinces.find(item => String(item.districtId) === String(areaIds[0]))
    const city = province?.childDistrict?.find(item => String(item.districtId) === String(areaIds[1]))
    const area = city?.childDistrict?.find(item => String(item.districtId) === String(areaIds[2]))
    if (!province || !city || !area) return null
    return [province.districtName, city.districtName, area.districtName]
}

/**
 * 将 IP 定位的省市名解析为商城区划三级 ID（省/市/区）
 */
export const mapIpLocationToAreaIds = (
    location: IpLocationInfo | null | undefined,
    provinces: DistrictItem[]
): string[] | null => {
    if (!location || !Array.isArray(provinces) || !provinces.length) return null
    const provinceName = String(location.province || '').trim()
    const cityName = String(location.city || location.province || '').trim()
    if (!provinceName && !cityName) return null

    const province =
        pickBestDistrict(provinces, provinceName) ||
        pickBestDistrict(provinces, cityName)
    if (!province) return null

    const cities = province.childDistrict || []
    const city =
        pickBestDistrict(cities, cityName) ||
        pickBestDistrict(cities, provinceName) ||
        cities[0]
    if (!city) return null

    const areas = city.childDistrict || []
    const area =
        pickBestDistrict(areas, String(location.region || '').trim()) ||
        areas[0]
    if (!area) return null

    return [String(province.districtId), String(city.districtId), String(area.districtId)]
}

const isChinaLocation = (location: IpLocationInfo) => {
    const code = String(location.countryCode || '').toUpperCase()
    if (code === 'CN') return true
    const name = normalizeCountryName(location.country || '')
    if (name === '中国' || name === '中华人民共和国') return true
    // 仅有省市区、无国家时，按中国大陆处理
    if (!code && !name && (location.province || location.city)) return true
    return false
}

/**
 * 将 IP 国家信息映射到商城国家（匹配不到返回 null，由调用方回落中国）
 */
export const mapIpLocationToCountry = (
    location: IpLocationInfo | null | undefined,
    countries: CountryEntity[]
): VisitorCountry | null => {
    if (!location || !Array.isArray(countries) || !countries.length) return null

    const code = String(location.countryCode || '').toUpperCase()
    const wantedNames = new Set<string>()
    const fromIso = ISO_TO_CN_NAME[code]
    if (fromIso) wantedNames.add(fromIso)
    const fromApi = normalizeCountryName(location.country || '')
    if (fromApi) wantedNames.add(fromApi)

    if (!wantedNames.size) return null

    for (const c of countries) {
        const name = String(c.countryName || '').trim()
        if (!name || !wantedNames.has(name)) continue
        const id = Number(c.id ?? c.countryId)
        const phone = String(c.internationalDialingCode || '').trim()
        if (!Number.isFinite(id) || id <= 0 || !phone) continue
        return { countryId: id, countryName: name, phoneCode: phone }
    }
    return null
}

type CachedDelivery = {
    countryId: number
    countryName: string
    phoneCode: string
    areaIds: string[] | null
    areaNames?: string[] | null
    source?: VisitorDeliveryLocation['source']
}

export const getCachedVisitorDelivery = (): VisitorDeliveryLocation | null => {
    if (!import.meta.client) return null
    try {
        const raw = sessionStorage.getItem(CACHE_KEY)
        if (!raw) return null
        const parsed = JSON.parse(raw) as CachedDelivery
        if (!parsed || !Number(parsed.countryId) || !parsed.countryName || !parsed.phoneCode) return null
        const areaIds =
            Array.isArray(parsed.areaIds) &&
            parsed.areaIds.length === 3 &&
            parsed.areaIds.every((x) => Number(x) > 0)
                ? parsed.areaIds.map(String)
                : null
        return {
            countryId: Number(parsed.countryId),
            countryName: String(parsed.countryName),
            phoneCode: String(parsed.phoneCode),
            areaIds,
            areaNames: Array.isArray(parsed.areaNames) && parsed.areaNames.length === 3
                ? parsed.areaNames.map(String)
                : null,
            source: parsed.source
        }
    } catch {
        return null
    }
}

export const setCachedVisitorDelivery = (loc: VisitorDeliveryLocation) => {
    if (!import.meta.client) return
    try {
        sessionStorage.setItem(CACHE_KEY, JSON.stringify(loc))
        window.dispatchEvent(new CustomEvent<VisitorDeliveryLocation>(VISITOR_DELIVERY_CHANGE_EVENT, {
            detail: loc
        }))
    } catch {
        // ignore
    }
}

/** @deprecated 保留旧缓存键兼容；新逻辑请用 getCachedVisitorDelivery */
export const getCachedIpAreaIds = (): string[] | null => {
    const cached = getCachedVisitorDelivery()
    return cached?.areaIds || null
}

export const setCachedIpAreaIds = (ids: string[]) => {
    setCachedVisitorDelivery({
        ...DEFAULT_CN_COUNTRY,
        areaIds: ids,
        source: 'manual'
    })
}

/**
 * 拉取 IP 定位并映射国家 +（中国）省市区；失败返回 null
 */
export const resolveVisitorDeliveryLocation = async (
    fetchIpLocation: () => Promise<IpLocationInfo | null>,
    options: { ignoreCache?: boolean } = {}
): Promise<VisitorDeliveryLocation | null> => {
    if (!options.ignoreCache) {
        const cached = getCachedVisitorDelivery()
        if (cached) return cached
    }

    const location = await fetchIpLocation()
    if (!location || location.internal) return null

    const countryRes = await queryCountryAndCurrency({})
    const countries = Array.isArray(countryRes?.data?.baseCountryEntities)
        ? (countryRes.data.baseCountryEntities as CountryEntity[])
        : []

    let country = mapIpLocationToCountry(location, countries)
    // 有中国省市信号但国家未匹配时，仍按中国处理
    if (!country && isChinaLocation(location)) {
        country = { ...DEFAULT_CN_COUNTRY }
    }
    if (!country) return null

    let areaIds: string[] | null = null
    let areaNames: string[] | null = null
    if (country.phoneCode === '+86' || country.countryId === 1) {
        if (location.province || location.city) {
            const districtRes = await queryDistrictAll({})
            const provinces = Array.isArray(districtRes?.data)
                ? (districtRes.data as DistrictItem[])
                : []
            areaIds = mapIpLocationToAreaIds(location, provinces)
            areaNames = mapAreaIdsToNames(areaIds, provinces)
        }
        // 中国但省市映射失败：交给调用方用北京兜底，这里仍返回国家
        if (!areaIds) {
            areaIds = null
        }
    }

    const result: VisitorDeliveryLocation = {
        ...country,
        areaIds,
        areaNames,
        source: 'ip'
    }
    setCachedVisitorDelivery(result)
    return result
}

/**
 * 仅解析省市区（兼容旧调用）；失败返回 null
 */
export const resolveVisitorAreaIds = async (
    fetchIpLocation: () => Promise<IpLocationInfo | null>
): Promise<string[] | null> => {
    const loc = await resolveVisitorDeliveryLocation(fetchIpLocation)
    return loc?.areaIds || null
}
