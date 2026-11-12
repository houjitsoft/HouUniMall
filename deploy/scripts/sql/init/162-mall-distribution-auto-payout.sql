-- 161-mall-distribution-auto-payout.sql
-- 存量/新装幂等：补齐余额自动到账开关列（与 migrations/20261112-mall-distribution-gap-fill.sql 同口径）

SET @db := DATABASE();

SET @has_auto_payout := (
  SELECT COUNT(1) FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = @db
    AND TABLE_NAME = 'hou_mall_distribution_setting'
    AND COLUMN_NAME = 'auto_payout_balance'
);
SET @sql := IF(@has_auto_payout = 0,
  'ALTER TABLE hou_mall_distribution_setting ADD COLUMN auto_payout_balance tinyint NOT NULL DEFAULT 1 COMMENT ''余额提现自动到账：1=审核通过后余额入账并完结 0=人工已打款'' AFTER auto_upgrade',
  'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;
