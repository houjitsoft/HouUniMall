-- 2026-11-03 完整应用 / 运维工具
-- 独立服务（一套完整系统的 standalone 产品）归入「完整应用」。
-- 「应用 / 工具」改名为「完整应用」；「开发 / 运维」「开发与运维」改名为「运维 / 工具」。
-- 日志、AI 网关、低代码、统一登录仍留在运维 / 工具。
-- 幂等：按分类名称定位，不写死分类 ID（生产 id 与新装自增 id 不一致）。
-- 重跑只重复同一归位，不把客户后来改过名的其它分类改回去。
SET NAMES utf8mb4;

CREATE TABLE IF NOT EXISTS hou_addon_cate_bak_fullapp_20261103 AS SELECT * FROM hou_addon_cate;
CREATE TABLE IF NOT EXISTS hou_addon_plugin_bak_fullapp_20261103 AS SELECT * FROM hou_addon_plugin;
CREATE TABLE IF NOT EXISTS hou_market_plugin_bak_fullapp_20261103 AS SELECT * FROM hou_market_plugin;
CREATE TABLE IF NOT EXISTS hou_permission_menu_bak_fullapp_20261103 AS
  SELECT id, pid, name, plugin_code, cate_id, paths, display_pos FROM hou_permission_menu;

UPDATE hou_addon_cate
   SET name='完整应用', sort=0, update_time=UNIX_TIMESTAMP()
 WHERE name IN ('应用 / 工具');

UPDATE hou_addon_cate
   SET name='运维 / 工具', update_time=UNIX_TIMESTAMP()
 WHERE name IN ('开发 / 运维', '开发与运维');

-- 已改名的「完整应用」也固定排到第一（sort=0）
UPDATE hou_addon_cate
   SET sort=0, update_time=UNIX_TIMESTAMP()
 WHERE name='完整应用' AND COALESCE(sort, 0) <> 0;

INSERT INTO hou_addon_cate (name, sort, create_time, update_time)
SELECT '完整应用', 0, UNIX_TIMESTAMP(), UNIX_TIMESTAMP()
 WHERE NOT EXISTS (SELECT 1 FROM hou_addon_cate WHERE name='完整应用');

INSERT INTO hou_addon_cate (name, sort, create_time, update_time)
SELECT '运维 / 工具', 60, UNIX_TIMESTAMP(), UNIX_TIMESTAMP()
 WHERE NOT EXISTS (SELECT 1 FROM hou_addon_cate WHERE name='运维 / 工具');

SET @full_app := (SELECT id FROM hou_addon_cate WHERE name='完整应用' ORDER BY sort, id LIMIT 1);
SET @ops_tool := (SELECT id FROM hou_addon_cate WHERE name='运维 / 工具' ORDER BY sort, id LIMIT 1);

-- 收费独立服务里的完整业务系统。日志与 AI 网关留在运维 / 工具。
UPDATE hou_addon_plugin
   SET cate_id=@full_app, category='完整应用', update_time=NOW()
 WHERE deleted=0
   AND plugin_code IN (
        'crm','cms','erp','finance','geo','im','mes','oa',
        'orderfood','pm','rider','supplier','webpos'
   );

UPDATE hou_market_plugin
   SET cate_id=@full_app, category='完整应用', update_time=NOW()
 WHERE deleted=0
   AND plugin_code IN (
        'crm','cms','erp','finance','geo','im','mes','oa',
        'orderfood','pm','rider','supplier','webpos'
   );

-- 工具类：目录 cate_id 为空时会误落到「应用 / 工具」。改名后固定留在运维 / 工具。
UPDATE hou_addon_plugin
   SET cate_id=@ops_tool, category='运维 / 工具', update_time=NOW()
 WHERE deleted=0
   AND plugin_code IN ('aicode','logging','aigateway','lowcode','unilogin');

UPDATE hou_market_plugin
   SET cate_id=@ops_tool, category='运维 / 工具', update_time=NOW()
 WHERE deleted=0
   AND plugin_code IN ('aicode','logging','aigateway','lowcode','unilogin');

UPDATE hou_addon_plugin p
  JOIN hou_addon_cate c ON c.id=p.cate_id
   SET p.category=c.name, p.update_time=NOW()
 WHERE p.deleted=0
   AND c.name IN ('完整应用', '运维 / 工具')
   AND NOT (p.category COLLATE utf8mb4_general_ci <=> c.name COLLATE utf8mb4_general_ci);

UPDATE hou_market_plugin m
  JOIN hou_addon_cate c ON c.id=m.cate_id
   SET m.category=c.name, m.update_time=NOW()
 WHERE m.deleted=0
   AND c.name IN ('完整应用', '运维 / 工具')
   AND NOT (m.category COLLATE utf8mb4_general_ci <=> c.name COLLATE utf8mb4_general_ci);

-- 插件位根菜单与目录 cate_id 对齐，避免菜单管理仍显示旧分类。
UPDATE hou_permission_menu m
  JOIN hou_addon_plugin p
    ON p.plugin_code COLLATE utf8mb4_general_ci = m.plugin_code COLLATE utf8mb4_general_ci
   AND p.deleted=0
   SET m.cate_id=p.cate_id, m.update_time=UNIX_TIMESTAMP()
 WHERE m.pid=0
   AND COALESCE(p.cate_id,0)>0
   AND m.plugin_code IN (
        'crm','cms','erp','finance','geo','im','mes','oa','orderfood','pm',
        'rider','supplier','webpos','logging','aigateway','lowcode','ocr','aicode'
   );
