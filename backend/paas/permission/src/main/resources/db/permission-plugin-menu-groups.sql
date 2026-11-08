-- 插件一级菜单分组根种子（宿主资产）。
-- 方案：《开源插件化架构/插件菜单治理方案.md》§5.1/§5.2。
-- 幂等：NOT EXISTS 防重插 + UPDATE 修复漂移；可重复执行。
--
-- 关键约定：
-- 1. params = 'host:menu-group' 是宿主归属标记，
--    PluginMenuSyncPort.ownershipPredicate 已排除该标记 → 插件卸载永不触碰分组根；
-- 2. perms = 'shell:group:{key}' 仅作稳定标识，不授予任何角色——
--    用户可见性由组内子菜单授权 + MenuAncestorSupport 祖先补全决定；
-- 3. 分组 key/sort 与 plugin-registry 的 MenuGroupRegistry 由契约测试锁定一致；
-- 4. 空分组由前端自动隐藏，后端不做空组清理。

-- group: biz|业务经营|3800|Shop
INSERT INTO hou_permission_menu
  (pid, type, name, icon, sort, perms, paths, component, selected, params,
   is_cache, is_show, is_disable, create_time, update_time)
SELECT 0, 'M', '业务经营', 'Shop', 3800,
       'shell:group:biz', '/group/biz', '', '', 'host:menu-group',
       0, 1, 0, UNIX_TIMESTAMP(), UNIX_TIMESTAMP()
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM hou_permission_menu WHERE perms = 'shell:group:biz');

-- group: growth|营销增长|3700|Promotion
INSERT INTO hou_permission_menu
  (pid, type, name, icon, sort, perms, paths, component, selected, params,
   is_cache, is_show, is_disable, create_time, update_time)
SELECT 0, 'M', '营销增长', 'Promotion', 3700,
       'shell:group:growth', '/group/growth', '', '', 'host:menu-group',
       0, 1, 0, UNIX_TIMESTAMP(), UNIX_TIMESTAMP()
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM hou_permission_menu WHERE perms = 'shell:group:growth');

-- group: org|组织协同|3600|User
INSERT INTO hou_permission_menu
  (pid, type, name, icon, sort, perms, paths, component, selected, params,
   is_cache, is_show, is_disable, create_time, update_time)
SELECT 0, 'M', '组织协同', 'User', 3600,
       'shell:group:org', '/group/org', '', '', 'host:menu-group',
       0, 1, 0, UNIX_TIMESTAMP(), UNIX_TIMESTAMP()
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM hou_permission_menu WHERE perms = 'shell:group:org');

-- group: fin|财务合规|3500|Money
INSERT INTO hou_permission_menu
  (pid, type, name, icon, sort, perms, paths, component, selected, params,
   is_cache, is_show, is_disable, create_time, update_time)
SELECT 0, 'M', '财务合规', 'Money', 3500,
       'shell:group:fin', '/group/fin', '', '', 'host:menu-group',
       0, 1, 0, UNIX_TIMESTAMP(), UNIX_TIMESTAMP()
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM hou_permission_menu WHERE perms = 'shell:group:fin');

-- group: ai|智能工具|3400|Cpu
INSERT INTO hou_permission_menu
  (pid, type, name, icon, sort, perms, paths, component, selected, params,
   is_cache, is_show, is_disable, create_time, update_time)
SELECT 0, 'M', '智能工具', 'Cpu', 3400,
       'shell:group:ai', '/group/ai', '', '', 'host:menu-group',
       0, 1, 0, UNIX_TIMESTAMP(), UNIX_TIMESTAMP()
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM hou_permission_menu WHERE perms = 'shell:group:ai');

-- group: other|其他应用|3300|Grid
INSERT INTO hou_permission_menu
  (pid, type, name, icon, sort, perms, paths, component, selected, params,
   is_cache, is_show, is_disable, create_time, update_time)
SELECT 0, 'M', '其他应用', 'Grid', 3300,
       'shell:group:other', '/group/other', '', '', 'host:menu-group',
       0, 1, 0, UNIX_TIMESTAMP(), UNIX_TIMESTAMP()
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM hou_permission_menu WHERE perms = 'shell:group:other');

-- 漂移修复：分组根已存在但关键属性被改动时拉回规范值（幂等）。
UPDATE hou_permission_menu
SET pid = 0, type = 'M', params = 'host:menu-group', is_show = 1, is_disable = 0,
    update_time = UNIX_TIMESTAMP()
WHERE perms IN ('shell:group:biz', 'shell:group:growth', 'shell:group:org',
                'shell:group:fin', 'shell:group:ai', 'shell:group:other');
