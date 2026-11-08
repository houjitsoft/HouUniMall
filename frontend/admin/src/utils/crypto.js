/**
 * 加密工具类
 */
import { JSEncrypt } from 'jsencrypt'

// RSA公钥（与后端私钥对应）
// 最后更新: 2026-01-30
const RSA_PUBLIC_KEY = `-----BEGIN PUBLIC KEY-----
MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAkBWv1uzhtXtFe9a0PIy9
TTb980Nzbv5t3Vp2Yue5fhXSoiNBoo5sypuOu2tryyGjB0cxQIXrlNUPLz6ic7wq
WN2+4zFLbglIZEjpMrj2OzSVPF3lfvF1uzR122sj9+Q2Tpg8D2M2qD1HpPVGOMvF
kOsIlycGQmxGoyMorQFMqxkmiwo6h1BUr+uI/H+WWDxgckOmKuNXTaD52296wgIW
6ypqZs2CPq5aZyvnM6YIxv9/xk4tJHmS0t5Wzm5RNErZcu64ikV6n3jeu8kfm3W0
lhZ87t698QHJmVOaCUljQS9l6IqYzaziADqRDO1qgyPS5HnGKEeLu5FX/bzl3YcR
ywIDAQAB
-----END PUBLIC KEY-----`

/**
 * RSA加密工具类
 */
export class CryptoUtil {
  /**
   * 使用RSA公钥加密密码
   * @param {string} password - 原始密码
   * @returns {string} 加密后的密码
   */
  static encryptPassword(password) {
    if (!password) {
      throw new Error('密码不能为空')
    }
    
    try {
      // 创建JSEncrypt实例
      const encrypt = new JSEncrypt()
      
      // 设置公钥
      encrypt.setPublicKey(RSA_PUBLIC_KEY)
      
      // 加密密码
      const encryptedPassword = encrypt.encrypt(password)
      
      if (!encryptedPassword) {
        throw new Error('RSA加密失败')
      }
      
      return encryptedPassword
    } catch (error) {
      throw new Error('密码加密失败: ' + error.message)
    }
  }
}

export default CryptoUtil