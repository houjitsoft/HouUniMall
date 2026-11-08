import request from '@/utils/request'

// 插件中心控制面由 base 提供，经 BFF 统一访问。
const BASE_URL = '/web/plugin-center'
const dataOf = (response) => response?.data ?? response

export const getPluginCategories = async () => dataOf(await request.post(`${BASE_URL}/categories`))

export const getPluginSummary = async () => dataOf(await request.post(`${BASE_URL}/summary`))
export const getPluginList = async (params = {}) => dataOf(await request.post(`${BASE_URL}/list`, params))
/** 插件详情：instanceId 用于按站点收敛购买状态 */
export const getPluginDetail = async (pluginCode, instanceId) => {
  const payload = { pluginCode }
  if (instanceId) payload.instanceId = instanceId
  return dataOf(await request.post(`${BASE_URL}/detail`, payload))
}
/** 安装插件：instanceId 用于按站点授权校验 */
export const installPlugin = (pluginCode, instanceId) => {
  const payload = { pluginCode }
  if (instanceId) payload.instanceId = instanceId
  return request.post(`${BASE_URL}/install`, payload)
}
export const uninstallPlugin = (pluginCode) => request.post(`${BASE_URL}/uninstall`, { pluginCode })
export const togglePlugin = (pluginCode, enabled) => request.post(`${BASE_URL}/toggle`, { pluginCode, enabled })
export const savePluginConfig = (pluginCode, configJson) => request.post(`${BASE_URL}/config/save`, { pluginCode, configJson })
/** 创建插件：真实生成 plugins/{hostService}/{code} 骨架并写目录/市场库 */
export const createPlugin = (payload) => request.post(`${BASE_URL}/create`, payload)
