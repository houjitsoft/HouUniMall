-- =====================================================================
-- aiagent 表结构与实体对齐（2026-10-27）
--
-- 背景：25-aiagent.sql 初建表时与 entity/*.java 字段不同步，服务起来后
-- 所有 /web/ai/** 接口一律返回 20601「数据保存失败」，实际是
--   java.sql.SQLSyntaxErrorException: Unknown column 'config_group' in 'field list'
-- 逐个补会重复踩坑，这里一次性对齐全部 8 张表。
--
-- 已执行：① RH2288，库 meoc
-- =====================================================================

-- 1) hou_ai_model：实体 AiModel 有 @TableLogic deleteFlag，表缺列
ALTER TABLE hou_ai_model
  ADD COLUMN delete_flag TINYINT NOT NULL DEFAULT 0 COMMENT '删除标记 0未删 1已删' AFTER update_time;

-- 2) hou_ai_prompt_category：实体 AiPromptCategory 有 deleteFlag，表缺列
ALTER TABLE hou_ai_prompt_category
  ADD COLUMN delete_flag TINYINT NOT NULL DEFAULT 0 COMMENT '删除标记 0未删 1已删' AFTER update_time;

-- 3) hou_ai_prompt：实体 AiPrompt 有 deleteFlag，表缺列
ALTER TABLE hou_ai_prompt
  ADD COLUMN delete_flag TINYINT NOT NULL DEFAULT 0 COMMENT '删除标记 0未删 1已删' AFTER update_time;

-- 4) hou_ai_config：实体 AiConfig 有 configGroup，表缺列
--    （remark 已在实体侧用 @TableField("config_desc") 映射到既有列，不动表）
--    2026-10-31 幂等化：10-meoc-schema 的 CREATE 已带上 config_group（对齐生产实库），
--    从零 init 链上这个 ALTER 会撞 Duplicate column（1060）中断整个 data 段。
--    守卫：列已存在则跳过（存量生产库补丁语义不变）。
SET @has_ai_config_group := (
  SELECT COUNT(*) FROM information_schema.columns
   WHERE table_schema = DATABASE() AND table_name = 'hou_ai_config' AND column_name = 'config_group');
SET @ai_config_group_sql := IF(@has_ai_config_group = 0,
  'ALTER TABLE hou_ai_config ADD COLUMN config_group VARCHAR(64) NOT NULL DEFAULT ''default'' COMMENT ''配置分组 gateway/model/default'' AFTER config_value',
  'SELECT ''hou_ai_config.config_group 已存在，跳过'' AS msg');
PREPARE ai_cfg_group FROM @ai_config_group_sql; EXECUTE ai_cfg_group; DEALLOCATE PREPARE ai_cfg_group;

-- 5) hou_ai_generation_record：实体 AiGenerationRecord 用的是
--    scene / model_name / error_msg / tokens / cost_amount，
--    而初建表是 type / model_id / input_params / cost_time 且 NOT NULL 无默认，
--    实体插入必然失败。该表当前 0 行，补齐实体所需列并放宽旧列为可空。
--    2026-10-31 幂等化：25-aiagent 的 CREATE 已是新结构（scene/model_name/
--    error_msg/tokens/cost_amount 全在），从零 init 链上这组 ALTER/MODIFY
--    会撞 Duplicate column（1060）或 Unknown column（1054，旧列已不在）。
--    守卫：scene 列不存在（=旧结构存量库）才执行整组。
SET @has_gen_scene := (
  SELECT COUNT(*) FROM information_schema.columns
   WHERE table_schema = DATABASE() AND table_name = 'hou_ai_generation_record' AND column_name = 'scene');
SET @gen_fix_sql := IF(@has_gen_scene = 0,
  'ALTER TABLE hou_ai_generation_record
     ADD COLUMN scene VARCHAR(64) DEFAULT NULL COMMENT ''生成场景'' AFTER user_id,
     ADD COLUMN model_name VARCHAR(100) DEFAULT NULL COMMENT ''使用模型'' AFTER scene,
     ADD COLUMN error_msg VARCHAR(1000) DEFAULT NULL COMMENT ''失败原因'' AFTER result,
     ADD COLUMN tokens INT NOT NULL DEFAULT 0 COMMENT ''消耗token'' AFTER status,
     ADD COLUMN cost_amount DECIMAL(12,4) NOT NULL DEFAULT 0 COMMENT ''费用'' AFTER tokens,
     MODIFY COLUMN type VARCHAR(50) DEFAULT NULL,
     MODIFY COLUMN model_id INT UNSIGNED DEFAULT NULL,
     MODIFY COLUMN cost_time INT DEFAULT 0',
  'SELECT ''hou_ai_generation_record 已为新结构，跳过'' AS msg');
PREPARE gen_fix FROM @gen_fix_sql; EXECUTE gen_fix; DEALLOCATE PREPARE gen_fix;

-- 复核用（执行后应无输出差异）
-- SELECT TABLE_NAME, COLUMN_NAME FROM information_schema.COLUMNS
--  WHERE TABLE_SCHEMA='meoc' AND TABLE_NAME LIKE 'hou_ai_%'
--    AND COLUMN_NAME IN ('delete_flag','config_group','scene','model_name','error_msg','tokens','cost_amount')
--  ORDER BY TABLE_NAME, COLUMN_NAME;
