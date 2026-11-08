/**
 * pc 端错误码映射层开关
 *
 * 解决的问题：pc 端历史上用裸码数组 `CLEAR_USER_CODE_LIST = [10002, 10086, 11002]`
 * 判定「要清登录态」，而这些旧码存在跨枚举冲突（11002 在骑手端语义是「正在审核」），
 * 裸码硬编码会把业务错误误判成 token 失效。现在判定只看 category === 'AUTH'，
 * 未认领但本端语义确定的旧码在 legacyAuthCodes 里显式声明。
 */

/**
 * 是否把 code=0 视为成功。
 *
 * pc 端各接口统一以 1 表示成功，0 是失败（与后端 CommonStatusEnum.FAIL=0 一致），故为 false。
 */
export const compatZeroAsSuccess = false

/**
 * 「查询为空」类错误码：命中时按 EMPTY_STATE 放行、不弹 Toast。
 *
 * 与 admin / cmsadmin 端保持一致。注意旧码 11007（权限不足）**不在**此集合：
 * 它不是空数据，放行会静默吞掉越权操作，新码 31005 必须显式提示。
 */
export const passAsEmptyNames = [
  'SYSTEM_QUERY_RESULT_EMPTY', // 20007，旧码 10060
  'AFTER_SALE_RECORD_NOT_FOUND', // 43001，旧码 14052
  'MERCHANT_AUDIT_INFO_NOT_FOUND', // 33001，旧码 10047
  'MERCHANT_CONTRACT_TEMPLATE_NOT_FOUND' // 33002，旧码 16012（定义未使用，归属待业务确认）
]

/**
 * 兼容期未认领旧码：码表.md 尚未认领，但本端语义确定为「登录失效」。
 *
 * 已清空。原 10086（GlobalExceptionEnum.NO_FORBIDDEN「登录失败，请重新登录」）
 * 本轮已认领为统一新码 **30801 LOGIN_FAILED**，`category = AUTH`，
 * 由 `resolveError()` 直接命中映射表并走 `shouldRedirectLogin()`，不再需要端内声明。
 *
 * 反面例子（永远不要加回来）：11002 在 BizCodeEnume 是「用户不存在」、在骑手端是「正在审核」，
 * 属跨枚举冲突码，本轮已拆成 32001（NOT_FOUND）/ 32801（BIZ），**都不是 AUTH**。
 * 把它当登录失效会让「用户正在审核」被误判成 token 失效清登录态。
 */
export const legacyAuthCodes = []

/**
 * 是否启用 message 关键词兜底（兼容期）。
 * 灰度期后端可能仍返回未登记的旧码，映射表命中不了时用关键词推类别。
 */
export const enableKeywordFallback = true

/**
 * 是否在控制台打印后端原文。
 * 后端原文一律只进 console.warn，绝不出屏（PRD §6.2）。
 */
export const enableBackendMessageLog = true
