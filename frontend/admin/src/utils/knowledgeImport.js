export function normalizeKnowledgeImportRows(rows = []) {
  return rows.map((row = {}) => ({
    title: String(row.title || '').trim(),
    category: String(row.category || '企业简介').trim() || '企业简介',
    content: String(row.content || '').trim(),
    tags: Array.isArray(row.tags)
      ? row.tags.map((tag) => String(tag).trim()).filter(Boolean)
      : String(row.tags || '').split(/[,，]/).map((tag) => tag.trim()).filter(Boolean),
    visibility: ['private', 'shared'].includes(row.visibility) ? row.visibility : 'project',
    sourceName: row.sourceName || '',
  }))
}

export function summarizeKnowledgeImport(result = {}) {
  return {
    validCount: Number(result.validCount || result.rows?.length || 0),
    errorCount: Number(result.errorCount || result.errors?.length || 0),
    rows: normalizeKnowledgeImportRows(result.rows || []),
    errors: Array.isArray(result.errors) ? result.errors : [],
  }
}
