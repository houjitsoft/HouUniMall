package com.houjiang.bffweb.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.houjiang.bffweb.config.BugFeedbackUploadFilter;
import com.houjiang.bffweb.controller.WebBugFeedbackController;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.http.*;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.anything;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.headerDoesNotExist;
import static org.springframework.test.web.client.response.MockRestResponseCreators.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class BugFeedbackRelayTest {
    private BugFeedbackRelayService relay;
    private MockRestServiceServer central;
    private MockMvc mvc;
    private ValueOperations<String, String> values;
    private static final String PAYLOAD = "{\"sourceSite\":\"https://forged.test\",\"errorContent\":\"错误\",\"token\":\"secret-token\"}";
    private static final String RECEIPT = "{\"code\":1,\"data\":{\"id\":\"receipt-1\"}}";
    private static final String ID = "12345678-1234-1234-1234-123456789012";

    @BeforeEach @SuppressWarnings("unchecked") void setup() {
        StringRedisTemplate redis = mock(StringRedisTemplate.class);
        values = mock(ValueOperations.class);
        when(redis.opsForValue()).thenReturn(values);
        when(values.get("hou-user-token-user_active-token")).thenReturn("{\"userId\":1}");
        relay = new BugFeedbackRelayService(redis, new ObjectMapper());
        central = MockRestServiceServer.bindTo((RestTemplate) ReflectionTestUtils.getField(relay, "http")).build();
        mvc = MockMvcBuilders.standaloneSetup(new WebBugFeedbackController(relay)).build();
    }

    private MockMultipartFile payload() {
        return new MockMultipartFile("payload", "", "application/json", PAYLOAD.getBytes(StandardCharsets.UTF_8));
    }

    @Test void unsignedAndExpiredSessionsAreUnauthorized() throws Exception {
        mvc.perform(multipart("/web/bug-feedback/submit").file(payload())).andExpect(status().isUnauthorized());
        mvc.perform(multipart("/web/bug-feedback/submit").file(payload()).header("Authorization", "Bearer expired"))
                .andExpect(status().isUnauthorized());
        central.verify();
    }

    @Test void redisFailureDoesNotUseStaleSession() throws Exception {
        when(values.get(anyString())).thenThrow(new IllegalStateException("redis-password"));
        mvc.perform(multipart("/web/bug-feedback/submit").file(payload()).header("token", "active-token"))
                .andExpect(status().isServiceUnavailable()).andExpect(content().string(org.hamcrest.Matchers.not(
                        org.hamcrest.Matchers.containsString("redis-password"))));
    }

    @Test void submitDropsCredentialsAndReplacesPayloadOrigin() throws Exception {
        central.expect(requestTo("https://unimall.houjit.com/web/platform/bug-feedback/report"))
                .andExpect(method(HttpMethod.POST)).andExpect(headerDoesNotExist("Authorization"))
                .andExpect(headerDoesNotExist("token")).andExpect(headerDoesNotExist("Cookie"))
                .andExpect(headerDoesNotExist("X-Bug-Client-IP"))
                .andExpect(org.springframework.test.web.client.match.MockRestRequestMatchers.content().string(org.hamcrest.Matchers.allOf(
                        org.hamcrest.Matchers.containsString("http://localhost:80"),
                        org.hamcrest.Matchers.not(org.hamcrest.Matchers.containsString("forged.test")),
                        org.hamcrest.Matchers.not(org.hamcrest.Matchers.containsString("secret-token")))))
                .andRespond(withSuccess(RECEIPT, MediaType.APPLICATION_JSON));
        mvc.perform(multipart("/web/bug-feedback/submit").file(payload()).header("Authorization", "Bearer active-token")
                .header("Cookie", "session=private").header("Origin", "https://evil.test"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.code").value(1)).andExpect(jsonPath("$.data.id").value("receipt-1"));
        central.verify();
    }

    @Test void platformHttpFailureIsNotSuccessAndDoesNotLeakBody() throws Exception {
        central.expect(anything()).andRespond(withServerError().body("private-stack-and-token"));
        mvc.perform(multipart("/web/bug-feedback/submit").file(payload()).header("token", "active-token"))
                .andExpect(status().isBadGateway()).andExpect(jsonPath("$.code").value(502))
                .andExpect(content().string(org.hamcrest.Matchers.not(org.hamcrest.Matchers.containsString("private-stack"))));
        central.verify();
    }

    @Test void missingReceiptIdIsFailure() throws Exception {
        central.expect(anything()).andRespond(withSuccess("{\"code\":1,\"data\":{}}", MediaType.APPLICATION_JSON));
        mvc.perform(multipart("/web/bug-feedback/submit").file(payload()).header("token", "active-token"))
                .andExpect(status().isBadGateway());
    }

    @Test void businessFailureIsNotSuccess() throws Exception {
        central.expect(anything()).andRespond(withSuccess("{\"code\":0,\"data\":{\"id\":\"fake\"}}", MediaType.APPLICATION_JSON));
        mvc.perform(multipart("/web/bug-feedback/submit").file(payload()).header("token", "active-token"))
                .andExpect(status().isBadGateway());
    }

    @Test void redirectIsNotSuccess() throws Exception {
        central.expect(anything()).andRespond(withStatus(HttpStatus.FOUND).header("Location", "https://evil.test"));
        mvc.perform(multipart("/web/bug-feedback/submit").file(payload()).header("token", "active-token"))
                .andExpect(status().isBadGateway());
        central.verify();
    }

    @Test void excessiveFileSizeCountAndTotalAreRejected() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("token", "active-token");
        MultipartFileStub big = new MultipartFileStub(4L * 1024 * 1024 + 1);
        assertEquals(413, assertThrows(ResponseStatusException.class,
                () -> relay.submit(PAYLOAD, List.of(big.file), request)).getStatusCode().value());
        var small = new MockMultipartFile("files", "a.png", "image/png", new byte[]{1});
        assertEquals(413, assertThrows(ResponseStatusException.class,
                () -> relay.submit(PAYLOAD, List.of(small, small, small, small, small), request)).getStatusCode().value());
        var four = new MultipartFileStub(4L * 1024 * 1024).file;
        assertEquals(413, assertThrows(ResponseStatusException.class,
                () -> relay.submit(PAYLOAD, List.of(four, four, four, small), request)).getStatusCode().value());
        central.verify();
    }

    private static class MultipartFileStub {
        final org.springframework.web.multipart.MultipartFile file = mock(org.springframework.web.multipart.MultipartFile.class);
        MultipartFileStub(long size) { when(file.getSize()).thenReturn(size); }
    }

    @Test void platformRoutesAreNotExposed() throws Exception {
        mvc.perform(multipart("/web/platform/bug-feedback/report").file(payload())).andExpect(status().isNotFound());
        mvc.perform(post("/web/platform/bug-feedback/page").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isNotFound());
        mvc.perform(get("/web/platform/bug-feedback/" + ID)).andExpect(status().isNotFound());
        mvc.perform(post("/web/platform/bug-feedback/" + ID + "/status")
                .contentType(MediaType.APPLICATION_JSON).content("{}")) .andExpect(status().isNotFound());
        mvc.perform(get("/web/platform/bug-feedback/" + ID + "/attachments/0")).andExpect(status().isNotFound());
        central.verify();
    }

    @Test void centralUrlCannotBeHttpOrUnlistedOrContainPath() {
        MockHttpServletRequest request = new MockHttpServletRequest(); request.addHeader("token", "active-token");
        for (String url : List.of("http://unimall.houjit.com", "https://evil.test", "https://unimall.houjit.com/other",
                "https://unimall.houjit.com/?url=x", "https://user@unimall.houjit.com")) {
            ReflectionTestUtils.setField(relay, "centralUrl", url);
            assertEquals(503, assertThrows(ResponseStatusException.class,
                    () -> relay.submit(PAYLOAD, List.of(), request)).getStatusCode().value());
        }
        central.verify();
    }

    private MockHttpServletRequest raw(String body) {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/web/bug-feedback/submit");
        request.addHeader("token", "active-token");
        request.setContentType("multipart/form-data; boundary=test-boundary");
        request.setContent(body.getBytes(StandardCharsets.UTF_8)); return request;
    }

    @Test void uploadFilterParsesBoundedPayloadWithoutContainerMultipart() throws Exception {
        var request = raw("--test-boundary\r\nContent-Disposition: form-data; name=\"payload\"\r\n\r\n"
                + PAYLOAD + "\r\n--test-boundary--\r\n");
        AtomicBoolean called = new AtomicBoolean();
        new BugFeedbackUploadFilter(relay).doFilter(request, new MockHttpServletResponse(), (parsed, response) -> {
            called.set(true);
            assertEquals(PAYLOAD, parsed.getParameter("payload"));
            assertInstanceOf(org.springframework.web.multipart.MultipartHttpServletRequest.class, parsed);
        });
        assertTrue(called.get());
    }

    @Test void uploadFilterAndControllerWorkTogether() throws Exception {
        central.expect(requestTo("https://unimall.houjit.com/web/platform/bug-feedback/report"))
                .andRespond(withSuccess(RECEIPT, MediaType.APPLICATION_JSON));
        MockMvc filtered = MockMvcBuilders.standaloneSetup(new WebBugFeedbackController(relay))
                .addFilters(new BugFeedbackUploadFilter(relay)).build();
        filtered.perform(post("/web/bug-feedback/submit").header("token", "active-token")
                .contentType("multipart/form-data; boundary=test-boundary")
                .content("--test-boundary\r\nContent-Disposition: form-data; name=\"payload\"\r\n\r\n"
                        + PAYLOAD + "\r\n--test-boundary--\r\n"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.data.id").value("receipt-1"));
        central.verify();
    }

    @Test void uploadFilterRejectsUnauthenticatedBeforeParsing() throws Exception {
        var request = raw("malformed"); request.removeHeader("token");
        MockHttpServletResponse response = new MockHttpServletResponse();
        new BugFeedbackUploadFilter(relay).doFilter(request, response, (a, b) -> fail("不得放行"));
        assertEquals(401, response.getStatus());
    }

    @Test void chunkedOversizeIsRejectedWhileReading() throws Exception {
        var request = new MockHttpServletRequest("POST", "/web/bug-feedback/submit") {
            @Override public int getContentLength() { return -1; }
            @Override public long getContentLengthLong() { return -1; }
            { setContentType("multipart/form-data; boundary=test-boundary"); addHeader("token", "active-token"); }
        };
        request.setContent(("--test-boundary\r\nContent-Disposition: form-data; name=\"files\"; filename=\"x\"\r\n\r\n"
                + "x".repeat(4 * 1024 * 1024 + 1) + "\r\n--test-boundary--\r\n").getBytes(StandardCharsets.UTF_8));
        MockHttpServletResponse response = new MockHttpServletResponse();
        new BugFeedbackUploadFilter(relay).doFilter(request, response, (a, b) -> fail("不得放行超限流"));
        assertEquals(413, response.getStatus());
    }
}
