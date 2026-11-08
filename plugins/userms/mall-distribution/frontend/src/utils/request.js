import { getPluginContext } from '../context.js'

/**
 * 返利结算插件：后端是 mall 主服务的原生 Controller（/mall/rebate/**），
 * 经 BFF 的商城模块直通通道 /web/mall/module/rebate/** 访问，因此全部走宿主
 * callCoreApi 透传原始路径。
 *
 * 注意 mall-distribution 不能进 PLUGIN_ROUTE_CODES：一旦进去，buildCoreApiPath()
 * 会以「插件 API 必须通过插件专属通道调用」为由拒掉每一个请求，而它的后端
 * 并没有挂在 /web/plugin/{code}/** 上，于是全线 404。
 */
const invoke = (method, url, data, config = {}) => {
  const context = getPluginContext()
  return context.callCoreApi(url, data, { method, config })
}

const request = (configOrUrl, maybeConfig = {}) => {
  const config = typeof configOrUrl === 'string' ? maybeConfig : (configOrUrl || {})
  const url = typeof configOrUrl === 'string' ? configOrUrl : config.url
  const method = String(config.method || 'get').toLowerCase()
  const data = method === 'get' || method === 'delete' ? config.params ?? config.data : config.data
  return invoke(method, url, data, config)
}
request.get = (url, config) => invoke('get', url, config?.params, config)
request.post = (url, data, config) => invoke('post', url, data, config)
request.put = (url, data, config) => invoke('put', url, data, config)
request.delete = (url, config) => invoke('delete', url, config?.data, config)
request.patch = (url, data, config) => invoke('patch', url, data, config)
export default request
