-- ============================================================================
-- 29 - GEO 的 AI 治理表切换到 AI 中台（hou_ai_*），GEO 代码零改动
-- ============================================================================
-- 背景：aiagent 定为平台级 AI 中台，GEO 的 AI 治理能力迁入，表名统一 hou_ai_* 前缀。
--
-- 做法：把 hou_geo_ai_model / hou_geo_ai_balance 改为指向中台表的**可更新视图**。
--   好处：GEO 现有代码与运行镜像完全不用改，读写自动落到 hou_ai_model / hou_ai_balance；
--         出问题可秒级回滚（DROP VIEW + RENAME 备份表回来）。
--
-- 为什么安全（已核对 GeoSchemaInitializer）：
--   - tableExists() / ensureTableColumn() 查 information_schema，
--     视图同样登记在 TABLES / COLUMNS 中，故启动期 ALTER 会被判定"列已存在"而跳过，不会报错。
--   - ensureAiModelCatalog() 的 INSERT IGNORE 三条（perplexity/claude/gemini）code 已存在，会被忽略。
--   - migrateLegacyAiBalances() 是 UPDATE，可更新视图会落到基表。
--
-- ⚠️ 语义区分：hou_geo_ai_model 存的是「GEO 要分析的 AI 问答平台」（被分析对象），
--    与中台「可调用的 LLM」语义不同。故迁移后统一打 type='geo_target'，
--    视图只暴露 type='geo_target'，避免 GEO 页面混入可调用模型。
--
-- 回滚：DROP VIEW hou_geo_ai_model; RENAME TABLE hou_geo_ai_model_bak_20261027 TO hou_geo_ai_model;
--       DROP VIEW hou_geo_ai_balance; RENAME TABLE hou_geo_ai_balance_bak_20261027 TO hou_geo_ai_balance;
--
-- 本脚本幂等：用 information_schema 判断对象类型后再决定改名/删视图，可重复执行。
-- ============================================================================

-- ----------------------------------------------------------------------------
-- 零、切换前先把两侧语义与增量对齐
-- ----------------------------------------------------------------------------
-- 迁移过去的 12 个 GEO 平台，语义上标为 geo_target（不是可调用的 chat 模型）
UPDATE hou_ai_model
SET type = 'geo_target'
WHERE code IN ('chatgpt','deepseek','wenxin','tongyi','doubao','kimi',
               'yuanbao','zhipu','nano','perplexity','claude','gemini')
  AND type <> 'geo_target';

-- 切换前最后一次余额增量同步（GEO 线上仍在写旧表，避免切走时丢这几秒的数据）
INSERT INTO hou_ai_balance
  (subject_type, subject_key, free_quota_tokens, free_used_tokens,
   free_quota_started_at, free_quota_expires_at, used_tokens, purchased_balance_tokens,
   create_time, update_time)
SELECT
  b.subject_type, b.subject_key, b.free_quota_tokens, b.free_used_tokens,
  b.free_quota_started_at, b.free_quota_expires_at, b.used_tokens, b.purchased_balance_tokens,
  b.create_time, b.update_time
FROM hou_geo_ai_balance b
ON DUPLICATE KEY UPDATE
  free_quota_tokens        = VALUES(free_quota_tokens),
  free_used_tokens         = VALUES(free_used_tokens),
  free_quota_started_at    = VALUES(free_quota_started_at),
  free_quota_expires_at    = VALUES(free_quota_expires_at),
  used_tokens              = VALUES(used_tokens),
  purchased_balance_tokens = VALUES(purchased_balance_tokens),
  update_time              = VALUES(update_time);

-- ----------------------------------------------------------------------------
-- 一、hou_geo_ai_model -> 视图（指向 hou_ai_model，仅暴露 geo_target）
-- ----------------------------------------------------------------------------
SET @tt  := (SELECT TABLE_TYPE FROM information_schema.TABLES
             WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'hou_geo_ai_model');
SET @bak := (SELECT COUNT(*) FROM information_schema.TABLES
             WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'hou_geo_ai_model_bak_20261027');
SET @sql := CASE
  WHEN @tt IS NULL            THEN 'SELECT ''skip: hou_geo_ai_model missing'''
  WHEN @tt = 'VIEW'           THEN 'DROP VIEW hou_geo_ai_model'
  WHEN @bak > 0               THEN 'DROP TABLE hou_geo_ai_model'
  ELSE 'RENAME TABLE hou_geo_ai_model TO hou_geo_ai_model_bak_20261027'
END;
PREPARE s FROM @sql; EXECUTE s; DEALLOCATE PREPARE s;

CREATE OR REPLACE
  ALGORITHM = MERGE
  SQL SECURITY INVOKER
VIEW hou_geo_ai_model AS
SELECT
  m.id          AS id,
  m.code        AS code,
  m.name        AS name,
  m.region      AS region,
  m.vendor      AS vendor,
  m.status      AS status,
  m.sort        AS sort_order,
  m.create_time AS create_time,
  m.update_time AS update_time
FROM hou_ai_model m
WHERE m.delete_flag = 0
  AND m.type = 'geo_target';

-- 补齐切换窗口内旧表可能新增的平台（旧表此后不再更新）
INSERT IGNORE INTO hou_ai_model
  (code, name, vendor, region, type, api_url, api_key, description, status, sort, usage_count, delete_flag)
SELECT b.code, b.name, b.vendor, COALESCE(NULLIF(b.region,''),'domestic'), 'geo_target', '', '',
       CONCAT(COALESCE(b.vendor,''), ' ', b.name), b.status, b.sort_order, 0, 0
FROM hou_geo_ai_model_bak_20261027 b
WHERE b.code IS NOT NULL AND b.code <> '';

-- ----------------------------------------------------------------------------
-- 二、hou_geo_ai_balance -> 视图（指向 hou_ai_balance，可读写）
-- ----------------------------------------------------------------------------
SET @tt  := (SELECT TABLE_TYPE FROM information_schema.TABLES
             WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'hou_geo_ai_balance');
SET @bak := (SELECT COUNT(*) FROM information_schema.TABLES
             WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'hou_geo_ai_balance_bak_20261027');
SET @sql := CASE
  WHEN @tt IS NULL            THEN 'SELECT ''skip: hou_geo_ai_balance missing'''
  WHEN @tt = 'VIEW'           THEN 'DROP VIEW hou_geo_ai_balance'
  WHEN @bak > 0               THEN 'DROP TABLE hou_geo_ai_balance'
  ELSE 'RENAME TABLE hou_geo_ai_balance TO hou_geo_ai_balance_bak_20261027'
END;
PREPARE s FROM @sql; EXECUTE s; DEALLOCATE PREPARE s;

CREATE OR REPLACE
  ALGORITHM = MERGE
  SQL SECURITY INVOKER
VIEW hou_geo_ai_balance AS
SELECT
  b.id                       AS id,
  b.subject_type             AS subject_type,
  b.subject_key              AS subject_key,
  b.free_quota_tokens        AS free_quota_tokens,
  b.free_used_tokens         AS free_used_tokens,
  b.free_quota_started_at    AS free_quota_started_at,
  b.free_quota_expires_at    AS free_quota_expires_at,
  b.used_tokens              AS used_tokens,
  b.purchased_balance_tokens AS purchased_balance_tokens,
  b.create_time              AS create_time,
  b.update_time              AS update_time
FROM hou_ai_balance b;

-- 补齐切换窗口内旧表的新增主体
INSERT IGNORE INTO hou_ai_balance
  (subject_type, subject_key, free_quota_tokens, free_used_tokens,
   free_quota_started_at, free_quota_expires_at, used_tokens, purchased_balance_tokens,
   create_time, update_time)
SELECT b.subject_type, b.subject_key, b.free_quota_tokens, b.free_used_tokens,
       b.free_quota_started_at, b.free_quota_expires_at, b.used_tokens, b.purchased_balance_tokens,
       b.create_time, b.update_time
FROM hou_geo_ai_balance_bak_20261027 b;

-- ============================================================================
-- 验收：
--   SELECT TABLE_NAME, TABLE_TYPE FROM information_schema.TABLES
--     WHERE TABLE_SCHEMA='meoc' AND TABLE_NAME IN ('hou_geo_ai_model','hou_geo_ai_balance');
--     -- 预期均为 VIEW
--   SELECT COUNT(*) FROM hou_geo_ai_model;    -- 预期 12
--   SELECT COUNT(*) FROM hou_geo_ai_balance;  -- 预期 >= 302
--   SELECT COUNT(*) FROM hou_ai_model;        -- 预期 15
--   SELECT COUNT(*) FROM hou_ai_balance;      -- 预期 >= 302
--   写入验证（确认视图可更新，落到基表）：
--   INSERT INTO hou_geo_ai_balance (subject_type, subject_key) VALUES ('guest','__cutover_probe__');
--   SELECT COUNT(*) FROM hou_ai_balance WHERE subject_key='__cutover_probe__';  -- 应为 1
--   DELETE FROM hou_geo_ai_balance WHERE subject_key='__cutover_probe__';
-- ============================================================================
