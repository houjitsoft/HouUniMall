package com.houjiang.commons.util;

import com.houjiang.commons.vo.user.UserInfoVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 站点实例标识（{@code instanceId}）的<b>服务端解析</b>——插件市场 / 插件授权 / 插件订单的身份收口。
 *
 * <p><b>为什么必须有这个类</b>：插件市场、授权、订单三处控制器原先都直接读请求体里的
 * {@code instanceId}。那是<b>越权口子</b>：任何登录用户都能把请求伪造成别人的站点，
 * 用别人的授权装付费插件、把别人的授权码解绑、把安装记录写进别人站点造成数据污染。
 * 插件中心（{@code PluginCenterController}）已经先改成了服务端解析，本类把这套口径
 * 抽成<b>单一事实来源</b>，避免三处各写一份、慢慢长歪。
 *
 * <p><b>解析顺序（身份归属类字段必须单一事实来源）</b>：
 * <ol>
 *   <li>登录态有 {@code userId} → {@code u-{userId}}（插件购买/安装按会员隔离，优先）；</li>
 *   <li>否则有 {@code orgId} → 用它（单租户开源站「一个组织 = 一个站点实例」）；</li>
 *   <li>{@link SecurityContextUtils} 不认商城管理员的 {@code Map} principal，
 *       这里兜底取 {@code id} 派生 {@code mall-{id}}——不兜底会让商城管理员调用插件侧
 *       接口时整片 500（principal 是 Map 时 {@code getCurrentUserOrNull()} 恒返回 null）；</li>
 *   <li>都没有 → {@code null}，由 {@link #requireAndOverride(Map)} 抛出。</li>
 * </ol>
 * 退化到用户级（{@code u-} / {@code mall-}）而不是返回 null，是为了避免空标识让
 * 授权查询退化成「不限实例」——那等于把别人的授权也查出来。
 * 读授权时可用 {@link #resolveLookupIds()} 同时覆盖用户实例与组织实例，避免历史
 * 「绑在 orgId 上」的授权在改成按会员优先后突然消失。
 *
 * <p><b>为什么不是「有传就用、没传才解析」</b>：那种兼容写法等于漏洞还在，只是加了个默认值。
 * 请求体里带身份字段一律丢弃并记 warn（{@code log.warn}，不是硬拒 400）——
 * 前端 7 处调用无条件带 {@code instanceId}，硬拒会直接打挂整个插件市场页面。
 *
 * @author Amos
 * @since 2026-09-18
 */
@Slf4j
public final class SiteInstanceResolver {

    private SiteInstanceResolver() {
    }

    /**
     * 服务端解析当前请求的站点实例标识。
     *
     * @return 实例标识；未登录或无法识别时返回 {@code null}
     */
    public static String resolveOrNull() {
        List<String> ids = resolveLookupIds();
        return ids.isEmpty() ? null : ids.get(0);
    }

    /**
     * 当前登录可用来查授权的全部实例标识（去重、保序）。
     *
     * <p>顺序：{@code u-{userId}} → {@code orgId} → 商城管理员 {@code mall-{id}}。
     * 新签发/绑码写回只用首项（按会员）；列表合并授权时遍历全部，兼容历史 org 绑定。
     */
    public static List<String> resolveLookupIds() {
        Set<String> ids = new LinkedHashSet<>();
        UserInfoVO user = SecurityContextUtils.getCurrentUserOrNull();
        if (user != null) {
            String userId = user.getUserId();
            if (userId != null && !userId.isBlank()) {
                ids.add("u-" + userId.trim());
            }
            String orgId = user.getOrgId();
            if (orgId != null && !orgId.isBlank()) {
                ids.add(orgId.trim());
            }
        }
        String fromMap = fromPrincipalMap();
        if (fromMap != null && !fromMap.isBlank()) {
            ids.add(fromMap.trim());
        }
        return new ArrayList<>(ids);
    }

    /**
     * 解析站点实例标识，并<b>写回请求体</b>。
     *
     * <p>写回是关键：下游 service（如 {@code PluginMarketService.listPlugins(Map)}）
     * 会自己再从 {@code params} 里读一次 {@code instanceId}。只在控制器层换掉返回值，
     * service 读到的仍是客户端传来的原值，等于没修。写回后无论谁读都是可信值。
     *
     * @param params 请求体；为 {@code null} 时只解析不写回
     * @return 服务端解析出的站点实例标识
     * @throws IllegalArgumentException 未登录或无法识别站点实例时抛出
     */
    public static String requireAndOverride(Map<String, Object> params) {
        if (params != null
                && (params.containsKey("instanceId")
                || params.containsKey("userId")
                || params.containsKey("orgId"))) {
            log.warn("请求携带了客户端身份字段（instanceId/userId/orgId），已忽略并以登录态为准");
        }
        String resolved = resolveOrNull();
        if (resolved == null || resolved.isBlank()) {
            throw new IllegalArgumentException("未登录或无法识别站点实例");
        }
        String instanceId = resolved.trim();
        if (params != null) {
            params.put("instanceId", instanceId);
        }
        return instanceId;
    }

    /**
     * 解析站点实例标识并写回请求体，<b>解析不出时返回 {@code null} 而不是抛异常</b>。
     *
     * <p>给「instanceId 可选」的历史端点用（如插件中心 {@code /toggle}、订单 {@code /mine}）：
     * 这些端点在拿不到实例时会退化成「按本站点库内记录校验」或「返回空列表」，
     * 属于 fail-closed 的旧语义。用 {@link #requireAndOverride(Map)} 会把老客户端
     * 直接打成 500，所以这里保留可空。
     *
     * <p>区别只在于<b>拿不到身份时怎么办</b>；拿到了身份，客户端传值一样会被忽略。
     *
     * @param params 请求体；为 {@code null} 时只解析不写回
     * @return 实例标识；未登录或无法识别时返回 {@code null}
     */
    public static String resolveAndOverrideOrNull(Map<String, Object> params) {
        if (params != null
                && (params.containsKey("instanceId")
                || params.containsKey("userId")
                || params.containsKey("orgId"))) {
            log.warn("请求携带了客户端身份字段（instanceId/userId/orgId），已忽略并以登录态为准");
        }
        String resolved = resolveOrNull();
        if (resolved == null || resolved.isBlank()) {
            return null;
        }
        String instanceId = resolved.trim();
        if (params != null) {
            params.put("instanceId", instanceId);
        }
        return instanceId;
    }

    /**
     * 解析原始值（未规范化）。仅供本类内部与需要区分「原始来源」的诊断接口使用：
     * 对外一律走 {@link #resolveOrNull()}，保证落库与比对只有一种形态。
     * （2026-10-27 补回：bdde04a23 移除了该方法，但 SiteIdentityController 的
     * 站点身份诊断链路依赖它——恢复 API 面，不改现有解析行为。）
     */
    public static String resolveRawOrNull() {
        return resolveOrNull();
    }

    /**
     * 实例标识形态收敛：小写、无协议、无端口、无路径、无 {@code org:}/{@code =} 前缀。
     * 凡是从请求体/配置/命令行进来的实例标识，都应先过这里再落库或比对。
     *
     * @param raw 原始标识（可为空）
     * @return 规范化后的标识；无法识别时返回 {@code null}
     */
    public static String normalize(String raw) {
        if (raw == null) {
            return null;
        }
        String value = raw.trim();
        if (value.isEmpty()) {
            return null;
        }
        int scheme = value.indexOf("://");
        if (scheme >= 0) {
            value = value.substring(scheme + 3);
        }
        int slash = value.indexOf('/');
        if (slash >= 0) {
            value = value.substring(0, slash);
        }
        int colon = value.indexOf(':');
        if (colon > 0) {
            value = value.substring(0, colon);
        }
        int eq = value.indexOf('=');
        if (eq > 0) {
            value = value.substring(eq + 1);
        } else if (value.regionMatches(true, 0, "org:", 0, 4)) {
            value = value.substring(4);
        }
        value = value.trim().toLowerCase();
        return value.isEmpty() ? null : value;
    }

    /**
     * 当前登录用户 ID（数字）。用于合并 {@code buyer_id=用户} 且 {@code instance_id} 为空的历史授权。
     */
    public static Long resolveUserIdOrNull() {
        UserInfoVO user = SecurityContextUtils.getCurrentUserOrNull();
        String userId = user == null ? null : user.getUserId();
        if (userId == null || userId.isBlank()) {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.getPrincipal() instanceof Map<?, ?> map) {
                Object value = map.get("userId") == null ? map.get("uid") : map.get("userId");
                userId = value == null ? null : String.valueOf(value);
            }
        }
        if (userId == null || userId.isBlank()) {
            return null;
        }
        try {
            return Long.valueOf(userId.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * 当前登录态对应的购买方 ID（{@code orgId} 为数字时返回，否则 {@code null}）。
     *
     * <p>用途：在线购买签发的授权有唯一买方，激活时要能回答「这张码是不是我的」。
     * 组织 ID 就是购买方；退化到用户级标识（{@code u-}/{@code mall-}）不是数字，
     * 此时返回 {@code null} 表示「无法判定归属」，调用方应<b>不做</b>归属校验，
     * 而不是拿 0 去比对把正常用户全挡掉。
     */
    public static Long resolveBuyerIdOrNull() {
        UserInfoVO user = SecurityContextUtils.getCurrentUserOrNull();
        String orgId = user == null ? null : user.getOrgId();
        if (orgId == null || orgId.isBlank()) {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.getPrincipal() instanceof Map<?, ?> map) {
                Object value = map.get("orgId");
                orgId = value == null ? null : String.valueOf(value);
            }
        }
        if (orgId == null || orgId.isBlank()) {
            return null;
        }
        try {
            return Long.valueOf(orgId.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * 商城管理员链路兜底：{@link SecurityContextUtils#getCurrentUser()} 只认
     * {@code LoginUser} / {@code UserInfoVO} / {@code String}，而
     * {@code MallAdminTokenAuthenticationFilter} 给商城管理员塞的是 {@code Map}
     * （键：{@code id / root / account / name / avatar / multipoint_login / disable}），
     * 走到那里会抛「用户信息类型不支持」。这里直接从 Map 取身份，保证插件侧接口不因
     * principal 类型不同而整片失效。
     *
     * @return 实例标识；无法识别返回 {@code null}
     */
    private static String fromPrincipalMap() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof Map<?, ?> map)) {
            return null;
        }
        Object orgId = map.get("orgId");
        if (orgId != null && !String.valueOf(orgId).isBlank()) {
            return String.valueOf(orgId).trim();
        }
        Object id = map.get("id");
        if (id != null && !String.valueOf(id).isBlank()) {
            return "mall-" + String.valueOf(id).trim();
        }
        Object userId = map.get("userId") == null ? map.get("uid") : map.get("userId");
        if (userId != null && !String.valueOf(userId).isBlank()) {
            return "u-" + String.valueOf(userId).trim();
        }
        return null;
    }
}
