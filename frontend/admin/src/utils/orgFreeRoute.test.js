import { describe, expect, it } from 'vitest'
import { isOrgFreeRoute } from './orgFreeRoute.js'

describe('isOrgFreeRoute', () => {
  it('hides org switcher on mall marketing plugins', () => {
    expect(isOrgFreeRoute('/plugin/order-pintuan')).toBe(true)
    expect(isOrgFreeRoute('/plugin/order-pintuan/index')).toBe(true)
    expect(isOrgFreeRoute('/plugin/order-seckill')).toBe(true)
    expect(isOrgFreeRoute('/plugin/goods-discount')).toBe(true)
    expect(isOrgFreeRoute('/plugin/mall-live/activity')).toBe(true)
    expect(isOrgFreeRoute('/plugin/national-subsidy/orders')).toBe(true)
  })

  it('does not treat orderfood as order-* family', () => {
    expect(isOrgFreeRoute('/plugin/orderfood')).toBe(true)
    expect(isOrgFreeRoute('/plugin/order')).toBe(false)
  })

  it('keeps org switcher on org-scoped modules', () => {
    expect(isOrgFreeRoute('/plugin/oa')).toBe(false)
    expect(isOrgFreeRoute('/plugin/crm')).toBe(false)
    expect(isOrgFreeRoute('/plugin/geo')).toBe(false)
    expect(isOrgFreeRoute('/setting')).toBe(false)
    expect(isOrgFreeRoute('/project')).toBe(false)
  })
})
