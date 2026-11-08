package com.houjiang.plugin.artifact.controller;

import com.houjiang.commons.constant.PluginDistErrorCode;
import com.houjiang.commons.util.ResponseResult;
import com.houjiang.plugin.artifact.download.DownloadUrlResult;
import com.houjiang.plugin.artifact.download.PluginArtifactDownloadService;
import com.houjiang.plugin.artifact.download.RawArtifact;
import com.houjiang.plugin.artifact.exception.ArtifactNotFoundException;
import com.houjiang.plugin.artifact.exception.ArtifactRangeNotSatisfiableException;
import com.houjiang.plugin.artifact.exception.DownloadDeniedException;
import com.houjiang.plugin.artifact.exception.DownloadTokenException;
import com.houjiang.plugin.artifact.identity.PluginIdentity;
import com.houjiang.plugin.artifact.identity.PluginIdentityResolver;
import com.houjiang.plugin.artifact.model.ArtifactStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.util.StreamUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * 制品下载接口（PaaS 内部 {@code /base/plugin-center/artifact/**}）。
 *
 * <p>两个端点：
 * <ul>
 *   <li>{@code POST /base/plugin-center/artifact/download-url} —— 换票：授权判定 + 签发限时、限次、绑租户的令牌；</li>
 *   <li>{@code GET  /base/plugin-center/artifact/raw} —— 回源：令牌自证身份，流式输出，支持 Range 断点续传。</li>
 * </ul>
 *
 * <p><b>raw 为什么能进鉴权白名单</b>：请求里唯一的凭据就是令牌本身，
 * 令牌里已经绑定了「制品 + 站点 + 10 分钟 + 5 次命中」，会话对它没有额外价值，
 * 而插件引擎（非浏览器）拿不到商城的登录 Cookie/Token，只能走令牌。
 *
 * <p><b>{@code ?aid=} 为什么不可直接用</b>：{@code hou_market_plugin_version.jar_url} 里存的是
 * {@code /base/plugin-center/artifact/raw?aid={artifactId}}，那是<b>规范指针</b>不是可直接访问的链接——
 * 缺令牌时本端点一律回 401/3001，必须先走 download-url 换票。
 *
 * @author Amos
 * @since M2
 */
@Slf4j
@Controller
@RequestMapping("/base/plugin-center/artifact")
public class PluginArtifactController {

    /**
     * 制品 MIME。
     */
    private static final String CONTENT_TYPE_JAR = "application/java-archive";

    private final PluginArtifactDownloadService downloadService;

    /**
     * 构造下载控制器。
     *
     * @param downloadService 下载服务
     */
    public PluginArtifactController(PluginArtifactDownloadService downloadService) {
        this.downloadService = Objects.requireNonNull(downloadService, "PluginArtifactDownloadService 不能为空");
    }

    /**
     * 换取下载链接。
     *
     * <p>入参<b>只认 {@code pluginCode} 与 {@code version}</b>：
     * {@code instanceId} / {@code userId} 一律以服务端登录态为准，请求体里传了一概丢弃
     * （并记 warn），否则任何人都能把自己的请求伪装成别人的站点去拉付费制品。
     *
     * @param params  请求体
     * @param request 原始请求（取 IP / UA 做泄露溯源）
     * @return 下载链接与制品指纹；未授权时 data 为 null
     */
    @PostMapping("/download-url")
    @ResponseBody
    public ResponseResult<Object> downloadUrl(@RequestBody(required = false) Map<String, Object> params,
                                              HttpServletRequest request) {
        PluginIdentity identity = PluginIdentityResolver.fromCurrentContext();
        if (identity == null) {
            return ResponseResult.fail(PluginDistErrorCode.NOT_LOGIN, "未登录或登录已过期");
        }
        rejectClientSuppliedIdentity(params);
        try {
            String kind = text(params, "artifactKind");
            DownloadUrlResult result = kind == null || kind.isBlank() || "jar".equals(kind)
                    ? downloadService.issueDownloadUrl(text(params, "pluginCode"), text(params, "version"), identity,
                        resolveClientIp(request), request == null ? null : request.getHeader(HttpHeaders.USER_AGENT))
                    : downloadService.issueDownloadUrl(text(params, "pluginCode"), text(params, "version"), kind, identity,
                        resolveClientIp(request), request == null ? null : request.getHeader(HttpHeaders.USER_AGENT));
            return ResponseResult.success(result);
        } catch (DownloadDeniedException e) {
            return ResponseResult.fail(e.getCode(), e.getMessage());
        } catch (IllegalStateException e) {
            log.error("签发下载链接失败（配置问题）: {}", e.getMessage());
            return ResponseResult.fail(500, "下载链接签发失败: " + e.getMessage());
        } catch (Exception e) {
            log.error("签发下载链接失败", e);
            return ResponseResult.fail(500, "下载链接签发失败: " + e.getMessage());
        }
    }

    /**
     * 回源下载：流式输出制品字节，支持 HTTP Range（206）。
     *
     * @param token       下载令牌
     * @param rangeHeader Range 请求头
     * @param response    原始响应（直接写字节，不经过消息转换器）
     */
    @GetMapping("/raw")
    public void raw(@RequestParam(value = "token", required = false) String token,
                    @RequestHeader(value = HttpHeaders.RANGE, required = false) String rangeHeader,
                    HttpServletResponse response) throws IOException {
        if (!StringUtils.hasText(token)) {
            writeJson(response, HttpStatus.UNAUTHORIZED, PluginDistErrorCode.NOT_LOGIN,
                    "下载链接缺少令牌，请先调用 /base/plugin-center/artifact/download-url 换取");
            return;
        }
        RawArtifact raw;
        try {
            raw = downloadService.openByToken(token, rangeHeader);
        } catch (DownloadTokenException e) {
            writeJson(response, HttpStatus.FORBIDDEN, e.getCode(), e.getMessage());
            return;
        } catch (DownloadDeniedException e) {
            writeJson(response, HttpStatus.NOT_FOUND, e.getCode(), e.getMessage());
            return;
        } catch (ArtifactNotFoundException e) {
            writeJson(response, HttpStatus.NOT_FOUND,
                    PluginDistErrorCode.ARTIFACT_NOT_FOUND, e.getMessage());
            return;
        } catch (ArtifactRangeNotSatisfiableException e) {
            response.setStatus(HttpStatus.REQUESTED_RANGE_NOT_SATISFIABLE.value());
            response.setHeader(HttpHeaders.CONTENT_RANGE, "bytes */" + e.getTotalSize());
            return;
        }

        ArtifactStream stream = raw.getStream();
        try (stream) {
            response.setStatus(stream.isRanged()
                    ? HttpStatus.PARTIAL_CONTENT.value()
                    : HttpStatus.OK.value());
            response.setContentType(raw.getFileName().endsWith(".jar") ? CONTENT_TYPE_JAR : MediaType.APPLICATION_OCTET_STREAM_VALUE);
            response.setHeader(HttpHeaders.CONTENT_LENGTH, String.valueOf(stream.getSize()));
            response.setHeader(HttpHeaders.ACCEPT_RANGES, "bytes");
            response.setHeader(HttpHeaders.CONTENT_DISPOSITION,
                    "attachment; filename=\"" + raw.getFileName() + "\"");
            // 制品链接是短时效且带租户身份的，绝不能被中间缓存共享
            response.setHeader(HttpHeaders.CACHE_CONTROL, "private, no-store, max-age=0");
            response.setHeader("X-Artifact-Id", nullToEmpty(raw.getArtifactId()));
            response.setHeader("X-Artifact-Md5", nullToEmpty(raw.getMd5()));
            response.setHeader("X-Artifact-Sha256", nullToEmpty(raw.getSha256()));
            response.setHeader("X-Artifact-Size", String.valueOf(raw.getSizeBytes()));
            if (stream.isRanged()) {
                response.setHeader(HttpHeaders.CONTENT_RANGE, stream.toContentRange());
            }
            long sent=stream.getStream().transferTo(response.getOutputStream());
            response.flushBuffer();
            if(sent==stream.getSize())raw.transferred();
        }
    }

    /**
     * 请求体里出现身份字段一律丢弃并告警：说明有调用方（或攻击者）在尝试自带站点身份。
     *
     * @param params 请求体
     */
    private static void rejectClientSuppliedIdentity(Map<String, Object> params) {
        if (params == null) {
            return;
        }
        if (params.containsKey("instanceId") || params.containsKey("userId")) {
            log.warn("下载换票请求携带了客户端身份字段，已忽略并以登录态为准");
        }
    }

    /**
     * 取客户端 IP：优先 X-Forwarded-For（经过 nginx），再退回 TCP 对端地址。
     *
     * @param request 原始请求，可为 null
     * @return 客户端 IP，可能为 null
     */
    private static String resolveClientIp(HttpServletRequest request) {
        if (request == null) {
            return null;
        }
        String forwarded = request.getHeader("X-Forwarded-For");
        if (StringUtils.hasText(forwarded)) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    /**
     * 取请求体里的文本字段。
     *
     * @param params 请求体
     * @param key    字段名
     * @return 文本值，缺失时返回 null
     */
    private static String text(Map<String, Object> params, String key) {
        if (params == null) {
            return null;
        }
        Object value = params.get(key);
        return value == null ? null : value.toString();
    }

    private static String nullToEmpty(String value) {
        return value == null ? "" : value;
    }

    /**
     * 写 JSON 错误响应（回源是二进制端点，失败时不能用消息转换器，直接写字节）。
     *
     * @param response 响应
     * @param status   HTTP 状态
     * @param code     插件分发错误码
     * @param message  描述
     * @throws IOException 写失败
     */
    private static void writeJson(HttpServletResponse response, HttpStatus status, int code, String message)
            throws IOException {
        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.getWriter().write("{\"code\":" + code
                + ",\"message\":\"" + escapeJson(message == null ? "" : message)
                + "\",\"data\":null}");
        response.getWriter().flush();
    }

    /**
     * JSON 字符串转义（只处理必须转义的几类字符，够用且无三方依赖）。
     *
     * @param value 原始文本
     * @return 转义后的文本
     */
    private static String escapeJson(String value) {
        StringBuilder sb = new StringBuilder(value.length() + 16);
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            switch (c) {
                case '"' -> sb.append("\\\"");
                case '\\' -> sb.append("\\\\");
                case '\n' -> sb.append("\\n");
                case '\r' -> sb.append("\\r");
                case '\t' -> sb.append("\\t");
                default -> {
                    if (c < 0x20) {
                        sb.append(String.format("\\u%04x", (int) c));
                    } else {
                        sb.append(c);
                    }
                }
            }
        }
        return sb.toString();
    }
}
