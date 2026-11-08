package com.houjiang.commons.annnotation;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/** 表示它在运行时 */
@Retention(RUNTIME)
/** 表示它只能放在方法上 */
@Target(METHOD)
@SuppressWarnings("all")
public @interface AccessLimit {

}
