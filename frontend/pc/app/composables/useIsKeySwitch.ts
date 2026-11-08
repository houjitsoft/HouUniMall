/**
 * 平台应用开关查询 Composable
 * 根据配置键查询平台是否开启应用
 *
 * 说明：开关接口本身很快，但首页并发打满浏览器连接池时，ofetch 短超时会先被 Abort。
 * 这里用原生 fetch + high priority，并做 sessionStorage / 失败短缓存，避免 hydrate/重挂载反复报错。
 */

import { ref } from 'vue'

const SWITCH_TIMEOUT_MS = 20_000
const SWITCH_CACHE_TTL_MS = 5 * 60_000
const SWITCH_FAIL_CACHE_TTL_MS = 15_000
const STORAGE_PREFIX = 'font_b2c_key_switch_'

type CacheEntry = { at: number; value: string | null; failed?: boolean }

const memoryCache = new Map<string, CacheEntry>()
const inflight = new Map<string, Promise<string | null>>()
const warnedAt = new Map<string, number>()

const readStorage = (configKey: string): CacheEntry | null => {
    if (typeof sessionStorage === 'undefined') return null
    try {
        const raw = sessionStorage.getItem(STORAGE_PREFIX + configKey)
        if (!raw) return null
        const parsed = JSON.parse(raw) as CacheEntry
        if (!parsed || typeof parsed.at !== 'number') return null
        return parsed
    } catch {
        return null
    }
}

const writeStorage = (configKey: string, entry: CacheEntry) => {
    if (typeof sessionStorage === 'undefined') return
    try {
        sessionStorage.setItem(STORAGE_PREFIX + configKey, JSON.stringify(entry))
    } catch {
        // quota / private mode：忽略
    }
}

const getFreshCache = (configKey: string): string | null | undefined => {
    const now = Date.now()
    const mem = memoryCache.get(configKey)
    if (mem) {
        const ttl = mem.failed ? SWITCH_FAIL_CACHE_TTL_MS : SWITCH_CACHE_TTL_MS
        if (now - mem.at < ttl) return mem.value
    }
    const stored = readStorage(configKey)
    if (stored && !stored.failed && now - stored.at < SWITCH_CACHE_TTL_MS) {
        memoryCache.set(configKey, stored)
        return stored.value
    }
    return undefined
}

const remember = (configKey: string, value: string | null, failed = false) => {
    const entry: CacheEntry = { at: Date.now(), value, failed }
    memoryCache.set(configKey, entry)
    if (!failed) writeStorage(configKey, entry)
}

const logSwitchError = (configKey: string, err: unknown) => {
    // hydration/重挂载时并发请求被 abort 很常见，不必刷屏
    const name = (err as any)?.name || ''
    const msg = String((err as any)?.message || err || '')
    if (name === 'AbortError' || /aborted|AbortError/i.test(msg)) return
    const now = Date.now()
    const last = warnedAt.get(configKey) || 0
    if (now - last < 60_000) return
    warnedAt.set(configKey, now)
    console.warn(`查询平台应用开关失败(${configKey}):`, err)
}

const fetchSwitchValue = async (configKey: string): Promise<string | null> => {
    if (typeof fetch === 'undefined') return null
    const ctrl = new AbortController()
    const timer = setTimeout(() => ctrl.abort(), SWITCH_TIMEOUT_MS)
    try {
        const url = `/web/base/sys-config/switch/query-language?configKey=${encodeURIComponent(configKey)}`
        const res = await fetch(url, {
            method: 'GET',
            signal: ctrl.signal,
            credentials: 'same-origin',
            // 浏览器支持时提高优先级，减少被首页大请求挤出连接池
            priority: 'high'
        } as RequestInit & { priority?: string })
        if (!res.ok) return null
        const json = await res.json()
        return (json?.data?.configValue as string | undefined) || null
    } finally {
        clearTimeout(timer)
    }
}

/**
 * 应用启动时预取常用开关，抢在首页其它请求之前占连接。
 */
export const prefetchPlatformSwitches = (keys: string[] = ['language_switch', 'aiComparison_switch']) => {
    return Promise.all(keys.map((key) => resolveSwitchValue(key)))
}

const resolveSwitchValue = async (configKey: string): Promise<string | null> => {
    const cached = getFreshCache(configKey)
    if (cached !== undefined) return cached

    const pending = inflight.get(configKey)
    if (pending) return pending

    const request = (async () => {
        try {
            const value = await fetchSwitchValue(configKey)
            remember(configKey, value, false)
            return value
        } catch (err) {
            remember(configKey, null, true)
            logSwitchError(configKey, err)
            return null
        } finally {
            inflight.delete(configKey)
        }
    })()

    inflight.set(configKey, request)
    return request
}

/**
 * 查询平台应用开关
 * 根据配置键查询平台是否开启应用，返回配置值
 */
export const useIsKeySwitch = () => {
    const loading = ref(false)
    const error = ref<string | null>(null)

    const checkKeySwitch = async (configKey: string): Promise<string | null> => {
        loading.value = true
        error.value = null
        try {
            return await resolveSwitchValue(configKey)
        } catch (err: any) {
            error.value = err?.message || '查询平台应用开关失败'
            return null
        } finally {
            loading.value = false
        }
    }

    const isKeyEnabled = async (configKey: string): Promise<boolean> => {
        const value = await checkKeySwitch(configKey)
        return value === '1' || value === 'true' || value === 'on' || value === 'yes'
    }

    return {
        loading,
        error,
        checkKeySwitch,
        isKeyEnabled
    }
}
