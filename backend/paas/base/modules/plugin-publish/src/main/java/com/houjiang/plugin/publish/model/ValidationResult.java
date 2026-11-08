package com.houjiang.plugin.publish.model;

import java.util.ArrayList;
import java.util.List;
import lombok.Data;

/**
 * 清单校验的结构化结果。
 *
 * <p><b>刻意不使用异常中断</b>：上传接口要一次性把「所有字段级问题」返回给发布者，
 * 让他一次改完，而不是改一个字段重试一次。只有 ERROR 级问题才会阻断上传，WARN 仅提示。
 *
 * @author Amos
 * @since M1
 */
@Data
public class ValidationResult {

    /**
     * 是否通过（没有任何 ERROR 级问题）。
     */
    private boolean pass;

    /**
     * 问题明细，字段级。
     */
    private List<ValidationIssue> issues = new ArrayList<>();

    /**
     * 构造一个通过的结果。
     *
     * @return 通过结果
     */
    public static ValidationResult ok() {
        ValidationResult result = new ValidationResult();
        result.setPass(true);
        return result;
    }

    /**
     * 由问题列表构造结果，自动判定是否通过。
     *
     * @param issues 问题明细
     * @return 校验结果
     */
    public static ValidationResult of(List<ValidationIssue> issues) {
        ValidationResult result = new ValidationResult();
        result.setIssues(issues == null ? new ArrayList<>() : new ArrayList<>(issues));
        result.setPass(result.getIssues().stream().noneMatch(ValidationIssue::isError));
        return result;
    }

    /**
     * 追加一条 ERROR 级问题（会阻断上传）。
     *
     * @param field 出问题的字段
     * @param code  错误码（{@code PluginDistErrorCode}）
     * @param msg   中文描述
     */
    public void addError(String field, int code, String msg) {
        issues.add(ValidationIssue.error(field, code, msg));
        pass = false;
    }

    /**
     * 追加一条 WARN 级问题（不阻断）。
     *
     * @param field 出问题的字段
     * @param code  错误码
     * @param msg   中文描述
     */
    public void addWarn(String field, int code, String msg) {
        issues.add(ValidationIssue.warn(field, code, msg));
    }

    /**
     * @return 是否存在 ERROR 级问题
     */
    public boolean hasError() {
        return issues.stream().anyMatch(ValidationIssue::isError);
    }

    /**
     * @return ERROR 级问题的首个错误码，没有则返回 0
     */
    public int firstErrorCode() {
        return issues.stream()
                .filter(ValidationIssue::isError)
                .map(ValidationIssue::getCode)
                .findFirst()
                .orElse(0);
    }
}
