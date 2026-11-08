const ISSUE_PRESENTATIONS = {
  http_status: {
    title: '页面无法正常打开',
    cause: '网站返回了异常访问状态，可能是页面不存在、服务器报错或跳转配置错误。',
    impact: 'AI 搜索和搜索引擎无法稳定读取这个页面，页面通常不会被收录。',
    action: '检查页面是否能在浏览器正常打开，并让技术人员修复状态码或错误跳转。',
  },
  non_html: {
    title: '检测地址不是网页',
    cause: '当前地址返回的不是可阅读的 HTML 网页，可能指向文件、接口或下载资源。',
    impact: 'AI 搜索无法把它当作官网内容进行理解和引用。',
    action: '改用真实的官网页面地址重新检测，例如网站首页、产品页或文章页。',
  },
  robots_blocked: {
    title: 'AI 搜索被禁止访问',
    cause: '网站的 robots.txt 规则禁止了当前选择的 AI 搜索爬虫。',
    impact: '对应 AI 平台无法读取页面内容，也就很难在回答中引用您的网站。',
    action: '让网站技术人员在 robots.txt 中允许 GPTBot，或允许本次检测所选的爬虫。',
  },
  meta_noindex: {
    title: '页面设置了禁止收录',
    cause: '页面代码中包含 noindex 指令，明确要求搜索平台不要收录。',
    impact: '即使页面能打开，搜索引擎和部分 AI 搜索也会放弃收录。',
    action: '确认该页面应公开后，移除页面中的 noindex 指令，再重新检测。',
  },
  missing_title: {
    title: '页面缺少标题',
    cause: '页面代码中没有清晰的网页标题。',
    impact: '搜索平台难以准确判断页面主题，搜索结果的标题也可能异常。',
    action: '补充一个准确、唯一并包含核心业务词的页面标题。',
  },
  missing_description: {
    title: '页面缺少内容摘要',
    cause: '页面没有提供用于概括内容的 description。',
    impact: '搜索平台理解页面和生成搜索摘要时缺少重要信息。',
    action: '补充一段自然概括页面价值的内容摘要，建议控制在 70～160 个汉字。',
  },
  missing_h1: {
    title: '页面缺少主标题',
    cause: '页面正文中没有明确的一级主标题。',
    impact: 'AI 搜索和搜索引擎不容易快速识别页面的核心主题。',
    action: '为页面补充一个清晰且唯一的 H1 主标题。',
  },
  heading_level_jump: {
    title: '页面标题层级混乱',
    cause: '正文标题没有按一级、二级、三级的顺序组织，存在跨级。',
    impact: '页面结构不够清楚，会降低 AI 对内容层次和重点的理解。',
    action: '按 H1、H2、H3 的内容层级重新整理标题，不要跳级使用。',
  },
  invalid_json_ld: {
    title: '结构化数据格式错误',
    cause: '页面中的 JSON-LD 结构化数据存在语法或字段错误。',
    impact: '搜索平台无法正确识别企业、产品、文章或常见问题等内容类型。',
    action: '让技术人员修复 JSON-LD 语法和必填字段，并重新验证。',
  },
  missing_canonical: {
    title: '页面未声明标准网址',
    cause: '页面没有声明 canonical 标准网址。',
    impact: '存在多个相似网址时，搜索平台可能无法判断应收录哪一个版本。',
    action: '让技术人员为页面补充指向当前正式网址的 canonical 标签。',
  },
  not_indexable: {
    title: '页面当前不可收录',
    cause: '检测结果显示页面未满足抓取和收录条件，但没有返回更具体的原因。',
    impact: 'AI 搜索和搜索引擎可能无法收录或引用这个页面。',
    action: '把技术详情交给网站技术人员排查，修复后重新检测。',
  },
}

const LEVELS = {
  high: { label: '必须处理', type: 'danger' },
  medium: { label: '建议处理', type: 'warning' },
  low: { label: '可以优化', type: 'info' },
}

function issueKey(issue = {}) {
  return issue.code || `${issue.severity || 'unknown'}:${issue.message || ''}`
}

export function presentAuditIssue(issue = {}, recommendation = '') {
  const known = ISSUE_PRESENTATIONS[issue.code]
  const level = LEVELS[issue.severity] || LEVELS.medium
  return {
    key: issueKey(issue),
    code: issue.code || 'unknown',
    severity: issue.severity || 'medium',
    levelLabel: level.label,
    levelType: level.type,
    title: known?.title || '页面存在待优化项',
    cause: known?.cause || issue.message || '检测发现页面存在一项尚未通过的规则。',
    impact: known?.impact || '可能影响搜索平台理解、收录或引用这个页面。',
    action: recommendation || known?.action || '请让网站技术人员根据技术详情完成处理，然后重新检测。',
    rawMessage: issue.message || '',
  }
}

function check(state, label, detail) {
  return { state, label, detail }
}

function buildChecks(result, evidence) {
  const statusCode = Number(result.statusCode ?? evidence.statusCode)
  const contentType = String(evidence.contentType || '').toLowerCase()
  const metaRobots = String(evidence.metaRobots || '').toLowerCase()
  return [
    Number.isFinite(statusCode)
      ? check(statusCode >= 200 && statusCode < 300 ? 'passed' : 'needs-action', '网站可以正常打开', statusCode >= 200 && statusCode < 300 ? '访问正常' : '网站返回异常状态')
      : check('unavailable', '网站可以正常打开', '没有取得访问结果'),
    contentType
      ? check(contentType.includes('html') ? 'passed' : 'needs-action', '检测地址是正常网页', contentType.includes('html') ? '网页格式正常' : '当前地址不是 HTML 网页')
      : check('unavailable', '检测地址是正常网页', '没有取得页面格式'),
    evidence.robotsAllowed === true
      ? check('passed', 'AI 搜索可以访问', '网站未禁止当前检测目标')
      : evidence.robotsAllowed === false
        ? check('needs-action', 'AI 搜索可以访问', '网站规则禁止了当前检测目标')
        : check('unavailable', 'AI 搜索可以访问', '没有取得访问权限结果'),
    metaRobots
      ? check(metaRobots.includes('noindex') ? 'needs-action' : 'passed', '页面允许被收录', metaRobots.includes('noindex') ? '页面设置了禁止收录' : '未设置禁止收录')
      : check('passed', '页面允许被收录', '未发现禁止收录指令'),
    evidence.canonical
      ? check('passed', '标准网址已声明', '搜索平台可以识别正式网址')
      : check('needs-action', '标准网址已声明', '建议补充标准网址声明'),
  ]
}

export function buildAuditPresentation(result = {}) {
  const evidence = result.evidence || {}
  const runFailed = String(result.runStatus || '').toLowerCase() === 'failed'
  if (runFailed) {
    return {
      state: 'unavailable',
      title: '暂时无法完成检测',
      summary: '本次检测没有取得完整结果，请检查网址是否可访问后重新检测。',
      issues: [],
      checks: buildChecks(result, evidence),
    }
  }

  const recommendations = new Map(
    (result.recommendations || []).map(item => [item.code, item.message]),
  )
  const rawIssues = [...(result.issues || [])]
  if (!evidence.canonical && (result.statusCode || evidence.statusCode)) {
    rawIssues.push({ code: 'missing_canonical', severity: 'medium' })
  }
  if (result.indexable === false && rawIssues.length === 0) {
    rawIssues.push({ code: 'not_indexable', severity: 'high' })
  }
  const issues = rawIssues.map(issue => presentAuditIssue(issue, recommendations.get(issue.code)))
  const state = issues.length ? 'needs-action' : result.indexable === true ? 'passed' : 'unavailable'
  const title = state === 'passed'
    ? '检测通过'
    : state === 'needs-action'
      ? `发现 ${issues.length} 项需要处理`
      : '暂时无法判断是否正常'
  const summary = state === 'passed'
    ? '这个页面可以被 AI 搜索和搜索引擎正常访问、理解和收录。'
    : state === 'needs-action'
      ? '页面已经完成检测，请按下面的处理方法逐项修复，修复后再重新检测。'
      : '本次返回的信息不完整，请稍后重新检测。'

  return { state, title, summary, issues, checks: buildChecks(result, evidence) }
}
