import { describe, it, expect } from 'vitest'
import { attachPluginSecondaryMenus, PLUGIN_SECONDARY_MENUS } from './pluginSecondaryMenus'

describe('attachPluginSecondaryMenus', () => {
  it('为 /plugin/<code> 节点挂上声明的二级菜单（含挂分组根下的场景）', () => {
    const tree = [
      {
        id: 100352,
        pid: 0,
        name: '业务经营',
        perms: 'shell:group:biz',
        children: [
          { id: 100900, pid: 100352, name: 'WebPOS 收银台', perms: 'plugin:webpos', paths: '/plugin/webpos', children: [] }
        ]
      }
    ]
    attachPluginSecondaryMenus(tree)
    const webpos = tree[0].children[0]
    expect(webpos.children).toHaveLength(PLUGIN_SECONDARY_MENUS.webpos.length)
    expect(webpos.children.map((c) => c.paths)).toEqual(
      PLUGIN_SECONDARY_MENUS.webpos.map((m) => m.path)
    )
    expect(webpos.children[0].pid).toBe(100900)
    expect(webpos.children[0].pluginCode).toBe('webpos')
  })

  it('幂等：已有 children 的节点不被覆盖，重复调用不产生重复', () => {
    const tree = [
      {
        id: 1,
        pid: 0,
        paths: '/plugin/webpos',
        children: [{ id: 99, pid: 1, paths: '/plugin/webpos/custom', children: [] }]
      }
    ]
    attachPluginSecondaryMenus(tree)
    expect(tree[0].children).toHaveLength(1)
    expect(tree[0].children[0].paths).toBe('/plugin/webpos/custom')
    attachPluginSecondaryMenus(tree)
    expect(tree[0].children).toHaveLength(1)
  })

  it('未声明的插件不受影响；子路径 /plugin/<code>/xxx 不误挂', () => {
    const tree = [
      { id: 2, pid: 0, paths: '/plugin/erp', children: [] },
      { id: 3, pid: 0, paths: '/plugin/webpos/checkout', children: [] }
    ]
    attachPluginSecondaryMenus(tree)
    expect(tree[0].children).toHaveLength(0)
    expect(tree[1].children).toHaveLength(0)
  })

  it('子菜单 id 在多次调用间保持稳定', () => {
    const build = () => [{ id: 5, pid: 0, paths: '/plugin/webpos', children: [] }]
    const a = attachPluginSecondaryMenus(build())[0].children.map((c) => c.id)
    const b = attachPluginSecondaryMenus(build())[0].children.map((c) => c.id)
    expect(a).toEqual(b)
  })
})
