package com.houjiang.commons.errorcode;

import java.util.Arrays;
import java.util.Objects;

/**
 * 错误码元信息（不可变）。
 *
 * <p>解决的问题：旧枚举一项只有「数字 + 一句中文」，没有级别、没有归属模块、没有排查指引、
 * 没有废弃标记，客服拿到用户截图里的数字码无法反查语义（PRD 痛点 5）。
 *
 * <p>本类把 PRD §5 的全部字段固化成不可变对象，由 {@link Builder} 构造：
 * <ul>
 *   <li>必填：{@code code}、{@code userMessage}（Builder 会强校验，空值直接抛异常）。</li>
 *   <li>有默认值：{@code level=ERROR}、{@code category=SYSTEM}、{@code uiAction=TOAST}、
 *       {@code status=ACTIVE}、{@code httpSemantic=INTERNAL_500}、{@code exposeToUser=true}。</li>
 *   <li>选填：{@code devMessage}、{@code troubleshooting}、{@code legacyCodes}、{@code since}、{@code remark}。</li>
 * </ul>
 *
 * <p><b>禁止</b>把 {@code devMessage} / 异常根因 / SQL / 堆栈 / 内部 URL / token 放进 {@code userMessage}
 * —— 自检 V11 会拦截敏感词，V12 会校验 devMessage 长度与隐私。
 *
 * @author Amos
 * @since v1.2.0
 */
public final class ErrorMeta {

    /** userMessage 最大长度（汉字数，PRD §5.1）。 */
    public static final int MAX_USER_MESSAGE_LENGTH = 30;

    /** devMessage 最大长度（PRD §5.1）。 */
    public static final int MAX_DEV_MESSAGE_LENGTH = 200;

    /** troubleshooting 最大长度（PRD §5.2）。 */
    public static final int MAX_TROUBLESHOOTING_LENGTH = 300;

    private final int code;
    private final String userMessage;
    private final String devMessage;
    private final ErrorLevel level;
    private final boolean retryable;
    private final UiAction uiAction;
    private final ErrorCategory category;
    private final ErrorCodeStatus status;
    private final HttpSemantic httpSemantic;
    private final boolean exposeToUser;
    private final String troubleshooting;
    private final int[] legacyCodes;
    private final String since;
    private final String remark;

    private ErrorMeta(Builder builder) {
        this.code = builder.code;
        this.userMessage = builder.userMessage;
        this.devMessage = builder.devMessage == null ? "" : builder.devMessage;
        this.level = builder.level;
        this.retryable = builder.retryable;
        this.uiAction = builder.uiAction;
        this.category = builder.category;
        this.status = builder.status;
        this.httpSemantic = builder.httpSemantic;
        this.exposeToUser = builder.exposeToUser;
        this.troubleshooting = builder.troubleshooting == null ? "" : builder.troubleshooting;
        this.legacyCodes = builder.legacyCodes == null ? new int[0] : Arrays.copyOf(builder.legacyCodes, builder.legacyCodes.length);
        this.since = builder.since == null ? "" : builder.since;
        this.remark = builder.remark == null ? "" : builder.remark;
    }

    /**
     * 创建元信息构造器。
     *
     * @param code        错误码值，必须为正数
     * @param userMessage 面向用户的中文文案，非空、≤30 汉字
     * @return 构造器
     * @throws IllegalArgumentException 码值非正或用户文案为空时抛出
     */
    public static Builder builder(int code, String userMessage) {
        if (code <= 0) {
            throw new IllegalArgumentException("错误码必须为正整数：code=" + code);
        }
        if (userMessage == null || userMessage.trim().isEmpty()) {
            throw new IllegalArgumentException("userMessage 为必填项，不能为空：code=" + code);
        }
        return new Builder(code, userMessage);
    }

    /**
     * @return 错误码值
     */
    public int code() {
        return code;
    }

    /**
     * @return 用户可见文案
     */
    public String userMessage() {
        return userMessage;
    }

    /**
     * @return 开发/排障文案（只进日志，不出屏）
     */
    public String devMessage() {
        return devMessage;
    }

    /**
     * @return 错误级别
     */
    public ErrorLevel level() {
        return level;
    }

    /**
     * @return 是否可安全重试
     */
    public boolean retryable() {
        return retryable;
    }

    /**
     * @return 前端默认展示动作
     */
    public UiAction uiAction() {
        return uiAction;
    }

    /**
     * @return 错误类别（前端兜底依据）
     */
    public ErrorCategory category() {
        return category;
    }

    /**
     * @return 生命周期状态
     */
    public ErrorCodeStatus status() {
        return status;
    }

    /**
     * @return HTTP 语义标注（仅元数据）
     */
    public HttpSemantic httpSemantic() {
        return httpSemantic;
    }

    /**
     * @return 是否允许前端直出 userMessage
     */
    public boolean exposeToUser() {
        return exposeToUser;
    }

    /**
     * @return 排查指引
     */
    public String troubleshooting() {
        return troubleshooting;
    }

    /**
     * @return 旧码映射列表（副本）
     */
    public int[] legacyCodes() {
        return Arrays.copyOf(legacyCodes, legacyCodes.length);
    }

    /**
     * @return 引入版本
     */
    public String since() {
        return since;
    }

    /**
     * @return 备注
     */
    public String remark() {
        return remark;
    }

    /**
     * {@link ErrorMeta} 的构造器。
     *
     * <p>解决的问题：元信息字段多且多数可选，用 13 参构造方法极易传错位；
     * Builder 让「必填强校验 + 选填有默认」在一个地方收口。
     */
    public static final class Builder {

        private final int code;
        private final String userMessage;
        private String devMessage;
        private ErrorLevel level = ErrorLevel.ERROR;
        private boolean retryable = false;
        private UiAction uiAction = UiAction.TOAST;
        private ErrorCategory category = ErrorCategory.SYSTEM;
        private ErrorCodeStatus status = ErrorCodeStatus.ACTIVE;
        private HttpSemantic httpSemantic = HttpSemantic.INTERNAL_500;
        private boolean exposeToUser = true;
        private String troubleshooting;
        private int[] legacyCodes;
        private String since;
        private String remark;

        private Builder(int code, String userMessage) {
            this.code = code;
            this.userMessage = userMessage;
        }

        /**
         * @param devMessage 面向开发/排障的文案，可含技术细节，不得含用户隐私与凭据
         * @return this
         */
        public Builder devMessage(String devMessage) {
            this.devMessage = devMessage;
            return this;
        }

        /**
         * @param level 错误级别
         * @return this
         */
        public Builder level(ErrorLevel level) {
            this.level = level;
            return this;
        }

        /**
         * @param retryable 相同请求是否可安全重试
         * @return this
         */
        public Builder retryable(boolean retryable) {
            this.retryable = retryable;
            return this;
        }

        /**
         * @param uiAction 前端默认展示动作
         * @return this
         */
        public Builder uiAction(UiAction uiAction) {
            this.uiAction = uiAction;
            return this;
        }

        /**
         * @param category 错误类别
         * @return this
         */
        public Builder category(ErrorCategory category) {
            this.category = category;
            return this;
        }

        /**
         * @param status 生命周期状态
         * @return this
         */
        public Builder status(ErrorCodeStatus status) {
            this.status = status;
            return this;
        }

        /**
         * @param httpSemantic HTTP 语义标注
         * @return this
         */
        public Builder httpSemantic(HttpSemantic httpSemantic) {
            this.httpSemantic = httpSemantic;
            return this;
        }

        /**
         * @param exposeToUser 是否允许前端直出 userMessage（支付/风控/鉴权类建议 false）
         * @return this
         */
        public Builder exposeToUser(boolean exposeToUser) {
            this.exposeToUser = exposeToUser;
            return this;
        }

        /**
         * @param troubleshooting 排查指引，FATAL 级别必填
         * @return this
         */
        public Builder troubleshooting(String troubleshooting) {
            this.troubleshooting = troubleshooting;
            return this;
        }

        /**
         * 登记旧码 → 新码映射（允许一对多）。
         *
         * <p>解决的问题：519 项旧枚举既有重复码又有跨枚举冲突，无法自动 1:1 映射。
         * 这里不另建映射表，而是由新码反向声明，运行时由 {@link LegacyCodeIndex} 反向构建索引 ——
         * 单一真源，杜绝双向漂移。
         *
         * <p><b>不要</b>登记成功语义旧码（0/1/200/11000/11001/10004/10006 等），自检 V10 会拦截。
         *
         * @param legacyCodes 旧码列表
         * @return this
         */
        public Builder legacyCodes(int... legacyCodes) {
            this.legacyCodes = legacyCodes == null ? new int[0] : Arrays.copyOf(legacyCodes, legacyCodes.length);
            return this;
        }

        /**
         * @param since 引入版本，如 v1.2.0
         * @return this
         */
        public Builder since(String since) {
            this.since = since;
            return this;
        }

        /**
         * @param remark 备注、常见误用提醒
         * @return this
         */
        public Builder remark(String remark) {
            this.remark = remark;
            return this;
        }

        /**
         * 构造不可变的 {@link ErrorMeta}。
         *
         * @return 元信息
         * @throws IllegalArgumentException 必填项缺失或明显非法时抛出
         */
        public ErrorMeta build() {
            Objects.requireNonNull(level, "level 不能为 null");
            Objects.requireNonNull(uiAction, "uiAction 不能为 null");
            Objects.requireNonNull(category, "category 不能为 null");
            Objects.requireNonNull(status, "status 不能为 null");
            Objects.requireNonNull(httpSemantic, "httpSemantic 不能为 null");
            if (devMessage != null && devMessage.length() > MAX_DEV_MESSAGE_LENGTH) {
                throw new IllegalArgumentException(
                        "devMessage 超过 " + MAX_DEV_MESSAGE_LENGTH + " 字：code=" + code + "，实际=" + devMessage.length());
            }
            if (troubleshooting != null && troubleshooting.length() > MAX_TROUBLESHOOTING_LENGTH) {
                throw new IllegalArgumentException(
                        "troubleshooting 超过 " + MAX_TROUBLESHOOTING_LENGTH + " 字：code=" + code
                                + "，实际=" + troubleshooting.length());
            }
            return new ErrorMeta(this);
        }
    }
}
