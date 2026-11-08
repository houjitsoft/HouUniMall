-- ================================================================
-- Mall商城模块数据库表结构SQL
-- 生成时间: 2026-01-05
-- 包含模块: 分销、返利、仓储、风控、店铺、首页共30张表
-- ================================================================

-- ================================================================
-- 一、分销模块 (Distribution Module) - 6张表
-- ================================================================

-- 1. 分销员表
DROP TABLE IF EXISTS `hou_mall_distributor`;
CREATE TABLE `hou_mall_distributor` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` BIGINT(20) DEFAULT NULL COMMENT '用户ID',
  `shop_id` BIGINT(20) DEFAULT NULL COMMENT '店铺ID',
  `distributor_no` VARCHAR(50) DEFAULT NULL COMMENT '分销员编号',
  `real_name` VARCHAR(50) DEFAULT NULL COMMENT '真实姓名',
  `phone` VARCHAR(20) DEFAULT NULL COMMENT '手机号',
  `level_id` BIGINT(20) DEFAULT NULL COMMENT '分销等级ID',
  `parent_id` BIGINT(20) DEFAULT NULL COMMENT '上级分销员ID',
  `total_commission` DECIMAL(10,2) DEFAULT 0.00 COMMENT '累计佣金',
  `withdrawn_commission` DECIMAL(10,2) DEFAULT 0.00 COMMENT '已提现佣金',
  `pending_commission` DECIMAL(10,2) DEFAULT 0.00 COMMENT '待结算佣金',
  `available_commission` DECIMAL(10,2) DEFAULT 0.00 COMMENT '可提现佣金',
  `order_count` INT(11) DEFAULT 0 COMMENT '推广订单数',
  `sub_count` INT(11) DEFAULT 0 COMMENT '下级分销员数量',
  `status` TINYINT(1) DEFAULT 1 COMMENT '状态 0-禁用 1-正常',
  `apply_remark` VARCHAR(500) DEFAULT NULL COMMENT '申请备注',
  `audit_remark` VARCHAR(500) DEFAULT NULL COMMENT '审核备注',
  `audit_time` DATETIME DEFAULT NULL COMMENT '审核时间',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` TINYINT(1) DEFAULT 0 COMMENT '是否删除 0-未删除 1-已删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_shop` (`user_id`, `shop_id`, `deleted`),
  KEY `idx_shop_id` (`shop_id`),
  KEY `idx_parent_id` (`parent_id`),
  KEY `idx_level_id` (`level_id`),
  KEY `idx_phone` (`phone`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='分销员表';

-- 2. 分销等级表
DROP TABLE IF EXISTS `hou_mall_distributor_level`;
CREATE TABLE `hou_mall_distributor_level` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `shop_id` BIGINT(20) DEFAULT NULL COMMENT '店铺ID',
  `name` VARCHAR(50) NOT NULL COMMENT '等级名称',
  `icon` VARCHAR(255) DEFAULT NULL COMMENT '等级图标',
  `sort` INT(11) DEFAULT 0 COMMENT '等级排序',
  `level1_rate` DECIMAL(5,2) DEFAULT 0.00 COMMENT '一级佣金比例(%)',
  `level2_rate` DECIMAL(5,2) DEFAULT 0.00 COMMENT '二级佣金比例(%)',
  `upgrade_amount` DECIMAL(10,2) DEFAULT NULL COMMENT '升级条件-累计销售额',
  `upgrade_order_count` INT(11) DEFAULT NULL COMMENT '升级条件-累计订单数',
  `upgrade_sub_count` INT(11) DEFAULT NULL COMMENT '升级条件-下级分销员数',
  `is_default` TINYINT(1) DEFAULT 0 COMMENT '是否默认等级 0-否 1-是',
  `status` TINYINT(1) DEFAULT 1 COMMENT '状态 0-禁用 1-启用',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` TINYINT(1) DEFAULT 0 COMMENT '是否删除 0-未删除 1-已删除',
  PRIMARY KEY (`id`),
  KEY `idx_shop_id` (`shop_id`),
  KEY `idx_sort` (`sort`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='分销等级表';

-- 3. 佣金规则表
DROP TABLE IF EXISTS `hou_mall_commission_rule`;
CREATE TABLE `hou_mall_commission_rule` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `shop_id` BIGINT(20) DEFAULT NULL COMMENT '店铺ID',
  `name` VARCHAR(100) NOT NULL COMMENT '规则名称',
  `type` TINYINT(1) NOT NULL COMMENT '规则类型 1-全店通用 2-指定分类 3-指定商品',
  `related_id` BIGINT(20) DEFAULT NULL COMMENT '关联ID(分类ID或商品ID)',
  `level1_rate` DECIMAL(5,2) DEFAULT NULL COMMENT '一级佣金比例(%)',
  `level2_rate` DECIMAL(5,2) DEFAULT NULL COMMENT '二级佣金比例(%)',
  `level1_amount` DECIMAL(10,2) DEFAULT NULL COMMENT '固定佣金金额(一级)',
  `level2_amount` DECIMAL(10,2) DEFAULT NULL COMMENT '固定佣金金额(二级)',
  `calc_type` TINYINT(1) DEFAULT 1 COMMENT '计算方式 1-按比例 2-固定金额',
  `priority` INT(11) DEFAULT 0 COMMENT '优先级(数字越大优先级越高)',
  `status` TINYINT(1) DEFAULT 1 COMMENT '状态 0-禁用 1-启用',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` TINYINT(1) DEFAULT 0 COMMENT '是否删除 0-未删除 1-已删除',
  PRIMARY KEY (`id`),
  KEY `idx_shop_id` (`shop_id`),
  KEY `idx_type_related` (`type`, `related_id`),
  KEY `idx_priority` (`priority`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='佣金规则表';

-- 4. 佣金记录表
DROP TABLE IF EXISTS `hou_mall_commission_record`;
CREATE TABLE `hou_mall_commission_record` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `record_no` VARCHAR(50) NOT NULL COMMENT '记录编号',
  `distributor_id` BIGINT(20) NOT NULL COMMENT '分销员ID',
  `shop_id` BIGINT(20) DEFAULT NULL COMMENT '店铺ID',
  `order_id` BIGINT(20) NOT NULL COMMENT '关联订单ID',
  `order_no` VARCHAR(50) NOT NULL COMMENT '关联订单号',
  `goods_name` VARCHAR(200) DEFAULT NULL COMMENT '商品名称',
  `order_amount` DECIMAL(10,2) DEFAULT 0.00 COMMENT '订单金额',
  `commission_amount` DECIMAL(10,2) DEFAULT 0.00 COMMENT '佣金金额',
  `commission_rate` DECIMAL(5,2) DEFAULT 0.00 COMMENT '佣金比例(%)',
  `level` TINYINT(1) DEFAULT 1 COMMENT '佣金层级 1-一级 2-二级',
  `source_distributor_id` BIGINT(20) DEFAULT NULL COMMENT '来源分销员ID(被推广的分销员)',
  `status` TINYINT(1) DEFAULT 0 COMMENT '状态 0-待结算 1-已结算 2-已取消',
  `settle_time` DATETIME DEFAULT NULL COMMENT '结算时间',
  `remark` VARCHAR(500) DEFAULT NULL COMMENT '备注',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` TINYINT(1) DEFAULT 0 COMMENT '是否删除 0-未删除 1-已删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_record_no` (`record_no`),
  KEY `idx_distributor_id` (`distributor_id`),
  KEY `idx_order_id` (`order_id`),
  KEY `idx_status` (`status`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='佣金记录表';

-- 5. 分销商品表
DROP TABLE IF EXISTS `hou_mall_distribution_goods`;
CREATE TABLE `hou_mall_distribution_goods` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `shop_id` BIGINT(20) DEFAULT NULL COMMENT '店铺ID',
  `goods_id` BIGINT(20) NOT NULL COMMENT '商品ID',
  `goods_name` VARCHAR(200) DEFAULT NULL COMMENT '商品名称',
  `commission_rate` DECIMAL(5,2) DEFAULT 0.00 COMMENT '佣金比例%',
  `status` TINYINT(1) DEFAULT 1 COMMENT '状态: 0-禁用 1-启用',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` TINYINT(1) DEFAULT 0 COMMENT '是否删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_shop_goods` (`shop_id`, `goods_id`, `deleted`),
  KEY `idx_goods_id` (`goods_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='分销商品表';

-- 6. 分销订单表
DROP TABLE IF EXISTS `hou_mall_distribution_order`;
CREATE TABLE `hou_mall_distribution_order` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `shop_id` BIGINT(20) DEFAULT NULL COMMENT '店铺ID',
  `order_id` BIGINT(20) NOT NULL COMMENT '订单ID',
  `order_no` VARCHAR(50) NOT NULL COMMENT '订单编号',
  `distributor_id` BIGINT(20) NOT NULL COMMENT '分销员ID',
  `buyer_id` BIGINT(20) NOT NULL COMMENT '购买用户ID',
  `order_amount` DECIMAL(10,2) DEFAULT 0.00 COMMENT '订单金额',
  `commission_amount` DECIMAL(10,2) DEFAULT 0.00 COMMENT '佣金金额',
  `order_status` TINYINT(1) DEFAULT 0 COMMENT '订单状态',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` TINYINT(1) DEFAULT 0 COMMENT '是否删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_order_distributor` (`order_id`, `distributor_id`, `deleted`),
  KEY `idx_distributor_id` (`distributor_id`),
  KEY `idx_buyer_id` (`buyer_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='分销订单表';

-- ================================================================
-- 二、返利模块 (Rebate Module) - 4张表
-- ================================================================

-- 7. 返利规则表
DROP TABLE IF EXISTS `hou_mall_rebate_rule`;
CREATE TABLE `hou_mall_rebate_rule` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `rule_name` VARCHAR(100) NOT NULL COMMENT '规则名称',
  `rule_code` VARCHAR(50) NOT NULL COMMENT '规则编码',
  `shop_id` BIGINT(20) DEFAULT NULL COMMENT '店铺ID',
  `rebate_type` TINYINT(1) NOT NULL COMMENT '返利类型 1-会员返利 2-消费返利 3-推广返利 4-积分返利',
  `calc_type` TINYINT(1) DEFAULT 1 COMMENT '计算方式 1-固定金额 2-百分比',
  `rebate_value` DECIMAL(10,2) DEFAULT 0.00 COMMENT '返利值（固定金额或百分比）',
  `min_amount` DECIMAL(10,2) DEFAULT NULL COMMENT '最低消费金额',
  `max_rebate` DECIMAL(10,2) DEFAULT NULL COMMENT '最高返利金额',
  `goods_type` TINYINT(1) DEFAULT 0 COMMENT '适用商品类型 0-全部商品 1-指定分类 2-指定商品',
  `goods_ids` TEXT DEFAULT NULL COMMENT '适用商品/分类ID（JSON数组）',
  `member_levels` VARCHAR(255) DEFAULT NULL COMMENT '适用会员等级（JSON数组）',
  `start_time` DATETIME DEFAULT NULL COMMENT '生效开始时间',
  `end_time` DATETIME DEFAULT NULL COMMENT '生效结束时间',
  `priority` INT(11) DEFAULT 0 COMMENT '优先级',
  `status` TINYINT(1) DEFAULT 1 COMMENT '状态 0-禁用 1-启用',
  `remark` VARCHAR(500) DEFAULT NULL COMMENT '备注',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` TINYINT(1) DEFAULT 0 COMMENT '是否删除 0-未删除 1-已删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_rule_code` (`rule_code`, `deleted`),
  KEY `idx_shop_id` (`shop_id`),
  KEY `idx_rebate_type` (`rebate_type`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='返利规则表';

-- 8. 返利记录表
DROP TABLE IF EXISTS `hou_mall_rebate_record`;
CREATE TABLE `hou_mall_rebate_record` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `rebate_no` VARCHAR(50) NOT NULL COMMENT '返利单号',
  `shop_id` BIGINT(20) DEFAULT NULL COMMENT '店铺ID',
  `user_id` BIGINT(20) NOT NULL COMMENT '用户ID',
  `order_id` BIGINT(20) DEFAULT NULL COMMENT '关联订单ID',
  `order_no` VARCHAR(50) DEFAULT NULL COMMENT '关联订单号',
  `rule_id` BIGINT(20) DEFAULT NULL COMMENT '返利规则ID',
  `rebate_type` TINYINT(1) NOT NULL COMMENT '返利类型 1-会员返利 2-消费返利 3-推广返利 4-积分返利',
  `order_amount` DECIMAL(10,2) DEFAULT 0.00 COMMENT '订单金额',
  `rebate_amount` DECIMAL(10,2) DEFAULT 0.00 COMMENT '返利金额',
  `rebate_points` INT(11) DEFAULT 0 COMMENT '返利积分',
  `status` TINYINT(1) DEFAULT 0 COMMENT '状态 0-待结算 1-已结算 2-已取消 3-已失效',
  `settle_time` DATETIME DEFAULT NULL COMMENT '结算时间',
  `invalid_reason` VARCHAR(500) DEFAULT NULL COMMENT '失效原因',
  `remark` VARCHAR(500) DEFAULT NULL COMMENT '备注',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` TINYINT(1) DEFAULT 0 COMMENT '是否删除 0-未删除 1-已删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_rebate_no` (`rebate_no`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_order_id` (`order_id`),
  KEY `idx_status` (`status`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='返利记录表';

-- 9. 返利发放表
DROP TABLE IF EXISTS `hou_mall_rebate_payment`;
CREATE TABLE `hou_mall_rebate_payment` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `payment_no` VARCHAR(50) NOT NULL COMMENT '发放单号',
  `shop_id` BIGINT(20) DEFAULT NULL COMMENT '店铺ID',
  `user_id` BIGINT(20) NOT NULL COMMENT '用户ID',
  `payment_amount` DECIMAL(10,2) NOT NULL COMMENT '发放金额',
  `payment_method` TINYINT(1) DEFAULT 1 COMMENT '发放方式 1-余额 2-银行卡 3-微信 4-支付宝',
  `payment_account` VARCHAR(100) DEFAULT NULL COMMENT '收款账号',
  `payee_name` VARCHAR(50) DEFAULT NULL COMMENT '收款人姓名',
  `bank_name` VARCHAR(100) DEFAULT NULL COMMENT '银行名称',
  `rebate_record_ids` TEXT DEFAULT NULL COMMENT '关联返利记录ID（JSON数组）',
  `status` TINYINT(1) DEFAULT 0 COMMENT '状态 0-待发放 1-发放中 2-已发放 3-发放失败',
  `payment_time` DATETIME DEFAULT NULL COMMENT '发放时间',
  `transaction_no` VARCHAR(100) DEFAULT NULL COMMENT '交易流水号',
  `fail_reason` VARCHAR(500) DEFAULT NULL COMMENT '失败原因',
  `operator_id` BIGINT(20) DEFAULT NULL COMMENT '操作人ID',
  `operator_name` VARCHAR(50) DEFAULT NULL COMMENT '操作人姓名',
  `remark` VARCHAR(500) DEFAULT NULL COMMENT '备注',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` TINYINT(1) DEFAULT 0 COMMENT '是否删除 0-未删除 1-已删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_payment_no` (`payment_no`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_status` (`status`),
  KEY `idx_payment_time` (`payment_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='返利发放表';

-- 10. 返利调整表
DROP TABLE IF EXISTS `hou_mall_rebate_adjustment`;
CREATE TABLE `hou_mall_rebate_adjustment` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `adjust_no` VARCHAR(50) NOT NULL COMMENT '调整单号',
  `shop_id` BIGINT(20) DEFAULT NULL COMMENT '店铺ID',
  `user_id` BIGINT(20) NOT NULL COMMENT '用户ID',
  `rebate_record_id` BIGINT(20) DEFAULT NULL COMMENT '关联返利记录ID',
  `adjust_type` TINYINT(1) NOT NULL COMMENT '调整类型 1-增加 2-减少',
  `adjust_amount` DECIMAL(10,2) NOT NULL COMMENT '调整金额',
  `before_amount` DECIMAL(10,2) DEFAULT 0.00 COMMENT '调整前金额',
  `after_amount` DECIMAL(10,2) DEFAULT 0.00 COMMENT '调整后金额',
  `reason` VARCHAR(500) NOT NULL COMMENT '调整原因',
  `operator_id` BIGINT(20) DEFAULT NULL COMMENT '操作人ID',
  `operator_name` VARCHAR(50) DEFAULT NULL COMMENT '操作人姓名',
  `audit_status` TINYINT(1) DEFAULT 0 COMMENT '审核状态 0-待审核 1-已通过 2-已拒绝',
  `auditor_id` BIGINT(20) DEFAULT NULL COMMENT '审核人ID',
  `auditor_name` VARCHAR(50) DEFAULT NULL COMMENT '审核人姓名',
  `audit_time` DATETIME DEFAULT NULL COMMENT '审核时间',
  `audit_remark` VARCHAR(500) DEFAULT NULL COMMENT '审核备注',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` TINYINT(1) DEFAULT 0 COMMENT '是否删除 0-未删除 1-已删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_adjust_no` (`adjust_no`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_audit_status` (`audit_status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='返利调整表';

-- ================================================================
-- 三、风控模块 (Risk Control Module) - 3张表
-- ================================================================

-- 11. 风控规则表
DROP TABLE IF EXISTS `hou_mall_risk_rule`;
CREATE TABLE `hou_mall_risk_rule` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `rule_name` VARCHAR(100) NOT NULL COMMENT '规则名称',
  `rule_code` VARCHAR(50) NOT NULL COMMENT '规则编码',
  `shop_id` BIGINT(20) DEFAULT NULL COMMENT '店铺ID',
  `rule_type` TINYINT(1) NOT NULL COMMENT '规则类型 1-库存预警 2-价格异常 3-订单异常 4-交易风险 5-账户安全',
  `monitor_index` VARCHAR(100) NOT NULL COMMENT '监控指标 如：stock_quantity, price_change_rate, order_frequency',
  `compare_type` TINYINT(1) NOT NULL COMMENT '比较方式 1-大于 2-小于 3-等于 4-大于等于 5-小于等于 6-区间',
  `threshold` DECIMAL(15,2) DEFAULT NULL COMMENT '阈值',
  `threshold_max` DECIMAL(15,2) DEFAULT NULL COMMENT '阈值上限（区间比较时使用）',
  `alert_level` TINYINT(1) DEFAULT 2 COMMENT '预警等级 1-低 2-中 3-高 4-紧急',
  `notify_methods` VARCHAR(255) DEFAULT NULL COMMENT '通知方式（JSON数组）1-站内信 2-短信 3-邮件 4-钉钉',
  `notify_receivers` TEXT DEFAULT NULL COMMENT '通知接收人（JSON数组）',
  `check_interval` INT(11) DEFAULT 60 COMMENT '检查频率（分钟）',
  `status` TINYINT(1) DEFAULT 1 COMMENT '状态 0-禁用 1-启用',
  `remark` VARCHAR(500) DEFAULT NULL COMMENT '备注',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` TINYINT(1) DEFAULT 0 COMMENT '是否删除 0-未删除 1-已删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_rule_code` (`rule_code`, `deleted`),
  KEY `idx_shop_id` (`shop_id`),
  KEY `idx_rule_type` (`rule_type`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='风控规则表';

-- 12. 风控监控表
DROP TABLE IF EXISTS `hou_mall_risk_monitor`;
CREATE TABLE `hou_mall_risk_monitor` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `shop_id` BIGINT(20) DEFAULT NULL COMMENT '店铺ID',
  `monitor_type` TINYINT(1) NOT NULL COMMENT '监控类型 1-库存监控 2-价格监控 3-订单监控 4-交易监控 5-账户监控',
  `target_id` BIGINT(20) NOT NULL COMMENT '监控对象ID（商品ID/订单ID/用户ID等）',
  `target_type` VARCHAR(50) DEFAULT NULL COMMENT '监控对象类型',
  `target_name` VARCHAR(200) DEFAULT NULL COMMENT '监控对象名称',
  `current_value` DECIMAL(15,2) DEFAULT 0.00 COMMENT '当前值',
  `base_value` DECIMAL(15,2) DEFAULT 0.00 COMMENT '基准值',
  `change_rate` DECIMAL(8,2) DEFAULT 0.00 COMMENT '变化率',
  `monitor_status` TINYINT(1) DEFAULT 0 COMMENT '监控状态 0-正常 1-预警 2-异常',
  `last_check_time` DATETIME DEFAULT NULL COMMENT '最后检查时间',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` TINYINT(1) DEFAULT 0 COMMENT '是否删除 0-未删除 1-已删除',
  PRIMARY KEY (`id`),
  KEY `idx_shop_id` (`shop_id`),
  KEY `idx_target` (`target_type`, `target_id`),
  KEY `idx_monitor_status` (`monitor_status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='风控监控表';

-- 13. 风控预警表
DROP TABLE IF EXISTS `hou_mall_risk_alert`;
CREATE TABLE `hou_mall_risk_alert` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `alert_no` VARCHAR(50) NOT NULL COMMENT '预警编号',
  `shop_id` BIGINT(20) DEFAULT NULL COMMENT '店铺ID',
  `rule_id` BIGINT(20) DEFAULT NULL COMMENT '关联规则ID',
  `rule_name` VARCHAR(100) DEFAULT NULL COMMENT '规则名称',
  `alert_type` TINYINT(1) NOT NULL COMMENT '预警类型 1-库存预警 2-价格异常 3-订单异常 4-交易风险 5-账户安全',
  `alert_level` TINYINT(1) DEFAULT 2 COMMENT '预警等级 1-低 2-中 3-高 4-紧急',
  `target_id` BIGINT(20) DEFAULT NULL COMMENT '预警对象ID',
  `target_type` VARCHAR(50) DEFAULT NULL COMMENT '预警对象类型',
  `target_name` VARCHAR(200) DEFAULT NULL COMMENT '预警对象名称',
  `alert_content` TEXT DEFAULT NULL COMMENT '预警内容',
  `current_value` DECIMAL(15,2) DEFAULT NULL COMMENT '当前值',
  `threshold` DECIMAL(15,2) DEFAULT NULL COMMENT '阈值',
  `status` TINYINT(1) DEFAULT 0 COMMENT '状态 0-待处理 1-处理中 2-已处理 3-已忽略',
  `handler_id` BIGINT(20) DEFAULT NULL COMMENT '处理人ID',
  `handler_name` VARCHAR(50) DEFAULT NULL COMMENT '处理人姓名',
  `handle_time` DATETIME DEFAULT NULL COMMENT '处理时间',
  `handle_result` TEXT DEFAULT NULL COMMENT '处理结果',
  `handle_remark` VARCHAR(500) DEFAULT NULL COMMENT '处理备注',
  `notify_status` TINYINT(1) DEFAULT 0 COMMENT '通知状态 0-未通知 1-已通知',
  `notify_time` DATETIME DEFAULT NULL COMMENT '通知时间',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` TINYINT(1) DEFAULT 0 COMMENT '是否删除 0-未删除 1-已删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_alert_no` (`alert_no`),
  KEY `idx_shop_id` (`shop_id`),
  KEY `idx_alert_type` (`alert_type`),
  KEY `idx_status` (`status`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='风控预警表';

-- ================================================================
-- 四、店铺模块 (Shop Module) - 7张表
-- ================================================================

-- 14. 店铺信息表
DROP TABLE IF EXISTS `hou_mall_shop`;
CREATE TABLE `hou_mall_shop` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `shop_no` VARCHAR(50) NOT NULL COMMENT '店铺编号',
  `shop_name` VARCHAR(100) NOT NULL COMMENT '店铺名称',
  `shop_logo` VARCHAR(255) DEFAULT NULL COMMENT '店铺Logo',
  `shop_type` TINYINT(1) DEFAULT 2 COMMENT '店铺类型: 1-自营 2-入驻商家',
  `merchant_id` BIGINT(20) DEFAULT NULL COMMENT '商家ID',
  `merchant_name` VARCHAR(100) DEFAULT NULL COMMENT '商家名称',
  `contact_person` VARCHAR(50) DEFAULT NULL COMMENT '联系人',
  `contact_phone` VARCHAR(20) DEFAULT NULL COMMENT '联系电话',
  `contact_email` VARCHAR(100) DEFAULT NULL COMMENT '联系邮箱',
  `level_id` BIGINT(20) DEFAULT NULL COMMENT '店铺等级ID',
  `province` VARCHAR(50) DEFAULT NULL COMMENT '所在省份',
  `city` VARCHAR(50) DEFAULT NULL COMMENT '所在城市',
  `district` VARCHAR(50) DEFAULT NULL COMMENT '所在区域',
  `address` VARCHAR(255) DEFAULT NULL COMMENT '详细地址',
  `business_license` VARCHAR(255) DEFAULT NULL COMMENT '营业执照',
  `license_no` VARCHAR(100) DEFAULT NULL COMMENT '营业执照号',
  `legal_person` VARCHAR(50) DEFAULT NULL COMMENT '法人代表',
  `id_card_no` VARCHAR(20) DEFAULT NULL COMMENT '身份证号',
  `id_card_front` VARCHAR(255) DEFAULT NULL COMMENT '身份证正面',
  `id_card_back` VARCHAR(255) DEFAULT NULL COMMENT '身份证反面',
  `bank_account` VARCHAR(50) DEFAULT NULL COMMENT '银行账户',
  `bank_name` VARCHAR(100) DEFAULT NULL COMMENT '开户行',
  `account_name` VARCHAR(50) DEFAULT NULL COMMENT '账户名',
  `settlement_rate` DECIMAL(5,2) DEFAULT 0.00 COMMENT '结算费率%',
  `quality_deposit` DECIMAL(10,2) DEFAULT 0.00 COMMENT '质保金金额',
  `paid_deposit` DECIMAL(10,2) DEFAULT 0.00 COMMENT '已缴质保金',
  `business_categories` TEXT DEFAULT NULL COMMENT '经营类目JSON',
  `description` TEXT DEFAULT NULL COMMENT '店铺简介',
  `status` TINYINT(1) DEFAULT 0 COMMENT '状态: 0-待审核 1-正常 2-冻结 3-关闭',
  `audit_remark` VARCHAR(500) DEFAULT NULL COMMENT '审核备注',
  `audit_time` DATETIME DEFAULT NULL COMMENT '审核时间',
  `open_time` DATETIME DEFAULT NULL COMMENT '开店时间',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` TINYINT(1) DEFAULT 0 COMMENT '是否删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_shop_no` (`shop_no`, `deleted`),
  KEY `idx_merchant_id` (`merchant_id`),
  KEY `idx_status` (`status`),
  KEY `idx_level_id` (`level_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='店铺信息表';

-- 15. 店铺等级表
DROP TABLE IF EXISTS `hou_mall_shop_level`;
CREATE TABLE `hou_mall_shop_level` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `level_name` VARCHAR(50) NOT NULL COMMENT '等级名称',
  `level` INT(11) NOT NULL COMMENT '等级值',
  `min_sales` DECIMAL(15,2) DEFAULT 0.00 COMMENT '最低销售额要求',
  `commission_rate` DECIMAL(5,2) DEFAULT 0.00 COMMENT '佣金比例%',
  `special_rights` TEXT DEFAULT NULL COMMENT '特殊权益JSON',
  `icon` VARCHAR(255) DEFAULT NULL COMMENT '等级图标',
  `description` VARCHAR(500) DEFAULT NULL COMMENT '等级说明',
  `sort` INT(11) DEFAULT 0 COMMENT '排序',
  `status` TINYINT(1) DEFAULT 1 COMMENT '状态: 0-禁用 1-启用',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` TINYINT(1) DEFAULT 0 COMMENT '是否删除',
  PRIMARY KEY (`id`),
  KEY `idx_level` (`level`),
  KEY `idx_sort` (`sort`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='店铺等级表';

-- 16. 商家入驻申请表
DROP TABLE IF EXISTS `hou_mall_merchant_entry`;
CREATE TABLE `hou_mall_merchant_entry` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `apply_no` VARCHAR(50) NOT NULL COMMENT '申请单号',
  `merchant_name` VARCHAR(100) NOT NULL COMMENT '商家名称',
  `contact_person` VARCHAR(50) NOT NULL COMMENT '联系人',
  `contact_phone` VARCHAR(20) NOT NULL COMMENT '联系电话',
  `contact_email` VARCHAR(100) DEFAULT NULL COMMENT '联系邮箱',
  `shop_name` VARCHAR(100) NOT NULL COMMENT '店铺名称',
  `shop_type` TINYINT(1) DEFAULT 2 COMMENT '店铺类型',
  `business_license` VARCHAR(255) NOT NULL COMMENT '营业执照',
  `license_no` VARCHAR(100) NOT NULL COMMENT '营业执照号',
  `legal_person` VARCHAR(50) NOT NULL COMMENT '法人代表',
  `id_card_no` VARCHAR(20) NOT NULL COMMENT '身份证号',
  `id_card_front` VARCHAR(255) NOT NULL COMMENT '身份证正面',
  `id_card_back` VARCHAR(255) NOT NULL COMMENT '身份证反面',
  `business_categories` TEXT DEFAULT NULL COMMENT '经营类目JSON',
  `province` VARCHAR(50) DEFAULT NULL COMMENT '所在省份',
  `city` VARCHAR(50) DEFAULT NULL COMMENT '所在城市',
  `district` VARCHAR(50) DEFAULT NULL COMMENT '所在区域',
  `address` VARCHAR(255) DEFAULT NULL COMMENT '详细地址',
  `remark` VARCHAR(500) DEFAULT NULL COMMENT '申请备注',
  `status` TINYINT(1) DEFAULT 0 COMMENT '状态: 0-待审核 1-审核通过 2-审核拒绝 3-已入驻',
  `audit_by` BIGINT(20) DEFAULT NULL COMMENT '审核人',
  `audit_remark` VARCHAR(500) DEFAULT NULL COMMENT '审核备注',
  `audit_time` DATETIME DEFAULT NULL COMMENT '审核时间',
  `shop_id` BIGINT(20) DEFAULT NULL COMMENT '关联店铺ID',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` TINYINT(1) DEFAULT 0 COMMENT '是否删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_apply_no` (`apply_no`),
  KEY `idx_status` (`status`),
  KEY `idx_shop_id` (`shop_id`),
  KEY `idx_contact_phone` (`contact_phone`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商家入驻申请表';

-- 17. 询价单表
DROP TABLE IF EXISTS `hou_mall_inquiry`;
CREATE TABLE `hou_mall_inquiry` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `inquiry_no` VARCHAR(50) NOT NULL COMMENT '询价单号',
  `shop_id` BIGINT(20) DEFAULT NULL COMMENT '店铺ID',
  `buyer_id` BIGINT(20) NOT NULL COMMENT '买家ID',
  `buyer_name` VARCHAR(50) DEFAULT NULL COMMENT '买家名称',
  `buyer_phone` VARCHAR(20) DEFAULT NULL COMMENT '买家电话',
  `goods_id` BIGINT(20) NOT NULL COMMENT '商品ID',
  `goods_name` VARCHAR(200) DEFAULT NULL COMMENT '商品名称',
  `goods_image` VARCHAR(255) DEFAULT NULL COMMENT '商品图片',
  `spec` VARCHAR(200) DEFAULT NULL COMMENT '规格',
  `quantity` INT(11) DEFAULT 1 COMMENT '询价数量',
  `expected_price` DECIMAL(10,2) DEFAULT NULL COMMENT '期望价格',
  `remark` VARCHAR(500) DEFAULT NULL COMMENT '询价备注',
  `quoted_price` DECIMAL(10,2) DEFAULT NULL COMMENT '报价',
  `quote_remark` VARCHAR(500) DEFAULT NULL COMMENT '报价说明',
  `quote_time` DATETIME DEFAULT NULL COMMENT '报价时间',
  `status` TINYINT(1) DEFAULT 0 COMMENT '状态: 0-待报价 1-已报价 2-已接受 3-已拒绝 4-已过期',
  `expire_time` DATETIME DEFAULT NULL COMMENT '过期时间',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` TINYINT(1) DEFAULT 0 COMMENT '是否删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_inquiry_no` (`inquiry_no`),
  KEY `idx_shop_id` (`shop_id`),
  KEY `idx_buyer_id` (`buyer_id`),
  KEY `idx_goods_id` (`goods_id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='询价单表';

-- 18. 发票表
DROP TABLE IF EXISTS `hou_mall_invoice`;
CREATE TABLE `hou_mall_invoice` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `invoice_no` VARCHAR(50) NOT NULL COMMENT '发票编号',
  `order_id` BIGINT(20) NOT NULL COMMENT '订单ID',
  `order_no` VARCHAR(50) NOT NULL COMMENT '订单编号',
  `shop_id` BIGINT(20) DEFAULT NULL COMMENT '店铺ID',
  `buyer_id` BIGINT(20) NOT NULL COMMENT '买家ID',
  `invoice_type` TINYINT(1) DEFAULT 3 COMMENT '发票类型: 1-增值税专用发票 2-增值税普通发票 3-电子发票',
  `invoice_title` VARCHAR(100) NOT NULL COMMENT '发票抬头',
  `tax_no` VARCHAR(50) DEFAULT NULL COMMENT '纳税人识别号',
  `invoice_amount` DECIMAL(10,2) DEFAULT 0.00 COMMENT '发票金额',
  `tax_amount` DECIMAL(10,2) DEFAULT 0.00 COMMENT '税额',
  `total_amount` DECIMAL(10,2) DEFAULT 0.00 COMMENT '价税合计',
  `invoice_content` VARCHAR(255) DEFAULT NULL COMMENT '开票内容',
  `company_address` VARCHAR(255) DEFAULT NULL COMMENT '公司地址',
  `company_phone` VARCHAR(20) DEFAULT NULL COMMENT '公司电话',
  `bank_name` VARCHAR(100) DEFAULT NULL COMMENT '开户银行',
  `bank_account` VARCHAR(50) DEFAULT NULL COMMENT '银行账号',
  `receiver_name` VARCHAR(50) DEFAULT NULL COMMENT '收件人',
  `receiver_phone` VARCHAR(20) DEFAULT NULL COMMENT '收件电话',
  `receiver_address` VARCHAR(255) DEFAULT NULL COMMENT '收件地址',
  `pdf_url` VARCHAR(255) DEFAULT NULL COMMENT '电子发票PDF',
  `status` TINYINT(1) DEFAULT 0 COMMENT '状态: 0-待开具 1-已开具 2-已邮寄 3-已作废',
  `issue_time` DATETIME DEFAULT NULL COMMENT '开票时间',
  `mail_time` DATETIME DEFAULT NULL COMMENT '邮寄时间',
  `void_time` DATETIME DEFAULT NULL COMMENT '作废时间',
  `void_reason` VARCHAR(500) DEFAULT NULL COMMENT '作废原因',
  `remark` VARCHAR(500) DEFAULT NULL COMMENT '备注',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` TINYINT(1) DEFAULT 0 COMMENT '是否删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_invoice_no` (`invoice_no`),
  KEY `idx_order_id` (`order_id`),
  KEY `idx_buyer_id` (`buyer_id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='发票表';

-- 19. 货到付款订单表
DROP TABLE IF EXISTS `hou_mall_cash_on_delivery`;
CREATE TABLE `hou_mall_cash_on_delivery` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `order_id` BIGINT(20) NOT NULL COMMENT '订单ID',
  `order_no` VARCHAR(50) NOT NULL COMMENT '订单编号',
  `shop_id` BIGINT(20) DEFAULT NULL COMMENT '店铺ID',
  `buyer_id` BIGINT(20) NOT NULL COMMENT '买家ID',
  `order_amount` DECIMAL(10,2) DEFAULT 0.00 COMMENT '订单金额',
  `delivery_fee` DECIMAL(10,2) DEFAULT 0.00 COMMENT '配送费',
  `total_amount` DECIMAL(10,2) DEFAULT 0.00 COMMENT '应付总额',
  `receiver_name` VARCHAR(50) NOT NULL COMMENT '收货人',
  `receiver_phone` VARCHAR(20) NOT NULL COMMENT '收货电话',
  `receiver_address` VARCHAR(255) NOT NULL COMMENT '收货地址',
  `delivery_status` TINYINT(1) DEFAULT 0 COMMENT '配送状态: 0-待发货 1-配送中 2-已送达 3-已签收',
  `payment_status` TINYINT(1) DEFAULT 0 COMMENT '支付状态: 0-未支付 1-已支付',
  `delivery_time` DATETIME DEFAULT NULL COMMENT '发货时间',
  `received_time` DATETIME DEFAULT NULL COMMENT '签收时间',
  `paid_time` DATETIME DEFAULT NULL COMMENT '支付时间',
  `paid_amount` DECIMAL(10,2) DEFAULT NULL COMMENT '实付金额',
  `courier_id` BIGINT(20) DEFAULT NULL COMMENT '配送员ID',
  `courier_name` VARCHAR(50) DEFAULT NULL COMMENT '配送员姓名',
  `courier_phone` VARCHAR(20) DEFAULT NULL COMMENT '配送员电话',
  `remark` VARCHAR(500) DEFAULT NULL COMMENT '备注',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` TINYINT(1) DEFAULT 0 COMMENT '是否删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_order_id` (`order_id`, `deleted`),
  KEY `idx_buyer_id` (`buyer_id`),
  KEY `idx_delivery_status` (`delivery_status`),
  KEY `idx_payment_status` (`payment_status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='货到付款订单表';

-- 20. 质保金表
DROP TABLE IF EXISTS `hou_mall_quality_deposit`;
CREATE TABLE `hou_mall_quality_deposit` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `deposit_no` VARCHAR(50) NOT NULL COMMENT '质保金单号',
  `shop_id` BIGINT(20) NOT NULL COMMENT '店铺ID',
  `shop_name` VARCHAR(100) DEFAULT NULL COMMENT '店铺名称',
  `merchant_id` BIGINT(20) DEFAULT NULL COMMENT '商家ID',
  `merchant_name` VARCHAR(100) DEFAULT NULL COMMENT '商家名称',
  `required_amount` DECIMAL(10,2) DEFAULT 0.00 COMMENT '应缴金额',
  `paid_amount` DECIMAL(10,2) DEFAULT 0.00 COMMENT '已缴金额',
  `unpaid_amount` DECIMAL(10,2) DEFAULT 0.00 COMMENT '待缴金额',
  `payment_method` TINYINT(1) DEFAULT 1 COMMENT '缴纳方式: 1-银行转账 2-支付宝 3-微信 4-其他',
  `payment_voucher` VARCHAR(255) DEFAULT NULL COMMENT '缴纳凭证',
  `payment_time` DATETIME DEFAULT NULL COMMENT '缴纳时间',
  `status` TINYINT(1) DEFAULT 0 COMMENT '状态: 0-未缴纳 1-已缴纳 2-部分缴纳 3-已退还',
  `refund_amount` DECIMAL(10,2) DEFAULT NULL COMMENT '退还金额',
  `refund_reason` VARCHAR(500) DEFAULT NULL COMMENT '退还原因',
  `refund_time` DATETIME DEFAULT NULL COMMENT '退还时间',
  `remark` VARCHAR(500) DEFAULT NULL COMMENT '备注',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` TINYINT(1) DEFAULT 0 COMMENT '是否删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_deposit_no` (`deposit_no`),
  KEY `idx_shop_id` (`shop_id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='质保金表';

-- ================================================================
-- 五、仓储模块 (Storage Module) - 6张表
-- ================================================================

-- 21. 仓库表
DROP TABLE IF EXISTS `hou_mall_warehouse`;
CREATE TABLE `hou_mall_warehouse` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `warehouse_code` VARCHAR(50) NOT NULL COMMENT '仓库编码',
  `warehouse_name` VARCHAR(100) NOT NULL COMMENT '仓库名称',
  `shop_id` BIGINT(20) DEFAULT NULL COMMENT '店铺ID',
  `warehouse_type` TINYINT(1) DEFAULT 1 COMMENT '仓库类型 1-自营仓 2-第三方仓 3-虚拟仓',
  `province` VARCHAR(50) DEFAULT NULL COMMENT '省份',
  `city` VARCHAR(50) DEFAULT NULL COMMENT '城市',
  `district` VARCHAR(50) DEFAULT NULL COMMENT '区县',
  `address` VARCHAR(255) DEFAULT NULL COMMENT '详细地址',
  `contact_name` VARCHAR(50) DEFAULT NULL COMMENT '联系人',
  `contact_phone` VARCHAR(20) DEFAULT NULL COMMENT '联系电话',
  `area` DOUBLE DEFAULT NULL COMMENT '仓库面积（平方米）',
  `is_default` TINYINT(1) DEFAULT 0 COMMENT '是否默认仓库 0-否 1-是',
  `status` TINYINT(1) DEFAULT 1 COMMENT '状态 0-禁用 1-启用',
  `remark` VARCHAR(500) DEFAULT NULL COMMENT '备注',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` TINYINT(1) DEFAULT 0 COMMENT '是否删除 0-未删除 1-已删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_warehouse_code` (`warehouse_code`, `deleted`),
  KEY `idx_shop_id` (`shop_id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='仓库表';

-- 22. 库存表
DROP TABLE IF EXISTS `hou_mall_stock`;
CREATE TABLE `hou_mall_stock` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `warehouse_id` BIGINT(20) NOT NULL COMMENT '仓库ID',
  `goods_id` BIGINT(20) NOT NULL COMMENT '商品ID',
  `sku_id` BIGINT(20) DEFAULT NULL COMMENT 'SKU ID',
  `goods_name` VARCHAR(200) DEFAULT NULL COMMENT '商品名称',
  `sku_spec` VARCHAR(200) DEFAULT NULL COMMENT 'SKU规格',
  `quantity` INT(11) DEFAULT 0 COMMENT '库存数量',
  `available_qty` INT(11) DEFAULT 0 COMMENT '可用数量',
  `locked_qty` INT(11) DEFAULT 0 COMMENT '锁定数量',
  `warning_qty` INT(11) DEFAULT 0 COMMENT '预警数量',
  `cost_price` DECIMAL(10,2) DEFAULT 0.00 COMMENT '成本价',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` TINYINT(1) DEFAULT 0 COMMENT '是否删除 0-否 1-是',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_warehouse_goods_sku` (`warehouse_id`, `goods_id`, `sku_id`, `deleted`),
  KEY `idx_goods_id` (`goods_id`),
  KEY `idx_sku_id` (`sku_id`),
  KEY `idx_quantity` (`quantity`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='库存表';

-- 23. 入库单表
DROP TABLE IF EXISTS `hou_mall_stock_inbound`;
CREATE TABLE `hou_mall_stock_inbound` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `inbound_no` VARCHAR(50) NOT NULL COMMENT '入库单号',
  `shop_id` BIGINT(20) DEFAULT NULL COMMENT '店铺ID',
  `warehouse_id` BIGINT(20) NOT NULL COMMENT '仓库ID',
  `inbound_type` TINYINT(1) DEFAULT 1 COMMENT '入库类型 1-采购入库 2-退货入库 3-调拨入库 4-盘盈入库 5-其他入库',
  `related_no` VARCHAR(50) DEFAULT NULL COMMENT '关联单号（采购单/退货单/调拨单）',
  `supplier_id` BIGINT(20) DEFAULT NULL COMMENT '供应商ID',
  `total_quantity` INT(11) DEFAULT 0 COMMENT '入库商品数量',
  `total_amount` DECIMAL(15,2) DEFAULT 0.00 COMMENT '入库商品总金额',
  `status` TINYINT(1) DEFAULT 0 COMMENT '状态 0-待入库 1-部分入库 2-已完成 3-已取消',
  `expected_time` DATETIME DEFAULT NULL COMMENT '预计入库时间',
  `actual_time` DATETIME DEFAULT NULL COMMENT '实际入库时间',
  `operator_id` BIGINT(20) DEFAULT NULL COMMENT '操作人ID',
  `operator_name` VARCHAR(50) DEFAULT NULL COMMENT '操作人姓名',
  `remark` VARCHAR(500) DEFAULT NULL COMMENT '备注',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` TINYINT(1) DEFAULT 0 COMMENT '是否删除 0-未删除 1-已删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_inbound_no` (`inbound_no`),
  KEY `idx_warehouse_id` (`warehouse_id`),
  KEY `idx_status` (`status`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='入库单表';

-- 24. 出库单表
DROP TABLE IF EXISTS `hou_mall_stock_outbound`;
CREATE TABLE `hou_mall_stock_outbound` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `outbound_no` VARCHAR(50) NOT NULL COMMENT '出库单号',
  `shop_id` BIGINT(20) DEFAULT NULL COMMENT '店铺ID',
  `warehouse_id` BIGINT(20) NOT NULL COMMENT '仓库ID',
  `outbound_type` TINYINT(1) DEFAULT 1 COMMENT '出库类型 1-销售出库 2-调拨出库 3-盘亏出库 4-报损出库 5-其他出库',
  `related_no` VARCHAR(50) DEFAULT NULL COMMENT '关联单号（订单号/调拨单）',
  `total_quantity` INT(11) DEFAULT 0 COMMENT '出库商品数量',
  `total_amount` DECIMAL(15,2) DEFAULT 0.00 COMMENT '出库商品总金额',
  `status` TINYINT(1) DEFAULT 0 COMMENT '状态 0-待出库 1-部分出库 2-已完成 3-已取消',
  `expected_time` DATETIME DEFAULT NULL COMMENT '预计出库时间',
  `actual_time` DATETIME DEFAULT NULL COMMENT '实际出库时间',
  `operator_id` BIGINT(20) DEFAULT NULL COMMENT '操作人ID',
  `operator_name` VARCHAR(50) DEFAULT NULL COMMENT '操作人姓名',
  `remark` VARCHAR(500) DEFAULT NULL COMMENT '备注',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` TINYINT(1) DEFAULT 0 COMMENT '是否删除 0-未删除 1-已删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_outbound_no` (`outbound_no`),
  KEY `idx_warehouse_id` (`warehouse_id`),
  KEY `idx_status` (`status`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='出库单表';

-- 25. 库存调拨单表
DROP TABLE IF EXISTS `hou_mall_stock_transfer`;
CREATE TABLE `hou_mall_stock_transfer` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `transfer_no` VARCHAR(50) NOT NULL COMMENT '调拨单号',
  `shop_id` BIGINT(20) DEFAULT NULL COMMENT '店铺ID',
  `from_warehouse_id` BIGINT(20) NOT NULL COMMENT '调出仓库ID',
  `to_warehouse_id` BIGINT(20) NOT NULL COMMENT '调入仓库ID',
  `transfer_type` TINYINT(1) DEFAULT 1 COMMENT '调拨类型 1-普通调拨 2-紧急调拨',
  `total_quantity` INT(11) DEFAULT 0 COMMENT '调拨商品数量',
  `status` TINYINT(1) DEFAULT 0 COMMENT '状态 0-待审核 1-已审核 2-调拨中 3-已完成 4-已取消',
  `expected_time` DATETIME DEFAULT NULL COMMENT '预计完成时间',
  `actual_time` DATETIME DEFAULT NULL COMMENT '实际完成时间',
  `applicant_id` BIGINT(20) DEFAULT NULL COMMENT '申请人ID',
  `applicant_name` VARCHAR(50) DEFAULT NULL COMMENT '申请人姓名',
  `auditor_id` BIGINT(20) DEFAULT NULL COMMENT '审核人ID',
  `auditor_name` VARCHAR(50) DEFAULT NULL COMMENT '审核人姓名',
  `audit_time` DATETIME DEFAULT NULL COMMENT '审核时间',
  `audit_remark` VARCHAR(500) DEFAULT NULL COMMENT '审核备注',
  `remark` VARCHAR(500) DEFAULT NULL COMMENT '备注',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` TINYINT(1) DEFAULT 0 COMMENT '是否删除 0-未删除 1-已删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_transfer_no` (`transfer_no`),
  KEY `idx_from_warehouse` (`from_warehouse_id`),
  KEY `idx_to_warehouse` (`to_warehouse_id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='库存调拨单表';

-- 26. 库存盘点单表
DROP TABLE IF EXISTS `hou_mall_stock_inventory`;
CREATE TABLE `hou_mall_stock_inventory` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `inventory_no` VARCHAR(50) NOT NULL COMMENT '盘点单号',
  `shop_id` BIGINT(20) DEFAULT NULL COMMENT '店铺ID',
  `warehouse_id` BIGINT(20) NOT NULL COMMENT '仓库ID',
  `inventory_type` TINYINT(1) DEFAULT 1 COMMENT '盘点类型 1-全盘 2-抽盘 3-动态盘点',
  `inventory_scope` TINYINT(1) DEFAULT 1 COMMENT '盘点范围 1-全部商品 2-指定分类 3-指定商品',
  `total_quantity` INT(11) DEFAULT 0 COMMENT '盘点商品数量',
  `profit_quantity` INT(11) DEFAULT 0 COMMENT '盘盈数量',
  `loss_quantity` INT(11) DEFAULT 0 COMMENT '盘亏数量',
  `status` TINYINT(1) DEFAULT 0 COMMENT '状态 0-待盘点 1-盘点中 2-待审核 3-已完成 4-已取消',
  `start_time` DATETIME DEFAULT NULL COMMENT '开始时间',
  `end_time` DATETIME DEFAULT NULL COMMENT '结束时间',
  `operator_id` BIGINT(20) DEFAULT NULL COMMENT '操作人ID',
  `operator_name` VARCHAR(50) DEFAULT NULL COMMENT '操作人姓名',
  `auditor_id` BIGINT(20) DEFAULT NULL COMMENT '审核人ID',
  `auditor_name` VARCHAR(50) DEFAULT NULL COMMENT '审核人姓名',
  `audit_time` DATETIME DEFAULT NULL COMMENT '审核时间',
  `audit_remark` VARCHAR(500) DEFAULT NULL COMMENT '审核备注',
  `remark` VARCHAR(500) DEFAULT NULL COMMENT '备注',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` TINYINT(1) DEFAULT 0 COMMENT '是否删除 0-未删除 1-已删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_inventory_no` (`inventory_no`),
  KEY `idx_warehouse_id` (`warehouse_id`),
  KEY `idx_status` (`status`),
  KEY `idx_start_time` (`start_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='库存盘点单表';

-- ================================================================
-- 六、首页模块 (Home Module) - 4张表
-- ================================================================

-- 27. Banner轮播图表
DROP TABLE IF EXISTS `hou_mall_banner`;
CREATE TABLE `hou_mall_banner` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `shop_id` BIGINT(20) DEFAULT NULL COMMENT '店铺ID(NULL表示平台级)',
  `title` VARCHAR(100) DEFAULT NULL COMMENT 'Banner标题',
  `image_url` VARCHAR(255) NOT NULL COMMENT '图片URL',
  `link_url` VARCHAR(255) DEFAULT NULL COMMENT '跳转链接',
  `link_type` TINYINT(1) DEFAULT 5 COMMENT '链接类型: 1-商品 2-分类 3-活动 4-外链 5-无跳转',
  `link_id` BIGINT(20) DEFAULT NULL COMMENT '关联ID(商品ID/分类ID/活动ID)',
  `position` VARCHAR(50) DEFAULT 'home' COMMENT '位置: home-首页 category-分类页',
  `bg_color` VARCHAR(20) DEFAULT NULL COMMENT '背景色',
  `badge` VARCHAR(50) DEFAULT NULL COMMENT '角标文字',
  `sort` INT(11) DEFAULT 0 COMMENT '排序(越大越前)',
  `start_time` DATETIME DEFAULT NULL COMMENT '开始时间',
  `end_time` DATETIME DEFAULT NULL COMMENT '结束时间',
  `status` TINYINT(1) DEFAULT 1 COMMENT '状态: 0-禁用 1-启用',
  `create_by` BIGINT(20) DEFAULT NULL COMMENT '创建人',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` TINYINT(1) DEFAULT 0 COMMENT '是否删除',
  PRIMARY KEY (`id`),
  KEY `idx_shop_id` (`shop_id`),
  KEY `idx_position` (`position`),
  KEY `idx_status_sort` (`status`, `sort`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Banner轮播图表';

-- 28. 首页楼层配置表
DROP TABLE IF EXISTS `hou_mall_home_floor`;
CREATE TABLE `hou_mall_home_floor` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `shop_id` BIGINT(20) DEFAULT NULL COMMENT '店铺ID(NULL表示平台级)',
  `floor_name` VARCHAR(100) NOT NULL COMMENT '楼层名称',
  `floor_type` VARCHAR(50) NOT NULL COMMENT '楼层类型: banner/seckill/recommend/brand/category',
  `floor_data` TEXT DEFAULT NULL COMMENT '楼层配置JSON',
  `sort` INT(11) DEFAULT 0 COMMENT '排序',
  `status` TINYINT(1) DEFAULT 1 COMMENT '状态: 0-禁用 1-启用',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` TINYINT(1) DEFAULT 0 COMMENT '是否删除',
  PRIMARY KEY (`id`),
  KEY `idx_shop_id` (`shop_id`),
  KEY `idx_status_sort` (`status`, `sort`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='首页楼层配置表';

-- 29. 公告快报表
DROP TABLE IF EXISTS `hou_mall_notice`;
CREATE TABLE `hou_mall_notice` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `shop_id` BIGINT(20) DEFAULT NULL COMMENT '店铺ID(NULL表示平台级)',
  `title` VARCHAR(200) NOT NULL COMMENT '公告标题',
  `content` TEXT DEFAULT NULL COMMENT '公告内容',
  `notice_type` TINYINT(1) DEFAULT 2 COMMENT '类型: 1-快报 2-公告 3-通知',
  `cover_image` VARCHAR(255) DEFAULT NULL COMMENT '封面图片',
  `link_url` VARCHAR(255) DEFAULT NULL COMMENT '详情链接',
  `is_top` TINYINT(1) DEFAULT 0 COMMENT '是否置顶',
  `view_count` INT(11) DEFAULT 0 COMMENT '浏览次数',
  `start_time` DATETIME DEFAULT NULL COMMENT '开始时间',
  `end_time` DATETIME DEFAULT NULL COMMENT '结束时间',
  `status` TINYINT(1) DEFAULT 0 COMMENT '状态: 0-草稿 1-已发布 2-已下线',
  `create_by` BIGINT(20) DEFAULT NULL COMMENT '创建人',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` TINYINT(1) DEFAULT 0 COMMENT '是否删除',
  PRIMARY KEY (`id`),
  KEY `idx_shop_id` (`shop_id`),
  KEY `idx_notice_type` (`notice_type`),
  KEY `idx_status` (`status`),
  KEY `idx_is_top` (`is_top`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='公告快报表';

-- 30. 推荐商品表
DROP TABLE IF EXISTS `hou_mall_recommend_goods`;
CREATE TABLE `hou_mall_recommend_goods` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `shop_id` BIGINT(20) DEFAULT NULL COMMENT '店铺ID(NULL表示平台级)',
  `goods_id` BIGINT(20) NOT NULL COMMENT '商品ID',
  `goods_name` VARCHAR(200) DEFAULT NULL COMMENT '商品名称(冗余字段)',
  `goods_image` VARCHAR(255) DEFAULT NULL COMMENT '商品图片(冗余字段)',
  `goods_price` DECIMAL(10,2) DEFAULT NULL COMMENT '商品价格(冗余字段)',
  `market_price` DECIMAL(10,2) DEFAULT NULL COMMENT '市场价格(冗余字段)',
  `recommend_type` TINYINT(1) DEFAULT 1 COMMENT '推荐类型: 1-热门推荐 2-新品推荐 3-猜你喜欢 4-人气榜单',
  `position` VARCHAR(50) DEFAULT 'home' COMMENT '推荐位置: home-首页 category-分类页 detail-详情页',
  `sort` INT(11) DEFAULT 0 COMMENT '排序值',
  `status` TINYINT(1) DEFAULT 1 COMMENT '状态: 0-禁用 1-启用',
  `start_time` DATETIME DEFAULT NULL COMMENT '开始时间',
  `end_time` DATETIME DEFAULT NULL COMMENT '结束时间',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` TINYINT(1) DEFAULT 0 COMMENT '是否删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_goods_type_position` (`goods_id`, `recommend_type`, `position`, `deleted`),
  KEY `idx_shop_id` (`shop_id`),
  KEY `idx_recommend_type` (`recommend_type`),
  KEY `idx_status_sort` (`status`, `sort`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='推荐商品表';

-- ================================================================
-- SQL脚本执行完成
-- 共创建30张表
-- ================================================================
-- 分销模块(6): distributor, distributor_level, commission_rule, commission_record, distribution_goods, distribution_order
-- 返利模块(4): rebate_rule, rebate_record, rebate_payment, rebate_adjustment
-- 风控模块(3): risk_rule, risk_monitor, risk_alert
-- 店铺模块(7): shop, shop_level, merchant_entry, inquiry, invoice, cash_on_delivery, quality_deposit
-- 仓储模块(6): warehouse, stock, stock_inbound, stock_outbound, stock_transfer, stock_inventory
-- 首页模块(4): banner, home_floor, notice, recommend_goods
-- ================================================================