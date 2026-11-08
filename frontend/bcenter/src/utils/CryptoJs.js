import CryptoJS from 'crypto-js'

const aesKey = '2y0u2n5a0f1e2n4g' // 密钥
const aesIv = 'y2u0a2n5f0e1n2g4' // 初始向量

/**
 *
 * @param {*} text
 * @returns 加密
 */
export const aesEncrypt = (text) => {
  var key = CryptoJS.enc.Utf8.parse(aesKey),
    iv = CryptoJS.enc.Utf8.parse(aesIv),
    srcs = CryptoJS.enc.Utf8.parse(text),
    // CBC 加密方式，Pkcs7 填充方式
    encrypted = CryptoJS.AES.encrypt(srcs, key, {
      iv: iv,
      mode: CryptoJS.mode.CBC,
      padding: CryptoJS.pad.Pkcs7,
    })
  return encrypted.toString()
}
/**
 *
 * @param {*} text
 * @returns 解密函数
 */
export const aesDecrypt = (text) => {
  var key = CryptoJS.enc.Utf8.parse(aesKey),
    iv = CryptoJS.enc.Utf8.parse(aesIv),
    srcs = text,
    // CBC 加密方式，Pkcs7 填充方式
    decrypted = CryptoJS.AES.decrypt(srcs, key, {
      iv: iv,
      mode: CryptoJS.mode.CBC,
      padding: CryptoJS.pad.Pkcs7,
    })
  return decrypted.toString(CryptoJS.enc.Utf8)
}
