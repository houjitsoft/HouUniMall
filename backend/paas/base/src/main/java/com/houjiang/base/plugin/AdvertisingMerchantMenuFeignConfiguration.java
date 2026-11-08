package com.houjiang.base.plugin;

import feign.RequestInterceptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;

/** Uses only the internal service credential for advertising menu lifecycle calls. */
public class AdvertisingMerchantMenuFeignConfiguration {

    private final String token;

    public AdvertisingMerchantMenuFeignConfiguration(
            @Value("${houcloud.plugin.internal-job-token:}") String token) {
        this.token = token == null ? "" : token.trim();
    }

    @Bean
    public RequestInterceptor advertisingLifecycleRequestInterceptor() {
        return template -> {
            if (token.isBlank()) {
                throw new IllegalStateException(
                        "houcloud.plugin.internal-job-token 未配置，拒绝同步商家端广告菜单");
            }
            template.removeHeader("Authorization");
            template.header("X-Internal-Job-Token", token);
        };
    }
}
