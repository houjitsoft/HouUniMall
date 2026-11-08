-- ============================================================
-- AI 中台前台菜单补齐：用量统计 / 余额配额（2026-10-27）
--
-- 背景：aiagent 作为平台级 AI 中台上线后，BFF 已暴露 /web/ai/usage/** 与 /web/ai/balance/**，
--       后台页面（views/ai/AiUsage.vue、AiBalance.vue）与路由（router/modules/ai）也已补齐，
--       但菜单树里只有原先 6 个子页（61203~61208），这两个新页面入口不存在，用户看不到。
--
-- 幂等：INSERT IGNORE 按主键，可重复执行
-- ============================================================

-- ⚠️⚠️ 执行目标库（血泪）：①上 MySQL 有两个，本脚本必须跑在【宿主 3306 的 meoc】，
--    即 aiagent / shell 等服务实际连的库（连接串与凭据见 Nacos ai.yaml，不入库本文件）。
--    本机连法：ssh 到构建服务器后，按 Nacos ai.yaml 里的 url/username/password 连宿主 3306 执行。
--    ❌ 不要跑进 fresh12-mysql 容器（docker exec fresh12-mysql ... meoc）：那是另一份同构副本，
--       写进去线上后台根本不读（2026-10-27 实测踩坑，白写一轮）。
--    判别方法：SELECT COUNT(*) FROM hou_ai_balance; 真库是 302，容器副本里 hou_ai_balance 表不存在。

-- ⚠️ 注意：61210 已被「AI概览 → 查看统计」占用（pid=61203），不可复用，
--    这里顺延到 61351 / 61352（INSERT IGNORE 会静默跳过冲突 id，务必执行后复核 name 是否写入成功）。
INSERT IGNORE INTO `hou_permission_menu`
(`id`, `pid`, `type`, `name`, `icon`, `sort`, `perms`, `paths`, `component`, `selected`, `params`,
 `is_cache`, `is_show`, `is_disable`, `create_time`, `update_time`)
VALUES
(61351, 242, 'C', 'AI用量统计', 'DataAnalysis', 7, 'shell:ai:usage',   '/ai/usage',   'ai/AiUsage',   '', '', 0, 1, 0, UNIX_TIMESTAMP(), UNIX_TIMESTAMP()),
(61352, 242, 'C', 'AI余额配额', 'Coin',         8, 'shell:ai:balance', '/ai/balance', 'ai/AiBalance', '', '', 0, 1, 0, UNIX_TIMESTAMP(), UNIX_TIMESTAMP());

-- 与既有 6 个 AI 子页保持一致：授权给角色 1 / 2 / 5
INSERT IGNORE INTO `hou_permission_role_menu` (`role_id`, `menu_id`) VALUES
(1, 61351), (2, 61351), (5, 61351),
(1, 61352), (2, 61352), (5, 61352);
