package com.houjiang.plugin.api.host;

/**
 * standalone 插件能力调用响应。
 *
 * <p>采用「{@code ok} 标志位 + 结果/错误」而非直接返回结构，是为了让宿主侧能区分
 * <b>调用成功但业务返回空</b>（{@code ok=true, result=null}）与<b>调用失败</b>
 * （{@code ok=false}）。两者对 Core 意义完全不同：前者是正常结果，后者需要走降级。
 *
 * @param ok     调用是否成功
 * @param result 返回值序列化结果；无返回值（void）时为 null
 * @param error  失败原因；成功时为 null
 * @author Amos
 */
public record CapabilityCallResponse(boolean ok, Object result, String error) {

    public static CapabilityCallResponse success(Object result) {
        return new CapabilityCallResponse(true, result, null);
    }

    public static CapabilityCallResponse failure(String error) {
        return new CapabilityCallResponse(false, null, error);
    }
}
