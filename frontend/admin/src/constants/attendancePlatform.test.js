import { describe, expect, it } from 'vitest'

import { getAttendanceSyncDisabledReason } from './attendancePlatform.js'

describe('getAttendanceSyncDisabledReason', () => {
  it('does not treat an omitted credential status as unconfigured', () => {
    expect(getAttendanceSyncDisabledReason({
      source: 'wechat-work',
      enabled: true,
      credentialConfigured: undefined
    })).toBe('')
  })

  it('explains every state that disables synchronization', () => {
    expect(getAttendanceSyncDisabledReason({ source: 'wechat-work', loading: true }))
      .toBe('正在读取考勤配置，请稍候')
    expect(getAttendanceSyncDisabledReason({ source: 'wechat-work', syncing: true }))
      .toBe('考勤正在同步，请稍候')
    expect(getAttendanceSyncDisabledReason({ source: 'wechat-work', enabled: false }))
      .toBe('企业微信同步已暂停，请到考勤设置启用')
    expect(getAttendanceSyncDisabledReason({
      source: 'wechat-work',
      enabled: true,
      credentialConfigured: false
    })).toBe('企业微信凭证未配置，请到考勤设置或系统设置 → 第三方集成中完成配置')
  })
})
