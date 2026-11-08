package com.houjiang.bffweb.controller.plugin;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.mock.http.client.MockClientHttpRequest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.mock.web.MockMultipartHttpServletRequest;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

class PluginPublishProxyControllerTest {

    private static final String BASE_URL = "http://base-test";

    @Test
    void uploadRebuildsParsedMultipartRequestWithFileAndFormFields() {
        byte[] jarBytes = "PK-fake-plugin-jar".getBytes(StandardCharsets.UTF_8);
        RestTemplate restTemplate = PluginPublishProxyController.createRestTemplate();
        MockRestServiceServer server = MockRestServiceServer.bindTo(restTemplate).build();
        server.expect(requestTo(BASE_URL + "/base/plugin-center/publish/upload"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(request -> {
                    MediaType contentType = request.getHeaders().getContentType();
                    assertTrue(contentType != null && MediaType.MULTIPART_FORM_DATA.includes(contentType));
                    byte[] body = ((MockClientHttpRequest) request).getBodyAsBytes();
                    String multipart = new String(body, StandardCharsets.ISO_8859_1);
                    assertTrue(multipart.contains("name=\"file\""), "文件 part 不能在 BFF 转发时丢失");
                    assertTrue(multipart.contains("filename=\"mall-advertising.jar\""));
                    assertTrue(multipart.contains("name=\"pluginCode\""));
                    assertTrue(multipart.contains("mall-advertising"));
                    assertTrue(multipart.contains("name=\"version\""));
                    assertTrue(multipart.contains("1.0.0"));
                    int jarOffset = indexOf(body, jarBytes);
                    assertTrue(jarOffset >= 0, "JAR 字节必须完整转发");
                    assertArrayEquals(jarBytes,
                            java.util.Arrays.copyOfRange(body, jarOffset, jarOffset + jarBytes.length));
                })
                .andRespond(withSuccess("{\"code\":1,\"message\":\"ok\",\"data\":{\"versionId\":7}}",
                        MediaType.APPLICATION_JSON));

        PluginPublishProxyController controller =
                new PluginPublishProxyController(BASE_URL, restTemplate);
        MockMultipartHttpServletRequest request = new MockMultipartHttpServletRequest();
        request.setMethod("POST");
        request.setRequestURI("/web/plugin-center/publish/upload");
        request.addHeader("Authorization", "Bearer release-token");
        request.addFile(new MockMultipartFile(
                "file", "mall-advertising.jar", "application/java-archive", jarBytes));
        request.addParameter("pluginCode", "mall-advertising");
        request.addParameter("version", "1.0.0");
        request.addParameter("uploadId", "upload-1");

        var response = controller.forward(request);

        server.verify();
        assertEquals(200, response.getStatusCode().value());
    }

    private static int indexOf(byte[] source, byte[] target) {
        outer:
        for (int i = 0; i <= source.length - target.length; i++) {
            for (int j = 0; j < target.length; j++) {
                if (source[i + j] != target[j]) {
                    continue outer;
                }
            }
            return i;
        }
        return -1;
    }
}
