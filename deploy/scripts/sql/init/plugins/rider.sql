-- rider owned baseline; structure only from tracked 10-meoc-schema.sql.
-- Supplemental/unimplemented entity schemas require separate verified migrations.
SET NAMES utf8mb4;
SET @OWNED_OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS;
SET FOREIGN_KEY_CHECKS=0;

CREATE TABLE IF NOT EXISTS `hou_rider_evaluation` (
  `evaluation_id` bigint NOT NULL AUTO_INCREMENT COMMENT '评价ID',
  `order_id` bigint NOT NULL COMMENT '订单ID',
  `rider_id` bigint NOT NULL COMMENT '骑手ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `rating` tinyint NOT NULL COMMENT '评分：1-5星',
  `content` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '评价内容',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint DEFAULT '0' COMMENT '删除标志：0-未删除，1-已删除',
  PRIMARY KEY (`evaluation_id`),
  KEY `idx_order_id` (`order_id`),
  KEY `idx_rider_id` (`rider_id`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='骑手评价表';

CREATE TABLE IF NOT EXISTS `hou_rider_info` (
  `rider_id` bigint NOT NULL COMMENT '骑手ID',
  `rider_name` varchar(50) NOT NULL COMMENT '骑手姓名',
  `id_type` tinyint DEFAULT '1' COMMENT '证件类型1-身份证',
  `id_number` varchar(50) NOT NULL COMMENT '证件号',
  `valid_start_date` date NOT NULL COMMENT '证件有效期开始时间',
  `valid_end_date` date NOT NULL COMMENT '证件有效期结束时间',
  `front_image` varchar(255) NOT NULL COMMENT '证件照正面',
  `back_image` varchar(255) NOT NULL COMMENT '证件照背面',
  `status` tinyint DEFAULT '1' COMMENT '审核状态 1-审核中 2-审核成功 3-审核失败',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint DEFAULT '0' COMMENT '删除标记',
  PRIMARY KEY (`rider_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='骑手实名信息表';

CREATE TABLE IF NOT EXISTS `hou_rider_rider_base` (
  `rider_id` bigint NOT NULL AUTO_INCREMENT COMMENT '骑手ID',
  `rider_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '骑手名称',
  `rider_phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '骑手电话',
  `password` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '密码',
  `verification_code` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '验证码',
  `rating` decimal(3,2) DEFAULT '0.00' COMMENT '评分',
  `total_earnings` decimal(10,2) DEFAULT '0.00' COMMENT '总收益',
  `avatar` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '骑手头像',
  `real_name_status` tinyint DEFAULT '0' COMMENT '实名认证状态：0-未认证，1-审核中，2-已认证',
  `status` tinyint DEFAULT '0' COMMENT '状态：0-启用，1-禁用',
  `total_deliveries` int DEFAULT '0' COMMENT '配送总量',
  `total_delivery_fee` decimal(10,2) DEFAULT '0.00' COMMENT '配送费总额',
  `withdrawn_amount` decimal(10,2) DEFAULT '0.00' COMMENT '已提现金额',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint DEFAULT '0' COMMENT '删除标志：0-未删除，1-已删除',
  PRIMARY KEY (`rider_id`),
  UNIQUE KEY `uk_rider_phone` (`rider_phone`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='骑手基础信息表';

CREATE TABLE IF NOT EXISTS `hou_rider_settlement` (
  `settlement_id` bigint NOT NULL AUTO_INCREMENT COMMENT '结算ID',
  `rider_id` bigint NOT NULL COMMENT '骑手ID',
  `settlement_amount` decimal(10,2) NOT NULL COMMENT '结算金额',
  `settlement_type` tinyint DEFAULT '1' COMMENT '结算类型：1-配送费，2-奖励，3-其他',
  `settlement_status` tinyint DEFAULT '0' COMMENT '结算状态：0-待结算，1-已结算，2-结算失败',
  `settlement_time` datetime DEFAULT NULL COMMENT '结算时间',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '备注',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint DEFAULT '0' COMMENT '删除标志：0-未删除，1-已删除',
  PRIMARY KEY (`settlement_id`),
  KEY `idx_rider_id` (`rider_id`),
  KEY `idx_settlement_status` (`settlement_status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='骑手结算表';

CREATE TABLE IF NOT EXISTS `hou_rider_withdraw` (
  `withdraw_id` bigint NOT NULL AUTO_INCREMENT COMMENT '提现ID',
  `rider_id` bigint NOT NULL COMMENT '骑手ID',
  `withdraw_amount` decimal(10,2) NOT NULL COMMENT '申请提现金额',
  `withdraw_time` datetime DEFAULT NULL COMMENT '申请时间',
  `bank_name` varchar(100) DEFAULT NULL COMMENT '提现银行',
  `holder_name` varchar(50) DEFAULT NULL COMMENT '开户人姓名',
  `card_number` varchar(50) DEFAULT NULL COMMENT '银行卡号',
  `status` tinyint DEFAULT '1' COMMENT '审核状态 1-待审核 2-审核通过 3-审核未通过',
  `withdraw_fees` decimal(10,2) DEFAULT '0.00' COMMENT '提现手续费',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint DEFAULT '0' COMMENT '删除标记',
  PRIMARY KEY (`withdraw_id`),
  KEY `idx_rider_id` (`rider_id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='骑手提现表';

SET FOREIGN_KEY_CHECKS=@OWNED_OLD_FOREIGN_KEY_CHECKS;
