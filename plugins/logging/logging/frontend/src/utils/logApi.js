export const unwrap = (response) => response?.data ?? response ?? {}
export const unwrapPage = (response) => {
  const value = unwrap(response)
  return value?.list || value?.records || value?.data || value || { list: [], total: 0 }
}
export const defaultDateRange = () => []
const identity = (row) => row || {}
export const mapAccessRow = identity
export const mapAuditErrorRow = identity
export const mapElkRow = identity
export const mapBusinessRow = identity
export const mapLoginRow = identity
export const mapOperationRow = identity
