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

UPDATE hou_merchant_mall_shop_admin a
JOIN hou_mall_merchant_account ma ON ma.admin_id = a.id
SET a.user_id = ma.user_id
WHERE a.user_id IS NULL;

INSERT INTO `hou_mall_store`
  (`merchant_id`, `store_no`, `store_name`, `contact_person`, `contact_phone`, `address`,
   `business_status`, `rider_delivery_enabled`, `pos_enabled`)
SELECT m.id,
       CONCAT('STORE-', LPAD(m.id, 6, '0')),
       CONCAT(m.name, ' 默认门店'),
       m.contact,
       TRIM(REPLACE(REPLACE(REPLACE(COALESCE(m.mobile, ''), '[', ''), ']', ''), CHAR(34), '')),
       m.address_info,
       COALESCE(m.business_status, 1),
       0,
       0
FROM `hou_merchant_mall_shop` m
WHERE COALESCE(m.delete_time, 0) = 0
  AND NOT EXISTS (
    SELECT 1 FROM `hou_mall_store` s
    WHERE s.merchant_id = m.id AND s.deleted = 0
  );
