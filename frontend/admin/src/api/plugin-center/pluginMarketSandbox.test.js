import { beforeEach, describe, expect, it, vi } from 'vitest'

const request = { post: vi.fn() }

vi.mock('@/utils/request', () => ({ default: request }))

describe('插件市场沙箱支付 API', () => {
  beforeEach(() => {
    request.post.mockReset()
  })

  it('提交模拟支付成功并返回后端数据', async () => {
    request.post.mockResolvedValueOnce({ data: { orderNo: 'PO-1', result: 'SUCCESS' } })
    const { confirmSandboxPluginPayment } = await import('./pluginMarket')

    await expect(confirmSandboxPluginPayment('PO-1')).resolves.toEqual({ orderNo: 'PO-1', result: 'SUCCESS' })
    expect(request.post).toHaveBeenCalledWith('/web/plugin-center/market/pay/sandbox/confirm', {
      orderNo: 'PO-1',
      result: 'SUCCESS'
    })
  })
})
