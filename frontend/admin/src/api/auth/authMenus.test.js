import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'

vi.mock('@/utils/request', () => ({ default: { post: vi.fn() } }))
vi.mock('@/utils/auth.js', () => ({ getToken: vi.fn(), setAuthInfo: vi.fn() }))

import request from '@/utils/request'
import { getToken } from '@/utils/auth'
import { getUserMenus } from './auth'
import { readUserMenusCache, writeUserMenusCache } from '@/utils/userMenusCache'

const storage = () => {
  const data = new Map()
  return { getItem: key => data.get(key) ?? null, setItem: (key, value) => data.set(key, String(value)), removeItem: key => data.delete(key) }
}

describe('user menu response cache ownership', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    getToken.mockReturnValue('account-a')
    vi.stubGlobal('localStorage', storage())
    vi.stubGlobal('sessionStorage', storage())
  })
  afterEach(() => vi.unstubAllGlobals())

  it('removes cached grants when the server successfully returns an empty menu list', async () => {
    writeUserMenusCache({}, [{ id: 20, paths: '/plugin/erp' }])
    request.post.mockResolvedValue({ code: 1, data: [] })

    await getUserMenus({}, { forceRefresh: true })

    expect(readUserMenusCache({})).toBeNull()
  })

  it('does not save a previous account response under the new account identity', async () => {
    let resolve
    request.post.mockReturnValue(new Promise(done => { resolve = done }))
    const previous = getUserMenus({}, { forceRefresh: true })
    getToken.mockReturnValue('account-b')
    writeUserMenusCache({}, [{ id: 30, paths: '/plugin/oa' }])

    resolve({ code: 1, data: [{ id: 20, paths: '/plugin/erp' }] })
    await previous

    expect(readUserMenusCache({})).toEqual([{ id: 30, paths: '/plugin/oa' }])
  })

  it('does not treat a malformed response as successful permission revocation', async () => {
    writeUserMenusCache({}, [{ id: 20, paths: '/plugin/erp' }])
    request.post.mockResolvedValue({ code: 1, data: {} })

    await getUserMenus({}, { forceRefresh: true })

    expect(readUserMenusCache({})).toEqual([{ id: 20, paths: '/plugin/erp' }])
  })

  it('does not return new account cached menus to an old failed request', async () => {
    let reject
    request.post.mockReturnValue(new Promise((_, fail) => { reject = fail }))
    const previous = getUserMenus({}, { forceRefresh: true })
    getToken.mockReturnValue('account-b')
    writeUserMenusCache({}, [{ id: 30, paths: '/plugin/oa' }])

    reject(new Error('previous request failed'))
    const response = await previous

    expect(response.code).toBe(0)
    expect(response.data).toBeUndefined()
    expect(readUserMenusCache({})).toEqual([{ id: 30, paths: '/plugin/oa' }])
  })
})
