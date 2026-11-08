import { getPluginContext } from './context.js'
import { getToken } from '@/utils/auth.js'

function call(path, body = {}, options = {}) {
  return getPluginContext().callApi(path, { ...body, token: getToken() || '' }, options)
}

export function dataOf(response) {
  if (response && typeof response === 'object' && Object.hasOwn(response, 'data')) return response.data
  return response
}

/** 平台/店铺优惠券模板分页列表 */
export async function loadTemplates(params = {}) {
  const data = dataOf(await call('voucher/template/platform/list', {
    page: params.page || 1,
    limit: params.limit || 20,
    voucherTTitle: params.voucherTTitle || undefined,
    voucherTState: params.voucherTState ?? undefined
  }))
  return data || { totalCount: 0, list: [] }
}

export function saveTemplate(template) {
  return call(template.voucherTId ? 'voucher/template/platform/update' : 'voucher/template/insert', template).then(dataOf)
}

export function deleteTemplate(voucherTId) {
  return call('voucher/template/platform/delete', { voucherTId }).then(dataOf)
}

export function updateTemplateState(voucherTId, voucherTState) {
  return call('voucher/template/platform/update', { voucherTId, voucherTState }).then(dataOf)
}
