-- ============================================================
-- 清理「AI 中台自研知识库」半成品遗留表（2026-10-29）
--
-- ⚠️⚠️ 执行目标库：①【宿主 3306 的 meoc】，不是 fresh12-mysql 容器副本
--     连宿主 3306 meoc（凭据见 Nacos ai.yaml） < 本文件
--
-- 背景：
--   这 10 张表来自一次未完成的自研 RAG 后台（前端 Dataset/Evaluator/Experiment/
--   Observability/ModelConfig/Prompt 六个目录、21 个 Vue 页面，无路由、无后端实现）。
--   2026-10-29 确认改走「对接独立部署的 WeKnora」，该路线废弃：
--     · 前端 21 个孤儿页 + 5 个 api 文件已删除；
--     · 10-meoc-schema.sql 中的建表语句已移除；
--     · 生产库实测 10 张表全部 0 行。
--
-- 🔴 备份：
--   DDL 已导出到 ① /home/houjit/db-backup/hou_ai_admin_tables_ddl_20261029.sql（211 行）。
--   恢复：mysql -umeoc -p*** -h127.0.0.1 -P3306 meoc < 该文件
--
-- 幂等：DROP TABLE IF EXISTS，重复执行无副作用。
-- 注意：只删 hou_ai_admin_* 前缀；正在使用的 hou_ai_prompt / hou_ai_model /
--       hou_ai_workflow 等表不受影响。
-- ============================================================

DROP TABLE IF EXISTS
  `hou_ai_admin_dataset`,
  `hou_ai_admin_dataset_item`,
  `hou_ai_admin_evaluator`,
  `hou_ai_admin_experiment`,
  `hou_ai_admin_experiment_result`,
  `hou_ai_admin_model_config`,
  `hou_ai_admin_prompt`,
  `hou_ai_admin_prompt_version`,
  `hou_ai_admin_span`,
  `hou_ai_admin_trace`;

-- ---------------- 复核（必须返回空） ----------------
SELECT table_name FROM information_schema.tables
WHERE table_schema = DATABASE() AND table_name LIKE 'hou_ai_admin_%';

-- 对照：在用的 AI 表仍在
SELECT COUNT(*) AS ai_tables_left FROM information_schema.tables
WHERE table_schema = DATABASE() AND table_name LIKE 'hou_ai_%' AND table_name NOT LIKE 'hou_ai_admin_%';
