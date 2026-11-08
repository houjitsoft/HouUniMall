import type { ShopModal } from "./shop.d"
import request from '@/utils/request'

// 店铺设置--获取店铺数据
export function getShopConfig() {
    return request.get({ url: '/mall.shop/detail' })
}

// 店铺设置--设置店铺数据
export function setShopConfig(params: ShopModal) {
    return request.post({ url: '/mall.shop/edit', params })
}