package com.houjiang.mall.config;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;

/** 确保商家一对多门店主数据在部署环境中完成幂等升级。 */
@Component
@RequiredArgsConstructor
public class MallStoreSchemaInitializer implements ApplicationRunner {

    private final DataSource dataSource;

    @Override
    public void run(ApplicationArguments args) {
        ResourceDatabasePopulator populator = new ResourceDatabasePopulator(
                new ClassPathResource("sql/mall_store_tables.sql"));
        populator.setContinueOnError(false);
        populator.execute(dataSource);
    }
}
