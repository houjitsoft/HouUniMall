package com.houjiang.mall.validator;

import com.houjiang.commons.util.ResponseResult;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * 商城后台校验基类
 * <p>
 * 替代 like-admin 的 like-common Validator 工具，统一使用 jakarta.validation
 * 标准注解（@NotNull / @NotBlank / @Min / @Max ...）+ 本类的辅助方法。
 * <p>
 * 用法：
 * <pre>
 *   public class DictTypeParam extends BaseValidator {
 *       @NotBlank(message = "类型名称不能为空")
 *       private String name;
 *   }
 *
 // Controller 中
 *
 // 抛 IllegalArgumentException
 *   BaseValidator.validate(param);
 // 或者
/**
 *
 *   ResponseResult&lt;?&gt; err = BaseValidator.check(param);
 *   if (err != null) return err;
 * </pre>
 *
 * @author Amos
 */
public abstract class BaseValidator {

    private static final ValidatorFactory FACTORY = Validation.buildDefaultValidatorFactory();
    private static final Validator VALIDATOR = FACTORY.getValidator();

    /**
     * 校验对象，失败抛 {@link IllegalArgumentException}（由全局异常处理器统一转 ResponseResult）。
     *
     * @param target 待校验对象（任意带 jakarta.validation 注解的 DTO）
     */
    public static <T> void validate(T target) {
        String msg = firstErrorMessage(target);
        if (msg != null) {
            throw new IllegalArgumentException(msg);
        }
    }

    /**
     * 校验对象，失败返回失败响应；成功返回 null。
     *
     * @param target 待校验对象
     * @return null 表示校验通过；非 null 表示校验失败的 ResponseResult
     */
    public static <T> ResponseResult<Void> check(T target) {
        String msg = firstErrorMessage(target);
        if (msg == null) {
            return null;
        }
        return ResponseResult.error(msg);
    }

    /**
     * 取首个错误信息（按字段顺序）。
     *
     * @param target 待校验对象
     * @return 首条错误 message，无错误返回 null
     */
    public static <T> String firstErrorMessage(T target) {
        if (target == null) {
            return "参数不能为空";
        }
        Set<ConstraintViolation<T>> violations = VALIDATOR.validate(target);
        if (violations.isEmpty()) {
            return null;
        }
        return violations.iterator().next().getMessage();
    }

    /**
     * 取全部错误信息，逗号分隔。
     *
     * @param target 待校验对象
     * @return 错误信息组合串，无错误返回 null
     */
    public static <T> String allErrorMessages(T target) {
        if (target == null) {
            return "参数不能为空";
        }
        Set<ConstraintViolation<T>> violations = VALIDATOR.validate(target);
        if (violations.isEmpty()) {
            return null;
        }
        return violations.stream()
                .map(ConstraintViolation::getMessage)
                .collect(Collectors.joining(", "));
    }

    /**
     * 断言：id 必须是大于 0 的正整数。
     *
     * @param id    主键
     * @param field 字段名，用于错误提示
     */
    public static void mustId(Integer id, String field) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException(field + "必须为大于0的整数");
        }
    }

    /**
     * 断言：字符串不能为空白。
     *
     * @param value 字符串
     * @param field 字段名
     */
    public static void mustNotBlank(String value, String field) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(field + "不能为空");
        }
    }
}
