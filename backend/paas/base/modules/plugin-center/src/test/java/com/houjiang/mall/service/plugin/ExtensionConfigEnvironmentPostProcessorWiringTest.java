package com.houjiang.mall.service.plugin;

import org.junit.jupiter.api.Test;
import org.springframework.boot.Banner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.io.support.SpringFactoriesLoader;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.PropertySource;
import org.springframework.core.env.StandardEnvironment;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * {@link ExtensionConfigEnvironmentPostProcessor} 的「注册是否真的生效」独立验证（QA）。
 *
 * <p>区别于 {@link ExtensionConfigEnvironmentPostProcessorTest}（直接驱动实例），本类回答：
 * <ol>
 *   <li>Q1：{@code META-INF/spring.factories} 的注册是否真的能被 {@link SpringFactoriesLoader} 发现；</li>
 *   <li>Q2：真实 {@link SpringApplication} 启动是否回调装载器，键是否可解析、优先级是否最低；</li>
 *   <li>Q3：真实启动下，未被发现的 code 不产生任何属性源（零副作用）。</li>
 * </ol>
 * 全部使用 {@code WebApplicationType.NONE} 的最小上下文，无数据库、无 Web、无外部依赖。
 *
 * @author Amos
 */
class ExtensionConfigEnvironmentPostProcessorWiringTest {

    private static final String SOURCE_NAME = "houcloud.ext.demo";

    // ============ Q1：spring.factories 注册真的能被发现 ============

    @Test
    void springFactoriesRegistrationIsDiscoverable() {
        // ① 注册名断言：Spring Boot 启动期就是用 loadFactoryNames 发现 EnvironmentPostProcessor 的
        List<String> names = SpringFactoriesLoader.loadFactoryNames(EnvironmentPostProcessor.class, null);
        assertTrue(names.contains(ExtensionConfigEnvironmentPostProcessor.class.getName()),
                "spring.factories 必须注册 ExtensionConfigEnvironmentPostProcessor，实际=" + names);

        // ② 实例化断言：经 SpringFactoriesLoader 真实实例化出本实现（忽略 Boot 自带需特殊构造器的实现）
        List<EnvironmentPostProcessor> factories = SpringFactoriesLoader.forDefaultResourceLocation()
                .load(EnvironmentPostProcessor.class, (type, name, cause) -> {
                    // Boot 自带实现（如 CloudFoundryVcapEnvironmentPostProcessor）需要 DeferredLogFactory 构造器，
                    // 由 Boot 运行期特殊装配；此处只关心二开装载器可被发现并实例化。
                });
        assertTrue(factories.stream().anyMatch(f -> f instanceof ExtensionConfigEnvironmentPostProcessor),
                "SpringFactoriesLoader 必须能实例化 ExtensionConfigEnvironmentPostProcessor，实际="
                        + factories.stream().map(f -> f.getClass().getName()).toList());
    }

    // ============ Q2：真实 Spring Boot 启动回调 + 键可解析 + 最低优先级 ============

    @Test
    void realSpringBootBootInvokesLoaderAndKeysResolve() {
        try (ConfigurableApplicationContext context = boot()) {
            ConfigurableEnvironment environment = context.getEnvironment();

            assertTrue(environment.getPropertySources().contains(SOURCE_NAME),
                    "真实启动应产生属性源 " + SOURCE_NAME + "，实际=" 
                            + environment.getPropertySources().stream().map(PropertySource::getName).toList());
            assertEquals("0.88", environment.getProperty("houcloud.ext.demo.price.discount"),
                    "真实启动下二开 yml 的键必须可解析");
            assertEquals(Boolean.TRUE,
                    environment.getProperty("houcloud.ext.demo.feature.enabled", Boolean.class));
        }
    }

    @Test
    void realSpringBootCommandLineOverridesExtensionYml() {
        SpringApplication application = minimalApplication();
        try (ConfigurableApplicationContext context =
                     application.run("--houcloud.ext.demo.price.discount=0.5")) {
            assertEquals("0.5", context.getEnvironment().getProperty("houcloud.ext.demo.price.discount"),
                    "命令行参数必须能覆盖二开 yml（证明二开属性源处于最低优先级）");
        }
    }

    // ============ Q3：缺失/未发现 code 的零副作用（真实启动） ============

    @Test
    void realSpringBootMissingCodeHasZeroSideEffect() {
        try (ConfigurableApplicationContext context = boot()) {
            ConfigurableEnvironment environment = context.getEnvironment();

            assertNull(environment.getProperty("houcloud.ext.absent.price.discount"),
                    "未发现且无 yml 的 code 不得解析出任何键");
            assertFalse(environment.getPropertySources().contains("houcloud.ext.absent"),
                    "未发现且无 yml 的 code 不得产生属性源");

            long extensionSources = environment.getPropertySources().stream()
                    .filter(source -> source.getName().startsWith("houcloud.ext.")).count();
            assertEquals(1L, extensionSources,
                    "本 classpath 只声明 demo 一个二开，不得多出其它 houcloud.ext.* 属性源");
        }
    }

    // ============ helpers ============

    /** 断言装载器属性源位于 systemEnvironment 之后（最低优先级）。 */
    @Test
    void extensionPropertySourceSitsBelowSystemSources() {
        try (ConfigurableApplicationContext context = boot()) {
            List<String> order = context.getEnvironment().getPropertySources().stream()
                    .map(PropertySource::getName).toList();
            int own = order.indexOf(SOURCE_NAME);
            int systemEnvironment = order.indexOf(StandardEnvironment.SYSTEM_ENVIRONMENT_PROPERTY_SOURCE_NAME);
            assertTrue(own > systemEnvironment,
                    SOURCE_NAME + " 必须位于 systemEnvironment 之后（低优先级），实际顺序=" + order);
        }
    }

    private static ConfigurableApplicationContext boot() {
        return minimalApplication().run();
    }

    private static SpringApplication minimalApplication() {
        SpringApplication application = new SpringApplication(QaMinimalBootConfig.class);
        application.setWebApplicationType(WebApplicationType.NONE);
        application.setBannerMode(Banner.Mode.OFF);
        application.setLogStartupInfo(false);
        return application;
    }

    /** 最小启动源：无 Bean、无自动装配、无组件扫描，只为触发真实启动流程。 */
    @SpringBootConfiguration
    static class QaMinimalBootConfig {
    }
}
