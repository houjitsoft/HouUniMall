import request from '@/utils/request'

const BASE = '/web/plugin/ums-corp-order'

function post(path, data = {}) {
  return request.post(`${BASE}${path}`, data)
}

export const umsCorpApi = {
  getConfig: (data) => post('/config/get', data),
  saveConfig: (data) => post('/config/save', data),
  orderPage: (data) => post('/order/page', data),
  orderCreate: (data) => post('/order/create', data),
  orderQuery: (data) => post('/order/query', data),
  orderClose: (data) => post('/order/close', data),
  orderRefund: (data) => post('/order/refund', data),
  transfer: (data) => post('/transfer', data),
  split: (data) => post('/split', data),
  opPage: (data) => post('/op/page', data),
  opQuery: (data) => post('/op/query', data),
  balance: (data) => post('/balance', data)
}

export default umsCorpApi
