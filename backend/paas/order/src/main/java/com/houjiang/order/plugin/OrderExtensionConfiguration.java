package com.houjiang.order.plugin;

import com.houjiang.plugin.runtime.PluginRuntimeAutoConfiguration;
import com.houjiang.plugin.runtime.gateway.ExtensionGatewaySupport;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * order 侧扩展点装配（R1.2 / §1.1）。
 *
 * <p>与既有 {@link OrderPromotionConfiguration} 同构：集中暴露 order 域全部扩展点网关 Bean。
 * {@link PluginRuntimeAutoConfiguration} 的 {@code bootEnabled} 默认为 false，引入它<b>不会</b>
 * 启动 PF4J、不会扫描装载插件，只为拿到扩展点注册表、统一调用骨架与调用器。
 *
 * <p>order 域网关：
 * <ul>
 *     <li>{@link CartCalcGateway}（购物车算价，after*）</li>
 *     <li>{@link CartValidateGateway}（购物车校验，before*）</li>
 *     <li>{@link FreightCalcGateway}（运费计算，after*）</li>
 *     <li>{@link OrderCreateBeforeGateway}（下单前拦截，before*）</li>
 *     <li>{@link OrderCreateAfterGateway}（下单后通知，after*）</li>
 *     <li>{@link AfterSaleCreateGateway}（售后创建前校验，before*）</li>
 * </ul>
 *
 * @author Amos
 */
@Configuration
@Import(PluginRuntimeAutoConfiguration.class)
public class OrderExtensionConfiguration {

    @Bean
    public CartCalcGateway cartCalcGateway(ExtensionGatewaySupport support) {
        return new CartCalcGateway(support);
    }

    @Bean
    public CartValidateGateway cartValidateGateway(ExtensionGatewaySupport support) {
        return new CartValidateGateway(support);
    }

    @Bean
    public FreightCalcGateway freightCalcGateway(ExtensionGatewaySupport support) {
        return new FreightCalcGateway(support);
    }

    @Bean
    public OrderCreateBeforeGateway orderCreateBeforeGateway(ExtensionGatewaySupport support) {
        return new OrderCreateBeforeGateway(support);
    }

    @Bean
    public OrderCreateAfterGateway orderCreateAfterGateway(ExtensionGatewaySupport support) {
        return new OrderCreateAfterGateway(support);
    }

    @Bean
    public AfterSaleCreateGateway afterSaleCreateGateway(ExtensionGatewaySupport support) {
        return new AfterSaleCreateGateway(support);
    }
}
