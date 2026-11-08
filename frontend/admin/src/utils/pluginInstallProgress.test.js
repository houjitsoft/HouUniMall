import { describe, expect, it } from 'vitest'
import { describeInstallProgress } from './pluginInstallProgress'

describe('describeInstallProgress', () => {
  it('maps HEALTH success', () => {
    const info = describeInstallProgress({ step: 'HEALTH', state: 'SUCCESS' })
    expect(info.percent).toBe(100)
    expect(info.healthOk).toBe(true)
  })
  it('keeps mid progress for DOWNLOAD', () => {
    const info = describeInstallProgress({ step: 'DOWNLOAD', state: 'RUNNING' })
    expect(info.label).toContain('下载')
    expect(info.percent).toBeGreaterThan(0)
    expect(info.percent).toBeLessThan(100)
  })
})
