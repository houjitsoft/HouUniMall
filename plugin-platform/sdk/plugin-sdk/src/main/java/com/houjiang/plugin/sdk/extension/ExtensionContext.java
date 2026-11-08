package com.houjiang.plugin.sdk.extension;

/**
 * 扩展点上下文基类。
 *
 * <p>所有业务上下文（如 <code>OrderCreateContext</code>）继承本类，统一提供「中断」能力：
 * <code>before*</code> 类扩展点调用 {@link #abort(String)} 后，宿主会中断后续处理并按业务异常抛出。
 *
 * <p>约定：
 * <ul>
 *   <li>入参通过子类的 <code>in</code> 属性只读暴露，扩展点不得改写</li>
 *   <li>结果通过子类的 <code>out</code> 属性写入，只允许 <code>after*</code> 类扩展点修改</li>
 *   <li>中断原因会被宿主原样带到业务异常中，应写面向运营的中文说明</li>
 * </ul>
 *
 * @author HouCloud
 */
public abstract class ExtensionContext {

    private String abortReason;

    /**
     * 中断后续处理。
     *
     * @param reason 中断原因，会展示给调用方
     */
    public void abort(String reason) {
        this.abortReason = reason;
    }

    /**
     * @return 是否已被某个扩展点中断
     */
    public boolean isAborted() {
        return abortReason != null;
    }

    /**
     * @return 中断原因，未中断时返回 {@code null}
     */
    public String getAbortReason() {
        return abortReason;
    }
}
