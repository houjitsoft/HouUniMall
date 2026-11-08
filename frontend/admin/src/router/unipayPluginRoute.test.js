// @vitest-environment jsdom

import { describe, expect, it } from 'vitest'
import router from './index'

describe('payment service route ownership', () => {
  it('registers payment pages on the payment service and redirects the old plugin address', () => {
    expect(router.hasRoute('PaymentDashboard')).toBe(true)
    expect(router.resolve('/payment/dashboard').name).toBe('PaymentDashboard')
    expect(router.resolve('/payment/recharge').name).toBe('PaymentRecharge')
    expect(router.getRoutes().some((route) => route.path === '/plugin/unipay/:pathMatch(.*)*')).toBe(true)
  })
})
