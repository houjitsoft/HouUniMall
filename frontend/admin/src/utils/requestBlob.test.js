import { beforeEach, describe, expect, it, vi } from 'vitest'

vi.mock('element-plus', () => ({ ElMessage: vi.fn() }))

import request from './request'

const createStorage = () => {
  const values = new Map()
  return {
    getItem: (key) => values.get(key) ?? null,
    setItem: (key, value) => values.set(key, String(value)),
    removeItem: (key) => values.delete(key)
  }
}

describe('request blob responses', () => {
  beforeEach(() => {
    vi.stubGlobal('localStorage', createStorage())
    vi.stubGlobal('sessionStorage', createStorage())
    vi.stubGlobal('window', {
      location: { origin: 'http://localhost', pathname: '/', href: '' }
    })
  })

  it('returns the axios response so callers can read blob headers', async () => {
    const blob = new Blob(['xlsx'], { type: 'application/octet-stream' })
    const response = await request.get('/download', {
      responseType: 'blob',
      adapter: async (config) => ({
        data: blob,
        status: 200,
        statusText: 'OK',
        headers: { 'content-disposition': 'attachment; filename="report.xlsx"' },
        config,
        request: {}
      })
    })

    expect(response.data).toBe(blob)
    expect(response.headers['content-disposition']).toContain('report.xlsx')
  })
})
