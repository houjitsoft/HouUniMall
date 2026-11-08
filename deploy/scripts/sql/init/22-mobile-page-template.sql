-- =====================================================================
-- UniMall 统一初始化 ㉒ 增量迁移：移动端页面模板
-- 范围：移动端装修新增「模板选择」——后台选模板、按页面生效、前台 uniapp 按模板渲染
-- 幂等：可重复执行（ADD COLUMN 前先查 information_schema）
-- 依赖：hou_base_modularizationd（由 10-meoc-schema.sql 创建）
-- 说明：本文件属平台核心（Core），不含任何插件。
-- =====================================================================

-- ---------------------------------------------------------------------
-- 1. 页面表增加模板字段
--    按「页面」生效：每个页面记自己套用的模板，未设置则沿用 pageData 原始版式。
--    存量页面不改值，保持原渲染行为，属零风险变更。
-- ---------------------------------------------------------------------
SET @ddl := (
  SELECT IF(
    (SELECT COUNT(*) FROM information_schema.COLUMNS
      WHERE TABLE_SCHEMA = DATABASE()
        AND TABLE_NAME = 'hou_base_modularizationd'
        AND COLUMN_NAME = 'page_template_code') > 0,
    'DO 0',
    'ALTER TABLE `hou_base_modularizationd`
       ADD COLUMN `page_template_code` varchar(50) DEFAULT NULL
       COMMENT ''本页套用的移动端模板 code：simple/vivid/premium；NULL=不套用模板，沿用 pageData 原始版式'''
  )
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 页面所属端。实体 BaseModularizationdEntity 已有该字段，缺列会让所有走该实体的
-- 查询直接 500（典型：底部导航 queryBottomNavigation 报 20601，H5 底部菜单消失）。
SET @ddl := (
  SELECT IF(
    (SELECT COUNT(*) FROM information_schema.COLUMNS
      WHERE TABLE_SCHEMA = DATABASE()
        AND TABLE_NAME = 'hou_base_modularizationd'
        AND COLUMN_NAME = 'page_platform') > 0,
    'DO 0',
    'ALTER TABLE `hou_base_modularizationd`
       ADD COLUMN `page_platform` varchar(10) NOT NULL DEFAULT ''mobile''
       COMMENT ''页面所属端：mobile=移动端(H5/小程序) pc=PC端'''
  )
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- ---------------------------------------------------------------------
-- 2. 兜底：若表中存在历史脏值（模板库外的 code），归零，避免前台取不到模板
--    模板库当前为 simple / vivid / premium；后续新增模板码不在此白名单内。
-- ---------------------------------------------------------------------
UPDATE `hou_base_modularizationd`
   SET `page_template_code` = NULL
 WHERE `page_template_code` IS NOT NULL
   AND `page_template_code` NOT IN ('simple', 'vivid', 'premium');

-- ---------------------------------------------------------------------
-- 3. 后台菜单：装修管理（id=60010, paths=/mall/decoration）下新增「模板选择」
--    id 不写死：60011 已被「商城设置」占用，取 MAX(id)+1，按 paths 判重。
--    注意：hou_permission_menu 的类型列名是 type（非 menu_type），
--    且无 redirect / is_del 列。
--    幂等：INSERT ... SELECT ... WHERE NOT EXISTS，按 paths 判重。
-- ---------------------------------------------------------------------
SET @menu_path := '/mall/decoration/pages/template';
SET @menu_id := (
  SELECT IFNULL(MAX(`id`), 0) + 1 FROM `hou_permission_menu`
   WHERE `id` < 900000000
);
INSERT INTO `hou_permission_menu`
  (`id`, `pid`, `name`, `type`, `sort`, `perms`, `paths`, `icon`, `component`,
   `is_show`, `is_cache`, `create_time`, `update_time`)
SELECT @menu_id, 60010, '模板选择', 'C', 8950, 'mall:decoration:template',
       @menu_path, 'Brush', 'mall/decoration/pages/template',
       1, 1, UNIX_TIMESTAMP(), UNIX_TIMESTAMP()
 WHERE NOT EXISTS (
       SELECT 1 FROM (SELECT `id` FROM `hou_permission_menu` WHERE `paths` = @menu_path) AS t
 )
   AND EXISTS (SELECT 1 FROM (SELECT `id` FROM `hou_permission_menu` WHERE `id` = 60010) AS p);

-- 授权给已有装修权限的角色（继承 60010 的授权集合；超级管理员 role_id=1 全量权限，不重复处理）
SET @new_menu_id := (
  SELECT `id` FROM `hou_permission_menu` WHERE `paths` = @menu_path LIMIT 1
);
INSERT IGNORE INTO `hou_permission_role_menu` (`role_id`, `menu_id`)
SELECT r.`role_id`, @new_menu_id
  FROM `hou_permission_role_menu` r
 WHERE r.`menu_id` = 60010;
