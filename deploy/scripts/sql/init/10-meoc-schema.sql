-- =====================================================================
-- HouCloud 统一初始化 ②/④：商城核心（Core）全量表结构（676 张基表）
-- 来源：生产库 meoc 结构只读导出（2026-09-16），仅结构、无数据。
-- 清洗：去除 AUTO_INCREMENT 值、DEFINER 子句、*_bak_ 临时备份表，
--       并剔除全部插件表（插件表由插件安装时创建，见 init/plugins/）。
-- 要求：MySQL 8.0+（utf8mb4_0900_ai_ci）
-- =====================================================================

-- MySQL dump 10.13  Distrib 8.0.36, for Linux (x86_64)
--
-- Host: 127.0.0.1    Database: meoc
-- ------------------------------------------------------
-- Server version	8.0.36

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `hou_addon_activity_combination_base`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_addon_activity_combination_base` (
  `combination_id` int NOT NULL AUTO_INCREMENT COMMENT '组合活动id',
  `shop_id` int NOT NULL COMMENT '店铺id',
  `combination_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '组合活动名称',
  `combination_type` tinyint(1) NOT NULL DEFAULT '1' COMMENT '组合活动类型 1固定组合 2搭配组合 ',
  `start_time` datetime DEFAULT NULL COMMENT '开始时间',
  `end_time` datetime DEFAULT NULL COMMENT '结束时间',
  `combination_status` int DEFAULT '0' COMMENT '组合活动状态  0-未开始;1-已开始;2-已结束；3-已失效',
  `combination_price` decimal(10,2) unsigned DEFAULT NULL COMMENT '组合活动价格',
  `combination_image` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '组合活动主图',
  `transport_template_id` int DEFAULT NULL COMMENT '运费模板id',
  `transport_area_id` int DEFAULT NULL COMMENT '售卖区域id',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `combination_original_price` decimal(10,2) unsigned DEFAULT NULL COMMENT '组合活动原价',
  PRIMARY KEY (`combination_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_addon_activity_combination_goods`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_addon_activity_combination_goods` (
  `combination_goods_id` int NOT NULL AUTO_INCREMENT COMMENT '组合商品id',
  `combination_id` int NOT NULL COMMENT '组合活动id',
  `combination_type` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '1' COMMENT '组合活动类型 1固定组合 2搭配组合 ',
  `is_main_product` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '0' COMMENT '是否为主商品 0否 1是',
  `goods_id` int NOT NULL COMMENT '商品id',
  `goods_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '商品名称',
  `goods_price` decimal(10,2) NOT NULL COMMENT '商品价格',
  `goods_num` int DEFAULT '0' COMMENT '商品数量',
  `matching_prices` decimal(10,2) DEFAULT NULL COMMENT '搭配价格',
  PRIMARY KEY (`combination_goods_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_addon_activity_discount_base`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_addon_activity_discount_base` (
  `discount_id` int unsigned NOT NULL AUTO_INCREMENT COMMENT '限时编号',
  `discount_name` varchar(50) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL COMMENT '活动名称',
  `discount_start_time` datetime NOT NULL COMMENT '活动开始时间',
  `discount_end_time` datetime NOT NULL COMMENT '活动结束时间',
  `user_id` varchar(18) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL COMMENT '用户编号',
  `shop_id` int unsigned NOT NULL COMMENT '店铺编号',
  `user_nick_name` varchar(50) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL DEFAULT '' COMMENT '用户名',
  `shop_name` varchar(50) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL COMMENT '店铺名称',
  `discount_lower_limit` int unsigned NOT NULL DEFAULT '1' COMMENT '购买下限，1为不限制',
  `discount_state` int unsigned NOT NULL DEFAULT '1' COMMENT '状态， 1-已开始；2-已结束；3-已失效；0-未开始；4-预热中',
  `discount_upper_limit` int NOT NULL COMMENT '每种商品限购 限购次数',
  `discount_create_time` datetime NOT NULL DEFAULT '2019-01-29 17:08:20' COMMENT '活动创建时间',
  `pre_start_hour` int DEFAULT '0' COMMENT '预热时间',
  `discount_upper_time` int DEFAULT NULL COMMENT '每种商品限购 限购天数',
  `pre_start_time` datetime DEFAULT NULL COMMENT '预热开始时间',
  PRIMARY KEY (`discount_id`) USING BTREE,
  KEY `discount_id` (`discount_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 ROW_FORMAT=DYNAMIC COMMENT='限时折扣活动表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_addon_activity_discount_goods`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_addon_activity_discount_goods` (
  `discount_goods_id` int unsigned NOT NULL AUTO_INCREMENT COMMENT '限时折扣商品表',
  `discount_id` int unsigned NOT NULL COMMENT '限时活动编号',
  `discount_name` varchar(50) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL COMMENT '活动名称',
  `goods_id` int unsigned NOT NULL COMMENT '商品编号',
  `common_id` int NOT NULL,
  `shop_id` int unsigned NOT NULL COMMENT '店铺编号',
  `goods_name` varchar(100) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL COMMENT '商品名称',
  `goods_price` decimal(10,2) NOT NULL COMMENT '商品原价',
  `discount_price` decimal(10,2) NOT NULL COMMENT '限时折扣价格',
  `goods_image` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL COMMENT '商品图片',
  `goods_start_time` datetime NOT NULL COMMENT '开始时间',
  `goods_end_time` datetime NOT NULL COMMENT '结束时间',
  `goods_lower_limit` int unsigned NOT NULL DEFAULT '0' COMMENT '购买下限，0为不限制',
  `discount_goods_state` tinyint unsigned NOT NULL DEFAULT '1' COMMENT '状态， 1-已开始；2-已结束；3-已失效；0-未开始；4-预热中',
  `goods_upper_limit` int NOT NULL DEFAULT '0' COMMENT '购买上限',
  `discount_upper_time` int DEFAULT '0' COMMENT '每种商品限购 限购天数',
  `discount_amount` decimal(10,2) DEFAULT '10.00' COMMENT '折扣',
  `pre_start_hour` int DEFAULT '0' COMMENT '预热时间',
  `pre_start_time` datetime DEFAULT NULL COMMENT '预热开始时间',
  PRIMARY KEY (`discount_goods_id`) USING BTREE,
  KEY `discount_id` (`discount_id`) USING BTREE,
  KEY `goods_id` (`goods_id`) USING BTREE,
  KEY `common_id` (`common_id`) USING BTREE,
  KEY `shop_id` (`shop_id`) USING BTREE,
  KEY `goods_upper_limit` (`goods_upper_limit`) USING BTREE,
  KEY `goods_lower_limit` (`goods_lower_limit`) USING BTREE,
  KEY `discount_goods_state` (`discount_goods_state`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 ROW_FORMAT=DYNAMIC COMMENT='限时折扣商品表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_addon_activity_live_account`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_addon_activity_live_account` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `user_id` varchar(64) NOT NULL COMMENT '用户ID（关联用户中心）',
  `nick_name` varchar(100) DEFAULT NULL COMMENT '昵称',
  `avatar` varchar(255) DEFAULT NULL COMMENT '头像',
  `role_type` tinyint(1) NOT NULL COMMENT '角色：1-主播，2-运营',
  `status` tinyint(1) NOT NULL DEFAULT '1' COMMENT '状态：0-禁用，1-启用',
  `remark` varchar(255) DEFAULT NULL COMMENT '备注',
  `create_by` varchar(64) DEFAULT NULL COMMENT '创建人',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT NULL COMMENT '更新人',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_role_type` (`role_type`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='直播账号（主播/运营账号）';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_addon_activity_live_goods_audit`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_addon_activity_live_goods_audit` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `room_id` bigint NOT NULL COMMENT '直播间ID',
  `goods_id` bigint NOT NULL COMMENT '商品ID',
  `goods_name` varchar(200) DEFAULT NULL COMMENT '商品名称',
  `audit_status` tinyint(1) NOT NULL DEFAULT '0' COMMENT '审核状态：0-待审核，1-通过，2-拒绝',
  `reject_reason` varchar(255) DEFAULT NULL COMMENT '拒绝原因',
  `create_by` varchar(64) DEFAULT NULL COMMENT '创建人',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT NULL COMMENT '更新人',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_room_id` (`room_id`),
  KEY `idx_goods_id` (`goods_id`),
  KEY `idx_audit_status` (`audit_status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='直播间商品审核';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_addon_activity_live_message`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_addon_activity_live_message` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `room_id` bigint NOT NULL COMMENT '直播间ID',
  `user_id` varchar(64) NOT NULL COMMENT '用户ID',
  `user_nick` varchar(100) DEFAULT NULL COMMENT '用户昵称',
  `msg_type` tinyint(1) NOT NULL COMMENT '消息类型：1-文本，2-点赞，3-礼物',
  `content` varchar(500) DEFAULT NULL COMMENT '文本内容或礼物信息',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_room_id` (`room_id`),
  KEY `idx_msg_type` (`msg_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='直播互动消息（弹幕/点赞/礼物）';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_addon_activity_live_room`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_addon_activity_live_room` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `room_title` varchar(200) NOT NULL COMMENT '房间标题',
  `cover_image` varchar(255) DEFAULT NULL COMMENT '封面图',
  `anchor_account_id` bigint NOT NULL COMMENT '主播账号ID',
  `status` tinyint(1) NOT NULL DEFAULT '0' COMMENT '状态：0-未开播，1-直播中，2-已结束，3-封禁',
  `scheduled_start_time` datetime DEFAULT NULL COMMENT '计划开播时间',
  `start_time` datetime DEFAULT NULL COMMENT '实际开播时间',
  `end_time` datetime DEFAULT NULL COMMENT '结束时间',
  `online_count` int NOT NULL DEFAULT '0' COMMENT '在线人数',
  `like_count` int NOT NULL DEFAULT '0' COMMENT '点赞数',
  `create_by` varchar(64) DEFAULT NULL COMMENT '创建人',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT NULL COMMENT '更新人',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_anchor_account` (`anchor_account_id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='直播间';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_addon_activity_none_goods`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_addon_activity_none_goods` (
  `id` int NOT NULL AUTO_INCREMENT,
  `none_type` int DEFAULT NULL COMMENT '商品类型',
  `common_id` int DEFAULT NULL COMMENT '商品ID',
  `display_order` int NOT NULL DEFAULT '0' COMMENT '排序，数字越大，越靠前',
  `update_date` timestamp NOT NULL DEFAULT '2018-09-05 16:00:00' COMMENT '更新时间',
  `hou_activity_verify` tinyint NOT NULL DEFAULT '1' COMMENT '审核 1通过，0未通过，10审核中',
  `hou_activity_verify_remark` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '审核失败原因',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 ROW_FORMAT=DYNAMIC COMMENT='活动专区商品';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_addon_activity_none_slide`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_addon_activity_none_slide` (
  `id` int NOT NULL AUTO_INCREMENT,
  `none_type` int NOT NULL DEFAULT '0' COMMENT '专区类型',
  `action_slide` varchar(1000) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL DEFAULT '' COMMENT '轮播地址',
  `action_type` tinyint NOT NULL DEFAULT '0' COMMENT '详情跳转类型',
  `action_detail` varchar(1000) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL DEFAULT '' COMMENT '跳转详情',
  `display_order` int NOT NULL DEFAULT '0' COMMENT '排序，数字越大，越靠前',
  `update_by` varchar(32) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL DEFAULT '' COMMENT '更新人',
  `update_date` timestamp NOT NULL DEFAULT '2018-09-05 16:00:00' COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 ROW_FORMAT=DYNAMIC COMMENT='活动专区轮播';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_addon_activity_none_type`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_addon_activity_none_type` (
  `display_order` int DEFAULT '0',
  `none_type` int NOT NULL AUTO_INCREMENT COMMENT '类型专区',
  `none_name` varchar(500) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL DEFAULT '' COMMENT '专区名称',
  PRIMARY KEY (`none_type`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 ROW_FORMAT=DYNAMIC COMMENT='专区活动类型';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_addon_activity_piecing_together`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_addon_activity_piecing_together` (
  `pintuan_id` int NOT NULL AUTO_INCREMENT COMMENT '主键，自增长',
  `shop_id` int NOT NULL COMMENT '店铺ID',
  `user_id` varchar(18) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL DEFAULT '0' COMMENT '商家ID',
  `name` varchar(50) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL DEFAULT '' COMMENT '拼团活动名称',
  `person_num` int NOT NULL DEFAULT '1' COMMENT '几人成团',
  `start_time` datetime NOT NULL DEFAULT '2019-04-22 14:10:07' COMMENT '开团时间',
  `end_time` datetime NOT NULL DEFAULT '2019-04-22 14:10:07' COMMENT '结束时间',
  `created_time` datetime NOT NULL DEFAULT '2019-04-22 14:10:07' COMMENT '创建时间',
  `status` tinyint(1) NOT NULL DEFAULT '1' COMMENT '是否可用，1正常 0结束 2 待开启',
  `common_id` int NOT NULL DEFAULT '0' COMMENT '商品ID',
  `start_hour` int NOT NULL DEFAULT '1' COMMENT '持续时间',
  `update_time` datetime NOT NULL DEFAULT '2019-04-22 14:10:07' COMMENT '更新时间',
  `display_order` int NOT NULL DEFAULT '0' COMMENT '排序',
  PRIMARY KEY (`pintuan_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 ROW_FORMAT=DYNAMIC COMMENT='拼团主表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_addon_activity_piecing_together_buyer`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_addon_activity_piecing_together_buyer` (
  `id` int NOT NULL AUTO_INCREMENT,
  `detail_id` int NOT NULL COMMENT '拼团详情',
  `user_id` varchar(18) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL COMMENT '购买者ID',
  `created_time` datetime NOT NULL COMMENT '购买时间',
  `mark_id` varchar(55) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL COMMENT '是否是同一个团的，对应hou_pingtuan_mark表',
  `ranking` int NOT NULL COMMENT '在团购，人中排第几。0为单独团',
  `user_name` varchar(100) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL DEFAULT '' COMMENT '用户昵称',
  `user_avatar` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL DEFAULT 'http://fkgou-images.oss-cn-qingdao.aliyuncs.com/file/upload/20190110/20190110094018.png?Expires=1862444433&OSSAccessKeyId=LTAIHv7U1m8yTCrg&Signature=UaFGoQWe9s9r18850JAePkBRwh4%3D' COMMENT '头像地址',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 ROW_FORMAT=DYNAMIC COMMENT='购买人';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_addon_activity_piecing_together_config`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_addon_activity_piecing_together_config` (
  `id` int NOT NULL AUTO_INCREMENT,
  `config_name` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '模块',
  `config_type` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT '2' COMMENT '1 代表模块1 （不可新增 删除）  2 代表 模块 2（可新增 删除）',
  `config_title_one` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '一级标题',
  `config_title_two` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '二级标题',
  `config_data` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '页面数据',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_addon_activity_piecing_together_detail`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_addon_activity_piecing_together_detail` (
  `detail_id` int NOT NULL AUTO_INCREMENT,
  `pintuan_id` int NOT NULL COMMENT '拼团共用ID，如开始时间等',
  `goods_id` int NOT NULL COMMENT '参加拼团的商品ID',
  `price_ori` float(10,2) NOT NULL COMMENT '原价格',
  `price` float(10,2) NOT NULL COMMENT '团购价格',
  `price_one` float(10,2) NOT NULL COMMENT '单独购买价格',
  `cid` int NOT NULL COMMENT '商品类别id',
  `common_id` int NOT NULL COMMENT '商品ID',
  PRIMARY KEY (`detail_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 ROW_FORMAT=DYNAMIC COMMENT='拼团详情';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_addon_activity_piecing_together_mark`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_addon_activity_piecing_together_mark` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '主键，自增长',
  `mark_id` varchar(55) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL DEFAULT '0' COMMENT '团长标识',
  `user_id` varchar(18) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL COMMENT '团长ID',
  `detail_id` int NOT NULL COMMENT '拼团详情',
  `created_time` datetime NOT NULL COMMENT '创建时间',
  `num` tinyint NOT NULL COMMENT '当前参团人数',
  `status` tinyint(1) NOT NULL DEFAULT '0' COMMENT '0,等待成团。1为已成团，2为成团失败，3为已完成退款',
  `user_name` varchar(100) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL DEFAULT '' COMMENT '用户名',
  `surplus_num` tinyint NOT NULL DEFAULT '0' COMMENT '还差几人成团',
  `user_avatar` varchar(522) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL DEFAULT 'http://fkgou-images.oss-cn-qingdao.aliyuncs.com/file/upload/20190110/20190110094018.png?Expires=1862444433&OSSAccessKeyId=LTAIHv7U1m8yTCrg&Signature=UaFGoQWe9s9r18850JAePkBRwh4%3D' COMMENT '头像地址',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 ROW_FORMAT=DYNAMIC COMMENT='拼团，团长标识-我的拼团';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_addon_activity_piecing_together_temp`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_addon_activity_piecing_together_temp` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '主键',
  `detail_id` int NOT NULL COMMENT '活动详情id',
  `mark_id` bigint NOT NULL DEFAULT '0' COMMENT '团长id,0表示当前用户为团长',
  `goods_id` bigint NOT NULL COMMENT '商品id',
  `order_id` varchar(100) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL COMMENT '订单id',
  `user_id` varchar(18) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL COMMENT '用户id',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `type` tinyint NOT NULL DEFAULT '0' COMMENT '0拼团，1单独购买',
  `pay_start` int NOT NULL DEFAULT '0' COMMENT '支付状态 0 未付款 1 已付款 2 以退款',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `order_id` (`order_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 ROW_FORMAT=DYNAMIC COMMENT='拼团信息表 参与者所属团 及拼团类型';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_addon_activity_points_mall_tool`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_addon_activity_points_mall_tool` (
  `tool_id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `tool_name` varchar(100) NOT NULL COMMENT '工具名称',
  `tool_code` varchar(100) NOT NULL COMMENT '工具编码',
  `position_code` varchar(100) DEFAULT NULL COMMENT '展示位置编码',
  `config_json` text COMMENT '配置内容(JSON)',
  `status` tinyint(1) NOT NULL DEFAULT '0' COMMENT '状态：0-禁用，1-启用',
  `start_time` datetime DEFAULT NULL COMMENT '生效开始时间',
  `end_time` datetime DEFAULT NULL COMMENT '生效结束时间',
  `create_by` varchar(64) DEFAULT NULL COMMENT '创建人',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT NULL COMMENT '更新人',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(255) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`tool_id`),
  KEY `idx_tool_code` (`tool_code`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='积分商城工具配置';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_addon_activity_redpacket_base`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_addon_activity_redpacket_base` (
  `redpacket_id` int NOT NULL AUTO_INCREMENT COMMENT '红包编号',
  `redpacket_t_id` int NOT NULL COMMENT '红包模版编号',
  `redpacket_title` varchar(50) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL COMMENT '红包标题',
  `redpacket_start_date` datetime NOT NULL COMMENT '红包有效期开始时间',
  `redpacket_end_date` datetime NOT NULL COMMENT '红包有效期结束时间',
  `redpacket_price` int NOT NULL COMMENT '红包面额',
  `redpacket_t_orderlimit` decimal(10,2) NOT NULL COMMENT '红包使用时的订单限额',
  `redpacket_state` tinyint NOT NULL COMMENT '红包状态(1-未用,2-已用,3-过期)',
  `redpacket_active_date` datetime NOT NULL COMMENT '红包发放日期',
  `redpacket_owner_id` varchar(50) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL COMMENT '红包所有者id',
  `redpacket_owner_name` varchar(225) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '红包所有者名称',
  `redpacket_order_id` varchar(500) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL DEFAULT '' COMMENT '使用该红包的订单编号',
  `redpacket_from` tinyint(1) NOT NULL DEFAULT '0' COMMENT '红包来源：1-用户自己领取，2-其它用户转增',
  `redpacket_type` int DEFAULT '2' COMMENT '红包类型，1-新人注册红包，2-普通红包，3-多品红包 4-店铺红包 5-单品新人红包 6-专属红包 默认2  7 plus红包',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`redpacket_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 ROW_FORMAT=DYNAMIC COMMENT='红包表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_addon_activity_redpacket_template`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_addon_activity_redpacket_template` (
  `redpacket_t_id` int NOT NULL AUTO_INCREMENT COMMENT '红包模版编号',
  `redpacket_t_type` tinyint(1) NOT NULL DEFAULT '2' COMMENT '红包类型，1-新人注册红包，2-普通红包，3-多品红包 4-店铺红包 5-单品多品红包 6-专属红包默认 7-plus红包',
  `redpacket_t_title` varchar(50) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL COMMENT '红包模版名称',
  `redpacket_t_desc` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL COMMENT '红包模版描述',
  `redpacket_t_start_date` datetime NOT NULL COMMENT '红包模版有效期开始时间',
  `redpacket_t_end_date` datetime DEFAULT NULL COMMENT '红包模版有效期结束时间',
  `redpacket_t_price` int NOT NULL COMMENT '红包模版面额',
  `redpacket_t_orderlimit` decimal(10,2) NOT NULL COMMENT '红包使用时的消费限额',
  `redpacket_t_state` tinyint NOT NULL DEFAULT '1' COMMENT '红包模版状态(1-有效,2-失效,3-删除)',
  `redpacket_t_total` int NOT NULL COMMENT '模版可发放的红包总数  0 不限制',
  `redpacket_t_giveout` int NOT NULL COMMENT '模版已发放的红包数量',
  `redpacket_t_add_date` datetime NOT NULL COMMENT '模版的创建时间',
  `redpacket_t_update_date` datetime NOT NULL COMMENT '模版的最后修改时间',
  `redpacket_t_eachlimit` int NOT NULL DEFAULT '1' COMMENT '每人限领张数 0 不限制',
  `order_operator` varchar(32) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '操作人',
  `order_operator_time` datetime DEFAULT NULL COMMENT '操作时间',
  `redpacket_remark` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '审批',
  `redpacket_is_booking` tinyint DEFAULT '1' COMMENT '红包是否倒计时1-否2-是',
  `rob_redpacket_start_date` datetime DEFAULT NULL COMMENT '抢红包开始时间',
  `rob_redpacket_end_date` datetime DEFAULT NULL COMMENT '抢红包结束时间',
  `red_termofvalidity` int DEFAULT NULL COMMENT '自红包发放起，有效天数',
  PRIMARY KEY (`redpacket_t_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 ROW_FORMAT=DYNAMIC COMMENT='红包模版表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_addon_activity_reduction_policy_base`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_addon_activity_reduction_policy_base` (
  `promotion_id` int NOT NULL AUTO_INCREMENT COMMENT '活动ID',
  `promotion_name` varchar(200) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL COMMENT '活动名称',
  `start_time` datetime NOT NULL COMMENT '活动开始时间',
  `end_time` datetime NOT NULL COMMENT '活动结束时间',
  `promotion_status` int DEFAULT NULL COMMENT '活动状态:1-正常;0-已结束;2-管理员已关闭  3 未开始',
  `shop_id` int NOT NULL COMMENT '店铺ID',
  `created_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`promotion_id`) USING BTREE,
  UNIQUE KEY `hou_activity_reduction_policy_base_promotion_id_uindex` (`promotion_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 ROW_FORMAT=DYNAMIC COMMENT='满减赠折基本信息表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_addon_activity_reduction_policy_goods`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_addon_activity_reduction_policy_goods` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `promotion_id` int DEFAULT NULL COMMENT '活动ID',
  `common_id` int DEFAULT NULL COMMENT '商品公共表ID',
  `goods_id` int DEFAULT NULL COMMENT '商品ID',
  `shop_id` int DEFAULT NULL COMMENT '店铺ID',
  `created_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 ROW_FORMAT=DYNAMIC COMMENT='满减赠折商品表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_addon_activity_reduction_policy_rule`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_addon_activity_reduction_policy_rule` (
  `rule_id` int NOT NULL AUTO_INCREMENT COMMENT '规则ID',
  `promotion_id` int NOT NULL COMMENT '活动ID',
  `promotion_content` varchar(200) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL COMMENT '活动内容',
  `has_giveaway` int DEFAULT '0' COMMENT '是否有赠品:1-有;0-无',
  `giveaway_id` int DEFAULT NULL COMMENT '赠品ID',
  `meet_amount` decimal(10,0) DEFAULT NULL COMMENT '满足金额',
  `discount_content` varchar(50) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '优惠内容',
  `discount_type` int DEFAULT NULL COMMENT '折扣类型:1-满减;2-满赠;3-满折',
  `created_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`rule_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 ROW_FORMAT=DYNAMIC COMMENT='满减赠折规则表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_addon_activity_seckill_base`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_addon_activity_seckill_base` (
  `seckill_id` int unsigned NOT NULL AUTO_INCREMENT COMMENT '限时编号',
  `seckill_name` varchar(50) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL COMMENT '活动名称',
  `seckill_start_time` datetime NOT NULL COMMENT '活动开始时间',
  `seckill_end_time` datetime NOT NULL COMMENT '活动结束时间',
  `user_id` varchar(18) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL COMMENT '用户编号',
  `shop_id` int unsigned NOT NULL COMMENT '店铺编号',
  `user_nick_name` varchar(50) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL DEFAULT '' COMMENT '用户名',
  `shop_name` varchar(50) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL COMMENT '店铺名称',
  `seckill_state` int unsigned NOT NULL DEFAULT '1' COMMENT '状态:0-未开始;1-已开始;2-已结束；3-已失效',
  `seckill_hour` int NOT NULL COMMENT '场次:\n1-08：00;\n2-12：00;\n3-16：00;\n4-20：00;\n5-00：00;',
  `seckill_create_time` datetime NOT NULL DEFAULT '2019-01-29 17:08:20' COMMENT '活动创建时间',
  `seckill_start_date` date DEFAULT NULL COMMENT '活动开始日期',
  `cancel_time` int DEFAULT NULL COMMENT '订单取消时间(分钟)',
  `seckill_doods_recommend` int DEFAULT NULL COMMENT '推荐标志 0-未推荐 1-已推荐',
  PRIMARY KEY (`seckill_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 ROW_FORMAT=DYNAMIC COMMENT='限时秒杀活动表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_addon_activity_seckill_goods`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_addon_activity_seckill_goods` (
  `seckill_goods_id` int unsigned NOT NULL AUTO_INCREMENT COMMENT '限时秒杀商品表',
  `seckill_id` int unsigned NOT NULL COMMENT '限时活动编号',
  `seckill_name` varchar(50) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL COMMENT '活动名称',
  `goods_id` int unsigned NOT NULL COMMENT '商品编号',
  `common_id` int NOT NULL,
  `shop_id` int unsigned NOT NULL COMMENT '店铺编号',
  `goods_name` varchar(100) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL COMMENT '商品名称',
  `goods_price` decimal(10,2) NOT NULL COMMENT '商品原价',
  `seckill_price` decimal(10,2) NOT NULL COMMENT '限时秒杀价格',
  `goods_image` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL COMMENT '商品图片',
  `goods_start_time` datetime NOT NULL COMMENT '开始时间',
  `goods_end_time` datetime NOT NULL COMMENT '结束时间',
  `seckill_goods_state` tinyint unsigned NOT NULL DEFAULT '1' COMMENT '状态:0-未开始;1-已开始;2-已结束；3-已失效',
  `goods_upper_limit` int NOT NULL DEFAULT '0' COMMENT '限购数量',
  `seckill_num` int NOT NULL COMMENT '秒杀库存数量',
  PRIMARY KEY (`seckill_goods_id`) USING BTREE,
  KEY `seckill_id` (`seckill_id`) USING BTREE,
  KEY `goods_id` (`goods_id`) USING BTREE,
  KEY `common_id` (`common_id`) USING BTREE,
  KEY `shop_id` (`shop_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 ROW_FORMAT=DYNAMIC COMMENT='限时秒杀商品表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_addon_activity_spinwin`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_addon_activity_spinwin` (
  `hou_activity_id` int NOT NULL AUTO_INCREMENT,
  `hou_activity_title` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '活动标题',
  `hou_activity_start_time` timestamp NOT NULL DEFAULT '2019-09-03 07:35:00' COMMENT '活动开始时间',
  `hou_activity_end_time` timestamp NOT NULL DEFAULT '2019-09-03 07:35:00' COMMENT '活动结束时间',
  `partake_num_is_display` int NOT NULL DEFAULT '1' COMMENT '参与人数是否显示：1-显示，2-隐藏',
  `partake_num` int NOT NULL DEFAULT '0' COMMENT '参与人数',
  `partake_num_is_limit` int NOT NULL DEFAULT '1' COMMENT '参与人数限制是否限制：1-不限，2-限制',
  `partake_num_limit` int NOT NULL DEFAULT '0' COMMENT '参与人数限制',
  `graphic_explanation` varchar(1000) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL DEFAULT '' COMMENT '图文说明',
  `first_screen_advertise` varchar(1000) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL DEFAULT '' COMMENT '首屏广告',
  `first_screen_is_display` int NOT NULL DEFAULT '1' COMMENT '首屏广告是否展示1-显示2-不显示',
  `is_round_award_information` int NOT NULL DEFAULT '1' COMMENT '是否轮播获奖信息：1-开启，2关闭',
  `hou_activity_is_enable` int NOT NULL DEFAULT '1' COMMENT '活动是否可用1-可用2不可用3-删除',
  `update_by` varchar(32) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL DEFAULT '' COMMENT '更新人',
  `update_date` timestamp NOT NULL DEFAULT '2019-11-20 05:13:00' COMMENT '更新时间',
  `total_sweepstakes_is_limit` int NOT NULL DEFAULT '1' COMMENT '总抽奖机会:是否限制：1-不限，2-限制',
  `total_sweepstakes_limit` int NOT NULL DEFAULT '0' COMMENT '总抽奖机会',
  `winning_times_per_person` int NOT NULL DEFAULT '0' COMMENT '每人每日抽奖次数',
  `winning_rate` decimal(10,2) DEFAULT NULL,
  `expected_number_of_participants` int NOT NULL DEFAULT '0' COMMENT '预计参与人数',
  PRIMARY KEY (`hou_activity_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 ROW_FORMAT=DYNAMIC COMMENT='幸运大转盘活动';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_addon_activity_spinwin_gift`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_addon_activity_spinwin_gift` (
  `gift_id` int unsigned NOT NULL AUTO_INCREMENT,
  `gift_name` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL DEFAULT '' COMMENT '礼品名称',
  `redpacket_t_id` int NOT NULL DEFAULT '0' COMMENT '红包模版编号',
  `gift_is_redpacket` int NOT NULL DEFAULT '1' COMMENT '礼品是否是红包1-是,2-否',
  `gift_is_enable` int NOT NULL DEFAULT '1' COMMENT '礼品是否可用1-可用,2-不可用',
  `update_by` varchar(32) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL DEFAULT '' COMMENT '更新人',
  `update_date` timestamp NOT NULL DEFAULT '2018-09-05 16:00:00' COMMENT '更新时间',
  `gift_image` varchar(1000) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL DEFAULT '' COMMENT '礼品图片',
  PRIMARY KEY (`gift_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 ROW_FORMAT=DYNAMIC COMMENT='大转盘活动礼品';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_addon_activity_spinwin_lucky_draw_times`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_addon_activity_spinwin_lucky_draw_times` (
  `time_id` int unsigned NOT NULL AUTO_INCREMENT,
  `hou_activity_id` int NOT NULL DEFAULT '0' COMMENT '活动id',
  `user_id` varchar(18) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL COMMENT '用户id',
  `lucky_draw_times` int NOT NULL DEFAULT '0' COMMENT '总抽奖次数',
  `surplus_draw_times` int NOT NULL DEFAULT '0' COMMENT '剩余抽奖次数',
  `update_date` timestamp NOT NULL DEFAULT '2018-09-05 16:00:00' COMMENT '更新时间',
  `winning_times` int NOT NULL DEFAULT '0' COMMENT '中奖次数',
  `number_of_daily_lottery` int NOT NULL DEFAULT '0' COMMENT '每日抽奖次数',
  `user_name` varchar(100) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL DEFAULT '' COMMENT '用户名',
  `num_sweepstakes_today` int NOT NULL DEFAULT '0' COMMENT '今日抽奖次数',
  PRIMARY KEY (`time_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 ROW_FORMAT=DYNAMIC COMMENT='大转盘用户抽奖次数';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_addon_activity_spinwin_person`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_addon_activity_spinwin_person` (
  `id` int unsigned NOT NULL AUTO_INCREMENT,
  `prize_id` int NOT NULL DEFAULT '0' COMMENT '奖项id',
  `hou_activity_id` int NOT NULL DEFAULT '0' COMMENT '活动id',
  `user_id` varchar(18) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL COMMENT '用户id',
  `user_name` varchar(50) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL DEFAULT '' COMMENT '用户名',
  `user_avatar` varchar(500) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL DEFAULT '' COMMENT '用户头像',
  `update_date` datetime NOT NULL COMMENT '更新时间',
  `redpacket_id` int DEFAULT '0' COMMENT '红包id',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 ROW_FORMAT=DYNAMIC COMMENT='幸运大转盘抽奖记录';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_addon_activity_spinwin_prize`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_addon_activity_spinwin_prize` (
  `prize_id` int unsigned NOT NULL AUTO_INCREMENT,
  `prize_grade` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL DEFAULT '' COMMENT '奖项等级',
  `prize_name` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL DEFAULT '' COMMENT '奖项名称',
  `gift_id` int NOT NULL DEFAULT '0' COMMENT '礼品id',
  `hou_activity_id` int NOT NULL DEFAULT '0' COMMENT '活动id',
  `gift_num` int NOT NULL DEFAULT '0' COMMENT '礼品数量',
  `update_by` varchar(32) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL DEFAULT '' COMMENT '更新人',
  `update_date` timestamp NOT NULL DEFAULT '2018-09-05 16:00:00' COMMENT '更新时间',
  `give_out_gift_num` int NOT NULL DEFAULT '0' COMMENT '给出的礼品数量',
  `is_win` varchar(10) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL DEFAULT '1' COMMENT '是否中奖 1中奖2不中奖',
  `display_order` int NOT NULL DEFAULT '0' COMMENT '奖项排序',
  PRIMARY KEY (`prize_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 ROW_FORMAT=DYNAMIC COMMENT='大转盘活动奖项';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_addon_activity_thousands_guests_price`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_addon_activity_thousands_guests_price` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `shop_id` bigint NOT NULL COMMENT '店铺ID',
  `goods_id` bigint NOT NULL COMMENT '商品ID',
  `sku_id` bigint DEFAULT NULL COMMENT 'SKU ID',
  `segment_type` tinyint(1) NOT NULL COMMENT '人群类型：0-全部，1-指定用户，2-会员等级，3-标签',
  `segment_value` varchar(100) DEFAULT NULL COMMENT '人群值（userId/等级编码/标签编码等）',
  `price_type` tinyint(1) NOT NULL COMMENT '价格类型：1-指定价格，2-折扣百分比，3-减金额',
  `price_value` decimal(10,2) NOT NULL COMMENT '价格值/折扣值/减金额',
  `priority` int NOT NULL DEFAULT '0' COMMENT '优先级（数字越大优先级越高）',
  `status` tinyint(1) NOT NULL DEFAULT '1' COMMENT '状态：0-禁用，1-启用',
  `start_time` datetime DEFAULT NULL COMMENT '生效开始时间',
  `end_time` datetime DEFAULT NULL COMMENT '生效结束时间',
  `create_by` varchar(64) DEFAULT NULL COMMENT '创建人',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT NULL COMMENT '更新人',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(255) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`),
  KEY `idx_shop_goods` (`shop_id`,`goods_id`),
  KEY `idx_sku_id` (`sku_id`),
  KEY `idx_status` (`status`),
  KEY `idx_segment` (`segment_type`,`segment_value`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='千客千价价格策略';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_addon_activity_voucher_base`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_addon_activity_voucher_base` (
  `voucher_id` int NOT NULL AUTO_INCREMENT COMMENT '代金券编号',
  `voucher_t_id` int NOT NULL DEFAULT '0' COMMENT '代金券模版编号',
  `voucher_title` varchar(50) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL DEFAULT '' COMMENT '代金券标题',
  `voucher_start_date` datetime NOT NULL COMMENT '代金券有效期开始时间',
  `voucher_end_date` datetime NOT NULL COMMENT '代金券有效期结束时间',
  `voucher_price` decimal(10,2) NOT NULL COMMENT '代金券面额',
  `voucher_limit` decimal(10,2) NOT NULL COMMENT '代金券使用时的订单限额',
  `voucher_shop_id` int NOT NULL DEFAULT '0' COMMENT '代金券的店铺id',
  `voucher_state` tinyint NOT NULL DEFAULT '1' COMMENT '代金券状态(1-未用,2-已用,3-过期,4-收回)',
  `voucher_active_date` datetime NOT NULL COMMENT '代金券发放日期',
  `voucher_owner_id` varchar(50) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL DEFAULT '0' COMMENT '代金券所有者id',
  `voucher_owner_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '代金券所有者名称',
  `voucher_order_id` varchar(25) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT '0' COMMENT '使用该代金券的订单编号',
  PRIMARY KEY (`voucher_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 ROW_FORMAT=DYNAMIC COMMENT='代金券表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_addon_activity_voucher_common`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_addon_activity_voucher_common` (
  `id` int NOT NULL AUTO_INCREMENT,
  `voucher_t_id` int NOT NULL DEFAULT '0' COMMENT '代金券模版编号',
  `shop_id` int NOT NULL COMMENT '店铺id',
  `update_date` timestamp NOT NULL DEFAULT '2018-09-05 16:00:00' COMMENT '更新时间',
  `common_id` int NOT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 ROW_FORMAT=DYNAMIC COMMENT='商品参与的跨店满减活动';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_addon_activity_voucher_goods`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_addon_activity_voucher_goods` (
  `id` int NOT NULL AUTO_INCREMENT,
  `voucher_t_id` int NOT NULL DEFAULT '0' COMMENT '代金券模版编号',
  `shop_id` int NOT NULL COMMENT '店铺id',
  `update_date` timestamp NOT NULL DEFAULT '2018-09-05 16:00:00' COMMENT '更新时间',
  `common_id` int NOT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 ROW_FORMAT=DYNAMIC COMMENT='优惠券使用的商品';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_addon_activity_voucher_shop`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_addon_activity_voucher_shop` (
  `id` int NOT NULL AUTO_INCREMENT,
  `shop_id` int NOT NULL COMMENT '店铺id',
  `display_order` int NOT NULL DEFAULT '0' COMMENT '显示顺序',
  `update_by` varchar(32) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL DEFAULT '' COMMENT '更新人',
  `update_date` timestamp NOT NULL DEFAULT '2018-09-05 16:00:00' COMMENT '更新时间',
  `hou_activity_verify` tinyint NOT NULL DEFAULT '1' COMMENT '审核 1通过，0未通过，10审核中',
  `hou_activity_verify_remark` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL DEFAULT '' COMMENT '审核失败原因',
  `voucher_t_id` int NOT NULL DEFAULT '0' COMMENT '代金券模版编号',
  `wait_verify_goods_num` int NOT NULL DEFAULT '0' COMMENT '待审核数',
  `pass_verify_goods_num` int NOT NULL DEFAULT '0' COMMENT '通过审核的商品数',
  `refuse_verify_goods_num` int NOT NULL DEFAULT '0' COMMENT '拒绝审核审核商品数',
  `all_goods_num` int NOT NULL DEFAULT '0' COMMENT '提报商品总数',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 ROW_FORMAT=DYNAMIC COMMENT='平台优惠券的参加店铺';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_addon_activity_voucher_template`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_addon_activity_voucher_template` (
  `voucher_t_id` int NOT NULL AUTO_INCREMENT COMMENT '代金券模版编号',
  `voucher_t_title` varchar(50) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL DEFAULT '' COMMENT '代金券模版名称',
  `voucher_t_desc` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL DEFAULT '' COMMENT '代金券模版描述',
  `shop_class_id` int NOT NULL DEFAULT '0',
  `voucher_t_start_date` datetime NOT NULL DEFAULT '2019-06-25 00:00:00' COMMENT '代金券模版有效期开始时间',
  `voucher_t_end_date` datetime NOT NULL DEFAULT '2019-06-25 00:00:00' COMMENT '代金券模版有效期结束时间',
  `voucher_t_price` decimal(10,2) NOT NULL COMMENT '代金券模版面额',
  `voucher_t_limit` decimal(10,2) NOT NULL COMMENT '代金券使用时的订单限额',
  `shop_id` int NOT NULL DEFAULT '0' COMMENT '代金券模版的店铺id',
  `shop_name` varchar(50) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL DEFAULT '' COMMENT '店铺名称',
  `voucher_t_creator_id` varchar(50) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL DEFAULT '0' COMMENT '代金券模版的创建者id',
  `voucher_t_state` tinyint NOT NULL DEFAULT '1' COMMENT '代金券模版状态(1-有效,2-失效)',
  `voucher_t_total` int NOT NULL DEFAULT '0' COMMENT '模版可发放的代金券总数',
  `voucher_t_giveout` int NOT NULL DEFAULT '0' COMMENT '模版已发放的代金券数量',
  `voucher_t_used` int NOT NULL DEFAULT '0' COMMENT '模版已经使用过的代金券',
  `voucher_t_add_date` datetime NOT NULL DEFAULT '2019-06-25 00:00:00' COMMENT '模版的创建时间',
  `voucher_t_update_date` datetime NOT NULL DEFAULT '2019-06-25 00:00:00' COMMENT '模版的最后修改时间',
  `combo_id` int NOT NULL DEFAULT '0' COMMENT '套餐编号',
  `voucher_t_points` int NOT NULL DEFAULT '0' COMMENT '兑换所需积分',
  `voucher_t_eachlimit` int NOT NULL DEFAULT '1' COMMENT '每人限领张数',
  `voucher_t_styleimg` varchar(200) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT '' COMMENT '样式模版图片',
  `voucher_t_customimg` varchar(500) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '自定义代金券模板图片',
  `voucher_t_remark` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL DEFAULT '' COMMENT '参加平台活动审核失败原因',
  `voucher_b_start_date` datetime NOT NULL DEFAULT '2019-06-25 00:00:00' COMMENT '优惠券报名开始时间',
  `voucher_b_end_date` datetime NOT NULL DEFAULT '2019-06-25 00:00:00' COMMENT '优惠券报名结束时间',
  `is_shop_voucher` tinyint(1) DEFAULT NULL COMMENT '1 是店铺优惠券 2是商品优惠券 3 商品兑换券',
  PRIMARY KEY (`voucher_t_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 ROW_FORMAT=DYNAMIC COMMENT='代金券模版表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_addon_cate`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_addon_cate` (
  `id` int NOT NULL AUTO_INCREMENT,
  `name` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `create_time` int DEFAULT NULL,
  `update_time` int DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='插件中心分类表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_addon_plugin`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_addon_plugin` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `service_code` varchar(64) COLLATE utf8mb4_general_ci NOT NULL COMMENT '所属服务编码',
  `plugin_code` varchar(64) COLLATE utf8mb4_general_ci NOT NULL COMMENT '插件编码',
  `name` varchar(128) COLLATE utf8mb4_general_ci NOT NULL COMMENT '插件名称',
  `category` varchar(64) COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '服务内分类',
  `version` varchar(32) COLLATE utf8mb4_general_ci NOT NULL DEFAULT '1.0.0',
  `description` varchar(500) COLLATE utf8mb4_general_ci NOT NULL DEFAULT '',
  `terminals_json` varchar(500) COLLATE utf8mb4_general_ci NOT NULL DEFAULT '[]',
  `dependencies_json` varchar(500) COLLATE utf8mb4_general_ci NOT NULL DEFAULT '[]',
  `entry_path` varchar(255) COLLATE utf8mb4_general_ci NOT NULL DEFAULT '',
  `config_schema` text COLLATE utf8mb4_general_ci,
  `status` tinyint NOT NULL DEFAULT '1',
  `sort` int NOT NULL DEFAULT '0',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` tinyint NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_service_plugin` (`service_code`,`plugin_code`),
  KEY `idx_service_status` (`service_code`,`status`,`deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='统一插件目录';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_addon_plugin_install`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_addon_plugin_install` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `plugin_code` varchar(64) COLLATE utf8mb4_general_ci NOT NULL COMMENT '插件编码',
  `installed` tinyint NOT NULL DEFAULT '0' COMMENT '是否已安装：0否 1是',
  `enabled` tinyint NOT NULL DEFAULT '0' COMMENT '是否启用：0否 1是',
  `config_json` text COLLATE utf8mb4_general_ci COMMENT '插件配置JSON',
  `version` varchar(32) COLLATE utf8mb4_general_ci NOT NULL DEFAULT '1.0.0' COMMENT '插件版本',
  `jar_path` varchar(512) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT 'installed jar path (market online install)',
  `checksum` varchar(64) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT 'installed jar md5',
  `install_time` datetime DEFAULT NULL COMMENT '安装时间',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `deleted` tinyint NOT NULL DEFAULT '0' COMMENT '是否删除：0否 1是',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_plugin_code` (`plugin_code`),
  KEY `idx_enabled` (`enabled`),
  KEY `idx_deleted` (`deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='统一插件安装状态表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_addon_plugin_migration`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_addon_plugin_migration` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `plugin_code` varchar(64) NOT NULL COMMENT '插件唯一编码',
  `version` varchar(32) NOT NULL COMMENT '插件版本',
  `script` varchar(512) DEFAULT NULL COMMENT '执行的脚本路径，逗号分隔',
  `checksum` varchar(64) DEFAULT NULL COMMENT '脚本内容 SHA-256 摘要',
  `status` tinyint NOT NULL DEFAULT '0' COMMENT '1=成功,0=失败',
  `message` varchar(1024) DEFAULT NULL COMMENT '失败原因/备注',
  `applied_time` datetime DEFAULT NULL COMMENT '最近一次执行时间',
  `create_time` datetime DEFAULT NULL,
  `update_time` datetime DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_plugin_version` (`plugin_code`,`version`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='插件 schema 迁移记录（幂等）';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_addon_tags`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_addon_tags` (
  `id` int NOT NULL,
  `name` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_ai_chat_history`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_ai_chat_history` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` int unsigned NOT NULL COMMENT '用户ID',
  `title` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '对话标题',
  `model_id` int unsigned NOT NULL COMMENT '模型ID',
  `content` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci COMMENT '对话内容(JSON格式)',
  `message_count` int NOT NULL DEFAULT '0' COMMENT '消息数量',
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '状态(0-删除,1-正常)',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_model_id` (`model_id`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='AI对话历史表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_ai_config`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_ai_config` (
  `id` int unsigned NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `config_key` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '配置键名',
  `config_value` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '配置值',
  `config_group` varchar(64) NOT NULL DEFAULT 'default' COMMENT '配置分组(global/security/gateway，对齐生产实库 2026-10-31)',
  `config_desc` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '配置描述',
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '状态(0-禁用,1-启用)',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_config_key` (`config_key`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='AI配置表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_ai_generation_record`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_ai_generation_record` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` int unsigned NOT NULL COMMENT '用户ID',
  `type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '生成类型(text-文本生成,image-图像生成,code-代码生成)',
  `model_id` int unsigned NOT NULL COMMENT '模型ID',
  `prompt` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci COMMENT '提示词',
  `input_params` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci COMMENT '输入参数(JSON格式)',
  `result` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci COMMENT '生成结果',
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '状态(0-失败,1-成功)',
  `cost_time` int NOT NULL DEFAULT '0' COMMENT '耗时(毫秒)',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_type` (`type`),
  KEY `idx_model_id` (`model_id`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='AI生成记录表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_ai_model`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_ai_model` (
  `id` int unsigned NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '模型名称',
  `type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '模型类型(text-文本生成,image-图像生成,code-代码生成,chat-对话模型)',
  `api_url` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'API地址',
  `api_key` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'API密钥(加密存储)',
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci COMMENT '模型描述',
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '状态(0-禁用,1-启用)',
  `temperature` decimal(3,2) NOT NULL DEFAULT '0.70' COMMENT '温度参数(0-2)',
  `max_tokens` int NOT NULL DEFAULT '2048' COMMENT '最大token数',
  `usage_count` int NOT NULL DEFAULT '0' COMMENT '使用次数',
  `sort` int NOT NULL DEFAULT '0' COMMENT '排序',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_type` (`type`),
  KEY `idx_status` (`status`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='AI模型表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_ai_prompt`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_ai_prompt` (
  `id` int unsigned NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '提示词名称',
  `category_id` int unsigned NOT NULL COMMENT '分类ID',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '提示词内容',
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci COMMENT '提示词描述',
  `tags` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '标签(逗号分隔)',
  `usage_count` int NOT NULL DEFAULT '0' COMMENT '使用次数',
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '状态(0-禁用,1-启用)',
  `sort` int NOT NULL DEFAULT '0' COMMENT '排序',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_category_id` (`category_id`),
  KEY `idx_status` (`status`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='AI提示词表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_ai_prompt_category`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_ai_prompt_category` (
  `id` int unsigned NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '分类名称',
  `parent_id` int unsigned NOT NULL DEFAULT '0' COMMENT '父级ID(0表示顶级分类)',
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci COMMENT '分类描述',
  `sort` int NOT NULL DEFAULT '0' COMMENT '排序',
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '状态(0-禁用,1-启用)',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_parent_id` (`parent_id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='AI提示词分类表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_ai_token_usage`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_ai_token_usage` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '记录 ID',
  `tenant_id` bigint unsigned NOT NULL DEFAULT '0' COMMENT '租户 ID',
  `project_id` bigint unsigned DEFAULT NULL COMMENT '项目 ID',
  `module_name` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'geo' COMMENT '模块名 geo/mall/oa 等',
  `biz_type` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '业务类型 ai_studio_keyword_expand 等',
  `biz_id` bigint unsigned DEFAULT NULL COMMENT '关联业务 ID（如 expand_batch.id）',
  `provider` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'dify' COMMENT '提供方 dify/openai 等',
  `model_name` varchar(128) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '模型名称',
  `model_config_id` int DEFAULT NULL COMMENT 'hou_base_ai_config.id',
  `scene` varchar(32) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'load|expand 等场景',
  `seed` varchar(256) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '种子词（可选）',
  `prompt_tokens` int unsigned DEFAULT '0' COMMENT '输入 tokens',
  `completion_tokens` int unsigned DEFAULT '0' COMMENT '输出 tokens',
  `total_tokens` int unsigned DEFAULT '0' COMMENT '总 tokens',
  `total_price` decimal(12,6) DEFAULT NULL COMMENT '费用',
  `currency` varchar(16) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '币种 USD/CNY',
  `latency_ms` int unsigned DEFAULT NULL COMMENT '延迟毫秒',
  `message_id` varchar(128) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'Dify message_id',
  `conversation_id` varchar(128) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'Dify conversation_id',
  `menu_level1` varchar(128) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '一级菜单',
  `menu_level2` varchar(128) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '二级菜单',
  `action_name` varchar(128) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '触发按钮或后台动作',
  `source_path` varchar(256) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '前端页面路径',
  `create_by` bigint unsigned DEFAULT NULL COMMENT '操作人',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_tenant_time` (`tenant_id`,`project_id`,`create_time`),
  KEY `idx_tenant_module_time` (`tenant_id`,`module_name`,`create_time`),
  KEY `idx_biz` (`biz_type`,`biz_id`),
  KEY `idx_tenant_biz_time` (`tenant_id`,`biz_type`,`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='AI Token 使用量';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_aicode_agent`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_aicode_agent` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint unsigned NOT NULL,
  `name` varchar(120) NOT NULL,
  `pairing_code_hash` varchar(128) DEFAULT NULL,
  `token_hash` varchar(128) DEFAULT NULL,
  `status` varchar(20) NOT NULL DEFAULT 'OFFLINE',
  `version` varchar(40) DEFAULT NULL,
  `last_heartbeat` datetime DEFAULT NULL,
  `create_time` datetime NOT NULL,
  `update_time` datetime NOT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_agent_tenant` (`tenant_id`,`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_aicode_analysis_task`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_aicode_analysis_task` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint unsigned NOT NULL,
  `code_source_id` bigint unsigned NOT NULL,
  `commit_sha` varchar(80) NOT NULL,
  `execution_mode` varchar(20) NOT NULL,
  `status` varchar(20) NOT NULL DEFAULT 'QUEUED',
  `error_message` varchar(1000) DEFAULT NULL,
  `index_version` int NOT NULL DEFAULT '1',
  `started_at` datetime DEFAULT NULL,
  `finished_at` datetime DEFAULT NULL,
  `create_time` datetime NOT NULL,
  `update_time` datetime NOT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_analysis_tenant_source` (`tenant_id`,`code_source_id`,`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_aicode_change_set`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_aicode_change_set` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint unsigned NOT NULL,
  `task_id` bigint unsigned NOT NULL,
  `plan_json` json DEFAULT NULL,
  `diff_text` longtext,
  `risk_level` varchar(20) NOT NULL DEFAULT 'LOW',
  `state` varchar(30) NOT NULL DEFAULT 'DIFF_READY',
  `create_time` datetime NOT NULL,
  `update_time` datetime NOT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_change_tenant_task` (`tenant_id`,`task_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_aicode_code_source`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_aicode_code_source` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint unsigned NOT NULL,
  `name` varchar(120) NOT NULL,
  `source_type` varchar(20) NOT NULL DEFAULT 'git',
  `repository_url` varchar(500) NOT NULL,
  `default_branch` varchar(120) NOT NULL DEFAULT 'main',
  `execution_mode` varchar(20) NOT NULL DEFAULT 'cloud',
  `credential_ref` varchar(120) DEFAULT NULL,
  `status` varchar(20) NOT NULL DEFAULT 'PENDING',
  `last_analyzed_commit` varchar(80) DEFAULT NULL,
  `create_time` datetime NOT NULL,
  `update_time` datetime NOT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_source_tenant` (`tenant_id`,`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_aicode_conversation`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_aicode_conversation` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint unsigned NOT NULL,
  `code_source_id` bigint unsigned NOT NULL,
  `module_id` bigint unsigned NOT NULL,
  `model_id` int DEFAULT NULL,
  `status` varchar(20) NOT NULL DEFAULT 'OPEN',
  `title` varchar(240) DEFAULT NULL,
  `prompt_tokens` int NOT NULL DEFAULT '0',
  `completion_tokens` int NOT NULL DEFAULT '0',
  `create_time` datetime NOT NULL,
  `update_time` datetime NOT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_conversation_tenant` (`tenant_id`,`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_aicode_git_operation`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_aicode_git_operation` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint unsigned NOT NULL,
  `task_id` bigint unsigned NOT NULL,
  `operation_type` varchar(30) NOT NULL,
  `branch_name` varchar(200) DEFAULT NULL,
  `commit_sha` varchar(80) DEFAULT NULL,
  `status` varchar(20) NOT NULL DEFAULT 'QUEUED',
  `log_text` text,
  `create_time` datetime NOT NULL,
  `update_time` datetime NOT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_git_operation_tenant_task` (`tenant_id`,`task_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_aicode_module`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_aicode_module` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint unsigned NOT NULL,
  `code_source_id` bigint unsigned NOT NULL,
  `parent_id` bigint unsigned NOT NULL DEFAULT '0',
  `name` varchar(160) NOT NULL,
  `module_path` varchar(500) NOT NULL,
  `module_type` varchar(40) NOT NULL DEFAULT 'business',
  `summary` text,
  `dependency_json` json DEFAULT NULL,
  `analysis_commit` varchar(80) DEFAULT NULL,
  `create_time` datetime NOT NULL,
  `update_time` datetime NOT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_module_tree` (`tenant_id`,`code_source_id`,`parent_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_aicode_module_file`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_aicode_module_file` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint unsigned NOT NULL,
  `module_id` bigint unsigned NOT NULL,
  `file_path` varchar(700) NOT NULL,
  `file_role` varchar(60) DEFAULT NULL,
  `summary` text,
  `content_hash` varchar(128) DEFAULT NULL,
  `create_time` datetime NOT NULL,
  `update_time` datetime NOT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_module_file` (`tenant_id`,`module_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_aicode_task`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_aicode_task` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint unsigned NOT NULL,
  `conversation_id` bigint unsigned NOT NULL,
  `requirement` text NOT NULL,
  `state` varchar(30) NOT NULL DEFAULT 'PLAN_READY',
  `branch_name` varchar(200) DEFAULT NULL,
  `base_branch` varchar(120) DEFAULT NULL,
  `create_time` datetime NOT NULL,
  `update_time` datetime NOT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_task_tenant` (`tenant_id`,`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_base_ai_config`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_base_ai_config` (
  `id` int unsigned NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `provider_code` varchar(50) COLLATE utf8mb4_general_ci NOT NULL COMMENT 'AI服务提供商代码',
  `provider_name` varchar(100) COLLATE utf8mb4_general_ci NOT NULL COMMENT 'AI服务提供商名称',
  `provider_name_en` varchar(100) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '英文名称',
  `model_name` varchar(100) COLLATE utf8mb4_general_ci NOT NULL COMMENT '模型名称',
  `model_version` varchar(50) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '模型版本',
  `api_key` varchar(500) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT 'API密钥(加密存储)',
  `api_secret` varchar(500) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT 'API密钥Secret(加密存储)',
  `api_endpoint` varchar(500) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT 'API端点地址',
  `app_id` varchar(100) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '应用ID',
  `region` varchar(50) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '服务区域',
  `max_tokens` int DEFAULT '2000' COMMENT '最大token数',
  `temperature` decimal(3,2) DEFAULT '0.70' COMMENT '温度参数(0-1)',
  `top_p` decimal(3,2) DEFAULT '0.90' COMMENT 'Top P参数(0-1)',
  `frequency_penalty` decimal(3,2) DEFAULT '0.00' COMMENT '频率惩罚(-2.0到2.0)',
  `presence_penalty` decimal(3,2) DEFAULT '0.00' COMMENT '存在惩罚(-2.0到2.0)',
  `is_enabled` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否启用: 0-禁用, 1-启用',
  `is_default` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否默认: 0-否, 1-是',
  `priority` int DEFAULT '0' COMMENT '优先级(数字越大优先级越高)',
  `rate_limit` int DEFAULT '60' COMMENT '速率限制(每分钟请求数)',
  `timeout` int DEFAULT '30' COMMENT '超时时间(秒)',
  `retry_times` int DEFAULT '3' COMMENT '重试次数',
  `support_stream` tinyint(1) DEFAULT '1' COMMENT '是否支持流式输出',
  `support_function` tinyint(1) DEFAULT '0' COMMENT '是否支持函数调用',
  `support_vision` tinyint(1) DEFAULT '0' COMMENT '是否支持视觉理解',
  `cost_per_1k_input` decimal(10,6) DEFAULT NULL COMMENT '每1K输入token成本(元)',
  `cost_per_1k_output` decimal(10,6) DEFAULT NULL COMMENT '每1K输出token成本(元)',
  `description` text COLLATE utf8mb4_general_ci COMMENT '配置说明',
  `config_json` text COLLATE utf8mb4_general_ci COMMENT '额外配置(JSON格式)',
  `status` tinyint(1) DEFAULT '1' COMMENT '状态: 0-停用, 1-正常, 2-维护中',
  `sort_order` int DEFAULT '0' COMMENT '排序顺序',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uk_provider_model` (`provider_code`,`model_name`) USING BTREE,
  KEY `idx_provider_code` (`provider_code`) USING BTREE,
  KEY `idx_is_enabled` (`is_enabled`) USING BTREE,
  KEY `idx_is_default` (`is_default`) USING BTREE,
  KEY `idx_status` (`status`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='AI服务配置表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_base_ai_import_attachment`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_base_ai_import_attachment` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `draft_id` bigint NOT NULL,
  `file_id` bigint NOT NULL,
  `object_key` varchar(128) DEFAULT NULL,
  `business_record_id` varchar(128) DEFAULT NULL,
  `file_name` varchar(255) NOT NULL,
  `file_url` varchar(1000) NOT NULL,
  `status` varchar(32) NOT NULL DEFAULT 'pending',
  `error_message` varchar(1000) DEFAULT NULL,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_ai_import_attachment_record` (`object_key`,`business_record_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='AI智能入库附件';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_base_ai_import_draft`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_base_ai_import_draft` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `draft_no` varchar(64) NOT NULL,
  `user_id` varchar(64) NOT NULL,
  `org_id` varchar(64) DEFAULT NULL,
  `system_key` varchar(64) NOT NULL,
  `description` varchar(500) NOT NULL,
  `status` varchar(32) NOT NULL DEFAULT 'draft',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_ai_import_draft_no` (`draft_no`),
  KEY `idx_ai_import_draft_user_status` (`user_id`,`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='AI智能入库草稿';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_base_ai_import_file`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_base_ai_import_file` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `draft_id` bigint NOT NULL,
  `file_name` varchar(255) NOT NULL,
  `file_hash` varchar(64) NOT NULL,
  `file_url` varchar(1000) DEFAULT NULL,
  `file_size` bigint NOT NULL DEFAULT '0',
  `content_type` varchar(128) DEFAULT NULL,
  `ai_raw_json` json DEFAULT NULL,
  `edited_fields_json` json DEFAULT NULL,
  `status` varchar(32) NOT NULL DEFAULT 'uploaded',
  `error_message` varchar(1000) DEFAULT NULL,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_ai_import_draft_file` (`draft_id`,`file_hash`),
  KEY `idx_ai_import_file_draft_status` (`draft_id`,`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='AI智能入库文件';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_base_ai_import_result`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_base_ai_import_result` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `draft_id` bigint NOT NULL,
  `file_id` bigint NOT NULL,
  `function_key` varchar(128) DEFAULT NULL,
  `object_key` varchar(128) DEFAULT NULL,
  `target_table` varchar(128) DEFAULT NULL,
  `ai_fields_json` json DEFAULT NULL,
  `edited_fields_json` json DEFAULT NULL,
  `status` varchar(32) NOT NULL DEFAULT 'pending',
  `error_message` varchar(1000) DEFAULT NULL,
  `business_record_id` varchar(128) DEFAULT NULL,
  `detail_url` varchar(1000) DEFAULT NULL,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_ai_import_result_file` (`file_id`),
  KEY `idx_ai_import_result_draft_status` (`draft_id`,`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='AI智能入库结果';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_base_article_base`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_base_article_base` (
  `article_id` int NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `article_desc` mediumtext CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL COMMENT '描述',
  `article_title` varchar(30) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL DEFAULT '' COMMENT '标题',
  `article_url` varchar(100) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL DEFAULT '' COMMENT '调用网址-url，默认为本页面构造的网址，可填写其它页面',
  `article_group_id` tinyint NOT NULL COMMENT '组',
  `article_sort` int NOT NULL DEFAULT '0' COMMENT '排序',
  `article_status` int NOT NULL DEFAULT '2' COMMENT '状态 1:启用  2:关闭',
  `article_add_time` datetime NOT NULL COMMENT '添加世间',
  PRIMARY KEY (`article_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 ROW_FORMAT=DYNAMIC COMMENT='网站初始化内容设置';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_base_article_group`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_base_article_group` (
  `article_group_id` int NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `article_group_title` varchar(60) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL DEFAULT '' COMMENT '标题',
  `article_group_sort` smallint NOT NULL DEFAULT '0' COMMENT '排序',
  PRIMARY KEY (`article_group_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 ROW_FORMAT=DYNAMIC COMMENT='网站初始化内容分组表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_base_country`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_base_country` (
  `id` int NOT NULL AUTO_INCREMENT,
  `country_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '国家',
  `international_dialing_code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '国际区号',
  `delete_type` tinyint(1) DEFAULT '2' COMMENT '是否允许删除 1 是  2 否',
  `country_initial` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '国家首字母',
  `country_common_use` tinyint DEFAULT '2' COMMENT '是否常用 1 是 2 否',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci ROW_FORMAT=DYNAMIC COMMENT='国家';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_base_currency_symbol`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_base_currency_symbol` (
  `id` int unsigned NOT NULL AUTO_INCREMENT COMMENT '货币符号主键',
  `currency_symbol` varchar(4) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '￥' COMMENT '货币符号',
  `remark` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '备注',
  `status` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否使用',
  `exchange_rate` decimal(10,4) DEFAULT '0.0000' COMMENT '汇率',
  `currency_code` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '货币代码',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_base_dashboard_pref`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_base_dashboard_pref` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `user_id` varchar(64) NOT NULL COMMENT '用户ID（userId，与 JWT 解析一致）',
  `org_id` varchar(64) DEFAULT NULL COMMENT '组织ID（可空）',
  `cols` int NOT NULL DEFAULT '12' COMMENT '栅格列数（默认12）',
  `refresh_interval` int NOT NULL DEFAULT '0' COMMENT '自动刷新间隔(秒)，0表示关闭',
  `version` int NOT NULL DEFAULT '1' COMMENT '版本号（保存时 +1）',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='Dashboard 用户偏好主表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_base_dashboard_pref_item`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_base_dashboard_pref_item` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `pref_id` bigint NOT NULL COMMENT '主表id',
  `module_id` varchar(64) NOT NULL COMMENT '模块标识（如 oa/payment/contract/erp/...）',
  `visible` tinyint NOT NULL DEFAULT '1' COMMENT '是否显示 1是 0否',
  `sort` int NOT NULL DEFAULT '0' COMMENT '排序（小值在前）',
  `x` int NOT NULL DEFAULT '0' COMMENT '栅格 X',
  `y` int NOT NULL DEFAULT '0' COMMENT '栅格 Y',
  `w` int NOT NULL DEFAULT '4' COMMENT '栅格 宽度（column span，默认 4）',
  `h` int NOT NULL DEFAULT '1' COMMENT '栅格 高度',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_pref_module` (`pref_id`,`module_id`),
  KEY `idx_pref` (`pref_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='Dashboard 用户偏好明细表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_base_dashboard_widget`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_base_dashboard_widget` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` varchar(64) NOT NULL,
  `org_id` varchar(64) DEFAULT NULL,
  `widget_key` varchar(64) NOT NULL,
  `widget_name` varchar(128) DEFAULT NULL,
  `x` int NOT NULL DEFAULT '0',
  `y` int NOT NULL DEFAULT '0',
  `w` int NOT NULL DEFAULT '4',
  `h` int NOT NULL DEFAULT '4',
  `config` text,
  `enabled` tinyint(1) NOT NULL DEFAULT '1',
  `sort_order` int NOT NULL DEFAULT '0',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_base_dealer_apply_info`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_base_dealer_apply_info` (
  `id` int unsigned NOT NULL AUTO_INCREMENT COMMENT '主键id',
  `supplier_shop_id` int NOT NULL COMMENT '供应商店铺id',
  `supplier_shop_name` varchar(50) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL COMMENT '供应商店铺名',
  `supplier_user_id` varchar(50) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL COMMENT '供应商用户id',
  `supplier_user_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '供应商用户名',
  `dealer_shop_id` int NOT NULL COMMENT '经销商id',
  `dealer_shop_name` varchar(50) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL COMMENT '经销商店铺名',
  `dealer_user_id` varchar(50) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL COMMENT '经销商用户id',
  `dealer_user_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '经销商用户名',
  `dealer_user_phone` varchar(20) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL COMMENT '经销商手机号',
  `dealer_status` tinyint(1) NOT NULL DEFAULT '1' COMMENT '分销状态 1待审核  2审核通过 3审核不通过  4已删除',
  `dealer_remark` varchar(1000) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT '' COMMENT '申请审核说明',
  `dealer_level_id` int DEFAULT '0' COMMENT '经销等级',
  `dealer_level_name` varchar(100) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '经销等级名称',
  `dealer_discount` int DEFAULT '0' COMMENT '经销折扣',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 ROW_FORMAT=DYNAMIC COMMENT='分销商申请信息表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_base_dealer_level_info`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_base_dealer_level_info` (
  `dealer_level_id` int unsigned NOT NULL AUTO_INCREMENT COMMENT '经销商等级id',
  `supplier_shop_id` int NOT NULL COMMENT '供应商店铺id',
  `dealer_level_name` varchar(100) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL COMMENT '经销商等级名称',
  `dealer_discount` int NOT NULL COMMENT '等级折扣',
  `dealer_level_sort` int NOT NULL DEFAULT '1' COMMENT '等级排序',
  `dealer_level_status` tinyint(1) NOT NULL DEFAULT '1' COMMENT '经销商等级状态  1正常 2删除',
  PRIMARY KEY (`dealer_level_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 ROW_FORMAT=DYNAMIC COMMENT='经销商等级';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_base_dealer_putaway_record`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_base_dealer_putaway_record` (
  `id` int unsigned NOT NULL AUTO_INCREMENT COMMENT '主键id',
  `supply_common_id` int NOT NULL COMMENT '供应商商品commonId',
  `supply_goods_id` int NOT NULL DEFAULT '0' COMMENT '立即购买供应商品id',
  `dealer_common_id` int NOT NULL COMMENT '经销商品commonId',
  `dealer_goods_id` int NOT NULL DEFAULT '0' COMMENT '立即购买经销商品id（一键上架全部上架值为0）',
  `dealer_shop_id` int NOT NULL COMMENT '经销店铺id',
  `dealer_shop_name` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL COMMENT '经销店铺名',
  `supplier_shop_id` int NOT NULL COMMENT '供应商店铺id',
  `supplier_shop_name` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '供应商店铺',
  `dealer_price` decimal(20,2) NOT NULL COMMENT '经销供货价',
  `dealer_num` int NOT NULL DEFAULT '0' COMMENT '上架数量',
  `record_state` tinyint(1) NOT NULL DEFAULT '1' COMMENT '一键上架记录状态  1正常  2取消',
  `dealer_type` tinyint(1) DEFAULT '1' COMMENT '1一键上架  2立即购买',
  `dealer_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '经销发布时间',
  `is_price_update` int DEFAULT '2' COMMENT '同步商品商品价格是否修改 1 是 2 否 未修改价格的商品不展示',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 ROW_FORMAT=DYNAMIC COMMENT='商品供应记录';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_base_dealer_set`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_base_dealer_set` (
  `id` int unsigned NOT NULL AUTO_INCREMENT COMMENT '主键id',
  `config_key` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '配置key',
  `value1` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '0' COMMENT '爆品数量/佣金比例  都用这个字段',
  `value2` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '0' COMMENT '上限折扣价',
  `value3` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '0' COMMENT '折扣价',
  `config_enable` tinyint(1) NOT NULL DEFAULT '1' COMMENT '1可用  2不可用',
  `remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '说明',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci ROW_FORMAT=DYNAMIC COMMENT='供应商配置项';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_base_department`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_base_department` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `org_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '公司组织',
  `department_code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '部门代码',
  `department_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '部门名称',
  `department_full_name` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '部门全称',
  `parent_id` bigint DEFAULT NULL COMMENT '上级部门ID',
  `parent_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '上级部门名称',
  `department_level` int DEFAULT '1' COMMENT '部门层级',
  `department_path` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '部门路径(用/分隔)',
  `manager_id` bigint DEFAULT NULL COMMENT '部门负责人员工ID',
  `manager_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '部门负责人姓名',
  `deputy_manager_id` bigint DEFAULT NULL COMMENT '部门副负责人员工ID',
  `deputy_manager_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '部门副负责人姓名',
  `department_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT '管理部门' COMMENT '部门类型: 管理部门/业务部门/支持部门/临时部门',
  `department_status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT '正常' COMMENT '部门状态: 正常/停用/合并/撤销',
  `employee_count` int DEFAULT '0' COMMENT '员工人数',
  `budget_amount` decimal(15,2) DEFAULT '0.00' COMMENT '预算金额',
  `cost_center_code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '成本中心代码',
  `office_location` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '办公地点',
  `office_phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '办公电话',
  `office_email` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '部门邮箱',
  `establishment_date` datetime DEFAULT NULL COMMENT '成立日期',
  `sort_order` int DEFAULT '0' COMMENT '排序号',
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci COMMENT '部门描述',
  `responsibilities` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci COMMENT '部门职责',
  `remarks` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci COMMENT '备注',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `del_flag` tinyint DEFAULT '0' COMMENT '删除标志 0-正常 1-删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_department_code` (`department_code`),
  KEY `idx_parent_id` (`parent_id`),
  KEY `idx_department_name` (`department_name`),
  KEY `idx_department_status` (`department_status`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='OA部门管理表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_base_district`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_base_district` (
  `district_id` mediumint unsigned NOT NULL AUTO_INCREMENT COMMENT '地区id',
  `district_name` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL DEFAULT '' COMMENT '地区名称',
  `district_code` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '行政区划代码',
  `district_parent_id` mediumint unsigned NOT NULL DEFAULT '0' COMMENT '父id',
  `district_is_level` tinyint(1) NOT NULL DEFAULT '1' COMMENT '等级',
  PRIMARY KEY (`district_id`) USING BTREE,
  KEY `upid` (`district_parent_id`) USING BTREE COMMENT '(null)',
  KEY `district_id` (`district_id`) USING BTREE COMMENT 'null'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 ROW_FORMAT=DYNAMIC COMMENT='地区表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_base_express`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_base_express` (
  `express_id` int NOT NULL AUTO_INCREMENT,
  `express_name` varchar(30) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL COMMENT '快递公司',
  `express_pinyin` varchar(30) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL COMMENT '物流',
  `express_status` tinyint(1) NOT NULL DEFAULT '0' COMMENT '状态0关闭1开启',
  PRIMARY KEY (`express_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 ROW_FORMAT=DYNAMIC COMMENT='快递表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_base_floor_adpage`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_base_floor_adpage` (
  `floor_adpage_id` int NOT NULL AUTO_INCREMENT COMMENT '楼层id',
  `floor_adpage_name` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL DEFAULT '' COMMENT '楼层名称',
  `left_adpage_banner` text CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL COMMENT '左侧轮播',
  `right_hot_recommend` text CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci COMMENT '特色推荐',
  `left_adpage_nav` text CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL COMMENT '左侧楼层导航',
  `right_adpage_nav` text CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL COMMENT '右侧楼层导航',
  `floor_adpage_order` int DEFAULT '9999999' COMMENT '楼层默认排序',
  `floor_status` int DEFAULT '1' COMMENT '楼层状态1启用2不启用',
  `floor_update_time` datetime DEFAULT '1970-05-24 10:42:02' COMMENT '楼层更新时间',
  `floor_type` tinyint(1) NOT NULL COMMENT '1 普通店铺配置  2供应商配置',
  `page_type` int DEFAULT NULL COMMENT '页面类型  5单楼层 6多楼层 1 天天特价 2 特色专题  3 为你推荐 4家居馆  (1、2、3、4 状态在gdb_pc_index_config表）',
  PRIMARY KEY (`floor_adpage_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 ROW_FORMAT=DYNAMIC COMMENT='pc首页楼层广告';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_base_information_classify`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_base_information_classify` (
  `classify_id` int NOT NULL AUTO_INCREMENT COMMENT '分类id',
  `classify_name` varchar(50) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL COMMENT '分类名称',
  `classify_parent_id` int NOT NULL DEFAULT '0' COMMENT '父类',
  `classify_sort` smallint NOT NULL DEFAULT '255' COMMENT '排序',
  `classify_level` tinyint(1) NOT NULL COMMENT '分类级别',
  `classify_show_status` tinyint(1) DEFAULT '1' COMMENT '是否在前台显示',
  PRIMARY KEY (`classify_id`) USING BTREE,
  KEY `classify_parent_id` (`classify_parent_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 ROW_FORMAT=DYNAMIC COMMENT='资讯分类表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_base_information_info`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_base_information_info` (
  `information_id` int NOT NULL AUTO_INCREMENT COMMENT '资讯id',
  `classify_id` int NOT NULL COMMENT '资讯分类id',
  `information_title` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '资讯标题',
  `information_cover` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci COMMENT '资讯封面',
  `information_desc` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci COMMENT '资讯内容',
  `information_status` tinyint(1) DEFAULT NULL COMMENT '资讯状态 0已暂存 1已发布 2已下架',
  `page_view` int DEFAULT '0' COMMENT '浏览量',
  `page_view_set` int DEFAULT '0' COMMENT '设置的浏览量',
  `page_view_natural` int DEFAULT '0' COMMENT '自然浏览量',
  `is_recommend` tinyint(1) DEFAULT '0' COMMENT '是否推荐 0不推荐 1推荐',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `release_source` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '发布来源',
  `release_time` datetime DEFAULT NULL COMMENT '发布时间',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`information_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci ROW_FORMAT=DYNAMIC COMMENT='资讯文章表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_base_integral_rules`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_base_integral_rules` (
  `id` int NOT NULL AUTO_INCREMENT,
  `rule_login` int NOT NULL COMMENT '登录赠送积分',
  `rule_register` int DEFAULT NULL COMMENT '注册赠送积分',
  `rule_consume` int DEFAULT NULL COMMENT '\r\n消费与赠送积分比例',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 ROW_FORMAT=DYNAMIC COMMENT='积分规则表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_base_kd100_config`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_base_kd100_config` (
  `id` int NOT NULL AUTO_INCREMENT,
  `customer` varchar(100) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '快递100 Customer',
  `key` varchar(100) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '快递100 Key',
  `api_url` varchar(255) COLLATE utf8mb4_general_ci DEFAULT 'https://poll.kuaidi100.com/poll/query.do' COMMENT '接口地址',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='快递100配置表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_base_kdniao_config`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_base_kdniao_config` (
  `id` int NOT NULL AUTO_INCREMENT,
  `EBussinessID` varchar(50) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '快递鸟Id',
  `AppKey` varchar(50) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT '0' COMMENT '快递鸟密钥',
  `instruct_code` int DEFAULT NULL COMMENT '快递鸟接口指令 购买套餐不一样 指令不一样',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 ROW_FORMAT=DYNAMIC COMMENT='店铺批量打印快递鸟信息表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_base_language_translation`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_base_language_translation` (
  `id` int unsigned NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `cn_text` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '中文原文',
  `language_code` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '语言代码(en/ja/ko/fr/de)',
  `original_translation` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci COMMENT '原始译文(AI翻译)',
  `corrected_translation` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci COMMENT '修正后的译文(人工校正)',
  `is_corrected` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否已修正: 0-未修正, 1-已修正',
  `translation_source` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT 'AI' COMMENT '翻译来源: AI/Manual',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uk_cn_language` (`cn_text`(100),`language_code`) USING BTREE,
  KEY `idx_cn_text` (`cn_text`(100)) USING BTREE,
  KEY `idx_language_code` (`language_code`) USING BTREE,
  KEY `idx_is_corrected` (`is_corrected`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='语言包翻译表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_base_membership_grade`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_base_membership_grade` (
  `id` int NOT NULL AUTO_INCREMENT,
  `grade_name` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '等级名称',
  `grade_rule` int DEFAULT NULL COMMENT '等级规则',
  `grade_discount` decimal(10,2) DEFAULT NULL COMMENT '折扣',
  `grade_start` int DEFAULT NULL COMMENT '1正常 0 删除',
  `grade_url` varchar(500) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '等级图片url',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 ROW_FORMAT=DYNAMIC COMMENT='会员等级';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_base_merchant_agreement`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_base_merchant_agreement` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '协议id',
  `name` varchar(50) NOT NULL COMMENT '商家协议/用户协议',
  `url` varchar(250) NOT NULL COMMENT 'url',
  `sign` varchar(50) NOT NULL COMMENT '标记',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '上传时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_merchant_agreement_sign` (`sign`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='用户/商家协议表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_base_merchant_plus_rule`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_base_merchant_plus_rule` (
  `plus_ruleid` int NOT NULL AUTO_INCREMENT COMMENT '规则ID',
  `plus_propaganda` varchar(512) DEFAULT NULL COMMENT 'plus会员宣传语',
  `plus_signs` varchar(512) DEFAULT NULL COMMENT '推荐标识语',
  `plus_monthcardlogo` varchar(512) DEFAULT NULL COMMENT '月卡logo',
  `plus_seasoncardlogo` varchar(512) DEFAULT NULL COMMENT '季卡logo',
  `plus_yearcardlogo` varchar(512) DEFAULT NULL COMMENT '年卡logo',
  `plus_backdrop` varchar(512) DEFAULT NULL COMMENT '开通界面背景图',
  `plus_memberagreement` mediumtext COMMENT 'plus会员用户协议',
  `plus_merchantnotice` mediumtext COMMENT '商家须知',
  `plus_status` tinyint NOT NULL DEFAULT '0' COMMENT '0关闭 1开启',
  `plus_updatetime` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`plus_ruleid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='PLUS会员平台规则';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_base_message_template`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_base_message_template` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `template_code` varchar(100) NOT NULL COMMENT '模板编码(唯一)',
  `template_name` varchar(200) NOT NULL COMMENT '模板名称',
  `biz_type` varchar(50) DEFAULT NULL COMMENT '业务类型: PROJECT/TASK/ORDER/WORKFLOW等',
  `title_template` text COMMENT '标题模板',
  `content_template` text COMMENT '内容模板(支持变量如${projectName})',
  `channels` varchar(200) DEFAULT NULL COMMENT '支持渠道(逗号分隔): INBOX,EMAIL,SMS,DINGTALK,WECOM,WECHAT,FEISHU',
  `inbox_template_code` varchar(100) DEFAULT NULL COMMENT '站内信模板CODE',
  `email_template_code` varchar(100) DEFAULT NULL COMMENT '邮件模板CODE',
  `sms_template_code` varchar(100) DEFAULT NULL COMMENT '短信模板CODE',
  `dingtalk_template_code` varchar(100) DEFAULT NULL COMMENT '钉钉模板CODE',
  `wecom_template_code` varchar(100) DEFAULT NULL COMMENT '企业微信模板CODE',
  `wechat_template_code` varchar(100) DEFAULT NULL COMMENT '微信公众号模板CODE',
  `feishu_template_code` varchar(100) DEFAULT NULL COMMENT '飞书模板CODE',
  `status` int NOT NULL DEFAULT '1' COMMENT '状态: 0禁用 1启用',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  `org_id` varchar(50) DEFAULT NULL COMMENT '组织ID',
  `create_time` int DEFAULT NULL COMMENT '创建时间',
  `update_time` int DEFAULT NULL COMMENT '更新时间',
  `delete_time` int DEFAULT NULL COMMENT '删除时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_template_code` (`template_code`),
  KEY `idx_biz_type` (`biz_type`),
  KEY `idx_status` (`status`),
  KEY `idx_org_id` (`org_id`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='消息模板表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_base_modularization_flie`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_base_modularization_flie` (
  `id` int NOT NULL AUTO_INCREMENT,
  `file_name` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '文件名称 + 文件后缀',
  `file_url` varchar(500) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '文件地址',
  `file_cover_url` varchar(500) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '视频封面url',
  `file_type` int DEFAULT NULL COMMENT '文件类型  1 图片 2 视频',
  `file_create_time` datetime DEFAULT NULL COMMENT '文件上传时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 ROW_FORMAT=DYNAMIC COMMENT='组件化  图片 视频空间  ';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_base_modularization_flie_shop`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_base_modularization_flie_shop` (
  `id` int NOT NULL AUTO_INCREMENT,
  `shop_id` int DEFAULT NULL COMMENT '店铺id',
  `file_name` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '文件名称+文件后缀',
  `file_url` varchar(500) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '文件地址',
  `file_cover_url` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '视频封面url',
  `file_type` int DEFAULT NULL COMMENT '文件类型 1图片 2视频',
  `file_create_time` datetime DEFAULT NULL COMMENT '文件上传时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_base_modularizationd`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_base_modularizationd` (
  `page_id` int NOT NULL AUTO_INCREMENT COMMENT '页面id',
  `page_name` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '页面名称',
  `page_data` longtext CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci COMMENT '页面内容',
  `page_type` int DEFAULT '0' COMMENT '0 未发布 1 以发布',
  `page_start` int DEFAULT '0' COMMENT '0 未使用 1已使用',
  `page_default` int DEFAULT '0' COMMENT '0 不是系统默认 1 是系统默认 ',
  `page_cover_url` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '系统默认页面 封面url',
  `page_is_index` int DEFAULT '0' COMMENT '是否是首页 1 是 2 二级页  3 底部导航',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `goodsAllIdJson` text CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci COMMENT '当前页面的所有商品id',
  PRIMARY KEY (`page_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 ROW_FORMAT=DYNAMIC COMMENT='组件化 页面';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_base_modularizationd_cat`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_base_modularizationd_cat` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT 'id',
  `page_name` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '页面名称',
  `page_type` int DEFAULT NULL COMMENT '页面类型 1 三级分类页 2 二级分类页 3 二级分类页样式二 4一级分类样式一 5 一级分类样式二',
  `page_data` longtext CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci COMMENT '页面数据',
  `page_start` int DEFAULT NULL COMMENT '页面状态 1 使用中2 未使用',
  `create_time` datetime DEFAULT NULL COMMENT '时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 ROW_FORMAT=DYNAMIC COMMENT='模板装修分类页面';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_base_organization`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_base_organization` (
  `org_id` int NOT NULL AUTO_INCREMENT COMMENT '组织ID',
  `org_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '组织名称',
  `org_code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '组织编码',
  `org_type` int DEFAULT '1' COMMENT '组织类型：1-国内，2-海个，3-其他',
  `org_status` int DEFAULT '1' COMMENT '状态：1-正常，0-禁用',
  `contact_phone` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '联系电话',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`org_id`),
  UNIQUE KEY `uk_org_code` (`org_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='组织管理表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_base_oss_config`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_base_oss_config` (
  `oss_id` int NOT NULL AUTO_INCREMENT COMMENT '主键',
  `oss_type` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT 'oss类型',
  `oss_backetName` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT 'API的bucket名称',
  `oss_folder` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT 'API的文件夹名称',
  `oss_region` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '服务器集群地址',
  `oss_endpoint` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '域名',
  `oss_accessKeyId` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '密钥',
  `oss_accessKeySecret` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '私钥',
  `oss_states` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT '1' COMMENT '状态值 00禁用  01启用，默认启用',
  `oss_isActive` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT '0' COMMENT '伪删除标识 00删除 01正常，默认删除',
  `oss_createdDate` datetime DEFAULT NULL COMMENT '创建时间',
  `oss_updateDate` datetime DEFAULT NULL COMMENT '修改时间',
  `oss_remark` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '配置的备注信息',
  PRIMARY KEY (`oss_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 ROW_FORMAT=DYNAMIC COMMENT='阿里OSS配置信息表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_base_pc_index_config`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_base_pc_index_config` (
  `id` int NOT NULL AUTO_INCREMENT,
  `page_name` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '页面名称',
  `page_type` int DEFAULT NULL COMMENT '页面类型 1 天天特价 2 特色专题  3 为你推荐 4家居馆 5单楼层 6多楼层（5、6状态在gdb_merchant_floor_adpage表）',
  `page_title` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '页面标题',
  `page_two_title` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '二级页面标题',
  `page_data` text CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci COMMENT '页面数据',
  `page_start` int DEFAULT '1' COMMENT '页面状态 1 开启 2 关闭',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '创建时间',
  `page_sort` int DEFAULT NULL COMMENT '排序',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_base_platform_brank`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_base_platform_brank` (
  `id` int NOT NULL AUTO_INCREMENT,
  `bank_card_id` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL COMMENT '银行卡卡号',
  `bank_card_name` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL COMMENT '开户人姓名',
  `bank_name` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL COMMENT '开户银行名称',
  `brank_note` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '备注',
  `brank_update_time` datetime(6) NOT NULL COMMENT '修改时间',
  `brank_unionpay_good` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '银联号',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 ROW_FORMAT=DYNAMIC COMMENT='平台收款银行卡表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_base_protocol`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_base_protocol` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `name` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '协议名称',
  `type` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '协议类型：PERSONAL-个人入驻，ENTERPRISE-企业入驻，SUPPLIER-供应商入驻，COOPERATION-合作协议',
  `file_url` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '文件URL',
  `file_name` varchar(200) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '文件名称',
  `sort` int DEFAULT '0' COMMENT '排序',
  `status` tinyint DEFAULT '1' COMMENT '状态：1-启用，0-禁用',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_type` (`type`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='协议管理表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_base_search_word`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_base_search_word` (
  `search_id` int NOT NULL AUTO_INCREMENT,
  `search_keyword` varchar(200) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT 'key值',
  `search_char_index` varchar(200) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '下标',
  `search_nums` int DEFAULT '0' COMMENT '数量',
  PRIMARY KEY (`search_id`) USING BTREE,
  KEY `search_keyword` (`search_keyword`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 ROW_FORMAT=DYNAMIC COMMENT='搜索热门词';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_base_sensitive`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_base_sensitive` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT 'id',
  `sensitive_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '敏感词',
  `sensitive_classification_id` int DEFAULT NULL COMMENT '敏感词分类id',
  `sensitive_add_time` datetime DEFAULT NULL COMMENT '添加时间',
  `sensitive_state` int DEFAULT '1' COMMENT '状态 1启用  0 不启用',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci ROW_FORMAT=DYNAMIC COMMENT='敏感词词库';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_base_sensitive_classification`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_base_sensitive_classification` (
  `classification_id` int NOT NULL AUTO_INCREMENT COMMENT 'id',
  `classification_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '分类名称',
  `classification_add_time` datetime DEFAULT NULL COMMENT '添加时间',
  `classification_state` int DEFAULT '1' COMMENT '状态 1启用  0 不启用',
  PRIMARY KEY (`classification_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci ROW_FORMAT=DYNAMIC COMMENT='敏感词分类';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_base_sensitive_config`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_base_sensitive_config` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT 'id',
  `sensitive_start` int DEFAULT '1' COMMENT '敏感词开关 1 开 0 关',
  `sensitive_server` int DEFAULT '1' COMMENT '敏感词服务 1 平台服务  2 第三方服务',
  `access_key_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '阿里云AccessKey ID',
  `access_key_secret` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '阿里云AccessKey Secret',
  `region_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '阿里云服务地域ID',
  `member_information_start` int DEFAULT '0' COMMENT '会员信息 是否开启敏感词 1 是 0 否',
  `product_evaluation_start` int DEFAULT '0' COMMENT '产品评价 是否开启敏感词 1 是 0 否',
  `supplier_information_start` int DEFAULT '0' COMMENT '供应商信息 是否开启敏感词 1 是 0 否',
  `product_information_start` int DEFAULT '0' COMMENT '产品信息 是否开启敏感词 1 是 0 否',
  `sales_promotion_start` int DEFAULT '0' COMMENT '促销活动 是否开启敏感词 1 是 0 否',
  `decoration_module_start` int DEFAULT '0' COMMENT '装修模块 是否开启敏感词 1 是 0 否',
  `cause_management_start` int DEFAULT '0' COMMENT '原因管理 是否开启敏感词 1 是 0 否',
  `article_management_start` int DEFAULT '0' COMMENT '文章管理 是否开启敏感词 1 是 0 否',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci ROW_FORMAT=DYNAMIC COMMENT='敏感词配置';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_base_sensitive_record`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_base_sensitive_record` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT 'id',
  `sensitive_txt` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '文本内容',
  `sensitive_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '敏感词',
  `classification_id` int DEFAULT NULL COMMENT '敏感词分类id',
  `record_add_time` datetime DEFAULT NULL COMMENT '命中时间',
  `user_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '用户id',
  `user_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '用户名称',
  `user_type` int DEFAULT NULL COMMENT '用户类别 1 普通用户 2 平台用户 ',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci ROW_FORMAT=DYNAMIC COMMENT='敏感词命中记录';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_base_share_url`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_base_share_url` (
  `id` int NOT NULL AUTO_INCREMENT,
  `share_desc` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT '' COMMENT '描述',
  `share_url` varchar(1000) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL DEFAULT '' COMMENT '分享路径',
  `type` tinyint NOT NULL DEFAULT '0' COMMENT '1自由拼团分享路径',
  `update_time` datetime NOT NULL DEFAULT '2020-03-01 10:00:28' COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 ROW_FORMAT=DYNAMIC COMMENT='分享地址';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_base_shop_modularizationd`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_base_shop_modularizationd` (
  `page_id` int NOT NULL COMMENT '页面id',
  `shop_id` int DEFAULT NULL COMMENT '店铺id',
  `page_name` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '页面名称',
  `page_data` longtext CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci COMMENT '页面内容',
  `page_type` int DEFAULT NULL COMMENT '0未发布 1已发布',
  `page_start` int DEFAULT NULL COMMENT '0未使用 1已使用',
  `page_is_index` int DEFAULT NULL COMMENT '是否是首页 1 是 2二级页  3 底部导航',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `goodsAllIdJson` text CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci COMMENT '当前商品所有的id',
  PRIMARY KEY (`page_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_base_sms_info`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_base_sms_info` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `org_id` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL,
  `sms_secret_id` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '私钥',
  `sms_secret_key` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '密钥',
  `sms_sdk_app_id` varchar(100) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '短信工具应用APP的id',
  `sms_sign` varchar(100) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '公司标识',
  `sms_status` varchar(50) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL DEFAULT '1' COMMENT '启用和禁用状态，00禁用  01启用，默认启用',
  `sms_type_name` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '类型名称',
  `sms_type` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT 'sms类型',
  `sms_region` varchar(50) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '服务器集群地址',
  `sms_isActive` varchar(50) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL DEFAULT '1' COMMENT '伪删除标识 00删除 01正常，默认正常',
  `sms_remark` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '信息配置备注',
  `sms_createdDate` timestamp NULL DEFAULT NULL COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 ROW_FORMAT=DYNAMIC COMMENT='短信配置信息表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_base_sys_configs`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_base_sys_configs` (
  `config_key` varchar(225) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL COMMENT '配置键',
  `config_value` longtext CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci COMMENT '配置值:imageType;//商品id2:店铺id3:链接4:分类10:红包11:美食中国12:为你推荐13:特价推荐14:新品推荐''15:溯源资讯16:店铺活动17:精选商品18:商城公告'',19新人专区;20平台活动专区;21:9.9包邮专区;22:品牌生活;23:好货溯源',
  `config_type` varchar(50) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL DEFAULT '' COMMENT '配置类型',
  `config_comment` text CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL COMMENT '配置内容描述',
  `config_sort` int NOT NULL DEFAULT '0' COMMENT '配置排序',
  `org_id` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '公司组织ID',
  `op_type` varchar(100) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL DEFAULT '' COMMENT '操作类型，1关键字，2商品编号{"goodsId":5026,"commonId":5026}，3链接{"url":http://},4店铺{"shopId":11} 5不传值 6分类id {\\"catId\\":10000104}',
  `op_value` text CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL COMMENT '操作类型值',
  `config_enable` tinyint(1) NOT NULL DEFAULT '1' COMMENT '状态值，1可用，0不可用',
  `terminal_type` int NOT NULL DEFAULT '1' COMMENT '终端选择，1pc，2移动端',
  `create_by` varchar(32) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL DEFAULT '0' COMMENT '创建人',
  `create_date` datetime DEFAULT '2018-09-05 00:00:00' COMMENT '创建时间',
  `update_by` varchar(32) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL DEFAULT '0' COMMENT '更新人',
  `update_date` datetime DEFAULT '2018-09-05 00:00:00' COMMENT '更新时间',
  PRIMARY KEY (`config_key`) USING BTREE,
  KEY `config_type` (`config_type`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 ROW_FORMAT=DYNAMIC COMMENT='配置表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_base_system_message`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_base_system_message` (
  `message_id` int NOT NULL AUTO_INCREMENT COMMENT '消息id',
  `message_user_id` varchar(50) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL COMMENT '消息接收者id',
  `message_user_name` varchar(50) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '消息接收者',
  `message_type` tinyint(1) NOT NULL DEFAULT '1' COMMENT '消息类型：1平台公告 2被投诉提醒 3违规提醒 4催发货提醒 5其他消息通知',
  `message_title` varchar(100) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL COMMENT '消息标题',
  `message_content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '消息内容',
  `message_islook` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否读取0未1读取',
  `message_create_time` datetime NOT NULL COMMENT '消息创建时间',
  `message_isdel` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否删除 0：未删除 1：已删除',
  `shop_id` varchar(225) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '店铺id',
  `create_user` varchar(32) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `ts` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '时间戳',
  `message_order_id` varchar(50) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '订单id',
  `message_processing_status` int DEFAULT NULL COMMENT '是否处理 1 未处理  2 已处理',
  PRIMARY KEY (`message_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 ROW_FORMAT=DYNAMIC COMMENT='系统消息表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_base_system_platform_nav`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_base_system_platform_nav` (
  `nav_id` int NOT NULL AUTO_INCREMENT COMMENT '索引ID',
  `nav_title` varchar(100) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL COMMENT '导航标题',
  `nav_url` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL COMMENT '导航链接',
  `nav_new_open` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否以新窗口打开，0为否，1为是，默认为0',
  `nav_displayorder` tinyint unsigned NOT NULL DEFAULT '255' COMMENT '排序',
  `nav_active` tinyint(1) NOT NULL DEFAULT '1' COMMENT '是否启用 0 否  1是',
  `nav_property` tinyint(1) DEFAULT '0' COMMENT '1商城 2批发市场',
  PRIMARY KEY (`nav_id`) USING BTREE,
  KEY `nav_displayorder` (`nav_displayorder`) USING BTREE,
  KEY `nav_active` (`nav_active`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 ROW_FORMAT=DYNAMIC COMMENT='页面导航表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_base_system_web_config`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_base_system_web_config` (
  `config_key` varchar(50) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL COMMENT '数组下标',
  `config_value` text CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL COMMENT '数组值',
  `config_type` varchar(50) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL,
  `config_enable` tinyint(1) NOT NULL DEFAULT '1' COMMENT '状态值，1可能，0不可用',
  `config_comment` text CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL,
  `config_datatype` enum('string','json','number') CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL DEFAULT 'string' COMMENT '数据类型',
  PRIMARY KEY (`config_key`) USING BTREE,
  KEY `index` (`config_key`,`config_type`) USING BTREE COMMENT '(null)'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 ROW_FORMAT=DYNAMIC COMMENT='网站配置表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_base_tag_class`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_base_tag_class` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '会员标签等级关联表id',
  `member_id` varchar(18) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '会员id',
  `tag_id` varchar(50) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '会员标签id',
  `member_class_id` int DEFAULT NULL COMMENT '会员等级id',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 ROW_FORMAT=DYNAMIC COMMENT='会员标签等级关联表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_base_vk_set`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_base_vk_set` (
  `id` int unsigned NOT NULL AUTO_INCREMENT COMMENT 'id',
  `commission_config` tinyint(1) NOT NULL DEFAULT '0' COMMENT '0 无业绩归属   1推广人分佣  2分享人分佣  3推广+分享',
  `one_tg_rate` varchar(5) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL DEFAULT '0' COMMENT '一级推广佣金',
  `two_tg_rate` varchar(5) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL COMMENT '二级推广佣金',
  `fx_rate` varchar(5) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL DEFAULT '0' COMMENT '分享佣金',
  `background_image` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '分销中心背景图',
  `after_sales_days` int NOT NULL DEFAULT '30' COMMENT '售后天数',
  `vk_state` tinyint(1) NOT NULL DEFAULT '1' COMMENT '1开启 2关闭',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 ROW_FORMAT=DYNAMIC COMMENT='微客配置表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_base_web_config`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_base_web_config` (
  `config_key` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL COMMENT '配置键',
  `config_value` text CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL COMMENT '配置值',
  `config_type` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '类型',
  `config_enable` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL COMMENT '状态值，1可能，0不可用',
  `config_comment` text CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL COMMENT '名称解释',
  `config_datatype` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '数据类型',
  `config_remarks` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`config_key`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 ROW_FORMAT=DYNAMIC COMMENT='网站配置表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_base_wx_live_goods`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_base_wx_live_goods` (
  `id` int NOT NULL AUTO_INCREMENT,
  `common_id` int DEFAULT NULL COMMENT '商品id',
  `goods_id` int DEFAULT NULL COMMENT '规格id',
  `wx_goods_id` int DEFAULT NULL COMMENT '小程序审核后返回的goodsId',
  `shop_id` int DEFAULT NULL COMMENT '店铺id',
  `shop_name` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '店铺名',
  `common_name` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '商品名称',
  `goods_price` decimal(10,2) DEFAULT NULL COMMENT '商品价格',
  `common_stock` int DEFAULT NULL COMMENT '商品库存',
  `common_image` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '商品图片',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `operation` int DEFAULT '0' COMMENT '操作 0无 1添加 2删除',
  `isverify` int DEFAULT NULL COMMENT '审核状态 1 通过 2 审核中 3 不通过  4待平台审核  5待微信审核',
  `cause` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '审核原因',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 ROW_FORMAT=DYNAMIC COMMENT='微信直播商品表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_base_wx_live_host`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_base_wx_live_host` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '自增id',
  `user_id` varchar(18) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '主播用户id',
  `shop_id` int DEFAULT NULL COMMENT '主播对应商家id',
  `wechat_id` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '主播微信号',
  `head_portrait` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '头像',
  `creata_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `operation` int DEFAULT NULL COMMENT '操作 1 添加 2 删除 0无',
  `state` int DEFAULT NULL COMMENT '状态  1 通过  2 审核中 3 未通过 4 待平台审核 5 带微信审核 6 关闭',
  `cause` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '审核原因   ',
  `hoststate` int DEFAULT '2' COMMENT '是否停播 1 是 2否',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 ROW_FORMAT=DYNAMIC COMMENT='微信直播主播申请表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_base_wx_live_studio`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_base_wx_live_studio` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '自增id',
  `shop_id` int DEFAULT NULL COMMENT '店铺id',
  `name` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '房间名字',
  `coverImg` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL COMMENT '直播间背景图',
  `startTime` datetime NOT NULL COMMENT '直播开始时间',
  `endTime` datetime NOT NULL COMMENT '直播结束时间',
  `anchorName` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL COMMENT '主播昵称',
  `anchorWechat` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL COMMENT '主播微信号',
  `subAnchorWechat` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '主播副号微信号',
  `createrWechat` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '创建者微信号',
  `shareImg` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL COMMENT '直播间分享图',
  `feedsImg` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL COMMENT '购物直播频道封面图',
  `isFeedsPublic` int DEFAULT '1' COMMENT ' 是否开启官方收录，1 开启，0 关闭',
  `type` int NOT NULL COMMENT '直播类型，1 推流 0 手机直播',
  `closeLike` int NOT NULL COMMENT '是否关闭点赞 1 关闭  0 开启',
  `closeGoods` int NOT NULL COMMENT '是否关闭商品货架 1 关闭  0开启',
  `closeComment` int NOT NULL COMMENT '是否开启评论 1 关闭 0开启',
  `closeReplay` int unsigned DEFAULT '0' COMMENT '是否关闭回放 1 关闭 0开启',
  `closeShare` int DEFAULT '1' COMMENT '是否关闭分享 1关闭 0开启',
  `closeKf` int DEFAULT '0' COMMENT '是否关闭客服',
  `operation` int DEFAULT NULL COMMENT '0 无 1添加',
  `isverify` int DEFAULT NULL COMMENT '审核状态 1 通过 2 审核中 3 不通过  4 未开始 5 一开始\r\n6 已结束',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 ROW_FORMAT=DYNAMIC COMMENT='微信直播直播间申请表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_datav_base_district`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_datav_base_district` (
  `district_id` mediumint unsigned NOT NULL AUTO_INCREMENT COMMENT '地区id',
  `district_name` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL DEFAULT '' COMMENT '地区名称',
  `district_code` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '行政区划代码',
  `district_parent_id` mediumint unsigned NOT NULL DEFAULT '0' COMMENT '父id',
  `district_displayorder` smallint NOT NULL DEFAULT '0' COMMENT '排序',
  `district_region` varchar(50) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL DEFAULT '' COMMENT '区域名称 - 华北、东北、华东、华南、华中、西南、西北、港澳台、海外',
  `district_is_leaf` tinyint(1) NOT NULL DEFAULT '1' COMMENT '无子类',
  `district_is_level` tinyint(1) NOT NULL DEFAULT '1' COMMENT '等级',
  PRIMARY KEY (`district_id`) USING BTREE,
  KEY `upid` (`district_parent_id`,`district_displayorder`) USING BTREE COMMENT '(null)',
  KEY `district_id` (`district_id`) USING BTREE COMMENT 'null'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 ROW_FORMAT=DYNAMIC COMMENT='地区表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_datav_consume_deposit`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_datav_consume_deposit` (
  `deposit_buyer_id` varchar(30) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL DEFAULT '' COMMENT '用户号',
  `deposit_total_fee` decimal(12,2) NOT NULL COMMENT '交易金额',
  `deposit_gmt_create` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '交易创建时间',
  `deposit_type` int DEFAULT '1' COMMENT '1是余额 2 是充值卡 ',
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'id',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 ROW_FORMAT=DYNAMIC COMMENT='数据大屏充值表-支付回调callback使用-确认付款';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_datav_discount_base`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_datav_discount_base` (
  `discount_id` int unsigned NOT NULL AUTO_INCREMENT COMMENT '限时编号',
  `discount_name` varchar(50) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL COMMENT '活动名称',
  `discount_start_time` datetime NOT NULL COMMENT '活动开始时间',
  `discount_end_time` datetime NOT NULL COMMENT '活动结束时间',
  PRIMARY KEY (`discount_id`) USING BTREE,
  KEY `discount_id` (`discount_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 ROW_FORMAT=DYNAMIC COMMENT='数据大屏限时折扣活动表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_datav_goods_base`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_datav_goods_base` (
  `goods_id` int unsigned NOT NULL AUTO_INCREMENT COMMENT '商品id',
  `common_id` int unsigned NOT NULL DEFAULT '0' COMMENT '商品公共表id',
  PRIMARY KEY (`goods_id`) USING BTREE,
  UNIQUE KEY `goods_id` (`goods_id`) USING BTREE,
  KEY `common_id` (`common_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 ROW_FORMAT=DYNAMIC COMMENT='数据大屏商品表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_datav_goods_cat`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_datav_goods_cat` (
  `cat_id` int NOT NULL AUTO_INCREMENT,
  `cat_name` varchar(50) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL COMMENT ' 分类名称',
  `cat_parent_id` int NOT NULL DEFAULT '0' COMMENT '父类',
  PRIMARY KEY (`cat_id`) USING BTREE,
  KEY `cat_parent_id` (`cat_parent_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 ROW_FORMAT=DYNAMIC COMMENT='数据大屏商品分类表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_datav_goods_common`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_datav_goods_common` (
  `common_id` int NOT NULL AUTO_INCREMENT COMMENT '商品id',
  `common_name` varchar(500) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL DEFAULT '' COMMENT '商品名称',
  `shop_status` tinyint unsigned NOT NULL DEFAULT '3' COMMENT '店铺状态-3：开店成功 2:待审核付款 1:待审核资料  0:关闭',
  `common_salenum` int unsigned NOT NULL DEFAULT '0' COMMENT '商品销量',
  `common_image` varchar(500) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL DEFAULT '' COMMENT '商品主图',
  `common_state` int unsigned NOT NULL DEFAULT '0' COMMENT '商品状态 0下架，1正常，10违规（禁售）,2店铺下架商品下架,3商品删除',
  `common_verify` tinyint unsigned NOT NULL DEFAULT '1' COMMENT '商品审核 1通过，0未通过，10审核中',
  `common_add_time` datetime NOT NULL DEFAULT '2019-01-29 13:37:12' COMMENT '商品添加时间',
  `common_collect` int NOT NULL DEFAULT '0' COMMENT '商品收藏量',
  `common_evaluate` int NOT NULL DEFAULT '0' COMMENT '商品评论数',
  `common_is_screen` int DEFAULT '1' COMMENT '商品是否屏蔽：1-否；2-是',
  `integral_goods` int DEFAULT '1' COMMENT '积分商品 0是 1否',
  `shop_id` int DEFAULT NULL COMMENT '店铺id',
  PRIMARY KEY (`common_id`) USING BTREE,
  KEY `common_verify` (`common_verify`) USING BTREE,
  KEY `common_state` (`common_state`) USING BTREE,
  KEY `common_name` (`common_name`(255)) USING BTREE,
  KEY `shop_status` (`shop_status`) USING BTREE,
  KEY `common_id` (`common_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 ROW_FORMAT=DYNAMIC COMMENT='数据大屏商品sku表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_datav_log_action`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_datav_log_action` (
  `user_id` varchar(18) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '用户id',
  `user_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '用户昵称 不唯一',
  `log_time` timestamp NULL DEFAULT NULL COMMENT '记录时间',
  `log_ip` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT 'ip地址',
  PRIMARY KEY (`user_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci ROW_FORMAT=DYNAMIC COMMENT='数据大屏用户行为日志表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_datav_order_base`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_datav_order_base` (
  `order_id` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '订单号',
  `shop_id` int DEFAULT NULL COMMENT '卖家店铺id',
  `shop_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '卖家店铺名称',
  `pay_code` int DEFAULT NULL COMMENT '支付方式1-余额支付，2-支付宝支付，3-微信支付，4-通联支付,5-健康账户余额支付,6-购物卡支付，7-充值卡+支付宝，8-充值卡+微信,9-蜂币支付，10-蜂币+支付宝，11-蜂币+微信, 13-账期支付, 14-主管支付，16-paypal支付',
  `order_payment_amount` decimal(10,2) DEFAULT NULL COMMENT '应付金额（商品实际支付金额 + 运费）',
  `order_status` tinyint DEFAULT NULL COMMENT '订单状态 1.待付款 2.已付款 3.待发货 4.已发货 5.已签收 6.确认收货 7.取消 8.退款中 9.已退款 11.待自提\\r\\n12 虚拟订单已过期',
  `buyer_user_id` varchar(18) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '买家id',
  `buyer_user_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '买家姓名',
  `order_receiver_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '收货人的姓名',
  `order_receiver_address` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '收货人的详细地址',
  `order_receiver_contact` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '收货人的联系方式',
  `order_from` tinyint DEFAULT '1' COMMENT '手机端1PC 2wap微信浏览器 3安卓 5ios 6微信小程序 7wap浏览器',
  `integral_goods` int DEFAULT '1' COMMENT '积分商品订单 0是 1否',
  `order_return_status` tinyint(1) DEFAULT '0' COMMENT '退货状态:0是无退货,1是退货中,2是退货完成，3退货不通过',
  `order_refund_status` tinyint(1) DEFAULT '0' COMMENT '退款状态:0是无退款,1是退款中,2是退款完成,3退款不通过',
  `order_receiver_province_id` int DEFAULT '0' COMMENT '收货人收货地址区域ID',
  `order_promotion_id` tinyint(1) DEFAULT '1' COMMENT '活动ID 0 普通点单 1 砍价（废弃） 2 秒杀 3 限时折扣 4拼团 5 满减增折',
  `order_create_time` datetime DEFAULT NULL COMMENT '订单生成时间',
  `order_discount_fee` decimal(10,2) DEFAULT NULL COMMENT '优惠价格（商品金额-支付金额）',
  PRIMARY KEY (`order_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci ROW_FORMAT=DYNAMIC COMMENT='数据大屏统计订单信息表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_datav_order_cancel_reason`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_datav_order_cancel_reason` (
  `cancel_reason_id` int NOT NULL,
  `cancel_reason_content` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '取消订单的原因',
  PRIMARY KEY (`cancel_reason_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci ROW_FORMAT=DYNAMIC COMMENT='数据大屏订单取消原因表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_datav_order_goods`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_datav_order_goods` (
  `order_id` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '订单id',
  `goods_id` int DEFAULT NULL COMMENT '商品id',
  `common_id` int DEFAULT NULL COMMENT '商品common_id',
  `shop_id` int DEFAULT NULL COMMENT '店铺ID',
  `shop_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '店铺名',
  `goods_class_id` int DEFAULT NULL COMMENT '商品对应的类目ID',
  `order_goods_num` smallint DEFAULT NULL COMMENT '商品数量',
  `goods_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '商品名称',
  `goods_return_status` tinyint(1) DEFAULT '0' COMMENT '退货状态:0是无退货,1是退货中,2是退货完成 3商家拒绝退货',
  `goods_refund_status` tinyint(1) DEFAULT '0' COMMENT '退款状态：0：无退款 1：退款中 2：退款完成  3：商家拒绝退款',
  PRIMARY KEY (`order_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci ROW_FORMAT=DYNAMIC COMMENT='数据大屏统计-订单商品表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_datav_order_return`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_datav_order_return` (
  `order_id` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '订单id',
  `return_reason_id` int DEFAULT NULL COMMENT '退款理由id',
  `return_cash` decimal(8,2) DEFAULT NULL COMMENT '退款金额',
  `return_add_time` datetime DEFAULT NULL COMMENT '添加时间',
  PRIMARY KEY (`order_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci ROW_FORMAT=DYNAMIC COMMENT='数据大屏售后记录表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_datav_piecing_together`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_datav_piecing_together` (
  `pintuan_id` int NOT NULL AUTO_INCREMENT COMMENT '主键，自增长',
  `shop_id` int NOT NULL COMMENT '店铺ID',
  `user_id` varchar(18) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL DEFAULT '0' COMMENT '商家ID',
  `name` varchar(50) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL DEFAULT '' COMMENT '拼团活动名称',
  `start_time` datetime NOT NULL DEFAULT '2019-04-22 14:10:07' COMMENT '开团时间',
  `end_time` datetime NOT NULL DEFAULT '2019-04-22 14:10:07' COMMENT '结束时间',
  PRIMARY KEY (`pintuan_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 ROW_FORMAT=DYNAMIC COMMENT='数据大屏-拼团主表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_datav_reduction_policy_base`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_datav_reduction_policy_base` (
  `promotion_id` int NOT NULL AUTO_INCREMENT COMMENT '活动ID',
  `promotion_name` varchar(200) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL COMMENT '活动名称',
  `start_time` datetime NOT NULL COMMENT '活动开始时间',
  `end_time` datetime NOT NULL COMMENT '活动结束时间',
  PRIMARY KEY (`promotion_id`) USING BTREE,
  UNIQUE KEY `activity_reduction_policy_base_promotion_id_uindex` (`promotion_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 ROW_FORMAT=DYNAMIC COMMENT='满减赠折基本信息表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_datav_search_word`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_datav_search_word` (
  `search_id` int NOT NULL AUTO_INCREMENT,
  `search_keyword` varchar(200) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT 'key值',
  `search_char_index` varchar(200) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '下标',
  `search_nums` int DEFAULT '0' COMMENT '数量',
  PRIMARY KEY (`search_id`) USING BTREE,
  KEY `search_keyword` (`search_keyword`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 ROW_FORMAT=DYNAMIC COMMENT='数据大屏搜索热门词';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_datav_seckill_base`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_datav_seckill_base` (
  `seckill_id` int unsigned NOT NULL AUTO_INCREMENT COMMENT '限时编号',
  `seckill_name` varchar(50) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL COMMENT '活动名称',
  `seckill_start_time` datetime NOT NULL COMMENT '活动开始时间',
  `seckill_end_time` datetime NOT NULL COMMENT '活动结束时间',
  PRIMARY KEY (`seckill_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 ROW_FORMAT=DYNAMIC COMMENT='数据大屏限时秒杀活动表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_datav_shop_base`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_datav_shop_base` (
  `shop_id` int unsigned NOT NULL AUTO_INCREMENT,
  `shop_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '店铺名称',
  `shop_create_time` datetime DEFAULT NULL COMMENT '开店时间',
  `shop_logo` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT '' COMMENT '店铺logo',
  `shop_status` int DEFAULT '0' COMMENT '店铺状态-3：开店成功 2:待审核付款 1:待审核资料  0:关闭',
  `shop_type` tinyint DEFAULT '1' COMMENT '店铺类型: 1-卖家店铺; 2:供应商店铺',
  `user_id` varchar(18) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '用户id',
  PRIMARY KEY (`shop_id`) USING BTREE,
  KEY `shop_status` (`shop_status`) USING BTREE,
  KEY `shop_id` (`shop_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 ROW_FORMAT=DYNAMIC COMMENT='数据大屏店铺表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_datav_shop_join_base`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_datav_shop_join_base` (
  `join_id` varchar(50) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL,
  `shop_province` int DEFAULT '0' COMMENT '店铺所在地-省',
  `user_id` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '用户ID',
  `audit_status` int DEFAULT '0' COMMENT '审核状态:0-待审核；1-审核通过；2-审核不通过',
  PRIMARY KEY (`join_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 ROW_FORMAT=DYNAMIC COMMENT='数据大屏店铺入驻信息表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_datav_shop_join_pay`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_datav_shop_join_pay` (
  `pay_id` int NOT NULL AUTO_INCREMENT,
  `user_id` varchar(50) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '用户ID',
  `audit_status` int DEFAULT '0' COMMENT '审核状态:0-待商家上传付款凭证;1-待审核付款凭证;2-审核不通过；3-审核通过',
  PRIMARY KEY (`pay_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 ROW_FORMAT=DYNAMIC COMMENT='数据大屏-店铺租金表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_datav_user_access`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_datav_user_access` (
  `id` int NOT NULL AUTO_INCREMENT,
  `user_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '用户id',
  `user_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '用户名',
  `goods_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '商品id',
  `add_time` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '添加时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci ROW_FORMAT=DYNAMIC COMMENT='数据大屏用户访问表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_datav_user_cart`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_datav_user_cart` (
  `cart_id` int NOT NULL AUTO_INCREMENT COMMENT 'id',
  `common_id` int NOT NULL COMMENT '商品Id',
  `insert_cart_time` datetime NOT NULL COMMENT '插入购物车时间',
  PRIMARY KEY (`cart_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 ROW_FORMAT=DYNAMIC COMMENT='数据大屏购物车表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_datav_user_footprint`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_datav_user_footprint` (
  `footprint_id` int NOT NULL AUTO_INCREMENT,
  `common_id` int NOT NULL COMMENT '商品id',
  `footprint_time` varchar(32) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL DEFAULT '2018-09-05 --' COMMENT '时间',
  PRIMARY KEY (`footprint_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 ROW_FORMAT=DYNAMIC COMMENT='数据大屏商品访问足迹表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_datav_user_info`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_datav_user_info` (
  `user_id` varchar(18) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '用户id',
  `user_integral` int DEFAULT '0' COMMENT '用户积分',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `user_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '用户昵称 不唯一',
  PRIMARY KEY (`user_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci ROW_FORMAT=DYNAMIC COMMENT='数据大屏用户表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_datav_user_info_detail`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_datav_user_info_detail` (
  `user_id` varchar(18) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '用户id',
  `user_avatar` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci COMMENT '头像地址',
  `user_platform` int DEFAULT NULL COMMENT '注册用户来源 1.pc;2.h5;3.小程序; 4:app ',
  `user_provinceid` int DEFAULT '0' COMMENT '省份id',
  PRIMARY KEY (`user_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci ROW_FORMAT=DYNAMIC COMMENT='数据大屏用户详细信息表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_datav_user_resource`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_datav_user_resource` (
  `user_id` varchar(50) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL COMMENT '用户id',
  `user_money` decimal(16,2) unsigned NOT NULL DEFAULT '0.00' COMMENT '用户资金-预付款充值、转账、结算后的款项等等',
  `user_recharge_card` decimal(16,2) NOT NULL DEFAULT '0.00' COMMENT '充值卡余额-只能从来购物',
  PRIMARY KEY (`user_id`) USING BTREE,
  KEY `user_id` (`user_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 ROW_FORMAT=DYNAMIC COMMENT='数据大屏用户资源表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_decorate_page`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_decorate_page` (
  `id` int unsigned NOT NULL AUTO_INCREMENT COMMENT '主键',
  `type` tinyint unsigned NOT NULL DEFAULT '10' COMMENT '页面类型 1=商城首页, 2=个人中心, 3=客服设置 4-PC首页',
  `name` varchar(100) NOT NULL DEFAULT '' COMMENT '页面名称',
  `data` text COMMENT '页面数据',
  `meta` text COMMENT '页面设置',
  `create_time` int unsigned NOT NULL DEFAULT '0' COMMENT '创建时间',
  `update_time` int unsigned NOT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='装修页面配置表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_decorate_tabbar`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_decorate_tabbar` (
  `id` int unsigned NOT NULL AUTO_INCREMENT COMMENT '主键',
  `name` varchar(20) NOT NULL DEFAULT '' COMMENT '导航名称',
  `selected` varchar(200) NOT NULL DEFAULT '' COMMENT '未选图标',
  `unselected` varchar(200) NOT NULL DEFAULT '' COMMENT '已选图标',
  `link` varchar(200) DEFAULT NULL COMMENT '链接地址',
  `is_show` tinyint unsigned NOT NULL DEFAULT '1' COMMENT '显示状态',
  `create_time` int unsigned NOT NULL DEFAULT '0' COMMENT '创建时间',
  `update_time` int unsigned NOT NULL DEFAULT '0' COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='装修底部导航表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_doc_category`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_doc_category` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '分类ID',
  `org_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '组织ID',
  `parent_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '父分类ID',
  `name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '分类名称',
  `code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '分类编码',
  `sort` int DEFAULT '0' COMMENT '排序',
  `description` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '分类描述',
  `doc_count` int DEFAULT '0' COMMENT '文档数量',
  `status` tinyint DEFAULT '1' COMMENT '状态：0-禁用，1-启用',
  `create_time` int NOT NULL COMMENT '创建时间',
  `update_time` int NOT NULL COMMENT '更新时间',
  `create_by` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '创建人',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_org_code` (`org_id`,`code`),
  KEY `idx_org` (`org_id`),
  KEY `idx_parent` (`parent_id`),
  KEY `idx_code` (`code`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='文档分类表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_doc_collaborator`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_doc_collaborator` (
  `id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '协作ID',
  `org_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '组织ID',
  `document_id` int NOT NULL COMMENT '文档ID',
  `admin_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '管理员ID',
  `role` tinyint NOT NULL COMMENT '协作角色：0-所有者，1-编辑者，2-查看者',
  `status` tinyint DEFAULT '1' COMMENT '状态：0-禁用，1-启用',
  `create_time` int NOT NULL COMMENT '创建时间',
  `update_time` int NOT NULL COMMENT '更新时间',
  `create_by` int NOT NULL COMMENT '创建人',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_document_admin` (`document_id`,`admin_id`),
  KEY `idx_org` (`org_id`),
  KEY `idx_document` (`document_id`),
  KEY `idx_admin` (`admin_id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='文档协作表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_doc_comment`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_doc_comment` (
  `id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '评论ID',
  `org_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '组织ID',
  `document_id` int NOT NULL COMMENT '文档ID',
  `parent_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '父评论ID',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '评论内容',
  `admin_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '作者ID',
  `likes` int DEFAULT '0' COMMENT '点赞数',
  `status` tinyint DEFAULT '1' COMMENT '状态：0-禁用，1-启用',
  `created_time` datetime NOT NULL COMMENT '创建时间',
  `updated_time` datetime NOT NULL COMMENT '更新时间',
  `deleted_at` datetime DEFAULT NULL COMMENT '删除时间',
  PRIMARY KEY (`id`),
  KEY `idx_org` (`org_id`),
  KEY `idx_document` (`document_id`),
  KEY `idx_parent` (`parent_id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='文档评论表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_doc_document`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_doc_document` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '文档ID',
  `org_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '组织ID',
  `name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '文档名称',
  `file_type` int NOT NULL COMMENT '文档类型',
  `attachment_type` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '附件类型',
  `category_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '分类ID',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci COMMENT '文档内容',
  `status` tinyint NOT NULL DEFAULT '0' COMMENT '文档状态：0-草稿，1-已发布，2-已归档',
  `size` int DEFAULT '0' COMMENT '文档大小(字节)',
  `views` int DEFAULT '0' COMMENT '浏览次数',
  `likes` int DEFAULT '0' COMMENT '点赞数',
  `comments` int DEFAULT '0' COMMENT '评论数',
  `versions` int DEFAULT '1' COMMENT '版本数',
  `collaborators` int DEFAULT '1' COMMENT '协作者数',
  `admin_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '作者ID',
  `is_star` tinyint DEFAULT '0' COMMENT '是否收藏：0-否，1-是',
  `is_share` tinyint DEFAULT '0' COMMENT '是否分享：0-否，1-是',
  `create_time` int NOT NULL COMMENT '创建时间',
  `update_time` int NOT NULL COMMENT '更新时间',
  `delete_time` int DEFAULT NULL COMMENT '删除时间',
  PRIMARY KEY (`id`),
  KEY `idx_org` (`org_id`),
  KEY `idx_author` (`admin_id`),
  KEY `idx_status` (`status`),
  KEY `idx_type` (`file_type`),
  KEY `idx_category` (`category_id`),
  KEY `idx_star` (`is_star`),
  KEY `idx_share` (`is_share`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='文档主表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_doc_import`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_doc_import` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '导入ID',
  `org_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '组织ID',
  `document_id` int DEFAULT NULL COMMENT '文档ID',
  `file_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '文件名',
  `file_size` int NOT NULL COMMENT '文件大小',
  `file_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '文件类型',
  `file_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '文件URL',
  `status` tinyint NOT NULL DEFAULT '0' COMMENT '导入状态：0-待处理，1-处理中，2-已完成，3-失败',
  `error_message` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci COMMENT '错误信息',
  `create_time` int NOT NULL COMMENT '创建时间',
  `update_time` int NOT NULL COMMENT '更新时间',
  `create_by` int NOT NULL COMMENT '创建人',
  PRIMARY KEY (`id`),
  KEY `idx_org` (`org_id`),
  KEY `idx_status` (`status`),
  KEY `idx_created_by` (`create_by`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='文档导入记录表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_doc_permission`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_doc_permission` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '权限ID',
  `org_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '组织ID',
  `document_id` int NOT NULL COMMENT '文档ID',
  `admin_id` int NOT NULL COMMENT '管理员ID',
  `permission_type` tinyint NOT NULL COMMENT '权限类型：0-只读，1-可写，2-管理员',
  `status` tinyint DEFAULT '1' COMMENT '状态：0-禁用，1-启用',
  `create_time` int NOT NULL COMMENT '创建时间',
  `update_time` int NOT NULL COMMENT '更新时间',
  `create_by` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '创建人',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_document_admin` (`document_id`,`admin_id`),
  KEY `idx_org` (`org_id`),
  KEY `idx_document` (`document_id`),
  KEY `idx_admin` (`admin_id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='文档权限表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_doc_setting`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_doc_setting` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `org_id` int DEFAULT NULL COMMENT '所属公司',
  `name` varchar(100) NOT NULL COMMENT '名称',
  `code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '后缀代码',
  `type` int NOT NULL COMMENT '类型：1文档类型2附件类型3文档状态',
  `description` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '简介',
  `sort` int DEFAULT '0' COMMENT '排序',
  `status` tinyint DEFAULT '1' COMMENT '状态：0-禁用，1-正常',
  `create_time` int DEFAULT NULL COMMENT '创建时间',
  `update_time` int DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_type` (`type`),
  KEY `idx_code` (`code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='文档设置表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_doc_share`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_doc_share` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '分享ID',
  `org_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '组织ID',
  `document_id` int NOT NULL COMMENT '文档ID',
  `share_code` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '分享码',
  `password` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '访问密码',
  `expire_time` int DEFAULT NULL COMMENT '过期时间',
  `permissions` json DEFAULT NULL COMMENT '权限设置',
  `views` int DEFAULT '0' COMMENT '访问次数',
  `downloads` int DEFAULT '0' COMMENT '下载次数',
  `status` tinyint DEFAULT '1' COMMENT '状态：0-禁用，1-启用',
  `create_time` int NOT NULL COMMENT '创建时间',
  `create_by` int NOT NULL COMMENT '创建人',
  PRIMARY KEY (`id`),
  KEY `idx_org` (`org_id`),
  KEY `idx_document` (`document_id`),
  KEY `idx_share_code` (`share_code`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='文档分享表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_doc_star`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_doc_star` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '收藏ID',
  `org_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '组织ID',
  `document_id` int NOT NULL COMMENT '文档ID',
  `admin_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '管理员ID',
  `create_time` int NOT NULL COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_document_admin` (`document_id`,`admin_id`),
  KEY `idx_org` (`org_id`),
  KEY `idx_document` (`document_id`),
  KEY `idx_admin` (`admin_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='文档收藏表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_doc_tag`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_doc_tag` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '标签ID',
  `org_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '组织ID',
  `name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '标签名称',
  `color` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '标签颜色',
  `doc_count` int DEFAULT '0' COMMENT '文档数量',
  `sort` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '排序',
  `status` tinyint DEFAULT '1' COMMENT '状态：0-禁用，1-启用',
  `create_time` int NOT NULL COMMENT '创建时间',
  `update_time` int NOT NULL COMMENT '更新时间',
  `created_by` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '创建人',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_org_name` (`org_id`,`name`),
  KEY `idx_org` (`org_id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='文档标签表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_doc_tag_relation`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_doc_tag_relation` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `document_id` int NOT NULL COMMENT '文档ID',
  `tag_id` int NOT NULL COMMENT '标签ID',
  `status` int DEFAULT '0' COMMENT '状态：0-正常，1-禁用',
  `sort` int DEFAULT '0' COMMENT '排序',
  `create_time` int DEFAULT NULL COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_document_id` (`document_id`),
  KEY `idx_tag_id` (`tag_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='文档标签关联表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_doc_template`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_doc_template` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '模板ID',
  `org_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '组织ID',
  `name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '模板名称',
  `type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '模板类型',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci COMMENT '模板内容',
  `description` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '模板描述',
  `use_count` int DEFAULT '0' COMMENT '使用次数',
  `status` tinyint DEFAULT '1' COMMENT '状态：0-禁用，1-启用',
  `create_time` int NOT NULL COMMENT '创建时间',
  `update_time` int NOT NULL COMMENT '更新时间',
  `created_by` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '创建人',
  PRIMARY KEY (`id`),
  KEY `idx_org` (`org_id`),
  KEY `idx_type` (`type`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='文档模板表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_doc_version`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_doc_version` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '版本ID',
  `org_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '组织ID',
  `document_id` int NOT NULL COMMENT '文档ID',
  `version` int NOT NULL COMMENT '版本号',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci COMMENT '文档内容',
  `size` int DEFAULT '0' COMMENT '文档大小(字节)',
  `admin_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '作者ID',
  `remark` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '版本备注',
  `create_time` int NOT NULL COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_org` (`org_id`),
  KEY `idx_document` (`document_id`),
  KEY `idx_version` (`version`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='文档版本表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_geo_ab_experiment`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_geo_ab_experiment` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint unsigned NOT NULL DEFAULT '0',
  `project_id` bigint unsigned NOT NULL DEFAULT '0',
  `name` varchar(128) COLLATE utf8mb4_unicode_ci NOT NULL,
  `hypothesis` varchar(512) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `status` varchar(32) COLLATE utf8mb4_unicode_ci DEFAULT 'running',
  `start_date` date DEFAULT NULL,
  `end_date` date DEFAULT NULL,
  `winner_variant` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `result_json` json DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_tenant_status` (`tenant_id`,`status`),
  KEY `idx_tenant_project_status` (`tenant_id`,`project_id`,`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='A/B实验';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_geo_ai_balance`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_geo_ai_balance` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `subject_type` varchar(16) NOT NULL COMMENT 'tenant=登录用户 guest=访客',
  `subject_key` varchar(64) NOT NULL COMMENT 'tenant userId 或 guest id',
  `free_quota_tokens` bigint NOT NULL DEFAULT '100000' COMMENT '赠送 Token 额度',
  `free_used_tokens` bigint NOT NULL DEFAULT '0',
  `free_quota_started_at` datetime DEFAULT NULL,
  `free_quota_expires_at` datetime DEFAULT NULL,
  `used_tokens` bigint NOT NULL DEFAULT '0' COMMENT '已消耗 Token',
  `purchased_balance_tokens` bigint NOT NULL DEFAULT '0',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_subject` (`subject_type`,`subject_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='GEO AI 免费额度';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_geo_ai_discoverability`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_geo_ai_discoverability` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint unsigned NOT NULL DEFAULT '0',
  `project_id` bigint unsigned DEFAULT NULL,
  `page_url` varchar(512) COLLATE utf8mb4_unicode_ci NOT NULL,
  `score` int DEFAULT NULL,
  `gaps_json` json DEFAULT NULL COMMENT '多模态/结构化缺口',
  `multimodal_json` json DEFAULT NULL COMMENT 'alt/subtitle/transcript缺失',
  `checked_at` datetime DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_tenant_url` (`tenant_id`,`page_url`(191))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='AI可发现性检测(B3.2)';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_geo_ai_model`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_geo_ai_model` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `code` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '模型编码 chatgpt/deepseek/wenxin',
  `name` varchar(128) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '显示名称',
  `region` varchar(32) COLLATE utf8mb4_unicode_ci DEFAULT 'domestic' COMMENT 'domestic/global',
  `vendor` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '0禁用 1启用',
  `sort_order` int NOT NULL DEFAULT '0',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_code` (`code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='GEO监测AI引擎目录';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_geo_ai_model_changelog`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_geo_ai_model_changelog` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `ai_model_id` bigint unsigned NOT NULL,
  `change_date` date NOT NULL,
  `change_type` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `summary` varchar(512) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `impact_level` varchar(32) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `detail_json` json DEFAULT NULL,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_model_date` (`ai_model_id`,`change_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='AI模型变更日志';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_geo_ai_model_profile`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_geo_ai_model_profile` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint unsigned NOT NULL DEFAULT '0',
  `project_id` bigint unsigned DEFAULT NULL,
  `ai_model_id` bigint unsigned NOT NULL,
  `geo_score` int DEFAULT NULL COMMENT 'GEO适配分',
  `profile_json` json DEFAULT NULL COMMENT '能力画像28.3',
  `diagnosis_json` json DEFAULT NULL COMMENT '品牌诊断28.4',
  `refreshed_at` datetime DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_tenant_model` (`tenant_id`,`project_id`,`ai_model_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='模型GEO画像与诊断';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_geo_ai_snippet`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_geo_ai_snippet` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint unsigned NOT NULL DEFAULT '0',
  `project_id` bigint unsigned NOT NULL DEFAULT '0',
  `page_url` varchar(512) COLLATE utf8mb4_unicode_ci NOT NULL,
  `snippet_text` varchar(1024) COLLATE utf8mb4_unicode_ci NOT NULL,
  `cite_score` int DEFAULT NULL,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_tenant_url` (`tenant_id`,`page_url`(191)),
  KEY `idx_project_url` (`tenant_id`,`project_id`,`page_url`(191))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='可引用片段库';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_geo_ai_studio_expand_batch`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_geo_ai_studio_expand_batch` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '批次 ID',
  `tenant_id` bigint unsigned NOT NULL DEFAULT '0' COMMENT '租户 ID',
  `project_id` bigint unsigned DEFAULT NULL COMMENT '项目 ID',
  `seed` varchar(256) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '种子关键词',
  `mode` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'ai' COMMENT '扩展模式 code',
  `industry` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'geo' COMMENT '行业',
  `models_json` json DEFAULT NULL COMMENT 'AI 模型列表 JSON 数组',
  `source` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'dify' COMMENT 'dify|suffix|suffix_fallback|cached',
  `scene` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'expand' COMMENT 'load=页面加载 expand=手动扩展',
  `keyword_count` int NOT NULL DEFAULT '0' COMMENT '本批次关键词数',
  `dify_message` varchar(512) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'Dify 异常/提示摘要',
  `create_by` bigint unsigned DEFAULT NULL COMMENT '操作人',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `is_deleted` tinyint NOT NULL DEFAULT '0' COMMENT '0正常 1删除',
  PRIMARY KEY (`id`),
  KEY `idx_tenant_project_time` (`tenant_id`,`project_id`,`create_time`),
  KEY `idx_tenant_seed_mode` (`tenant_id`,`project_id`,`seed`(64),`mode`,`industry`,`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='AI创作·关键词扩展批次';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_geo_ai_studio_keyword`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_geo_ai_studio_keyword` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '明细 ID',
  `batch_id` bigint unsigned NOT NULL COMMENT '批次 ID',
  `tenant_id` bigint unsigned NOT NULL DEFAULT '0' COMMENT '租户 ID',
  `project_id` bigint unsigned DEFAULT NULL COMMENT '项目 ID',
  `keyword` varchar(256) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '关键词',
  `template` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '意图模板：指南型/对比型等',
  `opportunity` int DEFAULT NULL COMMENT '机会指数 60-95',
  `difficulty` int DEFAULT NULL COMMENT '难度 35-58',
  `trend` varchar(16) COLLATE utf8mb4_unicode_ci DEFAULT 'stable' COMMENT '趋势 up|stable|down',
  `covered` tinyint NOT NULL DEFAULT '0' COMMENT '是否已覆盖',
  `cover_status` varchar(16) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'gap' COMMENT 'gap|in_progress|covered',
  `sort_order` int NOT NULL DEFAULT '0' COMMENT '排序',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `is_deleted` tinyint NOT NULL DEFAULT '0' COMMENT '0正常 1删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_batch_keyword` (`batch_id`,`keyword`(191)),
  KEY `idx_batch_sort` (`batch_id`,`sort_order`),
  KEY `idx_tenant_keyword` (`tenant_id`,`project_id`,`keyword`(64))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='AI创作·关键词扩展明细';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_geo_aliyun_digital_human_resource`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_geo_aliyun_digital_human_resource` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint unsigned NOT NULL DEFAULT '0',
  `local_asset_id` bigint unsigned DEFAULT NULL,
  `provider` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'aliyun-avatar',
  `resource_type` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `external_resource_id` varchar(128) COLLATE utf8mb4_unicode_ci NOT NULL,
  `name` varchar(256) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `status` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'PENDING',
  `error_message` varchar(2000) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `metadata_json` json DEFAULT NULL,
  `synced_at` datetime DEFAULT NULL,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_tenant_provider_external_resource` (`tenant_id`,`provider`,`external_resource_id`),
  KEY `idx_tenant_resource_type_status` (`tenant_id`,`resource_type`,`status`),
  KEY `idx_local_asset` (`local_asset_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='阿里云灵眸数字人资源索引';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_geo_api_key`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_geo_api_key` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint unsigned NOT NULL DEFAULT '0',
  `project_id` bigint unsigned NOT NULL DEFAULT '0',
  `name` varchar(128) COLLATE utf8mb4_unicode_ci NOT NULL,
  `api_key_hash` varchar(128) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'Key哈希',
  `scopes_json` json DEFAULT NULL,
  `expire_at` datetime DEFAULT NULL,
  `last_used_at` datetime DEFAULT NULL,
  `status` varchar(32) COLLATE utf8mb4_unicode_ci DEFAULT 'active',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_tenant_status` (`tenant_id`,`status`),
  KEY `idx_tenant_project_status` (`tenant_id`,`project_id`,`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='开放API Key';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_geo_article`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_geo_article` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint unsigned NOT NULL DEFAULT '0',
  `project_id` bigint unsigned DEFAULT NULL,
  `org_id` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '组织ID，路由正文附表',
  `title` varchar(256) COLLATE utf8mb4_unicode_ci NOT NULL,
  `topic` varchar(256) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '关键词/主题',
  `content_type` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '文章/Q&A/视频',
  `studio_keyword_id` bigint unsigned DEFAULT NULL COMMENT 'AI创作关键词ID',
  `expand_batch_id` bigint unsigned DEFAULT NULL COMMENT '扩词批次ID',
  `meta_json` json DEFAULT NULL COMMENT 'AI生成元数据',
  `body` mediumtext COLLATE utf8mb4_unicode_ci,
  `status` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'draft' COMMENT 'draft/pending/published/failed',
  `adapt_status` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'none' COMMENT 'none/previewed/ready',
  `preview_batch_no` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '最近 Dify 预览批次',
  `settings_id` bigint unsigned DEFAULT NULL COMMENT '创作指令模板',
  `distribute_post_id` bigint unsigned DEFAULT NULL COMMENT '关联分发记录',
  `platforms_json` json DEFAULT NULL COMMENT '目标平台',
  `published_at` datetime DEFAULT NULL,
  `fail_reason` varchar(512) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `stats_json` json DEFAULT NULL,
  `create_by` bigint unsigned DEFAULT NULL,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_deleted` tinyint NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `idx_tenant_status` (`tenant_id`,`project_id`,`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='文章全生命周期(15.7)';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_geo_article_1`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_geo_article_1` (
  `article_id` bigint unsigned NOT NULL COMMENT '主表 hou_geo_article.id',
  `body` mediumtext COLLATE utf8mb4_unicode_ci,
  `platforms_json` json DEFAULT NULL,
  `fail_reason` varchar(512) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `stats_json` json DEFAULT NULL,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`article_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='文章正文附表(按组织分表)';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_geo_article_platform_publish`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_geo_article_platform_publish` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint unsigned NOT NULL,
  `project_id` bigint unsigned NOT NULL,
  `article_id` bigint unsigned NOT NULL,
  `platform` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `platform_account_id` bigint unsigned DEFAULT NULL,
  `status` varchar(24) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'pending',
  `published_at` datetime DEFAULT NULL,
  `publish_link` varchar(1024) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `external_post_id` varchar(256) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `omni_job_id` bigint unsigned DEFAULT NULL,
  `omni_job_item_id` bigint unsigned DEFAULT NULL,
  `error_message` varchar(2048) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `last_attempt_at` datetime DEFAULT NULL,
  `attempt_count` int NOT NULL DEFAULT '0',
  `create_by` bigint unsigned DEFAULT NULL,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_geo_article_platform_publish` (`tenant_id`,`project_id`,`article_id`,`platform`),
  KEY `idx_geo_article_platform_publish_article` (`tenant_id`,`project_id`,`article_id`),
  KEY `idx_geo_article_platform_publish_status` (`tenant_id`,`project_id`,`status`,`update_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='GEO鏂囩珷骞冲彴鍙戝竷鍙版查';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_geo_article_settings`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_geo_article_settings` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint unsigned NOT NULL DEFAULT '0',
  `project_id` bigint unsigned DEFAULT NULL,
  `name` varchar(128) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '指令模板名称',
  `is_default` tinyint NOT NULL DEFAULT '0',
  `tone` varchar(32) COLLATE utf8mb4_unicode_ci DEFAULT '严谨' COMMENT '语气',
  `structure` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT 'conclusion_first' COMMENT '文章结构',
  `word_count` int DEFAULT '1500' COMMENT '目标篇幅',
  `entity_inject_json` json DEFAULT NULL COMMENT '实体词注入',
  `geo_rules_json` json DEFAULT NULL COMMENT 'GEO检测规则',
  `default_channels_json` json DEFAULT NULL COMMENT '默认发布渠道',
  `knowledge_ids_json` json DEFAULT NULL COMMENT '关联知识库条目',
  `create_by` bigint unsigned DEFAULT NULL,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_deleted` tinyint NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `idx_tenant_default` (`tenant_id`,`project_id`,`is_default`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='AI创作指令/文章设置(15.6)';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_geo_attribution_record`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_geo_attribution_record` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint unsigned NOT NULL DEFAULT '0',
  `project_id` bigint unsigned DEFAULT NULL,
  `factor` varchar(128) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'Schema/内容/外链',
  `contribution_pct` decimal(6,2) DEFAULT NULL,
  `roi` decimal(8,2) DEFAULT NULL,
  `metrics` varchar(256) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `actions` varchar(512) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `stat_period` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_tenant_period` (`tenant_id`,`stat_period`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='效果归因';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_geo_authority_citation`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_geo_authority_citation` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint unsigned NOT NULL DEFAULT '0',
  `project_id` bigint unsigned DEFAULT NULL,
  `page_url` varchar(512) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `citation_type` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `scan_result_json` json DEFAULT NULL,
  `scan_at` datetime DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_tenant_scan` (`tenant_id`,`scan_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='权威引用扫描';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_geo_backlink`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_geo_backlink` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint unsigned NOT NULL DEFAULT '0',
  `project_id` bigint unsigned DEFAULT NULL,
  `domain` varchar(256) COLLATE utf8mb4_unicode_ci NOT NULL,
  `target_url` varchar(512) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `link_count` int DEFAULT '0',
  `quality_score` int DEFAULT NULL,
  `link_type` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `change_type` varchar(32) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'new/lost',
  `stat_date` date DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_tenant_domain` (`tenant_id`,`domain`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='反向链接';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_geo_brand_health_snapshot`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_geo_brand_health_snapshot` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint unsigned NOT NULL DEFAULT '0',
  `project_id` bigint unsigned DEFAULT NULL,
  `health_score` int DEFAULT NULL,
  `dimensions_json` json DEFAULT NULL COMMENT '能见度/引用/情感/技术',
  `issues_json` json DEFAULT NULL,
  `stat_date` date NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_tenant_date` (`tenant_id`,`project_id`,`stat_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='品牌健康度快照(模块8)';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_geo_brand_product`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_geo_brand_product` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint unsigned NOT NULL,
  `project_id` bigint unsigned NOT NULL,
  `product_name` varchar(128) COLLATE utf8mb4_unicode_ci NOT NULL,
  `normalized_name` varchar(128) COLLATE utf8mb4_unicode_ci NOT NULL,
  `description` text COLLATE utf8mb4_unicode_ci,
  `source_type` varchar(16) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'manual' COMMENT 'manual/ai',
  `source_url` varchar(512) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_deleted` tinyint NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_geo_brand_product_name` (`tenant_id`,`project_id`,`normalized_name`,`is_deleted`),
  KEY `idx_geo_brand_product_project` (`tenant_id`,`project_id`,`is_deleted`,`update_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='GEO品牌产品系列';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_geo_brand_variant`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_geo_brand_variant` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint unsigned NOT NULL DEFAULT '0',
  `project_id` bigint unsigned DEFAULT NULL,
  `variant_text` varchar(128) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '品牌变体/别名',
  `normalized_variant` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '去重用规范化别名',
  `variant_type` varchar(32) COLLATE utf8mb4_unicode_ci DEFAULT 'alias' COMMENT 'alias/abbrev/misspell',
  `create_by` bigint unsigned DEFAULT NULL,
  `update_by` bigint unsigned DEFAULT NULL,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_deleted` tinyint NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_tenant_project_type_variant` (`tenant_id`,`project_id`,`variant_type`,`normalized_variant`),
  KEY `idx_tenant_project` (`tenant_id`,`project_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='品牌变体词典';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_geo_china_map_poi`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_geo_china_map_poi` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint unsigned NOT NULL DEFAULT '0',
  `project_id` bigint unsigned DEFAULT NULL,
  `store_id` bigint unsigned DEFAULT NULL,
  `binding_id` bigint unsigned DEFAULT NULL,
  `platform` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'baidu/amap',
  `poi_id` varchar(128) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `poi_name` varchar(256) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `claim_status` varchar(32) COLLATE utf8mb4_unicode_ci DEFAULT 'unclaimed' COMMENT 'unclaimed/claimed/pending',
  `map_rank` int DEFAULT NULL,
  `ai_cited` tinyint DEFAULT '0' COMMENT '是否被本地AI摘要引用',
  `sync_at` datetime DEFAULT NULL,
  `address` varchar(512) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `phone` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `website` varchar(512) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `category` varchar(128) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `business_hours_json` text COLLATE utf8mb4_unicode_ci,
  `source_type` varchar(16) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'import',
  `raw_json` longtext COLLATE utf8mb4_unicode_ci,
  `external_updated_at` datetime DEFAULT NULL,
  `sync_job_id` bigint unsigned DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_store_platform` (`store_id`,`platform`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='百度/高德POI(29.3)';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_geo_citation`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_geo_citation` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint unsigned NOT NULL DEFAULT '0',
  `project_id` bigint unsigned DEFAULT NULL,
  `snippet` varchar(1024) COLLATE utf8mb4_unicode_ci NOT NULL,
  `source_url` varchar(512) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `domain` varchar(256) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `platform` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'AI模型',
  `quality` varchar(32) COLLATE utf8mb4_unicode_ci DEFAULT 'core' COMMENT 'core/edge/noise',
  `cite_count` int DEFAULT '1',
  `first_seen_at` datetime DEFAULT NULL,
  `last_seen_at` datetime DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_tenant_domain` (`tenant_id`,`domain`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='AI引用片段';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_geo_citation_domain_stat`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_geo_citation_domain_stat` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint unsigned NOT NULL DEFAULT '0',
  `project_id` bigint unsigned DEFAULT NULL,
  `domain` varchar(256) COLLATE utf8mb4_unicode_ci NOT NULL,
  `domain_type` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `cite_count` int DEFAULT '0',
  `ratio` decimal(6,2) DEFAULT NULL,
  `stat_date` date DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_tenant_date` (`tenant_id`,`stat_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='引用域名统计';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_geo_commerce_order`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_geo_commerce_order` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint unsigned NOT NULL DEFAULT '0',
  `product_id` bigint unsigned DEFAULT NULL,
  `order_no` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `amount` decimal(10,2) NOT NULL,
  `pay_status` varchar(32) COLLATE utf8mb4_unicode_ci DEFAULT 'pending' COMMENT 'pending/paid/refunded',
  `pay_time` datetime DEFAULT NULL,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_order_no` (`order_no`),
  KEY `idx_tenant_pay` (`tenant_id`,`pay_status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='SaaS订单(30.3)';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_geo_commerce_product`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_geo_commerce_product` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint unsigned NOT NULL DEFAULT '0',
  `name` varchar(128) COLLATE utf8mb4_unicode_ci NOT NULL,
  `sku_code` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `quota_json` json DEFAULT NULL COMMENT '蒸馏词/创作/投放额度',
  `price` decimal(10,2) DEFAULT NULL,
  `status` varchar(32) COLLATE utf8mb4_unicode_ci DEFAULT 'active',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_tenant_status` (`tenant_id`,`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='SaaS商品套餐(30.2)';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_geo_competitor`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_geo_competitor` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint unsigned NOT NULL DEFAULT '0',
  `project_id` bigint unsigned DEFAULT NULL,
  `brand_name` varchar(128) COLLATE utf8mb4_unicode_ci NOT NULL,
  `domain` varchar(256) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `ai_model_ids` json DEFAULT NULL,
  `selling_points` text COLLATE utf8mb4_unicode_ci COMMENT '主要竞争点',
  `aliases_json` json DEFAULT NULL COMMENT '相似名称和别名',
  `source_type` varchar(16) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'manual' COMMENT 'manual/ai',
  `source_url` varchar(512) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '识别来源网址',
  `is_self` tinyint NOT NULL DEFAULT '0',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_deleted` tinyint NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `idx_tenant_project` (`tenant_id`,`project_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='竞品品牌';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_geo_competitor_metric`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_geo_competitor_metric` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint unsigned NOT NULL DEFAULT '0',
  `project_id` bigint unsigned DEFAULT NULL,
  `competitor_id` bigint unsigned NOT NULL,
  `metric_key` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'SOV/avg_rank/citations',
  `metric_value` decimal(12,4) DEFAULT NULL,
  `stat_date` date DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_competitor_metric` (`competitor_id`,`metric_key`,`stat_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='竞品指标';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_geo_content_asset`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_geo_content_asset` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint unsigned NOT NULL DEFAULT '0',
  `project_id` bigint unsigned DEFAULT NULL,
  `name` varchar(256) COLLATE utf8mb4_unicode_ci NOT NULL,
  `asset_type` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'article/image/video/audio',
  `tags_json` json DEFAULT NULL,
  `file_url` varchar(512) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `freshness_score` int DEFAULT NULL,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `is_deleted` tinyint NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `idx_tenant_type` (`tenant_id`,`asset_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='内容素材库';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_geo_content_asset_version`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_geo_content_asset_version` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `asset_id` bigint unsigned NOT NULL,
  `version_label` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `article_body` mediumtext COLLATE utf8mb4_unicode_ci,
  `schema_json` json DEFAULT NULL,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_asset` (`asset_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='素材版本';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_geo_content_generate_job`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_geo_content_generate_job` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint unsigned NOT NULL DEFAULT '0',
  `project_id` bigint unsigned DEFAULT NULL,
  `job_no` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'genjob_xxx',
  `status` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'processing' COMMENT 'processing/done/partial_failed/failed',
  `total_count` int NOT NULL DEFAULT '0',
  `success_count` int NOT NULL DEFAULT '0',
  `failed_count` int NOT NULL DEFAULT '0',
  `expand_batch_id` bigint unsigned DEFAULT NULL,
  `config_json` json DEFAULT NULL COMMENT 'model/type/mode/industry/videoConfig',
  `operator_id` bigint unsigned DEFAULT NULL,
  `error_msg` varchar(512) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `finished_at` datetime DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_job_no` (`job_no`),
  KEY `idx_tenant_status` (`tenant_id`,`project_id`,`status`,`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='AI创作批量生成任务';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_geo_content_generate_job_item`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_geo_content_generate_job_item` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `job_id` bigint unsigned NOT NULL,
  `sort_order` int NOT NULL DEFAULT '0',
  `keyword` varchar(256) COLLATE utf8mb4_unicode_ci NOT NULL,
  `template` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `studio_keyword_id` bigint unsigned DEFAULT NULL,
  `status` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'pending' COMMENT 'pending/processing/success/failed',
  `article_id` bigint unsigned DEFAULT NULL COMMENT '关联文章 hou_geo_article.id',
  `error_msg` varchar(512) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `started_at` datetime DEFAULT NULL,
  `finished_at` datetime DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_job_status` (`job_id`,`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='AI创作批量生成子项';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_geo_content_rewrite`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_geo_content_rewrite` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `draft_id` bigint unsigned NOT NULL,
  `platform` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'xhs/toutiao/douyin等',
  `similarity_pct` int DEFAULT NULL,
  `body` mediumtext COLLATE utf8mb4_unicode_ci,
  `status` varchar(32) COLLATE utf8mb4_unicode_ci DEFAULT 'done',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_draft_platform` (`draft_id`,`platform`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='分平台改写版本';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_geo_conversion_event`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_geo_conversion_event` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint NOT NULL,
  `project_id` bigint NOT NULL,
  `goal_id` bigint NOT NULL,
  `distill_task_id` bigint DEFAULT NULL,
  `distill_result_id` bigint DEFAULT NULL,
  `content_type` varchar(40) DEFAULT NULL,
  `content_id` bigint DEFAULT NULL,
  `touchpoint_id` bigint DEFAULT NULL,
  `event_code` varchar(80) NOT NULL,
  `event_value` decimal(18,4) DEFAULT NULL,
  `currency` varchar(12) DEFAULT NULL,
  `source_type` varchar(24) NOT NULL COMMENT 'sdk/webhook/csv/admin',
  `occurred_at` datetime NOT NULL,
  `page_url` varchar(1000) DEFAULT NULL,
  `referrer_url` varchar(1000) DEFAULT NULL,
  `utm_source` varchar(120) DEFAULT NULL,
  `utm_medium` varchar(120) DEFAULT NULL,
  `utm_campaign` varchar(200) DEFAULT NULL,
  `utm_content` varchar(200) DEFAULT NULL,
  `utm_term` varchar(200) DEFAULT NULL,
  `session_id` varchar(100) DEFAULT NULL,
  `visitor_id` varchar(100) DEFAULT NULL,
  `contact_name_encrypted` text,
  `contact_phone_encrypted` text,
  `contact_email_encrypted` text,
  `contact_phone_hash` char(64) DEFAULT NULL,
  `contact_email_hash` char(64) DEFAULT NULL,
  `idempotency_key` varchar(120) NOT NULL,
  `external_event_id` varchar(120) DEFAULT NULL,
  `import_batch_id` bigint DEFAULT NULL,
  `properties_json` text,
  `create_time` datetime NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_event_idempotency` (`tenant_id`,`project_id`,`source_type`,`idempotency_key`),
  KEY `idx_event_goal_time` (`tenant_id`,`project_id`,`goal_id`,`occurred_at`),
  KEY `idx_event_task_time` (`tenant_id`,`project_id`,`distill_task_id`,`occurred_at`),
  KEY `idx_event_contact_phone` (`tenant_id`,`project_id`,`contact_phone_hash`),
  KEY `idx_event_contact_email` (`tenant_id`,`project_id`,`contact_email_hash`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='真实转化事件账本';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_geo_conversion_goal`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_geo_conversion_goal` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint unsigned NOT NULL DEFAULT '0',
  `project_id` bigint unsigned DEFAULT NULL,
  `goal_type` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'exposure/indexing/content/distribute/lead',
  `name` varchar(128) COLLATE utf8mb4_unicode_ci NOT NULL,
  `event_code` varchar(80) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '项目内稳定事件代码',
  `target_url` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '目标页面或匹配规则',
  `event_config_json` text COLLATE utf8mb4_unicode_ci COMMENT '事件扩展配置',
  `target_value` decimal(12,4) NOT NULL,
  `current_value` decimal(12,4) DEFAULT '0.0000',
  `unit` varchar(32) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '%/篇/次',
  `deadline` date DEFAULT NULL,
  `status` varchar(32) COLLATE utf8mb4_unicode_ci DEFAULT 'active',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_tenant_type` (`tenant_id`,`project_id`,`goal_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='转化目标(30.1)';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_geo_conversion_import_batch`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_geo_conversion_import_batch` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint NOT NULL,
  `project_id` bigint NOT NULL,
  `file_name` varchar(255) NOT NULL,
  `source_type` varchar(24) NOT NULL,
  `total_rows` int NOT NULL DEFAULT '0',
  `success_rows` int NOT NULL DEFAULT '0',
  `failed_rows` int NOT NULL DEFAULT '0',
  `error_json` text,
  `status` varchar(24) NOT NULL,
  `create_by` bigint DEFAULT NULL,
  `started_at` datetime NOT NULL,
  `finished_at` datetime DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_import_project_time` (`tenant_id`,`project_id`,`started_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='转化事件 CSV 导入批次';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_geo_conversion_touchpoint`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_geo_conversion_touchpoint` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint NOT NULL,
  `project_id` bigint NOT NULL,
  `session_id` varchar(100) DEFAULT NULL,
  `visitor_id` varchar(100) DEFAULT NULL,
  `distill_task_id` bigint DEFAULT NULL,
  `distill_result_id` bigint DEFAULT NULL,
  `content_type` varchar(40) DEFAULT NULL,
  `content_id` bigint DEFAULT NULL,
  `page_url` varchar(1000) DEFAULT NULL,
  `referrer_url` varchar(1000) DEFAULT NULL,
  `utm_source` varchar(120) DEFAULT NULL,
  `utm_medium` varchar(120) DEFAULT NULL,
  `utm_campaign` varchar(200) DEFAULT NULL,
  `utm_content` varchar(200) DEFAULT NULL,
  `utm_term` varchar(200) DEFAULT NULL,
  `occurred_at` datetime NOT NULL,
  `create_time` datetime NOT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_touch_session` (`tenant_id`,`project_id`,`session_id`,`occurred_at`),
  KEY `idx_touch_content` (`tenant_id`,`project_id`,`content_type`,`content_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='转化归因访问触点';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_geo_crawl_audit`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_geo_crawl_audit` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint unsigned NOT NULL DEFAULT '0',
  `project_id` bigint unsigned NOT NULL DEFAULT '0',
  `page_url` varchar(512) COLLATE utf8mb4_unicode_ci NOT NULL,
  `indexable` tinyint DEFAULT NULL,
  `reason` varchar(256) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `robots_ok` tinyint DEFAULT NULL,
  `audit_type` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'indexability/robots/budget',
  `user_agent` varchar(128) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'Googlebot' COMMENT 'crawler used for audit',
  `audit_at` datetime DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_tenant_url` (`tenant_id`,`page_url`(191)),
  KEY `idx_project_audit` (`tenant_id`,`project_id`,`audit_type`,`audit_at`),
  KEY `idx_project_url_crawler_audit` (`tenant_id`,`project_id`,`audit_type`,`page_url`(120),`user_agent`,`audit_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='爬虫可索引审计';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_geo_crawler_policy`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_geo_crawler_policy` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `platform_or_bot` varchar(128) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'GPTBot/BaiduSpider等',
  `policy_type` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `summary` varchar(512) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `detail_json` json DEFAULT NULL,
  `impact_level` varchar(32) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '高/中/低',
  `effective_date` date DEFAULT NULL,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_bot_date` (`platform_or_bot`,`effective_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='爬虫政策情报(F1)';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_geo_distill_goal_relation`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_geo_distill_goal_relation` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint NOT NULL,
  `project_id` bigint NOT NULL,
  `distill_task_id` bigint NOT NULL,
  `goal_id` bigint NOT NULL,
  `role` varchar(16) NOT NULL COMMENT 'primary/secondary',
  `create_by` bigint DEFAULT NULL,
  `create_time` datetime NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_distill_goal` (`tenant_id`,`project_id`,`distill_task_id`,`goal_id`),
  KEY `idx_distill_role` (`tenant_id`,`project_id`,`distill_task_id`,`role`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='蒸馏任务主辅转化目标关系';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_geo_distill_result`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_geo_distill_result` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint unsigned NOT NULL DEFAULT '0',
  `project_id` bigint unsigned DEFAULT NULL,
  `task_id` bigint unsigned NOT NULL,
  `keyword` varchar(256) COLLATE utf8mb4_unicode_ci NOT NULL,
  `heat` int DEFAULT NULL,
  `opportunity` int DEFAULT NULL,
  `merged_to_library` tinyint NOT NULL DEFAULT '0' COMMENT '是否已入训练词库',
  `used_in_creation` tinyint NOT NULL DEFAULT '0' COMMENT '是否已转创作',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_task` (`task_id`),
  KEY `idx_tenant_keyword` (`tenant_id`,`keyword`(64))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='蒸馏结果归档(A1.8)';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_geo_distill_task`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_geo_distill_task` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint unsigned NOT NULL DEFAULT '0',
  `project_id` bigint unsigned DEFAULT NULL,
  `task_name` varchar(128) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `seed_json` json DEFAULT NULL COMMENT '种子词/竞品/行业语料',
  `source_type` varchar(32) COLLATE utf8mb4_unicode_ci DEFAULT 'seed' COMMENT 'seed/competitor/industry',
  `strategy` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT '均衡策略' COMMENT '蒸馏策略',
  `target_count` int DEFAULT '500' COMMENT '目标词数',
  `status` varchar(32) COLLATE utf8mb4_unicode_ci DEFAULT 'pending' COMMENT 'pending/running/done/failed',
  `result_count` int DEFAULT '0',
  `quality_score` int DEFAULT NULL COMMENT '质量分',
  `create_by` bigint unsigned DEFAULT NULL,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `finished_at` datetime DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_tenant_status` (`tenant_id`,`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='关键词蒸馏任务(A1.7)';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_geo_distribute_geo_check`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_geo_distribute_geo_check` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `post_id` bigint unsigned NOT NULL,
  `check_score` int DEFAULT NULL,
  `blocked` tinyint NOT NULL DEFAULT '0',
  `checks_json` json DEFAULT NULL COMMENT '检测项明细',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_post_time` (`post_id`,`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='分发GEO检测结果';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_geo_distribute_post`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_geo_distribute_post` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint unsigned NOT NULL DEFAULT '0',
  `project_id` bigint unsigned DEFAULT NULL,
  `platform` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'xhs/toutiao/douyin/bjh',
  `platform_account_id` bigint unsigned DEFAULT NULL,
  `content_type` varchar(32) COLLATE utf8mb4_unicode_ci DEFAULT '图文' COMMENT '图文/视频/微头条',
  `title` varchar(256) COLLATE utf8mb4_unicode_ci NOT NULL,
  `conclusion` varchar(512) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '首段结论',
  `body` mediumtext COLLATE utf8mb4_unicode_ci COMMENT '正文',
  `tags` varchar(512) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '话题标签',
  `entities` varchar(512) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '实体词',
  `site_url` varchar(512) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `cover_url` varchar(512) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `media_url` varchar(1024) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '实际发布的视频或音频地址',
  `video_meta_json` text COLLATE utf8mb4_unicode_ci COMMENT '视频生成与发布元数据',
  `status` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'draft' COMMENT 'draft/pending/published/failed',
  `plan_time` datetime DEFAULT NULL COMMENT '定时发布',
  `published_at` datetime DEFAULT NULL,
  `publish_link` varchar(512) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `fail_reason` varchar(512) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `stats_json` json DEFAULT NULL COMMENT '阅读/赞/评论',
  `need_approval` tinyint NOT NULL DEFAULT '0',
  `geo_score` int DEFAULT NULL COMMENT '最近GEO检测分',
  `source_draft_id` bigint unsigned DEFAULT NULL COMMENT '来源内容草稿',
  `studio_keyword_id` bigint unsigned DEFAULT NULL COMMENT 'AI创作关键词明细ID',
  `target_keyword` varchar(256) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '目标关键词快照',
  `omni_job_id` bigint unsigned DEFAULT NULL COMMENT 'Omni任务来源',
  `create_by` bigint unsigned DEFAULT NULL,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_deleted` tinyint NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `idx_tenant_platform_status` (`tenant_id`,`platform`,`status`,`plan_time`),
  KEY `idx_account` (`platform_account_id`),
  KEY `idx_studio_keyword_status` (`tenant_id`,`studio_keyword_id`,`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='平台分发内容(小红书/头条等)';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_geo_effect_report`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_geo_effect_report` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint unsigned NOT NULL DEFAULT '0',
  `project_id` bigint unsigned DEFAULT NULL,
  `title` varchar(256) COLLATE utf8mb4_unicode_ci NOT NULL,
  `report_type` varchar(32) COLLATE utf8mb4_unicode_ci DEFAULT 'weekly' COMMENT 'weekly/monthly/roi',
  `period_start` date DEFAULT NULL,
  `period_end` date DEFAULT NULL,
  `compare_json` json DEFAULT NULL,
  `roi_json` json DEFAULT NULL,
  `file_url` varchar(512) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'PDF存储',
  `status` varchar(32) COLLATE utf8mb4_unicode_ci DEFAULT 'done',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_tenant_type` (`tenant_id`,`report_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='效果评估报告';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_geo_entity`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_geo_entity` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint unsigned NOT NULL DEFAULT '0',
  `project_id` bigint unsigned DEFAULT NULL,
  `entity_text` varchar(256) COLLATE utf8mb4_unicode_ci NOT NULL,
  `entity_type` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '品牌/产品/组织',
  `confidence` int DEFAULT NULL,
  `source` varchar(32) COLLATE utf8mb4_unicode_ci DEFAULT 'manual',
  `aligned` tinyint DEFAULT '0',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `is_deleted` tinyint NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `idx_tenant_type` (`tenant_id`,`entity_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='实体库';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_geo_entity_alignment`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_geo_entity_alignment` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint unsigned NOT NULL DEFAULT '0',
  `own_entity_id` bigint unsigned NOT NULL,
  `external_source` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT 'wikidata',
  `external_entity_id` varchar(128) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `external_name` varchar(256) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `match_score` int DEFAULT NULL,
  `aligned_at` datetime DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_own_entity` (`own_entity_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='实体对齐';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_geo_expand_mode`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_geo_expand_mode` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `group_id` bigint unsigned NOT NULL,
  `code` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '模式值 ai/longtail/compare',
  `label` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '展示名',
  `description` varchar(512) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '说明',
  `template` varchar(32) COLLATE utf8mb4_unicode_ci DEFAULT '指南型' COMMENT '意图模板',
  `suffix_json` json DEFAULT NULL COMMENT '后缀数组 [" 是什么"," 怎么做"]',
  `prefix_json` json DEFAULT NULL COMMENT '前缀数组 ["什么是","为什么需要"]',
  `is_virtual` tinyint NOT NULL DEFAULT '0' COMMENT '1=虚拟模式如全意图混合',
  `sort_order` int NOT NULL DEFAULT '0',
  `status` tinyint NOT NULL DEFAULT '1',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_code` (`code`),
  KEY `idx_group_sort` (`group_id`,`sort_order`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='AI创作扩展模式';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_geo_expand_mode_group`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_geo_expand_mode_group` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `code` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '分组编码',
  `label` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '分组名称',
  `sort_order` int NOT NULL DEFAULT '0',
  `status` tinyint NOT NULL DEFAULT '1',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_code` (`code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='AI创作扩展模式分组';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_geo_freshness_alert`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_geo_freshness_alert` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint unsigned NOT NULL DEFAULT '0',
  `asset_id` bigint unsigned DEFAULT NULL,
  `alert_level` varchar(32) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `message` varchar(512) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `status` varchar(32) COLLATE utf8mb4_unicode_ci DEFAULT 'open',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_tenant_status` (`tenant_id`,`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='内容新鲜度告警';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_geo_gbp_profile`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_geo_gbp_profile` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint unsigned NOT NULL DEFAULT '0',
  `project_id` bigint unsigned DEFAULT NULL,
  `store_id` bigint unsigned DEFAULT NULL,
  `binding_id` bigint unsigned DEFAULT NULL,
  `profile_json` json DEFAULT NULL,
  `ai_summary` text COLLATE utf8mb4_unicode_ci COMMENT '本地AI摘要',
  `sync_at` datetime DEFAULT NULL,
  `source_type` varchar(16) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'import',
  `normalized_json` longtext COLLATE utf8mb4_unicode_ci,
  `raw_json` longtext COLLATE utf8mb4_unicode_ci,
  `external_updated_at` datetime DEFAULT NULL,
  `sync_job_id` bigint unsigned DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_store` (`store_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='GBP/地图资料';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_geo_hallucination_alert`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_geo_hallucination_alert` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint unsigned NOT NULL DEFAULT '0',
  `project_id` bigint unsigned DEFAULT NULL,
  `question_id` bigint unsigned DEFAULT NULL,
  `claim_text` varchar(1024) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `severity` varchar(32) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `status` varchar(32) COLLATE utf8mb4_unicode_ci DEFAULT 'open' COMMENT 'open/resolved',
  `resolved_at` datetime DEFAULT NULL,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_tenant_status` (`tenant_id`,`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='AI幻觉告警';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_geo_hot_trend`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_geo_hot_trend` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint unsigned NOT NULL DEFAULT '0',
  `project_id` bigint unsigned DEFAULT NULL,
  `platform` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'baidu/douyin/xhs/toutiao',
  `rank_no` int DEFAULT NULL,
  `title` varchar(256) COLLATE utf8mb4_unicode_ci NOT NULL,
  `heat` bigint DEFAULT NULL,
  `tags_json` json DEFAULT NULL,
  `related_keywords_json` json DEFAULT NULL,
  `trend_date` date NOT NULL,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_platform_date` (`platform`,`trend_date`,`rank_no`),
  KEY `idx_tenant_project_platform_date` (`tenant_id`,`project_id`,`platform`,`trend_date`,`rank_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='热搜/热榜';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_geo_hot_trend_sync_log`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_geo_hot_trend_sync_log` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint unsigned NOT NULL DEFAULT '0',
  `project_id` bigint unsigned NOT NULL DEFAULT '0',
  `platform` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL,
  `sync_count` int DEFAULT '0',
  `status` varchar(32) COLLATE utf8mb4_unicode_ci DEFAULT 'success',
  `error_msg` varchar(512) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `sync_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_platform_time` (`platform`,`sync_at`),
  KEY `idx_tenant_project_time` (`tenant_id`,`project_id`,`sync_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='热搜同步日志';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_geo_image_asset`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_geo_image_asset` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint unsigned NOT NULL DEFAULT '0',
  `project_id` bigint unsigned DEFAULT NULL,
  `name` varchar(256) COLLATE utf8mb4_unicode_ci NOT NULL,
  `category` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT 'product' COMMENT 'logo/product/scene/cover/infographic',
  `file_url` varchar(512) COLLATE utf8mb4_unicode_ci NOT NULL,
  `thumb_url` varchar(512) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `tags_json` json DEFAULT NULL,
  `file_size` bigint DEFAULT NULL COMMENT '字节',
  `width` int DEFAULT NULL,
  `height` int DEFAULT NULL,
  `ai_recommended` tinyint NOT NULL DEFAULT '0',
  `create_by` bigint unsigned DEFAULT NULL,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `is_deleted` tinyint NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `idx_tenant_category` (`tenant_id`,`project_id`,`category`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='企业图库(17.5)';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_geo_image_category`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_geo_image_category` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint NOT NULL,
  `project_id` bigint NOT NULL,
  `name` varchar(64) NOT NULL,
  `create_time` datetime NOT NULL,
  `update_time` datetime NOT NULL,
  `is_deleted` tinyint NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_geo_image_category_project_name` (`tenant_id`,`project_id`,`name`,`is_deleted`),
  KEY `idx_geo_image_category_project` (`tenant_id`,`project_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='GEO企业图像分类';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_geo_indexing_record`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_geo_indexing_record` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint unsigned NOT NULL DEFAULT '0',
  `project_id` bigint unsigned DEFAULT NULL,
  `keyword_or_question` varchar(512) COLLATE utf8mb4_unicode_ci NOT NULL,
  `ai_model_code` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'deepseek/doubao/yuanbao等',
  `index_status` varchar(32) COLLATE utf8mb4_unicode_ci DEFAULT 'no' COMMENT 'yes/no/partial',
  `index_url` varchar(512) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `rank` int DEFAULT NULL COMMENT '收录排名位置，越小越靠前',
  `first_indexed_at` datetime DEFAULT NULL,
  `last_indexed_at` datetime DEFAULT NULL,
  `stat_date` date DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_tenant_model` (`tenant_id`,`ai_model_code`,`stat_date`),
  KEY `idx_keyword` (`keyword_or_question`(191))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='AI收录明细(30.5)';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_geo_industry_benchmark`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_geo_industry_benchmark` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint unsigned NOT NULL DEFAULT '0',
  `project_id` bigint unsigned DEFAULT NULL,
  `industry` varchar(128) COLLATE utf8mb4_unicode_ci NOT NULL,
  `metric_key` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'SOV/citation_rate/health',
  `industry_value` decimal(12,4) DEFAULT NULL,
  `self_value` decimal(12,4) DEFAULT NULL,
  `gap` decimal(12,4) DEFAULT NULL,
  `stat_period` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_tenant_industry` (`tenant_id`,`industry`,`metric_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='行业基准(F2)';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_geo_intent_page_match`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_geo_intent_page_match` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint unsigned NOT NULL DEFAULT '0',
  `project_id` bigint unsigned DEFAULT NULL,
  `page_id` bigint unsigned DEFAULT NULL,
  `page_url` varchar(512) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `intent` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `match_score` int DEFAULT NULL,
  `mismatch_reason` varchar(256) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_tenant_page` (`tenant_id`,`page_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='页面意图匹配矩阵';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_geo_intent_record`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_geo_intent_record` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint unsigned NOT NULL DEFAULT '0',
  `project_id` bigint unsigned DEFAULT NULL,
  `question` varchar(512) COLLATE utf8mb4_unicode_ci NOT NULL,
  `primary_intent` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `sub_intents_json` json DEFAULT NULL,
  `priority` int DEFAULT NULL,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_tenant_intent` (`tenant_id`,`primary_intent`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='意图记录';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_geo_intent_rule_config`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_geo_intent_rule_config` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint unsigned NOT NULL DEFAULT '0',
  `project_id` bigint unsigned DEFAULT NULL,
  `taxonomy` varchar(128) COLLATE utf8mb4_unicode_ci DEFAULT '8类意图',
  `weight` int DEFAULT '80',
  `config_json` json DEFAULT NULL,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_tenant_project` (`tenant_id`,`project_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='意图规则配置(A3)';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_geo_internal_link_audit`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_geo_internal_link_audit` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint unsigned NOT NULL DEFAULT '0',
  `project_id` bigint unsigned DEFAULT NULL,
  `page_url` varchar(512) COLLATE utf8mb4_unicode_ci NOT NULL,
  `inbound_count` int DEFAULT '0',
  `is_orphan` tinyint DEFAULT '0',
  `pagerank_loss` decimal(8,4) DEFAULT NULL,
  `suggestion` varchar(512) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `audit_at` datetime DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_tenant_orphan` (`tenant_id`,`is_orphan`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='内链/孤岛审计';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_geo_jsonld_validate_log`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_geo_jsonld_validate_log` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint unsigned NOT NULL DEFAULT '0',
  `project_id` bigint unsigned NOT NULL DEFAULT '0',
  `page_id` bigint unsigned DEFAULT NULL,
  `input_json` mediumtext COLLATE utf8mb4_unicode_ci,
  `valid` tinyint NOT NULL DEFAULT '0',
  `errors_json` json DEFAULT NULL,
  `warnings_json` json DEFAULT NULL,
  `create_by` bigint unsigned DEFAULT NULL,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_tenant_time` (`tenant_id`,`create_time`),
  KEY `idx_project_page` (`tenant_id`,`project_id`,`page_id`,`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='JSON-LD校验日志';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_geo_keyword`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_geo_keyword` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint unsigned NOT NULL DEFAULT '0',
  `project_id` bigint unsigned DEFAULT NULL,
  `keyword` varchar(256) COLLATE utf8mb4_unicode_ci NOT NULL,
  `template_tag` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '问题模板',
  `intent` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `difficulty` int DEFAULT NULL,
  `opportunity` int DEFAULT NULL,
  `trend_pct` decimal(6,2) DEFAULT NULL,
  `covered` tinyint DEFAULT '0',
  `lang` varchar(16) COLLATE utf8mb4_unicode_ci DEFAULT 'zh',
  `cluster_id` bigint unsigned DEFAULT NULL,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `is_deleted` tinyint NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `idx_tenant_keyword` (`tenant_id`,`project_id`,`intent`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='关键词/扩展词';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_geo_keyword_coverage`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_geo_keyword_coverage` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint unsigned NOT NULL DEFAULT '0',
  `project_id` bigint unsigned DEFAULT NULL,
  `studio_keyword_id` bigint unsigned NOT NULL COMMENT 'hou_geo_ai_studio_keyword.id',
  `keyword_text` varchar(256) COLLATE utf8mb4_general_ci NOT NULL COMMENT '关键词快照',
  `source_type` varchar(32) COLLATE utf8mb4_general_ci NOT NULL COMMENT 'content_draft|distribute_post|article',
  `source_id` bigint unsigned NOT NULL COMMENT '来源记录 ID',
  `platform` varchar(32) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT 'xhs/toutiao 等，draft 可为空',
  `cover_status` varchar(32) COLLATE utf8mb4_general_ci NOT NULL DEFAULT 'draft' COMMENT 'draft|pending|published|failed|revoked',
  `publish_link` varchar(512) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `covered_at` datetime DEFAULT NULL COMMENT '首次变为 published 的时间',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_deleted` tinyint NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_source` (`source_type`,`source_id`,`platform`),
  KEY `idx_keyword_status` (`studio_keyword_id`,`cover_status`),
  KEY `idx_tenant_keyword` (`tenant_id`,`keyword_text`(64))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='关键词覆盖明细';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_geo_keyword_discover_task`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_geo_keyword_discover_task` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint unsigned NOT NULL DEFAULT '0',
  `project_id` bigint unsigned DEFAULT NULL,
  `task_name` varchar(128) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `seed_json` json DEFAULT NULL,
  `mode` varchar(32) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'ai/question/longtail',
  `ai_model_ids` json DEFAULT NULL,
  `status` varchar(32) COLLATE utf8mb4_unicode_ci DEFAULT 'pending',
  `result_count` int DEFAULT '0',
  `create_by` bigint unsigned DEFAULT NULL,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `finished_at` datetime DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_tenant_status` (`tenant_id`,`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='关键词发现任务';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_geo_keyword_index`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_geo_keyword_index` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint unsigned NOT NULL DEFAULT '0',
  `project_id` bigint unsigned DEFAULT NULL,
  `keyword` varchar(256) COLLATE utf8mb4_unicode_ci NOT NULL,
  `heat_index` int DEFAULT NULL,
  `competition_index` int DEFAULT NULL,
  `opportunity_index` int DEFAULT NULL,
  `trend_json` json DEFAULT NULL COMMENT '日/周趋势',
  `stat_date` date NOT NULL,
  `source` varchar(128) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '历史导入' COMMENT '真实指标数据来源',
  `region` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '全国' COMMENT '指标统计地区',
  `collected_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '指标采集或导入时间',
  PRIMARY KEY (`id`),
  KEY `idx_tenant_date` (`tenant_id`,`stat_date`),
  KEY `idx_geo_keyword_index_lookup` (`tenant_id`,`project_id`,`keyword`,`stat_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='关键词指数(30.6)';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_geo_kg_snapshot`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_geo_kg_snapshot` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint unsigned NOT NULL DEFAULT '0',
  `project_id` bigint unsigned DEFAULT NULL,
  `nodes_json` json DEFAULT NULL,
  `edges_json` json DEFAULT NULL,
  `refreshed_at` datetime DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_tenant_project` (`tenant_id`,`project_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='知识图谱快照';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_geo_knowledge_item`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_geo_knowledge_item` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint unsigned NOT NULL DEFAULT '0',
  `project_id` bigint unsigned DEFAULT NULL,
  `title` varchar(256) COLLATE utf8mb4_unicode_ci NOT NULL,
  `item_type` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT 'faq' COMMENT 'intro/manual/faq/case/policy',
  `content` mediumtext COLLATE utf8mb4_unicode_ci,
  `word_count` int DEFAULT NULL,
  `vector_status` varchar(32) COLLATE utf8mb4_unicode_ci DEFAULT 'pending' COMMENT 'pending/done/failed',
  `is_default_context` tinyint NOT NULL DEFAULT '0',
  `file_url` varchar(512) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '导入文档路径',
  `source_type` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'manual',
  `source_name` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `visibility` varchar(16) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'project',
  `current_version` int NOT NULL DEFAULT '1',
  `owner_user_id` bigint unsigned DEFAULT NULL,
  `create_by` bigint unsigned DEFAULT NULL,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_deleted` tinyint NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `idx_tenant_type` (`tenant_id`,`project_id`,`item_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='企业知识库(17.6)';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_geo_knowledge_item_tag`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_geo_knowledge_item_tag` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint unsigned NOT NULL DEFAULT '0',
  `project_id` bigint unsigned DEFAULT NULL,
  `knowledge_id` bigint unsigned NOT NULL,
  `tag_id` bigint unsigned NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_geo_knowledge_item_tag` (`knowledge_id`,`tag_id`),
  KEY `idx_geo_knowledge_item_tag_project` (`tenant_id`,`project_id`,`knowledge_id`),
  KEY `idx_geo_knowledge_item_tag_tag` (`tenant_id`,`project_id`,`tag_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='GEO知识库条目标签';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_geo_knowledge_share`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_geo_knowledge_share` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint unsigned NOT NULL DEFAULT '0',
  `project_id` bigint unsigned DEFAULT NULL,
  `knowledge_id` bigint unsigned NOT NULL,
  `user_id` bigint unsigned NOT NULL,
  `access_level` varchar(16) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'viewer',
  `created_by` bigint unsigned DEFAULT NULL,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_geo_knowledge_share` (`knowledge_id`,`user_id`),
  KEY `idx_geo_knowledge_share_project` (`tenant_id`,`project_id`,`knowledge_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='GEO知识库共享成员';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_geo_knowledge_tag`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_geo_knowledge_tag` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint unsigned NOT NULL DEFAULT '0',
  `project_id` bigint unsigned DEFAULT NULL,
  `name` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_deleted` tinyint NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_geo_knowledge_tag` (`tenant_id`,`project_id`,`name`,`is_deleted`),
  KEY `idx_geo_knowledge_tag_project` (`tenant_id`,`project_id`,`is_deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='GEO知识库标签';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_geo_knowledge_version`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_geo_knowledge_version` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint unsigned NOT NULL DEFAULT '0',
  `project_id` bigint unsigned DEFAULT NULL,
  `knowledge_id` bigint unsigned NOT NULL,
  `version_no` int NOT NULL,
  `title` varchar(256) COLLATE utf8mb4_unicode_ci NOT NULL,
  `item_type` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT 'faq',
  `content` mediumtext COLLATE utf8mb4_unicode_ci,
  `word_count` int DEFAULT NULL,
  `change_note` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `created_by` bigint unsigned DEFAULT NULL,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `is_deleted` tinyint NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_geo_knowledge_version` (`tenant_id`,`project_id`,`knowledge_id`,`version_no`),
  KEY `idx_geo_knowledge_version_item` (`tenant_id`,`project_id`,`knowledge_id`,`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='GEO知识库版本';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_geo_landing_page`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_geo_landing_page` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint unsigned NOT NULL DEFAULT '0',
  `project_id` bigint unsigned DEFAULT NULL,
  `title` varchar(256) COLLATE utf8mb4_unicode_ci NOT NULL,
  `keyword` varchar(256) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `html_content` mediumtext COLLATE utf8mb4_unicode_ci,
  `schema_json` json DEFAULT NULL,
  `meta_json` json DEFAULT NULL COMMENT '行业/风格/GEO特性/评分等元数据',
  `status` varchar(32) COLLATE utf8mb4_unicode_ci DEFAULT 'draft',
  `create_by` bigint unsigned DEFAULT NULL,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_tenant_status` (`tenant_id`,`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='AI落地页(30.7)';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_geo_llm_endpoint`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_geo_llm_endpoint` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint unsigned NOT NULL DEFAULT '0',
  `project_id` bigint unsigned NOT NULL,
  `alias` varchar(128) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '自定义别名',
  `usage_mode` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'index_only' COMMENT 'index_only/index_write',
  `web_search` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'off' COMMENT 'off/on/manual',
  `provider_code` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'deepseek/doubao/hunyuan/qwen/ernie/custom',
  `api_endpoint` varchar(512) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'chat/completions 完整地址',
  `api_key` varchar(1024) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '加密存储 ENC::',
  `model_name` varchar(128) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '模型 ID',
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '1正常 0停用',
  `last_test_at` datetime DEFAULT NULL,
  `last_test_ok` tinyint DEFAULT NULL COMMENT '1成功 0失败',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_deleted` tinyint NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `idx_geo_llm_endpoint_project` (`tenant_id`,`project_id`,`is_deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='GEO项目级大模型API对接';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_geo_local_monitor_plan`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_geo_local_monitor_plan` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint unsigned NOT NULL,
  `project_id` bigint unsigned NOT NULL,
  `schedule_type` varchar(16) NOT NULL DEFAULT 'manual',
  `run_time` time DEFAULT NULL,
  `weekday` tinyint DEFAULT NULL,
  `timezone` varchar(64) NOT NULL DEFAULT 'Asia/Shanghai',
  `models_json` text NOT NULL,
  `is_enabled` tinyint NOT NULL DEFAULT '0',
  `next_run_at` datetime DEFAULT NULL,
  `last_run_at` datetime DEFAULT NULL,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_local_monitor_plan_project` (`tenant_id`,`project_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='本地AI监测计划';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_geo_local_monitor_query`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_geo_local_monitor_query` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint unsigned NOT NULL,
  `project_id` bigint unsigned NOT NULL,
  `store_id` bigint unsigned DEFAULT NULL,
  `city` varchar(64) NOT NULL,
  `query_text` varchar(512) NOT NULL,
  `brand_name` varchar(160) DEFAULT NULL,
  `domain` varchar(256) DEFAULT NULL,
  `aliases_json` text,
  `competitors_json` text,
  `is_enabled` tinyint NOT NULL DEFAULT '1',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_deleted` tinyint NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `idx_local_monitor_query_project` (`tenant_id`,`project_id`,`is_enabled`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='本地AI监测查询词';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_geo_local_monitor_run`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_geo_local_monitor_run` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint unsigned NOT NULL,
  `project_id` bigint unsigned NOT NULL,
  `plan_id` bigint unsigned DEFAULT NULL,
  `trigger_type` varchar(16) NOT NULL,
  `status` varchar(24) NOT NULL DEFAULT 'pending',
  `query_count` int NOT NULL DEFAULT '0',
  `model_count` int NOT NULL DEFAULT '0',
  `success_count` int NOT NULL DEFAULT '0',
  `failure_count` int NOT NULL DEFAULT '0',
  `models_json` text NOT NULL,
  `error_message` varchar(1024) DEFAULT NULL,
  `started_at` datetime DEFAULT NULL,
  `finished_at` datetime DEFAULT NULL,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_local_monitor_run_project` (`tenant_id`,`project_id`,`status`,`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='本地AI监测运行';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_geo_local_nap_issue`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_geo_local_nap_issue` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint unsigned NOT NULL,
  `project_id` bigint unsigned NOT NULL,
  `store_id` bigint unsigned NOT NULL,
  `binding_id` bigint unsigned DEFAULT NULL,
  `audit_id` bigint unsigned NOT NULL,
  `platform` varchar(32) NOT NULL,
  `field_code` varchar(32) NOT NULL,
  `expected_value` text,
  `observed_value` text,
  `suggested_value` text,
  `status` varchar(24) NOT NULL DEFAULT 'open',
  `approved_by` bigint unsigned DEFAULT NULL,
  `approved_at` datetime DEFAULT NULL,
  `rejected_reason` varchar(512) DEFAULT NULL,
  `sync_job_id` bigint unsigned DEFAULT NULL,
  `workflow_task_id` bigint unsigned DEFAULT NULL,
  `last_checked_at` datetime DEFAULT NULL,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_local_nap_issue_project` (`tenant_id`,`project_id`,`status`),
  KEY `idx_local_nap_issue_identity` (`store_id`,`platform`,`field_code`,`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='本地化NAP修复问题';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_geo_local_platform_binding`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_geo_local_platform_binding` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint unsigned NOT NULL,
  `project_id` bigint unsigned NOT NULL,
  `store_id` bigint unsigned NOT NULL,
  `platform_account_id` bigint unsigned DEFAULT NULL,
  `platform` varchar(32) NOT NULL,
  `external_id` varchar(160) DEFAULT NULL,
  `external_name` varchar(256) DEFAULT NULL,
  `access_mode` varchar(16) NOT NULL DEFAULT 'import',
  `capabilities_json` text,
  `status` varchar(24) NOT NULL DEFAULT 'unconfigured',
  `raw_snapshot_json` longtext,
  `last_sync_at` datetime DEFAULT NULL,
  `last_error` varchar(1024) DEFAULT NULL,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_deleted` tinyint NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_local_binding` (`tenant_id`,`project_id`,`store_id`,`platform`,`external_id`,`is_deleted`),
  KEY `idx_local_binding_project` (`tenant_id`,`project_id`,`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='本地化平台绑定';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_geo_local_query_monitor`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_geo_local_query_monitor` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint unsigned NOT NULL DEFAULT '0',
  `project_id` bigint unsigned DEFAULT NULL,
  `query_text` varchar(512) COLLATE utf8mb4_unicode_ci NOT NULL,
  `city` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `ai_model_code` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `mentioned` tinyint DEFAULT '0',
  `rank_no` decimal(6,2) DEFAULT NULL,
  `record_date` date NOT NULL,
  `query_id` bigint unsigned DEFAULT NULL,
  `run_id` bigint unsigned DEFAULT NULL,
  `store_id` bigint unsigned DEFAULT NULL,
  `status` varchar(24) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'succeeded',
  `competitor_mentioned` tinyint NOT NULL DEFAULT '0',
  `raw_answer` longtext COLLATE utf8mb4_unicode_ci,
  `citations_json` longtext COLLATE utf8mb4_unicode_ci,
  `source_urls_json` longtext COLLATE utf8mb4_unicode_ci,
  `error_message` varchar(1024) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `captured_at` datetime DEFAULT NULL,
  `external_request_id` varchar(160) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `model_name` varchar(128) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `provider_code` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_tenant_date` (`tenant_id`,`record_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='本地AI摘要监测(29.6)';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_geo_local_store`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_geo_local_store` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint unsigned NOT NULL DEFAULT '0',
  `project_id` bigint unsigned DEFAULT NULL,
  `store_code` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `store_name` varchar(128) COLLATE utf8mb4_unicode_ci NOT NULL,
  `address` varchar(512) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `phone` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `lat` decimal(10,6) DEFAULT NULL,
  `lng` decimal(10,6) DEFAULT NULL,
  `platform_refs_json` json DEFAULT NULL COMMENT '地图/GBP ID',
  `status` varchar(16) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'active',
  `country_code` varchar(8) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'CN',
  `province` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `city` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `district` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `postal_code` varchar(24) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `website` varchar(512) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `category` varchar(128) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `business_hours_json` text COLLATE utf8mb4_unicode_ci,
  `source_type` varchar(16) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'manual',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_deleted` tinyint NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `idx_tenant` (`tenant_id`),
  KEY `idx_local_store_project` (`tenant_id`,`project_id`,`status`,`is_deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='本地门店';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_geo_local_sync_job`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_geo_local_sync_job` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint unsigned NOT NULL,
  `project_id` bigint unsigned NOT NULL,
  `store_id` bigint unsigned DEFAULT NULL,
  `binding_id` bigint unsigned DEFAULT NULL,
  `job_type` varchar(24) NOT NULL,
  `platform` varchar(32) DEFAULT NULL,
  `trigger_type` varchar(24) NOT NULL DEFAULT 'manual',
  `status` varchar(24) NOT NULL DEFAULT 'pending',
  `idempotency_key` varchar(160) NOT NULL,
  `success_count` int NOT NULL DEFAULT '0',
  `failure_count` int NOT NULL DEFAULT '0',
  `input_json` longtext,
  `result_json` longtext,
  `error_code` varchar(64) DEFAULT NULL,
  `error_message` varchar(1024) DEFAULT NULL,
  `started_at` datetime DEFAULT NULL,
  `finished_at` datetime DEFAULT NULL,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_local_sync_idempotency` (`tenant_id`,`project_id`,`idempotency_key`),
  KEY `idx_local_sync_project` (`tenant_id`,`project_id`,`status`,`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='本地化同步任务';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_geo_media_resource`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_geo_media_resource` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint unsigned NOT NULL DEFAULT '0',
  `project_id` bigint unsigned NOT NULL DEFAULT '0',
  `external_media_key` varchar(191) COLLATE utf8mb4_unicode_ci NOT NULL,
  `media_keys_json` json DEFAULT NULL COMMENT '透镜 media_keys',
  `account_count` int DEFAULT NULL COMMENT '透镜 account_count',
  `name` varchar(256) COLLATE utf8mb4_unicode_ci NOT NULL,
  `account_name` varchar(256) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '透镜 account_name',
  `dimension` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'account',
  `media_type` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `media_type_label` varchar(128) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `platform` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `platform_label` varchar(128) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `news_source` varchar(128) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `news_source_label` varchar(128) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `industry` varchar(128) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `industry_label` varchar(128) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `index_status` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `index_status_label` varchar(128) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `area` varchar(128) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `title_limit` int DEFAULT NULL COMMENT '透镜 title_limit',
  `is_recommend` tinyint DEFAULT NULL COMMENT '透镜 is_recommend',
  `auth_status` int DEFAULT NULL COMMENT '透镜 auth_status',
  `is_frequent` tinyint DEFAULT NULL COMMENT '透镜 is_frequent',
  `can_geo` tinyint DEFAULT NULL COMMENT '透镜 can_geo',
  `icon_color` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '透镜 icon_color',
  `logo` varchar(1024) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '透镜 logo',
  `url` varchar(1024) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `submit_url` varchar(1024) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `case_url` varchar(1024) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `inclusion` varchar(128) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `inclusion_label` varchar(128) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `geo_engines_json` json DEFAULT NULL COMMENT '透镜 geo_engines',
  `success_rate` decimal(8,2) DEFAULT NULL,
  `cite_count` int DEFAULT NULL,
  `article_count` int DEFAULT NULL COMMENT '透镜 article_count',
  `cited_by_json` json DEFAULT NULL COMMENT '透镜 cited_by',
  `cite_count_window` int DEFAULT NULL,
  `cited_by_window_json` json DEFAULT NULL COMMENT '透镜 cited_by_window',
  `stats_window_json` json DEFAULT NULL COMMENT '透镜 stats_window',
  `list_price` decimal(12,2) DEFAULT NULL,
  `sell_price` decimal(12,2) DEFAULT NULL,
  `publish_time` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '透镜 publish_time',
  `sort_type` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '透镜 sort_type',
  `note` text COLLATE utf8mb4_unicode_ci,
  `tags_json` json DEFAULT NULL,
  `metadata_json` json DEFAULT NULL,
  `source_system` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'timus',
  `favorite` tinyint NOT NULL DEFAULT '0',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_geo_media_resource_scope_key` (`tenant_id`,`project_id`,`external_media_key`),
  KEY `idx_geo_media_resource_scope` (`tenant_id`,`project_id`,`dimension`),
  KEY `idx_geo_media_resource_name` (`tenant_id`,`project_id`,`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='GEO媒体信源目录';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_geo_menu`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_geo_menu` (
  `id` int unsigned NOT NULL AUTO_INCREMENT,
  `pid` int unsigned NOT NULL DEFAULT '0' COMMENT '父级菜单 id',
  `type` char(1) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'M' COMMENT 'M=目录 C=菜单 A=按钮',
  `name` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '菜单名称',
  `icon` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT '' COMMENT 'Element Plus 图标名',
  `sort` int NOT NULL DEFAULT '0' COMMENT '排序（越大越靠前）',
  `perms` varchar(128) COLLATE utf8mb4_unicode_ci DEFAULT '' COMMENT '权限标识 geo:xxx',
  `paths` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT '' COMMENT '前端路由 /geo/...',
  `component` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT '' COMMENT '预留组件路径',
  `menu_mode` varchar(16) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'expert' COMMENT 'workflow | expert',
  `is_cache` tinyint NOT NULL DEFAULT '0',
  `is_show` tinyint NOT NULL DEFAULT '1',
  `is_disable` tinyint NOT NULL DEFAULT '0',
  `create_time` int unsigned NOT NULL DEFAULT '0',
  `update_time` int unsigned NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `idx_pid` (`pid`),
  KEY `idx_mode_sort` (`menu_mode`,`sort`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='GEO 菜单';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_geo_model_adapt_rule`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_geo_model_adapt_rule` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint unsigned NOT NULL DEFAULT '0',
  `ai_model_id` bigint unsigned DEFAULT NULL,
  `rule_name` varchar(128) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `rule_json` json DEFAULT NULL,
  `status` tinyint DEFAULT '1',
  PRIMARY KEY (`id`),
  KEY `idx_tenant_model` (`tenant_id`,`ai_model_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='分模型适配规则';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_geo_monitor_config`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_geo_monitor_config` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint unsigned NOT NULL DEFAULT '0',
  `project_id` bigint unsigned DEFAULT NULL,
  `ai_model_ids` json DEFAULT NULL COMMENT '监测模型ID列表',
  `keyword_pool_json` json DEFAULT NULL COMMENT '监测词库',
  `collect_frequency` varchar(32) COLLATE utf8mb4_unicode_ci DEFAULT 'daily' COMMENT '采集频率',
  `alert_channels_json` json DEFAULT NULL COMMENT '邮件/企微/钉钉',
  `hallucination_threshold` decimal(5,2) DEFAULT '0.30' COMMENT '幻觉阈值',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_tenant_project` (`tenant_id`,`project_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='监测与预警配置';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_geo_monitor_question`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_geo_monitor_question` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint unsigned NOT NULL DEFAULT '0',
  `project_id` bigint unsigned DEFAULT NULL,
  `question` varchar(512) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '监测问题',
  `intent` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '意图',
  `heat` int DEFAULT '0',
  `frequency` int DEFAULT '0',
  `source` varchar(32) COLLATE utf8mb4_unicode_ci DEFAULT 'manual' COMMENT 'manual/hot_trend/csv/a1',
  `in_monitor` tinyint NOT NULL DEFAULT '0',
  `ai_model_ids` json DEFAULT NULL,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_deleted` tinyint NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `idx_tenant_monitor` (`tenant_id`,`project_id`,`in_monitor`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='监测问题库';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_geo_multimodal_job`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_geo_multimodal_job` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint unsigned NOT NULL DEFAULT '0',
  `project_id` bigint unsigned DEFAULT NULL,
  `job_type` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'visual/tts/avatar/chart',
  `title` varchar(256) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `params_json` json DEFAULT NULL,
  `status` varchar(32) COLLATE utf8mb4_unicode_ci DEFAULT 'pending',
  `asset_id` bigint unsigned DEFAULT NULL,
  `error_msg` varchar(512) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `finished_at` datetime DEFAULT NULL,
  `video_project_id` bigint unsigned DEFAULT NULL COMMENT 'AI video project ID',
  `parent_job_id` bigint unsigned DEFAULT NULL,
  `root_job_id` bigint unsigned DEFAULT NULL,
  `scene_id` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `provider_code` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `model_name` varchar(128) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `external_task_id` varchar(191) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `idempotency_key` varchar(128) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `workflow_stage` varchar(32) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `stage_order` smallint unsigned NOT NULL DEFAULT '0',
  `progress` tinyint unsigned NOT NULL DEFAULT '0',
  `retry_count` smallint unsigned NOT NULL DEFAULT '0',
  `max_retry_count` smallint unsigned NOT NULL DEFAULT '3',
  `error_code` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `error_detail` text COLLATE utf8mb4_unicode_ci,
  `output_url` varchar(1024) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `thumbnail_url` varchar(1024) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `callback_token_hash` varchar(128) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `callback_meta_json` json DEFAULT NULL,
  `cost_estimated` decimal(18,6) NOT NULL DEFAULT '0.000000',
  `cost_actual` decimal(18,6) NOT NULL DEFAULT '0.000000',
  `currency` varchar(8) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'CNY',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `started_at` datetime DEFAULT NULL,
  `timeout_at` datetime DEFAULT NULL,
  `next_poll_at` datetime DEFAULT NULL,
  `version` int unsigned NOT NULL DEFAULT '0',
  `callback_event_id` varchar(191) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_tenant_idempotency` (`tenant_id`,`idempotency_key`),
  UNIQUE KEY `uk_provider_external` (`provider_code`,`external_task_id`),
  KEY `idx_tenant_type_status` (`tenant_id`,`job_type`,`status`),
  KEY `idx_root_stage` (`root_job_id`,`workflow_stage`,`status`),
  KEY `idx_poll` (`status`,`next_poll_at`),
  KEY `idx_video_project_time` (`video_project_id`,`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='多模态生成任务';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_geo_multimodal_job_log`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_geo_multimodal_job_log` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint unsigned NOT NULL DEFAULT '0',
  `job_id` bigint unsigned NOT NULL,
  `root_job_id` bigint unsigned DEFAULT NULL,
  `event_type` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL,
  `from_status` varchar(32) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `to_status` varchar(32) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `workflow_stage` varchar(32) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `progress` tinyint unsigned NOT NULL DEFAULT '0',
  `provider_code` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `external_task_id` varchar(191) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `callback_event_id` varchar(191) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `message` varchar(512) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `metadata_json` json DEFAULT NULL,
  `operator_id` bigint unsigned DEFAULT NULL,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_provider_callback_event` (`provider_code`,`external_task_id`,`callback_event_id`),
  KEY `idx_job_time` (`job_id`,`create_time`),
  KEY `idx_root_id` (`root_job_id`,`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Multimodal job event log';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_geo_nap_audit`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_geo_nap_audit` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint unsigned NOT NULL DEFAULT '0',
  `project_id` bigint unsigned DEFAULT NULL,
  `store_id` bigint unsigned DEFAULT NULL,
  `binding_id` bigint unsigned DEFAULT NULL,
  `channel` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'website/baidu/gbp',
  `name_ok` tinyint DEFAULT NULL,
  `address_ok` tinyint DEFAULT NULL,
  `phone_ok` tinyint DEFAULT NULL,
  `issue_detail` varchar(512) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `audit_at` datetime DEFAULT NULL,
  `sync_job_id` bigint unsigned DEFAULT NULL,
  `expected_json` longtext COLLATE utf8mb4_unicode_ci,
  `observed_json` longtext COLLATE utf8mb4_unicode_ci,
  `result_json` longtext COLLATE utf8mb4_unicode_ci,
  `is_recheck` tinyint NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `idx_store` (`store_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='NAP一致性审计';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_geo_omni_job`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_geo_omni_job` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint unsigned NOT NULL DEFAULT '0',
  `project_id` bigint unsigned DEFAULT NULL,
  `job_no` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'job_xxx',
  `plan_id` bigint unsigned DEFAULT NULL COMMENT '月计划ID',
  `plan_item_id` bigint unsigned DEFAULT NULL COMMENT '月计划明细ID',
  `source_title` varchar(256) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `source_json` json DEFAULT NULL COMMENT '源内容:title/conclusion/body/entities',
  `trigger_type` varchar(32) COLLATE utf8mb4_unicode_ci DEFAULT 'immediate' COMMENT 'immediate/schedule',
  `operator_id` bigint unsigned DEFAULT NULL,
  `operator_name` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `status` varchar(32) COLLATE utf8mb4_unicode_ci DEFAULT 'processing' COMMENT 'processing/done/partial_failed',
  `success_count` int DEFAULT '0',
  `total_count` int DEFAULT '0',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `finished_at` datetime DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_job_no` (`job_no`),
  KEY `idx_tenant_time` (`tenant_id`,`create_time`),
  KEY `idx_plan_item` (`plan_id`,`plan_item_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='全平台Omni发布任务';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_geo_omni_job_item`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_geo_omni_job_item` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `job_id` bigint unsigned NOT NULL,
  `platform` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL,
  `platform_account_id` bigint unsigned DEFAULT NULL,
  `adapted_title` varchar(256) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `adapted_body` mediumtext COLLATE utf8mb4_unicode_ci,
  `similarity_pct` int DEFAULT NULL COMMENT 'Dify风险值',
  `publishable` tinyint DEFAULT NULL COMMENT 'Dify可否发布',
  `adapt_reason` varchar(512) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'Dify判定说明',
  `adapt_source` varchar(32) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'dify/local',
  `status` varchar(32) COLLATE utf8mb4_unicode_ci DEFAULT 'pending' COMMENT 'success/failed/pending',
  `status_text` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `published_at` datetime DEFAULT NULL,
  `publish_link` varchar(512) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `error_msg` varchar(512) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `distribute_post_id` bigint unsigned DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_job_platform` (`job_id`,`platform`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Omni分平台发布项';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_geo_omni_job_log`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_geo_omni_job_log` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint unsigned NOT NULL DEFAULT '0',
  `project_id` bigint unsigned DEFAULT NULL,
  `job_id` bigint unsigned NOT NULL,
  `job_item_id` bigint unsigned DEFAULT NULL,
  `platform` varchar(32) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `event_type` varchar(48) COLLATE utf8mb4_unicode_ci NOT NULL,
  `level` varchar(16) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'INFO',
  `message` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `detail_json` longtext COLLATE utf8mb4_unicode_ci,
  `create_by` bigint unsigned DEFAULT NULL,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_omni_job_log_job` (`tenant_id`,`project_id`,`job_id`,`create_time`,`id`),
  KEY `idx_omni_job_log_item` (`job_item_id`,`create_time`,`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Omni发布任务追加式事件日志';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_geo_operation_audit`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_geo_operation_audit` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint unsigned NOT NULL DEFAULT '0',
  `user_id` bigint unsigned DEFAULT NULL,
  `user_name` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `action_type` varchar(32) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '登录/创建/编辑/删除/导出',
  `module` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `description` varchar(512) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `ip_address` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `risk_level` varchar(16) COLLATE utf8mb4_unicode_ci DEFAULT '低' COMMENT '低/中/高',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_tenant_time` (`tenant_id`,`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='操作审计日志(24.6)';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_geo_outreach_prospect`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_geo_outreach_prospect` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint unsigned NOT NULL DEFAULT '0',
  `domain` varchar(256) COLLATE utf8mb4_unicode_ci NOT NULL,
  `domain_authority` int DEFAULT NULL,
  `prospect_type` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `status` varchar(32) COLLATE utf8mb4_unicode_ci DEFAULT 'new',
  `roi_estimate` decimal(8,2) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_tenant_status` (`tenant_id`,`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='外链拓展目标';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_geo_outreach_task`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_geo_outreach_task` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint unsigned NOT NULL DEFAULT '0',
  `project_id` bigint unsigned DEFAULT NULL,
  `domain` varchar(256) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `task_type` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `status` varchar(32) COLLATE utf8mb4_unicode_ci DEFAULT 'pending',
  `assignee_id` bigint unsigned DEFAULT NULL,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_tenant_status` (`tenant_id`,`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='外链拓展任务';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_geo_page_schema_version`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_geo_page_schema_version` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `page_id` bigint unsigned NOT NULL,
  `version_no` int NOT NULL DEFAULT '1',
  `template_id` bigint unsigned DEFAULT NULL,
  `schema_fields_json` json DEFAULT NULL,
  `json_ld` mediumtext COLLATE utf8mb4_unicode_ci,
  `status` varchar(32) COLLATE utf8mb4_unicode_ci DEFAULT 'draft' COMMENT 'draft/published',
  `validation_status` varchar(16) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'unvalidated',
  `errors_json` json DEFAULT NULL,
  `warnings_json` json DEFAULT NULL,
  `published_at` datetime DEFAULT NULL,
  `create_by` bigint unsigned DEFAULT NULL,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_page_version` (`page_id`,`version_no`),
  KEY `idx_page_template` (`template_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='页面Schema版本';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_geo_page_tab_cache`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_geo_page_tab_cache` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint unsigned NOT NULL DEFAULT '0',
  `project_id` bigint unsigned DEFAULT NULL,
  `tab_key` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'keywordTrend/backlinkGraph等27个key',
  `payload_json` json NOT NULL,
  `refreshed_at` datetime DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_tenant_tab` (`tenant_id`,`project_id`,`tab_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='页面Tab数据缓存';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_geo_performance_metric`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_geo_performance_metric` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint unsigned NOT NULL DEFAULT '0',
  `project_id` bigint unsigned DEFAULT NULL,
  `page_url` varchar(512) COLLATE utf8mb4_unicode_ci NOT NULL,
  `lcp` decimal(8,2) DEFAULT NULL COMMENT 'ms',
  `fid` decimal(8,2) DEFAULT NULL,
  `cls` decimal(8,4) DEFAULT NULL,
  `mobile_score` int DEFAULT NULL,
  `lighthouse_score` int DEFAULT NULL,
  `checked_at` datetime DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_tenant_url` (`tenant_id`,`page_url`(191))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='经典性能指标(21)';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_geo_platform_account`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_geo_platform_account` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint unsigned NOT NULL DEFAULT '0',
  `project_id` bigint unsigned DEFAULT NULL,
  `platform` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'xhs/toutiao/douyin/bjh/wechat',
  `account_name` varchar(128) COLLATE utf8mb4_unicode_ci NOT NULL,
  `account_id` varchar(128) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '平台侧账号ID',
  `auth_status` varchar(32) COLLATE utf8mb4_unicode_ci DEFAULT 'normal' COMMENT 'normal/expiring/expired',
  `access_token` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci COMMENT 'encrypted Token or Cookie',
  `refresh_token` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci COMMENT 'encrypted Refresh Token',
  `expire_at` datetime DEFAULT NULL,
  `last_sync_at` datetime DEFAULT NULL,
  `is_default` tinyint NOT NULL DEFAULT '0',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_deleted` tinyint NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `idx_tenant_platform` (`tenant_id`,`platform`,`auth_status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='平台OAuth账号';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_geo_platform_adapt_record`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_geo_platform_adapt_record` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint unsigned NOT NULL DEFAULT '0',
  `project_id` bigint unsigned DEFAULT NULL,
  `preview_batch_no` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '预览批次 preview_xxx',
  `draft_id` bigint unsigned DEFAULT NULL COMMENT '关联文章/草稿 ID',
  `article_id` bigint unsigned DEFAULT NULL COMMENT '关联 hou_geo_article.id',
  `job_id` bigint unsigned DEFAULT NULL COMMENT 'Omni 发布任务 ID',
  `job_item_id` bigint unsigned DEFAULT NULL COMMENT '分平台发布项 ID',
  `platform` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '平台编码 xhs/zhihu 等',
  `source_title` varchar(256) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '原文标题',
  `keyword` varchar(256) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '目标关键词',
  `adapted_title` varchar(256) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '适配标题',
  `similarity_pct` int DEFAULT NULL COMMENT '风险值 0-100，越低越安全',
  `publishable` tinyint NOT NULL DEFAULT '0' COMMENT '可否发布 1是 0否',
  `reason` varchar(512) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'Dify 判定说明',
  `adapt_source` varchar(32) COLLATE utf8mb4_unicode_ci DEFAULT 'dify' COMMENT 'dify/local',
  `token_usage_id` bigint unsigned DEFAULT NULL COMMENT 'hou_ai_token_usage.id',
  `phase` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'preview' COMMENT 'preview/publish',
  `create_by` bigint unsigned DEFAULT NULL,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_tenant_draft_phase` (`tenant_id`,`draft_id`,`phase`),
  KEY `idx_job_platform` (`job_id`,`platform`),
  KEY `idx_preview_batch` (`preview_batch_no`),
  KEY `idx_article_phase` (`article_id`,`phase`,`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Dify分平台适配判定记录';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_geo_platform_auth_log`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_geo_platform_auth_log` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint unsigned NOT NULL,
  `project_id` bigint unsigned NOT NULL,
  `account_id` bigint unsigned NOT NULL,
  `action` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'OAuth授权、Cookie授权、连接检测、刷新Token、设为默认、解绑账号',
  `result` varchar(16) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'success/failed',
  `detail` varchar(1000) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `operator_id` bigint unsigned DEFAULT NULL,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_project_account_time` (`tenant_id`,`project_id`,`account_id`,`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='GEO平台账号授权操作日志';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_geo_platform_config`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_geo_platform_config` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint unsigned NOT NULL DEFAULT '0',
  `platform` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'xhs/toutiao',
  `app_id` varchar(128) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `app_secret` varchar(256) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '加密存储',
  `oauth_redirect_uri` varchar(512) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `api_mode` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'sandbox' COMMENT 'sandbox/production',
  `publish_defaults_json` json DEFAULT NULL COMMENT '默认内容类型/话题模板等',
  `ai_prompt_json` json DEFAULT NULL COMMENT '分平台 AI 语气模板',
  `extra_json` json DEFAULT NULL COMMENT 'scope/endpoint 等',
  `status` tinyint NOT NULL DEFAULT '1',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_tenant_platform` (`tenant_id`,`platform`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='平台开放平台配置';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_geo_platform_credential`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_geo_platform_credential` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint unsigned NOT NULL DEFAULT '0',
  `project_id` bigint unsigned DEFAULT NULL,
  `platform` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL,
  `account_id` varchar(128) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '为空表示平台默认账号',
  `username` varchar(128) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '登录账号明文',
  `password` varchar(1024) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '登录密码（AES 加密存储）',
  `enabled` tinyint NOT NULL DEFAULT '1',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_deleted` tinyint NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `idx_geo_credential_scope` (`tenant_id`,`project_id`,`platform`,`is_deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='GEO平台登录凭证（自动重新登录用）';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_geo_platform_publish_quota`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_geo_platform_publish_quota` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint unsigned NOT NULL DEFAULT '0',
  `project_id` bigint unsigned DEFAULT NULL,
  `platform` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL,
  `monthly_limit` int DEFAULT NULL,
  `default_publish_time` varchar(8) COLLATE utf8mb4_unicode_ci DEFAULT '09:30',
  `enabled` tinyint NOT NULL DEFAULT '1',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_tenant_platform` (`tenant_id`,`project_id`,`platform`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_geo_project`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_geo_project` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '主键',
  `org_id` int DEFAULT NULL,
  `tenant_id` bigint unsigned NOT NULL DEFAULT '0' COMMENT '租户ID',
  `name` varchar(128) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '项目名称',
  `client_name` varchar(128) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '客户名称',
  `brand_name` varchar(128) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '主品牌名',
  `domain` varchar(256) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '主域名',
  `status` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'active' COMMENT 'active/archived',
  `monitor_scope_json` json DEFAULT NULL COMMENT '监测范围：模型/词库/竞品',
  `sov_target` decimal(5,2) DEFAULT NULL COMMENT 'SOV目标%',
  `task_count` int NOT NULL DEFAULT '0' COMMENT '关联任务数',
  `create_by` bigint unsigned DEFAULT NULL,
  `update_by` bigint unsigned DEFAULT NULL,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_deleted` tinyint NOT NULL DEFAULT '0',
  `dify_dataset_id` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '项目独立 Dify Dataset ID',
  `dify_dataset_name` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '项目独立 Dify 知识库名称',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_geo_project_tenant_name_deleted` (`tenant_id`,`name`,`is_deleted`),
  UNIQUE KEY `uk_geo_project_tenant_domain_deleted` (`tenant_id`,`domain`,`is_deleted`),
  KEY `idx_tenant_status` (`tenant_id`,`status`,`is_deleted`),
  KEY `idx_geo_project_tenant_org` (`tenant_id`,`org_id`,`is_deleted`,`update_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='GEO项目/客户';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_geo_project_member`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_geo_project_member` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint unsigned NOT NULL DEFAULT '0',
  `project_id` bigint unsigned NOT NULL COMMENT '项目ID',
  `user_id` bigint unsigned NOT NULL COMMENT '用户ID',
  `user_name` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `role` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'editor' COMMENT 'admin/editor/analyst',
  `task_count` int NOT NULL DEFAULT '0',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `is_deleted` tinyint NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_project_user` (`project_id`,`user_id`),
  KEY `idx_tenant_project` (`tenant_id`,`project_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='GEO项目成员';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_geo_project_seed`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_geo_project_seed` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '主键',
  `tenant_id` bigint unsigned NOT NULL COMMENT '租户ID',
  `project_id` bigint unsigned NOT NULL COMMENT '项目ID',
  `seed_text` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '用户输入的种子词',
  `normalized_seed` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '去重用规范化种子词',
  `sort_order` int NOT NULL DEFAULT '0' COMMENT '显示顺序',
  `create_by` bigint unsigned DEFAULT NULL,
  `update_by` bigint unsigned DEFAULT NULL,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_tenant_project_seed` (`tenant_id`,`project_id`,`normalized_seed`),
  KEY `idx_tenant_project_sort` (`tenant_id`,`project_id`,`sort_order`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='GEO项目监测种子词';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_geo_publish_plan`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_geo_publish_plan` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint unsigned NOT NULL DEFAULT '0',
  `project_id` bigint unsigned DEFAULT NULL,
  `org_id` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `plan_month` varchar(7) COLLATE utf8mb4_unicode_ci NOT NULL,
  `name` varchar(128) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `status` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'draft',
  `default_platforms_json` json DEFAULT NULL,
  `article_count` int NOT NULL DEFAULT '0',
  `scheduled_count` int NOT NULL DEFAULT '0',
  `rules_json` json DEFAULT NULL,
  `create_by` bigint unsigned DEFAULT NULL,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_deleted` tinyint NOT NULL DEFAULT '0',
  `article_base_time` time DEFAULT '10:00:00' COMMENT '文章基准发送时间',
  `video_base_time` time DEFAULT '18:00:00' COMMENT '视频基准发送时间',
  `stagger_minutes` int DEFAULT '120' COMMENT '平台错峰窗口分钟',
  `warnings_json` text COLLATE utf8mb4_unicode_ci COMMENT '计划告警',
  `activated_at` datetime DEFAULT NULL,
  `paused_at` datetime DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_tenant_month` (`tenant_id`,`project_id`,`plan_month`,`is_deleted`),
  KEY `idx_status` (`tenant_id`,`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_geo_publish_plan_item`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_geo_publish_plan_item` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `plan_id` bigint unsigned NOT NULL,
  `article_id` bigint unsigned NOT NULL,
  `plan_date` date NOT NULL,
  `plan_time` time NOT NULL DEFAULT '09:30:00',
  `platforms_json` json NOT NULL,
  `preview_batch_no` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `omni_job_id` bigint unsigned DEFAULT NULL,
  `status` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'pending',
  `sort_order` int NOT NULL DEFAULT '0',
  `error_msg` varchar(512) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_plan_date` (`plan_id`,`plan_date`),
  KEY `idx_article` (`article_id`),
  KEY `idx_status` (`status`,`plan_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_geo_ranking_kpi`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_geo_ranking_kpi` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint unsigned NOT NULL DEFAULT '0',
  `project_id` bigint unsigned DEFAULT NULL,
  `kpi_name` varchar(128) COLLATE utf8mb4_unicode_ci NOT NULL,
  `target_value` decimal(12,4) DEFAULT NULL,
  `current_value` decimal(12,4) DEFAULT NULL,
  `deadline` date DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_tenant_project` (`tenant_id`,`project_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='排名KPI目标';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_geo_ranking_record`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_geo_ranking_record` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint unsigned NOT NULL DEFAULT '0',
  `project_id` bigint unsigned DEFAULT NULL,
  `keyword_or_question` varchar(512) COLLATE utf8mb4_unicode_ci NOT NULL,
  `ai_model_id` bigint unsigned DEFAULT NULL,
  `rank_no` decimal(6,2) DEFAULT NULL,
  `mentioned` tinyint DEFAULT '0',
  `record_date` date NOT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_tenant_date` (`tenant_id`,`record_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='AI排名记录';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_geo_research_conversion`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_geo_research_conversion` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint unsigned NOT NULL,
  `project_id` bigint unsigned NOT NULL,
  `source_type` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL,
  `source_id` bigint unsigned NOT NULL,
  `source_text` varchar(512) COLLATE utf8mb4_unicode_ci NOT NULL,
  `target_type` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL,
  `target_id` bigint unsigned DEFAULT NULL,
  `status` varchar(16) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'pending',
  `error_message` varchar(512) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `create_by` bigint unsigned DEFAULT NULL,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_project_source_target` (`tenant_id`,`project_id`,`source_type`,`source_id`,`target_type`),
  KEY `idx_project_status` (`tenant_id`,`project_id`,`status`,`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='研究结果转化来源台账';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_geo_role`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_geo_role` (
  `id` int unsigned NOT NULL AUTO_INCREMENT,
  `name` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '角色名称',
  `desc` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '描述',
  `sort` int NOT NULL DEFAULT '0',
  `create_time` int unsigned NOT NULL DEFAULT '0',
  `update_time` int unsigned NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='GEO 角色';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_geo_role_menu`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_geo_role_menu` (
  `role_id` int unsigned NOT NULL,
  `menu_id` int unsigned NOT NULL,
  PRIMARY KEY (`role_id`,`menu_id`),
  KEY `idx_menu_id` (`menu_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='GEO 角色-菜单';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_geo_run`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_geo_run` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint NOT NULL,
  `project_id` bigint NOT NULL,
  `run_type` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `idempotency_key` varchar(128) COLLATE utf8mb4_unicode_ci NOT NULL,
  `status` varchar(24) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'pending',
  `total_count` int NOT NULL DEFAULT '0',
  `success_count` int NOT NULL DEFAULT '0',
  `failed_count` int NOT NULL DEFAULT '0',
  `request_json` longtext COLLATE utf8mb4_unicode_ci,
  `result_json` longtext COLLATE utf8mb4_unicode_ci,
  `error_code` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `error_message` varchar(1000) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `started_at` datetime DEFAULT NULL,
  `finished_at` datetime DEFAULT NULL,
  `create_by` bigint DEFAULT NULL,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_geo_run_idempotency` (`tenant_id`,`project_id`,`run_type`,`idempotency_key`),
  KEY `idx_geo_run_project_status` (`tenant_id`,`project_id`,`status`,`create_time`),
  KEY `idx_geo_run_type_time` (`run_type`,`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='GEO 统一运行记录';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_geo_run_event`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_geo_run_event` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint NOT NULL,
  `project_id` bigint NOT NULL,
  `run_id` bigint NOT NULL,
  `item_id` bigint DEFAULT NULL,
  `event_type` varchar(48) COLLATE utf8mb4_unicode_ci NOT NULL,
  `from_status` varchar(24) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `to_status` varchar(24) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `message` varchar(1000) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `detail_json` longtext COLLATE utf8mb4_unicode_ci,
  `create_by` bigint DEFAULT NULL,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_geo_run_event_run` (`tenant_id`,`project_id`,`run_id`,`create_time`),
  KEY `idx_geo_run_event_item` (`item_id`,`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='GEO 统一运行事件';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_geo_run_item`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_geo_run_item` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint NOT NULL,
  `project_id` bigint NOT NULL,
  `run_id` bigint NOT NULL,
  `item_key` varchar(512) COLLATE utf8mb4_unicode_ci NOT NULL,
  `status` varchar(24) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'pending',
  `input_json` longtext COLLATE utf8mb4_unicode_ci,
  `output_json` longtext COLLATE utf8mb4_unicode_ci,
  `raw_response` longtext COLLATE utf8mb4_unicode_ci,
  `error_code` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `error_message` varchar(1000) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `attempt_count` int NOT NULL DEFAULT '0',
  `next_retry_at` datetime DEFAULT NULL,
  `started_at` datetime DEFAULT NULL,
  `finished_at` datetime DEFAULT NULL,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_geo_run_item_key` (`run_id`,`item_key`(191)),
  KEY `idx_geo_run_item_project` (`tenant_id`,`project_id`,`run_id`),
  KEY `idx_geo_run_item_retry` (`status`,`next_retry_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='GEO 统一运行子项';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_geo_sandbox_run`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_geo_sandbox_run` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint unsigned NOT NULL DEFAULT '0',
  `project_id` bigint unsigned NOT NULL DEFAULT '0',
  `ai_model_ids` json DEFAULT NULL,
  `input_content` mediumtext COLLATE utf8mb4_unicode_ci,
  `result_json` json DEFAULT NULL,
  `create_by` bigint unsigned DEFAULT NULL,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_tenant_time` (`tenant_id`,`create_time`),
  KEY `idx_project_time` (`tenant_id`,`project_id`,`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='模型沙箱运行';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_geo_scheduled_send_task`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_geo_scheduled_send_task` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `plan_id` bigint NOT NULL,
  `tenant_id` bigint NOT NULL,
  `project_id` bigint DEFAULT NULL,
  `plan_date` date NOT NULL,
  `content_type` varchar(16) NOT NULL,
  `content_id` bigint NOT NULL,
  `content_title` varchar(255) DEFAULT NULL,
  `platform_code` varchar(32) NOT NULL,
  `scheduled_at` datetime NOT NULL,
  `executed_at` datetime DEFAULT NULL,
  `status` varchar(20) NOT NULL DEFAULT 'pending',
  `warning_code` varchar(64) DEFAULT NULL,
  `error_message` varchar(1000) DEFAULT NULL,
  `retry_count` int NOT NULL DEFAULT '0',
  `omni_job_id` bigint DEFAULT NULL,
  `idempotency_key` varchar(96) NOT NULL,
  `create_time` datetime NOT NULL,
  `update_time` datetime NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_scheduled_send_idempotency` (`idempotency_key`),
  KEY `idx_scheduled_send_due` (`status`,`scheduled_at`),
  KEY `idx_scheduled_send_plan` (`plan_id`,`plan_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='全渠道定时发送分平台任务';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_geo_schema_template`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_geo_schema_template` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint unsigned NOT NULL DEFAULT '0',
  `project_id` bigint unsigned NOT NULL DEFAULT '0',
  `name` varchar(128) COLLATE utf8mb4_unicode_ci NOT NULL,
  `schema_type` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'Article/Product/FAQPage',
  `description` varchar(512) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `json_ld_template` mediumtext COLLATE utf8mb4_unicode_ci COMMENT 'JSON-LD模板',
  `fields_json` json DEFAULT NULL,
  `tags_json` json DEFAULT NULL COMMENT '标签',
  `usage_count` int DEFAULT '0',
  `status` tinyint NOT NULL DEFAULT '1',
  `create_by` bigint unsigned DEFAULT NULL,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_deleted` tinyint NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `idx_tenant_type` (`tenant_id`,`schema_type`),
  KEY `idx_project_type` (`tenant_id`,`project_id`,`schema_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Schema模板';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_geo_semantic_html`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_geo_semantic_html` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint unsigned NOT NULL DEFAULT '0',
  `project_id` bigint unsigned DEFAULT NULL,
  `page_url` varchar(512) COLLATE utf8mb4_unicode_ci NOT NULL,
  `score` int DEFAULT NULL,
  `issues_json` json DEFAULT NULL,
  `checked_at` datetime DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_tenant_url` (`tenant_id`,`page_url`(191))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='语义HTML检测(22)';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_geo_sentiment_alert`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_geo_sentiment_alert` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint unsigned NOT NULL DEFAULT '0',
  `project_id` bigint unsigned DEFAULT NULL,
  `alert_type` varchar(32) COLLATE utf8mb4_unicode_ci DEFAULT 'negative_spike' COMMENT 'negative_spike/rumor/competitor',
  `title` varchar(256) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `severity` varchar(32) COLLATE utf8mb4_unicode_ci DEFAULT '中',
  `status` varchar(32) COLLATE utf8mb4_unicode_ci DEFAULT 'open' COMMENT 'open/closed',
  `detail_json` json DEFAULT NULL,
  `closed_at` datetime DEFAULT NULL,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_tenant_status` (`tenant_id`,`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='情感舆情告警';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_geo_sentiment_record`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_geo_sentiment_record` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint unsigned NOT NULL DEFAULT '0',
  `project_id` bigint unsigned DEFAULT NULL,
  `question` varchar(512) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `sentiment` varchar(32) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'positive/neutral/negative',
  `score` decimal(5,2) DEFAULT NULL,
  `platform` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `record_time` datetime NOT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_tenant_time` (`tenant_id`,`record_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='情感记录';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_geo_site_health_metric`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_geo_site_health_metric` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint unsigned NOT NULL DEFAULT '0',
  `project_id` bigint unsigned NOT NULL DEFAULT '0',
  `page_url` varchar(512) COLLATE utf8mb4_unicode_ci NOT NULL,
  `metric_key` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'lcp/cls/fid/depth',
  `metric_value` decimal(12,4) DEFAULT NULL,
  `correlation` decimal(6,4) DEFAULT NULL,
  `measured_at` datetime DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_tenant_metric` (`tenant_id`,`metric_key`),
  KEY `idx_project_metric` (`tenant_id`,`project_id`,`metric_key`,`measured_at`),
  KEY `idx_project_page_metric_time` (`tenant_id`,`project_id`,`page_url`(120),`metric_key`,`measured_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='站点健康指标';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_geo_site_page`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_geo_site_page` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint unsigned NOT NULL DEFAULT '0',
  `project_id` bigint unsigned DEFAULT NULL,
  `url` varchar(512) COLLATE utf8mb4_unicode_ci NOT NULL,
  `title` varchar(256) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `schema_type` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `config_status` varchar(32) COLLATE utf8mb4_unicode_ci DEFAULT 'none' COMMENT 'none/partial/done/error',
  `warning_count` int DEFAULT '0',
  `error_count` int DEFAULT '0',
  `last_validated_at` datetime DEFAULT NULL,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_deleted` tinyint NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_tenant_project_url` (`tenant_id`,`project_id`,`url`(191)),
  KEY `idx_tenant_status` (`tenant_id`,`config_status`,`schema_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='站点页面';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_geo_source_penetration`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_geo_source_penetration` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint unsigned NOT NULL DEFAULT '0',
  `project_id` bigint unsigned DEFAULT NULL,
  `domain` varchar(256) COLLATE utf8mb4_unicode_ci NOT NULL,
  `domain_type` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '官网/媒体/社媒/百科',
  `penetration_pct` decimal(6,2) DEFAULT NULL,
  `cite_count` int DEFAULT '0',
  `trend_pct` decimal(6,2) DEFAULT NULL,
  `stat_date` date DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_tenant_date` (`tenant_id`,`stat_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='信源穿透(模块6)';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_geo_support_recharge_order`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_geo_support_recharge_order` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint unsigned NOT NULL DEFAULT '0',
  `user_id` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'hou_user_info.user_id',
  `order_no` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '商户订单号 GEOTK...',
  `package_id` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL,
  `token_amount` bigint unsigned NOT NULL DEFAULT '0',
  `amount` decimal(10,2) NOT NULL DEFAULT '0.00' COMMENT '元',
  `pay_status` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'pending' COMMENT 'pending/paid/failed',
  `h5_url` varchar(1024) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `pay_time` datetime DEFAULT NULL,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_order_no` (`order_no`),
  KEY `idx_user_status` (`user_id`,`pay_status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='GEO智能客服Token充值订单';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_geo_system_setting`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_geo_system_setting` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `setting_key` varchar(64) NOT NULL COMMENT '设置键',
  `setting_value` text COMMENT '设置值',
  `setting_name` varchar(128) DEFAULT NULL COMMENT '设置名称',
  `value_type` varchar(20) DEFAULT 'string' COMMENT 'string/number/boolean/json',
  `update_by` bigint DEFAULT NULL COMMENT '最后修改人',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_setting_key` (`setting_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='GEO 平台系统设置';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_geo_technical_check_run`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_geo_technical_check_run` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint unsigned NOT NULL DEFAULT '0',
  `project_id` bigint unsigned DEFAULT NULL,
  `check_type` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'js/semantic/mobile/lighthouse',
  `page_url` varchar(512) COLLATE utf8mb4_unicode_ci NOT NULL,
  `passed` tinyint NOT NULL DEFAULT '0',
  `result_json` json DEFAULT NULL,
  `create_by` bigint unsigned DEFAULT NULL,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_tenant_type_time` (`tenant_id`,`check_type`,`create_time`),
  KEY `idx_project_type_time` (`tenant_id`,`project_id`,`check_type`,`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='技术检测运行记录';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_geo_topic_alert_sub`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_geo_topic_alert_sub` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint unsigned NOT NULL DEFAULT '0',
  `project_id` bigint unsigned DEFAULT NULL,
  `email` varchar(128) COLLATE utf8mb4_unicode_ci NOT NULL,
  `channel` varchar(32) COLLATE utf8mb4_unicode_ci DEFAULT 'email',
  `status` varchar(16) COLLATE utf8mb4_unicode_ci DEFAULT 'active',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_tenant_email` (`tenant_id`,`email`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='话题热度告警订阅(A2)';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_geo_topic_cluster`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_geo_topic_cluster` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint unsigned NOT NULL DEFAULT '0',
  `project_id` bigint unsigned DEFAULT NULL,
  `name` varchar(128) COLLATE utf8mb4_unicode_ci NOT NULL,
  `pillar_url` varchar(512) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `authority_score` int DEFAULT NULL,
  `parent_id` bigint unsigned DEFAULT NULL,
  `sort_order` int DEFAULT '0',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `is_deleted` tinyint NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `idx_tenant_parent` (`tenant_id`,`parent_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='话题集群';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_geo_topic_cluster_item`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_geo_topic_cluster_item` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `cluster_id` bigint unsigned NOT NULL,
  `keyword_id` bigint unsigned DEFAULT NULL,
  `keyword_text` varchar(256) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_cluster` (`cluster_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='话题集群-关键词';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_geo_topic_pillar_plan`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_geo_topic_pillar_plan` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint unsigned NOT NULL DEFAULT '0',
  `project_id` bigint unsigned DEFAULT NULL,
  `cluster_id` bigint unsigned DEFAULT NULL,
  `cluster_name` varchar(128) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `pillar_url` varchar(512) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `sub_pages` int DEFAULT '0',
  `suggested_title` varchar(256) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_tenant_cluster` (`tenant_id`,`cluster_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='支柱内容架构(A2)';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_geo_user_role`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_geo_user_role` (
  `user_id` bigint unsigned NOT NULL COMMENT 'hou_user_info.user_id',
  `role_id` int unsigned NOT NULL,
  PRIMARY KEY (`user_id`,`role_id`),
  KEY `idx_role_id` (`role_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='GEO 用户-角色';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_geo_video_project`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_geo_video_project` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint unsigned NOT NULL DEFAULT '0',
  `geo_project_id` bigint unsigned DEFAULT NULL,
  `title` varchar(256) COLLATE utf8mb4_unicode_ci NOT NULL,
  `video_type` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'product',
  `target_platforms_json` json DEFAULT NULL,
  `aspect_ratio` varchar(16) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '9:16',
  `duration_seconds` int unsigned NOT NULL DEFAULT '30',
  `language` varchar(16) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'zh-CN',
  `current_version_id` bigint unsigned DEFAULT NULL,
  `review_status` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'draft',
  `creator_id` bigint unsigned DEFAULT NULL,
  `is_deleted` tinyint unsigned NOT NULL DEFAULT '0',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_tenant_geo_project` (`tenant_id`,`geo_project_id`,`is_deleted`),
  KEY `idx_creator_time` (`creator_id`,`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='AI video project';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_geo_video_project_version`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_geo_video_project_version` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint unsigned NOT NULL DEFAULT '0',
  `video_project_id` bigint unsigned NOT NULL,
  `version_no` int unsigned NOT NULL,
  `brief_json` json DEFAULT NULL,
  `script_json` json DEFAULT NULL,
  `storyboard_json` json DEFAULT NULL,
  `avatar_json` json DEFAULT NULL,
  `packaging_json` json DEFAULT NULL,
  `publish_json` json DEFAULT NULL,
  `batch_draft_json` json DEFAULT NULL COMMENT '完整批量视频草稿，包含方案、覆盖配置和任务状态',
  `submitted` tinyint unsigned NOT NULL DEFAULT '0',
  `source_job_id` bigint unsigned DEFAULT NULL,
  `creator_id` bigint unsigned DEFAULT NULL,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_project_version` (`video_project_id`,`version_no`),
  KEY `idx_tenant_project` (`tenant_id`,`video_project_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='AI video immutable version';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_geo_video_review`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_geo_video_review` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint unsigned NOT NULL DEFAULT '0',
  `video_project_id` bigint unsigned NOT NULL,
  `version_id` bigint unsigned NOT NULL,
  `job_id` bigint unsigned DEFAULT NULL,
  `status` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL,
  `timecode_ms` bigint unsigned DEFAULT NULL,
  `comment` varchar(1000) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `reviewer_id` bigint unsigned DEFAULT NULL,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_project_version_time` (`video_project_id`,`version_id`,`create_time`),
  KEY `idx_tenant_status` (`tenant_id`,`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='AI video review';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_geo_visibility_daily`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_geo_visibility_daily` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint unsigned NOT NULL DEFAULT '0',
  `project_id` bigint unsigned DEFAULT NULL,
  `stat_date` date NOT NULL,
  `mention_count` int DEFAULT '0',
  `avg_rank` decimal(6,2) DEFAULT NULL,
  `sov` decimal(6,2) DEFAULT NULL,
  `model_count` int DEFAULT '0',
  `platform_stats_json` json DEFAULT NULL COMMENT '分平台SOV',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_tenant_date` (`tenant_id`,`project_id`,`stat_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='能见度日聚合';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_geo_visibility_detail`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_geo_visibility_detail` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint unsigned NOT NULL DEFAULT '0',
  `project_id` bigint unsigned DEFAULT NULL,
  `question_id` bigint unsigned DEFAULT NULL,
  `ai_model_id` bigint unsigned DEFAULT NULL,
  `question` varchar(512) COLLATE utf8mb4_unicode_ci NOT NULL,
  `platform` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '模型显示名',
  `mentioned` tinyint NOT NULL DEFAULT '0',
  `rank_no` decimal(6,2) DEFAULT NULL COMMENT '位次',
  `sov` decimal(6,2) DEFAULT NULL COMMENT 'SOV%',
  `raw_answer` mediumtext COLLATE utf8mb4_unicode_ci COMMENT '原始回答',
  `collect_time` datetime NOT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_collect` (`tenant_id`,`project_id`,`collect_time`),
  KEY `idx_question_model` (`question_id`,`ai_model_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='品牌能见度明细';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_geo_webhook_config`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_geo_webhook_config` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint unsigned NOT NULL DEFAULT '0',
  `callback_url` varchar(512) COLLATE utf8mb4_unicode_ci NOT NULL,
  `events_json` json DEFAULT NULL,
  `secret` varchar(256) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `status` tinyint DEFAULT '1',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_tenant` (`tenant_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Webhook配置';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_geo_webhook_log`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_geo_webhook_log` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint unsigned NOT NULL DEFAULT '0',
  `webhook_id` bigint unsigned DEFAULT NULL,
  `event_type` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `callback_url` varchar(512) COLLATE utf8mb4_unicode_ci NOT NULL,
  `http_status` int DEFAULT NULL,
  `response_body` varchar(2000) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `payload_json` json DEFAULT NULL,
  `success` tinyint NOT NULL DEFAULT '0',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_tenant_time` (`tenant_id`,`create_time`),
  KEY `idx_webhook` (`webhook_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Webhook投递日志';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_geo_workflow_task`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_geo_workflow_task` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint unsigned NOT NULL DEFAULT '0',
  `project_id` bigint unsigned DEFAULT NULL,
  `title` varchar(256) COLLATE utf8mb4_unicode_ci NOT NULL,
  `description` text COLLATE utf8mb4_unicode_ci COMMENT '任务背景、修复步骤与验收标准',
  `task_type` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '内容/技术/链接/分发',
  `priority` varchar(16) COLLATE utf8mb4_unicode_ci DEFAULT '中',
  `assignee_id` bigint unsigned DEFAULT NULL,
  `assignee_name` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `status` varchar(32) COLLATE utf8mb4_unicode_ci DEFAULT '待开始',
  `due_date` date DEFAULT NULL,
  `module_code` varchar(32) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'A1/16/12等',
  `workflow_ref_id` bigint unsigned DEFAULT NULL COMMENT '关联hou_workflow',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_deleted` tinyint NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `idx_tenant_status` (`tenant_id`,`status`,`due_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='GEO工作流任务';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_goods_cat`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_goods_cat` (
  `cat_id` int NOT NULL AUTO_INCREMENT,
  `cat_name` varchar(50) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL COMMENT '分类名称',
  `cat_parent_id` int NOT NULL DEFAULT '0' COMMENT '父类',
  `cat_pic` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT '' COMMENT '分类图片',
  `type_id` int DEFAULT '0' COMMENT '类型id',
  `cat_commission` float DEFAULT '0' COMMENT '分佣比例',
  `cat_is_virtual` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否允许虚拟',
  `cat_displayorder` smallint NOT NULL DEFAULT '255' COMMENT '排序',
  `cat_level` tinyint(1) NOT NULL COMMENT '分类级别',
  `cat_show_status` tinyint(1) DEFAULT '1' COMMENT '是否在前台显示',
  `is_integral_goods` int DEFAULT '0' COMMENT '标注该分类是否是积分商品的分类 1是 0否',
  `cat_alias` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '分类别名',
  `group_id` int DEFAULT NULL COMMENT '商品属性组id',
  `refund_ratio` decimal(10,2) DEFAULT NULL COMMENT '质保金',
  `refund_proportion` int DEFAULT NULL COMMENT '退费比例',
  `baby_commission` decimal(10,4) DEFAULT NULL COMMENT '大宝占类目佣金的比例',
  `bigbaby_commission` decimal(10,4) DEFAULT NULL COMMENT '大宝占比二级',
  `cat_frist_parent_id` int DEFAULT NULL COMMENT '一级父分类ID',
  PRIMARY KEY (`cat_id`) USING BTREE,
  KEY `cat_parent_id` (`cat_parent_id`) USING BTREE,
  KEY `type_id` (`type_id`) USING BTREE,
  KEY `idx_cat_id` (`cat_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 ROW_FORMAT=DYNAMIC COMMENT='商品分类表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_goods_common_i18n`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_goods_common_i18n` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `common_id` int NOT NULL COMMENT '商品ID(mall goods id / classic common_id)',
  `lang_code` varchar(16) COLLATE utf8mb4_general_ci NOT NULL COMMENT '语种 en/ja/ko/fr/de/es',
  `common_name` varchar(500) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '商品名称译文',
  `promotion_tips` varchar(500) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '卖点/广告词译文',
  `common_body` longtext COLLATE utf8mb4_general_ci COMMENT '详情HTML译文',
  `spec_name_json` text COLLATE utf8mb4_general_ci COMMENT '规格名译文JSON',
  `spec_value_json` text COLLATE utf8mb4_general_ci COMMENT '规格值译文JSON',
  `source` varchar(32) COLLATE utf8mb4_general_ci DEFAULT 'manual' COMMENT 'manual/ai/corrected',
  `status` varchar(16) COLLATE utf8mb4_general_ci DEFAULT 'draft' COMMENT 'draft/published/stale',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_common_lang` (`common_id`,`lang_code`),
  KEY `idx_lang_status` (`lang_code`,`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='商品多语言内容';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_job_execution_log`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_job_execution_log` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '日志ID',
  `job_id` bigint DEFAULT NULL COMMENT '任务ID',
  `job_name` varchar(128) NOT NULL DEFAULT '' COMMENT '任务名称',
  `invoke_target` varchar(500) NOT NULL DEFAULT '' COMMENT '调用目标',
  `status` varchar(20) NOT NULL DEFAULT 'running' COMMENT '状态：running/success/failed',
  `start_time` datetime NOT NULL COMMENT '开始时间',
  `end_time` datetime DEFAULT NULL COMMENT '结束时间',
  `duration_ms` bigint DEFAULT NULL COMMENT '耗时毫秒',
  `error_msg` varchar(2000) DEFAULT '' COMMENT '错误信息',
  PRIMARY KEY (`id`),
  KEY `idx_job_id` (`job_id`),
  KEY `idx_status` (`status`),
  KEY `idx_start_time` (`start_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='定时任务执行日志表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_job_platform`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_job_platform` (
  `job_id` bigint NOT NULL AUTO_INCREMENT COMMENT '任务ID',
  `job_name` varchar(64) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL DEFAULT '' COMMENT '任务名称',
  `job_group` varchar(64) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL DEFAULT 'DEFAULT' COMMENT '任务组名',
  `invoke_target` varchar(500) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL COMMENT '调用目标字符串',
  `cron_expression` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT '' COMMENT 'cron执行表达式',
  `status` char(1) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT '0' COMMENT '状态（0正常 1暂停）',
  `create_by` varchar(64) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT '' COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `remark` varchar(500) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT '' COMMENT '备注信息',
  `time_out_period` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '超时时间配置 （例如：查询超过30分钟的数据）',
  PRIMARY KEY (`job_id`,`job_name`,`job_group`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 ROW_FORMAT=DYNAMIC COMMENT='定时任务调度表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_log_access`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_log_access` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT '' COMMENT '用户ID（未登录时为空）',
  `username` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT '' COMMENT '用户名（未登录时为空）',
  `method` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT '' COMMENT 'HTTP请求方法（GET/POST/PUT/DELETE等）',
  `path` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT '' COMMENT '请求路径',
  `query_string` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci COMMENT '查询字符串参数',
  `ip` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT '' COMMENT '客户端IP地址',
  `user_agent` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT '' COMMENT '浏览器User-Agent',
  `status_code` int DEFAULT '0' COMMENT 'HTTP响应状态码',
  `response_time` int DEFAULT '0' COMMENT '响应耗时（毫秒）',
  `referer` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT '' COMMENT '来源页面URL',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_create_time` (`create_time`) COMMENT '按时间检索索引',
  KEY `idx_user_id` (`user_id`) COMMENT '按用户ID检索索引',
  KEY `idx_path` (`path`(191)) COMMENT '按请求路径检索索引',
  KEY `idx_status_code` (`status_code`) COMMENT '按HTTP状态码检索索引'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='访问日志表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_log_business`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_log_business` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT '' COMMENT '操作用户ID',
  `username` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT '' COMMENT '操作用户名',
  `module` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT '' COMMENT '所属模块（如pm、oa、crm）',
  `action` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT '' COMMENT '操作动作（create:新增 / update:修改 / delete:删除 / approve:审批）',
  `target_type` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT '' COMMENT '目标对象类型（如project、task、contract）',
  `target_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT '' COMMENT '目标对象ID',
  `detail` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci COMMENT '操作详情描述',
  `before_data` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci COMMENT '变更前的数据快照（JSON格式）',
  `after_data` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci COMMENT '变更后的数据快照（JSON格式）',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_create_time` (`create_time`) COMMENT '按时间检索索引',
  KEY `idx_user_id` (`user_id`) COMMENT '按用户ID检索索引',
  KEY `idx_module` (`module`) COMMENT '按模块检索索引',
  KEY `idx_action` (`action`) COMMENT '按操作动作检索索引',
  KEY `idx_target_type` (`target_type`) COMMENT '按目标对象类型检索索引'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='业务日志表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_log_error`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_log_error` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `module` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT '' COMMENT '所属模块（如pm、oa、crm）',
  `error_type` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT '' COMMENT '异常类名（全限定名）',
  `error_message` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci COMMENT '错误消息内容',
  `stack_trace` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci COMMENT '异常堆栈信息',
  `request_url` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT '' COMMENT '触发异常的请求URL',
  `request_method` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT '' COMMENT 'HTTP请求方法',
  `request_params` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci COMMENT '请求参数',
  `ip` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT '' COMMENT '客户端IP地址',
  `user_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT '' COMMENT '用户ID',
  `username` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT '' COMMENT '用户名',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_create_time` (`create_time`) COMMENT '按时间检索索引',
  KEY `idx_module` (`module`) COMMENT '按模块检索索引',
  KEY `idx_error_type` (`error_type`(191)) COMMENT '按异常类型检索索引',
  KEY `idx_user_id` (`user_id`) COMMENT '按用户ID检索索引'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='错误日志表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_log_login`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_log_login` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT '' COMMENT '用户ID',
  `username` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT '' COMMENT '用户名',
  `login_type` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT '' COMMENT '登录方式（password:密码 / sms:短信 / wechat:微信 / third:第三方）',
  `login_status` tinyint DEFAULT '0' COMMENT '登录状态：1成功 0失败',
  `ip` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT '' COMMENT '客户端IP地址',
  `location` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT '' COMMENT '登录地点（由IP解析，可选）',
  `device` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT '' COMMENT '设备类型',
  `browser` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT '' COMMENT '浏览器名称及版本',
  `os` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT '' COMMENT '操作系统',
  `fail_reason` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT '' COMMENT '登录失败原因',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_create_time` (`create_time`) COMMENT '按时间检索索引',
  KEY `idx_user_id` (`user_id`) COMMENT '按用户ID检索索引',
  KEY `idx_username` (`username`) COMMENT '按用户名检索索引',
  KEY `idx_login_status` (`login_status`) COMMENT '按登录状态检索索引'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='登录日志表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_log_operation`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_log_operation` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT '' COMMENT '用户ID',
  `username` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT '' COMMENT '用户名',
  `module` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT '' COMMENT '所属模块（如pm、oa、crm）',
  `operation` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT '' COMMENT '操作描述',
  `method` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT '' COMMENT '类名.方法名',
  `request_method` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT '' COMMENT 'HTTP请求方法（GET/POST/PUT/DELETE等）',
  `request_url` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT '' COMMENT '请求URL',
  `request_params` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci COMMENT '请求参数',
  `response_result` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci COMMENT '响应结果（超长时截断存储）',
  `ip` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT '' COMMENT '客户端IP地址',
  `execution_time` int DEFAULT '0' COMMENT '执行耗时（毫秒）',
  `status` tinyint DEFAULT '1' COMMENT '执行状态：1成功 0失败',
  `error_msg` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci COMMENT '失败时的错误信息',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_create_time` (`create_time`) COMMENT '按时间检索索引',
  KEY `idx_user_id` (`user_id`) COMMENT '按用户ID检索索引',
  KEY `idx_module` (`module`) COMMENT '按模块检索索引',
  KEY `idx_status` (`status`) COMMENT '按执行状态检索索引'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='操作日志表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_log_setting`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_log_setting` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `setting_key` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '配置键名（全局唯一）',
  `setting_value` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci COMMENT '配置值',
  `description` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT '' COMMENT '配置项说明',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_setting_key` (`setting_key`) COMMENT '配置键唯一索引'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='日志设置表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_mall_admin`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_mall_admin` (
  `id` int unsigned NOT NULL AUTO_INCREMENT,
  `root` tinyint unsigned NOT NULL DEFAULT '0' COMMENT '是否超级管理员 0-否 1-是',
  `name` varchar(32) NOT NULL DEFAULT '' COMMENT '名称',
  `avatar` varchar(255) NOT NULL DEFAULT '' COMMENT '用户头像',
  `account` varchar(32) NOT NULL DEFAULT '' COMMENT '账号',
  `password` varchar(32) NOT NULL COMMENT '密码',
  `login_time` int DEFAULT NULL COMMENT '最后登录时间',
  `login_ip` varchar(15) DEFAULT '' COMMENT '最后登录ip',
  `multipoint_login` tinyint unsigned DEFAULT '1' COMMENT '是否支持多处登录：1-是；0-否；',
  `disable` tinyint unsigned DEFAULT '0' COMMENT '是否禁用：0-否；1-是；',
  `create_time` int NOT NULL COMMENT '创建时间',
  `update_time` int DEFAULT NULL COMMENT '修改时间',
  `delete_time` int DEFAULT NULL COMMENT '删除时间',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_account` (`account`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='管理员表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_mall_admin_dept`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_mall_admin_dept` (
  `admin_id` int NOT NULL DEFAULT '0' COMMENT '管理员id',
  `dept_id` int NOT NULL DEFAULT '0' COMMENT '部门id',
  PRIMARY KEY (`admin_id`,`dept_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='部门关联表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_mall_admin_jobs`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_mall_admin_jobs` (
  `admin_id` int NOT NULL COMMENT '管理员id',
  `jobs_id` int NOT NULL COMMENT '岗位id',
  PRIMARY KEY (`admin_id`,`jobs_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='岗位关联表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_mall_admin_role`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_mall_admin_role` (
  `admin_id` int NOT NULL COMMENT '管理员id',
  `role_id` int NOT NULL COMMENT '角色id',
  PRIMARY KEY (`admin_id`,`role_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='角色关联表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_mall_admin_session`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_mall_admin_session` (
  `id` int unsigned NOT NULL AUTO_INCREMENT,
  `admin_id` int unsigned NOT NULL COMMENT '用户id',
  `terminal` tinyint(1) NOT NULL DEFAULT '1' COMMENT '客户端类型：1-pc管理后台 2-mobile手机管理后台',
  `token` varchar(32) NOT NULL COMMENT '令牌',
  `update_time` int DEFAULT NULL COMMENT '更新时间',
  `expire_time` int NOT NULL COMMENT '到期时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `admin_id_client` (`admin_id`,`terminal`) USING BTREE COMMENT '一个用户在一个终端只有一个token',
  UNIQUE KEY `token` (`token`) USING BTREE COMMENT 'token是唯一的'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='管理员会话表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_mall_announcement`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_mall_announcement` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '公告 ID',
  `title` varchar(200) NOT NULL DEFAULT '' COMMENT '公告标题',
  `content` longtext COMMENT '公告内容',
  `type` tinyint NOT NULL DEFAULT '1' COMMENT '类型：1-系统通知 2-活动公告 3-规则变更',
  `is_top` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否置顶：0-否 1-是',
  `publish_time` datetime DEFAULT NULL COMMENT '发布时间',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_type` (`type`),
  KEY `idx_publish_time` (`publish_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='系统公告';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_mall_banner`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_mall_banner` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `shop_id` bigint DEFAULT NULL COMMENT '店铺ID(NULL表示平台级)',
  `title` varchar(100) NOT NULL COMMENT 'Banner标题',
  `image_url` varchar(500) NOT NULL COMMENT '图片URL',
  `link_url` varchar(500) DEFAULT NULL COMMENT '跳转链接',
  `link_type` tinyint NOT NULL DEFAULT '1' COMMENT '链接类型: 1-商品 2-分类 3-活动 4-外链 5-无跳转',
  `link_id` bigint DEFAULT NULL COMMENT '关联ID(商品ID/分类ID/活动ID)',
  `position` varchar(50) NOT NULL DEFAULT 'home' COMMENT '位置: home-首页 category-分类页',
  `bg_color` varchar(50) DEFAULT NULL COMMENT '背景色',
  `badge` varchar(20) DEFAULT NULL COMMENT '角标文字',
  `sort` int NOT NULL DEFAULT '0' COMMENT '排序(越大越前)',
  `start_time` datetime DEFAULT NULL COMMENT '开始时间',
  `end_time` datetime DEFAULT NULL COMMENT '结束时间',
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '状态: 0-禁用 1-启用',
  `create_by` bigint DEFAULT NULL COMMENT '创建人',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否删除',
  PRIMARY KEY (`id`),
  KEY `idx_shop_id` (`shop_id`),
  KEY `idx_position` (`position`),
  KEY `idx_status` (`status`),
  KEY `idx_sort` (`sort`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='Banner轮播图表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_mall_cash_on_delivery`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_mall_cash_on_delivery` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `order_id` bigint NOT NULL COMMENT '订单ID',
  `order_no` varchar(64) NOT NULL COMMENT '订单编号',
  `shop_id` bigint NOT NULL COMMENT '店铺ID',
  `buyer_id` bigint NOT NULL COMMENT '买家ID',
  `order_amount` decimal(12,2) NOT NULL COMMENT '订单金额',
  `delivery_fee` decimal(10,2) NOT NULL DEFAULT '0.00' COMMENT '配送费',
  `total_amount` decimal(12,2) NOT NULL COMMENT '应付总额',
  `receiver_name` varchar(50) NOT NULL COMMENT '收货人',
  `receiver_phone` varchar(20) NOT NULL COMMENT '收货电话',
  `receiver_address` varchar(500) NOT NULL COMMENT '收货地址',
  `delivery_status` tinyint NOT NULL DEFAULT '0' COMMENT '配送状态: 0-待发货 1-配送中 2-已送达 3-已签收',
  `payment_status` tinyint NOT NULL DEFAULT '0' COMMENT '支付状态: 0-未支付 1-已支付',
  `delivery_time` datetime DEFAULT NULL COMMENT '发货时间',
  `received_time` datetime DEFAULT NULL COMMENT '签收时间',
  `paid_time` datetime DEFAULT NULL COMMENT '支付时间',
  `paid_amount` decimal(12,2) DEFAULT NULL COMMENT '实付金额',
  `courier_id` bigint DEFAULT NULL COMMENT '配送员ID',
  `courier_name` varchar(50) DEFAULT NULL COMMENT '配送员姓名',
  `courier_phone` varchar(20) DEFAULT NULL COMMENT '配送员电话',
  `remark` text COMMENT '备注',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_order_id` (`order_id`),
  KEY `idx_order_no` (`order_no`),
  KEY `idx_shop_id` (`shop_id`),
  KEY `idx_buyer_id` (`buyer_id`),
  KEY `idx_delivery_status` (`delivery_status`),
  KEY `idx_payment_status` (`payment_status`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='货到付款订单表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_mall_commission_record`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_mall_commission_record` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `shop_id` bigint DEFAULT NULL COMMENT '店铺ID',
  `distributor_id` bigint NOT NULL COMMENT '分销员ID',
  `order_id` bigint NOT NULL COMMENT '关联订单ID',
  `order_no` varchar(64) NOT NULL COMMENT '订单编号',
  `order_amount` decimal(12,2) NOT NULL COMMENT '订单金额',
  `commission_amount` decimal(12,2) NOT NULL COMMENT '佣金金额',
  `commission_rate` decimal(5,2) NOT NULL COMMENT '佣金比例%',
  `commission_type` tinyint NOT NULL DEFAULT '1' COMMENT '佣金类型: 1-一级 2-二级',
  `status` tinyint NOT NULL DEFAULT '0' COMMENT '状态: 0-待确认 1-已确认 2-已结算 3-已取消',
  `settle_time` datetime DEFAULT NULL COMMENT '结算时间',
  `remark` varchar(255) DEFAULT NULL COMMENT '备注',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否删除',
  PRIMARY KEY (`id`),
  KEY `idx_shop_id` (`shop_id`),
  KEY `idx_distributor_id` (`distributor_id`),
  KEY `idx_order_id` (`order_id`),
  KEY `idx_status` (`status`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='佣金记录表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_mall_commission_rule`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_mall_commission_rule` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `shop_id` bigint DEFAULT NULL COMMENT '店铺ID',
  `name` varchar(100) NOT NULL COMMENT '规则名称',
  `category_id` bigint DEFAULT NULL COMMENT '商品分类ID(空表示全部)',
  `goods_id` bigint DEFAULT NULL COMMENT '商品ID(空表示分类下全部)',
  `level_id` bigint DEFAULT NULL COMMENT '适用分销等级(空表示全部)',
  `first_rate` decimal(5,2) NOT NULL COMMENT '一级佣金比例%',
  `second_rate` decimal(5,2) DEFAULT NULL COMMENT '二级佣金比例%',
  `min_amount` decimal(12,2) DEFAULT NULL COMMENT '最低订单金额',
  `priority` int NOT NULL DEFAULT '0' COMMENT '优先级(越大越优先)',
  `start_time` datetime DEFAULT NULL COMMENT '生效时间',
  `end_time` datetime DEFAULT NULL COMMENT '失效时间',
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '状态: 0-禁用 1-启用',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否删除',
  PRIMARY KEY (`id`),
  KEY `idx_shop_id` (`shop_id`),
  KEY `idx_category_id` (`category_id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='佣金规则表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_mall_customer`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_mall_customer` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` bigint DEFAULT NULL COMMENT '关联用户ID',
  `customer_no` varchar(32) DEFAULT NULL COMMENT '客户编号',
  `name` varchar(50) NOT NULL COMMENT '客户名称',
  `phone` varchar(20) DEFAULT NULL COMMENT '手机号',
  `email` varchar(100) DEFAULT NULL COMMENT '邮箱',
  `avatar` varchar(255) DEFAULT NULL COMMENT '头像',
  `gender` tinyint DEFAULT '0' COMMENT '性别: 0-未知 1-男 2-女',
  `birthday` date DEFAULT NULL COMMENT '生日',
  `province` varchar(50) DEFAULT NULL COMMENT '省份',
  `city` varchar(50) DEFAULT NULL COMMENT '城市',
  `district` varchar(50) DEFAULT NULL COMMENT '区县',
  `address` varchar(255) DEFAULT NULL COMMENT '详细地址',
  `group_id` bigint DEFAULT NULL COMMENT '客户分组ID',
  `level` int NOT NULL DEFAULT '1' COMMENT '客户等级',
  `points` int NOT NULL DEFAULT '0' COMMENT '积分',
  `balance` decimal(12,2) NOT NULL DEFAULT '0.00' COMMENT '余额',
  `total_amount` decimal(12,2) NOT NULL DEFAULT '0.00' COMMENT '累计消费金额',
  `order_count` int NOT NULL DEFAULT '0' COMMENT '订单数',
  `last_consume_time` datetime DEFAULT NULL COMMENT '最后消费时间',
  `source` varchar(50) DEFAULT NULL COMMENT '来源渠道',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '状态: 0-禁用 1-正常',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_customer_no` (`customer_no`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_phone` (`phone`),
  KEY `idx_group_id` (`group_id`),
  KEY `idx_status` (`status`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='客户信息表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_mall_customer_follow`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_mall_customer_follow` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `customer_id` bigint NOT NULL COMMENT '客户ID',
  `operator_id` bigint DEFAULT NULL COMMENT '操作人ID',
  `operator_name` varchar(50) DEFAULT NULL COMMENT '操作人名称',
  `type` tinyint NOT NULL DEFAULT '1' COMMENT '跟进类型: 1-电话 2-微信 3-拜访 4-其他',
  `content` text COMMENT '跟进内容',
  `next_follow_time` datetime DEFAULT NULL COMMENT '下次跟进时间',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否删除',
  PRIMARY KEY (`id`),
  KEY `idx_customer_id` (`customer_id`),
  KEY `idx_operator_id` (`operator_id`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='客户跟进记录表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_mall_customer_group`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_mall_customer_group` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `name` varchar(50) NOT NULL COMMENT '分组名称',
  `description` varchar(255) DEFAULT NULL COMMENT '分组说明',
  `sort` int NOT NULL DEFAULT '0' COMMENT '排序',
  `customer_count` int NOT NULL DEFAULT '0' COMMENT '客户数量',
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '状态: 0-禁用 1-正常',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否删除',
  PRIMARY KEY (`id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='客户分组表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_mall_customer_tag`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_mall_customer_tag` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `name` varchar(50) NOT NULL COMMENT '标签名称',
  `color` varchar(20) DEFAULT '#409EFF' COMMENT '标签颜色',
  `description` varchar(255) DEFAULT NULL COMMENT '标签说明',
  `sort` int NOT NULL DEFAULT '0' COMMENT '排序',
  `customer_count` int NOT NULL DEFAULT '0' COMMENT '客户数量',
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '状态: 0-禁用 1-正常',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否删除',
  PRIMARY KEY (`id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='客户标签表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_mall_customer_tag_rel`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_mall_customer_tag_rel` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `customer_id` bigint NOT NULL COMMENT '客户ID',
  `tag_id` bigint NOT NULL COMMENT '标签ID',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_customer_tag` (`customer_id`,`tag_id`),
  KEY `idx_tag_id` (`tag_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='客户标签关联表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_mall_decorate_page`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_mall_decorate_page` (
  `page_id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '页面 ID',
  `shop_id` bigint DEFAULT NULL COMMENT '店铺 ID（NULL 表示平台级）',
  `page_name` varchar(100) NOT NULL DEFAULT '' COMMENT '页面名称',
  `page_type` tinyint NOT NULL DEFAULT '1' COMMENT '类型：1-首页 2-自定义页',
  `page_data` longtext COMMENT '页面设计数据 JSON',
  `is_home` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否设为首页：0-否 1-是',
  `status` tinyint(1) NOT NULL DEFAULT '1' COMMENT '状态：0-禁用 1-启用',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`page_id`),
  KEY `idx_shop_id` (`shop_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='店铺装修页面';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_mall_decorate_tabbar`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_mall_decorate_tabbar` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '主键 ID',
  `shop_id` bigint DEFAULT NULL COMMENT '店铺 ID（NULL 表示平台级）',
  `content` longtext COMMENT '底部导航配置 JSON',
  `status` tinyint(1) NOT NULL DEFAULT '1' COMMENT '状态：0-禁用 1-启用',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_shop_id` (`shop_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='底部导航配置';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_mall_dept`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_mall_dept` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT 'id',
  `name` varchar(30) NOT NULL DEFAULT '' COMMENT '部门名称',
  `pid` bigint NOT NULL DEFAULT '0' COMMENT '上级部门id',
  `sort` int NOT NULL DEFAULT '0' COMMENT '排序',
  `leader` varchar(64) DEFAULT NULL COMMENT '负责人',
  `mobile` varchar(16) DEFAULT NULL COMMENT '联系电话',
  `status` tinyint(1) NOT NULL DEFAULT '0' COMMENT '部门状态（0停用 1正常）',
  `create_time` int NOT NULL COMMENT '创建时间',
  `update_time` int DEFAULT NULL COMMENT '修改时间',
  `delete_time` int DEFAULT NULL COMMENT '删除时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='部门表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_mall_distribution_goods`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_mall_distribution_goods` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `shop_id` bigint DEFAULT NULL COMMENT '店铺ID',
  `goods_id` bigint NOT NULL COMMENT '商品ID',
  `goods_name` varchar(200) NOT NULL COMMENT '商品名称',
  `commission_rate` decimal(5,2) NOT NULL COMMENT '佣金比例%',
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '状态: 0-禁用 1-启用',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_goods_id` (`goods_id`),
  KEY `idx_shop_id` (`shop_id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='分销商品表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_mall_distribution_order`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_mall_distribution_order` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `shop_id` bigint DEFAULT NULL COMMENT '店铺ID',
  `order_id` bigint NOT NULL COMMENT '订单ID',
  `order_no` varchar(64) NOT NULL COMMENT '订单编号',
  `distributor_id` bigint NOT NULL COMMENT '分销员ID',
  `buyer_id` bigint NOT NULL COMMENT '购买用户ID',
  `order_amount` decimal(12,2) NOT NULL COMMENT '订单金额',
  `commission_amount` decimal(12,2) NOT NULL COMMENT '佣金金额',
  `order_status` tinyint NOT NULL COMMENT '订单状态',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_order_id` (`order_id`),
  KEY `idx_shop_id` (`shop_id`),
  KEY `idx_distributor_id` (`distributor_id`),
  KEY `idx_buyer_id` (`buyer_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='分销订单表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_mall_distributor`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_mall_distributor` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `shop_id` bigint DEFAULT NULL COMMENT '店铺ID',
  `user_id` bigint NOT NULL COMMENT '关联用户ID',
  `parent_id` bigint DEFAULT NULL COMMENT '上级分销员ID',
  `distributor_code` varchar(32) DEFAULT NULL COMMENT '分销员编码',
  `real_name` varchar(50) NOT NULL COMMENT '真实姓名',
  `phone` varchar(20) NOT NULL COMMENT '联系电话',
  `level_id` bigint DEFAULT NULL COMMENT '分销等级ID',
  `status` tinyint NOT NULL DEFAULT '0' COMMENT '状态: 0-待审核 1-正常 2-禁用 3-拒绝',
  `total_commission` decimal(12,2) NOT NULL DEFAULT '0.00' COMMENT '累计佣金',
  `available_commission` decimal(12,2) NOT NULL DEFAULT '0.00' COMMENT '可提现佣金',
  `invite_code` varchar(20) DEFAULT NULL COMMENT '邀请码',
  `audit_remark` varchar(255) DEFAULT NULL COMMENT '审核备注',
  `audit_time` datetime DEFAULT NULL COMMENT '审核时间',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_id` (`user_id`),
  UNIQUE KEY `uk_distributor_code` (`distributor_code`),
  KEY `idx_shop_id` (`shop_id`),
  KEY `idx_parent_id` (`parent_id`),
  KEY `idx_status` (`status`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='分销员表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_mall_distributor_level`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_mall_distributor_level` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `shop_id` bigint DEFAULT NULL COMMENT '店铺ID',
  `name` varchar(50) NOT NULL COMMENT '等级名称',
  `level` int NOT NULL COMMENT '等级值',
  `upgrade_condition` varchar(500) DEFAULT NULL COMMENT '升级条件JSON',
  `first_rate` decimal(5,2) NOT NULL DEFAULT '0.00' COMMENT '一级佣金比例%',
  `second_rate` decimal(5,2) DEFAULT '0.00' COMMENT '二级佣金比例%',
  `icon` varchar(255) DEFAULT NULL COMMENT '等级图标',
  `description` varchar(500) DEFAULT NULL COMMENT '等级说明',
  `is_default` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否默认等级',
  `sort` int NOT NULL DEFAULT '0' COMMENT '排序',
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '状态: 0-禁用 1-启用',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否删除',
  PRIMARY KEY (`id`),
  KEY `idx_shop_id` (`shop_id`),
  KEY `idx_level` (`level`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='分销等级表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_mall_file`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_mall_file` (
  `file_id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '文件 ID',
  `shop_id` bigint DEFAULT NULL COMMENT '店铺 ID',
  `cate_id` bigint DEFAULT NULL COMMENT '分类 ID',
  `file_name` varchar(255) NOT NULL DEFAULT '' COMMENT '文件名',
  `file_path` varchar(500) NOT NULL DEFAULT '' COMMENT '文件路径',
  `file_size` bigint DEFAULT NULL COMMENT '文件大小（字节）',
  `file_type` varchar(50) DEFAULT NULL COMMENT '文件类型',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`file_id`),
  KEY `idx_shop_id` (`shop_id`),
  KEY `idx_cate_id` (`cate_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='文件信息';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_mall_file_category`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_mall_file_category` (
  `cate_id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '分类 ID',
  `shop_id` bigint DEFAULT NULL COMMENT '店铺 ID',
  `cate_name` varchar(100) NOT NULL DEFAULT '' COMMENT '分类名称',
  `sort` int NOT NULL DEFAULT '0' COMMENT '排序',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`cate_id`),
  KEY `idx_shop_id` (`shop_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='文件分类';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_mall_finance_expense`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_mall_finance_expense` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `expense_no` varchar(32) DEFAULT NULL COMMENT '支出编号',
  `type` varchar(30) DEFAULT NULL COMMENT '支出类型: refund/commission/other',
  `amount` decimal(12,2) NOT NULL DEFAULT '0.00' COMMENT '金额',
  `related_order_no` varchar(50) DEFAULT NULL COMMENT '关联订单号',
  `remark` varchar(255) DEFAULT NULL COMMENT '备注',
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '0-待确认 1-已确认',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` tinyint(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `idx_type` (`type`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='支出记录表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_mall_finance_income`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_mall_finance_income` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `income_no` varchar(32) DEFAULT NULL COMMENT '收入编号',
  `type` varchar(30) DEFAULT NULL COMMENT '收入类型: order/commission/other',
  `amount` decimal(12,2) NOT NULL DEFAULT '0.00' COMMENT '金额',
  `related_order_no` varchar(50) DEFAULT NULL COMMENT '关联订单号',
  `remark` varchar(255) DEFAULT NULL COMMENT '备注',
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '0-待确认 1-已确认',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` tinyint(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `idx_type` (`type`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='收入记录表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_mall_finance_settlement`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_mall_finance_settlement` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `settlement_no` varchar(32) DEFAULT NULL COMMENT '结算编号',
  `shop_id` bigint DEFAULT NULL COMMENT '店铺ID',
  `shop_name` varchar(100) DEFAULT NULL COMMENT '店铺名称',
  `period_start` date DEFAULT NULL COMMENT '结算周期开始',
  `period_end` date DEFAULT NULL COMMENT '结算周期结束',
  `total_amount` decimal(12,2) NOT NULL DEFAULT '0.00' COMMENT '结算金额',
  `commission_amount` decimal(12,2) NOT NULL DEFAULT '0.00' COMMENT '平台佣金',
  `actual_amount` decimal(12,2) NOT NULL DEFAULT '0.00' COMMENT '实际打款',
  `status` tinyint NOT NULL DEFAULT '0' COMMENT '0-待结算 1-已结算 2-已打款',
  `settle_time` datetime DEFAULT NULL COMMENT '结算时间',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` tinyint(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `idx_shop_id` (`shop_id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='结算记录表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_mall_finance_withdraw`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_mall_finance_withdraw` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `withdraw_no` varchar(32) DEFAULT NULL COMMENT '提现单号',
  `shop_id` bigint DEFAULT NULL COMMENT '店铺ID',
  `shop_name` varchar(100) DEFAULT NULL COMMENT '店铺名称',
  `member_id` bigint DEFAULT NULL COMMENT '会员ID',
  `member_name` varchar(100) DEFAULT NULL COMMENT '会员名称',
  `withdraw_amount` decimal(12,2) NOT NULL DEFAULT '0.00' COMMENT '提现金额',
  `fee` decimal(12,2) NOT NULL DEFAULT '0.00' COMMENT '手续费',
  `actual_amount` decimal(12,2) NOT NULL DEFAULT '0.00' COMMENT '实付金额',
  `bank_account` varchar(100) DEFAULT NULL COMMENT '银行账户',
  `bank_name` varchar(100) DEFAULT NULL COMMENT '开户行',
  `account_name` varchar(100) DEFAULT NULL COMMENT '账户名',
  `settlement_no` varchar(32) DEFAULT NULL COMMENT '关联结算单号',
  `status` tinyint NOT NULL DEFAULT '0' COMMENT '状态: 0-待审核 1-已通过 2-已拒绝 3-已打款',
  `auditor` varchar(50) DEFAULT NULL COMMENT '审核人',
  `audit_remark` varchar(500) DEFAULT NULL COMMENT '审核备注',
  `audit_time` datetime DEFAULT NULL COMMENT '审核时间',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否删除',
  PRIMARY KEY (`id`),
  KEY `idx_shop_id` (`shop_id`),
  KEY `idx_member_id` (`member_id`),
  KEY `idx_status` (`status`),
  KEY `idx_settlement_no` (`settlement_no`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='提现申请表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_mall_gather`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_mall_gather` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '主键 ID',
  `goods_id` bigint DEFAULT NULL COMMENT '商品 ID',
  `shop_id` bigint DEFAULT NULL COMMENT '店铺 ID',
  `gather_price` decimal(10,2) DEFAULT NULL COMMENT '集采价格',
  `gather_quantity` int DEFAULT NULL COMMENT '集采数量',
  `status` tinyint(1) NOT NULL DEFAULT '1' COMMENT '状态：0-禁用 1-启用',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注说明',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_goods_id` (`goods_id`),
  KEY `idx_shop_id` (`shop_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='集采业务';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_mall_goods`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_mall_goods` (
  `id` int unsigned NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `create_time` int unsigned NOT NULL DEFAULT '0' COMMENT '创建时间',
  `update_time` int unsigned NOT NULL DEFAULT '0' COMMENT '修改时间',
  `delete_time` int unsigned NOT NULL DEFAULT '0' COMMENT '删除时间',
  `shop_id` int unsigned NOT NULL COMMENT '店铺id',
  `name` varchar(64) NOT NULL COMMENT '商品名称',
  `code` varchar(32) NOT NULL COMMENT '商品货号',
  `descript` varchar(255) NOT NULL DEFAULT '' COMMENT '商品描述',
  `category_ids` varchar(255) NOT NULL COMMENT '商品分类',
  `shop_category_id` int unsigned NOT NULL DEFAULT '0' COMMENT '商家分类',
  `image` varchar(255) NOT NULL COMMENT '主图',
  `video_is` tinyint unsigned NOT NULL DEFAULT '0' COMMENT '是否有视频',
  `video_from` tinyint unsigned DEFAULT '0' COMMENT '视频来源 1素材库 2链接',
  `video_cover` varchar(255) NOT NULL DEFAULT '' COMMENT '视频封面',
  `video_uri` varchar(255) NOT NULL DEFAULT '' COMMENT '视频地址',
  `sku_type` tinyint unsigned NOT NULL DEFAULT '0' COMMENT '规格类型 0单规格 1多规格',
  `is_express` tinyint unsigned NOT NULL DEFAULT '1' COMMENT '开启快递配送',
  `express_type` tinyint unsigned NOT NULL DEFAULT '0' COMMENT '运费类型 0包邮 1运费模板 2 统一运费',
  `express_tpl_id` int unsigned NOT NULL DEFAULT '0' COMMENT '运费模板ID',
  `express_money` decimal(10,2) unsigned NOT NULL DEFAULT '0.00' COMMENT '统一运费',
  `total_stock` int unsigned NOT NULL DEFAULT '0' COMMENT '总库存',
  `stock_warning` int unsigned NOT NULL DEFAULT '0' COMMENT '库存预警 0不做预警',
  `min_price` decimal(10,2) unsigned NOT NULL DEFAULT '0.00' COMMENT '最小价格',
  `max_price` decimal(10,2) unsigned NOT NULL DEFAULT '0.00' COMMENT '最大价格',
  `min_line_price` decimal(10,2) unsigned NOT NULL DEFAULT '0.00' COMMENT '最小划线价',
  `max_line_price` decimal(10,2) unsigned NOT NULL DEFAULT '0.00' COMMENT '最大划线价',
  `sale_num` int unsigned NOT NULL DEFAULT '0' COMMENT '总销量',
  `virtual_sale_num` int unsigned NOT NULL DEFAULT '0' COMMENT '虚拟销量',
  `visit_num` int unsigned NOT NULL DEFAULT '0' COMMENT '浏览量',
  `virtual_visit_num` int unsigned NOT NULL DEFAULT '0' COMMENT '虚拟浏览量',
  `sale_money` decimal(10,2) unsigned NOT NULL DEFAULT '0.00' COMMENT '总销售额',
  `sort` int unsigned NOT NULL DEFAULT '0' COMMENT '排序',
  `status` tinyint unsigned NOT NULL DEFAULT '0' COMMENT '状态 0仓库 1上架',
  `audit_status` tinyint unsigned NOT NULL DEFAULT '0' COMMENT '审核状态0待审核 1通过 2拒绝',
  `audit_content` varchar(255) NOT NULL DEFAULT '' COMMENT '审核内容',
  `shop_recommend` tinyint unsigned NOT NULL DEFAULT '0' COMMENT '店内推荐 1推荐 0不推荐',
  `is_recycle` tinyint unsigned NOT NULL DEFAULT '0' COMMENT '回收站 1是 0否',
  `content` longtext COMMENT '详情',
  `is_discount` tinyint unsigned NOT NULL DEFAULT '0' COMMENT '会员折扣 1是 0否',
  `discount_type` tinyint unsigned NOT NULL DEFAULT '0' COMMENT '折扣规则 0默认 1单独折扣 2单独价格',
  `is_distribution` tinyint unsigned NOT NULL DEFAULT '0' COMMENT '参与分销 1是 0否',
  `distribution_type` tinyint unsigned NOT NULL DEFAULT '0' COMMENT '分销规则 0默认 1单独佣金比例 2单独设置佣金',
  PRIMARY KEY (`id`),
  KEY `idx_shop` (`shop_id`),
  KEY `idx_status` (`status`,`audit_status`,`is_recycle`),
  KEY `idx_name` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='Mall商品主表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_mall_goods_category`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_mall_goods_category` (
  `id` int unsigned NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `create_time` int unsigned NOT NULL DEFAULT '0' COMMENT '创建时间',
  `update_time` int unsigned NOT NULL DEFAULT '0' COMMENT '修改时间',
  `delete_time` int unsigned NOT NULL DEFAULT '0' COMMENT '删除时间',
  `name` varchar(50) NOT NULL COMMENT '名称',
  `pid` int unsigned NOT NULL DEFAULT '0' COMMENT '父级ID',
  `level` tinyint unsigned NOT NULL DEFAULT '1' COMMENT '等级',
  `image` varchar(255) NOT NULL DEFAULT '' COMMENT '图标',
  `sort` int unsigned NOT NULL DEFAULT '0' COMMENT '排序',
  `is_show` tinyint unsigned NOT NULL DEFAULT '1' COMMENT '是否显示',
  PRIMARY KEY (`id`),
  KEY `idx_pid` (`pid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='Mall商品分类';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_mall_goods_category_index`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_mall_goods_category_index` (
  `id` int unsigned NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `create_time` int unsigned NOT NULL DEFAULT '0',
  `update_time` int unsigned NOT NULL DEFAULT '0',
  `delete_time` int unsigned NOT NULL DEFAULT '0',
  `goods_id` int unsigned NOT NULL COMMENT '商品ID',
  `category_id` int unsigned NOT NULL COMMENT '分类ID',
  PRIMARY KEY (`id`),
  KEY `idx_goods` (`goods_id`),
  KEY `idx_cat` (`category_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='Mall商品分类关联';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_mall_goods_collect`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_mall_goods_collect` (
  `id` int unsigned NOT NULL AUTO_INCREMENT,
  `create_time` int unsigned NOT NULL DEFAULT '0',
  `update_time` int unsigned NOT NULL DEFAULT '0',
  `delete_time` int unsigned NOT NULL DEFAULT '0',
  `user_id` int unsigned NOT NULL,
  `shop_id` int unsigned NOT NULL,
  `goods_id` int unsigned NOT NULL,
  `status` tinyint unsigned NOT NULL DEFAULT '1' COMMENT '1收藏 0取消',
  PRIMARY KEY (`id`),
  KEY `idx_user` (`user_id`),
  KEY `idx_goods` (`goods_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='Mall商品收藏';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_mall_goods_comment`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_mall_goods_comment` (
  `id` int unsigned NOT NULL AUTO_INCREMENT,
  `create_time` int unsigned NOT NULL DEFAULT '0',
  `update_time` int unsigned NOT NULL DEFAULT '0',
  `delete_time` int unsigned NOT NULL DEFAULT '0',
  `shop_id` int unsigned NOT NULL,
  `user_id` int unsigned NOT NULL,
  `order_id` int unsigned NOT NULL DEFAULT '0',
  `order_goods_id` int unsigned NOT NULL DEFAULT '0',
  `goods_id` int unsigned NOT NULL,
  `goods_sku_id` int unsigned NOT NULL DEFAULT '0',
  `score` tinyint unsigned NOT NULL DEFAULT '5',
  `content` varchar(1000) NOT NULL,
  `is_show` tinyint unsigned NOT NULL DEFAULT '1',
  `is_reply` tinyint unsigned NOT NULL DEFAULT '0',
  `reply_content` varchar(1000) NOT NULL DEFAULT '',
  `describe_score` tinyint unsigned NOT NULL DEFAULT '5',
  `service_score` tinyint unsigned NOT NULL DEFAULT '5',
  `logistics_score` tinyint unsigned NOT NULL DEFAULT '5',
  `has_images` tinyint unsigned NOT NULL DEFAULT '0',
  `images` varchar(2000) NOT NULL DEFAULT '',
  PRIMARY KEY (`id`),
  KEY `idx_goods` (`goods_id`),
  KEY `idx_user` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='Mall商品评论';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_mall_goods_distribution`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_mall_goods_distribution` (
  `id` int unsigned NOT NULL AUTO_INCREMENT,
  `create_time` int unsigned NOT NULL DEFAULT '0',
  `update_time` int unsigned NOT NULL DEFAULT '0',
  `delete_time` int unsigned NOT NULL DEFAULT '0',
  `goods_id` int unsigned NOT NULL,
  `goods_sku_id` int unsigned NOT NULL DEFAULT '0',
  `distribution_level_id` int unsigned NOT NULL,
  `alone_self_ratio` decimal(10,2) unsigned NOT NULL DEFAULT '0.00',
  `alone_one_ratio` decimal(10,2) unsigned NOT NULL DEFAULT '0.00',
  `alone_two_ratio` decimal(10,2) unsigned NOT NULL DEFAULT '0.00',
  `alone_self_money` decimal(10,2) unsigned NOT NULL DEFAULT '0.00',
  `alone_one_money` decimal(10,2) unsigned NOT NULL DEFAULT '0.00',
  `alone_two_money` decimal(10,2) unsigned NOT NULL DEFAULT '0.00',
  PRIMARY KEY (`id`),
  KEY `idx_goods` (`goods_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='Mall商品分销关联';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_mall_goods_favorite`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_mall_goods_favorite` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '主键 ID',
  `user_id` bigint NOT NULL COMMENT '用户 ID',
  `goods_id` bigint DEFAULT NULL COMMENT '商品 ID',
  `goods_name` varchar(255) DEFAULT NULL COMMENT '商品名称(冗余)',
  `goods_image` varchar(500) DEFAULT NULL COMMENT '商品图片(冗余)',
  `goods_price` decimal(10,2) DEFAULT NULL COMMENT '商品价格(冗余)',
  `shop_id` bigint DEFAULT NULL COMMENT '店铺 ID',
  `shop_name` varchar(255) DEFAULT NULL COMMENT '店铺名称(冗余)',
  `favorite_type` tinyint NOT NULL DEFAULT '1' COMMENT '收藏类型 1-商品收藏 2-店铺收藏',
  `is_valid` tinyint(1) NOT NULL DEFAULT '1' COMMENT '是否有效 0-失效 1-有效',
  `invalid_reason` varchar(500) DEFAULT NULL COMMENT '失效原因',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否删除 0-未删除 1-已删除',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_goods_id` (`goods_id`),
  KEY `idx_shop_id` (`shop_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='商品收藏';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_mall_goods_image`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_mall_goods_image` (
  `id` int unsigned NOT NULL AUTO_INCREMENT,
  `create_time` int unsigned NOT NULL DEFAULT '0',
  `update_time` int unsigned NOT NULL DEFAULT '0',
  `delete_time` int unsigned NOT NULL DEFAULT '0',
  `goods_id` int unsigned NOT NULL COMMENT '商品ID',
  `image` varchar(255) NOT NULL COMMENT '图片',
  PRIMARY KEY (`id`),
  KEY `idx_goods` (`goods_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='Mall商品轮播图';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_mall_goods_sku`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_mall_goods_sku` (
  `id` int unsigned NOT NULL AUTO_INCREMENT,
  `create_time` int unsigned NOT NULL DEFAULT '0',
  `update_time` int unsigned NOT NULL DEFAULT '0',
  `delete_time` int unsigned NOT NULL DEFAULT '0',
  `shop_id` int unsigned NOT NULL,
  `goods_id` int unsigned NOT NULL,
  `image` varchar(255) NOT NULL DEFAULT '',
  `sku_value_ids` varchar(1000) NOT NULL DEFAULT '[]',
  `sku_value_arr` varchar(10000) NOT NULL,
  `price` decimal(10,2) unsigned NOT NULL,
  `line_price` decimal(10,2) unsigned NOT NULL DEFAULT '0.00',
  `market_price` decimal(10,2) unsigned NOT NULL DEFAULT '0.00',
  `stock` int unsigned NOT NULL DEFAULT '0',
  `weight` decimal(10,3) unsigned NOT NULL DEFAULT '0.000',
  `volume` decimal(10,3) unsigned NOT NULL DEFAULT '0.000',
  `code` varchar(64) NOT NULL DEFAULT '',
  PRIMARY KEY (`id`),
  KEY `idx_goods` (`goods_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='Mall商品SKU';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_mall_goods_sku_name`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_mall_goods_sku_name` (
  `id` int unsigned NOT NULL AUTO_INCREMENT,
  `create_time` int unsigned NOT NULL DEFAULT '0',
  `update_time` int unsigned NOT NULL DEFAULT '0',
  `delete_time` int unsigned NOT NULL DEFAULT '0',
  `goods_id` int unsigned NOT NULL,
  `name` varchar(255) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_goods` (`goods_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='Mall商品SKU规格名';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_mall_home_floor`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_mall_home_floor` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `shop_id` bigint DEFAULT NULL COMMENT '店铺ID(NULL表示平台级)',
  `floor_name` varchar(50) NOT NULL COMMENT '楼层名称',
  `floor_type` varchar(50) NOT NULL COMMENT '楼层类型: banner/seckill/recommend/brand/category',
  `floor_data` text COMMENT '楼层配置JSON',
  `sort` int NOT NULL DEFAULT '0' COMMENT '排序',
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '状态: 0-禁用 1-启用',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否删除',
  PRIMARY KEY (`id`),
  KEY `idx_shop_id` (`shop_id`),
  KEY `idx_floor_type` (`floor_type`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='首页楼层配置表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_mall_inquiry`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_mall_inquiry` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `inquiry_no` varchar(32) NOT NULL COMMENT '询价单号',
  `shop_id` bigint NOT NULL COMMENT '店铺ID',
  `buyer_id` bigint NOT NULL COMMENT '买家ID',
  `buyer_name` varchar(100) DEFAULT NULL COMMENT '买家名称',
  `buyer_phone` varchar(20) DEFAULT NULL COMMENT '买家电话',
  `goods_id` bigint NOT NULL COMMENT '商品ID',
  `goods_name` varchar(200) DEFAULT NULL COMMENT '商品名称',
  `goods_image` varchar(500) DEFAULT NULL COMMENT '商品图片',
  `spec` varchar(200) DEFAULT NULL COMMENT '规格',
  `quantity` int NOT NULL DEFAULT '1' COMMENT '询价数量',
  `expected_price` decimal(10,2) DEFAULT NULL COMMENT '期望价格',
  `remark` text COMMENT '询价备注',
  `quoted_price` decimal(10,2) DEFAULT NULL COMMENT '报价',
  `quote_remark` text COMMENT '报价说明',
  `quote_time` datetime DEFAULT NULL COMMENT '报价时间',
  `status` tinyint NOT NULL DEFAULT '0' COMMENT '状态: 0-待报价 1-已报价 2-已接受 3-已拒绝 4-已过期',
  `expire_time` datetime DEFAULT NULL COMMENT '过期时间',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_inquiry_no` (`inquiry_no`),
  KEY `idx_shop_id` (`shop_id`),
  KEY `idx_buyer_id` (`buyer_id`),
  KEY `idx_goods_id` (`goods_id`),
  KEY `idx_status` (`status`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='询价单表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_mall_invoice`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_mall_invoice` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `invoice_no` varchar(32) NOT NULL COMMENT '发票编号',
  `order_id` bigint NOT NULL COMMENT '订单ID',
  `order_no` varchar(64) NOT NULL COMMENT '订单编号',
  `shop_id` bigint NOT NULL COMMENT '店铺ID',
  `buyer_id` bigint NOT NULL COMMENT '买家ID',
  `invoice_type` tinyint NOT NULL DEFAULT '1' COMMENT '发票类型: 1-增值税专用发票 2-增值税普通发票 3-电子发票',
  `invoice_title` varchar(200) NOT NULL COMMENT '发票抬头',
  `tax_no` varchar(100) DEFAULT NULL COMMENT '纳税人识别号',
  `invoice_amount` decimal(12,2) NOT NULL COMMENT '发票金额',
  `tax_amount` decimal(10,2) NOT NULL DEFAULT '0.00' COMMENT '税额',
  `total_amount` decimal(12,2) NOT NULL COMMENT '价税合计',
  `invoice_content` text COMMENT '开票内容',
  `company_address` varchar(200) DEFAULT NULL COMMENT '公司地址',
  `company_phone` varchar(20) DEFAULT NULL COMMENT '公司电话',
  `bank_name` varchar(100) DEFAULT NULL COMMENT '开户银行',
  `bank_account` varchar(100) DEFAULT NULL COMMENT '银行账号',
  `receiver_name` varchar(50) DEFAULT NULL COMMENT '收件人',
  `receiver_phone` varchar(20) DEFAULT NULL COMMENT '收件电话',
  `receiver_address` varchar(500) DEFAULT NULL COMMENT '收件地址',
  `pdf_url` varchar(500) DEFAULT NULL COMMENT '电子发票PDF',
  `status` tinyint NOT NULL DEFAULT '0' COMMENT '状态: 0-待开具 1-已开具 2-已邮寄 3-已作废',
  `issue_time` datetime DEFAULT NULL COMMENT '开票时间',
  `mail_time` datetime DEFAULT NULL COMMENT '邮寄时间',
  `void_time` datetime DEFAULT NULL COMMENT '作废时间',
  `void_reason` varchar(500) DEFAULT NULL COMMENT '作废原因',
  `remark` text COMMENT '备注',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_invoice_no` (`invoice_no`),
  KEY `idx_order_id` (`order_id`),
  KEY `idx_shop_id` (`shop_id`),
  KEY `idx_buyer_id` (`buyer_id`),
  KEY `idx_status` (`status`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='发票表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_mall_jobs`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_mall_jobs` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT 'id',
  `name` varchar(50) NOT NULL COMMENT '岗位名称',
  `code` varchar(64) NOT NULL COMMENT '岗位编码',
  `sort` int DEFAULT '0' COMMENT '显示顺序',
  `status` tinyint(1) NOT NULL DEFAULT '0' COMMENT '状态（0停用 1正常）',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  `create_time` int NOT NULL COMMENT '创建时间',
  `update_time` int DEFAULT NULL COMMENT '修改时间',
  `delete_time` int DEFAULT NULL COMMENT '删除时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='岗位表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_mall_live_account`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_mall_live_account` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `account_name` varchar(50) NOT NULL COMMENT '账号名称',
  `platform` varchar(30) DEFAULT NULL COMMENT '平台: wechat/douyin/kuaishou',
  `avatar` varchar(255) DEFAULT NULL COMMENT '头像',
  `phone` varchar(20) DEFAULT NULL COMMENT '手机号',
  `description` varchar(255) DEFAULT NULL COMMENT '说明',
  `follower_count` int NOT NULL DEFAULT '0' COMMENT '粉丝数',
  `live_count` int NOT NULL DEFAULT '0' COMMENT '直播次数',
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '状态: 0-禁用 1-正常',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` tinyint(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='直播账号表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_mall_live_activity`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_mall_live_activity` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `title` varchar(100) NOT NULL COMMENT '直播标题',
  `cover_image` varchar(255) DEFAULT NULL COMMENT '封面图',
  `description` text COMMENT '直播说明',
  `anchor_id` bigint DEFAULT NULL COMMENT '主播ID',
  `anchor_name` varchar(50) DEFAULT NULL COMMENT '主播名称',
  `room_id` varchar(50) DEFAULT NULL COMMENT '直播间ID',
  `start_time` datetime DEFAULT NULL COMMENT '开始时间',
  `end_time` datetime DEFAULT NULL COMMENT '结束时间',
  `viewer_count` int NOT NULL DEFAULT '0' COMMENT '观看人数',
  `like_count` int NOT NULL DEFAULT '0' COMMENT '点赞数',
  `status` tinyint NOT NULL DEFAULT '0' COMMENT '状态: 0-未开始 1-直播中 2-已结束 3-已取消',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` tinyint(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `idx_status` (`status`),
  KEY `idx_start_time` (`start_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='直播活动表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_mall_live_product_audit`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_mall_live_product_audit` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `live_id` bigint NOT NULL COMMENT '直播ID',
  `goods_id` bigint NOT NULL COMMENT '商品ID',
  `goods_name` varchar(100) DEFAULT NULL COMMENT '商品名称',
  `goods_image` varchar(255) DEFAULT NULL COMMENT '商品图片',
  `price` decimal(10,2) DEFAULT NULL COMMENT '直播价',
  `audit_status` tinyint NOT NULL DEFAULT '0' COMMENT '审核状态: 0-待审核 1-通过 2-拒绝',
  `audit_remark` varchar(255) DEFAULT NULL COMMENT '审核备注',
  `submit_time` datetime DEFAULT NULL COMMENT '提交时间',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` tinyint(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `idx_live_id` (`live_id`),
  KEY `idx_audit_status` (`audit_status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='直播商品审核表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_mall_marketing_setting`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_mall_marketing_setting` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `setting_key` varchar(50) NOT NULL COMMENT '配置键（如: enable_coupon）',
  `setting_value` varchar(500) NOT NULL COMMENT '配置值（如: 1, true, 100）',
  `setting_type` varchar(20) NOT NULL DEFAULT 'boolean' COMMENT '值类型: boolean/string/int',
  `description` varchar(200) DEFAULT NULL COMMENT '配置说明',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_setting_key` (`setting_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='营销设置配置表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_mall_merchant_account`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_mall_merchant_account` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '关系ID',
  `merchant_id` int NOT NULL COMMENT '商家ID',
  `user_id` bigint NOT NULL COMMENT '统一账号ID',
  `admin_id` int NOT NULL COMMENT '商家中心管理员ID',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_mall_merchant_account_user` (`user_id`),
  UNIQUE KEY `uk_mall_merchant_account_admin` (`admin_id`),
  KEY `idx_mall_merchant_account_merchant` (`merchant_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='统一账号商家权限';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_mall_merchant_entry`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_mall_merchant_entry` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `apply_no` varchar(32) NOT NULL COMMENT '申请单号',
  `merchant_name` varchar(100) NOT NULL COMMENT '商家名称',
  `contact_person` varchar(50) NOT NULL COMMENT '联系人',
  `contact_phone` varchar(20) NOT NULL COMMENT '联系电话',
  `contact_email` varchar(100) DEFAULT NULL COMMENT '联系邮箱',
  `shop_name` varchar(100) NOT NULL COMMENT '店铺名称',
  `shop_type` tinyint NOT NULL COMMENT '店铺类型',
  `business_license` varchar(500) DEFAULT NULL COMMENT '营业执照',
  `license_no` varchar(100) DEFAULT NULL COMMENT '营业执照号',
  `legal_person` varchar(50) DEFAULT NULL COMMENT '法人代表',
  `id_card_no` varchar(50) DEFAULT NULL COMMENT '身份证号',
  `id_card_front` varchar(500) DEFAULT NULL COMMENT '身份证正面',
  `id_card_back` varchar(500) DEFAULT NULL COMMENT '身份证反面',
  `business_categories` text COMMENT '经营类目JSON',
  `province` varchar(50) DEFAULT NULL COMMENT '所在省份',
  `city` varchar(50) DEFAULT NULL COMMENT '所在城市',
  `district` varchar(50) DEFAULT NULL COMMENT '所在区域',
  `address` varchar(200) DEFAULT NULL COMMENT '详细地址',
  `remark` text COMMENT '申请备注',
  `status` tinyint NOT NULL DEFAULT '0' COMMENT '状态: 0-待审核 1-审核通过 2-审核拒绝 3-已入驻',
  `audit_by` bigint DEFAULT NULL COMMENT '审核人',
  `audit_remark` varchar(500) DEFAULT NULL COMMENT '审核备注',
  `audit_time` datetime DEFAULT NULL COMMENT '审核时间',
  `shop_id` bigint DEFAULT NULL COMMENT '关联店铺ID',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_apply_no` (`apply_no`),
  KEY `idx_shop_id` (`shop_id`),
  KEY `idx_status` (`status`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='商家入驻申请表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_mall_mood`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_mall_mood` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `content` text COMMENT '心得内容',
  `images` text COMMENT '图片JSON',
  `user_id` bigint DEFAULT NULL COMMENT '用户ID',
  `user_name` varchar(50) DEFAULT NULL COMMENT '用户名',
  `like_count` int NOT NULL DEFAULT '0',
  `comment_count` int NOT NULL DEFAULT '0',
  `status` tinyint NOT NULL DEFAULT '0' COMMENT '0-待审核 1-已通过 2-已拒绝 3-已下线',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` tinyint(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='心得表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_mall_notice`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_mall_notice` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `shop_id` bigint DEFAULT NULL COMMENT '店铺ID(NULL表示平台级)',
  `title` varchar(200) NOT NULL COMMENT '公告标题',
  `content` text COMMENT '公告内容',
  `notice_type` tinyint NOT NULL DEFAULT '1' COMMENT '类型: 1-快报 2-公告 3-通知',
  `cover_image` varchar(500) DEFAULT NULL COMMENT '封面图片',
  `link_url` varchar(500) DEFAULT NULL COMMENT '详情链接',
  `is_top` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否置顶',
  `view_count` int NOT NULL DEFAULT '0' COMMENT '浏览次数',
  `start_time` datetime DEFAULT NULL COMMENT '开始时间',
  `end_time` datetime DEFAULT NULL COMMENT '结束时间',
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '状态: 0-草稿 1-已发布 2-已下线',
  `create_by` bigint DEFAULT NULL COMMENT '创建人',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否删除',
  PRIMARY KEY (`id`),
  KEY `idx_shop_id` (`shop_id`),
  KEY `idx_notice_type` (`notice_type`),
  KEY `idx_status` (`status`),
  KEY `idx_is_top` (`is_top`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='公告快报表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_mall_notice_setting`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_mall_notice_setting` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '主键 ID',
  `shop_id` bigint DEFAULT NULL COMMENT '店铺 ID',
  `notify_type` varchar(50) NOT NULL DEFAULT '' COMMENT '通知类型',
  `sms_enabled` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否启用短信：0-否 1-是',
  `app_push_enabled` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否启用 APP 推送：0-否 1-是',
  `in_site_enabled` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否启用站内信：0-否 1-是',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_shop_id` (`shop_id`),
  KEY `idx_notify_type` (`notify_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='消息通知设置';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_mall_points_tool`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_mall_points_tool` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `name` varchar(100) NOT NULL COMMENT '商品名称',
  `cover_image` varchar(255) DEFAULT NULL COMMENT '封面图',
  `images` text COMMENT '商品图片JSON数组',
  `description` text COMMENT '商品说明',
  `points_price` int NOT NULL DEFAULT '0' COMMENT '积分价格',
  `cash_price` decimal(10,2) NOT NULL DEFAULT '0.00' COMMENT '现金价格(积分+现金模式)',
  `stock` int NOT NULL DEFAULT '0' COMMENT '库存',
  `exchange_count` int NOT NULL DEFAULT '0' COMMENT '已兑换数量',
  `exchange_limit` int NOT NULL DEFAULT '0' COMMENT '每人限兑数量(0=不限)',
  `sort` int NOT NULL DEFAULT '0' COMMENT '排序',
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '状态: 0-下架 1-上架',
  `start_time` datetime DEFAULT NULL COMMENT '兑换开始时间',
  `end_time` datetime DEFAULT NULL COMMENT '兑换结束时间',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否删除',
  PRIMARY KEY (`id`),
  KEY `idx_status` (`status`),
  KEY `idx_sort` (`sort`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='积分商品表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_mall_quality_deposit`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_mall_quality_deposit` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `deposit_no` varchar(32) NOT NULL COMMENT '质保金单号',
  `shop_id` bigint NOT NULL COMMENT '店铺ID',
  `shop_name` varchar(100) DEFAULT NULL COMMENT '店铺名称',
  `merchant_id` bigint DEFAULT NULL COMMENT '商家ID',
  `merchant_name` varchar(100) DEFAULT NULL COMMENT '商家名称',
  `required_amount` decimal(12,2) NOT NULL COMMENT '应缴金额',
  `paid_amount` decimal(12,2) NOT NULL DEFAULT '0.00' COMMENT '已缴金额',
  `unpaid_amount` decimal(12,2) NOT NULL COMMENT '待缴金额',
  `payment_method` tinyint DEFAULT NULL COMMENT '缴纳方式: 1-银行转账 2-支付宝 3-微信 4-其他',
  `payment_voucher` varchar(500) DEFAULT NULL COMMENT '缴纳凭证',
  `payment_time` datetime DEFAULT NULL COMMENT '缴纳时间',
  `status` tinyint NOT NULL DEFAULT '0' COMMENT '状态: 0-未缴纳 1-已缴纳 2-部分缴纳 3-已退还',
  `refund_amount` decimal(12,2) DEFAULT NULL COMMENT '退还金额',
  `refund_reason` varchar(500) DEFAULT NULL COMMENT '退还原因',
  `refund_time` datetime DEFAULT NULL COMMENT '退还时间',
  `remark` text COMMENT '备注',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_deposit_no` (`deposit_no`),
  KEY `idx_shop_id` (`shop_id`),
  KEY `idx_merchant_id` (`merchant_id`),
  KEY `idx_status` (`status`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='质保金表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_mall_rebate_adjustment`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_mall_rebate_adjustment` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `adjustment_no` varchar(64) NOT NULL COMMENT '调整单号',
  `record_id` bigint DEFAULT NULL COMMENT '关联返利记录ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `adjust_type` tinyint NOT NULL COMMENT '调整类型: 1-增加 2-扣减',
  `adjust_amount` decimal(12,2) NOT NULL COMMENT '调整金额',
  `before_amount` decimal(12,2) DEFAULT NULL COMMENT '调整前金额',
  `after_amount` decimal(12,2) DEFAULT NULL COMMENT '调整后金额',
  `reason` varchar(500) NOT NULL COMMENT '调整原因',
  `audit_status` tinyint NOT NULL DEFAULT '0' COMMENT '审核状态: 0-待审核 1-已通过 2-已拒绝',
  `auditor_id` bigint DEFAULT NULL COMMENT '审核人ID',
  `auditor_name` varchar(50) DEFAULT NULL COMMENT '审核人姓名',
  `audit_time` datetime DEFAULT NULL COMMENT '审核时间',
  `audit_remark` varchar(255) DEFAULT NULL COMMENT '审核备注',
  `create_by` bigint NOT NULL COMMENT '创建人',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_adjustment_no` (`adjustment_no`),
  KEY `idx_record_id` (`record_id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_audit_status` (`audit_status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='返利调整表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_mall_rebate_payment`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_mall_rebate_payment` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `payment_no` varchar(64) NOT NULL COMMENT '发放单号',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `payment_amount` decimal(12,2) NOT NULL COMMENT '发放金额',
  `payment_method` tinyint NOT NULL DEFAULT '1' COMMENT '发放方式: 1-余额 2-银行卡 3-微信 4-支付宝',
  `account_info` varchar(255) DEFAULT NULL COMMENT '收款账户信息',
  `status` tinyint NOT NULL DEFAULT '0' COMMENT '状态: 0-待发放 1-发放中 2-已发放 3-发放失败',
  `pay_time` datetime DEFAULT NULL COMMENT '发放时间',
  `fail_reason` varchar(255) DEFAULT NULL COMMENT '失败原因',
  `operator_id` bigint DEFAULT NULL COMMENT '操作人ID',
  `operator_name` varchar(50) DEFAULT NULL COMMENT '操作人姓名',
  `remark` varchar(255) DEFAULT NULL COMMENT '备注',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_payment_no` (`payment_no`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_status` (`status`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='返利发放表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_mall_rebate_record`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_mall_rebate_record` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `record_no` varchar(64) NOT NULL COMMENT '记录编号',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `order_id` bigint DEFAULT NULL COMMENT '订单ID',
  `order_no` varchar(64) DEFAULT NULL COMMENT '订单编号',
  `order_amount` decimal(12,2) DEFAULT NULL COMMENT '订单金额',
  `rebate_amount` decimal(12,2) NOT NULL COMMENT '返利金额',
  `rule_id` bigint DEFAULT NULL COMMENT '关联规则ID',
  `rebate_type` tinyint NOT NULL COMMENT '返利类型',
  `status` tinyint NOT NULL DEFAULT '0' COMMENT '状态: 0-待审核 1-已审核 2-已发放 3-已取消',
  `audit_by` bigint DEFAULT NULL COMMENT '审核人',
  `audit_time` datetime DEFAULT NULL COMMENT '审核时间',
  `pay_time` datetime DEFAULT NULL COMMENT '发放时间',
  `remark` varchar(255) DEFAULT NULL COMMENT '备注',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_record_no` (`record_no`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_order_id` (`order_id`),
  KEY `idx_rule_id` (`rule_id`),
  KEY `idx_status` (`status`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='返利记录表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_mall_rebate_rule`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_mall_rebate_rule` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `name` varchar(100) NOT NULL COMMENT '规则名称',
  `rebate_type` tinyint NOT NULL DEFAULT '1' COMMENT '返利类型: 1-比例返利 2-固定金额 3-阶梯返利 4-积分返利',
  `calc_type` tinyint NOT NULL DEFAULT '1' COMMENT '计算方式: 1-订单金额 2-商品金额',
  `rebate_value` decimal(12,2) NOT NULL COMMENT '返利值(比例或金额)',
  `min_amount` decimal(12,2) DEFAULT NULL COMMENT '最低订单金额',
  `max_rebate` decimal(12,2) DEFAULT NULL COMMENT '最大返利金额',
  `conditions` text COMMENT '规则条件JSON',
  `priority` int NOT NULL DEFAULT '0' COMMENT '优先级',
  `start_time` datetime DEFAULT NULL COMMENT '生效时间',
  `end_time` datetime DEFAULT NULL COMMENT '失效时间',
  `description` varchar(500) DEFAULT NULL COMMENT '规则说明',
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '状态: 0-禁用 1-启用',
  `create_by` bigint DEFAULT NULL COMMENT '创建人',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_name` (`name`),
  KEY `idx_status` (`status`),
  KEY `idx_rebate_type` (`rebate_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='返利规则表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_mall_recommend_goods`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_mall_recommend_goods` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `shop_id` bigint DEFAULT NULL COMMENT '店铺ID(NULL表示平台级)',
  `goods_id` bigint NOT NULL COMMENT '商品ID',
  `goods_name` varchar(200) DEFAULT NULL COMMENT '商品名称(冗余字段)',
  `goods_image` varchar(500) DEFAULT NULL COMMENT '商品图片(冗余字段)',
  `goods_price` decimal(10,2) DEFAULT NULL COMMENT '商品价格(冗余字段)',
  `market_price` decimal(10,2) DEFAULT NULL COMMENT '市场价格(冗余字段)',
  `recommend_type` tinyint NOT NULL DEFAULT '1' COMMENT '推荐类型: 1-热门推荐 2-新品推荐 3-猜你喜欢 4-人气榜单',
  `position` varchar(50) NOT NULL DEFAULT 'home' COMMENT '位置: home-首页 category-分类页 detail-详情页',
  `sort` int NOT NULL DEFAULT '0' COMMENT '排序',
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '状态: 0-禁用 1-启用',
  `start_time` datetime DEFAULT NULL COMMENT '开始时间',
  `end_time` datetime DEFAULT NULL COMMENT '结束时间',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否删除',
  PRIMARY KEY (`id`),
  KEY `idx_shop_id` (`shop_id`),
  KEY `idx_goods_id` (`goods_id`),
  KEY `idx_recommend_type` (`recommend_type`),
  KEY `idx_position` (`position`),
  KEY `idx_status` (`status`),
  KEY `idx_sort` (`sort`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='推荐商品表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_mall_review`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_mall_review` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `target_type` varchar(20) NOT NULL COMMENT '评论目标类型: selection/mood',
  `target_id` bigint NOT NULL COMMENT '目标ID',
  `user_id` bigint DEFAULT NULL COMMENT '用户ID',
  `user_name` varchar(50) DEFAULT NULL COMMENT '用户名',
  `content` text COMMENT '评论内容',
  `parent_id` bigint DEFAULT NULL COMMENT '父评论ID',
  `like_count` int NOT NULL DEFAULT '0',
  `status` tinyint NOT NULL DEFAULT '0' COMMENT '0-待审核 1-已通过 2-已拒绝',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` tinyint(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `idx_target` (`target_type`,`target_id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='评论表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_mall_risk_alert`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_mall_risk_alert` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `shop_id` bigint DEFAULT NULL COMMENT '店铺ID',
  `alert_no` varchar(64) NOT NULL COMMENT '预警编号',
  `rule_id` bigint DEFAULT NULL COMMENT '关联规则ID',
  `alert_type` tinyint NOT NULL COMMENT '预警类型',
  `alert_level` tinyint NOT NULL COMMENT '预警等级',
  `alert_title` varchar(200) NOT NULL COMMENT '预警标题',
  `alert_content` text NOT NULL COMMENT '预警内容',
  `alert_data` text COMMENT '预警数据JSON',
  `target_type` varchar(50) DEFAULT NULL COMMENT '目标类型: goods/order/user/account',
  `target_id` bigint DEFAULT NULL COMMENT '目标ID',
  `status` tinyint NOT NULL DEFAULT '0' COMMENT '状态: 0-待处理 1-处理中 2-已处理 3-已忽略',
  `handler_id` bigint DEFAULT NULL COMMENT '处理人ID',
  `handler_name` varchar(50) DEFAULT NULL COMMENT '处理人姓名',
  `handle_time` datetime DEFAULT NULL COMMENT '处理时间',
  `handle_result` varchar(500) DEFAULT NULL COMMENT '处理结果',
  `handle_remark` varchar(500) DEFAULT NULL COMMENT '处理备注',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_alert_no` (`alert_no`),
  KEY `idx_shop_id` (`shop_id`),
  KEY `idx_rule_id` (`rule_id`),
  KEY `idx_alert_type` (`alert_type`),
  KEY `idx_status` (`status`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='预警记录表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_mall_risk_monitor`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_mall_risk_monitor` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `shop_id` bigint DEFAULT NULL COMMENT '店铺ID',
  `monitor_type` tinyint NOT NULL COMMENT '监控类型: 1-实时监控 2-日报 3-周报 4-月报',
  `monitor_date` date NOT NULL COMMENT '监控日期',
  `alert_count` int NOT NULL DEFAULT '0' COMMENT '预警数量',
  `pending_count` int NOT NULL DEFAULT '0' COMMENT '待处理数量',
  `handled_count` int NOT NULL DEFAULT '0' COMMENT '已处理数量',
  `ignored_count` int NOT NULL DEFAULT '0' COMMENT '已忽略数量',
  `high_level_count` int NOT NULL DEFAULT '0' COMMENT '高等级预警数量',
  `statistics_data` text COMMENT '统计数据JSON',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否删除',
  PRIMARY KEY (`id`),
  KEY `idx_shop_id` (`shop_id`),
  KEY `idx_monitor_date` (`monitor_date`),
  KEY `idx_monitor_type` (`monitor_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='风控监控表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_mall_risk_rule`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_mall_risk_rule` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `shop_id` bigint DEFAULT NULL COMMENT '店铺ID',
  `rule_name` varchar(100) NOT NULL COMMENT '规则名称',
  `rule_type` tinyint NOT NULL COMMENT '规则类型: 1-库存预警 2-价格异常 3-订单异常 4-交易风险 5-账户安全',
  `rule_config` text NOT NULL COMMENT '规则配置JSON',
  `alert_level` tinyint NOT NULL DEFAULT '2' COMMENT '预警等级: 1-低 2-中 3-高 4-紧急',
  `alert_method` varchar(100) DEFAULT NULL COMMENT '预警方式(多选JSON): 系统消息/短信/邮件/钉钉',
  `alert_users` varchar(500) DEFAULT NULL COMMENT '预警接收人IDs',
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '状态: 0-禁用 1-启用',
  `description` varchar(500) DEFAULT NULL COMMENT '规则说明',
  `create_by` bigint DEFAULT NULL COMMENT '创建人',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否删除',
  PRIMARY KEY (`id`),
  KEY `idx_shop_id` (`shop_id`),
  KEY `idx_rule_type` (`rule_type`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='风控规则表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_mall_selection`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_mall_selection` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `title` varchar(100) NOT NULL COMMENT '标题',
  `cover_image` varchar(255) DEFAULT NULL COMMENT '封面',
  `content` text COMMENT '内容',
  `category_id` bigint DEFAULT NULL COMMENT '分类ID',
  `author_id` bigint DEFAULT NULL COMMENT '作者ID',
  `author_name` varchar(50) DEFAULT NULL COMMENT '作者名',
  `view_count` int NOT NULL DEFAULT '0' COMMENT '浏览数',
  `like_count` int NOT NULL DEFAULT '0' COMMENT '点赞数',
  `status` tinyint NOT NULL DEFAULT '0' COMMENT '0-草稿 1-已发布 2-已下线',
  `publish_time` datetime DEFAULT NULL COMMENT '发布时间',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` tinyint(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `idx_category_id` (`category_id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='选题表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_mall_service_config`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_mall_service_config` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '主键 ID',
  `shop_id` bigint DEFAULT NULL COMMENT '店铺 ID',
  `service_qq` varchar(50) DEFAULT NULL COMMENT '客服 QQ',
  `service_wechat` varchar(50) DEFAULT NULL COMMENT '客服微信',
  `service_phone` varchar(50) DEFAULT NULL COMMENT '客服电话',
  `service_email` varchar(100) DEFAULT NULL COMMENT '客服邮箱',
  `service_hours` varchar(100) DEFAULT NULL COMMENT '服务时间',
  `auto_reply` text COMMENT '自动回复内容',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_shop_id` (`shop_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='客服配置';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_mall_shop`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_mall_shop` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `shop_no` varchar(32) NOT NULL COMMENT '店铺编号',
  `shop_name` varchar(100) NOT NULL COMMENT '店铺名称',
  `shop_logo` varchar(500) DEFAULT NULL COMMENT '店铺Logo',
  `shop_type` tinyint NOT NULL DEFAULT '1' COMMENT '店铺类型: 1-自营 2-入驻商家',
  `merchant_id` bigint DEFAULT NULL COMMENT '商家ID',
  `merchant_name` varchar(100) DEFAULT NULL COMMENT '商家名称',
  `contact_person` varchar(50) DEFAULT NULL COMMENT '联系人',
  `contact_phone` varchar(20) DEFAULT NULL COMMENT '联系电话',
  `contact_email` varchar(100) DEFAULT NULL COMMENT '联系邮箱',
  `level_id` bigint DEFAULT NULL COMMENT '店铺等级ID',
  `province` varchar(50) DEFAULT NULL COMMENT '所在省份',
  `city` varchar(50) DEFAULT NULL COMMENT '所在城市',
  `district` varchar(50) DEFAULT NULL COMMENT '所在区域',
  `address` varchar(200) DEFAULT NULL COMMENT '详细地址',
  `business_license` varchar(500) DEFAULT NULL COMMENT '营业执照',
  `license_no` varchar(100) DEFAULT NULL COMMENT '营业执照号',
  `legal_person` varchar(50) DEFAULT NULL COMMENT '法人代表',
  `id_card_no` varchar(50) DEFAULT NULL COMMENT '身份证号',
  `id_card_front` varchar(500) DEFAULT NULL COMMENT '身份证正面',
  `id_card_back` varchar(500) DEFAULT NULL COMMENT '身份证反面',
  `bank_account` varchar(100) DEFAULT NULL COMMENT '银行账户',
  `bank_name` varchar(100) DEFAULT NULL COMMENT '开户行',
  `account_name` varchar(100) DEFAULT NULL COMMENT '账户名',
  `settlement_rate` decimal(5,2) NOT NULL DEFAULT '0.00' COMMENT '结算费率%',
  `quality_deposit` decimal(12,2) NOT NULL DEFAULT '0.00' COMMENT '质保金金额',
  `paid_deposit` decimal(12,2) NOT NULL DEFAULT '0.00' COMMENT '已缴质保金',
  `business_categories` text COMMENT '经营类目JSON',
  `description` text COMMENT '店铺简介',
  `status` tinyint NOT NULL DEFAULT '0' COMMENT '状态: 0-待审核 1-正常 2-冻结 3-关闭',
  `audit_remark` varchar(500) DEFAULT NULL COMMENT '审核备注',
  `audit_time` datetime DEFAULT NULL COMMENT '审核时间',
  `open_time` datetime DEFAULT NULL COMMENT '开店时间',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_shop_no` (`shop_no`),
  KEY `idx_merchant_id` (`merchant_id`),
  KEY `idx_level_id` (`level_id`),
  KEY `idx_status` (`status`),
  KEY `idx_shop_type` (`shop_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='店铺信息表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_mall_shop_category`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_mall_shop_category` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `name` varchar(100) NOT NULL COMMENT '分类名称',
  `icon` varchar(500) DEFAULT NULL COMMENT '分类图标',
  `sort` int NOT NULL DEFAULT '0' COMMENT '排序（数字越大越靠前）',
  `shop_count` int NOT NULL DEFAULT '0' COMMENT '关联店铺数量',
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '状态: 0-禁用 1-启用',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否删除: 0-否 1-是',
  PRIMARY KEY (`id`),
  KEY `idx_status` (`status`),
  KEY `idx_sort` (`sort`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='商家经营类目表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_mall_shop_level`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_mall_shop_level` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `level_name` varchar(50) NOT NULL COMMENT '等级名称',
  `level` int NOT NULL COMMENT '等级值',
  `min_sales` decimal(12,2) NOT NULL DEFAULT '0.00' COMMENT '最低销售额要求',
  `commission_rate` decimal(5,2) NOT NULL DEFAULT '0.00' COMMENT '佣金比例%',
  `special_rights` text COMMENT '特殊权益JSON',
  `icon` varchar(500) DEFAULT NULL COMMENT '等级图标',
  `description` varchar(500) DEFAULT NULL COMMENT '等级说明',
  `sort` int NOT NULL DEFAULT '0' COMMENT '排序',
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '状态: 0-禁用 1-启用',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_level` (`level`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='店铺等级表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_mall_shop_menu`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_mall_shop_menu` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `pid` bigint NOT NULL DEFAULT '0' COMMENT '父级ID，0为顶级',
  `name` varchar(100) NOT NULL COMMENT '菜单名称',
  `menu_type` tinyint NOT NULL DEFAULT '0' COMMENT '菜单类型: 0-目录 1-菜单 2-按钮',
  `icon` varchar(200) DEFAULT NULL COMMENT '菜单图标',
  `permission` varchar(200) DEFAULT NULL COMMENT '权限标识，如 shop:goods:list',
  `path` varchar(200) DEFAULT NULL COMMENT '路由地址',
  `component` varchar(200) DEFAULT NULL COMMENT '组件路径',
  `sort` int NOT NULL DEFAULT '0' COMMENT '排序',
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '状态: 0-禁用 1-启用',
  `is_show` tinyint(1) NOT NULL DEFAULT '1' COMMENT '是否显示: 0-隐藏 1-显示',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否删除: 0-否 1-是',
  PRIMARY KEY (`id`),
  KEY `idx_pid` (`pid`),
  KEY `idx_status` (`status`),
  KEY `idx_sort` (`sort`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='商家菜单权限表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_mall_shopping_cart`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_mall_shopping_cart` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '主键 ID',
  `user_id` bigint NOT NULL COMMENT '用户 ID',
  `shop_id` bigint DEFAULT NULL COMMENT '店铺 ID',
  `shop_name` varchar(255) DEFAULT NULL COMMENT '店铺名称',
  `goods_id` bigint DEFAULT NULL COMMENT '商品 ID',
  `goods_name` varchar(255) DEFAULT NULL COMMENT '商品名称',
  `goods_image` varchar(500) DEFAULT NULL COMMENT '商品图片',
  `sku_id` bigint DEFAULT NULL COMMENT 'SKU ID',
  `sku_spec` varchar(500) DEFAULT NULL COMMENT 'SKU 规格信息',
  `price` decimal(10,2) DEFAULT NULL COMMENT '商品价格',
  `quantity` int NOT NULL DEFAULT '1' COMMENT '商品数量',
  `selected` tinyint(1) NOT NULL DEFAULT '1' COMMENT '是否选中 0-未选中 1-已选中',
  `is_valid` tinyint(1) NOT NULL DEFAULT '1' COMMENT '是否有效 0-失效 1-有效',
  `invalid_reason` varchar(500) DEFAULT NULL COMMENT '失效原因',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否删除 0-未删除 1-已删除',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_goods_id` (`goods_id`),
  KEY `idx_shop_id` (`shop_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='购物车';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_mall_stock`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_mall_stock` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `warehouse_id` bigint NOT NULL COMMENT '仓库ID',
  `goods_id` bigint NOT NULL COMMENT '商品ID',
  `sku_id` bigint NOT NULL COMMENT 'SKU ID',
  `goods_name` varchar(200) DEFAULT NULL COMMENT '商品名称',
  `sku_spec` varchar(500) DEFAULT NULL COMMENT 'SKU规格',
  `quantity` int NOT NULL DEFAULT '0' COMMENT '库存数量',
  `available_qty` int NOT NULL DEFAULT '0' COMMENT '可用数量',
  `locked_qty` int NOT NULL DEFAULT '0' COMMENT '锁定数量',
  `warning_qty` int DEFAULT '10' COMMENT '预警数量',
  `cost_price` decimal(12,2) DEFAULT NULL COMMENT '成本价',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_warehouse_sku` (`warehouse_id`,`sku_id`),
  KEY `idx_goods_id` (`goods_id`),
  KEY `idx_sku_id` (`sku_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='库存表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_mall_stock_inbound`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_mall_stock_inbound` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `shop_id` bigint DEFAULT NULL COMMENT '店铺ID',
  `inbound_no` varchar(64) NOT NULL COMMENT '入库单号',
  `warehouse_id` bigint NOT NULL COMMENT '仓库ID',
  `inbound_type` tinyint NOT NULL COMMENT '入库类型: 1-采购入库 2-退货入库 3-调拨入库 4-盘盈入库',
  `supplier_id` bigint DEFAULT NULL COMMENT '供应商ID',
  `total_quantity` int NOT NULL DEFAULT '0' COMMENT '总数量',
  `total_amount` decimal(12,2) DEFAULT NULL COMMENT '总金额',
  `status` tinyint NOT NULL DEFAULT '0' COMMENT '状态: 0-待确认 1-已确认 2-已取消',
  `confirm_time` datetime DEFAULT NULL COMMENT '确认时间',
  `operator_id` bigint DEFAULT NULL COMMENT '操作人ID',
  `operator_name` varchar(50) DEFAULT NULL COMMENT '操作人姓名',
  `remark` varchar(255) DEFAULT NULL COMMENT '备注',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_inbound_no` (`inbound_no`),
  KEY `idx_shop_id` (`shop_id`),
  KEY `idx_warehouse_id` (`warehouse_id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='入库单表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_mall_stock_inventory`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_mall_stock_inventory` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `shop_id` bigint DEFAULT NULL COMMENT '店铺ID',
  `inventory_no` varchar(64) NOT NULL COMMENT '盘点单号',
  `warehouse_id` bigint NOT NULL COMMENT '仓库ID',
  `inventory_type` tinyint NOT NULL DEFAULT '1' COMMENT '盘点类型: 1-全部盘点 2-分类盘点 3-抽样盘点',
  `status` tinyint NOT NULL DEFAULT '0' COMMENT '状态: 0-待盘点 1-盘点中 2-待审核 3-已完成 4-已取消',
  `profit_quantity` int DEFAULT '0' COMMENT '盘盈数量',
  `loss_quantity` int DEFAULT '0' COMMENT '盘亏数量',
  `start_time` datetime DEFAULT NULL COMMENT '开始时间',
  `end_time` datetime DEFAULT NULL COMMENT '结束时间',
  `auditor_id` bigint DEFAULT NULL COMMENT '审核人ID',
  `auditor_name` varchar(50) DEFAULT NULL COMMENT '审核人姓名',
  `audit_time` datetime DEFAULT NULL COMMENT '审核时间',
  `audit_remark` varchar(255) DEFAULT NULL COMMENT '审核备注',
  `operator_id` bigint DEFAULT NULL COMMENT '操作人ID',
  `operator_name` varchar(50) DEFAULT NULL COMMENT '操作人姓名',
  `remark` varchar(255) DEFAULT NULL COMMENT '备注',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_inventory_no` (`inventory_no`),
  KEY `idx_shop_id` (`shop_id`),
  KEY `idx_warehouse_id` (`warehouse_id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='盘点单表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_mall_stock_outbound`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_mall_stock_outbound` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `shop_id` bigint DEFAULT NULL COMMENT '店铺ID',
  `outbound_no` varchar(64) NOT NULL COMMENT '出库单号',
  `warehouse_id` bigint NOT NULL COMMENT '仓库ID',
  `outbound_type` tinyint NOT NULL COMMENT '出库类型: 1-销售出库 2-调拨出库 3-盘亏出库 4-报损出库',
  `order_id` bigint DEFAULT NULL COMMENT '关联订单ID',
  `total_quantity` int NOT NULL DEFAULT '0' COMMENT '总数量',
  `status` tinyint NOT NULL DEFAULT '0' COMMENT '状态: 0-待确认 1-已确认 2-已取消',
  `confirm_time` datetime DEFAULT NULL COMMENT '确认时间',
  `operator_id` bigint DEFAULT NULL COMMENT '操作人ID',
  `operator_name` varchar(50) DEFAULT NULL COMMENT '操作人姓名',
  `remark` varchar(255) DEFAULT NULL COMMENT '备注',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_outbound_no` (`outbound_no`),
  KEY `idx_shop_id` (`shop_id`),
  KEY `idx_warehouse_id` (`warehouse_id`),
  KEY `idx_order_id` (`order_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='出库单表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_mall_stock_reservation`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_mall_stock_reservation` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `order_no` varchar(64) NOT NULL COMMENT '商城订单号',
  `warehouse_id` bigint NOT NULL COMMENT '仓库ID',
  `sku_id` bigint NOT NULL COMMENT 'SKU ID',
  `quantity` int NOT NULL COMMENT '预占数量',
  `status` varchar(20) NOT NULL COMMENT 'PENDING/RESERVED/COMMITTED/RELEASED/RETURNED',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_stock_reservation_order_sku` (`order_no`,`warehouse_id`,`sku_id`),
  KEY `idx_stock_reservation_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='商城订单库存预占明细';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_mall_stock_transfer`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_mall_stock_transfer` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `shop_id` bigint DEFAULT NULL COMMENT '店铺ID',
  `transfer_no` varchar(64) NOT NULL COMMENT '调拨单号',
  `from_warehouse_id` bigint NOT NULL COMMENT '调出仓库ID',
  `to_warehouse_id` bigint NOT NULL COMMENT '调入仓库ID',
  `total_quantity` int NOT NULL DEFAULT '0' COMMENT '总数量',
  `status` tinyint NOT NULL DEFAULT '0' COMMENT '状态: 0-待审核 1-已审核 2-调拨中 3-已完成 4-已取消',
  `auditor_id` bigint DEFAULT NULL COMMENT '审核人ID',
  `auditor_name` varchar(50) DEFAULT NULL COMMENT '审核人姓名',
  `audit_time` datetime DEFAULT NULL COMMENT '审核时间',
  `audit_remark` varchar(255) DEFAULT NULL COMMENT '审核备注',
  `complete_time` datetime DEFAULT NULL COMMENT '完成时间',
  `operator_id` bigint DEFAULT NULL COMMENT '操作人ID',
  `operator_name` varchar(50) DEFAULT NULL COMMENT '操作人姓名',
  `remark` varchar(255) DEFAULT NULL COMMENT '备注',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_transfer_no` (`transfer_no`),
  KEY `idx_shop_id` (`shop_id`),
  KEY `idx_from_warehouse` (`from_warehouse_id`),
  KEY `idx_to_warehouse` (`to_warehouse_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='调拨单表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_mall_store`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_mall_store` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '门店ID',
  `merchant_id` int NOT NULL COMMENT '所属商家ID',
  `store_no` varchar(32) NOT NULL COMMENT '门店编号',
  `store_name` varchar(100) NOT NULL COMMENT '门店名称',
  `contact_person` varchar(50) DEFAULT NULL COMMENT '门店联系人',
  `contact_phone` varchar(20) DEFAULT NULL COMMENT '门店联系电话',
  `province` varchar(50) DEFAULT NULL COMMENT '省份',
  `city` varchar(50) DEFAULT NULL COMMENT '城市',
  `district` varchar(50) DEFAULT NULL COMMENT '区县',
  `address` varchar(255) DEFAULT NULL COMMENT '详细地址',
  `business_status` tinyint NOT NULL DEFAULT '1' COMMENT '营业状态: 0-停业 1-营业',
  `rider_delivery_enabled` tinyint NOT NULL DEFAULT '0' COMMENT '骑手配送: 0-未启用 1-已启用',
  `pos_enabled` tinyint NOT NULL DEFAULT '0' COMMENT '收银系统: 0-未启用 1-已启用',
  `deleted` tinyint NOT NULL DEFAULT '0' COMMENT '逻辑删除: 0-否 1-是',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_mall_store_no` (`store_no`),
  KEY `idx_mall_store_merchant` (`merchant_id`),
  KEY `idx_mall_store_status` (`business_status`,`deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='商家门店主数据';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_mall_style`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_mall_style` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '主键 ID',
  `shop_id` bigint DEFAULT NULL COMMENT '店铺 ID（NULL 表示平台级）',
  `theme_color` varchar(20) DEFAULT NULL COMMENT '主题色',
  `style_config` longtext COMMENT '风格配置 JSON',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_shop_id` (`shop_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='商城风格配置';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_mall_system_menu`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_mall_system_menu` (
  `id` int unsigned NOT NULL AUTO_INCREMENT COMMENT '主键',
  `pid` int unsigned NOT NULL DEFAULT '0' COMMENT '上级菜单',
  `type` char(2) NOT NULL DEFAULT '' COMMENT '权限类型: M=目录，C=菜单，A=按钮',
  `name` varchar(100) NOT NULL DEFAULT '' COMMENT '菜单名称',
  `icon` varchar(100) NOT NULL DEFAULT '' COMMENT '菜单图标',
  `sort` smallint unsigned NOT NULL DEFAULT '0' COMMENT '菜单排序',
  `perms` varchar(100) NOT NULL DEFAULT '' COMMENT '权限标识',
  `paths` varchar(100) NOT NULL DEFAULT '' COMMENT '路由地址',
  `component` varchar(200) NOT NULL DEFAULT '' COMMENT '前端组件',
  `selected` varchar(200) NOT NULL DEFAULT '' COMMENT '选中路径',
  `params` varchar(200) NOT NULL DEFAULT '' COMMENT '路由参数',
  `is_cache` tinyint unsigned NOT NULL DEFAULT '0' COMMENT '是否缓存: 0=否, 1=是',
  `is_show` tinyint unsigned NOT NULL DEFAULT '1' COMMENT '是否显示: 0=否, 1=是',
  `is_disable` tinyint unsigned NOT NULL DEFAULT '0' COMMENT '是否禁用: 0=否, 1=是',
  `create_time` int unsigned NOT NULL DEFAULT '0' COMMENT '创建时间',
  `update_time` int unsigned NOT NULL DEFAULT '0' COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_pid` (`pid`),
  KEY `idx_perms` (`perms`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='系统菜单表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_mall_system_role`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_mall_system_role` (
  `id` int unsigned NOT NULL AUTO_INCREMENT,
  `name` varchar(16) NOT NULL DEFAULT '' COMMENT '名称',
  `desc` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT '' COMMENT '描述',
  `sort` int DEFAULT '0' COMMENT '排序',
  `create_time` int DEFAULT NULL COMMENT '创建时间',
  `update_time` int DEFAULT NULL COMMENT '更新时间',
  `delete_time` int DEFAULT NULL COMMENT '删除时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='角色表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_mall_system_role_menu`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_mall_system_role_menu` (
  `role_id` int unsigned NOT NULL DEFAULT '0' COMMENT '角色ID',
  `menu_id` int unsigned NOT NULL DEFAULT '0' COMMENT '菜单ID',
  PRIMARY KEY (`role_id`,`menu_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='角色菜单关系表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_mall_topic_category`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_mall_topic_category` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `name` varchar(50) NOT NULL COMMENT '分类名称',
  `icon` varchar(255) DEFAULT NULL COMMENT '图标',
  `sort` int NOT NULL DEFAULT '0' COMMENT '排序',
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '0-禁用 1-正常',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` tinyint(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='话题分类表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_mall_user_address`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_mall_user_address` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '主键 ID',
  `user_id` bigint NOT NULL COMMENT '用户 ID',
  `receiver_name` varchar(50) NOT NULL DEFAULT '' COMMENT '收货人姓名',
  `receiver_phone` varchar(20) NOT NULL DEFAULT '' COMMENT '收货人电话',
  `province_code` varchar(20) DEFAULT NULL COMMENT '省份编码',
  `province_name` varchar(50) DEFAULT NULL COMMENT '省份名称',
  `city_code` varchar(20) DEFAULT NULL COMMENT '城市编码',
  `city_name` varchar(50) DEFAULT NULL COMMENT '城市名称',
  `district_code` varchar(20) DEFAULT NULL COMMENT '区县编码',
  `district_name` varchar(50) DEFAULT NULL COMMENT '区县名称',
  `detail_address` varchar(255) NOT NULL DEFAULT '' COMMENT '详细地址',
  `address_tag` varchar(20) DEFAULT NULL COMMENT '地址标签: home-家 company-公司 school-学校',
  `is_default` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否默认地址 0-否 1-是',
  `post_code` varchar(20) DEFAULT NULL COMMENT '邮政编码',
  `longitude` double DEFAULT NULL COMMENT '经度',
  `latitude` double DEFAULT NULL COMMENT '纬度',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否删除 0-未删除 1-已删除',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_is_default` (`is_default`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='用户收货地址';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_mall_warehouse`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_mall_warehouse` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `shop_id` bigint DEFAULT NULL COMMENT '店铺ID',
  `warehouse_code` varchar(32) NOT NULL COMMENT '仓库编码',
  `warehouse_name` varchar(100) NOT NULL COMMENT '仓库名称',
  `warehouse_type` tinyint NOT NULL DEFAULT '1' COMMENT '仓库类型: 1-自营仓 2-第三方仓 3-虚拟仓',
  `province` varchar(50) DEFAULT NULL COMMENT '省份',
  `city` varchar(50) DEFAULT NULL COMMENT '城市',
  `district` varchar(50) DEFAULT NULL COMMENT '区县',
  `address` varchar(255) DEFAULT NULL COMMENT '详细地址',
  `contact_name` varchar(50) DEFAULT NULL COMMENT '联系人',
  `contact_phone` varchar(20) DEFAULT NULL COMMENT '联系电话',
  `is_default` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否默认仓库',
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '状态: 0-禁用 1-启用',
  `remark` varchar(255) DEFAULT NULL COMMENT '备注',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_warehouse_code` (`warehouse_code`),
  KEY `idx_shop_id` (`shop_id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='仓库表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_market_developer`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_market_developer` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL COMMENT '账号 ID',
  `developer_name` varchar(64) NOT NULL COMMENT '开发者名称',
  `email` varchar(128) DEFAULT NULL,
  `total_plugins` int NOT NULL DEFAULT '0',
  `total_income_cents` bigint NOT NULL DEFAULT '0',
  `settled_income_cents` bigint NOT NULL DEFAULT '0',
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '1 正常 0 禁用',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='插件市场-开发者';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_market_plugin`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_market_plugin` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `plugin_code` varchar(64) NOT NULL COMMENT '插件唯一编码',
  `name` varchar(128) NOT NULL COMMENT '插件名称',
  `author_id` bigint NOT NULL DEFAULT '1' COMMENT '开发者/作者 ID，平台自营为 1',
  `category` varchar(64) DEFAULT NULL COMMENT '分类',
  `description` text COMMENT '详细描述',
  `short_desc` varchar(256) DEFAULT NULL COMMENT '一句话简介',
  `icon_url` varchar(512) DEFAULT NULL COMMENT '图标地址',
  `screenshots_json` text COMMENT '截图 JSON 数组',
  `latest_version` varchar(32) DEFAULT NULL COMMENT '最新版本号',
  `license_type` enum('FREE','PAID','TRIAL') NOT NULL DEFAULT 'FREE' COMMENT '授权类型',
  `price_cents` int NOT NULL DEFAULT '0' COMMENT '价格（分），FREE 为 0',
  `trial_days` int NOT NULL DEFAULT '0' COMMENT '试用天数，TRIAL 有效',
  `download_count` int NOT NULL DEFAULT '0' COMMENT '下载次数',
  `install_count` int NOT NULL DEFAULT '0' COMMENT '安装次数',
  `rating_avg` decimal(3,2) NOT NULL DEFAULT '0.00' COMMENT '平均评分',
  `rating_count` int NOT NULL DEFAULT '0' COMMENT '评分人数',
  `status` tinyint NOT NULL DEFAULT '0' COMMENT '0草稿 1审核中 2已上架 3已下架 4已拒绝',
  `min_framework_version` varchar(32) DEFAULT NULL COMMENT '最低框架版本',
  `tags_json` text COMMENT '标签 JSON 数组',
  `terminals_json` text COMMENT '适用端 JSON 数组',
  `dependencies_json` text COMMENT '依赖插件 JSON 数组',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` tinyint(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_plugin_code` (`plugin_code`),
  KEY `idx_status_category` (`status`,`category`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='插件市场-插件仓库';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_market_plugin_license`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_market_plugin_license` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `plugin_code` varchar(64) NOT NULL,
  `buyer_id` bigint NOT NULL COMMENT '购买方/站点 ID',
  `instance_id` varchar(128) DEFAULT NULL COMMENT '绑定实例（域名/机器码）',
  `license_key` varchar(256) NOT NULL COMMENT 'License Key',
  `license_type` enum('FREE','PAID','TRIAL') NOT NULL COMMENT '授权类型',
  `max_instances` int NOT NULL DEFAULT '1' COMMENT '最大实例数',
  `expire_time` datetime DEFAULT NULL COMMENT '过期时间，NULL=永久',
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '1 有效 0 失效',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_license_key` (`license_key`),
  KEY `idx_buyer_plugin` (`buyer_id`,`plugin_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='插件市场-授权许可';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_market_plugin_order`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_market_plugin_order` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `order_no` varchar(64) NOT NULL COMMENT '插件订单号（业务主键，唯一）',
  `plugin_code` varchar(64) NOT NULL COMMENT '插件编码',
  `version` varchar(32) DEFAULT NULL COMMENT '购买目标版本，为空表示最新版本',
  `license_type` varchar(16) NOT NULL COMMENT 'PAID 付费 / TRIAL 试用',
  `valid_days` int DEFAULT NULL COMMENT '授权有效天数，NULL 或 <=0 表示永久',
  `amount` decimal(12,2) NOT NULL DEFAULT '0.00' COMMENT '应付金额（元）',
  `currency` varchar(8) NOT NULL DEFAULT 'CNY' COMMENT '币种',
  `buyer_id` bigint NOT NULL DEFAULT '1' COMMENT '购买方站点 ID，平台自营为 1',
  `instance_id` varchar(128) DEFAULT NULL COMMENT '站点实例（域名），支付成功后授权绑定到此实例',
  `pay_method` varchar(32) DEFAULT NULL COMMENT '支付方式：WXPAY / ALIPAY 等',
  `terminal` varchar(16) DEFAULT NULL COMMENT '终端：PC / H5 / APP',
  `cashier_no` varchar(64) DEFAULT NULL COMMENT '收银台流水号（order 侧）',
  `pay_order_id` varchar(64) DEFAULT NULL COMMENT '渠道支付单号',
  `pay_data` text COMMENT '下单返回的支付参数（二维码/表单）JSON',
  `status` tinyint NOT NULL DEFAULT '0' COMMENT '0待支付 1已支付 2已关闭 3已退款',
  `paid_time` datetime DEFAULT NULL COMMENT '支付完成时间',
  `expire_time` datetime DEFAULT NULL COMMENT '支付截止时间（超时未付自动关闭）',
  `license_id` bigint DEFAULT NULL COMMENT '支付成功后自动签发的授权 ID',
  `license_key` varchar(128) DEFAULT NULL COMMENT '自动签发的授权码（冗余便于展示）',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_order_no` (`order_no`),
  UNIQUE KEY `uk_cashier_no` (`cashier_no`),
  KEY `idx_plugin_instance` (`plugin_code`,`instance_id`),
  KEY `idx_instance_status` (`instance_id`,`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='插件市场在线订单';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_market_plugin_review`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_market_plugin_review` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `plugin_code` varchar(64) NOT NULL,
  `user_id` bigint NOT NULL,
  `rating` tinyint NOT NULL COMMENT '评分 1-5',
  `content` text COMMENT '评价内容',
  `reply` text COMMENT '开发者回复',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_plugin_user` (`plugin_code`,`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='插件市场-用户评价';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_market_plugin_version`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_market_plugin_version` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `plugin_code` varchar(64) NOT NULL COMMENT '插件编码',
  `version` varchar(32) NOT NULL COMMENT '版本号',
  `jar_url` varchar(512) DEFAULT NULL COMMENT 'JAR 下载地址',
  `jar_md5` varchar(64) DEFAULT NULL COMMENT 'JAR MD5',
  `jar_size` bigint DEFAULT NULL COMMENT 'JAR 大小（字节）',
  `jar_signature` text COMMENT 'JAR RSA 签名',
  `changelog` text COMMENT '更新说明',
  `schema_sql` text COMMENT '安装时执行的 schema 迁移 SQL',
  `rollback_sql` text COMMENT '回滚 SQL',
  `plugin_yaml` text COMMENT 'plugin.yaml 原文',
  `compatibility_json` text COMMENT '兼容性声明',
  `status` tinyint NOT NULL DEFAULT '0' COMMENT '0待审 1通过 2拒绝 3废弃',
  `review_note` text COMMENT '审核备注',
  `publish_time` datetime DEFAULT NULL COMMENT '发布时间',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_plugin_version` (`plugin_code`,`version`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='插件市场-插件版本';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_market_settlement`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_market_settlement` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `developer_id` bigint NOT NULL,
  `period_start` date NOT NULL,
  `period_end` date NOT NULL,
  `net_amount_cents` bigint NOT NULL DEFAULT '0' COMMENT '结算净额（分）',
  `status` tinyint NOT NULL DEFAULT '0' COMMENT '0待结算 1已结算',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_developer` (`developer_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='插件市场-开发者结算';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_merchant_account_log`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_merchant_account_log` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `sn` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '???',
  `user_id` int DEFAULT NULL COMMENT '??ID',
  `change_object` int DEFAULT NULL COMMENT '????',
  `from_sn` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '????',
  `change_type` int DEFAULT NULL COMMENT '????',
  `change_amount` decimal(10,2) DEFAULT NULL COMMENT '????',
  `left_amount` decimal(10,2) DEFAULT NULL COMMENT '?????',
  `account_status` int DEFAULT NULL COMMENT '????: [1=??, 2=??]',
  `remark` text COLLATE utf8mb4_general_ci COMMENT '????',
  `params` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '????',
  `create_time` bigint DEFAULT NULL COMMENT '????',
  `update_time` bigint DEFAULT NULL COMMENT '????',
  `delete_time` bigint DEFAULT NULL COMMENT '????',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='????';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_merchant_admin`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_merchant_admin` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `root` int DEFAULT NULL COMMENT '??????? 0-? 1-?',
  `name` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '????',
  `avatar` varchar(500) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '????',
  `account` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '????',
  `password` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '????',
  `multipoint_login` int DEFAULT NULL COMMENT '????: [0=?, 1=?]',
  `disable` int DEFAULT NULL COMMENT '????: [0=?, 1=?]',
  `login_ip` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '????IP',
  `login_time` bigint DEFAULT NULL COMMENT '??????',
  `create_time` bigint DEFAULT NULL COMMENT '????',
  `update_time` bigint DEFAULT NULL COMMENT '????',
  `delete_time` bigint DEFAULT NULL COMMENT '????',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='?????';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_merchant_admin_dept`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_merchant_admin_dept` (
  `admin_id` int DEFAULT NULL COMMENT '管理员id',
  `dept_id` int DEFAULT NULL COMMENT '部门id'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='部门关联表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_merchant_admin_jobs`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_merchant_admin_jobs` (
  `admin_id` int DEFAULT NULL COMMENT '管理员id',
  `jobs_id` int DEFAULT NULL COMMENT '岗位id'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='岗位关联表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_merchant_admin_role`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_merchant_admin_role` (
  `admin_id` int DEFAULT NULL COMMENT '管理员id',
  `role_id` int DEFAULT NULL COMMENT '角色id'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='角色关联表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_merchant_announcement`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_merchant_announcement` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `title` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '??',
  `content` text COLLATE utf8mb4_general_ci COMMENT '??',
  `priority` int DEFAULT NULL COMMENT '??? 1?? 2?? 3??',
  `create_time` bigint DEFAULT NULL COMMENT '????',
  `update_time` bigint DEFAULT NULL COMMENT '????',
  `delete_time` bigint DEFAULT NULL COMMENT '????',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='????';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_merchant_area`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_merchant_area` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '????',
  `parent_id` int DEFAULT NULL COMMENT '??????',
  `level` int DEFAULT NULL COMMENT '?? 0-???1-???2-????3-??',
  `name` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '??',
  `short` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '??',
  `city_code` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '????',
  `zip_code` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '????',
  `gcj02_lng` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '??',
  `gcj02_lat` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '??',
  `db09_lng` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '??',
  `db09_lat` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '??',
  `remark1` text COLLATE utf8mb4_general_ci COMMENT '???',
  `remark2` text COLLATE utf8mb4_general_ci COMMENT '???',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='??';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_merchant_article`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_merchant_article` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `cid` int DEFAULT NULL COMMENT '??',
  `title` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '??',
  `desc` text COLLATE utf8mb4_general_ci COMMENT '??',
  `abstract` text COLLATE utf8mb4_general_ci COMMENT '??',
  `image` varchar(500) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '??',
  `content` text COLLATE utf8mb4_general_ci COMMENT '??',
  `author` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '??',
  `click_virtual` int DEFAULT NULL COMMENT '?????',
  `click_actual` int DEFAULT NULL COMMENT '?????',
  `sort` int DEFAULT NULL COMMENT '??',
  `is_show` int DEFAULT NULL COMMENT '????: [0=?, 1=?]',
  `create_time` bigint DEFAULT NULL COMMENT '????',
  `update_time` bigint DEFAULT NULL COMMENT '????',
  `delete_time` bigint DEFAULT NULL COMMENT '????',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='??';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_merchant_article_cate`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_merchant_article_cate` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `name` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '名称',
  `sort` int DEFAULT NULL COMMENT '排序',
  `is_show` int DEFAULT NULL COMMENT '是否显示: [0=否, 1=是]',
  `create_time` bigint DEFAULT NULL COMMENT '创建时间',
  `update_time` bigint DEFAULT NULL COMMENT '更新时间',
  `delete_time` bigint DEFAULT NULL COMMENT '删除时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='文章分类';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_merchant_article_collect`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_merchant_article_collect` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `user_id` int DEFAULT NULL COMMENT '用户ID',
  `article_id` int DEFAULT NULL COMMENT '文章ID',
  `status` int DEFAULT NULL COMMENT '收藏状态 0-未收藏 1-已收藏',
  `create_time` bigint DEFAULT NULL COMMENT '创建时间',
  `update_time` bigint DEFAULT NULL COMMENT '更新时间',
  `delete_time` bigint DEFAULT NULL COMMENT '删除时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='文章收藏';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_merchant_config`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_merchant_config` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `type` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '类型',
  `name` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '键',
  `value` text COLLATE utf8mb4_general_ci COMMENT '值',
  `create_time` bigint DEFAULT NULL COMMENT '创建时间',
  `update_time` bigint DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='系统配置';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_merchant_coupon`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_merchant_coupon` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `create_time` bigint DEFAULT NULL COMMENT '????',
  `update_time` bigint DEFAULT NULL COMMENT '????',
  `delete_time` bigint DEFAULT NULL COMMENT '????',
  `shop_id` int DEFAULT NULL COMMENT '??id',
  `name` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '??',
  `stock_type` int DEFAULT NULL COMMENT '???? 0???? 1????',
  `stock` int DEFAULT NULL COMMENT '????',
  `has_get_num` int DEFAULT NULL COMMENT '?????',
  `has_use_num` int DEFAULT NULL COMMENT '?????',
  `use_time_type` int DEFAULT NULL COMMENT '???? 0???? 1????? 2?????',
  `use_start_time` bigint DEFAULT NULL COMMENT '??????',
  `use_end_time` bigint DEFAULT NULL COMMENT '??????',
  `use_current_day` int DEFAULT NULL COMMENT '???????????',
  `use_next_day` int DEFAULT NULL COMMENT '???????????',
  `money` int DEFAULT NULL COMMENT '?????',
  `condition_type` int DEFAULT NULL COMMENT '???? 0??? 1???',
  `condition_order_money` decimal(10,2) DEFAULT NULL COMMENT '????',
  `get_type` int DEFAULT NULL COMMENT '???? 0???? 1????',
  `get_limit` int DEFAULT NULL COMMENT '???? 0??? 1???? 2??????',
  `get_limit_num` int DEFAULT NULL COMMENT '??????',
  `get_limit_day_num` int DEFAULT NULL COMMENT '????????',
  `use_goods_type` int DEFAULT NULL COMMENT '???? 0???? 1???? 2?????',
  `status` int DEFAULT NULL COMMENT '?? 0??? 1??? 2???',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='???';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_merchant_coupon_get`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_merchant_coupon_get` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `create_time` bigint DEFAULT NULL COMMENT '创建时间',
  `update_time` bigint DEFAULT NULL COMMENT '修改时间',
  `delete_time` bigint DEFAULT NULL COMMENT '删除时间',
  `user_id` int DEFAULT NULL COMMENT '用户id',
  `shop_id` int DEFAULT NULL COMMENT '店铺id',
  `coupon_id` int DEFAULT NULL COMMENT '优惠券id',
  `start_time` bigint DEFAULT NULL COMMENT '使用开始时间',
  `end_time` bigint DEFAULT NULL COMMENT '使用结束时间',
  `status` int DEFAULT NULL COMMENT '状态 0未使用 1已使用 2已过期 3已作废',
  `order_id` int DEFAULT NULL COMMENT '订单id',
  `use_time` bigint DEFAULT NULL COMMENT '使用时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='优惠券领取';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_merchant_coupon_goods`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_merchant_coupon_goods` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `create_time` bigint DEFAULT NULL COMMENT '创建时间',
  `update_time` bigint DEFAULT NULL COMMENT '修改时间',
  `delete_time` bigint DEFAULT NULL COMMENT '删除时间',
  `coupon_id` int DEFAULT NULL COMMENT '优惠券id',
  `goods_id` int DEFAULT NULL COMMENT '商品id',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='优惠券商品';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_merchant_decorate_page`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_merchant_decorate_page` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `type` int DEFAULT NULL COMMENT '????',
  `name` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '????',
  `data` text COLLATE utf8mb4_general_ci COMMENT '????',
  `common` text COLLATE utf8mb4_general_ci COMMENT '????',
  `is_home` int DEFAULT NULL COMMENT '????',
  `create_time` bigint DEFAULT NULL COMMENT '????',
  `update_time` bigint DEFAULT NULL COMMENT '????',
  `delete_time` bigint DEFAULT NULL COMMENT '????',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='????';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_merchant_decorate_tabbar`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_merchant_decorate_tabbar` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `name` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '导航名称',
  `selected` varchar(500) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '未选图标',
  `unselected` varchar(500) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '已选图标',
  `link` varchar(500) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '链接地址',
  `create_time` bigint DEFAULT NULL COMMENT '创建时间',
  `update_time` bigint DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='底部导航';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_merchant_dept`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_merchant_dept` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `pid` int DEFAULT NULL COMMENT '上级部门',
  `name` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '部门名称',
  `leader` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '负责人名',
  `mobile` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '联系电话',
  `sort` int DEFAULT NULL COMMENT '排序编号',
  `status` int DEFAULT NULL COMMENT '是否禁用: [0=否, 1=是]',
  `create_time` bigint DEFAULT NULL COMMENT '创建时间',
  `update_time` bigint DEFAULT NULL COMMENT '更新时间',
  `delete_time` bigint DEFAULT NULL COMMENT '删除时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='系统部门';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_merchant_dev_crontab`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_merchant_dev_crontab` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `name` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '任务名称',
  `type` int DEFAULT NULL COMMENT '类型 1-定时任务',
  `system` int DEFAULT NULL COMMENT '是否系统任务 0-否 1-是',
  `remark` text COLLATE utf8mb4_general_ci COMMENT '备注信息',
  `command` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '执行命令',
  `status` int DEFAULT NULL COMMENT '执行状态: 1=正在运行, 2=任务停止, 3=发生错误',
  `params` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '参数',
  `expression` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '执行规则',
  `error` text COLLATE utf8mb4_general_ci COMMENT '错误信息',
  `last_time` bigint DEFAULT NULL COMMENT '结束时间',
  `time` bigint DEFAULT NULL COMMENT '最大执行时长',
  `max_time` bigint DEFAULT NULL COMMENT '最大执行时长',
  `create_time` bigint DEFAULT NULL COMMENT '创建时间',
  `update_time` bigint DEFAULT NULL COMMENT '更新时间',
  `delete_time` bigint DEFAULT NULL COMMENT '删除时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='计划任务';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_merchant_dict_data`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_merchant_dict_data` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `type_id` int DEFAULT NULL COMMENT '??',
  `type_value` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '???',
  `name` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '??',
  `value` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '??',
  `remark` text COLLATE utf8mb4_general_ci COMMENT '??',
  `sort` int DEFAULT NULL COMMENT '??',
  `status` int DEFAULT NULL COMMENT '??: [0=??, 1-??]',
  `create_time` bigint DEFAULT NULL COMMENT '????',
  `update_time` bigint DEFAULT NULL COMMENT '????',
  `delete_time` bigint DEFAULT NULL COMMENT '????',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='????';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_merchant_dict_type`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_merchant_dict_type` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `name` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '字典名称',
  `type` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '字典类型',
  `remark` text COLLATE utf8mb4_general_ci COMMENT '字典备注',
  `status` int DEFAULT NULL COMMENT '字典状态: [0=停用, 1=正常]',
  `create_time` bigint DEFAULT NULL COMMENT '创建时间',
  `update_time` bigint DEFAULT NULL COMMENT '更新时间',
  `delete_time` bigint DEFAULT NULL COMMENT '删除时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='字典类型';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_merchant_distribution_apply`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_merchant_distribution_apply` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `create_time` bigint DEFAULT NULL COMMENT '????',
  `update_time` bigint DEFAULT NULL COMMENT '????',
  `delete_time` bigint DEFAULT NULL COMMENT '????',
  `user_id` int DEFAULT NULL COMMENT '??id',
  `real_name` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '????',
  `mobile` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '???',
  `province_id` int DEFAULT NULL COMMENT '??',
  `city_id` int DEFAULT NULL COMMENT '??',
  `district_id` int DEFAULT NULL COMMENT '??',
  `reason` text COLLATE utf8mb4_general_ci COMMENT '????',
  `audit_status` int DEFAULT NULL COMMENT '???0-????1-?????2-?????',
  `audit_content` text COLLATE utf8mb4_general_ci COMMENT '????',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='?????';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_merchant_distribution_level`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_merchant_distribution_level` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `create_time` bigint DEFAULT NULL COMMENT '创建时间',
  `update_time` bigint DEFAULT NULL COMMENT '修改时间',
  `delete_time` bigint DEFAULT NULL COMMENT '删除时间',
  `name` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '名称',
  `level` int DEFAULT NULL COMMENT '分销等级',
  `is_default` int DEFAULT NULL COMMENT '默认等级 1是 0否',
  `description` text COLLATE utf8mb4_general_ci COMMENT '说明',
  `self_ratio` decimal(10,2) DEFAULT NULL COMMENT '自购佣金比例',
  `one_ratio` decimal(10,2) DEFAULT NULL COMMENT '一级分销比例',
  `two_ratio` decimal(10,2) DEFAULT NULL COMMENT '二级分销比例',
  `condition` int DEFAULT NULL COMMENT '等级条件 -1未设置 0任意条件 1全部条件',
  `order_one_money_is` int DEFAULT NULL COMMENT '单次消费金额 条件开启 1是0否',
  `order_one_money` decimal(10,2) DEFAULT NULL COMMENT '单次消费金额',
  `order_all_money_is` int DEFAULT NULL COMMENT '累计消费金额 条件开启 1是0否',
  `order_all_money` decimal(10,2) DEFAULT NULL COMMENT '累计消费金额',
  `order_all_num_is` int DEFAULT NULL COMMENT '累计消费次数 条件开启 1是0否',
  `order_all_num` int DEFAULT NULL COMMENT '累计消费次数',
  `commission_is` int DEFAULT NULL COMMENT '已结算佣金收入 开启条件 1是 0否',
  `commission_money` decimal(10,2) DEFAULT NULL COMMENT '已结算佣金收入',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='分销等级';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_merchant_distribution_order`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_merchant_distribution_order` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `create_time` bigint DEFAULT NULL COMMENT '修改时间',
  `update_time` bigint DEFAULT NULL COMMENT '创建时间',
  `delete_time` bigint DEFAULT NULL COMMENT '删除时间',
  `shop_id` int DEFAULT NULL COMMENT '店铺id',
  `user_id` int DEFAULT NULL COMMENT '买家id',
  `user_distribution_id` int DEFAULT NULL COMMENT '分销商id',
  `order_id` int DEFAULT NULL COMMENT '订单ID',
  `order_goods_id` int DEFAULT NULL COMMENT '订单商品ID',
  `level` int DEFAULT NULL COMMENT '分销层级',
  `ratio` decimal(10,2) DEFAULT NULL COMMENT '分销比例',
  `alone_money` decimal(10,2) DEFAULT NULL COMMENT '单独佣金',
  `money` decimal(10,2) DEFAULT NULL COMMENT '佣金',
  `status` int DEFAULT NULL COMMENT '状态 0待结算 1已结算 2已失效',
  `settlement_time` bigint DEFAULT NULL COMMENT '结算时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='分销订单';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_merchant_file`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_merchant_file` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `cid` int DEFAULT NULL COMMENT '??ID',
  `source_id` int DEFAULT NULL COMMENT '???id',
  `source` int DEFAULT NULL COMMENT '????[0-??,1-??]',
  `type` int DEFAULT NULL COMMENT '????: [10=??, 20=??]',
  `name` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '????',
  `uri` varchar(500) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '????',
  `create_time` bigint DEFAULT NULL COMMENT '????',
  `update_time` bigint DEFAULT NULL COMMENT '????',
  `delete_time` bigint DEFAULT NULL COMMENT '????',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='??';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_merchant_file_cate`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_merchant_file_cate` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `pid` int DEFAULT NULL COMMENT '父级ID',
  `type` int DEFAULT NULL COMMENT '分类类型: [10=图片,20=视频]',
  `name` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '分类名称',
  `source` int DEFAULT NULL COMMENT '来源类型[0-后台,1-用户,2-商家]',
  `source_id` int DEFAULT NULL COMMENT '上传者id',
  `create_time` bigint DEFAULT NULL COMMENT '创建时间',
  `update_time` bigint DEFAULT NULL COMMENT '更新时间',
  `delete_time` bigint DEFAULT NULL COMMENT '删除时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='文件分类';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_merchant_hot_search`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_merchant_hot_search` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `name` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '关键词',
  `sort` int DEFAULT NULL COMMENT '排序',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='热门搜索';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_merchant_index_visit`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_merchant_index_visit` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '??ID',
  `ip` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '????ip',
  `terminal` int DEFAULT NULL COMMENT '??????',
  `visit` int DEFAULT NULL COMMENT '????',
  `create_time` bigint DEFAULT NULL COMMENT '????',
  `update_time` bigint DEFAULT NULL COMMENT '????',
  `delete_time` bigint DEFAULT NULL COMMENT '????',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='??????';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_merchant_jobs`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_merchant_jobs` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `code` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '岗位编码',
  `name` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '岗位名称',
  `remark` text COLLATE utf8mb4_general_ci COMMENT '岗位备注',
  `sort` int DEFAULT NULL COMMENT '岗位排序',
  `status` int DEFAULT NULL COMMENT '是否停用: [0=否, 1=是]',
  `create_time` bigint DEFAULT NULL COMMENT '创建时间',
  `update_time` bigint DEFAULT NULL COMMENT '更新时间',
  `delete_time` bigint DEFAULT NULL COMMENT '删除时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='系统岗位';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_merchant_mall_cart`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_merchant_mall_cart` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `create_time` bigint DEFAULT NULL COMMENT '????',
  `update_time` bigint DEFAULT NULL COMMENT '????',
  `delete_time` bigint DEFAULT NULL COMMENT '????',
  `user_id` int DEFAULT NULL COMMENT '??ID',
  `shop_id` int DEFAULT NULL COMMENT '??id',
  `goods_id` int DEFAULT NULL COMMENT '??ID',
  `goods_sku_id` int DEFAULT NULL COMMENT '??skuID',
  `num` int DEFAULT NULL COMMENT '??',
  `select` int DEFAULT NULL COMMENT '??',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='???';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_merchant_mall_express_company`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_merchant_mall_express_company` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `create_time` bigint DEFAULT NULL COMMENT '????',
  `update_time` bigint DEFAULT NULL COMMENT '????',
  `delete_time` bigint DEFAULT NULL COMMENT '????',
  `name` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '??',
  `image` varchar(500) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '??',
  `code_kd` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '????',
  `code_kd100` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '??0100??',
  `code_kdniao` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '?????',
  `sort` int DEFAULT NULL COMMENT '??',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='????';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_merchant_mall_express_tpl`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_merchant_mall_express_tpl` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `create_time` bigint DEFAULT NULL COMMENT '创建时间',
  `update_time` bigint DEFAULT NULL COMMENT '修改时间',
  `delete_time` bigint DEFAULT NULL COMMENT '删除时间',
  `shop_id` int DEFAULT NULL COMMENT '店铺id',
  `name` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '名称',
  `type` int DEFAULT NULL COMMENT '计费方式 0件 1 重 2体积',
  `content` text COLLATE utf8mb4_general_ci COMMENT '配送内容',
  `remark` text COLLATE utf8mb4_general_ci COMMENT '备注',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='运费模板';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_merchant_mall_gather`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_merchant_mall_gather` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `create_time` bigint DEFAULT NULL COMMENT '????',
  `update_time` bigint DEFAULT NULL COMMENT '????',
  `delete_time` bigint DEFAULT NULL COMMENT '????',
  `shop_id` int DEFAULT NULL COMMENT '??id',
  `links` text COLLATE utf8mb4_general_ci COMMENT '????',
  `type` int DEFAULT NULL COMMENT '???? 0????',
  `category_id` int DEFAULT NULL COMMENT '????',
  `shop_category_id` int DEFAULT NULL COMMENT '??????',
  `nums` int DEFAULT NULL COMMENT '???',
  `success` int DEFAULT NULL COMMENT '???',
  `failed` int DEFAULT NULL COMMENT '???',
  `gather_money` int DEFAULT NULL COMMENT '??????',
  `status` int DEFAULT NULL COMMENT '?? 0??? 1??? 2???',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='??';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_merchant_mall_gather_goods`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_merchant_mall_gather_goods` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `create_time` bigint DEFAULT NULL COMMENT '创建时间',
  `update_time` bigint DEFAULT NULL COMMENT '修改时间',
  `delete_time` bigint DEFAULT NULL COMMENT '删除时间',
  `shop_id` int DEFAULT NULL COMMENT '店铺id',
  `gather_id` int DEFAULT NULL COMMENT '采集id',
  `status` int DEFAULT NULL COMMENT '状态 1成功 0失败',
  `deal` int DEFAULT NULL COMMENT '已处理 1是 0否',
  `link` varchar(500) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '链接',
  `content` text COLLATE utf8mb4_general_ci COMMENT '商品内容',
  `gather` text COLLATE utf8mb4_general_ci COMMENT '采集记录',
  `channel` int DEFAULT NULL COMMENT '渠道',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='采集商品';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_merchant_mall_goods`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_merchant_mall_goods` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `create_time` bigint DEFAULT NULL COMMENT '创建时间',
  `update_time` bigint DEFAULT NULL COMMENT '修改时间',
  `delete_time` bigint DEFAULT NULL COMMENT '删除时间',
  `shop_id` int DEFAULT NULL COMMENT '店铺id',
  `name` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '商品名称',
  `code` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '商品货号',
  `descript` text COLLATE utf8mb4_general_ci COMMENT '商品描述',
  `category_ids` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '商品分类',
  `shop_category_id` int DEFAULT NULL COMMENT '商家分类',
  `image` varchar(500) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '主图',
  `video_is` int DEFAULT NULL COMMENT '是否有视频',
  `video_from` int DEFAULT NULL COMMENT '视频来源 1素材库 2链接',
  `video_cover` varchar(500) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '视频封面',
  `video_uri` varchar(500) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '视频地址',
  `sku_type` int DEFAULT NULL COMMENT '规格类型 0单规格 1多规格',
  `is_express` int DEFAULT NULL COMMENT '开启快递配送',
  `express_type` int DEFAULT NULL COMMENT '运费类型 0包邮 1运费模板 2 统一运费',
  `express_tpl_id` int DEFAULT NULL COMMENT '运费模板ID',
  `express_money` decimal(10,2) DEFAULT NULL COMMENT '统一运费',
  `total_stock` int DEFAULT NULL COMMENT '总库存',
  `stock_warning` int DEFAULT NULL COMMENT '库存预警 0不做预警',
  `min_price` decimal(10,2) DEFAULT NULL COMMENT '最小价格',
  `max_price` decimal(10,2) DEFAULT NULL COMMENT '最大价格',
  `min_line_price` decimal(10,2) DEFAULT NULL COMMENT '最小划线价',
  `max_line_price` decimal(10,2) DEFAULT NULL COMMENT '最大划线价',
  `sale_num` int DEFAULT NULL COMMENT '总销量',
  `virtual_sale_num` int DEFAULT NULL COMMENT '虚拟销量',
  `visit_num` int DEFAULT NULL COMMENT '浏览量',
  `virtual_visit_num` int DEFAULT NULL COMMENT '虚拟浏览量',
  `sale_money` decimal(10,2) DEFAULT NULL COMMENT '总销售额',
  `sort` int DEFAULT NULL COMMENT '排序',
  `status` int DEFAULT NULL COMMENT '状态 0仓库 1上架',
  `audit_status` int DEFAULT NULL COMMENT '审核状态0待审核 1通过 2拒绝',
  `audit_content` text COLLATE utf8mb4_general_ci COMMENT '审核内容',
  `shop_recommend` int DEFAULT NULL COMMENT '店内推荐 1推荐 0不推荐',
  `is_recycle` int DEFAULT NULL COMMENT '回收站 1是 0否',
  `content` text COLLATE utf8mb4_general_ci COMMENT '详情',
  `is_discount` int DEFAULT NULL COMMENT '会员折扣 1是 0否',
  `discount_type` int DEFAULT NULL COMMENT '折扣规则 0默认 1单独折扣 2单独价格',
  `is_distribution` int DEFAULT NULL COMMENT '参与分销 1是 0否',
  `distribution_type` int DEFAULT NULL COMMENT '分销规则 0默认 1单独佣金比例 2单独设置佣金',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='商品';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_merchant_mall_goods_category`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_merchant_mall_goods_category` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `create_time` bigint DEFAULT NULL COMMENT '创建时间',
  `update_time` bigint DEFAULT NULL COMMENT '修改时间',
  `delete_time` bigint DEFAULT NULL COMMENT '删除时间',
  `name` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '名称',
  `pid` int DEFAULT NULL COMMENT '父级ID',
  `level` int DEFAULT NULL COMMENT '等级',
  `image` varchar(500) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '图标',
  `sort` int DEFAULT NULL COMMENT '排序',
  `is_show` int DEFAULT NULL COMMENT '是否显示',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='商品分类';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_merchant_mall_goods_category_index`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_merchant_mall_goods_category_index` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `create_time` bigint DEFAULT NULL COMMENT '创建时间',
  `update_time` bigint DEFAULT NULL COMMENT '修改时间',
  `delete_time` bigint DEFAULT NULL COMMENT '删除时间',
  `goods_id` int DEFAULT NULL COMMENT '商品ID',
  `category_id` int DEFAULT NULL COMMENT '分类ID',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='商品分类关联';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_merchant_mall_goods_collect`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_merchant_mall_goods_collect` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `create_time` bigint DEFAULT NULL COMMENT '创建时间',
  `update_time` bigint DEFAULT NULL COMMENT '修改时间',
  `delete_time` bigint DEFAULT NULL COMMENT '删除时间',
  `user_id` int DEFAULT NULL COMMENT '用户ID',
  `shop_id` int DEFAULT NULL COMMENT '店铺id',
  `goods_id` int DEFAULT NULL COMMENT '商品ID',
  `status` int DEFAULT NULL COMMENT '状态 1收藏 0取消收藏',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='商品收藏';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_merchant_mall_goods_comment`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_merchant_mall_goods_comment` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `create_time` bigint DEFAULT NULL COMMENT '创建时间',
  `update_time` bigint DEFAULT NULL COMMENT '修改时间',
  `delete_time` bigint DEFAULT NULL COMMENT '删除时间',
  `shop_id` int DEFAULT NULL COMMENT '店铺id',
  `user_id` int DEFAULT NULL COMMENT '用户ID',
  `order_id` int DEFAULT NULL COMMENT '订单ID',
  `order_goods_id` int DEFAULT NULL COMMENT '订单商品ID',
  `goods_id` int DEFAULT NULL COMMENT '商品ID',
  `goods_sku_id` int DEFAULT NULL COMMENT '商品skuID',
  `score` int DEFAULT NULL COMMENT '评分',
  `content` text COLLATE utf8mb4_general_ci COMMENT '内容',
  `is_show` int DEFAULT NULL COMMENT '是否显示',
  `is_reply` int DEFAULT NULL COMMENT '已回复',
  `reply_content` text COLLATE utf8mb4_general_ci COMMENT '回复内容',
  `describe_score` int DEFAULT NULL COMMENT '描述评分',
  `service_score` int DEFAULT NULL COMMENT '服务评分',
  `logistics_score` int DEFAULT NULL COMMENT '物流评分',
  `has_images` int DEFAULT NULL COMMENT '有图片',
  `images` text COLLATE utf8mb4_general_ci COMMENT '评论图片',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='商品评论';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_merchant_mall_goods_discount`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_merchant_mall_goods_discount` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `create_time` bigint DEFAULT NULL COMMENT '创建时间',
  `update_time` bigint DEFAULT NULL COMMENT '修改时间',
  `delete_time` bigint DEFAULT NULL COMMENT '删除时间',
  `goods_id` int DEFAULT NULL COMMENT '商品id',
  `goods_sku_id` int DEFAULT NULL COMMENT '商品sku id',
  `user_level_id` int DEFAULT NULL COMMENT '等级id',
  `alone_discount` decimal(10,2) DEFAULT NULL COMMENT '单独折扣',
  `alone_price` decimal(10,2) DEFAULT NULL COMMENT '单独价格',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='商品折扣';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_merchant_mall_goods_distribution`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_merchant_mall_goods_distribution` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `create_time` bigint DEFAULT NULL COMMENT '创建时间',
  `update_time` bigint DEFAULT NULL COMMENT '修改时间',
  `delete_time` bigint DEFAULT NULL COMMENT '删除时间',
  `goods_id` int DEFAULT NULL COMMENT '商品id',
  `goods_sku_id` int DEFAULT NULL COMMENT '商品sku id',
  `distribution_level_id` int DEFAULT NULL COMMENT '分销等级id',
  `alone_self_ratio` decimal(10,2) DEFAULT NULL COMMENT '单独佣金比例 自购比例',
  `alone_one_ratio` decimal(10,2) DEFAULT NULL COMMENT '单独佣金比例 一级比例',
  `alone_two_ratio` decimal(10,2) DEFAULT NULL COMMENT '单独佣金比例 二级比例',
  `alone_self_money` decimal(10,2) DEFAULT NULL COMMENT '单独佣金金额 自购佣金',
  `alone_one_money` decimal(10,2) DEFAULT NULL COMMENT '单独佣金金额 一级佣金',
  `alone_two_money` decimal(10,2) DEFAULT NULL COMMENT '单独佣金金额 二级佣金',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='商品分销';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_merchant_mall_goods_image`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_merchant_mall_goods_image` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `create_time` bigint DEFAULT NULL COMMENT '创建时间',
  `update_time` bigint DEFAULT NULL COMMENT '修改时间',
  `delete_time` bigint DEFAULT NULL COMMENT '删除时间',
  `goods_id` int DEFAULT NULL COMMENT '商品ID',
  `image` varchar(500) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '图片',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='商品轮播图';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_merchant_mall_goods_sku`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_merchant_mall_goods_sku` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `create_time` bigint DEFAULT NULL COMMENT '创建时间',
  `update_time` bigint DEFAULT NULL COMMENT '修改时间',
  `delete_time` bigint DEFAULT NULL COMMENT '删除时间',
  `shop_id` int DEFAULT NULL COMMENT '店铺id',
  `goods_id` int DEFAULT NULL COMMENT '商品ID',
  `image` varchar(500) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '规格图片',
  `sku_value_ids` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT 'sku值 ID集合',
  `sku_value_arr` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT 'sku值 值集合',
  `price` decimal(10,2) DEFAULT NULL COMMENT '销售价',
  `line_price` decimal(10,2) DEFAULT NULL COMMENT '划线价',
  `market_price` decimal(10,2) DEFAULT NULL COMMENT '市场价',
  `stock` int DEFAULT NULL COMMENT '库存',
  `weight` decimal(10,2) DEFAULT NULL COMMENT '重量',
  `volume` decimal(10,2) DEFAULT NULL COMMENT '体积',
  `code` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '条形码',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='商品SKU';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_merchant_mall_goods_sku_name`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_merchant_mall_goods_sku_name` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `create_time` bigint DEFAULT NULL COMMENT '创建时间',
  `update_time` bigint DEFAULT NULL COMMENT '修改时间',
  `delete_time` bigint DEFAULT NULL COMMENT '删除时间',
  `goods_id` int DEFAULT NULL COMMENT '商品ID',
  `name` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '名称',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='商品SKU名称';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_merchant_mall_goods_sku_value`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_merchant_mall_goods_sku_value` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `create_time` bigint DEFAULT NULL COMMENT '创建时间',
  `update_time` bigint DEFAULT NULL COMMENT '修改时间',
  `delete_time` bigint DEFAULT NULL COMMENT '删除时间',
  `goods_id` int DEFAULT NULL COMMENT '商品ID',
  `sku_name_id` int DEFAULT NULL COMMENT 'sku名称ID',
  `value` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT 'sku值',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='商品SKU值';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_merchant_mall_goods_visit`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_merchant_mall_goods_visit` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `create_time` bigint DEFAULT NULL COMMENT '创建时间',
  `update_time` bigint DEFAULT NULL COMMENT '修改时间',
  `delete_time` bigint DEFAULT NULL COMMENT '删除时间',
  `shop_id` int DEFAULT NULL COMMENT '店铺',
  `goods_id` int DEFAULT NULL COMMENT '商品',
  `user_id` int DEFAULT NULL COMMENT '用户',
  `num` int DEFAULT NULL COMMENT '访问数',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='商品浏览';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_merchant_mall_order`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_merchant_mall_order` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `create_time` bigint DEFAULT NULL COMMENT '创建时间',
  `update_time` bigint DEFAULT NULL COMMENT '修改时间',
  `delete_time` bigint DEFAULT NULL COMMENT '删除时间',
  `create_id` int DEFAULT NULL COMMENT '创建id 用于标识同一次提交的订单',
  `sn` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '订单号',
  `shop_id` int DEFAULT NULL COMMENT '店铺id',
  `user_id` int DEFAULT NULL COMMENT '用户id',
  `type` int DEFAULT NULL COMMENT '订单类型 0普通订单 10秒杀 20 拼团',
  `ms_id` int DEFAULT NULL COMMENT '秒杀id',
  `pt_id` int DEFAULT NULL COMMENT '拼团id',
  `pt_open_id` int DEFAULT NULL COMMENT '拼团开团id',
  `pt_open_join_id` int DEFAULT NULL COMMENT '拼团参团id',
  `from` int DEFAULT NULL COMMENT '订单来源 1微信小程序',
  `status` int DEFAULT NULL COMMENT '状态 0待付款 1待发货 2待收货 3已完成 4已关闭',
  `pay_is` int DEFAULT NULL COMMENT '已支付 0未支付 1已支付',
  `pay_way` int DEFAULT NULL COMMENT '支付方式0未知 1余额 2微信 3支付宝',
  `pay_time` bigint DEFAULT NULL COMMENT '支付时间',
  `pay_transaction_no` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '支付流水号',
  `address` text COLLATE utf8mb4_general_ci COMMENT '地址',
  `address_contact` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '联系人',
  `address_mobile` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '联系电话',
  `address_content` text COLLATE utf8mb4_general_ci COMMENT '联系地址',
  `delivery_type` int DEFAULT NULL COMMENT '配送方式 0快递',
  `money` decimal(10,2) DEFAULT NULL COMMENT '总金额',
  `goods_origin_money` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '商品原价总额',
  `goods_money` decimal(10,2) DEFAULT NULL COMMENT '商品购买价总额',
  `goods_nums` int DEFAULT NULL COMMENT '商品总数',
  `express_money` decimal(10,2) DEFAULT NULL COMMENT '运费总额',
  `discount_money` decimal(10,2) DEFAULT NULL COMMENT '会员折扣总额',
  `coupon_money` decimal(10,2) DEFAULT NULL COMMENT '使用优惠券金额',
  `coupon_get_id` int DEFAULT NULL COMMENT '优惠券领取id',
  `need_pay_money` decimal(10,2) DEFAULT NULL COMMENT '应付金额',
  `pay_money` decimal(10,2) DEFAULT NULL COMMENT '实付金额',
  `order_goods_num` int DEFAULT NULL COMMENT '订单商品数 0未记录',
  `total_goods_num` int DEFAULT NULL COMMENT '商品数',
  `express_is` int DEFAULT NULL COMMENT '已发货 0未发货 1已发货',
  `express_time` bigint DEFAULT NULL COMMENT '发货时间',
  `express_company_id` int DEFAULT NULL COMMENT '物流公司',
  `express_no` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '物流单号',
  `express_confirm_time` bigint DEFAULT NULL COMMENT '收货时间',
  `delivery_content` text COLLATE utf8mb4_general_ci COMMENT '发货内容',
  `delivery_content1` text COLLATE utf8mb4_general_ci COMMENT '自动发货自定义内容',
  `delivery_content_type` int DEFAULT NULL COMMENT '自动发货类型 0固定内容 1自定义内容',
  `wechat_mini_express_sync` int DEFAULT NULL COMMENT '微信小程序发货信息录入',
  `wechat_mini_express_sync_time` bigint DEFAULT NULL COMMENT '录入时间',
  `express_again` int DEFAULT NULL COMMENT '已重新发货 0否 1是',
  `cancel_time` bigint DEFAULT NULL COMMENT '取消时间',
  `after_sale_deadline` bigint DEFAULT NULL COMMENT '售后截止时间',
  `user_remark` text COLLATE utf8mb4_general_ci COMMENT '用户备注',
  `shop_remark` text COLLATE utf8mb4_general_ci COMMENT '商家备注',
  `settlement_is` int DEFAULT NULL COMMENT '结算状态 0 未结算 1已结算',
  `service_ratio` decimal(10,2) DEFAULT NULL COMMENT '服务费率',
  `shop_info` text COLLATE utf8mb4_general_ci COMMENT '店铺详情',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='订单';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_merchant_mall_order_after_sale`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_merchant_mall_order_after_sale` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `create_time` bigint DEFAULT NULL COMMENT '创建时间',
  `update_time` bigint DEFAULT NULL COMMENT '修改时间',
  `delete_time` bigint DEFAULT NULL COMMENT '删除时间',
  `sn` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '售后单号',
  `shop_id` int DEFAULT NULL COMMENT '商家id',
  `user_id` int DEFAULT NULL COMMENT '用户id',
  `from` int DEFAULT NULL COMMENT '来源',
  `order_id` int DEFAULT NULL COMMENT '订单id',
  `order_goods_id` int DEFAULT NULL COMMENT '订单商品id',
  `order_goods_ids` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '订单商品ids',
  `is_order_all` int DEFAULT NULL COMMENT '整单退款 1是 0否 2未记录',
  `type` int DEFAULT NULL COMMENT '类型 0仅退款',
  `status` int DEFAULT NULL COMMENT '状态 0申请中 10等待退款 20售后成功 30 售后失败',
  `sub_status` int DEFAULT NULL COMMENT '子状态',
  `refund_reason` int DEFAULT NULL COMMENT '退款原因',
  `refund_content` text COLLATE utf8mb4_general_ci COMMENT '退款说明',
  `refund_image` varchar(500) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '退款图片',
  `refund_money` decimal(10,2) DEFAULT NULL COMMENT '退款金额',
  `refuse_content` text COLLATE utf8mb4_general_ci COMMENT '拒绝原因',
  `settlement_is` int DEFAULT NULL COMMENT '结算 0未结算 1已结算',
  `service_ratio` decimal(10,2) DEFAULT NULL COMMENT '服务费率',
  `return_express_company` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '寄回物流公司',
  `return_express_no` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '寄回物流单号',
  `return_image` varchar(500) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '寄回凭证',
  `return_remark` text COLLATE utf8mb4_general_ci COMMENT '寄回备注',
  `return_time` bigint DEFAULT NULL COMMENT '寄回时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='订单售后';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_merchant_mall_order_after_sale_log`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_merchant_mall_order_after_sale_log` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `create_time` bigint DEFAULT NULL COMMENT '创建时间',
  `update_time` bigint DEFAULT NULL COMMENT '修改时间',
  `delete_time` bigint DEFAULT NULL COMMENT '删除时间',
  `after_sale_id` int DEFAULT NULL COMMENT '售后id',
  `operator_type` int DEFAULT NULL COMMENT '操作人 0 系统 1 管理员  2 商家 3用户',
  `operator_id` int DEFAULT NULL COMMENT '操作人id',
  `content` text COLLATE utf8mb4_general_ci COMMENT '操作内容',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='订单售后日志';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_merchant_mall_order_create`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_merchant_mall_order_create` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `create_time` bigint DEFAULT NULL COMMENT '创建时间',
  `update_time` bigint DEFAULT NULL COMMENT '修改时间',
  `delete_time` bigint DEFAULT NULL COMMENT '删除时间',
  `sn` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '总订单编号',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='订单创建';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_merchant_mall_order_goods`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_merchant_mall_order_goods` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `create_time` bigint DEFAULT NULL COMMENT '创建时间',
  `update_time` bigint DEFAULT NULL COMMENT '修改时间',
  `delete_time` bigint DEFAULT NULL COMMENT '删除时间',
  `order_id` int DEFAULT NULL COMMENT '订单id',
  `shop_id` int DEFAULT NULL COMMENT '店铺id',
  `user_id` int DEFAULT NULL COMMENT '用户id',
  `goods_id` int DEFAULT NULL COMMENT '商品id',
  `goods_name` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '商品名称',
  `goods_image` varchar(500) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '商品图片',
  `goods_code` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '商品货号',
  `goods_sku_id` int DEFAULT NULL COMMENT 'sku id',
  `goods_sku_value` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '商品规格值名称',
  `goods_original_price` decimal(10,2) DEFAULT NULL COMMENT '原价',
  `goods_price` decimal(10,2) DEFAULT NULL COMMENT '购买价格',
  `goods_price_type` int DEFAULT NULL COMMENT '购买价格类型 0普通 1秒杀 2拼团',
  `goods_num` int DEFAULT NULL COMMENT '购买数量',
  `money` decimal(10,2) DEFAULT NULL COMMENT '总额',
  `goods_money` decimal(10,2) DEFAULT NULL COMMENT '商品购买价总额',
  `goods_origin_money` decimal(10,2) DEFAULT NULL COMMENT '商品原价总额',
  `express_money` decimal(10,2) DEFAULT NULL COMMENT '运费总额',
  `discount_money` decimal(10,2) DEFAULT NULL COMMENT '折扣总额',
  `coupon_money` decimal(10,2) DEFAULT NULL COMMENT '优惠券总额',
  `need_pay_money` decimal(10,2) DEFAULT NULL COMMENT '应付',
  `pay_money` decimal(10,2) DEFAULT NULL COMMENT '实付',
  `comment` int DEFAULT NULL COMMENT '评论 0 否 1是 2不可评论',
  `after_sale` int DEFAULT NULL COMMENT '售后 0 无售后 1售后处理中 2售后已处理',
  `after_sale_id` int DEFAULT NULL COMMENT '售后ID',
  `after_sale_money` decimal(10,2) DEFAULT NULL COMMENT '售后金额',
  `content` text COLLATE utf8mb4_general_ci COMMENT '记录内容',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='订单商品';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_merchant_mall_order_log`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_merchant_mall_order_log` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `create_time` bigint DEFAULT NULL COMMENT '创建时间',
  `update_time` bigint DEFAULT NULL COMMENT '修改时间',
  `delete_time` bigint DEFAULT NULL COMMENT '删除时间',
  `order_id` int DEFAULT NULL COMMENT '订单id',
  `channel` int DEFAULT NULL COMMENT '动作',
  `operator_type` int DEFAULT NULL COMMENT '类型 0系统 1管理员 2商家 3用户',
  `operator_id` int DEFAULT NULL COMMENT '操作人id',
  `content` text COLLATE utf8mb4_general_ci COMMENT '内容',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='订单日志';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_merchant_mall_pay`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_merchant_mall_pay` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `name` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '模版名称',
  `icon` varchar(500) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '渠道图标',
  `pay_type` int DEFAULT NULL COMMENT '支付方式: [1=余额支付, 2=微信支付, 3=支付宝支付]',
  `sort` int DEFAULT NULL COMMENT '排序编号',
  `remark` text COLLATE utf8mb4_general_ci COMMENT '备注信息',
  `config` text COLLATE utf8mb4_general_ci COMMENT '配置参数',
  `create_time` bigint DEFAULT NULL COMMENT '创建时间',
  `update_time` bigint DEFAULT NULL COMMENT '更新时间',
  `delete_time` bigint DEFAULT NULL COMMENT '删除时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='支付配置';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_merchant_mall_pay_scene`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_merchant_mall_pay_scene` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `mall_pay_id` int DEFAULT NULL COMMENT '支付配置ID',
  `scene` int DEFAULT NULL COMMENT '场景编码: [1=微信小程序, 2=微信公众号, 3=H5, 4=PC, 5=APP]',
  `is_default` int DEFAULT NULL COMMENT '默认支付: [0=否的, 1=是的]',
  `status` int DEFAULT NULL COMMENT '方式状态: [0=关闭, 1=开启]',
  `create_time` bigint DEFAULT NULL COMMENT '创建时间',
  `update_time` bigint DEFAULT NULL COMMENT '更新时间',
  `delete_time` bigint DEFAULT NULL COMMENT '删除时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='支付方式';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_merchant_mall_refund`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_merchant_mall_refund` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `create_time` bigint DEFAULT NULL COMMENT '创建时间',
  `update_time` bigint DEFAULT NULL COMMENT '修改时间',
  `delete_time` bigint DEFAULT NULL COMMENT '删除时间',
  `shop_id` int DEFAULT NULL COMMENT '店铺id',
  `user_id` int DEFAULT NULL COMMENT '用户id',
  `sn` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '售后单号',
  `order_id` int DEFAULT NULL COMMENT '订单ID',
  `order_after_sale_id` int DEFAULT NULL COMMENT '售后id',
  `pay_way` int DEFAULT NULL COMMENT '支付方式',
  `from_sn` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '售后单号',
  `type` int DEFAULT NULL COMMENT '类型',
  `money` decimal(10,2) DEFAULT NULL COMMENT '金额',
  `order_money` decimal(10,2) DEFAULT NULL COMMENT '原订单支付金额',
  `transaction_sn` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '第三方流水号',
  `status` int DEFAULT NULL COMMENT '状态 0 退款中 1 成功 2失败',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='退款记录';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_merchant_mall_refund_log`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_merchant_mall_refund_log` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `create_time` bigint DEFAULT NULL COMMENT '创建时间',
  `update_time` bigint DEFAULT NULL COMMENT '修改时间',
  `delete_time` bigint DEFAULT NULL COMMENT '删除时间',
  `refund_id` int DEFAULT NULL COMMENT '退款id',
  `sn` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '售后单号',
  `money` decimal(10,2) DEFAULT NULL COMMENT '金额',
  `refund_status` int DEFAULT NULL COMMENT '退款状态',
  `content` text COLLATE utf8mb4_general_ci COMMENT '记录内容',
  `operator_id` int DEFAULT NULL COMMENT '操作人',
  `operator_type` int DEFAULT NULL COMMENT '操作人类型',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='退款日志';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_merchant_mall_settlement`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_merchant_mall_settlement` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `create_time` bigint DEFAULT NULL COMMENT '创建时间',
  `update_time` bigint DEFAULT NULL COMMENT '修改时间',
  `delete_time` bigint DEFAULT NULL COMMENT '删除时间',
  `shop_id` int DEFAULT NULL COMMENT '店铺id',
  `sn` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '流水号',
  `order_money` decimal(10,2) DEFAULT NULL COMMENT '营业额',
  `distribution_money` decimal(10,2) DEFAULT NULL COMMENT '分销金额',
  `order_num` int DEFAULT NULL COMMENT '订单数',
  `order_refund_num` int DEFAULT NULL COMMENT '退款订单数',
  `order_refund_money` decimal(10,2) DEFAULT NULL COMMENT '退款金额',
  `service_ratio` decimal(10,2) DEFAULT NULL COMMENT '服务费比率',
  `service_money` decimal(10,2) DEFAULT NULL COMMENT '服务费',
  `money` decimal(10,2) DEFAULT NULL COMMENT '结算入账金额',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='结算';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_merchant_mall_settlement_record`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_merchant_mall_settlement_record` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `create_time` bigint DEFAULT NULL COMMENT '创建时间',
  `update_time` bigint DEFAULT NULL COMMENT '修改时间',
  `delete_time` bigint DEFAULT NULL COMMENT '删除时间',
  `shop_id` int DEFAULT NULL COMMENT '店铺id',
  `settlement_id` int DEFAULT NULL COMMENT '结算id',
  `sn` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '流水号',
  `from_sn` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '来源单号',
  `type` int DEFAULT NULL COMMENT '类型 1订单收入 2订单售后退款',
  `order_money` decimal(10,2) DEFAULT NULL COMMENT '订单金额',
  `order_refund_money` decimal(10,2) DEFAULT NULL COMMENT '订单退款金额',
  `service_money` decimal(10,2) DEFAULT NULL COMMENT '服务费',
  `service_ratio` decimal(10,2) DEFAULT NULL COMMENT '服务费率',
  `money` decimal(10,2) DEFAULT NULL COMMENT '结算入账金额',
  `distribution_money` decimal(10,2) DEFAULT NULL COMMENT '分销金额',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='结算记录';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_merchant_mall_shop`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_merchant_mall_shop` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `create_time` bigint DEFAULT NULL COMMENT '创建时间',
  `update_time` bigint DEFAULT NULL COMMENT '修改时间',
  `delete_time` bigint DEFAULT NULL COMMENT '删除时间',
  `name` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '名称',
  `logo` varchar(500) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT 'logo',
  `desc` text COLLATE utf8mb4_general_ci COMMENT '店铺简介',
  `shop_category_id` int DEFAULT NULL COMMENT '经营类目',
  `type` int DEFAULT NULL COMMENT '类型 0自营 1入驻',
  `shop_status` int DEFAULT NULL COMMENT '店铺状态 1正常 0冻结',
  `business_status` int DEFAULT NULL COMMENT '经营状态 1营业中 0暂停营业',
  `contact` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '联系人',
  `mobile` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '联系手机',
  `province_id` int DEFAULT NULL COMMENT '省',
  `city_id` int DEFAULT NULL COMMENT '市',
  `district_id` int DEFAULT NULL COMMENT '区',
  `address_info` text COLLATE utf8mb4_general_ci COMMENT '详细地址',
  `lng` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '纬度',
  `lat` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '经度',
  `return_contact` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '退货联系人',
  `return_mobile` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '退货联系手机',
  `return_province_id` int DEFAULT NULL COMMENT '退货省份',
  `return_city_id` int DEFAULT NULL COMMENT '退货城市',
  `return_district_id` int DEFAULT NULL COMMENT '退货区县',
  `return_address_info` text COLLATE utf8mb4_general_ci COMMENT '退货详细地址',
  `home_recommend` int DEFAULT NULL COMMENT '精选店铺 1是 0否',
  `goods_audit` int DEFAULT NULL COMMENT '产品审核 1需要审核 0无需审核',
  `business_license` varchar(500) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '营业执照',
  `other_license` varchar(500) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '其他执照',
  `service_ratio` decimal(10,2) DEFAULT NULL COMMENT '服务费百分比',
  `score` decimal(10,2) DEFAULT NULL COMMENT '店铺评分',
  `star` decimal(10,2) DEFAULT NULL COMMENT '店铺星级',
  `visit_num` int DEFAULT NULL COMMENT '进店人数',
  `money` decimal(10,2) DEFAULT NULL COMMENT '店铺余额',
  `sale_money` int DEFAULT NULL COMMENT '营业额',
  `sale_orders` int DEFAULT NULL COMMENT '成交订单',
  `sale_online_goods` int DEFAULT NULL COMMENT '在售商品数',
  `gather_money` int DEFAULT NULL COMMENT '采集余额',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='商家店铺';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_merchant_mall_shop_account_log`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_merchant_mall_shop_account_log` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `create_time` bigint DEFAULT NULL COMMENT '创建时间',
  `update_time` bigint DEFAULT NULL COMMENT '修改时间',
  `delete_time` bigint DEFAULT NULL COMMENT '删除时间',
  `shop_id` int DEFAULT NULL COMMENT '店铺id',
  `sn` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '流水号',
  `change_object` int DEFAULT NULL COMMENT '变动对象 0余额',
  `change_type` int DEFAULT NULL COMMENT '变动类型',
  `account_status` int DEFAULT NULL COMMENT '账户状态 0无变化 1增益 2减益',
  `from_id` int DEFAULT NULL COMMENT '来源id',
  `from_sn` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '来源单号',
  `change_amount` decimal(10,2) DEFAULT NULL COMMENT '变动金额',
  `left_amount` decimal(10,2) DEFAULT NULL COMMENT '剩余金额',
  `remark` text COLLATE utf8mb4_general_ci COMMENT '备注',
  `params` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '记录参数',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='商家流水';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_merchant_mall_shop_admin`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_merchant_mall_shop_admin` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `shop_id` int DEFAULT NULL COMMENT '店铺id',
  `user_id` bigint DEFAULT NULL COMMENT '关联统一会员ID',
  `super` int DEFAULT NULL COMMENT '超级管理员 1是 0否',
  `name` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '用户账号',
  `face` varchar(500) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '用户头像',
  `status` int DEFAULT NULL COMMENT '状态 1正常 0停用',
  `login_all` int DEFAULT NULL COMMENT '允许多处登录 1支持 0支持',
  `login_last_time` bigint DEFAULT NULL COMMENT '最后登录时间',
  `login_last_ip` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '最后登录IP',
  `create_time` bigint DEFAULT NULL COMMENT '创建时间',
  `update_time` bigint DEFAULT NULL COMMENT '更新时间',
  `delete_time` bigint DEFAULT NULL COMMENT '删除时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='商家账号';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_merchant_mall_shop_admin_dept`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_merchant_mall_shop_admin_dept` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `dept_id` int DEFAULT NULL COMMENT '部门id',
  `shop_id` int DEFAULT NULL COMMENT '店铺id',
  `admin_id` int DEFAULT NULL COMMENT '管理员id',
  `create_time` bigint DEFAULT NULL COMMENT '创建时间',
  `update_time` bigint DEFAULT NULL COMMENT '更新时间',
  `delete_time` bigint DEFAULT NULL COMMENT '删除时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='商家部门关联';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_merchant_mall_shop_admin_job`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_merchant_mall_shop_admin_job` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `shop_id` int DEFAULT NULL COMMENT '店铺id',
  `admin_id` int DEFAULT NULL COMMENT '管理员id',
  `job_id` int DEFAULT NULL COMMENT '岗位id',
  `create_time` bigint DEFAULT NULL COMMENT '创建时间',
  `update_time` bigint DEFAULT NULL COMMENT '更新时间',
  `delete_time` bigint DEFAULT NULL COMMENT '删除时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='商家岗位关联';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_merchant_mall_shop_admin_role`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_merchant_mall_shop_admin_role` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `shop_id` int DEFAULT NULL COMMENT '店铺id',
  `admin_id` int DEFAULT NULL COMMENT '管理员id',
  `create_time` bigint DEFAULT NULL COMMENT '创建时间',
  `update_time` bigint DEFAULT NULL COMMENT '更新时间',
  `delete_time` bigint DEFAULT NULL COMMENT '删除时间',
  `role_id` int DEFAULT NULL COMMENT '角色id',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='商家角色关联';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_merchant_mall_shop_apply`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_merchant_mall_shop_apply` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `create_time` bigint DEFAULT NULL COMMENT '创建时间',
  `update_time` bigint DEFAULT NULL COMMENT '修改时间',
  `delete_time` bigint DEFAULT NULL COMMENT '删除时间',
  `user_id` int DEFAULT NULL COMMENT '用户id',
  `cid` int DEFAULT NULL COMMENT '主营类目',
  `contact` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '联系人',
  `mobile` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '手机',
  `province_id` int DEFAULT NULL COMMENT '省',
  `city_id` int DEFAULT NULL COMMENT '市',
  `district_id` int DEFAULT NULL COMMENT '区',
  `address_info` text COLLATE utf8mb4_general_ci COMMENT '地址详情',
  `apply_content` text COLLATE utf8mb4_general_ci COMMENT '申请说明',
  `audit_status` int DEFAULT NULL COMMENT '状态 0待审核 1通过 2拒绝',
  `audit_content` text COLLATE utf8mb4_general_ci COMMENT '审核说明',
  `follow` int DEFAULT NULL COMMENT '跟进 0未跟进 1跟进中',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='商家申请';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_merchant_mall_shop_bank`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_merchant_mall_shop_bank` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `create_time` bigint DEFAULT NULL COMMENT '创建时间',
  `update_time` bigint DEFAULT NULL COMMENT '修改时间',
  `delete_time` bigint DEFAULT NULL COMMENT '删除时间',
  `shop_id` int DEFAULT NULL COMMENT '店铺id',
  `name` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '银行名称',
  `branch` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '支行名称',
  `is_default` int DEFAULT NULL COMMENT '是否默认',
  `username` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '开户名',
  `card_number` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '卡号',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='商家银行卡';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_merchant_mall_shop_category`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_merchant_mall_shop_category` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `create_time` bigint DEFAULT NULL COMMENT '创建时间',
  `update_time` bigint DEFAULT NULL COMMENT '修改时间',
  `delete_time` bigint DEFAULT NULL COMMENT '删除时间',
  `name` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '名称',
  `image` varchar(500) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '图片',
  `sort` int DEFAULT NULL COMMENT '排序',
  `shop_nums` int DEFAULT NULL COMMENT '店铺数量',
  `status` int DEFAULT NULL COMMENT '状态 1显示 0不显示',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='店铺经营类目';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_merchant_mall_shop_collect`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_merchant_mall_shop_collect` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `create_time` bigint DEFAULT NULL COMMENT '创建时间',
  `update_time` bigint DEFAULT NULL COMMENT '修改时间',
  `delete_time` bigint DEFAULT NULL COMMENT '删除时间',
  `user_id` int DEFAULT NULL COMMENT '用户ID',
  `shop_id` int DEFAULT NULL COMMENT '店铺id',
  `status` int DEFAULT NULL COMMENT '状态 1收藏 0取消收藏',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='店铺收藏';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_merchant_mall_shop_config`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_merchant_mall_shop_config` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `create_time` bigint DEFAULT NULL COMMENT '创建时间',
  `update_time` bigint DEFAULT NULL COMMENT '修改时间',
  `delete_time` bigint DEFAULT NULL COMMENT '删除时间',
  `name` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '名称',
  `type` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '类型',
  `value` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '值',
  `shop_id` int DEFAULT NULL COMMENT '店铺id',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='店铺配置';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_merchant_mall_shop_decorate`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_merchant_mall_shop_decorate` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `create_time` bigint DEFAULT NULL COMMENT '创建时间',
  `update_time` bigint DEFAULT NULL COMMENT '修改时间',
  `delete_time` bigint DEFAULT NULL COMMENT '删除时间',
  `shop_id` int DEFAULT NULL COMMENT '店铺装修',
  `type` int DEFAULT NULL COMMENT '类型 0店铺主页 1 客服',
  `content` text COLLATE utf8mb4_general_ci COMMENT '装修内容',
  `common` text COLLATE utf8mb4_general_ci COMMENT '公共配置',
  `is_home` int DEFAULT NULL COMMENT '是否首页',
  `name` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '页面名称',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='店铺装修';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_merchant_mall_shop_dept`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_merchant_mall_shop_dept` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `shop_id` int DEFAULT NULL COMMENT '店铺id',
  `pid` int DEFAULT NULL COMMENT '上级部门',
  `name` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '部门名称',
  `contact` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '负责人名',
  `mobile` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '联系电话',
  `sort` int DEFAULT NULL COMMENT '排序编号',
  `status` int DEFAULT NULL COMMENT '是否禁用: [0=否, 1=是]',
  `create_time` bigint DEFAULT NULL COMMENT '创建时间',
  `update_time` bigint DEFAULT NULL COMMENT '更新时间',
  `delete_time` bigint DEFAULT NULL COMMENT '删除时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='商家部门';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_merchant_mall_shop_goods_category`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_merchant_mall_shop_goods_category` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `create_time` bigint DEFAULT NULL COMMENT '创建时间',
  `update_time` bigint DEFAULT NULL COMMENT '修改时间',
  `delete_time` bigint DEFAULT NULL COMMENT '删除时间',
  `shop_id` int DEFAULT NULL COMMENT '店铺ID',
  `name` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '名称',
  `image` varchar(500) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '图标',
  `sort` int DEFAULT NULL COMMENT '排序',
  `is_show` int DEFAULT NULL COMMENT '是否显示',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='商家商品分类';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_merchant_mall_shop_job`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_merchant_mall_shop_job` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `code` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '岗位编码',
  `name` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '岗位名称',
  `remark` text COLLATE utf8mb4_general_ci COMMENT '岗位备注',
  `sort` int DEFAULT NULL COMMENT '岗位排序',
  `status` int DEFAULT NULL COMMENT '是否停用: [0=否, 1=是]',
  `shop_id` int DEFAULT NULL COMMENT '店铺id',
  `create_time` bigint DEFAULT NULL COMMENT '创建时间',
  `update_time` bigint DEFAULT NULL COMMENT '更新时间',
  `delete_time` bigint DEFAULT NULL COMMENT '删除时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='商家岗位';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_merchant_mall_shop_menu`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_merchant_mall_shop_menu` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `pid` int DEFAULT NULL COMMENT '上级菜单',
  `type` int DEFAULT NULL COMMENT '类型  0目录 1菜单 2按钮',
  `name` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '菜单名称',
  `icon` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '菜单图标',
  `route` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '路由地址',
  `sort` int DEFAULT NULL COMMENT '菜单排序',
  `component` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '前端组件',
  `selected` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '选中路径',
  `route_params` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '路由参数',
  `access` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '权限标识',
  `is_cache` int DEFAULT NULL COMMENT '是否缓存: [0=否, 1=是]',
  `status` int DEFAULT NULL COMMENT '状态 1正常 0禁用',
  `show_status` int DEFAULT NULL COMMENT '显示状态1 显示 0不显示',
  `create_time` bigint DEFAULT NULL COMMENT '创建时间',
  `update_time` bigint DEFAULT NULL COMMENT '更新时间',
  `delete_time` bigint DEFAULT NULL COMMENT '删除时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='商家菜单';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_merchant_mall_shop_role`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_merchant_mall_shop_role` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `create_time` bigint DEFAULT NULL COMMENT '创建时间',
  `update_time` bigint DEFAULT NULL COMMENT '修改时间',
  `delete_time` bigint DEFAULT NULL COMMENT '删除时间',
  `shop_id` int DEFAULT NULL COMMENT '店铺id',
  `name` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '角色名',
  `sort` int DEFAULT NULL COMMENT '排序',
  `desc` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '描述',
  `status` int DEFAULT NULL COMMENT '状态',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='商家角色';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_merchant_mall_shop_role_menu`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_merchant_mall_shop_role_menu` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `role_id` int DEFAULT NULL COMMENT '角色ID',
  `menu_id` int DEFAULT NULL COMMENT '菜单ID',
  `shop_id` int DEFAULT NULL COMMENT '店铺id',
  `create_time` bigint DEFAULT NULL COMMENT '创建时间',
  `update_time` bigint DEFAULT NULL COMMENT '更新时间',
  `delete_time` bigint DEFAULT NULL COMMENT '删除时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='商家角色菜单关系';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_merchant_mall_shop_session`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_merchant_mall_shop_session` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `create_time` bigint DEFAULT NULL COMMENT '创建时间',
  `update_time` bigint DEFAULT NULL COMMENT '修改时间',
  `delete_time` bigint DEFAULT NULL COMMENT '删除时间',
  `shop_id` int DEFAULT NULL COMMENT '店铺id',
  `admin_id` int DEFAULT NULL COMMENT '管理员id',
  `token` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT 'token',
  `terminal` int DEFAULT NULL COMMENT '终端',
  `expire_time` int DEFAULT NULL COMMENT '过期时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='商家登录';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_merchant_mall_shop_visit`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_merchant_mall_shop_visit` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `create_time` bigint DEFAULT NULL COMMENT '创建时间',
  `update_time` bigint DEFAULT NULL COMMENT '修改时间',
  `delete_time` bigint DEFAULT NULL COMMENT '删除时间',
  `shop_id` int DEFAULT NULL COMMENT '店铺',
  `num` int DEFAULT NULL COMMENT '访问数',
  `ip` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT 'ip',
  `terminal` int DEFAULT NULL COMMENT '终端',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='商家访问统计';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_merchant_mall_shop_withdraw`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_merchant_mall_shop_withdraw` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `create_time` bigint DEFAULT NULL COMMENT '创建时间',
  `update_time` bigint DEFAULT NULL COMMENT '修改时间',
  `delete_time` bigint DEFAULT NULL COMMENT '删除时间',
  `shop_id` int DEFAULT NULL COMMENT '店铺id',
  `sn` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '提现单号',
  `money` decimal(10,2) DEFAULT NULL COMMENT '提现金额',
  `real_money` decimal(10,2) DEFAULT NULL COMMENT '实际金额',
  `service_money` decimal(10,2) DEFAULT NULL COMMENT '手续费',
  `service_ratio` decimal(10,2) DEFAULT NULL COMMENT '手续费比例',
  `status` int DEFAULT NULL COMMENT '状态 0申请中 1处理中 2成功 3失败',
  `bank_id` int DEFAULT NULL COMMENT '银行id',
  `bank_name` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '银行名称',
  `bank_branch` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '支行名称',
  `bank_username` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '开户名',
  `bank_card_number` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '卡号',
  `content` text COLLATE utf8mb4_general_ci COMMENT '提现说明',
  `transfer_voucher` varchar(500) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '转账凭证',
  `transfer_time` bigint DEFAULT NULL COMMENT '转账时间',
  `transfer_content` text COLLATE utf8mb4_general_ci COMMENT '转账说明',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='商家提现';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_merchant_ms`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_merchant_ms` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `create_time` bigint DEFAULT NULL COMMENT '????',
  `update_time` bigint DEFAULT NULL COMMENT '????',
  `delete_time` bigint DEFAULT NULL COMMENT '????',
  `shop_id` int DEFAULT NULL COMMENT '??id',
  `goods_id` int DEFAULT NULL COMMENT '??id',
  `max_price` int DEFAULT NULL COMMENT '?????',
  `min_price` int DEFAULT NULL COMMENT '?????',
  `status` int DEFAULT NULL COMMENT '?? 0??? 1??? 2???',
  `start_time` bigint DEFAULT NULL COMMENT '????',
  `end_time` bigint DEFAULT NULL COMMENT '????',
  `audit_status` int DEFAULT NULL COMMENT '???? 0??? 10??? 20???? 30????',
  `audit_content` text COLLATE utf8mb4_general_ci COMMENT '????',
  `order_limit` int DEFAULT NULL COMMENT '???? 0???',
  `total_stock` int DEFAULT NULL COMMENT '???',
  `stock` int DEFAULT NULL COMMENT '????',
  `sale_num` int DEFAULT NULL COMMENT '???',
  `visit_num` int DEFAULT NULL COMMENT '???',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='????';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_merchant_ms_goods`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_merchant_ms_goods` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `create_time` bigint DEFAULT NULL COMMENT '创建时间',
  `update_time` bigint DEFAULT NULL COMMENT '修改时间',
  `delete_time` bigint DEFAULT NULL COMMENT '删除时间',
  `ms_id` int DEFAULT NULL COMMENT '秒杀id',
  `shop_id` int DEFAULT NULL COMMENT '店铺id',
  `goods_id` int DEFAULT NULL COMMENT '商品id',
  `goods_sku_id` int DEFAULT NULL COMMENT '商品sku id',
  `price` decimal(10,2) DEFAULT NULL COMMENT '秒杀价格',
  `price_int` int DEFAULT NULL COMMENT '秒杀价格 证整数',
  `total_stock` int DEFAULT NULL COMMENT '总库存',
  `stock` int DEFAULT NULL COMMENT '剩余库存',
  `sale_num` int DEFAULT NULL COMMENT '销量',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='秒杀活动商品';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_merchant_notice_record`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_merchant_notice_record` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `user_id` int DEFAULT NULL COMMENT '??',
  `title` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '??',
  `content` text COLLATE utf8mb4_general_ci COMMENT '??',
  `scene_id` int DEFAULT NULL COMMENT '??',
  `read` int DEFAULT NULL COMMENT '????: [0=??, 1=??]',
  `recipient` int DEFAULT NULL COMMENT '????????;1-??;2-??;3-??;4-??(?????)',
  `send_type` int DEFAULT NULL COMMENT '?????? 1-???? 2-???? 3-???? 4-?????',
  `notice_type` int DEFAULT NULL COMMENT '???? 1-???? 2-???',
  `extra` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '??',
  `create_time` bigint DEFAULT NULL COMMENT '????',
  `update_time` bigint DEFAULT NULL COMMENT '????',
  `delete_time` bigint DEFAULT NULL COMMENT '????',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='????';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_merchant_notice_setting`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_merchant_notice_setting` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `scene_id` int DEFAULT NULL COMMENT '场景编号',
  `scene_name` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '场景名称',
  `scene_desc` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '场景描述',
  `recipient` int DEFAULT NULL COMMENT '接收人员: [1=用户, 2=平台]',
  `type` int DEFAULT NULL COMMENT '通知类型: [1=业务, 2=验证码]',
  `system_notice` text COLLATE utf8mb4_general_ci COMMENT '系统的通知设置',
  `sms_notice` text COLLATE utf8mb4_general_ci COMMENT '短信的通知设置',
  `oa_notice` text COLLATE utf8mb4_general_ci COMMENT '公众号通知设置',
  `mnp_notice` text COLLATE utf8mb4_general_ci COMMENT '小程序通知设置',
  `update_time` bigint DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='通知设置';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_merchant_official_account_reply`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_merchant_official_account_reply` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `name` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '规则名',
  `keyword` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '关键词',
  `reply_type` int DEFAULT NULL COMMENT '回复类型: [1=关注回复 2=关键字回复, 3=默认回复]',
  `matching_type` int DEFAULT NULL COMMENT '匹配方式: [1=全匹配, 2=模糊匹配]',
  `content_type` int DEFAULT NULL COMMENT '内容类型: [1=文本]',
  `status` int DEFAULT NULL COMMENT '启动状态: [1=启动, 0=关闭]',
  `content` text COLLATE utf8mb4_general_ci COMMENT '回复内容',
  `sort` int DEFAULT NULL COMMENT '排序编号',
  `create_time` bigint DEFAULT NULL COMMENT '创建时间',
  `update_time` bigint DEFAULT NULL COMMENT '更新时间',
  `delete_time` bigint DEFAULT NULL COMMENT '删除时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='公众号回复';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_merchant_operation_log`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_merchant_operation_log` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `admin_id` int DEFAULT NULL COMMENT '???ID',
  `admin_name` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '?????',
  `account` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '??',
  `action` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '????',
  `type` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '????: GET/POST/PUT',
  `url` varchar(500) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '????',
  `params` text COLLATE utf8mb4_general_ci COMMENT '????',
  `result` text COLLATE utf8mb4_general_ci COMMENT '????',
  `ip` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '??IP',
  `create_time` bigint DEFAULT NULL COMMENT '????',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='??????';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_merchant_printer`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_merchant_printer` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `create_time` bigint DEFAULT NULL COMMENT '????',
  `update_time` bigint DEFAULT NULL COMMENT '????',
  `delete_time` bigint DEFAULT NULL COMMENT '????',
  `shop_id` int DEFAULT NULL COMMENT '??id',
  `name` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '??',
  `status` int DEFAULT NULL COMMENT '?? 1?? 0??',
  `brand` int DEFAULT NULL COMMENT '????? 0?? 1???',
  `order_print` text COLLATE utf8mb4_general_ci COMMENT '????????',
  `content` text COLLATE utf8mb4_general_ci COMMENT '?????',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='???';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_merchant_printer_template`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_merchant_printer_template` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `create_time` bigint DEFAULT NULL COMMENT '创建时间',
  `update_time` bigint DEFAULT NULL COMMENT '修改时间',
  `delete_time` bigint DEFAULT NULL COMMENT '删除时间',
  `shop_id` int DEFAULT NULL COMMENT '店铺id',
  `type` int DEFAULT NULL COMMENT '类型 0商品订单',
  `name` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '名称',
  `printer_use` int DEFAULT NULL COMMENT '是否显示 0未使用 1已使用',
  `content` text COLLATE utf8mb4_general_ci COMMENT '模版设置',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='打印机模版';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_merchant_pt`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_merchant_pt` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `create_time` bigint DEFAULT NULL COMMENT '????',
  `update_time` bigint DEFAULT NULL COMMENT '????',
  `delete_time` bigint DEFAULT NULL COMMENT '????',
  `shop_id` int DEFAULT NULL COMMENT '??id',
  `goods_id` int DEFAULT NULL COMMENT '??id',
  `max_price` int DEFAULT NULL COMMENT '?????',
  `min_price` int DEFAULT NULL COMMENT '?????',
  `status` int DEFAULT NULL COMMENT '?? 0??? 1??? 2???',
  `audit_status` int DEFAULT NULL COMMENT '???? 0??? 10??? 20???? 30????',
  `audit_content` text COLLATE utf8mb4_general_ci COMMENT '????',
  `start_time` bigint DEFAULT NULL COMMENT '????',
  `end_time` bigint DEFAULT NULL COMMENT '????',
  `time_limit` int DEFAULT NULL COMMENT '???? ??',
  `user_num` int DEFAULT NULL COMMENT '????',
  `sale_num` int DEFAULT NULL COMMENT '???',
  `visit_num` int DEFAULT NULL COMMENT '???',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='????';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_merchant_pt_goods`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_merchant_pt_goods` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `create_time` bigint DEFAULT NULL COMMENT '创建时间',
  `update_time` bigint DEFAULT NULL COMMENT '修改时间',
  `delete_time` bigint DEFAULT NULL COMMENT '删除时间',
  `pt_id` int DEFAULT NULL COMMENT '拼团id',
  `shop_id` int DEFAULT NULL COMMENT '店铺id',
  `goods_id` int DEFAULT NULL COMMENT '商品id',
  `goods_sku_id` int DEFAULT NULL COMMENT '商品sku id',
  `price` decimal(10,2) DEFAULT NULL COMMENT '拼团价格',
  `price_int` int DEFAULT NULL COMMENT '拼团价格 正整数',
  `sale_num` int DEFAULT NULL COMMENT '销量',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='拼团活动商品';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_merchant_pt_open`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_merchant_pt_open` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `create_time` bigint DEFAULT NULL COMMENT '创建时间',
  `update_time` bigint DEFAULT NULL COMMENT '修改时间',
  `delete_time` bigint DEFAULT NULL COMMENT '删除时间',
  `pt_id` int DEFAULT NULL COMMENT '拼团活动id',
  `user_id` int DEFAULT NULL COMMENT '用户id',
  `shop_id` int DEFAULT NULL COMMENT '店铺id',
  `timeout` int DEFAULT NULL COMMENT '失效时间',
  `order_id` int DEFAULT NULL COMMENT '订单id',
  `order_goods_id` int DEFAULT NULL COMMENT '订单商品id',
  `status` int DEFAULT NULL COMMENT '状态 0拼团中 1拼团成功 2拼团失败',
  `user_num` int DEFAULT NULL COMMENT '开团人数',
  `join_num` int DEFAULT NULL COMMENT '参团人数',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='拼团开团';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_merchant_pt_open_join`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_merchant_pt_open_join` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `create_time` bigint DEFAULT NULL COMMENT '创建时间',
  `update_time` bigint DEFAULT NULL COMMENT '修改时间',
  `delete_time` bigint DEFAULT NULL COMMENT '删除时间',
  `pt_id` int DEFAULT NULL COMMENT '拼团id',
  `pt_open_id` int DEFAULT NULL COMMENT '拼团开团id',
  `user_id` int DEFAULT NULL COMMENT '用户id',
  `shop_id` int DEFAULT NULL COMMENT '店铺id',
  `order_id` int DEFAULT NULL COMMENT '订单id',
  `order_goods_id` int DEFAULT NULL COMMENT '订单商品id',
  `is_open` int DEFAULT NULL COMMENT '是否团长 1是 0否',
  `is_refund` int DEFAULT NULL COMMENT '已退款 1是 0否 拼团失败时退款记录',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='拼团参团';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_merchant_refund_log`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_merchant_refund_log` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `sn` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '编号',
  `record_id` int DEFAULT NULL COMMENT '退款记录ID',
  `user_id` int DEFAULT NULL COMMENT '关联用户ID',
  `handle_id` int DEFAULT NULL COMMENT '处理管理ID',
  `order_amount` decimal(10,2) DEFAULT NULL COMMENT '总应付的金额',
  `refund_amount` decimal(10,2) DEFAULT NULL COMMENT '本次退款金额',
  `refund_status` int DEFAULT NULL COMMENT '退款状态: [0=退款中, 1=退款成功, 2=退款失败]',
  `refund_msg` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '退款信息',
  `create_time` bigint DEFAULT NULL COMMENT '创建时间',
  `update_time` bigint DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='退款日志';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_merchant_refund_record`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_merchant_refund_record` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `sn` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '退款编号',
  `user_id` int DEFAULT NULL COMMENT '关联用户ID',
  `order_id` int DEFAULT NULL COMMENT '来源订单ID',
  `order_sn` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '来源单号SN',
  `order_type` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '订单类型: [order=商品订单, recharge=充值订单]',
  `order_amount` decimal(10,2) DEFAULT NULL COMMENT '总应付款金额',
  `refund_amount` decimal(10,2) DEFAULT NULL COMMENT '本次退款金额',
  `transaction_id` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '平台交易流水号',
  `refund_way` int DEFAULT NULL COMMENT '退款方式: 1=线上退款, 2=线下退款',
  `refund_type` int DEFAULT NULL COMMENT '退款类型: 1=后台退款',
  `refund_status` int DEFAULT NULL COMMENT '退款状态: 0=退款中, 1=退款成功, 2=退款失败',
  `create_time` bigint DEFAULT NULL COMMENT '创建时间',
  `update_time` bigint DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='退款记录';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_merchant_sms_log`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_merchant_sms_log` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `scene_id` int DEFAULT NULL COMMENT '??',
  `mobile` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '????',
  `content` text COLLATE utf8mb4_general_ci COMMENT '????',
  `code` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '??????????????',
  `is_verify` int DEFAULT NULL COMMENT '??????0-??1-?',
  `check_num` int DEFAULT NULL COMMENT '????',
  `send_status` int DEFAULT NULL COMMENT '?????0-????1-?????2-????',
  `send_time` bigint DEFAULT NULL COMMENT '????',
  `results` text COLLATE utf8mb4_general_ci COMMENT '????',
  `create_time` bigint DEFAULT NULL COMMENT '????',
  `update_time` bigint DEFAULT NULL COMMENT '????',
  `delete_time` bigint DEFAULT NULL COMMENT '????',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='????';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_merchant_system_log_login`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_merchant_system_log_login` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `admin_id` int DEFAULT NULL COMMENT '管理员ID',
  `username` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '登录账号',
  `ip` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '登录IP',
  `os` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '操作系统',
  `browser` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '浏览器',
  `status` int DEFAULT NULL COMMENT '操作状态: [1=成功, 2=失败]',
  `create_time` bigint DEFAULT NULL COMMENT '创建时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='系统登录日志';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_merchant_system_menu`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_merchant_system_menu` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `pid` int DEFAULT NULL COMMENT '上级菜单',
  `type` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '权限类型: [M=目录, C=菜单, A=按钮]',
  `name` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '菜单名称',
  `icon` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '菜单图标',
  `sort` int DEFAULT NULL COMMENT '菜单排序',
  `perms` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '权限标识',
  `paths` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '路由地址',
  `component` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '前端组件',
  `selected` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '选中路径',
  `params` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '路由参数',
  `is_cache` int DEFAULT NULL COMMENT '是否缓存: [0=否, 1=是]',
  `is_show` int DEFAULT NULL COMMENT '是否显示: [0=否, 1=是]',
  `is_disable` int DEFAULT NULL COMMENT '是否禁用: [0=否, 1=是]',
  `create_time` bigint DEFAULT NULL COMMENT '创建时间',
  `update_time` bigint DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='系统菜单';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_merchant_system_role`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_merchant_system_role` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `name` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '角色名称',
  `desc` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '描述',
  `sort` int DEFAULT NULL COMMENT '角色排序',
  `create_time` bigint DEFAULT NULL COMMENT '创建时间',
  `update_time` bigint DEFAULT NULL COMMENT '更新时间',
  `delete_time` bigint DEFAULT NULL COMMENT '删除时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='系统角色';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_merchant_system_role_menu`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_merchant_system_role_menu` (
  `role_id` int DEFAULT NULL COMMENT '角色ID',
  `menu_id` int DEFAULT NULL COMMENT '菜单ID'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='角色菜单关系';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_merchant_user`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_merchant_user` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `sn` int DEFAULT NULL COMMENT '??',
  `avatar` varchar(500) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '????',
  `real_name` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '????',
  `nickname` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '????',
  `account` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '????',
  `password` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '????',
  `mobile` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '????',
  `sex` int DEFAULT NULL COMMENT '????: [1=?, 2=?]',
  `birthday` int DEFAULT NULL COMMENT '??',
  `channel` int DEFAULT NULL COMMENT '????',
  `is_disable` int DEFAULT NULL COMMENT '????: [0=?, 1=?]',
  `login_ip` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '????IP',
  `login_time` bigint DEFAULT NULL COMMENT '??????',
  `is_new_user` int DEFAULT NULL COMMENT '??????: [0=?, 1=?]',
  `user_money` decimal(10,2) DEFAULT NULL COMMENT '????',
  `total_use_money` decimal(10,2) DEFAULT NULL COMMENT '?????',
  `total_order_money` decimal(10,2) DEFAULT NULL COMMENT '????',
  `total_order_num` decimal(10,2) DEFAULT NULL COMMENT '???',
  `total_recharge_money` decimal(10,2) DEFAULT NULL COMMENT '????',
  `order_last_time` bigint DEFAULT NULL COMMENT '??????',
  `invite_id` int DEFAULT NULL COMMENT '???id',
  `admin_update_leader` int DEFAULT NULL COMMENT '???????? 0? 1?',
  `gid` int DEFAULT NULL COMMENT '??id',
  `pid` int DEFAULT NULL COMMENT '??id',
  `next_nums` int DEFAULT NULL COMMENT '???',
  `user_level_id` int DEFAULT NULL COMMENT '????id',
  `is_distribution` int DEFAULT NULL COMMENT '?????',
  `distribution_time` bigint DEFAULT NULL COMMENT '???????',
  `distribution_status` int DEFAULT NULL COMMENT '????? 1?? 0??',
  `distribution_level_id` int DEFAULT NULL COMMENT '?????id',
  `distribution_money` decimal(10,2) DEFAULT NULL COMMENT '????',
  `distribution_total_money` decimal(10,2) DEFAULT NULL COMMENT '?????',
  `distribution_code` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '???',
  `distribution_bind_time` bigint DEFAULT NULL COMMENT '????',
  `create_time` bigint DEFAULT NULL COMMENT '????',
  `update_time` bigint DEFAULT NULL COMMENT '????',
  `delete_time` bigint DEFAULT NULL COMMENT '????',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='????';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_merchant_user_address`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_merchant_user_address` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `user_id` int DEFAULT NULL COMMENT '用户ID',
  `contact` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '联系人名',
  `mobile` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '联系方式',
  `province_id` int DEFAULT NULL COMMENT '省',
  `city_id` int DEFAULT NULL COMMENT '市',
  `district_id` int DEFAULT NULL COMMENT '区',
  `info` text COLLATE utf8mb4_general_ci COMMENT '详细地址',
  `longitude` int DEFAULT NULL COMMENT '经度',
  `latitude` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '纬度',
  `is_default` int DEFAULT NULL COMMENT '是否默认: [0=否, 1=是]',
  `create_time` bigint DEFAULT NULL COMMENT '创建时间',
  `update_time` bigint DEFAULT NULL COMMENT '修改时间',
  `delete_time` bigint DEFAULT NULL COMMENT '删除时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='用户地址';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_merchant_user_auth`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_merchant_user_auth` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `user_id` int DEFAULT NULL COMMENT '用户ID',
  `openid` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT 'Openid',
  `unionid` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT 'Unionid',
  `terminal` int DEFAULT NULL COMMENT '客户端类型: [1=微信小程序, 2=微信公众号, 3=手机H5, 4=电脑PC, 5=苹果APP, 6=安卓APP]',
  `create_time` bigint DEFAULT NULL COMMENT '创建时间',
  `update_time` bigint DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='用户授权';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_merchant_user_level`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_merchant_user_level` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `create_time` bigint DEFAULT NULL COMMENT '创建时间',
  `update_time` bigint DEFAULT NULL COMMENT '更新时间',
  `delete_time` bigint DEFAULT NULL COMMENT '删除时间',
  `name` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '名称',
  `level` int DEFAULT NULL COMMENT '等级',
  `icon` varchar(500) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '图标',
  `bg` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '背景',
  `description` text COLLATE utf8mb4_general_ci COMMENT '描述',
  `is_discount` int DEFAULT NULL COMMENT '等级折扣 1参与等级折扣 0无等级折扣',
  `discount_ratio` decimal(10,2) DEFAULT NULL COMMENT '等级折扣比例',
  `is_default` int DEFAULT NULL COMMENT '是否默认等级',
  `condition` int DEFAULT NULL COMMENT '等级条件 -1未设置 0任意条件 1全部条件',
  `order_one_money_is` int DEFAULT NULL COMMENT '单次消费金额 条件开启 1是0否',
  `order_one_money` decimal(10,2) DEFAULT NULL COMMENT '单次消费金额',
  `order_all_money_is` int DEFAULT NULL COMMENT '累计消费金额 条件开启 1是0否',
  `order_all_money` decimal(10,2) DEFAULT NULL COMMENT '累计消费金额',
  `order_all_num_is` int DEFAULT NULL COMMENT '累计消费次数 条件开启 1是0否',
  `order_all_num` int DEFAULT NULL COMMENT '累计消费次数',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='用户等级';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_merchant_user_recharge`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_merchant_user_recharge` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `user_id` int DEFAULT NULL COMMENT '用户ID',
  `money` decimal(10,2) DEFAULT NULL COMMENT '充值金额',
  `sn` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '订单编号',
  `pay_way` int DEFAULT NULL COMMENT '支付方式: [2=微信支付, 3=支付宝支付]',
  `pay_status` int DEFAULT NULL COMMENT '支付状态: [0=待支付, 1=已支付]',
  `pay_time` bigint DEFAULT NULL COMMENT '支付时间',
  `terminal` int DEFAULT NULL COMMENT '下单终端',
  `transaction_sn` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '支付编号',
  `wechat_mini_express_sync` int DEFAULT NULL COMMENT '微信小程序发货信息录入',
  `wechat_mini_express_sync_time` bigint DEFAULT NULL COMMENT '录入时间',
  `create_time` bigint DEFAULT NULL COMMENT '创建时间',
  `update_time` bigint DEFAULT NULL COMMENT '更新时间',
  `delete_time` bigint DEFAULT NULL COMMENT '删除时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='充值订单';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_merchant_user_session`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_merchant_user_session` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `user_id` int DEFAULT NULL COMMENT '用户ID',
  `terminal` int DEFAULT NULL COMMENT '客户端类型',
  `token` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '令牌',
  `update_time` bigint DEFAULT NULL COMMENT '创建时间',
  `expire_time` bigint DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='用户会话';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_merchant_user_withdraw`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_merchant_user_withdraw` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `create_time` bigint DEFAULT NULL COMMENT '创建时间',
  `update_time` bigint DEFAULT NULL COMMENT '修改时间',
  `delete_time` bigint DEFAULT NULL COMMENT '删除时间',
  `user_id` int DEFAULT NULL COMMENT '用户id',
  `sn` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '提现单号',
  `batch_no` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '提现批次',
  `type` int DEFAULT NULL COMMENT '类型 1钱包余额 2微信零钱 3银行卡 4微信收款码 5支付宝收款码',
  `status` int DEFAULT NULL COMMENT '状态 0待提 1提现 2提现成 3提现失败',
  `money` decimal(10,2) DEFAULT NULL COMMENT '提现金额',
  `real_money` decimal(10,2) DEFAULT NULL COMMENT '实际金额',
  `service_money` decimal(10,2) DEFAULT NULL COMMENT '手续费',
  `bank_name` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '银行',
  `bank_sub_name` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '银行支行',
  `bank_account` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '银行卡账号',
  `bank_account_name` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '银行卡姓名',
  `wechat_account` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '微信账号',
  `wechat_account_name` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '微信真实姓名',
  `wechat_qr_code` varchar(500) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '微信收款码',
  `wechat_pay_message` text COLLATE utf8mb4_general_ci COMMENT '微信转账零钱 支付信息',
  `wechat_pay_result` text COLLATE utf8mb4_general_ci COMMENT '微信转账零钱 结果信息',
  `wechat_pay_no` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '微信转账零钱 支付单号',
  `wechat_pay_time` bigint DEFAULT NULL COMMENT '微信转账零钱 支付时间',
  `alipay_account` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '支付宝账号',
  `alipay_account_name` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '支付宝真实姓名',
  `alipay_qr_code` varchar(500) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '支付宝收款码',
  `remark` text COLLATE utf8mb4_general_ci COMMENT '提现备注',
  `audit_content` text COLLATE utf8mb4_general_ci COMMENT '审核备注',
  `transfer_voucher` varchar(500) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '转账凭证',
  `transfer_time` bigint DEFAULT NULL COMMENT '转账时间',
  `transfer_content` text COLLATE utf8mb4_general_ci COMMENT '转账备注',
  `wx_transfer_time` bigint DEFAULT NULL COMMENT '发起微信转账时间',
  `wx_transfer_fail_reason` text COLLATE utf8mb4_general_ci COMMENT '微信转账失败原因',
  `package_info` text COLLATE utf8mb4_general_ci COMMENT '微信支付收款页的package信息',
  `sub_status` int DEFAULT NULL COMMENT '子状态: 0-无需处理 1-微信待用户收款 2.微信用户收款操作完成 3.已收微信回调',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='用户提现';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_message_channel`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_message_channel` (
  `id` int unsigned NOT NULL AUTO_INCREMENT,
  `channel_code` varchar(32) NOT NULL COMMENT '渠道编码: INBOX/EMAIL/SMS/DINGTALK/WECOM/WECHAT/FEISHU',
  `channel_name` varchar(50) NOT NULL COMMENT '渠道名称',
  `config` text COMMENT '渠道配置JSON(AppKey/Secret等)',
  `status` tinyint(1) DEFAULT '1' COMMENT '状态: 0禁用 1启用',
  `sort` int DEFAULT '0' COMMENT '排序',
  `org_id` varchar(32) DEFAULT NULL COMMENT '组织ID',
  `create_time` int DEFAULT NULL COMMENT '创建时间',
  `update_time` int DEFAULT NULL COMMENT '更新时间',
  `delete_time` int DEFAULT NULL COMMENT '删除时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_channel_code` (`channel_code`,`org_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='消息渠道配置表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_message_inbox`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_message_inbox` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `user_id` varchar(32) NOT NULL COMMENT '用户ID',
  `title` varchar(200) NOT NULL COMMENT '消息标题',
  `content` text COMMENT '消息内容',
  `msg_type` tinyint(1) DEFAULT '1' COMMENT '消息类型: 1系统通知 2业务提醒 3待办事项',
  `biz_type` varchar(32) DEFAULT NULL COMMENT '业务类型',
  `biz_id` varchar(64) DEFAULT NULL COMMENT '业务ID(点击跳转用)',
  `is_read` tinyint(1) DEFAULT '0' COMMENT '是否已读: 0未读 1已读',
  `read_time` int DEFAULT NULL COMMENT '阅读时间',
  `org_id` varchar(32) DEFAULT NULL COMMENT '组织ID',
  `create_time` int DEFAULT NULL COMMENT '创建时间',
  `delete_time` int DEFAULT NULL COMMENT '删除时间',
  `idempotency_key` varchar(160) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_message_inbox_idempotency` (`idempotency_key`),
  KEY `idx_user_read` (`user_id`,`is_read`),
  KEY `idx_biz` (`biz_type`,`biz_id`),
  KEY `idx_create_time` (`create_time`),
  KEY `idx_message_inbox_owner` (`user_id`,`org_id`,`is_read`,`delete_time`,`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='用户消息收件箱';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_message_jpush_config`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_message_jpush_config` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `org_id` varchar(64) NOT NULL COMMENT '组织ID',
  `app_key` varchar(128) NOT NULL COMMENT '极光应用 AppKey',
  `master_secret` varchar(128) NOT NULL COMMENT '极光应用 Master Secret',
  `apns_enabled` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否启用iOS推送(APNs)：0-否 1-是',
  `apns_certificate` text COMMENT 'APNs证书(.p12)路径',
  `apns_password` varchar(128) DEFAULT NULL COMMENT 'APNs证书密码',
  `apns_production` tinyint(1) NOT NULL DEFAULT '0' COMMENT 'APNs环境：0-开发 1-生产',
  `default_title` varchar(200) DEFAULT NULL COMMENT '默认推送标题',
  `default_icon` varchar(500) DEFAULT NULL COMMENT '默认推送图标URL',
  `channel` varchar(50) DEFAULT NULL COMMENT '推送渠道：android/ios/all',
  `status` tinyint(1) NOT NULL DEFAULT '1' COMMENT '状态：0-禁用 1-启用',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_org_id` (`org_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='极光推送配置表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_message_jpush_history`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_message_jpush_history` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `org_id` varchar(64) NOT NULL COMMENT '组织ID',
  `push_type` varchar(30) NOT NULL COMMENT '推送类型：single-单推 group-群推 tag-标签推送 broadcast-广播',
  `title` varchar(200) NOT NULL COMMENT '推送标题',
  `content` text NOT NULL COMMENT '推送内容',
  `target_type` varchar(30) DEFAULT NULL COMMENT '目标类型：registration_id/alias/tag/all',
  `target_value` text COMMENT '目标值（registration_id列表/别名/标签，JSON数组）',
  `platform` varchar(50) DEFAULT 'all' COMMENT '推送平台：android/ios/all',
  `extras` text COMMENT '扩展字段(JSON)',
  `msg_type` varchar(30) DEFAULT 'notification' COMMENT '消息类型：notification-通知 message-自定义消息',
  `send_status` tinyint(1) NOT NULL DEFAULT '0' COMMENT '发送状态：0-待发送 1-发送成功 2-发送失败 3-部分成功',
  `send_time` datetime DEFAULT NULL COMMENT '发送时间',
  `target_count` int DEFAULT '0' COMMENT '目标设备数',
  `success_count` int DEFAULT '0' COMMENT '成功送达数',
  `fail_count` int DEFAULT '0' COMMENT '失败数',
  `click_count` int DEFAULT '0' COMMENT '点击数',
  `jpush_send_id` varchar(128) DEFAULT NULL COMMENT '极光返回的发送ID',
  `error_msg` text COMMENT '错误信息',
  `operator_id` int DEFAULT NULL COMMENT '操作人ID',
  `operator_name` varchar(50) DEFAULT NULL COMMENT '操作人姓名',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_org_id` (`org_id`),
  KEY `idx_push_type` (`push_type`),
  KEY `idx_send_status` (`send_status`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='极光推送历史记录表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_message_record`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_message_record` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `template_code` varchar(64) DEFAULT NULL COMMENT '模板编码',
  `biz_type` varchar(32) DEFAULT NULL COMMENT '业务类型',
  `biz_id` varchar(64) DEFAULT NULL COMMENT '业务ID',
  `channel` varchar(32) NOT NULL COMMENT '发送渠道',
  `receiver_id` varchar(32) NOT NULL COMMENT '接收人ID',
  `receiver_name` varchar(50) DEFAULT NULL COMMENT '接收人姓名',
  `receiver_contact` varchar(100) DEFAULT NULL COMMENT '接收人联系方式(手机/邮箱)',
  `title` varchar(200) DEFAULT NULL COMMENT '消息标题',
  `content` text COMMENT '消息内容',
  `send_status` tinyint(1) DEFAULT '0' COMMENT '发送状态: 0待发送 1发送中 2成功 3失败',
  `send_time` int DEFAULT NULL COMMENT '发送时间',
  `fail_reason` varchar(500) DEFAULT NULL COMMENT '失败原因',
  `retry_count` int DEFAULT '0' COMMENT '重试次数',
  `org_id` varchar(32) DEFAULT NULL COMMENT '组织ID',
  `create_time` int DEFAULT NULL COMMENT '创建时间',
  `idempotency_key` varchar(160) DEFAULT NULL,
  `event_type` varchar(48) DEFAULT NULL,
  `error_code` varchar(64) DEFAULT NULL,
  `provider_message_id` varchar(160) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_message_record_idempotency` (`idempotency_key`),
  KEY `idx_biz` (`biz_type`,`biz_id`),
  KEY `idx_receiver` (`receiver_id`),
  KEY `idx_send_status` (`send_status`),
  KEY `idx_create_time` (`create_time`),
  KEY `idx_message_record_org_filter` (`org_id`,`biz_type`,`channel`,`send_status`,`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='消息发送记录表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_message_setting`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_message_setting` (
  `id` int NOT NULL AUTO_INCREMENT,
  `scene_id` int NOT NULL COMMENT '场景id',
  `scene_name` varchar(255) NOT NULL DEFAULT '' COMMENT '场景名称',
  `scene_desc` varchar(255) NOT NULL DEFAULT '' COMMENT '场景描述',
  `recipient` tinyint(1) NOT NULL DEFAULT '1' COMMENT '接收者 1-用户 2-平台',
  `type` tinyint(1) NOT NULL DEFAULT '1' COMMENT '通知类型: 1-业务通知 2-验证码',
  `system_notice` text COMMENT '系统通知设置',
  `sms_notice` text COMMENT '短信通知设置',
  `oa_notice` text COMMENT '公众号通知设置',
  `mnp_notice` text COMMENT '小程序通知设置',
  `support` char(10) NOT NULL DEFAULT '' COMMENT '支持的发送类型 1-系统通知 2-短信通知 3-微信模板消息 4-小程序提醒',
  `update_time` int DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='通知设置表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_message_subscribe`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_message_subscribe` (
  `id` int unsigned NOT NULL AUTO_INCREMENT,
  `user_id` varchar(32) NOT NULL COMMENT '用户ID',
  `biz_type` varchar(32) NOT NULL COMMENT '业务类型',
  `channel_inbox` tinyint(1) DEFAULT '1' COMMENT '站内信: 0关闭 1开启',
  `channel_email` tinyint(1) DEFAULT '0' COMMENT '邮件: 0关闭 1开启',
  `channel_sms` tinyint(1) DEFAULT '0' COMMENT '短信: 0关闭 1开启',
  `channel_dingtalk` tinyint(1) DEFAULT '0' COMMENT '钉钉: 0关闭 1开启',
  `channel_wecom` tinyint(1) DEFAULT '0' COMMENT '企微: 0关闭 1开启',
  `channel_wechat` tinyint(1) DEFAULT '0' COMMENT '微信: 0关闭 1开启',
  `channel_feishu` tinyint(1) DEFAULT '0' COMMENT '飞书: 0关闭 1开启',
  `org_id` varchar(32) DEFAULT NULL COMMENT '组织ID',
  `create_time` int DEFAULT NULL COMMENT '创建时间',
  `update_time` int DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_biz` (`user_id`,`biz_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='用户消息订阅设置';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_message_template`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_message_template` (
  `id` int unsigned NOT NULL AUTO_INCREMENT,
  `template_code` varchar(64) NOT NULL COMMENT '模板编码(唯一)',
  `template_name` varchar(100) NOT NULL COMMENT '模板名称',
  `biz_type` varchar(32) DEFAULT NULL COMMENT '业务类型: PROJECT/TASK/ORDER/WORKFLOW等',
  `title_template` varchar(200) DEFAULT NULL COMMENT '标题模板',
  `content_template` text COMMENT '内容模板(支持变量如${projectName})',
  `channels` varchar(100) DEFAULT 'INBOX' COMMENT '支持渠道(逗号分隔): INBOX,EMAIL,SMS',
  `status` tinyint(1) DEFAULT '1' COMMENT '状态: 0禁用 1启用',
  `remark` varchar(255) DEFAULT NULL COMMENT '备注',
  `org_id` varchar(32) DEFAULT NULL COMMENT '组织ID',
  `create_time` int DEFAULT NULL COMMENT '创建时间',
  `update_time` int DEFAULT NULL COMMENT '更新时间',
  `delete_time` int DEFAULT NULL COMMENT '删除时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_template_code` (`template_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='消息模板表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_order_balance_recharge`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_order_balance_recharge` (
  `id` int NOT NULL AUTO_INCREMENT,
  `user_id` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '用户id',
  `user_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '用户名',
  `user_mobile` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '联系方式',
  `pay_code` tinyint(1) DEFAULT NULL COMMENT '充值方式 1支付宝 2 微信 3 线下支付',
  `recharge_order_start` tinyint(1) DEFAULT NULL COMMENT '支付状态 1 待支付 2 待审核 3 审核失败 4 已取消 5 已完成',
  `recharge_order_id` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '充值单号',
  `recharge_amount` decimal(10,2) DEFAULT NULL COMMENT '充值金额',
  `recharge_image` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci COMMENT '线下支付 审核凭证',
  `recharge_msg` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '审核备注',
  `recharge_other_number` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '第三方支付平台交易号',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci ROW_FORMAT=DYNAMIC COMMENT='余额充值订单表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_order_barter`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_order_barter` (
  `order_barter_id` int NOT NULL AUTO_INCREMENT COMMENT '换货id',
  `shop_id` int DEFAULT NULL COMMENT '商家id',
  `barter_shipping_code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '换货物流单号',
  `barter_post_code` int DEFAULT NULL COMMENT '换货公司id',
  `barter_mobile` varchar(18) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '换货手机号',
  `order_return_id` int DEFAULT NULL COMMENT '订单退款id',
  `order_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '订单id',
  `return_code` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '退款编号',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `barter_status` tinyint(1) DEFAULT NULL COMMENT '换货状态 0发出换货物流 1用户确认收货 ',
  `delivery_time` datetime DEFAULT NULL COMMENT '收货时间',
  PRIMARY KEY (`order_barter_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci ROW_FORMAT=DYNAMIC COMMENT='订单换货表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_order_base`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_order_base` (
  `order_id` varchar(100) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL COMMENT '订单号',
  `shop_id` int NOT NULL COMMENT '卖家店铺id',
  `order_gate_shop_id` int DEFAULT NULL COMMENT '卖家门店id',
  `shop_name` varchar(50) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL COMMENT '卖家店铺名称',
  `deliver_shop_id` int DEFAULT NULL COMMENT '发货店铺id',
  `deliver_shop_name` varchar(50) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '发货店铺',
  `buyer_user_id` varchar(50) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL DEFAULT '0' COMMENT '买家id',
  `buyer_user_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '买家姓名',
  `seller_user_id` varchar(18) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '卖家id',
  `seller_user_name` varchar(50) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '卖家姓名',
  `order_date` date NOT NULL COMMENT '订单日期',
  `order_create_time` datetime NOT NULL COMMENT '订单生成时间',
  `order_receiver_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '收货人的姓名',
  `order_receiver_address` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL COMMENT '收货人的详细地址',
  `order_receiver_contact` varchar(50) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL COMMENT '收货人的联系方式',
  `order_receiver_date` datetime DEFAULT NULL COMMENT '收货时间（最晚收货时间）',
  `order_receiver_province_id` int NOT NULL DEFAULT '0' COMMENT '收货人收货地址区域ID',
  `order_receiver_city_id` int NOT NULL DEFAULT '0' COMMENT '收货人市ID（便于后期扩充）',
  `order_receiver_area_id` int NOT NULL DEFAULT '0' COMMENT '收货人区ID（便于后期统计扩充）',
  `payment_id` varchar(50) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '支付方式id',
  `payment_name` varchar(50) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '支付方式名称',
  `payment_time` datetime DEFAULT NULL COMMENT '支付(付款)时间',
  `payment_number` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '支付单号',
  `payment_other_number` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '第三方支付平台交易号',
  `pay_code` int DEFAULT NULL COMMENT '支付方式1-余额支付，2-支付宝支付，3-微信支付，6-购物卡支付，12 线下支付 16-paypal支付 17-0元兑换券 18 Stripe 支付',
  `order_seller_name` varchar(50) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '发货人的姓名',
  `order_seller_address` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '发货人的地址',
  `order_seller_contact` varchar(50) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '发货人的联系方式',
  `order_shipping_time` datetime DEFAULT NULL COMMENT '配送时间',
  `order_shipping_express_id` int DEFAULT '0' COMMENT '配送公司ID',
  `order_shipping_code` varchar(1000) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '物流单号',
  `order_shipping_message` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '卖家备注',
  `order_finished_time` datetime DEFAULT NULL COMMENT '订单完成时间',
  `order_goods_amount` decimal(10,2) NOT NULL COMMENT '商品总价格(不包含运费)',
  `order_payment_amount` decimal(10,2) NOT NULL COMMENT '应付金额（商品实际支付金额 + 运费）',
  `order_discount_fee` decimal(10,2) DEFAULT NULL COMMENT '优惠价格（商品金额-支付金额）',
  `order_adjust_fee` decimal(10,2) DEFAULT NULL COMMENT '手工调整金额',
  `order_shipping_fee` decimal(10,2) DEFAULT '0.00' COMMENT '运费价格',
  `order_buyer_evaluation_status` tinyint(1) NOT NULL DEFAULT '0' COMMENT '买家评价状态 0-未评价 1-已评价',
  `order_message` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT '' COMMENT '订单留言',
  `order_status` tinyint NOT NULL DEFAULT '0' COMMENT '订单状态 1.待付款  3.待发货 4.已发货  6.确认收货 7.取消 8.退款中 9.已退款 11.待自提\r\n12 虚拟订单已过期  16 部分发货',
  `order_discount_type` int DEFAULT '0' COMMENT '优惠方式：0.无；1.代金券；2.红包；3.店铺折扣；4.天九会员；5.红包代金券一起使用',
  `voucher_price` decimal(10,2) DEFAULT NULL COMMENT '代金券面额',
  `voucher_code` varchar(32) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '代金券编码',
  `order_refund_status` tinyint unsigned DEFAULT '0' COMMENT '退款状态:0是无退款,1是退款中,2是退款完成,3退款不通过',
  `order_return_status` tinyint unsigned DEFAULT '0' COMMENT '退货状态:0是无退货,1是退货中,2是退货完成，3退货不通过',
  `order_barter_status` tinyint(1) DEFAULT '0' COMMENT '换货状态：0：无换货 1：申请换货中 2：换货完成  3：商家拒绝换货',
  `order_refund_amount` decimal(10,2) DEFAULT NULL COMMENT '退款金额',
  `order_from` tinyint DEFAULT '1' COMMENT '订单来源 1PC 2h5 3小程序 4app',
  `order_commission_fee` decimal(10,2) DEFAULT NULL COMMENT '交易佣金',
  `order_commission_return_fee` decimal(10,2) DEFAULT NULL COMMENT '交易佣金退款',
  `order_shop_hidden` tinyint(1) DEFAULT '0' COMMENT '卖家删除',
  `order_cancel_identity` tinyint(1) DEFAULT '0' COMMENT '订单取消者身份   1-买家 2-卖家 3-系统',
  `order_cancel_reason` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_unicode_ci DEFAULT '' COMMENT '订单取消原因',
  `order_cancel_date` datetime DEFAULT NULL COMMENT '订单取消时间',
  `redpacket_code` varchar(32) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '红包编码',
  `redpacket_price` decimal(10,2) DEFAULT NULL COMMENT '红包面额',
  `order_rpt_price` decimal(10,2) DEFAULT NULL COMMENT '红包抵扣订单金额',
  `order_settlement_time` datetime DEFAULT NULL COMMENT '订单结算时间',
  `order_is_settlement` tinyint unsigned DEFAULT '0' COMMENT '订单是否结算 1-已结算 0-未结算',
  `district_id` mediumint DEFAULT '0' COMMENT '所在地,从店铺中同步，冗余检索使用',
  `payment_code` tinyint(1) DEFAULT '1' COMMENT '1.线上支付  2.线下支付 3.货到付款',
  `order_promotion_id` tinyint(1) DEFAULT '0' COMMENT '活动ID 0 普通点单 1 砍价（废弃） 2 秒杀 3 限时折扣 4拼团 5 满减增折',
  `groupbuy_status` int NOT NULL DEFAULT '0' COMMENT '北京光声_拼团状态1-拼团中 2-已成功 3未成功 0非拼团',
  `order_buy_id` varchar(50) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '北京光声_限时抢购id、拼团id、砍价id',
  `order_delay_status` tinyint(1) DEFAULT '0' COMMENT '0，未启用 1，启用',
  `order_settlement_id` varchar(250) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT '0' COMMENT '结算单id',
  `order_del` tinyint(1) NOT NULL DEFAULT '0' COMMENT '删除 0：不删除，1：删除',
  `ts` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '时间戳',
  `virtual_goods` int DEFAULT '1' COMMENT '虚拟订单 0 是 1 否',
  `period_of_validity` datetime DEFAULT NULL COMMENT '虚拟订单有效期',
  `past_due` int DEFAULT NULL COMMENT '过期退款 0 是 1 否',
  `redeem_code` varchar(32) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '虚拟商品兑换码',
  `redeem_code_url` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '虚拟商品兑换二维码url',
  `is_cancel` int unsigned DEFAULT '1' COMMENT '是否核销0是1否 2已过期',
  `cancel_date` datetime DEFAULT NULL COMMENT '核销时间',
  `integral_goods` int DEFAULT '1' COMMENT '积分商品订单 0是 1否',
  `total_integral` int DEFAULT '0' COMMENT '所需的积分',
  `order_is_shop_take` int DEFAULT NULL COMMENT '是否是门店自提订单 1是 2否',
  `vk_share_user_id` varchar(18) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT '0' COMMENT '微客申请分享人id',
  `dealer_commission` decimal(10,2) DEFAULT '0.00' COMMENT '待发货订单 经销商佣金',
  `dealer_return_commission` decimal(10,2) DEFAULT '0.00' COMMENT '经销商退还佣金',
  `supply_commission` decimal(10,2) DEFAULT '0.00' COMMENT '待发货订单供应商佣金',
  `supply_return_commission` decimal(10,2) DEFAULT '0.00' COMMENT '供应商退还佣金',
  `order_type` tinyint(1) DEFAULT '1' COMMENT '1普通订单   2商家批发订单(商家购买批发商商品订单)  3爆品绑定订单   4直播订单',
  `drop_shipping` tinyint(1) DEFAULT '2' COMMENT '用户订单是否供应商代发货 1是  2否',
  `order_last_return_time` datetime DEFAULT NULL COMMENT '订单最后售后时间',
  `order_supply_amount` decimal(10,2) DEFAULT NULL COMMENT '商品订单供货价',
  `order_company` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '单位',
  `pintuan_markId` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '拼团的 团id',
  `manjian_discount` decimal(10,2) DEFAULT NULL COMMENT '满减优惠金额',
  `manzhe_discount` decimal(10,2) DEFAULT NULL COMMENT '满折优惠金额',
  `giveaway_info` text CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci COMMENT '赠品信息',
  `base_member_discount` decimal(10,2) DEFAULT NULL COMMENT '会员折扣',
  `order_supply_is_settlement` int DEFAULT '0' COMMENT '是否 经销 结算',
  `order_supply_settlement_id` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '经销结算id',
  `order_supply_settlement_time` datetime DEFAULT NULL COMMENT '经销结算时间',
  `invoice_status` int DEFAULT '0' COMMENT '开票状态:0-未开票;1-审核中;2-已出票;3-未通过',
  `invoice_id` int DEFAULT NULL COMMENT '发票ID',
  `seckill_cancel_time` datetime DEFAULT NULL COMMENT '秒杀订单取消时间',
  `refund_application_time` datetime DEFAULT NULL COMMENT '用户申请退款时间',
  `refund_application_goods_time` datetime DEFAULT NULL COMMENT '用户申请退货时间',
  `return_method_type` int DEFAULT NULL COMMENT '1.退款,2.仅退款,3退货退款',
  `plus_discount` decimal(10,2) DEFAULT NULL COMMENT 'plus会员折扣',
  `plus_order` int DEFAULT '0' COMMENT '是否为plus订单 0 否 1 是',
  `distribution_discount` decimal(10,2) DEFAULT NULL COMMENT '经销折扣',
  `is_delivery` int DEFAULT '2' COMMENT '是否同城配送2-否 1-是',
  `delivery_chain_id` int DEFAULT NULL COMMENT '骑手-门店ID',
  `distributor_id` varchar(18) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL DEFAULT '0' COMMENT '骑手id',
  `distributor_evaluation` tinyint(1) DEFAULT '0' COMMENT '是否评价骑手0-否 1-是',
  `is_delivery_del` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否骑手删除0-否 1-是',
  `mobile` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '骑手电话',
  `pickup` tinyint(1) NOT NULL DEFAULT '0' COMMENT '0待取货 1已取货，待配送',
  `service_num` int DEFAULT '0' COMMENT '服务订单支付次数',
  `pickup_num` varchar(11) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '提货编号',
  `delivery_time` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '骑手接单时间',
  `end_free` decimal(10,2) DEFAULT '0.00' COMMENT '最终 配送费 （消费者）',
  `delivery_charge` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '配送费',
  `delivery_end_time` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '配送时间',
  `shop_affirm` tinyint DEFAULT '0' COMMENT '商家是否确认 0否 1是',
  `combination_id` int DEFAULT NULL COMMENT '组合商品id',
  `combination_type` int DEFAULT NULL COMMENT '组合类型',
  `combination_price` decimal(10,2) DEFAULT NULL COMMENT '组合价格',
  `is_gift_order` tinyint(1) DEFAULT '0' COMMENT '是否是礼包订单 0否 1是',
  `purchase_number` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '进货编号',
  `original_order` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '一件代发商品拆分前的原始订单号',
  `order_source` int DEFAULT '0' COMMENT '订单来源:0:自营订单,1:代发平台供应链订单,2:代发怡亚通供应链订单',
  `country_id` int DEFAULT '1' COMMENT '国家id',
  `country_name` varchar(100) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT '中国' COMMENT '国家名称',
  `international_dialing_code` varchar(50) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT '+86' COMMENT '国际区号',
  `currency_symbol` varchar(4) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT '￥' COMMENT '货币符号',
  `exchange_rate` decimal(10,4) DEFAULT '1.0000' COMMENT '汇率',
  `currency_code` varchar(10) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT 'CNY' COMMENT '货币代码',
  PRIMARY KEY (`order_id`) USING BTREE,
  UNIQUE KEY `order_id` (`order_id`) USING BTREE,
  KEY `shop_id` (`shop_id`) USING BTREE,
  KEY `buyer_user_id` (`buyer_user_id`) USING BTREE,
  KEY `seller_user_id` (`seller_user_id`) USING BTREE,
  KEY `order_status` (`order_status`) USING BTREE,
  KEY `order_finished_time` (`order_finished_time`) USING BTREE,
  KEY `buyer_user_name` (`buyer_user_name`) USING BTREE,
  KEY `shop_name` (`shop_name`) USING BTREE,
  KEY `order_create_time` (`order_create_time`) USING BTREE,
  KEY `payment_number` (`payment_number`) USING BTREE,
  KEY `order_shipping_express_id` (`order_shipping_express_id`) USING BTREE,
  KEY `payment_time` (`payment_time`) USING BTREE,
  KEY `redpacket_code` (`redpacket_code`) USING BTREE,
  KEY `payment_other_number` (`payment_other_number`) USING BTREE,
  KEY `order_receiver_area_id` (`order_receiver_area_id`) USING BTREE,
  KEY `order_goods_amount` (`order_goods_amount`) USING BTREE,
  KEY `order_payment_amount` (`order_payment_amount`) USING BTREE,
  KEY `order_refund_status` (`order_refund_status`) USING BTREE,
  KEY `order_return_status` (`order_return_status`) USING BTREE,
  KEY `order_is_settlement` (`order_is_settlement`) USING BTREE,
  KEY `order_settlement_id` (`order_settlement_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 ROW_FORMAT=DYNAMIC COMMENT='订单详细信息';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_order_cancel_reason`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_order_cancel_reason` (
  `cancel_reason_id` int NOT NULL AUTO_INCREMENT,
  `cancel_reason_content` varchar(100) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT '' COMMENT '取消订单的原因',
  `cancel_identity` tinyint(1) DEFAULT '0' COMMENT '类型 1-退款 2-退货',
  PRIMARY KEY (`cancel_reason_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 ROW_FORMAT=DYNAMIC COMMENT='订单取消原因表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_order_commission`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_order_commission` (
  `order_commission_id` int NOT NULL AUTO_INCREMENT COMMENT '订单佣金id',
  `order_id` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '订单号',
  `goods_id` int NOT NULL COMMENT '商品id',
  `goods_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '商品名',
  `goods_price` decimal(10,2) NOT NULL COMMENT '商品价格',
  `goods_num` int DEFAULT NULL COMMENT '购买数量',
  `goods_image` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '商品图片',
  `cat_id` int DEFAULT NULL COMMENT '分类id',
  `shop_id` int DEFAULT NULL COMMENT '商品所属店铺id',
  `share_user_id` varchar(18) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '分享用户id',
  `share_user_name` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '分享用户名',
  `share_user_mobile` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '分享用户手机号',
  `tg_one_user_id` varchar(18) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '一级推广员',
  `tg_one_user_name` varchar(18) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '一级推广员名称',
  `tg_one_user_mobile` varchar(18) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '一级推广员手机号',
  `tg_two_user_id` varchar(18) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '二级推广员',
  `tg_two_user_name` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '二级推广员名称',
  `tg_two_user_mobile` varchar(100) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '二级推广员手机号',
  `buy_user_id` varchar(18) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL COMMENT '购买用户id',
  `buy_user_name` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL COMMENT '购买用户名',
  `buy_user_mobile` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL COMMENT '购买用户手机号',
  `buy_user_avatar` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '购买用户头像',
  `buy_user_time` datetime DEFAULT NULL COMMENT '购买用户最后登录时间',
  `bind_user_id` varchar(18) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '购买人绑定的上级id',
  `order_status` tinyint(1) NOT NULL DEFAULT '1' COMMENT '状态 1未入账  3已入账  4已退款 5已取消 6已冻结',
  `order_price` decimal(10,2) NOT NULL COMMENT '订单金额',
  `return_price` decimal(10,2) NOT NULL DEFAULT '0.00' COMMENT '退款金额',
  `one_tg_rate` varchar(10) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL DEFAULT '0' COMMENT '一级推广佣金比例',
  `two_tg_rate` varchar(10) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL DEFAULT '0' COMMENT '二级推广佣金比例',
  `fx_rate` varchar(10) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL DEFAULT '0' COMMENT '分享佣金比例',
  `one_tg_commission` decimal(10,2) NOT NULL DEFAULT '0.00' COMMENT '一级推广佣金',
  `two_tg_commission` decimal(10,2) NOT NULL COMMENT '二级推广佣金',
  `fx_commission` decimal(10,2) NOT NULL COMMENT '分享佣金',
  `return_one_tg_commission` decimal(10,2) NOT NULL DEFAULT '0.00' COMMENT '退还一级推广佣金',
  `return_two_tg_commission` decimal(10,2) NOT NULL DEFAULT '0.00' COMMENT '退还二级推广佣金',
  `return_fx_commission` decimal(10,2) NOT NULL DEFAULT '0.00' COMMENT '退还分享佣金',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '下单时间',
  `pay_time` datetime DEFAULT NULL COMMENT '支付时间',
  `return_time` datetime DEFAULT NULL COMMENT '退款时间',
  `commission_type` tinyint(1) NOT NULL DEFAULT '1' COMMENT '1会员微客佣金   2员工微客佣金',
  `bind_type` tinyint(1) NOT NULL DEFAULT '1' COMMENT '购买用户是否分享用户下级  1是  2否',
  `settle_record_id` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '结算交易id',
  `settle_state` tinyint(1) DEFAULT '0' COMMENT '平台给商家结算订单状态 0未结算  1已结算',
  `settle_time` datetime DEFAULT NULL COMMENT '结算时间',
  `fx_settlement_status` tinyint(1) DEFAULT '0' COMMENT '分销结算给用户状态 0未结算 1已结算',
  `fx_settlement_time` datetime DEFAULT NULL COMMENT '分销结算给用户时间',
  `fx_tg` int DEFAULT '1' COMMENT '分享 1 推广 2 ',
  `vk_shop_id` int DEFAULT NULL COMMENT '分销小店id',
  PRIMARY KEY (`order_commission_id`) USING BTREE,
  KEY `share_user_id_2` (`share_user_id`) USING BTREE,
  KEY `buy_user_id` (`buy_user_id`) USING BTREE,
  KEY `shop_id` (`shop_id`) USING BTREE,
  KEY `share_user_id` (`buy_user_id`,`share_user_id`) USING BTREE,
  KEY `create_time` (`create_time`) USING BTREE,
  KEY `return_time` (`return_time`) USING BTREE,
  KEY `pay_time` (`pay_time`) USING BTREE,
  KEY `order_id` (`order_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 ROW_FORMAT=DYNAMIC COMMENT='佣金订单';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_order_complaints`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_order_complaints` (
  `complaints_id` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '投诉单号',
  `id` int NOT NULL AUTO_INCREMENT,
  `complaints_type` int DEFAULT NULL COMMENT '投诉类型 1 未按时发货 2 虚假发货 3 违规出售商品 4 骚扰辱骂  5 售后退款  ',
  `complaints_reason` varchar(500) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '原因描叙',
  `complaints_voucher` text CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci COMMENT '投诉凭证',
  `complaints_contact` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '联系方式',
  `complaints_goodsid` varchar(500) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '投诉商品id',
  `complaints_status` int DEFAULT '0' COMMENT '投诉状态 0 待卖家处理 1 投诉成立 2 投诉不成立 3 投诉撤销  4 待平台处理',
  `compensation_amount` decimal(10,2) DEFAULT NULL COMMENT '赔付金额',
  `compensation_status` int DEFAULT '0' COMMENT '是否赔付 0 未赔付 1 赔付',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `revoke_num` int DEFAULT '0' COMMENT '撤销次数',
  `order_id` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '订单id',
  `user_id` varchar(100) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '买家id',
  `failure_time` datetime DEFAULT NULL COMMENT '处理时效时间',
  `seller_voucher` text CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci COMMENT '卖家凭证',
  `seller_reason` text CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci COMMENT '卖家说明',
  `shop_id` int DEFAULT NULL COMMENT '店铺id',
  `fund_destination` int DEFAULT NULL COMMENT '罚款去向 1  账户余额 2平台账户',
  `is_platform` int DEFAULT '0' COMMENT '是否为平台罚扣 0 否 1 是',
  `phone_number` varchar(50) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '消费者手机号',
  `is_appeal` int DEFAULT '0' COMMENT '是否申诉  0 未申诉 1 申诉',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 ROW_FORMAT=DYNAMIC COMMENT='用户投诉表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_order_dealer_info`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_order_dealer_info` (
  `id` int NOT NULL AUTO_INCREMENT,
  `order_id` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '订单号',
  `goods_id` int DEFAULT NULL COMMENT '商家skuid',
  `supplier_goods_id` int DEFAULT NULL COMMENT '供应商skuid',
  `tax_inclusive_amount` decimal(10,2) DEFAULT NULL COMMENT '含税金额',
  `total_tax_inclusive_amount` decimal(10,2) DEFAULT NULL COMMENT '含税金额  *  下单数量',
  `dealer_discount` decimal(10,2) DEFAULT NULL COMMENT '经销折扣',
  `dealer_discount_amout` decimal(10,2) DEFAULT NULL COMMENT '经销折扣金额',
  `original_order` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '一件代发商品拆分前的原始订单号',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci ROW_FORMAT=DYNAMIC COMMENT='经销单 经销折扣信息';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_order_goods`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_order_goods` (
  `order_goods_id` int NOT NULL AUTO_INCREMENT COMMENT 'id',
  `order_id` varchar(50) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL COMMENT '订单id',
  `shop_id` int NOT NULL DEFAULT '0' COMMENT '店铺ID',
  `shop_name` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL COMMENT '店铺名',
  `deliver_shop_id` int DEFAULT NULL COMMENT '发货店铺id',
  `deliver_shop_name` varchar(50) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '发货店铺',
  `drop_shipping` tinyint(1) DEFAULT '2' COMMENT '用户订单是否供应商代发货 1是  2否',
  `goods_id` int NOT NULL COMMENT '商品id',
  `common_id` int NOT NULL DEFAULT '0' COMMENT '商品common_id',
  `buyer_user_id` varchar(50) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL DEFAULT '0' COMMENT '买家id',
  `goods_name` varchar(100) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL COMMENT '商品名称',
  `goods_class_id` int DEFAULT NULL COMMENT '商品对应的类目ID',
  `order_spec_chineseValue` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL COMMENT '中文规格值',
  `goods_price` decimal(10,2) DEFAULT NULL COMMENT '商品价格（商品原价，goods_base中的商品价格未参加任何活动的价格）',
  `order_goods_num` smallint unsigned NOT NULL DEFAULT '1' COMMENT '商品数量',
  `goods_image` varchar(500) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL COMMENT '商品图片',
  `order_goods_returnnum` smallint unsigned DEFAULT '0' COMMENT '退货数量',
  `order_goods_amount` decimal(10,2) NOT NULL COMMENT '商品金额 （实付金额）= order_goods_payment_amount* order_goods_num',
  `order_goods_platform_bear` decimal(10,2) DEFAULT NULL COMMENT '优惠活动中平台承担的金额',
  `order_goods_discount_fee` decimal(10,2) DEFAULT NULL COMMENT '优惠金额 = （商品原价-实付金额）*商品数量(废弃)',
  `order_goods_payment_amount` decimal(20,2) unsigned NOT NULL COMMENT '实付金额',
  `order_goods_adjust_fee` decimal(10,2) DEFAULT NULL COMMENT '手工调整金额',
  `order_goods_commission` decimal(10,2) DEFAULT NULL COMMENT '订单商品的佣金 (总)(废弃)',
  `order_goods_status` tinyint(1) NOT NULL COMMENT '订单状态',
  `goods_return_price` decimal(10,2) DEFAULT NULL COMMENT '订单商品退款金额',
  `order_goods_evaluation_status` tinyint(1) NOT NULL COMMENT '评价状态 0为评价，1已评价',
  `order_rpt_price` decimal(10,2) NOT NULL COMMENT '红包抵扣金额',
  `voucher_price` decimal(10,2) NOT NULL COMMENT '代金券抵扣金额',
  `goods_return_status` tinyint(1) NOT NULL COMMENT '退货状态:0是无退货,1是退货中,2是退货完成 3商家拒绝退货',
  `goods_refund_status` tinyint(1) NOT NULL DEFAULT '0' COMMENT '退款状态：0：无退款 1：退款中 2：退款完成  3：商家拒绝退款',
  `goods_barter_status` tinyint(1) DEFAULT '0' COMMENT '换货状态：0：无换货 1：申请换货中 2：换货完成  3：商家拒绝换货',
  `order_goods_time` datetime NOT NULL COMMENT '创建时间',
  `order_goods_finish_time` datetime DEFAULT NULL COMMENT '订单商品完成时间',
  `order_goods_id_deliver` tinyint(1) DEFAULT '0' COMMENT '商品订单是否已经发货。1-是，0-否，默认0',
  `order_seller_name` varchar(50) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '发货人的姓名',
  `order_seller_address` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '发货人的地址',
  `order_seller_contact` varchar(50) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '发货人的联系方式',
  `order_shipping_time` datetime DEFAULT NULL COMMENT '配送时间',
  `order_shipping_express_id` int DEFAULT '0' COMMENT '配送公司ID',
  `order_shipping_code` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '物流单号',
  `shipping_fee` decimal(10,2) NOT NULL COMMENT '商品运费',
  `order_shipping_message` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '卖家备注',
  `promotion_id` tinyint(1) DEFAULT '0' COMMENT '活动ID 0 普通点单 1 砍价（废弃） 2 秒杀 3 限时折扣 4拼团 5 满减赠 6 满减折',
  `goods_promotion_id` int DEFAULT '0' COMMENT '活动ID',
  `order_del` tinyint(1) NOT NULL DEFAULT '0' COMMENT '删除 0：不删除，1：删除',
  `ts` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '时间戳',
  `integral` int DEFAULT NULL COMMENT '商品购买所需积分',
  `dealer_commission` decimal(10,2) DEFAULT '0.00' COMMENT '代发货订单 经销商佣金',
  `dealer_return_commission` decimal(10,2) DEFAULT '0.00' COMMENT '经销商退还佣金',
  `supply_commission` decimal(10,2) DEFAULT '0.00' COMMENT '代发货订单供应商佣金',
  `supply_return_commission` decimal(10,2) DEFAULT '0.00' COMMENT '供应商退还佣金',
  `goods_member_discount` decimal(10,2) DEFAULT NULL COMMENT '会员折扣',
  `business_refund_time` datetime DEFAULT NULL COMMENT '商家拒绝退款时间',
  `total_goods_shipping_fee` decimal(10,2) DEFAULT NULL COMMENT '商品总邮费',
  `goods_plus_discount` decimal(10,2) DEFAULT NULL COMMENT 'plus会员折扣金额',
  `goods_plus_dis` int DEFAULT NULL COMMENT '是否为plus商品  0 不是  1是',
  `full_reduction_discount` decimal(10,2) DEFAULT NULL COMMENT '满减活动折扣金额',
  `full_folding_discount` decimal(10,2) DEFAULT NULL COMMENT '满折活动折扣金额',
  `order_goods_adjust_before_price` decimal(10,2) DEFAULT NULL COMMENT '改价之前价格',
  `${database.prefix.activity}hou_activity_unit_price` decimal(10,2) DEFAULT NULL COMMENT '活动单价',
  `voucher_id` int DEFAULT NULL COMMENT '商品优惠券id',
  `shipment_quantity` int DEFAULT '0' COMMENT '发货数量',
  `shipment_status` tinyint(1) DEFAULT '0' COMMENT '发货状态 0待发货 1部分发货 2发货完成',
  `ver_id` int DEFAULT NULL COMMENT '商品版本id',
  `barter_goods_id` int DEFAULT NULL COMMENT '换货商品id',
  `order_barter_id` int DEFAULT NULL COMMENT '换货物流id',
  PRIMARY KEY (`order_goods_id`) USING BTREE,
  UNIQUE KEY `order_goods_id` (`order_goods_id`) USING BTREE,
  KEY `order_id` (`order_id`) USING BTREE COMMENT '(null)',
  KEY `buyer_user_id` (`buyer_user_id`) USING BTREE,
  KEY `order_goods_status` (`order_goods_status`) USING BTREE,
  KEY `order_goods_time` (`order_goods_time`) USING BTREE,
  KEY `goods_id` (`goods_id`) USING BTREE,
  KEY `shop_id` (`shop_id`) USING BTREE,
  KEY `order_goods_id_deliver` (`order_goods_id_deliver`) USING BTREE,
  KEY `order_shipping_express_id` (`order_shipping_express_id`) USING BTREE,
  KEY `common_id` (`common_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 ROW_FORMAT=DYNAMIC COMMENT='订单商品表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_order_goods_evaluation`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_order_goods_evaluation` (
  `evaluation_goods_id` int NOT NULL AUTO_INCREMENT COMMENT '商品评论id',
  `user_id` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL COMMENT '会员ID',
  `user_name` varchar(50) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL COMMENT '会员名',
  `user_avatar` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL COMMENT '会员头像',
  `order_id` varchar(50) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL COMMENT '订单ID',
  `shop_id` int NOT NULL COMMENT '商家ID',
  `shop_name` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '店铺名称',
  `common_id` int NOT NULL COMMENT '商品ID',
  `goods_id` int NOT NULL COMMENT '规格Id',
  `goods_name` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '商品名',
  `goods_spec` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '商品规格',
  `scores` tinyint(1) DEFAULT '5' COMMENT '1-5分',
  `result` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '结果',
  `content` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '内容',
  `additional_content_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '追评回复时间',
  `image` text CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci COMMENT '晒单图片',
  `isanonymous` tinyint(1) DEFAULT '1' COMMENT '是否匿名评价 0 不匿名 1  匿名',
  `create_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `status` tinyint(1) DEFAULT '1' COMMENT '状态 0禁止显示 1显示 2置顶',
  `explain_content` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '解释内容',
  `update_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '评价时间',
  `additional_reply_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '商家回复追评时间',
  `evaluation_from` tinyint DEFAULT NULL COMMENT ' 1 ios  2 pc 3 wap 4 android 5默认好评',
  PRIMARY KEY (`evaluation_goods_id`) USING BTREE,
  KEY `order_id` (`order_id`) USING BTREE,
  KEY `shop_name` (`shop_name`) USING BTREE,
  KEY `update_time` (`update_time`) USING BTREE,
  KEY `common_id` (`common_id`) USING BTREE,
  KEY `user_id` (`user_id`) USING BTREE,
  KEY `goods_name` (`goods_name`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 ROW_FORMAT=DYNAMIC COMMENT='商品评论表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_order_invoice_address`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_order_invoice_address` (
  `address_id` int NOT NULL AUTO_INCREMENT,
  `user_id` varchar(50) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL COMMENT '收票人ID',
  `user_name` varchar(50) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '收票人姓名',
  `user_mobile` varchar(50) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '收票人手机号',
  `user_mail` varchar(100) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '收票人邮箱',
  `address_province` int DEFAULT NULL COMMENT '收票地址-省',
  `address_city` int DEFAULT NULL COMMENT '收票地址-市',
  `address_area` int DEFAULT NULL COMMENT '收票地址-区',
  `address_detail` varchar(200) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '详细地址',
  `is_default` int DEFAULT '0' COMMENT '是否默认:1-是；0-否',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `last_update_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '上次更新时间',
  PRIMARY KEY (`address_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 ROW_FORMAT=DYNAMIC COMMENT='发票地址管理表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_order_invoice_info`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_order_invoice_info` (
  `invoice_id` int NOT NULL AUTO_INCREMENT COMMENT '发票ID',
  `order_id` varchar(100) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL COMMENT '订单ID',
  `invoice_content` int DEFAULT '0' COMMENT '发票内容:0-商品明细;1-商品类别',
  `audit_comment` varchar(500) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '商家审核备注',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '申请时间',
  `finish_time` datetime DEFAULT NULL COMMENT '开票时间',
  `invoice_amount` decimal(10,2) NOT NULL DEFAULT '0.00' COMMENT '发票金额',
  `invoice_status` int NOT NULL DEFAULT '0' COMMENT '开票状态:0-未开票;1-审核中;2-已出票;3-未通过',
  `last_update_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '上次更新时间',
  `user_id` varchar(18) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL COMMENT '收票人ID',
  `user_name` varchar(50) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '收票人姓名',
  `user_mobile` varchar(50) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '收票人手机号',
  `user_mail` varchar(100) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '收票人邮箱',
  `address_province` int DEFAULT NULL COMMENT '收票地址-省',
  `address_city` int DEFAULT NULL COMMENT '收票地址-市',
  `address_area` int DEFAULT NULL COMMENT '收票地址-区',
  `address_detail` varchar(200) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '详细地址',
  `invoice_type` int NOT NULL DEFAULT '0' COMMENT '发票类型: 0-普票；1-增值税专用票',
  `invoice_head_type` int NOT NULL DEFAULT '1' COMMENT '抬头类型:1-个人；2-单位',
  `invoice_head` varchar(200) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL COMMENT '发票抬头',
  `company_tax_id` varchar(100) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '单位税号',
  `register_address` varchar(500) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '注册地址',
  `register_number` varchar(100) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '注册电话',
  `opening_bank` varchar(200) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '开户银行',
  `bank_account` varchar(200) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '银行账号',
  `invoice_pic` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '图片',
  PRIMARY KEY (`invoice_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 ROW_FORMAT=DYNAMIC COMMENT='订单发票表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_order_invoice_template_info`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_order_invoice_template_info` (
  `invoice_tmp_id` int NOT NULL AUTO_INCREMENT,
  `user_id` varchar(50) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL COMMENT '用户ID',
  `invoice_type` int NOT NULL DEFAULT '0' COMMENT '发票类型: 0-普票；1-增值税专用票',
  `invoice_head_type` int NOT NULL DEFAULT '1' COMMENT '抬头类型:1-个人；2-单位',
  `invoice_head` varchar(200) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL COMMENT '发票抬头',
  `company_tax_id` varchar(100) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '单位税号',
  `register_address` varchar(500) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '注册地址',
  `register_number` varchar(100) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '注册电话',
  `opening_bank` varchar(200) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '开户银行',
  `bank_account` varchar(200) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '银行账号',
  `is_default` int DEFAULT '0' COMMENT '是否默认：1-是；0-否',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `last_update_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '上次更新时间',
  `invoice_content` int DEFAULT '0' COMMENT '发票内容:0-商品明细;1-商品类别',
  PRIMARY KEY (`invoice_tmp_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 ROW_FORMAT=DYNAMIC COMMENT='发票模版管理表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_order_payment_vocher`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_order_payment_vocher` (
  `id` int NOT NULL AUTO_INCREMENT,
  `order_id` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '关联ordere id',
  `order_payment_voucher_imgs` text CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci COMMENT '支付凭证url 图片路径',
  `review_status` tinyint DEFAULT NULL COMMENT '审核状态  0 待上传   1 待审核 2 已审核 3 审核驳回',
  `review_message` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '审核信息',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `review_time` datetime DEFAULT NULL COMMENT '审核时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 ROW_FORMAT=DYNAMIC COMMENT='支付凭证关联表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_order_return`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_order_return` (
  `order_return_id` int unsigned NOT NULL AUTO_INCREMENT COMMENT '退货记录ID',
  `order_id` varchar(50) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '订单编号',
  `order_amount` decimal(8,2) DEFAULT NULL COMMENT '订单总额',
  `order_goods_num` int DEFAULT NULL COMMENT '退货数量',
  `return_code` varchar(100) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '退货编号',
  `return_type` tinyint DEFAULT '1' COMMENT '1-退款申请 2-退货申请 3-换货申请',
  `shop_id` int unsigned DEFAULT NULL COMMENT '卖家ID(shopId)',
  `shop_name` varchar(50) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '店铺名称',
  `seller_user_id` varchar(18) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '卖家Id',
  `buyer_user_id` varchar(18) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '买家ID',
  `buyer_user_account` varchar(50) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '买家会员名',
  `return_add_time` datetime DEFAULT NULL COMMENT '添加时间',
  `return_reason_id` int DEFAULT NULL COMMENT '退款理由id',
  `return_reason` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '退款理由',
  `return_message` varchar(300) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '退货备注',
  `return_real_name` varchar(30) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '收货人',
  `return_addr_id` int DEFAULT NULL COMMENT '收货地址id(商家地址库id)',
  `return_addr_name` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '收货地址',
  `return_addr` varchar(150) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '收货地址详情',
  `return_shipping_code` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '单号',
  `return_post_code` int DEFAULT NULL COMMENT '物流id',
  `return_tel` varchar(20) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '联系电话',
  `return_mobile` varchar(20) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '联系手机',
  `return_state` tinyint DEFAULT '1' COMMENT '0-商家拒绝 1-新发起等待卖家审核 2-卖家审核通过 3-卖家审核不通过 4-卖家收到货物 5-平台审核通过 6-买家已填物流 7-平台拒绝 8-退款关闭 9-平台介入 10验收不通过 11待买家退货 12待验收 13退货关闭 15用户撤销退款关闭 16用户撤销退货关闭 18 卖家发出换货物流  19 买家确认收货 20用户撤销换货关闭 21 部分发出换货物流  22 买家部分确认收货 23 换货关闭',
  `return_cash` decimal(8,2) DEFAULT NULL COMMENT '退款金额',
  `return_shop_time` datetime DEFAULT NULL COMMENT '商家处理时间',
  `return_shop_message` varchar(300) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '商家备注',
  `return_finish_time` datetime DEFAULT NULL COMMENT '退款完成时间',
  `return_commision_fee` decimal(8,2) DEFAULT NULL COMMENT '退还佣金(暂时废弃)',
  `behalf_deliver` tinyint(1) DEFAULT '0' COMMENT '分销代发货  0：不代发货 1：代发货(分销订单DD) 2：代发货（供应订单SP）(暂时废弃)',
  `return_rpt_cash` decimal(10,2) DEFAULT NULL COMMENT '退还平台红包金额',
  `return_shop_handle` tinyint(1) DEFAULT NULL COMMENT '商家处理结果  1-待处理  2-卖家审核通过 3-卖家审核不通过(暂废弃，状态同return_state)',
  `order_is_settlement` tinyint(1) DEFAULT '0' COMMENT '是否结算',
  `order_settlement_time` datetime DEFAULT NULL COMMENT '结算时间',
  `order_settlement_id` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '结算单号',
  `order_operator` varchar(32) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '审核操作人',
  `order_operator_time` datetime DEFAULT NULL COMMENT '操作时间',
  `ts` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '时间戳',
  `order_supply_is_settlement` tinyint(1) DEFAULT '0' COMMENT '供应商是否结算',
  `order_supply_settlement_time` datetime DEFAULT NULL COMMENT '供应商结算时间',
  `order_supply_settlement_id` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '供应商结算id',
  `return_member_discount` decimal(10,2) DEFAULT NULL COMMENT '退还会员折扣',
  `return_supper_discount_fee` decimal(10,2) DEFAULT NULL COMMENT '退还经销等级折扣',
  `return_shipping_fee` decimal(10,2) DEFAULT NULL COMMENT '退还运费 订单全退退运费 半退不退',
  `return_method_status` int DEFAULT NULL COMMENT '1.退款 2.仅退款 3退货退款 4换货',
  `fill_logistics_time` datetime DEFAULT NULL COMMENT '填写物流时间',
  `close_time` datetime DEFAULT NULL COMMENT '关闭时间',
  `return_change_num` int DEFAULT '0' COMMENT '用户修改次数',
  `expiration_time` datetime DEFAULT NULL COMMENT '失效时间',
  `revoke_num` int DEFAULT '0' COMMENT '撤销次数',
  `return_voucher` text CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci COMMENT '退货凭证',
  `call_status` int DEFAULT '0' COMMENT '0 不可唤起 1 可以唤起',
  `resubmissions_number` int DEFAULT '0' COMMENT '重新提交次数',
  `historical_return_status` varchar(50) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '历史状态',
  `return_number` varchar(20) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '收货人联系电话',
  `storage_id` int DEFAULT NULL COMMENT '仓库id',
  `return_payment_voucher` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '平台退款凭证',
  `is_send_back` tinyint(1) DEFAULT '2' COMMENT '是否寄回供应商 1已寄回 2未寄回',
  `send_back_notes` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '寄回备注',
  PRIMARY KEY (`order_return_id`) USING BTREE,
  UNIQUE KEY `return_code` (`return_code`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 ROW_FORMAT=DYNAMIC COMMENT='退货表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_order_return_platform`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_order_return_platform` (
  `platform_intervention_id` int NOT NULL AUTO_INCREMENT,
  `order_id` varchar(50) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL COMMENT '订单编号',
  `return_code` varchar(50) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL COMMENT '售后单号',
  `shop_name` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL COMMENT '店铺名',
  `shop_id` int NOT NULL COMMENT '店铺id',
  `intervention_state` int NOT NULL DEFAULT '0' COMMENT '平台介入状态 0:待卖家上传凭证  1:待平台审核 2 已处理  3 买家撤销',
  `application_time` datetime NOT NULL COMMENT '申请时间',
  `buyer_user_id` varchar(50) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL COMMENT '买家id',
  `seller_user_id` varchar(50) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL COMMENT '卖家id(店铺id)',
  `return_state` int DEFAULT NULL COMMENT '售后状态  卖家审核不通过',
  `seller_voucher` text CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci COMMENT '卖家凭证',
  `buyer_voucher` text CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci COMMENT '买家凭证',
  `buyer_remarks` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '买家备注说明',
  `seller_remarks` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '卖家备注说明',
  `processing_results` int DEFAULT NULL COMMENT '平台处理结果 1 买家胜利  0卖家胜利 2 用户撤销',
  `processing_time` datetime DEFAULT NULL COMMENT '平台处理时间',
  `expriation_time` datetime DEFAULT NULL COMMENT '失效时间',
  `return_method_type` int DEFAULT NULL COMMENT '1.退款,2.仅退款,3退货退款',
  PRIMARY KEY (`platform_intervention_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 ROW_FORMAT=DYNAMIC COMMENT='平台介入表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_order_return_reason_yiyatong`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_order_return_reason_yiyatong` (
  `order_return_reason_id` int NOT NULL AUTO_INCREMENT,
  `order_return_reason_content` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL COMMENT '投诉理由内容',
  `order_return_reason_sort` int NOT NULL DEFAULT '225' COMMENT '投诉理由排序',
  `order_return_reason_type` int NOT NULL COMMENT '投诉类型1-退款,2-退货退款,3-换货,4-仅退款,5-部分金额退款',
  PRIMARY KEY (`order_return_reason_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 ROW_FORMAT=DYNAMIC COMMENT='退回原因 -- 字典表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_order_settlement`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_order_settlement` (
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
  `shop_id` int unsigned NOT NULL COMMENT '店铺ID',
  `shop_name` varchar(50) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL DEFAULT '' COMMENT '店铺名',
  `os_order_type` tinyint(1) NOT NULL COMMENT '结算订单类型 1-普通订单 2-商家一键上架供应商销售订单商家结算单  3商家一键上架供应商销售订单供应商结算单 4直播分佣结算 5webpos结算单',
  `os_redpacket_amount` decimal(10,2) NOT NULL DEFAULT '0.00' COMMENT '红包金额',
  `os_redpacket_return_amount` decimal(10,2) NOT NULL DEFAULT '0.00' COMMENT '退还红包',
  `district_id` mediumint NOT NULL DEFAULT '0' COMMENT '地区id,0表示全国',
  `os_is_display` tinyint(1) unsigned zerofill DEFAULT '1' COMMENT '结算单是否显示 0不显示 1显示',
  `os_base_member_discount` decimal(10,2) DEFAULT '0.00' COMMENT '会员折扣',
  `os_return_member_discount` decimal(10,2) DEFAULT '0.00' COMMENT '退还会员折扣',
  `os_tg_commission` decimal(10,2) NOT NULL DEFAULT '0.00' COMMENT '推广佣金',
  `os_fx_commission` decimal(10,2) NOT NULL DEFAULT '0.00' COMMENT '分享佣金',
  `os_supper_discount_fee` decimal(10,2) DEFAULT NULL COMMENT '供应商经销等级 折扣',
  `os_return_supper_discount_fee` decimal(10,2) DEFAULT NULL COMMENT '供应商退还经销等级折扣',
  `os_supply_price` decimal(10,2) DEFAULT NULL COMMENT '供货价',
  `os_return_supply_price` decimal(10,2) DEFAULT NULL COMMENT '退还供货价',
  `os_return_tg_commission` decimal(10,2) NOT NULL COMMENT '退还推广佣金',
  `os_return_fx_commission` decimal(10,2) NOT NULL COMMENT '退还分享佣金',
  `self_selling_commission` decimal(10,2) DEFAULT '0.00' COMMENT '自销商品佣金',
  `self_selling_shipping_fee` decimal(10,2) DEFAULT '0.00' COMMENT '自销商品运费',
  `self_selling_return_cash` decimal(10,2) DEFAULT '0.00' COMMENT '自销退单金额',
  `self_selling_return_commission` decimal(10,2) DEFAULT '0.00' COMMENT '自销退还佣金',
  `sell_on_amount` decimal(10,2) DEFAULT '0.00' COMMENT '经销商品金额',
  `sell_on_commission` decimal(10,2) DEFAULT '0.00' COMMENT '经销商品佣金',
  `sell_on_return_cash` decimal(10,2) DEFAULT '0.00' COMMENT '经销商品佣金',
  `sell_on_return_commission` decimal(10,2) DEFAULT '0.00' COMMENT '经销退单佣金',
  PRIMARY KEY (`os_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 ROW_FORMAT=DYNAMIC COMMENT='订单结算表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_order_shipment`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_order_shipment` (
  `shipment_id` int NOT NULL AUTO_INCREMENT COMMENT '发货id',
  `order_id` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '订单id',
  `goods_ids` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '配送商品',
  `goods_nums` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '配送数量',
  `order_shipping_time` datetime DEFAULT NULL COMMENT '配送时间',
  `order_shipping_express_id` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '0' COMMENT '配送公司ID',
  `order_shipping_code` varchar(1000) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '物流单号',
  `order_shipping_message` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '卖家备注',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `shipment_type` tinyint DEFAULT '1' COMMENT '1 物流配送 2 自主配送',
  `delivery_person` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '配送人',
  `delivery_telephone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '配送联系方式',
  `delivery_certificate` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci COMMENT '配送凭证',
  `storage_id` int DEFAULT NULL COMMENT '仓库id',
  `self_selling_commission` decimal(10,2) DEFAULT '0.00' COMMENT '自销商品佣金',
  `self_selling_shipping_fee` decimal(10,2) DEFAULT '0.00' COMMENT '自销商品运费',
  `self_selling_return_cash` decimal(10,2) DEFAULT '0.00' COMMENT '自销退单金额',
  `self_selling_return_commission` decimal(10,2) DEFAULT '0.00' COMMENT '自销退还佣金',
  `sell_on_amount` decimal(10,2) DEFAULT '0.00' COMMENT '经销商品金额',
  `sell_on_commission` decimal(10,2) DEFAULT '0.00' COMMENT '经销商品佣金',
  `sell_on_return_cash` decimal(10,2) DEFAULT '0.00' COMMENT '经销商品退单金额',
  `sell_on_return_commission` decimal(10,2) DEFAULT '0.00' COMMENT '经销退单佣金',
  `sender_address` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '发货地址（针对物流、快递）',
  PRIMARY KEY (`shipment_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci ROW_FORMAT=DYNAMIC COMMENT='订单发货表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_order_supplier_base`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_order_supplier_base` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT 'id',
  `order_id` varchar(100) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL COMMENT '订单号',
  `supplier_shop_id` int DEFAULT NULL COMMENT '供应商店铺id',
  `goods_id` int DEFAULT NULL COMMENT '商品id',
  `common_id` int DEFAULT NULL COMMENT '商品id',
  `goods_price` decimal(10,2) DEFAULT NULL COMMENT '供应商 的商品价格',
  `goods_num` int DEFAULT NULL COMMENT '购买数量',
  `goods_price_sum` decimal(10,2) DEFAULT NULL COMMENT '订单价 商品价格乘购买数量 减运费 ',
  `goods_payment_amount` decimal(10,2) DEFAULT NULL COMMENT '供应商实付金额 含运费',
  `order_refund_price` decimal(10,2) DEFAULT NULL COMMENT '退款金额',
  `order_return_price` decimal(10,2) DEFAULT NULL COMMENT '退货金额',
  `order_return_discount` decimal(10,2) DEFAULT NULL COMMENT '退还的经销折扣 全退 退经销折扣 半退不退',
  `supper_commission` decimal(10,2) DEFAULT NULL COMMENT '平台抽供应商佣金',
  `supper_return_commission` decimal(10,2) DEFAULT NULL COMMENT '平台抽供应商退还佣金',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_order_supplier_user_cart`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_order_supplier_user_cart` (
  `cart_id` int NOT NULL AUTO_INCREMENT COMMENT 'id',
  `user_id` varbinary(18) NOT NULL DEFAULT '0' COMMENT '买家id',
  `shop_id` int NOT NULL COMMENT '店铺id',
  `shop_name` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL COMMENT '店铺名称',
  `common_id` int NOT NULL COMMENT '商品Id',
  `goods_id` int unsigned NOT NULL DEFAULT '0' COMMENT '规格id',
  `goods_name` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL COMMENT '商品名称',
  `goods_image` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL COMMENT '商品图片',
  `goods_num` int NOT NULL DEFAULT '1' COMMENT '数量',
  `goods_price` decimal(10,2) NOT NULL COMMENT '商品价格',
  `goods_stock` int NOT NULL COMMENT '库存',
  `goods_type` tinyint(1) NOT NULL DEFAULT '2' COMMENT '1普通商品  2供应商商品',
  `goods_spec_chinese_value` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL COMMENT '规格',
  `goods_moq` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '商品起订量',
  `insert_cart_time` datetime NOT NULL COMMENT '插入购物车时间',
  `cart_status` tinyint(1) NOT NULL DEFAULT '0' COMMENT '0 正常 1 删除 2 店铺关闭商品下架',
  `shop_gate_id` int DEFAULT NULL COMMENT '门店id',
  PRIMARY KEY (`cart_id`) USING BTREE,
  KEY `user_id` (`user_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 ROW_FORMAT=DYNAMIC COMMENT='供应商采购表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_order_user_cart`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_order_user_cart` (
  `cart_id` int NOT NULL AUTO_INCREMENT COMMENT 'id',
  `user_id` varbinary(18) NOT NULL DEFAULT '0' COMMENT '买家id',
  `shop_id` int NOT NULL COMMENT '店铺id',
  `shop_name` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL COMMENT '店铺名称',
  `common_id` int NOT NULL COMMENT '商品Id',
  `goods_id` int unsigned NOT NULL DEFAULT '0' COMMENT '规格id',
  `goods_name` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL COMMENT '商品名称',
  `goods_image` varchar(500) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL COMMENT '商品图片',
  `goods_num` int NOT NULL DEFAULT '1' COMMENT '数量',
  `goods_price` decimal(10,2) NOT NULL COMMENT '商品价格',
  `goods_stock` int NOT NULL COMMENT '库存',
  `goods_type` tinyint(1) NOT NULL DEFAULT '1' COMMENT '1普通商品  2供应商商品',
  `goods_spec` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '规格(废弃)',
  `goods_spec_chinese_value` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL COMMENT '规格',
  `goods_moq` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '商品起订量',
  `insert_cart_time` datetime NOT NULL COMMENT '插入购物车时间',
  `cart_status` tinyint(1) NOT NULL DEFAULT '0' COMMENT '0 正常 1 删除 2 店铺关闭商品下架',
  `shop_gate_id` int DEFAULT NULL COMMENT '门店id',
  PRIMARY KEY (`cart_id`) USING BTREE,
  KEY `user_id` (`user_id`) USING BTREE COMMENT '(null)'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 ROW_FORMAT=DYNAMIC COMMENT='购物车表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_order_yiyatong`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_order_yiyatong` (
  `id` int NOT NULL AUTO_INCREMENT,
  `order_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '订单号',
  `order_sn` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '怡亚通订单号',
  `batch_order_sn` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '怡亚通订单批次号batchOrderSn',
  `order_status` int DEFAULT NULL COMMENT '订单状态：10为已提交待付款,20为已付款待发货,30为已发货待收货,40为确认收货交易成功,50、60为已取消,70、80、90、100为已关闭',
  `sku_ids` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '订单中包含的skuId信息，多个skuId用逗号隔开',
  `pay_amount` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '订单总金额(含运费)',
  `total_amount` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '商品总金额(不含运费)',
  `order_status_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '订单状态名称',
  `return_sn` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '订单退款售后单号',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci ROW_FORMAT=DYNAMIC COMMENT='怡亚通订单表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_pay_asset_details`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_pay_asset_details` (
  `detail_id` int NOT NULL AUTO_INCREMENT,
  `detail_type` int DEFAULT '0' COMMENT '明细类型:0-默认;1-余额充值;2-订单退款;3-提现失败;4-余额提现;5-余额转账;6-购物消费;7-购物卡充值;8-余额扣款；13分销佣金提现余额, 14 分销推广固定奖励',
  `asset_type` int DEFAULT '0' COMMENT '资产类型:1-收入；2-支出',
  `asset_amount` decimal(10,2) DEFAULT '0.00' COMMENT '金额',
  `asset_direction` varchar(100) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '来源/去向',
  `order_id` varchar(50) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '订单号',
  `create_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `last_update_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '上次更新时间',
  `pay_type` int DEFAULT '0' COMMENT '支付类型:1-余额；2-购物卡',
  `user_id` varchar(18) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL COMMENT '用户ID',
  `detail_comment` varchar(100) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT '' COMMENT '备注',
  PRIMARY KEY (`detail_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_pay_authorization_applet_information`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_pay_authorization_applet_information` (
  `authorizer_id` int NOT NULL AUTO_INCREMENT,
  `authorizer_appid` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL COMMENT '授权方appid',
  `secret` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '小程序secret',
  `authorizer_refresh_token` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL COMMENT '接口刷新令牌',
  `shop_id` int DEFAULT NULL COMMENT '商家id',
  `video_id_list` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '预览信息 视频',
  `pic_id_list` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '预览信息 图片',
  `audit_id` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '审核编号',
  PRIMARY KEY (`authorizer_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 ROW_FORMAT=DYNAMIC COMMENT='授权小程序信息表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_pay_bankcard`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_pay_bankcard` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` varchar(32) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL COMMENT '用户ID',
  `bank_code` varchar(32) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '银行编码',
  `bank_name` varchar(64) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '银行名称',
  `card_no_encrypted` varchar(256) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL COMMENT '卡号密文（AES）',
  `card_no_tail` varchar(8) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '卡号后四位',
  `card_type` tinyint NOT NULL DEFAULT '1' COMMENT '卡类型：1-储蓄卡；2-信用卡',
  `holder_name` varchar(64) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '持卡人',
  `holder_id_encrypted` varchar(256) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '持卡人身份证密文',
  `holder_mobile` varchar(16) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '银行预留手机号',
  `is_default` tinyint NOT NULL DEFAULT '0' COMMENT '是否默认：0-否；1-是',
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '状态：0-已删除；1-正常；2-已冻结',
  `verify_status` tinyint NOT NULL DEFAULT '0' COMMENT '验证状态：0-未验证；1-已验证',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_user_status` (`user_id`,`status`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 ROW_FORMAT=DYNAMIC COMMENT='用户银行卡';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_pay_callback_log`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_pay_callback_log` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `cashier_no` varchar(64) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '收银台订单号',
  `pay_order_id` varchar(64) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '支付平台订单号',
  `mch_order_no` varchar(64) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '商户订单号',
  `channel` varchar(32) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL COMMENT '渠道：WECHAT/ALIPAY/UNIONPAY/HUAWEIPAY/APPLEPAY/PAYPAL',
  `event_type` varchar(32) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL DEFAULT 'PAY' COMMENT '事件类型：PAY/REFUND/CLOSE',
  `req_headers` text CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci COMMENT '请求头原文',
  `req_body` longtext CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci COMMENT '请求报文原文',
  `sign_verify` tinyint NOT NULL DEFAULT '0' COMMENT '验签结果：0-未验；1-通过；2-不通过',
  `process_status` tinyint NOT NULL DEFAULT '0' COMMENT '处理状态：0-待处理；1-成功；2-失败；3-重复',
  `response_body` text CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci COMMENT '响应内容',
  `err_msg` varchar(500) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '错误信息',
  `receive_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '接收时间',
  `process_time` datetime DEFAULT NULL COMMENT '处理完成时间',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_cashier_no` (`cashier_no`) USING BTREE,
  KEY `idx_pay_order_id` (`pay_order_id`) USING BTREE,
  KEY `idx_channel_event` (`channel`,`event_type`) USING BTREE,
  KEY `idx_receive_time` (`receive_time`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 ROW_FORMAT=DYNAMIC COMMENT='支付渠道回调流水';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_pay_card`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_pay_card` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `card_num` varchar(30) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL COMMENT '卡号',
  `total_price` int NOT NULL COMMENT '面额',
  `give_price` float(10,2) DEFAULT '0.00' COMMENT '赠送(废弃)',
  `password` varchar(30) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL COMMENT '密码',
  `statut` tinyint NOT NULL COMMENT '状态0未使用，1已使用',
  `use_name` varchar(20) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '使用者',
  `creat_time` datetime DEFAULT NULL COMMENT '创建时间',
  `stime` datetime DEFAULT NULL COMMENT '有效开始时间',
  `etime` datetime DEFAULT NULL COMMENT '到期时间',
  `card_desc` varchar(60) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '卡片描述',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `indexnum` (`card_num`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 ROW_FORMAT=DYNAMIC COMMENT='mall_fkg系统充值卡表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_pay_card_base`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_pay_card_base` (
  `card_id` int NOT NULL AUTO_INCREMENT COMMENT '卡的id',
  `app_id` int NOT NULL DEFAULT '9999' COMMENT 'app id  ： 9999 通用',
  `card_name` varchar(100) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL COMMENT '卡名称',
  `card_prize` varchar(2000) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL COMMENT '卡片里面的奖品',
  `card_desc` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL DEFAULT '' COMMENT '卡片描述',
  `card_start_time` date NOT NULL COMMENT '卡的有效开始时间',
  `card_end_time` date NOT NULL COMMENT '卡的有效结束时间',
  `card_num` int NOT NULL DEFAULT '0' COMMENT '卡片数量',
  PRIMARY KEY (`card_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 ROW_FORMAT=DYNAMIC COMMENT='卡片基础信息表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_pay_card_info`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_pay_card_info` (
  `card_code` varchar(50) CHARACTER SET utf8mb3 COLLATE utf8mb3_czech_ci NOT NULL COMMENT '卡片激活码',
  `card_password` varchar(20) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL DEFAULT '' COMMENT '卡片密码',
  `card_id` int NOT NULL COMMENT '卡片id',
  `card_fetch_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '领奖时间',
  `card_media_id` smallint NOT NULL COMMENT '媒体id,参照base_card_media表',
  `server_id` varchar(11) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL DEFAULT '0' COMMENT '领卡人的服务器id',
  `user_id` varchar(18) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL COMMENT '用户ID',
  `user_account` varchar(100) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL DEFAULT '' COMMENT '领卡人账号',
  `card_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '卡牌生成时间',
  `card_money` decimal(10,2) NOT NULL DEFAULT '0.00' COMMENT '充值卡余额',
  `card_froze_money` decimal(10,2) NOT NULL DEFAULT '0.00' COMMENT '卡冻结金额',
  `parent_id` int DEFAULT '0' COMMENT '标记1可用2已使用0正常',
  PRIMARY KEY (`card_code`) USING BTREE,
  KEY `ucpIndexs` (`user_id`,`card_money`,`parent_id`) USING BTREE,
  KEY `card_id` (`card_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 ROW_FORMAT=DYNAMIC COMMENT='卡片信息表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_pay_card_record`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_pay_card_record` (
  `card_id` int NOT NULL AUTO_INCREMENT COMMENT '卡的id',
  `app_id` int DEFAULT '9999' COMMENT 'app id  ： 9999 通用',
  `card_creator` varchar(100) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '创建者',
  `total_price` int DEFAULT NULL COMMENT '面额',
  `card_desc` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT '' COMMENT '卡片描述',
  `creat_time` datetime DEFAULT NULL COMMENT '创建时间',
  `stime` int unsigned DEFAULT NULL COMMENT '有效开始时间',
  `etime` int unsigned DEFAULT NULL COMMENT '到期时间',
  `card_number` int DEFAULT '0' COMMENT '卡片数量',
  `card_start` int NOT NULL COMMENT '开始卡序',
  `card_end` int NOT NULL COMMENT '结束卡序',
  PRIMARY KEY (`card_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 ROW_FORMAT=DYNAMIC COMMENT='生成充值卡信息记录表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_pay_cashier_order`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_pay_cashier_order` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `cashier_no` varchar(64) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL COMMENT '收银台订单号（前端感知）',
  `mch_order_no` varchar(64) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '商户订单号（下游）',
  `request_no` varchar(64) DEFAULT NULL COMMENT '业务幂等请求号',
  `mall_order_no` varchar(64) DEFAULT NULL COMMENT '商城订单号',
  `pay_order_id` varchar(64) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '支付平台订单号（回调关联）',
  `user_id` varchar(32) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL COMMENT '下单用户ID',
  `mch_no` varchar(32) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '商户号',
  `app_id` varchar(32) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '应用ID',
  `terminal` varchar(32) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL COMMENT '终端：WX_MINI/ALI_MINI/BD_MINI/TT_MINI/QQ_MINI/360_MINI/KS_MINI/LARK_MINI/JD_MINI/XHS_MINI/APP/HW/QUICKAPP/HARMONY/WAP/PC',
  `pay_method` varchar(32) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL COMMENT '支付方式：WECHAT/ALIPAY/BALANCE/UNIONPAY/APPLEPAY/HUAWEIPAY/PAYPAL/BANKCARD',
  `amount` decimal(12,2) NOT NULL DEFAULT '0.00' COMMENT '支付金额（元）',
  `currency` varchar(8) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL DEFAULT 'CNY' COMMENT '币种',
  `subject` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '订单标题',
  `body` varchar(500) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '订单描述',
  `client_ip` varchar(64) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '下单客户端IP',
  `notify_url` varchar(500) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '业务回调地址',
  `return_url` varchar(500) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '支付完成跳转地址',
  `ext_param` text CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci COMMENT '扩展参数(JSON)',
  `pay_data` text CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci COMMENT '下发给前端的调起参数(JSON)',
  `status` tinyint NOT NULL DEFAULT '0' COMMENT '状态：0-待支付；1-支付中；2-支付成功；3-支付失败；4-已关闭；5-已退款',
  `err_code` varchar(64) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '错误码',
  `err_msg` varchar(500) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '错误信息',
  `expire_time` datetime DEFAULT NULL COMMENT '支付过期时间',
  `success_time` datetime DEFAULT NULL COMMENT '支付成功时间',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uk_cashier_no` (`cashier_no`) USING BTREE,
  UNIQUE KEY `uk_cashier_request_no` (`request_no`),
  KEY `idx_mch_order_no` (`mch_order_no`) USING BTREE,
  KEY `idx_pay_order_id` (`pay_order_id`) USING BTREE,
  KEY `idx_user_status` (`user_id`,`status`) USING BTREE,
  KEY `idx_create_time` (`create_time`) USING BTREE,
  KEY `idx_cashier_mall_order_no` (`mall_order_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 ROW_FORMAT=DYNAMIC COMMENT='统一收银台订单';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_pay_channel`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_pay_channel` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `channel_code` varchar(32) NOT NULL COMMENT '渠道编码（如 WX_JSAPI/ALI_QR/UNION_PAY/YSF_PAY/STRIPE）',
  `channel_name` varchar(128) NOT NULL COMMENT '渠道名称',
  `provider` varchar(32) NOT NULL COMMENT '渠道服务商：WECHAT/ALIPAY/UNIONPAY/CMB/ICBC/STRIPE/PAYPAL',
  `pay_method` varchar(32) NOT NULL COMMENT '支付方式（对应 PayMethodEnum：WX_JSAPI/ALI_QR等）',
  `priority` int NOT NULL DEFAULT '100' COMMENT '基础优先级（越小越优先）',
  `weight` int NOT NULL DEFAULT '100' COMMENT '权重（用于加权随机/轮询，越大命中概率越高）',
  `daily_limit` decimal(20,2) DEFAULT NULL COMMENT '日限额（NULL 不限）',
  `single_limit` decimal(20,2) DEFAULT NULL COMMENT '单笔限额（NULL 不限）',
  `single_min` decimal(20,2) DEFAULT '0.01' COMMENT '单笔最小金额',
  `fee_rate` decimal(8,5) DEFAULT NULL COMMENT '渠道费率（如 0.006 = 0.6%）',
  `fallback_codes` varchar(512) DEFAULT NULL COMMENT '降级渠道编码（逗号分隔，按序降级）',
  `health_score` int NOT NULL DEFAULT '100' COMMENT '健康度评分 0-100（实时统计的成功率）',
  `total_count` bigint NOT NULL DEFAULT '0' COMMENT '总调用次数',
  `success_count` bigint NOT NULL DEFAULT '0' COMMENT '成功次数',
  `last_failed_at` datetime DEFAULT NULL COMMENT '最后一次失败时间',
  `circuit_status` tinyint NOT NULL DEFAULT '0' COMMENT '熔断状态：0-正常 1-半开 2-熔断',
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '状态：0-停用 1-启用',
  `merchant_no` varchar(32) DEFAULT NULL COMMENT '所属商户号（NULL 全局）',
  `config_json` text COMMENT '渠道扩展配置 JSON（appId/mchId/key 等）',
  `remark` varchar(512) DEFAULT NULL COMMENT '备注',
  `creator_id` varchar(64) DEFAULT NULL COMMENT '创建人',
  `updater_id` varchar(64) DEFAULT NULL COMMENT '更新人',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '逻辑删除：0-否 1-是',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_channel_code` (`channel_code`),
  KEY `idx_pay_method` (`pay_method`,`status`,`priority`),
  KEY `idx_provider_status` (`provider`,`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='支付渠道配置表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_pay_channel_compensate`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_pay_channel_compensate` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `task_no` varchar(32) NOT NULL COMMENT '任务编号',
  `biz_no` varchar(64) NOT NULL COMMENT '业务订单号',
  `biz_type` tinyint NOT NULL DEFAULT '1' COMMENT '业务类型：1-支付 2-退款 3-提现',
  `channel_code` varchar(32) NOT NULL COMMENT '渠道编码',
  `query_count` int NOT NULL DEFAULT '0' COMMENT '查询次数',
  `max_query` int NOT NULL DEFAULT '10' COMMENT '最大查询次数',
  `next_execute_time` datetime NOT NULL COMMENT '下次执行时间',
  `last_query_time` datetime DEFAULT NULL COMMENT '最后一次查询时间',
  `last_result` varchar(64) DEFAULT NULL COMMENT '最后一次查询结果（PENDING/SUCCESS/FAILED/CLOSED）',
  `task_status` tinyint NOT NULL DEFAULT '1' COMMENT '任务状态：1-待执行 2-执行中 3-成功结束 4-失败结束 5-已放弃',
  `final_status` varchar(32) DEFAULT NULL COMMENT '最终订单状态',
  `error_msg` varchar(512) DEFAULT NULL COMMENT '错误信息',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '逻辑删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_task_no` (`task_no`),
  KEY `idx_biz_no` (`biz_no`),
  KEY `idx_status_next` (`task_status`,`next_execute_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='统一补偿查询任务表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_pay_channel_log`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_pay_channel_log` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `log_no` varchar(32) NOT NULL COMMENT '日志编号',
  `biz_no` varchar(64) DEFAULT NULL COMMENT '业务订单号（cashierNo/refundNo）',
  `route_no` varchar(32) DEFAULT NULL COMMENT '命中的路由规则编号',
  `channel_code` varchar(32) NOT NULL COMMENT '最终选中的渠道编码',
  `pay_method` varchar(32) DEFAULT NULL COMMENT '支付方式',
  `terminal` varchar(32) DEFAULT NULL COMMENT '终端',
  `merchant_no` varchar(32) DEFAULT NULL COMMENT '商户号',
  `amount` decimal(20,2) DEFAULT NULL COMMENT '金额',
  `select_strategy` varchar(16) DEFAULT NULL COMMENT '选中时的策略：PRIORITY/WEIGHT/RANDOM/FALLBACK',
  `fallback_chain` varchar(512) DEFAULT NULL COMMENT '降级链路（c1->c2->c3 表示发生过降级）',
  `result` tinyint NOT NULL DEFAULT '0' COMMENT '调用结果：0-未知 1-成功 2-失败',
  `cost_ms` int DEFAULT NULL COMMENT '调用耗时(ms)',
  `error_code` varchar(64) DEFAULT NULL COMMENT '错误码',
  `error_msg` varchar(512) DEFAULT NULL COMMENT '错误信息',
  `extra_context` text COMMENT '扩展上下文 JSON',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '逻辑删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_log_no` (`log_no`),
  KEY `idx_biz_no` (`biz_no`),
  KEY `idx_channel` (`channel_code`,`result`),
  KEY `idx_create` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='渠道调用日志表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_pay_channel_route`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_pay_channel_route` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `route_no` varchar(32) NOT NULL COMMENT '规则编号',
  `route_name` varchar(128) NOT NULL COMMENT '规则名称',
  `biz_type` tinyint NOT NULL DEFAULT '0' COMMENT '业务类型：0-全部 1-下单 2-退款 3-提现',
  `pay_method` varchar(32) DEFAULT NULL COMMENT '支付方式（NULL 任意）',
  `terminal` varchar(32) DEFAULT NULL COMMENT '终端（H5/MP-WEIXIN/MP-ALIPAY/APP，NULL 任意）',
  `merchant_no` varchar(32) DEFAULT NULL COMMENT '商户号（NULL 任意）',
  `amount_min` decimal(20,2) DEFAULT NULL COMMENT '最小金额（NULL 不限）',
  `amount_max` decimal(20,2) DEFAULT NULL COMMENT '最大金额（NULL 不限）',
  `channel_codes` varchar(1024) NOT NULL COMMENT '候选渠道（逗号分隔，按权重选取）',
  `strategy` varchar(16) NOT NULL DEFAULT 'WEIGHT' COMMENT '路由策略：PRIORITY-按优先级 WEIGHT-加权 RANDOM-随机',
  `priority` int NOT NULL DEFAULT '100' COMMENT '规则优先级（越小越优先）',
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '状态：0-停用 1-启用',
  `effective_time` datetime DEFAULT NULL COMMENT '生效时间',
  `expire_time` datetime DEFAULT NULL COMMENT '失效时间',
  `remark` varchar(512) DEFAULT NULL COMMENT '备注',
  `creator_id` varchar(64) DEFAULT NULL COMMENT '创建人',
  `updater_id` varchar(64) DEFAULT NULL COMMENT '更新人',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '逻辑删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_route_no` (`route_no`),
  KEY `idx_biz_method` (`biz_type`,`pay_method`,`priority`),
  KEY `idx_status_priority` (`status`,`priority`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='渠道路由规则表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_pay_config`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_pay_config` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `paymentMode` varchar(50) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '支付方式 00微信 PC h5 app 02微信小程序 03支付宝APP 04 微信公众号',
  `appID` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT 'APP Id',
  `appSecret` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT 'APP Secret',
  `appMCHId` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '商户号',
  `appAPIKey` varchar(2048) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT 'APP密钥',
  `refundUrl` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '微信退款URL',
  `notifyUrl` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '微信支付回调接口',
  `redirectUrl` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '微信异步通知页面路径',
  `weChatPcPath` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '微信pc端证书地址',
  `weChatWapPath` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '微信wap端证书地址',
  `gatewayURL` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '支付宝网关请求地址',
  `shoppingNotifyUrl` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '支付宝服务器异步通知页面路径',
  `rechargeNotifyUrl2` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '支付宝付款服务器异步通知页面路径',
  `shoppingReturnUrl` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '支付宝页面跳转同步通知页面路径',
  `shoppingReturnUrlPc` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '支付宝pc购物回调',
  `paymentStatus` char(5) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT '1' COMMENT '启用和禁用状态，00禁用  01启用，默认启用',
  `paymentIsActive` char(5) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT '1' COMMENT '伪删除标识 00删除 01正常，默认正常',
  `paymentUpdateDate` timestamp NULL DEFAULT '2001-01-01 17:01:01' COMMENT '更新时间',
  `cert_public_key_res2` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '支付宝公钥证书',
  `cert_root_key` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '支付宝根证书',
  `cert_app_public_key` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '应用公钥证书',
  `plusNotifyUrl` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT 'plus会员回调地址',
  `balanceRechargeNotifyUrl` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '余额充值回调地址',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 ROW_FORMAT=DYNAMIC COMMENT='支付配置信息表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_pay_config_change_log`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_pay_config_change_log` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `target_table` varchar(64) COLLATE utf8mb4_general_ci NOT NULL,
  `target_id` bigint NOT NULL,
  `operation` enum('CREATE','UPDATE','DELETE','TOGGLE') COLLATE utf8mb4_general_ci DEFAULT NULL,
  `before_json` text COLLATE utf8mb4_general_ci,
  `after_json` text COLLATE utf8mb4_general_ci,
  `operator` varchar(64) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `operator_ip` varchar(64) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_pay_config_shop`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_pay_config_shop` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `appID` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT 'APP Id',
  `appSecret` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT 'APP Secret',
  `appMCHId` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '商户号',
  `appAPIKey` varchar(2048) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT 'APP密钥',
  `refundUrl` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '微信退款URL',
  `notifyUrl` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '微信支付回调接口',
  `redirectUrl` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '微信异步通知页面路径',
  `appletNotifyUrl` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '微信小程序回调',
  `weChatWapPath` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '微信wap端证书地址',
  `shopId` int DEFAULT NULL COMMENT '店铺id',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 ROW_FORMAT=DYNAMIC COMMENT='品牌店铺支付配置信息表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_pay_consume_deposit`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_pay_consume_deposit` (
  `deposit_trade_no` varchar(64) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL DEFAULT '' COMMENT '交易号',
  `deposit_buyer_id` varchar(30) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL DEFAULT '' COMMENT '用户号',
  `deposit_total_fee` decimal(12,2) NOT NULL DEFAULT '0.00' COMMENT '交易金额',
  `deposit_pay_channel` int NOT NULL COMMENT '充值的付款方式',
  `deposit_gmt_create` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '交易创建时间',
  `deposit_gmt_payment` datetime DEFAULT NULL COMMENT '交易付款时间',
  `deposit_gmt_close` datetime DEFAULT NULL COMMENT '交易关闭时间',
  `deposit_trade_status` tinyint(1) NOT NULL COMMENT '交易状态',
  `deposit_return_trade_no` varchar(50) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT '' COMMENT '充值成功以后的流水',
  `deposit_is_recharge_package` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否为套餐充值，1-是；0-普通充值',
  `deposit_type` int DEFAULT '1' COMMENT '1是余额 2 是充值卡 ',
  `deposit_operator` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL,
  `order_operator` varchar(32) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '后台充值操作人',
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'id',
  `ts` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '时间戳',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `deposit_trade_no` (`deposit_trade_no`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 ROW_FORMAT=DYNAMIC COMMENT='充值表-支付回调callback使用-确认付款';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_pay_consume_record`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_pay_consume_record` (
  `consume_record_id` bigint(20) unsigned zerofill NOT NULL AUTO_INCREMENT COMMENT '交易记录id',
  `order_id` varchar(100) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL COMMENT 'U开头：支付单号， R开头：充值单号， W开头：提现单号， T开头：转账单号， 纯短数字：结算单号',
  `user_id` varchar(18) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL COMMENT '所属用id',
  `user_nickname` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '昵称',
  `record_money` decimal(16,2) NOT NULL COMMENT '金额',
  `record_after_money` decimal(20,2) NOT NULL DEFAULT '0.00' COMMENT '操作之后的金额',
  `record_date` datetime NOT NULL COMMENT '年-月-日',
  `record_year` smallint NOT NULL COMMENT '年',
  `record_month` tinyint NOT NULL COMMENT '月',
  `record_day` tinyint NOT NULL COMMENT '日',
  `record_title` varchar(100) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL DEFAULT '' COMMENT '标题',
  `record_desc` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL COMMENT '描述',
  `record_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `trade_type_id` tinyint(1) NOT NULL DEFAULT '0' COMMENT '交易类型(详情pdb_wordbook_pay_trade_type)',
  `user_type` tinyint(1) NOT NULL COMMENT '1-收款方 2-付款方 3-合并订单',
  `record_status` tinyint(1) NOT NULL DEFAULT '1' COMMENT '付款状态',
  `record_payorder` varchar(4000) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '实际支付单号',
  `record_paytime` datetime DEFAULT NULL COMMENT '支付时间',
  `record_delete` tinyint(1) NOT NULL DEFAULT '0' COMMENT '0未删除 1删除',
  `credit_remain` decimal(16,2) DEFAULT NULL COMMENT '白条剩余还款金额',
  `plus_or_minus` int NOT NULL DEFAULT '1' COMMENT '金额正负判断(1:+0:-)',
  `record_withdraw` tinyint unsigned NOT NULL DEFAULT '0' COMMENT '是否提现0未提现 1已提现',
  `os_commis_amount` decimal(10,2) NOT NULL DEFAULT '0.00' COMMENT '佣金金额',
  `os_commis_return_amount` decimal(10,2) NOT NULL DEFAULT '0.00' COMMENT '退款佣金金额',
  `record_operator` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL,
  `ts` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '时间戳',
  `total_integral` int DEFAULT NULL COMMENT '兑换积分商品所用的积分',
  `is_current_user` int DEFAULT '1' COMMENT '判断当前订单是否属于当前用户 1 是 2 否',
  PRIMARY KEY (`consume_record_id`) USING BTREE,
  UNIQUE KEY `consume_record_id` (`consume_record_id`) USING BTREE,
  KEY `order_id` (`order_id`) USING BTREE,
  KEY `user_id` (`user_id`) USING BTREE,
  KEY `record_payorder` (`record_payorder`(255)) USING BTREE,
  KEY `record_status` (`record_status`) USING BTREE,
  KEY `user_type` (`user_type`) USING BTREE,
  KEY `record_time` (`record_time`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 ROW_FORMAT=DYNAMIC COMMENT='交易明细表-账户收支明细-资金流水表-账户金额变化流水';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_pay_consume_trade`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_pay_consume_trade` (
  `consume_trade_id` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '交易订单id',
  `order_id` varchar(100) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL COMMENT '商户订单id',
  `buyer_id` varchar(18) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL DEFAULT '0' COMMENT '买家id',
  `seller_id` varchar(18) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT '0' COMMENT '卖家id',
  `pay_user_id` varchar(18) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL DEFAULT '0' COMMENT '付款人id',
  `order_state_id` tinyint NOT NULL DEFAULT '1' COMMENT '订单状态()',
  `trade_type_id` tinyint(1) NOT NULL COMMENT '交易类型',
  `payment_channel_id` tinyint NOT NULL DEFAULT '0' COMMENT '支付渠道',
  `trade_type` tinyint(1) NOT NULL DEFAULT '2' COMMENT '1:担保交易  2：直接交易',
  `order_payment_amount` decimal(10,2) NOT NULL DEFAULT '0.00' COMMENT '总付款额度 = trade_payment_amount + trade_payment_money + trade_payment_recharge_card + trade_payment_points',
  `trade_payment_amount` decimal(10,2) NOT NULL DEFAULT '0.00' COMMENT '实付金额，在线支付金额',
  `trade_third_party_amount` decimal(10,2) DEFAULT '0.00' COMMENT '第三方支付金额',
  `trade_payment_money` decimal(10,2) DEFAULT '0.00' COMMENT '余额支付',
  `trade_payment_recharge_card` decimal(10,2) DEFAULT '0.00' COMMENT '充值卡余额支付',
  `trade_discount` decimal(10,2) DEFAULT '0.00' COMMENT '折扣优惠',
  `trade_date` date NOT NULL COMMENT '年-月-日',
  `trade_year` smallint NOT NULL COMMENT '年',
  `trade_month` tinyint NOT NULL COMMENT '月',
  `trade_day` tinyint NOT NULL COMMENT '日',
  `trade_title` varchar(100) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL DEFAULT '' COMMENT '标题',
  `trade_desc` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL DEFAULT '' COMMENT '描述',
  `trade_create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `trade_pay_time` timestamp NOT NULL DEFAULT '2001-01-01 17:01:01' COMMENT '付款时间',
  `trade_finish_time` timestamp NOT NULL DEFAULT '2001-01-01 17:01:01' COMMENT '结束时间',
  `ts` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '时间戳',
  `total_integral` int DEFAULT '0' COMMENT '兑换积分商品所用的积分',
  `integral_goods` int NOT NULL DEFAULT '1' COMMENT '积分商品 0 是 1 否',
  PRIMARY KEY (`order_id`) USING BTREE,
  KEY `company_id` (`payment_channel_id`) USING BTREE,
  KEY `consume_trade_id` (`consume_trade_id`) USING BTREE,
  KEY `buyer_id` (`buyer_id`) USING BTREE,
  KEY `seller_id` (`seller_id`) USING BTREE,
  KEY `order_state_id` (`order_state_id`) USING BTREE,
  KEY `userId` (`buyer_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 ROW_FORMAT=DYNAMIC COMMENT='交易订单表-强调唯一订单-充值则先创建充值订单';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_pay_consume_withdraw`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_pay_consume_withdraw` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `pay_uid` varchar(18) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL COMMENT '会员支付ID',
  `orderid` varchar(50) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '交易明细ID',
  `amount` decimal(10,2) NOT NULL COMMENT '提现金额',
  `add_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `censor` varchar(50) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '管理员',
  `check_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
  `is_succeed` tinyint DEFAULT '0' COMMENT '是否成功 0  待审核, 1 进行中, 2 打款完成, 3 打款中, 4 不通过',
  `bankflow` varchar(50) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '银行流水账号',
  `con` text CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci COMMENT '描述',
  `bank` varchar(50) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '银行名称',
  `bank_address` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '银行详细地址',
  `cardno` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '银行卡号',
  `cardname` varchar(50) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '开户人姓名',
  `fee` float(10,2) DEFAULT '0.00' COMMENT '手续费',
  `remark` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '平台审核备注',
  `shop_name` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '店铺名',
  `user_name` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '用户名',
  `settlement_order_id` varchar(2555) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '结算单号',
  `pay_amount` decimal(10,2) DEFAULT '0.00' COMMENT '付款金额',
  `withdraw_type` tinyint(1) DEFAULT '1' COMMENT '结算订单类型 1-普通订单 2-商家一键上架供应商销售订单商家结算单  3商家一键上架供应商销售订单供应商结算单 4直播分佣结算',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 ROW_FORMAT=DYNAMIC COMMENT='提现申请表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_pay_division_receiver`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_pay_division_receiver` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '主键',
  `mch_no` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '所属商户号',
  `receiver_no` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '接收方业务编号(平台生成)',
  `receiver_type` varchar(16) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '接收方类型 PERSON/MERCHANT/PLATFORM',
  `receiver_name` varchar(128) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '接收方名称',
  `account_type` varchar(16) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'BANK' COMMENT '账户类型 BANK/INNER',
  `account_no` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '账户号(银行卡/平台账户)',
  `account_bank` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '开户行',
  `id_card` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '证件号(脱敏存储)',
  `phone` varchar(32) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '联系手机',
  `share_ratio` int NOT NULL DEFAULT '0' COMMENT '默认分账比例(万分比 0-10000)',
  `priority` int NOT NULL DEFAULT '0' COMMENT '优先级 数字越大越优先',
  `min_share_amount` bigint DEFAULT NULL COMMENT '最低分账金额（分）',
  `max_share_amount` bigint DEFAULT NULL COMMENT '最大分账金额（分）',
  `is_fallback` tinyint(1) DEFAULT '0' COMMENT '是否兜底接收方',
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '0已停用 1启用',
  `remark` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '备注',
  `create_user_id` bigint DEFAULT NULL COMMENT '创建人',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_mch_receiver` (`mch_no`,`receiver_no`),
  KEY `idx_status` (`status`),
  KEY `idx_type` (`receiver_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='自研分账接收方预配置';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_pay_fx_account`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_pay_fx_account` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '主键id',
  `user_id` varchar(18) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL COMMENT '用户id',
  `user_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '用户名',
  `user_mobile` varchar(50) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '用户手机号',
  `total_account` decimal(20,2) unsigned DEFAULT '0.00' COMMENT '总金额',
  `vk_account` decimal(20,2) unsigned DEFAULT '0.00' COMMENT '微客佣金可提现',
  `withdraw_freeze_account` decimal(20,2) DEFAULT NULL COMMENT '提现冻结金额',
  `mini_wx_open_id` varchar(50) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '小程序openId',
  `mini_wx_user_name` varchar(225) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '小程序微信名',
  `mini_wx_union_id` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '小程序用户标识',
  `wx_open_id` varchar(50) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '用户微信openid',
  `wx_user_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '微信授权用户名',
  `wx_union_id` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '微信用户标识',
  `create_time` datetime NOT NULL DEFAULT '2021-04-08 00:00:00' COMMENT '创建时间',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `id` (`id`) USING BTREE,
  UNIQUE KEY `user_id` (`user_id`) USING BTREE,
  KEY `user_mobile` (`user_mobile`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 ROW_FORMAT=DYNAMIC COMMENT='用户分销佣金账户表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_pay_fx_record`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_pay_fx_record` (
  `record_id` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL COMMENT '交易单号',
  `user_id` varchar(18) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL COMMENT '用户ID',
  `user_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '用户名',
  `record_amount` decimal(20,2) DEFAULT NULL COMMENT '交易金额',
  `before_record_amount` decimal(20,2) DEFAULT NULL COMMENT '交易之前用户总余额',
  `after_record_amount` decimal(20,2) DEFAULT NULL COMMENT '交易之后用户总余额',
  `record_status` tinyint(1) NOT NULL COMMENT '交易状态 1:待处理 2交易完成 3交易取消4提现失败',
  `trade_type_id` tinyint DEFAULT NULL COMMENT '交易类型1获得佣金 2提现',
  `record_type` tinyint(1) NOT NULL DEFAULT '1' COMMENT '1微客佣金 2员工佣金',
  `plus_or_minus` int DEFAULT NULL COMMENT '金额正负判断(1:+0:-)',
  `pay_code` int DEFAULT NULL COMMENT '支付方式1支付宝 2微信',
  `pay_from` int DEFAULT NULL COMMENT '支付来源手机端1PC 2wap微信浏览器 3安卓 5ios 6微信小程序 7wap浏览器',
  `wx_open_id` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '提现到微信用户openid',
  `wx_user_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '提现到微信用户昵称',
  `record_name` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '描述',
  `record_status_name` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '状态描述',
  `record_date` date DEFAULT NULL COMMENT '日期',
  `create_time` datetime DEFAULT NULL COMMENT '交易时间',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`record_id`) USING BTREE,
  UNIQUE KEY `record_id` (`record_id`) USING BTREE,
  KEY `user_id` (`user_id`) USING BTREE,
  KEY `trade_type_id` (`trade_type_id`) USING BTREE,
  KEY `record_date` (`record_date`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 ROW_FORMAT=DYNAMIC COMMENT='用户分销佣金交易记录表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_pay_fx_trade`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_pay_fx_trade` (
  `third_pay_id` varchar(50) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL COMMENT '第三方交易请求单号',
  `record_id` varchar(50) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '交易单号',
  `third_trade_no` varchar(100) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '第三方交易单号',
  `user_id` varchar(18) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '用户id',
  `user_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '用户名',
  `total_money` decimal(20,2) DEFAULT NULL COMMENT '交易总金额',
  `trade_rate` decimal(20,2) DEFAULT NULL COMMENT '交易费率',
  `trade_desc` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '交易描述',
  `trade_type_id` tinyint DEFAULT NULL COMMENT '交易类型',
  `trade_status` int DEFAULT NULL COMMENT '是否支付 0:未支付 1:已支付 2:提现失败 3已取消',
  `pay_type` varchar(50) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '支付宝:ALI  微信:WX',
  `error_code` varchar(50) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '第三方失败错误码',
  `error_msg` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '回调失败时返回的信息',
  `query_count` int DEFAULT '0' COMMENT '提现失败补偿次数',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `notify_time` datetime DEFAULT NULL COMMENT '回调时间',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`third_pay_id`) USING BTREE,
  UNIQUE KEY `third_pay_id` (`third_pay_id`) USING BTREE,
  KEY `record_id` (`record_id`) USING BTREE,
  KEY `trade_type_id` (`trade_type_id`) USING BTREE,
  KEY `third_trade_no` (`third_trade_no`) USING BTREE,
  KEY `error_code` (`error_code`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 ROW_FORMAT=DYNAMIC COMMENT='分销佣金提现第三方交易记录表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_pay_member_product`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_pay_member_product` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '主键',
  `member_id` bigint NOT NULL COMMENT '会员ID(关联 PayUserBaseEntity)',
  `mch_no` varchar(32) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '所属商户号(可空)',
  `product_code` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '产品编码 PAY_BASIC/WITHDRAW/DIVISION/QUICK_PAY 等',
  `product_name` varchar(128) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '产品名称',
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '0已关闭 1已开通 2待审核 3已驳回',
  `fee_rate` int NOT NULL DEFAULT '0' COMMENT '费率(万分比)',
  `daily_limit` bigint DEFAULT NULL COMMENT '日限额(分)',
  `single_limit` bigint DEFAULT NULL COMMENT '单笔限额(分)',
  `open_time` datetime DEFAULT NULL COMMENT '开通时间',
  `close_time` datetime DEFAULT NULL COMMENT '关闭时间',
  `remark` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '备注',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_member_product` (`member_id`,`product_code`),
  KEY `idx_status` (`status`),
  KEY `idx_mch` (`mch_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='自研会员产品开通记录';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_pay_merchant`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_pay_merchant` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `mch_no` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '平台分配商户号（唯一）',
  `app_id` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '应用ID（商户侧）',
  `mch_type` tinyint(1) NOT NULL DEFAULT '1' COMMENT '商户类型：1-企业，2-个体工商户，3-个人',
  `company_name` varchar(128) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '企业/商户全称',
  `mch_short_name` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '企业简称',
  `uni_credit` varchar(32) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '统一社会信用代码（企业必填）',
  `license_img` varchar(256) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '营业执照图片URL',
  `legal_name` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '法人/负责人姓名',
  `legal_id_type` tinyint(1) NOT NULL DEFAULT '1' COMMENT '证件类型：1-身份证 2-港澳居民来往内地通行证 3-护照',
  `legal_id_no` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '法人证件号',
  `legal_phone` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '法人手机号',
  `legal_email` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '法人邮箱',
  `legal_id_front` varchar(256) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '法人证件正面URL',
  `legal_id_back` varchar(256) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '法人证件背面URL',
  `contact_name` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '联系人姓名',
  `contact_phone` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '联系人手机',
  `contact_email` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '联系人邮箱',
  `province_code` varchar(12) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '省份代码',
  `city_code` varchar(12) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '城市代码',
  `district_code` varchar(12) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '区县代码',
  `address` varchar(256) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '详细地址',
  `business_scope` varchar(512) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '经营范围',
  `industry_code` varchar(16) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '行业编码（MCC）',
  `login_username` varchar(32) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '商户后台登录账号',
  `cusid` varchar(32) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '收银宝商户号（对接通联）',
  `agency_no` varchar(32) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '拓展方/代理商编号',
  `org_id` varchar(32) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '服务商ID',
  `status` tinyint NOT NULL DEFAULT '0' COMMENT '商户状态：0-待审核 1-审核中 2-已生效 3-已冻结 4-已注销 5-审核失败',
  `status_remark` varchar(256) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '状态备注（如审核失败原因）',
  `approved_time` datetime DEFAULT NULL COMMENT '审核通过时间',
  `approved_by` varchar(32) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '审核人ID',
  `frozen_time` datetime DEFAULT NULL COMMENT '冻结时间',
  `frozen_reason` varchar(256) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '冻结原因',
  `settle_type` tinyint(1) DEFAULT '1' COMMENT '结算账户类型：1-对公 2-对私',
  `settle_bank_code` varchar(16) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '结算银行编码',
  `settle_bank_name` varchar(128) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '结算银行名称',
  `settle_branch_name` varchar(128) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '开户支行名称',
  `settle_account_name` varchar(128) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '结算账户户名',
  `settle_account_no` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '结算账号',
  `settle_reserved_mobile` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '银行预留手机号',
  `settle_cycle` tinyint(1) DEFAULT '1' COMMENT '结算周期：0-D0 1-T1 2-T2 3-周结 4-月结',
  `creator_id` varchar(32) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '创建人ID',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '逻辑删除：0-未删除 1-已删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_mch_no` (`mch_no`),
  KEY `idx_uni_credit` (`uni_credit`),
  KEY `idx_legal_phone` (`legal_phone`),
  KEY `idx_status` (`status`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='支付商户主表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_pay_merchant_fee`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_pay_merchant_fee` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `mch_no` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '商户号',
  `product_code` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '产品编码（对应 merchant_product.product_code）',
  `pay_method` varchar(32) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '支付方式编码：WX_LITE/ALI_JSAPI 等；为空代表整产品默认',
  `fee_type` tinyint(1) NOT NULL DEFAULT '1' COMMENT '费率类型：1-按比例 2-按笔固定 3-混合',
  `fee_rate` decimal(8,6) DEFAULT '0.000000' COMMENT '费率（比例模式，0.006000 = 0.6%）',
  `fee_fixed` decimal(10,2) DEFAULT '0.00' COMMENT '固定手续费（元）',
  `fee_min` decimal(10,2) DEFAULT NULL COMMENT '单笔手续费下限',
  `fee_max` decimal(10,2) DEFAULT NULL COMMENT '单笔手续费上限',
  `effective_time` datetime DEFAULT NULL COMMENT '费率生效时间',
  `expire_time` datetime DEFAULT NULL COMMENT '费率失效时间',
  `status` tinyint(1) NOT NULL DEFAULT '1' COMMENT '状态：0-已停用 1-生效中',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '逻辑删除：0-未删除 1-已删除',
  PRIMARY KEY (`id`),
  KEY `idx_mch_product` (`mch_no`,`product_code`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='商户费率配置';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_pay_merchant_product`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_pay_merchant_product` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `mch_no` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '商户号',
  `product_code` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '产品编码：WX_PAY / ALI_PAY / QUICK_PAY / BALANCE / DIVISION / WITHDRAW 等',
  `product_name` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '产品名称',
  `status` tinyint NOT NULL DEFAULT '0' COMMENT '产品状态：0-申请中 1-已开通 2-已停用 3-审核失败',
  `single_limit` decimal(14,2) DEFAULT NULL COMMENT '单笔限额（元）',
  `daily_limit` decimal(14,2) DEFAULT NULL COMMENT '单日累计限额（元）',
  `monthly_limit` decimal(14,2) DEFAULT NULL COMMENT '单月累计限额（元）',
  `open_time` datetime DEFAULT NULL COMMENT '开通时间',
  `close_time` datetime DEFAULT NULL COMMENT '关闭时间',
  `remark` varchar(256) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '备注',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '逻辑删除：0-未删除 1-已删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_mch_product` (`mch_no`,`product_code`,`deleted`),
  KEY `idx_mch_no` (`mch_no`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='商户产品开通记录';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_pay_merchant_qual`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_pay_merchant_qual` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `tenant_id` bigint NOT NULL DEFAULT '0' COMMENT '租户 ID（单租户填 0）',
  `terminal` varchar(20) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL COMMENT '终端编码（TerminalTypeEnum：WX_MINI/ALI_MINI/HARMONY 等 16 项）',
  `channel` varchar(20) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL COMMENT '支付渠道：WECHAT/ALIPAY/UNIONPAY/APPLE/GOOGLE/PAYPAL/STRIPE/HUAWEI',
  `qual_name` varchar(100) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL COMMENT '资质名称',
  `env` char(1) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL DEFAULT 'P' COMMENT '环境：S沙箱 P生产',
  `app_id` varchar(128) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '平台 App ID',
  `app_secret` varchar(512) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '平台 App Secret（AES 加密）',
  `mch_id` varchar(64) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '渠道商户号',
  `api_key` varchar(2048) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '渠道 API 密钥/RSA 私钥（AES 加密）',
  `api_public_key` varchar(2048) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '渠道 API 公钥/平台公钥',
  `notify_url` varchar(512) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '异步回调地址',
  `return_url` varchar(512) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '同步跳转地址',
  `ext_config` text CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci COMMENT '扩展配置（JSON）',
  `status` tinyint(1) NOT NULL DEFAULT '1' COMMENT '状态：0禁用 1启用',
  `remark` varchar(500) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '备注',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_by` varchar(64) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '创建人',
  `update_by` varchar(64) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '更新人',
  `is_deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '逻辑删除：0未删 1已删',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uk_tenant_terminal_channel_env` (`tenant_id`,`terminal`,`channel`,`env`,`is_deleted`) USING BTREE,
  KEY `idx_terminal_channel` (`terminal`,`channel`) USING BTREE,
  KEY `idx_status` (`status`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 ROW_FORMAT=DYNAMIC COMMENT='商户资质主表（按终端+渠道+环境唯一）';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_pay_merchant_qual_cert`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_pay_merchant_qual_cert` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `qual_id` bigint NOT NULL COMMENT '资质主表 ID',
  `cert_type` varchar(30) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL COMMENT '证书类型：WX_CERT_P12/ALI_PUBLIC/ALI_ROOT/APP_PUBLIC/UNIONPAY_PFX',
  `cert_path` varchar(512) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL COMMENT '证书服务器路径',
  `cert_serial_no` varchar(128) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '证书序列号',
  `expire_time` datetime DEFAULT NULL COMMENT '证书过期时间',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '上传时间',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_qual_id` (`qual_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 ROW_FORMAT=DYNAMIC COMMENT='商户资质证书表（1资质 N 证书）';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_pay_recon_diff`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_pay_recon_diff` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '主键',
  `recon_file_id` bigint NOT NULL COMMENT '对账文件 ID',
  `channel_code` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '渠道编码',
  `bill_date` date NOT NULL COMMENT '账单日期',
  `local_order_no` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '平台订单号',
  `channel_order_no` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '渠道订单号',
  `local_amount` bigint DEFAULT NULL COMMENT '平台金额(分)',
  `channel_amount` bigint DEFAULT NULL COMMENT '渠道金额(分)',
  `diff_type` varchar(16) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '差异类型 LOCAL_ONLY/CHANNEL_ONLY/AMOUNT_DIFF/STATUS_DIFF',
  `diff_amount` bigint DEFAULT NULL COMMENT '差异金额(分)',
  `handle_status` tinyint NOT NULL DEFAULT '0' COMMENT '0待处理 1已处理 2已忽略',
  `handle_user_id` bigint DEFAULT NULL COMMENT '处理人',
  `handle_time` datetime DEFAULT NULL COMMENT '处理时间',
  `handle_remark` varchar(512) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '处理备注',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_file` (`recon_file_id`),
  KEY `idx_status` (`handle_status`),
  KEY `idx_local_order` (`local_order_no`),
  KEY `idx_channel_order` (`channel_order_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='自研对账差异单';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_pay_recon_file`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_pay_recon_file` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '主键',
  `channel_code` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '渠道编码(如 WX/ALI/UNION)',
  `bill_date` date NOT NULL COMMENT '账单日期',
  `bill_type` varchar(16) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'TRADE' COMMENT '账单类型 TRADE/REFUND/SETTLE',
  `file_url` varchar(512) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '账单文件 URL',
  `file_md5` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '文件 MD5',
  `total_count` int NOT NULL DEFAULT '0' COMMENT '记录数',
  `total_amount` bigint NOT NULL DEFAULT '0' COMMENT '总金额(分)',
  `status` tinyint NOT NULL DEFAULT '0' COMMENT '0待拉取 1拉取中 2已落地 3比对中 4已比对 9失败',
  `error_msg` varchar(512) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '失败原因',
  `pull_time` datetime DEFAULT NULL COMMENT '拉取时间',
  `match_time` datetime DEFAULT NULL COMMENT '比对完成时间',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_channel_date_type` (`channel_code`,`bill_date`,`bill_type`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='自研对账文件登记表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_pay_recon_report`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_pay_recon_report` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '主键',
  `mch_no` varchar(32) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '商户号(NULL表示全平台)',
  `channel_code` varchar(32) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '渠道编码(NULL表示全渠道)',
  `bill_month` varchar(7) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '账单月份 yyyy-MM',
  `total_count` int NOT NULL DEFAULT '0' COMMENT '总笔数',
  `total_amount` bigint NOT NULL DEFAULT '0' COMMENT '总金额(分)',
  `success_count` int NOT NULL DEFAULT '0' COMMENT '成功笔数',
  `success_amount` bigint NOT NULL DEFAULT '0' COMMENT '成功金额(分)',
  `diff_count` int NOT NULL DEFAULT '0' COMMENT '差异笔数',
  `diff_amount` bigint NOT NULL DEFAULT '0' COMMENT '差异金额(分)',
  `report_url` varchar(512) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '报表导出URL',
  `gen_time` datetime NOT NULL COMMENT '生成时间',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_mch_channel_month` (`mch_no`,`channel_code`,`bill_month`),
  KEY `idx_month` (`bill_month`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='自研对账月度报表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_pay_refund_order`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_pay_refund_order` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `refund_no` varchar(64) NOT NULL COMMENT '退款幂等单号',
  `cashier_no` varchar(64) NOT NULL COMMENT '收银台订单号',
  `mall_order_no` varchar(64) NOT NULL COMMENT '商城订单号',
  `amount` decimal(18,2) NOT NULL COMMENT '退款金额',
  `reason` varchar(255) DEFAULT NULL COMMENT '退款原因',
  `status` varchar(20) NOT NULL COMMENT '退款状态',
  `channel_refund_no` varchar(128) DEFAULT NULL COMMENT '渠道退款单号',
  `failure_reason` varchar(500) DEFAULT NULL COMMENT '失败原因',
  `success_time` datetime DEFAULT NULL COMMENT '退款成功时间',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_refund_refund_no` (`refund_no`),
  KEY `idx_refund_cashier_no` (`cashier_no`),
  KEY `idx_refund_mall_order_no` (`mall_order_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='支付退款订单';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_pay_risk_blacklist`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_pay_risk_blacklist` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `list_type` tinyint NOT NULL COMMENT '名单类型：1-黑名单 2-白名单',
  `target_type` tinyint NOT NULL COMMENT '目标类型：1-用户ID 2-商户号 3-IP 4-银行卡号 5-手机号 6-身份证号',
  `target_value` varchar(128) NOT NULL COMMENT '目标取值',
  `biz_scope` tinyint NOT NULL DEFAULT '0' COMMENT '业务作用域：0-全部 1-收银台下单 2-退款 3-提现 4-转账',
  `reason` varchar(512) DEFAULT NULL COMMENT '加入原因',
  `risk_level` tinyint DEFAULT '1' COMMENT '风险等级：1-低 2-中 3-高',
  `effective_time` datetime DEFAULT NULL COMMENT '生效时间',
  `expire_time` datetime DEFAULT NULL COMMENT '失效时间（NULL 永久）',
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '状态：0-失效 1-生效',
  `operator_id` varchar(64) DEFAULT NULL COMMENT '操作人ID',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '逻辑删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_list_target` (`list_type`,`target_type`,`target_value`,`biz_scope`,`deleted`),
  KEY `idx_target_value` (`target_value`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='支付风控黑白名单';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_pay_risk_log`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_pay_risk_log` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `log_no` varchar(32) NOT NULL COMMENT '日志单号（唯一）',
  `biz_type` tinyint NOT NULL COMMENT '业务类型：1-收银台下单 2-退款 3-提现 4-转账 5-充值',
  `biz_no` varchar(64) DEFAULT NULL COMMENT '业务单号（订单号/退款号/提现号等）',
  `mch_no` varchar(32) DEFAULT NULL COMMENT '商户号',
  `user_id` varchar(64) DEFAULT NULL COMMENT '用户ID',
  `user_ip` varchar(64) DEFAULT NULL COMMENT '用户IP',
  `amount` decimal(20,2) DEFAULT NULL COMMENT '金额（元）',
  `pay_method` varchar(32) DEFAULT NULL COMMENT '支付方式编码',
  `hit_rule_nos` varchar(1024) DEFAULT NULL COMMENT '命中规则编号（英文逗号分隔）',
  `hit_rules_json` text COMMENT '命中规则明细（JSON 数组）',
  `risk_level` tinyint NOT NULL DEFAULT '0' COMMENT '风险等级：0-正常 1-低 2-中 3-高',
  `action` tinyint NOT NULL DEFAULT '1' COMMENT '最终动作：1-放行 2-拦截 3-告警 4-待审核',
  `review_status` tinyint NOT NULL DEFAULT '0' COMMENT '审核状态 0未提交 1待审核 2审核中 3通过 4驳回',
  `review_user_id` bigint DEFAULT NULL COMMENT '审核人',
  `review_time` datetime DEFAULT NULL COMMENT '审核时间',
  `review_remark` varchar(512) DEFAULT NULL COMMENT '审核备注',
  `reject_reason` varchar(512) DEFAULT NULL COMMENT '拦截/告警/审核原因',
  `audit_status` tinyint DEFAULT NULL COMMENT '审核状态：0-未审核 1-审核通过 2-审核驳回',
  `audit_result` varchar(512) DEFAULT NULL COMMENT '审核结果说明',
  `audit_operator` varchar(64) DEFAULT NULL COMMENT '审核人ID',
  `audit_time` datetime DEFAULT NULL COMMENT '审核时间',
  `extra_context` text COMMENT '扩展上下文（JSON）',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '逻辑删除：0-未删除 1-已删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_log_no` (`log_no`),
  KEY `idx_biz` (`biz_type`,`biz_no`),
  KEY `idx_mch` (`mch_no`,`create_time`),
  KEY `idx_user` (`user_id`,`create_time`),
  KEY `idx_action_level` (`action`,`risk_level`),
  KEY `idx_audit` (`audit_status`),
  KEY `idx_review_status` (`review_status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='支付风控拦截/告警日志';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_pay_risk_rule`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_pay_risk_rule` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `rule_no` varchar(32) NOT NULL COMMENT '规则编号（唯一，业务标识）',
  `rule_name` varchar(128) NOT NULL COMMENT '规则名称',
  `rule_type` tinyint NOT NULL DEFAULT '1' COMMENT '规则类型：1-金额限额 2-频次控制 3-黑白名单 4-自定义',
  `biz_type` tinyint NOT NULL DEFAULT '0' COMMENT '业务类型：0-全部 1-收银台下单 2-退款 3-提现 4-转账 5-充值',
  `scope` tinyint NOT NULL DEFAULT '1' COMMENT '作用域：1-全局 2-商户 3-用户 4-支付方式',
  `scope_value` varchar(128) DEFAULT NULL COMMENT '作用域取值（商户号/用户ID/支付方式编码等）',
  `trigger_field` varchar(64) DEFAULT NULL COMMENT '触发字段：amount/count/sumAmount/ip/userId 等',
  `operator` varchar(16) DEFAULT '>=' COMMENT '比较运算符：>=、<=、=、!=、IN、NOT_IN',
  `threshold_value` decimal(20,4) DEFAULT NULL COMMENT '阈值（金额单位元；频次为次数）',
  `time_window` int DEFAULT '0' COMMENT '时间窗口（秒）：0-不限 86400-1天 2592000-30天',
  `action` tinyint NOT NULL DEFAULT '1' COMMENT '命中动作：1-拦截 2-告警 3-人工审核 4-仅记录',
  `priority` int NOT NULL DEFAULT '100' COMMENT '规则优先级（数字越小优先级越高）',
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '状态：0-禁用 1-启用',
  `effective_time` datetime DEFAULT NULL COMMENT '生效时间',
  `expire_time` datetime DEFAULT NULL COMMENT '失效时间',
  `remark` varchar(512) DEFAULT NULL COMMENT '备注/说明',
  `creator_id` varchar(64) DEFAULT NULL COMMENT '创建人ID',
  `updater_id` varchar(64) DEFAULT NULL COMMENT '更新人ID',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '逻辑删除：0-未删除 1-已删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_rule_no` (`rule_no`),
  KEY `idx_rule_type_status` (`rule_type`,`status`),
  KEY `idx_biz_scope` (`biz_type`,`scope`,`scope_value`),
  KEY `idx_priority` (`priority`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='支付风控规则表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_pay_security`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_pay_security` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` varchar(32) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL COMMENT '用户ID',
  `pay_password` varchar(128) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '支付密码(BCrypt)',
  `real_name_encrypted` varchar(256) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '实名姓名密文',
  `id_card_encrypted` varchar(256) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '身份证号密文',
  `real_name_status` tinyint NOT NULL DEFAULT '0' COMMENT '实名认证状态：0-未认证；1-待审核；2-已认证；3-已拒绝',
  `real_name_time` datetime DEFAULT NULL COMMENT '实名认证通过时间',
  `pwd_error_count` int NOT NULL DEFAULT '0' COMMENT '密码错误次数',
  `pwd_lock_time` datetime DEFAULT NULL COMMENT '密码锁定时间',
  `last_modify_time` datetime DEFAULT NULL COMMENT '密码最后修改时间',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uk_user_id` (`user_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 ROW_FORMAT=DYNAMIC COMMENT='支付安全（支付密码/实名）';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_pay_transfer_money`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_pay_transfer_money` (
  `transfer_id` int NOT NULL AUTO_INCREMENT COMMENT '转账编号',
  `from_user` varchar(50) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL COMMENT '发起转帐或红包的人',
  `to_user` varchar(50) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL COMMENT '接收人',
  `send_time` datetime NOT NULL COMMENT '发送时间',
  `receive_time` datetime DEFAULT NULL COMMENT '收到时间',
  `transfer_money` decimal(10,2) NOT NULL COMMENT '转账金额',
  `transfer_status` tinyint NOT NULL DEFAULT '0' COMMENT '状态：0为处理中,1为已收到，2为过期',
  `transfer_txt` text CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci COMMENT '转账说明',
  `transfer_type` tinyint(1) NOT NULL DEFAULT '1' COMMENT '交易类型： 1 转帐 2红包',
  `transaction_number` varchar(50) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL COMMENT '交易单号',
  PRIMARY KEY (`transfer_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 ROW_FORMAT=DYNAMIC COMMENT='转帐，红包';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_pay_user_base`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_pay_user_base` (
  `user_id` varchar(18) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL COMMENT '用户id',
  `user_pay_passwd` varchar(100) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL DEFAULT '' COMMENT '支付确认密码',
  `ts` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '时间戳',
  PRIMARY KEY (`user_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 ROW_FORMAT=DYNAMIC COMMENT='用户基础信息表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_pay_user_resource`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_pay_user_resource` (
  `user_id` varchar(18) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL COMMENT '用户id',
  `user_money_pending_settlement` decimal(16,2) NOT NULL DEFAULT '0.00' COMMENT '待结算余额(废弃)',
  `user_money` decimal(16,2) unsigned NOT NULL DEFAULT '0.00' COMMENT '用户资金-预付款充值、转账、结算后的款项等等',
  `user_package_money` decimal(16,2) NOT NULL DEFAULT '0.00' COMMENT '用户充值套餐金额(废弃)',
  `user_money_frozen` decimal(16,2) NOT NULL DEFAULT '0.00' COMMENT '冻结余额',
  `user_recharge_card` decimal(16,2) NOT NULL DEFAULT '0.00' COMMENT '充值卡余额-只能从来购物',
  `is_delete` int DEFAULT NULL COMMENT '是否删除0:否;1:是',
  `ts` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '时间戳',
  PRIMARY KEY (`user_id`) USING BTREE,
  KEY `user_id` (`user_id`) USING BTREE,
  KEY `user_recharge_card` (`user_recharge_card`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 ROW_FORMAT=DYNAMIC COMMENT='用户资源表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_pay_withdraw_config`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_pay_withdraw_config` (
  `id` int unsigned NOT NULL AUTO_INCREMENT,
  `price` decimal(10,2) NOT NULL DEFAULT '50.00',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 ROW_FORMAT=DYNAMIC COMMENT='提现配置';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_permission_menu`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_permission_menu` (
  `id` int unsigned NOT NULL AUTO_INCREMENT COMMENT '主键',
  `pid` int unsigned NOT NULL DEFAULT '0' COMMENT '上级菜单',
  `type` char(2) NOT NULL DEFAULT '' COMMENT '权限类型: M=目录，C=菜单，A=按钮',
  `name` varchar(100) NOT NULL DEFAULT '' COMMENT '菜单名称',
  `icon` varchar(100) NOT NULL DEFAULT '' COMMENT '菜单图标',
  `sort` smallint unsigned NOT NULL DEFAULT '0' COMMENT '菜单排序',
  `perms` varchar(100) NOT NULL DEFAULT '' COMMENT '权限标识',
  `paths` varchar(100) NOT NULL DEFAULT '' COMMENT '路由地址',
  `component` varchar(200) NOT NULL DEFAULT '' COMMENT '前端组件',
  `selected` varchar(200) NOT NULL DEFAULT '' COMMENT '选中路径',
  `params` varchar(200) NOT NULL DEFAULT '' COMMENT '路由参数',
  `is_cache` tinyint unsigned NOT NULL DEFAULT '0' COMMENT '是否缓存: 0=否, 1=是',
  `is_show` tinyint unsigned NOT NULL DEFAULT '1' COMMENT '是否显示: 0=否, 1=是',
  `is_disable` tinyint unsigned NOT NULL DEFAULT '0' COMMENT '是否禁用: 0=否, 1=是',
  `create_time` int unsigned NOT NULL DEFAULT '0' COMMENT '创建时间',
  `update_time` int unsigned NOT NULL DEFAULT '0' COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_pid` (`pid`),
  KEY `idx_type_show_disable_sort` (`type`,`is_show`,`is_disable`,`sort` DESC,`id`),
  KEY `idx_pid_sort_id` (`pid`,`sort`,`id`),
  KEY `idx_pid_sort` (`pid`,`sort` DESC,`id` DESC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='系统菜单表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_permission_role`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_permission_role` (
  `id` int unsigned NOT NULL AUTO_INCREMENT,
  `name` varchar(16) NOT NULL DEFAULT '' COMMENT '名称',
  `desc` varchar(128) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL DEFAULT '' COMMENT '描述',
  `sort` int DEFAULT '0' COMMENT '排序',
  `create_time` int DEFAULT NULL COMMENT '创建时间',
  `update_time` int DEFAULT NULL COMMENT '更新时间',
  `delete_time` int DEFAULT NULL COMMENT '删除时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='角色表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_permission_role_menu`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_permission_role_menu` (
  `role_id` int unsigned NOT NULL DEFAULT '0' COMMENT '角色ID',
  `menu_id` int unsigned NOT NULL DEFAULT '0' COMMENT '菜单ID',
  PRIMARY KEY (`role_id`,`menu_id`) USING BTREE,
  KEY `idx_menu_id` (`menu_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='角色菜单关系表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_permission_user_role`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_permission_user_role` (
  `user_id` bigint NOT NULL COMMENT '会员员id',
  `role_id` int NOT NULL COMMENT '角色id',
  PRIMARY KEY (`user_id`,`role_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='角色关联表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_rider_evaluation`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
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
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_rider_info`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
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
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_rider_rider_base`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
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
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_rider_settlement`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
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
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_rider_withdraw`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
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
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_supplier_common_base`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
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
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_supplier_dealer_info`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
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
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_supplier_dealer_level_info`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_supplier_dealer_level_info` (
  `dealer_level_id` int unsigned NOT NULL AUTO_INCREMENT COMMENT '经销商等级id',
  `supplier_shop_id` int NOT NULL COMMENT '供应商店铺id',
  `dealer_level_name` varchar(100) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL COMMENT '经销商等级名称',
  `dealer_discount` int NOT NULL COMMENT '等级折扣',
  `dealer_level_sort` int NOT NULL DEFAULT '1' COMMENT '等级排序',
  `dealer_level_status` tinyint(1) NOT NULL DEFAULT '1' COMMENT '经销商等级状态  1正常 2删除',
  PRIMARY KEY (`dealer_level_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 ROW_FORMAT=DYNAMIC COMMENT='经销商等级';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_supplier_dealer_putaway_record`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
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
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_supplier_goods_base`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
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
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_supplier_goods_base_version`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
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
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_supplier_goods_common_detail`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_supplier_goods_common_detail` (
  `supplier_common_id` int NOT NULL AUTO_INCREMENT,
  `common_body` text CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL COMMENT '商品内容',
  `product_file` text CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci COMMENT '商品文件',
  PRIMARY KEY (`supplier_common_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 ROW_FORMAT=DYNAMIC COMMENT='供应商商品公共内容详情表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_supplier_goods_common_stock`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
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
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_supplier_goods_details_version`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
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
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_supplier_goods_image_version`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
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
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_supplier_goods_images`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
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
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_supplier_goods_version`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
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
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_supplier_goods_yiyatong`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
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
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_supplier_order_settlement`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
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
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_supplier_order_user_cart`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
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
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_supplier_proc_config`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
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
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_supplier_shop_base`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
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
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_supplier_shop_class_bind`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
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
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_supplier_shop_class_bind_tmp`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
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
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_supplier_shop_join_base`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
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
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_supplier_shop_join_pay`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
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
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_supplier_shop_level`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
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
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_supplier_shop_level_upgrade_record`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
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
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_supplier_shop_retention_money`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
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
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_supplier_upload_album`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
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
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_supplier_upload_base`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
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
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_supplier_yiyatong_cat`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_supplier_yiyatong_cat` (
  `id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '分类id',
  `name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '分类名称',
  `pid` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '父id',
  `level` int DEFAULT NULL COMMENT '等级',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci ROW_FORMAT=DYNAMIC COMMENT='贻亚通商品分类表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_supplier_yiyatong_msg_callback`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
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
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_sys_queue`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_sys_queue` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '队列ID',
  `name` varchar(128) NOT NULL DEFAULT '' COMMENT '队列名称',
  `module` varchar(64) NOT NULL DEFAULT 'system' COMMENT '所属模块',
  `status` varchar(20) NOT NULL DEFAULT 'pending' COMMENT '状态：pending/running/completed/failed/cancelled',
  `priority` varchar(20) NOT NULL DEFAULT 'medium' COMMENT '优先级：high/medium/low',
  `progress` int NOT NULL DEFAULT '0' COMMENT '进度0-100',
  `retry_count` int NOT NULL DEFAULT '0' COMMENT '重试次数',
  `max_retry` int NOT NULL DEFAULT '3' COMMENT '最大重试次数',
  `payload` text COMMENT '任务载荷JSON',
  `error_msg` varchar(1000) DEFAULT '' COMMENT '错误信息',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `started_at` datetime DEFAULT NULL COMMENT '开始执行时间',
  `finished_at` datetime DEFAULT NULL COMMENT '完成时间',
  PRIMARY KEY (`id`),
  KEY `idx_status` (`status`),
  KEY `idx_module` (`module`),
  KEY `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='系统队列表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_thirdparty_oa_config`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_thirdparty_oa_config` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `org_id` varchar(50) NOT NULL COMMENT '组织ID',
  `platform` varchar(50) NOT NULL COMMENT '平台名称 (dingtalk, wechat-work, feishu, yuque)',
  `config_name` varchar(100) NOT NULL COMMENT '配置名称',
  `enabled` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否启用 (0:禁用, 1:启用)',
  `config_json` text COMMENT '配置参数JSON',
  `description` varchar(500) DEFAULT NULL COMMENT '描述',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_by` varchar(50) DEFAULT NULL COMMENT '创建人',
  `update_by` varchar(50) DEFAULT NULL COMMENT '更新人',
  `deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否删除 (0:未删除, 1:已删除)',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_platform_org` (`platform`,`org_id`,`deleted`),
  KEY `idx_platform` (`platform`),
  KEY `idx_org_id` (`org_id`),
  KEY `idx_enabled` (`enabled`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='第三方集成配置表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_undo_log`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_undo_log` (
  `branch_id` bigint NOT NULL COMMENT '分支事务ID',
  `xid` varchar(100) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL COMMENT '全局事务ID',
  `context` varchar(128) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL COMMENT '上下文',
  `rollback_info` longblob NOT NULL COMMENT '回滚信息',
  `log_status` int NOT NULL COMMENT '状态，0正常，1全局已完成',
  `log_created` datetime(6) NOT NULL COMMENT '创建时间',
  `log_modified` datetime(6) NOT NULL COMMENT '修改时间',
  UNIQUE KEY `ux_undo_log` (`xid`,`branch_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 ROW_FORMAT=DYNAMIC COMMENT='AT transaction mode undo table';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_user_access`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_user_access` (
  `id` int NOT NULL AUTO_INCREMENT,
  `user_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '用户id',
  `user_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '用户名',
  `goods_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '商品id',
  `add_time` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '添加时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci ROW_FORMAT=DYNAMIC COMMENT='用户访问表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_user_address`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_user_address` (
  `user_address_id` int NOT NULL AUTO_INCREMENT,
  `user_id` varchar(18) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL COMMENT '所属店铺',
  `user_address_contact` varchar(50) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL COMMENT '所属联系地址',
  `user_address_province_id` int NOT NULL COMMENT '所属地址区',
  `user_address_city_id` int NOT NULL COMMENT '所属地址城市',
  `user_address_area_id` int NOT NULL COMMENT '所属地址区',
  `user_address_area` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL COMMENT '所在地区-字符串组合',
  `user_address_address` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL COMMENT '街道地址-不必重复填写地区',
  `user_address_phone` varchar(50) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '联系电话',
  `user_address_default` tinyint(1) NOT NULL DEFAULT '0' COMMENT '默认地址0不是1是',
  `user_address_time` datetime DEFAULT NULL,
  `update_time` datetime DEFAULT NULL COMMENT '修改时间',
  `ts` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '时间戳',
  `international_type` tinyint(1) DEFAULT '2' COMMENT '国际地址 1 是 2 否',
  `country_id` int DEFAULT '1' COMMENT '国家id',
  `country_name` varchar(100) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT '中国' COMMENT '国家名称',
  `international_dialing_code` varchar(50) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT '+86' COMMENT '国际区号',
  PRIMARY KEY (`user_address_id`) USING BTREE,
  KEY `user_id` (`user_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 ROW_FORMAT=DYNAMIC COMMENT='用户地址表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_user_bind_connect`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_user_bind_connect` (
  `bind_id` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '绑定标记：mobie/微信/支付宝openid  绑定类型+openid',
  `bind_type` tinyint NOT NULL COMMENT '绑定类型 1 :mobile 2:weixin ',
  `user_id` varchar(18) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '0' COMMENT '用户id',
  `bind_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '绑定时间',
  `bind_nickname` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '' COMMENT '第三方用户昵称',
  `bind_avator` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '第三方用户头像地址',
  `bind_openid` varchar(200) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL DEFAULT '' COMMENT '第三方用户身份认证编号',
  `bind_unionid` varchar(255) CHARACTER SET latin1 COLLATE latin1_swedish_ci DEFAULT NULL,
  `create_user` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '创建者',
  `is_auto_login` int DEFAULT '1' COMMENT '微信小程序是否自动登录 1是 2 否',
  `ts` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '时间戳',
  `official_account_openid` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '公众号openid',
  PRIMARY KEY (`bind_id`,`bind_type`) USING BTREE,
  KEY `user_id` (`user_id`) USING BTREE,
  KEY `bind_type` (`bind_type`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC COMMENT='用户绑定表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_user_business_info`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_user_business_info` (
  `id` int NOT NULL AUTO_INCREMENT,
  `tax_id` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '公司税号',
  `company_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '公司名称',
  `business_license` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '营业执照',
  `bank_card_no` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '银行卡号',
  `register_bank` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '开户行',
  `contact_name` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '联系人姓名',
  `contact_number` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '联系人电话',
  `company_province_id` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '公司地址_省',
  `company_city_id` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '公司地址_市',
  `company_district_id` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '公司地址_区',
  `company_detail_address` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '公司地址_详细地址',
  `mail_address` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '邮箱地址',
  `user_id` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '关联用户ID',
  `create_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `last_update_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '上次更新时间',
  `is_approved` int DEFAULT '0' COMMENT '审核状态:0-待审核,1-审核通过,2-审核不通过',
  `audit_comment` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '审核备注',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC COMMENT='企业用户信息表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_user_dept`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_user_dept` (
  `user_id` bigint NOT NULL DEFAULT '0' COMMENT '管理员id',
  `dept_id` int NOT NULL DEFAULT '0' COMMENT '部门id',
  `org_id` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`user_id`,`dept_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='部门关联表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_user_favorites_goods`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_user_favorites_goods` (
  `favorites_goods_id` int NOT NULL AUTO_INCREMENT COMMENT '商品收藏编号',
  `user_id` varbinary(18) NOT NULL COMMENT '用户id',
  `common_id` int unsigned NOT NULL COMMENT '商品id',
  `goods_id` int DEFAULT NULL COMMENT '货品ID',
  `favorites_goods_time` datetime NOT NULL COMMENT '收藏时间',
  `ts` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '时间戳',
  PRIMARY KEY (`favorites_goods_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 ROW_FORMAT=DYNAMIC COMMENT='收藏的商品';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_user_favorites_shop`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_user_favorites_shop` (
  `favorites_shop_id` int NOT NULL AUTO_INCREMENT,
  `user_id` varchar(18) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL COMMENT '用户ID',
  `shop_id` int NOT NULL COMMENT '店铺id',
  `shop_name` varchar(50) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL COMMENT '店铺名称',
  `shop_logo` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL COMMENT '店铺logo',
  `favorites_shop_time` datetime DEFAULT NULL,
  PRIMARY KEY (`favorites_shop_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 ROW_FORMAT=DYNAMIC COMMENT='收藏的店铺';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_user_info`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_user_info` (
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `user_mobile` varchar(50) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT '1' COMMENT '手机号码',
  `password` char(80) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL COMMENT '密码',
  `international_dialing_code` varchar(50) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT '+86' COMMENT '国际区号',
  `user_name` varchar(225) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '用户账号',
  `user_state` tinyint unsigned NOT NULL DEFAULT '1' COMMENT '状态(0:未激活,1:未认证,2:已认证,3:锁定4.多账号)',
  `user_integral` int DEFAULT '0' COMMENT '用户积分',
  `user_lastlogin_time` datetime DEFAULT NULL COMMENT '上次登录时间',
  `user_lastlogin_ip` varchar(15) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '上次登录IP',
  `user_count_login` int NOT NULL COMMENT '登录次数 ',
  `user_is_new` int NOT NULL COMMENT '是否为新用户 0 不是  1是',
  `user_nickname` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '用户昵称',
  `create_user` varchar(32) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_user` varchar(32) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '修改人',
  `update_time` datetime DEFAULT NULL COMMENT '修改时间',
  `ts` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '时间戳',
  `community_background` varchar(500) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '社区个人页背景图',
  `user_signature` varchar(500) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '社区个性签名',
  `is_business` int DEFAULT '0' COMMENT '是否企业:1-是;0-否',
  `follower_num` int DEFAULT '0' COMMENT '粉丝数',
  `follow_num` int DEFAULT '0' COMMENT '关注数',
  `grade_id` int DEFAULT NULL COMMENT '会员等级id',
  `member_create_time` datetime DEFAULT NULL COMMENT '成为会员时间',
  `register_terminal` varchar(32) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '注册终端: unimall/houcloud/hougeo（houmall 为更名前历史值，仍兼容识别）',
  `is_delete` tinyint(2) unsigned zerofill DEFAULT '00' COMMENT '是否删除0:否;1:是',
  `is_admin` tinyint DEFAULT '0' COMMENT '是否为最高管理员：1是0否\n',
  PRIMARY KEY (`user_id`) USING BTREE,
  UNIQUE KEY `user_id` (`user_id`) USING BTREE,
  KEY `user_mobile` (`user_mobile`) USING BTREE,
  KEY `user_name` (`user_name`(191)) USING BTREE,
  KEY `user_lastlogin_ip` (`user_lastlogin_ip`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 ROW_FORMAT=DYNAMIC COMMENT='用户基本信息表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_user_info_detail`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_user_info_detail` (
  `user_id` bigint NOT NULL COMMENT '用户id',
  `user_avatar` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '头像地址',
  `user_gender` tinyint unsigned NOT NULL DEFAULT '0' COMMENT '性别 0保密1男2女',
  `user_truename` varchar(20) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '真实姓名',
  `user_birth` varchar(10) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '生日(YYYY-MM-DD)',
  `user_reg_time` bigint unsigned NOT NULL DEFAULT '0' COMMENT '注册时间',
  `user_reg_ip` varchar(15) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '注册IP',
  `user_mobile_verify` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否已手机认证 0 否 1是',
  `user_idcard` varchar(30) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '身份证',
  `user_identity_type` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '证件类型  1身份证 2护照 3军官证',
  `user_identity_statu` int DEFAULT NULL COMMENT '0未审核1待审核2审核成功3审核失败',
  `user_identity_font_logo` varchar(500) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '证件照反面',
  `user_identity_face_logo` varchar(500) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '证件照正面',
  `user_identity_start_time` date DEFAULT NULL COMMENT '身份证有效期开始时间',
  `user_identity_end_time` date DEFAULT NULL COMMENT '身份证有效期结束时间',
  `user_parent_id` varchar(18) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT '0' COMMENT '上级推广员ID ',
  `user_platform` int DEFAULT NULL COMMENT '注册用户来源 1.pc;2.h5;3.小程序;4.app;5.后台',
  `create_user` varchar(32) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '创建者',
  `ts` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '时间戳',
  `user_bind_shop_id` int DEFAULT NULL COMMENT '用户绑定店铺id',
  `user_bind_store_id` int DEFAULT NULL COMMENT '用户绑定门店id',
  `validate_long_term` int DEFAULT '0' COMMENT '是否长期有效:1-是；0-否',
  PRIMARY KEY (`user_id`) USING BTREE,
  UNIQUE KEY `user_id` (`user_id`) USING BTREE,
  KEY `user_reg_time` (`user_reg_time`) USING BTREE,
  KEY `user_parent_id` (`user_parent_id`) USING BTREE,
  KEY `user_identity_statu` (`user_identity_statu`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 ROW_FORMAT=DYNAMIC COMMENT='用户详细信息表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_user_invite_record`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_user_invite_record` (
  `id` int unsigned NOT NULL AUTO_INCREMENT COMMENT '主键id',
  `user_id` varchar(18) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL COMMENT '用户id',
  `user_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '用户名',
  `user_mobile` varchar(20) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL COMMENT '用户手机号',
  `user_avatar` varchar(500) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL COMMENT '用户头像',
  `share_user_id` varchar(18) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL COMMENT '分享用户id',
  `relevance_id` varchar(100) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL COMMENT '关联id',
  `invite_time` datetime NOT NULL COMMENT '邀请时间',
  `invite_type` tinyint NOT NULL DEFAULT '1' COMMENT '邀请类型 1申请微客邀请   2首次分享购买邀请',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `id` (`id`) USING BTREE,
  KEY `user_id` (`user_id`) USING BTREE,
  KEY `parent_user_id` (`share_user_id`) USING BTREE,
  KEY `user_id_2` (`user_id`,`share_user_id`,`invite_type`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 ROW_FORMAT=DYNAMIC COMMENT='邀请记录表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_user_inviter`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_user_inviter` (
  `id` int NOT NULL AUTO_INCREMENT,
  `user_id` varchar(18) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '用户id',
  `one_inviter` varchar(18) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '一级邀请人',
  `two_inviter` varchar(18) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '二级邀请人',
  `is_superior` tinyint(1) DEFAULT '1' COMMENT '是否有上级',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci ROW_FORMAT=DYNAMIC COMMENT='用户邀请人表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_user_jobs`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_user_jobs` (
  `user_id` bigint NOT NULL COMMENT '会员id',
  `jobs_id` int NOT NULL COMMENT '岗位id',
  PRIMARY KEY (`user_id`,`jobs_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='岗位关联表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_user_live_statistics`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_user_live_statistics` (
  `id` int NOT NULL AUTO_INCREMENT,
  `user_id` varchar(220) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL COMMENT '用户id',
  `plat_form` int DEFAULT '0' COMMENT '端口   1.pc;2.h5;3.小程序; 4: app',
  `user_mobile` varchar(25) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '用户手机号',
  `login_time` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '登录时间',
  `ts` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '时间戳',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 ROW_FORMAT=DYNAMIC COMMENT='用户日活跃-每日第一次登录';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_user_log_action`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_user_log_action` (
  `log_id` int NOT NULL AUTO_INCREMENT COMMENT '日志id',
  `user_id` varchar(18) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT '0' COMMENT '玩家Id',
  `user_name` varchar(100) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT '' COMMENT '角色名称',
  `method` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '请求方法',
  `log_param` text CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci COMMENT '请求的参数',
  `operation` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '请求描述',
  `log_ip` varchar(20) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL DEFAULT '' COMMENT 'ip地址',
  `log_date` date NOT NULL COMMENT '日志日期',
  `log_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '记录时间',
  `ts` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '时间戳',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`log_id`) USING BTREE,
  KEY `log_date` (`log_date`) USING BTREE COMMENT '(null)',
  KEY `player_id` (`user_id`) USING BTREE COMMENT '(null)'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 ROW_FORMAT=DYNAMIC COMMENT='用户行为日志表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_user_organization`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_user_organization` (
  -- 🔴 id 必须是 varchar：UserOrganizationEntity 用 @TableId(type=ASSIGN_ID) 雪花ID（String），
  --    int AUTO_INCREMENT 会让 19 位雪花数字 Out of range（2026-10-28 绑定组织页「创建组织失败」实锤）。
  `id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `user_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '用户ID',
  `org_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '组织ID',
  `is_primary` int DEFAULT '0' COMMENT '是否主组织：1-是，0-否',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_org` (`user_id`,`org_id`),
  KEY `idx_user_org_user_id` (`user_id`),
  KEY `idx_user_org_org_id` (`org_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='用户组织关联表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_user_platform_message`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_user_platform_message` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` varchar(18) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '用户id',
  `message_content` varchar(511) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '消息内容',
  `is_reader` int DEFAULT '0' COMMENT '0:未读；1:已读',
  `creater` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '创建人',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '更新',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
  `message_title` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '标题内容',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC COMMENT='站内信';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_user_plus_equity`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_user_plus_equity` (
  `equity_id` int NOT NULL AUTO_INCREMENT,
  `equity_name` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '权益名称',
  `equity_type` int DEFAULT NULL COMMENT '1 PLUS专属折扣, 2 无门槛红包,3 红包券,4 送积分,5 积分倍率',
  `equity_value` varchar(500) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '权益值',
  `equity_addtime` datetime DEFAULT NULL COMMENT '权益添加时间',
  `equity_status` int DEFAULT NULL COMMENT '权益状态 0 关闭  1 开启',
  `red_effective` int DEFAULT NULL COMMENT '无门槛红包有效时间',
  `red_provide` int DEFAULT NULL COMMENT '红包发放时间  几号',
  `discount_limit` decimal(10,2) DEFAULT NULL COMMENT '每月最多可抵扣额度',
  PRIMARY KEY (`equity_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 ROW_FORMAT=DYNAMIC COMMENT='plus会员权益表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_user_plus_equity_temp`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_user_plus_equity_temp` (
  `equity_id` int NOT NULL AUTO_INCREMENT,
  `equity_name` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '权益名称',
  `equity_type` int DEFAULT NULL COMMENT '1 PLUS专属折扣, 2 无门槛红包,3 红包券,4 送积分,5 积分倍率',
  `equity_value` varchar(500) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '权益值',
  `equity_addtime` datetime DEFAULT NULL COMMENT '权益添加时间',
  `equity_status` int DEFAULT NULL COMMENT '权益状态 0 关闭  1 开启',
  `red_effective` int DEFAULT NULL COMMENT '无门槛红包有效时间',
  `red_provide` int DEFAULT NULL COMMENT '红包发放时间  几号',
  `discount_limit` decimal(10,2) DEFAULT NULL COMMENT '每月最多可抵扣额度',
  PRIMARY KEY (`equity_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 ROW_FORMAT=DYNAMIC COMMENT='plus会员权益表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_user_plus_info`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_user_plus_info` (
  `plus_id` int NOT NULL AUTO_INCREMENT,
  `user_id` varchar(18) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '用户id',
  `thali_type` int DEFAULT NULL COMMENT '套餐类型 1 月卡 2 季卡 3 年卡',
  `thali_starttime` datetime DEFAULT NULL COMMENT '套餐开始时间',
  `thali_endtime` datetime DEFAULT NULL COMMENT '套餐结束时间',
  `used_amount` decimal(10,2) DEFAULT NULL COMMENT '当月已用额度',
  `total_amount` decimal(10,2) DEFAULT NULL COMMENT '已优惠总额度',
  `creat_time` datetime DEFAULT NULL COMMENT '创建时间',
  `earn_points` bigint DEFAULT NULL COMMENT '获取积分',
  `thali_status` int DEFAULT '1' COMMENT '会员状态 0 过期 1 正常',
  `renewal_display` int DEFAULT '0' COMMENT '是否显示续费按钮 0 不显示 1 显示(暂时废弃)',
  `red_grantnum` int DEFAULT '0' COMMENT '剩余红包发放次数',
  `plus_equity` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '权益id',
  `red_grantmonth` int DEFAULT NULL COMMENT '上次红包发放月份',
  `expiration_notice` int DEFAULT '0' COMMENT '续费短信是否发送 0 未发送 1 已发送',
  PRIMARY KEY (`plus_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_user_plus_order`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_user_plus_order` (
  `plus_orderid` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '支付单号',
  `user_id` varchar(18) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '用户id',
  `pay_price` decimal(10,2) DEFAULT NULL COMMENT '支付金额',
  `pay_type` int DEFAULT NULL COMMENT '支付方式 1-余额支付，2-购物卡，3-微信支付，4 支付宝 5 平台赠送',
  `start_time` datetime DEFAULT NULL COMMENT '生效时间',
  `end_time` datetime DEFAULT NULL COMMENT '到期时间',
  `thali_type` int DEFAULT NULL COMMENT '套餐类型 1 月卡 2 季卡 3 年卡',
  `creat_time` datetime DEFAULT NULL COMMENT '支付时间',
  `plus_equity` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'plus权益',
  `is_pay` int DEFAULT '1' COMMENT '是否已支付 0 否  1 是',
  PRIMARY KEY (`plus_orderid`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC COMMENT='plus会员充值订单表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_user_plus_renew`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_user_plus_renew` (
  `renew_id` int NOT NULL AUTO_INCREMENT,
  `thali_type` int DEFAULT NULL COMMENT '套餐类型 1 月卡 2 季卡 3 年卡',
  `user_id` varchar(18) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '用户id',
  `renew_starttime` datetime DEFAULT NULL COMMENT '续费开始时间',
  `renew_endtime` datetime DEFAULT NULL COMMENT '续费结束时间',
  `creat_time` datetime DEFAULT NULL COMMENT '创建时间',
  `plus_equity` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '权益id',
  `renew_status` int DEFAULT NULL COMMENT '状态 0 未生效 1 已生效',
  `plus_orderid` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '充值订单',
  PRIMARY KEY (`renew_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_user_points_log`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_user_points_log` (
  `points_log_id` int NOT NULL AUTO_INCREMENT,
  `points_log_type` tinyint(1) NOT NULL DEFAULT '1' COMMENT '类型 1获取积分2积分消费',
  `class_id` tinyint(1) NOT NULL COMMENT '积分类型1.会员注册,2.会员登录3.评价4.购买商品5.6.管理员操作7.积分换购商品8.积分兑换代金券9退货',
  `user_id` varchar(50) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL COMMENT '会员编号',
  `points_log_points` int NOT NULL DEFAULT '0' COMMENT '积分变更',
  `freeze_points` int NOT NULL DEFAULT '0' COMMENT '冻结积分',
  `points_log_time` datetime NOT NULL COMMENT '创建时间',
  `points_log_desc` varchar(100) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '操作 会员注册、会员登录、会员购买、换购商品，平台操作',
  `points_log_operate` varchar(100) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '描述',
  `points_log_flag` varchar(20) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL COMMENT '标记',
  `order_id` varchar(50) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '订单号-确认收货',
  `ts` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '时间戳',
  PRIMARY KEY (`points_log_id`) USING BTREE,
  KEY `log_user_id` (`user_id`) USING BTREE COMMENT '用户id索引'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 ROW_FORMAT=DYNAMIC COMMENT='会员积分日志表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_user_role_relation`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_user_role_relation` (
  `user_id` varchar(64) NOT NULL DEFAULT '' COMMENT '用户ID',
  `role_id` int unsigned NOT NULL DEFAULT '0' COMMENT '角色ID',
  PRIMARY KEY (`user_id`,`role_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='用户角色关系表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Ucenter member menu permission domain (owned by userms)
--

CREATE TABLE IF NOT EXISTS `hou_ucenter_space` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `owner_user_id` BIGINT NOT NULL,
  `name` VARCHAR(100) NOT NULL,
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '1=active, 0=closed',
  `version` BIGINT NOT NULL DEFAULT 1,
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `close_time` DATETIME DEFAULT NULL,
  `active_owner_user_id` BIGINT GENERATED ALWAYS AS (IF(`status` = 1, `owner_user_id`, NULL)) STORED,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_ucenter_active_space_owner` (`active_owner_user_id`),
  KEY `idx_ucenter_space_owner` (`owner_user_id`),
  KEY `idx_ucenter_space_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='Ucenter permission space';

CREATE TABLE IF NOT EXISTS `hou_ucenter_space_member` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `space_id` BIGINT NOT NULL,
  `user_id` BIGINT NOT NULL,
  `role` VARCHAR(16) NOT NULL COMMENT 'OWNER or MEMBER',
  `status` VARCHAR(16) NOT NULL DEFAULT 'ACTIVE' COMMENT 'ACTIVE or LEFT',
  `joined_by` BIGINT DEFAULT NULL,
  `join_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `leave_time` DATETIME DEFAULT NULL,
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `active_user_id` BIGINT GENERATED ALWAYS AS (IF(`status` = 'ACTIVE', `user_id`, NULL)) STORED,
  `active_owner_space_id` BIGINT GENERATED ALWAYS AS (IF(`status` = 'ACTIVE' AND `role` = 'OWNER', `space_id`, NULL)) STORED,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_ucenter_active_user` (`active_user_id`),
  UNIQUE KEY `uk_ucenter_active_owner` (`active_owner_space_id`),
  KEY `idx_ucenter_member_space` (`space_id`, `status`),
  KEY `idx_ucenter_member_user` (`user_id`),
  CONSTRAINT `fk_ucenter_member_space` FOREIGN KEY (`space_id`) REFERENCES `hou_ucenter_space` (`id`) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='Ucenter active and historical memberships';

CREATE TABLE IF NOT EXISTS `hou_ucenter_menu` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `code` VARCHAR(100) NOT NULL,
  `parent_code` VARCHAR(100) DEFAULT NULL,
  `name_key` VARCHAR(100) NOT NULL,
  `route_path` VARCHAR(160) NOT NULL,
  `icon` VARCHAR(80) DEFAULT NULL,
  `sort` INT NOT NULL DEFAULT 0,
  `mandatory` TINYINT NOT NULL DEFAULT 0,
  `owner_only` TINYINT NOT NULL DEFAULT 0,
  `is_show` TINYINT NOT NULL DEFAULT 1,
  `is_enabled` TINYINT NOT NULL DEFAULT 1,
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_ucenter_menu_code` (`code`),
  KEY `idx_ucenter_menu_parent_sort` (`parent_code`, `sort`),
  CONSTRAINT `fk_ucenter_menu_parent` FOREIGN KEY (`parent_code`) REFERENCES `hou_ucenter_menu` (`code`) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='Ucenter menu catalog';

CREATE TABLE IF NOT EXISTS `hou_ucenter_member_menu` (
  `member_id` BIGINT NOT NULL,
  `menu_id` BIGINT NOT NULL,
  `granted_by` BIGINT NOT NULL,
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`member_id`, `menu_id`),
  KEY `idx_ucenter_member_menu_menu` (`menu_id`),
  CONSTRAINT `fk_ucenter_member_menu_member` FOREIGN KEY (`member_id`) REFERENCES `hou_ucenter_space_member` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_ucenter_member_menu_menu` FOREIGN KEY (`menu_id`) REFERENCES `hou_ucenter_menu` (`id`) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='Explicit menu grants for normal members';

CREATE TABLE IF NOT EXISTS `hou_ucenter_invitation` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `space_id` BIGINT NOT NULL,
  `inviter_user_id` BIGINT NOT NULL,
  `invitee_user_id` BIGINT NOT NULL,
  `masked_mobile` CHAR(4) NOT NULL,
  `token_digest` CHAR(64) NOT NULL,
  `status` VARCHAR(16) NOT NULL DEFAULT 'PENDING' COMMENT 'PENDING, ACCEPTED, REJECTED, CANCELLED, EXPIRED',
  `expires_at` DATETIME NOT NULL,
  `accepted_at` DATETIME DEFAULT NULL,
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `pending_invitee_user_id` BIGINT GENERATED ALWAYS AS (IF(`status` = 'PENDING', `invitee_user_id`, NULL)) STORED,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_ucenter_invitation_token` (`token_digest`),
  UNIQUE KEY `uk_ucenter_pending_invitee` (`space_id`, `pending_invitee_user_id`),
  KEY `idx_ucenter_invitee_status` (`invitee_user_id`, `status`, `expires_at`),
  CONSTRAINT `fk_ucenter_invitation_space` FOREIGN KEY (`space_id`) REFERENCES `hou_ucenter_space` (`id`) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='Ucenter invitation lifecycle';

CREATE TABLE IF NOT EXISTS `hou_ucenter_invitation_menu` (
  `invitation_id` BIGINT NOT NULL,
  `menu_id` BIGINT NOT NULL,
  PRIMARY KEY (`invitation_id`, `menu_id`),
  KEY `idx_ucenter_invitation_menu_menu` (`menu_id`),
  CONSTRAINT `fk_ucenter_invitation_menu_invitation` FOREIGN KEY (`invitation_id`) REFERENCES `hou_ucenter_invitation` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_ucenter_invitation_menu_menu` FOREIGN KEY (`menu_id`) REFERENCES `hou_ucenter_menu` (`id`) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='Proposed invitation menu grants';

CREATE TABLE IF NOT EXISTS `hou_ucenter_permission_audit` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `space_id` BIGINT NOT NULL,
  `actor_user_id` BIGINT NOT NULL,
  `target_member_id` BIGINT DEFAULT NULL,
  `event_type` VARCHAR(48) NOT NULL,
  `before_permissions` JSON DEFAULT NULL,
  `after_permissions` JSON DEFAULT NULL,
  `correlation_id` VARCHAR(64) NOT NULL,
  `result` VARCHAR(16) NOT NULL,
  `client_ip` VARCHAR(45) DEFAULT NULL,
  `user_agent` VARCHAR(255) DEFAULT NULL,
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_ucenter_audit_space_time` (`space_id`, `create_time`),
  KEY `idx_ucenter_audit_actor_time` (`actor_user_id`, `create_time`),
  KEY `idx_ucenter_audit_target` (`target_member_id`),
  CONSTRAINT `fk_ucenter_audit_space` FOREIGN KEY (`space_id`) REFERENCES `hou_ucenter_space` (`id`) ON DELETE RESTRICT,
  CONSTRAINT `fk_ucenter_audit_target` FOREIGN KEY (`target_member_id`) REFERENCES `hou_ucenter_space_member` (`id`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='Sanitized ucenter permission audit';

--
-- Table structure for table `hou_user_seller_base`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_user_seller_base` (
  `seller_id` int NOT NULL AUTO_INCREMENT COMMENT '卖家id',
  `seller_name` varchar(50) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL COMMENT '卖家用户名',
  `shop_id` int unsigned NOT NULL COMMENT '店铺ID',
  `user_id` varchar(18) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL COMMENT '会员ID',
  `rights_group_id` int unsigned NOT NULL COMMENT '卖家组ID',
  `seller_is_admin` tinyint unsigned NOT NULL COMMENT '是否管理员(0-不是 1-是)',
  `seller_login_time` datetime NOT NULL COMMENT '最后登录时间',
  `seller_group_id` int unsigned NOT NULL COMMENT '卖家组ID',
  `shop_type` tinyint(1) DEFAULT '1' COMMENT '1卖家店铺 2供应商店铺 3 品牌店铺',
  PRIMARY KEY (`seller_id`) USING BTREE,
  KEY `user_id` (`user_id`) USING BTREE,
  KEY `seller_group_id` (`seller_group_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 ROW_FORMAT=DYNAMIC COMMENT='卖家用户表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_user_seller_group`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_user_seller_group` (
  `group_id` int unsigned NOT NULL AUTO_INCREMENT COMMENT '卖家组编号',
  `group_name` varchar(50) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL COMMENT '组名',
  `limits` text CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL COMMENT '权限',
  `smt_limits` text CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL COMMENT '消息权限范围',
  `shop_id` int unsigned NOT NULL COMMENT '店铺编号',
  PRIMARY KEY (`group_id`) USING BTREE,
  KEY `group_id` (`group_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 ROW_FORMAT=DYNAMIC COMMENT='卖家用户组表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_user_tag_info`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_user_tag_info` (
  `tag_id` int NOT NULL AUTO_INCREMENT COMMENT '会员标签id',
  `tag_name` varchar(250) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL COMMENT '会员标签name',
  `tag_weight` int NOT NULL COMMENT '会员标签权重',
  `tag_image` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '会员标签图片',
  `tag_describe` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '会员标签描述',
  `tag_add_data` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '会员标签新增时间',
  `tag_isactive` int NOT NULL DEFAULT '1' COMMENT '会员标签00删除 01正常，默认正常',
  `tag_upd_data` timestamp NULL DEFAULT NULL COMMENT '会员标签更新时间',
  `date` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`tag_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_user_upload_album`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_user_upload_album` (
  `album_id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '商品图片Id',
  `album_name` varchar(100) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL DEFAULT '' COMMENT '商品图片地址',
  `album_cover` varchar(100) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL DEFAULT '' COMMENT '封面',
  `album_desc` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL DEFAULT '' COMMENT '描述',
  `album_num` mediumint unsigned NOT NULL DEFAULT '0' COMMENT '内容数量',
  `album_is_default` tinyint unsigned NOT NULL DEFAULT '0' COMMENT '默认相册，1是，0否',
  `album_displayorder` smallint NOT NULL DEFAULT '255' COMMENT '排序',
  `album_type` enum('video','other','image') CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL DEFAULT 'image' COMMENT '附件册',
  `user_id` varchar(18) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL DEFAULT '0' COMMENT '所属用户id',
  `shop_id` int unsigned NOT NULL DEFAULT '0',
  PRIMARY KEY (`album_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 ROW_FORMAT=DYNAMIC COMMENT='用户相册表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_user_upload_base`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_user_upload_base` (
  `upload_id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '商品图片Id',
  `album_id` bigint NOT NULL,
  `user_id` varchar(18) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL COMMENT '用户id',
  `shop_id` int unsigned NOT NULL DEFAULT '0' COMMENT '店铺id',
  `upload_url_prefix` varchar(500) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT '',
  `upload_path` varchar(500) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL DEFAULT '',
  `upload_type` enum('video','other','image') CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT 'image' COMMENT 'image|video|',
  `upload_size` int DEFAULT NULL COMMENT '原文件大小',
  `upload_name` text CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci COMMENT '附件标题',
  `upload_time` int DEFAULT NULL COMMENT '附件日期',
  PRIMARY KEY (`upload_id`) USING BTREE,
  KEY `album_id` (`user_id`,`album_id`,`upload_type`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 ROW_FORMAT=DYNAMIC COMMENT='用户附件表-图片、视频';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_user_vk`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_user_vk` (
  `id` int NOT NULL AUTO_INCREMENT,
  `user_id` varchar(18) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL COMMENT '用户id',
  `user_name` varchar(50) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL COMMENT '用户名',
  `real_name` varchar(50) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '用户真实姓名',
  `mobile` varchar(50) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '手机号',
  `tg_num` int NOT NULL DEFAULT '0' COMMENT '推广人数（下级微客数）',
  `share_num` int NOT NULL DEFAULT '0' COMMENT '分享人数',
  `source` varchar(20) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT '1' COMMENT '来源  1平台自己添加  2.通过成为微客条件',
  `total_tg_commission` varchar(20) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT '0.00' COMMENT '推广佣金总额',
  `total_share_commission` varchar(20) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT '0.00' COMMENT '分享佣金总额',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `withdrawn_commission` varchar(20) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT '0.00' COMMENT '已提现佣金',
  `pending_withdrawn_commission` varchar(20) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT '0.00' COMMENT '待提现佣金',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `id` (`id`) USING BTREE,
  UNIQUE KEY `user_id` (`user_id`) USING BTREE,
  KEY `user_id_2` (`user_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 ROW_FORMAT=DYNAMIC COMMENT='店铺微客会员表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_user_vk_set`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_user_vk_set` (
  `id` int unsigned NOT NULL AUTO_INCREMENT COMMENT 'id',
  `vk_title` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '顶部标题',
  `vk_image` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '顶部图片',
  `apply_type` tinyint(1) NOT NULL DEFAULT '1' COMMENT '申请条件  1注册成功  2注册成功+购买礼包商品',
  `poster_image` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT '' COMMENT '海报图片',
  `vk_state` tinyint(1) NOT NULL DEFAULT '1' COMMENT '是否开启 1开启 2关闭',
  `vk_notice` text CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci COMMENT '微客须知',
  `fixed_rewards` decimal(10,2) DEFAULT NULL COMMENT '固定奖励',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 ROW_FORMAT=DYNAMIC COMMENT='微客配置表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_website_lead`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_website_lead` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `name` varchar(40) NOT NULL DEFAULT '',
  `phone` varchar(20) NOT NULL DEFAULT '',
  `company` varchar(80) NOT NULL DEFAULT '',
  `remark` varchar(300) NOT NULL DEFAULT '',
  `source` varchar(32) NOT NULL DEFAULT 'case_briefing',
  `page_key` varchar(80) NOT NULL DEFAULT '',
  `page_title` varchar(200) NOT NULL DEFAULT '',
  `page_url` varchar(500) NOT NULL DEFAULT '',
  `ip` varchar(64) NOT NULL DEFAULT '',
  `status` tinyint NOT NULL DEFAULT '0',
  `follow_remark` varchar(500) NOT NULL DEFAULT '',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_website_lead_phone` (`phone`),
  KEY `idx_website_lead_source` (`source`),
  KEY `idx_website_lead_status` (`status`),
  KEY `idx_website_lead_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_workflow_application`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_workflow_application` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `process_key` varchar(64) NOT NULL COMMENT '流程定义Key',
  `process_instance_id` varchar(64) DEFAULT NULL COMMENT '流程实例ID',
  `approval_number` varchar(50) NOT NULL COMMENT '审批编号',
  `applicant_id` varchar(64) NOT NULL COMMENT '申请人ID',
  `applicant_name` varchar(100) NOT NULL COMMENT '申请人姓名',
  `applicant_department` varchar(100) DEFAULT NULL COMMENT '申请人部门',
  `title` varchar(200) NOT NULL COMMENT '申请标题',
  `content` text COMMENT '申请内容',
  `status` varchar(20) DEFAULT 'DRAFT' COMMENT '状态: DRAFT,SUBMITTED,APPROVED,REJECTED,CANCELLED',
  `submit_time` datetime DEFAULT NULL COMMENT '提交时间',
  `complete_time` datetime DEFAULT NULL COMMENT '完成时间',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `approval_number` (`approval_number`),
  KEY `idx_applicant_id` (`applicant_id`),
  KEY `idx_process_instance_id` (`process_instance_id`),
  KEY `idx_approval_number` (`approval_number`),
  KEY `idx_status` (`status`),
  KEY `idx_submit_time` (`submit_time`),
  KEY `idx_complete_time` (`complete_time`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='工作流申请主表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_workflow_approval_record`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_workflow_approval_record` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `application_id` bigint NOT NULL COMMENT '申请ID',
  `task_id` varchar(64) NOT NULL COMMENT '任务ID',
  `approver_id` varchar(64) NOT NULL COMMENT '审批人ID',
  `approver_name` varchar(100) NOT NULL COMMENT '审批人姓名',
  `approval_result` varchar(20) NOT NULL COMMENT '审批结果: APPROVED,REJECTED',
  `approval_comment` text COMMENT '审批意见',
  `approval_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '审批时间',
  PRIMARY KEY (`id`),
  KEY `idx_application_id` (`application_id`),
  KEY `idx_task_id` (`task_id`),
  KEY `idx_approver_id` (`approver_id`),
  KEY `idx_approval_time` (`approval_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='流程审批记录表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_workflow_attachment`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_workflow_attachment` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `application_id` bigint NOT NULL COMMENT '申请ID',
  `file_name` varchar(200) NOT NULL COMMENT '文件名',
  `file_path` varchar(500) NOT NULL COMMENT '文件路径',
  `file_size` bigint DEFAULT NULL COMMENT '文件大小',
  `file_type` varchar(50) DEFAULT NULL COMMENT '文件类型',
  `upload_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '上传时间',
  `uploader_id` varchar(64) DEFAULT NULL COMMENT '上传人ID',
  `uploader_name` varchar(100) DEFAULT NULL COMMENT '上传人姓名',
  PRIMARY KEY (`id`),
  KEY `idx_application_id` (`application_id`),
  KEY `idx_upload_time` (`upload_time`),
  KEY `idx_uploader_id` (`uploader_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='流程附件表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_workflow_cc_recipient`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_workflow_cc_recipient` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `application_id` bigint NOT NULL COMMENT '申请ID',
  `user_id` varchar(64) NOT NULL COMMENT '用户ID',
  `user_name` varchar(100) NOT NULL COMMENT '用户姓名',
  `type` varchar(20) DEFAULT 'FIXED' COMMENT '类型: FIXED,AD_HOC',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_application_id` (`application_id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_type` (`type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='流程抄送人表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_workflow_delegation`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_workflow_delegation` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `original_assignee_id` varchar(64) NOT NULL COMMENT '原审批人ID',
  `delegated_assignee_id` varchar(64) NOT NULL COMMENT '委托审批人ID',
  `task_id` varchar(64) DEFAULT NULL COMMENT '任务ID',
  `application_id` bigint DEFAULT NULL COMMENT '申请ID',
  `start_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '开始时间',
  `end_time` datetime DEFAULT NULL COMMENT '结束时间',
  `status` varchar(20) DEFAULT 'ACTIVE' COMMENT '状态: ACTIVE,COMPLETED,CANCELLED',
  `reason` varchar(200) DEFAULT NULL COMMENT '委托原因',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_original_assignee_id` (`original_assignee_id`),
  KEY `idx_delegated_assignee_id` (`delegated_assignee_id`),
  KEY `idx_task_id` (`task_id`),
  KEY `idx_application_id` (`application_id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='流程委托关系表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_workflow_form_data`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_workflow_form_data` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `application_id` bigint NOT NULL COMMENT '申请ID',
  `form_key` varchar(100) NOT NULL COMMENT '表单Key',
  `form_data` json DEFAULT NULL COMMENT '表单数据',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_application_id` (`application_id`),
  KEY `idx_form_key` (`form_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='流程表单数据表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_workflow_form_definition`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_workflow_form_definition` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `form_key` varchar(100) NOT NULL COMMENT '表单Key',
  `form_name` varchar(100) NOT NULL COMMENT '表单名称',
  `form_schema` json DEFAULT NULL COMMENT '表单结构定义',
  `form_config` json DEFAULT NULL COMMENT '表单配置',
  `version` varchar(20) DEFAULT '1.0' COMMENT '表单版本',
  `is_active` tinyint(1) DEFAULT '1' COMMENT '是否启用',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `form_key` (`form_key`),
  KEY `idx_is_active` (`is_active`),
  KEY `idx_version` (`version`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='流程表单定义表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_workflow_node_config`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_workflow_node_config` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `process_key` varchar(64) NOT NULL COMMENT '流程Key',
  `node_id` varchar(100) NOT NULL COMMENT '节点ID',
  `node_name` varchar(100) NOT NULL COMMENT '节点名称',
  `node_type` varchar(50) NOT NULL COMMENT '节点类型: USER_TASK,SERVICE_TASK,GATEWAY',
  `assignee_type` varchar(50) DEFAULT NULL COMMENT '分配类型: USER,GROUP,EXPRESSION',
  `assignee_value` varchar(200) DEFAULT NULL COMMENT '分配值',
  `form_key` varchar(100) DEFAULT NULL COMMENT '关联表单Key',
  `is_required` tinyint(1) DEFAULT '1' COMMENT '是否必填',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_process_node` (`process_key`,`node_id`),
  KEY `idx_process_key` (`process_key`),
  KEY `idx_node_type` (`node_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='流程节点配置表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_workflow_notification`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_workflow_notification` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `application_id` bigint NOT NULL COMMENT '申请ID',
  `task_id` varchar(64) DEFAULT NULL COMMENT '任务ID',
  `receiver_id` varchar(64) NOT NULL COMMENT '接收人ID',
  `receiver_name` varchar(100) NOT NULL COMMENT '接收人姓名',
  `notification_type` varchar(50) NOT NULL COMMENT '通知类型: EMAIL,SMS,SYSTEM',
  `notification_content` text COMMENT '通知内容',
  `send_status` varchar(20) DEFAULT 'PENDING' COMMENT '发送状态: PENDING,SENT,FAILED',
  `send_time` datetime DEFAULT NULL COMMENT '发送时间',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_application_id` (`application_id`),
  KEY `idx_task_id` (`task_id`),
  KEY `idx_receiver_id` (`receiver_id`),
  KEY `idx_send_status` (`send_status`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='流程通知记录表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_workflow_process_config`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_workflow_process_config` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `process_key` varchar(64) NOT NULL COMMENT '流程Key',
  `process_name` varchar(100) NOT NULL COMMENT '流程名称',
  `process_category` varchar(50) DEFAULT NULL COMMENT '流程分类',
  `description` text COMMENT '流程描述',
  `icon` varchar(100) DEFAULT NULL COMMENT '流程图标',
  `print_format` varchar(20) DEFAULT 'DEFAULT' COMMENT '打印格式: DEFAULT,CUSTOM',
  `is_active` tinyint(1) DEFAULT '1' COMMENT '是否启用',
  `sort_order` int DEFAULT '0' COMMENT '排序',
  `scope_type` varchar(20) DEFAULT 'ALL' COMMENT '可见范围类型: ALL,ORGANIZATION,DEPARTMENT',
  `scope_id` varchar(64) DEFAULT NULL COMMENT '可见范围ID',
  `scope_name` varchar(100) DEFAULT NULL COMMENT '可见范围名称',
  `notification_enabled` tinyint(1) DEFAULT '1' COMMENT '是否启用通知',
  `approver_empty_action` varchar(20) DEFAULT 'AUTO_APPROVE' COMMENT '审批人节点为空时处理方式: AUTO_APPROVE,TRANSFER',
  `approver_leave_action` varchar(20) DEFAULT 'REMIND_ADMIN' COMMENT '审批人离职时处理方式: REMIND_ADMIN,AUTO_HANDOVER',
  `handler_empty_action` varchar(20) DEFAULT 'AUTO_APPROVE' COMMENT '办理人节点为空时处理方式: AUTO_APPROVE,TRANSFER',
  `handler_leave_action` varchar(20) DEFAULT 'REMIND_ADMIN' COMMENT '办理人离职时处理方式: REMIND_ADMIN,AUTO_HANDOVER',
  `duplicate_approver_strategy` varchar(20) DEFAULT 'FIRST_NODE_ONLY' COMMENT '重复审批人策略: FIRST_NODE_ONLY,CONTINUOUS_ONLY,ALL_NODES',
  `allow_proxy_submission` tinyint(1) DEFAULT '0' COMMENT '允许代他人提交申请',
  `prevent_approver_modification` tinyint(1) DEFAULT '0' COMMENT '员工不可修改固定审批人',
  `prevent_cc_deletion` tinyint(1) DEFAULT '0' COMMENT '员工不可删除固定抄送人',
  `prevent_handler_modification` tinyint(1) DEFAULT '1' COMMENT '员工不可修改固定办理人',
  `allow_revocation_after_approval` tinyint(1) DEFAULT '0' COMMENT '通过后允许撤销',
  `allow_add_ad_hoc_approver` tinyint(1) DEFAULT '1' COMMENT '允许增加临时审批人',
  `require_approval_comment` tinyint(1) DEFAULT '0' COMMENT '审批时必须填写意见',
  `require_handling_comment` tinyint(1) DEFAULT '0' COMMENT '办理时必须填写意见',
  `require_e_signature` tinyint(1) DEFAULT '0' COMMENT '审批时需要电子签名',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `process_key` (`process_key`),
  KEY `idx_process_category` (`process_category`),
  KEY `idx_is_active` (`is_active`),
  KEY `idx_sort_order` (`sort_order`),
  KEY `idx_scope_type` (`scope_type`),
  KEY `idx_scope_id` (`scope_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='流程定义配置表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_workflow_revocation`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_workflow_revocation` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `application_id` bigint NOT NULL COMMENT '申请ID',
  `revoker_id` varchar(64) NOT NULL COMMENT '撤销人ID',
  `revoker_name` varchar(100) NOT NULL COMMENT '撤销人姓名',
  `reason` varchar(200) DEFAULT NULL COMMENT '撤销原因',
  `approver_consent` tinyint(1) DEFAULT '0' COMMENT '审批人同意',
  `handler_consent` tinyint(1) DEFAULT '0' COMMENT '办理人同意',
  `status` varchar(20) DEFAULT 'PENDING' COMMENT '状态: PENDING,APPROVED,REJECTED',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_application_id` (`application_id`),
  KEY `idx_revoker_id` (`revoker_id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='流程撤销记录表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_workflow_statistics`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_workflow_statistics` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `process_key` varchar(64) NOT NULL COMMENT '流程Key',
  `stat_date` date NOT NULL COMMENT '统计日期',
  `total_count` int DEFAULT '0' COMMENT '总申请数',
  `approved_count` int DEFAULT '0' COMMENT '已通过数',
  `rejected_count` int DEFAULT '0' COMMENT '已拒绝数',
  `pending_count` int DEFAULT '0' COMMENT '待处理数',
  `avg_duration` decimal(10,2) DEFAULT NULL COMMENT '平均处理时长(小时)',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_process_date` (`process_key`,`stat_date`),
  KEY `idx_process_key` (`process_key`),
  KEY `idx_stat_date` (`stat_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='流程统计表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_workflow_task_history`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_workflow_task_history` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `application_id` bigint NOT NULL COMMENT '申请ID',
  `task_id` varchar(64) NOT NULL COMMENT '任务ID',
  `task_name` varchar(100) NOT NULL COMMENT '任务名称',
  `assignee_id` varchar(64) DEFAULT NULL COMMENT '处理人ID',
  `assignee_name` varchar(100) DEFAULT NULL COMMENT '处理人姓名',
  `action` varchar(20) NOT NULL COMMENT '操作: APPROVE,REJECT,TRANSFER,HANDOVER,DELEGATE',
  `comment` text COMMENT '处理意见',
  `signature_data` text COMMENT '电子签名数据',
  `action_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_application_id` (`application_id`),
  KEY `idx_task_id` (`task_id`),
  KEY `idx_assignee_id` (`assignee_id`),
  KEY `idx_action` (`action`),
  KEY `idx_action_time` (`action_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='流程任务历史表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_workflow_viewer`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_workflow_viewer` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `application_id` bigint NOT NULL COMMENT '申请ID',
  `user_id` varchar(64) NOT NULL COMMENT '用户ID',
  `user_name` varchar(100) NOT NULL COMMENT '用户姓名',
  `permission_type` varchar(20) DEFAULT 'VIEW' COMMENT '权限类型: VIEW,EDIT',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_application_id` (`application_id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_permission_type` (`permission_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='流程查看权限表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
