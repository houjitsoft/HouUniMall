import { describe,it,expect } from 'vitest'
import { normalizeCrmMenus } from './crmMenuNormalize'
describe('CRM历史菜单归位',()=>{
 it('设置从按钮下归位，重复菜单去重，其他模块保留',()=>{
  const other={paths:'/oa',children:[{type:'A',paths:'/oa/test'}]}
  const tree=[other,{paths:'/crm',children:[{type:'M',paths:'/crm/customer',children:[{type:'C',paths:'/crm/customer/list',children:[{type:'A',paths:'/crm/customer/followup',children:[{type:'C',paths:'/crm/setting/basic'}]}]}]},{type:'M',paths:'/crm/setting',children:[{type:'C',paths:'/crm/setting/basic'}]},{type:'M',paths:'/crm/sales',children:[{type:'C',paths:'/crm/sales/order'},{type:'C',paths:'/crm/sales/orders'}]}]}]
  normalizeCrmMenus(tree)
  expect(tree[0]).toBe(other)
  expect(tree[1].children[0].children[0].children).toBeUndefined()
  expect(tree[1].children[1].children).toHaveLength(1)
  expect(tree[1].children[2].children).toHaveLength(1)
  expect(tree[1].children[2].children[0].paths).toBe('/crm/sales/orders')
 })
})

 it('别名菜单保留正确页面标题，隐藏设置不被重新加入',()=>{
  const tree=[{paths:'/crm',children:[{type:'M',paths:'/crm/product',children:[{type:'C',name:'产品规格',paths:'/crm/product/spec'},{type:'C',name:'产品列表',paths:'/crm/product/list'}]},{type:'M',paths:'/crm/customer',children:[{type:'C',paths:'/crm/customer/list',children:[{type:'A',paths:'/crm/customer/button',children:[{type:'C',paths:'/crm/setting/security',is_show:0}]}]}]},{type:'M',paths:'/crm/setting'}]}]
  normalizeCrmMenus(tree)
  expect(tree[0].children[0].children).toHaveLength(1)
  expect(tree[0].children[0].children[0].name).toBe('产品列表')
  expect(tree[0].children[2].children||[]).toHaveLength(0)
 })
