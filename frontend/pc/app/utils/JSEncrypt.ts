/**
 * JSEncrypt 密码加密工具
 * 
 * 封装了 RSA 密码加密功能，使用配置的公钥进行加密
 */

import JSEncrypt from 'jsencrypt'

/**
 * RSA 公钥
 */
const RSA_PUBLIC_KEY = '-----BEGIN PUBLIC KEY-----\n' +
    'MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAkBWv1uzhtXtFe9a0PIy9\n' +
    'TTb980Nzbv5t3Vp2Yue5fhXSoiNBoo5sypuOu2tryyGjB0cxQIXrlNUPLz6ic7wq\n' +
    'WN2+4zFLbglIZEjpMrj2OzSVPF3lfvF1uzR122sj9+Q2Tpg8D2M2qD1HpPVGOMvF\n' +
    'kOsIlycGQmxGoyMorQFMqxkmiwo6h1BUr+uI/H+WWDxgckOmKuNXTaD52296wgIW\n' +
    '6ypqZs2CPq5aZyvnM6YIxv9/xk4tJHmS0t5Wzm5RNErZcu64ikV6n3jeu8kfm3W0\n' +
    'lhZ87t698QHJmVOaCUljQS9l6IqYzaziADqRDO1qgyPS5HnGKEeLu5FX/bzl3YcR\n' +
    'ywIDAQAB\n' +
    '-----END PUBLIC KEY-----'

/**
 * 加密密码
 * 
 * @param password 原始密码
 * @returns 加密后的密码，如果加密失败则返回原密码
 */
export const encryptPassword = (password: string): string => {
    if (!password) {
        return password
    }

    try {
        const encrypt = new JSEncrypt()
        encrypt.setPublicKey(RSA_PUBLIC_KEY)
        const encrypted = encrypt.encrypt(password)

        if (!encrypted) {
            console.error('密码加密失败，使用原密码')
            return password
        }

        return encrypted
    } catch (error) {
        console.error('密码加密过程出错:', error)
        return password
    }
}

/**
 * 获取 RSA 公钥
 * 
 * @returns RSA 公钥字符串
 */
export const getRSAPublicKey = (): string => {
    return RSA_PUBLIC_KEY
}
