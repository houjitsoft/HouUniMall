/**
 * 插件清单只补充服务器已授权菜单的展示信息。
 * 菜单根、父子关系、落点和路由权限均由服务器菜单树决定；
 * 历史 manifest 的 group 不再参与导航装配，也不能提供应用授权。
 */

import { t as translate } from '@/i18n-element'

/** 插件菜单 id 基数：与 hou_permission_menu 官方 id 段（1xxxx / 2xxxx / 6xxxx）错开 */
export const PLUGIN_MENU_BASE_ID = 120000

/** 统一插件一级菜单的产品名称，兼容旧权限表和旧前端制品中的英文名称。 */
export const PLUGIN_DISPLAY_NAMES = Object.freeze({
  activity: '营销活动',
  aicode: 'AI 开发',
  finance: '财务中心',
  logging: '日志管理',
  orderfood: '餐饮管理',
  pm: '项目管理',
  supplier: '供应商管理'
})

/**
 * 收集整棵树里已有的一级（pid=0）菜单名，供强命名做撞名检测。
 * @param {Array} nodes 菜单节点
 * @param {Set<string>} [acc]
 * @returns {Set<string>}
 */
function collectTopLevelNames(nodes, acc = new Set()) {
  if (!Array.isArray(nodes)) return acc
  for (const node of nodes) {
    if (!node) continue
    if (Number(node.pid) === 0 && node.name) acc.add(String(node.name))
    collectTopLevelNames(node.children, acc)
  }
  return acc
}

export function normalizePluginMenuNames(nodes) {
  if (!Array.isArray(nodes)) return nodes
  // 一级菜单名占用表：同时拦截语言包译文与 PLUGIN_DISPLAY_NAMES 兜底表的命名。
  // 典型事故：plugin.pm.name 与兜底表都指向「项目管理」，而官方菜单 id=11 也叫「项目管理」，
  // 侧栏于是出现两条同名一级入口。库里本来就叫这个名字的（候选名 === 原名）不拦截。
  const topLevelNames = collectTopLevelNames(nodes)

  nodes.forEach((node) => {
    const path = normalizePluginMenuPath(node?.paths || node?.path)
    const match = path.match(/^\/plugin\/([^/]+)/)
    const code = node?.pluginCode || (match && match[1])
    const isProductRoot = /^\/plugin\/[^/]+(?:\/apps)?$/.test(path)
    const i18nKey = node?.i18nKey || node?.titleKey || (code && isProductRoot ? `plugin.${String(code).toLowerCase()}.name` : '')
    const translated = i18nKey ? translate(i18nKey) : ''
    const fallback = String(node?.name || '')
    const mapped = isProductRoot ? (PLUGIN_DISPLAY_NAMES[String(code || '').toLowerCase()] || '') : ''
    const candidate = translated && translated !== i18nKey ? translated : (mapped || fallback)
    // 候选名已被树中另一个一级菜单占用、且不是它自己的原名 → 保留库里原名，不制造新的重名。
    // 两条路径都要拦：语言包（locales/zh-CN/plugin.json 的 plugin.pm.name = 「项目管理」）
    // 与 PLUGIN_DISPLAY_NAMES 兜底表，否则只拦后者会被译文绕过。
    const displayName = candidate !== fallback && topLevelNames.has(candidate) ? fallback : candidate

    if (displayName && (node.pid === 0 || node.type === 'M' || node.pluginRoute)) {
      node.name = displayName
      if (Number(node.pid) === 0) topLevelNames.add(String(displayName))
    }
    if (Array.isArray(node.children)) normalizePluginMenuNames(node.children)
  })
  return nodes
}

/**
 * 规范化菜单路径：补前导斜杠、去尾部斜杠
 * @param {string} path 原始路径
 * @returns {string}
 */
export function normalizePluginMenuPath(path) {
  if (!path || typeof path !== 'string') return ''
  const s = path.trim()
  if (!s) return ''
  const withSlash = s.startsWith('/') ? s : `/${s}`
  return withSlash.length > 1 ? withSlash.replace(/\/+$/, '') : withSlash
}

/**
 * 将插件菜单声明统一到插件命名空间。历史 manifest 可能仍声明
 * `/crm/...`、`/oa/...` 这类宿主根路径；这些路径不能直接注册为产品入口。
 */
export function normalizePluginRoutePath(code, path) {
  const normalized = normalizePluginMenuPath(path)
  const pluginCode = String(code || '').trim()
  if (!normalized || !pluginCode) return normalized
  const prefix = `/plugin/${pluginCode}`
  const legacy = `/${pluginCode}`
  if (normalized === legacy || normalized.startsWith(`${legacy}/`)) {
    return `${prefix}${normalized.slice(legacy.length)}`
  }
  return normalized
}

/**
 * 插件编码 → 稳定的数字偏移，保证菜单 id 在多次加载间一致
 * @param {string} code 插件编码
 * @returns {number}
 */
function codeOffset(code) {
  const text = String(code || '')
  let hash = 0
  for (let i = 0; i < text.length; i += 1) {
    hash = (hash * 31 + text.charCodeAt(i)) % 100000
  }
  return hash % 9000
}

/**
 * 把插件前端产物声明的 menus 转成展示元数据，是否展示由服务器菜单决定
 * @param {{code:string,name?:string,version?:string}} plugin 插件信息（含 code）
 * @param {Array<{path?:string,paths?:string,title?:string,name?:string,i18nKey?:string,titleKey?:string,icon?:string,sort?:number,perms?:string}>} menus 产物声明的菜单
 * @returns {Array<object>} 菜单节点
 */
export function buildPluginMenuNodes(plugin, menus) {
  const code = plugin?.code
  if (!code || !Array.isArray(menus)) return []
  const offset = codeOffset(code)
  const nodes = []

  menus.forEach((menu, index) => {
    const path = normalizePluginRoutePath(code, menu?.path || menu?.paths)
    if (!path) return
    const i18nKey = menu?.i18nKey || menu?.titleKey
    const translated = i18nKey ? translate(i18nKey) : ''
    nodes.push({
      id: PLUGIN_MENU_BASE_ID + offset * 10 + index,
      pid: Number(menu?.pid) || 0,
      type: 'M',
      name: translated && translated !== i18nKey ? translated : (menu?.title || menu?.name || code),
      i18nKey: i18nKey || undefined,
      icon: menu?.icon || 'Document',
      sort: Number(menu?.sort) || 900,
      perms: menu?.perms || `plugin:${code}`,
      paths: path,
      component: '',
      is_show: 1,
      is_disable: 0,
      children: [],
      pluginCode: code,
      pluginRoute: true
    })
  })

  return nodes
}

/**
 * 就地补全服务器菜单中的缺失名称与图标，不新增菜单或更改授权字段。
 * 精确路径匹配防止宽根声明覆盖服务器只授予的某个子页。
 */
export function mergePluginMenus(menuTree, pluginMenus) {
  if (!Array.isArray(menuTree)) return menuTree
  const metadata = new Map((Array.isArray(pluginMenus) ? pluginMenus : [])
    .filter(node => node && normalizePluginMenuPath(node.paths))
    .map(node => [normalizePluginMenuPath(node.paths), node]))
  const walk = nodes => {
    for (const node of nodes || []) {
      const artifact = metadata.get(normalizePluginMenuPath(node?.paths || node?.path))
      if (artifact) {
        if (!node.name && artifact.name) node.name = artifact.name
        if (!node.icon && artifact.icon) node.icon = artifact.icon
      }
      if (node?.children?.length) walk(node.children)
    }
  }
  walk(menuTree)
  return menuTree
}

export default {
  PLUGIN_MENU_BASE_ID,
  normalizePluginMenuPath,
  normalizePluginRoutePath,
  buildPluginMenuNodes,
  mergePluginMenus,
  PLUGIN_DISPLAY_NAMES,
  normalizePluginMenuNames
}
