package com.houjiang.payment.util;

import cn.hutool.crypto.SecureUtil;
import cn.hutool.crypto.asymmetric.Sign;
import cn.hutool.crypto.asymmetric.SignAlgorithm;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 签名工具类
 * 按照文档规范实现MD5/RSA/SM2签名
 * 
 * @author Amos
 */
@Slf4j
public class SignUtil {
    
    /**
     * 生成MD5签名
     * 
     * @param params 参数Map
     * @param key MD5密钥
     * @return 签名结果（大写）
     */
    public static String signMD5(Map<String, String> params, String key) {
        // 第一步：参数排序拼接
        String stringA = buildSignString(params);
        
        // 第二步：拼接密钥
        String stringSignTemp = stringA + "&key=" + key;
        
        log.debug("MD5签名原文: {}", stringSignTemp);
        
        // 第三步：MD5运算并转大写
        String sign = SecureUtil.md5(stringSignTemp).toUpperCase();
        
        log.debug("MD5签名结果: {}", sign);
        
        return sign;
    }
    
    /**
     * 验证MD5签名
     * 
     * @param params 参数Map（包含sign字段）
     * @param key MD5密钥
     * @return true-验证通过 false-验证失败
     */
    public static boolean verifyMD5(Map<String, String> params, String key) {
        if (params == null || !params.containsKey("sign")) {
            return false;
        }
        
        String receivedSign = params.get("sign");
        
        // 移除sign参数后重新计算签名
        Map<String, String> signParams = new HashMap<>(params);
        signParams.remove("sign");
        
        String calculatedSign = signMD5(signParams, key);
        
        boolean result = calculatedSign.equals(receivedSign);
        
        if (!result) {
            log.warn("MD5签名验证失败! 接收到的签名:{}, 计算的签名:{}", receivedSign, calculatedSign);
        }
        
        return result;
    }
    
    /**
     * 生成RSA签名
     * 
     * @param params 参数Map
     * @param privateKey RSA私钥
     * @return 签名结果（Base64编码）
     */
    public static String signRSA(Map<String, String> params, String privateKey) {
        // 参数排序拼接（RSA不拼接key）
        String stringA = buildSignString(params);
        
        log.debug("RSA签名原文: {}", stringA);
        
        // RSA签名
        Sign sign = SecureUtil.sign(SignAlgorithm.SHA256withRSA, privateKey, null);
        byte[] signBytes = sign.sign(stringA.getBytes(StandardCharsets.UTF_8));
        String signResult = Base64.getEncoder().encodeToString(signBytes);
        
        log.debug("RSA签名结果: {}", signResult);
        
        return signResult;
    }
    
    /**
     * 验证RSA签名
     * 
     * @param params 参数Map（包含sign字段）
     * @param publicKey RSA公钥
     * @return true-验证通过 false-验证失败
     */
    public static boolean verifyRSA(Map<String, String> params, String publicKey) {
        if (params == null || !params.containsKey("sign")) {
            return false;
        }
        
        String receivedSign = params.get("sign");
        
        // 移除sign参数
        Map<String, String> signParams = new HashMap<>(params);
        signParams.remove("sign");
        
        String stringA = buildSignString(signParams);
        
        // RSA验签
        Sign sign = SecureUtil.sign(SignAlgorithm.SHA256withRSA, null, publicKey);
        byte[] signBytes = Base64.getDecoder().decode(receivedSign);
        
        boolean result = sign.verify(stringA.getBytes(StandardCharsets.UTF_8), signBytes);
        
        if (!result) {
            log.warn("RSA签名验证失败! 签名原文:{}", stringA);
        }
        
        return result;
    }
    
    /**
     * 生成SM2签名（国密）
     * 
     * @param params 参数Map
     * @param privateKey SM2私钥
     * @return 签名结果（Base64编码）
     */
    public static String signSM2(Map<String, String> params, String privateKey) {
        // 参数排序拼接（SM2不拼接key）
        String stringA = buildSignString(params);
        
        log.debug("SM2签名原文: {}", stringA);
        
        try {
            // SM2签名 - 使用国密算法
            cn.hutool.crypto.asymmetric.SM2 sm2 = new cn.hutool.crypto.asymmetric.SM2(privateKey, null);
            byte[] signBytes = sm2.sign(stringA.getBytes(StandardCharsets.UTF_8));
            String signResult = Base64.getEncoder().encodeToString(signBytes);
            
            log.debug("SM2签名结果: {}", signResult);
            
            return signResult;
        } catch (Exception e) {
            log.error("SM2签名失败", e);
            throw new RuntimeException("SM2签名失败", e);
        }
    }
    
    /**
     * 验证SM2签名
     * 
     * @param params 参数Map（包含sign字段）
     * @param publicKey SM2公钥
     * @return true-验证通过 false-验证失败
     */
    public static boolean verifySM2(Map<String, String> params, String publicKey) {
        if (params == null || !params.containsKey("sign")) {
            return false;
        }
        
        String receivedSign = params.get("sign");
        
        // 移除sign参数
        Map<String, String> signParams = new HashMap<>(params);
        signParams.remove("sign");
        
        String stringA = buildSignString(signParams);
        
        try {
            // SM2验签
            cn.hutool.crypto.asymmetric.SM2 sm2 = new cn.hutool.crypto.asymmetric.SM2(null, publicKey);
            byte[] signBytes = Base64.getDecoder().decode(receivedSign);
            
            boolean result = sm2.verify(stringA.getBytes(StandardCharsets.UTF_8), signBytes);
            
            if (!result) {
                log.warn("SM2签名验证失败! 签名原文:{}", stringA);
            }
            
            return result;
        } catch (Exception e) {
            log.error("SM2验签失败", e);
            return false;
        }
    }
    
    /**
     * 构建签名字符串
     * 规则：
     * 1. 过滤空值参数
     * 2. 按参数名ASCII码排序
     * 3. 使用&连接：key1=value1&key2=value2
     * 
     * @param params 参数Map
     * @return 签名原文字符串
     */
    private static String buildSignString(Map<String, String> params) {
        if (params == null || params.isEmpty()) {
            return "";
        }
        
        // 过滤空值并按key排序
        return params.entrySet().stream()
                // 过滤空值（null或空字符串）
                .filter(entry -> StringUtils.isNotBlank(entry.getValue()))
                // 排除sign参数本身
                .filter(entry -> !"sign".equals(entry.getKey()))
                // 按key的ASCII码排序（字典序）
                .sorted(Map.Entry.comparingByKey())
                // 拼接成 key=value 格式
                .map(entry -> entry.getKey() + "=" + entry.getValue())
                // 用 & 连接
                .collect(Collectors.joining("&"));
    }
    
    /**
     * 将对象转换为签名Map
     * 使用反射提取对象的所有字段
     * 
     * @param obj 待签名对象
     * @return 参数Map
     */
    public static Map<String, String> objectToSignMap(Object obj) {
        Map<String, String> map = new HashMap<>();
        
        if (obj == null) {
            return map;
        }
        
        try {
            Class<?> clazz = obj.getClass();
            java.lang.reflect.Field[] fields = clazz.getDeclaredFields();
            
            for (java.lang.reflect.Field field : fields) {
                field.setAccessible(true);
                Object value = field.get(obj);
                
                if (value != null && !"serialVersionUID".equals(field.getName())) {
                    map.put(field.getName(), value.toString());
                }
            }
        } catch (Exception e) {
            log.error("对象转签名Map失败", e);
        }
        
        return map;
    }
}
