-- =====================================================================
-- 插件 mes 的数据库建表脚本（共 129 张表）
-- 用途：安装「mes」插件后按需执行（也可由插件自身的迁移通道建表）。
-- 🔴 商城核心（Core）初始化不包含本文件；未安装该插件时不要执行。
-- 全部 CREATE TABLE 已统一补 IF NOT EXISTS（幂等，可重复执行，不会删数据）。
-- 执行期间临时关闭外键检查：插件之间存在跨插件外键（如 oa 引用 hou_pm_project），
-- 未同时安装被引用插件时允许该外键暂时悬空。
-- =====================================================================

SET FOREIGN_KEY_CHECKS=0;

-- ---------- 基础表结构（结构基线） ----------
-- Table structure for table `hou_mes_acceptance`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_mes_acceptance` (
  `id` int NOT NULL AUTO_INCREMENT,
  `code_type` enum('1','2') DEFAULT '1' COMMENT '验收类型:1=加工单,2=委外加工单',
  `chase_code` varchar(50) DEFAULT NULL COMMENT '加工单单号',
  `production_code` varchar(50) DEFAULT NULL COMMENT '生产单号',
  `outsourcing_code` varchar(50) DEFAULT NULL COMMENT '委外加工单单号',
  `purchase_code` varchar(50) DEFAULT NULL COMMENT '单据编号',
  `purchase_time` varchar(50) DEFAULT NULL COMMENT '单据日期',
  `delivery_time` int DEFAULT NULL COMMENT '交货时间',
  `workshop_id` int DEFAULT NULL COMMENT '车间',
  `work_id` int DEFAULT NULL COMMENT '生产工人',
  `goods_name` varchar(255) DEFAULT NULL COMMENT '商品名称',
  `goods_code` varchar(100) DEFAULT NULL COMMENT '商品编号',
  `goods_unit` int DEFAULT NULL COMMENT '商品单位',
  `default_unit` int DEFAULT NULL COMMENT '基本单位',
  `number` double(15,4) DEFAULT NULL COMMENT '数量',
  `finish_number` double(15,4) DEFAULT NULL COMMENT '完成数量',
  `basic_nmber` double(15,4) DEFAULT NULL COMMENT '基本数量',
  `unit_price` double(15,4) DEFAULT NULL COMMENT '单价',
  `all_money` float(11,2) DEFAULT NULL COMMENT '金额',
  `examine_status` enum('0','1') DEFAULT '0' COMMENT '审核状态:0=未审核,1=已审核',
  `examine_time` int DEFAULT NULL COMMENT '审核时间',
  `status` enum('0','1') DEFAULT '0' COMMENT '状态:0=未完成,1=已完成',
  `remark` varchar(255) DEFAULT NULL COMMENT '备注',
  `preparation_id` int DEFAULT NULL COMMENT '制单人',
  `examine_id` int DEFAULT NULL COMMENT '审核人',
  `operation_id` int DEFAULT NULL COMMENT '最后操作人',
  `mrp_status` enum('0','1') DEFAULT '0' COMMENT 'mrp状态',
  `work_time` int DEFAULT NULL COMMENT '开工时间',
  `finish_time` int DEFAULT NULL COMMENT '完工时间',
  `createtime` int DEFAULT NULL COMMENT '创建时间',
  `updatetime` int DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `purchase_code` (`purchase_code`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='验收单';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_mes_acceptance_goods`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_mes_acceptance_goods` (
  `id` int NOT NULL AUTO_INCREMENT,
  `type` enum('1','2') DEFAULT '1' COMMENT '类型:1=原材料,2=商品',
  `production_id` int DEFAULT NULL COMMENT '生产ID',
  `goods_id` int DEFAULT NULL COMMENT '商品ID',
  `goods_code` varchar(255) DEFAULT NULL COMMENT '商品编号',
  `goods_name` varchar(255) DEFAULT NULL COMMENT '商品名称',
  `goods_note` varchar(255) DEFAULT NULL COMMENT '商品规格型号',
  `unit_id` int DEFAULT NULL COMMENT '单位ID',
  `default_unit` int DEFAULT NULL COMMENT '默认单位',
  `purchase` double(15,4) DEFAULT '0.0000' COMMENT '进价',
  `need_number` double(15,4) DEFAULT '0.0000' COMMENT '生产数量',
  `number` double(15,4) DEFAULT '0.0000' COMMENT '验收数量',
  `bad_number` double(15,4) DEFAULT '0.0000' COMMENT '不合格数量',
  `basic_nmber` double(15,4) DEFAULT NULL COMMENT '基本数量',
  `money` double(15,4) DEFAULT NULL COMMENT '金额',
  `remark` text COMMENT '备注',
  `stores_id` int DEFAULT NULL COMMENT '仓库',
  `allow_stock` double(15,4) DEFAULT '0.0000' COMMENT '可用库存',
  `goods_attr` int DEFAULT NULL COMMENT '商品属性',
  `batch_id` int DEFAULT NULL COMMENT '批次ID',
  `batch_name` varchar(100) DEFAULT NULL COMMENT '批次名称',
  `manufacture` int DEFAULT NULL COMMENT '生产日期',
  `guarantee` int DEFAULT NULL COMMENT '保质期',
  `validity` int DEFAULT NULL COMMENT '到期时间',
  `period` text COMMENT '序列号',
  `createtime` int DEFAULT NULL COMMENT '创建时间',
  `updatetime` int DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='验收单同步商品';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_mes_acceptance_info`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_mes_acceptance_info` (
  `id` int NOT NULL AUTO_INCREMENT,
  `type` enum('1','2') DEFAULT '1' COMMENT '类型:1=原材料,2=商品',
  `production_id` int DEFAULT NULL COMMENT '生产ID',
  `goods_id` int DEFAULT NULL COMMENT '商品ID',
  `goods_code` varchar(255) DEFAULT NULL COMMENT '商品编号',
  `goods_name` varchar(255) DEFAULT NULL COMMENT '商品名称',
  `goods_note` varchar(255) DEFAULT NULL COMMENT '商品规格型号',
  `unit_id` int DEFAULT NULL COMMENT '单位ID',
  `default_unit` int DEFAULT NULL COMMENT '默认单位',
  `purchase` double(15,4) DEFAULT '0.0000' COMMENT '进价',
  `need_number` double(15,4) DEFAULT '0.0000' COMMENT '生产数量',
  `number` double(15,4) DEFAULT '0.0000' COMMENT '验收数量',
  `bad_number` double(15,4) DEFAULT '0.0000' COMMENT '不合格数量',
  `basic_nmber` double(15,4) DEFAULT NULL COMMENT '基本数量',
  `money` double(15,4) DEFAULT NULL COMMENT '金额',
  `remark` text COMMENT '备注',
  `stores_id` int DEFAULT NULL COMMENT '仓库',
  `allow_stock` double(15,4) DEFAULT '0.0000' COMMENT '可用库存',
  `goods_attr` int DEFAULT NULL COMMENT '商品属性',
  `batch_id` int DEFAULT NULL COMMENT '批次ID',
  `batch_name` varchar(100) DEFAULT NULL COMMENT '批次名称',
  `manufacture` int DEFAULT NULL COMMENT '生产日期',
  `guarantee` int DEFAULT NULL COMMENT '保质期',
  `validity` int DEFAULT NULL COMMENT '到期时间',
  `period` text COMMENT '序列号',
  `createtime` int DEFAULT NULL COMMENT '创建时间',
  `updatetime` int DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='验收单详情';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_mes_accounts`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_mes_accounts` (
  `id` int NOT NULL AUTO_INCREMENT,
  `admin_id` int DEFAULT NULL,
  `account_coding` varchar(100) NOT NULL COMMENT '账户编号',
  `account_name` varchar(100) NOT NULL COMMENT '账户名称',
  `balance` double(15,4) DEFAULT NULL COMMENT '当前余额',
  `start_account_balance` double(15,4) NOT NULL DEFAULT '0.0000' COMMENT '期初余额',
  `start_account_time` varchar(20) NOT NULL DEFAULT '' COMMENT '建账日期',
  `account_type_id` enum('1','2') NOT NULL DEFAULT '1' COMMENT '账户类型:1=现金,2=银行存款',
  `is_default` enum('0','1') DEFAULT '0' COMMENT '默认:0=否,1=是',
  `createtime` int DEFAULT NULL COMMENT '创建时间',
  `updatetime` int DEFAULT NULL COMMENT '更新时间',
  `deletetime` int DEFAULT NULL COMMENT '删除时间',
  `org_id` varchar(64) NOT NULL DEFAULT '1',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_mes_accounts_org` (`org_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='账户表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_mes_accounts_copewith`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_mes_accounts_copewith` (
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
  `org_id` varchar(64) NOT NULL DEFAULT '1',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_mes_accounts_copewith_org` (`org_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='应付账款明细表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_mes_accounts_log`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_mes_accounts_log` (
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
  `org_id` varchar(64) NOT NULL DEFAULT '1',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_mes_accounts_log_org` (`org_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='账户记录表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_mes_accounts_receivable`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_mes_accounts_receivable` (
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
  `org_id` varchar(64) NOT NULL DEFAULT '1',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_mes_accounts_receivable_org` (`org_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='应收账款明细表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_mes_accounts_transfer`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_mes_accounts_transfer` (
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
  `org_id` varchar(64) NOT NULL DEFAULT '1',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_mes_accounts_transfer_org` (`org_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='资金转账单';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_mes_accounts_transfer_info`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_mes_accounts_transfer_info` (
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
-- Table structure for table `hou_mes_allocation`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_mes_allocation` (
  `id` int NOT NULL AUTO_INCREMENT,
  `admin_id` int DEFAULT NULL,
  `purchase_code` varchar(50) NOT NULL DEFAULT '' COMMENT '单据编号',
  `purchase_time` int DEFAULT NULL COMMENT '单据日期',
  `preparation_id` int DEFAULT NULL COMMENT '制单人',
  `examine_id` int DEFAULT NULL COMMENT '审核人',
  `operation_id` int DEFAULT NULL COMMENT '最后操作人',
  `examine_status` enum('0','1') DEFAULT '0' COMMENT '审核状态:0=未审核,1=已审核',
  `examine_time` int DEFAULT NULL COMMENT '审核时间',
  `remarks` varchar(255) DEFAULT NULL COMMENT '备注',
  `print_num` int DEFAULT '0' COMMENT '打印次数',
  `createtime` int DEFAULT NULL COMMENT '创建时间',
  `updatetime` int DEFAULT NULL COMMENT '更新时间',
  `org_id` varchar(64) NOT NULL DEFAULT '1',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_mes_allocation_org` (`org_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='调拨单';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_mes_allocation_info`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_mes_allocation_info` (
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
-- Table structure for table `hou_mes_assemble`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_mes_assemble` (
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
-- Table structure for table `hou_mes_assemble_info`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_mes_assemble_info` (
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
-- Table structure for table `hou_mes_assemble_info_temp`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_mes_assemble_info_temp` (
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
-- Table structure for table `hou_mes_assemble_temp`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_mes_assemble_temp` (
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
-- Table structure for table `hou_mes_attr_combination`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_mes_attr_combination` (
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
-- Table structure for table `hou_mes_attribute`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_mes_attribute` (
  `id` int NOT NULL AUTO_INCREMENT,
  `name` varchar(100) DEFAULT NULL COMMENT '属性名称',
  `createtime` int DEFAULT NULL COMMENT '创建时间',
  `updatetime` int DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='属性';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_mes_aux_combination`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_mes_aux_combination` (
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
-- Table structure for table `hou_mes_auxiliary`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_mes_auxiliary` (
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
-- Table structure for table `hou_mes_bom`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_mes_bom` (
  `id` int NOT NULL AUTO_INCREMENT,
  `purchase_code` varchar(50) DEFAULT NULL COMMENT '单据编号',
  `name` varchar(255) DEFAULT NULL COMMENT '清单名称',
  `goods_name` varchar(255) DEFAULT NULL COMMENT '商品名称',
  `goods_code` varchar(100) DEFAULT NULL COMMENT '商品编号',
  `number` double(15,4) DEFAULT NULL COMMENT '数量',
  `all_money` float(11,2) DEFAULT NULL COMMENT '金额',
  `wages` double(15,4) DEFAULT '0.0000' COMMENT '计件工资',
  `createtime` int DEFAULT NULL COMMENT '创建时间',
  `updatetime` int DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `purchase_code` (`purchase_code`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='bom清单';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_mes_bom_info`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_mes_bom_info` (
  `id` int NOT NULL AUTO_INCREMENT,
  `bom_id` int DEFAULT NULL COMMENT '清单ID',
  `procedure_id` int DEFAULT NULL COMMENT '工序ID',
  `goods_id` int DEFAULT NULL COMMENT '商品ID',
  `goods_code` varchar(255) DEFAULT NULL COMMENT '商品编号',
  `goods_name` varchar(255) DEFAULT NULL COMMENT '商品名称',
  `goods_note` varchar(255) DEFAULT NULL COMMENT '商品规格型号',
  `unit_id` int DEFAULT NULL COMMENT '单位ID',
  `default_unit` int DEFAULT NULL COMMENT '默认单位',
  `purchase` double(15,4) DEFAULT '0.0000' COMMENT '进价',
  `number` double(15,4) DEFAULT '0.0000' COMMENT '数量',
  `basic_nmber` double(15,4) DEFAULT NULL COMMENT '基本数量',
  `money` double(15,4) DEFAULT NULL COMMENT '金额',
  `remark` text COMMENT '备注',
  `createtime` int DEFAULT NULL COMMENT '创建时间',
  `updatetime` int DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='bom商品';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_mes_bom_procedure`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_mes_bom_procedure` (
  `id` int NOT NULL AUTO_INCREMENT,
  `bom_id` int DEFAULT NULL COMMENT '清单ID',
  `purchase_code` varchar(255) DEFAULT NULL COMMENT '工序编号',
  `name` varchar(255) DEFAULT NULL COMMENT '工序名称',
  `workshop_id` int DEFAULT NULL COMMENT '车间ID',
  `wages` double(15,4) DEFAULT '0.0000' COMMENT '单位工资',
  `hours` float(11,2) DEFAULT '0.00' COMMENT '标准工时',
  `remark` text COMMENT '备注',
  `createtime` int DEFAULT NULL COMMENT '创建时间',
  `updatetime` int DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='bom工序';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_mes_checkout`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_mes_checkout` (
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
-- Table structure for table `hou_mes_checkout_info`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_mes_checkout_info` (
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
-- Table structure for table `hou_mes_collection`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_mes_collection` (
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
-- Table structure for table `hou_mes_collection_account`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_mes_collection_account` (
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
-- Table structure for table `hou_mes_collection_sale`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_mes_collection_sale` (
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
-- Table structure for table `hou_mes_cost`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_mes_cost` (
  `id` int NOT NULL AUTO_INCREMENT,
  `admin_id` int DEFAULT NULL,
  `purchase_code` varchar(50) DEFAULT NULL COMMENT '单据编号',
  `purchase_time` int DEFAULT NULL COMMENT '单据日期',
  `business_type` enum('1','2','3','4') DEFAULT NULL COMMENT '业务类型:1=采购,2=销售,3=采购退回,4=销售退回',
  `suppliers_id` int DEFAULT NULL COMMENT '供应商',
  `suppliers_ids` int DEFAULT NULL COMMENT '源单往来单位',
  `expend_type_id` int DEFAULT NULL COMMENT '支出类别',
  `customers_id` int DEFAULT NULL COMMENT '客户',
  `income_type_id` int DEFAULT NULL COMMENT '收入类别',
  `purchase_number` varchar(50) DEFAULT NULL COMMENT '源单号',
  `purchase_times` int DEFAULT NULL COMMENT '源单日期',
  `money` double(15,4) DEFAULT NULL COMMENT '金额',
  `pay_money` double(15,4) DEFAULT NULL COMMENT '未付金额',
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='采购销售费用清单';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_mes_customers`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_mes_customers` (
  `id` int NOT NULL AUTO_INCREMENT,
  `admin_id` int DEFAULT NULL,
  `customer_code` varchar(50) DEFAULT NULL COMMENT '客户编号',
  `customertype_id` int NOT NULL DEFAULT '0' COMMENT '客户类别',
  `customerlevel_id` int NOT NULL DEFAULT '1' COMMENT '客户等级',
  `customer_name` varchar(150) NOT NULL DEFAULT '' COMMENT '客户名称',
  `saler` varchar(100) NOT NULL DEFAULT '' COMMENT '销售人员',
  `connet_name` varchar(50) NOT NULL DEFAULT '' COMMENT '联系人',
  `customer_mobile` varchar(11) NOT NULL DEFAULT '' COMMENT '客户手机',
  `customer_phone` varchar(20) NOT NULL DEFAULT '' COMMENT '客户座机',
  `qq_wx_email` varchar(100) NOT NULL DEFAULT '' COMMENT 'QQ/微信/Email',
  `need_receive_account` double(15,4) NOT NULL DEFAULT '0.0000' COMMENT '应收款余额',
  `receive_account` double(15,4) DEFAULT NULL COMMENT '期初应收款',
  `opening_prepayment` double(15,4) DEFAULT NULL COMMENT '期初预收款',
  `identification` varchar(100) DEFAULT NULL COMMENT '纳税人识别号',
  `getgoods_address` varchar(255) NOT NULL DEFAULT '' COMMENT '收货地址',
  `note` varchar(255) NOT NULL DEFAULT '' COMMENT '备注',
  `deposit_bank` varchar(255) DEFAULT NULL COMMENT '开户银行',
  `bank_number` varchar(255) DEFAULT NULL COMMENT '银行卡号',
  `staff_member_id` int DEFAULT NULL COMMENT '销售人员',
  `account_time` int unsigned DEFAULT NULL COMMENT '余额日期',
  `third_id` int DEFAULT '0' COMMENT '三方ID',
  `status` enum('0','1') NOT NULL DEFAULT '0' COMMENT '状态',
  `createtime` int DEFAULT NULL COMMENT '创建时间',
  `updatetime` int DEFAULT NULL COMMENT '更新时间',
  `org_id` varchar(64) NOT NULL DEFAULT '1',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_mes_customers_org` (`org_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='委外工厂表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_mes_customers_user`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_mes_customers_user` (
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='联系人';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_mes_employee_report`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_mes_employee_report` (
  `id` int NOT NULL AUTO_INCREMENT,
  `org_id` int DEFAULT NULL COMMENT '组织ID',
  `factory_name` varchar(100) DEFAULT NULL COMMENT '工厂名称',
  `shift_info` varchar(100) DEFAULT NULL COMMENT '班次信息',
  `work_center` varchar(100) DEFAULT NULL COMMENT '工作中心',
  `operator` varchar(100) DEFAULT NULL COMMENT '操作员',
  `equipment_id` int DEFAULT NULL COMMENT '设备ID',
  `equipment_code` varchar(100) DEFAULT NULL COMMENT '设备编码',
  `equipment_name` varchar(255) DEFAULT NULL COMMENT '设备名称',
  `production_task_id` int DEFAULT NULL COMMENT '生产任务ID',
  `production_batch_no` varchar(100) DEFAULT NULL COMMENT '生产批号',
  `start_time` datetime DEFAULT NULL COMMENT '开始时间',
  `material_spec` varchar(255) DEFAULT NULL COMMENT '物料信息',
  `process_info` varchar(255) DEFAULT NULL COMMENT '工序信息',
  `completion_time` datetime DEFAULT NULL COMMENT '完工时间',
  `qualified_quantity` double(15,4) DEFAULT '0.0000' COMMENT '合格数量',
  `production_quantity` double(15,4) DEFAULT '0.0000' COMMENT '生产数量',
  `reported_quantity` double(15,4) DEFAULT '0.0000' COMMENT '报工数量',
  `scrapped_quantity` double(15,4) DEFAULT '0.0000' COMMENT '报废数量',
  `good_product_rate` decimal(5,2) DEFAULT '0.00' COMMENT '良品率(%)',
  `completion_rate` decimal(5,2) DEFAULT '0.00' COMMENT '完工率(%)',
  `is_disabled` enum('0','1') DEFAULT '0' COMMENT '是否停用:0=否,1=是',
  `current_personnel` varchar(100) DEFAULT NULL COMMENT '当前人员',
  `running_time` int DEFAULT NULL COMMENT '运行时长(分钟)',
  `equipment_start_time` datetime DEFAULT NULL COMMENT '设备开始时间',
  `status` enum('0','1','2') DEFAULT '0' COMMENT '状态:0=未开始,1=进行中,2=已完成',
  `createtime` int DEFAULT NULL COMMENT '创建时间',
  `updatetime` int DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_org_id` (`org_id`),
  KEY `idx_equipment_id` (`equipment_id`),
  KEY `idx_production_task_id` (`production_task_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='员工报工主表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_mes_enclosure`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_mes_enclosure` (
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
-- Table structure for table `hou_mes_enclosure_log`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_mes_enclosure_log` (
  `id` int NOT NULL AUTO_INCREMENT,
  `enclosure_id` int NOT NULL DEFAULT '0' COMMENT '附件ID',
  `order_id` int DEFAULT '0' COMMENT '单据ID',
  `purchase_code` varchar(50) DEFAULT NULL COMMENT '单据编号',
  `type` enum('1','2','3','4','5','6') DEFAULT NULL COMMENT '单据类型:1=购货订单,2:购货单,3:购货退货单,4:销货订单,5:销货单,6:销货退货单',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='原始单据关联表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_mes_equipment_running_record`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_mes_equipment_running_record` (
  `id` int NOT NULL AUTO_INCREMENT,
  `report_id` int NOT NULL COMMENT '报工ID',
  `equipment_id` int DEFAULT NULL COMMENT '设备ID',
  `equipment_code` varchar(100) DEFAULT NULL COMMENT '设备编码',
  `start_time` datetime DEFAULT NULL COMMENT '开始时间',
  `end_time` datetime DEFAULT NULL COMMENT '结束时间',
  `running_duration` int DEFAULT NULL COMMENT '运行时长(分钟)',
  `status` enum('0','1','2') DEFAULT '0' COMMENT '状态:0=停止,1=运行,2=故障',
  `createtime` int DEFAULT NULL COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_report_id` (`report_id`),
  KEY `idx_equipment_id` (`equipment_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='设备运行记录表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_mes_expend_type`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_mes_expend_type` (
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
-- Table structure for table `hou_mes_expenditure`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_mes_expenditure` (
  `id` int NOT NULL AUTO_INCREMENT,
  `admin_id` int DEFAULT NULL,
  `purchase_code` varchar(50) DEFAULT NULL COMMENT '单据编号',
  `purchase_time` int DEFAULT NULL COMMENT '单据日期',
  `business_type` enum('1','2') DEFAULT NULL COMMENT '业务类型:1=其他支出,2=其他',
  `suppliers_id` int DEFAULT NULL COMMENT '供应商',
  `accounts_id` int DEFAULT NULL COMMENT '账户',
  `cost_id` int DEFAULT NULL COMMENT '采购销售ID',
  `money` double(15,4) DEFAULT NULL COMMENT '金额',
  `pay_money` double(15,4) DEFAULT NULL COMMENT '已付金额',
  `arrears` double(15,4) DEFAULT NULL COMMENT '本次欠款',
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='其他支出单';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_mes_expenditure_info`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_mes_expenditure_info` (
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
-- Table structure for table `hou_mes_goods`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_mes_goods` (
  `id` int NOT NULL AUTO_INCREMENT,
  `admin_id` int unsigned DEFAULT NULL,
  `goods_code` varchar(30) NOT NULL DEFAULT '' COMMENT '商品编号',
  `goods_name` varchar(255) NOT NULL DEFAULT '' COMMENT '商品名称',
  `goods_barcode` varchar(50) DEFAULT '' COMMENT '商品条码',
  `goods_specification_id` varchar(50) NOT NULL DEFAULT '' COMMENT '规格型号',
  `goods_type_id` int NOT NULL DEFAULT '0' COMMENT '商品类别',
  `manager_warehouse_id` int NOT NULL DEFAULT '0' COMMENT '首选仓库',
  `unit_id` int NOT NULL DEFAULT '0' COMMENT '计量单位',
  `more_unit` enum('1','2') NOT NULL DEFAULT '2' COMMENT '多计量单位:1=是,2=否',
  `more_unit_id` int DEFAULT NULL COMMENT '多单位ID',
  `market_sell_price` double(15,4) NOT NULL DEFAULT '0.0000' COMMENT '零售价',
  `wholesale_price` double(15,4) NOT NULL DEFAULT '0.0000' COMMENT '批发价',
  `vip_price` double(15,4) NOT NULL DEFAULT '0.0000' COMMENT 'vip会员价',
  `discount` double(15,4) unsigned DEFAULT '0.0000' COMMENT '折扣率1',
  `discount2` double(15,4) unsigned DEFAULT '0.0000' COMMENT '折扣率2',
  `purchase` double(15,4) unsigned DEFAULT '0.0000' COMMENT '预计采购价',
  `goods_image` varchar(255) NOT NULL DEFAULT '' COMMENT '商品主图',
  `goods_images` text COMMENT '商品图集',
  `content` text COMMENT '商品详情',
  `inventory` double(15,4) NOT NULL DEFAULT '0.0000' COMMENT '总库存',
  `more_attribute` enum('0','1') DEFAULT '0' COMMENT '多属性:0=否,1=是',
  `attribute_id` varchar(30) DEFAULT '' COMMENT '商品属性条码',
  `note` varchar(255) NOT NULL COMMENT '备注',
  `is_offten` enum('0','1') NOT NULL DEFAULT '0' COMMENT '是否常用:0=否,1=是',
  `stock_warning` enum('0','1') DEFAULT '0' COMMENT '库存预警:0=否,1=是',
  `min_stock` int DEFAULT '0' COMMENT '最低库存',
  `max_stock` int DEFAULT '0' COMMENT '最高库存',
  `branch` enum('0','1') DEFAULT '0' COMMENT '分仓预警:0=否,1=是',
  `warehousing` int DEFAULT NULL COMMENT '首选入库单位',
  `warehouse` int DEFAULT NULL COMMENT '首选出库单位',
  `period` double(15,4) DEFAULT '0.0000' COMMENT '期初数量',
  `unit_cost` double(15,4) DEFAULT '0.0000' COMMENT '单位成本',
  `price` double(15,4) DEFAULT '0.0000' COMMENT '期初总价',
  `period_status` enum('0','1') DEFAULT '0' COMMENT '期初设置',
  `status` enum('0','1') NOT NULL DEFAULT '1' COMMENT '状态:0=下架,1=上架',
  `is_edit` enum('0','1') DEFAULT '1' COMMENT '允许修改:0=否,1=是',
  `auxiliary` tinyint DEFAULT NULL COMMENT '设置商品辅助属性',
  `auxiliary_type` varchar(255) DEFAULT NULL COMMENT '辅助属性',
  `auxiliary_type_c` varchar(255) DEFAULT NULL COMMENT '辅助属性2',
  `batch` tinyint DEFAULT NULL COMMENT '启用批次保质期管理',
  `shelf_life` int DEFAULT NULL COMMENT '保质期天数',
  `early_warning` int DEFAULT NULL COMMENT '预警天数',
  `serial` tinyint DEFAULT NULL COMMENT '启用序列号',
  `tag` set('hot','sale') DEFAULT 'sale' COMMENT '标签',
  `sale_number` double(15,4) DEFAULT '0.0000' COMMENT '销量',
  `order_status` enum('0','1') DEFAULT '1' COMMENT '订货通显示状态:0=否,1=是',
  `goods_type` enum('1','2') DEFAULT '1' COMMENT '商品类型:1=原材料,2=成品',
  `goods_id` int DEFAULT NULL COMMENT '进销存商品ID',
  `kuerp_goods_name` varchar(255) DEFAULT NULL COMMENT '进销存商品名称',
  `createtime` int DEFAULT NULL COMMENT '创建时间',
  `updatetime` int DEFAULT NULL COMMENT '更新时间',
  `deletetime` int unsigned DEFAULT NULL,
  `org_id` varchar(64) NOT NULL DEFAULT '1',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_mes_goods_org` (`org_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='商品表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_mes_goods_attr`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_mes_goods_attr` (
  `id` int NOT NULL AUTO_INCREMENT,
  `admin_id` int DEFAULT NULL,
  `goods_code` varchar(30) NOT NULL DEFAULT '' COMMENT '商品编号',
  `goods_name` varchar(255) NOT NULL DEFAULT '' COMMENT '商品名称',
  `goods_barcode` varchar(50) DEFAULT '' COMMENT '商品条码',
  `goods_id` int NOT NULL DEFAULT '0' COMMENT '商品ID',
  `attribute_id` int DEFAULT NULL COMMENT '属性组合ID',
  `attr_code` varchar(30) DEFAULT NULL COMMENT '属性编号',
  `barcode` varchar(30) DEFAULT NULL COMMENT '属性条码',
  `unit` int NOT NULL DEFAULT '0' COMMENT '计量单位',
  `market_sell_price` double(15,4) unsigned DEFAULT NULL COMMENT '零售价',
  `wholesale_price` double(15,4) unsigned DEFAULT NULL COMMENT '批发价',
  `vip_price` double(15,4) unsigned DEFAULT NULL COMMENT 'vip会员价',
  `discount` double(15,4) unsigned DEFAULT NULL COMMENT '折扣率1',
  `discount2` double(15,4) unsigned DEFAULT NULL COMMENT '折扣率2',
  `purchase` double(15,4) unsigned DEFAULT NULL COMMENT '预计采购价',
  `createtime` int DEFAULT NULL COMMENT '创建时间',
  `updatetime` int DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='商品属性价格表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_mes_goods_batch`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_mes_goods_batch` (
  `id` int NOT NULL AUTO_INCREMENT,
  `admin_id` int DEFAULT NULL,
  `goods_id` int NOT NULL DEFAULT '0' COMMENT '商品ID',
  `goods_code` varchar(30) DEFAULT NULL COMMENT '商品编号',
  `attribute_id` int DEFAULT '0' COMMENT '属性组合ID',
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
-- Table structure for table `hou_mes_goods_batch_exchange_info`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_mes_goods_batch_exchange_info` (
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
-- Table structure for table `hou_mes_goods_batch_info`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_mes_goods_batch_info` (
  `id` int NOT NULL AUTO_INCREMENT,
  `admin_id` int DEFAULT NULL,
  `batch_id` int DEFAULT NULL,
  `goods_id` int NOT NULL DEFAULT '0' COMMENT '商品ID',
  `goods_code` varchar(30) DEFAULT NULL COMMENT '商品编号',
  `attribute_id` int DEFAULT '0' COMMENT '属性组合ID',
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
  `type` tinyint DEFAULT '0' COMMENT '类型:1=期初,2=购货,3=盘盈,4=其他入库,5=组合单,6=拆卸单,7=委外加工',
  `createtime` int DEFAULT NULL COMMENT '创建时间',
  `updatetime` int DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='商品批次详情表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_mes_goods_batch_log`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_mes_goods_batch_log` (
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
  `business_type` enum('1','2','3','4','5','6','7','8','9','10','11','12','13','14.15','16') DEFAULT NULL COMMENT '业务类型:1=期初余额,2=普通采购,3=采购退回,4=普通销售,5=销售退回,6=调拨单,7=盘盈,8=其他入库,9=盘亏,10=其他出库,11=组合单,12=组合单,13=拆卸单,14=拆卸单,15=委外加工,16=验收',
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
-- Table structure for table `hou_mes_goods_batch_sale_info`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_mes_goods_batch_sale_info` (
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
-- Table structure for table `hou_mes_goods_batch_salee_info`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_mes_goods_batch_salee_info` (
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
-- Table structure for table `hou_mes_goods_batch_stock`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_mes_goods_batch_stock` (
  `id` int NOT NULL AUTO_INCREMENT,
  `admin_id` int DEFAULT NULL,
  `batch_id` int DEFAULT NULL,
  `goods_id` int NOT NULL DEFAULT '0' COMMENT '商品ID',
  `goods_code` varchar(30) DEFAULT NULL COMMENT '商品编号',
  `attribute_id` int DEFAULT '0' COMMENT '属性组合ID',
  `stores_id` int NOT NULL DEFAULT '0' COMMENT '仓库ID',
  `stock` double(15,4) unsigned NOT NULL DEFAULT '0.0000' COMMENT '库存',
  `status` enum('0','1') NOT NULL DEFAULT '1' COMMENT '状态:0=关闭,1=正常',
  `createtime` int DEFAULT NULL COMMENT '创建时间',
  `updatetime` int DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='商品批次库存表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_mes_goods_exchange`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_mes_goods_exchange` (
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
-- Table structure for table `hou_mes_goods_exchange_info`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_mes_goods_exchange_info` (
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
  `basic_nmber` double(15,4) DEFAULT '0.0000' COMMENT '基本数量',
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
-- Table structure for table `hou_mes_goods_excpur`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_mes_goods_excpur` (
  `id` int NOT NULL AUTO_INCREMENT,
  `exchange_id` int DEFAULT '0',
  `purchase_id` int DEFAULT NULL,
  `type` enum('0','1','2') DEFAULT '0' COMMENT '单据类型',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='退单关联';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_mes_goods_excpur_info`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_mes_goods_excpur_info` (
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
-- Table structure for table `hou_mes_goods_log`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_mes_goods_log` (
  `id` int NOT NULL AUTO_INCREMENT,
  `admin_id` int DEFAULT NULL,
  `purchase_code` varchar(50) DEFAULT NULL COMMENT '单据编号',
  `purchase_id` int DEFAULT NULL COMMENT '单据ID',
  `purchase_time` int DEFAULT NULL COMMENT '单据日期',
  `business_type` enum('1','2','3','4','5','6','7','8','9','10','11','12','13','14','15','16','17') NOT NULL DEFAULT '1' COMMENT '业务类型:1=期初余额,2=普通采购,3=采购退回,4=普通销售,5=销售退回,6=调拨单,7=成本调整,8=盘盈,9=其他入库,10=盘亏,11=其他出库,12=组装单,13组装单,14=拆卸单,15=拆卸单,16=委外加工,17=验收',
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
-- Table structure for table `hou_mes_goods_period`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_mes_goods_period` (
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
-- Table structure for table `hou_mes_goods_price`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_mes_goods_price` (
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
-- Table structure for table `hou_mes_goods_purchase`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_mes_goods_purchase` (
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
-- Table structure for table `hou_mes_goods_purchase_info`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_mes_goods_purchase_info` (
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
  `basic_nmber` double(15,4) DEFAULT '0.0000' COMMENT '基本数量',
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
-- Table structure for table `hou_mes_goods_purchase_order`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_mes_goods_purchase_order` (
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
-- Table structure for table `hou_mes_goods_purchase_order_info`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_mes_goods_purchase_order_info` (
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
  `basic_nmber` double(15,4) DEFAULT '0.0000' COMMENT '基本数量',
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
-- Table structure for table `hou_mes_goods_purchase_summary`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_mes_goods_purchase_summary` (
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
  `basic_nmber` double(15,4) DEFAULT '0.0000' COMMENT '基本数量',
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
-- Table structure for table `hou_mes_goods_purord`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_mes_goods_purord` (
  `id` int NOT NULL AUTO_INCREMENT,
  `order_id` int DEFAULT '0',
  `purchase_id` int DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='订单关联';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_mes_goods_purord_info`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_mes_goods_purord_info` (
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
-- Table structure for table `hou_mes_goods_serial`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_mes_goods_serial` (
  `id` int NOT NULL AUTO_INCREMENT,
  `admin_id` int DEFAULT NULL,
  `goods_id` int DEFAULT NULL COMMENT '商品ID',
  `goods_code` varchar(30) DEFAULT NULL COMMENT '商品编号',
  `stores_id` int unsigned DEFAULT NULL COMMENT '仓库ID',
  `attribute_id` varchar(255) DEFAULT NULL COMMENT '属性组合',
  `serial` varchar(30) DEFAULT NULL COMMENT '序列号',
  `status` enum('1','2','3','4','5','6','7','8','9','10','11','12','13','14','15') DEFAULT '1' COMMENT '状态:1=购货,2=购货退货,3=销货,4=销货退货,5=盘点,6=其他,7=盘盈,8=其他入库,9=盘亏,10=其他出库,11=组合单,12=组合单,13=拆卸单,14=拆卸单,15=验收',
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
-- Table structure for table `hou_mes_goods_serial_log`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_mes_goods_serial_log` (
  `id` int NOT NULL AUTO_INCREMENT,
  `admin_id` int DEFAULT NULL,
  `goods_id` int DEFAULT NULL COMMENT '商品ID',
  `stores_id` int unsigned DEFAULT NULL COMMENT '仓库ID',
  `attribute_id` int DEFAULT NULL COMMENT '属性组合',
  `serial` varchar(30) DEFAULT NULL COMMENT '序列号',
  `business_type` enum('1','2','3','4','5','6','7','8','9','10','11','12','13','14','15','16') DEFAULT NULL COMMENT '业务类型:1=期初,2=普通采购,3=采购退回,4=普通销售,5=销售退回,6=调拨单,,7=盘盈,8=其他入库,9=盘亏,10=其他出库,11=组合单,12=组合单,13=拆卸单,14=拆卸单,15=委外加工,16=验收',
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
-- Table structure for table `hou_mes_goods_stock`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_mes_goods_stock` (
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
-- Table structure for table `hou_mes_goods_stock_warning`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_mes_goods_stock_warning` (
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
-- Table structure for table `hou_mes_goods_store_info`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_mes_goods_store_info` (
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
-- Table structure for table `hou_mes_goods_type`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_mes_goods_type` (
  `id` int NOT NULL AUTO_INCREMENT,
  `admin_id` int DEFAULT NULL,
  `supplier_type_name` varchar(120) NOT NULL DEFAULT '' COMMENT '类别名称',
  `supplier_type_image` varchar(255) DEFAULT '' COMMENT '图片',
  `pid` int NOT NULL DEFAULT '0' COMMENT '上级',
  `path` varchar(255) DEFAULT NULL COMMENT 'path',
  `status` enum('0','1') NOT NULL DEFAULT '1' COMMENT '状态:0=禁用,1=正常',
  `weigh` int DEFAULT '0',
  `order_status` enum('0','1') DEFAULT '1' COMMENT '订货通显示状态:0=否,1=是',
  `createtime` int DEFAULT NULL COMMENT '创建时间',
  `updatetime` int DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='商品分类';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_mes_income`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_mes_income` (
  `id` int NOT NULL AUTO_INCREMENT,
  `admin_id` int DEFAULT NULL,
  `purchase_code` varchar(50) DEFAULT NULL COMMENT '单据编号',
  `purchase_time` int DEFAULT NULL COMMENT '单据日期',
  `business_type` enum('1','2') DEFAULT NULL COMMENT '业务类型:1=其他收入,2=其他',
  `customers_id` int DEFAULT NULL COMMENT '客户',
  `accounts_id` int DEFAULT NULL COMMENT '账户',
  `cost_id` int DEFAULT NULL COMMENT '采购销售ID',
  `money` double(15,4) DEFAULT NULL COMMENT '金额',
  `pay_money` double(15,4) DEFAULT NULL COMMENT '已收金额',
  `arrears` double(15,4) DEFAULT NULL COMMENT '本次欠款',
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='其他收入单';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_mes_income_info`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_mes_income_info` (
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
-- Table structure for table `hou_mes_inspection_item`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_mes_inspection_item` (
  `id` int NOT NULL AUTO_INCREMENT,
  `report_id` int NOT NULL COMMENT '点检报告ID',
  `item_name` varchar(255) DEFAULT NULL COMMENT '点检项目名称',
  `item_code` varchar(100) DEFAULT NULL COMMENT '点检项目编码',
  `standard_value` varchar(100) DEFAULT NULL COMMENT '标准值',
  `actual_value` varchar(100) DEFAULT NULL COMMENT '实际值',
  `result` enum('0','1') DEFAULT '0' COMMENT '结果:0=正常,1=异常',
  `remark` varchar(255) DEFAULT NULL COMMENT '备注',
  `createtime` int DEFAULT NULL COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_report_id` (`report_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='点检项目明细表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_mes_inspection_report`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_mes_inspection_report` (
  `id` int NOT NULL AUTO_INCREMENT,
  `task_id` int NOT NULL COMMENT '点检任务ID',
  `equipment_id` int DEFAULT NULL COMMENT '设备ID',
  `equipment_code` varchar(100) DEFAULT NULL COMMENT '设备编码',
  `equipment_name` varchar(255) DEFAULT NULL COMMENT '设备名称',
  `inspection_time` datetime DEFAULT NULL COMMENT '点检时间',
  `inspector` varchar(100) DEFAULT NULL COMMENT '点检人',
  `inspection_result` enum('0','1','2') DEFAULT '0' COMMENT '点检结果:0=正常,1=异常,2=故障',
  `inspection_content` text COMMENT '点检内容',
  `abnormal_description` text COMMENT '异常描述',
  `handling_suggestion` text COMMENT '处理建议',
  `handling_status` enum('0','1','2') DEFAULT '0' COMMENT '处理状态:0=未处理,1=处理中,2=已处理',
  `handler` varchar(100) DEFAULT NULL COMMENT '处理人',
  `handling_time` datetime DEFAULT NULL COMMENT '处理时间',
  `remark` varchar(255) DEFAULT NULL COMMENT '备注',
  `createtime` int DEFAULT NULL COMMENT '创建时间',
  `updatetime` int DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_task_id` (`task_id`),
  KEY `idx_equipment_id` (`equipment_id`),
  KEY `idx_inspection_time` (`inspection_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='点检报告表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_mes_inspection_task`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_mes_inspection_task` (
  `id` int NOT NULL AUTO_INCREMENT,
  `org_id` int DEFAULT NULL COMMENT '组织ID',
  `equipment_id` int DEFAULT NULL COMMENT '设备ID',
  `equipment_code` varchar(100) DEFAULT NULL COMMENT '设备编码',
  `equipment_name` varchar(255) DEFAULT NULL COMMENT '设备名称',
  `equipment_spec` varchar(255) DEFAULT NULL COMMENT '设备规格',
  `work_center` varchar(100) DEFAULT NULL COMMENT '工作中心',
  `workstation` varchar(100) DEFAULT NULL COMMENT '工位',
  `pending_quantity` int DEFAULT '0' COMMENT '待点检数量',
  `inspection_type` enum('1','2') DEFAULT '1' COMMENT '点检类型:1=日常点检,2=定期点检',
  `inspection_cycle` int DEFAULT NULL COMMENT '点检周期(天)',
  `last_inspection_time` datetime DEFAULT NULL COMMENT '上次点检时间',
  `next_inspection_time` datetime DEFAULT NULL COMMENT '下次点检时间',
  `status` enum('0','1','2') DEFAULT '0' COMMENT '状态:0=待点检,1=点检中,2=已完成',
  `createtime` int DEFAULT NULL COMMENT '创建时间',
  `updatetime` int DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_org_id` (`org_id`),
  KEY `idx_equipment_id` (`equipment_id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='点检任务表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_mes_inventory`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_mes_inventory` (
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
-- Table structure for table `hou_mes_inventory_info`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_mes_inventory_info` (
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
-- Table structure for table `hou_mes_inventory_serial`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_mes_inventory_serial` (
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
-- Table structure for table `hou_mes_inventory_serial_info`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_mes_inventory_serial_info` (
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
-- Table structure for table `hou_mes_invoicing`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_mes_invoicing` (
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
-- Table structure for table `hou_mes_invoicing_log`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_mes_invoicing_log` (
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
-- Table structure for table `hou_mes_material`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_mes_material` (
  `id` int NOT NULL AUTO_INCREMENT,
  `code_type` enum('1','2') DEFAULT '1' COMMENT '单据类型:1=加工单,2=委外加工单',
  `chase_code` varchar(50) DEFAULT NULL COMMENT '加工单单号',
  `plan_code` varchar(50) DEFAULT NULL COMMENT '生产计划单号',
  `production_code` varchar(50) DEFAULT NULL COMMENT '生产单号',
  `picking_code` varchar(50) DEFAULT NULL COMMENT '领料单单号',
  `outsourcing_code` varchar(50) DEFAULT NULL COMMENT '委外加工单单号',
  `purchase_code` varchar(50) DEFAULT NULL COMMENT '单据编号',
  `purchase_time` varchar(50) DEFAULT NULL COMMENT '单据日期',
  `delivery_time` int DEFAULT NULL COMMENT '交货时间',
  `workshop_id` int DEFAULT NULL COMMENT '车间',
  `work_id` int DEFAULT NULL COMMENT '生产工人',
  `goods_name` varchar(255) DEFAULT NULL COMMENT '商品名称',
  `goods_code` varchar(100) DEFAULT NULL COMMENT '商品编号',
  `goods_unit` int DEFAULT NULL COMMENT '商品单位',
  `default_unit` int DEFAULT NULL COMMENT '基本单位',
  `number` double(15,4) DEFAULT NULL COMMENT '数量',
  `finish_number` double(15,4) DEFAULT NULL COMMENT '完成数量',
  `basic_nmber` double(15,4) DEFAULT NULL COMMENT '基本数量',
  `unit_price` double(15,4) DEFAULT NULL COMMENT '单价',
  `all_money` float(11,2) DEFAULT NULL COMMENT '金额',
  `examine_status` enum('0','1') DEFAULT '0' COMMENT '审核状态:0=未审核,1=已审核',
  `examine_time` int DEFAULT NULL COMMENT '审核时间',
  `status` enum('0','1') DEFAULT '0' COMMENT '状态:0=未完成,1=已完成',
  `remark` varchar(255) DEFAULT NULL COMMENT '备注',
  `preparation_id` int DEFAULT NULL COMMENT '制单人',
  `examine_id` int DEFAULT NULL COMMENT '审核人',
  `operation_id` int DEFAULT NULL COMMENT '最后操作人',
  `mrp_status` enum('0','1') DEFAULT '0' COMMENT 'mrp状态',
  `work_time` int DEFAULT NULL COMMENT '开工时间',
  `finish_time` int DEFAULT NULL COMMENT '完工时间',
  `working_id` int DEFAULT NULL COMMENT '工序ID',
  `createtime` int DEFAULT NULL COMMENT '创建时间',
  `updatetime` int DEFAULT NULL COMMENT '更新时间',
  `org_id` varchar(64) NOT NULL DEFAULT '1',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `purchase_code` (`purchase_code`) USING BTREE,
  KEY `idx_mes_material_org` (`org_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='生产退料单';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_mes_material_info`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_mes_material_info` (
  `id` int NOT NULL AUTO_INCREMENT,
  `type` enum('1','2') DEFAULT '1' COMMENT '类型:1=原材料,2=商品',
  `production_id` int DEFAULT NULL COMMENT '生产ID',
  `goods_id` int DEFAULT NULL COMMENT '商品ID',
  `goods_code` varchar(255) DEFAULT NULL COMMENT '商品编号',
  `goods_name` varchar(255) DEFAULT NULL COMMENT '商品名称',
  `goods_note` varchar(255) DEFAULT NULL COMMENT '商品规格型号',
  `unit_id` int DEFAULT NULL COMMENT '单位ID',
  `default_unit` int DEFAULT NULL COMMENT '默认单位',
  `purchase` double(15,4) DEFAULT '0.0000' COMMENT '进价',
  `number` double(15,4) DEFAULT '0.0000' COMMENT '数量',
  `basic_nmber` double(15,4) DEFAULT NULL COMMENT '基本数量',
  `money` double(15,4) DEFAULT NULL COMMENT '金额',
  `remark` text COMMENT '备注',
  `stock_number` double(15,4) DEFAULT '0.0000' COMMENT '库存总数量',
  `need_number` double(15,4) DEFAULT '0.0000' COMMENT '需求数量',
  `requisition_number` double(15,4) DEFAULT '0.0000' COMMENT '领用数量',
  `use_number` double(15,4) DEFAULT '0.0000' COMMENT '使用数量',
  `back_number` double(15,4) DEFAULT '0.0000' COMMENT '退料数量',
  `stores_id` int DEFAULT NULL COMMENT '仓库',
  `allow_stock` double(15,4) DEFAULT '0.0000' COMMENT '可用库存',
  `goods_attr` int DEFAULT NULL COMMENT '商品属性',
  `batch_id` int DEFAULT NULL COMMENT '批次ID',
  `batch_name` varchar(100) DEFAULT NULL COMMENT '批次名称',
  `manufacture` int DEFAULT NULL COMMENT '生产日期',
  `guarantee` int DEFAULT NULL COMMENT '保质期',
  `validity` int DEFAULT NULL COMMENT '到期时间',
  `period` text COMMENT '序列号',
  `createtime` int DEFAULT NULL COMMENT '创建时间',
  `updatetime` int DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='退料单详情';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_mes_material_loading_record`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_mes_material_loading_record` (
  `id` int NOT NULL AUTO_INCREMENT,
  `report_id` int NOT NULL COMMENT '报工ID',
  `material_code` varchar(100) DEFAULT NULL COMMENT '物料编码',
  `material_name` varchar(255) DEFAULT NULL COMMENT '物料名称',
  `loading_quantity` double(15,4) DEFAULT '0.0000' COMMENT '上料数量',
  `loading_time` datetime DEFAULT NULL COMMENT '上料时间',
  `operator` varchar(100) DEFAULT NULL COMMENT '操作员',
  `createtime` int DEFAULT NULL COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_report_id` (`report_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='上料记录表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_mes_material_quota`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_mes_material_quota` (
  `id` int NOT NULL AUTO_INCREMENT,
  `report_id` int NOT NULL COMMENT '报工ID',
  `material_code` varchar(100) DEFAULT NULL COMMENT '物料编码',
  `material_name` varchar(255) DEFAULT NULL COMMENT '物料名称',
  `material_spec` varchar(255) DEFAULT NULL COMMENT '物料规格',
  `material_alias` varchar(100) DEFAULT NULL COMMENT '物料代号',
  `basic_usage_numerator` double(15,4) DEFAULT '0.0000' COMMENT '基本用量(分子)',
  `basic_usage_denominator` double(15,4) DEFAULT '0.0000' COMMENT '基础用量(分母)',
  `sub_item_loss` decimal(5,2) DEFAULT '0.00' COMMENT '子件损耗(%)',
  `is_material_loaded` enum('0','1') DEFAULT '0' COMMENT '是否上料:0=否,1=是',
  `supplier` varchar(100) DEFAULT NULL COMMENT '供',
  `createtime` int DEFAULT NULL COMMENT '创建时间',
  `updatetime` int DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_report_id` (`report_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='用料定额表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_mes_material_return_record`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_mes_material_return_record` (
  `id` int NOT NULL AUTO_INCREMENT,
  `report_id` int NOT NULL COMMENT '报工ID',
  `material_code` varchar(100) DEFAULT NULL COMMENT '物料编码',
  `material_name` varchar(255) DEFAULT NULL COMMENT '物料名称',
  `return_quantity` double(15,4) DEFAULT '0.0000' COMMENT '退料数量',
  `return_time` datetime DEFAULT NULL COMMENT '退料时间',
  `operator` varchar(100) DEFAULT NULL COMMENT '操作员',
  `createtime` int DEFAULT NULL COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_report_id` (`report_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='退料记录表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_mes_mrp`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_mes_mrp` (
  `id` int NOT NULL AUTO_INCREMENT,
  `purchase_code` varchar(50) DEFAULT NULL COMMENT '生产计划单号',
  `purchase_time` varchar(50) DEFAULT NULL COMMENT '单据日期',
  `delivery_time` int DEFAULT NULL COMMENT '交货时间',
  `workshop_id` int DEFAULT NULL COMMENT '车间',
  `goods_name` varchar(255) DEFAULT NULL COMMENT '商品名称',
  `goods_code` varchar(100) DEFAULT NULL COMMENT '商品编号',
  `goods_unit` int DEFAULT NULL COMMENT '商品单位',
  `default_unit` int DEFAULT NULL COMMENT '基本单位',
  `number` double(15,4) DEFAULT NULL COMMENT '数量',
  `basic_nmber` double(15,4) DEFAULT NULL COMMENT '基本数量',
  `unit_price` double(15,4) DEFAULT NULL COMMENT '单价',
  `all_money` float(11,2) DEFAULT NULL COMMENT '金额',
  `examine_status` enum('0','1') DEFAULT '0' COMMENT '审核状态:0=未审核,1=已审核',
  `examine_time` int DEFAULT NULL COMMENT '审核时间',
  `status` enum('0','1') DEFAULT '0' COMMENT '状态:0=未完成,1=已完成',
  `remark` varchar(255) DEFAULT NULL COMMENT '备注',
  `preparation_id` int DEFAULT NULL COMMENT '制单人',
  `examine_id` int DEFAULT NULL COMMENT '审核人',
  `operation_id` int DEFAULT NULL COMMENT '最后操作人',
  `mrp_status` enum('0','1') DEFAULT '0' COMMENT 'mrp状态',
  `createtime` int DEFAULT NULL COMMENT '创建时间',
  `updatetime` int DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `purchase_code` (`purchase_code`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='mrp计算';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_mes_mrp_info`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_mes_mrp_info` (
  `id` int NOT NULL AUTO_INCREMENT,
  `plan_id` int DEFAULT NULL COMMENT '计划ID',
  `goods_id` int DEFAULT NULL COMMENT '商品ID',
  `goods_code` varchar(255) DEFAULT NULL COMMENT '商品编号',
  `goods_name` varchar(255) DEFAULT NULL COMMENT '商品名称',
  `goods_note` varchar(255) DEFAULT NULL COMMENT '商品规格型号',
  `unit_id` int DEFAULT NULL COMMENT '单位ID',
  `default_unit` int DEFAULT NULL COMMENT '默认单位',
  `purchase` double(15,4) DEFAULT '0.0000' COMMENT '进价',
  `number` double(15,4) DEFAULT '0.0000' COMMENT '数量',
  `basic_nmber` double(15,4) DEFAULT NULL COMMENT '基本数量',
  `stock` double(15,4) DEFAULT NULL COMMENT '库存数',
  `buy_number` double(15,4) DEFAULT NULL COMMENT '缺口数',
  `money` double(15,4) DEFAULT NULL COMMENT '金额',
  `remark` text COMMENT '备注',
  `createtime` int DEFAULT NULL COMMENT '创建时间',
  `updatetime` int DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `purchase_code` (`goods_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='mrp详情';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_mes_outsourcing`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_mes_outsourcing` (
  `id` int NOT NULL AUTO_INCREMENT,
  `plan_code` varchar(50) DEFAULT NULL COMMENT '生产计划单号',
  `production_code` varchar(50) DEFAULT NULL COMMENT '生产单号',
  `purchase_code` varchar(50) DEFAULT NULL COMMENT '单据编号',
  `purchase_time` varchar(50) DEFAULT NULL COMMENT '单据日期',
  `delivery_time` int DEFAULT NULL COMMENT '交货时间',
  `workshop_id` int DEFAULT NULL COMMENT '车间',
  `work_id` int DEFAULT NULL COMMENT '生产工人',
  `goods_name` varchar(255) DEFAULT NULL COMMENT '商品名称',
  `goods_code` varchar(100) DEFAULT NULL COMMENT '商品编号',
  `goods_unit` int DEFAULT NULL COMMENT '商品单位',
  `default_unit` int DEFAULT NULL COMMENT '基本单位',
  `number` double(15,4) DEFAULT NULL COMMENT '数量',
  `finish_number` double(15,4) DEFAULT NULL COMMENT '完成数量',
  `basic_nmber` double(15,4) DEFAULT NULL COMMENT '基本数量',
  `unit_price` double(15,4) DEFAULT NULL COMMENT '单价',
  `all_money` float(11,2) DEFAULT NULL COMMENT '金额',
  `examine_status` enum('0','1') DEFAULT '0' COMMENT '审核状态:0=未审核,1=已审核',
  `examine_time` int DEFAULT NULL COMMENT '审核时间',
  `status` enum('0','1') DEFAULT '0' COMMENT '状态:0=未完成,1=已完成',
  `remark` varchar(255) DEFAULT NULL COMMENT '备注',
  `preparation_id` int DEFAULT NULL COMMENT '制单人',
  `examine_id` int DEFAULT NULL COMMENT '审核人',
  `operation_id` int DEFAULT NULL COMMENT '最后操作人',
  `mrp_status` enum('0','1') DEFAULT '0' COMMENT 'mrp状态',
  `work_time` int DEFAULT NULL COMMENT '开工时间',
  `finish_time` int DEFAULT NULL COMMENT '完工时间',
  `customers_id` int DEFAULT NULL COMMENT '工厂ID',
  `free_money` double(15,4) DEFAULT '0.0000' COMMENT '委外费用',
  `createtime` int DEFAULT NULL COMMENT '创建时间',
  `updatetime` int DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `purchase_code` (`purchase_code`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='委外加工单';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_mes_outsourcing_info`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_mes_outsourcing_info` (
  `id` int NOT NULL AUTO_INCREMENT,
  `type` enum('1','2') DEFAULT '1' COMMENT '类型:1=原材料,2=商品',
  `production_id` int DEFAULT NULL COMMENT '生产ID',
  `goods_id` int DEFAULT NULL COMMENT '商品ID',
  `goods_code` varchar(255) DEFAULT NULL COMMENT '商品编号',
  `goods_name` varchar(255) DEFAULT NULL COMMENT '商品名称',
  `goods_note` varchar(255) DEFAULT NULL COMMENT '商品规格型号',
  `unit_id` int DEFAULT NULL COMMENT '单位ID',
  `default_unit` int DEFAULT NULL COMMENT '默认单位',
  `purchase` double(15,4) DEFAULT '0.0000' COMMENT '进价',
  `number` double(15,4) DEFAULT '0.0000' COMMENT '数量',
  `basic_nmber` double(15,4) DEFAULT NULL COMMENT '基本数量',
  `money` double(15,4) DEFAULT NULL COMMENT '金额',
  `remark` text COMMENT '备注',
  `stock_number` double(15,4) DEFAULT '0.0000' COMMENT '库存总数量',
  `need_number` double(15,4) DEFAULT '0.0000' COMMENT '需求数量',
  `requisition_number` double(15,4) DEFAULT '0.0000' COMMENT '领用数量',
  `use_number` double(15,4) DEFAULT '0.0000' COMMENT '使用数量',
  `back_number` double(15,4) DEFAULT '0.0000' COMMENT '退料数量',
  `stores_id` int DEFAULT NULL COMMENT '仓库',
  `allow_stock` double(15,4) DEFAULT '0.0000' COMMENT '可用库存',
  `goods_attr` int DEFAULT NULL COMMENT '商品属性',
  `batch_id` int DEFAULT NULL COMMENT '批次ID',
  `batch_name` varchar(100) DEFAULT NULL COMMENT '批次名称',
  `manufacture` int DEFAULT NULL COMMENT '生产日期',
  `guarantee` int DEFAULT NULL COMMENT '保质期',
  `validity` int DEFAULT NULL COMMENT '到期时间',
  `period` text COMMENT '序列号',
  `createtime` int DEFAULT NULL COMMENT '创建时间',
  `updatetime` int DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='委外加工单详情';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_mes_payment`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_mes_payment` (
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
-- Table structure for table `hou_mes_payment_account`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_mes_payment_account` (
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
-- Table structure for table `hou_mes_payment_log`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_mes_payment_log` (
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
-- Table structure for table `hou_mes_payment_sale`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_mes_payment_sale` (
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
-- Table structure for table `hou_mes_picking`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_mes_picking` (
  `id` int NOT NULL AUTO_INCREMENT,
  `plan_code` varchar(50) DEFAULT NULL COMMENT '生产计划单号',
  `production_code` varchar(50) DEFAULT NULL COMMENT '生产单号',
  `purchase_code` varchar(50) DEFAULT NULL COMMENT '单据编号',
  `purchase_time` varchar(50) DEFAULT NULL COMMENT '单据日期',
  `delivery_time` int DEFAULT NULL COMMENT '交货时间',
  `workshop_id` int DEFAULT NULL COMMENT '车间',
  `work_id` int DEFAULT NULL COMMENT '生产工人',
  `goods_name` varchar(255) DEFAULT NULL COMMENT '商品名称',
  `goods_code` varchar(100) DEFAULT NULL COMMENT '商品编号',
  `goods_unit` int DEFAULT NULL COMMENT '商品单位',
  `default_unit` int DEFAULT NULL COMMENT '基本单位',
  `number` double(15,4) DEFAULT NULL COMMENT '数量',
  `finish_number` double(15,4) DEFAULT NULL COMMENT '完成数量',
  `basic_nmber` double(15,4) DEFAULT NULL COMMENT '基本数量',
  `unit_price` double(15,4) DEFAULT NULL COMMENT '单价',
  `all_money` float(11,2) DEFAULT NULL COMMENT '金额',
  `examine_status` enum('0','1') DEFAULT '0' COMMENT '审核状态:0=未审核,1=已审核',
  `examine_time` int DEFAULT NULL COMMENT '审核时间',
  `status` enum('0','1') DEFAULT '0' COMMENT '状态:0=未完成,1=已完成',
  `remark` varchar(255) DEFAULT NULL COMMENT '备注',
  `preparation_id` int DEFAULT NULL COMMENT '制单人',
  `examine_id` int DEFAULT NULL COMMENT '审核人',
  `operation_id` int DEFAULT NULL COMMENT '最后操作人',
  `mrp_status` enum('0','1') DEFAULT '0' COMMENT 'mrp状态',
  `work_time` int DEFAULT NULL COMMENT '开工时间',
  `finish_time` int DEFAULT NULL COMMENT '完工时间',
  `createtime` int DEFAULT NULL COMMENT '创建时间',
  `updatetime` int DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `purchase_code` (`purchase_code`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='生产领料单';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_mes_picking_info`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_mes_picking_info` (
  `id` int NOT NULL AUTO_INCREMENT,
  `type` enum('1','2') DEFAULT '1' COMMENT '类型:1=原材料,2=商品',
  `production_id` int DEFAULT NULL COMMENT '生产ID',
  `goods_id` int DEFAULT NULL COMMENT '商品ID',
  `goods_code` varchar(255) DEFAULT NULL COMMENT '商品编号',
  `goods_name` varchar(255) DEFAULT NULL COMMENT '商品名称',
  `goods_note` varchar(255) DEFAULT NULL COMMENT '商品规格型号',
  `unit_id` int DEFAULT NULL COMMENT '单位ID',
  `default_unit` int DEFAULT NULL COMMENT '默认单位',
  `purchase` double(15,4) DEFAULT '0.0000' COMMENT '进价',
  `number` double(15,4) DEFAULT '0.0000' COMMENT '数量',
  `basic_nmber` double(15,4) DEFAULT NULL COMMENT '基本数量',
  `money` double(15,4) DEFAULT NULL COMMENT '金额',
  `remark` text COMMENT '备注',
  `stock_number` double(15,4) DEFAULT '0.0000' COMMENT '库存总数量',
  `need_number` double(15,4) DEFAULT '0.0000' COMMENT '需求数量',
  `requisition_number` double(15,4) DEFAULT '0.0000' COMMENT '领用数量',
  `use_number` double(15,4) DEFAULT '0.0000' COMMENT '使用数量',
  `back_number` double(15,4) DEFAULT '0.0000' COMMENT '退料数量',
  `stores_id` int DEFAULT NULL COMMENT '仓库',
  `allow_stock` double(15,4) DEFAULT '0.0000' COMMENT '可用库存',
  `goods_attr` int DEFAULT NULL COMMENT '商品属性',
  `batch_id` int DEFAULT NULL COMMENT '批次ID',
  `batch_name` varchar(100) DEFAULT NULL COMMENT '批次名称',
  `manufacture` int DEFAULT NULL COMMENT '生产日期',
  `guarantee` int DEFAULT NULL COMMENT '保质期',
  `validity` int DEFAULT NULL COMMENT '到期时间',
  `period` text COMMENT '序列号',
  `createtime` int DEFAULT NULL COMMENT '创建时间',
  `updatetime` int DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='领料单详情';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_mes_plan`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_mes_plan` (
  `id` int NOT NULL AUTO_INCREMENT,
  `purchase_code` varchar(50) DEFAULT NULL COMMENT '生产计划单号',
  `purchase_time` varchar(50) DEFAULT NULL COMMENT '单据日期',
  `delivery_time` int DEFAULT NULL COMMENT '交货时间',
  `workshop_id` int DEFAULT NULL COMMENT '车间',
  `goods_name` varchar(255) DEFAULT NULL COMMENT '商品名称',
  `goods_code` varchar(100) DEFAULT NULL COMMENT '商品编号',
  `goods_unit` int DEFAULT NULL COMMENT '商品单位',
  `default_unit` int DEFAULT NULL COMMENT '基本单位',
  `number` double(15,4) DEFAULT NULL COMMENT '数量',
  `basic_nmber` double(15,4) DEFAULT NULL COMMENT '基本数量',
  `unit_price` double(15,4) DEFAULT NULL COMMENT '单价',
  `all_money` float(11,2) DEFAULT NULL COMMENT '金额',
  `examine_status` enum('0','1') DEFAULT '0' COMMENT '审核状态:0=未审核,1=已审核',
  `examine_time` int DEFAULT NULL COMMENT '审核时间',
  `status` enum('0','1') DEFAULT '0' COMMENT '状态:0=未完成,1=已完成',
  `remark` varchar(255) DEFAULT NULL COMMENT '备注',
  `preparation_id` int DEFAULT NULL COMMENT '制单人',
  `examine_id` int DEFAULT NULL COMMENT '审核人',
  `operation_id` int DEFAULT NULL COMMENT '最后操作人',
  `mrp_status` enum('0','1') DEFAULT '0' COMMENT 'mrp状态',
  `createtime` int DEFAULT NULL COMMENT '创建时间',
  `updatetime` int DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `purchase_code` (`purchase_code`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='生产计划';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_mes_plan_info`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_mes_plan_info` (
  `id` int NOT NULL AUTO_INCREMENT,
  `plan_id` int DEFAULT NULL COMMENT '计划ID',
  `goods_id` int DEFAULT NULL COMMENT '商品ID',
  `goods_code` varchar(255) DEFAULT NULL COMMENT '商品编号',
  `goods_name` varchar(255) DEFAULT NULL COMMENT '商品名称',
  `goods_note` varchar(255) DEFAULT NULL COMMENT '商品规格型号',
  `unit_id` int DEFAULT NULL COMMENT '单位ID',
  `default_unit` int DEFAULT NULL COMMENT '默认单位',
  `purchase` double(15,4) DEFAULT '0.0000' COMMENT '进价',
  `number` double(15,4) DEFAULT '0.0000' COMMENT '数量',
  `basic_nmber` double(15,4) DEFAULT NULL COMMENT '基本数量',
  `money` double(15,4) DEFAULT NULL COMMENT '金额',
  `remark` text COMMENT '备注',
  `createtime` int DEFAULT NULL COMMENT '创建时间',
  `updatetime` int DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `purchase_code` (`goods_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='计划详情';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_mes_prefix`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_mes_prefix` (
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
-- Table structure for table `hou_mes_process_route`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_mes_process_route` (
  `id` int NOT NULL AUTO_INCREMENT,
  `org_id` int DEFAULT NULL COMMENT '组织ID',
  `material_category` varchar(100) DEFAULT NULL COMMENT '物料分类',
  `category_code` varchar(100) DEFAULT NULL COMMENT '分类编码',
  `version_no` varchar(50) DEFAULT NULL COMMENT '版本号',
  `process_code` varchar(100) DEFAULT NULL COMMENT '工艺编号',
  `product_name` varchar(255) DEFAULT NULL COMMENT '产品名称',
  `product_code` varchar(100) DEFAULT NULL COMMENT '产品编号',
  `process_count` int DEFAULT '0' COMMENT '工序数量',
  `standard_work_time` decimal(10,2) DEFAULT '0.00' COMMENT '标准工时(小时)',
  `creator` varchar(100) DEFAULT NULL COMMENT '创建人',
  `status` enum('0','1','2','3') DEFAULT '0' COMMENT '状态:0=草稿,1=已审核,2=已关闭,3=已还原',
  `createtime` int DEFAULT NULL COMMENT '创建时间',
  `updatetime` int DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_org_id` (`org_id`),
  KEY `idx_category_code` (`category_code`),
  KEY `idx_process_code` (`process_code`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='工艺路线主表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_mes_process_route_detail`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_mes_process_route_detail` (
  `id` int NOT NULL AUTO_INCREMENT,
  `route_id` int NOT NULL COMMENT '工艺路线ID',
  `process_line_no` varchar(50) DEFAULT NULL COMMENT '工序行号',
  `process_code` varchar(100) DEFAULT NULL COMMENT '工序编码',
  `process_name` varchar(255) DEFAULT NULL COMMENT '工序名称',
  `work_center` varchar(100) DEFAULT NULL COMMENT '工作中心',
  `workstation` varchar(100) DEFAULT NULL COMMENT '工位',
  `standard_time` decimal(10,2) DEFAULT '0.00' COMMENT '标准工时(小时)',
  `setup_time` decimal(10,2) DEFAULT '0.00' COMMENT '准备时间(小时)',
  `process_order` int DEFAULT '0' COMMENT '工序顺序',
  `is_first_process` enum('0','1') DEFAULT '0' COMMENT '是否首道工序:0=否,1=是',
  `is_last_process` enum('0','1') DEFAULT '0' COMMENT '是否末道工序:0=否,1=是',
  `is_required` enum('0','1') DEFAULT '1' COMMENT '是否必做:0=否,1=是',
  `remark` varchar(255) DEFAULT NULL COMMENT '备注',
  `createtime` int DEFAULT NULL COMMENT '创建时间',
  `updatetime` int DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_route_id` (`route_id`),
  KEY `idx_process_code` (`process_code`),
  KEY `idx_process_order` (`process_order`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='工艺路线工序明细表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_mes_production`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_mes_production` (
  `id` int NOT NULL AUTO_INCREMENT,
  `org_id` int DEFAULT NULL COMMENT '组织ID',
  `dept_id` int DEFAULT NULL COMMENT '部门ID',
  `task_level` varchar(50) DEFAULT NULL COMMENT '任务等级',
  `task_category` varchar(50) DEFAULT NULL COMMENT '任务类别',
  `production_batch` varchar(100) DEFAULT NULL COMMENT '生产批次',
  `is_transfer_workshop` enum('0','1') DEFAULT '0' COMMENT '是否转车间:0=否,1=是',
  `is_frozen` enum('0','1') DEFAULT '0' COMMENT '是否冻结:0=否,1=是',
  `is_scrap` enum('0','1') DEFAULT '0' COMMENT '是否废单:0=否,1=是',
  `is_material_received` enum('0','1') DEFAULT '0' COMMENT '是否领料:0=否,1=是',
  `bom_version` varchar(50) DEFAULT NULL COMMENT 'BOM版本',
  `bom_version_desc` varchar(255) DEFAULT NULL COMMENT 'BOM版本说明',
  `process_route_version` varchar(50) DEFAULT NULL COMMENT '工艺路线版本',
  `process_route_version_desc` varchar(255) DEFAULT NULL COMMENT '工艺路线版本说明',
  `plan_code` varchar(50) DEFAULT NULL COMMENT '生产计划单号',
  `purchase_code` varchar(50) DEFAULT NULL COMMENT '单据编号',
  `purchase_time` varchar(50) DEFAULT NULL COMMENT '单据日期',
  `delivery_time` int DEFAULT NULL COMMENT '交货时间',
  `workshop_id` int DEFAULT NULL COMMENT '车间',
  `work_id` int DEFAULT NULL COMMENT '生产工人',
  `goods_name` varchar(255) DEFAULT NULL COMMENT '商品名称',
  `goods_code` varchar(100) DEFAULT NULL COMMENT '商品编号',
  `goods_unit` int DEFAULT NULL COMMENT '商品单位',
  `default_unit` int DEFAULT NULL COMMENT '基本单位',
  `number` double(15,4) DEFAULT NULL COMMENT '数量',
  `finish_number` double(15,4) DEFAULT NULL COMMENT '完成数量',
  `basic_nmber` double(15,4) DEFAULT NULL COMMENT '基本数量',
  `unit_price` double(15,4) DEFAULT NULL COMMENT '单价',
  `all_money` float(11,2) DEFAULT NULL COMMENT '金额',
  `examine_status` enum('0','1') DEFAULT '0' COMMENT '审核状态:0=未审核,1=已审核',
  `examine_time` int DEFAULT NULL COMMENT '审核时间',
  `status` enum('0','1') DEFAULT '0' COMMENT '状态:0=未完成,1=已完成',
  `remark` varchar(255) DEFAULT NULL COMMENT '备注',
  `preparation_id` int DEFAULT NULL COMMENT '制单人',
  `examine_id` int DEFAULT NULL COMMENT '审核人',
  `operation_id` int DEFAULT NULL COMMENT '最后操作人',
  `mrp_status` enum('0','1') DEFAULT '0' COMMENT 'mrp状态',
  `work_time` int DEFAULT NULL COMMENT '开工时间',
  `finish_time` int DEFAULT NULL COMMENT '完工时间',
  `createtime` int DEFAULT NULL COMMENT '创建时间',
  `updatetime` int DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `purchase_code` (`purchase_code`) USING BTREE,
  KEY `idx_org_id` (`org_id`),
  KEY `idx_dept_id` (`dept_id`),
  KEY `idx_production_batch` (`production_batch`),
  KEY `idx_bom_version` (`bom_version`),
  KEY `idx_process_route_version` (`process_route_version`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='生产单';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_mes_production_info`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_mes_production_info` (
  `id` int NOT NULL AUTO_INCREMENT,
  `type` enum('1','2') DEFAULT '1' COMMENT '类型:1=原材料,2=商品',
  `production_id` int DEFAULT NULL COMMENT '生产ID',
  `goods_id` int DEFAULT NULL COMMENT '商品ID',
  `goods_code` varchar(255) DEFAULT NULL COMMENT '商品编号',
  `goods_name` varchar(255) DEFAULT NULL COMMENT '商品名称',
  `goods_note` varchar(255) DEFAULT NULL COMMENT '商品规格型号',
  `unit_id` int DEFAULT NULL COMMENT '单位ID',
  `default_unit` int DEFAULT NULL COMMENT '默认单位',
  `purchase` double(15,4) DEFAULT '0.0000' COMMENT '进价',
  `number` double(15,4) DEFAULT '0.0000' COMMENT '数量',
  `basic_nmber` double(15,4) DEFAULT NULL COMMENT '基本数量',
  `money` double(15,4) DEFAULT NULL COMMENT '金额',
  `remark` text COMMENT '备注',
  `stock_number` double(15,4) DEFAULT '0.0000' COMMENT '库存总数量',
  `need_number` double(15,4) DEFAULT '0.0000' COMMENT '需求数量',
  `requisition_number` double(15,4) DEFAULT '0.0000' COMMENT '领用数量',
  `use_number` double(15,4) DEFAULT '0.0000' COMMENT '使用数量',
  `back_number` double(15,4) DEFAULT '0.0000' COMMENT '退料数量',
  `createtime` int DEFAULT NULL COMMENT '创建时间',
  `updatetime` int DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='生产单详情';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_mes_report_record`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_mes_report_record` (
  `id` int NOT NULL AUTO_INCREMENT,
  `report_id` int NOT NULL COMMENT '报工ID',
  `report_time` datetime DEFAULT NULL COMMENT '报工时间',
  `reported_quantity` double(15,4) DEFAULT '0.0000' COMMENT '报工数量',
  `qualified_quantity` double(15,4) DEFAULT '0.0000' COMMENT '合格数量',
  `unqualified_quantity` double(15,4) DEFAULT '0.0000' COMMENT '不合格数量',
  `operator` varchar(100) DEFAULT NULL COMMENT '操作员',
  `remark` varchar(255) DEFAULT NULL COMMENT '备注',
  `createtime` int DEFAULT NULL COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_report_id` (`report_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='报工记录表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_mes_sales_invoicing`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_mes_sales_invoicing` (
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
-- Table structure for table `hou_mes_sales_invoicing_log`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_mes_sales_invoicing_log` (
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
-- Table structure for table `hou_mes_sales_payment_log`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_mes_sales_payment_log` (
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
-- Table structure for table `hou_mes_sets`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_mes_sets` (
  `id` int NOT NULL AUTO_INCREMENT,
  `admin_id` int DEFAULT NULL,
  `type` tinyint DEFAULT '1' COMMENT '类型:1=基础参数,2=功能参数',
  `name` varchar(100) DEFAULT NULL COMMENT '变量名称',
  `nickname` varchar(100) DEFAULT NULL COMMENT '名称',
  `value` text COMMENT '设置值',
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
-- Table structure for table `hou_mes_settlementtype`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_mes_settlementtype` (
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
-- Table structure for table `hou_mes_spendingtype`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_mes_spendingtype` (
  `id` int NOT NULL,
  `spending_type_name` varchar(120) NOT NULL DEFAULT '' COMMENT '类型名称',
  `spending_type_image` varchar(255) DEFAULT '' COMMENT '图片',
  `status` enum('0','1') DEFAULT '1' COMMENT '状态:0=禁用,1=启用',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='支出类别表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_mes_split`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_mes_split` (
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
-- Table structure for table `hou_mes_split_info`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_mes_split_info` (
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
-- Table structure for table `hou_mes_split_info_temp`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_mes_split_info_temp` (
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
-- Table structure for table `hou_mes_split_temp`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_mes_split_temp` (
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
-- Table structure for table `hou_mes_staff_member`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_mes_staff_member` (
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
-- Table structure for table `hou_mes_stores`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_mes_stores` (
  `id` int NOT NULL AUTO_INCREMENT,
  `admin_id` int DEFAULT NULL,
  `store_code` varchar(30) NOT NULL DEFAULT '' COMMENT '仓库编号',
  `name` varchar(255) NOT NULL DEFAULT '' COMMENT '仓库名称',
  `status` enum('0','1') DEFAULT '1' COMMENT '状态:0=关闭,1=正常',
  `createtime` int DEFAULT NULL COMMENT '创建时间',
  `updatetime` int DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='仓库表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_mes_suppliers`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_mes_suppliers` (
  `id` int NOT NULL AUTO_INCREMENT,
  `admin_id` int DEFAULT NULL,
  `supplyer_code` varchar(30) NOT NULL DEFAULT '' COMMENT '供应商编号',
  `supplyertype_id` int NOT NULL DEFAULT '0' COMMENT '供应商类别',
  `supplyer_name` varchar(120) NOT NULL COMMENT '供应商名称',
  `first_connect_name` varchar(100) NOT NULL DEFAULT '' COMMENT '联系人',
  `first_connect_mobile` varchar(11) NOT NULL COMMENT '手机',
  `first_connect_phone` varchar(20) DEFAULT '' COMMENT '座机',
  `qq_wx_email` varchar(120) DEFAULT '' COMMENT 'QQ/微信/Email',
  `connect_address` varchar(255) DEFAULT '' COMMENT '联系地址',
  `note` varchar(255) DEFAULT '' COMMENT '备注',
  `need_give_account` float(11,2) NOT NULL DEFAULT '0.00' COMMENT '应付款余额',
  `receive_account` double(15,4) DEFAULT NULL COMMENT '期初应付款',
  `account_time` int unsigned DEFAULT NULL COMMENT '余额日期',
  `opening_payable` double(15,4) unsigned DEFAULT '0.0000' COMMENT '期初应付款',
  `opening_prepayment` double(15,4) unsigned DEFAULT '0.0000' COMMENT '期初预付款',
  `vat_rate` double(15,4) unsigned DEFAULT NULL COMMENT '增值税税率',
  `identification` varchar(50) DEFAULT NULL COMMENT '纳税人识别号',
  `deposit_bank` varchar(150) DEFAULT NULL COMMENT '开户银行',
  `bank_number` varchar(30) DEFAULT NULL COMMENT '银行卡号',
  `status` enum('0','1') NOT NULL DEFAULT '1' COMMENT '状态:0=禁用,1=正常',
  `createtime` int DEFAULT NULL COMMENT '创建时间',
  `updatetime` int DEFAULT NULL COMMENT '更新时间',
  `org_id` varchar(64) NOT NULL DEFAULT '1',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_mes_suppliers_org` (`org_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='供应商表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_mes_suppliers_user`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_mes_suppliers_user` (
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
-- Table structure for table `hou_mes_supplyertype`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_mes_supplyertype` (
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
-- Table structure for table `hou_mes_transfers`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_mes_transfers` (
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
-- Table structure for table `hou_mes_unit`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_mes_unit` (
  `id` int NOT NULL AUTO_INCREMENT,
  `admin_id` int DEFAULT NULL,
  `name` varchar(100) DEFAULT NULL COMMENT '单位名称',
  `type` enum('1','2') DEFAULT '1' COMMENT '类型:1=单一单位,2=多单位',
  `is_main` enum('0','1') DEFAULT '0' COMMENT '是否主单位:0=否,1=是',
  `createtime` int DEFAULT NULL COMMENT '创建时间',
  `updatetime` int DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='计量单位';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_mes_unit_conversion`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_mes_unit_conversion` (
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
-- Table structure for table `hou_mes_warehouse`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_mes_warehouse` (
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
  `org_id` varchar(64) NOT NULL DEFAULT '1',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_mes_warehouse_org` (`org_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='其他出库单';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_mes_warehouse_info`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_mes_warehouse_info` (
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
-- Table structure for table `hou_mes_warehousing`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_mes_warehousing` (
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
-- Table structure for table `hou_mes_warehousing_info`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_mes_warehousing_info` (
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
-- Table structure for table `hou_mes_workshop`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_mes_workshop` (
  `id` int NOT NULL AUTO_INCREMENT,
  `code` varchar(30) NOT NULL DEFAULT '' COMMENT '车间编号',
  `name` varchar(255) NOT NULL DEFAULT '' COMMENT '仓库名称',
  `status` enum('0','1') DEFAULT '1' COMMENT '状态:0=关闭,1=正常',
  `createtime` int DEFAULT NULL COMMENT '创建时间',
  `updatetime` int DEFAULT NULL COMMENT '更新时间',
  `org_id` varchar(64) NOT NULL DEFAULT '1',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_mes_workshop_org` (`org_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='车间表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_mes_workshop_plan`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_mes_workshop_plan` (
  `id` int NOT NULL AUTO_INCREMENT,
  `org_id` int DEFAULT NULL COMMENT '组织ID',
  `production_task` varchar(100) DEFAULT NULL COMMENT '生产任务',
  `production_order` varchar(100) DEFAULT NULL COMMENT '生产工单',
  `line_no` varchar(50) DEFAULT NULL COMMENT '行号',
  `material_code` varchar(100) DEFAULT NULL COMMENT '物料编码',
  `material_name` varchar(255) DEFAULT NULL COMMENT '物料名称',
  `material_spec` varchar(255) DEFAULT NULL COMMENT '物料规格',
  `material_alias` varchar(100) DEFAULT NULL COMMENT '物料代号',
  `production_dept` varchar(100) DEFAULT NULL COMMENT '生产部门',
  `start_date` date DEFAULT NULL COMMENT '开工日期',
  `completion_date` date DEFAULT NULL COMMENT '完工日期',
  `status` enum('0','1','2') DEFAULT '0' COMMENT '状态:0=未开始,1=进行中,2=已完成',
  `createtime` int DEFAULT NULL COMMENT '创建时间',
  `updatetime` int DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_production_task` (`production_task`),
  KEY `idx_production_order` (`production_order`),
  KEY `idx_org_id` (`org_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='车间计划主表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_mes_workshop_plan_process`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_mes_workshop_plan_process` (
  `id` int NOT NULL AUTO_INCREMENT,
  `plan_id` int NOT NULL COMMENT '车间计划ID',
  `process_line_no` varchar(50) DEFAULT NULL COMMENT '工序行号',
  `process_code` varchar(100) DEFAULT NULL COMMENT '工序编码',
  `process_name` varchar(255) DEFAULT NULL COMMENT '工序名称',
  `work_center` varchar(100) DEFAULT NULL COMMENT '工作中心',
  `workstation` varchar(100) DEFAULT NULL COMMENT '工位',
  `is_wip_traceable` enum('0','1') DEFAULT '0' COMMENT '是否在制品追溯:0=否,1=是',
  `is_process_inspection` enum('0','1') DEFAULT '0' COMMENT '是否工序检验:0=否,1=是',
  `is_first_article_inspection` enum('0','1') DEFAULT '0' COMMENT '首件检:0=否,1=是',
  `is_first_process` enum('0','1') DEFAULT '0' COMMENT '是否首道工序:0=否,1=是',
  `is_last_process` enum('0','1') DEFAULT '0' COMMENT '是否末道工序:0=否,1=是',
  `process_plan_start` datetime DEFAULT NULL COMMENT '工序计划开始时间',
  `process_plan_end` datetime DEFAULT NULL COMMENT '工序计划完工时间',
  `production_quantity` double(15,4) DEFAULT '0.0000' COMMENT '生产数量',
  `dispatched_quantity` double(15,4) DEFAULT '0.0000' COMMENT '派工数量',
  `reported_quantity` double(15,4) DEFAULT '0.0000' COMMENT '报工数量',
  `qualified_quantity` double(15,4) DEFAULT '0.0000' COMMENT '合格数量',
  `unqualified_quantity` double(15,4) DEFAULT '0.0000' COMMENT '不合格数量',
  `yield_rate` decimal(5,2) DEFAULT '0.00' COMMENT '得率(%)',
  `createtime` int DEFAULT NULL COMMENT '创建时间',
  `updatetime` int DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_plan_id` (`plan_id`),
  KEY `idx_process_code` (`process_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='车间计划工序资料表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_mes_writeoff`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_mes_writeoff` (
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
-- Table structure for table `hou_mes_writeoff_info`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_mes_writeoff_info` (
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
-- Table structure for table `hou_mes_writeoff_log`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_mes_writeoff_log` (
  `id` int NOT NULL AUTO_INCREMENT,
  `admin_id` int DEFAULT NULL,
  `source_id` int DEFAULT NULL COMMENT '源单ID',
  `write_off` double(15,4) DEFAULT NULL COMMENT '未核销金额',
  `money` double(15,4) DEFAULT NULL COMMENT '已核销金额',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='核销单据信息';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_mes_writeoff_source`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_mes_writeoff_source` (
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

SET FOREIGN_KEY_CHECKS=1;
