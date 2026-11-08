package com.houjiang.order.plugin;

import com.houjiang.order.feign.ActivityServiceClient;
import com.houjiang.plugin.api.capability.coupon.CouponQueryCapability;
import com.houjiang.plugin.api.extension.order.PromotionCalcExtension;
import com.houjiang.plugin.engine.registry.ExtensionRegistry;
import com.houjiang.plugin.runtime.PluginRuntimeAutoConfiguration;
import com.houjiang.plugin.runtime.PromotionCalcGateway;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * order 侧促销算价装配：把遗留 activity 调用包装成扩展点实现，注册进扩展点注册表，
 * 使 {@link PromotionCalcGateway} 在本进程可用。
 *
 * <p><b>为什么 order 也能用扩展点</b>：插件引擎的 {@code bootEnabled} 默认为 false，
 * 引入 {@link PluginRuntimeAutoConfiguration} <b>不会</b>启动 PF4J、不会扫描装载插件，
 * 只为拿到扩展点注册表与 {@code ExtensionInvoker}（其 {@code enabled} 默认 true）。
 * 因此本进程只装载下面这一个遗留适配器，行为完全可控。
 *
 * <p><b>迁移期临时装配</b>：秒杀 / 限时折扣等活动各自插件化后，会由插件自行注册
 * {@link PromotionCalcExtension} 实现（可 standalone 部署，经
 * {@code PluginHostApi} 的远程通道调用），届时删除本配置与
 * {@link LegacyActivityPromotionCalcExtension}。
 *
 * @author Amos
 */
@Configuration
@Import(PluginRuntimeAutoConfiguration.class)
public class OrderPromotionConfiguration {

    /** 遗留适配器在注册表中的插件码与优先级。 */
    public static final String LEGACY_PLUGIN_CODE = "activity-legacy";
    public static final int LEGACY_PRIORITY = 0;

    @Bean
    public LegacyActivityPromotionCalcExtension legacyActivityPromotionCalcExtension(
            ExtensionRegistry extensionRegistry,
            ActivityServiceClient activityServiceClient) {
        LegacyActivityPromotionCalcExtension extension =
                new LegacyActivityPromotionCalcExtension(activityServiceClient);
        extensionRegistry.register(PromotionCalcExtension.class, extension,
                LEGACY_PLUGIN_CODE, LEGACY_PRIORITY, "official");
        return extension;
    }

    @Bean
    public PromotionCalcGateway promotionCalcGateway(
            com.houjiang.plugin.engine.registry.ExtensionInvoker extensionInvoker) {
        return new PromotionCalcGateway(extensionInvoker);
    }

    /**
     * 券查询能力的遗留实现。
     *
     * <p>券状态校验不是算价，故不走扩展点注册表，而是作为普通能力 Bean 注入；
     * 券活动插件化后改为经 {@code PluginHostApi} 发现（standalone 插件走远程通道）。
     */
    @Bean
    public CouponQueryCapability couponQueryCapability(ActivityServiceClient activityServiceClient) {
        return new LegacyActivityCouponQueryCapability(activityServiceClient);
    }
}
