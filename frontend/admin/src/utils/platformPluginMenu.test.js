import { describe, expect, it } from 'vitest'
import {
  normalizePlatformPluginMenus,
  normalizePlatformPluginPath,
  PLATFORM_PLUGIN_CENTER_PATH,
  PLATFORM_PLUGIN_MARKET_PATH
} from './platformPluginMenu'

const legacyTree = () => [{
  id: 60000,
  pid: 0,
  type: 'M',
  name: '\u5546\u57ce\u7ba1\u7406',
  paths: '/mall',
  is_show: 1,
  children: [
    { id: 60080, pid: 60000, type: 'C', name: '\u63d2\u4ef6\u4e2d\u5fc3', paths: '/mall/addons', is_show: 1, children: [] },
    { id: 60081, pid: 60000, type: 'C', name: '\u63d2\u4ef6\u5e02\u573a', paths: '/mall/market', is_show: 1, children: [] }
  ]
}]

describe('platform plugin menu normalization', () => {
  it('maps legacy paths to the platform namespace', () => {
    expect(normalizePlatformPluginPath('/mall/addons')).toBe(PLATFORM_PLUGIN_CENTER_PATH)
    expect(normalizePlatformPluginPath('/mall/market')).toBe(PLATFORM_PLUGIN_MARKET_PATH)
  })

  it('moves legacy entries out of the mall menu', () => {
    const tree = legacyTree()
    normalizePlatformPluginMenus(tree)

    const mall = tree.find((node) => node.paths === '/mall')
    const platform = tree.find((node) => node.paths === '/platform')
    expect(mall.children).toHaveLength(0)
    expect(platform.children.map((node) => node.paths)).toEqual([
      PLATFORM_PLUGIN_CENTER_PATH,
      PLATFORM_PLUGIN_MARKET_PATH
    ])
    expect(platform.children.every((node) => node.pid === platform.id)).toBe(true)
  })

  it('keeps a mall submenu that owns its own children (id=11024 商城应用)', () => {
    // 库里 id=11024「插件中心」paths=/mall/addons，挂商城管理下，
    // 带 7 个商城应用子项（文章/素材/客服/消息/公告/预存款/数据采集）。
    // 它不是通往平台插件中心的跳板，搬迁会让这批商城菜单错挂到一级「插件中心」下。
    const tree = [{
      id: 1,
      pid: 0,
      type: 'M',
      name: '\u5546\u57ce\u7ba1\u7406',
      paths: '/mall',
      is_show: 1,
      children: [{
        id: 11024,
        pid: 1,
        type: 'C',
        name: '\u63d2\u4ef6\u4e2d\u5fc3',
        paths: '/mall/addons',
        is_show: 1,
        children: [
          { id: 60153, pid: 11024, type: 'M', name: '\u6587\u7ae0\u7ba1\u7406', paths: '/mall/admin/application/article', is_show: 1, children: [] },
          { id: 60158, pid: 11024, type: 'C', name: '\u9884\u5b58\u6b3e', paths: '/mall/admin/application/deposit', is_show: 1, children: [] }
        ]
      }]
    }]
    normalizePlatformPluginMenus(tree)

    const mall = tree.find((node) => node.paths === '/mall')
    expect(tree.some((node) => node.paths === '/platform')).toBe(false)
    expect(mall.children).toHaveLength(1)
    expect(mall.children[0].children).toHaveLength(2)
    expect(mall.children[0].paths).toBe('/mall/addons')
  })

  it('does not convert a legacy root with non-plugin children into a platform root', () => {
    const tree = [{
      id: 4, pid: 0, type: 'M', name: '旧业务目录', paths: '/addons', is_show: 1,
      children: [{ id: 321, pid: 4, type: 'C', name: '骑手管理', paths: '/rider/manage' }]
    }]
    normalizePlatformPluginMenus(tree)
    expect(tree[0].paths).toBe('/addons')
    expect(tree[0].name).toBe('旧业务目录')
    expect(tree[0].children[0].paths).toBe('/rider/manage')
    expect(tree.some(node => node.paths === '/platform')).toBe(false)
  })

  it('keeps repeated normalization idempotent for a mall business subtree', () => {
    const tree = legacyTree()
    tree[0].children.push({
      id: 11024, pid: 60000, type: 'M', name: '商城应用', paths: '/mall/addons',
      children: [{ id: 60153, pid: 11024, paths: '/mall/admin/application/article' }]
    })
    normalizePlatformPluginMenus(tree)
    const once = structuredClone(tree)
    normalizePlatformPluginMenus(tree)
    expect(tree).toEqual(once)
    expect(tree[0].children[0].paths).toBe('/mall/addons')
    expect(tree.find(node => node.paths === '/platform').children.map(node => node.id)).toEqual([60080, 60081])
  })

  it('does not create a platform menu without a permitted plugin entry', () => {
    const tree = [{ id: 60000, pid: 0, type: 'M', paths: '/mall', children: [] }]
    normalizePlatformPluginMenus(tree)
    expect(tree.map((node) => node.paths)).toEqual(['/mall'])
  })
})
