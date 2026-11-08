package com.houjiang.payment.controller.api;

import com.houjiang.commons.util.ResponseResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

/**
 * 支付短信验证码 Controller（Task 0 骨架）
 *
 * 对应前端 /api/payment/sms/send
 * Task 0：生成 6 位验证码写入 Redis(5 分钟)；真实短信通道在后续 Task 对接 thirdparty
 *
 * @author Amos
 */
@Slf4j
@RestController
@RequestMapping("/api/payment/sms")
public class SmsController {

    private final StringRedisTemplate redisTemplate;

    public SmsController(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    private static final String REDIS_KEY_PREFIX = "pay:sms:code:";
    private static final long EXPIRE_SECONDS = 300L;

    /**
     * 发送验证码
     * @param body {phone|mobile, scene}
     */
    @PostMapping("/send")
    public ResponseResult<Map<String, Object>> send(@RequestBody Map<String, String> body) {
        String mobile = body == null ? null : body.get("phone");
        if (!StringUtils.hasText(mobile)) {
            mobile = body == null ? null : body.get("mobile");
        }
        String scene = body == null ? "DEFAULT" : body.getOrDefault("scene", "DEFAULT");
        if (!StringUtils.hasText(mobile) || !mobile.matches("^1\\d{10}$")) {
            return ResponseResult.fail(500, "手机号格式不正确");
        }
        String code = String.valueOf(ThreadLocalRandom.current().nextInt(100000, 999999));
        redisTemplate.opsForValue().set(REDIS_KEY_PREFIX + scene + ":" + mobile,
                code, EXPIRE_SECONDS, TimeUnit.SECONDS);
        log.info("[sms/send] mobile={}, scene={}, code={}(仅开发日志，生产不打印)", mobile, scene, code);
        // TODO Task x: 调用 thirdparty-sms 真实发送
        return ResponseResult.success(Map.of("success", true, "expireIn", EXPIRE_SECONDS));
    }

    /**
     * 校验验证码
     */
    @PostMapping("/verify")
    public ResponseResult<Boolean> verify(@RequestBody Map<String, String> body) {
        String mobile = body == null ? null : body.get("phone");
        if (!StringUtils.hasText(mobile)) {
            mobile = body == null ? null : body.get("mobile");
        }
        String scene = body == null ? "DEFAULT" : body.getOrDefault("scene", "DEFAULT");
        String code = body == null ? null : body.get("code");
        if (!StringUtils.hasText(mobile) || !StringUtils.hasText(code)) {
            return ResponseResult.fail(500, "phone/code 必填");
        }
        String key = REDIS_KEY_PREFIX + scene + ":" + mobile;
        String cached = redisTemplate.opsForValue().get(key);
        if (cached == null) {
            return ResponseResult.fail(500, "验证码已过期");
        }
        if (!cached.equals(code)) {
            return ResponseResult.fail(500, "验证码不正确");
        }
        redisTemplate.delete(key);
        return ResponseResult.success(true);
    }
}
