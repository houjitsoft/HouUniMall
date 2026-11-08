package com.houjiang.plugin.artifact.controller;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.houjiang.commons.constant.PluginDistErrorCode;
import com.houjiang.commons.util.ResponseResult;
import com.houjiang.commons.vo.user.UserInfoVO;
import com.houjiang.plugin.artifact.download.DownloadUrlResult;
import com.houjiang.plugin.artifact.download.PluginArtifactDownloadService;
import com.houjiang.plugin.artifact.download.RawArtifact;
import com.houjiang.plugin.artifact.exception.ArtifactRangeNotSatisfiableException;
import com.houjiang.plugin.artifact.exception.DownloadDeniedException;
import com.houjiang.plugin.artifact.exception.DownloadTokenException;
import com.houjiang.plugin.artifact.identity.PluginIdentity;
import com.houjiang.plugin.artifact.model.ArtifactStream;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * 制品下载端点单测：回源响应头 / Range 206 / 416 / 令牌失效，以及未授权时 data 为 null。
 *
 * <p>登录态通过真实 {@link SecurityContextHolder} 注入（而不是 mock 静态工具），
 * 这样连「SecurityContextUtils 能不能从 principal 取出 UserInfoVO」也一并验证。
 *
 * @author Amos
 * @since M2
 */
class PluginArtifactControllerTest {

    private PluginArtifactDownloadService downloadService;

    private PluginArtifactController controller;

    @BeforeEach
    void setUp() {
        downloadService = mock(PluginArtifactDownloadService.class);
        controller = new PluginArtifactController(downloadService);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void rawWritesHeadersAndStreamsBytes() throws Exception {
        byte[] jar = "PKfake-jar-content".getBytes(StandardCharsets.UTF_8);
        when(downloadService.openByToken(anyString(), any())).thenReturn(
                rawArtifact(artifactStream(jar, 0L, jar.length - 1L, jar.length)));

        MockHttpServletResponse response = new MockHttpServletResponse();
        controller.raw("token-1", null, response);

        assertEquals(200, response.getStatus());
        assertEquals("application/java-archive", response.getContentType());
        assertEquals(String.valueOf(jar.length), response.getHeader("Content-Length"));
        assertEquals("bytes", response.getHeader("Accept-Ranges"));
        assertEquals("attachment; filename=\"coupon-demo-1.0.0.jar\"",
                response.getHeader("Content-Disposition"));
        assertEquals("private, no-store, max-age=0", response.getHeader("Cache-Control"));
        assertEquals("md5-1", response.getHeader("X-Artifact-Md5"));
        assertEquals("sha256-1", response.getHeader("X-Artifact-Sha256"));
        assertEquals("100", response.getHeader("X-Artifact-Size"));
        assertNull(response.getHeader("Content-Range"), "非区间响应不应带 Content-Range");
        assertArrayEquals(jar, response.getContentAsByteArray());
    }

    @Test
    void rawSupportsRangeAndReturns206() throws Exception {
        byte[] slice = "2345".getBytes(StandardCharsets.UTF_8);
        when(downloadService.openByToken(eq("token-1"), eq("bytes=2-5"))).thenReturn(
                rawArtifact(artifactStream(slice, 2L, 5L, 10L)));

        MockHttpServletResponse response = new MockHttpServletResponse();
        controller.raw("token-1", "bytes=2-5", response);

        assertEquals(206, response.getStatus(), "断点续传必须回 206，否则客户端会当成全量重下");
        assertEquals("bytes 2-5/10", response.getHeader("Content-Range"));
        assertEquals("4", response.getHeader("Content-Length"));
        assertArrayEquals(slice, response.getContentAsByteArray());
    }

    @Test
    void rawReturns416WhenRangeUnsatisfiable() throws Exception {
        when(downloadService.openByToken(anyString(), any()))
                .thenThrow(new ArtifactRangeNotSatisfiableException("起点超出制品长度", 100L));

        MockHttpServletResponse response = new MockHttpServletResponse();
        controller.raw("token-1", "bytes=5000-6000", response);

        assertEquals(416, response.getStatus());
        assertEquals("bytes */100", response.getHeader("Content-Range"));
    }

    @Test
    void rawRejectsMissingTokenWithNotLogin() throws Exception {
        MockHttpServletResponse response = new MockHttpServletResponse();
        controller.raw(null, null, response);

        assertEquals(401, response.getStatus());
        assertTrue(response.getContentAsString().contains("\"code\":" + PluginDistErrorCode.NOT_LOGIN),
                "jar_url 里的 ?aid= 不是可直接访问的链接，缺令牌一律要先换票");
    }

    @Test
    void rawRejectsExpiredTokenWithDistCode() throws Exception {
        when(downloadService.openByToken(anyString(), any())).thenThrow(
                new DownloadTokenException(PluginDistErrorCode.DOWNLOAD_TOKEN_EXPIRED, "下载链接已过期"));

        MockHttpServletResponse response = new MockHttpServletResponse();
        controller.raw("expired", null, response);

        assertEquals(403, response.getStatus());
        assertTrue(response.getContentAsString()
                .contains("\"code\":" + PluginDistErrorCode.DOWNLOAD_TOKEN_EXPIRED));
    }

    @Test
    void downloadUrlReturnsIssuedLink() {
        givenLogin();
        when(downloadService.issueDownloadUrl(anyString(), anyString(), any(), any(), any()))
                .thenReturn(new DownloadUrlResult("/base/plugin-center/artifact/raw?token=abc", "abc", "aid-1",
                        "coupon-demo", "1.0.0", "md5-1", "sha256-1", 100L,
                        "coupon-demo-1.0.0.jar", System.currentTimeMillis() + 600_000L, 5));

        ResponseResult<Object> result = controller.downloadUrl(
                Map.of("pluginCode", "coupon-demo", "version", "1.0.0"), null);

        assertEquals(1, result.getCode(), "ResponseResult.success 的成功码是 CommonStatusEnum.SUCCESS=1");
        assertEquals("coupon-demo",
                ((DownloadUrlResult) result.getData()).getPluginCode());
    }

    @Test
    void downloadUrlKeepsDataNullWhenDenied() {
        givenLogin();
        when(downloadService.issueDownloadUrl(anyString(), anyString(), any(), any(), any()))
                .thenThrow(new DownloadDeniedException(PluginDistErrorCode.NOT_PURCHASED, "未购买"));

        ResponseResult<Object> result = controller.downloadUrl(
                Map.of("pluginCode", "coupon-demo", "version", "1.0.0"), null);

        assertEquals(PluginDistErrorCode.NOT_PURCHASED, result.getCode());
        assertNull(result.getData(), "未授权时不回传任何制品信息，连 md5 都不给");
    }

    @Test
    void downloadUrlRejectsAnonymousCaller() {
        SecurityContextHolder.clearContext();

        ResponseResult<Object> result = controller.downloadUrl(
                Map.of("pluginCode", "coupon-demo", "version", "1.0.0"), null);

        assertEquals(PluginDistErrorCode.NOT_LOGIN, result.getCode());
        assertNull(result.getData());
    }

    @Test
    void downloadUrlIgnoresClientSuppliedInstanceId() {
        givenLogin();
        when(downloadService.issueDownloadUrl(anyString(), anyString(), any(), any(), any()))
                .thenThrow(new DownloadDeniedException(PluginDistErrorCode.NOT_PURCHASED, "未购买"));

        Map<String, Object> params = new LinkedHashMap<>();
        params.put("pluginCode", "coupon-demo");
        params.put("version", "1.0.0");
        params.put("instanceId", "attacker-org");
        params.put("userId", "999");
        controller.downloadUrl(params, null);

        ArgumentCaptor<PluginIdentity> captor = ArgumentCaptor.forClass(PluginIdentity.class);
        verify(downloadService).issueDownloadUrl(eq("coupon-demo"), eq("1.0.0"),
                captor.capture(), any(), any());
        assertEquals("org-1", captor.getValue().getInstanceId());
        assertNotEquals("attacker-org", captor.getValue().getInstanceId(),
                "请求体里的 instanceId 一律以登录态覆盖");
        assertEquals(42L, captor.getValue().getUserId());
    }

    @Test
    void commercialRawReceiptsRunOnlyAfterCompleteTransferAndFlush() throws Exception {
        for (String kind : java.util.List.of("source", "oci")) {
            for (boolean ranged : java.util.List.of(false, true)) {
                Runnable receipt = mock(Runnable.class);
                byte[] bytes = new byte[ranged ? 4 : 10];
                ArtifactStream stream = artifactStream(bytes, ranged ? 2 : 0, ranged ? 5 : 9, 10);
                when(downloadService.openByToken(eq("ticket"), any())).thenReturn(
                        new RawArtifact(stream, "aid", "erp", "1.0.0", "md5", "sha", 10,
                                kind.equals("source") ? "erp-source.tar.gz" : "erp-runtime.tar", receipt));
                MockHttpServletResponse response = org.mockito.Mockito.spy(new MockHttpServletResponse());
                controller.raw("ticket", ranged ? "bytes=2-5" : null, response);
                assertEquals(ranged ? 206 : 200, response.getStatus());
                assertEquals("application/octet-stream", response.getContentType());
                var order = org.mockito.Mockito.inOrder(response, receipt);
                order.verify(response).flushBuffer();
                order.verify(receipt).run();
                verify(receipt, org.mockito.Mockito.times(1)).run();
            }
        }
    }

    @Test
    void shortReadDoesNotRecordReceipt() throws Exception {
        Runnable receipt = mock(Runnable.class);
        ArtifactStream stream = new ArtifactStream(new ByteArrayInputStream(new byte[3]), 10, 10, 0, 9, null);
        when(downloadService.openByToken(eq("ticket"), any())).thenReturn(
                new RawArtifact(stream, "aid", "erp", "1.0.0", "md5", "sha", 10, "erp-source.tar.gz", receipt));
        controller.raw("ticket", null, new MockHttpServletResponse());
        org.mockito.Mockito.verifyNoInteractions(receipt);
    }

    @Test
    void readWriteAndFlushFailuresDoNotRecordReceiptAndCloseStream() throws Exception {
        for (String failure : java.util.List.of("read", "write", "flush")) {
            Runnable receipt = mock(Runnable.class);
            Runnable closed = mock(Runnable.class);
            java.io.InputStream input = failure.equals("read") ? new java.io.InputStream() {
                @Override public int read() throws java.io.IOException { throw new java.io.IOException("读取失败"); }
            } : new ByteArrayInputStream(new byte[10]);
            ArtifactStream stream = new ArtifactStream(input, 10, 10, 0, 9, closed);
            when(downloadService.openByToken(eq("ticket"), any())).thenReturn(
                    new RawArtifact(stream, "aid", "erp", "1.0.0", "md5", "sha", 10, "erp-runtime.tar", receipt));
            MockHttpServletResponse response = org.mockito.Mockito.spy(new MockHttpServletResponse());
            if (failure.equals("write")) {
                jakarta.servlet.ServletOutputStream output = mock(jakarta.servlet.ServletOutputStream.class);
                org.mockito.Mockito.doThrow(new java.io.IOException("写入失败")).when(output).write(any(byte[].class), org.mockito.ArgumentMatchers.anyInt(), org.mockito.ArgumentMatchers.anyInt());
                org.mockito.Mockito.doReturn(output).when(response).getOutputStream();
            } else if (failure.equals("flush")) {
                org.mockito.Mockito.doThrow(new java.io.IOException("刷新失败")).when(response).flushBuffer();
            }
            org.junit.jupiter.api.Assertions.assertThrows(java.io.IOException.class, () -> controller.raw("ticket", null, response));
            org.mockito.Mockito.verifyNoInteractions(receipt);
            verify(closed).run();
        }
    }

    private static ArtifactStream artifactStream(byte[] bytes, long start, long end, long total) {
        return new ArtifactStream(new ByteArrayInputStream(bytes), bytes.length, total, start, end, null);
    }

    private static RawArtifact rawArtifact(ArtifactStream stream) {
        return new RawArtifact(stream, "aid-1", "coupon-demo", "1.0.0", "md5-1", "sha256-1", 100L);
    }

    private static void givenLogin() {
        UserInfoVO user = new UserInfoVO();
        user.setUserId("42");
        user.setOrgId("org-1");
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(user, null, Collections.emptyList()));
    }
}
