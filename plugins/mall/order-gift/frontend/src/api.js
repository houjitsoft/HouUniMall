import { getPluginContext } from './context.js'

function call(path, body = {}, options = {}) {
  return getPluginContext().callApi(path, body, options)
}

export function dataOf(response) {
  if (response && typeof response === 'object' && Object.hasOwn(response, 'data')) return response.data
  return response
}

export async function loadRules() {
  return dataOf(await call('admin/rules', {}, { method: 'get' })) || { list: [], stats: {} }
}

export function saveRule(rule) {
  return call('admin/rules', rule, { method: 'post' }).then(dataOf)
}

export function deleteRule(id) {
  return call('admin/rules/delete', {}, { method: 'post', config: { params: { id } } }).then(dataOf)
}

export async function loadRecords(params = {}) {
  return dataOf(await call('admin/records', {}, { method: 'get', config: { params } })) || { total: 0, list: [] }
}

export function evaluate(payload) {
  return call('admin/evaluate', payload, { method: 'post' }).then(dataOf)
}
