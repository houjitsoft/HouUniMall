package com.houjiang.mall.mapper.plugin;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.houjiang.mall.entity.plugin.AddonPluginMigration;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 插件 schema 迁移记录（hou_addon_plugin_migration）数据访问（P0-4）。
 *
 * <p>幂等键 {@code uk_plugin_version(plugin_code, version)}：{@link #countSuccess} 供安装前判重，
 * {@link #upsert} 以显式 SQL 落一条最新尝试结果（成功/失败均覆盖同一行，保留审计留痕）。
 *
 * @author Amos
 */
@Mapper
public interface AddonPluginMigrationMapper extends BaseMapper<AddonPluginMigration> {

    /**
     * 统计某插件某版本已成功迁移的记录数（&gt;0 表示无需重复迁移）。
     *
     * @param pluginCode 插件编码
     * @param version    插件版本
     * @return status=1 的行数
     */
    @Select("SELECT COUNT(1) FROM hou_addon_plugin_migration "
            + "WHERE plugin_code=#{pluginCode} AND version=#{version} AND status=1")
    int countSuccess(@Param("pluginCode") String pluginCode, @Param("version") String version);

    /**
     * 幂等落一条迁移结果：命中唯一键 {@code uk_plugin_version} 时覆盖脚本/摘要/状态/原因与时间，
     * 使「失败后重试成功」只保留最终态一行。
     *
     * @param record 迁移记录（plugin_code / version / status 必填）
     * @return 受影响行数（MySQL upsert：插入=1，更新=2）
     */
    @Insert("INSERT INTO hou_addon_plugin_migration "
            + "(plugin_code, version, script, checksum, status, message, applied_time, create_time, update_time) "
            + "VALUES (#{pluginCode}, #{version}, #{script}, #{checksum}, #{status}, #{message}, NOW(), NOW(), NOW()) "
            + "ON DUPLICATE KEY UPDATE script=VALUES(script), checksum=VALUES(checksum), status=VALUES(status), "
            + " message=VALUES(message), applied_time=NOW(), update_time=NOW()")
    int upsert(AddonPluginMigration record);

    /**
     * 落一条带操作类型的迁移结果（M4 升级 / 回滚审计）。
     *
     * <p>幂等键 {@code uk_mig_op(plugin_code, version, operation, from_version)}：
     * 同一「版本 + 操作 + 来源版本」只留一行，重复执行覆盖为最新结果。
     * install（operation=1）与 upgrade（2）/ rollback（3）的键不同，因此历史不会被互相覆盖。
     *
     * <p>⚠️ {@code fromVersion} 不能传 {@code null}：该列是 {@code NOT NULL DEFAULT ''}
     * （为空串才能被唯一键真正约束，见 DDL 说明），传 null 会直接违反非空约束。
     *
     * @param pluginCode  插件编码
     * @param version     版本（回滚时填「回滚到的版本」）
     * @param operation   1=install 2=upgrade 3=rollback
     * @param fromVersion 来源版本（install 传空串，rollback 传「回滚自哪个版本」）
     * @param rollbackOf  回滚自哪条迁移记录 ID，非回滚传 null
     * @param script      实际执行的脚本路径（多个以逗号分隔）
     * @param checksum    已执行脚本内容的 SHA-256
     * @param status      1=成功 0=失败
     * @param message     失败原因或备注
     * @return 受影响行数
     */
    @Insert("INSERT INTO hou_addon_plugin_migration "
            + "(plugin_code, version, operation, from_version, rollback_of, script, checksum, status, message, "
            + " applied_time, create_time, update_time) "
            + "VALUES (#{pluginCode}, #{version}, #{operation}, #{fromVersion}, #{rollbackOf}, #{script}, "
            + " #{checksum}, #{status}, #{message}, NOW(), NOW(), NOW()) "
            + "ON DUPLICATE KEY UPDATE script=VALUES(script), checksum=VALUES(checksum), status=VALUES(status), "
            + " message=VALUES(message), applied_time=NOW(), update_time=NOW()")
    int upsertOperation(@Param("pluginCode") String pluginCode,
                        @Param("version") String version,
                        @Param("operation") int operation,
                        @Param("fromVersion") String fromVersion,
                        @Param("rollbackOf") Long rollbackOf,
                        @Param("script") String script,
                        @Param("checksum") String checksum,
                        @Param("status") int status,
                        @Param("message") String message);

    /**
     * 查某条迁移记录的 ID（M4 回滚时填 {@code rollback_of} 用）。
     *
     * @param pluginCode  插件编码
     * @param version     版本
     * @param operation   操作类型
     * @param fromVersion 来源版本
     * @return 记录 ID，不存在返回 null
     */
    @Select("SELECT id FROM hou_addon_plugin_migration "
            + "WHERE plugin_code=#{pluginCode} AND version=#{version} AND operation=#{operation} "
            + "AND from_version=#{fromVersion} ORDER BY id DESC LIMIT 1")
    Long selectId(@Param("pluginCode") String pluginCode,
                  @Param("version") String version,
                  @Param("operation") int operation,
                  @Param("fromVersion") String fromVersion);
}
