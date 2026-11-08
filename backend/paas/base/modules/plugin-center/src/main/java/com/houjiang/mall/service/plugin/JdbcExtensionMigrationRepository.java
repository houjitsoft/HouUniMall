package com.houjiang.mall.service.plugin;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * 基于 JDBC 的迁移幂等仓库（R2.1）。
 *
 * <p>落地幂等表 {@code hou_ext_migration(code, script, version, checksum, status, message, updated_at)}，
 * 建表脚本为 classpath 资源 {@value #DDL_RESOURCE}（{@code CREATE TABLE IF NOT EXISTS}，可重复执行）。
 * 表本身即二开数据，故沿用 {@code hou_ext_} 前缀。
 *
 * <p>写审计使用 MySQL {@code INSERT ... ON DUPLICATE KEY UPDATE}（主键 {@code (code, script)}），
 * 天然幂等：同脚本重复执行不产生多行。
 *
 * @author Amos
 */
@Slf4j
public class JdbcExtensionMigrationRepository implements ExtensionMigrationRepository {

    /** 幂等表名（二开前缀）。 */
    public static final String TABLE = "hou_ext_migration";

    /** 幂等表建表脚本（classpath 资源）。 */
    public static final String DDL_RESOURCE = "db/hou_ext_migration.sql";

    /** {@code message} 列宽（varchar(1024)）截断阈值。 */
    private static final int MESSAGE_MAX = 1000;

    private final DataSource dataSource;
    private volatile boolean schemaReady;

    public JdbcExtensionMigrationRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public boolean isApplied(String code, String script, String checksum) {
        ensureSchema();
        String sql = "SELECT checksum FROM " + TABLE
                + " WHERE code = ? AND script = ? AND status = 'SUCCESS'";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, code);
            statement.setString(2, script);
            try (ResultSet rs = statement.executeQuery()) {
                return rs.next() && checksum != null && checksum.equals(rs.getString(1));
            }
        } catch (SQLException e) {
            throw new IllegalStateException("查询二开迁移审计失败 (" + code + "/" + script + ")：" + e.getMessage(), e);
        }
    }

    @Override
    public void markSuccess(String code, String version, String script, String checksum) {
        upsert(code, version, script, checksum, "SUCCESS", null);
    }

    @Override
    public void markFailure(String code, String version, String script, String checksum, String message) {
        upsert(code, version, script, checksum, "FAILED", truncate(message));
    }

    /** 幂等写入审计行（主键 code+script，冲突即更新）。 */
    private void upsert(String code, String version, String script, String checksum, String status, String message) {
        ensureSchema();
        String sql = "INSERT INTO " + TABLE
                + " (code, script, version, checksum, status, message) VALUES (?, ?, ?, ?, ?, ?)"
                + " ON DUPLICATE KEY UPDATE version = VALUES(version), checksum = VALUES(checksum),"
                + " status = VALUES(status), message = VALUES(message)";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, code);
            statement.setString(2, script);
            statement.setString(3, version);
            statement.setString(4, checksum);
            statement.setString(5, status);
            statement.setString(6, message);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new IllegalStateException("写入二开迁移审计失败 (" + code + "/" + script + ")：" + e.getMessage(), e);
        }
    }

    /** 懒加载建表：{@code CREATE TABLE IF NOT EXISTS}，重复调用无副作用。 */
    synchronized void ensureSchema() {
        if (schemaReady) {
            return;
        }
        try {
            ResourceDatabasePopulator populator = new ResourceDatabasePopulator(new ClassPathResource(DDL_RESOURCE));
            populator.setContinueOnError(false);
            populator.execute(dataSource);
            schemaReady = true;
            log.debug("二开迁移幂等表就绪: {}", TABLE);
        } catch (RuntimeException e) {
            throw new IllegalStateException("初始化二开迁移幂等表 " + TABLE + " 失败：" + e.getMessage(), e);
        }
    }

    private String truncate(String message) {
        if (message == null) {
            return null;
        }
        return message.length() <= MESSAGE_MAX ? message : message.substring(0, MESSAGE_MAX);
    }
}
