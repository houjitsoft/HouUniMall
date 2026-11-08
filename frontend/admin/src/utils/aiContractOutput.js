/**
 * 智能体「契约型输出」的解析与展开。
 *
 * <p>背景：后台预置的智能体（促销活动文案、定价建议、退款归因、对账差异…）提示词里
 * 写死了 JSON 键名，前台/后台按字段解析，这是设计，不是模型跑歪。
 *
 * <p>但「试用对话」是给人看的：把 `{"banner":"…","sms":"…"}` 原样甩出来，
 * 人既判断不了文案好坏，也看不出改提示词后哪里变了。所以这里把契约 JSON
 * 摊成「字段 → 值」的行，键名尽量翻成人话；表里没有的键原样显示，不猜。
 *
 * <p>展开是有条件的：解析失败、或字段数不足两个，一律返回空数组，
 * 让它走自然语言原文——宁可不动，也不能把普通回复误当 JSON 拆开。
 */

/** 契约字段的中文名（覆盖后台已预置的 40+ 个智能体） */
export const FIELD_LABELS = {
  // ---- 通用（跨场景复用） ----
  id: 'ID',
  name: '名称',
  type: '类型',
  status: '状态',
  level: '等级',
  score: '得分',
  value: '数值',
  comment: '点评',
  total: '总量',
  count: '条数',
  ratio: '占比',
  reason: '原因',
  summary: '总结',
  text: '原文',
  content: '内容',
  note: '说明',
  category: '类别',
  tags: '标签',
  source: '来源',
  page: '页面',
  priority: '优先级',
  owner: '责任人',
  expected: '预期效果',
  actual: '实际',
  evidence: '依据',
  basis: '判断依据',
  confidence: '置信度',
  item: '检查项',
  items: '明细',
  action: '动作',
  actions: '改进项',
  plan: '方案',
  why: '理由',
  script: '话术',
  objections: '异议处理',
  time: '时间',
  goal: '目标',
  // ---- 促销 / 文案 ----
  banner: '商品页 Banner',
  sms: '短信文案',
  community: '社群文案',
  popup: '弹窗文案',
  push: '推送文案',
  inbox: '站内信文案',
  incentive: '激励文案',
  riskWords: '风险词',
  style: '风格',
  audience: '目标人群',
  body: '正文',
  title: '标题',
  subtitle: '副标题',
  button: '按钮文案',
  tone: '整体语气',
  sellingPoints: '卖点',
  detail: '详情文案',
  floors: '楼层文案',
  floor: '楼层',
  segment: '人群',
  variants: '文案变体',
  feature: '核心特征',
  dataGaps: '数据缺口',
  assumptions: '假设前提',
  risks: '风险提示',
  segments: '分层结果',
  feed: '信息流文案',
  search: '搜索广告文案',
  hook: '开场钩子',
  shots: '分镜',
  visual: '画面提示',
  voice: '口播文案',
  tip: '提示',
  cta: '行动指令',
  rhythm: '清仓节奏',
  placeholders: '占位符',
  gentle: '温和提醒',
  informative: '信息型',
  benefit: '利益型',
  channel: '建议渠道',
  interaction: '互动动作',
  // ---- 定价 / 商品 / 库存 ----
  price: '价格',
  margin: '毛利率',
  breakeven: '保本价',
  suggest: '建议',
  tier: '档位',
  scene: '适用场景',
  qty: '数量',
  spec: '规格',
  sku: 'SKU',
  goods: '商品',
  refPrice: '参考价',
  stockQty: '库存数量',
  daysLeft: '可售天数',
  suggestQty: '建议补货量',
  ageDays: '在库天数',
  tiedCapital: '占用资金',
  lossIfClear: '清仓亏损',
  listTotal: '吊牌总价',
  bundlePrice: '组合价',
  marginNote: '毛利说明',
  bundles: '搭配组合',
  slowMovers: '慢动商品',
  deviation: '偏离度',
  maybeNormal: '是否可能正常',
  toCheck: '待核项',
  faqs: '问答对',
  q: '问题',
  a: '回答',
  unknown: '无法回答的问题',
  dims: '规格维度',
  values: '取值',
  skuCount: 'SKU 组合数',
  missing: '缺失项',
  conflicts: '冲突项',
  must: '必选商品',
  nice: '推荐加购',
  optional: '可选商品',
  filters: '筛选条件',
  attrs: '属性',
  brand: '品牌',
  budget: '预算',
  levels: '会员等级',
  threshold: '升级门槛',
  benefits: '权益',
  costRate: '权益成本率',
  upgradePath: '升级路径',
  // ---- 推荐 / 对比 / 评价 ----
  query: '主查询',
  synonyms: '同义扩展',
  conclusion: '一句话结论',
  diff: '差异维度',
  dim: '维度',
  impact: '影响程度',
  paths: '选择路径',
  pick: '推荐',
  size: '推荐尺码',
  alternative: '备选尺码',
  risk: '退换风险',
  tips: '买前提醒',
  sampleSize: '样本量',
  suspicious: '疑似刷单',
  good: '好评集中点',
  bad: '差评集中点',
  point: '集中点',
  quote: '代表性原话',
  praise: '好评主题',
  complaints: '差评主题',
  improvements: '改进建议',
  topic: '主题',
  buckets: '原因分布',
  fixable: '可改进',
  evidenceEnough: '证据是否充分',
  plans: '礼物方案',
  etiquette: '讲究',
  taboos: '送礼禁忌',
  budgetNote: '预算说明',
  options: '可选回答',
  questions: '问题清单',
  service: '服务权益',
  experience: '体验权益',
  identity: '身份权益',
  // ---- 审核 / 风控 / 数据 ----
  result: '审核结论',
  riskLevel: '风险等级',
  checks: '检查项',
  missingDocs: '缺失材料',
  signals: '命中特征',
  needMoreEvidence: '还需补充的证据',
  level1: '一级类目',
  level2: '二级类目',
  candidates: '候选类目',
  needLicense: '需要资质',
  pass: '是否可上架',
  issues: '违规项',
  suggestion: '修改建议',
  required: '是否必备',
  present: '是否已具备',
  severity: '严重程度',
  fix: '修复建议',
  issue: '问题',
  blocked: '拦截数',
  suspectedFake: '疑似刷单数',
  weight: '权重',
  signal: '异常信号',
  possibleCauses: '可能原因',
  howToVerify: '验证方式',
  expectedEffect: '预期效果',
  highlights: '关键指标',
  anomalies: '异常项',
  scores: '维度得分',
  weakness: '短板',
  fixes: '改法',
  top3: '本周三件事',
  watch: '观察项',
  now: '立即做',
  resource: '所需资源',
  what: '改什么',
  how: '怎么改',
  where: '在哪改',
  achievement: '目标达成',
  metric: '指标',
  target: '目标',
  rate: '达成率',
  rootCause: '主要矛盾',
  attribution: '归因',
  factor: '因素',
  nextActions: '下次改动',
  change: '改什么',
  from: '改前',
  to: '改后',
  core: '核心词',
  longTail: '长尾词',
  word: '关键词',
  position: '布词位置',
  competition: '竞争度',
  titleOrder: '标题优先顺序',
  diffs: '差异明细',
  orderNo: '订单号',
  platformAmt: '平台金额',
  channelAmt: '渠道金额',
  gap: '差额',
  cause: '可能原因',
  howToCheck: '核查路径',
  totalGap: '差异总额',
  gapRate: '差异率',
  withinThreshold: '是否在阈值内',
  hits: '命中项',
  licenseCheck: '资质核对',
  passRate: '合格率',
  distribution: '问题分布',
  deductions: '扣分项',
  metrics: '响应指标',
  drills: '改进演练',
  // ---- 客服 / IM / 工单 ----
  intent: '意图',
  group: '分组',
  coGroup: '协同组',
  needHuman: '需要人工',
  reply: '回复内容',
  needConfirm: '需要确认',
  clarify: '需要澄清',
  changes: '变更项',
  field: '字段',
  address: '收货信息',
  greeting: '开场白',
  askInfo: '需问信息',
  forbidden: '禁用说法',
  ask: '追问内容',
  draftAnswer: '草拟回复',
  soothe: '安抚话术',
  doNot: '禁忌说法',
  emotion: '情绪',
  escalate: '是否升级',
  escalatePath: '升级路径',
  followUp: '后续跟进',
  publicReply: '公开回复',
  internalAction: '内部动作',
  closing: '收尾话术',
  askReview: '邀评话术',
  nextStep: '下一步',
  groups: '分组',
  keyword: '关键词',
  matchType: '匹配方式',
  contentType: '内容类型',
  rules: '规则',
  userMsgCount: '用户消息数',
  csMsgCount: '客服消息数',
  firstResponseDuration: '首响时长',
  avgResponseDuration: '平均响应',
  maxResponseDuration: '最长响应',
  deduct: '扣分',
  strengths: '做得好的',
  gaps: '待改进',
  example: '例句',
  howToFix: '改进办法',
  tickets: '工单',
  slaHours: 'SLA 时限',
  firstReply: '首响话术',
  needInfo: '待补信息',
  overdueHours: '超时小时',
  urgency: '紧急度',
  replyTemplate: '首响话术',
  scripts: '话术方案',
  bottomLine: '让价底线',
  resolved: '是否已解决',
  pending: '待办事项',
  ticket: '关联工单',
  duplicates: '重复知识',
  needReview: '待人工复核',
  steps: '操作步骤',
  freight: '运费承担',
  eligible: '是否符合条件',
  blockers: '不满足项',
  plainWord: '人话解释',
  eta: '预计到达',
  needTicket: '是否需工单',
  symptom: '现象',
  doThis: '处理办法',
  needService: '是否需送修',
  safety: '安全提示',
  firstUse: '首次使用',
  care: '日常养护',
  selfCheck: '自检对照',
  warehouse: '仓储方案',
  needSplit: '是否分仓',
  regions: '覆盖区域',
  carriers: '快递对比',
  bestFor: '适用场景',
  packing: '包装建议',
  promise: '时效承诺',
  exceptionScript: '异常话术',
  // ---- 通知 / 模板 ----
  event: '触发事件',
  events: '事件',
  bizType: '业务类型',
  timing: '发送时机',
  repeat: '重复策略',
  channels: '渠道',
  primary: '首选渠道',
  fallbacks: '降级渠道',
  skip: '是否跳过',
  skipReason: '跳过原因',
  merged: '合并结果',
  mergedTitle: '合并标题',
  templateCode: '模板编码',
  templateName: '模板名称',
  titleTemplate: '标题模板',
  contentTemplate: '内容模板',
  variables: '变量',
  meaning: '变量含义',
  var: '变量',
  before: '改前',
  after: '改后',
  part: '字段',
  quietHours: '免打扰时段',
  maxPerDay: '每日上限',
  remark: '备注',
  'undefined': '未声明变量',
  mismatched: '类型不匹配',
  sample: '渲染样例',
  rewrites: '改写建议',
}

export const labelOf = (key) => FIELD_LABELS[key] || key

/** 把任意值压成一行可读文本 */
export const textOf = (v) => {
  if (v === null || v === undefined || v === '') return ''
  if (Array.isArray(v)) return v.map((x) => textOf(x)).filter((s) => s !== '').join('、')
  if (typeof v === 'boolean') return v ? '是' : '否'
  if (typeof v === 'object') return JSON.stringify(v)
  return String(v)
}

/**
 * 从回复里抠出 JSON。
 *
 * <p>模型不一定听话：可能套 ```json 围栏，也可能在 JSON 前加一句「好的，结果如下」。
 * 这里依次尝试围栏内容、整段、从第一个括号到结尾、从第一个括号到最后一个闭合括号。
 * 全部失败返回 null —— 说明这是自然语言回复，保留原文。
 */
export const extractJson = (text) => {
  const raw = String(text || '').trim()
  if (!raw) return null
  const candidates = []
  const fenced = raw.match(/```(?:json)?\s*([\s\S]*?)```/i)
  if (fenced) candidates.push(fenced[1])
  candidates.push(raw)
  const start = raw.search(/[{[]/)
  if (start > 0) {
    const tail = raw.slice(start)
    candidates.push(tail)
    const lastClose = Math.max(tail.lastIndexOf('}'), tail.lastIndexOf(']'))
    if (lastClose > 0) candidates.push(tail.slice(0, lastClose + 1))
  }
  for (const c of candidates) {
    const s = String(c).trim()
    if (!s) continue
    try {
      const val = JSON.parse(s)
      if (val && typeof val === 'object') return val
    } catch {
      /* 换下一个候选 */
    }
  }
  return null
}

const isPlainObject = (v) => v && typeof v === 'object' && !Array.isArray(v)

const pairsOf = (obj) =>
  Object.entries(obj).map(([k, v]) => ({ k: labelOf(k), v: textOf(v) || '—' }))

/**
 * 把契约 JSON 摊成行：
 *   - 标量       → { type:'text', label, value }
 *   - 标量数组   → { type:'text', value:'a、b' }
 *   - 空数组     → { type:'empty' }
 *   - 对象数组   → 一个 { type:'group' } 标题 + 每项一个 { type:'pairs' }（#1/#2…）
 *   - 对象       → { type:'group' } 标题 + 缩进递归
 */
export const buildRows = (value) => {
  const rows = []
  const walk = (obj, depth) => {
    Object.entries(obj).forEach(([k, v]) => {
      if (Array.isArray(v)) {
        if (!v.length) {
          rows.push({ label: labelOf(k), type: 'empty', depth })
          return
        }
        if (v.every(isPlainObject)) {
          rows.push({ label: labelOf(k), type: 'group', count: v.length, depth })
          v.forEach((item, i) => {
            rows.push({
              label: `#${i + 1}`,
              type: 'pairs',
              depth: depth + 1,
              pairs: pairsOf(item)
            })
          })
          return
        }
        rows.push({ label: labelOf(k), type: 'text', value: textOf(v) || '—', depth })
        return
      }
      if (isPlainObject(v)) {
        rows.push({ label: labelOf(k), type: 'group', depth })
        walk(v, depth + 1)
        return
      }
      rows.push({ label: labelOf(k), type: 'text', value: textOf(v) || '—', depth })
    })
  }

  if (Array.isArray(value)) {
    if (value.every(isPlainObject)) {
      value.forEach((item, i) => {
        rows.push({ label: `#${i + 1}`, type: 'group', depth: 0 })
        rows.push({ label: '', type: 'pairs', depth: 1, pairs: pairsOf(item) })
      })
    } else {
      rows.push({ label: '结果', type: 'text', value: textOf(value) || '—', depth: 0 })
    }
    return rows
  }
  walk(value, 0)
  return rows
}

/**
 * 回复 → 行数组。解析不出来、或只有单个字段时返回空数组，
 * 调用方据此决定继续显示原文（避免把寒暄、单字段回复误拆）。
 */
export const buildStructured = (text) => {
  const value = extractJson(text)
  if (!value) return []
  const rows = buildRows(value)
  return rows.length >= 2 ? rows : []
}

export const prettyJson = (text) => {
  const value = extractJson(text)
  return value ? JSON.stringify(value, null, 2) : String(text || '')
}

/**
 * 输出是不是 JSON 契约。
 *
 * <p>判断依据只有提示词本身（后台预置的智能体都写了「输出 JSON，键名固定」）。
 * 卡片上标出来，是为了让人一眼知道：这个智能体的回复是给程序解析的，
 * 试用对话里看到的是展开后的字段表，不是它坏了。
 */
export const isContractOutput = (agent) => {
  const p = agent?.prompt || ''
  if (/键名固定|输出契约/.test(p)) return true
  // 注意：intent_recognize 的提示词写的是「不输出 JSON、不用列表符号」，
  // 只按「输出 JSON」四个字匹配会把它误判成契约型，卡片上就挂错标签。
  return /输出\s*JSON/i.test(p) && !/不(要)?输出\s*JSON/.test(p)
}
