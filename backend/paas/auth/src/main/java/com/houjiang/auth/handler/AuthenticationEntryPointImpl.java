package com.houjiang.auth.handler;

import com.alibaba.fastjson.JSON;
import com.houjiang.commons.util.ResponseResult;
import com.houjiang.commons.util.WebUtils;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

//spring security权限异常拦截

/**
 * @author Amos
 */
@Component
public class AuthenticationEntryPointImpl implements AuthenticationEntryPoint {
    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException, ServletException {

        System.out.println(authException.getLocalizedMessage()+"---"+authException.getMessage());

        ResponseResult result = ResponseResult.fail(HttpStatus.UNAUTHORIZED.value(), authException.getMessage());
        String json = JSON.toJSONString(result);
        WebUtils.renderString(response,json);
    }



}
