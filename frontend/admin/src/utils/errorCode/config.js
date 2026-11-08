/**
 * admin 端错误码映射层开关
 *
 * 解决的问题：两端判定互相矛盾（admin 判 0 为失败、cmsadmin 判 0 为成功）。
 * 把差异收敛到本文件的开关里，映射层逻辑本身两端完全一致。
 */

/**
 * 是否把 code=0 视为成功。
 *
 * admin 端历史上把 0 当失败（与后端 CommonStatusEnum.FAIL=0 一致），故保持 false；
 * cmsadmin 端为保持现网行为设为 true。收敛到唯一成功码集合时改为 false。
 */
export const compatZeroAsSuccess = false

/**
 * 「查询为空」类错误码：命中时按 EMPTY_STATE 放行、不弹 Toast。
 *
 * 解决的问题：cmsadmin 历史上用魔法数字白名单 VALID_CODES=[0,1,10060,11007,14052,10047,16012]
 * 放行这些码。这里改为按**新码名称**显式声明，杜绝魔法数字，且与后端枚举同源。
 *
 * 注意：旧码 11007（权限不足）已从这里移除 —— 它不是空数据，放行会静默吞掉越权操作，
 * 新码 31005 必须显式提示「你没有该操作权限，请联系管理员」。
 */
export const passAsEmptyNames = [
  'SYSTEM_QUERY_RESULT_EMPTY', // 20007，旧码 10060
  'AFTER_SALE_RECORD_NOT_FOUND', // 43001，旧码 14052
  'MERCHANT_AUDIT_INFO_NOT_FOUND', // 33001，旧码 10047
  'MERCHANT_CONTRACT_TEMPLATE_NOT_FOUND' // 33002，旧码 16012（定义未使用，归属待业务确认）
]

/**
 * 端内显式声明的登录失效码：这些旧码**无法靠映射表自动判定**，必须由本端声明。
 *
 * 解决的问题：admin 端历史上直接判 `res.code === 11006 || res.code === 401` 就清 token 跳登录，
 * 裸码散落在拦截器里。这里收敛成本端显式声明，语义仍由生成映射表出（走 AUTH 类别兜底文案）。
 *
 * ## 11006 为什么必须留在这里（不要删）
 *
 * 11006 是**歧义旧码**：被两个新码共同认领——
 * - 30802 `USER_NOT_LOGGED_IN`（AUTH）：admin 端语义「用户未登录」
 * - 35010 `RIDER_WITHDRAW_ACCOUNT_INVALID`（VALIDATION）：骑手端语义「只能提现到本人账户」
 *
 * 后端刻意让它保持 AMBIGUOUS、不产出 1:1 自动映射（若一律映射成「未登录」，
 * 骑手提现填错账户就会被踢出登录页，bug 原样复现）。歧义码在映射表里查不到唯一结果，
 * 所以**只能由端内按本端语义显式声明**，admin 端=未登录→跳登录，骑手端=提现账户错误→不跳。
 *
 * ## 401 为什么留在这里
 *
 * HTTP 未授权状态码被当业务码用（全站 948 处中的一处），不属业务码体系、不编新码，
 * admin 端历史行为等同登录失效，按 HTTP 语义直接判定即可。
 *
 * **禁止**把 35010 复制到 admin 端；**禁止**把 11006 复制到骑手端。
 */
export const legacyAuthCodes = [
  11006, // 歧义码：admin 端语义=未登录（30802），骑手端语义=只能提现到本人账户（35010）
  401 // HTTP 未授权，admin 端历史行为等同登录失效
]

/**
 * 是否启用 message 关键词兜底（兼容期）。
 *
 * 灰度期后端可能仍返回旧码，映射表命中不了，此时用关键词推类别。
 * 新码全量切换后置为 false 下线。
 */
export const enableKeywordFallback = true

/**
 * 是否在控制台打印后端原文。
 * 后端原文一律只进 console.warn，绝不出屏（PRD §6.2）。
 */
export const enableBackendMessageLog = true
