-- 商城设置(id=60011) 子项扁平化到系统设置(id=18)
-- 规则：与系统设置下已有同名菜单冲突时，保留外面的；商城侧同名壳删除，其子项改挂到外层父级。
-- 2026-10-08

-- 0. 备份
CREATE TABLE IF NOT EXISTS hou_permission_menu_bak_mall_setting_20261008 AS
SELECT * FROM hou_permission_menu
WHERE id = 60011
   OR pid = 60011
   OR pid IN (60142, 60143, 60144, 60145, 60146, 60147);

CREATE TABLE IF NOT EXISTS hou_permission_role_menu_bak_mall_setting_20261008 AS
SELECT rm.*
FROM hou_permission_role_menu rm
WHERE rm.menu_id IN (
  SELECT id FROM hou_permission_menu_bak_mall_setting_20261008
);

-- 1. 同名「网站设置」：保留外层 264，商城侧子项改挂到 264，删除壳 60142
UPDATE hou_permission_menu
SET pid = 264,
    update_time = UNIX_TIMESTAMP()
WHERE pid = 60142
  AND id IN (60211, 60212, 60213);

UPDATE hou_permission_menu
SET type = 'M',
    update_time = UNIX_TIMESTAMP()
WHERE id = 264
  AND type <> 'M';

DELETE FROM hou_permission_role_menu WHERE menu_id = 60142;
DELETE FROM hou_permission_menu WHERE id = 60142;

-- 2. 同名「系统管理」：保留外层 10004，商城侧子项改挂到 10004，删除壳 60143
UPDATE hou_permission_menu
SET pid = 10004,
    update_time = UNIX_TIMESTAMP()
WHERE pid = 60143
  AND id IN (60214, 60215, 60216, 60217);

UPDATE hou_permission_menu
SET type = 'M',
    update_time = UNIX_TIMESTAMP()
WHERE id = 10004
  AND type <> 'M';

DELETE FROM hou_permission_role_menu WHERE menu_id = 60143;
DELETE FROM hou_permission_menu WHERE id = 60143;

-- 3. 商城设置内部同名「支付设置」：保留带完整子树的 60145，隐藏旧桥接行 2061
UPDATE hou_permission_menu
SET is_show = 0,
    update_time = UNIX_TIMESTAMP()
WHERE id = 2061
  AND pid = 60011
  AND name = '支付设置';

-- 4. 其余子项提升到系统设置(id=18)，保持相对顺序
UPDATE hou_permission_menu SET pid = 18, sort = 980, update_time = UNIX_TIMESTAMP() WHERE id = 60137 AND pid = 60011;
UPDATE hou_permission_menu SET pid = 18, sort = 970, update_time = UNIX_TIMESTAMP() WHERE id = 60138 AND pid = 60011;
UPDATE hou_permission_menu SET pid = 18, sort = 960, update_time = UNIX_TIMESTAMP() WHERE id = 60139 AND pid = 60011;
UPDATE hou_permission_menu SET pid = 18, sort = 950, update_time = UNIX_TIMESTAMP() WHERE id = 60140 AND pid = 60011;
UPDATE hou_permission_menu SET pid = 18, sort = 940, update_time = UNIX_TIMESTAMP() WHERE id = 60141 AND pid = 60011;
UPDATE hou_permission_menu SET pid = 18, sort = 930, update_time = UNIX_TIMESTAMP() WHERE id = 60144 AND pid = 60011;
UPDATE hou_permission_menu SET pid = 18, sort = 920, update_time = UNIX_TIMESTAMP() WHERE id = 60145 AND pid = 60011;
UPDATE hou_permission_menu SET pid = 18, sort = 910, update_time = UNIX_TIMESTAMP() WHERE id = 60146 AND pid = 60011;
UPDATE hou_permission_menu SET pid = 18, sort = 900, update_time = UNIX_TIMESTAMP() WHERE id = 60147 AND pid = 60011;
UPDATE hou_permission_menu SET pid = 18, sort = 890, update_time = UNIX_TIMESTAMP() WHERE id = 2064 AND pid = 60011;
UPDATE hou_permission_menu SET pid = 18, sort = 880, update_time = UNIX_TIMESTAMP() WHERE id = 2063 AND pid = 60011;
UPDATE hou_permission_menu SET pid = 18, sort = 870, update_time = UNIX_TIMESTAMP() WHERE id = 2062 AND pid = 60011;
UPDATE hou_permission_menu SET pid = 18, sort = 860, update_time = UNIX_TIMESTAMP() WHERE id = 2060 AND pid = 60011;

-- 5. 已隐藏的旧支付设置桥接行也挂到系统设置，避免残留在空壳下
UPDATE hou_permission_menu
SET pid = 18,
    sort = 1,
    update_time = UNIX_TIMESTAMP()
WHERE id = 2061
  AND pid = 60011;

-- 6. 删除空壳「商城设置」
DELETE FROM hou_permission_role_menu WHERE menu_id = 60011;
DELETE FROM hou_permission_menu WHERE id = 60011;
