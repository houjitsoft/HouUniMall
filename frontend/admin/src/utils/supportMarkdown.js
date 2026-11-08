/**
 * 智能客服 Markdown 轻量渲染（模型回答含 **、列表、标题等）
 * 先转义 HTML，再安全替换常见语法，供 v-html 使用。
 */

/** 已是块级 HTML 的正文（GEO 文章入库格式），不再按 Markdown 转义 */
const HTML_BLOCK_START = /^\s*<(p|h[1-6]|blockquote|table|ul|ol|div|section|figure|img|br)\b/i

function escapeHtml(text) {
  return String(text)
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/"/g, '&quot;')
}

function inlineFormat(raw) {
  let s = escapeHtml(raw)
  s = s.replace(/\*\*(.+?)\*\*/g, '<strong>$1</strong>')
  s = s.replace(/`([^`]+)`/g, '<code>$1</code>')
  return s
}

function closeList(html, state) {
  if (state.inUl) {
    html.push('</ul>')
    state.inUl = false
  }
  if (state.inOl) {
    html.push('</ol>')
    state.inOl = false
  }
}

/**
 * @param {string} text
 * @returns {string} HTML
 */
export function renderSupportMarkdown(text) {
  if (text == null || text === '') return ''

  const raw = String(text)
  // GEO 文章正文入库时统一转成了 HTML，若再按 Markdown 逐行转义，
  // 页面会把 <p style=...> 当文本显示出来，所以块级 HTML 直接原样返回。
  if (HTML_BLOCK_START.test(raw)) return raw

  const lines = raw.split('\n')
  const html = []
  const state = { inUl: false, inOl: false }

  for (const line of lines) {
    const trimmed = line.trim()

    if (!trimmed) {
      closeList(html, state)
      continue
    }

    const h4 = trimmed.match(/^###\s+(.+)$/)
    if (h4) {
      closeList(html, state)
      html.push(`<h4 class="geo-cs-md__h4">${inlineFormat(h4[1])}</h4>`)
      continue
    }

    const h3 = trimmed.match(/^##\s+(.+)$/)
    if (h3) {
      closeList(html, state)
      html.push(`<h3 class="geo-cs-md__h3">${inlineFormat(h3[1])}</h3>`)
      continue
    }

    const bullet = trimmed.match(/^[-*•]\s+(.+)$/)
    if (bullet) {
      if (state.inOl) {
        html.push('</ol>')
        state.inOl = false
      }
      if (!state.inUl) {
        html.push('<ul class="geo-cs-md__ul">')
        state.inUl = true
      }
      html.push(`<li>${inlineFormat(bullet[1])}</li>`)
      continue
    }

    const ordered = trimmed.match(/^\d+[.)]\s+(.+)$/)
    if (ordered) {
      if (state.inUl) {
        html.push('</ul>')
        state.inUl = false
      }
      if (!state.inOl) {
        html.push('<ol class="geo-cs-md__ol">')
        state.inOl = true
      }
      html.push(`<li>${inlineFormat(ordered[1])}</li>`)
      continue
    }

    closeList(html, state)
    html.push(`<p class="geo-cs-md__p">${inlineFormat(trimmed)}</p>`)
  }

  closeList(html, state)
  return html.join('')
}
