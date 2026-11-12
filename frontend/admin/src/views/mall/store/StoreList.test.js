// @vitest-environment jsdom
import { flushPromises, shallowMount } from '@vue/test-utils'
import { beforeEach, describe, expect, it, vi } from 'vitest'
const mocks = vi.hoisted(() => ({
  mallShopLists: vi.fn(), mallShopProvisionOptions: vi.fn(),
  mallShopProvisionAccountOptions: vi.fn(), mallShopProvisionCreate: vi.fn(),
  shopLevelList: vi.fn(), push: vi.fn(), error: vi.fn(), info: vi.fn()
}))
vi.mock('@/api/mall/shopAdmin', () => ({
  mallShopLists: mocks.mallShopLists, mallShopProvisionOptions: mocks.mallShopProvisionOptions,
  mallShopProvisionAccountOptions: mocks.mallShopProvisionAccountOptions,
  mallShopProvisionCreate: mocks.mallShopProvisionCreate,
  mallShopLevelOptions: mocks.shopLevelList
}))
vi.mock('vue-router', () => ({ useRouter: () => ({ push: mocks.push }) }))
vi.mock('element-plus', () => ({ ElMessage: { success: vi.fn(), error: mocks.error, info: mocks.info } }))
import StoreList from './StoreList.vue'
const mountList = () => shallowMount(StoreList, {
  global: {
    stubs: { 'el-table': { template: '<div><slot /></div>' }, 'el-table-column': true },
    config: { warnHandler: () => {} },
    directives: { loading: {} }
  }
})
describe('店铺列表参考图与交互', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    mocks.mallShopLists.mockResolvedValue({ code: 1, data: { lists: [{ id: 1, name: '测试店铺', shopStatus: 1 }], count: 1 } })
    mocks.mallShopProvisionOptions.mockResolvedValue({ code: 1, data: [{ id: 2, name: '软件服务' }] })
    mocks.mallShopProvisionAccountOptions.mockResolvedValue({ code: 1, data: [] })
    mocks.shopLevelList.mockResolvedValue({ code: 1, data: [{ id: 1, storeLevel: '普通店铺' }] })
  })
  it('挂载时加载真实店铺与等级，创建表单按需加载类目', async () => {
    const wrapper = mountList()
    await flushPromises()
    expect(mocks.mallShopLists).toHaveBeenCalledWith({ page: 1, size: 15 })
    expect(mocks.shopLevelList).toHaveBeenCalledOnce()
    expect(mocks.mallShopProvisionOptions).not.toHaveBeenCalled()
    wrapper.vm.handleAdd()
    await flushPromises()
    expect(mocks.mallShopProvisionOptions).toHaveBeenCalledOnce()
    expect(wrapper.vm.createVisible).toBe(true)
    wrapper.unmount()
  })
  it('保留图示列及操作，不添加删除或账号管理，待补图页签禁用', async () => {
    const wrapper = mountList()
    await flushPromises()
    expect(wrapper.html()).toContain('店主账号：')
    expect(wrapper.html()).toContain('店铺曾用名')
    expect(wrapper.html()).toContain('审核状态')
    expect(wrapper.html()).toContain('创建店铺')
    expect(wrapper.html()).not.toContain('账号管理')
    expect(wrapper.html()).not.toContain('type="selection"')
    expect(wrapper.findAll('.store-tab:disabled')).toHaveLength(4)
    wrapper.unmount()
  })
  it('五项筛选在服务端分页前生效，查询回第一页，重置清空条件', async () => {
    const wrapper = mountList()
    await flushPromises()
    Object.assign(wrapper.vm.queryParams, { ownerAccount: ' 138 ', name: ' 测试 ', entryType: 2, shop_status: 0, shopLevelId: 3 })
    wrapper.vm.pagination.pageNo = 4
    wrapper.vm.handleSearch()
    await flushPromises()
    expect(mocks.mallShopLists).toHaveBeenLastCalledWith({ page: 1, size: 15, ownerAccount: '138', shopName: '测试', entryType: 2, status: 0, shopLevelId: 3 })
    wrapper.vm.handleReset()
    await flushPromises()
    expect(mocks.mallShopLists).toHaveBeenLastCalledWith({ page: 1, size: 15 })
    wrapper.unmount()
  })
  it('查看进入只读模式，编辑与修改共用编辑入口', async () => {
    const wrapper = mountList()
    await flushPromises()
    wrapper.vm.handleView({ id: 9 })
    expect(mocks.push).toHaveBeenLastCalledWith({ path: '/mall/store/edit', query: { id: 9, mode: 'view' } })
    wrapper.vm.handleEdit({ id: 9 })
    expect(mocks.push).toHaveBeenLastCalledWith({ path: '/mall/store/edit', query: { id: 9 } })
    wrapper.unmount()
  })
  it('失败清空数据和总数并显示真实错误', async () => {
    const wrapper = mountList()
    await flushPromises()
    mocks.mallShopLists.mockResolvedValueOnce({ code: 0, message: '无店铺权限' })
    await wrapper.vm.fetchList()
    expect(wrapper.vm.list).toEqual([])
    expect(wrapper.vm.pagination.total).toBe(0)
    expect(wrapper.vm.listError).toBe('无店铺权限')
    wrapper.unmount()
  })
  it('快速重复查询不会被更早响应覆盖', async () => {
    const wrapper = mountList()
    await flushPromises()
    let resolveOld
    mocks.mallShopLists.mockImplementationOnce(() => new Promise((resolve) => { resolveOld = resolve }))
    const oldRequest = wrapper.vm.fetchList()
    mocks.mallShopLists.mockResolvedValueOnce({ code: 1, data: { lists: [{ id: 2, name: '新结果' }], count: 1 } })
    await wrapper.vm.fetchList()
    resolveOld({ code: 1, data: { lists: [{ id: 3, name: '旧结果' }], count: 9 } })
    await oldRequest
    expect(wrapper.vm.list[0].name).toBe('新结果')
    expect(wrapper.vm.pagination.total).toBe(1)
    wrapper.unmount()
  })
  it('导出沿用筛选并遍历全部分页，不只导出当前页', async () => {
    const wrapper = mountList()
    await flushPromises()
    wrapper.vm.queryParams.name = '测试'
    const createUrl = vi.fn(() => 'blob:test')
    const oldCreate = URL.createObjectURL
    const oldRevoke = URL.revokeObjectURL
    URL.createObjectURL = createUrl
    URL.revokeObjectURL = vi.fn()
    const click = vi.spyOn(HTMLAnchorElement.prototype, 'click').mockImplementation(() => {})
    mocks.mallShopLists.mockResolvedValueOnce({ code: 1, data: { lists: [{ id: 1 }], count: 2 } })
      .mockResolvedValueOnce({ code: 1, data: { lists: [{ id: 2 }], count: 2 } })
    await wrapper.vm.handleExport()
    expect(mocks.mallShopLists).toHaveBeenNthCalledWith(2, { page: 1, size: 100, shopName: '测试' })
    expect(mocks.mallShopLists).toHaveBeenNthCalledWith(3, { page: 2, size: 100, shopName: '测试' })
    expect(createUrl).toHaveBeenCalledOnce()
    expect(click).toHaveBeenCalledOnce()
    click.mockRestore()
    URL.createObjectURL = oldCreate
    URL.revokeObjectURL = oldRevoke
    wrapper.unmount()
  })
  it('分页缺失时导出失败，不下载部分数据', async () => {
    const wrapper = mountList()
    await flushPromises()
    mocks.mallShopLists.mockResolvedValueOnce({ code: 1, data: { lists: [{ id: 1 }], count: 2 } })
      .mockResolvedValueOnce({ code: 1, data: { lists: [], count: 2 } })
    await wrapper.vm.handleExport()
    expect(mocks.error).toHaveBeenLastCalledWith('结果过多或分页数据不完整，请缩小筛选范围后导出')
    expect(wrapper.vm.exporting).toBe(false)
    wrapper.unmount()
  })
})
