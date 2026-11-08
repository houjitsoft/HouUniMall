package com.houjiang.bffweb.config;

import org.springframework.context.annotation.Configuration;

/**
 * 权限管理配置类
/**
 * 
 * @author Amos
 * @since 2025-01-07
 */
@Configuration
public class PermissionConfig {
    
    /** 权限管理相关配置 */
    public static final String DEFAULT_ORG_ID = "org_001";
    public static final String DEFAULT_SERVICE_NAME = "shell";
    public static final String DEFAULT_PERMISSION_TYPE = "READ";
    
    /** 权限服务配置 */
    public static final String PERMISSION_SERVICE_NAME = "permission";
    public static final String PERMISSION_SERVICE_URL = "http://permission";
}
