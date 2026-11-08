-- =====================================================================
-- 插件 erp 的数据库建表脚本（共 159 张表）
-- 用途：安装「erp」插件后按需执行（也可由插件自身的迁移通道建表）。
-- 🔴 商城核心（Core）初始化不包含本文件；未安装该插件时不要执行。
-- 全部 CREATE TABLE 已统一补 IF NOT EXISTS（幂等，可重复执行，不会删数据）。
-- 执行期间临时关闭外键检查：插件之间存在跨插件外键（如 oa 引用 hou_pm_project），
-- 未同时安装被引用插件时允许该外键暂时悬空。
-- =====================================================================

SET FOREIGN_KEY_CHECKS=0;

-- ---------- 基础表结构（结构基线） ----------
-- Table structure for table `hou_erp_accounts_copewith`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_erp_accounts_copewith` (
  `id` int NOT NULL AUTO_INCREMENT,
  `admin_id` int DEFAULT NULL,
  `suppliers_id` int NOT NULL DEFAULT '0' COMMENT '供应商',
  `purchase_time` int DEFAULT NULL COMMENT '单据日期',
  `purchase_code` varchar(100) NOT NULL DEFAULT '' COMMENT '单据编号',
  `business_type` enum('0','1','2','3','4','5','6','7','8','9','10') DEFAULT NULL COMMENT '业务类型:0=期初余额,1=预收冲应收,2=预付冲应付,3=应收冲应付,4=应收转应收,5=应付转应付,6=普通采购,7=采购退回,8=其他支出,9=付款,10=核销',
  `receivable` double(15,4) DEFAULT '0.0000' COMMENT '增加应付款',
  `advance` double(15,4) DEFAULT '0.0000' COMMENT '增加预付款',
  `remark` varchar(255) DEFAULT NULL COMMENT '备注',
  `sale_money` double(15,4) DEFAULT NULL COMMENT '购货金额',
  `receivable_money` double(15,4) DEFAULT NULL COMMENT '应付金额',
  `amount` double(15,4) DEFAULT NULL COMMENT '优惠金额',
  `cost` double(15,4) DEFAULT NULL,
  `real_money` double(15,4) DEFAULT NULL COMMENT '实际付款金额',
  `createtime` int DEFAULT NULL COMMENT '创建时间',
  `updatetime` int DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='应付账款明细表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_erp_accounts_log`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_erp_accounts_log` (
  `id` int NOT NULL AUTO_INCREMENT,
  `admin_id` int DEFAULT NULL,
  `account_coding` varchar(100) NOT NULL COMMENT '账户编号',
  `account_name` varchar(100) NOT NULL COMMENT '账户名称',
  `balance` double(15,4) NOT NULL DEFAULT '0.0000' COMMENT '当前余额',
  `income` double(15,4) DEFAULT NULL COMMENT '收入',
  `expenditure` double(15,4) DEFAULT NULL COMMENT '支出',
  `pay_time` int DEFAULT NULL COMMENT '日期',
  `bill_code` varchar(50) DEFAULT NULL COMMENT '单据编号',
  `business_type` tinyint DEFAULT NULL COMMENT '业务类型:1=期初余额,2=普通采购,3=购货付款,4=购货退货,5=销货,6=销货退货,7=收款单,8=付款单,9=其他收入单,10=其他支出单,11=转账单转入,12=转账单转出',
  `supplyer_code` varchar(50) DEFAULT NULL COMMENT '往来单位',
  `customers_id` int DEFAULT NULL COMMENT '往来单位',
  `payment` int DEFAULT NULL COMMENT '收/付款人',
  `remark` varchar(255) DEFAULT NULL COMMENT '备注',
  `createtime` int DEFAULT NULL COMMENT '创建时间',
  `updatetime` int DEFAULT NULL COMMENT '更新时间',
  `deletetime` int DEFAULT NULL COMMENT '删除时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='账户记录表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_erp_accounts_receivable`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_erp_accounts_receivable` (
  `id` int NOT NULL AUTO_INCREMENT,
  `admin_id` int DEFAULT NULL,
  `customers_id` int NOT NULL DEFAULT '0' COMMENT '客户',
  `purchase_time` int DEFAULT NULL COMMENT '单据日期',
  `purchase_code` varchar(100) NOT NULL DEFAULT '' COMMENT '单据编号',
  `business_type` enum('0','1','2','3','4','5','6','7','8','9','10') DEFAULT NULL COMMENT '业务类型:0=期初余额,1=预收冲应收,2=预付冲应付,3=应收冲应付,4=应收转应收,5=应付转应付,6=普通销售,7=销售退回,8=其他收入,9=收款,10=核销',
  `receivable` double(15,4) DEFAULT NULL COMMENT '增加应收款',
  `advance` double(15,4) DEFAULT NULL COMMENT '增加预收款',
  `staff_member_id` int DEFAULT NULL COMMENT '销售人员',
  `remark` varchar(255) DEFAULT NULL COMMENT '备注',
  `sale_money` double(15,4) DEFAULT NULL COMMENT '销售金额',
  `receivable_money` double(15,4) DEFAULT NULL COMMENT '应收金额',
  `amount` double(15,4) DEFAULT NULL COMMENT '优惠金额',
  `cost` double(15,4) DEFAULT NULL COMMENT '客户承担费用',
  `real_money` double(15,4) DEFAULT NULL COMMENT '实际收款金额',
  `createtime` int DEFAULT NULL COMMENT '创建时间',
  `updatetime` int DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='应收账款明细表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_erp_accounts_transfer`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_erp_accounts_transfer` (
  `id` int NOT NULL AUTO_INCREMENT,
  `admin_id` int DEFAULT NULL,
  `purchase_code` varchar(50) DEFAULT NULL COMMENT '单据编号',
  `purchase_time` int DEFAULT NULL COMMENT '转账日期',
  `money` double(15,4) DEFAULT NULL COMMENT '合计金额',
  `remark` varchar(255) DEFAULT NULL COMMENT '备注',
  `preparation_id` int DEFAULT NULL COMMENT '制单人',
  `operation_id` int DEFAULT NULL COMMENT '最后操作人',
  `examine_id` int DEFAULT NULL COMMENT '审核人',
  `examine_status` enum('0','1') DEFAULT '0' COMMENT '审核状态:0=未审核,1=已审核',
  `examine_time` int DEFAULT NULL COMMENT '审核时间',
  `print_num` int DEFAULT NULL COMMENT '打印次数',
  `pay_status` enum('0','1','2') DEFAULT '0' COMMENT '付款状态:0=未付款,1=全部付款,2=部分付款',
  `createtime` int DEFAULT NULL COMMENT '创建时间',
  `updatetime` int DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='资金转账单';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_erp_accounts_transfer_info`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_erp_accounts_transfer_info` (
  `id` int NOT NULL AUTO_INCREMENT,
  `admin_id` int DEFAULT NULL,
  `purchase_code` varchar(50) DEFAULT NULL COMMENT '单据编号',
  `purchase_id` int DEFAULT NULL COMMENT '转账单ID',
  `purchase_time` int DEFAULT NULL COMMENT '转账日期',
  `accounts_out_id` int DEFAULT NULL COMMENT '转出账户',
  `accounts_enter_id` int DEFAULT NULL COMMENT '转入账户',
  `settlementtype_id` int DEFAULT NULL COMMENT '结算方式',
  `settlement_number` varchar(50) DEFAULT NULL COMMENT '结算号',
  `money` double(15,4) DEFAULT NULL COMMENT '金额',
  `remark` varchar(255) DEFAULT NULL COMMENT '备注',
  `createtime` int DEFAULT NULL COMMENT '创建时间',
  `updatetime` int DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='转账单详情';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_erp_address`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_erp_address` (
  `id` int NOT NULL AUTO_INCREMENT,
  `admin_id` int DEFAULT NULL,
  `nickname` varchar(100) DEFAULT NULL COMMENT '地址简称',
  `connect_name` varchar(100) NOT NULL COMMENT '联系人',
  `connect_phone` varchar(20) NOT NULL COMMENT '联系电话',
  `connect_mobile` varchar(11) NOT NULL COMMENT '手机号码',
  `province` int NOT NULL DEFAULT '0' COMMENT '省',
  `city` int NOT NULL DEFAULT '0' COMMENT '市',
  `area` int NOT NULL DEFAULT '0' COMMENT '区',
  `detail_address` varchar(255) NOT NULL COMMENT '详细地址',
  `post_code` varchar(50) NOT NULL COMMENT '邮政编码',
  `is_default` enum('0','1') NOT NULL DEFAULT '0' COMMENT '默认地址:0=否,1=是',
  `createtime` int DEFAULT NULL COMMENT '创建时间',
  `updatetime` int DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='发货地址表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_erp_adjustment`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_erp_adjustment` (
  `id` int NOT NULL AUTO_INCREMENT,
  `admin_id` int DEFAULT NULL,
  `purchase_code` varchar(50) DEFAULT NULL COMMENT '单据编号',
  `purchase_time` int DEFAULT NULL COMMENT '单据日期',
  `business_type` enum('1') DEFAULT NULL COMMENT '业务类型:1=成本调整',
  `money` double(15,4) DEFAULT NULL COMMENT '金额',
  `preparation_id` int DEFAULT NULL COMMENT '制单人',
  `remark` varchar(255) DEFAULT NULL COMMENT '备注',
  `print_num` int DEFAULT NULL COMMENT '打印次数',
  `createtime` int DEFAULT NULL COMMENT '创建时间',
  `updatetime` int DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='成本调整单';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_erp_adjustment_info`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_erp_adjustment_info` (
  `id` int NOT NULL AUTO_INCREMENT,
  `admin_id` int DEFAULT NULL,
  `purchase_code` varchar(50) DEFAULT NULL COMMENT '单据编号',
  `purchase_time` int DEFAULT NULL COMMENT '单据日期',
  `goods_id` int DEFAULT NULL COMMENT '商品ID',
  `goods_unit` int DEFAULT '0' COMMENT '单位',
  `money` double(15,4) DEFAULT NULL COMMENT '金额',
  `stores_id` int DEFAULT NULL COMMENT '仓库',
  `remark` varchar(255) DEFAULT NULL COMMENT '备注',
  `createtime` int DEFAULT NULL COMMENT '创建时间',
  `updatetime` int DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='成本调整单详情';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_erp_allocation_info`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_erp_allocation_info` (
  `id` int NOT NULL AUTO_INCREMENT,
  `admin_id` int DEFAULT NULL,
  `allocation_id` varchar(50) NOT NULL DEFAULT '' COMMENT '调拨单ID',
  `goods_id` int DEFAULT NULL COMMENT '商品ID',
  `goods_attr` int DEFAULT NULL COMMENT '商品属性',
  `goods_unit` int DEFAULT NULL COMMENT '商品单位',
  `stores_out` int DEFAULT NULL COMMENT '调出仓库',
  `stores_enter` int DEFAULT NULL COMMENT '调入仓库',
  `number` int DEFAULT NULL COMMENT '数量',
  `goods_batch_id` int DEFAULT NULL COMMENT '批次ID',
  `goods_batch_name` varchar(255) DEFAULT NULL COMMENT '批次',
  `goods_serial_id` text COMMENT '序列号',
  `remarks` varchar(255) DEFAULT NULL COMMENT '分录备注',
  `allow_stock` double(15,4) DEFAULT '0.0000' COMMENT '可用库存',
  `goods_note` varchar(255) DEFAULT NULL COMMENT '商品备注',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='调拨单详情';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_erp_area`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_erp_area` (
  `id` int NOT NULL COMMENT 'ID',
  `pid` int DEFAULT NULL COMMENT '父id',
  `shortname` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '简称',
  `name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '名称',
  `level` tinyint DEFAULT NULL COMMENT '层级 0 1 2 省市区县',
  `adcode` varchar(100) DEFAULT NULL COMMENT '地区编号',
  `lng` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '经度',
  `lat` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '纬度'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='地区表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_erp_assemble`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_erp_assemble` (
  `id` int NOT NULL AUTO_INCREMENT,
  `admin_id` int DEFAULT NULL,
  `purchase_code` varchar(50) DEFAULT NULL COMMENT '单据编号',
  `purchase_time` int DEFAULT NULL COMMENT '单据日期',
  `business_type` enum('1','2') DEFAULT NULL COMMENT '业务类型:1=组装单',
  `money` double(15,4) DEFAULT NULL COMMENT '费用',
  `remark` varchar(255) DEFAULT NULL COMMENT '备注',
  `preparation_id` int DEFAULT NULL COMMENT '制单人',
  `operation_id` int DEFAULT NULL COMMENT '最后操作人',
  `examine_id` int DEFAULT NULL COMMENT '审核人',
  `examine_status` enum('0','1') DEFAULT '0' COMMENT '审核状态:0=未审核,1=已审核',
  `examine_time` int DEFAULT NULL COMMENT '审核时间',
  `print_num` int DEFAULT NULL COMMENT '打印次数',
  `createtime` int DEFAULT NULL COMMENT '创建时间',
  `updatetime` int DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='组装单';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_erp_assemble_info`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_erp_assemble_info` (
  `id` int NOT NULL AUTO_INCREMENT,
  `admin_id` int DEFAULT NULL,
  `purchase_code` varchar(50) NOT NULL DEFAULT '' COMMENT '单据编号',
  `purchase_time` int NOT NULL DEFAULT '0' COMMENT '单据日期',
  `business_type` enum('1','2') NOT NULL DEFAULT '1' COMMENT '类型:1=组装单',
  `type` enum('1','2') NOT NULL DEFAULT '2' COMMENT '类型:1=组合件,2=子件',
  `goods_id` int NOT NULL DEFAULT '0' COMMENT '商品ID',
  `goods_code` varchar(50) NOT NULL DEFAULT '' COMMENT '商品编号',
  `goods_attr` int DEFAULT NULL COMMENT '商品属性',
  `goods_unit` int NOT NULL DEFAULT '0' COMMENT '商品单位',
  `stores_id` int NOT NULL DEFAULT '0' COMMENT '仓库',
  `allow_stock` double(15,4) DEFAULT '0.0000' COMMENT '可用库存',
  `number` double(15,4) DEFAULT '0.0000' COMMENT '数量',
  `period` text COMMENT '序列号',
  `basic_number` double(15,4) DEFAULT '0.0000' COMMENT '基本数量',
  `unit_cost` double DEFAULT '0' COMMENT '单位成本',
  `cost_money` double DEFAULT '0' COMMENT '成本',
  `remark` varchar(255) DEFAULT NULL COMMENT '备注',
  `batch_id` int DEFAULT NULL COMMENT '批次ID',
  `batch` varchar(255) DEFAULT NULL COMMENT '批次',
  `manufacture` int DEFAULT NULL COMMENT '生产日期',
  `guarantee` int DEFAULT NULL COMMENT '保质期',
  `validity` int DEFAULT NULL COMMENT '过期时间',
  `preparation_id` int DEFAULT NULL COMMENT '制单人',
  `operation_id` int DEFAULT NULL COMMENT '最后操作人',
  `examine_id` int DEFAULT NULL COMMENT '审核人',
  `examine_status` enum('0','1') DEFAULT '0' COMMENT '审核状态:0=未审核,1=已审核',
  `examine_time` int DEFAULT NULL COMMENT '审核时间',
  `print_num` int DEFAULT '0' COMMENT '打印次数',
  `createtime` int DEFAULT NULL COMMENT '创建时间',
  `updatetime` int DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='组装单详情';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_erp_assemble_info_temp`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_erp_assemble_info_temp` (
  `id` int NOT NULL AUTO_INCREMENT,
  `admin_id` int DEFAULT NULL,
  `purchase_id` int DEFAULT NULL,
  `purchase_code` varchar(50) NOT NULL DEFAULT '' COMMENT '单据编号',
  `purchase_time` int NOT NULL DEFAULT '0' COMMENT '单据日期',
  `business_type` enum('1','2') NOT NULL DEFAULT '1' COMMENT '类型:1=组装单',
  `type` enum('1','2') NOT NULL DEFAULT '2' COMMENT '类型:1=组合件,2=子件',
  `goods_id` int NOT NULL DEFAULT '0' COMMENT '商品ID',
  `goods_code` varchar(50) NOT NULL DEFAULT '' COMMENT '商品编号',
  `goods_attr` int DEFAULT NULL COMMENT '商品属性',
  `goods_unit` int NOT NULL DEFAULT '0' COMMENT '商品单位',
  `stores_id` int NOT NULL DEFAULT '0' COMMENT '仓库',
  `allow_stock` double(15,4) DEFAULT '0.0000' COMMENT '可用库存',
  `number` double(15,4) DEFAULT '0.0000' COMMENT '数量',
  `period` text COMMENT '序列号',
  `basic_number` double(15,4) DEFAULT '0.0000' COMMENT '基本数量',
  `unit_cost` double DEFAULT '0' COMMENT '单位成本',
  `cost_money` double DEFAULT '0' COMMENT '成本',
  `remark` varchar(255) DEFAULT NULL COMMENT '备注',
  `batch_id` int DEFAULT NULL COMMENT '批次ID',
  `batch` varchar(255) DEFAULT NULL COMMENT '批次',
  `manufacture` int DEFAULT NULL COMMENT '生产日期',
  `guarantee` int DEFAULT NULL COMMENT '保质期',
  `validity` int DEFAULT NULL COMMENT '过期时间',
  `preparation_id` int DEFAULT NULL COMMENT '制单人',
  `operation_id` int DEFAULT NULL COMMENT '最后操作人',
  `examine_id` int DEFAULT NULL COMMENT '审核人',
  `examine_status` enum('0','1') DEFAULT '0' COMMENT '审核状态:0=未审核,1=已审核',
  `examine_time` int DEFAULT NULL COMMENT '审核时间',
  `print_num` int DEFAULT '0' COMMENT '打印次数',
  `createtime` int DEFAULT NULL COMMENT '创建时间',
  `updatetime` int DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='组装单详情模板';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_erp_assemble_temp`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_erp_assemble_temp` (
  `id` int NOT NULL AUTO_INCREMENT,
  `admin_id` int DEFAULT NULL,
  `temp_name` varchar(255) NOT NULL DEFAULT '' COMMENT '模板名称',
  `purchase_code` varchar(50) DEFAULT NULL COMMENT '单据编号',
  `purchase_time` int DEFAULT NULL COMMENT '单据日期',
  `business_type` enum('1','2') DEFAULT NULL COMMENT '业务类型:1=组装单',
  `money` double(15,4) DEFAULT NULL COMMENT '费用',
  `remark` varchar(255) DEFAULT NULL COMMENT '备注',
  `preparation_id` int DEFAULT NULL COMMENT '制单人',
  `operation_id` int DEFAULT NULL COMMENT '最后操作人',
  `examine_id` int DEFAULT NULL COMMENT '审核人',
  `examine_status` enum('0','1') DEFAULT '0' COMMENT '审核状态:0=未审核,1=已审核',
  `examine_time` int DEFAULT NULL COMMENT '审核时间',
  `print_num` int DEFAULT NULL COMMENT '打印次数',
  `createtime` int DEFAULT NULL COMMENT '创建时间',
  `updatetime` int DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='组装单模板';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_erp_attr_combination`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_erp_attr_combination` (
  `id` int NOT NULL AUTO_INCREMENT,
  `attr_name` varchar(255) DEFAULT NULL,
  `attr_ids` varchar(255) NOT NULL DEFAULT '' COMMENT '属性组合ID(从小到大)',
  `attr_code` varchar(30) NOT NULL DEFAULT '' COMMENT '属性编号',
  `createtime` int DEFAULT NULL COMMENT '创建时间',
  `updatetime` int DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='属性组合';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_erp_attribute`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_erp_attribute` (
  `id` int NOT NULL AUTO_INCREMENT,
  `name` varchar(100) DEFAULT NULL COMMENT '属性名称',
  `createtime` int DEFAULT NULL COMMENT '创建时间',
  `updatetime` int DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='属性';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_erp_aux_combination`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_erp_aux_combination` (
  `id` int NOT NULL AUTO_INCREMENT,
  `admin_id` int unsigned DEFAULT NULL,
  `attr_ids` varchar(255) NOT NULL DEFAULT '' COMMENT '属性组合ID(从小到大)',
  `attr_code` varchar(30) NOT NULL DEFAULT '' COMMENT '属性编号',
  `createtime` int DEFAULT NULL COMMENT '创建时间',
  `updatetime` int DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='辅助属性组合';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_erp_auxiliary`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_erp_auxiliary` (
  `id` int NOT NULL AUTO_INCREMENT,
  `admin_id` int DEFAULT NULL,
  `name` varchar(120) NOT NULL DEFAULT '' COMMENT '属性名称',
  `image` varchar(255) NOT NULL DEFAULT '' COMMENT '图片',
  `pid` int NOT NULL DEFAULT '0' COMMENT '上级',
  `path` varchar(255) DEFAULT NULL COMMENT 'path',
  `weigh` int DEFAULT NULL,
  `status` enum('0','1') NOT NULL DEFAULT '1' COMMENT '状态:0=禁用,1=正常',
  `createtime` int DEFAULT NULL COMMENT '创建时间',
  `updatetime` int DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='商品辅助属性';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_erp_banner`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_erp_banner` (
  `id` int NOT NULL AUTO_INCREMENT,
  `type_id` int DEFAULT NULL,
  `name` varchar(120) NOT NULL DEFAULT '' COMMENT '名称',
  `image` varchar(255) NOT NULL DEFAULT '' COMMENT '图片',
  `link` varchar(255) DEFAULT '' COMMENT '链接',
  `status` enum('0','1') NOT NULL DEFAULT '1' COMMENT '状态:0=禁用,1=正常',
  `createtime` int DEFAULT NULL COMMENT '创建时间',
  `updatetime` int DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='轮播表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_erp_banner_type`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_erp_banner_type` (
  `id` int NOT NULL AUTO_INCREMENT,
  `name` varchar(120) NOT NULL DEFAULT '' COMMENT '名称',
  `type` tinyint NOT NULL DEFAULT '0' COMMENT '广告位',
  `status` enum('0','1') NOT NULL DEFAULT '1' COMMENT '状态:0=禁用,1=正常',
  `createtime` int DEFAULT NULL COMMENT '创建时间',
  `updatetime` int DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='轮播位表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_erp_checkout`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_erp_checkout` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `checkdate` varchar(50) DEFAULT NULL COMMENT '结账日期',
  `checktime` int DEFAULT NULL COMMENT '结账日期',
  `preparation_id` int DEFAULT NULL COMMENT '操作人',
  `preparation_time` int DEFAULT NULL COMMENT '操作日期',
  `createtime` int DEFAULT NULL COMMENT '创建时间',
  `updatetime` int DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='结账表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_erp_checkout_info`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_erp_checkout_info` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `checkout_id` int DEFAULT NULL COMMENT '结账ID',
  `goods_id` int DEFAULT NULL COMMENT '商品名称',
  `goods_code` varchar(50) DEFAULT NULL COMMENT '商品编号',
  `basic_nmber` decimal(15,4) DEFAULT '0.0000' COMMENT '数量',
  `all_money` decimal(15,4) DEFAULT '0.0000' COMMENT '成本',
  `purchase_id` int DEFAULT NULL COMMENT '结账截止购货ID',
  `purchase_time` int DEFAULT NULL COMMENT '单据时间',
  `av_basic_nmber` decimal(15,4) DEFAULT '0.0000' COMMENT '移动平均数量',
  `av_all_money` decimal(15,4) DEFAULT '0.0000' COMMENT '移动平均成本',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='结账详情表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_erp_collection`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_erp_collection` (
  `id` int NOT NULL AUTO_INCREMENT,
  `admin_id` int DEFAULT NULL,
  `purchase_code` varchar(50) DEFAULT NULL COMMENT '单据编号',
  `purchase_time` int DEFAULT NULL COMMENT '单据时间',
  `business_type` enum('1','2') DEFAULT '1' COMMENT '类型:1=预收单,2=收款单',
  `customers_id` int DEFAULT NULL COMMENT '客户',
  `money` double(15,4) DEFAULT NULL COMMENT '收款合计',
  `write_off` double(15,4) DEFAULT NULL COMMENT '核销金额',
  `discount` double(15,4) DEFAULT NULL COMMENT '整单折扣',
  `advance` double(15,4) DEFAULT NULL COMMENT '预收款',
  `staff_member_id` int unsigned DEFAULT NULL COMMENT '收款人',
  `remarks` varchar(255) DEFAULT NULL COMMENT '备注',
  `preparation_id` int DEFAULT NULL COMMENT '制单人',
  `examine_id` int DEFAULT NULL COMMENT '审核人',
  `operation_id` int DEFAULT NULL COMMENT '最后操作人',
  `examine_time` int DEFAULT NULL COMMENT '审核时间',
  `examine_status` enum('0','1') NOT NULL DEFAULT '0' COMMENT '审核状态:0=未审核,1=已审核',
  `print_num` int DEFAULT NULL COMMENT '打印次数',
  `createtime` int DEFAULT NULL COMMENT '创建时间',
  `updatetime` int DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='收款单';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_erp_collection_account`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_erp_collection_account` (
  `id` int NOT NULL AUTO_INCREMENT,
  `admin_id` int DEFAULT NULL,
  `collection_id` int NOT NULL DEFAULT '0' COMMENT '收款单ID',
  `purchase_code` varchar(50) NOT NULL DEFAULT '' COMMENT '单据编号',
  `purchase_time` int NOT NULL DEFAULT '0' COMMENT '单据时间',
  `accounts_id` int unsigned NOT NULL DEFAULT '0' COMMENT '结算账户',
  `money` double(15,4) NOT NULL DEFAULT '0.0000' COMMENT '收款金额',
  `settlementtype` int unsigned NOT NULL DEFAULT '0' COMMENT '结算方式',
  `number` varchar(50) DEFAULT '' COMMENT '结算号',
  `remark` varchar(255) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='收款账户信息';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_erp_collection_sale`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_erp_collection_sale` (
  `id` int NOT NULL AUTO_INCREMENT,
  `admin_id` int DEFAULT NULL,
  `collection_id` int NOT NULL DEFAULT '0' COMMENT '收款单ID',
  `purchase_code` varchar(50) NOT NULL DEFAULT '' COMMENT '单据编号',
  `purchase_time` int NOT NULL DEFAULT '0' COMMENT '单据时间',
  `purchase_number` varchar(50) NOT NULL DEFAULT '' COMMENT '原单编号',
  `business_type` enum('1','2','3','4','5','6') NOT NULL DEFAULT '1' COMMENT '业务类别:1=预收单,2=预付单,3=应收,4=应付,5=期初应收/付款,6=期初预收/付款',
  `times` int NOT NULL DEFAULT '0' COMMENT '单据日期',
  `money` double(15,4) NOT NULL DEFAULT '0.0000' COMMENT '单据金额',
  `write_off` double(15,4) DEFAULT NULL COMMENT '已核销金额',
  `write_off_money` double(15,4) DEFAULT NULL COMMENT '未核销金额',
  `write_off_price` double(15,4) DEFAULT '0.0000' COMMENT '本次核销金额',
  `remark` varchar(255) DEFAULT NULL COMMENT '单据备注',
  `old_money` double(15,4) DEFAULT NULL,
  `old_status` tinyint DEFAULT NULL,
  `old_status_i` tinyint DEFAULT NULL,
  `business_name` varchar(255) DEFAULT NULL COMMENT '单据名称',
  `source_id` int NOT NULL DEFAULT '0' COMMENT '源单ID',
  `type` enum('1','2','3','4') DEFAULT NULL COMMENT '类型:1=购货/销货,2=退货,3=期初,4=核销',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='收款单单据信息';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_erp_config`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_erp_config` (
  `id` int unsigned NOT NULL AUTO_INCREMENT,
  `admin_id` int DEFAULT NULL,
  `name` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT '' COMMENT '变量名',
  `group` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT '' COMMENT '分组',
  `title` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT '' COMMENT '变量标题',
  `tip` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT '' COMMENT '变量描述',
  `type` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT '' COMMENT '类型:string,text,int,bool,array,datetime,date,file',
  `value` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci COMMENT '变量值',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci COMMENT '变量字典数据',
  `rule` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT '' COMMENT '验证规则',
  `extend` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT '' COMMENT '扩展属性',
  `setting` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT '' COMMENT '配置',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `name` (`name`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='系统配置';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_erp_customer_goodscoding`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_erp_customer_goodscoding` (
  `id` int NOT NULL AUTO_INCREMENT,
  `admin_id` int DEFAULT NULL,
  `customers_id` int NOT NULL DEFAULT '0' COMMENT '客户',
  `goods_code` varchar(30) NOT NULL DEFAULT '' COMMENT '关联商品',
  `unit_id` int DEFAULT NULL COMMENT '单位',
  `customer_code` varchar(50) NOT NULL DEFAULT '' COMMENT '客户物料编码',
  `createtime` int DEFAULT NULL COMMENT '创建时间',
  `updatetime` int DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='客户物料编码';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_erp_customerlevel`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_erp_customerlevel` (
  `id` int NOT NULL AUTO_INCREMENT,
  `admin_id` int DEFAULT NULL,
  `name` varchar(100) NOT NULL DEFAULT '' COMMENT '等级名称',
  `status` enum('0','1') NOT NULL DEFAULT '0' COMMENT '状态:0=禁用,1=正常',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='客户等级';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_erp_customers_user`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_erp_customers_user` (
  `id` int NOT NULL AUTO_INCREMENT,
  `admin_id` int DEFAULT NULL,
  `customers_code` varchar(30) NOT NULL DEFAULT '' COMMENT '客户编号',
  `sid` int unsigned DEFAULT NULL COMMENT '客户ID',
  `first_connect_name` varchar(100) NOT NULL DEFAULT '' COMMENT '联系人名称',
  `first_connect_mobile` varchar(11) NOT NULL COMMENT '手机',
  `first_connect_phone` varchar(20) NOT NULL COMMENT '座机',
  `qq_wx_email` varchar(120) NOT NULL COMMENT 'QQ/微信/Email',
  `connect_address` varchar(255) NOT NULL COMMENT '联系地址',
  `is_first` tinyint DEFAULT '0' COMMENT '首要联系人:0=否,1=是',
  `status` enum('0','1') NOT NULL DEFAULT '1' COMMENT '状态:0=禁用,1=正常',
  `createtime` int DEFAULT NULL COMMENT '创建时间',
  `updatetime` int DEFAULT NULL COMMENT '更新时间',
  `deletetime` int DEFAULT NULL COMMENT '删除时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='客户联系人';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_erp_customertype`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_erp_customertype` (
  `id` int NOT NULL AUTO_INCREMENT,
  `admin_id` int DEFAULT NULL,
  `type_name` varchar(100) NOT NULL COMMENT '分类名称',
  `pid` int NOT NULL DEFAULT '0' COMMENT '上级分类id',
  `path` varchar(255) DEFAULT NULL,
  `type_image` varchar(255) DEFAULT NULL COMMENT '分类图',
  `status` enum('0','1') NOT NULL DEFAULT '0' COMMENT '状态:0=禁用,1=正常',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='客户分类表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_erp_driver`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_erp_driver` (
  `id` int NOT NULL AUTO_INCREMENT,
  `admin_id` int DEFAULT NULL,
  `name` varchar(100) DEFAULT NULL COMMENT '名称',
  `phone` varchar(20) DEFAULT NULL COMMENT '电话',
  `plate` varchar(100) DEFAULT NULL COMMENT '车牌号',
  `status` enum('0','1') DEFAULT '1' COMMENT '状态:0=禁用,1=正常',
  `createtime` int DEFAULT NULL COMMENT '创建时间',
  `updatetime` int DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='司机表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_erp_enclosure`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_erp_enclosure` (
  `id` int NOT NULL AUTO_INCREMENT,
  `image` varchar(255) NOT NULL DEFAULT '' COMMENT '图片',
  `type` enum('1','2') DEFAULT '1' COMMENT '类型:1=购货,2=销货',
  `status` enum('0','1') DEFAULT '1' COMMENT '状态:0=禁用,1=启用',
  `upload_date` varchar(30) DEFAULT NULL COMMENT '上传日期',
  `createtime` int DEFAULT NULL COMMENT '创建时间',
  `updatetime` int DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='原始单据表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_erp_enclosure_log`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_erp_enclosure_log` (
  `id` int NOT NULL AUTO_INCREMENT,
  `enclosure_id` int NOT NULL DEFAULT '0' COMMENT '附件ID',
  `order_id` int DEFAULT '0' COMMENT '单据ID',
  `purchase_code` varchar(50) DEFAULT NULL COMMENT '单据编号',
  `type` enum('1','2','3','4','5','6') DEFAULT NULL COMMENT '单据类型:1=购货订单,2:购货单,3:购货退货单,4:销货订单,5:销货单,6:销货退货单',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='原始单据关联表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_erp_expend_type`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_erp_expend_type` (
  `id` int NOT NULL AUTO_INCREMENT,
  `admin_id` int DEFAULT NULL,
  `name` varchar(120) NOT NULL DEFAULT '' COMMENT '名称',
  `image` varchar(255) NOT NULL DEFAULT '' COMMENT '图片',
  `pid` int NOT NULL DEFAULT '0' COMMENT '上级分类id',
  `status` enum('0','1') NOT NULL DEFAULT '1' COMMENT '状态:0=禁用,1=正常',
  `path` varchar(255) DEFAULT NULL,
  `createtime` int DEFAULT NULL COMMENT '创建时间',
  `updatetime` int DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='支出类别表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_erp_expenditure_info`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_erp_expenditure_info` (
  `id` int NOT NULL AUTO_INCREMENT,
  `admin_id` int DEFAULT NULL,
  `purchase_code` varchar(50) DEFAULT NULL COMMENT '单据编号',
  `purchase_id` int DEFAULT NULL COMMENT '收入单ID',
  `purchase_time` int DEFAULT NULL COMMENT '单据日期',
  `business_type` enum('1','2') DEFAULT NULL COMMENT '业务类型:1=其他收入',
  `suppliers_id` int DEFAULT NULL COMMENT '供应商',
  `expend_type_id` int DEFAULT NULL COMMENT '支出类别',
  `money` double(15,4) DEFAULT NULL COMMENT '金额',
  `remark` varchar(255) DEFAULT NULL COMMENT '备注',
  `cost_id` int DEFAULT NULL,
  `createtime` int DEFAULT NULL COMMENT '创建时间',
  `updatetime` int DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='其他支出单详情';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_erp_goods_batch`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_erp_goods_batch` (
  `id` int NOT NULL AUTO_INCREMENT,
  `admin_id` int DEFAULT NULL,
  `goods_id` int NOT NULL DEFAULT '0' COMMENT '商品ID',
  `goods_code` varchar(30) DEFAULT NULL COMMENT '商品编号',
  `attribute_id` int DEFAULT '0',
  `stores_id` int NOT NULL DEFAULT '0' COMMENT '仓库ID',
  `batch_name` varchar(255) DEFAULT NULL COMMENT '批次',
  `manufacture` int unsigned DEFAULT NULL COMMENT '生产日期',
  `guarantee` int unsigned DEFAULT NULL COMMENT '保质期',
  `validity` int DEFAULT NULL COMMENT '有效期至',
  `address` varchar(255) DEFAULT NULL COMMENT '产地',
  `registration` varchar(255) DEFAULT NULL COMMENT '注册证号',
  `production` varchar(255) DEFAULT NULL COMMENT '生产许可证',
  `createtime` int DEFAULT NULL COMMENT '创建时间',
  `updatetime` int DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='商品批次表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_erp_goods_batch_exchange_info`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_erp_goods_batch_exchange_info` (
  `id` int NOT NULL AUTO_INCREMENT,
  `admin_id` int DEFAULT NULL,
  `batch_id` int DEFAULT NULL,
  `goods_id` int NOT NULL DEFAULT '0' COMMENT '商品ID',
  `goods_code` varchar(30) DEFAULT NULL COMMENT '商品编号',
  `attribute_id` varchar(30) DEFAULT NULL COMMENT '属性组合ID',
  `goods_unit` int DEFAULT NULL COMMENT '商品单位',
  `stores_id` int NOT NULL DEFAULT '0' COMMENT '仓库ID',
  `stock` double(15,4) unsigned NOT NULL DEFAULT '0.0000' COMMENT '库存',
  `status` enum('0','1') NOT NULL DEFAULT '1' COMMENT '状态:0=关闭,1=正常',
  `number` float(15,4) unsigned DEFAULT '0.0000' COMMENT '期初数量',
  `batch_name` varchar(255) DEFAULT NULL COMMENT '批次',
  `manufacture` int unsigned DEFAULT NULL COMMENT '生产日期',
  `guarantee` int unsigned DEFAULT NULL COMMENT '保质期',
  `validity` int DEFAULT NULL COMMENT '有效期至',
  `purchase_id` int DEFAULT NULL COMMENT '购货单号',
  `purchase_info_id` int DEFAULT NULL COMMENT '购货单详情ID',
  `type` tinyint DEFAULT '0' COMMENT '类型:1=期初,2=购货',
  `createtime` int DEFAULT NULL COMMENT '创建时间',
  `updatetime` int DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='商品退货批次详情表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_erp_goods_batch_info`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_erp_goods_batch_info` (
  `id` int NOT NULL AUTO_INCREMENT,
  `admin_id` int DEFAULT NULL,
  `batch_id` int DEFAULT NULL,
  `goods_id` int NOT NULL DEFAULT '0' COMMENT '商品ID',
  `goods_code` varchar(30) DEFAULT NULL COMMENT '商品编号',
  `attribute_id` int DEFAULT '0',
  `goods_unit` int DEFAULT NULL COMMENT '商品单位',
  `stores_id` int NOT NULL DEFAULT '0' COMMENT '仓库ID',
  `stock` double(15,4) unsigned NOT NULL DEFAULT '0.0000' COMMENT '库存',
  `status` enum('0','1') NOT NULL DEFAULT '1' COMMENT '状态:0=关闭,1=正常',
  `number` float(15,4) unsigned DEFAULT '0.0000' COMMENT '期初数量',
  `batch_name` varchar(255) DEFAULT NULL COMMENT '批次',
  `manufacture` int unsigned DEFAULT NULL COMMENT '生产日期',
  `guarantee` int unsigned DEFAULT NULL COMMENT '保质期',
  `validity` int DEFAULT NULL COMMENT '有效期至',
  `purchase_id` int DEFAULT NULL COMMENT '购货单号',
  `purchase_info_id` int DEFAULT NULL COMMENT '购货单详情ID',
  `assemble_id` int DEFAULT NULL COMMENT '组合单ID',
  `split_id` int DEFAULT NULL COMMENT '拆卸单ID',
  `type` tinyint DEFAULT '0' COMMENT '类型:1=期初,2=购货,3=盘盈,4=其他入库,5=组合单,6=拆卸单',
  `createtime` int DEFAULT NULL COMMENT '创建时间',
  `updatetime` int DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='商品批次详情表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_erp_goods_batch_log`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_erp_goods_batch_log` (
  `id` int NOT NULL AUTO_INCREMENT,
  `admin_id` int DEFAULT NULL,
  `goods_id` int NOT NULL DEFAULT '0' COMMENT '商品ID',
  `unit_price` double(15,4) DEFAULT '0.0000' COMMENT '商品单价',
  `tax_included` double(15,4) DEFAULT '0.0000' COMMENT '含税单价',
  `all_money` double(15,4) DEFAULT '0.0000' COMMENT '金额',
  `tax_money` double(15,4) DEFAULT '0.0000' COMMENT '税额',
  `money_tax_price` double(15,4) DEFAULT '0.0000' COMMENT '价税合计',
  `goods_unit` int DEFAULT NULL COMMENT '商品单位',
  `stores_id` int DEFAULT NULL COMMENT '仓库ID',
  `batch_id` int DEFAULT NULL COMMENT '批次ID',
  `number` double(15,4) DEFAULT '0.0000' COMMENT '数量',
  `purchase_code` varchar(50) DEFAULT NULL COMMENT '单据编号',
  `purchase_time` int DEFAULT NULL COMMENT '单据时间',
  `business_type` enum('1','2','3','4','5','6','7','8','9','10','11','12','13','14','16') DEFAULT NULL COMMENT '业务类型:1=期初余额,2=普通采购,3=采购退回,4=普通销售,5=销售退回,6=调拨单,7=盘盈,8=其他入库,9=盘亏,10=其他出库,11=组合单,12=组合单,13=拆卸单,14=拆卸单,16=生产',
  `customer_id` int DEFAULT NULL COMMENT '客户/供应商',
  `sale_id` int DEFAULT NULL COMMENT '销售人员',
  `remarks` varchar(255) DEFAULT NULL COMMENT '备注',
  `order_id` int DEFAULT NULL COMMENT '来源关联ID',
  `createtime` int DEFAULT NULL COMMENT '创建时间',
  `updatetime` int DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='批次跟踪表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_erp_goods_batch_sale_info`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_erp_goods_batch_sale_info` (
  `id` int NOT NULL AUTO_INCREMENT,
  `admin_id` int DEFAULT NULL,
  `batch_id` int DEFAULT NULL,
  `goods_id` int NOT NULL DEFAULT '0' COMMENT '商品ID',
  `goods_code` varchar(30) DEFAULT NULL COMMENT '商品编号',
  `attribute_id` varchar(30) DEFAULT NULL COMMENT '属性组合ID',
  `goods_unit` int DEFAULT NULL COMMENT '商品单位',
  `stores_id` int NOT NULL DEFAULT '0' COMMENT '仓库ID',
  `stock` double(15,4) unsigned NOT NULL DEFAULT '0.0000' COMMENT '库存',
  `status` enum('0','1') NOT NULL DEFAULT '1' COMMENT '状态:0=关闭,1=正常',
  `number` float(15,4) unsigned DEFAULT '0.0000' COMMENT '期初数量',
  `batch_name` varchar(255) DEFAULT NULL COMMENT '批次',
  `manufacture` int unsigned DEFAULT NULL COMMENT '生产日期',
  `guarantee` int unsigned DEFAULT NULL COMMENT '保质期',
  `validity` int DEFAULT NULL COMMENT '有效期至',
  `purchase_id` int DEFAULT NULL COMMENT '购货单号',
  `purchase_info_id` int DEFAULT NULL COMMENT '购货单详情ID',
  `type` tinyint DEFAULT '0' COMMENT '类型:1=期初,2=购货',
  `createtime` int DEFAULT NULL COMMENT '创建时间',
  `updatetime` int DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='商品销售批次详情表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_erp_goods_batch_salee_info`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_erp_goods_batch_salee_info` (
  `id` int NOT NULL AUTO_INCREMENT,
  `admin_id` int DEFAULT NULL,
  `batch_id` int DEFAULT NULL,
  `goods_id` int NOT NULL DEFAULT '0' COMMENT '商品ID',
  `goods_code` varchar(30) DEFAULT NULL COMMENT '商品编号',
  `attribute_id` varchar(30) DEFAULT NULL COMMENT '属性组合ID',
  `goods_unit` int DEFAULT NULL COMMENT '商品单位',
  `stores_id` int NOT NULL DEFAULT '0' COMMENT '仓库ID',
  `stock` double(15,4) unsigned NOT NULL DEFAULT '0.0000' COMMENT '库存',
  `status` enum('0','1') NOT NULL DEFAULT '1' COMMENT '状态:0=关闭,1=正常',
  `number` float(15,4) unsigned DEFAULT '0.0000' COMMENT '期初数量',
  `batch_name` varchar(255) DEFAULT NULL COMMENT '批次',
  `manufacture` int unsigned DEFAULT NULL COMMENT '生产日期',
  `guarantee` int unsigned DEFAULT NULL COMMENT '保质期',
  `validity` int DEFAULT NULL COMMENT '有效期至',
  `purchase_id` int DEFAULT NULL COMMENT '购货单号',
  `purchase_info_id` int DEFAULT NULL COMMENT '购货单详情ID',
  `type` tinyint DEFAULT '0' COMMENT '类型:1=期初,2=购货',
  `createtime` int DEFAULT NULL COMMENT '创建时间',
  `updatetime` int DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='商品退货批次详情表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_erp_goods_batch_stock`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_erp_goods_batch_stock` (
  `id` int NOT NULL AUTO_INCREMENT,
  `admin_id` int DEFAULT NULL,
  `batch_id` int DEFAULT NULL,
  `goods_id` int NOT NULL DEFAULT '0' COMMENT '商品ID',
  `goods_code` varchar(30) DEFAULT NULL COMMENT '商品编号',
  `attribute_id` int DEFAULT '0',
  `stores_id` int NOT NULL DEFAULT '0' COMMENT '仓库ID',
  `stock` double(15,4) unsigned NOT NULL DEFAULT '0.0000' COMMENT '库存',
  `status` enum('0','1') NOT NULL DEFAULT '1' COMMENT '状态:0=关闭,1=正常',
  `createtime` int DEFAULT NULL COMMENT '创建时间',
  `updatetime` int DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='商品批次库存表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_erp_goods_exchange`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_erp_goods_exchange` (
  `id` int NOT NULL AUTO_INCREMENT,
  `admin_id` int DEFAULT NULL,
  `purchase_code` varchar(50) DEFAULT NULL COMMENT '单据编号',
  `purchase_type` enum('0','1','2') DEFAULT '0' COMMENT '类型:0=退货单,1=购货订单退货,2=购货单退货',
  `purchase_time` int DEFAULT NULL COMMENT '单据日期',
  `delivery_time` int DEFAULT NULL COMMENT '交货时间',
  `supplyer_code` varchar(30) DEFAULT NULL COMMENT '供应商编号',
  `number` double(15,4) DEFAULT '0.0000' COMMENT '数量',
  `all_money` double(15,4) DEFAULT '0.0000' COMMENT '购货金额',
  `purchase_order_id` varchar(50) DEFAULT NULL COMMENT '购货订单号',
  `purchase_id` varchar(50) DEFAULT NULL COMMENT '购货单号',
  `sales_order_id` varchar(50) DEFAULT NULL COMMENT '销货订单号',
  `preferential_rate` double(15,4) DEFAULT '0.0000' COMMENT '优惠率',
  `preferential_money` double(15,4) DEFAULT '0.0000' COMMENT '优惠额',
  `real_money` double(15,4) DEFAULT '0.0000' COMMENT '优惠后金额',
  `tax_money` double(15,4) DEFAULT NULL,
  `purchase_money` double(15,4) DEFAULT '0.0000' COMMENT '采购费用',
  `preparation_id` int DEFAULT NULL COMMENT '制单人',
  `examine_id` int DEFAULT NULL COMMENT '审核人',
  `operation_id` int DEFAULT NULL COMMENT '最后操作人',
  `pay_status` enum('0','1','2') DEFAULT '0' COMMENT '付款状态:0=未付款,1=全部付款,2=部分付款',
  `check_status` enum('0','1') DEFAULT '0' COMMENT '对单状态:0=未对单,1=已对单',
  `examine_status` enum('0','1') DEFAULT '0' COMMENT '审核状态:0=未审核,1=已审核',
  `examine_time` int DEFAULT NULL COMMENT '审核时间',
  `pay_money` double(15,4) DEFAULT '0.0000' COMMENT '已付款金额',
  `account` int DEFAULT NULL COMMENT '付款账户',
  `retreat_status` enum('0','1','2') DEFAULT NULL COMMENT '退款状态:0=未退款,1=全部退款,2=部分退款',
  `retreat_number` double(15,4) DEFAULT '0.0000' COMMENT '退货数量',
  `retreat_money` double(15,4) DEFAULT '0.0000' COMMENT '退款金额',
  `note` varchar(255) DEFAULT NULL COMMENT '备注',
  `arrears` double(15,4) DEFAULT '0.0000' COMMENT '本次欠款',
  `all_arrears` double(15,4) DEFAULT '0.0000' COMMENT '总欠款',
  `print_num` int DEFAULT '0' COMMENT '打印次数',
  `cost_data` text COMMENT '采购销售费用',
  `createtime` int DEFAULT NULL COMMENT '创建时间',
  `updatetime` int DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='购货退货单';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_erp_goods_exchange_info`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_erp_goods_exchange_info` (
  `id` int NOT NULL AUTO_INCREMENT,
  `admin_id` int DEFAULT NULL,
  `purchase_code` varchar(50) DEFAULT NULL COMMENT '单据编号',
  `purchase_type` enum('0','1','2') DEFAULT NULL COMMENT '类型:1=购货,2=退货',
  `purchase_time` int DEFAULT NULL COMMENT '单据日期',
  `delivery_time` int DEFAULT NULL COMMENT '交货时间',
  `supplyer_code` varchar(30) DEFAULT NULL COMMENT '供应商编号',
  `goods_code` varchar(30) NOT NULL DEFAULT '' COMMENT '商品编号',
  `goods_note` varchar(255) DEFAULT NULL COMMENT '商品备注',
  `goods_attr` int DEFAULT NULL COMMENT '商品属性',
  `goods_unit` int DEFAULT NULL COMMENT '商品单位',
  `stores_id` int DEFAULT NULL COMMENT '仓库',
  `allow_stock` double(15,4) DEFAULT '0.0000' COMMENT '可用库存',
  `number` double(15,4) DEFAULT '0.0000' COMMENT '数量',
  `default_unit` int DEFAULT NULL COMMENT '基本单位',
  `basic_nmber` double(15,4) DEFAULT '0.0000',
  `unit_price` double(15,4) DEFAULT '0.0000' COMMENT '购货单价',
  `tax_included` double(15,4) DEFAULT NULL COMMENT '含税单价',
  `discount` float(5,2) DEFAULT '0.00' COMMENT '折扣率',
  `dis_money` double(15,4) DEFAULT '0.0000' COMMENT '折扣额',
  `all_money` double(15,4) DEFAULT '0.0000' COMMENT '购货金额',
  `tax_rate` double(15,4) DEFAULT NULL COMMENT '税率',
  `tax_money` double(15,4) DEFAULT NULL COMMENT '税额',
  `money_tax_price` double(15,4) DEFAULT NULL COMMENT '价税合计',
  `remark` varchar(255) DEFAULT NULL COMMENT '备注',
  `purchase_order_id` varchar(50) DEFAULT NULL COMMENT '购货订单号',
  `purchase_id` varchar(50) DEFAULT NULL COMMENT '购货单号',
  `sales_order_id` varchar(50) DEFAULT NULL COMMENT '销货订单号',
  `returns_order_id` varchar(50) DEFAULT NULL COMMENT '退货订单号',
  `purchase_info_id` int DEFAULT NULL COMMENT '购货单详情ID',
  `preferential_rate` float(5,2) DEFAULT '0.00' COMMENT '优惠率',
  `preferential_money` double(15,4) DEFAULT '0.0000' COMMENT '优惠额',
  `real_money` double(15,4) DEFAULT '0.0000' COMMENT '优惠后金额',
  `purchase_money` double(15,4) DEFAULT '0.0000' COMMENT '采购费用',
  `preparation_id` int DEFAULT NULL COMMENT '制单人',
  `examine_id` int DEFAULT NULL COMMENT '审核人',
  `operation_id` int DEFAULT NULL COMMENT '最后操作人',
  `pay_status` enum('0','1','2') DEFAULT '0' COMMENT '付款状态:0=未付款,1=部分付款,2=全部付款',
  `check_status` enum('0','1') DEFAULT '0' COMMENT '对单状态:0=未对单,1=已对单',
  `examine_status` enum('0','1') DEFAULT '0' COMMENT '审核状态:0=未审核,1=已审核',
  `examine_time` int DEFAULT NULL COMMENT '审核时间',
  `pay_money` double(15,4) DEFAULT '0.0000' COMMENT '已付款金额',
  `retreat_status` tinyint DEFAULT '0' COMMENT '退款状态:0=未退款,1=全部退款,2=部分退款',
  `retreat_number` double(15,4) DEFAULT '0.0000' COMMENT '退货数量',
  `retreat_money` double(15,4) DEFAULT '0.0000' COMMENT '退款金额',
  `account` int DEFAULT NULL COMMENT '付款账户',
  `print_num` int DEFAULT '0' COMMENT '打印次数',
  `batch_id` int DEFAULT NULL,
  `batch_name` varchar(100) DEFAULT NULL,
  `manufacture` int DEFAULT NULL,
  `guarantee` int DEFAULT NULL,
  `validity` int DEFAULT NULL,
  `period` text COMMENT '序列号',
  `createtime` int DEFAULT NULL COMMENT '创建时间',
  `updatetime` int DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='退货明细';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_erp_goods_exchangethird`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_erp_goods_exchangethird` (
  `id` int NOT NULL AUTO_INCREMENT,
  `order_num` varchar(100) DEFAULT '0' COMMENT '外部订单号',
  `purchase_id` int DEFAULT NULL COMMENT '销货单ID',
  `third_id` int DEFAULT NULL COMMENT '三方ID',
  `createtime` int DEFAULT NULL COMMENT '创建时间',
  `updatetime` int DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='三方销售退货单关联';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_erp_goods_excpur`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_erp_goods_excpur` (
  `id` int NOT NULL AUTO_INCREMENT,
  `exchange_id` int DEFAULT '0',
  `purchase_id` int DEFAULT NULL,
  `type` enum('0','1','2') DEFAULT '0' COMMENT '单据类型',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='退单关联';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_erp_goods_excpur_info`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_erp_goods_excpur_info` (
  `id` int NOT NULL AUTO_INCREMENT,
  `exchange_id` int DEFAULT '0',
  `purchase_id` int DEFAULT NULL,
  `type` enum('0','1','2') DEFAULT NULL COMMENT '类型:1=购货订单,2=购货单',
  `number` float(11,2) DEFAULT '0.00',
  `money` float(11,2) DEFAULT '0.00' COMMENT '退款金额',
  `all_money` double(15,4) DEFAULT '0.0000' COMMENT '购货金额',
  `tax_rate` double(15,4) DEFAULT '0.0000' COMMENT '税率',
  `tax_money` double(15,4) DEFAULT '0.0000' COMMENT '税额',
  `money_tax_price` double(15,4) DEFAULT '0.0000' COMMENT '价税合计',
  `status` enum('0','1') DEFAULT '0' COMMENT '是否审核',
  `times` int DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='退单关联';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_erp_goods_log`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_erp_goods_log` (
  `id` int NOT NULL AUTO_INCREMENT,
  `admin_id` int DEFAULT NULL,
  `purchase_code` varchar(50) DEFAULT NULL COMMENT '单据编号',
  `purchase_id` int DEFAULT NULL COMMENT '单据ID',
  `purchase_time` int DEFAULT NULL COMMENT '单据日期',
  `business_type` enum('1','2','3','4','5','6','7','8','9','10','11','12','13','14','15','17') NOT NULL DEFAULT '1' COMMENT '业务类型:1=期初余额,2=普通采购,3=采购退回,4=普通销售,5=销售退回,6=调拨单,7=成本调整,8=盘盈,9=其他入库,10=盘亏,11=其他出库,12=组装单,13组装单,14=拆卸单,15=拆卸单,17=生产',
  `customer_id` int DEFAULT NULL COMMENT '往来单位',
  `goods_id` int NOT NULL DEFAULT '0' COMMENT '商品ID',
  `goods_code` varchar(30) DEFAULT NULL COMMENT '商品编号',
  `attribute_id` int DEFAULT NULL COMMENT '属性组合ID',
  `goods_unit` int DEFAULT NULL COMMENT '商品单位',
  `remark` varchar(255) DEFAULT NULL COMMENT '备注',
  `stores_id` int DEFAULT '0' COMMENT '仓库ID',
  `enter_number` double(15,4) DEFAULT NULL COMMENT '入库数量',
  `enter_basics` double(15,4) DEFAULT NULL COMMENT '基本单位数量',
  `enter_unit` double(15,4) DEFAULT NULL COMMENT '单位成本',
  `enter_cost` double(15,4) DEFAULT NULL COMMENT '成本',
  `out_number` double(15,4) DEFAULT NULL COMMENT '出库数量',
  `out_basics` double(15,4) DEFAULT NULL COMMENT '基本单位数量',
  `out_unit` double(15,4) DEFAULT NULL COMMENT '单位成本',
  `out_cost` double(15,4) DEFAULT NULL COMMENT '成本',
  `balance_basics` double(15,4) DEFAULT NULL COMMENT '基本单位数量',
  `balance_unit` double(15,4) DEFAULT NULL COMMENT '单位成本',
  `balance_cost` double(15,4) DEFAULT NULL COMMENT '成本',
  `createtime` int DEFAULT NULL COMMENT '创建时间',
  `updatetime` int DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='商品收发明细表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_erp_goods_period`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_erp_goods_period` (
  `id` int NOT NULL AUTO_INCREMENT,
  `admin_id` int DEFAULT NULL,
  `goods_id` int NOT NULL DEFAULT '0' COMMENT '商品ID',
  `goods_code` varchar(30) DEFAULT NULL COMMENT '商品编号',
  `attribute_id` int DEFAULT NULL COMMENT '属性组合ID',
  `stores_id` int NOT NULL DEFAULT '0' COMMENT '仓库ID',
  `stock` double(15,4) unsigned NOT NULL DEFAULT '0.0000' COMMENT '库存',
  `status` enum('0','1') NOT NULL DEFAULT '1' COMMENT '状态:0=关仓,1=正常',
  `period` double(15,4) unsigned DEFAULT '0.0000' COMMENT '期初数量',
  `unit_cost` double(15,4) unsigned DEFAULT '0.0000' COMMENT '单位成本',
  `price` double(15,4) unsigned DEFAULT '0.0000' COMMENT '期初总价',
  `stock_warning` enum('0','1') DEFAULT '0' COMMENT '库存预警:0=否,1=是',
  `min_stock` double(15,4) DEFAULT '0.0000' COMMENT '最低库存',
  `max_stock` double(15,4) DEFAULT '0.0000' COMMENT '最高库存',
  `address` varchar(255) DEFAULT NULL COMMENT '产地',
  `registration` varchar(100) DEFAULT NULL COMMENT '注册证号',
  `production` varchar(100) DEFAULT NULL COMMENT '生产许可证',
  `createtime` int DEFAULT NULL COMMENT '创建时间',
  `updatetime` int DEFAULT NULL COMMENT '更新时间',
  `deletetime` int DEFAULT NULL COMMENT '删除时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='商品期初表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_erp_goods_price`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_erp_goods_price` (
  `id` int NOT NULL AUTO_INCREMENT,
  `admin_id` int DEFAULT NULL,
  `goods_code` varchar(30) NOT NULL DEFAULT '' COMMENT '商品编号',
  `goods_name` varchar(255) NOT NULL DEFAULT '' COMMENT '商品名称',
  `goods_barcode` varchar(50) DEFAULT '' COMMENT '商品条码',
  `goods_id` int NOT NULL DEFAULT '0' COMMENT '商品ID',
  `unit` int NOT NULL DEFAULT '0' COMMENT '计量单位',
  `market_sell_price` double(15,4) NOT NULL DEFAULT '0.0000' COMMENT '零售价',
  `wholesale_price` double(15,4) NOT NULL DEFAULT '0.0000' COMMENT '批发价',
  `vip_price` double(15,4) NOT NULL DEFAULT '0.0000' COMMENT 'vip会员价',
  `discount` double(15,4) unsigned DEFAULT '0.0000' COMMENT '折扣率1',
  `discount2` double(15,4) unsigned DEFAULT '0.0000' COMMENT '折扣率2',
  `purchase` double(15,4) unsigned DEFAULT '0.0000' COMMENT '预计采购价',
  `is_default` tinyint NOT NULL DEFAULT '0' COMMENT '是否主单位:0=否,1=是',
  `createtime` int DEFAULT NULL COMMENT '创建时间',
  `updatetime` int DEFAULT NULL COMMENT '更新时间',
  `deletetime` int DEFAULT NULL COMMENT '删除时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='商品价格表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_erp_goods_purchase`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_erp_goods_purchase` (
  `id` int NOT NULL AUTO_INCREMENT,
  `admin_id` int DEFAULT NULL,
  `purchase_code` varchar(50) DEFAULT NULL COMMENT '单据编号',
  `purchase_type` enum('1','2') DEFAULT '1' COMMENT '类型:1=购货,2=退货',
  `purchase_time` int DEFAULT NULL COMMENT '单据日期',
  `delivery_time` int DEFAULT NULL COMMENT '交货时间',
  `supplyer_code` varchar(30) DEFAULT NULL COMMENT '供应商编号',
  `number` double(15,4) DEFAULT '0.0000' COMMENT '数量',
  `all_money` float(11,2) DEFAULT '0.00' COMMENT '购货金额',
  `purchase_order_id` varchar(50) DEFAULT NULL COMMENT '购货订单号',
  `sales_order_id` varchar(50) DEFAULT NULL COMMENT '销货订单号',
  `returns_order_id` varchar(50) DEFAULT NULL COMMENT '退货订单号',
  `preferential_rate` float(5,2) DEFAULT '0.00' COMMENT '优惠率',
  `preferential_money` double(15,4) DEFAULT '0.0000' COMMENT '优惠额',
  `real_money` double(15,4) DEFAULT '0.0000' COMMENT '优惠后金额',
  `tax_money` double(15,4) DEFAULT NULL COMMENT '税额',
  `purchase_money` double(15,4) DEFAULT '0.0000' COMMENT '采购费用',
  `preparation_id` int DEFAULT NULL COMMENT '制单人',
  `examine_id` int DEFAULT NULL COMMENT '审核人',
  `operation_id` int DEFAULT NULL COMMENT '最后操作人',
  `pay_status` enum('0','1','2') DEFAULT '0' COMMENT '付款状态:0=未付款,1=全部付款,2=部分付款',
  `check_status` enum('0','1') DEFAULT '0' COMMENT '对单状态:0=未对单,1=已对单',
  `examine_status` enum('0','1') DEFAULT '0' COMMENT '审核状态:0=未审核,1=已审核',
  `examine_time` int DEFAULT NULL COMMENT '审核时间',
  `pay_money` double(15,4) DEFAULT '0.0000' COMMENT '已付款金额',
  `account` int DEFAULT NULL COMMENT '付款账户',
  `note` varchar(255) DEFAULT NULL COMMENT '备注',
  `arrears` double(15,4) DEFAULT '0.0000' COMMENT '本次欠款',
  `all_arrears` double(15,4) DEFAULT '0.0000' COMMENT '总欠款',
  `print_num` int DEFAULT '0' COMMENT '打印次数',
  `cost_data` text COMMENT '采购费用',
  `createtime` int DEFAULT NULL COMMENT '创建时间',
  `updatetime` int DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `purchase_code` (`purchase_code`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='购货单';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_erp_goods_purchase_info`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_erp_goods_purchase_info` (
  `id` int NOT NULL AUTO_INCREMENT,
  `admin_id` int DEFAULT NULL,
  `purchase_code` varchar(50) DEFAULT NULL COMMENT '单据编号',
  `purchase_type` enum('1','2') DEFAULT '1' COMMENT '类型:1=购货,2=退货',
  `purchase_time` int DEFAULT NULL COMMENT '单据日期',
  `delivery_time` int DEFAULT NULL COMMENT '交货时间',
  `supplyer_code` varchar(30) DEFAULT NULL COMMENT '供应商编号',
  `goods_code` varchar(30) NOT NULL DEFAULT '' COMMENT '商品编号',
  `goods_note` varchar(255) DEFAULT NULL COMMENT '商品备注',
  `goods_attr` int DEFAULT NULL COMMENT '商品属性',
  `goods_unit` int DEFAULT NULL COMMENT '商品单位',
  `stores_id` int DEFAULT NULL COMMENT '仓库',
  `allow_stock` double(15,4) DEFAULT '0.0000' COMMENT '可用库存',
  `number` double(15,4) DEFAULT '0.0000' COMMENT '数量',
  `default_unit` int DEFAULT NULL COMMENT '基本单位',
  `basic_nmber` double(15,4) DEFAULT '0.0000',
  `unit_price` double(15,4) DEFAULT '0.0000' COMMENT '购货单价',
  `tax_included` double(15,4) DEFAULT NULL COMMENT '含税单价',
  `discount` float(5,2) DEFAULT '0.00' COMMENT '折扣率',
  `dis_money` double(15,4) DEFAULT '0.0000' COMMENT '折扣额',
  `all_money` double(15,4) DEFAULT '0.0000' COMMENT '购货金额',
  `tax_rate` double(15,4) DEFAULT NULL COMMENT '税率',
  `tax_money` double(15,4) DEFAULT NULL COMMENT '税额',
  `money_tax_price` double(15,4) DEFAULT NULL COMMENT '价税合计',
  `remark` varchar(255) DEFAULT NULL COMMENT '备注',
  `purchase_order_id` varchar(50) DEFAULT NULL COMMENT '购货订单号',
  `sales_order_id` varchar(50) DEFAULT NULL COMMENT '销货订单号',
  `returns_order_id` varchar(50) DEFAULT NULL COMMENT '退货订单号',
  `preferential_rate` float(5,2) DEFAULT '0.00' COMMENT '优惠率',
  `preferential_money` double(15,4) DEFAULT '0.0000' COMMENT '优惠额',
  `real_money` double(15,4) DEFAULT '0.0000' COMMENT '优惠后金额',
  `purchase_money` double(15,4) DEFAULT '0.0000' COMMENT '采购费用',
  `preparation_id` int DEFAULT NULL COMMENT '制单人',
  `examine_id` int DEFAULT NULL COMMENT '审核人',
  `operation_id` int DEFAULT NULL COMMENT '最后操作人',
  `pay_status` enum('0','1','2') DEFAULT '0' COMMENT '付款状态:0=未付款,1=部分付款,2=全部付款',
  `check_status` enum('0','1') DEFAULT '0' COMMENT '对单状态:0=未对单,1=已对单',
  `examine_status` enum('0','1') DEFAULT '0' COMMENT '审核状态:0=未审核,1=已审核',
  `examine_time` int DEFAULT NULL COMMENT '审核时间',
  `pay_money` double(15,4) DEFAULT '0.0000' COMMENT '已付款金额',
  `account` int DEFAULT NULL COMMENT '付款账户',
  `print_num` int DEFAULT '0' COMMENT '打印次数',
  `order_info_id` varchar(255) DEFAULT NULL COMMENT '购货订单详情ID',
  `batch_id` int DEFAULT NULL,
  `batch_name` varchar(100) DEFAULT NULL,
  `manufacture` int DEFAULT NULL,
  `guarantee` int DEFAULT NULL,
  `validity` int DEFAULT NULL,
  `period` text COMMENT '序列号',
  `createtime` int DEFAULT NULL COMMENT '创建时间',
  `updatetime` int DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `purchase_code` (`purchase_code`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='商品采购明细';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_erp_goods_purchase_order`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_erp_goods_purchase_order` (
  `id` int NOT NULL AUTO_INCREMENT,
  `admin_id` int DEFAULT NULL,
  `purchase_code` varchar(50) DEFAULT NULL COMMENT '单据编号',
  `purchase_type` enum('1','2') DEFAULT '1' COMMENT '类型:1=购货,2=退货',
  `purchase_time` int DEFAULT NULL COMMENT '单据日期',
  `delivery_time` int DEFAULT NULL COMMENT '交货时间',
  `supplyer_code` varchar(30) DEFAULT NULL COMMENT '供应商编号',
  `number` double(15,4) DEFAULT '0.0000' COMMENT '数量',
  `all_money` double(15,4) DEFAULT '0.0000' COMMENT '购货金额',
  `purchase_id` varchar(50) DEFAULT NULL COMMENT '购货单号',
  `sales_order_id` varchar(50) DEFAULT NULL COMMENT '销货订单号',
  `returns_order_id` varchar(50) DEFAULT NULL COMMENT '退货订单号',
  `preferential_rate` double(15,4) DEFAULT '0.0000' COMMENT '优惠率',
  `preferential_money` double(15,4) DEFAULT '0.0000' COMMENT '优惠额',
  `real_money` double(15,4) DEFAULT '0.0000' COMMENT '优惠后金额',
  `tax_money` double(15,4) unsigned DEFAULT NULL COMMENT '税额',
  `purchase_money` double(15,4) DEFAULT '0.0000' COMMENT '采购费用',
  `preparation_id` int DEFAULT NULL COMMENT '制单人',
  `examine_id` int DEFAULT NULL COMMENT '审核人',
  `operation_id` int DEFAULT NULL COMMENT '最后操作人',
  `status` enum('0','1','2','3') DEFAULT '0' COMMENT '状态:0=未入库,1=全部入库,2=部分入库,3=已取消',
  `check_status` enum('0','1') DEFAULT '0' COMMENT '对单状态:0=未对单,1=已对单',
  `examine_status` enum('0','1') DEFAULT '0' COMMENT '审核状态:0=未审核,1=已审核',
  `examine_time` int DEFAULT NULL COMMENT '审核时间',
  `pay_money` double(15,4) DEFAULT '0.0000' COMMENT '已付款金额',
  `store_number` double(15,4) DEFAULT '0.0000' COMMENT '入库数量',
  `account` int DEFAULT NULL COMMENT '付款账户',
  `note` varchar(255) DEFAULT NULL COMMENT '备注',
  `arrears` double(15,4) DEFAULT '0.0000' COMMENT '本次欠款',
  `all_arrears` double(15,4) DEFAULT '0.0000' COMMENT '总欠款',
  `print_num` int DEFAULT '0' COMMENT '打印次数',
  `createtime` int DEFAULT NULL COMMENT '创建时间',
  `updatetime` int DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `purchase_code` (`purchase_code`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='购货订单';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_erp_goods_purchase_order_info`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_erp_goods_purchase_order_info` (
  `id` int NOT NULL AUTO_INCREMENT,
  `admin_id` int DEFAULT NULL,
  `purchase_code` varchar(50) DEFAULT NULL COMMENT '单据编号',
  `purchase_type` enum('1','2') DEFAULT '1' COMMENT '类型:1=购货,2=退货',
  `purchase_time` int DEFAULT NULL COMMENT '单据日期',
  `delivery_time` int DEFAULT NULL COMMENT '交货时间',
  `supplyer_code` varchar(30) DEFAULT NULL COMMENT '供应商编号',
  `goods_code` varchar(30) NOT NULL DEFAULT '' COMMENT '商品编号',
  `goods_note` varchar(255) DEFAULT NULL COMMENT '商品备注',
  `goods_attr` int DEFAULT NULL COMMENT '商品属性',
  `goods_unit` int DEFAULT NULL COMMENT '商品单位',
  `stores_id` int DEFAULT NULL COMMENT '仓库',
  `allow_stock` double(15,4) DEFAULT '0.0000' COMMENT '可用库存',
  `number` double(15,4) DEFAULT '0.0000' COMMENT '数量',
  `purchase_number` double(15,4) DEFAULT '0.0000' COMMENT '可生成购货单数量',
  `default_unit` int DEFAULT NULL COMMENT '基本单位',
  `basic_nmber` double(15,4) DEFAULT '0.0000',
  `unit_price` double(15,4) DEFAULT '0.0000' COMMENT '购货单价',
  `tax_included` double(15,4) DEFAULT NULL COMMENT '含税单价',
  `discount` double(15,4) DEFAULT '0.0000' COMMENT '折扣率',
  `dis_money` double(15,4) DEFAULT '0.0000' COMMENT '折扣额',
  `all_money` double(15,4) DEFAULT '0.0000' COMMENT '购货金额',
  `tax_rate` double(15,4) unsigned DEFAULT '0.0000' COMMENT '税率',
  `tax_money` double(15,4) unsigned DEFAULT '0.0000' COMMENT '税额',
  `money_tax_price` double(15,4) DEFAULT '0.0000' COMMENT '价税合计',
  `remark` varchar(255) DEFAULT NULL COMMENT '备注',
  `purchase_id` varchar(50) DEFAULT NULL COMMENT '购货单号',
  `sales_order_id` varchar(50) DEFAULT NULL COMMENT '销货订单号',
  `returns_order_id` varchar(50) DEFAULT NULL COMMENT '退货订单号',
  `preferential_rate` double(15,4) DEFAULT '0.0000' COMMENT '优惠率',
  `preferential_money` double(15,4) DEFAULT '0.0000' COMMENT '优惠额',
  `real_money` double(15,4) DEFAULT '0.0000' COMMENT '优惠后金额',
  `purchase_money` double(15,4) DEFAULT '0.0000' COMMENT '采购费用',
  `preparation_id` int DEFAULT NULL COMMENT '制单人',
  `examine_id` int DEFAULT NULL COMMENT '审核人',
  `operation_id` int DEFAULT NULL COMMENT '最后操作人',
  `status` enum('0','1','2','3') DEFAULT '0' COMMENT '状态:0=未入库,1=全部入库,2=部分入库,3=已取消',
  `check_status` enum('0','1') DEFAULT '0' COMMENT '对单状态:0=未对单,1=已对单',
  `examine_status` enum('0','1') DEFAULT '0' COMMENT '审核状态:0=未审核,1=已审核',
  `examine_time` int DEFAULT NULL COMMENT '审核时间',
  `pay_money` double(15,4) DEFAULT '0.0000' COMMENT '已付款金额',
  `account` int DEFAULT NULL COMMENT '付款账户',
  `print_num` int DEFAULT '0' COMMENT '打印次数',
  `library_number` double(15,4) DEFAULT '0.0000' COMMENT '未入库数量',
  `library_money` double(15,4) DEFAULT '0.0000' COMMENT '未入库金额',
  `library_time` int DEFAULT NULL COMMENT '最后入库时间',
  `createtime` int DEFAULT NULL COMMENT '创建时间',
  `updatetime` int DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `purchase_code` (`purchase_code`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='购货订单详情';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_erp_goods_purchase_summary`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_erp_goods_purchase_summary` (
  `id` int NOT NULL AUTO_INCREMENT,
  `admin_id` int DEFAULT NULL,
  `purchase_code` varchar(50) DEFAULT NULL COMMENT '单据编号',
  `purchase_type` enum('1','2') DEFAULT '1' COMMENT '类型:1=购货,2=退货',
  `purchase_time` int DEFAULT NULL COMMENT '单据日期',
  `delivery_time` int DEFAULT NULL COMMENT '交货时间',
  `supplyer_code` varchar(30) DEFAULT NULL COMMENT '供应商编号',
  `goods_code` varchar(30) NOT NULL DEFAULT '' COMMENT '商品编号',
  `goods_note` varchar(255) DEFAULT NULL COMMENT '商品备注',
  `goods_attr` int DEFAULT NULL COMMENT '商品属性',
  `goods_unit` int DEFAULT NULL COMMENT '商品单位',
  `stores_id` int DEFAULT NULL COMMENT '仓库',
  `allow_stock` double(15,4) DEFAULT '0.0000' COMMENT '可用库存',
  `number` double(15,4) DEFAULT '0.0000' COMMENT '数量',
  `default_unit` int DEFAULT NULL COMMENT '基本单位',
  `basic_nmber` double(15,4) DEFAULT '0.0000',
  `unit_price` float(11,2) DEFAULT '0.00' COMMENT '购货单价',
  `tax_included` double(15,4) DEFAULT NULL COMMENT '含税单价',
  `discount` float(5,2) DEFAULT '0.00' COMMENT '折扣率',
  `dis_money` double(15,4) DEFAULT '0.0000' COMMENT '折扣额',
  `all_money` double(15,4) DEFAULT '0.0000' COMMENT '购货金额',
  `tax_rate` double(15,4) DEFAULT NULL COMMENT '税率',
  `tax_money` double(15,4) DEFAULT NULL COMMENT '税额',
  `money_tax_price` double(15,4) DEFAULT NULL COMMENT '价税合计',
  `remark` varchar(255) DEFAULT NULL COMMENT '备注',
  `purchase_order_id` varchar(50) DEFAULT NULL COMMENT '购货订单号',
  `sales_order_id` varchar(50) DEFAULT NULL COMMENT '购货单号',
  `returns_order_id` varchar(50) DEFAULT NULL COMMENT '退货订单号',
  `preferential_rate` float(5,2) DEFAULT '0.00' COMMENT '优惠率',
  `preferential_money` double(15,4) DEFAULT '0.0000' COMMENT '优惠额',
  `real_money` double(15,4) DEFAULT '0.0000' COMMENT '优惠后金额',
  `purchase_money` double(15,4) DEFAULT '0.0000' COMMENT '采购费用',
  `preparation_id` int DEFAULT NULL COMMENT '制单人',
  `examine_id` int DEFAULT NULL COMMENT '审核人',
  `operation_id` int DEFAULT NULL COMMENT '最后操作人',
  `pay_status` enum('0','1','2') DEFAULT '0' COMMENT '付款状态:0=未付款,1=部分付款,2=全部付款',
  `check_status` enum('0','1') DEFAULT '0' COMMENT '对单状态:0=未对单,1=已对单',
  `examine_status` enum('0','1') DEFAULT '0' COMMENT '审核状态:0=未审核,1=已审核',
  `examine_time` int DEFAULT NULL COMMENT '审核时间',
  `pay_money` double(15,4) DEFAULT '0.0000' COMMENT '已付款金额',
  `account` int DEFAULT NULL COMMENT '付款账户',
  `print_num` int DEFAULT '0' COMMENT '打印次数',
  `order_info_id` int DEFAULT NULL,
  `batch_id` int DEFAULT NULL,
  `batch_name` varchar(100) DEFAULT NULL,
  `manufacture` int DEFAULT NULL,
  `guarantee` int DEFAULT NULL,
  `validity` int DEFAULT NULL,
  `period` text COMMENT '序列号',
  `createtime` int DEFAULT NULL COMMENT '创建时间',
  `updatetime` int DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='采购汇总表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_erp_goods_purord`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_erp_goods_purord` (
  `id` int NOT NULL AUTO_INCREMENT,
  `order_id` int DEFAULT '0',
  `purchase_id` int DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='订单关联';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_erp_goods_purord_info`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_erp_goods_purord_info` (
  `id` int NOT NULL AUTO_INCREMENT,
  `order_id` int DEFAULT '0',
  `purchase_id` int DEFAULT NULL COMMENT '购货明细ID',
  `type` enum('0','1','2') DEFAULT NULL COMMENT '类型:1=购货订单,2=购货单',
  `number` float(11,2) DEFAULT '0.00' COMMENT '数量',
  `money` float(11,2) DEFAULT '0.00' COMMENT '已付款金额',
  `all_money` float(11,2) DEFAULT '0.00' COMMENT '购货金额',
  `tax_rate` double(15,4) DEFAULT '0.0000' COMMENT '税率',
  `tax_money` double(15,4) DEFAULT '0.0000' COMMENT '税额',
  `money_tax_price` double(15,4) DEFAULT '0.0000' COMMENT '价税合计',
  `status` enum('0','1') DEFAULT '0',
  `times` int DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='订单关联';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_erp_goods_saleexc`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_erp_goods_saleexc` (
  `id` int NOT NULL AUTO_INCREMENT,
  `exchange_id` int DEFAULT '0',
  `purchase_id` int DEFAULT NULL,
  `type` enum('0','1','2') DEFAULT '0' COMMENT '单据类型',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='退单关联';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_erp_goods_saleexc_info`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_erp_goods_saleexc_info` (
  `id` int NOT NULL AUTO_INCREMENT,
  `exchange_id` int DEFAULT '0',
  `purchase_id` int DEFAULT NULL,
  `type` enum('0','1','2') DEFAULT NULL COMMENT '类型:1=销货订单,2=销货单',
  `number` float(11,2) DEFAULT '0.00',
  `money` float(11,2) DEFAULT '0.00' COMMENT '退款金额',
  `all_money` double(15,4) DEFAULT '0.0000' COMMENT '购货金额',
  `tax_rate` double(15,4) DEFAULT '0.0000' COMMENT '税率',
  `tax_money` double(15,4) DEFAULT '0.0000' COMMENT '税额',
  `money_tax_price` double(15,4) DEFAULT '0.0000' COMMENT '价税合计',
  `status` enum('0','1') DEFAULT '0' COMMENT '是否审核',
  `times` int DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='退单关联';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_erp_goods_saleord`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_erp_goods_saleord` (
  `id` int NOT NULL AUTO_INCREMENT,
  `order_id` int DEFAULT '0',
  `purchase_id` int DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='销售订单关联';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_erp_goods_saleord_info`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_erp_goods_saleord_info` (
  `id` int NOT NULL AUTO_INCREMENT,
  `order_id` int DEFAULT '0',
  `purchase_id` int DEFAULT NULL COMMENT '销货明细ID',
  `number` float(11,2) DEFAULT '0.00' COMMENT '数量',
  `money` float(11,2) DEFAULT '0.00' COMMENT '已收款金额',
  `all_money` float(11,2) DEFAULT '0.00' COMMENT '销货金额',
  `tax_rate` double(15,4) DEFAULT '0.0000' COMMENT '税率',
  `tax_money` double(15,4) DEFAULT '0.0000' COMMENT '税额',
  `money_tax_price` double(15,4) DEFAULT '0.0000' COMMENT '价税合计',
  `status` enum('0','1') DEFAULT '0',
  `times` int DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='销售订单关联';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_erp_goods_sales`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_erp_goods_sales` (
  `id` int NOT NULL AUTO_INCREMENT,
  `admin_id` int DEFAULT NULL,
  `purchase_code` varchar(50) DEFAULT NULL COMMENT '单据编号',
  `order_code` varchar(50) DEFAULT NULL COMMENT '零售编号',
  `order_time` int DEFAULT NULL COMMENT '零售时间',
  `order_type` enum('1','2') DEFAULT '1' COMMENT '单据类型:1=销货单,2=零售单',
  `purchase_type` enum('1','2') DEFAULT '1' COMMENT '类型:1=销货,2=退货',
  `purchase_time` int DEFAULT NULL COMMENT '单据日期',
  `delivery_time` int DEFAULT NULL COMMENT '交货时间',
  `customers_id` int DEFAULT NULL COMMENT '客户',
  `receiving_address` varchar(255) DEFAULT NULL COMMENT '收货地址',
  `sale_user` int DEFAULT NULL COMMENT '销售员',
  `number` double(15,4) DEFAULT '0.0000' COMMENT '数量',
  `all_money` float(15,4) DEFAULT '0.0000' COMMENT '销货金额',
  `purchase_order_id` varchar(50) DEFAULT NULL COMMENT '购货订单号',
  `sales_order_id` varchar(50) DEFAULT NULL COMMENT '销货订单号',
  `returns_order_id` varchar(50) DEFAULT NULL COMMENT '退货订单号',
  `preferential_rate` float(5,2) DEFAULT '0.00' COMMENT '优惠率',
  `preferential_money` double(15,4) DEFAULT '0.0000' COMMENT '优惠额',
  `real_money` double(15,4) DEFAULT '0.0000' COMMENT '优惠后金额',
  `tax_money` double(15,4) DEFAULT NULL COMMENT '税额',
  `money_tax_price` double(15,4) DEFAULT '0.0000' COMMENT '价税合计',
  `purchase_money` double(15,4) DEFAULT '0.0000' COMMENT '采购费用',
  `customers_money` double(15,4) DEFAULT '0.0000' COMMENT '客户承担费用',
  `preparation_id` int DEFAULT NULL COMMENT '制单人',
  `examine_id` int DEFAULT NULL COMMENT '审核人',
  `operation_id` int DEFAULT NULL COMMENT '最后操作人',
  `pay_status` enum('0','1','2') DEFAULT '0' COMMENT '收款状态:0=未收款,1=全部收款,2=部分收款',
  `check_status` enum('0','1') DEFAULT '0' COMMENT '对单状态:0=未对单,1=已对单',
  `examine_status` enum('0','1') DEFAULT '0' COMMENT '审核状态:0=未审核,1=已审核',
  `examine_time` int DEFAULT NULL COMMENT '审核时间',
  `pay_money` double(15,4) DEFAULT '0.0000' COMMENT '已付款金额',
  `account` int DEFAULT NULL COMMENT '付款账户',
  `note` varchar(255) DEFAULT NULL COMMENT '备注',
  `arrears` double(15,4) DEFAULT '0.0000' COMMENT '本次欠款',
  `all_arrears` double(15,4) DEFAULT '0.0000' COMMENT '总欠款',
  `print_num` int DEFAULT '0' COMMENT '打印次数',
  `cost_data` text COMMENT '采购销售费用',
  `third_id` int DEFAULT '0' COMMENT '三方ID',
  `createtime` int DEFAULT NULL COMMENT '创建时间',
  `updatetime` int DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `purchase_code` (`purchase_code`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='销货单';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_erp_goods_sales_exchange`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_erp_goods_sales_exchange` (
  `id` int NOT NULL AUTO_INCREMENT,
  `admin_id` int DEFAULT NULL,
  `purchase_code` varchar(50) DEFAULT NULL COMMENT '单据编号',
  `order_code` varchar(50) DEFAULT NULL COMMENT '零售编号',
  `order_time` int DEFAULT NULL COMMENT '零售时间',
  `order_type` enum('1','2') DEFAULT '1' COMMENT '单据类型:1=销货单,2=零售单',
  `purchase_type` enum('0','1','2') DEFAULT '0' COMMENT '类型:0=退货,1=销货订单退货,2=销货单退货',
  `purchase_time` int DEFAULT NULL COMMENT '单据日期',
  `delivery_time` int DEFAULT NULL COMMENT '交货时间',
  `customers_id` int DEFAULT NULL COMMENT '客户',
  `receiving_address` varchar(255) DEFAULT NULL COMMENT '收货地址',
  `sale_user` int DEFAULT NULL COMMENT '销售员',
  `number` double(15,4) DEFAULT '0.0000' COMMENT '数量',
  `all_money` double(15,4) DEFAULT '0.0000' COMMENT '购货金额',
  `purchase_order_id` varchar(50) DEFAULT NULL COMMENT '购货订单号',
  `purchase_id` varchar(50) DEFAULT NULL COMMENT '购货单号',
  `sales_order_id` varchar(50) DEFAULT NULL COMMENT '销货订单号',
  `preferential_rate` float(5,2) DEFAULT '0.00' COMMENT '优惠率',
  `preferential_money` double(15,4) DEFAULT '0.0000' COMMENT '优惠额',
  `real_money` double(15,4) DEFAULT '0.0000' COMMENT '优惠后金额',
  `tax_money` double(15,4) DEFAULT NULL COMMENT '税额',
  `money_tax_price` double(15,4) DEFAULT '0.0000' COMMENT '价税合计',
  `purchase_money` double(15,4) DEFAULT '0.0000' COMMENT '采购费用',
  `customers_money` double(15,4) DEFAULT '0.0000' COMMENT '客户承担费用',
  `preparation_id` int DEFAULT NULL COMMENT '制单人',
  `examine_id` int DEFAULT NULL COMMENT '审核人',
  `operation_id` int DEFAULT NULL COMMENT '最后操作人',
  `pay_status` enum('0','1','2') DEFAULT '0' COMMENT '收款状态:0=未收款,1=全部收款,2=部分收款',
  `check_status` enum('0','1') DEFAULT '0' COMMENT '对单状态:0=未对单,1=已对单',
  `examine_status` enum('0','1') DEFAULT '0' COMMENT '审核状态:0=未审核,1=已审核',
  `examine_time` int DEFAULT NULL COMMENT '审核时间',
  `pay_money` double(15,4) DEFAULT '0.0000' COMMENT '已付款金额',
  `account` int DEFAULT NULL COMMENT '付款账户',
  `retreat_status` enum('0','1','2') DEFAULT NULL COMMENT '退款状态:0=未退款,1=全部退款,2=部分退款',
  `retreat_number` double(15,4) DEFAULT '0.0000' COMMENT '退货数量',
  `retreat_money` double(15,4) DEFAULT '0.0000' COMMENT '退款金额',
  `note` varchar(255) DEFAULT NULL COMMENT '备注',
  `arrears` double(15,4) DEFAULT '0.0000' COMMENT '本次欠款',
  `all_arrears` double(15,4) DEFAULT '0.0000' COMMENT '总欠款',
  `print_num` int DEFAULT '0' COMMENT '打印次数',
  `cost_data` text COMMENT '采购销售费用',
  `third_id` int DEFAULT '0' COMMENT '三方ID',
  `createtime` int DEFAULT NULL COMMENT '创建时间',
  `updatetime` int DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='销货退货单';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_erp_goods_sales_exchange_info`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_erp_goods_sales_exchange_info` (
  `id` int NOT NULL AUTO_INCREMENT,
  `admin_id` int DEFAULT NULL,
  `purchase_code` varchar(50) DEFAULT NULL COMMENT '单据编号',
  `purchase_type` enum('0','1','2') DEFAULT NULL COMMENT '类型:1=销货,2=退货',
  `purchase_time` int DEFAULT NULL COMMENT '单据日期',
  `delivery_time` int DEFAULT NULL COMMENT '交货时间',
  `customers_id` int DEFAULT NULL COMMENT '客户',
  `receiving_address` varchar(255) DEFAULT NULL COMMENT '收货地址',
  `sale_user` int DEFAULT NULL COMMENT '销售员',
  `goods_code` varchar(30) NOT NULL DEFAULT '' COMMENT '商品编号',
  `goods_note` varchar(255) DEFAULT NULL COMMENT '商品备注',
  `goods_attr` int DEFAULT NULL COMMENT '属性',
  `goods_unit` int DEFAULT NULL COMMENT '商品单位',
  `stores_id` int DEFAULT NULL COMMENT '仓库',
  `allow_stock` double(15,4) DEFAULT '0.0000' COMMENT '可用库存',
  `number` double(15,4) DEFAULT '0.0000' COMMENT '数量',
  `default_unit` int DEFAULT NULL COMMENT '基本单位',
  `basic_nmber` double(15,4) DEFAULT '0.0000',
  `unit_price` double(15,4) DEFAULT '0.0000' COMMENT '购货单价',
  `cost_price` double(15,4) DEFAULT NULL COMMENT '成本价',
  `tax_included` double(15,4) DEFAULT '0.0000' COMMENT '含税单价',
  `discount` float(5,2) DEFAULT '0.00' COMMENT '折扣率',
  `dis_money` double(15,4) DEFAULT '0.0000' COMMENT '折扣额',
  `all_money` double(15,4) DEFAULT '0.0000' COMMENT '购货金额',
  `tax_rate` double(15,4) DEFAULT '0.0000' COMMENT '税率',
  `tax_money` double(15,4) DEFAULT '0.0000' COMMENT '税额',
  `money_tax_price` double(15,4) DEFAULT '0.0000' COMMENT '价税合计',
  `remark` varchar(255) DEFAULT NULL COMMENT '备注',
  `purchase_order_id` varchar(50) DEFAULT NULL COMMENT '购货订单号',
  `purchase_id` varchar(50) DEFAULT NULL COMMENT '购货单号',
  `sales_order_id` varchar(50) DEFAULT NULL COMMENT '销货订单号',
  `returns_order_id` varchar(50) DEFAULT NULL COMMENT '退货订单号',
  `purchase_info_id` int DEFAULT NULL COMMENT '购货单详情ID',
  `preferential_rate` float(5,2) DEFAULT '0.00' COMMENT '优惠率',
  `preferential_money` float(11,2) DEFAULT '0.00' COMMENT '优惠额',
  `real_money` float(11,2) DEFAULT '0.00' COMMENT '优惠后金额',
  `purchase_money` float(11,2) DEFAULT '0.00' COMMENT '采购费用',
  `customers_money` float(11,2) DEFAULT '0.00' COMMENT '客户承担费用',
  `preparation_id` int DEFAULT NULL COMMENT '制单人',
  `examine_id` int DEFAULT NULL COMMENT '审核人',
  `operation_id` int DEFAULT NULL COMMENT '最后操作人',
  `pay_status` enum('0','1','2') DEFAULT '0' COMMENT '收款状态:0=未收款,1=全部收款,2=部分收款',
  `check_status` enum('0','1') DEFAULT '0' COMMENT '对单状态:0=未对单,1=已对单',
  `examine_status` enum('0','1') DEFAULT '0' COMMENT '审核状态:0=未审核,1=已审核',
  `examine_time` int DEFAULT NULL COMMENT '审核时间',
  `pay_money` float(11,2) DEFAULT '0.00' COMMENT '已付款金额',
  `retreat_status` float(11,2) DEFAULT '0.00' COMMENT '退款状态:0=未退款,1=全部退款,2=部分退款',
  `retreat_number` float(11,2) DEFAULT '0.00' COMMENT '退货数量',
  `retreat_money` float(11,2) DEFAULT '0.00' COMMENT '退款金额',
  `account` int DEFAULT NULL COMMENT '付款账户',
  `print_num` int DEFAULT '0' COMMENT '打印次数',
  `batch_id` int DEFAULT NULL,
  `batch_name` varchar(100) DEFAULT NULL COMMENT '批次',
  `manufacture` int DEFAULT NULL COMMENT '生产日期',
  `guarantee` int DEFAULT NULL COMMENT '保质期',
  `validity` int DEFAULT NULL COMMENT '过期时间',
  `period` text COMMENT '序列号',
  `createtime` int DEFAULT NULL COMMENT '创建时间',
  `updatetime` int DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='退货明细';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_erp_goods_sales_info`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_erp_goods_sales_info` (
  `id` int NOT NULL AUTO_INCREMENT,
  `admin_id` int DEFAULT NULL,
  `purchase_code` varchar(50) DEFAULT NULL COMMENT '单据编号',
  `purchase_type` enum('1','2') DEFAULT '1' COMMENT '类型:1=销货,2=退货',
  `purchase_time` int DEFAULT NULL COMMENT '单据日期',
  `delivery_time` int DEFAULT NULL COMMENT '交货时间',
  `customers_id` int DEFAULT NULL COMMENT '客户',
  `receiving_address` varchar(255) DEFAULT NULL COMMENT '收货地址',
  `sale_user` int DEFAULT NULL COMMENT '销售员',
  `goods_code` varchar(30) NOT NULL DEFAULT '' COMMENT '商品编号',
  `goods_note` varchar(255) DEFAULT NULL COMMENT '商品备注',
  `goods_attr` int DEFAULT NULL COMMENT '商品属性',
  `goods_unit` int DEFAULT NULL COMMENT '商品单位',
  `stores_id` int DEFAULT NULL COMMENT '仓库',
  `allow_stock` double(15,4) DEFAULT '0.0000' COMMENT '可用库存',
  `number` double(15,4) DEFAULT '0.0000' COMMENT '数量',
  `default_unit` int DEFAULT NULL COMMENT '基本单位',
  `basic_nmber` double(15,4) DEFAULT '0.0000',
  `unit_price` double(15,4) DEFAULT '0.0000' COMMENT '销货单价',
  `cost_price` double(15,4) DEFAULT NULL COMMENT '成本价',
  `tax_included` double(15,4) DEFAULT '0.0000' COMMENT '含税单价',
  `discount` float(5,2) DEFAULT '0.00' COMMENT '折扣率',
  `dis_money` double(15,4) DEFAULT '0.0000' COMMENT '折扣额',
  `all_money` double(15,4) DEFAULT '0.0000' COMMENT '购货金额',
  `tax_rate` double(15,4) DEFAULT '0.0000' COMMENT '税率',
  `tax_money` double(15,4) DEFAULT '0.0000' COMMENT '税额',
  `money_tax_price` double(15,4) DEFAULT '0.0000' COMMENT '价税合计',
  `remark` varchar(255) DEFAULT NULL COMMENT '备注',
  `purchase_order_id` varchar(50) DEFAULT NULL COMMENT '销货订单号',
  `sales_order_id` varchar(50) DEFAULT NULL COMMENT '销货订单号',
  `returns_order_id` varchar(50) DEFAULT NULL COMMENT '退货订单号',
  `preferential_rate` float(5,2) DEFAULT '0.00' COMMENT '优惠率',
  `preferential_money` double(15,4) DEFAULT '0.0000' COMMENT '优惠额',
  `real_money` double(15,4) DEFAULT '0.0000' COMMENT '优惠后金额',
  `purchase_money` double(15,4) DEFAULT '0.0000' COMMENT '采购费用',
  `customers_money` double(15,4) DEFAULT '0.0000' COMMENT '客户承担费用',
  `preparation_id` int DEFAULT NULL COMMENT '制单人',
  `examine_id` int DEFAULT NULL COMMENT '审核人',
  `operation_id` int DEFAULT NULL COMMENT '最后操作人',
  `pay_status` enum('0','1','2') DEFAULT '0' COMMENT '收款状态:0=未收款,1=部分收款,2=全部收款',
  `check_status` enum('0','1') DEFAULT '0' COMMENT '对单状态:0=未对单,1=已对单',
  `examine_status` enum('0','1') DEFAULT '0' COMMENT '审核状态:0=未审核,1=已审核',
  `examine_time` int DEFAULT NULL COMMENT '审核时间',
  `pay_money` double(15,4) DEFAULT '0.0000' COMMENT '已付款金额',
  `account` int DEFAULT NULL COMMENT '付款账户',
  `print_num` int DEFAULT '0' COMMENT '打印次数',
  `order_info_id` varchar(255) DEFAULT NULL COMMENT '购货订单详情ID',
  `batch_id` int DEFAULT NULL,
  `batch_name` varchar(100) DEFAULT NULL,
  `manufacture` int DEFAULT NULL,
  `guarantee` int DEFAULT NULL,
  `validity` int DEFAULT NULL,
  `period` text COMMENT '序列号',
  `createtime` int DEFAULT NULL COMMENT '创建时间',
  `updatetime` int DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='商品销售明细';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_erp_goods_sales_order`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_erp_goods_sales_order` (
  `id` int NOT NULL AUTO_INCREMENT,
  `admin_id` int DEFAULT NULL,
  `purchase_code` varchar(50) DEFAULT NULL COMMENT '单据编号',
  `purchase_type` enum('1','2') DEFAULT '1' COMMENT '类型:1=销货,2=退货',
  `purchase_time` int DEFAULT NULL COMMENT '单据日期',
  `delivery_time` int DEFAULT NULL COMMENT '交货时间',
  `customers_id` int DEFAULT NULL COMMENT '客户',
  `receiving_address` varchar(255) DEFAULT NULL COMMENT '收货地址',
  `sale_user` int DEFAULT NULL COMMENT '销售员',
  `number` double(15,4) DEFAULT '0.0000' COMMENT '数量',
  `all_money` double(15,4) DEFAULT '0.0000' COMMENT '购货金额',
  `purchase_id` varchar(50) DEFAULT NULL COMMENT '销货单号',
  `sales_order_id` varchar(50) DEFAULT NULL COMMENT '销货订单号',
  `returns_order_id` varchar(50) DEFAULT NULL COMMENT '退货订单号',
  `discount` float(5,2) DEFAULT '10.00' COMMENT '折扣',
  `preferential_rate` float(5,2) DEFAULT '0.00' COMMENT '优惠率',
  `preferential_money` double(15,4) DEFAULT '0.0000' COMMENT '优惠额',
  `real_money` double(15,4) DEFAULT '0.0000' COMMENT '优惠后金额',
  `tax_money` double(15,4) DEFAULT NULL COMMENT '税额',
  `money_tax_price` double(15,4) DEFAULT '0.0000' COMMENT '价税合计',
  `purchase_money` double(15,4) DEFAULT '0.0000' COMMENT '采购费用',
  `preparation_id` int DEFAULT NULL COMMENT '制单人',
  `examine_id` int DEFAULT NULL COMMENT '审核人',
  `operation_id` int DEFAULT NULL COMMENT '最后操作人',
  `status` enum('0','1','2','3') DEFAULT '0' COMMENT '状态:0=未出库,1=全部出库,2=部分出库,3=已取消',
  `check_status` enum('0','1') DEFAULT '0' COMMENT '对单状态:0=未对单,1=已对单',
  `examine_status` enum('0','1') DEFAULT '0' COMMENT '审核状态:0=未审核,1=已审核',
  `examine_time` int DEFAULT NULL COMMENT '审核时间',
  `pay_money` double(15,4) DEFAULT '0.0000' COMMENT '已付款金额',
  `store_number` double(15,4) DEFAULT '0.0000' COMMENT '入库数量',
  `account` int DEFAULT NULL COMMENT '付款账户',
  `note` varchar(255) DEFAULT NULL COMMENT '备注',
  `arrears` double(15,4) DEFAULT '0.0000' COMMENT '本次欠款',
  `all_arrears` double(15,4) DEFAULT '0.0000' COMMENT '总欠款',
  `print_num` int DEFAULT '0' COMMENT '打印次数',
  `createtime` int DEFAULT NULL COMMENT '创建时间',
  `updatetime` int DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `purchase_code` (`purchase_code`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='销货订单';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_erp_goods_sales_order_info`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_erp_goods_sales_order_info` (
  `id` int NOT NULL AUTO_INCREMENT,
  `admin_id` int DEFAULT NULL,
  `purchase_code` varchar(50) DEFAULT NULL COMMENT '单据编号',
  `purchase_type` enum('1','2') DEFAULT '1' COMMENT '类型:1=销货,2=退货',
  `purchase_time` int DEFAULT NULL COMMENT '单据日期',
  `delivery_time` int DEFAULT NULL COMMENT '交货时间',
  `customers_id` int DEFAULT NULL COMMENT '客户',
  `receiving_address` varchar(255) DEFAULT NULL COMMENT '收货地址',
  `sale_user` int DEFAULT NULL COMMENT '销售员',
  `goods_code` varchar(30) NOT NULL DEFAULT '' COMMENT '商品编号',
  `goods_note` varchar(255) DEFAULT NULL COMMENT '商品备注',
  `goods_attr` int DEFAULT NULL COMMENT '商品属性',
  `goods_unit` int DEFAULT NULL COMMENT '商品单位',
  `stores_id` int DEFAULT NULL COMMENT '仓库',
  `allow_stock` double(15,4) DEFAULT '0.0000' COMMENT '可用库存',
  `number` double(15,4) DEFAULT '0.0000' COMMENT '数量',
  `purchase_number` double(15,4) DEFAULT '0.0000' COMMENT '可生成购货单数量',
  `default_unit` int DEFAULT NULL COMMENT '基本单位',
  `basic_nmber` double(15,4) DEFAULT '0.0000',
  `unit_price` double(15,4) DEFAULT '0.0000' COMMENT '购货单价',
  `cost_price` double(15,4) DEFAULT NULL COMMENT '成本价',
  `tax_included` double(15,4) DEFAULT '0.0000' COMMENT '含税单价',
  `discount` double(15,4) DEFAULT '0.0000' COMMENT '折扣率',
  `dis_money` double(15,4) DEFAULT '0.0000' COMMENT '折扣额',
  `all_money` double(15,4) DEFAULT '0.0000' COMMENT '购货金额',
  `tax_rate` double(15,4) DEFAULT '0.0000' COMMENT '税率',
  `tax_money` double(15,4) DEFAULT '0.0000' COMMENT '税额',
  `money_tax_price` double(15,4) DEFAULT '0.0000' COMMENT '价税合计',
  `remark` varchar(255) DEFAULT NULL COMMENT '备注',
  `purchase_id` varchar(50) DEFAULT NULL COMMENT '购货单号',
  `sales_order_id` varchar(50) DEFAULT NULL COMMENT '销货订单号',
  `returns_order_id` varchar(50) DEFAULT NULL COMMENT '退货订单号',
  `preferential_rate` float(5,2) DEFAULT '0.00' COMMENT '优惠率',
  `preferential_money` float(11,2) DEFAULT '0.00' COMMENT '优惠额',
  `real_money` float(11,2) DEFAULT '0.00' COMMENT '优惠后金额',
  `purchase_money` float(11,2) DEFAULT '0.00' COMMENT '采购费用',
  `preparation_id` int DEFAULT NULL COMMENT '制单人',
  `examine_id` int DEFAULT NULL COMMENT '审核人',
  `operation_id` int DEFAULT NULL COMMENT '最后操作人',
  `status` enum('0','1','2','3') DEFAULT '0' COMMENT '状态:0=未出库,1=全部出库,2=部分出库,3=已取消',
  `check_status` enum('0','1') DEFAULT '0' COMMENT '对单状态:0=未对单,1=已对单',
  `examine_status` enum('0','1') DEFAULT '0' COMMENT '审核状态:0=未审核,1=已审核',
  `examine_time` int DEFAULT NULL COMMENT '审核时间',
  `pay_money` float(11,2) DEFAULT '0.00' COMMENT '已付款金额',
  `account` int DEFAULT NULL COMMENT '付款账户',
  `print_num` int DEFAULT '0' COMMENT '打印次数',
  `library_number` int DEFAULT '0' COMMENT '未入库数量',
  `library_money` float(11,2) DEFAULT '0.00' COMMENT '未入库金额',
  `library_time` int DEFAULT NULL COMMENT '最后入库时间',
  `createtime` int DEFAULT NULL COMMENT '创建时间',
  `updatetime` int DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `purchase_code` (`purchase_code`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='销货订单详情';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_erp_goods_sales_profit`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_erp_goods_sales_profit` (
  `id` int NOT NULL AUTO_INCREMENT,
  `admin_id` int DEFAULT NULL,
  `purchase_time` int DEFAULT NULL COMMENT '单据日期',
  `customers_id` int DEFAULT NULL COMMENT '客户',
  `sale_user` int DEFAULT NULL COMMENT '销售员',
  `preparation_id` int DEFAULT NULL COMMENT '制单人',
  `examine_id` int DEFAULT NULL COMMENT '审核人',
  `purchase_code` varchar(50) DEFAULT NULL COMMENT '单据编号',
  `purchase_type` enum('1','2') DEFAULT NULL COMMENT '业务类别:1=普通销售,2=销售退货',
  `number` double(15,4) DEFAULT NULL COMMENT '数量',
  `amount` double(15,4) DEFAULT NULL COMMENT '折扣额',
  `all_money` varchar(255) DEFAULT NULL COMMENT '销售收入',
  `tax_money` double(15,4) DEFAULT NULL COMMENT '税额',
  `money_tax_price` double(15,4) DEFAULT NULL COMMENT '价税合计',
  `sales_cost` double(15,4) DEFAULT NULL COMMENT '销售成本',
  `preferential_money` double(15,4) DEFAULT NULL COMMENT '优惠额',
  `purchase_money` double(15,4) DEFAULT NULL COMMENT '销售费用',
  `real_money` double(15,4) DEFAULT NULL COMMENT '优惠后金额',
  `customers_money` double(15,4) DEFAULT NULL COMMENT '客户承担费用',
  `need_money` double(15,4) DEFAULT NULL COMMENT '应收金额',
  `pay_money` double(15,4) DEFAULT NULL COMMENT '已收款金额',
  `remark` varchar(255) DEFAULT NULL COMMENT '整单备注',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='销售利润表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_erp_goods_sales_summary`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_erp_goods_sales_summary` (
  `id` int NOT NULL AUTO_INCREMENT,
  `admin_id` int DEFAULT NULL,
  `purchase_code` varchar(50) DEFAULT NULL COMMENT '单据编号',
  `purchase_type` enum('1','2') DEFAULT '1' COMMENT '类型:1=购货,2=退货',
  `purchase_time` int DEFAULT NULL COMMENT '单据日期',
  `delivery_time` int DEFAULT NULL COMMENT '交货时间',
  `customers_id` int DEFAULT NULL COMMENT '客户',
  `receiving_address` varchar(255) DEFAULT NULL COMMENT '收货地址',
  `sale_user` int DEFAULT NULL COMMENT '销售员',
  `goods_code` varchar(30) NOT NULL DEFAULT '' COMMENT '商品编号',
  `goods_note` varchar(255) DEFAULT NULL COMMENT '商品备注',
  `goods_attr` int DEFAULT NULL COMMENT '商品属性',
  `goods_unit` int DEFAULT NULL COMMENT '商品单位',
  `stores_id` int DEFAULT NULL COMMENT '仓库',
  `allow_stock` double(15,4) DEFAULT '0.0000' COMMENT '可用库存',
  `number` double(15,4) DEFAULT '0.0000' COMMENT '数量',
  `default_unit` int DEFAULT NULL COMMENT '基本单位',
  `basic_nmber` double(15,4) DEFAULT '0.0000',
  `unit_price` double(15,4) DEFAULT '0.0000' COMMENT '购货单价',
  `cost_price` double(15,4) DEFAULT NULL,
  `tax_included` double(15,4) DEFAULT NULL COMMENT '含税单价',
  `discount` float(5,2) DEFAULT '0.00' COMMENT '折扣率',
  `dis_money` double(15,4) DEFAULT '0.0000' COMMENT '折扣额',
  `all_money` float(15,4) DEFAULT '0.0000' COMMENT '购货金额',
  `tax_rate` double(15,4) DEFAULT '0.0000' COMMENT '税率',
  `tax_money` double(15,4) DEFAULT '0.0000' COMMENT '税额',
  `money_tax_price` double(15,4) DEFAULT NULL COMMENT '价税合计',
  `remark` varchar(255) DEFAULT NULL COMMENT '备注',
  `purchase_order_id` varchar(50) DEFAULT NULL COMMENT '销货订单号',
  `sales_order_id` varchar(50) DEFAULT NULL COMMENT '销货单号',
  `returns_order_id` varchar(50) DEFAULT NULL COMMENT '退货单号',
  `preferential_rate` float(5,2) DEFAULT '0.00' COMMENT '优惠率',
  `preferential_money` double(15,4) DEFAULT '0.0000' COMMENT '优惠额',
  `real_money` double(15,4) DEFAULT '0.0000' COMMENT '优惠后金额',
  `purchase_money` double(15,4) DEFAULT '0.0000' COMMENT '采购费用',
  `customers_money` double(15,4) DEFAULT '0.0000' COMMENT '客户承担费用',
  `preparation_id` int DEFAULT NULL COMMENT '制单人',
  `examine_id` int DEFAULT NULL COMMENT '审核人',
  `operation_id` int DEFAULT NULL COMMENT '最后操作人',
  `pay_status` enum('0','1','2') DEFAULT '0' COMMENT '付款状态:0=未付款,1=部分付款,2=全部付款',
  `check_status` enum('0','1') DEFAULT '0' COMMENT '对单状态:0=未对单,1=已对单',
  `examine_status` enum('0','1') DEFAULT '0' COMMENT '审核状态:0=未审核,1=已审核',
  `examine_time` int DEFAULT NULL COMMENT '审核时间',
  `pay_money` double(15,4) DEFAULT '0.0000' COMMENT '已付款金额',
  `account` int DEFAULT NULL COMMENT '付款账户',
  `print_num` int DEFAULT '0' COMMENT '打印次数',
  `order_info_id` int DEFAULT NULL,
  `batch_id` int DEFAULT NULL COMMENT '批次ID',
  `batch_name` varchar(100) DEFAULT NULL COMMENT '批次',
  `manufacture` int DEFAULT NULL COMMENT '生产日期',
  `guarantee` int DEFAULT NULL COMMENT '保质期',
  `validity` int DEFAULT NULL COMMENT '过期时间',
  `period` text COMMENT '序列号',
  `createtime` int DEFAULT NULL COMMENT '创建时间',
  `updatetime` int DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='销售汇总表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_erp_goods_salethird`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_erp_goods_salethird` (
  `id` int NOT NULL AUTO_INCREMENT,
  `order_num` varchar(100) DEFAULT '0' COMMENT '外部订单号',
  `purchase_id` int DEFAULT NULL COMMENT '销货单ID',
  `third_id` int DEFAULT NULL COMMENT '三方ID',
  `createtime` int DEFAULT NULL COMMENT '创建时间',
  `updatetime` int DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='三方销售订单关联';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_erp_goods_serial`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_erp_goods_serial` (
  `id` int NOT NULL AUTO_INCREMENT,
  `admin_id` int DEFAULT NULL,
  `goods_id` int DEFAULT NULL COMMENT '商品ID',
  `goods_code` varchar(30) DEFAULT NULL COMMENT '商品编号',
  `stores_id` int unsigned DEFAULT NULL COMMENT '仓库ID',
  `attribute_id` varchar(255) DEFAULT NULL COMMENT '属性组合',
  `serial` varchar(30) DEFAULT NULL COMMENT '序列号',
  `status` enum('1','2','3','4','5','6','7','8','9','10','11','12','13','14','15') DEFAULT '1' COMMENT '状态:1=购货,2=购货退货,3=销货,4=销货退货,5=盘点,6=其他,7=盘盈,8=其他入库,9=盘亏,10=其他出库,11=组合单,12=组合单,13=拆卸单,14=拆卸单,15=生产',
  `store_status` enum('0','1','2') DEFAULT '0' COMMENT '在库状态:0=未入库,1=在库,2=出库',
  `remark` varchar(255) DEFAULT NULL COMMENT '备注',
  `purchase_id` int DEFAULT NULL COMMENT '购货单ID',
  `purchase_code` varchar(30) DEFAULT NULL COMMENT '购货单单号',
  `purchase_exchange_id` int DEFAULT NULL COMMENT '购货退货单ID',
  `purchase_exchange_code` varchar(30) DEFAULT NULL COMMENT '购货退货单单号',
  `sale_id` int DEFAULT NULL COMMENT '销货单ID',
  `sale_code` varchar(30) DEFAULT NULL COMMENT '销货单号',
  `sale_exchange_id` int DEFAULT NULL COMMENT '销货退货单ID',
  `sale_exchange_code` varchar(30) DEFAULT NULL COMMENT '销货退货单单号',
  `order_id` int DEFAULT NULL COMMENT '来源ID',
  `warehousing_id` int DEFAULT NULL COMMENT '入库单ID',
  `warehousing_status` enum('1','2') DEFAULT '1' COMMENT '是否其他入库:1=否,2=是',
  `warehoue_id` int DEFAULT NULL COMMENT '出库单ID',
  `assemble_id` int DEFAULT NULL COMMENT '组合单ID',
  `assemble_status` enum('1','2') DEFAULT '2' COMMENT '组装单入库',
  `split_id` int DEFAULT NULL COMMENT '拆卸单ID',
  `split_status` enum('1','2') DEFAULT '2' COMMENT '拆卸单入库',
  `old_status` int DEFAULT NULL,
  `old_store` int DEFAULT NULL,
  `createtime` int DEFAULT NULL COMMENT '创建时间',
  `updatetime` int DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='商品序列号';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_erp_goods_serial_log`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_erp_goods_serial_log` (
  `id` int NOT NULL AUTO_INCREMENT,
  `admin_id` int DEFAULT NULL,
  `goods_id` int DEFAULT NULL COMMENT '商品ID',
  `stores_id` int unsigned DEFAULT NULL COMMENT '仓库ID',
  `attribute_id` int DEFAULT NULL COMMENT '属性组合',
  `serial` varchar(30) DEFAULT NULL COMMENT '序列号',
  `business_type` enum('1','2','3','4','5','6','7','8','9','10','11','12','13','14','16') DEFAULT NULL COMMENT '业务类型:1=期初,2=普通采购,3=采购退回,4=普通销售,5=销售退回,6=调拨单,,7=盘盈,8=其他入库,9=盘亏,10=其他出库,11=组合单,12=组合单,13=拆卸单,14=拆卸单,16=生产',
  `store_status` enum('0','1','2') DEFAULT '0' COMMENT '在库状态:0=未入库,1=在库,2=出库',
  `remarks` varchar(255) DEFAULT NULL COMMENT '备注',
  `purchase_id` int DEFAULT NULL COMMENT '单据ID',
  `purchase_code` varchar(30) DEFAULT NULL COMMENT '单据编号',
  `purchase_time` int DEFAULT NULL COMMENT '单据时间',
  `customer_id` int DEFAULT NULL COMMENT '往来单位',
  `order_id` int DEFAULT NULL COMMENT '来源ID',
  `createtime` int DEFAULT NULL COMMENT '创建时间',
  `updatetime` int DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='商品序列号跟踪';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_erp_goods_stock`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_erp_goods_stock` (
  `id` int NOT NULL AUTO_INCREMENT,
  `admin_id` int DEFAULT NULL,
  `goods_id` int NOT NULL DEFAULT '0' COMMENT '商品ID',
  `goods_code` varchar(30) DEFAULT NULL COMMENT '商品编号',
  `attribute_id` int DEFAULT NULL COMMENT '属性组合ID',
  `stores_id` int NOT NULL DEFAULT '0' COMMENT '仓库ID',
  `stock` double(15,4) unsigned NOT NULL DEFAULT '0.0000' COMMENT '库存',
  `status` enum('0','1') NOT NULL DEFAULT '1' COMMENT '状态:0=关仓,1=正常',
  `period` double(15,4) unsigned DEFAULT '0.0000' COMMENT '期初数量',
  `unit_cost` double(15,4) unsigned DEFAULT '0.0000' COMMENT '单位成本',
  `price` double(15,4) unsigned DEFAULT '0.0000' COMMENT '期初总价',
  `stock_warning` enum('0','1') DEFAULT '0' COMMENT '库存预警:0=否,1=是',
  `min_stock` double(15,4) DEFAULT '0.0000' COMMENT '最低库存',
  `max_stock` double(15,4) DEFAULT '0.0000' COMMENT '最高库存',
  `address` varchar(255) DEFAULT NULL COMMENT '产地',
  `registration` varchar(100) DEFAULT NULL COMMENT '注册证号',
  `production` varchar(100) DEFAULT NULL COMMENT '生产许可证',
  `cost_money` double(15,4) DEFAULT '0.0000' COMMENT '总成本',
  `createtime` int DEFAULT NULL COMMENT '创建时间',
  `updatetime` int DEFAULT NULL COMMENT '更新时间',
  `deletetime` int DEFAULT NULL COMMENT '删除时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='商品库存表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_erp_goods_stock_warning`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_erp_goods_stock_warning` (
  `id` int NOT NULL AUTO_INCREMENT,
  `admin_id` int DEFAULT NULL,
  `goods_id` int NOT NULL DEFAULT '0' COMMENT '商品ID',
  `goods_code` varchar(30) DEFAULT NULL COMMENT '商品编号',
  `stores_id` int NOT NULL DEFAULT '0' COMMENT '仓库ID',
  `status` enum('0','1') NOT NULL DEFAULT '1' COMMENT '状态:0=关仓,1=正常',
  `stock_warning` enum('0','1') DEFAULT '0' COMMENT '库存预警:0=否,1=是',
  `min_stock` double(15,4) unsigned DEFAULT NULL COMMENT '最低库存',
  `max_stock` double(15,4) unsigned DEFAULT NULL COMMENT '最高库存',
  `createtime` int DEFAULT NULL COMMENT '创建时间',
  `updatetime` int DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='商品库存预警表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_erp_goods_store_info`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_erp_goods_store_info` (
  `id` int NOT NULL AUTO_INCREMENT,
  `order_id` int DEFAULT '0',
  `purchase_id` int DEFAULT NULL,
  `number` double(15,4) DEFAULT '0.0000',
  `money` double(15,4) DEFAULT '0.0000',
  `all_money` double(15,4) DEFAULT '0.0000',
  `createtime` int DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='入库';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_erp_income_info`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_erp_income_info` (
  `id` int NOT NULL AUTO_INCREMENT,
  `admin_id` int DEFAULT NULL,
  `purchase_code` varchar(50) DEFAULT NULL COMMENT '单据编号',
  `purchase_id` int DEFAULT NULL COMMENT '收入单ID',
  `purchase_time` int DEFAULT NULL COMMENT '单据日期',
  `business_type` enum('1','2') DEFAULT NULL COMMENT '业务类型:1=其他收入',
  `customers_id` int DEFAULT NULL COMMENT '客户',
  `income_type_id` int DEFAULT NULL COMMENT '收入类别',
  `money` double(15,4) DEFAULT NULL COMMENT '金额',
  `remark` varchar(255) DEFAULT NULL COMMENT '备注',
  `createtime` int DEFAULT NULL COMMENT '创建时间',
  `updatetime` int DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='其他收入单详情';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_erp_income_type`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_erp_income_type` (
  `id` int NOT NULL AUTO_INCREMENT,
  `admin_id` int DEFAULT NULL,
  `name` varchar(120) NOT NULL DEFAULT '' COMMENT '名称',
  `image` varchar(255) NOT NULL DEFAULT '' COMMENT '图片',
  `pid` int NOT NULL DEFAULT '0' COMMENT '上级分类id',
  `status` enum('0','1') NOT NULL DEFAULT '1' COMMENT '状态:0=禁用,1=正常',
  `path` varchar(255) DEFAULT NULL,
  `createtime` int DEFAULT NULL COMMENT '创建时间',
  `updatetime` int DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='收入类别表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_erp_inventory`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_erp_inventory` (
  `id` int NOT NULL AUTO_INCREMENT,
  `admin_id` int DEFAULT NULL,
  `purchase_code` varchar(50) DEFAULT NULL COMMENT '盘点编号',
  `purchase_time` int DEFAULT NULL COMMENT '盘点时间',
  `store_type` enum('1','2') DEFAULT '1' COMMENT '盘点仓库:1=全部仓库,2=单仓',
  `result` varchar(255) DEFAULT NULL COMMENT '盘点结果',
  `createtime` int DEFAULT NULL COMMENT '创建时间',
  `updatetime` int DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='盘点';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_erp_inventory_info`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_erp_inventory_info` (
  `id` int NOT NULL AUTO_INCREMENT,
  `admin_id` int DEFAULT NULL,
  `purchase_code` varchar(50) DEFAULT NULL COMMENT '盘点编号',
  `purchase_time` int DEFAULT NULL COMMENT '盘点时间',
  `goods_type` int DEFAULT NULL COMMENT '商品类别',
  `goods_id` int DEFAULT NULL COMMENT '商品ID',
  `stores_id` int DEFAULT NULL COMMENT '仓库ID',
  `goods_attr` int DEFAULT NULL COMMENT '商品属性',
  `stock` double(15,4) DEFAULT NULL COMMENT '系统库存',
  `invertory_stock` double(15,4) DEFAULT NULL COMMENT '盘点库存',
  `profit` double(15,4) DEFAULT NULL COMMENT '盘盈盘亏',
  `createtime` int DEFAULT NULL COMMENT '创建时间',
  `updatetime` int DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='盘点详情';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_erp_inventory_serial`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_erp_inventory_serial` (
  `id` int NOT NULL AUTO_INCREMENT,
  `admin_id` int DEFAULT NULL,
  `purchase_code` varchar(50) DEFAULT NULL COMMENT '盘点编号',
  `purchase_time` int DEFAULT NULL COMMENT '盘点时间',
  `store_type` enum('1','2') DEFAULT '1' COMMENT '盘点仓库:1=全部仓库,2=单仓',
  `result` varchar(255) DEFAULT NULL COMMENT '盘点结果',
  `createtime` int DEFAULT NULL COMMENT '创建时间',
  `updatetime` int DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='序列号盘点';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_erp_inventory_serial_info`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_erp_inventory_serial_info` (
  `id` int NOT NULL AUTO_INCREMENT,
  `admin_id` int DEFAULT NULL,
  `purchase_code` varchar(50) DEFAULT NULL COMMENT '盘点编号',
  `purchase_time` int DEFAULT NULL COMMENT '盘点时间',
  `goods_type` int DEFAULT NULL COMMENT '商品类别',
  `goods_id` int DEFAULT NULL COMMENT '商品ID',
  `stores_id` int DEFAULT NULL COMMENT '仓库ID',
  `goods_attr` int DEFAULT NULL COMMENT '商品属性',
  `stock` double(15,4) DEFAULT NULL COMMENT '系统库存',
  `invertory_stock` double(15,4) DEFAULT NULL COMMENT '盘点库存',
  `profit` double(15,4) DEFAULT NULL COMMENT '盘盈盘亏',
  `period_period` text COMMENT '盘点序列号',
  `matching` int DEFAULT NULL COMMENT '匹配',
  `inventory` int DEFAULT NULL COMMENT '盘盈',
  `loss` int DEFAULT NULL COMMENT '盘亏',
  `createtime` int DEFAULT NULL COMMENT '创建时间',
  `updatetime` int DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='序列号盘点详情';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_erp_invoicing`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_erp_invoicing` (
  `id` int NOT NULL AUTO_INCREMENT,
  `admin_id` int DEFAULT NULL,
  `suppliers_code` varchar(50) DEFAULT NULL COMMENT '供应商编号',
  `status` enum('0','1','2','3','4','5') DEFAULT NULL COMMENT '状态:0=未付款,1=全部付款,2=部分付款,3=未退款,4=全部退款,5=部分退款',
  `bill_time` int DEFAULT NULL COMMENT '单据日期',
  `bill_code` varchar(50) DEFAULT NULL COMMENT '源单据编号',
  `bill_codes` varchar(50) DEFAULT NULL COMMENT '单据编号',
  `money` double(15,4) DEFAULT NULL COMMENT '金额',
  `tax` double(15,4) DEFAULT NULL COMMENT '税额',
  `real_money` double(15,4) DEFAULT NULL COMMENT '价税合计',
  `pay_money` double(15,4) DEFAULT '0.0000' COMMENT '已开票金额',
  `give_money` double(15,4) DEFAULT NULL COMMENT '未开票金额',
  `invoice_status` enum('0','1') DEFAULT '0' COMMENT '开票状态:0=未开,1=已开',
  `createtime` int DEFAULT NULL COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='采购开票表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_erp_invoicing_log`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_erp_invoicing_log` (
  `id` int NOT NULL AUTO_INCREMENT,
  `admin_id` int DEFAULT NULL,
  `suppliers_code` varchar(50) DEFAULT NULL COMMENT '供应商编号',
  `status` enum('0','1','2','3','4','5') DEFAULT NULL COMMENT '状态:0=未付款,1=全部付款,2=部分付款,3=未退款,4=全部退款,5=部分退款',
  `bill_time` int DEFAULT NULL COMMENT '单据日期',
  `bill_code` varchar(50) DEFAULT NULL COMMENT '源单据编号',
  `bill_codes` varchar(50) DEFAULT NULL COMMENT '单据编号',
  `money` double(15,4) DEFAULT NULL COMMENT '金额',
  `tax` double(15,4) DEFAULT NULL COMMENT '税额',
  `real_money` double(15,4) DEFAULT NULL COMMENT '价税合计',
  `pay_money` double(15,4) DEFAULT '0.0000' COMMENT '已开票金额',
  `give_money` double(15,4) DEFAULT NULL COMMENT '未开票金额',
  `createtime` int DEFAULT NULL COMMENT '创建时间',
  `times` int DEFAULT NULL COMMENT '开票时间',
  `invoice` varchar(255) DEFAULT NULL COMMENT '发票号',
  `invoice_header` varchar(255) DEFAULT NULL COMMENT '发票抬头',
  `invoicing_id` int DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='采购开票表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_erp_payment`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_erp_payment` (
  `id` int NOT NULL AUTO_INCREMENT,
  `admin_id` int DEFAULT NULL,
  `purchase_code` varchar(50) DEFAULT NULL COMMENT '单据编号',
  `purchase_time` int DEFAULT NULL COMMENT '单据时间',
  `business_type` enum('1','2') DEFAULT '1' COMMENT '类型:1=预付单,2=收款单',
  `customers_id` int DEFAULT NULL COMMENT '客户',
  `money` double(15,4) DEFAULT NULL COMMENT '付款合计',
  `write_off` double(15,4) DEFAULT NULL COMMENT '核销金额',
  `discount` double(15,4) DEFAULT NULL COMMENT '整单折扣',
  `advance` double(15,4) DEFAULT NULL COMMENT '预付款',
  `staff_member_id` int unsigned DEFAULT NULL COMMENT '付款人',
  `remarks` varchar(255) DEFAULT NULL COMMENT '备注',
  `preparation_id` int DEFAULT NULL COMMENT '制单人',
  `examine_id` int DEFAULT NULL COMMENT '审核人',
  `operation_id` int DEFAULT NULL COMMENT '最后操作人',
  `examine_time` int DEFAULT NULL COMMENT '审核时间',
  `examine_status` enum('0','1') NOT NULL DEFAULT '0' COMMENT '审核状态:0=未审核,1=已审核',
  `print_num` int DEFAULT NULL COMMENT '打印次数',
  `createtime` int DEFAULT NULL COMMENT '创建时间',
  `updatetime` int DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='付款单';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_erp_payment_account`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_erp_payment_account` (
  `id` int NOT NULL AUTO_INCREMENT,
  `admin_id` int DEFAULT NULL,
  `collection_id` int NOT NULL DEFAULT '0' COMMENT '收款单ID',
  `purchase_code` varchar(50) NOT NULL DEFAULT '' COMMENT '单据编号',
  `purchase_time` int NOT NULL DEFAULT '0' COMMENT '单据时间',
  `accounts_id` int unsigned NOT NULL DEFAULT '0' COMMENT '结算账户',
  `money` double(15,4) NOT NULL DEFAULT '0.0000' COMMENT '付款金额',
  `settlementtype` int unsigned NOT NULL DEFAULT '0' COMMENT '结算方式',
  `number` varchar(50) DEFAULT '' COMMENT '结算号',
  `remark` varchar(255) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='付款账户信息';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_erp_payment_log`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_erp_payment_log` (
  `id` int NOT NULL AUTO_INCREMENT,
  `admin_id` int DEFAULT NULL,
  `suppliers_code` varchar(50) DEFAULT NULL COMMENT '供应商编号',
  `business_type` enum('1','2','3','4','5','6','7') DEFAULT NULL COMMENT '业务类型:1=普通采购,2=采购退回,3=付款,4=退款,5=期初应付款,6=期初预付款,7=核销',
  `bill_time` int DEFAULT NULL COMMENT '单据日期',
  `bill_code` varchar(50) DEFAULT NULL COMMENT '源单据编号',
  `bill_codes` varchar(50) DEFAULT NULL COMMENT '单据编号',
  `purchase_money` double(15,4) DEFAULT NULL COMMENT '采购金额',
  `money_tax_price` double(15,4) DEFAULT NULL,
  `discount_money` double(15,4) DEFAULT NULL COMMENT '优惠金额',
  `real_money` double(15,4) DEFAULT NULL COMMENT '优惠后金额',
  `pay_money` double(15,4) DEFAULT NULL COMMENT '本次付款',
  `give_money` double(15,4) DEFAULT NULL COMMENT '应付款余额',
  `reamrk` varchar(255) DEFAULT NULL COMMENT '备注',
  `createtime` int DEFAULT NULL COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='采购付款一览表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_erp_payment_sale`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_erp_payment_sale` (
  `id` int NOT NULL AUTO_INCREMENT,
  `admin_id` int DEFAULT NULL,
  `collection_id` int NOT NULL DEFAULT '0' COMMENT '收款单ID',
  `purchase_code` varchar(50) NOT NULL DEFAULT '' COMMENT '单据编号',
  `purchase_time` int NOT NULL DEFAULT '0' COMMENT '单据时间',
  `purchase_number` varchar(50) NOT NULL DEFAULT '' COMMENT '原单编号',
  `business_type` enum('1','2','3','4','5','6') NOT NULL DEFAULT '1' COMMENT '单据类型:1=预收单,2=预付单,3=应收,4=应付,5=期初应收/付款,6=期初预收/付款',
  `times` int NOT NULL DEFAULT '0' COMMENT '单据日期',
  `money` double(15,4) NOT NULL DEFAULT '0.0000' COMMENT '单据金额',
  `write_off` double(15,4) DEFAULT NULL COMMENT '已核销金额',
  `write_off_money` double(15,4) DEFAULT NULL COMMENT '未核销金额',
  `write_off_price` double(15,4) DEFAULT '0.0000' COMMENT '本次核销金额',
  `remark` varchar(255) DEFAULT NULL COMMENT '单据备注',
  `old_money` double(15,4) DEFAULT NULL,
  `old_status` tinyint DEFAULT NULL,
  `old_money_i` double(15,4) DEFAULT NULL,
  `old_status_i` tinyint DEFAULT NULL,
  `business_name` varchar(255) DEFAULT NULL COMMENT '单据名称',
  `source_id` int NOT NULL DEFAULT '0' COMMENT '源单ID',
  `type` enum('1','2','3','4') DEFAULT NULL COMMENT '类型:1=购货/销货,2=退货,3=期初,4=核销',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='付款单单据信息';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_erp_prefix`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_erp_prefix` (
  `id` int NOT NULL AUTO_INCREMENT,
  `admin_id` int DEFAULT NULL,
  `type` tinyint unsigned NOT NULL DEFAULT '1' COMMENT '单据类型',
  `name` varchar(255) NOT NULL DEFAULT '' COMMENT '单据',
  `prefix` varchar(10) NOT NULL DEFAULT '' COMMENT '前缀',
  `year` enum('1','2') NOT NULL DEFAULT '1' COMMENT '年',
  `month` enum('1','2') NOT NULL DEFAULT '1' COMMENT '月',
  `day` enum('1','2') NOT NULL DEFAULT '1' COMMENT '日',
  `hour` enum('1','2') NOT NULL DEFAULT '1' COMMENT '时',
  `minute` enum('1','2') NOT NULL DEFAULT '1' COMMENT '分',
  `second` enum('1','2') NOT NULL DEFAULT '1' COMMENT '秒',
  `start` int unsigned DEFAULT NULL COMMENT '起始数',
  `strlen` int unsigned DEFAULT '0' COMMENT '自定义数字长度',
  `example` varchar(50) DEFAULT NULL COMMENT '示例',
  `dbname` varchar(50) DEFAULT NULL COMMENT '表',
  `field` varchar(50) DEFAULT NULL COMMENT '字段名',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `prefix` (`prefix`) USING BTREE,
  UNIQUE KEY `type` (`type`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='单号设置';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_erp_sales_invoicing`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_erp_sales_invoicing` (
  `id` int NOT NULL AUTO_INCREMENT,
  `admin_id` int DEFAULT NULL,
  `customers_id` int DEFAULT NULL COMMENT '客户',
  `status` enum('0','1','2','3','4','5') DEFAULT NULL COMMENT '状态:0=未收款,1=全部收款,2=部分收款,3=未退款,4=全部退款,5=部分退款',
  `bill_time` int DEFAULT NULL COMMENT '单据日期',
  `bill_code` varchar(50) DEFAULT NULL COMMENT '源单据编号',
  `bill_codes` varchar(50) DEFAULT NULL COMMENT '单据编号',
  `money` double(15,4) DEFAULT NULL COMMENT '金额',
  `tax` double(15,4) DEFAULT NULL COMMENT '税额',
  `real_money` double(15,4) DEFAULT NULL COMMENT '价税合计',
  `customers_money` double(15,4) DEFAULT '0.0000' COMMENT '客户承担费用',
  `pay_money` double(15,4) DEFAULT '0.0000' COMMENT '已开票金额',
  `give_money` double(15,4) DEFAULT NULL COMMENT '未开票金额',
  `invoice_status` enum('0','1') DEFAULT '0' COMMENT '开票状态:0=未开,1=已开',
  `createtime` int DEFAULT NULL COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='销货开票表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_erp_sales_invoicing_log`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_erp_sales_invoicing_log` (
  `id` int NOT NULL AUTO_INCREMENT,
  `admin_id` int DEFAULT NULL,
  `customers_id` int DEFAULT NULL COMMENT '客户',
  `status` enum('0','1','2','3','4','5') DEFAULT NULL COMMENT '状态:0=未收款,1=全部收款,2=部分收款,3=未退款,4=全部退款,5=部分退款',
  `bill_time` int DEFAULT NULL COMMENT '单据日期',
  `bill_code` varchar(50) DEFAULT NULL COMMENT '源单据编号',
  `bill_codes` varchar(50) DEFAULT NULL COMMENT '单据编号',
  `money` double(15,4) DEFAULT NULL COMMENT '金额',
  `tax` double(15,4) DEFAULT NULL COMMENT '税额',
  `real_money` double(15,4) DEFAULT NULL COMMENT '价税合计',
  `customers_money` double(15,4) DEFAULT '0.0000' COMMENT '客户承担费用',
  `pay_money` double(15,4) DEFAULT '0.0000' COMMENT '已开票金额',
  `give_money` double(15,4) DEFAULT NULL COMMENT '未开票金额',
  `createtime` int DEFAULT NULL COMMENT '创建时间',
  `times` int DEFAULT NULL COMMENT '开票时间',
  `invoice` varchar(255) DEFAULT NULL COMMENT '发票号',
  `invoice_header` varchar(255) DEFAULT NULL COMMENT '发票抬头',
  `invoicing_id` int DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='销货开票表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_erp_sales_payment_log`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_erp_sales_payment_log` (
  `id` int NOT NULL AUTO_INCREMENT,
  `admin_id` int DEFAULT NULL,
  `customers_id` int DEFAULT NULL COMMENT '客户',
  `sale_user` int DEFAULT NULL COMMENT '销售员',
  `business_type` enum('1','2','3','4','5','6','7') DEFAULT NULL COMMENT '业务类型:1=销货,2=销货退回,3=付款,4=退款,5=期初应收款,6=期初预收款,7=核销',
  `bill_time` int DEFAULT NULL COMMENT '单据日期',
  `bill_code` varchar(50) DEFAULT NULL COMMENT '源单据编号',
  `bill_codes` varchar(50) DEFAULT NULL COMMENT '单据编号',
  `purchase_money` double(15,4) DEFAULT NULL COMMENT '销售金额',
  `money_tax_price` double(15,4) DEFAULT NULL COMMENT '价税合计',
  `discount_money` double(15,4) DEFAULT NULL COMMENT '优惠金额',
  `real_money` double(15,4) DEFAULT NULL COMMENT '优惠后金额',
  `customers_money` double(15,4) DEFAULT '0.0000' COMMENT '客户承担费用',
  `pay_money` double(15,4) DEFAULT NULL COMMENT '本次付款',
  `give_money` double(15,4) DEFAULT NULL COMMENT '应收款余额',
  `reamrk` varchar(255) DEFAULT NULL COMMENT '备注',
  `createtime` int DEFAULT NULL COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='销货收款一览表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_erp_sets`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_erp_sets` (
  `id` int NOT NULL AUTO_INCREMENT,
  `admin_id` int DEFAULT NULL,
  `type` tinyint DEFAULT '1' COMMENT '类型:1=基础参数,2=功能参数',
  `name` varchar(100) DEFAULT NULL COMMENT '变量名称',
  `nickname` varchar(100) DEFAULT NULL COMMENT '名称',
  `value` varchar(255) DEFAULT NULL COMMENT '设置值',
  `is_edit` enum('0','1') DEFAULT '0' COMMENT '修改:0=允许,1=不允许',
  `notice` varchar(255) DEFAULT NULL COMMENT '提示',
  `explain` varchar(255) DEFAULT NULL COMMENT '说明',
  `is_checked` tinyint DEFAULT '0' COMMENT '是否选中',
  `set` text COMMENT '场景',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `name` (`name`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='设置';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_erp_settlementtype`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_erp_settlementtype` (
  `id` int NOT NULL AUTO_INCREMENT,
  `admin_id` int DEFAULT NULL,
  `name` varchar(100) NOT NULL DEFAULT '' COMMENT '结算方式名称',
  `image` varchar(255) DEFAULT '' COMMENT '图片',
  `status` enum('0','1') NOT NULL DEFAULT '1' COMMENT '结算方式状态:0=禁用,1=启用',
  `createtime` int DEFAULT NULL COMMENT '创建时间',
  `updatetime` int DEFAULT NULL COMMENT '编辑时间',
  `deletetime` int DEFAULT NULL COMMENT '删除时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='结算方式表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_erp_spendingtype`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_erp_spendingtype` (
  `id` int NOT NULL,
  `spending_type_name` varchar(120) NOT NULL DEFAULT '' COMMENT '类型名称',
  `spending_type_image` varchar(255) DEFAULT '' COMMENT '图片',
  `status` enum('0','1') DEFAULT '1' COMMENT '状态:0=禁用,1=启用',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='支出类别表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_erp_split`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_erp_split` (
  `id` int NOT NULL AUTO_INCREMENT,
  `admin_id` int DEFAULT NULL,
  `purchase_code` varchar(50) DEFAULT NULL COMMENT '单据编号',
  `purchase_time` int DEFAULT NULL COMMENT '单据日期',
  `business_type` enum('1','2') DEFAULT NULL COMMENT '业务类型:1=拆卸单',
  `money` double(15,4) DEFAULT NULL COMMENT '费用',
  `remark` varchar(255) DEFAULT NULL COMMENT '备注',
  `preparation_id` int DEFAULT NULL COMMENT '制单人',
  `operation_id` int DEFAULT NULL COMMENT '最后操作人',
  `examine_id` int DEFAULT NULL COMMENT '审核人',
  `examine_status` enum('0','1') DEFAULT '0' COMMENT '审核状态:0=未审核,1=已审核',
  `examine_time` int DEFAULT NULL COMMENT '审核时间',
  `print_num` int DEFAULT NULL COMMENT '打印次数',
  `createtime` int DEFAULT NULL COMMENT '创建时间',
  `updatetime` int DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='拆卸单';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_erp_split_info`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_erp_split_info` (
  `id` int NOT NULL AUTO_INCREMENT,
  `admin_id` int DEFAULT NULL,
  `purchase_code` varchar(50) NOT NULL DEFAULT '' COMMENT '单据编号',
  `purchase_time` int NOT NULL DEFAULT '0' COMMENT '单据日期',
  `business_type` enum('1','2') NOT NULL DEFAULT '1' COMMENT '类型:1=拆卸单',
  `type` enum('1','2') NOT NULL DEFAULT '2' COMMENT '类型:1=组合件,2=子件',
  `goods_id` int NOT NULL DEFAULT '0' COMMENT '商品ID',
  `goods_code` varchar(50) NOT NULL DEFAULT '' COMMENT '商品编号',
  `goods_attr` int DEFAULT NULL COMMENT '商品属性',
  `goods_unit` int NOT NULL DEFAULT '0' COMMENT '商品单位',
  `stores_id` int NOT NULL DEFAULT '0' COMMENT '仓库',
  `allow_stock` double(15,4) DEFAULT '0.0000' COMMENT '可用库存',
  `number` double(15,4) DEFAULT '0.0000' COMMENT '数量',
  `period` text COMMENT '序列号',
  `basic_number` double(15,4) DEFAULT '0.0000' COMMENT '基本数量',
  `unit_cost` double DEFAULT '0' COMMENT '单位成本',
  `cost_money` double DEFAULT '0' COMMENT '成本',
  `remark` varchar(255) DEFAULT NULL COMMENT '备注',
  `batch_id` int DEFAULT NULL COMMENT '批次ID',
  `batch` varchar(255) DEFAULT NULL COMMENT '批次',
  `manufacture` int DEFAULT NULL COMMENT '生产日期',
  `guarantee` int DEFAULT NULL COMMENT '保质期',
  `validity` int DEFAULT NULL COMMENT '过期时间',
  `preparation_id` int DEFAULT NULL COMMENT '制单人',
  `operation_id` int DEFAULT NULL COMMENT '最后操作人',
  `examine_id` int DEFAULT NULL COMMENT '审核人',
  `examine_status` enum('0','1') DEFAULT '0' COMMENT '审核状态:0=未审核,1=已审核',
  `examine_time` int DEFAULT NULL COMMENT '审核时间',
  `print_num` int DEFAULT '0' COMMENT '打印次数',
  `createtime` int DEFAULT NULL COMMENT '创建时间',
  `updatetime` int DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='拆卸单详情';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_erp_split_info_temp`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_erp_split_info_temp` (
  `id` int NOT NULL AUTO_INCREMENT,
  `admin_id` int DEFAULT NULL,
  `purchase_id` int DEFAULT NULL,
  `purchase_code` varchar(50) NOT NULL DEFAULT '' COMMENT '单据编号',
  `purchase_time` int NOT NULL DEFAULT '0' COMMENT '单据日期',
  `business_type` enum('1','2') NOT NULL DEFAULT '1' COMMENT '类型:1=拆卸单',
  `type` enum('1','2') NOT NULL DEFAULT '2' COMMENT '类型:1=组合件,2=子件',
  `goods_id` int NOT NULL DEFAULT '0' COMMENT '商品ID',
  `goods_code` varchar(50) NOT NULL DEFAULT '' COMMENT '商品编号',
  `goods_attr` int DEFAULT NULL COMMENT '商品属性',
  `goods_unit` int NOT NULL DEFAULT '0' COMMENT '商品单位',
  `stores_id` int NOT NULL DEFAULT '0' COMMENT '仓库',
  `allow_stock` double(15,4) DEFAULT '0.0000' COMMENT '可用库存',
  `number` double(15,4) DEFAULT '0.0000' COMMENT '数量',
  `period` text COMMENT '序列号',
  `basic_number` double(15,4) DEFAULT '0.0000' COMMENT '基本数量',
  `unit_cost` double DEFAULT '0' COMMENT '单位成本',
  `cost_money` double DEFAULT '0' COMMENT '成本',
  `remark` varchar(255) DEFAULT NULL COMMENT '备注',
  `batch_id` int DEFAULT NULL COMMENT '批次ID',
  `batch` varchar(255) DEFAULT NULL COMMENT '批次',
  `manufacture` int DEFAULT NULL COMMENT '生产日期',
  `guarantee` int DEFAULT NULL COMMENT '保质期',
  `validity` int DEFAULT NULL COMMENT '过期时间',
  `preparation_id` int DEFAULT NULL COMMENT '制单人',
  `operation_id` int DEFAULT NULL COMMENT '最后操作人',
  `examine_id` int DEFAULT NULL COMMENT '审核人',
  `examine_status` enum('0','1') DEFAULT '0' COMMENT '审核状态:0=未审核,1=已审核',
  `examine_time` int DEFAULT NULL COMMENT '审核时间',
  `print_num` int DEFAULT '0' COMMENT '打印次数',
  `createtime` int DEFAULT NULL COMMENT '创建时间',
  `updatetime` int DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='拆卸单详情模板';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_erp_split_temp`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_erp_split_temp` (
  `id` int NOT NULL AUTO_INCREMENT,
  `admin_id` int DEFAULT NULL,
  `temp_name` varchar(255) NOT NULL DEFAULT '' COMMENT '模板名称',
  `purchase_code` varchar(50) DEFAULT NULL COMMENT '单据编号',
  `purchase_time` int DEFAULT NULL COMMENT '单据日期',
  `business_type` enum('1','2') DEFAULT NULL COMMENT '业务类型:1=拆卸单',
  `money` double(15,4) DEFAULT NULL COMMENT '费用',
  `remark` varchar(255) DEFAULT NULL COMMENT '备注',
  `preparation_id` int DEFAULT NULL COMMENT '制单人',
  `operation_id` int DEFAULT NULL COMMENT '最后操作人',
  `examine_id` int DEFAULT NULL COMMENT '审核人',
  `examine_status` enum('0','1') DEFAULT '0' COMMENT '审核状态:0=未审核,1=已审核',
  `examine_time` int DEFAULT NULL COMMENT '审核时间',
  `print_num` int DEFAULT NULL COMMENT '打印次数',
  `createtime` int DEFAULT NULL COMMENT '创建时间',
  `updatetime` int DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='拆卸单模板';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_erp_staff_member`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_erp_staff_member` (
  `id` int NOT NULL AUTO_INCREMENT,
  `admin_id` int DEFAULT NULL,
  `men_code` varchar(100) DEFAULT NULL COMMENT '职员编号',
  `name` varchar(100) DEFAULT NULL COMMENT '职员名称',
  `phone` varchar(20) DEFAULT NULL COMMENT '电话',
  `status` enum('0','1') DEFAULT '1' COMMENT '状态:0=禁用,1=正常',
  `createtime` int DEFAULT NULL COMMENT '创建时间',
  `updatetime` int DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='职员表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_erp_suppliers_user`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_erp_suppliers_user` (
  `id` int NOT NULL AUTO_INCREMENT,
  `admin_id` int DEFAULT NULL,
  `supplyer_code` varchar(30) NOT NULL DEFAULT '' COMMENT '供应商编号',
  `sid` int unsigned DEFAULT NULL COMMENT '供应商ID',
  `first_connect_name` varchar(100) NOT NULL DEFAULT '' COMMENT '联系人名称',
  `first_connect_mobile` varchar(11) NOT NULL COMMENT '手机',
  `first_connect_phone` varchar(20) NOT NULL COMMENT '座机',
  `qq_wx_email` varchar(120) NOT NULL COMMENT 'QQ/微信/Email',
  `connect_address` varchar(255) NOT NULL COMMENT '联系地址',
  `is_first` tinyint DEFAULT '0' COMMENT '首要联系人:0=否,1=是',
  `status` enum('0','1') NOT NULL DEFAULT '1' COMMENT '状态:0=禁用,1=正常',
  `createtime` int DEFAULT NULL COMMENT '创建时间',
  `updatetime` int DEFAULT NULL COMMENT '更新时间',
  `deletetime` int DEFAULT NULL COMMENT '删除时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='供应商联系人';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_erp_supplyertype`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_erp_supplyertype` (
  `id` int NOT NULL AUTO_INCREMENT,
  `admin_id` int DEFAULT NULL,
  `supplier_type_name` varchar(120) NOT NULL DEFAULT '' COMMENT '类别名称',
  `supplier_type_img` varchar(255) NOT NULL DEFAULT '' COMMENT '图片',
  `pid` int NOT NULL DEFAULT '0' COMMENT '上级',
  `path` varchar(255) DEFAULT NULL COMMENT 'path',
  `status` enum('0','1') NOT NULL DEFAULT '1' COMMENT '状态:0=禁用,1=正常',
  `weigh` int DEFAULT NULL,
  `createtime` int DEFAULT NULL COMMENT '创建时间',
  `updatetime` int DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='供应商分类';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_erp_third_account`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_erp_third_account` (
  `id` int NOT NULL AUTO_INCREMENT,
  `name` varchar(120) NOT NULL DEFAULT '' COMMENT '名称',
  `image` varchar(255) NOT NULL DEFAULT '' COMMENT '图片',
  `account` varchar(30) NOT NULL DEFAULT '' COMMENT '账号',
  `salt` varchar(100) DEFAULT NULL COMMENT '秘钥',
  `status` enum('0','1') NOT NULL DEFAULT '1' COMMENT '状态:0=禁用,1=正常',
  `createtime` int DEFAULT NULL COMMENT '创建时间',
  `updatetime` int DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='接口账号表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_erp_transfers`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_erp_transfers` (
  `id` int NOT NULL,
  `documents_code` varchar(50) NOT NULL COMMENT '单据编号',
  `documents_date` varchar(50) NOT NULL COMMENT '单据日期',
  `goods_code` varchar(100) NOT NULL COMMENT '商品编号',
  `goods_type_id` int NOT NULL COMMENT '商品类别',
  `goodsnote` text NOT NULL COMMENT '商品备注',
  `createtime` int NOT NULL COMMENT '创建时间',
  `updatetime` int NOT NULL COMMENT '更新时间',
  `deletetime` int DEFAULT NULL COMMENT '删除时间',
  `num` int NOT NULL COMMENT '数量',
  `unit_id` int NOT NULL COMMENT '单位',
  `out_store_id` int NOT NULL COMMENT '调出仓库',
  `to_store_id` int NOT NULL COMMENT '调入仓库',
  `maker_id` int NOT NULL COMMENT '制单人',
  `fl_note` text NOT NULL COMMENT '分录备注',
  `audit` varchar(100) NOT NULL COMMENT '审核人',
  `main_note` text NOT NULL COMMENT '备注',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_erp_unit_conversion`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_erp_unit_conversion` (
  `id` int NOT NULL AUTO_INCREMENT,
  `admin_id` int DEFAULT NULL,
  `unit_name` varchar(255) DEFAULT NULL,
  `main_unit` int NOT NULL DEFAULT '0' COMMENT '主计量单位',
  `vice_unit` int NOT NULL DEFAULT '0' COMMENT '副计量单位',
  `advance_rate` float(11,2) NOT NULL DEFAULT '0.00' COMMENT '换算进率',
  `createtime` int DEFAULT NULL COMMENT '创建时间',
  `updatetime` int DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='计量单位换算';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_erp_user`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_erp_user` (
  `id` int unsigned NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `admin_id` int DEFAULT NULL,
  `group_id` int unsigned DEFAULT '0' COMMENT '组别ID',
  `username` varchar(32) DEFAULT '' COMMENT '用户名',
  `nickname` varchar(50) DEFAULT '' COMMENT '昵称',
  `password` varchar(32) DEFAULT '' COMMENT '密码',
  `salt` varchar(30) DEFAULT '' COMMENT '密码盐',
  `email` varchar(100) DEFAULT '' COMMENT '电子邮箱',
  `mobile` varchar(11) DEFAULT '' COMMENT '手机号',
  `avatar` varchar(255) DEFAULT '' COMMENT '头像',
  `level` tinyint unsigned NOT NULL DEFAULT '0' COMMENT '等级',
  `gender` tinyint unsigned NOT NULL DEFAULT '0' COMMENT '性别',
  `birthday` date DEFAULT NULL COMMENT '生日',
  `bio` varchar(100) DEFAULT '' COMMENT '格言',
  `money` decimal(10,2) NOT NULL DEFAULT '0.00' COMMENT '余额',
  `score` int NOT NULL DEFAULT '0' COMMENT '积分',
  `successions` int unsigned NOT NULL DEFAULT '1' COMMENT '连续登录天数',
  `maxsuccessions` int unsigned NOT NULL DEFAULT '1' COMMENT '最大连续登录天数',
  `prevtime` int DEFAULT NULL COMMENT '上次登录时间',
  `logintime` int DEFAULT NULL COMMENT '登录时间',
  `loginip` varchar(50) DEFAULT '' COMMENT '登录IP',
  `loginfailure` tinyint unsigned NOT NULL DEFAULT '0' COMMENT '失败次数',
  `joinip` varchar(50) DEFAULT '' COMMENT '加入IP',
  `jointime` int DEFAULT NULL COMMENT '加入时间',
  `createtime` int DEFAULT NULL COMMENT '创建时间',
  `updatetime` int DEFAULT NULL COMMENT '更新时间',
  `deletetime` int DEFAULT NULL COMMENT '删除时间',
  `token` varchar(50) DEFAULT '' COMMENT 'Token',
  `status` varchar(30) DEFAULT 'normal' COMMENT '状态',
  `verification` varchar(255) DEFAULT '' COMMENT '验证',
  `binding` tinyint NOT NULL DEFAULT '2' COMMENT '是否绑定',
  `customers_id` int DEFAULT NULL COMMENT '客户ID',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `email` (`email`) USING BTREE,
  KEY `mobile` (`mobile`) USING BTREE,
  KEY `username` (`username`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='会员表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_erp_user_address`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_erp_user_address` (
  `id` int NOT NULL AUTO_INCREMENT,
  `admin_id` int DEFAULT NULL,
  `user_id` int DEFAULT NULL COMMENT '用户ID',
  `connect_name` varchar(100) NOT NULL COMMENT '联系人',
  `connect_mobile` varchar(11) NOT NULL COMMENT '手机号码',
  `province` int NOT NULL DEFAULT '0' COMMENT '省',
  `city` int NOT NULL DEFAULT '0' COMMENT '市',
  `area` int NOT NULL DEFAULT '0' COMMENT '区',
  `detail_address` varchar(255) NOT NULL COMMENT '详细地址',
  `is_default` enum('0','1') NOT NULL DEFAULT '0' COMMENT '默认地址:0=否,1=是',
  `createtime` int DEFAULT NULL COMMENT '创建时间',
  `updatetime` int DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='收货地址表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_erp_user_applet`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_erp_user_applet` (
  `id` int unsigned NOT NULL AUTO_INCREMENT,
  `user_id` int DEFAULT NULL COMMENT '用户ID',
  `name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT '' COMMENT '组名',
  `rules` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci COMMENT '权限节点',
  `applet` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci COMMENT '小程序规则',
  `createtime` int DEFAULT NULL COMMENT '添加时间',
  `updatetime` int DEFAULT NULL COMMENT '更新时间',
  `status` enum('normal','hidden') CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '状态',
  `image` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '图片',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='会员常用设置表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_erp_user_cart`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_erp_user_cart` (
  `id` int NOT NULL AUTO_INCREMENT,
  `user_id` int DEFAULT NULL COMMENT '用户ID',
  `goods_id` int NOT NULL DEFAULT '0' COMMENT '商品ID',
  `goods_unit` int DEFAULT NULL COMMENT '商品单位',
  `goods_attr` int DEFAULT NULL COMMENT '商品属性',
  `number` float(11,2) NOT NULL DEFAULT '0.00' COMMENT '数量',
  `createtime` int DEFAULT NULL COMMENT '创建时间',
  `updatetime` int DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='购物车表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_erp_user_config`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_erp_user_config` (
  `id` int unsigned NOT NULL AUTO_INCREMENT,
  `admin_id` int DEFAULT NULL,
  `name` varchar(30) DEFAULT '' COMMENT '变量名',
  `group` varchar(30) DEFAULT '' COMMENT '分组',
  `title` varchar(100) DEFAULT '' COMMENT '变量标题',
  `tip` varchar(100) DEFAULT '' COMMENT '变量描述',
  `type` varchar(30) DEFAULT '' COMMENT '类型:string,text,int,bool,array,datetime,date,file',
  `value` text COMMENT '变量值',
  `content` text COMMENT '变量字典数据',
  `rule` varchar(100) DEFAULT '' COMMENT '验证规则',
  `extend` varchar(255) DEFAULT '' COMMENT '扩展属性',
  `setting` varchar(255) DEFAULT '' COMMENT '配置',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `name` (`name`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='系统配置';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_erp_user_history`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_erp_user_history` (
  `id` int NOT NULL AUTO_INCREMENT,
  `user_id` int DEFAULT NULL COMMENT '用户ID',
  `goods_id` int NOT NULL DEFAULT '0' COMMENT '商品ID',
  `goods_unit` int DEFAULT NULL COMMENT '商品单位',
  `goods_attr` int DEFAULT NULL COMMENT '商品属性',
  `look_date` varchar(50) DEFAULT NULL COMMENT '日期',
  `createtime` int DEFAULT NULL COMMENT '创建时间',
  `updatetime` int DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='浏览历史表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_erp_user_oauth`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_erp_user_oauth` (
  `id` int unsigned NOT NULL AUTO_INCREMENT,
  `user_id` int DEFAULT '0' COMMENT '用户',
  `provider` varchar(50) NOT NULL COMMENT '厂商',
  `platform` varchar(50) NOT NULL COMMENT '平台',
  `unionid` varchar(50) DEFAULT NULL COMMENT '厂商ID',
  `openid` varchar(50) NOT NULL COMMENT '平台ID',
  `nickname` varchar(255) DEFAULT '' COMMENT '昵称',
  `sex` tinyint(1) DEFAULT '0' COMMENT '性别',
  `country` varchar(50) DEFAULT NULL COMMENT '国家',
  `province` varchar(50) DEFAULT NULL COMMENT '省',
  `city` varchar(50) DEFAULT NULL COMMENT '市',
  `headimgurl` varchar(500) DEFAULT '' COMMENT '头像',
  `logintime` int DEFAULT NULL COMMENT '登录时间',
  `logincount` int DEFAULT '0' COMMENT '累计登陆',
  `expire_in` int DEFAULT NULL COMMENT '过期周期(s)',
  `expiretime` int DEFAULT NULL COMMENT '过期时间',
  `session_key` varchar(45) DEFAULT '' COMMENT 'session_key',
  `refresh_token` varchar(110) DEFAULT NULL COMMENT 'refresh_token',
  `access_token` varchar(110) DEFAULT NULL COMMENT 'access_token',
  `createtime` int DEFAULT '0' COMMENT '创建时间',
  `updatetime` int DEFAULT '0' COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='第三方授权';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_erp_user_order`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_erp_user_order` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `user_id` int DEFAULT NULL COMMENT '用户ID',
  `name` varchar(255) DEFAULT NULL COMMENT '姓名',
  `phone` varchar(20) DEFAULT NULL COMMENT '联系方式',
  `address` varchar(255) DEFAULT NULL COMMENT '详细地址',
  `order_num` varchar(50) DEFAULT '' COMMENT '订单号',
  `order_price` float(11,2) NOT NULL DEFAULT '0.00' COMMENT '订单价格',
  `status` tinyint unsigned DEFAULT '0' COMMENT '状态:0=待审核,1=已审核,2=待发货,3=待收货,4=已收货,5=退款中,6=退款完成,7=交易取消,8=交易完成',
  `remark` varchar(255) DEFAULT NULL COMMENT '备注',
  `pay_status` tinyint unsigned DEFAULT '0' COMMENT '支付状态:0=支付中,1=支付成功,2=支付失败',
  `pay_time` int unsigned DEFAULT NULL COMMENT '付款时间',
  `diver_type` enum('1','2') DEFAULT '1' COMMENT '配送类型:1=专业配送,2=到店自取',
  `store_id` int DEFAULT NULL COMMENT '门店ID',
  `diver_id` int DEFAULT NULL COMMENT '司机ID',
  `diver_name` varchar(255) DEFAULT NULL COMMENT '司机姓名',
  `diver_phone` varchar(100) DEFAULT NULL COMMENT '司机电话',
  `createtime` int DEFAULT NULL COMMENT '创建时间',
  `updatetime` int DEFAULT NULL COMMENT '更新时间',
  `deletetime` int DEFAULT NULL COMMENT '删除时间',
  `numb` int DEFAULT '1' COMMENT '当月序号',
  `goods_images` text COMMENT '附件',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 COMMENT='订单表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_erp_user_orderinfo`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_erp_user_orderinfo` (
  `id` int NOT NULL AUTO_INCREMENT,
  `user_id` int DEFAULT NULL COMMENT '用户ID',
  `order_id` int DEFAULT NULL COMMENT '订单ID',
  `goods_id` int NOT NULL DEFAULT '0' COMMENT '商品ID',
  `goods_unit` int DEFAULT NULL COMMENT '商品单位',
  `goods_attr` int DEFAULT NULL COMMENT '商品属性',
  `goods_name` varchar(255) DEFAULT NULL COMMENT '商品名称',
  `goods_image` varchar(255) DEFAULT NULL COMMENT '商品图片',
  `unit_name` varchar(255) DEFAULT NULL COMMENT '单位名称',
  `attr_name` varchar(255) DEFAULT NULL COMMENT '属性名称',
  `number` float(11,2) NOT NULL DEFAULT '0.00' COMMENT '数量',
  `price` float(11,2) NOT NULL DEFAULT '0.00' COMMENT '单价',
  `money` float(11,2) NOT NULL DEFAULT '0.00' COMMENT '总价',
  `createtime` int DEFAULT NULL COMMENT '创建时间',
  `updatetime` int DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='订货单详情表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_erp_user_ordersale`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_erp_user_ordersale` (
  `id` int NOT NULL AUTO_INCREMENT,
  `order_id` int DEFAULT NULL COMMENT '订单ID',
  `sales_id` int NOT NULL DEFAULT '0' COMMENT '销货单ID',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='订货单关联表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_erp_user_ordersaleinfo`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_erp_user_ordersaleinfo` (
  `id` int NOT NULL AUTO_INCREMENT,
  `sales_id` int DEFAULT NULL COMMENT '销货单ID',
  `orderinfo_id` int DEFAULT NULL COMMENT '订单详情ID',
  `salesinfo_id` int NOT NULL DEFAULT '0' COMMENT '销货单详情ID',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='订货单详情关联表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_erp_user_sets`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_erp_user_sets` (
  `id` int NOT NULL AUTO_INCREMENT,
  `type` tinyint DEFAULT '1' COMMENT '类型:1=基础参数,2=功能参数',
  `name` varchar(100) DEFAULT NULL COMMENT '变量名称',
  `nickname` varchar(100) DEFAULT NULL COMMENT '名称',
  `value` varchar(255) DEFAULT NULL COMMENT '设置值',
  `is_edit` enum('0','1') DEFAULT '0' COMMENT '修改:0=允许,1=不允许',
  `notice` varchar(255) DEFAULT NULL COMMENT '提示',
  `explain` varchar(255) DEFAULT NULL COMMENT '说明',
  `is_checked` tinyint DEFAULT '0' COMMENT '是否选中',
  `set` text COMMENT '场景',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `name` (`name`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='订货通设置';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_erp_warehouse`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_erp_warehouse` (
  `id` int NOT NULL AUTO_INCREMENT,
  `admin_id` int DEFAULT NULL,
  `purchase_code` varchar(50) DEFAULT NULL COMMENT '单据编号',
  `purchase_time` int DEFAULT NULL COMMENT '单据时间',
  `business_type` enum('1','2') DEFAULT NULL COMMENT '业务类别:1=盘亏,2=其他出库',
  `number` double(15,4) DEFAULT NULL COMMENT '数量',
  `price` double(15,4) DEFAULT NULL COMMENT '金额',
  `customers_id` int DEFAULT NULL COMMENT '客户',
  `preparation_id` int DEFAULT NULL COMMENT '制单人',
  `examine_id` int DEFAULT NULL COMMENT '审核人',
  `operation_id` int DEFAULT NULL COMMENT '最后操作人',
  `examine_time` int DEFAULT NULL COMMENT '审核时间',
  `examine_status` enum('0','1') NOT NULL DEFAULT '0' COMMENT '审核状态:0=未审核,1=已审核',
  `remarks` varchar(255) DEFAULT NULL COMMENT '备注',
  `print_num` int DEFAULT NULL COMMENT '打印次数',
  `order_id` int DEFAULT NULL COMMENT '盘点单ID',
  `order_type` enum('1','2') DEFAULT NULL COMMENT '单据类型:1=盘点,2=序列号盘点',
  `createtime` int DEFAULT NULL COMMENT '创建时间',
  `updatetime` int DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='其他出库单';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_erp_warehouse_info`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_erp_warehouse_info` (
  `id` int NOT NULL AUTO_INCREMENT,
  `admin_id` int DEFAULT NULL,
  `purchase_code` varchar(50) DEFAULT NULL COMMENT '盘点编号',
  `purchase_time` int DEFAULT NULL COMMENT '盘点时间',
  `goods_id` int DEFAULT NULL COMMENT '商品',
  `goods_attr` int DEFAULT NULL COMMENT '商品属性',
  `goods_unit` int DEFAULT NULL COMMENT '商品单位',
  `stores_id` int DEFAULT NULL COMMENT '仓库',
  `stock` double(15,4) DEFAULT NULL COMMENT '可用库存',
  `number` double(15,4) DEFAULT NULL COMMENT '数量',
  `basic_nmber` double(15,4) DEFAULT NULL COMMENT '基础数量',
  `period` text COMMENT '序列号',
  `unit_price` double(15,4) DEFAULT NULL COMMENT '出库单价',
  `price` double(15,4) DEFAULT NULL COMMENT '出库金额',
  `remark` varchar(255) DEFAULT NULL COMMENT '备注',
  `batch` varchar(255) DEFAULT NULL COMMENT '批次',
  `batch_id` int DEFAULT NULL COMMENT '批次ID',
  `manufacture` int DEFAULT NULL COMMENT '生产日期',
  `guarantee` int DEFAULT NULL COMMENT '保质期',
  `validity` int DEFAULT NULL COMMENT '有效期至',
  `createtime` int DEFAULT NULL COMMENT '创建时间',
  `updatetime` int DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='其他出库单详情';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_erp_warehousing`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_erp_warehousing` (
  `id` int NOT NULL AUTO_INCREMENT,
  `admin_id` int DEFAULT NULL,
  `purchase_code` varchar(50) DEFAULT NULL COMMENT '单据编号',
  `purchase_time` int DEFAULT NULL COMMENT '单据时间',
  `business_type` enum('1','2') DEFAULT NULL COMMENT '业务类别:1=盘盈,2=其他入库',
  `number` double(15,4) DEFAULT NULL COMMENT '数量',
  `price` double(15,4) DEFAULT NULL COMMENT '金额',
  `suppliers_id` int DEFAULT NULL COMMENT '供应商',
  `preparation_id` int DEFAULT NULL COMMENT '制单人',
  `examine_id` int DEFAULT NULL COMMENT '审核人',
  `operation_id` int DEFAULT NULL COMMENT '最后操作人',
  `examine_time` int DEFAULT NULL COMMENT '审核时间',
  `examine_status` enum('0','1') NOT NULL DEFAULT '0' COMMENT '审核状态:0=未审核,1=已审核',
  `remarks` varchar(255) DEFAULT NULL COMMENT '备注',
  `print_num` int DEFAULT NULL COMMENT '打印次数',
  `order_id` int DEFAULT NULL COMMENT '盘点单ID',
  `order_type` enum('1','2') DEFAULT NULL COMMENT '单据类型:1=盘点,2=序列号盘点',
  `createtime` int DEFAULT NULL COMMENT '创建时间',
  `updatetime` int DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='其他入库单';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_erp_warehousing_info`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_erp_warehousing_info` (
  `id` int NOT NULL AUTO_INCREMENT,
  `admin_id` int DEFAULT NULL,
  `purchase_code` varchar(50) DEFAULT NULL COMMENT '盘点编号',
  `purchase_time` int DEFAULT NULL COMMENT '盘点时间',
  `goods_id` int DEFAULT NULL COMMENT '商品',
  `goods_attr` int DEFAULT NULL COMMENT '商品属性',
  `goods_unit` int DEFAULT NULL COMMENT '商品单位',
  `stores_id` int DEFAULT NULL COMMENT '仓库',
  `stock` double(15,4) DEFAULT NULL COMMENT '可用库存',
  `number` double(15,4) DEFAULT NULL COMMENT '数量',
  `basic_nmber` double(15,4) DEFAULT NULL COMMENT '基础数量',
  `period` text COMMENT '序列号',
  `unit_price` double(15,4) DEFAULT NULL COMMENT '入库单价',
  `price` double(15,4) DEFAULT NULL COMMENT '入库金额',
  `remark` varchar(255) DEFAULT NULL COMMENT '备注',
  `batch` varchar(255) DEFAULT NULL COMMENT '批次',
  `batch_id` int DEFAULT NULL COMMENT '批次ID',
  `manufacture` int DEFAULT NULL COMMENT '生产日期',
  `guarantee` int DEFAULT NULL COMMENT '保质期',
  `validity` int DEFAULT NULL COMMENT '有效期至',
  `createtime` int DEFAULT NULL COMMENT '创建时间',
  `updatetime` int DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='其他入库单详情';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_erp_writeoff`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_erp_writeoff` (
  `id` int NOT NULL AUTO_INCREMENT,
  `admin_id` int DEFAULT NULL,
  `purchase_code` varchar(50) DEFAULT NULL COMMENT '单据编号',
  `purchase_time` int DEFAULT NULL COMMENT '单据时间',
  `business_type` enum('1','2','3','4','5') NOT NULL DEFAULT '1' COMMENT '类型:1=预收冲应收,2=预付冲应付,3=应收冲应付,4=应收转应收,5=应付转应付',
  `customers_id` int DEFAULT NULL COMMENT '客户',
  `customers_out_id` int DEFAULT NULL COMMENT '转出客户',
  `customers_enter_id` int DEFAULT NULL COMMENT '转入客户',
  `suppliers_id` int DEFAULT NULL COMMENT '供应商',
  `suppliers_out_id` int DEFAULT NULL COMMENT '转出供应商',
  `suppliers_enter_id` int DEFAULT NULL COMMENT '转入供应商',
  `write_off` double(15,4) DEFAULT NULL COMMENT '核销金额',
  `remarks` varchar(255) DEFAULT NULL COMMENT '备注',
  `preparation_id` int DEFAULT NULL COMMENT '制单人',
  `examine_id` int DEFAULT NULL COMMENT '审核人',
  `operation_id` int DEFAULT NULL COMMENT '最后操作人',
  `examine_time` int DEFAULT NULL COMMENT '审核时间',
  `examine_status` enum('0','1') NOT NULL DEFAULT '0' COMMENT '审核状态:0=未审核,1=已审核',
  `print_num` int DEFAULT NULL COMMENT '打印次数',
  `createtime` int DEFAULT NULL COMMENT '创建时间',
  `updatetime` int DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='核销单';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_erp_writeoff_info`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_erp_writeoff_info` (
  `id` int NOT NULL AUTO_INCREMENT,
  `admin_id` int DEFAULT NULL,
  `collection_id` int NOT NULL DEFAULT '0' COMMENT '核销单ID',
  `purchase_code` varchar(50) NOT NULL DEFAULT '' COMMENT '单据编号',
  `purchase_type` enum('1','2','3','4','5','6') DEFAULT NULL COMMENT '单据类型:1=预收单,2=预付单,3=应收,4=应付,5=期初应收/付款,6=期初预收/付款',
  `purchase_time` int NOT NULL DEFAULT '0' COMMENT '单据时间',
  `type` enum('1','2','3','4','5','6') DEFAULT '1' COMMENT '类型:1=购货/销货,2=退货,3=期初,4=核销',
  `purchase_number` varchar(50) NOT NULL DEFAULT '' COMMENT '原单编号',
  `business_type` tinyint NOT NULL DEFAULT '0' COMMENT '类型:1=预收冲应收,2=预付冲应付,3=应收冲应付,4=应收转应收,5=应付转应付',
  `times` int NOT NULL DEFAULT '0' COMMENT '单据日期',
  `money` double(15,4) NOT NULL DEFAULT '0.0000' COMMENT '单据金额',
  `write_off` double(15,4) DEFAULT NULL COMMENT '已核销金额',
  `write_off_money` double(15,4) DEFAULT NULL COMMENT '未核销金额',
  `write_off_price` double(15,4) DEFAULT '0.0000' COMMENT '本次核销金额',
  `old_money` double(15,4) DEFAULT NULL,
  `old_status` int DEFAULT NULL,
  `top` enum('1','2') DEFAULT '1' COMMENT '位置:1=上,2=下',
  `source_id` int DEFAULT NULL COMMENT '源单ID',
  `business_name` varchar(255) DEFAULT NULL COMMENT '单据名称',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='核销单据信息';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_erp_writeoff_log`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_erp_writeoff_log` (
  `id` int NOT NULL AUTO_INCREMENT,
  `admin_id` int DEFAULT NULL,
  `source_id` int DEFAULT NULL COMMENT '源单ID',
  `write_off` double(15,4) DEFAULT NULL COMMENT '未核销金额',
  `money` double(15,4) DEFAULT NULL COMMENT '已核销金额',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='核销单据信息';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_erp_writeoff_source`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_erp_writeoff_source` (
  `id` int NOT NULL AUTO_INCREMENT,
  `admin_id` int DEFAULT NULL,
  `purchase_type` enum('1','2','3','4','5','6') DEFAULT NULL COMMENT '单据类型:1=预收单,2=预付单,3=应收,4=应付,5=期初应收/付款,6=期初预收/付款',
  `type` enum('1','2','3','4') DEFAULT NULL COMMENT '类型:1=购货/销货,2=退货,3=期初,4=核销',
  `suppliers_code` varchar(50) DEFAULT NULL COMMENT '供应商编号',
  `customers_id` int DEFAULT NULL COMMENT '客户Id',
  `purchase_code` varchar(50) NOT NULL DEFAULT '' COMMENT '原单编号',
  `purchase_time` int DEFAULT NULL COMMENT '单据时间',
  `price` double(15,4) DEFAULT '0.0000' COMMENT '单据金额',
  `money` double(15,4) DEFAULT '0.0000' COMMENT '已核销金额',
  `write_off` double(15,4) DEFAULT '0.0000' COMMENT '未核销金额',
  `remark` varchar(255) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='核销单源单';
/*!40101 SET character_set_client = @saved_cs_client */;

--

-- ---------- plugins/erp/migrations/V1__erp_schema.sql ----------
-- =====================================================================
-- ERP 演示数据初始化脚本
-- 目标库: meoc (jdbc:mysql://127.0.0.1:3306/meoc)
-- 说明:
--   1) 仅重建/新建 ERP 业务表(均为空表), 不与其它服务冲突
--   2) hou_erp_accounts_receivable 被 finance 服务共享, 保持原结构只灌数据
--   3) 业务数据统一 admin_id=1 (默认组织), createtime 等使用可读日期时间
-- 执行: mysql -h127.0.0.1 -umeoc -p'***' meoc < erp_demo.sql
-- =====================================================================
SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- =====================================================================
-- 一、商品管理
-- =====================================================================

CREATE TABLE IF NOT EXISTS `hou_erp_goods_type` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `admin_id` bigint DEFAULT 1,
  `supplier_type_name` varchar(120) NOT NULL DEFAULT '' COMMENT '类别名称',
  `supplier_type_image` varchar(255) DEFAULT '' COMMENT '图片',
  `pid` bigint NOT NULL DEFAULT 0 COMMENT '上级分类ID',
  `path` varchar(255) DEFAULT NULL COMMENT '路径',
  `status` varchar(1) NOT NULL DEFAULT '1' COMMENT '状态:0=禁用,1=正常',
  `weigh` int DEFAULT 0 COMMENT '排序权重',
  `order_status` varchar(1) DEFAULT '1' COMMENT '订货通显示状态:0=否,1=是',
  `createtime` datetime DEFAULT NULL COMMENT '创建时间',
  `updatetime` datetime DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='商品分类';


CREATE TABLE IF NOT EXISTS `hou_erp_goods` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `admin_id` bigint DEFAULT 1,
  `goods_code` varchar(50) NOT NULL DEFAULT '' COMMENT '商品编号',
  `goods_name` varchar(255) NOT NULL DEFAULT '' COMMENT '商品名称',
  `goods_barcode` varchar(50) DEFAULT '' COMMENT '商品条码',
  `goods_specification_id` varchar(255) DEFAULT '' COMMENT '规格型号',
  `goods_type_id` bigint DEFAULT NULL COMMENT '商品类别ID',
  `manager_warehouse_id` bigint DEFAULT NULL COMMENT '首选仓库ID',
  `unit_id` bigint DEFAULT NULL COMMENT '计量单位ID',
  `more_unit` varchar(1) DEFAULT '2' COMMENT '多计量单位:1=是,2=否',
  `more_unit_id` bigint DEFAULT NULL COMMENT '多单位ID',
  `market_sell_price` decimal(15,2) DEFAULT 0 COMMENT '零售价',
  `wholesale_price` decimal(15,2) DEFAULT 0 COMMENT '批发价',
  `vip_price` decimal(15,2) DEFAULT 0 COMMENT 'VIP会员价',
  `discount` decimal(8,2) DEFAULT 100 COMMENT '折扣率1',
  `discount2` decimal(8,2) DEFAULT 100 COMMENT '折扣率2',
  `purchase` decimal(15,2) DEFAULT 0 COMMENT '预计采购价',
  `goods_image` varchar(500) DEFAULT '' COMMENT '商品主图',
  `goods_images` text COMMENT '商品图集',
  `content` text COMMENT '商品详情',
  `inventory` decimal(15,2) DEFAULT 0 COMMENT '库存',
  `more_attribute` varchar(1) DEFAULT '0' COMMENT '多属性:0=否,1=是',
  `attribute_id` varchar(255) DEFAULT '' COMMENT '商品属性条码',
  `note` varchar(255) DEFAULT '' COMMENT '备注',
  `is_offten` varchar(1) DEFAULT '0' COMMENT '是否常用:0=否,1=是',
  `stock_warning` varchar(1) DEFAULT '0' COMMENT '库存预警:0=否,1=是',
  `min_stock` decimal(15,2) DEFAULT 0 COMMENT '最低库存',
  `max_stock` decimal(15,2) DEFAULT 0 COMMENT '最高库存',
  `branch` varchar(1) DEFAULT '0' COMMENT '分仓预警:0=否,1=是',
  `warehousing` bigint DEFAULT NULL COMMENT '首选入库单位',
  `warehouse` bigint DEFAULT NULL COMMENT '首选出库单位',
  `period` decimal(15,2) DEFAULT 0 COMMENT '期初数量',
  `unit_cost` decimal(15,2) DEFAULT 0 COMMENT '单位成本',
  `price` decimal(15,2) DEFAULT 0 COMMENT '期初总价',
  `period_status` varchar(1) DEFAULT '0' COMMENT '期初设置:0=否,1=是',
  `status` varchar(1) DEFAULT '1' COMMENT '状态:0=下架,1=上架',
  `is_edit` varchar(1) DEFAULT '1' COMMENT '允许修改:0=否,1=是',
  `auxiliary` int DEFAULT NULL COMMENT '辅助属性',
  `auxiliary_type` varchar(255) DEFAULT NULL COMMENT '辅助属性类型',
  `auxiliary_type_c` varchar(255) DEFAULT NULL COMMENT '辅助属性类型2',
  `batch` int DEFAULT 0 COMMENT '批次管理:0=否,1=是',
  `shelf_life` int DEFAULT 0 COMMENT '保质期天数',
  `early_warning` int DEFAULT 0 COMMENT '预警天数',
  `serial` int DEFAULT 0 COMMENT '序列号管理:0=否,1=是',
  `tag` varchar(255) DEFAULT '' COMMENT '标签',
  `sale_number` decimal(15,2) DEFAULT 0 COMMENT '销量',
  `order_status` varchar(1) DEFAULT '1' COMMENT '订货通显示状态',
  `promotion` varchar(1) DEFAULT '0' COMMENT '是否促销',
  `createtime` datetime DEFAULT NULL,
  `updatetime` datetime DEFAULT NULL,
  `deletetime` datetime DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_goods_code` (`goods_code`),
  KEY `idx_goods_type` (`goods_type_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='商品表';


CREATE TABLE IF NOT EXISTS `hou_erp_goods_attr` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `admin_id` bigint DEFAULT 1,
  `goods_code` varchar(50) NOT NULL DEFAULT '' COMMENT '商品编号',
  `goods_name` varchar(255) NOT NULL DEFAULT '' COMMENT '商品名称',
  `goods_barcode` varchar(50) DEFAULT '' COMMENT '商品条码',
  `goods_id` bigint NOT NULL DEFAULT 0 COMMENT '商品ID',
  `attribute_id` bigint DEFAULT NULL COMMENT '属性组合ID',
  `attr_code` varchar(50) DEFAULT NULL COMMENT '属性编号',
  `barcode` varchar(50) DEFAULT NULL COMMENT '属性条码',
  `unit` bigint NOT NULL DEFAULT 0 COMMENT '计量单位',
  `market_sell_price` decimal(15,2) DEFAULT NULL COMMENT '零售价',
  `wholesale_price` decimal(15,2) DEFAULT NULL COMMENT '批发价',
  `vip_price` decimal(15,2) DEFAULT NULL COMMENT 'VIP会员价',
  `discount` decimal(8,2) DEFAULT NULL COMMENT '折扣率1',
  `discount2` decimal(8,2) DEFAULT NULL COMMENT '折扣率2',
  `purchase` decimal(15,2) DEFAULT NULL COMMENT '预计采购价',
  `createtime` datetime DEFAULT NULL,
  `updatetime` datetime DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_goods_id` (`goods_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='商品属性价格表';


-- =====================================================================
-- 二、基础资料: 单位/客户/供应商/仓库
-- =====================================================================

CREATE TABLE IF NOT EXISTS `hou_erp_customers` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `admin_id` bigint DEFAULT 1,
  `customer_code` varchar(50) DEFAULT NULL COMMENT '客户编号',
  `customer_name` varchar(150) NOT NULL DEFAULT '' COMMENT '客户名称',
  `customer_level_id` bigint DEFAULT 1 COMMENT '客户等级',
  `contact_name` varchar(50) NOT NULL DEFAULT '' COMMENT '联系人',
  `contact_phone` varchar(20) NOT NULL DEFAULT '' COMMENT '联系电话',
  `contact_mobile` varchar(11) NOT NULL DEFAULT '' COMMENT '手机号码',
  `email` varchar(100) DEFAULT '' COMMENT '邮箱',
  `fax` varchar(30) DEFAULT '' COMMENT '传真',
  `address` varchar(255) NOT NULL DEFAULT '' COMMENT '地址',
  `post_code` varchar(20) DEFAULT '' COMMENT '邮政编码',
  `bank` varchar(100) DEFAULT '' COMMENT '开户银行',
  `bank_account` varchar(100) DEFAULT '' COMMENT '银行账号',
  `tax_number` varchar(50) DEFAULT '' COMMENT '税号',
  `start_receivable` decimal(15,2) DEFAULT 0 COMMENT '期初应收款',
  `current_receivable` decimal(15,2) DEFAULT 0 COMMENT '当前应收款',
  `credit_limit` decimal(15,2) DEFAULT 0 COMMENT '信用额度',
  `staff_member_id` bigint DEFAULT NULL COMMENT '销售人员ID',
  `status` int DEFAULT 1 COMMENT '状态:0=停用,1=启用',
  `remark` varchar(255) DEFAULT '' COMMENT '备注',
  `createtime` datetime DEFAULT NULL,
  `updatetime` datetime DEFAULT NULL,
  `deletetime` datetime DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_customer_code` (`customer_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='客户表';


CREATE TABLE IF NOT EXISTS `hou_erp_suppliers` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `admin_id` bigint DEFAULT 1,
  `supplier_code` varchar(50) NOT NULL DEFAULT '' COMMENT '供应商编号',
  `supplier_name` varchar(120) NOT NULL COMMENT '供应商名称',
  `contact_name` varchar(50) NOT NULL DEFAULT '' COMMENT '联系人',
  `contact_phone` varchar(20) DEFAULT '' COMMENT '联系电话',
  `contact_mobile` varchar(11) NOT NULL COMMENT '手机号码',
  `email` varchar(120) DEFAULT '' COMMENT '邮箱',
  `fax` varchar(30) DEFAULT '' COMMENT '传真',
  `address` varchar(255) DEFAULT '' COMMENT '地址',
  `post_code` varchar(20) DEFAULT '' COMMENT '邮编',
  `bank` varchar(100) DEFAULT '' COMMENT '开户银行',
  `bank_account` varchar(100) DEFAULT '' COMMENT '银行账号',
  `tax_number` varchar(50) DEFAULT '' COMMENT '税号',
  `start_payable` decimal(15,2) DEFAULT 0 COMMENT '期初应付款',
  `current_payable` decimal(15,2) DEFAULT 0 COMMENT '当前应付款',
  `credit_limit` decimal(15,2) DEFAULT 0 COMMENT '信用额度',
  `status` int DEFAULT 1 COMMENT '状态:0=停用,1=启用',
  `remark` varchar(255) DEFAULT '' COMMENT '备注',
  `createtime` datetime DEFAULT NULL,
  `updatetime` datetime DEFAULT NULL,
  `deletetime` datetime DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_supplier_code` (`supplier_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='供应商表';


CREATE TABLE IF NOT EXISTS `hou_erp_stores` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `admin_id` bigint DEFAULT 1,
  `store_code` varchar(50) NOT NULL DEFAULT '' COMMENT '仓库编号',
  `store_name` varchar(255) NOT NULL DEFAULT '' COMMENT '仓库名称',
  `store_type` int DEFAULT 1 COMMENT '仓库类型:1=原料仓,2=成品仓,3=半成品仓,4=退货仓',
  `address` varchar(255) DEFAULT '' COMMENT '仓库地址',
  `manager` varchar(50) DEFAULT '' COMMENT '仓库管理员',
  `contact_phone` varchar(20) DEFAULT '' COMMENT '联系电话',
  `area` decimal(15,2) DEFAULT 0 COMMENT '仓库面积(㎡)',
  `capacity` decimal(15,2) DEFAULT 0 COMMENT '容量',
  `is_default` int DEFAULT 0 COMMENT '是否默认:0=否,1=是',
  `status` int DEFAULT 1 COMMENT '状态:0=停用,1=启用',
  `remark` varchar(255) DEFAULT '' COMMENT '备注',
  `createtime` datetime DEFAULT NULL,
  `updatetime` datetime DEFAULT NULL,
  `deletetime` datetime DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_store_code` (`store_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='仓库表';


CREATE TABLE IF NOT EXISTS `hou_erp_unit` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `admin_id` bigint DEFAULT 1,
  `name` varchar(100) DEFAULT NULL COMMENT '单位名称',
  `type` varchar(1) DEFAULT '1' COMMENT '类型:1=单一单位,2=多单位',
  `is_main` varchar(1) DEFAULT '0' COMMENT '是否主单位',
  `createtime` datetime DEFAULT NULL,
  `updatetime` datetime DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='计量单位';


-- =====================================================================
-- 三、销售管理: 销售单/明细/机会/发货
-- =====================================================================

CREATE TABLE IF NOT EXISTS `hou_erp_sale` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `admin_id` bigint DEFAULT 1,
  `sale_code` varchar(50) NOT NULL DEFAULT '' COMMENT '销售单编号',
  `sale_time` datetime DEFAULT NULL COMMENT '销售日期',
  `customers_id` bigint DEFAULT NULL COMMENT '客户ID',
  `customers_name` varchar(150) DEFAULT '' COMMENT '客户名称(冗余)',
  `money` decimal(15,2) DEFAULT 0 COMMENT '销售金额',
  `real_money` decimal(15,2) DEFAULT 0 COMMENT '实际收款金额',
  `amount` decimal(15,2) DEFAULT 0 COMMENT '优惠金额',
  `staff_member_id` bigint DEFAULT NULL COMMENT '销售人员ID',
  `preparation_id` bigint DEFAULT NULL COMMENT '制单人',
  `examine_id` bigint DEFAULT NULL COMMENT '审核人',
  `operation_id` bigint DEFAULT NULL COMMENT '最后操作人',
  `examine_status` int DEFAULT 0 COMMENT '审核状态:0=未审核,1=已审核',
  `examine_time` datetime DEFAULT NULL COMMENT '审核时间',
  `pay_status` int DEFAULT 0 COMMENT '收款状态:0=未收款,1=全部收款,2=部分收款',
  `remark` varchar(255) DEFAULT '' COMMENT '备注',
  `print_num` int DEFAULT 0 COMMENT '打印次数',
  `createtime` datetime DEFAULT NULL,
  `updatetime` datetime DEFAULT NULL,
  `deletetime` datetime DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_sale_code` (`sale_code`),
  KEY `idx_customers_id` (`customers_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='销售单';


CREATE TABLE IF NOT EXISTS `hou_erp_sale_info` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `admin_id` bigint DEFAULT 1,
  `sale_id` bigint NOT NULL DEFAULT 0 COMMENT '销售单ID',
  `sale_code` varchar(50) NOT NULL DEFAULT '' COMMENT '销售单编号',
  `goods_id` bigint NOT NULL DEFAULT 0 COMMENT '商品ID',
  `goods_code` varchar(50) NOT NULL DEFAULT '' COMMENT '商品编号',
  `goods_name` varchar(255) NOT NULL DEFAULT '' COMMENT '商品名称',
  `number` decimal(15,2) NOT NULL DEFAULT 0 COMMENT '数量',
  `price` decimal(15,2) NOT NULL DEFAULT 0 COMMENT '单价',
  `money` decimal(15,2) NOT NULL DEFAULT 0 COMMENT '金额',
  `stores_id` bigint DEFAULT NULL COMMENT '发货仓库',
  `remark` varchar(255) DEFAULT '' COMMENT '备注',
  `createtime` datetime DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_sale_id` (`sale_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='销售单明细';


CREATE TABLE IF NOT EXISTS `hou_erp_opportunity` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `admin_id` bigint DEFAULT 1,
  `opportunity_code` varchar(50) NOT NULL DEFAULT '' COMMENT '机会编号',
  `opportunity_name` varchar(255) NOT NULL DEFAULT '' COMMENT '机会名称',
  `customer_id` bigint DEFAULT NULL COMMENT '客户ID',
  `customer_name` varchar(150) DEFAULT '' COMMENT '客户名称',
  `amount` decimal(15,2) DEFAULT 0 COMMENT '预计金额',
  `probability` int DEFAULT 50 COMMENT '赢单概率(%)',
  `stage` varchar(20) DEFAULT '跟进中' COMMENT '阶段:初步接触/需求确认/方案报价/商务谈判/赢单/输单',
  `owner` varchar(50) DEFAULT '' COMMENT '负责人',
  `expected_date` date DEFAULT NULL COMMENT '预计成交日期',
  `next_contact_time` datetime DEFAULT NULL COMMENT '下次跟进时间',
  `note` varchar(500) DEFAULT '' COMMENT '备注',
  `status` int DEFAULT 0 COMMENT '状态:0=进行中,1=已赢单,2=已输单',
  `createtime` datetime DEFAULT NULL,
  `updatetime` datetime DEFAULT NULL,
  `deletetime` datetime DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_opp_code` (`opportunity_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='销售机会';


CREATE TABLE IF NOT EXISTS `hou_erp_delivery` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `admin_id` bigint DEFAULT 1,
  `delivery_code` varchar(50) NOT NULL DEFAULT '' COMMENT '发货单编号',
  `sale_id` bigint DEFAULT NULL COMMENT '销售单ID',
  `sale_code` varchar(50) DEFAULT '' COMMENT '销售单编号',
  `customer_id` bigint DEFAULT NULL COMMENT '客户ID',
  `customer_name` varchar(150) DEFAULT '' COMMENT '客户名称',
  `delivery_time` datetime DEFAULT NULL COMMENT '发货日期',
  `express_company` varchar(50) DEFAULT '' COMMENT '物流公司',
  `express_no` varchar(50) DEFAULT '' COMMENT '运单号',
  `goods_num` decimal(15,2) DEFAULT 0 COMMENT '商品总数量',
  `money` decimal(15,2) DEFAULT 0 COMMENT '发货金额',
  `receiver_name` varchar(50) DEFAULT '' COMMENT '收货人',
  `receiver_phone` varchar(20) DEFAULT '' COMMENT '收货电话',
  `receiver_address` varchar(255) DEFAULT '' COMMENT '收货地址',
  `status` int DEFAULT 0 COMMENT '状态:0=待发货,1=已发货,2=已签收,3=已退回',
  `note` varchar(255) DEFAULT '' COMMENT '备注',
  `createtime` datetime DEFAULT NULL,
  `updatetime` datetime DEFAULT NULL,
  `deletetime` datetime DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_delivery_code` (`delivery_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='销售发货单';


-- =====================================================================
-- 四、采购管理: 采购单/明细/计划
-- =====================================================================

CREATE TABLE IF NOT EXISTS `hou_erp_purchase` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `admin_id` bigint DEFAULT 1,
  `purchase_code` varchar(50) NOT NULL DEFAULT '' COMMENT '采购单编号',
  `purchase_time` datetime DEFAULT NULL COMMENT '采购日期',
  `suppliers_id` bigint DEFAULT NULL COMMENT '供应商ID',
  `suppliers_name` varchar(120) DEFAULT '' COMMENT '供应商名称(冗余)',
  `money` decimal(15,2) DEFAULT 0 COMMENT '采购金额',
  `real_money` decimal(15,2) DEFAULT 0 COMMENT '实际付款金额',
  `amount` decimal(15,2) DEFAULT 0 COMMENT '优惠金额',
  `preparation_id` bigint DEFAULT NULL COMMENT '制单人',
  `examine_id` bigint DEFAULT NULL COMMENT '审核人',
  `operation_id` bigint DEFAULT NULL COMMENT '最后操作人',
  `examine_status` int DEFAULT 0 COMMENT '审核状态:0=未审核,1=已审核',
  `examine_time` datetime DEFAULT NULL COMMENT '审核时间',
  `pay_status` int DEFAULT 0 COMMENT '付款状态:0=未付款,1=全部付款,2=部分付款',
  `remark` varchar(255) DEFAULT '' COMMENT '备注',
  `print_num` int DEFAULT 0 COMMENT '打印次数',
  `createtime` datetime DEFAULT NULL,
  `updatetime` datetime DEFAULT NULL,
  `deletetime` datetime DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_purchase_code` (`purchase_code`),
  KEY `idx_suppliers_id` (`suppliers_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='采购单';


CREATE TABLE IF NOT EXISTS `hou_erp_purchase_info` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `admin_id` bigint DEFAULT 1,
  `purchase_id` bigint NOT NULL DEFAULT 0 COMMENT '采购单ID',
  `purchase_code` varchar(50) NOT NULL DEFAULT '' COMMENT '采购单编号',
  `goods_id` bigint NOT NULL DEFAULT 0 COMMENT '商品ID',
  `goods_code` varchar(50) NOT NULL DEFAULT '' COMMENT '商品编号',
  `goods_name` varchar(255) NOT NULL DEFAULT '' COMMENT '商品名称',
  `number` decimal(15,2) NOT NULL DEFAULT 0 COMMENT '数量',
  `price` decimal(15,2) NOT NULL DEFAULT 0 COMMENT '单价',
  `money` decimal(15,2) NOT NULL DEFAULT 0 COMMENT '金额',
  `stores_id` bigint DEFAULT NULL COMMENT '入库仓库',
  `remark` varchar(255) DEFAULT '' COMMENT '备注',
  `createtime` datetime DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_purchase_id` (`purchase_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='采购单明细';


CREATE TABLE IF NOT EXISTS `hou_erp_purchase_plan` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `admin_id` bigint DEFAULT 1,
  `plan_code` varchar(50) NOT NULL DEFAULT '' COMMENT '计划编号',
  `plan_time` datetime DEFAULT NULL COMMENT '计划日期',
  `supplier_id` bigint DEFAULT NULL COMMENT '供应商ID',
  `supplier_name` varchar(120) DEFAULT '' COMMENT '供应商名称',
  `goods_num` decimal(15,2) DEFAULT 0 COMMENT '商品总数量',
  `estimate_money` decimal(15,2) DEFAULT 0 COMMENT '预计金额',
  `status` int DEFAULT 0 COMMENT '状态:0=草稿,1=已审核,2=已转采购',
  `note` varchar(255) DEFAULT '' COMMENT '备注',
  `createtime` datetime DEFAULT NULL,
  `updatetime` datetime DEFAULT NULL,
  `deletetime` datetime DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_plan_code` (`plan_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='采购计划';


-- =====================================================================
-- 五、库存管理: 库存/入库/出库/调拨/盘点
-- =====================================================================

CREATE TABLE IF NOT EXISTS `hou_erp_stock` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `admin_id` bigint DEFAULT 1,
  `goods_id` bigint NOT NULL DEFAULT 0 COMMENT '商品ID',
  `stores_id` bigint NOT NULL DEFAULT 0 COMMENT '仓库ID',
  `current_stock` decimal(15,2) DEFAULT 0 COMMENT '当前库存数量',
  `available_stock` decimal(15,2) DEFAULT 0 COMMENT '可用库存数量',
  `reserved_stock` decimal(15,2) DEFAULT 0 COMMENT '预占库存数量',
  `safe_stock` decimal(15,2) DEFAULT 0 COMMENT '安全库存数量',
  `min_stock` decimal(15,2) DEFAULT 0 COMMENT '最小库存数量',
  `max_stock` decimal(15,2) DEFAULT 0 COMMENT '最大库存数量',
  `avg_cost` decimal(15,2) DEFAULT 0 COMMENT '平均成本',
  `latest_cost` decimal(15,2) DEFAULT 0 COMMENT '最新成本',
  `goods_unit` varchar(20) DEFAULT '' COMMENT '商品单位',
  `goods_attr` varchar(50) DEFAULT '' COMMENT '商品属性',
  `batch_number` varchar(50) DEFAULT '' COMMENT '批次号',
  `production_date` datetime DEFAULT NULL COMMENT '生产日期',
  `expiry_date` datetime DEFAULT NULL COMMENT '有效期至',
  `status` int DEFAULT 1 COMMENT '状态:0=停用,1=正常',
  `remark` varchar(255) DEFAULT '' COMMENT '备注',
  `createtime` datetime DEFAULT NULL,
  `updatetime` datetime DEFAULT NULL,
  `deletetime` datetime DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_goods_id` (`goods_id`),
  KEY `idx_stores_id` (`stores_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='库存表';


CREATE TABLE IF NOT EXISTS `hou_erp_inbound` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `admin_id` bigint DEFAULT 1,
  `inbound_code` varchar(50) NOT NULL DEFAULT '' COMMENT '入库单编号',
  `inbound_time` datetime DEFAULT NULL COMMENT '入库日期',
  `inbound_type` int DEFAULT 1 COMMENT '入库类型:1=采购入库,2=生产入库,3=退货入库,4=其他',
  `supplier_id` bigint DEFAULT NULL COMMENT '供应商ID',
  `supplier_name` varchar(120) DEFAULT '' COMMENT '供应商名称',
  `stores_id` bigint DEFAULT NULL COMMENT '入库仓库',
  `goods_num` decimal(15,2) DEFAULT 0 COMMENT '商品总数量',
  `money` decimal(15,2) DEFAULT 0 COMMENT '入库金额',
  `preparation_id` bigint DEFAULT NULL COMMENT '制单人',
  `examine_status` int DEFAULT 1 COMMENT '审核状态:0=未审核,1=已审核',
  `examine_time` datetime DEFAULT NULL COMMENT '审核时间',
  `note` varchar(255) DEFAULT '' COMMENT '备注',
  `createtime` datetime DEFAULT NULL,
  `updatetime` datetime DEFAULT NULL,
  `deletetime` datetime DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_inbound_code` (`inbound_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='入库单';


CREATE TABLE IF NOT EXISTS `hou_erp_inbound_info` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `admin_id` bigint DEFAULT 1,
  `inbound_id` bigint NOT NULL DEFAULT 0 COMMENT '入库单ID',
  `inbound_code` varchar(50) NOT NULL DEFAULT '' COMMENT '入库单编号',
  `goods_id` bigint NOT NULL DEFAULT 0 COMMENT '商品ID',
  `goods_code` varchar(50) NOT NULL DEFAULT '' COMMENT '商品编号',
  `goods_name` varchar(255) NOT NULL DEFAULT '' COMMENT '商品名称',
  `number` decimal(15,2) NOT NULL DEFAULT 0 COMMENT '数量',
  `price` decimal(15,2) NOT NULL DEFAULT 0 COMMENT '单价',
  `money` decimal(15,2) NOT NULL DEFAULT 0 COMMENT '金额',
  `stores_id` bigint DEFAULT NULL COMMENT '入库仓库',
  `createtime` datetime DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_inbound_id` (`inbound_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='入库单明细';

CREATE TABLE IF NOT EXISTS `hou_erp_outbound` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `admin_id` bigint DEFAULT 1,
  `outbound_code` varchar(50) NOT NULL DEFAULT '' COMMENT '出库单编号',
  `outbound_time` datetime DEFAULT NULL COMMENT '出库日期',
  `outbound_type` int DEFAULT 1 COMMENT '出库类型:1=销售出库,2=生产领料,3=报废,4=其他',
  `customer_id` bigint DEFAULT NULL COMMENT '客户ID',
  `customer_name` varchar(150) DEFAULT '' COMMENT '客户名称',
  `stores_id` bigint DEFAULT NULL COMMENT '出库仓库',
  `goods_num` decimal(15,2) DEFAULT 0 COMMENT '商品总数量',
  `money` decimal(15,2) DEFAULT 0 COMMENT '出库金额',
  `preparation_id` bigint DEFAULT NULL COMMENT '制单人',
  `examine_status` int DEFAULT 1 COMMENT '审核状态',
  `examine_time` datetime DEFAULT NULL COMMENT '审核时间',
  `note` varchar(255) DEFAULT '' COMMENT '备注',
  `createtime` datetime DEFAULT NULL,
  `updatetime` datetime DEFAULT NULL,
  `deletetime` datetime DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_outbound_code` (`outbound_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='出库单';


CREATE TABLE IF NOT EXISTS `hou_erp_outbound_info` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `admin_id` bigint DEFAULT 1,
  `outbound_id` bigint NOT NULL DEFAULT 0 COMMENT '出库单ID',
  `outbound_code` varchar(50) NOT NULL DEFAULT '' COMMENT '出库单编号',
  `goods_id` bigint NOT NULL DEFAULT 0 COMMENT '商品ID',
  `goods_code` varchar(50) NOT NULL DEFAULT '' COMMENT '商品编号',
  `goods_name` varchar(255) NOT NULL DEFAULT '' COMMENT '商品名称',
  `number` decimal(15,2) NOT NULL DEFAULT 0 COMMENT '数量',
  `price` decimal(15,2) NOT NULL DEFAULT 0 COMMENT '单价',
  `money` decimal(15,2) NOT NULL DEFAULT 0 COMMENT '金额',
  `stores_id` bigint DEFAULT NULL COMMENT '出库仓库',
  `createtime` datetime DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_outbound_id` (`outbound_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='出库单明细';

CREATE TABLE IF NOT EXISTS `hou_erp_allocation` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `admin_id` bigint DEFAULT 1,
  `purchase_code` varchar(50) NOT NULL DEFAULT '' COMMENT '调拨单编号',
  `purchase_time` datetime DEFAULT NULL COMMENT '单据日期',
  `preparation_id` bigint DEFAULT NULL COMMENT '制单人',
  `examine_id` bigint DEFAULT NULL COMMENT '审核人',
  `operation_id` bigint DEFAULT NULL COMMENT '最后操作人',
  `examine_status` int DEFAULT 0 COMMENT '审核状态:0=未审核,1=已审核',
  `examine_time` datetime DEFAULT NULL COMMENT '审核时间',
  `remarks` varchar(255) DEFAULT '' COMMENT '备注',
  `print_num` int DEFAULT 0 COMMENT '打印次数',
  `createtime` datetime DEFAULT NULL,
  `updatetime` datetime DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='调拨单';


-- =====================================================================
-- 六、财务管理: 账户/收支/应收应付/预算/资金/成本
-- =====================================================================

CREATE TABLE IF NOT EXISTS `hou_erp_accounts` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `admin_id` bigint DEFAULT 1,
  `account_coding` varchar(100) NOT NULL COMMENT '账户编号',
  `account_name` varchar(100) NOT NULL COMMENT '账户名称',
  `balance` decimal(15,2) DEFAULT 0 COMMENT '当前余额',
  `start_account_balance` decimal(15,2) NOT NULL DEFAULT 0 COMMENT '期初余额',
  `start_account_time` varchar(20) NOT NULL DEFAULT '' COMMENT '建账日期',
  `account_type_id` int NOT NULL DEFAULT 1 COMMENT '账户类型:1=现金,2=银行存款',
  `is_default` int DEFAULT 0 COMMENT '默认:0=否,1=是',
  `status` int DEFAULT 1 COMMENT '状态:0=关闭,1=正常',
  `createtime` datetime DEFAULT NULL,
  `updatetime` datetime DEFAULT NULL,
  `deletetime` datetime DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_account_coding` (`account_coding`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='账户表';


CREATE TABLE IF NOT EXISTS `hou_erp_income` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `admin_id` bigint DEFAULT 1,
  `income_code` varchar(50) NOT NULL DEFAULT '' COMMENT '收入单编号',
  `income_time` datetime DEFAULT NULL COMMENT '收入日期',
  `income_type` int DEFAULT 1 COMMENT '收入类型:1=销售收入,2=其他收入,3=预收账款',
  `customer_id` bigint DEFAULT NULL COMMENT '客户ID',
  `customer_name` varchar(150) DEFAULT '' COMMENT '客户名称',
  `account_id` bigint DEFAULT NULL COMMENT '收款账户',
  `account_name` varchar(100) DEFAULT '' COMMENT '收款账户名称',
  `money` decimal(15,2) DEFAULT 0 COMMENT '收入金额',
  `counterparty` varchar(100) DEFAULT '' COMMENT '往来单位',
  `note` varchar(255) DEFAULT '' COMMENT '备注',
  `createtime` datetime DEFAULT NULL,
  `updatetime` datetime DEFAULT NULL,
  `deletetime` datetime DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_income_code` (`income_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='收入单';


CREATE TABLE IF NOT EXISTS `hou_erp_expenditure` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `admin_id` bigint DEFAULT 1,
  `expend_code` varchar(50) NOT NULL DEFAULT '' COMMENT '支出单编号',
  `expend_time` datetime DEFAULT NULL COMMENT '支出日期',
  `expend_type` int DEFAULT 1 COMMENT '支出类型:1=采购付款,2=工资,3=房租,4=水电,5=其他',
  `supplier_id` bigint DEFAULT NULL COMMENT '供应商ID',
  `supplier_name` varchar(120) DEFAULT '' COMMENT '供应商名称',
  `account_id` bigint DEFAULT NULL COMMENT '付款账户',
  `account_name` varchar(100) DEFAULT '' COMMENT '付款账户名称',
  `money` decimal(15,2) DEFAULT 0 COMMENT '支出金额',
  `counterparty` varchar(100) DEFAULT '' COMMENT '往来单位',
  `note` varchar(255) DEFAULT '' COMMENT '备注',
  `createtime` datetime DEFAULT NULL,
  `updatetime` datetime DEFAULT NULL,
  `deletetime` datetime DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_expend_code` (`expend_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='支出单';


CREATE TABLE IF NOT EXISTS `hou_erp_fund` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `admin_id` bigint DEFAULT 1,
  `fund_code` varchar(50) NOT NULL DEFAULT '' COMMENT '资金单编号',
  `fund_time` datetime DEFAULT NULL COMMENT '资金日期',
  `fund_type` int DEFAULT 1 COMMENT '资金类型:1=收款,2=付款,3=转账,4=提现,5=存款',
  `account_out_id` bigint DEFAULT NULL COMMENT '转出账户',
  `account_out_name` varchar(100) DEFAULT '' COMMENT '转出账户名称',
  `account_enter_id` bigint DEFAULT NULL COMMENT '转入账户',
  `account_enter_name` varchar(100) DEFAULT '' COMMENT '转入账户名称',
  `money` decimal(15,2) DEFAULT 0 COMMENT '金额',
  `counterparty` varchar(100) DEFAULT '' COMMENT '往来单位',
  `note` varchar(255) DEFAULT '' COMMENT '备注',
  `status` int DEFAULT 1 COMMENT '状态:0=草稿,1=已确认',
  `createtime` datetime DEFAULT NULL,
  `updatetime` datetime DEFAULT NULL,
  `deletetime` datetime DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_fund_code` (`fund_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='资金管理单';


CREATE TABLE IF NOT EXISTS `hou_erp_budget` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `admin_id` bigint DEFAULT 1,
  `budget_code` varchar(50) NOT NULL DEFAULT '' COMMENT '预算编号',
  `budget_name` varchar(150) NOT NULL DEFAULT '' COMMENT '预算名称',
  `budget_type` int DEFAULT 1 COMMENT '预算类型:1=收入,2=支出,3=投资',
  `department` varchar(100) DEFAULT '' COMMENT '预算部门',
  `year` int DEFAULT NULL COMMENT '年度',
  `month` int DEFAULT NULL COMMENT '月度(0=年度)',
  `amount` decimal(15,2) DEFAULT 0 COMMENT '预算金额',
  `used_amount` decimal(15,2) DEFAULT 0 COMMENT '已用金额',
  `note` varchar(255) DEFAULT '' COMMENT '备注',
  `status` int DEFAULT 0 COMMENT '状态:0=草稿,1=已审批,2=已关闭',
  `createtime` datetime DEFAULT NULL,
  `updatetime` datetime DEFAULT NULL,
  `deletetime` datetime DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_budget_code` (`budget_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='预算管理';


CREATE TABLE IF NOT EXISTS `hou_erp_cost` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `admin_id` bigint DEFAULT 1,
  `cost_code` varchar(50) NOT NULL DEFAULT '' COMMENT '成本单编号',
  `cost_time` datetime DEFAULT NULL COMMENT '成本日期',
  `goods_id` bigint DEFAULT NULL COMMENT '商品ID',
  `goods_code` varchar(50) DEFAULT '' COMMENT '商品编号',
  `goods_name` varchar(255) DEFAULT '' COMMENT '商品名称',
  `cost_type` int DEFAULT 1 COMMENT '成本类型:1=原材料,2=人工,3=制造费用,4=其他',
  `direct_cost` decimal(15,2) DEFAULT 0 COMMENT '直接成本',
  `indirect_cost` decimal(15,2) DEFAULT 0 COMMENT '间接成本',
  `total_cost` decimal(15,2) DEFAULT 0 COMMENT '总成本',
  `quantity` decimal(15,2) DEFAULT 0 COMMENT '产量',
  `unit_cost` decimal(15,2) DEFAULT 0 COMMENT '单位成本',
  `note` varchar(255) DEFAULT '' COMMENT '备注',
  `createtime` datetime DEFAULT NULL,
  `updatetime` datetime DEFAULT NULL,
  `deletetime` datetime DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_cost_code` (`cost_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='成本管理';


-- 应收应付明细(共享表,保持原结构: int时间戳)

-- 应付明细

-- =====================================================================
-- 七、产品管理: BOM 物料清单
-- =====================================================================

CREATE TABLE IF NOT EXISTS `hou_erp_bom` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `admin_id` bigint DEFAULT 1,
  `bom_code` varchar(50) NOT NULL DEFAULT '' COMMENT 'BOM编号',
  `product_id` bigint DEFAULT NULL COMMENT '成品商品ID',
  `product_code` varchar(50) DEFAULT '' COMMENT '成品编号',
  `product_name` varchar(255) DEFAULT '' COMMENT '成品名称',
  `version` varchar(20) DEFAULT 'V1.0' COMMENT '版本',
  `component_id` bigint DEFAULT NULL COMMENT '组件商品ID',
  `component_code` varchar(50) DEFAULT '' COMMENT '组件编号',
  `component_name` varchar(255) DEFAULT '' COMMENT '组件名称',
  `component_number` decimal(15,2) DEFAULT 0 COMMENT '单件用量',
  `unit_cost` decimal(15,2) DEFAULT 0 COMMENT '组件成本',
  `total_cost` decimal(15,2) DEFAULT 0 COMMENT '组件成本小计',
  `note` varchar(255) DEFAULT '' COMMENT '备注',
  `status` int DEFAULT 1 COMMENT '状态:0=停用,1=启用',
  `createtime` datetime DEFAULT NULL,
  `updatetime` datetime DEFAULT NULL,
  `deletetime` datetime DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_bom_code` (`bom_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='物料清单BOM';


-- =====================================================================
-- 八、生产管理: 车间/工艺/生产计划/生产订单/质检
-- =====================================================================

CREATE TABLE IF NOT EXISTS `hou_erp_workshop` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `admin_id` bigint DEFAULT 1,
  `workshop_code` varchar(50) NOT NULL DEFAULT '' COMMENT '车间编号',
  `workshop_name` varchar(150) NOT NULL DEFAULT '' COMMENT '车间名称',
  `workshop_type` int DEFAULT 1 COMMENT '车间类型:1=机加工,2=装配,3=电子,4=喷涂,5=其他',
  `manager` varchar(50) DEFAULT '' COMMENT '车间主任',
  `address` varchar(255) DEFAULT '' COMMENT '车间位置',
  `device_num` int DEFAULT 0 COMMENT '设备数量',
  `staff_num` int DEFAULT 0 COMMENT '人员数量',
  `area` decimal(15,2) DEFAULT 0 COMMENT '车间面积',
  `status` int DEFAULT 1 COMMENT '状态:0=停用,1=启用',
  `note` varchar(255) DEFAULT '' COMMENT '备注',
  `createtime` datetime DEFAULT NULL,
  `updatetime` datetime DEFAULT NULL,
  `deletetime` datetime DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_workshop_code` (`workshop_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='车间';


CREATE TABLE IF NOT EXISTS `hou_erp_process` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `admin_id` bigint DEFAULT 1,
  `process_code` varchar(50) NOT NULL DEFAULT '' COMMENT '工艺编号',
  `process_name` varchar(150) NOT NULL DEFAULT '' COMMENT '工艺名称',
  `workshop_id` bigint DEFAULT NULL COMMENT '所属车间ID',
  `workshop_name` varchar(150) DEFAULT '' COMMENT '所属车间名称',
  `process_type` int DEFAULT 1 COMMENT '工艺类型:1=机加工,2=SMT贴片,3=焊接,4=装配,5=测试,6=包装',
  `machine` varchar(100) DEFAULT '' COMMENT '使用设备',
  `standard_hours` decimal(10,2) DEFAULT 0 COMMENT '标准工时(小时/件)',
  `unit_cost` decimal(15,2) DEFAULT 0 COMMENT '单位加工成本',
  `status` int DEFAULT 1 COMMENT '状态:0=停用,1=启用',
  `note` varchar(255) DEFAULT '' COMMENT '备注',
  `createtime` datetime DEFAULT NULL,
  `updatetime` datetime DEFAULT NULL,
  `deletetime` datetime DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_process_code` (`process_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='工艺路线';


CREATE TABLE IF NOT EXISTS `hou_erp_production_plan` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `admin_id` bigint DEFAULT 1,
  `plan_code` varchar(50) NOT NULL DEFAULT '' COMMENT '计划编号',
  `plan_time` datetime DEFAULT NULL COMMENT '计划日期',
  `goods_id` bigint DEFAULT NULL COMMENT '商品ID',
  `goods_code` varchar(50) DEFAULT '' COMMENT '商品编号',
  `goods_name` varchar(255) DEFAULT '' COMMENT '商品名称',
  `plan_number` decimal(15,2) DEFAULT 0 COMMENT '计划数量',
  `finished_number` decimal(15,2) DEFAULT 0 COMMENT '已完成数量',
  `plan_start_time` datetime DEFAULT NULL COMMENT '计划开始',
  `plan_end_time` datetime DEFAULT NULL COMMENT '计划完成',
  `priority` int DEFAULT 2 COMMENT '优先级:1=高,2=中,3=低',
  `workshop_id` bigint DEFAULT NULL COMMENT '执行车间',
  `status` int DEFAULT 0 COMMENT '状态:0=草稿,1=已下达,2=进行中,3=已完成,4=已取消',
  `note` varchar(255) DEFAULT '' COMMENT '备注',
  `createtime` datetime DEFAULT NULL,
  `updatetime` datetime DEFAULT NULL,
  `deletetime` datetime DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_plan_code` (`plan_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='生产计划';


CREATE TABLE IF NOT EXISTS `hou_erp_production_order` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `admin_id` bigint DEFAULT 1,
  `order_code` varchar(50) NOT NULL DEFAULT '' COMMENT '生产单编号',
  `order_time` datetime DEFAULT NULL COMMENT '下达日期',
  `plan_id` bigint DEFAULT NULL COMMENT '生产计划ID',
  `plan_code` varchar(50) DEFAULT '' COMMENT '生产计划编号',
  `goods_id` bigint DEFAULT NULL COMMENT '商品ID',
  `goods_code` varchar(50) DEFAULT '' COMMENT '商品编号',
  `goods_name` varchar(255) DEFAULT '' COMMENT '商品名称',
  `order_number` decimal(15,2) DEFAULT 0 COMMENT '生产数量',
  `finished_number` decimal(15,2) DEFAULT 0 COMMENT '完工数量',
  `process_id` bigint DEFAULT NULL COMMENT '工艺ID',
  `workshop_id` bigint DEFAULT NULL COMMENT '执行车间',
  `machine` varchar(100) DEFAULT '' COMMENT '使用设备',
  `status` int DEFAULT 0 COMMENT '状态:0=待排产,1=生产中,2=已完成,3=已暂停,4=已取消',
  `start_time` datetime DEFAULT NULL COMMENT '开始时间',
  `end_time` datetime DEFAULT NULL COMMENT '完成时间',
  `quality_status` int DEFAULT 0 COMMENT '质检状态:0=未质检,1=质检合格,2=有不合格',
  `note` varchar(255) DEFAULT '' COMMENT '备注',
  `createtime` datetime DEFAULT NULL,
  `updatetime` datetime DEFAULT NULL,
  `deletetime` datetime DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_order_code` (`order_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='生产订单';


CREATE TABLE IF NOT EXISTS `hou_erp_quality` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `admin_id` bigint DEFAULT 1,
  `check_code` varchar(50) NOT NULL DEFAULT '' COMMENT '质检单编号',
  `check_time` datetime DEFAULT NULL COMMENT '质检日期',
  `order_id` bigint DEFAULT NULL COMMENT '生产订单ID',
  `order_code` varchar(50) DEFAULT '' COMMENT '生产订单编号',
  `goods_id` bigint DEFAULT NULL COMMENT '商品ID',
  `goods_code` varchar(50) DEFAULT '' COMMENT '商品编号',
  `goods_name` varchar(255) DEFAULT '' COMMENT '商品名称',
  `check_number` decimal(15,2) DEFAULT 0 COMMENT '检验数量',
  `ok_number` decimal(15,2) DEFAULT 0 COMMENT '合格数量',
  `bad_number` decimal(15,2) DEFAULT 0 COMMENT '不合格数量',
  `check_type` int DEFAULT 1 COMMENT '检验类型:1=来料检验,2=制程检验,3=成品检验',
  `result` int DEFAULT 0 COMMENT '结果:0=不合格,1=合格,2=待检',
  `standard` varchar(255) DEFAULT '' COMMENT '检验标准',
  `bad_reason` varchar(255) DEFAULT '' COMMENT '不合格原因',
  `handler` varchar(50) DEFAULT '' COMMENT '检验员',
  `note` varchar(255) DEFAULT '' COMMENT '备注',
  `createtime` datetime DEFAULT NULL,
  `updatetime` datetime DEFAULT NULL,
  `deletetime` datetime DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_check_code` (`check_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='质检单';


-- =====================================================================
-- 九、概览与报表
-- =====================================================================

CREATE TABLE IF NOT EXISTS `hou_erp_dashboard` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `admin_id` bigint DEFAULT 1,
  `total_sales` decimal(15,2) DEFAULT 0 COMMENT '销售总额',
  `total_purchases` decimal(15,2) DEFAULT 0 COMMENT '采购总额',
  `total_inventory` decimal(15,2) DEFAULT 0 COMMENT '库存总额',
  `total_accounts` decimal(15,2) DEFAULT 0 COMMENT '账户总额',
  `createtime` datetime DEFAULT NULL,
  `updatetime` datetime DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='ERP概览';


CREATE TABLE IF NOT EXISTS `hou_erp_report` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `admin_id` bigint DEFAULT 1,
  `report_type` varchar(20) DEFAULT '' COMMENT '报表类型:financial/business/inventory/sales',
  `report_name` varchar(100) DEFAULT '' COMMENT '报表名称',
  `start_time` datetime DEFAULT NULL COMMENT '开始时间',
  `end_time` datetime DEFAULT NULL COMMENT '结束时间',
  `report_data` text COMMENT '报表数据JSON',
  `createtime` datetime DEFAULT NULL,
  `updatetime` datetime DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='ERP报表';

SET FOREIGN_KEY_CHECKS = 1;

-- =====================================================================
-- 十、库存调整单
-- =====================================================================
CREATE TABLE IF NOT EXISTS `hou_erp_stock_adjustment` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `admin_id` bigint DEFAULT 1,
  `adjust_code` varchar(50) NOT NULL DEFAULT '' COMMENT '调整单编号',
  `adjust_time` datetime DEFAULT NULL COMMENT '调整日期',
  `goods_id` bigint DEFAULT NULL COMMENT '商品ID',
  `goods_code` varchar(50) DEFAULT '' COMMENT '商品编号',
  `goods_name` varchar(255) DEFAULT '' COMMENT '商品名称',
  `stores_id` bigint DEFAULT NULL COMMENT '仓库ID',
  `stores_name` varchar(255) DEFAULT '' COMMENT '仓库名称',
  `before_number` decimal(15,2) DEFAULT 0 COMMENT '调整前数量',
  `after_number` decimal(15,2) DEFAULT 0 COMMENT '调整后数量',
  `number` decimal(15,2) DEFAULT 0 COMMENT '调整数量(正增负减)',
  `adjust_type` int DEFAULT 1 COMMENT '调整类型:1=盘盈,2=盘亏,3=报损,4=其他',
  `reason` varchar(255) DEFAULT '' COMMENT '调整原因',
  `handler` varchar(50) DEFAULT '' COMMENT '经手人',
  `examine_status` int DEFAULT 1 COMMENT '审核状态:0=未审核,1=已审核',
  `note` varchar(255) DEFAULT '' COMMENT '备注',
  `createtime` datetime DEFAULT NULL,
  `updatetime` datetime DEFAULT NULL,
  `deletetime` datetime DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_adjust_code` (`adjust_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='库存调整单';


SET FOREIGN_KEY_CHECKS = 1;

SET FOREIGN_KEY_CHECKS=1;
