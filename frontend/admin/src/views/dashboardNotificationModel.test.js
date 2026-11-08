import { describe, expect, it } from 'vitest'

import {
  normalizeNotification,
  normalizeNotificationPage,
  resolveNotificationRoute
} from './dashboardNotificationModel'

describe('dashboard notification model', () => {
  it('normalizes inbox records and pagination', () => {
    expect(normalizeNotification({
      id: 91,
      title: '',
      content: null,
      isRead: '1',
      bizType: 'PM_TASK',
      bizId: 42,
      createTime: '1784890000'
    })).toEqual({
      id: '91',
      title: '未命名消息',
      content: '',
      read: true,
      bizType: 'PM_TASK',
      bizId: '42',
      createTime: 1784890000
    })

    expect(normalizeNotificationPage({
      data: { list: [{ id: 91, title: '任务通知' }], total: 21, page: 1, size: 20 }
    })).toEqual(expect.objectContaining({ page: 1, size: 20, total: 21 }))
  })

  it.each([
    [{ bizType: 'PM_TASK', bizId: '42' }, '/project/task/detail/42'],
    [{ bizType: 'PM_PROJECT', bizId: '9' }, '/project/list?projectId=9'],
    [{ bizType: 'OTHER', bizId: '9' }, null],
    [{ bizType: 'PM_TASK', bizId: '' }, null]
  ])('maps notification business target', (message, expected) => {
    expect(resolveNotificationRoute(message)).toBe(expected)
  })
})
