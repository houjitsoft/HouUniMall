package com.houjiang.goods.util.vop;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.input.BoundedInputStream;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.NoHttpResponseException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.config.ConnectionConfig;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.HttpClientConnectionManager;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.impl.client.DefaultBackoffStrategy;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.impl.conn.SystemDefaultDnsResolver;
import org.apache.http.protocol.HttpContext;
import org.apache.http.ssl.SSLContexts;
import org.apache.http.util.EntityUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Scope;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

/**
 * 高并发场景下的httpClient优化使用
 *
 * @author Amos
 */
@Service
@Scope("singleton")
public class JDHttpClientUtils {
    /**
     * HTTP_CLIENT_CONNECTION_MANAGER
     */
    private static PoolingHttpClientConnectionManager httpClientConnectionManager = null;
    /**
     * HTTP_CLIENT
     */
    private static HttpClient HTTP_CLIENT = null;
    /**
     * 链接最大数量
     */
    private static int maxTotal = 25600;
    /**
     * 链接最大路由数量
     */
    private static int maxPerRoute = 5;
    /**
     * 链接超时时间
     */
    private static int connectTimeout = 30000;
    /**
     * http链接最大闲置时间
     */
    private static int httpIdleTimeSec = 60;
    /**
     * 重试次数
     */
    private static int retryCount = 3;
    /**
     * http最大响应尺寸
     */
    private static double maxHttpResponseMb = 1;

    @PostConstruct
    public static void postConstruct() throws Throwable {
        PlainConnectionSocketFactory plainConnectionSocketFactory = new PlainConnectionSocketFactory() {
            @Override
            public Socket connectSocket(int connectTimeout, Socket socket, HttpHost host, InetSocketAddress remoteAddress, InetSocketAddress localAddress, HttpContext context) throws IOException {
                context.setAttribute("remoteAddress", remoteAddress);
                return super.connectSocket(connectTimeout, socket, host, remoteAddress, localAddress, context);
            }
        };
        TrustStrategy acceptingTrustStrategy = (cert, authType) -> true;
        SSLContext sslContext = SSLContexts.custom().loadTrustMaterial(null, acceptingTrustStrategy).build();
        SSLConnectionSocketFactory sslConnectionSocketFactory = new SSLConnectionSocketFactory(sslContext, NoopHostnameVerifier.INSTANCE);
        httpClientConnectionManager = new PoolingHttpClientConnectionManager(RegistryBuilder.<ConnectionSocketFactory>create()
                .register("http", plainConnectionSocketFactory)
                .register("https", sslConnectionSocketFactory)
                .build(), SystemDefaultDnsResolver.INSTANCE);
        httpClientConnectionManager.setMaxTotal(maxTotal);
        httpClientConnectionManager.setDefaultMaxPerRoute(maxPerRoute);
        httpClientConnectionManager.setDefaultConnectionConfig(ConnectionConfig.custom().setCharset(StandardCharsets.UTF_8).build());
    }

    @Bean("httpClientConnectionManager")
    public static HttpClientConnectionManager getHttpClientConnectionManager() {
        return httpClientConnectionManager;
    }

    @Bean("httpClient")
    public static HttpClient getHttpClientBuilder() throws Throwable {
        return HTTP_CLIENT = HttpClientBuilder.create().setConnectionManager(httpClientConnectionManager)
                .setRetryHandler((exception, executionCount, context) -> {
                    //org.apache.http.impl.client.DefaultHttpRequestRetryHandler.DefaultHttpRequestRetryHandler(int, boolean)
                    if (executionCount >= retryCount) {
                        //超过重试次数
                        return false;
                    }
                    //服务器连接丢失
                    return exception instanceof NoHttpResponseException;
                })
                .setDefaultRequestConfig(RequestConfig.custom()
                        .setConnectTimeout(connectTimeout)
                        .setConnectionRequestTimeout(connectTimeout)
                        .setSocketTimeout(connectTimeout)
                        .setCookieSpec(CookieSpecs.STANDARD_STRICT)
                        .build())
                .setConnectionManagerShared(false)
                //.setBackoffManager(new AIMDBackoffManager())
                .setConnectionBackoffStrategy(new DefaultBackoffStrategy())
                .build();
    }

    /**
     * 自动清理链接池
     */
    @Scheduled(fixedDelay = 10L * 1000, initialDelay = 120L * 1000)
    public static void autoCloseConnectionsSchedule() {
        try {
            httpClientConnectionManager.closeExpiredConnections();
            httpClientConnectionManager.closeIdleConnections(httpIdleTimeSec, TimeUnit.SECONDS);
        } catch (Exception ignored) {
        }
    }

    @Nullable
    public static String getContentByEntity(@NotNull HttpEntity httpEntity) throws IOException {
        try (InputStream content = httpEntity.getContent();
             BoundedInputStream boundedInputStream = new BoundedInputStream(content, (long) (maxHttpResponseMb * 1024L * 1024L))) {
            return IOUtils.toString(boundedInputStream, StandardCharsets.UTF_8);
        } finally {
            EntityUtils.consumeQuietly(httpEntity);
        }
    }

    public static String httpPost(HttpPost httpPost) {
        HttpResponse executeResponse = null;
        try {
            executeResponse = HTTP_CLIENT.execute(httpPost, HttpClientContext.create());
            return getContentByEntity(executeResponse.getEntity());
        } catch (Throwable e) {
            throw new RuntimeException(e);
        } finally {
            if (executeResponse != null) {
                EntityUtils.consumeQuietly(executeResponse.getEntity());
            }
        }
    }

    public static String httpGet(HttpGet httpGet) {
        HttpResponse executeResponse = null;
        try {
            executeResponse = HTTP_CLIENT.execute(httpGet, HttpClientContext.create());
            return getContentByEntity(executeResponse.getEntity());
        } catch (Throwable e) {
            throw new RuntimeException(e);
        } finally {
            if (executeResponse != null) {
                EntityUtils.consumeQuietly(executeResponse.getEntity());
            }
        }
    }
}
