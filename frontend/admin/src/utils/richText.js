/**
 * 任务/项目描述富文本展示与编辑回填共用工具
 * - 已有 HTML：原样输出（必要时把裸换行转为 br）
 * - 转义 HTML（&lt;div&gt;）：还原后再输出
 * - 纯文本：转义 + URL 可点击 + 换行转 br
 */

/**
 * 去掉可能导致 RTL/乱序的 dir 属性
 * @param {string} html
 * @returns {string}
 */
export function normalizeRichTextHtml(html) {
  if (!html) return ''
  return String(html)
    .replace(/\sdir=["']rtl["']/gi, ' dir="ltr"')
    .replace(/\sstyle=["'][^"']*direction\s*:\s*rtl[^"']*["']/gi, '')
}

/**
 * 将描述内容转为可安全用于 v-html / contenteditable 的 HTML
 * @param {string} html
 * @returns {string}
 */
export function formatRichTextContent(html) {
  if (!html) return ''
  let text = String(html).trim()
  if (!text) return ''

  // 转义后的 HTML 标签（如 &lt;p&gt;）先还原
  if (!/<[a-z][\s\S]*>/i.test(text) && /&lt;[a-z]/i.test(text)) {
    const div = document.createElement('div')
    div.innerHTML = text
    text = div.innerHTML || text
  }

  // 已含 HTML：保留标签；若仍有裸换行则转为 br，便于「元数据纯文本 + 正文 HTML」混排
  if (/<[a-z][\s\S]*>/i.test(text)) {
    const withBreaks = text.includes('\n') ? text.replace(/\n/g, '<br>') : text
    return normalizeRichTextHtml(withBreaks)
  }

  // 纯文本
  const escaped = text
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
  const withLinks = escaped.replace(
    /(https?:\/\/[^\s<]+)/g,
    '<a href="$1" target="_blank" rel="noopener noreferrer">$1</a>'
  )
  return normalizeRichTextHtml(withLinks.replace(/\n/g, '<br>'))
}
