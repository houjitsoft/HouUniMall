package com.houjiang.payment.channel.client;

import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * @author Amos
 */
@Component
public class AlipayClientFactory {

    private static final String DEFAULT_GATEWAY = "https://openapi.alipay.com/gateway.do";

    public AlipayClient create(Map<String, Object> config) {
        return new DefaultAlipayClient(
                value(config, "gatewayUrl", DEFAULT_GATEWAY),
                value(config, "appId", null),
                value(config, "privateKey", null),
                "json",
                "UTF-8",
                value(config, "alipayPublicKey", null),
                "RSA2");
    }

    private String value(Map<String, Object> config, String key, String defaultValue) {
        Object value = config.get(key);
        return value == null || String.valueOf(value).isBlank() ? defaultValue : String.valueOf(value);
    }
}
