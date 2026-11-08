/**
 * 地区代码选择 composable
 * 
 * 提供统一的地区代码选择功能
 * 
 * @example
 * const { showAreaCode, selectedCountry, handleSelect, handleClose } = useAreaCode()
 * // 在模板中使用
 * <AreaCode v-if="showAreaCode" @select="handleSelect" @close="handleClose" />
 */

import { ref, reactive } from 'vue'
import type { CountryInfo } from '~/types/form'
import type { UseAreaCodeReturn } from '~/types/composable'

// 重新导出类型，保持向后兼容
export type { UseAreaCodeReturn }

/**
 * 默认国家信息（中国）
 */
const DEFAULT_COUNTRY: CountryInfo = {
    code: '+86',
    name: '中国',
    abbreviation: 'CN',
    countryId: 1
}

/**
 * 地区代码选择 composable
 * 
 * @param defaultCountry 默认国家信息，默认为中国
 * @returns 地区代码选择相关的方法和状态
 */
export const useAreaCode = (defaultCountry: CountryInfo = DEFAULT_COUNTRY): UseAreaCodeReturn => {
    const showAreaCode = ref(false)
    const selectedCountry = reactive<CountryInfo>({ ...defaultCountry })

    /**
     * 处理地区代码选择
     */
    const handleSelect = (country: CountryInfo) => {
        Object.assign(selectedCountry, country)
        showAreaCode.value = false
    }

    /**
     * 关闭地区代码选择器
     */
    const handleClose = () => {
        showAreaCode.value = false
    }

    /**
     * 打开地区代码选择器
     */
    const handleOpen = () => {
        showAreaCode.value = true
    }

    return {
        showAreaCode,
        selectedCountry,
        handleSelect,
        handleClose,
        handleOpen
    }
}
