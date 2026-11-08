package com.houjiang.mall.config.market;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.houjiang.mall.entity.market.MarketPayConfig;
import com.houjiang.mall.mapper.market.MarketPayConfigMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 支付配置的最终取值：{@code 基线（yml / Nacos / 环境变量） + 数据库覆盖}。
 *
 * <p><b>为什么要分层</b>：
 * <table>
 *   <tr><th>参数类型</th><th>落点</th><th>理由</th></tr>
 *   <tr><td>业务参数（开关、售价、套餐、回调地址、币种）</td>
 *       <td>数据库 {@code hou_market_pay_config}</td>
 *       <td>运营要在后台改价 / 上下架套餐 / 切沙箱，改一次重启一次服务不可接受</td></tr>
 *   <tr><td>密钥（apiKey、商户私钥）</td>
 *       <td>环境变量 / Nacos，库里只存 {@code ${ENV_NAME}} 引用</td>
 *       <td>数据库会被备份、导出、被 SQL 注入拖走；库里出现明文密钥等于把商户资金交出去</td></tr>
 * </table>
 *
 * <p><b>取值优先级</b>：库里有这一行 &gt; Nacos &gt; application-plugin.yml &gt; 代码默认值。
 * 库里没建表 / 没数据 / 查询报错，一律回落基线，行为与改造前完全一致（因此可以先上线代码、后建表）。
 *
 * <p><b>并发</b>：对外只暴露 {@link #current()} 返回的不可变快照，刷新时整体替换引用，
 * 不会出现「读到一半新一半旧」的配置。
 *
 * @author Amos
 */
@Slf4j
@Component
public class PluginPayConfigStore {

    /** 密钥类配置键：这些键在库里只能是 {@code ${ENV}} 引用，明文一律忽略并告警 */
    private static final List<String> SECRET_KEYS = List.of("api-key", "private-key");

    /**
     * 各作用域允许的配置键白名单。
     *
     * <p>为什么两侧都要校验：键名写错（{@code publicBaseUrl} 写成 {@code public-base-url} 之外的任何变体）
     * 不会报错、只是<b>永远不生效</b>，排查成本极高。保存时在管理接口拦一次，加载时在
     * {@code applyGlobal/applyChannel/applyPlan} 的默认分支再兜一次。
     */
    public static final Map<String, List<String>> ALLOWED_KEYS = new LinkedHashMap<>();

    static {
        // 用 LinkedHashMap 而不是 Map.of：管理页按这个顺序渲染下拉，顺序必须稳定
        ALLOWED_KEYS.put(MarketPayConfig.SCOPE_GLOBAL, List.of("enabled", "channel", "sandbox", "currency",
                "pay-window-minutes", "public-base-url", "notify-path", "return-url",
                "notify-tolerance-minutes"));
        ALLOWED_KEYS.put(MarketPayConfig.SCOPE_CHANNEL, List.of("app-id", "mch-id", "api-key", "public-key",
                "cert-serial-no", "private-key", "notify-url", "pay-methods"));
        ALLOWED_KEYS.put(MarketPayConfig.SCOPE_PLAN, List.of("name", "price", "currency", "valid-days",
                "license-type", "enabled"));
    }

    private final PluginMarketPayProperties baseline;
    private final ObjectProvider<MarketPayConfigMapper> mapperProvider;
    private final Environment environment;

    /** 当前生效的配置快照；刷新时整体替换 */
    private final AtomicReference<PluginMarketPayProperties> snapshot = new AtomicReference<>();

    /** 配置表是否可用（上次加载的结果，用于配置页提示「表还没建」） */
    private volatile boolean tableReady = false;

    public PluginPayConfigStore(PluginMarketPayProperties baseline,
                                ObjectProvider<MarketPayConfigMapper> mapperProvider,
                                Environment environment) {
        this.baseline = baseline;
        this.mapperProvider = mapperProvider;
        this.environment = environment;
    }

    @PostConstruct
    public void init() {
        try {
            reload();
        } catch (Exception e) {
            // 兜底：配置加载出任何问题都不能阻断服务启动，先用基线跑，
            // 运营建表后调 /web/plugin-center/market/pay/admin/reload 即可生效
            log.error("[支付配置] 启动时加载失败，暂用 Nacos/yml 基线运行", e);
            snapshot.set(baseline.deepCopy());
        }
    }

    /** 当前生效配置（快照，读到的永远是同一份一致状态）。 */
    public PluginMarketPayProperties current() {
        PluginMarketPayProperties view = snapshot.get();
        return view != null ? view : baseline;
    }

    /** 配置表是否可读（建表并写入首行后为 true）。 */
    public boolean isTableReady() {
        return tableReady;
    }

    /**
     * 重新加载：从库里读出全部配置行，在基线副本上覆盖后整体替换快照。
     *
     * <p>由管理接口在改完配置后调用；也用于「先上线代码、后建表」的场景——
     * 建表后调一次刷新即可生效，无需重启服务。
     */
    public synchronized PluginMarketPayProperties reload() {
        PluginMarketPayProperties view = baseline.deepCopy();
        view.setLoadedAt(LocalDateTime.now());

        List<MarketPayConfig> rows;
        try {
            // getIfAvailable 本身也要放进 try：容器启动早期 Mapper / SqlSessionFactory 可能尚未就绪，
            // 此时抛异常绝不能把 @PostConstruct 打断——配置加载失败不该让整个服务起不来。
            MarketPayConfigMapper mapper = mapperProvider == null ? null : mapperProvider.getIfAvailable();
            if (mapper == null) {
                view.setConfigSource("yml");
                snapshot.set(view);
                return view;
            }
            rows = mapper.selectList(new LambdaQueryWrapper<MarketPayConfig>()
                    .orderByAsc(MarketPayConfig::getScope)
                    .orderByAsc(MarketPayConfig::getScopeKey)
                    .orderByAsc(MarketPayConfig::getSubKey)
                    .orderByAsc(MarketPayConfig::getConfigKey));
            tableReady = true;
        } catch (Exception e) {
            // 表没建 / 库连不上 / Mapper 未就绪：一律回落基线，之后可调 reload 重试
            tableReady = false;
            view.setConfigSource("yml(table-missing)");
            log.warn("[支付配置] hou_market_pay_config 不可用，回落 Nacos/yml 基线: {}", e.getMessage());
            snapshot.set(view);
            return view;
        }

        if (rows == null || rows.isEmpty()) {
            view.setConfigSource("yml");
            snapshot.set(view);
            return view;
        }

        List<String> unresolved = new ArrayList<>();
        int applied = 0;
        int skipped = 0;
        for (MarketPayConfig row : rows) {
            if (applyRow(view, row, unresolved)) {
                applied++;
            } else {
                skipped++;
            }
        }
        view.setUnresolvedSecrets(unresolved);
        view.setConfigSource(applied > 0 ? "db+yml" : "yml");
        log.info("[支付配置] 已加载：来源={} 生效行={} 跳过行={} 未注入密钥={}",
                view.getConfigSource(), applied, skipped, unresolved);
        snapshot.set(view);
        return view;
    }

    /** 把一行配置应用到快照上；返回是否成功应用（未知键 / 密钥明文返回 false）。 */
    private boolean applyRow(PluginMarketPayProperties view, MarketPayConfig row, List<String> unresolved) {
        if (row == null || isBlank(row.getScope()) || isBlank(row.getConfigKey())) {
            return false;
        }
        String scope = row.getScope().trim().toUpperCase(Locale.ROOT);
        String key = row.getConfigKey().trim().toLowerCase(Locale.ROOT);
        String value = resolve(row, unresolved);
        if (value == null) {
            return false;
        }
        switch (scope) {
            case MarketPayConfig.SCOPE_GLOBAL -> {
                return applyGlobal(view, key, value);
            }
            case MarketPayConfig.SCOPE_CHANNEL -> {
                String code = safe(row.getScopeKey()).trim().toUpperCase(Locale.ROOT);
                if (code.isEmpty()) {
                    return false;
                }
                PluginMarketPayProperties.ChannelProperties cp =
                        view.getChannels().computeIfAbsent(code, k -> new PluginMarketPayProperties.ChannelProperties());
                return applyChannel(cp, key, value);
            }
            case MarketPayConfig.SCOPE_PLAN -> {
                String plugin = safe(row.getScopeKey()).trim();
                String plan = safe(row.getSubKey()).trim();
                if (plugin.isEmpty() || plan.isEmpty()) {
                    return false;
                }
                Map<String, PluginMarketPayProperties.PlanProperties> byPlan =
                        view.getPlans().computeIfAbsent(plugin, k -> new LinkedHashMap<>());
                PluginMarketPayProperties.PlanProperties pp =
                        byPlan.computeIfAbsent(plan, k -> new PluginMarketPayProperties.PlanProperties());
                return applyPlan(pp, key, value);
            }
            default -> {
                log.warn("[支付配置] 未知 scope={} key={}，已忽略", scope, key);
                return false;
            }
        }
    }

    private boolean applyGlobal(PluginMarketPayProperties view, String key, String value) {
        switch (key) {
            case "enabled" -> view.setEnabled(boolOf(value));
            case "channel" -> view.setChannel(value);
            case "sandbox" -> view.setSandbox(boolOf(value));
            case "currency" -> view.setCurrency(value);
            case "pay-window-minutes" -> view.setPayWindowMinutes(intOf(value, view.getPayWindowMinutes()));
            case "public-base-url" -> view.setPublicBaseUrl(value);
            case "notify-path" -> view.setNotifyPath(value);
            case "return-url" -> view.setReturnUrl(value);
            case "notify-tolerance-minutes" ->
                    view.setNotifyToleranceMinutes(intOf(value, view.getNotifyToleranceMinutes()));
            default -> {
                log.warn("[支付配置] 未知的全局配置键 {}，已忽略", key);
                return false;
            }
        }
        return true;
    }

    private boolean applyChannel(PluginMarketPayProperties.ChannelProperties cp, String key, String value) {
        switch (key) {
            case "app-id" -> cp.setAppId(value);
            case "mch-id" -> cp.setMchId(value);
            case "api-key" -> cp.setApiKey(value);
            case "public-key" -> cp.setPublicKey(value);
            case "cert-serial-no" -> cp.setCertSerialNo(value);
            case "private-key" -> cp.setPrivateKeyPath(value);
            case "notify-url" -> cp.setNotifyUrl(value);
            case "pay-methods" -> cp.setPayMethods(value);
            default -> {
                log.warn("[支付配置] 未知的渠道配置键 {}，已忽略", key);
                return false;
            }
        }
        return true;
    }

    /**
     * 套餐配置。<b>价格与天数解析失败必须拒绝应用，不能静默取 0</b>：
     * price 静默变 0 = 免费送授权；valid-days 静默变 0 = 买断变永久。
     * 这两条都是直接资损，宁可这一行配置不生效（回落到 yml/商品表），也不能猜一个值。
     */
    private boolean applyPlan(PluginMarketPayProperties.PlanProperties pp, String key, String value) {
        switch (key) {
            case "name" -> pp.setName(value);
            case "price" -> {
                BigDecimal price = parseDecimal(value);
                if (price == null) {
                    log.error("[支付配置] 套餐 price 无法解析，该行不生效（避免价格静默变 0 白送授权）: {}", value);
                    return false;
                }
                if (price.compareTo(BigDecimal.ZERO) < 0) {
                    log.error("[支付配置] 套餐 price 为负数，该行不生效: {}", value);
                    return false;
                }
                pp.setPrice(price);
            }
            case "currency" -> pp.setCurrency(value);
            case "valid-days" -> {
                Integer days = parseInt(value);
                if (days == null) {
                    log.error("[支付配置] 套餐 valid-days 无法解析，该行不生效（避免天数静默变 0 导致永久授权）: {}",
                            value);
                    return false;
                }
                pp.setValidDays(days < 0 ? 0 : days);
            }
            case "license-type" -> pp.setLicenseType(value);
            case "enabled" -> pp.setEnabled(boolOf(value));
            default -> {
                log.warn("[支付配置] 未知的套餐配置键 {}，已忽略", key);
                return false;
            }
        }
        return true;
    }

    /**
     * 解析配置值：{@code ${ENV_NAME}} 从环境变量取值；密钥类撞上明文则拒绝应用。
     *
     * <p>这里刻意不做「取不到就留空字符串」的静默降级——密钥没注入必须体现在
     * {@code unresolvedSecrets} 里，让配置页能高亮出来，而不是等到支付回调验签失败才发现。
     */
    private String resolve(MarketPayConfig row, List<String> unresolved) {
        String raw = row.getConfigValue();
        if (raw == null || raw.trim().isEmpty()) {
            return null;
        }
        String value = raw.trim();
        if (value.startsWith("${") && value.endsWith("}") && value.length() > 3) {
            String name = value.substring(2, value.length() - 1).trim();
            String resolved = environment == null ? null : environment.getProperty(name);
            if (resolved == null || resolved.isBlank()) {
                unresolved.add(name);
                return null;
            }
            return resolved;
        }
        boolean secretRow = Integer.valueOf(1).equals(row.getSecret())
                || SECRET_KEYS.contains(safe(row.getConfigKey()).trim().toLowerCase(Locale.ROOT));
        if (secretRow) {
            log.error("[支付配置] 密钥类配置 {}/{} 在库中是明文，已忽略。请改为 ${{环境变量名}} 引用，"
                            + "明文密钥禁止入库（库会被备份/导出，等同于泄露商户资金）",
                    row.getScopeKey(), row.getConfigKey());
            unresolved.add("明文密钥:" + safe(row.getScopeKey()) + "/" + safe(row.getConfigKey()));
            return null;
        }
        return value;
    }

    // ——————————————————————————— 管理视图 ———————————————————————————

    /**
     * 供配置页展示的视图：<b>任何密钥值都不出现在返回里</b>，只回「是否已注入」。
     *
     * <p>为什么不回脱敏后的值：即便是 {@code 19****09} 这种掩码，配合商户号也足以做社工/撞库，
     * 而且配置页的截图会流传。只回布尔量，运营看到「未注入」就知道该去找谁要环境变量。
     */
    public Map<String, Object> describe() {
        PluginMarketPayProperties view = current();
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("source", view.getConfigSource());
        out.put("tableReady", tableReady);
        out.put("loadedAt", view.getLoadedAt() == null ? null : view.getLoadedAt().toString());
        out.put("unresolvedSecrets", view.getUnresolvedSecrets());

        Map<String, Object> global = new LinkedHashMap<>();
        global.put("enabled", view.isEnabled());
        global.put("channel", view.getChannel());
        global.put("sandbox", view.isSandbox());
        global.put("currency", view.getCurrency());
        global.put("payWindowMinutes", view.getPayWindowMinutes());
        global.put("publicBaseUrl", view.getPublicBaseUrl());
        global.put("notifyPath", view.getNotifyPath());
        global.put("returnUrl", view.getReturnUrl());
        global.put("notifyToleranceMinutes", view.getNotifyToleranceMinutes());
        out.put("global", global);
        out.put("notifyUrl", view.notifyUrlOf(view.code()));

        Map<String, Object> channels = new LinkedHashMap<>();
        if (view.getChannels() != null) {
            view.getChannels().forEach((code, cp) -> {
                Map<String, Object> one = new LinkedHashMap<>();
                one.put("appId", cp.getAppId());
                one.put("mchId", cp.getMchId());
                one.put("apiKeyInjected", !isBlank(cp.getApiKey()));
                one.put("publicKeyInjected", !isBlank(cp.getPublicKey()));
                one.put("privateKeyInjected", !isBlank(cp.getPrivateKeyPath()));
                one.put("certSerialNo", cp.getCertSerialNo());
                one.put("notifyUrl", cp.getNotifyUrl());
                one.put("payMethods", cp.getPayMethods());
                one.put("complete", cp.complete());
                one.put("missingFields", cp.missingFields());
                channels.put(code, one);
            });
        }
        out.put("channels", channels);

        Map<String, Object> plans = new LinkedHashMap<>();
        if (view.getPlans() != null) {
            view.getPlans().forEach((plugin, byPlan) -> {
                Map<String, Object> inner = new LinkedHashMap<>();
                byPlan.forEach((code, pp) -> {
                    Map<String, Object> one = new LinkedHashMap<>();
                    one.put("name", pp.getName());
                    one.put("price", pp.getPrice());
                    one.put("currency", pp.getCurrency());
                    one.put("validDays", pp.getValidDays());
                    one.put("licenseType", pp.getLicenseType());
                    one.put("enabled", pp.isEnabled());
                    inner.put(code, one);
                });
                plans.put(plugin, inner);
            });
        }
        out.put("plans", plans);
        return out;
    }

    /**
     * 保存一行配置前的校验：密钥必须是 {@code ${ENV}} 引用；值类型必须能解析。
     *
     * @return 错误信息；null 表示通过
     */
    public String validate(MarketPayConfig row) {
        if (row == null || isBlank(row.getScope()) || isBlank(row.getConfigKey())) {
            return "scope 与 configKey 必填";
        }
        String scope = row.getScope().trim().toUpperCase(Locale.ROOT);
        if (!MarketPayConfig.SCOPE_GLOBAL.equals(scope)
                && !MarketPayConfig.SCOPE_CHANNEL.equals(scope)
                && !MarketPayConfig.SCOPE_PLAN.equals(scope)) {
            return "scope 只能是 GLOBAL / CHANNEL / PLAN，当前为 " + scope;
        }
        if (MarketPayConfig.SCOPE_CHANNEL.equals(scope) && isBlank(row.getScopeKey())) {
            return "CHANNEL 作用域必须填 scopeKey（渠道编码）";
        }
        if (MarketPayConfig.SCOPE_PLAN.equals(scope) && (isBlank(row.getScopeKey()) || isBlank(row.getSubKey()))) {
            return "PLAN 作用域必须填 scopeKey（插件编码）与 subKey（套餐编码）";
        }
        String key = row.getConfigKey().trim().toLowerCase(Locale.ROOT);
        List<String> allowed = ALLOWED_KEYS.get(scope);
        if (allowed != null && !allowed.contains(key)) {
            return "未知的配置键 " + scope + "/" + key + "，可用键：" + String.join("、", allowed);
        }
        boolean isSecret = SECRET_KEYS.contains(key);
        row.setConfigKey(key);
        row.setSecret(isSecret ? 1 : 0);
        String value = row.getConfigValue() == null ? "" : row.getConfigValue().trim();
        if (isSecret) {
            if (!(value.startsWith("${") && value.endsWith("}") && value.length() > 3)) {
                return "密钥类配置（" + key + "）禁止明文入库，请填 ${环境变量名}，"
                        + "真正的值在 Nacos / 容器环境变量里注入";
            }
            return null;
        }
        if (value.isEmpty()) {
            return null;
        }
        // 非密钥：按值类型预解析一次，避免脏数据落库后静默生效
        try {
            switch (valueType(row)) {
                case "BOOL" -> boolOf(value);
                case "INT" -> Integer.parseInt(value);
                case "DECIMAL" -> new BigDecimal(value);
                default -> { }
            }
        } catch (Exception e) {
            return "值类型 " + valueType(row) + " 无法解析：" + value;
        }
        return null;
    }

    private static String valueType(MarketPayConfig row) {
        return isBlank(row.getValueType()) ? "STRING" : row.getValueType().trim().toUpperCase(Locale.ROOT);
    }

    // ——————————————————————————— 工具 ———————————————————————————

    private static boolean boolOf(String value) {
        if (value == null) {
            return false;
        }
        String v = value.trim().toLowerCase(Locale.ROOT);
        return "true".equals(v) || "1".equals(v) || "yes".equals(v) || "on".equals(v);
    }

    private static int intOf(String value, int fallback) {
        try {
            return Integer.parseInt(value.trim());
        } catch (Exception e) {
            return fallback;
        }
    }

    /** 解析小数；失败返回 null（由调用方决定拒绝还是回落，不在本方法里替它兜底）。 */
    private static BigDecimal parseDecimal(String value) {
        if (value == null) {
            return null;
        }
        try {
            return new BigDecimal(value.trim());
        } catch (Exception e) {
            return null;
        }
    }

    /** 解析整数；失败返回 null。 */
    private static Integer parseInt(String value) {
        if (value == null) {
            return null;
        }
        try {
            return Integer.valueOf(value.trim());
        } catch (Exception e) {
            return null;
        }
    }

    private static String safe(String value) {
        return value == null ? "" : value;
    }

    private static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
