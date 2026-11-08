package com.houjiang.merchant.common.validator.annotation;

import com.houjiang.merchant.common.validator.IntArrayEmptyValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = IntArrayEmptyValidator.class)
@Target({ ElementType.PARAMETER,ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
public @interface IntArrayEmpty {

    String message() default "数组不允许为空";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default { };

}
