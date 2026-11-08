import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'

const request = vi.hoisted(() => ({
  get: vi.fn(),
  post: vi.fn(),
  patch: vi.fn()
}))

vi.mock('@/utils/request', () => ({ default: request }))

import { fileRecognizeApi } from './file-recognize'

describe('智能入库目录 API', () => {
  beforeEach(() => {
    vi.resetAllMocks()
    vi.useFakeTimers()
  })

  afterEach(() => vi.useRealTimers())

  it('部署窗口持续 35 秒返回 502 时自动等待并重试目录请求', async () => {
    for (let i = 0; i < 8; i += 1) {
      request.get.mockRejectedValueOnce({ response: { status: 502 } })
    }
    request.get.mockResolvedValueOnce({ code: 1, data: { systems: [{ key: 'mall' }] } })

    const responsePromise = fileRecognizeApi.getImportCatalog()
    await vi.advanceTimersByTimeAsync(35000)

    await expect(responsePromise).resolves.toMatchObject({ code: 1 })
    expect(request.get).toHaveBeenCalledTimes(9)
    expect(request.get).toHaveBeenNthCalledWith(1, '/web/common/file/import/catalog', {
      skipErrorMessage: true
    })
  })

  it('权限错误不重试，保留原错误供页面提示', async () => {
    const permissionError = { response: { status: 401 } }
    request.get.mockRejectedValueOnce(permissionError)

    await expect(fileRecognizeApi.getImportCatalog()).rejects.toBe(permissionError)
    expect(request.get).toHaveBeenCalledTimes(1)
  })
})
