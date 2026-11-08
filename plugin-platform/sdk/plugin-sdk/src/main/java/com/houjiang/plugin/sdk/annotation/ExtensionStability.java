package com.houjiang.plugin.sdk.annotation;

import java.lang.annotation.*;

/**
 * 扩展点契约的稳定性分级。标注在 <b>扩展点接口</b> 上（{@code plugin-api}），不是实现上。
 *
 * <p>目的：让二开与插件开发者在编译期就知道某个扩展点能不能放心依赖，
 * 也让官方在改契约时明确自己承担什么责任。
 *
 * <ul>
 *   <li>{@link Stability#STABLE}：宿主已真实接入并有契约测试守护；破坏性变更必须跨两个宿主版本缓冲，
 *       且必须在编译期暴露（改签名 / 删字段）。</li>
 *   <li>{@link Stability#EXPERIMENTAL}：契约已发布但宿主可能尚未接入，或语义仍可能调整。
 *       允许在当前开发周期内修改；改为破坏性变更时需在 {@link #note()} 写明迁移方式。</li>
 *   <li>{@link Stability#DEPRECATED}：已废弃，{@link #note()} 必须写明替代扩展点；
 *       至少保留两个宿主版本后移除。</li>
 * </ul>
 *
 * <p>默认 {@code EXPERIMENTAL}：新扩展点一律先以实验态发布，宿主真实接入 + 契约测试齐备后才升级为 STABLE。
 *
 * @author HouCloud
 */
@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface ExtensionStability {

    /**
     * @return 稳定性级别，默认 {@link Stability#EXPERIMENTAL}
     */
    Stability value() default Stability.EXPERIMENTAL;

    /**
     * @return 起始宿主版本，如 {@code 1.4}
     */
    String since() default "";

    /**
     * @return 变更/废弃说明，{@code DEPRECATED} 必填替代方案
     */
    String note() default "";

    /**
     * 稳定性级别。
     */
    enum Stability {
        /** 稳定契约：破坏性变更需跨版本缓冲 */
        STABLE,
        /** 实验契约：可调整，不保证跨版本兼容 */
        EXPERIMENTAL,
        /** 已废弃：至少保留两个宿主版本 */
        DEPRECATED
    }
}
