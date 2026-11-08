package com.houjiang.base.service;

import com.houjiang.commons.util.ResponseResult;

import java.util.Map;

/**
 * RSA密钥管理Service
 * 用于管理系统RSA加密密钥的配置
 * 
 * @author Amos
 * @date 2026-01-04
 */
public interface RSAKeyManagementService {

    /**
     * 获取RSA公钥
     * @return RSA公钥（PEM格式）
     */
    ResponseResult<String> getRSAPublicKey();

    /**
     * 获取RSA密钥配置（包含公钥和私钥）
     * 仅管理员可调用
     * @return RSA密钥配置
     */
    ResponseResult<Map<String, String>> getRSAKeys();

    /**
     * 保存RSA密钥配置
     * @param keys 包含publicKey和privateKey的Map
     * @return 操作结果
     */
    ResponseResult<String> saveRSAKeys(Map<String, String> keys);

    /**
     * 生成RSA密钥对
     * @param keySize 密钥长度（建议2048或4096）
     * @return 生成的密钥对（publicKey和privateKey）
     */
    ResponseResult<Map<String, String>> generateRSAKeys(int keySize);

    /**
     * 测试RSA加密解密功能
     * @param keys 要测试的密钥配置
     * @return 测试结果，包含加密、解密等状态信息
     */
    ResponseResult<Map<String, Object>> testRSAEncryption(Map<String, String> keys);
}
