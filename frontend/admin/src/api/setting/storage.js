/**
 * OSS存储配置API接口
 * 对接后端 /web/base/ossConfig 接口
 */
import request from '@/utils/request'
import { getToken } from '@/utils/auth'

/**
 * 查询所有OSS存储配置列表
 * @returns {Promise} 存储配置列表
 */
export function queryAllOssConfig() {
  return request.post('/web/base/ossConfig/list')
}

/**
 * 查询指定类型的OSS配置详情
 * @param {string} ossType OSS类型 (aliyun/minio/tencentCloud/westFss)
 * @returns {Promise} 指定类型配置
 */
export function selectOSSInfo(ossType) {
  return request.post('/web/base/ossConfig/detail', {
    token: getToken(),
    ossType
  })
}

/**
 * 新增或更新OSS配置（存在则更新，不存在则添加）
 * @param {Object} data OSS配置数据
 * @param {number} [data.ossId] 配置ID（更新时必填）
 * @param {string} data.ossType OSS类型
 * @param {string} data.ossBacketName 存储桶名
 * @param {string} data.ossFolder 文件夹名
 * @param {string} data.ossRegion 服务器集群地址/FTP地址
 * @param {string} data.ossEndpoint API域名/访问端点
 * @param {string} data.ossAccessKeyId 访问密钥/FTP用户名
 * @param {string} data.ossAccessKeySecret 私钥/FTP密码
 * @param {string} data.ossStates 状态 01启用 00禁用
 * @param {string} [data.ossRemark] 备注
 * @returns {Promise} 操作结果
 */
export function saveOssConfig(data) {
  return request.post('/web/base/ossConfig/save', {
    token: getToken(),
    ...data
  })
}

/**
 * 修改OSS配置启用状态
 * @param {number} ossId 配置ID
 * @param {string} ossStates 状态 01启用 00禁用
 * @returns {Promise} 操作结果
 */
export function updateOssStates(ossId, ossStates) {
  return request.post('/web/base/ossConfig/updateStates', {
    token: getToken(),
    ossId,
    ossStates
  })
}

/**
 * 存储监控概览
 */
export function queryStorageMonitor() {
  return request.post('/web/base/ossConfig/monitor')
}

/**
 * 素材库文件列表
 * @param {Object} params page, limit, fileName, fileType
 */
export function queryStorageFiles(params = {}) {
  return request.post('/web/base/storage/files/list', {
    page: params.page || 1,
    limit: params.limit || 20,
    fileName: params.fileName || undefined,
    fileType: params.fileType ?? undefined
  })
}

/**
 * 删除素材库文件
 * @param {number[]} fileIds
 */
export function deleteStorageFiles(fileIds) {
  return request.post('/web/base/storage/files/delete', { fileIds })
}

/**
 * 登记已上传文件
 */
export function registerStorageFile(data) {
  return request.post('/web/base/storage/files/register', data)
}

/**
 * 上传文件到当前启用的 OSS
 * @param {File} file
 */
export function uploadStorageFile(file) {
  const fd = new FormData()
  fd.append('file', file)
  return request.post('/web/thirdparty/oss/uploadFile', fd, {
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}
