package com.houjiang.mall.controller.market;

import com.houjiang.commons.util.ResponseResult;
import com.houjiang.mall.service.market.PluginDownloadStatsService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 插件下载次数：平台侧真实存储 + 对外读取。
 *
 * <p><b>两个方向，别混</b>：</p>
 * <ul>
 *   <li>{@code POST /base/plugin-center/market/download/report} —— <b>别的安装站点打过来</b>。
 *       站点装完插件后把「哪个站点、装了哪个版本」上报到平台，平台落明细并重算下载次数。
 *       它没有平台登录态，只凭共享令牌 {@code X-Platform-Token}（平台未配置令牌时
 *       <b>fail-closed</b>：宁可不收，也不能让任何人往平台库里写数据）。</li>
 *   <li>{@code POST /base/plugin-center/market/download/stats} —— <b>读真实计数</b>。
 *       任何安装人（含开源自部署站点）都可免登录读取；只返回聚合后的次数与站点数，
 *       不含站点标识明细，读不出「谁在用什么」。</li>
 * </ul>
 *
 * @author Amos
 */
@Slf4j
@RestController
@RequestMapping("/base/plugin-center/market/download")
@RequiredArgsConstructor
public class PluginDownloadController {

    private final PluginDownloadStatsService downloadStatsService;

    /** 平台共享令牌：站点上报必须带它。 */
    @Value("${houcloud.platform.token:}")
    private String platformToken;

    /**
     * 站点上报一次下载 / 安装。
     *
     * <p>幂等：同站点同版本重复上报不会重复计数（唯一键在库层保证）。
     */
    @PostMapping("/report")
    public ResponseResult report(@RequestBody(required = false) Map<String, Object> param,
                                 HttpServletRequest request) {
        if (!StringUtils.hasText(platformToken)) {
            log.warn("拒绝插件下载上报：平台侧未配置 houcloud.platform.token（fail-closed）");
            return ResponseResult.fail(403, "平台未开启插件下载上报");
        }
        String supplied = request == null ? null : request.getHeader("X-Platform-Token");
        if (!StringUtils.hasText(supplied) || !constantTimeEquals(platformToken, supplied.trim())) {
            log.warn("拒绝插件下载上报：令牌校验失败");
            return ResponseResult.fail(401, "站点上报令牌无效");
        }
        String pluginCode = text(param, "pluginCode");
        if (!StringUtils.hasText(pluginCode)) {
            pluginCode = text(param, "code");
        }
        if (!StringUtils.hasText(pluginCode)) {
            return ResponseResult.error("缺少插件编码 pluginCode");
        }
        String version = text(param, "version");
        String siteCode = text(param, "siteCode");
        if (!StringUtils.hasText(siteCode)) {
            return ResponseResult.error("缺少站点标识 siteCode");
        }
        String siteName = text(param, "siteName");
        String source = text(param, "source");
        String ip = clientIp(request);
        boolean counted = downloadStatsService.record(pluginCode, version, siteCode, siteName, source, ip);
        Map<String, Object> result = downloadStatsService.statsOf(pluginCode);
        result.put("counted", counted);
        return ResponseResult.success(result);
    }

    /**
     * 读取真实下载次数（免登录，只返回聚合值）。
     *
     * <p>入参支持两种写法：{@code codes: [...]}（批量，列表页用）与 {@code pluginCode: "x"}（详情页用）。
     */
    @PostMapping("/stats")
    public ResponseResult stats(@RequestBody(required = false) Map<String, Object> param) {
        List<String> codes = new ArrayList<>();
        Object raw = param == null ? null : param.get("codes");
        if (raw instanceof List<?> list) {
            for (Object item : list) {
                if (item != null && StringUtils.hasText(String.valueOf(item))) {
                    codes.add(String.valueOf(item).trim());
                }
            }
        }
        String single = text(param, "pluginCode");
        if (!StringUtils.hasText(single)) {
            single = text(param, "code");
        }
        if (StringUtils.hasText(single)) {
            codes.add(single);
        }
        if (codes.isEmpty()) {
            return ResponseResult.error("缺少插件编码 codes 或 pluginCode");
        }
        Map<String, Integer> counts = downloadStatsService.counts(codes);
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("counts", counts);
        if (StringUtils.hasText(single)) {
            result.put("detail", downloadStatsService.statsOf(single));
        }
        return ResponseResult.success(result);
    }

    private static String text(Map<String, Object> param, String key) {
        Object value = param == null ? null : param.get(key);
        return value == null ? null : String.valueOf(value).trim();
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
