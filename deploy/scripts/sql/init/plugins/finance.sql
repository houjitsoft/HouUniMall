-- =====================================================================
-- 插件 finance 的数据库建表脚本（共 11 张表）
-- 用途：安装「finance」插件后按需执行（也可由插件自身的迁移通道建表）。
-- 🔴 商城核心（Core）初始化不包含本文件；未安装该插件时不要执行。
-- 全部 CREATE TABLE 已统一补 IF NOT EXISTS（幂等，可重复执行，不会删数据）。
-- 执行期间临时关闭外键检查：插件之间存在跨插件外键（如 oa 引用 hou_pm_project），
-- 未同时安装被引用插件时允许该外键暂时悬空。
-- =====================================================================

SET FOREIGN_KEY_CHECKS=0;

-- ---------- 基础表结构（结构基线） ----------
-- Table structure for table `hou_finance_accounts_payable`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_finance_accounts_payable` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `org_id` varchar(50) NOT NULL COMMENT '组织ID',
  `supplier_id` int DEFAULT NULL COMMENT '供应商ID',
  `supplier_name` varchar(255) DEFAULT NULL COMMENT '供应商名称',
  `code` varchar(100) NOT NULL COMMENT '单据编号',
  `business_date` date DEFAULT NULL COMMENT '业务日期',
  `business_type` varchar(50) DEFAULT NULL COMMENT '业务类型:采购,退货,其他',
  `total_amount` decimal(15,2) NOT NULL DEFAULT '0.00' COMMENT '应付总金额',
  `paid_amount` decimal(15,2) NOT NULL DEFAULT '0.00' COMMENT '已付金额',
  `unpaid_amount` decimal(15,2) NOT NULL DEFAULT '0.00' COMMENT '未付金额',
  `status` tinyint NOT NULL DEFAULT '0' COMMENT '状态:0=未付款,1=部分付款,2=已付款',
  `due_date` date DEFAULT NULL COMMENT '到期日期',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  `create_by` int DEFAULT NULL COMMENT '创建人',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` int DEFAULT NULL COMMENT '更新人',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `del_flag` tinyint NOT NULL DEFAULT '0' COMMENT '删除标志:0=正常,1=删除',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_org_id` (`org_id`) USING BTREE,
  KEY `idx_supplier_id` (`supplier_id`) USING BTREE,
  KEY `idx_code` (`code`) USING BTREE,
  KEY `idx_status` (`status`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='应付账款表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_finance_bookkeeping_income_budget`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_finance_bookkeeping_income_budget` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `yer` year NOT NULL DEFAULT '2020' COMMENT '年份',
  `mth` enum('1','2','3','4','5','6','7','8','9','10','11','12') NOT NULL DEFAULT '1' COMMENT '月份',
  `amount` decimal(10,2) NOT NULL DEFAULT '0.00' COMMENT '目标收入',
  `create_time` int DEFAULT NULL COMMENT '创建时间',
  `update_time` int DEFAULT NULL COMMENT '更新时间',
  `status` varchar(255) DEFAULT NULL COMMENT '状态',
  `delete_time` int DEFAULT NULL COMMENT '删除时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 COMMENT='收入预算';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_finance_bookkeeping_income_log`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_finance_bookkeeping_income_log` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `group_id` int unsigned NOT NULL DEFAULT '0' COMMENT '部门',
  `subjects_id` mediumint unsigned NOT NULL DEFAULT '0' COMMENT '支付账户',
  `org_id` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '组织机构id',
  `type_id` mediumint unsigned NOT NULL DEFAULT '0' COMMENT '收入类别',
  `datatime` int DEFAULT NULL COMMENT '账单时间',
  `create_time` int DEFAULT NULL COMMENT '创建时间',
  `create_id` int unsigned DEFAULT NULL COMMENT '创建人',
  `audittime` int DEFAULT NULL COMMENT '审批时间',
  `audit_id` int unsigned DEFAULT NULL COMMENT '审批人',
  `amount` decimal(10,2) NOT NULL DEFAULT '0.00' COMMENT '金额',
  `status` enum('unaudited','audited') NOT NULL DEFAULT 'unaudited' COMMENT '状态',
  `proofimage` text COMMENT '凭证',
  `ocr_content` mediumtext COMMENT '附件OCR识别全文',
  `desc` varchar(255) NOT NULL DEFAULT '' COMMENT '详细事项',
  `update_time` int DEFAULT NULL COMMENT '更新时间',
  `delete_time` bigint DEFAULT NULL COMMENT '删除时间',
  `contract_id` int DEFAULT NULL COMMENT '合同ID',
  `project_id` int DEFAULT NULL COMMENT '项目ID',
  `supplier_id` int DEFAULT NULL COMMENT '服务商ID',
  PRIMARY KEY (`id`),
  KEY `group_id` (`group_id`),
  KEY `subjects_id` (`subjects_id`),
  KEY `type_id` (`type_id`),
  KEY `create_id` (`create_id`),
  KEY `audit_id` (`audit_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 COMMENT='收入表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_finance_bookkeeping_income_type`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_finance_bookkeeping_income_type` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `name` varchar(50) NOT NULL DEFAULT '' COMMENT '名称',
  `descr` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL DEFAULT '' COMMENT '描述',
  `status` tinyint DEFAULT '1' COMMENT '是否停用: [0=否, 1=是]',
  `create_time` int DEFAULT NULL COMMENT '创建时间',
  `update_time` int DEFAULT NULL COMMENT '删除时间',
  `delete_time` int DEFAULT NULL COMMENT '删除时间',
  `org_id` varchar(50) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 COMMENT='收入类别';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_finance_bookkeeping_spending_budget`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_finance_bookkeeping_spending_budget` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `yer` year NOT NULL DEFAULT '2020' COMMENT '年份',
  `mth` enum('1','2','3','4','5','6','7','8','9','10','11','12') NOT NULL DEFAULT '1' COMMENT '月份',
  `amount` decimal(10,2) NOT NULL DEFAULT '0.00' COMMENT '预计支出',
  `create_time` int DEFAULT NULL COMMENT '创建时间',
  `update_time` int DEFAULT NULL COMMENT '更新时间',
  `delete_time` int DEFAULT NULL COMMENT '删除时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 COMMENT='支出预算';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_finance_bookkeeping_spending_log`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_finance_bookkeeping_spending_log` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `group_id` int unsigned NOT NULL DEFAULT '0' COMMENT '部门',
  `subjects_id` mediumint unsigned NOT NULL DEFAULT '0' COMMENT '支付账户',
  `type_id` mediumint unsigned NOT NULL DEFAULT '0' COMMENT '支出类别',
  `org_id` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '组织机构ID',
  `datatime` int DEFAULT NULL COMMENT '账单时间',
  `create_time` int DEFAULT NULL COMMENT '创建时间',
  `create_id` int unsigned DEFAULT NULL COMMENT '创建人',
  `audittime` int DEFAULT NULL COMMENT '审批时间',
  `audit_id` int unsigned DEFAULT NULL COMMENT '审批人',
  `amount` decimal(10,2) NOT NULL DEFAULT '0.00' COMMENT '金额',
  `status` enum('unaudited','audited') NOT NULL DEFAULT 'unaudited' COMMENT '状态',
  `proofimage` text COMMENT '凭证',
  `ocr_content` mediumtext COMMENT '附件OCR识别全文',
  `desc` varchar(255) NOT NULL DEFAULT '' COMMENT '详细事项',
  `update_time` int DEFAULT NULL COMMENT '更新时间',
  `delete_time` bigint DEFAULT NULL COMMENT '删除时间',
  `project_id` int DEFAULT NULL COMMENT '项目ID',
  `contract_id` int DEFAULT NULL COMMENT '合同ID',
  `supplier_id` int DEFAULT NULL COMMENT '服务商ID',
  PRIMARY KEY (`id`),
  KEY `group_id` (`group_id`),
  KEY `subjects_id` (`subjects_id`),
  KEY `type_id` (`type_id`),
  KEY `create_id` (`create_id`),
  KEY `audit_id` (`audit_id`),
  KEY `idx_org_id` (`org_id`(100)),
  KEY `idx_delete_time` (`delete_time`),
  KEY `idx_datatime` (`datatime`),
  KEY `idx_contract_id` (`contract_id`),
  KEY `idx_project_id` (`project_id`),
  KEY `idx_supplier_id` (`supplier_id`),
  KEY `idx_org_create_id` (`org_id`,`create_time`,`id`),
  KEY `idx_spending_contract_status_del` (`contract_id`,`status`,`delete_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 COMMENT='支出表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_finance_bookkeeping_spending_type`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_finance_bookkeeping_spending_type` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `name` varchar(50) NOT NULL DEFAULT '' COMMENT '名称',
  `descr` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL DEFAULT '' COMMENT '描述',
  `status` tinyint DEFAULT '1' COMMENT '是否停用: [0=否, 1=是]',
  `delete_time` int DEFAULT NULL COMMENT '删除时间',
  `create_time` int DEFAULT NULL COMMENT '创建时间',
  `update_time` int DEFAULT NULL COMMENT '更新时间',
  `org_id` varchar(50) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 COMMENT='支出类别';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_finance_bookkeeping_subjects`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_finance_bookkeeping_subjects` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `name` varchar(50) NOT NULL DEFAULT '' COMMENT '名称',
  `descr` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL DEFAULT '' COMMENT '描述',
  `delete_time` int DEFAULT NULL,
  `create_time` int DEFAULT NULL,
  `update_time` int DEFAULT NULL,
  `status` tinyint DEFAULT '1' COMMENT '是否停用: [0=否, 1=是]',
  `org_id` varchar(50) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 COMMENT='支付账户';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_finance_invoice`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_finance_invoice` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `org_id` varchar(50) NOT NULL COMMENT '组织ID',
  `invoice_no` varchar(100) NOT NULL COMMENT '发票号',
  `customer_id` int DEFAULT NULL COMMENT '客户ID',
  `customer_name` varchar(255) DEFAULT NULL COMMENT '客户名称',
  `invoice_type` tinyint NOT NULL COMMENT '发票类型:1=增值税专用发票,2=增值税普通发票,3=电子发票',
  `amount` decimal(15,2) NOT NULL COMMENT '开票金额',
  `tax_rate` decimal(5,2) NOT NULL COMMENT '税率',
  `tax_amount` decimal(15,2) DEFAULT NULL COMMENT '税额',
  `invoice_date` date DEFAULT NULL COMMENT '开票日期',
  `status` tinyint NOT NULL DEFAULT '0' COMMENT '状态:0=待开票,1=已开票,2=已作废',
  `invoice_content` varchar(500) DEFAULT NULL COMMENT '发票内容',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  `create_by` int DEFAULT NULL COMMENT '创建人',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` int DEFAULT NULL COMMENT '更新人',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `del_flag` tinyint NOT NULL DEFAULT '0' COMMENT '删除标志:0=正常,1=删除',
  `source_type` varchar(32) DEFAULT NULL COMMENT '来源类型 income/spending',
  `source_id` bigint DEFAULT NULL COMMENT '来源账簿记录ID',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_org_id` (`org_id`) USING BTREE,
  KEY `idx_invoice_no` (`invoice_no`) USING BTREE,
  KEY `idx_customer_id` (`customer_id`) USING BTREE,
  KEY `idx_finance_invoice_source` (`source_type`,`source_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='发票管理表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_finance_payment_record`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_finance_payment_record` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `org_id` varchar(50) NOT NULL COMMENT '组织ID',
  `payment_no` varchar(100) NOT NULL COMMENT '收款单号',
  `customer_id` int DEFAULT NULL COMMENT '客户ID',
  `customer_name` varchar(255) DEFAULT NULL COMMENT '客户名称',
  `amount` decimal(15,2) NOT NULL COMMENT '收款金额',
  `payment_method` tinyint NOT NULL COMMENT '收款方式:1=银行转账,2=支付宝,3=微信支付,4=现金',
  `payment_date` date DEFAULT NULL COMMENT '收款日期',
  `status` tinyint NOT NULL DEFAULT '0' COMMENT '状态:0=待确认,1=已确认,2=已作废',
  `bank_account` varchar(100) DEFAULT NULL COMMENT '银行账号',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  `create_by` int DEFAULT NULL COMMENT '创建人',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` int DEFAULT NULL COMMENT '更新人',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `del_flag` tinyint NOT NULL DEFAULT '0' COMMENT '删除标志:0=正常,1=删除',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_org_id` (`org_id`) USING BTREE,
  KEY `idx_payment_no` (`payment_no`) USING BTREE,
  KEY `idx_customer_id` (`customer_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='收款记录表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_finance_tax`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_finance_tax` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `org_id` varchar(50) NOT NULL,
  `name` varchar(100) NOT NULL,
  `status` tinyint NOT NULL DEFAULT '1',
  `remark` varchar(500) DEFAULT NULL,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `delete_flag` tinyint NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `idx_finance_tax_org` (`org_id`,`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--

-- Finance fund source links provide explicit, tenant-scoped idempotency for
-- OA contract sync, payable spending vouchers, and payment income vouchers.
CREATE TABLE IF NOT EXISTS `hou_finance_fund_source_link` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `org_id` varchar(50) NOT NULL,
  `source_type` varchar(32) NOT NULL,
  `source_id` bigint NOT NULL,
  `target_type` varchar(32) NOT NULL,
  `target_id` bigint NOT NULL,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_fund_source_target` (`org_id`,`source_type`,`source_id`,`target_type`),
  UNIQUE KEY `uk_fund_target` (`org_id`,`target_type`,`target_id`),
  KEY `idx_fund_source_link_target` (`target_type`,`target_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='财务业务来源关联';

SET FOREIGN_KEY_CHECKS=1;
