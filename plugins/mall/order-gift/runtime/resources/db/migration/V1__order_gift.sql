-- =====================================================================
-- 满赠插件（order-gift）建表脚本
-- 宿主：order。表前缀 hou_plugin_order_gift_
-- 幂等：全部 CREATE TABLE IF NOT EXISTS。
-- 边界：只做「规则配置 + 可赠判定 + 赠出登记」，不改宿主订单结算字段；
--       是否把赠品加进订单由调用方（结算页 / 下单流程）决定。
-- =====================================================================

CREATE TABLE IF NOT EXISTS `hou_plugin_order_gift_rule` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '主键',
  `rule_name` varchar(100) NOT NULL COMMENT '规则名称',
  `scope_type` varchar(16) NOT NULL DEFAULT 'ALL' COMMENT '生效范围：ALL 全场 / CATEGORY 类目 / GOODS 指定商品',
  `scope_ids` text COMMENT '范围值：类目ID或商品ID，逗号分隔',
  `threshold_amount` decimal(12,2) NOT NULL DEFAULT '0.00' COMMENT '门槛金额（订单商品金额，含）',
  `gift_mode` varchar(16) NOT NULL DEFAULT 'FIXED' COMMENT 'FIXED 固定赠品 / CHOOSE 赠品池多选一',
  `max_gift_kinds` int NOT NULL DEFAULT '1' COMMENT 'CHOOSE 模式下每单最多可选几种赠品',
  `status` tinyint(1) NOT NULL DEFAULT '1' COMMENT '1 启用 0 停用',
  `priority` int NOT NULL DEFAULT '0' COMMENT '优先级，数值大者先命中',
  `start_time` datetime DEFAULT NULL COMMENT '生效开始，空=不限',
  `end_time` datetime DEFAULT NULL COMMENT '生效结束，空=不限',
  `remark` varchar(255) DEFAULT NULL COMMENT '备注',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_status` (`status`, `priority`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='满赠规则';

CREATE TABLE IF NOT EXISTS `hou_plugin_order_gift_item` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '主键',
  `rule_id` bigint unsigned NOT NULL COMMENT '所属规则',
  `goods_id` varchar(64) NOT NULL COMMENT '赠品商品 ID',
  `sku_id` varchar(64) DEFAULT NULL COMMENT '赠品 SKU ID，空=默认规格',
  `gift_title` varchar(200) NOT NULL COMMENT '赠品名称（冗余展示）',
  `gift_qty` int NOT NULL DEFAULT '1' COMMENT '每单赠送数量',
  `stock_total` int NOT NULL DEFAULT '0' COMMENT '赠品总库存，0=不限',
  `stock_used` int NOT NULL DEFAULT '0' COMMENT '已赠出数量',
  `sort` int NOT NULL DEFAULT '0' COMMENT '排序',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_rule` (`rule_id`, `sort`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='满赠赠品池';

CREATE TABLE IF NOT EXISTS `hou_plugin_order_gift_record` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '主键',
  `order_no` varchar(64) NOT NULL COMMENT '订单号',
  `user_id` varchar(50) NOT NULL COMMENT '会员 ID',
  `rule_id` bigint unsigned NOT NULL COMMENT '命中规则',
  `item_id` bigint unsigned NOT NULL COMMENT '赠品项',
  `goods_id` varchar(64) NOT NULL COMMENT '赠品商品 ID',
  `gift_title` varchar(200) NOT NULL COMMENT '赠品名称',
  `gift_qty` int NOT NULL DEFAULT '1' COMMENT '赠送数量',
  `status` tinyint(1) NOT NULL DEFAULT '1' COMMENT '1 已赠出 0 已撤销',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_order_item` (`order_no`, `item_id`),
  KEY `idx_user` (`user_id`),
  KEY `idx_rule` (`rule_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='满赠赠出记录';

INSERT INTO `hou_plugin_order_gift_rule` (`rule_name`, `scope_type`, `threshold_amount`, `gift_mode`, `max_gift_kinds`, `status`, `priority`, `remark`)
SELECT '满 200 送赠品示例', 'ALL', 200.00, 'FIXED', 1, 0, 0, '默认停用，配好赠品再启用'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `hou_plugin_order_gift_rule`);
