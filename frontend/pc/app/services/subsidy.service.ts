import type { SubsidyQuote } from '~/utils/subsidy'

/**
 * 国补接口客户端。
 *
 * <p>插件接口统一走宿主分发端点 `/plugin/{code}/{path}`，
 * 未安装国补插件时该端点不存在——因此所有调用都<b>吞掉异常并返回 null</b>，
 * 让页面按「无国补」正常渲染，而不是把整个商品页/结算页打挂。
 */

/** 插件分发基址。 */
const BASE = '/plugin/national-subsidy'

function headers(): Record<string, string> {
  const token = useCookie<string | undefined>('token')
  return token.value ? { Authorization: `Bearer ${token.value}` } : {}
}

async function post(path: string, body: unknown): Promise<any> {
  try {
    return await $fetch(`${BASE}${path}`, {
      method: 'POST',
      body: body as Record<string, unknown>,
      headers: headers()
    })
  } catch (error) {
    // 插件未安装 / 无权限 / 网络异常：统一按「无国补」处理，不影响主流程
    console.warn(`[national-subsidy] ${path} 调用失败，按无国补处理`, error)
    return null
  }
}

/** 试算入参。 */
export interface SubsidyQuoteParams {
  orderNo?: string | null
  shopId?: number | null
  provinceCode?: string | null
  hasCoupon?: boolean
  hasPromotion?: boolean
  hasPoint?: boolean
  items: Array<{
    goodsId: number | null
    categoryId: string | null
    quantity: number
    unitPrice: number | string
    provinceCode?: string | null
  }>
}

/** 试算（商品详情 / 购物车 / 结算页共用）。未安装插件时返回 null。 */
export async function fetchSubsidyQuote(params: SubsidyQuoteParams): Promise<SubsidyQuote | null> {
  const response = await post('/front/quote', params)
  return (response?.data ?? response ?? null) as SubsidyQuote | null
}

/** 查询本人实名状态。 */
export async function fetchRealnameStatus(): Promise<{
  verified: boolean
  realName?: string | null
  idCardMask?: string | null
} | null> {
  try {
    const response = await $fetch(`${BASE}/front/realname/status`, { headers: headers() })
    return (response?.data ?? response ?? null) as any
  } catch (error) {
    console.warn('[national-subsidy] 实名状态查询失败', error)
    return null
  }
}

/** 提交实名认证。 */
export async function submitRealname(payload: { realName: string; idCard: string }): Promise<boolean> {
  const response = await post('/front/realname/submit', payload)
  return Boolean(response?.data ?? response)
}
