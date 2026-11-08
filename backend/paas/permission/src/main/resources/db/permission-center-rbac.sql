-- 权限中心 21 个核心权限码的幂等修复。
-- 所有父节点均按 perms 定位，不依赖固定自增 ID。

INSERT INTO hou_permission_menu
  (pid, type, name, icon, sort, perms, paths, component, selected, params,
   is_cache, is_show, is_disable, create_time, update_time)
SELECT parent.id, 'M', '权限管理', 'Lock', 9500,
       'shell:setting:permission', '/setting/permission', '', '', '',
       0, 1, 0, parent.create_time, parent.update_time
FROM hou_permission_menu parent
WHERE parent.perms = 'shell:setting'
  AND NOT EXISTS (
    SELECT 1 FROM hou_permission_menu menu
    WHERE menu.perms = 'shell:setting:permission'
  )
LIMIT 1;

INSERT INTO hou_permission_menu
  (pid, type, name, icon, sort, perms, paths, component, selected, params,
   is_cache, is_show, is_disable, create_time, update_time)
SELECT root.id, 'C', '菜单管理', 'Menu', 1,
       'shell:setting:permission:menu', '/setting/permission/menu',
       'setting/MenuManagement', '', '', 0, 1, 0, root.create_time, root.update_time
FROM hou_permission_menu root
WHERE root.perms = 'shell:setting:permission'
  AND NOT EXISTS (
    SELECT 1 FROM hou_permission_menu menu
    WHERE menu.perms = 'shell:setting:permission:menu'
  )
LIMIT 1;

INSERT INTO hou_permission_menu
  (pid, type, name, icon, sort, perms, paths, component, selected, params,
   is_cache, is_show, is_disable, create_time, update_time)
SELECT parent.id, 'A', '新增菜单', 'Plus', 1,
       'shell:setting:permission:menu:add', '', '', '', '',
       0, 1, 0, parent.create_time, parent.update_time
FROM hou_permission_menu parent
WHERE parent.perms = 'shell:setting:permission:menu'
  AND NOT EXISTS (
    SELECT 1 FROM hou_permission_menu menu
    WHERE menu.perms = 'shell:setting:permission:menu:add'
  )
LIMIT 1;

INSERT INTO hou_permission_menu
  (pid, type, name, icon, sort, perms, paths, component, selected, params,
   is_cache, is_show, is_disable, create_time, update_time)
SELECT parent.id, 'A', '编辑菜单', 'Edit', 2,
       'shell:setting:permission:menu:edit', '', '', '', '',
       0, 1, 0, parent.create_time, parent.update_time
FROM hou_permission_menu parent
WHERE parent.perms = 'shell:setting:permission:menu'
  AND NOT EXISTS (
    SELECT 1 FROM hou_permission_menu menu
    WHERE menu.perms = 'shell:setting:permission:menu:edit'
  )
LIMIT 1;

INSERT INTO hou_permission_menu
  (pid, type, name, icon, sort, perms, paths, component, selected, params,
   is_cache, is_show, is_disable, create_time, update_time)
SELECT parent.id, 'A', '删除菜单', 'Delete', 3,
       'shell:setting:permission:menu:delete', '', '', '', '',
       0, 1, 0, parent.create_time, parent.update_time
FROM hou_permission_menu parent
WHERE parent.perms = 'shell:setting:permission:menu'
  AND NOT EXISTS (
    SELECT 1 FROM hou_permission_menu menu
    WHERE menu.perms = 'shell:setting:permission:menu:delete'
  )
LIMIT 1;

INSERT INTO hou_permission_menu
  (pid, type, name, icon, sort, perms, paths, component, selected, params,
   is_cache, is_show, is_disable, create_time, update_time)
SELECT parent.id, 'A', '启用菜单', 'Check', 4,
       'shell:setting:permission:menu:enable', '', '', '', '',
       0, 1, 0, parent.create_time, parent.update_time
FROM hou_permission_menu parent
WHERE parent.perms = 'shell:setting:permission:menu'
  AND NOT EXISTS (
    SELECT 1 FROM hou_permission_menu menu
    WHERE menu.perms = 'shell:setting:permission:menu:enable'
  )
LIMIT 1;

INSERT INTO hou_permission_menu
  (pid, type, name, icon, sort, perms, paths, component, selected, params,
   is_cache, is_show, is_disable, create_time, update_time)
SELECT parent.id, 'A', '禁用菜单', 'Close', 5,
       'shell:setting:permission:menu:disable', '', '', '', '',
       0, 1, 0, parent.create_time, parent.update_time
FROM hou_permission_menu parent
WHERE parent.perms = 'shell:setting:permission:menu'
  AND NOT EXISTS (
    SELECT 1 FROM hou_permission_menu menu
    WHERE menu.perms = 'shell:setting:permission:menu:disable'
  )
LIMIT 1;

INSERT INTO hou_permission_menu
  (pid, type, name, icon, sort, perms, paths, component, selected, params,
   is_cache, is_show, is_disable, create_time, update_time)
SELECT root.id, 'C', '角色管理', 'User', 2,
       'shell:setting:permission:role', '/setting/permission/role',
       'setting/RoleManagement', '', '', 0, 1, 0, root.create_time, root.update_time
FROM hou_permission_menu root
WHERE root.perms = 'shell:setting:permission'
  AND NOT EXISTS (
    SELECT 1 FROM hou_permission_menu menu
    WHERE menu.perms = 'shell:setting:permission:role'
  )
LIMIT 1;

INSERT INTO hou_permission_menu
  (pid, type, name, icon, sort, perms, paths, component, selected, params,
   is_cache, is_show, is_disable, create_time, update_time)
SELECT parent.id, 'A', '新增角色', 'Plus', 1,
       'shell:setting:permission:role:add', '', '', '', '',
       0, 1, 0, parent.create_time, parent.update_time
FROM hou_permission_menu parent
WHERE parent.perms = 'shell:setting:permission:role'
  AND NOT EXISTS (
    SELECT 1 FROM hou_permission_menu menu
    WHERE menu.perms = 'shell:setting:permission:role:add'
  )
LIMIT 1;

INSERT INTO hou_permission_menu
  (pid, type, name, icon, sort, perms, paths, component, selected, params,
   is_cache, is_show, is_disable, create_time, update_time)
SELECT parent.id, 'A', '编辑角色', 'Edit', 2,
       'shell:setting:permission:role:edit', '', '', '', '',
       0, 1, 0, parent.create_time, parent.update_time
FROM hou_permission_menu parent
WHERE parent.perms = 'shell:setting:permission:role'
  AND NOT EXISTS (
    SELECT 1 FROM hou_permission_menu menu
    WHERE menu.perms = 'shell:setting:permission:role:edit'
  )
LIMIT 1;

INSERT INTO hou_permission_menu
  (pid, type, name, icon, sort, perms, paths, component, selected, params,
   is_cache, is_show, is_disable, create_time, update_time)
SELECT parent.id, 'A', '删除角色', 'Delete', 3,
       'shell:setting:permission:role:delete', '', '', '', '',
       0, 1, 0, parent.create_time, parent.update_time
FROM hou_permission_menu parent
WHERE parent.perms = 'shell:setting:permission:role'
  AND NOT EXISTS (
    SELECT 1 FROM hou_permission_menu menu
    WHERE menu.perms = 'shell:setting:permission:role:delete'
  )
LIMIT 1;

INSERT INTO hou_permission_menu
  (pid, type, name, icon, sort, perms, paths, component, selected, params,
   is_cache, is_show, is_disable, create_time, update_time)
SELECT parent.id, 'A', '分配权限', 'Key', 4,
       'shell:setting:permission:role:assign', '', '', '', '',
       0, 1, 0, parent.create_time, parent.update_time
FROM hou_permission_menu parent
WHERE parent.perms = 'shell:setting:permission:role'
  AND NOT EXISTS (
    SELECT 1 FROM hou_permission_menu menu
    WHERE menu.perms = 'shell:setting:permission:role:assign'
  )
LIMIT 1;

INSERT INTO hou_permission_menu
  (pid, type, name, icon, sort, perms, paths, component, selected, params,
   is_cache, is_show, is_disable, create_time, update_time)
SELECT root.id, 'C', '用户管理', 'UserFilled', 3,
       'shell:setting:permission:user', '/setting/permission/user',
       'setting/UserPermissions', '', '', 0, 1, 0, root.create_time, root.update_time
FROM hou_permission_menu root
WHERE root.perms = 'shell:setting:permission'
  AND NOT EXISTS (
    SELECT 1 FROM hou_permission_menu menu
    WHERE menu.perms = 'shell:setting:permission:user'
  )
LIMIT 1;

INSERT INTO hou_permission_menu
  (pid, type, name, icon, sort, perms, paths, component, selected, params,
   is_cache, is_show, is_disable, create_time, update_time)
SELECT parent.id, 'A', '新增用户', 'Plus', 1,
       'shell:setting:permission:user:add', '', '', '', '',
       0, 1, 0, parent.create_time, parent.update_time
FROM hou_permission_menu parent
WHERE parent.perms = 'shell:setting:permission:user'
  AND NOT EXISTS (
    SELECT 1 FROM hou_permission_menu menu
    WHERE menu.perms = 'shell:setting:permission:user:add'
  )
LIMIT 1;

INSERT INTO hou_permission_menu
  (pid, type, name, icon, sort, perms, paths, component, selected, params,
   is_cache, is_show, is_disable, create_time, update_time)
SELECT parent.id, 'A', '编辑用户', 'Edit', 2,
       'shell:setting:permission:user:edit', '', '', '', '',
       0, 1, 0, parent.create_time, parent.update_time
FROM hou_permission_menu parent
WHERE parent.perms = 'shell:setting:permission:user'
  AND NOT EXISTS (
    SELECT 1 FROM hou_permission_menu menu
    WHERE menu.perms = 'shell:setting:permission:user:edit'
  )
LIMIT 1;

INSERT INTO hou_permission_menu
  (pid, type, name, icon, sort, perms, paths, component, selected, params,
   is_cache, is_show, is_disable, create_time, update_time)
SELECT parent.id, 'A', '删除用户', 'Delete', 3,
       'shell:setting:permission:user:delete', '', '', '', '',
       0, 1, 0, parent.create_time, parent.update_time
FROM hou_permission_menu parent
WHERE parent.perms = 'shell:setting:permission:user'
  AND NOT EXISTS (
    SELECT 1 FROM hou_permission_menu menu
    WHERE menu.perms = 'shell:setting:permission:user:delete'
  )
LIMIT 1;

INSERT INTO hou_permission_menu
  (pid, type, name, icon, sort, perms, paths, component, selected, params,
   is_cache, is_show, is_disable, create_time, update_time)
SELECT parent.id, 'A', '重置密码', 'Refresh', 4,
       'shell:setting:permission:user:reset', '', '', '', '',
       0, 1, 0, parent.create_time, parent.update_time
FROM hou_permission_menu parent
WHERE parent.perms = 'shell:setting:permission:user'
  AND NOT EXISTS (
    SELECT 1 FROM hou_permission_menu menu
    WHERE menu.perms = 'shell:setting:permission:user:reset'
  )
LIMIT 1;

INSERT INTO hou_permission_menu
  (pid, type, name, icon, sort, perms, paths, component, selected, params,
   is_cache, is_show, is_disable, create_time, update_time)
SELECT parent.id, 'A', '分配角色', 'User', 5,
       'shell:setting:permission:user:assign', '', '', '', '',
       0, 1, 0, parent.create_time, parent.update_time
FROM hou_permission_menu parent
WHERE parent.perms = 'shell:setting:permission:user'
  AND NOT EXISTS (
    SELECT 1 FROM hou_permission_menu menu
    WHERE menu.perms = 'shell:setting:permission:user:assign'
  )
LIMIT 1;

INSERT INTO hou_permission_menu
  (pid, type, name, icon, sort, perms, paths, component, selected, params,
   is_cache, is_show, is_disable, create_time, update_time)
SELECT parent.id, 'A', '启用用户', 'Check', 6,
       'shell:setting:permission:user:enable', '', '', '', '',
       0, 1, 0, parent.create_time, parent.update_time
FROM hou_permission_menu parent
WHERE parent.perms = 'shell:setting:permission:user'
  AND NOT EXISTS (
    SELECT 1 FROM hou_permission_menu menu
    WHERE menu.perms = 'shell:setting:permission:user:enable'
  )
LIMIT 1;

INSERT INTO hou_permission_menu
  (pid, type, name, icon, sort, perms, paths, component, selected, params,
   is_cache, is_show, is_disable, create_time, update_time)
SELECT parent.id, 'A', '禁用用户', 'Close', 7,
       'shell:setting:permission:user:disable', '', '', '', '',
       0, 1, 0, parent.create_time, parent.update_time
FROM hou_permission_menu parent
WHERE parent.perms = 'shell:setting:permission:user'
  AND NOT EXISTS (
    SELECT 1 FROM hou_permission_menu menu
    WHERE menu.perms = 'shell:setting:permission:user:disable'
  )
LIMIT 1;

INSERT INTO hou_permission_menu
  (pid, type, name, icon, sort, perms, paths, component, selected, params,
   is_cache, is_show, is_disable, create_time, update_time)
SELECT root.id, 'A', '清理权限缓存', 'Refresh', 99,
       'shell:setting:permission:cache:clear', '', '', '', '',
       0, 1, 0, root.create_time, root.update_time
FROM hou_permission_menu root
WHERE root.perms = 'shell:setting:permission'
  AND NOT EXISTS (
    SELECT 1 FROM hou_permission_menu menu
    WHERE menu.perms = 'shell:setting:permission:cache:clear'
  )
LIMIT 1;

-- 使用派生表读取父 ID，兼容 MySQL 对“更新并读取同一表”的限制。
UPDATE hou_permission_menu
SET pid = (
      SELECT parent_id FROM (
        SELECT id AS parent_id FROM hou_permission_menu WHERE perms = 'shell:setting'
      ) parent_lookup
    ),
    type = 'M', name = '权限管理', icon = 'Lock', sort = 9500,
    paths = '/setting/permission', component = '', selected = '', params = '',
    is_cache = 0, is_show = 1, is_disable = 0
WHERE perms = 'shell:setting:permission';

UPDATE hou_permission_menu
SET pid = (
      SELECT parent_id FROM (
        SELECT id AS parent_id FROM hou_permission_menu
        WHERE perms = 'shell:setting:permission'
      ) parent_lookup
    ),
    type = 'C', name = '菜单管理', icon = 'Menu', sort = 1,
    paths = '/setting/permission/menu', component = 'setting/MenuManagement',
    selected = '', params = '', is_cache = 0, is_show = 1, is_disable = 0
WHERE perms = 'shell:setting:permission:menu';

UPDATE hou_permission_menu
SET pid = (
      SELECT parent_id FROM (
        SELECT id AS parent_id FROM hou_permission_menu
        WHERE perms = 'shell:setting:permission'
      ) parent_lookup
    ),
    type = 'C', name = '角色管理', icon = 'User', sort = 2,
    paths = '/setting/permission/role', component = 'setting/RoleManagement',
    selected = '', params = '', is_cache = 0, is_show = 1, is_disable = 0
WHERE perms = 'shell:setting:permission:role';

UPDATE hou_permission_menu
SET pid = (
      SELECT parent_id FROM (
        SELECT id AS parent_id FROM hou_permission_menu
        WHERE perms = 'shell:setting:permission'
      ) parent_lookup
    ),
    type = 'C', name = '用户管理', icon = 'UserFilled', sort = 3,
    paths = '/setting/permission/user', component = 'setting/UserPermissions',
    selected = '', params = '', is_cache = 0, is_show = 1, is_disable = 0
WHERE perms = 'shell:setting:permission:user';

UPDATE hou_permission_menu
SET pid = (
      SELECT parent_id FROM (
        SELECT id AS parent_id FROM hou_permission_menu
        WHERE perms = 'shell:setting:permission:menu'
      ) parent_lookup
    ),
    type = 'A', paths = '', component = '', selected = '', params = '',
    is_cache = 0, is_show = 1, is_disable = 0
WHERE perms IN (
  'shell:setting:permission:menu:add',
  'shell:setting:permission:menu:edit',
  'shell:setting:permission:menu:delete',
  'shell:setting:permission:menu:enable',
  'shell:setting:permission:menu:disable'
);

UPDATE hou_permission_menu
SET pid = (
      SELECT parent_id FROM (
        SELECT id AS parent_id FROM hou_permission_menu
        WHERE perms = 'shell:setting:permission:role'
      ) parent_lookup
    ),
    type = 'A', paths = '', component = '', selected = '', params = '',
    is_cache = 0, is_show = 1, is_disable = 0
WHERE perms IN (
  'shell:setting:permission:role:add',
  'shell:setting:permission:role:edit',
  'shell:setting:permission:role:delete',
  'shell:setting:permission:role:assign'
);

UPDATE hou_permission_menu
SET pid = (
      SELECT parent_id FROM (
        SELECT id AS parent_id FROM hou_permission_menu
        WHERE perms = 'shell:setting:permission:user'
      ) parent_lookup
    ),
    type = 'A', paths = '', component = '', selected = '', params = '',
    is_cache = 0, is_show = 1, is_disable = 0
WHERE perms IN (
  'shell:setting:permission:user:add',
  'shell:setting:permission:user:edit',
  'shell:setting:permission:user:delete',
  'shell:setting:permission:user:reset',
  'shell:setting:permission:user:assign',
  'shell:setting:permission:user:enable',
  'shell:setting:permission:user:disable'
);

UPDATE hou_permission_menu
SET pid = (
      SELECT parent_id FROM (
        SELECT id AS parent_id FROM hou_permission_menu
        WHERE perms = 'shell:setting:permission'
      ) parent_lookup
    ),
    type = 'A', name = '清理权限缓存', icon = 'Refresh', sort = 99,
    paths = '', component = '', selected = '', params = '',
    is_cache = 0, is_show = 1, is_disable = 0
WHERE perms = 'shell:setting:permission:cache:clear';

-- 管理员按角色名称识别，不依赖 role_id=1；普通角色不会被自动授权。
INSERT INTO hou_permission_role_menu (role_id, menu_id)
SELECT r.id, menu.id
FROM hou_permission_role r
JOIN hou_permission_menu menu
  ON (menu.perms = 'shell:setting:permission'
      OR menu.perms LIKE 'shell:setting:permission:%')
LEFT JOIN hou_permission_role_menu role_menu
  ON role_menu.role_id = r.id AND role_menu.menu_id = menu.id
WHERE r.delete_time IS NULL
  AND LOWER(TRIM(r.name)) IN ('admin', 'super_admin', '管理员', '超级管理员')
  AND role_menu.role_id IS NULL;

-- 迁移后核查：以下结果应依次为 21 个核心权限码、0 个重复权限码、0 个错误父子关系。
SELECT perms, type, pid, name, paths, component
FROM hou_permission_menu
WHERE perms IN (
  'shell:setting:permission',
  'shell:setting:permission:menu',
  'shell:setting:permission:menu:add',
  'shell:setting:permission:menu:edit',
  'shell:setting:permission:menu:delete',
  'shell:setting:permission:menu:enable',
  'shell:setting:permission:menu:disable',
  'shell:setting:permission:role',
  'shell:setting:permission:role:add',
  'shell:setting:permission:role:edit',
  'shell:setting:permission:role:delete',
  'shell:setting:permission:role:assign',
  'shell:setting:permission:user',
  'shell:setting:permission:user:add',
  'shell:setting:permission:user:edit',
  'shell:setting:permission:user:delete',
  'shell:setting:permission:user:reset',
  'shell:setting:permission:user:assign',
  'shell:setting:permission:user:enable',
  'shell:setting:permission:user:disable',
  'shell:setting:permission:cache:clear'
)
ORDER BY perms;

SELECT perms, COUNT(*) AS duplicate_count
FROM hou_permission_menu
WHERE perms = 'shell:setting:permission'
   OR perms LIKE 'shell:setting:permission:%'
GROUP BY perms
HAVING COUNT(*) > 1;

SELECT child.perms, child.pid, parent.perms AS actual_parent
FROM hou_permission_menu child
LEFT JOIN hou_permission_menu parent ON parent.id = child.pid
WHERE child.perms = 'shell:setting:permission:user'
   OR child.perms LIKE 'shell:setting:permission:user:%'
ORDER BY child.perms;

SELECT r.id AS role_id, r.name AS role_name, COUNT(DISTINCT menu.id) AS permission_count
FROM hou_permission_role r
JOIN hou_permission_role_menu role_menu ON role_menu.role_id = r.id
JOIN hou_permission_menu menu ON menu.id = role_menu.menu_id
WHERE r.delete_time IS NULL
  AND LOWER(TRIM(r.name)) IN ('admin', 'super_admin', '管理员', '超级管理员')
  AND (menu.perms = 'shell:setting:permission'
       OR menu.perms LIKE 'shell:setting:permission:%')
GROUP BY r.id, r.name;
