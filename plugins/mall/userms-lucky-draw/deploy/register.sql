-- 抽奖转盘插件登记（幂等，可重复执行）
INSERT INTO hou_addon_plugin (cate_id, service_code, plugin_code, name, category, version, description, terminals_json, dependencies_json, entry_path, config_schema, status, sort )
SELECT 4, 'mall', 'userms-lucky-draw', '抽奖转盘', '营销/运营', '1.0.0',
       '抽奖转盘（activity 拆分，归属 userms 宿主）',
       '["admin"]', '[]', 'index.mjs', '{}', 1, 0
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM hou_addon_plugin WHERE plugin_code = 'userms-lucky-draw' AND deleted = 0);

INSERT INTO hou_addon_plugin_install (plugin_code, installed, enabled, version, jar_path, checksum, install_time, update_time)
VALUES ('userms-lucky-draw', 1, 1, '1.0.0', '/app/plugins/userms-lucky-draw/1.0.0/userms-lucky-draw-plugin-1.0.0.jar',
        '7fcb2467d1a5be9c7b1d8e3e16d643a99f374198fc7756f85939851a332648d0', NOW(), NOW())
ON DUPLICATE KEY UPDATE installed = 1, enabled = 1, version = '1.0.0',
    jar_path = VALUES(jar_path), checksum = VALUES(checksum), update_time = NOW(), deleted = 0;

INSERT INTO hou_permission_menu (pid, type, name, icon, sort, perms, paths, component, selected, params,
                                plugin_code, is_cache, is_show, is_disable, create_time, update_time, display_pos, cate_id)
SELECT 0, 'M', '抽奖转盘', 'Trophy', 3290, 'userms-lucky-draw:root', '/plugin/userms-lucky-draw', '', '', '',
       'userms-lucky-draw', 0, 1, 0, UNIX_TIMESTAMP(), UNIX_TIMESTAMP(), 'plugin', 4
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM hou_permission_menu WHERE plugin_code = 'userms-lucky-draw');

INSERT INTO hou_market_plugin (plugin_code, name, author_id, category, cate_id, description, short_desc,
                               latest_version, license_type, price_cents, trial_days, download_count, install_count,
                               rating_avg, rating_count, status, min_framework_version, tags_json, terminals_json,
                               dependencies_json, deleted, visibility)
VALUES ('userms-lucky-draw', '抽奖转盘', 1, '营销/运营', 4,
        '抽奖转盘（activity 拆分，归属 userms 宿主）', '抽奖转盘活动',
        '1.0.0', 'FREE', 0, 0, 0, 0, 0.00, 0, 2, '1.0.0', '["official"]', '["admin"]', '[]', 0, 1)
ON DUPLICATE KEY UPDATE name = VALUES(name), status = 2, deleted = 0, visibility = 1,
    latest_version = VALUES(latest_version), update_time = NOW();

INSERT INTO hou_permission_role_menu (role_id, menu_id)
SELECT 1, m.id FROM hou_permission_menu m
WHERE m.plugin_code = 'userms-lucky-draw'
  AND NOT EXISTS (SELECT 1 FROM hou_permission_role_menu rm WHERE rm.role_id = 1 AND rm.menu_id = m.id);
