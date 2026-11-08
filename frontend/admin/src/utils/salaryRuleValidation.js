export const validateEnabledSalaryRule = (rule) => {
  if (rule?.isEnabled !== 1) return ''

  if (rule.ruleValue == null) {
    return '请输入规则数值'
  }
  if (!rule.ruleUnit) return '请选择规则单位'
  if (rule.ruleCategory === 'late') {
    const { checkoutTime, graceMinutes } = parseLateRuleParam(rule.ruleParam)
    if (!checkoutTime) return '请输入前日下班时间'
    if (graceMinutes == null) return '请输入次日宽限分钟'
  }
  if (['meal', 'tax'].includes(rule.ruleCategory) && !rule.ruleParam) {
    return rule.ruleCategory === 'tax' ? '请输入税率' : '请输入下班时间'
  }
  if (!rule.ruleFormula) return '请输入计算公式'
  return ''
}

export const buildLateRuleParam = (checkoutTime, graceMinutes) => (
  checkoutTime && graceMinutes != null ? `${checkoutTime}|${graceMinutes}` : `${checkoutTime || ''}|${graceMinutes ?? ''}`
)

export const parseLateRuleParam = (ruleParam) => {
  const [checkoutTime = '', rawGraceMinutes = ''] = String(ruleParam || '').split('|')
  const parsedGraceMinutes = rawGraceMinutes.trim() === '' ? null : Number(rawGraceMinutes)
  return {
    checkoutTime: checkoutTime.trim(),
    graceMinutes: Number.isInteger(parsedGraceMinutes) ? parsedGraceMinutes : null
  }
}
