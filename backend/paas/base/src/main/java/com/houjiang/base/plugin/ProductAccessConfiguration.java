package com.houjiang.base.plugin;

import java.time.Clock;
import java.util.Map;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.web.client.RestTemplate;
import org.springframework.boot.web.client.RestTemplateBuilder;
import java.time.Duration;

@Configuration
public class ProductAccessConfiguration {
    @Bean @LoadBalanced
    public RestTemplate productIdentityClient(RestTemplateBuilder builder) {
        return builder.setConnectTimeout(Duration.ofSeconds(2)).setReadTimeout(Duration.ofSeconds(3)).build();
    }
    @Bean
    public ProductAccessService productAccessService(JdbcTemplate jdbc, com.houjiang.mall.service.market.PluginLicenseService licenseService) {
        return new ProductAccessService((code, instance) -> {
            var installs = jdbc.queryForList("SELECT version, updated_at FROM hou_service_installation WHERE instance_id=? AND plugin_code=? AND status=1",instance,code);
            if (installs.size()!=1)
                return new ProductAccessService.State(false,"disabled",0);
            // All current standalone products require a site-bound paid license.
            var license=licenseService.findValidPaid(code,instance);
            if(license==null)return new ProductAccessService.State(false,"unlicensed",0);
            // Official maintenance expiration does not affect the delivered paid runtime.
            return new ProductAccessService.State(true,installs.get(0).get("version")+":"+installs.get(0).get("updated_at")+":"+license.getId(),Long.MAX_VALUE);
        }, Clock.systemUTC());
    }
    private static boolean one(Object value) { return value instanceof Number n ? n.intValue()==1 : "1".equals(String.valueOf(value)); }
}
