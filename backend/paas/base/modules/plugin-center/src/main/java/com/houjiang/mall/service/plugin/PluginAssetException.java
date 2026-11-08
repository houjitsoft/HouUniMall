package com.houjiang.mall.service.plugin;

/**
 * 插件前端产物请求异常（T11）。
 *
 * <p>带 HTTP 状态码语义，便于 Controller 直接映射：
 * <ul>
 *   <li>400：请求非法（路径穿越、后缀未白名单、编码异常、code/version 格式非法）</li>
 *   <li>500：服务端读取产物失败</li>
 * </ul>
 *
 * @author Amos
 */
public class PluginAssetException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    private final int status;

    public PluginAssetException(String message) {
        this(message, 400);
    }

    public PluginAssetException(String message, int status) {
        super(message);
        this.status = status;
    }

    public int getStatus() {
        return status;
    }
}
