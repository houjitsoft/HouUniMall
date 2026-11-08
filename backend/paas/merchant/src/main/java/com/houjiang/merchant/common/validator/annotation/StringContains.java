package com.houjiang.merchant.common.validator.annotation;

import com.houjiang.merchant.common.validator.StringContainsValidator;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = StringContainsValidator.class)
@Target({ ElementType.PARAMETER,ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
public @interface StringContains {

    String message() default "字符串不符合规则";

    String[] values() default {};

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default { };

}
