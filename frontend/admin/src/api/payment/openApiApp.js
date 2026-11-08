/**
 * OpenAPI 应用管理：BFF /web/pay/openapi-app → payment /api/payment/admin/openapi-app
 */
import request from '@/utils/request'

export const openApiAppApi = {
  page: (params) => request.post('/web/pay/openapi-app/page', params || {}),
  detail: (id) => request.get(`/web/pay/openapi-app/detail/${id}`),
  save: (data) => request.post('/web/pay/openapi-app/save', data),
  toggle: (param) => request.post('/web/pay/openapi-app/toggle', param),
  resetSecret: (param) => request.post('/web/pay/openapi-app/reset-secret', param)
}

export default openApiAppApi
