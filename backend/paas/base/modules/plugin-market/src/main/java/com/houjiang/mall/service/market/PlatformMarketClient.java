package com.houjiang.mall.service.market;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.houjiang.mall.model.market.MarketPluginItem;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 站点侧：从平台（unimall.houjit.com）实时读取插件市场插件商品。
 *
 * <p><b>为什么要有它（2026-10-29 架构口径，负责人确认）</b>：
 * 插件商品的<b>唯一权威源是平台</b>——有什么插件、什么版本、什么价格、真实下载次数，
 * 只在平台的库里。开源包发到 GitHub 时本地<b>只有已安装插件的表</b>
 * （{@code hou_addon_plugin} / install / license），<b>不带任何市场插件商品快照</b>；
 * 站点打开插件中心时，市场页的目录从这里实时拉取。这样平台上新、改价、下架，
 * 所有站点的市场页立即生效，不需要重新发开源包。</p>
 *
 * <p><b>铁律（别改反）</b>：</p>
 * <ul>
 *   <li>目录拉不到（平台不可达 / 未配地址）→ <b>回落本地表</b>，绝不把市场页打成空白；
 *       平台自身（unimall.houjit.com）不配 {@code houcloud.platform.url}，天然走本地权威表；</li>
 *   <li>目录整体做<b>进程内缓存</b>（默认 300s）：目录是低频变化数据，每次列表页都打平台
 *       会把平台当成压力源；失败后 60s 冷却，避免平台打个盹时站点连环重试；</li>
 *   <li>返回的目录里只有<b>公开插件商品信息</b>，站点自己的安装态 / 授权由调用方合并——
 *       平台永远不携带站点私有状态。</li>
 * </ul>
 *
 * @author Amos
 */
@Slf4j
@Service
public class PlatformMarketClient {

    /** 平台目录缓存默认时长（秒）。 */
    private static final long DEFAULT_CACHE_SECONDS = 300;

    /** 拉取失败后的冷却时长（秒）：失败后短时间内不再打平台，直接回落本地。 */
    private static final long FAIL_COOLDOWN_SECONDS = 60;

    @Value("${houcloud.platform.url:}")
    private String platformUrl;

    /** 站点标识（与协议回传同源配置）：换票时绑定到下载令牌，平台可追溯哪个站点拉的包。 */
    @Value("${houcloud.platform.site-code:}")
    private String siteCode;

    /** 站点名称（留痕展示用）。 */
    @Value("${houcloud.platform.site-name:}")
    private String siteName;

    /** 平台共享令牌：与协议回传 / 下载上报同一把钥匙（同一配置项）。 */
    @Value("${houcloud.platform.token:}")
    private String platformToken;

    /** 目录缓存时长，默认 300 秒。 */
    @Value("${houcloud.platform.market-cache-seconds:300}")
    private long cacheSeconds;

    private final RestTemplate http;

    /**
     * 安装换票专用客户端。目录拉取可以短超时快速回落本地；换票失败会直接打断安装，
     * 必须容忍评估站 / 跨网 HTTPS 握手抖动（实测 unioss→平台偶发超过 1s，原 1s 连接超时
     * 会稳定打出「Connect timed out」）。
     */
    private final RestTemplate acquireHttp;

    /** 目录缓存：平台插件商品整体缓存。 */
    private volatile RemoteCatalog cachedCatalog;

    private volatile long cachedAt = 0L;

    /** 上次失败时间：冷却期内不再请求平台。 */
    private volatile long failedAt = 0L;

    public PlatformMarketClient() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        // 目录：短超时，失败即回落本地表，不拖慢市场列表
        factory.setConnectTimeout(1000);
        factory.setReadTimeout(5000);
        this.http = new RestTemplate(factory);

        SimpleClientHttpRequestFactory acquireFactory = new SimpleClientHttpRequestFactory();
        // 换票：连接 10s / 读 30s（仅 JSON 元数据，不含制品下载）
        acquireFactory.setConnectTimeout(10_000);
        acquireFactory.setReadTimeout(30_000);
        this.acquireHttp = new RestTemplate(acquireFactory);
    }

    /**
     * 是否对接了平台（配了平台地址即视为对接）。
     *
     * @return true = 目录优先从平台读
     */
    public boolean platformLinked() {
        return StringUtils.hasText(platformUrl);
    }

    /**
     * 拉取平台市场目录（带缓存 + 失败冷却）。
     *
     * @return 平台目录；未对接 / 平台不可达 / 响应异常时返回 null，调用方回落本地表
     */
    public RemoteCatalog catalog() {
        if (!platformLinked()) {
            return null;
        }
        long now = System.currentTimeMillis();
        long cacheMs = (cacheSeconds > 0 ? cacheSeconds : DEFAULT_CACHE_SECONDS) * 1000L;
        RemoteCatalog cached = this.cachedCatalog;
        if (cached != null && now - cachedAt < cacheMs) {
            return cached;
        }
        if (now - failedAt < FAIL_COOLDOWN_SECONDS * 1000L) {
            // 冷却期内不再打平台：市场页每次回落本地，不打日志刷屏
            return cached;
        }
        String url = trimSlash(platformUrl) + "/web/plugin-center/market/remote/catalog";
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            ResponseEntity<String> resp = http.postForEntity(url, new HttpEntity<>(Map.of()), String.class);
            if (!resp.getStatusCode().is2xxSuccessful() || !StringUtils.hasText(resp.getBody())) {
                failedAt = now;
                log.warn("读取平台市场目录失败：HTTP {}（回落本地表）", resp.getStatusCode());
                return cached;
            }
            JSONObject json = JSON.parseObject(resp.getBody());
            Object data = json == null ? null : json.get("data");
            if (!(data instanceof JSONObject dataObj)) {
                failedAt = now;
                log.warn("平台市场目录响应缺少 data（回落本地表）");
                return cached;
            }
            RemoteCatalog catalog = parseCatalog(dataObj);
            if (catalog.getPlugins().isEmpty()) {
                // 空目录视为异常：平台不可能一个在架插件都没有，回落本地而不是清空市场页
                failedAt = now;
                log.warn("平台市场目录为空，按异常处理（回落本地表）");
                return cached;
            }
            this.cachedCatalog = catalog;
            this.cachedAt = now;
            return catalog;
        } catch (Exception e) {
            failedAt = now;
            log.warn("读取平台市场目录异常（回落本地表）: {}", e.getMessage());
            return cached;
        }
    }

    /**
     * 向平台换票插件安装包（2026-10-30）：插件商品 / 版本审核 / 制品存在的权威判定
     * 全在平台侧完成；本地不需要市场版本快照。制品本体经 BFF raw 端点流式拉取。
     *
     * <p>与 {@link #catalog()} 的差别：换票是「安装这个动作」的一部分，
     * 不缓存、失败即抛（调用方直接把错误展示给管理员）；目录是展示数据，
     * 有缓存与回落。</p>
     *
     * @param pluginCode 插件编码
     * @param version    版本号；空 = 平台取最新已审核版本
     * @return 换票结果：{version, jarMd5, sha256, sizeBytes, pluginYaml, hostService,
     *         token, downloadUrl(BFF 相对路径)}
     * @throws IllegalStateException 未对接平台 / 令牌未配置 / 平台拒绝（错误文案直接面向管理员）
     */
    public Map<String, Object> acquire(String pluginCode, String version) {
        if (!platformLinked()) {
            throw new IllegalStateException("未配置平台地址 houcloud.platform.url，无法在线安装平台插件");
        }
        if (!StringUtils.hasText(platformToken)) {
            throw new IllegalStateException("未配置平台令牌 houcloud.platform.token，无法在线安装平台插件");
        }
        if (!StringUtils.hasText(siteCode)) {
            throw new IllegalStateException("未配置站点标识 houcloud.platform.site-code，无法在线安装平台插件");
        }
        String url = trimSlash(platformUrl) + "/web/plugin-center/market/remote/acquire";
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("pluginCode", pluginCode);
        if (StringUtils.hasText(version)) {
            body.put("version", version.trim());
        }
        body.put("siteCode", siteCode.trim());
        if (StringUtils.hasText(siteName)) {
            body.put("siteName", siteName.trim());
        }
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("X-Platform-Token", platformToken.trim());
            ResponseEntity<String> resp = acquireHttp.postForEntity(
                    url, new HttpEntity<>(body, headers), String.class);
            if (!resp.getStatusCode().is2xxSuccessful() || !StringUtils.hasText(resp.getBody())) {
                throw new IllegalStateException("平台换票失败：HTTP " + resp.getStatusCode());
            }
            JSONObject json = JSON.parseObject(resp.getBody());
            Integer code = json == null ? null : json.getInteger("code");
            Object data = json == null ? null : json.get("data");
            if (code == null || code != 1) {
                // 平台侧的拒绝文案直接透传（未上架 / 付费 / 版本未审核 / 制品缺失）
                String message = json == null || !StringUtils.hasText(json.getString("message"))
                        ? "平台拒绝换票" : json.getString("message");
                throw new IllegalStateException(message);
            }
            if (!(data instanceof JSONObject dataObj)) {
                throw new IllegalStateException("平台换票响应缺少 data");
            }
            Map<String, Object> result = new LinkedHashMap<>();
            dataObj.forEach((k, v) -> result.put(k, v));
            // downloadUrl 是 BFF 相对路径：拼成绝对地址给下载器直接拉包
            Object relative = result.get("downloadUrl");
            if (relative != null && StringUtils.hasText(String.valueOf(relative))) {
                result.put("downloadUrl", trimSlash(platformUrl) + relative);
            }
            return result;
        } catch (IllegalStateException e) {
            throw e;
        } catch (Exception e) {
            throw new IllegalStateException("平台换票异常: " + e.getMessage(), e);
        }
    }

    /**
     * 站点购买授权上报平台（2026-10-30）：站点在线购买付费插件后，把签发的授权
     * 异步同步到平台侧落库——平台 {@code remote/acquire} 换票对付费插件校验
     * 「该站点是否已持有有效授权」，此前是无条件拒绝（注释写「授权同步是独立
     * 商业链路，未开通前诚实拒绝」），导致站点侧完成了购买、拿到了授权，
     * 安装时平台却不认账，付费变现闭环断在派发环节。
     *
     * <p>与 {@link #acquire} 同模型：凭共享令牌 {@code X-Platform-Token} 准入，
     * 平台按 {@code (pluginCode, siteCode, sourceOrderNo)} 幂等落
     * {@code hou_market_plugin_license}。上报失败不抛出、不阻断发证——
     * 授权的主记录在站点侧，平台那份是派发凭证副本；失败由调用方记日志，
     * 站点可由后续购买/重装时的重试补齐。</p>
     *
     * @param grant 授权上报参数：pluginCode / orderNo / licenseKey / licenseType /
     *              termDays / sandbox / expireTime(ISO-8601)
     * @return true=平台已受理；false=未配置平台或平台拒绝（调用方决定是否记日志）
     */
    public boolean reportLicenseGrant(Map<String, Object> grant) {
        if (!platformLinked() || !StringUtils.hasText(platformToken) || !StringUtils.hasText(siteCode)) {
            return false;
        }
        try {
            String url = trimSlash(platformUrl) + "/web/plugin-center/market/remote/license/report";
            Map<String, Object> body = new LinkedHashMap<>(grant == null ? Map.of() : grant);
            body.put("siteCode", siteCode.trim());
            if (StringUtils.hasText(siteName)) {
                body.put("siteName", siteName.trim());
            }
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("X-Platform-Token", platformToken.trim());
            ResponseEntity<String> resp = http.postForEntity(url, new HttpEntity<>(body, headers), String.class);
            if (!resp.getStatusCode().is2xxSuccessful() || !StringUtils.hasText(resp.getBody())) {
                log.warn("购买授权上报平台失败：HTTP {} plugin={}", resp.getStatusCode(), body.get("pluginCode"));
                return false;
            }
            JSONObject json = JSON.parseObject(resp.getBody());
            Integer code = json == null ? null : json.getInteger("code");
            if (code == null || code != 1) {
                log.warn("购买授权上报被平台拒绝：{} plugin={}",
                        json == null ? "无响应体" : json.getString("message"), body.get("pluginCode"));
                return false;
            }
            return true;
        } catch (Exception e) {
            log.warn("购买授权上报平台异常（不阻断发证，待后续重试补齐）: {} err={}",
                    grant == null ? "?" : grant.get("pluginCode"), e.getMessage());
            return false;
        }
    }

    private RemoteCatalog parseCatalog(JSONObject data) {
        RemoteCatalog catalog = new RemoteCatalog();
        catalog.setPlugins(new ArrayList<>());
        Object plugins = data.get("plugins");
        if (plugins instanceof JSONArray pluginArray) {
            for (int i = 0; i < pluginArray.size(); i++) {
                Object raw = pluginArray.get(i);
                MarketPluginItem item = raw instanceof JSONObject obj
                        ? obj.toJavaObject(MarketPluginItem.class)
                        : JSON.parseObject(JSON.toJSONString(raw), MarketPluginItem.class);
                if (item != null && StringUtils.hasText(item.getCode())) {
                    catalog.getPlugins().add(item);
                }
            }
        }
        Object categories = data.get("categories");
        if (categories instanceof JSONArray cateArray) {
            catalog.setCategories(new ArrayList<>());
            for (int i = 0; i < cateArray.size(); i++) {
                Object raw = cateArray.get(i);
                if (raw instanceof JSONObject obj) {
                    Map<String, Object> row = new LinkedHashMap<>();
                    obj.forEach((k, v) -> row.put(k, v));
                    catalog.getCategories().add(row);
                }
            }
        }
        catalog.setPlans(stringKeyedMap(data.get("plans")));
        catalog.setVersions(stringKeyedMap(data.get("versions")));
        return catalog;
    }

    @SuppressWarnings("unchecked")
    private Map<String, List<Map<String, Object>>> stringKeyedMap(Object raw) {
        Map<String, List<Map<String, Object>>> result = new LinkedHashMap<>();
        if (!(raw instanceof JSONObject obj)) {
            return result;
        }
        for (Map.Entry<String, Object> entry : obj.entrySet()) {
            if (!(entry.getValue() instanceof JSONArray array)) {
                continue;
            }
            List<Map<String, Object>> rows = new ArrayList<>(array.size());
            for (int i = 0; i < array.size(); i++) {
                Object row = array.get(i);
                if (row instanceof JSONObject rowObj) {
                    Map<String, Object> typed = new LinkedHashMap<>();
                    rowObj.forEach((k, v) -> typed.put(k, v));
                    rows.add(typed);
                }
            }
            result.put(entry.getKey(), rows);
        }
        return result;
    }

    private static String trimSlash(String url) {
        String value = url == null ? "" : url.trim();
        while (value.endsWith("/")) {
            value = value.substring(0, value.length() - 1);
        }
        return value;
    }

    /**
     * 平台市场目录：公开插件商品 + 分类字典 + 每插件商品套餐与版本摘要。
     *
     * <p>这些全是平台侧的公开数据；站点自己的安装态 / 授权由
     * {@link PluginMarketService} 合并，不在这里。</p>
     */
    @lombok.Data
    public static class RemoteCatalog {

        /** 在架插件商品基线（含真实下载次数；安装态 / 授权字段为空，由站点合并）。 */
        private List<MarketPluginItem> plugins = List.of();

        /** 平台分类字典（[{id, name, count}]，与市场页 Tab 同源）。 */
        private List<Map<String, Object>> categories = List.of();

        /** 插件编码 → 价格套餐列表（购买入口用）。 */
        private Map<String, List<Map<String, Object>>> plans = Map.of();

        /** 插件编码 → 已发布版本摘要列表（详情页「版本」区用）。 */
        private Map<String, List<Map<String, Object>>> versions = Map.of();
    }
}
