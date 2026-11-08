package com.houjiang.plugin.artifact.exception;

/**
 * 下载授权被拒绝（未购买 / 授权过期 / 授权吊销 / 版本下架 / 版本未发布）。
 *
 * <p>错误码取 {@code PluginDistErrorCode}：3002 未购买、3003 授权过期、3004 授权吊销、
 * 3005 已下架、3006 版本未发布、3009 制品不存在。
 *
 * <p>上层（{@code PluginArtifactController}）统一转成
 * {@code ResponseResult.fail(code, message)}，<b>data 恒为 null</b>：
 * 未授权时不回传任何制品信息（连 md5 都不给），避免「没买也能拿到制品指纹」。
 *
 * @author Amos
 * @since M2
 */
public class DownloadDeniedException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /**
     * 插件分发错误码。
     */
    private final int code;

    /**
     * 构造拒绝异常。
     *
     * @param code    错误码
     * @param message 描述
     */
    public DownloadDeniedException(int code, String message) {
        super(message);
        this.code = code;
    }

    /**
     * @return 插件分发错误码
     */
    public int getCode() {
        return code;
    }
}
