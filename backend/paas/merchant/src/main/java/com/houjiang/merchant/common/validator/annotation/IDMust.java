package com.houjiang.merchant.common.validator.annotation;

import com.houjiang.merchant.common.validator.IDMustValidator;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = IDMustValidator.class)
@Target({ ElementType.PARAMETER,ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
public @interface IDMust {

    String message() default "id参数必须存在且大于0";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default { };

}
