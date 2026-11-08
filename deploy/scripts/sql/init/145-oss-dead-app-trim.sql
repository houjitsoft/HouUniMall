-- 145：开源裁剪·死应用入口回收（✅ 2026-10-30 起进主仓 order.txt，与 141 同款「平台库守卫」自适应）
-- ====================================================================
-- 背景（2026-10-29 第二十八坑，2026-10-30 根治漏发）：顶栏「插件应用」默认出现
--     「支付中心」「低代码平台」两项，用户质疑「为什么默认会有这两个插件」。
--     查证结论：两条都不是市场安装的，而是 99-seed.sql 带进来的插件位菜单
--     （id=15 /plugin/unipay、id=21 /lowcode），但开源包根本没有交付它们的运行资产：
--       ① 低代码平台：开源树没有 frontend/admin 的 lowcode 路由与视图产物，
--          直连 /lowcode/dashboard 返回「页面不存在 路径未注册」；库里另有 62 条
--          /lowcode* 菜单，其中 24001-24056 的父节点 23001-23010 已不存在，属历史悬挂数据。
--       ② 支付中心旧插件入口（/plugin/unipay）：2026-11-02 起支付中心是 payment 独立服务，
--          开源包含 backend/paas/payment 与 admin /payment/**。152 会把菜单改回 /payment；
--          本脚本只清残留的 unipay 插件路径与目录/安装登记，不再删除 /payment 核心菜单。
--
-- 🔴 平台库守卫（与 141 同款，2026-10-30 实测锚点：生产真库 hou_platform_site 有 1 行）：
--     平台库（hou_platform_site 有行 = 生产/商业部署，低代码与支付中心有真实产物和宿主）
--     → 全部操作跳过；站点/开源形态（表不存在或 0 行）→ 执行裁剪。
--     生产全新重装的恢复路径同 141 头注释：备份表还原或重走安装动作。
--
-- 语义 = 显式卸载三件套（与 PluginMenuSyncPort.remove 一致）：
--       删插件位菜单 + 角色关联 → install 置未安装 → 目录行软删（reconciler 无在用信号不复活）。
-- 执行时机：必须在 110（建 unipay）与 141（保留 unipay 后回收）之后。
-- 幂等：DELETE/UPDATE 重复执行无副作用；守卫 PREPARE 幂等。
SET NAMES utf8mb4;

-- ---------------------------------------------------------------- 平台库守卫
SET @has_platform_tbl_145 := (
  SELECT COUNT(*) FROM information_schema.tables
   WHERE table_schema = DATABASE() AND table_name = 'hou_platform_site');
SET @guard_sql_145 := IF(@has_platform_tbl_145 > 0,
  'SELECT COUNT(*) INTO @is_platform_145 FROM hou_platform_site',
  'SET @is_platform_145 := 0');
PREPARE g145 FROM @guard_sql_145; EXECUTE g145; DEALLOCATE PREPARE g145;
SELECT IF(@is_platform_145 > 0,
       '平台库（托管站点存在）——跳过死入口裁剪，低代码/支付中心菜单保留',
       '站点/开源形态——执行死入口裁剪：低代码/支付中心的位菜单/安装记录/目录行撤下')
     AS trim_mode_145;

-- 一次性备份（重跑不覆盖；平台库也会建空备份表，留痕无害）
CREATE TABLE IF NOT EXISTS hou_permission_menu_bak_deadapp_20261029 AS
 SELECT * FROM hou_permission_menu;
CREATE TABLE IF NOT EXISTS hou_addon_plugin_bak_deadapp_20261029 AS
 SELECT * FROM hou_addon_plugin;
CREATE TABLE IF NOT EXISTS hou_addon_plugin_install_bak_deadapp_20261029 AS
 SELECT * FROM hou_addon_plugin_install;

-- ① 低代码平台：先删角色关联，再删菜单（按路径前缀圈定，不依赖 pid——历史父节点已缺失）
DELETE rm FROM hou_permission_role_menu rm
  JOIN hou_permission_menu m ON m.id = rm.menu_id
 WHERE @is_platform_145 = 0
   AND (m.paths = '/lowcode' OR m.paths LIKE '/lowcode/%');

DELETE FROM hou_permission_menu
 WHERE @is_platform_145 = 0
   AND (paths = '/lowcode' OR paths LIKE '/lowcode/%' OR plugin_code = 'lowcode');

-- ② 支付中心：2026-11-02 起是 payment 独立服务核心入口（152 已改回 /payment）。
--    开源包含 backend/paas/payment + admin /payment/**，菜单必须保留。
--    这里只清残留的旧插件路径，以及 unipay 目录/安装登记，避免对账器把它当已装插件。
DELETE rm FROM hou_permission_role_menu rm
  JOIN hou_permission_menu m ON m.id = rm.menu_id
 WHERE @is_platform_145 = 0
   AND (m.paths = '/plugin/unipay' OR m.paths LIKE '/plugin/unipay/%'
        OR m.plugin_code = 'unipay');

DELETE FROM hou_permission_menu
 WHERE @is_platform_145 = 0
   AND (paths = '/plugin/unipay' OR paths LIKE '/plugin/unipay/%'
        OR plugin_code = 'unipay');

-- ③ 支付中心（unipay）：install 置未安装
UPDATE hou_addon_plugin_install
   SET installed=0, enabled=0, deleted=1, update_time=NOW()
 WHERE @is_platform_145 = 0
   AND plugin_code = 'unipay';

-- ④ 支付中心（unipay）：目录行软删
UPDATE hou_addon_plugin
   SET deleted=1, update_time=NOW()
 WHERE @is_platform_145 = 0
   AND plugin_code = 'unipay';

-- 自检：低代码仍应为 0；旧 unipay 插件路径/目录应为 0；/payment 核心菜单应保留
SELECT 'lowcode_menus_alive' AS check_item, COUNT(*) c FROM hou_permission_menu
 WHERE paths = '/lowcode' OR paths LIKE '/lowcode/%';
SELECT 'unipay_plugin_paths_alive' AS check_item, COUNT(*) c FROM hou_permission_menu
 WHERE paths = '/plugin/unipay' OR paths LIKE '/plugin/unipay/%' OR plugin_code = 'unipay';
SELECT 'payment_core_menus' AS check_item, COUNT(*) c FROM hou_permission_menu
 WHERE paths = '/payment' OR paths LIKE '/payment/%' OR perms = 'shell:payment' OR perms LIKE 'shell:payment:%';
SELECT 'unipay_addon_alive' AS check_item, COUNT(*) c FROM hou_addon_plugin
 WHERE plugin_code='unipay' AND deleted=0;
SELECT 'plugin_apps_alive' AS check_item, COUNT(*) c FROM hou_addon_plugin
 WHERE deleted=0;
