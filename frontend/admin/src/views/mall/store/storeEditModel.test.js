import { describe, expect, it } from 'vitest'

import { buildStoreUpdatePayload, normalizeStoreDetail } from './storeEditModel'

describe('店铺编辑数据转换', () => {
  it('入驻类型与等级保留真实值，未记录不填默认等级', () => {
    expect(normalizeStoreDetail()).toMatchObject({ entryType: null, shopLevelId: null })
    const form = normalizeStoreDetail({ id: 9, entryType: 2, shopLevelId: 3 })
    expect(buildStoreUpdatePayload(form)).toMatchObject({ entryType: 2, shopLevelId: 3 })
    const unknown = buildStoreUpdatePayload(normalizeStoreDetail({ id: 8 }))
    expect(unknown).not.toHaveProperty('entryType')
    expect(unknown).not.toHaveProperty('shopLevelId')
  })
  it('将 merchant 详情转换为编辑表单', () => {
    expect(normalizeStoreDetail({
      id: 1,
      name: '测试店铺',
      type: 0,
      shopCategory: { id: 8, name: '软件服务' },
      contact: '张三',
      mobile: '13800138000',
      shopStatus: 1,
      businessStatus: 0,
      account: { loginAccount: '13800138000' }
    })).toMatchObject({
      id: 1,
      name: '测试店铺',
      type: 0,
      shopCategoryId: 8,
      businessStatus: 0,
      loginAccount: '13800138000'
    })
  })

  it('保存时裁剪文本并保留状态字段', () => {
    expect(buildStoreUpdatePayload({
      id: '1',
      name: ' 测试店铺 ',
      type: 1,
      shopCategoryId: 8,
      contact: ' 张三 ',
      mobile: ' 13800138000 ',
      addressInfo: ' 上海市 ',
      lat: '31.2',
      lng: '121.4',
      shopStatus: 1,
      businessStatus: 0
    })).toEqual({
      id: 1,
      name: '测试店铺',
      type: 1,
      shopCategoryId: 8,
      contact: '张三',
      mobile: '13800138000',
      addressInfo: '上海市',
      lat: '31.2',
      lng: '121.4',
      shopStatus: 1,
      businessStatus: 0
    })
  })
})
