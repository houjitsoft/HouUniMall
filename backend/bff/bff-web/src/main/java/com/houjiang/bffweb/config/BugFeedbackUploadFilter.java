package com.houjiang.bffweb.config;

import com.houjiang.bffweb.service.BugFeedbackRelayService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.tomcat.util.http.fileupload.FileUpload;
import org.apache.tomcat.util.http.fileupload.UploadContext;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.support.DefaultMultipartHttpServletRequest;
import org.springframework.web.server.ResponseStatusException;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

/** 仅反馈入口流式限额，不改变站内其他上传接口的 multipart 配置。 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 30)
public class BugFeedbackUploadFilter extends OncePerRequestFilter {
    private final BugFeedbackRelayService relay;
    public BugFeedbackUploadFilter(BugFeedbackRelayService relay) { this.relay = relay; }

    @Override protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI().substring(request.getContextPath().length());
        return !"POST".equals(request.getMethod()) || !path.equals("/web/bug-feedback/submit");
    }

    @Override protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest parsed;
        try {
            relay.requireSession(request);
            if (request.getContentLengthLong() > 12L * 1024 * 1024) throw BugFeedbackRelayService.error(413);
            if (request.getContentType() == null || !MediaType.parseMediaType(request.getContentType())
                    .isCompatibleWith(MediaType.MULTIPART_FORM_DATA)) throw BugFeedbackRelayService.error(400);
            FileUpload upload = new FileUpload();
            upload.setSizeMax(12L * 1024 * 1024);
            upload.setFileSizeMax(4L * 1024 * 1024);
            var iterator = upload.getItemIterator(new UploadContext() {
                public long contentLength() { return request.getContentLengthLong(); }
                public String getCharacterEncoding() { return "UTF-8"; }
                public String getContentType() { return request.getContentType(); }
                public InputStream getInputStream() throws IOException { return request.getInputStream(); }
            });
            var files = new LinkedMultiValueMap<String, MultipartFile>();
            Map<String, String[]> parameters = new HashMap<>();
            Map<String, String> contentTypes = new HashMap<>();
            int fileCount = 0;
            while (iterator.hasNext()) {
                var item = iterator.next();
                if ("payload".equals(item.getFieldName())) {
                    if (parameters.containsKey("payload")) throw BugFeedbackRelayService.error(400);
                    byte[] bytes;
                    try (InputStream input = item.openStream()) { bytes = input.readNBytes(48001); }
                    if (bytes.length > 48000) throw BugFeedbackRelayService.error(400);
                    parameters.put("payload", new String[]{new String(bytes, StandardCharsets.UTF_8)});
                    contentTypes.put("payload", MediaType.APPLICATION_JSON_VALUE);
                } else if ("files".equals(item.getFieldName()) && !item.isFormField()) {
                    if (++fileCount > 4) throw BugFeedbackRelayService.error(413);
                    byte[] bytes;
                    try (InputStream input = item.openStream()) { bytes = input.readAllBytes(); }
                    if (bytes.length == 0) throw BugFeedbackRelayService.error(400);
                    files.add("files", new FeedbackFile(bytes));
                } else throw BugFeedbackRelayService.error(400);
            }
            if (!parameters.containsKey("payload")) throw BugFeedbackRelayService.error(400);
            parsed = new DefaultMultipartHttpServletRequest(request, files, parameters, contentTypes);
        } catch (Exception invalid) {
            int status = invalid instanceof ResponseStatusException expected ? expected.getStatusCode().value()
                    : invalid instanceof org.apache.tomcat.util.http.fileupload.impl.SizeException
                    || invalid instanceof org.apache.tomcat.util.http.fileupload.impl.FileUploadIOException ? 413 : 400;
            response.setStatus(status);
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.setCharacterEncoding("UTF-8");
            response.setHeader("Cache-Control", "private, no-store");
            response.getWriter().write("{\"code\":" + status + ",\"message\":\""
                    + BugFeedbackRelayService.error(status).getReason() + "\"}");
            return;
        }
        chain.doFilter(parsed, response);
    }

    private record FeedbackFile(byte[] bytes) implements MultipartFile {
        public String getName() { return "files"; }
        public String getOriginalFilename() { return "feedback-image"; }
        public String getContentType() { return MediaType.APPLICATION_OCTET_STREAM_VALUE; }
        public boolean isEmpty() { return bytes.length == 0; }
        public long getSize() { return bytes.length; }
        public byte[] getBytes() { return bytes; }
        public InputStream getInputStream() { return new ByteArrayInputStream(bytes); }
        public void transferTo(File destination) throws IOException { java.nio.file.Files.write(destination.toPath(), bytes); }
    }
}
