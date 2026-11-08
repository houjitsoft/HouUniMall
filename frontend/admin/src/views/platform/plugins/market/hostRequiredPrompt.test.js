// @vitest-environment jsdom
import { describe, it, expect, vi, beforeEach } from 'vitest'
import { ElMessageBox } from 'element-plus'
import {
  hostUnavailable,
  hostHintText,
  resolveHostMeta,
  promptHostRequired
} from './hostRequiredPrompt'

vi.mock('element-plus', async () => {
  const actual = await vi.importActual('element-plus')
  return {
    ...actual,
    ElMessageBox: {
      confirm: vi.fn()
    }
  }
})

describe('hostRequiredPrompt', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  it('hostUnavailable 仅在 hostAvailable === false 时为真', () => {
    expect(hostUnavailable({ hostAvailable: false })).toBe(true)
    expect(hostUnavailable({ hostAvailable: true })).toBe(false)
    expect(hostUnavailable({ hostAvailable: null })).toBe(false)
    expect(hostUnavailable({})).toBe(false)
  })

  it('resolveHostMeta 按 hostService code 匹配目录项展示名', () => {
    const catalog = [{ code: 'pm', name: '项目管理', licenseType: 'PAID', licensed: false }]
    const meta = resolveHostMeta({ hostService: 'pm', name: 'AI 编码' }, catalog)
    expect(meta.hostCode).toBe('pm')
    expect(meta.hostName).toBe('项目管理')
    expect(meta.hostListing.code).toBe('pm')
  })

  it('hostHintText 有宿主名时提示先安装宿主名', () => {
    const catalog = [{ code: 'pm', name: '项目管理' }]
    expect(hostHintText({ hostService: 'pm' }, catalog)).toBe('需先安装「项目管理」')
    expect(hostHintText({ hostService: 'orderfood' }, [])).toBe('需 orderfood 服务')
  })

  it('promptHostRequired 确认后回调 onGoHost', async () => {
    ElMessageBox.confirm.mockResolvedValueOnce('confirm')
    const onGoHost = vi.fn()
    const ok = await promptHostRequired(
      { code: 'aicode', name: 'AI 编码', hostService: 'pm', hostAvailable: false },
      { catalog: [{ code: 'pm', name: '项目管理' }], onGoHost }
    )
    expect(ok).toBe(true)
    expect(onGoHost).toHaveBeenCalledWith(expect.objectContaining({
      hostCode: 'pm',
      hostName: '项目管理'
    }))
    expect(ElMessageBox.confirm.mock.calls[0][1]).toBe('需要先购买/安装宿主插件')
    expect(String(ElMessageBox.confirm.mock.calls[0][0])).toContain('项目管理')
  })

  it('promptHostRequired 取消不回调', async () => {
    ElMessageBox.confirm.mockRejectedValueOnce('cancel')
    const onGoHost = vi.fn()
    const ok = await promptHostRequired(
      { code: 'aicode', hostService: 'pm', hostAvailable: false },
      { onGoHost }
    )
    expect(ok).toBe(false)
    expect(onGoHost).not.toHaveBeenCalled()
  })
})
