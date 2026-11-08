/**
 * 入驻流程专用登录态写入（2026-09-24 由 tcenter 迁入）
 *
 * bcenter 原有 auth.ts 只暴露 getToken / clearAuthInfo；入驻注册成功需要写入 token，
 * 这里补齐写入能力，存储键与 bcenter 保持一致。
 */
import { TOKEN_KEY } from '@/enums/cacheEnums'
import cache from '@/utils/cache'

export function setToken(token) {
    cache.set(TOKEN_KEY, token)
}

export function removeToken() {
    cache.remove(TOKEN_KEY)
}
