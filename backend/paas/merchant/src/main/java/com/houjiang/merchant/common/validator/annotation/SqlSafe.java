package com.houjiang.merchant.common.validator.annotation;

import com.houjiang.merchant.common.validator.SqlSafeValidator;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Constraint(validatedBy = SqlSafeValidator.class)
public @interface SqlSafe {
    String message() default "输入内容包含SQL注入风险字符";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}