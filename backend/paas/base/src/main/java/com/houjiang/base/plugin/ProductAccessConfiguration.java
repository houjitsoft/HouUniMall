package com.houjiang.base.plugin;

import java.time.Clock;
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
            // ★ 先按市场声明分流：license_type=FREE 的产品不进付费门控。
            //   开源包内置的免费插件（logging 等 11 个）既不会写 hou_service_installation
            //   （那张表只记独立部署的付费服务实例），也永远不会签发 PAID 授权；此前一律
            //   走下面的付费链路，结果恒为 disabled —— 日志中心整块接口返回
            //   423「Product not enabled or unlicensed」，后台告警角标永远取不到数。
            //   这里只要求「插件已登记且处于启用态」，不放宽任何付费校验：
            //   license_type 为 PAID/TRIAL、或市场里根本没有该行（历史/内部产品）时，
            //   仍然原样走付费链路，行为与改动前完全一致。
            String licenseType = jdbc.query(
                    "SELECT license_type FROM hou_market_plugin WHERE plugin_code=? AND deleted=0 ORDER BY id DESC LIMIT 1",
                    rs -> rs.next() ? rs.getString(1) : null, code);
            if (licenseType != null
                    && !com.houjiang.mall.service.market.PluginLicenseService.requiresLicense(licenseType)) {
                var free = jdbc.queryForList(
                        "SELECT version FROM hou_addon_plugin WHERE plugin_code=? AND deleted=0 AND status=1", code);
                if (free.isEmpty()) return new ProductAccessService.State(false,"disabled",0);
                return new ProductAccessService.State(true,"free:"+free.get(0).get("version"),Long.MAX_VALUE);
            }
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
}
