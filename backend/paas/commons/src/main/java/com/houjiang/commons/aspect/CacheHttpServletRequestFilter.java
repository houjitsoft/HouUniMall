package com.houjiang.commons.aspect;

import org.springframework.stereotype.Component;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;

/**
 * @ClassName CacheHttpServletRequestFilter
 * @Description 1
 * @Author Amos
 * @Date 2023/6/14 19:38
 * @Version 1.0
 * @author Amos
 */

@Component
public class CacheHttpServletRequestFilter implements Filter {
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }
    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {

        HttpServletRequest httpServletRequest = (HttpServletRequest) servletRequest;
        String contentType = httpServletRequest.getContentType();

        //判断请求类型
        //表单请求
        if (contentType == null) {
            filterChain.doFilter(servletRequest, servletResponse);
            return;
        //文件上传类型
        } else if (contentType.startsWith("multipart/")) {
            filterChain.doFilter(servletRequest, servletResponse);
            return;
        //json请求
        } else if (contentType.startsWith("application/json")) {

            ServletRequest requestWrapper = null;
            if (servletRequest instanceof HttpServletRequest) {
                requestWrapper = new JsonRequestWrapper(httpServletRequest);
            }
            //获取请求中的流如何，将取出来的字符串，再次转换成流，然后把它放入到新request对象中。
            // 在chain.doFiler方法中传递新的request对象
            if (requestWrapper != null) {
                filterChain.doFilter(requestWrapper, servletResponse);
            } else {
                filterChain.doFilter(servletRequest, servletResponse);
            }
            return;
        } else {
            filterChain.doFilter(servletRequest, servletResponse);
            return;
        }

    }
    @Override
    public void destroy() {
    }
}