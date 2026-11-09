-- 满赠插件登记（幂等，可重复执行）
INSERT INTO hou_addon_plugin (cate_id, service_code, plugin_code, name, category, version, description, terminals_json, dependencies_json, entry_path, config_schema, status, sort )
SELECT 4, 'mall', 'order-gift', '满赠', '营销与运营', '1.0.3',
       '满额赠品与买赠，支持固定赠品、多选一赠品池与赠品库存控制',
       '["admin"]', '[]', 'index.mjs', '{}', 1, 0
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM hou_addon_plugin WHERE plugin_code = 'order-gift' AND deleted = 0);
-- 上面那条只在首装时插入；已存在的行必须显式 UPDATE，否则升级后版本号永远停在首装值。
UPDATE hou_addon_plugin SET version = '1.0.3', status = 1, deleted = 0
WHERE plugin_code = 'order-gift' AND deleted = 0;


INSERT INTO hou_addon_plugin_install (plugin_code, installed, enabled, version, jar_path, checksum, install_time, update_time)
VALUES ('order-gift', 1, 1, '1.0.3', '/app/plugins/order-gift/1.0.3/order-gift-plugin-1.0.3.jar',
        NULL, NOW(), NOW())
ON DUPLICATE KEY UPDATE installed = 1, enabled = 1, version = '1.0.3',
    jar_path = VALUES(jar_path), checksum = VALUES(checksum), update_time = NOW(), deleted = 0;

INSERT INTO hou_permission_menu (pid, type, name, icon, sort, perms, paths, component, selected, params,
                                plugin_code, is_cache, is_show, is_disable, create_time, update_time, display_pos, cate_id)
SELECT 0, 'M', '满赠', 'Present', 3330, 'order-gift:root', '/plugin/order-gift', '', '', '',
       'order-gift', 0, 1, 0, UNIX_TIMESTAMP(), UNIX_TIMESTAMP(), 'plugin', 4
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM hou_permission_menu WHERE plugin_code = 'order-gift');

INSERT INTO hou_market_plugin (plugin_code, name, author_id, category, cate_id, description, short_desc,
                               latest_version, license_type, price_cents, trial_days, download_count, install_count,
                               rating_avg, rating_count, status, min_framework_version, tags_json, terminals_json,
                               dependencies_json, deleted, visibility)
VALUES ('order-gift', '满赠', 1, '营销与运营', 4,
        '满额赠品与买赠，支持固定赠品、多选一赠品池与赠品库存控制', '满额送赠品',
        '1.0.3', 'FREE', 0, 0, 0, 0, 0.00, 0, 2, '1.0.0', '["official"]', '["admin"]', '[]', 0, 1)
ON DUPLICATE KEY UPDATE name = VALUES(name), status = 2, deleted = 0, visibility = 1,
    latest_version = VALUES(latest_version), update_time = NOW();

INSERT INTO hou_permission_role_menu (role_id, menu_id)
SELECT 1, m.id FROM hou_permission_menu m
WHERE m.plugin_code = 'order-gift'
  AND NOT EXISTS (SELECT 1 FROM hou_permission_role_menu rm WHERE rm.role_id = 1 AND rm.menu_id = m.id);

-- ---------------------------------------------------------------------------
-- 市场版本行（1.0.3）
--
-- 只登记商品条目、不登记版本行时，市场详情恒显示「共 0 个已审核版本」，
-- 安装按钮无处可点：后端只认 status=1 的版本行（PluginMarketService.resolveVersion）。
-- 开源包没有平台制品仓，这里登记的是「随安装包落盘的本地制品」，
-- 路径与 plugin-seed 投放口径一致：{plugins}/{code}/{version}/{code}-plugin-{version}.jar
-- ---------------------------------------------------------------------------
INSERT INTO hou_market_plugin_version (plugin_code, version, status, storage_type, jar_url,
                                       changelog, publish_time, upload_status, submit_time, create_time)
VALUES ('order-gift', '1.0.3', 1, 'LOCAL',
        'file:///app/plugins/order-gift/1.0.3/order-gift-plugin-1.0.3.jar',
        '开源包内置版本，随安装包发布', NOW(), 1, NOW(), NOW())
ON DUPLICATE KEY UPDATE status = 1, storage_type = VALUES(storage_type),
    jar_url = VALUES(jar_url), changelog = VALUES(changelog),
    publish_time = VALUES(publish_time), upload_status = 1,
    submit_time = VALUES(submit_time), offline_time = NULL, offline_reason = NULL;

-- 旧版本置为已下架：制品目录已被新版本替换，留着可安装的旧行只会点出「安装包地址不可用」。
UPDATE hou_market_plugin_version SET status = 5,
    offline_time = NOW(), offline_reason = '已发布新版本，旧制品不再随包提供'
WHERE plugin_code = 'order-gift' AND version <> '1.0.3' AND status = 1;
