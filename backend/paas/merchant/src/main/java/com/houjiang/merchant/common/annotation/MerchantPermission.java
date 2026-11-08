package com.houjiang.merchant.common.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Binds a merchant controller operation to one or more role-assignable menu permissions.
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface MerchantPermission {

    /**
     * The request is allowed when the current administrator owns any enabled menu token here.
     */
    String[] anyOf();
}
