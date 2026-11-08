/**
 * Pinia 状态管理入口文件
 * 
 * 统一导出所有状态管理模块，并提供配置说明
 */

// ==================== 类型扩展 ====================

import 'pinia'
import type { PersistenceOptions } from 'pinia-plugin-persistedstate'

declare module 'pinia' {
  export interface DefineStoreOptionsBase<S, Store> {
    persist?: boolean | PersistenceOptions<S> | Array<PersistenceOptions<S>>
  }
}

// ==================== 导出 Store ====================

// 导出用户模块
export { useUserStore } from './modules/user'

// 导出商城模块
export { useMallStore } from './modules/mall'

// 导出商品模块
export { useGoodsStore } from './modules/goods'
export type { GoodsList, ShopList } from './modules/goods'

// 导出对比模块
export { useComparisonStore } from './modules/comparison'
export type { ComparisonItem } from './modules/comparison'

// 导出文章模块
export { useArticleStore } from './modules/article'

// 导出 AI 助手模块
export { useAiStore } from './modules/ai'

// 导出 PLUS 会员模块
export { usePlusStore } from './modules/plus'
// 导出支付模块
export { usePaymentStore } from './modules/payment'

// 导出所有 Store 的类型
export type StoreType =
    ReturnType<typeof useUserStore> &
    ReturnType<typeof useMallStore> &
    ReturnType<typeof useGoodsStore> &
    ReturnType<typeof useComparisonStore> &
    ReturnType<typeof useArticleStore> &
    ReturnType<typeof useAiStore> &
    ReturnType<typeof import('./modules/plus').usePlusStore> &
    ReturnType<typeof import('./modules/payment').usePaymentStore>

/**
 * Pinia 状态管理使用说明
 * 
 * - 模块化设计：user.ts（用户信息）、mall.ts（商城配置）、goods.ts（购物车）
 * - 持久化：使用 pinia-plugin-persistedstate，用户和商城用 localStorage，购物车用 sessionStorage
 * - 类型安全：完整的 TypeScript 类型支持（类型扩展已集成在本文件中）
 * - 插件注册：app/plugins/pinia.client.ts（Nuxt 自动加载）
 * 
 * 使用示例：
 * ```ts
 * const userStore = useUserStore()
 * // 密码登录
 * const result = await userStore.loginByPassword('13800138000', 'password', 1)
 * // 验证码登录
 * const result = await userStore.loginByCode('13800138000', '123456', 1, '+86')
 * // 退出登录
 * await userStore.logout()
 * ```
 */
