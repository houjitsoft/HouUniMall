package com.houjiang.mall;

import com.houjiang.plugin.runtime.PluginRuntimeAutoConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Import;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Mall商城服务启动类
 * 提供分销、返利、仓储、风控等功能
 * 
 * @author Amos
 */
@EnableFeignClients(basePackages = {
        "com.houjiang.mall",
        // 商城域插件共性 Feign（activity-shared 的 com.houjiang.activity.feign）。
        // 只扫自身包会让插件 @Autowired 的 FeignClient 缺 bean，装载后首次调用即
        // NoSuchBeanDefinitionException（order/goods/userms 也都显式追加了这个包）。
        "com.houjiang.activity.feign"
        // plugin.market Feign adapters belong to base (plugin-center control plane).
        // Do not scan them from mall — see 系统架构.md §4.
})
@EnableDiscoveryClient
@EnableScheduling
@Import(PluginRuntimeAutoConfiguration.class)
@SpringBootApplication(scanBasePackages = {"com.houjiang.*"})
public class MallApplication {

    public static void main(String[] args) {
        SpringApplication.run(MallApplication.class, args);
    }

}
