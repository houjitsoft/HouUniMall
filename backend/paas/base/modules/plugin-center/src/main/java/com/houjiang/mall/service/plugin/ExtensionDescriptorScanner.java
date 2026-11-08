package com.houjiang.mall.service.plugin;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * 二开交付描述 {@code META-INF/houcloud-extension.properties} 的 classpath 发现器。
 *
 * <p><b>唯一事实来源（single source of truth）</b>：二开能力有两个消费者需要「枚举 classpath 上全部二开 code」——
 * <ol>
 *   <li>{@link ExtensionSchemaMigrationRunner}：按 code 扫描 {@code db/migration/{code}/*.sql} 做 schema 迁移；</li>
 *   <li>{@link ExtensionConfigEnvironmentPostProcessor}：按 code 装载 {@code houcloud-ext-{code}.yml} 专属配置。</li>
 * </ol>
 * 二者若各写一套发现口径必然随时间漂移，故统一抽取到本类；{@link #DESCRIPTOR_PATTERN} 亦在此定义并被复用。
 *
 * <p>发现口径与既有实现完全一致（不改变行为）：以 {@code classpath*:META-INF/houcloud-extension.properties}
 * 匹配全部资源，逐份解析，取 {@code code}（去空白，非空才计入）与 {@code version}（缺省 {@code unknown}）。
 * 多份同名资源由 classpath 顺序决定后写覆盖前写（与既有 {@code LinkedHashMap} 语义一致）。
 *
 * @author Amos
 */
@Slf4j
public final class ExtensionDescriptorScanner {

    /** 二开交付描述 classpath 定位（{@code classpath*} 可命中全部二开模块 JAR）。 */
    public static final String DESCRIPTOR_PATTERN = "classpath*:META-INF/houcloud-extension.properties";

    /** 描述文件中「二开 code」键名。 */
    public static final String KEY_CODE = "code";

    /** 描述文件中「二开版本」键名。 */
    public static final String KEY_VERSION = "version";

    /** 版本缺省值（描述文件未声明 version 时）。 */
    public static final String VERSION_UNKNOWN = "unknown";

    private ExtensionDescriptorScanner() {
        // 纯静态工具类，禁止实例化
    }

    /** 描述文件中「宿主服务」键名。 */
    public static final String KEY_HOST_SERVICE = "hostService";

    /** 描述文件中「契约」键名。 */
    public static final String KEY_CONTRACTS = "contracts";

    /**
     * 扫描全部二开描述（含 hostService / contracts），供升级风险报告使用。
     *
     * @return 有序列表；每项含 code/version/hostService/contracts（缺省键为空串）
     */
    public static List<Map<String, String>> discoverDetailed() {
        return discoverDetailed(new PathMatchingResourcePatternResolver());
    }

    /**
     * @param resolver 资源解析器；null 时返回空列表
     */
    public static List<Map<String, String>> discoverDetailed(ResourcePatternResolver resolver) {
        List<Map<String, String>> list = new java.util.ArrayList<>();
        if (resolver == null) {
            return list;
        }
        try {
            for (Resource resource : resolver.getResources(DESCRIPTOR_PATTERN)) {
                Properties properties = new Properties();
                try (InputStream in = resource.getInputStream();
                     InputStreamReader reader = new InputStreamReader(in, StandardCharsets.UTF_8)) {
                    properties.load(reader);
                } catch (IOException e) {
                    log.warn("读取二开描述失败 {}：{}", resource.getFilename(), e.getMessage());
                    continue;
                }
                String code = properties.getProperty(KEY_CODE);
                if (!StringUtils.hasText(code)) {
                    continue;
                }
                Map<String, String> row = new LinkedHashMap<>();
                row.put(KEY_CODE, code.trim());
                row.put(KEY_VERSION, properties.getProperty(KEY_VERSION, VERSION_UNKNOWN));
                row.put(KEY_HOST_SERVICE, trimOrEmpty(properties.getProperty(KEY_HOST_SERVICE)));
                row.put(KEY_CONTRACTS, trimOrEmpty(properties.getProperty(KEY_CONTRACTS)));
                list.add(row);
            }
        } catch (IOException e) {
            log.warn("扫描二开描述失败：{}", e.getMessage());
        }
        return list;
    }

    private static String trimOrEmpty(String value) {
        return value == null ? "" : value.trim();
    }

    /**
     * 用默认的 {@link PathMatchingResourcePatternResolver} 扫描当前 classpath 上全部二开。
     *
     * @return 有序（发现顺序）的 {@code code → version} 映射
     */
    public static Map<String, String> discover() {
        return discover(new PathMatchingResourcePatternResolver());
    }

    /**
     * 扫描指定资源解析器可见的全部二开交付描述。
     *
     * @param resolver 资源解析器；为 {@code null} 时返回空映射（调用方无须判空）
     * @return 有序（发现顺序）的 {@code code → version} 映射；无二开时为空映射
     */
    public static Map<String, String> discover(ResourcePatternResolver resolver) {
        Map<String, String> descriptors = new LinkedHashMap<>();
        if (resolver == null) {
            return descriptors;
        }
        try {
            for (Resource resource : resolver.getResources(DESCRIPTOR_PATTERN)) {
                Properties properties = new Properties();
                try (InputStream in = resource.getInputStream();
                     InputStreamReader reader = new InputStreamReader(in, StandardCharsets.UTF_8)) {
                    properties.load(reader);
                } catch (IOException e) {
                    log.warn("读取二开描述失败 {}：{}", resource.getFilename(), e.getMessage());
                    continue;
                }
                String code = properties.getProperty(KEY_CODE);
                if (StringUtils.hasText(code)) {
                    descriptors.put(code.trim(), properties.getProperty(KEY_VERSION, VERSION_UNKNOWN));
                }
            }
        } catch (IOException e) {
            log.warn("扫描二开描述失败：{}", e.getMessage());
        }
        return descriptors;
    }
}
