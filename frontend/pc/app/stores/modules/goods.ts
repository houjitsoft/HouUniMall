import { defineStore } from 'pinia'
import type { GoodsList, ShopList } from '~/types/goods'
import type { GoodsState } from '~/types/store'
import { queryShoppingCartCount } from '~/services/order.service'

// 重新导出类型，保持向后兼容
export type { GoodsList, ShopList }

export const useGoodsStore = defineStore('goods', {
    // 状态
    state: (): GoodsState => ({
        shopList: [],
        cartCount: 0
    }),

    // 计算属性
    getters: {
        // 获取店铺列表
        getShopList: (state) => state.shopList,
        // 获取购物车角标数量
        getCartCount: (state) => state.cartCount,
        // 获取商品总数
        getTotalGoodsCount: (state) => {
            return state.shopList.reduce((total, shop) => {
                return total + shop.goodsList.reduce((sum, goods) => sum + goods.goodsNumber, 0)
            }, 0)
        },
        // 获取总价格
        getTotalPrice: (state) => {
            return state.shopList.reduce((total, shop) => total + shop.shopTotalPrice, 0)
        }
    },

    // 操作方法
    actions: {
        /**
         * 拉取购物车数量并更新 cartCount（仅已登录时请求接口，未登录时置 0）
         * 添加/删除购物车后应调用此方法以刷新头部角标
         */
        async fetchCartCount() {
            const userStore = useUserStore()
            if (!userStore.isLogin) {
                this.cartCount = 0
                return
            }
            try {
                const res = await queryShoppingCartCount({})
                if (res?.code === 1 && typeof res?.data === 'number' && res.data > 0) {
                    this.cartCount = res.data
                } else {
                    this.cartCount = 0
                }
            } catch (_) {
                this.cartCount = 0
            }
        },

        // 设置店铺列表（用于立即购买）
        setShopList(shopList: ShopList[]) {
            this.shopList = shopList
        },

        // 添加商品到店铺列表
        addGoodsToShop(shopId: number, shopName: string, goods: GoodsList) {
            const shopIndex = this.shopList.findIndex(shop => shop.shopId === shopId)

            if (shopIndex === -1) {
                // 店铺不存在，创建新店铺
                const newShop: ShopList = {
                    shopId,
                    shopName,
                    shopInvoices: 0,
                    invoiceActive: 0,
                    goodsList: [goods],
                    shopTotalPrice: goods.goodsTotal
                }
                this.shopList.push(newShop)
            } else {
                const shop = this.shopList[shopIndex]
                if (!shop) return

                // 店铺已存在，检查商品是否已存在
                const goodsIndex = shop.goodsList.findIndex(
                    item => item.goodsId === goods.goodsId && item.goodsSpecifications === goods.goodsSpecifications
                )

                if (goodsIndex === -1) {
                    // 商品不存在，添加新商品
                    shop.goodsList.push(goods)
                } else {
                    // 商品已存在，更新数量和总价
                    const existingGoods = shop.goodsList[goodsIndex]
                    if (existingGoods) {
                        existingGoods.goodsNumber += goods.goodsNumber
                        existingGoods.goodsTotal = existingGoods.singlePrice * existingGoods.goodsNumber
                    }
                }

                // 更新店铺总价
                shop.shopTotalPrice = shop.goodsList.reduce(
                    (sum, item) => sum + item.goodsTotal, 0
                )
            }
        },

        // 清空店铺列表
        clearShopList() {
            this.shopList = []
        }
    },

    persist: {
        key: 'goods-state',
        storage: typeof window !== 'undefined' ? window.sessionStorage : undefined,
        paths: ['shopList']
    } as any
})
