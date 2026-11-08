import { defineStore } from 'pinia'
import type { GoodsSkuItem } from '~/types/goods'
import type { ComparisonItem, ComparisonState } from '~/types/store'
import { addGoodsContrast, deleteGoodsContrast } from '~/services/goods.service'
import { useBrowserId } from '~/composables/useBrowserId'
import { Toast } from '~/composables/useToast'

// 重新导出类型，保持向后兼容
export type { ComparisonItem }

export const useComparisonStore = defineStore('comparison', {
    state: (): ComparisonState => ({
        items: [],
        visible: true,
        currentCommonId: null,
        loading: false
    }),

    getters: {
        // 获取对比商品列表
        getItems: (state) => state.items,
        // 获取对比商品数量
        getCount: (state) => state.items.length,
        // 是否已添加某个商品
        isAdded: (state) => (goodsId: number) => {
            return state.items.some(item => item.goodsId === goodsId)
        },
        // 是否显示对比栏
        isVisible: (state) => state.visible && state.items.length > 0
    },

    actions: {
        // 添加商品到对比列表（调用接口）
        async addItemWithApi(product: {
            goodsId: number
            goodsName?: string
            goodsImage?: string
            goodsCode?: string
            commonPrice?: number
            goodsNumber?: number
        }): Promise<boolean> {
            // 防止重复调用
            if (this.loading) {
                return false
            }

            // 检查是否已存在
            if (this.items.some(i => i.goodsId === product.goodsId)) {
                Toast.warning('该商品已在对比列表中')
                return false
            }

            // 限制最多对比数量
            if (this.items.length >= 4) {
                Toast.warning('对比列表最多只能添加4个商品')
                return false
            }

            this.loading = true
            try {
                // 调用接口添加对比
                const response = await addGoodsContrast({
                    goodsId: String(product.goodsId),
                    goodsNum: product.goodsNumber || 1,
                    browserId: useBrowserId()
                })

                if (response?.code === 1) {
                    // 构建对比商品数据
                    const comparisonItem: ComparisonItem = {
                        goodsId: product.goodsId,
                        goodsName: product.goodsName || '',
                        goodsImage: product.goodsImage,
                        goodsCode: product.goodsCode,
                        commonPrice: product.commonPrice,
                    }

                    // 添加到本地列表
                    this.items.push(comparisonItem)
                    this.visible = true
                    Toast.success('已添加到对比列表')
                    return true
                } else {
                    Toast.error(response?.message || '添加对比失败，请稍后重试')
                    return false
                }
            } catch (error: any) {
                console.error('添加对比失败:', error)
                Toast.error(error?.message || '添加对比失败，请稍后重试')
                return false
            } finally {
                this.loading = false
            }
        },

        // 从对比列表移除商品（调用接口）
        async removeItemWithApi(goodsId: number): Promise<boolean> {
            // 防止重复调用
            if (this.loading) {
                return false
            }

            this.loading = true
            try {
                const response = await deleteGoodsContrast({
                    goodsIds: [goodsId],
                    browserId: useBrowserId()
                })

                if (response?.code === 1) {
                    // 接口成功后再从本地移除
                    const index = this.items.findIndex(item => item.goodsId === goodsId)
                    if (index > -1) {
                        this.items.splice(index, 1)
                    }
                    return true
                } else {
                    Toast.error(response?.message || '删除失败，请稍后重试')
                    return false
                }
            } catch (error: any) {
                console.error('删除对比商品失败:', error)
                Toast.error(error?.message || '删除失败，请稍后重试')
                return false
            } finally {
                this.loading = false
            }
        },

        // 清空对比列表（调用接口）
        async clearItemsWithApi(): Promise<boolean> {
            // 防止重复调用
            if (this.loading) {
                return false
            }

            if (this.items.length === 0) {
                return true
            }

            this.loading = true
            try {
                const goodsIds = this.items.map(item => item.goodsId)
                const response = await deleteGoodsContrast({
                    goodsIds,
                    browserId: useBrowserId()
                })

                if (response?.code === 1) {
                    // 接口成功后再清空本地列表
                    this.items = []
                    return true
                } else {
                    Toast.error(response?.message || '清空失败，请稍后重试')
                    return false
                }
            } catch (error: any) {
                console.error('清空对比列表失败:', error)
                Toast.error(error?.message || '清空失败，请稍后重试')
                return false
            } finally {
                this.loading = false
            }
        },

        // 设置当前 commonId，如果不同则清空对比列表（调用接口）
        async setCurrentCommonIdWithApi(commonId: number | null): Promise<void> {
            if (this.currentCommonId !== null && this.currentCommonId !== commonId && commonId !== null) {
                // commonId 变化，需要删除所有对比商品
                if (this.items.length > 0) {
                    // 调用清空接口
                    await this.clearItemsWithApi()
                }
            }
            this.currentCommonId = commonId
        },

        // 添加商品到对比列表（仅本地操作，不调用接口）
        addItem(item: ComparisonItem) {
            // 检查是否已存在
            const exists = this.items.some(i => i.goodsId === item.goodsId)
            if (exists) {
                return false // 已存在，返回 false
            }

            // 限制最多对比数量（最多4个）
            if (this.items.length >= 4) {
                return false // 已达到最大数量
            }

            this.items.push(item)
            this.visible = true
            return true
        },

        // 从对比列表移除商品（仅本地操作，不调用接口）
        removeItem(goodsId: number) {
            const index = this.items.findIndex(item => item.goodsId === goodsId)
            if (index > -1) {
                this.items.splice(index, 1)
            }
        },

        // 清空对比列表（仅本地操作，不调用接口）
        clearItems() {
            this.items = []
        },

        // 切换显示/隐藏
        toggleVisible() {
            this.visible = !this.visible
        },

        // 设置显示状态
        setVisible(visible: boolean) {
            this.visible = visible
        },

        // 设置当前 commonId，如果不同则清空对比列表（仅本地操作，不调用接口）
        setCurrentCommonId(commonId: number | null) {
            if (this.currentCommonId !== null && this.currentCommonId !== commonId) {
                // commonId 变化，清空对比列表
                this.items = []
            }
            this.currentCommonId = commonId
        }
    },

    persist: {
        key: 'comparison-state',
        storage: typeof window !== 'undefined' ? window.sessionStorage : undefined,
        paths: ['items', 'visible', 'currentCommonId']
        // loading 状态不持久化
    } as any
})
