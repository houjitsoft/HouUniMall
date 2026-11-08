import { beforeEach, describe, expect, it, vi } from 'vitest'
import { createPinia, setActivePinia } from 'pinia'

const app = vi.hoisted(() => ({ setExtraMenus: vi.fn(), fetchMenus: vi.fn() }))
vi.mock('@/stores/app', () => ({ useAppStore: () => app }))
vi.mock('@/utils/userMenusCache', () => ({ invalidateUserMenusCache: vi.fn() }))

vi.mock('@/api/plugin-center/pluginCenter', () => ({
  getPluginList: vi.fn()
}))

// 保留真实 dashboard store，仅隔离模块注册表的视图依赖。
vi.mock('@/config/dashboard/modules', () => ({ DASHBOARD_MODULES: [], buildDefaultItems: () => [] }))

import { getPluginList } from '@/api/plugin-center/pluginCenter'
import { useDashboardStore } from './dashboard'
import { normalizePluginApiResponse, normalizePluginManifest, usePluginStore } from './plugin'

describe('plugin API response normalization', () => {
  it('preserves the business envelope returned by the host interceptor', () => {
    const response = {
      code: 1,
      message: 'ok',
      data: { list: [{ id: 1 }] }
    }

    expect(normalizePluginApiResponse(response)).toBe(response)
  })

  it('unwraps an actual Axios-like HTTP response exactly once', () => {
    const payload = { code: 1, message: 'ok', data: { list: [] } }

    expect(normalizePluginApiResponse({ status: 200, headers: {}, data: payload })).toBe(payload)
  })
})

describe('plugin store loading', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
    vi.clearAllMocks()
  })

  it('shares one in-flight load with concurrent callers', async () => {
    let resolveList
    getPluginList.mockImplementation(() => new Promise((resolve) => {
      resolveList = resolve
    }))
    const store = usePluginStore()

    const guardLoad = store.loadInstalledPlugins()
    const layoutLoad = store.loadInstalledPlugins()
    resolveList([])

    await expect(guardLoad).resolves.toBe(true)
    await expect(layoutLoad).resolves.toBe(true)
    expect(getPluginList).toHaveBeenCalledTimes(1)
    expect(store.loadedOnce).toBe(true)
  })

  it('keeps the loader retryable when the installed-plugin request fails', async () => {
    getPluginList.mockRejectedValue(new Error('plugin backend is still starting'))
    const store = usePluginStore()

    await expect(store.loadInstalledPlugins()).resolves.toBe(false)
    expect(store.loadedOnce).toBe(false)
    expect(store.loading).toBe(false)
    expect(store.errors).toEqual([
      { code: '*', message: 'plugin backend is still starting' }
    ])
  })
})


describe('插件卸载清理工作台面板', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
    vi.clearAllMocks()
    app.fetchMenus.mockResolvedValue()
  })

  it.each([false, true])('卸载 OA 清除其全部面板并保留其他插件面板（teardown 异常：%s）', async (teardownFails) => {
    const store = usePluginStore()
    const dashboard = useDashboardStore()
    const component = { render: () => null }
    dashboard.registerPluginPanel('dailyTodo', { code: 'oa', component })
    dashboard.registerPluginPanel('notes', { code: 'oa', component })
    dashboard.registerPluginPanel('projectSummary', { code: 'pm', component })
    const otherPanel = dashboard.pluginPanels.projectSummary
    const teardown = vi.fn().mockImplementation(async () => {
      if (teardownFails) throw new Error('卸载回调失败')
    })
    store.manifests = { oa: { teardown }, pm: {} }
    store.loadedCodes = ['oa', 'pm']
    vi.spyOn(store, 'resolveRouter').mockResolvedValue(null)
    const unregisterPanels = vi.spyOn(dashboard, 'unregisterPluginPanels')
    expect(dashboard.todoPanel).toBe(component)

    await expect(store.unregisterPlugin('oa')).resolves.toBe(true)

    expect(teardown).toHaveBeenCalledTimes(1)
    expect(unregisterPanels).toHaveBeenCalledExactlyOnceWith('oa')
    expect(dashboard.todoPanel).toBeNull()
    expect(dashboard.pluginPanels).toEqual({ projectSummary: otherPanel })
    expect(store.loadedCodes).toEqual(['pm'])
    expect(store.manifests.oa).toBeUndefined()
    expect(store.manifests.pm).toBeDefined()
    expect(store.errors).toEqual(teardownFails
      ? [{ code: 'oa', message: 'teardown 失败: 卸载回调失败' }]
      : [])
  })

  it('清空插件时移除全部插件面板', async () => {
    const store = usePluginStore()
    const dashboard = useDashboardStore()
    const component = { render: () => null }
    dashboard.registerPluginPanel('dailyTodo', { code: 'oa', component })
    dashboard.registerPluginPanel('projectSummary', { code: 'pm', component })
    store.loadedCodes = ['oa', 'pm']
    store.manifests = { oa: {}, pm: {} }
    vi.spyOn(store, 'resolveRouter').mockResolvedValue(null)

    await store.clearPlugins()

    expect(dashboard.pluginPanels).toEqual({})
    expect(dashboard.todoPanel).toBeNull()
    expect(store.loadedCodes).toEqual([])
    expect(store.manifests).toEqual({})
  })
})


describe('purchased plugin page layouts', () => {
  it('loads the product shell only when the purchased manifest provides one', async () => {
    const component = { render: () => null }
    const load = vi.fn().mockResolvedValue({ default: component })
    const manifest = normalizePluginManifest('geo', { default: { layout: load } })
    expect(load).not.toHaveBeenCalled()
    await expect(manifest.layout.__asyncLoader()).resolves.toBe(component)
    expect(load).toHaveBeenCalledTimes(1)
    expect(normalizePluginManifest('erp', { routes: [] }).layout).toBeNull()
  })
})
