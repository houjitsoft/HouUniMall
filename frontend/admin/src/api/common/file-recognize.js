/**
 * 文件识别API
 */
import request from '@/utils/request'

// restartall 最后会滚动重启 bff-web，Windows 上启动约需 30～40 秒。
// 目录查询是幂等 GET，可在该短暂窗口内静默重试；权限和业务错误必须原样返回。
const CATALOG_RETRY_DELAYS_MS = [2000, 3000, 5000, 5000, 5000, 5000, 5000, 5000]

const wait = (delayMs) => new Promise(resolve => setTimeout(resolve, delayMs))

const isTransientGatewayError = (error) => {
  const status = error?.response?.status
  if ([502, 503, 504].includes(status)) return true

  if (error?.response) return false
  return ['ECONNREFUSED', 'ECONNRESET', 'ERR_NETWORK', 'ETIMEDOUT'].includes(error?.code)
}

const getImportCatalogWithRetry = async () => {
  let retryIndex = 0
  while (true) {
    try {
      return await request.get('/web/common/file/import/catalog', {
        skipErrorMessage: true
      })
    } catch (error) {
      if (!isTransientGatewayError(error) || retryIndex >= CATALOG_RETRY_DELAYS_MS.length) throw error
      await wait(CATALOG_RETRY_DELAYS_MS[retryIndex++])
    }
  }
}

export const fileRecognizeApi = {
  getImportCatalog: getImportCatalogWithRetry,

  // 打开面板时恢复草稿是锦上添花：失败不应弹「没权限」之类误导提示
  resumeImportDraft: () => request.get('/web/common/file/import/draft/resume', {
    skipErrorMessage: true
  }),

  getImportDraft: (draftNo) => request.get(`/web/common/file/import/draft/${encodeURIComponent(draftNo)}`, {
    skipErrorMessage: true
  }),

  recognizeForImport: (formData) => request.post('/web/common/file/import/recognize', formData, {
    headers: { 'Content-Type': 'multipart/form-data' },
    timeout: 210000
  }),

  updateImportItem: (itemId, data) => request.patch(`/web/common/file/import/item/${itemId}`, data),

  confirmImport: (itemIds) => request.post('/web/common/file/import/confirm', { itemIds }),

  // 上传并识别文件
  // 支持传入FormData或file对象
  uploadAndRecognize: async (fileOrFormData) => {
    let formData
    if (fileOrFormData instanceof FormData) {
      formData = fileOrFormData
    } else {
      formData = new FormData()
      formData.append('file', fileOrFormData)
    }
    return request.post('/web/common/file/recognize', formData, {
      headers: {
        'Content-Type': 'multipart/form-data'
      },
      // 系统默认 AI 模型识别最长等待 180 秒，避免沿用全局 45 秒超时提前中断
      timeout: 210000
    })
  },
  
  // 确认识别结果并跳转
  confirmAndRedirect: async (data) => {
    return request.post('/web/common/file/confirm', data)
  }
}
