import * as XLSX from 'xlsx'

export function exportCrmRows(rows, name) {
  const workbook = XLSX.utils.book_new()
  XLSX.utils.book_append_sheet(workbook, XLSX.utils.json_to_sheet(rows), '数据')
  XLSX.writeFile(workbook, `${name}-${new Date().toISOString().slice(0, 10)}.xlsx`)
}
