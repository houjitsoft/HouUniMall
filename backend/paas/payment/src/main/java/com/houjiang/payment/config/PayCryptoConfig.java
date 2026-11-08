package com.houjiang.payment.config;

import com.houjiang.commons.util.PayCredentialCryptoUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import jakarta.annotation.PostConstruct;

/**
 * 渠道凭证加密 KEY/IV 注入配置
 * 启动时从 Nacos 读取 houjiang.pay.crypto.key / iv，覆盖默认硬编码
 *
 * @author Amos
 */
@Slf4j
@Configuration
public class PayCryptoConfig {

    @Value("${houjiang.pay.crypto.key:}")
    private String cryptoKey;

    @Value("${houjiang.pay.crypto.iv:}")
    private String cryptoIv;

    /**
     * 注入 Nacos 配置到加密工具类
     */
    @PostConstruct
    public void initCryptoKeyIv() {
        if (cryptoKey != null && cryptoKey.length() == 16
                && cryptoIv != null && cryptoIv.length() == 16) {
            PayCredentialCryptoUtil.setKeyIv(cryptoKey, cryptoIv);
            log.info("[PayCryptoConfig] AES KEY/IV 已从 Nacos 注入，长度=16/16，前 4 位 key={}***，iv={}***",
                    cryptoKey.substring(0, 4), cryptoIv.substring(0, 4));
        } else {
            log.warn("[PayCryptoConfig] Nacos 未配置 houjiang.pay.crypto.key/iv 或长度非 16，使用默认硬编码（生产慎用）");
        }
    }
}
