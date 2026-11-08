-- 商品折扣插件登记（幂等，可重复执行）
INSERT INTO hou_addon_plugin (cate_id, service_code, plugin_code, name, category, version, description, terminals_json, dependencies_json, entry_path, config_schema, status, sort )
SELECT 4, 'mall', 'goods-discount', '商品折扣', '营销/运营', '1.0.0',
       '商品折扣（activity 拆分，归属 goods 宿主）',
       '["admin"]', '[]', 'index.mjs', '{}', 1, 0
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM hou_addon_plugin WHERE plugin_code = 'goods-discount' AND deleted = 0);

INSERT INTO hou_addon_plugin_install (plugin_code, installed, enabled, version, jar_path, checksum, install_time, update_time)
VALUES ('goods-discount', 1, 1, '1.0.0', '/app/plugins/goods-discount/1.0.0/goods-discount-plugin-1.0.0.jar',
        '74b8be1af8d8c461fbd9c10b3f30c43cbfc81deb7abaf08fb3d68e1f683a78ae', NOW(), NOW())
ON DUPLICATE KEY UPDATE installed = 1, enabled = 1, version = '1.0.0',
    jar_path = VALUES(jar_path), checksum = VALUES(checksum), update_time = NOW(), deleted = 0;

INSERT INTO hou_permission_menu (pid, type, name, icon, sort, perms, paths, component, selected, params,
                                plugin_code, is_cache, is_show, is_disable, create_time, update_time, display_pos, cate_id)
SELECT 0, 'M', '商品折扣', 'PriceTag', 3270, 'goods-discount:root', '/plugin/goods-discount', '', '', '',
       'goods-discount', 0, 1, 0, UNIX_TIMESTAMP(), UNIX_TIMESTAMP(), 'plugin', 4
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM hou_permission_menu WHERE plugin_code = 'goods-discount');

INSERT INTO hou_market_plugin (plugin_code, name, author_id, category, cate_id, description, short_desc,
                               latest_version, license_type, price_cents, trial_days, download_count, install_count,
                               rating_avg, rating_count, status, min_framework_version, tags_json, terminals_json,
                               dependencies_json, deleted, visibility)
VALUES ('goods-discount', '商品折扣', 1, '营销/运营', 4,
        '商品折扣（activity 拆分，归属 goods 宿主）', '商品折扣',
        '1.0.0', 'FREE', 0, 0, 0, 0, 0.00, 0, 2, '1.0.0', '["official"]', '["admin"]', '[]', 0, 1)
ON DUPLICATE KEY UPDATE name = VALUES(name), status = 2, deleted = 0, visibility = 1,
    latest_version = VALUES(latest_version), update_time = NOW();

INSERT INTO hou_permission_role_menu (role_id, menu_id)
SELECT 1, m.id FROM hou_permission_menu m
WHERE m.plugin_code = 'goods-discount'
  AND NOT EXISTS (SELECT 1 FROM hou_permission_role_menu rm WHERE rm.role_id = 1 AND rm.menu_id = m.id);
