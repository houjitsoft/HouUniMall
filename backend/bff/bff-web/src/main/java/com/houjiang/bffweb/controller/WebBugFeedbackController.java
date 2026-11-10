package com.houjiang.bffweb.controller;

import com.houjiang.bffweb.service.BugFeedbackRelayService;
import com.houjiang.commons.annnotation.NoSysLog;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;

@RestController
@NoSysLog
public class WebBugFeedbackController {
    private final BugFeedbackRelayService relay;

    public WebBugFeedbackController(BugFeedbackRelayService relay) { this.relay = relay; }

    @PostMapping(value = "/web/bug-feedback/submit", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<byte[]> submit(@RequestPart(value = "payload", required = false) String payload,
                                         @RequestPart(value = "files", required = false) List<MultipartFile> files,
                                         HttpServletRequest request) {
        return relay.submit(payload == null ? request.getParameter("payload") : payload, files, request);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> failure(Exception failure) {
        int status = failure instanceof ResponseStatusException expected ? expected.getStatusCode().value()
                : failure instanceof MaxUploadSizeExceededException ? 413
                : failure instanceof org.springframework.web.bind.ServletRequestBindingException
                || failure instanceof org.springframework.http.converter.HttpMessageNotReadableException
                || failure instanceof org.springframework.web.method.annotation.MethodArgumentTypeMismatchException
                || failure instanceof org.springframework.web.multipart.MultipartException ? 400 : 503;
        return ResponseEntity.status(status).cacheControl(CacheControl.noStore().cachePrivate())
                .body(Map.of("code", status, "message", BugFeedbackRelayService.error(status).getReason()));
    }
}
