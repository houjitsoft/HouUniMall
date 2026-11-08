package com.houjiang.userms.config;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Configuration;

/**
 * MyBatis配置类
 * 
 * @author Amos
 * @since 2025-10-08
 */
@Configuration
@MapperScan(basePackages = {
        "com.houjiang.commons.mapper",
        "com.houjiang.userms.mapper",
        "com.houjiang.userms.ucenter.mapper"
})
public class MyBatisConfig {
}
