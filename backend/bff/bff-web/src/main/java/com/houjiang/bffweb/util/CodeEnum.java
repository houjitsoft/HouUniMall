package com.houjiang.bffweb.util;

/**
 * 错误码枚举接口
/**
 * 用于支持 ResponseResult 中接收枚举类型参数的方法
 * 
 * @author Amos
 * @since 2025-01-01
 */
public interface CodeEnum {
    /**
     * 获取错误码
/**
     * @return 错误码
     */
    int getCode();
    
    /**
     * 获取错误消息
/**
     * @return 错误消息
     */
    String getMsg();
}

