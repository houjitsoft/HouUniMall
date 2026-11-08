-- 2026-10-16 补齐 pm 独立服务缺失的三张表。
-- 背景：实体类与 mapper 一直引用这三张表，但仓库里只有 pm.sql 基座 + 4 个迁移，
-- 从未有建表脚本（历史上是手工 DDL）。独立微服务部署时按实体/mapper 反推补齐。
-- 幂等：IF NOT EXISTS。

CREATE TABLE IF NOT EXISTS `hou_pm_task_attachment` (
  `id` int NOT NULL AUTO_INCREMENT,
  `task_id` int NOT NULL COMMENT '任务ID',
  `file_name` varchar(255) NOT NULL COMMENT '文件名称',
  `file_url` varchar(500) NOT NULL COMMENT '文件URL',
  `file_size` bigint DEFAULT NULL COMMENT '文件大小（字节）',
  `user_id` int DEFAULT NULL COMMENT '上传人ID',
  `user_name` varchar(64) DEFAULT NULL COMMENT '上传人名称',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `del_flag` varchar(1) NOT NULL DEFAULT '0' COMMENT '删除标记：0正常 1已删除',
  `version` int NOT NULL DEFAULT 0 COMMENT '版本号',
  PRIMARY KEY (`id`),
  KEY `idx_hou_pm_task_attachment_task` (`task_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='任务附件';

CREATE TABLE IF NOT EXISTS `hou_pm_task_comment` (
  `id` int NOT NULL AUTO_INCREMENT,
  `task_id` int NOT NULL COMMENT '任务ID',
  `content` text COMMENT '评论内容',
  `user_id` int DEFAULT NULL COMMENT '评论人ID',
  `user_name` varchar(64) DEFAULT NULL COMMENT '评论人名称',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `del_flag` varchar(1) NOT NULL DEFAULT '0' COMMENT '删除标记：0正常 1已删除',
  `version` int NOT NULL DEFAULT 0 COMMENT '版本号',
  PRIMARY KEY (`id`),
  KEY `idx_hou_pm_task_comment_task` (`task_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='任务评论';

CREATE TABLE IF NOT EXISTS `hou_pm_team_member` (
  `id` int NOT NULL AUTO_INCREMENT,
  `team_id` int NOT NULL COMMENT '团队ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `role` varchar(32) NOT NULL DEFAULT 'member' COMMENT '团队角色：leader负责人 member成员',
  `join_time` datetime DEFAULT NULL COMMENT '加入时间',
  `create_by` varchar(64) DEFAULT NULL COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT NULL COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `del_flag` varchar(1) NOT NULL DEFAULT '0' COMMENT '删除标志：0代表存在 2代表删除',
  `version` int NOT NULL DEFAULT 0 COMMENT '版本号',
  PRIMARY KEY (`id`),
  KEY `idx_hou_pm_team_member_team` (`team_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='团队成员关联';
