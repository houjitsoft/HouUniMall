// @vitest-environment jsdom
import { beforeEach, describe, expect, it, vi } from 'vitest'
import { mount, flushPromises } from '@vue/test-utils'
import ElementPlus from 'element-plus'
import MenuManagement from './MenuManagement.vue'
import { updateMenuDisplay } from '@/api/permission/menuDisplay'

const menus = [{ id: 88, pid: 0, type: 'M', name: 'ERP', paths: '/plugin/erp',
  pluginCode: 'erp', displayPos: 'plugin', cateId: 87,
  children: [{ id: 89, pid: 88, type: 'C', name: '采购管理', paths: '/plugin/erp/purchase' }] }]
vi.mock('@/utils/request', () => ({ default: { post: vi.fn(async () => ({ code: 1, data: structuredClone(menus) })) } }))
vi.mock('@/api/permission/menuDisplay', () => ({ updateMenuDisplay: vi.fn(async () => ({ code: 1 })) }))
vi.mock('@/api/plugin-center/pluginCenter', () => ({ getPluginCategories: vi.fn(async () => [{ id: 87, name: '供应链与生产' }]) }))
vi.mock('@/stores/app', () => ({ useAppStore: () => ({ loadPluginCategories: vi.fn(async () => []) }) }))
vi.mock('@/utils/userMenusCache', () => ({ refreshMenusAfterPermissionChange: vi.fn(), clearMenuCacheAndRefresh: vi.fn(async () => {}) }))
vi.mock('@/components/setting/PermissionTabs.vue', () => ({ default: { template: '<div />' } }))
const mountPage = async () => {
  const wrapper = mount(MenuManagement, { global: { plugins: [ElementPlus] } })
  await flushPromises()
  return wrapper
}
beforeEach(() => vi.clearAllMocks())
describe('菜单管理分类与搜索', () => {
  it('隐藏再恢复应用时仍提交原分类，不把归属清零', async () => {
    const wrapper = await mountPage()
    const row = wrapper.vm.menuList[0]
    await wrapper.vm.handleDisplayPosChange(row, 'hidden')
    await wrapper.vm.handleDisplayPosChange(row, 'plugin')
    expect(updateMenuDisplay.mock.calls.map(([payload]) => payload)).toEqual([
      { id: 88, displayPos: 'hidden', cateId: 87 },
      { id: 88, displayPos: 'plugin', cateId: 87 }
    ])
    wrapper.unmount()
  })
  it('搜索内部子菜单后自动展开祖先行', async () => {
    const wrapper = await mountPage()
    await wrapper.findAll('[role="tab"]').find(tab => tab.text() === '应用菜单').trigger('click')
    await wrapper.find('input[placeholder="请输入菜单名称"]').setValue('采购')
    await wrapper.findAll('button').find(button => button.text() === '搜索').trigger('click')
    await flushPromises()
    const child = wrapper.findAll('.el-table__body tr').find(row => row.text().includes('采购管理'))
    expect(child).toBeDefined()
    expect(child.isVisible()).toBe(true)
    wrapper.unmount()
  })
})
