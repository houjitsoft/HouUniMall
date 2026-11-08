package com.houjiang.aiagent.handler;

import com.alibaba.fastjson.JSON;
import com.houjiang.commons.util.ResponseResult;
import com.houjiang.commons.util.WebUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * 未认证响应，与其余 unimall-* 服务口径一致。
 */
@Component
public class AuthenticationEntryPointImpl implements AuthenticationEntryPoint {

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException authException) throws IOException {
        ResponseResult<?> result = ResponseResult.fail(authException.getMessage());
        WebUtils.renderString(response, JSON.toJSONString(result));
    }
}
