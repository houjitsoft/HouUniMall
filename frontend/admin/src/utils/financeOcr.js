/**
 * 财务账簿 OCR 共用工具（收入/支出）
 * 科目匹配、日期解析、字段抽取集中在一处，避免双页漂移。
 */

export function parseOcrDate(value) {
  if (!value) return null
  const str = String(value).trim()
  const normalized = str.replace(/[年月]/g, '-').replace(/日/g, '').replace(/\./g, '-').replace(/\//g, '-')
  const d = new Date(normalized)
  return Number.isNaN(d.getTime()) ? null : d
}

/**
 * @param {object} data recognize API data
 * @param {'spending'|'income'} direction 金额正则关键词方向
 */
export function extractOcrFields(data, direction = 'spending') {
  const fields = data?.fields && typeof data.fields === 'object' ? { ...data.fields } : {}
  let text = [data?.fullText, data?.rawText, data?.text, data?.summary].filter(Boolean).join('\n')
  const jsonMatch = text.match(/\{[\s\S]*\}/)
  if (jsonMatch) {
    try {
      const parsed = JSON.parse(jsonMatch[0])
      if (parsed && typeof parsed === 'object') {
        Object.assign(fields, parsed.fields || parsed)
        text += `\n${parsed.fullText || ''}\n${parsed.summary || ''}`
      }
    } catch (_) { /* 继续正则兜底 */ }
  }
  if (fields.amount == null && fields.money == null && fields.total == null) {
    const amountKeys = direction === 'income'
      ? '金额|价税合计|含税合计|合计|总额|实收|收款|收入|amount|money|total'
      : '金额|价税合计|含税合计|合计|总额|实付|付款|支出|amount|money|total'
    const amountMatch = text.match(new RegExp(
      `(?:${amountKeys})[^\\d]{0,16}(\\d{1,3}(?:[,，]\\d{3})+(?:\\.\\d{1,2})?|\\d+(?:\\.\\d{1,2})?)`,
      'i'
    ))
    if (amountMatch) fields.amount = amountMatch[1]
  }
  if (fields.date == null && fields.datatime == null && fields.occurDate == null) {
    const dateMatch = text.match(/((?:19|20)\d{2}[年./-]\d{1,2}[月./-]\d{1,2}(?:日)?)/)
    if (dateMatch) fields.date = dateMatch[1]
  }
  return fields
}

/**
 * 将 OCR 账户/渠道文案匹配到账户科目下拉（对公账户/银行卡/支付宝/微信/现金）
 * @param {Array<{id:any,name?:string,status?:any}>} subjects
 * @param {string|null|undefined} subjectName
 * @param {string} fullText
 */
export function matchSubjectId(subjects, subjectName, fullText = '') {
  if (!Array.isArray(subjects) || !subjects.length) return null
  const active = subjects.filter((s) => s && s.id != null && Number(s.status) !== 0)
  const list = active.length ? active : subjects
  const hay = `${subjectName || ''} ${fullText || ''}`.replace(/\s+/g, '')
  if (!hay) return null

  const byExact = list
    .filter((s) => {
      const name = String(s.name || '').trim()
      if (!name || !hay.includes(name)) return false
      if (name === '微信' && hay.includes('企业微信') && !/(微信支付|微信零钱|微信收款|微信转账)/.test(hay)) {
        return false
      }
      return true
    })
    .sort((a, b) => String(b.name || '').length - String(a.name || '').length)[0]
  if (byExact) return byExact.id

  const aliasGroups = [
    { keys: ['对公账户', '对公', '公户', '对公打款', '对公转账', '银行转账', '网银', '公对公'], prefer: '对公' },
    { keys: ['银行卡', '储蓄卡', '借记卡', '信用卡'], prefer: '银行' },
    { keys: ['支付宝', 'alipay'], prefer: '支付宝' },
    { keys: ['微信支付', '微信零钱', '微信收款', '微信转账'], prefer: '微信' },
    { keys: ['现金', '现金支付'], prefer: '现金' }
  ]
  for (const group of aliasGroups) {
    if (!group.keys.some((k) => hay.includes(k))) continue
    const hit = list.find((s) => String(s.name || '').includes(group.prefer))
      || list.find((s) => group.keys.some((k) => String(s.name || '').includes(k)))
    if (hit) return hit.id
  }
  if (/\b微信\b|微信支付/.test(hay) && !hay.includes('企业微信')) {
    const wx = list.find((s) => String(s.name || '').includes('微信'))
    if (wx) return wx.id
  }
  return null
}

/** 支付凭证未写明渠道时，默认「对公账户」 */
export function defaultCorporateSubjectId(subjects) {
  if (!Array.isArray(subjects)) return null
  const corp = subjects.find((s) => String(s.name || '').includes('对公'))
  return corp?.id ?? null
}

/** 组装可持久化的 OCR 文本块（收入/支出共用） */
export function buildOcrBlock(fileName, data) {
  const fields = data?.fields && typeof data.fields === 'object' ? data.fields : {}
  const fullText = data?.fullText || data?.rawText || data?.text || ''
  const lines = [`【${fileName || '附件'}】`]
  if (data?.type) lines.push(`类型：${data.type}`)
  if (data?.summary) lines.push(`摘要：${data.summary}`)
  if (fullText) lines.push(`全文：\n${fullText}`)
  if (Object.keys(fields).length) lines.push(`结构化字段：\n${JSON.stringify(fields, null, 2)}`)
  if (data?.source) lines.push(`识别来源：${data.source}`)
  return lines.join('\n')
}
