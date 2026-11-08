package com.houjiang.merchant.common.validator;

import com.houjiang.merchant.common.validator.annotation.SqlSafe;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.regex.Pattern;

/**
 * @author Amos
 */
public class SqlSafeValidator implements ConstraintValidator<SqlSafe, String> {

    /** SQL注入关键词模式 */
    private static final Pattern SQL_INJECTION_PATTERN = Pattern.compile(
            "(?i).*(union|select|insert|delete|update|drop|create|alter|exec|execute|script|javascript|vbscript|onload|onerror|<script|</script).*"
    );

    /** 特殊字符模式 */
    private static final Pattern SPECIAL_CHARS_PATTERN = Pattern.compile(
            ".*[';\"\\\\|&$><`!].*"
    );

    @Override
    public void initialize(SqlSafe constraintAnnotation) {
        // 无需初始化配置
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null || value.trim().isEmpty()) {
            return true;
        }

        // 自动检测SQL注入关键词
        if (SQL_INJECTION_PATTERN.matcher(value).find()) {
            updateErrorMessage(context, "检测到SQL注入攻击关键词");
            return false;
        }

        // 检测危险特殊字符
        if (SPECIAL_CHARS_PATTERN.matcher(value).find()) {
            updateErrorMessage(context, "包含危险特殊字符");
            return false;
        }

        // 智能检测：如果是数字和逗号组合，验证格式
        if (value.contains(",")) {
            return isValidNumberList(value, context);
        }

        return true;
    }

    /**
     * 验证数字列表格式（如分类ID列表）
     */
    private boolean isValidNumberList(String value, ConstraintValidatorContext context) {
        if (!value.matches("^[0-9,\\s]*$")) {
            updateErrorMessage(context, "数字列表格式不正确，只能包含数字、逗号和空格");
            return false;
        }

        // 验证每个数字是否有效
        String[] parts = value.split(",");
        for (String part : parts) {
            String trimmed = part.trim();
            if (!trimmed.isEmpty()) {
                try {
                    int num = Integer.parseInt(trimmed);
                    if (num <= 0) {
                        updateErrorMessage(context, "数字必须大于0");
                        return false;
                    }
                } catch (NumberFormatException e) {
                    updateErrorMessage(context, "包含无效数字: " + trimmed);
                    return false;
                }
            }
        }
        return true;
    }

    private void updateErrorMessage(ConstraintValidatorContext context, String message) {
        context.disableDefaultConstraintViolation();
        context.buildConstraintViolationWithTemplate(message).addConstraintViolation();
    }
}