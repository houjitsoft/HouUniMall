package com.houjiang.plugin.artifact.delivery;

import com.houjiang.commons.util.ResponseResult;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 平台对站点的插件制品分发端点（免登录，共享令牌准入）。
 *
 * <p>调用方是<b>别的安装人</b>的站点后端（开源/自部署站点配了
 * {@code houcloud.platform.url} 后，市场在线安装走这里换票）：它没有平台
 * 登录态，只凭共享令牌 {@code X-Platform-Token}——平台未配置令牌时
 * <b>fail-closed</b>，宁可不发制品，也不能让任何人换票。</p>
 *
 * <p>制品本体仍经现有 {@code /base/plugin-center/artifact/raw}（BFF 侧
 * {@code /web/plugin-center/artifact/raw}）流式输出：令牌自证身份
 * （10 分钟 + 限次 + 绑站点），raw 端点无需会话。</p>
 *
 * @author Amos
 * @since 2026-10-30
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/base/plugin-center/market/remote")
public class PlatformPluginDistController {

    private final PlatformPluginDistService distService;

    /** 平台共享令牌：站点换票必须带它（与协议回传 / 下载上报同一把钥匙）。 */
    @Value("${houcloud.platform.token:}")
    private String platformToken;

    /**
     * 站点换取插件安装包下载令牌。
     *
     * <p>插件商品 / 版本审核 / 制品存在的权威判定全部在平台侧完成，
     * 站点本地不再需要市场版本快照。付费插件当前直接拒绝（站点级购买
     * 同步未开放），避免「免费列表里点付费安装」的歧义错误。</p>
     *
     * @param params  {pluginCode, version?, siteCode, siteName?, instanceId?}
     * @param request 原始请求（取 X-Platform-Token 与 IP）
     */
    @PostMapping("/acquire")
    public ResponseResult acquire(@RequestBody(required = false) Map<String, Object> params,
                                  HttpServletRequest request) {
        if (!StringUtils.hasText(platformToken)) {
            log.warn("拒绝站点换票：平台侧未配置 houcloud.platform.token（fail-closed）");
            return ResponseResult.fail(403, "平台未开启站点插件分发");
        }
        String supplied = request == null ? null : request.getHeader("X-Platform-Token");
        if (!StringUtils.hasText(supplied) || !constantTimeEquals(platformToken, supplied.trim())) {
            log.warn("拒绝站点换票：令牌校验失败");
            return ResponseResult.fail(401, "站点令牌无效");
        }
        try {
            Map<String, Object> result = distService.acquireForSite(
                    text(params, "pluginCode"),
                    text(params, "version"),
                    text(params, "siteCode"),
                    text(params, "siteName"),
                    text(params, "instanceId"),
                    clientIp(request));
            return ResponseResult.success(result);
        } catch (IllegalArgumentException e) {
            return ResponseResult.error(e.getMessage());
        } catch (IllegalStateException e) {
            // 业务性拒绝（未上架 / 付费 / 版本未审核 / 制品缺失）：如实转述，站点原样展示
            return ResponseResult.fail(400, e.getMessage());
        } catch (RuntimeException e) {
            log.error("站点换票失败", e);
            return ResponseResult.fail(500, "换票失败: " + e.getMessage());
        }
    }

    private static String text(Map<String, Object> params, String key) {
        Object value = params == null ? null : params.get(key);
        return value == null ? null : String.valueOf(value).trim();
    }

    /**
     * 站点购买授权上报（2026-10-30）：站点在线购买付费插件发证后异步上报，
     * 平台落授权副本——acquire 换票按 (pluginCode, siteCode) 校验副本放行付费插件。
     *
     * <p>与 {@link #acquire} 同一道门（共享令牌 fail-closed）：站点后端持
     * {@code X-Platform-Token} 调用，终端用户不可伪造。幂等由服务层按
     * sourceOrderNo 保证，重发安全。</p>
     *
     * @param params  {pluginCode, siteCode, orderNo, licenseKey?, licenseType?,
     *                 termDays?, sandbox?, expireTime?, instanceId?}
     * @param request 原始请求（取 X-Platform-Token）
     */
    @PostMapping("/license/report")
    public ResponseResult licenseReport(@RequestBody(required = false) Map<String, Object> params,
                                        HttpServletRequest request) {
        if (!StringUtils.hasText(platformToken)) {
            log.warn("拒绝授权上报：平台侧未配置 houcloud.platform.token（fail-closed）");
            return ResponseResult.fail(403, "平台未开启站点授权同步");
        }
        String supplied = request == null ? null : request.getHeader("X-Platform-Token");
        if (!StringUtils.hasText(supplied) || !constantTimeEquals(platformToken, supplied.trim())) {
            log.warn("拒绝授权上报：令牌校验失败");
            return ResponseResult.fail(401, "站点令牌无效");
        }
        try {
            return ResponseResult.success(distService.reportSiteLicenseGrant(params));
        } catch (IllegalArgumentException e) {
            return ResponseResult.error(e.getMessage());
        } catch (IllegalStateException e) {
            // 商品不存在 / 归属冲突：如实转述给站点日志排查
            return ResponseResult.fail(400, e.getMessage());
        } catch (RuntimeException e) {
            log.error("站点授权上报失败", e);
            return ResponseResult.fail(500, "授权上报失败: " + e.getMessage());
        }
    }

    private static String clientIp(HttpServletRequest request) {
        if (request == null) {
            return "";
        }
        String forwarded = request.getHeader("X-Forwarded-For");
        if (StringUtils.hasText(forwarded)) {
            int comma = forwarded.indexOf(',');
            return (comma > 0 ? forwarded.substring(0, comma) : forwarded).trim();
        }
        String real = request.getHeader("X-Real-IP");
        return StringUtils.hasText(real) ? real.trim() : String.valueOf(request.getRemoteAddr());
    }

    private static boolean constantTimeEquals(String expected, String actual) {
        if (expected == null || actual == null || expected.length() != actual.length()) {
            return false;
        }
        int diff = 0;
        for (int i = 0; i < expected.length(); i++) {
            diff |= expected.charAt(i) ^ actual.charAt(i);
        }
        return diff == 0;
    }
}
