package com.houjiang.auth.plugin;

import com.houjiang.commons.util.ResponseResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Supplier;

/**
 * 可选插件调用守卫：把「插件未安装/不可用」从异常降级为明确的失败应答。
 *
 * <p><b>背景（方案 §1.4.4、闭坑清单 P0「Core 失去独立运行」）</b>：
 * {@code auth} 通过 {@code @FeignClient("webpos")} / {@code @FeignClient("mall-rider")}
 * 硬编码调用商业插件服务。插件一旦做成「未安装即不存在」的收费插件，
 * 这些调用会直接抛异常（服务未注册 / 503），把「插件缺失」变成「Core 不可用」。
 *
 * <p>本守卫让调用方在插件缺失时返回明确的失败应答而不是崩溃，
 * 保证 P0 验收项「未装相关插件 Core 仍可登录/浏览/下单」成立——
 * 骑手/收银台登录只是旁路入口，主登录链路不受影响。
 *
 * <p><b>范围说明</b>：本类只做「调用失败不炸」这一层保护，不解决依赖方向。
 * 真正的「可选能力发现」（{@code PluginHostApi} 按 pluginCode 发现能力、缺失走降级）
 * 取决于方案 §10 第 9 条尚未确认的选型（可选能力发现 vs 收银/配送核心留 Core），
 * 该待办落定前不在主链路替方案拍板。
 *
 * @author Amos
 */
public final class OptionalPluginCaller {

    private static final Logger log = LoggerFactory.getLogger(OptionalPluginCaller.class);

    private OptionalPluginCaller() {
    }

    /**
     * 执行一个指向可选插件的远程调用；插件缺失或调用失败时降级为失败应答，绝不抛异常。
     *
     * @param pluginCode 插件编码，仅用于日志与提示，如 {@code webpos}、{@code delivery}
     * @param action     动作名，仅用于日志与提示，如 {@code webPosLogin}
     * @param remoteCall 真实远程调用（通常是 Feign 客户端方法）
     * @param <T>        应答数据类型
     * @return 远程调用结果；失败时为明确的失败应答
     */
    public static <T> ResponseResult<T> call(String pluginCode, String action,
                                             Supplier<ResponseResult<T>> remoteCall) {
        try {
            ResponseResult<T> result = remoteCall.get();
            if (result == null) {
                log.warn("可选插件调用返回空: plugin={}, action={}", pluginCode, action);
                return unavailable(pluginCode, action);
            }
            return result;
        } catch (Exception e) {
            // 插件未安装（服务未注册/503）或调用异常，一律降级为失败应答
            log.warn("可选插件调用失败，按未安装降级: plugin={}, action={}, reason={}",
                    pluginCode, action, e.getMessage());
            return unavailable(pluginCode, action);
        }
    }

    private static <T> ResponseResult<T> unavailable(String pluginCode, String action) {
        return ResponseResult.error("插件[" + pluginCode + "]未安装或不可用，无法执行" + action);
    }
}
