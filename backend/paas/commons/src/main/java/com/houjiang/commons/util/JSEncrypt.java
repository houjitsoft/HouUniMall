package com.houjiang.commons.util;

import com.houjiang.commons.config.RSAKeyConfig;
import org.apache.commons.codec.binary.Base64;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import javax.crypto.Cipher;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.Security;
import java.security.spec.PKCS8EncodedKeySpec;

/**
 * @author Amos
 */
public class JSEncrypt {

    /**
     * 解密方法 - 优先使用数据库加载的私钥，回退到默认私钥
     */
    public static String decrypt(String encryptedData) throws Exception {
        String privateKey = RSAKeyConfig.getCachedPrivateKey();
        if (privateKey == null || privateKey.isBlank()) {
            throw new IllegalStateException("RSA private key is not configured");
        }
        return decrypt(encryptedData, privateKey);
    }

    /**
     * 解密方法 - 支持传入私钥
     * @param encryptedData 加密的数据
     * @param pemPrivateKey PEM格式的私钥（可以是PKCS#1或PKCS#8格式）
     * @return 解密后的明文
     * @throws Exception 解密失败时抛出异常
     */
    public static String decrypt(String encryptedData, String pemPrivateKey) throws Exception {
        // 检查输入是否为空
        if (encryptedData == null || encryptedData.isEmpty()) {
            throw new IllegalArgumentException("加密数据不能为空");
        }
        
        if (pemPrivateKey == null || pemPrivateKey.trim().isEmpty()) {
            throw new IllegalArgumentException("私钥不能为空");
        }
        
        Security.addProvider(new BouncyCastleProvider());
        
        // 处理私钥格式
        String processedPrivateKey = pemPrivateKey.replaceAll("\\n", "")
                .replace("-----BEGIN RSA PRIVATE KEY-----", "")
                .replace("-----END RSA PRIVATE KEY-----", "")
                .replace("-----BEGIN PRIVATE KEY-----", "")
                .replace("-----END PRIVATE KEY-----", "");
        // 使用Base64解码得到字节数组（使用处理后的私钥）
        byte[] privateKeyBytes = java.util.Base64.getDecoder().decode(processedPrivateKey);

        PrivateKey privateKey;
        try {
            // 尝试方法1: 先尝试PKCS#8格式（标准格式，兼容性更好）
            try {
                PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(privateKeyBytes);
                KeyFactory keyFactory = KeyFactory.getInstance("RSA");
                privateKey = keyFactory.generatePrivate(keySpec);
            } catch (Exception e1) {
                // 尝试方法2: PKCS#1格式解析（使用BouncyCastle）
                try {
                    // 使用BouncyCastle解析PKCS#1格式
                    org.bouncycastle.asn1.ASN1Sequence seq = org.bouncycastle.asn1.ASN1Sequence.getInstance(privateKeyBytes);
                    org.bouncycastle.asn1.pkcs.RSAPrivateKey rsaPrivKey = org.bouncycastle.asn1.pkcs.RSAPrivateKey.getInstance(seq);
                    java.security.spec.RSAPrivateKeySpec rsaPrivKeySpec = new java.security.spec.RSAPrivateKeySpec(
                            rsaPrivKey.getModulus(), rsaPrivKey.getPrivateExponent());
                    
                    KeyFactory keyFactory = KeyFactory.getInstance("RSA");
                    privateKey = keyFactory.generatePrivate(rsaPrivKeySpec);
                } catch (Exception e2) {
                    // 尝试方法3: 使用BouncyCastle提供者
                    try {
                        org.bouncycastle.asn1.pkcs.RSAPrivateKey rsaPrivKey = org.bouncycastle.asn1.pkcs.RSAPrivateKey.getInstance(privateKeyBytes);
                        java.security.spec.RSAPrivateKeySpec rsaPrivKeySpec = new java.security.spec.RSAPrivateKeySpec(
                                rsaPrivKey.getModulus(), rsaPrivKey.getPrivateExponent());
                        
                        KeyFactory keyFactory = KeyFactory.getInstance("RSA", "BC");
                        privateKey = keyFactory.generatePrivate(rsaPrivKeySpec);
                    } catch (Exception e3) {
                        throw new Exception("无法解析RSA私钥，已尝试PKCS#8、PKCS#1和BC提供者: " + e3.getMessage(), e3);
                    }
                }
            }
        } catch (Exception e) {
            throw new Exception("RSA私钥解析失败: " + e.getMessage(), e);
        }
        
        // 解密 - 使用正确的填充方式
        // 不指定提供者
        Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        cipher.init(Cipher.DECRYPT_MODE, privateKey);
        byte[] encryptedBytes = Base64.decodeBase64(encryptedData);
        // 添加长度检查
        if (encryptedBytes.length == 0) {
            throw new IllegalArgumentException("加密数据长度为0");
        }
        
        byte[] decryptedData = cipher.doFinal(encryptedBytes);

        return new String(decryptedData, StandardCharsets.UTF_8);
    }
}
