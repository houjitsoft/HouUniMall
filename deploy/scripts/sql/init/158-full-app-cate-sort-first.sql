-- 2026-11-03 「完整应用」排到分类第一位（右上角 / 插件中心 / 市场同源 sort）
-- AddonCateMapper.selectAll：ORDER BY sort ASC, id ASC
SET NAMES utf8mb4;

UPDATE hou_addon_cate
   SET sort=0, update_time=UNIX_TIMESTAMP()
 WHERE name='完整应用'
   AND COALESCE(sort, 0) <> 0;
