import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'
import { createPinia, setActivePinia } from 'pinia'

vi.mock('@/api/auth/auth', () => ({ getUserMenus: vi.fn() }))
vi.mock('@/utils/auth', () => ({ getToken: vi.fn() }))
vi.mock('@/utils/userMenusCache', () => ({
  invalidateUserMenusCache: vi.fn(),
  readUserMenusCache: vi.fn(),
  refreshPermissionsFromServer: vi.fn().mockResolvedValue(true)
}))
vi.mock('@/api/plugin-center/pluginCenter', () => ({ getPluginCategories: vi.fn() }))

import { getUserMenus } from '@/api/auth/auth'
import { getToken } from '@/utils/auth'
import { readUserMenusCache } from '@/utils/userMenusCache'
import { getPluginCategories } from '@/api/plugin-center/pluginCenter'
import { useAppStore } from './app'

const appMenu = (id, code) => ({ id, pid: 0, type: 'M', name: code, paths: `/plugin/${code}`, display_pos: 'plugin', children: [] })
const deferred = () => {
  let resolve
  const promise = new Promise(done => { resolve = done })
  return { promise, resolve }
}

describe('server menu refresh and account isolation', () => {
  let store
  beforeEach(() => {
    vi.clearAllMocks()
    getToken.mockReturnValue('account-a')
    readUserMenusCache.mockReturnValue(null)
    vi.stubGlobal('navigator', { onLine: true })
    setActivePinia(createPinia())
    store = useAppStore()
    store.clearMenus({ skipInvalidate: true })
  })
  afterEach(() => vi.unstubAllGlobals())

  it('does not mark server permissions loaded when plugin localization rebuilds before the first fetch', async () => {
    store._rebuildMenus()
    expect(store.menuLoaded).toBe(false)
    getUserMenus.mockResolvedValue({ code: 1, data: [appMenu(30, 'oa')] })
    await store.fetchMenus()
    expect(store.menuLoaded).toBe(true)
    expect(store.baseMenus.map(menu => menu.id)).toEqual([30])
  })

  it('removes every old entry when a successful forced refresh returns no permissions', async () => {
    store._applyMenuData([appMenu(20, 'erp')])
    getUserMenus.mockResolvedValue({ code: 1, data: [] })

    await store.fetchMenus(true)

    expect(store.baseMenus).toEqual([])
    expect(store.dynamicMenus).toEqual([])
    expect(store.menuLoaded).toBe(true)
  })

  it('replaces cached permissions with an empty successful background response', async () => {
    const fresh = deferred()
    readUserMenusCache.mockReturnValue([appMenu(20, 'erp')])
    getUserMenus.mockReturnValue(fresh.promise)
    await store.fetchMenus()
    expect(store.baseMenus.map(menu => menu.id)).toEqual([20])

    fresh.resolve({ code: 1, data: { list: [] } })
    await fresh.promise

    expect(store.baseMenus).toEqual([])
    expect(store.dynamicMenus).toEqual([])
  })

  it('ignores a previous account response that finishes after the new account menus', async () => {
    const previous = deferred()
    getUserMenus.mockReturnValueOnce(previous.promise)
    const previousFetch = store.fetchMenus(true)
    getToken.mockReturnValue('account-b')
    store.clearMenus({ skipInvalidate: true })
    getUserMenus.mockResolvedValueOnce({ code: 1, data: [appMenu(30, 'oa')] })
    await store.fetchMenus(true)

    previous.resolve({ code: 1, data: [appMenu(20, 'erp')] })
    await previousFetch

    expect(store.baseMenus.map(menu => menu.id)).toEqual([30])
    expect(store.dynamicMenus.map(menu => menu.id)).toEqual([30])
  })

  it('ignores cached background refreshes after menu state has been cleared', async () => {
    const previous = deferred()
    readUserMenusCache.mockReturnValue([appMenu(20, 'erp')])
    getUserMenus.mockReturnValue(previous.promise)
    await store.fetchMenus()
    store.clearMenus({ skipInvalidate: true })

    previous.resolve({ code: 1, data: [appMenu(20, 'erp')] })
    await previous.promise

    expect(store.baseMenus).toEqual([])
    expect(store.dynamicMenus).toEqual([])
    expect(store.menuLoaded).toBe(false)
  })

  it('keeps the new account request shared when an older request finishes', async () => {
    const previous = deferred()
    const current = deferred()
    getUserMenus.mockReturnValueOnce(previous.promise).mockReturnValue(current.promise)
    const previousFetch = store.fetchMenus(true)
    getToken.mockReturnValue('account-b')
    store.clearMenus({ skipInvalidate: true })
    const currentFetch = store.fetchMenus(true)
    previous.resolve({ code: 1, data: [] })
    await previousFetch

    const concurrentFetch = store.fetchMenus(true)
    current.resolve({ code: 1, data: [appMenu(30, 'oa')] })
    await Promise.all([currentFetch, concurrentFetch])

    expect(getUserMenus).toHaveBeenCalledTimes(2)
    expect(store.baseMenus.map(menu => menu.id)).toEqual([30])
  })

  it('can force a new request after returning cached menus', async () => {
    readUserMenusCache.mockReturnValue([appMenu(20, 'erp')])
    vi.stubGlobal('navigator', { onLine: false })
    await store.fetchMenus()
    getUserMenus.mockResolvedValue({ code: 1, data: [] })

    await store.fetchMenus(true)

    expect(store.baseMenus).toEqual([])
    expect(store.dynamicMenus).toEqual([])
  })

  it('keeps the current sidebar visible while a forced refresh is still in flight', async () => {
    store._applyMenuData([appMenu(20, 'erp')])
    store.setActiveNavMenu(20)
    const pending = deferred()
    getUserMenus.mockReturnValue(pending.promise)

    store.clearMenus({ skipInvalidate: true, keepVisible: true })
    const refresh = store.fetchMenus(true)

    expect(store.dynamicMenus.map(menu => menu.id)).toEqual([20])
    expect(store.baseMenus.map(menu => menu.id)).toEqual([20])
    expect(store.activeNavMenuId).toBe('20')
    expect(store.menuLoaded).toBe(true)

    pending.resolve({ code: 1, data: [appMenu(30, 'oa')] })
    await refresh

    expect(store.dynamicMenus.map(menu => menu.id)).toEqual([30])
    expect(store.baseMenus.map(menu => menu.id)).toEqual([30])
  })

  it('drops an in-flight menu response after a visible refresh is started', async () => {
    const previous = deferred()
    readUserMenusCache.mockReturnValue([appMenu(20, 'erp')])
    getUserMenus.mockReturnValueOnce(previous.promise)
    await store.fetchMenus()
    const next = deferred()
    getUserMenus.mockReturnValueOnce(next.promise)

    store.clearMenus({ skipInvalidate: true, keepVisible: true })
    const refresh = store.fetchMenus(true)
    previous.resolve({ code: 1, data: [appMenu(99, 'stale')] })
    await previous.promise
    expect(store.baseMenus.map(menu => menu.id)).toEqual([20])

    next.resolve({ code: 1, data: [appMenu(30, 'oa')] })
    await refresh
    expect(store.baseMenus.map(menu => menu.id)).toEqual([30])
  })

  it('does not synthesize WebPOS secondary menus absent from the server tree', () => {
    store._applyMenuData([appMenu(40, 'webpos')])
    expect(store.dynamicMenus[0].children).toEqual([])
  })

  it('keeps the current session categories when refreshing fails and clears the error after retry', async () => {
    const warning = vi.spyOn(console, 'warn').mockImplementation(() => {})
    getPluginCategories.mockResolvedValueOnce([{ id: 42, name: '客户分类' }])
    await store.loadPluginCategories()
    getPluginCategories.mockRejectedValueOnce(new Error('temporarily unavailable'))

    await store.loadPluginCategories()

    expect(store.pluginCategories).toEqual([{ id: 42, name: '客户分类' }])
    expect(store.pluginCategoriesError).toBe(true)
    getPluginCategories.mockResolvedValueOnce([{ id: 42, name: '已改名分类' }])
    await store.loadPluginCategories()
    expect(store.pluginCategories).toEqual([{ id: 42, name: '已改名分类' }])
    expect(store.pluginCategoriesError).toBe(false)
    warning.mockRestore()
  })

  it('rejects stale categories across account changes', async () => {
    getPluginCategories.mockResolvedValueOnce([{ id: 42, name: '账号A分类' }])
    await store.loadPluginCategories()
    const previous = deferred()
    getPluginCategories.mockReturnValueOnce(previous.promise)
    const previousFetch = store.loadPluginCategories()
    getToken.mockReturnValue('account-b')
    store.clearMenus({ skipInvalidate: true })
    expect(store.pluginCategories).toEqual([])

    getPluginCategories.mockResolvedValueOnce([{ id: 8, name: '账号B分类' }])
    await store.loadPluginCategories()
    previous.resolve([{ id: 42, name: '账号A新分类' }])
    await previousFetch

    expect(store.pluginCategories).toEqual([{ id: 8, name: '账号B分类' }])
    expect(store.pluginCategoriesError).toBe(false)
  })

  it('reports invalid category responses without discarding the last successful dictionary', async () => {
    const warning = vi.spyOn(console, 'warn').mockImplementation(() => {})
    getPluginCategories.mockResolvedValueOnce([{ id: 42, name: '客户分类' }])
    await store.loadPluginCategories()
    getPluginCategories.mockResolvedValueOnce({ message: 'bad gateway' })

    await store.loadPluginCategories()

    expect(store.pluginCategories).toEqual([{ id: 42, name: '客户分类' }])
    expect(store.pluginCategoriesError).toBe(true)
    warning.mockRestore()
  })
})
