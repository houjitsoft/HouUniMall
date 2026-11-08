package com.houjiang.base.officialdownload;

import com.houjiang.commons.util.ResponseResult;
import com.houjiang.plugin.artifact.model.ArtifactStream;
import com.houjiang.plugin.artifact.spi.ArtifactStorage;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.util.LinkedHashMap;
import java.util.Map;
import java.security.MessageDigest;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/base/official-download")
@ConditionalOnProperty(name = "unimall.official-download.enabled", havingValue = "true")
public class OfficialDownloadController {
    private final OfficialDownloadService service;
    private final JdbcOfficialDownloadStore store;
    private final DownloadSecurity security;
    private final ArtifactStorage storage;
    private final OfficialDownloadProperties properties;
    private final Clock clock = Clock.systemUTC();

    public OfficialDownloadController(OfficialDownloadService service, JdbcOfficialDownloadStore store,
                                      DownloadSecurity security, ArtifactStorage storage,
                                      OfficialDownloadProperties properties) {
        this.service = service;
        this.store = store;
        this.security = security;
        this.storage = storage;
        this.properties = properties;
    }

    @PostMapping("/admin/artifacts")
    public ResponseResult<Object> registerArtifact(@RequestBody Map<String, Object> body,
                                                   @org.springframework.web.bind.annotation.RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorization) {
        String expected = properties.getReleaseApiToken();
        String provided = authorization != null && authorization.startsWith("Bearer ") ? authorization.substring(7) : "";
        if (expected == null || expected.length() < 32 || !MessageDigest.isEqual(
                expected.getBytes(StandardCharsets.UTF_8), provided.getBytes(StandardCharsets.UTF_8))) {
            return ResponseResult.fail(403, "无权登记发行包");
        }
        String product = text(body, "product");
        String version = text(body, "version");
        String fileName = text(body, "fileName");
        String objectKey = text(body, "objectKey");
        String sha256 = text(body, "sha256");
        if (isBlank(product) || isBlank(version) || isBlank(fileName) || isBlank(objectKey)
                || sha256 == null || !sha256.matches("^[a-fA-F0-9]{64}$")
                || objectKey.contains("..") || objectKey.startsWith("/") || !objectKey.startsWith("open-releases/")) {
            return ResponseResult.fail(400, "发行包元数据不合法");
        }
        store.registerArtifact(product.trim(), version.trim(), text(body, "gitTag"), text(body, "gitCommit"),
                fileName.trim(), objectKey.trim(), sha256.toLowerCase(java.util.Locale.ROOT));
        return ResponseResult.success();
    }

    @PostMapping("/sms/send")
    public ResponseResult<Object> send(@RequestBody Map<String, Object> body, HttpServletRequest request) {
        try {
            service.requestCode(text(body, "phone"), clientIp(request));
            return ResponseResult.success(Map.of("cooldownSeconds", 60));
        } catch (DownloadFlowException e) {
            return failure(e);
        }
    }

    @GetMapping("/artifacts")
    public ResponseResult<Object> artifacts() {
        return ResponseResult.success(store.listPublishedArtifacts());
    }

    @PostMapping("/sms/verify")
    public ResponseResult<Object> verify(@RequestBody Map<String, Object> body, HttpServletRequest request) {
        try {
            VerifiedSession session = service.verifyCode(new VerifyCodeCommand(text(body, "phone"), text(body, "code"),
                    text(body, "identityType"), text(body, "companyName"), text(body, "useCase"), text(body, "source"),
                    text(body, "privacyVersion"), bool(body, "privacyAccepted"), bool(body, "marketingConsent"),
                    clientIp(request), request.getHeader(HttpHeaders.USER_AGENT)));
            return ResponseResult.success(Map.of("sessionToken", session.token(), "expiresAt", session.expiresAt().toString()));
        } catch (DownloadFlowException e) {
            return failure(e);
        }
    }

    @PostMapping("/ticket")
    public ResponseResult<Object> ticket(@RequestBody Map<String, Object> body, HttpServletRequest request) {
        try {
            DownloadTicket ticket = service.issueTicket(text(body, "sessionToken"), text(body, "product"),
                    text(body, "version"), clientIp(request));
            Map<String, Object> result = new LinkedHashMap<>();
            result.put("downloadUrl", "/web/open-download/file?ticket=" + URLEncoder.encode(ticket.token(), StandardCharsets.UTF_8));
            result.put("expiresAt", ticket.expiresAt().toString());
            result.put("remainingDownloads", ticket.remainingDownloads());
            return ResponseResult.success(result);
        } catch (DownloadFlowException e) {
            return failure(e);
        }
    }

    @GetMapping("/file")
    public void file(@RequestParam("ticket") String token, HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        String ipHash = security.hmac(clientIp(request) == null ? "" : clientIp(request));
        DownloadTicket ticket = store.consume(security.hmac(token), ipHash, clock.instant());
        if (ticket == null) {
            writeError(response, HttpStatus.FORBIDDEN, "下载链接无效、已过期或次数已用完");
            return;
        }
        DownloadArtifact artifact = store.findById(ticket.artifactId());
        if (artifact == null || !artifact.published()) {
            writeError(response, HttpStatus.NOT_FOUND, "发行包已下架");
            return;
        }
        try (ArtifactStream stream = storage.open(artifact.objectKey(), null, null)) {
            response.setStatus(HttpStatus.OK.value());
            response.setContentType(MediaType.APPLICATION_OCTET_STREAM_VALUE);
            response.setHeader(HttpHeaders.CONTENT_LENGTH, String.valueOf(stream.getSize()));
            response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename*=UTF-8''" +
                    URLEncoder.encode(artifact.fileName(), StandardCharsets.UTF_8).replace("+", "%20"));
            response.setHeader(HttpHeaders.CACHE_CONTROL, "private, no-store, max-age=0");
            response.setHeader("X-Artifact-Sha256", artifact.sha256());
            long sent = stream.getStream().transferTo(response.getOutputStream());
            response.flushBuffer();
            store.recordEvent(ticket.leadId(), artifact.id(), "DOWNLOAD", ipHash,
                    sent == stream.getSize() ? "COMPLETED" : "INTERRUPTED");
        } catch (IOException e) {
            store.recordEvent(ticket.leadId(), artifact.id(), "DOWNLOAD", ipHash, "FAILED");
            throw e;
        }
    }

    private static ResponseResult<Object> failure(DownloadFlowException e) {
        return ResponseResult.fail(400, e.getMessage(), Map.of("error", e.code()));
    }
    private static String text(Map<String, Object> body, String key) { Object v = body == null ? null : body.get(key); return v == null ? null : String.valueOf(v); }
    private static boolean bool(Map<String, Object> body, String key) { return body != null && Boolean.parseBoolean(String.valueOf(body.get(key))); }
    private static boolean isBlank(String value) { return value == null || value.isBlank(); }
    private static String clientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        return forwarded == null || forwarded.isBlank() ? request.getRemoteAddr() : forwarded.split(",")[0].trim();
    }
    private static void writeError(HttpServletResponse response, HttpStatus status, String message) throws IOException {
        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.getWriter().write("{\"code\":400,\"message\":\"" + message + "\"}");
    }
}
