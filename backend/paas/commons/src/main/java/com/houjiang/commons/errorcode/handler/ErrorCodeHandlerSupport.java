package com.houjiang.commons.errorcode.handler;

import com.houjiang.commons.errorcode.ErrorCodeDef;
import com.houjiang.commons.errorcode.ErrorMeta;

import java.util.List;
import java.util.Locale;

import java.util.List;
import java.util.Locale;

/**
 * 全局异常处理器接入支持：统一码渲染 + 业务文案脱敏出口。
 *
 * <p><b>v1.3.0 第二阶段重构后的定位</b>：全站 wire 上不再出现任何旧码。
 * {@code BizCodeEnume} / {@code RiderCodeEnume} / {@code GlobalExceptionEnum} 已不再是第二套码源，
 * 它们的每一个存活项的码值都<b>直接从统一码枚举取</b>（形如
 * {@code TOKEN_IS_INVALID(AuthErrorCode.TOKEN_INVALID.code(), ...)}）。
 * 因此运行时<b>不做任何旧码 → 新码翻译</b>，本类也不再提供旧码透传开关。
 *
 * <p>保留的两个能力：
 * <ol>
 *   <li>{@link #of(ErrorCodeDef)} —— 把统一码包成渲染结果，handler 直接取 {@code code()} / {@code message()}；</li>
 *   <li>{@link #safeMessage(String, ErrorCodeDef)} —— 业务文案脱敏出口，把 SQL / 类名 / 堆栈挡在屏外。
 *       这与「换码」是两件独立的事，业务异常自带码值<b>原样透传</b>，但文案必须过这一道。</li>
 * </ol>
 *
 * <p><b>为什么放在 commons 的 errorcode 包而不是独立模块</b>：错误码的唯一真源在 commons，
 * 治理者必须与真源同体；bff-web 通过「只依赖 errorcode 包 + 通配排除传递依赖」的方式接入。
 *
 * @author Amos
 * @since v1.2.0
 */
public final class ErrorCodeHandlerSupport {

    /**
     * 判断文案是否疑似夹带内部细节的特征词（小写比对）。
     *
     * <p>解决的问题：只靠人肉 review 保证「异常文案不出屏」不可靠，这里给出可执行的判定标准。
     * 命中即用统一码的中文文案替代，原文由调用方写进日志。
     */
    private static final List<String> INTERNAL_LEAK_MARKERS = List.of(
            "select ", "insert ", "update ", "delete from", " from ", " where ",
            "sql", "exception", "nullpointer", "caused by", "at com.", ".java:",
            "http://", "https://", "stacktrace", "classnotfound", "nested exception"
    );

    private ErrorCodeHandlerSupport() {
        // 工具类禁止实例化
    }

    /**
     * 直接按统一新码构造渲染结果。
     *
     * <p>v1.3.0 起这是唯一的构造入口：第二阶段重构后 wire 上只有统一码，
     * 不存在「旧码 + 目标新码」这种中间态。
     *
     * @param unified 统一新码
     * @return 渲染结果
     */
    public static ErrorRenderResult of(ErrorCodeDef unified) {
        return new ErrorRenderResult(unified);
    }

    /**
     * 业务文案脱敏出口：疑似夹带内部细节时改用统一码的中文文案。
     *
     * <p>解决的问题：业务异常（commons 的 BizCoreException / LoginToException / DefinedException、
     * bff-web 侧的 UserDefinedException）自带码值<b>原样透传</b>，
     * 但它们的 message 由业务方自由填写，历史上有把 SQL 片段、类名、堆栈原文塞进去的情况。
     * 本项目要开源，响应体里出现这些内部细节既难堪又有信息泄露风险。
     * 这里做最后一道出口检查：命中特征词即用统一码的 {@code userMessage} 兜底，
     * 调用方负责把原文写进日志（本类零依赖，不引入任何日志框架）。
     *
     * @param raw      业务异常自带的原始文案
     * @param fallback 命中泄漏特征时使用的统一错误码
     * @return 可以安全出屏的中文文案，绝不返回 null
     */
    public static String safeMessage(String raw, ErrorCodeDef fallback) {
        if (raw == null || raw.isBlank()) {
            return fallback.meta().userMessage();
        }
        String lower = raw.toLowerCase(Locale.ROOT);
        for (String marker : INTERNAL_LEAK_MARKERS) {
            if (lower.contains(marker)) {
                return fallback.meta().userMessage();
            }
        }
        return raw;
    }


    /**
     * 渲染结果：码值 + 用户文案 + 排障文案 + 元信息。
     *
     * <p>解决的问题：过去 handler 里 <code>ResponseResult.fail(5000, "服务端异常：" + simpleCause(e))</code>
     * 把异常根因拼进响应，浏览器能直接看到 SQL 与内部类名。现在 code/message/devMessage 分离，
     * devMessage 只进日志。
     */
    public static final class ErrorRenderResult {

        private final ErrorCodeDef def;

        private ErrorRenderResult(ErrorCodeDef def) {
            this.def = def;
        }

        /**
         * @return 最终响应码（统一新码，不再受任何开关影响）
         */
        public int code() {
            return def.code();
        }

        /**
         * @return 最终响应文案（统一码的中文 userMessage）
         */
        public String message() {
            return def.meta().userMessage();
        }

        /**
         * @return 排障文案（只进日志，不出屏）
         */
        public String devMessage() {
            return def.meta().devMessage();
        }

        /**
         * @return 元信息
         */
        public ErrorMeta meta() {
            return def.meta();
        }

        /**
         * @return 命中的统一新码定义
         */
        public ErrorCodeDef def() {
            return def;
        }
    }
}
