package com.houjiang.plugin.sdk.annotation;

import java.lang.annotation.*;

/**
 * 声明插件自有接口的路由。
 *
 * <p>插件<strong>不注册自己的 Controller</strong>：所有插件接口统一由宿主的
 * <code>PluginDispatcher</code>（<code>/plugin/{code}/{path}</code>）分发。这样插件永远无法覆盖宿主路径，
 * 且鉴权、限流、审计可在分发端点单点插桩。
 *
 * <p>示例：
 * <pre>{@code
 * @HouRoute(path = "/list", method = "POST", permission = "mall:coupon:list")
 * public ResponseResult<?> list(Map<String, Object> params) { ... }
 * }</pre>
 *
 * @author HouCloud
 */
@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface HouRoute {

    /**
     * 路径，相对 <code>/plugin/{code}</code>，需以 {@code /} 开头
     */
    String path();

    /**
     * HTTP 方法，字符串形式（避免 SDK 依赖 spring-web），默认 {@code POST}
     */
    String method() default "POST";

    /**
     * 权限标识。
     *
     * <p>为空表示仅校验登录态——这是历史兼容策略，属于提权面（任何登录用户都能调用）。
     * 宿主开启 {@code houcloud.plugin.require-declared-route-permission=true} 后，
     * 未声明权限的路由会被直接拒绝（403），因此插件应显式声明权限。
     */
    String permission() default "";

    /**
     * 接口描述
     */
    String description() default "";

    /**
     * 允许的<b>插件调用方</b>白名单（OSGi {@code x-friends} 风格）。
     *
     * <p>插件接口默认对「宿主」（浏览器登录用户）开放，但<b>不默认对其他插件开放</b>：
     * 跨插件调用必须在这里显式点名，否则分发器按策略拒绝。这把「谁能调我」从口头约定
     * 变成可审查的声明，也让依赖拓扑可以从注解直接读出来。
     *
     * <p>取值语义：
     * <ul>
     *   <li>{@code {}}（默认）—— 不做调用方限制，完全沿用历史行为；</li>
     *   <li>{@code {"erp"}} —— 仅允许 erp 插件调用；</li>
     *   <li>{@code {"host"}} —— 仅允许宿主侧（浏览器登录用户）调用，拒绝任何插件间调用；</li>
     *   <li>{@code {"internal"}} —— 仅允许宿主内部可信链路（job 服务等凭内部任务令牌的调用）；</li>
     *   <li>{@code {"erp", "host"}} —— 两者都允许；</li>
     *   <li>{@code {"*"}} —— 允许任何插件（仍拒绝宿主，除非同时写 host）。</li>
     * </ul>
     *
     * <p>用 Spring {@code @RequestMapping} 声明的 Controller 路由没有本注解可挂，
     * 请改用类级 / 方法级 {@link HouCaller}；两者同时出现时取并集。
     *
     * <p><b>调用方身份由服务端认定，不看请求头</b>：前端自己带 {@code X-Plugin-Caller} 无效，
     * 只有宿主可信过滤器校验内部凭证后写入 request attribute 的身份才被采信。
     * 否则任何登录用户都能伪造来源，白名单形同虚设。
     *
     * <p>未声明本属性时的兜底策略由宿主的
     * {@code houcloud.plugin.enforce-caller-whitelist} 决定：
     * {@code false}（默认）= 不限制（历史兼容）；{@code true} = 未声明即拒绝插件间调用。
     */
    String[] allowCallers() default {};
}
