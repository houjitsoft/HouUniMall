package com.houjiang.auth.handler;


import com.alibaba.fastjson.JSON;
import com.houjiang.commons.exception.BizCodeEnume;
import com.houjiang.commons.util.ResponseResult;
import com.houjiang.commons.util.WebUtils;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

//spring security认证异常拦截

/**
 * @author Amos
 */
@Component
public class AccessDeniedHandlerImpl implements AccessDeniedHandler {


    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException) throws IOException, ServletException {
        ResponseResult result = ResponseResult.fail(HttpStatus.FORBIDDEN.value(), BizCodeEnume.INSUFFICIENT_PERMISSIONS.getMsg());
        //处理异常
        String json = JSON.toJSONString(result);
        WebUtils.renderString(response,json);

    }
}


