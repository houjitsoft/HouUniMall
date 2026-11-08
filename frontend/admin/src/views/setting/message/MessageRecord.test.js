// @vitest-environment jsdom
import { flushPromises, mount } from '@vue/test-utils'
import { beforeEach, describe, expect, it, vi } from 'vitest'

const mocks = vi.hoisted(() => ({
  route: { query: { bizType: 'PM' } },
  recordApi: {
    getList: vi.fn(),
    getStatistics: vi.fn(),
    getById: vi.fn(),
    resend: vi.fn()
  }
}))

vi.mock('vue-router', () => ({ useRoute: () => mocks.route }))
vi.mock('@/api/setting/message', () => ({ recordApi: mocks.recordApi }))
vi.mock('element-plus', () => ({
  ElMessage: { success: vi.fn(), error: vi.fn() }
}))
vi.mock('@element-plus/icons-vue', () => {
  const icon = { template: '<span />' }
  return { Search: icon, List: icon, Message: icon, RefreshRight: icon }
})

import MessageRecord from './MessageRecord.vue'

const passthrough = { template: '<div v-bind="$attrs"><slot /></div>' }
const mountPage = () => mount(MessageRecord, {
  global: {
    stubs: {
      'el-tabs': passthrough,
      'el-tab-pane': passthrough,
      'el-row': passthrough,
      'el-col': passthrough,
      'el-card': passthrough,
      'el-select': true,
      'el-option': true,
      'el-input': true,
      'el-date-picker': true,
      'el-button': true,
      'el-icon': passthrough,
      'el-table': true,
      'el-table-column': true,
      'el-tag': passthrough,
      'el-pagination': true,
      'el-dialog': passthrough,
      'el-descriptions': passthrough,
      'el-descriptions-item': passthrough,
      'el-alert': true,
      'el-empty': true
    }
  }
})

describe('MessageRecord', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    mocks.route.query = { bizType: 'PM' }
    mocks.recordApi.getList.mockResolvedValue({
      code: 1,
      data: {
        list: [{ id: 91, sendStatus: 3, eventType: 'TASK_ASSIGNED', bizId: '42' }],
        total: 1,
        page: 1,
        size: 20
      }
    })
    mocks.recordApi.getStatistics.mockResolvedValue({
      code: 1,
      data: { total: 1, success: 0, failed: 1, pending: 0 }
    })
    mocks.recordApi.getById.mockResolvedValue({
      code: 1,
      data: {
        id: 91,
        eventType: 'TASK_ASSIGNED',
        bizType: 'PM_TASK',
        bizId: '42',
        idempotencyKeyDigest: 'a31f9c20',
        receiverContact: '13****00',
        errorCode: 'TIMEOUT',
        failReason: '连接超时',
        retryCount: 2,
        sendStatus: 3
      }
    })
    mocks.recordApi.resend.mockResolvedValue({ code: 1 })
  })

  it('hydrates the PM preset and reuses one complete filter for list and statistics', async () => {
    const wrapper = mountPage()
    await flushPromises()

    expect(mocks.recordApi.getList).toHaveBeenCalledWith(expect.objectContaining({
      bizType: 'PM', page: 1, size: 20
    }))
    expect(mocks.recordApi.getStatistics).toHaveBeenCalledWith(expect.objectContaining({
      bizType: 'PM'
    }))

    wrapper.vm.searchForm.eventType = 'TASK_ASSIGNED'
    wrapper.vm.searchForm.sendStatus = 3
    wrapper.vm.searchForm.receiver = '李'
    wrapper.vm.searchForm.dateRange = [new Date('2026-07-24T00:00:00Z'), new Date('2026-07-24T23:59:59Z')]
    wrapper.vm.activeChannel = 'INBOX'
    await wrapper.vm.refreshRecordData()

    const filter = expect.objectContaining({
      bizType: 'PM',
      eventType: 'TASK_ASSIGNED',
      channel: 'INBOX',
      sendStatus: 3,
      receiver: '李',
      startTime: 1784851200,
      endTime: 1784937599
    })
    expect(mocks.recordApi.getList).toHaveBeenLastCalledWith(filter)
    expect(mocks.recordApi.getStatistics).toHaveBeenLastCalledWith(filter)
  })

  it('loads safe diagnostics and refreshes records after a real retry', async () => {
    const wrapper = mountPage()
    await flushPromises()

    await wrapper.vm.handleDetail({ id: 91 })
    await flushPromises()

    expect(mocks.recordApi.getById).toHaveBeenCalledWith(91)
    expect(wrapper.text()).toContain('a31f9c20')
    expect(wrapper.text()).toContain('TIMEOUT')
    expect(wrapper.text()).not.toContain('13****00')

    await wrapper.vm.handleRetry({ id: 91, sendStatus: 3 })
    await flushPromises()

    expect(mocks.recordApi.resend).toHaveBeenCalledWith(91)
    expect(mocks.recordApi.getList).toHaveBeenCalledTimes(2)
    expect(mocks.recordApi.getStatistics).toHaveBeenCalledTimes(2)
  })
})
