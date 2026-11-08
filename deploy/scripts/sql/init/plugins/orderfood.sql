-- =====================================================================
-- 插件 orderfood 的数据库建表脚本（共 54 张表）
-- 用途：安装「orderfood」插件后按需执行（也可由插件自身的迁移通道建表）。
-- 🔴 商城核心（Core）初始化不包含本文件；未安装该插件时不要执行。
-- 全部 CREATE TABLE 已统一补 IF NOT EXISTS（幂等，可重复执行，不会删数据）。
-- 执行期间临时关闭外键检查：插件之间存在跨插件外键（如 oa 引用 hou_pm_project），
-- 未同时安装被引用插件时允许该外键暂时悬空。
-- =====================================================================

SET FOREIGN_KEY_CHECKS=0;

-- ---------- 基础表结构（结构基线） ----------
-- Table structure for table `hou_orderfood_address`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_orderfood_address` (
  `id` int unsigned NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `user_id` int DEFAULT '0' COMMENT '会员ID',
  `title` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '收货人姓名',
  `mobile` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '手机号',
  `suffix` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '先生' COMMENT '后缀',
  `address` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '地址',
  `remark` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '门牌号',
  `tag` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '家' COMMENT '标签',
  `isDefault` int DEFAULT '0' COMMENT '是否是默认地址  1是 0否 默认否',
  `createtime` datetime DEFAULT NULL COMMENT '创建时间',
  `updatetime` datetime DEFAULT NULL COMMENT '更新时间',
  `deletetime` datetime DEFAULT NULL COMMENT '删除时间',
  `lon` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '经度',
  `lat` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '纬度',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='收货地址表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_orderfood_app_type`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_orderfood_app_type` (
  `id` int unsigned NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `title` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '分类名称',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='页面类型';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_orderfood_app_url`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_orderfood_app_url` (
  `id` int unsigned NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `type_id` int unsigned DEFAULT '0' COMMENT '页面类型id',
  `title` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '标题',
  `url` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT 'url',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='小程序页面url表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_orderfood_cart`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_orderfood_cart` (
  `id` int unsigned NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `user_id` int DEFAULT '0' COMMENT '会员ID',
  `store_id` int DEFAULT '0' COMMENT '商家id',
  `goods_id` int DEFAULT '0' COMMENT '商品ID',
  `item_id` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '商品规格ID',
  `item_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '商品规格名称',
  `price` decimal(10,2) DEFAULT NULL COMMENT '商品价格',
  `count` int DEFAULT '0' COMMENT '商品数量',
  `status` int DEFAULT '0' COMMENT '状态 1:缺货 2：失效  ',
  `createtime` bigint DEFAULT NULL COMMENT '创建时间',
  `updatetime` bigint DEFAULT NULL COMMENT '更新时间',
  `order_type` tinyint(1) DEFAULT '0' COMMENT '自取or外卖',
  `type` int DEFAULT '2' COMMENT '类型:1=单规格,2=多规格',
  `spu_info` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci COMMENT '单规格数组',
  `spu_hash` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '唯一哈希值',
  `spu_info_ys` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci COMMENT '单规格数组-原始数据',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='购物车表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_orderfood_config`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_orderfood_config` (
  `id` int NOT NULL AUTO_INCREMENT,
  `type` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '类型',
  `name` varchar(60) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT '' COMMENT '名称',
  `value` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci COMMENT '值',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `class_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='配置表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_orderfood_coupon`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_orderfood_coupon` (
  `id` int unsigned NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `title` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '标题',
  `image` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '图片',
  `with_amount` decimal(10,2) unsigned DEFAULT NULL COMMENT '所需金额条件',
  `used_amount` decimal(10,2) unsigned DEFAULT NULL COMMENT '抵扣金额',
  `quota` int DEFAULT '0' COMMENT '发券数量',
  `start_time` datetime DEFAULT NULL COMMENT '生效时间',
  `end_time` datetime DEFAULT NULL COMMENT '失效时间',
  `draw_range` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '可领取时间段',
  `remarks` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '说明',
  `astrict` int DEFAULT '1' COMMENT '每人限领',
  `createtime` datetime DEFAULT NULL COMMENT '创建时间',
  `updatetime` datetime DEFAULT NULL COMMENT '更新时间',
  `deletetime` datetime DEFAULT NULL COMMENT '删除时间',
  `weigh` int DEFAULT '0' COMMENT '权重',
  `store_switch` int DEFAULT '0' COMMENT '是否允许积分兑换',
  `state` int DEFAULT '1' COMMENT '状态值:0=禁用,1=正常',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='优惠券信息表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_orderfood_coupon_receive`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_orderfood_coupon_receive` (
  `id` int unsigned NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `user_id` int DEFAULT '0' COMMENT '会员ID',
  `coupon_id` int unsigned DEFAULT '0' COMMENT '优惠券ID',
  `money` decimal(10,2) unsigned DEFAULT NULL COMMENT '优惠券金额',
  `create_time` datetime DEFAULT NULL COMMENT '领取时间',
  `state` int DEFAULT '0' COMMENT '状态值:1为已使用，0为已领取未使用，-1为已过期',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='优惠券领取记录表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_orderfood_distribution_grade`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_orderfood_distribution_grade` (
  `id` int unsigned NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '等级名称',
  `image` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '等级icon',
  `one_level` int unsigned DEFAULT '0' COMMENT '一级分销比例',
  `two_level` int unsigned DEFAULT '0' COMMENT '二级分销比例',
  `team_xx` int DEFAULT '0' COMMENT '升级条件-团队所需人数',
  `amount` decimal(10,2) unsigned DEFAULT NULL COMMENT '升级条件-下级支付金额',
  `createtime` datetime DEFAULT NULL COMMENT '创建时间',
  `updatetime` datetime DEFAULT NULL COMMENT '更新时间',
  `deletetime` datetime DEFAULT NULL COMMENT '删除时间',
  `weigh` int DEFAULT '0' COMMENT '权重',
  `state` int DEFAULT '1' COMMENT '状态值:0=禁用,1=正常',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='分销等级表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_orderfood_distribution_grade_log`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_orderfood_distribution_grade_log` (
  `id` int unsigned NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `user_id` int DEFAULT '0' COMMENT '会员ID',
  `before` int DEFAULT '0' COMMENT '变更前等级',
  `after` int DEFAULT '0' COMMENT '变更后等级',
  `createtime` datetime DEFAULT NULL COMMENT '创建时间',
  `updatetime` datetime DEFAULT NULL COMMENT '更新时间',
  `flag` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '操作人 系统,/人工',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='分销等级变更记录表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_orderfood_distribution_log`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_orderfood_distribution_log` (
  `id` int unsigned NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `user_id` int DEFAULT '0' COMMENT '受益人',
  `pay_user_id` int DEFAULT '0' COMMENT '支付者',
  `order_id` int DEFAULT '0' COMMENT '订单ID',
  `pay_amount` decimal(10,2) unsigned DEFAULT NULL COMMENT '支付金额',
  `pay_time` datetime DEFAULT NULL COMMENT '支付时间',
  `distribution_amount` decimal(10,2) DEFAULT NULL COMMENT '分佣金额',
  `createtime` datetime DEFAULT NULL COMMENT '创建时间',
  `updatetime` datetime DEFAULT NULL COMMENT '更新时间',
  `status` enum('1','2') CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '1' COMMENT '状态1=待入账2=已入账',
  `lesson_id` int DEFAULT '0' COMMENT '课程id',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='分销明细表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_orderfood_distribution_tghb`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_orderfood_distribution_tghb` (
  `id` int NOT NULL AUTO_INCREMENT,
  `img` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '图片地址',
  `createtime` datetime NOT NULL COMMENT '创建时间',
  `imgx` int NOT NULL COMMENT '图片x轴',
  `imgy` int NOT NULL COMMENT '图片y轴',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='推广海报图片';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_orderfood_goods`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_orderfood_goods` (
  `id` int unsigned NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `type_id` int DEFAULT '0' COMMENT '分类id',
  `title` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '商品名称',
  `tags` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '标签',
  `store_switch` int DEFAULT '0' COMMENT '门店是否通用',
  `store_ids` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '门店ID',
  `image` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '商品缩略图',
  `images` varchar(1500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '商品详情图',
  `sku_json` varchar(1500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '商品规格:title=名称,intro=价格',
  `createtime` datetime DEFAULT NULL COMMENT '创建时间',
  `updatetime` datetime DEFAULT NULL COMMENT '更新时间',
  `deletetime` datetime DEFAULT NULL COMMENT '删除时间',
  `state` int DEFAULT '1' COMMENT '状态值:0=禁用,1=正常',
  `shipment` int DEFAULT '1' COMMENT '出货类型:1=自取,2=外卖,3=均可',
  `json` varchar(1500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '详情',
  `weigh` int DEFAULT '0' COMMENT '权重',
  `type` int DEFAULT '1' COMMENT '商品类型:1=单规格,2=多规格',
  `price` decimal(10,2) DEFAULT NULL COMMENT '价格',
  `feedback` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci COMMENT '编辑时的回显数据，请不要动',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='商品表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_orderfood_goods_sku_item`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_orderfood_goods_sku_item` (
  `id` int unsigned NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `goods_id` int DEFAULT '0' COMMENT '所属商品',
  `spec_id` int NOT NULL DEFAULT '0' COMMENT '所属规格',
  `name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT '' COMMENT '名字',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='Sku商品规格';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_orderfood_goods_sku_product`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_orderfood_goods_sku_product` (
  `id` int unsigned NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `image` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT '' COMMENT '图片',
  `goods_id` int NOT NULL DEFAULT '0' COMMENT '所属商品',
  `key` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT '' COMMENT 'key',
  `market_price` decimal(10,2) NOT NULL COMMENT '原价',
  `price` decimal(10,2) NOT NULL COMMENT '现价',
  `stock` int NOT NULL DEFAULT '0' COMMENT '库存',
  `sn` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT '' COMMENT '商品编码',
  `status` tinyint(1) NOT NULL DEFAULT '1' COMMENT '状态',
  `stock_alarm` int DEFAULT NULL COMMENT '库存预警',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 ROW_FORMAT=DYNAMIC COMMENT='Sku商品';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_orderfood_goods_sku_spec`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_orderfood_goods_sku_spec` (
  `id` int unsigned NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `goods_id` int NOT NULL DEFAULT '0' COMMENT '所属商品',
  `name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT '' COMMENT '名字',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='Sku商品规格';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_orderfood_goods_type`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_orderfood_goods_type` (
  `id` int unsigned NOT NULL AUTO_INCREMENT COMMENT '主键id',
  `name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '类型',
  `store_switch` int DEFAULT '0' COMMENT '门店是否通用',
  `store_ids` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '门店ID',
  `weigh` int DEFAULT '0' COMMENT '权重',
  `createtime` datetime DEFAULT NULL COMMENT '创建时间',
  `updatetime` datetime DEFAULT NULL COMMENT '更新时间',
  `deletetime` datetime DEFAULT NULL COMMENT '删除时间',
  `state` int DEFAULT '1' COMMENT '状态值:0=禁用,1=正常',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='商品类型表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_orderfood_message_log`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_orderfood_message_log` (
  `id` int unsigned NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `user_id` int DEFAULT '0' COMMENT '会员ID',
  `point` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '推送消息节点',
  `status` enum('0','1') CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '1' COMMENT '状态 0=失败 1=成功',
  `notice` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci COMMENT '通知内容',
  `result` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci COMMENT '返回结果',
  `createtime` bigint DEFAULT NULL COMMENT '创建时间',
  `updatetime` bigint DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='消息推送日志表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_orderfood_message_push`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_orderfood_message_push` (
  `id` int unsigned NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `masterplate` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '模版消息ID',
  `type` enum('1','2','3','4') CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '1' COMMENT '通知类型 1=支付成功通知,2=积分变更通知,3=订单状态通知,4=加菜通知',
  `multiplejson` varchar(1500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '详细内容:title=字段名,value=介绍',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='消息推送表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_orderfood_notice`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_orderfood_notice` (
  `id` int NOT NULL AUTO_INCREMENT,
  `title` varchar(200) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL COMMENT '标题',
  `image` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT '' COMMENT '图片',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '内容',
  `createtime` datetime DEFAULT NULL COMMENT '添加时间',
  `updatetime` datetime DEFAULT NULL COMMENT '更新时间',
  `description` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '描述',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `id` (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='官方信息通知';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_orderfood_order`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_orderfood_order` (
  `id` int unsigned NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `order_no` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '订单编号',
  `order_type` int DEFAULT '1' COMMENT '订单类型 1=自取 2=外卖 3=堂食 4=预约',
  `user_id` int DEFAULT '0' COMMENT '会员ID',
  `store_id` int DEFAULT '0' COMMENT '门店ID',
  `address_id` int DEFAULT '0' COMMENT '收货地址ID',
  `coupon_id` int DEFAULT '0' COMMENT '优惠券ID',
  `cart_ids` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '购买的购物车id',
  `status` int DEFAULT '1' COMMENT '订单状态 1：待付款 2：待收货 3：已完成 4：待退款 5：已取消 6:备餐 7:出餐 8:待取单 9:已退款 10:预约中,11,制作中',
  `business_status` int DEFAULT '0' COMMENT '商家接单状态 0：无 1：待接单 2：已接单',
  `order_amount_total` decimal(10,2) unsigned DEFAULT NULL COMMENT '实际付款金额',
  `logistics_fee` decimal(10,2) unsigned DEFAULT NULL COMMENT '配送费金额',
  `out_trade_no` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '三方支付单号',
  `remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '订单备注',
  `code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '取餐号',
  `subscribe_time` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '预约时间',
  `service_time` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '送达时间',
  `tableware_count` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '餐具数量',
  `subscribe_tel` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '预留电话',
  `pay_time` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '支付时间',
  `createtime` datetime NOT NULL COMMENT '创建时间',
  `updatetime` datetime DEFAULT NULL COMMENT '更新时间',
  `confirm_time` datetime DEFAULT NULL COMMENT '收货/自提时间',
  `print_no` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '打印订单号',
  `out_refund_no` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '退款单号',
  `refund_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '三方退款单号',
  `apply` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '退款申请原因',
  `last_status` int DEFAULT '0' COMMENT '退款前一步订单状态',
  `dd_time` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '到店时间',
  `subscribe_name` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '订座人',
  `seat_id` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '0' COMMENT '桌号id',
  `type` enum('1','2') CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '1' COMMENT '订单状态 1=支付订单 2=充值订单',
  `recharge` decimal(10,2) DEFAULT NULL COMMENT '充值赠送金额',
  `pay_type` int DEFAULT '0' COMMENT '支付方式:1=微信,2=钱包,3=现金支付,0=暂无,4,支付宝支付',
  `dc_type` enum('1','2') CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '1' COMMENT '点餐类型:1=用户点餐,2=门店点餐',
  `address_info` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci COMMENT '收货地址信息',
  `people_count` int DEFAULT NULL COMMENT '就餐人数',
  `cwf_fee` decimal(50,0) DEFAULT NULL COMMENT '餐位费',
  `p_type` int DEFAULT '1' COMMENT '支付类型 1=先付后餐 2=先餐后付',
  `is_settlement` int DEFAULT '0' COMMENT '是否结算',
  `finish_time` datetime DEFAULT NULL COMMENT '完成时间',
  PRIMARY KEY (`id`,`createtime`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='订单表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_orderfood_order_detail`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_orderfood_order_detail` (
  `id` int unsigned NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `order_id` int DEFAULT '0' COMMENT '订单ID',
  `score_id` int DEFAULT '0' COMMENT '门店ID',
  `goods_id` int DEFAULT '0' COMMENT '商品ID',
  `goods_item_id` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '商品规格ID',
  `goods_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '商品名称',
  `item_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '商品规格名称',
  `goods_count` int DEFAULT '0' COMMENT '商品数量',
  `goods_price` decimal(10,2) unsigned DEFAULT NULL COMMENT '商品单价',
  `goods_prices` decimal(10,2) unsigned DEFAULT NULL COMMENT '商品总价',
  `createtime` datetime DEFAULT NULL COMMENT '创建时间',
  `updatetime` datetime DEFAULT NULL COMMENT '更新时间',
  `spu_info` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci COMMENT '单规格数组',
  `print_status` int DEFAULT '0' COMMENT '是否已打印:0=否,1=是',
  `is_sc` int DEFAULT '0' COMMENT '是否已送出:0=否,1=是',
  `numbers` int DEFAULT '1' COMMENT '次数',
  `user_id` int DEFAULT NULL COMMENT '用户ID',
  `hc_print_status` int DEFAULT '0',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='订单详情表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_orderfood_pact`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_orderfood_pact` (
  `id` int unsigned NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `title` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '标题',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci COMMENT '内容',
  `createtime` datetime DEFAULT NULL COMMENT '创建时间',
  `updatetime` datetime DEFAULT NULL COMMENT '更新时间',
  `weigh` int DEFAULT '0' COMMENT '权重',
  `store_switch` int DEFAULT '0' COMMENT '开关',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='协议表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_orderfood_point_order`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_orderfood_point_order` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `order_no` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '订单编号',
  `user_id` int DEFAULT '0' COMMENT '会员ID',
  `goods_id` int DEFAULT '0' COMMENT '商品ID',
  `address_id` int DEFAULT '0' COMMENT '收货地址ID',
  `coupon_id` int DEFAULT '0' COMMENT '优惠券ID',
  `status` int DEFAULT '1' COMMENT '订单状态 1：待发货 2：待收货 3：已完成',
  `goods_count` int DEFAULT '0' COMMENT '商品数量',
  `goods_price` decimal(10,2) unsigned DEFAULT NULL COMMENT '商品单价',
  `goods_prices` decimal(10,2) unsigned DEFAULT NULL COMMENT '商品总价',
  `order_amount_total` decimal(10,2) unsigned DEFAULT NULL COMMENT '付款积分',
  `logistics_fee` decimal(10,2) unsigned DEFAULT NULL COMMENT '配送费金额',
  `remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '订单备注',
  `pay_time` datetime DEFAULT NULL COMMENT '支付时间',
  `delivery_time` datetime DEFAULT NULL COMMENT '发货时间',
  `courier` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '快递公司',
  `numbers` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '快递单号',
  `createtime` datetime DEFAULT NULL COMMENT '创建时间',
  `updatetime` datetime DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='积分订单';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_orderfood_pointgoods`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_orderfood_pointgoods` (
  `id` int unsigned NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `type_id` int unsigned DEFAULT '0' COMMENT '分类',
  `coupon_id` int unsigned DEFAULT '0' COMMENT '优惠券id',
  `title` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '标题',
  `keywords` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '关键字',
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci COMMENT '描述',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci COMMENT '详情',
  `need` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci COMMENT '兑换须知',
  `thumbnail` varchar(300) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '商品缩略图',
  `images` varchar(1500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '商品图片',
  `price` decimal(10,2) unsigned DEFAULT NULL COMMENT '所需价格(积分)',
  `line_price` decimal(10,2) unsigned DEFAULT NULL COMMENT '划线价格(积分)',
  `stock` int unsigned DEFAULT '0' COMMENT '库存',
  `daily` int unsigned DEFAULT '0' COMMENT '日限购',
  `createtime` datetime DEFAULT NULL COMMENT '创建时间',
  `updatetime` datetime DEFAULT NULL COMMENT '更新时间',
  `deletetime` datetime DEFAULT NULL COMMENT '删除时间',
  `state` int DEFAULT '1' COMMENT '状态值:0=禁用,1=正常',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `title` (`title`) USING BTREE,
  KEY `type_id` (`type_id`) USING BTREE,
  KEY `keywords` (`keywords`(191)) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='积分商品表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_orderfood_pointgoods_type`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_orderfood_pointgoods_type` (
  `id` int unsigned NOT NULL AUTO_INCREMENT COMMENT '主键id',
  `name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '类型',
  `weigh` int DEFAULT '0' COMMENT '权重',
  `createtime` datetime DEFAULT NULL COMMENT '创建时间',
  `updatetime` datetime DEFAULT NULL COMMENT '更新时间',
  `deletetime` datetime DEFAULT NULL COMMENT '删除时间',
  `state` int DEFAULT '1' COMMENT '状态值:0=禁用,1=正常',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='积分商品类型表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_orderfood_print`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_orderfood_print` (
  `id` int unsigned NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `title` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '打印机名称',
  `brand` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '标识',
  `print_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '打印机账号',
  `print_key` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '打印机秘钥',
  `print_temp` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci COMMENT '打印机模板',
  `status` enum('normal','hidden') CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT 'normal' COMMENT '状态',
  `createtime` datetime DEFAULT NULL COMMENT '创建时间',
  `updatetime` datetime DEFAULT NULL COMMENT '更新时间',
  `deletetime` datetime DEFAULT NULL COMMENT '删除时间',
  `ip` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT 'ip',
  `port` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '端口',
  `path` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '路径',
  `appid` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci COMMENT 'appid',
  `appsecret` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci COMMENT 'appsecret',
  `access_token` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci COMMENT 'access_token',
  `sn` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '打印机编号',
  `print_msg` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '打印标签',
  `count` int DEFAULT '1' COMMENT '打印次数',
  `store_id` int DEFAULT NULL,
  `print_location` enum('front','kitchen') CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT 'front',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='打印机配置表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_orderfood_print_log`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_orderfood_print_log` (
  `id` int unsigned NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `order_no` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '订单编号',
  `store_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '店铺名称',
  `createtime` datetime DEFAULT NULL COMMENT '打印时间',
  `updatetime` datetime DEFAULT NULL COMMENT '更新时间',
  `store_id` int DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='打印日志表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_orderfood_recharge`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_orderfood_recharge` (
  `id` int unsigned NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `amount` decimal(10,2) DEFAULT NULL COMMENT '充值金额',
  `amount_over` decimal(10,2) DEFAULT NULL COMMENT '获得金额',
  `status` enum('normal','hidden') CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT 'normal' COMMENT '状态,''normal'',''hidden''',
  `createtime` datetime DEFAULT NULL COMMENT '创建时间',
  `updatetime` datetime DEFAULT NULL COMMENT '更新时间',
  `deletetime` datetime DEFAULT NULL COMMENT '删除时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='充值优惠表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_orderfood_refund`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_orderfood_refund` (
  `id` int unsigned NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `order_id` int DEFAULT '0' COMMENT '订单ID',
  `store_id` int DEFAULT '0' COMMENT '门店ID',
  `amount` decimal(10,2) unsigned DEFAULT NULL COMMENT '退款金额',
  `user_id` int DEFAULT '0' COMMENT '退款用户',
  `refund_status` int DEFAULT '1' COMMENT '商家退款状态:1=待退款,2=已退款,3=拒绝退款',
  `createtime` datetime DEFAULT NULL COMMENT '创建时间',
  `updatetime` datetime DEFAULT NULL COMMENT '更新时间',
  `apply` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '退款申请原因',
  `refund_time` datetime DEFAULT NULL COMMENT '退款时间',
  `order_no` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL,
  `is_settlement` int DEFAULT '0' COMMENT '是否结算',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='退款流水表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_orderfood_revenue`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_orderfood_revenue` (
  `id` int unsigned NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `store_id` int DEFAULT '0' COMMENT '门店ID',
  `type` enum('1','2') CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '1' COMMENT '类型:1=收入,2=支出',
  `amount` decimal(10,2) unsigned DEFAULT NULL COMMENT '金额',
  `user_id` int DEFAULT '0' COMMENT '用户',
  `createtime` datetime DEFAULT NULL COMMENT '创建时间',
  `updatetime` datetime DEFAULT NULL COMMENT '更新时间',
  `pay_type` enum('1','2','3','0','4') CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '0' COMMENT '支付方式:1=微信,2=钱包,3=现金支付,0=暂无,4,支付宝支付',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='店铺收支明细表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_orderfood_rotation`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_orderfood_rotation` (
  `id` int unsigned NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `image` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '图片',
  `url` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '链接',
  `type` int DEFAULT '1' COMMENT '类型:1=首页上方,2=首页下方,3=我的,4=积分商城',
  `createtime` datetime DEFAULT NULL COMMENT '创建时间',
  `updatetime` datetime DEFAULT NULL COMMENT '更新时间',
  `deletetime` datetime DEFAULT NULL COMMENT '删除时间',
  `weigh` int DEFAULT '0' COMMENT '权重',
  `state` int DEFAULT '1' COMMENT '状态值:0=禁用,1=正常',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='缩略图表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_orderfood_search_history`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_orderfood_search_history` (
  `id` int unsigned NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `user_id` int DEFAULT '0' COMMENT '会员ID',
  `title` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '搜索内容',
  `number` int DEFAULT NULL COMMENT '搜索次数',
  `createtime` datetime DEFAULT NULL COMMENT '创建时间',
  `updatetime` datetime DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='搜索历史表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_orderfood_seat`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_orderfood_seat` (
  `id` int unsigned NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `store_id` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '门店ID',
  `title` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '桌位名称',
  `no` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '桌位编号',
  `status` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT 'normal' COMMENT '状态,''normal'',''hidden''',
  `createtime` datetime DEFAULT NULL COMMENT '创建时间',
  `updatetime` datetime DEFAULT NULL COMMENT '更新时间',
  `deletetime` datetime DEFAULT NULL COMMENT '删除时间',
  `type` int DEFAULT NULL COMMENT '桌类型',
  `img` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '桌号图片',
  `use_status` int DEFAULT '1' COMMENT '使用状态:1=空闲中,2=使用中,3=打扫中',
  `floor` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '楼层',
  `image1` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '先餐后付二维码',
  `image` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '先付后餐二维码',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='门店桌号表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_orderfood_seat_type`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_orderfood_seat_type` (
  `id` int unsigned NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `title` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '分类名称',
  `no` int DEFAULT NULL COMMENT '人数',
  `createtime` datetime DEFAULT NULL COMMENT '创建时间',
  `updatetime` datetime DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='门店桌号分类表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_orderfood_share`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_orderfood_share` (
  `id` int unsigned NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `user_id` int DEFAULT '0' COMMENT '会员ID',
  `p_id` int DEFAULT '0' COMMENT '会员ID',
  `desc` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '说明',
  `createtime` datetime DEFAULT NULL COMMENT '创建时间',
  `updatetime` datetime DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='分享邀请表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_orderfood_store`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_orderfood_store` (
  `id` int unsigned NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `title` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '门店名称',
  `alias_title` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '门店别名',
  `address` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '门店地址',
  `lon` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '门店经度',
  `lat` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '门店纬度',
  `contacts` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '联系人',
  `tel` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '门店电话',
  `image` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '门店缩略图',
  `images` varchar(1500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '门店轮播图片',
  `desc` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '门店简介',
  `start_times` time DEFAULT NULL COMMENT '营业开始时间',
  `end_times` time DEFAULT NULL COMMENT '营业结束时间',
  `subscribe_switch` int DEFAULT '0' COMMENT '预订开关',
  `delivery_switch` int DEFAULT '0' COMMENT '自提开关',
  `takeaway_switch` int DEFAULT '0' COMMENT '外卖开关',
  `createtime` datetime DEFAULT NULL COMMENT '创建时间',
  `updatetime` datetime DEFAULT NULL COMMENT '更新时间',
  `deletetime` datetime DEFAULT NULL COMMENT '删除时间',
  `weigh` int DEFAULT '0' COMMENT '权重',
  `state` int DEFAULT '1' COMMENT '状态值:0=禁用,1=正常',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci COMMENT '外卖超时赔付',
  `balance` decimal(10,2) unsigned DEFAULT NULL COMMENT '余额',
  `trade_switch` int DEFAULT '1' COMMENT '营业开关',
  `authentication` int DEFAULT '0' COMMENT '认证状态:0=未认证,1=已认证',
  `cleck_goods_counts` int DEFAULT '0' COMMENT '商品点击数',
  `print_sn` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '打印机编码',
  `deposit` decimal(10,2) unsigned DEFAULT NULL COMMENT '预约定金',
  `radius` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '服务半径(km)',
  `admin_id` int DEFAULT NULL COMMENT '绑定账号ID',
  `eat_type` int DEFAULT NULL COMMENT '1=先付后餐 2=先餐后付',
  `print_hc_sn` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='门店表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_orderfood_store_admin`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_orderfood_store_admin` (
  `id` int NOT NULL AUTO_INCREMENT,
  `admin_id` int DEFAULT NULL,
  `store_ids` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='门店权限表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_orderfood_store_authen`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_orderfood_store_authen` (
  `id` int unsigned NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `store_id` int DEFAULT '0' COMMENT '门店ID',
  `title` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '姓名',
  `id_number` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '身份证号',
  `mobile` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '手机号',
  `store_image` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '店铺图片',
  `yy_image` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '营业执照',
  `font_image` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '身份证正面',
  `back_image` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '身份证反面',
  `hard_image` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '手持身份证',
  `self_image` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '个人自拍',
  `status` int DEFAULT '0' COMMENT '审核状态:0=审核中,1=审核成功,2=审核失败',
  `result` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '审核拒绝原因',
  `createtime` datetime DEFAULT NULL COMMENT '创建时间',
  `updatetime` datetime DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='门店认证表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_orderfood_store_collect`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_orderfood_store_collect` (
  `id` int unsigned NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `user_id` int DEFAULT '0' COMMENT '会员ID',
  `store_id` int unsigned DEFAULT '0' COMMENT '门店id',
  `createtime` datetime DEFAULT NULL COMMENT '创建时间',
  `updatetime` datetime DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='门店收藏表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_orderfood_store_user`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_orderfood_store_user` (
  `id` int unsigned NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `store_id` int DEFAULT '0' COMMENT '门店ID',
  `user_id` int DEFAULT '0' COMMENT '用户id',
  `order_type` int DEFAULT '2' COMMENT '用户角色 1=店长 2=员工',
  `status` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT 'normal' COMMENT '状态',
  `createtime` datetime DEFAULT NULL COMMENT '创建时间',
  `updatetime` datetime DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='门店用户关联表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_orderfood_store_view`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_orderfood_store_view` (
  `id` int unsigned NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `user_id` int DEFAULT '0' COMMENT '会员ID',
  `store_id` int unsigned DEFAULT '0' COMMENT '门店id',
  `createtime` datetime DEFAULT NULL COMMENT '第一次访问时间',
  `updatetime` datetime DEFAULT NULL COMMENT '最新访问时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='门店浏览表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_orderfood_third`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_orderfood_third` (
  `id` int unsigned NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `user_id` int unsigned DEFAULT '0' COMMENT '会员ID',
  `platform` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '' COMMENT '第三方应用',
  `apptype` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '' COMMENT '应用类型',
  `unionid` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '' COMMENT '第三方UNIONID',
  `openname` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT '' COMMENT '第三方会员昵称',
  `openid` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '' COMMENT '第三方OPENID',
  `access_token` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '' COMMENT 'AccessToken',
  `refresh_token` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT 'RefreshToken',
  `expires_in` int unsigned DEFAULT '0' COMMENT '有效期',
  `createtime` datetime DEFAULT NULL COMMENT '创建时间',
  `updatetime` datetime DEFAULT NULL COMMENT '更新时间',
  `logintime` datetime DEFAULT NULL COMMENT '登录时间',
  `expiretime` datetime DEFAULT NULL COMMENT '过期时间',
  `alipay_open_id` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  KEY `user_id` (`user_id`,`platform`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='第三方登录表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_orderfood_user`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_orderfood_user` (
  `id` int unsigned NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `group_id` int unsigned NOT NULL DEFAULT '0' COMMENT '组别ID',
  `username` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT '' COMMENT '用户名',
  `nickname` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT '' COMMENT '昵称',
  `realname` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT '' COMMENT '真实姓名',
  `password` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT '' COMMENT '密码',
  `salt` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT '' COMMENT '密码盐',
  `email` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT '' COMMENT '电子邮箱',
  `mobile` varchar(11) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT '' COMMENT '手机号',
  `staffnumber` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT '' COMMENT '员工工号',
  `avatar` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT '' COMMENT '头像',
  `level` int unsigned NOT NULL DEFAULT '0' COMMENT '等级',
  `vip` int unsigned DEFAULT '0' COMMENT 'VIP等级',
  `gender` int unsigned NOT NULL DEFAULT '0' COMMENT '性别',
  `birthday` date DEFAULT NULL COMMENT '生日',
  `bio` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT '' COMMENT '格言',
  `money` decimal(10,2) NOT NULL COMMENT '余额',
  `score` int NOT NULL DEFAULT '0' COMMENT '积分',
  `successions` int unsigned NOT NULL DEFAULT '1' COMMENT '连续登录天数',
  `maxsuccessions` int unsigned NOT NULL DEFAULT '1' COMMENT '最大连续登录天数',
  `prevtime` datetime DEFAULT NULL COMMENT '上次登录时间',
  `logintime` datetime DEFAULT NULL COMMENT '登录时间',
  `loginip` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT '' COMMENT '登录IP',
  `loginfailure` tinyint unsigned NOT NULL DEFAULT '0' COMMENT '失败次数',
  `joinip` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT '' COMMENT '加入IP',
  `jointime` datetime DEFAULT NULL COMMENT '加入时间',
  `createtime` datetime DEFAULT NULL COMMENT '创建时间',
  `updatetime` datetime DEFAULT NULL COMMENT '更新时间',
  `token` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT '' COMMENT 'Token',
  `status` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT '' COMMENT '状态',
  `verification` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT '' COMMENT '验证',
  `title` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT '' COMMENT '称号',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `username` (`username`) USING BTREE,
  KEY `email` (`email`) USING BTREE,
  KEY `mobile` (`mobile`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC COMMENT='会员表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_orderfood_user_bank`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_orderfood_user_bank` (
  `id` int unsigned NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `store_id` int DEFAULT '0' COMMENT '门店ID',
  `user_id` int DEFAULT '0' COMMENT '用户ID',
  `type` int DEFAULT '1' COMMENT '提现类型 1=银行卡 2=微信 3=支付宝',
  `bank_name` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '银行名称',
  `name` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '姓名',
  `bank_code` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '银行卡号',
  `bank_mobile` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '手机号',
  `createtime` datetime DEFAULT NULL COMMENT '创建时间',
  `updatetime` datetime DEFAULT NULL COMMENT '更新时间',
  `zfb_code` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '支付宝账号',
  `wx_code` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '微信账号',
  `zfb_img` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '支付宝收款码',
  `wx_img` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '微信收款码',
  `status` int DEFAULT '0' COMMENT '状态:0=审核中,1=审核通过,2=审核拒绝',
  `refuse` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '审核拒绝原因',
  `bank_icon` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '图标',
  `bank_type` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '银行卡类型',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='提现账户表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_orderfood_user_detail`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_orderfood_user_detail` (
  `id` int unsigned NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `user_id` int unsigned NOT NULL DEFAULT '0' COMMENT '用户id',
  `parent_id` int unsigned DEFAULT '0' COMMENT '上级id',
  `proxy_level` int DEFAULT '1' COMMENT '代理等级 默认1 青铜',
  `student_id` int DEFAULT '0' COMMENT '学号',
  `proxy_amound` decimal(10,2) unsigned DEFAULT NULL COMMENT '佣金',
  `freeze_amound` decimal(10,2) DEFAULT NULL COMMENT '冻结佣金',
  `openid` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT 'openid',
  `signins` int unsigned NOT NULL DEFAULT '1' COMMENT '连续签到天数',
  `login_flag` int DEFAULT '2' COMMENT '注册方式1手机 2微信授权',
  `subscribe` tinyint(1) DEFAULT '1' COMMENT '订阅开关 0关 1开',
  `code` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '会员码地址',
  `createtime` datetime DEFAULT NULL COMMENT '创建时间',
  `updatetime` datetime DEFAULT NULL COMMENT '更新时间',
  `alipay_open_id` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '支付宝OPENID',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='会员表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_orderfood_user_money_log`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_orderfood_user_money_log` (
  `id` int unsigned NOT NULL AUTO_INCREMENT,
  `user_id` int unsigned NOT NULL DEFAULT '0' COMMENT '会员ID',
  `money` decimal(10,2) NOT NULL COMMENT '变更余额',
  `before` decimal(10,2) NOT NULL COMMENT '变更前余额',
  `after` decimal(10,2) NOT NULL COMMENT '变更后余额',
  `memo` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT '' COMMENT '备注',
  `createtime` bigint DEFAULT NULL COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC COMMENT='会员余额变动表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_orderfood_user_score_log`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_orderfood_user_score_log` (
  `id` int unsigned NOT NULL AUTO_INCREMENT,
  `user_id` int unsigned NOT NULL DEFAULT '0' COMMENT '会员ID',
  `score` int NOT NULL DEFAULT '0' COMMENT '变更积分',
  `before` int NOT NULL DEFAULT '0' COMMENT '变更前积分',
  `after` int NOT NULL DEFAULT '0' COMMENT '变更后积分',
  `memo` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT '' COMMENT '备注',
  `createtime` datetime DEFAULT NULL COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC COMMENT='会员积分变动表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_orderfood_user_signin`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_orderfood_user_signin` (
  `id` int unsigned NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `user_id` int DEFAULT '0' COMMENT '会员ID',
  `time` bigint DEFAULT NULL COMMENT '签到时间',
  `last_time` bigint DEFAULT NULL COMMENT '上次签到时间',
  `points` int DEFAULT '0' COMMENT '获得积分',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='用户签到表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_orderfood_withdrawal`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_orderfood_withdrawal` (
  `id` int unsigned NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `store_id` int DEFAULT '0' COMMENT '门店名称',
  `user_id` int DEFAULT '0' COMMENT '提现用户',
  `amount` decimal(10,2) DEFAULT NULL COMMENT '提现金额',
  `type` int DEFAULT '0' COMMENT '提现方式',
  `status` int DEFAULT '0' COMMENT '状态:0=未审核,1=待到账,2=审核拒绝,3=已到账',
  `refuse` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '审核拒绝原因',
  `month` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '年月',
  `createtime` datetime DEFAULT NULL COMMENT '创建时间',
  `updatetime` datetime DEFAULT NULL COMMENT '更新时间',
  `residue_amount` decimal(10,2) DEFAULT NULL COMMENT '剩余可提现金额',
  `order_sn` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '提现订单号',
  `out_batch_no` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '第三方订单号',
  `bank_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '银行卡名称',
  `bank_no` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '银行卡号',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='提现管理表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_orderfood_withdrawals`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_orderfood_withdrawals` (
  `id` int unsigned NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `store_id` int DEFAULT '0' COMMENT '门店名称',
  `user_id` int DEFAULT '0' COMMENT '提现用户',
  `amount` decimal(10,2) DEFAULT NULL COMMENT '提现金额',
  `type` int DEFAULT '0' COMMENT '提现方式',
  `status` int DEFAULT '0' COMMENT '状态:0=未审核,1=待到账,2=同意,3=拒绝',
  `refuse` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '审核拒绝原因',
  `month` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '年月',
  `createtime` datetime DEFAULT NULL COMMENT '创建时间',
  `updatetime` datetime DEFAULT NULL COMMENT '更新时间',
  `residue_amount` decimal(10,2) DEFAULT NULL COMMENT '剩余可提现金额',
  `order_sn` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '提现订单号',
  `out_batch_no` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '第三方订单号',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='分销提现管理表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_orderfood_yy_label`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_orderfood_yy_label` (
  `id` int unsigned NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `title` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '标签名称',
  `createtime` datetime DEFAULT NULL COMMENT '创建时间',
  `updatetime` datetime DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='预约标签表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_orderfood_yy_rule`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_orderfood_yy_rule` (
  `id` int unsigned NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `label_id` int DEFAULT NULL COMMENT '标签ID',
  `interval` decimal(10,1) DEFAULT NULL COMMENT '间隔时间',
  `ks_times` time DEFAULT NULL COMMENT '开始时间',
  `js_times` time DEFAULT NULL COMMENT '结束时间',
  `createtime` datetime DEFAULT NULL COMMENT '创建时间',
  `updatetime` datetime DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='预约规则表';
/*!40101 SET character_set_client = @saved_cs_client */;

--

-- ---------- plugins/orderfood/runtime/resources/sql/orderfood_tables.sql ----------
-- 点餐系统数据库表结构
-- 数据库名：meoc
-- 表前缀：hou_orderfood_
-- 说明：本系统复用已有的用户表(user_info)、店铺表(shop_base)、商品表(goods_base)、订单表(order_base)
-- 只创建点餐系统特有的表结构

-- 1. 商品分类表 (点餐系统特有的分类表，与商城的分类区分)
CREATE TABLE IF NOT EXISTS `hou_orderfood_category` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `name` varchar(50) NOT NULL COMMENT '分类名称',
  `description` varchar(255) DEFAULT NULL COMMENT '分类描述',
  `shop_id` int(11) NOT NULL COMMENT '店铺ID（关联shop_base表）',
  `sort` int(11) NOT NULL DEFAULT '0' COMMENT '排序',
  `status` tinyint(4) NOT NULL DEFAULT '1' COMMENT '状态：1-启用，2-禁用',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `delete_time` datetime DEFAULT NULL COMMENT '删除时间',
  PRIMARY KEY (`id`),
  KEY `idx_shop_id` (`shop_id`),
  KEY `idx_status` (`status`),
  KEY `idx_sort` (`sort`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='点餐商品分类表';

-- 2. 点餐订单扩展表 (扩展order_base表的点餐特有字段)
CREATE TABLE IF NOT EXISTS `hou_orderfood_order_ext` (
  `order_id` varchar(100) NOT NULL COMMENT '订单号（关联order_base表）',
  `table_number` varchar(20) DEFAULT NULL COMMENT '餐桌号',
  `dining_type` tinyint(4) NOT NULL DEFAULT '1' COMMENT '就餐方式：1-堂食，2-外卖，3-自提',
  `estimated_time` int(11) DEFAULT NULL COMMENT '预计制作时间（分钟）',
  `actual_time` int(11) DEFAULT NULL COMMENT '实际制作时间（分钟）',
  `special_requirements` text COMMENT '特殊要求',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`order_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='点餐订单扩展表';

-- 插入测试数据

-- 插入分类数据（假设店铺ID为1，需要确保shop_base表中存在对应店铺）

-- 插入订单扩展数据（假设订单号对应order_base表）
SET FOREIGN_KEY_CHECKS=1;
