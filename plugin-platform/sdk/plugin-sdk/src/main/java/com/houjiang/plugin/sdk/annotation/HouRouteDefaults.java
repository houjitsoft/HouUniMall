package com.houjiang.plugin.sdk.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Default permissions for Spring mappings declared by one plugin controller.
 *
 * <p>{@link HouRoute} remains the method-level override for routes whose
 * permission does not follow the normal read/write convention. When a
 * controller uses this annotation, GET mappings receive {@link #readPermission()}
 * and other supported write mappings receive {@link #writePermission()}.
 */
@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface HouRouteDefaults {

    /** Permission used by GET mappings. */
    String readPermission() default "";

    /** Permission used by POST/PUT/DELETE mappings. */
    String writePermission() default "";
}
