import { getPluginContext } from '../context.js'
const request = (configOrUrl, maybeConfig = {}) => {
  const config = typeof configOrUrl === 'string' ? maybeConfig : (configOrUrl || {})
  const url = typeof configOrUrl === 'string' ? configOrUrl : config.url
  const method = String(config.method || 'get').toLowerCase()
  const raw = String(url || '')
  const match = raw.match(/^\/(?:(?:web|api)\/)?(?:plugin\/)?(?:logging|log)(?:\/(.*))?$/)
  const data = method === 'get' || method === 'delete' ? config.params ?? config.data : config.data
  const context = getPluginContext()
  return match ? context.callApi(match[1] || '/', data, { method, config }) : context.callCoreApi(raw, data, { method, config })
}
request.get = (url, config) => request(url, { ...config, method: 'get' })
request.post = (url, data, config) => request(url, { ...config, method: 'post', data })
request.put = (url, data, config) => request(url, { ...config, method: 'put', data })
request.delete = (url, config) => request(url, { ...config, method: 'delete' })
request.patch = (url, data, config) => request(url, { ...config, method: 'patch', data })
export default request
