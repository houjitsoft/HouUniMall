function stableSerialize(value) {
    if (value === undefined) return ''
    if (value === null || typeof value !== 'object') return JSON.stringify(value)
    if (Array.isArray(value)) return `[${value.map(stableSerialize).join(',')}]`
    if (typeof URLSearchParams !== 'undefined' && value instanceof URLSearchParams) {
        return value.toString()
    }

    return `{${Object.keys(value)
        .sort()
        .map((key) => `${JSON.stringify(key)}:${stableSerialize(value[key])}`)
        .join(',')}}`
}

export function buildRequestKey(config = {}) {
    return [
        String(config.method || 'get').toUpperCase(),
        String(config.url || ''),
        stableSerialize(config.params),
        stableSerialize(config.data)
    ].join('&')
}
