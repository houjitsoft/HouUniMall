package com.houjiang.mall.entity.plugin;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 插件 schema 迁移记录（hou_addon_plugin_migration，P0-4）。
 *
 * <p>唯一键 {@code uk_plugin_version(plugin_code, version)} 保证幂等：同一插件同一版本只成功迁移一次，
 * 重复安装时由 {@code SchemaMigrationRunner} 查 {@code status=1} 命中即跳过。
 *
 * <p>约束（说明文档 §7.4.2 P1「数据迁移与代码回滚混为一谈」）：迁移只做可审计的前向变更；
 * 卸载默认保留业务数据，本表只增不随卸载清理，失败也以 {@code status=0} 留痕供排障。
 *
 * @author Amos
 */
@Data
@TableName("hou_addon_plugin_migration")
public class AddonPluginMigration implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 迁移成功 */
    public static final int STATUS_SUCCESS = 1;
    /** 迁移失败 */
    public static final int STATUS_FAILED = 0;

    @TableId(type = IdType.AUTO)
    private Long id;
    /** 插件唯一编码 */
    private String pluginCode;
    /** 插件版本（取自插件描述符，取不到时为 unknown） */
    private String version;
    /** 实际执行的脚本路径（多个以逗号分隔） */
    private String script;
    /** 已执行脚本内容的 SHA-256 摘要（审计用） */
    private String checksum;
    /** 1=成功，0=失败 */
    private Integer status;
    /** 失败原因或备注 */
    private String message;
    private LocalDateTime appliedTime;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
