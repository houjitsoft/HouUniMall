-- =====================================================================
-- goods · ERP 物理仓同步幂等日志
--
-- 修复缺陷 D12：ERP 出库不通知商城 → 线上超卖
--
-- 幂等设计：
--   erp_flow_no 作为幂等键（行级会拼 #skuId），唯一索引 + INSERT IGNORE。
--   重复调用（网络重试 / 补偿 Job / 人工重推）返回 0，不重复加减库存。
--
-- 由 goods 服务消费，故建在 goods 的初始化脚本里。
-- =====================================================================

CREATE TABLE IF NOT EXISTS `hou_mall_stock_erp_sync` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `erp_flow_no` varchar(80) NOT NULL DEFAULT '' COMMENT '幂等键：ERP 流水号#skuId',
  `doc_no` varchar(50) NOT NULL DEFAULT '' COMMENT 'ERP 单据号',
  `doc_type` varchar(32) NOT NULL DEFAULT '' COMMENT '单据类型',
  `warehouse_id` bigint NOT NULL DEFAULT 0 COMMENT '商城仓库ID',
  `sku_id` bigint NOT NULL DEFAULT 0 COMMENT 'SKU ID',
  `quantity` int NOT NULL DEFAULT 0 COMMENT '同步数量',
  `direction` tinyint NOT NULL DEFAULT 0 COMMENT '方向:1=出库,-1=入库',
  `cost` decimal(15,2) DEFAULT NULL COMMENT '成本价',
  `batch_no` varchar(50) NOT NULL DEFAULT '' COMMENT '批次号',
  `occur_time` datetime DEFAULT NULL COMMENT '业务发生时间',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uk_flow_no` (`erp_flow_no`),
  KEY `idx_doc` (`doc_no`),
  KEY `idx_sku_time` (`sku_id`, `create_time`),
  KEY `idx_warehouse` (`warehouse_id`, `create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='ERP 物理仓同步幂等日志';

-- ---------------------------------------------------------------------
-- 迁移后自检
-- ---------------------------------------------------------------------
SELECT 'VERIFY_erp_sync_table' AS check_name, COUNT(*) AS should_be_1
  FROM information_schema.tables
 WHERE table_schema = DATABASE() AND table_name = 'hou_mall_stock_erp_sync';

-- 幂等键唯一性必须成立，否则重复同步会重复加减库存
SELECT 'VERIFY_unique_key' AS check_name, COUNT(*) AS should_be_1
  FROM information_schema.statistics
 WHERE table_schema = DATABASE() AND table_name = 'hou_mall_stock_erp_sync' AND index_name = 'uk_flow_no';