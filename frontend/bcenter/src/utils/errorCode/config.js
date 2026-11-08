/**
 * tcenter 端错误码映射层开关
 *
 * 解决的问题：tcenter 历史上用魔法数字白名单
 * `VALID_CODES = new Set([0, 1, 10060, 11007, 14052, 10047, 16012])` 放行返回值，
 * 其中 0 在后端 CommonStatusEnum 里是 FAIL、11007 是「权限不足」——
 * 同一个 code=0 在 admin 是报错弹窗、在 tcenter 是静默放行，两端表现完全相反。
 *
 * 本轮改造：
 * - 白名单从魔法数字改为按**新码名称**显式声明（见 passAsEmptyNames）；
 * - 11007（权限不足）从放行集合中移除，改为显式 Toast「你没有该操作权限，请联系管理员」；
 * - 0 的放行由 compatZeroAsSuccess 开关兜住，保持现网行为，置 false 即对齐唯一成功码集合。
 */

/**
 * 是否把 code=0 视为成功。
 *
 * tcenter 现网行为是放行 0，故本轮保持 true（避免一刀切打断现有流程）；
 * 灰度观察期结束后置为 false，即与 admin 端、与后端 CommonStatusEnum.FAIL=0 对齐。
 */
export const compatZeroAsSuccess = true

/**
 * 「查询为空」类错误码：命中时按 EMPTY_STATE 放行、不弹 Toast。
 *
 * 与 admin 端保持一致。注意旧码 11007（权限不足）**不在**此集合：
 * 它不是空数据，放行会静默吞掉越权操作，新码 31005 必须显式提示。
 */
export const passAsEmptyNames = [
  'SYSTEM_QUERY_RESULT_EMPTY', // 20007，旧码 10060
  'AFTER_SALE_RECORD_NOT_FOUND', // 43001，旧码 14052
  'MERCHANT_AUDIT_INFO_NOT_FOUND', // 33001，旧码 10047
  'MERCHANT_CONTRACT_TEMPLATE_NOT_FOUND' // 33002，旧码 16012（定义未使用，归属待业务确认）
]

/**
 * 兼容期未认领旧码：码表.md 本轮不认领，但本端语义确定为「登录失效」。
 *
 * tcenter 端历史上没有登录态跳转逻辑（非白名单码一律 Toast 后 reject），故本列表为空。
 * 若后续加入跳登录，只能在这里按旧码显式声明，禁止在拦截器里写裸码。
 */
export const legacyAuthCodes = []

/**
 * 是否启用 message 关键词兜底（兼容期）。
 * tcenter 历史代码没有任何关键词分支，仍保留开关以便灰度期兼容未登记的旧码。
 */
export const enableKeywordFallback = true

/**
 * 是否在控制台打印后端原文。
 * 后端原文一律只进 console.warn，绝不出屏（PRD §6.2）。
 */
export const enableBackendMessageLog = true
