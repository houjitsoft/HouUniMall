-- 2026-09-28：将历史核心「支付中心」原位迁为 unipay 插件入口。
-- 分类 ID 必须从 hou_addon_cate 读取，禁止写死。
-- 兼容旧名「办公与财务」与合并后的「财务 / 办公」（159 合并）。
-- 保留菜单 ID 与 hou_permission_role_menu 授权；不手工登记 hou_addon_plugin。
SET NAMES utf8mb4;

CREATE TABLE IF NOT EXISTS hou_permission_menu_bak_unipay_20260928 AS
 SELECT * FROM hou_permission_menu;
CREATE TABLE IF NOT EXISTS hou_permission_role_menu_bak_unipay_20260928 AS
 SELECT * FROM hou_permission_role_menu;

CREATE TABLE IF NOT EXISTS hou_plugin_navigation_migration (
  migration_key VARCHAR(100) NOT NULL PRIMARY KEY,
  applied_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='导航数据迁移完成记录，重跑不覆盖后续人工配置';

SET @unipay_apply = NOT EXISTS (
  SELECT 1 FROM hou_plugin_navigation_migration
  WHERE migration_key='20260928-payment-to-unipay-plugin'
);

DROP PROCEDURE IF EXISTS assert_unipay_navigation_20260928;
DELIMITER $$
CREATE PROCEDURE assert_unipay_navigation_20260928()
BEGIN
  DECLARE category_count INT DEFAULT 0;
  DECLARE category_id INT DEFAULT 0;
  DECLARE payment_root_count INT DEFAULT 0;

  IF @unipay_apply THEN
    SELECT COUNT(*),COALESCE(MIN(id),0) INTO category_count,category_id
      FROM hou_addon_cate
     WHERE name COLLATE utf8mb4_general_ci IN ('办公与财务','财务 / 办公');
    IF category_count < 1 THEN
      SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT='unipay migration requires hou_addon_cate named 办公与财务 or 财务 / 办公';
    END IF;
    -- 若新旧名并存，优先取「财务 / 办公」
    SELECT id INTO category_id FROM hou_addon_cate
     WHERE name COLLATE utf8mb4_general_ci IN ('财务 / 办公','办公与财务')
     ORDER BY CASE name WHEN '财务 / 办公' THEN 0 ELSE 1 END, sort, id
     LIMIT 1;

    SELECT COUNT(*) INTO payment_root_count
      FROM hou_permission_menu WHERE id=15;
    IF payment_root_count <> 1 THEN
      SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT='unipay migration requires legacy payment menu id=15';
    END IF;

    SET @unipay_cate_id = category_id;
  END IF;
END$$
DELIMITER ;
CALL assert_unipay_navigation_20260928();
DROP PROCEDURE assert_unipay_navigation_20260928;

DROP TEMPORARY TABLE IF EXISTS unipay_menu_tree_20260928;
CREATE TEMPORARY TABLE unipay_menu_tree_20260928 AS
WITH RECURSIVE menu_tree AS (
  SELECT id FROM hou_permission_menu WHERE @unipay_apply AND id=15
  UNION ALL
  SELECT child.id
    FROM hou_permission_menu child
    JOIN menu_tree parent ON child.pid=parent.id
)
SELECT id FROM menu_tree;
ALTER TABLE unipay_menu_tree_20260928 ADD PRIMARY KEY (id);

-- 一批历史种子把支付菜单错误挂在低代码根 id=21 下。按支付权限+旧路由收敛到
-- 同一个 unipay 命名空间；这些行不提升为插件根，也不改角色授权或菜单 ID。
DROP TEMPORARY TABLE IF EXISTS unipay_legacy_payment_rows_20260928;
CREATE TEMPORARY TABLE unipay_legacy_payment_rows_20260928 AS
SELECT menu.id FROM hou_permission_menu menu
LEFT JOIN unipay_menu_tree_20260928 tree ON tree.id=menu.id
 WHERE @unipay_apply
   AND tree.id IS NULL
   AND menu.perms LIKE 'shell:payment:%'
   AND (menu.paths='/payment' OR menu.paths LIKE '/payment/%');
ALTER TABLE unipay_legacy_payment_rows_20260928 ADD PRIMARY KEY (id);

START TRANSACTION;

-- 先统一改写整棵支付菜单树的页面路由；API /web/payment/** 不在本表中，不受影响。
UPDATE hou_permission_menu menu
JOIN unipay_menu_tree_20260928 tree ON tree.id=menu.id
SET menu.paths=CASE
      WHEN menu.paths='/payment' THEN '/plugin/unipay'
      WHEN menu.paths LIKE '/payment/%' THEN CONCAT('/plugin/unipay',SUBSTRING(menu.paths,9))
      ELSE menu.paths END,
    menu.selected=CASE
      WHEN menu.selected='/payment' THEN '/plugin/unipay'
      WHEN menu.selected LIKE '/payment/%' THEN CONCAT('/plugin/unipay',SUBSTRING(menu.selected,9))
      ELSE menu.selected END,
    menu.update_time=UNIX_TIMESTAMP()
WHERE @unipay_apply;

UPDATE hou_permission_menu menu
JOIN unipay_legacy_payment_rows_20260928 legacy ON legacy.id=menu.id
SET menu.paths=CONCAT('/plugin/unipay',SUBSTRING(menu.paths,9)),
    menu.selected=CASE
      WHEN menu.selected='/payment' THEN '/plugin/unipay'
      WHEN menu.selected LIKE '/payment/%' THEN CONCAT('/plugin/unipay',SUBSTRING(menu.selected,9))
      ELSE menu.selected END,
    menu.display_pos='hidden',menu.cate_id=0,menu.update_time=UNIX_TIMESTAMP()
WHERE @unipay_apply;

-- 原位复用 id=15，角色授权无需迁移；分类仅使用上方实时解析出的 ID。
UPDATE hou_permission_menu
SET pid=0,type='M',name='支付中心',icon='CreditCard',sort=3030,
    perms='shell:payment',paths='/plugin/unipay',component='',selected='',
    params='plugin:unipay',plugin_code='unipay',display_pos='plugin',
    cate_id=@unipay_cate_id,update_time=UNIX_TIMESTAMP()
WHERE @unipay_apply AND id=15;

-- 分类只属于插件根；内部菜单继续保持原层级和原权限 ID。
UPDATE hou_permission_menu menu
JOIN unipay_menu_tree_20260928 tree ON tree.id=menu.id
SET menu.display_pos='left',menu.cate_id=0,menu.update_time=UNIX_TIMESTAMP()
WHERE @unipay_apply AND menu.id<>15;

INSERT INTO hou_plugin_navigation_migration (migration_key)
 SELECT '20260928-payment-to-unipay-plugin' WHERE @unipay_apply;

COMMIT;

SELECT menu.id,menu.pid,menu.name,menu.paths,menu.plugin_code,menu.display_pos,
       menu.cate_id,cate.name AS category_name
  FROM hou_permission_menu menu
  LEFT JOIN hou_addon_cate cate ON cate.id=menu.cate_id
 WHERE menu.id=15;
SELECT 'legacy_payment_route' AS issue,menu.id,menu.paths
  FROM hou_permission_menu menu
 WHERE menu.perms LIKE 'shell:payment:%'
   AND (menu.paths='/payment' OR menu.paths LIKE '/payment/%');

DROP TEMPORARY TABLE unipay_menu_tree_20260928;
DROP TEMPORARY TABLE unipay_legacy_payment_rows_20260928;
