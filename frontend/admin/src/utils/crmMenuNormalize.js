import { crmNavigationAliases } from './crmNavigation'
/** 修复历史 CRM 导航结构，不改变按钮权限及其他模块。 */
export function normalizeCrmMenus(menus) {
  const crm = menus?.find(m => (m.paths || m.path) === '/crm')
  if (!crm) return menus
  const settings = crm.children?.find(m => (m.paths || m.path) === '/crm/setting')
  const misplaced = []
  const pathOf = m => m.paths || m.path || ''
  const aliases = { ...crmNavigationAliases,
    '/crm/sales/order': '/crm/sales/orders',
    '/crm/service/ticket': '/crm/service/tickets',
    '/crm/marketing/activity': '/crm/marketing/campaigns'
  }
  const pagePaths = new Set()
  const visit = (nodes, parentPath) => {
    const seen = new Set()
    return (nodes || []).flatMap(node => {
      if (Number(node.is_show) === 0 || Number(node.is_disable) === 1) return []
      const path = aliases[pathOf(node)] || pathOf(node)
      if (path.startsWith('/crm/setting/') && !parentPath.startsWith('/crm/setting')) {
        misplaced.push(node)
        return []
      }
      const children = visit(node.children, path)
      if (node.type === 'A' || Number(node.is_show) === 0 || Number(node.is_disable) === 1) return []
      if (seen.has(path)) return []
      if (node.type === 'C' && pagePaths.has(path)) return []
      if (node.type === 'C') pagePaths.add(path)
      seen.add(path)
      const item = { ...node, paths: path }
      const titles={'/crm/product':'产品列表','/crm/clue':'销售线索','/crm/opportunity/list':'商机列表','/crm/customer/recycle':'客户回收站','/crm/marketing':'营销管理','/crm/product/plan':'产品管理'}
      if(titles[path]){item.name=titles[path];item.title=titles[path]}
      if(node.type==='M'&&path==='/crm/product'){item.name='产品管理';item.title='产品管理'}
      if (children.length) item.children = children
      else delete item.children
      return [item]
    })
  }
  const children = visit(crm.children, '/crm')
  const settingMenu = children.find(m => pathOf(m) === '/crm/setting')
  if (settings && settingMenu) {
    const existing = new Set((settingMenu.children || []).map(pathOf))
    settingMenu.children = [...(settingMenu.children || []), ...misplaced.filter(m => !existing.has(pathOf(m)) && existing.add(pathOf(m))).map(m=>({...m,pid:settingMenu.id}))]
  }
  crm.children = children
  return menus
}
