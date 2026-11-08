import JSEncrypt from 'jsencrypt'

export default {
  encryptData: function (data) {
    // 公钥
    const publicKey =
      '-----BEGIN PUBLIC KEY-----\n' +
      'MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEA1MNwaogyNVlMWcSw1WlS\n' +
      '+qJ1tcvH1ZYtDsZe5nDcZe89UNPehzr/rU8VzNGBFaUIVEqHMAhtcJfQjbekIA7v\n' +
      'wGbok1RjhY/TwB1XY19x0spePw4OEbpo3hVOJJ8fP64tz21fUXSvTbUzPhPdtufg\n' +
      'OGGbdVeZr7liZoNz+zKNor8+EpaPJDmI2blq3RM6kRyjLxlicu2Kfct8N/etlw9x\n' +
      'aqdu6MgOGKzJi8ntET5lnMKiG4zRojKCTL6ocOs0ZeKC8aKvJPWB02lObWEfq45W\n' +
      'PyHQulFEDbZCbYgWMLB/Qobs/WOFK6Wnkxl2uCmf90irMybyJAsx7igMfl0OQ1AE\n' +
      'ywIDAQAB\n' +
      '-----END PUBLIC KEY-----'
    // 创建 JSEncrypt 实例
    const encrypt = new JSEncrypt()
    encrypt.setPublicKey(publicKey)
    // 加密数据
    const encrypted = encrypt.encrypt(data)
    return encrypted
  },
}
