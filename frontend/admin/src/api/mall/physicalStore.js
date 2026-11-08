import request from '@/utils/request'

export const physicalStoreList = (data) => request.post('/web/mall/store/list', data)
export const physicalStoreMerchantOptions = () => request.post('/web/mall/store/merchant-options')
export const physicalStoreAccountOptions = (data) => request.post('/web/mall/store/account-options', data)
export const physicalStoreCreate = (data) => request.post('/web/mall/store/create', data)
export const physicalStoreToggle = (data) => request.post('/web/mall/store/toggle', data)
