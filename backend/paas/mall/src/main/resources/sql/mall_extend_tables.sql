-- ================================================================
-- Mall 商城扩展表（用于 dcenter 商家管理后台）
-- 创建时间：2026-03-18
-- ================================================================

-- 1. 商品评价表
DROP TABLE IF EXISTS `hou_mall_goods_comment`;
CREATE TABLE `hou_mall_goods_comment` (
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
DROP TABLE IF EXISTS `hou_mall_decorate_page`;
CREATE TABLE `hou_mall_decorate_page` (
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
DROP TABLE IF EXISTS `hou_mall_file_category`;
CREATE TABLE `hou_mall_file_category` (
  `cate_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '分类 ID',
  `shop_id` bigint(20) DEFAULT NULL COMMENT '店铺 ID',
  `cate_name` varchar(100) NOT NULL COMMENT '分类名称',
  `sort` int(11) NOT NULL DEFAULT '0' COMMENT '排序',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`cate_id`),
  KEY `idx_shop_id` (`shop_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='文件分类表';

-- 4. 文件表
DROP TABLE IF EXISTS `hou_mall_file`;
CREATE TABLE `hou_mall_file` (
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
DROP TABLE IF EXISTS `hou_mall_announcement`;
CREATE TABLE `hou_mall_announcement` (
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
DROP TABLE IF EXISTS `hou_mall_notice_setting`;
CREATE TABLE `hou_mall_notice_setting` (
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
DROP TABLE IF EXISTS `hou_mall_service_config`;
CREATE TABLE `hou_mall_service_config` (
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
DROP TABLE IF EXISTS `hou_mall_gather`;
CREATE TABLE `hou_mall_gather` (
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
