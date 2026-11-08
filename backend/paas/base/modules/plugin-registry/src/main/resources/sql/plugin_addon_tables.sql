-- =====================================================================
-- 插件化架构 · 站点库表（幂等建表，可随 mall 启动重复执行）
--   hou_addon_plugin            站点插件目录（P0-3：plugin.yaml 单向同步目标）
--   hou_addon_plugin_migration  插件 schema 迁移记录（P0-4：迁移幂等 + 审计）
-- 说明：两表此前仅在代码/文档出现、无受管 DDL；此处补齐，均用 IF NOT EXISTS，
--       已存在则不改动（列差异由各自 Mapper 的显式 SQL 兼容）。
-- =====================================================================

CREATE TABLE IF NOT EXISTS `hou_addon_plugin` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `service_code` varchar(64) NOT NULL COMMENT '归属服务编码，如 mall',
  `plugin_code` varchar(64) NOT NULL COMMENT '插件唯一编码',
  `name` varchar(128) DEFAULT NULL COMMENT '插件名称',
  `category` varchar(64) DEFAULT NULL COMMENT '分类展示名',
  `cate_id` int NOT NULL DEFAULT 0 COMMENT '分类引用 hou_addon_cate.id；人工设置优先',
  `version` varchar(32) DEFAULT NULL COMMENT '版本',
  `description` varchar(512) DEFAULT NULL COMMENT '描述',
  `terminals_json` varchar(255) DEFAULT NULL COMMENT '适用端 JSON 数组',
  `dependencies_json` varchar(512) DEFAULT NULL COMMENT '依赖插件编码 JSON 数组',
  `entry_path` varchar(255) DEFAULT NULL COMMENT '前端入口',
  `config_schema` text COMMENT '配置面板 Schema JSON',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '1=上架,0=下架',
  `sort` int NOT NULL DEFAULT 0 COMMENT '排序权重（人工维护，upsert 不覆盖）',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '@TableLogic 软删:0=正常,1=删除',
  `create_time` datetime DEFAULT NULL,
  `update_time` datetime DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_service_plugin` (`service_code`,`plugin_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='站点插件目录（plugin.yaml 单向同步目标）';

CREATE TABLE IF NOT EXISTS `hou_addon_plugin_migration` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `plugin_code` varchar(64) NOT NULL COMMENT '插件唯一编码',
  `version` varchar(32) NOT NULL COMMENT '插件版本',
  `script` varchar(512) DEFAULT NULL COMMENT '执行的脚本路径，逗号分隔',
  `checksum` varchar(64) DEFAULT NULL COMMENT '脚本内容 SHA-256 摘要',
  `status` tinyint NOT NULL DEFAULT 0 COMMENT '1=成功,0=失败',
  `message` varchar(1024) DEFAULT NULL COMMENT '失败原因/备注',
  `applied_time` datetime DEFAULT NULL COMMENT '最近一次执行时间',
  `create_time` datetime DEFAULT NULL,
  `update_time` datetime DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_plugin_version` (`plugin_code`,`version`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='插件 schema 迁移记录（幂等）';
