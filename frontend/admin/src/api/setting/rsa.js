import request from '@/utils/request'

/**
 * RSA密钥管理API
 */

/**
 * 获取RSA公钥
 */
export function getRSAPublicKey() {
  return request({
    url: '/web/base/sysConfig/rsa/publicKey',
    method: 'get'
  })
}

/**
 * 获取RSA密钥配置（包含公钥和私钥）
 */
export function getRSAKeys() {
  return request({
    url: '/web/base/sysConfig/rsa/keys',
    method: 'get'
  })
}

/**
 * 保存RSA密钥配置
 * @param {Object} keys - 密钥配置对象
 * @param {string} keys.publicKey - RSA公钥
 * @param {string} keys.privateKey - RSA私钥
 */
export function saveRSAKeys(keys) {
  return request({
    url: '/web/base/sysConfig/rsa/keys',
    method: 'post',
    data: keys
  })
}

/**
 * 生成RSA密钥对
 * @param {number} keySize - 密钥长度（1024/2048/4096）
 */
export function generateRSAKeys(keySize = 2048) {
  return request({
    url: '/web/base/sysConfig/rsa/generate',
    method: 'post',
    params: { keySize }
  })
}

/**
 * 测试RSA加密解密
 * @param {Object} keys - 密钥配置对象
 * @param {string} keys.publicKey - RSA公钥
 * @param {string} keys.privateKey - RSA私钥
 */
export function testRSAEncryption(keys) {
  return request({
    url: '/web/base/sysConfig/rsa/test',
    method: 'post',
    data: keys
  })
}
