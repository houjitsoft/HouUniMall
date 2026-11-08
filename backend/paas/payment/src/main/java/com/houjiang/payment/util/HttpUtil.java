package com.houjiang.payment.util;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * HTTP请求工具类
 * 用于调用通企付API接口
 * 
 * @author Amos
 */
@Slf4j
public class HttpUtil {
    
    /**
     * 默认连接超时时间(毫秒)
     */
    private static final int DEFAULT_CONNECT_TIMEOUT = 30000;
    
    /**
     * 默认读取超时时间(毫秒)
     */
    private static final int DEFAULT_READ_TIMEOUT = 30000;
    
    /**
     * POST请求 - JSON格式
     * 
     * @param url 请求地址
     * @param jsonBody JSON请求体
     * @return 响应内容
     */
    public static String post(String url, String jsonBody) {
        return post(url, jsonBody, DEFAULT_CONNECT_TIMEOUT, DEFAULT_READ_TIMEOUT);
    }
    
    /**
     * POST请求 - JSON格式（自定义超时时间）
     * 
     * @param url 请求地址
     * @param jsonBody JSON请求体
     * @param connectTimeout 连接超时时间(毫秒)
     * @param readTimeout 读取超时时间(毫秒)
     * @return 响应内容
     */
    public static String post(String url, String jsonBody, int connectTimeout, int readTimeout) {
        log.info("发送POST请求, url:{}", url);
        log.debug("请求体: {}", jsonBody);
        
        try {
            HttpResponse response = HttpRequest.post(url)
                    .contentType("application/json")
                    .charset(StandardCharsets.UTF_8)
                    .body(jsonBody)
                    .timeout(connectTimeout)
                    .execute();
            
            int status = response.getStatus();
            String body = response.body();
            
            log.info("收到响应, status:{}", status);
            log.debug("响应体: {}", body);
            
            if (status != 200) {
                log.error("HTTP请求失败, status:{}, body:{}", status, body);
                throw new RuntimeException("HTTP请求失败, status:" + status);
            }
            
            return body;
            
        } catch (Exception e) {
            log.error("HTTP请求异常, url:{}", url, e);
            throw new RuntimeException("HTTP请求异常: " + e.getMessage(), e);
        }
    }
    
    /**
     * POST请求 - 表单格式
     * 
     * @param url 请求地址
     * @param formParams 表单参数
     * @return 响应内容
     */
    public static String postForm(String url, Map<String, Object> formParams) {
        return postForm(url, formParams, DEFAULT_CONNECT_TIMEOUT, DEFAULT_READ_TIMEOUT);
    }
    
    /**
     * POST请求 - 表单格式（自定义超时时间）
     * 
     * @param url 请求地址
     * @param formParams 表单参数
     * @param connectTimeout 连接超时时间(毫秒)
     * @param readTimeout 读取超时时间(毫秒)
     * @return 响应内容
     */
    public static String postForm(String url, Map<String, Object> formParams, int connectTimeout, int readTimeout) {
        log.info("发送POST表单请求, url:{}", url);
        log.debug("表单参数: {}", formParams);
        
        try {
            HttpResponse response = HttpRequest.post(url)
                    .form(formParams)
                    .charset(StandardCharsets.UTF_8)
                    .timeout(connectTimeout)
                    .execute();
            
            int status = response.getStatus();
            String body = response.body();
            
            log.info("收到响应, status:{}", status);
            log.debug("响应体: {}", body);
            
            if (status != 200) {
                log.error("HTTP请求失败, status:{}, body:{}", status, body);
                throw new RuntimeException("HTTP请求失败, status:" + status);
            }
            
            return body;
            
        } catch (Exception e) {
            log.error("HTTP请求异常, url:{}", url, e);
            throw new RuntimeException("HTTP请求异常: " + e.getMessage(), e);
        }
    }
    
    /**
     * GET请求
     * 
     * @param url 请求地址
     * @return 响应内容
     */
    public static String get(String url) {
        return get(url, DEFAULT_CONNECT_TIMEOUT, DEFAULT_READ_TIMEOUT);
    }
    
    /**
     * GET请求（自定义超时时间）
     * 
     * @param url 请求地址
     * @param connectTimeout 连接超时时间(毫秒)
     * @param readTimeout 读取超时时间(毫秒)
     * @return 响应内容
     */
    public static String get(String url, int connectTimeout, int readTimeout) {
        log.info("发送GET请求, url:{}", url);
        
        try {
            HttpResponse response = HttpRequest.get(url)
                    .charset(StandardCharsets.UTF_8)
                    .timeout(connectTimeout)
                    .execute();
            
            int status = response.getStatus();
            String body = response.body();
            
            log.info("收到响应, status:{}", status);
            log.debug("响应体: {}", body);
            
            if (status != 200) {
                log.error("HTTP请求失败, status:{}, body:{}", status, body);
                throw new RuntimeException("HTTP请求失败, status:" + status);
            }
            
            return body;
            
        } catch (Exception e) {
            log.error("HTTP请求异常, url:{}", url, e);
            throw new RuntimeException("HTTP请求异常: " + e.getMessage(), e);
        }
    }
}
