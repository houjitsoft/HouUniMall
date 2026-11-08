import { ElMessage } from 'element-plus'

/** 支持的文件扩展名 */
export const acceptedTypes = '.pdf,.doc,.docx,.xls,.xlsx,.png,.jpg,.jpeg,.gif,.zip,.gz,.rar,.7z'

/**
 * 上传前校验文件类型与大小
 * @param {File} file 文件对象
 * @returns {boolean} 是否通过校验
 */
export function beforeUpload(file) {
  const isValidType = acceptedTypes.split(',').some((type) => {
    return file.name.toLowerCase().endsWith(type.toLowerCase())
  })
  const isLt100M = file.size / 1024 / 1024 < 100

  if (!isValidType) {
    ElMessage.error('不支持的文件格式！')
    return false
  }
  if (!isLt100M) {
    ElMessage.error('文件大小不能超过 100MB!')
    return false
  }
  return true
}

/**
 * 根据文件名获取展示类型
 * @param {string} fileName 文件名
 * @returns {string} 文件类型文案
 */
export function getFileType(fileName) {
  const ext = fileName.split('.').pop()?.toLowerCase()
  const typeMap = {
    pdf: 'PDF',
    doc: 'Word',
    docx: 'Word',
    xls: 'Excel',
    xlsx: 'Excel',
    png: '图片',
    jpg: '图片',
    jpeg: '图片',
    gif: '图片',
    zip: '压缩包',
    gz: '压缩包',
    rar: '压缩包',
    '7z': '压缩包'
  }
  return typeMap[ext] || ext?.toUpperCase() || '未知'
}

/**
 * 识别类型展示文案
 * @param {string} type 识别类型
 * @returns {string}
 */
export function getRecognizeTypeText(type) {
  const typeMap = {
    contract: '合同',
    receivable: '应收',
    payable: '应付',
    goods: '商品',
    document: '单据',
    invoice: '发票',
    unknown: '未知'
  }
  return typeMap[type] || '未知'
}

/**
 * 识别类型标签样式
 * @param {string} type 识别类型
 * @returns {string}
 */
export function getRecognizeTypeTag(type) {
  const tagMap = {
    contract: 'success',
    receivable: 'warning',
    payable: 'danger',
    goods: 'info',
    document: '',
    invoice: 'success',
    unknown: 'info'
  }
  return tagMap[type] || 'info'
}

/** 识别后跳转路由 */
export const recognizeRouteMap = {
  contract: '/plugin/oa/contract/list',
  receivable: '/finance/fund/receivable',
  payable: '/finance/fund/payable',
  goods: '/goods/list',
  document: '/plugin/oa/document/list',
  invoice: '/finance/invoice/list'
}

export * from './xiaojiangImportState.js'
