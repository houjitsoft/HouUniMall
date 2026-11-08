package com.houjiang.mall.controller.plugin;

import com.houjiang.commons.util.ResponseResult;
import com.houjiang.commons.util.SiteInstanceResolver;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 站点身份：把「当前登录会话对应哪个站点实例」这件事交给服务端回答。
 *
 * <p><b>为什么要有这个接口（审计 2026-10-27 P1-4）</b>：前端一直在用
 * {@code window.location.hostname} 自造 {@code instanceId}，而后端
 * {@link SiteInstanceResolver} 用的是 {@code orgId}（退化时 {@code u-用户ID} /
 * {@code mall-商城管理员ID}）。两套口径永远不相等，于是「买了授权但授权管理页是空的」
 * 「解绑提示不是本站点的授权」这类问题反复出现，而且每次都要靠翻日志才能解释。
 *
 * <p>现在前端不再猜：启动时调本接口拿一次，缓存下来用于展示与请求。
 * 展示归展示——<b>后端仍然只认登录态</b>（{@code SiteInstanceResolver.requireAndOverride}），
 * 前端传什么都不影响判定，所以这个接口不构成越权面。
 *
 * <p>返回字段：
 * <ul>
 *   <li>{@code instanceId}：规范化后的站点实例标识（小写、无协议/端口/路径）；</li>
 *   <li>{@code buyerId}：可解析为数字时的购买方 ID（组织 ID），否则 {@code null}；</li>
 *   <li>{@code kind}：{@code org} / {@code user} / {@code mall-admin} / {@code anonymous}；</li>
 *   <li>{@code raw}：未规范化的原始值，仅用于排障展示，不参与任何判定。</li>
 * </ul>
 *
 * @author Amos
 * @since 2026-10-27
 */
@Slf4j
@RestController
@RequestMapping("/base/plugin-center/site")
public class SiteIdentityController {

    @GetMapping("/identity")
    public ResponseResult identity() {
        String raw = SiteInstanceResolver.resolveRawOrNull();
        String instanceId = SiteInstanceResolver.normalize(raw);
        Map<String, Object> view = new LinkedHashMap<>();
        view.put("instanceId", instanceId);
        view.put("buyerId", SiteInstanceResolver.resolveBuyerIdOrNull());
        view.put("kind", kindOf(instanceId));
        view.put("raw", raw);
        return ResponseResult.success(view);
    }

    private String kindOf(String instanceId) {
        if (instanceId == null) {
            return "anonymous";
        }
        if (instanceId.startsWith("u-")) {
            return "user";
        }
        if (instanceId.startsWith("mall-")) {
            return "mall-admin";
        }
        return "org";
    }
}
