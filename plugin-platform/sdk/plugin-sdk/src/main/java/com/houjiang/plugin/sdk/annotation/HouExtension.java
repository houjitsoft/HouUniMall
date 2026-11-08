package com.houjiang.plugin.sdk.annotation;

import com.houjiang.plugin.sdk.extension.ExtensionMode;

import java.lang.annotation.*;

/**
 * 标注一个类为扩展点实现。
 * 插件通过此注解声明自己实现了哪个框架扩展点。
 *
 * <p>示例：</p>
 * <pre>
 * {@code @HouExtension(point = OrderCreateExtension.class, priority = 10)}
 * public class CouponOrderExtension implements OrderCreateExtension {
 *     ...
 * }
 * </pre>
 *
 * @author HouCloud
 */
@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface HouExtension {

    /**
     * 所实现的扩展点接口
     *
     * @return 扩展点 Class
     */
    Class<?> point();

    /**
     * Stable source identifier used by build-time (classpath) extensions.
     * Runtime PF4J plugins get their source identifier from the plugin manifest,
     * so this value can remain empty for those plugins.
     */
    String code() default "";

    /**
     * How this implementation participates in the extension point. Build-time
     * customizations should use {@code REPLACE} only when they intentionally
     * take over the official implementation; hooks should remain {@code APPEND}.
     */
    ExtensionMode mode() default ExtensionMode.APPEND;

    /**
     * 执行优先级，数值越小越先执行。
     * 默认值 100，框架内置扩展点优先级为 0。
     *
     * @return 优先级数值
     */
    int priority() default 100;

    /**
     * 实现归属：{@code official}=官方实现，{@code custom}=构建期二开，{@code thirdparty}=商业插件。
     *
     * <p>默认 {@link #mode()} 为 {@link ExtensionMode#APPEND}。需要接管官方实现时，
     * 构建期二开显式声明 {@link ExtensionMode#REPLACE}；同一扩展点只能有一个 custom replacement。
     * 同一扩展点出现两个同级（同 {@link #priority()}）且同为 {@code custom} 的不同实现时，
     * 注册阶段直接失败并记录双方实现类与来源，不允许按加载顺序碰运气。
     *
     * @return 实现归属标识
     */
    String owner() default "official";

    /**
     * 扩展点描述
     *
     * @return 描述文本
     */
    String description() default "";
}
