-- 2026-11-03 合并重复的财务办公分类
-- 保留「财务 / 办公」；回收「办公 / 财务」「办公与财务」等同义空分类。
-- 幂等：按名称定位，不写死被删分类的 ID。
SET NAMES utf8mb4;

CREATE TABLE IF NOT EXISTS hou_addon_cate_bak_merge_fin_20261103 AS SELECT * FROM hou_addon_cate;

SET @keep := (
  SELECT id FROM hou_addon_cate
   WHERE name='财务 / 办公'
   ORDER BY sort, id LIMIT 1
);

-- 没有「财务 / 办公」时，把同义分类里最早的一行改成权威名
SET @alias := (
  SELECT id FROM hou_addon_cate
   WHERE name IN ('办公 / 财务', '办公与财务')
   ORDER BY sort, id LIMIT 1
);

UPDATE hou_addon_cate
   SET name='财务 / 办公', update_time=UNIX_TIMESTAMP()
 WHERE @keep IS NULL AND @alias IS NOT NULL AND id=@alias;

SET @keep := IFNULL(@keep, (
  SELECT id FROM hou_addon_cate WHERE name='财务 / 办公' ORDER BY sort, id LIMIT 1
));

INSERT INTO hou_addon_cate (name, sort, create_time, update_time)
SELECT '财务 / 办公', 3, UNIX_TIMESTAMP(), UNIX_TIMESTAMP()
 WHERE @keep IS NULL
   AND NOT EXISTS (SELECT 1 FROM hou_addon_cate WHERE name='财务 / 办公');

SET @keep := IFNULL(@keep, (
  SELECT id FROM hou_addon_cate WHERE name='财务 / 办公' ORDER BY sort, id LIMIT 1
));

-- 把挂在同义分类上的目录 / 市场 / 菜单迁到保留分类
UPDATE hou_addon_plugin p
  JOIN hou_addon_cate c ON c.id=p.cate_id
   SET p.cate_id=@keep, p.category='财务 / 办公', p.update_time=NOW()
 WHERE @keep IS NOT NULL
   AND p.deleted=0
   AND c.name IN ('办公 / 财务', '办公与财务')
   AND p.cate_id <> @keep;

UPDATE hou_market_plugin m
  JOIN hou_addon_cate c ON c.id=m.cate_id
   SET m.cate_id=@keep, m.category='财务 / 办公', m.update_time=NOW()
 WHERE @keep IS NOT NULL
   AND m.deleted=0
   AND c.name IN ('办公 / 财务', '办公与财务')
   AND m.cate_id <> @keep;

UPDATE hou_permission_menu menu
  JOIN hou_addon_cate c ON c.id=menu.cate_id
   SET menu.cate_id=@keep, menu.update_time=UNIX_TIMESTAMP()
 WHERE @keep IS NOT NULL
   AND c.name IN ('办公 / 财务', '办公与财务')
   AND menu.cate_id <> @keep;

-- 文案仍写旧名、但 cate_id 已是保留行的，一并改成权威名
UPDATE hou_addon_plugin
   SET category='财务 / 办公', update_time=NOW()
 WHERE deleted=0 AND cate_id=@keep
   AND category IN ('办公 / 财务', '办公与财务', '财务');

UPDATE hou_market_plugin
   SET category='财务 / 办公', update_time=NOW()
 WHERE deleted=0 AND cate_id=@keep
   AND category IN ('办公 / 财务', '办公与财务', '财务');

-- 删除同义空壳分类（保留行除外）
DELETE FROM hou_addon_cate
 WHERE name IN ('办公 / 财务', '办公与财务')
   AND (@keep IS NULL OR id <> @keep);
