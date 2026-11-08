-- ============================================================
-- AI 中台 · 智能体管理 后台菜单（2026-10-27）
--
-- ⚠️⚠️ 执行目标库：①【宿主 3306 的 meoc】，不是 fresh12-mysql 容器副本
--     连宿主 3306 meoc（凭据见 Nacos ai.yaml） < 本文件
--     判别：SELECT COUNT(*) FROM hou_ai_balance; 真库 302。
--
-- 说明：挂在「AI智能」(pid=242) 下，sort=0 使其排在子菜单首位；
--       前端同时须 bump userMenusCache.js 的 MENU_CACHE_KEY。
-- ============================================================

INSERT INTO `hou_permission_menu`
(`id`, `pid`, `type`, `name`, `icon`, `sort`, `perms`, `paths`, `component`, `selected`, `params`,
 `is_cache`, `is_show`, `is_disable`, `create_time`, `update_time`)
VALUES
(61353, 242, 'C', '智能体管理', 'MagicStick', 0, 'shell:ai:agent', '/ai/agents', 'ai/AgentList', '', '', 0, 1, 0, UNIX_TIMESTAMP(), UNIX_TIMESTAMP());

-- 与原 6 个 AI 子页保持一致：授权给角色 1 / 2 / 5
INSERT IGNORE INTO `hou_permission_role_menu` (`role_id`, `menu_id`) VALUES
(1, 61353), (2, 61353), (5, 61353);

-- ---------------- 复核（必须看到「智能体管理」） ----------------
SELECT id, pid, name, paths, component, sort FROM hou_permission_menu WHERE id = 61353;
SELECT menu_id, GROUP_CONCAT(role_id) AS roles FROM hou_permission_role_menu WHERE menu_id = 61353 GROUP BY menu_id;
