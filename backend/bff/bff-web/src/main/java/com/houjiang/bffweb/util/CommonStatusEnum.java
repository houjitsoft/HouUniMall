package com.houjiang.bffweb.util;

import lombok.Getter;

/**
 * 通用状态枚举
/**
 * 
 * @author Amos
 * @since 2025-01-01
 */
public enum CommonStatusEnum {

    /**
     * 成功
     */
    SUCCESS(1, "操作成功"),
    
    /**
     * 失败
     */
    FAIL(0, "操作失败");

    @Getter
    private int code;
    
    @Getter
    private String value;

    CommonStatusEnum(int code, String value) {
        this.code = code;
        this.value = value;
    }
}

