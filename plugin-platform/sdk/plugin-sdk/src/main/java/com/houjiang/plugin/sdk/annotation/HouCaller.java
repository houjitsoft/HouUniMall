package com.houjiang.plugin.sdk.annotation;

import java.lang.annotation.*;

/**
 * 声明插件接口允许的<b>调用方</b>，可标注在 Controller 类或方法上。
 *
 * <p>存在理由：{@link HouRoute#allowCallers()} 只能挂在 {@code @HouRoute} 方法上，
 * 而存量插件（GEO / OA / ERP 等）大量使用 Spring 原生 {@code @RequestMapping} 系列注解声明
 * Controller，这些路由无处声明调用方。本注解补上这块：类级声明对全类生效，方法级优先于类级，
 * 与 {@link HouRoute#allowCallers()} 同时出现时取并集。
 *
 * <p>语义与 {@link HouRoute#allowCallers()} 完全一致（保留代号 {@code host} / {@code *} /
 * {@code internal} / {@code anonymous} / 插件编码）：
 * <ul>
 *   <li>{@code host} —— 浏览器登录用户；</li>
 *   <li>{@code internal} —— 宿主内部可信链路（如 job 服务凭内部任务令牌调用）；</li>
 *   <li>{@code anonymous} —— 明确公开的非敏感只读接口；会跳过独立服务的产品访问拦截；</li>
 *   <li>{@code "geo"} 等插件编码 —— 仅允许该插件调用；</li>
 *   <li>{@code *} —— 任意插件（不含宿主，宿主必须显式写 {@code host}）。</li>
 * </ul>
 *
 * <p>示例（只放行内部定时任务，浏览器不可直接触发）：
 * <pre>{@code
 * @RestController
 * @RequestMapping("/geo/job/feign")
 * @HouCaller({"internal"})
 * public class GeoJobFeignController { ... }
 * }</pre>
 *
 * <p><b>调用方身份由服务端认定</b>：宿主的可信过滤器校验内部凭证后写入 request attribute，
 * 前端自带的 {@code X-Plugin-Caller} 之类请求头一律无效。
 *
 * @author HouCloud
 * @see HouRoute#allowCallers()
 */
@Documented
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface HouCaller {

    /**
     * 允许的调用方代号（大小写不敏感）。留空表示不通过本注解追加限制。
     */
    String[] value() default {};
}
