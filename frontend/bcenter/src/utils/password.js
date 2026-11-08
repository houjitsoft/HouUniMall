/** 密码格式：1-纯数字;2-纯英文;3-数字英文;4-数字英文符号 */
export const PasswordFormat = {
  PURE_NUMBER: '1',
  PURE_LETTER: '2',
  LETTER_NUMBER: '3',
  COMPLEX: '4',
}

const PASSWORD_RULES = {
  [PasswordFormat.PURE_NUMBER]: /^[0-9]+$/,
  [PasswordFormat.PURE_LETTER]: /^[a-zA-Z]+$/,
  [PasswordFormat.LETTER_NUMBER]: /^[a-zA-Z0-9]+$/,
  [PasswordFormat.COMPLEX]:
    /^[a-zA-Z0-9!@#$%^&*()_+\-=\[\]{};':"\\|,.<>\/?]+$/,
}

/**
 * 密码校验（与 PC PasswordInput / utils/password 规则一致）
 * @param {string} password
 * @param {string} passwordFormat
 * @param {string} passwordLength
 * @param {string} [desc]
 */
export function validatePassword(
  password,
  passwordFormat,
  passwordLength,
  desc
) {
  const minLength = parseInt(passwordLength, 10) || 6

  if (!password || password.length < minLength) {
    return {
      valid: false,
      message: desc || `密码长度不能少于${minLength}个字符`,
    }
  }

  const rule = PASSWORD_RULES[passwordFormat]
  if (!rule) {
    return {
      valid: false,
      message: desc || '密码格式配置错误',
    }
  }

  if (!rule.test(password)) {
    const formatDescriptions = {
      [PasswordFormat.PURE_NUMBER]: '纯数字',
      [PasswordFormat.PURE_LETTER]: '纯英文',
      [PasswordFormat.LETTER_NUMBER]: '数字和英文',
      [PasswordFormat.COMPLEX]: '数字、英文和符号',
    }
    const formatDesc = formatDescriptions[passwordFormat] || '指定格式'
    return {
      valid: false,
      message:
        desc ||
        `密码格式不正确，应为${formatDesc}的组合，长度至少${minLength}位`,
    }
  }

  if (passwordFormat === PasswordFormat.LETTER_NUMBER) {
    const hasNumber = /\d/.test(password)
    const hasLetter = /[a-zA-Z]/.test(password)
    if (!hasNumber || !hasLetter) {
      return {
        valid: false,
        message:
          desc || `密码必须同时包含数字和英文字母，长度至少${minLength}位`,
      }
    }
  }

  if (passwordFormat === PasswordFormat.COMPLEX) {
    const hasNumber = /\d/.test(password)
    const hasLetter = /[a-zA-Z]/.test(password)
    if (!hasNumber || !hasLetter) {
      return {
        valid: false,
        message:
          desc || `密码必须同时包含数字和英文字母，长度至少${minLength}位`,
      }
    }
  }

  return {
    valid: true,
    message: '密码格式符合要求',
  }
}
