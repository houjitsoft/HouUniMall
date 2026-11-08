-- 2026-09-21 插件导航与初始分类。适用于当前共享库的一次性数据迁移；
-- 运行期分类维护必须调用 base/permission API，禁止把本脚本的跨服务 JOIN 搬进运行代码。
-- 六类只是初始值：按名称分配新 ID，不覆盖客户分类；完成标记保证重跑不撤销后续人工调整。
-- 先在隔离库验证，再于维护窗口执行；同名 *_bak_nav_20260921 永久保留首次执行前快照。
-- 本脚本不创建插件登记、不改安装/授权状态、不触碰商户菜单、不删除任何业务菜单。
SET NAMES utf8mb4;

CREATE TABLE IF NOT EXISTS hou_addon_cate_bak_nav_20260921 AS SELECT * FROM hou_addon_cate;
CREATE TABLE IF NOT EXISTS hou_addon_plugin_bak_nav_20260921 AS SELECT * FROM hou_addon_plugin;
CREATE TABLE IF NOT EXISTS hou_market_plugin_bak_nav_20260921 AS SELECT * FROM hou_market_plugin;
CREATE TABLE IF NOT EXISTS hou_permission_menu_bak_nav_20260921 AS SELECT * FROM hou_permission_menu;

-- 老初始化包也可直接执行，不依赖会覆盖客户数据的历史迁移。
SET @nav_db = DATABASE();
SET @nav_sql = (SELECT IF(COUNT(*)=0, 'ALTER TABLE hou_addon_cate ADD COLUMN sort INT NOT NULL DEFAULT 0', 'SELECT 1') FROM information_schema.columns WHERE table_schema=@nav_db AND table_name='hou_addon_cate' AND column_name='sort');
PREPARE nav_stmt FROM @nav_sql; EXECUTE nav_stmt; DEALLOCATE PREPARE nav_stmt;
SET @nav_sql = (SELECT IF(COUNT(*)=0, 'ALTER TABLE hou_addon_plugin ADD COLUMN cate_id INT NOT NULL DEFAULT 0', 'SELECT 1') FROM information_schema.columns WHERE table_schema=@nav_db AND table_name='hou_addon_plugin' AND column_name='cate_id');
PREPARE nav_stmt FROM @nav_sql; EXECUTE nav_stmt; DEALLOCATE PREPARE nav_stmt;
SET @nav_sql = (SELECT IF(COUNT(*)=0, 'ALTER TABLE hou_market_plugin ADD COLUMN cate_id INT NOT NULL DEFAULT 0', 'SELECT 1') FROM information_schema.columns WHERE table_schema=@nav_db AND table_name='hou_market_plugin' AND column_name='cate_id');
PREPARE nav_stmt FROM @nav_sql; EXECUTE nav_stmt; DEALLOCATE PREPARE nav_stmt;
SET @nav_sql = (SELECT IF(COUNT(*)=0, 'ALTER TABLE hou_permission_menu ADD COLUMN cate_id INT NOT NULL DEFAULT 0', 'SELECT 1') FROM information_schema.columns WHERE table_schema=@nav_db AND table_name='hou_permission_menu' AND column_name='cate_id');
PREPARE nav_stmt FROM @nav_sql; EXECUTE nav_stmt; DEALLOCATE PREPARE nav_stmt;
SET @nav_sql = (SELECT IF(COUNT(*)=0, 'ALTER TABLE hou_permission_menu ADD COLUMN plugin_code VARCHAR(64) NOT NULL DEFAULT ''''', 'SELECT 1') FROM information_schema.columns WHERE table_schema=@nav_db AND table_name='hou_permission_menu' AND column_name='plugin_code');
PREPARE nav_stmt FROM @nav_sql; EXECUTE nav_stmt; DEALLOCATE PREPARE nav_stmt;
SET @nav_sql = (SELECT IF(COUNT(*)=0, 'ALTER TABLE hou_permission_menu ADD COLUMN display_pos VARCHAR(16) NOT NULL DEFAULT ''left''', 'SELECT 1') FROM information_schema.columns WHERE table_schema=@nav_db AND table_name='hou_permission_menu' AND column_name='display_pos');
PREPARE nav_stmt FROM @nav_sql; EXECUTE nav_stmt; DEALLOCATE PREPARE nav_stmt;

CREATE TABLE IF NOT EXISTS hou_plugin_navigation_migration (
  migration_key VARCHAR(100) NOT NULL PRIMARY KEY,
  applied_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='导航数据迁移完成记录，重跑不覆盖后续人工配置';

DROP TEMPORARY TABLE IF EXISTS nav_defaults_20260921;
CREATE TEMPORARY TABLE nav_defaults_20260921 (
  name VARCHAR(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci PRIMARY KEY,
  sort INT NOT NULL
);
INSERT INTO nav_defaults_20260921 VALUES
 ('营销与客户',10),('内容与沟通',20),('办公与财务',30),
 ('供应链与生产',40),('门店与履约',50),('开发与运维',60);

DROP TEMPORARY TABLE IF EXISTS nav_plugin_defaults_20260921;
CREATE TEMPORARY TABLE nav_plugin_defaults_20260921 (
 plugin_code VARCHAR(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci PRIMARY KEY,
 category_name VARCHAR(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL
);
INSERT INTO nav_plugin_defaults_20260921 VALUES
 ('crm','营销与客户'),('geo','营销与客户'),('activity','营销与客户'),('mall-activity','营销与客户'),
 ('mall-advertising','营销与客户'),('mall-points','营销与客户'),('mall-distribution','营销与客户'),('demo-coupon','营销与客户'),
 ('cms','内容与沟通'),('mall-live','内容与沟通'),('im','内容与沟通'),
 ('oa','办公与财务'),('pm','办公与财务'),('finance','办公与财务'),('mall-rebate','办公与财务'),
 ('erp','供应链与生产'),('mes','供应链与生产'),('supplier','供应链与生产'),('mall-gather','供应链与生产'),
 ('webpos','门店与履约'),('orderfood','门店与履约'),('rider','门店与履约'),
 ('mall-webpos','门店与履约'),('mall-orderfood','门店与履约'),
 ('aigateway','开发与运维'),('aicode','开发与运维'),('lowcode','开发与运维'),
 ('logging','开发与运维'),('unilogin','开发与运维'),('mall-risk','开发与运维');

-- 历史 ID 仅用作旧官方分类的身份校验，绝不用于新分类分配或重命名。
-- ID 相同但名称被客户改过、名称相同但 ID 不同，均不视为可回收的旧官方分类。
DROP TEMPORARY TABLE IF EXISTS nav_old_stock_20260921;
CREATE TEMPORARY TABLE nav_old_stock_20260921 AS
 SELECT id FROM hou_addon_cate WHERE
 (id=1 AND name IN ('营销','客户营销')) OR (id=2 AND name='工具') OR
 (id=3 AND name IN ('财务','财务办公')) OR (id=4 AND name='商城') OR
 (id=5 AND name IN ('支付','支付结算')) OR (id=6 AND name IN ('办公','生产制造')) OR
 (id=7 AND name='ERP') OR (id=8 AND name='CRM') OR
 (id=9 AND name='合规') OR (id=10 AND name='生产');

START TRANSACTION;
SET @nav_apply = NOT EXISTS (SELECT 1 FROM hou_plugin_navigation_migration
 WHERE migration_key='20260921-plugin-navigation-categories');

INSERT INTO hou_addon_cate (name,sort,create_time,update_time)
 SELECT d.name,d.sort,UNIX_TIMESTAMP(),UNIX_TIMESTAMP() FROM nav_defaults_20260921 d
 WHERE @nav_apply AND NOT EXISTS (SELECT 1 FROM hou_addon_cate c
   WHERE c.name COLLATE utf8mb4_general_ci=d.name COLLATE utf8mb4_general_ci);

-- 仅未分类或仍使用已知旧默认分类的官方插件采用初始分类；客户引用及未知插件保持原样。
UPDATE hou_addon_plugin p
 JOIN nav_plugin_defaults_20260921 d ON d.plugin_code COLLATE utf8mb4_general_ci=p.plugin_code COLLATE utf8mb4_general_ci
 JOIN (SELECT name,MIN(id) AS id FROM hou_addon_cate GROUP BY name) c
   ON c.name COLLATE utf8mb4_general_ci=d.category_name COLLATE utf8mb4_general_ci
 LEFT JOIN nav_old_stock_20260921 old ON old.id=p.cate_id
 SET p.cate_id=c.id,p.category=c.name,p.update_time=NOW()
 WHERE @nav_apply AND (COALESCE(p.cate_id,0)=0 OR old.id IS NOT NULL);

-- 市场引用以目录为权威；尚未登记的官方市场条目仅初始化旧默认/未分类状态。
UPDATE hou_market_plugin m
 JOIN nav_plugin_defaults_20260921 d ON d.plugin_code COLLATE utf8mb4_general_ci=m.plugin_code COLLATE utf8mb4_general_ci
 JOIN hou_addon_plugin p ON p.plugin_code COLLATE utf8mb4_general_ci=m.plugin_code COLLATE utf8mb4_general_ci AND p.deleted=0
 JOIN hou_addon_cate c ON c.id=p.cate_id
 SET m.cate_id=p.cate_id,m.category=c.name,m.update_time=NOW()
 WHERE @nav_apply AND (m.cate_id<>p.cate_id OR NOT(m.category COLLATE utf8mb4_general_ci<=>c.name COLLATE utf8mb4_general_ci));
UPDATE hou_market_plugin m
 JOIN nav_plugin_defaults_20260921 d ON d.plugin_code COLLATE utf8mb4_general_ci=m.plugin_code COLLATE utf8mb4_general_ci
 JOIN (SELECT name,MIN(id) AS id FROM hou_addon_cate GROUP BY name) c
   ON c.name COLLATE utf8mb4_general_ci=d.category_name COLLATE utf8mb4_general_ci
 LEFT JOIN nav_old_stock_20260921 old ON old.id=m.cate_id
 LEFT JOIN hou_addon_plugin p ON p.plugin_code COLLATE utf8mb4_general_ci=m.plugin_code COLLATE utf8mb4_general_ci AND p.deleted=0
 SET m.cate_id=c.id,m.category=c.name,m.update_time=NOW()
 WHERE @nav_apply AND p.id IS NULL AND (COALESCE(m.cate_id,0)=0 OR old.id IS NOT NULL);

-- 核心六项固定；只修改这些根的落点/顺序，不改核心业务子树。
UPDATE hou_permission_menu SET pid=0,display_pos='left',plugin_code='',
 sort=CASE id WHEN 19 THEN 10000 WHEN 1 THEN 9000 WHEN 1009 THEN 8500
              WHEN 15 THEN 8000 WHEN 4 THEN 7000 WHEN 18 THEN 6000 END,
 update_time=UNIX_TIMESTAMP()
 WHERE @nav_apply AND id IN (19,1,1009,15,4,18);
-- 商品与装修仍是商城能力；保留 ID、路由和全部子项，只恢复父级。
UPDATE hou_permission_menu SET pid=1,display_pos='left',update_time=UNIX_TIMESTAMP()
 WHERE @nav_apply AND pid=0 AND plugin_code='' AND
 ((id=1002 AND perms='mall:goods') OR (id=60010 AND perms='mall:decoration'));
-- 插件市场/管理始终属于插件中心，保持业务页面和权限 ID。
UPDATE hou_permission_menu SET pid=4,display_pos='left',update_time=UNIX_TIMESTAMP()
 WHERE @nav_apply AND ((id=100235 AND perms='mall:market') OR (id=239 AND perms='shell:addon'));

-- 老核心路由的应用入口也有显式归属；lowcode 继续使用内置 /lowcode 路由。
-- 2026-09-16 初装快照仍把骑手根写为 O2O、子页挂在插件中心；只修复这组旧签名。
UPDATE hou_permission_menu SET name='骑手配送',perms='shell:rider',paths='/rider',plugin_code='rider',
 sort=840,update_time=UNIX_TIMESTAMP()
 WHERE @nav_apply AND id=5 AND name='O2O' AND paths='' AND perms='';
UPDATE hou_permission_menu SET pid=5,update_time=UNIX_TIMESTAMP()
 WHERE @nav_apply AND pid=4 AND id IN (321,322,323,324,325) AND perms LIKE 'shell:rider:%';
UPDATE hou_permission_menu SET plugin_code=CASE
 WHEN id=21 AND paths='/lowcode' THEN 'lowcode'
 WHEN id=20 AND paths='/aigateway' THEN 'aigateway'
 WHEN id=11 AND paths='/project' THEN 'pm'
 WHEN id=5 AND perms='shell:rider' THEN 'rider'
 WHEN id=60016 AND perms='mall:live' THEN 'mall-live' ELSE plugin_code END
 WHERE @nav_apply AND plugin_code='' AND id IN (5,11,20,21,60016);
UPDATE hou_permission_menu SET plugin_code=SUBSTRING(params,8)
 WHERE @nav_apply AND plugin_code='' AND params LIKE 'plugin:%'
 AND id NOT IN (19,1,1009,15,4,18);
UPDATE hou_permission_menu SET plugin_code=SUBSTRING_INDEX(SUBSTRING(paths,9),'/',1)
 WHERE @nav_apply AND plugin_code='' AND paths LIKE '/plugin/%'
 AND id NOT IN (19,1,1009,15,4,18);

-- 已有路由配置声明 aicloud 是 aicode 的历史别名；统一归属，路径继续兼容。
UPDATE hou_permission_menu SET plugin_code='aicode',update_time=UNIX_TIMESTAMP()
 WHERE @nav_apply AND plugin_code='aicloud';

-- 按真实插件归属找根，不把应用内部页面/按钮提升为一级菜单。
DROP TEMPORARY TABLE IF EXISTS nav_roots_20260921;
CREATE TEMPORARY TABLE nav_roots_20260921 AS
 SELECT m.id,m.plugin_code FROM hou_permission_menu m
 LEFT JOIN hou_permission_menu parent ON parent.id=m.pid
 WHERE m.id NOT IN (19,1,1009,15,4,18) AND m.plugin_code<>'' AND TRIM(m.type) IN ('M','C')
 AND (m.pid=0 OR parent.id IS NULL OR COALESCE(parent.plugin_code,'')='' OR
      parent.plugin_code COLLATE utf8mb4_general_ci<>m.plugin_code COLLATE utf8mb4_general_ci);
UPDATE hou_permission_menu m JOIN nav_roots_20260921 r ON r.id=m.id
 SET m.pid=0,m.display_pos='plugin',m.sort=IF(m.plugin_code='lowcode',700,m.sort),m.update_time=UNIX_TIMESTAMP()
 WHERE @nav_apply;

-- 初装旧快照同时带 /plugin/aicloud 与 /plugin/aicode：旧别名根仅保留隐藏记录，
-- 子页迁给规范根，避免重复入口；不删除菜单或角色授权引用。
DROP TEMPORARY TABLE IF EXISTS nav_alias_roots_20260921;
CREATE TEMPORARY TABLE nav_alias_roots_20260921 AS
 SELECT alias.id,MIN(canonical.id) AS canonical_id FROM hou_permission_menu alias
 JOIN hou_permission_menu canonical ON canonical.plugin_code='aicode' AND canonical.pid=0
   AND canonical.paths='/plugin/aicode' AND canonical.id<>alias.id
 WHERE alias.plugin_code='aicode' AND alias.paths='/plugin/aicloud' AND alias.pid=0
 GROUP BY alias.id;
UPDATE hou_permission_menu child JOIN nav_alias_roots_20260921 alias ON alias.id=child.pid
 SET child.pid=alias.canonical_id,child.update_time=UNIX_TIMESTAMP() WHERE @nav_apply;
UPDATE hou_permission_menu m JOIN nav_alias_roots_20260921 alias ON alias.id=m.id
 SET m.display_pos='hidden',m.update_time=UNIX_TIMESTAMP() WHERE @nav_apply;
DELETE r FROM nav_roots_20260921 r JOIN nav_alias_roots_20260921 alias ON alias.id=r.id;
DROP TEMPORARY TABLE nav_alias_roots_20260921;

-- 菜单只镜像目录分类；新装尚未生成目录行时，按同一初始映射补足根分类。
UPDATE hou_permission_menu m
 JOIN nav_plugin_defaults_20260921 d ON d.plugin_code COLLATE utf8mb4_general_ci=m.plugin_code COLLATE utf8mb4_general_ci
 JOIN hou_addon_plugin p ON p.plugin_code COLLATE utf8mb4_general_ci=m.plugin_code COLLATE utf8mb4_general_ci AND p.deleted=0
 SET m.cate_id=p.cate_id,m.update_time=UNIX_TIMESTAMP()
 WHERE @nav_apply AND m.cate_id<>p.cate_id;
UPDATE hou_permission_menu m
 JOIN nav_roots_20260921 r ON r.id=m.id
 JOIN nav_plugin_defaults_20260921 d ON d.plugin_code COLLATE utf8mb4_general_ci=m.plugin_code COLLATE utf8mb4_general_ci
 JOIN (SELECT name,MIN(id) AS id FROM hou_addon_cate GROUP BY name) c
   ON c.name COLLATE utf8mb4_general_ci=d.category_name COLLATE utf8mb4_general_ci
 LEFT JOIN hou_addon_plugin p ON p.plugin_code COLLATE utf8mb4_general_ci=m.plugin_code COLLATE utf8mb4_general_ci AND p.deleted=0
 LEFT JOIN nav_old_stock_20260921 old ON old.id=m.cate_id
 SET m.cate_id=c.id,m.update_time=UNIX_TIMESTAMP()
 WHERE @nav_apply AND p.id IS NULL AND (COALESCE(m.cate_id,0)=0 OR old.id IS NOT NULL);

-- 保留历史分组和占位行作审计，但从导航隐藏；不创建任何分类菜单根。
UPDATE hou_permission_menu SET display_pos='hidden',update_time=UNIX_TIMESTAMP()
 WHERE @nav_apply AND pid=0 AND params='host:menu-group';
UPDATE hou_permission_menu m LEFT JOIN hou_permission_menu child ON child.pid=m.id
 SET m.display_pos='hidden',m.update_time=UNIX_TIMESTAMP()
 WHERE @nav_apply AND m.pid=0 AND m.name='占位' AND m.paths='' AND child.id IS NULL AND m.plugin_code='';

-- 只清理可证实的旧官方空分类；任何引用（含软删插件/市场行）都会阻止删除。
DELETE c FROM hou_addon_cate c
 JOIN nav_old_stock_20260921 old ON old.id=c.id
 LEFT JOIN hou_addon_plugin p ON p.cate_id=c.id
 LEFT JOIN hou_market_plugin market ON market.cate_id=c.id
 LEFT JOIN hou_permission_menu menu ON menu.cate_id=c.id
 WHERE @nav_apply AND p.id IS NULL AND market.id IS NULL AND menu.id IS NULL;

INSERT INTO hou_plugin_navigation_migration (migration_key)
 SELECT '20260921-plugin-navigation-categories' WHERE @nav_apply;
COMMIT;

SELECT id,name,sort FROM hou_addon_cate ORDER BY sort,id;
SELECT id,name,paths,sort FROM hou_permission_menu WHERE pid=0 AND display_pos='left' ORDER BY sort DESC,id;
-- 应为空；未知客户一级核心项仅报告，不会被本迁移擅自隐藏或删除。
SELECT 'unexpected_left_root' AS issue,id,name,paths FROM hou_permission_menu
 WHERE pid=0 AND display_pos='left' AND id NOT IN (19,1,1009,15,4,18);
SELECT 'nested_plugin_root' AS issue,m.id,m.name,m.pid FROM hou_permission_menu m
 JOIN nav_roots_20260921 r ON r.id=m.id WHERE m.pid<>0 OR m.display_pos<>'plugin';

DROP TEMPORARY TABLE nav_roots_20260921;
DROP TEMPORARY TABLE nav_old_stock_20260921;
DROP TEMPORARY TABLE nav_plugin_defaults_20260921;
DROP TEMPORARY TABLE nav_defaults_20260921;
