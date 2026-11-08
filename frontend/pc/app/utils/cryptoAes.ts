import CryptoJS from 'crypto-js'
import type { CreateOrdersParams } from '~/types/business'

/** AES-128-CBC 密钥 */
const ORDER_AES_KEY = '2y0u2n5a0f1e2n4g'
/** 初始向量 */
const ORDER_AES_IV = 'y2u0a2n5f0e1n2g4'

function aesKeyAndIv(): { key: CryptoJS.lib.WordArray; iv: CryptoJS.lib.WordArray } {
    const key = CryptoJS.enc.Utf8.parse(ORDER_AES_KEY.slice(0, 16))
    const iv = CryptoJS.enc.Utf8.parse(ORDER_AES_IV.slice(0, 16))
    return { key, iv }
}

/**
 * AES-128-CBC + PKCS7，输出 Base64 密文（不含 OpenSSL salt 头）
 */
export function encryptAesCbcPkcs7Base64(plainText: string): string {
    const { key, iv } = aesKeyAndIv()
    const encrypted = CryptoJS.AES.encrypt(plainText, key, {
        iv,
        mode: CryptoJS.mode.CBC,
        padding: CryptoJS.pad.Pkcs7
    })
    return CryptoJS.enc.Base64.stringify(encrypted.ciphertext)
}

/**
 * AES-128-CBC + PKCS7，解密 Base64 密文，失败返回 null
 */
export function decryptAesCbcPkcs7Base64(cipherText: string): string | null {
    try {
        const { key, iv } = aesKeyAndIv()
        const cipherParams = CryptoJS.lib.CipherParams.create({
            ciphertext: CryptoJS.enc.Base64.parse(cipherText)
        })
        const decrypted = CryptoJS.AES.decrypt(cipherParams, key, {
            iv,
            mode: CryptoJS.mode.CBC,
            padding: CryptoJS.pad.Pkcs7
        })
        const plain = CryptoJS.enc.Utf8.stringify(decrypted)
        return plain || null
    } catch {
        return null
    }
}

/**
 * JSON 序列化后 AES 加密（Base64）
 */
export function encryptJsonToBase64(payload: unknown): string {
    return encryptAesCbcPkcs7Base64(JSON.stringify(payload))
}

/**
 * 创建订单：订单参数（含 token）JSON 序列化后 AES 加密，得到作为 HTTP 原始 body 发送的 Base64 密文字符串
 */
export function encryptCreateOrdersPayload(params: CreateOrdersParams & { token: string }): string {
    return encryptJsonToBase64(params)
}
