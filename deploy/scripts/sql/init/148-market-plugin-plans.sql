SET NAMES utf8mb4;

-- =============================================================================
-- 付费插件标准价格套餐（线上自助购买闭环）
--
-- 背景（2026-10-29）：市场卡片的付费插件此前只能「激活授权」，用户要线下找平台
-- 拿授权码。改成「购买」后必须能下单，而下单要求该插件在 hou_market_plan 视角
-- 有一个可用的价格套餐（PluginPayChannelRouter#plansOf）。生产实测 PLAN 行 0 条，
-- 于是 checkout 对所有付费插件都回「暂不可配置有效价格，暂不可购买」。
--
-- 口径：
--   1) 每个已上架、PAID、已有标价的插件生成一个 standard 套餐；
--   2) price 取 hou_market_plugin.price_cents / 100（元，两位小数），与市场标价一致，
--      不另造价格，避免「卡片 ¥199、收银台 ¥299」；
--   3) valid-days = 365（首次购买含 12 个月维护期）；
--   4) 幂等：先按 scope=PLAN 清理本脚本口径的行再插入，可重复执行。
-- =============================================================================

DELETE FROM `hou_market_pay_config` WHERE `scope` = 'PLAN';

INSERT INTO `hou_market_pay_config`
  (`scope`, `scope_key`, `sub_key`, `config_key`, `config_value`, `value_type`, `secret`, `remark`)
SELECT 'PLAN', p.`plugin_code`, 'standard', 'name', '标准版（含 12 个月维护）', 'STRING', 0,
       '2026-10-29 线上自助购买：按市场标价生成'
FROM `hou_market_plugin` p
WHERE p.`deleted` = 0 AND p.`license_type` = 'PAID' AND COALESCE(p.`price_cents`, 0) > 0;

INSERT INTO `hou_market_pay_config`
  (`scope`, `scope_key`, `sub_key`, `config_key`, `config_value`, `value_type`, `secret`, `remark`)
SELECT 'PLAN', p.`plugin_code`, 'standard', 'price',
       CAST(p.`price_cents` / 100 AS DECIMAL(10, 2)), 'DECIMAL', 0,
       '与 hou_market_plugin.price_cents 同源'
FROM `hou_market_plugin` p
WHERE p.`deleted` = 0 AND p.`license_type` = 'PAID' AND COALESCE(p.`price_cents`, 0) > 0;

INSERT INTO `hou_market_pay_config`
  (`scope`, `scope_key`, `sub_key`, `config_key`, `config_value`, `value_type`, `secret`, `remark`)
SELECT 'PLAN', p.`plugin_code`, 'standard', 'valid-days', '365', 'INT', 0, '12 个月维护期'
FROM `hou_market_plugin` p
WHERE p.`deleted` = 0 AND p.`license_type` = 'PAID' AND COALESCE(p.`price_cents`, 0) > 0;

INSERT INTO `hou_market_pay_config`
  (`scope`, `scope_key`, `sub_key`, `config_key`, `config_value`, `value_type`, `secret`, `remark`)
SELECT 'PLAN', p.`plugin_code`, 'standard', 'license-type', 'PAID', 'STRING', 0, NULL
FROM `hou_market_plugin` p
WHERE p.`deleted` = 0 AND p.`license_type` = 'PAID' AND COALESCE(p.`price_cents`, 0) > 0;

INSERT INTO `hou_market_pay_config`
  (`scope`, `scope_key`, `sub_key`, `config_key`, `config_value`, `value_type`, `secret`, `remark`)
SELECT 'PLAN', p.`plugin_code`, 'standard', 'enabled', 'true', 'BOOLEAN', 0, NULL
FROM `hou_market_plugin` p
WHERE p.`deleted` = 0 AND p.`license_type` = 'PAID' AND COALESCE(p.`price_cents`, 0) > 0;
