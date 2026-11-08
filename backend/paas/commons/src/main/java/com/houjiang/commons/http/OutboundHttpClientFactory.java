package com.houjiang.commons.http;

import okhttp3.OkHttpClient;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import java.util.concurrent.TimeUnit;

/**
 * 出站 HTTP 客户端统一超时工厂，避免裸 {@code new RestTemplate()} / {@code new OkHttpClient()} 无限等待。
 * @author Amos
 */
public final class OutboundHttpClientFactory {

    /** 常规内部/第三方回调：connect 5s / read 30s */
    public static final int DEFAULT_CONNECT_SEC = 5;
    public static final int DEFAULT_READ_SEC = 30;

    /** 企微/钉钉等外部 SaaS：connect 10s / read 60s */
    public static final int EXTERNAL_CONNECT_SEC = 10;
    public static final int EXTERNAL_READ_SEC = 60;

    private OutboundHttpClientFactory() {
    }

    public static OkHttpClient okHttpDefault() {
        return okHttp(DEFAULT_CONNECT_SEC, DEFAULT_READ_SEC);
    }

    public static OkHttpClient okHttpExternal() {
        return okHttp(EXTERNAL_CONNECT_SEC, EXTERNAL_READ_SEC);
    }

    public static OkHttpClient okHttp(int connectSec, int readSec) {
        return new OkHttpClient.Builder()
                .connectTimeout(connectSec, TimeUnit.SECONDS)
                .readTimeout(readSec, TimeUnit.SECONDS)
                .writeTimeout(readSec, TimeUnit.SECONDS)
                .callTimeout(connectSec + readSec, TimeUnit.SECONDS)
                .build();
    }

    public static RestTemplate restTemplateDefault() {
        return restTemplate(DEFAULT_CONNECT_SEC * 1000, DEFAULT_READ_SEC * 1000);
    }

    public static RestTemplate restTemplateExternal() {
        return restTemplate(EXTERNAL_CONNECT_SEC * 1000, EXTERNAL_READ_SEC * 1000);
    }

    public static RestTemplate restTemplate(int connectMs, int readMs) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(connectMs);
        factory.setReadTimeout(readMs);
        return new RestTemplate(factory);
    }
}
