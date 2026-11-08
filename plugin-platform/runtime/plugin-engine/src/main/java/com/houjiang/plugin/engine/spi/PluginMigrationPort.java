package com.houjiang.plugin.engine.spi;

/**
 * 插件 schema 迁移端口（SPI，阶段 2）。
 *
 * <p>由宿主（mall）实现并注入：插件制品内的 {@code migration/} 脚本需要连接业务库执行，
 * 引擎本身不持有数据源，因此只定义契约、不绑定具体迁移框架（Flyway/Liquibase/自研）。
 *
 * <p><b>可选</b>：未注入时安装链路跳过 MIGRATED 阶段，行为与旧版一致（向后兼容）。
 * 注入后流程为 {@code VERIFIED → MIGRATED → ACTIVE}；迁移失败时引擎整体回滚并置 FAILED。
 *
 * <p><b>约束（§7.4.2 P1「数据迁移与代码回滚混为一谈」）</b>：
 * 迁移只做可审计的前向变更；卸载默认保留数据，回滚不自动删表。
 */
public interface PluginMigrationPort {

    /**
     * 执行插件 schema 迁移。
     *
     * @param pluginCode 插件编码
     * @param version    插件版本（取自插件描述符，取不到时为 {@code unknown}）
     * @throws RuntimeException 迁移失败时抛出，由引擎触发整体回滚
     */
    void migrate(String pluginCode, String version);
}
