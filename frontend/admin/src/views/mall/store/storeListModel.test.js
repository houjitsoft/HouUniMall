import { describe, expect, it } from 'vitest'
import {
  buildCreateStorePayload,
  buildStoreListParams,
  buildStoreExportCsv,
  formatStoreDate,
  maskStoreAccount,
  storeAuditState,
  normalizeShopCategoryPage,
  normalizeStorePage
} from './storeListModel'

describe('商城店铺列表接口适配', () => {
  it('缺失字段不生成 NaN 筛选', () => {
    expect(buildStoreListParams({}, { pageNo: 1, pageSize: 15 })).toEqual({ page: 1, size: 15 })
  })
  it('个人企业筛选不复用自营入驻字段', () => {
    expect(buildStoreListParams({ ownerAccount: ' 138 ', entryType: 2, shopLevelId: 3 }, { pageNo: 1, pageSize: 15 }))
      .toEqual({ page: 1, size: 15, ownerAccount: '138', entryType: 2, shopLevelId: 3 })
    expect(normalizeStorePage({ lists: [{ type: 1 }], count: 1 }).list[0].entry_type_text).toBe('—')
  })
  it('脱敏店主账号且不会将缺失审核信息伪装成开店成功', () => {
    expect(maskStoreAccount('13800138000')).toBe('138****8000')
    expect(maskStoreAccount('a@example.com')).toBe('a***@example.com')
    expect(maskStoreAccount('')).toBe('—')
    expect(storeAuditState(null).label).toBe('未记录')
    expect(storeAuditState(1)).toEqual({ label: '开店成功', tone: 'success' })
  })
  it('开店日期支持秒、毫秒和日期字符串，缺失时间不补造', () => {
    expect(formatStoreDate(0)).toBe('—')
    expect(formatStoreDate('2026-11-12 12:00:00')).toBe('2026-11-12')
    expect(formatStoreDate('invalid')).toBe('—')
    expect(formatStoreDate(Date.parse('2026-11-12T00:00:00Z'))).toBe('2026-11-12')
    expect(formatStoreDate(Date.parse('2026-11-12T00:00:00Z') / 1000)).toBe('2026-11-12')
  })
  it('导出字段与表格一致，转义引号、换行和表格公式', () => {
    const csv = buildStoreExportCsv([{ id: 1, name: '=HYPERLINK("x")', account: { loginAccount: '13800138000' }, previousName: '旧名,\n"店"' }])
    expect(csv.startsWith('\uFEFF')).toBe(true)
    expect(csv).toContain('138****8000')
    expect(csv).not.toContain('13800138000')
    expect(csv).toContain('"\'=HYPERLINK(""x"")"')
    expect(csv).toContain('"旧名,\n""店"""')
  })
  it('把页面筛选字段转换为当前 mall 服务参数', () => {
    expect(buildStoreListParams({
      name: ' 厚匠 ',
      type: '2',
      shop_category_id: '9',
      business_status: '0',
      shop_status: '1'
    }, { pageNo: 3, pageSize: 20 })).toEqual({
      page: 3,
      size: 20,
      shopName: '厚匠',
      shopType: 2,
      shopCategoryId: 9,
      businessStatus: 0,
      status: 1
    })
  })

  it('读取 MyBatis IPage 并适配现有表格字段', () => {
    const page = normalizeStorePage({
      records: [{
        id: 7,
        shopName: '厚匠旗舰店',
        shopLogo: '/logo.png',
        shopNo: 'SHOP-7',
        shopType: 1,
        businessCategories: '["软件","服务"]',
        status: 1
      }],
      total: 12
    })

    expect(page.total).toBe(12)
    expect(page.list[0]).toMatchObject({
      name: '厚匠旗舰店',
      logo: '/logo.png',
      account: { loginAccount: 'SHOP-7' },
      shop_category: { name: '软件、服务' },
      type_text: '官方自营',
      business_status: 1,
      shop_status: 1
    })
  })

  it('读取当前经营类目分页字段', () => {
    expect(normalizeShopCategoryPage({ records: [{ id: 1, name: '软件' }] }))
      .toEqual([{ id: 1, name: '软件' }])
  })

  it('适配商家中心店铺列表字段', () => {
    const page = normalizeStorePage({
      lists: [{
        id: 8,
        name: '入驻商家',
        type: 1,
        typeText: '商家入驻',
        shopStatus: 1,
        businessStatus: 0,
        shopCategory: { id: 2, name: '软件服务' },
        account: { loginAccount: 'merchant_8' }
      }],
      count: 1
    })

    expect(page.total).toBe(1)
    expect(page.list[0]).toMatchObject({
      type_text: '商家入驻',
      shop_status: 1,
      business_status: 0,
      shop_category: { id: 2, name: '软件服务' }
    })
  })

  it('把新增商家表单转换为商家开通参数', () => {
    expect(buildCreateStorePayload({
      name: ' 厚匠测试商家 ',
      type: 1,
      shopCategoryId: 3,
      contact: ' 张三 ',
      mobile: ' 13800138000 ',
      addressInfo: ' 上海市浦东新区 ',
      lat: '31.2304',
      lng: '121.4737',
      shopStatus: 1,
      businessStatus: 1,
      accountMode: 'new',
      platformUserId: '',
      accountIdentifier: ' 13800138000 ',
      accountPassword: '12345678',
      accountPasswordAgain: '12345678'
    })).toEqual({
      name: '厚匠测试商家',
      type: 1,
      shopCategoryId: 3,
      contact: '张三',
      mobile: '13800138000',
      addressInfo: '上海市浦东新区',
      lat: '31.2304',
      lng: '121.4737',
      shopStatus: 1,
      businessStatus: 1,
      accountMode: 'new',
      platformUserId: null,
      accountIdentifier: '13800138000',
      accountPassword: '12345678',
      accountPasswordAgain: '12345678'
    })
  })
})
