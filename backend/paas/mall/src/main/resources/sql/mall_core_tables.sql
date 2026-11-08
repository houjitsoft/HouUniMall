-- ================================================================
-- Mall商城核心电商功能补充建表SQL
-- 生成时间: 2026-01-05
-- 新增表: 购物车、用户收货地址、商品收藏 共3张表
-- ================================================================

-- 1. 购物车表
DROP TABLE IF EXISTS `hou_mall_shopping_cart`;
CREATE TABLE `hou_mall_shopping_cart` (
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
DROP TABLE IF EXISTS `hou_mall_user_address`;
CREATE TABLE `hou_mall_user_address` (
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
DROP TABLE IF EXISTS `hou_mall_goods_favorite`;
CREATE TABLE `hou_mall_goods_favorite` (
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
