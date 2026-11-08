package com.houjiang.mall.config;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;

/**
 * 确保插件化架构站点库表在部署环境中幂等建表（P0-3 {@code hou_addon_plugin} 目录、
 * P0-4 {@code hou_addon_plugin_migration} 迁移记录）。
 *
 * <p>脚本均为 {@code CREATE TABLE IF NOT EXISTS}，随 mall 启动重复执行安全；
 * 与 {@link MallStoreSchemaInitializer} 同一范式（{@link ResourceDatabasePopulator} + 启动期执行）。
 *
 * @author Amos
 */
@Component
@RequiredArgsConstructor
public class PluginAddonSchemaInitializer implements ApplicationRunner {

    private final DataSource dataSource;

    @Override
    public void run(ApplicationArguments args) {
        ResourceDatabasePopulator populator = new ResourceDatabasePopulator(
                new ClassPathResource("sql/plugin_addon_tables.sql"));
        populator.setContinueOnError(false);
        populator.execute(dataSource);
    }
}
