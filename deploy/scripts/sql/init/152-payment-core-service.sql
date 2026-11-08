-- 2026-11-02：支付中心从 unipay 插件回归 payment 独立服务。
-- 菜单留在左侧核心树（id=15），路径 /plugin/unipay/** 改回 /payment/**，
-- 摘掉 plugin_code / params，目录行与安装记录软删，避免对账器把它当成已装插件。
-- 角色授权不删。幂等：重复执行只改仍挂在插件地址上的行。
-- 开源形态由随后的 145 按平台库守卫回收本菜单；本脚本本身不区分站点。
SET NAMES utf8mb4;

CREATE TABLE IF NOT EXISTS hou_permission_menu_bak_payment_core_20261102 AS
 SELECT * FROM hou_permission_menu;
CREATE TABLE IF NOT EXISTS hou_permission_role_menu_bak_payment_core_20261102 AS
 SELECT * FROM hou_permission_role_menu;
CREATE TABLE IF NOT EXISTS hou_addon_plugin_bak_payment_core_20261102 AS
 SELECT * FROM hou_addon_plugin WHERE plugin_code = 'unipay';
CREATE TABLE IF NOT EXISTS hou_addon_plugin_install_bak_payment_core_20261102 AS
 SELECT * FROM hou_addon_plugin_install WHERE plugin_code = 'unipay';

-- '/plugin/unipay' 长度为 14，子路径从第 15 个字符（前导 /）拼到 /payment 后。
UPDATE hou_permission_menu
SET paths = CASE
      WHEN paths = '/plugin/unipay' THEN '/payment'
      WHEN paths LIKE '/plugin/unipay/%' THEN CONCAT('/payment', SUBSTRING(paths, 15))
      ELSE paths END,
    selected = CASE
      WHEN selected = '/plugin/unipay' THEN '/payment'
      WHEN selected LIKE '/plugin/unipay/%' THEN CONCAT('/payment', SUBSTRING(selected, 15))
      ELSE selected END,
    plugin_code = IF(plugin_code = 'unipay', '', plugin_code),
    params = IF(params = 'plugin:unipay', '', params),
    display_pos = IF(display_pos = 'plugin', 'left', display_pos),
    cate_id = IF(pid = 0 AND (perms = 'shell:payment' OR plugin_code = 'unipay' OR paths IN ('/plugin/unipay', '/payment')), 0, cate_id),
    update_time = UNIX_TIMESTAMP()
WHERE paths = '/plugin/unipay' OR paths LIKE '/plugin/unipay/%'
   OR selected = '/plugin/unipay' OR selected LIKE '/plugin/unipay/%'
   OR plugin_code = 'unipay'
   OR params = 'plugin:unipay'
   OR perms = 'shell:payment' OR perms LIKE 'shell:payment:%';

-- 根菜单固定为左侧一级，排在数据中台(8900)与财务管理(7500)之间。
UPDATE hou_permission_menu
SET pid = 0,
    type = 'M',
    name = '支付中心',
    icon = 'CreditCard',
    sort = 8200,
    perms = 'shell:payment',
    paths = '/payment',
    component = '',
    selected = '',
    params = '',
    plugin_code = '',
    display_pos = 'left',
    cate_id = 0,
    is_show = 1,
    is_disable = 0,
    update_time = UNIX_TIMESTAMP()
WHERE id = 15;

UPDATE hou_addon_plugin
   SET status = 0, deleted = 1, update_time = NOW()
 WHERE plugin_code = 'unipay';

UPDATE hou_addon_plugin_install
   SET installed = 0, enabled = 0, deleted = 1, update_time = NOW()
 WHERE plugin_code = 'unipay';

SELECT id, pid, name, paths, plugin_code, display_pos, sort
  FROM hou_permission_menu
 WHERE id = 15 OR perms = 'shell:payment' OR perms LIKE 'shell:payment:%'
 ORDER BY pid, sort DESC, id;
