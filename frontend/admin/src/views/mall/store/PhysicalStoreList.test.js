// @vitest-environment jsdom
import { flushPromises, shallowMount } from '@vue/test-utils'
import { beforeEach, describe, expect, it, vi } from 'vitest'

const mocks = vi.hoisted(() => ({
  list: vi.fn(),
  options: vi.fn(),
  accounts: vi.fn(),
  create: vi.fn(),
  toggle: vi.fn()
}))

vi.mock('@/api/mall/physicalStore', () => ({
  physicalStoreList: mocks.list,
  physicalStoreMerchantOptions: mocks.options,
  physicalStoreAccountOptions: mocks.accounts,
  physicalStoreCreate: mocks.create,
  physicalStoreToggle: mocks.toggle
}))
vi.mock('element-plus', () => ({ ElMessage: { success: vi.fn(), error: vi.fn() } }))

import PhysicalStoreList from './PhysicalStoreList.vue'

describe('商城门店列表', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    mocks.list.mockResolvedValue({
      code: 1,
      data: {
        records: [{ id: 1, storeName: 'Nike 默认门店', merchantName: 'Nike', posEnabled: 0, riderDeliveryEnabled: 0 }],
        total: 1
      }
    })
    mocks.options.mockResolvedValue({ code: 1, data: [{ id: 1, name: 'Nike' }] })
  })

  it('首屏分别加载门店列表和商家选项', async () => {
    const wrapper = shallowMount(PhysicalStoreList, {
      global: {
        directives: { loading: {} },
        stubs: {
          'el-table': true,
          'el-dialog': true
        }
      }
    })
    await flushPromises()

    expect(mocks.list).toHaveBeenCalledWith({ page: 1, size: 15 })
    expect(mocks.options).toHaveBeenCalledOnce()
    expect(wrapper.text()).toContain('门店列表')
    expect(wrapper.text()).toContain('新增门店')
  })
})
