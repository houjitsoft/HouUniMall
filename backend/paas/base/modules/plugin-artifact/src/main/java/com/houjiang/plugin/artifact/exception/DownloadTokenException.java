package com.houjiang.plugin.artifact.exception;

/**
 * 下载令牌异常：令牌无效 / 已过期 / 越权 / 超过限次。
 *
 * <p>错误码取 {@code PluginDistErrorCode}：
 * <ul>
 *   <li>{@code 3007} 令牌无效或已过期（含签名不匹配、留痕不一致、命中次数用尽）；</li>
 *   <li>{@code 3008} 令牌与租户（instanceId）不匹配，疑似链接被转发到别的站点。</li>
 * </ul>
 *
 * <p><b>为什么「签名不匹配」也报 3007 而不是单独的错误码</b>：方案 §4.5 只给了 3007/3008 两个码，
 * 再细分需要扩码（会动到 3001-3018 的编号体系）；更重要的是<b>不向调用方泄露失败原因</b>——
 * 「签名错」和「已过期」对攻击者都是「换个链接再试」，合并返回反而更省心也更安全。
 *
 * @author Amos
 * @since M2
 */
public class DownloadTokenException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /**
     * 插件分发错误码（3007 / 3008）。
     */
    private final int code;

    /**
     * 构造下载令牌异常。
     *
     * @param code    错误码
     * @param message 描述
     */
    public DownloadTokenException(int code, String message) {
        super(message);
        this.code = code;
    }

    /**
     * 构造下载令牌异常（带原因）。
     *
     * @param code    错误码
     * @param message 描述
     * @param cause   根因
     */
    public DownloadTokenException(int code, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
    }

    /**
     * @return 插件分发错误码
     */
    public int getCode() {
        return code;
    }
}
