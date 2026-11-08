package com.houjiang.payment.ums;

import com.houjiang.payment.ums.client.UmsClient;
import com.houjiang.payment.ums.config.UmsConfigStore;
import com.houjiang.payment.ums.service.UmsCorpOrderService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.client.RestTemplate;

/**
 * 银联企业订单能力装配（payment 宿主内嵌；插件包同逻辑供市场安装）。
 */
@Configuration
public class UmsCorpOrderConfiguration {

    @Bean
    public UmsConfigStore umsConfigStore(JdbcTemplate jdbcTemplate) {
        return new UmsConfigStore(jdbcTemplate);
    }

    @Bean
    public UmsClient umsClient() {
        RestTemplate rest = new RestTemplate();
        var factory = new org.springframework.http.client.SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(10000);
        factory.setReadTimeout(35000);
        rest.setRequestFactory(factory);
        return new UmsClient(rest);
    }

    @Bean
    public UmsCorpOrderService umsCorpOrderService(JdbcTemplate jdbcTemplate,
                                                  UmsConfigStore configStore,
                                                  UmsClient umsClient) {
        return new UmsCorpOrderService(jdbcTemplate, configStore, umsClient);
    }
}
