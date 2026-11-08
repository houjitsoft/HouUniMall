package com.houjiang.commons.util.secverifyapiutil;

import com.alibaba.fastjson.JSONObject;
import org.apache.commons.codec.binary.Base64;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * AES加密解密
 * @author Amos
 */
public class AesEncryptUtil {
    /***
     * key和iv值可以随机生成
     */
    private static String KEY = "2y0u2n5a0f1e2n4g";

    private static String IV = "y2u0a2n5f0e1n2g4";

    /***
     * 加密
     * @param  object 要加密的数据
     * @return encrypt
     */
    public static String encrypt(Object object){
        return encrypt(JSONObject.toJSONString(object), KEY, IV);
    }

    /***
     * param data 需要解密的数据
     * 调用desEncrypt（）方法
     */
    public static String desEncrypt(Object object){
        return desEncrypt(JSONObject.toJSONString(object), KEY, IV);
    }

    /**
     * 解密「已经是密文字符串」的入参（不走 JSON 序列化）。
     *
     * 背景：desEncrypt(Object) 内部会先 JSONObject.toJSONString(object)；当调用方传进来的
     * 本身就是 String（例如 @RequestBody String para 收到的密文）时，toJSONString 会再包一层
     * 双引号，变成 {@code "xxxx=="}，Base64 解码必然抛 IllegalArgumentException，最终表现为
     * 下单接口 403。涉及密文的入口必须走这个方法。
     *
     * @param data 原始的密文字符串（Base64）
     * @return 解密后的明文；传入 null 或解密失败时返回 null
     */
    public static String desEncryptRaw(String data){
        if (data == null) {
            return null;
        }
        // 兼容上下游把密文当 JSON 字符串发送的情况（首尾多出来的双引号）
        String trimmed = data.trim();
        if (trimmed.length() >= 2 && trimmed.startsWith("\"") && trimmed.endsWith("\"")) {
            trimmed = trimmed.substring(1, trimmed.length() - 1);
        }
        return desEncrypt(trimmed, KEY, IV);
    }

    /**
     * 加密方法
     * @param data  要加密的数据
     * @param key 加密key
     * @param iv 加密iv
     * @return 加密的结果
     */
    private static String encrypt(String data, String key, String iv){
        try {
            //"算法/模式/补码方式"NoPadding PkcsPadding
            Cipher cipher = Cipher.getInstance("AES/CBC/NoPadding");
            int blockSize = cipher.getBlockSize();

            byte[] dataBytes = data.getBytes();
            int plaintextLength = dataBytes.length;
            if (plaintextLength % blockSize != 0) {
                plaintextLength = plaintextLength + (blockSize - (plaintextLength % blockSize));
            }

            byte[] plaintext = new byte[plaintextLength];
            System.arraycopy(dataBytes, 0, plaintext, 0, dataBytes.length);

            SecretKeySpec keyspec = new SecretKeySpec(key.getBytes(), "AES");
            IvParameterSpec ivspec = new IvParameterSpec(iv.getBytes());

            cipher.init(Cipher.ENCRYPT_MODE, keyspec, ivspec);
            byte[] encrypted = cipher.doFinal(plaintext);

            return new Base64().encodeToString(encrypted);

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 解密方法
     * @param data 要解密的数据
     * @param key  解密key
     * @param iv 解密iv
     * @return 解密的结果
     */
    private static String desEncrypt(String data, String key, String iv){
        try {
            byte[] encrypted1 = new Base64().decode(data);

            Cipher cipher = Cipher.getInstance("AES/CBC/NoPadding");
            SecretKeySpec keySpec = new SecretKeySpec(key.getBytes(), "AES");
            IvParameterSpec ivSpec = new IvParameterSpec(iv.getBytes());
            cipher.init(Cipher.DECRYPT_MODE, keySpec, ivSpec);
            byte[] original = cipher.doFinal(encrypted1);
            return new String(original).trim();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}