package com.houjiang.aiagent.handler;

import com.alibaba.fastjson.JSON;
import com.houjiang.commons.util.ResponseResult;
import com.houjiang.commons.util.WebUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * 鉴权失败（已登录但权限不足）响应，与其余 unimall-* 服务口径一致。
 */
@Component
public class AccessDeniedHandlerImpl implements AccessDeniedHandler {

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response,
                       AccessDeniedException accessDeniedException) throws IOException {
        ResponseResult<?> result = ResponseResult.fail(403, accessDeniedException.getMessage());
        WebUtils.renderString(response, JSON.toJSONString(result));
    }
}
