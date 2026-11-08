package com.houjiang.payment.plugin;

import com.houjiang.plugin.runtime.PluginRuntimeAutoConfiguration;
import com.houjiang.plugin.runtime.gateway.ExtensionGatewaySupport;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * payment 侧扩展点装配：集中暴露支付域三个薄网关（回调 / 促销落库 / 退款）。
 *
 * <p>与 {@code OrderPromotionConfiguration} 同构：{@link PluginRuntimeAutoConfiguration} 的
 * {@code bootEnabled} 默认为 false，引入它<b>不会</b>启动 PF4J、不会扫描装载插件，
 * 只为拿到扩展点注册表 / 调用器 / {@link ExtensionGatewaySupport}。
 *
 * <p>无任何二开（或 {@code -Pcustom-all} profile 关闭）时，注册表快照为空，
 * 各网关的 {@code invoke} 一次 volatile 读后零开销返回，官方链路行为不变。
 *
 * @author Amos
 */
@Configuration
@Import(PluginRuntimeAutoConfiguration.class)
public class PaymentExtensionConfiguration {

    @Bean
    public PayCallbackGateway payCallbackGateway(ExtensionGatewaySupport extensionGatewaySupport) {
        return new PayCallbackGateway(extensionGatewaySupport);
    }

    @Bean
    public PromotionPaySettleGateway promotionPaySettleGateway(ExtensionGatewaySupport extensionGatewaySupport) {
        return new PromotionPaySettleGateway(extensionGatewaySupport);
    }

    @Bean
    public RefundCreateGateway refundCreateGateway(ExtensionGatewaySupport extensionGatewaySupport) {
        return new RefundCreateGateway(extensionGatewaySupport);
    }
}
