-- 银联企业订单分账：插件位菜单（pid=0 display_pos=plugin）
-- 备份：hou_permission_menu_bak_ums_20261103

CREATE TABLE IF NOT EXISTS hou_permission_menu_bak_ums_20261103 LIKE hou_permission_menu;
INSERT IGNORE INTO hou_permission_menu_bak_ums_20261103 SELECT * FROM hou_permission_menu WHERE plugin_code='ums-corp-order' OR paths LIKE '/plugin/ums-corp-order%';

-- 根
INSERT INTO hou_permission_menu
(pid,type,name,icon,sort,perms,paths,component,selected,params,plugin_code,is_cache,is_show,is_disable,display_pos,cate_id,create_time,update_time)
SELECT 0,'M','银联企业订单分账','CreditCard',2800,'ums-corp-order:root','/plugin/ums-corp-order','','','plugin:ums-corp-order','ums-corp-order',0,1,0,'plugin',
       (SELECT id FROM hou_addon_cate WHERE name LIKE '%办公%' OR name LIKE '%财务%' ORDER BY id LIMIT 1),
       UNIX_TIMESTAMP(), UNIX_TIMESTAMP()
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM hou_permission_menu WHERE perms='ums-corp-order:root' LIMIT 1);

SET @ums_root := (SELECT id FROM hou_permission_menu WHERE perms='ums-corp-order:root' ORDER BY id DESC LIMIT 1);
SET @cate := (SELECT IFNULL(cate_id,3) FROM hou_permission_menu WHERE id=@ums_root);

INSERT INTO hou_permission_menu
(pid,type,name,icon,sort,perms,paths,component,selected,params,plugin_code,is_cache,is_show,is_disable,display_pos,cate_id,create_time,update_time)
SELECT @ums_root,'C','渠道配置','Setting',100,'ums-corp-order:config','/plugin/ums-corp-order/config','','','plugin:ums-corp-order','ums-corp-order',0,1,0,'left',@cate,UNIX_TIMESTAMP(),UNIX_TIMESTAMP()
FROM DUAL WHERE @ums_root IS NOT NULL AND NOT EXISTS (SELECT 1 FROM hou_permission_menu WHERE perms='ums-corp-order:config' LIMIT 1);

INSERT INTO hou_permission_menu
(pid,type,name,icon,sort,perms,paths,component,selected,params,plugin_code,is_cache,is_show,is_disable,display_pos,cate_id,create_time,update_time)
SELECT @ums_root,'C','企业订单','List',90,'ums-corp-order:order','/plugin/ums-corp-order/orders','','','plugin:ums-corp-order','ums-corp-order',0,1,0,'left',@cate,UNIX_TIMESTAMP(),UNIX_TIMESTAMP()
FROM DUAL WHERE @ums_root IS NOT NULL AND NOT EXISTS (SELECT 1 FROM hou_permission_menu WHERE perms='ums-corp-order:order' LIMIT 1);

INSERT INTO hou_permission_menu
(pid,type,name,icon,sort,perms,paths,component,selected,params,plugin_code,is_cache,is_show,is_disable,display_pos,cate_id,create_time,update_time)
SELECT @ums_root,'C','划付分账','Share',80,'ums-corp-order:split','/plugin/ums-corp-order/split','','','plugin:ums-corp-order','ums-corp-order',0,1,0,'left',@cate,UNIX_TIMESTAMP(),UNIX_TIMESTAMP()
FROM DUAL WHERE @ums_root IS NOT NULL AND NOT EXISTS (SELECT 1 FROM hou_permission_menu WHERE perms='ums-corp-order:split' LIMIT 1);

INSERT INTO hou_permission_menu
(pid,type,name,icon,sort,perms,paths,component,selected,params,plugin_code,is_cache,is_show,is_disable,display_pos,cate_id,create_time,update_time)
SELECT @ums_root,'C','操作记录','Document',70,'ums-corp-order:ops','/plugin/ums-corp-order/ops','','','plugin:ums-corp-order','ums-corp-order',0,1,0,'left',@cate,UNIX_TIMESTAMP(),UNIX_TIMESTAMP()
FROM DUAL WHERE @ums_root IS NOT NULL AND NOT EXISTS (SELECT 1 FROM hou_permission_menu WHERE perms='ums-corp-order:ops' LIMIT 1);

INSERT INTO hou_permission_menu
(pid,type,name,icon,sort,perms,paths,component,selected,params,plugin_code,is_cache,is_show,is_disable,display_pos,cate_id,create_time,update_time)
SELECT @ums_root,'C','商户余额','Wallet',60,'ums-corp-order:balance','/plugin/ums-corp-order/balance','','','plugin:ums-corp-order','ums-corp-order',0,1,0,'left',@cate,UNIX_TIMESTAMP(),UNIX_TIMESTAMP()
FROM DUAL WHERE @ums_root IS NOT NULL AND NOT EXISTS (SELECT 1 FROM hou_permission_menu WHERE perms='ums-corp-order:balance' LIMIT 1);

-- 授权超级管理员 role_id=1
INSERT IGNORE INTO hou_permission_role_menu (role_id, menu_id)
SELECT 1, id FROM hou_permission_menu WHERE plugin_code='ums-corp-order' OR perms LIKE 'ums-corp-order:%';

-- 插件目录登记（右上角「插件应用」面板依赖 installed+enabled）
INSERT INTO hou_addon_plugin
  (cate_id, service_code, plugin_code, name, category, version, description,
   terminals_json, dependencies_json, entry_path, config_schema, status, sort, deleted)
VALUES
  (IFNULL(@cate, 3), 'payment', 'ums-corp-order', '银联企业订单分账', '财务 / 办公', '1.0.0',
   '银联商务企业订单支付（生成/查询/关闭/退款/划付/分账），宿主挂载 payment',
   '["admin"]', '[]', 'index.mjs', '{}', 1, 2800, 0)
ON DUPLICATE KEY UPDATE
  name = VALUES(name),
  category = VALUES(category),
  version = VALUES(version),
  description = VALUES(description),
  service_code = VALUES(service_code),
  entry_path = VALUES(entry_path),
  status = 1,
  deleted = 0,
  cate_id = VALUES(cate_id);

-- jar_path 留空：宿主已内嵌 UMS API，不走 PF4J 再装一份
INSERT INTO hou_addon_plugin_install
  (plugin_code, installed, enabled, config_json, version, install_time, update_time, deleted)
SELECT 'ums-corp-order', 1, 1, '{}', '1.0.0', NOW(), NOW(), 0
FROM DUAL
WHERE NOT EXISTS (
  SELECT 1 FROM hou_addon_plugin_install WHERE plugin_code = 'ums-corp-order' LIMIT 1
);

UPDATE hou_addon_plugin_install
SET installed = 1, enabled = 1, version = '1.0.0', deleted = 0, update_time = NOW()
WHERE plugin_code = 'ums-corp-order';
