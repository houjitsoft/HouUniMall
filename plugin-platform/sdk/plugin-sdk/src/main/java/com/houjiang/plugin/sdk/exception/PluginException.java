package com.houjiang.plugin.sdk.exception;

/**
 * 插件异常基类。
 *
 * <p>插件内部可自由抛出；宿主 <code>ExtensionInvoker</code> 会捕获全部异常并记录到
 * <code>plugin.{code}</code> 日志 + 埋点，<strong>不向主流程传播</strong>。
 *
 * <p>唯一例外：<code>ON_INSTALL</code> 生命周期钩子抛出本异常会导致安装失败并进入回滚。
 *
 * @author HouCloud
 */
public class PluginException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /** 抛出异常的插件编码，由宿主填充 */
    private String pluginCode;

    public PluginException(String message) {
        super(message);
    }

    public PluginException(String message, Throwable cause) {
        super(message, cause);
    }

    public PluginException(String pluginCode, String message, Throwable cause) {
        super(message, cause);
        this.pluginCode = pluginCode;
    }

    public String getPluginCode() {
        return pluginCode;
    }

    public void setPluginCode(String pluginCode) {
        this.pluginCode = pluginCode;
    }
}
