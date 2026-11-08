-- 2026-10-30 半成品 embedded 付费商品下架（版本 OFFLINE+无制品+前端资产未构建，购买即装不了=收款不发货）：
-- mall-points 积分商城 / mall-live 直播带货 / mall-distribution 分销推广 / mall-rebate 返利结算
-- status 2(在架)→3(未上架，同 mall-activity 等半成品先例)；待产品化完成（前端资产分发链路+制品上传）后重新上架。
UPDATE hou_market_plugin SET status = 3, update_time = NOW() WHERE plugin_code IN ('mall-points','mall-live','mall-distribution','mall-rebate') AND status = 2;
SELECT plugin_code, name, status FROM hou_market_plugin WHERE plugin_code IN ('mall-points','mall-live','mall-distribution','mall-rebate');
