/**
 * OA 工作台应用跳转路径解析（与后端 WorkbenchPathResolver 对齐）
 * 应用宫格子菜单 pid=54017（工作台 /oa/apps）
 */

/** @deprecated 旧占位目录 55169，应用已挂到 54017 */
export const OA_WORKBENCH_PLACEHOLDER_MENU_ID = 55169

export const OA_WORKBENCH_MENU_ID = 54017

const toPluginPath = (path) => String(path || '').replace(/^\/oa(?=\/|$)/, '/plugin/oa')

const BY_PERMS = {
  'shell:oa:dashboard': '/oa/screen',
  'shell:oa:approval': '/oa/approval',
  'shell:oa:report': '/oa/report',
  'shell:oa:supplier': '/oa/supplier',
  'shell:oa:attendance': '/oa/attendance',
  'shell:oa:attendance:lists': '/oa/attendance/lists',
  'shell:oa:meeting': '/oa/meeting',
  'shell:oa:notice': '/oa/notice',
  'shell:oa:salary': '/oa/salary/payslip',
  'shell:oa:hr': '/oa/hr',
  'shell:oa:news': '/oa/news',
  'shell:oa:learning': '/oa/learning',
  'shell:oa:live': '/oa/live',
  'shell:oa:ai': '/oa/ai',
  'shell:oa:aifanfan': '/oa/aifanfan',
  'shell:oa:card': '/oa/card',
  'shell:oa:contractqun': '/oa/wecom/crm/group',
  'shell:oa:contract': '/oa/contact'
}

const BY_NAME = {
  投屏: '/oa/screen',
  审批: '/oa/approval',
  汇报: '/oa/report',
  客户联系: '/oa/contact',
  企业名片: '/oa/card',
  服务商管理: '/oa/supplier',
  服务商管理2: '/oa/supplier',
  打卡: '/oa/attendance',
  考勤列表: '/oa/attendance/lists',
  会议室: '/oa/meeting/room',
  会议: '/oa/meeting',
  公告: '/oa/notice',
  工资条: '/oa/salary/payslip',
  人事助手: '/oa/hr',
  行业资讯: '/oa/news',
  学习园地: '/oa/learning',
  直播: '/oa/live',
  '智能机器人 AI+': '/oa/ai',
  爱番番: '/oa/aifanfan',
  客户群: '/oa/wecom/crm/group'
}

const COMPONENT_PATH = {
  dashboard: '/oa/screen',
  approval: '/oa/approval',
  report: '/oa/report'
}

/** 工作台应用宫格全部可跳转路由（供路由权限校验） */
export const OA_WORKBENCH_APP_ROUTES = [
  ...new Set([...Object.values(BY_PERMS), ...Object.values(BY_NAME)].map(toPluginPath))
]

/**
 * 规范化路径
 * @param {string} path 原始路径
 * @returns {string}
 */
function normalizePath(path) {
  if (!path || typeof path !== 'string') return ''
  let p = path.trim()
  if (!p.startsWith('/')) p = `/${p}`
  while (p.length > 1 && p.endsWith('/')) p = p.slice(0, -1)
  return p
}

/**
 * 是否为不可用占位路径
 * @param {string} path 路径
 * @returns {boolean}
 */
function isAmbiguousOaPath(path) {
  const p = normalizePath(path)
  return !p || p === '/plugin/oa' || p === '/plugin/oa/'
}

/**
 * 解析工作台应用跳转路径
 * @param {object} menu 菜单项
 * @returns {string}
 */
export function resolveWorkbenchAppPath(menu) {
  if (!menu) return ''
  const name = (menu.name || '').trim()
  const perms = (menu.perms || '').trim()
  const paths = normalizePath(menu.navPath || menu.paths || menu.path || '')
  const component = (menu.component || '').trim()

  if (!isAmbiguousOaPath(paths)) {
    if (paths === '/oa/dashboard' && (name === '投屏' || perms === 'shell:oa:dashboard')) {
      return '/plugin/oa/screen'
    }
    if (paths === '/oa/contract' && name === '客户联系') {
      return '/plugin/oa/contact'
    }
    if (
      (paths === '/supplier' || paths.startsWith('/supplier/'))
      && (name === '服务商管理' || name === '服务商管理2' || perms === 'shell:oa:supplier')
    ) {
      return '/plugin/oa/supplier'
    }
    return toPluginPath(paths)
  }

  if (component) {
    if (component.startsWith('/oa/')) return toPluginPath(normalizePath(component))
    if (COMPONENT_PATH[component]) return toPluginPath(COMPONENT_PATH[component])
  }

  if (perms && BY_PERMS[perms]) return toPluginPath(BY_PERMS[perms])
  if (name && BY_NAME[name]) return toPluginPath(BY_NAME[name])
  return ''
}
