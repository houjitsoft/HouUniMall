-- =====================================================================
-- 插件发布 / 发布审核 两个菜单（插件中心 id=4 下的子项）
--
-- 背景：后端 /base/plugin-center/publish/** 六个端点早已齐备，但前端没有任何调用方，
--       「上传 → 维护 → 发布」只能 curl 直调 base。2026-10-27 补了
--       /platform/plugins/publish 与 /platform/plugins/review 两个页面，
--       这里把菜单入口挂到「插件中心」下（插件中心本身 display_pos=hidden，
--       入口在右上角「插件应用」面板底部「前往插件中心」，所以子项也不需要上左侧）。
--
-- 幂等：主键交给 AUTO_INCREMENT，按 paths 判重；角色授权按「原本有插件中心(4)权限的角色」
--       补齐，重复执行不会重复插入。
-- =====================================================================
SET NAMES utf8mb4;

-- 1) 插件发布
INSERT INTO `hou_permission_menu`
  (`pid`, `type`, `name`, `icon`, `sort`, `perms`, `paths`, `component`, `selected`, `params`,
   `is_cache`, `is_show`, `is_disable`, `create_time`, `update_time`, `display_pos`, `cate_id`, `plugin_code`)
SELECT 4, 'C', '插件发布', 'Upload', 60, 'shell:addons:publish',
       '/platform/plugins/publish', 'platform/plugins/publish', '', '',
       0, 1, 0, UNIX_TIMESTAMP(), UNIX_TIMESTAMP(), 'hidden', 0, ''
 WHERE NOT EXISTS (SELECT 1 FROM (SELECT `paths` FROM `hou_permission_menu`) t
                    WHERE t.`paths` = '/platform/plugins/publish')
   AND EXISTS (SELECT 1 FROM (SELECT `id` FROM `hou_permission_menu`) p WHERE p.`id` = 4);

-- 2) 发布审核
INSERT INTO `hou_permission_menu`
  (`pid`, `type`, `name`, `icon`, `sort`, `perms`, `paths`, `component`, `selected`, `params`,
   `is_cache`, `is_show`, `is_disable`, `create_time`, `update_time`, `display_pos`, `cate_id`, `plugin_code`)
SELECT 4, 'C', '发布审核', 'View', 61, 'shell:addons:review',
       '/platform/plugins/review', 'platform/plugins/review', '', '',
       0, 1, 0, UNIX_TIMESTAMP(), UNIX_TIMESTAMP(), 'hidden', 0, ''
 WHERE NOT EXISTS (SELECT 1 FROM (SELECT `paths` FROM `hou_permission_menu`) t
                    WHERE t.`paths` = '/platform/plugins/review')
   AND EXISTS (SELECT 1 FROM (SELECT `id` FROM `hou_permission_menu`) p WHERE p.`id` = 4);

-- 3) 授权：原本能进插件中心(4)的角色，一并补上这两个子项
INSERT IGNORE INTO `hou_permission_role_menu` (`role_id`, `menu_id`)
SELECT DISTINCT r.`role_id`, m.`id`
  FROM `hou_permission_role_menu` r
  JOIN `hou_permission_menu` m
    ON m.`paths` IN ('/platform/plugins/publish', '/platform/plugins/review')
 WHERE r.`menu_id` = 4;

-- 4) 自检输出（人工核对用；不影响退出码）
SELECT 'plugin_publish_menus' AS check_item, COUNT(*) AS hit
  FROM `hou_permission_menu`
 WHERE `paths` IN ('/platform/plugins/publish', '/platform/plugins/review');
