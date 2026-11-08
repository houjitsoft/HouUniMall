-- 141：开源裁剪·站点形态自适应版（✅ 2026-10-30 起进主仓 order.txt，替换旧的「仅导出树」模式）
-- ====================================================================
-- 背景（2026-10-28 第十八坑，2026-10-30 老牛第三次反馈后根治）：
--     99-seed.sql 是生产库快照导出，带入了 15 个商业插件的插件位一级菜单
--     （/plugin/erp|geo|pm|orderfood|logging|supplier|im|aicode|crm|webpos|oa|
--     mes|finance|cms|activity）。PluginRegistryReconciler（base 启动对账）以
--     「插件位菜单存在」为已安装信号之一，启动即把这 15 个未随开源包交付的插件
--     upsert 进 hou_addon_plugin(status=1) + hou_addon_plugin_install(installed=1)
--     —— 开源新装系统里，市场收费插件全部显示「已安装」。
--
-- 🔴 为什么现在敢进主仓 order.txt（旧版不敢）：
--     旧版无条件裁剪，生产误跑会删生产插件菜单，靠「不进主仓 order.txt」防误——
--     但 GitHub 开源仓 = 主仓直推，order.txt 不引用 = 开源包永远不带裁剪，
--     重打包/重导出全靠人手工补，10-28/10-29 三个候选 zip 全部漏裁（用户实测中招）。
--     本版加「平台库守卫」自适应：
--       hou_platform_site 表有行 = 平台库（托管着下游站点的生产/商业部署，
--         这些插件真实内置已安装）→ 全部操作跳过，生产菜单毫发无损；
--       表不存在或 0 行 = 站点/开源形态 → 执行裁剪（产品铁律：初始安装后
--         所有插件默认未安装，站长从插件中心自己装）。
--     实测锚点（2026-10-30）：生产真库 hou_platform_site 有 1 行（协议回传登记）。
--
-- ⚠️ 生产全新重装（init 链从零灌库）的已知权衡：全新生产库 hou_platform_site
--     也是 0 行，本脚本会执行裁剪。灾备重建后 15 个内置插件的位菜单按两路恢复：
--     ① 从本脚本自动建的备份表 hou_permission_menu_bak_osstrim_* 还原；
--     ② 或对相应插件重走安装动作（安装建菜单）。重装后运维验证清单应包含此项。
--
-- 修法（显式卸载语义，PluginMenuSyncPort.remove 同款）：
--     ① 物理删除插件位菜单 + 角色关联（rm JOIN 同删，无悬挂引用）；
--     ② install 记录置未安装（installed=0 + deleted=1，不构成 reconciler「既有安装」信号）；
--     ③ 目录行软删（deleted=1）——reconciler 软删保护 + 无在用信号，重启不复活。
-- 保留：unipay（支付中心，110-payment-to-unipay-plugin.sql 设计内的开源核心）；
--     /project、/aigateway、/rider、/mall/admin/live 等非 /plugin 前缀的历史位菜单
--     不动（不构成 reconciler 安装信号，且走核心内置路由；/lowcode 由 145 处理）。
-- 幂等：DELETE/UPDATE 重复执行无副作用；守卫 PREPARE 幂等。
SET NAMES utf8mb4;

-- ---------------------------------------------------------------- 平台库守卫
SET @has_platform_tbl := (
  SELECT COUNT(*) FROM information_schema.tables
   WHERE table_schema = DATABASE() AND table_name = 'hou_platform_site');
SET @guard_sql := IF(@has_platform_tbl > 0,
  'SELECT COUNT(*) INTO @is_platform FROM hou_platform_site',
  'SET @is_platform := 0');
PREPARE g FROM @guard_sql; EXECUTE g; DEALLOCATE PREPARE g;
SELECT IF(@is_platform > 0,
       '平台库（托管站点存在）——跳过开源裁剪，15 个内置插件菜单保留',
       '站点/开源形态——执行开源裁剪：未交付插件的位菜单/安装记录/目录行全部撤下')
     AS trim_mode_141;

-- 一次性备份（重跑不覆盖；平台库也会建空备份表，留痕无害）
CREATE TABLE IF NOT EXISTS hou_permission_menu_bak_osstrim_20261028 AS
 SELECT * FROM hou_permission_menu;
CREATE TABLE IF NOT EXISTS hou_addon_plugin_bak_osstrim_20261028 AS
 SELECT * FROM hou_addon_plugin;
CREATE TABLE IF NOT EXISTS hou_addon_plugin_install_bak_osstrim_20261028 AS
 SELECT * FROM hou_addon_plugin_install;

-- ① 插件位菜单 + 角色关联（谓词四重限定：守卫 + pid=0 + plugin 位 + 15 个 code 精确圈定）
DELETE rm FROM hou_permission_role_menu rm
  JOIN hou_permission_menu m ON m.id = rm.menu_id
 WHERE @is_platform = 0
   AND m.pid=0 AND m.display_pos='plugin' AND m.paths LIKE '/plugin/%'
   AND m.plugin_code IN ('crm','logging','cms','im','finance','mes','webpos','oa',
                          'pm','supplier','aicode','erp','geo','orderfood','activity');

DELETE FROM hou_permission_menu
 WHERE @is_platform = 0
   AND pid=0 AND display_pos='plugin' AND paths LIKE '/plugin/%'
   AND plugin_code IN ('crm','logging','cms','im','finance','mes','webpos','oa',
                       'pm','supplier','aicode','erp','geo','orderfood','activity');

-- ② 安装记录置未安装（installed=0 才不会被 reconciler 当「既有安装」信号）
UPDATE hou_addon_plugin_install
   SET installed=0, enabled=0, deleted=1, update_time=NOW()
 WHERE @is_platform = 0
   AND plugin_code IN ('crm','logging','cms','im','finance','mes','webpos','oa',
                       'pm','supplier','aicode','erp','geo','orderfood','activity');

-- ③ 目录行软删（reconciler 的反向保护：软删 + 无在用信号 = 不复活）
UPDATE hou_addon_plugin
   SET deleted=1, update_time=NOW()
 WHERE @is_platform = 0
   AND plugin_code IN ('crm','logging','cms','im','finance','mes','webpos','oa',
                       'pm','supplier','aicode','erp','geo','orderfood','activity');

-- 自检（站点形态下：位一级菜单应只剩 unipay；目录/安装表存活行应只剩 unipay）
SELECT 'plugin_pos_menus_alive' AS check_item, COUNT(*) c FROM hou_permission_menu
 WHERE pid=0 AND display_pos='plugin' AND paths LIKE '/plugin/%';
SELECT 'addon_rows_alive' AS check_item, COUNT(*) c FROM hou_addon_plugin
 WHERE deleted=0;
SELECT 'install_rows_alive' AS check_item, COUNT(*) c FROM hou_addon_plugin_install
 WHERE installed=1 AND deleted=0;
