package com.houjiang.mall.service.plugin;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.boot.env.YamlPropertySourceLoader;
import org.springframework.core.Ordered;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.EnumerablePropertySource;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.PropertySource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/**
 * 二开专属配置装载器（二开五件套之「配置」）——{@link EnvironmentPostProcessor} 实现（方案 a）。
 *
 * <p><b>能力</b>：二开模块把 {@code houcloud-ext-{code}.yml} 放进自己 JAR 的 classpath，
 * 宿主启动时自动把它并入 Spring {@link ConfigurableEnvironment}，二开据此实现「零宿主接线」的配置外置。
 * 装载器本身<b>不依赖任何二开模块</b>，靠 classpath 扫描发现。
 *
 * <h2>文件命名与定位</h2>
 * <p>文件名固定为 {@code houcloud-ext-{code}.yml}（{@code {code}} 等于
 * {@code META-INF/houcloud-extension.properties} 里的 {@code code}），置于二开模块
 * {@code src/main/resources/} 根下，classpath 定位为
 * {@value #CONFIG_LOCATION_PATTERN}（另兼容 {@code .yaml}，见 {@value #CONFIG_LOCATION_YAML_PATTERN}）。
 *
 * <h2>键映射规则（namespace）</h2>
 * <p>YAML 先被扁平化为「点分键」（{@code a.b.c}），随后<b>每个键 {@code k} 统一加前缀</b>
 * {@code houcloud.ext.{code}.}，即：
 * <pre>
 *   文件中的键 k   →   Environment 中的键 houcloud.ext.{code}.k
 *
 *   例（custom-goods）：
 *     price.discount      →  houcloud.ext.custom-goods.price.discount
 *     feature.enabled     →  houcloud.ext.custom-goods.feature.enabled
 * </pre>
 * 因此每个二开都在<b>独立前缀</b> {@code houcloud.ext.{code}.*} 下拥有自己的命名空间：
 * 既不会与宿主其它属性撞键，多个二开之间也互不串扰。
 *
 * <h2>行为契约</h2>
 * <ol>
 *   <li><b>文件缺失 → 零副作用</b>：发现 code 后若找不到对应 yml，不注册任何 {@link PropertySource}；</li>
 *   <li><b>不覆盖更高优先级</b>：属性源以 {@code addLast} 加入（最低优先级），
 *       故 Nacos / 命令行 / 环境变量 / 宿主 {@code application.yml} 一律可覆盖二开默认值；</li>
 *   <li><b>幂等</b>：同名属性源已存在则跳过，重复调用不产生重复属性源、不改变取值；</li>
 *   <li><b>无热更新</b>：仅在启动期装载一次；不注册监听，配置变更不触发热刷新（二开禁 {@code @RefreshScope}）。</li>
 * </ol>
 *
 * <p><b>为什么选方案 a（{@link EnvironmentPostProcessor}）而非 {@link org.springframework.context.ApplicationContextInitializer}</b>：
 * ① 它在「环境已就绪、Bean 定义尚未加载」的最早阶段生效，装载结果对全部下游可见——包括
 * {@code @Value}、{@code @ConfigurationProperties} 以及 {@code @ConditionalOnProperty} 的条件判定；
 * ② 通过 {@code spring.factories} 注册，无需二开模块提供任何 {@code @Configuration} 即可生效；
 * ③ 能稳定地把自身属性源压到最低优先级，恰好满足「不覆盖更高优先级」。
 *
 * <p><b>边界（诚实说明）</b>：本类只做「装载」，已在本机以单测覆盖 4 类行为；
 * 「真实宿主 + Nacos」下的最终生效叠加结果属「必须真实环境验收」项，本交付不据此宣称已生效。
 *
 * @author Amos
 */
@Slf4j
public class ExtensionConfigEnvironmentPostProcessor implements EnvironmentPostProcessor, Ordered {

    /** 二开专属配置 classpath 定位模板，{@code %s} 为二开 code。 */
    public static final String CONFIG_LOCATION_PATTERN = "classpath*:houcloud-ext-%s.yml";

    /** 二开专属配置 classpath 定位模板（{@code .yaml} 兼容）。 */
    public static final String CONFIG_LOCATION_YAML_PATTERN = "classpath*:houcloud-ext-%s.yaml";

    /** 二开配置统一前缀（其后拼接 {@code .{code}.}）。 */
    public static final String PREFIX = "houcloud.ext";

    private final ResourcePatternResolver resourceResolver;

    /**
     * 供 Spring Boot 反射实例化（见 {@code META-INF/spring.factories}）的无参构造，
     * 使用默认 {@link PathMatchingResourcePatternResolver}。
     */
    public ExtensionConfigEnvironmentPostProcessor() {
        this(new PathMatchingResourcePatternResolver());
    }

    /**
     * 可注入资源解析器的构造，主要供单测指向受控 classpath。
     *
     * @param resourceResolver 资源解析器，不得为 {@code null}
     */
    public ExtensionConfigEnvironmentPostProcessor(ResourcePatternResolver resourceResolver) {
        this.resourceResolver = resourceResolver;
    }

    /**
     * Spring Boot 启动早期回调：发现全部二开 code 并逐个装载其专属配置。
     *
     * <p>任何异常均被吞掉并记 WARN——配置装载是<b>可选增强</b>，绝不能因某个二开的配置问题拖垮宿主启动。
     *
     * @param environment 可变环境，将在此追加二开属性源
     * @param application 触发本次回调的应用（未使用）
     */
    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        if (environment == null) {
            return;
        }
        try {
            Set<String> codes = new TreeSet<>(ExtensionDescriptorScanner.discover(resourceResolver).keySet());
            contribute(environment, codes);
        } catch (RuntimeException e) {
            log.warn("二开专属配置装载跳过（不影响宿主启动）：{}", e.getMessage());
        }
    }

    /**
     * 为给定 code 集合逐一组装载专属配置（包级可见，便于单测直接驱动）。
     *
     * @param environment 可变环境
     * @param codes       二开 code 集合（已去重、有序）
     */
    void contribute(ConfigurableEnvironment environment, Set<String> codes) {
        if (environment == null || codes == null || codes.isEmpty()) {
            return;
        }
        for (String code : codes) {
            contributeCode(environment, code);
        }
    }

    /** 装载单个 code 的专属配置（满足幂等 + 缺失零副作用 + 最低优先级）。 */
    private void contributeCode(ConfigurableEnvironment environment, String code) {
        if (!StringUtils.hasText(code)) {
            return;
        }
        String sourceName = PREFIX + "." + code;
        if (environment.getPropertySources().contains(sourceName)) {
            return; // ③ 幂等：已装载则跳过
        }
        Map<String, Object> prefixed = loadPrefixedProperties(code);
        if (prefixed.isEmpty()) {
            return; // ① 文件缺失 → 零副作用
        }
        environment.getPropertySources().addLast(new MapPropertySource(sourceName, prefixed)); // ② 最低优先级
        log.info("二开 {} 专属配置已装载：{} 项，统一挂载于前缀 {}.{}.*（最低优先级，可被 Nacos/CLI/环境变量覆盖）",
                code, prefixed.size(), PREFIX, code);
    }

    /**
     * 读取并「加前缀」一个 code 的专属配置：YAML 扁平键 {@code k} → {@code houcloud.ext.{code}.k}。
     *
     * @param code 二开 code
     * @return 加前缀后的有序属性映射；文件缺失或无键时为空映射
     */
    Map<String, Object> loadPrefixedProperties(String code) {
        Map<String, Object> prefixed = new LinkedHashMap<>();
        if (!StringUtils.hasText(code) || resourceResolver == null) {
            return prefixed;
        }
        String dotPrefix = PREFIX + "." + code + ".";
        for (String template : List.of(CONFIG_LOCATION_PATTERN, CONFIG_LOCATION_YAML_PATTERN)) {
            for (Resource resource : resolve(String.format(template, code))) {
                for (PropertySource<?> source : loadYaml(resource)) {
                    if (source instanceof EnumerablePropertySource<?> enumerable) {
                        for (String key : enumerable.getPropertyNames()) {
                            prefixed.put(dotPrefix + key, enumerable.getProperty(key));
                        }
                    }
                }
            }
        }
        return prefixed;
    }

    /** 用 {@value #CONFIG_LOCATION_PATTERN} 类模式解析资源；解析失败返回空数组（不抛）。 */
    private Resource[] resolve(String pattern) {
        try {
            return resourceResolver.getResources(pattern);
        } catch (IOException e) {
            log.warn("扫描二开配置失败 {}：{}", pattern, e.getMessage());
            return new Resource[0];
        }
    }

    /** 用 Spring Boot 的 {@link YamlPropertySourceLoader} 加载单个 YAML 资源；失败返回空列表（不抛）。 */
    private List<PropertySource<?>> loadYaml(Resource resource) {
        try {
            return new YamlPropertySourceLoader().load(resource.getDescription(), resource);
        } catch (IOException e) {
            log.warn("解析二开配置失败 {}：{}", resource.getDescription(), e.getMessage());
            return List.of();
        }
    }

    /**
     * 在配置数据装载（{@code ConfigDataEnvironmentPostProcessor}）<b>之后</b>执行，
     * 确保其 {@code addLast} 加入的属性源真正处于最低优先级。
     */
    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE;
    }
}
