import { computed } from 'vue'
import { getVisitorIpLocation, queryDistrictAll } from '~/services/base.service'
import { getAddresses, getDefaultAddresses } from '~/services/userms.service'
import type { DistrictItem } from '~/types/address'
import {
    DEFAULT_CN_AREA_IDS,
    DEFAULT_CN_COUNTRY,
    getCachedVisitorDelivery,
    mapAreaIdsToNames,
    resolveVisitorDeliveryLocation,
    setCachedVisitorDelivery,
    type VisitorDeliveryLocation
} from '~/utils/visitorArea'

let initializationPromise: Promise<VisitorDeliveryLocation> | null = null

const validAreaIds = (value: unknown): value is string[] => (
    Array.isArray(value)
    && value.length === 3
    && value.every(item => Number(item) > 0)
)

const addressToLocation = (address: Record<string, any> | null | undefined): VisitorDeliveryLocation | null => {
    if (!address || typeof address !== 'object') return null
    const countryId = Number(address.countryId || DEFAULT_CN_COUNTRY.countryId)
    const countryName = String(address.countryName || DEFAULT_CN_COUNTRY.countryName)
    const phoneCode = String(address.internationalDialingCode || DEFAULT_CN_COUNTRY.phoneCode)
    const rawAreaIds = [
        address.userAddressProvinceId ?? address.provinceId,
        address.userAddressCityId ?? address.cityId,
        address.userAddressAreaId ?? address.areaId
    ].map(String)
    const china = phoneCode === '+86' || countryId === DEFAULT_CN_COUNTRY.countryId

    if (china && !validAreaIds(rawAreaIds)) return null
    return {
        countryId,
        countryName,
        phoneCode,
        areaIds: china ? rawAreaIds : null,
        areaNames: null,
        source: 'address'
    }
}

/**
 * 商城浏览区域的唯一状态源。
 * 顶部区域入口与商品详情配送区域都通过这里读写，避免各自定位、各自兜底。
 */
export const useVisitorRegion = () => {
    const location = useState<VisitorDeliveryLocation | null>('visitor-region-location', () => null)
    const districtTree = useState<DistrictItem[]>('visitor-region-district-tree', () => [])
    const loading = useState<boolean>('visitor-region-loading', () => false)
    const initialized = useState<boolean>('visitor-region-initialized', () => false)

    const loadDistrictTree = async (): Promise<DistrictItem[]> => {
        if (districtTree.value.length) return districtTree.value
        try {
            const response = await queryDistrictAll({})
            districtTree.value = Array.isArray(response?.data) ? response.data as DistrictItem[] : []
        } catch (error) {
            console.warn('加载商城区域数据失败:', error)
            districtTree.value = []
        }
        return districtTree.value
    }

    const normalizeLocation = async (value: VisitorDeliveryLocation): Promise<VisitorDeliveryLocation> => {
        const china = value.phoneCode === '+86' || value.countryId === DEFAULT_CN_COUNTRY.countryId
        const areaIds = china && validAreaIds(value.areaIds)
            ? value.areaIds.map(String)
            : (china ? [...DEFAULT_CN_AREA_IDS] : null)
        let areaNames = Array.isArray(value.areaNames) && value.areaNames.length === 3
            ? value.areaNames.map(String)
            : null
        if (areaIds && !areaNames) {
            areaNames = mapAreaIdsToNames(areaIds, await loadDistrictTree())
        }
        return {
            countryId: Number(value.countryId || DEFAULT_CN_COUNTRY.countryId),
            countryName: String(value.countryName || DEFAULT_CN_COUNTRY.countryName),
            phoneCode: String(value.phoneCode || DEFAULT_CN_COUNTRY.phoneCode),
            areaIds,
            areaNames,
            source: value.source || 'fallback'
        }
    }

    const setVisitorRegion = async (value: VisitorDeliveryLocation): Promise<VisitorDeliveryLocation> => {
        const normalized = await normalizeLocation(value)
        location.value = normalized
        initialized.value = true
        setCachedVisitorDelivery(normalized)
        return normalized
    }

    const resolveAddressLocation = async (): Promise<VisitorDeliveryLocation | null> => {
        try {
            const response = await getDefaultAddresses({})
            const fromDefault = addressToLocation(response?.data as any)
            if (fromDefault) return fromDefault
        } catch {
            // 默认地址为空或接口暂时不可用时继续尝试地址列表。
        }
        try {
            const response = await getAddresses({ platForm: 1 })
            const list = Array.isArray(response?.data) ? response.data : []
            const preferred = list.find(item => Number(item?.userAddressDefault) === 1) || list[0]
            return addressToLocation(preferred as any)
        } catch {
            return null
        }
    }

    const locateByIp = async (force = false): Promise<VisitorDeliveryLocation> => {
        loading.value = true
        try {
            const resolved = await resolveVisitorDeliveryLocation(async () => {
                const response = await getVisitorIpLocation()
                return response?.code === 1 && response.data ? response.data : null
            }, { ignoreCache: force })
            if (resolved) return await setVisitorRegion({ ...resolved, source: 'ip' })
            return await setVisitorRegion({
                ...DEFAULT_CN_COUNTRY,
                areaIds: [...DEFAULT_CN_AREA_IDS],
                areaNames: null,
                source: 'fallback'
            })
        } catch (error) {
            console.warn('访客区域定位失败，使用默认区域:', error)
            return await setVisitorRegion({
                ...DEFAULT_CN_COUNTRY,
                areaIds: [...DEFAULT_CN_AREA_IDS],
                areaNames: null,
                source: 'fallback'
            })
        } finally {
            loading.value = false
        }
    }

    const initializeVisitorRegion = async (loggedIn = false): Promise<VisitorDeliveryLocation> => {
        if (location.value && initialized.value) return location.value
        if (initializationPromise) return initializationPromise

        initializationPromise = (async () => {
            loading.value = true
            try {
                const cached = getCachedVisitorDelivery()
                // 手动选区代表用户明确意图，优先级高于默认收货地址。
                if (cached?.source === 'manual') return await setVisitorRegion(cached)

                if (loggedIn) {
                    const fromAddress = await resolveAddressLocation()
                    if (fromAddress) return await setVisitorRegion(fromAddress)
                }

                if (cached) return await setVisitorRegion(cached)
                return await locateByIp(false)
            } finally {
                loading.value = false
                initializationPromise = null
            }
        })()
        return initializationPromise
    }

    const resolveAreaNames = async (areaIds: readonly unknown[]): Promise<string[] | null> => (
        mapAreaIdsToNames(areaIds, await loadDistrictTree())
    )

    const displayLabel = computed(() => {
        const current = location.value
        if (!current) return '选择区域'
        if (current.phoneCode !== '+86' && current.countryName) return current.countryName
        const names = (current.areaNames || []).filter(Boolean)
        if (!names.length) return current.countryName || '选择区域'
        // 直辖市的省、市名称相同，顶部只显示一次。
        return names.filter((name, index) => index === 0 || name !== names[index - 1]).join('')
    })

    return {
        location,
        districtTree,
        loading,
        initialized,
        displayLabel,
        loadDistrictTree,
        resolveAreaNames,
        setVisitorRegion,
        locateByIp,
        initializeVisitorRegion
    }
}
