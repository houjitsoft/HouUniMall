import { beforeEach, describe, expect, it, vi } from 'vitest'
import { createPinia, setActivePinia } from 'pinia'

vi.mock('@/utils/auth', () => ({
  getToken: () => 'valid-token',
  setAuthInfo: vi.fn(),
  clearAuthInfo: vi.fn(),
  getUserId: vi.fn(),
  normalizeUserIdInUserInfo: vi.fn()
}))
vi.mock('@/api/auth/auth.js', () => ({ authService: {} }))
vi.mock('@/stores/app', () => ({ useAppStore: vi.fn() }))
vi.mock('@/utils/userMenusCache', () => ({
  invalidateUserMenusCache: vi.fn(),
  refreshPermissionsFromServer: vi.fn()
}))

import { useAuthStore } from './auth'

function createStorage() {
  const values = new Map()
  return {
    getItem: (key) => values.get(key) ?? null,
    setItem: (key, value) => values.set(key, String(value)),
    removeItem: (key) => values.delete(key)
  }
}

describe('auth organization restore', () => {
  beforeEach(() => {
    vi.stubGlobal('localStorage', createStorage())
    vi.stubGlobal('sessionStorage', createStorage())
    setActivePinia(createPinia())
  })

  it('preserves the selected organization after reloading when it still belongs to the user', () => {
    localStorage.setItem('currentOrgId', '2')
    localStorage.setItem('userInfo', JSON.stringify({
      userId: '640697115455213568',
      orgList: [
        { org_id: 1, is_primary: 1 },
        { org_id: 2, is_primary: 0 }
      ]
    }))

    useAuthStore().initAuth()

    expect(localStorage.getItem('currentOrgId')).toBe('2')
  })
})
