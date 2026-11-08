-- 拼团插件补齐二级菜单：拼团设置 / 拼团报表（1.0.0 → 1.0.1）
-- 备份：hou_permission_menu_bak_pintuan_20261103
-- 菜单中文名一律 UNHEX 写入。客户端若不是 utf8mb4，直接写中文会把
-- 「拼团管理」存成 æ‹¼å›¢ç®¡ç†（评估站 2026-11-03 已发生）。
SET NAMES utf8mb4;

CREATE TABLE IF NOT EXISTS hou_permission_menu_bak_pintuan_20261103 LIKE hou_permission_menu;
INSERT IGNORE INTO hou_permission_menu_bak_pintuan_20261103
SELECT * FROM hou_permission_menu
WHERE plugin_code = 'order-pintuan' OR perms LIKE 'order-pintuan:%' OR paths LIKE '/plugin/order-pintuan%';

-- 确保根菜单存在（已装插件时通常已有）
INSERT INTO hou_permission_menu
(pid, type, name, icon, sort, perms, paths, component, selected, params, plugin_code, is_cache, is_show, is_disable, display_pos, cate_id, create_time, update_time)
SELECT 0, 'M', CONVERT(UNHEX('E68BBCE59BA2') USING utf8mb4), 'UserFilled', 3230, 'order-pintuan:root', '/plugin/order-pintuan', '', '', 'plugin:order-pintuan',
       'order-pintuan', 0, 1, 0, 'plugin',
       (SELECT id FROM hou_addon_cate WHERE name LIKE '%营销%' ORDER BY id LIMIT 1),
       UNIX_TIMESTAMP(), UNIX_TIMESTAMP()
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM hou_permission_menu WHERE perms = 'order-pintuan:root' LIMIT 1);

SET @pintuan_root := (SELECT id FROM hou_permission_menu WHERE perms = 'order-pintuan:root' ORDER BY id DESC LIMIT 1);
SET @cate := (SELECT IFNULL(cate_id, 1) FROM hou_permission_menu WHERE id = @pintuan_root);

-- 拼团管理（已有则只校正 sort/pid）
INSERT INTO hou_permission_menu
(pid, type, name, icon, sort, perms, paths, component, selected, params, plugin_code, is_cache, is_show, is_disable, display_pos, cate_id, create_time, update_time)
SELECT @pintuan_root, 'C', CONVERT(UNHEX('E68BBCE59BA2E7AEA1E79086') USING utf8mb4), 'UserFilled', 3, 'order-pintuan:root:index', '/plugin/order-pintuan/index', '', '',
       'plugin:order-pintuan', 'order-pintuan', 0, 1, 0, 'left', @cate, UNIX_TIMESTAMP(), UNIX_TIMESTAMP()
FROM DUAL
WHERE @pintuan_root IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM hou_permission_menu WHERE perms = 'order-pintuan:root:index' LIMIT 1);

UPDATE hou_permission_menu
SET pid = @pintuan_root, sort = 3, name = CONVERT(UNHEX('E68BBCE59BA2E7AEA1E79086') USING utf8mb4), paths = '/plugin/order-pintuan/index',
    display_pos = 'left', is_show = 1, is_disable = 0, update_time = UNIX_TIMESTAMP()
WHERE perms = 'order-pintuan:root:index' AND @pintuan_root IS NOT NULL;

-- 拼团设置
INSERT INTO hou_permission_menu
(pid, type, name, icon, sort, perms, paths, component, selected, params, plugin_code, is_cache, is_show, is_disable, display_pos, cate_id, create_time, update_time)
SELECT @pintuan_root, 'C', CONVERT(UNHEX('E68BBCE59BA2E8AEBEE7BDAE') USING utf8mb4), 'Setting', 2, 'order-pintuan:root:settings', '/plugin/order-pintuan/settings', '', '',
       'plugin:order-pintuan', 'order-pintuan', 0, 1, 0, 'left', @cate, UNIX_TIMESTAMP(), UNIX_TIMESTAMP()
FROM DUAL
WHERE @pintuan_root IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM hou_permission_menu WHERE perms = 'order-pintuan:root:settings' LIMIT 1);

-- 拼团报表
INSERT INTO hou_permission_menu
(pid, type, name, icon, sort, perms, paths, component, selected, params, plugin_code, is_cache, is_show, is_disable, display_pos, cate_id, create_time, update_time)
SELECT @pintuan_root, 'C', CONVERT(UNHEX('E68BBCE59BA2E68AA5E8A1A8') USING utf8mb4), 'DataAnalysis', 1, 'order-pintuan:root:reports', '/plugin/order-pintuan/reports', '', '',
       'plugin:order-pintuan', 'order-pintuan', 0, 1, 0, 'left', @cate, UNIX_TIMESTAMP(), UNIX_TIMESTAMP()
FROM DUAL
WHERE @pintuan_root IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM hou_permission_menu WHERE perms = 'order-pintuan:root:reports' LIMIT 1);

-- 已写入的乱码名称按 perms 强制回正（与是否已存在无关）
UPDATE hou_permission_menu
SET name = CONVERT(UNHEX('E68BBCE59BA2E8AEBEE7BDAE') USING utf8mb4), update_time = UNIX_TIMESTAMP()
WHERE perms = 'order-pintuan:root:settings';

UPDATE hou_permission_menu
SET name = CONVERT(UNHEX('E68BBCE59BA2E68AA5E8A1A8') USING utf8mb4), update_time = UNIX_TIMESTAMP()
WHERE perms = 'order-pintuan:root:reports';

-- 授权超级管理员 + 普通用户（后台演示账号多为 role_id=2）
INSERT IGNORE INTO hou_permission_role_menu (role_id, menu_id)
SELECT r.role_id, m.id
FROM (SELECT 1 AS role_id UNION ALL SELECT 2) r
JOIN hou_permission_menu m
  ON (m.plugin_code = 'order-pintuan' OR m.perms LIKE 'order-pintuan:%');

-- 插件版本与前端入口
UPDATE hou_addon_plugin
SET version = '1.0.1',
    entry_path = 'index.mjs',
    update_time = NOW()
WHERE plugin_code = 'order-pintuan';

UPDATE hou_addon_plugin_install
SET version = '1.0.1', enabled = 1, installed = 1, deleted = 0, update_time = NOW()
WHERE plugin_code = 'order-pintuan';
