-- 二开 schema 迁移幂等表（R2.1）。
-- 由 classpath 版迁移执行器 ExtensionSchemaMigrationRunner / JdbcExtensionMigrationRepository 自维护；
-- CREATE TABLE IF NOT EXISTS，可重复执行（幂等，零副作用）。
-- 表名沿用 hou_ext_ 前缀，因为「迁移审计」本身即二开数据。
CREATE TABLE IF NOT EXISTS `hou_ext_migration` (
    `code`       varchar(64)   NOT NULL COMMENT '二开模块 code',
    `script`     varchar(255)  NOT NULL COMMENT '迁移脚本名（相对 db/migration/{code}/）',
    `version`    varchar(32)            DEFAULT NULL COMMENT '二开版本',
    `checksum`   char(64)               DEFAULT NULL COMMENT '脚本内容 SHA-256',
    `status`     varchar(16)   NOT NULL COMMENT '执行状态：SUCCESS / FAILED',
    `message`    varchar(1024)          DEFAULT NULL COMMENT '失败原因（截断）',
    `updated_at` datetime      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`code`, `script`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT = '二开 schema 迁移幂等表';
