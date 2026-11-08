package com.houjiang.plugin.publish.model;

import lombok.Data;

/**
 * 清单校验的单条问题。
 *
 * <p>{@code level} 取 {@code ERROR} / {@code WARN}，只有 {@code ERROR} 会阻断上传。
 * {@code code} 取自 {@code PluginDistErrorCode}，便于前端按错误码分支（不要依赖 msg 文案）。
 *
 * @author Amos
 * @since M1
 */
@Data
public class ValidationIssue {

    /**
     * 错误级别：ERROR / WARN。
     */
    public static final String LEVEL_ERROR = "ERROR";

    /**
     * 警告级别：ERROR / WARN。
     */
    public static final String LEVEL_WARN = "WARN";

    /**
     * 出问题的字段（如 code / version / minFrameworkVersion / dependencies / size / manifest）。
     */
    private String field;

    /**
     * 级别：ERROR / WARN。
     */
    private String level;

    /**
     * 错误码（{@code PluginDistErrorCode}）。
     */
    private int code;

    /**
     * 中文描述。
     */
    private String msg;

    /**
     * 构造 ERROR 级问题。
     *
     * @param field 字段
     * @param code  错误码
     * @param msg   描述
     * @return 问题
     */
    public static ValidationIssue error(String field, int code, String msg) {
        return of(field, LEVEL_ERROR, code, msg);
    }

    /**
     * 构造 WARN 级问题。
     *
     * @param field 字段
     * @param code  错误码
     * @param msg   描述
     * @return 问题
     */
    public static ValidationIssue warn(String field, int code, String msg) {
        return of(field, LEVEL_WARN, code, msg);
    }

    private static ValidationIssue of(String field, String level, int code, String msg) {
        ValidationIssue issue = new ValidationIssue();
        issue.setField(field);
        issue.setLevel(level);
        issue.setCode(code);
        issue.setMsg(msg);
        return issue;
    }

    /**
     * @return 是否为 ERROR 级（阻断级）
     */
    public boolean isError() {
        return LEVEL_ERROR.equals(level);
    }
}
