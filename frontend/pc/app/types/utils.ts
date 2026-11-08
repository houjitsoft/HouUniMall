/**
 * 工具类相关类型定义
 */

/**
 * Cookie 配置选项
 */
export interface CookieOptions {
    path?: string
    domain?: string
    expires?: Date
    maxAge?: number
    secure?: boolean
    sameSite?: 'strict' | 'lax' | 'none'
}

/**
 * 密码格式类型（对应接口返回的 passwordFormat）
 */
export enum PasswordFormat {
    PURE_NUMBER = '1',      // 纯数字
    PURE_LETTER = '2',      // 纯英文
    LETTER_NUMBER = '3',    // 数字英文
    COMPLEX = '4'           // 数字英文符号
}

/**
 * 密码强度等级
 */
export enum PasswordStrength {
    WEAK = 1,       // 弱
    MEDIUM = 2,     // 中等
    STRONG = 3,     // 强
}
