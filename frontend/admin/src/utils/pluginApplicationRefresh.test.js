// @vitest-environment jsdom
import { beforeEach, describe, expect, it, vi } from 'vitest'

const refreshMenusAfterPermissionChange = vi.fn(async () => {})
const syncInstalledPlugins = vi.fn(async () => true)
const pluginStore = { loadedOnce: true, loading: false, syncInstalledPlugins }

vi.mock('@/utils/userMenusCache', () => ({ refreshMenusAfterPermissionChange }))
vi.mock('@/stores/plugin', () => ({ usePluginStore: () => pluginStore }))

import { refreshPluginApplications, registerPluginApplicationRefresher } from './pluginApplicationRefresh'

describe('refreshPluginApplications', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    pluginStore.loadedOnce = true
    pluginStore.loading = false
  })

  it('安装成功后同时刷新权限菜单、应用目录和插件路由', async () => {
    const catalog = vi.fn(async () => {})
    const stop = registerPluginApplicationRefresher(catalog)

    await refreshPluginApplications()

    expect(refreshMenusAfterPermissionChange).toHaveBeenCalledTimes(1)
    expect(catalog).toHaveBeenCalledTimes(1)
    expect(syncInstalledPlugins).toHaveBeenCalledTimes(1)
    stop()
  })

  it('插件运行时还没开始加载时不补注册路由', async () => {
    pluginStore.loadedOnce = false
    pluginStore.loading = false

    await refreshPluginApplications()

    expect(refreshMenusAfterPermissionChange).toHaveBeenCalledTimes(1)
    expect(syncInstalledPlugins).not.toHaveBeenCalled()
  })
})
