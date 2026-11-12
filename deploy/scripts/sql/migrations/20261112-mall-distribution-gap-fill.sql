-- 2026-11-12 分销推广缺口补齐：余额自动到账开关列
-- 幂等：列已存在则跳过

SET @db := DATABASE();

SET @has_auto_payout := (
  SELECT COUNT(1) FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = @db
    AND TABLE_NAME = 'hou_mall_distribution_setting'
    AND COLUMN_NAME = 'auto_payout_balance'
);
SET @sql := IF(@has_auto_payout = 0,
  'ALTER TABLE hou_mall_distribution_setting ADD COLUMN auto_payout_balance tinyint NOT NULL DEFAULT 1 COMMENT ''余额提现自动到账：1=审核通过后余额入账并完结 0=人工已打款'' AFTER auto_upgrade',
  'SELECT ''auto_payout_balance already exists'' AS info');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

UPDATE hou_mall_distribution_setting
SET auto_payout_balance = 1
WHERE auto_payout_balance IS NULL;

SELECT id, shop_id, enabled, auto_upgrade, auto_payout_balance
FROM hou_mall_distribution_setting
LIMIT 5;
