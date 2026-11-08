import { describe, expect, it } from 'vitest'
import {
  buildPhysicalStoreListParams,
  buildPhysicalStorePayload,
  normalizePhysicalStorePage
} from './physicalStoreModel'

describe('门店列表模型', () => {
  it('查询条件明确使用门店和所属商家字段', () => {
    expect(buildPhysicalStoreListParams({
      storeName: ' 南京店 ',
      merchantId: '3',
      businessStatus: '1',
      riderDeliveryEnabled: '',
      posEnabled: '0'
    }, { pageNo: 2, pageSize: 20 })).toEqual({
      page: 2,
      size: 20,
      storeName: '南京店',
      merchantId: 3,
      businessStatus: 1,
      posEnabled: 0
    })
  })

  it('新增门店必须带所属商家和门店能力配置', () => {
    expect(buildPhysicalStorePayload({
      merchantId: '2', storeNo: ' NJ-01 ', storeName: ' 南京店 ',
      contactPerson: ' 张三 ', contactPhone: ' 13800138000 ',
      province: ' 江苏 ', city: ' 南京 ', district: ' 建邺 ', address: ' 江东中路 ',
      businessStatus: 1, riderDeliveryEnabled: 1, posEnabled: 0
    })).toEqual({
      merchantId: 2, storeNo: 'NJ-01', storeName: '南京店',
      contactPerson: '张三', contactPhone: '13800138000',
      province: '江苏', city: '南京', district: '建邺', address: '江东中路',
      businessStatus: 1, riderDeliveryEnabled: 1, posEnabled: 0
    })
  })

  it('已有账号模式只提交账号授权信息', () => {
    expect(buildPhysicalStorePayload({
      accountMode: 'existing', platformUserId: 'user-42', merchantId: '2', storeNo: '', storeName: '南京店',
      contactPerson: '', contactPhone: '', province: '', city: '', district: '', address: '',
      businessStatus: 1, riderDeliveryEnabled: 0, posEnabled: 0
    })).toMatchObject({ accountMode: 'existing', platformUserId: 'user-42', accountIdentifier: null })
  })

  it('新账号模式提交手机号和密码', () => {
    expect(buildPhysicalStorePayload({
      accountMode: 'new', accountIdentifier: '13800138000', accountPassword: 'secret12', accountPasswordAgain: 'secret12',
      merchantId: '2', storeNo: '', storeName: '南京店', contactPerson: '', contactPhone: '', province: '', city: '', district: '', address: '',
      businessStatus: 1, riderDeliveryEnabled: 0, posEnabled: 0
    })).toMatchObject({ accountMode: 'new', accountIdentifier: '13800138000', accountPassword: 'secret12', accountPasswordAgain: 'secret12' })
  })

  it('只读取真实门店分页数据', () => {
    expect(normalizePhysicalStorePage({ records: [{ id: 7, merchantId: 2 }], total: 1 }))
      .toEqual({ list: [{ id: 7, merchantId: 2 }], total: 1 })
    expect(normalizePhysicalStorePage({ lists: [{ id: 9 }], count: 1 }))
      .toEqual({ list: [], total: 0 })
  })
})
