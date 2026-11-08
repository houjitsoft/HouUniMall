import { describe, expect, it } from 'vitest'
import { collectNavItems, pickLeftNavItems, pickPluginNavItems, getMenuDisplayPos, pathWithinMenu, findMenuById } from './menuDisplay'

describe('一级菜单唯一落点', () => {
  it('只接收根节点，隐藏分组的子项不会被提升到全局', () => {
    const tree = [{ id: 1, pid: 0, display_pos: 'left' }, { id: 2, pid: 0, display_pos: 'plugin' },
      { id: 3, pid: 0, display_pos: 'hidden', params: 'host:menu-group', children: [{ id: 4, display_pos: 'plugin' }] },
      { id: 5, pid: 1, display_pos: 'plugin' }]
    expect(pickLeftNavItems(tree).map(m => m.id)).toEqual([1])
    expect(pickPluginNavItems(tree).map(m => m.id)).toEqual([2])
  })
  it('显式落点优先，菜单编号大小不会推断成插件', () => {
    expect(collectNavItems([{ id: 900000, pid: 0, display_pos: 'left' }])[0].display_pos).toBe('left')
    expect(getMenuDisplayPos({ id: 900000 })).toBe('left')
    expect(getMenuDisplayPos({ paths: '/plugin/erp' })).toBe('plugin')
  })
  it('兼容序列化数字，按钮、隐藏和停用项不展示', () => {
    expect(collectNavItems([{ id: 1, pid: '0', is_show: '1', is_disable: '0' }, { id: 2, type: 'A' }, { id: 3, is_show: '0' }, { id: 4, is_disable: '1' }]).map(m => m.id)).toEqual([1])
  })
  it('低代码保留内置路径仍可放插件位', () => {
    expect(pickPluginNavItems([{ id: 21, pid: 0, paths: '/lowcode', display_pos: 'plugin' }])[0].paths).toBe('/lowcode')
  })
})
describe('应用内部导航定位', () => {
  const erp = { id: 10, paths: '/plugin/erp', children: [{ id: 11, paths: '/plugin/erp/purchase' }] }
  it('按路径边界识别应用而非相似前缀', () => {
    expect(pathWithinMenu(erp, '/plugin/erp/purchase/1')).toBe(true)
    expect(pathWithinMenu(erp, '/plugin/erp-other')).toBe(false)
    expect(findMenuById([erp], '11')).toBe(erp.children[0])
  })
})
