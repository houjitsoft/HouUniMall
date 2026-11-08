package com.houjiang.merchant.common.util;

import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.List;

/**
 * 请求工具类
 */
public class RequestUtils {

    /**
     * 获取请求对象
     *
     * @author Amos
     * @return HttpServletRequest
     */
    public static HttpServletRequest handler() {
        ServletRequestAttributes servletRequestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (servletRequestAttributes != null) {
            return servletRequestAttributes.getRequest();
        }
        return null;
    }

    /**
     * 获取不带参请求URl
     * 示例: https://127.0.0.1:8082/api/system/menu/menus
     *
     * @author Amos
     * @return String
     */
    public static String url() {
        HttpServletRequest request = RequestUtils.handler();
        if (request != null) {
            return request.getRequestURL().toString();
        }
        return "";
    }

    /**
     * 获取带端口的请求地址
     * 示例: https://127.0.0.1:8082
     *
     * @author Amos
     * @return String
     */
    public static String uri() {
        String domain = RequestUtils.domain();
        if (!Arrays.asList(443,80,0).contains(RequestUtils.port())) {
            domain += ":" + RequestUtils.port();
        }

        return domain;
    }

    /**
     * 获取请求路由
     * 示例: /api/system/menu/menus
     *
     * @author Amos
     * @return String
     */
    public static String route() {
        HttpServletRequest request = RequestUtils.handler();
        if (request != null) {
            return request.getRequestURI();
        }
        return "";
    }

    /**
     * 获取请求端口
     * 示例: 443/80
     *
     * @author Amos
     * @return Integer
     */
    public static Integer port() {
        HttpServletRequest request = RequestUtils.handler();
        if (request != null) {
            return request.getServerPort();
        }
        return 0;
    }

    /**
     * 获取请求域名
     * 示例: https://127.0.0.1
     *
     * @author Amos
     * @return String
     */
    public static String domain() {
        HttpServletRequest request = RequestUtils.handler();
        if (request != null) {
            String requestUrl = request.getRequestURL().toString();
            List<String> urls = Arrays.asList(requestUrl.split("/"));

            // 优先从反向代理请求头获取协议（如 Nginx 代理后 X-Forwarded-Proto）
            String forwardedProto = request.getHeader("X-Forwarded-Proto");
            String agree;
            if (forwardedProto != null && !forwardedProto.isEmpty()) {
                agree = forwardedProto.split(",")[0].trim() + ":";
            } else {
                // 备用：从请求 URL 直接获取协议
                // "https:" 或 "http:"
                agree = urls.get(0);
                if (request.getServerPort() == 443 || requestUrl.startsWith("https")) {
                    agree = "https:";
                }
            }

            return agree + "//" + urls.get(2).split(":")[0];
        }
        return null;
    }

    /**
     * 获取设备标识
     *
     * @author Amos
     * @return String
     */
    public static String device() {
        HttpServletRequest request = RequestUtils.handler();
        if (request != null) {
            String userAgent = request.getHeader("User-Agent");
            if(userAgent.contains("Android")) {
                return "Android";
            }else if(userAgent.contains("iPhone") || userAgent.contains("iPod") || userAgent.contains("iPad")) {
                return "IOS";
            } else {
                return "wap";
            }
        }

        return "wap";
    }

    /**
     * 判断是否是GET请求
     *
     * @author Amos
     * @return Boolean
     */
    public static Boolean isGet() {
        ServletRequestAttributes servletRequestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (servletRequestAttributes != null) {
            HttpServletRequest request = servletRequestAttributes.getRequest();
            return request.getMethod().equals("GET");
        }
        return false;
    }

    /**
     * 判断是否是POST请求
     *
     * @author Amos
     * @return Boolean
     */
    public static Boolean isPost() {
        ServletRequestAttributes servletRequestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (servletRequestAttributes != null) {
            HttpServletRequest request = servletRequestAttributes.getRequest();
            return request.getMethod().equals("POST");
        }
        return false;
    }

    /**
     * 判断是否是PUT请求
     *
     * @author Amos
     * @return Boolean
     */
    public static Boolean isPUT() {
        ServletRequestAttributes servletRequestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (servletRequestAttributes != null) {
            HttpServletRequest request = servletRequestAttributes.getRequest();
            return request.getMethod().equals("PUT");
        }
        return false;
    }

    /**
     * 判断是否是DELETE请求
     *
     * @author Amos
     * @return Boolean
     */
    public static Boolean isDelete() {
        ServletRequestAttributes servletRequestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (servletRequestAttributes != null) {
            HttpServletRequest request = servletRequestAttributes.getRequest();
            return request.getMethod().equals("DELETE");
        }
        return false;
    }

}
