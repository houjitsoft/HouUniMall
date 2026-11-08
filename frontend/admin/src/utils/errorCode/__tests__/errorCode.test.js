/**
 * 统一错误码映射层单测（admin 端）
 *
 * 覆盖：
 * 1. 成功码集合收敛（0 在 admin 端是失败、1/200 是成功）
 * 2. 命中新码 → 出 userMessage；exposeToUser=false → 出 category 兜底
 * 3. 旧码 → 新码归一（11007 → 31005 等）
 * 4. 未命中 → 按 category 兜底，绝不直出后端原文
 * 5. 跳登录只由 AUTH 类别触发
 * 6. 「查询为空」放行集合：4 个空数据码放行、11007/31005 不放行
 * 7. 脱敏正则：SQL / 堆栈 / 内部 URL / token 一律判定为不可出屏
 */
import { describe, it, expect } from 'vitest'
import {
  isSuccessCode,
  normalizeCode,
  codeOfName,
  resolveError,
  shouldPassAsEmpty,
  shouldRedirectLogin,
  PASS_AS_EMPTY
} from '../index.js'
import {
  CATEGORY_TEXT,
  sanitizeBackendMessage,
  inferCategoryByKeyword
} from '../categoryFallback.js'
import {
  LEGACY_TO_NEW,
  AMBIGUOUS_LEGACY_CODES,
  AMBIGUOUS_CANDIDATES
} from '../errorCodeMap.generated.js'

describe('生成映射表不得出现重复 key（歧义码隔离）', () => {
  it('LEGACY_TO_NEW 与 AMBIGUOUS_LEGACY_CODES 交集必须为空', () => {
    // JS 对象字面量里重复 key 后者覆盖前者：若歧义旧码同时写进 LEGACY_TO_NEW，
    // 11006 会被静默判成骑手语义 35010（VALIDATION，不跳登录），
    // 而 admin 端 11006 的真身是「用户未登录」（30802，AUTH，必须跳登录）。
    for (const legacy of AMBIGUOUS_LEGACY_CODES) {
      expect(LEGACY_TO_NEW[legacy]).toBeUndefined()
    }
  })

  it('歧义旧码的候选全部登记在 AMBIGUOUS_CANDIDATES', () => {
    expect([...AMBIGUOUS_CANDIDATES[11006]].sort((a, b) => a - b)).toEqual([30802, 35010])
    expect([...AMBIGUOUS_CANDIDATES[11002]].sort((a, b) => a - b)).toEqual([32001, 32801])
  })

  it('10016 → 30803 已就位，且仍判为登录失效', () => {
    expect(normalizeCode(10016)).toBe(codeOfName('TOKEN_INVALID'))
    expect(normalizeCode(10016)).toBe(30803)
    expect(shouldRedirectLogin(resolveError(10016))).toBe(true)
  })

  it('10012（系统异常）归一到 SYSTEM 域，且绝不判为登录失效', () => {
    // 10012 = BizCodeEnume.SYSTEM_ERROR「系统异常，请联系客服」，与认证无关。
    // 它曾与 10016 / 10086 并列进 rider / sellerapp 的未授权数组，已由前端移除。
    // 第二阶段重构后它被正式认领到 SystemErrorCode.UNKNOWN_ERROR，
    // 这里锁的是「认领可以，但必须落在 SYSTEM 域、不能触发跳登录」。
    expect(normalizeCode(10012)).toBe(codeOfName('UNKNOWN_ERROR'))
    expect(normalizeCode(10012)).toBe(20010)
    expect(shouldRedirectLogin(resolveError(10012))).toBe(false)
    expect(resolveError(10012).category).toBe('SYSTEM')
    expect(AMBIGUOUS_LEGACY_CODES).not.toContain(10012)
  })

  it('同码多义旧码（11009）不进 LEGACY_TO_NEW，由端内显式取候选', () => {
    // 11009 既可能是「数据库访问失败」(20601)，也可能是认证侧语义 (30009)。
    // 11006 的教训：盲猜一个会把用户误踢下线，所以只登记候选、不出 1:1 映射。
    expect(LEGACY_TO_NEW[11009]).toBeUndefined()
    expect(AMBIGUOUS_CANDIDATES[11009]).toContain(20601)
    expect(normalizeCode(11009)).toBeNull()
    expect(shouldRedirectLogin(resolveError(11009))).toBe(false)
  })
})

describe('成功码集合收敛', () => {
  it('1 与 200 是成功', () => {
    expect(isSuccessCode(1)).toBe(true)
    expect(isSuccessCode(200)).toBe(true)
  })

  it('admin 端 0 是失败（不与 cmsadmin 的放行行为混淆）', () => {
    expect(isSuccessCode(0)).toBe(false)
  })

  it('新码段内的错误码不是成功', () => {
    expect(isSuccessCode(20010)).toBe(false)
    expect(isSuccessCode(31005)).toBe(false)
  })
})

describe('码值归一', () => {
  it('新码直接命中', () => {
    expect(normalizeCode(31005)).toBe(31005)
  })

  it('旧码归一到新码', () => {
    expect(normalizeCode(11007)).toBe(codeOfName('PERMISSION_ACCESS_DENIED'))
    expect(normalizeCode(10060)).toBe(codeOfName('SYSTEM_QUERY_RESULT_EMPTY'))
    expect(normalizeCode(14052)).toBe(codeOfName('AFTER_SALE_RECORD_NOT_FOUND'))
    expect(normalizeCode(10047)).toBe(codeOfName('MERCHANT_AUDIT_INFO_NOT_FOUND'))
    expect(normalizeCode(16012)).toBe(codeOfName('MERCHANT_CONTRACT_TEMPLATE_NOT_FOUND'))
  })

  it('歧义旧码无法归一（本轮不认领）', () => {
    expect(normalizeCode(15010)).toBeNull()
    expect(normalizeCode(11006)).toBeNull()
  })
})

describe('映射命中与兜底', () => {
  it('命中新码时出映射文案', () => {
    const resolved = resolveError(31005)
    expect(resolved.matched).toBe(true)
    expect(resolved.category).toBe('PERMISSION')
    expect(resolved.text).toBe('你没有该操作权限，请联系管理员')
  })

  it('exposeToUser=false 的码一律出 category 兜底文案', () => {
    const code = codeOfName('PAYMENT_REFUND_FAILED')
    const resolved = resolveError(code)
    expect(resolved.exposeToUser).toBe(false)
    expect(resolved.text).toBe(CATEGORY_TEXT.SYSTEM)
    expect(resolved.text).not.toBe('退款失败，请联系客服处理')
  })

  it('未命中时按关键词推类别兜底，不直出后端原文', () => {
    const resolved = resolveError(15010, { backendMessage: '用户未登录，请先登录' })
    expect(resolved.matched).toBe(false)
    expect(resolved.category).toBe('AUTH')
    expect(resolved.text).toBe(CATEGORY_TEXT.AUTH)
  })

  it('既未命中也无关键词时降级为 SYSTEM 兜底', () => {
    const resolved = resolveError(15010, { backendMessage: '' })
    expect(resolved.category).toBe('SYSTEM')
    expect(resolved.text).toBe(CATEGORY_TEXT.SYSTEM)
  })
})

describe('跳登录只由 AUTH 类别触发', () => {
  it('AUTH 类别触发跳登录', () => {
    expect(shouldRedirectLogin(resolveError(codeOfName('TOKEN_EXPIRED')))).toBe(true)
  })

  it('PERMISSION 类别不跳登录（停留原页）', () => {
    expect(shouldRedirectLogin(resolveError(31005))).toBe(false)
  })

  it('SYSTEM 类别不跳登录', () => {
    expect(shouldRedirectLogin(resolveError(20010))).toBe(false)
  })
})

describe('查询为空放行集合', () => {
  it('4 个空数据新码放行', () => {
    expect(shouldPassAsEmpty(codeOfName('SYSTEM_QUERY_RESULT_EMPTY'))).toBe(true)
    expect(shouldPassAsEmpty(codeOfName('AFTER_SALE_RECORD_NOT_FOUND'))).toBe(true)
    expect(shouldPassAsEmpty(codeOfName('MERCHANT_AUDIT_INFO_NOT_FOUND'))).toBe(true)
    expect(shouldPassAsEmpty(codeOfName('MERCHANT_CONTRACT_TEMPLATE_NOT_FOUND'))).toBe(true)
  })

  it('旧码 10060 / 14052 / 10047 / 16012 归一后仍放行', () => {
    expect(shouldPassAsEmpty(10060)).toBe(true)
    expect(shouldPassAsEmpty(14052)).toBe(true)
    expect(shouldPassAsEmpty(10047)).toBe(true)
    expect(shouldPassAsEmpty(16012)).toBe(true)
  })

  it('权限不足（旧码 11007 / 新码 31005）不再放行', () => {
    expect(shouldPassAsEmpty(11007)).toBe(false)
    expect(shouldPassAsEmpty(31005)).toBe(false)
    expect(PASS_AS_EMPTY.size).toBe(4)
  })
})

describe('resolveError 接线四要素', () => {
  it('① 新码命中：返回码值语义七要素，且是映射文案', () => {
    const r = resolveError(31005)
    expect(r.matched).toBe(true)
    expect(r.code).toBe(31005)
    expect(r.name).toBe('PERMISSION_ACCESS_DENIED')
    expect(r.text).toBe('你没有该操作权限，请联系管理员')
    expect(r.level).toBe('WARN')
    expect(r.retryable).toBe(false)
    expect(r.category).toBe('PERMISSION')
    expect(r.uiAction).toBe('TOAST')
  })

  it('② 旧码 legacy 反查命中：归一到新码后出映射语义', () => {
    const r = resolveError(11007)
    expect(r.matched).toBe(true)
    expect(r.code).toBe(31005)
    expect(r.legacyCode).toBe(11007)
    expect(r.name).toBe('PERMISSION_ACCESS_DENIED')
    expect(r.text).toBe('你没有该操作权限，请联系管理员')
  })

  it('③ 查不到时兜底不崩：原始码带进返回值，走 SYSTEM 文案', () => {
    const r = resolveError(999999, { backendMessage: 'select * from t where id=1' })
    expect(r.matched).toBe(false)
    expect(r.code).toBe(999999)
    expect(r.category).toBe('SYSTEM')
    expect(r.text).toBe(CATEGORY_TEXT.SYSTEM)
    expect(r.text).not.toContain('select')
  })

  it('④ 0 与 1 的成功判定保持向后兼容', () => {
    expect(isSuccessCode(1)).toBe(true)
    expect(isSuccessCode(200)).toBe(true)
    // admin 端 0 是 FAIL，与后端 CommonStatusEnum 一致
    expect(isSuccessCode(0)).toBe(false)
  })
})

describe('兼容期未认领旧码的端内声明', () => {
  it('admin 端声明的 11006 / 401 判为登录失效', () => {
    expect(shouldRedirectLogin(resolveError(11006))).toBe(true)
    expect(shouldRedirectLogin(resolveError(401))).toBe(true)
  })

  it('跨枚举冲突旧码 11002 不被当成登录失效', () => {
    expect(shouldRedirectLogin(resolveError(11002))).toBe(false)
  })
})

describe('后端原文脱敏', () => {
  it('SQL 与异常类名判定为不可出屏', () => {
    expect(sanitizeBackendMessage('select * from user where id = 1').safe).toBe(false)
    expect(sanitizeBackendMessage('BadSqlGrammarException: syntax error').safe).toBe(false)
    expect(sanitizeBackendMessage('NullPointerException at com.houjiang.A.b(A.java:12)').safe).toBe(false)
  })

  it('内部 URL / 路径 / 凭据判定为不可出屏', () => {
    expect(sanitizeBackendMessage('调用 http://10.0.0.1:8080/web 失败').safe).toBe(false)
    expect(sanitizeBackendMessage('D:\\data\\upload\\a.jar 不存在').safe).toBe(false)
    expect(sanitizeBackendMessage('Authorization 校验失败').safe).toBe(false)
  })

  it('干净的业务文案可以出屏', () => {
    expect(sanitizeBackendMessage('手机号已存在').safe).toBe(true)
  })

  it('空消息不可出屏', () => {
    expect(sanitizeBackendMessage('').safe).toBe(false)
  })
})

describe('关键词推类别', () => {
  it('推不出时返回 null', () => {
    expect(inferCategoryByKeyword('')).toBeNull()
    expect(inferCategoryByKeyword('一个没有任何关键词的错误')).toBeNull()
  })

  it('能推出常见类别', () => {
    expect(inferCategoryByKeyword('权限不足')).toBe('PERMISSION')
    expect(inferCategoryByKeyword('服务暂时不可用')).toBe('UPSTREAM')
    expect(inferCategoryByKeyword('该记录不存在')).toBe('NOT_FOUND')
  })
})
