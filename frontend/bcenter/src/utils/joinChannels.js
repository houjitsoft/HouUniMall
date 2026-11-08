/**
 * 入驻流程专用请求通道（2026-09-24 由 tcenter 迁入）
 *
 * 与 tcenter 完全一致：统一使用服务对应的 BFF 新路由，
 * 保证后端接口路径与响应结构不变（返回完整响应体 { code, data, message }）。
 */
import { requestBody } from '@/utils/joinRequest'

export const joinBase = requestBody('mall-base/')
export const joinBusiness = requestBody('mall-business/')
export const joinUserms = requestBody('mall-userms/')
export const joinGoods = requestBody('mall-goods/')
export const joinThird = requestBody('mall-third-party/')
export const joinSupplier = requestBody('mall-supplier/')
