package com.houjiang.bffweb.controller;

import com.houjiang.bffweb.feign.BaseServiceFeign;
import com.houjiang.bffweb.support.BasePluginCenterUrlResolver;
import com.houjiang.bffweb.util.ResponseResult;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.util.StreamUtils;
import org.springframework.web.client.RestTemplate;

@RestController
@RequestMapping("/web/open-download")
public class WebOfficialDownloadController {
    private final BaseServiceFeign base;
    private final BasePluginCenterUrlResolver resolver;
    private final RestTemplate restTemplate = new RestTemplate();

    public WebOfficialDownloadController(BaseServiceFeign base, BasePluginCenterUrlResolver resolver) {
        this.base = base;
        this.resolver = resolver;
    }

    @PostMapping("/sms/send")
    public ResponseResult send(@RequestBody Map<String, Object> request) {
        return base.officialDownloadSendSms(request);
    }

    @GetMapping("/artifacts")
    public ResponseResult artifacts() { return base.officialDownloadArtifacts(); }

    @PostMapping("/sms/verify")
    public ResponseResult verify(@RequestBody Map<String, Object> request) {
        return base.officialDownloadVerifySms(request);
    }

    @PostMapping("/ticket")
    public ResponseResult ticket(@RequestBody Map<String, Object> request) {
        return base.officialDownloadTicket(request);
    }

    @GetMapping("/file")
    public void file(@RequestParam("ticket") String ticket, jakarta.servlet.http.HttpServletRequest request,
                     HttpServletResponse response) throws IOException {
        String target = resolver.resolve().url() + "/base/official-download/file?ticket=" +
                java.net.URLEncoder.encode(ticket, java.nio.charset.StandardCharsets.UTF_8);
        try {
            restTemplate.execute(target, HttpMethod.GET, outgoing -> {
                String forwarded = request.getHeader("X-Forwarded-For");
                outgoing.getHeaders().set("X-Forwarded-For", forwarded == null ? request.getRemoteAddr() : forwarded);
                String ua = request.getHeader(HttpHeaders.USER_AGENT);
                if (ua != null) outgoing.getHeaders().set(HttpHeaders.USER_AGENT, ua);
            }, upstream -> {
                response.setStatus(upstream.getStatusCode().value());
                for (String name : new String[]{HttpHeaders.CONTENT_TYPE, HttpHeaders.CONTENT_LENGTH,
                        HttpHeaders.CONTENT_DISPOSITION, HttpHeaders.CACHE_CONTROL, "X-Artifact-Sha256"}) {
                    String value = upstream.getHeaders().getFirst(name);
                    if (value != null) response.setHeader(name, value);
                }
                try (InputStream in = upstream.getBody()) {
                    if (in != null) StreamUtils.copy(in, response.getOutputStream());
                }
                response.flushBuffer();
                return null;
            });
        } catch (Exception e) {
            if (!response.isCommitted()) response.sendError(502, "官方下载服务暂时不可用");
        }
    }
}
