import { beforeEach, describe, it, expect } from 'vitest'
import { setLocale } from '@/i18n-element'

beforeEach(() => setLocale('zh-CN'))
import { buildPluginMenuNodes, mergePluginMenus, normalizePluginMenuNames, normalizePluginRoutePath } from './pluginMenuMerge'
import { isPathAllowedByMenuTree } from './menuNavigate'

const plugin = { code: 'mall-coupon', name: '优惠券插件' }

const baseTree = () => [
  {
    id: 60000,
    pid: 0,
    type: 'M',
    name: '商城',
    paths: '/mall',
    is_show: 1,
    children: [
      { id: 60001, pid: 60000, type: 'C', name: '商城工作台', paths: '/mall/dashboard/overview', is_show: 1, children: [] }
    ]
  }
]

it('normalizes legacy plugin roots into the /plugin namespace', () => {
  expect(normalizePluginRoutePath('crm', '/crm/customer')).toBe('/plugin/crm/customer')
  expect(normalizePluginRoutePath('geo', '/geo')).toBe('/plugin/geo')
  expect(normalizePluginRoutePath('oa', '/plugin/oa/apps')).toBe('/plugin/oa/apps')
})

describe('插件菜单合并（T10 动态路由 / 菜单权限联动）', () => {
  it('历史英文插件一级菜单统一显示中文产品名', () => {
    const tree = [
      { pid: 0, type: 'M', name: 'Logging', paths: '/plugin/logging', children: [] },
      { pid: 0, type: 'M', name: 'Supplier', paths: '/plugin/supplier', children: [] },
      { pid: 0, type: 'M', name: 'Project Management', paths: '/plugin/pm', children: [] },
      { pid: 0, type: 'M', name: 'AICode', paths: '/plugin/aicode', children: [] }
    ]
    normalizePluginMenuNames(tree)
    expect(tree.map((item) => item.name)).toEqual(['日志管理', '供应商管理', '项目管理', 'AI 开发'])
  })

  it('插件菜单按宿主当前语言解析翻译键', () => {
    const tree = normalizePluginMenuNames([
      { pid: 0, type: 'M', name: 'Finance', paths: '/plugin/finance', i18nKey: 'plugin.finance.name', children: [] }
    ])
    expect(tree[0].name).toBe('财务中心')
  })

  it('候选名已被另一个一级菜单占用时保留库里原名（防侧栏重名）', () => {
    const tree = [
      { pid: 0, type: 'M', name: '项目管理', paths: '/project', i18nKey: 'nonexistent.official', children: [] },
      { pid: 0, type: 'M', name: 'PM', paths: '/plugin/pm', i18nKey: 'nonexistent.plugin', children: [] }
    ]
    normalizePluginMenuNames(tree)

    // 官方菜单已占用「项目管理」：pm 插件不得被兜底表或语言包改成同名
    expect(tree[0].name).toBe('项目管理')
    expect(tree[1].name).toBe('PM')
  })

  it('未撞名时仍按兜底表显示中文产品名（回归）', () => {
    const tree = [
      { pid: 0, type: 'M', name: 'OrderFood', paths: '/plugin/orderfood', i18nKey: 'nonexistent.plugin', children: [] }
    ]
    normalizePluginMenuNames(tree)

    expect(tree[0].name).toBe('餐饮管理')
  })

  it('未授权的无 group 插件清单不能新增入口或授予权限', () => {
    const tree = mergePluginMenus(baseTree(), buildPluginMenuNodes({ code: 'webpos' }, [
      { path: '/plugin/webpos', title: 'WebPOS 收银台', icon: 'Monitor', sort: 3000 }
    ]))
    expect(tree).toHaveLength(1)
    expect(isPathAllowedByMenuTree(tree, '/plugin/webpos')).toBe(false)
    expect(isPathAllowedByMenuTree(tree, '/mall/dashboard/overview')).toBe(true)
  })

  it('精确匹配已有授权菜单时只补展示字段，保留ID、落点、父级与子树', () => {
    const allowed = { id: 30, pid: 0, paths: '/plugin/webpos', display_pos: 'plugin', perms: 'webpos:view', children: [
      { id: 31, pid: 30, name: '订单', paths: '/plugin/webpos/orders' }
    ] }
    const tree = [...baseTree(), allowed]
    const nodes = buildPluginMenuNodes({ code: 'webpos' }, [
      { path: '/plugin/webpos', title: 'WebPOS 收银台', icon: 'Monitor', pid: 999, perms: 'forged', group: 'biz' },
      { path: '/plugin/webpos/settings', title: '未授权设置' }
    ])
    mergePluginMenus(tree, nodes)
    mergePluginMenus(tree, nodes)
    expect(tree).toHaveLength(2)
    expect(tree[1]).toEqual({
      id: 30, pid: 0, paths: '/plugin/webpos', display_pos: 'plugin', perms: 'webpos:view',
      name: 'WebPOS 收银台', icon: 'Monitor',
      children: [{ id: 31, pid: 30, name: '订单', paths: '/plugin/webpos/orders' }]
    })
    expect(tree[1].children.map(menu => menu.paths)).toEqual(['/plugin/webpos/orders'])
    expect(isPathAllowedByMenuTree(tree, '/plugin/webpos/orders')).toBe(true)
    expect(isPathAllowedByMenuTree(tree, '/plugin/webpos/settings')).toBe(false)
  })

  it('旧宿主分组授权不能代替具体应用授权', () => {
    const group = { id: 90, pid: 0, params: 'host:menu-group', perms: 'shell:group:biz', paths: '/group/biz', children: [] }
    const tree = mergePluginMenus([group], buildPluginMenuNodes({ code: 'erp' }, [
      { path: '/plugin/erp', title: 'ERP', group: 'biz' }
    ]))
    expect(tree).toEqual([group])
    expect(group.children).toEqual([])
    expect(isPathAllowedByMenuTree(tree, '/plugin/erp')).toBe(false)
  })

  it('仅授权子页时清单域根不能升级授权范围', () => {
    const tree = [{ id: 31, pid: 0, paths: '/plugin/webpos/orders', name: '订单', children: [] }]
    mergePluginMenus(tree, buildPluginMenuNodes({ code: 'webpos' }, [{ path: '/plugin/webpos', title: 'WebPOS' }]))
    expect(tree).toHaveLength(1)
    expect(isPathAllowedByMenuTree(tree, '/plugin/webpos/orders/123')).toBe(true)
    expect(isPathAllowedByMenuTree(tree, '/plugin/webpos/settings')).toBe(false)
  })

  it('缺少 path 的菜单声明被忽略', () => {
    expect(buildPluginMenuNodes(plugin, [{ title: '没有路径' }, { path: '' }])).toEqual([])
  })
})

it('preserves finance subsection names instead of renaming them all to the product name', () => {
  const tree = [{ pid: 1, type: 'M', name: 'Finance', paths: '/plugin/finance', children: [
    { pid: 2, type: 'M', name: '资金管理', paths: '/plugin/finance/fund', children: [] },
    { pid: 2, type: 'C', name: '财务报表', paths: '/plugin/finance/report', children: [] }
  ] }]
  normalizePluginMenuNames(tree)
  expect(tree[0].name).toBe('财务中心')
  expect(tree[0].children.map(m => m.name)).toEqual(['资金管理', '财务报表'])
})
