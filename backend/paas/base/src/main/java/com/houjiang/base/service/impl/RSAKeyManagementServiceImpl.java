package com.houjiang.base.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.houjiang.base.entity.BaseSysConfigsEntity;
import com.houjiang.base.mapper.BaseSysConfigsMapper;
import com.houjiang.base.service.RSAKeyManagementService;
import com.houjiang.commons.constant.CommonType;
import com.houjiang.commons.util.JSEncrypt;
import com.houjiang.commons.util.ResponseResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

/**
 * RSA密钥管理Service实现类
 * 
 * @author Amos
 * @date 2026-01-04
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RSAKeyManagementServiceImpl implements RSAKeyManagementService {

    private final BaseSysConfigsMapper baseSysConfigsMapper;

    /** ConfigKey常量 */
    private static final String RSA_PUBLIC_KEY = "rsa_public_key";
    private static final String RSA_PRIVATE_KEY = "rsa_private_key";

    @Override
    public ResponseResult<String> getRSAPublicKey() {
        try {
            BaseSysConfigsEntity config = baseSysConfigsMapper.selectOne(
                new QueryWrapper<BaseSysConfigsEntity>().eq("config_key", RSA_PUBLIC_KEY)
            );
            
            if (config != null && config.getConfigValue() != null) {
                return ResponseResult.success(config.getConfigValue());
            }
            
            return ResponseResult.success("未配置RSA公钥");
        } catch (Exception e) {
            log.error("获取RSA公钥失败", e);
            return ResponseResult.error("获取RSA公钥失败: " + e.getMessage());
        }
    }

    @Override
    public ResponseResult<Map<String, String>> getRSAKeys() {
        try {
            BaseSysConfigsEntity publicKeyConfig = baseSysConfigsMapper.selectOne(
                new QueryWrapper<BaseSysConfigsEntity>().eq("config_key", RSA_PUBLIC_KEY)
            );
            
            BaseSysConfigsEntity privateKeyConfig = baseSysConfigsMapper.selectOne(
                new QueryWrapper<BaseSysConfigsEntity>().eq("config_key", RSA_PRIVATE_KEY)
            );
            
            Map<String, String> keys = new HashMap<>();
            keys.put("publicKey", publicKeyConfig != null ? publicKeyConfig.getConfigValue() : "");
            keys.put("privateKey", privateKeyConfig != null ? privateKeyConfig.getConfigValue() : "");
            
            return ResponseResult.success(keys);
        } catch (Exception e) {
            log.error("获取RSA密钥配置失败", e);
            return ResponseResult.error("获取RSA密钥配置失败: " + e.getMessage());
        }
    }

    @Override
    public ResponseResult<String> saveRSAKeys(Map<String, String> keys) {
        try {
            String publicKey = keys.get("publicKey");
            String privateKey = keys.get("privateKey");
            
            if (publicKey == null || publicKey.trim().isEmpty()) {
                return ResponseResult.error("公钥不能为空");
            }
            
            if (privateKey == null || privateKey.trim().isEmpty()) {
                return ResponseResult.error("私钥不能为空");
            }
            
            // 验证公钥格式
            if (!publicKey.contains("BEGIN PUBLIC KEY")) {
                return ResponseResult.error("公钥格式错误，应以-----BEGIN PUBLIC KEY-----开头");
            }
            
            // 验证私钥格式
            if (!privateKey.contains("BEGIN") || !privateKey.contains("PRIVATE KEY")) {
                return ResponseResult.error("私钥格式错误，应包含BEGIN PRIVATE KEY或BEGIN RSA PRIVATE KEY");
            }
            
            // 保存公钥
            saveOrUpdateConfig(RSA_PUBLIC_KEY, publicKey, "RSA公钥配置");
            
            // 保存私钥
            saveOrUpdateConfig(RSA_PRIVATE_KEY, privateKey, "RSA私钥配置");
            
            log.info("RSA密钥配置保存成功");
            return ResponseResult.success("RSA密钥配置保存成功，请重启auth服务使配置生效");
        } catch (Exception e) {
            log.error("保存RSA密钥配置失败", e);
            return ResponseResult.error("保存RSA密钥配置失败: " + e.getMessage());
        }
    }

    @Override
    public ResponseResult<Map<String, String>> generateRSAKeys(int keySize) {
        try {
            if (keySize != 1024 && keySize != 2048 && keySize != 4096) {
                return ResponseResult.error("密钥长度只支持1024、2048或4096位");
            }
            
            log.info("开始生成{}位RSA密钥对...", keySize);
            
            // 生成密钥对
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
            keyPairGenerator.initialize(keySize);
            KeyPair keyPair = keyPairGenerator.generateKeyPair();
            
            // 获取公钥和私钥
            PublicKey publicKey = keyPair.getPublic();
            PrivateKey privateKey = keyPair.getPrivate();
            
            // 转换为PEM格式
            String publicKeyPEM = encodeToPEM(publicKey.getEncoded(), "PUBLIC KEY");
            String privateKeyPEM = encodeToPEM(privateKey.getEncoded(), "RSA PRIVATE KEY");
            
            Map<String, String> keys = new HashMap<>();
            keys.put("publicKey", publicKeyPEM);
            keys.put("privateKey", privateKeyPEM);
            
            log.info("RSA密钥对生成成功，密钥长度: {}位", keySize);
            return ResponseResult.success(keys);
        } catch (Exception e) {
            log.error("生成RSA密钥对失败", e);
            return ResponseResult.error("生成RSA密钥对失败: " + e.getMessage());
        }
    }

    @Override
    public ResponseResult<Map<String, Object>> testRSAEncryption(Map<String, String> keys) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            String publicKey = keys.get("publicKey");
            String privateKey = keys.get("privateKey");
            
            if (publicKey == null || publicKey.trim().isEmpty()) {
                return ResponseResult.error("公钥不能为空");
            }
            
            if (privateKey == null || privateKey.trim().isEmpty()) {
                return ResponseResult.error("私钥不能为空");
            }
            
            // 测试数据
            String testData = "TestPassword123!@#";
            
            // 1. 验证公钥格式
            result.put("publicKeyFormat", publicKey.contains("BEGIN PUBLIC KEY") ? "正确" : "错误");
            
            // 2. 验证私钥格式
            result.put("privateKeyFormat", 
                (privateKey.contains("BEGIN RSA PRIVATE KEY") || privateKey.contains("BEGIN PRIVATE KEY")) ? "正确" : "错误");
            
            // 3. 测试加密
            try {
                String encryptedData = encryptWithPublicKey(testData, publicKey);
                result.put("encryptionTest", "通过");
                result.put("encryptedData", encryptedData.substring(0, Math.min(50, encryptedData.length())) + "...");
                
                // 4. 测试解密
                try {
                    String decryptedData = JSEncrypt.decrypt(encryptedData);
                    result.put("decryptionTest", "通过");
                    result.put("decryptedData", decryptedData);
                    result.put("dataMatch", testData.equals(decryptedData) ? "通过" : "失败");
                } catch (Exception e) {
                    result.put("decryptionTest", "失败: " + e.getMessage());
                    result.put("dataMatch", "未测试");
                }
            } catch (Exception e) {
                result.put("encryptionTest", "失败: " + e.getMessage());
                result.put("decryptionTest", "未测试");
                result.put("dataMatch", "未测试");
            }
            
            // 5. 总体状态
            boolean allPassed = "正确".equals(result.get("publicKeyFormat")) &&
                                "正确".equals(result.get("privateKeyFormat")) &&
                                "通过".equals(result.get("encryptionTest")) &&
                                "通过".equals(result.get("decryptionTest")) &&
                                "通过".equals(result.get("dataMatch"));
            
            result.put("overallStatus", allPassed ? "成功" : "失败");
            
            return ResponseResult.success(result);
        } catch (Exception e) {
            log.error("测试RSA加密解密失败", e);
            result.put("error", e.getMessage());
            result.put("overallStatus", "异常");
            return ResponseResult.error("测试失败: " + e.getMessage());
        }
    }

    /**
     * 保存或更新配置
     */
    private void saveOrUpdateConfig(String configKey, String configValue, String configName) {
        BaseSysConfigsEntity config = baseSysConfigsMapper.selectOne(
            new QueryWrapper<BaseSysConfigsEntity>().eq("config_key", configKey)
        );
        
        if (config == null) {
            // 新增
            config = new BaseSysConfigsEntity();
            config.setConfigKey(configKey);
            config.setConfigValue(configValue);
            // 使用 configComment 字段存储配置名称/描述
            config.setConfigComment(configName);
            // 使用 SETTING 类型，并转换为 String
            config.setConfigType(String.valueOf(CommonType.configType.SETTING.getCode()));
            // configEnable 是 Integer 类型，设置为 1
            config.setConfigEnable(1);
            // 设置操作类型为空
            config.setOpType("");
            // 设置操作类型值为空，满足数据库 NOT NULL 约束
            config.setOpValue("");
            baseSysConfigsMapper.insert(config);
        } else {
            // 更新
            baseSysConfigsMapper.update(null, 
                new UpdateWrapper<BaseSysConfigsEntity>()
                    .eq("config_key", configKey)
                    .set("config_value", configValue)
            );
        }
    }

    /**
     * 将密钥编码为PEM格式
     */
    private String encodeToPEM(byte[] keyBytes, String type) {
        StringBuilder pem = new StringBuilder();
        pem.append("-----BEGIN ").append(type).append("-----\n");
        
        String base64 = Base64.getEncoder().encodeToString(keyBytes);
        int index = 0;
        while (index < base64.length()) {
            int endIndex = Math.min(index + 64, base64.length());
            pem.append(base64, index, endIndex).append("\n");
            index = endIndex;
        }
        
        pem.append("-----END ").append(type).append("-----");
        return pem.toString();
    }

    /**
     * 使用公钥加密数据
     */
    private String encryptWithPublicKey(String data, String publicKeyPEM) throws Exception {
        // 移除PEM头尾
        String publicKeyContent = publicKeyPEM
            .replace("-----BEGIN PUBLIC KEY-----", "")
            .replace("-----END PUBLIC KEY-----", "")
            .replaceAll("\\s", "");
        
        byte[] keyBytes = Base64.getDecoder().decode(publicKeyContent);
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        PublicKey publicKey = keyFactory.generatePublic(keySpec);
        
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.ENCRYPT_MODE, publicKey);
        byte[] encryptedBytes = cipher.doFinal(data.getBytes(StandardCharsets.UTF_8));
        
        return Base64.getEncoder().encodeToString(encryptedBytes);
    }
}
