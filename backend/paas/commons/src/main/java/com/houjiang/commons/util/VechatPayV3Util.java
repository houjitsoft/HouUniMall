package com.houjiang.commons.util;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.StringUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Random;

/**
 * @author Amos
 */
public class VechatPayV3Util {
    /**
     * @param method 请求方法 post
     * @param canonicalUrl 请求地址
     * @param body 请求参数
     * @param merchantId 这里用的商户号
     * @param certSerialNo 商户证书序列号
     * @param keyPath 商户证书地址
     * @return
     * @throws Exception
     */
    public static String getToken(
            String method,
            String canonicalUrl,
            String body,
            String merchantId,
            String certSerialNo,
            String keyPath) throws Exception {
        String signStr = "";
        //获取32位随机字符串
        String nonceStr = getRandomString(32);
        //当前系统运行时间
        long timestamp = System.currentTimeMillis() / 1000;
        if (StringUtils.isEmpty(body)) {
            body = "";
        }
        //签名操作
        String message = buildMessage(method, canonicalUrl, timestamp, nonceStr, body);
        //签名操作
        String signature = sign(message.getBytes("utf-8"), keyPath);
        //组装参数
        signStr = "mchid=\"" + merchantId + "\",timestamp=\"" +  timestamp+ "\",nonce_str=\"" + nonceStr
                + "\",serial_no=\"" + certSerialNo + "\",signature=\"" + signature + "\"";

        return signStr;
    }

    public static String buildMessage(String method, String canonicalUrl, long timestamp, String nonceStr, String body) {
//    String canonicalUrl = url.encodedPath();
//    if (url.encodedQuery() != null) {
//      canonicalUrl += "?" + url.encodedQuery();
//    }
        return method + "\n" + canonicalUrl + "\n" + timestamp + "\n" + nonceStr + "\n" + body + "\n";
    }

    public static String sign(byte[] message, String keyPath) throws Exception {
        Signature sign = Signature.getInstance("SHA256withRSA");
        sign.initSign(getPrivateKey(keyPath));
        sign.update(message);
        return Base64.encodeBase64String(sign.sign());
    }

    /**
     * 微信支付-前端唤起支付参数-获取商户私钥
     *
     * @param filename 私钥文件路径  (required)
     * @return 私钥对象
     */
    public static PrivateKey getPrivateKey(String filename) throws IOException {
        System.out.println("签名 证书地址是 "+filename);
        String content = new String(Files.readAllBytes(Paths.get(filename)), "utf-8");
        try {
            String privateKey = content.replace("-----BEGIN PRIVATE KEY-----", "")
                    .replace("-----END PRIVATE KEY-----", "")
                    .replaceAll("\\s+", "");
            //System.out.println("--------privateKey---------:"+privateKey);
            KeyFactory kf = KeyFactory.getInstance("RSA");

                PrivateKey privateKey1 = kf.generatePrivate(
                    new PKCS8EncodedKeySpec(Base64.decodeBase64(privateKey)));
            return privateKey1;
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("当前Java环境不支持RSA", e);
        } catch (InvalidKeySpecException e) {
            throw new RuntimeException("无效的密钥格式");
        }
    }
    /**
     * 获取随机位数的字符串
     * @param length
     * @return
     */
    public static String getRandomString(int length) {
        String base = "abcdefghijklmnopqrstuvwxyz0123456789";
        Random random = new Random();
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < length; i++) {
            int number = random.nextInt(base.length());
            sb.append(base.charAt(number));
        }
        return sb.toString();
    }
}
