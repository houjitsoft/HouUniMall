import { PasswordFormat, PasswordStrength } from '~/types/utils'

// 重新导出类型，保持向后兼容
export { PasswordFormat, PasswordStrength }

// 密码校验规则（根据接口返回的格式）
const PASSWORD_RULES: Record<string, RegExp> = {
    [PasswordFormat.PURE_NUMBER]: /^[0-9]+$/,                    // 纯数字
    [PasswordFormat.PURE_LETTER]: /^[a-zA-Z]+$/,                 // 纯英文
    [PasswordFormat.LETTER_NUMBER]: /^[a-zA-Z0-9]+$/,            // 数字英文
    [PasswordFormat.COMPLEX]: /^[a-zA-Z0-9!@#$%^&*()_+\-=\[\]{};':"\\|,.<>\/?]+$/,  // 数字英文符号
};

/**
 * 密码校验函数（根据接口返回的规则）
 * @param password 密码字符串
 * @param passwordFormat 密码格式（接口返回的 passwordFormat: "1"-纯数字;"2"-纯英文;"3"-数字英文;"4"-数字英文符号）
 * @param passwordLength 最小长度（接口返回的 passwordLength，字符串类型）
 * @param desc 密码规则描述（接口返回的 desc，用于错误提示）
 * @returns 校验结果
 */
export const validatePassword = (
    password: string,
    passwordFormat: string,
    passwordLength: string,
    desc?: string
): {
    valid: boolean;
    message: string;
} => {
    // 转换为数字类型的最小长度
    const minLength = parseInt(passwordLength, 10) || 6

    // 检查密码长度
    if (password.length < minLength) {
        return {
            valid: false,
            message: desc || `密码长度不能少于${minLength}个字符`,
        };
    }

    // 获取对应的验证规则
    const rule = PASSWORD_RULES[passwordFormat]
    if (!rule) {
        return {
            valid: false,
            message: desc || '密码格式配置错误',
        };
    }

    // 检查密码格式是否符合要求
    if (!rule.test(password)) {
        // 根据格式类型生成错误提示
        const formatDescriptions: Record<string, string> = {
            [PasswordFormat.PURE_NUMBER]: '纯数字',
            [PasswordFormat.PURE_LETTER]: '纯英文',
            [PasswordFormat.LETTER_NUMBER]: '数字和英文',
            [PasswordFormat.COMPLEX]: '数字、英文和符号',
        };

        const formatDesc = formatDescriptions[passwordFormat] || '指定格式'
        return {
            valid: false,
            message: desc || `密码格式不正确，应为${formatDesc}的组合，长度至少${minLength}位`,
        };
    }

    // 对于格式"3"（数字英文），需要同时包含数字和英文字母
    if (passwordFormat === PasswordFormat.LETTER_NUMBER) {
        const hasNumber = /\d/.test(password)
        const hasLetter = /[a-zA-Z]/.test(password)
        
        if (!hasNumber || !hasLetter) {
            return {
                valid: false,
                message: desc || `密码必须同时包含数字和英文字母，长度至少${minLength}位`,
            };
        }
    }

    // 对于格式"4"（数字英文符号），需要同时包含数字和英文字母（符号可选）
    if (passwordFormat === PasswordFormat.COMPLEX) {
        const hasNumber = /\d/.test(password)
        const hasLetter = /[a-zA-Z]/.test(password)
        
        if (!hasNumber || !hasLetter) {
            return {
                valid: false,
                message: desc || `密码必须同时包含数字和英文字母，长度至少${minLength}位`,
            };
        }
    }

    return {
        valid: true,
        message: '密码格式符合要求',
    };
};

/**
 * 获取密码格式描述
 * @param format 密码格式（接口返回的 passwordFormat）
 * @returns 格式描述
 */
export const getPasswordFormatDescription = (format: string): string => {
    const formatDescriptions: Record<string, string> = {
        [PasswordFormat.PURE_NUMBER]: '纯数字',
        [PasswordFormat.PURE_LETTER]: '纯英文',
        [PasswordFormat.LETTER_NUMBER]: '数字和英文',
        [PasswordFormat.COMPLEX]: '数字、英文和符号',
    };
    return formatDescriptions[format] || '未知格式';
};

/**
 * 获取密码强度描述
 * @param strength 密码强度
 * @returns 强度描述
 */
export const getPasswordStrengthDescription = (strength: PasswordStrength): string => {
    const strengthDescriptions = {
        [PasswordStrength.WEAK]: '弱',
        [PasswordStrength.MEDIUM]: '中等',
        [PasswordStrength.STRONG]: '强',
    };
    return strengthDescriptions[strength];
};
