package com.houjiang.commons.aspect;

import com.houjiang.commons.annnotation.RepeatClick;
import com.houjiang.commons.config.RedisCache;
import com.houjiang.commons.util.ResponseResult;
import org.apache.commons.codec.digest.DigestUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Amos
 */
@Aspect
@Component
public class RepeatClickAspect {
    private static final Logger log = LoggerFactory.getLogger(RepeatClickAspect.class);

    @Resource
    private RedisCache redisCache;

    /**
     * @param joinPoint
     * @param repeatClick
     * @return
     * @throws Throwable
     */
    @Around("@annotation(repeatClick)")
    public Object repeatClick(ProceedingJoinPoint joinPoint, RepeatClick repeatClick) throws Throwable {
        Object[] args = joinPoint.getArgs();
        /*
/**
         * 获取请求信息
         */
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder
                .getRequestAttributes();
        assert attributes != null;
        HttpServletRequest request = attributes.getRequest();
        // 获取执行方法
        Method method = ((MethodSignature) joinPoint.getSignature()).getMethod();
        // 获取防重复提交注解
        // 获取token以及方法标记，生成redisKey和redisValue
        String url = request.getRequestURI();
        // redisKey的前缀
        String PREVENT_DUPLICATION_PREFIX = "PREVENT_DUPLICATION_PREFIX:";
        /*
/**
         *  通过前缀 + url  + 函数参数签名 来生成redis上的 key
         */
        String redisKey = PREVENT_DUPLICATION_PREFIX
                .concat(url)
                .concat(getMethodSign(method, joinPoint.getArgs()));
        // 重复点击注解为空  跳过
        if (ObjectUtils.isEmpty(repeatClick)) {
            return joinPoint.proceed(args);
        }
        // 使用错误提示
        if (ObjectUtils.isEmpty(redisKey) || repeatClick.expireSeconds() == 0) {
            return joinPoint.proceed(args);
        }
        try {
            if (redisCache.hasKey(redisKey)) {
                return ResponseResult.fail(0, "请勿重复点击");
            }
            redisCache.setCacheObject(redisKey, UUID.randomUUID().toString());
            redisCache.expire(redisKey, repeatClick.expireSeconds(), repeatClick.unit());
        } catch (Exception e) {
            // Redis异常时不阻塞业务，仅记录警告日志，允许请求继续执行
            log.warn("RepeatClick Redis操作异常，跳过防重复检查: {}", e.getMessage());
        }
        return joinPoint.proceed(args);
    }
    /**
     * 生成方法标记：采用数字签名算法SHA1对方法签名字符串加签
     *
     * @param method 1
     * @param args 1
     * @return 1
     */
    private String getMethodSign(Method method, Object... args) {
        StringBuilder sb = new StringBuilder(method.toString());
        Object arg = args[0];
        sb.append(arg);
        return DigestUtils.sha1Hex(sb.toString());
    }
}