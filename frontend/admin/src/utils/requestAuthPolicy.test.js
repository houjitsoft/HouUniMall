import { beforeEach, describe, expect, it, vi } from 'vitest'

vi.mock('element-plus', () => {
  const ElMessage = vi.fn()
  ElMessage.error = vi.fn()
  return { ElMessage }
})

import request from './request'
import { CATEGORY_TEXT } from './errorCode/categoryFallback'

function createStorage() {
  const values = new Map()
  return {
    getItem: (key) => values.get(key) ?? null,
    setItem: (key, value) => values.set(key, String(value)),
    removeItem: (key) => values.delete(key)
  }
}

function createResponse(config, status, data) {
  return {
    data,
    status,
    statusText: status === 200 ? 'OK' : 'Forbidden',
    headers: {},
    config,
    request: {}
  }
}

describe('request authentication error policy', () => {
  beforeEach(() => {
    vi.stubGlobal('localStorage', createStorage())
    vi.stubGlobal('sessionStorage', createStorage())
    vi.stubGlobal('window', {
      location: { origin: 'http://localhost', pathname: '/', href: '' }
    })
    localStorage.setItem('token', 'token-that-must-survive')
  })

  it('does not clear the session for a business 403 permission response', async () => {
    await expect(request.get('/permission-denied', {
      adapter: async (config) => createResponse(config, 200, {
        code: 403,
        message: '权限不足'
      })
    })).rejects.toThrow(CATEGORY_TEXT.PERMISSION)

    expect(localStorage.getItem('token')).toBe('token-that-must-survive')
    expect(window.location.href).toBe('')
  })

  it('does not clear the session for an HTTP 403 response without an auth message', async () => {
    await expect(request.get('/forbidden', {
      adapter: async (config) => createResponse(config, 403, {
        code: 403,
        message: 'Forbidden'
      })
    })).rejects.toBeTruthy()

    expect(localStorage.getItem('token')).toBe('token-that-must-survive')
    expect(window.location.href).toBe('')
  })
})
