const retiredBrandHosts = new Set(['bbc.houjit.com'])

export function resolveBrandAsset(value?: string | null) {
    if (!value) return ''
    try {
        const url = new URL(value, window.location.origin)
        return retiredBrandHosts.has(url.hostname) ? '' : url.href
    } catch {
        return ''
    }
}
