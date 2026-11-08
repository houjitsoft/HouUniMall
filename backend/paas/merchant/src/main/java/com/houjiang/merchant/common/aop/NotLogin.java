package com.houjiang.merchant.common.aop;

import java.lang.annotation.*;

/**
 * 免登录校验注解类（已废弃）
 * 已迁移到Spring Security + JWT认证机制
 * 白名单路径配置在SecurityConfig中
 * 
 * @deprecated 使用SecurityConfig白名单替代，不再需要此注解
 */
@Deprecated
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface NotLogin {
}
