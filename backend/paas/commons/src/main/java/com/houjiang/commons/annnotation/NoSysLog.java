package com.houjiang.commons.annnotation;

import java.lang.annotation.*;

/**
 * 跳过操作日志自动采集（及本方法上的自动审计）。
 * <p>仅用于心跳、探活等极高频非业务出口；业务增改删查不要使用。</p>
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface NoSysLog {
}
