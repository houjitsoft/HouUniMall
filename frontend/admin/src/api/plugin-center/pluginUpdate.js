/**
 * 插件安装 / 升级 / 卸载 / 回滚 API（Cloud 后台 /web/plugin-center/update）
 *
 * 与「插件中心」（pluginCenter.js）的分工：
 *   插件中心 = 轻量链路，只写安装状态位 + 补菜单，不下载 jar、不校验、不建表；
 *   本模块  = 完整编排链路（plugin-update），8 步流水线：
 *             预检 → 授权 → 下载 → 四重校验 → 停旧 → 备份 → 迁移 → 装载 → 健康检查。
 *
 * 因此「在线安装 / 升级 / 卸载」一律走本模块；pluginCenter 的 install/uninstall
 * 只作为兜底保留，新代码不要再调用。
 *
 * 注意：接口是同步返回 taskId 的异步任务模型 —— install/upgrade 返回后任务可能还在跑，
 * 需要用 pollTask 轮询 taskStatus 直到终态（SUCCESS / ROLLED_BACK / FAILED）。
 */
import request from '@/utils/request'

const BASE_URL = '/web/plugin-center/update'
const dataOf = (response) => response?.data ?? response

/**
 * 安装插件。
 * @param {string} pluginCode 插件编码
 * @param {string} [version] 版本号，不传表示最新已发布版本
 * @returns {Promise<object>} 安装结果（含 taskId）
 */
export const installFromUpdate = async (pluginCode, version) => {
  const payload = { pluginCode }
  if (version) payload.version = version
  return dataOf(await request.post(`${BASE_URL}/install`, payload))
}

/**
 * 升级插件。
 * @param {string} pluginCode 插件编码
 * @param {string} [targetVersion] 目标版本，不传表示最新
 */
export const upgradePlugin = async (pluginCode, targetVersion) => {
  const payload = { pluginCode }
  if (targetVersion) payload.targetVersion = targetVersion
  return dataOf(await request.post(`${BASE_URL}/upgrade`, payload))
}

/**
 * 升级前风险报告（将备份 / 可能不可逆迁移 / 二开冲突）。
 * @param {string} pluginCode
 * @param {string} [targetVersion]
 * @param {string} [hostService]
 */
export const getUpgradeRisk = async (pluginCode, targetVersion, hostService) => {
  const payload = { pluginCode }
  if (targetVersion) payload.targetVersion = targetVersion
  if (hostService) payload.hostService = hostService
  return dataOf(await request.post(`${BASE_URL}/upgrade-risk`, payload))
}

/**
 * 回滚插件（豁免授权与下架状态，属履约动作）。
 * @param {string} pluginCode 插件编码
 * @param {string} [targetVersion] 目标版本，不传取服务端登记的回滚锚点
 */
export const rollbackPlugin = async (pluginCode, targetVersion) => {
  const payload = { pluginCode }
  if (targetVersion) payload.targetVersion = targetVersion
  return dataOf(await request.post(`${BASE_URL}/rollback`, payload))
}

/**
 * 卸载插件（编排版，带任务流水与补偿）。
 * @param {string} pluginCode 插件编码
 * @param {boolean} [purgeData=false] 是否回收业务数据（把业务表 RENAME 成 _bak_<时间戳>）。
 *   默认 false —— 删表不可逆，且插件表可能被别的模块引用，绝不能默认执行。
 */
export const uninstallFromUpdate = async (pluginCode, purgeData = false) =>
  dataOf(await request.post(`${BASE_URL}/uninstall`, { pluginCode, purgeData }))

/**
 * 兼容性预检：安装/升级前先问一次，把「框架版本过低 / 依赖未满足」提前暴露。
 * @param {string} pluginCode 插件编码
 * @param {string} [version] 版本号
 */
export const precheckPlugin = async (pluginCode, version) => {
  const payload = { pluginCode }
  if (version) payload.version = version
  return dataOf(await request.post(`${BASE_URL}/precheck`, payload))
}

/**
 * 查询任务状态（轮询用）。
 * @param {string} taskId 任务 ID
 * @param {string} pluginCode 插件编码
 */
export const getUpdateTaskStatus = async (taskId, pluginCode) =>
  dataOf(await request.post(`${BASE_URL}/task/status`, { taskId, pluginCode }))

/**
 * 变更历史（可用于「最近更新」与失败排查）。
 * @param {string} [pluginCode] 插件编码，不传返回全部
 * @param {number} [page=1]
 * @param {number} [pageSize=20]
 */
export const getUpdateHistory = async (pluginCode, page = 1, pageSize = 20) => {
  const payload = { page, pageSize }
  if (pluginCode) payload.pluginCode = pluginCode
  return dataOf(await request.post(`${BASE_URL}/history`, payload))
}

/** 终态：轮询到达这些状态即可停止。 */
const TERMINAL_STATES = new Set(['SUCCESS', 'ROLLED_BACK', 'FAILED'])

/**
 * 轮询安装/升级任务直到终态。
 *
 * 为什么必须轮询而不是固定等待：任务包含下载（最大 64MB）与 schema 迁移，
 * 耗时从几百毫秒到几十秒不等，固定 sleep 在慢机器上会误判失败（假阴性）。
 *
 * @param {string} taskId 任务 ID
 * @param {string} pluginCode 插件编码
 * @param {object} [options]
 * @param {number} [options.intervalMs=1000] 轮询间隔
 * @param {number} [options.timeoutMs=180000] 超时上限（3 分钟）
 * @param {(status: object) => void} [options.onProgress] 每次拿到非终态时的回调（渲染进度）
 * @returns {Promise<object>} 终态任务状态
 */
export const pollUpdateTask = async (taskId, pluginCode, options = {}) => {
  const { intervalMs = 1000, timeoutMs = 180000, onProgress } = options
  const deadline = Date.now() + timeoutMs
  let last = null
  while (Date.now() < deadline) {
    last = await getUpdateTaskStatus(taskId, pluginCode)
    const state = last?.state ?? last?.data?.state
    if (state && TERMINAL_STATES.has(String(state).toUpperCase())) {
      return last
    }
    if (typeof onProgress === 'function') onProgress(last)
    await new Promise((resolve) => setTimeout(resolve, intervalMs))
  }
  throw new Error('任务处理超时，请在插件变更历史中查看最终结果')
}

/**
 * 一键安装并等待完成（预检 → 安装 → 轮询）。
 *
 * @param {string} pluginCode 插件编码
 * @param {string} [version] 版本号
 * @param {(status: object) => void} [onProgress] 进度回调
 * @returns {Promise<{precheck: object, result: object}>}
 */
export const installAndWait = async (pluginCode, version, onProgress) => {
  const precheck = await precheckPlugin(pluginCode, version).catch(() => null)
  const result = await installFromUpdate(pluginCode, version)
  const taskId = result?.taskId
  if (!taskId) {
    return { precheck, result }
  }
  const finalStatus = await pollUpdateTask(taskId, pluginCode, { onProgress })
  return { precheck, result, finalStatus }
}

/**
 * 一键升级并等待完成（须先经前端风险确认对话框）。
 */
export const upgradeAndWait = async (pluginCode, targetVersion, onProgress) => {
  const precheck = await precheckPlugin(pluginCode, targetVersion).catch(() => null)
  const result = await upgradePlugin(pluginCode, targetVersion)
  const taskId = result?.taskId
  if (!taskId) {
    return { precheck, result, finalStatus: result }
  }
  const finalStatus = await pollUpdateTask(taskId, pluginCode, { onProgress })
  return { precheck, result, finalStatus }
}
