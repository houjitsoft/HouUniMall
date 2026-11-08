import request from '@/utils/request'

const BASE = '/web/plugin-center/service'
const dataOf = (response) => response?.data ?? response
const SERVICES = new Set(['crm', 'cms', 'erp', 'finance', 'geo', 'im', 'logging', 'mes', 'oa', 'orderfood', 'pm', 'rider', 'supplier', 'webpos'])
export const isStandaloneService = (code) => SERVICES.has(code)
export const getServiceDelivery = async (pluginCode, version, artifactKind) => dataOf(await request.post(`${BASE}/delivery`, { pluginCode, version, artifactKind }))
export const installService = async (pluginCode, version) => dataOf(await request.post(`${BASE}/install`, { pluginCode, version }))
export const upgradeService = async (pluginCode, version) => dataOf(await request.post(`${BASE}/upgrade`, { pluginCode, version }))
export const getServiceTask = async (taskId) => dataOf(await request.post(`${BASE}/task/status`, { taskId }))
export const getServiceHistory = async () => dataOf(await request.post(`${BASE}/history`))

export const installServiceAndWait = async (pluginCode, version, onProgress) => {
  const result = await installService(pluginCode, version)
  if (!result?.taskId) throw new Error('安装代理没有返回任务编号')
  const deadline = Date.now() + 900_000
  while (Date.now() < deadline) {
    const status = await getServiceTask(result.taskId)
    onProgress?.(status)
    if (status?.state === 'SUCCESS') return { result, finalStatus: status }
    if (['FAILED', 'ROLLED_BACK'].includes(status?.state)) {
      const err = new Error(status.errorMsg || status.result || '容器安装失败')
      err.installStatus = status
      throw err
    }
    await new Promise((resolve) => setTimeout(resolve, 2000))
  }
  throw new Error('安装仍在处理，请在安装记录中查询最终状态')
}

export const upgradeServiceAndWait = async (pluginCode, version, onProgress) => {
  const result = await upgradeService(pluginCode, version)
  if (!result?.taskId) throw new Error('升级代理没有返回任务编号')
  const deadline = Date.now() + 900_000
  while (Date.now() < deadline) {
    const status = await getServiceTask(result.taskId)
    onProgress?.(status)
    if (status?.state === 'SUCCESS') return { result, finalStatus: status }
    if (['FAILED', 'ROLLED_BACK'].includes(status?.state)) {
      const err = new Error(status.errorMsg || status.result || '容器升级失败')
      err.installStatus = status
      throw err
    }
    await new Promise((resolve) => setTimeout(resolve, 2000))
  }
  throw new Error('升级仍在处理，请在安装记录中查询最终状态')
}

/**
 * 浏览器下载制品：优先同站 raw，避免内网 MinIO 地址并记录实际交付。
 * 安装代理永远只用相对 raw，禁止改走外链。
 */
export const downloadServiceArtifact = async (pluginCode, version, kind) => {
  if (!['source', 'runtime'].includes(kind)) throw new Error('不支持的下载类型')
  const delivery = await getServiceDelivery(pluginCode, version, kind)
  const artifact = delivery?.[kind]
  const cloudUrl = String(artifact?.cloudUrl || '').trim()
  const rawUrl = String(artifact?.url || '').trim()
  const link = document.createElement('a')
  if (rawUrl.startsWith('/web/plugin-center/artifact/raw?token=')) {
    link.href = request.getUri({ url: rawUrl })
  } else if (/^https?:\/\//i.test(cloudUrl)) {
    link.href = cloudUrl
  } else {
    throw new Error('下载地址无效')
  }
  link.download = artifact.fileName || `${pluginCode}-${kind}`
  document.body.appendChild(link)
  link.click()
  link.remove()
}
