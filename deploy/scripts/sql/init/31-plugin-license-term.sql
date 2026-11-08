-- =====================================================================
-- 授权期限列：hou_market_plugin_license.term_days（幂等）
--
-- 背景（审计 2026-10-27 P2-2）：价格套餐上的 valid_days 在下单时被静默丢弃——
--   hou_market_plugin_order.valid_days 对 PAID 单恒写 NULL，发证时又固定走
--   「首购 +12 个月维护期」的官方政策。结果是「买 30 天版」和「买永久版」拿到
--   完全一样的授权，资损方向对平台不利，且对账时无法解释价差。
--
-- 处理：把「这一笔购买折算的服务期限」落到授权行上，累计维护期由
--   OfficialMaintenancePolicy.recalculate() 按每笔 grant 的 term_days 累加
--   （term_days 为空时才回退到 12 个月），既保留原有政策，又能表达短周期套餐。
--
-- 幂等：按 information_schema 判定「表存在且列不存在」才 ALTER。
-- 依赖：30-commercial-delivery.sql（hou_market_plugin_license 由它建表）
-- =====================================================================

SET NAMES utf8mb4;

SET @term_db = DATABASE();

SET @lic_exists = (SELECT COUNT(*) FROM information_schema.tables
                   WHERE table_schema = @term_db AND table_name = 'hou_market_plugin_license');
SET @lic_has_term = (SELECT COUNT(*) FROM information_schema.columns
                     WHERE table_schema = @term_db AND table_name = 'hou_market_plugin_license'
                       AND column_name = 'term_days');
SET @term_sql = IF(@lic_exists = 1 AND @lic_has_term = 0,
  'ALTER TABLE `hou_market_plugin_license` ADD COLUMN `term_days` INT NULL COMMENT ''该笔购买折算的服务期限（天）；NULL=按官方政策 12 个月''',
  'SELECT 1');
PREPARE term_stmt FROM @term_sql; EXECUTE term_stmt; DEALLOCATE PREPARE term_stmt;

-- 说明：订单侧的期限不需要新列——hou_market_plugin_order.valid_days 已存在，
-- 本次只是让它对 PAID 单也真实写入（此前恒写 NULL），发证时据此落 term_days。
