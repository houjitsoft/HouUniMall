import { describe, expect, it, vi } from 'vitest'
import { navigateFromMenuNode, collectSecondaryMenuRoots, resolveFirstLevelMenuByDescendant } from './firstLevelMenuNav'

vi.mock('element-plus', () => ({ ElMessage: { error: vi.fn(), warning: vi.fn() } }))

const navigate = (children) => {
  const router = { push: vi.fn().mockResolvedValue(undefined) }
  navigateFromMenuNode({ id: 5, pid: 0, name: 'O2O', paths: '/rider', children }, { router })
  return router.push.mock.calls[0]?.[0]
}

describe('O2O rider navigation', () => {
  it('opens the authorized rider list instead of inventing a dashboard route', () => {
    expect(navigate([
      { id: 325, paths: '/rider/performance', is_show: 1 },
      { id: 321, paths: '/rider/list', is_show: 1 }
    ])).toBe('/rider/list')
  })
  it('uses an authorized visible child when the user has no rider-list permission', () => {
    expect(navigate([
      { id: 321, paths: '/rider/list', is_show: 0 },
      { id: 325, paths: '/rider/performance', is_show: 1 }
    ])).toBe('/rider/performance')
  })
})

describe('grouped module secondary navigation', () => {
  it('keeps the module root and siblings when a deeply nested page is active', async () => {
    const { resolveGroupedModuleMenu } = await import('./firstLevelMenuNav')
    const finance = { paths: '/plugin/finance', children: [
      { paths: '/plugin/finance/fund', children: [{ paths: '/plugin/finance/fund/receipt' }] },
      { paths: '/plugin/finance/report' }
    ] }
    expect(resolveGroupedModuleMenu({ children: [finance] }, '/plugin/finance/fund/receipt')).toBe(finance)
  })
  it('finds OA via authorized descendants even when the root is the apps route', async () => {
    const { resolveGroupedModuleMenu } = await import('./firstLevelMenuNav')
    const oa = { paths: '/plugin/oa/apps', children: [{ paths: '/plugin/oa/hr/employee' }] }
    expect(resolveGroupedModuleMenu({ children: [oa] }, '/plugin/oa/hr/employee')).toBe(oa)
    expect(resolveGroupedModuleMenu({ children: [oa] }, '/plugin/crm')).toBe(null)
    expect(resolveGroupedModuleMenu({ children: [{ ...oa, is_show: 0 }] }, '/plugin/oa/hr/employee')).toBe(null)
  })
})

describe('legacy alias ownership', () => {
  it('keeps AICode secondary navigation in its canonical group even if AICloud is earlier or longer', async () => {
    const { resolveFirstLevelMenuByDescendant } = await import('./firstLevelMenuNav')
    const ai = { children: [{ paths: '/plugin/aicode', children: [{ paths: '/plugin/aicode/workbench' }] }] }
    const other = { children: [{ paths: '/plugin/aicloud' }] }
    for (const groups of [[other, ai], [ai, other]]) {
      expect(resolveFirstLevelMenuByDescendant(groups, '/plugin/aicode')).toBe(ai)
      expect(resolveFirstLevelMenuByDescendant(groups, '/plugin/aicloud/workbench')).toBe(ai)
    }
  })
})


describe('marketing category entry', () => {
  it('opens category 4 even when the activity root has children', () => {
    const router = { push: vi.fn().mockResolvedValue(undefined) }
    const onNavigate = vi.fn()
    navigateFromMenuNode({ paths: '/plugin/activity', children: [{ paths: '/plugin/activity/coupon' }] }, { router, onNavigate })
    expect(router.push).toHaveBeenCalledWith('/platform/plugins?cateId=4&entry=marketing')
    expect(onNavigate).toHaveBeenCalledOnce()
  })
  it('keeps existing activity child links', () => {
    const router = { push: vi.fn().mockResolvedValue(undefined) }
    navigateFromMenuNode({ paths: '/plugin/activity/coupon' }, { router })
    expect(router.push).toHaveBeenCalledWith('/plugin/activity/coupon')
  })
})

describe('application entry destinations', () => {
  const target = menu => {
    const router = { push: vi.fn().mockResolvedValue(undefined) }
    navigateFromMenuNode(menu, { router })
    return router.push.mock.calls[0]?.[0]
  }

  it('opens an authorized lowcode page rather than an ungranted dashboard', () => {
    expect(target({ paths: '/lowcode', children: [
      { paths: '/lowcode/application', type: 'C', is_show: 1 }
    ] })).toBe('/lowcode/application')
  })

  it('skips actions, hidden pages and disabled branches when opening a plugin', () => {
    expect(target({ paths: '/plugin/erp', children: [
      { paths: '/plugin/erp/delete', type: 'A' },
      { paths: '/plugin/erp/hidden', is_show: '0' },
      { paths: '/plugin/erp/disabled', is_disable: '1', children: [{ paths: '/plugin/erp/disabled/list' }] },
      { paths: '/plugin/erp/purchase', children: [{ paths: '/plugin/erp/purchase/order', type: 'C' }] }
    ] })).toBe('/plugin/erp/purchase/order')
  })

  it('descends through a pathless group without inventing an application route', () => {
    expect(target({ paths: '/plugin/erp', children: [
      { name: '采购', children: [{ paths: '/plugin/erp/purchase/order', type: 'C' }] }
    ] })).toBe('/plugin/erp/purchase/order')
  })

  it('keeps a standalone application root when there are no visible children', () => {
    expect(target({ paths: '/plugin/mall-rebate', children: [{ paths: '/plugin/mall-rebate/delete', type: 'A' }] })).toBe('/plugin/mall-rebate')
  })

  it('opens OA workbench instead of the first authorized leaf like notice', () => {
    expect(target({
      id: 100317,
      pid: 0,
      name: 'OA办公',
      paths: '/plugin/oa/apps',
      perms: 'shell:oa:apps',
      display_pos: 'plugin',
      children: [
        {
          name: '公告通知',
          type: 'M',
          perms: 'shell:oa:nav:notice',
          children: [{ name: '公告通知', type: 'C', paths: '/plugin/oa/notice', is_show: 1 }]
        },
        { name: '工作台', type: 'C', paths: '/plugin/oa/apps', perms: 'shell:oa:nav:apps', is_show: 1 },
        { name: '工作流', type: 'C', paths: '/plugin/oa/workflow', is_show: 1 }
      ]
    })).toBe('/plugin/oa/apps')
  })

  // 生产库结构：公告挂在「工作台」子树下，firstApplicationPage 会先钻进工作台再落到 notice
  it('opens OA apps when notice is nested under the workbench child (prod shape)', () => {
    expect(target({
      id: 101366,
      pid: 0,
      name: 'OA办公',
      paths: '/plugin/oa/apps',
      perms: 'shell:oa:apps',
      display_pos: 'plugin',
      type: 'M',
      is_show: 1,
      children: [
        {
          id: 101385,
          pid: 101366,
          name: '工作台',
          type: 'C',
          paths: '/plugin/oa/apps',
          perms: 'shell:oa:nav:apps',
          sort: 9999,
          is_show: 1,
          children: [
            {
              id: 101388,
              pid: 101385,
              name: '公告通知',
              type: 'M',
              perms: 'shell:oa:nav:notice',
              is_show: 1,
              children: [
                { id: 101389, pid: 101388, name: '公告通知', type: 'C', paths: '/plugin/oa/notice', perms: 'shell:oa:nav:notice:notice', is_show: 1 }
              ]
            }
          ]
        },
        { id: 101386, pid: 101366, name: '工作流', type: 'C', paths: '/plugin/oa/workflow', is_show: 1 }
      ]
    })).toBe('/plugin/oa/apps')
  })

  // GEO 工作台首页就是根路径 /plugin/geo（GEODashboard），子树首叶子常是关键词研究
  it('opens GEO dashboard home instead of the first research leaf', () => {
    expect(target({
      id: 100942,
      pid: 0,
      name: 'GEO',
      paths: '/plugin/geo',
      perms: 'shell:geo:apps',
      display_pos: 'plugin',
      type: 'M',
      is_show: 1,
      children: [
        { name: '关键词研究', type: 'C', paths: '/plugin/geo/research/keywords', is_show: 1, sort: 1900 },
        { name: '话题聚类', type: 'C', paths: '/plugin/geo/research/topic-clusters', is_show: 1, sort: 1800 }
      ]
    })).toBe('/plugin/geo')
  })
})


describe('independent mall business roots', () => {
  it('opens the first authorized page through nested groups instead of inventing a dashboard', () => {
    const router = { push: vi.fn().mockResolvedValue(undefined) }
    navigateFromMenuNode({ id: 1002, pid: 0, paths: '/mall/goods', children: [
      { paths: '/mall/goods/hidden', is_show: 0 },
      { paths: '/mall/goods/disabled', is_disable: 1, children: [{ paths: '/mall/goods/disabled/list' }] },
      { paths: '/mall/goods/manage', children: [
        { paths: '/mall/goods/delete', type: 'A' },
        { paths: '/mall/goods/list', type: 'C' }
      ] }
    ] }, { router })
    expect(router.push).toHaveBeenCalledWith('/mall/goods/list')
  })

  it('does not invent /dashboard for a plain directory with only leaf pages', () => {
    const router = { push: vi.fn().mockResolvedValue(undefined) }
    navigateFromMenuNode({
      id: 1002, pid: 0, name: '商品管理', paths: '/mall/goods',
      children: [
        { id: 2004, type: 'C', paths: '/mall/goods/list', children: [
          { id: 2008, type: 'A', paths: '/mall/goods/import' },
          { id: 2009, type: 'A', paths: '/mall/goods/audit' }
        ] },
        { id: 2005, type: 'C', paths: '/mall/goods/category' }
      ]
    }, { router })
    expect(router.push).toHaveBeenCalledWith('/mall/goods/list')
    expect(router.push).not.toHaveBeenCalledWith('/mall/goods/dashboard')
  })
})


it('skips obsolete mall child routes while preserving server authorization', () => {
  const router = { push: vi.fn().mockResolvedValue(undefined), resolve: path => ({ name: path.includes('/admin/') ? 'NotFound' : 'CustomerStatistics' }) }
  navigateFromMenuNode({ paths: '/mall/customer', children: [
    { paths: '/mall/admin/consumer', children: [{ paths: '/mall/admin/consumer/lists', type: 'C' }] },
    { paths: '/mall/customer/statistics', type: 'C' }
  ] }, { router })
  expect(router.push).toHaveBeenCalledWith('/mall/customer/statistics')
})

describe('二级侧栏的一级菜单候选集', () => {
  const tree = [
    // 库里真实存在：顶级菜单为 type='C' 但自身带子菜单（用户管理）
    {
      id: 1005, pid: 0, type: 'C', name: '用户管理', paths: '/mall/customer',
      display_pos: 'left', is_show: 1,
      children: [
        { id: 319, pid: 1005, type: 'C', name: '用户行为', paths: '/users/member/behavior', is_show: 1 },
        { id: 320, pid: 1005, type: 'C', name: '用户统计', paths: '/users/member/statistics', is_show: 1 }
      ]
    },
    // 历史目录型一级菜单
    { id: 19, pid: 0, type: 'M', name: '仪表盘', paths: '/dashboard', display_pos: 'left', is_show: 1 },
    // 隐藏的目录型一级菜单（历史父级，需保留）
    { id: 1, pid: 0, type: 'M', name: '商城管理', paths: '/mall', display_pos: 'hidden', is_show: 1 }
  ]

  it('把 type=C 的一级业务菜单纳入候选，否则其二级栏不会渲染', () => {
    const ids = collectSecondaryMenuRoots(tree).map((menu) => menu.id)
    expect(ids).toContain(1005)
  })

  it('保留原有 type=M 与隐藏父级，只做合并不删项', () => {
    const ids = collectSecondaryMenuRoots(tree).map((menu) => menu.id)
    expect(ids).toContain(19)
    expect(ids).toContain(1)
  })

  it('同一菜单不会重复出现', () => {
    const ids = collectSecondaryMenuRoots(tree).map((menu) => menu.id)
    expect(ids.filter((id) => id === 19)).toHaveLength(1)
    expect(ids.filter((id) => id === 1005)).toHaveLength(1)
  })

  it('从 /users/member/behavior 能反查到「用户管理」一级菜单，二级栏才渲染得出来', () => {
    const roots = collectSecondaryMenuRoots(tree)
    const root = resolveFirstLevelMenuByDescendant(roots, '/users/member/behavior')
    expect(root?.id).toBe(1005)
    expect(root.children.filter((m) => Number(m.is_show) === 1)).toHaveLength(2)
  })

  it('空树与 null 入参不报错', () => {
    expect(collectSecondaryMenuRoots()).toEqual([])
    expect(collectSecondaryMenuRoots([])).toEqual([])
  })
})
