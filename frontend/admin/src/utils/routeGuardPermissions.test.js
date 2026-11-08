// @vitest-environment jsdom
import { beforeEach, afterEach, describe, expect, it, vi } from 'vitest'

const fixtures = vi.hoisted(() => ({ app: null, plugin: null }))
vi.mock('@/utils/auth', () => ({ getToken: () => 'account-a', getUserId: () => 7 }))
vi.mock('@/stores/auth', () => ({ useAuthStore: () => ({ userInfo: { id: 7 } }) }))
vi.mock('@/stores/app', () => ({ useAppStore: () => fixtures.app }))
vi.mock('@/stores/plugin', () => ({ usePluginStore: () => fixtures.plugin }))
vi.mock('@/utils/userMenusCache', () => ({ refreshPermissionsFromServer: vi.fn().mockResolvedValue(true) }))
vi.mock('@/utils/permission', () => ({ hasPermission: () => false }))
vi.mock('@/config/dashboard/modules', () => ({ findModuleByRoute: () => null }))

import { beforeEachGuard } from './routeGuard'
import { buildPluginMenuNodes, mergePluginMenus } from './pluginMenuMerge'
import { isPathAllowedByMenuTree } from './menuNavigate'

const dashboard = { id: 19, pid: 0, paths: '/dashboard', name: '仪表盘', type: 'M' }
const webpos = {
  id: 40, pid: 0, paths: '/plugin/webpos', name: 'WebPOS', type: 'M', display_pos: 'plugin',
  children: [{ id: 41, pid: 40, paths: '/plugin/webpos/orders', name: '订单', type: 'C' }]
}
const artifact = () => buildPluginMenuNodes({ code: 'webpos' }, [
  { path: '/plugin/webpos', title: 'WebPOS 收银台', icon: 'Monitor', sort: 3000 }
])
const visit = async path => {
  const next = vi.fn()
  await beforeEachGuard({ path, fullPath: path, name: 'PluginWebpos', meta: {}, query: {} }, {}, next)
  return next
}

describe('server-authorized plugin navigation', () => {
  beforeEach(() => {
    localStorage.clear()
    localStorage.setItem('multiOrganizationEnabled', '0')
    fixtures.app = { menuLoaded: true, baseMenus: [dashboard], dynamicMenus: [dashboard], fetchMenus: vi.fn() }
    fixtures.plugin = { loadedOnce: true, loadedCodes: ['webpos'], loadInstalledPlugins: vi.fn(), waitForBackgroundPlugins: vi.fn() }
    vi.spyOn(console, 'warn').mockImplementation(() => {})
  })
  afterEach(() => vi.restoreAllMocks())

  it('redirects the old activity entry without loading its plugin and still protects the target', async () => {
    const next = await visit('/plugin/activity')
    expect(next).toHaveBeenCalledWith({ path: '/platform/plugins', query: { cateId: '4', entry: 'marketing' }, replace: true })
    expect(fixtures.plugin.waitForBackgroundPlugins).not.toHaveBeenCalled()
    expect(await visit('/platform/plugins')).toHaveBeenCalledWith({ path: '/dashboard', replace: true })
  })

  it('does not create WebPOS access from the real ungrouped manifest menu', () => {
    const tree = mergePluginMenus([dashboard], artifact())
    expect(tree.map(menu => menu.paths)).toEqual(['/dashboard'])
    expect(isPathAllowedByMenuTree(tree, '/plugin/webpos')).toBe(false)
  })

  it('rejects a deep link even when the runtime menu tree already contains an unauthorized manifest root', async () => {
    fixtures.app.dynamicMenus = [dashboard, ...artifact()]
    const next = await visit('/plugin/webpos/orders')
    expect(next).toHaveBeenCalledWith({ path: '/dashboard', replace: true })
  })

  it('still loads the plugin and permits a server-authorized page', async () => {
    fixtures.app.baseMenus = [dashboard, webpos]
    fixtures.app.dynamicMenus = [dashboard, webpos]
    fixtures.plugin.loadedOnce = false
    fixtures.plugin.loadInstalledPlugins.mockImplementation(async () => { fixtures.plugin.loadedOnce = true })

    const next = await visit('/plugin/webpos/orders')

    expect(fixtures.plugin.loadInstalledPlugins).toHaveBeenCalledWith({ priorityCode: 'webpos' })
    expect(next).toHaveBeenCalledWith()
  })

  it('does not turn an authorized application container into permission for an absent sibling page', async () => {
    fixtures.app.baseMenus = [dashboard, webpos]
    fixtures.app.dynamicMenus = [dashboard, webpos]
    const next = await visit('/plugin/webpos/settings')
    expect(next).toHaveBeenCalledWith({ path: '/dashboard', replace: true })
  })

  it('keeps detail pages under an authorized leaf accessible', async () => {
    fixtures.app.baseMenus = [dashboard, webpos]
    fixtures.app.dynamicMenus = [dashboard, webpos]
    const next = await visit('/plugin/webpos/orders/123')
    expect(next).toHaveBeenCalledWith()
  })

  it('preserves an authorized single-page plugin root without granting unlisted child pages', async () => {
    fixtures.app.baseMenus = [dashboard, { ...webpos, children: [] }]
    fixtures.app.dynamicMenus = fixtures.app.baseMenus
    expect(await visit('/plugin/webpos')).toHaveBeenCalledWith()
    expect(await visit('/plugin/webpos/settings')).toHaveBeenCalledWith({ path: '/dashboard', replace: true })
  })

  it('ignores disabled server nodes even when a runtime copy is enabled', async () => {
    fixtures.app.baseMenus = [dashboard, { ...webpos, is_disable: '1' }]
    fixtures.app.dynamicMenus = [dashboard, webpos]
    expect(await visit('/plugin/webpos/orders')).toHaveBeenCalledWith({ path: '/dashboard', replace: true })
  })

  it('preserves authorized core entries and the built-in lowcode page', async () => {
    const paths = ['/dashboard', '/mall', '/mall/store', '/payment', '/platform/plugins', '/setting', '/lowcode']
    fixtures.app.baseMenus = paths.map((paths, id) => ({ id, pid: 0, paths }))
    for (const path of paths) expect(await visit(path)).toHaveBeenCalledWith()
  })

  it('does not grant OA sibling pages from a workbench-only permission', async () => {
    fixtures.app.baseMenus = [{ id: 7, paths: '/plugin/oa', children: [
      { id: 54017, paths: '/plugin/oa/apps', name: '工作台' }
    ] }]
    expect(await visit('/plugin/oa/apps')).toHaveBeenCalledWith()
    expect(await visit('/plugin/oa/workbench')).toHaveBeenCalledWith()
    expect(await visit('/plugin/oa/approval')).toHaveBeenCalledWith({ path: '/dashboard', replace: true })
  })

  it('rejects plugin routes when the successful server menu tree is empty', async () => {
    fixtures.app.baseMenus = []
    fixtures.app.dynamicMenus = artifact()
    const next = await visit('/plugin/webpos')
    expect(next).toHaveBeenCalledWith({ path: '/dashboard', replace: true })
  })

  it('does not grant access when server menu loading fails', async () => {
    fixtures.app.menuLoaded = false
    fixtures.app.baseMenus = []
    fixtures.app.fetchMenus.mockRejectedValue(new Error('permission backend unavailable'))
    const next = await visit('/plugin/webpos')
    expect(next).toHaveBeenCalledWith({ path: '/dashboard', replace: true })
  })
})
