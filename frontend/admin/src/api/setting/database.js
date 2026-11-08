import request from '@/utils/request'

const BASE = '/web/setting/database'

function unwrapData(res, fallback = null) {
  if (res && (res.code === 1 || res.code === 200)) {
    return res.data !== undefined && res.data !== null ? res.data : fallback
  }
  const msg = res?.message || '请求失败'
  throw new Error(msg)
}

export async function listDatabases() {
  const res = await request.get(`${BASE}/list`)
  const data = unwrapData(res, [])
  return Array.isArray(data) ? data : []
}

export async function listTables(dbName) {
  const res = await request.get(`${BASE}/${encodeURIComponent(dbName)}/tables`)
  const data = unwrapData(res, [])
  return Array.isArray(data) ? data : []
}

export async function listColumns(dbName, tableName) {
  const res = await request.get(
    `${BASE}/${encodeURIComponent(dbName)}/tables/${encodeURIComponent(tableName)}/columns`
  )
  const data = unwrapData(res, [])
  return Array.isArray(data) ? data : []
}

export async function listSqlPatches() {
  const res = await request.get(`${BASE}/patches`)
  const data = unwrapData(res, [])
  return Array.isArray(data) ? data : []
}

export async function readSqlPatch(patchId) {
  const res = await request.get(`${BASE}/patch/${patchId}/content`)
  const data = unwrapData(res, {})
  return data && typeof data === 'object' ? data : { content: '' }
}

export async function executeSql(data) {
  const res = await request.post(`${BASE}/execute`, data)
  return unwrapData(res, null)
}

export async function executeSqlPatch(patchId, data = {}) {
  const res = await request.post(`${BASE}/patch/${patchId}/execute`, data)
  return unwrapData(res, null)
}

export async function importWecomScrmSql(data = {}) {
  const res = await request.post(`${BASE}/wecom/import`, data)
  return unwrapData(res, null)
}
