export const DOUYIN_REVIEW_DEMO_ACCOUNT = Object.freeze({
  accountName: '抖音账号',
  isReviewDemo: true
})

export function isDouyinReviewDemoEnabled(platform, reviewDemo) {
  return platform === 'douyin' && String(reviewDemo) === '1'
}

export function resolveDouyinReviewAccounts(accounts, reviewDemoEnabled) {
  const realAccounts = Array.isArray(accounts) ? accounts : []
  if (realAccounts.length || !reviewDemoEnabled) return realAccounts
  return [DOUYIN_REVIEW_DEMO_ACCOUNT]
}

export function shouldInterceptDouyinReviewPublish(reviewDemoEnabled, accountName) {
  return reviewDemoEnabled && accountName === DOUYIN_REVIEW_DEMO_ACCOUNT.accountName
}

export function canContinueDouyinReviewFlow(reviewDemoEnabled, detectResult) {
  if (!detectResult) return false
  return reviewDemoEnabled || detectResult.blocked !== true
}
