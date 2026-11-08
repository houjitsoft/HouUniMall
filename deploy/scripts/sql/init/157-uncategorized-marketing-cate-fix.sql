-- 2026-11-03 未分类营销插件归位
-- 红包 / 轮播图 / 活动管理中枢 / 营销总览 / 抽奖转盘 / 直播带货(cms-live)
-- 以及同族的 userms-points-mall：plugin.yaml 旧文案「营销与客户」与权威分类名
-- 「营销 / 运营」「客户 / 营销」不同名，cate_id 一直为 0 → 顶栏与插件中心落「未分类」。
-- 同族已装插件（拼团 / 秒杀 / mall-live / mall-points）均在「营销 / 运营」，本批对齐该桶。
-- 按分类名定位，不写死分类 ID。幂等。
SET NAMES utf8mb4;

SET @mkt_ops := (
  SELECT id FROM hou_addon_cate
   WHERE name IN ('营销 / 运营', '营销与客户')
   ORDER BY CASE name WHEN '营销 / 运营' THEN 0 ELSE 1 END, sort, id
   LIMIT 1
);

-- 若站点还没有「营销 / 运营」，再回落「客户 / 营销」
SET @mkt_ops := IFNULL(@mkt_ops, (
  SELECT id FROM hou_addon_cate
   WHERE name IN ('客户 / 营销')
   ORDER BY sort, id LIMIT 1
));

UPDATE hou_addon_plugin
   SET cate_id=@mkt_ops,
       category=COALESCE((SELECT name FROM hou_addon_cate WHERE id=@mkt_ops), '营销 / 运营'),
       update_time=NOW()
 WHERE deleted=0
   AND @mkt_ops IS NOT NULL
   AND plugin_code IN (
        'userms-redpacket','userms-lucky-draw','userms-points-mall',
        'cms-carousel','cms-live',
        'order-activity-hub','order-marketing'
   )
   AND COALESCE(cate_id,0) <> @mkt_ops;

UPDATE hou_market_plugin
   SET cate_id=@mkt_ops,
       category=COALESCE((SELECT name FROM hou_addon_cate WHERE id=@mkt_ops), '营销 / 运营'),
       update_time=NOW()
 WHERE deleted=0
   AND @mkt_ops IS NOT NULL
   AND plugin_code IN (
        'userms-redpacket','userms-lucky-draw','userms-points-mall',
        'cms-carousel','cms-live',
        'order-activity-hub','order-marketing'
   )
   AND COALESCE(cate_id,0) <> @mkt_ops;

UPDATE hou_permission_menu
   SET cate_id=@mkt_ops, update_time=UNIX_TIMESTAMP()
 WHERE @mkt_ops IS NOT NULL
   AND plugin_code IN (
        'userms-redpacket','userms-lucky-draw','userms-points-mall',
        'cms-carousel','cms-live',
        'order-activity-hub','order-marketing'
   )
   AND COALESCE(cate_id,0) <> @mkt_ops;
