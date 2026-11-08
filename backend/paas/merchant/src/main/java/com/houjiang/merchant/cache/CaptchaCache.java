package com.houjiang.merchant.cache;

import com.houjiang.merchant.common.util.*;
import com.houjiang.commons.config.RedisCache;

import java.util.concurrent.TimeUnit;

/**
 * 验证码缓存器
 * @author Amos
 */
public class CaptchaCache {

    private static final String PREFIX = "merchant:";

    /**
     * 获取 RedisCache 实例
     *
     * @return RedisCache
     */
    private static RedisCache getRedisCache() {
        return SpringUtils.getBean(RedisCache.class);
    }

    public static String get(String uuid) {
        String ip = IpUtils.getIpAddress().replaceAll("\\.", "");
        String captchaKey = YmlUtils.get("merchant.captcha.token") + ip + ":" + uuid;
        Object code = getRedisCache().getCacheObject(PREFIX + captchaKey);

        if (StringUtils.isNull(code) || StringUtils.isEmpty(code.toString())) {
            return "";
        }

        getRedisCache().deleteObject(PREFIX + captchaKey);
        return code.toString();
    }

    public static void set(String code, String uuid) {
        String ip   = IpUtils.getIpAddress().replaceAll("\\.", "");
        String verifyKey = YmlUtils.get("merchant.captcha.token") + ip + ":" + uuid;
        long expireTime = Long.parseLong(YmlUtils.get("merchant.captcha.expire"));

        getRedisCache().setCacheObject(PREFIX + verifyKey, code.toLowerCase(), (int)expireTime, TimeUnit.SECONDS);
    }

}
