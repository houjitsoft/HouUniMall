package com.houjiang.plugin.update.precheck;

/**
 * 兼容性预检结果（P0-9，方案 §4.4 {@code /precheck}）。
 *
 * <p>{@code detail} 是<b>直接给终端用户看</b>的文案，按钮禁用提示原样展示它，
 * 因此必须写成「当前框架版本 1.8 低于插件要求 2.0」这种带具体数值的完整句子，
 * 而不是 {@code INCOMPATIBLE_FRAMEWORK} 这类需要二次翻译的枚举名。
 *
 * @author Amos
 * @since M3
 */
public final class PrecheckResult {

    /**
     * 是否通过。
     */
    private final boolean pass;

    /**
     * 不通过时的错误码（3014 框架版本不足 / 3015 依赖缺失 / 3009 版本不存在）。
     */
    private final Integer reason;

    /**
     * 不通过时的具体原因（可直接展示）。
     */
    private final String detail;

    private PrecheckResult(boolean pass, Integer reason, String detail) {
        this.pass = pass;
        this.reason = reason;
        this.detail = detail;
    }

    /**
     * 构造通过结果。
     *
     * @return 通过结果
     */
    public static PrecheckResult pass() {
        return new PrecheckResult(true, null, null);
    }

    /**
     * 构造拒绝结果。
     *
     * @param reason 错误码
     * @param detail 具体原因文案
     * @return 拒绝结果
     */
    public static PrecheckResult fail(int reason, String detail) {
        return new PrecheckResult(false, reason, detail);
    }

    /**
     * @return 是否通过
     */
    public boolean isPass() {
        return pass;
    }

    /**
     * @return 错误码，通过时为 null
     */
    public Integer getReason() {
        return reason;
    }

    /**
     * @return 具体原因，通过时为 null
     */
    public String getDetail() {
        return detail;
    }
}
