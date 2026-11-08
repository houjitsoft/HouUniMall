import { getCurrentOrgIdOrNull, setCurrentOrgId } from '@/utils/orgUtils'

/**
 * 切换到另一个组织并重载当前页面，确保所有组织相关的内存数据重新初始化。
 * 依赖可注入，便于验证“先持久化、后重载”的安全顺序。
 *
 * @param {string|number|null|undefined} orgId
 * @param {Object} dependencies
 * @returns {boolean} 是否实际发生了组织切换
 */
export function switchOrganizationAndReload(orgId, dependencies = {}) {
  const normalized = orgId == null ? '' : String(orgId).trim()
  if (!normalized) return false

  const readCurrentOrgId = dependencies.readCurrentOrgId || getCurrentOrgIdOrNull
  const writeCurrentOrgId = dependencies.writeCurrentOrgId || setCurrentOrgId
  const reload = dependencies.reload || (() => window.location.reload())
  const currentOrgId = String(readCurrentOrgId() || '').trim()

  if (currentOrgId === normalized) return false

  writeCurrentOrgId(normalized, true)
  reload()
  return true
}
