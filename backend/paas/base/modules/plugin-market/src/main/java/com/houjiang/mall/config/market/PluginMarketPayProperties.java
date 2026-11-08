package com.houjiang.mall.config.market;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 收费插件购买闭环的支付配置（{@code houcloud.plugin.market.pay}）。
 *
 * <p><b>这是一套收费插件的收款配置</b>：租户在插件市场下单 → 本配置指定的渠道收款 →
 * 渠道回调 {@code /web/plugin-center/market/pay/notify/{channel}} → 订单置为已支付并自动签发授权。
 * 免费插件（FREE）不经过这里。
 *
 * <p><b>配置落点</b>（三选一，优先级从低到高）：
 * <ol>
 *   <li>本地兜底：{@code backend/paas/mall/src/main/resources/application-plugin.yml}</li>
 *   <li>Nacos：{@code mall.yaml} 里同名键覆盖（生产推荐，改完无需重新打包）</li>
 *   <li>环境变量：形如 {@code HOU_PLUGIN_PAY_CHANNEL}，详见下方每个字段的注释</li>
 * </ol>
 *
 * <p><b>fail-fast</b>：{@link #assertUsable()} 在真正发起支付前逐项校验必填项，
 * 缺哪一项就抛「哪一项未配置 + 该填什么 + 去哪配」的具体错误，不做静默降级——
 * 支付配置缺失却静默放行，结果是租户付了钱拿不到授权，比直接报错难排查得多。
 *
 * <p>⚠️ <b>需要部署方自行填写的项</b>见 {@link ChannelProperties} 与 {@link PlanProperties}，
 * 代码里一律不内置任何真实商户号 / 密钥。
 *
 * @author Amos
 */
@Data
@Component
@ConfigurationProperties(prefix = "houcloud.plugin.market.pay")
public class PluginMarketPayProperties {

    /**
     * 是否启用在线购买闭环。关闭后 {@code /base/plugin-center/market/order/create} 直接拒绝下单，
     * 提示「在线购买未开启」，避免租户下出一张永远无法支付的单。
     */
    private boolean enabled = false;

    /**
     * 默认支付渠道编码：{@code CASHIER}（复用商城既有收银台，推荐）/
     * {@code WECHAT_V3} / {@code ALIPAY} / {@code MOCK}。
     * <p>⚠️ 需要你填：决定钱从哪条通道收。{@code WECHAT_V3}/{@code ALIPAY}
     * 需同时在 {@link #channels} 配好对应商户参数；未接入的渠道会在下单时明确报错。
     */
    private String channel = "CASHIER";

    /**
     * 沙箱开关：true 走 {@code MOCK} 渠道（不碰真实资金，本地自测用），
     * 并允许「手动模拟回调」接口；false 走真实渠道。
     * <p>⚠️ 生产必须为 false：沙箱下任何人都能把订单刷成已支付并领走授权。
     *
     * <p><b>默认值为什么是 false</b>：默认值必须是「安全侧」。沙箱下
     * {@code /base/plugin-center/market/pay/sandbox/confirm} 可把任意订单刷成已支付并领走授权，
     * 一旦有人忘了显式关掉（或 Nacos 配置丢了回落到默认值），就是白送授权。
     * 要联调就显式开 {@code sandbox=true}，开完记得关。
     */
    private boolean sandbox = false;

    /** 默认币种（ISO 4217），套餐未单独声明时使用。⚠️ 跨境收款需你改成目标币种。 */
    private String currency = "CNY";

    /** 订单支付窗口（分钟），超时未支付自动关闭。 */
    private int payWindowMinutes = 30;

    /**
     * 本服务对外可访问的基础地址，用于拼回调地址，例如 {@code https://unimall.houjit.com}。
     * <p>⚠️ 需要你填：必须是从公网能访问到本站点的地址；填错或留空会导致渠道回调打不进来，
     * 表现为「用户已付款、订单一直待支付」。
     */
    private String publicBaseUrl = "";

    /**
     * 回调路径（相对 BFF 的 {@code /web} 前缀）。最终回调地址 =
     * {@code publicBaseUrl + notifyPath + "/" + channel}。
     * <p>⚠️ 微信/支付宝商户后台登记的回调地址必须与此一致，否则验签通过也会被渠道拒绝或收不到通知。
     */
    private String notifyPath = "/web/plugin-center/market/pay/notify";

    /** 支付成功后前端跳转地址（渠道支持时下发）。留空表示不跳转。 */
    private String returnUrl = "";

    /**
     * 回调验签容忍的时钟偏移（分钟）：渠道时间戳与本机相差超过此值视为重放攻击。
     */
    private int notifyToleranceMinutes = 5;

    /**
     * 各渠道的商户参数。key 为渠道编码（与 {@link #channel} 同域：{@code WECHAT_V3}/{@code ALIPAY}）。
     * <p>⚠️ 全部需要你填，且禁止提交到 git——请用 Nacos 或环境变量注入。
     */
    private Map<String, ChannelProperties> channels = new LinkedHashMap<>();

    /**
     * 价格套餐：{@code pluginCode -> (planCode -> 套餐)}。
     * <p>套餐优先于商品表 {@code hou_market_plugin.price_cents}：
     * 配了套餐，下单必须传 {@code planCode}；没配套餐，回落到商品表标价。
     * <p>⚠️ 需要你按实际售价配置，代码不内置任何价格。
     */
    private Map<String, Map<String, PlanProperties>> plans = new LinkedHashMap<>();

    // ——— 以下三个字段由 PluginPayConfigStore 在生成快照时填充，yml 里不需要配 ———

    /**
     * 当前快照的配置来源：{@code yml}=只有基线（配置表未建或未配）；
     * {@code db}=已被 {@code hou_market_pay_config} 覆盖；{@code db+yml}=部分覆盖。
     * <p>仅用于配置页展示与排查「我改了库里的值为什么没生效」。
     */
    private String configSource = "yml";

    /**
     * 引用了但环境里没注入的密钥变量名，例如 {@code HOU_PAY_WECHAT_V3_API_KEY}。
     * <p>非空即代表该渠道不可用，会在配置页高亮；密钥值本身不会出现在这里。
     */
    private List<String> unresolvedSecrets = new ArrayList<>();

    /** 快照生成时间。 */
    private LocalDateTime loadedAt;

    /** 深拷贝：Store 基于基线生成快照时用，避免污染基线对象。 */
    public PluginMarketPayProperties deepCopy() {
        PluginMarketPayProperties copy = new PluginMarketPayProperties();
        copy.setEnabled(this.enabled);
        copy.setChannel(this.channel);
        copy.setSandbox(this.sandbox);
        copy.setCurrency(this.currency);
        copy.setPayWindowMinutes(this.payWindowMinutes);
        copy.setPublicBaseUrl(this.publicBaseUrl);
        copy.setNotifyPath(this.notifyPath);
        copy.setReturnUrl(this.returnUrl);
        copy.setNotifyToleranceMinutes(this.notifyToleranceMinutes);
        copy.setConfigSource(this.configSource);
        copy.setLoadedAt(this.loadedAt);
        copy.setUnresolvedSecrets(new ArrayList<>(this.unresolvedSecrets == null
                ? java.util.Collections.emptyList() : this.unresolvedSecrets));

        Map<String, ChannelProperties> ch = new LinkedHashMap<>();
        if (this.channels != null) {
            this.channels.forEach((k, v) -> ch.put(k, v == null ? null : v.copy()));
        }
        copy.setChannels(ch);

        Map<String, Map<String, PlanProperties>> pl = new LinkedHashMap<>();
        if (this.plans != null) {
            this.plans.forEach((pk, byPlan) -> {
                Map<String, PlanProperties> inner = new LinkedHashMap<>();
                if (byPlan != null) {
                    byPlan.forEach((sk, v) -> inner.put(sk, v == null ? null : v.copy()));
                }
                pl.put(pk, inner);
            });
        }
        copy.setPlans(pl);
        return copy;
    }

    /**
     * 发起支付前的配置自检：缺项抛 {@link IllegalStateException}，错误信息直接给到前端。
     *
     * <p>为什么不做「缺了就用默认值」：支付链路的默认值几乎一定是错的（默认商户号收款 =
     * 钱收进别人的账户），宁可下单失败也不能悄悄用错配置。
     */
    public void assertUsable() {
        if (!enabled) {
            throw new IllegalStateException(
                    "在线购买未开启：请配置 houcloud.plugin.market.pay.enabled=true"
                            + "（Nacos mall.yaml 或环境变量 HOU_PLUGIN_PAY_ENABLED=true）");
        }
        if (channel == null || channel.isBlank()) {
            throw new IllegalStateException(
                    "未配置支付渠道：请配置 houcloud.plugin.market.pay.channel"
                            + "（CASHIER / WECHAT_V3 / ALIPAY / MOCK）");
        }
        if (isBlank(publicBaseUrl) && !"MOCK".equalsIgnoreCase(code())) {
            throw new IllegalStateException(
                    "未配置对外基础地址：请配置 houcloud.plugin.market.pay.public-base-url"
                            + "（例如 https://unimall.houjit.com），否则支付渠道无法回调，订单会一直停在待支付");
        }
    }

    /** 渠道编码（大写、去空格）。 */
    public String code() {
        return channel == null ? "" : channel.trim().toUpperCase();
    }

    /** 沙箱或显式 MOCK 渠道都走模拟通道。 */
    public boolean mockMode() {
        return sandbox || "MOCK".equalsIgnoreCase(code());
    }

    /** 取指定渠道的商户参数；未配置返回 null（调用方按需报错）。 */
    public ChannelProperties channel(String code) {
        if (channels == null || code == null) {
            return null;
        }
        return channels.get(code.trim().toUpperCase());
    }

    /** 取某插件的套餐；不存在返回 null。 */
    public PlanProperties plan(String pluginCode, String planCode) {
        if (plans == null || isBlank(pluginCode) || isBlank(planCode)) {
            return null;
        }
        Map<String, PlanProperties> byPlan = plans.get(pluginCode.trim());
        return byPlan == null ? null : byPlan.get(planCode.trim());
    }

    /** 某插件是否配置了套餐（配置了就必须按套餐下单）。 */
    public boolean hasPlans(String pluginCode) {
        if (plans == null || isBlank(pluginCode)) {
            return false;
        }
        Map<String, PlanProperties> byPlan = plans.get(pluginCode.trim());
        return byPlan != null && !byPlan.isEmpty();
    }

    /** 拼出渠道回调地址，例如 https://unimall.houjit.com/web/plugin-center/market/pay/notify/WECHAT_V3 */
    public String notifyUrlOf(String channelCode) {
        String base = publicBaseUrl == null ? "" : publicBaseUrl.trim();
        while (base.endsWith("/")) {
            base = base.substring(0, base.length() - 1);
        }
        String path = isBlank(notifyPath) ? "/web/plugin-center/market/pay/notify" : notifyPath.trim();
        if (!path.startsWith("/")) {
            path = "/" + path;
        }
        String code = channelCode == null ? "" : channelCode.trim().toUpperCase();
        return base + path + (code.isEmpty() ? "" : "/" + code);
    }

    /** 支付窗口（分钟），配置异常（<=0）时回落 30 分钟。 */
    public int effectivePayWindowMinutes() {
        return payWindowMinutes > 0 ? payWindowMinutes : 30;
    }

    private static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    /**
     * 单个支付渠道的商户参数。
     *
     * <p>⚠️ <b>以下字段全部需要部署方自行填写</b>（Nacos / 环境变量），代码与 git 中不保留真实值：
     */
    @Data
    public static class ChannelProperties {

        /**
         * 微信：应用号 / 公众号 appId；支付宝：应用 appId。
         * <p>⚠️ 需要你填。环境变量：{@code HOU_PLUGIN_PAY_CHANNELS_WECHAT_V3_APP_ID}
         */
        private String appId = "";

        /**
         * 商户号（微信 mchId / 支付宝 PID）。
         * <p>⚠️ 需要你填。钱最终结算到这个商户号，填错等于替别人收款。
         */
        private String mchId = "";

        /**
         * API 密钥：微信 V3 为 APIv3 密钥（32 位，用于回调解密），
         * 支付宝为应用私钥（PKCS#8，用于请求签名）。
         * <p>⚠️ 需要你填，禁止进 git。
         */
        private String apiKey = "";

        /**
         * 平台公钥 / 证书公钥：微信为平台证书公钥，支付宝为支付宝公钥（用于回调验签）。
         * <p>⚠️ 需要你填。
         */
        private String publicKey = "";

        /**
         * 商户 API 证书序列号（微信 V3 专用，随请求头 {@code Wechatpay-Serial} 下发）。
         */
        private String certSerialNo = "";

        /** 商户私钥证书路径或 PEM 内容（微信 V3 请求签名用）。 */
        private String privateKeyPath = "";

        /**
         * 该渠道单独指定的回调地址；留空则用全局 {@code publicBaseUrl + notifyPath}。
         * <p>⚠️ 多域名部署时需要你填。
         */
        private String notifyUrl = "";

        /** 该渠道支持的支付方式列表，如 [NATIVE, JSAPI, H5]；留空表示不限制。 */
        private String payMethods = "";

        /**
         * 该渠道商户参数是否齐备（用于下发「待配置清单」给前端，不阻断下单）。
         */
        public boolean complete() {
            return !isBlank(appId) && !isBlank(mchId) && !isBlank(apiKey) && !isBlank(publicKey);
        }

        /**
         * 缺哪些必填项（供报错与引导页展示）。
         */
        public java.util.List<String> missingFields() {
            java.util.List<String> missing = new java.util.ArrayList<>();
            if (isBlank(appId)) {
                missing.add("appId（应用号）");
            }
            if (isBlank(mchId)) {
                missing.add("mchId（商户号）");
            }
            if (isBlank(apiKey)) {
                missing.add("apiKey（API 密钥 / 应用私钥）");
            }
            if (isBlank(publicKey)) {
                missing.add("publicKey（平台公钥，用于回调验签）");
            }
            return missing;
        }

        /** 深拷贝（供 {@link PluginMarketPayProperties#deepCopy()} 使用）。 */
        ChannelProperties copy() {
            ChannelProperties c = new ChannelProperties();
            c.setAppId(this.appId);
            c.setMchId(this.mchId);
            c.setApiKey(this.apiKey);
            c.setPublicKey(this.publicKey);
            c.setCertSerialNo(this.certSerialNo);
            c.setPrivateKeyPath(this.privateKeyPath);
            c.setNotifyUrl(this.notifyUrl);
            c.setPayMethods(this.payMethods);
            return c;
        }

        private static boolean isBlank(String value) {
            return value == null || value.trim().isEmpty();
        }
    }

    /**
     * 价格套餐。
     *
     * <p>⚠️ <b>price / currency / validDays 需要你按实际商业策略填写</b>。
     */
    @Data
    public static class PlanProperties {

        /** 套餐展示名，如「年度版」「7 天试用」。⚠️ 需要你填。 */
        private String name = "";

        /**
         * 售价（元，两位小数）。
         * <p>⚠️ 需要你填。填 0 表示免费/试用，会跳过收银台直接结单发证。
         */
        private BigDecimal price = BigDecimal.ZERO;

        /** 套餐单独指定的币种；留空取全局 {@link #currency}。 */
        private String currency = "";

        /**
         * 授权有效天数：&gt;0 表示买断 N 天，null 或 &lt;=0 表示永久。
         * <p>⚠️ 需要你填。TRIAL 套餐的天数最终以「首次安装成功」起算（见 PluginLicenseService）。
         */
        private Integer validDays;

        /** 套餐绑定的授权类型：PAID / TRIAL。留空取商品表 licenseType。 */
        private String licenseType = "";

        /** 是否上架（false 则该套餐不可选，用于下架某个价位）。 */
        private boolean enabled = true;

        /** 深拷贝（供 {@link PluginMarketPayProperties#deepCopy()} 使用）。 */
        PlanProperties copy() {
            PlanProperties p = new PlanProperties();
            p.setName(this.name);
            p.setPrice(this.price);
            p.setCurrency(this.currency);
            p.setValidDays(this.validDays);
            p.setLicenseType(this.licenseType);
            p.setEnabled(this.enabled);
            return p;
        }
    }
}
