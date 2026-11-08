package com.houjiang.base.controller;

import com.houjiang.base.service.PlatformConsentService;
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

import java.util.List;
import java.util.Map;

/**
 * 平台侧：站点协议签署记录回传Controller。
 *
 * <p>命名空间 {@code /base/platform/consent}。</p>
 *
 * <p><b>{@code /report} 的鉴权</b>：站点回传不带平台登录态（它只是上报一条留痕），
 * 用共享令牌 {@code X-Platform-Token}（平台侧配置 {@code houcloud.platform.token}，
 * 与站点侧同值下发）。
 * 令牌<b>未配置时 fail-closed</b>——宁可收不到回传，也不能让任何人往平台库里写签署记录。</p>
 *
 * <p>{@code /page} 与 {@code /sites} 是平台后台查询通道，由 BFF 侧做登录与权限校验后转发。</p>
 *
 * @author Amos
 */
@Slf4j
@RestController
@RequestMapping("base/platform/consent")
@RequiredArgsConstructor
public class PlatformConsentController {

    private final PlatformConsentService platformConsentService;

    @Value("${houcloud.platform.token:}")
    private String platformToken;

    /**
     * 站点回传签署记录（幂等，失败不影响站点侧站内留痕）。
     */
    @PostMapping("/report")
    public ResponseResult report(@RequestBody(required = false) Map<String, Object> param,
                                 HttpServletRequest request) {
        if (!StringUtils.hasText(platformToken)) {
            log.warn("拒绝站点协议回传：平台侧未配置 houcloud.platform.token（fail-closed）");
            return ResponseResult.fail(403, "平台未开启站点协议回传");
        }
        String supplied = request == null ? null : request.getHeader("X-Platform-Token");
        if (!StringUtils.hasText(supplied) || !constantTimeEquals(platformToken, supplied.trim())) {
            log.warn("拒绝站点协议回传：令牌校验失败");
            return ResponseResult.fail(401, "站点回传令牌无效");
        }
        if (param == null || param.isEmpty()) {
            return ResponseResult.error("回传内容为空");
        }
        String siteCode = param.get("siteCode") == null ? "" : String.valueOf(param.get("siteCode")).trim();
        String siteName = param.get("siteName") == null ? "" : String.valueOf(param.get("siteName")).trim();
        Object items = param.get("items");
        if (!(items instanceof List<?> list)) {
            return ResponseResult.error("缺少签署记录列表 items");
        }
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> records = (List<Map<String, Object>>) list;
        return platformConsentService.report(records, siteCode, siteName);
    }

    /**
     * 平台后台：签署记录分页。
     */
    @PostMapping("/page")
    public ResponseResult page(@RequestBody(required = false) Map<String, Object> param) {
        return platformConsentService.page(param);
    }

    /**
     * 平台后台：已接入回传的站点清单。
     */
    @PostMapping("/sites")
    public ResponseResult sites() {
        return platformConsentService.sites();
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
