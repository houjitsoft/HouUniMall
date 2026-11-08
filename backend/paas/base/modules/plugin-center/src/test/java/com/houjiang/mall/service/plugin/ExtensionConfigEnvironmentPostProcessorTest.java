package com.houjiang.mall.service.plugin;

import org.junit.jupiter.api.Test;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.PropertySource;
import org.springframework.core.env.StandardEnvironment;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * {@link ExtensionConfigEnvironmentPostProcessor} 单测（二开五件套之「配置」）。
 *
 * <p>用<b>真实 classpath 夹具</b>（{@code src/test/resources/houcloud-ext-{demo,other}.yml}
 * 与 {@code META-INF/houcloud-extension.properties}）驱动，覆盖四条硬性行为：
 * <ol>
 *   <li>① 文件缺失 → 零副作用；</li>
 *   <li>② 文件存在 → 属性可见且落于 {@code houcloud.ext.{code}.*} 前缀；</li>
 *   <li>③ 多 code → 互不串扰；</li>
 *   <li>④ 不覆盖更高优先级属性；</li>
 *   <li>⑤ 幂等（重复装载不产生重复属性源），并端到端走 {@code postProcessEnvironment} 的真实发现。</li>
 * </ol>
 *
 * @author Amos
 */
class ExtensionConfigEnvironmentPostProcessorTest {

    private static final String DEMO = "demo";
    private static final String OTHER = "other";
    private static final String MISSING = "absent";

    private final ExtensionConfigEnvironmentPostProcessor processor =
            new ExtensionConfigEnvironmentPostProcessor();

    // ① 文件缺失 → 零副作用
    @Test
    void missingConfigFileHasZeroSideEffect() {
        StandardEnvironment environment = new StandardEnvironment();
        int before = environment.getPropertySources().size();

        processor.contribute(environment, Set.of(MISSING));

        assertEquals(before, environment.getPropertySources().size(), "缺失配置不得新增任何属性源");
        assertFalse(environment.getPropertySources().contains("houcloud.ext." + MISSING));
        assertNull(environment.getProperty("houcloud.ext." + MISSING + ".price.discount"));
    }

    // ② 文件存在 → 属性可见且前缀正确（且不泄漏到无前缀根键）
    @Test
    void presentConfigFileIsVisibleUnderPrefixedNamespace() {
        StandardEnvironment environment = new StandardEnvironment();

        processor.contribute(environment, Set.of(DEMO));

        assertTrue(environment.getPropertySources().contains("houcloud.ext." + DEMO), "应注册前缀属性源");
        assertEquals("0.88", environment.getProperty("houcloud.ext.demo.price.discount"));
        assertEquals(Boolean.TRUE, environment.getProperty("houcloud.ext.demo.feature.enabled", Boolean.class));
        assertNull(environment.getProperty("price.discount"), "二开配置不得污染无前缀的根键");
    }

    // ③ 多 code → 互不串扰
    @Test
    void multipleCodesDoNotCrossContaminate() {
        StandardEnvironment environment = new StandardEnvironment();

        processor.contribute(environment, Set.of(DEMO, OTHER));

        assertEquals("0.88", environment.getProperty("houcloud.ext.demo.price.discount"));
        assertEquals("0.5", environment.getProperty("houcloud.ext.other.price.discount"));
        assertNull(environment.getProperty("houcloud.ext.other.feature.enabled"), "other 未声明 feature.enabled");
        assertNull(environment.getProperty("houcloud.ext.demo.nope"));
    }

    // ④ 不覆盖更高优先级属性
    @Test
    void doesNotOverrideHigherPriorityProperty() {
        StandardEnvironment environment = new StandardEnvironment();
        environment.getPropertySources().addFirst(
                new MapPropertySource("highPriority", Map.of("houcloud.ext.demo.price.discount", "0.1")));

        processor.contribute(environment, Set.of(DEMO));

        assertEquals("0.1", environment.getProperty("houcloud.ext.demo.price.discount"), "更高优先级必须胜出");

        List<String> order = environment.getPropertySources().stream().map(PropertySource::getName).toList();
        int high = order.indexOf("highPriority");
        int own = order.indexOf("houcloud.ext." + DEMO);
        assertTrue(high >= 0 && own > high, "装载器属性源须位于更高优先级源之后（低优先级）: " + order);
    }

    // ⑤ 幂等 + postProcessEnvironment 端到端（走真实描述发现）
    @Test
    void isIdempotentAndWiredThroughPostProcessEnvironment() {
        StandardEnvironment environment = new StandardEnvironment();

        processor.postProcessEnvironment(environment, null);
        int sizeAfterFirst = environment.getPropertySources().size();
        assertEquals("0.88", environment.getProperty("houcloud.ext.demo.price.discount"),
                "postProcessEnvironment 应发现 code=demo 并装载其配置");

        processor.postProcessEnvironment(environment, null);

        assertEquals(sizeAfterFirst, environment.getPropertySources().size(), "重复装载必须幂等");
        assertEquals(1L, environment.getPropertySources().stream()
                .filter(source -> source.getName().equals("houcloud.ext." + DEMO)).count(),
                "同名属性源只应存在一份");
    }
}
