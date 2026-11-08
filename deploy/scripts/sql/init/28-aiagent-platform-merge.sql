-- ============================================================================
-- 28 - AI 中台数据层统一：GEO 的 AI 治理能力迁至 aiagent，表名统一 hou_ai_*
-- ============================================================================
-- 决策：aiagent 作为平台级 AI 中台，GEO 的 AI 治理能力（模型档案 / 余额配额 / 计量）迁入。
--      所有中台表统一 hou_ai_* 前缀。
--
-- 迁入范围（AI 中台能力）：
--   hou_geo_ai_model    (12 行)  -> hou_ai_model    (合并模型档案，code 为唯一键)
--   hou_geo_ai_balance  (287 行) -> hou_ai_balance  (新建，租户/访客 AI 余额与免费配额)
--   hou_ai_token_usage  (1316 行) -> 原地保留，前缀已合规，仅所有权划转给 aiagent
--
-- 保留在 GEO（属 GEO 业务域，不是中台能力，不迁）：
--   hou_geo_ai_model_profile     项目维度的模型画像（geo_score）
--   hou_geo_ai_studio_keyword    AI Studio 扩词产出
--   hou_geo_ai_studio_expand_batch / hou_geo_ai_discoverability / hou_geo_ai_snippet
--   hou_geo_model_adapt_rule     GEO 平台适配规则
--
-- 本脚本幂等，可重复执行。
-- ============================================================================

-- ----------------------------------------------------------------------------
-- 一、hou_ai_model 补齐中台字段：code（唯一）/ vendor / region
-- ----------------------------------------------------------------------------
SET @sql := (SELECT IF(
  EXISTS(SELECT 1 FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE()
         AND TABLE_NAME = 'hou_ai_model' AND COLUMN_NAME = 'code'),
  'SELECT ''skip add hou_ai_model.code''',
  'ALTER TABLE hou_ai_model ADD COLUMN code varchar(64) NULL COMMENT ''模型编码（迁移自 hou_geo_ai_model.code）'' AFTER name'
));
PREPARE s FROM @sql; EXECUTE s; DEALLOCATE PREPARE s;

SET @sql := (SELECT IF(
  EXISTS(SELECT 1 FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE()
         AND TABLE_NAME = 'hou_ai_model' AND COLUMN_NAME = 'vendor'),
  'SELECT ''skip add hou_ai_model.vendor''',
  'ALTER TABLE hou_ai_model ADD COLUMN vendor varchar(64) NULL COMMENT ''厂商：OpenAI / DeepSeek / Baidu 等'' AFTER code'
));
PREPARE s FROM @sql; EXECUTE s; DEALLOCATE PREPARE s;

SET @sql := (SELECT IF(
  EXISTS(SELECT 1 FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE()
         AND TABLE_NAME = 'hou_ai_model' AND COLUMN_NAME = 'region'),
  'SELECT ''skip add hou_ai_model.region''',
  'ALTER TABLE hou_ai_model ADD COLUMN region varchar(32) NOT NULL DEFAULT ''domestic'' COMMENT ''domestic 国内 / global 海外'' AFTER vendor'
));
PREPARE s FROM @sql; EXECUTE s; DEALLOCATE PREPARE s;

-- 回填既有 3 行预置模型的编码与厂商（避免与 GEO 迁移数据撞名）
UPDATE hou_ai_model SET code = 'gpt-4',   vendor = 'OpenAI',    region = 'global'   WHERE id = 1 AND (code IS NULL OR code = '');
UPDATE hou_ai_model SET code = 'claude3', vendor = 'Anthropic', region = 'global'   WHERE id = 2 AND (code IS NULL OR code = '');
UPDATE hou_ai_model SET code = 'dalle3',  vendor = 'OpenAI',    region = 'global'   WHERE id = 3 AND (code IS NULL OR code = '');

-- 唯一键：先清理可能的空值，再建（幂等）
SET @sql := (SELECT IF(
  EXISTS(SELECT 1 FROM information_schema.STATISTICS WHERE TABLE_SCHEMA = DATABASE()
         AND TABLE_NAME = 'hou_ai_model' AND INDEX_NAME = 'uk_code'),
  'SELECT ''skip index uk_code''',
  'ALTER TABLE hou_ai_model ADD UNIQUE KEY uk_code (code)'
));
PREPARE s FROM @sql; EXECUTE s; DEALLOCATE PREPARE s;

-- ----------------------------------------------------------------------------
-- 二、迁移 GEO 模型档案（12 行）到 hou_ai_model
--     GEO 侧走的是 dify/网关，没有直连 api_url/api_key，type 统一先落 chat
-- ----------------------------------------------------------------------------
INSERT IGNORE INTO hou_ai_model
  (code, name, vendor, region, type, api_url, api_key, description, status, sort, usage_count, delete_flag)
SELECT
  m.code,
  m.name,
  m.vendor,
  COALESCE(NULLIF(m.region, ''), 'domestic'),
  'chat',
  '',
  '',
  CONCAT(COALESCE(m.vendor, ''), ' ', m.name),
  m.status,
  m.sort_order,
  0,
  0
FROM hou_geo_ai_model m
WHERE m.code IS NOT NULL AND m.code <> '';

-- ----------------------------------------------------------------------------
-- 三、新建 hou_ai_balance（AI 余额与免费配额，从 hou_geo_ai_balance 结构对齐）
-- ----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS hou_ai_balance (
  id                      bigint       NOT NULL AUTO_INCREMENT,
  subject_type            varchar(16)  NOT NULL                COMMENT '主体类型：tenant 租户 / guest 访客 / user 用户',
  subject_key             varchar(64)  NOT NULL                COMMENT '主体标识：租户ID 或 访客标识',
  free_quota_tokens       bigint       NOT NULL DEFAULT 100000 COMMENT '免费额度（token）',
  free_used_tokens        bigint       NOT NULL DEFAULT 0      COMMENT '免费额度已用（token）',
  free_quota_started_at   datetime     NULL                    COMMENT '免费额度生效时间',
  free_quota_expires_at   datetime     NULL                    COMMENT '免费额度到期时间',
  used_tokens             bigint       NOT NULL DEFAULT 0      COMMENT '累计已用 token（含付费）',
  purchased_balance_tokens bigint      NOT NULL DEFAULT 0      COMMENT '已购余额（token）',
  create_time             datetime     NULL     DEFAULT CURRENT_TIMESTAMP,
  update_time             datetime     NULL     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uk_subject (subject_type, subject_key),
  KEY idx_update_time (update_time)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = 'AI 中台 - 主体余额与配额';

-- 迁移余额（287 行）
INSERT IGNORE INTO hou_ai_balance
  (subject_type, subject_key, free_quota_tokens, free_used_tokens,
   free_quota_started_at, free_quota_expires_at, used_tokens, purchased_balance_tokens,
   create_time, update_time)
SELECT
  b.subject_type, b.subject_key, b.free_quota_tokens, b.free_used_tokens,
  b.free_quota_started_at, b.free_quota_expires_at, b.used_tokens, b.purchased_balance_tokens,
  b.create_time, b.update_time
FROM hou_geo_ai_balance b;

-- ----------------------------------------------------------------------------
-- 四、hou_ai_token_usage 所有权划转（前缀已合规，无需改结构）
--     补充 module_name 默认值约束说明：module_name=geo 为 GEO 历史数据，
--     迁入后 aiagent 写入统一为 module_name=aiagent，历史数据原样保留。
-- ----------------------------------------------------------------------------
SET @sql := (SELECT IF(
  EXISTS(SELECT 1 FROM information_schema.STATISTICS WHERE TABLE_SCHEMA = DATABASE()
         AND TABLE_NAME = 'hou_ai_token_usage' AND INDEX_NAME = 'idx_module_scene'),
  'SELECT ''skip index idx_module_scene''',
  'ALTER TABLE hou_ai_token_usage ADD KEY idx_module_scene (module_name, scene)'
));
PREPARE s FROM @sql; EXECUTE s; DEALLOCATE PREPARE s;

-- ============================================================================
-- 验收：
--   SELECT COUNT(*) FROM hou_ai_model;     -- 预期 15（3 预置 + 12 迁移）
--   SELECT COUNT(*) FROM hou_ai_balance;   -- 预期 287
--   SELECT COUNT(*) FROM hou_ai_token_usage; -- 预期 1316（原样）
-- ============================================================================
