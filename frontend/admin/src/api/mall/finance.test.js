import { beforeEach, describe, expect, it, vi } from 'vitest'

const request = vi.hoisted(() => ({
  get: vi.fn(),
  post: vi.fn()
}))

vi.mock('@/utils/request', () => ({ default: request }))

import {
  getRefundList,
  getRefundStats,
  getShopBalanceList,
  getShopSettlementRecords,
  getUserBalanceList,
  getUserDepositList,
  retryRefund
} from './finance'

describe('商城平台财务 API', () => {
  beforeEach(() => vi.clearAllMocks())

  it('所有平台财务请求都经商城平台接口，不再直连商家后台', async () => {
    await getRefundStats({ status: 0 })
    await getRefundList({ page_no: 1 })
    await retryRefund({ id: 12 })
    await getShopBalanceList({ page_no: 2 })
    await getShopSettlementRecords({ page: 3 })
    await getUserBalanceList({ account_type: 'balance' })
    await getUserDepositList({ pay_status: 1 })

    const urls = [...request.get.mock.calls, ...request.post.mock.calls].map(([url]) => url)
    expect(urls).not.toContainEqual(expect.stringContaining('/web/merchant/'))
    expect(urls).toEqual(expect.arrayContaining([
      '/web/mall/finance/refund/stats',
      '/web/mall/finance/refund/list',
      '/web/mall/finance/refund/retry',
      '/web/mall/finance/shop/balance/list',
      '/web/mall/finance/settlement/list',
      '/web/mall/finance/user/wallet/list',
      '/web/mall/finance/user/recharge/list'
    ]))
  })
})
