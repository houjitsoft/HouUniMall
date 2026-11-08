-- 统一登录插件登记（幂等，可重复执行）
INSERT INTO hou_addon_plugin (cate_id, service_code, plugin_code, name, category, version, description, terminals_json, dependencies_json, entry_path, config_schema, status, sort )
SELECT 9, 'auth', 'unilogin', '统一登录', '运维/工具', '1.0.2',
       '统一第三方登录插件。以渠道注册表驱动，当前内置微信开放平台扫码登录，后续可扩展企业微信、钉钉、QQ、飞书等渠道；登录页自动注入各渠道入口。',
       '["admin"]', '[]', 'index.mjs', '{}', 1, 0
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM hou_addon_plugin WHERE plugin_code = 'unilogin' AND deleted = 0);

INSERT INTO hou_addon_plugin_install (plugin_code, installed, enabled, version, jar_path, checksum, install_time, update_time)
-- 后端在该插件的宿主服务里（auth），没有独立插件 jar：jar_path/checksum 记 NULL。
-- 原先这里写的是 /app/plugins/unilogin/1.0.2/unilogin-plugin-1.0.2.jar，
-- 但包里从来没有产出过这个文件，留着一个不存在的路径只会让「已安装」这条记录说谎。
VALUES ('unilogin', 1, 1, '1.0.2', NULL,
        NULL, NOW(), NOW())
ON DUPLICATE KEY UPDATE installed = 1, enabled = 1, version = '1.0.2',
    jar_path = NULL, checksum = NULL, update_time = NOW(), deleted = 0;

INSERT INTO hou_permission_menu (pid, type, name, icon, sort, perms, paths, component, selected, params,
                                plugin_code, is_cache, is_show, is_disable, create_time, update_time, display_pos, cate_id)
SELECT 0, 'M', '统一登录', 'Connection', 9080, 'shell:unilogin:settings', '/plugin/unilogin/settings', '', '', '',
       'unilogin', 0, 1, 0, UNIX_TIMESTAMP(), UNIX_TIMESTAMP(), 'plugin', 9
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM hou_permission_menu WHERE plugin_code = 'unilogin');

INSERT INTO hou_market_plugin (plugin_code, name, author_id, category, cate_id, description, short_desc,
                               latest_version, license_type, price_cents, trial_days, download_count, install_count,
                               rating_avg, rating_count, status, min_framework_version, tags_json, terminals_json,
                               dependencies_json, deleted, visibility)
VALUES ('unilogin', '统一登录', 1, '运维/工具', 9,
        '统一第三方登录插件。以渠道注册表驱动，当前内置微信开放平台扫码登录，后续可扩展企业微信、钉钉、QQ、飞书等渠道；登录页自动注入各渠道入口。', '统一第三方登录',
        '1.0.2', 'FREE', 0, 0, 0, 0, 0.00, 0, 2, '1.0.0', '["official"]', '["admin"]', '[]', 0, 1)
ON DUPLICATE KEY UPDATE name = VALUES(name), status = 2, deleted = 0, visibility = 1,
    latest_version = VALUES(latest_version), update_time = NOW();

INSERT INTO hou_permission_role_menu (role_id, menu_id)
SELECT 1, m.id FROM hou_permission_menu m
WHERE m.plugin_code = 'unilogin'
  AND NOT EXISTS (SELECT 1 FROM hou_permission_role_menu rm WHERE rm.role_id = 1 AND rm.menu_id = m.id);
