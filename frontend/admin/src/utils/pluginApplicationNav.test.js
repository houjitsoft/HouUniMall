import { describe, expect, it } from 'vitest'
import { buildApplicationItems, buildInstalledApplicationItems, groupApplicationItems, filterApplicationItems, selectManagedMenus, menuPluginCode, pluginCategoryId } from './pluginApplicationNav'

const menu = (code, extra = {}) => ({ id: code, pid: 0, name: code.toUpperCase(), paths: `/plugin/${code}`, plugin_code: code, display_pos: 'plugin', cate_id: 7, ...extra })
const product = (code, extra = {}) => ({ code, name: code.toUpperCase(), installed: true, enabled: true, cateId: 7, ...extra })

describe('日常应用入口', () => {
  it('仅展示有权限菜单且已安装启用的应用，去重但保留同服务不同应用', () => {
    const tree = [menu('erp'), menu('erp'), menu('oa'), menu('crm'), menu('pm'), menu('aicode')]
    const items = buildApplicationItems(tree, [product('erp'), product('oa', { installed: false }), product('crm', { enabled: false }), product('geo'), product('pm', { hostService: 'pm' }), product('aicode', { hostService: 'pm' })])
    expect(items.map(x => x.code).sort()).toEqual(['aicode', 'erp', 'pm'])
  })
  it('维护到期不移除已交付应用，健康异常保留卡片', () => {
    const [item] = buildApplicationItems([menu('erp')], [product('erp', { licensed: false, maintenanceExpired: true, healthy: false })])
    expect(item.code).toBe('erp')
    expect(item.unavailable).toBe(true)
  })
  it('低代码内置路由按菜单权限保留，空权限树不泄露应用', () => {
    const lowcode = menu('lowcode', { paths: '/lowcode', plugin_code: '' })
    expect(buildApplicationItems([lowcode], []).map(x => x.code)).toEqual(['lowcode'])
    expect(buildApplicationItems([], [product('erp')])).toEqual([])
  })
  it('隐藏、停用、按钮、仅内部子菜单不产生应用入口', () => {
    const tree = [menu('erp', { is_show: 0 }), menu('oa', { is_disable: 1 }), menu('crm', { type: 'A' }), { id: 4, pid: 0, display_pos: 'left', children: [menu('geo', { pid: 4 })] }]
    expect(buildApplicationItems(tree, ['erp', 'oa', 'crm', 'geo'].map(c => product(c)))).toEqual([])
  })
  it('兼容编码别名，分类以插件目录为准', () => {
    expect(menuPluginCode({ paths: '/plugin/aicloud' })).toBe('aicode')
    expect(buildApplicationItems([menu('erp')], [product('erp', { cateId: 42 })])[0].cateId).toBe(42)
  })
  it('布尔字符串不会把未装和停用应用当作可用', () => {
    expect(buildApplicationItems([menu('erp'), menu('oa')], [product('erp', { installed: 'false' }), product('oa', { enabled: '0' })])).toEqual([])
  })
})

describe('数据库分类与搜索', () => {
  it('按接口分类顺序展示，隐藏空分类，支持任意新增分类', () => {
    const groups = groupApplicationItems([{ code: 'erp', name: 'ERP', cateId: 42 }, { code: 'oa', name: 'OA', cateId: 8 }], [{ id: 8, name: '办公' }, { id: 9, name: '空类' }, { id: 42, name: '客户新增分类' }])
    expect(groups.map(g => g.name)).toEqual(['全部应用', '办公', '客户新增分类'])
    expect(groups[0].items).toHaveLength(2)
  })
  it('失效分类统一归未分类，常用仅包含当前可访问的应用', () => {
    const groups = groupApplicationItems([{ code: 'erp', name: 'ERP', cateId: 77 }], [], ['erp', 'secret'])
    expect(groups.map(g => g.name)).toEqual(['常用', '全部应用', '未分类'])
    expect(groups[0].items.map(x => x.code)).toEqual(['erp'])
  })
  it('已安装清单可含无菜单项；右上角入口只列有权限可进的应用', () => {
    const catalog = [product('erp'), product('pm')]
    const tree = [menu('erp')]
    expect(buildInstalledApplicationItems(tree, catalog).map(item => item.code).sort()).toEqual(['erp', 'pm'])
    expect(buildApplicationItems(tree, catalog).map(item => item.code)).toEqual(['erp'])
  })

  it('已安装清单与插件中心同一分类，菜单入口和空分类插件都按目录归类', () => {
    const catalog = [
      product('erp', { cateId: 2 }),
      product('oa', { installed: false, cateId: 3 }),
      product('live', { cateId: null, name: '直播' }),
      product('rider', { enabled: false, cateId: 8 })
    ]
    const tree = [
      menu('erp', { cate_id: 99 }),
      menu('article', { paths: '/mall/admin/application/article', plugin_code: 'mall-application-article', cate_id: 7, name: '文章管理' })
    ]
    const items = buildInstalledApplicationItems(tree, catalog)
    expect(items.map(item => item.code).sort()).toEqual(['erp', 'live', 'rider'])
    expect(pluginCategoryId({ cateId: null })).toBe(0)
    expect(items.find(item => item.code === 'erp').cateId).toBe(2)
    expect(items.find(item => item.code === 'live').cateId).toBe(0)
    expect(items.find(item => item.code === 'rider').unavailableReason).toBe('disabled')
    const groups = groupApplicationItems(items, [{ id: 2, name: '应用 / 工具' }, { id: 8, name: '配送 / 门店' }])
    expect(groups.find(group => group.name === '全部应用').items).toHaveLength(3)
    expect(groups.find(group => group.name === '应用 / 工具').items.map(item => item.code)).toEqual(['erp'])
    expect(groups.find(group => group.name === '配送 / 门店').items.map(item => item.code)).toEqual(['rider'])
    expect(groups.find(group => group.name === '未分类').items.map(item => item.code)).toEqual(['live'])
  })
  it('名称与编码都能搜索且不改变分组数据', () => {
    const items = [{ code: 'erp', name: '企业资源管理' }, { code: 'oa', name: '办公' }]
    expect(filterApplicationItems(items, ' ERP ').map(x => x.code)).toEqual(['erp'])
    expect(filterApplicationItems(items, '办公').map(x => x.code)).toEqual(['oa'])
    expect(items).toHaveLength(2)
  })
})

describe('菜单管理视图', () => {
  const tree = [{ id: 1, pid: 0, name: '商城管理', display_pos: 'left', children: [{ id: 2, name: '商品' }] }, menu('erp', { children: [{ id: 12, name: '采购管理' }] }), { id: 90, pid: 0, name: '旧分组', display_pos: 'hidden', params: 'host:menu-group' }]
  it('核心、应用、隐藏分开呈现并保留子树', () => {
    expect(selectManagedMenus(tree, 'core').map(x => x.id)).toEqual([1])
    expect(selectManagedMenus(tree, 'applications')[0].children[0].name).toBe('采购管理')
    expect(selectManagedMenus(tree, 'hidden').map(x => x.id)).toEqual([90])
  })
  it('按应用分类筛选，搜索子菜单仍保留应用父节点', () => {
    expect(selectManagedMenus(tree, 'applications', { cateId: 8 })).toEqual([])
    expect(selectManagedMenus(tree, 'applications', { cateId: 7, query: '采购' }).map(x => x.id)).toEqual(['erp'])
  })
})
