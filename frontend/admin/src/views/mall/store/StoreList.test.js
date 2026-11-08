// @vitest-environment jsdom
import { flushPromises, shallowMount } from '@vue/test-utils'
import { beforeEach, describe, expect, it, vi } from 'vitest'

const mocks = vi.hoisted(() => ({
  mallShopLists: vi.fn(),
  mallShopProvisionOptions: vi.fn(),
  mallShopProvisionAccountOptions: vi.fn(),
  mallShopProvisionCreate: vi.fn(),
  mallShopStatus: vi.fn(),
  push: vi.fn()
}))

vi.mock('@/api/mall/shopAdmin', () => ({
  mallShopLists: mocks.mallShopLists,
  mallShopProvisionOptions: mocks.mallShopProvisionOptions,
  mallShopProvisionAccountOptions: mocks.mallShopProvisionAccountOptions,
  mallShopProvisionCreate: mocks.mallShopProvisionCreate,
  mallShopStatus: mocks.mallShopStatus
}))
vi.mock('@/utils/request', () => ({ default: { post: vi.fn() } }))
vi.mock('vue-router', () => ({ useRouter: () => ({ push: mocks.push }) }))
vi.mock('element-plus', () => ({
  ElMessage: { success: vi.fn(), error: vi.fn(), info: vi.fn() },
  ElMessageBox: { confirm: vi.fn() }
}))

import StoreList from './StoreList.vue'

describe('商城店铺列表页面', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    mocks.mallShopLists.mockResolvedValue({
      code: 1,
      data: { records: [{ id: 1, shopName: '测试店铺', status: 1 }], total: 1 }
    })
    mocks.mallShopProvisionOptions.mockResolvedValue({
      code: 1,
      data: [{ id: 2, name: '软件服务' }]
    })
    mocks.mallShopProvisionAccountOptions.mockResolvedValue({ code: 1, data: [] })
  })

  it('挂载时通过现行 bff API 加载店铺与经营类目', async () => {
    const wrapper = shallowMount(StoreList, {
      global: {
        stubs: {
          'el-table': true
        },
        directives: {
          loading: {}
        }
      }
    })
    await flushPromises()

    expect(mocks.mallShopLists).toHaveBeenCalledWith({ page: 1, size: 15 })
    expect(mocks.mallShopProvisionOptions).toHaveBeenCalledOnce()

    wrapper.unmount()
  })

  it('展示店铺语义，不把店铺列表误标为门店或商家列表', async () => {
    const wrapper = shallowMount(StoreList, {
      global: {
        stubs: { 'el-table': true },
        directives: { loading: {} }
      }
    })
    await flushPromises()

    expect(wrapper.html()).toContain('店铺名称')
    expect(wrapper.text()).toContain('新增店铺')
    expect(wrapper.text()).not.toContain('门店列表')
    expect(wrapper.text()).not.toContain('新增商家')
    wrapper.unmount()
  })
})
