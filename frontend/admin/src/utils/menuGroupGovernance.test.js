/**
 * 菜单分组治理回归用例（2026-09-17）。
 *
 * 背景：2026-09-16 用户两次报告过同一类观感问题 ——「点开一级菜单，显示的还是一级菜单」。
 * 根因是插件挂进宿主分组（业务经营 / 其他应用）后，二级栏把**整组子项**铺了出来。
 * 修复落在 `resolveGroupedModuleMenu`（命中分组根时下钻到路径命中的最深子项）与
 * `isHeaderGroupMenu` / `pickFirstLevelMenus`（顶栏分组与侧栏一级的职责分离）。
 *
 * 这些行为此前只有零散的函数级用例，缺「用户可感知判据」层面的断言，所以本文件把它们钉住：
 *   1. **二级栏标题绝不能是宿主分组根名** —— 这是最直接的人工验收判据；
 *   2. **二级栏内容绝不是分组根的整组子项**；
 *   3. 顶栏分组不进侧栏一级集合，空分组自动隐藏；
 *   4. 多分组并存时不得串组。
 *
 * 这些用例都是纯函数级（不挂组件、不启浏览器），改动 `firstLevelMenuNav.js` 时能立刻报警。
 */
import { describe, expect, it } from 'vitest'
import {
  HEADER_GROUP_PERMS,
  isHeaderGroupMenu,
  isMenuGroup,
  pickFirstLevelMenus,
  resolveGroupedModuleMenu,
  visibleGroupChildren
} from './firstLevelMenuNav'

const MENU_GROUP_PARAM = 'host:menu-group'

/** 宿主分组根：业务经营（顶栏右上角渲染，不进侧栏） */
const bizGroup = (children) => ({
  id: 100352,
  pid: 0,
  type: 'M',
  name: '业务经营',
  paths: '/group/biz',
  perms: 'shell:group:biz',
  params: MENU_GROUP_PARAM,
  is_show: 1,
  children
})

/** 分组下的插件模块：OA 的服务端落点是子路径 /plugin/oa/apps */
const oaModule = {
  id: 100317,
  pid: 100352,
  type: 'M',
  name: 'OA办公',
  paths: '/plugin/oa/apps',
  perms: 'shell:oa:apps',
  is_show: 1,
  children: [
    { id: 100318, pid: 100317, type: 'C', name: '员工管理', paths: '/plugin/oa/hr/employee', is_show: 1, children: [] },
    { id: 100319, pid: 100317, type: 'C', name: '已下线页面', paths: '/plugin/oa/legacy', is_show: 0, children: [] }
  ]
}

/** 同组下的另一个模块：WebPOS */
const webposModule = {
  id: 100900,
  pid: 100352,
  type: 'M',
  name: 'WebPOS 收银台',
  paths: '/plugin/webpos',
  perms: 'shell:webpos:apps',
  is_show: 1,
  children: [
    { id: 100901, pid: 100900, type: 'C', name: '收银台', paths: '/plugin/webpos/checkout', is_show: 1, children: [] }
  ]
}

const dashboard = {
  id: 19, pid: 0, type: 'M', name: '仪表盘', paths: '/dashboard',
  perms: 'dashboard:modules', is_show: 1, children: []
}

describe('二级栏下钻：用户可感知判据', () => {
  it('命中分组内的深路径时，二级栏归属是模块而绝不是宿主分组根', () => {
    const group = bizGroup([oaModule, webposModule])
    const hit = resolveGroupedModuleMenu(group, '/plugin/oa/hr/employee')

    expect(hit).toBe(oaModule)
    // 判据一：二级栏标题不是分组根名（「点开一级菜单，显示的还是一级菜单」的直接检测）
    expect(hit.name).not.toBe(group.name)
    expect(hit.name).toBe('OA办公')
  })

  it('二级栏内容不是分组根的整组子项（不会把同组其它模块一起铺出来）', () => {
    const group = bizGroup([oaModule, webposModule])
    const hit = resolveGroupedModuleMenu(group, '/plugin/oa/hr/employee')

    // 判据二：铺开整组的典型特征就是「二级栏等于分组根的全部可见子项」
    expect(hit.children).not.toEqual(visibleGroupChildren(group))
    expect(hit).toBe(oaModule)
    expect(group.children).toHaveLength(2)
  })

  it('同一组内切换模块时归属跟着路径走，不会一直停在第一个模块', () => {
    const group = bizGroup([oaModule, webposModule])

    expect(resolveGroupedModuleMenu(group, '/plugin/oa/hr/employee')).toBe(oaModule)
    expect(resolveGroupedModuleMenu(group, '/plugin/webpos/checkout')).toBe(webposModule)
  })

  it('不属于本分组的路径返回 null（避免串组铺开别的分组子项）', () => {
    const group = bizGroup([webposModule])

    expect(resolveGroupedModuleMenu(group, '/plugin/oa/hr/employee')).toBeNull()
  })

  it('隐藏子项不参与下钻匹配（is_show=0 的模块不因命中路径而被选中）', () => {
    const group = bizGroup([{ ...oaModule, is_show: 0 }])

    expect(resolveGroupedModuleMenu(group, '/plugin/oa/hr/employee')).toBeNull()
  })

  it('尾斜杠与深层路径都能命中同一模块', () => {
    const group = bizGroup([oaModule])

    expect(resolveGroupedModuleMenu(group, '/plugin/oa/apps')).toBe(oaModule)
    expect(resolveGroupedModuleMenu(group, '/plugin/oa/apps/')).toBe(oaModule)
    expect(resolveGroupedModuleMenu(group, '/plugin/oa/hr/employee/detail')).toBe(oaModule)
  })
})

describe('顶栏分组与侧栏一级的职责分离', () => {
  it('分组根被识别为顶栏分组，普通一级菜单不是', () => {
    expect(isMenuGroup(bizGroup([]))).toBe(true)
    expect(isHeaderGroupMenu(bizGroup([]))).toBe(true)

    expect(isMenuGroup(dashboard)).toBe(false)
    expect(isHeaderGroupMenu(dashboard)).toBe(false)
  })

  it('分组判定优先按 perms 稳定标识，名称只作兜底', () => {
    // 改名不影响识别（perms 是 shell:group:biz）
    expect(isHeaderGroupMenu({ ...bizGroup([]), name: '经营业务' })).toBe(true)
    // 无 perms 时按约定名称兜底
    expect(isHeaderGroupMenu({ ...bizGroup([]), perms: '', name: '业务经营' })).toBe(true)
    // 既不是约定 perms 也不是约定名称 —— 不能误判为顶栏分组
    expect(isHeaderGroupMenu({ ...bizGroup([]), perms: 'shell:other:apps', name: '普通模块' })).toBe(false)
  })

  it('HEADER_GROUP_PERMS 与分组键一一对应', () => {
    expect(HEADER_GROUP_PERMS).toEqual(['shell:group:biz', 'shell:group:other'])
  })

  it('空分组自动隐藏，非空分组保留（组织协同/财务合规这类空分组不该出现在一级集合里）', () => {
    const emptyGroup = { ...bizGroup([]), id: 100358, name: '组织协同', perms: 'shell:group:org' }
    const menus = pickFirstLevelMenus([dashboard, emptyGroup, bizGroup([webposModule])])

    expect(menus.map((m) => m.name)).toEqual(['仪表盘', '业务经营'])
  })

  it('分组下只剩隐藏子项时同样视为空分组', () => {
    const hiddenOnly = bizGroup([{ ...webposModule, is_show: 0 }])
    const menus = pickFirstLevelMenus([dashboard, { ...hiddenOnly, id: 100359, name: '财务合规' }])

    expect(menus.map((m) => m.name)).toEqual(['仪表盘'])
  })
})

describe('分组内可见子项过滤', () => {
  it('剔除隐藏项与动作型节点（type=A），is_show 缺省视为可见', () => {
    const group = bizGroup([
      webposModule,
      { id: 1, pid: 100352, type: 'M', name: '已隐藏', paths: '/plugin/hidden', is_show: 0, children: [] },
      { id: 2, pid: 100352, type: 'A', name: '新增按钮', perms: 'x:add', is_show: 1, children: [] },
      { id: 3, pid: 100352, type: 'M', name: '前端产物节点', paths: '/plugin/legacy', children: [] }
    ])

    expect(visibleGroupChildren(group).map((m) => m.name)).toEqual(['WebPOS 收银台', '前端产物节点'])
  })
})
