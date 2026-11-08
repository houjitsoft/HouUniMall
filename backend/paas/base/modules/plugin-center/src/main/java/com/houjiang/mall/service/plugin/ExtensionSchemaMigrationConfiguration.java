package com.houjiang.mall.service.plugin;

import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

/**
 * 二开 classpath 迁移执行器的<b>可选</b>装配（R2.1）。
 *
 * <p>默认<b>关闭</b>（{@code houcloud.ext.migration.enabled} 未设置即不生效），因此对宿主启动
 * 零影响、也不建任何表；由交付/运维在启用二开站点时显式打开：
 * <pre>{@code
 * houcloud.ext.migration.enabled: true
 * }</pre>
 *
 * <p>启用后：注入基于宿主 {@link DataSource} 的幂等仓库，暴露
 * {@link ExtensionSchemaMigrationRunner}，并在应用就绪后遍历全部二开
 * （{@code META-INF/houcloud-extension.properties}）执行 schema 迁移——重复启动因幂等表而零副作用。
 *
 * <p>真实装载与迁移执行属「必须真实环境（DB/BFF）验收」项。
 *
 * @author Amos
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnProperty(prefix = "houcloud.ext.migration", name = "enabled", havingValue = "true")
public class ExtensionSchemaMigrationConfiguration {

    /** 幂等迁移审计仓库（表 {@code hou_ext_migration}）。 */
    @Bean
    public ExtensionMigrationRepository extensionMigrationRepository(DataSource dataSource) {
        return new JdbcExtensionMigrationRepository(dataSource);
    }

    /** classpath 版迁移执行器（与既有 jar 内 {@code SchemaMigrationRunner} 并列）。 */
    @Bean
    public ExtensionSchemaMigrationRunner extensionSchemaMigrationRunner(
            DataSource dataSource, ExtensionMigrationRepository extensionMigrationRepository) {
        return new ExtensionSchemaMigrationRunner(dataSource, extensionMigrationRepository);
    }

    /** 应用就绪后触发一次全量幂等迁移。 */
    @Bean
    public ApplicationRunner extensionSchemaMigrationRunnerTrigger(ExtensionSchemaMigrationRunner runner) {
        return args -> runner.migrateAll();
    }
}
