package com.houjiang.base.officialdownload;

import com.houjiang.commons.util.ResponseResult;
import java.time.Clock;
import java.util.Map;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(OfficialDownloadProperties.class)
@ConditionalOnProperty(name = "unimall.official-download.enabled", havingValue = "true")
public class OfficialDownloadConfiguration {
    @Bean
    DownloadSecurity officialDownloadSecurity(OfficialDownloadProperties p) {
        return new DownloadSecurity(p.getEncryptionKey(), p.getHmacKey());
    }

    @Bean
    OfficialDownloadService officialDownloadService(JdbcOfficialDownloadStore store, DownloadSecurity security,
                                                    OfficialDownloadSmsClient sms, OfficialDownloadProperties p) {
        VerificationCodeSender sender = phone -> {
            ResponseResult<String> response = sms.sendCode(Map.of("userMobile", phone, "scene", "official-download"));
            if (response == null || response.getCode() != 1 || response.getData() == null) {
                throw new DownloadFlowException("SMS_SEND_FAILED", response == null ? "短信服务暂时不可用" : response.getMessage());
            }
            return response.getData();
        };
        return new OfficialDownloadService(store, store, store, store, store, security, sender, Clock.systemUTC(),
                new OfficialDownloadPolicy(p.getSmsCooldownSeconds(), p.getCodeTtlSeconds(), p.getMaxCodeAttempts(),
                        p.getSessionTtlSeconds(), p.getMaxDownloads()));
    }
}
