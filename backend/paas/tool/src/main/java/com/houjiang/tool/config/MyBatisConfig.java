package com.houjiang.tool.config;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Configuration;

/**
 * MyBatis 配置：数据源来自 Nacos（mybatis.yaml / mutual.yaml 扩展配置），
 * tool 宿主内的嵌入式插件（如 ocr）通过 JdbcTemplate 复用同一主 DataSource。
 */
@Configuration
@MapperScan(basePackages = {
        "com.houjiang.commons.mapper",
        "com.houjiang.tool.mapper"
})
public class MyBatisConfig {
}
