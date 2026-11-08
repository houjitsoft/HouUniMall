package com.houjiang.merchant.common.validator.annotation;

import com.houjiang.merchant.common.validator.IntegerContainsValidator;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = IntegerContainsValidator.class)
@Target({ ElementType.PARAMETER,ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
public @interface IntegerContains {

    String message() default "数值不符合规则";

    int[] values() default {};

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default { };

}
