-- =====================================================================
-- 插件 webpos 的数据库建表脚本（共 18 张表）
-- 用途：安装「webpos」插件后按需执行（也可由插件自身的迁移通道建表）。
-- 🔴 商城核心（Core）初始化不包含本文件；未安装该插件时不要执行。
-- 全部 CREATE TABLE 已统一补 IF NOT EXISTS（幂等，可重复执行，不会删数据）。
-- 执行期间临时关闭外键检查：插件之间存在跨插件外键（如 oa 引用 hou_pm_project），
-- 未同时安装被引用插件时允许该外键暂时悬空。
-- =====================================================================

SET FOREIGN_KEY_CHECKS=0;

-- ---------- 基础表结构（结构基线） ----------
-- Table structure for table `hou_wps_gate_staff`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_wps_gate_staff` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `shop_gate_id` bigint NOT NULL COMMENT '门店ID',
  `name` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '姓名',
  `phone` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '手机号码',
  `password` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '登录密码',
  `entry_time` date NOT NULL COMMENT '入职时间',
  `job_number` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '工号',
  `sex` tinyint(1) NOT NULL COMMENT '性别 0男 1女',
  `role_id` bigint NOT NULL COMMENT '员工角色ID',
  `status` tinyint(1) NOT NULL DEFAULT '0' COMMENT '账号状态 0正常 1禁用',
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '创建时间',
  `create_by_id` bigint NOT NULL COMMENT '创建人ID',
  `update_time` timestamp NULL DEFAULT NULL COMMENT '更新时间',
  `update_by_id` bigint DEFAULT NULL COMMENT '更新人ID',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci ROW_FORMAT=DYNAMIC COMMENT='门店职员表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_wps_goods_barcode_config`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_wps_goods_barcode_config` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '支付方式ID',
  `shop_gate_id` bigint NOT NULL COMMENT '门店ID',
  `type` tinyint NOT NULL DEFAULT '0' COMMENT '类型  1位数 2:数值',
  `identifier` int DEFAULT NULL COMMENT '标识符',
  `code` int DEFAULT NULL COMMENT '编码',
  `price` int DEFAULT NULL COMMENT '金额',
  `amount` int DEFAULT NULL COMMENT '数量',
  `check` int DEFAULT NULL COMMENT '校验码',
  `create_time` timestamp NULL DEFAULT NULL COMMENT '创建时间',
  `create_by_id` bigint DEFAULT NULL COMMENT '创建人ID',
  `update_time` timestamp NULL DEFAULT NULL COMMENT '更新时间',
  `update_by_id` bigint DEFAULT NULL COMMENT '更新人ID',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `shop_gate_id` (`shop_gate_id`,`type`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci ROW_FORMAT=DYNAMIC COMMENT='门店收银设备配置';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_wps_machine`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_wps_machine` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT 'id',
  `shop_gate_id` bigint NOT NULL COMMENT '门店ID',
  `code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '设备编号',
  `name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '设备名称',
  `ip` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '设备ip',
  `factory` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '厂家名称',
  `online` tinyint DEFAULT '0' COMMENT '设备在线状态 0:离线 1:在线',
  `uniquely` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '设备唯一标识符',
  `del` tinyint DEFAULT '0' COMMENT '是否删除 0:否 1:是',
  `create_time` timestamp NULL DEFAULT NULL COMMENT '创建时间',
  `create_by_id` bigint DEFAULT NULL COMMENT '创建人ID',
  `update_time` timestamp NULL DEFAULT NULL COMMENT '更新时间',
  `update_by_id` bigint DEFAULT NULL COMMENT '更新人ID',
  `online_time` datetime DEFAULT NULL COMMENT '上线时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uniquely` (`uniquely`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci ROW_FORMAT=DYNAMIC COMMENT='门店收银相关设备';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_wps_machine_print_config`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_wps_machine_print_config` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT 'id',
  `shop_gate_id` bigint NOT NULL COMMENT '门店ID',
  `auto` tinyint DEFAULT '0' COMMENT '是否自动打印0 否 1:是',
  `active` tinyint DEFAULT NULL COMMENT '激活的收银小票模板索引',
  `order_template` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci COMMENT '收银小票模板json',
  `weight_template` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci COMMENT '称重小票模板json',
  `create_time` timestamp NULL DEFAULT NULL COMMENT '创建时间',
  `create_by_id` bigint DEFAULT NULL COMMENT '创建人ID',
  `update_time` timestamp NULL DEFAULT NULL COMMENT '更新时间',
  `update_by_id` bigint DEFAULT NULL COMMENT '更新人ID',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `shop_gate_id` (`shop_gate_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci ROW_FORMAT=DYNAMIC COMMENT='门店收银相关设备';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_wps_order`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_wps_order` (
  `order_no` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '订单编号',
  `order_status` int NOT NULL DEFAULT '0' COMMENT '订单状态 0待支付 1已支付 2已完成 3已关闭 4已取消',
  `refund_status` int DEFAULT '0' COMMENT '退款状态 0未退款 1已退款',
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '订单创建时间',
  `create_staff_id` bigint NOT NULL COMMENT '订单创建人',
  `pay_time` timestamp NULL DEFAULT NULL COMMENT '订单支付时间',
  `refund_time` timestamp NULL DEFAULT NULL COMMENT '退款时间',
  `refund_staff_id` bigint DEFAULT NULL COMMENT '订单退款人',
  `cancel_time` timestamp NULL DEFAULT NULL COMMENT '订单取消时间',
  `cancel_staff_id` bigint DEFAULT NULL COMMENT '订单取消人',
  `total_count` bigint NOT NULL COMMENT '订单总数量',
  `total_amount` decimal(11,2) NOT NULL COMMENT '订单总金额',
  `pay_total_amount` decimal(11,2) NOT NULL COMMENT '应付总金额',
  `grade_discount` decimal(11,2) NOT NULL DEFAULT '1.00' COMMENT '折扣率',
  `discount_amount` decimal(11,2) NOT NULL COMMENT '折扣金额',
  `coupon_id` bigint DEFAULT NULL COMMENT '优惠券ID',
  `coupon_amount` decimal(11,2) NOT NULL COMMENT '优惠券优惠金额',
  `member_id` bigint DEFAULT NULL COMMENT '会员ID',
  `pay_way_id` bigint DEFAULT NULL COMMENT '付款方式ID',
  `shop_id` bigint DEFAULT NULL COMMENT '店铺ID',
  `shop_gate_id` bigint NOT NULL COMMENT '门店ID',
  `real_pay_total_amount` decimal(11,2) DEFAULT NULL COMMENT '实际支付金额 针对现金支付',
  `give_change_amount` decimal(11,2) DEFAULT NULL COMMENT '找零金额  针对现金支付',
  `auth_code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '付款码',
  `pay_code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '支付返回码',
  `pay_msg` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '支付返回说明',
  `refund_code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '退款返回码',
  `refund_msg` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '退款返回说明',
  `create_machine_id` bigint DEFAULT NULL COMMENT '订单创建的机器id',
  `order_is_settlement` tinyint DEFAULT '0' COMMENT '订单是否结算 1-已结算 0-未结算',
  `order_settlement_id` varchar(250) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '结算单id',
  `order_settlement_time` datetime DEFAULT NULL COMMENT '订单结算时间',
  `order_commission_fee` decimal(10,2) DEFAULT NULL COMMENT '交易佣金',
  PRIMARY KEY (`order_no`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci ROW_FORMAT=DYNAMIC COMMENT='门店订单表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_wps_order_item`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_wps_order_item` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `order_no` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '订单编号',
  `goods_id` bigint NOT NULL COMMENT '商品ID',
  `count` bigint NOT NULL COMMENT '商品数量',
  `goods_price` decimal(11,2) NOT NULL COMMENT '商品价格',
  `goods_now_weight` decimal(11,2) DEFAULT NULL COMMENT '商品当前称重(单位随商品属性单变)',
  `is_weight_goods` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '0' COMMENT '是否是称重商品 0:否 1:是',
  `voucher_price` decimal(10,2) DEFAULT NULL COMMENT '代金券抵扣金额',
  `discount_amount` decimal(11,2) NOT NULL COMMENT '折扣金额',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci ROW_FORMAT=DYNAMIC COMMENT='门店订单商品表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_wps_payment_way`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_wps_payment_way` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `payment_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '支付方式',
  `payment_scene` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '支付场景',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci ROW_FORMAT=DYNAMIC COMMENT='门店支付方式表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_wps_role`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_wps_role` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `role_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '角色名称',
  `description` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '角色说明',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci ROW_FORMAT=DYNAMIC COMMENT='门店角色表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_wps_staff`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_wps_staff` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `staff_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '员工姓名',
  `staff_no` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '员工工号',
  `phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '手机号',
  `password` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '密码',
  `role_id` bigint DEFAULT NULL COMMENT '角色ID',
  `shop_id` bigint DEFAULT NULL COMMENT '门店ID',
  `status` tinyint DEFAULT '1' COMMENT '状态：0-禁用，1-启用',
  `last_login_time` datetime DEFAULT NULL COMMENT '最后登录时间',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint DEFAULT '0' COMMENT '删除标志：0-未删除，1-已删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_staff_no` (`staff_no`),
  UNIQUE KEY `uk_phone` (`phone`),
  KEY `idx_shop_id` (`shop_id`),
  KEY `idx_role_id` (`role_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='员工表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_wps_staff_login`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_wps_staff_login` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `gate_staff_id` bigint NOT NULL COMMENT '门店员工ID',
  `token` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '登录标识',
  `login_start_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '登录开始时间',
  `login_end_time` timestamp NULL DEFAULT NULL COMMENT '登录结束时间',
  `total_money` decimal(11,2) DEFAULT NULL COMMENT '收银总额',
  `xj_money` decimal(11,2) DEFAULT NULL COMMENT '现金支付金额',
  `ye_money` decimal(11,2) DEFAULT NULL COMMENT '余额支付金额',
  `wx_money` decimal(11,2) DEFAULT NULL COMMENT '微信支付金额',
  `zfb_money` decimal(11,2) DEFAULT NULL COMMENT '支付宝支付金额',
  `paid_in_money` decimal(11,2) DEFAULT NULL COMMENT '实缴金额',
  `change_shifts_status` tinyint(1) DEFAULT '0' COMMENT '是否交接班 0未交接班 1已交接班',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci ROW_FORMAT=DYNAMIC COMMENT='门店职员登录表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_wps_undo_log`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_wps_undo_log` (
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

-- ---------- plugins/webpos/runtime/resources/sql/javaGoods/新增脚本.sql ----------
CREATE TABLE IF NOT EXISTS `wps_gate_staff` (
                                  `id` bigint NOT NULL AUTO_INCREMENT,
                                  `shop_gate_id` bigint NOT NULL COMMENT '门店ID',
                                  `name` varchar(20)  NOT NULL COMMENT '姓名',
                                  `phone` varchar(30)  NOT NULL COMMENT '手机号码',
                                  `password` varchar(100)  NOT NULL COMMENT '登录密码',
                                  `entry_time` date NOT NULL COMMENT '入职时间',
                                  `job_number` varchar(50)  NOT NULL COMMENT '工号',
                                  `sex` tinyint(1) NOT NULL COMMENT '性别 0男 1女',
                                  `role_id` bigint NOT NULL COMMENT '员工角色ID',
                                  `status` tinyint(1) NOT NULL DEFAULT '0' COMMENT '账号状态 0正常 1禁用',
                                  `create_time` timestamp NOT NULL COMMENT '创建时间',
                                  `create_by_id` bigint NOT NULL COMMENT '创建人ID',
                                  `update_time` timestamp NULL DEFAULT NULL COMMENT '更新时间',
                                  `update_by_id` bigint DEFAULT NULL COMMENT '更新人ID',
                                  PRIMARY KEY (`id`)
) ENGINE=InnoDB   COMMENT='门店员工表';




CREATE TABLE IF NOT EXISTS `wps_order` (
                             `order_no` varchar(32)  NOT NULL COMMENT '订单编号',
                             `order_status` int NOT NULL DEFAULT '0' COMMENT '订单状态 0待支付 1已支付 2已完成 3已关闭 4已取消',
                             `refund_status` int DEFAULT '0' COMMENT '退款状态 0未退款 1已退款',
                             `create_time` timestamp NOT NULL COMMENT '订单创建时间',
                             `create_staff_id` bigint NOT NULL COMMENT '订单创建人',
                             `pay_time` timestamp NULL DEFAULT NULL COMMENT '订单支付时间',
                             `refund_time` timestamp NULL DEFAULT NULL COMMENT '退款时间',
                             `refund_staff_id` bigint DEFAULT NULL COMMENT '订单退款人',
                             `cancel_time` timestamp NULL DEFAULT NULL COMMENT '订单取消时间',
                             `cancel_staff_id` bigint DEFAULT NULL COMMENT '订单取消人',
                             `total_count` bigint NOT NULL COMMENT '订单总数量',
                             `total_amount` decimal(11,2) NOT NULL COMMENT '订单总金额',
                             `pay_total_amount` decimal(11,2) NOT NULL COMMENT '应付总金额',
                             `grade_discount` decimal(11,2) NOT NULL DEFAULT '1.00' COMMENT '折扣率',
                             `discount_amount` decimal(11,2) NOT NULL DEFAULT '0.00' COMMENT '折扣金额',
                             `coupon_id` bigint DEFAULT NULL COMMENT '优惠券ID',
                             `coupon_amount` decimal(11,2) NOT NULL DEFAULT '0.00' COMMENT '优惠券优惠金额',
                             `member_id` bigint DEFAULT NULL COMMENT '会员ID',
                             `pay_way_id` bigint DEFAULT NULL COMMENT '付款方式ID',
                             `shop_gate_id` bigint NOT NULL COMMENT '门店ID',
                             `real_pay_total_amount` decimal(11,2) DEFAULT NULL COMMENT '实际支付金额 针对现金支付',
                             `give_change_amount` decimal(11,2) DEFAULT NULL COMMENT '找零金额  针对现金支付',
                             `auth_code` varchar(50)  DEFAULT NULL COMMENT '付款码',
                             `pay_code` varchar(50)  DEFAULT NULL COMMENT '支付返回码',
                             `pay_msg` varchar(255)  DEFAULT NULL COMMENT '支付返回说明',
                             `refund_code` varchar(50)  DEFAULT NULL COMMENT '退款返回码',
                             `refund_msg` varchar(255)  DEFAULT NULL COMMENT '退款返回说明',
                             PRIMARY KEY (`order_no`)
) ENGINE=InnoDB  COMMENT='wps订单';


CREATE TABLE IF NOT EXISTS `wps_order_item` (
                                  `id` bigint NOT NULL AUTO_INCREMENT,
                                  `order_no` varchar(32)  NOT NULL COMMENT '订单编号',
                                  `goods_id` bigint NOT NULL COMMENT '商品ID',
                                  `count` bigint NOT NULL COMMENT '商品数量',
                                  `goods_price` decimal(11,2) NOT NULL COMMENT '商品价格',
                                  PRIMARY KEY (`id`)
) ENGINE=InnoDB  COMMENT='wps订单详细信息';


CREATE TABLE IF NOT EXISTS `wps_payment_way` (
                                   `id` bigint NOT NULL AUTO_INCREMENT,
                                   `payment_name` varchar(50)  NOT NULL COMMENT '支付方式',
                                   `payment_scene` varchar(255)  DEFAULT NULL COMMENT '支付场景',
                                   PRIMARY KEY (`id`)
) ENGINE=InnoDB  COMMENT='支付方式';


CREATE TABLE IF NOT EXISTS `wps_payment_way_config` (
                                          `payment_way_id` bigint NOT NULL COMMENT '支付方式ID',
                                          `shop_gate_id` bigint NOT NULL COMMENT '门店ID',
                                          `zfb_app_id` varchar(200)  DEFAULT NULL COMMENT '支付宝appId',
                                          `zfb_private_key` text  COMMENT '支付宝商家应用私钥',
                                          `zfb_public_key` text  COMMENT '支付宝应用公钥',
                                          `wx_app_id` varchar(200)  DEFAULT NULL COMMENT '微信appId',
                                          `wx_account` varchar(500)  DEFAULT NULL COMMENT '微信支付商家号',
                                          `wx_api_secret_key` text  COMMENT '微信API密钥',
                                          `wx_app_secret` text  COMMENT '微信appSecret',
                                          `pay_status` tinyint(1) DEFAULT '0' COMMENT '状态 0可用 1不可用',
                                          `update_time` timestamp NOT NULL COMMENT '更新时间',
                                          `update_by_id` bigint NOT NULL COMMENT '更新人ID',
                                          PRIMARY KEY (`payment_way_id`,`shop_gate_id`) USING BTREE
) ENGINE=InnoDB  COMMENT='支付方式配置';

CREATE TABLE IF NOT EXISTS `wps_role` (
                            `id` bigint NOT NULL AUTO_INCREMENT,
                            `role_name` varchar(50)  NOT NULL COMMENT '角色名称',
                            `description` varchar(100)  NOT NULL COMMENT '角色说明',
                            PRIMARY KEY (`id`)
) ENGINE=InnoDB  COMMENT='角色表';


CREATE TABLE IF NOT EXISTS `wps_staff_login` (
                                   `id` bigint NOT NULL AUTO_INCREMENT,
                                   `gate_staff_id` bigint NOT NULL COMMENT '门店员工ID',
                                   `token` varchar(200)  NOT NULL COMMENT '登录标识',
                                   `login_start_time` timestamp NOT NULL COMMENT '登录开始时间',
                                   `login_end_time` timestamp NULL DEFAULT NULL COMMENT '登录结束时间',
                                   `total_money` decimal(11,2) DEFAULT NULL COMMENT '收银总额',
                                   `xj_money` decimal(11,2) DEFAULT NULL COMMENT '现金支付金额',
                                   `ye_money` decimal(11,2) DEFAULT NULL COMMENT '余额支付金额',
                                   `wx_money` decimal(11,2) DEFAULT NULL COMMENT '微信支付金额',
                                   `zfb_money` decimal(11,2) DEFAULT NULL COMMENT '支付宝支付金额',
                                   `paid_in_money` decimal(11,2) DEFAULT NULL COMMENT '实缴金额',
                                   `change_shifts_status` tinyint(1) DEFAULT '0' COMMENT '是否交接班 0未交接班 1已交接班',
                                   PRIMARY KEY (`id`)
) ENGINE=InnoDB  COMMENT='员工登录、交接班信息表';





SET FOREIGN_KEY_CHECKS=1;
