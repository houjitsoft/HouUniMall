package com.houjiang.mall.controller.market;

import com.houjiang.commons.util.PluginManageGate;
import com.houjiang.commons.util.ResponseResult;
import com.houjiang.commons.util.SiteInstanceResolver;
import com.houjiang.mall.model.market.CatalogUpdateQuery;
import com.houjiang.mall.service.market.PluginCatalogUpdateService;
import com.houjiang.mall.service.market.PluginMarketCatalogService;
import com.houjiang.mall.service.market.PluginMarketService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 插件市场（形态 A：归属 base，对外经 BFF {@code /web/plugin-center/market/**} 暴露）。
 *
 * <p>T7 第一层：市场浏览 + 定价 + 版本。在线安装随后接入 plugin-engine。
 *
 * @author Amos
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/base/plugin-center/market")
public class PluginMarketController {

    private final PluginMarketService pluginMarketService;

    private final PluginCatalogUpdateService pluginCatalogUpdateService;

    private final PluginMarketCatalogService pluginMarketCatalogService;

    /** 价格套餐来源：详情接口要把「买哪个套餐」的选项交给前端，否则前端无从传 planCode。 */
    private final com.houjiang.mall.pay.plugin.PluginPayChannelRouter channelRouter;

    /**
     * 宿主框架版本：用于逐版本标出 {@code compatible}。
     *
     * <p>与 {@code houcloud.update.framework-version} 同源；市场模块不依赖 plugin-update，
     * 这里直接用配置读取，避免为了一个字符串引入模块耦合。
     */
    @org.springframework.beans.factory.annotation.Value("${houcloud.update.framework-version:1.0.0}")
    private String frameworkVersion;

    /**
     * 写动作（在线安装）是否强制校验插件管理权限。与插件中心、插件更新共用同一开关
     * 与同一判定实现（{@link PluginManageGate}）。
     *
     * <p>默认 <b>true</b>：这是「装插件」的两扇门之一（另一扇是 plugin-update 的
     * install/uninstall/upgrade/rollback）。只锁一扇等于没锁——攻击者走没锁的那扇即可。
     */
    @org.springframework.beans.factory.annotation.Value("${houcloud.plugin.require-manage-authority:true}")
    private boolean requireManageAuthority;

    @PostMapping("/list")
    public ResponseResult list(@RequestBody(required = false) Map<String, Object> params) {
        try {
            requiredInstance(params);
            return ResponseResult.success(pluginMarketService.listPlugins(params));
        } catch (Exception e) {
            log.error("查询插件市场列表失败", e);
            return ResponseResult.fail(500, "查询插件市场列表失败: " + e.getMessage());
        }
    }

    /**
     * 市场分类。
     *
     * <p>返回 {@code [{id, name, count}]}：<b>id 来自 hou_addon_cate（唯一分类字典）</b>、
     * count 为该分类下已上架插件数、未归类插件归入 id=0 的「未分类」桶。
     * 市场页据此渲染分类 Tab、按 id 筛选，与插件中心、顶栏「插件应用」三处同源同数。
     */
    @PostMapping("/categories")
    public ResponseResult categories() {
        try {
            return ResponseResult.success(pluginMarketService.categoryOptions());
        } catch (Exception e) {
            log.error("查询插件市场分类失败", e);
            return ResponseResult.fail(500, "查询插件市场分类失败: " + e.getMessage());
        }
    }

    /**
     * 平台对外目录（2026-10-29 架构口径）：全部在架插件商品 + 分类字典 + 套餐 + 版本摘要。
     *
     * <p>调用方是「别的安装人」的站点后端（开源包本地没有插件商品快照，市场页目录
     * 实时从这里读）。免平台登录态——插件商品本就是公开信息，与官网市场页同口径；
     * 站点私有状态（安装态/授权）不在这里，由站点合并。白名单见
     * {@code PluginCenterSessionFilter#isCredentialEndpoint}。</p>
     */
    @PostMapping("/remote/catalog")
    public ResponseResult remoteCatalog() {
        try {
            return ResponseResult.success(pluginMarketService.remoteCatalog());
        } catch (Exception e) {
            log.error("生成平台市场目录失败", e);
            return ResponseResult.fail(500, "生成平台市场目录失败: " + e.getMessage());
        }
    }

    @PostMapping("/detail")
    public ResponseResult detail(@RequestBody Map<String, Object> params) {
        try {
            String code = requiredCode(params);
            String instanceText = requiredInstance(params);
            Object detail = pluginMarketService.detail(code, instanceText);
            // 🔴 2026-10-27 修 P2：详情带出该插件已启用的价格套餐。
            // 以前前端只能看到插件商品表标价，checkout 也就从不传 planCode；插件一旦按套餐售卖，
            // 下单会被后端拒（「必须指定 planCode」），而前端根本无从知道有哪些套餐。
            if (detail instanceof Map<?, ?> map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> view = (Map<String, Object>) map;
                view.put("plans", channelRouter.plansOf(code));
            }
            return ResponseResult.success(detail);
        } catch (Exception e) {
            log.error("查询插件市场详情失败", e);
            return ResponseResult.fail(500, "查询插件市场详情失败: " + e.getMessage());
        }
    }

    /**
     * 在线安装：下载 → MD5 校验 → 落盘 → 登记安装态。
     *
     * <p>参数：{@code pluginCode}（必填）、{@code version}（可选，缺省取最新已审核版本）、
     * {@code installProfile}（可选）。
     *
     * <p>{@code instanceId} <b>不是入参</b>：由服务端按登录态解析，请求体里传了会被忽略
     * 并记 warn（见 {@link #requiredInstance(Map)}）。授权校验因此只会落在当前登录站点上。
     */
    @PostMapping("/install")
    public ResponseResult install(@RequestBody Map<String, Object> params) {
        ResponseResult<Object> denied = PluginManageGate.check(requireManageAuthority);
        if (denied != null) {
            return denied;
        }
        try {
            String code = requiredCode(params);
            Object version = params == null ? null : params.get("version");
            String versionText = version == null ? null : version.toString().trim();
            Object profile = params == null ? null : params.get("installProfile");
            String profileText = profile == null ? null : profile.toString().trim();
            String instanceText = requiredInstance(params);
            return ResponseResult.success(pluginMarketService.installFromMarket(
                    code, versionText, profileText, instanceText));
        } catch (IllegalArgumentException | IllegalStateException e) {
            // 业务拒绝（未上架 / 未授权 / 跨宿主 3020 / 安装包缺失）不是系统故障：
            // 不回 500、不打 error 栈，否则监控会把「用户点了不该点的插件」算成线上故障。
            log.warn("在线安装插件被拒: {}", e.getMessage());
            return ResponseResult.fail(400, e.getMessage());
        } catch (Exception e) {
            log.error("在线安装插件失败", e);
            return ResponseResult.fail(500, "在线安装插件失败: " + e.getMessage());
        }
    }

    /**
     * 租户视角的已发布版本列表（市场详情页「版本选择」用）。
     *
     * <p>与 {@code /base/plugin-center/publish/versions/list} 的区别：那个是发布者看自己的全部版本
     * （含草稿/审核中），本接口是租户看<b>已发布</b>版本，带 changelog 与兼容性判定。
     * 两者都不返回 {@code jarUrl}——下载必须走 {@code /base/plugin-center/artifact/download-url} 换票。
     *
     * <p><b>制品指纹一律不下发</b>：{@code jarMd5} / {@code jarSha256} / {@code jarSize}
     * 已从返回体剔除（见 {@code PluginMarketCatalogService#toItem}）。本接口对任意已登录用户
     * 开放，返回指纹等于「不买也能枚举全部版本的制品指纹」。完整性校验所需的摘要由
     * {@code /base/plugin-center/artifact/download-url} 换票成功后随链接下发。
     *
     * <p>参数：{@code pluginCode}（必填）、{@code currentVersion}（可选，标出当前版本）。
     * 本接口不读 {@code instanceId}——剔除了制品指纹与 {@code jarUrl} 之后，
     * 剩下的版本列表（版本号 / changelog / 兼容性）确实是与租户无关的公开信息。
     */
    @PostMapping("/versions")
    public ResponseResult versions(@RequestBody(required = false) Map<String, Object> params) {
        try {
            String code = requiredCode(params);
            Object current = params == null ? null : params.get("currentVersion");
            return ResponseResult.success(pluginMarketCatalogService.versions(
                    code,
                    current == null ? null : current.toString().trim(),
                    frameworkVersion));
        } catch (IllegalArgumentException e) {
            return ResponseResult.fail(400, e.getMessage());
        } catch (Exception e) {
            log.error("查询插件版本列表失败", e);
            return ResponseResult.fail(500, "查询插件版本列表失败: " + e.getMessage());
        }
    }

    /**
     * 目录「有更新」判定（P0-10 / M5）。
     *
     * <p>插件中心把自己已装的 {@code codes[]} + {@code versions[]} 一次性送上来，
     * 服务端按 <b>SemVer</b>（不是字符串比较）判断哪些有更高版本，
     * 并把被拦下的原因（未购买 / 已过期 / 框架版本过低）一并回给前端。
     *
     * <p>入参兼容两种写法：
     * <ol>
     *   <li>{@code plugins: [{code, version}]}（推荐）；</li>
     *   <li>{@code codes: [...]} + {@code versions: [...]}（并行数组，与方案 §4.2 文案一致）。</li>
     * </ol>
     * {@code instanceId} 由服务端按登录态解析（<b>不取自请求体</b>），授权判定因此
     * 严格收敛在当前登录站点上；解析不出身份则直接拒绝。
     */
    @PostMapping("/catalog/updates")
    public ResponseResult catalogUpdates(@RequestBody Map<String, Object> params) {
        try {
            String instanceText = requiredInstance(params);
            return ResponseResult.success(
                    pluginCatalogUpdateService.catalogUpdates(instanceText, parseQueries(params)));
        } catch (Exception e) {
            log.error("查询插件更新失败", e);
            return ResponseResult.fail(500, "查询插件更新失败: " + e.getMessage());
        }
    }

    /**
     * 解析「已装清单」入参。
     *
     * @param params 原始入参
     * @return 查询项列表
     */
    @SuppressWarnings("unchecked")
    private List<CatalogUpdateQuery> parseQueries(Map<String, Object> params) {
        List<CatalogUpdateQuery> queries = new ArrayList<>();
        if (params == null || params.isEmpty()) {
            return queries;
        }
        Object plugins = params.get("plugins");
        if (plugins instanceof List<?> list) {
            for (Object raw : list) {
                if (raw instanceof Map<?, ?> item) {
                    Map<String, Object> typed = (Map<String, Object>) item;
                    Object code = typed.get("code") == null ? typed.get("pluginCode") : typed.get("code");
                    if (code == null) {
                        continue;
                    }
                    Object version = typed.get("version") == null
                            ? typed.get("currentVersion")
                            : typed.get("version");
                    queries.add(new CatalogUpdateQuery(code.toString().trim(),
                            version == null ? null : version.toString().trim()));
                } else if (raw != null) {
                    // 也允许直接给字符串数组：[{code}] 或 ["codeA", "codeB"]
                    queries.add(new CatalogUpdateQuery(raw.toString().trim(), null));
                }
            }
            if (!queries.isEmpty()) {
                return queries;
            }
        }
        Object codes = params.get("codes");
        if (codes instanceof List<?> codeList) {
            List<?> versions = params.get("versions") instanceof List<?> versionList ? versionList : List.of();
            for (int i = 0; i < codeList.size(); i++) {
                Object code = codeList.get(i);
                if (code == null) {
                    continue;
                }
                Object version = i < versions.size() ? versions.get(i) : null;
                queries.add(new CatalogUpdateQuery(code.toString().trim(),
                        version == null ? null : version.toString().trim()));
            }
        }
        return queries;
    }

    private String requiredCode(Map<String, Object> params) {
        Object code = params == null ? null : params.get("pluginCode");
        if (code == null) {
            code = params == null ? null : params.get("code");
        }
        if (code == null || code.toString().trim().isEmpty()) {
            throw new IllegalArgumentException("插件编码不能为空");
        }
        return code.toString().trim();
    }

    /**
     * 站点实例标识：<b>由服务端按登录态解析</b>，不信任请求体。
     *
     * <p>这里委托 {@link SiteInstanceResolver#requireAndOverride(Map)}，与插件中心
     * {@code PluginCenterController} 保持同一口径：请求体里带的
     * {@code instanceId / userId / orgId} 一律丢弃并记 warn（不是硬拒 400——
     * 前端 7 处调用无条件带 {@code instanceId}，硬拒会直接打挂插件市场页面），
     * 解析失败则抛 {@link IllegalArgumentException}。
     *
     * <p>关键点：解析结果会<b>写回</b> {@code params} 的 {@code instanceId} 键。
     * 下游 {@code PluginMarketService.listPlugins(Map)} 会自己再从 {@code params}
     * 里读一次，只换返回值不写回等于没修。
     *
     * @param params 请求体；解析成功后其中的 {@code instanceId} 被覆盖为服务端可信值
     * @return 服务端解析出的站点实例标识
     * @throws IllegalArgumentException 未登录或无法识别站点实例时抛出
     */
    private String requiredInstance(Map<String, Object> params) {
        return SiteInstanceResolver.requireAndOverride(params);
    }
}
