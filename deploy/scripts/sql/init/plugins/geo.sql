-- GEO independent installation baseline, MySQL 8.0+.
-- Extracted from tracked 10-meoc-schema.sql (structure only, 2026-09-24).
-- Contains only GEO-owned tables; never import shared AI/base/permission tables.
SET NAMES utf8mb4;
SET @GEO_OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS;
SET FOREIGN_KEY_CHECKS=0;

CREATE TABLE IF NOT EXISTS `hou_geo_ab_experiment` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint unsigned NOT NULL DEFAULT '0',
  `project_id` bigint unsigned NOT NULL DEFAULT '0',
  `name` varchar(128) COLLATE utf8mb4_unicode_ci NOT NULL,
  `hypothesis` varchar(512) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `status` varchar(32) COLLATE utf8mb4_unicode_ci DEFAULT 'running',
  `start_date` date DEFAULT NULL,
  `end_date` date DEFAULT NULL,
  `winner_variant` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `result_json` json DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_tenant_status` (`tenant_id`,`status`),
  KEY `idx_tenant_project_status` (`tenant_id`,`project_id`,`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='A/B实验';

CREATE TABLE IF NOT EXISTS `hou_geo_ai_balance` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `subject_type` varchar(16) NOT NULL COMMENT 'tenant=登录用户 guest=访客',
  `subject_key` varchar(64) NOT NULL COMMENT 'tenant userId 或 guest id',
  `free_quota_tokens` bigint NOT NULL DEFAULT '100000' COMMENT '赠送 Token 额度',
  `free_used_tokens` bigint NOT NULL DEFAULT '0',
  `free_quota_started_at` datetime DEFAULT NULL,
  `free_quota_expires_at` datetime DEFAULT NULL,
  `used_tokens` bigint NOT NULL DEFAULT '0' COMMENT '已消耗 Token',
  `purchased_balance_tokens` bigint NOT NULL DEFAULT '0',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_subject` (`subject_type`,`subject_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='GEO AI 免费额度';

CREATE TABLE IF NOT EXISTS `hou_geo_ai_discoverability` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint unsigned NOT NULL DEFAULT '0',
  `project_id` bigint unsigned DEFAULT NULL,
  `page_url` varchar(512) COLLATE utf8mb4_unicode_ci NOT NULL,
  `score` int DEFAULT NULL,
  `gaps_json` json DEFAULT NULL COMMENT '多模态/结构化缺口',
  `multimodal_json` json DEFAULT NULL COMMENT 'alt/subtitle/transcript缺失',
  `checked_at` datetime DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_tenant_url` (`tenant_id`,`page_url`(191))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='AI可发现性检测(B3.2)';

CREATE TABLE IF NOT EXISTS `hou_geo_ai_model` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `code` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '模型编码 chatgpt/deepseek/wenxin',
  `name` varchar(128) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '显示名称',
  `region` varchar(32) COLLATE utf8mb4_unicode_ci DEFAULT 'domestic' COMMENT 'domestic/global',
  `vendor` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '0禁用 1启用',
  `sort_order` int NOT NULL DEFAULT '0',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_code` (`code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='GEO监测AI引擎目录';

CREATE TABLE IF NOT EXISTS `hou_geo_ai_model_changelog` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `ai_model_id` bigint unsigned NOT NULL,
  `change_date` date NOT NULL,
  `change_type` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `summary` varchar(512) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `impact_level` varchar(32) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `detail_json` json DEFAULT NULL,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_model_date` (`ai_model_id`,`change_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='AI模型变更日志';

CREATE TABLE IF NOT EXISTS `hou_geo_ai_model_profile` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint unsigned NOT NULL DEFAULT '0',
  `project_id` bigint unsigned DEFAULT NULL,
  `ai_model_id` bigint unsigned NOT NULL,
  `geo_score` int DEFAULT NULL COMMENT 'GEO适配分',
  `profile_json` json DEFAULT NULL COMMENT '能力画像28.3',
  `diagnosis_json` json DEFAULT NULL COMMENT '品牌诊断28.4',
  `refreshed_at` datetime DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_tenant_model` (`tenant_id`,`project_id`,`ai_model_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='模型GEO画像与诊断';

CREATE TABLE IF NOT EXISTS `hou_geo_ai_snippet` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint unsigned NOT NULL DEFAULT '0',
  `project_id` bigint unsigned NOT NULL DEFAULT '0',
  `page_url` varchar(512) COLLATE utf8mb4_unicode_ci NOT NULL,
  `snippet_text` varchar(1024) COLLATE utf8mb4_unicode_ci NOT NULL,
  `cite_score` int DEFAULT NULL,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_tenant_url` (`tenant_id`,`page_url`(191)),
  KEY `idx_project_url` (`tenant_id`,`project_id`,`page_url`(191))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='可引用片段库';

CREATE TABLE IF NOT EXISTS `hou_geo_ai_studio_expand_batch` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '批次 ID',
  `tenant_id` bigint unsigned NOT NULL DEFAULT '0' COMMENT '租户 ID',
  `project_id` bigint unsigned DEFAULT NULL COMMENT '项目 ID',
  `seed` varchar(256) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '种子关键词',
  `mode` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'ai' COMMENT '扩展模式 code',
  `industry` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'geo' COMMENT '行业',
  `models_json` json DEFAULT NULL COMMENT 'AI 模型列表 JSON 数组',
  `source` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'dify' COMMENT 'dify|suffix|suffix_fallback|cached',
  `scene` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'expand' COMMENT 'load=页面加载 expand=手动扩展',
  `keyword_count` int NOT NULL DEFAULT '0' COMMENT '本批次关键词数',
  `dify_message` varchar(512) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'Dify 异常/提示摘要',
  `create_by` bigint unsigned DEFAULT NULL COMMENT '操作人',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `is_deleted` tinyint NOT NULL DEFAULT '0' COMMENT '0正常 1删除',
  PRIMARY KEY (`id`),
  KEY `idx_tenant_project_time` (`tenant_id`,`project_id`,`create_time`),
  KEY `idx_tenant_seed_mode` (`tenant_id`,`project_id`,`seed`(64),`mode`,`industry`,`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='AI创作·关键词扩展批次';

CREATE TABLE IF NOT EXISTS `hou_geo_ai_studio_keyword` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '明细 ID',
  `batch_id` bigint unsigned NOT NULL COMMENT '批次 ID',
  `tenant_id` bigint unsigned NOT NULL DEFAULT '0' COMMENT '租户 ID',
  `project_id` bigint unsigned DEFAULT NULL COMMENT '项目 ID',
  `keyword` varchar(256) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '关键词',
  `template` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '意图模板：指南型/对比型等',
  `opportunity` int DEFAULT NULL COMMENT '机会指数 60-95',
  `difficulty` int DEFAULT NULL COMMENT '难度 35-58',
  `trend` varchar(16) COLLATE utf8mb4_unicode_ci DEFAULT 'stable' COMMENT '趋势 up|stable|down',
  `covered` tinyint NOT NULL DEFAULT '0' COMMENT '是否已覆盖',
  `cover_status` varchar(16) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'gap' COMMENT 'gap|in_progress|covered',
  `sort_order` int NOT NULL DEFAULT '0' COMMENT '排序',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `is_deleted` tinyint NOT NULL DEFAULT '0' COMMENT '0正常 1删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_batch_keyword` (`batch_id`,`keyword`(191)),
  KEY `idx_batch_sort` (`batch_id`,`sort_order`),
  KEY `idx_tenant_keyword` (`tenant_id`,`project_id`,`keyword`(64))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='AI创作·关键词扩展明细';

CREATE TABLE IF NOT EXISTS `hou_geo_aliyun_digital_human_resource` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint unsigned NOT NULL DEFAULT '0',
  `local_asset_id` bigint unsigned DEFAULT NULL,
  `provider` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'aliyun-avatar',
  `resource_type` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `external_resource_id` varchar(128) COLLATE utf8mb4_unicode_ci NOT NULL,
  `name` varchar(256) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `status` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'PENDING',
  `error_message` varchar(2000) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `metadata_json` json DEFAULT NULL,
  `synced_at` datetime DEFAULT NULL,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_tenant_provider_external_resource` (`tenant_id`,`provider`,`external_resource_id`),
  KEY `idx_tenant_resource_type_status` (`tenant_id`,`resource_type`,`status`),
  KEY `idx_local_asset` (`local_asset_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='阿里云灵眸数字人资源索引';

CREATE TABLE IF NOT EXISTS `hou_geo_api_key` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint unsigned NOT NULL DEFAULT '0',
  `project_id` bigint unsigned NOT NULL DEFAULT '0',
  `name` varchar(128) COLLATE utf8mb4_unicode_ci NOT NULL,
  `api_key_hash` varchar(128) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'Key哈希',
  `scopes_json` json DEFAULT NULL,
  `expire_at` datetime DEFAULT NULL,
  `last_used_at` datetime DEFAULT NULL,
  `status` varchar(32) COLLATE utf8mb4_unicode_ci DEFAULT 'active',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_tenant_status` (`tenant_id`,`status`),
  KEY `idx_tenant_project_status` (`tenant_id`,`project_id`,`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='开放API Key';

CREATE TABLE IF NOT EXISTS `hou_geo_article` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint unsigned NOT NULL DEFAULT '0',
  `project_id` bigint unsigned DEFAULT NULL,
  `org_id` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '组织ID，路由正文附表',
  `title` varchar(256) COLLATE utf8mb4_unicode_ci NOT NULL,
  `topic` varchar(256) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '关键词/主题',
  `content_type` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '文章/Q&A/视频',
  `studio_keyword_id` bigint unsigned DEFAULT NULL COMMENT 'AI创作关键词ID',
  `expand_batch_id` bigint unsigned DEFAULT NULL COMMENT '扩词批次ID',
  `meta_json` json DEFAULT NULL COMMENT 'AI生成元数据',
  `body` mediumtext COLLATE utf8mb4_unicode_ci,
  `status` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'draft' COMMENT 'draft/pending/published/failed',
  `adapt_status` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'none' COMMENT 'none/previewed/ready',
  `preview_batch_no` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '最近 Dify 预览批次',
  `settings_id` bigint unsigned DEFAULT NULL COMMENT '创作指令模板',
  `distribute_post_id` bigint unsigned DEFAULT NULL COMMENT '关联分发记录',
  `platforms_json` json DEFAULT NULL COMMENT '目标平台',
  `published_at` datetime DEFAULT NULL,
  `fail_reason` varchar(512) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `stats_json` json DEFAULT NULL,
  `create_by` bigint unsigned DEFAULT NULL,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_deleted` tinyint NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `idx_tenant_status` (`tenant_id`,`project_id`,`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='文章全生命周期(15.7)';

CREATE TABLE IF NOT EXISTS `hou_geo_article_1` (
  `article_id` bigint unsigned NOT NULL COMMENT '主表 hou_geo_article.id',
  `body` mediumtext COLLATE utf8mb4_unicode_ci,
  `platforms_json` json DEFAULT NULL,
  `fail_reason` varchar(512) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `stats_json` json DEFAULT NULL,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`article_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='文章正文附表(按组织分表)';

CREATE TABLE IF NOT EXISTS `hou_geo_article_platform_publish` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint unsigned NOT NULL,
  `project_id` bigint unsigned NOT NULL,
  `article_id` bigint unsigned NOT NULL,
  `platform` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `platform_account_id` bigint unsigned DEFAULT NULL,
  `status` varchar(24) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'pending',
  `published_at` datetime DEFAULT NULL,
  `publish_link` varchar(1024) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `external_post_id` varchar(256) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `omni_job_id` bigint unsigned DEFAULT NULL,
  `omni_job_item_id` bigint unsigned DEFAULT NULL,
  `error_message` varchar(2048) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `last_attempt_at` datetime DEFAULT NULL,
  `attempt_count` int NOT NULL DEFAULT '0',
  `create_by` bigint unsigned DEFAULT NULL,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_geo_article_platform_publish` (`tenant_id`,`project_id`,`article_id`,`platform`),
  KEY `idx_geo_article_platform_publish_article` (`tenant_id`,`project_id`,`article_id`),
  KEY `idx_geo_article_platform_publish_status` (`tenant_id`,`project_id`,`status`,`update_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='GEO鏂囩珷骞冲彴鍙戝竷鍙版查';

CREATE TABLE IF NOT EXISTS `hou_geo_article_settings` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint unsigned NOT NULL DEFAULT '0',
  `project_id` bigint unsigned DEFAULT NULL,
  `name` varchar(128) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '指令模板名称',
  `is_default` tinyint NOT NULL DEFAULT '0',
  `tone` varchar(32) COLLATE utf8mb4_unicode_ci DEFAULT '严谨' COMMENT '语气',
  `structure` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT 'conclusion_first' COMMENT '文章结构',
  `word_count` int DEFAULT '1500' COMMENT '目标篇幅',
  `entity_inject_json` json DEFAULT NULL COMMENT '实体词注入',
  `geo_rules_json` json DEFAULT NULL COMMENT 'GEO检测规则',
  `default_channels_json` json DEFAULT NULL COMMENT '默认发布渠道',
  `knowledge_ids_json` json DEFAULT NULL COMMENT '关联知识库条目',
  `create_by` bigint unsigned DEFAULT NULL,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_deleted` tinyint NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `idx_tenant_default` (`tenant_id`,`project_id`,`is_default`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='AI创作指令/文章设置(15.6)';

CREATE TABLE IF NOT EXISTS `hou_geo_attribution_record` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint unsigned NOT NULL DEFAULT '0',
  `project_id` bigint unsigned DEFAULT NULL,
  `factor` varchar(128) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'Schema/内容/外链',
  `contribution_pct` decimal(6,2) DEFAULT NULL,
  `roi` decimal(8,2) DEFAULT NULL,
  `metrics` varchar(256) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `actions` varchar(512) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `stat_period` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_tenant_period` (`tenant_id`,`stat_period`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='效果归因';

CREATE TABLE IF NOT EXISTS `hou_geo_authority_citation` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint unsigned NOT NULL DEFAULT '0',
  `project_id` bigint unsigned DEFAULT NULL,
  `page_url` varchar(512) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `citation_type` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `scan_result_json` json DEFAULT NULL,
  `scan_at` datetime DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_tenant_scan` (`tenant_id`,`scan_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='权威引用扫描';

CREATE TABLE IF NOT EXISTS `hou_geo_backlink` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint unsigned NOT NULL DEFAULT '0',
  `project_id` bigint unsigned DEFAULT NULL,
  `domain` varchar(256) COLLATE utf8mb4_unicode_ci NOT NULL,
  `target_url` varchar(512) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `link_count` int DEFAULT '0',
  `quality_score` int DEFAULT NULL,
  `link_type` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `change_type` varchar(32) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'new/lost',
  `stat_date` date DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_tenant_domain` (`tenant_id`,`domain`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='反向链接';

CREATE TABLE IF NOT EXISTS `hou_geo_brand_health_snapshot` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint unsigned NOT NULL DEFAULT '0',
  `project_id` bigint unsigned DEFAULT NULL,
  `health_score` int DEFAULT NULL,
  `dimensions_json` json DEFAULT NULL COMMENT '能见度/引用/情感/技术',
  `issues_json` json DEFAULT NULL,
  `stat_date` date NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_tenant_date` (`tenant_id`,`project_id`,`stat_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='品牌健康度快照(模块8)';

CREATE TABLE IF NOT EXISTS `hou_geo_brand_product` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint unsigned NOT NULL,
  `project_id` bigint unsigned NOT NULL,
  `product_name` varchar(128) COLLATE utf8mb4_unicode_ci NOT NULL,
  `normalized_name` varchar(128) COLLATE utf8mb4_unicode_ci NOT NULL,
  `description` text COLLATE utf8mb4_unicode_ci,
  `source_type` varchar(16) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'manual' COMMENT 'manual/ai',
  `source_url` varchar(512) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_deleted` tinyint NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_geo_brand_product_name` (`tenant_id`,`project_id`,`normalized_name`,`is_deleted`),
  KEY `idx_geo_brand_product_project` (`tenant_id`,`project_id`,`is_deleted`,`update_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='GEO品牌产品系列';

CREATE TABLE IF NOT EXISTS `hou_geo_brand_variant` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint unsigned NOT NULL DEFAULT '0',
  `project_id` bigint unsigned DEFAULT NULL,
  `variant_text` varchar(128) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '品牌变体/别名',
  `normalized_variant` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '去重用规范化别名',
  `variant_type` varchar(32) COLLATE utf8mb4_unicode_ci DEFAULT 'alias' COMMENT 'alias/abbrev/misspell',
  `create_by` bigint unsigned DEFAULT NULL,
  `update_by` bigint unsigned DEFAULT NULL,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_deleted` tinyint NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_tenant_project_type_variant` (`tenant_id`,`project_id`,`variant_type`,`normalized_variant`),
  KEY `idx_tenant_project` (`tenant_id`,`project_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='品牌变体词典';

CREATE TABLE IF NOT EXISTS `hou_geo_china_map_poi` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint unsigned NOT NULL DEFAULT '0',
  `project_id` bigint unsigned DEFAULT NULL,
  `store_id` bigint unsigned DEFAULT NULL,
  `binding_id` bigint unsigned DEFAULT NULL,
  `platform` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'baidu/amap',
  `poi_id` varchar(128) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `poi_name` varchar(256) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `claim_status` varchar(32) COLLATE utf8mb4_unicode_ci DEFAULT 'unclaimed' COMMENT 'unclaimed/claimed/pending',
  `map_rank` int DEFAULT NULL,
  `ai_cited` tinyint DEFAULT '0' COMMENT '是否被本地AI摘要引用',
  `sync_at` datetime DEFAULT NULL,
  `address` varchar(512) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `phone` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `website` varchar(512) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `category` varchar(128) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `business_hours_json` text COLLATE utf8mb4_unicode_ci,
  `source_type` varchar(16) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'import',
  `raw_json` longtext COLLATE utf8mb4_unicode_ci,
  `external_updated_at` datetime DEFAULT NULL,
  `sync_job_id` bigint unsigned DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_store_platform` (`store_id`,`platform`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='百度/高德POI(29.3)';

CREATE TABLE IF NOT EXISTS `hou_geo_citation` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint unsigned NOT NULL DEFAULT '0',
  `project_id` bigint unsigned DEFAULT NULL,
  `snippet` varchar(1024) COLLATE utf8mb4_unicode_ci NOT NULL,
  `source_url` varchar(512) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `domain` varchar(256) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `platform` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'AI模型',
  `quality` varchar(32) COLLATE utf8mb4_unicode_ci DEFAULT 'core' COMMENT 'core/edge/noise',
  `cite_count` int DEFAULT '1',
  `first_seen_at` datetime DEFAULT NULL,
  `last_seen_at` datetime DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_tenant_domain` (`tenant_id`,`domain`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='AI引用片段';

CREATE TABLE IF NOT EXISTS `hou_geo_citation_domain_stat` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint unsigned NOT NULL DEFAULT '0',
  `project_id` bigint unsigned DEFAULT NULL,
  `domain` varchar(256) COLLATE utf8mb4_unicode_ci NOT NULL,
  `domain_type` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `cite_count` int DEFAULT '0',
  `ratio` decimal(6,2) DEFAULT NULL,
  `stat_date` date DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_tenant_date` (`tenant_id`,`stat_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='引用域名统计';

CREATE TABLE IF NOT EXISTS `hou_geo_commerce_order` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint unsigned NOT NULL DEFAULT '0',
  `product_id` bigint unsigned DEFAULT NULL,
  `order_no` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `amount` decimal(10,2) NOT NULL,
  `pay_status` varchar(32) COLLATE utf8mb4_unicode_ci DEFAULT 'pending' COMMENT 'pending/paid/refunded',
  `pay_time` datetime DEFAULT NULL,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_order_no` (`order_no`),
  KEY `idx_tenant_pay` (`tenant_id`,`pay_status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='SaaS订单(30.3)';

CREATE TABLE IF NOT EXISTS `hou_geo_commerce_product` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint unsigned NOT NULL DEFAULT '0',
  `name` varchar(128) COLLATE utf8mb4_unicode_ci NOT NULL,
  `sku_code` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `quota_json` json DEFAULT NULL COMMENT '蒸馏词/创作/投放额度',
  `price` decimal(10,2) DEFAULT NULL,
  `status` varchar(32) COLLATE utf8mb4_unicode_ci DEFAULT 'active',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_tenant_status` (`tenant_id`,`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='SaaS商品套餐(30.2)';

CREATE TABLE IF NOT EXISTS `hou_geo_competitor` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint unsigned NOT NULL DEFAULT '0',
  `project_id` bigint unsigned DEFAULT NULL,
  `brand_name` varchar(128) COLLATE utf8mb4_unicode_ci NOT NULL,
  `domain` varchar(256) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `ai_model_ids` json DEFAULT NULL,
  `selling_points` text COLLATE utf8mb4_unicode_ci COMMENT '主要竞争点',
  `aliases_json` json DEFAULT NULL COMMENT '相似名称和别名',
  `source_type` varchar(16) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'manual' COMMENT 'manual/ai',
  `source_url` varchar(512) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '识别来源网址',
  `is_self` tinyint NOT NULL DEFAULT '0',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_deleted` tinyint NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `idx_tenant_project` (`tenant_id`,`project_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='竞品品牌';

CREATE TABLE IF NOT EXISTS `hou_geo_competitor_metric` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint unsigned NOT NULL DEFAULT '0',
  `project_id` bigint unsigned DEFAULT NULL,
  `competitor_id` bigint unsigned NOT NULL,
  `metric_key` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'SOV/avg_rank/citations',
  `metric_value` decimal(12,4) DEFAULT NULL,
  `stat_date` date DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_competitor_metric` (`competitor_id`,`metric_key`,`stat_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='竞品指标';

CREATE TABLE IF NOT EXISTS `hou_geo_content_asset` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint unsigned NOT NULL DEFAULT '0',
  `project_id` bigint unsigned DEFAULT NULL,
  `name` varchar(256) COLLATE utf8mb4_unicode_ci NOT NULL,
  `asset_type` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'article/image/video/audio',
  `tags_json` json DEFAULT NULL,
  `file_url` varchar(512) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `freshness_score` int DEFAULT NULL,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `is_deleted` tinyint NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `idx_tenant_type` (`tenant_id`,`asset_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='内容素材库';

CREATE TABLE IF NOT EXISTS `hou_geo_content_asset_version` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `asset_id` bigint unsigned NOT NULL,
  `version_label` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `article_body` mediumtext COLLATE utf8mb4_unicode_ci,
  `schema_json` json DEFAULT NULL,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_asset` (`asset_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='素材版本';

CREATE TABLE IF NOT EXISTS `hou_geo_content_generate_job` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint unsigned NOT NULL DEFAULT '0',
  `project_id` bigint unsigned DEFAULT NULL,
  `job_no` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'genjob_xxx',
  `status` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'processing' COMMENT 'processing/done/partial_failed/failed',
  `total_count` int NOT NULL DEFAULT '0',
  `success_count` int NOT NULL DEFAULT '0',
  `failed_count` int NOT NULL DEFAULT '0',
  `expand_batch_id` bigint unsigned DEFAULT NULL,
  `config_json` json DEFAULT NULL COMMENT 'model/type/mode/industry/videoConfig',
  `operator_id` bigint unsigned DEFAULT NULL,
  `error_msg` varchar(512) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `finished_at` datetime DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_job_no` (`job_no`),
  KEY `idx_tenant_status` (`tenant_id`,`project_id`,`status`,`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='AI创作批量生成任务';

CREATE TABLE IF NOT EXISTS `hou_geo_content_generate_job_item` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `job_id` bigint unsigned NOT NULL,
  `sort_order` int NOT NULL DEFAULT '0',
  `keyword` varchar(256) COLLATE utf8mb4_unicode_ci NOT NULL,
  `template` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `studio_keyword_id` bigint unsigned DEFAULT NULL,
  `status` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'pending' COMMENT 'pending/processing/success/failed',
  `article_id` bigint unsigned DEFAULT NULL COMMENT '关联文章 hou_geo_article.id',
  `error_msg` varchar(512) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `started_at` datetime DEFAULT NULL,
  `finished_at` datetime DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_job_status` (`job_id`,`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='AI创作批量生成子项';

CREATE TABLE IF NOT EXISTS `hou_geo_content_rewrite` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `draft_id` bigint unsigned NOT NULL,
  `platform` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'xhs/toutiao/douyin等',
  `similarity_pct` int DEFAULT NULL,
  `body` mediumtext COLLATE utf8mb4_unicode_ci,
  `status` varchar(32) COLLATE utf8mb4_unicode_ci DEFAULT 'done',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_draft_platform` (`draft_id`,`platform`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='分平台改写版本';

CREATE TABLE IF NOT EXISTS `hou_geo_conversion_event` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint NOT NULL,
  `project_id` bigint NOT NULL,
  `goal_id` bigint NOT NULL,
  `distill_task_id` bigint DEFAULT NULL,
  `distill_result_id` bigint DEFAULT NULL,
  `content_type` varchar(40) DEFAULT NULL,
  `content_id` bigint DEFAULT NULL,
  `touchpoint_id` bigint DEFAULT NULL,
  `event_code` varchar(80) NOT NULL,
  `event_value` decimal(18,4) DEFAULT NULL,
  `currency` varchar(12) DEFAULT NULL,
  `source_type` varchar(24) NOT NULL COMMENT 'sdk/webhook/csv/admin',
  `occurred_at` datetime NOT NULL,
  `page_url` varchar(1000) DEFAULT NULL,
  `referrer_url` varchar(1000) DEFAULT NULL,
  `utm_source` varchar(120) DEFAULT NULL,
  `utm_medium` varchar(120) DEFAULT NULL,
  `utm_campaign` varchar(200) DEFAULT NULL,
  `utm_content` varchar(200) DEFAULT NULL,
  `utm_term` varchar(200) DEFAULT NULL,
  `session_id` varchar(100) DEFAULT NULL,
  `visitor_id` varchar(100) DEFAULT NULL,
  `contact_name_encrypted` text,
  `contact_phone_encrypted` text,
  `contact_email_encrypted` text,
  `contact_phone_hash` char(64) DEFAULT NULL,
  `contact_email_hash` char(64) DEFAULT NULL,
  `idempotency_key` varchar(120) NOT NULL,
  `external_event_id` varchar(120) DEFAULT NULL,
  `import_batch_id` bigint DEFAULT NULL,
  `properties_json` text,
  `create_time` datetime NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_event_idempotency` (`tenant_id`,`project_id`,`source_type`,`idempotency_key`),
  KEY `idx_event_goal_time` (`tenant_id`,`project_id`,`goal_id`,`occurred_at`),
  KEY `idx_event_task_time` (`tenant_id`,`project_id`,`distill_task_id`,`occurred_at`),
  KEY `idx_event_contact_phone` (`tenant_id`,`project_id`,`contact_phone_hash`),
  KEY `idx_event_contact_email` (`tenant_id`,`project_id`,`contact_email_hash`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='真实转化事件账本';

CREATE TABLE IF NOT EXISTS `hou_geo_conversion_goal` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint unsigned NOT NULL DEFAULT '0',
  `project_id` bigint unsigned DEFAULT NULL,
  `goal_type` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'exposure/indexing/content/distribute/lead',
  `name` varchar(128) COLLATE utf8mb4_unicode_ci NOT NULL,
  `event_code` varchar(80) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '项目内稳定事件代码',
  `target_url` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '目标页面或匹配规则',
  `event_config_json` text COLLATE utf8mb4_unicode_ci COMMENT '事件扩展配置',
  `target_value` decimal(12,4) NOT NULL,
  `current_value` decimal(12,4) DEFAULT '0.0000',
  `unit` varchar(32) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '%/篇/次',
  `deadline` date DEFAULT NULL,
  `status` varchar(32) COLLATE utf8mb4_unicode_ci DEFAULT 'active',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_tenant_type` (`tenant_id`,`project_id`,`goal_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='转化目标(30.1)';

CREATE TABLE IF NOT EXISTS `hou_geo_conversion_import_batch` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint NOT NULL,
  `project_id` bigint NOT NULL,
  `file_name` varchar(255) NOT NULL,
  `source_type` varchar(24) NOT NULL,
  `total_rows` int NOT NULL DEFAULT '0',
  `success_rows` int NOT NULL DEFAULT '0',
  `failed_rows` int NOT NULL DEFAULT '0',
  `error_json` text,
  `status` varchar(24) NOT NULL,
  `create_by` bigint DEFAULT NULL,
  `started_at` datetime NOT NULL,
  `finished_at` datetime DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_import_project_time` (`tenant_id`,`project_id`,`started_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='转化事件 CSV 导入批次';

CREATE TABLE IF NOT EXISTS `hou_geo_conversion_touchpoint` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint NOT NULL,
  `project_id` bigint NOT NULL,
  `session_id` varchar(100) DEFAULT NULL,
  `visitor_id` varchar(100) DEFAULT NULL,
  `distill_task_id` bigint DEFAULT NULL,
  `distill_result_id` bigint DEFAULT NULL,
  `content_type` varchar(40) DEFAULT NULL,
  `content_id` bigint DEFAULT NULL,
  `page_url` varchar(1000) DEFAULT NULL,
  `referrer_url` varchar(1000) DEFAULT NULL,
  `utm_source` varchar(120) DEFAULT NULL,
  `utm_medium` varchar(120) DEFAULT NULL,
  `utm_campaign` varchar(200) DEFAULT NULL,
  `utm_content` varchar(200) DEFAULT NULL,
  `utm_term` varchar(200) DEFAULT NULL,
  `occurred_at` datetime NOT NULL,
  `create_time` datetime NOT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_touch_session` (`tenant_id`,`project_id`,`session_id`,`occurred_at`),
  KEY `idx_touch_content` (`tenant_id`,`project_id`,`content_type`,`content_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='转化归因访问触点';

CREATE TABLE IF NOT EXISTS `hou_geo_crawl_audit` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint unsigned NOT NULL DEFAULT '0',
  `project_id` bigint unsigned NOT NULL DEFAULT '0',
  `page_url` varchar(512) COLLATE utf8mb4_unicode_ci NOT NULL,
  `indexable` tinyint DEFAULT NULL,
  `reason` varchar(256) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `robots_ok` tinyint DEFAULT NULL,
  `audit_type` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'indexability/robots/budget',
  `user_agent` varchar(128) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'Googlebot' COMMENT 'crawler used for audit',
  `audit_at` datetime DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_tenant_url` (`tenant_id`,`page_url`(191)),
  KEY `idx_project_audit` (`tenant_id`,`project_id`,`audit_type`,`audit_at`),
  KEY `idx_project_url_crawler_audit` (`tenant_id`,`project_id`,`audit_type`,`page_url`(120),`user_agent`,`audit_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='爬虫可索引审计';

CREATE TABLE IF NOT EXISTS `hou_geo_crawler_policy` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `platform_or_bot` varchar(128) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'GPTBot/BaiduSpider等',
  `policy_type` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `summary` varchar(512) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `detail_json` json DEFAULT NULL,
  `impact_level` varchar(32) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '高/中/低',
  `effective_date` date DEFAULT NULL,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_bot_date` (`platform_or_bot`,`effective_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='爬虫政策情报(F1)';

CREATE TABLE IF NOT EXISTS `hou_geo_distill_goal_relation` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint NOT NULL,
  `project_id` bigint NOT NULL,
  `distill_task_id` bigint NOT NULL,
  `goal_id` bigint NOT NULL,
  `role` varchar(16) NOT NULL COMMENT 'primary/secondary',
  `create_by` bigint DEFAULT NULL,
  `create_time` datetime NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_distill_goal` (`tenant_id`,`project_id`,`distill_task_id`,`goal_id`),
  KEY `idx_distill_role` (`tenant_id`,`project_id`,`distill_task_id`,`role`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='蒸馏任务主辅转化目标关系';

CREATE TABLE IF NOT EXISTS `hou_geo_distill_result` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint unsigned NOT NULL DEFAULT '0',
  `project_id` bigint unsigned DEFAULT NULL,
  `task_id` bigint unsigned NOT NULL,
  `keyword` varchar(256) COLLATE utf8mb4_unicode_ci NOT NULL,
  `heat` int DEFAULT NULL,
  `opportunity` int DEFAULT NULL,
  `merged_to_library` tinyint NOT NULL DEFAULT '0' COMMENT '是否已入训练词库',
  `used_in_creation` tinyint NOT NULL DEFAULT '0' COMMENT '是否已转创作',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_task` (`task_id`),
  KEY `idx_tenant_keyword` (`tenant_id`,`keyword`(64))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='蒸馏结果归档(A1.8)';

CREATE TABLE IF NOT EXISTS `hou_geo_distill_task` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint unsigned NOT NULL DEFAULT '0',
  `project_id` bigint unsigned DEFAULT NULL,
  `task_name` varchar(128) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `seed_json` json DEFAULT NULL COMMENT '种子词/竞品/行业语料',
  `source_type` varchar(32) COLLATE utf8mb4_unicode_ci DEFAULT 'seed' COMMENT 'seed/competitor/industry',
  `strategy` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT '均衡策略' COMMENT '蒸馏策略',
  `target_count` int DEFAULT '500' COMMENT '目标词数',
  `status` varchar(32) COLLATE utf8mb4_unicode_ci DEFAULT 'pending' COMMENT 'pending/running/done/failed',
  `result_count` int DEFAULT '0',
  `quality_score` int DEFAULT NULL COMMENT '质量分',
  `create_by` bigint unsigned DEFAULT NULL,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `finished_at` datetime DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_tenant_status` (`tenant_id`,`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='关键词蒸馏任务(A1.7)';

CREATE TABLE IF NOT EXISTS `hou_geo_distribute_geo_check` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `post_id` bigint unsigned NOT NULL,
  `check_score` int DEFAULT NULL,
  `blocked` tinyint NOT NULL DEFAULT '0',
  `checks_json` json DEFAULT NULL COMMENT '检测项明细',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_post_time` (`post_id`,`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='分发GEO检测结果';

CREATE TABLE IF NOT EXISTS `hou_geo_distribute_post` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint unsigned NOT NULL DEFAULT '0',
  `project_id` bigint unsigned DEFAULT NULL,
  `platform` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'xhs/toutiao/douyin/bjh',
  `platform_account_id` bigint unsigned DEFAULT NULL,
  `content_type` varchar(32) COLLATE utf8mb4_unicode_ci DEFAULT '图文' COMMENT '图文/视频/微头条',
  `title` varchar(256) COLLATE utf8mb4_unicode_ci NOT NULL,
  `conclusion` varchar(512) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '首段结论',
  `body` mediumtext COLLATE utf8mb4_unicode_ci COMMENT '正文',
  `tags` varchar(512) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '话题标签',
  `entities` varchar(512) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '实体词',
  `site_url` varchar(512) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `cover_url` varchar(512) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `media_url` varchar(1024) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '实际发布的视频或音频地址',
  `video_meta_json` text COLLATE utf8mb4_unicode_ci COMMENT '视频生成与发布元数据',
  `status` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'draft' COMMENT 'draft/pending/published/failed',
  `plan_time` datetime DEFAULT NULL COMMENT '定时发布',
  `published_at` datetime DEFAULT NULL,
  `publish_link` varchar(512) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `fail_reason` varchar(512) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `stats_json` json DEFAULT NULL COMMENT '阅读/赞/评论',
  `need_approval` tinyint NOT NULL DEFAULT '0',
  `geo_score` int DEFAULT NULL COMMENT '最近GEO检测分',
  `source_draft_id` bigint unsigned DEFAULT NULL COMMENT '来源内容草稿',
  `studio_keyword_id` bigint unsigned DEFAULT NULL COMMENT 'AI创作关键词明细ID',
  `target_keyword` varchar(256) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '目标关键词快照',
  `omni_job_id` bigint unsigned DEFAULT NULL COMMENT 'Omni任务来源',
  `create_by` bigint unsigned DEFAULT NULL,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_deleted` tinyint NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `idx_tenant_platform_status` (`tenant_id`,`platform`,`status`,`plan_time`),
  KEY `idx_account` (`platform_account_id`),
  KEY `idx_studio_keyword_status` (`tenant_id`,`studio_keyword_id`,`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='平台分发内容(小红书/头条等)';

CREATE TABLE IF NOT EXISTS `hou_geo_effect_report` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint unsigned NOT NULL DEFAULT '0',
  `project_id` bigint unsigned DEFAULT NULL,
  `title` varchar(256) COLLATE utf8mb4_unicode_ci NOT NULL,
  `report_type` varchar(32) COLLATE utf8mb4_unicode_ci DEFAULT 'weekly' COMMENT 'weekly/monthly/roi',
  `period_start` date DEFAULT NULL,
  `period_end` date DEFAULT NULL,
  `compare_json` json DEFAULT NULL,
  `roi_json` json DEFAULT NULL,
  `file_url` varchar(512) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'PDF存储',
  `status` varchar(32) COLLATE utf8mb4_unicode_ci DEFAULT 'done',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_tenant_type` (`tenant_id`,`report_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='效果评估报告';

CREATE TABLE IF NOT EXISTS `hou_geo_entity` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint unsigned NOT NULL DEFAULT '0',
  `project_id` bigint unsigned DEFAULT NULL,
  `entity_text` varchar(256) COLLATE utf8mb4_unicode_ci NOT NULL,
  `entity_type` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '品牌/产品/组织',
  `confidence` int DEFAULT NULL,
  `source` varchar(32) COLLATE utf8mb4_unicode_ci DEFAULT 'manual',
  `aligned` tinyint DEFAULT '0',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `is_deleted` tinyint NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `idx_tenant_type` (`tenant_id`,`entity_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='实体库';

CREATE TABLE IF NOT EXISTS `hou_geo_entity_alignment` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint unsigned NOT NULL DEFAULT '0',
  `own_entity_id` bigint unsigned NOT NULL,
  `external_source` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT 'wikidata',
  `external_entity_id` varchar(128) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `external_name` varchar(256) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `match_score` int DEFAULT NULL,
  `aligned_at` datetime DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_own_entity` (`own_entity_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='实体对齐';

CREATE TABLE IF NOT EXISTS `hou_geo_expand_mode` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `group_id` bigint unsigned NOT NULL,
  `code` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '模式值 ai/longtail/compare',
  `label` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '展示名',
  `description` varchar(512) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '说明',
  `template` varchar(32) COLLATE utf8mb4_unicode_ci DEFAULT '指南型' COMMENT '意图模板',
  `suffix_json` json DEFAULT NULL COMMENT '后缀数组 [" 是什么"," 怎么做"]',
  `prefix_json` json DEFAULT NULL COMMENT '前缀数组 ["什么是","为什么需要"]',
  `is_virtual` tinyint NOT NULL DEFAULT '0' COMMENT '1=虚拟模式如全意图混合',
  `sort_order` int NOT NULL DEFAULT '0',
  `status` tinyint NOT NULL DEFAULT '1',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_code` (`code`),
  KEY `idx_group_sort` (`group_id`,`sort_order`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='AI创作扩展模式';

CREATE TABLE IF NOT EXISTS `hou_geo_expand_mode_group` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `code` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '分组编码',
  `label` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '分组名称',
  `sort_order` int NOT NULL DEFAULT '0',
  `status` tinyint NOT NULL DEFAULT '1',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_code` (`code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='AI创作扩展模式分组';

CREATE TABLE IF NOT EXISTS `hou_geo_freshness_alert` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint unsigned NOT NULL DEFAULT '0',
  `asset_id` bigint unsigned DEFAULT NULL,
  `alert_level` varchar(32) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `message` varchar(512) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `status` varchar(32) COLLATE utf8mb4_unicode_ci DEFAULT 'open',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_tenant_status` (`tenant_id`,`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='内容新鲜度告警';

CREATE TABLE IF NOT EXISTS `hou_geo_gbp_profile` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint unsigned NOT NULL DEFAULT '0',
  `project_id` bigint unsigned DEFAULT NULL,
  `store_id` bigint unsigned DEFAULT NULL,
  `binding_id` bigint unsigned DEFAULT NULL,
  `profile_json` json DEFAULT NULL,
  `ai_summary` text COLLATE utf8mb4_unicode_ci COMMENT '本地AI摘要',
  `sync_at` datetime DEFAULT NULL,
  `source_type` varchar(16) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'import',
  `normalized_json` longtext COLLATE utf8mb4_unicode_ci,
  `raw_json` longtext COLLATE utf8mb4_unicode_ci,
  `external_updated_at` datetime DEFAULT NULL,
  `sync_job_id` bigint unsigned DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_store` (`store_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='GBP/地图资料';

CREATE TABLE IF NOT EXISTS `hou_geo_hallucination_alert` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint unsigned NOT NULL DEFAULT '0',
  `project_id` bigint unsigned DEFAULT NULL,
  `question_id` bigint unsigned DEFAULT NULL,
  `claim_text` varchar(1024) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `severity` varchar(32) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `status` varchar(32) COLLATE utf8mb4_unicode_ci DEFAULT 'open' COMMENT 'open/resolved',
  `resolved_at` datetime DEFAULT NULL,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_tenant_status` (`tenant_id`,`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='AI幻觉告警';

CREATE TABLE IF NOT EXISTS `hou_geo_hot_trend` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint unsigned NOT NULL DEFAULT '0',
  `project_id` bigint unsigned DEFAULT NULL,
  `platform` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'baidu/douyin/xhs/toutiao',
  `rank_no` int DEFAULT NULL,
  `title` varchar(256) COLLATE utf8mb4_unicode_ci NOT NULL,
  `heat` bigint DEFAULT NULL,
  `tags_json` json DEFAULT NULL,
  `related_keywords_json` json DEFAULT NULL,
  `trend_date` date NOT NULL,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_platform_date` (`platform`,`trend_date`,`rank_no`),
  KEY `idx_tenant_project_platform_date` (`tenant_id`,`project_id`,`platform`,`trend_date`,`rank_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='热搜/热榜';

CREATE TABLE IF NOT EXISTS `hou_geo_hot_trend_sync_log` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint unsigned NOT NULL DEFAULT '0',
  `project_id` bigint unsigned NOT NULL DEFAULT '0',
  `platform` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL,
  `sync_count` int DEFAULT '0',
  `status` varchar(32) COLLATE utf8mb4_unicode_ci DEFAULT 'success',
  `error_msg` varchar(512) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `sync_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_platform_time` (`platform`,`sync_at`),
  KEY `idx_tenant_project_time` (`tenant_id`,`project_id`,`sync_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='热搜同步日志';

CREATE TABLE IF NOT EXISTS `hou_geo_image_asset` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint unsigned NOT NULL DEFAULT '0',
  `project_id` bigint unsigned DEFAULT NULL,
  `name` varchar(256) COLLATE utf8mb4_unicode_ci NOT NULL,
  `category` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT 'product' COMMENT 'logo/product/scene/cover/infographic',
  `file_url` varchar(512) COLLATE utf8mb4_unicode_ci NOT NULL,
  `thumb_url` varchar(512) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `tags_json` json DEFAULT NULL,
  `file_size` bigint DEFAULT NULL COMMENT '字节',
  `width` int DEFAULT NULL,
  `height` int DEFAULT NULL,
  `ai_recommended` tinyint NOT NULL DEFAULT '0',
  `create_by` bigint unsigned DEFAULT NULL,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `is_deleted` tinyint NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `idx_tenant_category` (`tenant_id`,`project_id`,`category`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='企业图库(17.5)';

CREATE TABLE IF NOT EXISTS `hou_geo_image_category` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint NOT NULL,
  `project_id` bigint NOT NULL,
  `name` varchar(64) NOT NULL,
  `create_time` datetime NOT NULL,
  `update_time` datetime NOT NULL,
  `is_deleted` tinyint NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_geo_image_category_project_name` (`tenant_id`,`project_id`,`name`,`is_deleted`),
  KEY `idx_geo_image_category_project` (`tenant_id`,`project_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='GEO企业图像分类';

CREATE TABLE IF NOT EXISTS `hou_geo_indexing_record` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint unsigned NOT NULL DEFAULT '0',
  `project_id` bigint unsigned DEFAULT NULL,
  `keyword_or_question` varchar(512) COLLATE utf8mb4_unicode_ci NOT NULL,
  `ai_model_code` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'deepseek/doubao/yuanbao等',
  `index_status` varchar(32) COLLATE utf8mb4_unicode_ci DEFAULT 'no' COMMENT 'yes/no/partial',
  `index_url` varchar(512) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `rank` int DEFAULT NULL COMMENT '收录排名位置，越小越靠前',
  `first_indexed_at` datetime DEFAULT NULL,
  `last_indexed_at` datetime DEFAULT NULL,
  `stat_date` date DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_tenant_model` (`tenant_id`,`ai_model_code`,`stat_date`),
  KEY `idx_keyword` (`keyword_or_question`(191))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='AI收录明细(30.5)';

CREATE TABLE IF NOT EXISTS `hou_geo_industry_benchmark` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint unsigned NOT NULL DEFAULT '0',
  `project_id` bigint unsigned DEFAULT NULL,
  `industry` varchar(128) COLLATE utf8mb4_unicode_ci NOT NULL,
  `metric_key` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'SOV/citation_rate/health',
  `industry_value` decimal(12,4) DEFAULT NULL,
  `self_value` decimal(12,4) DEFAULT NULL,
  `gap` decimal(12,4) DEFAULT NULL,
  `stat_period` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_tenant_industry` (`tenant_id`,`industry`,`metric_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='行业基准(F2)';

CREATE TABLE IF NOT EXISTS `hou_geo_intent_page_match` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint unsigned NOT NULL DEFAULT '0',
  `project_id` bigint unsigned DEFAULT NULL,
  `page_id` bigint unsigned DEFAULT NULL,
  `page_url` varchar(512) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `intent` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `match_score` int DEFAULT NULL,
  `mismatch_reason` varchar(256) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_tenant_page` (`tenant_id`,`page_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='页面意图匹配矩阵';

CREATE TABLE IF NOT EXISTS `hou_geo_intent_record` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint unsigned NOT NULL DEFAULT '0',
  `project_id` bigint unsigned DEFAULT NULL,
  `question` varchar(512) COLLATE utf8mb4_unicode_ci NOT NULL,
  `primary_intent` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `sub_intents_json` json DEFAULT NULL,
  `priority` int DEFAULT NULL,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_tenant_intent` (`tenant_id`,`primary_intent`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='意图记录';

CREATE TABLE IF NOT EXISTS `hou_geo_intent_rule_config` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint unsigned NOT NULL DEFAULT '0',
  `project_id` bigint unsigned DEFAULT NULL,
  `taxonomy` varchar(128) COLLATE utf8mb4_unicode_ci DEFAULT '8类意图',
  `weight` int DEFAULT '80',
  `config_json` json DEFAULT NULL,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_tenant_project` (`tenant_id`,`project_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='意图规则配置(A3)';

CREATE TABLE IF NOT EXISTS `hou_geo_internal_link_audit` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint unsigned NOT NULL DEFAULT '0',
  `project_id` bigint unsigned DEFAULT NULL,
  `page_url` varchar(512) COLLATE utf8mb4_unicode_ci NOT NULL,
  `inbound_count` int DEFAULT '0',
  `is_orphan` tinyint DEFAULT '0',
  `pagerank_loss` decimal(8,4) DEFAULT NULL,
  `suggestion` varchar(512) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `audit_at` datetime DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_tenant_orphan` (`tenant_id`,`is_orphan`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='内链/孤岛审计';

CREATE TABLE IF NOT EXISTS `hou_geo_jsonld_validate_log` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint unsigned NOT NULL DEFAULT '0',
  `project_id` bigint unsigned NOT NULL DEFAULT '0',
  `page_id` bigint unsigned DEFAULT NULL,
  `input_json` mediumtext COLLATE utf8mb4_unicode_ci,
  `valid` tinyint NOT NULL DEFAULT '0',
  `errors_json` json DEFAULT NULL,
  `warnings_json` json DEFAULT NULL,
  `create_by` bigint unsigned DEFAULT NULL,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_tenant_time` (`tenant_id`,`create_time`),
  KEY `idx_project_page` (`tenant_id`,`project_id`,`page_id`,`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='JSON-LD校验日志';

CREATE TABLE IF NOT EXISTS `hou_geo_keyword` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint unsigned NOT NULL DEFAULT '0',
  `project_id` bigint unsigned DEFAULT NULL,
  `keyword` varchar(256) COLLATE utf8mb4_unicode_ci NOT NULL,
  `template_tag` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '问题模板',
  `intent` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `difficulty` int DEFAULT NULL,
  `opportunity` int DEFAULT NULL,
  `trend_pct` decimal(6,2) DEFAULT NULL,
  `covered` tinyint DEFAULT '0',
  `lang` varchar(16) COLLATE utf8mb4_unicode_ci DEFAULT 'zh',
  `cluster_id` bigint unsigned DEFAULT NULL,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `is_deleted` tinyint NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `idx_tenant_keyword` (`tenant_id`,`project_id`,`intent`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='关键词/扩展词';

CREATE TABLE IF NOT EXISTS `hou_geo_keyword_coverage` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint unsigned NOT NULL DEFAULT '0',
  `project_id` bigint unsigned DEFAULT NULL,
  `studio_keyword_id` bigint unsigned NOT NULL COMMENT 'hou_geo_ai_studio_keyword.id',
  `keyword_text` varchar(256) COLLATE utf8mb4_general_ci NOT NULL COMMENT '关键词快照',
  `source_type` varchar(32) COLLATE utf8mb4_general_ci NOT NULL COMMENT 'content_draft|distribute_post|article',
  `source_id` bigint unsigned NOT NULL COMMENT '来源记录 ID',
  `platform` varchar(32) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT 'xhs/toutiao 等，draft 可为空',
  `cover_status` varchar(32) COLLATE utf8mb4_general_ci NOT NULL DEFAULT 'draft' COMMENT 'draft|pending|published|failed|revoked',
  `publish_link` varchar(512) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `covered_at` datetime DEFAULT NULL COMMENT '首次变为 published 的时间',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_deleted` tinyint NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_source` (`source_type`,`source_id`,`platform`),
  KEY `idx_keyword_status` (`studio_keyword_id`,`cover_status`),
  KEY `idx_tenant_keyword` (`tenant_id`,`keyword_text`(64))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='关键词覆盖明细';

CREATE TABLE IF NOT EXISTS `hou_geo_keyword_discover_task` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint unsigned NOT NULL DEFAULT '0',
  `project_id` bigint unsigned DEFAULT NULL,
  `task_name` varchar(128) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `seed_json` json DEFAULT NULL,
  `mode` varchar(32) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'ai/question/longtail',
  `ai_model_ids` json DEFAULT NULL,
  `status` varchar(32) COLLATE utf8mb4_unicode_ci DEFAULT 'pending',
  `result_count` int DEFAULT '0',
  `create_by` bigint unsigned DEFAULT NULL,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `finished_at` datetime DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_tenant_status` (`tenant_id`,`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='关键词发现任务';

CREATE TABLE IF NOT EXISTS `hou_geo_keyword_index` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint unsigned NOT NULL DEFAULT '0',
  `project_id` bigint unsigned DEFAULT NULL,
  `keyword` varchar(256) COLLATE utf8mb4_unicode_ci NOT NULL,
  `heat_index` int DEFAULT NULL,
  `competition_index` int DEFAULT NULL,
  `opportunity_index` int DEFAULT NULL,
  `trend_json` json DEFAULT NULL COMMENT '日/周趋势',
  `stat_date` date NOT NULL,
  `source` varchar(128) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '历史导入' COMMENT '真实指标数据来源',
  `region` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '全国' COMMENT '指标统计地区',
  `collected_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '指标采集或导入时间',
  PRIMARY KEY (`id`),
  KEY `idx_tenant_date` (`tenant_id`,`stat_date`),
  KEY `idx_geo_keyword_index_lookup` (`tenant_id`,`project_id`,`keyword`,`stat_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='关键词指数(30.6)';

CREATE TABLE IF NOT EXISTS `hou_geo_kg_snapshot` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint unsigned NOT NULL DEFAULT '0',
  `project_id` bigint unsigned DEFAULT NULL,
  `nodes_json` json DEFAULT NULL,
  `edges_json` json DEFAULT NULL,
  `refreshed_at` datetime DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_tenant_project` (`tenant_id`,`project_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='知识图谱快照';

CREATE TABLE IF NOT EXISTS `hou_geo_knowledge_item` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint unsigned NOT NULL DEFAULT '0',
  `project_id` bigint unsigned DEFAULT NULL,
  `title` varchar(256) COLLATE utf8mb4_unicode_ci NOT NULL,
  `item_type` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT 'faq' COMMENT 'intro/manual/faq/case/policy',
  `content` mediumtext COLLATE utf8mb4_unicode_ci,
  `word_count` int DEFAULT NULL,
  `vector_status` varchar(32) COLLATE utf8mb4_unicode_ci DEFAULT 'pending' COMMENT 'pending/done/failed',
  `is_default_context` tinyint NOT NULL DEFAULT '0',
  `file_url` varchar(512) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '导入文档路径',
  `source_type` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'manual',
  `source_name` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `visibility` varchar(16) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'project',
  `current_version` int NOT NULL DEFAULT '1',
  `owner_user_id` bigint unsigned DEFAULT NULL,
  `create_by` bigint unsigned DEFAULT NULL,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_deleted` tinyint NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `idx_tenant_type` (`tenant_id`,`project_id`,`item_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='企业知识库(17.6)';

CREATE TABLE IF NOT EXISTS `hou_geo_knowledge_item_tag` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint unsigned NOT NULL DEFAULT '0',
  `project_id` bigint unsigned DEFAULT NULL,
  `knowledge_id` bigint unsigned NOT NULL,
  `tag_id` bigint unsigned NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_geo_knowledge_item_tag` (`knowledge_id`,`tag_id`),
  KEY `idx_geo_knowledge_item_tag_project` (`tenant_id`,`project_id`,`knowledge_id`),
  KEY `idx_geo_knowledge_item_tag_tag` (`tenant_id`,`project_id`,`tag_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='GEO知识库条目标签';

CREATE TABLE IF NOT EXISTS `hou_geo_knowledge_share` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint unsigned NOT NULL DEFAULT '0',
  `project_id` bigint unsigned DEFAULT NULL,
  `knowledge_id` bigint unsigned NOT NULL,
  `user_id` bigint unsigned NOT NULL,
  `access_level` varchar(16) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'viewer',
  `created_by` bigint unsigned DEFAULT NULL,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_geo_knowledge_share` (`knowledge_id`,`user_id`),
  KEY `idx_geo_knowledge_share_project` (`tenant_id`,`project_id`,`knowledge_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='GEO知识库共享成员';

CREATE TABLE IF NOT EXISTS `hou_geo_knowledge_tag` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint unsigned NOT NULL DEFAULT '0',
  `project_id` bigint unsigned DEFAULT NULL,
  `name` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_deleted` tinyint NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_geo_knowledge_tag` (`tenant_id`,`project_id`,`name`,`is_deleted`),
  KEY `idx_geo_knowledge_tag_project` (`tenant_id`,`project_id`,`is_deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='GEO知识库标签';

CREATE TABLE IF NOT EXISTS `hou_geo_knowledge_version` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint unsigned NOT NULL DEFAULT '0',
  `project_id` bigint unsigned DEFAULT NULL,
  `knowledge_id` bigint unsigned NOT NULL,
  `version_no` int NOT NULL,
  `title` varchar(256) COLLATE utf8mb4_unicode_ci NOT NULL,
  `item_type` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT 'faq',
  `content` mediumtext COLLATE utf8mb4_unicode_ci,
  `word_count` int DEFAULT NULL,
  `change_note` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `created_by` bigint unsigned DEFAULT NULL,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `is_deleted` tinyint NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_geo_knowledge_version` (`tenant_id`,`project_id`,`knowledge_id`,`version_no`),
  KEY `idx_geo_knowledge_version_item` (`tenant_id`,`project_id`,`knowledge_id`,`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='GEO知识库版本';

CREATE TABLE IF NOT EXISTS `hou_geo_landing_page` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint unsigned NOT NULL DEFAULT '0',
  `project_id` bigint unsigned DEFAULT NULL,
  `title` varchar(256) COLLATE utf8mb4_unicode_ci NOT NULL,
  `keyword` varchar(256) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `html_content` mediumtext COLLATE utf8mb4_unicode_ci,
  `schema_json` json DEFAULT NULL,
  `meta_json` json DEFAULT NULL COMMENT '行业/风格/GEO特性/评分等元数据',
  `status` varchar(32) COLLATE utf8mb4_unicode_ci DEFAULT 'draft',
  `create_by` bigint unsigned DEFAULT NULL,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_tenant_status` (`tenant_id`,`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='AI落地页(30.7)';

CREATE TABLE IF NOT EXISTS `hou_geo_llm_endpoint` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint unsigned NOT NULL DEFAULT '0',
  `project_id` bigint unsigned NOT NULL,
  `alias` varchar(128) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '自定义别名',
  `usage_mode` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'index_only' COMMENT 'index_only/index_write',
  `web_search` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'off' COMMENT 'off/on/manual',
  `provider_code` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'deepseek/doubao/hunyuan/qwen/ernie/custom',
  `api_endpoint` varchar(512) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'chat/completions 完整地址',
  `api_key` varchar(1024) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '加密存储 ENC::',
  `model_name` varchar(128) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '模型 ID',
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '1正常 0停用',
  `last_test_at` datetime DEFAULT NULL,
  `last_test_ok` tinyint DEFAULT NULL COMMENT '1成功 0失败',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_deleted` tinyint NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `idx_geo_llm_endpoint_project` (`tenant_id`,`project_id`,`is_deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='GEO项目级大模型API对接';

CREATE TABLE IF NOT EXISTS `hou_geo_local_monitor_plan` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint unsigned NOT NULL,
  `project_id` bigint unsigned NOT NULL,
  `schedule_type` varchar(16) NOT NULL DEFAULT 'manual',
  `run_time` time DEFAULT NULL,
  `weekday` tinyint DEFAULT NULL,
  `timezone` varchar(64) NOT NULL DEFAULT 'Asia/Shanghai',
  `models_json` text NOT NULL,
  `is_enabled` tinyint NOT NULL DEFAULT '0',
  `next_run_at` datetime DEFAULT NULL,
  `last_run_at` datetime DEFAULT NULL,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_local_monitor_plan_project` (`tenant_id`,`project_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='本地AI监测计划';

CREATE TABLE IF NOT EXISTS `hou_geo_local_monitor_query` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint unsigned NOT NULL,
  `project_id` bigint unsigned NOT NULL,
  `store_id` bigint unsigned DEFAULT NULL,
  `city` varchar(64) NOT NULL,
  `query_text` varchar(512) NOT NULL,
  `brand_name` varchar(160) DEFAULT NULL,
  `domain` varchar(256) DEFAULT NULL,
  `aliases_json` text,
  `competitors_json` text,
  `is_enabled` tinyint NOT NULL DEFAULT '1',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_deleted` tinyint NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `idx_local_monitor_query_project` (`tenant_id`,`project_id`,`is_enabled`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='本地AI监测查询词';

CREATE TABLE IF NOT EXISTS `hou_geo_local_monitor_run` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint unsigned NOT NULL,
  `project_id` bigint unsigned NOT NULL,
  `plan_id` bigint unsigned DEFAULT NULL,
  `trigger_type` varchar(16) NOT NULL,
  `status` varchar(24) NOT NULL DEFAULT 'pending',
  `query_count` int NOT NULL DEFAULT '0',
  `model_count` int NOT NULL DEFAULT '0',
  `success_count` int NOT NULL DEFAULT '0',
  `failure_count` int NOT NULL DEFAULT '0',
  `models_json` text NOT NULL,
  `error_message` varchar(1024) DEFAULT NULL,
  `started_at` datetime DEFAULT NULL,
  `finished_at` datetime DEFAULT NULL,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_local_monitor_run_project` (`tenant_id`,`project_id`,`status`,`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='本地AI监测运行';

CREATE TABLE IF NOT EXISTS `hou_geo_local_nap_issue` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint unsigned NOT NULL,
  `project_id` bigint unsigned NOT NULL,
  `store_id` bigint unsigned NOT NULL,
  `binding_id` bigint unsigned DEFAULT NULL,
  `audit_id` bigint unsigned NOT NULL,
  `platform` varchar(32) NOT NULL,
  `field_code` varchar(32) NOT NULL,
  `expected_value` text,
  `observed_value` text,
  `suggested_value` text,
  `status` varchar(24) NOT NULL DEFAULT 'open',
  `approved_by` bigint unsigned DEFAULT NULL,
  `approved_at` datetime DEFAULT NULL,
  `rejected_reason` varchar(512) DEFAULT NULL,
  `sync_job_id` bigint unsigned DEFAULT NULL,
  `workflow_task_id` bigint unsigned DEFAULT NULL,
  `last_checked_at` datetime DEFAULT NULL,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_local_nap_issue_project` (`tenant_id`,`project_id`,`status`),
  KEY `idx_local_nap_issue_identity` (`store_id`,`platform`,`field_code`,`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='本地化NAP修复问题';

CREATE TABLE IF NOT EXISTS `hou_geo_local_platform_binding` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint unsigned NOT NULL,
  `project_id` bigint unsigned NOT NULL,
  `store_id` bigint unsigned NOT NULL,
  `platform_account_id` bigint unsigned DEFAULT NULL,
  `platform` varchar(32) NOT NULL,
  `external_id` varchar(160) DEFAULT NULL,
  `external_name` varchar(256) DEFAULT NULL,
  `access_mode` varchar(16) NOT NULL DEFAULT 'import',
  `capabilities_json` text,
  `status` varchar(24) NOT NULL DEFAULT 'unconfigured',
  `raw_snapshot_json` longtext,
  `last_sync_at` datetime DEFAULT NULL,
  `last_error` varchar(1024) DEFAULT NULL,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_deleted` tinyint NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_local_binding` (`tenant_id`,`project_id`,`store_id`,`platform`,`external_id`,`is_deleted`),
  KEY `idx_local_binding_project` (`tenant_id`,`project_id`,`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='本地化平台绑定';

CREATE TABLE IF NOT EXISTS `hou_geo_local_query_monitor` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint unsigned NOT NULL DEFAULT '0',
  `project_id` bigint unsigned DEFAULT NULL,
  `query_text` varchar(512) COLLATE utf8mb4_unicode_ci NOT NULL,
  `city` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `ai_model_code` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `mentioned` tinyint DEFAULT '0',
  `rank_no` decimal(6,2) DEFAULT NULL,
  `record_date` date NOT NULL,
  `query_id` bigint unsigned DEFAULT NULL,
  `run_id` bigint unsigned DEFAULT NULL,
  `store_id` bigint unsigned DEFAULT NULL,
  `status` varchar(24) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'succeeded',
  `competitor_mentioned` tinyint NOT NULL DEFAULT '0',
  `raw_answer` longtext COLLATE utf8mb4_unicode_ci,
  `citations_json` longtext COLLATE utf8mb4_unicode_ci,
  `source_urls_json` longtext COLLATE utf8mb4_unicode_ci,
  `error_message` varchar(1024) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `captured_at` datetime DEFAULT NULL,
  `external_request_id` varchar(160) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `model_name` varchar(128) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `provider_code` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_tenant_date` (`tenant_id`,`record_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='本地AI摘要监测(29.6)';

CREATE TABLE IF NOT EXISTS `hou_geo_local_store` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint unsigned NOT NULL DEFAULT '0',
  `project_id` bigint unsigned DEFAULT NULL,
  `store_code` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `store_name` varchar(128) COLLATE utf8mb4_unicode_ci NOT NULL,
  `address` varchar(512) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `phone` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `lat` decimal(10,6) DEFAULT NULL,
  `lng` decimal(10,6) DEFAULT NULL,
  `platform_refs_json` json DEFAULT NULL COMMENT '地图/GBP ID',
  `status` varchar(16) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'active',
  `country_code` varchar(8) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'CN',
  `province` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `city` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `district` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `postal_code` varchar(24) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `website` varchar(512) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `category` varchar(128) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `business_hours_json` text COLLATE utf8mb4_unicode_ci,
  `source_type` varchar(16) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'manual',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_deleted` tinyint NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `idx_tenant` (`tenant_id`),
  KEY `idx_local_store_project` (`tenant_id`,`project_id`,`status`,`is_deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='本地门店';

CREATE TABLE IF NOT EXISTS `hou_geo_local_sync_job` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint unsigned NOT NULL,
  `project_id` bigint unsigned NOT NULL,
  `store_id` bigint unsigned DEFAULT NULL,
  `binding_id` bigint unsigned DEFAULT NULL,
  `job_type` varchar(24) NOT NULL,
  `platform` varchar(32) DEFAULT NULL,
  `trigger_type` varchar(24) NOT NULL DEFAULT 'manual',
  `status` varchar(24) NOT NULL DEFAULT 'pending',
  `idempotency_key` varchar(160) NOT NULL,
  `success_count` int NOT NULL DEFAULT '0',
  `failure_count` int NOT NULL DEFAULT '0',
  `input_json` longtext,
  `result_json` longtext,
  `error_code` varchar(64) DEFAULT NULL,
  `error_message` varchar(1024) DEFAULT NULL,
  `started_at` datetime DEFAULT NULL,
  `finished_at` datetime DEFAULT NULL,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_local_sync_idempotency` (`tenant_id`,`project_id`,`idempotency_key`),
  KEY `idx_local_sync_project` (`tenant_id`,`project_id`,`status`,`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='本地化同步任务';

CREATE TABLE IF NOT EXISTS `hou_geo_media_resource` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint unsigned NOT NULL DEFAULT '0',
  `project_id` bigint unsigned NOT NULL DEFAULT '0',
  `external_media_key` varchar(191) COLLATE utf8mb4_unicode_ci NOT NULL,
  `media_keys_json` json DEFAULT NULL COMMENT '透镜 media_keys',
  `account_count` int DEFAULT NULL COMMENT '透镜 account_count',
  `name` varchar(256) COLLATE utf8mb4_unicode_ci NOT NULL,
  `account_name` varchar(256) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '透镜 account_name',
  `dimension` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'account',
  `media_type` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `media_type_label` varchar(128) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `platform` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `platform_label` varchar(128) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `news_source` varchar(128) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `news_source_label` varchar(128) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `industry` varchar(128) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `industry_label` varchar(128) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `index_status` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `index_status_label` varchar(128) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `area` varchar(128) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `title_limit` int DEFAULT NULL COMMENT '透镜 title_limit',
  `is_recommend` tinyint DEFAULT NULL COMMENT '透镜 is_recommend',
  `auth_status` int DEFAULT NULL COMMENT '透镜 auth_status',
  `is_frequent` tinyint DEFAULT NULL COMMENT '透镜 is_frequent',
  `can_geo` tinyint DEFAULT NULL COMMENT '透镜 can_geo',
  `icon_color` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '透镜 icon_color',
  `logo` varchar(1024) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '透镜 logo',
  `url` varchar(1024) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `submit_url` varchar(1024) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `case_url` varchar(1024) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `inclusion` varchar(128) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `inclusion_label` varchar(128) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `geo_engines_json` json DEFAULT NULL COMMENT '透镜 geo_engines',
  `success_rate` decimal(8,2) DEFAULT NULL,
  `cite_count` int DEFAULT NULL,
  `article_count` int DEFAULT NULL COMMENT '透镜 article_count',
  `cited_by_json` json DEFAULT NULL COMMENT '透镜 cited_by',
  `cite_count_window` int DEFAULT NULL,
  `cited_by_window_json` json DEFAULT NULL COMMENT '透镜 cited_by_window',
  `stats_window_json` json DEFAULT NULL COMMENT '透镜 stats_window',
  `list_price` decimal(12,2) DEFAULT NULL,
  `sell_price` decimal(12,2) DEFAULT NULL,
  `publish_time` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '透镜 publish_time',
  `sort_type` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '透镜 sort_type',
  `note` text COLLATE utf8mb4_unicode_ci,
  `tags_json` json DEFAULT NULL,
  `metadata_json` json DEFAULT NULL,
  `source_system` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'timus',
  `favorite` tinyint NOT NULL DEFAULT '0',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_geo_media_resource_scope_key` (`tenant_id`,`project_id`,`external_media_key`),
  KEY `idx_geo_media_resource_scope` (`tenant_id`,`project_id`,`dimension`),
  KEY `idx_geo_media_resource_name` (`tenant_id`,`project_id`,`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='GEO媒体信源目录';

CREATE TABLE IF NOT EXISTS `hou_geo_menu` (
  `id` int unsigned NOT NULL AUTO_INCREMENT,
  `pid` int unsigned NOT NULL DEFAULT '0' COMMENT '父级菜单 id',
  `type` char(1) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'M' COMMENT 'M=目录 C=菜单 A=按钮',
  `name` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '菜单名称',
  `icon` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT '' COMMENT 'Element Plus 图标名',
  `sort` int NOT NULL DEFAULT '0' COMMENT '排序（越大越靠前）',
  `perms` varchar(128) COLLATE utf8mb4_unicode_ci DEFAULT '' COMMENT '权限标识 geo:xxx',
  `paths` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT '' COMMENT '前端路由 /geo/...',
  `component` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT '' COMMENT '预留组件路径',
  `menu_mode` varchar(16) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'expert' COMMENT 'workflow | expert',
  `is_cache` tinyint NOT NULL DEFAULT '0',
  `is_show` tinyint NOT NULL DEFAULT '1',
  `is_disable` tinyint NOT NULL DEFAULT '0',
  `create_time` int unsigned NOT NULL DEFAULT '0',
  `update_time` int unsigned NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `idx_pid` (`pid`),
  KEY `idx_mode_sort` (`menu_mode`,`sort`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='GEO 菜单';

CREATE TABLE IF NOT EXISTS `hou_geo_model_adapt_rule` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint unsigned NOT NULL DEFAULT '0',
  `ai_model_id` bigint unsigned DEFAULT NULL,
  `rule_name` varchar(128) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `rule_json` json DEFAULT NULL,
  `status` tinyint DEFAULT '1',
  PRIMARY KEY (`id`),
  KEY `idx_tenant_model` (`tenant_id`,`ai_model_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='分模型适配规则';

CREATE TABLE IF NOT EXISTS `hou_geo_monitor_config` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint unsigned NOT NULL DEFAULT '0',
  `project_id` bigint unsigned DEFAULT NULL,
  `ai_model_ids` json DEFAULT NULL COMMENT '监测模型ID列表',
  `keyword_pool_json` json DEFAULT NULL COMMENT '监测词库',
  `collect_frequency` varchar(32) COLLATE utf8mb4_unicode_ci DEFAULT 'daily' COMMENT '采集频率',
  `alert_channels_json` json DEFAULT NULL COMMENT '邮件/企微/钉钉',
  `hallucination_threshold` decimal(5,2) DEFAULT '0.30' COMMENT '幻觉阈值',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_tenant_project` (`tenant_id`,`project_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='监测与预警配置';

CREATE TABLE IF NOT EXISTS `hou_geo_monitor_question` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint unsigned NOT NULL DEFAULT '0',
  `project_id` bigint unsigned DEFAULT NULL,
  `question` varchar(512) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '监测问题',
  `intent` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '意图',
  `heat` int DEFAULT '0',
  `frequency` int DEFAULT '0',
  `source` varchar(32) COLLATE utf8mb4_unicode_ci DEFAULT 'manual' COMMENT 'manual/hot_trend/csv/a1',
  `in_monitor` tinyint NOT NULL DEFAULT '0',
  `ai_model_ids` json DEFAULT NULL,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_deleted` tinyint NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `idx_tenant_monitor` (`tenant_id`,`project_id`,`in_monitor`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='监测问题库';

CREATE TABLE IF NOT EXISTS `hou_geo_multimodal_job` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint unsigned NOT NULL DEFAULT '0',
  `project_id` bigint unsigned DEFAULT NULL,
  `job_type` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'visual/tts/avatar/chart',
  `title` varchar(256) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `params_json` json DEFAULT NULL,
  `status` varchar(32) COLLATE utf8mb4_unicode_ci DEFAULT 'pending',
  `asset_id` bigint unsigned DEFAULT NULL,
  `error_msg` varchar(512) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `finished_at` datetime DEFAULT NULL,
  `video_project_id` bigint unsigned DEFAULT NULL COMMENT 'AI video project ID',
  `parent_job_id` bigint unsigned DEFAULT NULL,
  `root_job_id` bigint unsigned DEFAULT NULL,
  `scene_id` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `provider_code` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `model_name` varchar(128) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `external_task_id` varchar(191) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `idempotency_key` varchar(128) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `workflow_stage` varchar(32) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `stage_order` smallint unsigned NOT NULL DEFAULT '0',
  `progress` tinyint unsigned NOT NULL DEFAULT '0',
  `retry_count` smallint unsigned NOT NULL DEFAULT '0',
  `max_retry_count` smallint unsigned NOT NULL DEFAULT '3',
  `error_code` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `error_detail` text COLLATE utf8mb4_unicode_ci,
  `output_url` varchar(1024) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `thumbnail_url` varchar(1024) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `callback_token_hash` varchar(128) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `callback_meta_json` json DEFAULT NULL,
  `cost_estimated` decimal(18,6) NOT NULL DEFAULT '0.000000',
  `cost_actual` decimal(18,6) NOT NULL DEFAULT '0.000000',
  `currency` varchar(8) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'CNY',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `started_at` datetime DEFAULT NULL,
  `timeout_at` datetime DEFAULT NULL,
  `next_poll_at` datetime DEFAULT NULL,
  `version` int unsigned NOT NULL DEFAULT '0',
  `callback_event_id` varchar(191) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_tenant_idempotency` (`tenant_id`,`idempotency_key`),
  UNIQUE KEY `uk_provider_external` (`provider_code`,`external_task_id`),
  KEY `idx_tenant_type_status` (`tenant_id`,`job_type`,`status`),
  KEY `idx_root_stage` (`root_job_id`,`workflow_stage`,`status`),
  KEY `idx_poll` (`status`,`next_poll_at`),
  KEY `idx_video_project_time` (`video_project_id`,`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='多模态生成任务';

CREATE TABLE IF NOT EXISTS `hou_geo_multimodal_job_log` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint unsigned NOT NULL DEFAULT '0',
  `job_id` bigint unsigned NOT NULL,
  `root_job_id` bigint unsigned DEFAULT NULL,
  `event_type` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL,
  `from_status` varchar(32) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `to_status` varchar(32) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `workflow_stage` varchar(32) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `progress` tinyint unsigned NOT NULL DEFAULT '0',
  `provider_code` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `external_task_id` varchar(191) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `callback_event_id` varchar(191) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `message` varchar(512) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `metadata_json` json DEFAULT NULL,
  `operator_id` bigint unsigned DEFAULT NULL,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_provider_callback_event` (`provider_code`,`external_task_id`,`callback_event_id`),
  KEY `idx_job_time` (`job_id`,`create_time`),
  KEY `idx_root_id` (`root_job_id`,`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Multimodal job event log';

CREATE TABLE IF NOT EXISTS `hou_geo_nap_audit` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint unsigned NOT NULL DEFAULT '0',
  `project_id` bigint unsigned DEFAULT NULL,
  `store_id` bigint unsigned DEFAULT NULL,
  `binding_id` bigint unsigned DEFAULT NULL,
  `channel` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'website/baidu/gbp',
  `name_ok` tinyint DEFAULT NULL,
  `address_ok` tinyint DEFAULT NULL,
  `phone_ok` tinyint DEFAULT NULL,
  `issue_detail` varchar(512) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `audit_at` datetime DEFAULT NULL,
  `sync_job_id` bigint unsigned DEFAULT NULL,
  `expected_json` longtext COLLATE utf8mb4_unicode_ci,
  `observed_json` longtext COLLATE utf8mb4_unicode_ci,
  `result_json` longtext COLLATE utf8mb4_unicode_ci,
  `is_recheck` tinyint NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `idx_store` (`store_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='NAP一致性审计';

CREATE TABLE IF NOT EXISTS `hou_geo_omni_job` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint unsigned NOT NULL DEFAULT '0',
  `project_id` bigint unsigned DEFAULT NULL,
  `job_no` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'job_xxx',
  `plan_id` bigint unsigned DEFAULT NULL COMMENT '月计划ID',
  `plan_item_id` bigint unsigned DEFAULT NULL COMMENT '月计划明细ID',
  `source_title` varchar(256) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `source_json` json DEFAULT NULL COMMENT '源内容:title/conclusion/body/entities',
  `trigger_type` varchar(32) COLLATE utf8mb4_unicode_ci DEFAULT 'immediate' COMMENT 'immediate/schedule',
  `operator_id` bigint unsigned DEFAULT NULL,
  `operator_name` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `status` varchar(32) COLLATE utf8mb4_unicode_ci DEFAULT 'processing' COMMENT 'processing/done/partial_failed',
  `success_count` int DEFAULT '0',
  `total_count` int DEFAULT '0',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `finished_at` datetime DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_job_no` (`job_no`),
  KEY `idx_tenant_time` (`tenant_id`,`create_time`),
  KEY `idx_plan_item` (`plan_id`,`plan_item_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='全平台Omni发布任务';

CREATE TABLE IF NOT EXISTS `hou_geo_omni_job_item` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `job_id` bigint unsigned NOT NULL,
  `platform` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL,
  `platform_account_id` bigint unsigned DEFAULT NULL,
  `adapted_title` varchar(256) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `adapted_body` mediumtext COLLATE utf8mb4_unicode_ci,
  `similarity_pct` int DEFAULT NULL COMMENT 'Dify风险值',
  `publishable` tinyint DEFAULT NULL COMMENT 'Dify可否发布',
  `adapt_reason` varchar(512) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'Dify判定说明',
  `adapt_source` varchar(32) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'dify/local',
  `status` varchar(32) COLLATE utf8mb4_unicode_ci DEFAULT 'pending' COMMENT 'success/failed/pending',
  `status_text` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `published_at` datetime DEFAULT NULL,
  `publish_link` varchar(512) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `error_msg` varchar(512) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `distribute_post_id` bigint unsigned DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_job_platform` (`job_id`,`platform`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Omni分平台发布项';

CREATE TABLE IF NOT EXISTS `hou_geo_omni_job_log` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint unsigned NOT NULL DEFAULT '0',
  `project_id` bigint unsigned DEFAULT NULL,
  `job_id` bigint unsigned NOT NULL,
  `job_item_id` bigint unsigned DEFAULT NULL,
  `platform` varchar(32) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `event_type` varchar(48) COLLATE utf8mb4_unicode_ci NOT NULL,
  `level` varchar(16) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'INFO',
  `message` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `detail_json` longtext COLLATE utf8mb4_unicode_ci,
  `create_by` bigint unsigned DEFAULT NULL,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_omni_job_log_job` (`tenant_id`,`project_id`,`job_id`,`create_time`,`id`),
  KEY `idx_omni_job_log_item` (`job_item_id`,`create_time`,`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Omni发布任务追加式事件日志';

CREATE TABLE IF NOT EXISTS `hou_geo_operation_audit` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint unsigned NOT NULL DEFAULT '0',
  `user_id` bigint unsigned DEFAULT NULL,
  `user_name` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `action_type` varchar(32) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '登录/创建/编辑/删除/导出',
  `module` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `description` varchar(512) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `ip_address` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `risk_level` varchar(16) COLLATE utf8mb4_unicode_ci DEFAULT '低' COMMENT '低/中/高',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_tenant_time` (`tenant_id`,`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='操作审计日志(24.6)';

CREATE TABLE IF NOT EXISTS `hou_geo_outreach_prospect` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint unsigned NOT NULL DEFAULT '0',
  `domain` varchar(256) COLLATE utf8mb4_unicode_ci NOT NULL,
  `domain_authority` int DEFAULT NULL,
  `prospect_type` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `status` varchar(32) COLLATE utf8mb4_unicode_ci DEFAULT 'new',
  `roi_estimate` decimal(8,2) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_tenant_status` (`tenant_id`,`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='外链拓展目标';

CREATE TABLE IF NOT EXISTS `hou_geo_outreach_task` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint unsigned NOT NULL DEFAULT '0',
  `project_id` bigint unsigned DEFAULT NULL,
  `domain` varchar(256) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `task_type` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `status` varchar(32) COLLATE utf8mb4_unicode_ci DEFAULT 'pending',
  `assignee_id` bigint unsigned DEFAULT NULL,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_tenant_status` (`tenant_id`,`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='外链拓展任务';

CREATE TABLE IF NOT EXISTS `hou_geo_page_schema_version` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `page_id` bigint unsigned NOT NULL,
  `version_no` int NOT NULL DEFAULT '1',
  `template_id` bigint unsigned DEFAULT NULL,
  `schema_fields_json` json DEFAULT NULL,
  `json_ld` mediumtext COLLATE utf8mb4_unicode_ci,
  `status` varchar(32) COLLATE utf8mb4_unicode_ci DEFAULT 'draft' COMMENT 'draft/published',
  `validation_status` varchar(16) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'unvalidated',
  `errors_json` json DEFAULT NULL,
  `warnings_json` json DEFAULT NULL,
  `published_at` datetime DEFAULT NULL,
  `create_by` bigint unsigned DEFAULT NULL,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_page_version` (`page_id`,`version_no`),
  KEY `idx_page_template` (`template_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='页面Schema版本';

CREATE TABLE IF NOT EXISTS `hou_geo_page_tab_cache` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint unsigned NOT NULL DEFAULT '0',
  `project_id` bigint unsigned DEFAULT NULL,
  `tab_key` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'keywordTrend/backlinkGraph等27个key',
  `payload_json` json NOT NULL,
  `refreshed_at` datetime DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_tenant_tab` (`tenant_id`,`project_id`,`tab_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='页面Tab数据缓存';

CREATE TABLE IF NOT EXISTS `hou_geo_performance_metric` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint unsigned NOT NULL DEFAULT '0',
  `project_id` bigint unsigned DEFAULT NULL,
  `page_url` varchar(512) COLLATE utf8mb4_unicode_ci NOT NULL,
  `lcp` decimal(8,2) DEFAULT NULL COMMENT 'ms',
  `fid` decimal(8,2) DEFAULT NULL,
  `cls` decimal(8,4) DEFAULT NULL,
  `mobile_score` int DEFAULT NULL,
  `lighthouse_score` int DEFAULT NULL,
  `checked_at` datetime DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_tenant_url` (`tenant_id`,`page_url`(191))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='经典性能指标(21)';

CREATE TABLE IF NOT EXISTS `hou_geo_platform_account` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint unsigned NOT NULL DEFAULT '0',
  `project_id` bigint unsigned DEFAULT NULL,
  `platform` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'xhs/toutiao/douyin/bjh/wechat',
  `account_name` varchar(128) COLLATE utf8mb4_unicode_ci NOT NULL,
  `account_id` varchar(128) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '平台侧账号ID',
  `auth_status` varchar(32) COLLATE utf8mb4_unicode_ci DEFAULT 'normal' COMMENT 'normal/expiring/expired',
  `access_token` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci COMMENT 'encrypted Token or Cookie',
  `refresh_token` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci COMMENT 'encrypted Refresh Token',
  `expire_at` datetime DEFAULT NULL,
  `last_sync_at` datetime DEFAULT NULL,
  `is_default` tinyint NOT NULL DEFAULT '0',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_deleted` tinyint NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `idx_tenant_platform` (`tenant_id`,`platform`,`auth_status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='平台OAuth账号';

CREATE TABLE IF NOT EXISTS `hou_geo_platform_adapt_record` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint unsigned NOT NULL DEFAULT '0',
  `project_id` bigint unsigned DEFAULT NULL,
  `preview_batch_no` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '预览批次 preview_xxx',
  `draft_id` bigint unsigned DEFAULT NULL COMMENT '关联文章/草稿 ID',
  `article_id` bigint unsigned DEFAULT NULL COMMENT '关联 hou_geo_article.id',
  `job_id` bigint unsigned DEFAULT NULL COMMENT 'Omni 发布任务 ID',
  `job_item_id` bigint unsigned DEFAULT NULL COMMENT '分平台发布项 ID',
  `platform` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '平台编码 xhs/zhihu 等',
  `source_title` varchar(256) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '原文标题',
  `keyword` varchar(256) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '目标关键词',
  `adapted_title` varchar(256) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '适配标题',
  `similarity_pct` int DEFAULT NULL COMMENT '风险值 0-100，越低越安全',
  `publishable` tinyint NOT NULL DEFAULT '0' COMMENT '可否发布 1是 0否',
  `reason` varchar(512) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'Dify 判定说明',
  `adapt_source` varchar(32) COLLATE utf8mb4_unicode_ci DEFAULT 'dify' COMMENT 'dify/local',
  `token_usage_id` bigint unsigned DEFAULT NULL COMMENT 'hou_ai_token_usage.id',
  `phase` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'preview' COMMENT 'preview/publish',
  `create_by` bigint unsigned DEFAULT NULL,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_tenant_draft_phase` (`tenant_id`,`draft_id`,`phase`),
  KEY `idx_job_platform` (`job_id`,`platform`),
  KEY `idx_preview_batch` (`preview_batch_no`),
  KEY `idx_article_phase` (`article_id`,`phase`,`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Dify分平台适配判定记录';

CREATE TABLE IF NOT EXISTS `hou_geo_platform_auth_log` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint unsigned NOT NULL,
  `project_id` bigint unsigned NOT NULL,
  `account_id` bigint unsigned NOT NULL,
  `action` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'OAuth授权、Cookie授权、连接检测、刷新Token、设为默认、解绑账号',
  `result` varchar(16) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'success/failed',
  `detail` varchar(1000) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `operator_id` bigint unsigned DEFAULT NULL,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_project_account_time` (`tenant_id`,`project_id`,`account_id`,`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='GEO平台账号授权操作日志';

CREATE TABLE IF NOT EXISTS `hou_geo_platform_config` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint unsigned NOT NULL DEFAULT '0',
  `platform` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'xhs/toutiao',
  `app_id` varchar(128) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `app_secret` varchar(256) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '加密存储',
  `oauth_redirect_uri` varchar(512) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `api_mode` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'sandbox' COMMENT 'sandbox/production',
  `publish_defaults_json` json DEFAULT NULL COMMENT '默认内容类型/话题模板等',
  `ai_prompt_json` json DEFAULT NULL COMMENT '分平台 AI 语气模板',
  `extra_json` json DEFAULT NULL COMMENT 'scope/endpoint 等',
  `status` tinyint NOT NULL DEFAULT '1',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_tenant_platform` (`tenant_id`,`platform`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='平台开放平台配置';

CREATE TABLE IF NOT EXISTS `hou_geo_platform_credential` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint unsigned NOT NULL DEFAULT '0',
  `project_id` bigint unsigned DEFAULT NULL,
  `platform` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL,
  `account_id` varchar(128) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '为空表示平台默认账号',
  `username` varchar(128) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '登录账号明文',
  `password` varchar(1024) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '登录密码（AES 加密存储）',
  `enabled` tinyint NOT NULL DEFAULT '1',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_deleted` tinyint NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `idx_geo_credential_scope` (`tenant_id`,`project_id`,`platform`,`is_deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='GEO平台登录凭证（自动重新登录用）';

CREATE TABLE IF NOT EXISTS `hou_geo_platform_publish_quota` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint unsigned NOT NULL DEFAULT '0',
  `project_id` bigint unsigned DEFAULT NULL,
  `platform` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL,
  `monthly_limit` int DEFAULT NULL,
  `default_publish_time` varchar(8) COLLATE utf8mb4_unicode_ci DEFAULT '09:30',
  `enabled` tinyint NOT NULL DEFAULT '1',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_tenant_platform` (`tenant_id`,`project_id`,`platform`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `hou_geo_project` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '主键',
  `org_id` int DEFAULT NULL,
  `tenant_id` bigint unsigned NOT NULL DEFAULT '0' COMMENT '租户ID',
  `name` varchar(128) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '项目名称',
  `client_name` varchar(128) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '客户名称',
  `brand_name` varchar(128) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '主品牌名',
  `domain` varchar(256) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '主域名',
  `status` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'active' COMMENT 'active/archived',
  `monitor_scope_json` json DEFAULT NULL COMMENT '监测范围：模型/词库/竞品',
  `sov_target` decimal(5,2) DEFAULT NULL COMMENT 'SOV目标%',
  `task_count` int NOT NULL DEFAULT '0' COMMENT '关联任务数',
  `create_by` bigint unsigned DEFAULT NULL,
  `update_by` bigint unsigned DEFAULT NULL,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_deleted` tinyint NOT NULL DEFAULT '0',
  `dify_dataset_id` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '项目独立 Dify Dataset ID',
  `dify_dataset_name` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '项目独立 Dify 知识库名称',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_geo_project_tenant_name_deleted` (`tenant_id`,`name`,`is_deleted`),
  UNIQUE KEY `uk_geo_project_tenant_domain_deleted` (`tenant_id`,`domain`,`is_deleted`),
  KEY `idx_tenant_status` (`tenant_id`,`status`,`is_deleted`),
  KEY `idx_geo_project_tenant_org` (`tenant_id`,`org_id`,`is_deleted`,`update_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='GEO项目/客户';

CREATE TABLE IF NOT EXISTS `hou_geo_project_member` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint unsigned NOT NULL DEFAULT '0',
  `project_id` bigint unsigned NOT NULL COMMENT '项目ID',
  `user_id` bigint unsigned NOT NULL COMMENT '用户ID',
  `user_name` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `role` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'editor' COMMENT 'admin/editor/analyst',
  `task_count` int NOT NULL DEFAULT '0',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `is_deleted` tinyint NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_project_user` (`project_id`,`user_id`),
  KEY `idx_tenant_project` (`tenant_id`,`project_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='GEO项目成员';

CREATE TABLE IF NOT EXISTS `hou_geo_project_seed` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '主键',
  `tenant_id` bigint unsigned NOT NULL COMMENT '租户ID',
  `project_id` bigint unsigned NOT NULL COMMENT '项目ID',
  `seed_text` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '用户输入的种子词',
  `normalized_seed` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '去重用规范化种子词',
  `sort_order` int NOT NULL DEFAULT '0' COMMENT '显示顺序',
  `create_by` bigint unsigned DEFAULT NULL,
  `update_by` bigint unsigned DEFAULT NULL,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_tenant_project_seed` (`tenant_id`,`project_id`,`normalized_seed`),
  KEY `idx_tenant_project_sort` (`tenant_id`,`project_id`,`sort_order`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='GEO项目监测种子词';

CREATE TABLE IF NOT EXISTS `hou_geo_publish_plan` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint unsigned NOT NULL DEFAULT '0',
  `project_id` bigint unsigned DEFAULT NULL,
  `org_id` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `plan_month` varchar(7) COLLATE utf8mb4_unicode_ci NOT NULL,
  `name` varchar(128) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `status` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'draft',
  `default_platforms_json` json DEFAULT NULL,
  `article_count` int NOT NULL DEFAULT '0',
  `scheduled_count` int NOT NULL DEFAULT '0',
  `rules_json` json DEFAULT NULL,
  `create_by` bigint unsigned DEFAULT NULL,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_deleted` tinyint NOT NULL DEFAULT '0',
  `article_base_time` time DEFAULT '10:00:00' COMMENT '文章基准发送时间',
  `video_base_time` time DEFAULT '18:00:00' COMMENT '视频基准发送时间',
  `stagger_minutes` int DEFAULT '120' COMMENT '平台错峰窗口分钟',
  `warnings_json` text COLLATE utf8mb4_unicode_ci COMMENT '计划告警',
  `activated_at` datetime DEFAULT NULL,
  `paused_at` datetime DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_tenant_month` (`tenant_id`,`project_id`,`plan_month`,`is_deleted`),
  KEY `idx_status` (`tenant_id`,`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `hou_geo_publish_plan_item` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `plan_id` bigint unsigned NOT NULL,
  `article_id` bigint unsigned NOT NULL,
  `plan_date` date NOT NULL,
  `plan_time` time NOT NULL DEFAULT '09:30:00',
  `platforms_json` json NOT NULL,
  `preview_batch_no` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `omni_job_id` bigint unsigned DEFAULT NULL,
  `status` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'pending',
  `sort_order` int NOT NULL DEFAULT '0',
  `error_msg` varchar(512) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_plan_date` (`plan_id`,`plan_date`),
  KEY `idx_article` (`article_id`),
  KEY `idx_status` (`status`,`plan_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `hou_geo_ranking_kpi` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint unsigned NOT NULL DEFAULT '0',
  `project_id` bigint unsigned DEFAULT NULL,
  `kpi_name` varchar(128) COLLATE utf8mb4_unicode_ci NOT NULL,
  `target_value` decimal(12,4) DEFAULT NULL,
  `current_value` decimal(12,4) DEFAULT NULL,
  `deadline` date DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_tenant_project` (`tenant_id`,`project_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='排名KPI目标';

CREATE TABLE IF NOT EXISTS `hou_geo_ranking_record` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint unsigned NOT NULL DEFAULT '0',
  `project_id` bigint unsigned DEFAULT NULL,
  `keyword_or_question` varchar(512) COLLATE utf8mb4_unicode_ci NOT NULL,
  `ai_model_id` bigint unsigned DEFAULT NULL,
  `rank_no` decimal(6,2) DEFAULT NULL,
  `mentioned` tinyint DEFAULT '0',
  `record_date` date NOT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_tenant_date` (`tenant_id`,`record_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='AI排名记录';

CREATE TABLE IF NOT EXISTS `hou_geo_research_conversion` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint unsigned NOT NULL,
  `project_id` bigint unsigned NOT NULL,
  `source_type` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL,
  `source_id` bigint unsigned NOT NULL,
  `source_text` varchar(512) COLLATE utf8mb4_unicode_ci NOT NULL,
  `target_type` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL,
  `target_id` bigint unsigned DEFAULT NULL,
  `status` varchar(16) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'pending',
  `error_message` varchar(512) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `create_by` bigint unsigned DEFAULT NULL,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_project_source_target` (`tenant_id`,`project_id`,`source_type`,`source_id`,`target_type`),
  KEY `idx_project_status` (`tenant_id`,`project_id`,`status`,`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='研究结果转化来源台账';

CREATE TABLE IF NOT EXISTS `hou_geo_role` (
  `id` int unsigned NOT NULL AUTO_INCREMENT,
  `name` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '角色名称',
  `desc` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '描述',
  `sort` int NOT NULL DEFAULT '0',
  `create_time` int unsigned NOT NULL DEFAULT '0',
  `update_time` int unsigned NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='GEO 角色';

CREATE TABLE IF NOT EXISTS `hou_geo_role_menu` (
  `role_id` int unsigned NOT NULL,
  `menu_id` int unsigned NOT NULL,
  PRIMARY KEY (`role_id`,`menu_id`),
  KEY `idx_menu_id` (`menu_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='GEO 角色-菜单';

CREATE TABLE IF NOT EXISTS `hou_geo_run` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint NOT NULL,
  `project_id` bigint NOT NULL,
  `run_type` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `idempotency_key` varchar(128) COLLATE utf8mb4_unicode_ci NOT NULL,
  `status` varchar(24) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'pending',
  `total_count` int NOT NULL DEFAULT '0',
  `success_count` int NOT NULL DEFAULT '0',
  `failed_count` int NOT NULL DEFAULT '0',
  `request_json` longtext COLLATE utf8mb4_unicode_ci,
  `result_json` longtext COLLATE utf8mb4_unicode_ci,
  `error_code` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `error_message` varchar(1000) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `started_at` datetime DEFAULT NULL,
  `finished_at` datetime DEFAULT NULL,
  `create_by` bigint DEFAULT NULL,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_geo_run_idempotency` (`tenant_id`,`project_id`,`run_type`,`idempotency_key`),
  KEY `idx_geo_run_project_status` (`tenant_id`,`project_id`,`status`,`create_time`),
  KEY `idx_geo_run_type_time` (`run_type`,`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='GEO 统一运行记录';

CREATE TABLE IF NOT EXISTS `hou_geo_run_event` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint NOT NULL,
  `project_id` bigint NOT NULL,
  `run_id` bigint NOT NULL,
  `item_id` bigint DEFAULT NULL,
  `event_type` varchar(48) COLLATE utf8mb4_unicode_ci NOT NULL,
  `from_status` varchar(24) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `to_status` varchar(24) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `message` varchar(1000) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `detail_json` longtext COLLATE utf8mb4_unicode_ci,
  `create_by` bigint DEFAULT NULL,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_geo_run_event_run` (`tenant_id`,`project_id`,`run_id`,`create_time`),
  KEY `idx_geo_run_event_item` (`item_id`,`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='GEO 统一运行事件';

CREATE TABLE IF NOT EXISTS `hou_geo_run_item` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint NOT NULL,
  `project_id` bigint NOT NULL,
  `run_id` bigint NOT NULL,
  `item_key` varchar(512) COLLATE utf8mb4_unicode_ci NOT NULL,
  `status` varchar(24) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'pending',
  `input_json` longtext COLLATE utf8mb4_unicode_ci,
  `output_json` longtext COLLATE utf8mb4_unicode_ci,
  `raw_response` longtext COLLATE utf8mb4_unicode_ci,
  `error_code` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `error_message` varchar(1000) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `attempt_count` int NOT NULL DEFAULT '0',
  `next_retry_at` datetime DEFAULT NULL,
  `started_at` datetime DEFAULT NULL,
  `finished_at` datetime DEFAULT NULL,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_geo_run_item_key` (`run_id`,`item_key`(191)),
  KEY `idx_geo_run_item_project` (`tenant_id`,`project_id`,`run_id`),
  KEY `idx_geo_run_item_retry` (`status`,`next_retry_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='GEO 统一运行子项';

CREATE TABLE IF NOT EXISTS `hou_geo_sandbox_run` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint unsigned NOT NULL DEFAULT '0',
  `project_id` bigint unsigned NOT NULL DEFAULT '0',
  `ai_model_ids` json DEFAULT NULL,
  `input_content` mediumtext COLLATE utf8mb4_unicode_ci,
  `result_json` json DEFAULT NULL,
  `create_by` bigint unsigned DEFAULT NULL,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_tenant_time` (`tenant_id`,`create_time`),
  KEY `idx_project_time` (`tenant_id`,`project_id`,`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='模型沙箱运行';

CREATE TABLE IF NOT EXISTS `hou_geo_scheduled_send_task` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `plan_id` bigint NOT NULL,
  `tenant_id` bigint NOT NULL,
  `project_id` bigint DEFAULT NULL,
  `plan_date` date NOT NULL,
  `content_type` varchar(16) NOT NULL,
  `content_id` bigint NOT NULL,
  `content_title` varchar(255) DEFAULT NULL,
  `platform_code` varchar(32) NOT NULL,
  `scheduled_at` datetime NOT NULL,
  `executed_at` datetime DEFAULT NULL,
  `status` varchar(20) NOT NULL DEFAULT 'pending',
  `warning_code` varchar(64) DEFAULT NULL,
  `error_message` varchar(1000) DEFAULT NULL,
  `retry_count` int NOT NULL DEFAULT '0',
  `omni_job_id` bigint DEFAULT NULL,
  `idempotency_key` varchar(96) NOT NULL,
  `create_time` datetime NOT NULL,
  `update_time` datetime NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_scheduled_send_idempotency` (`idempotency_key`),
  KEY `idx_scheduled_send_due` (`status`,`scheduled_at`),
  KEY `idx_scheduled_send_plan` (`plan_id`,`plan_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='全渠道定时发送分平台任务';

CREATE TABLE IF NOT EXISTS `hou_geo_schema_template` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint unsigned NOT NULL DEFAULT '0',
  `project_id` bigint unsigned NOT NULL DEFAULT '0',
  `name` varchar(128) COLLATE utf8mb4_unicode_ci NOT NULL,
  `schema_type` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'Article/Product/FAQPage',
  `description` varchar(512) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `json_ld_template` mediumtext COLLATE utf8mb4_unicode_ci COMMENT 'JSON-LD模板',
  `fields_json` json DEFAULT NULL,
  `tags_json` json DEFAULT NULL COMMENT '标签',
  `usage_count` int DEFAULT '0',
  `status` tinyint NOT NULL DEFAULT '1',
  `create_by` bigint unsigned DEFAULT NULL,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_deleted` tinyint NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `idx_tenant_type` (`tenant_id`,`schema_type`),
  KEY `idx_project_type` (`tenant_id`,`project_id`,`schema_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Schema模板';

CREATE TABLE IF NOT EXISTS `hou_geo_semantic_html` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint unsigned NOT NULL DEFAULT '0',
  `project_id` bigint unsigned DEFAULT NULL,
  `page_url` varchar(512) COLLATE utf8mb4_unicode_ci NOT NULL,
  `score` int DEFAULT NULL,
  `issues_json` json DEFAULT NULL,
  `checked_at` datetime DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_tenant_url` (`tenant_id`,`page_url`(191))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='语义HTML检测(22)';

CREATE TABLE IF NOT EXISTS `hou_geo_sentiment_alert` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint unsigned NOT NULL DEFAULT '0',
  `project_id` bigint unsigned DEFAULT NULL,
  `alert_type` varchar(32) COLLATE utf8mb4_unicode_ci DEFAULT 'negative_spike' COMMENT 'negative_spike/rumor/competitor',
  `title` varchar(256) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `severity` varchar(32) COLLATE utf8mb4_unicode_ci DEFAULT '中',
  `status` varchar(32) COLLATE utf8mb4_unicode_ci DEFAULT 'open' COMMENT 'open/closed',
  `detail_json` json DEFAULT NULL,
  `closed_at` datetime DEFAULT NULL,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_tenant_status` (`tenant_id`,`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='情感舆情告警';

CREATE TABLE IF NOT EXISTS `hou_geo_sentiment_record` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint unsigned NOT NULL DEFAULT '0',
  `project_id` bigint unsigned DEFAULT NULL,
  `question` varchar(512) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `sentiment` varchar(32) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'positive/neutral/negative',
  `score` decimal(5,2) DEFAULT NULL,
  `platform` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `record_time` datetime NOT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_tenant_time` (`tenant_id`,`record_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='情感记录';

CREATE TABLE IF NOT EXISTS `hou_geo_site_health_metric` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint unsigned NOT NULL DEFAULT '0',
  `project_id` bigint unsigned NOT NULL DEFAULT '0',
  `page_url` varchar(512) COLLATE utf8mb4_unicode_ci NOT NULL,
  `metric_key` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'lcp/cls/fid/depth',
  `metric_value` decimal(12,4) DEFAULT NULL,
  `correlation` decimal(6,4) DEFAULT NULL,
  `measured_at` datetime DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_tenant_metric` (`tenant_id`,`metric_key`),
  KEY `idx_project_metric` (`tenant_id`,`project_id`,`metric_key`,`measured_at`),
  KEY `idx_project_page_metric_time` (`tenant_id`,`project_id`,`page_url`(120),`metric_key`,`measured_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='站点健康指标';

CREATE TABLE IF NOT EXISTS `hou_geo_site_page` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint unsigned NOT NULL DEFAULT '0',
  `project_id` bigint unsigned DEFAULT NULL,
  `url` varchar(512) COLLATE utf8mb4_unicode_ci NOT NULL,
  `title` varchar(256) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `schema_type` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `config_status` varchar(32) COLLATE utf8mb4_unicode_ci DEFAULT 'none' COMMENT 'none/partial/done/error',
  `warning_count` int DEFAULT '0',
  `error_count` int DEFAULT '0',
  `last_validated_at` datetime DEFAULT NULL,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_deleted` tinyint NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_tenant_project_url` (`tenant_id`,`project_id`,`url`(191)),
  KEY `idx_tenant_status` (`tenant_id`,`config_status`,`schema_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='站点页面';

CREATE TABLE IF NOT EXISTS `hou_geo_source_penetration` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint unsigned NOT NULL DEFAULT '0',
  `project_id` bigint unsigned DEFAULT NULL,
  `domain` varchar(256) COLLATE utf8mb4_unicode_ci NOT NULL,
  `domain_type` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '官网/媒体/社媒/百科',
  `penetration_pct` decimal(6,2) DEFAULT NULL,
  `cite_count` int DEFAULT '0',
  `trend_pct` decimal(6,2) DEFAULT NULL,
  `stat_date` date DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_tenant_date` (`tenant_id`,`stat_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='信源穿透(模块6)';

CREATE TABLE IF NOT EXISTS `hou_geo_support_recharge_order` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint unsigned NOT NULL DEFAULT '0',
  `user_id` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'hou_user_info.user_id',
  `order_no` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '商户订单号 GEOTK...',
  `package_id` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL,
  `token_amount` bigint unsigned NOT NULL DEFAULT '0',
  `amount` decimal(10,2) NOT NULL DEFAULT '0.00' COMMENT '元',
  `pay_status` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'pending' COMMENT 'pending/paid/failed',
  `h5_url` varchar(1024) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `pay_time` datetime DEFAULT NULL,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_order_no` (`order_no`),
  KEY `idx_user_status` (`user_id`,`pay_status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='GEO智能客服Token充值订单';

CREATE TABLE IF NOT EXISTS `hou_geo_system_setting` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `setting_key` varchar(64) NOT NULL COMMENT '设置键',
  `setting_value` text COMMENT '设置值',
  `setting_name` varchar(128) DEFAULT NULL COMMENT '设置名称',
  `value_type` varchar(20) DEFAULT 'string' COMMENT 'string/number/boolean/json',
  `update_by` bigint DEFAULT NULL COMMENT '最后修改人',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_setting_key` (`setting_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='GEO 平台系统设置';

CREATE TABLE IF NOT EXISTS `hou_geo_technical_check_run` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint unsigned NOT NULL DEFAULT '0',
  `project_id` bigint unsigned DEFAULT NULL,
  `check_type` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'js/semantic/mobile/lighthouse',
  `page_url` varchar(512) COLLATE utf8mb4_unicode_ci NOT NULL,
  `passed` tinyint NOT NULL DEFAULT '0',
  `result_json` json DEFAULT NULL,
  `create_by` bigint unsigned DEFAULT NULL,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_tenant_type_time` (`tenant_id`,`check_type`,`create_time`),
  KEY `idx_project_type_time` (`tenant_id`,`project_id`,`check_type`,`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='技术检测运行记录';

CREATE TABLE IF NOT EXISTS `hou_geo_topic_alert_sub` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint unsigned NOT NULL DEFAULT '0',
  `project_id` bigint unsigned DEFAULT NULL,
  `email` varchar(128) COLLATE utf8mb4_unicode_ci NOT NULL,
  `channel` varchar(32) COLLATE utf8mb4_unicode_ci DEFAULT 'email',
  `status` varchar(16) COLLATE utf8mb4_unicode_ci DEFAULT 'active',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_tenant_email` (`tenant_id`,`email`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='话题热度告警订阅(A2)';

CREATE TABLE IF NOT EXISTS `hou_geo_topic_cluster` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint unsigned NOT NULL DEFAULT '0',
  `project_id` bigint unsigned DEFAULT NULL,
  `name` varchar(128) COLLATE utf8mb4_unicode_ci NOT NULL,
  `pillar_url` varchar(512) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `authority_score` int DEFAULT NULL,
  `parent_id` bigint unsigned DEFAULT NULL,
  `sort_order` int DEFAULT '0',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `is_deleted` tinyint NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `idx_tenant_parent` (`tenant_id`,`parent_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='话题集群';

CREATE TABLE IF NOT EXISTS `hou_geo_topic_cluster_item` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `cluster_id` bigint unsigned NOT NULL,
  `keyword_id` bigint unsigned DEFAULT NULL,
  `keyword_text` varchar(256) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_cluster` (`cluster_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='话题集群-关键词';

CREATE TABLE IF NOT EXISTS `hou_geo_topic_pillar_plan` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint unsigned NOT NULL DEFAULT '0',
  `project_id` bigint unsigned DEFAULT NULL,
  `cluster_id` bigint unsigned DEFAULT NULL,
  `cluster_name` varchar(128) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `pillar_url` varchar(512) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `sub_pages` int DEFAULT '0',
  `suggested_title` varchar(256) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_tenant_cluster` (`tenant_id`,`cluster_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='支柱内容架构(A2)';

CREATE TABLE IF NOT EXISTS `hou_geo_user_role` (
  `user_id` bigint unsigned NOT NULL COMMENT 'hou_user_info.user_id',
  `role_id` int unsigned NOT NULL,
  PRIMARY KEY (`user_id`,`role_id`),
  KEY `idx_role_id` (`role_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='GEO 用户-角色';

CREATE TABLE IF NOT EXISTS `hou_geo_video_project` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint unsigned NOT NULL DEFAULT '0',
  `geo_project_id` bigint unsigned DEFAULT NULL,
  `title` varchar(256) COLLATE utf8mb4_unicode_ci NOT NULL,
  `video_type` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'product',
  `target_platforms_json` json DEFAULT NULL,
  `aspect_ratio` varchar(16) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '9:16',
  `duration_seconds` int unsigned NOT NULL DEFAULT '30',
  `language` varchar(16) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'zh-CN',
  `current_version_id` bigint unsigned DEFAULT NULL,
  `review_status` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'draft',
  `creator_id` bigint unsigned DEFAULT NULL,
  `is_deleted` tinyint unsigned NOT NULL DEFAULT '0',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_tenant_geo_project` (`tenant_id`,`geo_project_id`,`is_deleted`),
  KEY `idx_creator_time` (`creator_id`,`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='AI video project';

CREATE TABLE IF NOT EXISTS `hou_geo_video_project_version` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint unsigned NOT NULL DEFAULT '0',
  `video_project_id` bigint unsigned NOT NULL,
  `version_no` int unsigned NOT NULL,
  `brief_json` json DEFAULT NULL,
  `script_json` json DEFAULT NULL,
  `storyboard_json` json DEFAULT NULL,
  `avatar_json` json DEFAULT NULL,
  `packaging_json` json DEFAULT NULL,
  `publish_json` json DEFAULT NULL,
  `batch_draft_json` json DEFAULT NULL COMMENT '完整批量视频草稿，包含方案、覆盖配置和任务状态',
  `submitted` tinyint unsigned NOT NULL DEFAULT '0',
  `source_job_id` bigint unsigned DEFAULT NULL,
  `creator_id` bigint unsigned DEFAULT NULL,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_project_version` (`video_project_id`,`version_no`),
  KEY `idx_tenant_project` (`tenant_id`,`video_project_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='AI video immutable version';

CREATE TABLE IF NOT EXISTS `hou_geo_video_review` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint unsigned NOT NULL DEFAULT '0',
  `video_project_id` bigint unsigned NOT NULL,
  `version_id` bigint unsigned NOT NULL,
  `job_id` bigint unsigned DEFAULT NULL,
  `status` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL,
  `timecode_ms` bigint unsigned DEFAULT NULL,
  `comment` varchar(1000) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `reviewer_id` bigint unsigned DEFAULT NULL,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_project_version_time` (`video_project_id`,`version_id`,`create_time`),
  KEY `idx_tenant_status` (`tenant_id`,`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='AI video review';

CREATE TABLE IF NOT EXISTS `hou_geo_visibility_daily` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint unsigned NOT NULL DEFAULT '0',
  `project_id` bigint unsigned DEFAULT NULL,
  `stat_date` date NOT NULL,
  `mention_count` int DEFAULT '0',
  `avg_rank` decimal(6,2) DEFAULT NULL,
  `sov` decimal(6,2) DEFAULT NULL,
  `model_count` int DEFAULT '0',
  `platform_stats_json` json DEFAULT NULL COMMENT '分平台SOV',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_tenant_date` (`tenant_id`,`project_id`,`stat_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='能见度日聚合';

CREATE TABLE IF NOT EXISTS `hou_geo_visibility_detail` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint unsigned NOT NULL DEFAULT '0',
  `project_id` bigint unsigned DEFAULT NULL,
  `question_id` bigint unsigned DEFAULT NULL,
  `ai_model_id` bigint unsigned DEFAULT NULL,
  `question` varchar(512) COLLATE utf8mb4_unicode_ci NOT NULL,
  `platform` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '模型显示名',
  `mentioned` tinyint NOT NULL DEFAULT '0',
  `rank_no` decimal(6,2) DEFAULT NULL COMMENT '位次',
  `sov` decimal(6,2) DEFAULT NULL COMMENT 'SOV%',
  `raw_answer` mediumtext COLLATE utf8mb4_unicode_ci COMMENT '原始回答',
  `collect_time` datetime NOT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_collect` (`tenant_id`,`project_id`,`collect_time`),
  KEY `idx_question_model` (`question_id`,`ai_model_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='品牌能见度明细';

CREATE TABLE IF NOT EXISTS `hou_geo_webhook_config` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint unsigned NOT NULL DEFAULT '0',
  `callback_url` varchar(512) COLLATE utf8mb4_unicode_ci NOT NULL,
  `events_json` json DEFAULT NULL,
  `secret` varchar(256) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `status` tinyint DEFAULT '1',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_tenant` (`tenant_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Webhook配置';

CREATE TABLE IF NOT EXISTS `hou_geo_webhook_log` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint unsigned NOT NULL DEFAULT '0',
  `webhook_id` bigint unsigned DEFAULT NULL,
  `event_type` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `callback_url` varchar(512) COLLATE utf8mb4_unicode_ci NOT NULL,
  `http_status` int DEFAULT NULL,
  `response_body` varchar(2000) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `payload_json` json DEFAULT NULL,
  `success` tinyint NOT NULL DEFAULT '0',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_tenant_time` (`tenant_id`,`create_time`),
  KEY `idx_webhook` (`webhook_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Webhook投递日志';

CREATE TABLE IF NOT EXISTS `hou_geo_workflow_task` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint unsigned NOT NULL DEFAULT '0',
  `project_id` bigint unsigned DEFAULT NULL,
  `title` varchar(256) COLLATE utf8mb4_unicode_ci NOT NULL,
  `description` text COLLATE utf8mb4_unicode_ci COMMENT '任务背景、修复步骤与验收标准',
  `task_type` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '内容/技术/链接/分发',
  `priority` varchar(16) COLLATE utf8mb4_unicode_ci DEFAULT '中',
  `assignee_id` bigint unsigned DEFAULT NULL,
  `assignee_name` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `status` varchar(32) COLLATE utf8mb4_unicode_ci DEFAULT '待开始',
  `due_date` date DEFAULT NULL,
  `module_code` varchar(32) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'A1/16/12等',
  `workflow_ref_id` bigint unsigned DEFAULT NULL COMMENT '关联hou_workflow',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_deleted` tinyint NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `idx_tenant_status` (`tenant_id`,`status`,`due_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='GEO工作流任务';

SET FOREIGN_KEY_CHECKS=@GEO_OLD_FOREIGN_KEY_CHECKS;
