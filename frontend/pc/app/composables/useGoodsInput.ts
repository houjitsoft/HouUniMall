import { ref } from 'vue'
import {
    queryBusinessGoodsCodeBySub as queryBusinessGoodsCodeBySubService,
    queryGoodsByBusinessGoodsCode as queryGoodsByBusinessGoodsCodeService,
    queryGoodsCodeBySub as queryGoodsCodeBySubService,
    queryGoodsByGoodsCode as queryGoodsByGoodsCodeService
} from '~/services/goods.service'
import type { DropdownState, DebounceTimer } from '~/types/composable'

/**
 * 商品输入下拉框 Composable
 * 用于管理商品编码输入下拉框的状态和逻辑
 */
export const useGoodsInput = () => {
    const dropdowns = ref<Record<number, DropdownState>>({})
    const debounceTimers = ref<Record<string, DebounceTimer>>({})

    /**
     * 查询业务商品编码列表接口
     */
    const queryBusinessGoodsCodeBySub = async (businessGoodsCode: string): Promise<string[]> => {
        try {
            return await queryBusinessGoodsCodeBySubService(businessGoodsCode)
        } catch (error) {
            console.error('查询业务商品编码失败:', error)
            return []
        }
    }

    /**
     * 根据业务商品编码查询商品详情接口
     */
    const queryGoodsByBusinessGoodsCode = async (businessGoodsCode: string) => {
        try {
            return await queryGoodsByBusinessGoodsCodeService(businessGoodsCode)
        } catch (error) {
            console.error('查询商品详情失败:', error)
            return null
        }
    }

    /**
     * 查询商品编码列表接口
     */
    const queryGoodsCodeBySub = async (goodsCode: string): Promise<string[]> => {
        try {
            return await queryGoodsCodeBySubService(goodsCode)
        } catch (error) {
            console.error('查询商品编码失败:', error)
            return []
        }
    }

    /**
     * 根据商品编码查询商品详情接口
     */
    const queryGoodsByGoodsCode = async (goodsCode: string) => {
        try {
            return await queryGoodsByGoodsCodeService(goodsCode)
        } catch (error) {
            console.error('查询商品详情失败:', error)
            return null
        }
    }

    /**
     * 初始化下拉框状态
     */
    const initDropdown = (index: number) => {
        if (!dropdowns.value[index]) {
            dropdowns.value[index] = { visible: false, options: [], loading: false }
        }
    }

    /**
     * 防抖查询
     */
    const debounceQuery = (
        timerKey: string,
        queryFn: () => Promise<string[]>,
        index: number,
        delay: number = 500
    ) => {
        // 清除之前的定时器
        if (debounceTimers.value[timerKey]) {
            clearTimeout(debounceTimers.value[timerKey])
        }

        // 防抖处理
        debounceTimers.value[timerKey] = setTimeout(async () => {
            initDropdown(index)
            const dropdown = dropdowns.value[index]
            if (dropdown) {
                dropdown.loading = true
                const options = await queryFn()
                dropdown.options = options
                dropdown.visible = options.length > 0
                dropdown.loading = false
            }
        }, delay)
    }

    /**
     * 清除定时器
     */
    const clearTimers = () => {
        Object.values(debounceTimers.value).forEach(timer => {
            if (timer) {
                clearTimeout(timer)
            }
        })
        debounceTimers.value = {}
    }

    /**
     * 隐藏所有下拉框
     */
    const hideAllDropdowns = () => {
        Object.keys(dropdowns.value).forEach(key => {
            const dropdown = dropdowns.value[Number(key)]
            if (dropdown) {
                dropdown.visible = false
            }
        })
    }

    return {
        dropdowns,
        debounceTimers,
        queryBusinessGoodsCodeBySub,
        queryGoodsByBusinessGoodsCode,
        queryGoodsCodeBySub,
        queryGoodsByGoodsCode,
        initDropdown,
        debounceQuery,
        clearTimers,
        hideAllDropdowns
    }
}
