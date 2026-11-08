-- =====================================================================
-- 149 · 商品扩展模块建表（属性/品牌/内容/标签/仓储/草稿/足迹/易亚通）
--
-- 背景：`backend/paas/goods` 有 59 个实体，而 meoc 库只建了其中 22 个对应的表，
--       剩下 37 个模块的表缺失，导致这些模块的接口一律 SQLSyntaxError（20602）。
-- 口径：本库表名统一带 `hou_` 前缀，脚本按 `hou_<entity_table>` 建表；
--       字段以实体类（`@TableName` / `@TableId` / `@TableField(exist=false)`）为准反推，
--       非库字段（List/Map/@TableField(exist=false)）不建列。
-- 幂等：全部 `CREATE TABLE IF NOT EXISTS`，重复执行不报错、不动已有表。
-- 说明：这些表建成后是空表，对应功能需在后台录入数据后才有内容。
-- =====================================================================

SET NAMES utf8mb4;
CREATE TABLE IF NOT EXISTS `hou_goods_article_info` (
  `article_id` varchar(255) NOT NULL COMMENT 'articleId',
  `user_id` varchar(255) COMMENT 'userId',
  `article_title` varchar(255) COMMENT 'articleTitle',
  `article_content` text COMMENT 'articleContent',
  `article_cover` varchar(255) COMMENT 'articleCover',
  `read_num` int(11) COMMENT 'readNum',
  `remark_num` int(11) COMMENT 'remarkNum',
  `audit_state` int(11) COMMENT 'auditState',
  `audit_comment` varchar(255) COMMENT 'auditComment',
  `common_id` int(11) COMMENT 'commonId',
  `tag_id` int(11) COMMENT 'tagId',
  `create_time` datetime COMMENT 'createTime',
  `last_updated_time` datetime COMMENT 'lastUpdatedTime',
  `goods_id` int(11) COMMENT 'goodsId',
  `cat_id` int(11) COMMENT 'catId',
  `article_type` int(11) COMMENT 'articleType',
  `like_num` int(11) COMMENT 'likeNum',
  `star_num` int(11) COMMENT 'starNum',
  PRIMARY KEY (`article_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='社区心得表';

CREATE TABLE IF NOT EXISTS `hou_goods_article_like` (
  `like_id` int(11) NOT NULL AUTO_INCREMENT COMMENT 'likeId',
  `article_id` varchar(255) COMMENT 'articleId',
  `liked_id` varchar(255) COMMENT 'likedId',
  `like_user` varchar(255) COMMENT 'likeUser',
  `liked_user` varchar(255) COMMENT 'likedUser',
  `like_type` int(11) COMMENT 'likeType',
  `create_time` datetime COMMENT 'createTime',
  `last_updated_time` datetime COMMENT 'lastUpdatedTime',
  `is_read` int(11) COMMENT 'isRead',
  PRIMARY KEY (`like_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='@ClassName';

CREATE TABLE IF NOT EXISTS `hou_goods_article_remark` (
  `remark_id` int(11) NOT NULL AUTO_INCREMENT COMMENT 'remarkId',
  `article_id` varchar(255) COMMENT 'articleId',
  `remark_content` text COMMENT 'remarkContent',
  `remarked_id` text COMMENT 'remarkedId',
  `remark_user` text COMMENT 'remarkUser',
  `remarked_user` text COMMENT 'remarkedUser',
  `remark_type` int(11) COMMENT 'remarkType',
  `audit_state` int(11) COMMENT 'auditState',
  `create_time` datetime COMMENT 'createTime',
  `last_updated_time` datetime COMMENT 'lastUpdatedTime',
  `audit_comment` varchar(255) COMMENT 'auditComment',
  `like_num` int(11) COMMENT 'likeNum',
  `is_read` int(11) COMMENT 'isRead',
  PRIMARY KEY (`remark_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='社区心得评论表';

CREATE TABLE IF NOT EXISTS `hou_goods_article_resource` (
  `resource_id` int(11) NOT NULL AUTO_INCREMENT COMMENT 'resourceId',
  `article_id` varchar(255) COMMENT 'articleId',
  `resource_url` text COMMENT 'resourceUrl',
  `resource_type` int(11) COMMENT 'resourceType',
  `create_time` datetime COMMENT 'createTime',
  `last_updated_time` datetime COMMENT 'lastUpdatedTime',
  PRIMARY KEY (`resource_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='@ClassName';

CREATE TABLE IF NOT EXISTS `hou_goods_article_star` (
  `star_id` int(11) NOT NULL AUTO_INCREMENT COMMENT 'starId',
  `article_id` varchar(255) COMMENT 'articleId',
  `star_user` int(11) COMMENT 'starUser',
  `starred_user` int(11) COMMENT 'starredUser',
  `create_time` datetime COMMENT 'createTime',
  `last_updated_time` datetime COMMENT 'lastUpdatedTime',
  `is_read` int(11) COMMENT 'isRead',
  PRIMARY KEY (`star_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='@ClassName';

CREATE TABLE IF NOT EXISTS `hou_goods_article_user_follow` (
  `follow_id` int(11) NOT NULL AUTO_INCREMENT COMMENT 'followId',
  `follow_user_id` int(11) COMMENT 'followUserId',
  `followed_user_id` int(11) COMMENT 'followedUserId',
  `create_time` datetime COMMENT 'createTime',
  `last_updated_time` datetime COMMENT 'lastUpdatedTime',
  `is_read` int(11) COMMENT 'isRead',
  PRIMARY KEY (`follow_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='@ClassName';

CREATE TABLE IF NOT EXISTS `hou_goods_attribute` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT 'id',
  `cat_id` int(11) COMMENT 'catId',
  `commodity_attribute` varchar(255) COMMENT 'commodityAttribute',
  `attribute_status` int(11) COMMENT 'attributeStatus',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='@ClassName';

CREATE TABLE IF NOT EXISTS `hou_goods_attribute_content` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT 'id',
  `attribute_content` text COMMENT 'attributeContent',
  `attribute_content_id` int(11) COMMENT 'attributeContentId',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='@ClassName';

CREATE TABLE IF NOT EXISTS `hou_goods_attribute_group` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT 'id',
  `name` varchar(255) COMMENT 'name',
  `remark` text COMMENT 'remark',
  `create_time` datetime COMMENT 'createTime',
  `update_time` datetime COMMENT 'updateTime',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='属性组表';

CREATE TABLE IF NOT EXISTS `hou_goods_attribute_group_relation` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT 'id',
  `attribute_group_id` int(11) COMMENT 'attributeGroupId',
  `attribute_type_id` int(11) COMMENT 'attributeTypeId',
  `attribute_type_name` varchar(255) COMMENT 'attributeTypeName',
  `is_filter` int(11) COMMENT 'isFilter',
  `sort` int(11) COMMENT 'sort',
  `create_time` datetime COMMENT 'createTime',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='属性组关联表';

CREATE TABLE IF NOT EXISTS `hou_goods_attribute_type` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT 'id',
  `name` varchar(255) COMMENT 'name',
  `remark` text COMMENT 'remark',
  `value_count` int(11) COMMENT 'valueCount',
  `create_time` datetime COMMENT 'createTime',
  `update_time` datetime COMMENT 'updateTime',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='属性类型表';

CREATE TABLE IF NOT EXISTS `hou_goods_attribute_type_value` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT 'id',
  `attribute_type_id` int(11) COMMENT 'attributeTypeId',
  `value_name` varchar(255) COMMENT 'valueName',
  `sort` int(11) COMMENT 'sort',
  `create_time` datetime COMMENT 'createTime',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='属性类型值表';

CREATE TABLE IF NOT EXISTS `hou_goods_attribute_value` (
  `attribute_value_id` int(11) NOT NULL AUTO_INCREMENT COMMENT 'attributeValueId',
  `attribute_value_name` varchar(255) COMMENT 'attributeValueName',
  `attribute_id` int(11) COMMENT 'attributeId',
  `display_order` int(11) COMMENT 'displayOrder',
  PRIMARY KEY (`attribute_value_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品属性值表(MerchantGoodsAttributeValue)表实体类';

CREATE TABLE IF NOT EXISTS `hou_goods_base` (
  `goods_id` int(11) NOT NULL AUTO_INCREMENT COMMENT 'goodsId',
  `common_id` int(11) COMMENT 'commonId',
  `shop_id` int(11) COMMENT 'shopId',
  `shop_name` varchar(255) COMMENT 'shopName',
  `goods_name` varchar(255) COMMENT 'goodsName',
  `goods_promotion_tips` varchar(255) COMMENT 'goodsPromotionTips',
  `cat_id` int(11) COMMENT 'catId',
  `brand_id` int(11) COMMENT 'brandId',
  `goods_spec` varchar(255) COMMENT 'goodsSpec',
  `goods_price` decimal(14,2) COMMENT 'goodsPrice',
  `goods_market_price` decimal(14,2) COMMENT 'goodsMarketPrice',
  `goods_shop_gate_stock` int(11) COMMENT 'goodsShopGateStock',
  `goods_alarm` int(11) COMMENT 'goodsAlarm',
  `goods_code` varchar(255) COMMENT 'goodsCode',
  `goods_is_recommend` int(11) COMMENT 'goodsIsRecommend',
  `goods_click` int(11) COMMENT 'goodsClick',
  `goods_salenum` int(11) COMMENT 'goodsSalenum',
  `goods_collect` int(11) COMMENT 'goodsCollect',
  `goods_image` text COMMENT 'goodsImage',
  `goods_evaluation_good_star` int(11) COMMENT 'goodsEvaluationGoodStar',
  `goods_evaluation_count` int(11) COMMENT 'goodsEvaluationCount',
  `goods_max_sale` int(11) COMMENT 'goodsMaxSale',
  `goods_is_shelves` int(11) COMMENT 'goodsIsShelves',
  `goods_cubage` decimal(14,2) COMMENT 'goodsCubage',
  `goods_min_sale` int(11) COMMENT 'goodsMinSale',
  `merchant_code` varchar(255) COMMENT 'merchantCode',
  `commodity_bar_type_code` varchar(255) COMMENT 'commodityBarTypeCode',
  `spec_value` varchar(255) COMMENT 'specValue',
  `goods_volume` decimal(14,2) COMMENT 'goodsVolume',
  `goods_cost_price` decimal(14,2) COMMENT 'goodsCostPrice',
  `is_delete` int(11) COMMENT 'isDelete',
  `create_user` varchar(255) COMMENT 'createUser',
  `create_time` datetime COMMENT 'createTime',
  `update_user` varchar(255) COMMENT 'updateUser',
  `update_time` datetime COMMENT 'updateTime',
  `ts` datetime COMMENT 'ts',
  `need_integral` int(11) COMMENT 'needIntegral',
  `msrp` varchar(255) COMMENT 'msrp',
  PRIMARY KEY (`goods_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品表(GoodsBase)';

CREATE TABLE IF NOT EXISTS `hou_goods_base_version` (
  `goods_base_ver_id` int(11) NOT NULL AUTO_INCREMENT COMMENT 'goodsBaseVerId',
  `ver_id` int(11) COMMENT 'verId',
  `common_id` int(11) COMMENT 'commonId',
  `common_name` varchar(255) COMMENT 'commonName',
  `ver_body` varchar(255) COMMENT 'verBody',
  `shop_id` int(11) COMMENT 'shopId',
  `operate_user_id` varchar(255) COMMENT 'operateUserId',
  `operate_user_name` varchar(255) COMMENT 'operateUserName',
  `operate_time` datetime COMMENT 'operateTime',
  `version_no` varchar(255) COMMENT 'versionNo',
  PRIMARY KEY (`goods_base_ver_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品sku版本(GoodsBaseVersion)表实体类';

CREATE TABLE IF NOT EXISTS `hou_goods_brand` (
  `brand_id` int(11) NOT NULL AUTO_INCREMENT COMMENT 'brandId',
  `brand_name` varchar(255) COMMENT 'brandName',
  `brand_initial` varchar(255) COMMENT 'brandInitial',
  `brand_pic` text COMMENT 'brandPic',
  `brand_hf` varchar(255) COMMENT 'brandHf',
  PRIMARY KEY (`brand_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品品牌表';

CREATE TABLE IF NOT EXISTS `hou_goods_brand_cat` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT 'id',
  `cat_id` int(11) COMMENT 'catId',
  `brand_id` int(11) COMMENT 'brandId',
  `brand_name` varchar(255) COMMENT 'brandName',
  `brand_pic` text COMMENT 'brandPic',
  `brand_hf` varchar(255) COMMENT 'brandHf',
  `cat_name` varchar(255) COMMENT 'catName',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='@ClassName';

CREATE TABLE IF NOT EXISTS `hou_goods_brand_type` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT 'id',
  `name` varchar(255) COMMENT 'name',
  `sort` int(11) COMMENT 'sort',
  `create_time` datetime COMMENT 'createTime',
  `update_time` datetime COMMENT 'updateTime',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='品牌类型表';

CREATE TABLE IF NOT EXISTS `hou_goods_brand_type_relation` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT 'id',
  `brand_type_id` int(11) COMMENT 'brandTypeId',
  `brand_id` int(11) COMMENT 'brandId',
  `create_time` datetime COMMENT 'createTime',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='品牌类型关联表';

CREATE TABLE IF NOT EXISTS `hou_goods_cat` (
  `cat_id` int(11) NOT NULL COMMENT 'catId',
  `cat_name` varchar(255) COMMENT 'catName',
  `cat_parent_id` int(11) COMMENT 'catParentId',
  `cat_pic` text COMMENT 'catPic',
  `type_id` int(11) COMMENT 'typeId',
  `cat_commission` float COMMENT 'catCommission',
  `cat_is_virtual` int(11) COMMENT 'catIsVirtual',
  `cat_displayorder` int(11) COMMENT 'catDisplayorder',
  `cat_level` int(11) COMMENT 'catLevel',
  `cat_show_status` int(11) COMMENT 'catShowStatus',
  `baby_commission` decimal(14,2) COMMENT 'babyCommission',
  `bigbaby_commission` decimal(14,2) COMMENT 'bigbabyCommission',
  `cat_frist_parent_id` int(11) COMMENT 'catFristParentId',
  `is_integral_goods` int(11) COMMENT 'isIntegralGoods',
  PRIMARY KEY (`cat_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='@ClassName GoodsCatEntity';

CREATE TABLE IF NOT EXISTS `hou_goods_category_attributes` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT 'id',
  `common_id` int(11) COMMENT 'commonId',
  `attribute_id` int(11) COMMENT 'attributeId',
  `attribute_name` varchar(255) COMMENT 'attributeName',
  `attribute_value_name` varchar(255) COMMENT 'attributeValueName',
  `is_search` int(11) COMMENT 'isSearch',
  `is_required` int(11) COMMENT 'isRequired',
  `display_order` int(11) COMMENT 'displayOrder',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品设置的类目属性表(GoodsCategoryAttributes)表实体类';

CREATE TABLE IF NOT EXISTS `hou_goods_common` (
  `common_id` int(11) NOT NULL AUTO_INCREMENT COMMENT 'commonId',
  `common_name` varchar(255) COMMENT 'commonName',
  `common_promotion_tips` varchar(255) COMMENT 'commonPromotionTips',
  `cat_id` int(11) COMMENT 'catId',
  `cat_name` varchar(255) COMMENT 'catName',
  `shop_id` int(11) COMMENT 'shopId',
  `shop_name` varchar(255) COMMENT 'shopName',
  `shop_goods_cat_id` varchar(255) COMMENT 'shopGoodsCatId',
  `goods_id` varchar(255) COMMENT 'goodsId',
  `shop_self_support` int(11) COMMENT 'shopSelfSupport',
  `shop_status` int(11) COMMENT 'shopStatus',
  `common_spec_value` varchar(255) COMMENT 'commonSpecValue',
  `brand_id` int(11) COMMENT 'brandId',
  `brand_name` varchar(255) COMMENT 'brandName',
  `common_image` text COMMENT 'commonImage',
  `common_state` int(11) COMMENT 'commonState',
  `common_verify` int(11) COMMENT 'commonVerify',
  `common_add_time` datetime COMMENT 'commonAddTime',
  `common_sell_time` datetime COMMENT 'commonSellTime',
  `common_price` decimal(14,2) COMMENT 'commonPrice',
  `common_market_price` decimal(14,2) COMMENT 'commonMarketPrice',
  `common_cost_price` decimal(14,2) COMMENT 'commonCostPrice',
  `common_stock` int(11) COMMENT 'commonStock',
  `common_alarm` int(11) COMMENT 'commonAlarm',
  `common_cubage` decimal(14,2) COMMENT 'commonCubage',
  `common_collect` int(11) COMMENT 'commonCollect',
  `common_evaluate` int(11) COMMENT 'commonEvaluate',
  `common_salenum` int(11) COMMENT 'commonSalenum',
  `common_is_recommend` int(11) COMMENT 'commonIsRecommend',
  `common_is_xian` int(11) COMMENT 'commonIsXian',
  `district_id` int(11) COMMENT 'districtId',
  `supply_shop_id` int(11) COMMENT 'supplyShopId',
  `common_parent_id` int(11) COMMENT 'commonParentId',
  `goods_recommended_min_price` decimal(14,2) COMMENT 'goodsRecommendedMinPrice',
  `goods_recommended_max_price` decimal(14,2) COMMENT 'goodsRecommendedMaxPrice',
  `product_distributor_flag` int(11) COMMENT 'productDistributorFlag',
  `transport_area_id` int(11) COMMENT 'transportAreaId',
  `international_transport_area` int(11) COMMENT 'internationalTransportArea',
  `transport_template` int(11) COMMENT 'transportTemplate',
  `international_transport_template` int(11) COMMENT 'internationalTransportTemplate',
  `common_update_by` varchar(255) COMMENT 'commonUpdateBy',
  `common_update_time` datetime COMMENT 'commonUpdateTime',
  `common_is_locked` int(11) COMMENT 'commonIsLocked',
  `restriction_times` int(11) COMMENT 'restrictionTimes',
  `restriction_hours` int(11) COMMENT 'restrictionHours',
  `order_operator` varchar(255) COMMENT 'orderOperator',
  `order_operator_time` datetime COMMENT 'orderOperatorTime',
  `common_click` int(11) COMMENT 'commonClick',
  `type` int(11) COMMENT 'type',
  `common_video` varchar(255) COMMENT 'commonVideo',
  `common_is_screen` int(11) COMMENT 'commonIsScreen',
  `common_verify_time` datetime COMMENT 'commonVerifyTime',
  `submit_verify_time` datetime COMMENT 'submitVerifyTime',
  `spec_param` varchar(255) COMMENT 'specParam',
  `import_number` varchar(255) COMMENT 'importNumber',
  `create_user` varchar(255) COMMENT 'createUser',
  `update_time` datetime COMMENT 'updateTime',
  `is_delete` int(11) COMMENT 'isDelete',
  `ts` datetime COMMENT 'ts',
  `virtual_goods` int(11) COMMENT 'virtualGoods',
  `period_of_validity` datetime COMMENT 'periodOfValidity',
  `past_due` int(11) COMMENT 'pastDue',
  `integral_goods` int(11) COMMENT 'integralGoods',
  `common_is_shop_take` int(11) COMMENT 'commonIsShopTake',
  `goods_type` int(11) COMMENT 'goodsType',
  `dealer_goods_flag` int(11) COMMENT 'dealerGoodsFlag',
  `plus_goods` int(11) COMMENT 'plusGoods',
  `plus_time` datetime COMMENT 'plusTime',
  `plu` varchar(255) COMMENT 'plu',
  `unit` varchar(255) COMMENT 'unit',
  `shelf` varchar(255) COMMENT 'shelf',
  `region` varchar(255) COMMENT 'region',
  `remark` text COMMENT 'remark',
  `image_list` text COMMENT 'imageList',
  `is_gift_products` int(11) COMMENT 'isGiftProducts',
  `fixed_rewards` decimal(14,2) COMMENT 'fixedRewards',
  `supplier_source` int(11) COMMENT 'supplierSource',
  `points_rate` double COMMENT 'pointsRate',
  PRIMARY KEY (`common_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品表(GoodsCommon)';

CREATE TABLE IF NOT EXISTS `hou_goods_common_attribute_association` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT 'id',
  `common_id` int(11) COMMENT 'commonId',
  `attribute_id` int(11) COMMENT 'attributeId',
  `attribute_content_id` int(11) COMMENT 'attributeContentId',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='(MerchantGoodsCommonAttributeAssociation)';

CREATE TABLE IF NOT EXISTS `hou_goods_common_detail` (
  `common_id` int(11) NOT NULL COMMENT 'commonId',
  `common_body` varchar(255) COMMENT 'commonBody',
  `ts` datetime COMMENT 'ts',
  PRIMARY KEY (`common_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='@ClassName';

CREATE TABLE IF NOT EXISTS `hou_goods_details_version` (
  `goods_details_ver_id` int(11) NOT NULL AUTO_INCREMENT COMMENT 'goodsDetailsVerId',
  `ver_id` int(11) COMMENT 'verId',
  `common_id` int(11) COMMENT 'commonId',
  `common_name` varchar(255) COMMENT 'commonName',
  `ver_body` varchar(255) COMMENT 'verBody',
  `shop_id` int(11) COMMENT 'shopId',
  `operate_user_id` varchar(255) COMMENT 'operateUserId',
  `operate_user_name` varchar(255) COMMENT 'operateUserName',
  `operate_time` datetime COMMENT 'operateTime',
  `version_no` varchar(255) COMMENT 'versionNo',
  PRIMARY KEY (`goods_details_ver_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品详情版本(GoodsDetailsVersion)表实体类';

CREATE TABLE IF NOT EXISTS `hou_goods_draft` (
  `draft_id` int(11) NOT NULL AUTO_INCREMENT COMMENT 'draftId',
  `draft_content` text COMMENT 'draftContent',
  `cat_id` int(11) COMMENT 'catId',
  `shop_id` int(11) COMMENT 'shopId',
  `create_time` datetime COMMENT 'createTime',
  `draft_name` varchar(255) COMMENT 'draftName',
  PRIMARY KEY (`draft_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='@ClassName GoodsDraftEntity';

CREATE TABLE IF NOT EXISTS `hou_goods_food_exhibition_hall` (
  `exhibition_hall_id` int(11) NOT NULL AUTO_INCREMENT COMMENT 'exhibitionHallId',
  `exhibition_hall_name` varchar(255) COMMENT 'exhibitionHallName',
  `exhibition_hall_image` text COMMENT 'exhibitionHallImage',
  `exhibition_hall_logo` text COMMENT 'exhibitionHallLogo',
  `exhibition_hall_logo_wap` text COMMENT 'exhibitionHallLogoWap',
  `exhibition_hall_desc1` text COMMENT 'exhibitionHallDesc1',
  `exhibition_hall_desc2` text COMMENT 'exhibitionHallDesc2',
  `exhibition_hall_desc3` text COMMENT 'exhibitionHallDesc3',
  `goods_id` varchar(255) COMMENT 'goodsId',
  `publish_time` datetime COMMENT 'publishTime',
  `exhibition_hall_status` tinyint(1) COMMENT 'exhibitionHallStatus',
  PRIMARY KEY (`exhibition_hall_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='美食中国表';

CREATE TABLE IF NOT EXISTS `hou_goods_group_bind_attribute` (
  `bind_id` int(11) NOT NULL AUTO_INCREMENT COMMENT 'bindId',
  `group_id` int(11) COMMENT 'groupId',
  `attribute_id` int(11) COMMENT 'attributeId',
  `is_search` int(11) COMMENT 'isSearch',
  `is_required` int(11) COMMENT 'isRequired',
  `display_order` int(11) COMMENT 'displayOrder',
  PRIMARY KEY (`bind_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品属性组绑定属性表(MerchantGoodsGroupBindAttribute)表实体类';

CREATE TABLE IF NOT EXISTS `hou_goods_image_version` (
  `goods_image_ver_id` int(11) NOT NULL AUTO_INCREMENT COMMENT 'goodsImageVerId',
  `ver_id` int(11) COMMENT 'verId',
  `common_id` int(11) COMMENT 'commonId',
  `common_name` varchar(255) COMMENT 'commonName',
  `ver_body` varchar(255) COMMENT 'verBody',
  `shop_id` int(11) COMMENT 'shopId',
  `operate_user_id` varchar(255) COMMENT 'operateUserId',
  `operate_user_name` varchar(255) COMMENT 'operateUserName',
  `operate_time` datetime COMMENT 'operateTime',
  `version_no` varchar(255) COMMENT 'versionNo',
  PRIMARY KEY (`goods_image_ver_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品图片版本(GoodsImageVersion)表实体类';

CREATE TABLE IF NOT EXISTS `hou_goods_images` (
  `images_id` int(11) NOT NULL AUTO_INCREMENT COMMENT 'imagesId',
  `common_id` int(11) COMMENT 'commonId',
  `goods_id` int(11) COMMENT 'goodsId',
  `shop_id` int(11) COMMENT 'shopId',
  `images_image` text COMMENT 'imagesImage',
  `images_displayorder` int(11) COMMENT 'imagesDisplayorder',
  `images_is_default` int(11) COMMENT 'imagesIsDefault',
  `type` int(11) COMMENT 'type',
  `ts` datetime COMMENT 'ts',
  PRIMARY KEY (`images_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='@ClassName';

CREATE TABLE IF NOT EXISTS `hou_goods_jingdong_dropshipping` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT 'id',
  `common_id` int(11) COMMENT 'commonId',
  `spu_id` varchar(255) COMMENT 'spuId',
  `goods_id` int(11) COMMENT 'goodsId',
  `sku_id` varchar(255) COMMENT 'skuId',
  `shop_id` int(11) COMMENT 'shopId',
  `is_delete` int(11) COMMENT 'isDelete',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='京东商品代发记录(GoodsJingdongDropshipping)表实体类';

CREATE TABLE IF NOT EXISTS `hou_goods_shop_cat_version` (
  `goods_shop_cat_ver_id` int(11) NOT NULL AUTO_INCREMENT COMMENT 'goodsShopCatVerId',
  `ver_id` int(11) COMMENT 'verId',
  `common_id` int(11) COMMENT 'commonId',
  `common_name` varchar(255) COMMENT 'commonName',
  `ver_body` varchar(255) COMMENT 'verBody',
  `shop_id` int(11) COMMENT 'shopId',
  `operate_user_id` varchar(255) COMMENT 'operateUserId',
  `operate_user_name` varchar(255) COMMENT 'operateUserName',
  `operate_time` datetime COMMENT 'operateTime',
  `version_no` varchar(255) COMMENT 'versionNo',
  PRIMARY KEY (`goods_shop_cat_ver_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品店铺分类版本(GoodsShopCatVersion)表实体类';

CREATE TABLE IF NOT EXISTS `hou_goods_stock` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT 'id',
  `goods_id` int(11) COMMENT 'goodsId',
  `common_id` int(11) COMMENT 'commonId',
  `goods_stock` int(11) COMMENT 'goodsStock',
  `goods_alarm` int(11) COMMENT 'goodsAlarm',
  `create_user` varchar(255) COMMENT 'createUser',
  `create_time` datetime COMMENT 'createTime',
  `update_user` varchar(255) COMMENT 'updateUser',
  `update_time` datetime COMMENT 'updateTime',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品库存表';

CREATE TABLE IF NOT EXISTS `hou_goods_tag_class_info` (
  `class_id` int(11) NOT NULL AUTO_INCREMENT COMMENT 'classId',
  `class_name` varchar(255) COMMENT 'className',
  `is_active` int(11) COMMENT 'isActive',
  `create_time` datetime COMMENT 'createTime',
  `last_updated_time` datetime COMMENT 'lastUpdatedTime',
  PRIMARY KEY (`class_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='话题分类表';

CREATE TABLE IF NOT EXISTS `hou_goods_tag_info` (
  `tag_id` int(11) NOT NULL AUTO_INCREMENT COMMENT 'tagId',
  `class_id` int(11) COMMENT 'classId',
  `tag_content` text COMMENT 'tagContent',
  `tag_subtitle` varchar(255) COMMENT 'tagSubtitle',
  `tag_cover` varchar(255) COMMENT 'tagCover',
  `is_active` int(11) COMMENT 'isActive',
  `create_time` datetime COMMENT 'createTime',
  `last_updated_time` datetime COMMENT 'lastUpdatedTime',
  PRIMARY KEY (`tag_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='社区话题表';

CREATE TABLE IF NOT EXISTS `hou_goods_type` (
  `type_id` int(11) NOT NULL AUTO_INCREMENT COMMENT 'typeId',
  `type_name` varchar(255) COMMENT 'typeName',
  `type_displayorder` int(11) COMMENT 'typeDisplayorder',
  `type_draft` tinyint(1) COMMENT 'typeDraft',
  PRIMARY KEY (`type_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品类型表-要取消各种快捷定位';

CREATE TABLE IF NOT EXISTS `hou_goods_type_brand` (
  `type_brand_id` int(11) NOT NULL AUTO_INCREMENT COMMENT 'typeBrandId',
  `type_id` int(11) COMMENT 'typeId',
  `brand_id` int(11) COMMENT 'brandId',
  PRIMARY KEY (`type_brand_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品类型与品牌对应表';

CREATE TABLE IF NOT EXISTS `hou_goods_type_spec` (
  `type_spec_id` int(11) NOT NULL AUTO_INCREMENT COMMENT 'typeSpecId',
  `type_id` int(11) COMMENT 'typeId',
  `spec_id` int(11) COMMENT 'specId',
  PRIMARY KEY (`type_spec_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品类型与规格对应表';

CREATE TABLE IF NOT EXISTS `hou_goods_user_footprint` (
  `footprint_id` int(11) NOT NULL AUTO_INCREMENT COMMENT 'footprintId',
  `user_id` varchar(255) COMMENT 'userId',
  `common_id` int(11) COMMENT 'commonId',
  `cat_id` int(11) COMMENT 'catId',
  `footprint_time` varchar(255) COMMENT 'footprintTime',
  `footprint_date` varchar(255) COMMENT 'footprintDate',
  PRIMARY KEY (`footprint_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='@ClassName';

CREATE TABLE IF NOT EXISTS `hou_goods_version` (
  `ver_id` int(11) NOT NULL AUTO_INCREMENT COMMENT 'verId',
  `common_id` int(11) COMMENT 'commonId',
  `common_name` varchar(255) COMMENT 'commonName',
  `ver_body` varchar(255) COMMENT 'verBody',
  `shop_id` int(11) COMMENT 'shopId',
  `operate_user_id` varchar(255) COMMENT 'operateUserId',
  `operate_user_name` varchar(255) COMMENT 'operateUserName',
  `operate_time` datetime COMMENT 'operateTime',
  `version_no` varchar(255) COMMENT 'versionNo',
  PRIMARY KEY (`ver_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品版本(GoodsVersion)表实体类';

CREATE TABLE IF NOT EXISTS `hou_storage_goods_base` (
  `storage_goods_id` int(11) NOT NULL AUTO_INCREMENT COMMENT 'storageGoodsId',
  `shop_id` int(11) COMMENT 'shopId',
  `goods_id` int(11) COMMENT 'goodsId',
  `common_id` int(11) COMMENT 'commonId',
  `storage_stock` int(11) COMMENT 'storageStock',
  `storage_id` int(11) COMMENT 'storageId',
  `create_time` datetime COMMENT 'createTime',
  `update_time` datetime COMMENT 'updateTime',
  PRIMARY KEY (`storage_goods_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='仓库中商品信息表(StorageGoodsBase)表实体类';

CREATE TABLE IF NOT EXISTS `hou_storage_info` (
  `storage_id` int(11) NOT NULL AUTO_INCREMENT COMMENT 'storageId',
  `storage_code` varchar(255) COMMENT 'storageCode',
  `storage_name` varchar(255) COMMENT 'storageName',
  `storage_province` int(11) COMMENT 'storageProvince',
  `storage_city` int(11) COMMENT 'storageCity',
  `storage_area` int(11) COMMENT 'storageArea',
  `storage_address` text COMMENT 'storageAddress',
  `storage_contacts` varchar(255) COMMENT 'storageContacts',
  `storage_mobile` varchar(255) COMMENT 'storageMobile',
  `storage_status` int(11) COMMENT 'storageStatus',
  `shop_id` int(11) COMMENT 'shopId',
  `create_time` datetime COMMENT 'createTime',
  `update_time` datetime COMMENT 'updateTime',
  PRIMARY KEY (`storage_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品仓库表(StorageInfo)表实体类';

CREATE TABLE IF NOT EXISTS `hou_storage_inventory_flow` (
  `inventory_flow_id` varchar(255) NOT NULL COMMENT 'inventoryFlowId',
  `shop_id` int(11) COMMENT 'shopId',
  `put_storage_id` int(11) COMMENT 'putStorageId',
  `out_storage_id` int(11) COMMENT 'outStorageId',
  `type` int(11) COMMENT 'type',
  `type_details` int(11) COMMENT 'typeDetails',
  `start_time` datetime COMMENT 'startTime',
  `end_time` datetime COMMENT 'endTime',
  `inventory_flow_num` int(11) COMMENT 'inventoryFlowNum',
  `operator_id` varchar(255) COMMENT 'operatorId',
  `operator_name` varchar(255) COMMENT 'operatorName',
  `operator_time` datetime COMMENT 'operatorTime',
  `create_time` datetime COMMENT 'createTime',
  `notes` varchar(255) COMMENT 'notes',
  PRIMARY KEY (`inventory_flow_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='仓库库存流水表(StorageInventoryFlow)表实体类';

CREATE TABLE IF NOT EXISTS `hou_storage_inventory_flow_goods` (
  `inventory_flow_goods_id` int(11) NOT NULL AUTO_INCREMENT COMMENT 'inventoryFlowGoodsId',
  `inventory_flow_id` varchar(255) COMMENT 'inventoryFlowId',
  `goods_id` int(11) COMMENT 'goodsId',
  `shop_id` int(11) COMMENT 'shopId',
  `goods_num` int(11) COMMENT 'goodsNum',
  `create_time` datetime COMMENT 'createTime',
  `buy_num` int(11) COMMENT 'buyNum',
  PRIMARY KEY (`inventory_flow_goods_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='仓库库存流水商品表(StorageInventoryFlowGoods)表实体类';

CREATE TABLE IF NOT EXISTS `hou_supplier_goods_yiyatong` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT 'id',
  `common_id` int(11) COMMENT 'commonId',
  `spu_id` varchar(255) COMMENT 'spuId',
  `goods_id` int(11) COMMENT 'goodsId',
  `sku_id` varchar(255) COMMENT 'skuId',
  `cat_id` varchar(255) COMMENT 'catId',
  `shop_id` int(11) COMMENT 'shopId',
  `is_delete` int(11) COMMENT 'isDelete',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='怡亚通商品代发记录';

CREATE TABLE IF NOT EXISTS `hou_supplier_yiyatong_cat` (
  `id` varchar(255) NOT NULL COMMENT 'id',
  `name` varchar(255) COMMENT 'name',
  `pid` varchar(255) COMMENT 'pid',
  `level` int(11) COMMENT 'level',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='贻亚通商品分类表';

CREATE TABLE IF NOT EXISTS `hou_yiyatong_msg_callback` (
  `id` int(11) NOT NULL COMMENT 'id',
  `message_id` text COMMENT 'messageId',
  `app_key` varchar(255) COMMENT 'appKey',
  `enum_num` varchar(255) COMMENT 'enumNum',
  `content` text COMMENT 'content',
  `create_time` varchar(255) COMMENT 'createTime',
  `sign` varchar(255) COMMENT 'sign',
  `is_use` int(11) COMMENT 'isUse',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='怡亚通消息回调记录表';


-- ---------------------------------------------------------------------
-- 兼容视图：当前运行的 goods jar 实体表名是裸名（`goods_brand`），主表为 `hou_` 前缀。
-- 建同名视图做桥接，使旧构建无需重发即可用；将来用带前缀实体的新构建时直接走主表。
-- 单表 SELECT * 视图在 MySQL 下可读可写，实测 INSERT/UPDATE/DELETE 正常。
-- ---------------------------------------------------------------------
CREATE OR REPLACE VIEW `goods_article_info` AS SELECT * FROM `hou_goods_article_info`;
CREATE OR REPLACE VIEW `goods_article_like` AS SELECT * FROM `hou_goods_article_like`;
CREATE OR REPLACE VIEW `goods_article_remark` AS SELECT * FROM `hou_goods_article_remark`;
CREATE OR REPLACE VIEW `goods_article_resource` AS SELECT * FROM `hou_goods_article_resource`;
CREATE OR REPLACE VIEW `goods_article_star` AS SELECT * FROM `hou_goods_article_star`;
CREATE OR REPLACE VIEW `goods_article_user_follow` AS SELECT * FROM `hou_goods_article_user_follow`;
CREATE OR REPLACE VIEW `goods_attribute` AS SELECT * FROM `hou_goods_attribute`;
CREATE OR REPLACE VIEW `goods_attribute_content` AS SELECT * FROM `hou_goods_attribute_content`;
CREATE OR REPLACE VIEW `goods_attribute_group` AS SELECT * FROM `hou_goods_attribute_group`;
CREATE OR REPLACE VIEW `goods_attribute_group_relation` AS SELECT * FROM `hou_goods_attribute_group_relation`;
CREATE OR REPLACE VIEW `goods_attribute_type` AS SELECT * FROM `hou_goods_attribute_type`;
CREATE OR REPLACE VIEW `goods_attribute_type_value` AS SELECT * FROM `hou_goods_attribute_type_value`;
CREATE OR REPLACE VIEW `goods_attribute_value` AS SELECT * FROM `hou_goods_attribute_value`;
CREATE OR REPLACE VIEW `goods_base` AS SELECT * FROM `hou_goods_base`;
CREATE OR REPLACE VIEW `goods_base_version` AS SELECT * FROM `hou_goods_base_version`;
CREATE OR REPLACE VIEW `goods_brand` AS SELECT * FROM `hou_goods_brand`;
CREATE OR REPLACE VIEW `goods_brand_cat` AS SELECT * FROM `hou_goods_brand_cat`;
CREATE OR REPLACE VIEW `goods_brand_type` AS SELECT * FROM `hou_goods_brand_type`;
CREATE OR REPLACE VIEW `goods_brand_type_relation` AS SELECT * FROM `hou_goods_brand_type_relation`;
CREATE OR REPLACE VIEW `goods_cat` AS SELECT * FROM `hou_goods_cat`;
CREATE OR REPLACE VIEW `goods_category_attributes` AS SELECT * FROM `hou_goods_category_attributes`;
CREATE OR REPLACE VIEW `goods_common` AS SELECT * FROM `hou_goods_common`;
CREATE OR REPLACE VIEW `goods_common_attribute_association` AS SELECT * FROM `hou_goods_common_attribute_association`;
CREATE OR REPLACE VIEW `goods_common_detail` AS SELECT * FROM `hou_goods_common_detail`;
CREATE OR REPLACE VIEW `goods_details_version` AS SELECT * FROM `hou_goods_details_version`;
CREATE OR REPLACE VIEW `goods_draft` AS SELECT * FROM `hou_goods_draft`;
CREATE OR REPLACE VIEW `goods_food_exhibition_hall` AS SELECT * FROM `hou_goods_food_exhibition_hall`;
CREATE OR REPLACE VIEW `goods_group_bind_attribute` AS SELECT * FROM `hou_goods_group_bind_attribute`;
CREATE OR REPLACE VIEW `goods_image_version` AS SELECT * FROM `hou_goods_image_version`;
CREATE OR REPLACE VIEW `goods_images` AS SELECT * FROM `hou_goods_images`;
CREATE OR REPLACE VIEW `goods_jingdong_dropshipping` AS SELECT * FROM `hou_goods_jingdong_dropshipping`;
CREATE OR REPLACE VIEW `goods_shop_cat_version` AS SELECT * FROM `hou_goods_shop_cat_version`;
CREATE OR REPLACE VIEW `goods_stock` AS SELECT * FROM `hou_goods_stock`;
CREATE OR REPLACE VIEW `goods_tag_class_info` AS SELECT * FROM `hou_goods_tag_class_info`;
CREATE OR REPLACE VIEW `goods_tag_info` AS SELECT * FROM `hou_goods_tag_info`;
CREATE OR REPLACE VIEW `goods_type` AS SELECT * FROM `hou_goods_type`;
CREATE OR REPLACE VIEW `goods_type_brand` AS SELECT * FROM `hou_goods_type_brand`;
CREATE OR REPLACE VIEW `goods_type_spec` AS SELECT * FROM `hou_goods_type_spec`;
CREATE OR REPLACE VIEW `goods_user_footprint` AS SELECT * FROM `hou_goods_user_footprint`;
CREATE OR REPLACE VIEW `goods_version` AS SELECT * FROM `hou_goods_version`;
CREATE OR REPLACE VIEW `storage_goods_base` AS SELECT * FROM `hou_storage_goods_base`;
CREATE OR REPLACE VIEW `storage_info` AS SELECT * FROM `hou_storage_info`;
CREATE OR REPLACE VIEW `storage_inventory_flow` AS SELECT * FROM `hou_storage_inventory_flow`;
CREATE OR REPLACE VIEW `storage_inventory_flow_goods` AS SELECT * FROM `hou_storage_inventory_flow_goods`;
CREATE OR REPLACE VIEW `supplier_goods_yiyatong` AS SELECT * FROM `hou_supplier_goods_yiyatong`;
CREATE OR REPLACE VIEW `supplier_yiyatong_cat` AS SELECT * FROM `hou_supplier_yiyatong_cat`;
CREATE OR REPLACE VIEW `yiyatong_msg_callback` AS SELECT * FROM `hou_yiyatong_msg_callback`;
