-- 160-mall-distribution-settle-accuracy.sql
-- 分销结算准确性：全新安装补齐「订单事件挂钩 + 商品级佣金 / 退款等比冲减」所需结构
--
-- 背景：这两批 DDL 原先只存在于
--   deploy/scripts/sql/init/36-mall-distribution-order-hook.sql      （未登记进 order.txt）
--   deploy/scripts/sql/migrations/20261102_distribution_settle_accuracy.sql（migrations 目录，init 链不读）
-- ① 真库是人工执行迁移补上的，但全新安装的库会缺 goods_snapshot / refunded_amount /
-- original_commission_amount 三列及 commission_amount 默认值，结算代码直接 Unknown column，
-- 或者下单登记分销订单时因无默认值整条 INSERT 失败。
-- 本脚本是 [schema] 段唯一入口；上面两个历史文件保留作「已有库升级」路径，勿再重复登记。
--
-- 幂等：列 / 索引已存在则跳过，可在已执行过迁移的库（含评估站、演示站）上安全重跑。
-- 依赖：33 / 34 / 35 已建出 hou_mall_distribution_order 与 hou_mall_commission_record。

-- 1) commission_amount 默认值：下单登记时佣金尚未计算，INSERT 不带该列会报
--    Field 'commission_amount' doesn't have a default value，整条分销订单登记失败。
ALTER TABLE hou_mall_distribution_order
    MODIFY COLUMN commission_amount decimal(12,2) NOT NULL DEFAULT '0.00' COMMENT '佣金金额（订单完成结算后回填）';

-- 2) 结算按 order_status=3 扫描，补索引避免全表扫
SET @ddl = IF(
    (SELECT COUNT(*) FROM information_schema.STATISTICS
      WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'hou_mall_distribution_order'
        AND INDEX_NAME = 'idx_hmdo_order_status') = 0,
    'CREATE INDEX idx_hmdo_order_status ON hou_mall_distribution_order (order_status)',
    'SELECT 1');
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 3) 下单商品快照：按行解析分销商品覆盖 / 佣金规则 / 等级比例
SET @ddl = IF(
    (SELECT COUNT(*) FROM information_schema.COLUMNS
      WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'hou_mall_distribution_order'
        AND COLUMN_NAME = 'goods_snapshot') = 0,
    'ALTER TABLE hou_mall_distribution_order ADD COLUMN goods_snapshot text NULL COMMENT ''商品快照 JSON [{goodsId,categoryId,goodsName,amount}]'' AFTER order_amount',
    'SELECT 1');
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 4) 累计已退款金额：部分退款时按 keepRatio 等比冲减待结算佣金
SET @ddl = IF(
    (SELECT COUNT(*) FROM information_schema.COLUMNS
      WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'hou_mall_distribution_order'
        AND COLUMN_NAME = 'refunded_amount') = 0,
    'ALTER TABLE hou_mall_distribution_order ADD COLUMN refunded_amount decimal(12,2) NOT NULL DEFAULT ''0.00'' COMMENT ''累计已退款金额（等比冲减佣金用）'' AFTER commission_amount',
    'SELECT 1');
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 5) 生成时原始佣金：退款冲减的基准（不随冲减变化，已结算佣金不追回）
SET @ddl = IF(
    (SELECT COUNT(*) FROM information_schema.COLUMNS
      WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'hou_mall_commission_record'
        AND COLUMN_NAME = 'original_commission_amount') = 0,
    'ALTER TABLE hou_mall_commission_record ADD COLUMN original_commission_amount decimal(12,2) NULL COMMENT ''生成时原始佣金（退款冲减基准）'' AFTER commission_amount',
    'SELECT 1');
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 6) 历史待结算记录回填基准（只补空值，可重复执行）
UPDATE hou_mall_commission_record
SET original_commission_amount = commission_amount
WHERE original_commission_amount IS NULL
  AND deleted = 0;
