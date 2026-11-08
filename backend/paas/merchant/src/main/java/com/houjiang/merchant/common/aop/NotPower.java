package com.houjiang.merchant.common.aop;

import java.lang.annotation.*;

/**
 * 免权限校验注解类（已废弃）
 * 已迁移到Spring Security + JWT认证机制
 * 权限校验由拦截器中的checkAuth方法处理
 * 
 * @deprecated 使用Spring Security权限机制替代，不再需要此注解
 */
@Deprecated
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface NotPower {
}
