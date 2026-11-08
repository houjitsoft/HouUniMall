// @vitest-environment jsdom
import { beforeEach, describe, expect, it, vi } from 'vitest'
const request = vi.hoisted(() => ({ post: vi.fn(), getUri: vi.fn(({ url }) => `/api${url}`) }))
vi.mock('@/utils/request', () => ({ default: request }))
import { installServiceAndWait, downloadServiceArtifact } from './service'
beforeEach(() => vi.clearAllMocks())
describe('独立服务购买交付', () => {
  it('只有代理成功回执才返回安装成功', async () => {
    request.post.mockResolvedValueOnce({ data: { taskId: 'task' } }).mockResolvedValueOnce({ data: { state: 'SUCCESS' } })
    expect((await installServiceAndWait('erp', '1.0.0')).finalStatus.state).toBe('SUCCESS')
    expect(request.post.mock.calls[0]).toEqual(['/web/plugin-center/service/install', { pluginCode: 'erp', version: '1.0.0' }])
  })
  it('回滚意味着本次安装失败', async () => {
    request.post.mockResolvedValueOnce({ taskId: 'task' }).mockResolvedValueOnce({ state: 'ROLLED_BACK', result: '原版本已恢复' })
    await expect(installServiceAndWait('erp', '1.0.0')).rejects.toThrow('原版本已恢复')
  })
  it('缺少任务编号不能显示成功', async () => {
    request.post.mockResolvedValueOnce({})
    await expect(installServiceAndWait('erp', '1.0.0')).rejects.toThrow('任务编号')
  })
  it('只请求用户选择的源码包并使用实际 API 前缀下载', async () => {
    request.post.mockResolvedValueOnce({ data: { source: { url: '/web/plugin-center/artifact/raw?token=t', fileName: 'erp-source.tar.gz' } } })
    const click = vi.spyOn(HTMLAnchorElement.prototype, 'click').mockImplementation(() => {})
    await downloadServiceArtifact('erp', '1.0.0', 'source')
    expect(request.post).toHaveBeenCalledWith('/web/plugin-center/service/delivery', { pluginCode: 'erp', version: '1.0.0', artifactKind: 'source' })
    expect(request.getUri).toHaveBeenCalledWith({ url: '/web/plugin-center/artifact/raw?token=t' })
    expect(click).toHaveBeenCalledOnce(); click.mockRestore()
  })
  it('优先使用同站 raw，避免内网地址并记录实际交付' , async () => {
    request.post.mockResolvedValueOnce({
      data: {
        source: {
          url: '/web/plugin-center/artifact/raw?token=t',
          cloudUrl: 'http://127.0.0.1:19000/unimall-plugins/a?X-Amz-Signature=1',
          fileName: 'erp-source.tar.gz',
        },
      },
    })
    const click = vi.spyOn(HTMLAnchorElement.prototype, 'click').mockImplementation(() => {})
    await downloadServiceArtifact('erp', '1.0.0', 'source')
    expect(request.getUri).toHaveBeenCalledWith({ url: '/web/plugin-center/artifact/raw?token=t' })
    expect(click).toHaveBeenCalledOnce()
    click.mockRestore()
  })
  it('拒绝仅含不受信任外链且无相对 raw 的响应', async () => {
    request.post.mockResolvedValueOnce({ source: { url: 'https://invalid.example/archive' } })
    await expect(downloadServiceArtifact('erp', '1.0.0', 'source')).rejects.toThrow('下载地址无效')
  })
})
