-- 插件中心(id=4) 移出左侧一级菜单：管理入口改由右上角「插件应用」面板底部「前往插件中心」进入。
-- 2026-10-08 项目负责人确认：左侧与右上角不重复展示插件中心。

CREATE TABLE IF NOT EXISTS hou_permission_menu_bak_addons_left_20261008 AS
SELECT * FROM hou_permission_menu WHERE id = 4;

UPDATE hou_permission_menu
SET display_pos = 'hidden',
    update_time = UNIX_TIMESTAMP()
WHERE id = 4
  AND pid = 0
  AND perms = 'shell:addons'
  AND display_pos = 'left';
