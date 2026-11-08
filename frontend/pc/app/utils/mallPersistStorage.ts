/**
 * mall store 持久化：禁止插件在 store 创建时自动 hydrate（getItem 恒为 null），
 * 由 hydration-ready.client 在 app:mounted 之后手动读取并写入，避免 Hydration mismatch。
 */
export const mallPersistStorage = {
    getItem(_key: string): string | null {
        return null
    },
    setItem(key: string, value: string): void {
        if (!import.meta.client) return
        try {
            localStorage.setItem(key, value)
        } catch {
            // ignore quota / private mode
        }
    }
}

export function readMallPersistState(): Record<string, unknown> | null {
    if (!import.meta.client) return null
    try {
        const raw = localStorage.getItem('mall-state')
        if (!raw) return null
        const parsed = JSON.parse(raw)
        return parsed && typeof parsed === 'object' ? parsed : null
    } catch {
        return null
    }
}
