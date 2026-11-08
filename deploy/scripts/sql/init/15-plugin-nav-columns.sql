-- =====================================================================
-- 平台核心增量：插件导航列（幂等，可在种子数据之前重复执行）
--
-- 背景：10-meoc-schema.sql 是 2026-09-16 的结构快照，早于下列列的引入时间。
--       99-seed.sql 末尾的菜单校正语句直接 UPDATE display_pos，若这些列还没建，
--       全新库初始化会以 ERROR 1054 中断（set -e 下 Docker 初始化直接停摆，
--       初始管理员也不会落库）。
-- 因此本脚本必须在 99-seed.sql **之前**执行；100-plugin-navigation-categories.sql
-- 里保留同样一段幂等 ALTER，是为了让那份迁移脚本能被单独人工执行。
--
-- 依赖：10-meoc-schema.sql（hou_permission_menu / hou_addon_cate /
--       hou_addon_plugin / hou_market_plugin）
-- 幂等：全部按 information_schema 判定「表存在且列不存在」才 ALTER。
-- =====================================================================

SET NAMES utf8mb4;

SET @nav_db = DATABASE();

-- hou_permission_menu.cate_id（插件分类 ID，0 = 非插件菜单）
SET @nav_exists = (SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = @nav_db AND table_name = 'hou_permission_menu');
SET @nav_has_col = (SELECT COUNT(*) FROM information_schema.columns WHERE table_schema = @nav_db AND table_name = 'hou_permission_menu' AND column_name = 'cate_id');
SET @nav_sql = IF(@nav_exists = 1 AND @nav_has_col = 0,
  'ALTER TABLE `hou_permission_menu` ADD COLUMN `cate_id` INT NOT NULL DEFAULT 0 COMMENT ''插件分类ID，0=非插件菜单''',
  'SELECT 1');
PREPARE nav_stmt FROM @nav_sql; EXECUTE nav_stmt; DEALLOCATE PREPARE nav_stmt;

-- hou_permission_menu.plugin_code（插件菜单归属的插件编码，'' = 非插件菜单）
SET @nav_has_col = (SELECT COUNT(*) FROM information_schema.columns WHERE table_schema = @nav_db AND table_name = 'hou_permission_menu' AND column_name = 'plugin_code');
SET @nav_sql = IF(@nav_exists = 1 AND @nav_has_col = 0,
  'ALTER TABLE `hou_permission_menu` ADD COLUMN `plugin_code` VARCHAR(64) NOT NULL DEFAULT '''' COMMENT ''插件编码，空=非插件菜单''',
  'SELECT 1');
PREPARE nav_stmt FROM @nav_sql; EXECUTE nav_stmt; DEALLOCATE PREPARE nav_stmt;

-- hou_permission_menu.display_pos（一级菜单落点：left / plugin / hidden）
SET @nav_has_col = (SELECT COUNT(*) FROM information_schema.columns WHERE table_schema = @nav_db AND table_name = 'hou_permission_menu' AND column_name = 'display_pos');
SET @nav_sql = IF(@nav_exists = 1 AND @nav_has_col = 0,
  'ALTER TABLE `hou_permission_menu` ADD COLUMN `display_pos` VARCHAR(16) NOT NULL DEFAULT ''left'' COMMENT ''一级菜单落点：left=左侧 plugin=右上角插件面板 hidden=不展示''',
  'SELECT 1');
PREPARE nav_stmt FROM @nav_sql; EXECUTE nav_stmt; DEALLOCATE PREPARE nav_stmt;

-- hou_addon_cate.sort（插件分类排序）
SET @cate_exists = (SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = @nav_db AND table_name = 'hou_addon_cate');
SET @cate_has_col = (SELECT COUNT(*) FROM information_schema.columns WHERE table_schema = @nav_db AND table_name = 'hou_addon_cate' AND column_name = 'sort');
SET @nav_sql = IF(@cate_exists = 1 AND @cate_has_col = 0,
  'ALTER TABLE `hou_addon_cate` ADD COLUMN `sort` INT NOT NULL DEFAULT 0',
  'SELECT 1');
PREPARE nav_stmt FROM @nav_sql; EXECUTE nav_stmt; DEALLOCATE PREPARE nav_stmt;

-- hou_addon_plugin.cate_id（已登记插件的分类）
SET @addon_exists = (SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = @nav_db AND table_name = 'hou_addon_plugin');
SET @addon_has_col = (SELECT COUNT(*) FROM information_schema.columns WHERE table_schema = @nav_db AND table_name = 'hou_addon_plugin' AND column_name = 'cate_id');
SET @nav_sql = IF(@addon_exists = 1 AND @addon_has_col = 0,
  'ALTER TABLE `hou_addon_plugin` ADD COLUMN `cate_id` INT NOT NULL DEFAULT 0',
  'SELECT 1');
PREPARE nav_stmt FROM @nav_sql; EXECUTE nav_stmt; DEALLOCATE PREPARE nav_stmt;

-- hou_market_plugin.cate_id（市场商品的分类）
SET @market_exists = (SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = @nav_db AND table_name = 'hou_market_plugin');
SET @market_has_col = (SELECT COUNT(*) FROM information_schema.columns WHERE table_schema = @nav_db AND table_name = 'hou_market_plugin' AND column_name = 'cate_id');
SET @nav_sql = IF(@market_exists = 1 AND @market_has_col = 0,
  'ALTER TABLE `hou_market_plugin` ADD COLUMN `cate_id` INT NOT NULL DEFAULT 0',
  'SELECT 1');
PREPARE nav_stmt FROM @nav_sql; EXECUTE nav_stmt; DEALLOCATE PREPARE nav_stmt;

-- 插件导航迁移完成记录表（100-plugin-navigation-categories.sql 会用到；
-- 提前建好可让「先建列、后补数据」的顺序在任何入口下都成立）
CREATE TABLE IF NOT EXISTS `hou_plugin_navigation_migration` (
  `migration_key` VARCHAR(100) NOT NULL,
  `applied_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`migration_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='导航数据迁移完成记录，重跑不覆盖后续人工配置';
