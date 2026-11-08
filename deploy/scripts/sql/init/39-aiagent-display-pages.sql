-- =====================================================================
-- AI 智能体：PC 商城 / H5 的展示位置与直达路径（2026-10-29）
--
-- 需求：后台智能体卡片要能一眼看到「这个能力在 PC 站和手机端分别出现在哪」，
--       并且点一下就能打开那个页面看真实效果，而不是只写一句「商品管理 → AI 生成文案」。
--
-- 口径：
--   pc_page / h5_page  人话描述（在哪个页面哪个位置），留空 = 该端还没接入
--   pc_path / h5_path  可直接打开的路径，留空 = 没有可跳转的页面
--                      以 http 开头则按绝对地址处理，否则按站点根拼接
--                      H5 是 uni-app hash 路由，必须是 /h5/#/pages/xxx 形式
--
-- 幂等：加列用 information_schema 判断；回填按 code UPDATE，重复执行结果一致。
-- =====================================================================

-- ---------- 1. 加列（幂等） ----------
SET @ddl = IF(
  (SELECT COUNT(*) FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'hou_ai_agent' AND COLUMN_NAME = 'pc_page') > 0,
  'SELECT 1',
  "ALTER TABLE `hou_ai_agent` ADD COLUMN `pc_page` varchar(255) NOT NULL DEFAULT '' COMMENT 'PC 商城展示位置（人话描述）' AFTER `usage_page`"
);
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @ddl = IF(
  (SELECT COUNT(*) FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'hou_ai_agent' AND COLUMN_NAME = 'pc_path') > 0,
  'SELECT 1',
  "ALTER TABLE `hou_ai_agent` ADD COLUMN `pc_path` varchar(255) NOT NULL DEFAULT '' COMMENT 'PC 商城访问路径，后台一键打开' AFTER `pc_page`"
);
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @ddl = IF(
  (SELECT COUNT(*) FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'hou_ai_agent' AND COLUMN_NAME = 'h5_page') > 0,
  'SELECT 1',
  "ALTER TABLE `hou_ai_agent` ADD COLUMN `h5_page` varchar(255) NOT NULL DEFAULT '' COMMENT 'H5 / 小程序展示位置（人话描述）' AFTER `pc_path`"
);
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @ddl = IF(
  (SELECT COUNT(*) FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'hou_ai_agent' AND COLUMN_NAME = 'h5_path') > 0,
  'SELECT 1',
  "ALTER TABLE `hou_ai_agent` ADD COLUMN `h5_path` varchar(255) NOT NULL DEFAULT '' COMMENT 'H5 访问路径（uni-app hash 路由），后台一键打开' AFTER `h5_page`"
);
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- ---------- 2. 回填已真实接入前台的三个智能体 ----------
-- 只登记「前台代码里确实调用了这个智能体、页面能打开」的位置；
-- 其余智能体保持空值，后台卡片显示「还没接入」——这本身就是要传达的事实，不要编造入口。

-- 2.1 商品智能推荐：PC 商品详情（ProductDetail.vue）+ H5 商品详情（descrption.vue）
--     跳转落到商品列表：详情页需要具体商品 id，用不存在的 id 打开只会看到报错页，没有排错价值。
UPDATE `hou_ai_agent`
   SET `pc_page` = '商品详情页 → 「猜你喜欢」推荐位（重排推荐顺序）',
       `pc_path` = '/pc/product/list',
       `h5_page` = '商品详情页 → 「猜你喜欢」推荐位（重排推荐顺序）',
       `h5_path` = '/h5/#/pages/productlist/productlist',
       `update_time` = NOW()
 WHERE `code` = 'goods_recommend';

-- 2.2 个性化首页推荐：PC 首页（index.vue 楼层副标题）+ H5 首页
UPDATE `hou_ai_agent`
   SET `pc_page` = '首页 → 「精选商品」楼层副标题',
       `pc_path` = '/pc/',
       `h5_page` = '首页 → 「精选商品」楼层副标题',
       `h5_path` = '/h5/#/pages/index/index',
       `update_time` = NOW()
 WHERE `code` = 'home_personal';

-- 2.3 用户意图识别：PC AI 助手页（ai/assistant.vue）+ H5 AI 搜索
UPDATE `hou_ai_agent`
   SET `pc_page` = 'AI 助手页 → 对话回复',
       `pc_path` = '/pc/ai/assistant',
       `h5_page` = 'AI 搜索 → 搜索结果页的智能答复',
       `h5_path` = '/h5/#/aisearch/index',
       `update_time` = NOW()
 WHERE `code` = 'intent_recognize';

-- ---------- 验收 ----------
-- SELECT code, name, terminal, pc_page, pc_path, h5_page, h5_path
--   FROM hou_ai_agent
--  WHERE pc_page <> '' OR h5_page <> ''
--  ORDER BY code;
-- 期望 3 行：goods_recommend / home_personal / intent_recognize

-- ---------- 回滚 ----------
-- ALTER TABLE hou_ai_agent DROP COLUMN pc_page, DROP COLUMN pc_path, DROP COLUMN h5_page, DROP COLUMN h5_path;
