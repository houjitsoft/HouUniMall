export type ConsumerAuthKind = 'login' | 'register' | 'forgot-password'

const AUTH_PATHS: Record<ConsumerAuthKind, string> = {
    login: '/login',
    register: '/register',
    'forgot-password': '/forgot-password'
}

const LEGACY_AUTH_PATHS = new Set([
    '/login',
    '/login/register',
    '/login/forgetPassword'
])

const UCENTER_AUTH_HASHES = new Set([
    '/login',
    '/register',
    '/forgot-password',
    '/redirect'
])

const safeReturnTarget = (raw?: string): string => {
    if (typeof raw !== 'string' || !raw.trim()) return ''
    let target = raw.trim()
    for (let index = 0; index < 2; index += 1) {
        try {
            const decoded = decodeURIComponent(target)
            if (decoded === target) break
            target = decoded
        } catch {
            return ''
        }
    }

    if (
        !target.startsWith('/') ||
        target.startsWith('//') ||
        /^\/[\\/]/.test(target) ||
        /[\u0000-\u001f\u007f]/.test(target)
    ) return ''
    const plainPath = target.split(/[?#]/)[0] || '/'
    if (LEGACY_AUTH_PATHS.has(plainPath)) return ''

    if (target.startsWith('/ucenter/#/')) {
        const hashPath = target.slice('/ucenter/#'.length).split('?')[0]
        if (UCENTER_AUTH_HASHES.has(hashPath)) return ''
    }

    try {
        const resolved = new URL(target, 'https://pc.local')
        if (resolved.origin !== 'https://pc.local') return ''
    } catch {
        return ''
    }
    return target
}

export const ucenterAuthUrl = (kind: ConsumerAuthKind, redirect?: string): string => {
    const path = AUTH_PATHS[kind]
    if (!path) throw new Error(`Unsupported consumer auth route: ${kind}`)
    const safeRedirect = safeReturnTarget(redirect)
    return `/ucenter/#${path}${safeRedirect ? `?redirect=${encodeURIComponent(safeRedirect)}` : ''}`
}

export const ucenterAuthUrlFromSearch = (kind: ConsumerAuthKind, search = ''): string => {
    const redirect = new URLSearchParams(search).get('redirect')
    return ucenterAuthUrl(kind, redirect || undefined)
}
