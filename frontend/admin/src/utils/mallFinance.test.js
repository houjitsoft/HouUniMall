import { describe, expect, it } from 'vitest'
import {
  compactQuery,
  formatDateTime,
  formatMoney,
  normalizePage,
  pickValue,
  financeStatus,
  unwrapData
} from './mallFinance.js'

describe('商城财务响应适配', () => {
  it('兼容 MyBatis Plus 分页结构', () => {
    expect(normalizePage({ records: [{ id: 1 }], total: 21, current: 2, size: 10 })).toEqual({
      list: [{ id: 1 }], total: 21, page: 2, size: 10, extend: {}
    })
  })

  it('兼容 LikeShop 分页结构', () => {
    expect(normalizePage({ lists: [{ id: 2 }], count: 8, page_no: 1, page_size: 20, extend: { wait: 3 } })).toEqual({
      list: [{ id: 2 }], total: 8, page: 1, size: 20, extend: { wait: 3 }
    })
  })

  it('从 BFF 响应中安全取数据并处理空响应', () => {
    expect(unwrapData({ code: 200, data: { totalIncome: 10 } })).toEqual({ totalIncome: 10 })
    expect(normalizePage()).toEqual({ list: [], total: 0, page: 1, size: 10, extend: {} })
  })

  it('统一格式化金额和时间', () => {
    expect(formatMoney(1234.5)).toBe('1,234.50')
    expect(formatMoney(null)).toBe('0.00')
    expect(formatDateTime(1710000000)).toMatch(/^2024-/)
    expect(formatDateTime('2026-09-08T10:00:00')).toBe('2026-09-08 10:00:00')
    expect(formatDateTime(null)).toBe('-')
  })

  it('请求参数忽略空筛选值但保留数字零', () => {
    expect(compactQuery({ status: '', keyword: '  ', page: 1, pay_status: 0, account_type: 'balance' })).toEqual({
      page: 1,
      pay_status: 0,
      account_type: 'balance'
    })
  })

  it('按 LikeShop 退款状态定义显示平台退款记录', () => {
    expect(financeStatus(0, 'refund')).toEqual({ text: '退款中', type: 'warning' })
    expect(financeStatus(1, 'refund')).toEqual({ text: '退款成功', type: 'success' })
    expect(financeStatus(2, 'refund')).toEqual({ text: '退款失败', type: 'danger' })
  })

  it('可读取退款记录中的嵌套买家字段', () => {
    expect(pickValue({ user: { nickname: '张三' } }, ['user.nickname', 'nickname'])).toBe('张三')
  })
})
