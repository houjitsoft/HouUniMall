package com.houjiang.base;

import com.houjiang.plugin.runtime.PluginRuntimeAutoConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Import;
import org.mybatis.spring.annotation.MapperScan;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author Amos
 */
@EnableFeignClients(basePackages = {
        "com.houjiang.base",
        "com.houjiang.mall",
        "com.houjiang.plugin.market"
})
@SpringBootApplication(scanBasePackages = {"com.houjiang.*"})
@MapperScan(basePackages = {"com.houjiang.base.mapper", "com.houjiang.mall.mapper"}, annotationClass = Mapper.class)
@EnableDiscoveryClient
@org.springframework.scheduling.annotation.EnableScheduling
@Import(PluginRuntimeAutoConfiguration.class)
public class BaseApplication {

    public static void main(String[] args) {
        SpringApplication.run(BaseApplication.class, args);
    }

}
