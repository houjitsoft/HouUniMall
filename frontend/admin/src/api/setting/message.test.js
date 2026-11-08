import { beforeEach, describe, expect, it, vi } from 'vitest'

const request = vi.hoisted(() => ({
  get: vi.fn(),
  post: vi.fn(),
  delete: vi.fn()
}))

vi.mock('@/utils/request', () => ({ default: request }))

import { inboxApi, recordApi } from './message'

describe('inboxApi', () => {
  beforeEach(() => vi.clearAllMocks())

  it('uses current-user inbox routes without identity parameters', async () => {
    await inboxApi.getList({ page: 1, size: 20, isRead: 0, userId: 501 })
    await inboxApi.getUnreadCount(501)
    await inboxApi.markAsRead(91)
    await inboxApi.markAllAsRead(501)

    expect(request.get).toHaveBeenNthCalledWith(1, '/web/message/inbox/list', {
      params: { page: 1, size: 20, isRead: 0 }
    })
    expect(request.get).toHaveBeenNthCalledWith(2, '/web/message/inbox/unread-count')
    expect(request.post).toHaveBeenNthCalledWith(1, '/web/message/inbox/91/read')
    expect(request.post).toHaveBeenNthCalledWith(2, '/web/message/inbox/read-all')
  })
})

describe('recordApi', () => {
  beforeEach(() => vi.clearAllMocks())

  it('passes the complete delivery filter to list and statistics', async () => {
    const filter = {
      page: 2,
      size: 50,
      bizType: 'PM',
      eventType: 'TASK_ASSIGNED',
      channel: 'INBOX',
      sendStatus: 3,
      receiver: '李',
      startTime: 1784800000,
      endTime: 1784890000,
      ignored: 'value'
    }

    await recordApi.getList(filter)
    await recordApi.getStatistics(filter)

    expect(request.get).toHaveBeenNthCalledWith(1, '/web/message/record/list', {
      params: {
        page: 2,
        size: 50,
        bizType: 'PM',
        eventType: 'TASK_ASSIGNED',
        channel: 'INBOX',
        sendStatus: 3,
        receiver: '李',
        startTime: 1784800000,
        endTime: 1784890000
      }
    })
    expect(request.get).toHaveBeenNthCalledWith(2, '/web/message/record/statistics', {
      params: {
        bizType: 'PM',
        eventType: 'TASK_ASSIGNED',
        channel: 'INBOX',
        sendStatus: 3,
        receiver: '李',
        startTime: 1784800000,
        endTime: 1784890000
      }
    })
  })
})
