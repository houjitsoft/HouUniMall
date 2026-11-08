/**
 * 解析统一 ResponseResult（code/message/data）或已解包的业务数据
 */

const SUCCESS_CODES = new Set([1, 200])

/**
 * @param {Object} response
 * @returns {*|null}
 */
export function unwrapResponseData(response) {
  if (!response || typeof response !== 'object') return null
  if (SUCCESS_CODES.has(response.code)) {
    return response.data !== undefined ? response.data : response
  }
  if (!Object.prototype.hasOwnProperty.call(response, 'code')) {
    return response
  }
  return null
}

/**
 * @param {Object} response
 * @returns {{ records: Array, total: number }|null}
 */
export function unwrapPageData(response) {
  const data = unwrapResponseData(response)
  if (!data || typeof data !== 'object') return null
  const records = data.records || data.list || (Array.isArray(data) ? data : [])
  return {
    records,
    total: data.total ?? records.length ?? 0
  }
}
