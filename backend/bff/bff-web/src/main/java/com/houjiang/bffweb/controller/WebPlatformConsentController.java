package com.houjiang.bffweb.controller;

import com.houjiang.bffweb.feign.BaseServiceFeign;
import com.houjiang.bffweb.util.ResponseResult;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 平台侧：站点协议签署记录回传与后台查询。
 *
 * <p><b>两个方向的调用，别混：</b></p>
 * <ul>
 *   <li>{@code POST /web/platform/consent/report} —— <b>别的站点打过来</b>。
 *       站点在自己库里写完 {@code hou_user_agreement_consent} 后，把同一条签署凭据上报到平台
 *       （平台地址来自站点的 {@code houcloud.platform.url}）。它<b>没有平台登录态</b>，
 *       只凭 {@code X-Platform-Token} 鉴权，令牌在 base 侧 fail-closed 校验。</li>
 *   <li>{@code POST /web/platform/consent/page} / {@code /sites} —— <b>平台自己人查</b>。
 *       走正常登录态，由网关/BFF 的既有鉴权链路把关。</li>
 * </ul>
 *
 * @author Amos
 */
@Slf4j
@RestController
@RequestMapping("/web/platform/consent")
@RequiredArgsConstructor
public class WebPlatformConsentController {

    private final BaseServiceFeign baseServiceFeign;

    /**
     * 站点回传签署记录（免登录，令牌鉴权在 base 侧）。
     */
    @PostMapping("/report")
    public ResponseResult report(@RequestBody(required = false) Map<String, Object> param,
                                 HttpServletRequest request) {
        String token = request == null ? null : request.getHeader("X-Platform-Token");
        if (token == null || token.isBlank()) {
            return ResponseResult.fail(401, "缺少站点回传令牌");
        }
        return baseServiceFeign.platformConsentReport(token.trim(), param == null ? new java.util.HashMap<>() : param);
    }

    /**
     * 平台后台：签署记录分页（可按站点、协议、用户、场景筛选）。
     */
    @PostMapping("/page")
    public ResponseResult page(@RequestBody(required = false) Map<String, Object> param) {
        return baseServiceFeign.platformConsentPage(param == null ? new java.util.HashMap<>() : param);
    }

    /**
     * 平台后台：已接入回传的站点清单。
     */
    @PostMapping("/sites")
    public ResponseResult sites(@RequestBody(required = false) Map<String, Object> param) {
        return baseServiceFeign.platformConsentSites(param);
    }
}
