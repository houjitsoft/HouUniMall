// @vitest-environment jsdom
import { beforeEach, describe, expect, it, vi } from 'vitest'
import { flushPromises, mount } from '@vue/test-utils'
import { reactive } from 'vue'

const fixtures = vi.hoisted(() => ({ app: null, auth: null, router: { push: vi.fn() } }))
vi.mock('@/stores/app', () => ({ useAppStore: () => fixtures.app }))
vi.mock('@/stores/auth', () => ({ useAuthStore: () => fixtures.auth }))
vi.mock('vue-router', () => ({ useRoute: () => ({ path: '/dashboard' }), useRouter: () => fixtures.router }))
vi.mock('@/utils/site', () => ({ getSiteInstanceId: () => 'test-site' }))
vi.mock('@/api/plugin-center/pluginCenter', () => ({ getPluginList: vi.fn() }))
vi.mock('@/utils/firstLevelMenuNav.js', async importOriginal => ({
  ...await importOriginal(), navigateFromMenuNode: vi.fn()
}))

import HeaderPluginMenu from './HeaderPluginMenu.vue'
import { getPluginList } from '@/api/plugin-center/pluginCenter'
import { navigateFromMenuNode } from '@/utils/firstLevelMenuNav.js'

const mountPanel = () => mount(HeaderPluginMenu, {
  global: {
    stubs: {
      'el-popover': { template: '<div><slot name="reference" /><slot /></div>' },
      'el-input': true,
      'el-icon': { template: '<span><slot /></span>' },
      'el-button': { template: '<button><slot /></button>' }
    },
    directives: { loading: {} }
  }
})

describe('application launcher permissions and retry', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    localStorage.clear()
    const authorizedMenu = {
      id: 20, pid: 0, paths: '/plugin/erp', display_pos: 'plugin', name: 'ERP',
      children: [{ id: 21, pid: 20, paths: '/plugin/erp/purchase', name: '采购管理' }]
    }
    fixtures.app = reactive({
      menuLoaded: true,
      baseMenus: [authorizedMenu],
      dynamicMenus: [{ ...authorizedMenu, children: [{ id: 22, pid: 20, paths: '/plugin/erp/admin', name: '管理设置' }] }],
      pluginCategories: [{ id: 42, name: '客户分类' }],
      pluginCategoriesError: false,
      loadPluginCategories: vi.fn().mockResolvedValue([]),
      setActiveNavMenu: vi.fn()
    })
    fixtures.auth = reactive({ token: 'account-a', userInfo: { id: 7 } })
    getPluginList.mockResolvedValue([{ code: 'erp', name: 'ERP', cateId: 42, installed: true, enabled: true }])
  })

  it('navigates using the server-authorized subtree even when the runtime tree contributes other pages', async () => {
    const wrapper = mountPanel()
    await flushPromises()

    await wrapper.get('.plugin-menu-panel__item').trigger('click')

    const [menu] = navigateFromMenuNode.mock.calls[0]
    expect(menu.children.map(child => child.paths)).toEqual(['/plugin/erp/purchase'])
    wrapper.unmount()
  })

  it('shows category failure and retries both state sources without removing existing groups', async () => {
    const wrapper = mountPanel()
    await flushPromises()
    fixtures.app.pluginCategoriesError = true
    await wrapper.vm.$nextTick()
    expect(wrapper.get('[role="status"]').text()).toContain('应用分类加载失败')
    expect(wrapper.text()).toContain('客户分类')

    await wrapper.get('[role="status"] button').trigger('click')
    await flushPromises()

    expect(fixtures.app.loadPluginCategories).toHaveBeenCalledTimes(2)
    expect(getPluginList).toHaveBeenCalledTimes(2)
    wrapper.unmount()
  })
  it('keeps a known unavailable application visible without navigating into its broken page', async () => {
    getPluginList.mockResolvedValue([{ code: 'erp', name: 'ERP', installed: true, enabled: true, healthStatus: 'UNAVAILABLE' }])
    const wrapper = mountPanel()
    await flushPromises()
    expect(wrapper.get('.plugin-menu-panel__item').text()).toContain('暂不可用')
    await wrapper.get('.plugin-menu-panel__item').trigger('click')
    expect(navigateFromMenuNode).not.toHaveBeenCalled()
    wrapper.unmount()
  })

})
