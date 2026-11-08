/**
 * 宿主依赖 UX（通用 hostService，不硬编码 pm）：
 * 小插件依托宿主服务；宿主本身常是付费独立插件（如 项目管理/pm、点餐/orderfood）。
 * 列表仍展示「需 xx 服务」真相；点击购买/安装时引导先去宿主货架。
 */
import { ElMessageBox } from 'element-plus'

/** 严格等于 false：旧目录未提供宿主信息时不得误杀。 */
export const hostUnavailable = (item) => item?.hostAvailable === false

/**
 * @param {object} item 依赖宿主的市场条目
 * @param {Array<object>} [catalog] 当前已加载的市场列表（用于解析宿主展示名/购买态）
 */
export function resolveHostMeta(item, catalog = []) {
  const hostCode = String(item?.hostService || '').trim()
  const listing = (catalog || []).find(
    (p) => String(p?.code || '').trim().toLowerCase() === hostCode.toLowerCase()
  ) || null
  return {
    hostCode,
    hostName: (listing?.name && String(listing.name).trim()) || hostCode || '宿主插件',
    hostListing: listing
  }
}

export function hostHintText(item, catalog = []) {
  const { hostCode, hostName } = resolveHostMeta(item, catalog)
  if (!hostCode) return ''
  return hostName && hostName !== hostCode
    ? `需先安装「${hostName}」`
    : `需 ${hostCode} 服务`
}

/**
 * 弹出「先买/装宿主」确认框；点确认后回调 onGoHost。
 * @returns {Promise<boolean>} 用户是否确认前往宿主
 */
export async function promptHostRequired(item, { catalog = [], onGoHost } = {}) {
  const { hostCode, hostName, hostListing } = resolveHostMeta(item, catalog)
  if (!hostCode) return false
  const pluginName = item?.name || item?.code || '该插件'
  const reason = item?.hostUnavailableReason
    ? String(item.hostUnavailableReason).trim()
    : `当前站点尚未部署「${hostName}」宿主服务`
  try {
    await ElMessageBox.confirm(
      `「${pluginName}」依赖宿主插件「${hostName}」（编码：${hostCode}）。${reason}。\n\n请先购买并安装该宿主插件，完成后再回来购买/安装本插件。`,
      '需要先购买/安装宿主插件',
      {
        type: 'warning',
        confirmButtonText: `去购买/安装「${hostName}」`,
        cancelButtonText: '取消',
        distinguishCancelAndClose: true,
        dangerouslyUseHTMLString: false
      }
    )
    if (typeof onGoHost === 'function') {
      await onGoHost({ hostCode, hostName, hostListing })
    }
    return true
  } catch {
    return false
  }
}
