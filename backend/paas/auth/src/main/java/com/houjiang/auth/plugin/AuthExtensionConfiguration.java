package com.houjiang.auth.plugin;

import com.houjiang.plugin.runtime.PluginRuntimeAutoConfiguration;
import com.houjiang.plugin.runtime.gateway.ExtensionGatewaySupport;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * auth 侧扩展点装配：暴露登录后扩展点网关。
 *
 * <p>与 {@code OrderPromotionConfiguration} 同构。{@link PluginRuntimeAutoConfiguration}
 * 的 {@code bootEnabled} 默认为 false，引入它不会启动 PF4J、不会扫描装载插件，
 * 只为拿到 {@link ExtensionGatewaySupport}。无二开时快照为空，网关零开销返回。
 *
 * @author Amos
 */
@Configuration
@Import(PluginRuntimeAutoConfiguration.class)
public class AuthExtensionConfiguration {

    @Bean
    public LoginAfterGateway loginAfterGateway(ExtensionGatewaySupport extensionGatewaySupport) {
        return new LoginAfterGateway(extensionGatewaySupport);
    }
}
