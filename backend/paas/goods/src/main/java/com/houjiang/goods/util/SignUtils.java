package com.houjiang.goods.util;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 * @ClassName getSign
 * @Description
 * @Author Amos
 * @Date 2024/11/7 11:21
 * @Version 1.0
 * @author Amos
 */

public class SignUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(SignUtils.class);

    public static void localMain(String[] args) {


        String bodyStr = "{\"skuId\":42}";


//        String chkSign = getSign(bodyStr, "270c449611614f4f92a8b36433793fdc");
//        System.out.println("chkSign ======== " + chkSign + ", sign ====== e2bd3279cfe9c74623a8be6fa138231f");
    }

    /**
     * 获取签名
     *
     * @param bodyStr
     * @param appSecret
     * @return
     */
    public static String getSign(String bodyStr,String appKey, String appSecret,String randomNumber, Long timestamp) {

        Map<String, String> params = JSON.parseObject(bodyStr, new TypeReference<>() {
        });
        params.put("appKey", appKey);
        params.put("timeStamp", timestamp.toString());
        params.put("randomNumber", randomNumber);
        params.put("version", "1.0");

        for (Map.Entry<String, String> entry : params.entrySet()) {
            // 拼接参数值字符串并进行utf-8解码，防止中文乱码产生
            String value = "";
            try {
                value = URLDecoder.decode(entry.getValue(), StandardCharsets.UTF_8);
            } catch (Exception e) {
                LOGGER.error(e.getMessage());
            }
            if (StringUtils.isBlank(value)) {
                continue;
            }
            params.put(entry.getKey(), value);
        }
        // 将参数以参数名的字典升序排序
        Map<String, String> sortParams = new TreeMap<>(params);
        Set<Map.Entry<String, String>> entrys = sortParams.entrySet();

        // 遍历排序的字典,并拼接格式
        StringBuilder valueSb = new StringBuilder();
        for (Map.Entry<String, String> entry : entrys) {
            if (StringUtils.isNotBlank(entry.getValue())) {
                valueSb.append(entry.getKey()).append("=").append(entry.getValue()).append("&");
            }

        }
        valueSb.append(appSecret);
        String sign = valueSb.toString();
        LOGGER.warn("加密前的拼接字段:"+sign);
        try {
            //NOSONAR
            sign = DigestUtils.md5Hex(sign.getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            LOGGER.error(e.getMessage());
        }
        LOGGER.warn("---sign---"+sign);
        return sign;
    }

}
