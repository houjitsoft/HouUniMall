package com.houjiang.plugin.sdk.annotation;

import java.lang.annotation.*;

/**
 * 标注一个类为 HouCloud 插件描述符。
 * 插件开发者需在插件主类上使用此注解声明插件元信息。
 *
 * <p>示例：</p>
 * <pre>
 * {@code @HouPlugin(code = "mall-coupon", name = "优惠券插件", version = "1.0.0")}
 * public class CouponPluginDescriptor extends Plugin {
 *     ...
 * }
 * </pre>
 *
 * @author HouCloud
 */
@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface HouPlugin {

    /**
     * 插件唯一编码，全局不可重复。
     * 命名规范：{模块}-{功能}，如 mall-coupon、pay-wechat
     *
     * @return 插件编码
     */
    String code();

    /**
     * 插件显示名称
     *
     * @return 插件名称
     */
    String name();

    /**
     * 插件版本号，遵循语义化版本（SemVer）
     *
     * @return 版本号，如 1.0.0
     */
    String version();

    /**
     * 插件作者（邮箱或名称）
     *
     * @return 作者信息
     */
    String author() default "";

    /**
     * 插件描述
     *
     * @return 描述文本
     */
    String description() default "";

    /**
     * 插件分类
     *
     * @return 分类标签，如 营销工具、支付渠道、商品工具
     */
    String category() default "通用";

    /**
     * 最低框架兼容版本
     *
     * @return 框架版本号
     */
    String minFrameworkVersion() default "1.0.0";

    /**
     * 依赖的其他插件编码列表
     *
     * @return 依赖插件编码数组
     */
    String[] dependencies() default {};

    /**
     * 许可类型
     *
     * @return FREE / PAID / TRIAL
     */
    LicenseType license() default LicenseType.FREE;

    /**
     * 许可类型枚举
     */
    enum LicenseType {
        /** 免费 */
        FREE,
        /** 付费 */
        PAID,
        /** 试用 */
        TRIAL
    }
}
