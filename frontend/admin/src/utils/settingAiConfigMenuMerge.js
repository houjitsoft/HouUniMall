export const SETTING_AI_CONFIG_PATH = '/setting/ai-config'
export const SETTING_AI_COST_PATH = '/setting/aicostingbalance'
export const SETTING_AI_GPT_USAGE_PATH = '/setting/ai-gpt-usage'

const SETTING_ROOT_PATH = '/setting'
const LEGACY_AI_CONFIG_PATH = '/setting/aiconfig'

/** 系统设置下的「AI管理」目录（含两个子菜单） */
export const SETTING_AI_MANAGE_MENU = {
  id: 'setting-ai-manage',
  type: 'M',
  name: 'AI管理',
  icon: 'Cpu',
  sort: 4,
  perms: '',
  paths: '',
  component: '',
  is_show: 1,
  is_disable: 0
}

export const SETTING_AI_CONFIG_MENU = {
  id: 'setting-ai-config',
  type: 'C',
  name: 'AI配置',
  icon: 'Cpu',
  sort: 1,
  perms: '',
  paths: SETTING_AI_CONFIG_PATH,
  component: 'setting/AiConfig',
  is_show: 1,
  is_disable: 0,
  children: []
}

export const SETTING_AI_COST_MENU = {
  id: 'setting-ai-cost',
  type: 'C',
  name: '用量费用',
  icon: 'Price',
  sort: 2,
  perms: '',
  paths: SETTING_AI_COST_PATH,
  component: 'setting/AiCostingBalance',
  is_show: 1,
  is_disable: 0,
  children: []
}

export const SETTING_AI_GPT_USAGE_MENU = {
  id: 'setting-ai-gpt-usage',
  type: 'C',
  name: 'AI 用量',
  icon: 'DataAnalysis',
  sort: 3,
  perms: '',
  paths: SETTING_AI_GPT_USAGE_PATH,
  component: 'setting/AiGptUsage',
  is_show: 1,
  is_disable: 0,
  children: []
}

function normalizePath(path) {
  if (!path) return ''
  const value = String(path).trim()
  if (!value) return ''
  return value.startsWith('/') ? value.replace(/\/+$/, '') || '/' : `/${value}`
}

function isSystemSettingMenu(menu) {
  return normalizePath(menu?.paths || menu?.path) === SETTING_ROOT_PATH || menu?.name === '系统设置'
}

function isAiManageMenu(menu) {
  return menu?.name === SETTING_AI_MANAGE_MENU.name
}

function isAiConfigMenu(menu) {
  const path = normalizePath(menu?.paths || menu?.path)
  return menu?.name === SETTING_AI_CONFIG_MENU.name
    || path === SETTING_AI_CONFIG_PATH
    || path === LEGACY_AI_CONFIG_PATH
}

function isAiCostMenu(menu) {
  const path = normalizePath(menu?.paths || menu?.path)
  return menu?.name === SETTING_AI_COST_MENU.name || path === SETTING_AI_COST_PATH
}

function isAiGptUsageMenu(menu) {
  const path = normalizePath(menu?.paths || menu?.path)
  return menu?.name === SETTING_AI_GPT_USAGE_MENU.name || path === SETTING_AI_GPT_USAGE_PATH
}

function takeMenu(list, matcher) {
  if (!Array.isArray(list)) return null
  const index = list.findIndex(matcher)
  if (index < 0) return null
  const [menu] = list.splice(index, 1)
  return menu
}

function ensureChildList(menu) {
  if (!Array.isArray(menu.children)) menu.children = []
  return menu.children
}

function upsertChild(parent, template, matcher) {
  const children = ensureChildList(parent)
  const existing = children.find(matcher)
  if (existing) {
    Object.assign(existing, {
      name: template.name,
      paths: template.paths,
      component: template.component,
      icon: template.icon || existing.icon,
      type: template.type,
      sort: template.sort,
      is_show: 1,
      is_disable: 0
    })
    if (!Array.isArray(existing.children)) existing.children = []
    return existing
  }
  const created = {
    ...template,
    pid: parent.id,
    children: []
  }
  children.push(created)
  return created
}

/**
 * 保证系统设置下有「AI管理」目录，且包含三个子菜单：AI配置、用量费用、AI 用量。
 * 不嵌套 GEO 站页面；Cloud / GEO 菜单保持独立。
 */
export function mergeSettingAiConfigMenu(menuTree) {
  if (!Array.isArray(menuTree) || menuTree.length === 0) return menuTree

  const systemSettingMenu = menuTree.find(isSystemSettingMenu)
  if (!systemSettingMenu) return menuTree

  const settingChildren = ensureChildList(systemSettingMenu)

  // 从系统设置平级捞出已存在的 AI 相关项，统一挂到「AI管理」下
  const existingManage = takeMenu(settingChildren, isAiManageMenu)
  const orphanConfig = takeMenu(settingChildren, isAiConfigMenu)
  const orphanCost = takeMenu(settingChildren, isAiCostMenu)
  const orphanGptUsage = takeMenu(settingChildren, isAiGptUsageMenu)

  const aiManage = existingManage || {
    ...SETTING_AI_MANAGE_MENU,
    pid: systemSettingMenu.id,
    children: []
  }
  Object.assign(aiManage, {
    name: SETTING_AI_MANAGE_MENU.name,
    type: 'M',
    icon: aiManage.icon || SETTING_AI_MANAGE_MENU.icon,
    sort: aiManage.sort || SETTING_AI_MANAGE_MENU.sort,
    is_show: 1,
    is_disable: 0
  })
  ensureChildList(aiManage)

  // 若原先挂在 AI管理 下，优先保留；再合并平级孤儿项
  if (orphanConfig) ensureChildList(aiManage).push(orphanConfig)
  if (orphanCost) ensureChildList(aiManage).push(orphanCost)
  if (orphanGptUsage) ensureChildList(aiManage).push(orphanGptUsage)

  // 清理 AI管理 下重复项后再规范化
  const nested = ensureChildList(aiManage)
  const configSource = nested.find(isAiConfigMenu) || orphanConfig
  const costSource = nested.find(isAiCostMenu) || orphanCost
  const gptUsageSource = nested.find(isAiGptUsageMenu) || orphanGptUsage
  aiManage.children = nested.filter((item) => !isAiConfigMenu(item) && !isAiCostMenu(item) && !isAiGptUsageMenu(item))

  if (configSource) aiManage.children.push(configSource)
  if (costSource) aiManage.children.push(costSource)
  if (gptUsageSource) aiManage.children.push(gptUsageSource)

  upsertChild(aiManage, SETTING_AI_CONFIG_MENU, isAiConfigMenu)
  upsertChild(aiManage, SETTING_AI_COST_MENU, isAiCostMenu)
  upsertChild(aiManage, SETTING_AI_GPT_USAGE_MENU, isAiGptUsageMenu)

  aiManage.children.sort((a, b) => Number(a.sort || 0) - Number(b.sort || 0))
  settingChildren.push(aiManage)
  settingChildren.sort((a, b) => Number(a.sort || 0) - Number(b.sort || 0))

  return menuTree
}
