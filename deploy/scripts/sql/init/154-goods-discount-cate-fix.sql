-- 154-goods-discount-cate-fix.sql
-- 修复：商品折扣插件在「插件中心 → 插件管理」落「未分类」，与市场卡片「客户 / 营销」不一致。
--
-- 根因链：
--   1) AddonPluginSyncService 对账只写 hou_addon_plugin.category 文本，从不写 cate_id；
--   2) plugin.yaml 旧分类文案「营销与客户」与权威分类名 hou_addon_cate(id=1)「客户 / 营销」不同名，
--      菜单同步 PluginMenuSyncPort.resolveCategoryId 在 cate_id=0 时按名字匹配 hou_addon_cate.name，
--      匹配落空 → 菜单 cate_id=0；
--   3) 展示层 PluginCenterService.resolveCategory / resolveCateId 依次读 addon.cate_id(0)→菜单 cate_id(0)
--      →目录文本同名匹配(失败)，最终归入「未分类」。
--   市场卡片因 hou_market_plugin 硬编码 cate_id=1 才显示正确。
--
-- 修法：与 hou_market_plugin(plugin_code='goods-discount').cate_id=1 对齐，直接写权威外键 cate_id=1。
--   upsertFromManifest 的 ON DUPLICATE 逻辑对 cate_id>0 保留不覆盖，并把 category 回写成字典名，
--   故本脚本与后续对账互相巩固，不会被冲回 0。
--
-- 幂等：仅当当前不是 1 时更新。hou_addon_plugin.update_time 为 datetime(NOW())，
--       hou_permission_menu.update_time 为 int 时间戳(UNIX_TIMESTAMP())。

UPDATE hou_addon_plugin
   SET cate_id = 1,
       category = COALESCE((SELECT name FROM hou_addon_cate WHERE id = 1), category),
       update_time = NOW()
 WHERE plugin_code = 'goods-discount'
   AND deleted = 0
   AND COALESCE(cate_id, 0) <> 1;

-- 插件位一级菜单（display_pos='plugin'，与顶栏「插件应用」同源）及其子项的分类绑定
UPDATE hou_permission_menu
   SET cate_id = 1,
       update_time = UNIX_TIMESTAMP()
 WHERE plugin_code = 'goods-discount'
   AND COALESCE(cate_id, 0) <> 1;
