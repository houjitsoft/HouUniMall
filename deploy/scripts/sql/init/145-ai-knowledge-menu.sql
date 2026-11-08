-- ============================================================
-- AI 中台 · 知识库（对接独立部署的 WeKnora）后台菜单 + 配置项（2026-10-29）
--
-- ⚠️⚠️ 执行目标库：①【宿主 3306 的 meoc】，不是 fresh12-mysql 容器副本
--     连宿主 3306 meoc（凭据见 Nacos ai.yaml） < 本文件
--
-- 说明：
--   1. 菜单挂在「AI智能」(pid=242) 下，sort=1，排在「智能对话」(sort=2) 之后；
--      前端同时须 bump userMenusCache.js 的 MENU_CACHE_KEY（v54 -> v55）。
--   2. 配置项落 hou_ai_config，由 aiagent 的 WeKnoraClient 读取（库优先、环境变量兜底）。
--      🔴 weknora_api_key 的实际值不入库脚本、不进仓库，部署后在目标环境单独 UPDATE。
--   3. 幂等：菜单用 INSERT IGNORE（固定 id），配置用 INSERT ... ON DUPLICATE KEY UPDATE 空值不覆盖。
-- ============================================================

INSERT IGNORE INTO `hou_permission_menu`
(`id`, `pid`, `type`, `name`, `icon`, `sort`, `perms`, `paths`, `component`, `selected`, `params`,
 `is_cache`, `is_show`, `is_disable`, `create_time`, `update_time`)
VALUES
(61354, 242, 'C', '知识库', 'Collection', 1, 'shell:ai:knowledge', '/ai/knowledge', 'ai/AiKnowledge', '', '', 0, 1, 0, UNIX_TIMESTAMP(), UNIX_TIMESTAMP());

-- 与其它 AI 子页保持一致：授权给角色 1 / 2 / 5
INSERT IGNORE INTO `hou_permission_role_menu` (`role_id`, `menu_id`) VALUES
(1, 61354), (2, 61354), (5, 61354);

-- 配置项：地址可默认，凭据留空待填
INSERT INTO `hou_ai_config` (`config_key`, `config_value`, `config_group`, `config_desc`)
VALUES
  ('weknora_base_url', 'http://127.0.0.1:8080', 'knowledge', 'WeKnora 服务地址（aiagent 为 host 网络，直连宿主回环）'),
  ('weknora_api_key',  '',                      'knowledge', 'WeKnora 租户级 API Key（capabilities 含 retrieve/chat），留空表示未接入')
ON DUPLICATE KEY UPDATE
  `config_group` = VALUES(`config_group`),
  `config_desc`  = VALUES(`config_desc`),
  `config_value` = IF(`hou_ai_config`.`config_value` IS NULL OR `hou_ai_config`.`config_value` = '', VALUES(`config_value`), `hou_ai_config`.`config_value`);

-- ---------------- 复核（必须看到「知识库」一行 + 两条配置） ----------------
SELECT id, pid, name, paths, component, sort FROM hou_permission_menu WHERE id = 61354;
SELECT menu_id, GROUP_CONCAT(role_id) AS roles FROM hou_permission_role_menu WHERE menu_id = 61354 GROUP BY menu_id;
SELECT config_key, config_group, LEFT(config_value, 32) AS v FROM hou_ai_config WHERE config_group = 'knowledge';
