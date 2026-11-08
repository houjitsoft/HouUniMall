package com.houjiang.mall.config;

import com.houjiang.commons.util.PlatformServiceSignature;
import feign.RequestInterceptor;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.Clock;
import java.util.UUID;
import java.util.function.Supplier;

/** 仅为 mall 到 merchant 的平台财务调用透传已经校验过的用户凭证。 */
public class MerchantPlatformFeignAuthConfig {

    private final String serviceSecret;
    private final Clock clock;
    private final Supplier<String> nonceSupplier;

    @Autowired
    public MerchantPlatformFeignAuthConfig(
            @Value("${mall.platform-finance.service-secret}") String serviceSecret) {
        this(serviceSecret, Clock.systemUTC(), () -> UUID.randomUUID().toString());
    }

    public MerchantPlatformFeignAuthConfig(String serviceSecret, Clock clock, Supplier<String> nonceSupplier) {
        if (serviceSecret == null || serviceSecret.isBlank()) {
            throw new IllegalStateException("mall.platform-finance.service-secret 未配置");
        }
        this.serviceSecret = serviceSecret;
        this.clock = clock;
        this.nonceSupplier = nonceSupplier;
    }

    @Bean
    public RequestInterceptor platformAuthorizationForwarder() {
        return template -> {
            if (!(RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes attributes)) {
                return;
            }
            HttpServletRequest request = attributes.getRequest();
            String authorization = request.getHeader("Authorization");
            if (authorization == null || authorization.isBlank()) {
                authorization = request.getHeader("authorization");
            }
            if (authorization != null && !authorization.isBlank()) {
                template.header("Authorization", authorization);
                long timestamp = clock.instant().getEpochSecond();
                String nonce = nonceSupplier.get();
                template.header(PlatformServiceSignature.TIMESTAMP_HEADER, Long.toString(timestamp));
                template.header(PlatformServiceSignature.NONCE_HEADER, nonce);
                template.header(PlatformServiceSignature.SIGNATURE_HEADER,
                        PlatformServiceSignature.sign(serviceSecret, timestamp, nonce, authorization,
                                template.method(), template.path(), template.body()));
            }
        };
    }
}
