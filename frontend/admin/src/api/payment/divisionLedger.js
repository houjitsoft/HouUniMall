/**
 * 分账台账：BFF /web/pay/division/ledger → payment /api/payment/division/ledger
 */
import request from '@/utils/request'

export const divisionLedgerApi = {
  page: (params) => request.post('/web/pay/division/ledger/page', params || {}),
  query: (params) => request.post('/web/pay/division/ledger/query', params || {}),
  create: (params) => request.post('/web/pay/division/ledger/create', params)
}

export default divisionLedgerApi
