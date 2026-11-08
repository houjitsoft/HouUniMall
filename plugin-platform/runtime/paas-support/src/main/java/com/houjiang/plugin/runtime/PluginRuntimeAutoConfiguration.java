package com.houjiang.plugin.runtime;

import com.houjiang.plugin.api.host.PluginHostApi;
import com.houjiang.plugin.engine.PluginEngineAutoConfiguration;
import com.houjiang.plugin.engine.registry.ExtensionInvoker;
import com.houjiang.plugin.runtime.gateway.ExtensionGatewaySupport;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * PaaS 侧插件运行时装配（最终版 §二：{@code plugin-platform/runtime/paas-support}）。
 *
 * <p>职责边界：
 * <ul>
 *     <li>{@code plugin-platform/runtime/plugin-engine} 提供引擎<b>实现</b>（PF4J 封装、生命周期、注册表）；</li>
 *     <li>本模块负责把引擎<b>装配进 PaaS 进程</b>，并对外提供 {@link PluginCapabilityGateway}。</li>
 * </ul>
 *
 * <p>宿主（当前为 mall）改为 {@code @Import} 本配置即可，不必再直接引用引擎装配类，
 * 使「引擎实现」与「宿主装配」两处变更解耦；将来若需要独立进程运行插件，
 * 以本模块为装配入口，不必改动 mall。
 *
 * <p>注意：本配置会连带启用 PF4J（引擎的全部 Bean）。仅需要落盘安装包、
 * 不需要真实装载插件的模块，应直接依赖 {@code plugin-engine} 并使用
 * {@code PluginArtifactStore}，不要引入本模块。
 *
 * @author HouCloud
 */
@Configuration
@Import(PluginEngineAutoConfiguration.class)
public class PluginRuntimeAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public PluginCapabilityGateway pluginCapabilityGateway(PluginHostApi pluginHostApi) {
        return new PluginCapabilityGateway(pluginHostApi);
    }

    @Bean
    @ConditionalOnMissingBean
    public PromotionCalcGateway promotionCalcGateway(ExtensionInvoker invoker) {
        return new PromotionCalcGateway(invoker);
    }

    /**
     * 宿主统一扩展点调用骨架（R1 / §1.1）：所有领域网关（PriceCalcGateway /
     * OrderCreateBeforeGateway 等）复用，把「调用 + 超时 + 熔断 + 降级 + 可观测 +
     * 故障转译」收敛到一处。领域网关只需构造注入本 Bean。
     */
    @Bean
    @ConditionalOnMissingBean
    public ExtensionGatewaySupport extensionGatewaySupport(ExtensionInvoker invoker) {
        return new ExtensionGatewaySupport(invoker);
    }
}
