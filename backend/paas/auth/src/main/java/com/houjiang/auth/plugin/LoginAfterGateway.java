package com.houjiang.auth.plugin;

import com.houjiang.plugin.api.extension.auth.LoginAfterExtension;
import com.houjiang.plugin.runtime.PluginAbortException;
import com.houjiang.plugin.runtime.gateway.ExtensionGatewaySupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * 登录后扩展点的宿主薄网关（{@code after*} 语义）。
 *
 * <p><b>做什么</b>：登录成功、token 已落 Redis 之后触发，供二开做打标签 / 风控 / 同步。
 * 插件把要回传给前端的字段写进 {@code ctx.attrs}，宿主合并进登录响应，Core 无需知道有哪些插件。
 *
 * <p><b>为什么 {@code after*}</b>：本扩展点在登录成功后触发，属结果通知；不参与认证判定。
 * 插件若显式 {@code abort}，网关抛 {@link PluginAbortException}（7001），由调用方决定是否中断响应。
 *
 * <p>超时 / 熔断 / 降级 / 可观测由 {@link ExtensionGatewaySupport} 统一处理；
 * LoginAfter 属故障 IGNORE 类（插件故障不影响登录）。
 *
 * @author Amos
 */
public class LoginAfterGateway {

    private static final Logger log = LoggerFactory.getLogger(LoginAfterGateway.class);

    private static final String POINT = "LoginAfter";

    private final ExtensionGatewaySupport support;

    public LoginAfterGateway(ExtensionGatewaySupport support) {
        this.support = Objects.requireNonNull(support, "support");
    }

    /**
     * 登录成功后触发扩展点。
     *
     * @param userId  用户 ID，可为 {@code null}（旧链路可能拿不到数值 ID）
     * @param token   登录令牌
     * @param attrsIn 宿主预置的上下文属性，可为 {@code null}
     * @return 插件写入（并合并预置项）的属性；无插件时返回空 Map
     * @throws PluginAbortException 当插件显式中断时抛出
     */
    public Map<String, Object> afterLogin(Long userId, String token, Map<String, Object> attrsIn) {
        LoginAfterExtension.Context ctx = new LoginAfterExtension.Context();
        ctx.userId = userId;
        ctx.token = token;
        if (attrsIn != null && !attrsIn.isEmpty()) {
            ctx.attrs.putAll(attrsIn);
        }

        support.invoke(LoginAfterExtension.class, ctx);

        if (ctx.isAborted()) {
            log.warn("[ext] point={} result=abort userId={} reason={}",
                    POINT, userId, ctx.getAbortReason());
            throw new PluginAbortException(POINT, ctx.getAbortReason());
        }
        if (ctx.attrs == null) {
            return Map.of();
        }
        return new LinkedHashMap<>(ctx.attrs);
    }
}
