package com.houjiang.auth.login.channel;

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.houjiang.auth.feign.LoginChannelConfigClient;
import com.houjiang.commons.constant.CommonStatusEnum;
import com.houjiang.commons.dto.ThirdPartyConfigDTO;
import com.houjiang.commons.util.ResponseResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 统一登录渠道注册中心
 *
 * <p>渠道来自两处，后者覆盖前者：</p>
 * <ol>
 *   <li>{@link LoginChannelDefaults} 的内置标准模板（微信/企业微信/钉钉/QQ/飞书）</li>
 *   <li>渠道编码声明：{@code unilogin.channel-codes} 里登记的额外编码</li>
 * </ol>
 *
 * <p>每个渠道的<b>凭据</b>按以下优先级解析（前者有值即用前者）：</p>
 * <ol>
 *   <li>Nacos {@code unilogin.channels.<code>.*} —— 运维的显式覆盖</li>
 *   <li>宿主「设置 → 第三方集成」页面填写的配置（auth 通过 thirdparty 只读通道获取）</li>
 *   <li>旧版 {@code wechat.open.*} —— 仅微信，兼容升级前已在生产的配置</li>
 *   <li>内置模板默认端点</li>
 * </ol>
 *
 * <p>Nacos 部分实时读取、第三方配置读取结果做 60 秒进程内缓存，
 * 因此设置页保存后最长一分钟生效，不需要重启服务。</p>
 *
 * <p>想写自定义实现时，把 {@link LoginChannelProvider} 注册成 Spring Bean 即可，
 * 它会优先于配置驱动实现被使用。</p>
 *
 * @author Amos
 */
@Slf4j
@Component
public class LoginChannelRegistry {

    /** 配置前缀 */
    private static final String PREFIX = "unilogin.channels.";
    /** 额外渠道编码列表的配置键 */
    private static final String EXTRA_CODES_KEY = "unilogin.channel-codes";
    /** 缓存有效期，毫秒 */
    private static final long CACHE_TTL_MS = 60_000L;

    private final Environment environment;
    private final RestTemplate restTemplate;
    private final LoginChannelConfigClient loginChannelConfigClient;
    /** 自定义 Provider（Bean 形式注册），优先于配置驱动实现 */
    private final Map<String, LoginChannelProvider> customProviders = new LinkedHashMap<>();
    /** 配置驱动实现的缓存，key=渠道编码 */
    private final Map<String, ConfigurableOAuthChannelProvider> cache = new ConcurrentHashMap<>();
    /** 「设置 → 第三方集成」凭据的缓存，key=渠道编码 */
    private final Map<String, Map<String, String>> thirdPartyCache = new ConcurrentHashMap<>();
    private volatile long cacheBuiltAt = 0L;

    @Autowired
    public LoginChannelRegistry(Environment environment,
                                @Qualifier("externalRestTemplate") RestTemplate restTemplate,
                                LoginChannelConfigClient loginChannelConfigClient,
                                List<LoginChannelProvider> providers) {
        this.environment = environment;
        this.restTemplate = restTemplate;
        this.loginChannelConfigClient = loginChannelConfigClient;
        if (providers != null) {
            for (LoginChannelProvider provider : providers) {
                if (provider != null && StrUtil.isNotBlank(provider.code())) {
                    customProviders.put(provider.code().trim().toLowerCase(), provider);
                    log.info("[unilogin] 注册自定义登录渠道实现: {}", provider.code());
                }
            }
        }
    }

    /**
     * 取渠道执行器。未知编码也会返回一个「未配置」的实例——
     * 这样前端拿到的是明确的「尚未开通」提示，而不是难以定位的 404。
     *
     * @param code 渠道编码，不区分大小写
     */
    public LoginChannelProvider get(String code) {
        if (StrUtil.isBlank(code)) {
            return null;
        }
        String key = code.trim().toLowerCase();
        LoginChannelProvider custom = customProviders.get(key);
        if (custom != null) {
            return custom;
        }
        refreshCacheIfNeeded();
        return cache.computeIfAbsent(key, this::build);
    }

    /** 全部已声明渠道（内置优先按固定顺序，自定义/额外渠道追加在后） */
    public List<LoginChannelProvider> all() {
        refreshCacheIfNeeded();
        Set<String> codes = new LinkedHashSet<>(LoginChannelDefaults.codes());
        for (String code : parseCodes(environment.getProperty(EXTRA_CODES_KEY))) {
            codes.add(code);
        }
        codes.addAll(customProviders.keySet());

        List<LoginChannelProvider> list = new ArrayList<>();
        for (String code : codes) {
            LoginChannelProvider provider = get(code);
            if (provider != null) {
                list.add(provider);
            }
        }
        return list;
    }

    /**
     * 渠道概况（供设置页/排障查看），只暴露编码、名称、是否已配置，
     * <b>不含任何凭据片段</b>。
     */
    public List<Map<String, Object>> describe() {
        List<Map<String, Object>> list = new ArrayList<>();
        for (LoginChannelProvider provider : all()) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("code", provider.code());
            item.put("name", provider.displayName());
            item.put("enabled", provider.isConfigured());
            list.add(item);
        }
        return list;
    }

    // ==================== 内部 ====================

    private void refreshCacheIfNeeded() {
        if (System.currentTimeMillis() - cacheBuiltAt > CACHE_TTL_MS) {
            cache.clear();
            thirdPartyCache.clear();
            cacheBuiltAt = System.currentTimeMillis();
        }
    }

    /**
     * 按「内置模板 + 分层凭据」构建渠道执行器。
     */
    private ConfigurableOAuthChannelProvider build(String code) {
        LoginChannelDef template = LoginChannelDefaults.templateOf(code);
        Map<String, String> fromThirdParty = readThirdPartyValues(code);
        LoginChannelDef def = LoginChannelDefaults.override(template, code, field -> {
            // 1) Nacos 显式配置优先
            String explicit = environment.getProperty(PREFIX + code + "." + field);
            if (StrUtil.isNotBlank(explicit)) {
                return explicit;
            }
            // 2) 设置页（第三方集成）填写的凭据
            String configured = fromThirdParty.get(field);
            if (StrUtil.isNotBlank(configured)) {
                return configured;
            }
            // 3) 旧版微信配置，仅微信
            return readLegacyWechat(code, field);
        });
        log.info("[unilogin] 渠道 {} 凭据解析完成: enabled={}", code, def.isConfigured());
        return new ConfigurableOAuthChannelProvider(def, restTemplate);
    }

    /** 旧版 {@code wechat.open.*} 兼容读取 */
    private String readLegacyWechat(String code, String field) {
        if (!"wechat".equals(code)) {
            return null;
        }
        switch (field) {
            case "app-id":
                return environment.getProperty("wechat.open.app-id");
            case "app-secret":
                return environment.getProperty("wechat.open.app-secret");
            case "redirect-uri":
                return environment.getProperty("wechat.open.redirect-uri");
            default:
                return null;
        }
    }

    /**
     * 读取「设置 → 第三方集成」里填写的凭据，映射成渠道字段。
     *
     * <p>失败一律返回空表：登录是关键路径，第三方配置服务抖动不能把登录拖挂，
     * 此时会自然回退到 Nacos 或内置默认。</p>
     *
     * @return key 为渠道字段的中划线名（app-id / app-secret / redirect-uri / custom-params）
     */
    private Map<String, String> readThirdPartyValues(String code) {
        Map<String, String> cached = thirdPartyCache.get(code);
        if (cached != null) {
            return cached;
        }
        Map<String, String> values = new HashMap<>();
        try {
            LoginChannelDef template = LoginChannelDefaults.templateOf(code);
            String platform = template == null ? null : template.getConfigPlatform();
            if (StrUtil.isNotBlank(platform)) {
                JSONObject dto = fetchThirdPartyConfig(platform);
                if (dto != null) {
                    fillFromThirdParty(values, template, dto);
                }
            }
        } catch (Exception e) {
            log.warn("[unilogin] 读取第三方集成配置失败, channel={}: {}", code, e.getMessage());
        }
        thirdPartyCache.put(code, values);
        return values;
    }

    private JSONObject fetchThirdPartyConfig(String platform) {
        ResponseResult<ThirdPartyConfigDTO> response = loginChannelConfigClient.getLoginChannelConfig(platform);
        if (response == null || response.getCode() != CommonStatusEnum.SUCCESS.getCode()
                || response.getData() == null) {
            return null;
        }
        return (JSONObject) JSON.toJSON(response.getData());
    }

    /** 把第三方配置里的子对象映射到渠道字段；显式关闭（enabled=false）视为未配置 */
    private void fillFromThirdParty(Map<String, String> values, LoginChannelDef template, JSONObject dto) {
        if (Boolean.FALSE.equals(dto.getBoolean("enabled"))) {
            return;
        }
        String configKey = template.getConfigKey();
        JSONObject sub = StrUtil.isBlank(configKey) ? dto : dto.getJSONObject(configKey);
        if (sub == null) {
            return;
        }
        putIfPresent(values, "app-id", sub.getString(
                StrUtil.blankToDefault(template.getConfigAppIdField(), "appId")));
        putIfPresent(values, "app-secret", sub.getString(
                StrUtil.blankToDefault(template.getConfigAppSecretField(), "appSecret")));
        putIfPresent(values, "redirect-uri", sub.getString(
                StrUtil.blankToDefault(template.getConfigRedirectField(), "callbackUrl")));

        // 渠道特有参数（如企业微信 agentId）转成 custom-params 模板变量
        List<String> extraPairs = new ArrayList<>();
        for (String item : StrUtil.blankToDefault(template.getConfigExtraFields(), "").split(",")) {
            if (StrUtil.isBlank(item) || !item.contains("=")) {
                continue;
            }
            int idx = item.indexOf('=');
            String varName = item.substring(0, idx).trim();
            String fieldName = item.substring(idx + 1).trim();
            String value = sub.getString(fieldName);
            if (StrUtil.isNotBlank(varName) && StrUtil.isNotBlank(value)) {
                extraPairs.add(varName + "=" + value.trim());
            }
        }
        if (!extraPairs.isEmpty()) {
            putIfPresent(values, "custom-params", String.join("&", extraPairs));
        }
    }

    private void putIfPresent(Map<String, String> values, String key, String value) {
        if (StrUtil.isNotBlank(value)) {
            values.put(key, value.trim());
        }
    }

    private List<String> parseCodes(String raw) {
        List<String> codes = new ArrayList<>();
        if (StrUtil.isBlank(raw)) {
            return codes;
        }
        for (String item : raw.split(",")) {
            if (StrUtil.isNotBlank(item)) {
                codes.add(item.trim().toLowerCase());
            }
        }
        return codes;
    }
}
