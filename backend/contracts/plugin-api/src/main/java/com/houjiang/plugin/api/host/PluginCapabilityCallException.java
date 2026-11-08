package com.houjiang.plugin.api.host;

/**
 * 插件能力调用失败（standalone 插件不可达、返回错误、响应为空等）。
 *
 * <p>与「能力未发现」区分开：{@code PluginHostApi#capability} 找不到能力时返回
 * {@link java.util.Optional#empty()} 表示<b>降级</b>；而本异常表示<b>能力存在但调用失败</b>，
 * 此时 Core 不能再静默当作"没有该能力"——例如算价插件调不通却按原价成交，
 * 会让用户多付钱。调用方必须显式决定：重试、报错、还是走兜底。
 *
 * @author Amos
 */
public class PluginCapabilityCallException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public PluginCapabilityCallException(String message) {
        super(message);
    }

    public PluginCapabilityCallException(String message, Throwable cause) {
        super(message, cause);
    }
}
