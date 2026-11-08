package com.houjiang.permission.security;

import com.alibaba.fastjson.JSON;
import com.houjiang.commons.exception.BizCodeEnume;
import com.houjiang.commons.exception.DefinedException;
import com.houjiang.commons.util.ResponseResult;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.ModelAndView;

import java.io.IOException;

/**
 * Keeps security failures consistent at both the servlet-filter and MVC layers.
 */
@Slf4j
@Component
public class PermissionSecurityExceptionHandler implements HandlerExceptionResolver,
        AuthenticationEntryPoint, AccessDeniedHandler, Ordered {

    @Override
    public ModelAndView resolveException(HttpServletRequest request, HttpServletResponse response,
                                         Object handler, Exception exception) {
        if (exception instanceof ResponseStatusException statusException) {
            write(response, statusException.getStatusCode().value(), message(statusException));
            return new ModelAndView();
        }
        if (exception instanceof DefinedException definedException) {
            int status = statusFor(definedException);
            write(response, status, definedException.getMessage());
            return new ModelAndView();
        }
        if (exception instanceof AccessDeniedException) {
            write(response, HttpStatus.FORBIDDEN.value(), exception.getMessage());
            return new ModelAndView();
        }
        if (exception instanceof AuthenticationException) {
            write(response, HttpStatus.UNAUTHORIZED.value(), exception.getMessage());
            return new ModelAndView();
        }
        return null;
    }

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException authException) throws IOException, ServletException {
        write(response, HttpStatus.UNAUTHORIZED.value(), authException.getMessage());
    }

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response,
                       AccessDeniedException accessDeniedException) throws IOException, ServletException {
        write(response, HttpStatus.FORBIDDEN.value(), accessDeniedException.getMessage());
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }

    private int statusFor(DefinedException exception) {
        int code = exception.getMessageCode();
        if (code == BizCodeEnume.USER_IS_NOT_LOGGED_IN.getCode()
                || code == BizCodeEnume.TOKEN_IS_ILLEGAL.getCode()
                || code == BizCodeEnume.TOKEN_IS_INVALID.getCode()) {
            return HttpStatus.UNAUTHORIZED.value();
        }
        if (code == BizCodeEnume.SYSTEM_ERROR.getCode()) {
            return HttpStatus.SERVICE_UNAVAILABLE.value();
        }
        return HttpStatus.INTERNAL_SERVER_ERROR.value();
    }

    private String message(ResponseStatusException exception) {
        return exception.getReason() == null
                ? HttpStatus.valueOf(exception.getStatusCode().value()).getReasonPhrase()
                : exception.getReason();
    }

    private void write(HttpServletResponse response, int status, String message) {
        if (response.isCommitted()) {
            return;
        }
        response.setStatus(status);
        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/json;charset=UTF-8");
        try {
            response.getWriter().write(JSON.toJSONString(ResponseResult.fail(status, message)));
        } catch (IOException exception) {
            log.error("写入权限服务安全异常响应失败", exception);
        }
    }
}
