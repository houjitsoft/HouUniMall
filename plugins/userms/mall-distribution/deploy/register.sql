-- 分销推广插件登记（幂等，可重复执行）
INSERT INTO hou_addon_plugin (cate_id, service_code, plugin_code, name, category, version, description, terminals_json, dependencies_json, entry_path, config_schema, status, sort )
SELECT 1, 'userms', 'mall-distribution', '分销推广', '营销与客户', '1.0.2',
       'Distributor management, levels, goods, orders, commission rules and records',
       '["admin"]', '[]', 'index.mjs', '{}', 1, 0
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM hou_addon_plugin WHERE plugin_code = 'mall-distribution' AND deleted = 0);

INSERT INTO hou_addon_plugin_install (plugin_code, installed, enabled, version, jar_path, checksum, install_time, update_time)
-- 后端在该插件的宿主服务里（mall），没有独立插件 jar：jar_path/checksum 记 NULL。
-- 原先这里写的是 /app/plugins/mall-distribution/1.0.2/mall-distribution-plugin-1.0.2.jar，
-- 但包里从来没有产出过这个文件，留着一个不存在的路径只会让「已安装」这条记录说谎。
VALUES ('mall-distribution', 1, 1, '1.0.2', NULL,
        NULL, NOW(), NOW())
ON DUPLICATE KEY UPDATE installed = 1, enabled = 1, version = '1.0.2',
    jar_path = NULL, checksum = NULL, update_time = NOW(), deleted = 0;

INSERT INTO hou_permission_menu (pid, type, name, icon, sort, perms, paths, component, selected, params,
                                plugin_code, is_cache, is_show, is_disable, create_time, update_time, display_pos, cate_id)
SELECT 0, 'M', '分销推广', 'Share', 3050, 'shell:mall-distribution:root', '/plugin/mall-distribution', '', '', '',
       'mall-distribution', 0, 1, 0, UNIX_TIMESTAMP(), UNIX_TIMESTAMP(), 'plugin', 1
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM hou_permission_menu WHERE plugin_code = 'mall-distribution');

INSERT INTO hou_market_plugin (plugin_code, name, author_id, category, cate_id, description, short_desc,
                               latest_version, license_type, price_cents, trial_days, download_count, install_count,
                               rating_avg, rating_count, status, min_framework_version, tags_json, terminals_json,
                               dependencies_json, deleted, visibility)
VALUES ('mall-distribution', '分销推广', 1, '营销与客户', 1,
        'Distributor management, levels, goods, orders, commission rules and records', '分销推广返佣',
        '1.0.2', 'FREE', 0, 0, 0, 0, 0.00, 0, 2, '1.0.0', '["official"]', '["admin"]', '[]', 0, 1)
ON DUPLICATE KEY UPDATE name = VALUES(name), status = 2, deleted = 0, visibility = 1,
    latest_version = VALUES(latest_version), update_time = NOW();

INSERT INTO hou_permission_role_menu (role_id, menu_id)
SELECT 1, m.id FROM hou_permission_menu m
WHERE m.plugin_code = 'mall-distribution'
  AND NOT EXISTS (SELECT 1 FROM hou_permission_role_menu rm WHERE rm.role_id = 1 AND rm.menu_id = m.id);
