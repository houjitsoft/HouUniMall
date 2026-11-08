package com.houjiang.goods.plugin;

import com.houjiang.plugin.runtime.PluginRuntimeAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * goods 侧扩展点装配（R1.1 / §1.1）。
 *
 * <p>与既有 {@code com.houjiang.order.plugin.OrderPromotionConfiguration} 同构：集中暴露 goods 域
 * 全部扩展点网关 Bean。{@link PluginRuntimeAutoConfiguration} 的 {@code bootEnabled} 默认为 false，
 * 引入它<b>不会</b>启动 PF4J、不会扫描装载插件，只为拿到扩展点注册表、统一调用骨架与调用器。
 *
 * <p>goods 域网关：{@link PriceCalcGateway}（取价，after*）、{@link StockLockGateway}（库存预占，before*）、
 * {@link StockChangeGateway}（库存变动，after*）。
 *
 * @author Amos
 */
@Configuration
@Import(PluginRuntimeAutoConfiguration.class)
public class GoodsExtensionConfiguration {

    @Bean
    public PriceCalcGateway priceCalcGateway(
            com.houjiang.plugin.runtime.gateway.ExtensionGatewaySupport support) {
        return new PriceCalcGateway(support);
    }

    @Bean
    public StockLockGateway stockLockGateway(
            com.houjiang.plugin.runtime.gateway.ExtensionGatewaySupport support) {
        return new StockLockGateway(support);
    }

    @Bean
    public StockChangeGateway stockChangeGateway(
            com.houjiang.plugin.runtime.gateway.ExtensionGatewaySupport support) {
        return new StockChangeGateway(support);
    }
}
