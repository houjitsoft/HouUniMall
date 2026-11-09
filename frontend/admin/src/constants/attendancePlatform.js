/** 考勤打卡来源（与考勤设置 → 打卡配置、后端 AttendancePlatformSource 一致） */
export const ATTENDANCE_PLATFORM_OPTIONS = [
  { value: 'platform', label: '平台', desc: 'HouUniMall 自带考勤打卡与统计' },
  { value: 'dingtalk', label: '钉钉', desc: '从钉钉考勤同步打卡与审批数据' },
  { value: 'wechat-work', label: '企业微信', desc: '从企业微信考勤应用同步数据' },
  { value: 'feishu', label: '飞书', desc: '从飞书考勤模块同步数据' }
]

const LABEL_MAP = Object.fromEntries(
  ATTENDANCE_PLATFORM_OPTIONS.map((item) => [item.value, item.label])
)

/**
 * 获取考勤来源显示名称
 * @param {string} source 平台标识
 * @returns {string}
 */
export function getAttendancePlatformLabel(source) {
  if (!source) return '未知'
  return LABEL_MAP[source] || source
}

/**
 * 是否为可同步员工的第三方考勤来源
 * @param {string} source 平台标识
 * @returns {boolean}
 */
export function isThirdPartyAttendanceSource(source) {
  return source === 'dingtalk' || source === 'wechat-work' || source === 'feishu'
}

/**
 * 同步员工按钮文案：同步员工（企业微信）
 * @param {string} source 平台标识
 * @returns {string}
 */
export function formatSyncEmployeeButtonText(source) {
  const label = getAttendancePlatformLabel(source)
  if (isThirdPartyAttendanceSource(source)) {
    return `同步员工（${label}）`
  }
  return '同步员工'
}

/**
 * 同步考勤按钮文案
 * @param {string} source 平台标识
 * @returns {string}
 */
export function formatSyncAttendanceButtonText(source) {
  const label = getAttendancePlatformLabel(source)
  if (isThirdPartyAttendanceSource(source)) {
    return `同步${label}考勤`
  }
  return '同步考勤'
}

/**
 * 返回同步按钮不可用的原因；空字符串表示允许点击。
 * credentialConfigured 缺失代表后台未提供状态，不能误判为未配置。
 */
export function getAttendanceSyncDisabledReason({
  source,
  enabled = true,
  credentialConfigured,
  loading = false,
  syncing = false
} = {}) {
  const label = getAttendancePlatformLabel(source)
  if (loading) return '正在读取考勤配置，请稍候'
  if (syncing) return '考勤正在同步，请稍候'
  if (!isThirdPartyAttendanceSource(source)) return '当前考勤来源无需第三方同步'
  if (enabled === false) return `${label}同步已暂停，请到考勤设置启用`
  if (credentialConfigured === false) {
    return `${label}凭证未配置，请到考勤设置或系统设置 → 第三方集成中完成配置`
  }
  return ''
}

/**
 * 列表搜索框 placeholder
 * @param {string} source 平台标识
 * @returns {string}
 */
export function getAttendanceListSearchPlaceholder(source) {
  if (source === 'dingtalk') return '姓名 / 钉钉用户ID'
  if (source === 'feishu') return '姓名 / 飞书用户ID'
  if (source === 'wechat-work') return '姓名 / 手机 / 企微ID'
  if (source === 'platform') return '姓名 / 工号'
  return '姓名 / 手机 / 第三方ID'
}

/**
 * 第三方用户ID 列标题
 * @param {string} source 平台标识
 * @returns {string}
 */
export function getAttendanceExternalIdColumnLabel(source) {
  if (source === 'dingtalk') return '钉钉用户ID'
  if (source === 'feishu') return '飞书用户ID'
  if (source === 'wechat-work') return '企微用户ID'
  return '第三方用户ID'
}
