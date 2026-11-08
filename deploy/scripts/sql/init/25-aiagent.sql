-- ============================================================
-- AI 智能体服务（aiagent / 注册名 ai）表结构
-- 承接 BFF /web/ai/** 全部接口：模型池、提示词、会话、生成记录、配置、用量
-- 幂等：全部 CREATE TABLE IF NOT EXISTS，可重复执行
-- ============================================================

-- 模型池：前端「AI模型管理」「AI设置」共用
CREATE TABLE IF NOT EXISTS `hou_ai_model` (
  `id`          bigint       NOT NULL AUTO_INCREMENT,
  `name`        varchar(128) NOT NULL COMMENT '模型名称，如 qwen-plus / mall-chat',
  `type`        varchar(32)  NOT NULL DEFAULT 'chat' COMMENT 'chat/text/image/code',
  `api_url`     varchar(512)          DEFAULT NULL COMMENT 'OpenAI 兼容地址；走网关时留空',
  `api_key`     varchar(512)          DEFAULT NULL COMMENT '网关档案密钥 sk-cp-…；禁止存厂商密钥',
  `temperature` decimal(3,2) NOT NULL DEFAULT 0.70,
  `max_tokens`  int          NOT NULL DEFAULT 2048,
  `status`      tinyint      NOT NULL DEFAULT 1 COMMENT '1启用 0禁用',
  `description` varchar(512)          DEFAULT NULL,
  `usage_count` bigint       NOT NULL DEFAULT 0,
  `sort`        int          NOT NULL DEFAULT 0,
  `create_time` datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `delete_flag` tinyint      NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_ai_model_name` (`name`, `delete_flag`),
  KEY `idx_ai_model_type` (`type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI 模型池';

-- 提示词分类
CREATE TABLE IF NOT EXISTS `hou_ai_prompt_category` (
  `id`          bigint       NOT NULL AUTO_INCREMENT,
  `name`        varchar(128) NOT NULL,
  `parent_id`   bigint       NOT NULL DEFAULT 0,
  `sort`        int          NOT NULL DEFAULT 0,
  `create_time` datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `delete_flag` tinyint      NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_ai_prompt_cat_parent` (`parent_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI 提示词分类';

-- 提示词模板
CREATE TABLE IF NOT EXISTS `hou_ai_prompt` (
  `id`          bigint       NOT NULL AUTO_INCREMENT,
  `name`        varchar(128) NOT NULL,
  `category_id` bigint       NOT NULL DEFAULT 0,
  `content`     text         NOT NULL COMMENT '提示词正文，支持 {input} 占位',
  `description` varchar(512)          DEFAULT NULL,
  `tags`        varchar(512)          DEFAULT NULL COMMENT '逗号分隔',
  `status`      tinyint      NOT NULL DEFAULT 1,
  `usage_count` bigint       NOT NULL DEFAULT 0,
  `create_time` datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `delete_flag` tinyint      NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_ai_prompt_category` (`category_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI 提示词模板';

-- AI 配置（key-value，前端「AI设置」按扁平 map 读写）
CREATE TABLE IF NOT EXISTS `hou_ai_config` (
  `id`           bigint       NOT NULL AUTO_INCREMENT,
  `config_key`   varchar(128) NOT NULL,
  `config_value` varchar(2000)         DEFAULT NULL,
  `config_group` varchar(64)  NOT NULL DEFAULT 'global',
  `remark`       varchar(256)          DEFAULT NULL,
  `create_time`  datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time`  datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_ai_config_key` (`config_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI 配置项';

-- 会话
CREATE TABLE IF NOT EXISTS `hou_ai_chat_session` (
  `id`            bigint       NOT NULL AUTO_INCREMENT,
  `session_no`    varchar(64)  NOT NULL COMMENT '会话唯一编号，前端按它续聊',
  `title`         varchar(256)          DEFAULT NULL,
  `model_name`    varchar(128)          DEFAULT NULL,
  `user_id`       bigint       NOT NULL DEFAULT 0,
  `message_count` int          NOT NULL DEFAULT 0,
  `create_time`   datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time`   datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `delete_flag`   tinyint      NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_ai_chat_session_no` (`session_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI 会话';

-- 会话消息（合规留痕的真相源）
CREATE TABLE IF NOT EXISTS `hou_ai_chat_message` (
  `id`          bigint       NOT NULL AUTO_INCREMENT,
  `session_id`  bigint       NOT NULL,
  `role`        varchar(16)  NOT NULL COMMENT 'user/assistant/system/tool',
  `content`     text,
  `model_name`  varchar(128)          DEFAULT NULL,
  `tokens`      int          NOT NULL DEFAULT 0,
  `create_time` datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `delete_flag` tinyint      NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_ai_msg_session` (`session_id`, `id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI 会话消息';

-- 生成记录（文生文 / 生图 / 数据分析统一留痕）
CREATE TABLE IF NOT EXISTS `hou_ai_generation_record` (
  `id`           bigint        NOT NULL AUTO_INCREMENT,
  `scene`        varchar(64)   NOT NULL COMMENT 'text_generate/summary/translate/correct/image/data_analyze…',
  `model_name`   varchar(128)           DEFAULT NULL,
  `prompt`       text,
  `result`       mediumtext,
  `status`       tinyint       NOT NULL DEFAULT 1 COMMENT '1成功 0失败',
  `error_msg`    varchar(1024)          DEFAULT NULL,
  `tokens`       int           NOT NULL DEFAULT 0,
  `cost_amount`  decimal(12,4) NOT NULL DEFAULT 0,
  `user_id`      bigint        NOT NULL DEFAULT 0,
  `create_time`  datetime      NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_ai_gen_scene` (`scene`, `id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI 生成记录';

-- 用量日结：以网关 token 为准，按场景/模型归因，用于成本分摊
CREATE TABLE IF NOT EXISTS `hou_ai_usage_daily` (
  `id`                bigint       NOT NULL AUTO_INCREMENT,
  `stat_date`         date         NOT NULL,
  `scene`             varchar(64)  NOT NULL DEFAULT 'total',
  `model_name`        varchar(128) NOT NULL DEFAULT '',
  `request_count`     bigint       NOT NULL DEFAULT 0,
  `prompt_tokens`     bigint       NOT NULL DEFAULT 0,
  `completion_tokens` bigint       NOT NULL DEFAULT 0,
  `total_tokens`      bigint       NOT NULL DEFAULT 0,
  `create_time`       datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time`       datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_ai_usage_daily` (`stat_date`, `scene`, `model_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI 用量日结';

-- 默认配置：先落一份，保证 /ai/config/all 首次调用不为空
INSERT IGNORE INTO `hou_ai_config` (`config_key`, `config_value`, `config_group`, `config_desc`) VALUES
  ('default_model', '', 'global', '默认模型名，留空表示由 AI 网关路由决定'),
  ('timeout', '60', 'global', '单次调用超时（秒）'),
  ('max_retries', '1', 'global', '重试次数；网关自带故障切换链，务必保持 1'),
  ('enable_cache', '0', 'global', '是否开启结果缓存 1/0'),
  ('cache_time', '3600', 'global', '缓存有效期（秒）'),
  ('encrypt_api_keys', '1', 'security', '密钥加密存储'),
  ('enable_access_log', '1', 'security', '记录调用日志'),
  ('enable_content_filter', '1', 'security', '输出内容过滤'),
  ('enable_sensitive_word_detection', '1', 'security', '敏感词检测'),
  ('ai_gateway_base_url', '', 'gateway', 'AI 网关 OpenAI 兼容地址，如 http://127.0.0.1:18301/v1'),
  ('ai_gateway_profile_key', '', 'gateway', '网关签发的档案密钥 sk-cp-…，禁止填厂商密钥');

-- 预置分类：保证提示词页首次打开有分类可选
INSERT IGNORE INTO `hou_ai_prompt_category` (`id`, `name`, `parent_id`, `sort`) VALUES
  (1, '通用', 0, 100),
  (2, '商品文案', 0, 90),
  (3, '客服话术', 0, 80),
  (4, '营销活动', 0, 70);
