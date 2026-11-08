/**
 * 校验客户端跳转目标，缓解开放重定向：仅允许 http(s)，禁止 javascript:/data: 及协议相对 URL。
 */
export function getSafeRedirectUrl(raw: unknown): string | null {
    if (raw == null || typeof raw !== 'string') return null
    const s = raw.trim()
    if (!s || s.startsWith('//')) return null
    const lower = s.toLowerCase()
    if (
        lower.startsWith('javascript:') ||
        lower.startsWith('vbscript:') ||
        lower.startsWith('data:')
    ) {
        return null
    }
    try {
        const base =
            typeof window !== 'undefined' ? window.location.origin : 'https://invalid.local'
        const u = new URL(s, base)
        if (u.protocol !== 'http:' && u.protocol !== 'https:') return null
        return u.href
    } catch {
        return null
    }
}

const PATH_SCHEME_RE = /^[a-zA-Z][a-zA-Z0-9+.-]*:/

function pathSegmentIsDotOrDotDot(seg: string): boolean {
    let decoded = seg
    try {
        decoded = decodeURIComponent(seg)
    } catch {
        /* 保持原串 */
    }
    return decoded === '.' || decoded === '..'
}

/**
 * 商品文件预览/下载地址：缓解路径遍历与危险 scheme。
 * - 绝对地址：仅允许通过 {@link getSafeRedirectUrl} 的 http(s) URL。
 * - 相对地址：必须以 `/` 开头（非 `//`），路径段经解码后不得为 `.` 或 `..`。
 */
export function sanitizeProductFileUrl(raw: unknown): string | null {
    if (raw == null || typeof raw !== 'string') return null
    const s = raw.trim()
    if (!s || s.startsWith('//')) return null
    const lower = s.toLowerCase()
    if (
        lower.startsWith('javascript:') ||
        lower.startsWith('vbscript:') ||
        lower.startsWith('data:')
    ) {
        return null
    }

    if (PATH_SCHEME_RE.test(s)) {
        return getSafeRedirectUrl(s)
    }

    if (!s.startsWith('/')) return null

    const pathOnly = (s.split('#')[0] ?? '').split('?')[0]?.replace(/\\/g, '/') ?? ''
    if (!pathOnly.startsWith('/')) return null
    const segments = pathOnly.split('/').filter(Boolean)
    if (segments.some(pathSegmentIsDotOrDotDot)) return null

    // 不依赖 window，避免 SSR 下用占位 origin 拼出错误绝对地址
    return s
}
