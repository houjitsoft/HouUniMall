/**
 * 插件发布 / 审核 API（base 插件中心 · 发布侧）
 *
 * 对接后端 `/base/plugin-center/publish/**`，经 BFF 走 `/web/plugin-center/publish/**`。
 * 另一组接口的区别：
 *   插件中心（/web/plugin-center/**）  = 站点侧：装了什么、启停
 *   插件市场（/web/plugin-center/market/**）= 买什么、在线安装
 *   发布侧（本文件）                  = 上架什么：上传制品 → 提交审核 → 审核 → 上下架
 *
 * 后端只允许平台管理员调用（adminGate），普通账号会收到 403。
 */
import request from '@/utils/request'

const BASE_URL = '/web/plugin-center/publish'
const dataOf = (response) => response?.data ?? response

/**
 * 上传制品。
 *
 * @param {Object} params
 * @param {File}   params.file        jar 文件
 * @param {string} params.pluginCode  插件编码（必须与包内 plugin.yaml 一致）
 * @param {string} params.version     版本号（语义化，如 1.2.0）
 * @param {string} [params.changelog] 更新说明
 * @param {string} [params.uploadId]  幂等键；不传则本函数按 code+version+size+时间戳生成
 * @returns {Promise<Object>} UploadResult
 */
export async function uploadArtifact ({ file, pluginCode, version, changelog, uploadId }) {
  const form = new FormData()
  form.append('file', file)
  form.append('pluginCode', pluginCode)
  form.append('version', version)
  if (changelog) form.append('changelog', changelog)
  form.append(
    'uploadId',
    uploadId || `${pluginCode}-${version}-${file?.size ?? 0}-${Date.now()}`
  )
  return dataOf(
    await request.post(`${BASE_URL}/upload`, form, {
      headers: { 'Content-Type': 'multipart/form-data' },
      // 制品普遍几十 MB，默认 45s 会先超时；给到 10 分钟
      timeout: 600000
    })
  )
}

/** 提交审核：draft / rejected → pending */
export const submitVersion = async (pluginCode, version) =>
  dataOf(await request.post(`${BASE_URL}/versions/submit`, { pluginCode, version }))

/**
 * 审核：pending → published（approve）/ rejected（reject）
 *
 * @param {number} versionId 版本行 ID（来自 versions/list）
 * @param {'approve'|'reject'} decision
 * @param {string} [note] 审核意见
 */
export const reviewVersion = async (versionId, decision, note) =>
  dataOf(await request.post(`${BASE_URL}/versions/review`, { versionId, decision, note }))

/** 下架：published → offline */
export const offlineVersion = async (pluginCode, version, reason) =>
  dataOf(await request.post(`${BASE_URL}/versions/offline`, { pluginCode, version, reason }))

/** 重新上架：offline → published */
export const relistVersion = async (pluginCode, version) =>
  dataOf(await request.post(`${BASE_URL}/versions/relist`, { pluginCode, version }))

/**
 * 版本列表。
 *
 * @param {string} [pluginCode] 不传表示全部插件
 * @param {number} [status] 1=已发布 2=待审核 3=已驳回 4=已下架 0/空=全部
 */
export const listVersions = async (pluginCode, status) =>
  dataOf(
    await request.post(`${BASE_URL}/versions/list`, {
      ...(pluginCode ? { pluginCode } : {}),
      ...(status === undefined || status === null || status === '' ? {} : { status })
    })
  )

/** 版本状态文案与标签色（与后端 PluginVersionStateMachine 对齐） */
export const VERSION_STATUS = {
  0: { text: '草稿', type: 'info' },
  1: { text: '已发布', type: 'success' },
  2: { text: '待审核', type: 'warning' },
  3: { text: '已驳回', type: 'danger' },
  4: { text: '已下架', type: 'info' }
}

export const versionStatusOf = (status) =>
  VERSION_STATUS[status] || { text: `未知(${status})`, type: 'info' }
