-- supplier owned baseline; structure only from tracked 10-meoc-schema.sql.
-- Supplemental/unimplemented entity schemas require separate verified migrations.
SET NAMES utf8mb4;
SET @OWNED_OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS;
SET FOREIGN_KEY_CHECKS=0;

CREATE TABLE IF NOT EXISTS `hou_supplier_common_base` (
  `supplier_common_id` int unsigned NOT NULL AUTO_INCREMENT COMMENT '供应商商品spuid',
  `common_name` varchar(500) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL DEFAULT '' COMMENT '供应商商品名称',
  `supplier_shop_id` int unsigned NOT NULL DEFAULT '0' COMMENT '供应商店铺id',
  `shop_name` varchar(50) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL DEFAULT '' COMMENT '供应商店铺名称',
  `supplier_goods_id` int NOT NULL DEFAULT '0' COMMENT '供应商商品skuid',
  `supplier_cat_id` int unsigned NOT NULL DEFAULT '0' COMMENT '供应商商品分类id',
  `cat_name` varchar(200) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL DEFAULT '' COMMENT '供应商商品分类名称',
  `spec_param` varchar(2000) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '商品规格',
  `common_spec_value` text CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci COMMENT '规格值',
  `supplier_type_id` int unsigned DEFAULT '0' COMMENT '类型id',
  `type` int DEFAULT '1' COMMENT '主图类型 1图片2视频',
  `common_image` varchar(500) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL DEFAULT '' COMMENT '商品主图',
  `common_video` varchar(1000) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT '' COMMENT '视频',
  `common_state` int unsigned NOT NULL DEFAULT '0' COMMENT '商品状态 0下架，1正常，10违规（禁售）,2店铺下架商品下架,3商品删除,4定时发布，5放入仓库',
  `common_state_remark` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL DEFAULT '' COMMENT '违规原因',
  `common_is_screen` int DEFAULT '1' COMMENT '商品是否屏蔽：1-否；2-是',
  `common_verify` tinyint unsigned NOT NULL DEFAULT '1' COMMENT '商品审核 1通过，0未通过，10审核中',
  `common_verify_remark` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL DEFAULT '' COMMENT '审核失败原因',
  `common_sell_time` datetime DEFAULT '2019-01-29 13:37:12' COMMENT '上架时间',
  `common_price` decimal(10,2) NOT NULL DEFAULT '0.00' COMMENT '商品价格',
  `common_market_price` decimal(10,2) NOT NULL DEFAULT '0.00' COMMENT '市场价',
  `common_cost_price` decimal(10,2) NOT NULL DEFAULT '0.00' COMMENT '成本价',
  `common_alarm` int unsigned NOT NULL DEFAULT '0' COMMENT '商品库存警报线',
  `common_cubage` decimal(10,2) NOT NULL DEFAULT '0.00' COMMENT '商品重量',
  `common_collect` int NOT NULL DEFAULT '0' COMMENT '商品收藏量',
  `common_evaluate` int NOT NULL DEFAULT '0' COMMENT '商品评论数',
  `common_salenum` int unsigned NOT NULL DEFAULT '0' COMMENT '商品销量',
  `district_id` mediumint NOT NULL DEFAULT '0' COMMENT '所在地,从店铺中同步，冗余检索使用',
  `goods_recommended_min_price` decimal(10,2) NOT NULL DEFAULT '0.00' COMMENT '建议最低零售价',
  `goods_recommended_max_price` decimal(10,2) NOT NULL DEFAULT '0.00' COMMENT '建议最高零售价',
  `transport_area_id` int NOT NULL DEFAULT '0' COMMENT '售卖区域id,和shop_transport_area中的id对应',
  `international_transport_area` int DEFAULT '0' COMMENT '国际售卖区域',
  `transport_template` int NOT NULL DEFAULT '0' COMMENT '运费模板',
  `international_transport_template` int DEFAULT '0' COMMENT '国际运费模板',
  `restriction_times` int NOT NULL DEFAULT '2' COMMENT '限购次数',
  `restriction_hours` int NOT NULL DEFAULT '2' COMMENT '限购天数',
  `order_operator` varchar(32) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '操作人',
  `order_operator_time` datetime DEFAULT NULL COMMENT '操作时间',
  `common_click` int DEFAULT '0' COMMENT '商品点击数量',
  `common_verify_time` datetime DEFAULT '2020-04-16 00:00:00' COMMENT '商品审核时间',
  `submit_verify_time` datetime DEFAULT '2020-04-16 00:00:00' COMMENT '提交审核时间',
  `create_user` varchar(18) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '创建者',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_user` varchar(18) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '修改人',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
  `is_delete` int DEFAULT '0' COMMENT '是否删除0:否;1:是',
  `technical_director_wx` varchar(500) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '技术负责人微信',
  `issued_on_behalf` int DEFAULT '0' COMMENT '是否支持代发:1:是,0否',
  `brand_id` int unsigned NOT NULL DEFAULT '0' COMMENT '品牌id',
  `brand_name` varchar(100) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL DEFAULT '' COMMENT '品牌名称',
  `virtual_goods` int DEFAULT '1' COMMENT '虚拟商品 0 是 1 否',
  `period_of_validity` datetime DEFAULT NULL COMMENT '虚拟商品有效期',
  `past_due` int DEFAULT NULL COMMENT '过期退款 0 是 1 否',
  `common_is_shop_take` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL,
  `goods_type` tinyint(1) DEFAULT '1' COMMENT '1普通商品   2供应商商品',
  `common_add_time` datetime NOT NULL DEFAULT '2019-01-29 13:37:12' COMMENT '商品添加时间',
  `common_update_time` datetime DEFAULT '2001-01-01 01:01:01' COMMENT '更新时间',
  `delivery_date` tinyint(1) DEFAULT NULL COMMENT '发货日 0是现货/1是订货',
  `custom_information` text CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci COMMENT '自定义信息',
  `after_service` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '售后服务',
  `common_stock` int unsigned NOT NULL DEFAULT '0' COMMENT '商品价格\r\n市场价\r\n成本价\r\n商品库存\r\n商品库存',
  `dealer_goods_flag` tinyint(1) DEFAULT '0' COMMENT '是否经销商品  0普通商品   1经销商品(商家一键上架供应商商品)  2购买经销商品（商家通过立即购买供应商上架的商品）',
  `shop_status` tinyint unsigned NOT NULL DEFAULT '3' COMMENT '店铺状态-3：开店成功 2:待审核付款 1:待审核资料  0:关闭',
  `common_is_locked` int NOT NULL DEFAULT '1' COMMENT '锁定 2 锁定 1 不锁定',
  PRIMARY KEY (`supplier_common_id`) USING BTREE,
  KEY `cat_id` (`supplier_cat_id`) USING BTREE,
  KEY `shop_id` (`supplier_shop_id`) USING BTREE,
  KEY `type_id` (`supplier_type_id`) USING BTREE,
  KEY `common_verify` (`common_verify`) USING BTREE,
  KEY `common_state` (`common_state`) USING BTREE,
  KEY `common_name` (`common_name`(255)) USING BTREE,
  KEY `shop_name` (`shop_name`) USING BTREE,
  KEY `common_id` (`supplier_common_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 ROW_FORMAT=DYNAMIC COMMENT='供应商商品spu表';

CREATE TABLE IF NOT EXISTS `hou_supplier_dealer_info` (
  `id` int NOT NULL AUTO_INCREMENT,
  `user_id` varchar(18) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '用户id',
  `level_Id` int DEFAULT NULL COMMENT '经销商等级id',
  `create_user` varchar(18) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '创建用户',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `shop_id` int DEFAULT NULL COMMENT '经销商 店铺id',
  `supplier_shop_id` int DEFAULT NULL COMMENT '供应商店铺id',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci ROW_FORMAT=DYNAMIC COMMENT='供应商下的经销商管理';

CREATE TABLE IF NOT EXISTS `hou_supplier_dealer_level_info` (
  `dealer_level_id` int unsigned NOT NULL AUTO_INCREMENT COMMENT '经销商等级id',
  `supplier_shop_id` int NOT NULL COMMENT '供应商店铺id',
  `dealer_level_name` varchar(100) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL COMMENT '经销商等级名称',
  `dealer_discount` int NOT NULL COMMENT '等级折扣',
  `dealer_level_sort` int NOT NULL DEFAULT '1' COMMENT '等级排序',
  `dealer_level_status` tinyint(1) NOT NULL DEFAULT '1' COMMENT '经销商等级状态  1正常 2删除',
  PRIMARY KEY (`dealer_level_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 ROW_FORMAT=DYNAMIC COMMENT='经销商等级';

CREATE TABLE IF NOT EXISTS `hou_supplier_dealer_putaway_record` (
  `id` int unsigned NOT NULL AUTO_INCREMENT COMMENT '主键id',
  `supply_common_id` int NOT NULL COMMENT '供应商商品spuid',
  `supply_goods_id` int NOT NULL DEFAULT '0' COMMENT '供应商商品skuid',
  `supplier_shop_id` int NOT NULL COMMENT '供应商店铺id',
  `supplier_shop_name` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '供应商店铺',
  `dealer_common_id` int NOT NULL COMMENT '经销商品spuid',
  `dealer_goods_id` int NOT NULL DEFAULT '0' COMMENT '经销商品skuid',
  `dealer_shop_id` int NOT NULL COMMENT '经销店铺id',
  `dealer_shop_name` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL COMMENT '经销店铺名',
  `dealer_price` decimal(20,2) NOT NULL COMMENT '经销供货价',
  `dealer_num` int NOT NULL DEFAULT '0' COMMENT '上架数量',
  `record_state` tinyint(1) NOT NULL DEFAULT '1' COMMENT '一键上架记录状态  1正常  2取消',
  `dealer_type` tinyint(1) DEFAULT '1' COMMENT '1 一键代发(商家一键代发供应商商品)  2商家购买供应商商品 3 一键铺货',
  `dealer_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '经销发布时间',
  `is_price_update` int DEFAULT '2' COMMENT '同步商品商品价格是否修改 1 是 2 否 未修改价格的商品不展示',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 ROW_FORMAT=DYNAMIC COMMENT='商品供应记录表';

CREATE TABLE IF NOT EXISTS `hou_supplier_goods_base` (
  `supplier_goods_id` int unsigned NOT NULL AUTO_INCREMENT COMMENT '供应商商品id',
  `supplier_common_id` int unsigned NOT NULL DEFAULT '0' COMMENT '供应商商品spuid',
  `supplier_shop_id` int unsigned NOT NULL DEFAULT '0' COMMENT '供应商店铺id',
  `shop_name` varchar(50) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL DEFAULT '' COMMENT '供应商店铺名称',
  `goods_name` varchar(500) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL DEFAULT '' COMMENT '商品名称（+规格名称）',
  `supplier_cat_id` int unsigned NOT NULL DEFAULT '0' COMMENT '商品分类id',
  `goods_spec` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL DEFAULT '' COMMENT '商品规格-JSON存储',
  `spec_value` varchar(2000) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '规格值',
  `goods_price` decimal(10,2) NOT NULL DEFAULT '0.00' COMMENT '商品价格',
  `goods_market_price` decimal(10,2) NOT NULL DEFAULT '0.00' COMMENT '市场价',
  `goods_alarm` int unsigned NOT NULL DEFAULT '0' COMMENT '库存预警值',
  `goods_code` varchar(50) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT '' COMMENT '订货编码',
  `goods_click` int unsigned NOT NULL DEFAULT '0' COMMENT '商品点击数量',
  `goods_salenum` int unsigned NOT NULL DEFAULT '0' COMMENT '销售数量',
  `goods_collect` int unsigned NOT NULL DEFAULT '0' COMMENT '收藏数量',
  `goods_image` varchar(500) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL DEFAULT '' COMMENT '商品主图',
  `goods_evaluation_good_star` tinyint unsigned NOT NULL DEFAULT '5' COMMENT '好评星级',
  `goods_evaluation_count` int unsigned NOT NULL DEFAULT '0' COMMENT '评价数',
  `goods_max_sale` int NOT NULL DEFAULT '0' COMMENT '单人最大购买数量',
  `goods_is_shelves` tinyint(1) NOT NULL DEFAULT '1' COMMENT '1-上架 2-下架 0店铺关闭手动下架',
  `goods_cubage` decimal(10,3) DEFAULT NULL COMMENT '商品重量',
  `commodity_bar_type_code` varchar(32) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '商品条型码',
  `goods_cost_price` decimal(10,2) DEFAULT NULL COMMENT '成本价',
  `is_delete` int DEFAULT '0' COMMENT '是否删除(0:否;1:是)',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
  `brand_id` int unsigned DEFAULT '0' COMMENT '品牌id',
  `goods_stock` int unsigned NOT NULL DEFAULT '0' COMMENT '商品库存',
  `merchant_code` varchar(32) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '商家编号',
  `goods_send_back` int DEFAULT NULL COMMENT '商品是否寄回样品 0:是 1:否',
  PRIMARY KEY (`supplier_goods_id`) USING BTREE,
  UNIQUE KEY `goods_id` (`supplier_goods_id`) USING BTREE,
  KEY `common_id` (`supplier_common_id`) USING BTREE,
  KEY `shop_id` (`supplier_shop_id`) USING BTREE,
  KEY `cat_id` (`supplier_cat_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 ROW_FORMAT=DYNAMIC COMMENT='供应商商品sku表';

CREATE TABLE IF NOT EXISTS `hou_supplier_goods_base_version` (
  `goods_base_ver_id` int NOT NULL AUTO_INCREMENT COMMENT '版本id',
  `ver_id` int DEFAULT NULL COMMENT '版本id',
  `common_id` int DEFAULT NULL COMMENT '商品id',
  `common_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '商品名称',
  `ver_body` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci COMMENT '商品数据',
  `shop_id` int DEFAULT NULL COMMENT '店铺id',
  `operate_user_id` varchar(18) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '操作人id',
  `operate_user_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '操作人名称',
  `operate_time` datetime DEFAULT NULL COMMENT '操作时间',
  `version_no` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '版本号',
  PRIMARY KEY (`goods_base_ver_id`) USING BTREE,
  KEY `goods_name_index` (`common_name`,`operate_user_name`,`operate_time`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci ROW_FORMAT=DYNAMIC COMMENT='商品sku版本';

CREATE TABLE IF NOT EXISTS `hou_supplier_goods_common_detail` (
  `supplier_common_id` int NOT NULL AUTO_INCREMENT,
  `common_body` text CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL COMMENT '商品内容',
  `product_file` text CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci COMMENT '商品文件',
  PRIMARY KEY (`supplier_common_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 ROW_FORMAT=DYNAMIC COMMENT='供应商商品公共内容详情表';

CREATE TABLE IF NOT EXISTS `hou_supplier_goods_common_stock` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '库存预警id',
  `supplier_goods_id` int NOT NULL COMMENT 'goodsId',
  `supplier_common_id` int NOT NULL COMMENT 'commonId',
  `goods_stock` int DEFAULT '0' COMMENT '商品库存',
  `goods_alarm` int DEFAULT '0' COMMENT '库存预警值',
  `create_user` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '创建者',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_user` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '修改人',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '修改时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci ROW_FORMAT=DYNAMIC COMMENT='供应商商品库存预警表';

CREATE TABLE IF NOT EXISTS `hou_supplier_goods_details_version` (
  `goods_details_ver_id` int NOT NULL AUTO_INCREMENT COMMENT '版本id',
  `ver_id` int DEFAULT NULL COMMENT '版本id',
  `common_id` int DEFAULT NULL COMMENT '商品id',
  `common_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '商品名称',
  `ver_body` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci COMMENT '商品数据',
  `shop_id` int DEFAULT NULL COMMENT '店铺id',
  `operate_user_id` varchar(18) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '操作人id',
  `operate_user_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '操作人名称',
  `operate_time` datetime DEFAULT NULL COMMENT '操作时间',
  `version_no` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '版本号',
  PRIMARY KEY (`goods_details_ver_id`) USING BTREE,
  KEY `goods_name_index` (`common_name`,`operate_user_name`,`operate_time`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci ROW_FORMAT=DYNAMIC COMMENT='商品详情版本';

CREATE TABLE IF NOT EXISTS `hou_supplier_goods_image_version` (
  `goods_image_ver_id` int NOT NULL AUTO_INCREMENT COMMENT '版本id',
  `ver_id` int DEFAULT NULL COMMENT '版本id',
  `common_id` int DEFAULT NULL COMMENT '商品id',
  `common_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '商品名称',
  `ver_body` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci COMMENT '商品数据',
  `shop_id` int DEFAULT NULL COMMENT '店铺id',
  `operate_user_id` varchar(18) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '操作人id',
  `operate_user_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '操作人名称',
  `operate_time` datetime DEFAULT NULL COMMENT '操作时间',
  `version_no` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '版本号',
  PRIMARY KEY (`goods_image_ver_id`) USING BTREE,
  KEY `goods_name_index` (`common_name`,`operate_user_name`,`operate_time`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci ROW_FORMAT=DYNAMIC COMMENT='商品图片版本';

CREATE TABLE IF NOT EXISTS `hou_supplier_goods_images` (
  `images_id` int unsigned NOT NULL AUTO_INCREMENT COMMENT '商品图片id',
  `supplier_common_id` int unsigned NOT NULL DEFAULT '0' COMMENT '商品公共内容id',
  `supplier_goods_id` int DEFAULT NULL COMMENT '商品id',
  `supplier_shop_id` int unsigned NOT NULL COMMENT '店铺id',
  `images_image` longtext CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL COMMENT '商品图片',
  `type` tinyint DEFAULT '1' COMMENT '主图类型： 1图片2视频',
  PRIMARY KEY (`images_id`) USING BTREE,
  KEY `common_id` (`supplier_common_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 ROW_FORMAT=DYNAMIC COMMENT='供应商商品图片表';

CREATE TABLE IF NOT EXISTS `hou_supplier_goods_version` (
  `ver_id` int NOT NULL AUTO_INCREMENT COMMENT '版本id',
  `common_id` int DEFAULT NULL COMMENT '商品id',
  `common_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '商品名称',
  `ver_body` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci COMMENT '商品数据',
  `shop_id` int DEFAULT NULL COMMENT '店铺id',
  `operate_user_id` varchar(18) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '操作人id',
  `operate_user_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '操作人名称',
  `operate_time` datetime DEFAULT NULL COMMENT '操作时间',
  `version_no` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '版本号',
  PRIMARY KEY (`ver_id`) USING BTREE,
  KEY `goods_name_index` (`common_name`,`operate_user_name`,`operate_time`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci ROW_FORMAT=DYNAMIC COMMENT='商品版本';

CREATE TABLE IF NOT EXISTS `hou_supplier_goods_yiyatong` (
  `id` int NOT NULL AUTO_INCREMENT,
  `common_id` int DEFAULT NULL COMMENT '商品commonId',
  `spu_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '贻亚通商品spuId',
  `goods_id` int DEFAULT NULL COMMENT '商品goodsId',
  `sku_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '贻亚通商品skuId',
  `cat_Id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '分类id',
  `shop_id` int DEFAULT NULL COMMENT '代发店铺id',
  `is_delete` int DEFAULT '0' COMMENT '是否删除0:否,1:是',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci ROW_FORMAT=DYNAMIC COMMENT='怡亚通商品代发记录';

CREATE TABLE IF NOT EXISTS `hou_supplier_order_settlement` (
  `os_id` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL COMMENT '结算单编号(年月店铺ID)',
  `os_start_date` datetime NOT NULL COMMENT '开始日期',
  `os_end_date` datetime NOT NULL COMMENT '结束日期',
  `os_order_amount` decimal(10,2) NOT NULL DEFAULT '0.00' COMMENT '订单金额',
  `os_shipping_amount` decimal(10,2) NOT NULL DEFAULT '0.00' COMMENT '运费',
  `os_order_return_amount` decimal(10,2) NOT NULL DEFAULT '0.00' COMMENT '退单金额',
  `os_commis_amount` decimal(10,2) NOT NULL DEFAULT '0.00' COMMENT '佣金金额',
  `os_commis_return_amount` decimal(10,2) NOT NULL DEFAULT '0.00' COMMENT '退还佣金',
  `os_shop_cost_amount` decimal(10,2) NOT NULL DEFAULT '0.00' COMMENT '店铺促销活动费用',
  `os_platform_bear_amount` decimal(10,2) NOT NULL DEFAULT '0.00' COMMENT '平台承担的店铺折扣金额',
  `os_platform_bear_return_amount` decimal(10,2) NOT NULL DEFAULT '0.00' COMMENT '退还的平台承担的折扣金额',
  `os_dealer_commission` decimal(10,2) NOT NULL DEFAULT '0.00' COMMENT '经销商佣金',
  `os_dealer_return_commission` decimal(10,2) NOT NULL DEFAULT '0.00' COMMENT '经销商退还佣金',
  `os_supply_commission` decimal(10,2) NOT NULL DEFAULT '0.00' COMMENT '供应商佣金',
  `os_supply_return_commission` decimal(10,2) NOT NULL DEFAULT '0.00' COMMENT '供应商退还佣金',
  `os_amount` decimal(10,2) NOT NULL DEFAULT '0.00' COMMENT '应结金额',
  `os_datetime` datetime NOT NULL COMMENT '生成结算单日期',
  `os_date` date NOT NULL COMMENT '结算单年月份',
  `os_state` tinyint NOT NULL DEFAULT '1' COMMENT '1默认(已出账)2店家已确认3平台已审核4结算完成 5已提现',
  `os_pay_date` datetime NOT NULL COMMENT '付款日期',
  `os_pay_content` varchar(200) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL DEFAULT '' COMMENT '支付备注',
  `supplier_shop_id` int unsigned NOT NULL COMMENT '供应商店铺ID',
  `shop_name` varchar(50) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL DEFAULT '' COMMENT '店铺名',
  `os_order_type` tinyint(1) NOT NULL COMMENT '结算订单类型 1-普通订单 2-商家一键上架供应商销售订单商家结算单  3商家一键上架供应商销售订单供应商结算单 4直播分佣结算',
  `os_redpacket_amount` decimal(10,2) NOT NULL DEFAULT '0.00' COMMENT '红包金额',
  `os_redpacket_return_amount` decimal(10,2) NOT NULL DEFAULT '0.00' COMMENT '退还红包',
  `district_id` mediumint NOT NULL DEFAULT '0' COMMENT '地区id,0表示全国',
  `os_is_display` tinyint(1) unsigned zerofill DEFAULT '1' COMMENT '结算单是否显示 0不显示 1显示',
  PRIMARY KEY (`os_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 ROW_FORMAT=DYNAMIC COMMENT='供应商订单结算表';

CREATE TABLE IF NOT EXISTS `hou_supplier_order_user_cart` (
  `cart_id` int NOT NULL AUTO_INCREMENT,
  `user_id` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '买家id',
  `supplier_shop_id` int DEFAULT NULL COMMENT '店铺id',
  `shop_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '店铺名称',
  `supplier_common_id` int DEFAULT NULL COMMENT '商品id',
  `supplier_goods_id` int DEFAULT NULL COMMENT '商品id',
  `goods_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '商品名称',
  `goods_image` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '商品图片',
  `goods_num` int DEFAULT NULL COMMENT '数量',
  `goods_stock` int DEFAULT NULL COMMENT '库存',
  `goods_spec_chineseValue` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '规格',
  `goods_moq` int DEFAULT NULL COMMENT '商品起订量',
  `cart_status` int DEFAULT '0' COMMENT '0 正常 1 删除 2 店铺关闭商品下架',
  `is_buy` int DEFAULT '0' COMMENT '类型 是否是购买的 1 是 0 不是',
  PRIMARY KEY (`cart_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci ROW_FORMAT=DYNAMIC COMMENT='进货车';

CREATE TABLE IF NOT EXISTS `hou_supplier_proc_config` (
  `config_key` varchar(50) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL COMMENT '数组下标',
  `config_value` text CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL COMMENT '数组值',
  `config_type` varchar(50) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL COMMENT '类型',
  `config_enable` tinyint(1) NOT NULL DEFAULT '1' COMMENT '状态值，1可用，0不可用',
  `config_comment` text CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL COMMENT '备注',
  `config_datatype` enum('string','json','number') CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL DEFAULT 'string' COMMENT '数据类型',
  PRIMARY KEY (`config_key`) USING BTREE,
  KEY `index` (`config_key`,`config_type`) USING BTREE COMMENT '(null)'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 ROW_FORMAT=DYNAMIC COMMENT='平台供应链配置表';

CREATE TABLE IF NOT EXISTS `hou_supplier_shop_base` (
  `supplier_shop_id` int unsigned NOT NULL COMMENT '供应商店铺id',
  `user_id` varchar(18) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT '0' COMMENT '用户id',
  `user_name` varchar(20) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT '' COMMENT '用户名称',
  `international_dialing_code` varchar(50) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT '+86' COMMENT '国际区号',
  `shop_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '店铺名称',
  `shop_self_support` enum('true','false') CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT 'false' COMMENT '是否自营',
  `shop_create_time` datetime DEFAULT NULL COMMENT '开店时间',
  `shop_settlement_cycle` mediumint DEFAULT '30' COMMENT '结算周期-天为单位-如果您想设置结算周期为一个月，则可以输入30',
  `shop_settlement_last_time` datetime DEFAULT NULL COMMENT '店铺上次结算时间，若是新开店铺没有结算单，则是开店日期',
  `shop_logo` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT '' COMMENT '店铺logo',
  `shop_status` int DEFAULT '0' COMMENT '店铺状态-3：开店成功 2:待审核付款 1:待审核资料  0:关闭',
  `shop_collect` int DEFAULT '0' COMMENT '店铺粉丝数',
  `shop_tel` varchar(12) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT '' COMMENT '卖家电话',
  `shop_free_shipping` int DEFAULT '0' COMMENT '免运费额度',
  `district_id` mediumint DEFAULT '0' COMMENT '所在地，使用最后一级分类',
  `shop_common_service` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '店铺售后服务',
  `shop_display_order` int DEFAULT '9999999' COMMENT '店铺默认排序',
  `payment_verify_status` tinyint(1) DEFAULT '0' COMMENT '付款信息审核1待审核 2审核通过 3审核失败',
  `shop_operator` varchar(32) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '审核操作人',
  `shop_operator_time` datetime DEFAULT NULL COMMENT '审核时间',
  `treasure_join_date` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '店铺审核通过时间',
  `shop_name_verify` tinyint DEFAULT '1' COMMENT '店铺修改审核1-正常，2-审核中,3-审核失败',
  `new_shop_name` varchar(50) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT '' COMMENT '新更改的店铺名',
  `shop_name_remark` varchar(100) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT '' COMMENT '店铺名审核备注',
  `shop_verify_status` int DEFAULT '1' COMMENT '店铺资质审核状态 1通过，0未通过，10审核中',
  `shop_verify_remark` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT '' COMMENT '店铺资质审核备注',
  `invoice_switch_status` int DEFAULT '0' COMMENT '发票功能开关状态:0-关闭;1-开启',
  `invoice_audit_status` int DEFAULT '3' COMMENT '发票开关审核状态:0-待审核；1-通过；2-不通过；3-未申请',
  `invoice_switch_reason` varchar(500) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '商家申请理由',
  `invoice_switch_create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '申请记录创建时间',
  `invoice_switch_update_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '申请记录更新时间',
  `platform_comment` varchar(500) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '平台审核备注',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
  `shop_type` tinyint DEFAULT '2' COMMENT '店铺类型: 1-卖家店铺; 2:供应商店铺',
  `shop_business` tinyint DEFAULT '0' COMMENT '0:个人入驻 1:企业入驻 2门店厂商3供应商',
  `shop_level_id` int DEFAULT '1' COMMENT '店铺等级',
  `is_upgrade_remind` int DEFAULT '1' COMMENT '是否进行了店铺等级升级提醒 0否 1是',
  PRIMARY KEY (`supplier_shop_id`) USING BTREE,
  KEY `shop_self_support` (`shop_self_support`) USING BTREE,
  KEY `shop_status` (`shop_status`) USING BTREE,
  KEY `shop_id` (`supplier_shop_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 ROW_FORMAT=DYNAMIC COMMENT='供应商店铺表';

CREATE TABLE IF NOT EXISTS `hou_supplier_shop_class_bind` (
  `shop_class_bind_id` int unsigned NOT NULL AUTO_INCREMENT COMMENT '经营类目id',
  `supplier_shop_id` int unsigned NOT NULL DEFAULT '0' COMMENT '供应商店铺id',
  `product_class_id` int unsigned NOT NULL DEFAULT '0' COMMENT '商品分类id',
  `commission_rate` decimal(4,2) NOT NULL COMMENT '百分比',
  `shop_class_bind_enable` tinyint(1) NOT NULL DEFAULT '1' COMMENT '是否启用',
  `supplier_join_id` varchar(50) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '入驻ID',
  PRIMARY KEY (`shop_class_bind_id`) USING BTREE,
  UNIQUE KEY `shop_id` (`supplier_shop_id`,`product_class_id`,`supplier_join_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 ROW_FORMAT=DYNAMIC COMMENT='供应商店铺经营类目-更新绑定类目时备份';

CREATE TABLE IF NOT EXISTS `hou_supplier_shop_class_bind_tmp` (
  `shop_class_bind_id` int unsigned NOT NULL AUTO_INCREMENT COMMENT 'id',
  `supplier_shop_id` int unsigned NOT NULL DEFAULT '0' COMMENT '店铺id',
  `supplier_cat_id` int DEFAULT NULL COMMENT '三级分类id',
  `batch_id` varchar(500) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '批次id',
  `start` int DEFAULT NULL COMMENT '状态 1审核中  2 审核通过 3 审核失败',
  `cat_name` varchar(500) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '分类名称 ',
  `creat_time` datetime DEFAULT NULL COMMENT '创建时间',
  `reason` text CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci COMMENT '原因',
  PRIMARY KEY (`shop_class_bind_id`) USING BTREE,
  UNIQUE KEY `shop_id` (`supplier_shop_id`,`supplier_cat_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 ROW_FORMAT=DYNAMIC COMMENT='店铺经营类目-申请绑定类目';

CREATE TABLE IF NOT EXISTS `hou_supplier_shop_join_base` (
  `supplier_join_id` varchar(50) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL COMMENT '入驻id',
  `contact_name` varchar(50) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT '' COMMENT '联系人姓名',
  `contact_phone` varchar(50) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT '' COMMENT '联系人手机',
  `international_dialing_code` varchar(50) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT '+86' COMMENT '国际区号',
  `true_name` varchar(50) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT '' COMMENT '真实姓名',
  `id_no` varchar(50) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT '' COMMENT '身份证号码',
  `id_pic_one` varchar(500) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT '' COMMENT '证件照正面',
  `id_pic_two` varchar(500) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT '' COMMENT '证件照反面',
  `business_licence_pic` varchar(500) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT '' COMMENT '营业执照照片',
  `manufacture_permit_pic` varchar(500) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT '' COMMENT '生产许可证',
  `open_permit_pic` varchar(500) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT '' COMMENT '开户许可证',
  `bl_province` int DEFAULT '0' COMMENT '营业执照所在地-省',
  `bl_city` int DEFAULT '0' COMMENT '营业执照所在地-市',
  `bl_area` int DEFAULT '0' COMMENT '营业执照所在地-区',
  `bl_address` varchar(500) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT '' COMMENT '营业执照所在地-详细地址',
  `validate_start_time` date DEFAULT NULL COMMENT '证件有效期开始时间',
  `validate_end_time` date DEFAULT NULL COMMENT '证件有效期结束时间',
  `validate_long_term` int DEFAULT '0' COMMENT '证件是否长期有效:1-是;0-否',
  `enterprise_name` varchar(200) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT '' COMMENT '公司名称',
  `tax_id` varchar(100) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT '' COMMENT '税号',
  `enterprise_phone` varchar(50) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT '' COMMENT '企业电话',
  `employee_num` int DEFAULT NULL COMMENT '员工人数',
  `register_money` decimal(11,2) DEFAULT NULL COMMENT '注册资金',
  `shop_name` varchar(100) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '店铺名称',
  `shop_province` int DEFAULT '0' COMMENT '店铺所在地-省',
  `shop_city` int DEFAULT '0' COMMENT '店铺所在地-市',
  `shop_area` int DEFAULT '0' COMMENT '店铺所在地-区',
  `shop_address` varchar(500) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT '' COMMENT '店铺所在地-详细地址',
  `shop_class` int DEFAULT NULL COMMENT '店铺分类',
  `corporate_agreement` varchar(500) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT '' COMMENT '合作协议',
  `audit_status` int DEFAULT '0' COMMENT '审核状态:0-待审核；1-审核通过；2-审核不通过',
  `user_id` varchar(18) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '用户ID',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `last_update_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '上次更新时间',
  `id_type` int DEFAULT '0' COMMENT '证件类型:0-身份证',
  `audit_comment` varchar(200) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT '' COMMENT '审核备注',
  `agreement_file_name` varchar(200) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '合作协议文件名称',
  `bank_name` varchar(50) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT '' COMMENT '开户银行',
  `bank_card_no` varchar(50) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT '' COMMENT '银行卡号',
  `bank_province` int DEFAULT NULL COMMENT '银行所在地-省',
  `bank_city` int DEFAULT NULL COMMENT '银行所在地-市',
  `bank_area` int DEFAULT NULL COMMENT '银行所在地-区',
  `bank_address` varchar(100) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT '' COMMENT '银行所在地-详细地址',
  `bank_branch_name` varchar(100) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT '' COMMENT '支行名称',
  `branch_id` varchar(100) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT '' COMMENT '支行银联号',
  `reg_address` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '营业制作地址',
  `shop_type` int DEFAULT '0' COMMENT '入驻类型:0-个人入驻;1-商家入驻;2-供应商入驻',
  `shop_level_id` int DEFAULT '1' COMMENT '店铺等级',
  PRIMARY KEY (`supplier_join_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 ROW_FORMAT=DYNAMIC COMMENT='供应商店铺入驻信息表';

CREATE TABLE IF NOT EXISTS `hou_supplier_shop_join_pay` (
  `pay_id` int NOT NULL AUTO_INCREMENT COMMENT '店铺入驻租金id',
  `contact_name` varchar(50) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT '' COMMENT '联系人姓名',
  `contact_phone` varchar(50) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT '' COMMENT '联系人手机',
  `shop_name` varchar(100) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '店铺名称',
  `shop_class` int DEFAULT NULL COMMENT '店铺分类',
  `shop_pay` decimal(10,2) DEFAULT NULL COMMENT '店铺租金',
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `last_update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '上次更新时间',
  `audit_status` int DEFAULT '0' COMMENT '审核状态:0-待商家上传付款凭证;1-待审核付款凭证;2-审核不通过；3-审核通过',
  `audit_comment` varchar(100) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '审核备注',
  `pay_image` varchar(500) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '支付图片',
  `pay_comment` varchar(500) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '支付说明',
  `user_id` varchar(18) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '用户ID',
  `upload_time` date DEFAULT NULL COMMENT '提交时间',
  PRIMARY KEY (`pay_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 ROW_FORMAT=DYNAMIC COMMENT='供应商店铺入驻租金表';

CREATE TABLE IF NOT EXISTS `hou_supplier_shop_level` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT 'id',
  `store_level` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '店铺等级名',
  `level_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '级别',
  `shop_list_first_show` tinyint(1) DEFAULT '0' COMMENT '店铺列表优先展示',
  `goods_list_first_show` tinyint(1) DEFAULT '0' COMMENT '商品列表优先展示',
  `marketing_activities` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '营销活动：1 优惠券 2 限时折扣 3 限时秒杀 4 满减赠折',
  `goods_is_limit` tinyint(1) DEFAULT '0' COMMENT '发布商品是否限制',
  `goods_count` int DEFAULT NULL COMMENT '可发布商品数',
  `image_is_limit` tinyint(1) DEFAULT '0' COMMENT '图片空间是否限制',
  `image_count` int DEFAULT NULL COMMENT '图片空间数',
  `expenses` decimal(10,2) DEFAULT NULL COMMENT '开店费用',
  `update_time` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci ROW_FORMAT=DYNAMIC COMMENT='店铺等级表';

CREATE TABLE IF NOT EXISTS `hou_supplier_shop_level_upgrade_record` (
  `record_id` int NOT NULL AUTO_INCREMENT COMMENT '记录_id',
  `supplier_shop_id` int DEFAULT NULL COMMENT '店铺_id',
  `before_shop_level_id` int DEFAULT NULL COMMENT '之前的店铺等级',
  `after_shop_level_id` int DEFAULT NULL COMMENT '升级的店铺等级',
  `audit_status` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '审核状态 0待审核 1审核成功 2审核失败',
  `audit_remarks` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '审核备注',
  `expenses` decimal(10,2) DEFAULT NULL COMMENT '补缴费用',
  `payment_voucher` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci COMMENT '支付凭证',
  `payment_remarks` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '支付备注',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  PRIMARY KEY (`record_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci ROW_FORMAT=DYNAMIC;

CREATE TABLE IF NOT EXISTS `hou_supplier_shop_retention_money` (
  `supplier_shop_id` int unsigned NOT NULL AUTO_INCREMENT COMMENT '供应商店铺id',
  `deposit` float(10,2) DEFAULT NULL COMMENT '保证金',
  `deposit_status` int NOT NULL DEFAULT '2' COMMENT '1:已缴纳2:尚未缴纳3审核中4审核失败',
  `deposit_remark` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT '' COMMENT '质保金审核备注',
  `payment_voucher` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '付款凭证',
  `payment_voucher_explain` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '付款凭证说明',
  `return_bond_status` int NOT NULL DEFAULT '1' COMMENT '5：退保成功2:退保申请3:退保审核4:审核成功1:正常',
  `return_bond_voucher` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL DEFAULT '' COMMENT '退保凭证',
  `return_bond_reason` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL DEFAULT '' COMMENT '退保原因',
  `bank_account_name` varchar(50) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT '' COMMENT '银行开户名',
  `bank_account_number` varchar(50) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '公司银行账号',
  `bank_name` varchar(50) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '开户银行支行名称',
  `bank_code` varchar(20) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '开户银行支行联行号',
  `bank_address` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '开户银行支行所在地',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `return_bond_remark` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT '' COMMENT '退保备注',
  PRIMARY KEY (`supplier_shop_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 ROW_FORMAT=DYNAMIC COMMENT='供应商店铺质保金信息表';

CREATE TABLE IF NOT EXISTS `hou_supplier_upload_album` (
  `album_id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '商品图片Id',
  `album_name` varchar(100) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL DEFAULT '' COMMENT '商品图片地址',
  `album_cover` varchar(100) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL DEFAULT '' COMMENT '封面',
  `album_desc` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL DEFAULT '' COMMENT '描述',
  `album_is_default` tinyint unsigned NOT NULL DEFAULT '0' COMMENT '默认相册，1是，0否',
  `album_display_order` int NOT NULL DEFAULT '255' COMMENT '排序',
  `album_type` varchar(50) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL DEFAULT 'image' COMMENT '附件册',
  `user_id` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL DEFAULT '0' COMMENT '所属用户id',
  `supplier_shop_id` int unsigned NOT NULL DEFAULT '0',
  PRIMARY KEY (`album_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 ROW_FORMAT=DYNAMIC COMMENT='用户相册表';

CREATE TABLE IF NOT EXISTS `hou_supplier_upload_base` (
  `upload_id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '商品图片Id',
  `album_id` bigint NOT NULL,
  `user_id` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL COMMENT '用户id',
  `supplier_shop_id` int unsigned NOT NULL DEFAULT '0' COMMENT '店铺id',
  `upload_url_prefix` varchar(500) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL DEFAULT '',
  `upload_path` varchar(500) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL DEFAULT '',
  `upload_type` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL DEFAULT 'image' COMMENT 'image|video|',
  `upload_size` int NOT NULL COMMENT '原文件大小',
  `upload_name` text CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL COMMENT '附件标题',
  `upload_time` int DEFAULT NULL COMMENT '附件日期',
  PRIMARY KEY (`upload_id`) USING BTREE,
  KEY `album_id` (`user_id`,`album_id`,`upload_type`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 ROW_FORMAT=DYNAMIC COMMENT='用户附件表-图片、视频';

CREATE TABLE IF NOT EXISTS `hou_supplier_yiyatong_cat` (
  `id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '分类id',
  `name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '分类名称',
  `pid` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '父id',
  `level` int DEFAULT NULL COMMENT '等级',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci ROW_FORMAT=DYNAMIC COMMENT='贻亚通商品分类表';

CREATE TABLE IF NOT EXISTS `hou_supplier_yiyatong_msg_callback` (
  `id` int NOT NULL AUTO_INCREMENT,
  `message_id` varchar(15) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '消息id',
  `app_key` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '应用密钥',
  `enum_num` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '枚举编号',
  `content` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '消息内容',
  `create_time` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '创建时间',
  `sign` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '签名',
  `is_use` int DEFAULT '0' COMMENT '是否已消费:0:未消费,1:已消费',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci ROW_FORMAT=DYNAMIC COMMENT='怡亚通消息回调记录表';

SET FOREIGN_KEY_CHECKS=@OWNED_OLD_FOREIGN_KEY_CHECKS;
