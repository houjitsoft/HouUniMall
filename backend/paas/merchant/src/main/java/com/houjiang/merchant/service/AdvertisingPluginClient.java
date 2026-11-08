package com.houjiang.merchant.service;

import feign.RequestInterceptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.context.annotation.Bean;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Map;

/** Narrow merchant-to-order client for the mall-advertising plugin. */
@FeignClient(name = "order", contextId = "merchantAdvertisingPlugin",
        configuration = AdvertisingPluginClient.InternalTokenConfiguration.class)
public interface AdvertisingPluginClient {

    @PostMapping("/plugin/mall-advertising/merchant/applications")
    Object list(@RequestBody Map<String, Object> trustedRequest);

    @PostMapping("/plugin/mall-advertising/merchant/positions")
    Object positions(@RequestBody Map<String, Object> trustedRequest);

    @PostMapping("/plugin/mall-advertising/merchant/applications/save")
    Object save(@RequestBody Map<String, Object> trustedRequest);

    @PostMapping("/plugin/mall-advertising/merchant/applications/submit")
    Object submit(@RequestBody Map<String, Object> trustedRequest);

    @PostMapping("/plugin/mall-advertising/merchant/applications/withdraw")
    Object withdraw(@RequestBody Map<String, Object> trustedRequest);

    final class InternalTokenConfiguration {
        public InternalTokenConfiguration() {
        }

        @Bean
        public RequestInterceptor advertisingInternalToken(
                @Value("${houcloud.plugin.internal-job-token:}") String token) {
            String trustedToken = token == null ? "" : token.trim();
            return template -> {
                template.removeHeader("Authorization");
                if (!trustedToken.isBlank()) {
                    template.header("X-Internal-Job-Token", trustedToken);
                }
            };
        }
    }
}
