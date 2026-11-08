package com.houjiang.merchant.common.validator;

import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.houjiang.merchant.common.validator.annotation.StringContains;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 验证字符串是否在数组中
 * @author Amos
 */
public class StringContainsValidator implements ConstraintValidator<StringContains, String> {

    private Set<String> limitValues;

    @Override
    public void initialize (StringContains stringContains) {
        limitValues = Arrays.stream(stringContains.values()).collect(Collectors.toSet());
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (StringUtils.isBlank(value)) {
            return true;
        }
        return limitValues.contains(value.trim());
    }

}
