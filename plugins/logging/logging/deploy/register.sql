-- 日志管理插件登记（幂等，可重复执行）
INSERT INTO hou_addon_plugin (cate_id, service_code, plugin_code, name, category, version, description, terminals_json, dependencies_json, entry_path, config_schema, status, sort )
SELECT 9, 'logging', 'logging', '日志管理', '运维/工具', '1.0.1',
       'Operation, access, business and ELK log management',
       '["admin"]', '[]', 'index.mjs', '{}', 1, 0
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM hou_addon_plugin WHERE plugin_code = 'logging' AND deleted = 0);
-- 上面那条只在首装时插入；已存在的行必须显式 UPDATE，否则升级后版本号永远停在首装值。
UPDATE hou_addon_plugin SET version = '1.0.1', status = 1, deleted = 0
WHERE plugin_code = 'logging' AND deleted = 0;

INSERT INTO hou_addon_plugin_install (plugin_code, installed, enabled, version, jar_path, checksum, install_time, update_time)
-- 后端在该插件的宿主服务里（logging），没有独立插件 jar：jar_path/checksum 记 NULL。
-- 原先这里写的是 /app/plugins/logging/1.0.0/logging-plugin-1.0.0.jar，
-- 但包里从来没有产出过这个文件，留着一个不存在的路径只会让「已安装」这条记录说谎。
VALUES ('logging', 1, 1, '1.0.1', NULL,
        NULL, NOW(), NOW())
ON DUPLICATE KEY UPDATE installed = 1, enabled = 1, version = '1.0.1',
    jar_path = NULL, checksum = NULL, update_time = NOW(), deleted = 0;

INSERT INTO hou_permission_menu (pid, type, name, icon, sort, perms, paths, component, selected, params,
                                plugin_code, is_cache, is_show, is_disable, create_time, update_time, display_pos, cate_id)
SELECT 0, 'M', '日志管理', 'Document', 3070, 'shell:logging:apps', '/plugin/logging', '', '', '',
       'logging', 0, 1, 0, UNIX_TIMESTAMP(), UNIX_TIMESTAMP(), 'plugin', 9
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM hou_permission_menu WHERE plugin_code = 'logging');

INSERT INTO hou_market_plugin (plugin_code, name, author_id, category, cate_id, description, short_desc,
                               latest_version, license_type, price_cents, trial_days, download_count, install_count,
                               rating_avg, rating_count, status, min_framework_version, tags_json, terminals_json,
                               dependencies_json, deleted, visibility)
VALUES ('logging', '日志管理', 1, '运维/工具', 9,
        'Operation, access, business and ELK log management', '系统日志管理',
        '1.0.1', 'FREE', 0, 0, 0, 0, 0.00, 0, 2, '1.0.0', '["official"]', '["admin"]', '[]', 0, 1)
ON DUPLICATE KEY UPDATE name = VALUES(name), status = 2, deleted = 0, visibility = 1,
    latest_version = VALUES(latest_version), update_time = NOW();

INSERT INTO hou_permission_role_menu (role_id, menu_id)
SELECT 1, m.id FROM hou_permission_menu m
WHERE m.plugin_code = 'logging'
  AND NOT EXISTS (SELECT 1 FROM hou_permission_role_menu rm WHERE rm.role_id = 1 AND rm.menu_id = m.id);

-- ---------------------------------------------------------------------------
-- 市场版本行（1.0.1）
--
-- 只登记商品条目、不登记版本行时，市场详情恒显示「共 0 个已审核版本」，
-- 安装按钮无处可点：后端只认 status=1 的版本行（PluginMarketService.resolveVersion）。
-- 该插件没有独立 jar（后端编在宿主服务里），jar_url 记 NULL：
-- 市场详情可正常列出本版本，但「在线安装」对它是无意义的，装的是宿主。
-- ---------------------------------------------------------------------------
INSERT INTO hou_market_plugin_version (plugin_code, version, status, storage_type, jar_url,
                                       changelog, publish_time, upload_status, submit_time, create_time)
VALUES ('logging', '1.0.1', 1, 'LOCAL',
        NULL,
        '开源包内置版本，随安装包发布', NOW(), 1, NOW(), NOW())
ON DUPLICATE KEY UPDATE status = 1, storage_type = VALUES(storage_type),
    jar_url = VALUES(jar_url), changelog = VALUES(changelog),
    publish_time = VALUES(publish_time), upload_status = 1,
    submit_time = VALUES(submit_time), offline_time = NULL, offline_reason = NULL;

-- 旧版本置为已下架：制品目录已被新版本替换，留着可安装的旧行只会点出「安装包地址不可用」。
UPDATE hou_market_plugin_version SET status = 5,
    offline_time = NOW(), offline_reason = '已发布新版本，旧制品不再随包提供'
WHERE plugin_code = 'logging' AND version <> '1.0.1' AND status = 1;
