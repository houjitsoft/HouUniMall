package com.houjiang.commons.annnotation;

import com.alibaba.fastjson.JSON;
import com.houjiang.commons.constant.CommonStatusEnum;
import com.houjiang.commons.exception.BizCodeEnume;
import com.houjiang.commons.util.ParseTokenUtils;
import com.houjiang.commons.util.ResponseResult;
import com.houjiang.commons.vo.user.UserInfoVO;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;
import java.io.BufferedReader;
import java.util.Map;
import java.util.Objects;

/**
 * @author Amos
 */
@Component
@Aspect
public class AdminTokenLimitAspect {

    @Around("@annotation(adminTokenLimit)")
    public Object around(ProceedingJoinPoint joinPoint, AdminTokenLimit adminTokenLimit) throws Throwable {
        HttpServletRequest request = ((ServletRequestAttributes) Objects.requireNonNull(RequestContextHolder.getRequestAttributes())).getRequest();
        request.setCharacterEncoding("utf8");
        // 获取单个参数
        BufferedReader reader = request.getReader();
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            sb.append(line);
        }
        String requestBody = sb.toString();
        Map<String, Object> map = JSON.parseObject(requestBody, Map.class);
        // 获取请求注解上的 value 值，作为唯一标识用户的 key
        String key = map.get("token") + "";
        if ("".equals(key)) {
            throw new SecurityException(BizCodeEnume.USER_DOES_NOT_EXIST.getMsg());
        }

        // 检查是否超过限制
        UserInfoVO platformUser = ParseTokenUtils.getPlatformUser(key);
        if (null == platformUser) {
            throw new SecurityException(BizCodeEnume.TOKEN_IS_INVALID.getMsg());
        }

        // 处理请求
        return joinPoint.proceed();
    }


}
