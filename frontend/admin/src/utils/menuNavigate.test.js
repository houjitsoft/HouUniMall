import { describe, expect, it } from 'vitest'
import {
  isPathAllowedByMenuTree,
  resolveMenuNavigatePath,
  resolveMenuItemPath,
  sanitizeSidebarMenuTree,
  isOaWorkbenchMenu,
  isMallWorkbenchMenu
} from './menuNavigate.js'
import mallRoutes from '@/router/modules/mall/index.js'

const mallFinanceMenuPaths = [
  '/mall/admin/finance/center',
  '/mall/admin/finance/refund',
  '/mall/admin/finance/mall',
  '/mall/admin/finance/shop_details/shop_settle',
  '/mall/admin/finance/shop_details/balance',
  '/mall/admin/finance/shop_details/settlement_record',
  '/mall/admin/finance/shop_details/withdraw',
  '/mall/admin/finance/user_details/withdraw',
  '/mall/admin/finance/user_details/balance',
  '/mall/admin/finance/user_details/commission',
  '/mall/admin/finance/user_details/deposit',
  '/mall/admin/finance/setting',
  '/mall/admin/finance/report',
  '/mall/admin/finance/expense',
  '/mall/admin/finance/income',
  '/mall/admin/finance/overview'
]

const workbenchMenu = {
  id: '54017',
  name: '工作台',
  type: 'M',
  paths: '/plugin/oa/apps',
  perms: 'shell:oa:apps',
  children: [
    { id: 54018, name: '投屏', type: 'C', paths: '/oa/screen', perms: 'shell:oa:dashboard' },
    { id: 54019, name: '审批', type: 'C', paths: '/oa/approval', perms: 'shell:oa:approval' },
    { id: 54020, name: '审批流程', type: 'C', paths: '/oa/contract/process', perms: 'shell:oa:contract:process' }
  ]
}

const menus = [
  {
    id: 7,
    name: 'OA办公',
    type: 'M',
    paths: '/oa',
    pid: 0,
    children: [
      workbenchMenu,
      { id: 88, name: '人事管理', type: 'M', paths: '/oa/hr', children: [] }
    ]
  }
]

describe('OA 工作台侧栏跳转', () => {
  it('OA 插件根路径沿用工作台菜单权限', () => {
    expect(isPathAllowedByMenuTree(menus, '/plugin/oa')).toBe(true)
    expect(isPathAllowedByMenuTree([
      { id: 7, name: 'OA办公', type: 'M', paths: '/oa', children: [
        { id: 99, name: '考勤', type: 'C', paths: '/oa/attendance/lists' }
      ] }
    ], '/plugin/oa')).toBe(false)
  })

  it('isOaWorkbenchMenu 兼容字符串 id', () => {
    expect(isOaWorkbenchMenu(workbenchMenu)).toBe(true)
  })

  it('isOaWorkbenchMenu 不误判 GEO 同名「工作台」分组', () => {
    const geoWorkbench = {
      id: 100001,
      name: '工作台',
      type: 'M',
      paths: '',
      perms: 'shell:geo:group:geo-dashboard'
    }
    expect(isOaWorkbenchMenu(geoWorkbench)).toBe(false)
  })

  it('isOaWorkbenchMenu 不误判商城同名「工作台」', () => {
    const mallWorkbench = {
      id: 2001,
      name: '工作台',
      type: 'C',
      paths: '/mall/dashboard/overview',
      perms: 'shell:mall:dashboard:overview'
    }
    expect(isOaWorkbenchMenu(mallWorkbench)).toBe(false)
  })

  it('点击商城工作台跳到核心仪表盘，不落到 OA', () => {
    const mallMenus = [
      {
        id: 1,
        name: '商城系统',
        type: 'M',
        paths: '/mall',
        pid: 0,
        children: [
          {
            id: 1001,
            name: '商城概览',
            type: 'C',
            paths: '/mall/dashboard',
            children: [
              {
                id: 2001,
                name: '工作台',
                type: 'C',
                paths: '/mall/dashboard/overview',
                perms: 'shell:mall:dashboard:overview'
              }
            ]
          }
        ]
      },
      ...menus
    ]
    expect(resolveMenuNavigatePath(mallMenus, '/mall/dashboard/overview')).toBe(
      '/dashboard'
    )
  })

  it('点击工作台路径不落到第一个子应用', () => {
    expect(resolveMenuNavigatePath(menus, '/plugin/oa/apps')).toBe('/plugin/oa/apps')
    expect(resolveMenuNavigatePath(menus, '/plugin/oa/workbench')).toBe('/plugin/oa/apps')
    expect(resolveMenuNavigatePath(menus, '54017')).toBe('/plugin/oa/apps')
    expect(resolveMenuItemPath({ paths: '/oa/attendance/lists' })).toBe('/plugin/oa/attendance/lists')
    expect(resolveMenuItemPath({ paths: '/oa/hr/employee' })).toBe('/plugin/oa/hr/employee')
  })

  it('sanitizeSidebarMenuTree 把工作台打成叶子', () => {
    const sanitized = sanitizeSidebarMenuTree(menus[0].children)
    const wb = sanitized.find((m) => m.name === '工作台')
    expect(wb).toBeTruthy()
    expect(wb.children).toBeUndefined()
  })

  it('商城工作台菜单为叶子且路径落到核心仪表盘', () => {
    const mallWorkbench = {
      id: 60001,
      name: '工作台',
      type: 'M',
      paths: '/mall/admin/workbench',
      perms: 'mall:workbench',
      children: [
        {
          id: 60101,
          name: '工作台概览',
          type: 'C',
          paths: '/mall/admin/workbench',
          perms: 'mall:workbench:index'
        }
      ]
    }
    expect(isMallWorkbenchMenu(mallWorkbench)).toBe(true)
    expect(resolveMenuItemPath(mallWorkbench)).toBe('/dashboard')
    const sanitized = sanitizeSidebarMenuTree([mallWorkbench])
    expect(sanitized[0].children).toBeUndefined()
    expect(resolveMenuItemPath(sanitized[0])).toBe('/dashboard')
  })
})

describe('商城财务菜单路由', () => {
  const financeMenuTree = [{
    id: 60007,
    name: '财务管理',
    paths: '/mall/admin/finance',
    children: mallFinanceMenuPaths.map((paths, index) => ({
      id: 60121 + index,
      name: `财务页面${index + 1}`,
      type: 'C',
      paths,
      perms: `mall:finance:test-${index + 1}`
    }))
  }]

  it('注册商城结算菜单的真实页面路由，不用插件兜底', () => {
    const registered = new Set(mallRoutes.children.map((route) => route.path))
    expect(registered.has('/mall/finance/center')).toBe(true)
    expect(registered.has('/mall/finance/refund')).toBe(true)
    expect(registered.has('/mall/finance/mall')).toBe(true)
    expect(registered.has('/mall/finance/shop_details/shop_settle')).toBe(true)
    expect(registered.has('/mall/finance/user_details/withdraw')).toBe(true)
    expect(registered.has('/mall/admin/finance/:pathMatch(.*)*')).toBe(false)
    expect(registered.has('/mall/finance/:pathMatch(.*)*')).toBe(false)
  })

  it('旧菜单权限允许访问对应的规范财务路径', () => {
    expect(isPathAllowedByMenuTree(financeMenuTree, '/mall/finance/overview')).toBe(true)
    expect(isPathAllowedByMenuTree(financeMenuTree, '/mall/finance/income')).toBe(true)
    expect(isPathAllowedByMenuTree(financeMenuTree, '/mall/finance/expense')).toBe(true)
    expect(isPathAllowedByMenuTree(financeMenuTree, '/mall/finance/report')).toBe(true)
    expect(isPathAllowedByMenuTree(financeMenuTree, '/mall/finance/setting')).toBe(true)
  })

  it('商城财务菜单不会被识别为插件财务路径', () => {
    expect(isPathAllowedByMenuTree(financeMenuTree, '/mall/finance/center')).toBe(true)
    expect(isPathAllowedByMenuTree(financeMenuTree, '/plugin/finance')).toBe(false)
  })
})

describe('CRM 客户列表导航回归', () => {
  const menus = [{ id: 13, type: 'M', paths: '/crm', children: [
    { id: 54024, type: 'M', paths: '/crm/customer', children: [
      { id: 55062, type: 'C', paths: '/crm/customer/list', children: [
        { id: 20010, type: 'A', paths: '/crm/customer/followup', children: [
          { id: 21045, type: 'C', paths: '/crm/setting/basic' }
        ] }
      ] },
      { id: 55555, type: 'C', paths: '/crm/customer/hidden', is_show: 0 }
    ] }
  ] }]
  it('各层级过滤按钮，客户列表保持可点击叶子', () => {
    const tree = sanitizeSidebarMenuTree(menus)
    expect(tree[0].children[0].children).toHaveLength(1)
    expect(tree[0].children[0].children[0].children).toBeUndefined()
    expect(menus[0].children[0].children[0].children).toHaveLength(1)
  })
  it('列表导航不会落到按钮下面的系统设置', () => {
    expect(resolveMenuNavigatePath(menus, '/crm/customer/list')).toBe('/plugin/crm/customer/list')
    expect(resolveMenuNavigatePath(menus, '/crm/customer')).toBe('/plugin/crm/customer/list')
  })
})

it('GEO cleanup keeps namespaced navigation and empty-path directories', async () => {
  const { filterGeoMenuSubtree } = await import('./menuNavigate')
  const menus = [{ paths: '', name: '品牌建档', children: [
    { paths: '/plugin/geo/settings/brand', name: '基础信息' },
    { paths: '/geo/workflow/projects', name: '项目' },
    { paths: '/plugin/im', name: '错误遗留项' }
  ] }]
  const result = filterGeoMenuSubtree(menus)
  expect(result).toHaveLength(1)
  expect(result[0].children.map(m => m.name)).toEqual(['基础信息', '项目'])
})
