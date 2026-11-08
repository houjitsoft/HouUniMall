package com.houjiang.commons.util;

import cn.hutool.captcha.CaptchaUtil;
import cn.hutool.captcha.CircleCaptcha;
import cn.hutool.captcha.generator.CodeGenerator;
import cn.hutool.captcha.generator.RandomGenerator;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * @ClassName ValidateCodeUtils
 * @Description ValidateCodeUtils
 * @Author Amos
 * @Date 2024/3/29 9:11
 * @Version 1.0
 * @author Amos
 */
@Service
public class ValidateCodeUtils {
    public static Map<String, String> generateValidateCode() {
        Map<String, String> codeMap = new HashMap<>();
        // 创建一个只包含数字的验证码生成器
        CodeGenerator generator = new RandomGenerator( "0123456789",4);
        //通过工具类生成图片验证码
        CircleCaptcha circleCaptcha = CaptchaUtil.createCircleCaptcha(150, 48, generator, 20);
        //验证码的值
        String codeValue = circleCaptcha.getCode();
        System.out.println("验证码为：" + codeValue);
        //将图片进行base64编码，并返回
        String imageBase64 = circleCaptcha.getImageBase64();
        codeMap.put("imageBase64", imageBase64);
        codeMap.put("codeValue", codeValue);
        return codeMap;
    }

}
