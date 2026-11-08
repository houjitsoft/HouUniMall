package com.houjiang.payment.config;

import com.houjiang.commons.util.PayCredentialCryptoUtil;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

/**
 * @author Amos
 */
@Configuration
public class PaymentCredentialCryptoConfiguration {

    @Value("${payment.crypto.key:}")
    private String key;

    @Value("${payment.crypto.iv:}")
    private String iv;

    @PostConstruct
    public void configure() {
        if (StringUtils.hasText(key) || StringUtils.hasText(iv)) {
            PayCredentialCryptoUtil.setKeyIv(key, iv);
        }
    }
}
