-- =====================================================================
-- 插件 pm 的数据库建表脚本（共 23 张表）
-- 用途：安装「pm」插件后按需执行（也可由插件自身的迁移通道建表）。
-- 🔴 商城核心（Core）初始化不包含本文件；未安装该插件时不要执行。
-- 全部 CREATE TABLE 已统一补 IF NOT EXISTS（幂等，可重复执行，不会删数据）。
-- 执行期间临时关闭外键检查：插件之间存在跨插件外键（如 oa 引用 hou_pm_project），
-- 未同时安装被引用插件时允许该外键暂时悬空。
-- =====================================================================

SET FOREIGN_KEY_CHECKS=0;

-- ---------- 基础表结构（结构基线） ----------
-- Table structure for table `hou_pm_files`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_pm_files` (
  `id` int unsigned NOT NULL AUTO_INCREMENT COMMENT '主键id',
  `project_id` int NOT NULL COMMENT '项目ID',
  `task_id` int NOT NULL COMMENT '任务ID',
  `url` varchar(255) NOT NULL COMMENT '附件url',
  `admin_id` int DEFAULT '0' COMMENT '创建人',
  `create_time` int unsigned NOT NULL DEFAULT '0' COMMENT '创建时间',
  `filename` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '' COMMENT '文件名称',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 COMMENT='附件表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_pm_log`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_pm_log` (
  `id` int unsigned NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `admin_id` int unsigned NOT NULL DEFAULT '0' COMMENT '管理员ID',
  `username` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT '' COMMENT '管理员名字',
  `url` varchar(1500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT '' COMMENT '操作页面',
  `title` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT '' COMMENT '日志标题',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '内容',
  `ip` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT '' COMMENT 'IP',
  `useragent` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT '' COMMENT 'User-Agent',
  `create_time` int DEFAULT NULL COMMENT '操作时间',
  `project_id` int DEFAULT NULL,
  `task_id` int DEFAULT NULL,
  `type` tinyint DEFAULT '0' COMMENT '0默认1更变2催促3留言',
  PRIMARY KEY (`id`),
  KEY `name` (`username`),
  KEY `type` (`type`),
  KEY `task_id` (`task_id`),
  KEY `project_id` (`project_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='操作日志表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_pm_notification_delivery`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_pm_notification_delivery` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `event_id` bigint NOT NULL,
  `org_id` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `receiver_key` varchar(192) COLLATE utf8mb4_unicode_ci NOT NULL,
  `receiver_pm_user_id` bigint DEFAULT NULL,
  `receiver_user_id` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `receiver_name` varchar(128) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `channel` varchar(24) COLLATE utf8mb4_unicode_ci NOT NULL,
  `receiver_contact` varchar(256) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `idempotency_key` varchar(160) COLLATE utf8mb4_unicode_ci NOT NULL,
  `title` varchar(256) COLLATE utf8mb4_unicode_ci NOT NULL,
  `content` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `biz_type` varchar(48) COLLATE utf8mb4_unicode_ci NOT NULL,
  `biz_id` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `status` tinyint NOT NULL DEFAULT '0',
  `retry_count` tinyint NOT NULL DEFAULT '0',
  `next_retry_time` bigint NOT NULL DEFAULT '0',
  `provider_message_id` varchar(160) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `error_code` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `error_message` varchar(1000) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `locked_by` varchar(96) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `locked_at` bigint DEFAULT NULL,
  `create_time` bigint NOT NULL,
  `update_time` bigint NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_pm_notification_delivery_target` (`event_id`,`receiver_key`,`channel`),
  UNIQUE KEY `uk_pm_notification_delivery_key` (`idempotency_key`),
  KEY `idx_pm_notification_delivery_due` (`status`,`next_retry_time`),
  KEY `idx_pm_notification_delivery_org` (`org_id`,`channel`,`status`,`create_time`),
  CONSTRAINT `fk_pm_notification_delivery_event` FOREIGN KEY (`event_id`) REFERENCES `hou_pm_notification_event` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='PM notification channel delivery';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_pm_notification_event`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_pm_notification_event` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `event_key` varchar(160) COLLATE utf8mb4_unicode_ci NOT NULL,
  `org_id` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `event_type` varchar(48) COLLATE utf8mb4_unicode_ci NOT NULL,
  `aggregate_type` varchar(24) COLLATE utf8mb4_unicode_ci NOT NULL,
  `aggregate_id` bigint NOT NULL,
  `aggregate_version` varchar(96) COLLATE utf8mb4_unicode_ci NOT NULL,
  `actor_pm_user_id` bigint DEFAULT NULL,
  `actor_user_id` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `setting_revision` bigint DEFAULT NULL,
  `payload_json` json NOT NULL,
  `status` tinyint NOT NULL DEFAULT '0',
  `reason_code` varchar(48) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `retry_count` tinyint NOT NULL DEFAULT '0',
  `next_retry_time` bigint NOT NULL DEFAULT '0',
  `error_code` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `error_message` varchar(1000) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `locked_by` varchar(96) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `locked_at` bigint DEFAULT NULL,
  `create_time` bigint NOT NULL,
  `update_time` bigint NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_pm_notification_event_key` (`event_key`),
  KEY `idx_pm_notification_event_due` (`status`,`next_retry_time`,`create_time`),
  KEY `idx_pm_notification_event_org_biz` (`org_id`,`aggregate_type`,`aggregate_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='PM transactional notification outbox';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_pm_project`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_pm_project` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `number` varchar(20) DEFAULT NULL COMMENT '项目编号',
  `name` varchar(50) NOT NULL COMMENT '项目名称',
  `category` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT 'internal' COMMENT '项目分类：internal内部项目 client客户项目 rd研发项目 operation运营项目 aftersale售后项目',
  `priority` tinyint DEFAULT '2' COMMENT '优先级：1低 2中 3高 4紧急',
  `sort` int NOT NULL DEFAULT '0' COMMENT '排序值，越大越靠前',
  `order_time` int DEFAULT NULL COMMENT '下单日期',
  `customer_id` int DEFAULT NULL COMMENT '客户ID',
  `customer_name` varchar(20) DEFAULT NULL COMMENT '客户名称',
  `contacts_id` int DEFAULT NULL COMMENT '联系人ID',
  `contacts_name` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '联系人姓名',
  `price` decimal(10,2) DEFAULT '0.00' COMMENT '项目金额',
  `expenses` decimal(10,2) DEFAULT '0.00' COMMENT '项目支出',
  `content` text COMMENT '工作内容和工作要求',
  `remark` varchar(100) DEFAULT NULL COMMENT '备注',
  `status` tinyint DEFAULT '1' COMMENT '状态-1关闭0未开始1进行中2已完成3归档4已暂停5已终止6已延期7售后中',
  `create_user_id` int DEFAULT '0' COMMENT '创建人ID',
  `project_user_id` int DEFAULT '0' COMMENT '项目经理',
  `user_ids` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '' COMMENT '成员',
  `visibility` varchar(16) NOT NULL DEFAULT 'public' COMMENT 'private/team/public',
  `sales_user_id` int DEFAULT NULL COMMENT '业务经理',
  `contract_id` int DEFAULT '0' COMMENT '合同ID',
  `start_time` int DEFAULT '0' COMMENT '开始时间',
  `end_time` int DEFAULT NULL COMMENT '截止时间',
  `test_time` int DEFAULT NULL COMMENT '初稿(测试)时间',
  `plan_hours` decimal(10,1) DEFAULT '0.0' COMMENT '计划工时(总)',
  `task_hours` decimal(10,1) DEFAULT '0.0' COMMENT '任务工时(已经安排任务花费工时)',
  `actual_hours` decimal(10,1) DEFAULT '0.0' COMMENT '实际工时（完成任务）',
  `complete_time` int DEFAULT NULL COMMENT '完成时间(归档)',
  `create_time` int DEFAULT NULL COMMENT '创建时间',
  `update_time` int DEFAULT NULL COMMENT '更新时间',
  `delete_time` int DEFAULT NULL COMMENT '删除时间',
  `admin_count` int DEFAULT '0' COMMENT '项目参与人数（统计任务的）',
  `task_count` int DEFAULT '0' COMMENT '任务总数量',
  `task_complate` int DEFAULT '0' COMMENT '任务完成量',
  `level` tinyint DEFAULT '1' COMMENT '优先级1无2不重要3次要4主要5紧急',
  `jobs` varchar(50) DEFAULT '' COMMENT '作业内容ID',
  `type` tinyint DEFAULT NULL COMMENT '项目类型：0=产品开发, 1=系统集成, 2=咨询服务, 3=维护支持, 4=售后服务',
  `wx_webhook` varchar(2000) DEFAULT '' COMMENT '企业微信机器人链接，多个换行分隔',
  `dd_webhook` varchar(2000) DEFAULT '' COMMENT '钉钉机器人链接，多个换行分隔',
  `feishu_webhook` varchar(2000) DEFAULT '' COMMENT '飞书机器人链接，多个换行分隔',
  `group_sync_mode` varchar(64) NOT NULL DEFAULT 'webhook' COMMENT '群同步方式：all/webhook/wecom_app/wecom_external_group 或逗号分隔',
  `wx_group_chat_id` varchar(512) DEFAULT '' COMMENT '企微客户群chat_id，多个换行分隔',
  `after_sales_enabled` tinyint NOT NULL DEFAULT '0' COMMENT '是否启用售后：0否 1是',
  `after_sales_start_time` int DEFAULT NULL COMMENT '售后开始时间（秒级时间戳）',
  `after_sales_end_time` int DEFAULT NULL COMMENT '售后结束时间（秒级时间戳）',
  PRIMARY KEY (`id`),
  KEY `idx_category` (`category`),
  KEY `idx_status_delete` (`status`,`delete_time`),
  KEY `idx_project_user_id` (`project_user_id`),
  KEY `idx_delete_time` (`delete_time`) USING BTREE,
  KEY `idx_update_time` (`update_time`) USING BTREE,
  KEY `idx_delete_update` (`delete_time`,`update_time`) USING BTREE,
  KEY `idx_status` (`status`),
  KEY `idx_delete_status_update` (`delete_time`,`status`,`update_time`),
  KEY `idx_pm_project_visibility` (`visibility`,`create_user_id`,`project_user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='项目列表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_pm_project_org`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_pm_project_org` (
  `id` int NOT NULL AUTO_INCREMENT,
  `project_id` int NOT NULL COMMENT '项目ID',
  `org_id` int NOT NULL COMMENT '组织ID',
  `create_time` int DEFAULT NULL COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_pm_project_org` (`project_id`,`org_id`),
  UNIQUE KEY `uq_pm_project_single_org` (`project_id`),
  KEY `idx_project_id` (`project_id`),
  KEY `idx_org_id` (`org_id`),
  KEY `idx_project_org` (`project_id`,`org_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='项目与组织关联表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_pm_project_org_isolation_backup_20260910`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_pm_project_org_isolation_backup_20260910` (
  `id` int NOT NULL AUTO_INCREMENT,
  `project_id` int NOT NULL COMMENT '项目ID',
  `org_id` int NOT NULL COMMENT '组织ID',
  `create_time` int DEFAULT NULL COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_pm_project_org` (`project_id`,`org_id`),
  KEY `idx_project_id` (`project_id`),
  KEY `idx_org_id` (`org_id`),
  KEY `idx_project_org` (`project_id`,`org_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='项目与组织关联表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_pm_scene`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_pm_scene` (
  `id` int NOT NULL AUTO_INCREMENT,
  `types` varchar(50) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL COMMENT '分类',
  `name` varchar(50) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL COMMENT '场景名称',
  `admin_id` int NOT NULL COMMENT '用户ID',
  `weigh` int NOT NULL DEFAULT '1' COMMENT '权重',
  `data` text CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci COMMENT '属性值',
  `type` tinyint(1) NOT NULL DEFAULT '0' COMMENT '1系统0自定义',
  `bydata` varchar(50) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL DEFAULT '' COMMENT '系统参数',
  `create_time` int NOT NULL COMMENT '创建时间',
  `update_time` int NOT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='场景';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_pm_setting`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_pm_setting` (
  `id` int unsigned NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `category` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '' COMMENT '设置分类：basic基础设置,notification提醒设置,workflow工作流设置',
  `setting_key` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '' COMMENT '设置键名',
  `setting_value` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci COMMENT '设置值',
  `setting_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT '' COMMENT '设置名称',
  `setting_desc` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT '' COMMENT '设置描述',
  `value_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT 'string' COMMENT '值类型：string,number,boolean,json',
  `org_id` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '组织ID',
  `create_time` int DEFAULT NULL COMMENT '创建时间',
  `update_time` int DEFAULT NULL COMMENT '更新时间',
  `create_user_id` int DEFAULT NULL COMMENT '创建人ID',
  `create_user_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT '' COMMENT '创建人名称',
  `update_user_id` int DEFAULT NULL COMMENT '更新人ID',
  `update_user_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT '' COMMENT '更新人名称',
  `del_flag` tinyint DEFAULT '0' COMMENT '删除标记：0正常 1已删除',
  `version` int DEFAULT '1' COMMENT '版本号',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_category_key_org` (`category`,`setting_key`,`org_id`),
  KEY `idx_category` (`category`),
  KEY `idx_org_id` (`org_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='项目管理设置表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_pm_setting_revision`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_pm_setting_revision` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `org_id` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `category` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL,
  `revision` bigint NOT NULL DEFAULT '0',
  `update_time` int NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_pm_setting_revision_org_category` (`org_id`,`category`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='task setting category revision';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_pm_statement`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_pm_statement` (
  `id` int NOT NULL AUTO_INCREMENT,
  `admin_id` int DEFAULT NULL,
  `nickname` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `plan_task` int DEFAULT NULL COMMENT '任务数量',
  `complete_task` int DEFAULT NULL COMMENT '完成任务数量',
  `plan_hours` decimal(10,1) DEFAULT NULL COMMENT '安排工时',
  `get_hours` decimal(10,1) DEFAULT NULL COMMENT '或得工时',
  `overdue` int DEFAULT '0' COMMENT '任务逾期数量',
  `achievement` decimal(10,2) DEFAULT '0.00' COMMENT '业绩',
  `year` int DEFAULT NULL COMMENT '年份',
  `month` int DEFAULT NULL COMMENT '月份',
  `create_time` int DEFAULT NULL COMMENT '创建时间',
  `update_time` int DEFAULT NULL COMMENT '更新时间',
  `delete_time` int DEFAULT NULL COMMENT '删除时间',
  `status` tinyint DEFAULT '0' COMMENT '状态1正常0正在生成2待确认',
  `remark` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT '' COMMENT '备注',
  PRIMARY KEY (`id`),
  KEY `year` (`year`),
  KEY `month` (`month`),
  KEY `status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_pm_task`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_pm_task` (
  `id` int NOT NULL AUTO_INCREMENT,
  `org_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `task_no` varchar(40) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'organization task number',
  `name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '任务名称',
  `project_id` int NOT NULL COMMENT '项目ID',
  `pid` int DEFAULT '0' COMMENT '上级任务0为顶级',
  `group_id` int DEFAULT '0' COMMENT '任务归属组',
  `user_ids` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT '' COMMENT '参与人员(只有查看权限)',
  `user_id` int DEFAULT '0' COMMENT '负责人ID',
  `plan_hours` decimal(10,1) DEFAULT '0.0' COMMENT '计划工时',
  `get_hours` decimal(10,1) DEFAULT '0.0' COMMENT '获得工时(要减去子任务)',
  `actual_hours` decimal(10,1) DEFAULT '0.0' COMMENT '实际工时',
  `complete_time` int DEFAULT NULL COMMENT '完成时间',
  `create_time` int DEFAULT NULL COMMENT '创建时间',
  `update_time` int DEFAULT '0' COMMENT '更新时间',
  `start_time` int DEFAULT NULL COMMENT '开始日期',
  `end_time` int DEFAULT '0' COMMENT '截止日期',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci COMMENT '任务说明',
  `progress` tinyint DEFAULT '0' COMMENT '进度0~100',
  `status` tinyint DEFAULT '1' COMMENT '状态-1关闭0未开始1进行中2完成',
  `create_admin_id` int DEFAULT NULL COMMENT '创建人',
  `level` tinyint DEFAULT '1' COMMENT '优先级1无2不重要3次要4主要5紧急',
  `actual_stime` int DEFAULT '0' COMMENT '实际开始时间',
  `sync_wecom` tinyint NOT NULL DEFAULT '0' COMMENT '是否同步到企业微信群：0否 1是',
  `wecom_webhook` varchar(512) COLLATE utf8mb4_unicode_ci DEFAULT '' COMMENT '企业微信群机器人 Webhook 链接',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_pm_task_org_no` (`org_id`,`task_no`),
  KEY `project_id` (`project_id`),
  KEY `admin_id` (`user_id`),
  KEY `idx_pm_task_statistics` (`project_id`,`status`,`user_id`,`create_time`,`complete_time`,`end_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='任务列表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_pm_task_assign_cursor`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_pm_task_assign_cursor` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `org_id` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `project_id` int NOT NULL,
  `last_user_id` int DEFAULT NULL,
  `update_time` int NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_pm_task_assign_cursor_org_project` (`org_id`,`project_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='project automatic assignment cursor';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_pm_task_sequence`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_pm_task_sequence` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `org_id` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `business_type` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL,
  `business_date` date NOT NULL,
  `current_value` int NOT NULL DEFAULT '0',
  `update_time` int NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_pm_task_sequence_business_date` (`org_id`,`business_type`,`business_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='organization business number sequence';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_pm_team`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_pm_team` (
  `id` int NOT NULL AUTO_INCREMENT,
  `team_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL,
  `project_id` int DEFAULT NULL,
  `create_time` int DEFAULT NULL,
  `update_time` int DEFAULT NULL,
  `delete_time` int DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='项目团队';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_pm_test_audit_page`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_pm_test_audit_page` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `org_id` varchar(64) NOT NULL DEFAULT '1',
  `release_id` bigint NOT NULL,
  `module_id` bigint DEFAULT NULL,
  `terminal` varchar(32) DEFAULT NULL,
  `page_url` varchar(1024) NOT NULL,
  `page_title` varchar(256) DEFAULT NULL,
  `open_result` varchar(128) DEFAULT NULL COMMENT '打开结果描述',
  `audit_status` varchar(32) NOT NULL DEFAULT 'pass' COMMENT 'pass/partial/fail/need_login',
  `inventory_json` mediumtext COMMENT '元素清单JSON',
  `interactions_json` mediumtext COMMENT '交互结果JSON',
  `errors_json` mediumtext COMMENT '报错明细JSON',
  `screenshot_url` varchar(1024) DEFAULT NULL,
  `remark` varchar(512) DEFAULT NULL,
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `del_flag` tinyint NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `idx_release_status` (`org_id`,`release_id`,`audit_status`,`del_flag`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='PM全量页面审计';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_pm_test_case`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_pm_test_case` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `org_id` varchar(64) NOT NULL DEFAULT '1',
  `release_id` bigint NOT NULL COMMENT '测试版本ID',
  `case_no` varchar(32) DEFAULT NULL COMMENT '用例编号',
  `title` varchar(256) NOT NULL COMMENT '用例标题',
  `module_name` varchar(128) DEFAULT NULL COMMENT '模块',
  `priority` tinyint NOT NULL DEFAULT '2' COMMENT '1高 2中 3低',
  `case_type` varchar(32) NOT NULL DEFAULT 'functional' COMMENT 'functional/boundary/exception/permission/flow',
  `preconditions` text COMMENT '前置条件',
  `steps` text COMMENT '测试步骤',
  `expected_result` text COMMENT '预期结果',
  `automation` tinyint NOT NULL DEFAULT '0' COMMENT '是否自动化',
  `status` varchar(32) NOT NULL DEFAULT 'draft' COMMENT 'draft/ready/review',
  `last_result` varchar(16) DEFAULT NULL COMMENT 'pass/fail/block/skip',
  `opentest_test_path` varchar(256) DEFAULT NULL COMMENT 'OpenTest tests 子目录路径',
  `opentest_test_name` varchar(256) DEFAULT NULL COMMENT 'OpenTest 测试文件名(无扩展名)',
  `opentest_session_id` bigint DEFAULT NULL COMMENT '关联 OpenTest 会话 ID',
  `create_user_id` bigint DEFAULT NULL,
  `create_user_name` varchar(64) DEFAULT NULL,
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `del_flag` tinyint NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `idx_release` (`release_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='测试用例';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_pm_test_defect`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_pm_test_defect` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `org_id` varchar(64) NOT NULL DEFAULT '1',
  `release_id` bigint NOT NULL,
  `case_id` bigint DEFAULT NULL,
  `defect_no` varchar(32) DEFAULT NULL,
  `title` varchar(256) NOT NULL,
  `severity` tinyint NOT NULL DEFAULT '2' COMMENT '1致命 2严重 3一般 4轻微',
  `priority` tinyint NOT NULL DEFAULT '2' COMMENT '1高 2中 3低',
  `status` varchar(32) NOT NULL DEFAULT 'open' COMMENT 'open/fixing/fixed/verify/closed/rejected',
  `steps` text COMMENT '复现步骤',
  `expected_result` text,
  `actual_result` text,
  `env` varchar(128) DEFAULT NULL,
  `assignee_id` bigint DEFAULT NULL,
  `assignee_name` varchar(64) DEFAULT NULL,
  `reporter_id` bigint DEFAULT NULL,
  `reporter_name` varchar(64) DEFAULT NULL,
  `fix_note` text COMMENT '修复说明',
  `verify_note` text COMMENT '验证说明',
  `verify_result` varchar(16) DEFAULT NULL COMMENT 'pass/fail',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `del_flag` tinyint NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `idx_release_status` (`release_id`,`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='测试缺陷';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_pm_test_document`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_pm_test_document` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `org_id` varchar(64) NOT NULL DEFAULT '1',
  `release_id` bigint NOT NULL,
  `doc_type` varchar(32) NOT NULL COMMENT 'requirement_review/test_plan/test_scope/risk/smoke_check/regression/release_check/acceptance/summary',
  `title` varchar(256) NOT NULL,
  `content` text COMMENT '正文(JSON或Markdown)',
  `scope_desc` text COMMENT '测试范围',
  `risk_list` text COMMENT '风险清单',
  `strategy` text COMMENT '测试策略',
  `pass_rate` decimal(5,2) DEFAULT NULL,
  `coverage_rate` decimal(5,2) DEFAULT NULL,
  `create_user_id` bigint DEFAULT NULL,
  `create_user_name` varchar(64) DEFAULT NULL,
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `del_flag` tinyint NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `idx_release_type` (`release_id`,`doc_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='测试文档(评审/计划/报告)';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_pm_test_execution`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_pm_test_execution` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `org_id` varchar(64) NOT NULL DEFAULT '1',
  `release_id` bigint NOT NULL,
  `case_id` bigint DEFAULT NULL,
  `exec_stage` varchar(32) NOT NULL COMMENT 'smoke/functional/regression/exploratory',
  `exec_type` varchar(32) DEFAULT NULL COMMENT 'manual/api/ui',
  `result` varchar(16) NOT NULL COMMENT 'pass/fail/block/skip',
  `actual_result` text,
  `executor_id` bigint DEFAULT NULL,
  `executor_name` varchar(64) DEFAULT NULL,
  `exec_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `env` varchar(128) DEFAULT NULL,
  `remark` text,
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `del_flag` tinyint NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `idx_release_stage` (`release_id`,`exec_stage`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='测试执行记录';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_pm_test_module`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_pm_test_module` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `org_id` varchar(64) NOT NULL DEFAULT '1',
  `release_id` bigint NOT NULL,
  `terminal` varchar(32) NOT NULL COMMENT 'admin/bcenter/supplier/pc',
  `module_name` varchar(128) NOT NULL COMMENT '模块名称',
  `page_count` int DEFAULT '0' COMMENT '页面数量',
  `prd_link` varchar(512) DEFAULT NULL COMMENT 'PRD链接或路径',
  `remark` varchar(512) DEFAULT NULL,
  `sort_order` int DEFAULT '0',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `del_flag` tinyint NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `idx_release_terminal` (`org_id`,`release_id`,`terminal`,`del_flag`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='PM待测模块清单';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_pm_test_release`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_pm_test_release` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '测试版本ID',
  `org_id` varchar(64) NOT NULL DEFAULT '1' COMMENT '组织ID',
  `project_id` int DEFAULT NULL COMMENT '关联项目ID',
  `release_name` varchar(128) NOT NULL COMMENT '版本/迭代名称',
  `version_code` varchar(64) DEFAULT NULL COMMENT '版本号',
  `current_stage` varchar(32) NOT NULL DEFAULT 'requirement_review' COMMENT '当前阶段',
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '1进行中 2已完成 0暂停',
  `submit_test_time` datetime DEFAULT NULL COMMENT '提测时间',
  `smoke_pass` tinyint NOT NULL DEFAULT '0' COMMENT '冒烟是否通过',
  `entry_pass` tinyint NOT NULL DEFAULT '0' COMMENT '准入是否通过',
  `plan_submit_date` date DEFAULT NULL COMMENT '计划提测日',
  `regression_date` date DEFAULT NULL COMMENT '计划回归日',
  `release_date` date DEFAULT NULL COMMENT '计划上线日',
  `env_url` varchar(512) DEFAULT NULL COMMENT '测试环境地址',
  `env_account` varchar(256) DEFAULT NULL COMMENT '测试账号说明',
  `prd_url` varchar(512) DEFAULT NULL COMMENT 'PRD原型地址',
  `e2e_project_path` varchar(256) DEFAULT 'e2e-tests' COMMENT 'e2e工程目录',
  `credentials_json` mediumtext COMMENT '各端测试账号JSON',
  `preflight_status` mediumtext COMMENT 'preflight:auth各端结果JSON',
  `audit_run_at` datetime DEFAULT NULL COMMENT '最近全量审计时间',
  `workflow_meta_json` mediumtext COMMENT 'OpenTest工作流步骤元数据JSON',
  `opentest_session_id` bigint DEFAULT NULL COMMENT '最近 OpenTest 会话 ID',
  `remark` text COMMENT '备注',
  `create_user_id` bigint DEFAULT NULL,
  `create_user_name` varchar(64) DEFAULT NULL,
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `del_flag` tinyint NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `idx_org_project` (`org_id`,`project_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='测试版本/迭代';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_pm_user`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_pm_user` (
  `id` int NOT NULL AUTO_INCREMENT,
  `truename` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '真实姓名',
  `user_id` bigint DEFAULT NULL COMMENT '会员ID，默认存hou_user_info的',
  `team_id` int DEFAULT NULL COMMENT '团队ID',
  `org_id` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '组织ID',
  `jobs_id` int DEFAULT NULL COMMENT '岗位ID',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_org_id` (`org_id`),
  KEY `idx_org_user` (`org_id`,`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='项目人员';
/*!40101 SET character_set_client = @saved_cs_client */;

--

-- ---------- plugins/pm/runtime/resources/sql/2026-07-23-task-statistics-settings-phase1.sql ----------
-- Task statistics and settings phase 1. MySQL 8, safe to execute repeatedly.
-- [自动建表] 此处原有的 DELIMITER 存储过程块已剥离：该执行器按 ";" 切分语句、不支持 DELIMITER；
-- 块内的迁移过程（断言 / 动态 ADD COLUMN 等）源码见 plugins/<code>/runtime/resources/sql/，需要时手工执行。

-- ---------- plugins/pm/runtime/resources/sql/2026-07-24-task-notification-phase2.sql ----------
-- Task notification phase 2. MySQL 8, safe to execute repeatedly.
-- [自动建表] 此处原有的 DELIMITER 存储过程块已剥离：该执行器按 ";" 切分语句、不支持 DELIMITER；
-- 块内的迁移过程（断言 / 动态 ADD COLUMN 等）源码见 plugins/<code>/runtime/resources/sql/，需要时手工执行。

-- ---------- plugins/pm/runtime/resources/sql/2026-08-04-project-after-sales-period.sql ----------
-- Project after-sales period. MySQL 8, safe to execute repeatedly.
-- [自动建表] 此处原有的 DELIMITER 存储过程块已剥离：该执行器按 ";" 切分语句、不支持 DELIMITER；
-- 块内的迁移过程（断言 / 动态 ADD COLUMN 等）源码见 plugins/<code>/runtime/resources/sql/，需要时手工执行。

SET FOREIGN_KEY_CHECKS=1;
