import { describe, expect, it } from 'vitest'
import { formatInstallFailure, formatMaintenanceCountdown, explainArtifactError } from './pluginInstallFeedback'

describe('formatInstallFailure', () => {
  it('优先展示后端 errorMsg 并带阶段提示', () => {
    expect(formatInstallFailure({ state: 'FAILED', step: 'HEALTH', errorMsg: 'nacos 未注册' }))
      .toContain('HEALTH')
    expect(formatInstallFailure({ state: 'FAILED', step: 'HEALTH', errorMsg: 'nacos 未注册' }))
      .toContain('nacos 未注册')
  })
  it('回滚态可读', () => {
    expect(formatInstallFailure({ state: 'ROLLED_BACK', step: 'ACTIVATE', errorMsg: '切换失败' }))
      .toMatch(/回滚/)
  })
  it('不把失败归因为「未上架」（商品上架状态与安装失败无因果关系）', () => {
    const out = formatInstallFailure({ state: 'FAILED', errorMsg: '连接超时' })
    expect(out).not.toContain('未上架')
  })
})

describe('explainArtifactError', () => {
  it('3002 说明是沙箱限制，并明确与商品上架无关', () => {
    const msg = explainArtifactError(3002, '只有正式购买对应产品后才能获取源码和服务制品')
    expect(msg).toContain('沙箱')
    // 用户看到这句就不该再去怀疑「商品没上架」或「我没买成功」
    expect(msg).not.toContain('未上架')
    expect(msg).toMatch(/无关|不相关|不是/)
  })
  it('3003 指向维护期到期', () => {
    expect(explainArtifactError(3003, '')).toMatch(/维护期/)
  })
  it('3006/3009 指向发布状态问题', () => {
    expect(explainArtifactError(3006, '')).toMatch(/未发布|下架/)
    expect(explainArtifactError(3009, '')).toMatch(/发行清单/)
  })
  it('未知错误码回显后端原文', () => {
    expect(explainArtifactError(99999, '磁盘满')).toBe('磁盘满')
  })
  it('无错误码也无消息时给出兜底', () => {
    expect(explainArtifactError(null, '')).toMatch(/制品获取失败/)
  })
})

describe('formatMaintenanceCountdown', () => {
  it('无维护期', () => {
    expect(formatMaintenanceCountdown(null).level).toBe('none')
  })
  it('剩余天数分级', () => {
    const in10 = new Date(Date.now() + 10 * 86400000).toISOString()
    expect(formatMaintenanceCountdown(in10).level).toBe('warn')
    const in90 = new Date(Date.now() + 90 * 86400000).toISOString()
    expect(formatMaintenanceCountdown(in90).level).toBe('ok')
  })
})
