package com.houjiang.mall.service.market;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.Map;

/**
 * 跨宿主安装派发器：把「装插件」这件事交给插件声明的那个宿主进程去做。
 *
 * <p><b>为什么不能由 base 自己装</b>：嵌入式插件由声明了 {@code hostService} 的宿主进程内的 PF4J 引擎装载
 * （OCR 声明的是 tool，由 tool 进程的引擎扫描 {@code /app/plugins} 装载）。base 自己的引擎只认识
 * hostService=base 的插件，直接落盘到 base 的 plugins 目录既不会被 tool 扫描到，也污染了 base 的目录。
 * 因此 base 只能做编排：下载制品 → 校验 → 派发给宿主 → 登记安装态。
 *
 * <p>地址解析优先级：
 * <ol>
 *   <li>显式配置 {@code houcloud.plugin.host-endpoints.{host}}（运维兜底 / 非注册中心部署）；</li>
 *   <li>注册中心 {@link DiscoveryClient} 按服务名取健康实例（默认路径，与业务 Feign 调用同源）。</li>
 * </ol>
 *
 * <p>调用凭据：内部令牌 {@code X-Internal-Job-Token}，与 job / geo / oa 等内部链路同一套
 * {@code houcloud.plugin.internal-job-token}。未配置时 fail-closed。
 */
@Component
public class PluginHostInstallDispatcher {

    private static final Logger log = LoggerFactory.getLogger(PluginHostInstallDispatcher.class);

    /** 宿主地址配置前缀：houcloud.plugin.host-endpoints.tool=http://tool:8296 */
    private static final String ENDPOINT_PREFIX = "houcloud.plugin.host-endpoints.";

    private final Environment environment;
    private final DiscoveryClient discoveryClient;
    private final String internalJobToken;
    private final HttpClient httpClient;

    public PluginHostInstallDispatcher(Environment environment,
                                       ObjectProvider<DiscoveryClient> discoveryClientProvider,
                                       @Value("${houcloud.plugin.internal-job-token:}") String internalJobToken) {
        this.environment = environment;
        this.discoveryClient = discoveryClientProvider == null ? null : discoveryClientProvider.getIfAvailable();
        this.internalJobToken = internalJobToken == null ? "" : internalJobToken.trim();
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
    }

    /**
     * 派发安装到目标宿主。
     *
     * @return 宿主返回的安装结果（含宿主侧落盘路径与状态）
     */
    public Map<String, Object> install(String hostService, String pluginCode, String version,
                                       byte[] content, String md5) {
        if (internalJobToken.isEmpty()) {
            throw new IllegalStateException("base 未配置 houcloud.plugin.internal-job-token，无法派发跨宿主安装");
        }
        String baseUrl = resolveBaseUrl(hostService);
        String boundary = "houcloud-plugin-" + Long.toHexString(System.nanoTime());
        byte[] body = multipart(boundary, pluginCode, version, md5, content);
        HttpRequest request = HttpRequest.newBuilder(URI.create(baseUrl + "/internal/plugin/install"))
                .header("Content-Type", "multipart/form-data; boundary=" + boundary)
                .header("X-Internal-Job-Token", internalJobToken)
                .timeout(Duration.ofSeconds(120))
                .POST(HttpRequest.BodyPublishers.ofByteArray(body))
                .build();
        String responseBody;
        int status;
        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            status = response.statusCode();
            responseBody = response.body();
        } catch (Exception e) {
            throw new IllegalStateException("调用 " + hostService + " 宿主安装接口失败: " + e.getMessage(), e);
        }
        if (status < 200 || status >= 300) {
            throw new IllegalStateException(hostService + " 宿主安装接口返回 " + status + "："
                    + firstLine(responseBody));
        }
        JSONObject json;
        try {
            json = JSON.parseObject(responseBody);
        } catch (RuntimeException e) {
            throw new IllegalStateException(hostService + " 宿主安装接口返回非 JSON：" + firstLine(responseBody), e);
        }
        int code = json.getIntValue("code");
        if (code != 0) {
            throw new IllegalStateException(hostService + " 宿主安装失败：" + json.getString("message"));
        }
        Map<String, Object> data = json.getObject("data", Map.class);
        log.info("[跨宿主安装] {}@{} 已由 {} 宿主装载：{}", pluginCode, version, hostService, data);
        return data == null ? Map.of() : data;
    }

    /**
     * 派发卸载到目标宿主。
     */
    public void uninstall(String hostService, String pluginCode) {
        if (internalJobToken.isEmpty()) {
            throw new IllegalStateException("base 未配置 houcloud.plugin.internal-job-token，无法派发跨宿主卸载");
        }
        String baseUrl = resolveBaseUrl(hostService);
        HttpRequest request = HttpRequest.newBuilder(
                        URI.create(baseUrl + "/internal/plugin/uninstall?pluginCode=" + pluginCode))
                .header("X-Internal-Job-Token", internalJobToken)
                .timeout(Duration.ofSeconds(60))
                .POST(HttpRequest.BodyPublishers.noBody())
                .build();
        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new IllegalStateException(hostService + " 宿主卸载接口返回 " + response.statusCode()
                        + "：" + firstLine(response.body()));
            }
        } catch (IllegalStateException e) {
            throw e;
        } catch (Exception e) {
            throw new IllegalStateException("调用 " + hostService + " 宿主卸载接口失败: " + e.getMessage(), e);
        }
    }

    private String resolveBaseUrl(String hostService) {
        String configured = environment.getProperty(ENDPOINT_PREFIX + hostService);
        if (configured != null && !configured.isBlank()) {
            return configured.trim().replaceAll("/+$", "");
        }
        if (discoveryClient != null) {
            List<ServiceInstance> instances = discoveryClient.getInstances(hostService);
            if (instances != null) {
                for (ServiceInstance instance : instances) {
                    URI uri = instance.getUri();
                    if (uri != null) {
                        return uri.toString().replaceAll("/+$", "");
                    }
                }
            }
            throw new IllegalStateException("注册中心未找到 " + hostService + " 服务的可用实例，"
                    + "可通过 " + ENDPOINT_PREFIX + hostService + " 显式指定宿主地址");
        }
        throw new IllegalStateException("无法解析 " + hostService + " 宿主地址（无注册中心），"
                + "请配置 " + ENDPOINT_PREFIX + hostService);
    }

    private static byte[] multipart(String boundary, String pluginCode, String version, String md5, byte[] content) {
        StringBuilder head = new StringBuilder();
        head.append("--").append(boundary).append("\r\n");
        head.append("Content-Disposition: form-data; name=\"pluginCode\"\r\n\r\n")
                .append(pluginCode).append("\r\n");
        head.append("--").append(boundary).append("\r\n");
        head.append("Content-Disposition: form-data; name=\"version\"\r\n\r\n")
                .append(version).append("\r\n");
        head.append("--").append(boundary).append("\r\n");
        head.append("Content-Disposition: form-data; name=\"md5\"\r\n\r\n")
                .append(md5 == null ? "" : md5).append("\r\n");
        head.append("--").append(boundary).append("\r\n");
        head.append("Content-Disposition: form-data; name=\"file\"; filename=\"")
                .append(pluginCode).append("-").append(version).append(".jar\"\r\n");
        head.append("Content-Type: application/java-archive\r\n\r\n");
        byte[] headBytes = head.toString().getBytes(StandardCharsets.UTF_8);
        byte[] tailBytes = ("\r\n--" + boundary + "--\r\n").getBytes(StandardCharsets.UTF_8);
        byte[] body = new byte[headBytes.length + content.length + tailBytes.length];
        System.arraycopy(headBytes, 0, body, 0, headBytes.length);
        System.arraycopy(content, 0, body, headBytes.length, content.length);
        System.arraycopy(tailBytes, 0, body, headBytes.length + content.length, tailBytes.length);
        return body;
    }

    private static String firstLine(String text) {
        if (text == null || text.isBlank()) {
            return "(空响应)";
        }
        String trimmed = text.trim();
        int newline = trimmed.indexOf('\n');
        String line = newline < 0 ? trimmed : trimmed.substring(0, newline);
        return line.length() > 300 ? line.substring(0, 300) + "…" : line;
    }
}
