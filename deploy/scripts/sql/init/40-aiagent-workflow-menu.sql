-- ============================================================
-- AI 中台 · 「工作流编排」后台菜单（2026-10-29）
--
-- ⚠️⚠️ 执行目标库：①【宿主 3306 的 meoc】，不是 fresh12-mysql 容器副本
--     ssh RH2288 "mysql -umeoc -p... -h127.0.0.1 -P3306 --default-character-set=utf8mb4 meoc" < 本文件
--
-- 背景：
--   工作流引擎与画布早已上线，但左侧「AI智能」下只有「智能体」没有「工作流编排」，
--   入口藏在智能体页的说明条里。工作流是智能体的一种执行模式（前置资源），
--   应该给独立入口，不要每次都绕经智能体页。
--
-- 落点：挂在「AI智能」(pid=242) 下，sort=8999 —— 紧贴在「智能体」(sort=9000) 之后，
--   排序上的相邻正好对应「先有流程、再绑到智能体」的因果顺序。
--   前端路由已存在：/ai/workflows -> views/ai/WorkflowList.vue
--   （设计器 /ai/workflow/design 由列表页与智能体页新窗口打开，不单独挂菜单）
--
-- 依赖：32-aiagent-agent-menu.sql（「智能体」61353 与本菜单的角色集合来源）
-- 幂等：固定 id + INSERT IGNORE，重复执行结果一致。
--
-- 幂等：固定 id + INSERT IGNORE，重复执行结果一致。
-- ============================================================

INSERT IGNORE INTO `hou_permission_menu`
(`id`, `pid`, `type`, `name`, `icon`, `sort`, `perms`, `paths`, `component`, `selected`, `params`,
 `is_cache`, `is_show`, `is_disable`, `create_time`, `update_time`)
VALUES
(61355, 242, 'C', '工作流编排', 'Connection', 8999, 'shell:ai:workflow', '/ai/workflows', 'ai/WorkflowList',
 '', '', 0, 1, 0, UNIX_TIMESTAMP(), UNIX_TIMESTAMP());

-- 角色授权：与「智能体」(61353) 保持一致的角色集合（超管角色 1 / 管理员 2 / 5）
INSERT IGNORE INTO `hou_permission_role_menu` (`role_id`, `menu_id`)
SELECT r.role_id, 61355
FROM (SELECT DISTINCT role_id FROM `hou_permission_role_menu` WHERE menu_id = 61353) r;

SELECT id, pid, name, paths, component, sort, display_pos FROM `hou_permission_menu` WHERE id = 61355;
