-- 会员画像 · 人群包（2026-11-09）
-- 幂等：重复执行安全。标签体系复用既有 hou_user_tag_info（标签字典）+ hou_base_tag_class（打标关系），
-- 本脚本只新增「人群包」这一张画像域自己的表。
SET NAMES utf8mb4;

CREATE TABLE IF NOT EXISTS `hou_user_portrait_crowd` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '主键',
  `crowd_name` varchar(100) NOT NULL COMMENT '人群包名称',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注/运营目的',
  `rule_json` varchar(2000) NOT NULL COMMENT '圈人规则快照（JSON）',
  `rule_text` varchar(500) DEFAULT NULL COMMENT '规则的中文可读描述',
  `member_count` int NOT NULL DEFAULT 0 COMMENT '保存时的命中人数',
  `member_ids` mediumtext COMMENT '成员ID快照，逗号分隔，用于导出与回溯',
  `create_by` bigint unsigned DEFAULT NULL COMMENT '创建人 user_id',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` tinyint NOT NULL DEFAULT 0 COMMENT '0 正常 1 已删除',
  PRIMARY KEY (`id`),
  KEY `idx_create_time` (`create_time`),
  KEY `idx_is_deleted` (`is_deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='会员画像-人群包';
