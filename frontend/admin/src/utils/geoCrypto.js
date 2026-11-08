import { JSEncrypt } from 'jsencrypt'

/** 使用服务端当前公钥加密密码，公钥可在系统配置中运行时轮换。 */
export function encryptPassword(password, publicKey) {
  if (!password) throw new Error('密码不能为空')
  if (!publicKey || publicKey === '未配置RSA公钥') throw new Error('系统未配置密码加密公钥')

  const encryptor = new JSEncrypt()
  encryptor.setPublicKey(publicKey)
  const encrypted = encryptor.encrypt(password)
  if (!encrypted) throw new Error('密码加密失败，请稍后重试')
  return encrypted
}
