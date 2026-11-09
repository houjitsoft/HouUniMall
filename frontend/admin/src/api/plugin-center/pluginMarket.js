/**
 * 插件市场 API（base 插件中心，经 /web/plugin-center/market 访问）
 * 对接 bff-web http://localhost:8391/web/plugin-center/market/*
 * 与「插件中心」（/web/plugin-center/*）的区别：
 *   插件市场 = 市场侧（浏览已上架插件、按版本在线安装）
 *   插件中心 = 站点侧（管理本站点已安装插件的启停与配置）
 */
import request from '@/utils/request'

const BASE_URL = '/web/plugin-center/market'
const dataOf = (response) => response?.data ?? response

/** 市场列表：{ list: MarketPluginItem[], categories: [{ id, name, count }] } */
export const getMarketList = async (params = {}) => dataOf(await request.post(`${BASE_URL}/list`, params))

/**
 * 市场分类：{ id, name, count }[]，id 为 hou_addon_cate.id（唯一分类字典），
 * id=0 为「未分类」兜底桶。市场页与列表接口返回的 categories 同源同结构。
 */
export const getMarketCategories = async () => dataOf(await request.post(`${BASE_URL}/categories`))

/** 插件详情：{ plugin: MarketPluginItem, versions: MarketPluginVersion[] }，instanceId 用于收敛购买状态 */
export const getMarketDetail = async (pluginCode, instanceId) => {
  const payload = { pluginCode }
  if (instanceId) payload.instanceId = instanceId
  return dataOf(await request.post(`${BASE_URL}/detail`, payload))
}

/**
 * 前端使用 manifest 口径的运行方式：EMBEDDED = runtime: embedded，STANDALONE = runtime: standalone。
 * 市场安装接口目前只认历史值 LIGHTWEIGHT / HEAVY，这里在 API 边界做最后一次映射。
 */
const INSTALL_PROFILE_TO_API = {
  EMBEDDED: 'LIGHTWEIGHT',
  STANDALONE: 'HEAVY'
}

/**
 * 在线安装：下载 → MD5 校验 → 落盘 → 登记安装态。
 * @param {string} pluginCode 插件编码
 * @param {string} [version] 版本号，不传表示最新已审核版本
 * @param {'EMBEDDED'|'STANDALONE'} [installProfile] 运行方式，对齐 plugin.yaml 的 runtime 字段
 * @param {string} instanceId 当前站点实例标识（域名），租户安装时必填
 */
export const installFromMarket = (pluginCode, version, installProfile = 'EMBEDDED', instanceId) => {
  const payload = {
    pluginCode,
    installProfile: INSTALL_PROFILE_TO_API[String(installProfile || '').toUpperCase()] || installProfile
  }
  if (version) {
    payload.version = version
  }
  if (instanceId) {
    payload.instanceId = instanceId
  }
  return request.post(`${BASE_URL}/install`, payload)
}

/**
 * 查询本站点某插件的授权状态。
 * @param {string} instanceId 当前站点实例标识（域名），租户查询时必填
 * @returns {{pluginCode:string,licensed:boolean,licenseType?:string,licenseKey?:string,
 *            instanceId?:string,expireTime?:string,permanent?:boolean}}
 */
export const getLicenseStatus = async (pluginCode, instanceId) => {
  const payload = { pluginCode }
  if (instanceId) payload.instanceId = instanceId
  return dataOf(await request.post(`${BASE_URL}/license/status`, payload))
}

/**
 * 激活付费插件授权：凭授权码激活，可选绑定站点实例（域名）。
 *
 * 授权码由平台侧在购买后签发（线下收款 → 发证），站点侧只能激活、不能自行签发。
 *
 * @param {string} licenseKey 授权码，形如 HOU-MALLWEBPOS-XXXXXXXXXXXXXXXX
 * @param {string} [pluginCode] 插件编码，用于校验授权码归属
 * @param {string} [instanceId] 站点实例标识（域名），传则绑定；已绑定其它实例会被拒绝
 */
export const activateLicense = (licenseKey, pluginCode, instanceId) => {
  const payload = { licenseKey }
  if (pluginCode) {
    payload.pluginCode = pluginCode
  }
  if (instanceId) {
    payload.instanceId = instanceId
  }
  return request.post(`${BASE_URL}/license/activate`, payload)
}

/**
 * 本站点已激活授权列表（授权管理页）。
 *
 * 后端按 instanceId 收敛，只会返回绑定到本站点的授权，不会返回其它客户的授权码。
 *
 * @param {string} instanceId 站点实例标识（域名），建议由 getSiteInstanceId() 提供
 * @returns {Promise<Array<{pluginCode,licenseKey,licenseType,instanceId,maxInstances,expireTime,permanent,status}>>}
 */
export const getMyLicenses = async (instanceId) =>
  dataOf(await request.post(`${BASE_URL}/license/mine`, { instanceId }))

/**
 * 解绑本站点授权：释放本实例与授权码的绑定关系，授权本身仍然有效、可在其它实例重新激活。
 *
 * 注意：插件仍处于安装状态时后端会拒绝解绑，需先在插件中心卸载。
 *
 * @param {string} licenseKey 授权码
 * @param {string} instanceId 站点实例标识（域名）
 */
export const unbindMyLicense = async (licenseKey, instanceId) =>
  dataOf(await request.post(`${BASE_URL}/license/unbind`, { licenseKey, instanceId }))

// ---------- 在线购买：插件订单 ----------

/**
 * 下单：生成待支付订单。
 * @param {{pluginCode:string, instanceId:string, licenseType?:string, version?:string,
 *          validDays?:number, buyerId?:number}} payload
 */
export const createPluginOrder = async (payload) => dataOf(await request.post(`${BASE_URL}/order/create`, payload))

/**
 * 发起支付：走商城既有收银台拿支付参数；金额为 0（试用）时后端直接自动结单发证。
 * @param {{orderNo:string, payMethod:string, terminal?:string, userId?:string,
 *          notifyUrl?:string, returnUrl?:string}} payload
 */
export const payPluginOrder = async (payload) => dataOf(await request.post(`${BASE_URL}/order/pay`, payload))

/**
 * 沙箱模拟支付确认：仅后端 pay.sandbox=true 时可用。
 * @param {string} orderNo 订单号
 * @param {'SUCCESS'|'FAILED'|'REFUNDED'} [result='SUCCESS'] 模拟结果
 */
export const confirmSandboxPluginPayment = async (orderNo, result = 'SUCCESS') =>
  dataOf(await request.post(`${BASE_URL}/pay/sandbox/confirm`, { orderNo, result }))

/**
 * 同步支付状态：后端会查询收银台真实状态，成功即自动签发授权（幂等）。
 * @param {string} orderNo 订单号
 * @param {string} [userId] 下单用户 ID，缺省时后端回退为站点 ID
 */
export const syncPluginOrder = async (orderNo, userId) =>
  dataOf(await request.post(`${BASE_URL}/order/sync`, { orderNo, userId }))

/** 取消待支付订单 */
export const cancelPluginOrder = async (orderNo) => dataOf(await request.post(`${BASE_URL}/order/cancel`, { orderNo }))

/**
 * 本站点订单列表（按 instanceId 收敛）。
 * @param {string} instanceId 站点实例标识（域名）
 */
export const getMyOrders = async (instanceId) => dataOf(await request.post(`${BASE_URL}/order/mine`, { instanceId }))

/**
 * 租户视角的已发布版本列表（市场详情页「版本选择」用）。
 *
 * 与发布者视角（/mall/publish/versions/list）不同：本接口只返回已发布版本，
 * 带 changelog 与兼容性判定，且不返回 jarUrl（下载必须走换票链路）。
 *
 * @param {string} pluginCode 插件编码
 * @param {string} [currentVersion] 当前已装版本，用于标出 isCurrent
 * @returns {Promise<Array<{version:string,changelog?:string,publishTime?:string,
 *          isCurrent:boolean,compatible?:boolean|null,minFrameworkVersion?:string}>>}
 */
export const getMarketVersions = async (pluginCode, currentVersion) => {
  const payload = { pluginCode }
  if (currentVersion) payload.currentVersion = currentVersion
  return dataOf(await request.post(`${BASE_URL}/versions`, payload))
}

/**
 * 目录「有更新」判定：把已装清单一次性送上去，服务端按 SemVer 判断哪些有更高版本。
 *
 * 用于插件列表页的「可升级」红点。
 *
 * @param {string} instanceId 站点实例标识（必填，授权判定按站点收敛）
 * @param {Array<{code:string,version?:string}>} plugins 已装插件清单
 */
export const getCatalogUpdates = async (instanceId, plugins = []) => {
  const payload = { instanceId, plugins }
  return dataOf(await request.post(`${BASE_URL}/catalog/updates`, payload))
}

/**
 * 宿主框架版本：{ frameworkVersion }
 *
 * 与市场兼容性预检、houcloud.update.framework-version 同源。
 * 「设置 → 系统信息」用它显示系统版本，不再依赖开源包缺失的 commons-service。
 */
export const getFrameworkVersion = async () => dataOf(await request.get(`${BASE_URL}/framework-version`))
