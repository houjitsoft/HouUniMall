export const auditStatusPresentation = Object.freeze({
  PASSED: { label: '已通过', type: 'success' },
  RISK: { label: '有风险', type: 'danger' },
  PENDING_SUBMISSION: { label: '待提交', type: 'warning' },
  PENDING_VERIFICATION: { label: '待验证', type: 'info' },
  FAILED: { label: '检测失败', type: 'danger' },
})

export function presentAuditStatus(status) {
  return auditStatusPresentation[status] || auditStatusPresentation.PENDING_VERIFICATION
}
