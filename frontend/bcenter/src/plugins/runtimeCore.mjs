const COMPONENT_PREFIX = 'plugin:'
const SELLER_TERMINALS = new Set(['seller', 'merchant', '商家端'])

export function parseSellerComponent(value) {
    const raw = String(value || '').trim()
    if (!raw.startsWith(COMPONENT_PREFIX)) return null
    const matched = raw.slice(COMPONENT_PREFIX.length).match(/^([a-z0-9][a-z0-9-]*)\/([a-z0-9][a-z0-9-]*)$/)
    return matched ? { code: matched[1], view: matched[2], key: raw } : null
}

export function collectAuthorizedSellerComponents(menuTree) {
    const found = new Map()
    const visit = (rows) => {
        if (!Array.isArray(rows)) return
        for (const row of rows) {
            if (!row || Number(row.status ?? 1) !== 1 || Number(row.show_status ?? 1) !== 1) continue
            const parsed = parseSellerComponent(row.component)
            if (parsed) {
                if (!found.has(parsed.code)) found.set(parsed.code, new Set())
                found.get(parsed.code).add(parsed.view)
            }
            visit(row.sub)
        }
    }
    visit(menuTree)
    return found
}

function hasSellerTerminal(plugin) {
    return Array.isArray(plugin?.terminals) && plugin.terminals.some((item) =>
        SELLER_TERMINALS.has(String(item || '').trim().toLowerCase())
    )
}

function isSellerBundle(value) {
    if (!value) return false
    const path = String(value).split(/[?#]/, 1)[0]
    return /(?:^|\/)bcenter\.mjs$/.test(path)
}

export function resolveSellerEntry(plugin) {
    if (!plugin?.installed || !plugin?.enabled || !hasSellerTerminal(plugin)) return null
    const explicit = plugin.sellerEntry || plugin.sellerFrontendUrl
    if (explicit) return isSellerBundle(explicit) ? String(explicit) : null
    const adminEntry = String(plugin.frontendUrl || '')
    if (!/(?:^|\/)index\.mjs(?:[?#]|$)/.test(adminEntry)) return null
    return adminEntry.replace(/\/index\.mjs(?=[?#]|$)/, '/bcenter.mjs')
}

function normalizeManifest(code, module) {
    const manifest = module?.default ?? module
    if (!manifest || typeof manifest !== 'object') {
        throw new Error(`商家插件 ${code} 缺少默认导出`)
    }
    const views = manifest.views && typeof manifest.views === 'object' ? manifest.views : {}
    return {
        views,
        setup: typeof manifest.setup === 'function' ? manifest.setup : null,
        teardown: typeof manifest.teardown === 'function' ? manifest.teardown : null
    }
}

/**
 * @param {{ importModule: Function, createContext?: Function }} options
 */
export function createSellerPluginRegistry({ importModule, createContext = () => ({}) }) {
    if (typeof importModule !== 'function') throw new TypeError('importModule 必须是函数')
    const loaded = new Map()
    const components = new Map()

    const unload = async (code) => {
        const current = loaded.get(code)
        if (!current) return
        await current.manifest.teardown?.()
        loaded.delete(code)
        for (const key of [...components.keys()]) {
            if (key.startsWith(`${COMPONENT_PREFIX}${code}/`)) components.delete(key)
        }
    }

    return {
        async sync(menuTree, plugins) {
            const authorized = collectAuthorizedSellerComponents(menuTree)
            const catalog = Array.isArray(plugins) ? plugins : []
            const targets = new Map()
            for (const plugin of catalog) {
                const code = String(plugin?.code || '').trim()
                const entry = resolveSellerEntry(plugin)
                if (code && entry && authorized.has(code)) targets.set(code, { plugin, entry })
            }

            for (const code of [...loaded.keys()]) {
                const target = targets.get(code)
                if (!target || target.entry !== loaded.get(code).entry) await unload(code)
            }

            for (const [code, target] of targets) {
                if (!loaded.has(code)) {
                    const manifest = normalizeManifest(code, await importModule(target.entry))
                    await manifest.setup?.(createContext(code, target.plugin))
                    loaded.set(code, { entry: target.entry, manifest })
                }
                const manifest = loaded.get(code).manifest
                for (const view of authorized.get(code)) {
                    const component = manifest.views[view]
                    if (component) components.set(`${COMPONENT_PREFIX}${code}/${view}`, component)
                }
            }
        },
        resolve(component) {
            return components.get(String(component || '')) || null
        },
        async reset() {
            for (const code of [...loaded.keys()]) await unload(code)
        },
        loadedCodes() {
            return [...loaded.keys()]
        }
    }
}
