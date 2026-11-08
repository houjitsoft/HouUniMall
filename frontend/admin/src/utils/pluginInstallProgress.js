/**
 * 安装/升级流水线步骤文案与进度百分比（购买页与升级对话框共用）。
 */

const STEP_ORDER = [
  'QUEUED',
  'DOWNLOAD',
  'VERIFY',
  'COMPAT',
  'BACKUP',
  'STOP',
  'SCHEMA',
  'START',
  'ACTIVATE',
  'HEALTH',
  'SUCCESS',
]

const STEP_LABEL = {
  QUEUED: '排队中',
  DOWNLOAD: '下载制品',
  VERIFY: '签名校验',
  COMPAT: '兼容性预检',
  BACKUP: '备份当前版本',
  STOP: '停旧版本',
  SCHEMA: 'Schema 迁移',
  START: '启动候选',
  ACTIVATE: '切换正式版',
  HEALTH: '健康检查',
  SUCCESS: '完成',
  FAILED: '失败',
  ROLLED_BACK: '已回滚',
  TICKET: '换票授权',
  PAID: '支付成功',
}

/**
 * @param {Record<string, any>|null|undefined} status
 * @returns {{ step: string, label: string, percent: number, healthOk: boolean|null, healthDetail: string }}
 */
export function describeInstallProgress(status) {
  const step = String(status?.step || status?.data?.step || status?.state || '').toUpperCase()
  const state = String(status?.state || '').toUpperCase()
  const label = STEP_LABEL[step] || STEP_LABEL[state] || (step || '处理中')
  let percent = 8
  const idx = STEP_ORDER.indexOf(step)
  if (idx >= 0) percent = Math.min(98, Math.round(((idx + 1) / STEP_ORDER.length) * 100))
  if (state === 'SUCCESS' || step === 'SUCCESS') percent = 100
  if (state === 'FAILED' || state === 'ROLLED_BACK') percent = Math.max(percent, 40)

  let healthOk = null
  let healthDetail = ''
  if (step === 'HEALTH' || state === 'SUCCESS') {
    healthOk = state === 'SUCCESS' || state === 'HEALTHY' || !status?.errorMsg
    healthDetail = status?.errorMsg || status?.result || (healthOk ? '探活通过' : '探活未通过')
  }
  if (state === 'FAILED' || state === 'ROLLED_BACK') {
    healthOk = false
    healthDetail = status?.errorMsg || status?.result || '未通过'
  }
  return { step: step || state || 'RUNNING', label, percent, healthOk, healthDetail }
}

export { STEP_LABEL, STEP_ORDER }
