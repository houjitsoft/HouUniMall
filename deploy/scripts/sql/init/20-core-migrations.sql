-- =====================================================================
-- HouCloud 统一初始化 ③/④：平台核心（Core）增量建表聚合
-- 范围：backend/paas 下核心服务的建表与菜单分组脚本，**不含任何插件**。
-- 全部 CREATE TABLE 已统一补 IF NOT EXISTS（幂等，可重复执行）。
-- 插件表不在本文件：插件是独立制品，装完商城后按需安装，见 init/plugins/。
-- =====================================================================


-- ---------- backend/paas/base/src/main/resources/sql/protocol_table.sql ----------
-- 协议管理表
CREATE TABLE IF NOT EXISTS `hou_base_protocol` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `name` varchar(100) NOT NULL COMMENT '协议名称',
  `type` varchar(50) NOT NULL COMMENT '协议类型：PERSONAL-个人入驻，ENTERPRISE-企业入驻，SUPPLIER-供应商入驻，COOPERATION-合作协议',
  `file_url` varchar(500) DEFAULT NULL COMMENT '文件URL',
  `file_name` varchar(200) DEFAULT NULL COMMENT '文件名称',
  `sort` int DEFAULT 0 COMMENT '排序',
  `status` tinyint DEFAULT 1 COMMENT '状态：1-启用，0-禁用',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_type` (`type`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='协议管理表';

-- 插入默认数据


-- ---------- backend/paas/goods/create_tables.sql ----------
-- =============================================
-- 商品管理模块数据库表创建脚本
-- 创建时间：2026-01-08
-- 说明：品牌类型、属性类型、属性组管理表
-- =============================================

-- 1. 品牌类型表
CREATE TABLE IF NOT EXISTS `goods_brand_type` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '品牌类型ID',
  `name` varchar(100) NOT NULL COMMENT '类型名称',
  `sort` int(11) DEFAULT '0' COMMENT '排序',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_sort` (`sort`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='品牌类型表';

-- 2. 品牌类型关联表
CREATE TABLE IF NOT EXISTS `goods_brand_type_relation` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `brand_type_id` int(11) NOT NULL COMMENT '品牌类型ID',
  `brand_id` int(11) NOT NULL COMMENT '品牌ID',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_brand_type_id` (`brand_type_id`),
  KEY `idx_brand_id` (`brand_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='品牌类型关联表';

-- 3. 属性类型表
CREATE TABLE IF NOT EXISTS `goods_attribute_type` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '属性类型ID',
  `name` varchar(100) NOT NULL COMMENT '属性类型名称',
  `remark` varchar(500) DEFAULT NULL COMMENT '属性类型备注',
  `value_count` int(11) DEFAULT '0' COMMENT '属性值数量',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_name` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='属性类型表';

-- 4. 属性类型值表
CREATE TABLE IF NOT EXISTS `goods_attribute_type_value` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '属性值ID',
  `attribute_type_id` int(11) NOT NULL COMMENT '属性类型ID',
  `value_name` varchar(100) NOT NULL COMMENT '属性值名称',
  `sort` int(11) DEFAULT '0' COMMENT '排序',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_attribute_type_id` (`attribute_type_id`),
  KEY `idx_sort` (`sort`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='属性类型值表';

-- 5. 属性组表
CREATE TABLE IF NOT EXISTS `goods_attribute_group` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '属性组ID',
  `name` varchar(100) NOT NULL COMMENT '属性组名称',
  `remark` varchar(500) DEFAULT NULL COMMENT '属性组备注',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_name` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='属性组表';

-- 6. 属性组关联表
CREATE TABLE IF NOT EXISTS `goods_attribute_group_relation` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `attribute_group_id` int(11) NOT NULL COMMENT '属性组ID',
  `attribute_type_id` int(11) NOT NULL COMMENT '属性类型ID',
  `attribute_type_name` varchar(100) DEFAULT NULL COMMENT '属性类型名称（冗余字段）',
  `is_filter` tinyint(1) DEFAULT '0' COMMENT '是否设为筛选条件：0-否，1-是',
  `sort` int(11) DEFAULT '0' COMMENT '排序',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_attribute_group_id` (`attribute_group_id`),
  KEY `idx_attribute_type_id` (`attribute_type_id`),
  KEY `idx_sort` (`sort`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='属性组关联表';

-- =============================================
-- 初始化数据示例
-- =============================================

-- 插入品牌类型示例数据

-- 插入属性类型示例数据

-- 插入属性组示例数据

-- =============================================
-- 说明：
-- 1. 执行此脚本前请确保数据库已选择正确（USE meoc;）
-- 2. 如果表已存在，CREATE TABLE 语句会被跳过
-- 3. 示例数据可以根据实际需要调整或删除
-- =============================================


-- ---------- backend/paas/mall/src/main/resources/sql/mall_tables.sql ----------
-- ================================================================
-- Mall商城模块数据库表结构SQL
-- 生成时间: 2026-01-05
-- 包含模块: 分销、返利、仓储、风控、店铺、首页共30张表
-- ================================================================

-- ================================================================
-- 一、分销模块 (Distribution Module) - 6张表
-- ================================================================

-- 1. 分销员表
CREATE TABLE IF NOT EXISTS `hou_mall_distributor` (
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
CREATE TABLE IF NOT EXISTS `hou_mall_distributor_level` (
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
CREATE TABLE IF NOT EXISTS `hou_mall_commission_rule` (
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
CREATE TABLE IF NOT EXISTS `hou_mall_commission_record` (
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
CREATE TABLE IF NOT EXISTS `hou_mall_distribution_goods` (
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
CREATE TABLE IF NOT EXISTS `hou_mall_distribution_order` (
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
CREATE TABLE IF NOT EXISTS `hou_mall_rebate_rule` (
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
CREATE TABLE IF NOT EXISTS `hou_mall_rebate_record` (
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
CREATE TABLE IF NOT EXISTS `hou_mall_rebate_payment` (
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
CREATE TABLE IF NOT EXISTS `hou_mall_rebate_adjustment` (
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
CREATE TABLE IF NOT EXISTS `hou_mall_risk_rule` (
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
CREATE TABLE IF NOT EXISTS `hou_mall_risk_monitor` (
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
CREATE TABLE IF NOT EXISTS `hou_mall_risk_alert` (
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
CREATE TABLE IF NOT EXISTS `hou_mall_shop` (
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
CREATE TABLE IF NOT EXISTS `hou_mall_shop_level` (
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
CREATE TABLE IF NOT EXISTS `hou_mall_merchant_entry` (
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
CREATE TABLE IF NOT EXISTS `hou_mall_inquiry` (
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
CREATE TABLE IF NOT EXISTS `hou_mall_invoice` (
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
CREATE TABLE IF NOT EXISTS `hou_mall_cash_on_delivery` (
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
CREATE TABLE IF NOT EXISTS `hou_mall_quality_deposit` (
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
CREATE TABLE IF NOT EXISTS `hou_mall_warehouse` (
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
CREATE TABLE IF NOT EXISTS `hou_mall_stock` (
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
CREATE TABLE IF NOT EXISTS `hou_mall_stock_inbound` (
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
CREATE TABLE IF NOT EXISTS `hou_mall_stock_outbound` (
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
CREATE TABLE IF NOT EXISTS `hou_mall_stock_transfer` (
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
CREATE TABLE IF NOT EXISTS `hou_mall_stock_inventory` (
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
CREATE TABLE IF NOT EXISTS `hou_mall_banner` (
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
CREATE TABLE IF NOT EXISTS `hou_mall_home_floor` (
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
CREATE TABLE IF NOT EXISTS `hou_mall_notice` (
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
CREATE TABLE IF NOT EXISTS `hou_mall_recommend_goods` (
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

-- ---------- backend/paas/mall/src/main/resources/sql/mall_core_tables.sql ----------
-- ================================================================
-- Mall商城核心电商功能补充建表SQL
-- 生成时间: 2026-01-05
-- 新增表: 购物车、用户收货地址、商品收藏 共3张表
-- ================================================================

-- 1. 购物车表
CREATE TABLE IF NOT EXISTS `hou_mall_shopping_cart` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` BIGINT(20) NOT NULL COMMENT '用户ID',
  `shop_id` BIGINT(20) DEFAULT NULL COMMENT '店铺ID',
  `shop_name` VARCHAR(100) DEFAULT NULL COMMENT '店铺名称',
  `goods_id` BIGINT(20) NOT NULL COMMENT '商品ID',
  `goods_name` VARCHAR(200) DEFAULT NULL COMMENT '商品名称',
  `goods_image` VARCHAR(255) DEFAULT NULL COMMENT '商品图片',
  `sku_id` BIGINT(20) DEFAULT NULL COMMENT 'SKU ID',
  `sku_spec` VARCHAR(200) DEFAULT NULL COMMENT 'SKU规格信息',
  `price` DECIMAL(10,2) DEFAULT 0.00 COMMENT '商品价格',
  `quantity` INT(11) DEFAULT 1 COMMENT '商品数量',
  `selected` TINYINT(1) DEFAULT 1 COMMENT '是否选中 0-未选中 1-已选中',
  `is_valid` TINYINT(1) DEFAULT 1 COMMENT '是否有效 0-失效 1-有效',
  `invalid_reason` VARCHAR(200) DEFAULT NULL COMMENT '失效原因',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` TINYINT(1) DEFAULT 0 COMMENT '是否删除 0-未删除 1-已删除',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_goods_id` (`goods_id`),
  KEY `idx_sku_id` (`sku_id`),
  KEY `idx_shop_id` (`shop_id`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='购物车表';

-- 2. 用户收货地址表
CREATE TABLE IF NOT EXISTS `hou_mall_user_address` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` BIGINT(20) NOT NULL COMMENT '用户ID',
  `receiver_name` VARCHAR(50) NOT NULL COMMENT '收货人姓名',
  `receiver_phone` VARCHAR(20) NOT NULL COMMENT '收货人电话',
  `province_code` VARCHAR(20) DEFAULT NULL COMMENT '省份编码',
  `province_name` VARCHAR(50) DEFAULT NULL COMMENT '省份名称',
  `city_code` VARCHAR(20) DEFAULT NULL COMMENT '城市编码',
  `city_name` VARCHAR(50) DEFAULT NULL COMMENT '城市名称',
  `district_code` VARCHAR(20) DEFAULT NULL COMMENT '区县编码',
  `district_name` VARCHAR(50) DEFAULT NULL COMMENT '区县名称',
  `detail_address` VARCHAR(255) NOT NULL COMMENT '详细地址',
  `address_tag` VARCHAR(20) DEFAULT NULL COMMENT '地址标签: home-家 company-公司 school-学校',
  `is_default` TINYINT(1) DEFAULT 0 COMMENT '是否默认地址 0-否 1-是',
  `post_code` VARCHAR(10) DEFAULT NULL COMMENT '邮政编码',
  `longitude` DOUBLE DEFAULT NULL COMMENT '经度',
  `latitude` DOUBLE DEFAULT NULL COMMENT '纬度',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` TINYINT(1) DEFAULT 0 COMMENT '是否删除 0-未删除 1-已删除',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_is_default` (`is_default`),
  KEY `idx_receiver_phone` (`receiver_phone`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户收货地址表';

-- 3. 商品收藏表
CREATE TABLE IF NOT EXISTS `hou_mall_goods_favorite` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` BIGINT(20) NOT NULL COMMENT '用户ID',
  `goods_id` BIGINT(20) NOT NULL COMMENT '商品ID',
  `goods_name` VARCHAR(200) DEFAULT NULL COMMENT '商品名称(冗余)',
  `goods_image` VARCHAR(255) DEFAULT NULL COMMENT '商品图片(冗余)',
  `goods_price` DECIMAL(10,2) DEFAULT NULL COMMENT '商品价格(冗余)',
  `shop_id` BIGINT(20) DEFAULT NULL COMMENT '店铺ID',
  `shop_name` VARCHAR(100) DEFAULT NULL COMMENT '店铺名称(冗余)',
  `favorite_type` TINYINT(1) DEFAULT 1 COMMENT '收藏类型 1-商品收藏 2-店铺收藏',
  `is_valid` TINYINT(1) DEFAULT 1 COMMENT '是否有效 0-失效 1-有效',
  `invalid_reason` VARCHAR(200) DEFAULT NULL COMMENT '失效原因',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` TINYINT(1) DEFAULT 0 COMMENT '是否删除 0-未删除 1-已删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_goods_type` (`user_id`, `goods_id`, `favorite_type`, `deleted`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_goods_id` (`goods_id`),
  KEY `idx_shop_id` (`shop_id`),
  KEY `idx_favorite_type` (`favorite_type`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品收藏表';

-- ================================================================
-- SQL脚本执行完成 - 核心电商功能补充
-- 共创建3张表: shopping_cart, user_address, goods_favorite
-- ================================================================


-- ---------- backend/paas/mall/src/main/resources/sql/mall_extend_tables.sql ----------
-- ================================================================
-- Mall 商城扩展表（用于 dcenter 商家管理后台）
-- 创建时间：2026-03-18
-- ================================================================

-- 1. 商品评价表
CREATE TABLE IF NOT EXISTS `hou_mall_goods_comment` (
  `comment_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '评价 ID',
  `goods_id` int(11) NOT NULL COMMENT '商品 ID',
  `user_id` bigint(20) NOT NULL COMMENT '用户ID',
  `order_id` bigint(20) DEFAULT NULL COMMENT '订单ID',
  `content` text COMMENT '评价内容',
  `star` tinyint(4) NOT NULL DEFAULT '5' COMMENT '评分星级 1-5',
  `reply_content` text COMMENT '商家回复内容',
  `reply_time` datetime DEFAULT NULL COMMENT '回复时间',
  `is_show` tinyint(1) NOT NULL DEFAULT '1' COMMENT '是否显示：0-不显示 1-显示',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`comment_id`),
  KEY `idx_goods_id` (`goods_id`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品评价表';

-- 2. 店铺装修页面表
CREATE TABLE IF NOT EXISTS `hou_mall_decorate_page` (
  `page_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '页面 ID',
  `shop_id` bigint(20) NOT NULL COMMENT '店铺 ID',
  `page_name` varchar(100) NOT NULL COMMENT '页面名称',
  `page_type` tinyint(4) NOT NULL DEFAULT '1' COMMENT '页面类型：1-首页 2-自定义页',
  `page_data` text COMMENT '页面设计数据 JSON',
  `is_home` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否设为首页：0-否 1-是',
  `status` tinyint(4) NOT NULL DEFAULT '1' COMMENT '状态：0-禁用 1-启用',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`page_id`),
  KEY `idx_shop_id` (`shop_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='店铺装修页面表';

-- 3. 文件分类表
CREATE TABLE IF NOT EXISTS `hou_mall_file_category` (
  `cate_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '分类 ID',
  `shop_id` bigint(20) DEFAULT NULL COMMENT '店铺 ID',
  `cate_name` varchar(100) NOT NULL COMMENT '分类名称',
  `sort` int(11) NOT NULL DEFAULT '0' COMMENT '排序',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`cate_id`),
  KEY `idx_shop_id` (`shop_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='文件分类表';

-- 4. 文件表
CREATE TABLE IF NOT EXISTS `hou_mall_file` (
  `file_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '文件 ID',
  `shop_id` bigint(20) DEFAULT NULL COMMENT '店铺 ID',
  `cate_id` bigint(20) DEFAULT NULL COMMENT '分类 ID',
  `file_name` varchar(255) NOT NULL COMMENT '文件名',
  `file_path` varchar(500) NOT NULL COMMENT '文件路径',
  `file_size` bigint(20) DEFAULT NULL COMMENT '文件大小 (字节)',
  `file_type` varchar(50) DEFAULT NULL COMMENT '文件类型',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`file_id`),
  KEY `idx_shop_id` (`shop_id`),
  KEY `idx_cate_id` (`cate_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='文件表';

-- 5. 系统公告表
CREATE TABLE IF NOT EXISTS `hou_mall_announcement` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '公告 ID',
  `title` varchar(200) NOT NULL COMMENT '公告标题',
  `content` text NOT NULL COMMENT '公告内容',
  `type` tinyint(4) NOT NULL DEFAULT '1' COMMENT '公告类型：1-系统通知 2-活动公告 3-规则变更',
  `is_top` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否置顶：0-否 1-是',
  `publish_time` datetime DEFAULT NULL COMMENT '发布时间',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_type` (`type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统公告表';

-- 6. 消息通知设置表
CREATE TABLE IF NOT EXISTS `hou_mall_notice_setting` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键 ID',
  `shop_id` bigint(20) DEFAULT NULL COMMENT '店铺 ID',
  `notify_type` varchar(50) NOT NULL COMMENT '通知类型：order-pay 订单支付 success 交易成功 refund 退款',
  `sms_enabled` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否启用短信：0-否 1-是',
  `app_push_enabled` tinyint(1) NOT NULL DEFAULT '1' COMMENT '是否启用 APP 推送：0-否 1-是',
  `站内信_enabled` tinyint(1) NOT NULL DEFAULT '1' COMMENT '是否启用站内信：0-否 1-是',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_shop_id` (`shop_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='消息通知设置表';

-- 7. 客服配置表
CREATE TABLE IF NOT EXISTS `hou_mall_service_config` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键 ID',
  `shop_id` bigint(20) NOT NULL COMMENT '店铺 ID',
  `service_qq` varchar(20) DEFAULT NULL COMMENT '客服 QQ',
  `service_wechat` varchar(50) DEFAULT NULL COMMENT '客服微信',
  `service_phone` varchar(20) DEFAULT NULL COMMENT '客服电话',
  `service_email` varchar(100) DEFAULT NULL COMMENT '客服邮箱',
  `service_hours` varchar(100) DEFAULT NULL COMMENT '服务时间',
  `auto_reply` text COMMENT '自动回复内容',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_shop_id` (`shop_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='客服配置表';

-- 8. 集采商品表
CREATE TABLE IF NOT EXISTS `hou_mall_gather` (
  `gather_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '集采 ID',
  `gather_name` varchar(200) NOT NULL COMMENT '集采名称',
  `shop_id` bigint(20) NOT NULL COMMENT '店铺 ID',
  `goods_id` bigint(20) NOT NULL COMMENT '商品 ID',
  `goods_name` varchar(200) NOT NULL COMMENT '商品名称',
  `gather_price` decimal(10,2) NOT NULL COMMENT '集采价格',
  `min_quantity` int(11) NOT NULL DEFAULT '1' COMMENT '最小起订量',
  `max_quantity` int(11) DEFAULT NULL COMMENT '最大采购量',
  `start_time` datetime NOT NULL COMMENT '开始时间',
  `end_time` datetime NOT NULL COMMENT '结束时间',
  `status` tinyint(4) NOT NULL DEFAULT '1' COMMENT '状态：0-待审核 1-进行中 2-已结束 3-已取消',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`gather_id`),
  KEY `idx_shop_id` (`shop_id`),
  KEY `idx_goods_id` (`goods_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='集采商品表';


-- ---------- backend/paas/mall/src/main/resources/sql/mall_store_tables.sql ----------
-- 商家与门店分离：商家是一方，门店是多方；配送与 POS 能力归属于门店。
CREATE TABLE IF NOT EXISTS `hou_mall_store` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '门店ID',
  `merchant_id` INT NOT NULL COMMENT '所属商家ID',
  `store_no` VARCHAR(32) NOT NULL COMMENT '门店编号',
  `store_name` VARCHAR(100) NOT NULL COMMENT '门店名称',
  `contact_person` VARCHAR(50) DEFAULT NULL COMMENT '门店联系人',
  `contact_phone` VARCHAR(20) DEFAULT NULL COMMENT '门店联系电话',
  `province` VARCHAR(50) DEFAULT NULL COMMENT '省份',
  `city` VARCHAR(50) DEFAULT NULL COMMENT '城市',
  `district` VARCHAR(50) DEFAULT NULL COMMENT '区县',
  `address` VARCHAR(255) DEFAULT NULL COMMENT '详细地址',
  `business_status` TINYINT NOT NULL DEFAULT 1 COMMENT '营业状态: 0-停业 1-营业',
  `rider_delivery_enabled` TINYINT NOT NULL DEFAULT 0 COMMENT '骑手配送: 0-未启用 1-已启用',
  `pos_enabled` TINYINT NOT NULL DEFAULT 0 COMMENT '收银系统: 0-未启用 1-已启用',
  `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除: 0-否 1-是',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_mall_store_no` (`store_no`),
  KEY `idx_mall_store_merchant` (`merchant_id`),
  KEY `idx_mall_store_status` (`business_status`, `deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='商家门店主数据';

-- 统一账号与商家的授权关系：账号可以先注册，再在新增门店时获得商家中心权限。
CREATE TABLE IF NOT EXISTS `hou_mall_merchant_account` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '关系ID',
  `merchant_id` INT NOT NULL COMMENT '商家ID',
  `user_id` BIGINT NOT NULL COMMENT '统一账号ID',
  `admin_id` INT NOT NULL COMMENT '商家中心管理员ID',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_mall_merchant_account_user` (`user_id`),
  UNIQUE KEY `uk_mall_merchant_account_admin` (`admin_id`),
  KEY `idx_mall_merchant_account_merchant` (`merchant_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='统一账号商家权限';

-- 给已有商家建立可识别的默认门店，确保升级后商家与门店关系完整；重复执行不会重复插入。
SET @shop_admin_user_id_exists := (SELECT COUNT(*) FROM information_schema.columns
  WHERE table_schema = DATABASE() AND table_name = 'hou_merchant_mall_shop_admin' AND column_name = 'user_id');
SET @shop_admin_user_id_ddl := IF(@shop_admin_user_id_exists = 0,
  'ALTER TABLE hou_merchant_mall_shop_admin ADD COLUMN user_id BIGINT NULL COMMENT ''关联统一会员ID''',
  'SELECT 1');
PREPARE shop_admin_user_id_stmt FROM @shop_admin_user_id_ddl;
EXECUTE shop_admin_user_id_stmt;
DEALLOCATE PREPARE shop_admin_user_id_stmt;




-- ---------- backend/paas/permission/src/main/resources/db/permission-center-rbac.sql ----------
-- 权限中心 21 个核心权限码的幂等修复。
-- 所有父节点均按 perms 定位，不依赖固定自增 ID。






















-- 使用派生表读取父 ID，兼容 MySQL 对“更新并读取同一表”的限制。








-- 管理员按角色名称识别，不依赖 role_id=1；普通角色不会被自动授权。

-- 迁移后核查：以下结果应依次为 21 个核心权限码、0 个重复权限码、0 个错误父子关系。
SELECT perms, type, pid, name, paths, component
FROM hou_permission_menu
WHERE perms IN (
  'shell:setting:permission',
  'shell:setting:permission:menu',
  'shell:setting:permission:menu:add',
  'shell:setting:permission:menu:edit',
  'shell:setting:permission:menu:delete',
  'shell:setting:permission:menu:enable',
  'shell:setting:permission:menu:disable',
  'shell:setting:permission:role',
  'shell:setting:permission:role:add',
  'shell:setting:permission:role:edit',
  'shell:setting:permission:role:delete',
  'shell:setting:permission:role:assign',
  'shell:setting:permission:user',
  'shell:setting:permission:user:add',
  'shell:setting:permission:user:edit',
  'shell:setting:permission:user:delete',
  'shell:setting:permission:user:reset',
  'shell:setting:permission:user:assign',
  'shell:setting:permission:user:enable',
  'shell:setting:permission:user:disable',
  'shell:setting:permission:cache:clear'
)
ORDER BY perms;

SELECT perms, COUNT(*) AS duplicate_count
FROM hou_permission_menu
WHERE perms = 'shell:setting:permission'
   OR perms LIKE 'shell:setting:permission:%'
GROUP BY perms
HAVING COUNT(*) > 1;

SELECT child.perms, child.pid, parent.perms AS actual_parent
FROM hou_permission_menu child
LEFT JOIN hou_permission_menu parent ON parent.id = child.pid
WHERE child.perms = 'shell:setting:permission:user'
   OR child.perms LIKE 'shell:setting:permission:user:%'
ORDER BY child.perms;

SELECT r.id AS role_id, r.name AS role_name, COUNT(DISTINCT menu.id) AS permission_count
FROM hou_permission_role r
JOIN hou_permission_role_menu role_menu ON role_menu.role_id = r.id
JOIN hou_permission_menu menu ON menu.id = role_menu.menu_id
WHERE r.delete_time IS NULL
  AND LOWER(TRIM(r.name)) IN ('admin', 'super_admin', '管理员', '超级管理员')
  AND (menu.perms = 'shell:setting:permission'
       OR menu.perms LIKE 'shell:setting:permission:%')
GROUP BY r.id, r.name;


-- ---------- backend/paas/permission/src/main/resources/db/permission-plugin-menu-groups.sql ----------
-- 插件一级菜单分组根种子（宿主资产）。
-- 方案：《开源插件化架构/插件菜单治理方案.md》§5.1/§5.2。
-- 幂等：NOT EXISTS 防重插 + UPDATE 修复漂移；可重复执行。
--
-- 关键约定：
-- 1. params = 'host:menu-group' 是宿主归属标记，
--    PluginMenuSyncPort.ownershipPredicate 已排除该标记 → 插件卸载永不触碰分组根；
-- 2. perms = 'shell:group:{key}' 仅作稳定标识，不授予任何角色——
--    用户可见性由组内子菜单授权 + MenuAncestorSupport 祖先补全决定；
-- 3. 分组 key/sort 与 plugin-registry 的 MenuGroupRegistry 由契约测试锁定一致；
-- 4. 空分组由前端自动隐藏，后端不做空组清理。

-- group: biz|业务经营|3800|Shop

-- group: growth|营销增长|3700|Promotion

-- group: org|组织协同|3600|User

-- group: fin|财务合规|3500|Money

-- group: ai|智能工具|3400|Cpu

-- group: other|其他应用|3300|Grid

-- 漂移修复：分组根已存在但关键属性被改动时拉回规范值（幂等）。


-- ---------- backend/paas/plugin-artifact/src/main/resources/sql/plugin_artifact_tables.sql ----------
-- =====================================================================
--  HouCloud 线上插件目录与分发 —— DDL（M0 地基 + M1 上传 + M3 补列与修键）
--  文件：plugin-artifact/src/main/resources/sql/plugin_artifact_tables.sql
--
--  ★幂等性说明（重要）：
--    本文件**可以反复执行**，每次执行结果一致，不会产生「列已存在 / 索引已存在」报错。
--    实现手法：MySQL 原生不支持 `ADD COLUMN IF NOT EXISTS` / `DROP INDEX IF EXISTS`，
--    因此统一用 information_schema 判断存在性，再拼动态 SQL 交给 PREPARE/EXECUTE 执行；
--    不存在需要变更时才执行 DDL，否则执行 `DO 0`（空操作）。
--    建表一律 `CREATE TABLE IF NOT EXISTS`。
--
--  执行方式：
--    1) 先 `USE <目标库>;`（脚本内所有判断都基于 `DATABASE()` 当前库），再整体执行本文件；
--    2) 生产执行前请备份涉及的表（hou_addon_plugin_migration / hou_market_plugin_version）；
--    3) 项目当前未引入 Flyway/Liquibase，本文件由人工在需要时执行，不做自动迁移。
--
--  ⚠️ 版本要求（仅第六段）：
--    第六段用「生成列 + 唯一索引」修 uk_idem，需要 MySQL 5.7+ / MariaDB 10.2+。
--    脚本会自行探测版本，不满足时保留旧键并打印告警，不会报错中断、也不会留下无互斥的半成品。
--
--  ⚠️ 部署顺序（M1 新增）：
--    本文件会为 `hou_market_plugin_version` 增加 12 列，而 `MarketPluginVersion` 实体
--    已同步新增这些字段。MyBatis-Plus 生成的 SELECT 会带上新列，
--    因此**必须先执行本文件再发布 mall**，否则 PluginMarketService 查询会因缺列报错。
-- =====================================================================

SET @hou_schema := DATABASE();


-- =====================================================================
--  一、改键：hou_addon_plugin_migration（已存在，改唯一键 + 扩 5 列）
--
--  原因：原唯一键 uk_plugin_version(plugin_code, version) 的语义是
--        「同一版本只允许一条成功记录」，无法表达三种不同动作：
--          install  : 空 -> 1.0.0
--          upgrade  : 1.0.0 -> 1.1.0
--          rollback : 1.1.0 -> 1.0.0
--        upgrade 与 rollback 会复用同一个 to_version（升上去再回滚回来），
--        旧唯一键会把第二次操作直接卡死。
--  做法：唯一键改为 uk_mig_op(plugin_code, version, operation, from_version)，
--        并补充 operation / from_version / operator_id / duration_ms / rollback_of 五列。
--
--  from_version 为什么是 NOT NULL DEFAULT ''：
--        MySQL 唯一键中 NULL 值互不冲突，若允许 NULL，则 operation=1(install)
--        的重复插入无法被 uk_mig_op 拦截，唯一键形同虚设。
--        因此 install 统一写空串，保证唯一键真正生效。
--        原建表语句位于 plugin-registry/.../plugin_addon_tables.sql，
--        为保证已有环境建表语义不被改动，这里不修改原文件，只追加幂等 ALTER。
-- =====================================================================

-- 1.1 删除旧唯一键 uk_plugin_version（存在才删）
SET @v_exists := (SELECT COUNT(*) FROM information_schema.statistics
                  WHERE table_schema = @hou_schema
                    AND table_name = 'hou_addon_plugin_migration'
                    AND index_name = 'uk_plugin_version');
SET @v_ddl := IF(@v_exists > 0,
                 'ALTER TABLE hou_addon_plugin_migration DROP INDEX uk_plugin_version',
                 'DO 0');
PREPARE hou_stmt FROM @v_ddl; EXECUTE hou_stmt; DEALLOCATE PREPARE hou_stmt;

-- 1.2 operation：1=install 2=upgrade 3=rollback
SET @v_exists := (SELECT COUNT(*) FROM information_schema.columns
                  WHERE table_schema = @hou_schema
                    AND table_name = 'hou_addon_plugin_migration'
                    AND column_name = 'operation');
SET @v_ddl := IF(@v_exists = 0,
                 'ALTER TABLE hou_addon_plugin_migration ADD COLUMN operation TINYINT NOT NULL DEFAULT 1 COMMENT ''1=install 2=upgrade 3=rollback''',
                 'DO 0');
PREPARE hou_stmt FROM @v_ddl; EXECUTE hou_stmt; DEALLOCATE PREPARE hou_stmt;

-- 1.3 from_version：来源版本，install 时写空串（NOT NULL 才能被唯一键约束）
SET @v_exists := (SELECT COUNT(*) FROM information_schema.columns
                  WHERE table_schema = @hou_schema
                    AND table_name = 'hou_addon_plugin_migration'
                    AND column_name = 'from_version');
SET @v_ddl := IF(@v_exists = 0,
                 'ALTER TABLE hou_addon_plugin_migration ADD COLUMN from_version VARCHAR(32) NOT NULL DEFAULT '''' COMMENT ''来源版本（install 时为空串）''',
                 'DO 0');
PREPARE hou_stmt FROM @v_ddl; EXECUTE hou_stmt; DEALLOCATE PREPARE hou_stmt;

-- 1.4 operator_id：操作人
SET @v_exists := (SELECT COUNT(*) FROM information_schema.columns
                  WHERE table_schema = @hou_schema
                    AND table_name = 'hou_addon_plugin_migration'
                    AND column_name = 'operator_id');
SET @v_ddl := IF(@v_exists = 0,
                 'ALTER TABLE hou_addon_plugin_migration ADD COLUMN operator_id BIGINT NULL COMMENT ''操作人''',
                 'DO 0');
PREPARE hou_stmt FROM @v_ddl; EXECUTE hou_stmt; DEALLOCATE PREPARE hou_stmt;

-- 1.5 duration_ms：迁移耗时，慢 DDL 排查
SET @v_exists := (SELECT COUNT(*) FROM information_schema.columns
                  WHERE table_schema = @hou_schema
                    AND table_name = 'hou_addon_plugin_migration'
                    AND column_name = 'duration_ms');
SET @v_ddl := IF(@v_exists = 0,
                 'ALTER TABLE hou_addon_plugin_migration ADD COLUMN duration_ms INT NULL COMMENT ''迁移耗时，慢 DDL 排查''',
                 'DO 0');
PREPARE hou_stmt FROM @v_ddl; EXECUTE hou_stmt; DEALLOCATE PREPARE hou_stmt;

-- 1.6 rollback_of：回滚自哪次迁移 ID
SET @v_exists := (SELECT COUNT(*) FROM information_schema.columns
                  WHERE table_schema = @hou_schema
                    AND table_name = 'hou_addon_plugin_migration'
                    AND column_name = 'rollback_of');
SET @v_ddl := IF(@v_exists = 0,
                 'ALTER TABLE hou_addon_plugin_migration ADD COLUMN rollback_of BIGINT NULL COMMENT ''回滚自哪次迁移ID''',
                 'DO 0');
PREPARE hou_stmt FROM @v_ddl; EXECUTE hou_stmt; DEALLOCATE PREPARE hou_stmt;

-- 1.7 存量数据回填：历史行 from_version 可能为 NULL，统一改成空串

-- 1.8 新唯一键 uk_mig_op（不存在才建；必须在列补齐之后）
SET @v_exists := (SELECT COUNT(*) FROM information_schema.statistics
                  WHERE table_schema = @hou_schema
                    AND table_name = 'hou_addon_plugin_migration'
                    AND index_name = 'uk_mig_op');
SET @v_ddl := IF(@v_exists = 0,
                 'ALTER TABLE hou_addon_plugin_migration ADD UNIQUE KEY uk_mig_op (plugin_code, version, operation, from_version)',
                 'DO 0');
PREPARE hou_stmt FROM @v_ddl; EXECUTE hou_stmt; DEALLOCATE PREPARE hou_stmt;


-- =====================================================================
--  二、扩列：hou_market_plugin_version（已存在，扩 12 列 —— 方案 §3.2）
--
--  这些列是 M1 上传链路的落点：storage_* 指向不可变存储身份（与可变的 jar_url 分离）、
--  jar_sha256 + signature_kid 支撑防篡改与密钥轮换、upload_status/upload_id 支撑上传幂等。
--  状态取值沿用现有代码：0=draft、1=published、3=pending、4=rejected、5=offline。
-- =====================================================================

-- 2.1 storage_type
SET @v_exists := (SELECT COUNT(*) FROM information_schema.columns
                  WHERE table_schema = @hou_schema AND table_name = 'hou_market_plugin_version'
                    AND column_name = 'storage_type');
SET @v_ddl := IF(@v_exists = 0,
                 'ALTER TABLE hou_market_plugin_version ADD COLUMN storage_type VARCHAR(16) NOT NULL DEFAULT ''LOCAL'' COMMENT ''LOCAL/OSS/COS''',
                 'DO 0');
PREPARE hou_stmt FROM @v_ddl; EXECUTE hou_stmt; DEALLOCATE PREPARE hou_stmt;

-- 2.2 storage_key：不可变存储身份（jar_url 是可变网关地址，两者必须分开）
SET @v_exists := (SELECT COUNT(*) FROM information_schema.columns
                  WHERE table_schema = @hou_schema AND table_name = 'hou_market_plugin_version'
                    AND column_name = 'storage_key');
SET @v_ddl := IF(@v_exists = 0,
                 'ALTER TABLE hou_market_plugin_version ADD COLUMN storage_key VARCHAR(512) NULL COMMENT ''存储对象键（与 jar_url 分离：jar_url 可变，key 才是不可变身份）''',
                 'DO 0');
PREPARE hou_stmt FROM @v_ddl; EXECUTE hou_stmt; DEALLOCATE PREPARE hou_stmt;

-- 2.3 jar_sha256
SET @v_exists := (SELECT COUNT(*) FROM information_schema.columns
                  WHERE table_schema = @hou_schema AND table_name = 'hou_market_plugin_version'
                    AND column_name = 'jar_sha256');
SET @v_ddl := IF(@v_exists = 0,
                 'ALTER TABLE hou_market_plugin_version ADD COLUMN jar_sha256 VARCHAR(64) NULL COMMENT ''制品 SHA-256（md5 仅防误传，sha256+签名防篡改）''',
                 'DO 0');
PREPARE hou_stmt FROM @v_ddl; EXECUTE hou_stmt; DEALLOCATE PREPARE hou_stmt;

-- 2.4 signature_kid：签名密钥 ID，密钥轮换必需
SET @v_exists := (SELECT COUNT(*) FROM information_schema.columns
                  WHERE table_schema = @hou_schema AND table_name = 'hou_market_plugin_version'
                    AND column_name = 'signature_kid');
SET @v_ddl := IF(@v_exists = 0,
                 'ALTER TABLE hou_market_plugin_version ADD COLUMN signature_kid VARCHAR(64) NULL COMMENT ''签名密钥ID，密钥轮换必需''',
                 'DO 0');
PREPARE hou_stmt FROM @v_ddl; EXECUTE hou_stmt; DEALLOCATE PREPARE hou_stmt;

-- 2.5 manifest_sha256：plugin.yaml 摘要，防清单被掉包
SET @v_exists := (SELECT COUNT(*) FROM information_schema.columns
                  WHERE table_schema = @hou_schema AND table_name = 'hou_market_plugin_version'
                    AND column_name = 'manifest_sha256');
SET @v_ddl := IF(@v_exists = 0,
                 'ALTER TABLE hou_market_plugin_version ADD COLUMN manifest_sha256 VARCHAR(64) NULL COMMENT ''plugin.yaml 摘要，防清单被掉包''',
                 'DO 0');
PREPARE hou_stmt FROM @v_ddl; EXECUTE hou_stmt; DEALLOCATE PREPARE hou_stmt;

-- 2.6 upload_status：0=上传中 1=完成 2=失败
SET @v_exists := (SELECT COUNT(*) FROM information_schema.columns
                  WHERE table_schema = @hou_schema AND table_name = 'hou_market_plugin_version'
                    AND column_name = 'upload_status');
SET @v_ddl := IF(@v_exists = 0,
                 'ALTER TABLE hou_market_plugin_version ADD COLUMN upload_status TINYINT NOT NULL DEFAULT 0 COMMENT ''0=上传中 1=完成 2=失败''',
                 'DO 0');
PREPARE hou_stmt FROM @v_ddl; EXECUTE hou_stmt; DEALLOCATE PREPARE hou_stmt;

-- 2.7 upload_id：客户端幂等键 clientOpId
SET @v_exists := (SELECT COUNT(*) FROM information_schema.columns
                  WHERE table_schema = @hou_schema AND table_name = 'hou_market_plugin_version'
                    AND column_name = 'upload_id');
SET @v_ddl := IF(@v_exists = 0,
                 'ALTER TABLE hou_market_plugin_version ADD COLUMN upload_id VARCHAR(64) NULL COMMENT ''客户端幂等键 clientOpId''',
                 'DO 0');
PREPARE hou_stmt FROM @v_ddl; EXECUTE hou_stmt; DEALLOCATE PREPARE hou_stmt;

-- 2.8 reviewer_id
SET @v_exists := (SELECT COUNT(*) FROM information_schema.columns
                  WHERE table_schema = @hou_schema AND table_name = 'hou_market_plugin_version'
                    AND column_name = 'reviewer_id');
SET @v_ddl := IF(@v_exists = 0,
                 'ALTER TABLE hou_market_plugin_version ADD COLUMN reviewer_id BIGINT NULL COMMENT ''审核人''',
                 'DO 0');
PREPARE hou_stmt FROM @v_ddl; EXECUTE hou_stmt; DEALLOCATE PREPARE hou_stmt;

-- 2.9 submit_time
SET @v_exists := (SELECT COUNT(*) FROM information_schema.columns
                  WHERE table_schema = @hou_schema AND table_name = 'hou_market_plugin_version'
                    AND column_name = 'submit_time');
SET @v_ddl := IF(@v_exists = 0,
                 'ALTER TABLE hou_market_plugin_version ADD COLUMN submit_time DATETIME NULL COMMENT ''提交审核时间''',
                 'DO 0');
PREPARE hou_stmt FROM @v_ddl; EXECUTE hou_stmt; DEALLOCATE PREPARE hou_stmt;

-- 2.10 offline_time
SET @v_exists := (SELECT COUNT(*) FROM information_schema.columns
                  WHERE table_schema = @hou_schema AND table_name = 'hou_market_plugin_version'
                    AND column_name = 'offline_time');
SET @v_ddl := IF(@v_exists = 0,
                 'ALTER TABLE hou_market_plugin_version ADD COLUMN offline_time DATETIME NULL COMMENT ''下架时间''',
                 'DO 0');
PREPARE hou_stmt FROM @v_ddl; EXECUTE hou_stmt; DEALLOCATE PREPARE hou_stmt;

-- 2.11 offline_reason
SET @v_exists := (SELECT COUNT(*) FROM information_schema.columns
                  WHERE table_schema = @hou_schema AND table_name = 'hou_market_plugin_version'
                    AND column_name = 'offline_reason');
SET @v_ddl := IF(@v_exists = 0,
                 'ALTER TABLE hou_market_plugin_version ADD COLUMN offline_reason VARCHAR(255) NULL COMMENT ''下架原因''',
                 'DO 0');
PREPARE hou_stmt FROM @v_ddl; EXECUTE hou_stmt; DEALLOCATE PREPARE hou_stmt;

-- 2.12 version_download_count：版本级下载埋点
SET @v_exists := (SELECT COUNT(*) FROM information_schema.columns
                  WHERE table_schema = @hou_schema AND table_name = 'hou_market_plugin_version'
                    AND column_name = 'version_download_count');
SET @v_ddl := IF(@v_exists = 0,
                 'ALTER TABLE hou_market_plugin_version ADD COLUMN version_download_count INT NOT NULL DEFAULT 0 COMMENT ''版本级下载埋点''',
                 'DO 0');
PREPARE hou_stmt FROM @v_ddl; EXECUTE hou_stmt; DEALLOCATE PREPARE hou_stmt;

-- 2.13 索引：idx_version_status（不存在才建）
SET @v_exists := (SELECT COUNT(*) FROM information_schema.statistics
                  WHERE table_schema = @hou_schema AND table_name = 'hou_market_plugin_version'
                    AND index_name = 'idx_version_status');
SET @v_ddl := IF(@v_exists = 0,
                 'ALTER TABLE hou_market_plugin_version ADD KEY idx_version_status (plugin_code, status)',
                 'DO 0');
PREPARE hou_stmt FROM @v_ddl; EXECUTE hou_stmt; DEALLOCATE PREPARE hou_stmt;

-- 2.14 唯一键：uk_version_upload（不存在才建；upload_id 为 NULL 的历史行不受影响）
SET @v_exists := (SELECT COUNT(*) FROM information_schema.statistics
                  WHERE table_schema = @hou_schema AND table_name = 'hou_market_plugin_version'
                    AND index_name = 'uk_version_upload');
SET @v_ddl := IF(@v_exists = 0,
                 'ALTER TABLE hou_market_plugin_version ADD UNIQUE KEY uk_version_upload (plugin_code, upload_id)',
                 'DO 0');
PREPARE hou_stmt FROM @v_ddl; EXECUTE hou_stmt; DEALLOCATE PREPARE hou_stmt;


-- =====================================================================
--  三、新增表
-- =====================================================================

-- 3.6 hou_plugin_artifact —— 制品权威 + 上传幂等锁（新增）
-- artifact_id = sha256(code|version|md5) 的完整 64 位十六进制，是制品的不可变身份；
-- 早期方案只取 40 位截断，存在碰撞风险，字段长度本身成本为零，故改存完整摘要。
-- upload_id 为客户端幂等键，重复提交同一 upload_id 直接返回首次结果，避免重复写盘。
CREATE TABLE IF NOT EXISTS hou_plugin_artifact (
  id            BIGINT AUTO_INCREMENT PRIMARY KEY,
  artifact_id   VARCHAR(64)  NOT NULL COMMENT '制品ID = sha256(code|version|md5) 完整十六进制，不可变身份',
  plugin_code   VARCHAR(64)  NOT NULL COMMENT '插件编码',
  version       VARCHAR(32)  NOT NULL COMMENT '插件版本',
  storage_type  VARCHAR(16)  NOT NULL COMMENT '存储类型：local/cos',
  storage_key   VARCHAR(512) NOT NULL COMMENT '存储键 {code}/{version}/{code}-{version}.jar',
  md5           VARCHAR(64)  NOT NULL COMMENT '制品 md5（小写十六进制）',
  sha256        VARCHAR(64)  NOT NULL COMMENT '制品 sha256（小写十六进制）',
  size_bytes    BIGINT       NOT NULL COMMENT '制品字节数',
  upload_status TINYINT      NOT NULL DEFAULT 0 COMMENT '0=上传中 1=已完成 2=已作废',
  upload_id     VARCHAR(64)  NOT NULL COMMENT '客户端幂等键',
  created_by    BIGINT       NULL COMMENT '上传人',
  create_time   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  UNIQUE KEY uk_artifact_id (artifact_id),
  UNIQUE KEY uk_upload_id (upload_id),
  KEY idx_code_version (plugin_code, version)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='插件制品元信息与上传幂等（新增）';

-- 3.6.1 签名 kid 扩列：实体 PluginArtifact.signature_kid 与
--      scripts/sql/migrations/20260922-plugin-artifact-signature-kid.sql 对齐。
--      曾漏装进建库脚本：全新安装的库没有这一列，上传制品时
--      Unknown column 'signature_kid' in 'field list'（2026-10-27 第 12 轮实测）。
--      幂等：列已存在时执行 SELECT 1。老库跑过 20260922 迁移的同理跳过。
--      🔴 不用 AFTER source_license：该列要到 30-commercial-delivery.sql 才加，
--      而 source 链遇 ERROR 1054 会中止、后面所有种子文件全部不执行
--      （2026-10-28 第四轮 purge 重装实测：admin 因此不进库，卡在"容器全绿但无法登录"）。
--      列追加到表尾，业务全部按列名取数，位置无关。
SET @v_exists := (SELECT COUNT(*) FROM information_schema.columns
                  WHERE table_schema = DATABASE() AND table_name = 'hou_plugin_artifact'
                    AND column_name = 'signature_kid');
SET @v_ddl := IF(@v_exists = 0,
                 'ALTER TABLE hou_plugin_artifact ADD COLUMN signature_kid VARCHAR(64) NULL COMMENT ''Ed25519 签名密钥 ID''',
                 'SELECT 1');
PREPARE hou_stmt FROM @v_ddl; EXECUTE hou_stmt; DEALLOCATE PREPARE hou_stmt;

-- 3.7 hou_plugin_update_task —— 安装/升级/回滚任务（新增）
-- 幂等：uk_idem(instance_id, plugin_code, op_type, to_version, state)
--       重复点击不产生第二条任务；state 进入终态后同一组合才允许再建一条。
-- 断点：step 记录当前阶段，失败重启后从该阶段续做，不必从下载重来。
CREATE TABLE IF NOT EXISTS hou_plugin_update_task (
  id           BIGINT AUTO_INCREMENT PRIMARY KEY,
  task_id      VARCHAR(40)  NOT NULL COMMENT '服务端生成',
  instance_id  VARCHAR(128) NOT NULL COMMENT '站点实例（执行主体）',
  plugin_code  VARCHAR(64)  NOT NULL COMMENT '插件编码',
  op_type      VARCHAR(16)  NOT NULL COMMENT 'INSTALL/UPGRADE/ROLLBACK',
  from_version VARCHAR(32)  NULL COMMENT '来源版本（INSTALL 时为 NULL）',
  to_version   VARCHAR(32)  NOT NULL COMMENT '目标版本',
  state        VARCHAR(16)  NOT NULL COMMENT 'PENDING/RUNNING/SUCCESS/ROLLED_BACK/FAILED',
  step         VARCHAR(32)  NULL COMMENT 'DOWNLOAD/VERIFY/COMPAT/BACKUP/STOP/SCHEMA/START/HEALTH',
  backup_path  VARCHAR(512) NULL COMMENT '备份版本目录，恢复用',
  error_code   VARCHAR(32)  NULL COMMENT '错误码（3001-3018 体系）',
  error_msg    VARCHAR(1024) NULL COMMENT '错误描述',
  operator_id  BIGINT       NULL COMMENT '操作人',
  start_time   DATETIME     NULL COMMENT '开始时间',
  end_time     DATETIME     NULL COMMENT '结束时间',
  UNIQUE KEY uk_idem (instance_id, plugin_code, op_type, to_version, state) COMMENT '幂等：重复点击不产生第二条任务',
  UNIQUE KEY uk_task_id (task_id),
  KEY idx_inst_state (instance_id, state)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='插件安装/升级/回滚任务（新增）';

-- 3.8 hou_plugin_download_token —— 下载链接签发留痕（新增）
-- max_hits 设计成「限次」而非「一次性」：断点续传/网络重试会多次命中同一 token，
-- 一次性会直接把正常的重试打死。nonce 受 uk_nonce 唯一约束实现防重放，落库用于事后审计溯源。
-- 数据保留建议：本表只用于泄露后的溯源追责，不参与任何业务流程，
--   ★建议保留 90 天★（M3 不实现定期清理，数据量上涨后由运维按此策略归档/清理）。
CREATE TABLE IF NOT EXISTS hou_plugin_download_token (
  id          BIGINT AUTO_INCREMENT PRIMARY KEY,
  token_id    VARCHAR(40) NOT NULL COMMENT '令牌ID',
  nonce       VARCHAR(32) NOT NULL COMMENT '防重放随机串（Redis SETNX 原子占用）',
  instance_id VARCHAR(128) NOT NULL COMMENT '站点实例',
  user_id     BIGINT NULL COMMENT '下载用户',
  plugin_code VARCHAR(64) NOT NULL COMMENT '插件编码',
  version     VARCHAR(32) NOT NULL COMMENT '插件版本',
  artifact_id VARCHAR(64) NOT NULL COMMENT '制品ID（长度与 hou_plugin_artifact.artifact_id 对齐：完整 sha256 十六进制）',
  issued_at   DATETIME NOT NULL COMMENT '签发时间',
  expires_at  DATETIME NOT NULL COMMENT '过期时间',
  max_hits    INT NOT NULL DEFAULT 5 COMMENT '限次而非一次：兼容断点续传/重试',
  hits        INT NOT NULL DEFAULT 0 COMMENT '已命中次数',
  client_ip   VARCHAR(64) NULL COMMENT '客户端 IP，泄露溯源',
  user_agent  VARCHAR(255) NULL COMMENT '客户端 UA，泄露溯源',
  UNIQUE KEY uk_token_id (token_id),
  UNIQUE KEY uk_nonce (nonce),
  KEY idx_inst_code (instance_id, plugin_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='下载链接签发留痕（新增）';


-- =====================================================================
--  四、扩列：hou_market_plugin（已存在，扩 3 列 —— 方案 §3.1「插件目录」）
--
--  这三列把「插件商品」从「平台自营」扩展到「第三方可发布」：
--    visibility   —— 谁能看见（私有 / 公开 / 指定租户），目录页按它过滤；
--    publisher_id —— 发布者（第三方开发者账号），用于「我的发布」列表与责任溯源；
--    source_repo  —— 源码仓库地址，审核时人工复核用。
--
--  ⚠️ M2 只落列、不落 Java 字段：MarketPlugin 实体暂不新增这三个属性，
--     否则 MyBatis-Plus 的 SELECT 会带上新列，形成「先发版还是先执行 SQL」的部署顺序耦合。
--     等 M3 目录页真正要读写它们时再一次性补实体 + 门控。
-- =====================================================================

-- 4.1 visibility：0=私有 1=公开 2=指定租户
SET @v_exists := (SELECT COUNT(*) FROM information_schema.columns
                  WHERE table_schema = @hou_schema AND table_name = 'hou_market_plugin'
                    AND column_name = 'visibility');
SET @v_ddl := IF(@v_exists = 0,
                 'ALTER TABLE hou_market_plugin ADD COLUMN visibility TINYINT NOT NULL DEFAULT 1 COMMENT ''0=私有 1=公开 2=指定租户''',
                 'DO 0');
PREPARE hou_stmt FROM @v_ddl; EXECUTE hou_stmt; DEALLOCATE PREPARE hou_stmt;

-- 4.2 publisher_id：发布者（第三方开发者账号）
SET @v_exists := (SELECT COUNT(*) FROM information_schema.columns
                  WHERE table_schema = @hou_schema AND table_name = 'hou_market_plugin'
                    AND column_name = 'publisher_id');
SET @v_ddl := IF(@v_exists = 0,
                 'ALTER TABLE hou_market_plugin ADD COLUMN publisher_id BIGINT NULL COMMENT ''发布者（第三方开发者）''',
                 'DO 0');
PREPARE hou_stmt FROM @v_ddl; EXECUTE hou_stmt; DEALLOCATE PREPARE hou_stmt;

-- 4.3 source_repo：源码仓库地址，审核时人工复核用
SET @v_exists := (SELECT COUNT(*) FROM information_schema.columns
                  WHERE table_schema = @hou_schema AND table_name = 'hou_market_plugin'
                    AND column_name = 'source_repo');
SET @v_ddl := IF(@v_exists = 0,
                 'ALTER TABLE hou_market_plugin ADD COLUMN source_repo VARCHAR(255) NULL COMMENT ''源码仓库地址''',
                 'DO 0');
PREPARE hou_stmt FROM @v_ddl; EXECUTE hou_stmt; DEALLOCATE PREPARE hou_stmt;

-- 4.4 索引：idx_visibility（目录页按可见性过滤，不存在才建）
SET @v_exists := (SELECT COUNT(*) FROM information_schema.statistics
                  WHERE table_schema = @hou_schema AND table_name = 'hou_market_plugin'
                    AND index_name = 'idx_visibility');
SET @v_ddl := IF(@v_exists = 0,
                 'ALTER TABLE hou_market_plugin ADD KEY idx_visibility (visibility, status)',
                 'DO 0');
PREPARE hou_stmt FROM @v_ddl; EXECUTE hou_stmt; DEALLOCATE PREPARE hou_stmt;


-- =====================================================================
--  五、扩列：hou_addon_plugin_install（已存在，扩 5 列 —— 方案 §3.5）
--
--  这五列是「插件安装态」从静态快照升级为「可编排」的落点：
--    update_status      —— 非终态互斥锁（0 空闲 / 1 安装中 / 2 升级中 / 3 回滚中 / 4 上次失败），
--                          M4 的并发互斥与「排空窗口」都读它；
--    update_started_at  —— 上锁时间，M4 的孤儿任务扫描靠它判断「锁了太久 = 进程死了」；
--    previous_version   —— 回滚锚点：升级成功后它指向升级前版本，回滚不是猜出来的；
--    upgradable_version —— 缓存「线上存在的最新更高版本」，避免目录页每次都回源算；
--    last_error         —— 最近一次失败原因，前端不必再查任务表就能展示「上次为什么失败」。
--
--  ⚠️ M3 只落列、不落 Java 字段：MallPluginInstall 实体暂不新增这五个属性，
--     否则 MyBatis-Plus 的 SELECT 会带上新列，形成「先发版还是先执行 SQL」的部署顺序耦合
--     （与第四节 hou_market_plugin 三列同样的取舍）。M4 实现升级/回滚时再一次性补实体。
-- =====================================================================

-- 5.1 update_status：0=空闲 1=安装中 2=升级中 3=回滚中 4=上次失败
SET @v_exists := (SELECT COUNT(*) FROM information_schema.columns
                  WHERE table_schema = @hou_schema AND table_name = 'hou_addon_plugin_install'
                    AND column_name = 'update_status');
SET @v_ddl := IF(@v_exists = 0,
                 'ALTER TABLE hou_addon_plugin_install ADD COLUMN update_status TINYINT NOT NULL DEFAULT 0 COMMENT ''0=空闲 1=安装中 2=升级中 3=回滚中 4=上次失败''',
                 'DO 0');
PREPARE hou_stmt FROM @v_ddl; EXECUTE hou_stmt; DEALLOCATE PREPARE hou_stmt;

-- 5.2 update_started_at：上锁时间，孤儿任务超时解锁用
SET @v_exists := (SELECT COUNT(*) FROM information_schema.columns
                  WHERE table_schema = @hou_schema AND table_name = 'hou_addon_plugin_install'
                    AND column_name = 'update_started_at');
SET @v_ddl := IF(@v_exists = 0,
                 'ALTER TABLE hou_addon_plugin_install ADD COLUMN update_started_at DATETIME NULL COMMENT ''本轮更新开始时间，用于识别孤儿任务超时解锁''',
                 'DO 0');
PREPARE hou_stmt FROM @v_ddl; EXECUTE hou_stmt; DEALLOCATE PREPARE hou_stmt;

-- 5.3 previous_version：升级前版本，回滚锚点
SET @v_exists := (SELECT COUNT(*) FROM information_schema.columns
                  WHERE table_schema = @hou_schema AND table_name = 'hou_addon_plugin_install'
                    AND column_name = 'previous_version');
SET @v_ddl := IF(@v_exists = 0,
                 'ALTER TABLE hou_addon_plugin_install ADD COLUMN previous_version VARCHAR(32) NULL COMMENT ''升级前版本，回滚锚点''',
                 'DO 0');
PREPARE hou_stmt FROM @v_ddl; EXECUTE hou_stmt; DEALLOCATE PREPARE hou_stmt;

-- 5.4 upgradable_version：缓存线上存在的最新更高版本
SET @v_exists := (SELECT COUNT(*) FROM information_schema.columns
                  WHERE table_schema = @hou_schema AND table_name = 'hou_addon_plugin_install'
                    AND column_name = 'upgradable_version');
SET @v_ddl := IF(@v_exists = 0,
                 'ALTER TABLE hou_addon_plugin_install ADD COLUMN upgradable_version VARCHAR(32) NULL COMMENT ''缓存：线上存在的最新更高版本''',
                 'DO 0');
PREPARE hou_stmt FROM @v_ddl; EXECUTE hou_stmt; DEALLOCATE PREPARE hou_stmt;

-- 5.5 last_error：最近一次失败原因
SET @v_exists := (SELECT COUNT(*) FROM information_schema.columns
                  WHERE table_schema = @hou_schema AND table_name = 'hou_addon_plugin_install'
                    AND column_name = 'last_error');
SET @v_ddl := IF(@v_exists = 0,
                 'ALTER TABLE hou_addon_plugin_install ADD COLUMN last_error VARCHAR(1024) NULL COMMENT ''最近一次更新失败原因''',
                 'DO 0');
PREPARE hou_stmt FROM @v_ddl; EXECUTE hou_stmt; DEALLOCATE PREPARE hou_stmt;

-- 5.6 索引：idx_update_status（M4 孤儿任务扫描按 update_status + update_started_at 扫，不存在才建）
SET @v_exists := (SELECT COUNT(*) FROM information_schema.statistics
                  WHERE table_schema = @hou_schema AND table_name = 'hou_addon_plugin_install'
                    AND index_name = 'idx_update_status');
SET @v_ddl := IF(@v_exists = 0,
                 'ALTER TABLE hou_addon_plugin_install ADD KEY idx_update_status (update_status, update_started_at)',
                 'DO 0');
PREPARE hou_stmt FROM @v_ddl; EXECUTE hou_stmt; DEALLOCATE PREPARE hou_stmt;


-- =====================================================================
--  六、修键：hou_plugin_update_task 的 uk_idem —— 语义与设计意图相反
--
--  【原键】UNIQUE KEY uk_idem (instance_id, plugin_code, op_type, to_version, state)
--  【真实语义】MySQL 唯一键是「值全部相同才冲突」，state 进了键里，
--             于是真实约束变成「同一组合的**同一状态**只能有一条」：
--               · 非终态互斥：成立（PENDING / RUNNING 各自唯一，第二条插不进来 → 3016）；
--               · 终态历史：❌ 反了——第二次成功安装置 SUCCESS 时会撞上一次的 SUCCESS 行。
--  【后果】代码只能在 PluginUpdateService#finishTask 里用「合并进已存在的终态行」兜底，
--         历史因此永远只剩「最后一次」，第 2、3 次安装记录被抹掉。
--
--  【修法】函数式唯一键：加一个生成列 active_key
--             非终态（PENDING / RUNNING）→ CONCAT(instance_id,'|',plugin_code,'|',op_type,'|',to_version)
--             终态（SUCCESS / FAILED / ROLLED_BACK）→ NULL
--          MySQL / MariaDB 唯一索引中 NULL 互不冲突，于是：
--               · 非终态仍然严格互斥（并发 3016 语义不变）；
--               · 终态可以任意多条共存，完整历史得以保留，合并兜底不再被触发。
--          （MySQL 8.0.13+ 也可以用「函数索引」直接写表达式，但生成列兼容到 5.7，
--            且 information_schema 里看得见、SHOW CREATE TABLE 里读得懂，排查更省事。）
--
--  ⚠️ 前置条件：生成列需要 MySQL 5.7+ / MariaDB 10.2+（本文件其余部分无此要求）。
--     脚本会先探测版本：不满足时**保留旧键 uk_idem 不动**并打印告警，
--     绝不留「旧键已删、新键没建」这种没有互斥保护的半成品。
-- =====================================================================

-- 6.0 版本探测：@v_gen_ok=1 才允许改键
--     版本串形如 '8.0.33' / '5.7.44-log' / '10.11.2-MariaDB'，取前两段判断
SET @v_version := @@version;
SET @v_major := CAST(SUBSTRING_INDEX(@v_version, '.', 1) AS UNSIGNED);
SET @v_minor := CAST(SUBSTRING_INDEX(SUBSTRING_INDEX(@v_version, '.', 2), '.', -1) AS UNSIGNED);
SET @v_mariadb := @v_version LIKE '%MariaDB%';
SET @v_gen_ok := IF(@v_mariadb,
                    (@v_major > 10 OR (@v_major = 10 AND @v_minor >= 2)),
                    (@v_major > 5 OR (@v_major = 5 AND @v_minor >= 7)));

-- 新键是否已存在（决定后面每一步是不是空操作）
SET @v_uk_active_exists := (SELECT COUNT(*) FROM information_schema.statistics
                            WHERE table_schema = @hou_schema AND table_name = 'hou_plugin_update_task'
                              AND index_name = 'uk_active');

-- 6.1 建新键前的必要清理：同一组合若存在多条非终态任务，ADD UNIQUE KEY 会直接报 Duplicate entry
--     旧 uk_idem 允许「一条 PENDING + 一条 RUNNING」共存（第二条 PENDING 与已有 RUNNING 状态不同，不冲突），
--     这两条在新键下 active_key 完全相同。这类行本质上是历史孤儿（服务只会跟踪其中一条），
--     保留 id 最大的那条，其余统一收敛为 FAILED/ORPHAN_TIMEOUT —— 与 M4 孤儿任务扫描的处理一致。
--     收敛后重跑本脚本不会再有重复组，因此这一步也是幂等的。

SELECT IF(@v_gen_ok = 0,
          CONCAT('⚠️ 当前 MySQL/MariaDB 版本 ', @v_version, ' 不支持生成列，已跳过 uk_active 改造，旧键 uk_idem 保持不变'),
          IF(@v_uk_active_exists > 0,
             'uk_active 已存在，跳过本段',
             'uk_active 前置检查通过：同一组合最多一条非终态任务')) AS ddl_notice;

-- 6.2 生成列 active_key（不存在才加；终态为 NULL，唯一索引下 NULL 不冲突）
SET @v_exists := (SELECT COUNT(*) FROM information_schema.columns
                  WHERE table_schema = @hou_schema AND table_name = 'hou_plugin_update_task'
                    AND column_name = 'active_key');
SET @v_ddl := IF(@v_gen_ok = 1 AND @v_exists = 0,
                 'ALTER TABLE hou_plugin_update_task '
                 'ADD COLUMN active_key VARCHAR(255) GENERATED ALWAYS AS ('
                 'IF(state IN (''PENDING'',''RUNNING''), '
                 'CONCAT(instance_id,''|'',plugin_code,''|'',op_type,''|'',to_version), NULL)) VIRTUAL '
                 'COMMENT ''非终态互斥键：终态为 NULL，唯一索引下 NULL 不冲突''',
                 'DO 0');
PREPARE hou_stmt FROM @v_ddl; EXECUTE hou_stmt; DEALLOCATE PREPARE hou_stmt;

-- 6.3 唯一键 uk_active（不存在才建；必须在旧键删除之前，全程保留互斥保护）
SET @v_ddl := IF(@v_gen_ok = 1 AND @v_uk_active_exists = 0,
                 'ALTER TABLE hou_plugin_update_task ADD UNIQUE KEY uk_active (active_key)',
                 'DO 0');
PREPARE hou_stmt FROM @v_ddl; EXECUTE hou_stmt; DEALLOCATE PREPARE hou_stmt;

-- 6.4 删除旧键 uk_idem（新键已生效才删；版本不支持时保留旧键兜底）
SET @v_exists := (SELECT COUNT(*) FROM information_schema.statistics
                  WHERE table_schema = @hou_schema AND table_name = 'hou_plugin_update_task'
                    AND index_name = 'uk_idem');
SET @v_ddl := IF(@v_gen_ok = 1 AND @v_exists > 0,
                 'ALTER TABLE hou_plugin_update_task DROP INDEX uk_idem',
                 'DO 0');
PREPARE hou_stmt FROM @v_ddl; EXECUTE hou_stmt; DEALLOCATE PREPARE hou_stmt;

-- =====================================================================
--  七、扩列：hou_market_plugin_license（已存在，扩 6 列 —— 方案 §3.3，M5 试用期）
--
--  背景：授权原先只有「status（1 有效 / 0 吊销）+ expire_time」两态，
--        无法回答三个业务问题：
--          ① 这份授权走到生命周期哪一步了（签发 / 绑定 / 激活 / 过期 / 吊销 / 解绑）？
--          ② 试用期从哪天开始算？（原设计按签发时间算，会出现「买了没装却在倒计时」）
--          ③ 这份授权为什么被吊销？（退款 / 违规 / 紧急下架，无溯源字段）
--
--    state              —— 生命周期状态机（§7.2）。与 status 正交：
--                          status 是「能不能用」的粗开关，state 是「走到哪一步」；
--                          复用 status 表达生命周期会破坏既有 findValid 语义，因此另开一列。
--    bind_time          —— 首次绑定站点实例时间。
--    first_install_time —— ★试用期起算点：首次<b>成功安装</b>的时刻（不是签发、不是下载）。
--                          回填后 expire_time = first_install_time + trial_days。
--                          「卸载重装」不重置该列，否则等于无限续期试用。
--    source_order_no    —— 来源订单号，吊销溯源。
--    revoke_reason      —— 吊销原因（平台 / 退款 / 违规 / 紧急下架）。
--    last_heartbeat     —— 运行时最后一次授权心跳（M5 只落列，心跳任务未接）。
--
--  ⚠️ 部署顺序：MarketPluginLicense 实体已同步新增 state / bind_time /
--     first_install_time / source_order_no / revoke_reason / last_heartbeat 六个属性，
--     MyBatis-Plus 生成的 SELECT 会带上新列，**必须先执行本文件再发布 mall**，
--     否则授权查询会因缺列报错（与第四段 hou_market_plugin 的口径一致）。
--
--  ⚠️ 存量数据：state 一律回填 'ISSUED' 的默认值。已激活（装过）的历史授权
--     没有 first_install_time 可追，其 expire_time 保持原值不变——
--     不做「现在补一个起算点」的猜测，避免把老租户的过期时间改短。
-- =====================================================================

-- 7.1 state：生命周期状态（ISSUED/BOUND/ACTIVATED/EXPIRED/REVOKED/UNBOUND）
SET @v_exists := (SELECT COUNT(*) FROM information_schema.columns
                  WHERE table_schema = @hou_schema AND table_name = 'hou_market_plugin_license'
                    AND column_name = 'state');
SET @v_ddl := IF(@v_exists = 0,
                 'ALTER TABLE hou_market_plugin_license ADD COLUMN state VARCHAR(16) NOT NULL DEFAULT ''ISSUED'' COMMENT ''ISSUED/BOUND/ACTIVATED/EXPIRED/REVOKED/UNBOUND''',
                 'DO 0');
PREPARE hou_stmt FROM @v_ddl; EXECUTE hou_stmt; DEALLOCATE PREPARE hou_stmt;

-- 7.2 bind_time：首次绑定站点实例时间
SET @v_exists := (SELECT COUNT(*) FROM information_schema.columns
                  WHERE table_schema = @hou_schema AND table_name = 'hou_market_plugin_license'
                    AND column_name = 'bind_time');
SET @v_ddl := IF(@v_exists = 0,
                 'ALTER TABLE hou_market_plugin_license ADD COLUMN bind_time DATETIME NULL COMMENT ''首次绑定站点实例时间''',
                 'DO 0');
PREPARE hou_stmt FROM @v_ddl; EXECUTE hou_stmt; DEALLOCATE PREPARE hou_stmt;

-- 7.3 first_install_time：★首次成功安装时间 = 试用期起算点(P1)
SET @v_exists := (SELECT COUNT(*) FROM information_schema.columns
                  WHERE table_schema = @hou_schema AND table_name = 'hou_market_plugin_license'
                    AND column_name = 'first_install_time');
SET @v_ddl := IF(@v_exists = 0,
                 'ALTER TABLE hou_market_plugin_license ADD COLUMN first_install_time DATETIME NULL COMMENT ''★首次成功安装时间 = 试用期起算点(P1)''',
                 'DO 0');
PREPARE hou_stmt FROM @v_ddl; EXECUTE hou_stmt; DEALLOCATE PREPARE hou_stmt;

-- 7.4 source_order_no：来源订单号，吊销溯源
SET @v_exists := (SELECT COUNT(*) FROM information_schema.columns
                  WHERE table_schema = @hou_schema AND table_name = 'hou_market_plugin_license'
                    AND column_name = 'source_order_no');
SET @v_ddl := IF(@v_exists = 0,
                 'ALTER TABLE hou_market_plugin_license ADD COLUMN source_order_no VARCHAR(64) NULL COMMENT ''来源订单号，吊销溯源''',
                 'DO 0');
PREPARE hou_stmt FROM @v_ddl; EXECUTE hou_stmt; DEALLOCATE PREPARE hou_stmt;

-- 7.5 revoke_reason：吊销原因（平台/退款/违规/紧急下架）
SET @v_exists := (SELECT COUNT(*) FROM information_schema.columns
                  WHERE table_schema = @hou_schema AND table_name = 'hou_market_plugin_license'
                    AND column_name = 'revoke_reason');
SET @v_ddl := IF(@v_exists = 0,
                 'ALTER TABLE hou_market_plugin_license ADD COLUMN revoke_reason VARCHAR(255) NULL COMMENT ''吊销原因（平台/退款/违规/紧急下架）''',
                 'DO 0');
PREPARE hou_stmt FROM @v_ddl; EXECUTE hou_stmt; DEALLOCATE PREPARE hou_stmt;

-- 7.6 last_heartbeat：运行时最后一次授权心跳(P1)
SET @v_exists := (SELECT COUNT(*) FROM information_schema.columns
                  WHERE table_schema = @hou_schema AND table_name = 'hou_market_plugin_license'
                    AND column_name = 'last_heartbeat');
SET @v_ddl := IF(@v_exists = 0,
                 'ALTER TABLE hou_market_plugin_license ADD COLUMN last_heartbeat DATETIME NULL COMMENT ''运行时最后一次授权心跳(P1)''',
                 'DO 0');
PREPARE hou_stmt FROM @v_ddl; EXECUTE hou_stmt; DEALLOCATE PREPARE hou_stmt;

-- 7.7 索引：idx_license_state（过期迁移与心跳按 state + expire_time 扫，不存在才建）
SET @v_exists := (SELECT COUNT(*) FROM information_schema.statistics
                  WHERE table_schema = @hou_schema AND table_name = 'hou_market_plugin_license'
                    AND index_name = 'idx_license_state');
SET @v_ddl := IF(@v_exists = 0,
                 'ALTER TABLE hou_market_plugin_license ADD KEY idx_license_state (state, expire_time)',
                 'DO 0');
PREPARE hou_stmt FROM @v_ddl; EXECUTE hou_stmt; DEALLOCATE PREPARE hou_stmt;

SELECT CONCAT('hou_market_plugin_license 扩列完成：state=',
              (SELECT COUNT(*) FROM information_schema.columns
                WHERE table_schema = @hou_schema AND table_name = 'hou_market_plugin_license'
                  AND column_name = 'state'),
              ' first_install_time=',
              (SELECT COUNT(*) FROM information_schema.columns
                WHERE table_schema = @hou_schema AND table_name = 'hou_market_plugin_license'
                  AND column_name = 'first_install_time')) AS ddl_notice;


-- ---------- backend/paas/plugin-center/src/main/resources/db/hou_ext_migration.sql ----------
-- 二开 schema 迁移幂等表（R2.1）。
-- 由 classpath 版迁移执行器 ExtensionSchemaMigrationRunner / JdbcExtensionMigrationRepository 自维护；
-- CREATE TABLE IF NOT EXISTS，可重复执行（幂等，零副作用）。
-- 表名沿用 hou_ext_ 前缀，因为「迁移审计」本身即二开数据。
CREATE TABLE IF NOT EXISTS `hou_ext_migration` (
    `code`       varchar(64)   NOT NULL COMMENT '二开模块 code',
    `script`     varchar(255)  NOT NULL COMMENT '迁移脚本名（相对 db/migration/{code}/）',
    `version`    varchar(32)            DEFAULT NULL COMMENT '二开版本',
    `checksum`   char(64)               DEFAULT NULL COMMENT '脚本内容 SHA-256',
    `status`     varchar(16)   NOT NULL COMMENT '执行状态：SUCCESS / FAILED',
    `message`    varchar(1024)          DEFAULT NULL COMMENT '失败原因（截断）',
    `updated_at` datetime      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`code`, `script`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT = '二开 schema 迁移幂等表';


-- ---------- backend/paas/plugin-market/src/main/resources/sql/mall_market_order_tables.sql ----------
-- ============================================================
-- 2026-09-16 迁移：历史同名表冲突（生产库 meoc 实测）
-- 现象：POST /web/mall/market/order/create 返回
--       SQLSyntaxErrorException: Unknown column 'license_type' in 'field list'
-- 根因：目标库已存在同名但字段完全不同的历史表（分账语义：
--       seller_id / amount_cents / platform_fee_cents / seller_income_cents / pay_status / pay_channel），
--       而本脚本用 CREATE TABLE IF NOT EXISTS，旧表存在时整段被静默跳过，
--       MarketPluginOrder 实体字段与旧表不匹配，insert 报未知列。
-- 处理：确认旧表无任何代码引用且行数为 0 后，先改名备份，再执行下方建表语句。
-- ============================================================
-- RENAME TABLE hou_market_plugin_order TO hou_market_plugin_order_legacy_bak_20260916;

-- 插件市场在线订单（hou_market_plugin_order）
-- 背景：此前收费链路为「线下收款 → 平台手工签发授权码 → 租户激活」，系统内不落订单。
-- 本表承载在线购买闭环：下单 → 调用已有收银台支付 → 支付成功自动签发并绑定授权。
--
-- 幂等约定：order_no 唯一，避免重复下单；cashier_no / pay_order_id 唯一，避免同一支付流水重复发证。

CREATE TABLE IF NOT EXISTS hou_market_plugin_order (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_no        VARCHAR(64)  NOT NULL COMMENT '插件订单号（业务主键，唯一）',
    plugin_code     VARCHAR(64)  NOT NULL COMMENT '插件编码',
    version         VARCHAR(32)  NULL     COMMENT '购买目标版本，为空表示最新版本',
    license_type    VARCHAR(16)  NOT NULL COMMENT 'PAID 付费 / TRIAL 试用',
    valid_days      INT          NULL     COMMENT '授权有效天数，NULL 或 <=0 表示永久',
    amount          DECIMAL(12,2) NOT NULL DEFAULT 0 COMMENT '应付金额（元）',
    currency        VARCHAR(8)   NOT NULL DEFAULT 'CNY' COMMENT '币种',
    buyer_id        BIGINT       NOT NULL DEFAULT 1 COMMENT '购买方站点 ID，平台自营为 1',
    instance_id     VARCHAR(128) NULL     COMMENT '站点实例（域名），支付成功后授权绑定到此实例',
    pay_method      VARCHAR(32)  NULL     COMMENT '支付方式：WXPAY / ALIPAY 等',
    terminal        VARCHAR(16)  NULL     COMMENT '终端：PC / H5 / APP',
    cashier_no      VARCHAR(64)  NULL     COMMENT '收银台流水号（order 侧）',
    pay_order_id    VARCHAR(64)  NULL     COMMENT '渠道支付单号',
    pay_data        TEXT         NULL     COMMENT '下单返回的支付参数（二维码/表单）JSON',
    status          TINYINT      NOT NULL DEFAULT 0 COMMENT '0待支付 1已支付 2已关闭 3已退款',
    paid_time       DATETIME     NULL     COMMENT '支付完成时间',
    expire_time     DATETIME     NULL     COMMENT '支付截止时间（超时未付自动关闭）',
    license_id      BIGINT       NULL     COMMENT '支付成功后自动签发的授权 ID',
    license_key     VARCHAR(128) NULL     COMMENT '自动签发的授权码（冗余便于展示）',
    create_time     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_order_no (order_no),
    UNIQUE KEY uk_cashier_no (cashier_no),
    KEY idx_plugin_instance (plugin_code, instance_id),
    KEY idx_instance_status (instance_id, status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='插件市场在线订单';


-- ---------- backend/paas/plugin-market/src/main/resources/sql/mall_market_plugin_catalog.sql ----------
-- Mall plugin market catalog and initial official SKU data.
-- Price is stored in price_cents (1 CNY = 100 cents). Purchase state is stored
-- in hou_market_plugin_license and must never be hard-coded in a page or manifest.

CREATE TABLE IF NOT EXISTS hou_market_plugin (
    id BIGINT NOT NULL AUTO_INCREMENT,
    plugin_code VARCHAR(64) NOT NULL,
    name VARCHAR(128) NOT NULL,
    author_id BIGINT NOT NULL DEFAULT 1,
    category VARCHAR(64) DEFAULT NULL,
    description TEXT,
    short_desc VARCHAR(256) DEFAULT NULL,
    icon_url VARCHAR(512) DEFAULT NULL,
    screenshots_json TEXT,
    latest_version VARCHAR(32) DEFAULT NULL,
    license_type VARCHAR(16) NOT NULL DEFAULT 'FREE',
    price_cents INT NOT NULL DEFAULT 0,
    trial_days INT NOT NULL DEFAULT 0,
    download_count INT NOT NULL DEFAULT 0,
    install_count INT NOT NULL DEFAULT 0,
    rating_avg DECIMAL(3,2) NOT NULL DEFAULT 0.00,
    rating_count INT NOT NULL DEFAULT 0,
    status TINYINT NOT NULL DEFAULT 0,
    min_framework_version VARCHAR(32) DEFAULT NULL,
    tags_json TEXT,
    terminals_json TEXT,
    dependencies_json TEXT,
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    UNIQUE KEY uk_market_plugin_code (plugin_code),
    KEY idx_market_plugin_status_category (status, category)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='插件市场商品与定价';

CREATE TABLE IF NOT EXISTS hou_market_plugin_version (
    id BIGINT NOT NULL AUTO_INCREMENT,
    plugin_code VARCHAR(64) NOT NULL,
    version VARCHAR(32) NOT NULL,
    jar_url VARCHAR(512) NOT NULL,
    jar_md5 VARCHAR(64) NOT NULL,
    jar_size BIGINT DEFAULT NULL,
    jar_signature TEXT,
    changelog TEXT,
    schema_sql TEXT,
    rollback_sql TEXT,
    plugin_yaml TEXT,
    compatibility_json TEXT,
    status TINYINT NOT NULL DEFAULT 0,
    review_note TEXT,
    publish_time DATETIME DEFAULT NULL,
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_market_plugin_version (plugin_code, version)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='插件市场版本制品';

CREATE TABLE IF NOT EXISTS hou_market_plugin_license (
    id BIGINT NOT NULL AUTO_INCREMENT,
    plugin_code VARCHAR(64) NOT NULL,
    buyer_id BIGINT NOT NULL DEFAULT 1,
    instance_id VARCHAR(128) DEFAULT NULL,
    license_key VARCHAR(256) NOT NULL,
    license_type VARCHAR(16) NOT NULL,
    max_instances INT NOT NULL DEFAULT 1,
    expire_time DATETIME DEFAULT NULL,
    status TINYINT NOT NULL DEFAULT 1,
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_market_license_key (license_key),
    KEY idx_market_license_buyer_plugin (buyer_id, plugin_code),
    KEY idx_market_license_instance (instance_id, status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='插件市场购买授权';

-- Initial official catalog. All currently migrated business plugins are paid
-- at 30,000 CNY by default. This migration seeds an empty catalog only; when
-- a row already exists, its operational pricing and license type are preserved.

-- Do not insert a blanket license here. A purchase is represented by a row in
-- hou_market_plugin_license created after payment or platform approval, and
-- the plugin center reads that row to calculate PURCHASED/NOT_PURCHASED.


-- ---------- backend/paas/plugin-registry/src/main/resources/sql/plugin_addon_tables.sql ----------
-- =====================================================================
-- 插件化架构 · 站点库表（幂等建表，可随 mall 启动重复执行）
--   hou_addon_plugin            站点插件目录（P0-3：plugin.yaml 单向同步目标）
--   hou_addon_plugin_migration  插件 schema 迁移记录（P0-4：迁移幂等 + 审计）
-- 说明：两表此前仅在代码/文档出现、无受管 DDL；此处补齐，均用 IF NOT EXISTS，
--       已存在则不改动（列差异由各自 Mapper 的显式 SQL 兼容）。
-- =====================================================================

CREATE TABLE IF NOT EXISTS `hou_addon_plugin` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `service_code` varchar(64) NOT NULL COMMENT '归属服务编码，如 mall',
  `plugin_code` varchar(64) NOT NULL COMMENT '插件唯一编码',
  `name` varchar(128) DEFAULT NULL COMMENT '插件名称',
  `category` varchar(64) DEFAULT NULL COMMENT '分类',
  `version` varchar(32) DEFAULT NULL COMMENT '版本',
  `description` varchar(512) DEFAULT NULL COMMENT '描述',
  `terminals_json` varchar(255) DEFAULT NULL COMMENT '适用端 JSON 数组',
  `dependencies_json` varchar(512) DEFAULT NULL COMMENT '依赖插件编码 JSON 数组',
  `entry_path` varchar(255) DEFAULT NULL COMMENT '前端入口',
  `config_schema` text COMMENT '配置面板 Schema JSON',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '1=上架,0=下架',
  `sort` int NOT NULL DEFAULT 0 COMMENT '排序权重（人工维护，upsert 不覆盖）',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '@TableLogic 软删:0=正常,1=删除',
  `create_time` datetime DEFAULT NULL,
  `update_time` datetime DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_service_plugin` (`service_code`,`plugin_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='站点插件目录（plugin.yaml 单向同步目标）';

CREATE TABLE IF NOT EXISTS `hou_addon_plugin_migration` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `plugin_code` varchar(64) NOT NULL COMMENT '插件唯一编码',
  `version` varchar(32) NOT NULL COMMENT '插件版本',
  `script` varchar(512) DEFAULT NULL COMMENT '执行的脚本路径，逗号分隔',
  `checksum` varchar(64) DEFAULT NULL COMMENT '脚本内容 SHA-256 摘要',
  `status` tinyint NOT NULL DEFAULT 0 COMMENT '1=成功,0=失败',
  `message` varchar(1024) DEFAULT NULL COMMENT '失败原因/备注',
  `applied_time` datetime DEFAULT NULL COMMENT '最近一次执行时间',
  `create_time` datetime DEFAULT NULL,
  `update_time` datetime DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_plugin_version` (`plugin_code`,`version`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='插件 schema 迁移记录（幂等）';


-- ---------- backend/paas/thirdparty/src/main/resources/sql/2026-07-24-task-notification-phase2.sql ----------
-- Task notification phase 2. MySQL 8, safe to execute repeatedly.
DELIMITER $$

DROP PROCEDURE IF EXISTS message_notification_phase2_assert_data$$
CREATE PROCEDURE message_notification_phase2_assert_data()
BEGIN
    IF EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_schema = DATABASE()
          AND table_name = 'hou_message_record'
          AND column_name = 'idempotency_key'
    ) AND EXISTS (
        SELECT idempotency_key
        FROM hou_message_record
        WHERE idempotency_key IS NOT NULL AND idempotency_key <> ''
        GROUP BY idempotency_key
        HAVING COUNT(*) > 1
    ) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'duplicate message record idempotency key';
    END IF;

    IF EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_schema = DATABASE()
          AND table_name = 'hou_message_inbox'
          AND column_name = 'idempotency_key'
    ) AND EXISTS (
        SELECT idempotency_key
        FROM hou_message_inbox
        WHERE idempotency_key IS NOT NULL AND idempotency_key <> ''
        GROUP BY idempotency_key
        HAVING COUNT(*) > 1
    ) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'duplicate message inbox idempotency key';
    END IF;
END$$

DROP PROCEDURE IF EXISTS message_notification_phase2_add_column$$
CREATE PROCEDURE message_notification_phase2_add_column(
    IN target_table VARCHAR(64),
    IN target_column VARCHAR(64),
    IN ddl_statement TEXT
)
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_schema = DATABASE()
          AND table_name = target_table
          AND column_name = target_column
    ) THEN
        SET @notification_phase2_ddl = ddl_statement;
        PREPARE notification_phase2_statement FROM @notification_phase2_ddl;
        EXECUTE notification_phase2_statement;
        DEALLOCATE PREPARE notification_phase2_statement;
    END IF;
END$$

DROP PROCEDURE IF EXISTS message_notification_phase2_add_index$$
CREATE PROCEDURE message_notification_phase2_add_index(
    IN target_table VARCHAR(64),
    IN target_index VARCHAR(64),
    IN ddl_statement TEXT
)
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.statistics
        WHERE table_schema = DATABASE()
          AND table_name = target_table
          AND index_name = target_index
    ) THEN
        SET @notification_phase2_ddl = ddl_statement;
        PREPARE notification_phase2_statement FROM @notification_phase2_ddl;
        EXECUTE notification_phase2_statement;
        DEALLOCATE PREPARE notification_phase2_statement;
    END IF;
END$$

CALL message_notification_phase2_add_column(
    'hou_message_record', 'idempotency_key',
    'ALTER TABLE hou_message_record ADD COLUMN idempotency_key VARCHAR(160) NULL'
)$$
CALL message_notification_phase2_add_column(
    'hou_message_record', 'event_type',
    'ALTER TABLE hou_message_record ADD COLUMN event_type VARCHAR(48) NULL'
)$$
CALL message_notification_phase2_add_column(
    'hou_message_record', 'error_code',
    'ALTER TABLE hou_message_record ADD COLUMN error_code VARCHAR(64) NULL'
)$$
CALL message_notification_phase2_add_column(
    'hou_message_record', 'provider_message_id',
    'ALTER TABLE hou_message_record ADD COLUMN provider_message_id VARCHAR(160) NULL'
)$$
CALL message_notification_phase2_add_column(
    'hou_message_inbox', 'idempotency_key',
    'ALTER TABLE hou_message_inbox ADD COLUMN idempotency_key VARCHAR(160) NULL'
)$$

CALL message_notification_phase2_assert_data()$$

CALL message_notification_phase2_add_index(
    'hou_message_record', 'uk_message_record_idempotency',
    'CREATE UNIQUE INDEX uk_message_record_idempotency ON hou_message_record (idempotency_key)'
)$$
CALL message_notification_phase2_add_index(
    'hou_message_inbox', 'uk_message_inbox_idempotency',
    'CREATE UNIQUE INDEX uk_message_inbox_idempotency ON hou_message_inbox (idempotency_key)'
)$$
CALL message_notification_phase2_add_index(
    'hou_message_record', 'idx_message_record_org_filter',
    'CREATE INDEX idx_message_record_org_filter ON hou_message_record (org_id, biz_type, channel, send_status, create_time)'
)$$
CALL message_notification_phase2_add_index(
    'hou_message_inbox', 'idx_message_inbox_owner',
    'CREATE INDEX idx_message_inbox_owner ON hou_message_inbox (user_id, org_id, is_read, delete_time, create_time)'
)$$

DROP PROCEDURE IF EXISTS message_notification_phase2_assert_data$$
DROP PROCEDURE IF EXISTS message_notification_phase2_add_column$$
DROP PROCEDURE IF EXISTS message_notification_phase2_add_index$$

DELIMITER ;
